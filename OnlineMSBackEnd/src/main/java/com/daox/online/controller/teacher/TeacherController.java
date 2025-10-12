package com.daox.online.controller.teacher;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.views.responseVO.user.ProfileVo;
import com.daox.online.mapper.SysUsersMapper;
import com.daox.online.service.UsersService;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 教师端 - 个人信息相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Resource
    private UsersService usersService;

    @Resource
    private SysUsersMapper sysUsersMapper;

    /**
     * 获取教师信息
     *
     * @param identifier 教师工号
     * @return 教师信息
     */
    @GetMapping("/profile")
    public RestBean<ProfileVo> getProfile(@RequestParam("identifier") String identifier) {
        ProfileVo profile = usersService.getProfile(identifier);
        if (profile == null) return RestBean.failure(404, "查询错误！");
        return RestBean.success(profile);
    }

    /**
     * 修改头像 - 调用public.api --> 上传url地址
     *
     * @param avatarUrl 头像url
     * @return 修改结果
     */
    @PostMapping("/avatar")
    public RestBean<String> uploadAvatar(@RequestParam("avatarUrl") String avatarUrl, HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[uploadAvatar.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        int i = sysUsersMapper.updateAvatar(userId, avatarUrl);
        if (i > 0) return RestBean.success("修改成功！");
        return RestBean.failure(500, "修改失败！");
    }

}
