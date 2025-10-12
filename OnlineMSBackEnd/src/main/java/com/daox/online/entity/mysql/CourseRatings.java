package com.daox.online.entity.mysql;

import java.io.Serializable;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 课程评分表<br />
 * 学生对课程的多维度评分和评价，支持匿名评分
 * TableName : course_ratings
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CourseRatings implements Serializable {

    /**
     * 评分ID
     */
    private String id;
    /**
     * 评分用户ID
     */
    private String userId;
    /**
     * 课程ID
     */
    private String courseId;
    /**
     * 总体评分 (1.0-5.0)
     */
    private BigDecimal overallRating;
    /**
     * 内容质量评分 (1.0-5.0)
     */
    private BigDecimal contentQuality;
    /**
     * 难度适中性评分 (1.0-5.0)
     */
    private BigDecimal difficultyLevel;
    /**
     * 实用性评分 (1.0-5.0)
     */
    private BigDecimal practicality;
    /**
     * 文字评价
     */
    private String comment;
    /**
     * 是否匿名评分 (0-实名, 1-匿名)
     */
    private Integer isAnonymous;
    /**
     * 创建时间
     */
    private Date createdAt;
    /**
     * 更新时间
     */
    private Date updatedAt;
    /**
     * 逻辑删除标志 (0-正常, 1-删除)
     */
    private Integer isDeleted;
}
