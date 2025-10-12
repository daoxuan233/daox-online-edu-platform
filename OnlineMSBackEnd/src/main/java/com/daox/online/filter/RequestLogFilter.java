package com.daox.online.filter;

import com.alibaba.fastjson2.JSONObject;
import com.daox.online.uilts.constant.Const;
import com.daox.online.uilts.SnowflakeIdGenerator;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 请求日志过滤器，用于记录所有用户请求信息
 */
@Slf4j
@Component // 将该类标记为Spring组件，使其成为Spring容器的一个Bean
public class RequestLogFilter extends OncePerRequestFilter {

    @Resource
    SnowflakeIdGenerator generator;

    private static final String MDC_REQUEST_ID_KEY = "reqId";

    private static final Set<String> IGNORE_URLS = Set.of(
            ".js", ".css", ".html", ".ico", ".png", ".jpg", ".jpeg", ".gif",
            "/swagger-ui/", "/v3/api-docs"
    );

    /**
     * 内部过滤器方法，用于处理请求和响应的日志记录
     * 该方法会在每个请求到达时被调用，用于记录请求的开始和结束时间，以及请求的参数和响应结果。
     *
     * @param request     请求对象
     * @param response    响应对象
     * @param filterChain 过滤器链
     * @throws ServletException 异常
     * @throws IOException      异常
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (this.isIgnoreUrl(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        MDC.put(MDC_REQUEST_ID_KEY, String.valueOf(generator.nextId()));
        try {
            this.logRequestStart(request);
            ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
            filterChain.doFilter(request, wrapper);
            this.logRequestEnd(wrapper, startTime);
            wrapper.copyBodyToResponse();
        } finally {
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }

    /**
     * 判定当前请求url是否不需要日志打印
     *
     * @param url 路径
     * @return 是否忽略
     */
    private boolean isIgnoreUrl(String url) {
        return IGNORE_URLS.stream().anyMatch(url::contains);
        // return false; // 没有匹配到忽略的路径，返回false
    }

    /**
     * 请求结束时的日志打印，包含处理耗时以及响应结果
     *
     * @param wrapper   用于读取响应结果的包装类
     * @param startTime 起始时间
     */
    public void logRequestEnd(ContentCachingResponseWrapper wrapper, long startTime) {
        long time = System.currentTimeMillis() - startTime;
        int status = wrapper.getStatus();
        String content;

        String contentType = wrapper.getContentType();
        boolean isTextBased = contentType != null && (
                contentType.startsWith("text/") ||
                        contentType.contains("json") ||
                        contentType.contains("xml")
        );

        if (status == 101) {
            content = "WebSocket 握手成功";
        } else if (status >= 400) {
            content = status + " 错误";
        } else {
            byte[] body = wrapper.getContentAsByteArray();
            if (body.length == 0) {
                content = "[Empty Response Body]";
            } else if (isTextBased) {
                content = new String(body, StandardCharsets.UTF_8);
            } else {
                content = "[Binary Response Body, Content-Type: " + contentType + "]";
            }
        }
        log.info("请求处理耗时: {}ms | 响应状态: {} | 响应结果: {}", time, status, content);
    }

    /**
     * 请求开始时的日志打印，包含请求全部信息，以及对应用户角色
     *
     * @param request 请求
     */
    public void logRequestStart(HttpServletRequest request) {
        /*long reqId = generator.nextId();
        MDC.put(MDC_REQUEST_ID_KEY, String.valueOf(reqId));
        JSONObject object = new JSONObject();
        request.getParameterMap().forEach((k, v) -> object.put(k, v.length > 0 ? v[0] : null));
        Object id = request.getAttribute(Const.ATTR_USER_ID);
        if (id != null) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            log.info("请求URL: \"{}\" ({}) | 远程IP地址: {} │ 身份: {} (UID: {}) | 角色: {} | 请求参数列表: {}",
                    request.getServletPath(), request.getMethod(), request.getRemoteAddr(),
                    user.getUsername(), id, user.getAuthorities(), object);
        } else {
            log.info("请求URL: \"{}\" ({}) | 远程IP地址: {} │ 身份: 未验证 | 请求参数列表: {}",
                    request.getServletPath(), request.getMethod(), request.getRemoteAddr(), object);
        }*/

        JSONObject object = new JSONObject();
        request.getParameterMap().forEach((k, v) -> object.put(k, v.length > 0 ? v[0] : null));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            Object id = request.getAttribute(Const.ATTR_USER_ID);
            log.info("请求URL: \"{}\" ({}) | 远程IP地址: {} │ 身份: {} (UID: {}) | 角色: {} | 请求参数列表: {}",
                    request.getServletPath(), request.getMethod(), request.getRemoteAddr(),
                    user.getUsername(), id, user.getAuthorities(), object);
        } else {
            log.info("请求URL: \"{}\" ({}) | 远程IP地址: {} │ 身份: 未验证 | 请求参数列表: {}",
                    request.getServletPath(), request.getMethod(), request.getRemoteAddr(), object);
        }
    }
}
