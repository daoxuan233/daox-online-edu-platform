package com.daox.online.controller.teacher;

import com.daox.online.entity.mysql.Courses;
import com.daox.online.entity.RestBean;
import com.daox.online.entity.dto.CourseCoreInfoDto;
import com.daox.online.entity.dto.CourseOutlineDto;
import com.daox.online.entity.views.requestVO.teacher.CoursePropertiesVo;
import com.daox.online.entity.views.requestVO.teacher.TeacherCourseVo;
import com.daox.online.entity.views.responseVO.EnrolledStudentVO;
import com.daox.online.entity.views.responseVO.FileUploadResponseVO;
import com.daox.online.entity.views.responseVO.course.TeacherCourseDetailVo;
import com.daox.online.entity.views.responseVO.user.UserCoursesVo;
import com.daox.online.mapper.CoursesMapper;
import com.daox.online.service.CoursesService;
import com.daox.online.service.FileStorageService;
import com.daox.online.uilts.FileUtils;
import com.daox.online.uilts.JSONUtil;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.UUID;

/**
 * 教师端 - 课程相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/courses")
public class TeacherCoursesController {

    @Resource
    private CoursesService coursesService;

    @Resource
    private FileUtils fileUtils;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private CoursesMapper coursesMapper;

    /**
     * 获取当前用户课程列表
     *
     * @param request 请求对象
     * @return 课程列表
     */
    @RequestMapping("/list")
    public RestBean<List<UserCoursesVo>> getMyCourseList(HttpServletRequest request) {
        // 获取当前用户ID（用于日志记录和权限验证）
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[getMyCourseList.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        List<UserCoursesVo> myCourseList = coursesService.getTeacherCourseList(userId);
        if (myCourseList.isEmpty()) {
            log.warn("[getMyCourseList.method]用户没有课程");
            return RestBean.forbidden("用户没有课程");
        }
        return RestBean.success(myCourseList);
    }

    /**
     * 创建课程 (课程主体 + 课程属性)
     *
     * @param courseCoreInfo 包含课程核心信息的 DTO
     * @return 创建结果
     */
    @PostMapping
    public RestBean<Courses> createCourse(@RequestBody CourseCoreInfoDto courseCoreInfo, HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[createCourse.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        return RestBean.success(coursesService.createCourse(userId, courseCoreInfo));
    }

    /**
     * 更新指定课程的核心信息
     *
     * @param courseId       要更新的课程的 ID (从 URL 路径中获取)
     * @param courseCoreInfo 包含要更新的课程核心信息的 DTO
     * @return 返回更新后的课程信息
     */
    @PostMapping("/{courseId}")
    public RestBean<?> updateCourseCoreInfo(@PathVariable String courseId,
                                            @RequestBody CourseCoreInfoDto courseCoreInfo,
                                            HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[updateCourseCoreInfo.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        return RestBean.success(coursesService.updateCourseCoreInfo(userId, courseId, courseCoreInfo));
    }

    /**
     * 全量更新指定课程的完整大纲 (章节 + 小节)
     * 采用“先删除旧大纲，再插入新大纲”的策略
     *
     * @param courseId   要更新大纲的课程的 ID
     * @param outlineDto 包含新的完整大纲结构的 DTO
     * @return 返回操作成功的消息
     */
    @PostMapping("/{courseId}/outline")
    public RestBean<?> updateCourseOutline(@PathVariable String courseId,
                                           @RequestBody CourseOutlineDto outlineDto,
                                           HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[updateCourseOutline.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        coursesService.updateCourseOutline(userId, courseId, outlineDto);
        return RestBean.success("更新课程大纲成功！");
    }

    /**
     * 获取课程大纲
     *
     * @param courseId 课程ID
     */
    @GetMapping("/{courseId}/outline")
    public RestBean<CourseOutlineDto> getCourseOutline(@PathVariable String courseId) {
        CourseOutlineDto courseOutline = coursesService.getCourseOutline(courseId);
        if (courseOutline != null) {
            return RestBean.success(courseOutline);
        } else {
            return RestBean.failure(500, "获取课程大纲失败");
        }
    }

    /**
     * 修改课程封面
     *
     * @param coverUrl 课程封面图片URL
     * @param courseId 课程ID
     * @param request  请求
     * @return 修改成功
     */
    @PostMapping("/update/cover")
    public RestBean<String> saveUploadCover(@RequestParam("coverUrl") String coverUrl,
                                            @RequestParam("courseId") String courseId,
                                            HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[uploadAvatar.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        int i = coursesMapper.updateCover(courseId, coverUrl, userId);
        if (i > 0) return RestBean.success("修改成功！");
        return RestBean.failure(500, "修改失败！");
    }

    /**
     * 获取课程详情
     *
     * @param courseId 课程id
     * @return 课程详情
     */
    @PostMapping("/detail")
    public RestBean<TeacherCourseDetailVo> getCourseDetail(String courseId) {
        TeacherCourseDetailVo courseDetailWithCategories = coursesService.getCourseDetailWithCategories(courseId);
        if (courseDetailWithCategories == null) return RestBean.failure(404, "课程不存在！");
        return RestBean.success(courseDetailWithCategories);
    }

    /**
     * 更新课程信息
     *
     * @param request            请求
     * @param courseId           课程id
     * @param courseVo           课程信息
     * @param coursePropertiesVo 课程属性信息
     * @return 修改成功
     */
    @PostMapping("/course/properties")
    public RestBean<String> updateCourse(HttpServletRequest request, String courseId, String courseVo, String coursePropertiesVo) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            log.warn("[updateCourse.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }

        TeacherCourseVo courseVoBean = JSONUtil.toBean(courseVo, TeacherCourseVo.class);
        log.info("[updateCourse.method]课程信息:{}", courseVoBean.toString());
        CoursePropertiesVo coursePropertiesVoBean = JSONUtil.toBean(coursePropertiesVo, CoursePropertiesVo.class);
        log.info("[updateCourse.method]课程属性信息:{}", coursePropertiesVoBean.toString());

        String string = coursesService.updateCourse(userId, courseId, courseVoBean, coursePropertiesVoBean);
        return RestBean.success(string);
    }

    /**
     * 删除课程
     *
     * @param courseId 课程ID
     * @param request  请求
     * @return 删除结果
     */
    @PostMapping("/course/delete")
    public RestBean<String> deleteCourse(@RequestParam("courseId") String courseId, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            log.warn("[deleteCourse.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        boolean result = coursesService.deleteCourse(userId, courseId);
        return result ? RestBean.success("课程删除成功") : RestBean.failure(500, "课程删除失败");
    }

    /**
     * 提交课程审核。
     * <p>
     * 保留旧的 /course/publish 路径作为兼容入口，但其语义已调整为“提交审核”，
     * 不再允许教师直接把课程改为已发布。
     * </p>
     *
     * @param courseId 课程ID
     * @param request  请求
     * @return 响应
     */
    @PostMapping({"/course/review/submit", "/course/publish"})
    public RestBean<String> submitCourseForReview(@RequestParam("courseId") String courseId, HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[submitCourseForReview.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        coursesService.submitCourseForReview(userId, courseId);
        log.info("[submitCourseForReview.method]提交课程审核成功: userId={}, courseId={}", userId, courseId);
        return RestBean.success("课程已提交审核");
    }

    /**
     * 教师归档草稿课程。
     *
     * @param courseId 课程ID
     * @param request  请求
     * @return 响应
     */
    @PostMapping("/course/archive")
    public RestBean<String> archiveCourse(@RequestParam("courseId") String courseId, HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[archiveCourse.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        coursesService.archiveCourseByTeacher(userId, courseId);
        return RestBean.success("归档课程成功");
    }

    /**
     * 获取选课学生
     *
     * @param courseId 课程ID
     * @param request  请求
     * @return 选课学生列表
     */
    @PostMapping("/course/students")
    public RestBean<List<EnrolledStudentVO>> getStudentList(@RequestParam("courseId") String courseId, HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[getStudentList.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        List<EnrolledStudentVO> studentList = coursesService.getStudentList(userId, courseId);
        if (studentList == null || studentList.isEmpty()) {
            log.warn("[getStudentList.method]获取学生列表失败: userId={}, courseId={}", userId, courseId);
            return RestBean.failure(500, "获取学生列表失败");
        }
        log.info("[getStudentList.method]获取学生列表成功: userId={}, courseId={}", userId, courseId);
        return RestBean.success(studentList);
    }

    /**
     * 上传课程封面
     *
     * @param file    文件
     * @param request 请求
     * @return 文件上传响应
     */
    @PostMapping("/upload/cover")
    public RestBean<FileUploadResponseVO> uploadCover(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[uploadCover.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        try {
            // 参数校验
            if (file.isEmpty()) {
                return RestBean.failure(400, "上传的封面文件不能为空");
            }

            if (userId.trim().isEmpty()) {
                return RestBean.unauthorized("用户ID不能为空");
            }

            // 文件大小校验（10MB限制）
            long maxFileSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxFileSize) {
                return RestBean.failure(400, "上传的文件过大，请确保文件大小不超过10MB");
            }

            // 获取原始文件名和扩展名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                return RestBean.failure(400, "文件名不能为空");
            }

            String fileExtension = "";
            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex > 0) {
                fileExtension = originalFilename.substring(lastDotIndex);
            }

            // 文件类型验证
            String contentType = file.getContentType();
            if (!fileUtils.isAllowedAvatarFileType(contentType, fileExtension)) {
                return RestBean.failure(400, "不支持的头像文件类型: " + (contentType != null ? contentType : "未知") + ", 扩展名: " + fileExtension);
            }

            // 生成唯一文件名：avatar_用户ID_UUID.扩展名
            String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            String newFileName = "cover_" + userId + "_" + uniqueId + fileExtension;

            // 构建对象存储路径：avatars/文件名
            String objectName = "cover/" + newFileName;

            // 上传文件到MinIO
            String storedObjectName = fileStorageService.uploadFile(file.getInputStream(), objectName, file.getContentType(), file.getSize());

            // 生成临时下载URL（7天有效期）
            URL downloadUrl = fileStorageService.generatePresignedDownloadUrl(storedObjectName, 7 * 24 * 3600);

            // 生成永久访问链接（通过应用服务器代理）
            String permanentUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/api/public/file/" + storedObjectName;

            // 构建响应对象
            FileUploadResponseVO response = new FileUploadResponseVO().setOriginalFilename(originalFilename).setStoredFilename(newFileName).setObjectName(storedObjectName).setDownloadUrl(downloadUrl.toString()).setPermanentUrl(permanentUrl).setFileType("avatar").setFileSize(file.getSize()).setContentType(file.getContentType()).setUserId(userId);

            log.info("头像上传成功: 原文件名={}, 存储路径={}, 用户ID={}, 客户端IP={}", originalFilename, storedObjectName, userId, request.getRemoteAddr());

            return RestBean.success(response);

        } catch (Exception e) {
            log.error("头像上传失败: 原文件名={}, 用户ID={}, 错误信息={}", file.getOriginalFilename(), userId, e.getMessage(), e);
            return RestBean.failure(500, "头像上传失败: " + e.getMessage());
        }
    }


}
