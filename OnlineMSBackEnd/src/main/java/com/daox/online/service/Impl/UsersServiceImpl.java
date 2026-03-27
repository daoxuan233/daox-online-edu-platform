package com.daox.online.service.Impl;

import com.daox.online.entity.mysql.UserDescribe;
import com.daox.online.entity.mysql.Users;
import com.daox.online.entity.views.requestVO.admin.AdminUserUpsertVO;
import com.daox.online.entity.views.responseVO.user.ProfileInfoVo;
import com.daox.online.entity.views.responseVO.user.ProfileVo;
import com.daox.online.mapper.SysUsersMapper;
import com.daox.online.service.AuditLogService;
import com.daox.online.service.SysUserService;
import com.daox.online.service.UsersService;
import com.daox.online.uilts.constant.Const;
import com.daox.online.uilts.DateUtils;
import com.daox.online.uilts.HybridIdGenerator;
import com.daox.online.uilts.InitialPassword;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UsersServiceImpl implements UsersService {

    @Resource
    private SysUsersMapper sysUsersMapper;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private InitialPassword initialPassword;

    @Resource
    private HybridIdGenerator hybridIdGenerator;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private AuditLogService auditLogService;

    /**
     * 获取个人信息
     *
     * @param identifier 学号/工号
     * @return 个人信息
     */
    @Override
    public ProfileVo getProfile(String identifier) {
        if (StringUtils.isEmpty(identifier)) {
            log.warn("[getProfile.method]: identifier为空");
            return null;
        }
        Users userByIdentifier = sysUsersMapper.findUserByIdentifier(identifier);
        if (userByIdentifier == null) {
            log.warn("[getProfile.method]:用户不存在");
            return null;
        }
        UserDescribe userDescribeById = sysUsersMapper.getUserDescribeById(userByIdentifier.getId());
        if (userDescribeById == null) {
            log.warn("[getProfile.method]:用户描述不存在");
        }
        return new ProfileVo()
                .setId(userByIdentifier.getId() != null ? userByIdentifier.getId() : "")
                .setIdentifier(userByIdentifier.getIdentifier() != null ? userByIdentifier.getIdentifier() : "")
                .setEmail(userByIdentifier.getEmail() != null ? userByIdentifier.getEmail() : "")
                .setNickname(userByIdentifier.getNickname() != null ? userByIdentifier.getNickname() : "")
                .setRole(userByIdentifier.getRole() != null ? userByIdentifier.getRole() : "")
                .setAvatarUrl(userByIdentifier.getAvatarUrl() != null ? userByIdentifier.getAvatarUrl() : "")
                .setCreatedAt(userByIdentifier.getCreatedAt() != null ? userByIdentifier.getCreatedAt() : null)
                .setPhone(userDescribeById != null && userDescribeById.getPhone() != null ? userDescribeById.getPhone() : "")
                .setGender(userDescribeById != null && userDescribeById.getGender() != null ? userDescribeById.getGender() : "")
                .setBirthday(userDescribeById != null && userDescribeById.getBirthday() != null ? userDescribeById.getBirthday() : null)
                .setBiography(userDescribeById != null && userDescribeById.getBiography() != null ? userDescribeById.getBiography() : "")
                ;
    }

    /**
     * 更新个人资料 - 学生
     *
     * @param profileVo 个人信息
     * @return 个人信息
     */
    @Override
    public ProfileVo updateProfile(ProfileVo profileVo) {
        // 首先取出userId
        String userId = profileVo.getId();
        if (StringUtils.isEmpty(userId)) {
            log.warn("[updateProfile.method]用户ID为空");
            return null;
        }
        Users userById = sysUserService.findUserById(userId);
        if (userById == null) {
            log.warn("[updateProfile.method]用户不存在{}", userId);
            return null;
        }
        UserDescribe userDescribeById = sysUsersMapper.getUserDescribeById(userId);
        int i = sysUsersMapper.updateUser(
                new Users()
                        .setId(userById.getId()).setNickname(profileVo.getNickname())
                        .setEmail(profileVo.getEmail()).setAvatarUrl(profileVo.getAvatarUrl())
                        .setUpdatedAt(DateUtils.convertToDate(LocalDateTime.now())));
        if (i > 0) {
            log.warn("[updateProfile.method]更新用户成功{}", userId);
            // 存在用户描述的情况
            if (userDescribeById != null) {
                log.info("[updateProfile.method]用户描述存在{}", userId);
                int ini = sysUsersMapper.updateUserDescribe(
                        new UserDescribe()
                                .setUserId(userDescribeById.getUserId())
                                .setPhone(profileVo.getPhone())
                                .setGender(profileVo.getGender())
                                .setBirthday(profileVo.getBirthday())
                                .setBiography(profileVo.getBiography()));
                // 更新
                if (ini > 0) {
                    log.warn("[updateProfile.method]更新用户描述成功{}", userId);
                    return new ProfileVo()
                            .setId(userById.getId())
                            .setIdentifier(userById.getIdentifier())
                            .setNickname(userById.getNickname())
                            .setEmail(userById.getEmail())
                            .setRole(userById.getRole())
                            .setAvatarUrl(userById.getAvatarUrl())
                            .setCreatedAt(userById.getCreatedAt())
                            .setPhone(profileVo.getPhone())
                            .setGender(profileVo.getGender())
                            .setBirthday(profileVo.getBirthday())
                            .setBiography(profileVo.getBiography());
                }
            }else {
                // 不存在 用户描述的情况
                log.warn("[updateProfile.method]用户描述不存在{}", userId);
                // 但是profileVo中存在相关数据 --> 加入
                if (profileVo.getPhone() != null || profileVo.getGender() != null || profileVo.getBirthday() != null || profileVo.getBiography() != null){
                    String id = hybridIdGenerator.generateId();
                    int i1 = sysUsersMapper.insertUserDescribe(new UserDescribe()
                            .setId(id)
                            .setUserId(userById.getId())
                            .setPhone(profileVo.getPhone())
                            .setGender(profileVo.getGender())
                            .setBirthday(profileVo.getBirthday())
                            .setBiography(profileVo.getBiography())
                    );
                    if (i1 > 0) {
                        log.warn("[updateProfile.method]插入用户描述成功{}", userId);
                        return new ProfileVo()
                                .setId(userById.getId())
                                .setIdentifier(userById.getIdentifier())
                                .setNickname(userById.getNickname())
                                .setEmail(userById.getEmail())
                                .setRole(userById.getRole())
                                .setAvatarUrl(userById.getAvatarUrl())
                                .setCreatedAt(userById.getCreatedAt())
                                .setPhone(profileVo.getPhone())
                                .setGender(profileVo.getGender())
                                .setBirthday(profileVo.getBirthday())
                                .setBiography(profileVo.getBiography());
                    }
                }

            }
        }
        return null;
    }

    /**
     * 判定用户角色 - 学生
     *
     * @param userId 用户ID
     * @return true: 是学生
     */
    @Override
    public boolean isStudent(String userId) {
        Users userById = sysUserService.findUserById(userId);
        if (userById == null) {
            log.warn("[isStudent.method]用户不存在{}", userId);
            return false;
        }
        return userById.getRole().equals(Const.ROLE_STUDENTS);
    }

    /**
     * 判定用户角色 - 教师
     *
     * @param userId 用户ID
     * @return true: 是教师
     */
    @Override
    public boolean isTeacher(String userId) {
        Users userById = sysUserService.findUserById(userId);
        if (userById == null) {
            log.warn("[isTeacher.method]用户不存在{}", userId);
            return false;
        }
        return userById.getRole().equals(Const.ROLE_TEACHERS);
    }

    /**
     * 判定用户角色 - 管理员
     *
     * @param userId 用户ID
     * @return true: 是管理员
     */
    @Override
    public boolean isAdmin(String userId) {
        Users userById = sysUserService.findUserById(userId);
        if (userById == null) {
            log.warn("[isAdmin.method]用户不存在{}", userId);
            return false;
        }
        return userById.getRole().equals(Const.ROLE_ADMIN);
    }

    /**
     * 获取用户列表 - 无Admin
     *
     * @return 用户列表
     */
    @Override
    public List<ProfileInfoVo> getUserListNotAdmin() {
        List<Users> userListNotAdmin = sysUsersMapper.getUserListNotAdmin();
        if (userListNotAdmin == null || userListNotAdmin.isEmpty()) {
            log.warn("[getUserListNotAdmin.method]用户列表为空");
            return Collections.emptyList();
        }
        return userListNotAdmin.stream().map(user -> {
            UserDescribe userDescribe = sysUsersMapper.getUserDescribeById(user.getId());
            return new ProfileInfoVo()
                    .setId(user.getId() != null ? user.getId() : "")
                    .setIdentifier(user.getIdentifier() != null ? user.getIdentifier() : "")
                    .setNickname(user.getNickname() != null ? user.getNickname() : "")
                    .setEmail(user.getEmail() != null ? user.getEmail() : "")
                    .setRole(user.getRole() != null ? user.getRole() : "")
                    .setAvatarUrl(user.getAvatarUrl() != null ? user.getAvatarUrl() : "")
                    .setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt() : null)
                    .setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt() : null)
                    .setPhone(userDescribe != null && userDescribe.getPhone() != null ? userDescribe.getPhone() : "")
                    .setGender(userDescribe != null && userDescribe.getGender() != null ? userDescribe.getGender() : "")
                    .setBirthday(userDescribe != null && userDescribe.getBirthday() != null ? userDescribe.getBirthday() : null)
                    .setBiography(userDescribe != null && userDescribe.getBiography() != null ? userDescribe.getBiography() : "")
                    .setIsDeleted(user.getIsDeleted() != null ? user.getIsDeleted() : 0);
        }).collect(Collectors.toList());
    }

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    @Override
    public ProfileInfoVo getUserDetail(String userId) {
        Users userById = sysUsersMapper.findUserById(userId);
        if (userById == null) {
            log.warn("[getUserDetail.method]用户不存在{}", userId);
            return null;
        }
        UserDescribe userDescribe = sysUsersMapper.getUserDescribeById(userId);

        return new ProfileInfoVo()
                .setId(userById.getId() != null ? userById.getId() : "")
                .setIdentifier(userById.getIdentifier() != null ? userById.getIdentifier() : "")
                .setNickname(userById.getNickname() != null ? userById.getNickname() : "")
                .setEmail(userById.getEmail() != null ? userById.getEmail() : "")
                .setRole(userById.getRole() != null ? userById.getRole() : "")
                .setAvatarUrl(userById.getAvatarUrl() != null ? userById.getAvatarUrl() : "")
                .setCreatedAt(userById.getCreatedAt() != null ? userById.getCreatedAt() : null)
                .setUpdatedAt(userById.getUpdatedAt() != null ? userById.getUpdatedAt() : null)
                .setPhone(userDescribe != null && userDescribe.getPhone() != null ? userDescribe.getPhone() : "")
                .setGender(userDescribe != null && userDescribe.getGender() != null ? userDescribe.getGender() : "")
                .setBirthday(userDescribe != null && userDescribe.getBirthday() != null ? userDescribe.getBirthday() : null)
                .setBiography(userDescribe != null && userDescribe.getBiography() != null ? userDescribe.getBiography() : "")
                .setIsDeleted(userById.getIsDeleted() != null ? userById.getIsDeleted() : 0);
    }

    /**
     * 删除用户 - 逻辑删除
     *
     * @param userId 用户ID
     * @return 被删除的用户信息
     */
    @Override
    public ProfileInfoVo deleteUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.warn("[deleteUser.method]参数错误: userId={}", userId);
            return null;
        }
        Users userById = sysUsersMapper.findUserById(userId);
        if (userById == null) {
            log.warn("[deleteUser.method]用户不存在{}", userId);
            return null;
        }
        int i = sysUsersMapper.deleteUser(userId);
        if (i <= 0) {
            log.warn("[deleteUser.method]删除用户失败{}", userId);
            return null;
        }
        ProfileInfoVo profileInfoVo = new ProfileInfoVo();
        Users userById1 = sysUsersMapper.findUserById(userId);
        BeanUtils.copyProperties(userById1, profileInfoVo);
        return profileInfoVo;
    }

    /**
     * 恢复被禁用的用户。
     *
     * @param userId 用户ID
     * @return 恢复后的用户信息
     */
    @Override
    public ProfileInfoVo restoreUser(String userId) {
        if (StringUtils.isBlank(userId)) {
            log.warn("[restoreUser.method]参数错误: userId为空");
            return null;
        }
        Users currentUser = sysUsersMapper.findUserById(userId);
        if (currentUser == null) {
            log.warn("[restoreUser.method]用户不存在: userId={}", userId);
            return null;
        }
        int affectedRows = sysUsersMapper.restoreUser(userId);
        if (affectedRows <= 0) {
            log.warn("[restoreUser.method]恢复用户失败: userId={}", userId);
            return null;
        }
        return getUserDetail(userId);
    }

    /**
     * 重置用户密码 - admin重置
     *
     * @param userId 用户ID
     * @return 新密码
     */
    @Override
    public String resetUserPassword(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.warn("[resetUserPassword.method]参数错误: userId={}", userId);
            return null;
        }
        Users userById = sysUsersMapper.findUserById(userId);
        if (userById == null) {
            log.warn("[resetUserPassword.method]用户不存在{}", userId);
            return null;
        }
        Map<String, Object> beforeSnapshot = buildPasswordResetSnapshot(userById);
        String newPassword = initialPassword.getInitialPassword2();
        String encodedPassword = passwordEncoder.encode(newPassword);
        int i = sysUsersMapper.resetUserPassword(userId, encodedPassword);
        Map<String, Object> afterSnapshot = buildPasswordResetSnapshot(userById);
        afterSnapshot.put("passwordReset", i > 0);
        afterSnapshot.put("updatedAt", LocalDateTime.now());
        auditLogService.recordSensitiveOperation(
                "USER_PASSWORD_RESET",
                "user",
                userId,
                beforeSnapshot,
                afterSnapshot,
                i > 0 ? "SUCCESS" : "FAILED",
                i > 0 ? "管理员重置用户密码成功" : "管理员重置用户密码失败");
        if (i <= 0) {
            log.warn("[resetUserPassword.method]更新用户密码失败{}", userId);
            return null;
        }
        return newPassword;
    }

    /**
     * 管理员创建用户。
     *
     * @param userUpsertVO 用户维护请求体
     * @return 创建后的用户详情
     */
    @Override
    @Transactional
    public ProfileInfoVo createUserByAdmin(AdminUserUpsertVO userUpsertVO) {
        if (!validateAdminUserPayload(userUpsertVO, true)) {
            return null;
        }
        if (sysUsersMapper.findUserByIdentifier(userUpsertVO.getIdentifier()) != null
                || sysUsersMapper.findUserByEmail(userUpsertVO.getEmail()) != null
                || sysUsersMapper.findUserByNickname(userUpsertVO.getNickname()) != null) {
            log.warn("[createUserByAdmin.method]用户标识、邮箱或昵称已存在: identifier={}, email={}",
                    userUpsertVO.getIdentifier(), userUpsertVO.getEmail());
            return null;
        }
        String userId = hybridIdGenerator.generateId();
        Date currentTime = DateUtils.convertToDate(LocalDateTime.now());
        Users newUser = new Users()
                .setId(userId)
                .setIdentifier(userUpsertVO.getIdentifier().trim())
                .setNickname(userUpsertVO.getNickname().trim())
                .setEmail(userUpsertVO.getEmail().trim())
                .setRole(userUpsertVO.getRole())
                .setPassword(passwordEncoder.encode(userUpsertVO.getPassword().trim()))
                .setAvatarUrl("")
                .setCreatedAt(currentTime)
                .setUpdatedAt(currentTime)
                .setIsDeleted(Boolean.FALSE.equals(userUpsertVO.getEnabled()) ? 1 : 0);
        int insertedUsers = sysUsersMapper.registerUser(newUser);
        if (insertedUsers <= 0) {
            log.warn("[createUserByAdmin.method]创建用户失败: identifier={}", userUpsertVO.getIdentifier());
            return null;
        }
        if (StringUtils.isNotBlank(userUpsertVO.getPhone())) {
            sysUsersMapper.insertUserDescribe(new UserDescribe()
                    .setId(hybridIdGenerator.generateId())
                    .setUserId(userId)
                    .setPhone(userUpsertVO.getPhone().trim()));
        }
        return getUserDetail(userId);
    }

    /**
     * 管理员更新用户基础信息。
     *
     * @param userId       用户ID
     * @param userUpsertVO 用户维护请求体
     * @return 更新后的用户详情
     */
    @Override
    @Transactional
    public ProfileInfoVo updateUserByAdmin(String userId, AdminUserUpsertVO userUpsertVO) {
        if (StringUtils.isBlank(userId) || !validateAdminUserPayload(userUpsertVO, false)) {
            return null;
        }
        Users currentUser = sysUsersMapper.findUserById(userId);
        if (currentUser == null) {
            log.warn("[updateUserByAdmin.method]用户不存在: userId={}", userId);
            return null;
        }
        Users identifierUser = sysUsersMapper.findUserByIdentifier(userUpsertVO.getIdentifier());
        if (identifierUser != null && !userId.equals(identifierUser.getId())) {
            log.warn("[updateUserByAdmin.method]学号或工号冲突: identifier={}", userUpsertVO.getIdentifier());
            return null;
        }
        Users emailUser = sysUsersMapper.findUserByEmail(userUpsertVO.getEmail());
        if (emailUser != null && !userId.equals(emailUser.getId())) {
            log.warn("[updateUserByAdmin.method]邮箱冲突: email={}", userUpsertVO.getEmail());
            return null;
        }
        Users nicknameUser = sysUsersMapper.findUserByNickname(userUpsertVO.getNickname());
        if (nicknameUser != null && !userId.equals(nicknameUser.getId())) {
            log.warn("[updateUserByAdmin.method]昵称冲突: nickname={}", userUpsertVO.getNickname());
            return null;
        }
        int updatedUsers = sysUsersMapper.updateUser(new Users()
                .setId(userId)
                .setIdentifier(userUpsertVO.getIdentifier().trim())
                .setNickname(userUpsertVO.getNickname().trim())
                .setEmail(userUpsertVO.getEmail().trim())
                .setRole(userUpsertVO.getRole())
                .setAvatarUrl(currentUser.getAvatarUrl())
                .setIsDeleted(Boolean.FALSE.equals(userUpsertVO.getEnabled()) ? 1 : 0)
                .setUpdatedAt(DateUtils.convertToDate(LocalDateTime.now())));
        if (updatedUsers <= 0) {
            log.warn("[updateUserByAdmin.method]更新用户失败: userId={}", userId);
            return null;
        }
        UserDescribe userDescribe = sysUsersMapper.getUserDescribeById(userId);
        if (userDescribe == null && StringUtils.isNotBlank(userUpsertVO.getPhone())) {
            sysUsersMapper.insertUserDescribe(new UserDescribe()
                    .setId(hybridIdGenerator.generateId())
                    .setUserId(userId)
                    .setPhone(userUpsertVO.getPhone().trim()));
        } else if (userDescribe != null) {
            sysUsersMapper.updateUserDescribe(new UserDescribe()
                    .setUserId(userId)
                    .setPhone(StringUtils.trimToEmpty(userUpsertVO.getPhone()))
                    .setGender(userDescribe.getGender())
                    .setBirthday(userDescribe.getBirthday())
                    .setBiography(userDescribe.getBiography()));
        }
        return getUserDetail(userId);
    }

    private Map<String, Object> buildPasswordResetSnapshot(Users user) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", user.getId());
        snapshot.put("identifier", user.getIdentifier());
        snapshot.put("nickname", user.getNickname());
        snapshot.put("email", user.getEmail());
        snapshot.put("role", user.getRole());
        snapshot.put("passwordSet", user.getPassword() != null && !user.getPassword().isBlank());
        snapshot.put("isDeleted", user.getIsDeleted());
        return snapshot;
    }

    /**
     * 校验管理员维护用户时提交的基础字段。
     *
     * @param userUpsertVO 请求体
     * @param checkPassword 是否强制校验密码
     * @return true 表示参数合法
     */
    private boolean validateAdminUserPayload(AdminUserUpsertVO userUpsertVO, boolean checkPassword) {
        if (userUpsertVO == null) {
            log.warn("[validateAdminUserPayload.method]参数为空");
            return false;
        }
        if (StringUtils.isBlank(userUpsertVO.getIdentifier())
                || StringUtils.isBlank(userUpsertVO.getNickname())
                || StringUtils.isBlank(userUpsertVO.getEmail())
                || StringUtils.isBlank(userUpsertVO.getRole())) {
            log.warn("[validateAdminUserPayload.method]必要参数缺失");
            return false;
        }
        if (checkPassword && StringUtils.isBlank(userUpsertVO.getPassword())) {
            log.warn("[validateAdminUserPayload.method]创建用户时密码不能为空");
            return false;
        }
        return true;
    }

    /**
     * 获取用户总数
     *
     * @return 用户总数
     */
    @Override
    public int getUserCount() {
        return sysUsersMapper.getUserCount();
    }

    /**
     * 获取管理员总数
     *
     * @return 管理员总数
     */
    @Override
    public int getAdminCount() {
        return sysUsersMapper.getAdminCount();
    }

    /**
     * 获取学生总数
     *
     * @return 学生总数
     */
    @Override
    public int getStudentCount() {
        return sysUsersMapper.getStudentCount();
    }

    /**
     * 获取教师总数
     *
     * @return 教师总数
     */
    @Override
    public int getTeacherCount() {
        return sysUsersMapper.getTeacherCount();
    }

    /**
     * 获取用户统计 - 有效数据
     *
     * @return 用户统计
     */
    @Override
    public int getUserCountValid() {
        return sysUsersMapper.getUserCountValid();
    }

    /**
     * 获取管理员统计 - 有效数据
     *
     * @return 管理员统计
     */
    @Override
    public int getAdminCountValid() {
        return sysUsersMapper.getAdminCountValid();
    }

    /**
     * 获取学生统计 - 有效数据
     *
     * @return 学生统计
     */
    @Override
    public int getStudentCountValid() {
        return sysUsersMapper.getStudentCountValid();
    }

    /**
     * 获取教师统计 - 有效数据
     *
     * @return 教师统计
     */
    @Override
    public int getTeacherCountValid() {
        return sysUsersMapper.getTeacherCountValid();
    }
}
