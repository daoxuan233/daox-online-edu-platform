package com.daox.online.entity.views.responseVO.assessment;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 测评结果响应VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AssessmentResultVO {
    
    /**
     * 测评ID
     */
    private String assessmentId;
    
    /**
     * 测评标题
     */
    private String assessmentTitle;
    
    /**
     * 学生ID
     */
    private String userId;
    
    /**
     * 学生姓名
     */
    private String userName;
    
    /**
     * 答卷状态
     */
    private String status;
    
    /**
     * 提交时间
     */
    private Date submitTime;
    
    /**
     * 答题花费时间（秒）
     */
    private Integer timeSpentSeconds;
    
    /**
     * 总得分
     */
    private BigDecimal totalScore;
    
    /**
     * 满分
     */
    private BigDecimal fullScore;
    
    /**
     * 得分率
     */
    private Double scoreRate;
    
    /**
     * 题目详细结果
     */
    private List<QuestionResult> questionResults;
    
    /**
     * 题目结果内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class QuestionResult {
        /**
         * 题目ID
         */
        private String questionId;
        
        /**
         * 题目标题
         */
        private String questionTitle;
        
        /**
         * 题目类型
         */
        private String questionType;
        
        /**
         * 学生答案
         */
        private Object studentAnswer;
        
        /**
         * 正确答案
         */
        private Object correctAnswer;
        
        /**
         * 该题得分
         */
        private BigDecimal score;
        
        /**
         * 该题满分
         */
        private BigDecimal fullScore;
        
        /**
         * 是否正确
         */
        private Boolean isCorrect;
    }
}