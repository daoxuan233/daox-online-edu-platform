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
}

