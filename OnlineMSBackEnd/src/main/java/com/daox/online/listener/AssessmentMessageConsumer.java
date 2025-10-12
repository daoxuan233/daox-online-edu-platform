package com.daox.online.listener;

import com.daox.online.entity.mongodb.Paper;
import com.daox.online.entity.mongodb.Question;
import com.daox.online.entity.mongodb.StudentAnswer;
import com.daox.online.entity.mongodb.dto.paper.PaperDetails;
import com.daox.online.entity.redis.AssessmentSession;
import com.daox.online.repository.mongodb.StudentAnswerRepository;
import com.daox.online.service.PaperService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // 自动注入所有 final 字段的构造函数
public class AssessmentMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentMessageConsumer.class);

    private final PaperService paperService;
    private final StudentAnswerRepository studentAnswerRepository;

    /**
     * 处理答卷提交消息
     *
     * @param session 评改所需数据
     */
    @RabbitListener(queues = "assessment.submission.queue")
    public void handleSubmissionMessage(AssessmentSession session) {
        String assessmentId = session.getAssessmentId();
        String userId = session.getUserId();
        logger.info("开始处理答卷, assessmentId: {}, userId: {}", assessmentId, userId);

        try {
            // 1. 获取评改所需的所有数据
            PaperDetails paperDetails = paperService.getPaperDetails(assessmentId);
            Paper paper = paperDetails.paper();
            Map<String, Question> questionsMap = paperDetails.questionsMap();
            Map<String, BigDecimal> questionScoreMap = paper.getSections().stream()
                    .flatMap(s -> s.getQuestions().stream())
                    .collect(Collectors.toMap(Paper.PaperQuestion::getQuestionId, Paper.PaperQuestion::getScore));

            // 2. 核心评改与数据转换逻辑
            AtomicHolder holder = gradeAnswers(session, questionsMap, questionScoreMap);

            // 3. 构建最终的 StudentAnswer 文档
            LocalDateTime now = LocalDateTime.now();
            StudentAnswer studentAnswer = new StudentAnswer()
                    .setAssessmentId(assessmentId)
                    .setUserId(userId)
                    .setCourseId(session.getCourseId())
                    .setStatus(holder.hasSubjective ? StudentAnswer.AnswerStatus.GRADING : StudentAnswer.AnswerStatus.GRADED)
                    .setSubmitTime(now)
                    .setCreatedAt(now)
                    .setUpdatedAt(now)
                    .setTimeSpentSeconds((int) Duration.between(session.getStartTime(), LocalDateTime.now()).toSeconds())
                    .setTotalScore(holder.totalScore)
                    .setAnswers(holder.gradedAnswers);

            // 4. 持久化到 MongoDB (幂等性核心)
            studentAnswerRepository.save(studentAnswer);
            logger.info("答卷处理成功并已存入MongoDB, assessmentId: {}, userId: {}", assessmentId, userId);

        } catch (DuplicateKeyException e) {
            // 这是预期的幂等性处理，不是错误，仅记录警告
            logger.warn("检测到重复消息，已忽略. assessmentId: {}, userId: {}. 这是正常现象。", assessmentId, userId);
        } catch (Exception e) {
            // 对于所有其他未知异常，记录严重错误，并抛出特定异常，让消息进入死信队列
            logger.error("处理答卷时发生未知严重错误, assessmentId: {}, userId: {}. Error: {}", assessmentId, userId, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("处理失败，消息将进入DLQ", e);
        }
    }

    /**
     * 评改答案并返回结果的辅助方法
     */
    private AtomicHolder gradeAnswers(AssessmentSession session, Map<String, Question> questionsMap, Map<String, BigDecimal> questionScoreMap) {
        AtomicHolder holder = new AtomicHolder();

        session.getAnswers().values().forEach(studentAns -> {
            String qId = studentAns.getQuestionId();
            Question question = questionsMap.get(qId);
            BigDecimal scoreInPaper = questionScoreMap.getOrDefault(qId, BigDecimal.ZERO);
            BigDecimal earnedScore = BigDecimal.ZERO;

            String gradingStatus = null;

            if (question != null) {
                // 判断是否是客观题
                boolean isObjective = EnumSet.of(Question.QuestionType.SINGLE_CHOICE, Question.QuestionType.MULTI_CHOICE, Question.QuestionType.TRUE_FALSE).contains(question.getType());

                if (isObjective) {
                    // 为客观题设置 gradingStatus
                    gradingStatus = StudentAnswer.GradingStatus.AUTO_GRADED;
                    if (isAnswerCorrect(question, studentAns.getResponse())) {
                        earnedScore = scoreInPaper;
                    }
                } else {
                    // 为主观题设置 gradingStatus
                    holder.hasSubjective = true; // 标记存在主观题
                    gradingStatus = StudentAnswer.GradingStatus.PENDING_MANUAL;
                }
            }

            holder.totalScore = holder.totalScore.add(earnedScore);
            // 创建 Answer 对象
            holder.gradedAnswers.add(new StudentAnswer.Answer(qId, studentAns.getResponse(), earnedScore, gradingStatus,null));
        });

        return holder;
    }

    /**
     * 答案比对逻辑
     */
    private boolean isAnswerCorrect(Question question, Object studentResponse) {
        if (studentResponse == null || question.getAnswer() == null) {
            return false;
        }
        try {
            return switch (question.getType()) {
                case SINGLE_CHOICE -> Objects.equals(question.getAnswer().toString(), studentResponse.toString());
                case TRUE_FALSE -> Objects.equals(question.getAnswer(), studentResponse);
                case MULTI_CHOICE -> {
                    // 检查类型以增强健壮性
                    if (!(question.getAnswer() instanceof Collection) || !(studentResponse instanceof Collection))
                        yield false;
                    // 使用 Collection 和 containsAll 进行无序比较
                    Collection<?> correctAns = (Collection<?>) question.getAnswer();
                    Collection<?> studentAns = (Collection<?>) studentResponse;
                    yield correctAns.size() == studentAns.size() && new HashSet<>(correctAns).containsAll(studentAns);
                }
                default -> false;
            };
        } catch (ClassCastException e) {
            logger.warn("答案比对时发生类型转换异常, questionId: {}. Error: {}", question.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * 用于在 lambda 表达式中传递多个值的内部辅助类
     */
    private static class AtomicHolder {
        BigDecimal totalScore = BigDecimal.ZERO;
        boolean hasSubjective = false;
        List<StudentAnswer.Answer> gradedAnswers = new ArrayList<>();
    }
}
