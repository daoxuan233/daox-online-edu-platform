package com.daox.ai.entity.mongodb;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * AI 调用审计记录。
 * <p>
 * 该文档既承担调用统计的原始事实表作用，也承担管理员审查列表的数据来源。
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "ai_call_records")
@CompoundIndex(name = "user_time_idx", def = "{'user_id': 1, 'created_at': -1}")
@CompoundIndex(name = "review_time_idx", def = "{'review_status': 1, 'created_at': -1}")
public class AiCallRecord {

    /**
     * 调用状态。
     */
    public enum CallStatus {
        SUCCESS,
        FAILED,
        REJECTED
    }

    /**
     * 审查状态。
     */
    public enum ReviewStatus {
        PASSED,
        PENDING,
        APPROVED,
        REJECTED
    }

    @Id
    private String id;

    /**
     * 平台内部追踪 ID。
     */
    @Indexed
    @Field("request_id")
    private String requestId;

    @Indexed
    @Field("user_id")
    private String userId;

    @Field("user_role")
    private String userRole;

    @Field("conversation_id")
    private String conversationId;

    @Field("requested_platform")
    private String requestedPlatform;

    @Field("final_platform")
    private String finalPlatform;

    @Field("model")
    private String model;

    @Field("question_content")
    private String questionContent;

    @Field("response_content")
    private String responseContent;

    @Field("call_status")
    private CallStatus callStatus;

    @Field("review_status")
    private ReviewStatus reviewStatus;

    @Field("review_reason")
    private String reviewReason;

    @Field("reviewed_by")
    private String reviewedBy;

    @Field("reviewed_at")
    private LocalDateTime reviewedAt;

    @Field("attempt_count")
    private Integer attemptCount;

    @Field("degraded")
    private Boolean degraded;

    @Field("quota_daily_limit")
    private Integer quotaDailyLimit;

    @Field("quota_daily_used")
    private Integer quotaDailyUsed;

    @Field("quota_hourly_limit")
    private Integer quotaHourlyLimit;

    @Field("quota_hourly_used")
    private Integer quotaHourlyUsed;

    @Field("latency_ms")
    private Long latencyMs;

    @Field("failure_reason")
    private String failureReason;

    @Indexed
    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("completed_at")
    private LocalDateTime completedAt;
}