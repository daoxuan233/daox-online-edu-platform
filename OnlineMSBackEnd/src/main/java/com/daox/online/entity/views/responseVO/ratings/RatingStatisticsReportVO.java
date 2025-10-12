package com.daox.online.entity.views.responseVO.ratings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 评分统计报告VO
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class RatingStatisticsReportVO {
    /**
     * 系统总览
     */
    private SystemOverview systemOverview;
    /**
     * 按月趋势
     */
    private List<MonthlyTrend> monthlyTrends;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SystemOverview {
        private Integer totalCourseRatings;
        private Integer totalTeacherRatings;
        private Integer totalUsers;
        private Integer activeCourses;
        private Integer activeTeachers;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthlyTrend {
        private String month;
        private Integer courseRatings;
        private Integer teacherRatings;
        private Integer newUsers;
    }
}