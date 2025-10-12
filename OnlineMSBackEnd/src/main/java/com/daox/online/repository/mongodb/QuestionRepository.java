package com.daox.online.repository.mongodb;

import com.daox.online.entity.mongodb.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 题库Repository接口
 * 提供题目的CRUD操作和自定义查询方法
 */
@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {

    /**
     * 获取所有未删除的题目
     * @return 课程列表
     */
    List<Question> findAllByIsDeletedFalse();


    /**
     * 根据课程ID查询题目列表（排除已删除的题目）
     *
     * @param courseId 课程ID
     * @param pageable 分页参数
     * @return 题目分页列表
     */
    Page<Question> findByCourseIdAndIsDeletedFalse(String courseId, Pageable pageable);

    /**
     * 根据课程ID查询题目列表(排除已删除的题目) - 非分页
     *
     * @param courseId 课程ID
     * @return 题目列表
     */
    List<Question> findByCourseIdAndIsDeletedFalse(String courseId);

    /**
     * 根据题型查询题目列表 - 非分页
     *
     * @param type 题型
     * @return 题目列表
     */
    List<Question> findByTypeAndIsDeletedFalse(String type);

    /**
     * 根据课程ID和题型查询题目列表 - 非分页
     *
     * @param courseId 课程ID
     * @param type     题型
     * @return 题目列表
     */
    List<Question> findByCourseIdAndTypeAndIsDeletedFalse(String courseId, String type);

    /**
     * 根据创建者ID查询题目列表
     *
     * @param creatorId 创建者ID
     * @param pageable  分页参数
     * @return 题目分页列表
     */
    Page<Question> findByCreatorIdAndIsDeletedFalse(String creatorId, Pageable pageable);

    /**
     * 根据标签查询题目列表
     *
     * @param tag      标签
     * @param pageable 分页参数
     * @return 题目分页列表
     */
    Page<Question> findByTagsContainingAndIsDeletedFalse(String tag, Pageable pageable);

    /**
     * 根据课程ID和标签查询题目列表
     *
     * @param courseId 课程ID
     * @param tag      标签
     * @param pageable 分页参数
     * @return 题目分页列表
     */
    Page<Question> findByCourseIdAndTagsContainingAndIsDeletedFalse(String courseId, String tag, Pageable pageable);

    /**
     * 根据题干关键词搜索题目
     *
     * @param keyword  关键词
     * @param pageable 分页参数
     * @return 题目分页列表
     */
    @Query("{'stem': {$regex: ?0, $options: 'i'}, 'is_deleted': false}")
    Page<Question> findByStemContainingIgnoreCaseAndIsDeletedFalse(String keyword, Pageable pageable);

    /**
     * 根据题干关键词搜索题目 - 非分页
     *
     * @param keyword 关键词
     * @return 题目列表
     */
    @Query("{'stem': {$regex: ?0, $options: 'i'}, 'is_deleted': false}")
    List<Question> findByStemContainingIgnoreCaseAndIsDeletedFalse(String keyword);

    /**
     * 根据题干关键词和标签搜索题目 - 非分页
     *
     * @param keyword 题干关键词
     * @param tag     标签（部分匹配）
     * @return 匹配的题目列表
     */
    @Query("{'stem': {$regex: ?0, $options: 'i'}, 'tags': {$regex: ?1, $options: 'i'}, 'is_deleted': false}")
    List<Question> findByStemContainingIgnoreCaseAndTagsRegexAndIsDeletedFalse(String keyword, String tag);

    /**
     * 根据题干关键词、任意标签、难度级别同时搜索题目 - 非分页
     *
     * @param keyword    题干关键词（部分匹配，不区分大小写）
     * @param tag        标签（部分匹配，不区分大小写）
     * @param difficulty 难度级别
     * @return 匹配的题目列表
     */
    @Query("{'stem': {$regex: ?0, $options: 'i'}, " +
            "'tags': {$regex: ?1, $options: 'i'}, " +
            "'difficulty': ?2, " +
            "'is_deleted': false}")
    List<Question> findByStemAndTagsAndDifficultyAndNotDeleted(String keyword, String tag, String difficulty);


    /**
     * 根据课程ID和题干关键词搜索题目
     *
     * @param courseId 课程ID
     * @param keyword  关键词
     * @param pageable 分页参数
     * @return 题目分页列表
     */
    @Query("{'course_id': ?0, 'stem': {$regex: ?1, $options: 'i'}, 'is_deleted': false}")
    Page<Question> findByCourseIdAndStemContainingIgnoreCaseAndIsDeletedFalse(String courseId, String keyword, Pageable pageable);


    /**
     * 统计课程下的题目数量（排除已删除的题目）
     *
     * @param courseId 课程ID
     * @return 题目数量
     */
    long countByCourseIdAndIsDeletedFalse(String courseId);

    /**
     * 统计课程下指定题型的题目数量
     *
     * @param courseId 课程ID
     * @param type     题型
     * @return 题目数量
     */
    long countByCourseIdAndTypeAndIsDeletedFalse(String courseId, String type);

    /**
     * 根据ID列表批量查询题目
     *
     * @param ids 题目ID列表
     * @return 题目列表
     */
    List<Question> findByIdInAndIsDeletedFalse(List<String> ids);

    /**
     * 根据id查询题目 - （排除已删除的题目）
     *
     * @param id 题目id
     * @return 题目
     */
    Question findByIdAndIsDeletedFalse(String id);
}