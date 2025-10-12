package com.daox.online.entity.views.responseVO.ratings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 教师课程评分VO
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class TeacherCourseRatingVO {
    /**
     * 课程ID
     */
    private String courseId;
    /**
     * 课程标题
     */
    private String courseTitle;
    /**
     * 总评分数
     */
    private Integer totalRatings;
    /**
     * 平均分数
     */
    private BigDecimal averageRating;
    /**
     * 最新评分时间
     */
    private Date latestRatingTime;
    /**
     * 本月新增评分数
     */
    private Integer monthlyRatings;
}