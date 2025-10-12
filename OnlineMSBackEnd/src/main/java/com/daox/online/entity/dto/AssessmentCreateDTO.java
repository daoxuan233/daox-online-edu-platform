package com.daox.online.entity.dto;

import com.daox.online.uilts.constant.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 测评基础信息创建DTO (Assessment Creation DTO)
 * 仅包含创建测评活动所需的元数据。
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentCreateDTO implements Serializable {
    /**
     * 课程ID (必填)
     * 指明此测评属于哪一门课程。
     */
    private String courseId;

    /**
     * 创建者ID (必填)
     * 指明此测评创建者。
     */
    private String creatorId;

    /**
     * 测评类型 (必填)
     * ClassroomExam、ChapterExam、MidtermExam、FinalExam、homework
     */
    private String assessmentType;

    /**
     * 测评标题 (必填)
     * 例如：“第一章 数据库基础 课后作业”。
     */
    private String title;

    /**
     * 测评开始时间 (必填)
     */
    private LocalDateTime startTime;

    /**
     * 测评结束时间 (必填)
     */
    private LocalDateTime endTime;

    /**
     * 答题时长(分钟) (必填)
     */
    private Integer durationMinutes;

    /**
     * 是否发布 (必填)
     * 0-暂存为草稿，1-正式发布。, 2删除
     */
    private Integer isPublished;
}
