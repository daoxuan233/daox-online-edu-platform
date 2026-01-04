package com.daox.ai.listener;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.daox.ai.entity.dto.common.QuestionEventDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class VectorSyncConsumer {

    @Resource
    private RedisVectorStore redisVectorStore;

    @RabbitListener(queues = "q.ai.vector.sync")
    public void handleSync(QuestionEventDTO event) {
        System.out.println("AI服务收到同步: " + event.getId());

        // 1. 处理删除
        if ("DELETE".equals(event.getOperation())) {
            redisVectorStore.delete(List.of(event.getId()));
            return;
        }

        // 2. 构建向量化的文本内容 (Content)
        // 策略：题干 + 换行 + 选项内容。这样 AI 搜"Java继承"时，也能匹配到选项里提到的概念。
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(event.getStem());

        if (event.getOptions() != null && !event.getOptions().isEmpty()) {
            contentBuilder.append("\n选项:\n");
            for (QuestionEventDTO.OptionDTO opt : event.getOptions()) {
                contentBuilder.append(opt.getKey()).append(". ").append(opt.getText()).append("\n");
            }
        }
        String vectorContent = contentBuilder.toString();

        // 3. 构建元数据 (Metadata)
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("courseId", event.getCourseId());
        metadata.put("creatorId", event.getCreatorId());
        metadata.put("type", event.getType());
        metadata.put("difficulty", event.getDifficulty());

        // Tags 处理: Redis Vector Store 对 List 支持有限，建议转为逗号分隔字符串
        if (event.getTags() != null) {
            metadata.put("tags", String.join(",", event.getTags()));
        }

        // 4. 执行 Upsert (Spring AI 会自动根据 ID 覆盖旧数据)
        // 注意：Document ID 必须和 MongoDB ID 严格一致
        Document doc = new Document(event.getId(), vectorContent, metadata);

        redisVectorStore.add(List.of(doc));
        log.info("✅ 已更新向量索引: {}", event.getId());
    }
}
