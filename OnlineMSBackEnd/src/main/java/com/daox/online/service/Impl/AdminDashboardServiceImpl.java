package com.daox.online.service.Impl;

import com.daox.online.entity.mysql.AuditLogs;
import com.daox.online.entity.mysql.Courses;
import com.daox.online.entity.mysql.Users;
import com.daox.online.entity.views.responseVO.SystemAnnouncementsVo;
import com.daox.online.entity.views.responseVO.admin.AdminDashboardOverviewVO;
import com.daox.online.entity.views.responseVO.course.CourseDetailedStatisticsVo;
import com.daox.online.mapper.SysUsersMapper;
import com.daox.online.service.AdminDashboardService;
import com.daox.online.service.AuditLogService;
import com.daox.online.service.CoursesService;
import com.daox.online.service.SystemAnnouncementsService;
import com.daox.online.service.UsersService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员首页概览服务实现。
 */
@Service
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final DateTimeFormatter TREND_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
    private static final Map<String, String> AUDIT_ICON_MAP = Map.ofEntries(
            Map.entry("COURSE_DELETE", "fas fa-book"),
            Map.entry("USER_PASSWORD_RESET", "fas fa-key"),
            Map.entry("QUESTION_VECTOR_SYNC_TRIGGER", "fas fa-arrows-rotate"),
            Map.entry("ADMIN_CAT_DEL_EM", "fas fa-bolt"),
            Map.entry("ADMIN_CAT_DEL_RG", "fas fa-calendar-days"),
            Map.entry("ADMIN_CAT_MIG", "fas fa-right-left"),
            Map.entry("ADMIN_CAT_DEL_EM_REQ", "fas fa-triangle-exclamation"),
            Map.entry("ADMIN_CAT_DEL_EM_DONE", "fas fa-trash-can"),
            Map.entry("ADMIN_CAT_DEL_RG_REQ", "fas fa-hourglass-half"),
            Map.entry("ADMIN_CAT_DEL_RG_WAIT", "fas fa-clock"),
            Map.entry("ADMIN_CAT_DEL_RG_DONE", "fas fa-check-double"),
            Map.entry("ADMIN_CAT_MIG_REQ", "fas fa-route"),
            Map.entry("ADMIN_CAT_MIG_DONE", "fas fa-shuffle")
    );
    private static final Map<String, String> AUDIT_TITLE_MAP = Map.ofEntries(
            Map.entry("COURSE_DELETE", "删除课程"),
            Map.entry("USER_PASSWORD_RESET", "密码重置"),
            Map.entry("QUESTION_VECTOR_SYNC_TRIGGER", "同步触发"),
            Map.entry("ADMIN_CAT_DEL_EM", "紧急删除分类"),
            Map.entry("ADMIN_CAT_DEL_RG", "常规删除分类"),
            Map.entry("ADMIN_CAT_MIG", "分类迁移"),
            Map.entry("ADMIN_CAT_DEL_EM_REQ", "紧急删除申请"),
            Map.entry("ADMIN_CAT_DEL_EM_DONE", "紧急删除完成"),
            Map.entry("ADMIN_CAT_DEL_RG_REQ", "常规删除申请"),
            Map.entry("ADMIN_CAT_DEL_RG_WAIT", "常规删除保留中"),
            Map.entry("ADMIN_CAT_DEL_RG_DONE", "常规删除完成"),
            Map.entry("ADMIN_CAT_MIG_REQ", "分类迁移申请"),
            Map.entry("ADMIN_CAT_MIG_DONE", "分类迁移完成")
    );

    @Resource
    private UsersService usersService;

    @Resource
    private CoursesService coursesService;

    @Resource
    private SystemAnnouncementsService systemAnnouncementsService;

    @Resource
    private AuditLogService auditLogService;

    @Resource
    private SysUsersMapper sysUsersMapper;

    @Override
    public AdminDashboardOverviewVO getDashboardOverview(int days) {
        int normalizedDays = normalizeTrendDays(days);
        List<Users> allUsers = sysUsersMapper.getUserListAll();
        List<Courses> allCourses = coursesService.getCourseListAll();
        CourseDetailedStatisticsVo courseStatistics = coursesService.getCourseDetailedStatistics();
        List<SystemAnnouncementsVo> notices = systemAnnouncementsService.getSystemAnnouncements(1, 6);
        List<AuditLogs> auditLogs = auditLogService.listAuditLogs(null, null, null, null, null, null);

        AdminDashboardOverviewVO overviewVO = new AdminDashboardOverviewVO();
        overviewVO.setOverview(buildOverview(allUsers, allCourses, courseStatistics, notices, auditLogs));
        overviewVO.setUserGrowthTrend(buildTrendPoints(allUsers, allCourses, normalizedDays));
        overviewVO.setSystemStatuses(buildSystemStatuses(allUsers, allCourses, courseStatistics, notices, auditLogs));
        overviewVO.setRecentActivities(buildRecentActivities(auditLogs));
        overviewVO.setPendingTasks(buildPendingTasks(allUsers, allCourses, auditLogs));
        overviewVO.setSystemNotifications(buildNotifications(notices));
        return overviewVO;
    }

    private AdminDashboardOverviewVO.Overview buildOverview(List<Users> allUsers,
                                                            List<Courses> allCourses,
                                                            CourseDetailedStatisticsVo courseStatistics,
                                                            List<SystemAnnouncementsVo> notices,
                                                            List<AuditLogs> auditLogs) {
        int totalUsers = usersService.getUserCountValid();
        int students = usersService.getStudentCountValid();
        int teachers = usersService.getTeacherCountValid();
        int fallbackTotalCourses = countValidCourses(allCourses);
        int fallbackPublishedCourses = countCoursesByStatus(allCourses, "published");
        int fallbackDraftCourses = countCoursesByStatus(allCourses, "draft");
        int totalCourses = courseStatistics == null || courseStatistics.getTotalCourses() == null || courseStatistics.getTotalCourses() == 0
                ? fallbackTotalCourses : courseStatistics.getTotalCourses();
        int publishedCourses = courseStatistics == null || courseStatistics.getPublishedCourses() == null
                ? fallbackPublishedCourses : courseStatistics.getPublishedCourses();
        int draftCourses = courseStatistics == null || courseStatistics.getDraftCourses() == null
                ? fallbackDraftCourses : courseStatistics.getDraftCourses();
        long disabledUsers = allUsers.stream().filter(user -> user.getIsDeleted() != null && user.getIsDeleted() == 1).count();
        long failedAuditCount = auditLogs.stream().filter(log -> "FAILED".equals(log.getStatus())).count();

        return new AdminDashboardOverviewVO.Overview()
                .setTotalUsers(totalUsers)
                .setStudents(students)
                .setTeachers(teachers)
                .setUserGrowth(calculateRatio(totalUsers - disabledUsers, totalUsers))
                .setTotalCourses(totalCourses)
                .setActiveCourses(publishedCourses)
                .setPendingCourses(draftCourses)
                .setCourseGrowth(calculateRatio(publishedCourses, totalCourses))
                .setStorageUsage(calculateRatio(failedAuditCount, auditLogs.size()))
                .setUptime(notices.isEmpty() ? "暂无公告" : notices.getFirst().getTitle());
    }

    private List<AdminDashboardOverviewVO.TrendPoint> buildTrendPoints(List<Users> allUsers,
                                                                       List<Courses> allCourses,
                                                                       int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1L);
        Map<LocalDate, Integer> userCreationMap = new LinkedHashMap<>();
        Map<LocalDate, Integer> courseCreationMap = new LinkedHashMap<>();
        for (int dayIndex = 0; dayIndex < days; dayIndex++) {
            LocalDate currentDate = startDate.plusDays(dayIndex);
            userCreationMap.put(currentDate, 0);
            courseCreationMap.put(currentDate, 0);
        }
        allUsers.forEach(user -> increaseDailyCount(user.getCreatedAt(), userCreationMap));
        allCourses.forEach(course -> increaseDailyCount(course.getCreatedAt(), courseCreationMap));

        int cumulativeUsers = 0;
        List<AdminDashboardOverviewVO.TrendPoint> trendPoints = new ArrayList<>();
        for (Map.Entry<LocalDate, Integer> entry : userCreationMap.entrySet()) {
            LocalDate currentDate = entry.getKey();
            cumulativeUsers += entry.getValue();
            trendPoints.add(new AdminDashboardOverviewVO.TrendPoint()
                    .setLabel(currentDate.format(TREND_LABEL_FORMATTER))
                    .setNewUsers(entry.getValue())
                    .setCumulativeUsers(cumulativeUsers)
                    .setNewCourses(courseCreationMap.getOrDefault(currentDate, 0)));
        }
        return trendPoints;
    }

    private List<AdminDashboardOverviewVO.SystemStatusItem> buildSystemStatuses(List<Users> allUsers,
                                                                                List<Courses> allCourses,
                                                                                CourseDetailedStatisticsVo courseStatistics,
                                                                                List<SystemAnnouncementsVo> notices,
                                                                                List<AuditLogs> auditLogs) {
        long disabledUsers = allUsers.stream().filter(user -> user.getIsDeleted() != null && user.getIsDeleted() == 1).count();
        long failedAuditCount = auditLogs.stream().filter(log -> "FAILED".equals(log.getStatus())).count();
        int activeNoticeCount = (int) notices.stream().filter(item -> item.getIsActive() != null && item.getIsActive() == 0).count();
        int draftCourses = courseStatistics == null || courseStatistics.getDraftCourses() == null
                ? countCoursesByStatus(allCourses, "draft")
                : courseStatistics.getDraftCourses();

        return List.of(
                new AdminDashboardOverviewVO.SystemStatusItem()
                        .setCode("user")
                        .setLabel("用户体系")
                        .setStatus(disabledUsers > 0 ? "warning" : "success")
                        .setValue(disabledUsers > 0 ? disabledUsers + " 个禁用账户" : "全部正常")
                        .setDescription("当前用户账号状态巡检结果"),
                new AdminDashboardOverviewVO.SystemStatusItem()
                        .setCode("course")
                        .setLabel("课程审核")
                        .setStatus(draftCourses > 0 ? "warning" : "success")
                        .setValue(draftCourses + " 门待处理")
                        .setDescription("待审核或待发布课程数量"),
                new AdminDashboardOverviewVO.SystemStatusItem()
                        .setCode("audit")
                        .setLabel("审计链路")
                        .setStatus(failedAuditCount > 0 ? "danger" : "success")
                        .setValue(failedAuditCount + " 条失败日志")
                        .setDescription("敏感操作审计的异常写入情况"),
                new AdminDashboardOverviewVO.SystemStatusItem()
                        .setCode("notice")
                        .setLabel("公告中心")
                        .setStatus(activeNoticeCount > 0 ? "success" : "warning")
                        .setValue(activeNoticeCount + " 条生效公告")
                        .setDescription("首页公告和运营通知发布状态")
        );
    }

    private List<AdminDashboardOverviewVO.ActivityItem> buildRecentActivities(List<AuditLogs> auditLogs) {
        return auditLogs.stream()
                .sorted(Comparator.comparing(AuditLogs::getCreatedAt, Comparator.nullsLast(Date::compareTo)).reversed())
                .limit(6)
                .map(log -> new AdminDashboardOverviewVO.ActivityItem()
                        .setId(log.getId())
                        .setIcon(resolveAuditIcon(log.getAction()))
                        .setTitle(buildAuditDisplayText(log.getAction(), log.getMessage()))
                        .setDescription((log.getOperatorName() == null || log.getOperatorName().isBlank() ? "系统任务" : log.getOperatorName())
                                + " · "
                                + (log.getResourceId() == null || log.getResourceId().isBlank() ? "未记录资源ID" : log.getResourceId()))
                        .setTime(formatDateTime(log.getCreatedAt()))
                        .setStatus(resolveAuditStatus(log.getStatus())))
                .toList();
    }

    private List<AdminDashboardOverviewVO.PendingTaskItem> buildPendingTasks(List<Users> allUsers,
                                                                             List<Courses> allCourses,
                                                                             List<AuditLogs> auditLogs) {
        List<AdminDashboardOverviewVO.PendingTaskItem> tasks = new ArrayList<>();

        allCourses.stream()
                .filter(course -> "draft".equals(course.getStatus()))
                .sorted(Comparator.comparing(Courses::getUpdatedAt, Comparator.nullsLast(Date::compareTo)).reversed())
                .limit(3)
                .forEach(course -> tasks.add(new AdminDashboardOverviewVO.PendingTaskItem()
                        .setId("course-" + course.getId())
                        .setTitle("课程待处理")
                        .setDescription(course.getTitle() + " 当前仍为草稿状态")
                        .setTime(formatDateTime(course.getUpdatedAt() == null ? course.getCreatedAt() : course.getUpdatedAt()))
                        .setPriority("high")
                        .setActionType("course")));

        auditLogs.stream()
                .filter(log -> "FAILED".equals(log.getStatus()))
                .sorted(Comparator.comparing(AuditLogs::getCreatedAt, Comparator.nullsLast(Date::compareTo)).reversed())
                .limit(2)
                .forEach(log -> tasks.add(new AdminDashboardOverviewVO.PendingTaskItem()
                        .setId("audit-" + log.getId())
                        .setTitle("审计异常待复核")
                        .setDescription(buildAuditDisplayText(log.getAction(), log.getMessage()))
                        .setTime(formatDateTime(log.getCreatedAt()))
                        .setPriority("medium")
                        .setActionType("audit")));

        allUsers.stream()
                .filter(user -> user.getIsDeleted() != null && user.getIsDeleted() == 1)
                .sorted(Comparator.comparing(Users::getUpdatedAt, Comparator.nullsLast(Date::compareTo)).reversed())
                .limit(2)
                .forEach(user -> tasks.add(new AdminDashboardOverviewVO.PendingTaskItem()
                        .setId("user-" + user.getId())
                        .setTitle("禁用用户复核")
                        .setDescription(user.getNickname() + " 当前处于禁用状态")
                        .setTime(formatDateTime(user.getUpdatedAt()))
                        .setPriority("low")
                        .setActionType("user")));

        return tasks.stream().limit(6).toList();
    }

    private List<AdminDashboardOverviewVO.NotificationItem> buildNotifications(List<SystemAnnouncementsVo> notices) {
        return notices.stream()
                .map(item -> new AdminDashboardOverviewVO.NotificationItem()
                        .setId(item.getId())
                        .setIcon(item.getIsActive() != null && item.getIsActive() == 0 ? "fas fa-bullhorn" : "fas fa-info-circle")
                        .setTitle(item.getTitle())
                        .setMessage(item.getContent())
                        .setTime(formatDateTime(item.getCreatedAt()))
                        .setRead(item.getIsActive() != null && item.getIsActive() != 0))
                .toList();
    }

    private void increaseDailyCount(Date createdAt, Map<LocalDate, Integer> targetMap) {
        if (createdAt == null) {
            return;
        }
        LocalDate date = createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (targetMap.containsKey(date)) {
            targetMap.put(date, targetMap.get(date) + 1);
        }
    }

    private int normalizeTrendDays(int days) {
        if (days == 7 || days == 30 || days == 90) {
            return days;
        }
        return 30;
    }

    private double calculateRatio(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0D;
        }
        return Math.round((numerator * 1000D / denominator)) / 10D;
    }

    private int countValidCourses(List<Courses> allCourses) {
        return (int) allCourses.stream()
                .filter(course -> course.getIsDeleted() == null || course.getIsDeleted() == 0)
                .count();
    }

    private int countCoursesByStatus(List<Courses> allCourses, String status) {
        return (int) allCourses.stream()
                .filter(course -> course.getIsDeleted() == null || course.getIsDeleted() == 0)
                .filter(course -> status.equals(course.getStatus()))
                .count();
    }

    private String resolveAuditIcon(String action) {
        return AUDIT_ICON_MAP.getOrDefault(action, "fas fa-shield-halved");
    }

    private String resolveAuditTitle(String action) {
        return AUDIT_TITLE_MAP.getOrDefault(action, action == null || action.isBlank() ? "未知动作" : action);
    }

    private String buildAuditDisplayText(String action, String message) {
        String auditTitle = resolveAuditTitle(action);
        if (message == null || message.isBlank()) {
            return auditTitle;
        }
        String normalizedMessage = message.trim();
        if (normalizedMessage.startsWith(auditTitle)) {
            return normalizedMessage;
        }
        return auditTitle + " · " + normalizedMessage;
    }

    private String resolveAuditStatus(String status) {
        if ("FAILED".equals(status)) {
            return "error";
        }
        if ("STARTED".equals(status)) {
            return "warning";
        }
        return "success";
    }

    private String formatDateTime(Date date) {
        if (date == null) {
            return "未知时间";
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
