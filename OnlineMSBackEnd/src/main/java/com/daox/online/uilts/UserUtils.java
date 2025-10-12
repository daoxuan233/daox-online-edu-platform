package com.daox.online.uilts;

import com.daox.online.uilts.constant.Const;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 用户工具类，提供获取当前用户ID的方法。
 * <p>
 * 注意：当前架构中，真正的用户ID存储在请求属性中，SecurityContext中存储的是用户名。
 */
@Slf4j
public class UserUtils {

    // TODO: 未来考虑改进JWT认证流程，在UserDetails中包含完整的用户信息。

    /**
     * 获取当前用户ID，优先从请求属性获取，失败时从 SecurityContext 获取用户名作为备用。
     * <p>
     * 注意：当前架构中，真正的用户ID存储在请求属性中，SecurityContext中存储的是用户名。
     *
     * @param request 当前 HTTP 请求对象
     * @return 当前用户ID，获取不到时返回 null
     */
    public static String getCurrentUserId(HttpServletRequest request) {
        // 优先从请求属性获取真正的用户ID
        if (request != null) {
            Object userId = request.getAttribute(Const.ATTR_USER_ID);
            if (userId != null) {
                log.debug("从请求属性获取到用户ID: {}", userId);
                return userId.toString();
            }
        }

        // 请求属性获取失败，尝试从 SecurityContext 获取用户名作为备用
        // 注意：这里获取的是用户名，不是用户ID，仅作为降级方案
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                log.warn("从SecurityContext获取到用户名作为备用ID: {}", username);
                return username;
            } else if (principal instanceof String) {
                log.warn("从SecurityContext获取到字符串作为备用ID: {}", principal);
                return (String) principal;
            }
        }

        log.error("无法获取当前用户ID，请求属性和SecurityContext均无有效信息");
        return null;
    }
}

