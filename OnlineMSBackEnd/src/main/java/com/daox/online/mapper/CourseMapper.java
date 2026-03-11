package com.daox.online.mapper;

import com.daox.online.entity.mysql.Courses;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 课程相关数据访问接口 - XML
 */
@Mapper
public interface CourseMapper {

    // TODO 获取用户已学课程数
    // int getCourseRatingStatistics(@Param("userId") String userId);

    /**
     * 获取已报名课程数
     * @param userId 用户ID
     * @return 已报名课程数
     */
    @Select("SELECT COUNT(*) AS enrolled_course_count FROM user_courses WHERE user_id = #{userId}")
    Integer getEnrolledCourseCount(@Param("userId") String userId);

    /**
     * 获取用户实际有学习进度的课程数
     * @param userId 用户ID
     * @return 实际有学习进度的课程数
     */
    @Select("SELECT COUNT(DISTINCT c.course_id) AS studied_course_count " +
            "FROM learning_progress lp " +
            "JOIN sections s ON lp.section_id = s.id " +
            "JOIN chapters c ON s.chapter_id = c.id " +
            "WHERE lp.user_id = #{userId};")
    Integer getProgressCourseCount(@Param("userId") String userId);

    /**
     * 新增课程 (选择性)
     */
    int insertSelective(Courses record);

    /**
     * 根据主键查询课程
     */
    Courses selectByPrimaryKey(String id);

    /**
     * 根据主键更新课程 (选择性)
     */
    int updateByPrimaryKeySelective(Courses record);

    /**
     * 根据主键更新课程 (全量)
     */
    int updateByPrimaryKey(Courses record);

    /**
     * 根据主键逻辑删除课程
     */
    int deleteByPrimaryKey(String id);

    /**
     * 检查课程是否存在
     */
    int countById(String id);

    /**
     * 校验 courseId 是否真实存在
     * @param courseId 课程ID
     * @return 存在返回 true，不存在返回 false
     */
    @Select("SELECT EXISTS(SELECT 1 FROM courses WHERE id = #{courseId})")
    Boolean existsById(String courseId);

    @Select("SELECT id, title FROM courses WHERE id = #{id} AND is_deleted = 0")
    Courses findById(String id);

    /**
     * 根据课程ID列表批量查询课程信息
     * @param ids 课程ID列表
     * @return 课程列表
     */
    List<Courses> findByIdIn(@Param("ids") List<String> ids);
}