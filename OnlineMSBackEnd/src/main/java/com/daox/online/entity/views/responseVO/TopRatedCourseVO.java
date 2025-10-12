package com.daox.online.entity.views.responseVO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 热门课程视图对象
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class TopRatedCourseVO {
    private String courseId;                    // 课程ID
    private String courseTitle;                 // 课程标题
    private String coverImageUrl;               // 课程封面图片URL
    private String teacherName;                 // 讲师姓名
    private Integer totalRatings;               // 总评分数
    private BigDecimal averageRating;           // 平均评分
    private Integer enrollmentCount;            // 选课人数
}
