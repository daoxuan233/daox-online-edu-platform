package com.daox.online.entity.views.responseVO.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 管理员首页概览响应体。
 * 聚合首页需要的统计、趋势、系统状态、最近活动、待处理事项和通知内容。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AdminDashboardOverviewVO {

    private Overview overview;

    private List<TrendPoint> userGrowthTrend;

    private List<SystemStatusItem> systemStatuses;

    private List<ActivityItem> recentActivities;

    private List<PendingTaskItem> pendingTasks;

    private List<NotificationItem> systemNotifications;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class Overview {
        private Integer totalUsers;
        private Integer students;
        private Integer teachers;
        private Double userGrowth;
        private Integer totalCourses;
        private Integer activeCourses;
        private Integer pendingCourses;
        private Double courseGrowth;
        private Double storageUsage;
        private String uptime;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class TrendPoint {
        private String label;
        private Integer newUsers;
        private Integer cumulativeUsers;
        private Integer newCourses;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class SystemStatusItem {
        private String code;
        private String label;
        private String status;
        private String value;
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class ActivityItem {
        private String id;
        private String icon;
        private String title;
        private String description;
        private String time;
        private String status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class PendingTaskItem {
        private String id;
        private String title;
        private String description;
        private String time;
        private String priority;
        private String actionType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class NotificationItem {
        private String id;
        private String icon;
        private String title;
        private String message;
        private String time;
        private Boolean read;
    }
}
