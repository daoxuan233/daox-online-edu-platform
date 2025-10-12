package com.daox.online.entity.mysql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

import java.math.BigDecimal;
import java.util.Date;

/**
* 讲师评分表<br />
学生对讲师的多维度评分和评价，关联具体课程，支持匿名评分
 * TableName : teacher_ratings
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TeacherRatings implements Serializable {

    /**
    * 评分ID
    */
    private String id;
    /**
    * 评分用户ID
    */
    private String userId;
    /**
    * 讲师ID
    */
    private String teacherId;
    /**
    * 关联课程ID
    */
    private String courseId;
    /**
    * 总体评分 (1.0-5.0)
    */
    private BigDecimal overallRating;
    /**
    * 教学质量评分 (1.0-5.0)
    */
    private BigDecimal teachingQuality;
    /**
    * 互动性评分 (1.0-5.0)
    */
    private BigDecimal interaction;
    /**
    * 专业性评分 (1.0-5.0)
    */
    private BigDecimal professionalism;
    /**
    * 文字评价
    */
    private String comment;
    /**
    * 是否匿名评分 (0-实名, 1-匿名)
    */
    private Integer isAnonymous;
    /**
    * 创建时间
    */
    private Date createdAt;
    /**
    * 更新时间
    */
    private Date updatedAt;
    /**
    * 逻辑删除标志 (0-正常, 1-删除)
    */
    private Integer isDeleted;

}
