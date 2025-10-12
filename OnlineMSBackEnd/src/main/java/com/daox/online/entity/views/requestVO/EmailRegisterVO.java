package com.daox.online.entity.views.requestVO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * 用户注册表单信息
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
/**
 * 用户注册表单信息
 */
public class EmailRegisterVO {
    /**
     * 邮箱
     */
    @Email
    String email;
    /**
     * 学号/工号
     */
    String identifier;
    /**
     * 验证码 (6位数字)
     */
    @Length(max = 6, min = 6)
    String code;
    /**
     * 用户名 (1-10个字符)
     */
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$") // 用户名只能包含字母、数字和汉字
    @Length(min = 1, max = 10) // 用户名长度限制为1到10个字符
    String username;
    /**
     * 密码 (6-20个字符)
     */
    @Length(min = 6, max = 25) // 密码长度限制为6到20个字符
    String password;

    /**
     * 头像链接
     * 可选字段，可为null
     */
    String avatarUrl;

}
