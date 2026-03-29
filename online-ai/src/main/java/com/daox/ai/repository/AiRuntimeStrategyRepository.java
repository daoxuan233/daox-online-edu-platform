package com.daox.ai.repository;

import com.daox.ai.entity.mongodb.AiRuntimeStrategy;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * AI 运行时策略仓库。
 */
@Repository
public interface AiRuntimeStrategyRepository extends ReactiveMongoRepository<AiRuntimeStrategy, String> {
}