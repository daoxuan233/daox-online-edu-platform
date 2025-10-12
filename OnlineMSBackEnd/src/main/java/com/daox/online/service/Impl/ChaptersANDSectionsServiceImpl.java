package com.daox.online.service.Impl;

import com.daox.online.entity.mysql.Chapters;
import com.daox.online.entity.mysql.Courses;
import com.daox.online.entity.mysql.Sections;
import com.daox.online.entity.views.responseVO.ChapterBriefVo;
import com.daox.online.mapper.ChaptersANDSectionsMapper;
import com.daox.online.mapper.CoursesMapper;
import com.daox.online.service.ChaptersANDSectionsService;
import com.daox.online.service.UsersService;
import com.daox.online.uilts.SecondaryHybridIdGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChaptersANDSectionsServiceImpl implements ChaptersANDSectionsService {

    @Resource
    private ChaptersANDSectionsMapper chaptersANDSectionsMapper;

    @Resource
    private CoursesMapper coursesMapper;

    @Resource
    private SecondaryHybridIdGenerator secondaryHybridIdGenerator;

    @Resource
    private UsersService usersService;

    /**
     * 获取课程章节 - 仅展示章节列表
     *
     * @param courseId 课程ID
     * @return 课程章节
     */
    @Override
    public List<ChapterBriefVo> getChaptersByCourseId(String courseId) {
        // 验证课程是否存在
        Courses course = coursesMapper.getCourseById(courseId);
        if (course == null) {
            log.error("[getChaptersByCourseId.method]课程不存在，courseId: {}", courseId);
            return Collections.emptyList();
        }
        List<Chapters> chaptersByCourseId = chaptersANDSectionsMapper.getChaptersByCourseId(courseId);
        if (chaptersByCourseId == null || chaptersByCourseId.isEmpty()) {
            log.error("[getChaptersByCourseId.method]课程ID：{} 没有章节", courseId);
            return Collections.emptyList();
        }
        return chaptersByCourseId.stream().map(this::convertToChapterBriefVo).collect(Collectors.toList());
    }

    /**
     * 获取视频播放地址
     *
     * @param sectionId 视频ID
     * @return 视频播放地址
     */
    @Override
    public Map<String, Integer> getVideoPlayUrl(String sectionId) {
        Sections sectionById = chaptersANDSectionsMapper.getSectionById(sectionId);
        if (sectionById == null) {
            log.warn("[getVideoPlayUrl.method]视频不存在，sectionId: {}", sectionId);
            return Collections.emptyMap();
        }
        if (sectionById.getVideoUrl() == null) {
            log.warn("[getVideoPlayUrl.method]视频不存在，sectionId: {}", sectionId);
            return Collections.emptyMap();
        }
        Map<String, Integer> videoPlayUrl = new HashMap<>();
        if (sectionById.getDurationSeconds() == 0) {
            log.warn("[getVideoPlayUrl.method]该id:{}输入文件类型非视频", sectionId);
            videoPlayUrl.put("视频不存在，该id获取的为文件类型", sectionById.getDurationSeconds());
            return videoPlayUrl;
        }
        videoPlayUrl.put(sectionById.getVideoUrl(), sectionById.getDurationSeconds());
        return videoPlayUrl;
    }

    /**
     * 创建章节
     *
     * @param courseId   课程ID
     * @param title      章节标题
     * @param orderIndex 排序索引
     * @return 创建结果
     */
    @Override
    public boolean createChapter(String userId, String courseId, String title, Integer orderIndex) {
        if (courseId == null || courseId.isEmpty() || title == null || title.isEmpty() || orderIndex == null || orderIndex < 0) {
            log.error("[createChapter.method]课程ID不能为空");
            return false;
        }
        if (!usersService.isTeacher(userId)) {
            log.error("[createChapter.method]用户不是教师，userId={}", userId);
            return false;
        }
        if (!coursesMapper.checkCourseExists(courseId)) {
            log.error("[createChapter.method]课程不存在: courseId={}", courseId);
            return false;
        }
        String id = secondaryHybridIdGenerator.generateId();
        return chaptersANDSectionsMapper.createChapter(new Chapters(id, courseId, title, orderIndex)) > 0;
    }

    /**
     * 更新章节
     *
     * @param userId     用户ID
     * @param chapterId  章节ID
     * @param title      章节标题
     * @param orderIndex 排序索引
     * @return 更新结果
     */
    @Override
    public boolean updateChapter(String userId, String chapterId, String title, Integer orderIndex) {
        if (!usersService.isTeacher(userId)) {
            log.warn("[updateChapter.method]用户不是老师: userId={}", userId);
            return false;
        }
        if (chapterId == null || chapterId.isEmpty() || title == null || title.isEmpty() || orderIndex == null) {
            log.warn("[updateChapter.method]参数错误: userId={}, chapterId={}, title={}, orderIndex={}", userId, chapterId, title, orderIndex);
            return false;
        }
        if (!chaptersANDSectionsMapper.checkChapterExists(chapterId)) {
            log.warn("[updateChapter.method]章节不存在: chapterId={}", chapterId);
            return false;
        }
        return chaptersANDSectionsMapper.updateChapter(chapterId, title, orderIndex) > 0;
    }

    /**
     * 获取小节列表
     *
     * @param chapterId 章节ID
     * @return 小节列表
     */
    @Override
    public List<Sections> getSectionsByChapterId(String chapterId) {
        if (!chaptersANDSectionsMapper.checkChapterExists(chapterId)) {
            log.warn("[getSectionsByChapterId.method]章节不存在: chapterId={}", chapterId);
            return null;
        }
        return chaptersANDSectionsMapper.getSectionsByChapterId(chapterId);
    }

    /**
     * 创建小节
     *
     * @param userId          用户ID
     * @param chapterId       章节ID
     * @param title           小节标题
     * @param videoUrl        视频播放地址
     * @param durationSeconds 视频时长
     * @param orderIndex      排序索引
     * @return 创建结果
     */
    @Override
    public boolean createSection(String userId, String chapterId, String title, String videoUrl, Integer durationSeconds, Integer orderIndex) {
        if (!usersService.isTeacher(userId)) {
            log.warn("[createSection.method]用户不是是老师，无法创建小节: userId={}", userId);
            return false;
        }
        // 参数验证
        if (chapterId == null || chapterId.isEmpty() || title == null || title.isEmpty()) {
            log.warn("[createSection.method]参数错误: chapterId={}, title={}", chapterId, title);
            return false;
        }
        // 校验章节是否存在
        if (!chaptersANDSectionsMapper.checkChapterExists(chapterId)) {
            log.warn("[createSection.method]章节不存在: chapterId={}", chapterId);
            return false;
        }
        String sectionId = secondaryHybridIdGenerator.generateId();
        int section = chaptersANDSectionsMapper.createSection(new Sections()
                .setId(sectionId)
                .setChapterId(chapterId)
                .setTitle(title)
                .setVideoUrl(videoUrl)
                .setDurationSeconds(durationSeconds)
                .setOrderIndex(orderIndex)
        );
        return section > 0;
    }

    private ChapterBriefVo convertToChapterBriefVo(Chapters chapter) {
        ChapterBriefVo vo = new ChapterBriefVo();
        vo.setId(chapter.getId());
        vo.setTitle(chapter.getTitle());
        vo.setOrder(chapter.getOrderIndex());
        vo.setSectionCount(chaptersANDSectionsMapper.countSectionsByChapterId(chapter.getId()));
        return vo;
    }
}
