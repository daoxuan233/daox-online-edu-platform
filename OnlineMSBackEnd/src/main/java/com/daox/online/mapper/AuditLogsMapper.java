package com.daox.online.mapper;

import com.daox.online.entity.mysql.AuditLogs;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 审计日志数据访问接口。
 */
@Mapper
public interface AuditLogsMapper {

    /**
     * 写入单条审计日志。
     *
     * @param auditLog 审计日志实体
     * @return 影响行数
     */
    @Insert("""
            INSERT INTO audit_logs (
                id,
                action,
                resource_type,
                resource_id,
                status,
                message,
                operator_id,
                operator_identifier,
                operator_name,
                operator_role,
                request_ip,
                request_method,
                request_path,
                request_source,
                before_snapshot,
                after_snapshot,
                created_at
            ) VALUES (
                #{id},
                #{action},
                #{resourceType},
                #{resourceId},
                #{status},
                #{message},
                #{operatorId},
                #{operatorIdentifier},
                #{operatorName},
                #{operatorRole},
                #{requestIp},
                #{requestMethod},
                #{requestPath},
                #{requestSource},
                #{beforeSnapshot},
                #{afterSnapshot},
                #{createdAt}
            )
            """)
    int insert(AuditLogs auditLog);

    /**
     * 按条件查询审计日志列表。
     *
     * @param keyword      关键字
     * @param action       操作动作
     * @param status       执行状态
     * @param operatorRole 操作者角色
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 审计日志列表
     */
    @Select("""
            <script>
            SELECT
                id,
                action,
                resource_type,
                resource_id,
                status,
                message,
                operator_id,
                operator_identifier,
                operator_name,
                operator_role,
                request_ip,
                request_method,
                request_path,
                request_source,
                before_snapshot,
                after_snapshot,
                created_at
            FROM audit_logs
            <where>
                <if test="keyword != null and keyword != ''">
                    AND (
                        resource_id LIKE CONCAT('%', #{keyword}, '%')
                        OR message LIKE CONCAT('%', #{keyword}, '%')
                        OR operator_id LIKE CONCAT('%', #{keyword}, '%')
                        OR operator_identifier LIKE CONCAT('%', #{keyword}, '%')
                        OR operator_name LIKE CONCAT('%', #{keyword}, '%')
                        OR request_ip LIKE CONCAT('%', #{keyword}, '%')
                        OR request_path LIKE CONCAT('%', #{keyword}, '%')
                    )
                </if>
                <if test="action != null and action != ''">
                    AND action = #{action}
                </if>
                <if test="status != null and status != ''">
                    AND status = #{status}
                </if>
                <if test="operatorRole != null and operatorRole != ''">
                    AND operator_role = #{operatorRole}
                </if>
                <if test="startDate != null and startDate != ''">
                    AND created_at <![CDATA[>=]]> CONCAT(#{startDate}, ' 00:00:00')
                </if>
                <if test="endDate != null and endDate != ''">
                    AND created_at <![CDATA[<=]]> CONCAT(#{endDate}, ' 23:59:59')
                </if>
            </where>
            ORDER BY created_at DESC
            LIMIT 500
            </script>
            """)
    @Results(id = "auditLogResultMap", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "action", property = "action"),
            @Result(column = "resource_type", property = "resourceType"),
            @Result(column = "resource_id", property = "resourceId"),
            @Result(column = "status", property = "status"),
            @Result(column = "message", property = "message"),
            @Result(column = "operator_id", property = "operatorId"),
            @Result(column = "operator_identifier", property = "operatorIdentifier"),
            @Result(column = "operator_name", property = "operatorName"),
            @Result(column = "operator_role", property = "operatorRole"),
            @Result(column = "request_ip", property = "requestIp"),
            @Result(column = "request_method", property = "requestMethod"),
            @Result(column = "request_path", property = "requestPath"),
            @Result(column = "request_source", property = "requestSource"),
            @Result(column = "before_snapshot", property = "beforeSnapshot"),
            @Result(column = "after_snapshot", property = "afterSnapshot"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<AuditLogs> selectAuditLogs(@Param("keyword") String keyword,
                                    @Param("action") String action,
                                    @Param("status") String status,
                                    @Param("operatorRole") String operatorRole,
                                    @Param("startDate") String startDate,
                                    @Param("endDate") String endDate);
}
