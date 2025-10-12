package com.daox.online.service.Impl;

import com.daox.online.entity.mysql.Assessments;
import com.daox.online.entity.mysql.Courses;
import com.daox.online.entity.dto.AssessmentCreateDTO;
import com.daox.online.entity.dto.AssessmentTeacherDTO;
import com.daox.online.entity.dto.SubmitAnswerRequest;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
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
            assessment.setAssessmentType(assessment.getAssessmentType());
            // 判断是否可以开始
            Date startTime = Date.from(assessment.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
            Date endTime = Date.from(assessment.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
            assessment.setCanStart(now.after(startTime) && now.before(endTime) && (Const.LEARNING_STATUS_NOT_START.equals(assessment.getStatus()) || Const.ASSESSMENT_STATUS_IN_PROGRESS.equals(assessment.getStatus())));

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

        // 判断是否可以开始
        Date now = new Date();
        Date startTime = Date.from(assessment.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(assessment.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
        assessment.setCanStart(now.after(startTime) && now.before(endTime) && (Const.LEARNING_STATUS_NOT_START.equals(assessment.getStatus()) || Const.ASSESSMENT_STATUS_IN_PROGRESS.equals(assessment.getStatus())));

        return assessment;
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
        Assessments assessment = assessmentsMapper.getAssessmentById(assessmentId);
        if (assessment == null) {
            throw new IllegalArgumentException("测评不存在。");
        }
        if (!Objects.equals(assessment.getIsPublished(), 1)) {
            throw new IllegalStateException("测评未发布或已过期。");
        }
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
        sessionRepository.save(session, assessment.getDurationMinutes());

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
        // 1. 从 Redis 获取当前会话
        AssessmentSession session = sessionRepository.findById(assessmentId, userId)
                .orElseThrow(() -> new IllegalStateException("测评会话不存在或已过期，请重新开始。"));

        // 2. 创建或更新答案
        AssessmentSession.AnswerPayload payload = new AssessmentSession.AnswerPayload();
        payload.setQuestionId(request.questionId());
        payload.setResponse(request.response());
        payload.setAnswerTime(LocalDateTime.now());

        session.getAnswers().put(request.questionId(), payload);

        // 3. 将更新后的会话存回 Redis (不改变过期时间)
        sessionRepository.save(session);
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
        // 1. 获取会话，并进行前置状态校验
        AssessmentSession session = sessionRepository.findById(assessmentId, userId)
                .orElseThrow(() -> new IllegalStateException("测评会話不存在或已过期，无法提交。"));

        if (Objects.equals(session.getStatus(), AssessmentSession.Status.SUBMITTED)) {
            throw new IllegalStateException("您已成功提交，请勿重复操作。");
        }

        // 2. 原子性状态锁定 (核心防重操作)
        session.setStatus(AssessmentSession.Status.SUBMITTING);
        sessionRepository.save(session); // 立即保存，将状态锁写入 Redis

        try {
            // 3. 发送消息到 RabbitMQ
            messageProducer.sendSubmissionMessage(session);

            // 4. 消息发送成功后，最终确认：将会话标记为"已提交"并设置15分钟TTL
            sessionRepository.markAsSubmitted(session, 15);

        } catch (Exception e) {
            // 5. 异常回滚：如果消息发送失败，将会话状态恢复为"答题中"，以便用户可以重试
            session.setStatus(AssessmentSession.Status.IN_PROGRESS);
            sessionRepository.save(session);
            // 向上抛出异常，让 Controller 层感知到失败
            throw new RuntimeException("提交失败，系统异常，请稍后重试。", e);
        }
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

        Assessments assessments = new Assessments()
                .setId(hybridIdGenerator.generateId())
                .setCourseId(assessmentCreateDTO.getCourseId())
                .setCreatorId(assessmentCreateDTO.getCreatorId())
                .setAssessmentType(assessmentCreateDTO.getAssessmentType())
                .setTitle(assessmentCreateDTO.getTitle())
                .setStartTime(DateUtils.convertToDate(LocalDateTime.now()))
                .setEndTime(DateUtils.convertToDate(LocalDateTime.now()))
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

}
