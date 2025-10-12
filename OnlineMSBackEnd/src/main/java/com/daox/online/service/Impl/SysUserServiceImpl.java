package com.daox.online.service.Impl;

import com.daox.online.entity.mysql.Users;
import com.daox.online.entity.views.UpdatePasswordVO;
import com.daox.online.entity.views.requestVO.ConfirmResetVO;
import com.daox.online.entity.views.requestVO.EmailRegisterVO;
import com.daox.online.entity.views.requestVO.EmailResetVO;
import com.daox.online.mapper.SysUsersMapper;
import com.daox.online.service.SysUserService;
import com.daox.online.uilts.constant.Const;
import com.daox.online.uilts.EmailVerificationCodeGenerator;
import com.daox.online.uilts.FlowUtils;
import com.daox.online.uilts.HybridIdGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SysUserServiceImpl implements SysUserService {

    @Value("${spring.web.verify.mail-limit}") // 验证码发送冷却时间，秒
    Integer mailLimit;

    @Resource
    private FlowUtils flowUtils;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private AmqpTemplate amqpTemplate;

    @Resource
    private SysUsersMapper sysUserMapper;

    @Resource
    private HybridIdGenerator hybridIdGenerator;

    /**
     * 根据用户名、邮箱、学号/工号查找用户 DTO。
     *
     * @param username 用户名、邮箱
     * @return Users DTO，找不到返回 null
     */
    @Override
    public Users findUserByUsernameOrEmail(String username) {
        log.info("根据用户名、邮箱、学号/工号查找用户 DTO: {}", username);
        Users user = sysUserMapper.findByUsernameOrEmailOrIdentifier(username);
        if (user == null) {
            log.warn("未找到用户: {}", username);
        }
        if (user != null) {
            log.info("找到用户: {}", user.getId());
            return user;
        }
        return null;
    }

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
    @Override
    public String registerEmailVerifyCode(String type, String email, String address) {
        // 防止并发请求 .intern()--> 将字符串放入常量池  防止内存泄漏
        // 使用 IP 地址作为锁对象，简单防止同一 IP 的并发请求 (非分布式锁)
        synchronized (address.intern()) { // 锁的是请求的ip地址  防止同一ip短时间内多次请求
            if (!this.verifyLimit(address)) {
                log.warn("IP '{}' 请求验证码过于频繁 (邮箱: {})", address, email);
                return "请求过于频繁，请 " + mailLimit + " 秒后再试";
            }
            String code = EmailVerificationCodeGenerator.generateVerificationCode();
            // 3. 准备发送到消息队列的数据
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            try {
                amqpTemplate.convertAndSend(Const.MQ_MAIL, data); // 发送消息到消息队列
                log.info("已将类型 '{}' 的验证码发送任务放入队列 (邮箱: {}, IP: {})", type, email, address);
                // 5. 将验证码存入 Redis，有效期 3 分钟
                stringRedisTemplate
                        .opsForValue()
                        .set(Const.VERIFY_EMAIL_DATA + email, code, 3, TimeUnit.MINUTES); // 验证码存入redis 3分钟过期
                return null; // 表示成功或已入队
            } catch (Exception e) {
                log.error("发送邮件任务到 RabbitMQ 或操作 Redis 时出错 (邮箱: {}, IP: {})", email, address, e);
                // 发送失败，不应限制用户重试 (理论上 MQ 应处理失败情况)
                // 可以考虑移除频率限制标记，让用户可以立即重试
                // stringRedisTemplate.delete(limitKey);
                return "发送验证码失败，请稍后重试";
            }
        }
    }

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
    @Override
    public String registerEmailAccount(EmailRegisterVO info) {
        log.info("注册邮箱账号: {}", info.getEmail());
        String email = info.getEmail();
        if (email == null || email.isEmpty()){
            log.warn("邮箱不能为空");
            return "邮箱不能为空！";
        }
        String code = this.getEmailVerifyCode(email);
        // 1. 校验验证码
        if (code == null || code.isEmpty()) return "请先获取验证码！";
        if (!code.equals(info.getCode())) return "验证码错误，请重新输入！";
        if (this.existsAccountByEmail(email)) return "邮箱已被注册！";
        if(this.existsAccountByIdentifier(info.getIdentifier())) return "该学号已存在";
        String username = info.getUsername();
        if (this.existsAccountByUsername(username)) return "用户名已被注册！请重试";
        String password = passwordEncoder.encode(info.getPassword());
        log.info("用户注册信息: {}", info);
        // TODO 用户信息入库
        Users users = new Users()
                .setId(UUID.randomUUID()+ hybridIdGenerator.generateId())
                .setIdentifier(info.getIdentifier())
                .setPassword(password)
                .setNickname(info.getUsername())
                .setEmail(info.getEmail())
                .setRole(Const.ROLE_STUDENTS)
                .setAvatarUrl(info.getAvatarUrl())
                .setCreatedAt(new Date());
        int i = sysUserMapper.registerUser(users);
        if (i > 0){
            log.info("用户注册成功: {}", users);
            return null;
        }
        return "注册失败，请稍后重试！";
    }

    /**
     * 获取Redis中存储的邮箱验证码
     *
     * @param email 邮箱
     * @return 验证码
     */
    private String getEmailVerifyCode(String email) {
        String key = Const.VERIFY_EMAIL_DATA + email;
        return stringRedisTemplate.opsForValue().get(key); // 获取验证码
    }

    /**
     * 校验邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    private Boolean existsAccountByEmail(String email) {
        Users userByEmail = sysUserMapper.findUserByEmail(email);
        return userByEmail != null;
    }

    /**
     * 校验用户名是否存在
     *
     * @param nickname 用户名
     * @return 是否存在
     */
    private Boolean existsAccountByUsername(String nickname) {
        Users userByUsername = sysUserMapper.findUserByNickname(nickname);
        return userByUsername != null;
    }

    /**
     * 校验学号是否存在
     *
     * @param identifier 学号/工号
     * @return 是否存在
     */
    private Boolean existsAccountByIdentifier(String identifier) {
        Users userByUsername = sysUserMapper.findUserByIdentifier(identifier);
        return userByUsername != null;
    }


    /**
     * 删除redis中的邮件验证码
     *
     * @param email 邮箱
     */
    private void deleteEmailVerifyCode(String email) {
        String key = Const.VERIFY_EMAIL_DATA + email;
        stringRedisTemplate.delete(key);
        log.debug("已删除邮箱 {} 的验证码", email);
    }

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
    @Override
    public String resetEmailAccountPassword(EmailResetVO info) {
        String verify = resetConfirm(new ConfirmResetVO(info.getEmail(), info.getCode()));
        if (verify != null) return verify;
        String email = info.getEmail();
        String password = passwordEncoder.encode(info.getPassword());
        int i = sysUserMapper.updatePassword(email, password);
        if (i > 0) this.deleteEmailVerifyCode(email);
        return i > 0 ? null : "更新失败，请联系管理员";
    }

    /**
     * 重置密码确认操作，校验验证码是否正确
     *
     * @param info 验证基本信息
     * @return 操作结果 <br/>
     * 1. 成功：null <br/>
     * 2. 失败：错误原因 <br/>
     */
    @Override
    public String resetConfirm(ConfirmResetVO info) {
        String email = info.getEmail();
        String code = this.getEmailVerifyCode(email);
        if (code == null || code.isEmpty()) return "请先获取验证码";
        if (!code.equals(info.getCode())) return "验证码错误，重新输入";
        return null;
    }

    /**
     * 根据用户ID查找用户
     *
     * @param userId 用户ID
     * @return 用户实体，如果找不到则返回 null
     */
    @Override
    public Users findUserById(String userId) {
        log.info("正在通过用户ID查找用户: {}", userId);
        return sysUserMapper.findUserById(userId);
    }

    /**
     * 修改密码 - 非验证验证码方式
     *
     * @param vo 密码修改信息
     * @return 错误信息，成功返回null
     */
    @Override
    public String updatePassword(UpdatePasswordVO vo) {
        // 1. 检查用户是否已登录
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "用户未登录或会话已过期";
        }

        // 1. 校验新密码和确认密码是否一致
        if (!vo.getNewPassword().equals(vo.getConfirmPassword())) {
            return "新密码与确认密码不一致";
        }

        // 2. 获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 去除username的前后双引号
        username = username.replaceAll("^\"|\"$", "");
        log.info("用户信息：{}",username);
        Users user = this.findUserByUsernameOrEmail(username);

        if (user == null) {
            log.warn("用户不存在: username={}", username);
            return "用户不存在";
        }

        // 3. 校验旧密码
        if (!passwordEncoder.matches(vo.getOldPassword(), user.getPassword())) {
            return "旧密码不正确";
        }

        // 4. 更新密码
        String encodedPassword = passwordEncoder.encode(vo.getNewPassword());
        int i = sysUserMapper.updatePassword_verify(user.getEmail(), encodedPassword, new Date());
        if (i > 0) {
            log.info("用户密码修改成功: username={}", username);
            return null;
        } else {
            log.error("用户密码修改失败: username={}", username);
            return "内部错误，用户密码修改失败";
        }
    }

    /**
     * @param username  用户名
     * @return UserDetails 用户信息
     * @throws UsernameNotFoundException 用户名不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = findUserByUsernameOrEmail(username);
        if (user == null) {
            log.warn("登录认证失败，用户不存在: {}", username);
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        // 检查用户状态
        if (Objects.equals(user.getIsDeleted(), Const.USER_IS_DELETED_TRUE)) {
            log.warn("用户 '{}' 尝试登录但账户已注销", username);
            throw new UsernameNotFoundException("账户已注销");
        }
        log.info("用户 '{}' (角色: {}) 正在进行登录认证...", username, user.getRole());
        return User
                .withUsername(username) // 使用数据库中的用户名
                .password(user.getPassword()) // 提供数据库中存储的加密密码
                .roles(user.getRole())
                .build();
    }

    /**
     * 针对ip地址进行邮件验证码获取限流
     *
     * @param address ip地址
     * @return 是否通过验证
     */
    private Boolean verifyLimit(String address) {
        String key = Const.VERIFY_EMAIL_LIMIT;
        return flowUtils.limitOnceCheck(key, mailLimit);
    }
}
