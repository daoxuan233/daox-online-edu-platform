package com.daox.ai.service;

import com.daox.ai.entity.dto.ConversationSummaryDTO;
import com.daox.ai.entity.mongodb.ChatMessage;
import com.daox.ai.entity.request.ModelPlatformOptions;
import com.daox.ai.entity.response.AIResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AIChatService {
    /**
     * 统一的 AI 对话入口。
     * <p>
     * 该方法会在一次调用中串联额度检查、内容审查、重试降级、结果落库与审计记录写入。
     *
     * @param userId         发起请求的用户ID
     * @param userRole       发起请求的用户角色
     * @param question       用户的问题
     * @param conversationId 继续对话，则传入之前的会话ID 否则就是新会话ID
     * @param options        模型平台与模型参数
     * @return 返回给前端的流式响应
     */
    Flux<AIResponse> streamChatAndSave(String userId, String userRole, String question,
                                       String conversationId, ModelPlatformOptions options);

    /**
     * 获取会话历史记录列表
     * @param userId 用户id
     * @return 会话列表
     */
    Mono<List<ConversationSummaryDTO>> getConversationSummaries(String userId);

    /**
     * 分页获取单个对话中的消息历史。
     * @param conversationId 对话ID
     * @param page 页码 (从0开始)
     * @param size 每页大小
     * @return 一页消息的列表，用 Mono 包装
     */
    Mono<List<ChatMessage>> getMessagesForConversation(String conversationId, int page, int size);

    /**
     * 删除一个会话
     * @param userId 用户id
     * @param conversationId 会话id
     */
    Mono<Void> deleteConversation(String userId,String conversationId);
}
