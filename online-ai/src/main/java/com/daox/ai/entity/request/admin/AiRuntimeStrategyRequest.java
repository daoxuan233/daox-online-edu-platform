package com.daox.ai.entity.request.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 管理员维护 AI 运行时策略的请求体。
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AiRuntimeStrategyRequest {

    /**
     * 是否启用审查。
     */
    private Boolean reviewEnabled;

    /**
     * 直接阻断词列表。
     */
    private List<String> blockedKeywords;

    /**
     * 人工审查词列表。
     */
    private List<String> reviewKeywords;

    /**
     * 最大尝试次数。
     */
    private Integer maxAttempts;

    /**
     * 平台降级顺序。
     */
    private List<String> fallbackPlatforms;
}