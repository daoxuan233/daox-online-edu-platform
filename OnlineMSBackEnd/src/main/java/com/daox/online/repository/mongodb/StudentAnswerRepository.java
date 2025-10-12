package com.daox.online.repository.mongodb;

import com.daox.online.entity.mongodb.StudentAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 学生答卷Repository接口
 * 提供学生答卷的CRUD操作和自定义查询方法
 */
@Repository
public interface StudentAnswerRepository extends MongoRepository<StudentAnswer, String> {

    /**
     * 根据测评ID和用户ID查询答卷（唯一）
     *
     * @param assessmentId 测评ID
     * @param userId       用户ID
     * @return 学生答卷
     */
    Optional<StudentAnswer> findByAssessmentIdAndUserId(String assessmentId, String userId);

    /**
     * 根据测评ID查询所有答卷
     *
     * @param assessmentId 测评ID
     * @param pageable     分页参数
     * @return 答卷分页列表
     */
    Page<StudentAnswer> findByAssessmentId(String assessmentId, Pageable pageable);

    /**
     * 根据用户ID查询答卷列表
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 答卷分页列表
     */
    Page<StudentAnswer> findByUserId(String userId, Pageable pageable);

    /**
     * 根据课程ID查询答卷列表
     *
     * @param courseId 课程ID
     * @param pageable 分页参数
     * @return 答卷分页列表
     */
    Page<StudentAnswer> findByCourseId(String courseId, Pageable pageable);

    /**
     * 根据状态查询答卷列表
     *
     * @param status   答卷状态
     * @param pageable 分页参数
     * @return 答卷分页列表
     */
    Page<StudentAnswer> findByStatus(String status, Pageable pageable);

    /**
     * 根据测评ID和状态查询答卷列表
     *
     * @param assessmentId 测评ID
     * @param status       答卷状态
     * @return 答卷列表
     */
    List<StudentAnswer> findByAssessmentIdAndStatus(String assessmentId, String status);

    /**
     * 根据课程ID和用户ID查询答卷列表
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 答卷分页列表
     */
    Page<StudentAnswer> findByCourseIdAndUserId(String courseId, String userId, Pageable pageable);

    /**
     * 统计测评的提交数量
     *
     * @param assessmentId 测评ID
     * @return 提交数量
     */
    long countByAssessmentId(String assessmentId);

    /**
     * 统计测评的已批阅数量
     *
     * @param assessmentId 测评ID
     * @return 已批阅数量
     */
    long countByAssessmentIdAndStatus(String assessmentId, String status);

    /**
     * 查询测评的平均分
     *
     * @param assessmentId 测评ID
     * @return 平均分统计结果
     */
    @Query(value = "{'assessment_id': ?0, 'status': 'graded'}", fields = "{'total_score': 1}")
    List<StudentAnswer> findScoresByAssessmentId(String assessmentId);

    /**
     * 根据分数范围查询答卷
     *
     * @param assessmentId 测评ID
     * @param minScore     最低分
     * @param maxScore     最高分
     * @param pageable     分页参数
     * @return 答卷分页列表
     */
    @Query("{'assessment_id': ?0, 'total_score': {$gte: ?1, $lte: ?2}, 'status': 'graded'}")
    Page<StudentAnswer> findByAssessmentIdAndScoreRange(String assessmentId, BigDecimal minScore, BigDecimal maxScore, Pageable pageable);

    /**
     * 根据提交时间范围查询答卷
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageable  分页参数
     * @return 答卷分页列表
     */
    Page<StudentAnswer> findBySubmitTimeBetween(Date startTime, Date endTime, Pageable pageable);

    /**
     * 查询需要批阅的答卷（状态为submitted或grading）
     *
     * @param pageable 分页参数
     * @return 答卷分页列表
     */
    @Query("{'status': {$in: ['submitted', 'grading']}}")
    Page<StudentAnswer> findPendingGradingAnswers(Pageable pageable);

    /**
     * 根据课程ID查询需要批阅的答卷
     *
     * @param courseId 课程ID
     * @param pageable 分页参数
     * @return 答卷分页列表
     */
    @Query("{'course_id': ?0, 'status': {$in: ['submitted', 'grading']}}")
    Page<StudentAnswer> findPendingGradingAnswersByCourseId(String courseId, Pageable pageable);

    /**
     * 根据测评ID列表和用户ID查询答卷列表
     *
     * @param assessmentIds 测评ID列表
     * @param userId        用户ID
     * @return 答卷列表
     */
    @Query("{'assessment_id': {$in: ?0}, 'user_id': ?1}")
    List<StudentAnswer> findByAssessmentIdInAndUserId(List<String> assessmentIds, String userId);

}