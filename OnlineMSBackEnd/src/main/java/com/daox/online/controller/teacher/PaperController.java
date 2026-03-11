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
            // 对于新建测评尚未组卷的场景，返回“未找到试卷”而不是500系统错误。
            // 这样前端可以按业务语义展示“暂无试卷内容”，而不是把它当成系统异常。
            log.info("[findPaperByAssessmentId.method] 未找到试卷: assessmentId={}", assessmentId);
            return RestBean.failure(404, "未找到与该测评关联的试卷");
        }
        return RestBean.success(paper);
    }

}
