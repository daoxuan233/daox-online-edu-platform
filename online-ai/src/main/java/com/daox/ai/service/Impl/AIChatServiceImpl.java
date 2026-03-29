package com.daox.ai.service.Impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.daox.ai.config.AIGovernanceProperties;
import com.daox.ai.entity.dto.ConversationSummaryDTO;
import com.daox.ai.entity.mongodb.AiCallRecord;
import com.daox.ai.entity.mongodb.AiQuotaPolicy;
import com.daox.ai.entity.mongodb.AiRuntimeStrategy;
import com.daox.ai.entity.mongodb.ChatMessage;
import com.daox.ai.entity.request.ModelPlatformOptions;
import com.daox.ai.entity.response.AIResponse;
import com.daox.ai.repository.AiCallRecordRepository;
import com.daox.ai.repository.AiQuotaPolicyRepository;
import com.daox.ai.repository.AiRuntimeStrategyRepository;
import com.daox.ai.repository.ChatMessageRepository;
import com.daox.ai.service.AIChatService;
import com.daox.ai.utils.Const;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI 对话服务实现。
 * <p>
 * 该实现统一接入以下治理能力：
 * <ul>
 *     <li>管理员视角的调用统计与审计落库</li>
 *     <li>用户 / 角色 / 全局三级额度控制</li>
 *     <li>输入与输出双向内容审查</li>
 *     <li>按平台顺序执行的重试与降级策略</li>
 * </ul>
 */
@Slf4j
@Service
public class AIChatServiceImpl implements AIChatService {

    private static final int HISTORY_SIZE = 15;
    private static final int STREAM_CHUNK_SIZE = 120;

    @Resource
    private ChatClient chatClient;

    @Resource
    private ChatMessageRepository chatMessageRepository;

    @Resource
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AiCallRecordRepository aiCallRecordRepository;

    @Resource
    private AiQuotaPolicyRepository aiQuotaPolicyRepository;

    @Resource
    private AiRuntimeStrategyRepository aiRuntimeStrategyRepository;

    @Resource
    private AIGovernanceProperties aiGovernanceProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<AIResponse> streamChatAndSave(String userId, String userRole, String question,
                                              String conversationId, ModelPlatformOptions options) {
        String normalizedRole = normalizeRole(userRole);
        String normalizedQuestion = question == null ? "" : question.trim();
        String validatedConversationId = validateConversationId(userId, conversationId);
        String requestedPlatform = options == null ? null : normalizePlatform(options.getPlatform());
        String requestedModel = options == null ? null : trimToNull(options.getModel());
        String requestId = UUID.randomUUID().toString();
        LocalDateTime startedAt = LocalDateTime.now();
        long startNanos = System.nanoTime();

        if (normalizedQuestion.isBlank()) {
            return buildTerminalMessage("提问内容不能为空");
        }

        Mono<List<Message>> historyMono = loadHistoryMessages(validatedConversationId);
        Mono<AiRuntimeStrategy> strategyMono = loadRuntimeStrategy();
        Mono<AiQuotaPolicy> quotaPolicyMono = resolveQuotaPolicy(userId, normalizedRole);

        return Mono.zip(historyMono, strategyMono, quotaPolicyMono)
                .flatMapMany(tuple -> {
                    List<Message> history = tuple.getT1();
                    AiRuntimeStrategy strategy = tuple.getT2();
                    AiQuotaPolicy quotaPolicy = tuple.getT3();

                    ModerationResult inputModeration = inspectText(normalizedQuestion, strategy, "输入");
                    if (inputModeration.blocked()) {
                        return rejectBeforeExecution(requestId, userId, normalizedRole, validatedConversationId,
                                requestedPlatform, requestedModel, normalizedQuestion, inputModeration,
                                startedAt, startNanos,
                                "请求命中内容审查规则，已进入管理员审查队列。", 0, false,
                                QuotaSnapshot.fromPolicy(quotaPolicy));
                    }

                    return checkAndConsumeQuota(userId, quotaPolicy)
                            .flatMapMany(quotaSnapshot -> {
                                if (!quotaSnapshot.allowed()) {
                                    return rejectBeforeExecution(requestId, userId, normalizedRole, validatedConversationId,
                                            requestedPlatform, requestedModel, normalizedQuestion,
                                            new ModerationResult(false, inputModeration.reviewStatus(), inputModeration.reason()),
                                            startedAt, startNanos, quotaSnapshot.rejectReason(), 0, false, quotaSnapshot);
                                }

                                List<String> candidates = buildPlatformCandidates(requestedPlatform, strategy);
                                int maxAttempts = Math.min(Math.max(strategy.getMaxAttempts(), 1), candidates.size());

                                return executeWithFallback(history, normalizedQuestion, requestedModel, candidates, maxAttempts)
                                        .flatMapMany(outcome -> handleExecutionSuccess(requestId, userId, normalizedRole,
                                                validatedConversationId, requestedPlatform, requestedModel,
                                                normalizedQuestion, inputModeration, quotaSnapshot,
                                                strategy, startedAt, startNanos, outcome))
                                        .onErrorResume(error -> handleExecutionFailure(requestId, userId, normalizedRole,
                                                validatedConversationId, requestedPlatform, requestedModel,
                                                normalizedQuestion, inputModeration, quotaSnapshot,
                                                startedAt, startNanos, error, maxAttempts));
                            });
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<List<ConversationSummaryDTO>> getConversationSummaries(String userId) {
        String conversationPrefix = Const.AI_ASSISTANT_CONVERSATION + userId + "_";
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("conversation_id").regex("^" + conversationPrefix)),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "conversation_id", "timestamp")),
                Aggregation.group("conversation_id")
                        .first("content").as("title")
                        .last("timestamp").as("lastMessageTime"),
                Aggregation.project()
                        .and("_id").as("conversationId")
                        .and("title").as("title")
                        .and("lastMessageTime").as("lastMessageTime")
        );
        return reactiveMongoTemplate.aggregate(aggregation, "chat_messages", ConversationSummaryDTO.class).collectList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<List<ChatMessage>> getMessagesForConversation(String conversationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return chatMessageRepository.findByConversationId(conversationId, pageable).collectList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> deleteConversation(String userId, String conversationId) {
        String expectedPrefix = Const.AI_ASSISTANT_CONVERSATION + userId + "_";
        if (conversationId == null || !conversationId.startsWith(expectedPrefix)) {
            return Mono.error(new IllegalArgumentException("无权删除该会话或会话ID无效"));
        }
        return chatMessageRepository.deleteByConversationId(conversationId);
    }

    /**
     * 处理模型调用成功后的分支。
     *
     * @return 返回给前端的响应流
     */
    private Flux<AIResponse> handleExecutionSuccess(String requestId, String userId, String userRole,
                                                    String conversationId, String requestedPlatform,
                                                    String requestedModel, String question,
                                                    ModerationResult inputModeration,
                                                    QuotaSnapshot quotaSnapshot, AiRuntimeStrategy strategy,
                                                    LocalDateTime startedAt, long startNanos,
                                                    ExecutionOutcome outcome) {
        ModerationResult outputModeration = inspectText(outcome.content(), strategy, "输出");
        long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

        if (outputModeration.blocked()) {
            AiCallRecord record = buildBaseRecord(requestId, userId, userRole, conversationId, requestedPlatform,
                    outcome.platform(), outcome.model(), question, outcome.content(), latencyMs, startedAt,
                    quotaSnapshot, outcome.attemptCount(), outcome.degraded())
                    .setCallStatus(AiCallRecord.CallStatus.REJECTED)
                    .setReviewStatus(AiCallRecord.ReviewStatus.PENDING)
                    .setReviewReason(outputModeration.reason())
                    .setFailureReason("模型输出命中内容审查规则")
                    .setCompletedAt(LocalDateTime.now());
            return aiCallRecordRepository.save(record)
                    .thenMany(buildTerminalMessage("模型响应命中审查规则，结果已被拦截并进入管理员审查队列。"));
        }

        AiCallRecord.ReviewStatus finalReviewStatus = mergeReviewStatus(inputModeration, outputModeration);
        String finalReviewReason = mergeReviewReason(inputModeration, outputModeration);

        return saveAiChatMessages(userId, question, outcome.content(), conversationId)
                .flatMapMany(savedTuple -> {
                    AiCallRecord record = buildBaseRecord(requestId, userId, userRole, savedTuple.getT1(), requestedPlatform,
                            outcome.platform(), outcome.model(), question, outcome.content(), latencyMs, startedAt,
                            quotaSnapshot, outcome.attemptCount(), outcome.degraded())
                            .setCallStatus(AiCallRecord.CallStatus.SUCCESS)
                            .setReviewStatus(finalReviewStatus)
                            .setReviewReason(finalReviewReason)
                            .setCompletedAt(LocalDateTime.now());
                    return aiCallRecordRepository.save(record)
                            .thenMany(buildSuccessResponses(outcome.content(), savedTuple.getT2()));
                });
    }

    /**
     * 处理模型调用失败后的分支。
     *
     * @return 返回给前端的响应流
     */
    private Flux<AIResponse> handleExecutionFailure(String requestId, String userId, String userRole,
                                                    String conversationId, String requestedPlatform,
                                                    String requestedModel, String question,
                                                    ModerationResult inputModeration,
                                                    QuotaSnapshot quotaSnapshot, LocalDateTime startedAt,
                                                    long startNanos, Throwable error, int attemptCount) {
        long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        AiCallRecord record = buildBaseRecord(requestId, userId, userRole, conversationId, requestedPlatform,
                null, requestedModel, question, null, latencyMs, startedAt, quotaSnapshot, attemptCount, false)
                .setCallStatus(AiCallRecord.CallStatus.FAILED)
                .setReviewStatus(inputModeration.reviewStatus())
                .setReviewReason(inputModeration.reason())
                .setFailureReason(error.getMessage())
                .setCompletedAt(LocalDateTime.now());
        return aiCallRecordRepository.save(record)
                .thenMany(buildTerminalMessage("AI 服务暂时不可用，已按降级策略重试失败，请稍后再试。"));
    }

    /**
     * 在调用真正发生前，统一处理拒绝场景。
     *
     * @return 返回给前端的响应流
     */
    private Flux<AIResponse> rejectBeforeExecution(String requestId, String userId, String userRole,
                                                   String conversationId, String requestedPlatform,
                                                   String requestedModel, String question,
                                                   ModerationResult moderationResult,
                                                   LocalDateTime startedAt, long startNanos,
                                                   String terminalMessage, int attemptCount,
                                                   boolean degraded, QuotaSnapshot quotaSnapshot) {
        long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        AiCallRecord record = buildBaseRecord(requestId, userId, userRole, conversationId, requestedPlatform,
                null, requestedModel, question, null, latencyMs, startedAt, quotaSnapshot, attemptCount, degraded)
                .setCallStatus(AiCallRecord.CallStatus.REJECTED)
                .setReviewStatus(moderationResult.reviewStatus())
                .setReviewReason(moderationResult.reason())
                .setFailureReason(terminalMessage)
                .setCompletedAt(LocalDateTime.now());
        return aiCallRecordRepository.save(record)
                .thenMany(buildTerminalMessage(terminalMessage));
    }

    /**
     * 构建统一的调用审计对象。
     *
     * @return 调用审计记录
     */
    private AiCallRecord buildBaseRecord(String requestId, String userId, String userRole,
                                         String conversationId, String requestedPlatform,
                                         String finalPlatform, String model, String question,
                                         String responseContent, long latencyMs,
                                         LocalDateTime startedAt, QuotaSnapshot quotaSnapshot,
                                         int attemptCount, boolean degraded) {
        return new AiCallRecord()
                .setRequestId(requestId)
                .setUserId(userId)
                .setUserRole(userRole)
                .setConversationId(conversationId)
                .setRequestedPlatform(requestedPlatform)
                .setFinalPlatform(finalPlatform)
                .setModel(model)
                .setQuestionContent(question)
                .setResponseContent(responseContent)
                .setAttemptCount(attemptCount)
                .setDegraded(degraded)
                .setQuotaDailyLimit(quotaSnapshot.dailyLimit())
                .setQuotaDailyUsed(quotaSnapshot.dailyUsed())
                .setQuotaHourlyLimit(quotaSnapshot.hourlyLimit())
                .setQuotaHourlyUsed(quotaSnapshot.hourlyUsed())
                .setLatencyMs(latencyMs)
                .setCreatedAt(startedAt);
    }

    /**
     * 载入历史消息。
     *
     * @param conversationId 会话 ID
     * @return 历史消息列表
     */
    private Mono<List<Message>> loadHistoryMessages(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return Mono.just(Collections.emptyList());
        }
        return getMessagesForConversation(conversationId, 0, HISTORY_SIZE)
                .map(chatMessages -> {
                    Collections.reverse(chatMessages);
                    return chatMessages.stream()
                            .<Message>map(message -> Const.AI_ASSISTANT.equals(message.getSenderId())
                                    ? new AssistantMessage(message.getContent())
                                    : new UserMessage(message.getContent()))
                            .collect(Collectors.toList());
                });
    }

    /**
     * 执行平台级重试与降级。
     *
     * @return 最终成功结果
     */
    private Mono<ExecutionOutcome> executeWithFallback(List<Message> history, String question,
                                                       String requestedModel, List<String> candidates,
                                                       int maxAttempts) {
        return executeWithFallback(history, question, requestedModel, candidates, maxAttempts, 0);
    }

    /**
     * 递归执行降级调用。
     *
     * @return 最终成功结果
     */
    private Mono<ExecutionOutcome> executeWithFallback(List<Message> history, String question,
                                                       String requestedModel, List<String> candidates,
                                                       int maxAttempts, int index) {
        if (index >= maxAttempts || index >= candidates.size()) {
            return Mono.error(new IllegalStateException("所有 AI 平台重试均失败"));
        }
        String platform = candidates.get(index);
        String model = resolveModel(platform, requestedModel);
        return invokePlatform(platform, model, history, question)
                .map(content -> new ExecutionOutcome(platform, model, content, index + 1, index > 0))
                .onErrorResume(error -> executeWithFallback(history, question, requestedModel, candidates, maxAttempts, index + 1));
    }

    /**
     * 具体调用某个平台。
     *
     * @return 模型完整回答
     */
    private Mono<String> invokePlatform(String platform, String model, List<Message> history, String question) {
        return Mono.fromCallable(() -> switch (platform) {
                    case "deepseek" -> ChatClient.builder(DeepSeekChatModel.builder().build())
                            .defaultOptions(ChatOptions.builder().model(model).build())
                            .build()
                            .prompt()
                            .messages(history)
                            .user(question)
                            .call()
                            .content();
                    case "qwen" -> ChatClient.builder(DashScopeChatModel.builder().build())
                            .defaultOptions(ChatOptions.builder().model(model).build())
                            .build()
                            .prompt()
                            .messages(history)
                            .user(question)
                            .call()
                            .content();
                    default -> chatClient.prompt()
                            .messages(history)
                            .user(question)
                            .call()
                            .content();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(content -> {
                    if (content == null || content.isBlank()) {
                        return Mono.error(new IllegalStateException("模型返回了空响应"));
                    }
                    return Mono.just(content);
                });
    }

    /**
     * 加载运行时策略。
     *
     * @return 运行时策略
     */
    private Mono<AiRuntimeStrategy> loadRuntimeStrategy() {
        return aiRuntimeStrategyRepository.findById(AiRuntimeStrategy.DEFAULT_ID)
                .switchIfEmpty(Mono.just(defaultRuntimeStrategy()));
    }

    /**
     * 解析当前生效的配额策略。
     *
     * @return 配额策略
     */
    private Mono<AiQuotaPolicy> resolveQuotaPolicy(String userId, String userRole) {
        return aiQuotaPolicyRepository.findByScopeTypeAndScopeValueAndEnabledTrue(AiQuotaPolicy.ScopeType.USER, userId)
                .switchIfEmpty(aiQuotaPolicyRepository.findByScopeTypeAndScopeValueAndEnabledTrue(AiQuotaPolicy.ScopeType.ROLE, userRole))
                .switchIfEmpty(aiQuotaPolicyRepository.findByScopeTypeAndScopeValueAndEnabledTrue(AiQuotaPolicy.ScopeType.GLOBAL, "default"))
                .switchIfEmpty(Mono.just(defaultQuotaPolicy(userRole)));
    }

    /**
     * 检查并消耗额度。
     *
     * @return 额度快照
     */
    private Mono<QuotaSnapshot> checkAndConsumeQuota(String userId, AiQuotaPolicy quotaPolicy) {
        return Mono.fromSupplier(() -> {
            if (!Boolean.TRUE.equals(quotaPolicy.getEnabled())) {
                return QuotaSnapshot.unlimited();
            }
            String dailyKey = Const.AI_QUOTA_DAILY + userId + ":" + LocalDate.now();
            String hourlyKey = Const.AI_QUOTA_HOURLY + userId + ":" + LocalDate.now() + ":" + LocalTime.now().getHour();
            int currentDaily = currentCounter(dailyKey);
            int currentHourly = currentCounter(hourlyKey);

            if (quotaPolicy.getDailyLimit() != null && quotaPolicy.getDailyLimit() > 0 && currentDaily >= quotaPolicy.getDailyLimit()) {
                return new QuotaSnapshot(quotaPolicy.getDailyLimit(), currentDaily,
                        quotaPolicy.getHourlyLimit(), currentHourly, false,
                        "当前 AI 日额度已用尽，请稍后再试或联系管理员。");
            }
            if (quotaPolicy.getHourlyLimit() != null && quotaPolicy.getHourlyLimit() > 0 && currentHourly >= quotaPolicy.getHourlyLimit()) {
                return new QuotaSnapshot(quotaPolicy.getDailyLimit(), currentDaily,
                        quotaPolicy.getHourlyLimit(), currentHourly, false,
                        "当前 AI 小时额度已用尽，请稍后再试或联系管理员。");
            }

            int nextDaily = incrementCounter(dailyKey, secondsUntilTomorrow());
            int nextHourly = incrementCounter(hourlyKey, secondsUntilNextHour());
            return new QuotaSnapshot(quotaPolicy.getDailyLimit(), nextDaily,
                    quotaPolicy.getHourlyLimit(), nextHourly, true, null);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 对文本执行内容审查。
     *
     * @return 审查结果
     */
    private ModerationResult inspectText(String text, AiRuntimeStrategy strategy, String direction) {
        if (strategy == null || !Boolean.TRUE.equals(strategy.getReviewEnabled()) || text == null || text.isBlank()) {
            return new ModerationResult(false, AiCallRecord.ReviewStatus.PASSED, null);
        }

        String blockedKeyword = findMatchedKeyword(text, strategy.getBlockedKeywords());
        if (blockedKeyword != null) {
            return new ModerationResult(true, AiCallRecord.ReviewStatus.PENDING,
                    direction + "命中阻断词: " + blockedKeyword);
        }

        String reviewKeyword = findMatchedKeyword(text, strategy.getReviewKeywords());
        if (reviewKeyword != null) {
            return new ModerationResult(false, AiCallRecord.ReviewStatus.PENDING,
                    direction + "命中人工审查词: " + reviewKeyword);
        }

        return new ModerationResult(false, AiCallRecord.ReviewStatus.PASSED, null);
    }

    /**
     * 合并输入输出的审查状态。
     *
     * @return 最终审查状态
     */
    private AiCallRecord.ReviewStatus mergeReviewStatus(ModerationResult inputModeration, ModerationResult outputModeration) {
        if (inputModeration.reviewStatus() == AiCallRecord.ReviewStatus.PENDING
                || outputModeration.reviewStatus() == AiCallRecord.ReviewStatus.PENDING) {
            return AiCallRecord.ReviewStatus.PENDING;
        }
        return AiCallRecord.ReviewStatus.PASSED;
    }

    /**
     * 合并输入输出的审查原因。
     *
     * @return 合并后的原因
     */
    private String mergeReviewReason(ModerationResult inputModeration, ModerationResult outputModeration) {
        List<String> reasons = new ArrayList<>();
        if (inputModeration.reason() != null && !inputModeration.reason().isBlank()) {
            reasons.add(inputModeration.reason());
        }
        if (outputModeration.reason() != null && !outputModeration.reason().isBlank()) {
            reasons.add(outputModeration.reason());
        }
        return reasons.isEmpty() ? null : String.join("; ", reasons);
    }

    /**
     * 构建平台尝试顺序。
     *
     * @return 去重后的平台列表
     */
    private List<String> buildPlatformCandidates(String requestedPlatform, AiRuntimeStrategy strategy) {
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        if (requestedPlatform != null && !requestedPlatform.isBlank()) {
            ordered.add(normalizePlatform(requestedPlatform));
        }
        List<String> fallbackPlatforms = strategy == null ? List.of() : strategy.getFallbackPlatforms();
        for (String platform : fallbackPlatforms) {
            ordered.add(normalizePlatform(platform));
        }
        if (ordered.isEmpty()) {
            ordered.add("openai");
            ordered.add("deepseek");
            ordered.add("qwen");
        }
        return new ArrayList<>(ordered);
    }

    /**
     * 构建成功响应流。
     *
     * @return 返回给前端的流式响应
     */
    private Flux<AIResponse> buildSuccessResponses(String content, String aiMessageId) {
        Flux<AIResponse> contentFlux = Flux.fromIterable(splitContent(content, STREAM_CHUNK_SIZE))
                .map(chunk -> AIResponse.builder().content(chunk).build());
        return Flux.concat(contentFlux, Mono.just(AIResponse.builder().recordId(aiMessageId).content("").build()));
    }

    /**
     * 构建单条提示消息。
     *
     * @return 单条响应流
     */
    private Flux<AIResponse> buildTerminalMessage(String message) {
        return Flux.just(AIResponse.builder().content(message).build());
    }

    /**
     * 保存聊天记录。
     *
     * @return 返回会话 ID 与 AI 消息 ID
     */
    private Mono<Tuple2<String, String>> saveAiChatMessages(String userId, String question, String aiResponse, String conversationId) {
        final String currentConversationId = (conversationId == null || conversationId.isBlank())
                ? Const.AI_ASSISTANT_CONVERSATION + userId + "_" + UUID.randomUUID()
                : conversationId;

        ChatMessage userMessage = new ChatMessage()
                .setConversationId(currentConversationId)
                .setSenderId(userId)
                .setReceiverId(Const.AI_ASSISTANT)
                .setMessageType(ChatMessage.MessageType.AI)
                .setContentType(ChatMessage.ContentType.TEXT)
                .setContent(question)
                .setTimestamp(LocalDateTime.now())
                .setStatus(ChatMessage.MessageStatus.READ);

        ChatMessage aiMessage = new ChatMessage()
                .setConversationId(currentConversationId)
                .setSenderId(Const.AI_ASSISTANT)
                .setReceiverId(userId)
                .setMessageType(ChatMessage.MessageType.AI)
                .setContentType(ChatMessage.ContentType.RICH_TEXT)
                .setContent(aiResponse)
                .setTimestamp(LocalDateTime.now().plusNanos(1_000_000))
                .setStatus(ChatMessage.MessageStatus.DELIVERED);

        return chatMessageRepository.saveAll(List.of(userMessage, aiMessage))
                .filter(chatMessage -> Const.AI_ASSISTANT.equals(chatMessage.getSenderId()))
                .next()
                .map(aiMsg -> Tuples.of(currentConversationId, aiMsg.getId()));
    }

    /**
     * 校验会话 ID 的归属。
     *
     * @return 合法会话 ID，非法时返回 null
     */
    private String validateConversationId(String userId, String conversationId) {
        String validatedConversationId = trimToNull(conversationId);
        if (validatedConversationId == null) {
            return null;
        }
        String conversationPrefix = Const.AI_ASSISTANT_CONVERSATION + userId + "_";
        if (!validatedConversationId.startsWith(conversationPrefix)) {
            log.warn("[streamChatAndSave.method] 传入的会话ID {} 无效或不属于当前用户 {}，将作为新会话处理。", validatedConversationId, userId);
            return null;
        }
        return validatedConversationId;
    }

    /**
     * 构建默认运行时策略。
     *
     * @return 默认策略
     */
    private AiRuntimeStrategy defaultRuntimeStrategy() {
        return new AiRuntimeStrategy()
                .setId(AiRuntimeStrategy.DEFAULT_ID)
                .setReviewEnabled(aiGovernanceProperties.getReview().isEnabled())
                .setBlockedKeywords(aiGovernanceProperties.getReview().getBlockedKeywords())
                .setReviewKeywords(aiGovernanceProperties.getReview().getReviewKeywords())
                .setMaxAttempts(Math.max(aiGovernanceProperties.getRetry().getMaxAttempts(), 1))
                .setFallbackPlatforms(aiGovernanceProperties.getRetry().getFallbackPlatforms());
    }

    /**
     * 构建默认配额策略。
     *
     * @return 默认配额策略
     */
    private AiQuotaPolicy defaultQuotaPolicy(String userRole) {
        AIGovernanceProperties.Quota quota = aiGovernanceProperties.getQuota();
        AiQuotaPolicy policy = new AiQuotaPolicy()
                .setScopeType(AiQuotaPolicy.ScopeType.GLOBAL)
                .setScopeValue("default")
                .setEnabled(true);
        switch (userRole) {
            case Const.ROLE_ADMIN -> {
                policy.setDailyLimit(quota.getAdminDailyLimit());
                policy.setHourlyLimit(quota.getAdminHourlyLimit());
            }
            case Const.ROLE_TEACHER -> {
                policy.setDailyLimit(quota.getTeacherDailyLimit());
                policy.setHourlyLimit(quota.getTeacherHourlyLimit());
            }
            case Const.ROLE_STUDENT -> {
                policy.setDailyLimit(quota.getStudentDailyLimit());
                policy.setHourlyLimit(quota.getStudentHourlyLimit());
            }
            default -> {
                policy.setDailyLimit(quota.getGlobalDailyLimit());
                policy.setHourlyLimit(quota.getGlobalHourlyLimit());
            }
        }
        return policy;
    }

    /**
     * 标准化角色。
     *
     * @return 标准化后的角色
     */
    private String normalizeRole(String userRole) {
        if (userRole == null || userRole.isBlank()) {
            return "unknown";
        }
        return userRole.toLowerCase(Locale.ROOT);
    }

    /**
     * 标准化平台。
     *
     * @return 标准化后的平台名
     */
    private String normalizePlatform(String platform) {
        if (platform == null || platform.isBlank()) {
            return "openai";
        }
        String normalized = platform.toLowerCase(Locale.ROOT);
        if (List.of("openai", "deepseek", "qwen").contains(normalized)) {
            return normalized;
        }
        return "openai";
    }

    /**
     * 解析最终模型名。
     *
     * @return 最终模型名
     */
    private String resolveModel(String platform, String requestedModel) {
        if (requestedModel != null && !requestedModel.isBlank()) {
            return requestedModel;
        }
        return switch (platform) {
            case "deepseek" -> "deepseek-chat";
            case "qwen" -> "qwen3-max-preview";
            default -> "default";
        };
    }

    /**
     * 查找首个命中的关键字。
     *
     * @return 命中的关键字，不存在时返回 null
     */
    private String findMatchedKeyword(String text, List<String> keywords) {
        if (text == null || keywords == null || keywords.isEmpty()) {
            return null;
        }
        String normalizedText = text.toLowerCase(Locale.ROOT);
        return keywords.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(keyword -> !keyword.isBlank())
                .filter(keyword -> normalizedText.contains(keyword.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(null);
    }

    /**
     * 将文本拆分为多个片段，继续保持 SSE 输出语义。
     *
     * @return 文本片段列表
     */
    private List<String> splitContent(String content, int chunkSize) {
        if (content == null || content.isBlank()) {
            return List.of("");
        }
        List<String> chunks = new ArrayList<>();
        for (int start = 0; start < content.length(); start += chunkSize) {
            int end = Math.min(start + chunkSize, content.length());
            chunks.add(content.substring(start, end));
        }
        return chunks;
    }

    /**
     * 字符串空白归一化。
     *
     * @return 去空格后的字符串，若为空白则返回 null
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    /**
     * 读取当前计数值。
     *
     * @return 当前计数值
     */
    private int currentCounter(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            log.warn("[currentCounter.method] Redis 计数值解析失败, key={}, value={}", key, value);
            return 0;
        }
    }

    /**
     * 递增计数器并设置过期时间。
     *
     * @return 递增后的值
     */
    private int incrementCounter(String key, long ttlSecond) {
        Long value = stringRedisTemplate.opsForValue().increment(key);
        if (value != null && value == 1L) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(Math.max(ttlSecond, 1)));
        }
        return value == null ? 0 : value.intValue();
    }

    /**
     * 计算距离次日零点的秒数。
     *
     * @return 秒数
     */
    private long secondsUntilTomorrow() {
        return Math.max(Duration.between(LocalDateTime.now(), LocalDate.now().plusDays(1).atStartOfDay()).getSeconds(), 1);
    }

    /**
     * 计算距离下一小时整点的秒数。
     *
     * @return 秒数
     */
    private long secondsUntilNextHour() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextHour = now.withMinute(0).withSecond(0).withNano(0).plusHours(1);
        return Math.max(Duration.between(now, nextHour).getSeconds(), 1);
    }

    /**
     * 文本审查结果。
     */
    private record ModerationResult(boolean blocked, AiCallRecord.ReviewStatus reviewStatus, String reason) {
    }

    /**
     * 执行成功结果。
     */
    private record ExecutionOutcome(String platform, String model, String content, int attemptCount, boolean degraded) {
    }

    /**
     * 额度快照。
     */
    private record QuotaSnapshot(Integer dailyLimit, Integer dailyUsed, Integer hourlyLimit,
                                 Integer hourlyUsed, boolean allowed, String rejectReason) {

        /**
         * 构建无限制快照。
         *
         * @return 无限额快照
         */
        static QuotaSnapshot unlimited() {
            return new QuotaSnapshot(null, 0, null, 0, true, null);
        }

        /**
         * 从策略对象构建初始快照。
         *
         * @param policy 配额策略
         * @return 初始快照
         */
        static QuotaSnapshot fromPolicy(AiQuotaPolicy policy) {
            if (policy == null) {
                return unlimited();
            }
            return new QuotaSnapshot(policy.getDailyLimit(), 0, policy.getHourlyLimit(), 0, true, null);
        }
    }
}