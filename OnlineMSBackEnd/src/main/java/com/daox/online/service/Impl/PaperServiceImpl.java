package com.daox.online.service.Impl;

import com.daox.online.entity.mysql.Assessments;
import com.daox.online.entity.mongodb.Paper;
import com.daox.online.entity.mongodb.Question;
import com.daox.online.entity.mongodb.dto.paper.*;
import com.daox.online.mapper.AssessmentsMapper;
import com.daox.online.repository.mongodb.PaperRepository;
import com.daox.online.repository.mongodb.QuestionRepository;
import com.daox.online.repository.redis.AssessmentSessionRepository;
import com.daox.online.service.PaperService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor // 自动注入所有 final 字段的构造函数
public class PaperServiceImpl implements PaperService {

    @Resource
    private PaperRepository paperRepository;
    @Resource
    private AssessmentsMapper assessmentsMapper;
    @Resource
    private QuestionRepository questionRepository;

    private final AssessmentSessionRepository sessionRepository;

    /**
     * 创建试卷
     *
     * @param paper 试卷信息
     * @return 创建结果
     */
    @Override
    public Paper createPaper(Paper paper) {
        log.info("[createPaper.method] 接收到的 paper: {}", paper);
        // 1. 基础校验：检查paper对象本身和核心ID
        if (paper == null) {
            log.warn("[createPaper.method] 接收到的 paper 对象为 null");
            return null;
        }
        if (!StringUtils.hasText(paper.getAssessmentId())) {
            log.warn("[createPaper.method] AssessmentId 为空");
            return null;
        }
        if (!StringUtils.hasText(paper.getCreatorId())) {
            log.warn("[createPaper.method] CreatorId 为空");
            return null;
        }
        if (!StringUtils.hasText(paper.getCourseId())) {
            log.warn("[createPaper.method] CourseId 为空");
            return null;
        }
        Assessments assessmentById = assessmentsMapper.getAssessmentById(paper.getAssessmentId());

        if (paper.getCreatedAt() == null) {
            paper.setCreatedAt(LocalDateTime.now());
        }
        paper.setUpdatedAt(LocalDateTime.now());

        // 检查 assessmentId 是否存在
        if (assessmentById == null) {
            log.warn("[createPaper.method] 关联的测评 (Assessment) 不存在, AssessmentId: {}", paper.getAssessmentId());
            return null;
        }
        // 3. 业务逻辑校验：试卷结构和内容是否有效
        if (CollectionUtils.isEmpty(paper.getSections())) {
            log.warn("[createPaper.method] 试卷的 sections (题目区段) 不能为空");
            return null;
        }
        // 可以在这里添加更详细的校验，例如检查总分是否大于0，每个区段是否都有题目等

        paperRepository.findFirstByAssessmentIdOrderByUpdatedAtDescCreatedAtDesc(paper.getAssessmentId())
                .ifPresent(existingPaper -> {
                    paper.setId(existingPaper.getId());
                    if (paper.getCreatedAt() == null) {
                        paper.setCreatedAt(existingPaper.getCreatedAt());
                    }
                });

        log.info("[createPaper.method] 试卷校验通过，准备保存。AssessmentId: {}", paper.getAssessmentId());
        return paperRepository.save(paper);
    }

    /**
     * 通过assessmentId查找试卷
     *
     * @param assessmentId 测评ID
     * @return 试卷信息
     */
    @Override
    public PaperDTO findPaperByAssessmentId(String assessmentId) {
        Assessments assessmentByIdANDNotDelete = assessmentsMapper.getAssessmentByIdANDNotDelete(assessmentId);
        if (assessmentByIdANDNotDelete == null) {
            log.warn("[findPaperByAssessmentId.method] 关联的测评 (Assessment) 不存在, AssessmentId: {}", assessmentId);
            return null;
        }
        // 这里是“编辑器预加载”场景：新建测评后试卷可能尚未创建，这属于正常业务状态。
        // 因此本方法采用“宽松查询”策略：没有试卷时返回 null，由上层决定是否提示“暂无试卷内容”。
        // 这样可以避免把“未组卷”误判为系统异常并返回 500。
        Paper paperByAssessmentId = findLatestPaperByAssessmentIdOrNull(assessmentId);
        if (paperByAssessmentId == null) {
            return null;
        }
        PaperDTO paperDTO = new PaperDTO();
        paperDTO.setId(paperByAssessmentId.getId());
        paperDTO.setTitle(paperByAssessmentId.getTitle());
        paperDTO.setDescription(paperByAssessmentId.getDescription());
        paperDTO.setTotalScore(paperByAssessmentId.getTotalScore());
        paperDTO.setCreatedAt(paperByAssessmentId.getCreatedAt());
        paperDTO.setUpdatedAt(paperByAssessmentId.getUpdatedAt());
        if (paperByAssessmentId.getSections() != null) {
            List<PaperDTO.PaperSection> paperSections = paperByAssessmentId.getSections().stream().map(section -> {
                PaperDTO.PaperSection paperSectionDTO = new PaperDTO.PaperSection();
                paperSectionDTO.setTitle(section.getTitle());
                paperSectionDTO.setDescription(section.getDescription());

                if (section.getQuestions() != null) {
                    List<PaperDTO.PaperQuestion> paperQuestions = section.getQuestions().stream().map(q -> {
                        PaperDTO.PaperQuestion paperQuestionDTO = new PaperDTO.PaperQuestion();
                        paperQuestionDTO.setScore(q.getScore());
                        paperQuestionDTO.setOrderIndex(q.getOrderIndex());
                        questionRepository.findById(q.getQuestionId()).ifPresent(paperQuestionDTO::setQuestion);
                        return paperQuestionDTO;
                    }).collect(Collectors.toList());
                    paperSectionDTO.setQuestions(paperQuestions);
                } else {
                    paperSectionDTO.setQuestions(Collections.emptyList());
                }
                return paperSectionDTO;
            }).collect(Collectors.toList());
            paperDTO.setSections(paperSections);
        } else {
            paperDTO.setSections(Collections.emptyList());
        }

        return paperDTO;
    }

    /**
     * 根据测评ID获取试卷及其包含的所有题目的详细信息
     *
     * @param assessmentId 测评ID
     * @return 包含试卷和题目Map的详情对象
     */
    @Override
    public PaperDetails getPaperDetails(String assessmentId) {
        // 1. 查找试卷
        Paper paper = findLatestPaperByAssessmentId(assessmentId);

        // 2. 提取所有题目ID
        List<String> questionIds = paper.getSections().stream()
                .flatMap(section -> section.getQuestions().stream())
                .map(Paper.PaperQuestion::getQuestionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if(questionIds.isEmpty()){
            throw new IllegalStateException("试卷中不包含任何题目: " + assessmentId);
        }

        // 3. 一次性批量查询所有题目
        Map<String, Question> questionsMap = questionRepository.findAllById(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        return new PaperDetails(paper, questionsMap);
    }

    private Paper findLatestPaperByAssessmentId(String assessmentId) {
        List<Paper> papers = paperRepository.findAllByAssessmentIdOrderByUpdatedAtDescCreatedAtDesc(assessmentId);
        if (papers == null || papers.isEmpty()) {
            throw new IllegalStateException("未找到与测评ID关联的试卷: " + assessmentId);
        }
        if (papers.size() > 1) {
            log.warn("[findLatestPaperByAssessmentId.method] 测评存在多份试卷记录，将使用最新一份。AssessmentId: {}, count: {}", assessmentId, papers.size());
        }
        return papers.get(0);
    }

    /**
     * 查询指定测评的最新试卷（宽松模式）
     * 适用于“新建测评后进入组卷页”这类允许暂时没有试卷的场景。
     *
     * @param assessmentId 测评ID
     * @return 最新试卷；若不存在则返回 null
     */
    private Paper findLatestPaperByAssessmentIdOrNull(String assessmentId) {
        List<Paper> papers = paperRepository.findAllByAssessmentIdOrderByUpdatedAtDescCreatedAtDesc(assessmentId);
        if (papers == null || papers.isEmpty()) {
            return null;
        }
        if (papers.size() > 1) {
            log.warn("[findLatestPaperByAssessmentIdOrNull.method] 测评存在多份试卷记录，将使用最新一份。AssessmentId: {}, count: {}", assessmentId, papers.size());
        }
        return papers.get(0);
    }

    /**
     * 为学生获取试卷内容 (包含权限校验和数据安全过滤)
     *
     * @param assessmentId 测评 ID
     * @param userId       学生 ID
     * @return 用于前端展示的 PaperDTO
     */
    @Override
    public StuPaperDTO getPaperForStudent(String assessmentId, String userId) {
        // 1. [核心] 权限校验：检查学生是否已开始考试
        boolean hasSession = sessionRepository.existsById(assessmentId, userId);
        if (!hasSession) {
            throw new IllegalStateException("您尚未开始本次测评或会话已过期，无法获取试卷。");
        }

        // 2. 获取原始数据 (复用已有方法)
        PaperDetails paperDetails = getPaperDetails(assessmentId);

        // 3. 将实体对象转换为安全 DTO
        return mapToPaperDTO(paperDetails);
    }

    /**
     * [私有] 将 PaperDetails 实体映射到安全的 PaperDTO
     *
     * @param paperDetails 包含 Paper 和 QuestionsMap 的原始数据
     * @return PaperDTO
     */
    private StuPaperDTO mapToPaperDTO(PaperDetails paperDetails) {
        Paper paper = paperDetails.paper();
        Map<String, Question> questionsMap = paperDetails.questionsMap();

        List<PaperSectionDTO> sectionDTOs = paper.getSections().stream()
                .map(section -> {
                    // `questionDTOs` 的生成逻辑保持不变
                    List<PaperQuestionDTO> questionDTOs = section.getQuestions().stream()
                            .map(paperQuestion -> {
                                Question question = questionsMap.get(paperQuestion.getQuestionId());
                                if (question == null) return null;

                                QuestionDTO safeQuestionDetails = new QuestionDTO(
                                        question.getId(),
                                        question.getType(),
                                        question.getStem(),
                                        question.getOptions()
                                );

                                return new PaperQuestionDTO(
                                        paperQuestion.getQuestionId(),
                                        paperQuestion.getScore(),
                                        paperQuestion.getOrderIndex(),
                                        safeQuestionDetails
                                );
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    // 1. 计算该分区的题目总数
                    int sectionTotalQuestions = questionDTOs.size();

                    // 2. 计算该分区的总分
                    BigDecimal sectionTotalScore = questionDTOs.stream()
                            .map(PaperQuestionDTO::score)
                            .reduce(BigDecimal.ZERO, BigDecimal::add); // 使用 reduce 安全地累加 BigDecimal

                    // 3. 创建包含新字段的 DTO
                    return new PaperSectionDTO(
                            section.getTitle(),
                            section.getDescription(),
                            sectionTotalQuestions, // 传入计算出的总题数
                            sectionTotalScore,     // 传入计算出的总分
                            questionDTOs
                    );
                })
                .collect(Collectors.toList());

        // paper 级别的总题数计算逻辑保持不变
        int totalQuestions = sectionDTOs.stream().mapToInt(PaperSectionDTO::totalQuestions).sum();
        // 考试时间
        Assessments assessmentById = assessmentsMapper.getAssessmentById(paper.getAssessmentId());

        return new StuPaperDTO(
                paper.getId(),
                paper.getTitle(),
                paper.getDescription(),
                paper.getTotalScore(),
                totalQuestions,
                assessmentById.getDurationMinutes(),
                sectionDTOs
        );
    }
}
