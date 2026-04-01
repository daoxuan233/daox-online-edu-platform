package com.daox.online.entity.mongodb;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类，用于统一存储所有类型的消息。
 * 支持一对一私聊、群聊、系统通知、AI对话等。
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "chat_messages")
@CompoundIndex(name = "conv_time_idx", def = "{'conversation_id': 1, 'timestamp': -1}") // 创建复合索引
public class ChatMessage {
    /**
     * 消息ID。
     */
    @Id
    private String id;

    /**
     * 会话ID，用于将消息分组到特定的对话中（私聊、群聊、AI对话等）<br />
     * 1、私聊： 一个唯一且固定的 conversationId,将两个用户的ID组合起来。为了保证组合的唯一性（无论A找B还是B找A，ID都应该相同） <br />
     * 2、群聊： 直接使用群组的唯一ID - 班级默认为一个群聊 [ 这个时候的群聊id应当是课程id ] <br />
     * 3、AI对话：将用户ID与一个固定的AI标识符组合，如果希望用户可以与AI开启多个独立的对话，那么可以在每次新会话开始时生成一个全新的唯一ID - UUID <br />
     * 4、系统通知：使用 "system" - <br/>Ⅰ. 用户相关的通知(发给特定用户的通知): userId + "_system" <br/>Ⅱ. 全局或主题通知: 如果是关于某个课程的全局通知，可以将 conversationId 设置为 courseId + "_announcement"
     */
    @Field("conversation_id")
    private String conversationId;

    /**
     * 发送者ID。
     * 对于系统通知，可以是 "system"。
     * 对于AI对话，可以是 "ai_assistant"。
     */
    @Indexed
    @Field("sender_id")
    private String senderId;

    /**
     * 接收者ID（主要用于一对一私聊）。
     * 在群聊中可以为 null。
     */
    @Indexed
    @Field("receiver_id")
    private String receiverId;

    /**
     * 群组ID（仅用于群聊消息）。
     */
    @Indexed
    @Field("group_id")
    private String groupId;

    /**
     * 消息类型
     */
    @Field("message_type")
    private MessageType messageType;

    /**
     * 消息内容类型
     */
    @Field("content_type")
    private ContentType contentType;

    /**
     * 消息内容。
     * 对于文本，是字符串。
     * 对于图片/文件，URL。
     */
    @Field("content")
    private String content;

    /**
     * 消息发送时间戳。
     */
    @Indexed
    @Field("timestamp")
    private LocalDateTime timestamp;

    /**
     * 消息状态
     */
    @Field("status")
    private MessageStatus status;

    /**
     * 好友标签
     * 好友对话：friend
     * 临时对话：temporary
        * 课程群聊：teacher / student
     */
    @Field("tag")
    private String tag;

    /**
     * 消息类型
     */
    public enum MessageType {
        PRIVATE, // 一对一私聊
        GROUP,   // 群聊
        SYSTEM,  // 系统通知
        AI       // AI对话
    }

    /**
     * 消息内容类型
     */
    public enum ContentType {
        TEXT,      // 纯文本
        IMAGE,     // 图片
        FILE,      // 文件
        RICH_TEXT  // 富文本
    }

    /**
     * 消息状态
     */
    public enum MessageStatus {
        SENT,      // 已发送
        DELIVERED, // 已送达
        READ,      // 已读
    }
}
