package com.daox.ai.service.Impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.daox.ai.entity.dto.ConversationSummaryDTO;
import com.daox.ai.entity.mongodb.ChatMessage;
import com.daox.ai.entity.response.AIResponse;
import com.daox.ai.repository.ChatMessageRepository;
import com.daox.ai.service.AIChatService;
import com.daox.ai.utils.Const;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
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
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AIChatServiceImpl implements AIChatService {

    @Resource
    private ChatClient chatClient;

    @Resource
    private ChatMessageRepository chatMessageRepository;

    @Resource
    private ReactiveMongoTemplate reactiveMongoTemplate;


    /**
     * 主方法：处理AI流式对话并异步保存记录。 [default -- OpenAI]
     * 这是Controller层应该调用的方法。
     *
     * @param userId         发起请求的用户ID
     * @param question       用户的问题
     * @param conversationId 继续对话，则传入之前的会话ID 否则就是新会话ID
     * @return 返回给前端的实时内容流 (Flux<String>)
     */
    @Override
    public Flux<AIResponse> streamChatAndSave(String userId, String question, String conversationId) {
        log.info("[streamChatAndSave.method]发送问题,question = {},user ID = {},conversation ID = {}", question, userId, conversationId);

        // 0. 检测并验证会话ID，得出一个“有效final”的会话ID
        String validatedConversationId = conversationId;
        if (validatedConversationId != null && !validatedConversationId.isBlank()) {
            String conversationPrefix = Const.AI_ASSISTANT_CONVERSATION + userId + "_";
            if (!validatedConversationId.startsWith(conversationPrefix)) {
                log.warn("[streamChatAndSave.method] 传入的会话ID {} 无效或不属于当前用户 {}，将作为新会话处理。", validatedConversationId, userId);
                validatedConversationId = null; // 将其置为null，以开启新会话
            }
        }
        // 将处理过的ID赋值给一个final变量，以便在lambda中使用
        final String finalConversationId = validatedConversationId;

        // 1. 如果有会话ID，则异步获取历史消息；否则，返回一个空的列表。
        Mono<List<Message>> historyMono = (finalConversationId != null && !finalConversationId.isBlank())
                ? getMessagesForConversation(finalConversationId, 0, 10) // 获取最近10条消息
                .map(chatMessages -> {
                    Collections.reverse(chatMessages); // 将消息反转，以符合时间顺序
                    return chatMessages.stream()
                            .<Message>map(message -> { // 显式转换为Message类型
                                if (Const.AI_ASSISTANT.equals(message.getSenderId())) {
                                    return new AssistantMessage(message.getContent());
                                } else {
                                    return new UserMessage(message.getContent());
                                }
                            })
                            .collect(Collectors.toList());
                })
                : Mono.just(Collections.emptyList()); // 新对话则历史为空

        // 2. 将历史消息加载与AI调用串联起来
        Flux<String> contentStream = historyMono.flatMapMany(history ->
                        // 直接构建调用链：加载历史消息，添加当前问题，然后发起流式请求
                        chatClient.prompt()
                                .messages(history)
                                // .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                                .user(question)
                                .stream()
                                .content()
                )
                .cache(); // 使用cache()来允许多次订阅

        // 3. 创建保存聊天记录的操作，获取AI消息ID
        Mono<String> aiMessageIdMono = contentStream
                .collect(Collectors.joining())
                .flatMap(aiResponse ->
                        saveAiChatMessages(userId, question, aiResponse, finalConversationId)
                )
                .map(Tuple2::getT2); // 获取AI消息ID

        // 4. 返回内容流，最后返回包含AI消息ID的结束信号
        return Flux.concat(
                // 先返回流式内容，recordId暂时为null
                contentStream.map(content -> AIResponse.builder().content(content).build()),
                // 流结束后，返回包含AI消息ID的最终响应
                aiMessageIdMono.map(aiMessageId ->
                        AIResponse.builder().recordId(aiMessageId).content("").build()
                )
        );
    }

    /**
     * 主方法：处理AI流式对话并异步保存记录。 [deepseek]
     * 这是Controller层应该调用的方法。
     *
     * @param userId         发起请求的用户ID
     * @param question       用户的问题
     * @param conversationId 继续对话，则传入之前的会话ID 否则就是新会话ID
     * @return 返回给前端的实时内容流 (Flux<String>)
     */
    @Override
    public Flux<String> streamChatAndSaveDeepSeek(String userId, String question, String conversationId) {
        log.info("[streamChatAndSaveDeepSeek.method]发送问题,question = {},user ID = {},conversation ID = {}", question, userId, conversationId);

        // 0. 检测并验证会话ID，得出一个“有效final”的会话ID
        String validatedConversationId = conversationId;
        if (validatedConversationId != null && !validatedConversationId.isBlank()) {
            String conversationPrefix = Const.AI_ASSISTANT_CONVERSATION + userId + "_";
            if (!validatedConversationId.startsWith(conversationPrefix)) {
                log.warn("[streamChatAndSaveDeepSeek.method] 传入的会话ID {} 无效或不属于当前用户 {}，将作为新会话处理。", validatedConversationId, userId);
                validatedConversationId = null; // 将其置为null，以开启新会话
            }
        }
        // 将处理过的ID赋值给一个final变量，以便在lambda中使用
        final String finalConversationId = validatedConversationId;

        // 1. 如果有会话ID，则异步获取历史消息；否则，返回一个空的列表。
        Mono<List<Message>> historyMono = (finalConversationId != null && !finalConversationId.isBlank())
                ? getMessagesForConversation(finalConversationId, 0, 15) // 获取最近10条消息
                .map(chatMessages -> {
                    Collections.reverse(chatMessages); // 将消息反转，以符合时间顺序
                    return chatMessages.stream()
                            .<Message>map(message -> { // 显式转换为Message类型
                                if (Const.AI_ASSISTANT.equals(message.getSenderId())) {
                                    return new AssistantMessage(message.getContent());
                                } else {
                                    return new UserMessage(message.getContent());
                                }
                            })
                            .collect(Collectors.toList());
                })
                : Mono.just(Collections.emptyList()); // 新对话则历史为空
        // 平台选择
        ChatClient.Builder builder = ChatClient.builder(DeepSeekChatModel.builder().build());
        // 2. 将历史消息加载与AI调用串联起来
        Flux<String> contentStream = historyMono.flatMapMany(history ->
                        // 直接构建调用链：加载历史消息，添加当前问题，然后发起流式请求
                        builder.defaultOptions(ChatOptions.builder().model("deepseek-chat").build())
                                .build()
                                .prompt()
                                .messages(history)
                                .user(question)
                                .stream()
                                .content()
                )
                .cache(); // 使用cache()来允许多次订阅

        // 3. 创建保存聊天记录的操作
        Mono<Void> saveOperation = contentStream
                .collect(Collectors.joining())
                .flatMap(aiResponse ->
                        saveAiChatMessages(userId, question, aiResponse, finalConversationId)
                )
                .then();

        // 4. 先返回内容流，待流结束后再执行保存操作
        return contentStream.concatWith(saveOperation.then(Mono.empty()));
    }

    @Override
    public Flux<AIResponse> streamChatAndSaveDeepSeekRes(String userId, String question, String conversationId) {
        log.info("[streamChatAndSaveDeepSeekRes.method]发送问题,question = {},user ID = {},conversation ID = {}", question, userId, conversationId);

        String validatedConversationId = conversationId;
        if (validatedConversationId != null && !validatedConversationId.isBlank()) {
            String conversationPrefix = Const.AI_ASSISTANT_CONVERSATION + userId + "_";
            if (!validatedConversationId.startsWith(conversationPrefix)) {
                log.warn("[streamChatAndSaveDeepSeekRes.method] 传入的会话ID {} 无效或不属于当前用户 {}，将作为新会话处理。", validatedConversationId, userId);
                validatedConversationId = null;
            }
        }
        final String finalConversationId = validatedConversationId;

        Mono<List<Message>> historyMono = (finalConversationId != null && !finalConversationId.isBlank())
                ? getMessagesForConversation(finalConversationId, 0, 15)
                .map(chatMessages -> {
                    Collections.reverse(chatMessages);
                    return chatMessages.stream()
                            .<Message>map(message -> {
                                if (Const.AI_ASSISTANT.equals(message.getSenderId())) {
                                    return new AssistantMessage(message.getContent());
                                } else {
                                    return new UserMessage(message.getContent());
                                }
                            })
                            .collect(Collectors.toList());
                })
                : Mono.just(Collections.emptyList());

        ChatClient.Builder builder = ChatClient.builder(DeepSeekChatModel.builder().build());
        Flux<String> contentStream = historyMono.flatMapMany(history ->
                        builder.defaultOptions(ChatOptions.builder().model("deepseek-chat").build())
                                .build()
                                .prompt()
                                .messages(history)
                                .user(question)
                                .stream()
                                .content()
                )
                .cache();

        Mono<String> aiMessageIdMono = contentStream
                .collect(Collectors.joining())
                .flatMap(aiResponse -> saveAiChatMessages(userId, question, aiResponse, finalConversationId))
                .map(Tuple2::getT2);

        return Flux.concat(
                contentStream.map(content -> AIResponse.builder().content(content).build()),
                aiMessageIdMono.map(aiMessageId -> AIResponse.builder().recordId(aiMessageId).content("").build())
        );
    }

    /**
     * 主方法：处理AI流式对话并异步保存记录。 [qwen]
     * 这是Controller层应该调用的方法。
     *
     * @param userId         发起请求的用户ID
     * @param question       用户的问题
     * @param conversationId 继续对话，则传入之前的会话ID 否则就是新会话ID
     * @return 返回给前端的实时内容流 (Flux<String>)
     */
    @Override
    public Flux<String> streamChatAndSaveQwen(String userId, String question, String conversationId) {
        log.info("[streamChatAndSaveQwen.method]发送问题,question = {},user ID = {},conversation ID = {}", question, userId, conversationId);

        // 0. 检测并验证会话ID，得出一个“有效final”的会话ID
        String validatedConversationId = conversationId;
        if (validatedConversationId != null && !validatedConversationId.isBlank()) {
            String conversationPrefix = Const.AI_ASSISTANT_CONVERSATION + userId + "_";
            if (!validatedConversationId.startsWith(conversationPrefix)) {
                log.warn("[streamChatAndSaveQwen.method] 传入的会话ID {} 无效或不属于当前用户 {}，将作为新会话处理。", validatedConversationId, userId);
                validatedConversationId = null; // 将其置为null，以开启新会话
            }
        }
        // 将处理过的ID赋值给一个final变量，以便在lambda中使用
        final String finalConversationId = validatedConversationId;

        // 1. 如果有会话ID，则异步获取历史消息；否则，返回一个空的列表。
        Mono<List<Message>> historyMono = (finalConversationId != null && !finalConversationId.isBlank())
                ? getMessagesForConversation(finalConversationId, 0, 15) // 获取最近10条消息
                .map(chatMessages -> {
                    Collections.reverse(chatMessages); // 将消息反转，以符合时间顺序
                    return chatMessages.stream()
                            .<Message>map(message -> { // 显式转换为Message类型
                                if (Const.AI_ASSISTANT.equals(message.getSenderId())) {
                                    return new AssistantMessage(message.getContent());
                                } else {
                                    return new UserMessage(message.getContent());
                                }
                            })
                            .collect(Collectors.toList());
                })
                : Mono.just(Collections.emptyList()); // 新对话则历史为空
        // 平台选择
        ChatClient.Builder builder = ChatClient.builder(DashScopeChatModel.builder().build());
        // 2. 将历史消息加载与AI调用串联起来
        Flux<String> contentStream = historyMono.flatMapMany(history ->
                        // 直接构建调用链：加载历史消息，添加当前问题，然后发起流式请求
                        builder.defaultOptions(ChatOptions.builder().model("qwen3-max-preview").build())
                                .build()
                                .prompt()
                                .messages(history)
                                .user(question)
                                .stream()
                                .content()
                )
                .cache(); // 使用cache()来允许多次订阅

        // 3. 创建保存聊天记录的操作
        Mono<Void> saveOperation = contentStream
                .collect(Collectors.joining())
                .flatMap(aiResponse ->
                        saveAiChatMessages(userId, question, aiResponse, finalConversationId)
                )
                .then();

        // 4. 先返回内容流，待流结束后再执行保存操作
        return contentStream.concatWith(saveOperation.then(Mono.empty()));
    }

    @Override
    public Flux<AIResponse> streamChatAndSaveQwenRes(String userId, String question, String conversationId) {
        log.info("[streamChatAndSaveQwenRes.method]发送问题,question = {},user ID = {},conversation ID = {}", question, userId, conversationId);

        String validatedConversationId = conversationId;
        if (validatedConversationId != null && !validatedConversationId.isBlank()) {
            String conversationPrefix = Const.AI_ASSISTANT_CONVERSATION + userId + "_";
            if (!validatedConversationId.startsWith(conversationPrefix)) {
                log.warn("[streamChatAndSaveQwenRes.method] 传入的会话ID {} 无效或不属于当前用户 {}，将作为新会话处理。", validatedConversationId, userId);
                validatedConversationId = null;
            }
        }
        final String finalConversationId = validatedConversationId;

        Mono<List<Message>> historyMono = (finalConversationId != null && !finalConversationId.isBlank())
                ? getMessagesForConversation(finalConversationId, 0, 15)
                .map(chatMessages -> {
                    Collections.reverse(chatMessages);
                    return chatMessages.stream()
                            .<Message>map(message -> {
                                if (Const.AI_ASSISTANT.equals(message.getSenderId())) {
                                    return new AssistantMessage(message.getContent());
                                } else {
                                    return new UserMessage(message.getContent());
                                }
                            })
                            .collect(Collectors.toList());
                })
                : Mono.just(Collections.emptyList());

        ChatClient.Builder builder = ChatClient.builder(DashScopeChatModel.builder().build());
        Flux<String> contentStream = historyMono.flatMapMany(history ->
                        builder.defaultOptions(ChatOptions.builder().model("qwen3-max-preview").build())
                                .build()
                                .prompt()
                                .messages(history)
                                .user(question)
                                .stream()
                                .content()
                )
                .cache();

        Mono<String> aiMessageIdMono = contentStream
                .collect(Collectors.joining())
                .flatMap(aiResponse -> saveAiChatMessages(userId, question, aiResponse, finalConversationId))
                .map(Tuple2::getT2);

        return Flux.concat(
                contentStream.map(content -> AIResponse.builder().content(content).build()),
                aiMessageIdMono.map(aiMessageId -> AIResponse.builder().recordId(aiMessageId).content("").build())
        );
    }

    /**
     * 获取会话历史记录列表
     *
     * @param userId 用户id
     * @return 会话列表
     */
    @Override
    public Mono<List<ConversationSummaryDTO>> getConversationSummaries(String userId) {
        log.info("[getConversationSummaries.method]获取会话历史记录列表,user ID = {}", userId);
        // 1. 拼接 conversation_id 的前缀，用于模糊匹配
        String conversationPrefix = Const.AI_ASSISTANT_CONVERSATION + userId + "_";
        // 2. 构建聚合管道 (Aggregation Pipeline)
        Aggregation aggregation = Aggregation.newAggregation(
                // 2.1. 模糊匹配: 筛选出所有符合前缀的会话
                Aggregation.match(Criteria.where("conversation_id").regex("^" + conversationPrefix)),
                // 2.2. 排序: 按会话ID和时间戳升序，为分组做准备
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "conversation_id", "timestamp")),
                // 2.3. 分组: 按 conversation_id 进行分组
                Aggregation.group("conversation_id")
                        // 取每个分组第一条消息的内容作为标题
                        .first("content").as("title")
                        // 取每个分组最后一条消息的时间作为最后更新时间
                        .last("timestamp").as("lastMessageTime"),

                // 2.4. 投射: 将分组结果映射到 ConversationSummaryDTO
                Aggregation.project()
                        .and("_id").as("conversationId") // 将 MongoDB 的 _id 字段映射为 conversationId
                        .and("title").as("title")
                        .and("lastMessageTime").as("lastMessageTime")
        );
        // 3. 执行聚合查询
        Flux<ConversationSummaryDTO> summariesFlux = reactiveMongoTemplate.aggregate(
                aggregation,
                "chat_messages", // 在 chat_messages 集合上执行聚合
                ConversationSummaryDTO.class // 结果映射的目标类
        );

        // 4. 将响应式流 (Flux) 收集成一个列表 (List)，并用 Mono 包装后返回
        return summariesFlux.collectList();
    }

    /**
     * 分页获取单个对话中的消息历史。
     *
     * @param conversationId 对话ID
     * @param page           页码 (从0开始)
     * @param size           每页大小
     * @return 一页消息的列表，用 Mono 包装
     */
    @Override
    public Mono<List<ChatMessage>> getMessagesForConversation(String conversationId, int page, int size) {
        // 1. 创建一个 Pageable 对象，并指定排序规则：按时间戳降序（最新的消息在最前面）
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        // 2. 调用 repository 的分页查询方法，它会返回一个 Flux
        Flux<ChatMessage> messagesFlux = chatMessageRepository.findByConversationId(conversationId, pageable);

        // 3. 将这个 Flux<ChatMessage> 收集成一个 List<ChatMessage>，并用 Mono 包装返回
        return messagesFlux.collectList();
    }

    /**
     * 删除一个会话
     *
     * @param userId         用户id
     * @param conversationId 会话id
     */
    @Override
    public Mono<Void> deleteConversation(String userId, String conversationId) {
        // 1. 构建预期的会话ID前缀，用于安全校验
        String expectedPrefix = Const.AI_ASSISTANT_CONVERSATION + userId + "_";

        // 2. 安全检查：验证该会话是否属于当前用户。
        //    这是为了防止一个用户删除另一个用户的会话。
        if (conversationId == null || !conversationId.startsWith(expectedPrefix)) {
            log.warn("[deleteConversation.method] 安全警告：用户 {} 尝试删除不属于自己的会话 {}。操作被拒绝。", userId, conversationId);
            // 返回一个带有错误的Mono，明确表示权限不足或请求无效。
            // 这比返回 Mono.empty() 更能清晰地表达错误状态。
            return Mono.error(new IllegalArgumentException("无权删除该会话或会话ID无效"));
        }

        // 3. 调用 repository 的删除方法。
        //    该方法本身返回一个 Mono<Void>，我们直接将其返回给调用者（Controller）。
        //    Controller 层可以根据这个 Mono 的完成或错误信号来给前端返回相应的成功或失败响应。
        log.info("[deleteConversation.method] 用户 {} 正在删除会话 {}", userId, conversationId);
        return chatMessageRepository.deleteByConversationId(conversationId);
    }

    /**
     * 私有辅助方法：负责创建并保存用户和AI的两条消息记录。
     *
     * @param userId         用户ID，用于标识消息的发送者。
     * @param question       用户的提问内容，将作为用户消息保存。
     * @param aiResponse     AI模型的回答内容，将作为AI消息保存。
     * @param conversationId (可选) 用于继续之前的对话。如果为空，将创建新会话。
     * @return 返回一个Mono，其中包含本次对话的ID和AI消息的ID。
     */
    private Mono<Tuple2<String, String>> saveAiChatMessages(String userId, String question, String aiResponse, String conversationId) {
        // 判断是新对话还是旧对话，如果是新对话，则生成一个新的UUID作为ID
        final String currentConversationId = (conversationId == null || conversationId.isBlank())
                ? Const.AI_ASSISTANT_CONVERSATION + userId + "_" + UUID.randomUUID()
                : conversationId;

        // 创建代表用户问题的ChatMessage对象
        ChatMessage userMessage = new ChatMessage()
                .setConversationId(currentConversationId)
                .setSenderId(userId)
                .setReceiverId(Const.AI_ASSISTANT) // AI的固定标识符
                .setMessageType(ChatMessage.MessageType.AI)
                .setContentType(ChatMessage.ContentType.TEXT)
                .setContent(question)
                .setTimestamp(LocalDateTime.now())
                .setStatus(ChatMessage.MessageStatus.READ);

        // 创建代表AI回答的ChatMessage对象
        ChatMessage aiMessage = new ChatMessage()
                .setConversationId(currentConversationId)
                .setSenderId(Const.AI_ASSISTANT)
                .setReceiverId(userId)
                .setMessageType(ChatMessage.MessageType.AI)
                .setContentType(ChatMessage.ContentType.RICH_TEXT)
                .setContent(aiResponse)
                .setTimestamp(LocalDateTime.now().plusNanos(1_000_000)) // 确保时间戳稍后于用户消息
                .setStatus(ChatMessage.MessageStatus.DELIVERED);

        // 使用 saveAll 批量插入这两条消息，这是一个响应式操作
        return chatMessageRepository.saveAll(List.of(userMessage, aiMessage))
                .filter(chatMessage -> Const.AI_ASSISTANT.equals(chatMessage.getSenderId()))
                .next()
                .map(aiMsg -> Tuples.of(currentConversationId, aiMsg.getId()));
    }

    public Flux<String> chatFluxTest(String question) {
        log.info("[chatFluxTest.method] 开始调用AI模型，问题: {}", question);

        return chatClient.prompt() // 使用不带参数的 prompt()
                .user(question) // 使用 .user() 方法传递问题
                .stream()       // 发起流式请求
                .content();     // 获取内容流

        // 返回带有诊断日志的流
        /*return contentStream
                .doOnNext(chunk -> {
                    // 每当收到一个数据块时，打印日志
                    log.info("AI-CHUNK: {}", chunk);
                })
                .doOnError(error -> {
                    // 当流内部发生任何错误时，打印详细的错误日志
                    log.error("AI-STREAM-ERROR: ", error);
                })
                .doOnComplete(() -> {
                    // 当流成功完成时，打印日志
                    log.info("AI-STREAM-COMPLETE: 流处理完成");
                });*/
    }

//    public Flux<String> streamChatAndSave(String userId, String question, String conversationId) {
//        log.info("[streamChatAndSave.method]发送问题,question = {},user ID = {},conversation ID = {}", question, userId, conversationId);
//        /*// 1. 调用AI模型，获取内容的响应式流
//        Flux<String> contentStream = chatClient.prompt()
//                .user(question)
//                .stream()
//                .content();
//
//        // 2.【核心】创建一个在后台执行的保存任务。
//        //    我们使用 .collect(Collectors.joining()) 来等待所有数据块都到达并拼接成一个完整字符串。
//        //    这个操作会返回一个 Mono<String>，代表未来的那个完整AI回答。
//        Mono<String> fullResponseMono = contentStream.collect(Collectors.joining());
//
//        // 3. 订阅这个Mono，当它完成时（即AI回答完整时），执行保存逻辑。
//        fullResponseMono.flatMap(aiResponse ->
//                saveAiChatMessages(userId, question, aiResponse, conversationId)
//        ).subscribe( // .subscribe() 是“即发即忘”的关键，它会启动这个后台任务而不会阻塞主流程
//                savedConvId -> log.info("后台对话记录保存成功，会话ID: {}", savedConvId), // 成功时的回调
//                error -> log.error("后台保存对话记录时发生错误", error)       // 【关键】错误时的回调
//        );
//
//        // 4.【重要】立即返回原始的、未经处理的 contentStream，确保前端能实时收到数据。
//        return contentStream;*/
//        /*--------------------------------------------------------------------------*/
//        // 1. 使用 .publish().autoConnect(1) 或 .cache() 让流可以被多次订阅
//        // .cache() 会缓存并重放所有元素给后续的订阅者
//        Flux<String> contentStream = chatClient.prompt()
//                .user(question)
//
//                .stream()
//                .content()
//                .cache(); // 使用 cache() 缓存流的结果
//
//        // 2. 创建保存操作的 Mono，它现在订阅的是缓存后的流
//        //    使用 .then() 将 Mono<String> 转换成 Mono<Void>，表示我们只关心它是否完成
//        Mono<Void> saveOperation = contentStream
//                .collect(Collectors.joining())
//                .flatMap(aiResponse ->
//                        saveAiChatMessages(userId, question, aiResponse, conversationId)
//                )
//                .then(); // 忽略成功结果，只保留完成或错误信号
//
//        // 3.【关键】使用 concatWith 将内容流和保存操作链接起来
//        //    concatWith 会先返回 contentStream 的所有元素。
//        //    当 contentStream 完成后，它会自动订阅并执行 saveOperation。
//        //    saveOperation.then(Mono.empty()) 确保保存操作完成后不会发出任何元素，只发出完成信号。
//        return contentStream.concatWith(saveOperation.then(Mono.empty()));
//
//    }

    @Getter
    @Setter
    // 定义结果类
    private static class ChatMessageResult {
        private final String conversationId;
        private final String aiMessageId;

        public ChatMessageResult(String conversationId, String aiMessageId) {
            this.conversationId = conversationId;
            this.aiMessageId = aiMessageId;
        }
    }
}
