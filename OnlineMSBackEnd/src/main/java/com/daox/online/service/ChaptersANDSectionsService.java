package com.daox.online.service;

import com.daox.online.entity.mysql.Sections;
import com.daox.online.entity.views.responseVO.ChapterBriefVo;

import java.util.List;
import java.util.Map;

/**
 * 课程章节和课时服务
 */
public interface ChaptersANDSectionsService {

    /**
     * 获取课程章节 - 仅展示章节列表
     *
     * @param courseId 课程ID
     * @return 课程章节
     */
    List<ChapterBriefVo> getChaptersByCourseId(String courseId);

    /**
     * 获取视频播放地址
     *
     * @param sectionId 视频ID
     * @return 视频播放地址
     */
    Map<String, Integer> getVideoPlayUrl(String sectionId);

    /**
     * 创建章节
     *
     * @param userId     用户ID
     * @param courseId   课程ID
     * @param title      章节标题
     * @param orderIndex 排序索引
     * @return 创建结果
     */
    boolean createChapter(String userId, String courseId, String title, Integer orderIndex);

    /**
     * 更新章节
     *
     * @param userId     用户ID
     * @param chapterId  章节ID
     * @param title      章节标题
     * @param orderIndex 排序索引
     * @return 更新结果
     */
    boolean updateChapter(String userId, String chapterId, String title, Integer orderIndex);

    /**
     * 获取小节列表
     *
     * @param chapterId 章节ID
     * @return 小节列表
     */
    List<Sections> getSectionsByChapterId(String chapterId);

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
    boolean createSection(String userId, String chapterId, String title, String videoUrl, Integer durationSeconds, Integer orderIndex);
}
