package com.daox.online.service;

import com.daox.online.entity.mysql.Users;
import com.daox.online.entity.views.UpdatePasswordVO;
import com.daox.online.entity.views.requestVO.ConfirmResetVO;
import com.daox.online.entity.views.requestVO.EmailRegisterVO;
import com.daox.online.entity.views.requestVO.EmailResetVO;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 账户服务接口，继承 UserDetailsService 以集成 Spring Security。
 * 定义用户查找、注册、密码重置等操作。
 */
public interface SysUserService extends UserDetailsService {
    /**
     * 根据用户名、邮箱查找用户 DTO。
     *
     * @param username 用户名、邮箱
     * @return Users DTO，找不到返回 null
     */
    @Nullable
    // 返回值可能为 null
    Users findUserByUsernameOrEmail(String username);

    /**
     * 注册邮箱验证码 <br/>
     * 1. 生成验证码 <br/>
     * 2. 发送验证码 <br/>
     * 3. 将验证码存入redis <br/>
     * 4. 返回验证码 <br/>
     * 5. 验证码类型：register 注册，reset 重置密码 <br/>
     * 6. 消息队列 RabbitMQ等待发送
     *
     * @param type    验证码类型
     * @param email   邮箱
     * @param address 请求ip
     * @return 操作结果 <br/>
     * 1. 成功：null <br/>
     * 2. 失败：错误原因 <br/>
     */
    String registerEmailVerifyCode(String type, String email, String address);

    /**
     * 注册邮箱账号 <br/>
     * 1. 校验验证码 <br/>
     * 2. 校验邮箱、用户名是否存在重名 <br/>
     * 3. 返回结果 <br/>
     *
     * @param info 注册信息
     * @return 操作结果 <br/>
     * 1. 成功：null <br/>
     * 2. 失败：错误原因 <br/>
     */
    String registerEmailAccount(EmailRegisterVO info);

    /**
     * 重置邮箱账号密码 <br/>
     * 1. 校验验证码 <br/>
     * 2. 校验邮箱是否存在 <br/>
     * 3. 重置密码 <br/>
     * 4. 返回结果 <br/>
     *
     * @param info 重置信息
     * @return 操作结果 <br/>
     * 1. 成功：null <br/>
     * 2. 失败：错误原因 <br/>
     */
    String resetEmailAccountPassword(EmailResetVO info);

    /**
     * 重置密码确认操作，校验验证码是否正确
     *
     * @param info 验证基本信息
     * @return 操作结果 <br/>
     * 1. 成功：null <br/>
     * 2. 失败：错误原因 <br/>
     */
    String resetConfirm(ConfirmResetVO info);

    /**
     * 根据用户ID查找用户
     *
     * @param userId 用户ID
     * @return 用户实体，如果找不到则返回 null
     */
    Users findUserById(String userId);

    /**
     * 修改密码
     * @param vo 密码修改信息
     * @return 错误信息，成功返回null
     */
    String updatePassword(UpdatePasswordVO vo);

}
