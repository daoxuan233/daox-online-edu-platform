package com.daox.online.service.Impl;

import com.daox.online.entity.views.responseVO.admin.AdminCategoryOperationPreviewVO;
import com.daox.online.service.CategoryNotificationService;
import com.daox.online.uilts.constant.Const;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 课程分类操作邮件通知服务实现类。
 *
 * <p>所有公开通知方法均标注 {@code @Async}，在独立线程池中异步执行，
 * 不阻塞、不干扰主业务事务的提交或回滚。</p>
 *
 * <p>邮件发送采用 <b>「直连优先��队列兜底」</b> 策略：</p>
 * <ol>
 *   <li>优先通过 {@link JavaMailSender} 直接经 SMTP 发送邮件；</li>
 *   <li>若 SMTP 发送失败，则将邮件任务投递至 RabbitMQ {@code mail} 队列，由监听器重试；</li>
 *   <li>若队列投递也失败，仅记录错误日志，<b>不向调用方抛出异常</b>，
 *       保证异步任务即使遭遇邮件故障也不会崩溃。</li>
 * </ol>
 *
 * @author DaoX Online Platform
 * @version 1.1
 * @see CategoryNotificationService
 */
@Service
@Slf4j
public class CategoryNotificationServiceImpl implements CategoryNotificationService {

    /** SMTP 邮件发送器，用于直连发送 */
    @Resource
    private JavaMailSender mailSender;

    /** RabbitMQ 模板，邮件直连失败时用于投递兜底任务 */
    @Resource
    private AmqpTemplate amqpTemplate;

    /** 发件人地址，从配置文件读取 */
    @Value("${spring.mail.username}")
    private String mailFromAddress;

    // ==================== 公开接口实现 ====================

    /**
     * 异步发送分类紧急删除邮件通知。
     *
     * <p>遍历受影响课程列表，去重后为每位教师发送一封紧急通知邮件，
     * 告知其所属课程分类已立即删除，并说明兜底分类信息（如有）。</p>
     *
     * @param categoryName       被紧急删除的分类名称
     * @param affectedCourses    受影响的课程摘要列表（含教师邮箱）
     * @param fallbackCategoryName 兜底分类名称，为 {@code null} 表示无需临时迁移
     */
    @Async
    @Override
    public void sendEmergencyDeleteEmails(String categoryName,
                                          List<AdminCategoryOperationPreviewVO.AffectedCourseItem> affectedCourses,
                                          String fallbackCategoryName) {
        if (affectedCourses == null || affectedCourses.isEmpty()) {
            log.info("[CategoryNotification] 紧急删除无受影响课程，跳过邮件通知: categoryName={}", categoryName);
            return;
        }

        // 兜底分类提示内容：有课程被迁移时提供具体说明，否则给出通用提示
        String fallbackHint = StringUtils.hasText(fallbackCategoryName)
                ? "\n您名下受影响的课程已被系统临时迁入「" + fallbackCategoryName
                + "」分类，请尽快重新选择正式分类。"
                : "\n请立即登录管理后台，检查并调整您名下相关课程的分类设置。";

        String subject = "【紧急通知】课程分类「" + categoryName + "」已删除，请立即更新课程分类";
        String content = buildEmailContent(
                "管理员已对课程分类「" + categoryName + "」执行了紧急删除操作，该分类现已从平台移除。"
                        + fallbackHint
                        + "\n\n请登录后台管理系统，及时完成课程分类的重新选择，以免影响课程正常展示和学生访问。"
        );

        int successCount = batchSendToTeachers(affectedCourses, subject, content);
        log.info("[CategoryNotification] 紧急删除邮件通知完成: categoryName={}, 成功通知教师数={}",
                categoryName, successCount);
    }

    /**
     * 异步发送分类常规删除申请邮件通知。
     *
     * <p>常规删除申请已提交，分类进入三天保留期，
     * 邮件中说明截止时间并告知教师需完成的操作步骤。</p>
     *
     * @param categoryName    被申请删除的分类名称
     * @param preserveUntil  保留期截止时间
     * @param affectedCourses 受影响的课程摘要列表（含教师邮箱）
     */
    @Async
    @Override
    public void sendRegularDeleteRequestEmails(String categoryName,
                                               Date preserveUntil,
                                               List<AdminCategoryOperationPreviewVO.AffectedCourseItem> affectedCourses) {
        if (affectedCourses == null || affectedCourses.isEmpty()) {
            log.info("[CategoryNotification] 常规删除申请无受影响课程，跳过邮件通知: categoryName={}", categoryName);
            return;
        }

        // 格式化截止时间，兜底展示文字
        String deadline = preserveUntil != null
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(preserveUntil)
                : "三天后";

        String subject = "【重要提醒】课程分类「" + categoryName + "」即将删除，请在保留期内完成调整";
        String content = buildEmailContent(
                "管理员已提交课程分类「" + categoryName + "」的常规删除申请，该分类将于 "
                        + deadline + " 正式删除（保留期结束或相关教师全部确认后执行）。\n\n"
                        + "请在截止日期前完成以下操作：\n"
                        + "  1. 登录后台管理系统；\n"
                        + "  2. 将您名下属于该分类的课程重新归类至其他分类；\n"
                        + "  3. 在系统待办中完成「分类删除确认」任务。\n\n"
                        + "若截止时间内未完成处理，系统将自动将您的课程迁入系统保留分类，请及时关注。"
        );

        int successCount = batchSendToTeachers(affectedCourses, subject, content);
        log.info("[CategoryNotification] 常规删除申请邮件通知完成: categoryName={}, 截止时间={}, 成功通知教师数={}",
                categoryName, deadline, successCount);
    }

    /**
     * 异步发送分类常规删除最终执行邮件通知。
     *
     * <p>三天保留期已满（或相关教师全部确认），系统已自动完成分类删除，
     * 通知教师分类已正式移除，请处理尚未重新分类的课程。</p>
     *
     * @param categoryName    已正式删除的分类名称
     * @param affectedCourses 受影响的课程摘要列表（含教师邮箱）
     */
    @Async
    @Override
    public void sendRegularDeleteFinalizedEmails(String categoryName,
                                                 List<AdminCategoryOperationPreviewVO.AffectedCourseItem> affectedCourses) {
        if (affectedCourses == null || affectedCourses.isEmpty()) {
            log.info("[CategoryNotification] 常规删除执行无受影响教师，跳过邮件通知: categoryName={}", categoryName);
            return;
        }

        String subject = "【通知】课程分类「" + categoryName + "」已正式删除";
        String content = buildEmailContent(
                "课程分类「" + categoryName + "」的三天保留期已结束（或相关教师均已确认），"
                        + "该分类现已正式从平台移除。\n\n"
                        + "如您尚未完成课程分类调整，请立即登录后台管理系统重新选择分类，"
                        + "以免影响课程的正常展示和学生访问。"
        );

        int successCount = batchSendToTeachers(affectedCourses, subject, content);
        log.info("[CategoryNotification] 常规删除执行邮件通知完成: categoryName={}, 成功通知教师数={}",
                categoryName, successCount);
    }

    /**
     * 异步发送分类迁移完成邮件通知。
     *
     * <p>分类迁移已执行，课程所属分类已从源分类迁移至目标分类，
     * 告知教师及时核对课程分类是否符合预期。</p>
     *
     * @param sourceCategoryName 原（迁出）分类名称
     * @param targetCategoryName 目标（迁入）分类名称
     * @param affectedCourses    受影响的课程摘要列表（含教师邮箱）
     */
    @Async
    @Override
    public void sendMigrationEmails(String sourceCategoryName,
                                    String targetCategoryName,
                                    List<AdminCategoryOperationPreviewVO.AffectedCourseItem> affectedCourses) {
        if (affectedCourses == null || affectedCourses.isEmpty()) {
            log.info("[CategoryNotification] 分类迁移无受影响课程，跳过邮件通知: source={}", sourceCategoryName);
            return;
        }

        String subject = "【通知】您的课程分类已由管理员迁移：「" + sourceCategoryName + "」→「" + targetCategoryName + "」";
        String content = buildEmailContent(
                "管理员已将课程分类「" + sourceCategoryName + "」下的课程迁移至「"
                        + targetCategoryName + "」。\n\n"
                        + "请登录后台管理系统核对您名下课程的分类设置，确认无误后无需额外操作。\n"
                        + "若对本次迁移有异议，请及时联系平台管理员处理。"
        );

        int successCount = batchSendToTeachers(affectedCourses, subject, content);
        log.info("[CategoryNotification] 分类迁移邮件通知完成: source={}, target={}, 成功通知教师数={}",
                sourceCategoryName, targetCategoryName, successCount);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 批量向受影响课程的教师发送通知邮件（自动去重，每位教师只发送一封）。
     *
     * @param affectedCourses 受影响的课程摘要列表
     * @param subject         邮件主题
     * @param content         邮件正文
     * @return 实际成功投递（直连或队列）的教师邮件数量
     */
    private int batchSendToTeachers(List<AdminCategoryOperationPreviewVO.AffectedCourseItem> affectedCourses,
                                    String subject,
                                    String content) {
        // 使用 Set 对教师邮箱去重，避免同一教师收到多封重复邮件
        Set<String> sentEmails = new HashSet<>();
        int successCount = 0;

        for (AdminCategoryOperationPreviewVO.AffectedCourseItem item : affectedCourses) {
            // 跳过无效邮箱或已发送过的邮箱
            if (!StringUtils.hasText(item.getTeacherEmail())
                    || !sentEmails.add(item.getTeacherEmail())) {
                continue;
            }
            doSendMail(item.getTeacherEmail(), subject, content);
            successCount++;
        }
        return successCount;
    }

    /**
     * 统一邮件正文模板构建。
     *
     * <p>添加问候语前缀和平台签名后缀，保持所有分类通知邮件风格一致。</p>
     *
     * @param body 邮件主体内容（不含问候和签名）
     * @return 完整邮件正文
     */
    private String buildEmailContent(String body) {
        return "【DaoX 在线教育平台通知】\n\n"
                + "尊敬的老师，您好！\n\n"
                + body
                + "\n\n如有任何疑问，请联系平台管理员。\n\n"
                + "此邮件由系统自动发送，请勿直接回复。\n"
                + "DaoX 在线教育平台";
    }

    /**
     * 「直连优先，队列兜底」核心发信方法。
     *
     * <p>执行顺序：</p>
     * <ol>
     *   <li>尝试通过 SMTP 直接发送；若成功则结束。</li>
     *   <li>若 SMTP 失败，将邮件任务以 {@code type=custom} 格式投递至
     *       RabbitMQ {@code mail} 队列（由 {@code MailQueueListener} 消费处理）。</li>
     *   <li>若队列投递也失败，仅记录错误日志，<b>不抛出任何异常</b>，
     *       避免异步任务崩溃影响其他通知的发送。</li>
     * </ol>
     *
     * @param email   收件人邮箱地址
     * @param subject 邮件主题
     * @param content 邮件正文
     */
    private void doSendMail(String email, String subject, String content) {
        // ---- 第一步：SMTP 直连发送 ----
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFromAddress);
            message.setTo(email);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("[CategoryNotification] 邮件 SMTP 直连发送成功: email={}", email);
            return; // 直连成功，直接返回
        } catch (Exception smtpEx) {
            log.warn("[CategoryNotification] 邮件 SMTP 直连失败，转投消息队列: email={}, error={}",
                    email, smtpEx.getMessage());
        }

        // ---- 第二步：RabbitMQ 队列兜底 ----
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "custom");
            payload.put("email", email);
            payload.put("subject", subject);
            payload.put("content", content);
            amqpTemplate.convertAndSend(Const.MQ_MAIL, payload);
            log.info("[CategoryNotification] 邮件已投递至消息队列: email={}", email);
        } catch (Exception mqEx) {
            // 两种方式均失败：只记录日志，不抛出异常，保证异步任务的健壮性
            log.error("[CategoryNotification] 邮件消息队列投递也失败，本次通知丢失: email={}, error={}",
                    email, mqEx.getMessage());
        }
    }
}

