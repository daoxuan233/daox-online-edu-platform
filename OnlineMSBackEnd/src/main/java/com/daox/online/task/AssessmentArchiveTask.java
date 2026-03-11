package com.daox.online.task;

import com.daox.online.mapper.AssessmentsMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 定时任务 - 测评归档任务。
 * <p>
 * 任务职责：
 * 1) 定时扫描已发布测评；
 * 2) 将截止时间已到的测评自动归档（is_published = 2）；
 * 3) 作为“按截止时间自动关闭测评”的统一兜底机制。
 * </p>
 */
@Slf4j
@Component
public class AssessmentArchiveTask {

    @Resource
    private AssessmentsMapper assessmentsMapper;

    /**
     * 定时归档到期测评。
     * <p>
     * 调度策略：
     * 1) 每10分钟执行一次；
     * 2) 采用批量 SQL 更新，避免逐条扫描带来的性能损耗；
     * 3) 仅记录任务开始、结束和更新条数，便于生产排障与审计。
     * </p>
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void archiveExpiredAssessments() {
        log.info("开始执行测评自动归档任务");
        LocalDateTime now = LocalDateTime.now();
        int archivedCount = assessmentsMapper.archiveExpiredAssessments(now);
        log.info("测评自动归档任务执行完成，本次归档 {} 条记录", archivedCount);
    }
}
