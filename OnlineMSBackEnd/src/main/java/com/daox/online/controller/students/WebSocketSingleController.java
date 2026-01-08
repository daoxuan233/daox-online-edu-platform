package com.daox.online.controller.students;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.mongodb.ChatMessage;
import com.daox.online.entity.views.responseVO.chat.ChatUserVo;
import com.daox.online.entity.views.responseVO.chat.ConversationVO;
import com.daox.online.service.WebSocketSingleService;
import com.daox.online.service.friend.FriendsServiceImpl;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 单聊控制器
 * 提供单聊相关的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/student/chat")
public class WebSocketSingleController {

    @Resource
    private WebSocketSingleService webSocketSingleService;

    @Resource
    private FriendsServiceImpl friendsService;

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

    /**
     * 查询好友
     *
     * @param request          请求
     * @param friendIdentifier 好友标识符（学号/工号）
     * @return 操作结果
     */
    @PostMapping("/friend")
    public RestBean<ChatUserVo> queryFriend(HttpServletRequest request, @RequestParam("friend_identifier") String friendIdentifier) {
        String userId = UserUtils.getCurrentUserId(request);
        return friendsService.queryFriendByIdentifier(userId, friendIdentifier);
    }

    /**
     * 添加好友
     *
     * @param request      请求
     * @param targetUserId 好友id
     * @param remark       备注
     * @return 操作结果
     */
    @PostMapping("/friend/add")
    public RestBean<String> addFriend(HttpServletRequest request,
                                      @RequestParam("targetUserId") String targetUserId,
                                      @RequestParam(value = "remark", required = false) String remark) {
        String userId = UserUtils.getCurrentUserId(request);
        try {
            return friendsService.addFriend(userId, targetUserId, remark);
        } catch (Exception e) {
            log.error("添加好友时发生错误", e);
            return RestBean.failure(500, "操作失败");
        }
    }

    /**
     * 确认好友申请
     *
     * @param request      请求
     * @param targetUserId 好友id
     * @param remark       备注
     * @return 操作结果
     */
    @PostMapping("/friend/confirm")
    public RestBean<String> confirmFriend(HttpServletRequest request,
                                          @RequestParam("targetUserId") String targetUserId,
                                          @RequestParam(value = "remark", required = false) String remark) {
        String userId = UserUtils.getCurrentUserId(request);
        try {
            return friendsService.confirmFriendRequest(userId, targetUserId, remark);
        } catch (Exception e) {
            log.error("确认好友申请时发生错误", e);
            return RestBean.failure(500, "操作失败");
        }
    }

    /**
     * 获取待确认好友申请列表
     *
     * @param request 请求
     * @return 待确认好友申请列表
     */
    @GetMapping("/friend/pending")
    public RestBean<List<ChatUserVo>> getPendingFriendRequests(HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        try {
            return friendsService.getPendingFriendRequestList(userId);
        } catch (Exception e) {
            log.error("获取待确认好友申请列表时发生错误", e);
            return RestBean.failure(500, "操作失败");
        }
    }

    /**
     * 统计用户待处理好友申请数
     *
     * @param request 请求
     * @return 待处理好友申请数
     */
     @GetMapping("/friend/pending/count")
    public RestBean<Integer> countPendingFriendRequests(HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        try {
            return friendsService.countPendingFriendRequest(userId);
        } catch (Exception e) {
            log.error("统计待处理好友申请数时发生错误", e);
            return RestBean.failure(500, "操作失败");
        }
    }
}
