package com.daox.online.entity.dto.teacher.marking;

/**
 * “完成批阅”操作的结果 DTO
 *
 * @param finalizedCount 成功归档的答卷数量
 * @param pendingCount   仍然未完成批改的答卷数量
 * @param message        总结信息
 */
public record FinalizeGradingResultDTO(
        long finalizedCount, // 成功归档的答卷数量
        long pendingCount,   // 仍然未完成批改的答卷数量
        String message       // 总结信息
) {
}