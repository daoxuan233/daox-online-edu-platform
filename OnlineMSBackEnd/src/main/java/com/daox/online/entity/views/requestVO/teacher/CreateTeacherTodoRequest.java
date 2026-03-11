package com.daox.online.entity.views.requestVO.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 教师端创建待办事项请求参数
 * 用于接收前端新增待办事项时提交的数据
 */
@Getter
@Setter
public class CreateTeacherTodoRequest {

    /**
     * 待办事项内容
     * 不能为空，最大长度 255
     */
    @NotBlank(message = "待办事项内容不能为空")
    @Size(max = 255, message = "待办事项内容长度不能超过255个字符")
    private String content;

    /**
     * 优先级
     * 支持 high / medium / low
     */
    @Pattern(regexp = "^(high|medium|low)?$", message = "优先级仅支持 high、medium、low")
    private String priority;

    /**
     * 截止时间
     * 允许为空，若为空表示无截止时间
     */
    private Date dueDate;
}
