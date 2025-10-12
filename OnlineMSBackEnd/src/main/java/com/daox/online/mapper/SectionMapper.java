package com.daox.online.mapper;

import com.daox.online.entity.mysql.Sections;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SectionMapper {
    /**
     * 批量插入
     * @param sections 待插入的章节
     * @return 插入的行数
     */
    int insertBatch(List<Sections> sections);

    /**
     * 根据课程ID删除 (需要子查询)
     * @param courseId  课程ID
     * @return 删除的行数
     */
    int deleteByCourseId(String courseId);

    /**
     * 根据课程ID查询
     * @param courseId 课程ID
     * @return 小节列表
     */
    List<Sections> findByCourseId(String courseId);

    /**
     * 【新增】批量更新小节信息
     * @param sections 待更新的小节列表
     * @return 更新的行数
     */
    int updateBatch(List<Sections> sections);

    /**
     * 【新增】根据ID列表批量删除小节
     * @param ids 待删除的小节ID列表
     * @return 删除的行数
     */
    int deleteBatchByIds(@Param("ids") List<String> ids);

    /**
     * 根据ID查询
     * @param id 小节ID
     * @return 小节
     */
    @Select("SELECT * FROM sections WHERE id = #{id}")
    Sections findById(String id);

    /**
     * 更新小节视频信息
     * @param sectionId 小节ID
     * @param videoFileId 视频文件ID
     * @param durationSeconds 视频时长（秒）
     */
    @Update("UPDATE sections SET video_url = #{videoFileId}, duration_seconds = #{durationSeconds} WHERE id = #{sectionId}")
    void updateVideoInfo(@Param("sectionId") String sectionId, @Param("videoFileId") String videoFileId, @Param("durationSeconds") int durationSeconds);

    /**
     * 根据小节ID查询课程ID
     * @param sectionId 小节ID
     * @return 课程ID
     */
    @Select("SELECT c.course_id FROM sections s " +
            "JOIN chapters c ON s.chapter_id = c.id " +
            "WHERE s.id = #{sectionId}")
    String findCourseIdBySectionId(String sectionId);
}