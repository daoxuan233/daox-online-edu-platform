package com.daox.online.entity.views.responseVO.chat.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程群聊全员禁言状态视图对象。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GroupMuteAllStatusVO implements Serializable {

    /**
     * 是否开启全员禁言。
     */
    private Boolean enabled;

    /**
     * 剩余禁言秒数。
     */
    private Long remainingSeconds;

    /**
     * 预计自动解除时间。
     */
    private LocalDateTime expireAt;
}