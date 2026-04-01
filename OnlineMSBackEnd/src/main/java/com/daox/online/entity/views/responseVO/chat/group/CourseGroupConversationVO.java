package com.daox.online.entity.views.responseVO.chat.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程群聊会话摘要视图对象。
 * <p>
 * 该对象用于课程群聊列表展示，聚合课程信息、任课教师信息、成员数量和最后一条群消息摘要。
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CourseGroupConversationVO implements Serializable {

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
     * 课程封面。
     */
    private String coverImageUrl;

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
     * 当前用户在群内的角色。
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
     * 当前用户在该课程群中的未读消息数。
     */
    private Integer unreadCount;

    /**
     * 最后一条消息内容。
     */
    private String lastMessageContent;

    /**
     * 最后一条消息发送者ID。
     */
    private String lastMessageSenderId;

    /**
     * 最后一条消息标签，用于区分 teacher / student。
     */
    private String lastMessageTag;

    /**
     * 最后一条消息时间。
     */
    private LocalDateTime lastMessageTime;
}