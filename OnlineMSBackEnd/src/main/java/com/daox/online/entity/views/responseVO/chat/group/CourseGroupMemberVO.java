package com.daox.online.entity.views.responseVO.chat.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 课程群聊成员视图对象。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CourseGroupMemberVO implements Serializable {

    /**
     * 用户ID。
     */
    private String userId;

    /**
     * 用户昵称。
     */
    private String userName;

    /**
     * 用户头像。
     */
    private String avatarUrl;

    /**
     * 用户角色。
     */
    private String role;

    /**
     * 是否为群主教师。
     */
    private Boolean teacherOwner;

    /**
     * 是否处于禁言状态。
     */
    private Boolean muted;
}