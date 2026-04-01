package com.daox.online.controller.teacher;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.mongodb.ChatMessage;
import com.daox.online.entity.views.responseVO.chat.ChatUserVo;
import com.daox.online.entity.views.responseVO.chat.ConversationVO;
import com.daox.online.service.WebSocketSingleService;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师端单聊控制器。
 * <p>
 * 教师与学生的点对点对话共用同一套消息服务，教师侧仅通过独立路由暴露查询、已读和未读统计能力，
 * 便于权限控制和前端按角色分流调用。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/chat")
public class WebSocketSingleTeacherController {

    @Resource
    private WebSocketSingleService webSocketSingleService;

    /**
     * 查看好友列表
     *
     * @param request 请求
     * @return 好友列表
     */
    @GetMapping("/friend")
    public RestBean<List<ChatUserVo>> getFriendList(HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        List<ChatUserVo> friendList = webSocketSingleService.getFriendList(userId);
        if (friendList != null) {
            return RestBean.success(friendList);
        } else {
            return RestBean.failure(400, "获取好友列表失败");
        }
    }

    /**
     * 获取用户的聊天会话列表
     *
     * @param request 请求
     * @return 会话列表
     */
    @GetMapping("/history")
    public RestBean<List<ConversationVO>> getHistoryList(HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        List<ConversationVO> historyList = webSocketSingleService.getConversationList(userId);
        if (historyList != null) {
            return RestBean.success(historyList);
        } else {
            return RestBean.failure(400, "获取会话列表失败");
        }
    }

    /**
     * 获取和指定对象的聊天记录详情
     *
     * @param request  请求
     * @param friendId 对象的ID
     * @return 聊天记录详情
     */
    @GetMapping("/history/detail")
    public RestBean<List<ChatMessage>> getHistoryDetail(HttpServletRequest request, String friendId) {
        String userId = UserUtils.getCurrentUserId(request);
        List<ChatMessage> historyDetail = webSocketSingleService.getChatHistory(userId, friendId);
        if (historyDetail != null) {
            return RestBean.success(historyDetail);
        } else {
            return RestBean.failure(400, "获取聊天记录详情失败");
        }
    }

    /**
     * 将与指定好友的聊天消息标记为已读
     *
     * @param request  请求对象
     * @param friendId 好友ID
     * @return 操作结果
     */
    @PostMapping("/read")
    public RestBean<Void> markAsRead(HttpServletRequest request, @RequestParam String friendId) {
        String userId = UserUtils.getCurrentUserId(request);
        try {
            webSocketSingleService.markMessagesAsRead(userId, friendId);
            return RestBean.success();
        } catch (Exception e) {
            log.error("标记消息为已读时发生错误", e);
            return RestBean.failure(500, "操作失败");
        }
    }

    /**
     * 获取当前用户所有未读消息的总数
     *
     * @param request 请求对象
     * @return 未读消息总数
     */
    @GetMapping("/unread-count")
    public RestBean<Long> getTotalUnreadCount(HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        try {
            long totalUnreadCount = webSocketSingleService.getTotalUnreadCount(userId);
            return RestBean.success(totalUnreadCount);
        } catch (Exception e) {
            log.error("获取未读消息总数时发生错误", e);
            return RestBean.failure(500, "获取失败");
        }
    }
}
