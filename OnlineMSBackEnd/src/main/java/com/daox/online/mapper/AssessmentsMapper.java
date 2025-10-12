package com.daox.online.mapper;

import com.daox.online.entity.mysql.Assessments;
import com.daox.online.entity.views.responseVO.StudentAssessmentVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AssessmentsMapper {

    /**
     * 获取学生的测评列表
     */
    @Select("""
                SELECT
                    a.id as assessment_id,
                    a.title,
                    a.start_time,
                    a.end_time,
                    a.duration_minutes,
                    a.course_id,
                    c.title as course_title,
                    c.cover_image_url as course_cover
                FROM assessments a
                INNER JOIN user_courses uc ON a.course_id = uc.course_id
                INNER JOIN courses c ON a.course_id = c.id
                WHERE uc.user_id = #{userId}
                AND a.is_published = 1
                AND c.status = 'published'
                AND c.is_deleted = 0
                ORDER BY a.start_time DESC
            """)
    List<StudentAssessmentVO> getStudentAssessments(@Param("userId") String userId);

    /**
     * 根据ID获取单个测评信息
     */
    @Select("""
                SELECT
                    a.id as assessmentId,
                    a.title,
                    a.start_time as startTime,
                    a.end_time as endTime,
                    a.duration_minutes as durationMinutes,
                    a.course_id as courseId,
                    c.title as courseTitle,
                    c.cover_image_url as courseCover
                FROM assessments a
                INNER JOIN user_courses uc ON a.course_id = uc.course_id
                INNER JOIN courses c ON a.course_id = c.id
                WHERE a.id = #{assessmentId}
                AND uc.user_id = #{userId}
                AND a.is_published = 1
                AND c.status = 'published'
                AND c.is_deleted = 0
            """)
    StudentAssessmentVO getAssessmentByIdComplex(@Param("assessmentId") String assessmentId, @Param("userId") String userId);

    /**
     * 获取测评列表 - 课程下
     *
     * @param courseID 课程ID
     * @return 测评列表
     */
    @Select("select * from assessments where course_id=#{courseID}")
    List<Assessments> getAssessmentsByCourseID(@Param("courseID") String courseID);

    /**
     * 获取所有测评
     *
     * @return 所有测评
     */
    @Select("select * from assessments")
    List<Assessments> getAssessmentsAll();

    /**
     * 创建测评
     *
     * @param assessments 测评信息
     * @return 创建结果
     */
    @Insert("insert into assessments(id, course_id, creator_id, assessment_type, title, start_time, end_time, duration_minutes, is_published) " +
            "values (#{id},#{courseId},#{creatorId},#{assessmentType},#{title},#{startTime},#{endTime},#{durationMinutes},#{isPublished})")
    int createAssessment(Assessments assessments);

    /**
     * 通过id查询测评
     *
     * @param id 测评id
     * @return 测评信息
     */
    @Select("select * from assessments where id=#{id}")
    Assessments getAssessmentById(@Param("id") String id);

    /**
     * 修改测评信息
     *
     * @param assessments 测评信息
     * @return 修改结果
     */
    @Update("update assessments set title=#{title},duration_minutes=#{duration},start_time=#{startTime},end_time=#{endTime},is_published=#{isPublished} where id=#{id}")
    int updateAssessment(Assessments assessments);

    /**
     * 删除测评 - 逻辑处理 - 将is_published = 2
     *
     * @param id 测评id
     * @return 删除结果
     */
    @Update("update assessments set is_published=2 where id=#{id}")
    int deleteAssessment(@Param("id") String id);

    /**
     * 发布测评
     *
     * @param id 测评id
     * @return 发布结果
     */
    @Update("update assessments set is_published=1 where id= #{id}")
    int publishAssessment(@Param("id") String id);

    /**
     * 通过assessmentId查询is_published不等于2的测评
     *
     * @param id 测评id
     * @return 测评信息
     */
    @Select("select * from assessments where id=#{id} and is_published!=2")
    Assessments getAssessmentByIdANDNotDelete(@Param("id") String id);

    /**
     * 通过creatorId查询所有测评
     * @param creatorId 创建者id
     * @return 测评列表
     */
    @Select("select id from assessments where creator_id=#{creatorId}")
    List<String> findIdsByCreatorId(@Param("creatorId") String creatorId);

    /**
     * 根据测评 ID 列表批量查询测评信息
     * @param ids 测评 ID 列表
     * @return Assessments 实体列表
     */
    List<Assessments> findByIdIn(@Param("ids") List<String> ids);
}
