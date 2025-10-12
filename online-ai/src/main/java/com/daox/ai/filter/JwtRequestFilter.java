package com.daox.ai.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.daox.ai.utils.Const;
import com.daox.ai.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT请求过滤器，不依赖Spring Security。
 * 它的核心职责是解析Authorization请求头中的JWT，
 * 如果Token有效，则将解析出的用户ID存入到请求的Attribute中，
 * 方便后续的Controller或Service层直接获取，而无需重复解析。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 设置为最高优先级，确保它在所有业务逻辑之前执行
public class JwtRequestFilter extends OncePerRequestFilter { // OncePerRequestFilter 确保过滤器在一次请求中只执行一次

    @Resource
    JwtUtils utils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 从请求头获取Authorization字段
        String authorization = request.getHeader("Authorization");

        // 使用JwtUtils解析Token
        DecodedJWT jwt = utils.resolveJWT(authorization);

        // 如果JWT解析成功且有效
        if (jwt != null) {
            // 从JWT中提取用户ID
            String userId = utils.toId(jwt);
            // 将用户ID存入请求的Attribute中，键为 "userId"
            // 这个 ATTR_USER_ID 应该是一个常量，例如在 Const 类中定义
            request.setAttribute(Const.ATTR_USER_ID, userId);
        }

        // 无论Token是否有效，都继续执行过滤器链
        // 授权决策将在具体的Controller方法中进行
        filterChain.doFilter(request, response);
    }
}
