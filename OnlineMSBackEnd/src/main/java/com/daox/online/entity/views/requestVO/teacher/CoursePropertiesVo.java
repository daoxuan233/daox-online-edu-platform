package com.daox.online.entity.views.requestVO.teacher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 课程属性VO
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class CoursePropertiesVo implements Serializable {
    /**
     * 课程id
     */
    private String courseId;
    /**
     * 课程难度等级 ('beginner' [初级], 'intermediate' [中级], 'advanced' [高级])
     */
    private String level;
    /**
     * 是否为新课程 (0表示FALSE，1表示TRUE)
     */
    private Integer isNew;
    /**
     * 适合人群列表
     */
    private String targetAudience;
    /**
     * 技术要求列表
     */
    private String requirements;
    /**
     * 当前价格
     */
    private BigDecimal price;
    /**
     * 原价，用于显示折扣
     */
    private BigDecimal originalPrice;

    @Override
    public String toString() {
        return "CoursePropertiesVo{" +
                "courseId='" + courseId + '\'' +
                ", level='" + level + '\'' +
                ", isNew=" + isNew +
                ", targetAudience='" + targetAudience + '\'' +
                ", requirements='" + requirements + '\'' +
                ", price=" + price +
                ", originalPrice=" + originalPrice +
                '}';
    }
}
