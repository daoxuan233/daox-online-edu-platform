package com.daox.online.service;

import com.daox.online.entity.views.responseVO.user.ProfileInfoVo;
import com.daox.online.entity.views.responseVO.user.ProfileVo;

import java.util.List;

public interface UsersService {
    /**
     * 获取个人信息
     *
     * @param identifier 学号/工号
     * @return 个人信息
     */
    ProfileVo getProfile(String identifier);

    /**
     * 更新个人资料 - 学生
     * @param profileVo 个人信息
     * @return 个人信息
     */
    ProfileVo updateProfile(ProfileVo profileVo);

    /**
     * 判定用户角色 - 学生
     *
     * @param userId 用户ID
     * @return true: 是学生
     */
    boolean isStudent(String userId);

    /**
     * 判定用户角色 - 教师
     *
     * @param userId 用户ID
     * @return true: 是教师
     */
    boolean isTeacher(String userId);

    /**
     * 判定用户角色 - 管理员
     *
     * @param userId 用户ID
     * @return true: 是管理员
     */
    boolean isAdmin(String userId);

    /**
     * 获取用户列表 - 无Admin
     *
     * @return 用户列表
     */
    List<ProfileInfoVo> getUserListNotAdmin();

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    ProfileInfoVo getUserDetail(String userId);

    /**
     * 删除用户 - 逻辑删除
     *
     * @param userId 用户ID
     * @return 被删除的用户信息
     */
    ProfileInfoVo deleteUser(String userId);

    /**
     * 重置用户密码 - admin重置
     *
     * @param userId 用户ID
     * @return 新密码
     */
    String resetUserPassword(String userId);

    /**
     * 获取用户总数
     *
     * @return 用户总数
     */
    int getUserCount();

    /**
     * 获取管理员总数
     *
     * @return 管理员总数
     */
    int getAdminCount();

    /**
     * 获取学生总数
     *
     * @return 学生总数
     */
    int getStudentCount();

    /**
     * 获取教师总数
     *
     * @return 教师总数
     */
    int getTeacherCount();

    /**
     * 获取用户统计 - 有效数据
     *
     * @return 用户统计
     */
    int getUserCountValid();

    /**
     * 获取管理员统计 - 有效数据
     *
     * @return 管理员统计
     */
    int getAdminCountValid();

    /**
     * 获取学生统计 - 有效数据
     *
     * @return 学生统计
     */
    int getStudentCountValid();

    /**
     * 获取教师统计 - 有效数据
     *
     * @return 教师统计
     */
    int getTeacherCountValid();
}
