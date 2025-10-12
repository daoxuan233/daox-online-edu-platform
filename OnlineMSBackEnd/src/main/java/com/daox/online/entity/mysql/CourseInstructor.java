package com.daox.online.entity.mysql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 课程讲师信息表<br />
 * 辅助讲师身份说明
 * TableName : course_instructor
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CourseInstructor implements Serializable {

    /**
     * 讲师ID
     */
    private String id;
    /**
     * 教师id
     */
    private String teacherId;
    /**
     * 讲师职位/头衔
     */
    private String title;
    /**
     * 讲师简介
     */
    private String biography;
}
