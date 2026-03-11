package com.daox.online.entity.dto.teacher.marking;

import java.math.BigDecimal;

/**
 * 学生答案片段 DTO
 * 用于在沉浸式批阅界面展示单个学生的作答情况
 * @see com.daox.online.entity.mongodb.StudentAnswer
 * @param submissionId 作答记录的 id
 * @param studentId    学生 id
 * @param studentName  学生姓名
 * @param identifier   学号
 * @param gradingStatus 批阅状态
 * @param response     学生的回答
 * @param maxScore     当前题目的满分（来自试卷配置）
 * @param currentScore 当前得分
 * @param comment      当前评语
 */
public record StudentAnswerSnippetDTO(
        String submissionId, // StudentAnswer 的 id
        String studentId,
        String studentName, // 或其他匿名化标识
        String identifier, // 学号
        String gradingStatus, // 批阅状态
        Object response, // 学生对该题的回答
        BigDecimal maxScore, // 当前题目的满分（来自试卷）
        BigDecimal currentScore, // 当前得分
        String comment // 当前评语
) {
}
