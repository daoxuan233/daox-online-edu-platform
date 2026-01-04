package com.daox.online.listener;

import com.daox.online.entity.dto.common.QuestionEventDTO;
import com.daox.online.entity.mongodb.Question;
import com.daox.online.repository.mongodb.QuestionRepository;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class QuestionProduceService {

    @Resource
    private QuestionRepository questionRepository;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 保存题目并同步给 AI
     */
    public Question save(Question question) {
        // 1. 业务落库 (MongoDB)
        Question saved = questionRepository.save(question);

        // 2. 异步发送消息 (建议捕获异常，不要影响主业务)
        try {
            sendToMq(saved, "SAVE");
        } catch (Exception e) {
            // 生产环境建议记录到一张 "消息发送失败表" 进行补偿
            System.err.println("AI同步失败: " + e.getMessage());
        }

        return saved;
    }

    /**
     * 发送题目事件到 RabbitMQ
     * @param q 题目
     * @param operation 操作类型 (SAVE/UPDATE/DELETE)
     */
    public void sendToMq(Question q, String operation) {
        QuestionEventDTO dto = new QuestionEventDTO();
        // 基础属性拷贝
        dto.setId(q.getId());
        dto.setCourseId(q.getCourseId());
        dto.setCreatorId(q.getCreatorId());
        dto.setStem(q.getStem());
        dto.setTags(q.getTags());
        dto.setOperation(operation);

        // 枚举转 String
        if (q.getType() != null) dto.setType(q.getType().name());
        if (q.getDifficulty() != null) dto.setDifficulty(q.getDifficulty().name());

        // 选项拷贝 (如果有)
        if (q.getOptions() != null) {
            dto.setOptions(q.getOptions().stream().map(o -> {
                QuestionEventDTO.OptionDTO od = new QuestionEventDTO.OptionDTO();
                od.setKey(o.getKey());
                od.setText(o.getText());
                return od;
            }).collect(Collectors.toList()));
        }

        // 发送: routingKey = question.save
        rabbitTemplate.convertAndSend("question.topic", "question." + operation.toLowerCase(), dto);
    }
}
