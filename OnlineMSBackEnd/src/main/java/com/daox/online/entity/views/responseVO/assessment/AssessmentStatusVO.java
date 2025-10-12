package com.daox.online.entity.views.responseVO.assessment;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 测评状态响应VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AssessmentStatusVO {
    
    /**
     * 测评ID
     */
    private String assessmentId;
    
    /**
     * 答卷状态: in_progress (答题中), submitted (已提交), grading (批阅中), graded (已批阅)
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
     * 已答题目数量
     */
    private Integer answeredCount;
    
    /**
     * 总题目数量
     */
    private Integer totalQuestions;
    
    /**
     * 答题进度百分比
     */
    private Double progressPercentage;
}