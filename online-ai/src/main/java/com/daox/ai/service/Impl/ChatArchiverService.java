package com.daox.ai.service.Impl;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

/**
 * 聊天记录归档服务
 * 负责将用户问题和 AI 回答归档到 Redis Stack (6381) 中的 chat_memory_idx 索引
 */
@Service
public class ChatArchiverService {

    private final VectorStore historyStore;

    public ChatArchiverService(@Qualifier("historyVectorStore") VectorStore historyStore) {
        this.historyStore = historyStore;
    }

    /**
     * 归档方法
     * 修改点 1: 返回值从 void 改为 Mono<Void>
     * 修改点 2: 去掉 @Async (我们要手动控制线程调度)
     */
    public Mono<Void> archive(String userId, String conversationId, String question, String answer) {

        // 使用 fromRunnable 包装阻塞逻辑
        return Mono.fromRunnable(() -> {
                    long now = System.currentTimeMillis();

                    Document userDoc = new Document(question, Map.of(
                            "userId", userId,
                            "conversationId", conversationId,
                            "role", "user",
                            "timestamp", now
                    ));

                    Document aiDoc = new Document(answer, Map.of(
                            "userId", userId,
                            "conversationId", conversationId,
                            "role", "assistant",
                            "timestamp", now
                    ));

                    // 这是一个阻塞操作 (Blocking IO)
                    historyStore.add(List.of(userDoc, aiDoc));
                })
                // 关键：将上述阻塞代码调度到 Reactor 的弹性线程池执行
                // 这起到了和 @Async 一样的“不阻塞主线程”的效果，但保留了流的特性
                .subscribeOn(Schedulers.boundedElastic())
                .then(); // 转为 Mono<Void>
    }
}
