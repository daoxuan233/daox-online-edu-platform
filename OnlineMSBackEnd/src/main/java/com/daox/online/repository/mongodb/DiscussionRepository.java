package com.daox.online.repository.mongodb;

import com.daox.online.entity.mongodb.Discussion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 讨论区Repository接口
 * 提供讨论帖的CRUD操作和自定义查询方法
 */
@Repository
public interface DiscussionRepository extends MongoRepository<Discussion, String> {

    /**
     * 根据课程ID查询讨论帖列表（按创建时间倒序）
     *
     * @param courseId 课程ID
     * @return 非分页的讨论帖列表
     */
    List<Discussion> findByCourseIdOrderByCreatedAtDesc(String courseId);

    /**
     * 根据课程ID查询置顶帖子
     *
     * @param courseId 课程ID
     * @return 置顶帖子列表
     */
    List<Discussion> findByCourseIdAndIsPinnedTrueOrderByCreatedAtDesc(String courseId);

    /**
     * 根据课程ID查询提问帖
     *
     * @param courseId 课程ID
     * @return 非分页的提问帖列表
     */
    List<Discussion> findByCourseIdAndIsQuestionTrueOrderByCreatedAtDesc(String courseId);

    /**
     * 根据课程ID查询未解决的提问帖
     *
     * @param courseId 课程ID
     * @return [非分页] 未解决提问帖分页列表
     */
    List<Discussion> findByCourseIdAndIsQuestionTrueAndIsResolvedFalseOrderByCreatedAtDesc(String courseId);

    /**
     * 根据作者ID查询讨论帖
     *
     * @param authorId 作者ID
     * @return [非分页] 讨论帖分页列表
     */
    @Query("{'author.id': ?0}")
    List<Discussion> findByAuthorId(String authorId);

    /**
     * 根据课程ID和作者ID查询讨论帖
     *
     * @param courseId 课程ID
     * @param authorId 作者ID
     * @return [非分页] 讨论帖分页列表
     */
    @Query("{'course_id': ?0, 'author.id': ?1}")
    List<Discussion> findByCourseIdAndAuthorId(String courseId, String authorId);

    /**
     * 根据标题关键词搜索讨论帖
     *
     * @param courseId 课程ID
     * @param keyword  关键词
     * @return [非分页] 讨论帖分页列表
     */
    @Query("{'course_id': ?0, 'title': {$regex: ?1, $options: 'i'}}")
    List<Discussion> findByCourseIdAndTitleContainingIgnoreCase(String courseId, String keyword);

    /**
     * 根据内容关键词搜索讨论帖
     *
     * @param courseId 课程ID
     * @param keyword  关键词
     * @return [非分页] 讨论帖分页列表
     */
    @Query("{'course_id': ?0, 'content': {$regex: ?1, $options: 'i'}}")
    List<Discussion> findByCourseIdAndContentContainingIgnoreCase(String courseId, String keyword);

    /**
     * 根据标题或内容关键词搜索讨论帖
     *
     * @param courseId 课程ID
     * @param keyword  关键词
     * @return [非分页] 讨论帖分页列表
     */
    @Query("{'course_id': ?0, $or: [{'title': {$regex: ?1, $options: 'i'}}, {'content': {$regex: ?1, $options: 'i'}}]}")
    List<Discussion> findByCourseIdAndTitleOrContentContainingIgnoreCase(String courseId, String keyword);

    /**
     * 根据点赞数排序查询热门讨论帖
     *
     * @param courseId 课程ID
     * @return [非分页] 讨论帖分页列表
     */
    List<Discussion> findByCourseIdOrderByVotesDesc(String courseId);

    /**
     * 根据时间范围查询讨论帖
     *
     * @param courseId  课程ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return [非分页] 讨论帖分页列表
     */
    List<Discussion> findByCourseIdAndCreatedAtBetween(String courseId, Date startTime, Date endTime);

    /**
     * 统计课程的讨论帖数量
     *
     * @param courseId 课程ID
     * @return 讨论帖数量
     */
    long countByCourseId(String courseId);

    /**
     * 统计课程的提问帖数量
     *
     * @param courseId 课程ID
     * @return 提问帖数量
     */
    long countByCourseIdAndIsQuestionTrue(String courseId);

    /**
     * 统计课程的未解决提问帖数量
     *
     * @param courseId 课程ID
     * @return 未解决提问帖数量
     */
    long countByCourseIdAndIsQuestionTrueAndIsResolvedFalse(String courseId);

    /**
     * 统计用户的发帖数量
     *
     * @param authorId 作者ID
     * @return 发帖数量
     */
    @Query(value = "{'author.id': ?0}", count = true)
    long countByAuthorId(String authorId);
}