package com.daox.online.service;

import com.daox.online.entity.dto.ChapterDto;
import com.daox.online.entity.mongodb.LearningNotes;
import com.daox.online.entity.mongodb.dto.CreateNoteDTO;
import com.daox.online.entity.mongodb.dto.UpdateNoteDTO;
import com.daox.online.entity.views.responseVO.course.CourseContentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface LearningProgressService {
    /**
     * 为学生获取带有个人学习进度的完整课程内容。
     *
     * <p>
     * 含课程和小节资料
     * </p>
     *
     * @param courseId  课程ID
     * @param studentId 学生ID
     * @return 课程内容
     */
    CourseContentDto getCourseContentForStudent(String courseId, String studentId);

    /**
     * 获取学习笔记 - 所有
     *
     * @param userId 用户ID
     * @return 学习笔记列表
     */
    List<LearningNotes> getLearningNotesAll(String userId);

    /**
     * 获取一个学生的所有课程笔记 (分页)
     *
     * @param studentId 学生ID
     * @param pageable  分页参数
     * @return 分页后的课程笔记
     */
    Page<LearningNotes> getAllCourseNotes(String studentId, Pageable pageable);

    Page<LearningNotes> getInboxNotes(String studentId, Pageable pageable);

    LearningNotes archiveInboxNote(String noteId, String studentId, String courseId, String chapterId, String sectionId);

    LearningNotes createInboxNote(String userId, CreateNoteDTO createNoteDTO);

    /**
     * 获取学习笔记 - 课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 学习笔记列表
     */
    List<LearningNotes> getLearningNotes(String userId, String courseId);

    /**
     * 获取学习笔记 - 章节
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @return 学习笔记列表
     */
    List<LearningNotes> getLearningNotesByChapterId(String userId, String courseId, String chapterId);

    /**
     * 获取学习笔记 - 小节
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @param sectionId 小节ID
     * @return 学习笔记列表
     */
    List<LearningNotes> getLearningNotesBySectionId(String userId, String courseId, String chapterId, String sectionId);

    /**
     * 创建学习笔记
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID [非必须]
     * @param sectionId 小节ID [非必须]
     * @param title     标题
     * @param content   内容
     * @param tags      标签
     * @param videoTime 视频时长 [非必须]
     * @return 学习笔记对象
     */
    LearningNotes createLearningNotes(String userId, String courseId, String chapterId, String sectionId, String title, String content, List<String> tags, Integer videoTime);

    /**
     * 创建学习笔记 - 自由
     *
     * @param userId        用户ID
     * @param createNoteDTO 笔记数据
     * @return 创建结果
     */
    LearningNotes createLearningFreedomNotes(String userId, CreateNoteDTO createNoteDTO);

    /**
     * 更新一篇已存在的学习笔记
     *
     * @param noteId        要更新的笔记ID
     * @param updateNoteDTO 更新的数据
     * @param studentId     执行操作的学生ID
     * @return 更新后的笔记实体
     */
    LearningNotes updateNote(String noteId, UpdateNoteDTO updateNoteDTO, String studentId);

    /**
     * 更新学习笔记
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID [非必须]
     * @param sectionId 小节ID [非必须]
     * @param title     标题
     * @param content   内容
     * @param tags      标签
     * @param videoTime 视频时长 [非必须]
     * @return 学习笔记对象
     */
    LearningNotes updateLearningNotes(String userId, String courseId, String chapterId, String sectionId, String title, String content, List<String> tags, Integer videoTime);

    /**
     * 获取指定用户的总学习时长
     *
     * @param userId 用户ID
     * @return Map包含总学习时长的秒数和分钟数
     */
    Map<String, Object> getTotalLearningTime(String userId);

    /**
     * 获取用户总体学习进度
     *
     * @param userId 用户ID
     * @return 进度百分比
     */
    Double getOverallProgress(String userId);

}
