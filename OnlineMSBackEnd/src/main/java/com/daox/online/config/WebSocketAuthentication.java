package com.daox.online.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.daox.online.uilts.JwtUtils;
import com.daox.online.uilts.constant.Const;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 认证配置器
 * 用于在握手阶段验证 JWT Token
 */
@Component
public class WebSocketAuthentication extends ServerEndpointConfig.Configurator {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthentication.class);

    // 使用静态变量来接收由 Spring 容器注入的 JwtUtils
    private static JwtUtils jwtUtils;

    // 提供一个静态方法用于 Spring 注入
    public static void setJwtUtils(JwtUtils utils) {
        WebSocketAuthentication.jwtUtils = utils;
    }

    /**
     * 拦截 WebSocket 握手请求，进行身份验证
     * @param sec the server endpoint configuration
     * @param request the handshake request
     * @param response the handshake response
     */
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        log.info("WebSocket 握手开始，准备进行身份验证...");
        // 调用父类的方法，通常是必须的
        super.modifyHandshake(sec, request, response);

        // 检查 jwtUtils 是否已成功注入
        if (jwtUtils == null) {
            log.error("WebSocketAuthentication 失败：JwtUtils 实例未注入。");
            return; // 注入失败，无法继续认证
        }

        // 1. 从请求头中获取Token，类似于JwtAuthenticationFilter从Authorization头获取
        // 我们约定客户端通过Sec-WebSocket-Protocol头传递JWT
        String token = null;
        List<String> protocols = request.getHeaders().get("Sec-WebSocket-Protocol");
        if (protocols != null && !protocols.isEmpty()) {
            // 客户端应直接发送token，我们在这里拼接成 "Bearer <token>" 的形式以供resolveJWT方法使用
            token = "Bearer " + protocols.get(0);
            log.info("从 Sec-WebSocket-Protocol 获取到 Token。");
        }else {
            log.warn("请求头 Sec-WebSocket-Protocol 中未找到 Token。");
        }

        // 2. 解析和验证JWT，与JwtAuthenticationFilter逻辑一致
        DecodedJWT jwt = jwtUtils.resolveJWT(token);

        if (jwt != null) {
            // 3. 如果验证成功，提取用户信息并存储
            // 这部分逻辑与JwtAuthenticationFilter类似，但存储目标不同
            UserDetails user = jwtUtils.toUser(jwt);
            String userId = jwtUtils.toId(jwt);

            // 在JwtAuthenticationFilter中，会创建一个AuthenticationToken并放入SecurityContext
            // 在WebSocket中，没有独立的SecurityContext，我们将认证信息存入userProperties
            // 这样在@OnOpen, @OnMessage等方法中可以通过Session.getUserProperties()获取
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            Map<String, Object> userProperties = sec.getUserProperties();
            userProperties.put("authentication", authentication); // 存储完整的认证对象
            userProperties.put(Const.ATTR_USER_ID, userId); // 存储用户ID，与Filter保持键名一致

            // 必须将客户端请求的protocol设置回响应，否则连接会因协议不匹配而失败
            response.getHeaders().put("Sec-WebSocket-Protocol", protocols);
        }
        // 4. 如果验证失败，jwt为null，我们不存储任何用户信息。
        // 在@OnOpen中可以检查userProperties来判断是否允许连接。
    }
}
