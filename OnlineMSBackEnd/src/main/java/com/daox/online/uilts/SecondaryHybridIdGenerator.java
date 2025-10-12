package com.daox.online.uilts;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 混合 ID 生成器 - 结合雪花算法与 UUID
 */
@Component
public class SecondaryHybridIdGenerator {
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;
    private final AtomicLong clockBackwardCount = new AtomicLong(0);
    @Value("${app.id-generator.enable-uuid-fallback:true}") // 修改为@Value并添加默认值
    private boolean enableUUIDFallback;
    @Value("${app.id-generator.append-uuid:false}") // 修改为@Value并添加默认值
    private boolean appendUUID;

    // 默认配置：时钟回拨时使用 UUID 替代，不追加 UUID 到雪花 ID
    public SecondaryHybridIdGenerator() {
        this(new SnowflakeIdGenerator(), true, false);
    }

    // 自定义配置
    public SecondaryHybridIdGenerator(SnowflakeIdGenerator snowflakeIdGenerator,
                                      boolean enableUUIDFallback,
                                      boolean appendUUID) {
        this.snowflakeIdGenerator = snowflakeIdGenerator;
        this.enableUUIDFallback = enableUUIDFallback;
        this.appendUUID = appendUUID;
    }

    /**
     * 生成混合 ID
     * @return 字符串形式的唯一 ID
     */
    public String generateId() {
        try {
            long snowflakeId = snowflakeIdGenerator.nextId();

            if (appendUUID) {
                // 方案1：将雪花ID与UUID拼接（更高唯一性，但ID更长）
                return String.format("%d-%s", snowflakeId, UUID.randomUUID());
            } else {
                // 方案2：仅返回雪花ID（保持原有长度，依赖雪花算法自身的唯一性）
                return String.valueOf(snowflakeId);
            }
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("Clock moved backwards") && enableUUIDFallback) {
                // 处理时钟回拨：使用 UUID 作为备选方案
                clockBackwardCount.incrementAndGet();
                return "uuid-" + clockBackwardCount.get() + "-" + UUID.randomUUID();
            }
            throw e;
        }
    }

    /**
     * 生成纯雪花算法 ID（保持原有功能）
     */
    public long generateSnowflakeId() {
        return snowflakeIdGenerator.nextId();
    }

    /**
     * 生成纯 UUID
     */
    public String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
