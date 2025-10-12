package com.daox.online.entity.views.responseVO.ratings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理员评分查询VO
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class AdminRatingVO {
    /**
     * 评分ID
     */
    private String ratingId;
    /**
     * 评分类型
     */
    private String ratingType;
    /**
     * 目标名称（课程名或讲师名）
     */
    private String targetName;
    /**
     * 评分用户名
     */
    private String userName;
    /**
     * 总体评分
     */
    private BigDecimal overallRating;
    /**
     * 文字评价
     */
    private String comment;
    /**
     * 是否匿名
     */
    private Boolean isAnonymous;
    /**
     * 评分时间
     */
    private Date createdAt;
    /**
     * 状态（正常/已删除）
     */
    private String status;
}
