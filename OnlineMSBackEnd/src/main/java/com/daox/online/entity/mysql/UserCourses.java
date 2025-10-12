package com.daox.online.entity.mysql;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生选课关联表<br />
 * 实现用户与课程的多对多关系-记录了哪个学生参加了哪门课程<br />
 * TableName:  user_courses
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserCourses implements Serializable {

    /**
     * 关联ID
     */
    private String id;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 课程ID
     */
    private String courseId;
    /**
     * 加入时间
     */
    private Date enrollmentDate;

}
