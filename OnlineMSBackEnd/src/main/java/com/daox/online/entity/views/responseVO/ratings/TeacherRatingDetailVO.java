package com.daox.online.entity.views.responseVO.ratings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 讲师评分详情VO
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class TeacherRatingDetailVO {
    /**
     * 评分ID
     */
    private String ratingId;
    /**
     * 课程标题
     */
    private String courseTitle;
    /**
     * 学生姓名（匿名时显示"匿名用户"）
     */
    private String studentName;
    /**
     * 总体评分
     */
    private BigDecimal overallRating;
    /**
     * 教学质量评分
     */
    private BigDecimal teachingQuality;
    /**
     * 互动性评分
     */
    private BigDecimal interaction;
    /**
     * 专业性评分
     */
    private BigDecimal professionalism;
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