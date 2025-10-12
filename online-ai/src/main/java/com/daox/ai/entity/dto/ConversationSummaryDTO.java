package com.daox.ai.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话摘要DTO
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryDTO implements Serializable {
    /**
     * 会话id
     */
    private String conversationId;
    private String title;          // 对话标题
    private LocalDateTime lastMessageTime; // 最后一条消息的时间
}