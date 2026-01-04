package com.daox.ai.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import com.daox.ai.config.memory.RedisChatMemory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@Configuration
public class ShortTermMemoryConfig {

    /**
     * 配置短时记忆 (连接 6380)
     * Spring Boot 会自动注入基于 application.yml 配置的 RedisConnectionFactory
     */
    // 注入 Spring Boot 自动配置好的 StringRedisTemplate (连接 6380)
    @Bean
    public ChatMemory redisChatMemory(StringRedisTemplate redisTemplate) {
        // 设置短时记忆过期时间为 4 小时
        return new RedisChatMemory(redisTemplate, Duration.ofHours(4));
    }

}
