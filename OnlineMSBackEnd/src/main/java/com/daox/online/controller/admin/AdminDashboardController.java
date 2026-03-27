package com.daox.online.controller.admin;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.views.responseVO.admin.AdminDashboardOverviewVO;
import com.daox.online.service.AdminDashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员首页概览接口。
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Resource
    private AdminDashboardService adminDashboardService;

    /**
     * 获取管理员首页概览数据。
     *
     * @param days 趋势统计天数，默认 30 天
     * @return 首页概览数据
     */
    @GetMapping("/overview")
    public RestBean<AdminDashboardOverviewVO> getDashboardOverview(@RequestParam(defaultValue = "30") Integer days) {
        return RestBean.success(adminDashboardService.getDashboardOverview(days));
    }
}
