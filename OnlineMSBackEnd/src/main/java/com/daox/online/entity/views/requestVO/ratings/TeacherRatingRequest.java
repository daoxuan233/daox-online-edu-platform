package com.daox.online.entity.views.requestVO.ratings;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 讲师评分请求对象
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class TeacherRatingRequest {
    @NotBlank(message = "讲师ID不能为空")
    private String teacherId;

    @NotBlank(message = "课程ID不能为空")
    private String courseId;

    @NotNull(message = "总体评分不能为空")
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    private Integer overallRating;

    @Min(value = 1, message = "教学质量评分不能小于1")
    @Max(value = 5, message = "教学质量评分不能大于5")
    private Integer teachingQuality;

    @Min(value = 1, message = "互动性评分不能小于1")
    @Max(value = 5, message = "互动性评分不能大于5")
    private Integer interaction;

    @Min(value = 1, message = "专业性评分不能小于1")
    @Max(value = 5, message = "专业性评分不能大于5")
    private Integer professionalism;

    @Size(max = 1000, message = "评价内容不能超过1000字")
    private String comment;

    private Boolean isAnonymous = false;
}