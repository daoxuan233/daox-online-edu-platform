package com.daox.online.controller.teacher;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.views.responseVO.teacher.TeacherWeeklyOverviewVO;
import com.daox.online.service.TeacherDashboardService;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教师工作台控制器。
 * <p>
 * 对外提供教师首页所需的聚合统计接口，当前主要包含“本周概览”数据查询能力。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/dashboard")
public class TeacherDashboardController {

    @Resource
    private TeacherDashboardService teacherDashboardService;

    /**
     * 获取当前登录教师的本周概览数据。
     *
     * @param request HTTP 请求对象
     * @return 教师本周概览统计结果
     */
    @GetMapping("/weekly-overview")
    public RestBean<TeacherWeeklyOverviewVO> getWeeklyOverview(HttpServletRequest request) {
        String teacherId = UserUtils.getCurrentUserId(request);
        if (teacherId == null) {
            log.warn("[TeacherDashboardController.getWeeklyOverview] 当前用户未认证");
            return RestBean.unauthorized("用户未认证");
        }
        return RestBean.success(teacherDashboardService.getWeeklyOverview(teacherId));
    }
}