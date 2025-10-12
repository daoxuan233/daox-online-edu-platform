package com.daox.online.entity.views.responseVO.course;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 课程详细统计信息VO
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class CourseDetailedStatisticsVo {
    
    /**
     * 课程总数
     */
    private Integer totalCourses;
    
    /**
     * 已发布课程数
     */
    private Integer publishedCourses;
    
    /**
     * 草稿课程数
     */
    private Integer draftCourses;
    
    /**
     * 已归档课程数
     */
    private Integer archivedCourses;
    
    /**
     * 公开课程数
     */
    private Integer publicCourses;
    
    /**
     * 私有课程数
     */
    private Integer privateCourses;
    
    /**
     * 总学习人数
     */
    private Integer totalEnrollments;
    
    /**
     * 按分类统计课程数量
     */
    private List<CategoryStatistics> categoryStatistics;
    
    /**
     * 按状态统计课程数量
     */
    private List<StatusStatistics> statusStatistics;
    
    /**
     * 最受欢迎的课程（按学习人数排序，前10名）
     */
    private List<PopularCourse> popularCourses;
    
    /**
     * 分类统计内部类
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryStatistics {
        private String categoryId;
        private String categoryName;
        private Integer courseCount;
        private Integer totalEnrollments;
    }
    
    /**
     * 状态统计内部类
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatusStatistics {
        private String status;
        private String statusName;
        private Integer courseCount;
    }
    
    /**
     * 热门课程内部类
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PopularCourse {
        private String courseId;
        private String courseTitle;
        private String teacherName;
        private String categoryName;
        private Integer enrollmentCount;
        private String status;
    }
}