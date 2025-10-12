package com.daox.online.mapper;

import com.daox.online.entity.mysql.Chapters;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChapterMapper {

    /**
     * 批量插入
     * @param chapters  章节
     * @return 插入的行数
     */
    int insertBatch(List<Chapters> chapters);

    /**
     * 根据课程ID删除
     * @param courseId 课程ID
     * @return 删除的行数
     */
    int deleteByCourseId(String courseId);

    /**
     * 根据课程ID查询
     * @param courseId 课程ID
     * @return 章节列表
     */
    List<Chapters> findByCourseId(String courseId);

    /**
     * 【新增】批量更新章节信息 (标题、排序)
     * @param chapters 待更新的章节列表
     * @return 更新的行数
     */
    int updateBatch(List<Chapters> chapters);

    /**
     * 【新增】根据ID列表批量删除章节
     * @param ids 待删除的章节ID列表
     * @return 删除的行数
     */
    int deleteBatchByIds(@Param("ids") List<String> ids);

    /**
     * 通过 chapterId 反查 courseId
     * @param chapterId 章节ID
     * @return 课程ID
     */
    @Select("SELECT course_id FROM chapters WHERE id = #{chapterId}")
    String findCourseIdByChapterId(String chapterId);
}
