package com.daox.online.entity.views.responseVO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 选课学生信息VO
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class EnrolledStudentVO {
    /**
     * 学生学号
     */
    private String identifier;
    /**
     * 学生昵称
     */
    private String nickname;
    /**
     * 学生邮箱
     */
    private String email;
    /**
     * 学生头像
     */
    private String avatarUrl;
    /**
     * 学生加入时间
     */
    private Date enrollmentDate;
}
