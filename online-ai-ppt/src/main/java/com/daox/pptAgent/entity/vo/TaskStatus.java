package com.daox.pptAgent.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 存储在Redis中的异步任务状态对象
 */
@Data
@NoArgsConstructor // 需要一个无参构造函数用于反序列化
public class TaskStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 会话ID
     */
    private String conversationId;
    /**
     * 任务状态
     */
    private Status status;
    /**
     * 任务结果URL
     */
    private String resultUrl;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 任务状态枚举
     */
    public enum Status {
        PENDING,   // 排队中
        PROCESSING, // 处理中
        COMPLETED,  // 已完成
        FAILED      // 失败
    }

    public TaskStatus(String taskId) {
        this.taskId = taskId;
        this.status = Status.PENDING;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
}