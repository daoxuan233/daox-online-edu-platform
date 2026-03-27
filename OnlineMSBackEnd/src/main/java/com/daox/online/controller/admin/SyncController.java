package com.daox.online.controller.admin;

import com.daox.online.entity.mongodb.Question;
import com.daox.online.listener.QuestionProduceService;
import com.daox.online.repository.mongodb.QuestionRepository;
import com.daox.online.service.AuditLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 全量同步控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/sync")
public class SyncController {

    @Resource
    private QuestionRepository questionRepository;

    @Resource
    private QuestionProduceService questionProduceService;

    @Resource
    private AuditLogService auditLogService;

    /**
     * 触发全量数据同步到 AI 向量库
     * 建议加权限控制，防止误触
     */
    @PostMapping("/start-all")
    public String startFullSync() {
        String taskId = UUID.randomUUID().toString();
        long totalQuestions = questionRepository.count();
        Map<String, Object> beforeSnapshot = new LinkedHashMap<>();
        beforeSnapshot.put("questionCount", totalQuestions);
        beforeSnapshot.put("syncScope", "ALL_QUESTIONS");
        Map<String, Object> afterSnapshot = new LinkedHashMap<>();
        afterSnapshot.put("taskId", taskId);
        afterSnapshot.put("dispatchMode", "ASYNC");
        afterSnapshot.put("status", "STARTED");
        auditLogService.recordSensitiveOperation(
                "QUESTION_VECTOR_SYNC_TRIGGER",
                "question_vector_sync",
                taskId,
                beforeSnapshot,
                afterSnapshot,
                "STARTED",
                "管理员触发题目全量同步");
        CompletableFuture.runAsync(this::doFullSync);
        return "后台同步任务已启动，请查看日志关注进度...";
    }

    private void doFullSync() {
        log.info("=== 开始全量同步题目到 RabbitMQ ===");

        int page = 0;
        int size = 500; // 每批处理 500 条
        long totalProcessed = 0;

        while (true) {
            // 1. 分页查询 MongoDB
            Page<Question> questionPage = questionRepository.findAll(PageRequest.of(page, size));

            if (questionPage.isEmpty()) {
                break; // 读完了
            }

            // 2. 遍历并发送
            for (Question q : questionPage.getContent()) {
                try {
                    // 【关键复用】直接调用之前写好的 save 逻辑中的发送部分
                    // 或者把发送逻辑抽取成 public 方法 sendToMq(Question q, String op)
                    // 这里假设你在 QuestionService 暴露了 sendToMq 方法
                    questionProduceService.sendToMq(q, "SAVE");

                    totalProcessed++;
                } catch (Exception e) {
                    log.error("题目同步失败 ID: {}", q.getId(), e);
                }
            }

            log.info("已处理批次: {}, 当前总数: {}", page, totalProcessed);
            page++;

            // 【可选】防止把 RabbitMQ 或 AI 服务打挂，稍微休息一下
            // try { Thread.sleep(100); } catch (InterruptedException e) {}
        }

        log.info("=== 全量同步完成，共推送 {} 条数据 ===", totalProcessed);
    }
}
