package com.daox.online.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 课程详情数据传输对象
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CourseDetailDTO implements Serializable {
    private String courseId;
    private String title;
    private String description;
    private String cover;
    private Integer price;
    private Integer originalPrice;
    private String level;
    private Boolean isNew;
    private Integer studentCount;
    private String targetAudience;
    private String technicalRequirements;
    private String teacherId;
    private String instructorId;
    private String instructorName;
    private String instructorAvatar;
    private String instructorTitle;
    private String instructorBiography;
}
