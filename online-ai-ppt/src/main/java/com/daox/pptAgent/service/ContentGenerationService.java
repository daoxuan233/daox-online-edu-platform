package com.daox.pptAgent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI内容生成服务
 * 负责根据主题和要求生成实际的PPT内容
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentGenerationService {

    private final ChatClient.Builder chatClientBuilder;

    /**
     * 生成PPT大纲的详细内容
     *
     * @param topic      主题
     * @param audience   目标受众
     * @param pageCount  页数
     * @return 包含实际内容的幻灯片列表
     */
    public List<SlideContent> generatePptContent(String topic, String audience, int pageCount) {
        log.info("开始生成PPT内容 - 主题: {}, 受众: {}, 页数: {}", topic, audience, pageCount);
        
        List<SlideContent> slides = new ArrayList<>();
        ChatClient chatClient = chatClientBuilder.build();
        
        try {
            // 1. 生成封面内容
            slides.add(generateCoverSlide(chatClient, topic, audience));
            
            // 2. 生成目录
            List<String> sections = generateTableOfContents(chatClient, topic, audience, pageCount);
            slides.add(new SlideContent("目录", sections));
            
            // 3. 为每个章节生成详细内容
            int contentPages = Math.max(pageCount - 4, 3); // 减去封面、目录、总结、感谢页
            int pagesPerSection = Math.max(contentPages / sections.size(), 1);
            
            for (String section : sections) {
                // 为每个章节生成多个详细页面
                for (int i = 0; i < pagesPerSection && slides.size() < pageCount - 2; i++) {
                    SlideContent content = generateDetailedContent(chatClient, topic, section, audience, i);
                    slides.add(content);
                }
            }
            
            // 4. 生成总结页
            slides.add(generateSummarySlide(chatClient, topic, slides));
            
            // 5. 生成感谢页
            slides.add(generateThankYouSlide(chatClient, topic, audience));
            
        } catch (Exception e) {
            log.error("生成PPT内容失败", e);
            // 返回默认内容
            return generateDefaultContent(topic, audience, pageCount);
        }
        
        log.info("PPT内容生成完成，共 {} 页", slides.size());
        return slides;
    }

    /**
     * 生成封面内容
     */
    private SlideContent generateCoverSlide(ChatClient chatClient, String topic, String audience) {
        String prompt = String.format("""
            为主题"%s"生成PPT封面页内容。
            目标受众：%s
            
            请生成3-4个要点，包括：
            1. 副标题或说明
            2. 演讲者/组织信息
            3. 日期
            4. 其他相关信息
            
            直接返回要点内容，每个要点一行，不要编号。
            """, topic, audience);
        
        String response = chatClient.prompt()
            .user(prompt)
            .call()
            .content();
        
        List<String> points = parseResponseToList(response);
        // 确保包含基本信息
        if (points.isEmpty()) {
            points.add("为" + audience + "精心准备");
            points.add("深度解析与实践指南");
        }
        points.add("日期: " + java.time.LocalDate.now());
        
        return new SlideContent("关于《" + topic + "》的专业汇报", points);
    }

    /**
     * 生成目录
     */
    private List<String> generateTableOfContents(ChatClient chatClient, String topic, String audience, int pageCount) {
        int sectionCount = Math.min(Math.max((pageCount - 4) / 2, 3), 5); // 3-5个章节
        
        String prompt = String.format("""
            为主题"%s"的PPT生成%d个主要章节标题。
            目标受众：%s
            
            要求：
            1. 章节标题要专业、简洁
            2. 符合逻辑顺序
            3. 覆盖主题的关键方面
            
            直接返回章节标题，每个标题一行，不要编号。
            """, topic, sectionCount, audience);
        
        String response = chatClient.prompt()
            .user(prompt)
            .call()
            .content();
        
        List<String> sections = parseResponseToList(response);
        
        // 确保至少有基本章节
        if (sections.size() < 3) {
            sections.clear();
            sections.add("背景介绍");
            sections.add("核心内容");
            sections.add("实践应用");
        }
        
        return sections.subList(0, Math.min(sections.size(), sectionCount));
    }

    /**
     * 生成详细内容页
     */
    private SlideContent generateDetailedContent(ChatClient chatClient, String topic, String section, String audience, int subIndex) {
        String slideTitle = subIndex == 0 ? section : section + " - 详细说明" + (subIndex > 0 ? " (" + (subIndex + 1) + ")" : "");
        
        String prompt = String.format("""
            为PPT主题"%s"的章节"%s"生成详细内容。
            目标受众：%s
            这是该章节的第%d页。
            
            要求：
            1. 生成4-6个要点
            2. 每个要点要有实质性内容
            3. 包含具体的数据、案例或说明
            4. 语言专业但易懂
            5. 符合目标受众的认知水平
            
            直接返回要点内容，每个要点一行。
            """, topic, section, audience, subIndex + 1);
        
        String response = chatClient.prompt()
            .user(prompt)
            .call()
            .content();
        
        List<String> points = parseResponseToList(response);
        
        // 确保有内容
        if (points.isEmpty()) {
            points.add("关键概念：深入理解" + section + "的核心要素");
            points.add("实施方法：系统化的执行步骤与最佳实践");
            points.add("案例分析：成功案例与经验总结");
            points.add("注意事项：避免常见误区与风险");
        }
        
        return new SlideContent(slideTitle, points.subList(0, Math.min(points.size(), 6)));
    }

    /**
     * 生成总结页
     */
    private SlideContent generateSummarySlide(ChatClient chatClient, String topic, List<SlideContent> previousSlides) {
        // 收集主要章节
        List<String> mainSections = new ArrayList<>();
        for (SlideContent slide : previousSlides) {
            if (!slide.title.contains("封面") && !slide.title.contains("目录") && 
                !slide.title.contains("详细说明")) {
                mainSections.add(slide.title);
            }
        }
        
        String prompt = String.format("""
            为PPT主题"%s"生成总结页内容。
            
            已覆盖的主要内容：%s
            
            请生成4-5个总结要点，包括：
            1. 核心观点回顾
            2. 关键发现或结论
            3. 实践建议
            4. 未来展望
            
            直接返回要点内容，每个要点一行。
            """, topic, String.join(", ", mainSections));
        
        String response = chatClient.prompt()
            .user(prompt)
            .call()
            .content();
        
        List<String> points = parseResponseToList(response);
        
        if (points.isEmpty()) {
            points.add("核心要点：全面掌握" + topic + "的关键知识");
            points.add("实践价值：将理论转化为实际应用");
            points.add("持续改进：建立持续学习和优化机制");
            points.add("未来展望：把握发展趋势，创造更大价值");
        }
        
        return new SlideContent("总结与展望", points);
    }

    /**
     * 生成感谢页
     */
    private SlideContent generateThankYouSlide(ChatClient chatClient, String topic, String audience) {
        List<String> points = new ArrayList<>();
        points.add("感谢您的关注与支持");
        points.add("欢迎提问与交流");
        points.add("期待进一步的合作机会");
        
        // 根据受众定制化
        if (audience.contains("技术") || audience.contains("开发")) {
            points.add("技术交流：tech@example.com");
        } else if (audience.contains("商业") || audience.contains("管理")) {
            points.add("商务合作：business@example.com");
        } else {
            points.add("联系方式：contact@example.com");
        }
        
        return new SlideContent("谢谢", points);
    }

    /**
     * 解析AI响应为列表
     */
    private List<String> parseResponseToList(String response) {
        List<String> result = new ArrayList<>();
        if (response != null && !response.isEmpty()) {
            String[] lines = response.split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                // 去除编号和无效行
                if (!trimmed.isEmpty()) {
                    // 去除常见的编号格式
                    trimmed = trimmed.replaceAll("^\\d+[.)、．]\\s*", "");
                    trimmed = trimmed.replaceAll("^[•·▪▫◦‣⁃]\\s*", "");
                    trimmed = trimmed.replaceAll("^[-*]\\s*", "");
                    if (!trimmed.isEmpty()) {
                        result.add(trimmed);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 生成默认内容（降级方案）
     */
    private List<SlideContent> generateDefaultContent(String topic, String audience, int pageCount) {
        log.warn("使用默认内容生成方案");
        List<SlideContent> slides = new ArrayList<>();
        
        // 封面
        slides.add(new SlideContent(
            "关于《" + topic + "》的专业汇报",
            List.of("为" + audience + "精心准备", "深度解析与实践指南", "日期: " + java.time.LocalDate.now())
        ));
        
        // 目录
        slides.add(new SlideContent("目录", List.of("背景介绍", "核心概念", "实施方案", "案例分析", "总结展望")));
        
        // 背景介绍
        slides.add(new SlideContent("背景介绍", 
            List.of(
                "行业现状：当前" + topic + "领域的发展状况",
                "市场需求：用户和市场的实际需求分析",
                "技术趋势：最新技术发展方向",
                "挑战机遇：面临的挑战与潜在机会"
            )
        ));
        
        // 核心概念
        slides.add(new SlideContent("核心概念",
            List.of(
                "基本定义：明确" + topic + "的核心含义",
                "关键要素：构成要素与相互关系",
                "理论基础：支撑的理论框架",
                "实践意义：对" + audience + "的价值"
            )
        ));
        
        // 添加更多内容页直到达到目标页数
        int currentPage = slides.size();
        while (currentPage < pageCount - 2) { // 留出总结和感谢页
            slides.add(new SlideContent(
                "实施方案 " + (currentPage - 3),
                List.of(
                    "步骤" + (currentPage - 3) + ".1：准备阶段的关键任务",
                    "步骤" + (currentPage - 3) + ".2：执行过程的注意事项",
                    "步骤" + (currentPage - 3) + ".3：质量控制与风险管理",
                    "步骤" + (currentPage - 3) + ".4：效果评估与优化改进"
                )
            ));
            currentPage++;
        }
        
        // 总结
        slides.add(new SlideContent("总结与展望",
            List.of(
                "核心要点回顾",
                "实施建议总结",
                "预期效果分析",
                "未来发展方向"
            )
        ));
        
        // 感谢页
        slides.add(new SlideContent("谢谢",
            List.of("感谢您的关注", "欢迎提问交流", "联系方式：contact@example.com")
        ));
        
        return slides;
    }

    /**
     * 幻灯片内容数据类
     */
    public static class SlideContent {
        public final String title;
        public final List<String> bulletPoints;
        
        public SlideContent(String title, List<String> bulletPoints) {
            this.title = title;
            this.bulletPoints = bulletPoints;
        }
    }
}