package com.daox.online.controller.students;

import com.daox.online.entity.mysql.CourseMaterials;
import com.daox.online.entity.RestBean;
import com.daox.online.entity.dto.ProgressUpdateRequestDto;
import com.daox.online.entity.mongodb.LearningNotes;
import com.daox.online.entity.mongodb.dto.CreateNoteDTO;
import com.daox.online.entity.mongodb.dto.UpdateNoteDTO;
import com.daox.online.entity.views.responseVO.course.CourseContentDto;
import com.daox.online.service.CoursesService;
import com.daox.online.service.FileService;
import com.daox.online.service.Impl.ChaptersANDSectionsServiceImpl;
import com.daox.online.service.LearningProgressService;
import com.daox.online.service.ProgressService;
import com.daox.online.uilts.UserUtils;
import com.daox.online.uilts.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学生端 -  学习记录相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/student/learning")
public class LearningController {

    @Resource
    private ChaptersANDSectionsServiceImpl chaptersANDSectionsService;

    @Resource
    private LearningProgressService learningProgressService;

    @Resource
    private CoursesService coursesService;

    @Resource
    private ProgressService progressService;

    @Resource
    private FileService fileService;

    @Resource
    private JwtUtils jwtUtils;

    /**
     * 获取视频播放地址
     *
     * @param sectionId 小节ID
     * @return Map : String -> 视频播放地址 , Integer -> 视频时长
     */
    @GetMapping("/video")
    public RestBean<Map<String, Integer>> getVideoPlayUrl(@RequestParam("sectionId") String sectionId) {
        log.info("getVideoPlayUrl: sectionId={}", sectionId);
        Map<String, Integer> result = chaptersANDSectionsService.getVideoPlayUrl(sectionId);
        if (result == null) {
            return RestBean.failure(404, "视频不存在");
        }
        return RestBean.success(result);
    }

    /**
     * 学生端更新视频学习进度接口
     * 前端应定期或在关键事件（暂停、关闭）时调用此接口。
     *
     * @param request 包含小节ID和当前观看秒数的请求体。
     * @return 成功的响应。
     */
    @PostMapping("/update")
    public RestBean<Void> updateProgress(@Valid @RequestBody ProgressUpdateRequestDto request, HttpServletRequest httpServletRequest) {
        String currentUserId = UserUtils.getCurrentUserId(httpServletRequest);
        if (currentUserId == null || currentUserId.isEmpty()) {
            log.warn("[updateProgress.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        progressService.updateVideoProgress(currentUserId, request.getSectionId(), request.getProgressSeconds());
        log.info("[updateProgress.method]更新进度成功,progressSeconds={}", request.getProgressSeconds());
        return RestBean.success();
    }

    /**
     * 学生端统一文件访问接口（触发学习进度记录）。
     * 支持从URL参数中获取token进行认证。
     */
    @GetMapping("/files/{fileId}")
    public void getFile(@PathVariable String fileId,
                        @RequestParam(value = "token", required = false) String token,
                        HttpServletRequest request, HttpServletResponse response) {
        String currentUserId = getCurrentUserIdWithTokenSupport(request, token);
        if (currentUserId == null) {
            log.warn("[getFile.method]用户未认证，fileId: {}", fileId);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        fileService.serveFile(fileId, currentUserId, request, response);
    }

    /**
     * 获取当前用户ID，支持从URL参数中的token进行认证
     *
     * @param request HTTP请求对象
     * @param token   URL参数中的token
     * @return 用户ID
     */
    private String getCurrentUserIdWithTokenSupport(HttpServletRequest request, String token) {
        // 首先尝试从请求属性获取（正常的JWT认证流程）
        String userId = UserUtils.getCurrentUserId(request);
        if (userId != null && !userId.equals("anonymousUser")) {
            return userId;
        }

        // 如果请求属性中没有用户ID，尝试从URL参数的token中解析
        if (token != null && !token.trim().isEmpty()) {
            try {
                // JwtUtils.resolveJWT方法期望token以"Bearer "开头，所以需要添加前缀
                com.auth0.jwt.interfaces.DecodedJWT jwt = jwtUtils.resolveJWT("Bearer " + token);
                if (jwt != null) {
                    return jwtUtils.toId(jwt);
                }
            } catch (Exception e) {
                log.warn("从URL参数解析token失败: {}", e.getMessage());
            }
        }

        return null;
    }

    /**
     * 学生端获取课程内容大纲接口
     * 一次性返回指定课程的所有章节、小节、关联资料，以及当前学生的个性化学习进度。
     *
     * @param courseId 课程ID
     * @return 包含完整课程内容结构的 RestBean
     */
    @GetMapping("/{courseId}/content")
    public RestBean<CourseContentDto> getCourseContent(@PathVariable String courseId, HttpServletRequest request) {
        String currentUserId = UserUtils.getCurrentUserId(request);
        if (currentUserId == null) {
            log.warn("[getCourseContent.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        CourseContentDto courseContent = learningProgressService.getCourseContentForStudent(courseId, currentUserId);

        // 使用统一的响应体 RestBean 包装成功的结果并返回
        return RestBean.success(courseContent);
    }

    /**
     * 获取课程资料 - 所有课程资料
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 课程资料列表
     */
    @GetMapping("/materials/all")
    public RestBean<List<CourseMaterials>> getCourseMaterials(String userId, String courseId) {
        List<CourseMaterials> courseMaterials = coursesService.getCourseMaterials(userId, courseId);
        if (courseMaterials == null || courseMaterials.isEmpty()) {
            log.warn("[getCourseMaterials.method]用户ID：{} 课程ID：{} 没有课程资料", userId, courseId);
            return RestBean.failure(404, "没有课程资料");
        }
        return RestBean.success(courseMaterials);
    }

    /**
     * 获取课程资料 - 章节
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @return 课程资料
     */
    @GetMapping("/materials/chapters")
    public RestBean<List<CourseMaterials>> getCourseMaterialsByChapterId(String userId, String courseId, String chapterId) {
        List<CourseMaterials> courseMaterials = coursesService.getCourseMaterialsByChapterId(userId, courseId, chapterId);
        if (courseMaterials == null || courseMaterials.isEmpty()) {
            log.warn("[getCourseMaterials.method]课程ID：{} 没有课程资料", courseId);
            return RestBean.failure(404, "没有课程资料");
        }
        return RestBean.success(courseMaterials);
    }

    /**
     * 获取课程资料 - 小节资料
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @param sectionId 小节ID
     * @return 课程资料
     */
    @GetMapping("/materials/sections")
    public RestBean<List<CourseMaterials>> getCourseMaterialsBySectionIdANDChapterId(String userId, String courseId, String chapterId, String sectionId) {
        List<CourseMaterials> courseMaterials = coursesService.getCourseMaterialsBySectionIdANDChapterId(userId, courseId, chapterId, sectionId);
        if (courseMaterials == null || courseMaterials.isEmpty()) {
            log.info("[getCourseMaterials] 课程无资料: courseId={}", courseId);
            return RestBean.failure(404, "没有课程资料");
        }
        return RestBean.success(courseMaterials);
    }

    /**
     * 获取学习笔记 - 所有
     *
     * @param request 请求
     * @return 学习笔记列表
     */
    @GetMapping("/notes")
    public RestBean<List<LearningNotes>> getLearningNotesAll(HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            return RestBean.failure(401, "用户未登录");
        }
        List<LearningNotes> learningNotes = learningProgressService.getLearningNotesAll(userId);
        if (learningNotes == null || learningNotes.isEmpty()) {
            log.warn("[getLearningNotesAll.method]用户ID：{} 没有学习笔记", userId);
            return RestBean.failure(404, "没有学习笔记");
        }
        return RestBean.success(learningNotes);
    }

    /**
     * 获取一个学生的所有课程笔记
     *
     * @param request  请求
     * @param pageable 分页参数
     * @return 课程笔记列表
     */
    @GetMapping("/notes/course")
    public RestBean<Page<LearningNotes>> getLearningNotesByCourse(HttpServletRequest request, @PageableDefault(size = 15, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            return RestBean.failure(401, "用户未登录");
        }
        Page<LearningNotes> pageLearningNotes = learningProgressService.getAllCourseNotes(userId, pageable);
        if (pageLearningNotes == null || pageLearningNotes.isEmpty()) {
            log.warn("[getLearningNotesByCourse.method]用户ID：{} 没有课程学习笔记", userId);
            return RestBean.failure(404, "用户暂没有没有课程学习笔记");
        }
        return RestBean.success(pageLearningNotes);
    }

    /**
     * 获取学习笔记 - 收件箱
     *
     * @param request  请求
     * @param pageable 分页参数
     * @return 收件箱学习笔记列表
     */
    @GetMapping("/notes/inbox")
    public RestBean<Page<LearningNotes>> getInboxNotes(HttpServletRequest request, @PageableDefault(size = 30, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            return RestBean.failure(401, "用户未登录");
        }
        Page<LearningNotes> pageLearningNotes = learningProgressService.getInboxNotes(userId, pageable);
        return RestBean.success(pageLearningNotes);
    }

    /**
     * 归档收件箱学习笔记
     *
     * @param noteId    笔记ID
     * @param request   请求
     * @param courseId  课程ID [非必须]
     * @param chapterId 章节ID [非必须]
     * @param sectionId 小节ID [非必须]
     * @return 归档后的学习笔记对象
     */
    @PutMapping("/notes/{id}/archive")
    public RestBean<LearningNotes> archiveInboxNote(@PathVariable("id") String noteId,
                                                    HttpServletRequest request,
                                                    @RequestParam(value = "courseId", required = false) String courseId,
                                                    @RequestParam(value = "chapterId", required = false) String chapterId,
                                                    @RequestParam(value = "sectionId", required = false) String sectionId) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            return RestBean.failure(401, "用户未登录");
        }
        try {
            LearningNotes archived = learningProgressService.archiveInboxNote(noteId, userId, courseId, chapterId, sectionId);
            return RestBean.success(archived);
        } catch (RuntimeException e) {
            return RestBean.failure(400, e.getMessage());
        }
    }

    /**
     * 获取学习笔记 - 指定课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 学习笔记列表
     */
    @GetMapping("/notes/course/specify")
    public RestBean<List<LearningNotes>> getLearningNotes(String userId, String courseId) {
        List<LearningNotes> learningNotes = learningProgressService.getLearningNotes(userId, courseId);
        if (learningNotes == null || learningNotes.isEmpty()) {
            log.warn("[getLearningNotes.method]用户ID：{} 课程ID：{} 没有学习笔记", userId, courseId);
            return RestBean.failure(404, "该课程下暂没有学习笔记");
        }
        return RestBean.success(learningNotes);
    }

    /**
     * 获取学习笔记 - 章节
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @return 学习笔记列表
     */
    @GetMapping("/notes/course/chapter")
    public RestBean<List<LearningNotes>> getLearningNotesByChapterId(String userId, String courseId, String chapterId) {
        List<LearningNotes> learningNotes = learningProgressService.getLearningNotesByChapterId(userId, courseId, chapterId);
        if (learningNotes == null || learningNotes.isEmpty()) {
            log.warn("[getLearningNotesByChapterId.method]用户ID：{} 课程ID：{} 没有学习笔记", userId, courseId);
            return RestBean.failure(404, "该课程的章节下暂没有学习笔记");
        }
        return RestBean.success(learningNotes);
    }

    /**
     * 获取学习笔记 - 小节
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @param sectionId 小节ID
     * @return 学习笔记列表
     */
    @GetMapping("/notes/course/sections")
    public RestBean<List<LearningNotes>> getLearningNotesBySectionId(String userId, String courseId, String chapterId, String sectionId) {
        List<LearningNotes> learningNotes = learningProgressService.getLearningNotesBySectionId(userId, courseId, chapterId, sectionId);
        if (learningNotes == null || learningNotes.isEmpty()) {
            log.warn("[getLearningNotesBySectionId.method]用户ID：{} 课程ID：{} 没有学习笔记", userId, courseId);
            return RestBean.failure(404, "该课程的章节下的小节暂没有学习笔记");
        }
        return RestBean.success(learningNotes);
    }

    /**
     * 创建学习笔记 - 课程
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID [非必选]
     * @param sectionId 小节ID [非必选]
     * @param title     标题
     * @param content   内容
     * @param tags      标签
     * @param videoTime 视频播放时间 [非必选]
     * @return 创建结果
     */
    @PostMapping("/create/note")
    public RestBean<String> createLearningNotes(String userId, String courseId, String chapterId, String sectionId, String title, String content, List<String> tags, Integer videoTime) {
        LearningNotes learningNotes = learningProgressService.createLearningNotes(userId, courseId, chapterId, sectionId, title, content, tags, videoTime);
        if (learningNotes == null) {
            log.warn("[createLearningNotes.method]用户ID：{} 课程ID：{} 没有学习笔记", userId, courseId);
            return RestBean.failure(404, "没有学习笔记");
        }
        return RestBean.success("创建学习笔记成功");
    }

    /**
     * 创建学习笔记 - 自由
     *
     * @param request       请求对象
     * @param createNoteDTO 创建笔记DTO
     * @return 创建结果
     */
    @PostMapping("/note/free")
    public RestBean<LearningNotes> createLearningFreedomNotes(HttpServletRequest request, @RequestBody CreateNoteDTO createNoteDTO) {
        String currentUserId = UserUtils.getCurrentUserId(request);
        if (currentUserId == null || currentUserId.isEmpty()) {
            log.warn("[createLearningNotes.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        LearningNotes learningNotes = learningProgressService.createLearningFreedomNotes(currentUserId, createNoteDTO);
        if (learningNotes == null) {
            return RestBean.failure(404, "没有学习笔记");
        }
        return RestBean.success(learningNotes);
    }

    /**
     * 创建学习笔记 - 收件箱
     *
     * @param request       请求对象
     * @param createNoteDTO 创建笔记DTO
     * @return 创建结果
     */
    @PostMapping("/note/inbox")
    public RestBean<LearningNotes> createInboxNote(HttpServletRequest request, @RequestBody CreateNoteDTO createNoteDTO) {
        String currentUserId = UserUtils.getCurrentUserId(request);
        if (currentUserId == null || currentUserId.isEmpty()) {
            return RestBean.failure(401, "用户未认证");
        }
        LearningNotes learningNotes = learningProgressService.createInboxNote(currentUserId, createNoteDTO);
        return RestBean.success(learningNotes);
    }

    /**
     * 更新学习笔记 - 覆盖式
     *
     * @param request       请求对象
     * @param updateNoteDTO 更新笔记DTO
     * @return 创建结果
     */
    @PostMapping("/note/{id}")
    public RestBean<LearningNotes> updateLearningNotes(@PathVariable("id") String noteId, HttpServletRequest request, @RequestBody UpdateNoteDTO updateNoteDTO) {
        String currentUserId = UserUtils.getCurrentUserId(request);
        if (currentUserId == null || currentUserId.isEmpty()) {
            log.warn("[updateLearningNotes.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        LearningNotes learningNotes = learningProgressService.updateNote(noteId, updateNoteDTO, currentUserId);
        if (learningNotes == null) {
            log.warn("[updateLearningNotes.method]用户ID：{}", currentUserId);
            return RestBean.failure(404, "没有学习笔记");
        }
        return RestBean.success(learningNotes);
    }

    /**
     * 更新学习笔记
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @param sectionId 小节ID
     * @param title     标题
     * @param content   内容
     * @param tags      标签
     * @param videoTime 视频播放时间
     * @return 更新结果
     */
    @PostMapping("/update/note")
    public RestBean<String> updateLearningNotes(String userId, String courseId, String chapterId, String sectionId, String title, String content, List<String> tags, Integer videoTime) {
        LearningNotes learningNotes = learningProgressService.updateLearningNotes(userId, courseId, chapterId, sectionId, title, content, tags, videoTime);
        if (learningNotes == null) {
            log.warn("[updateLearningNotes.method]用户ID：{} 课程ID：{} 没有学习笔记", userId, courseId);
            return RestBean.failure(404, "没有学习笔记");
        }
        return RestBean.success("更新学习笔记成功");
    }

    /**
     * 获取指定用户的总学习时长
     *
     * @param request 请求对象
     * @return 总学习时长（包含秒数和分钟数）
     */
    @GetMapping("/total/time")
    public RestBean<Map<String, Object>> getTotalLearningTime(HttpServletRequest request) {

        String userId = UserUtils.getCurrentUserId(request);

        log.info("[getTotalLearningTime] 获取用户总学习时长: userId={}", userId);

        Map<String, Object> result = learningProgressService.getTotalLearningTime(userId);
        if (result == null) {
            log.warn("[getTotalLearningTime] 获取用户总学习时长失败: userId={}", userId);
            return RestBean.failure(500, "获取总学习时长失败");
        }

        log.info("[getTotalLearningTime] 用户总学习时长: userId={}, totalSeconds={}, totalMinutes={}",
                userId, result.get("totalSeconds"), result.get("totalMinutes"));
        return RestBean.success(result);
    }

    /**
     * 获取用户总体学习进度
     *
     * @param request 请求
     * @return 总体学习进度百分比
     */
    @GetMapping("/overall/progress")
    public RestBean<Double> getOverallProgress(HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) return RestBean.failure(401, "用户未认证！");

        Double progress = learningProgressService.getOverallProgress(userId);
        return RestBean.success(progress);
    }

}
