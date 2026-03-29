package com.daox.online.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserCoursesMapper {
    /**
     * 检查学生是否已报名某课程
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 1 表示已报名，0 表示未报名
     */
    @Select("SELECT 1 FROM user_courses WHERE user_id = #{userId} AND course_id = #{courseId} LIMIT 1")
    Integer findByUserIdAndCourseId(@Param("userId") String userId, @Param("courseId") String courseId);

    /**
     * 获取课程下全部选课用户ID。
     *
     * @param courseId 课程ID
     * @return 用户ID列表
     */
    @Select("SELECT user_id FROM user_courses WHERE course_id = #{courseId}")
    List<String> findUserIdsByCourseId(@Param("courseId") String courseId);
}
