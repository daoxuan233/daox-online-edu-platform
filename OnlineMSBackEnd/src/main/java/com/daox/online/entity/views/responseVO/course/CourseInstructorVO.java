package com.daox.online.entity.views.responseVO.course;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 课程讲师信息
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class CourseInstructorVO implements Serializable {
    /**
     * 讲师ID
     */
    private String id;
    /**
     * 讲师名称
     */
    private String name;
    /**
     * 讲师职位/头衔
     */
    private String title;
    /**
     * 讲师简介
     */
    private String biography;
    /**
     * 讲师头像
     */
    private String avatar;
    /**
     * 讲师课程数量
     */
    private Integer courseCount;
    /**
     * 讲师课程列表
     */
    private List<CourseVo> courseList;
    /**
     * 讲师评分
     */
    private BigDecimal rating;
}
