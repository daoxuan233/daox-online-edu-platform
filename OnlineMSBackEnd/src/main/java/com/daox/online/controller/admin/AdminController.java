package com.daox.online.controller.admin;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.mysql.AuditLogs;
import com.daox.online.entity.views.requestVO.admin.AdminUserUpsertVO;
import com.daox.online.entity.views.responseVO.user.ProfileInfoVo;
import com.daox.online.service.AuditLogService;
import com.daox.online.service.UsersService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员控制层
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Resource
    private UsersService userService;

    @Resource
    private AuditLogService auditLogService;

    /**
     * 获取用户列表
     *
     * @return 用户列表
     */
    @GetMapping("/users")
    public RestBean<List<ProfileInfoVo>> getUserList() {
        List<ProfileInfoVo> userListNotAdmin = userService.getUserListNotAdmin();
        if (userListNotAdmin == null || userListNotAdmin.isEmpty()) {
            log.warn("[getUserList.method]用户列表为空");
            return RestBean.failure(500, "用户列表为空");
        }
        return RestBean.success(userListNotAdmin);
    }

    /**
     * 获取用户详情 - 通过ID获取
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/user/detail")
    public RestBean<ProfileInfoVo> getUserDetail(String userId) {
        ProfileInfoVo userDetail = userService.getUserDetail(userId);
        if (userDetail == null) {
            log.warn("[getUserDetail.method]用户不存在");
            return RestBean.failure(500, "用户不存在");
        }
        return RestBean.success(userDetail);
    }

    /**
     * 删除用户/逻辑更改状态
     *
     * @param userId 用户ID
     * @return 被删除的用户信息
     */
    @PostMapping("/user/status")
    public RestBean<ProfileInfoVo> updateUserStatus(String userId) {
        ProfileInfoVo profileInfoVo = userService.deleteUser(userId);
        if (profileInfoVo == null) {
            log.warn("[updateUserStatus.method]用户不存在");
            return RestBean.failure(500, "用户不存在");
        }
        return RestBean.success(profileInfoVo);
    }

    /**
     * 恢复被禁用的用户。
     *
     * @param userId 用户ID
     * @return 恢复后的用户信息
     */
    @PostMapping("/user/restore")
    public RestBean<ProfileInfoVo> restoreUser(@RequestParam String userId) {
        ProfileInfoVo profileInfoVo = userService.restoreUser(userId);
        if (profileInfoVo == null) {
            log.warn("[restoreUser.method]恢复用户失败");
            return RestBean.failure(500, "恢复用户失败");
        }
        return RestBean.success(profileInfoVo);
    }

    /**
     * 管理员创建用户。
     *
     * @param userUpsertVO 用户创建请求体
     * @return 创建后的用户信息
     */
    @PostMapping("/user/create")
    public RestBean<ProfileInfoVo> createUser(@RequestBody AdminUserUpsertVO userUpsertVO) {
        ProfileInfoVo profileInfoVo = userService.createUserByAdmin(userUpsertVO);
        if (profileInfoVo == null) {
            log.warn("[createUser.method]创建用户失败");
            return RestBean.failure(500, "创建用户失败，请检查标识、邮箱或昵称是否冲突");
        }
        return RestBean.success(profileInfoVo);
    }

    /**
     * 管理员更新用户基础信息。
     *
     * @param userId       用户ID
     * @param userUpsertVO 用户更新请求体
     * @return 更新后的用户信息
     */
    @PostMapping("/user/update")
    public RestBean<ProfileInfoVo> updateUser(@RequestParam String userId,
                                              @RequestBody AdminUserUpsertVO userUpsertVO) {
        ProfileInfoVo profileInfoVo = userService.updateUserByAdmin(userId, userUpsertVO);
        if (profileInfoVo == null) {
            log.warn("[updateUser.method]更新用户失败");
            return RestBean.failure(500, "更新用户失败，请检查标识、邮箱或昵称是否冲突");
        }
        return RestBean.success(profileInfoVo);
    }

    /**
     * 查询统一审计日志列表。
     *
     * @param keyword      关键字
     * @param action       操作动作编码
     * @param status       执行状态
     * @param operatorRole 操作者角色
     * @param startDate    开始日期，格式 yyyy-MM-dd
     * @param endDate      结束日期，格式 yyyy-MM-dd
     * @return 审计日志列表
     */
    @GetMapping("/audit-logs")
    public RestBean<List<AuditLogs>> getAuditLogs(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String action,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String operatorRole,
                                                  @RequestParam(required = false) String startDate,
                                                  @RequestParam(required = false) String endDate) {
        List<AuditLogs> auditLogs = auditLogService.listAuditLogs(keyword, action, status, operatorRole, startDate, endDate);
        return RestBean.success(auditLogs);
    }

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @return 重置的新密码
     */
    @PostMapping("/user/password")
    public RestBean<String> resetUserPassword(String userId) {
        String password = userService.resetUserPassword(userId);
        if (password == null) {
            log.warn("[resetUserPassword.method]用户不存在");
            return RestBean.failure(500, "用户不存在");
        }
        return RestBean.success("重置密码成功，新密码为：" + password);
    }


}
