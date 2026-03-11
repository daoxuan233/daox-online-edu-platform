package com.daox.online.service.Impl;

import com.daox.online.entity.mysql.Assessments;
import com.daox.online.entity.mysql.Courses;
import com.daox.online.entity.dto.AssessmentCreateDTO;
import com.daox.online.entity.dto.AssessmentTeacherDTO;
import com.daox.online.entity.dto.SubmitAnswerRequest;
import com.daox.online.entity.dto.teacher.assessment.AssessmentResultSummaryDTO;
import com.daox.online.entity.mongodb.Paper;
import com.daox.online.entity.mongodb.StudentAnswer;
import com.daox.online.entity.mongodb.dto.CourseBaseDTO;
import com.daox.online.entity.redis.AssessmentSession;
import com.daox.online.entity.views.requestDTO.SubmitAnswerDTO;
import com.daox.online.entity.views.responseVO.assessment.AssessmentPaperVO;
import com.daox.online.entity.views.responseVO.assessment.AssessmentResultVO;
import com.daox.online.entity.views.responseVO.assessment.AssessmentStatusVO;
import com.daox.online.entity.views.responseVO.StudentAssessmentVO;
import com.daox.online.listener.AssessmentMessageProducer;
import com.daox.online.mapper.AssessmentsMapper;
import com.daox.online.mapper.CoursesMapper;
import com.daox.online.repository.mongodb.PaperRepository;
import com.daox.online.repository.mongodb.QuestionRepository;
import com.daox.online.repository.mongodb.StudentAnswerRepository;
import com.daox.online.repository.redis.AssessmentSessionRepository;
import com.daox.online.service.AssessmentService;
import com.daox.online.service.UsersService;
import com.daox.online.uilts.DateUtils;
import com.daox.online.uilts.HybridIdGenerator;
import com.daox.online.uilts.constant.AssessmentType;
import com.daox.online.uilts.constant.Const;
import com.daox.online.uilts.SecondaryHybridIdGenerator;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor // 自动注入所有 final 字段的构造函数
public class AssessmentServiceImpl implements AssessmentService {

    @Resource
    private AssessmentsMapper assessmentsMapper;

    @Resource
    private StudentAnswerRepository studentAnswerRepository;

    @Resource
    private QuestionRepository questionRepository;

    @Resource
    private CoursesMapper coursesMapper;

    @Resource
    private UsersService usersService;

    @Resource
    private SecondaryHybridIdGenerator secondaryHybridIdGenerator;

    @Resource
    private HybridIdGenerator hybridIdGenerator;

    @Resource
    private PaperRepository paperRepository;

    private final AssessmentSessionRepository sessionRepository;

    private final AssessmentMessageProducer messageProducer;

    /**
     * 会话写入锁容器。
     * <p>
     * 设计目的：
     * 1) 对同一学生同一测评的“保存答案 / 提交试卷”操作进行串行化；
     * 2) 避免并发请求下“先读后写”覆盖导致答案丢失；
     * 3) 保证提交动作读取到的是最终一致的答案快照。
     * </p>
     */
    private final Map<String, ReentrantLock> sessionWriteLocks = new ConcurrentHashMap<>();

    /**
     * 教师端
     * 获取所有测评
     *
     * @return 所有测评
     */
    @Override
    public List<AssessmentTeacherDTO> allAssessments() {
        List<Assessments> assessmentsAll = assessmentsMapper.getAssessmentsAll();
        if (assessmentsAll == null || assessmentsAll.isEmpty()) {
            log.warn("[allAssessments.method] 获取全部测评失败");
            return Collections.emptyList();
        }
        return assessmentsAll.stream().map(assessments -> {
            Courses courseById = coursesMapper.getCourseById(assessments.getCourseId());
            if (courseById == null) {
                log.warn("[allAssessments.method] 获取课程信息失败, courseId: {}", assessments.getCourseId());
                return null;
            }
            return new AssessmentTeacherDTO()
                    .setId(assessments.getId())
                    .setCourse(new CourseBaseDTO()
                            .setId(courseById.getId())
                            .setName(courseById.getTitle()))
                    .setCreatorId(assessments.getCreatorId())
                    .setAssessmentType(assessments.getAssessmentType())
                    .setTitle(assessments.getTitle())
                    .setStartTime(assessments.getStartTime())
                    .setEndTime(assessments.getEndTime())
                    .setDurationMinutes(assessments.getDurationMinutes())
                    .setIsPublished(assessments.getIsPublished());
        }).collect(Collectors.toList());
    }

    /**
     * 获取学生的测评列表
     *
     * @param userId 用户ID
     * @param status 测评状态过滤（可选，传null表示不过滤）
     * @return 测评列表
     */
    @Override
    public List<StudentAssessmentVO> getStudentAssessments(String userId, String status) {
        // 1. 从MySQL获取基础测评信息
        List<StudentAssessmentVO> assessments = assessmentsMapper.getStudentAssessments(userId);

        if (assessments.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 获取测评ID列表
        List<String> assessmentIds = assessments.stream()
                .map(StudentAssessmentVO::getAssessmentId).collect(Collectors.toList());

        // 3. 从MongoDB批量获取学生答题状态
        List<StudentAnswer> studentAnswers = studentAnswerRepository.findByAssessmentIdInAndUserId(assessmentIds, userId);
        Map<String, StudentAnswer> answerMap = studentAnswers.stream().collect(Collectors.toMap(StudentAnswer::getAssessmentId, Function.identity()));
        Map<String, BigDecimal> fullScoreMap = buildAssessmentFullScoreMap(assessmentIds);

        // 4. 合并数据并设置状态
        Date now = new Date();
        List<StudentAssessmentVO> result = assessments.stream().map(assessment -> {
            StudentAnswer answer = answerMap.get(assessment.getAssessmentId());

            // 设置状态和相关信息
            if (answer != null) {
                assessment.setStatus(answer.getStatus());
                assessment.setSubmitTime(Date.from(answer.getSubmitTime().atZone(ZoneId.systemDefault()).toInstant())); // Convert LocalDateTime to Date
                assessment.setScore(answer.getTotalScore());
            } else {
                assessment.setStatus(Const.LEARNING_STATUS_NOT_START);
            }
            assessment.setFullScore(fullScoreMap.getOrDefault(assessment.getAssessmentId(), BigDecimal.ZERO));
            assessment.setAssessmentType(assessment.getAssessmentType());
            // 判断是否可以开始
            Date startTime = Date.from(assessment.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
            Date endTime = Date.from(assessment.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
            boolean canOperate = Objects.equals(assessment.getIsPublished(), 1);
            assessment.setCanStart(
                    canOperate
                            && now.after(startTime)
                            && now.before(endTime)
                            && (Const.LEARNING_STATUS_NOT_START.equals(assessment.getStatus()) || Const.ASSESSMENT_STATUS_IN_PROGRESS.equals(assessment.getStatus()))
            );

            return assessment;
        }).filter(assessment -> status == null || status.equals(assessment.getStatus())).collect(Collectors.toList());

        return result;
    }

    /**
     * 根据ID获取测评详情
     *
     * @param assessmentId 测评ID
     * @param userId       用户ID
     * @return 测评详情
     */
    @Override
    public StudentAssessmentVO getAssessmentById(String assessmentId, String userId) {
        // 获取单个测评信息
        StudentAssessmentVO assessment = assessmentsMapper.getAssessmentByIdComplex(assessmentId, userId);
        if (assessment == null) {
            return null;
        }

        // 获取学生答题状态
        Optional<StudentAnswer> answer = studentAnswerRepository.findByAssessmentIdAndUserId(assessmentId, userId);
        if (answer.isPresent()) {
            assessment.setStatus(answer.get().getStatus());
            assessment.setSubmitTime(Date.from(answer.get().getSubmitTime().atZone(ZoneId.systemDefault()).toInstant()));
            assessment.setScore(answer.get().getTotalScore());
        } else {
            assessment.setStatus(Const.LEARNING_STATUS_NOT_START);
        }
        assessment.setFullScore(resolveAssessmentFullScore(assessmentId));

        // 判断是否可以开始
        Date now = new Date();
        Date startTime = Date.from(assessment.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(assessment.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
        boolean canOperate = Objects.equals(assessment.getIsPublished(), 1);
        assessment.setCanStart(
                canOperate
                        && now.after(startTime)
                        && now.before(endTime)
                        && (Const.LEARNING_STATUS_NOT_START.equals(assessment.getStatus()) || Const.ASSESSMENT_STATUS_IN_PROGRESS.equals(assessment.getStatus()))
        );

        return assessment;
    }

    /**
     * 批量构建“测评ID -> 满分”的映射表。
     * <p>
     * 设计目标：
     * 1) 供学生测评列表接口一次性填充每张卡片的满分字段；
     * 2) 先批量读取试卷集合，避免逐条查询导致的 N+1 问题；
     * 3) 当同一测评存在多份历史试卷时，按更新时间优先选取最新版本；
     * 4) 若试卷缺少 totalScore，则自动按分题分值重新汇总，确保返回值可用。
     * </p>
     *
     * @param assessmentIds 测评ID列表
     * @return 满分映射表，未命中的测评将不出现在返回结果中
     */
    private Map<String, BigDecimal> buildAssessmentFullScoreMap(List<String> assessmentIds) {
        if (assessmentIds == null || assessmentIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Paper> papers = paperRepository.findByAssessmentIdIn(assessmentIds);
        if (papers == null || papers.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Paper> latestPaperMap = new HashMap<>();
        for (Paper paper : papers) {
            if (paper == null || paper.getAssessmentId() == null) {
                continue;
            }
            Paper current = latestPaperMap.get(paper.getAssessmentId());
            if (current == null || isPaperNewer(paper, current)) {
                latestPaperMap.put(paper.getAssessmentId(), paper);
            }
        }

        Map<String, BigDecimal> fullScoreMap = new HashMap<>();
        for (Map.Entry<String, Paper> entry : latestPaperMap.entrySet()) {
            fullScoreMap.put(entry.getKey(), calculatePaperFullScore(entry.getValue()));
        }
        return fullScoreMap;
    }

    /**
     * 解析单个测评的满分。
     * <p>
     * 适用场景：
     * 1) 学生查看单个测评详情；
     * 2) 服务层需要在非批量链路下补充满分字段。
     * </p>
     *
     * @param assessmentId 测评ID
     * @return 满分；若未找到试卷则返回 0
     */
    private BigDecimal resolveAssessmentFullScore(String assessmentId) {
        if (assessmentId == null || assessmentId.isBlank()) {
            return BigDecimal.ZERO;
        }
        return paperRepository.findFirstByAssessmentIdOrderByUpdatedAtDescCreatedAtDesc(assessmentId)
                .map(this::calculatePaperFullScore)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * 计算并返回试卷满分。
     * <p>
     * 计算规则：
     * 1) 若试卷 totalScore 已存在且非负，直接采用；
     * 2) 否则回退为“sections.questions.score”求和结果；
     * 3) 求和过程中自动忽略 null 分值，确保结果稳定。
     * </p>
     *
     * @param paper 试卷实体
     * @return 试卷满分；输入为空时返回 0
     */
    private BigDecimal calculatePaperFullScore(Paper paper) {
        if (paper == null) {
            return BigDecimal.ZERO;
        }
        if (paper.getTotalScore() != null && paper.getTotalScore().compareTo(BigDecimal.ZERO) >= 0) {
            return paper.getTotalScore();
        }
        if (paper.getSections() == null || paper.getSections().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return paper.getSections().stream()
                .filter(Objects::nonNull)
                .map(Paper.PaperSection::getQuestions)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .map(Paper.PaperQuestion::getScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 判断候选试卷是否比当前试卷更新。
     * <p>
     * 判定优先级：
     * 1) 先比较 updatedAt；
     * 2) updatedAt 相同或为空时比较 createdAt；
     * 3) 两者都不可比较时，保留当前记录不替换。
     * </p>
     *
     * @param candidate 候选试卷
     * @param current 当前已选试卷
     * @return true 表示候选试卷更新，应当替换
     */
    private boolean isPaperNewer(Paper candidate, Paper current) {
        LocalDateTime candidateUpdatedAt = candidate.getUpdatedAt();
        LocalDateTime currentUpdatedAt = current.getUpdatedAt();
        if (candidateUpdatedAt != null && currentUpdatedAt != null) {
            return candidateUpdatedAt.isAfter(currentUpdatedAt);
        }
        if (candidateUpdatedAt != null) {
            return true;
        }
        if (currentUpdatedAt != null) {
            return false;
        }

        LocalDateTime candidateCreatedAt = candidate.getCreatedAt();
        LocalDateTime currentCreatedAt = current.getCreatedAt();
        if (candidateCreatedAt != null && currentCreatedAt != null) {
            return candidateCreatedAt.isAfter(currentCreatedAt);
        }
        return candidateCreatedAt != null;
    }

    /**
     * 检查学生是否可以开始测评
     *
     * @param assessmentId 测评ID
     * @param userId       用户ID
     * @return 是否可以开始
     */
    @Override
    public boolean canStartAssessment(String assessmentId, String userId) {
        StudentAssessmentVO assessment = getAssessmentById(assessmentId, userId);
        return assessment != null && assessment.getCanStart();
    }

    /**
     * 获取试卷内容
     *
     * @param assessmentId 测评ID
     * @param userId       用户ID
     * @return 试卷内容
     */
    @Override
    public AssessmentPaperVO getAssessmentPaper(String assessmentId, String userId) {
        return null;
    }

    /**
     * 开始一次新的测评
     *
     * @param assessmentId 测评ID
     * @param userId       学生ID
     * @return 创建的会话
     */
    @Override
    public AssessmentSession startAssessment(String assessmentId, String userId) {
        // 1. 检查是否已存在进行中的会话
        if (sessionRepository.existsById(assessmentId, userId)) {
            throw new IllegalStateException("您已开始本次测评，请勿重复操作。");
        }

        // 2. 从MySQL查询测评信息，并进行业务校验
        Assessments assessment = loadAssessmentOrThrow(assessmentId);
        ensureAssessmentPublished(assessment);
        Date now = new Date();
        if (now.before(assessment.getStartTime()) || now.after(assessment.getEndTime())) {
            throw new IllegalStateException("当前时间不在测评的有效范围内。");
        }

        // 3. 创建新的会话对象
        AssessmentSession session = new AssessmentSession();
        session.setAssessmentId(assessment.getId());
        session.setUserId(userId);
        session.setCourseId(assessment.getCourseId());
        session.setStartTime(LocalDateTime.now());
        session.setStatus(AssessmentSession.Status.IN_PROGRESS);

        // 4. 存入 Redis 并设置过期时间
        // 关键约束：
        // 1) 会话时长不允许超过测评结束时间；
        // 2) 以“测评设定时长”和“距截止剩余时长”两者较小值作为最终TTL基准。
        int effectiveDurationMinutes = calculateEffectiveSessionDurationMinutes(assessment, now);
        sessionRepository.save(session, effectiveDurationMinutes);

        return session;
    }

    /**
     * 保存学生答案
     *
     * @param assessmentId 测评ID
     * @param userId       学生ID
     * @param request      答案请求
     */
    @Override
    public void saveAnswer(String assessmentId, String userId, SubmitAnswerRequest request) {
        if (request == null || request.questionId() == null || request.questionId().isBlank()) {
            throw new IllegalArgumentException("题目ID不能为空。");
        }

        ReentrantLock sessionLock = acquireSessionWriteLock(assessmentId, userId);
        sessionLock.lock();
        try {
            AssessmentSession session = sessionRepository.findById(assessmentId, userId)
                    .orElseThrow(() -> new IllegalStateException("测评会话不存在或已过期，请重新开始。"));

            ensureAssessmentCanContinue(assessmentId);

            if (AssessmentSession.Status.SUBMITTED.equals(session.getStatus())) {
                throw new IllegalStateException("试卷已提交，禁止继续保存答案。");
            }
            if (AssessmentSession.Status.SUBMITTING.equals(session.getStatus())) {
                throw new IllegalStateException("试卷正在提交中，请勿重复保存。");
            }

            AssessmentSession.AnswerPayload payload = new AssessmentSession.AnswerPayload();
            payload.setQuestionId(request.questionId());
            payload.setResponse(request.response());
            payload.setAnswerTime(LocalDateTime.now());

            session.getAnswers().put(request.questionId(), payload);
            sessionRepository.save(session);
        } finally {
            sessionLock.unlock();
        }
    }

    @Override
    public boolean submitAnswer(String assessmentId, String userId, SubmitAnswerDTO submitAnswerDTO) {
        return false;
    }

    /**
     * 提交试卷
     *
     * @param assessmentId 测评ID
     * @param userId       用户ID
     */
    @Override
    public void submitAssessment(String assessmentId, String userId) {
        ReentrantLock sessionLock = acquireSessionWriteLock(assessmentId, userId);
        sessionLock.lock();
        try {
            AssessmentSession session = sessionRepository.findById(assessmentId, userId)
                    .orElseThrow(() -> new IllegalStateException("测评会話不存在或已过期，无法提交。"));

            ensureAssessmentCanContinue(assessmentId);

            if (Objects.equals(session.getStatus(), AssessmentSession.Status.SUBMITTED)) {
                throw new IllegalStateException("您已成功提交，请勿重复操作。");
            }
            if (Objects.equals(session.getStatus(), AssessmentSession.Status.SUBMITTING)) {
                throw new IllegalStateException("试卷正在提交中，请稍后查看结果。");
            }

            session.setStatus(AssessmentSession.Status.SUBMITTING);
            sessionRepository.save(session);

            try {
                messageProducer.sendSubmissionMessage(session);
                sessionRepository.markAsSubmitted(session, 15);

            } catch (Exception e) {
                session.setStatus(AssessmentSession.Status.IN_PROGRESS);
                sessionRepository.save(session);
                throw new RuntimeException("提交失败，系统异常，请稍后重试。", e);
            }
        } finally {
            sessionLock.unlock();
        }
    }

    /**
     * 获取会话级写锁。
     * <p>
     * 锁粒度：assessmentId + userId。
     * 这样可以保证：
     * 1) 同一学生同一场考试内的写操作严格串行；
     * 2) 不同学生或不同测评之间互不阻塞，保持系统吞吐。
     * </p>
     *
     * @param assessmentId 测评ID
     * @param userId 学生ID
     * @return 对应会话的可重入锁
     */
    private ReentrantLock acquireSessionWriteLock(String assessmentId, String userId) {
        String lockKey = buildSessionLockKey(assessmentId, userId);
        return sessionWriteLocks.computeIfAbsent(lockKey, key -> new ReentrantLock());
    }

    /**
     * 构建会话级锁键。
     * <p>
     * 使用固定分隔符拼接，确保锁键稳定可复现，避免同一业务会话落到不同锁对象。
     * </p>
     *
     * @param assessmentId 测评ID
     * @param userId 学生ID
     * @return 会话锁键
     */
    private String buildSessionLockKey(String assessmentId, String userId) {
        return assessmentId + "::" + userId;
    }

    /**
     * 按测评ID加载测评，不存在时抛出统一业务异常。
     *
     * @param assessmentId 测评ID
     * @return 测评实体
     */
    private Assessments loadAssessmentOrThrow(String assessmentId) {
        Assessments assessment = assessmentsMapper.getAssessmentById(assessmentId);
        if (assessment == null) {
            throw new IllegalArgumentException("测评不存在。");
        }
        return assessment;
    }

    /**
     * 校验测评是否处于“已发布”状态。
     *
     * @param assessment 测评实体
     */
    private void ensureAssessmentPublished(Assessments assessment) {
        if (!Objects.equals(assessment.getIsPublished(), 1)) {
            throw new IllegalStateException("测评未发布或已过期。");
        }
    }

    /**
     * 校验测评是否仍允许“继续作答/提交”。
     * <p>
     * 业务规则：
     * 1) 测评必须处于已发布状态；
     * 2) 当前时间超过测评截止时间后，禁止继续保存答案与提交试卷。
     * </p>
     *
     * @param assessmentId 测评ID
     */
    private void ensureAssessmentCanContinue(String assessmentId) {
        Assessments assessment = loadAssessmentOrThrow(assessmentId);
        ensureAssessmentPublished(assessment);
        Date now = new Date();
        if (now.after(assessment.getEndTime())) {
            throw new IllegalStateException("测评已截止，禁止继续答题。");
        }
    }

    /**
     * 计算会话有效时长（分钟）。
     * <p>
     * 设计目的：
     * 1) 避免学生在临近截止时开始测评后，凭借“答题时长TTL”跨过截止时间继续作答；
     * 2) 将会话TTL收敛为“题目时长”和“距截止剩余时长”中的较小值；
     * 3) 结果至少为1分钟，保证Redis TTL参数合法。
     * </p>
     *
     * @param assessment 测评实体
     * @param now 当前时间
     * @return 会话TTL分钟值
     */
    private int calculateEffectiveSessionDurationMinutes(Assessments assessment, Date now) {
        int durationMinutes = Optional.ofNullable(assessment.getDurationMinutes()).orElse(1);
        long remainingMinutes = Duration.between(now.toInstant(), assessment.getEndTime().toInstant()).toMinutes();
        long boundedRemaining = Math.max(1L, remainingMinutes);
        return (int) Math.max(1L, Math.min(durationMinutes, boundedRemaining));
    }

    @Override
    public AssessmentStatusVO getAssessmentStatus(String assessmentId, String userId) {
        return null;
    }

    @Override
    public AssessmentResultVO getAssessmentResult(String assessmentId, String userId) {
        return null;
    }

    /**
     * 获取测评列表
     *
     * @param courseID 课程ID
     * @return 测评列表
     */
    @Override
    public List<Assessments> getAssessments(String courseID) {
        if (courseID == null || courseID.isEmpty()) {
            log.warn("[getAssessments.method] 课程ID不能为空");
            return Collections.emptyList();
        }
        if (!coursesMapper.checkCourseExists(courseID)) {
            log.warn("[getAssessments.method] 课程不存在: courseID={}", courseID);
            return Collections.emptyList();
        }
        if (!coursesMapper.checkCourseStatus(courseID)) {
            log.warn("[getAssessments.method] 课程未发布: courseID={}", courseID);
            return Collections.emptyList();
        }
        return assessmentsMapper.getAssessmentsByCourseID(courseID);
    }

    /**
     * 创建测评
     *
     * @param assessmentCreateDTO 创建测评参数
     * @return 测评ID
     */
    @Override
    @Transactional
    public Assessments createAssessment(AssessmentCreateDTO assessmentCreateDTO) {
        if (assessmentCreateDTO == null) {
            log.warn("[createAssessment.method] 创建测评失败: assessmentCreateDTO不能为空");
            return null;
        }

        // 判断assessmentType值是否是枚举中的值
        if (!AssessmentType.isValid(assessmentCreateDTO.getAssessmentType())) {
            log.warn("[createAssessment.method] 无效的测评类型: assessmentType={}", assessmentCreateDTO.getAssessmentType());
            return null;
        }

        // 创建测评时必须使用前端提交的开始/结束时间，不能使用系统当前时间。
        // 这里先读取DTO中的时间，后续统一做空值和区间校验，确保入库数据可靠。
        LocalDateTime startTime = assessmentCreateDTO.getStartTime();
        LocalDateTime endTime = assessmentCreateDTO.getEndTime();

        // 校验开始时间和结束时间不能为空，避免出现空时间导致的业务异常。
        if (startTime == null || endTime == null) {
            log.warn("[createAssessment.method] 创建测评失败: 开始时间或结束时间为空, startTime={}, endTime={}", startTime, endTime);
            return null;
        }

        // 校验时间区间合法性：结束时间必须严格晚于开始时间。
        // 如果结束时间早于或等于开始时间，测评将无法正常开启/关闭，因此直接拒绝创建。
        if (!endTime.isAfter(startTime)) {
            log.warn("[createAssessment.method] 创建测评失败: 时间区间非法, startTime={}, endTime={}", startTime, endTime);
            return null;
        }

        // 将DTO转换为数据库实体：
        // 1) start_time 使用用户选择的开始时间；
        // 2) end_time 使用用户选择的结束时间；
        // 3) 严禁使用 LocalDateTime.now() 覆盖用户输入，避免开始结束时间被写成同一时刻。
        Assessments assessments = new Assessments()
                .setId(hybridIdGenerator.generateId())
                .setCourseId(assessmentCreateDTO.getCourseId())
                .setCreatorId(assessmentCreateDTO.getCreatorId())
                .setAssessmentType(assessmentCreateDTO.getAssessmentType())
                .setTitle(assessmentCreateDTO.getTitle())
                .setStartTime(DateUtils.convertToDate(startTime))
                .setEndTime(DateUtils.convertToDate(endTime))
                .setDurationMinutes(assessmentCreateDTO.getDurationMinutes())
                .setIsPublished(assessmentCreateDTO.getIsPublished());
        int assessment = assessmentsMapper.createAssessment(assessments);
        if (assessment <= 0) {
            log.warn("[createAssessment.method] 创建测评失败: assessmentCreateDTO={}", assessmentCreateDTO);
            return null;
        }

        return assessments;
    }

    /**
     * 获取测评详情
     *
     * @param assessmentId 测评ID
     * @return 测评详情
     */
    @Override
    public Assessments getAssessmentDetails(String assessmentId) {
        if (assessmentId == null || assessmentId.isEmpty()) {
            log.warn("[getAssessmentDetails.method] 测评ID不能为空");
            return null;
        }
        Assessments assessmentById = assessmentsMapper.getAssessmentById(assessmentId);
        if (assessmentById == null) {
            log.warn("[getAssessmentDetails.method] 测评不存在: assessmentId={}", assessmentId);
            return null;
        }
        return assessmentById;
    }

    /**
     * 更新测评
     *
     * @param userId       用户ID
     * @param assessmentId 测评ID
     * @param title        标题
     * @param duration     持续时间
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @param isPublished  是否发布
     * @return 结果
     */
    @Override
    public Assessments updateAssessment(String userId, String assessmentId, String title, int duration, String startTime, String endTime, int isPublished) {
        if (!usersService.isTeacher(userId)) {
            log.warn("[updateAssessment.method] 用户不是老师: userId={}", userId);
            return null;
        }
        Assessments assessmentById = assessmentsMapper.getAssessmentById(assessmentId);
        if (assessmentById == null) {
            log.warn("[updateAssessment.method] 测评不存在: assessmentId={}", assessmentId);
            return null;
        }
        if (!assessmentById.getCreatorId().equals(userId)) {
            log.warn("[updateAssessment.method] 用户无权更新该测评: userId={}, assessmentId={}", userId, assessmentId);
            return null;
        }
        Assessments assessments = new Assessments().setId(assessmentId).setTitle(title).setDurationMinutes(duration).setStartTime(Date.from(LocalDateTime.parse(startTime).atZone(ZoneId.systemDefault()).toInstant())).setEndTime(Date.from(LocalDateTime.parse(endTime).atZone(ZoneId.systemDefault()).toInstant())).setIsPublished(isPublished);
        int update = assessmentsMapper.updateAssessment(assessments);
        if (update > 0) {
            log.info("[updateAssessment.method] 更新测评成功: userId={}, assessmentId={}, title={}, duration={}, startTime={}, endTime={}, isPublished={}", userId, assessmentId, title, duration, startTime, endTime, isPublished);
            return assessmentsMapper.getAssessmentById(assessmentId);
        }
        return null;
    }

    /**
     * 删除测评 - 逻辑处理 - 将is_published = -1
     *
     * @param userId       用户ID
     * @param assessmentId 测评ID
     * @return 删除结果
     */
    @Override
    public boolean deleteAssessment(String userId, String assessmentId) {
        if (assessmentId == null || assessmentId.isEmpty()) {
            log.warn("[deleteAssessment.method] 测评ID不能为空");
            return false;
        }
        Assessments assessmentById = assessmentsMapper.getAssessmentById(assessmentId);
        if (assessmentById == null) {
            log.warn("[deleteAssessment.method] 测评不存在: assessmentId={}", assessmentId);
            return false;
        }
        if (!usersService.isTeacher(userId) || !assessmentById.getCreatorId().equals(userId) || !usersService.isAdmin(userId)) {
            log.warn("[deleteAssessment.method] 用户无权删除该测评: userId={}, assessmentId={}", userId, assessmentId);
            return false;
        }
        int delete = assessmentsMapper.deleteAssessment(assessmentId);
        if (delete > 0) {
            log.info("[deleteAssessment.method] 删除测评成功: userId={}, assessmentId={}", userId, assessmentId);
            return true;
        }
        return false;
    }

    /**
     * 发布测评
     *
     * @param userId       用户ID
     * @param assessmentId 测评ID
     * @return 发布结果
     */
    @Override
    public boolean publishAssessment(String userId, String assessmentId) {
        if (assessmentId == null || assessmentId.isEmpty()) {
            log.warn("[publishAssessment.method] 测评ID不能为空");
            return false;
        }
        log.info("[publishAssessment.method] 发布测评: userId={}, \n assessmentId={}", userId, assessmentId);
        Assessments assessmentById = assessmentsMapper.getAssessmentById(assessmentId);
        if (assessmentById == null) {
            log.warn("[publishAssessment.method] 测评不存在: assessmentId={}", assessmentId);
            return false;
        }
        if (!usersService.isTeacher(userId) || !assessmentById.getCreatorId().equals(userId)) {
            log.warn("[publishAssessment.method] 用户无权发布该测评: userId={}, assessmentId={}", userId, assessmentId);
            return false;
        }
        int publish = assessmentsMapper.publishAssessment(assessmentId);
        if (publish > 0) {
            log.info("[publishAssessment.method] 发布测评成功: userId={}, assessmentId={}", userId, assessmentId);
            return true;
        }
        return false;
    }

    /**
     * 获取试卷结构
     *
     * @param assessmentId 测评ID
     * @return 试卷结构
     */
    @Override
    public Map<String, Object> getAssessmentPaperStructure(String assessmentId) {
        return null;
    }

    /**
     * 获取单个测评的结果汇总信息
     * <p>
     * 指标定义：
     * 1) 实际参考人数：当前测评在答卷集合中的记录总数；
     * 2) 应参考人数：测评所属课程的学习人数（courses.enrollment_count）；
     * 3) 平均分：当前测评总得分 / 应参考人数；
     * 4) 完成率：实际参考人数 / 应参考人数。
     * </p>
     *
     * @param userId       当前登录教师ID
     * @param assessmentId 测评ID
     * @return 测评结果汇总 DTO
     */
    @Override
    public AssessmentResultSummaryDTO getAssessmentResultSummary(String userId, String assessmentId) {
        if (assessmentId == null || assessmentId.isBlank()) {
            throw new IllegalArgumentException("测评ID不能为空");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        Assessments assessment = assessmentsMapper.getAssessmentById(assessmentId);
        if (assessment == null) {
            throw new IllegalArgumentException("测评不存在");
        }
        if (!Objects.equals(assessment.getCreatorId(), userId)) {
            throw new SecurityException("无权限查看该测评统计");
        }

        long actualParticipantCount = studentAnswerRepository.countByAssessmentId(assessmentId);

        Courses course = coursesMapper.getCourseById(assessment.getCourseId());
        long shouldParticipantCount = Optional.ofNullable(course)
                .map(Courses::getEnrollmentCount)
                .map(Integer::longValue)
                .orElse(0L);

        BigDecimal totalScore = studentAnswerRepository.findScoresByAssessmentId(assessmentId).stream()
                .map(StudentAnswer::getTotalScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageScore = shouldParticipantCount > 0
                ? totalScore.divide(BigDecimal.valueOf(shouldParticipantCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal completionRate = shouldParticipantCount > 0
                ? BigDecimal.valueOf(actualParticipantCount)
                .divide(BigDecimal.valueOf(shouldParticipantCount), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new AssessmentResultSummaryDTO(
                assessmentId,
                actualParticipantCount,
                shouldParticipantCount,
                averageScore,
                completionRate
        );
    }

}
