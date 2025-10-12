package com.daox.online.controller.admin;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.views.responseVO.user.ProfileInfoVo;
import com.daox.online.service.UsersService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
