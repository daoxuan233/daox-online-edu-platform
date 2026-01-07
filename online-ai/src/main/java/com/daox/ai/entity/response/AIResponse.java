package com.daox.ai.entity.response;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * AI响应数据返回 类
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class AIResponse implements Serializable {
    /**
     * 记录id
     */
    private String recordId;
    /**
     * 响应内容
     */
    private String content;
}
