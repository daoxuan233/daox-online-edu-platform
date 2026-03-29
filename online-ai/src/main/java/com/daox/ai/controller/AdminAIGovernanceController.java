package com.daox.ai.controller;

import com.daox.ai.entity.RestBean;
import com.daox.ai.entity.mongodb.AiCallRecord;
import com.daox.ai.entity.mongodb.AiQuotaPolicy;
import com.daox.ai.entity.mongodb.AiRuntimeStrategy;
import com.daox.ai.entity.request.admin.AiQuotaPolicyUpsertRequest;
import com.daox.ai.entity.request.admin.AiReviewDecisionRequest;
import com.daox.ai.entity.request.admin.AiRuntimeStrategyRequest;
import com.daox.ai.entity.response.admin.AiGovernanceOverviewVO;
import com.daox.ai.service.AIAdminGovernanceService;
import com.daox.ai.utils.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * AI 管理治理控制器。
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/ai/governance")
public class AdminAIGovernanceController {

    @Resource
    private AIAdminGovernanceService aiAdminGovernanceService;

    /**
     * 获取管理员 AI 治理总览数据。
     * <p>
     * 返回当日调用统计、待审数量以及按平台聚合后的基础监控指标。
     *
     * @param request 当前 HTTP 请求
     * @return AI 治理总览
     */
    @GetMapping("/overview")
    public Mono<RestBean<AiGovernanceOverviewVO>> getOverview(HttpServletRequest request) {
        return requireAdmin(request)
                .flatMap(operatorId -> aiAdminGovernanceService.getOverview())
                .map(RestBean::success)
                .onErrorResume(this::handleError);
    }

    /**
     * 分页查询 AI 调用记录。
     * <p>
     * 支持按用户、平台、调用状态和审查状态组合过滤，用于治理看板中的待审队列与记录列表。
     *
     * @param userId       用户 ID，可为空
     * @param platform     最终调用平台，可为空
     * @param callStatus   调用状态，可为空
     * @param reviewStatus 审查状态，可为空
     * @param page         页码，从 0 开始
     * @param size         每页大小
     * @param request      当前 HTTP 请求
     * @return 调用记录列表
     */
    @GetMapping("/records")
    public Mono<RestBean<List<AiCallRecord>>> getCallRecords(@RequestParam(required = false) String userId,
                                                             @RequestParam(required = false) String platform,
                                                             @RequestParam(required = false) AiCallRecord.CallStatus callStatus,
                                                             @RequestParam(required = false) AiCallRecord.ReviewStatus reviewStatus,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size,
                                                             HttpServletRequest request) {
        return requireAdmin(request)
                .flatMap(operatorId -> aiAdminGovernanceService.getCallRecords(userId, platform, callStatus, reviewStatus, page, size))
                .map(RestBean::success)
                .onErrorResume(this::handleError);
    }

    /**
     * 获取当前可见的 AI 配额策略列表。
     * <p>
     * 当数据库中不存在显式配置时，接口会回退返回系统默认的全局与角色级策略，避免前端空白。
     *
     * @param request 当前 HTTP 请求
     * @return 配额策略列表
     */
    @GetMapping("/quota-policies")
    public Mono<RestBean<List<AiQuotaPolicy>>> getQuotaPolicies(HttpServletRequest request) {
        return requireAdmin(request)
                .flatMap(operatorId -> aiAdminGovernanceService.getQuotaPolicies())
                .map(RestBean::success)
                .onErrorResume(this::handleError);
    }

    /**
     * 新增或更新单条配额策略。
     *
     * @param quotaPolicyRequest 配额策略请求体
     * @param request            当前 HTTP 请求
     * @return 保存后的策略对象
     */
    @PostMapping("/quota-policies")
    public Mono<RestBean<AiQuotaPolicy>> saveQuotaPolicy(@RequestBody AiQuotaPolicyUpsertRequest quotaPolicyRequest,
                                                         HttpServletRequest request) {
        return requireAdmin(request)
                .flatMap(operatorId -> aiAdminGovernanceService.saveQuotaPolicy(quotaPolicyRequest, operatorId))
                .map(RestBean::success)
                .onErrorResume(this::handleError);
    }

    /**
     * 获取当前生效的运行时治理策略。
     *
     * @param request 当前 HTTP 请求
     * @return 运行时策略
     */
    @GetMapping("/runtime-strategy")
    public Mono<RestBean<AiRuntimeStrategy>> getRuntimeStrategy(HttpServletRequest request) {
        return requireAdmin(request)
                .flatMap(operatorId -> aiAdminGovernanceService.getRuntimeStrategy())
                .map(RestBean::success)
                .onErrorResume(this::handleError);
    }

    /**
     * 保存运行时治理策略。
     * <p>
     * 该接口用于维护内容审查开关、阻断词、人工复核词以及模型重试降级顺序。
     *
     * @param runtimeStrategyRequest 运行时策略请求体
     * @param request                当前 HTTP 请求
     * @return 保存后的运行时策略
     */
    @PostMapping("/runtime-strategy")
    public Mono<RestBean<AiRuntimeStrategy>> saveRuntimeStrategy(@RequestBody AiRuntimeStrategyRequest runtimeStrategyRequest,
                                                                 HttpServletRequest request) {
        return requireAdmin(request)
                .flatMap(operatorId -> aiAdminGovernanceService.saveRuntimeStrategy(runtimeStrategyRequest, operatorId))
                .map(RestBean::success)
                .onErrorResume(this::handleError);
    }

    /**
     * 对待审 AI 调用记录提交人工审查结论。
     *
     * @param recordId              调用记录 ID
     * @param reviewDecisionRequest 审查结论请求体
     * @param request               当前 HTTP 请求
     * @return 更新后的调用记录
     */
    @PostMapping("/reviews/{recordId}")
    public Mono<RestBean<AiCallRecord>> reviewRecord(@PathVariable String recordId,
                                                     @RequestBody AiReviewDecisionRequest reviewDecisionRequest,
                                                     HttpServletRequest request) {
        return requireAdmin(request)
                .flatMap(operatorId -> aiAdminGovernanceService.reviewRecord(recordId, reviewDecisionRequest, operatorId))
                .map(RestBean::success)
                .onErrorResume(this::handleError);
    }

    /**
     * 校验当前请求是否具备管理员权限。
     *
     * @param request 当前 HTTP 请求
     * @return 当前操作者 ID
     */
    private Mono<String> requireAdmin(HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            return Mono.error(new IllegalStateException("用户未认证，请先登录"));
        }
        if (!UserUtils.isAdmin(request)) {
            return Mono.error(new SecurityException("仅管理员可访问该接口"));
        }
        return Mono.just(userId);
    }

    /**
     * 统一转换控制器异常响应。
     *
     * @param throwable 异常对象
     * @param <T>       业务数据泛型
     * @return 统一封装后的响应体
     */
    private <T> Mono<RestBean<T>> handleError(Throwable throwable) {
        if (throwable instanceof IllegalStateException stateException) {
            return Mono.just(RestBean.unauthorized(stateException.getMessage()));
        }
        if (throwable instanceof SecurityException securityException) {
            return Mono.just(RestBean.forbidden(securityException.getMessage()));
        }
        if (throwable instanceof IllegalArgumentException illegalArgumentException) {
            return Mono.just(RestBean.failure(400, illegalArgumentException.getMessage()));
        }
        log.error("[AdminAIGovernanceController] 处理管理员 AI 治理请求时发生异常", throwable);
        return Mono.just(RestBean.failure(500, "管理员 AI 治理请求处理失败"));
    }
}