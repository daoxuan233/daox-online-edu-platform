package com.daox.online.entity.views.responseVO.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 返回给前端的个人课程信息
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserCoursesVo implements Serializable {
    /**
     * 课程id
     */
    private String courseId;
    /**
     * 课程标题
     */
    private String courseTitle;
    /**
     * 课程描述
     */
    private String courseDescription;
    /**
     * 教师id
     */
    private String teacherId;
    /**
     * 教师姓名
     */
    private String teacherName;
    /**
     * 分类id
     */
    private String categoryId;
    /**
     * 分类名称
     */
    private String categoryName;
    /**
     * 课程封面
     */
    private String courseCover;
    /**
     * 课程状态
     */
    private String courseStatus;
    /**
     * 课程创建时间
     */
    private Date createTime;
    /**
     * 加入时间
     */
    private Date enrollmentDate;

    /**
     * 课程价格
     */
    private BigDecimal coursePrice;
}
