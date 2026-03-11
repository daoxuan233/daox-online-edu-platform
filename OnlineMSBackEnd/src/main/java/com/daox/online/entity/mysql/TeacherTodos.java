package com.daox.online.entity.mysql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 教师待办事项表实体<br />
 * 用于存储教师在工作台中维护的待办事项数据，支持新增、修改、删除、完成状态变更等业务能力<br />
 * TableName: teacher_todos
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TeacherTodos implements Serializable {

    /**
     * 待办事项主键ID
     */
    private String id;

    /**
     * 所属教师ID
     */
    private String teacherId;

    /**
     * 待办事项内容
     */
    private String content;

    /**
     * 优先级，支持 high / medium / low
     */
    private String priority;

    /**
     * 是否已完成，0-未完成，1-已完成
     */
    private Integer completed;

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

    /**
     * 逻辑删除标记，0-正常，1-已删除
     */
    private Integer isDeleted;
}
