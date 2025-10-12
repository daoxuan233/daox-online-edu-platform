package com.daox.online.controller.students;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.dto.StartAssessmentRequest;
import com.daox.online.entity.dto.SubmitAnswerRequest;
import com.daox.online.entity.mongodb.dto.paper.StuPaperDTO;
import com.daox.online.entity.views.responseVO.StudentAssessmentVO;
import com.daox.online.service.AssessmentService;
import com.daox.online.service.PaperService;
import com.daox.online.uilts.UserUtils;
import com.daox.online.uilts.constant.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生端 - 考核信息接口
 */
@Slf4j
@RestController
@RequestMapping("/api/student/assessments")
public class AssessmentController {

    @Resource
    private AssessmentService assessmentService;
    @Resource
    private PaperService paperService;

    /**
     * 获取我的测评列表
     *
     * @param status  测评状态过滤条件（可选）
     * @param request HTTP请求对象
     * @return 测评列表
     */
    @GetMapping("/list")
    public RestBean<List<StudentAssessmentVO>> getMyAssessments(@RequestParam(required = false) String status, HttpServletRequest request) {

        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return RestBean.failure(401, "用户未认证");
        }

        try {
            List<StudentAssessmentVO> assessments = assessmentService.getStudentAssessments(userId, status);
            return RestBean.success(assessments);
        } catch (Exception e) {
            log.error("获取测评列表失败: userId={}, status={}", userId, status, e);
            return RestBean.failure(500, "获取测评列表失败");
        }
    }

    /**
     * 获取单个测评详情
     *
     * @param assessmentId 测评ID
     * @param request      HTTP请求对象
     * @return 测评详情
     */
    @GetMapping("/{assessmentId}")
    public RestBean<StudentAssessmentVO> getAssessmentDetail(
            @PathVariable String assessmentId,
            HttpServletRequest request) {

        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return RestBean.failure(401, "用户未认证");
        }

        try {
            StudentAssessmentVO assessment = assessmentService.getAssessmentById(assessmentId, userId);
            if (assessment == null) {
                return RestBean.failure(404, "测评不存在");
            }
            return RestBean.success(assessment);
        } catch (Exception e) {
            log.error("获取测评详情失败: assessmentId={}, userId={}", assessmentId, userId, e);
            return RestBean.failure(500, "获取测评详情失败");
        }
    }

    /**
     * 检查是否可以开始测评
     *
     * @param assessmentId 测评ID
     * @param request      HTTP请求对象
     * @return 是否可以开始测评
     */
    @GetMapping("/can-start/{assessmentId}")
    public RestBean<Boolean> canStartAssessment(
            @PathVariable String assessmentId,
            HttpServletRequest request) {

        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return RestBean.failure(401, "用户未认证");
        }

        try {
            boolean canStart = assessmentService.canStartAssessment(assessmentId, userId);
            return RestBean.success(canStart);
        } catch (Exception e) {
            log.error("检查测评开始权限失败: assessmentId={}, userId={}", assessmentId, userId, e);
            return RestBean.failure(500, "检查测评开始权限失败");
        }
    }

    /**
     * 开始考试，创建测评会话
     *
     * @param request     包含 assessmentId
     * @param httpRequest HTTP 请求
     * @return 统一响应体
     */
    @PostMapping("/start")
    public RestBean<Void> startAssessment(@RequestBody StartAssessmentRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = UserUtils.getCurrentUserId(httpRequest);
            assessmentService.startAssessment(request.assessmentId(), userId);
            return RestBean.success();
        } catch (Exception e) {
            log.error("开始测评失败: {}", e.getMessage(), e);
            return RestBean.failure(400, e.getMessage());
        }
    }

    /**
     * 获取试卷内容接口
     *
     * @param assessmentId 测评 ID
     * @param httpRequest  HTTP 请求
     * @return 包含试卷内容的统一响应体
     */
    @GetMapping("/{assessmentId}/paper")
    public RestBean<StuPaperDTO> getPaperContent(@PathVariable String assessmentId, HttpServletRequest httpRequest) {
        try {
            String userId = UserUtils.getCurrentUserId(httpRequest);
            StuPaperDTO paperDTO = paperService.getPaperForStudent(assessmentId, userId);
            return RestBean.success(paperDTO);
        } catch (IllegalStateException e) {
            // 捕获业务校验异常，如会话不存在
            return RestBean.failure(403, e.getMessage());
        } catch (Exception e) {
            // 捕获其他未知异常，如数据库查询失败
            return RestBean.failure(500, "获取试卷失败，请稍后重试。");
        }
    }

    /**
     * 实时保存学生答案
     *
     * @param assessmentId 测评ID (从路径获取)
     * @param request      包含 questionId 和 response
     * @param httpRequest  HTTP 请求
     * @return 统一响应体
     */
    @PostMapping("/{assessmentId}/answer")
    public RestBean<Void> submitAnswer(@PathVariable String assessmentId, @RequestBody SubmitAnswerRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = UserUtils.getCurrentUserId(httpRequest);
            assessmentService.saveAnswer(assessmentId, userId, request);
            return RestBean.success();
        } catch (Exception e) {
            log.error("保存学生答案失败: {}", e.getMessage(), e);
            return RestBean.failure(400, e.getMessage());
        }
    }

    /**
     * 提交试卷
     *
     * @param assessmentId 测评ID
     * @param httpRequest  HTTP 请求
     * @return 统一响应体
     */
    @PostMapping("/{assessmentId}/submit")
    public RestBean<Void> submitAssessment(@PathVariable String assessmentId, HttpServletRequest httpRequest) {
        try {
            String userId = UserUtils.getCurrentUserId(httpRequest);
            assessmentService.submitAssessment(assessmentId, userId);
            return RestBean.success();
        } catch (IllegalStateException e) {
            // 捕获可预期的业务异常，例如重复提交
            return RestBean.failure(400, e.getMessage());
        } catch (Exception e) {
            // 捕获因消息队列故障等导致的未知异常
            return RestBean.failure(500, "提交失败，请稍后重试或联系管理员。");
        }
    }

    //TODO: 获取测评状态

    // TODO: 获取测评结果

    /**
     * 从JWT token中获取用户ID
     *
     * @param request HTTP请求对象
     * @return 用户ID，如果未认证则返回null
     */
    private String getUserIdFromToken(HttpServletRequest request) {
        Object userId = request.getAttribute(Const.ATTR_USER_ID);
        return userId != null ? userId.toString() : null;
    }

}
