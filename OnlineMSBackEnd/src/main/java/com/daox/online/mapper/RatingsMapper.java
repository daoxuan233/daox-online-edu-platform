package com.daox.online.mapper;

import com.daox.online.entity.mysql.CourseRatings;
import com.daox.online.entity.mysql.TeacherRatings;
import com.daox.online.entity.views.responseVO.TopRatedCourseVO;
import com.daox.online.entity.views.responseVO.ratings.CourseRatingStatisticsVO;
import com.daox.online.entity.views.responseVO.ratings.TeacherRatingStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface RatingsMapper {
    /**
     * 获取课程评分统计
     *
     * @param courseId 课程ID
     * @return 包含课程评分统计信息的Map
     */
    CourseRatingStatisticsVO getCourseRatingStatistics(@Param("courseId") String courseId);

    /**
     * 获取教师评分统计
     *
     * @param teacherId 教师ID
     * @return 评分统计信息
     */
    TeacherRatingStatisticsVO getTeacherRatingStatistics(@Param("teacherId") String teacherId);

    /**
     * 获取热门课程
     *
     * @param limit      限制数量
     * @param minRatings 最小评分数
     * @return 热门课程列表
     */
    List<TopRatedCourseVO> getTopRatedCourses(@Param("limit") Integer limit, @Param("minRatings") Integer minRatings);

    /**
     * 检查用户是否已选课
     */
    boolean isUserEnrolledInCourse(@Param("userId") String userId, @Param("courseId") String courseId);

    /**
     * 获取用户学习进度
     */
    BigDecimal getUserLearningProgress(@Param("userId") String userId, @Param("courseId") String courseId);

    /**
     * 检查用户是否已评分
     */
    boolean hasUserRatedCourse(@Param("userId") String userId, @Param("courseId") String courseId);

    /**
     * 获取用户最近评分次数
     */
    int getUserRecentRatingsCount(@Param("userId") String userId, @Param("hours") int hours);

    // ==================== 学生接口 ====================

    /**
     * 插入课程评分
     */
    int insertCourseRating(CourseRatings rating);

    /**
     * 插入讲师评分
     */
    int insertTeacherRating(TeacherRatings rating);

    /**
     * 检查用户是否选修过该讲师的课程
     */
    boolean hasUserTakenTeacherCourse(@Param("userId") String userId, @Param("teacherId") String teacherId);

    /**
     * 检查用户是否已评分讲师
     */
    boolean hasUserRatedTeacher(@Param("userId") String userId, @Param("teacherId") String teacherId);

    /**
     * 更新评分统计数据
     */
    void updateRatingStatistics(@Param("targetId") String targetId, @Param("targetType") String targetType);

}
