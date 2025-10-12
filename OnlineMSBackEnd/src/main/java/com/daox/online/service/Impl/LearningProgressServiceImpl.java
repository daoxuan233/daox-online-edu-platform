package com.daox.online.service.Impl;

import com.daox.online.controller.exception.AccessDeniedException;
import com.daox.online.entity.mysql.*;
import com.daox.online.entity.dto.ChapterDto;
import com.daox.online.entity.dto.SectionDto;
import com.daox.online.entity.mongodb.LearningNotes;
import com.daox.online.entity.mongodb.dto.CreateNoteDTO;
import com.daox.online.entity.mongodb.dto.UpdateNoteDTO;
import com.daox.online.entity.views.responseVO.course.CourseContentDto;
import com.daox.online.entity.views.responseVO.course.MaterialDto;
import com.daox.online.mapper.*;
import com.daox.online.repository.mongodb.LearningNotesRepository;
import com.daox.online.service.LearningProgressService;
import com.daox.online.service.SysUserService;
import static java.util.stream.Collectors.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class LearningProgressServiceImpl implements LearningProgressService {

    @Resource
    private LearningProgressMapper learningProgressMapper;

    @Resource
    private ChaptersANDSectionsMapper chaptersANDSectionsMapper;

    @Resource
    private LearningNotesRepository learningNotesRepository;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private CoursesMapper coursesMapper;
    @Resource
    private UserCoursesMapper userCoursesMapper;
    @Resource
    private CourseContentMapper courseContentMapper;

    /**
     * 为学生获取带有个人学习进度的完整课程内容。
     *
     * @param courseId  课程ID
     * @param studentId 学生ID
     * @return 课程内容
     */
    @Override
    public CourseContentDto getCourseContentForStudent(String courseId, String studentId) {
        // 1. 授权检查：确认学生是否已报名该课程
        if (userCoursesMapper.findByUserIdAndCourseId(studentId, courseId) == null) {
            throw new AccessDeniedException("您未报名该课程，无权访问内容。");
        }
        log.info("开始为学生 {} 获取课程 {} 的内容", studentId, courseId);

        // 2. 高效数据获取：通过4次查询获取所有所需数据
        List<Chapters> chapters = courseContentMapper.findChaptersByCourseId(courseId);
        List<Sections> sections = courseContentMapper.findSectionsByCourseId(courseId);
        List<CourseContentMapper.MaterialDetail> sectionMaterials = courseContentMapper.findSectionMaterialsByCourseId(courseId);
        List<CourseContentMapper.MaterialDetail> courseLevelMaterialsRaw = courseContentMapper.findCourseLevelMaterials(courseId);
        List<LearningProgress> progresses = courseContentMapper.findProgressByStudentAndCourse(studentId, courseId);

        // 3.1 将课程级别的资料直接转换为 DTO 列表
        List<MaterialDto> courseLevelMaterials = courseLevelMaterialsRaw.stream()
                .map(this::convertToMaterialDto)
                .collect(toList());

        // 3.2 将【小节资料】按其所属的 sectionId 分组存入 Map，方便后续根据小节ID快速查找
        Map<String, List<MaterialDto>> materialsBySectionId = sectionMaterials.stream()
                .collect(groupingBy(m -> m.sectionId, mapping(this::convertToMaterialDto, toList())));

        // 3.3 将【学习进度】记录按 sectionId 转换为 Map，实现 O(1) 复杂度的快速查找
        Map<String, LearningProgress> progressMap = progresses.stream()
                .collect(toMap(LearningProgress::getSectionId, p -> p, (existingValue, newValue) -> existingValue)); // 如果有重复记录，保留第一个

        // 3.4 将【小节】按其所属的 chapterId 分组
        Map<String, List<SectionDto>> sectionsByChapterId = sections.stream()
                .collect(groupingBy(Sections::getChapterId,
                        mapping(s -> convertToSectionDto(s, materialsBySectionId, progressMap), toList())));

        // 3.5 遍历所有章节，并从上面的 Map 中找到对应的子集（小节），组装成最终的章节 DTO 列表
        List<ChapterDto> chapterDtoList = chapters.stream()
                .map(c -> convertToChapterDto(c, sectionsByChapterId))
                .collect(toList());

        // 3.6 // 获取学习进度百分比
        Integer completedSectionsByUserIdAndCourseId = chaptersANDSectionsMapper.getCompletedSectionsByUserIdAndCourseId(studentId, courseId);
        Integer totalSectionsByCourseId = chaptersANDSectionsMapper.getTotalSectionsByCourseId(courseId);
        Double progressPercentage = (completedSectionsByUserIdAndCourseId).doubleValue() / (totalSectionsByCourseId.doubleValue()) * 100;

        // 步骤 4: 组装顶层返回对象
        CourseContentDto finalResult = new CourseContentDto();
        finalResult.setCourseId(courseId);
        finalResult.setTitle(coursesMapper.getCourseById(courseId).getTitle());
        finalResult.setProgressPercentage(progressPercentage);
        finalResult.setCourseMaterials(courseLevelMaterials);
        finalResult.setChapters(chapterDtoList);

        log.info("成功为学生 {} 组装了课程 {} 的完整内容，包含 {} 个章节。", studentId, courseId, chapterDtoList.size());
        return finalResult;
    }

    /**
     * 获取学习笔记 - 所有
     *
     * @param userId 用户ID
     * @return 学习笔记列表
     */
    @Override
    public List<LearningNotes> getLearningNotesAll(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.warn("[getLearningNotes.method]用户ID为空");
            return Collections.emptyList();
        }
        Users userById = sysUserService.findUserById(userId);
        if (userById == null) {
            log.warn("[getLearningNotes.method]用户不存在");
            return Collections.emptyList();
        }
        List<LearningNotes> byStudentId = learningNotesRepository.findByStudentId(userId);
        if (byStudentId == null || byStudentId.isEmpty()) {
            log.warn("[getLearningNotes.method]用户没有学习笔记");
            return Collections.emptyList();
        }
        return byStudentId;
    }

    /**
     * 获取一个学生的所有课程笔记 (分页)
     *
     * @param studentId 学生ID
     * @param pageable  分页参数
     * @return 分页后的课程笔记
     */
    @Override
    public Page<LearningNotes> getAllCourseNotes(String studentId, Pageable pageable) {
        return learningNotesRepository.findByStudentIdAndCourseIdIsNotNullAndIsDeletedFalse(studentId, pageable);
    }

    /**
     * 获取学习笔记 - 课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 学习笔记列表
     */
    @Override
    public List<LearningNotes> getLearningNotes(String userId, String courseId) {
        if (isInvalidRequest(userId, courseId)) {
            return Collections.emptyList();
        }

        if (isUserNotEnrolledInCourse(userId, courseId)) {
            return Collections.emptyList();
        }

        List<LearningNotes> notes = learningNotesRepository.findByStudentIdAndCourseId(userId, courseId);
        return notes != null ? notes : Collections.emptyList();
    }

    /**
     * 获取学习笔记 - 章节
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @return 学习笔记列表
     */
    @Override
    public List<LearningNotes> getLearningNotesByChapterId(String userId, String courseId, String chapterId) {
        if (isInvalidRequest(userId, courseId)) {
            return Collections.emptyList();
        }

        if (isUserNotEnrolledInCourse(userId, courseId)) {
            return Collections.emptyList();
        }

        List<LearningNotes> notes = learningNotesRepository.findByStudentIdAndCourseIdAndChapterId(userId, courseId, chapterId);
        return notes != null ? notes : Collections.emptyList();
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
    @Override
    public List<LearningNotes> getLearningNotesBySectionId(String userId, String courseId, String chapterId, String sectionId) {
        if (isInvalidRequest(userId, courseId)) {
            return Collections.emptyList();
        }

        if (isUserNotEnrolledInCourse(userId, courseId)) {
            return Collections.emptyList();
        }

        List<LearningNotes> notes = learningNotesRepository.findByStudentIdAndCourseIdAndChapterIdAndSectionId(userId, courseId, chapterId, sectionId);
        return notes != null ? notes : Collections.emptyList();
    }

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
     * @param videoTime 视频时间 [非必须]
     * @return 学习笔记对象
     */
    @Override
    public LearningNotes createLearningNotes(String userId, String courseId, String chapterId, String sectionId, String title, String content, List<String> tags, Integer videoTime) {
        // 参数验证
        if (isInvalidNoteCreationRequest(userId, courseId, title, content, tags)) {
            return null;
        }

        // 验证用户和课程
        if (isInvalidUserOrCourse(userId, courseId)) {
            return null;
        }

        // 验证章节和小节（如果提供）
        if (isInvalidChapterOrSection(chapterId, sectionId)) {
            return null;
        }

        // 创建并保存学习笔记
        LearningNotes newNote = createNewLearningNote(userId, courseId, chapterId, sectionId, title, content, tags, videoTime);
        return learningNotesRepository.save(newNote);
    }

    /**
     * 创建学习笔记 - 自由
     *
     * @param userId        用户ID
     * @param createNoteDTO 笔记数据
     * @return 创建结果
     */
    @Override
    public LearningNotes createLearningFreedomNotes(String userId, CreateNoteDTO createNoteDTO) {
        LearningNotes note = new LearningNotes()
                .setStudentId(userId)
                .setTitle(createNoteDTO.getTitle())
                .setContent(createNoteDTO.getContent())
                .setCourseId(createNoteDTO.getCourseId()) // 如果 dto.getCourseId() 为 null，则该字段不会被设置
                .setChapterId(createNoteDTO.getChapterId())
                .setSectionId(createNoteDTO.getSectionId())
                .setTags(createNoteDTO.getTags())
                .setIsPrivate(createNoteDTO.getIsPrivate())
                .setVideoTime(createNoteDTO.getVideoTime())
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now())
                .setIsDeleted(false);
        return learningNotesRepository.save(note);
    }

    /**
     * 更新一篇已存在的学习笔记<br/>
     *
     * 这里的逻辑是“覆盖式”更新，前端负责内容的拼接。
     *
     * @param noteId        要更新的笔记ID
     * @param updateNoteDTO 更新的数据
     * @param studentId     执行操作的学生ID
     * @return 更新后的笔记实体
     */
    @Override
    public LearningNotes updateNote(String noteId, UpdateNoteDTO updateNoteDTO, String studentId) {
        // 1. 使用我们自定义的方法查找笔记，同时验证了所有权
        LearningNotes existingNote = learningNotesRepository.findByIdAndStudentId(noteId, studentId)
                .orElseThrow(() -> new RuntimeException("笔记不存在或无权访问")); // 建议自定义异常

        // 2. 如果找到了，就更新字段
        existingNote.setTitle(updateNoteDTO.getTitle());
        existingNote.setContent(updateNoteDTO.getContent()); // 完全替换内容
        existingNote.setTags(updateNoteDTO.getTags());
        existingNote.setIsPrivate(updateNoteDTO.getIsPrivate());
        existingNote.setUpdatedAt(LocalDateTime.now()); // 更新“最后修改时间”

        // 3. 保存并返回更新后的文档
        return learningNotesRepository.save(existingNote);
    }

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
    @Override
    public LearningNotes updateLearningNotes(String userId, String courseId, String chapterId, String sectionId, String title, String content, List<String> tags, Integer videoTime) {
        if (isInvalidNoteCreationRequest(userId, courseId, title, content, tags)) {
            return null;
        }
        if (isInvalidUserOrCourse(userId, courseId)) {
            return null;
        }
        if (isInvalidChapterOrSection(chapterId, sectionId)) {
            return null;
        }
        return learningNotesRepository.save(createNewLearningNote(userId, courseId, chapterId, sectionId, title, content, tags, videoTime));
    }

    /**
     * 笔记创建请求参数验证
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @param title    标题
     * @param content  内容
     * @param tags     标签
     * @return 是否无效 请求 true：无效，false：有效
     */
    private boolean isInvalidNoteCreationRequest(String userId, String courseId, String title, String content, List<String> tags) {
        if (userId == null || userId.isEmpty() || courseId == null || courseId.isEmpty()
                || title == null || title.isEmpty() || content == null || content.isEmpty()
                || tags == null || tags.isEmpty()) {
            log.warn("[createLearningNotes.method]参数错误: userId={}, courseId={}, title={}, content={}, tags={}",
                    userId, courseId, title, content, tags);
            return true;
        }
        return false;
    }

    /**
     * 用户和课程验证
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 是否无效 true：无效，false：有效
     */
    private boolean isInvalidUserOrCourse(String userId, String courseId) {
        Users user = sysUserService.findUserById(userId);
        if (user == null) {
            log.warn("[createLearningNotes.method]用户不存在: userId={}", userId);
            return true;
        }

        Courses course = coursesMapper.getCourseById(courseId);
        if (course == null) {
            log.warn("[createLearningNotes.method]课程不存在: courseId={}", courseId);
            return true;
        }

        if (!coursesMapper.isUserEnrolledInCourse(userId, courseId)) {
            log.warn("[createLearningNotes.method]用户未加入课程: userId={}, courseId={}", userId, courseId);
            return true;
        }

        return false;
    }

    /**
     * 章节和小节验证
     *
     * @param chapterId 章节ID
     * @param sectionId 小节ID
     * @return 是否无效 true：无效，false：有效
     */
    private boolean isInvalidChapterOrSection(String chapterId, String sectionId) {
        if (chapterId != null && !chapterId.isEmpty()) {
            if (sectionId == null || sectionId.isEmpty()) {
                log.warn("[createLearningNotes.method]提供了章节ID但未提供小节ID: chapterId={}", chapterId);
                return true;
            }
            Sections section = chaptersANDSectionsMapper.getSectionById(sectionId);
            if (section == null) {
                log.warn("[createLearningNotes.method]小节不存在: sectionId={}", sectionId);
                return true;
            }
        }
        return false;
    }

    /**
     * 创建新的学习笔记
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @param sectionId 小节ID
     * @param title     标题
     * @param content   内容
     * @param tags      标签
     * @param videoTime 视频
     * @return 新的学习笔记对象
     */
    private LearningNotes createNewLearningNote(String userId, String courseId, String chapterId, String sectionId, String title, String content, List<String> tags, Integer videoTime) {
        return new LearningNotes()
                .setStudentId(userId)
                .setCourseId(courseId)
                .setChapterId(chapterId)
                .setSectionId(sectionId)
                .setTitle(title)
                .setContent(content)
                .setTags(tags)
                .setIsPrivate(true)
                .setVideoTime(videoTime)
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now())
                .setIsDeleted(false);
    }

    /**
     * 验证请求参数 - 课程ID和用户ID
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return true: 参数错误 false: 参数正确
     */
    private boolean isInvalidRequest(String userId, String courseId) {
        if (userId == null || userId.isEmpty() || courseId == null || courseId.isEmpty()) {
            log.warn("[isInvalidRequest.method]参数错误: userId={}, courseId={}", userId, courseId);
            return true;
        }

        Users userById = sysUserService.findUserById(userId);
        if (userById == null) {
            log.warn("[isInvalidRequest.method]用户不存在");
            return true;
        }

        return false;
    }

    /**
     * 验证用户是否加入课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return true: 未加入课程 false: 加入课程
     */
    private boolean isUserNotEnrolledInCourse(String userId, String courseId) {
        boolean enrolled = coursesMapper.isUserEnrolledInCourse(userId, courseId);
        if (!enrolled) {
            log.warn("[isUserNotEnrolledInCourse.method]用户未加入该课程");
            return true;
        }
        return false;
    }

    /**
     * 获取指定用户的总学习时长
     *
     * @param userId 用户ID
     * @return Map包含总学习时长的秒数和分钟数
     */
    @Override
    public Map<String, Object> getTotalLearningTime(String userId) {
        Map<String, Object> result = new HashMap<>();

        if (userId == null || userId.isEmpty()) {
            log.warn("[getTotalLearningTime.method]用户ID为空");
            result.put("totalSeconds", 0);
            result.put("totalMinutes", 0);
            return result;
        }

        // 验证用户是否存在
        Users userById = sysUserService.findUserById(userId);
        if (userById == null) {
            log.warn("[getTotalLearningTime.method]用户不存在: userId={}", userId);
            result.put("totalSeconds", 0);
            result.put("totalMinutes", 0);
            return result;
        }

        try {
            // 获取总学习时长（秒）
            int totalSeconds = learningProgressMapper.getTotalLearningTimeByUserId(userId);
            int totalMinutes = totalSeconds / 60;

            result.put("totalSeconds", totalSeconds);
            result.put("totalMinutes", totalMinutes);

            log.info("[getTotalLearningTime.method]用户ID: {} 总学习时长: {}秒 ({}分钟)", userId, totalSeconds, totalMinutes);
            return result;
        } catch (Exception e) {
            log.error("[getTotalLearningTime.method]获取学习时长失败: userId={}, error={}", userId, e.getMessage());
            result.put("totalSeconds", 0);
            result.put("totalMinutes", 0);
            return result;
        }
    }

    /**
     * 获取用户总体学习进度
     *
     * @param userId 用户ID
     * @return 进度百分比
     */
    @Override
    public Double getOverallProgress(String userId) {
        try {
            Integer totalSections = learningProgressMapper.getTotalSections(userId);
            Integer completedSections = learningProgressMapper.getCompletedSections(userId);

            log.info("[getOverallProgress.method]用户ID: {} 总小节数: {} 已完成小节数: {}", userId, totalSections, completedSections);

            if (totalSections == null || totalSections == 0 || completedSections == null) {
                return 0.0;
            }

            return (completedSections.doubleValue() / totalSections.doubleValue()) * 100;
        } catch (Exception e) {
            log.error("[getOverallProgress.method]获取学习进度失败: userId={}, error={}", userId, e.getMessage());
            return 0.0;
        }
    }

    /**
     * 用于 Entity -> DTO 的转换
     * @param chapter 章节
     * @param sectionsMap 小节
     * @return 转换后的章节DTO
     */
    private ChapterDto convertToChapterDto(Chapters chapter, Map<String, List<SectionDto>> sectionsMap) {
        ChapterDto dto = new ChapterDto();
        dto.setId(chapter.getId());
        dto.setTitle(chapter.getTitle());
        dto.setOrderIndex(chapter.getOrderIndex());
        // 从 Map 中安全地获取该章节下所有的小节，如果没有则返回空列表
        dto.setSections(sectionsMap.getOrDefault(chapter.getId(), new ArrayList<>()));
        return dto;
    }

    /**
     * 用于 Entity -> DTO 的转换
     * @param section 小节
     * @param materialsMap 材料
     * @param progressMap 进度
     * @return 转换后的小节DTO
     */
    private SectionDto convertToSectionDto(Sections section,
                                           Map<String, List<MaterialDto>> materialsMap,
                                           Map<String, LearningProgress> progressMap) {
        SectionDto dto = new SectionDto();
        dto.setId(section.getId());
        dto.setTitle(section.getTitle());
        dto.setOrderIndex(section.getOrderIndex());
        dto.setDurationSeconds(section.getDurationSeconds());

        // 关键：将 file_id 转换为前端可直接使用的 API URL
        if (StringUtils.hasText(section.getVideoUrl())) {
            // dto.setVideoUrl("/api/v1/files/" + section.getVideoUrl());
            dto.setVideoUrl("/api/student/learning/files/" + section.getVideoUrl());
        }

        // 附加小节资料
        dto.setMaterials(materialsMap.getOrDefault(section.getId(), new ArrayList<>()));

        // 设置个性化学习进度
        LearningProgress progress = progressMap.get(section.getId());
        if (progress != null) {
            dto.setStatus(progress.getStatus());
            dto.setProgressSeconds(progress.getProgressSeconds());
        } else {
            // 如果没有进度记录，说明学生从未开始过，给予默认值
            dto.setStatus("not_started");
            dto.setProgressSeconds(0);
        }
        return dto;
    }

    /**
     * 用于 Entity -> DTO 的转换
     * @param material  材料
     * @return 转换后的材料DTO
     */
    private MaterialDto convertToMaterialDto(CourseContentMapper.MaterialDetail material) {
        MaterialDto dto = new MaterialDto();
        dto.setId(material.id);
        dto.setTitle(material.title);
        dto.setFileId(material.fileId);
        dto.setMimeType(material.mimeType);
        return dto;
    }
}
