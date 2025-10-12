package com.daox.online.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.amqp.core.QueueBuilder.durable;

/**
 * RabbitMQ 配置类。
 * 配置 RabbitMQ 的队列、交换机、绑定、消息转换器等。
 */
@Configuration
public class RabbitMQConfiguration {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean("mailQueue")
    public Queue queue() {
        return durable("mail").build();
    }

    // --- 测评提交相关的配置 ---

    /**
     * 定义测评交换机 (Topic 类型，灵活强大)
     */
    @Bean
    public TopicExchange assessmentExchange() {
        return new TopicExchange("assessment.exchange", true, false);
    }

    /**
     * 定义测评提交队列
     */
    @Bean
    public Queue assessmentSubmissionQueue() {
        return QueueBuilder
                .durable("assessment.submission.queue")
                // [核心] 声明该队列的死信交换机
                .withArgument("x-dead-letter-exchange", "assessment.exchange.dlx")
                // [核心] 声明死信消息的路由键
                .withArgument("x-dead-letter-routing-key", "assessment.submission.dead")
                .build();
    }

    /**
     * 将队列绑定到交换机，并指定路由键
     */
    @Bean
    public Binding submissionBinding(TopicExchange assessmentExchange, Queue assessmentSubmissionQueue) {
        return BindingBuilder
                .bind(assessmentSubmissionQueue)
                .to(assessmentExchange)
                .with("assessment.submission.new");
    }

    // --- 死信队列 (DLQ) 相关配置 ---

    /**
     * 定义死信交换机 (DLX)
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("assessment.exchange.dlx");
    }

    /**
     * 定义死信队列 (DLQ)
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("assessment.submission.queue.dlq").build();
    }

    /**
     * 将死信队列绑定到死信交换机
     */
    @Bean
    public Binding deadLetterBinding(DirectExchange deadLetterExchange, Queue deadLetterQueue) {
        return BindingBuilder
                .bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with("assessment.submission.dead");
    }
}
