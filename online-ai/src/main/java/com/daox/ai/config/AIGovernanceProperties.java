package com.daox.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 治理配置。
 * <p>
 * 该配置作为治理的默认值来源，用于在管理员未显式写入运行时策略或配额策略时提供回退配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.ai.governance")
public class AIGovernanceProperties {

    /**
     * 额度控制默认配置。
     */
    private Quota quota = new Quota();

    /**
     * 内容审查默认配置。
     */
    private Review review = new Review();

    /**
     * 重试与降级默认配置。
     */
    private Retry retry = new Retry();

    /**
     * 配额配置。
     */
    @Data
    public static class Quota {
        private Integer globalDailyLimit = 120;
        private Integer globalHourlyLimit = 20;
        private Integer studentDailyLimit = 40;
        private Integer studentHourlyLimit = 8;
        private Integer teacherDailyLimit = 100;
        private Integer teacherHourlyLimit = 20;
        private Integer adminDailyLimit = 300;
        private Integer adminHourlyLimit = 60;
    }

    /**
     * 审查配置。
     */
    @Data
    public static class Review {
        private boolean enabled = true;
        private List<String> blockedKeywords = new ArrayList<>(List.of("刷单", "赌博", "外挂", "爆破"));
        private List<String> reviewKeywords = new ArrayList<>(List.of("代写", "绕过检测", "破解", "批量注册"));
    }

    /**
     * 重试与降级配置。
     */
    @Data
    public static class Retry {
        private Integer maxAttempts = 3;
        private List<String> fallbackPlatforms = new ArrayList<>(List.of("openai", "deepseek", "qwen"));
    }
}