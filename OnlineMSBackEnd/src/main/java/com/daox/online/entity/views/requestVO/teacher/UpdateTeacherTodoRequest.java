package com.daox.online.entity.views.requestVO.teacher;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 教师端更新待办事项请求参数
 * 支持对待办内容、优先级、完成状态、截止时间进行局部更新
 */
@Getter
@Setter
public class UpdateTeacherTodoRequest {

    /**
     * 待办事项内容
     * 可选，若传入则长度不能超过 255
     */
    @Size(max = 255, message = "待办事项内容长度不能超过255个字符")
    private String content;

    /**
     * 优先级
     * 可选，若传入仅支持 high / medium / low
     */
    @Pattern(regexp = "^(high|medium|low)?$", message = "优先级仅支持 high、medium、low")
    private String priority;

    /**
     * 完成状态
     * 可选，true 表示已完成，false 表示未完成
     */
    private Boolean completed;

    /**
     * 截止时间
     * 可选，传入值将覆盖原截止时间
     */
    private Date dueDate;
}
