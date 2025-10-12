package com.daox.online.entity.views.responseVO.course;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 课程详情
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class CourseDetailVO implements Serializable {
    /**
     * 课程id
     */
    private String courseId;
    /**
     * 课程标题
     */
    private String title;
    /**
     * 课程描述
     */
    private String description;
    /**
     * 课程封面
     */
    private String cover;
    /**
     * 课程价格
     */
    private Integer price;
    /**
     * 课程原价，用于显示折扣
     */
    private Integer originalPrice;
    /**
     * 课程难度等级
     */
    private String level;
    /**
     * 课程总时长
     */
    private String duration;
    /**
     * 是否为新课程
     */
    private Boolean isNew;
    /**
     * 学习人数
     */
    private Integer studentCount;
    /**
     * 课程评分 (0-5)
     */
    private BigDecimal rating;
    /**
     * 评价总数
     */
    private Integer ratingCount;
    /**
     * 适合人群
     */
    private String targetAudience;
    /**
     * 技术要求
     */
    private String technicalRequirements;
    /**
     * 讲师信息
     */
    private CourseInstructorVO instructor;
}
