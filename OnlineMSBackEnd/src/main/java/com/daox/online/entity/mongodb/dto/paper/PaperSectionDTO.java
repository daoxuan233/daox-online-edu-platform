package com.daox.online.entity.mongodb.dto.paper;

import java.math.BigDecimal;
import java.util.List;

/**
 * 试卷分区 DTO
 * @param title           节标题
 * @param description     节描述
 * @param totalQuestions  该分区的题目总数
 * @param totalScore      该分区的总分
 * @param questions       问题列表
 */
public record PaperSectionDTO(
        String title,
        String description,
        Integer totalQuestions,
        BigDecimal totalScore,
        List<PaperQuestionDTO> questions
) {
}
