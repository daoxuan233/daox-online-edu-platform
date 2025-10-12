package com.daox.online.listener;

import com.daox.online.entity.redis.AssessmentSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 测评消息生产者
 */
@Service
@RequiredArgsConstructor // 自动注入所有 final 字段的构造函数
public class AssessmentMessageProducer {
    private static final Logger logger = LoggerFactory.getLogger(AssessmentMessageProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${amqp.exchange.assessment}")
    private String exchangeName;

    @Value("${amqp.routing-key.submission}")
    private String routingKey;

    /**
     * 发送测评提交消息
     * @param session 学生的测评会话数据
     */
    public void sendSubmissionMessage(AssessmentSession session) {
        try {
            logger.info("准备发送测评提交消息到 RabbitMQ, assessmentId: {}, userId: {}",
                    session.getAssessmentId(), session.getUserId());

            rabbitTemplate.convertAndSend(exchangeName, routingKey, session);

            logger.info("测评提交消息发送成功, assessmentId: {}, userId: {}",
                    session.getAssessmentId(), session.getUserId());
        } catch (Exception e) {
            logger.error("发送测评提交消息到 RabbitMQ 失败, assessmentId: {}, userId: {}. Error: {}",
                    session.getAssessmentId(), session.getUserId(), e.getMessage());
            // 抛出运行时异常，以便上层业务进行事务回滚或状态恢复
            throw new RuntimeException("消息发送失败，请稍后重试", e);
        }
    }
}
