package com.daox.online.controller.admin;

import com.daox.online.entity.mysql.Courses;
import com.daox.online.entity.RestBean;
import com.daox.online.entity.views.requestVO.admin.AdminCategoryDeleteRequestVO;
import com.daox.online.entity.views.requestVO.admin.AdminCategoryMigrationRequestVO;
import com.daox.online.entity.views.requestVO.admin.AdminCategorySaveVO;
import com.daox.online.entity.views.requestVO.admin.AdminCourseWorkflowRequestVO;
import com.daox.online.entity.views.responseVO.admin.AdminCategoryDeleteResultVO;
import com.daox.online.entity.views.responseVO.admin.AdminCategoryOperationPreviewVO;
import com.daox.online.entity.views.responseVO.course.CourseCategoriesVo;
import com.daox.online.service.AuditLogService;
import com.daox.online.service.CoursesService;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员 - 课程相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/course")
public class CoursesController {

    @Resource
    private CoursesService coursesService;
    @Resource
    private AuditLogService auditLogService;

    /**
     * 获取所有课程
     *
     * @return 课程列表
     */
    @GetMapping("/list")
    public RestBean<List<Courses>> getCourseListAll() {
        List<Courses> courseListAll = coursesService.getCourseListAll();
        if (courseListAll == null || courseListAll.isEmpty()) {
            log.warn("[getCourseListAll.method]课程列表为空");
            return RestBean.failure(404, "课程列表为空");
        }
        return RestBean.success(courseListAll);
    }

    /**
     * 获取全部待审核课程。
     *
     * @return 待审核课程列表
     */
    @GetMapping("/review/pending")
    public RestBean<List<Courses>> listPendingReviewCourses() {
        return RestBean.success(coursesService.listPendingReviewCourses());
    }

    /**
     * 审核通过课程。
     *
     * @param request 审核请求体
     * @param httpServletRequest HTTP请求
     * @return 响应结果
     */
    @PostMapping("/review/approve")
    public RestBean<String> approveCourseReview(@Valid @RequestBody AdminCourseWorkflowRequestVO request,
                                                HttpServletRequest httpServletRequest) {
        String operatorId = UserUtils.getCurrentUserId(httpServletRequest);
        if (operatorId == null) {
            return RestBean.failure(401, "用户未认证");
        }
        coursesService.approveCourseReview(operatorId, request.getCourseId());
        return RestBean.success("课程审核通过");
    }

    /**
     * 驳回课程审核。
     *
     * @param request 审核请求体
     * @param httpServletRequest HTTP请求
     * @return 响应结果
     */
    @PostMapping("/review/reject")
    public RestBean<String> rejectCourseReview(@Valid @RequestBody AdminCourseWorkflowRequestVO request,
                                               HttpServletRequest httpServletRequest) {
        String operatorId = UserUtils.getCurrentUserId(httpServletRequest);
        if (operatorId == null) {
            return RestBean.failure(401, "用户未认证");
        }
        coursesService.rejectCourseReview(operatorId, request.getCourseId(), request.getComment());
        return RestBean.success("课程已驳回并退回草稿");
    }

    /**
     * 下架已发布课程。
     *
     * @param request 状态流转请求体
     * @param httpServletRequest HTTP请求
     * @return 响应结果
     */
    @PostMapping("/take-down")
    public RestBean<String> takeDownCourse(@Valid @RequestBody AdminCourseWorkflowRequestVO request,
                                           HttpServletRequest httpServletRequest) {
        String operatorId = UserUtils.getCurrentUserId(httpServletRequest);
        if (operatorId == null) {
            return RestBean.failure(401, "用户未认证");
        }
        coursesService.takeDownCourse(operatorId, request.getCourseId(), request.getComment());
        return RestBean.success("课程下架成功");
    }

    /**
     * 重新上架已下架课程。
     *
     * @param request 状态流转请求体
     * @param httpServletRequest HTTP请求
     * @return 响应结果
     */
    @PostMapping("/republish")
    public RestBean<String> republishCourse(@Valid @RequestBody AdminCourseWorkflowRequestVO request,
                                            HttpServletRequest httpServletRequest) {
        String operatorId = UserUtils.getCurrentUserId(httpServletRequest);
        if (operatorId == null) {
            return RestBean.failure(401, "用户未认证");
        }
        coursesService.republishCourse(operatorId, request.getCourseId());
        return RestBean.success("课程重新上架成功");
    }

    /**
     * 归档已下架课程。
     *
     * @param request 状态流转请求体
     * @param httpServletRequest HTTP请求
     * @return 响应结果
     */
    @PostMapping("/archive")
    public RestBean<String> archiveCourse(@Valid @RequestBody AdminCourseWorkflowRequestVO request,
                                          HttpServletRequest httpServletRequest) {
        String operatorId = UserUtils.getCurrentUserId(httpServletRequest);
        if (operatorId == null) {
            return RestBean.failure(401, "用户未认证");
        }
        coursesService.archiveCourseByAdmin(operatorId, request.getCourseId(), request.getComment());
        return RestBean.success("课程归档成功");
    }

    /**
     * 删除课程
     *
     * @param courseId 课程ID
     * @return 删除结果
     */
    @PostMapping("/delete")
    public RestBean<String> deleteCourse(@RequestParam("courseId") String courseId) {
        boolean result = coursesService.deleteCourse(courseId);
        if (!result) {
            log.warn("[deleteCourse.method]删除课程失败");
            return RestBean.failure(500, "删除课程失败");
        }
        return RestBean.success("课程删除成功");
    }

    /**
     * 获取课程分类信息 - 通过id查询
     *
     * @param id 分类id
     * @return 分类信息
     */
    @GetMapping("/categories")
    public RestBean<Map<String, Object>> getCourseCategoryById(@RequestParam String id) {
        Map<String, Object> courseCategory = coursesService.getCourseCategoryById(id);
        if (courseCategory == null) return RestBean.failure(404, "课程分类信息不存在!");
        return RestBean.success(courseCategory);
    }

    /**
     * 获取课程分类信息 - 树形结构
     *
     * @return 课程分类 <br />返回一个树形结构的课程分类列表，其中根分类在顶层，子分类嵌套在父分类的 children 列表中
     */
    @GetMapping("/categories/tree")
    public RestBean<List<CourseCategoriesVo>> getCourseCategoryTree() {
        List<CourseCategoriesVo> courseCategoriesTree = coursesService.getCourseCategoryTree();
        if (courseCategoriesTree == null) return RestBean.failure(404, "课程分类信息不存在!");
        return RestBean.success(courseCategoriesTree);
    }

    /**
     * 创建分类
     *
     * @param categorySaveVO 分类维护请求体
     * @return 创建结果
     */
    @PostMapping("/categories/create")
    public RestBean<String> createCourseCategory(@RequestBody AdminCategorySaveVO categorySaveVO) {
        boolean result = coursesService.createCourseCategory(
                categorySaveVO.getName(),
                categorySaveVO.getParentId(),
                categorySaveVO.getOrderIndex());
        if (!result) return RestBean.failure(500, "创建课程分类失败!");
        return RestBean.success("创建课程分类成功!");
    }

    /**
     * 更新分类
     *
     * @param id       分类id
     * @param categorySaveVO 分类维护请求体
     * @return 更新结果
     */
    @PostMapping("/categories/update")
    public RestBean<String> updateCourseCategory(@RequestParam String id,
                                                 @RequestBody AdminCategorySaveVO categorySaveVO) {
        boolean result = coursesService.updateCourseCategory(
                id,
                categorySaveVO.getName(),
                categorySaveVO.getParentId(),
                categorySaveVO.getOrderIndex());
        if (!result) return RestBean.failure(500, "更新课程分类失败!");
        return RestBean.success("更新课程分类成功!");
    }

    /**
     * 删除分类。
     *
     * @param id 分类ID
     * @return 删除结果
     */
    @PostMapping("/categories/delete")
    public RestBean<String> deleteCourseCategory(@RequestParam String id) {
        boolean result = coursesService.deleteCourseCategory(id);
        if (!result) return RestBean.failure(500, "删除课程分类失败，请确认分类下没有子分类和课程");
        return RestBean.success("删除课程分类成功!");
    }

    /**
     * 预览分类删除影响范围。
     *
     * @param id 分类ID
     * @return 预览结果
     */
    @GetMapping("/categories/delete/preview")
    public RestBean<AdminCategoryOperationPreviewVO> previewDeleteCourseCategory(@RequestParam("id") String id) {
        return RestBean.success(coursesService.previewDeleteCourseCategory(id));
    }

    /**
     * 紧急删除分类。
     *
     * @param request 删除请求
     * @param httpServletRequest HTTP请求
     * @return 删除结果
     */
    @PostMapping("/categories/delete/emergency")
    public RestBean<AdminCategoryDeleteResultVO> emergencyDeleteCourseCategory(@Valid @RequestBody AdminCategoryDeleteRequestVO request,
                                                                               HttpServletRequest httpServletRequest) {
        String operatorId = UserUtils.getCurrentUserId(httpServletRequest);
        if (operatorId == null) {
            return RestBean.failure(401, "用户未认证");
        }
        AdminCategoryDeleteResultVO result = coursesService.emergencyDeleteCourseCategory(operatorId, request);
        auditLogService.recordSensitiveOperation(
                "ADMIN_CAT_DEL_EM",
                "course_category",
                result.getCategoryId(),
                buildDeleteAuditSnapshot(operatorId, request),
                buildResultAuditSnapshot(result),
                "SUCCESS",
                "管理员通过接口执行课程分类紧急删除");
        return RestBean.success(result);
    }

    /**
     * 提交常规删除申请。
     *
     * @param request 删除请求
     * @param httpServletRequest HTTP请求
     * @return 删除结果
     */
    @PostMapping("/categories/delete/regular")
    public RestBean<AdminCategoryDeleteResultVO> regularDeleteCourseCategory(@Valid @RequestBody AdminCategoryDeleteRequestVO request,
                                                                             HttpServletRequest httpServletRequest) {
        String operatorId = UserUtils.getCurrentUserId(httpServletRequest);
        if (operatorId == null) {
            return RestBean.failure(401, "用户未认证");
        }
        AdminCategoryDeleteResultVO result = coursesService.regularDeleteCourseCategory(operatorId, request);
        auditLogService.recordSensitiveOperation(
                "ADMIN_CAT_DEL_RG",
                "course_category",
                result.getCategoryId(),
                buildDeleteAuditSnapshot(operatorId, request),
                buildResultAuditSnapshot(result),
                "SUCCESS",
                "管理员通过接口提交课程分类常规删除");
        return RestBean.success(result);
    }

    /**
     * 预览分类迁移影响范围。
     *
     * @param id 分类ID
     * @return 预览结果
     */
    @GetMapping("/categories/migration/preview")
    public RestBean<AdminCategoryOperationPreviewVO> previewCategoryMigration(@RequestParam("id") String id) {
        return RestBean.success(coursesService.previewCategoryMigration(id));
    }

    /**
     * 执行分类迁移。
     *
     * @param request 迁移请求
     * @param httpServletRequest HTTP请求
     * @return 迁移结果
     */
    @PostMapping("/categories/migration/execute")
    public RestBean<AdminCategoryDeleteResultVO> migrateCategoryCourses(@Valid @RequestBody AdminCategoryMigrationRequestVO request,
                                                                        HttpServletRequest httpServletRequest) {
        String operatorId = UserUtils.getCurrentUserId(httpServletRequest);
        if (operatorId == null) {
            return RestBean.failure(401, "用户未认证");
        }
        AdminCategoryDeleteResultVO result = coursesService.migrateCategoryCourses(operatorId, request);
        auditLogService.recordSensitiveOperation(
                "ADMIN_CAT_MIG",
                "course_category",
                result.getCategoryId(),
                buildMigrationAuditSnapshot(operatorId, request),
                buildResultAuditSnapshot(result),
                "SUCCESS",
                "管理员通过接口执行课程分类迁移");
        return RestBean.success(result);
    }

    /**
     * 构建删除请求审计快照。
     *
     * @param operatorId 操作人ID
     * @param request 删除请求
     * @return 审计快照
     */
    private Map<String, Object> buildDeleteAuditSnapshot(String operatorId, AdminCategoryDeleteRequestVO request) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("operatorId", operatorId);
        snapshot.put("categoryId", request.getCategoryId());
        snapshot.put("reason", request.getReason());
        return snapshot;
    }

    /**
     * 构建迁移请求审计快照。
     *
     * @param operatorId 操作人ID
     * @param request 迁移请求
     * @return 审计快照
     */
    private Map<String, Object> buildMigrationAuditSnapshot(String operatorId, AdminCategoryMigrationRequestVO request) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("operatorId", operatorId);
        snapshot.put("sourceCategoryId", request.getSourceCategoryId());
        snapshot.put("targetCategoryId", request.getTargetCategoryId());
        snapshot.put("reason", request.getReason());
        return snapshot;
    }

    /**
     * 构建分类操作结果审计快照。
     *
     * @param result 操作结果
     * @return 审计快照
     */
    private Map<String, Object> buildResultAuditSnapshot(AdminCategoryDeleteResultVO result) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("requestId", result.getRequestId());
        snapshot.put("operationType", result.getOperationType());
        snapshot.put("mode", result.getMode());
        snapshot.put("categoryId", result.getCategoryId());
        snapshot.put("categoryName", result.getCategoryName());
        snapshot.put("announcementId", result.getAnnouncementId());
        snapshot.put("effectiveAt", result.getEffectiveAt());
        snapshot.put("preserveUntil", result.getPreserveUntil());
        snapshot.put("message", result.getMessage());
        return snapshot;
    }

}
