package com.daox.online.repository.mongodb;

import com.daox.online.entity.mongodb.LearningNotes;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 学习笔记Repository接口
 */
@Repository
public interface LearningNotesRepository extends MongoRepository<LearningNotes, String> {

    /**
     * 根据学生ID获取所有学习笔记
     *
     * @param studentId 学生ID
     * @return 学习笔记列表
     */
    List<LearningNotes> findByStudentId(String studentId);

    @Query(value = "{ 'student_id': ?0, 'is_deleted': false, $or: [ { 'note_box': { $exists: false } }, { 'note_box': null }, { 'note_box': 'FREE' } ] }")
    List<LearningNotes> findFreeNotesByStudentId(String studentId);

    @Query(value = "{ 'student_id': ?0, 'is_deleted': false, 'note_box': 'INBOX' }")
    Page<LearningNotes> findInboxNotesByStudentId(String studentId, Pageable pageable);

    /**
     * 根据学生ID和课程ID获取学习笔记
     *
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @return 学习笔记列表
     */
    List<LearningNotes> findByStudentIdAndCourseId(String studentId, String courseId);

    /**
     * 根据学生ID、课程ID和章节ID获取学习笔记
     *
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @return 学习笔记列表
     */
    List<LearningNotes> findByStudentIdAndCourseIdAndChapterId(String studentId, String courseId, String chapterId);

    /**
     * 根据学生ID、课程ID、章节ID和小节ID获取学习笔记
     *
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @param sectionId 小节ID
     * @return 学习笔记列表
     */
    List<LearningNotes> findByStudentIdAndCourseIdAndChapterIdAndSectionId(String studentId, String courseId, String chapterId, String sectionId);

    /**
     * 根据标签获取学习笔记
     *
     * @param tags 标签列表
     * @return 学习笔记列表
     */
    List<LearningNotes> findByTagsIn(Collection<List<String>> tags);

    /**
     * 根据ID和学生ID查找笔记，用于权限校验
     *
     * @param id        笔记ID
     * @param studentId 学生ID
     * @return 笔记实体
     */
    Optional<LearningNotes> findByIdAndStudentId(String id, String studentId);

    /**
     * 根据学生ID，查询其所有 courseId 不为 null 且未被删除的笔记<br/>
     * <p>
     * Spring Data MongoDB 支持 IsNotNull 关键字来检查字段是否存在且不为 null
     * </p>
     *
     * @param studentId 学生ID
     * @param pageable  分页和排序参数
     * @return 分页后的课程笔记列表
     */
    @Query(value = "{ 'student_id': ?0, 'course_id': { $ne: null }, 'is_deleted': false, $or: [ { 'note_box': { $exists: false } }, { 'note_box': null }, { 'note_box': 'FREE' } ] }")
    Page<LearningNotes> findFreeCourseNotesByStudentId(String studentId, Pageable pageable);

    // 注意：创建和更新学习笔记请使用继承自MongoRepository的save()方法
}
