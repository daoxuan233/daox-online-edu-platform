package com.daox.online.controller.teacher;

import com.daox.online.entity.mysql.Sections;
import com.daox.online.entity.RestBean;
import com.daox.online.entity.views.responseVO.ChapterBriefVo;
import com.daox.online.service.ChaptersANDSectionsService;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师端 - 课程相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/courses/chapters")
public class ChaptersANDSectionsController {

    @Resource
    private ChaptersANDSectionsService chaptersANDSectionsService;

    /**
     * 获取课程章节 - 仅列表
     *
     * @param courseId 课程ID
     * @return 课程章节简要信息
     */
    @GetMapping("/list")
    public RestBean<List<ChapterBriefVo>> getCourseChapters(@RequestParam("courseId") String courseId) {
        List<ChapterBriefVo> chapterBriefVos = chaptersANDSectionsService.getChaptersByCourseId(courseId);
        if (chapterBriefVos == null || chapterBriefVos.isEmpty()) return RestBean.failure(404, "查询错误！");
        return RestBean.success(chapterBriefVos);
    }

    /**
     * 创建章节
     *
     * @param courseId   课程id
     * @param title      章节标题
     * @param orderIndex 排序索引
     * @param request    请求
     * @return 创建结果
     */
    @PostMapping("/create")
    public RestBean<String> createChapter(
            @RequestParam("courseId") String courseId,
            @RequestParam("title") String title,
            @RequestParam("orderIndex") Integer orderIndex,
            HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[createChapter.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        boolean chapter = chaptersANDSectionsService.createChapter(userId, courseId, title, orderIndex);
        if (!chapter) {
            log.warn("[createChapter.method]创建章节失败: userId={}, courseId={}, title={}, orderIndex={}", userId, courseId, title, orderIndex);
            return RestBean.failure(500, "创建章节失败");
        }
        return RestBean.success("创建章节成功");
    }

    /**
     * 更新章节
     *
     * @param chapterId  章节ID
     * @param title      章节标题
     * @param orderIndex 章节顺序
     * @param request    请求
     * @return 响应
     */
    @PostMapping("/updateChapter")
    public RestBean<String> updateChapter(@RequestParam("chapterId") String chapterId,
                                          @RequestParam("title") String title,
                                          @RequestParam("orderIndex") Integer orderIndex,
                                          HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[updateChapter.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        boolean b = chaptersANDSectionsService.updateChapter(userId, chapterId, title, orderIndex);
        if (b) {
            log.info("[updateChapter.method]更新章节成功: userId={}, chapterId={}, title={}, orderIndex={}", userId, chapterId, title, orderIndex);
            return RestBean.success("更新章节成功");
        } else {
            log.warn("[updateChapter.method]更新章节失败: userId={}, chapterId={}, title={}, orderIndex={}", userId, chapterId, title, orderIndex);
            return RestBean.failure(500, "更新章节失败");
        }
    }

    /**
     * 获取小节列表
     *
     * @param chapterId 章节ID
     * @return 小节列表
     */
    @GetMapping("/sections")
    public RestBean<List<Sections>> getSections(@RequestParam("chapterId") String chapterId) {
        List<Sections> sectionsByChapterId = chaptersANDSectionsService.getSectionsByChapterId(chapterId);
        if (sectionsByChapterId == null || sectionsByChapterId.isEmpty()) {
            return RestBean.failure(404, "查询错误！");
        }
        return RestBean.success(sectionsByChapterId);
    }

    /**
     * 创建小节
     *
     * @param chapterId       章节ID
     * @param title           小节标题
     * @param videoUrl        视频地址
     * @param durationSeconds 视频时长
     * @param orderIndex      排序索引
     * @param request         请求对象
     * @return 创建结果
     */
    @PostMapping("/createSection")
    public RestBean<String> createSection(@RequestParam("chapterId") String chapterId,
                                          @RequestParam("title") String title,
                                          @RequestParam("videoUrl") String videoUrl,
                                          @RequestParam("durationSeconds") Integer durationSeconds,
                                          @RequestParam("orderIndex") Integer orderIndex,
                                          HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[createSection.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        boolean section = chaptersANDSectionsService.createSection(userId, chapterId, title, videoUrl, durationSeconds, orderIndex);
        if (section) {
            log.info("[createSection.method]创建章节成功: userId={}, chapterId={}, title={}, videoUrl={}, durationSeconds={}, orderIndex={}", userId, chapterId, title, videoUrl, durationSeconds, orderIndex);
            return RestBean.success("创建章节成功");
        } else {
            log.warn("[createSection.method]创建章节失败: userId={}, chapterId={}, title={}, videoUrl={}, durationSeconds={}, orderIndex={}", userId, chapterId, title, videoUrl, durationSeconds, orderIndex);
            return RestBean.failure(500, "创建章节失败");
        }
    }
}
