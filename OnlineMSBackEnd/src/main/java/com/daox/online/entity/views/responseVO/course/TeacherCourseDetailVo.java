package com.daox.online.entity.views.responseVO.course;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 教师端 - 课程详情
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class TeacherCourseDetailVo implements Serializable {

    /**
     * 课程ID
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
     * 分类
     */
    String categories;
    /**
     * 状态
     */
    private String status;
    /**
     * 预期学习人数
     */
    private Integer enrollmentCount;
    /**
     * 是否删除 [1-"删除"，0-"正常"]
     */
    private Integer isDeleted;
    /**
     * 是否私有 [1-"私有"，0-"公开"]
     */
    private Integer isPrivate;
    /**
     * 课程难度
     */
    private String level;
    /**
     * 是否为新课程
     */
    private String isNew;
    /**
     * 适合人群 - 目标用户
     */
    private String targetAudience;
    /**
     * 课程技术要求
     */
    private String requirements;
    /**
     * 当前价格
     */
    private BigDecimal price;
    /**
     * 原价，用于显示折扣
     */
    private BigDecimal originalPrice;

}
