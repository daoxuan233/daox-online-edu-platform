package com.daox.online.service;

import com.daox.online.entity.views.responseVO.admin.AdminCategoryOperationPreviewVO;

import java.util.Date;
import java.util.List;

/**
 * 课程分类操作邮件通知服务接口。
 *
 * <p>负责在分类删除（紧急/常规）、迁移等高风险操作时，
 * 向受影响的教师发送邮件通知，确保教师能及时感知并处理课程分类变更。</p>
 *
 * <p>所有方法均为 <b>异步执行</b>（通过 {@code @Async} 注解），不阻塞、不干扰主业务事务。
 * 调用方应在事务提交后（通过 {@code TransactionSynchronizationManager.registerSynchronization}
 * 的 {@code afterCommit} 回调）触发这些方法，以保证：</p>
 * <ol>
 *   <li>仅在删除/迁移操作真正持久化后才发送通知；</li>
 *   <li>邮件发送失败不会引起主业务事务回滚。</li>
 * </ol>
 *
 * @author DaoX Online Platform
 * @version 1.1
 */
public interface CategoryNotificationService {

    /**
     * 异步发送分类紧急删除邮件通知给受影响的教师。
     *
     * <p>紧急删除已立即生效，需告知教师分类已被删除，请尽快调整课程分类。
     * 若存在兜底分类（{@code fallbackCategoryName} 不为空），则在邮件中说明
     * 受影响课程已被系统临时迁入兜底分类。</p>
     *
     * @param categoryName       被删除的分类名称
     * @param affectedCourses    受影响的课程摘要列表（包含教师邮箱等关键信息）
     * @param fallbackCategoryName 兜底分类名称；若为 {@code null} 则说明无课程需要临时迁移
     */
    void sendEmergencyDeleteEmails(String categoryName,
                                   List<AdminCategoryOperationPreviewVO.AffectedCourseItem> affectedCourses,
                                   String fallbackCategoryName);

    /**
     * 异步发送分类常规删除申请邮件通知给受影响的教师。
     *
     * <p>常规删除申请已提交，分类进入三天保留期，教师须在截止日前完成
     * 课程分类调整并确认系统待办，否则系统将自动执行删除并迁移残留课程。</p>
     *
     * @param categoryName    被申请删除的分类名称
     * @param preserveUntil  三天保留期截止时间
     * @param affectedCourses 受影响的课程摘要列表（包含教师邮箱等关键信息）
     */
    void sendRegularDeleteRequestEmails(String categoryName,
                                        Date preserveUntil,
                                        List<AdminCategoryOperationPreviewVO.AffectedCourseItem> affectedCourses);

    /**
     * 异步发送分类常规删除最终执行邮件通知给受影响的教师。
     *
     * <p>三天保留期已结束（或相关教师全部确认），系统已自动完成分类删除，
     * 通知教师尽快处理尚未重新分类的课程。</p>
     *
     * @param categoryName    已正式删除的分类名称
     * @param affectedCourses 受影响的课程摘要列表（包含教师邮箱等关键信息）
     */
    void sendRegularDeleteFinalizedEmails(String categoryName,
                                          List<AdminCategoryOperationPreviewVO.AffectedCourseItem> affectedCourses);

    /**
     * 异步发送分类迁移完成邮件通知给受影响的教师。
     *
     * <p>分类迁移已执行完毕，课程所属分类已发生变更，告知教师及时核对。</p>
     *
     * @param sourceCategoryName 原（迁出）分类名称
     * @param targetCategoryName 目标（迁入）分类名称
     * @param affectedCourses    受影响的课程摘要列表（包含教师邮箱等关键信息）
     */
    void sendMigrationEmails(String sourceCategoryName,
                             String targetCategoryName,
                             List<AdminCategoryOperationPreviewVO.AffectedCourseItem> affectedCourses);
}

