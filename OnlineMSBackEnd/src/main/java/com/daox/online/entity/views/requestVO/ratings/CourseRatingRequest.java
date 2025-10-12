package com.daox.online.entity.views.requestVO.ratings;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 课程评分请求对象
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class CourseRatingRequest {
    @NotBlank(message = "课程ID不能为空")
    private String courseId;

    @NotNull(message = "总体评分不能为空")
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    private Integer overallRating;

    @Min(value = 1, message = "内容质量评分不能小于1")
    @Max(value = 5, message = "内容质量评分不能大于5")
    private Integer contentQuality;

    @Min(value = 1, message = "难度评分不能小于1")
    @Max(value = 5, message = "难度评分不能大于5")
    private Double difficultyLevel;

    @Min(value = 1, message = "实用性评分不能小于1")
    @Max(value = 5, message = "实用性评分不能大于5")
    private Integer practicality;

    @Size(max = 1000, message = "评价内容不能超过1000字")
    private String comment;

    /**
     * 是否匿名
     */
    private Boolean isAnonymous = false;
}