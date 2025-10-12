package com.daox.online.service.Impl;

import com.daox.online.controller.exception.BusinessException;
import com.daox.online.controller.exception.ResourceNotFoundException;
import com.daox.online.entity.dto.teacher.marking.*;
import com.daox.online.entity.mongodb.Paper;
import com.daox.online.entity.mongodb.Question;
import com.daox.online.entity.mongodb.StudentAnswer;
import com.daox.online.entity.mysql.Assessments;
import com.daox.online.entity.mysql.Courses;
import com.daox.online.entity.mysql.Users;
import com.daox.online.mapper.AssessmentsMapper;
import com.daox.online.mapper.CourseMapper;
import com.daox.online.mapper.UserMapper;
import com.daox.online.repository.mongodb.PaperRepository;
import com.daox.online.repository.mongodb.QuestionRepository;
import com.daox.online.repository.mongodb.StudentAnswerRepository;
import com.mongodb.client.result.UpdateResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor // 自动注入所有 final 字段的构造函数
public class TeacherGradingService {
    private final MongoTemplate mongoTemplate;
    private final StudentAnswerRepository studentAnswerRepository;
    private final PaperRepository paperRepository;
    private final QuestionRepository questionRepository;
    private final AssessmentsMapper assessmentsMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    /**
     * 获取指定教师的待批阅任务列表
     * @param teacherId 教师ID
     * @return 阅卷任务 DTO 列表
     */
    public List<GradingTaskDTO> getGradingTasks(String teacherId) {
        log.info("[getGradingTasks.method]获取教师 {} 的待阅卷任务列表", teacherId);
        // --- 步骤 1: 从 MongoDB 聚合查询待批阅份数 ---
        List<PendingCountResult> pendingCounts = getPendingCountsFromMongo(teacherId);
        if (pendingCounts.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> assessmentIds = pendingCounts.stream().map(PendingCountResult::getAssessmentId).collect(Collectors.toList());

        // --- 步骤 2: 从 MySQL 批量查询元数据 ---
        Map<String, Assessments> assessmentsMap = assessmentsMapper.findByIdIn(assessmentIds).stream()
                .collect(Collectors.toMap(Assessments::getId, Function.identity()));

        List<String> courseIds = assessmentsMap.values().stream().map(Assessments::getCourseId).distinct().collect(Collectors.toList());
        Map<String, Courses> coursesMap = courseMapper.findByIdIn(courseIds).stream()
                .collect(Collectors.toMap(Courses::getId, Function.identity()));
        log.info("[getGradingTasks.method] 获取教师 {} 的待阅卷任务列表，关联课程信息：{}", teacherId, coursesMap);
        // --- 步骤 3: 从 MongoDB 批量计算主观题数量 ---
        Map<String, Integer> subjectiveQuestionCounts = getSubjectiveQuestionCounts(assessmentIds);

        // --- 步骤 4: 在 Java 中组装最终 DTO ---
        return pendingCounts.stream().map(pendingCount -> {
            Assessments assessment = assessmentsMap.get(pendingCount.getAssessmentId());
            if (assessment == null) return null; // 数据不一致，跳过

            Courses course = coursesMap.get(assessment.getCourseId());
            String courseName = (course != null) ? course.getTitle() : "未知课程";

            Integer subjectiveCount = subjectiveQuestionCounts.getOrDefault(assessment.getId(), 0);

            return new GradingTaskDTO(
                    assessment.getId(),
                    assessment.getTitle(),
                    courseName,
                    pendingCount.getCount(),
                    subjectiveCount
            );
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 获取单个测评的批阅详情（主观题列表及进度）
     * @param assessmentId 测评ID
     * @return 阅卷详情 DTO
     */
    public GradingDetailDTO getGradingTaskDetail(String assessmentId) {
        // 1. 获取测评基本信息 (来自 MySQL)
        Assessments assessmentInfo = assessmentsMapper.getAssessmentById(assessmentId);
        if (assessmentInfo == null) {
            throw new ResourceNotFoundException("测评不存在: " + assessmentId);
        }

        // 2. 获取试卷定义，并筛选出所有主观题 (来自 MongoDB)
        Paper paper = paperRepository.findByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("与该测评关联的试卷不存在: " + assessmentId));

        List<String> allQuestionIds = paper.getSections().stream()
                .flatMap(s -> s.getQuestions().stream().map(Paper.PaperQuestion::getQuestionId))
                .collect(Collectors.toList());

        Map<String, Question> questionsMap = questionRepository.findAllById(allQuestionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        List<Paper.PaperQuestion> subjectivePaperQuestions = paper.getSections().stream()
                .flatMap(s -> s.getQuestions().stream())
                .filter(pq -> {
                    Question q = questionsMap.get(pq.getQuestionId());
                    return q != null && Question.SUBJECTIVE_TYPES.contains(q.getType());
                })
                .collect(Collectors.toList());

        if (subjectivePaperQuestions.isEmpty()) {
            // 如果该试卷没有主观题，直接返回空列表
            return new GradingDetailDTO(assessmentInfo, Collections.emptyList());
        }

        // 3. [核心] 一次聚合查询，计算所有主观题的批阅进度
        Map<String, GradingProgress> progressMap = getGradingProgressFromMongo(assessmentId);

        // 4. 组装最终的 DTO
        List<GradingDetailDTO.SubjectiveQuestionProgress> progressList = subjectivePaperQuestions.stream()
                .map(pq -> {
                    Question question = questionsMap.get(pq.getQuestionId());
                    GradingProgress progress = progressMap.getOrDefault(pq.getQuestionId(), new GradingProgress(0L, 0L));

                    return new GradingDetailDTO.SubjectiveQuestionProgress(
                            pq.getQuestionId(),
                            question.getStem(),
                            pq.getScore(),
                            progress.gradedCount(),
                            progress.totalCount()
                    );
                })
                .collect(Collectors.toList());

        return new GradingDetailDTO(assessmentInfo, progressList);
    }

    /**
     * [新增] 分页获取某道主观题的所有学生答案
     * @param assessmentId 测评 ID
     * @param questionId   题目 ID
     * @param pageable     Spring 分页对象
     * @return 包含学生答案片段 DTO 的分页对象
     */
    public Page<StudentAnswerSnippetDTO> getStudentAnswersForQuestion(String assessmentId, String questionId, Pageable pageable) {
        // --- 步骤 1: (MongoDB) 构建分页查询 ---
        Query query = new Query(
                Criteria.where("assessment_id").is(assessmentId)
                        .and("status").is("grading") // 只查询状态为 grading 的答卷
                        .and("answers.question_id").is(questionId) // 确保答案数组中包含这道题
        ).with(pageable);
        log.info("[getStudentAnswersForQuestion.method] 获取测评 {} 的主观题 {} 的学生答案列表，分页参数：{}", assessmentId, questionId, pageable);
        // --- 步骤 2: (MongoDB) 执行分页查询 ---
        List<StudentAnswer> studentAnswersPage = mongoTemplate.find(query, StudentAnswer.class);

        // 为了获得总数，我们需要执行一次 count 查询
        long totalCount = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), StudentAnswer.class);
        Page<StudentAnswer> pageResult = PageableExecutionUtils.getPage(studentAnswersPage, pageable, () -> totalCount);

        if (!pageResult.hasContent()) {
            return Page.empty(pageable);
        }

        // --- 步骤 3: (Java) 提取 userId 列表 ---
        List<String> userIds = pageResult.getContent().stream()
                .map(StudentAnswer::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // --- 步骤 4: (MySQL) 批量查询学生信息 ---
        Map<String, Users> usersMap = userMapper.findByIdIn(userIds).stream()
                .collect(Collectors.toMap(Users::getId, Function.identity()));

        // --- 步骤 5: (Java) 组装 DTO ---
        List<StudentAnswerSnippetDTO> dtos = pageResult.getContent().stream().map(sa -> {
            Users user = usersMap.get(sa.getUserId());
            String studentName = (user != null) ? user.getNickname() : "未知学生"; // 使用 username 或 name
            String identifier = (user != null) ? user.getIdentifier() : "unknown";

            // 从答案数组中找到对应题目的答案
            StudentAnswer.Answer answer = sa.getAnswers().stream()
                    .filter(a -> questionId.equals(a.getQuestionId()))
                    .findFirst()
                    .orElse(new StudentAnswer.Answer(questionId, "【学生未作答】",new BigDecimal(0), StudentAnswer.GradingStatus.AUTO_GRADED, null));

            return new StudentAnswerSnippetDTO(
                    sa.getId(),
                    sa.getUserId(),
                    studentName,
                    identifier,
                    answer.getGradingStatus(),
                    answer.getResponse(),
                    answer.getScore(),
                    answer.getComment() // 评语字段暂时未实现，后续添加
            );
        }).collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, pageResult.getTotalElements());
    }

    /**
     * [新增] 提交对单个答案的评分
     * @param request 包含答卷ID、题目ID、分数和评语的请求体
     * @return 更新后的答卷总分
     */
    @Transactional // 推荐将此方法标记为事务性，以确保多步更新的一致性
    public BigDecimal gradeAnswer(GradeSubmissionRequest request) {
        // --- 步骤 1: (MongoDB) 原子性更新单题分数、评语和状态 ---
        Query query = Query.query(Criteria.where("_id").is(request.submissionId()));
        Update update = new Update()
                .set("answers.$[elem].score", request.score())
                .set("answers.$[elem].comment", request.comment())
                .set("answers.$[elem].grading_status", "manually_graded")
                .set("updated_at", LocalDateTime.now()) // 同时更新文档的最后修改时间
                .filterArray(Criteria.where("elem.question_id").is(request.questionId()));
        log.info("[gradeAnswer.method] 更新文档: {}", query);
        UpdateResult result = mongoTemplate.updateFirst(query, update, StudentAnswer.class);

        if (result.getMatchedCount() == 0) {
            throw new ResourceNotFoundException("未找到 ID 为 " + request.submissionId() + " 的答卷");
        }
        if (result.getModifiedCount() == 0) {
            // 如果找到了文档但没有修改，可能是因为数组中没有匹配的题目ID
            // 或者打分内容与原来完全一样。为简化逻辑，可视为一个警告或无需处理。
            // 这里我们抛出一个异常，因为这通常意味着请求中的 questionId 有误。
            throw new BusinessException("400", "在指定答卷中未找到题目ID: " + request.questionId());
        }

        // --- 步骤 2: (Java) 重新读取、计算并更新总分 ---
        // 使用 findById 确保我们从数据库获取最新的数据
        Optional<StudentAnswer> updatedAnswerOptional = studentAnswerRepository.findById(request.submissionId());
        if (updatedAnswerOptional.isEmpty()) {
            // 理论上不应该发生，因为上一步已经匹配成功
            throw new IllegalStateException("数据不一致：无法重新读取已更新的答卷 " + request.submissionId());
        }

        StudentAnswer updatedAnswer = updatedAnswerOptional.get();

        // 在应用层重新计算总分
        BigDecimal newTotalScore = updatedAnswer.getAnswers().stream()
                .map(StudentAnswer.Answer::getScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        updatedAnswer.setTotalScore(newTotalScore);

        // --- 步骤 3: (MongoDB) 保存更新后的总分 ---
        studentAnswerRepository.save(updatedAnswer);

        return newTotalScore;
    }

    /**
     * [新增] 完成并归档指定测评的批阅工作
     * @param assessmentId 测评 ID
     * @return 包含处理结果的 DTO
     */
    @Transactional
    public FinalizeGradingResultDTO finalizeGrading(String assessmentId) {
        // --- 步骤 1: (MongoDB) 获取本次测评所有主观题的权威列表 ---
        Paper paper = paperRepository.findByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("与该测评关联的试卷不存在: " + assessmentId));

        List<String> allQuestionIdsInPaper = paper.getSections().stream()
                .flatMap(s -> s.getQuestions().stream().map(Paper.PaperQuestion::getQuestionId))
                .collect(Collectors.toList());

        Set<String> subjectiveQuestionIds = questionRepository.findAllById(allQuestionIdsInPaper).stream()
                .filter(q -> Question.SUBJECTIVE_TYPES.contains(q.getType()))
                .map(Question::getId)
                .collect(Collectors.toSet());

        if (subjectiveQuestionIds.isEmpty()) {
            return new FinalizeGradingResultDTO(0, 0, "该试卷不包含任何主观题，无需归档。");
        }

        // --- 步骤 2: (MongoDB) 找出所有状态为 'grading' 的答卷 ---
        List<StudentAnswer> pendingSubmissions = studentAnswerRepository.findByAssessmentIdAndStatus(assessmentId, "grading");

        if (pendingSubmissions.isEmpty()) {
            return new FinalizeGradingResultDTO(0, 0, "没有待处理的答卷。");
        }

        // --- 步骤 3: (Java) 遍历并校验，筛选出真正完成批改的答卷 ID ---
        List<String> idsToFinalize = pendingSubmissions.stream()
                .filter(submission -> isFullyGraded(submission, subjectiveQuestionIds))
                .map(StudentAnswer::getId)
                .collect(Collectors.toList());

        long finalizedCount = 0;
        if (!idsToFinalize.isEmpty()) {
            // --- 步骤 4: (MongoDB) 使用 updateMany 批量归档 ---
            Query query = Query.query(Criteria.where("_id").in(idsToFinalize));
            Update update = new Update()
                    .set("status", StudentAnswer.AnswerStatus.GRADED)
                    .set("updated_at", LocalDateTime.now());

            UpdateResult result = mongoTemplate.updateMulti(query, update, StudentAnswer.class);
            finalizedCount = result.getModifiedCount();
            log.info("[finalizeGrading] 成功归档 {} 份答卷", finalizedCount);
        }

        // --- 步骤 5: (Java) 构建“部分成功”的响应 ---
        long pendingCount = pendingSubmissions.size() - finalizedCount;
        String message = String.format("操作完成！成功归档 %d 份答卷。", finalizedCount);
        if (pendingCount > 0) {
            message += String.format(" 仍有 %d 份答卷因部分题目未批改而待处理。", pendingCount);
        }

        return new FinalizeGradingResultDTO(finalizedCount, pendingCount, message);
    }

    /**
     * [新增] 校验一份答卷是否所有主观题都已批改完毕
     * @param submission 待校验的答卷
     * @param subjectiveQuestionIds 权威的主观题ID列表
     * @return boolean
     */
    private boolean isFullyGraded(StudentAnswer submission, Set<String> subjectiveQuestionIds) {
        // 将已批改的主观题ID收集到一个Set中
        Set<String> gradedQuestionIds = submission.getAnswers().stream()
                .filter(answer -> "manually_graded".equals(answer.getGradingStatus()))
                .map(StudentAnswer.Answer::getQuestionId)
                .collect(Collectors.toSet());

        // 只有当已批改的主观题集合完全包含了权威主观题集合时，才算完成
        return gradedQuestionIds.containsAll(subjectiveQuestionIds);
    }

    /**
     * [私有] 使用一次聚合查询，高效计算所有主观题的批阅进度
     */
    private Map<String, GradingProgress> getGradingProgressFromMongo(String assessmentId) {
        Aggregation aggregation = Aggregation.newAggregation(
                // 阶段1: 筛选出本次测评中所有状态为 'grading' 的答卷
                Aggregation.match(Criteria.where("assessment_id").is(assessmentId).and("status").is("grading")),
                // 阶段2: 展开 answers 数组
                Aggregation.unwind("answers"),
                // 阶段3: 按题目ID分组，并同时计算总数和已批改数
                Aggregation.group("answers.question_id")
                        .count().as("totalCount") // 总数
                        .sum(
                                // 条件操作符：如果 gradingStatus 是 'manually_graded'，则为1，否则为0
                                ConditionalOperators.when(Criteria.where("answers.grading_status").is("manually_graded"))
                                        .then(1)
                                        .otherwise(0)
                        ).as("gradedCount"), // 已批改数
                // 阶段4: 投影，将 _id 字段重命名为 questionId
                Aggregation.project("totalCount", "gradedCount").and("_id").as("questionId")
        );

        AggregationResults<GradingProgressQueryResult> results = mongoTemplate.aggregate(
                aggregation, "student_answers", GradingProgressQueryResult.class
        );

        // 将查询结果列表转换为 Map，方便后续查找
        return results.getMappedResults().stream()
                .collect(Collectors.toMap(
                        GradingProgressQueryResult::getQuestionId,
                        r -> new GradingProgress(r.getGradedCount(), r.getTotalCount())
                ));
    }

    /**
     * [私有] 用于映射聚合查询结果的内部类
     */
    @Data
    @AllArgsConstructor
    private static class GradingProgressQueryResult {
        private String questionId;
        private Long gradedCount;
        private Long totalCount;
    }

    /**
     * [私有] 从 MongoDB 聚合查询每个测评的待批阅答卷数
     */
    private List<PendingCountResult> getPendingCountsFromMongo(String teacherId) {
        // 首先从MySQL找到该教师创建的所有测评ID
        List<String> teacherAssessmentIds = assessmentsMapper.findIdsByCreatorId(teacherId);
        if(teacherAssessmentIds.isEmpty()) {
            return Collections.emptyList();
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("status").is("grading").and("assessment_id").in(teacherAssessmentIds)),
                Aggregation.group("assessment_id").count().as("count"),
                Aggregation.project("count").and("_id").as("assessmentId")
        );
        log.info("[getPendingCountsFromMongo.method] 获取教师 {} 的待阅卷任务列表，关联测评信息：{}", teacherId, teacherAssessmentIds);
        AggregationResults<PendingCountResult> results = mongoTemplate.aggregate(aggregation, "student_answers", PendingCountResult.class);
        return results.getMappedResults();
    }

    /**
     * [私有] 批量获取多个测评的主观题数量
     */
    private Map<String, Integer> getSubjectiveQuestionCounts(List<String> assessmentIds) {
        List<Paper> papers = paperRepository.findByAssessmentIdIn(assessmentIds);
        List<String> allQuestionIds = papers.stream()
                .flatMap(p -> p.getSections().stream())
                .flatMap(s -> s.getQuestions().stream())
                .map(Paper.PaperQuestion::getQuestionId)
                .distinct().collect(Collectors.toList());

        Map<String, Question.QuestionType> questionTypeMap = questionRepository.findAllById(allQuestionIds).stream()
                .collect(Collectors.toMap(Question::getId, Question::getType));
        log.info("[getSubjectiveQuestionCounts.method] 获取教师 {} 的待阅卷任务列表，关联题目信息：{}", assessmentIds, questionTypeMap);
        return papers.stream().collect(Collectors.toMap(Paper::getAssessmentId, paper -> {
            long count = paper.getSections().stream()
                    .flatMap(s -> s.getQuestions().stream())
                    .map(Paper.PaperQuestion::getQuestionId)
                    .map(questionTypeMap::get)
                    .filter(type -> EnumSet.of(
                                    Question.QuestionType.SHORT_ANSWER,
                                    Question.QuestionType.FILL_IN_BLANKS, // 填空题也可能需要人工批阅
                                    Question.QuestionType.PROGRAMMING)
                            .contains(type))
                    .count();
            return (int) count;
        }));
    }

    /**
     * [私有] 用于映射聚合查询结果的内部类
     */
    @Data
    @AllArgsConstructor
    private static class PendingCountResult {
        private String assessmentId;
        private Long count;
    }
}
