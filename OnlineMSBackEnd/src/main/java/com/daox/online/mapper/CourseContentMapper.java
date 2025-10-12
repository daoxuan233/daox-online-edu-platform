package com.daox.online.mapper;

import com.daox.online.entity.mysql.Chapters;
import com.daox.online.entity.mysql.LearningProgress;
import com.daox.online.entity.mysql.Sections;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseContentMapper {

    /**
     * 1. 获取课程下的所有章节
     */
    @Select("SELECT * FROM chapters WHERE course_id = #{courseId} ORDER BY order_index ")
    List<Chapters> findChaptersByCourseId(String courseId);

    /**
     * 2. 一次性获取课程下的所有小节
     */
    @Select("SELECT s.* FROM sections s JOIN chapters c ON s.chapter_id = c.id " + "WHERE c.course_id = #{courseId} ORDER BY c.order_index ASC, s.order_index ")
    List<Sections> findSectionsByCourseId(String courseId);

    /**
     * 定义一个 DTO 专门用于接收 JOIN 查询的结果
     */
    class MaterialDetail {
        public String id;
        public String title;
        public String fileId;
        public String mimeType;
        public String sectionId; // 需要这个字段来分组
    }

    /**
     * 3. 一次性获取课程下的所有小节资料，并 JOIN files 表获取 mime_type
     */
    @Select("SELECT cm.id, cm.title, cm.file_id as fileId, f.mime_type as mimeType, cm.section_id as sectionId " + "FROM course_materials cm JOIN files f ON cm.file_id = f.id " + "WHERE cm.course_id = #{courseId} AND cm.is_deleted = 0 AND cm.section_id IS NOT NULL")
    List<MaterialDetail> findSectionMaterialsByCourseId(String courseId);

    /**
     * 4. 一次性获取学生在指定课程下的所有学习进度
     */
    @Select("SELECT lp.* FROM learning_progress lp "
            + "JOIN sections s ON lp.section_id = s.id "
            + "JOIN chapters c ON s.chapter_id = c.id "
            + "WHERE lp.user_id = #{userId} AND c.course_id = #{courseId}")
    List<LearningProgress> findProgressByStudentAndCourse(@Param("userId") String userId, @Param("courseId") String courseId);

    /**
     * 查询指定课程下的【课程级别】资料。
     * 查询条件为 section_id 和 chapter_id 都为 NULL。
     *
     * @param courseId 课程ID
     * @return 课程级别的资料列表
     */
    @Select("SELECT cm.id, cm.title, cm.file_id as fileId, f.mime_type as mimeType "
            + "FROM course_materials cm JOIN files f ON cm.file_id = f.id "
            + "WHERE cm.course_id = #{courseId} AND cm.is_deleted = 0 "
            + "AND cm.section_id IS NULL AND cm.chapter_id IS NULL "
            + "ORDER BY cm.upload_time ")
    List<MaterialDetail> findCourseLevelMaterials(String courseId);
}
