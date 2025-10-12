package com.daox.online.controller;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.views.UpdatePasswordVO;
import com.daox.online.entity.views.requestVO.ConfirmResetVO;
import com.daox.online.entity.views.requestVO.EmailRegisterVO;
import com.daox.online.entity.views.requestVO.EmailResetVO;
import com.daox.online.entity.views.responseVO.*;
import com.daox.online.entity.views.responseVO.course.CourseCategoriesVo;
import com.daox.online.entity.views.responseVO.course.CourseVo;
import com.daox.online.entity.views.responseVO.ratings.CourseRatingStatisticsVO;
import com.daox.online.entity.views.responseVO.ratings.RatingPermissionVO;
import com.daox.online.entity.views.responseVO.ratings.TeacherRatingStatisticsVO;
import com.daox.online.service.*;
import com.daox.online.uilts.FileUtils;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.*;
import java.util.function.Supplier;

/**
 * 公共API控制器
 * 提供文件上传和下载等公共接口
 */
@Slf4j
@RestController
@RequestMapping("/api/public")
public class PublicAPIController {

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private CoursesService coursesService;

    @Resource
    private SystemAnnouncementsService systemAnnouncementsService;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private RatingsService ratingsService;

    @Resource
    private FileUtils fileUtils;

    @Resource
    private CourseCategoriesService courseCategoriesService;

    /**
     * 文件上传接口
     * 支持多种文件类型，自动根据文件类型创建对应文件夹
     * 文件命名格式：原文件名_上传者ID_唯一标识.扩展名
     *
     * @param file    上传的文件
     * @param userId  上传者ID
     * @param request HTTP请求对象
     * @return 上传结果，包含文件访问URL
     */
    @PostMapping("/upload")
    public RestBean<FileUploadResponseVO> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("userId") String userId, HttpServletRequest request) {

        try {
            // 参数校验
            if (file.isEmpty()) {
                return RestBean.failure(400, "上传文件不能为空");
            }

            if (userId == null || userId.trim().isEmpty()) {
                return RestBean.failure(400, "用户ID不能为空");
            }

            // 获取原始文件名和扩展名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                return RestBean.failure(400, "文件名不能为空");
            }

            // 提取文件扩展名
            String fileExtension = "";
            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex > 0) {
                fileExtension = originalFilename.substring(lastDotIndex);
            }

            // 文件类型验证
            String contentType = file.getContentType();
            if (!isAllowedFileType(contentType, fileExtension)) {
                return RestBean.failure(400, "不支持的文件类型: " + (contentType != null ? contentType : "未知") + ", 扩展名: " + fileExtension);
            }

            // 根据文件类型确定存储文件夹
            String fileType = determineFileType(file.getContentType(), fileExtension);

            // 生成唯一文件名：原文件名_上传者ID_UUID.扩展名
            String baseFileName = originalFilename;
            if (lastDotIndex > 0) {
                baseFileName = originalFilename.substring(0, lastDotIndex);
            }
            String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            String newFileName = baseFileName + "_" + userId + "_" + uniqueId + fileExtension;

            // 构建对象存储路径：文件类型/文件名
            String objectName = fileType + "/" + newFileName;

            // 上传文件到MinIO
            String storedObjectName = fileStorageService.uploadFile(file.getInputStream(), objectName, file.getContentType(), file.getSize());

            // 生成临时下载URL（7天有效期）
            URL downloadUrl = fileStorageService.generatePresignedDownloadUrl(storedObjectName, 7 * 24 * 3600);

            // 生成永久访问链接（通过应用服务器代理）
            String permanentUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/api/public/file/" + storedObjectName;

            // 构建响应对象
            FileUploadResponseVO response = new FileUploadResponseVO().setOriginalFilename(originalFilename).setStoredFilename(newFileName).setObjectName(storedObjectName).setDownloadUrl(downloadUrl.toString()).setPermanentUrl(permanentUrl).setFileType(fileType).setFileSize(file.getSize()).setContentType(file.getContentType()).setUserId(userId);

            log.info("文件上传成功: 原文件名={}, 存储路径={}, 用户ID={}, 客户端IP={}", originalFilename, storedObjectName, userId, request.getRemoteAddr());

            return RestBean.success(response);

        } catch (Exception e) {
            log.error("文件上传失败: 原文件名={}, 用户ID={}, 错误信息={}", file.getOriginalFilename(), userId, e.getMessage(), e);
            return RestBean.failure(500, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 文件下载接口
     * 生成临时下载链接
     *
     * @param objectName 文件在存储系统中的对象名称
     * @param duration   链接有效期（秒），默认1小时
     * @return 下载链接
     */
    @GetMapping("/download")
    public RestBean<String> generateDownloadUrl(@RequestParam("objectName") String objectName, @RequestParam(value = "duration", defaultValue = "3600") int duration) {

        try {
            // 参数校验
            if (objectName == null || objectName.trim().isEmpty()) {
                return RestBean.failure(400, "文件对象名称不能为空");
            }

            if (duration <= 0 || duration > 7 * 24 * 3600) { // 最大7天
                return RestBean.failure(400, "链接有效期必须在1秒到7天之间");
            }

            // 生成下载链接
            URL downloadUrl = fileStorageService.generatePresignedDownloadUrl(objectName, duration);

            log.info("生成下载链接成功: 对象名称={}, 有效期={}秒", objectName, duration);

            return RestBean.success(downloadUrl.toString());

        } catch (Exception e) {
            log.error("生成下载链接失败: 对象名称={}, 错误信息={}", objectName, e.getMessage(), e);
            return RestBean.failure(500, "生成下载链接失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件MIME类型和扩展名确定文件类型文件夹
     *
     * @param contentType   文件MIME类型
     * @param fileExtension 文件扩展名
     * @return 文件类型文件夹名称
     */
    private String determineFileType(String contentType, String fileExtension) {
        if (contentType == null) {
            contentType = "";
        }

        String lowerContentType = contentType.toLowerCase();
        String lowerExtension = fileExtension.toLowerCase();

        // 图片文件
        if (lowerContentType.startsWith("image/") || lowerExtension.matches("\\.(jpg|jpeg|png|gif|bmp|webp|svg)$")) {
            return "images";
        }

        // 视频文件
        if (lowerContentType.startsWith("video/") || lowerExtension.matches("\\.(mp4|avi|mov|wmv|flv|webm|mkv)$")) {
            return "videos";
        }

        // 音频文件
        if (lowerContentType.startsWith("audio/") || lowerExtension.matches("\\.(mp3|wav|flac|aac|ogg|wma)$")) {
            return "audios";
        }

        // 文档文件
        if (lowerContentType.contains("pdf") || lowerContentType.contains("document") || lowerContentType.contains("text") || lowerContentType.contains("spreadsheet") || lowerContentType.contains("presentation") || lowerExtension.matches("\\.(pdf|doc|docx|xls|xlsx|ppt|pptx|txt|rtf|odt|ods|odp)$")) {
            return "documents";
        }

        // 压缩文件
        if (lowerContentType.contains("zip") || lowerContentType.contains("rar") || lowerContentType.contains("compressed") || lowerExtension.matches("\\.(zip|rar|7z|tar|gz|bz2)$")) {
            return "archives";
        }

        // 其他文件
        return "others";
    }

    /**
     * 永久文件访问接口
     * 通过应用服务器代理访问MinIO中的文件
     *
     * @param response HTTP响应对象
     */
    @GetMapping("/file/**")
    public void getFile(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 从请求URI中提取完整的文件路径
            String requestURI = request.getRequestURI();
            String contextPath = request.getContextPath();
            String fullPath = requestURI.substring(contextPath.length());

            // 移除 "/api/public/file/" 前缀，获取实际的对象名称
            String prefix = "/api/public/file/";
            if (!fullPath.startsWith(prefix)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("无效的文件路径");
                return;
            }

            String objectName = fullPath.substring(prefix.length());

            // 参数校验
            if (objectName == null || objectName.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("文件对象名称不能为空");
                return;
            }

            // 生成临时下载链接（1小时有效期）
            URL downloadUrl = fileStorageService.generatePresignedDownloadUrl(objectName, 3600);

            // 重定向到MinIO的预签名URL
            response.sendRedirect(downloadUrl.toString());

            log.info("永久文件访问成功: 对象名称={}", objectName);

        } catch (Exception e) {
            // 从请求URI中提取对象名称用于日志记录
            String objectName = "未知";
            try {
                String requestURI = request.getRequestURI();
                String contextPath = request.getContextPath();
                String fullPath = requestURI.substring(contextPath.length());
                String prefix = "/api/public/file/";
                if (fullPath.startsWith(prefix)) {
                    objectName = fullPath.substring(prefix.length());
                }
            } catch (Exception ignored) {
                // 忽略提取对象名称时的异常
            }

            log.error("永久文件访问失败: 对象名称={}, 错误信息={}", objectName, e.getMessage(), e);
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("文件访问失败: " + e.getMessage());
            } catch (Exception ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }

    /**
     * 验证文件类型是否被允许
     *
     * @param contentType   文件MIME类型
     * @param fileExtension 文件扩展名
     * @return 是否允许上传
     */
    private boolean isAllowedFileType(String contentType, String fileExtension) {
        // 定义允许的MIME类型
        Set<String> allowedMimeTypes = new HashSet<>(Arrays.asList(
                // 图片类型
                "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp", "image/svg+xml",
                // 视频类型
                "video/mp4", "video/avi", "video/quicktime", "video/x-msvideo", "video/x-ms-wmv", "video/x-flv", "video/webm", "video/x-matroska",
                // 音频类型
                "audio/mpeg", "audio/mp3", "audio/wav", "audio/flac", "audio/aac", "audio/ogg", "audio/x-ms-wma",
                // 文档类型
                "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "text/plain", "application/rtf",
                // 压缩文件
                "application/zip", "application/x-rar-compressed", "application/vnd.rar", "application/x-7z-compressed", "application/x-tar", "application/gzip"));

        // 定义允许的文件扩展名
        Set<String> allowedExtensions = new HashSet<>(Arrays.asList(
                // 图片扩展名
                ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg",
                // 视频扩展名
                ".mp4", ".avi", ".mov", ".wmv", ".flv", ".webm", ".mkv",
                // 音频扩展名
                ".mp3", ".wav", ".flac", ".aac", ".ogg", ".wma",
                // 文档扩展名
                ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".rtf",
                // 压缩文件扩展名
                ".zip", ".rar", ".7z", ".tar", ".gz"));

        // 检查MIME类型
        boolean mimeTypeAllowed = contentType != null && allowedMimeTypes.contains(contentType.toLowerCase());

        // 检查文件扩展名
        boolean extensionAllowed = fileExtension != null && allowedExtensions.contains(fileExtension.toLowerCase());

        // 只要其中一个匹配就允许（考虑到某些情况下MIME类型可能不准确）
        return mimeTypeAllowed || extensionAllowed;
    }

    /**
     * 分页查询公开课程列表
     *
     * @param pageSize 页大小，默认为10
     * @param offset   偏移量，默认为0
     * @return 公开课程列表
     */
    @GetMapping("/courses")
    public RestBean<List<CourseVo>> publicCourseList(@RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "offset", defaultValue = "0") int offset) {
        // 调用服务层获取公开课程列表
        List<CourseVo> courseList = coursesService.publicCourseList(pageSize, offset);
        if (courseList == null || courseList.isEmpty()) return RestBean.failure(404, "公开课程信息不存在!");
        return RestBean.success(courseList);
    }

    /**
     * 获取课程基本信息
     *
     * @param courseId 课程ID
     * @return 课程基本信息
     */
    @GetMapping("/courses/{courseId}")
    public RestBean<CourseVo> getCourseDetail(@PathVariable("courseId") String courseId) {
        CourseVo courseDetail = coursesService.getCourseDetail(courseId);
        if (courseDetail == null) return RestBean.failure(404, "课程信息不存在!");
        return RestBean.success(courseDetail);
    }

    /**
     * 获取课程分类信息 - 树形结构
     *
     * @return 课程分类 <br />返回一个树形结构的课程分类列表，其中根分类在顶层，子分类嵌套在父分类的 children 列表中
     */
    @GetMapping("/categories")
    public RestBean<List<CourseCategoriesVo>> getCourseCategoryTree() {
        List<CourseCategoriesVo> courseCategoriesTree = coursesService.getCourseCategoryTree();
        if (courseCategoriesTree == null) return RestBean.failure(404, "课程分类信息不存在!");
        return RestBean.success(courseCategoriesTree);
    }

    /**
     * 获取课程分类信息 - 树形结构 - 用于前端组件
     *
     * @return 课程分类树形结构
     */
    @GetMapping("/categories/tree")
    public RestBean<List<CategoryVo>> getCategoryTree() {
        List<CategoryVo> categoryTree = courseCategoriesService.getCategoryTree();
        if (categoryTree == null) return RestBean.failure(404, "课程分类信息不存在!");
        return RestBean.success(categoryTree);
    }

    /**
     * 获取系统公告
     *
     * @param pageNum  页码 默认1
     * @param pageSize 每页数量 默认10
     * @return 系统公告列表
     */
    @GetMapping("/announcements")
    public RestBean<List<SystemAnnouncementsVo>> getSystemAnnouncements(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        List<SystemAnnouncementsVo> systemAnnouncements = systemAnnouncementsService.getSystemAnnouncements(pageNum, pageSize);
        if (systemAnnouncements == null) return RestBean.failure(404, "系统公告不存在!");
        return RestBean.success(systemAnnouncements);
    }

    /**
     * 上传头像
     *
     * @param file    头像文件
     * @param request HTTP请求对象
     * @return 包含头像URL的响应对象
     */
    @PostMapping("/upload/avatar")
    public RestBean<FileUploadResponseVO> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[uploadAvatar.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        try {
            // 参数校验
            if (file.isEmpty()) {
                return RestBean.failure(400, "上传的头像文件不能为空");
            }

            if (userId.trim().isEmpty()) {
                return RestBean.failure(400, "用户ID不能为空");
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
            String newFileName = "avatar_" + userId + "_" + uniqueId + fileExtension;

            // 构建对象存储路径：avatars/文件名
            String objectName = "avatars/" + newFileName;

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

    /**
     * 请求邮箱验证码
     *
     * @param email   请求邮箱
     * @param type    验证码类型 (使用 @Pattern 限制为 "register" 、 "reset"、""activate)。
     * @param request 请求 HttpServletRequest 用于获取客户端 IP 地址进行频率限制。
     * @return 是否请求成功
     */
    @GetMapping("/auth/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam @Email(message = "邮箱格式不正确") String email, @RequestParam @Pattern(regexp = "(register|reset|activate)", message = "无效的验证码类型") String type, HttpServletRequest request) {
        return this.messageHandle(() -> sysUserService.registerEmailVerifyCode(type, String.valueOf(email), request.getRemoteAddr()));
    }

    /**
     * 用户注册操作，需要先请求邮件验证码
     *
     * @param vo 注册信息
     * @return 是否注册成功
     */
    @PostMapping("/auth/register")
    public RestBean<Void> resetConfirm(@RequestBody @Valid EmailRegisterVO vo) {
        return this.messageHandle(() -> sysUserService.registerEmailAccount(vo));
    }

    /**
     * 执行密码重置确认，检查验证码是否正确
     *
     * @param vo 重置信息
     * @return 是否操作成功
     */
    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@RequestParam @Valid ConfirmResetVO vo) {
        return this.messageHandle(() -> sysUserService.resetConfirm(vo));
    }

    /**
     * 重置密码，需要先执行密码重置确认
     *
     * @param vo 重置信息
     * @return 是否操作成功
     */
    @PostMapping("/reset-password")
    public RestBean<Void> resetPassword(@RequestBody @Valid EmailResetVO vo) {
        return this.messageHandle(() -> sysUserService.resetEmailAccountPassword(vo));
    }

    /**
     * 针对返回值为string作为错误的方法进行统一封装
     *
     * @param action 具体的操作
     * @param <T>    相应结果类型
     * @return 返回结果
     */
    private <T> RestBean<T> messageHandle(Supplier<String> action) {
        String message = action.get();
        if (message == null) {
            return RestBean.success();
        } else return RestBean.failure(400, message);
    }

    /**
     * 修改密码
     *
     * @param vo 修改密码信息
     * @return 修改结果
     */
    @PostMapping("/update-password")
    public RestBean<Void> updatePassword(@Valid @RequestBody UpdatePasswordVO vo) {
        // 检查用户是否已登录
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return RestBean.failure(401, "请先登录");
        }
        return this.messageHandle(() -> sysUserService.updatePassword(vo));
    }

    /**
     * 获取课程评价统计信息
     *
     * @param courseId 课程ID
     * @return 课程评价统计信息
     */
    @GetMapping("/courses/ratings/statistics")
    public RestBean<CourseRatingStatisticsVO> getCourseRatingStatistics(@RequestParam String courseId) {
        CourseRatingStatisticsVO courseRatingStatistics = ratingsService.getCourseRatingStatistics(courseId);
        if (courseRatingStatistics == null) {
            return RestBean.failure(404, "课程不存在");
        }
        return RestBean.success(courseRatingStatistics);
    }

    /**
     * 获取讲师评分统计
     *
     * @param teacherId 讲师ID
     * @return 讲师评分统计信息
     */
    @GetMapping("/teachers/ratings/statistics")
    public RestBean<TeacherRatingStatisticsVO> getTeacherRatingStatistics(@RequestParam String teacherId) {
        TeacherRatingStatisticsVO teacherRatingStatistics = ratingsService.getTeacherRatingStatistics(teacherId);
        if (teacherRatingStatistics == null) {
            return RestBean.failure(404, "讲师不存在");
        }
        return RestBean.success(teacherRatingStatistics);
    }

    /**
     * 获取热门课程排行
     *
     * @param limit      返回数量限制，默认20，最大100
     * @param minRatings 最少评分数量要求，默认10
     * @return 热门课程列表
     */
    @GetMapping("/courses/top-rated")
    public RestBean<List<TopRatedCourseVO>> getTopRatedCourses(@RequestParam(defaultValue = "20") Integer limit, @RequestParam(defaultValue = "10") Integer minRatings) {
        try {
            // 参数验证
            if (limit > 100) {
                limit = 100;
            }
            if (limit < 1) {
                limit = 20;
            }
            if (minRatings < 0) {
                minRatings = 10;
            }

            List<TopRatedCourseVO> courses = ratingsService.getTopRatedCourses(limit, minRatings);
            return RestBean.success(courses);
        } catch (Exception e) {
            return RestBean.failure(500, "获取热门课程排行失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户评分权限
     *
     * @param courseId 课程ID
     * @param request  HTTP请求对象
     * @return 评分权限信息
     */
    @GetMapping("/courses/ratings/permission")
    public RestBean<RatingPermissionVO> checkRatingPermission(@RequestParam String courseId, HttpServletRequest request) {
        try {
            String userId = UserUtils.getCurrentUserId(request);
            if (userId == null) {
                return RestBean.failure(401, "用户未登录");
            }

            RatingPermissionVO permission = ratingsService.checkRatingPermission(userId, courseId);
            return RestBean.success(permission);
        } catch (Exception e) {
            return RestBean.failure(500, "检查评分权限失败: " + e.getMessage());
        }
    }


}
