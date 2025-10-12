package com.daox.online.entity.views.responseVO.chat;

import com.daox.online.entity.mongodb.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用于接收MongoDB聚合查询的每个会话的最后一条消息
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class LastMessageInfo {
    /**
     * 消息id
     */
    private String id;
    /**
     * 会话ID
     */
    private String conversationId;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
    /**
     * 消息状态
     */
    private ChatMessage.MessageStatus status;
    /**
     * 发送者id
     */
    private String senderId;
    /**
     * 接收者id
     */
    private String receiverId;
}
