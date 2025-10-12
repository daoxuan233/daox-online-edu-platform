package com.daox.online.filter;

import com.daox.online.entity.RestBean;
import com.daox.online.uilts.constant.Const;
import com.daox.online.uilts.FlowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 请求频率限制过滤器
 */
@Component
@Slf4j
@Order(Const.ORDER_FLOW_LIMIT)
public class FlowLimitingFilter extends HttpFilter {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    // 指定时间内最大请求次数限制
    @Value("${spring.web.flow.limit}")
    int limit;

    // 计数时间间隔，单位：秒
    @Value("${spring.web.flow.period}")
    int period;

    // 超出请求限制封禁时间
    @Value("${spring.web.flow.block}")
    int block;

    @Resource
    FlowUtils flowUtils;

    /**
     * 对请求进行拦截处理
     * @param request 请求
     * @param response 响应
     * @param chain 过滤器链
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        String address = request.getRemoteAddr();
        if ("OPTIONS".equals(request.getMethod())&&!tryCount(address))
            this.writeBlockMessage(response);
        else
            chain.doFilter(request, response);
    }

    /**
     * 尝试对指定ip地址请求计数，如果被限制则无法继续访问
     * @param address 请求的ip地址
     * @return 是否操作成功
     */
    private Boolean tryCount(String address){
        synchronized (address.intern()){
            // 已被封禁
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(Const.FLOW_LIMIT_BLOCK+address))) return false;

            String countKey = Const.FLOW_LIMIT_COUNTER + address;
            String blockKey = Const.FLOW_LIMIT_BLOCK + address;
            return flowUtils.limitPeriodCheck(countKey,blockKey,block,limit,period);
        }
    }

    /**
     * 为响应编写拦截内容，提示操作频繁
     * @param response 响应
     * @throws IOException IO异常
     */
    private void writeBlockMessage(HttpServletResponse response) throws IOException {
        response.setStatus(429); // 设置响应状态码为 429，表示请求被限制
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        out.write(RestBean.failure(429,"请求频率过快，稍后再试").asJsonString());
    }
}
