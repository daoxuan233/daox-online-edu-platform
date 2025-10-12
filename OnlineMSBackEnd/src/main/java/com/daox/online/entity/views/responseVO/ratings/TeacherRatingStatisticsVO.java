package com.daox.online.entity.views.responseVO.ratings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 讲师评分统计视图对象
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class TeacherRatingStatisticsVO {
    private String teacherId;                   // 讲师ID
    private String teacherName;                 // 讲师姓名
    private Integer totalRatings;               // 总评分数
    private BigDecimal averageRating;           // 平均评分
    private BigDecimal avgTeachingQuality;      // 教学质量平均分
    private BigDecimal avgInteraction;          // 互动性平均分
    private BigDecimal avgProfessionalism;      // 专业性平均分
    private Integer coursesTaught;              // 授课数量
    private Map<String, Integer> ratingDistribution; // 评分分布
}
