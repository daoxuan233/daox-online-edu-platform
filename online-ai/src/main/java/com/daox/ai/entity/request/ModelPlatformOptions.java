package com.daox.ai.entity.request;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * 模型平台选项
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class ModelPlatformOptions {
    /**
     * 模型平台
     * 默认为deep seek
     */
    private String platform; // 平台
    private String model;
}
