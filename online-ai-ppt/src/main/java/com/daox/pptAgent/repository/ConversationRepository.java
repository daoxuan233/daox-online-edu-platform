package com.daox.pptAgent.repository;

import com.daox.pptAgent.model.ConversationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * MongoDB数据访问层，由Spring Data自动实现。
 */
@Repository
public interface ConversationRepository extends MongoRepository<ConversationDocument, String> {
}