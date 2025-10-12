package com.daox.online.entity.mysql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
* 评分维度配置表<br />
配置课程和讲师评分的各个维度，支持动态调整评分项目
 * TableName : rating_dimensions
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RatingDimensions implements Serializable {

    /**
    * 维度ID
    */
    private String id;
    /**
    * 目标类型 (course-课程, teacher-讲师)
    */
    private String targetType;
    /**
    * 维度键名 (如: content_quality, teaching_quality)
    */
    private String dimensionKey;
    /**
    * 显示名称
    */
    private String displayName;
    /**
    * 维度描述
    */
    private String description;
    /**
    * 是否必填 (0-可选, 1-必填)
    */
    private Integer isRequired;
    /**
    * 显示顺序
    */
    private Integer sortOrder;
    /**
    * 是否启用 (0-禁用, 1-启用)
    */
    private Integer isEnabled;

}
