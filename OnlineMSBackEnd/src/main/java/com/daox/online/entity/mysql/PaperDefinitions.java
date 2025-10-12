package com.daox.online.entity.mysql;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 试卷定义表<br />
 * 作为测评活动与题库（MongoDB）之间的桥梁<br />
 * 此表定义了一份具体试卷的构成，即某次测评包含了哪些题目以及每道题目的分值<br />
 * TableName:  paper_definitions
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PaperDefinitions implements Serializable {

    /**
     * 关联ID
     */
    private String id;
    /**
     * 测评ID
     */
    private String assessmentId;
    /**
     * 题目ID 对应MongoDB中 `questions` 集合
     */
    private String questionId;
    /**
     * 分值
     */
    private BigDecimal score;
    /**
     * 题目顺序
     */
    private Integer orderIndex;

}
