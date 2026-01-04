package com.daox.pptAgent.service;

import com.daox.pptAgent.engine.PptGenerationEngine;
import com.daox.pptAgent.entity.vo.GenerationTask;
import com.daox.pptAgent.model.SessionState;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI的工具箱配置
 * 职责：定义所有可以被大语言模型调用的函数 (Function Calling)。
 * 每个@Bean都是一个独立的、原子化的工具。
 */
@Slf4j
@Service
@RequiredArgsConstructor // 自动注入所有 final 字段的构造函数
public class PptToolbox {

    private final PptGenerationEngine pptEngine;
    private final TaskStatusManager taskStatusManager;
    private final SessionManager sessionManager;

    // --- 数据记录 (Records) 定义 ---
    // 使用Java Record来定义函数的输入和输出参数，结构清晰，且自带序列化能力。
    // @JsonPropertyDescription 为每个字段提供了清晰的描述，AI会根据这些描述来理解如何填充参数。

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("用于规划PPT大纲的请求参数")
    public record OutlineRequest(
            @JsonProperty(value = "topic") @JsonPropertyDescription("演示文稿的核心主题") String topic,

            @JsonProperty(value = "audience") @JsonPropertyDescription("目标受众，例如：技术开发者、商业高管、学生") String audience,

            @JsonProperty(value = "pageCount") @JsonPropertyDescription("期望生成的幻灯片大致页数") int pageCount) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("结构化的PPT大纲，包含所有幻灯片的标题和要点")
    public record PptOutline(@JsonProperty(value = "title") @JsonPropertyDescription("整个PPT的总标题") String title,

                             @JsonProperty(value = "slides") @JsonPropertyDescription("所有幻灯片的结构化列表") List<Slide> slides) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Slide(@JsonProperty(value = "title") @JsonPropertyDescription("单张幻灯片的标题") String title,

                        @JsonProperty(value = "bulletPoints") @JsonPropertyDescription("幻灯片的主要内容，以要点列表形式呈现") List<String> bulletPoints) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("用于生成最终PPT文件的请求参数")
    public record FinalPptRequest(
            @JsonProperty(value = "outlineTitle") @JsonPropertyDescription("PPT的标题") String outlineTitle) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PptResult(@JsonPropertyDescription("生成的PPT文件的本地存储路径") String filePath,

                            @JsonPropertyDescription("给用户的友好成功消息") String message) {
    }


    // --- 工具Bean定义 ---
    // 在 Spring AI 1.0.0 中，使用 @Bean + @Description 注解来定义可被AI调用的工具函数
    // Bean的名称将作为工具的标识符

    @Tool(description = "第一步：根据用户的主题和要求，规划PPT大纲。这是制作PPT流程的起点。")
    public PptOutline createPptOutline(OutlineRequest request, ToolContext toolContext) {
        log.info("AI请求生成大纲，收到参数: topic={}, audience={}, pageCount={}", request.topic(), request.audience(), request.pageCount());

        // 从 ToolContext 中获取 userId
        String userId = (String) toolContext.getContext().get("userId");
        log.info("[createPptOutline.method] 获取到用户ID: {}", userId);

        // 构建动态的PPT大纲
        List<Slide> slides = new ArrayList<>();

        // 添加封面页
        slides.add(new Slide("封面页", List.of("主题: " + request.topic(), "为" + request.audience() + "准备", "日期: " + java.time.LocalDate.now())));

        // 添加目录页
        slides.add(new Slide("目录", List.of("背景介绍", "核心内容", "重点分析", "总结与展望")));

        // 根据页数要求动态生成内容页
        int contentPages = Math.max(request.pageCount() - 4, 2); // 减去封面、目录、总结、谢谢页

        // 添加背景介绍页
        if (contentPages > 0) {
            slides.add(new Slide("背景介绍", List.of("当前现状分析", "问题与挑战", "解决方案的必要性")));
            contentPages--;
        }

        // 添加核心内容页
        for (int i = 0; i < Math.min(contentPages, 3); i++) {
            slides.add(new Slide("核心内容 " + (i + 1), List.of("关键要点 " + ((i * 3) + 1), "关键要点 " + ((i * 3) + 2), "关键要点 " + ((i * 3) + 3), "支持数据或案例")));
        }

        // 添加总结页
        slides.add(new Slide("总结与展望", List.of("核心观点回顾", "重要成果总结", "未来规划与展望")));

        // 添加谢谢页
        slides.add(new Slide("谢谢", List.of("感谢聆听", "问答环节", "联系方式")));

        String title = "关于《" + request.topic() + "》的专业汇报";
        log.info("生成大纲成功：标题={}, 页数={}", title, slides.size());

        PptOutline outline = new PptOutline(title, slides);

        // 保存大纲到 session
        if (userId != null) {
            SessionState session = sessionManager.getSession(userId);
            session.setApprovedOutline(outline);
            sessionManager.saveSession(userId, session);
            log.info("已保存大纲到 session - 用户ID: {}", userId);
        }
        return outline;
    }

    /**
     * 生成最终的PPT文件
     *
     * @return GenerationTask 任务状态
     */
    @Tool(description = "最后一步：当用户确认大纲后，调用此工具生成最终的PPT文件。")
    public GenerationTask generateFinalPpt(FinalPptRequest request, ToolContext toolContext) {
        log.info("AI请求生成最终PPT，标题: {}", request.outlineTitle());

        // 1. 从 ToolContext 中获取 userId
        String userId = (String) toolContext.getContext().get("userId");
        log.info("获取到用户ID: {}", userId);

        // 2. 从 session 中获取已保存的大纲（必须存在并含有内容）
        SessionState session = sessionManager.getSession(userId);
        PptOutline outline = session.getApprovedOutline();
        
        if (outline == null) {
            throw new IllegalStateException("未在会话中找到已确认的大纲，请先调用 createPptOutline 获取并确认大纲。");
        }
        if (outline.slides() == null || outline.slides().isEmpty()) {
            throw new IllegalStateException("会话中的大纲不完整，缺少幻灯片内容，请重新生成并确认大纲。");
        }

        // 3. 创建一个异步任务
        var taskStatus = taskStatusManager.createTask();
        log.info("创建 PPT 生成任务，任务ID: {}", taskStatus.getTaskId());

        // 4. 异步调用生成引擎
        pptEngine.generatePptInBackground(taskStatus.getTaskId(), outline);

        // 3. 立即返回任务ID和提示信息
        String message = String.format("您的PPT生成任务（ID: %s）已启动，预计需要几分钟时间。您可以随时查询任务状态。", taskStatus.getTaskId());

        return new GenerationTask(taskStatus.getTaskId(), message);
    }
}
