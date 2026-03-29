package com.daox.ai.repository;

import com.daox.ai.entity.mongodb.AiCallRecord;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * AI 调用记录仓库。
 */
@Repository
public interface AiCallRecordRepository extends ReactiveMongoRepository<AiCallRecord, String> {
}