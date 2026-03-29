package com.daox.online.entity.mysql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 统一站内通知表。
 * <p>
 * 用于承载系统公告、测评发布、阅卷完成、课程变动等标准化通知。
 * 每条记录对应一个接收用户，便于按用户维度统计未读数和已读状态。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UserNotifications implements Serializable {

    private String id;
    private String userId;
    private String notificationType;
    private String sourceType;
    private String sourceId;
    private String title;
    private String content;
    private String level;
    private String actorId;
    private String courseId;
    private String relatedId;
    private Integer isRead;
    private Integer isDeleted;
    private Date createdAt;
    private Date readAt;
    private Date expiresAt;
}
