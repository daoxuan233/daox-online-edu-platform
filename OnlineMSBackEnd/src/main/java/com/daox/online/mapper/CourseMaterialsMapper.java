package com.daox.online.mapper;

import com.daox.online.entity.mysql.CourseMaterials;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 课程资料Mapper接口
 */
@Mapper
public interface CourseMaterialsMapper {
    /**
     * 插入课程资料
     *
     * @param material 课程资料
     */
    @Insert("INSERT INTO course_materials (id, course_id, chapter_id, section_id, title, description, file_id, upload_time, updated_at) " +
            "VALUES (#{id}, #{courseId}, #{chapterId}, #{sectionId}, #{title}, #{description}, #{fileId}, NOW(), NOW())")
    void insert(CourseMaterials material);


    /**
     * 根据小节ID列表，统计关联的课程资料数量
     * @param sectionIds 小节ID列表
     * @return 关联的资料总数
     */
    long countBySectionIds(@Param("sectionIds") List<String> sectionIds);
}