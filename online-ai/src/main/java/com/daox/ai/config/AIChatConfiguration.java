package com.daox.ai.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AIChatConfiguration {

    String systemPrompt =
            """
            你是一个 DaoX-在线教育 智能助手，你的任务是回答用户的问题。
            """;

    @Bean
    @Primary // 默认模型
    public ChatClient client(OpenAiChatModel model){
        return ChatClient
                .builder(model)
                // 预设AI角色
                .defaultSystem(systemPrompt)
                .build();
    }

    /**
     * alibaba - 模型
     * @param chatModel DashScopeChatModel
     * @return ChatClient
     */
    @Bean
    public ChatClient dashScopeChatClient(DashScopeChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}
