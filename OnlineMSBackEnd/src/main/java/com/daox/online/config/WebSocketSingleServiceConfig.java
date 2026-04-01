package com.daox.online.config;

import com.daox.online.controller.exception.BusinessException;
import com.daox.online.entity.mongodb.ChatMessage;
import com.daox.online.service.WebSocketSingleService;
import com.daox.online.uilts.JSONUtil;
import com.daox.online.uilts.constant.Const;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单聊 WebSocket 端点配置。
 * <p>
 * 负责管理教师/学生单聊连接、转发消息以及处理异常状态。
 * 当前实现采用单用户单活跃会话映射，因此在连接切换时需要显式规避旧连接覆盖新连接的问题。
 * </p>
 */
@ServerEndpoint(value = "/single", configurator = WebSocketAuthentication.class)
@Component
public class WebSocketSingleServiceConfig implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSingleServiceConfig.class);

    /**
     * 单点服务连接数
     */
    public static final Map<String, Session> numberOfConnectionsMap = new ConcurrentHashMap<>();

    private static WebSocketSingleService webSocketSingleService;

    @Resource
    public void setWebSocketSingleService(WebSocketSingleService webSocketSingleService) {
        WebSocketSingleServiceConfig.webSocketSingleService = webSocketSingleService;
    }

    /**
     * 单点服务连接
     * 链接建立成功时调用
     *
     * @param session 会话
     */
    @OnOpen
    public void onOpen(Session session) {
        Map<String, Object> userProperties = session.getUserProperties();
        String userId = (String) userProperties.get(Const.ATTR_USER_ID);

        if (userId != null) {
            Session previousSession = numberOfConnectionsMap.put(userId, session);
            if (previousSession != null && previousSession != session && previousSession.isOpen()) {
                log.info("检测到用户 {} 的旧 WebSocket 连接已被新连接替换", userId);
            }
            log.info("单点服务连接成功，当前用户ID: {}, 当前连接数: {}", userId, numberOfConnectionsMap.size());
        } else {
            log.warn("未授权的WebSocket连接尝试，即将关闭。");
            try {
                session.close();
            } catch (IOException e) {
                log.error("关闭未授权的WebSocket会话时出错", e);
                throw new BusinessException("401", e.getMessage());
            }
        }
    }

    /**
     * 连接关闭调用的方法
     *
     * @param session 会话
     */
    @OnClose
    public void onClose(Session session) {
        String userId = (String) session.getUserProperties().get(Const.ATTR_USER_ID);
        if (userId != null) {
            numberOfConnectionsMap.computeIfPresent(userId, (key, currentSession) -> currentSession == session ? null : currentSession);
            log.info("用户 {} 断开单点服务连接，当前连接数: {}", userId, numberOfConnectionsMap.size());
        }
    }

    /**
     * 接收到消息时调用
     *
     * @param session 会话
     * @param message 消息
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        log.info("收到消息: {}", message);
        String authenticatedUserId = (String) session.getUserProperties().get(Const.ATTR_USER_ID);
        if (authenticatedUserId == null) {
            throw new BusinessException("401", "当前连接未认证，无法发送消息");
        }

        // 解析消息并交给服务层处理，服务层会以服务端认证身份为准。
        ChatMessage bean = JSONUtil.toBean(message, ChatMessage.class);
        log.info("解析后的ChatMessage对象: senderId={}, receiverId={}, content={}, messageType={}", 
                bean.getSenderId(), bean.getReceiverId(), bean.getContent(), bean.getMessageType());

        ChatMessage chatMessage = webSocketSingleService.addSingleMessage(bean, authenticatedUserId);
        if (chatMessage != null) {
            // 将服务端落库后的最终消息回推给接收方，并给发送方回一个确认版本，便于前端同步 conversationId 与状态。
            String json = JSONUtil.toJson(chatMessage);
            this.sendMessageToUser(chatMessage.getReceiverId(), json);
            this.sendMessageToUser(authenticatedUserId, json);
            log.info("消息发送成功: {}", json);
        } else {
            // 只有当消息未能成功存储和处理时，才抛出异常
            throw new BusinessException("400", "消息发送失败");
        }
    }

    /**
     * 服务端发送信息给指定用户的客户端
     *
     * @param userId  用户ID - 接收消息的用户ID
     * @param message 消息
     */
    private void sendMessageToUser(String userId, String message) {
        Session session = numberOfConnectionsMap.get(userId); // 获取用户对应的Session
        if (session != null) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("发送消息给用户 {} 时出错", userId, e);
                throw new BusinessException("401", e.getMessage());
            }
        }
    }

    /**
     * 连接异常调用的方法
     *
     * @param session   会话
     * @param throwable 异常
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        String userId = (String) session.getUserProperties().get(Const.ATTR_USER_ID);
        log.error("用户 {} 发生异常，异常信息: {}", userId, throwable.getMessage());
        onClose(session);
        throw new BusinessException("500", throwable.getMessage());
    }

    /**
     * 初始化方法 确保单点服务已启动 初始化时调用
     *
     * @throws Exception 初始化异常
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("初始化WebSocketSingleServiceConfig成功");
    }
}
