package com.daox.online.entity.views.responseVO.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class NotificationPageVO implements Serializable {

    private Integer pageNum;
    private Integer pageSize;
    private Long total;
    private Long unreadCount;
    private List<NotificationItemVO> items;
}
