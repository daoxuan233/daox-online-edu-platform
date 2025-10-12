package com.daox.online.repository.mongodb;

import com.daox.online.entity.mongodb.Paper;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Paper（试卷）集合的MongoDB数据访问接口
 * Spring Data MongoDB会自动实现这个接口的基础CRUD功能。
 */
@Repository
public interface PaperRepository extends MongoRepository<Paper, String> {

    /**
     * 根据课程ID查询所有试卷
     *
     * @param courseId 课程ID
     * @return 该课程下的试卷列表
     */
    List<Paper> findByCourseId(String courseId);

    /**
     * 根据测评ID查找试卷
     *
     * @param assessmentId 测评ID
     * @return 试卷 Optional
     */
    Optional<Paper> findByAssessmentId(String assessmentId);

    /**
     * 根据测评ID列表查找试卷
     * @param assessmentIds 测评ID列表
     * @return 试卷列表
     */
    List<Paper> findByAssessmentIdIn(List<String> assessmentIds);

}
