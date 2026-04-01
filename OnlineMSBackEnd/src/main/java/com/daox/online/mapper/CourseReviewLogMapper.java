package com.daox.online.mapper;

import com.daox.online.entity.mysql.CourseReviewLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程审核流转记录数据访问接口。
 * <p>
 * 该表采用只增不改模式，每次课程状态流转都应写入一条历史记录。
 * </p>
 */
@Mapper
public interface CourseReviewLogMapper {

    /**
     * 新增一条课程审核流转记录。
     *
     * @param reviewLog 流转记录实体
     * @return 影响行数
     */
    @Insert("INSERT INTO course_review_log " +
            "(id, course_id, from_status, to_status, operator_id, operator_role, comment, created_at) " +
            "VALUES " +
            "(#{id}, #{courseId}, #{fromStatus}, #{toStatus}, #{operatorId}, #{operatorRole}, #{comment}, #{createdAt})")
    int insert(CourseReviewLog reviewLog);

    /**
     * 按课程查询审核历史。
     *
     * @param courseId 课程ID
     * @return 流转历史列表
     */
    @Select("SELECT id, course_id, from_status, to_status, operator_id, operator_role, comment, created_at " +
            "FROM course_review_log " +
            "WHERE course_id = #{courseId} " +
            "ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "course_id", property = "courseId"),
            @Result(column = "from_status", property = "fromStatus"),
            @Result(column = "to_status", property = "toStatus"),
            @Result(column = "operator_id", property = "operatorId"),
            @Result(column = "operator_role", property = "operatorRole"),
            @Result(column = "comment", property = "comment"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<CourseReviewLog> listByCourseId(@Param("courseId") String courseId);
}