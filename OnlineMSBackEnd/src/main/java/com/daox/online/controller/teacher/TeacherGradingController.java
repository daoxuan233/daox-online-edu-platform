package com.daox.online.controller.teacher;

import com.daox.online.aop.annotation.RequireCourseCreator;
import com.daox.online.entity.RestBean;
import com.daox.online.entity.dto.teacher.marking.*;
import com.daox.online.service.Impl.TeacherGradingService;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 教师阅卷控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/grading")
public class TeacherGradingController {

    @Resource
    private TeacherGradingService teacherGradingService;

    /**
     * 获取当前登录教师的所有待批阅测评任务
     *
     * @param request HttpServletRequest
     * @return 待批阅任务列表
     */
    @GetMapping("/tasks")
    public RestBean<List<GradingTaskDTO>> getGradingTasks(HttpServletRequest request) {
        String teacherId = UserUtils.getCurrentUserId(request);
        List<GradingTaskDTO> tasks = teacherGradingService.getGradingTasks(teacherId);
        return RestBean.success(tasks);
    }

    /**
     * [新增] 获取单个测评的批阅详情（主观题列表及进度）
     *
     * @param assessmentId 测评ID
     * @return 阅卷详情 DTO
     */
    @GetMapping("/tasks/{assessmentId}")
    @RequireCourseCreator // <-- [核心] 使用我们创建的安全注解进行权限校验
    public RestBean<GradingDetailDTO> getGradingTaskDetail(@PathVariable String assessmentId) {
        GradingDetailDTO detail = teacherGradingService.getGradingTaskDetail(assessmentId);
        return RestBean.success(detail);
    }

    @GetMapping("/tasks/{assessmentId}/status")
    @RequireCourseCreator
    public RestBean<GradingTaskStatusDTO> getGradingTaskStatus(@PathVariable String assessmentId) {
        GradingTaskStatusDTO status = teacherGradingService.getGradingTaskStatus(assessmentId);
        return RestBean.success(status);
    }

    /**
     * 分页获取单个主观题的所有学生答案
     *
     * @param assessmentId 测评ID
     * @param questionId   题目ID
     * @param pageable     Spring自动注入的分页对象 (默认每页10条)
     * @return 包含学生答案片段DTO的分页对象
     */
    @GetMapping("/tasks/{assessmentId}/questions/{questionId}/answers")
    @RequireCourseCreator // <-- [核心] 安全校验
    public RestBean<Page<StudentAnswerSnippetDTO>> getStudentAnswers(
            @PathVariable String assessmentId,
            @PathVariable String questionId,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<StudentAnswerSnippetDTO> page = teacherGradingService.getStudentAnswersForQuestion(assessmentId, questionId, pageable);
        return RestBean.success(page);
    }

    /**
     * [新增] 提交对单个答案的评分
     *
     * @param request 评分请求体
     * @return 包含成功状态及更新后总分的对象
     */
    @PostMapping("/answers/grade")
    public RestBean<Map<String, BigDecimal>> gradeAnswer(@Validated @RequestBody GradeSubmissionRequest request, HttpServletRequest httpServletRequest) {
        // TODO: 在 Service 层 gradeAnswer 方法的开头，需要增加权限校验逻辑：
        // 1. 根据 request.submissionId() 查出 StudentAnswer
        // 2. 获取其 assessmentId
        // 3. 根据 assessmentId 查出 Assessments
        // 4. 校验其 creatorId 是否为当前登录教师
        String currentUserId = UserUtils.getCurrentUserId(httpServletRequest);
        if (currentUserId == null) {
            log.warn("[gradeAnswer.method] 当前用户ID为空");
            return RestBean.unauthorized("用户未认证");
        }
        BigDecimal newTotalScore = teacherGradingService.gradeAnswer(request);
        return RestBean.success(Map.of("updatedTotalScore", newTotalScore));
    }

    /**
     * [新增] 完成并归档整个测评的批阅工作
     *
     * @param assessmentId 测评ID
     * @return 包含处理结果的 DTO
     */
    @PostMapping("/tasks/{assessmentId}/finalize")
    @RequireCourseCreator // <-- [核心] 安全校验
    public RestBean<FinalizeGradingResultDTO> finalizeGrading(@PathVariable String assessmentId) {
        FinalizeGradingResultDTO result = teacherGradingService.finalizeGrading(assessmentId);
        return RestBean.success(result);
    }
}
