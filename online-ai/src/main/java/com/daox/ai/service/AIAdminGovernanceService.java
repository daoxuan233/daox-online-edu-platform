package com.daox.ai.service;

import com.daox.ai.entity.mongodb.AiCallRecord;
import com.daox.ai.entity.mongodb.AiQuotaPolicy;
import com.daox.ai.entity.mongodb.AiRuntimeStrategy;
import com.daox.ai.entity.request.admin.AiQuotaPolicyUpsertRequest;
import com.daox.ai.entity.request.admin.AiReviewDecisionRequest;
import com.daox.ai.entity.request.admin.AiRuntimeStrategyRequest;
import com.daox.ai.entity.response.admin.AiGovernanceOverviewVO;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * AI 管理治理服务。
 * <p>
 * 该服务负责向管理员提供统计看板、审查列表、配额配置和运行策略配置能力。
 */
public interface AIAdminGovernanceService {

    /**
     * 获取治理总览数据。
     *
     * @return 总览数据
     */
    Mono<AiGovernanceOverviewVO> getOverview();

    /**
     * 查询 AI 调用记录。
     *
     * @param userId       用户 ID，可为空
     * @param platform     平台名称，可为空
     * @param callStatus   调用状态，可为空
     * @param reviewStatus 审查状态，可为空
     * @param page         页码
     * @param size         每页大小
     * @return 调用记录列表
     */
    Mono<List<AiCallRecord>> getCallRecords(String userId, String platform,
                                            AiCallRecord.CallStatus callStatus,
                                            AiCallRecord.ReviewStatus reviewStatus,
                                            int page, int size);

    /**
     * 获取全部配额策略。
     *
     * @return 配额策略列表
     */
    Mono<List<AiQuotaPolicy>> getQuotaPolicies();

    /**
     * 保存配额策略。
     *
     * @param request    请求体
     * @param operatorId 操作者 ID
     * @return 持久化后的策略
     */
    Mono<AiQuotaPolicy> saveQuotaPolicy(AiQuotaPolicyUpsertRequest request, String operatorId);

    /**
     * 获取当前运行时策略。
     *
     * @return 运行时策略
     */
    Mono<AiRuntimeStrategy> getRuntimeStrategy();

    /**
     * 保存运行时策略。
     *
     * @param request    请求体
     * @param operatorId 操作者 ID
     * @return 持久化后的策略
     */
    Mono<AiRuntimeStrategy> saveRuntimeStrategy(AiRuntimeStrategyRequest request, String operatorId);

    /**
     * 提交审查结论。
     *
     * @param recordId    记录 ID
     * @param request     审查请求体
     * @param operatorId  操作者 ID
     * @return 更新后的调用记录
     */
    Mono<AiCallRecord> reviewRecord(String recordId, AiReviewDecisionRequest request, String operatorId);
}