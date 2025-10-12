package com.daox.pptAgent.entity.vo;

/**
 * 用于接收聊天请求的简单数据对象。
 *
 * @param message 用户发送的消息
 */
public record ChatRequest(String message) {
}