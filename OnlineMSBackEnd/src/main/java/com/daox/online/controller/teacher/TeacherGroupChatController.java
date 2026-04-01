package com.daox.online.controller.teacher;

import com.daox.online.controller.exception.BusinessException;
import com.daox.online.entity.RestBean;
import com.daox.online.entity.mongodb.ChatMessage;
import com.daox.online.entity.views.requestVO.chat.group.GroupAnnouncementRequest;
import com.daox.online.entity.views.requestVO.chat.group.GroupMuteRequest;
import com.daox.online.entity.views.requestVO.chat.group.GroupMuteAllRequest;
import com.daox.online.entity.views.responseVO.chat.group.CourseGroupConversationVO;
import com.daox.online.entity.views.responseVO.chat.group.CourseGroupDetailVO;
import com.daox.online.entity.views.responseVO.chat.group.CourseGroupMemberVO;
import com.daox.online.entity.views.responseVO.chat.group.GroupMuteAllStatusVO;
import com.daox.online.entity.views.responseVO.chat.group.MutedGroupMemberVO;
import com.daox.online.service.CourseGroupChatService;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师端课程群聊控制器。
 * <p>
 * 提供教师查看课程群聊、历史消息、成员列表以及执行禁言、踢人、公告等管理所需的后端接口。
 * </p>
 */
@RestController
@RequestMapping("/api/teacher/group-chat")
public class TeacherGroupChatController {

    @Resource
    private CourseGroupChatService courseGroupChatService;

    /**
     * 获取当前教师可访问的课程群聊列表。
     *
     * @param request HTTP请求
     * @return 课程群聊列表
     */
    @GetMapping("/courses")
    public RestBean<List<CourseGroupConversationVO>> listGroups(HttpServletRequest request) {
        return RestBean.success(courseGroupChatService.listAccessibleGroups(currentUserId(request)));
    }

    /**
     * 获取课程群聊详情。
     *
     * @param request HTTP请求
     * @param courseId 课程ID
     * @return 群聊详情
     */
    @GetMapping("/course/{courseId}")
    public RestBean<CourseGroupDetailVO> getGroupDetail(HttpServletRequest request,
                                                        @PathVariable String courseId) {
        return RestBean.success(courseGroupChatService.getGroupDetail(currentUserId(request), courseId));
    }

    /**
     * 获取课程群聊历史消息。
     *
     * @param request HTTP请求
     * @param courseId 课程ID
     * @param before 查询时间上界，可为空
     * @param limit 查询条数
     * @return 历史消息列表
     */
    @GetMapping("/course/{courseId}/messages")
    public RestBean<List<ChatMessage>> getGroupHistory(HttpServletRequest request,
                                                       @PathVariable String courseId,
                                                       @RequestParam(required = false)
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                       LocalDateTime before,
                                                       @RequestParam(required = false, defaultValue = "50") Integer limit) {
        return RestBean.success(courseGroupChatService.getGroupHistory(currentUserId(request), courseId, before, limit));
    }

    /**
     * 获取课程群聊成员列表。
     *
     * @param request HTTP请求
     * @param courseId 课程ID
     * @return 成员列表
     */
    @GetMapping("/course/{courseId}/members")
    public RestBean<List<CourseGroupMemberVO>> getGroupMembers(HttpServletRequest request,
                                                               @PathVariable String courseId) {
        return RestBean.success(courseGroupChatService.getGroupMembers(currentUserId(request), courseId));
    }

    /**
     * 将当前教师在指定课程群中的未读消息标记为已读。
     *
     * @param request HTTP请求
     * @param courseId 课程ID
     * @return 操作结果
     */
    @PostMapping("/course/{courseId}/read")
    public RestBean<String> markGroupAsRead(HttpServletRequest request,
                                            @PathVariable String courseId) {
        courseGroupChatService.markGroupAsRead(currentUserId(request), courseId);
        return RestBean.success("群聊未读已清零");
    }

    /**
     * 获取课程群聊禁言名单。
     *
     * @param request HTTP请求
     * @param courseId 课程ID
     * @return 禁言名单
     */
    @GetMapping("/course/{courseId}/mute-members")
    public RestBean<List<MutedGroupMemberVO>> getMutedMembers(HttpServletRequest request,
                                                              @PathVariable String courseId) {
        return RestBean.success(courseGroupChatService.getMutedMembers(currentUserId(request), courseId));
    }

    /**
     * 获取课程群聊全员禁言状态。
     *
     * @param request HTTP请求
     * @param courseId 课程ID
     * @return 全员禁言状态
     */
    @GetMapping("/course/{courseId}/mute-all/status")
    public RestBean<GroupMuteAllStatusVO> getMuteAllStatus(HttpServletRequest request,
                                                           @PathVariable String courseId) {
        return RestBean.success(courseGroupChatService.getMuteAllStatus(currentUserId(request), courseId));
    }

    /**
     * 教师对课程群成员执行禁言。
     *
     * @param request HTTP请求
     * @param courseId 课程ID
     * @param muteRequest 禁言请求体
     * @return 操作结果
     */
    @PostMapping("/course/{courseId}/mute")
    public RestBean<String> muteMember(HttpServletRequest request,
                                       @PathVariable String courseId,
                                       @Valid @RequestBody GroupMuteRequest muteRequest) {
        courseGroupChatService.muteMember(
                currentUserId(request),
                courseId,
                muteRequest.getTargetUserId(),
                muteRequest.getDurationMinutes()
        );
        return RestBean.success("禁言成功");
    }

    /**
     * 教师对课程群执行全员禁言。
     *
     * @param request HTTP请求
     * @param courseId 课程ID
     * @param muteAllRequest 全员禁言请求体
     * @return 操作结果
     */
    @PostMapping("/course/{courseId}/mute-all")
    public RestBean<String> muteAllMembers(HttpServletRequest request,
                                           @PathVariable String courseId,
                                           @Valid @RequestBody GroupMuteAllRequest muteAllRequest) {
        courseGroupChatService.muteAllMembers(
                currentUserId(request),
                courseId,
                muteAllRequest.getDurationMinutes()
        );
        return RestBean.success("全员禁言成功");
    }

    /**
     * 教师解除课程群成员禁言。
     *
     * @param request HTTP请求
     * @param courseId 课程ID
     * @param targetUserId 被解除禁言学生ID
     * @return 操作结果
     */
    @DeleteMapping("/course/{courseId}/mute/{targetUserId}")
    public RestBean<String> unmuteMember(HttpServletRequest request,
                                         @PathVariable String courseId,
                                         @PathVariable String targetUserId) {
        courseGroupChatService.unmuteMember(currentUserId(request), courseId, targetUserId);
        return RestBean.success("解除禁言成功");
    }

    /**
     * 教师解除课程群全员禁言。
     *
     * @param request HTTP请求
     * @param courseId 课程ID
     * @return 操作结果
     */
    @DeleteMapping("/course/{courseId}/mute-all")
    public RestBean<String> unmuteAllMembers(HttpServletRequest request,
                                             @PathVariable String courseId) {
        courseGroupChatService.unmuteAllMembers(currentUserId(request), courseId);
        return RestBean.success("解除全员禁言成功");
    }

    /**
     * 教师将指定学生移出课程群聊。
     *
     * @param request HTTP请求
     * @param courseId 课程ID
     * @param targetUserId 目标学生ID
     * @return 操作结果
     */
    @PostMapping("/course/{courseId}/members/{targetUserId}/kick")
    public RestBean<String> removeMember(HttpServletRequest request,
                                         @PathVariable String courseId,
                                         @PathVariable String targetUserId) {
        courseGroupChatService.removeMember(currentUserId(request), courseId, targetUserId);
        return RestBean.success("移出群聊成功");
    }

    /**
     * 教师向课程群发布系统公告消息。
     *
     * @param request HTTP请求
     * @param courseId 课程ID
     * @param announcementRequest 公告请求体
     * @return 落库后的公告消息
     */
    @PostMapping("/course/{courseId}/announcement")
    public RestBean<ChatMessage> publishAnnouncement(HttpServletRequest request,
                                                     @PathVariable String courseId,
                                                     @Valid @RequestBody GroupAnnouncementRequest announcementRequest) {
        return RestBean.success(courseGroupChatService.publishAnnouncement(
                currentUserId(request),
                courseId,
                announcementRequest.getTitle(),
                announcementRequest.getContent()
        ));
    }

    /**
     * 获取当前认证用户ID。
     *
     * @param request HTTP请求
     * @return 当前用户ID
     */
    private String currentUserId(HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("401", "用户未认证");
        }
        return userId;
    }
}