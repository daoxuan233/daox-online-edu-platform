package com.daox.online.service;

import com.daox.online.entity.views.responseVO.admin.AdminDashboardOverviewVO;

/**
 * 管理员首页概览服务。
 */
public interface AdminDashboardService {

    /**
     * 获取管理员首页概览数据。
     *
     * @param days 趋势统计天数，支持 7 / 30 / 90
     * @return 管理员首页概览数据
     */
    AdminDashboardOverviewVO getDashboardOverview(int days);
}
