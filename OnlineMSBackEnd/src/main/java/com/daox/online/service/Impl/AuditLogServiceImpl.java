package com.daox.online.service.Impl;

import com.daox.online.entity.mysql.AuditLogs;
import com.daox.online.entity.mysql.Users;
import com.daox.online.mapper.AuditLogsMapper;
import com.daox.online.service.AuditLogService;
import com.daox.online.service.SysUserService;
import com.daox.online.uilts.HybridIdGenerator;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 审计日志服务实现类。
 */
@Service
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private static final int AUDIT_ACTION_MAX_LENGTH = 32;

    @Resource
    private AuditLogsMapper auditLogsMapper;

    @Resource
    private HybridIdGenerator hybridIdGenerator;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private SysUserService sysUserService;

    /**
     * 将敏感操作写入审计表。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSensitiveOperation(String action,
                                         String resourceType,
                                         String resourceId,
                                         Object beforeSnapshot,
                                         Object afterSnapshot,
                                         String status,
                                         String message) {
        try {
            HttpServletRequest request = getCurrentRequest();
            String operatorId = request == null ? null : UserUtils.getCurrentUserId(request);
            Users operator = operatorId == null ? null : sysUserService.findUserById(operatorId);
            String normalizedAction = normalizeAction(action);
            String normalizedStatus = normalizeStatus(status);
            AuditLogs auditLog = new AuditLogs()
                    .setId(hybridIdGenerator.generateId())
                    .setAction(normalizedAction)
                    .setResourceType(resourceType)
                    .setResourceId(resourceId)
                    .setStatus(normalizedStatus)
                    .setMessage(message)
                    .setOperatorId(operatorId)
                    .setOperatorIdentifier(operator == null ? null : operator.getIdentifier())
                    .setOperatorName(operator == null ? null : operator.getNickname())
                    .setOperatorRole(operator == null ? null : operator.getRole())
                    .setRequestIp(extractClientIp(request))
                    .setRequestMethod(request == null ? null : request.getMethod())
                    .setRequestPath(request == null ? null : request.getRequestURI())
                    .setRequestSource(buildRequestSource(request))
                    .setBeforeSnapshot(toJson(beforeSnapshot))
                    .setAfterSnapshot(toJson(afterSnapshot))
                    .setCreatedAt(new Date());
            auditLogsMapper.insert(auditLog);
        } catch (Exception exception) {
            log.error("[AuditLogServiceImpl.recordSensitiveOperation] 审计日志写入失败: action={}, resourceType={}, resourceId={}",
                    action, resourceType, resourceId, exception);
        }
    }

    /**
     * 提供管理员审计日志查询能力。
     */
    @Override
    public List<AuditLogs> listAuditLogs(String keyword,
                                         String action,
                                         String status,
                                         String operatorRole,
                                         String startDate,
                                         String endDate) {
        try {
            return auditLogsMapper.selectAuditLogs(keyword, action, status, operatorRole, startDate, endDate);
        } catch (Exception exception) {
            log.error("[AuditLogServiceImpl.listAuditLogs] 审计日志查询失败: action={}, status={}, operatorRole={}",
                    action, status, operatorRole, exception);
            return Collections.emptyList();
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private String buildRequestSource(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("ip", extractClientIp(request));
        source.put("userAgent", request.getHeader("User-Agent"));
        source.put("referer", request.getHeader("Referer"));
        source.put("forwardedFor", request.getHeader("X-Forwarded-For"));
        source.put("host", request.getHeader("Host"));
        return toJson(source);
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String toJson(Object source) {
        if (source == null) {
            return null;
        }
        try {
            Object normalized = objectMapper.convertValue(source, Object.class);
            Object sanitized = sanitizeValue(normalized);
            return objectMapper.writeValueAsString(sanitized);
        } catch (Exception exception) {
            log.warn("[AuditLogServiceImpl.toJson] 快照序列化失败: {}", exception.getMessage());
            return String.valueOf(source);
        }
    }

    private Object sanitizeValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> sanitized = new LinkedHashMap<>();
            map.forEach((key, entryValue) -> {
                String keyText = String.valueOf(key);
                if (isSensitiveKey(keyText)) {
                    sanitized.put(keyText, "***");
                } else {
                    sanitized.put(keyText, sanitizeValue(entryValue));
                }
            });
            return sanitized;
        }
        if (value instanceof Collection<?> collection) {
            List<Object> sanitized = new ArrayList<>(collection.size());
            collection.forEach(item -> sanitized.add(sanitizeValue(item)));
            return sanitized;
        }
        return value;
    }

    /**
     * 规范化审计动作编码，避免超出数据库字段长度。
     *
     * @param action 原始动作编码
     * @return 规范化后的动作编码
     */
    private String normalizeAction(String action) {
        if (action == null) {
            return null;
        }
        String normalized = action.trim();
        if (normalized.length() <= AUDIT_ACTION_MAX_LENGTH) {
            return normalized;
        }
        String shortened = normalized.substring(0, AUDIT_ACTION_MAX_LENGTH);
        log.warn("[AuditLogServiceImpl.normalizeAction] 审计动作过长，已截断: original={}, shortened={}", normalized, shortened);
        return shortened;
    }

    /**
     * 规范化执行状态，统一为大写格式。
     *
     * @param status 原始状态
     * @return 大写状态
     */
    private String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isSensitiveKey(String key) {
        String lowerKey = key.toLowerCase(Locale.ROOT);
        return lowerKey.contains("password")
                || lowerKey.contains("token")
                || lowerKey.contains("secret")
                || lowerKey.contains("credential")
                || lowerKey.contains("authorization");
    }
}
