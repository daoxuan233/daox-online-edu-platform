package com.daox.online.entity.dto.teacher.marking;

/**
 * 阅卷任务列表项 DTO
 * 用于在阅卷中心首页展示一个待办任务
 * @param assessmentId 测评 ID
 * @param title 测评标题
 * @param courseName 课程名称
 * @param pendingSubmissionCount 待批阅份数
 * @param subjectiveQuestionCount 主观题数量
 */
public record GradingTaskDTO(
        String assessmentId,
        String title,
        String courseName,
        Long pendingSubmissionCount, // 待批阅份数
        Integer subjectiveQuestionCount // 主观题数量
) {}