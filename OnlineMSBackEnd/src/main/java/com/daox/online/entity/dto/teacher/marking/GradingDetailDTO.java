package com.daox.online.entity.dto.teacher.marking;

import com.daox.online.entity.mysql.Assessments;

import java.math.BigDecimal;
import java.util.List;

/**
 * 阅卷详情 DTO
 * 用于展示单个测评任务的详情，主要是其包含的主观题列表及各题的批阅进度
 *
 * @param assessmentInfo      测评基本信息
 * @param subjectiveQuestions 主观题批阅进度列表
 */
public record GradingDetailDTO(
        Assessments assessmentInfo, // 测评基本信息
        List<SubjectiveQuestionProgress> subjectiveQuestions // 主观题批阅进度列表
) {
    /**
     * 内部记录，表示单个主观题的批阅进度
     *
     * @param questionId  题目ID
     * @param stem        题干预览
     * @param score       该题总分
     * @param gradedCount 已批阅数
     * @param totalCount  总需批阅数
     */
    public record SubjectiveQuestionProgress(
            String questionId,
            String stem, // 题干预览
            BigDecimal score, // 该题总分
            Long gradedCount, // 已批阅数
            Long totalCount   // 总需批阅数
    ) {
    }
}