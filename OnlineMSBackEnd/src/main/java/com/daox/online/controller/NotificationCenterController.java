package com.daox.online.controller;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.views.responseVO.notification.NotificationPageVO;
import com.daox.online.service.NotificationCenterService;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 统一消息通知中心控制器。
 * <p>
 * 对外提供当前登录用户的通知列表查询、未读数统计、单条已读和全部已读能力。
 * 控制器本身不区分学生端或教师端，统一通过当前请求中的用户身份完成数据隔离。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationCenterController {

    @Resource
    private NotificationCenterService notificationCenterService;

    /**
     * 分页查询当前用户的通知列表。
     *
     * @param pageNum          页码，从 1 开始
     * @param pageSize         每页条数
     * @param unreadOnly       是否只看未读通知
     * @param notificationType 通知类型过滤条件
     * @param request          当前 HTTP 请求
     * @return 当前用户的通知分页结果
     */
    @GetMapping("")
    public RestBean<NotificationPageVO> listNotifications(@RequestParam(required = false) Integer pageNum,
                                                          @RequestParam(required = false) Integer pageSize,
                                                          @RequestParam(required = false) Boolean unreadOnly,
                                                          @RequestParam(required = false) String notificationType,
                                                          HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null || userId.isBlank()) {
            log.warn("[NotificationCenterController.listNotifications] 用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        return RestBean.success(notificationCenterService.listCurrentUserNotifications(userId, pageNum, pageSize, unreadOnly, notificationType));
    }

    /**
     * 获取当前用户的未读通知总数。
     *
     * @param request 当前 HTTP 请求
     * @return 未读通知数
     */
    @GetMapping("/unread-count")
    public RestBean<Map<String, Long>> getUnreadCount(HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null || userId.isBlank()) {
            log.warn("[NotificationCenterController.getUnreadCount] 用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        return RestBean.success(Map.of("unreadCount", notificationCenterService.countUnread(userId)));
    }

    /**
     * 将指定通知标记为已读。
     *
     * @param notificationId 通知 ID
     * @param request        当前 HTTP 请求
     * @return 标记结果
     */
    @PostMapping("/{notificationId}/read")
    public RestBean<String> markAsRead(@PathVariable String notificationId, HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null || userId.isBlank()) {
            log.warn("[NotificationCenterController.markAsRead] 用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        boolean success = notificationCenterService.markAsRead(userId, notificationId);
        return success ? RestBean.success("标记已读成功") : RestBean.failure(404, "通知不存在或已读");
    }

    /**
     * 将当前用户的全部未读通知标记为已读。
     *
     * @param request 当前 HTTP 请求
     * @return 本次更新的通知数量
     */
    @PostMapping("/read-all")
    public RestBean<Map<String, Integer>> markAllAsRead(HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null || userId.isBlank()) {
            log.warn("[NotificationCenterController.markAllAsRead] 用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        int updatedCount = notificationCenterService.markAllAsRead(userId);
        return RestBean.success(Map.of("updatedCount", updatedCount));
    }
}
