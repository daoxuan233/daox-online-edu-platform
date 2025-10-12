package com.daox.online.service;

import com.daox.online.entity.mongodb.ChatMessage;
import com.daox.online.entity.views.responseVO.chat.LastMessageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天消息服务类，提供消息相关的数据库操作。
 */
@Service
public class ChatMessageService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public ChatMessageService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 查询指定用户的每个私聊会话的最后一条消息。
     * 使用MongoDB聚合管道实现，以获得最佳性能。
     *
     * @param userId 当前用户ID
     * @return 每个会话的最后一条消息列表
     */
    public List<LastMessageInfo> findLastMessagesPerConversation(String userId) {
        // 步骤 1: $match - 筛选出与该用户相关的、类型为PRIVATE的消息
        // 查询条件: message_type 是 PRIVATE 并且 (sender_id 是 userId 或者 receiver_id 是 userId)
        MatchOperation matchOperation = Aggregation.match(
                new Criteria().andOperator(
                        Criteria.where("message_type").is(ChatMessage.MessageType.PRIVATE),
                        new Criteria().orOperator(
                                Criteria.where("sender_id").is(userId),
                                Criteria.where("receiver_id").is(userId)
                        )
                )
        );

        // 步骤 2: $sort - 按时间戳降序，确保每个会话的第一条就是最新的
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "timestamp");

        // 步骤 3: $group - 按会话ID分组，并获取每个组的第一条记录（即最新消息）
        // 使用 $$ROOT 将整个文档作为 lastMessage 字段的值
        GroupOperation groupOperation = Aggregation.group("conversation_id")
                .first("$$ROOT").as("lastMessage");

        // 步骤 4: $replaceRoot - 将 lastMessage 字段提升为根文档，方便后续处理
        ReplaceRootOperation replaceRootOperation = Aggregation.replaceRoot("lastMessage");

        // 步骤 5: $project - 投影成 LastMessageInfo DTO 的结构
        // 这一步可以确保字段精确映射，特别是将MongoDB的 _id 字段映射到我们DTO中的 id 字段
        ProjectionOperation projectionOperation = Aggregation.project()
                .and("_id").as("id") // 将数据库的 _id 映射为 DTO 的 id
                .and("conversation_id").as("conversationId") // 将 conversation_id 映射为 conversationId
                .and("sender_id").as("senderId")             // 将 sender_id 映射为 senderId
                .and("receiver_id").as("receiverId")         // 将 receiver_id 映射为 receiverId
                .and("content").as("content")               // 其余同名字段也明确写出，更清晰
                .and("timestamp").as("timestamp")
                .and("status").as("status");

        // 构建完整的聚合管道
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                sortOperation,
                groupOperation,
                replaceRootOperation,
                projectionOperation
        );

        // 执行聚合查询，并指定输入集合名和输出类型
        AggregationResults<LastMessageInfo> results = mongoTemplate.aggregate(
                aggregation, "chat_messages", LastMessageInfo.class
        );

        // 返回映射后的结果列表
        return results.getMappedResults();
    }
}
