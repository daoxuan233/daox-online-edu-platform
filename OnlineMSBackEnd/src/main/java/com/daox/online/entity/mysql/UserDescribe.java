package com.daox.online.entity.mysql;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户描述信息<br />
 * TableName:  user_describe
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserDescribe implements Serializable {
    /**
     * 描述id
     */
    private String id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别
     * 男 [man]、女 [female]、其他 [other]
     */
    private String gender;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 简介 / 签名
     */
    private String biography;
}
