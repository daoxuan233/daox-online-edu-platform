package com.daox.online.controller.students;

import com.daox.online.controller.exception.BusinessException;
import com.daox.online.entity.RestBean;
import com.daox.online.entity.views.responseVO.ChapterBriefVo;
import com.daox.online.entity.views.responseVO.StuUserCoursesVo;
import com.daox.online.entity.views.responseVO.course.CourseDetailVO;
import com.daox.online.entity.views.responseVO.course.CourseVo;
import com.daox.online.service.ChaptersANDSectionsService;
import com.daox.online.service.CourseDetailService;
import com.daox.online.service.CoursesService;
import com.daox.online.uilts.UserUtils;
import com.daox.online.uilts.constant.RatingErrorCodes;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生端 - 课程相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/student/course")
public class StuCoursesController {

    @Resource
    private CoursesService coursesService;

    @Resource
    private ChaptersANDSectionsService chaptersANDSectionsService;

    @Resource
    private CourseDetailService courseDetailService;

    /**
     * 获取我的课程列表
     *
     * @param request 请求
     * @return 我的课程列表
     */
    @GetMapping("/list")
    public RestBean<List<StuUserCoursesVo>> getMyCourseList(HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) return RestBean.failure(401, "用户未认证！");
        List<StuUserCoursesVo> myCourseList = coursesService.getStuMyCourseList(userId);
        if (myCourseList == null) return RestBean.failure(404, "查询错误！");
        return RestBean.success(myCourseList);
    }

    /**
     * 课程搜索 - 关键词
     *
     * @param keyword 关键词
     * @return 课程列表
     */
    @GetMapping("/search/keyword")
    public RestBean<List<CourseVo>> searchCourses(@RequestParam("keyword") String keyword) {
        List<CourseVo> courseVos = coursesService.searchCourses(keyword);
        if (courseVos == null) return RestBean.failure(404, "查询错误！");
        return RestBean.success(courseVos);
    }

    /**
     * 课程搜索 - 分类
     *
     * @param categoryId 分类ID
     * @return 课程列表
     */
    @GetMapping("/search/category")
    public RestBean<List<CourseVo>> searchCoursesByCategory(@RequestParam("categoryId") String categoryId) {
        List<CourseVo> courseVos = coursesService.searchCoursesByCategory(categoryId);
        if (courseVos == null) return RestBean.failure(404, "查询错误！");
        return RestBean.success(courseVos);
    }

    /**
     * 搜索课程 - 等级
     *
     * @param level 等级
     * @return 课程列表
     */
    @GetMapping("/search/level")
    public RestBean<List<CourseVo>> searchCoursesByLevel(@RequestParam("level") String level) {
        List<CourseVo> courseVos = coursesService.searchCoursesByLevel(level);
        if (courseVos == null) return RestBean.failure(404, "查询错误！");
        return RestBean.success(courseVos);
    }

    /**
     * 获取课程详细信息
     *
     * @param courseId 课程ID
     * @return 课程详细信息
     */
    @GetMapping("/courses/detail")
    public RestBean<CourseDetailVO> getCourseDetail(@RequestParam("courseId") String courseId) {
        try {
            CourseDetailVO courseDetail = courseDetailService.getCourseDetail(courseId);
            return RestBean.success(courseDetail);
        } catch (BusinessException e) {
            return RestBean.failure(Integer.parseInt(e.getCode()), e.getMessage());
        } catch (Exception e) {
            log.error("课程详情查询异常: courseId={}", courseId, e);
            return RestBean.failure(Integer.parseInt(RatingErrorCodes.RATING_SYSTEM_DATABASE_ERROR), "系统繁忙，请稍后重试");
        }
    }

    /**
     * 加入课程
     *
     * @param request  请求
     * @param courseId 课程ID
     * @return 加入结果
     */
    @PostMapping("/enroll")
    public RestBean<String> joinCourse(HttpServletRequest request, @RequestParam("courseId") String courseId) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) return RestBean.failure(401, "用户未认证！");
        if (coursesService.isUserEnrolledInCourse(userId, courseId)) {
            return RestBean.failure(409, "已加入课程");
        }
        boolean result = coursesService.joinCourse(userId, courseId);
        if (!result) return RestBean.failure(500, "加入课程失败！");
        return RestBean.success("加入课程成功！");
    }

    /**
     * 退出课程
     *
     * @param request  请求
     * @param courseId 课程ID
     * @return 退出课程结果
     */
    @PostMapping("/unenroll")
    public RestBean<String> quitCourse(HttpServletRequest request, @RequestParam("courseId") String courseId) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) return RestBean.failure(401, "用户未认证！");
        boolean result = coursesService.quitCourse(userId, courseId);
        if (!result) return RestBean.failure(500, "退出课程失败！");
        return RestBean.success("退出课程成功！");
    }

    /**
     * 获取课程章节 - 仅列表
     *
     * @param courseId 课程ID
     * @return 课程章节简要信息
     */
    @GetMapping("/getCourseChapters")
    public RestBean<List<ChapterBriefVo>> getCourseChapters(@RequestParam("courseId") String courseId) {
        List<ChapterBriefVo> chapterBriefVos = chaptersANDSectionsService.getChaptersByCourseId(courseId);
        if (chapterBriefVos == null || chapterBriefVos.isEmpty()) return RestBean.failure(404, "查询错误！");
        return RestBean.success(chapterBriefVos);
    }

    /**
     * 获取已完成课程数量
     *
     * @param request 请求
     * @return 已完成课程数量
     */
    @GetMapping("/completed-courses-count")
    public RestBean<Integer> getCompletedCoursesCount(HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) return RestBean.failure(401, "用户未认证！");
        Integer count = coursesService.getCompletedCoursesCount(userId);
        if (count == null) return RestBean.failure(404, "已完成课程为空！");
        return RestBean.success(count);
    }
}
