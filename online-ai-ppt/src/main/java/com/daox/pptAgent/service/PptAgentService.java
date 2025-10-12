package com.daox.pptAgent.service;

import com.daox.pptAgent.model.SessionState;
import com.daox.pptAgent.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * AI代理服务（已重构为无状态）
 * 职责：作为系统大脑，处理核心对话逻辑。
 */
@Service
@RequiredArgsConstructor // 使用Lombok简化构造函数注入
public class PptAgentService {
    private final ChatClient.Builder chatClientBuilder;
    private final SessionManager sessionManager;
    private final ConversationRepository conversationRepository;
    // PptToolbox中的Bean会被Spring AI自动发现，无需在此处注入

    public String chat(String userId, String userInput) {
        // 1. 加载或创建会话
        SessionState sessionState = sessionManager.getSession(userId);
        sessionState.addMessage("user", userInput);

        // 2. 构建ChatClient，并启用函数调用
        ChatClient chatClient = chatClientBuilder.build();

        // --- TODO: 在阶段三，这里将是完整的、带Prompt和对话历史的AI调用 ---
        // 在阶段二，我们先模拟一个简单的调用，以验证流程
        // 假设用户输入 "做一个关于java的ppt"
        // 我们的目标是看到AI调用 createPptOutline 函数

        // 3. (模拟) AI调用与响应
        // 真实的AI调用会在这里
        // var response = chatClient.prompt().user(userInput)...
        System.out.println("用户 " + userId + " 发送消息: " + userInput);
        String aiResponse = "好的，正在为您规划关于 '" + userInput + "' 的PPT大纲...";
        sessionState.addMessage("assistant", aiResponse);

        // 4. 保存更新后的会话状态到Redis
        sessionManager.saveSession(userId, sessionState);

        // 5. 返回AI的响应
        return aiResponse;
    }
}
