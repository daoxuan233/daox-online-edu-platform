package com.daox.online.entity.mongodb.dto.paper;

import com.daox.online.entity.mongodb.Paper;
import com.daox.online.entity.mongodb.Question;

import java.util.Map;

/**
 * 封装了试卷和其包含的所有题目的详细信息
 * @param paper 试卷
 * @param questionsMap 包含所有题目信息的Map，键为题目ID，值为题目对象
 */
public record PaperDetails(Paper paper, Map<String, Question> questionsMap) {
}
