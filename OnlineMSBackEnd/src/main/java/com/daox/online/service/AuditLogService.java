package com.daox.online.service;

import com.daox.online.entity.mysql.AuditLogs;

import java.util.List;

/**
 * 审计日志服务接口。
 * 统一封装敏感操作留痕写入与管理员审计查询能力。
 */
public interface AuditLogService {

    /**
     * 记录敏感操作审计日志。
     *
     * @param action         操作动作编码
     * @param resourceType   资源类型
     * @param resourceId     资源唯一标识
     * @param beforeSnapshot 变更前快照
     * @param afterSnapshot  变更后快照
     * @param status         执行状态
     * @param message        补充说明
     */
    void recordSensitiveOperation(String action,
                                  String resourceType,
                                  String resourceId,
                                  Object beforeSnapshot,
                                  Object afterSnapshot,
                                  String status,
                                  String message);

    /**
     * 查询管理员审计日志列表。
     *
     * @param keyword      关键字，匹配操作者、资源ID、请求路径、说明等字段
     * @param action       操作动作编码
     * @param status       执行状态
     * @param operatorRole 操作者角色
     * @param startDate    开始日期，格式 yyyy-MM-dd
     * @param endDate      结束日期，格式 yyyy-MM-dd
     * @return 审计日志列表，按创建时间倒序
     */
    List<AuditLogs> listAuditLogs(String keyword,
                                  String action,
                                  String status,
                                  String operatorRole,
                                  String startDate,
                                  String endDate);
}
