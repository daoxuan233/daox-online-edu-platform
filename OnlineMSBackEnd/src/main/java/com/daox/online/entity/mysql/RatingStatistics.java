package com.daox.online.entity.mysql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 评分统计表<br />
 * 实时统计课程和讲师的评分数据，用于快速查询和排序
 * TableName : rating_statistics
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RatingStatistics implements Serializable {

    /**
     * 统计ID
     */
    private String id;
    /**
     * 目标类型 (course-课程, teacher-讲师)
     */
    private String targetType;
    /**
     * 目标ID (课程ID或讲师ID)
     */
    private String targetId;
    /**
     * 总评分数量
     */
    private Integer totalRatings;
    /**
     * 平均评分
     */
    private BigDecimal averageRating;
    /**
     * 评分分布统计 (JSON格式: {"1":count, "2":count, ...})
     */
    private Object ratingDistribution;
    /**
     * 最后更新时间
     */
    private Date lastUpdated;
}
