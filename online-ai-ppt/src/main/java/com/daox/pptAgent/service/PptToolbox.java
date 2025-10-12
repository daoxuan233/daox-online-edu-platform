package com.daox.pptAgent.service;

import com.daox.pptAgent.engine.PptGenerationEngine;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

/**
 * AI的工具箱配置
 * 职责：定义所有可以被大语言模型调用的函数 (Function Calling)。
 * 每个@Bean都是一个独立的、原子化的工具。
 */
@Configuration
@RequiredArgsConstructor // 自动注入所有 final 字段的构造函数
public class PptToolbox {

    private final PptGenerationEngine pptEngine;

    // --- 数据记录 (Records) 定义 ---
    // 使用Java Record来定义函数的输入和输出参数，结构清晰，且自带序列化能力。
    // @JsonPropertyDescription 为每个字段提供了清晰的描述，AI会根据这些描述来理解如何填充参数。

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("用于规划PPT大纲的请求参数")
    public record OutlineRequest(
            @JsonProperty(required = true)
            @JsonPropertyDescription("演示文稿的核心主题")
            String topic,

            @JsonProperty
            @JsonPropertyDescription("目标受众，例如：技术开发者、商业高管、学生")
            String audience,

            @JsonProperty(defaultValue = "5")
            @JsonPropertyDescription("期望生成的幻灯片大致页数")
            int pageCount
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("结构化的PPT大纲，包含所有幻灯片的标题和要点")
    public record PptOutline(
            @JsonProperty(required = true)
            @JsonPropertyDescription("整个PPT的總标题")
            String title,

            @JsonProperty(required = true)
            @JsonPropertyDescription("所有幻灯片的结构化列表")
            List<Slide> slides
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Slide(
            @JsonProperty(required = true)
            @JsonPropertyDescription("单张幻灯片的标题")
            String title,

            @JsonProperty(required = true)
            @JsonPropertyDescription("幻灯片的主要内容，以要点列表形式呈现")
            List<String> bulletPoints
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("用于生成最终PPT文件的请求参数")
    public record FinalPptRequest(
            @JsonProperty(required = true)
            @JsonPropertyDescription("已由用户确认的、包含所有幻灯片内容的完整大纲")
            PptOutline outline
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PptResult(
            @JsonProperty(required = true)
            @JsonPropertyDescription("生成的PPT文件的本地存储路径")
            String filePath,

            @JsonProperty(required = true)
            @JsonPropertyDescription("给用户的友好成功消息")
            String message
    ) {
    }


    // --- 工具Bean定义 ---

    @Bean("createPptOutline") // 显式指定Bean的名称，作为AI调用的函数名
    @Description("第一步：根据用户的主题和要求，规划PPT大纲。这是制作PPT流程的起点。")
    public Function<OutlineRequest, PptOutline> createPptOutline() {
        return request -> {
            // --- TODO: 在后续阶段，这里将调用LLM（非函数调用模式）来实际生成内容 ---
            // 模型的创造力将在这里发挥作用（使用较高的temperature）。

            // 在第一阶段，我们打印请求并返回一个模拟的大纲，以验证流程。
            System.out.println("AI请求生成大纲，收到参数: " + request);

            // 模拟返回一个固定的大纲结构
            var slides = List.of(
                    new Slide("封面页", List.of("主题: " + request.topic(), "为 " + request.audience() + " 准备")),
                    new Slide("内容页一", List.of("要点1", "要点2")),
                    new Slide("内容页二", List.of("要点3", "要点4")),
                    new Slide("总结页", List.of("回顾与总结")),
                    new Slide("谢谢", List.of("Q&A环节"))
            );
            return new PptOutline("关于 " + request.topic() + " 的报告", slides);
        };
    }

    @Bean("generateFinalPpt") // 显式指定Bean的名称
    @Description("最后一步：接收用户确认后的大纲，生成最终的PPT文件。")
    public Function<FinalPptRequest, PptResult> generateFinalPpt() {
        return request -> {
            // 在这里，我们将调用PptGenerationEngine来完成实际的文件生成工作。
            System.out.println("AI请求生成最终PPT，收到大纲标题: " + request.outline().title());

            String filePath = pptEngine.createPresentation(request.outline().title());

            return new PptResult(filePath, "您的PPT演示文稿已生成！");
        };
    }
}
