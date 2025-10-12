package com.daox.online.entity.mongodb;

import com.daox.online.entity.mysql.Assessments;
import com.daox.online.entity.mysql.Courses;
import com.daox.online.entity.mysql.Users;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷定义集合 (Collection: papers)
 * 存储每一次测评活动的具体试卷构成，是对MySQL中assessments记录的补充和扩展。
 * 它定义了一份试卷包含哪些题目、各题分值、总分以及题目分组等信息。
 */
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "papers")
public class Paper implements Serializable {

    /**
     * 试卷ID，建议与MySQL中assessments表的ID保持一致，形成一对一关联。
     * 这样既能保证唯一性，也便于直接关联查询。
     */
    @Id
    private String id;

    /**
     * 冗余字段：课程ID，关联MySQL
     * @see Courses#getId()
     * 便于在MongoDB中直接按课程检索试卷，提高查询效率。
     */
    @Field("course_id")
    @Indexed
    private String courseId;

    /**
     * 冗余字段：创建者ID，关联MySQL
     * @see Users#getId()
     * 创建者ID，便于后续查询和统计。
     */
    @Field("creator_id")
    @Indexed
    private String creatorId;

    /**
     * 试卷标题 [选填]
     */
    @Field("title")
    private String title;

    /**
     * 试卷描述 [选填]
     */
    @Field("description")
    private String description;

    /**
     * 测评id，关联MySQL [必填]
     * @see Assessments#getId()
     * 测评ID，便于后续关联查询和统计。
     */
    @Field("assessment_id")
    private String assessmentId;


    /**
     * 试卷总分，由所有题目分数累加得到。
     * 可以在创建试卷时预先计算并存储，方便前端展示和后续统计。
     */
    @Field("total_score")
    private BigDecimal totalScore;

    /**
     * 试卷创建时间
     */
    @Field("created_at")
    private LocalDateTime createdAt;

    /**
     * 试卷最后更新时间
     */
    @Field("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 试卷题目分组列表。
     * 允许将试卷题目按部分（如“第一部分：单选题”，“第二部分：简答题”）进行组织。
     * 如果试卷不分组，则该列表只包含一个元素。
     */
    @Field("sections")
    private List<PaperSection> sections;


    /**
     * 试卷题目分组内部类
     * 用于描述试卷的每个部分
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaperSection implements Serializable {
        /**
         * 分组标题，例如 "第一部分：选择题"
         */
        @Field("title")
        private String title;

        /**
         * 分组描述，可选，可对该部分题目做一些说明
         */
        @Field("description")
        private String description;

        /**
         * 该分组下的题目列表
         */
        @Field("questions")
        private List<PaperQuestion> questions;
    }


    /**
     * 试卷题目关联信息内部类
     * 定义了试卷中每一道题目的引用ID、分值和顺序
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaperQuestion implements Serializable {

        /**
         * 题目ID，关联MongoDB中questions集合的_id
         */
        @Field("question_id")
        private String questionId;

        /**
         * 该题目在本试卷中的分值
         */
        @Field("score")
        private BigDecimal score;

        /**
         * 题目在本分组内的显示顺序，从0开始
         */
        @Field("order_index")
        private Integer orderIndex;
    }
}
