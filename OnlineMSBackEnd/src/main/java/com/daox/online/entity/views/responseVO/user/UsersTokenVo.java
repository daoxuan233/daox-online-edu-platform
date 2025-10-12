package com.daox.online.entity.views.responseVO.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 返回前端的用户信息
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UsersTokenVo {
    /**
     * 用户名
     */
    String username;
    /**
     * 角色
     */
    String role;
    /**
     * token
     */
    String token;
    /**
     * 标识符
     */
    String identifier;
    /**
     * 头像
     */
    String avatarUrl;
    /**
     * 过期时间
     */
    Date expire;
}
