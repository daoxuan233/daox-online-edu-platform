package com.daox.pptAgent.engine;

import com.daox.pptAgent.service.PptToolbox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PptGenerationEngine 测试类
 */
public class PptGenerationEngineTest {
    
    private PptGenerationEngine engine;
    
    @TempDir
    Path tempDir;
    
    @Test
    @DisplayName("测试创建简单PPT（仅标题）")
    void testCreatePresentationWithTitleOnly() {
        // 执行
        String filePath = engine.createPresentation("测试PPT标题");
        
        // 验证
        assertNotNull(filePath, "文件路径不应为null");
        File file = new File(filePath);
        assertTrue(file.exists(), "PPT文件应该存在");
        assertTrue(file.getName().endsWith(".pptx"), "文件应该是PPTX格式");
        assertTrue(file.length() > 0, "文件大小应该大于0");
        
        System.out.println("生成的PPT文件: " + filePath);
        System.out.println("文件大小: " + file.length() + " bytes");
    }
    
    @Test
    @DisplayName("测试创建带完整大纲的PPT")
    void testCreatePresentationWithOutline() {
        // 准备测试数据
        List<PptToolbox.Slide> slides = new ArrayList<>();
        
        // 添加封面页
        slides.add(new PptToolbox.Slide("封面页", List.of(
            "Spring AI 技术分享",
            "2025年技术演讲",
            "演讲者：技术团队"
        )));
        
        // 添加目录页
        slides.add(new PptToolbox.Slide("目录", List.of(
            "1. 技术背景",
            "2. 核心概念",
            "3. 实施方案",
            "4. 案例分析",
            "5. 总结展望"
        )));
        
        // 添加内容页
        slides.add(new PptToolbox.Slide("技术背景", List.of(
            "AI技术的发展历程",
            "当前市场需求分析",
            "技术挑战与机遇"
        )));
        
        slides.add(new PptToolbox.Slide("核心概念", List.of(
            "Spring AI框架介绍",
            "Function Calling机制",
            "向量数据库集成",
            "模型选择与优化"
        )));
        
        slides.add(new PptToolbox.Slide("实施方案", List.of(
            "系统架构设计",
            "技术选型分析",
            "开发流程规划",
            "部署方案设计"
        )));
        
        slides.add(new PptToolbox.Slide("案例分析", List.of(
            "成功案例展示",
            "性能指标分析",
            "用户反馈总结"
        )));
        
        slides.add(new PptToolbox.Slide("总结展望", List.of(
            "项目成果总结",
            "未来发展方向",
            "技术演进规划"
        )));
        
        // 添加感谢页
        slides.add(new PptToolbox.Slide("谢谢", List.of(
            "感谢聆听",
            "欢迎提问",
            "联系方式：tech@example.com"
        )));
        
        PptToolbox.PptOutline outline = new PptToolbox.PptOutline(
            "Spring AI 技术分享 - 完整版",
            slides
        );
        
        // 执行
        String filePath = engine.createPresentation("Spring AI 技术分享", outline);
        
        // 验证
        assertNotNull(filePath, "文件路径不应为null");
        File file = new File(filePath);
        assertTrue(file.exists(), "PPT文件应该存在");
        assertTrue(file.getName().endsWith(".pptx"), "文件应该是PPTX格式");
        assertTrue(file.length() > 10000, "带大纲的PPT文件应该较大");
        
        System.out.println("生成的PPT文件: " + filePath);
        System.out.println("文件大小: " + (file.length() / 1024) + " KB");
        System.out.println("幻灯片数量: " + slides.size());
    }
    
    @Test
    @DisplayName("测试创建空大纲PPT（使用默认示例）")
    void testCreatePresentationWithNullOutline() {
        // 执行 - 传入null大纲，应该创建默认示例幻灯片
        String filePath = engine.createPresentation("示例演示文稿", null);
        
        // 验证
        assertNotNull(filePath, "文件路径不应为null");
        File file = new File(filePath);
        assertTrue(file.exists(), "PPT文件应该存在");
        assertTrue(file.length() > 0, "文件大小应该大于0");
        
        System.out.println("生成的示例PPT文件: " + filePath);
        System.out.println("文件大小: " + (file.length() / 1024) + " KB");
    }
    
    @Test
    @DisplayName("测试特殊字符标题处理")
    void testCreatePresentationWithSpecialCharacters() {
        // 准备包含特殊字符的标题
        String specialTitle = "测试/PPT\\标题*with?特殊<字符>";
        
        // 执行
        String filePath = engine.createPresentation(specialTitle);
        
        // 验证
        assertNotNull(filePath, "文件路径不应为null");
        File file = new File(filePath);
        assertTrue(file.exists(), "PPT文件应该存在");
        // 验证文件名中的特殊字符被处理
        assertFalse(file.getName().contains("/"), "文件名不应包含/");
        assertFalse(file.getName().contains("\\"), "文件名不应包含\\");
        assertFalse(file.getName().contains("*"), "文件名不应包含*");
        assertFalse(file.getName().contains("?"), "文件名不应包含?");
        assertFalse(file.getName().contains("<"), "文件名不应包含<");
        assertFalse(file.getName().contains(">"), "文件名不应包含>");
        
        System.out.println("特殊字符测试 - 原标题: " + specialTitle);
        System.out.println("生成的文件名: " + file.getName());
    }
    
    @Test
    @DisplayName("测试中文标题和内容")
    void testCreatePresentationWithChineseContent() {
        // 准备中文内容
        List<PptToolbox.Slide> slides = List.of(
            new PptToolbox.Slide("项目介绍", List.of(
                "项目背景与愿景",
                "核心技术栈",
                "团队介绍"
            )),
            new PptToolbox.Slide("技术架构", List.of(
                "微服务架构设计",
                "数据库设计方案",
                "缓存策略优化",
                "消息队列应用"
            )),
            new PptToolbox.Slide("项目成果", List.of(
                "功能实现情况",
                "性能测试结果",
                "用户增长数据"
            ))
        );
        
        PptToolbox.PptOutline outline = new PptToolbox.PptOutline(
            "中文技术分享演示文稿",
            slides
        );
        
        // 执行
        String filePath = engine.createPresentation("中文PPT测试", outline);
        
        // 验证
        assertNotNull(filePath, "文件路径不应为null");
        File file = new File(filePath);
        assertTrue(file.exists(), "PPT文件应该存在");
        assertTrue(file.length() > 0, "文件大小应该大于0");
        
        System.out.println("中文PPT测试 - 文件: " + filePath);
        System.out.println("文件大小: " + (file.length() / 1024) + " KB");
    }
    
    @Test
    @DisplayName("测试长内容处理")
    void testCreatePresentationWithLongContent() {
        // 准备长内容
        List<String> longBulletPoints = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            longBulletPoints.add("这是第" + i + "个要点，包含较长的文字内容用于测试PPT生成引擎对长文本的处理能力");
        }
        
        List<PptToolbox.Slide> slides = List.of(
            new PptToolbox.Slide("长内容测试页", longBulletPoints)
        );
        
        PptToolbox.PptOutline outline = new PptToolbox.PptOutline(
            "长内容处理测试",
            slides
        );
        
        // 执行
        String filePath = engine.createPresentation("长内容测试", outline);
        
        // 验证
        assertNotNull(filePath, "文件路径不应为null");
        File file = new File(filePath);
        assertTrue(file.exists(), "PPT文件应该存在");
        
        System.out.println("长内容测试 - 文件: " + filePath);
        System.out.println("要点数量: " + longBulletPoints.size());
        System.out.println("文件大小: " + (file.length() / 1024) + " KB");
    }
}