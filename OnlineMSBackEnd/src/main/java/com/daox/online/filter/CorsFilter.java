package com.daox.online.filter;

import com.daox.online.uilts.constant.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 跨域配置过滤器，仅处理跨域，添加跨域响应头
 */
@Component
@Order(Const.ORDER_CORS)
public class CorsFilter extends HttpFilter {
    @Value("${spring.web.cors.origin}")
    String origin; // 允许的源地址

    @Value("${spring.web.cors.credentials}")
    boolean credentials;

    @Value("${spring.web.cors.methods}")
    String methods; // 允许的请求方法

    /**
     * 处理跨域请求 <br/>
     * 1. 添加所有跨域相关响应头 <br/>
     * 2. 放行请求 <br/>
     * 3. 注意：该过滤器的优先级要高于其他过滤器，否则会被其他过滤器拦截 <br/>
     * @param request 请求
     * @param response 响应
     * @param chain 过滤器链
     * @throws IOException    输入输出异常
     * @throws ServletException servlet异常
     */
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        this.addCorsHeader(request, response);
        // 对于预检的OPTIONS请求，在添加完响应头后直接返回，不继续走过滤器链
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * 添加所有跨域相关响应头
     * @param request 请求
     * @param response 响应
     */
    private void addCorsHeader(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", this.resolveOrigin(request));
        response.setHeader("Access-Control-Allow-Methods", this.resolveMethod());
        response.setHeader("Access-Control-Max-Age", "3600");

        // 这是最关键的修改：不再写死允许的头，而是动态地允许所有浏览器在预检请求中申请的头
        String headers = request.getHeader("Access-Control-Request-Headers");
        if (headers != null) {
            response.setHeader("Access-Control-Allow-Headers", headers);
        }

        if(credentials) {
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }
    }

    /**
     * 解析配置文件中的请求方法
     * @return 解析得到的请求头值
     */
    private String resolveMethod(){
        return methods.equals("*") ? "GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE, PATCH" : methods;
    }

    /**
     * 解析配置文件中的请求原始站点
     * @param request 请求
     * @return 解析得到的请求头值
     */
    private String resolveOrigin(HttpServletRequest request){
        return origin.equals("*") ? request.getHeader("Origin") : origin;
    }
}
