package com.daox.pptAgent.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring AI 配置类
 * 负责配置 Spring AI 1.0.0 相关的 Bean
 */
@Slf4j
@Configuration
public class SpringAIConfig {

    @Value("${spring.ai.dashscope.api-key:}")
    private String apiKey;

    /**
     * 配置默认的 ChatClient.Builder
     * 这是 Spring AI 1.0.0 推荐的配置方式
     */
    @Bean
    @Primary
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        log.info("初始化 Spring AI ChatClient.Builder");
        
        // 创建自定义的日志记录器
        SimpleLoggerAdvisor loggerAdvisor = new SimpleLoggerAdvisor();
        
        return ChatClient.builder(chatModel)
                // 添加日志记录顾问
                .defaultAdvisors(loggerAdvisor)
                // 设置默认系统提示
                .defaultSystem("你是一个专业的AI助手，擅长创建结构化的内容。");
    }
    
    /**
     * 创建一个专门用于PPT生成的 ChatClient
     */
    @Bean(name = "pptChatClient")
    public ChatClient pptChatClient(ChatClient.Builder builder) {
        log.info("创建 PPT 专用 ChatClient");
        
        return builder
                .defaultSystem("""
                    你是一个专业的PPT制作助手。你的主要任务是：
                    1. 根据用户需求生成结构化的PPT大纲
                    2. 协助用户完善和优化PPT内容
                    3. 提供专业的演示建议
                    """)
                .build();
    }
    
    /**
     * 配置信息日志
     */
    @PostConstruct
    public void logConfiguration() {
        log.info("=====================================");
        log.info("Spring AI 1.0.0 配置已加载");
        log.info("API Key 配置状态: {}", apiKey != null && !apiKey.isEmpty() ? "已配置" : "未配置");
        log.info("=====================================");
    }
}