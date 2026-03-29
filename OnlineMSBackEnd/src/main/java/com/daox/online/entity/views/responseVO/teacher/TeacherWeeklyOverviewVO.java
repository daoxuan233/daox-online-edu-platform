package com.daox.online.entity.views.responseVO.teacher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 教师端工作台本周概览响应对象。
 * <p>
 * 该对象聚合教师首页“本周概览”卡片所需的核心统计数据，
 * 便于前端一次性获取并渲染新增学生、课程完成、作业提交和平均评分等信息。
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TeacherWeeklyOverviewVO {

    /**
     * 本周新增学生数。
     */
    private Long newStudents;

    /**
     * 本周完成课时数。
     */
    private Long completedLessons;

    /**
     * 本周作业或测评提交数。
     */
    private Long submittedAssignments;

    /**
     * 本周教师平均评分。
     */
    private BigDecimal averageRating;
}