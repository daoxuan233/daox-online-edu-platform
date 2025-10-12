package com.daox.online.service;

import com.daox.online.entity.views.responseVO.SystemAnnouncementsVo;

import java.time.LocalDateTime;
import java.util.List;

public interface SystemAnnouncementsService {
    /**
     * 获取系统公告
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 系统公告
     */
    List<SystemAnnouncementsVo> getSystemAnnouncements(int pageNum, int pageSize);

    /**
     * 发布系统公告
     *
     * @param userId    用户id
     * @param title     标题
     * @param content   内容
     * @param isActive  是否激活
     * @param cratedAt  创建时间
     * @param expiredAt 失效时间
     * @return 是否成功
     */
    SystemAnnouncementsVo publishSystemAnnouncement(String userId, String title, String content, Integer isActive, LocalDateTime cratedAt, LocalDateTime expiredAt);

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
    SystemAnnouncementsVo updateSystemAnnouncement(String id, String title, String content, Integer isActive, LocalDateTime expiredAt);

    /**
     * 删除公告 - 本质是过期公告
     *
     * @param id 公告id
     * @return 是否成功
     */
    Boolean deleteSystemAnnouncement(String id);
}
