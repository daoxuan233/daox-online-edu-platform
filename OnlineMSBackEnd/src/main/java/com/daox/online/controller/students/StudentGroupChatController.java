package com.daox.online.controller.students;

import com.daox.online.controller.exception.BusinessException;
import com.daox.online.entity.RestBean;
import com.daox.online.entity.mongodb.ChatMessage;
import com.daox.online.entity.views.responseVO.chat.group.CourseGroupConversationVO;
import com.daox.online.entity.views.responseVO.chat.group.CourseGroupDetailVO;
import com.daox.online.entity.views.responseVO.chat.group.CourseGroupMemberVO;
import com.daox.online.service.CourseGroupChatService;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生端课程群聊控制器。
 * <p>
 * 提供学生查看已选课程群聊、群成员以及群消息历史的后端接口。
 * </p>
 */
@RestController
@RequestMapping("/api/student/group-chat")
public class StudentGroupChatController {

    @Resource
    private CourseGroupChatService courseGroupChatService;

    /**
     * 获取当前学生可访问的课程群聊列表。
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
     * 将当前学生在指定课程群中的未读消息标记为已读。
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