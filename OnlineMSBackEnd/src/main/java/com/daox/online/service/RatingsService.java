package com.daox.online.service;

import com.daox.online.entity.views.requestVO.ratings.CourseRatingRequest;
import com.daox.online.entity.views.requestVO.ratings.TeacherRatingRequest;
import com.daox.online.entity.views.responseVO.TopRatedCourseVO;
import com.daox.online.entity.views.responseVO.ratings.CourseRatingStatisticsVO;
import com.daox.online.entity.views.responseVO.ratings.RatingPermissionVO;
import com.daox.online.entity.views.responseVO.ratings.TeacherRatingStatisticsVO;

import java.util.List;

/**
 * 评论业务层
 */
public interface RatingsService {

    /**
     * 获取课程评分统计 - public API
     *
     * @param courseId 课程ID
     * @return 评分统计
     */
    CourseRatingStatisticsVO getCourseRatingStatistics(String courseId);

    /**
     * 获取讲师评分统计
     *
     * @param teacherId 讲师ID
     * @return 讲师评分统计信息
     */
    TeacherRatingStatisticsVO getTeacherRatingStatistics(String teacherId);

    /**
     * 获取热门课程排行
     *
     * @param limit 返回数量限制
     * @param minRatings 最少评分数量要求
     * @return 热门课程列表
     */
    List<TopRatedCourseVO> getTopRatedCourses(Integer limit, Integer minRatings);

    /**
     * 检查用户评分权限
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 评分权限信息
     */
    RatingPermissionVO checkRatingPermission(String userId, String courseId);

    // ==================== 学生接口 ====================

    /**
     * 提交课程评分
     *
     * @param userId 用户ID
     * @param request 评分请求
     * @return 提交结果
     */
    String submitCourseRating(String userId, CourseRatingRequest request);

    /**
     * 提交讲师评分
     *
     * @param userId 用户ID
     * @param request 评分请求
     * @return 提交结果
     */
    String submitTeacherRating(String userId, TeacherRatingRequest request);

    /**
     * 检查讲师评分权限
     *
     * @param userId 用户ID
     * @param teacherId 讲师ID
     * @return 评分权限信息
     */
    RatingPermissionVO checkTeacherRatingPermission(String userId, String teacherId);
}
