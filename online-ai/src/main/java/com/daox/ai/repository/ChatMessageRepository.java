package com.daox.ai.repository;

import com.daox.ai.entity.mongodb.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ChatMessage 的响应式数据仓库接口。
 * 提供对 chat_messages 集合的异步、非阻塞访问。
 */
@Repository
public interface ChatMessageRepository extends ReactiveMongoRepository<ChatMessage, String> {

    /**
     * 根据会话ID分页查找消息。
     * @param conversationId 会话ID
     * @param pageable 分页参数（包含页码、每页大小和排序信息）
     * @return 消息的Flux流
     */
    Flux<ChatMessage> findByConversationId(String conversationId, Pageable pageable);

    /**
     * 删除指定会话ID的所有消息。
     * @param conversationId 要删除消息的会话ID
     * @return 一个 Mono<Void>，表示删除操作完成
     */
    Mono<Void> deleteByConversationId(String conversationId);
}
