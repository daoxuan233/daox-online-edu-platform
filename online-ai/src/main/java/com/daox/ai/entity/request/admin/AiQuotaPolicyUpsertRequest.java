package com.daox.ai.entity.request.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.daox.ai.entity.mongodb.AiQuotaPolicy;

/**
 * 管理员维护 AI 配额策略的请求体。
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AiQuotaPolicyUpsertRequest {

    /**
     * 作用域类型。
     */
    private AiQuotaPolicy.ScopeType scopeType;

    /**
     * 作用域值。
     */
    private String scopeValue;

    /**
     * 每日限额。
     */
    private Integer dailyLimit;

    /**
     * 每小时限额。
     */
    private Integer hourlyLimit;

    /**
     * 是否启用。
     */
    private Boolean enabled;

    /**
     * 备注信息。
     */
    private String remark;
}