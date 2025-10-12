package com.daox.online.entity.dto.teacher.marking;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 提交评分的请求体 DTO
 *
 * @param submissionId 答卷ID
 * @param questionId   题目ID
 * @param score        分数
 * @param comment      评语
 */
public record GradeSubmissionRequest(
        @NotNull(message = "答卷ID不能为空") String submissionId,
        @NotNull(message = "题目ID不能为空") String questionId,
        @NotNull(message = "分数不能为空") BigDecimal score,
        String comment // 评语，可选
) {
}