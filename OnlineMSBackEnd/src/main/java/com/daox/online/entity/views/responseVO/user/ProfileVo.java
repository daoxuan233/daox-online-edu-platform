package com.daox.online.entity.views.responseVO.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 个人中心视图对象
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ProfileVo {
    /**
     * 用户ID
     */
    private String id;
    /**
     * 学号/工号
     */
    private String identifier;
    /**
     * 昵称/用户名
     */
    private String nickname;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 'student' 'teacher' 'admin'
     */
    private String role;
    /**
     * 头像URL
     */
    private String avatarUrl;
    /**
     * 创建时间
     */
    private Date createdAt;
    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别
     * 男 [man]、女 [female]、其他 [other]
     */
    private String gender;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 简介 / 签名
     */
    private String biography;
}
