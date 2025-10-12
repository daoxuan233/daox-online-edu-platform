package com.daox.online.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
/**
 * 讲师课程数据传输对象
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InstructorCourseDTO implements Serializable {
    private String courseId;
    private String title;
    private String cover;
    private Integer price;
    private Integer studentCount;
    private BigDecimal rating;
}
