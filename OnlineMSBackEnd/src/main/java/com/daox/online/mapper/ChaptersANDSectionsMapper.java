package com.daox.online.mapper;

import com.daox.online.entity.mysql.Chapters;
import com.daox.online.entity.mysql.Sections;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChaptersANDSectionsMapper {
    /**
     * 获取课程章节
     *
     * @param courseId 课程id
     * @return 章节列表
     */
    @Select("select * from chapters where course_id=#{courseId}")
    @Results({@Result(id = true, column = "id", property = "id"), @Result(column = "course_id", property = "courseId"), @Result(column = "title", property = "title"), @Result(column = "order_index", property = "orderIndex")})
    List<Chapters> getChaptersByCourseId(String courseId);

    /**
     * 获取章节中小节信息 - 小节数
     *
     * @param chapterId 章节ID
     * @return 章节信息
     */
    @Select("SELECT COUNT(*) FROM sections WHERE chapter_id = #{chapterId}")
    int countSectionsByChapterId(String chapterId);

    /**
     * 获取视频播放地址<br />
     * 通过id查询小节信息
     *
     * @param sectionId 小节ID
     * @return 小节信息
     */
    @Select("select * from sections where id=#{sectionId}")
    @Results({@Result(column = "id", property = "id"), @Result(column = "chapter_id", property = "chapterId"), @Result(column = "title", property = "title"), @Result(column = "video_url", property = "videoUrl"), @Result(column = "duration_seconds", property = "durationSeconds"), @Result(column = "order_index", property = "orderIndex")})
    Sections getSectionById(String sectionId);


    /**
     * 获取章节中小节信息
     *
     * @param chapterId 章节ID
     * @return 小节列表
     */
    @Select("SELECT * FROM sections WHERE chapter_id = #{chapterId} ORDER BY order_index")
    @Results({@Result(column = "id", property = "id"), @Result(column = "chapter_id", property = "chapterId"), @Result(column = "title", property = "title"), @Result(column = "video_url", property = "videoUrl"), @Result(column = "duration_seconds", property = "durationSeconds"), @Result(column = "order_index", property = "orderIndex"),})
    List<Sections> getSectionsByChapterId(String chapterId);

    /**
     * 创建章节
     *
     * @param chapter 章节
     * @return 创建结果
     */
    @Insert("insert into chapters (id,course_id, title, order_index) values (#{id},#{courseId}, #{title}, #{orderIndex})")
    int createChapter(Chapters chapter);

    /**
     * 更新章节
     *
     * @param id         章节ID
     * @param title      章节标题
     * @param orderIndex 章节顺序
     * @return 更新结果
     */
    @Update("update chapters set title = #{title}, order_index = #{orderIndex} where id = #{id}")
    int updateChapter(String id, String title, Integer orderIndex);

    /**
     * 校验章节是否存在
     *
     * @param chapterId 章节ID
     * @return 存在结果
     */
    @Select("select count(*)>0 from chapters where id = #{chapterId}")
    boolean checkChapterExists(String chapterId);

    /**
     * 创建小节
     *
     * @param sections 小节信息
     * @return 创建结果
     */
    @Insert("insert into sections(id,chapter_id, title, video_url, duration_seconds,order_index) values(#{id},#{chapterId}, #{title}, #{videoUrl}, #{durationSeconds},#{orderIndex})")
    int createSection(Sections sections);

    /**
     * 获取指定课程的总小节数
     * @param courseId 课程ID
     * @return 小节数
     */
    @Select("SELECT COUNT(*) FROM sections WHERE chapter_id IN (SELECT id FROM chapters WHERE course_id = #{courseId})")
    Integer getTotalSectionsByCourseId(String courseId);

    /**
     * 获取用户在指定课程中已完成的小节数
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 小节数
     */
    @Select("SELECT COUNT(*) FROM learning_progress WHERE user_id = #{userId} AND section_id IN (SELECT id FROM sections WHERE chapter_id IN (SELECT id FROM chapters WHERE course_id = #{courseId}))")
    Integer getCompletedSectionsByUserIdAndCourseId(String userId, String courseId);

    /**
     * 获取指定课程的小节的总时间
     * @param courseId 课程ID
     * @return 小节总时间
     */
    @Select("SELECT SUM(duration_seconds) FROM sections WHERE chapter_id IN (SELECT id FROM chapters WHERE course_id = #{courseId})")
    Integer getTotalDurationByCourseId(String courseId);

}

