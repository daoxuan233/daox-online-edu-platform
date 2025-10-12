package com.daox.online.repository.mongodb;

import com.daox.online.entity.mongodb.ChatMessage;
import com.daox.online.entity.mongodb.Discussion;
import com.daox.online.entity.views.responseVO.chat.LastMessageInfo;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 聊天消息仓库
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {


    /**
     * 计算指定会话中当前用户的未读消息数（所有非已读状态的消息）
     *
     * @param conversationId 会话ID
     * @param currentUserId  当前用户ID
     * @param status         排除的消息状态 (e.g., READ)
     * @return 未读消息数
     */
    long countByConversationIdAndReceiverIdAndStatusNot(String conversationId, String currentUserId, ChatMessage.MessageStatus status);

    /**
     * 计算指定接收者的未读消息总数
     *
     * @param receiverId 接收者ID
     * @param status     排除的消息状态 (e.g., READ)
     * @return 未读消息总数
     */
    long countByReceiverIdAndStatusNot(String receiverId, ChatMessage.MessageStatus status);


    /**
     * 获取指定会话的所有聊天记录，并按时间逆序排列
     *
     * @param conversationId 会话ID
     * @return 聊天记录列表
     */
    List<ChatMessage> findByConversationIdOrderByTimestampDesc(String conversationId);

    /**
     * 更新指定会话中接收者为特定用户的消息状态为已读
     *
     * @param conversationId 会话ID
     * @param receiverId     接收者ID
     */
    @Query("{'conversationId': ?0, 'receiverId': ?1, 'status': {'$ne': 'READ'}}")
    @Update("{'$set': {'status': 'READ'}}")
    void updateStatusToRead(String conversationId, String receiverId);

}