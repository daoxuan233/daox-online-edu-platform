package com.daox.online.entity.mysql;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 测评活动表<br />
 * 由教师发起的测评活动（如期中考试、课后作业、平时检查等）。<br />
 * 规定了测评的名称、时间范围和时长等基本信息<br />
 * TableName: assessments
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Assessments implements Serializable {

    /**
     * 测评ID
     */
    private String id;
    /**
     * 课程ID
     */
    private String courseId;
    /**
     * 创建者ID
     */
    private String creatorId;
    /**
     * 测评类型(ClassroomExam [课堂作业]、ChapterExam [章节作业]、MidtermExam [期中考试]、FinalExam [期末考试]、homework [作业])
     */
    private String assessmentType;
    /**
     * 测评标题
     */
    private String title;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 答题时长(分)
     */
    private Integer durationMinutes;
    /**
     * 是否发布 0-未发布，1-发布，2-过期[逻辑删除]
     */
    private Integer isPublished;


}
