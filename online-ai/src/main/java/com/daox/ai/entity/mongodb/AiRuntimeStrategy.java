package com.daox.ai.entity.mongodb;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 运行时策略。
 * <p>
 * 管理员可以在线调整审查词、重试次数以及降级平台顺序。
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "ai_runtime_strategies")
public class AiRuntimeStrategy {

    /**
     * 默认策略主键。
     */
    public static final String DEFAULT_ID = "default";

    @Id
    private String id;

    @Field("review_enabled")
    private Boolean reviewEnabled;

    @Field("blocked_keywords")
    private List<String> blockedKeywords = new ArrayList<>();

    @Field("review_keywords")
    private List<String> reviewKeywords = new ArrayList<>();

    @Field("max_attempts")
    private Integer maxAttempts;

    @Field("fallback_platforms")
    private List<String> fallbackPlatforms = new ArrayList<>();

    @Field("updated_by")
    private String updatedBy;

    @Field("updated_at")
    private LocalDateTime updatedAt;
}