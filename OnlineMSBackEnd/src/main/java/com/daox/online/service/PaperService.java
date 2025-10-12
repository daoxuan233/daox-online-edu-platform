package com.daox.online.service;


import com.daox.online.entity.mongodb.Paper;
import com.daox.online.entity.mongodb.dto.paper.PaperDTO;
import com.daox.online.entity.mongodb.dto.paper.PaperDetails;
import com.daox.online.entity.mongodb.dto.paper.StuPaperDTO;

public interface PaperService {
    /**
     * 创建试卷
     * @param paper 试卷信息
     * @return 创建结果
     */
    Paper createPaper(Paper paper);

    /**
     * 通过assessmentId查找试卷
     * @param assessmentId 测评ID
     * @return 试卷信息
     */
    PaperDTO findPaperByAssessmentId(String assessmentId);

    /**
     * 根据测评ID获取试卷及其包含的所有题目的详细信息
     * @param assessmentId 测评ID
     * @return 包含试卷和题目Map的详情对象
     */
    PaperDetails getPaperDetails(String assessmentId);

    /**
     * 为学生获取试卷内容 (包含权限校验和数据安全过滤)
     *
     * @param assessmentId 测评 ID
     * @param userId       学生 ID
     * @return 用于前端展示的 PaperDTO
     */
    StuPaperDTO getPaperForStudent(String assessmentId, String userId);
}
