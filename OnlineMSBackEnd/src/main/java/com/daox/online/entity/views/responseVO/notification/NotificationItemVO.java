package com.daox.online.entity.views.responseVO.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class NotificationItemVO implements Serializable {

    private String id;
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
    private Date createdAt;
    private Date readAt;
    private Date expiresAt;
}
