package com.daox.online.service.Impl;

import com.daox.online.entity.mongodb.StudentAnswer;
import com.daox.online.entity.mysql.Courses;
import com.daox.online.entity.views.responseVO.teacher.TeacherWeeklyOverviewVO;
import com.daox.online.mapper.CoursesMapper;
import com.daox.online.mapper.TeacherDashboardMapper;
import com.daox.online.service.TeacherDashboardService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 教师工作台服务实现。
 * <p>
 * 该实现负责聚合 MySQL 与 MongoDB 中的统计数据，
 * 输出教师首页“本周概览”所需的统一响应结构。
 * </p>
 */
@Service
@Slf4j
public class TeacherDashboardServiceImpl implements TeacherDashboardService {

    /**
     * 可视为“已提交”的答卷状态集合。
     * <p>
     * 这里同时兼容 submitted、grading、graded、review 四类状态，
     * 避免历史数据或已完成批阅的数据在本周提交统计中被漏掉。
     * </p>
     */
    private static final List<String> WEEKLY_SUBMISSION_STATUSES = Arrays.asList(
            StudentAnswer.AnswerStatus.SUBMITTED,
            StudentAnswer.AnswerStatus.GRADING,
            StudentAnswer.AnswerStatus.GRADED,
            StudentAnswer.AnswerStatus.REVIEW
    );

    @Resource
    private TeacherDashboardMapper teacherDashboardMapper;

    @Resource
    private CoursesMapper coursesMapper;

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 获取教师端本周概览统计信息。
     * <p>
     * 统计范围定义为“本周一 00:00:00 至下周一 00:00:00”，
     * 统一按左闭右开区间进行查询，避免边界时间重复统计。
     * </p>
     *
     * @param teacherId 教师 ID
     * @return 本周概览统计结果
     */
    @Override
    public TeacherWeeklyOverviewVO getWeeklyOverview(String teacherId) {
        WeeklyRange weeklyRange = resolveCurrentWeekRange();
        long newStudents = safeLong(teacherDashboardMapper.countWeeklyNewStudents(
                teacherId,
                weeklyRange.getWeekStart(),
                weeklyRange.getWeekEnd()
        ));
        long completedLessons = safeLong(teacherDashboardMapper.countWeeklyCompletedLessons(
                teacherId,
                weeklyRange.getWeekStart(),
                weeklyRange.getWeekEnd()
        ));
        BigDecimal averageRating = normalizeAverageRating(teacherDashboardMapper.getWeeklyAverageRating(
                teacherId,
                weeklyRange.getWeekStart(),
                weeklyRange.getWeekEnd()
        ));
        long submittedAssignments = countWeeklySubmittedAssignments(teacherId, weeklyRange);

        log.info("[TeacherDashboardServiceImpl.getWeeklyOverview] 教师 {} 本周概览统计完成: newStudents={}, completedLessons={}, submittedAssignments={}, averageRating={}",
                teacherId, newStudents, completedLessons, submittedAssignments, averageRating);

        return new TeacherWeeklyOverviewVO()
                .setNewStudents(newStudents)
                .setCompletedLessons(completedLessons)
                .setSubmittedAssignments(submittedAssignments)
                .setAverageRating(averageRating);
    }

    /**
     * 统计教师课程在本周内的作业/测评提交数。
     * <p>
     * 由于答卷数据存储在 MongoDB 中，这里先查询教师名下课程 ID，
     * 再以课程集合和提交时间范围作为条件执行计数。
     * </p>
     *
     * @param teacherId   教师 ID
     * @param weeklyRange 本周时间范围
     * @return 本周提交数
     */
    private long countWeeklySubmittedAssignments(String teacherId, WeeklyRange weeklyRange) {
        List<String> teacherCourseIds = coursesMapper.getTeacherCourseList(teacherId).stream()
                .filter(Objects::nonNull)
                .filter(course -> course.getIsDeleted() == null || course.getIsDeleted() == 0)
                .map(Courses::getId)
                .filter(Objects::nonNull)
                .toList();

        if (teacherCourseIds.isEmpty()) {
            return 0L;
        }

        Criteria courseCriteria = Criteria.where("course_id").in(teacherCourseIds);
        Criteria submitTimeCriteria = Criteria.where("submit_time")
            .gte(weeklyRange.getWeekStartDateTime())
            .lt(weeklyRange.getWeekEndDateTime());
        Criteria statusCriteria = new Criteria().orOperator(
                Criteria.where("status").in(WEEKLY_SUBMISSION_STATUSES),
                Criteria.where("status").exists(false),
                Criteria.where("status").is(null)
        );
        Query query = new Query(new Criteria().andOperator(courseCriteria, submitTimeCriteria, statusCriteria));
        return mongoTemplate.count(query, StudentAnswer.class);
    }

    /**
     * 解析当前自然周的统计时间范围。
     *
     * @return 本周起止时间对象
     */
    private WeeklyRange resolveCurrentWeekRange() {
        LocalDate weekStartDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStartDateTime = weekStartDate.atStartOfDay();
        LocalDateTime weekEndDateTime = weekStartDate.plusWeeks(1).atStartOfDay();
        ZoneId zoneId = ZoneId.systemDefault();
        return new WeeklyRange(
                Date.from(weekStartDateTime.atZone(zoneId).toInstant()),
            Date.from(weekEndDateTime.atZone(zoneId).toInstant()),
            weekStartDateTime,
            weekEndDateTime
        );
    }

    /**
     * 将可能为空的 Long 值规范为非空 long。
     *
     * @param value 原始统计值
     * @return 非空 long 值
     */
    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    /**
     * 规范化平均评分的小数位数。
     *
     * @param averageRating 原始平均评分
     * @return 保留 1 位小数后的评分值
     */
    private BigDecimal normalizeAverageRating(BigDecimal averageRating) {
        BigDecimal safeValue = averageRating == null ? BigDecimal.ZERO : averageRating;
        return safeValue.setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 本周统计时间范围值对象。
     */
    private static final class WeeklyRange {

        private final Date weekStart;

        private final Date weekEnd;

        private final LocalDateTime weekStartDateTime;

        private final LocalDateTime weekEndDateTime;

        private WeeklyRange(Date weekStart, Date weekEnd, LocalDateTime weekStartDateTime, LocalDateTime weekEndDateTime) {
            this.weekStart = weekStart;
            this.weekEnd = weekEnd;
            this.weekStartDateTime = weekStartDateTime;
            this.weekEndDateTime = weekEndDateTime;
        }

        private Date getWeekStart() {
            return weekStart;
        }

        private Date getWeekEnd() {
            return weekEnd;
        }

        private LocalDateTime getWeekStartDateTime() {
            return weekStartDateTime;
        }

        private LocalDateTime getWeekEndDateTime() {
            return weekEndDateTime;
        }
    }
}