package com.daox.online.entity.views.requestVO.chat.group;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * 教师课程群聊全员禁言请求体。
 * <p>
 * 当前采用分钟级 TTL，实现临时全员禁言自动到期恢复。
 * </p>
 */
@Getter
@Setter
public class GroupMuteAllRequest {

    /**
     * 全员禁言时长，单位：分钟。
     */
    @Min(value = 1, message = "全员禁言时长不能小于1分钟")
    @Max(value = 10080, message = "全员禁言时长不能超过10080分钟")
    private Long durationMinutes;
}