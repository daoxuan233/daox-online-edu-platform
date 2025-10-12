package com.daox.online.entity.views.responseVO.chat;

import com.daox.online.entity.mongodb.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天会话列表的视图对象
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ConversationVO implements Serializable {
    /**
     * 会话id
     */
    private String conversationId;
    /**
     * 最后一条消息内容
     */
    private String lastMessageContent;
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
    /**
     * 消息状态
     */
    private ChatMessage.MessageStatus status;
    /**
     * 对方用户信息
     */
    private ChatUserVo friendInfo;
    /**
     * 未读消息数 (可选)
     */
    private int unreadCount;
}
