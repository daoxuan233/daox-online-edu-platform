package com.daox.online.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
            BigDecimal scoreInPaper = Optional.ofNullable(questionScoreMap.get(qId)).orElse(BigDecimal.ZERO);
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
     * <p>
     * 兼容策略：
     * 1) 兼容前端将多选题答案以 JSON 字符串提交的场景；
     * 2) 兼容判断题以 Boolean / 字符串（true/false、正确/错误）提交的场景；
     * 3) 兼容题库中历史数据存在字符串或集合两种答案存储形态。
     * </p>
     *
     * @param question 题目实体
     * @param studentResponse 学生原始答案
     * @return 是否判定正确
     */
    private boolean isAnswerCorrect(Question question, Object studentResponse) {
        if (studentResponse == null || question.getAnswer() == null) {
            return false;
        }
        try {
            return switch (question.getType()) {
                case SINGLE_CHOICE -> Objects.equals(extractSingleChoiceToken(question.getAnswer()), extractSingleChoiceToken(studentResponse));
                case TRUE_FALSE -> {
                    Boolean correct = parseBooleanAnswer(question.getAnswer());
                    Boolean student = parseBooleanAnswer(studentResponse);
                    if (correct == null || student == null) {
                        yield Objects.equals(normalizeToken(question.getAnswer()), normalizeToken(studentResponse));
                    }
                    yield Objects.equals(correct, student);
                }
                case MULTI_CHOICE -> {
                    Set<String> correctAns = parseOptionSet(question.getAnswer());
                    Set<String> studentAns = parseOptionSet(studentResponse);
                    yield !correctAns.isEmpty() && correctAns.equals(studentAns);
                }
                default -> false;
            };
        } catch (Exception e) {
            logger.warn("答案比对时发生异常, questionId: {}. Error: {}", question.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * 将输入答案统一规范化为字符串令牌。
     * <p>
     * 规范化规则：
     * 1) null -> 空字符串；
     * 2) 去除首尾空白；
     * 3) 统一转大写，降低大小写差异带来的误判。
     * </p>
     *
     * @param value 原始值
     * @return 规范化后的字符串
     */
    private String normalizeToken(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString().trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 解析判断题答案。
     * <p>
     * 支持输入：
     * 1) Boolean；
     * 2) 字符串 true/false；
     * 3) 中文“正确/错误、对/错”；
     * 4) 选项字母 A/B（约定 A=正确，B=错误，兼容历史题库录入习惯）。
     * </p>
     *
     * @param value 原始答案
     * @return 可识别时返回 Boolean，不可识别返回 null
     */
    private Boolean parseBooleanAnswer(Object value) {
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        String normalized = extractSingleChoiceToken(value);
        if (normalized.isEmpty()) {
            return null;
        }
        if (Set.of("TRUE", "1", "正确", "对", "A").contains(normalized)) {
            return true;
        }
        if (Set.of("FALSE", "0", "错误", "错", "B").contains(normalized)) {
            return false;
        }
        return null;
    }

    /**
     * 将多选答案解析为标准集合。
     * <p>
     * 支持输入：
     * 1) Collection（后端已反序列化）；
     * 2) JSON 数组字符串，如 ["A","B"]；
     * 3) 逗号分隔字符串，如 A,B。
     * </p>
     *
     * @param value 原始答案
     * @return 规范化后的去重集合
     */
    private Set<String> parseOptionSet(Object value) {
        if (value == null) {
            return Collections.emptySet();
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::normalizeToken)
                    .map(this::convertNumericIndexToken)
                    .filter(token -> !token.isBlank())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        if (value instanceof String raw) {
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) {
                return Collections.emptySet();
            }
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                try {
                    List<Object> parsed = OBJECT_MAPPER.readValue(trimmed, new TypeReference<List<Object>>() {
                    });
                    return parsed.stream()
                            .map(this::normalizeToken)
                            .map(this::convertNumericIndexToken)
                            .filter(token -> !token.isBlank())
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                } catch (JsonProcessingException e) {
                    logger.warn("多选答案JSON解析失败，将尝试按逗号分隔解析。raw={}, error={}", trimmed, e.getMessage());
                }
            }
            return Arrays.stream(trimmed.split(","))
                    .map(this::normalizeToken)
                    .map(this::convertNumericIndexToken)
                    .filter(token -> !token.isBlank())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return Set.of(convertNumericIndexToken(normalizeToken(value)));
    }

    /**
     * 提取单选题/判断题可比较的标准令牌。
     * <p>
     * 兼容策略：
     * 1) 兼容历史数据把单选答案存成数组（如 ["A"] / [0]）；
     * 2) 兼容前端或导入流程把答案存成 JSON 数组字符串；
     * 3) 兼容旧题库把选项保存为数字索引（0,1,2...），统一换算为 A,B,C...；
     * 4) 兼容逗号拼接字符串，默认取第一个有效值作为单选答案。
     * </p>
     *
     * @param value 原始答案值
     * @return 标准化后的单值令牌
     */
    private String extractSingleChoiceToken(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Collection<?> collection) {
            if (collection.isEmpty()) {
                return "";
            }
            Object first = collection.iterator().next();
            return convertNumericIndexToken(normalizeToken(first));
        }
        if (value instanceof String raw) {
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) {
                return "";
            }
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                try {
                    List<Object> parsed = OBJECT_MAPPER.readValue(trimmed, new TypeReference<List<Object>>() {
                    });
                    if (parsed.isEmpty()) {
                        return "";
                    }
                    return convertNumericIndexToken(normalizeToken(parsed.get(0)));
                } catch (JsonProcessingException e) {
                    logger.warn("单选答案JSON解析失败，将降级按普通字符串处理。raw={}, error={}", trimmed, e.getMessage());
                }
            }
            if (trimmed.contains(",")) {
                String firstToken = trimmed.split(",")[0];
                return convertNumericIndexToken(normalizeToken(firstToken));
            }
        }
        return convertNumericIndexToken(normalizeToken(value));
    }

    /**
     * 将数字索引令牌转换为选项字母令牌。
     * <p>
     * 转换规则：
     * 1) 当令牌为非负整数且在 [0,25] 内时，映射为 A~Z；
     * 2) 其他情况保持原值不变。
     * </p>
     *
     * @param token 标准化后的原始令牌
     * @return 转换后的令牌
     */
    private String convertNumericIndexToken(String token) {
        if (token == null || token.isBlank()) {
            return "";
        }
        if (!token.matches("\\d+")) {
            return token;
        }
        int index = Integer.parseInt(token);
        if (index < 0 || index > 25) {
            return token;
        }
        return String.valueOf((char) ('A' + index));
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
