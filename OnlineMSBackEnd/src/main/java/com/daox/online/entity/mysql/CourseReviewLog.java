package com.daox.online.entity.mysql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程审核流转记录表。
 * <p>
 * 该实体对应只增不改的课程审核历史，每次状态流转都应插入一条新记录，
 * 以便后续审计、教师回溯和管理员复核。
 * </p>
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CourseReviewLog implements Serializable {
    /**
     * 记录ID
     */
    private String id;

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 变更前状态
     */
    private String fromStatus;

    /**
     * 变更后状态
     */
    private String toStatus;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人角色 teacher/admin
     */
    private String operatorRole;

    /**
     * 审核意见（pending→draft 时必填）
     */
    private String comment;

    /**
     * 操作时间
     */
    private Date createdAt;
}
