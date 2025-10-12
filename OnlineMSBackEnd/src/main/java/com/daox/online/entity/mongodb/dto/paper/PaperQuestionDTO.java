package com.daox.online.entity.mongodb.dto.paper;

import java.math.BigDecimal;

/**
 * 试卷中的题目 DTO (包含分值和题目详情)
 * @param questionId  题目ID
 * @param score       分数
 * @param orderIndex  题目顺序索引
 * @param questionDetails 题目详情
 */
public record PaperQuestionDTO(
        String questionId,
        BigDecimal score,
        Integer orderIndex,
        QuestionDTO questionDetails
) {
}