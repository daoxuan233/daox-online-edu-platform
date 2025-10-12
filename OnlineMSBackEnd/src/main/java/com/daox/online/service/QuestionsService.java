package com.daox.online.service;

import com.daox.online.entity.mongodb.Question;
import com.daox.online.entity.views.responseVO.questions.QuestionVo;

import java.util.List;

/**
 * 题库管理功能 Service
 */
public interface QuestionsService {

    /**
     * 获取所有题目
     * @return 课程列表
     */
    List<QuestionVo> getQuestionsAll();

    /**
     * 获取课程所属的指定类型题目
     *
     * @param courseId 课程ID
     * @param type     题目类型
     * @return 题目列表
     */
    List<Question> getQuestions(String courseId, String type);

    /**
     * 获取课程所属的题目
     *
     * @param courseId 课程ID
     * @return 题目列表
     */
    List<Question> getCourseQuestions(String courseId);

    /**
     * 获取题型所属的题目
     *
     * @param type     题目类型
     * @return 题目列表
     */
    List<Question> getTypeQuestions(String type);

    /**
     * 创建题目
     *
     * @param question 题目
     * @return 创建结果
     */
    Question createQuestion(Question question);

    /**
     * 获取题目详情
     *
     * @param questionId 题目ID
     * @return 题目详情
     */
    Question getQuestionDetail(String questionId);

    /**
     * 更新题目
     *
     * @param question 题目
     * @return 更新结果
     */
    Question updateQuestion(Question question);

    /**
     * 删除题目
     *
     * @param questionId 题目ID
     * @return 删除结果
     */
    boolean deleteQuestion(String questionId);

    /**
     * 搜索题目 - 三者任一或则三者皆有
     *
     * @param keyword    关键字
     * @param tags       标签
     * @param difficulty 难度级别
     * @return 搜索结果
     */
    List<Question> searchQuestions(String keyword, String tags, String difficulty);
}
