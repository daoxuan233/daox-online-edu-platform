package com.daox.ai.service;

import com.daox.ai.entity.dto.ConversationSummaryDTO;
import com.daox.ai.entity.mongodb.ChatMessage;
import com.daox.ai.entity.response.AIResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AIChatService {
    /**
     * 主方法：处理AI流式对话并异步保存记录。 [default -- OpenAI]
     * 这是Controller层应该调用的方法。
     *
     * @param userId         发起请求的用户ID
     * @param question       用户的问题
     * @param conversationId 继续对话，则传入之前的会话ID 否则就是新会话ID
     * @return 返回给前端的实时内容流 (Flux<String>)
     */
    Flux<AIResponse> streamChatAndSave(String userId, String question, String conversationId);

    /**
     * 主方法：处理AI流式对话并异步保存记录。 [deepseek]
     * 这是Controller层应该调用的方法。
     *
     * @param userId         发起请求的用户ID
     * @param question       用户的问题
     * @param conversationId 继续对话，则传入之前的会话ID 否则就是新会话ID
     * @return 返回给前端的实时内容流 (Flux<String>)
     */
    Flux<String> streamChatAndSaveDeepSeek(String userId, String question, String conversationId);

    Flux<AIResponse> streamChatAndSaveDeepSeekRes(String userId, String question, String conversationId);

    /**
     * 主方法：处理AI流式对话并异步保存记录。 [qwen]
     * 这是Controller层应该调用的方法。
     *
     * @param userId         发起请求的用户ID
     * @param question       用户的问题
     * @param conversationId 继续对话，则传入之前的会话ID 否则就是新会话ID
     * @return 返回给前端的实时内容流 (Flux<String>)
     */
    Flux<String> streamChatAndSaveQwen(String userId, String question, String conversationId);

    Flux<AIResponse> streamChatAndSaveQwenRes(String userId, String question, String conversationId);

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
