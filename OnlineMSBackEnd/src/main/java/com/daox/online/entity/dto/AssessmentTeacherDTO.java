package com.daox.online.entity.dto;

import com.daox.online.entity.mongodb.dto.CourseBaseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 教师端
 * 查看所有测评的 数据
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AssessmentTeacherDTO {
    /**
     * 测评ID
     */
    private String id;
    /**
     * 课程基础信息，关联MySQL
     * @see CourseBaseDTO
     * 便于在MongoDB中直接按课程检索试卷，提高查询效率。
     */
    private CourseBaseDTO course;
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
