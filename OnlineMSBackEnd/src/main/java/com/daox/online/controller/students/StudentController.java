package com.daox.online.controller.students;

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
 * 学生相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Resource
    private UsersService usersService;

    @Resource
    private SysUsersMapper sysUsersMapper;

    /**
     * 获取个人信息
     *
     * @param identifier 学号/工号
     * @return 个人信息
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
     * @param request 请求
     * @param avatarUrl 头像url
     * @return 修改结果
     */
    @PostMapping("/avatar")
    public RestBean<String> uploadAvatar(@RequestParam("avatarUrl") String avatarUrl, HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null){
            log.warn("[uploadAvatar.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        log.info("修改头像：{}", avatarUrl);
        int i = sysUsersMapper.updateAvatar(userId, avatarUrl);
        if (i > 0) return RestBean.success("修改成功！");
        return RestBean.failure(500, "修改失败！");
    }

    /**
     * 更新个人资料
     * @param profileVo 个人信息
     * @return 个人信息
     */
    @PostMapping("/update")
    public RestBean<ProfileVo> updateProfile(@RequestBody ProfileVo profileVo) {
        if (profileVo == null){
            log.warn("[updateProfile.method]参数为空");
            return RestBean.failure(400, "参数为空！");
        }
        ProfileVo profileVo1 = usersService.updateProfile(profileVo);
        if (profileVo1 == null) return RestBean.failure(500, "修改失败！");
        return RestBean.success(profileVo1);
    }

}
