package com.daox.online.entity.views.requestVO.chat.group;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 教师群聊禁言请求体。
 * <p>
 * 当前实现使用分钟级禁言时长，便于配合 Redis TTL 做自动解禁。
 * </p>
 */
@Getter
@Setter
public class GroupMuteRequest {

    /**
     * 被禁言学生ID。
     */
    @NotBlank(message = "被禁言学生ID不能为空")
    private String targetUserId;

    /**
     * 禁言时长，单位：分钟。
     */
    @Min(value = 1, message = "禁言时长不能小于1分钟")
    @Max(value = 10080, message = "禁言时长不能超过10080分钟")
    private Long durationMinutes;
}