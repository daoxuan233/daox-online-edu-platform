package com.daox.online.entity.views.responseVO.teacher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 教师待办事项响应对象
 * 用于返回前端展示所需的待办事项数据结构
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TeacherTodoVo {

    /**
     * 待办事项ID
     */
    private String id;

    /**
     * 待办事项内容
     */
    private String content;

    /**
     * 优先级，high / medium / low
     */
    private String priority;

    /**
     * 是否已完成
     */
    private Boolean completed;

    /**
     * 截止时间
     */
    private Date dueDate;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
