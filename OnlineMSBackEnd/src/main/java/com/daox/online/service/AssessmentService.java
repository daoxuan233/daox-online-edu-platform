package com.daox.online.service;

import com.daox.online.entity.mysql.Assessments;
import com.daox.online.entity.dto.AssessmentCreateDTO;
import com.daox.online.entity.dto.AssessmentTeacherDTO;
import com.daox.online.entity.dto.SubmitAnswerRequest;
import com.daox.online.entity.dto.teacher.assessment.AssessmentResultSummaryDTO;
import com.daox.online.entity.redis.AssessmentSession;
import com.daox.online.entity.views.requestDTO.SubmitAnswerDTO;
import com.daox.online.entity.views.responseVO.assessment.AssessmentPaperVO;
import com.daox.online.entity.views.responseVO.assessment.AssessmentResultVO;
import com.daox.online.entity.views.responseVO.assessment.AssessmentStatusVO;
import com.daox.online.entity.views.responseVO.StudentAssessmentVO;

import java.util.List;
import java.util.Map;

public interface AssessmentService {

    /**
     * 教师端
     * 获取所有测评
     *
     * @return 所有测评
     */
    List<AssessmentTeacherDTO> allAssessments();

    /**
     * 获取学生的测评列表
     *
     * @param userId 用户ID
     * @param status 测评状态过滤（可选，传null表示不过滤）
     * @return 测评列表
     */
    List<StudentAssessmentVO> getStudentAssessments(String userId, String status);

    /**
     * 根据ID获取测评详情
     *
     * @param assessmentId 测评ID
     * @param userId       用户ID
     * @return 测评详情
     */
    StudentAssessmentVO getAssessmentById(String assessmentId, String userId);

    /**
     * 检查学生是否可以开始测评
     *
     * @param assessmentId 测评ID
     * @param userId       用户ID
     * @return 是否可以开始
     */
    boolean canStartAssessment(String assessmentId, String userId);

    /**
     * 获取试卷内容
     *
     * @param assessmentId 测评ID
     * @param userId       用户ID
     * @return 试卷内容
     */
    AssessmentPaperVO getAssessmentPaper(String assessmentId, String userId);

    /**
     * 开始一次新的测评
     *
     * @param assessmentId 测评ID
     * @param userId       学生ID
     * @return 创建的会话
     */
    AssessmentSession startAssessment(String assessmentId, String userId);

    /**
     * 保存学生答案
     *
     * @param assessmentId 测评ID
     * @param userId       学生ID
     * @param request      答案请求
     */
    void saveAnswer(String assessmentId, String userId, SubmitAnswerRequest request);

    /**
     * 提交答案
     *
     * @param assessmentId    测评ID
     * @param userId          用户ID
     * @param submitAnswerDTO 提交答案数据
     * @return 是否成功
     */
    boolean submitAnswer(String assessmentId, String userId, SubmitAnswerDTO submitAnswerDTO);

    /**
     * 提交试卷
     *
     * @param assessmentId 测评ID
     * @param userId       用户ID
     */
    void submitAssessment(String assessmentId, String userId);

    /**
     * 获取测评状态
     *
     * @param assessmentId 测评ID
     * @param userId       用户ID
     * @return 测评状态
     */
    AssessmentStatusVO getAssessmentStatus(String assessmentId, String userId);

    /**
     * 获取测评结果
     *
     * @param assessmentId 测评ID
     * @param userId       用户ID
     * @return 测评结果
     */
    AssessmentResultVO getAssessmentResult(String assessmentId, String userId);

    /**
     * 获取测评列表
     *
     * @param courseID 课程ID
     * @return 测评列表
     */
    List<Assessments> getAssessments(String courseID);

    /**
     * 创建测评
     *
     * @param assessmentCreateDTO 创建测评数据
     * @return 测评ID
     */
    Assessments createAssessment(AssessmentCreateDTO assessmentCreateDTO);

    /**
     * 获取测评详情
     *
     * @param assessmentId 测评ID
     * @return 测评详情
     */
    Assessments getAssessmentDetails(String assessmentId);

    /**
     * 更新测评
     *
     * @param userId       用户ID
     * @param assessmentId 测评ID
     * @param title        标题
     * @param duration     持续时间
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @param isPublished  是否发布
     * @return 更新后的结果
     */
    Assessments updateAssessment(
            String userId,
            String assessmentId,
            String title, int duration,
            String startTime, String endTime,
            int isPublished
    );

    /**
     * 删除测评 - 逻辑处理 - 将is_published = -1
     *
     * @param userId       用户ID
     * @param assessmentId 测评ID
     * @return 删除结果
     */
    boolean deleteAssessment(String userId, String assessmentId);

    /**
     * 发布测评
     *
     * @param userId       用户ID
     * @param assessmentId 测评ID
     * @return 发布结果
     */
    boolean publishAssessment(String userId, String assessmentId);

    /**
     * 获取试卷结构
     *
     * @param assessmentId 测评ID
     * @return 试卷结构
     */
    Map<String, Object> getAssessmentPaperStructure(String assessmentId);

    /**
     * 获取单个测评的结果汇总信息
     *
     * @param userId       当前登录教师ID
     * @param assessmentId 测评ID
     * @return 测评结果汇总
     */
    AssessmentResultSummaryDTO getAssessmentResultSummary(String userId, String assessmentId);
}
