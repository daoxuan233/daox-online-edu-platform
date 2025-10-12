package com.daox.online.entity.views.responseVO.ratings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 课程评价权限
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class RatingPermissionVO {
    private String userId;                    // 用户ID
    private String courseId;                  // 课程ID
    private Boolean canRate;                  // 是否可以评分
    private String reason;                    // 原因说明
    private BigDecimal currentProgress;       // 当前学习进度
    private BigDecimal requiredProgress;      // 要求的最小进度
}