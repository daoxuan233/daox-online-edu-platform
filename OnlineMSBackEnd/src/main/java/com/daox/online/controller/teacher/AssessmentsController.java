package com.daox.online.controller.teacher;

import com.daox.online.entity.mysql.Assessments;
import com.daox.online.entity.RestBean;
import com.daox.online.entity.dto.AssessmentCreateDTO;
import com.daox.online.entity.dto.AssessmentTeacherDTO;
import com.daox.online.entity.dto.PublishAssessmentRequest;
import com.daox.online.entity.dto.teacher.assessment.AssessmentResultSummaryDTO;
import com.daox.online.service.AssessmentService;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 教师端 - 测评相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/assessments")
public class AssessmentsController {

    @Resource
    private AssessmentService assessmentService;

    /**
     * 获取所有测评
     *
     * @return 所有测评
     */
    @GetMapping("")
    public RestBean<List<AssessmentTeacherDTO>> getAssessments() {
        List<AssessmentTeacherDTO> assessments = assessmentService.allAssessments();
        if (assessments == null || assessments.isEmpty()) {
            return RestBean.failure(404, "没有找到测评");
        }
        return RestBean.success(assessments);
    }

    /**
     * 获取测评列表
     *
     * @param courseID 课程ID
     * @return 测评列表
     */
    @GetMapping("/{courseID}")
    public RestBean<List<Assessments>> getAssessments(@PathVariable String courseID) {
        List<Assessments> assessments = assessmentService.getAssessments(courseID);
        if (assessments == null || assessments.isEmpty()) {
            return RestBean.failure(404, "没有找到该课程的测评");
        }
        return RestBean.success(assessments);
    }

    /**
     * 创建测评
     *
     * @param assessmentCreateDTO 测评信息
     * @return 创建的测评
     */
    @PostMapping("/create")
    public RestBean<Assessments> createAssessment(@RequestBody AssessmentCreateDTO assessmentCreateDTO) {
        Assessments assessments = assessmentService.createAssessment(assessmentCreateDTO);
        if (assessments == null) {
            return RestBean.failure(500, "创建测评失败");
        }
        return RestBean.success(assessments);
    }

    /**
     * 发布测评
     *
     * @param publishRequest 发布请求对象，包含测评ID
     * @param request        请求
     * @return 发布结果
     */
    @PostMapping("/publish")
    public RestBean<String> publishAssessment(@RequestBody PublishAssessmentRequest publishRequest, HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null || userId.isEmpty()) {
            log.warn("[publishAssessment.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        
        // 参数验证
        if (publishRequest == null || publishRequest.getAssessmentId() == null || publishRequest.getAssessmentId().trim().isEmpty()) {
            log.warn("[publishAssessment.method]测评ID不能为空");
            return RestBean.failure(400, "测评ID不能为空");
        }
        
        boolean result = assessmentService.publishAssessment(userId, publishRequest.getAssessmentId());
        if (!result) {
            return RestBean.failure(500, "发布测评失败");
        }
        return RestBean.success("发布测评成功");
    }

    /**
     * 获取测评详情
     *
     * @param assessmentId 测评ID
     * @return 测评详情
     */
    @GetMapping("/detail")
    public RestBean<Assessments> getAssessmentDetails(String assessmentId) {
        Assessments assessmentDetails = assessmentService.getAssessmentDetails(assessmentId);
        if (assessmentDetails == null) {
            return RestBean.failure(404, "没有找到该测评");
        }
        return RestBean.success(assessmentDetails);
    }

    /**
     * 更新测评
     *
     * @param assessmentId 测评ID
     * @param title        测评标题
     * @param duration     测评时长
     * @param startTime    测评开始时间
     * @param endTime      测评结束时间
     * @param isPublished  是否发布
     * @return 更新的测评
     */
    @PostMapping("/update")
    public RestBean<Assessments> updateAssessment(String assessmentId, String title, int duration, String startTime, String endTime, int isPublished, HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null || userId.isEmpty()) {
            log.warn("[updateAssessment.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        Assessments assessments = assessmentService.updateAssessment(userId, assessmentId, title, duration, startTime, endTime, isPublished);
        if (assessments == null) {
            return RestBean.failure(500, "更新测评失败");
        }
        return RestBean.success(assessments);
    }

    /**
     * 删除测评 - 逻辑处理 - 将is_published = -1
     *
     * @param assessmentId 测评ID
     * @return 删除结果
     */
    @PostMapping("/delete")
    public RestBean<String> deleteAssessment(String assessmentId, HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null || userId.isEmpty()) {
            log.warn("[deleteAssessment.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        boolean result = assessmentService.deleteAssessment(userId, assessmentId);
        if (!result) {
            return RestBean.failure(500, "删除测评失败");
        }
        return RestBean.success("删除测评成功");
    }

    /**
     * 获取试卷结构
     *
     * @param assessmentId 测评ID
     * @return 试卷结构
     */
    @GetMapping("/paper-structure")
    public RestBean<Map<String, Object>> getAssessmentPaperStructure(String assessmentId) {
        Map<String, Object> assessmentPaperStructure = assessmentService.getAssessmentPaperStructure(assessmentId);
        if (assessmentPaperStructure == null || assessmentPaperStructure.isEmpty()) {
            return RestBean.failure(404, "没有找到该测评的试卷结构");
        }
        return RestBean.success(assessmentPaperStructure);
    }

    /**
     * 获取单个测评的结果汇总
     * <p>
     * 返回内容包含：实际参考人数、应参考人数、平均分、完成率。
     * </p>
     *
     * @param assessmentId 测评ID
     * @param request      HTTP请求
     * @return 测评结果汇总
     */
    @GetMapping("/{assessmentId}/result-summary")
    public RestBean<AssessmentResultSummaryDTO> getAssessmentResultSummary(
            @PathVariable String assessmentId,
            HttpServletRequest request
    ) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null || userId.isBlank()) {
            log.warn("[getAssessmentResultSummary.method] 用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        try {
            AssessmentResultSummaryDTO resultSummary = assessmentService.getAssessmentResultSummary(userId, assessmentId);
            return RestBean.success(resultSummary);
        } catch (SecurityException e) {
            log.warn("[getAssessmentResultSummary.method] 权限不足: userId={}, assessmentId={}", userId, assessmentId);
            return RestBean.failure(403, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("[getAssessmentResultSummary.method] 参数或资源异常: assessmentId={}, error={}", assessmentId, e.getMessage());
            return RestBean.failure(404, e.getMessage());
        } catch (Exception e) {
            log.error("[getAssessmentResultSummary.method] 获取测评结果汇总失败: assessmentId={}", assessmentId, e);
            return RestBean.failure(500, "获取测评结果汇总失败");
        }
    }
}
