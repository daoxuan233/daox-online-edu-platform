package com.daox.online.entity.dto.teacher.marking;

public record GradingTaskStatusDTO(
        String assessmentId,
        long totalSubmissionCount,
        long pendingSubmissionCount,
        long gradedSubmissionCount,
        int progressPercentage,
        boolean completed
) {
}
