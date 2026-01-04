package com.daox.pptAgent.service;

import com.daox.pptAgent.model.SessionState;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 会话管理器
 * 职责：封装所有与Redis的交互，提供对SessionState的存、取、删操作。
 * 仅使用单个Redis Key："session"，通过Hash结构管理不同用户的会话。
 */
@Service
public class SessionManager {

    // Spring Boot自动配置的RedisTemplate，用于操作对象
    private final RedisTemplate<String, Object> redisTemplate;

    // 单一会话Key
    private static final String SESSION_KEY = "session";
    // 会话过期时间（例如30分钟）——对整个"session"键设置TTL
    private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(30);

    public SessionManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 根据用户ID获取会话状态。如果不存在，则创建一个新的并写入Hash。
     * @param userId 用户ID
     * @return 用户的会话状态
     */
    public SessionState getSession(String userId) {
        SessionState state = (SessionState) redisTemplate.opsForHash().get(SESSION_KEY, userId);
        if (state == null) {
            state = new SessionState();
            saveSession(userId, state); // 创建后立即保存一次
        }
        return state;
    }

    /**
     * 保存用户的会话状态到Redis Hash，并确保整体Key的过期时间。
     * @param userId 用户ID
     * @param state 要保存的会话状态
     */
    public void saveSession(String userId, SessionState state) {
        redisTemplate.opsForHash().put(SESSION_KEY, userId, state);

        // 如果没有设置过期时间，则设置一次整体Key的TTL
        Long ttl = redisTemplate.getExpire(SESSION_KEY);
        if (ttl == null || ttl == -1L) {
            redisTemplate.expire(SESSION_KEY, SESSION_TIMEOUT);
        }
    }

    /**
     * 从Redis中删除用户的会话状态（Hash字段）。
     * @param userId 用户ID
     */
    public void deleteSession(String userId) {
        redisTemplate.opsForHash().delete(SESSION_KEY, userId);
    }
}
