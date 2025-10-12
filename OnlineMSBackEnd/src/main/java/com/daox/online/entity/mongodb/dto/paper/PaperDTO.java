package com.daox.online.entity.mongodb.dto.paper;

import com.daox.online.entity.mongodb.Question;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PaperDTO {

    /**
     * 试卷ID，与MySQL中assessments表的ID保持一致，形成一对一关联。
     * 这样既能保证唯一性，也便于直接关联查询。
     */
    private String id;

    /**
     * 试卷标题
     */
    private String title;

    /**
     * 试卷描述 [选填]
     */
    private String description;

    /**
     * 试卷总分，由所有题目分数累加得到。
     * 可以在创建试卷时预先计算并存储，方便前端展示和后续统计。
     */
    private BigDecimal totalScore;

    /**
     * 试卷创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 试卷最后更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 试卷题目分组列表。
     * 允许将试卷题目按部分（如“第一部分：单选题”，“第二部分：简答题”）进行组织。
     * 如果试卷不分组，则该列表只包含一个元素。
     */
    private List<PaperDTO.PaperSection> sections;


    /**
     * 试卷题目分组内部类
     * 用于描述试卷的每个部分
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class PaperSection implements Serializable {
        /**
         * 分组标题，例如 "第一部分：选择题"
         */
        private String title;

        /**
         * 分组描述，可选，可对该部分题目做一些说明
         */
        private String description;

        /**
         * 该分组下的题目列表
         */
        private List<PaperDTO.PaperQuestion> questions;
    }


    /**
     * 试卷题目关联信息内部类
     * 定义了试卷中每一道题目的引用ID、分值和顺序
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class PaperQuestion implements Serializable {

        /**
         * 题目信息，关联MongoDB中questions集合
         * @see Question
         */
        private Question question;

        /**
         * 该题目在本试卷中的分值
         */
        private BigDecimal score;

        /**
         * 题目在本分组内的显示顺序，从0开始
         */
        private Integer orderIndex;
    }
}
