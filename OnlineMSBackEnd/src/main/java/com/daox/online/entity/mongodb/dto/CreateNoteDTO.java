package com.daox.online.entity.mongodb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 创建学习笔记 DTO
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateNoteDTO implements Serializable {
    // courseId, chapterId, sectionId 都是可选的，不提供就是自由笔记
    private String courseId;
    private String chapterId;
    private String sectionId;

    // title 和 content 是必填项
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;

    /**
     * 笔记来源类型，"AI_CHAT", "EMAIL", "MANUAL" = 除ai产生外的内部数据
     */
    private String sourceType;
    /**
     * 来源引用ID，根据source_type不同，存储不同的引用ID
     * 例如：AI_CHAT 可能是对话ID，EMAIL 可能是邮件ID，MANUAL = 除ai外的内部数据为空(NULL)
     */
    private String sourceRefId;

    private List<String> tags;
    private Boolean isPrivate = true;
    private Integer videoTime;
}
