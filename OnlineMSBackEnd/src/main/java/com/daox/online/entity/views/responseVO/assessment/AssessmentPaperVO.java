package com.daox.online.entity.views.responseVO.assessment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷内容VO
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentPaperVO {
    /**
     * 测评ID
     */
    private String assessmentId;
    
    /**
     * 测评标题
     */
    private String title;
    
    /**
     * 测评开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 测评结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 测评时长（分钟）
     */
    private Integer durationMinutes;
    
    /**
     * 题目列表
     */
    private List<QuestionVO> questions;
    
    /**
     * 学生已保存的答案
     */
    private List<SavedAnswerVO> savedAnswers;
    
    /**
     * 题目VO
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionVO {
        /**
         * 题目ID
         */
        private String questionId;
        
        /**
         * 题型
         */
        private String type;
        
        /**
         * 题干
         */
        private String stem;
        
        /**
         * 选项（仅选择题有）
         */
        private List<QuestionOptionVO> options;
        
        /**
         * 题目分值
         */
        private BigDecimal score;
    }
    
    /**
     * 题目选项VO
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionOptionVO {
        /**
         * 选项键值
         */
        private String key;
        
        /**
         * 选项内容
         */
        private String text;
    }
    
    /**
     * 已保存答案VO
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SavedAnswerVO {
        /**
         * 题目ID
         */
        private String questionId;
        
        /**
         * 学生答案
         */
        private Object response;
    }
}