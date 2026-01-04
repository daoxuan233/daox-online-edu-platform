package com.daox.pptAgent.entity.vo;

/**
 * 异步生成任务的视图对象，当任务启动后立即返回
 *
 * @param taskId  任务的唯一ID，用于后续查询状态
 * @param message 给用户的提示信息，例如 "PPT生成任务已开始"
 */
public record GenerationTask(String taskId, String message) {
}
