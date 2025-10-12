package com.daox.online.task;

import com.daox.online.mapper.SystemAnnouncementsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;

/**
 * 定时任务 - 系统公告
 */
@Slf4j
@Component
public class SystemAnnouncementTask {

    @Resource
    private SystemAnnouncementsMapper systemAnnouncementsMapper;

    /**
     * 每日1点执行一次 - 更新过期的系统公告
     */
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
    public void updateExpiredAnnouncements() {
        log.info("开始执行系统公告过期检查任务");
        LocalDateTime now = LocalDateTime.now();
        int updatedCount = systemAnnouncementsMapper.updateExpiredAnnouncements(now);
        log.info("系统公告过期检查任务完成，更新了 {} 条公告", updatedCount);
    }
}
