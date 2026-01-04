package com.daox.ai.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIRabbitConfig {
    // 1. 定义队列 (对应 @RabbitListener 中的 queues 参数)
    // 加上 @Bean，Spring 启动时会自动去 RabbitMQ 创建它
    @Bean
    public Queue vectorSyncQueue() {
        // 参数1: 队列名 "q.ai.vector.sync" (必须与 Consumer 中的一致)
        // 参数2: true 表示持久化 (RabbitMQ重启后还在)
        return new Queue("q.ai.vector.sync", true);
    }

    // 2. 定义交换机 (虽然 B 也定义了，但 A 这里定义是为了防呆，确保交换机存在)
    @Bean
    public TopicExchange questionExchange() {
        return new TopicExchange("question.topic");
    }

    // 3. 【关键】绑定：把“队列”系到“交换机”上
    // 这样，发送到 question.topic 的消息，只要 RoutingKey 匹配 question.#，就会流进这个队列
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(vectorSyncQueue())
                .to(questionExchange())
                .with("question.#"); // # 是通配符，匹配 question.save, question.delete 等
    }

    // 4. JSON 转换器 (必须有，否则无法解析 DTO)
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
