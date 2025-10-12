package com.daox.online.config;

import com.daox.online.uilts.JwtUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocket
public class WebSocketConfiguration {

    @Resource
    private JwtUtils jwtUtils;

    /**
     * 创建一个ServerEndpointExporter对象
     * 该对象的作用是将@ServerEndpoint注解的类自动注册到Spring容器中
     *
     * @return ServerEndpointExporter对象
     */
    @Bean
    public static ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 使用 @PostConstruct 注解，在 Spring 初始化该配置类后，
     * 调用 WebSocketAuthentication 的静态方法，将 jwtUtils 实例注入。
     */
    @PostConstruct
    public void setJwtUtils() {
        WebSocketAuthentication.setJwtUtils(jwtUtils);
    }
}
