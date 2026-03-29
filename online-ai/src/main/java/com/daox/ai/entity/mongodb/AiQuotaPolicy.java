package com.daox.ai.entity.mongodb;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * AI 调用额度策略。
 * <p>
 * 策略按作用域生效，优先级为 USER > ROLE > GLOBAL。
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "ai_quota_policies")
@CompoundIndex(name = "scope_unique_idx", def = "{'scope_type': 1, 'scope_value': 1}", unique = true)
public class AiQuotaPolicy {

    /**
     * 配额作用域。
     */
    public enum ScopeType {
        GLOBAL,
        ROLE,
        USER
    }

    @Id
    private String id;

    @Field("scope_type")
    private ScopeType scopeType;

    @Field("scope_value")
    private String scopeValue;

    @Field("daily_limit")
    private Integer dailyLimit;

    @Field("hourly_limit")
    private Integer hourlyLimit;

    @Field("enabled")
    private Boolean enabled;

    @Field("remark")
    private String remark;

    @Field("updated_by")
    private String updatedBy;

    @Field("updated_at")
    private LocalDateTime updatedAt;
}