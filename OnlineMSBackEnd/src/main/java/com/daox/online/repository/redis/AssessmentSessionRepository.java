package com.daox.online.repository.redis;

import com.daox.online.entity.redis.AssessmentSession;
import com.daox.online.uilts.RedisKeyHelper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class AssessmentSessionRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public AssessmentSessionRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    /**
     * 保存或更新一个测评会话，并设置过期时间
     * @param session 会话对象
     * @param durationMinutes 过期时间（分钟）
     */
    public void save(AssessmentSession session, Integer durationMinutes) {
        String key = RedisKeyHelper.getAssessmentSessionKey(session.getAssessmentId(), session.getUserId());
        // 增加5分钟缓冲时间，防止因网络延迟等问题导致提前过期
        long timeout = durationMinutes + 5;
        redisTemplate.opsForValue().set(key, session, timeout, TimeUnit.MINUTES);
    }

    /**
     * 保存或更新一个测评会话（不改变原有的过期时间）
     * @param session 会话对象
     */
    public void save(AssessmentSession session) {
        String key = RedisKeyHelper.getAssessmentSessionKey(session.getAssessmentId(), session.getUserId());
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS); // 获取剩余过期时间
        redisTemplate.opsForValue().set(key, session);
        if (expire != null && expire > 0) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS); // 重新设置过期时间
        }
    }

    /**
     * 根据测评ID和用户ID查找会话
     * @param assessmentId 测评ID
     * @param userId 用户ID
     * @return 会话 Optional
     */
    public Optional<AssessmentSession> findById(String assessmentId, String userId) {
        String key = RedisKeyHelper.getAssessmentSessionKey(assessmentId, userId);
        // AssessmentSession session = (AssessmentSession) redisTemplate.opsForValue().get(key);
        Object sessionObj = redisTemplate.opsForValue().get(key);
        System.out.println("Deserialized object type from Redis: " + (sessionObj != null ? sessionObj.getClass().getName() : "null"));
        AssessmentSession session = (AssessmentSession) sessionObj;
        return Optional.ofNullable(session);
    }

    /**
     * 检查会话是否存在
     * @param assessmentId 测评ID
     * @param userId 用户ID
     * @return boolean
     */
    public boolean existsById(String assessmentId, String userId) {
        String key = RedisKeyHelper.getAssessmentSessionKey(assessmentId, userId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 将会话标记为已提交，并设置一个较短的过期时间
     * @param session 会话对象
     * @param finalExpireMinutes 最终的过期时间（分钟）
     */
    public void markAsSubmitted(AssessmentSession session, int finalExpireMinutes) {
        String key = RedisKeyHelper.getAssessmentSessionKey(session.getAssessmentId(), session.getUserId());
        session.setStatus(AssessmentSession.Status.SUBMITTED);
        redisTemplate.opsForValue().set(key, session, finalExpireMinutes, TimeUnit.MINUTES);
    }
}
