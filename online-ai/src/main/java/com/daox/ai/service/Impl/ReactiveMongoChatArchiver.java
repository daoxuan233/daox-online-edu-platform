package com.daox.ai.service.Impl;

import com.daox.ai.entity.mongodb.ChatMessage;
import com.daox.ai.repository.ChatMessageRepository;
import com.daox.ai.utils.Const;
import jakarta.annotation.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReactiveMongoChatArchiver {

    @Resource
    private ChatMessageRepository repository;

    /**
     * 响应式冷备方法
     * @return Mono<Void> 表示任务完成，不返回具体数据
     */
    public Mono<Void> archive(String userId, String question, String aiResponse, String conversationId) {
        // 1. 确保有有效的 Conversation ID (保持你原有的逻辑)
        // 注意：在混合架构中，通常建议在 Service 入口处就确定好 ID，这里作为兜底
        String finalConvId = (conversationId == null || conversationId.isBlank())
                ? Const.AI_ASSISTANT_CONVERSATION + userId + "_" + UUID.randomUUID()
                : conversationId;

        LocalDateTime now = LocalDateTime.now();

        // 2. 创建用户消息
        ChatMessage userMessage = new ChatMessage()
                .setConversationId(finalConvId)
                .setSenderId(userId)
                .setReceiverId(Const.AI_ASSISTANT)
                .setMessageType(ChatMessage.MessageType.AI)
                .setContentType(ChatMessage.ContentType.TEXT)
                .setContent(question)
                .setTimestamp(now)
                .setStatus(ChatMessage.MessageStatus.READ);

        // 3. 创建 AI 消息
        ChatMessage aiMessage = new ChatMessage()
                .setConversationId(finalConvId)
                .setSenderId(Const.AI_ASSISTANT)
                .setReceiverId(userId)
                .setMessageType(ChatMessage.MessageType.AI)
                .setContentType(ChatMessage.ContentType.RICH_TEXT) // 假设 AI 输出富文本
                .setContent(aiResponse)
                .setTimestamp(now.plusNanos(1_000_000)) // 保持原有的微小时差逻辑
                .setStatus(ChatMessage.MessageStatus.DELIVERED);

        // 4. 执行非阻塞保存
        return repository.saveAll(List.of(userMessage, aiMessage))
                .then(); // 忽略保存结果，转换为 Mono<Void>
    }
}
