package com.daox.online.service;

import com.daox.online.entity.mongodb.ChatMessage;
import com.daox.online.entity.views.responseVO.chat.group.CourseGroupConversationVO;
import com.daox.online.entity.views.responseVO.chat.group.CourseGroupDetailVO;
import com.daox.online.entity.views.responseVO.chat.group.CourseGroupMemberVO;
import com.daox.online.entity.views.responseVO.chat.group.GroupMuteAllStatusVO;
import com.daox.online.entity.views.responseVO.chat.group.MutedGroupMemberVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程群聊服务接口。
 * <p>
 * 负责处理课程群聊会话列表、成员权限校验、消息入库以及教师禁言、踢人、公告等核心业务。
 * </p>
 */
public interface CourseGroupChatService {

    /**
     * 获取当前用户可访问的课程群聊列表。
     *
     * @param userId 当前用户ID
     * @return 群聊摘要列表
     */
    List<CourseGroupConversationVO> listAccessibleGroups(String userId);

    /**
     * 获取课程群聊详情。
     *
     * @param userId   当前用户ID
     * @param courseId 课程ID
     * @return 群聊详情
     */
    CourseGroupDetailVO getGroupDetail(String userId, String courseId);

    /**
     * 获取课程群聊历史消息。
     *
     * @param userId     当前用户ID
     * @param courseId   课程ID
     * @param beforeTime 查询时间上界，可为空
     * @param limit      查询条数
     * @return 群聊历史消息
     */
    List<ChatMessage> getGroupHistory(String userId, String courseId, LocalDateTime beforeTime, Integer limit);

    /**
     * 获取课程群聊成员列表。
     *
     * @param userId   当前用户ID
     * @param courseId 课程ID
     * @return 成员列表
     */
    List<CourseGroupMemberVO> getGroupMembers(String userId, String courseId);

    /**
     * 将当前用户在指定课程群中的未读消息标记为已读。
     *
     * @param userId   当前用户ID
     * @param courseId 课程ID
     */
    void markGroupAsRead(String userId, String courseId);

    /**
     * 获取当前课程群聊的禁言名单。
     *
     * @param teacherId 教师ID
     * @param courseId  课程ID
     * @return 禁言名单
     */
    List<MutedGroupMemberVO> getMutedMembers(String teacherId, String courseId);

    /**
     * 获取当前课程群聊的全员禁言状态。
     *
     * @param userId   当前用户ID
     * @param courseId 课程ID
     * @return 全员禁言状态
     */
    GroupMuteAllStatusVO getMuteAllStatus(String userId, String courseId);

    /**
     * 教师对课程群成员执行禁言。
     *
     * @param teacherId       教师ID
     * @param courseId        课程ID
     * @param targetUserId    被禁言学生ID
     * @param durationMinutes 禁言分钟数
     */
    void muteMember(String teacherId, String courseId, String targetUserId, Long durationMinutes);

    /**
     * 教师对课程群执行全员禁言。
     *
     * @param teacherId       教师ID
     * @param courseId        课程ID
     * @param durationMinutes 全员禁言分钟数
     */
    void muteAllMembers(String teacherId, String courseId, Long durationMinutes);

    /**
     * 教师解除课程群成员禁言。
     *
     * @param teacherId    教师ID
     * @param courseId     课程ID
     * @param targetUserId 被解除禁言学生ID
     */
    void unmuteMember(String teacherId, String courseId, String targetUserId);

    /**
     * 教师解除课程群全员禁言。
     *
     * @param teacherId 教师ID
     * @param courseId  课程ID
     */
    void unmuteAllMembers(String teacherId, String courseId);

    /**
     * 教师将课程群成员移出群聊。
     *
     * @param teacherId    教师ID
     * @param courseId     课程ID
     * @param targetUserId 被移出学生ID
     */
    void removeMember(String teacherId, String courseId, String targetUserId);

    /**
     * 教师发布课程群公告消息。
     *
     * @param teacherId 教师ID
     * @param courseId  课程ID
     * @param title     公告标题
     * @param content   公告内容
     * @return 落库后的公告消息
     */
    ChatMessage publishAnnouncement(String teacherId, String courseId, String title, String content);

    /**
     * 保存课程群聊消息。
     *
     * @param courseId             课程ID
     * @param requestMessage       客户端消息载荷
     * @param authenticatedUserId 经过握手认证的用户ID
     * @return 保存后的消息实体
     */
    ChatMessage saveGroupMessage(String courseId, ChatMessage requestMessage, String authenticatedUserId);

    /**
     * 判断用户是否允许连接课程群聊 WebSocket。
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     * @return true 表示允许连接
     */
    boolean canJoinGroupChat(String courseId, String userId);
}