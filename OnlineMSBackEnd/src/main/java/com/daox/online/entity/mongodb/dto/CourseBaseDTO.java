package com.daox.online.entity.mongodb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 课程基础信息
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CourseBaseDTO implements Serializable {
    /**
     * 课程id
     */
    private String id;
    /**
     * 课程名称
     */
    private String name;
}
