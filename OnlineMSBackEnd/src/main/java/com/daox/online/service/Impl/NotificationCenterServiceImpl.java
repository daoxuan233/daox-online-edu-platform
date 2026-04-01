package com.daox.online.service.Impl;

import com.daox.online.entity.mysql.Assessments;
import com.daox.online.entity.mysql.Courses;
import com.daox.online.entity.mysql.SystemAnnouncements;
import com.daox.online.entity.mysql.UserNotifications;
import com.daox.online.entity.mysql.Users;
import com.daox.online.entity.views.responseVO.notification.NotificationItemVO;
import com.daox.online.entity.views.responseVO.notification.NotificationPageVO;
import com.daox.online.mapper.SysUsersMapper;
import com.daox.online.mapper.UserCoursesMapper;
import com.daox.online.mapper.UserNotificationsMapper;
import com.daox.online.service.NotificationCenterService;
import com.daox.online.uilts.SecondaryHybridIdGenerator;
import com.daox.online.uilts.constant.NotificationConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 统一消息通知中心服务实现。
 * <p>
 * 该实现负责把分散在系统公告、课程、测评和阅卷流程中的站内提醒统一沉淀到
 * user_notifications 表中，并提供面向当前登录用户的查询与已读能力。
 * </p>
 */
@Service
@Slf4j
public class NotificationCenterServiceImpl implements NotificationCenterService {

    /** 默认页码。 */
    private static final int DEFAULT_PAGE_NUM = 1;

    /** 默认分页大小。 */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /** 单次查询允许的最大分页大小。 */
    private static final int MAX_PAGE_SIZE = 100;

    /** 日志中允许输出的接收人示例数量。 */
    private static final int MAX_RECIPIENT_LOG_SIZE = 3;

    @Resource
    private UserNotificationsMapper userNotificationsMapper;

    @Resource
    private SysUsersMapper sysUsersMapper;

    @Resource
    private UserCoursesMapper userCoursesMapper;

    @Resource
    private SecondaryHybridIdGenerator secondaryHybridIdGenerator;

    /**
     * 将系统公告分发为统一通知。
     *
     * @param announcement 公告实体
     * @param actor        公告发布人
     */
    @Override
    public void dispatchSystemAnnouncement(SystemAnnouncements announcement, Users actor) {
        if (announcement == null || !StringUtils.hasText(announcement.getId())) {
            return;
        }
        List<String> recipientIds = sysUsersMapper.findActiveUserIds();
        if (recipientIds == null || recipientIds.isEmpty()) {
            log.info("[NotificationCenter] 系统公告无有效接收人: announcementId={}", announcement.getId());
            return;
        }

        Date createdAt = announcement.getCreatedAt() == null ? new Date() : announcement.getCreatedAt();
        List<UserNotifications> notifications = buildNotifications(
                recipientIds,
                NotificationConstants.TYPE_SYSTEM_ANNOUNCEMENT,
                NotificationConstants.SOURCE_ANNOUNCEMENT,
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                NotificationConstants.LEVEL_IMPORTANT,
                actor == null ? null : actor.getId(),
                null,
                null,
                createdAt,
                announcement.getExpiredAt()
        );
        batchInsert(notifications, "dispatchSystemAnnouncement");
    }

    /**
     * 同步更新公告类通知内容。
     *
     * @param announcementId 公告 ID
     * @param title          更新后的标题
     * @param content        更新后的内容
     * @param expiresAt      更新后的过期时间
     */
    @Override
    public void syncAnnouncementNotification(String announcementId, String title, String content, Date expiresAt) {
        if (!StringUtils.hasText(announcementId)) {
            return;
        }
        userNotificationsMapper.updateBySource(
                NotificationConstants.SOURCE_ANNOUNCEMENT,
                announcementId,
                title,
                content,
                NotificationConstants.LEVEL_IMPORTANT,
                expiresAt
        );
    }

    /**
     * 删除指定公告对应的通知记录。
     *
     * @param announcementId 公告 ID
     */
    @Override
    public void removeAnnouncementNotification(String announcementId) {
        if (!StringUtils.hasText(announcementId)) {
            return;
        }
        userNotificationsMapper.markDeletedBySource(NotificationConstants.SOURCE_ANNOUNCEMENT, announcementId);
    }

    /**
     * 向选课学生发送测评发布通知。
     *
     * @param assessment 测评实体
     * @param course     课程实体
     * @param actor      操作人
     */
    @Override
    public void notifyAssessmentPublished(Assessments assessment, Courses course, Users actor) {
        if (assessment == null || course == null || !StringUtils.hasText(course.getId())) {
            return;
        }
        List<String> recipientIds = userCoursesMapper.findUserIdsByCourseId(course.getId());
        if (recipientIds == null || recipientIds.isEmpty()) {
            log.info("[NotificationCenter] 测评发布无选课学生: assessmentId={}, courseId={}", assessment.getId(), course.getId());
            return;
        }

        String title = "新测评已发布：" + safeText(assessment.getTitle(), "未命名测评");
        String content = "课程《" + safeText(course.getTitle(), "未命名课程") + "》已发布新测评，请及时进入课程查看并完成。";
        List<UserNotifications> notifications = buildNotifications(
                recipientIds,
                NotificationConstants.TYPE_ASSESSMENT_PUBLISHED,
                NotificationConstants.SOURCE_ASSESSMENT,
                assessment.getId(),
                title,
                content,
                NotificationConstants.LEVEL_INFO,
                actor == null ? null : actor.getId(),
                course.getId(),
                assessment.getId(),
                new Date(),
                null
        );
        batchInsert(notifications, "notifyAssessmentPublished");
    }

    /**
     * 向指定学生集合发送阅卷完成通知。
     *
     * @param assessment       测评实体
     * @param course           课程实体
     * @param actor            操作人
     * @param recipientUserIds 接收用户 ID 集合
     */
    @Override
    public void notifyGradingCompleted(Assessments assessment, Courses course, Users actor, Collection<String> recipientUserIds) {
        if (assessment == null || course == null || recipientUserIds == null || recipientUserIds.isEmpty()) {
            return;
        }
        String title = "测评阅卷已完成：" + safeText(assessment.getTitle(), "未命名测评");
        String content = "课程《" + safeText(course.getTitle(), "未命名课程") + "》的测评已完成阅卷，成绩和反馈现可查看。";
        List<UserNotifications> notifications = buildNotifications(
                recipientUserIds,
                NotificationConstants.TYPE_GRADING_COMPLETED,
                NotificationConstants.SOURCE_ASSESSMENT,
                assessment.getId(),
                title,
                content,
                NotificationConstants.LEVEL_SUCCESS,
                actor == null ? null : actor.getId(),
                course.getId(),
                assessment.getId(),
                new Date(),
                null
        );
        batchInsert(notifications, "notifyGradingCompleted");
    }

    /**
     * 向课程下的选课学生发送课程变动通知。
     *
     * @param course       课程实体
     * @param actor        操作人
     * @param changeAction 课程变动动作
     */
    @Override
    public void notifyCourseChanged(Courses course, Users actor, String changeAction) {
        if (course == null || !StringUtils.hasText(course.getId())) {
            return;
        }
        List<String> recipientIds = userCoursesMapper.findUserIdsByCourseId(course.getId());
        if (recipientIds == null || recipientIds.isEmpty()) {
            log.info("[NotificationCenter] 课程变动无选课学生: courseId={}, action={}", course.getId(), changeAction);
            return;
        }

        String title;
        String content;
        String level = NotificationConstants.LEVEL_INFO;
        String courseTitle = safeText(course.getTitle(), "未命名课程");
        if (NotificationConstants.COURSE_ACTION_PUBLISHED.equals(changeAction)) {
            title = "课程已发布：" + courseTitle;
            content = "课程《" + courseTitle + "》现已发布，您可以查看最新课程安排并开始学习。";
        } else if (NotificationConstants.COURSE_ACTION_REPUBLISHED.equals(changeAction)) {
            title = "课程已重新上架：" + courseTitle;
            content = "课程《" + courseTitle + "》已重新上架，您可以继续查看课程内容与学习安排。";
            level = NotificationConstants.LEVEL_SUCCESS;
        } else if (NotificationConstants.COURSE_ACTION_TAKEN_DOWN.equals(changeAction)) {
            title = "课程已下架：" + courseTitle;
            content = "课程《" + courseTitle + "》已被下架，请留意教师或管理员的后续安排。";
            level = NotificationConstants.LEVEL_WARNING;
        } else if (NotificationConstants.COURSE_ACTION_ARCHIVED.equals(changeAction)) {
            title = "课程安排有更新：" + courseTitle;
            content = "课程《" + courseTitle + "》已归档或暂停开放，请留意教师后续安排。";
            level = NotificationConstants.LEVEL_WARNING;
        } else if (NotificationConstants.COURSE_ACTION_OUTLINE_UPDATED.equals(changeAction)) {
            title = "课程内容有更新：" + courseTitle;
            content = "课程《" + courseTitle + "》的章节或小节内容已更新，请留意最新学习安排。";
        } else {
            title = "课程信息有更新：" + courseTitle;
            content = "课程《" + courseTitle + "》的信息或学习安排已调整，请及时查看。";
        }

        List<UserNotifications> notifications = buildNotifications(
                recipientIds,
                NotificationConstants.TYPE_COURSE_CHANGED,
                NotificationConstants.SOURCE_COURSE,
                course.getId(),
                title,
                content,
                level,
                actor == null ? null : actor.getId(),
                course.getId(),
                course.getId(),
                new Date(),
                null
        );
        batchInsert(notifications, "notifyCourseChanged");
    }

    /**
     * 向课程教师发送审核结果或后台状态流转通知。
     *
     * @param course       课程实体
     * @param actor        操作人
     * @param reviewAction 审核动作
     * @param comment      审核说明
     */
    @Override
    public void notifyCourseReviewResult(Courses course, Users actor, String reviewAction, String comment) {
        if (course == null || !StringUtils.hasText(course.getTeacherId())) {
            return;
        }

        String courseTitle = safeText(course.getTitle(), "未命名课程");
        String title;
        String content;
        String level;

        if (NotificationConstants.COURSE_ACTION_REVIEW_APPROVED.equals(reviewAction)) {
            title = "课程审核已通过：" + courseTitle;
            content = "您提交的课程《" + courseTitle + "》已审核通过，课程当前已发布。";
            level = NotificationConstants.LEVEL_SUCCESS;
        } else if (NotificationConstants.COURSE_ACTION_REVIEW_REJECTED.equals(reviewAction)) {
            title = "课程审核未通过：" + courseTitle;
            content = "您提交的课程《" + courseTitle + "》未通过审核，课程已退回草稿。" + appendComment(comment);
            level = NotificationConstants.LEVEL_WARNING;
        } else if (NotificationConstants.COURSE_ACTION_TAKEN_DOWN.equals(reviewAction)) {
            title = "课程已下架：" + courseTitle;
            content = "课程《" + courseTitle + "》已被管理员下架。" + appendComment(comment);
            level = NotificationConstants.LEVEL_WARNING;
        } else if (NotificationConstants.COURSE_ACTION_REPUBLISHED.equals(reviewAction)) {
            title = "课程已重新上架：" + courseTitle;
            content = "课程《" + courseTitle + "》已重新上架，学生可再次访问与学习。";
            level = NotificationConstants.LEVEL_SUCCESS;
        } else if (NotificationConstants.COURSE_ACTION_ARCHIVED.equals(reviewAction)) {
            title = "课程已归档：" + courseTitle;
            content = "课程《" + courseTitle + "》已完成归档处理。" + appendComment(comment);
            level = NotificationConstants.LEVEL_WARNING;
        } else {
            title = "课程状态已更新：" + courseTitle;
            content = "课程《" + courseTitle + "》的审核或状态已更新，请及时查看。" + appendComment(comment);
            level = NotificationConstants.LEVEL_INFO;
        }

        List<UserNotifications> notifications = buildNotifications(
                Collections.singleton(course.getTeacherId()),
                NotificationConstants.TYPE_COURSE_REVIEW_RESULT,
                NotificationConstants.SOURCE_COURSE,
                course.getId(),
                title,
                content,
                level,
                actor == null ? null : actor.getId(),
                course.getId(),
                course.getId(),
                new Date(),
                null
        );
        batchInsert(notifications, "notifyCourseReviewResult");
    }

    /**
     * 查询当前用户的通知分页数据。
     *
     * @param userId           当前用户 ID
     * @param pageNum          页码
     * @param pageSize         每页条数
     * @param unreadOnly       是否仅查询未读
     * @param notificationType 通知类型
     * @return 通知分页结果对象
     */
    @Override
    public NotificationPageVO listCurrentUserNotifications(String userId,
                                                           Integer pageNum,
                                                           Integer pageSize,
                                                           Boolean unreadOnly,
                                                           String notificationType) {
        if (!StringUtils.hasText(userId)) {
            return new NotificationPageVO()
                    .setPageNum(DEFAULT_PAGE_NUM)
                    .setPageSize(DEFAULT_PAGE_SIZE)
                    .setTotal(0L)
                    .setUnreadCount(0L)
                    .setItems(Collections.emptyList());
        }

        int normalizedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        int offset = (normalizedPageNum - 1) * normalizedPageSize;

        List<UserNotifications> notifications = userNotificationsMapper.listByUserId(
                userId,
                offset,
                normalizedPageSize,
                unreadOnly,
                notificationType
        );
        long total = userNotificationsMapper.countByUserId(userId, unreadOnly, notificationType);
        long unreadCount = userNotificationsMapper.countUnreadByUserId(userId);

        List<NotificationItemVO> items = notifications == null
                ? Collections.emptyList()
                : notifications.stream().map(this::toItemVO).toList();

        return new NotificationPageVO()
                .setPageNum(normalizedPageNum)
                .setPageSize(normalizedPageSize)
                .setTotal(total)
                .setUnreadCount(unreadCount)
                .setItems(items);
    }

    /**
     * 获取当前用户的未读通知数。
     *
     * @param userId 当前用户 ID
     * @return 未读数
     */
    @Override
    public long countUnread(String userId) {
        if (!StringUtils.hasText(userId)) {
            return 0L;
        }
        return userNotificationsMapper.countUnreadByUserId(userId);
    }

    /**
     * 标记单条通知为已读。
     *
     * @param userId         当前用户 ID
     * @param notificationId 通知 ID
     * @return 是否成功标记
     */
    @Override
    public boolean markAsRead(String userId, String notificationId) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(notificationId)) {
            return false;
        }
        return userNotificationsMapper.markAsRead(userId, notificationId, new Date()) > 0;
    }

    /**
     * 标记当前用户全部未读通知为已读。
     *
     * @param userId 当前用户 ID
     * @return 本次更新数量
     */
    @Override
    public int markAllAsRead(String userId) {
        if (!StringUtils.hasText(userId)) {
            return 0;
        }
        return userNotificationsMapper.markAllAsRead(userId, new Date());
    }

    /**
     * 构建统一通知实体列表。
     *
     * @param recipientUserIds 接收用户 ID 集合
     * @param notificationType 通知类型
     * @param sourceType       来源类型
     * @param sourceId         来源业务 ID
     * @param title            通知标题
     * @param content          通知内容
     * @param level            通知等级
     * @param actorId          操作人 ID
     * @param courseId         关联课程 ID
     * @param relatedId        关联业务 ID
     * @param createdAt        创建时间
     * @param expiresAt        过期时间
     * @return 待写入的通知实体集合
     */
    private List<UserNotifications> buildNotifications(Collection<String> recipientUserIds,
                                                       String notificationType,
                                                       String sourceType,
                                                       String sourceId,
                                                       String title,
                                                       String content,
                                                       String level,
                                                       String actorId,
                                                       String courseId,
                                                       String relatedId,
                                                       Date createdAt,
                                                       Date expiresAt) {
        if (recipientUserIds == null || recipientUserIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> distinctUserIds = recipientUserIds.stream()
                .filter(StringUtils::hasText)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        if (distinctUserIds.isEmpty()) {
            return Collections.emptyList();
        }

        Date created = createdAt == null ? new Date() : createdAt;
        List<UserNotifications> notifications = new ArrayList<>(distinctUserIds.size());
        for (String userId : distinctUserIds) {
            notifications.add(new UserNotifications()
                    .setId(secondaryHybridIdGenerator.generateId())
                    .setUserId(userId)
                    .setNotificationType(notificationType)
                    .setSourceType(sourceType)
                    .setSourceId(sourceId)
                    .setTitle(title)
                    .setContent(content)
                    .setLevel(level)
                    .setActorId(actorId)
                    .setCourseId(courseId)
                    .setRelatedId(relatedId)
                    .setIsRead(0)
                    .setIsDeleted(0)
                    .setCreatedAt(created)
                    .setReadAt(null)
                    .setExpiresAt(expiresAt));
        }
        return notifications;
    }

    /**
     * 拼接审核说明。
     *
     * @param comment 审核说明
     * @return 拼接后的文本片段
     */
    private String appendComment(String comment) {
        if (!StringUtils.hasText(comment)) {
            return "";
        }
        return " 审核说明：" + comment.trim();
    }

    /**
     * 批量插入通知记录。
     *
     * @param notifications 待写入的通知集合
     * @param scene         当前业务场景标识
     */
    private void batchInsert(List<UserNotifications> notifications, String scene) {
        if (notifications == null || notifications.isEmpty()) {
            return;
        }
        try {
            int inserted = userNotificationsMapper.batchInsert(notifications);
            log.info("[NotificationCenter] {} 完成: requested={}, inserted={}", scene, notifications.size(), inserted);
        } catch (Exception exception) {
            log.error("[NotificationCenter] {} 写入失败，已降级忽略，避免影响主业务: requested={}, sampleRecipients={}, sampleActorIdLength={}, error={}",
                    scene,
                    notifications.size(),
                    notifications.stream()
                            .map(UserNotifications::getUserId)
                            .filter(StringUtils::hasText)
                            .limit(MAX_RECIPIENT_LOG_SIZE)
                            .toList(),
                    notifications.stream()
                            .map(UserNotifications::getActorId)
                            .filter(StringUtils::hasText)
                            .findFirst()
                            .map(String::length)
                            .orElse(0),
                    exception.getMessage(),
                    exception);
        }
    }

    /**
     * 将实体对象转换为通知项响应对象。
     *
     * @param notification 通知实体
     * @return 前端展示用通知项
     */
    private NotificationItemVO toItemVO(UserNotifications notification) {
        return new NotificationItemVO()
                .setId(notification.getId())
                .setNotificationType(notification.getNotificationType())
                .setSourceType(notification.getSourceType())
                .setSourceId(notification.getSourceId())
                .setTitle(notification.getTitle())
                .setContent(notification.getContent())
                .setLevel(notification.getLevel())
                .setActorId(notification.getActorId())
                .setCourseId(notification.getCourseId())
                .setRelatedId(notification.getRelatedId())
                .setIsRead(notification.getIsRead())
                .setCreatedAt(notification.getCreatedAt())
                .setReadAt(notification.getReadAt())
                .setExpiresAt(notification.getExpiresAt());
    }

    /**
     * 对字符串做空值兜底处理。
     *
     * @param value    原始字符串
     * @param fallback 兜底文本
     * @return 处理后的字符串
     */
    private String safeText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }
}
