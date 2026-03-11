package com.daox.online.entity.dto.teacher.assessment;

import java.math.BigDecimal;

/**
 * 测评结果汇总 DTO
 * <p>
 * 用于教师端展示单个测评的核心结果指标。
 * </p>
 *
 * @param assessmentId          测评ID
 * @param actualParticipantCount 实际参考人数（已产生答卷记录的人数）
 * @param shouldParticipantCount 应参考人数（所属课程当前学习人数）
 * @param averageScore           平均分（测评总分之和 / 应参考人数）
 * @param completionRate         完成率（实际参考人数 / 应参考人数）
 */
public record AssessmentResultSummaryDTO(
        String assessmentId,
        long actualParticipantCount,
        long shouldParticipantCount,
        BigDecimal averageScore,
        BigDecimal completionRate
) {
}
