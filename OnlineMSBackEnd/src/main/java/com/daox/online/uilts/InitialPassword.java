package com.daox.online.uilts;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 密码初始化 - 算法
 */
@Component
public class InitialPassword {
    /**
     * 初始密码1：DaoX 拼接 学号/工号后6位
     *
     * @param identifier 学号/工号
     * @return 初始密码
     */
    public String getInitialPassword1(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return null;
        }
        return "DaoX" + identifier.substring(identifier.length() - 6);
    }

    /**
     * 初始密码2：复杂但高性能的16位随机序列
     *
     * @return 初始密码
     */
    public String getInitialPassword2() {
        String pwdOne = HybridIdGenerator.getInstance().generateId();
        String pwdTwo = UUID.randomUUID().toString();
        return pwdOne.substring(0, 8) + pwdTwo.substring(8, 16);
    }
}
