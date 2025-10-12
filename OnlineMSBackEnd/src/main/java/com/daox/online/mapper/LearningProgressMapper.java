package com.daox.online.mapper;

import com.daox.online.entity.mysql.LearningProgress;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

/**
 * 学习进度Mapper
 */
@Mapper
public interface LearningProgressMapper {

    /**
     * 获取学习进度
     *
     * @param userId    用户ID
     * @param sectionId 小节ID
     * @return 学习进度
     */
    @Select("select * from learning_progress where user_id=#{userId} and section_id=#{sectionId}")
    LearningProgress getLearningProgress(String userId, String sectionId);

    /**
     * 更新学习进度
     *
     * @param userId          用户ID
     * @param sectionId       小节ID
     * @param progressSeconds 进度秒数
     * @param status          状态
     * @param updatedAt       更新时间
     * @return 更新结果
     */
    @Update("update learning_progress set progress_seconds=#{progressSeconds},status=#{status},updated_at=#{updatedAt} where user_id=#{userId} and section_id=#{sectionId}")
    int updateLearningProgress(String userId, String sectionId, Integer progressSeconds, String status, Date updatedAt);

    /**
     * 获取指定课程的已学习的总时长
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 总时长（秒）
     */
    @Select("SELECT SUM(progress_seconds) FROM learning_progress WHERE user_id = #{userId} AND section_id IN (SELECT id FROM sections WHERE chapter_id IN (SELECT id FROM chapters WHERE course_id = #{courseId}))")
    Integer getTotalLearningTimeByCourseId(String userId, String courseId);

    /**
     * 获取指定用户的总学习时长（秒）
     *
     * @param userId 用户ID
     * @return 总学习时长（秒）
     */
    int getTotalLearningTimeByUserId(String userId);

    /**
     * 获取用户加入课程的总小节数
     * @param userId 用户ID
     * @return 总小节数
     */
    Integer getTotalSections(@Param("userId") String userId);

    /**
     * 获取用户已完成的小节数
     * @param userId 用户ID
     * @return 已完成的小节数
     */
    Integer getCompletedSections(@Param("userId") String userId);

    /**
     * 获取用户特定课程的学习状态
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 学习状态
     */
    String getCourseStatusByUserIdAndCourseId(@Param("userId") String userId, @Param("courseId") String courseId);

    /**
     * 根据课程ID删除学习进度记录
     * @param courseId 课程ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM learning_progress WHERE section_id IN (SELECT id FROM sections WHERE chapter_id IN (SELECT id FROM chapters WHERE course_id = #{courseId}))")
    int deleteByCourseId(String courseId);

    /**
     * 根据用户ID和课程ID查询学习进度
     * @param userId 用户ID
     * @param sectionId 小节ID
     * @return 学习进度
     */
    @Select("SELECT * FROM learning_progress WHERE user_id = #{userId} AND section_id = #{sectionId}")
    LearningProgress findByUserAndSection(@Param("userId") String userId, @Param("sectionId") String sectionId);

    /**
     * 插入学习进度
     * @param progress 学习进度
     */
    @Insert("INSERT INTO learning_progress (id, user_id, section_id, status, progress_seconds, updated_at) " +
            "VALUES (#{id}, #{userId}, #{sectionId}, #{status}, #{progressSeconds}, NOW())")
    void insert(LearningProgress progress);

    /**
     * 更新学习进度
     * @param progress 学习进度
     */
    @Update("UPDATE learning_progress SET status = #{status}, progress_seconds = #{progressSeconds}, updated_at = NOW() " +
            "WHERE id = #{id}")
    void update(LearningProgress progress);

    /**
     * 根据小节ID列表，统计关联的学习进度记录数量
     * @param sectionIds 小节ID列表
     * @return 关联的学习进度总数
     */
    long countBySectionIds(@Param("sectionIds") List<String> sectionIds);
}
