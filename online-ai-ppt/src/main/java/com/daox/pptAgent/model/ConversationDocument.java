package com.daox.pptAgent.model;

import com.daox.pptAgent.service.PptToolbox;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 映射MongoDB中的 "conversations" 集合的持久化对象。
 * 用于保存会话数据，包括用户ID、大纲、历史记录、状态、最终PPT路径、开始时间和结束时间。
 */
@Data
@Document(collection = "conversations")
public class ConversationDocument {

    /**
     * MongoDB文档ID，自动生成。
     * 用于唯一标识每个会话。
     */
    @Id
    private String id;

    /**
     * 用户ID，用于关联用户。
     */
    private String userId;

    /**
     * 审核通过后的PPT大纲。
     */
    private PptToolbox.PptOutline approvedOutline;

    /**
     * 会话历史记录。
     */
    private List<ChatMessage> history;

    /**
     * 会话状态。
     */
    private String status; // 例如 "COMPLETED", "FAILED"

    /**
     * 最终生成的PPT文件的路径。
     */
    private String finalPptPath;

    /**
     * 会话开始时间。
     */
    private LocalDateTime startTime;

    /**
     * 会话结束时间。
     */
    private LocalDateTime endTime;
}
