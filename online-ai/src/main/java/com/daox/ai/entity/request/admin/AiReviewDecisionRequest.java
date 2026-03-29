package com.daox.ai.entity.request.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.daox.ai.entity.mongodb.AiCallRecord;

/**
 * 管理员审查请求体。
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AiReviewDecisionRequest {

    /**
     * 审查决策，仅允许 APPROVED 或 REJECTED。
     */
    private AiCallRecord.ReviewStatus decision;

    /**
     * 审查备注。
     */
    private String note;
}