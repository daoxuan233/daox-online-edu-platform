package com.daox.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.JedisPooled;

/**
 * 配置 Redis Vector Store 连接
 */
@Configuration
public class RedisStackConfig {

    // 1. 创建专门连接 6381 的 Jedis 客户端
    // 这与你的缓存 Redis (Lettuce @ 6379) 是完全物理隔离的
    @Bean
    public JedisPooled vectorStoreJedis() {
        // 参数：Host, Port, User(默认为null或"default"), Password
        return new JedisPooled("101.126.19.83", 6381, "default", "My_DaoX-Strong_Password-Here_702.541");
    }

    // ==========================================
    // Index 1: 题库 (只读/检索为主)
    // ==========================================
    @Bean(name = "quizVectorStore")
    @Primary // 默认注入这个，或者你可以去掉 Primary 全部用 Qualifier
    public VectorStore quizVectorStore(JedisPooled vectorStoreJedis, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(vectorStoreJedis, embeddingModel)
                .indexName("question_idx")      // 索引名 A
                .prefix("doc:question:")        // 前缀 A (修改为 doc:question: 更清晰)
                .initializeSchema(true)
                .metadataFields(
                        RedisVectorStore.MetadataField.text("courseId"),
                        RedisVectorStore.MetadataField.text("type"),
                        RedisVectorStore.MetadataField.text("difficulty")
                )
                .build();
    }

    // ==========================================
    // Index 2: 聊天记忆 (读写频繁)
    // ==========================================
    @Bean(name = "historyVectorStore")
    public VectorStore chatHistoryVectorStore(JedisPooled vectorStoreJedis, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(vectorStoreJedis, embeddingModel)
                .indexName("chat_memory_idx")   // 关键：独立的索引名
                .prefix("doc:history:")         // 关键：独立的前缀
                .initializeSchema(true)
                .metadataFields(
                        // 必须字段：用于隔离不同用户的聊天记录
                        RedisVectorStore.MetadataField.tag("conversationId"),
                        RedisVectorStore.MetadataField.tag("userId"),
                        RedisVectorStore.MetadataField.numeric("timestamp")
                )
                .build();
    }
}
