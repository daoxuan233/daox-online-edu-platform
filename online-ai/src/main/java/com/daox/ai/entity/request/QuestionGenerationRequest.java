package com.daox.ai.entity.request;

import com.daox.ai.entity.CoursesVo;
import com.daox.ai.entity.ms.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 创建题目的请求参数
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGenerationRequest implements Serializable {
    /**
     * 课程信息
     */
    CoursesVo course;

    /**
     * 创建者ID（教师ID），用于记录题目是谁创建的
     */
    private String creatorId;

    /**
     * 期望生成的题型
     */
    private Question.QuestionType type;

    /**
     * 期望生成的题目难度
     */
    private Question.QuestionDifficulty difficulty;
}
