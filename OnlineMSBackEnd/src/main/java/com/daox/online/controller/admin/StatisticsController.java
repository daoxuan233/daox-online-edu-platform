package com.daox.online.controller.admin;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.views.responseVO.course.CourseDetailedStatisticsVo;
import com.daox.online.service.CoursesService;
import com.daox.online.service.UsersService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据统计分析
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/statistics")
public class StatisticsController {

    @Resource
    private UsersService usersService;

    @Resource
    private CoursesService coursesService;

    /**
     * 获取用户统计 - 现存数据库中所有的用户数量
     *
     * @return 用户统计数量
     */
    @GetMapping("/users")
    public RestBean<Integer> getUserStatistics() {
        return RestBean.success(usersService.getUserCount());
    }

    /**
     * 获取用户统计 - 有效数据
     * @return 用户统计数量
     */
    @GetMapping("/users/valid")
    public RestBean<Integer> getUserStatisticsValid() {
        return RestBean.success(usersService.getUserCountValid());
    }

    /**
     * 获取管理员统计数量
     *
     * @return 管理员统计数量
     */
    @GetMapping("/users/admin")
    public RestBean<Integer> getAdminStatistics() {
        return RestBean.success(usersService.getAdminCount());
    }

    /**
     * 获取管理员统计数量 - 获取有效数据
     *
     * @return 管理员统计数量
     */
    @GetMapping("/users/admin/valid")
    public RestBean<Integer> getAdminStatisticsValid() {
        return RestBean.success(usersService.getAdminCountValid());
    }

    /**
     * 获取教师统计数量
     *
     * @return 教师统计数量
     */
    @GetMapping("/users/teacher")
    public RestBean<Integer> getTeacherStatistics() {
        return RestBean.success(usersService.getTeacherCount());
    }

    /**
     * 获取教师统计数量 - 获取有效数据
     *
     * @return 教师统计数量
     */
    @GetMapping("/users/teacher/valid")
    public RestBean<Integer> getTeacherStatisticsValid() {
        return RestBean.success(usersService.getTeacherCountValid());
    }

    /**
     * 获取学生统计数量
     *
     * @return 学生统计数量
     */
    @GetMapping("/users/student")
    public RestBean<Integer> getStudentStatistics() {
        return RestBean.success(usersService.getStudentCount());
    }

    /**
     * 获取学生统计数量 - 获取有效数据
     *
     * @return 学生统计数量
     */
    @GetMapping("/users/student/valid")
    public RestBean<Integer> getStudentStatisticsValid() {
        return RestBean.success(usersService.getStudentCountValid());
    }

    /**
     * 获取课程统计 - 现存课程
     *
     * @return 课程统计数量
     */
    @GetMapping("/courses")
    public RestBean<Integer> getCourseStatistics() {
        return RestBean.success(coursesService.getCourseCount());
    }

    /**
     * 获取详细课程统计
     *
     * @return 详细课程统计信息
     */
    @GetMapping("/courses/detailed")
    public RestBean<CourseDetailedStatisticsVo> getCourseDetailedStatistics() {
        try {
            CourseDetailedStatisticsVo detailedStatistics = coursesService.getCourseDetailedStatistics();
            if (detailedStatistics == null) {
                log.error("[getCourseDetailedStatistics.method] 获取详细课程统计失败");
                return RestBean.failure(500, "获取详细课程统计失败");
            }
            return RestBean.success(detailedStatistics);
        } catch (Exception e) {
            log.error("[getCourseDetailedStatistics.method] 获取详细课程统计异常", e);
            return RestBean.failure(500, "获取详细课程统计异常");
        }
    }

}
