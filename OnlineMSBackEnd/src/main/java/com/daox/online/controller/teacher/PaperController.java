package com.daox.online.controller.teacher;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.mongodb.Paper;
import com.daox.online.entity.mongodb.dto.paper.PaperDTO;
import com.daox.online.service.PaperService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 教师端 - 试卷相关控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/paper")
public class PaperController {

    @Resource
    private PaperService paperService;

    /**
     * 创建试卷
     * @param paper 试卷信息
     * @return 创建结果
     */
    @PostMapping("")
    public RestBean<?> createPaper(@RequestBody Paper paper) {
        Paper paper1 = paperService.createPaper(paper);
        if (paper1 == null){
            log.info("[createPaper.method] 创建失败");
            return RestBean.failure(500, "创建失败");
        }
        return RestBean.success(paper1);
    }

    /**
     * 通过assessmentId查询试卷
     * @param assessmentId 测评ID
     * @return 试卷信息
     */
    @GetMapping("/{assessmentId}")
    public RestBean<?> findPaperByAssessmentId(@PathVariable String assessmentId) {
        PaperDTO paper = paperService.findPaperByAssessmentId(assessmentId);
        if (paper == null){
            log.info("[findPaperByAssessmentId.method] 获取失败");
            return RestBean.failure(500, "获取失败");
        }
        return RestBean.success(paper);
    }

}
