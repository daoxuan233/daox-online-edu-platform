package com.daox.online.entity.views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordVO {
    /**
     * 原密码
     */
    @Length(min = 6, max = 25)
    private String oldPassword;

    /**
     * 新密码
     */
    @Length(min = 6, max = 25)
    private String newPassword;

    /**
     * 确认新密码
     */
    @Length(min = 6, max = 25)
    private String confirmPassword;
}
