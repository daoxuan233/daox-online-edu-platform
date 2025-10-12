package com.daox.online.entity.mysql;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表<br />
 * 存储所有系统用户（学生、教师、管理员）的基础信息<br />
 * TableName:  users
 */
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Users implements Serializable {

    /**
     * 用户ID
     */
    private String id;
    /**
     * 学号/工号
     */
    private String identifier;
    /**
     * 密码
     */
    private String password;
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
     * 更新时间
     */
    private Date updatedAt;
    /**
     * 逻辑删除标志 [1-"删除"，0-"正常"]
     */
    private Integer isDeleted;

}
