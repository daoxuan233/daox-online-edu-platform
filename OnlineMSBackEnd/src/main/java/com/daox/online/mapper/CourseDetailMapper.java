package com.daox.online.mapper;

import com.daox.online.entity.dto.CourseDetailDTO;
import com.daox.online.entity.views.responseVO.course.CourseVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseDetailMapper {
    /**
     * 获取课程详情基础信息
     */
    CourseDetailDTO getCourseDetailById(@Param("courseId") String courseId);

    /**
     * 获取课程总时长（秒）
     */
    Integer getCourseDurationSeconds(@Param("courseId") String courseId);

    /**
     * 获取讲师的课程列表
     */
    List<CourseVo> getInstructorCourses(@Param("teacherId") String teacherId);

    /**
     * 获取讲师的课程数量
     */
    Integer getInstructorCourseCount(@Param("teacherId") String teacherId);
}
