package com.daox.online.entity.mongodb.dto.paper;

import com.daox.online.entity.mongodb.Question;

import java.util.List;

/**
 * [安全] 题目详情 DTO (过滤了答案、解析等敏感信息)
 * @param id 题目ID
 * @param type 题型
 * @param stem 题干
 * @param options 选项列表
 */
public record QuestionDTO(
        String id,
        Question.QuestionType type,
        String stem,
        List<Question.QuestionOption> options
) {
}