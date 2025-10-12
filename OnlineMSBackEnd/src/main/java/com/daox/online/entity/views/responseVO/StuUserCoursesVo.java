package com.daox.online.entity.views.responseVO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class StuUserCoursesVo {
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
     * 课程状态 - 学习状态<br />
     * 'not_started [ 未开始 ]', 'in_progress [ 进行中 ]', 'completed [ 已完成 ]' , 'start_expired [ 已过期 ]'
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
     * 学习进度百分比
     */
    private Double progressPercentage;

    /**
     * 剩余学习时间
     */
    private Integer remainingTime;

    /**
     * 课程总课时数
     */
    private Integer totalSections;
}
