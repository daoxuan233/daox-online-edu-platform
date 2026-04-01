package com.daox.online.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 课程核心信息 DTO
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CourseCoreInfoDto implements Serializable {

    // 来自 courses 表
    private String title;
    private String description;
    private String coverImageUrl;
    private String teacherId;
    private String categoryId;
    /**
     * 课程状态字段。
     * <p>
     * 合法值包括 draft、pending、published、taken_down、archived。
     * 普通课程编辑接口不会直接信任前端传入的状态，
     * 实际状态变更必须经由课程状态机流转接口完成。
     * </p>
     */
    private String status;
    private boolean privateCourse;

    // 来自 course_properties 表 (嵌套对象，结构更清晰)
    private CoursePropertiesDto properties;

    /**
     * 内部类，用于表示课程属性
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class CoursePropertiesDto {
        private String level; // 'beginner', 'intermediate', 'advanced'
        private String targetAudience;
        private String requirements;
        private BigDecimal price;
        private BigDecimal originalPrice;
    }

}
