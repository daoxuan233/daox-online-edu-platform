package com.daox.online.config;

import com.daox.online.entity.mongodb.ChatMessage;
import com.daox.online.service.CourseGroupChatService;
import com.daox.online.uilts.JSONUtil;
import com.daox.online.uilts.constant.Const;
import jakarta.annotation.Resource;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 课程群聊 WebSocket 端点配置。
 * <p>
 * 每个课程群聊独立维护在线用户映射，一个用户可同时连接多个课程群，
 * 但同一课程内仅保留该用户最后一次有效连接。
 * </p>
 */
@Slf4j
@Component
@ServerEndpoint(value = "/group/{courseId}", configurator = WebSocketAuthentication.class)
public class WebSocketGroupChatServiceConfig implements InitializingBean {

    /**
     * 课程群在线连接：courseId -> (userId -> session)。
     */
    public static final Map<String, Map<String, Session>> courseConnectionsMap = new ConcurrentHashMap<>();

    /**
     * 反向索引：session -> 课程群上下文。
     */
    private static final Map<Session, GroupSessionContext> sessionContextMap = new ConcurrentHashMap<>();

    private static CourseGroupChatService courseGroupChatService;

    @Resource
    public void setCourseGroupChatService(CourseGroupChatService courseGroupChatService) {
        WebSocketGroupChatServiceConfig.courseGroupChatService = courseGroupChatService;
    }

    /**
     * 建立课程群聊连接。
     *
     * @param session 会话
     * @param courseId 课程ID
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("courseId") String courseId) {
        String userId = (String) session.getUserProperties().get(Const.ATTR_USER_ID);
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(courseId)) {
            closeWithPolicyViolation(session, "群聊连接未认证或课程ID缺失");
            return;
        }
        if (courseGroupChatService == null || !courseGroupChatService.canJoinGroupChat(courseId, userId)) {
            closeWithPolicyViolation(session, "当前用户无权接入该课程群聊");
            return;
        }

        Map<String, Session> groupSessions = courseConnectionsMap.computeIfAbsent(courseId, key -> new ConcurrentHashMap<>());
        Session previousSession = groupSessions.put(userId, session);
        if (previousSession != null && previousSession != session && previousSession.isOpen()) {
            log.info("用户 {} 在课程 {} 群聊中的旧连接已被新连接替换", userId, courseId);
        }

        sessionContextMap.put(session, new GroupSessionContext(courseId, userId));
        log.info("课程群聊连接成功，courseId={}, userId={}, 当前课程在线人数={}", courseId, userId, groupSessions.size());
    }

    /**
     * 处理课程群聊消息。
     *
     * @param session 会话
     * @param courseId 课程ID
     * @param message 原始消息体
     */
    @OnMessage
    public void onMessage(Session session, @PathParam("courseId") String courseId, String message) {
        String authenticatedUserId = (String) session.getUserProperties().get(Const.ATTR_USER_ID);
        if (!StringUtils.hasText(authenticatedUserId)) {
            sendError(session, "401", "当前连接未认证，无法发送群消息");
            return;
        }

        try {
            ChatMessage requestMessage = JSONUtil.toBean(message, ChatMessage.class);
            ChatMessage savedMessage = courseGroupChatService.saveGroupMessage(courseId, requestMessage, authenticatedUserId);
            broadcastMessageToCourse(courseId, JSONUtil.toJson(savedMessage));
        } catch (Exception exception) {
            log.warn("课程群聊消息处理失败，courseId={}, userId={}, reason={}", courseId, authenticatedUserId, exception.getMessage());
            sendError(session,
                    exception instanceof com.daox.online.controller.exception.BusinessException businessException ? businessException.getCode() : "500",
                    exception.getMessage());
        }
    }

    /**
     * 关闭课程群聊连接。
     *
     * @param session 会话
     */
    @OnClose
    public void onClose(Session session) {
        GroupSessionContext context = sessionContextMap.remove(session);
        if (context == null) {
            return;
        }

        Map<String, Session> groupSessions = courseConnectionsMap.get(context.courseId());
        if (groupSessions != null) {
            groupSessions.computeIfPresent(context.userId(), (key, currentSession) -> currentSession == session ? null : currentSession);
            if (groupSessions.isEmpty()) {
                courseConnectionsMap.remove(context.courseId());
            }
        }

        log.info("课程群聊连接关闭，courseId={}, userId={}", context.courseId(), context.userId());
    }

    /**
     * 处理课程群聊连接异常。
     *
     * @param session 会话
     * @param throwable 异常
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        GroupSessionContext context = sessionContextMap.get(session);
        if (context != null) {
            log.error("课程群聊连接异常，courseId={}, userId={}, reason={}",
                    context.courseId(), context.userId(), throwable.getMessage(), throwable);
        } else {
            log.error("课程群聊连接异常，原因={}", throwable.getMessage(), throwable);
        }
    }

    /**
     * 广播消息到指定课程群。
     *
     * @param courseId 课程ID
     * @param message 消息文本
     */
    public static void broadcastMessageToCourse(String courseId, String message) {
        Map<String, Session> groupSessions = courseConnectionsMap.get(courseId);
        if (groupSessions == null || groupSessions.isEmpty()) {
            return;
        }

        groupSessions.forEach((userId, session) -> {
            if (session == null || !session.isOpen()) {
                return;
            }
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException exception) {
                log.warn("群聊消息广播失败，courseId={}, userId={}, reason={}", courseId, userId, exception.getMessage());
            }
        });
    }

    /**
     * 判断指定用户当前是否在线连接到某个课程群。
     *
     * @param courseId 课程ID
     * @param userId 用户ID
     * @return true 表示当前存在有效连接
     */
    public static boolean isCourseUserConnected(String courseId, String userId) {
        Map<String, Session> groupSessions = courseConnectionsMap.get(courseId);
        if (groupSessions == null || groupSessions.isEmpty()) {
            return false;
        }

        Session session = groupSessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 将指定用户主动断开出某个课程群聊连接。
     *
     * @param courseId 课程ID
     * @param userId 用户ID
     * @param reason 关闭原因
     */
    public static void disconnectCourseUser(String courseId, String userId, String reason) {
        Map<String, Session> groupSessions = courseConnectionsMap.get(courseId);
        if (groupSessions == null || groupSessions.isEmpty()) {
            return;
        }

        Session session = groupSessions.remove(userId);
        if (session == null) {
            return;
        }

        sessionContextMap.remove(session);
        if (groupSessions.isEmpty()) {
            courseConnectionsMap.remove(courseId);
        }

        if (!session.isOpen()) {
            return;
        }

        try {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, trimCloseReason(reason)));
        } catch (IOException exception) {
            log.warn("关闭课程群聊用户连接失败，courseId={}, userId={}, reason={}", courseId, userId, exception.getMessage());
        }
    }

    /**
     * 发送错误消息给当前会话。
     *
     * @param session 会话
     * @param code 错误码
     * @param message 错误信息
     */
    private void sendError(Session session, String code, String message) {
        if (session == null || !session.isOpen()) {
            return;
        }

        try {
            Map<String, Object> errorPayload = new HashMap<>();
            errorPayload.put("type", "ERROR");
            errorPayload.put("code", code);
            errorPayload.put("message", message);
            session.getBasicRemote().sendText(JSONUtil.toJson(errorPayload));
        } catch (IOException exception) {
            log.warn("发送课程群聊错误消息失败，reason={}", exception.getMessage());
        }
    }

    /**
     * 关闭不合法的群聊连接。
     *
     * @param session 会话
     * @param reason 关闭原因
     */
    private void closeWithPolicyViolation(Session session, String reason) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, trimCloseReason(reason)));
        } catch (IOException exception) {
            log.warn("关闭课程群聊非法连接失败，reason={}", exception.getMessage());
        }
    }

    /**
     * 修剪 WebSocket CloseReason，避免超过协议长度限制。
     *
     * @param reason 原始原因
     * @return 修剪后的原因
     */
    private static String trimCloseReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            return "群聊连接关闭";
        }
        String normalizedReason = reason.trim();
        return normalizedReason.length() > 120 ? normalizedReason.substring(0, 120) : normalizedReason;
    }

    /**
     * 初始化日志。
     */
    @Override
    public void afterPropertiesSet() {
        log.info("初始化课程群聊 WebSocket 端点成功");
    }

    /**
     * 课程群聊会话上下文。
     *
     * @param courseId 课程ID
     * @param userId 用户ID
     */
    private record GroupSessionContext(String courseId, String userId) {
    }
}