package com.daox.online.entity.views.responseVO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 学生考核信息
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class StudentAssessmentVO {
    /**
     * 考核ID
     */
    private String assessmentId;
    /**
     * 考核标题
     */
    private String title;
    /**
     * 考核类型(ClassroomExam [课堂作业]、ChapterExam [章节作业]、MidtermExam [期中考试]、FinalExam [期末考试]、homework [作业])
     */
    private String assessmentType;
    /**
     * 考核开始时间
     */
    private LocalDateTime startTime;
    /**
     * 考核结束时间
     */
    private LocalDateTime endTime;
    /**
     * 考核时长（分钟）
     */
    private Integer durationMinutes;
    /**
     * 考核所属课程ID
     */
    private String courseId;
    /**
     * 课程标题
     */
    private String courseTitle;
    /**
     * 课程封面
     */
    private String courseCover;
    /**
     * 考核状态
     */
    private String status; // not_started, in_progress, submitted, graded
    /**
     * 考核提交时间
     */
    private Date submitTime;
    /**
     * 考核得分
     */
    private BigDecimal score;
    /**
     * 考核满分（试卷总分）。
     * <p>
     * 字段说明：
     * 1) 优先使用试卷中已持久化的 totalScore；
     * 2) 若试卷 totalScore 缺失，则由后端按题目分值动态汇总；
     * 3) 用于前端准确展示“得分/满分”以及百分比，禁止使用硬编码满分。
     * </p>
     */
    private BigDecimal fullScore;
    /**
     * 是否可开始
     */
    private Boolean canStart;
    /**
     * 发布状态（0-未发布，1-已发布，2-已归档）
     * <p>
     * 说明：
     * 1) 该字段用于前后端在“可查看”与“可操作”之间做权限区分；
     * 2) 学生端可见已归档卡片，但不可发起作答操作；
     * 3) 该字段仅用于业务判断，不影响历史成绩展示。
     * </p>
     */
    private Integer isPublished;
}
