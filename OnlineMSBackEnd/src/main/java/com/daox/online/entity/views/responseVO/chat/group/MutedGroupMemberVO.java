package com.daox.online.entity.views.responseVO.chat.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群聊禁言成员视图对象。
 * <p>
 * 该对象用于教师查看当前课程群聊中的禁言名单及其剩余禁言时间。
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MutedGroupMemberVO implements Serializable {

    /**
     * 被禁言用户ID。
     */
    private String userId;

    /**
     * 被禁言用户昵称。
     */
    private String userName;

    /**
     * 被禁言用户头像。
     */
    private String avatarUrl;

    /**
     * 被禁言用户角色。
     */
    private String role;

    /**
     * 剩余禁言秒数。
     */
    private Long remainingSeconds;

    /**
     * 预计自动解禁时间。
     */
    private LocalDateTime expireAt;
}