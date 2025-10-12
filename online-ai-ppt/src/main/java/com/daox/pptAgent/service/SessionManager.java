package com.daox.pptAgent.service;

import com.daox.pptAgent.model.SessionState;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 会话管理器
 * 职责：封装所有与Redis的交互，提供对SessionState的存、取、删操作。
 */
@Service
public class SessionManager {

    // Spring Boot自动配置的RedisTemplate，用于操作对象
    private final RedisTemplate<String, Object> redisTemplate;

    // 会话在Redis中的Key前缀，避免与其他业务冲突
    private static final String SESSION_KEY_PREFIX = "ppt_agent_session:";
    // 会话过期时间（例如30分钟）
    private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(30);

    public SessionManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 根据用户ID获取会话状态。如果不存在，则创建一个新的。
     * @param userId 用户ID
     * @return 用户的会话状态
     */
    public SessionState getSession(String userId) {
        String key = SESSION_KEY_PREFIX + userId;
        SessionState state = (SessionState) redisTemplate.opsForValue().get(key);
        if (state == null) {
            state = new SessionState();
            saveSession(userId, state); // 创建后立即保存一次
        }
        return state;
    }

    /**
     * 保存用户的会话状态到Redis，并设置过期时间。
     * @param userId 用户ID
     * @param state 要保存的会话状态
     */
    public void saveSession(String userId, SessionState state) {
        String key = SESSION_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, state, SESSION_TIMEOUT);
    }

    /**
     * 从Redis中删除用户的会话状态。
     * @param userId 用户ID
     */
    public void deleteSession(String userId) {
        String key = SESSION_KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }
}
