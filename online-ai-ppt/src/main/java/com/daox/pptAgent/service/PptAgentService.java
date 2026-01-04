package com.daox.pptAgent.service;

import com.daox.pptAgent.engine.PptGenerationEngine;
import com.daox.pptAgent.model.ConversationDocument;
import com.daox.pptAgent.model.SessionState;
import com.daox.pptAgent.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI代理服务
 * 职责：作为系统大脑，处理核心对话逻辑。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PptAgentService {

    private final ChatClient.Builder chatClientBuilder;
    private final SessionManager sessionManager;
    private final TaskStatusManager taskStatusManager;
    private final ConversationRepository conversationRepository;
    private final PptToolbox pptToolbox;


    // 这是指导AI行为的核心指令
    private final String systemPrompt = """
            你是一个专业的PPT制作助手。你的工作流程严格分为两步：
            1.  **规划大纲**: 根据用户的主题、受众和页数要求，你必须首先调用 `createPptOutline` 工具来生成一个结构化的PPT大纲。生成后，将大纲清晰地展示给用户。
            2.  **生成PPT**: 在你展示完大纲后，你必须等待用户的确认。当用户的意图是确认或同意时（例如，他们说“好的”，“可以”，“就用这个大纲”等），你**必须**调用 `generateFinalPpt` 工具来启动最终的PPT生成任务。在用户确认之前，绝对不允许调用 `generateFinalPpt`。
            
            如果用户提出修改意见，你应该与他们沟通，然后根据新的要求再次调用 `createPptOutline` 来更新大纲。
            当前会话中已经确认的大纲: {outline}
            """;

    /**
     * 处理用户聊天请求
     *
     * @param userId    用户ID
     * @param userInput 用户输入内容
     * @return 聊天响应结果
     */
    public Object chat(String userId, String userInput) {
        log.info("处理用户聊天请求 - 用户ID: {}, 输入: {}", userId, userInput);

        // 1. 加载或创建会话，并添加用户最新消息
        SessionState sessionState = sessionManager.getSession(userId);
        sessionState.addMessage("user", userInput);

        // 2. 创建ChatClient
        ChatClient chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();

        // 3. 构建带有系统指令和历史记录的完整上下文
        String outlineState = sessionState.getApprovedOutline() != null ?
                sessionState.getApprovedOutline().toString() : "无";
        
        // 构建对话历史上下文（包含所有历史消息）
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("对话历史：\n");
        
        // 获取历史记录（除了最后一条，因为最后一条就是当前的userInput）
        List<com.daox.pptAgent.model.ChatMessage> history = sessionState.getHistory();
        int historySize = history.size();
        
        if (historySize > 1) {
            // 只显示最近的10轮对话（避免上下文过长）
            int startIndex = Math.max(0, historySize - 21); // 10轮 = 20条消息
            for (int i = startIndex; i < historySize - 1; i++) {
                com.daox.pptAgent.model.ChatMessage msg = history.get(i);
                String roleLabel = msg.role().equals("user") ? "用户" : "助手";
                contextBuilder.append(roleLabel).append(": ").append(msg.content()).append("\n");
            }
            log.info("加载了 {} 条历史消息", historySize - 1);
        } else {
            contextBuilder.append("（这是本次对话的第一条消息）\n");
        }
        
        contextBuilder.append("\n当前用户输入: ").append(userInput);
        String fullContext = contextBuilder.toString();

        // 4. 使用 Spring AI 1.0.0 的 API，将历史上下文传递给AI
        String response;
        try {
            log.debug("发送给AI的完整上下文: {}", fullContext);
            
            // 使用 ChatClient 的流式 API，将完整上下文作为user消息发送
            response = chatClient.prompt()
                    .system(sp -> sp.text(systemPrompt)
                            .param("outline", outlineState))
                    .user(fullContext) // 这里传递完整的上下文，包含历史记录
                    .tools(pptToolbox)  // 在 prompt 中注册工具
                    .toolContext(java.util.Map.of("userId", userId))  // 传递 userId 到工具上下文
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("调用AI模型失败", e);
            response = "抱歉，处理您的请求时出现错误。请稍后再试。";
        }

        // 5. 保存助手响应到会话历史
        if (response != null && !response.isEmpty()) {
            sessionState.addMessage("assistant", response);
            log.info("助手响应已保存到会话历史，当前历史总数: {}", sessionState.getHistory().size());
        }

        // 6. 保存会话状态到数据库
        ConversationDocument doc = new ConversationDocument();
        doc.setUserId(userId);
        doc.setHistory(sessionState.getHistory());
        doc.setApprovedOutline(sessionState.getApprovedOutline());
        doc.setStatus("ACTIVE");
        doc.setFinalPptPath(null);
        doc.setStartTime(sessionState.getCreateTime());
        doc.setEndTime(LocalDateTime.now());

        String conversationId = null;
        try {
            ConversationDocument savedDoc = conversationRepository.save(doc);
            conversationId = savedDoc.getId();
            log.info("会话状态已保存到MongoDB - ID: {}", conversationId);
        } catch (Exception e) {
            log.error("保存会话状态失败", e);
            // 如果数据库保存失败，使用userId作为会话ID
            conversationId = userId;
        }

        // 7. 保存会话到Redis
        sessionManager.saveSession(userId, sessionState);

        log.info("处理用户聊天请求完成 - 用户ID: {}, 会话ID: {}, 响应: {}", userId, conversationId, response);

        // 8. 尝试从响应中提取任务ID（当AI调用generateFinalPpt工具时）
        String extractedTaskId = extractTaskIdFromResponse(response);
        
        // 9. 构建返回结果
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("userId", userId);
        result.put("reply", response);  // 使用 "reply" 而不是 "response"，以便前端更好解析
        result.put("conversationId", conversationId != null ? conversationId : userId);
        result.put("sessionId", userId);  // sessionId 为 userId
        result.put("timestamp", LocalDateTime.now().toString());
        
        // 如果提取到任务ID，添加到响应中
        if (extractedTaskId != null) {
            result.put("taskId", extractedTaskId);
            log.info("检测到PPT生成任务 - 任务ID: {}", extractedTaskId);
        }
        
        return result;
    }

    /**
     * 从 AI 响应文本中提取任务ID
     * 当AI调用generateFinalPpt工具时，响应中会包含类似"ID: ppt-task-xxx"的文本
     * 
     * @param response AI的响应文本
     * @return 提取到的任务ID，如果没有则返回null
     */
    private String extractTaskIdFromResponse(String response) {
        if (response == null || response.isEmpty()) {
            return null;
        }
        
        try {
            // 匹配多种可能的任务ID格式
            // 例如："ID: ppt-task-xxx" 或 "任务ID：ppt-task-xxx" 或 "taskId: ppt-task-xxx"
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(ID|id|taskId|task_id|任务ID|任务id)[:\s：]+([a-zA-Z0-9-]{20,})"
            );
            java.util.regex.Matcher matcher = pattern.matcher(response);
            
            if (matcher.find()) {
                String taskId = matcher.group(2);
                log.debug("从响应中提取到任务ID: {}", taskId);
                return taskId;
            }
        } catch (Exception e) {
            log.warn("提取任务ID失败", e);
        }
        
        return null;
    }
    
    private String persistConversation(String userId, SessionState sessionState, String status) {
        ConversationDocument doc = new ConversationDocument();
        doc.setUserId(userId);
        doc.setHistory(sessionState.getHistory());
        doc.setApprovedOutline(sessionState.getApprovedOutline());
        doc.setStatus(status);
        doc.setStartTime(sessionState.getCreateTime());
        doc.setEndTime(LocalDateTime.now()); // 记录触发时间
        ConversationDocument savedDoc = conversationRepository.save(doc);
        return savedDoc.getId();
    }
}