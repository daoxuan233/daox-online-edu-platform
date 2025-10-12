package com.daox.online.controller.teacher;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.mongodb.Question;
import com.daox.online.entity.views.responseVO.questions.QuestionVo;
import com.daox.online.service.QuestionsService;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师端 - 题目相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/questions")
public class QuestionsController {

    @Resource
    private QuestionsService questionsService;

    /**
     * 获取所有题目
     *
     * @return 课程列表
     */
    @GetMapping("")
    public RestBean<List<QuestionVo>> getQuestions() {
        List<QuestionVo> questions = questionsService.getQuestionsAll();
        if (questions == null || questions.isEmpty()) {
            return RestBean.failure(404, "没有找到题目");
        }
        return RestBean.success(questions);
    }

    /**
     * 获取课程所属的指定类型题目
     *
     * @param courseId 课程ID
     * @param type     题目类型
     * @return 题目列表
     */
    @GetMapping("/list")
    public RestBean<List<Question>> getQuestions(@RequestParam("courseId") String courseId, @RequestParam("type") String type) {
        List<Question> questions = questionsService.getQuestions(courseId, type);
        if (questions == null || questions.isEmpty()) {
            return RestBean.failure(404, "没有找到题目");
        }
        return RestBean.success(questions);
    }

    /**
     * 获取课程所属的题目
     *
     * @param courseId 课程ID
     * @return 题目列表
     */
    @GetMapping("/{courseId}")
    public RestBean<List<Question>> getCourseQuestions(@PathVariable("courseId") String courseId) {
        List<Question> questions = questionsService.getCourseQuestions(courseId);
        if (questions == null || questions.isEmpty()) {
            return RestBean.failure(404, "没有找到题目");
        }
        return RestBean.success(questions);
    }

    /**
     * 获取题型所属的题目
     *
     * @param type 题型
     * @return 题目列表
     */
    @GetMapping("/list/{type}")
    public RestBean<List<Question>> getTypeQuestions(@PathVariable("type") String type) {
        List<Question> questions = questionsService.getTypeQuestions(type);
        if (questions == null || questions.isEmpty()) {
            return RestBean.failure(404, "没有找到题目");
        }
        return RestBean.success(questions);
    }

    /**
     * 创建题目
     *
     * @param question 题目对象
     * @return 创建结果
     */
    @PostMapping("/create")
    public RestBean<Question> createQuestion(@RequestBody Question question, HttpServletRequest request) {
        String currentUserId = UserUtils.getCurrentUserId(request);
        if (currentUserId == null){
            log.warn("[createQuestion.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        question.setCreatorId(currentUserId);
        Question question1 = questionsService.createQuestion(question);
        if (question1 != null) {
            return RestBean.success(question1);
        }
        return RestBean.failure(500, "创建失败");
    }

    /**
     * 获取题目详情
     *
     * @param questionId 题目ID
     * @return 题目详情
     */
    @GetMapping("/detail")
    public RestBean<Question> getQuestionDetail(@RequestParam("questionId") String questionId) {
        Question question = questionsService.getQuestionDetail(questionId);
        if (question != null) {
            return RestBean.success(question);
        }
        return RestBean.failure(404, "没有找到题目");
    }

    /**
     * 更新题目
     *
     * @param question 题目对象
     * @return 更新结果
     */
    @PostMapping("/update")
    public RestBean<Question> updateQuestion(@RequestBody Question question) {
        Question question1 = questionsService.updateQuestion(question);
        if (question1 != null) {
            return RestBean.success(question1);
        }
        return RestBean.failure(500, "更新失败");
    }

    /**
     * 删除题目
     *
     * @param questionId 题目ID
     * @return 删除结果
     */
    @PostMapping("/delete")
    public RestBean<String> deleteQuestion(@RequestParam("questionId") String questionId) {
        boolean result = questionsService.deleteQuestion(questionId);
        if (result) {
            return RestBean.success("删除成功");
        }
        return RestBean.failure(500, "删除失败");
    }

}
