package com.daox.online.repository.mongodb;

import com.daox.online.entity.mongodb.ChatMessage;
import com.daox.online.entity.mongodb.Discussion;
import com.daox.online.entity.views.responseVO.chat.LastMessageInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
     * 查询课程群聊的历史消息，并按时间倒序返回。
     *
     * @param conversationId 会话ID，即课程ID
     * @param messageType    消息类型，固定为 GROUP
     * @param pageable       分页参数
     * @return 群聊消息列表
     */
    List<ChatMessage> findByConversationIdAndMessageTypeOrderByTimestampDesc(String conversationId,
                                                                             ChatMessage.MessageType messageType,
                                                                             Pageable pageable);

    /**
     * 查询指定时间点之前的课程群聊消息，并按时间倒序返回。
     *
     * @param conversationId 会话ID，即课程ID
     * @param messageType    消息类型，固定为 GROUP
     * @param beforeTime     查询时间上界（不含）
     * @param pageable       分页参数
     * @return 群聊消息列表
     */
    List<ChatMessage> findByConversationIdAndMessageTypeAndTimestampBeforeOrderByTimestampDesc(String conversationId,
                                                                                                ChatMessage.MessageType messageType,
                                                                                                LocalDateTime beforeTime,
                                                                                                Pageable pageable);

    /**
     * 查询课程群聊可见的历史消息，并按时间倒序返回。
     * <p>
     * 课程群当前需要同时展示普通群消息与教师系统公告，因此这里采用消息类型集合查询。
     * </p>
     *
     * @param conversationId 会话ID，即课程ID
     * @param messageTypes   消息类型集合
     * @param pageable       分页参数
     * @return 群聊消息列表
     */
    List<ChatMessage> findByConversationIdAndMessageTypeInOrderByTimestampDesc(String conversationId,
                                                                               List<ChatMessage.MessageType> messageTypes,
                                                                               Pageable pageable);

    /**
     * 查询指定时间点之前课程群聊可见的历史消息，并按时间倒序返回。
     *
     * @param conversationId 会话ID，即课程ID
     * @param messageTypes   消息类型集合
     * @param beforeTime     查询时间上界（不含）
     * @param pageable       分页参数
     * @return 群聊消息列表
     */
    List<ChatMessage> findByConversationIdAndMessageTypeInAndTimestampBeforeOrderByTimestampDesc(String conversationId,
                                                                                                  List<ChatMessage.MessageType> messageTypes,
                                                                                                  LocalDateTime beforeTime,
                                                                                                  Pageable pageable);

    /**
     * 获取指定课程群聊的最后一条消息。
     *
     * @param conversationId 会话ID，即课程ID
     * @param messageType    消息类型，固定为 GROUP
     * @return 最新消息；若不存在则返回 null
     */
    ChatMessage findFirstByConversationIdAndMessageTypeOrderByTimestampDesc(String conversationId,
                                                                            ChatMessage.MessageType messageType);

    /**
     * 获取指定课程群聊最近一条对群成员可见的消息。
     *
     * @param conversationId 会话ID，即课程ID
     * @param messageTypes   消息类型集合
     * @return 最新消息；若不存在则返回 null
     */
    ChatMessage findFirstByConversationIdAndMessageTypeInOrderByTimestampDesc(String conversationId,
                                                                              List<ChatMessage.MessageType> messageTypes);

    /**
     * 更新指定会话中接收者为特定用户的消息状态为已读
     *
     * @param conversationId 会话ID
     * @param receiverId     接收者ID
     */
    @Query("{'conversation_id': ?0, 'receiver_id': ?1, 'status': {'$ne': 'READ'}}")
    @Update("{'$set': {'status': 'READ'}}")
    void updateStatusToRead(String conversationId, String receiverId);

}