package com.daox.ai.service.Impl;

import com.daox.ai.utils.Const;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ChatMemory shortTermMemory;       // 连接 Redis 6380 (List)
    private final VectorStore historyStore;         // 连接 Redis Stack 6381 (Vector Index: chat_memory_idx)

    // 注入两个归档服务
    private final ChatArchiverService redisVectorArchiver; // 负责存入 Redis Vector (长时记忆)
    private final ReactiveMongoChatArchiver mongoArchiver; // 负责存入 MongoDB (冷备)

    public ChatService(ChatClient.Builder builder,
                       ChatMemory shortTermMemory,
                       @Qualifier("historyVectorStore") VectorStore historyStore,
                       ChatArchiverService redisVectorArchiver,
                       ReactiveMongoChatArchiver mongoArchiver) {
        this.shortTermMemory = shortTermMemory;
        this.historyStore = historyStore;
        this.redisVectorArchiver = redisVectorArchiver;
        this.mongoArchiver = mongoArchiver;
        this.chatClient = builder.build();
    }

    public Flux<String> streamChat(String userId, String question, String conversationId) {

        // 0. redis-stack 不支持 `-`
        String safeUserId = (userId != null) ? userId.replace("-", "") : "";

        // 1. ID 预处理
        String effectiveConvId;
        if (conversationId == null || conversationId.isBlank()) {
            // 新会话：生成无连字符的 UUID
            String safeUuid = UUID.randomUUID().toString().replace("-", "");
            effectiveConvId = Const.AI_ASSISTANT_CONVERSATION + safeUserId + "_" + safeUuid;
        } else {
            // 旧会话：清洗传入的 ID，确保不含连字符
            effectiveConvId = conversationId.replace("-", "");
        }

        log.info("开始对话: User={}, ConvId={}", userId, effectiveConvId);

        // 2. [长时记忆] 检索增强 Advisor (RAG)
        var historyRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(historyStore)
                .topK(2)
                .similarityThreshold(0.7)
                .filterExpression(new FilterExpressionBuilder()
                        .eq("conversationId", effectiveConvId) // 严防串台：只搜当前会话
                        .build())
                .build();

        var longTermMemoryAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(historyRetriever)
                .build();

        // 3. [短时记忆] 上下文 Advisor
        var shortTermMemoryAdvisor = MessageChatMemoryAdvisor.builder(shortTermMemory)
                .build();

        // 用于收集流式响应的完整文本
        StringBuilder fullResponseBuilder = new StringBuilder();

        // 4. 定义对话流
        Flux<String> chatStream = chatClient.prompt()
                .user(question)
                // --- 修复点：使用本地定义的常量 ---
                .advisors(a -> a
                        .param(Const.CHAT_MEMORY_CONVERSATION_ID_KEY, effectiveConvId)
                        .param(Const.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                )
                .advisors(shortTermMemoryAdvisor, longTermMemoryAdvisor)
                .stream()
                .content()
                .doOnNext(fullResponseBuilder::append);

        // 5. 定义【双重归档】任务 (The Dual Archive Task)
        // 使用 Mono.defer 确保只有在流结束(fullResponseBuilder有值)时才创建该任务
        Mono<Void> archiveTask = Mono.defer(() -> {
            String fullResponse = fullResponseBuilder.toString();

            // 任务 A: 存入 MongoDB (冷备日志)
            Mono<Void> mongoTask = mongoArchiver.archive(userId, question, fullResponse, effectiveConvId)
                    .onErrorResume(e -> {
                        log.error("[Archive] MongoDB 归档失败: conversationId={}", effectiveConvId, e);
                        return Mono.empty(); // 吞掉异常，确保不影响另一个任务
                    });

            // 任务 B: 存入 Redis Vector (更新长时记忆)
            // 注意：这里调用的是修改后返回 Mono<Void> 的 ChatArchiverService
            Mono<Void> vectorTask = redisVectorArchiver.archive(userId, effectiveConvId, question, fullResponse)
                    .onErrorResume(e -> {
                        log.error("[Archive] Redis Vector 归档失败: conversationId={}", effectiveConvId, e);
                        return Mono.empty();
                    });

            // 并行执行两个归档任务 (Wait for both to complete)
            return Mono.when(mongoTask, vectorTask);
        });

        // 6. 组合流 (Weave Together)
        // 先吐出聊天字符，流结束后再执行归档，最后发送完成信号
        // 解释：archiveTask 是 Void，我们需要通过 .then() 转为一个空的 String 流。
        // 关键修复：使用 Mono.<String>empty() 显式告诉编译器这是 String 类型的 Empty
        return chatStream.concatWith(archiveTask.then(Mono.empty()));
    }
}
