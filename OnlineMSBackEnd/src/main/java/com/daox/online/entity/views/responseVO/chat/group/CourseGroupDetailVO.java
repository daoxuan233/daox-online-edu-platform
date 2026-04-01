package com.daox.online.entity.views.responseVO.chat.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程群聊详情视图对象。
 * <p>
 * 用于在进入群聊页时一次性返回课程基础信息、教师信息以及当前用户在群内的权限状态。
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CourseGroupDetailVO implements Serializable {

    /**
     * 课程ID，同时也是群聊会话ID。
     */
    private String courseId;

    /**
     * 群聊会话ID。
     */
    private String conversationId;

    /**
     * 课程标题。
     */
    private String courseTitle;

    /**
     * 课程简介。
     */
    private String courseDescription;

    /**
     * 课程封面。
     */
    private String coverImageUrl;

    /**
     * 课程状态。
     */
    private String courseStatus;

    /**
     * 任课教师ID。
     */
    private String teacherId;

    /**
     * 任课教师昵称。
     */
    private String teacherName;

    /**
     * 任课教师头像。
     */
    private String teacherAvatarUrl;

    /**
     * 群成员总数（教师 + 学生）。
     */
    private Long memberCount;

    /**
     * 当前用户角色。
     */
    private String currentUserRole;

    /**
     * 当前用户是否被禁言。
     */
    private Boolean currentUserMuted;

    /**
     * 当前课程群是否开启全员禁言。
     */
    private Boolean groupAllMuted;

    /**
     * 当前课程群全员禁言预计结束时间。
     */
    private LocalDateTime groupAllMutedExpireAt;

    /**
     * 当前用户是否为课程群主教师。
     */
    private Boolean currentUserTeacherOwner;
}