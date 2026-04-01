package com.daox.online.service;

import com.daox.online.entity.mysql.Assessments;
import com.daox.online.entity.mysql.Courses;
import com.daox.online.entity.mysql.SystemAnnouncements;
import com.daox.online.entity.mysql.Users;
import com.daox.online.entity.views.responseVO.notification.NotificationPageVO;

import java.util.Collection;
import java.util.Date;

/**
 * 统一消息通知中心服务接口。
 * <p>
 * 负责承接系统公告、测评发布、阅卷完成、课程变动等业务事件，
 * 并向统一通知表中落库，同时对外暴露查询与已读操作能力。
 * </p>
 */
public interface NotificationCenterService {

    /**
     * 分发系统公告通知。
     *
     * @param announcement 系统公告实体
     * @param actor        公告发布人
     */
    void dispatchSystemAnnouncement(SystemAnnouncements announcement, Users actor);

    /**
     * 同步更新公告类通知内容。
     *
     * @param announcementId 公告 ID
     * @param title          更新后的标题
     * @param content        更新后的内容
     * @param expiresAt      过期时间
     */
    void syncAnnouncementNotification(String announcementId, String title, String content, Date expiresAt);

    /**
     * 删除指定公告对应的统一通知记录。
     *
     * @param announcementId 公告 ID
     */
    void removeAnnouncementNotification(String announcementId);

    /**
     * 发送测评发布通知。
     *
     * @param assessment 测评实体
     * @param course     所属课程
     * @param actor      发布人
     */
    void notifyAssessmentPublished(Assessments assessment, Courses course, Users actor);

    /**
     * 发送阅卷完成通知。
     *
     * @param assessment       测评实体
     * @param course           所属课程
     * @param actor            操作人
     * @param recipientUserIds 接收用户 ID 集合
     */
    void notifyGradingCompleted(Assessments assessment, Courses course, Users actor, Collection<String> recipientUserIds);

    /**
     * 发送课程变动通知。
     *
     * @param course       课程实体
     * @param actor        操作人
     * @param changeAction 变动动作类型
     */
    void notifyCourseChanged(Courses course, Users actor, String changeAction);

    /**
     * 向课程教师发送审核结果或后台状态处理结果通知。
     *
     * @param course        课程实体
     * @param actor         操作人
     * @param reviewAction  审核/流转动作
     * @param comment       审核说明或备注
     */
    void notifyCourseReviewResult(Courses course, Users actor, String reviewAction, String comment);

    /**
     * 分页查询当前用户的通知列表。
     *
     * @param userId           当前用户 ID
     * @param pageNum          页码
     * @param pageSize         每页条数
     * @param unreadOnly       是否只看未读
     * @param notificationType 通知类型过滤条件
     * @return 通知分页结果
     */
    NotificationPageVO listCurrentUserNotifications(String userId,
                                                    Integer pageNum,
                                                    Integer pageSize,
                                                    Boolean unreadOnly,
                                                    String notificationType);

    /**
     * 统计当前用户未读通知数。
     *
     * @param userId 当前用户 ID
     * @return 未读数
     */
    long countUnread(String userId);

    /**
     * 标记单条通知为已读。
     *
     * @param userId         当前用户 ID
     * @param notificationId 通知 ID
     * @return 是否标记成功
     */
    boolean markAsRead(String userId, String notificationId);

    /**
     * 标记当前用户全部未读通知为已读。
     *
     * @param userId 当前用户 ID
     * @return 本次更新数量
     */
    int markAllAsRead(String userId);
}
