package com.daox.online.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 教师工作台统计 Mapper。
 * <p>
 * 负责查询教师首页“本周概览”所需的 MySQL 统计数据，
 * 包括新增学生、完成课时和平均评分等指标。
 * </p>
 */
@Mapper
public interface TeacherDashboardMapper {

    /**
     * 统计教师在指定时间范围内的新增学生数。
     * <p>
     * 统计口径为：在该教师名下任意课程中，本周首次或再次选课的学生去重人数。
     * 为避免一个学生在一周内加入多门课程导致重复计数，这里按学生 ID 去重。
     * </p>
     *
     * @param teacherId 教师 ID
     * @param weekStart 周起始时间（含）
     * @param weekEnd   周结束时间（不含）
     * @return 新增学生数
     */
    @Select({
            "SELECT COALESCE(COUNT(DISTINCT uc.user_id), 0)",
            "FROM user_courses uc",
            "INNER JOIN courses c ON uc.course_id = c.id",
            "WHERE c.teacher_id = #{teacherId}",
            "  AND c.is_deleted = 0",
            "  AND uc.enrollment_date >= #{weekStart}",
            "  AND uc.enrollment_date < #{weekEnd}"
    })
    Long countWeeklyNewStudents(@Param("teacherId") String teacherId,
                                @Param("weekStart") Date weekStart,
                                @Param("weekEnd") Date weekEnd);

    /**
     * 统计教师在指定时间范围内的完成课时数。
     * <p>
     * 统计口径为：教师名下课程的学习进度记录中，状态为 completed 且更新时间落在本周内的记录总数。
     * 该指标反映的是学生对教师课程章节/小节的完成情况。
     * </p>
     *
     * @param teacherId 教师 ID
     * @param weekStart 周起始时间（含）
     * @param weekEnd   周结束时间（不含）
     * @return 完成课时数
     */
    @Select({
            "SELECT COALESCE(COUNT(lp.id), 0)",
            "FROM learning_progress lp",
            "INNER JOIN sections s ON lp.section_id = s.id",
            "INNER JOIN chapters ch ON s.chapter_id = ch.id",
            "INNER JOIN courses c ON ch.course_id = c.id",
            "WHERE c.teacher_id = #{teacherId}",
            "  AND c.is_deleted = 0",
            "  AND lp.status = 'completed'",
            "  AND lp.updated_at >= #{weekStart}",
            "  AND lp.updated_at < #{weekEnd}"
    })
    Long countWeeklyCompletedLessons(@Param("teacherId") String teacherId,
                                     @Param("weekStart") Date weekStart,
                                     @Param("weekEnd") Date weekEnd);

    /**
     * 统计教师在指定时间范围内的平均评分。
     * <p>
     * 评分来源为 teacher_ratings 表中的教师维度评分，
     * 仅统计未删除且创建时间落在本周内的评分记录。
     * </p>
     *
     * @param teacherId 教师 ID
     * @param weekStart 周起始时间（含）
     * @param weekEnd   周结束时间（不含）
     * @return 平均评分，若没有评分则返回 0
     */
    @Select({
            "SELECT COALESCE(AVG(tr.overall_rating), 0)",
            "FROM teacher_ratings tr",
            "WHERE tr.teacher_id = #{teacherId}",
            "  AND tr.is_deleted = 0",
            "  AND tr.created_at >= #{weekStart}",
            "  AND tr.created_at < #{weekEnd}"
    })
    BigDecimal getWeeklyAverageRating(@Param("teacherId") String teacherId,
                                      @Param("weekStart") Date weekStart,
                                      @Param("weekEnd") Date weekEnd);
}