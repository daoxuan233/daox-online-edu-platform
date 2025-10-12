package com.daox.online.entity.views.requestVO.ratings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 评分查询请求对象
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class RatingQueryRequest {
    private Integer page = 1;
    private Integer size = 10;
    private String type;                // course/teacher
    private String courseId;
    private String teacherId;
    private String userId;
    private Integer minRating;
    private Integer maxRating;
    private String startDate;
    private String endDate;
    private String keyword;
    private String sortBy = "created_at";
    private String sortOrder = "desc";
}