package com.daox.online.entity.mongodb;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 学生答卷集合 (Collection: student_answers)
 * 用于完整存储每个学生的每一次测评提交记录
 */
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "student_answers")
@CompoundIndex(name = "assessment_user_idx", def = "{'assessment_id': 1, 'user_id': 1}", unique = true)
public class StudentAnswer implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 答卷记录的唯一主键
     */
    @Id
    private String id;

    /**
     * 关联MySQL assessments.id，标识所属测评
     */
    @Field("assessment_id")
    private String assessmentId;

    /**
     * 关联MySQL users.id，标识答题学生
     */
    @Field("user_id")
    private String userId;

    /**
     * 冗余字段，关联courses.id，便于按课程统计分析
     */
    @Field("course_id")
    @Indexed
    private String courseId;

    /**
     * 答卷状态: in_progress (答题中), submitted (已提交), grading (批阅中), graded (已批阅), review(需复查)
     */
    @Field("status")
    @Indexed
    private String status;

    /**
     * 试卷提交时间
     */
    @Field("submit_time")
    @Indexed
    private LocalDateTime submitTime;

    /**
     * 文档创建时间
     */
    @Field("created_at")
    @Indexed
    private LocalDateTime createdAt;

    /**
     * 文档最后更新时间（例如教师批改或申诉后修改分数的时间）
     */
    @Field("updated_at")
    @Indexed
    private LocalDateTime updatedAt;

    /**
     * 学生本次答题花费的总秒数
     */
    @Field("time_spent_seconds")
    private Integer timeSpentSeconds;

    /**
     * 最终得分，由系统自动或教师批阅后计算得出
     */
    @Field("total_score")
    @Indexed
    private BigDecimal totalScore;

    /**
     * 学生答案列表。每个对象包含question_id, response (学生答案) 和score (该题得分)
     */
    @Field("answers")
    private List<Answer> answers;

    /**
     * 学生答案内部类
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Answer implements Serializable {
        /**
         * 题目ID
         */
        @Field("question_id")
        private String questionId;

        /**
         * 学生答案
         */
        @Field("response")
        private Object response;

        /**
         * 该题得分
         */
        @Field("score")
        private BigDecimal score;

        /**
         * 单题的批改状态: auto_graded (机评), pending_manual (待人工批改), manually_graded (已人工批改)
         */
        @Field("grading_status")
        private String gradingStatus;

        /**
         * [新增] 教师评语
         */
        @Field("comment")
        private String comment;
    }

    /**
     * 单题批改状态枚举常量
     */
    @Data
    @NoArgsConstructor
    public static class GradingStatus implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        /**
         * 机评
         */
        public static final String AUTO_GRADED = "auto_graded";
        /**
         * 待人工批改
         */
        public static final String PENDING_MANUAL = "pending_manual";
        /**
         * 已人工批改
         */
        public static final String MANUALLY_GRADED = "manually_graded";
    }

    /**
     * 答卷状态枚举常量
     */
    public static class AnswerStatus {
        /**
         * 正在填写
         */
        public static final String IN_PROGRESS = "in_progress";
        /**
         * 已提交
         */
        public static final String SUBMITTED = "submitted";
        /**
         * 评分中 - 批阅中
         */
        public static final String GRADING = "grading";
        /**
         * 已评分
         */
        public static final String GRADED = "graded";
        /**
         * 复查 - 需复查
         */
        public static final String REVIEW = "review";
    }
}