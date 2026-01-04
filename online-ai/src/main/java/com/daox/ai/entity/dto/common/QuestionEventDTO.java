package com.daox.ai.entity.dto.common;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class QuestionEventDTO implements Serializable {

    /** 对应 MongoDB _id */
    private String id;

    /** 对应 courseId */
    private String courseId;

    /** 对应 creatorId */
    private String creatorId;

    /** 对应 type.name() */
    private String type;

    /** 对应 difficulty.name() */
    private String difficulty;

    /** 对应 stem */
    private String stem;

    /** 对应 options，我们需要传输选项内容以便 AI 理解 */
    private List<OptionDTO> options;

    /** 对应 tags */
    private List<String> tags;

    /** 操作类型: SAVE, DELETE */
    private String operation;

    @Data
    public static class OptionDTO implements Serializable {
        private String key;  // "A"
        private String text; // "Java是一种编程语言"
    }

}
