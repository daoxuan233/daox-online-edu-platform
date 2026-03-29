package com.daox.online.service;

import com.daox.online.entity.views.responseVO.teacher.TeacherWeeklyOverviewVO;

/**
 * 教师工作台服务接口。
 * <p>
 * 提供教师首页本周概览统计能力，供教师端 dashboard 页面展示关键业务指标。
 * </p>
 */
public interface TeacherDashboardService {

    /**
     * 获取指定教师的本周概览数据。
     *
     * @param teacherId 教师 ID
     * @return 教师本周概览统计结果
     */
    TeacherWeeklyOverviewVO getWeeklyOverview(String teacherId);
}