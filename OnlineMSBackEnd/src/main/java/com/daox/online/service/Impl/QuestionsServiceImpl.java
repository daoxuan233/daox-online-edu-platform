package com.daox.online.service.Impl;

import com.daox.online.entity.mongodb.Question;
import com.daox.online.entity.views.responseVO.questions.QuestionVo;
import com.daox.online.mapper.CoursesMapper;
import com.daox.online.repository.mongodb.QuestionRepository;
import com.daox.online.service.QuestionsService;
import com.daox.online.service.UsersService;
import com.daox.online.uilts.DateUtils;
import com.daox.online.uilts.constant.Const;
import jakarta.annotation.Resource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QuestionsServiceImpl implements QuestionsService {

    @Resource
    private QuestionRepository questionRepository;

    @Resource
    private CoursesMapper coursesMapper;

    @Resource
    private UsersService usersService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 获取所有题目
     *
     * @return 课程列表
     */
    @Override
    public List<QuestionVo> getQuestionsAll() {
        // 1. 获取所有未删除的题目
        List<Question> allQuestions = questionRepository.findAllByIsDeletedFalse();
        if (allQuestions.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 按 courseId 分组
        Map<String, List<Question>> questionsByCourse = allQuestions.stream()
                .collect(Collectors.groupingBy(Question::getCourseId));
        // 3. 构建 QuestionVo 列表
        return questionsByCourse.entrySet().stream()
                .map(entry -> {
                    String courseId = entry.getKey();
                    List<Question> questions = entry.getValue();
                    // 4. 查询课程标题
                    String courseTitle = coursesMapper.getCourseById(courseId).getTitle();

                    return new QuestionVo()
                            .setId(courseId)
                            .setTitle(courseTitle)
                            .setQuestion(questions);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取题目列表
     *
     * @param courseId 课程ID
     * @param type     题目类型
     * @return 题目列表
     */
    @Override
    public List<Question> getQuestions(String courseId, String type) {
        // 参数验证
        if (courseId == null || courseId.isEmpty() || type == null || type.isEmpty()) {
            log.warn("[getQuestions.method]参数错误: courseId={}, type={}", courseId, type);
            return Collections.emptyList();
        }

        //校验课程是否存在
        if (!coursesMapper.checkCourseExists(courseId)) {
            log.warn("[getQuestions.method]课程不存在: courseId={}", courseId);
            return Collections.emptyList();
        }

        return questionRepository.findByCourseIdAndTypeAndIsDeletedFalse(courseId, type);

    }

    /**
     * 获取课程所属的题目
     *
     * @param courseId 课程ID
     * @return 题目列表
     */
    @Override
    public List<Question> getCourseQuestions(String courseId) {
        // 定义缓存键
        String cacheKey = Const.QUESTION_COURSE + courseId;

        // 1. 尝试从缓存中获取
        Object cachedData = redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            try {
                // 如果缓存命中，则反序列化并返回
                List<Question> questions = objectMapper.convertValue(cachedData, new TypeReference<List<Question>>() {});
                log.info("[getCourseQuestions.method] 缓存命中，courseId: {}", courseId);
                return questions;
            } catch (Exception e) {
                log.error("[getCourseQuestions.method] 反序列化缓存失败，courseId: {}", courseId, e);
            }
        }
        log.info("[getCourseQuestions.method] 缓存未命中，查询数据库，courseId: {}", courseId);
        // 2. 缓存未命中，查询数据库
        //校验课程是否存在
        if (!coursesMapper.checkCourseExists(courseId)) {
            log.warn("[getCourseQuestions.method]课程不存在: courseId={}", courseId);
            return Collections.emptyList();
        }
        List<Question> questions = questionRepository.findByCourseIdAndIsDeletedFalse(courseId);

        // 3. 如果数据库查询结果非空，则存入缓存
        if (questions != null && !questions.isEmpty()) {
            try {
                // 设置1小时的缓存过期时间
                redisTemplate.opsForValue().set(cacheKey, questions, 1, TimeUnit.HOURS);
                log.info("[getCourseQuestions.method] 查询结果已存入缓存，courseId: {}", courseId);
            } catch (Exception e) {
                log.error("[getCourseQuestions.method] 缓存题目列表失败，courseId: {}", courseId, e);
            }
        }

        return questions;
    }

    /**
     * 获取题型所属的题目
     *
     * @param type 题目类型
     * @return 题目列表
     */
    @Override
    public List<Question> getTypeQuestions(String type) {
        // 检查类型是否存在
        if (type == null || type.isEmpty()) {
            log.warn("[getTypeQuestions.method]参数错误: type={}", type);
            return Collections.emptyList();
        }

        // 校验题目类型是否合法
        try {
            Question.QuestionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[getTypeQuestions.method]无效的题目类型: type={}", type);
            return Collections.emptyList();
        }

        return questionRepository.findByTypeAndIsDeletedFalse(type);

    }

    /**
     * 创建题目
     *
     * @param question 题目
     * @return 创建结果
     */
    @Override
    public Question createQuestion(Question question) {
        // 参数校验
        if (question == null || question.getCourseId() == null || question.getCreatorId() == null) {
            log.warn("[createQuestion.method]参数错误: question={}", question);
            throw new IllegalArgumentException("题目信息不完整");
        }

        // 校验课程是否存在
        if (!coursesMapper.checkCourseExists(question.getCourseId())) {
            log.warn("[createQuestion.method]课程不存在: courseId={}", question.getCourseId());
            throw new IllegalArgumentException("课程不存在");
        }

        // 校验创建者是否为教师
        if (!usersService.isTeacher(question.getCreatorId())) {
            log.warn("[createQuestion.method]用户不是教师: userId={}", question.getCreatorId());
            throw new IllegalArgumentException("用户不是教师");
        }

        if (question.getCreatedAt() == null){
            question.setCreatedAt(DateUtils.convertToDate(LocalDateTime.now()));
        }

        try {
            Question insertedQuestion = questionRepository.insert(question);
            log.info("[createQuestion.method]创建题目成功: question={}", insertedQuestion);
            return insertedQuestion;
        } catch (Exception e) {
            log.error("[createQuestion.method]创建题目失败: question={}", question, e);
            throw new RuntimeException("创建题目失败", e);
        }
    }

    /**
     * 获取题目详情
     *
     * @param questionId 题目ID
     * @return 题目详情
     */
    @Override
    public Question getQuestionDetail(String questionId) {
        if (questionId == null || questionId.isEmpty()) {
            log.warn("[getQuestionDetail.method]参数错误: questionId={}", questionId);
            return null;
        }
        return questionRepository.findById(questionId).orElse(null);
    }

    /**
     * 更新题目
     *
     * @param question 题目
     * @return 更新结果
     */
    @Override
    public Question updateQuestion(Question question) {
        if (question == null || question.getId() == null) {
            log.warn("[updateQuestion.method]参数错误: question={}", question);
            return null;
        }
        return questionRepository.save(question);
    }

    /**
     * 删除题目
     *
     * @param questionId 题目ID
     * @return 删除结果
     */
    @Override
    public boolean deleteQuestion(String questionId) {
        if (questionId == null || questionId.isEmpty()) {
            log.warn("[deleteQuestion.method]参数错误: questionId={}", questionId);
            return false;
        }
        Question question = questionRepository.findById(questionId).orElse(null);
        if (question == null) {
            log.warn("[deleteQuestion.method]题目不存在: questionId={}", questionId);
            return false;
        }
        question.setIsDeleted(true);
        questionRepository.save(question);
        log.info("[deleteQuestion.method]删除题目成功: questionId={}", questionId);
        return true;
    }

    /**
     * 搜索题目 - 三者任一或则三者皆有
     *
     * @param keyword    关键字
     * @param tags       标签
     * @param difficulty 难度级别
     * @return 搜索结果
     */
    @Override
    public List<Question> searchQuestions(String keyword, String tags, String difficulty) {
        if (keyword == null && tags == null && difficulty == null) {
            log.warn("[searchQuestions.method]参数错误: ");
            return Collections.emptyList();
        }
        // 只有关键词
        if (keyword != null && !keyword.isEmpty() && tags != null && !tags.isEmpty() && difficulty != null && !difficulty.isEmpty()) {
            return questionRepository.findByStemContainingIgnoreCaseAndIsDeletedFalse(keyword);
        }
        // 关键词和标签
        if (keyword != null && !keyword.isEmpty() && tags != null && !tags.isEmpty()) {
            return questionRepository.findByStemContainingIgnoreCaseAndTagsRegexAndIsDeletedFalse(keyword, tags);
        }
        // 关键词、标签、难度
        return questionRepository.findByStemAndTagsAndDifficultyAndNotDeleted(keyword, tags, difficulty);
    }


}
