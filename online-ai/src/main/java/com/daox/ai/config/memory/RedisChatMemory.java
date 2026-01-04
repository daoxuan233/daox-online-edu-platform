package com.daox.ai.config.memory;

// ================= 关键导入开始 =================
// 必须使用 spring-ai 包下的 Message，绝对不能用 spring-messaging 包
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.memory.ChatMemory;
// ================= 关键导入结束 =================

import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实现 Redis 作为短时记忆存储
 * 每个对话的记忆都存储在一个独立的 List 中，键为 "chat:memory:{conversationId}"
 */
@Slf4j
public class RedisChatMemory implements ChatMemory {

    private final StringRedisTemplate redisTemplate;
    private final String conversationPrefix = "chat:memory:";
    private final Duration timeToLive;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedisChatMemory(StringRedisTemplate redisTemplate, Duration timeToLive) {
        this.redisTemplate = redisTemplate;
        this.timeToLive = timeToLive;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        String key = conversationPrefix + conversationId;
        for (Message message : messages) {
            try {
                String json = objectMapper.writeValueAsString(new MessageDto(message));
                redisTemplate.opsForList().rightPush(key, json);
            } catch (Exception e) {
                log.error("[RedisChatMemory] 错误将消息序列化为 JSON: {}", e.getMessage(), e);
            }
        }
        redisTemplate.expire(key, timeToLive);
    }

    // ==========================================
    // 修复点 1：补全 get(String conversationId) 方法
    // ==========================================
    public List<Message> get(String conversationId, int lastN) {
        String key = conversationPrefix + conversationId;

        // 1. 获取 Redis List 中的所有数据
        // 这里的 0, -1 代表获取全部。
        // 优化建议：如果 lastN 很小，可以使用 range(key, -lastN, -1) 来减少网络传输
        List<String> rawJsonList = redisTemplate.opsForList().range(key, 0, -1);

        if (rawJsonList == null) return new ArrayList<>();

        List<Message> allMessages = rawJsonList.stream()
                .map(this::deserialize)
                .collect(Collectors.toList());

        // 2. 根据 lastN 截取
        int total = allMessages.size();
        if (lastN > 0 && total > lastN) {
            return allMessages.subList(total - lastN, total);
        }
        return allMessages;
    }

    // 必须实现此方法，默认获取最近 1000 条，或者全部
    @Override
    public List<Message> get(String conversationId) {
        return get(conversationId, 1000);
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(conversationPrefix + conversationId);
    }

    // ==========================================
    // 修复点 2：反序列化逻辑
    // ==========================================
    private Message deserialize(String json) {
        try {
            MessageDto dto = objectMapper.readValue(json, MessageDto.class);
            return switch (dto.role) {
                case "user" -> new UserMessage(dto.content);
                case "assistant" -> new AssistantMessage(dto.content);
                case "system" -> new SystemMessage(dto.content);
                default -> new UserMessage(dto.content);
            };
        } catch (Exception e) {
            // 容错处理：避免一条脏数据导致整个对话崩溃
            return new UserMessage("Error loading history");
        }
    }

    // DTO 用于 JSON 序列化
    static class MessageDto {
        public String role;
        public String content;

        public MessageDto() {}
        public MessageDto(Message m) {
            this.role = m.getMessageType().getValue();
            this.content = m.getText();
        }
    }
}