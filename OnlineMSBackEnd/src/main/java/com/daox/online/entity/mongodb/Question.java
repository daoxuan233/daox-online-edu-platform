package com.daox.online.entity.mongodb;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * 题库集合 (Collection: questions)
 * 作为系统的中央题库，存储所有课程的各类试题
 */
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "questions")
public class Question implements Serializable {

    /**
     * 文档唯一主键，自动生成
     */
    @Id
    private String id;

    /**
     * 关联MySQL courses.id，标识题目所属课程
     */
    @Field("course_id")
    @Indexed
    private String courseId;

    /**
     * 关联MySQL users.id，记录题目创建者（教师）
     */
    @Field("creator_id")
    @Indexed
    private String creatorId;

    /**
     * 题型枚举
     *
     * @see QuestionType
     */
    @Field("type")
    @Indexed
    private QuestionType type;

    /**
     * 题目难度，枚举
     *
     * @see QuestionDifficulty
     */
    @Field("difficulty")
    @Indexed
    private QuestionDifficulty difficulty;

    /**
     * 题干，即题目的主要问题描述
     */
    @Field("stem")
    private String stem;

    /**
     * 选项数组，仅用于选择题。对象包含key (如"A") 和text (选项内容)
     */
    @Field("options")
    private List<QuestionOption> options;

    /**
     * 题目的正确答案。单选题为String，多选题为Array<String>，判断题为Boolean，简答题为String或Array<String>
     */
    @Field("answer")
    private Object answer;

    /**
     * 答案的详细解析
     */
    @Field("analysis")
    private String analysis;

    /**
     * 知识点标签数组，用于题目检索和分类
     */
    @Field("tags")
    @Indexed
    private List<String> tags;

    /**
     * 逻辑删除标志，默认为false
     */
    @Field("is_deleted")
    @Indexed
    private Boolean isDeleted = false;

    /**
     * 文档创建时间
     */
    @Field("created_at")
    @Indexed
    private Date createdAt;

    /**
     * 文档最后更新时间
     */
    @Field("updated_at")
    private Date updatedAt;

    /**
     * 题目选项内部类
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionOption implements Serializable {
        /**
         * 选项键值，如"A", "B", "C", "D"
         */
        private String key;

        /**
         * 选项内容
         */
        private String text;
    }

    /**
     * 类型枚举
     */
    public enum QuestionType {
        /**
         * 单选题
         */
        SINGLE_CHOICE,
        /**
         * 多选题
         */
        MULTI_CHOICE,
        /**
         * 判断题
         */
        TRUE_FALSE,
        /**
         * 简答题
         */
        SHORT_ANSWER,   // 简答题
        /**
         * 填空题
         */
        FILL_IN_BLANKS,
        /**
         * 程序题
         */
        PROGRAMMING
    }

    /**
     * 难度枚举
     */
    public enum QuestionDifficulty {
        /**
         * 简单
         */
        EASY,
        /**
         * 中等
         */
        MEDIUM,
        /**
         * 困难
         */
        HARD
    }

    /**
     * 定义一个静态集合，用于快速判断一个题型是否为主观题
     */
    public static final EnumSet<QuestionType> SUBJECTIVE_TYPES = EnumSet.of(
            QuestionType.SHORT_ANSWER,
            QuestionType.FILL_IN_BLANKS,
            QuestionType.PROGRAMMING
    );


}