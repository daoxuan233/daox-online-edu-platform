package com.daox.ai.entity.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 管理员视角下的 AI 治理总览。
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AiGovernanceOverviewVO {

    /**
     * 今日调用总量。
     */
    private long totalCallsToday;

    /**
     * 今日成功调用量。
     */
    private long successCallsToday;

    /**
     * 今日失败调用量。
     */
    private long failedCallsToday;

    /**
     * 今日被拒绝调用量。
     */
    private long rejectedCallsToday;

    /**
     * 今日触发降级的调用量。
     */
    private long degradedCallsToday;

    /**
     * 当前待审记录数。
     */
    private long pendingReviews;

    /**
     * 按平台聚合的统计。
     */
    private List<PlatformMetric> platformMetrics;

    /**
     * 平台维度统计。
     */
    @Setter
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class PlatformMetric {
        private String platform;
        private long totalCalls;
        private long successCalls;
        private long degradedCalls;
    }
}