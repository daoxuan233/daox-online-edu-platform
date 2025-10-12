package com.daox.online.controller.admin;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.views.responseVO.SystemAnnouncementsVo;
import com.daox.online.service.SystemAnnouncementsService;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统公告管理
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/sys")
public class SystemController {

    @Resource
    private SystemAnnouncementsService systemAnnouncementsService;

    /**
     * 获取公告列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 公告列表
     */
    @GetMapping("/notice/list")
    public RestBean<List<SystemAnnouncementsVo>> getSystemAnnouncementsList(Integer pageNum, Integer pageSize) {
        List<SystemAnnouncementsVo> systemAnnouncements = systemAnnouncementsService.getSystemAnnouncements(pageNum, pageSize);
        if (systemAnnouncements == null) {
            return RestBean.failure(404, "系统公告不存在!");
        }
        return RestBean.success(systemAnnouncements);
    }

    /**
     * 发布系统公告
     *
     * @param title     标题
     * @param content   内容
     * @param isActive  是否激活 [0-激活 , 1-未激活]
     * @param cratedAt  创建时间
     * @param expiredAt 失效时间
     * @param request   请求
     * @return 新公告信息
     */
    @PostMapping("/notice/publish")
    public RestBean<SystemAnnouncementsVo> publishSystemAnnouncement(String title, String content, String isActive, LocalDateTime cratedAt, LocalDateTime expiredAt, HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[publishSystemAnnouncement.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        SystemAnnouncementsVo systemAnnouncementsVo = systemAnnouncementsService.publishSystemAnnouncement(userId, title, content, Integer.parseInt(isActive), cratedAt, expiredAt);
        if (systemAnnouncementsVo == null) {
            return RestBean.failure(500, "发布系统公告失败!");
        }
        return RestBean.success(systemAnnouncementsVo);
    }

    /**
     * 更新公告
     *
     * @param id        公告id
     * @param title     标题
     * @param content   内容
     * @param isActive  状态
     * @param expiredAt 过期时间
     * @return 更新后的公告
     */
    @PostMapping("/update")
    public RestBean<SystemAnnouncementsVo> updateSystemAnnouncement(@RequestParam("id") String id, String title, String content, Integer isActive, LocalDateTime expiredAt) {
        SystemAnnouncementsVo systemAnnouncementsVo = systemAnnouncementsService.updateSystemAnnouncement(id, title, content, isActive, expiredAt);
        if (systemAnnouncementsVo == null) {
            return RestBean.failure(500, "更新系统公告失败!");
        }
        return RestBean.success(systemAnnouncementsVo);
    }

    /**
     * 删除公告 - 本质是过期公告
     *
     * @param id 公告id
     * @return 是否成功
     */
    @PostMapping("/deleteSystemAnnouncement")
    public RestBean<String> deleteSystemAnnouncement(@RequestBody String id) {
        Boolean aBoolean = systemAnnouncementsService.deleteSystemAnnouncement(id);
        if (!aBoolean) {
            return RestBean.failure(500, "删除系统公告失败!");
        }
        return RestBean.success("删除系统公告成功!");
    }
}

