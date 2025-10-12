package com.daox.online.entity.mysql;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统公告表<br />
 * 用于管理员发布面向全站用户的系统级公告<br />
 * TableName:  system_announcements
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SystemAnnouncements implements Serializable {

    /**
     * 公告ID
     */
    private String id;
    /**
     * 公告标题
     */
    private String title;
    /**
     * 公告内容
     */
    private String content;
    /**
     * 发布者ID
     */
    private String creatorId;
    /**
     * 是否生效 0-是，1-否
     */
    private Integer isActive;
    /**
     * 发布时间
     */
    private Date createdAt;
    /**
     * 失效时间
     */
    private Date expiredAt;
}
