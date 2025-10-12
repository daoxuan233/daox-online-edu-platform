package com.daox.online.entity.views.responseVO.ratings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 课程评分统计
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class CourseRatingStatisticsVO {
    /**
     * 课程ID
     */
    private String courseId;
    /**
     * 课程标题
     */
    private String courseTitle;
    /**
     * 课程讲师ID
     */
    private String teacherId;
    /**
     * 讲师名称
     */
    private String teacherName;
    /**
     * 总评分数
     */
    private Integer totalRatings;
    /**
     * 平均分数(1.0-5.0)
     */
    private BigDecimal averageRating;
    /**
     * 内容质量分数(1.0-5.0)
     */
    private BigDecimal avgContentQuality;
    /**
     * 难度适中性维度的平均评分
     */
    private BigDecimal avgDifficultyLevel;
    /**
     * 实用性分数(1.0-5.0)
     */
    private BigDecimal avgPracticality;
    /**
     * 评分分布，显示各星级的评分数量
     */
    private Map<String, BigDecimal> ratingDistribution;

}
