package com.daox.online.entity.redis;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@AllArgsConstructor
@Accessors(chain = true)
@NoArgsConstructor
public class AssessmentSession implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 测评ID (关联 assessments.id)
     */
    private String assessmentId;

    /**
     * 学生ID (关联 users.id)
     */
    private String userId;

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 会话创建时间，即学生开始考试的时间
     */
    private LocalDateTime startTime;

    /**
     * 答案列表，使用线程安全的 Map 结构，方便快速更新
     * Key: questionId, Value: AnswerPayload
     */
    private Map<String, AnswerPayload> answers = new ConcurrentHashMap<>();

    /**
     * 会话状态
     */
    private String status;

    /**
     * 答案载荷内部类
     */
    @Data
    @NoArgsConstructor
    public static class AnswerPayload implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 题目ID
         */
        private String questionId;

        /**
         * 学生答案 (使用 Object 以支持不同题型)
         */
        private Object response;

        /**
         * 答题时间
         */
        private LocalDateTime answerTime;
    }

    /**
     * 会话状态常量
     */
    public static class Status {
        /**
         * 答题中
         */
        public static final String IN_PROGRESS = "in_progress";
        /**
         * 正在提交 (用于防重锁)
         */
        public static final String SUBMITTING = "submitting";
        /**
         * 已提交
         */
        public static final String SUBMITTED = "submitted";
    }

}
