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
     * 是否可开始
     */
    private Boolean canStart;
}
