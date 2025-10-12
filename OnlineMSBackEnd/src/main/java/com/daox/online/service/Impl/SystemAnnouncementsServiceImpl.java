package com.daox.online.service.Impl;

import com.daox.online.entity.mysql.SystemAnnouncements;
import com.daox.online.entity.mysql.Users;
import com.daox.online.entity.views.responseVO.SystemAnnouncementsVo;
import com.daox.online.mapper.SystemAnnouncementsMapper;
import com.daox.online.service.SysUserService;
import com.daox.online.service.SystemAnnouncementsService;
import com.daox.online.uilts.DateUtils;
import com.daox.online.uilts.SecondaryHybridIdGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.time.LocalDateTime.*;

@Service
@Slf4j
public class SystemAnnouncementsServiceImpl implements SystemAnnouncementsService {

    @Resource
    private SystemAnnouncementsMapper systemAnnouncementsMapper;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private SecondaryHybridIdGenerator secondaryHybridIdGenerator;

    /**
     * 获取系统公告
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 系统公告
     */
    @Override
    public List<SystemAnnouncementsVo> getSystemAnnouncements(int pageNum, int pageSize) {
        if (pageNum < 1 || pageSize < 1) {
            log.error("[getSystemAnnouncements.method]参数错误!");
            // 设为默认值
            pageNum = 1;
            pageSize = 10;
        }
        List<SystemAnnouncements> systemAnnouncements = systemAnnouncementsMapper.getSystemAnnouncements(pageNum, pageSize);
        if (systemAnnouncements == null || systemAnnouncements.isEmpty()) {
            log.error("[getSystemAnnouncements.method]获取系统公告失败!");
            return Collections.emptyList();
        }
        List<SystemAnnouncementsVo> systemAnnouncementsVos = new ArrayList<>();
        for (SystemAnnouncements systemAnnouncement : systemAnnouncements) {
            Users userById = sysUserService.findUserById(systemAnnouncement.getCreatorId());
            if (userById == null) {
                log.error("[getSystemAnnouncements.method]查询创建者信息失败！创建者ID: {}", systemAnnouncement.getCreatorId());
                continue;
            }

            SystemAnnouncementsVo vo = new SystemAnnouncementsVo()
                    .setId(systemAnnouncement.getId()).setTitle(systemAnnouncement.getTitle())
                    .setContent(systemAnnouncement.getContent()).setCreatorId(systemAnnouncement.getCreatorId())
                    .setIsActive(systemAnnouncement.getIsActive()).setCreatedAt(systemAnnouncement.getCreatedAt())
                    .setCreatorNickname(userById.getNickname()).setExpiredAt(systemAnnouncement.getExpiredAt());

            systemAnnouncementsVos.add(vo);
        }

        return systemAnnouncementsVos;
    }

    /**
     * 发布系统公告
     *
     * @param userId   用户id
     * @param title    标题
     * @param content  内容
     * @param isActive 是否激活
     * @param cratedAt 创建时间
     * @return 是否成功
     */
    @Override
    public SystemAnnouncementsVo publishSystemAnnouncement(String userId, String title, String content, Integer isActive, LocalDateTime cratedAt, LocalDateTime expiredAt) {
        if (userId == null || title == null || content == null) {
            log.warn("[publishSystemAnnouncement.method]参数错误: userId={}, title={}, content={}", userId, title, content);
            return null;
        }
        Users userById = sysUserService.findUserById(userId);
        if (userById == null) {
            log.warn("[publishSystemAnnouncement.method]用户不存在");
            return null;
        }
        String id = secondaryHybridIdGenerator.generateId();
        Date createdAtDate = expiredAt != null ? Date.from(cratedAt.atZone(ZoneId.systemDefault()).toInstant()) : null;
        Date expiredAtDate = expiredAt != null ? Date.from(expiredAt.atZone(ZoneId.systemDefault()).toInstant()) : null;
        SystemAnnouncements systemAnnouncements = new SystemAnnouncements()
                .setId(id).setTitle(title)
                .setContent(content)
                .setCreatorId(userId)
                .setIsActive(isActive)
                .setCreatedAt(createdAtDate)
                .setExpiredAt(expiredAtDate);
        int result = systemAnnouncementsMapper.publishSystemAnnouncement(systemAnnouncements);
        if (result > 0) {
            return new SystemAnnouncementsVo()
                    .setId(id)
                    .setTitle(title)
                    .setContent(content)
                    .setCreatorId(userId)
                    .setCreatorNickname(userById.getNickname())
                    .setIsActive(isActive)
                    .setCreatedAt(createdAtDate)
                    .setExpiredAt(expiredAtDate);
        }
        return null;
    }

    /**
     * 更新公告
     *
     * @param id        公告id
     * @param title     标题
     * @param content   内容
     * @param isActive  是否激活
     * @param expiredAt 失效时间
     * @return 是否成功 - 成功后的公告
     */
    @Override
    public SystemAnnouncementsVo updateSystemAnnouncement(String id, String title, String content, Integer isActive, LocalDateTime expiredAt) {
        if (id == null || title == null || content == null) {
            log.warn("[updateSystemAnnouncement.method]参数错误");
            return null;
        }
        SystemAnnouncements systemAnnouncementById = systemAnnouncementsMapper.getSystemAnnouncementById(id);
        if (systemAnnouncementById == null) {
            log.warn("[updateSystemAnnouncement.method]系统公告不存在");
            return null;
        }
        if (systemAnnouncementById.getIsActive() == 1) {
            log.warn("[updateSystemAnnouncement.method]系统公告已过期");
            return null;
        }
        if (expiredAt == null) {
            expiredAt = DateUtils.convertToLocalDateTime(systemAnnouncementById.getExpiredAt());
        }
        if (expiredAt.isBefore(LocalDateTime.now())) {
            log.warn("[updateSystemAnnouncement.method]系统公告已过期");
            int i = systemAnnouncementsMapper.updateExpiredAnnouncements(now());
            log.info("[updateSystemAnnouncement.method]系统公告已过期，更新了 {} 条公告", i);
            return null;
        }
        Users userById = sysUserService.findUserById(systemAnnouncementById.getCreatorId());
        if (userById == null) {
            log.warn("[updateSystemAnnouncement.method]用户不存在{}", systemAnnouncementById.getCreatorId());
            return null;
        }
        Date expiredAtDate = Date.from(expiredAt.atZone(ZoneId.systemDefault()).toInstant());
        int i = systemAnnouncementsMapper.updateSystemAnnouncement(id, title, content, isActive, expiredAtDate);
        if (i > 0) {
            log.info("[updateSystemAnnouncement.method]更新了 {} 条公告", i);
            return new SystemAnnouncementsVo()
                    .setId(id)
                    .setTitle(title)
                    .setContent(content)
                    .setCreatorId(userById.getId())
                    .setCreatorNickname(userById.getNickname())
                    .setIsActive(isActive)
                    .setCreatedAt(systemAnnouncementById.getCreatedAt())
                    .setExpiredAt(expiredAtDate);
        }
        log.info("[updateSystemAnnouncement.method]更新失败,i={}", i);
        return null;
    }

    /**
     * 删除公告 - 本质是过期公告
     *
     * @param id 公告id
     * @return 是否成功
     */
    @Override
    public Boolean deleteSystemAnnouncement(String id) {
        int i = systemAnnouncementsMapper.deleteSystemAnnouncement(id);
        return i > 0;
    }
}
