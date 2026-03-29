package com.daox.online.task;

import com.daox.online.service.CoursesService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 分类删除定时任务。
 * 定期扫描 Redis 中待完成的常规删除申请，并在满足条件后自动执行最终删除。
 */
@Slf4j
@Component
public class CategoryDeleteTask {

    @Resource
    private CoursesService coursesService;

    /**
     * 每十分钟巡检一次分类常规删除任务。
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void processPendingCategoryDeletes() {
        log.info("开始巡检待处理的课程分类常规删除任务");
        coursesService.processPendingCategoryDeletionTasks();
        log.info("课程分类常规删除任务巡检完成");
    }
}
