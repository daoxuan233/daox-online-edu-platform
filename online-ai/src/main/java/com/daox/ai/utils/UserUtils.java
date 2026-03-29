package com.daox.ai.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户工具类，提供获取当前用户ID的方法。
 * <p>
 * 注意：当前架构中，真正的用户ID存储在请求属性中
 */
@Slf4j
public class UserUtils {

    /**
     * 获取当前用户ID，优先从请求属性获取，失败时返回 null。
     *
     * @param request 当前 HTTP 请求对象
     * @return 当前用户ID，获取不到时返回 null
     */
    public static String getCurrentUserId(HttpServletRequest request) {
        // 优先从请求属性获取真正的用户ID
        if (request != null) {
            Object userId = request.getAttribute(Const.ATTR_USER_ID);
            if (userId != null) {
                // log.info("从请求属性获取到用户ID: {}", userId);
                return userId.toString();
            }
        }
        log.error("无法获取当前用户ID，请求属性无有效信息");
        return null;
    }

    /**
     * 获取当前用户角色。
     *
     * @param request 当前 HTTP 请求对象
     * @return 当前用户角色，获取不到时返回 {@code null}
     */
    public static String getCurrentUserRole(HttpServletRequest request) {
        if (request != null) {
            Object userRole = request.getAttribute(Const.ATTR_USER_ROLE);
            if (userRole != null) {
                return userRole.toString();
            }
        }
        log.warn("无法获取当前用户角色，请求属性无有效信息");
        return null;
    }

    /**
     * 判断当前请求是否来自管理员。
     *
     * @param request 当前 HTTP 请求对象
     * @return true 表示管理员，false 表示非管理员
     */
    public static boolean isAdmin(HttpServletRequest request) {
        return Const.ROLE_ADMIN.equalsIgnoreCase(getCurrentUserRole(request));
    }
}

