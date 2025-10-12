package com.daox.online.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * 学习进度更新请求的数据传输对象 (DTO)。
 * 用于学生端在观看视频时，向后端上报当前的观看进度。
 *
 * @see com.daox.online.controller.students.LearningController#updateProgress(ProgressUpdateRequestDto)
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ProgressUpdateRequestDto {

    /**
     * 正在观看的小节的ID。
     * 这是一个必填项，用于定位要更新哪一条学习记录。
     * - @NotBlank: 确保小节ID不为null，且不为空字符串或纯空格。
     */
    @NotBlank(message = "小节ID不能为空")
    private String sectionId;

    /**
     * 当前的观看进度，单位为秒。
     * 这是一个必填项。
     * - @NotNull: 确保前端必须传递此参数。
     * - @PositiveOrZero: 确保观看时长不能为负数，可以是0。
     */
    @NotNull(message = "进度时长不能为空")
    @PositiveOrZero(message = "进度时长不能为负数")
    private Integer progressSeconds;
}
