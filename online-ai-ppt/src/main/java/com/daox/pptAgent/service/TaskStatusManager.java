package com.daox.pptAgent.service;

import com.daox.pptAgent.entity.vo.TaskStatus;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 任务状态管理服务
 */
@Service
public class TaskStatusManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String TASK_KEY_PREFIX = "ppt_task_status:";
    private static final Duration TASK_TIMEOUT = Duration.ofHours(1); // 任务状态保留1小时

    public TaskStatusManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 创建一个新任务并返回其初始状态对象
     *
     * @return 任务状态对象
     */
    public TaskStatus createTask() {
        String taskId = UUID.randomUUID().toString();
        TaskStatus status = new TaskStatus(taskId);
        saveStatus(status);
        return status;
    }

    /**
     * 获取任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态，如果不存在则返回null
     */
    public TaskStatus getStatus(String taskId) {
        return (TaskStatus) redisTemplate.opsForValue().get(TASK_KEY_PREFIX + taskId);
    }

    /**
     * 更新任务为成功状态
     *
     * @param taskId    任务ID
     * @param resultUrl 成功后的结果URL
     */
    public void setSuccess(String taskId, String resultUrl) {
        TaskStatus status = getStatus(taskId);
        if (status != null) {
            status.setStatus(TaskStatus.Status.COMPLETED);
            status.setResultUrl(resultUrl);
            status.setUpdateTime(LocalDateTime.now());
            saveStatus(status);
        }
    }

    /**
     * 更新任务为失败状态
     *
     * @param taskId       任务ID
     * @param errorMessage 失败信息
     */
    public void setFailure(String taskId, String errorMessage) {
        TaskStatus status = getStatus(taskId);
        if (status != null) {
            status.setStatus(TaskStatus.Status.FAILED);
            status.setErrorMessage(errorMessage);
            status.setUpdateTime(LocalDateTime.now());
            saveStatus(status);
        }
    }

    /**
     * 更新任务状态（例如，更新为PROCESSING）
     *
     * @param taskId    任务ID
     * @param newStatus 新的状态
     */
    public void updateStatus(String taskId, TaskStatus.Status newStatus) {
        TaskStatus status = getStatus(taskId);
        if (status != null) {
            status.setStatus(newStatus);
            status.setUpdateTime(LocalDateTime.now());
            saveStatus(status);
        }
    }

    /**
     * 保存任务状态
     *
     * @param status 任务状态对象
     */
    private void saveStatus(TaskStatus status) {
        redisTemplate.opsForValue().set(TASK_KEY_PREFIX + status.getTaskId(), status, TASK_TIMEOUT);
    }
}
