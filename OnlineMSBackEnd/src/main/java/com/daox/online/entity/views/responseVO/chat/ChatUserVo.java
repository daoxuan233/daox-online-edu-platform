package com.daox.online.entity.views.responseVO.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 聊天用户Vo
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ChatUserVo implements Serializable {
    /**
     * 好友ID
     */
    private String friendId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 头像
     */
    private String avatarUrl;
    /**
     * 角色
     */
    private String role;
    /**
     * 备注
     */
    private String remark;
}
