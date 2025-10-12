package com.daox.online.entity.views.responseVO.ratings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程评分详情VO
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class CourseRatingDetailVO {
    /**
     * 评分ID
     */
    private String ratingId;
    /**
     * 学生姓名（匿名时显示"匿名用户"）
     */
    private String studentName;
    /**
     * 总体评分
     */
    private BigDecimal overallRating;
    /**
     * 内容质量评分
     */
    private BigDecimal contentQuality;
    /**
     * 难度适中性评分
     */
    private BigDecimal difficultyLevel;
    /**
     * 实用性评分
     */
    private BigDecimal practicality;
    /**
     * 文字评价
     */
    private String comment;
    /**
     * 是否匿名
     */
    private Boolean isAnonymous;
    /**
     * 评分时间
     */
    private Date createdAt;
}