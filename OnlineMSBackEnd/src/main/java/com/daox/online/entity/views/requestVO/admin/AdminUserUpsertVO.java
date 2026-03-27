package com.daox.online.entity.views.requestVO.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 管理员用户维护请求体。
 * 用于管理员在后台创建或更新用户基础信息。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AdminUserUpsertVO {

    /**
     * 学号/工号。
     */
    private String identifier;

    /**
     * 昵称/用户名。
     */
    private String nickname;

    /**
     * 邮箱。
     */
    private String email;

    /**
     * 手机号。
     */
    private String phone;

    /**
     * 用户角色：admin / teacher / student。
     */
    private String role;

    /**
     * 初始密码，仅创建时必填。
     */
    private String password;

    /**
     * 是否启用，true 为正常，false 为禁用。
     */
    private Boolean enabled;
}
