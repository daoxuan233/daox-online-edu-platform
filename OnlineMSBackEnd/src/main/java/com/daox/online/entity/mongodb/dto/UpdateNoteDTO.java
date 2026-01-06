package com.daox.online.entity.mongodb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 更新笔记 DTO
 * 用于编辑已存在的笔记内容
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateNoteDTO implements Serializable {

    // 允许修改标题
    private String title;

    // 允许修改内容 (富文本)
    private String content;

    // 允许重新打标签
    private List<String> tags;

    // 允许修改私密状态
    private Boolean isPrivate;

    // 注意：
    // 1. 状态 (INBOX/ARCHIVED) 的流转通常通过专门的 archive 接口处理，不在此处修改
    // 2. videoTime 通常创建后不建议随意修改，如需修改可在此添加字段
}