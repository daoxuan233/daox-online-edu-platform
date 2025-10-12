package com.daox.online.entity.dto.teacher.marking;

/**
 * 分级进度 传递进度数据的辅助记录类
 *
 * @param gradedCount 已批阅的
 * @param totalCount  总数
 */
public record GradingProgress(Long gradedCount, Long totalCount) {
}
