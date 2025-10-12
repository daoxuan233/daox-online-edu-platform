package com.daox.pptAgent.model;

import java.io.Serializable;

/**
 * 表示单条聊天消息的数据记录。
 * 实现 Serializable 接口，以便可以被存入Redis。
 * @param role 角色，例如 "user" 或 "assistant" [告诉模型]
 * @param content 消息内容
 */
public record ChatMessage(String role, String content) implements Serializable {
    // Java Record 自动处理构造函数、getter、equals、hashCode和toString
}