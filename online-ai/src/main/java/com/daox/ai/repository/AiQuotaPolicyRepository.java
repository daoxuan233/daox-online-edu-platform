package com.daox.ai.repository;

import com.daox.ai.entity.mongodb.AiQuotaPolicy;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * AI 配额策略仓库。
 */
@Repository
public interface AiQuotaPolicyRepository extends ReactiveMongoRepository<AiQuotaPolicy, String> {

    /**
     * 根据作用域查询生效策略。
     *
     * @param scopeType  作用域类型
     * @param scopeValue 作用域值
     * @return 匹配到的策略
     */
    Mono<AiQuotaPolicy> findByScopeTypeAndScopeValueAndEnabledTrue(AiQuotaPolicy.ScopeType scopeType, String scopeValue);

    /**
     * 查询全部策略。
     *
     * @return 策略列表
     */
    Flux<AiQuotaPolicy> findAllByOrderByUpdatedAtDesc();
}