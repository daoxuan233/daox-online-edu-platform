package com.daox.ai.service.Impl;

import com.daox.ai.config.AIGovernanceProperties;
import com.daox.ai.entity.mongodb.AiCallRecord;
import com.daox.ai.entity.mongodb.AiQuotaPolicy;
import com.daox.ai.entity.mongodb.AiRuntimeStrategy;
import com.daox.ai.entity.request.admin.AiQuotaPolicyUpsertRequest;
import com.daox.ai.entity.request.admin.AiReviewDecisionRequest;
import com.daox.ai.entity.request.admin.AiRuntimeStrategyRequest;
import com.daox.ai.entity.response.admin.AiGovernanceOverviewVO;
import com.daox.ai.repository.AiCallRecordRepository;
import com.daox.ai.repository.AiQuotaPolicyRepository;
import com.daox.ai.repository.AiRuntimeStrategyRepository;
import com.daox.ai.service.AIAdminGovernanceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AI 管理治理服务实现。
 */
@Slf4j
@Service
public class AIAdminGovernanceServiceImpl implements AIAdminGovernanceService {

    @Resource
    private AiCallRecordRepository aiCallRecordRepository;

    @Resource
    private AiQuotaPolicyRepository aiQuotaPolicyRepository;

    @Resource
    private AiRuntimeStrategyRepository aiRuntimeStrategyRepository;

    @Resource
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Resource
    private AIGovernanceProperties aiGovernanceProperties;

    @Override
    public Mono<AiGovernanceOverviewVO> getOverview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        Query todayQuery = new Query(Criteria.where("created_at").gte(todayStart).lt(tomorrowStart));
        Query pendingQuery = new Query(Criteria.where("review_status").is(AiCallRecord.ReviewStatus.PENDING));

        Mono<List<AiCallRecord>> todayRecordsMono = reactiveMongoTemplate.find(todayQuery, AiCallRecord.class).collectList();
        Mono<Long> pendingCountMono = reactiveMongoTemplate.count(pendingQuery, AiCallRecord.class);

        return Mono.zip(todayRecordsMono, pendingCountMono)
                .map(tuple -> {
                    List<AiCallRecord> todayRecords = tuple.getT1();
                    long pendingCount = tuple.getT2();
                    Map<String, List<AiCallRecord>> groupByPlatform = todayRecords.stream()
                            .collect(Collectors.groupingBy(record -> defaultPlatform(record.getFinalPlatform())));

                    List<AiGovernanceOverviewVO.PlatformMetric> metrics = groupByPlatform.entrySet().stream()
                            .map(entry -> AiGovernanceOverviewVO.PlatformMetric.builder()
                                    .platform(entry.getKey())
                                    .totalCalls(entry.getValue().size())
                                    .successCalls(entry.getValue().stream()
                                            .filter(record -> record.getCallStatus() == AiCallRecord.CallStatus.SUCCESS)
                                            .count())
                                    .degradedCalls(entry.getValue().stream()
                                            .filter(record -> Boolean.TRUE.equals(record.getDegraded()))
                                            .count())
                                    .build())
                            .sorted(Comparator.comparing(AiGovernanceOverviewVO.PlatformMetric::getPlatform))
                            .toList();

                    return AiGovernanceOverviewVO.builder()
                            .totalCallsToday(todayRecords.size())
                            .successCallsToday(todayRecords.stream()
                                    .filter(record -> record.getCallStatus() == AiCallRecord.CallStatus.SUCCESS)
                                    .count())
                            .failedCallsToday(todayRecords.stream()
                                    .filter(record -> record.getCallStatus() == AiCallRecord.CallStatus.FAILED)
                                    .count())
                            .rejectedCallsToday(todayRecords.stream()
                                    .filter(record -> record.getCallStatus() == AiCallRecord.CallStatus.REJECTED)
                                    .count())
                            .degradedCallsToday(todayRecords.stream()
                                    .filter(record -> Boolean.TRUE.equals(record.getDegraded()))
                                    .count())
                            .pendingReviews(pendingCount)
                            .platformMetrics(metrics)
                            .build();
                });
    }

    @Override
    public Mono<List<AiCallRecord>> getCallRecords(String userId, String platform,
                                                   AiCallRecord.CallStatus callStatus,
                                                   AiCallRecord.ReviewStatus reviewStatus,
                                                   int page, int size) {
        Query query = new Query();
        List<Criteria> criteria = new ArrayList<>();
        if (userId != null && !userId.isBlank()) {
            criteria.add(Criteria.where("user_id").is(userId));
        }
        if (platform != null && !platform.isBlank()) {
            criteria.add(Criteria.where("final_platform").is(defaultPlatform(platform)));
        }
        if (callStatus != null) {
            criteria.add(Criteria.where("call_status").is(callStatus));
        }
        if (reviewStatus != null) {
            criteria.add(Criteria.where("review_status").is(reviewStatus));
        }
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }
        query.with(PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "created_at")));
        return reactiveMongoTemplate.find(query, AiCallRecord.class).collectList();
    }

    @Override
    public Mono<List<AiQuotaPolicy>> getQuotaPolicies() {
        return aiQuotaPolicyRepository.findAllByOrderByUpdatedAtDesc()
                .collectList()
                .map(this::mergeWithDefaultPolicies);
    }

    @Override
    public Mono<AiQuotaPolicy> saveQuotaPolicy(AiQuotaPolicyUpsertRequest request, String operatorId) {
        if (request == null || request.getScopeType() == null) {
            return Mono.error(new IllegalArgumentException("scopeType 不能为空"));
        }
        String scopeValue = normalizeScopeValue(request.getScopeType(), request.getScopeValue());
        Query query = new Query(Criteria.where("scope_type").is(request.getScopeType()).and("scope_value").is(scopeValue));
        return reactiveMongoTemplate.findOne(query, AiQuotaPolicy.class)
                .defaultIfEmpty(new AiQuotaPolicy())
                .map(policy -> policy
                        .setScopeType(request.getScopeType())
                        .setScopeValue(scopeValue)
                        .setDailyLimit(request.getDailyLimit())
                        .setHourlyLimit(request.getHourlyLimit())
                        .setEnabled(Boolean.TRUE.equals(request.getEnabled()))
                        .setRemark(request.getRemark())
                        .setUpdatedBy(operatorId)
                        .setUpdatedAt(LocalDateTime.now()))
                .flatMap(aiQuotaPolicyRepository::save);
    }

    @Override
    public Mono<AiRuntimeStrategy> getRuntimeStrategy() {
        return aiRuntimeStrategyRepository.findById(AiRuntimeStrategy.DEFAULT_ID)
                .switchIfEmpty(Mono.just(defaultStrategy()));
    }

    @Override
    public Mono<AiRuntimeStrategy> saveRuntimeStrategy(AiRuntimeStrategyRequest request, String operatorId) {
        if (request == null) {
            return Mono.error(new IllegalArgumentException("运行时策略请求体不能为空"));
        }
        return aiRuntimeStrategyRepository.findById(AiRuntimeStrategy.DEFAULT_ID)
                .defaultIfEmpty(new AiRuntimeStrategy().setId(AiRuntimeStrategy.DEFAULT_ID))
                .map(strategy -> strategy
                        .setId(AiRuntimeStrategy.DEFAULT_ID)
                        .setReviewEnabled(Boolean.TRUE.equals(request.getReviewEnabled()))
                        .setBlockedKeywords(cleanKeywords(request.getBlockedKeywords()))
                        .setReviewKeywords(cleanKeywords(request.getReviewKeywords()))
                        .setMaxAttempts(safeMaxAttempts(request.getMaxAttempts()))
                        .setFallbackPlatforms(normalizePlatforms(request.getFallbackPlatforms()))
                        .setUpdatedBy(operatorId)
                        .setUpdatedAt(LocalDateTime.now()))
                .flatMap(aiRuntimeStrategyRepository::save);
    }

    @Override
    public Mono<AiCallRecord> reviewRecord(String recordId, AiReviewDecisionRequest request, String operatorId) {
        if (recordId == null || recordId.isBlank()) {
            return Mono.error(new IllegalArgumentException("recordId 不能为空"));
        }
        if (request == null || request.getDecision() == null) {
            return Mono.error(new IllegalArgumentException("审查结论不能为空"));
        }
        if (request.getDecision() != AiCallRecord.ReviewStatus.APPROVED
                && request.getDecision() != AiCallRecord.ReviewStatus.REJECTED) {
            return Mono.error(new IllegalArgumentException("审查结论仅支持 APPROVED 或 REJECTED"));
        }
        return aiCallRecordRepository.findById(recordId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("未找到对应的 AI 调用记录")))
                .map(record -> record
                        .setReviewStatus(request.getDecision())
                        .setReviewReason(request.getNote())
                        .setReviewedBy(operatorId)
                        .setReviewedAt(LocalDateTime.now()))
                .flatMap(aiCallRecordRepository::save);
    }

    private AiRuntimeStrategy defaultStrategy() {
        return new AiRuntimeStrategy()
                .setId(AiRuntimeStrategy.DEFAULT_ID)
                .setReviewEnabled(aiGovernanceProperties.getReview().isEnabled())
                .setBlockedKeywords(cleanKeywords(aiGovernanceProperties.getReview().getBlockedKeywords()))
                .setReviewKeywords(cleanKeywords(aiGovernanceProperties.getReview().getReviewKeywords()))
                .setMaxAttempts(safeMaxAttempts(aiGovernanceProperties.getRetry().getMaxAttempts()))
                .setFallbackPlatforms(normalizePlatforms(aiGovernanceProperties.getRetry().getFallbackPlatforms()));
    }

    /**
     * 将数据库中的已保存策略与系统默认策略合并。
     * <p>
     * 这样前端即使在数据库尚未初始化时，也能稳定获取全局与角色级的基线治理配置。
     * 若数据库中已存在同作用域策略，则优先返回持久化结果。
     *
     * @param persistedPolicies 数据库存量策略
     * @return 合并后的策略列表
     */
    private List<AiQuotaPolicy> mergeWithDefaultPolicies(List<AiQuotaPolicy> persistedPolicies) {
        Map<String, AiQuotaPolicy> mergedPolicies = new LinkedHashMap<>();
        buildDefaultPolicies().forEach(policy -> mergedPolicies.put(policyKey(policy), policy));
        persistedPolicies.forEach(policy -> mergedPolicies.put(policyKey(policy), policy));
        return mergedPolicies.values().stream()
                .sorted(Comparator
                        .comparingInt(this::policyOrder)
                        .thenComparing(policy -> defaultString(policy.getScopeValue())))
                .toList();
    }

    /**
     * 构建系统默认配额策略列表。
     *
     * @return 默认策略列表
     */
    private List<AiQuotaPolicy> buildDefaultPolicies() {
        LocalDateTime now = LocalDateTime.now();
        AIGovernanceProperties.Quota quota = aiGovernanceProperties.getQuota();
        return List.of(
                new AiQuotaPolicy()
                        .setScopeType(AiQuotaPolicy.ScopeType.GLOBAL)
                        .setScopeValue("default")
                        .setDailyLimit(quota.getGlobalDailyLimit())
                        .setHourlyLimit(quota.getGlobalHourlyLimit())
                        .setEnabled(true)
                        .setRemark("系统默认全局基线配额")
                        .setUpdatedBy("system-default")
                        .setUpdatedAt(now),
                new AiQuotaPolicy()
                        .setScopeType(AiQuotaPolicy.ScopeType.ROLE)
                        .setScopeValue("student")
                        .setDailyLimit(quota.getStudentDailyLimit())
                        .setHourlyLimit(quota.getStudentHourlyLimit())
                        .setEnabled(true)
                        .setRemark("系统默认学生角色配额")
                        .setUpdatedBy("system-default")
                        .setUpdatedAt(now),
                new AiQuotaPolicy()
                        .setScopeType(AiQuotaPolicy.ScopeType.ROLE)
                        .setScopeValue("teacher")
                        .setDailyLimit(quota.getTeacherDailyLimit())
                        .setHourlyLimit(quota.getTeacherHourlyLimit())
                        .setEnabled(true)
                        .setRemark("系统默认教师角色配额")
                        .setUpdatedBy("system-default")
                        .setUpdatedAt(now),
                new AiQuotaPolicy()
                        .setScopeType(AiQuotaPolicy.ScopeType.ROLE)
                        .setScopeValue("admin")
                        .setDailyLimit(quota.getAdminDailyLimit())
                        .setHourlyLimit(quota.getAdminHourlyLimit())
                        .setEnabled(true)
                        .setRemark("系统默认管理员角色配额")
                        .setUpdatedBy("system-default")
                        .setUpdatedAt(now)
        );
    }

    /**
     * 生成策略唯一键。
     *
     * @param policy 策略对象
     * @return 唯一键
     */
    private String policyKey(AiQuotaPolicy policy) {
        return policy.getScopeType() + ":" + defaultString(policy.getScopeValue());
    }

    /**
     * 返回策略排序权重，确保页面展示顺序稳定。
     *
     * @param policy 策略对象
     * @return 排序权重
     */
    private int policyOrder(AiQuotaPolicy policy) {
        if (policy.getScopeType() == AiQuotaPolicy.ScopeType.GLOBAL) {
            return 0;
        }
        if (policy.getScopeType() == AiQuotaPolicy.ScopeType.ROLE) {
            return switch (defaultString(policy.getScopeValue())) {
                case "admin" -> 1;
                case "teacher" -> 2;
                case "student" -> 3;
                default -> 4;
            };
        }
        return 5;
    }

    /**
     * 规避空字符串参与排序与拼接时的空指针问题。
     *
     * @param value 输入值
     * @return 非空字符串
     */
    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String normalizeScopeValue(AiQuotaPolicy.ScopeType scopeType, String scopeValue) {
        if (scopeType == AiQuotaPolicy.ScopeType.GLOBAL) {
            return "default";
        }
        if (scopeValue == null || scopeValue.isBlank()) {
            throw new IllegalArgumentException("scopeValue 不能为空");
        }
        return scopeType == AiQuotaPolicy.ScopeType.ROLE ? scopeValue.toLowerCase(Locale.ROOT) : scopeValue;
    }

    private List<String> cleanKeywords(List<String> keywords) {
        if (keywords == null) {
            return List.of();
        }
        return keywords.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(keyword -> !keyword.isBlank())
                .distinct()
                .toList();
    }

    private List<String> normalizePlatforms(List<String> platforms) {
        List<String> result = new ArrayList<>();
        if (platforms != null) {
            for (String platform : platforms) {
                String normalized = defaultPlatform(platform);
                if (!result.contains(normalized)) {
                    result.add(normalized);
                }
            }
        }
        if (result.isEmpty()) {
            result.addAll(List.of("openai", "deepseek", "qwen"));
        }
        return result;
    }

    private Integer safeMaxAttempts(Integer maxAttempts) {
        if (maxAttempts == null || maxAttempts < 1) {
            return 1;
        }
        return Math.min(maxAttempts, 5);
    }

    private String defaultPlatform(String platform) {
        if (platform == null || platform.isBlank()) {
            return "openai";
        }
        String normalized = platform.toLowerCase(Locale.ROOT);
        if (List.of("openai", "deepseek", "qwen").contains(normalized)) {
            return normalized;
        }
        return "openai";
    }
}