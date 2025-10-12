package com.daox.online.entity.mongodb.dto.paper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 最终返回给前端的完整试卷 DTO
 *
 * @param id              试卷ID
 * @param title           试卷标题
 * @param description     试卷描述
 * @param totalScore      总分
 * @param totalQuestions  题目总数
 * @param durationMinutes 考试时长(分)
 * @param sections        试卷章节列表
 */
public record StuPaperDTO(
        String id,
        String title,
        String description,
        BigDecimal totalScore,
        Integer totalQuestions,
        Integer durationMinutes,
        List<PaperSectionDTO> sections
) {
}