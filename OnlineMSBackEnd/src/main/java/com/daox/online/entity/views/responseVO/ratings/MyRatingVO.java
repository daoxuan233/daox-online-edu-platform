package com.daox.online.entity.views.responseVO.ratings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 我的评分记录视图对象
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MyRatingVO {
    private String ratingId;
    private String type;                // course/teacher
    private String courseId;
    private String courseName;
    private String teacherId;
    private String teacherName;
    private Integer overallRating;
    private String comment;
    private Boolean isAnonymous;
    private String ratingTime;
    private Boolean canModify;
}