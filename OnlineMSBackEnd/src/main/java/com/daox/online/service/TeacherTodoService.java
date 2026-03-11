package com.daox.online.service;

import com.daox.online.entity.views.requestVO.teacher.CreateTeacherTodoRequest;
import com.daox.online.entity.views.requestVO.teacher.UpdateTeacherTodoRequest;
import com.daox.online.entity.views.responseVO.teacher.TeacherTodoVo;

import java.util.List;

/**
 * 教师待办事项服务接口
 * 提供待办事项查询、新增、修改、删除业务能力
 */
public interface TeacherTodoService {

    /**
     * 查询指定教师的全部待办事项
     *
     * @param teacherId 教师ID
     * @return 待办事项列表
     */
    List<TeacherTodoVo> listTeacherTodos(String teacherId);

    /**
     * 创建教师待办事项
     *
     * @param teacherId 教师ID
     * @param request   创建请求参数
     * @return 创建后的待办事项
     */
    TeacherTodoVo createTeacherTodo(String teacherId, CreateTeacherTodoRequest request);

    /**
     * 更新指定教师的待办事项
     *
     * @param teacherId 教师ID
     * @param todoId    待办事项ID
     * @param request   更新请求参数
     * @return 更新后的待办事项
     */
    TeacherTodoVo updateTeacherTodo(String teacherId, String todoId, UpdateTeacherTodoRequest request);

    /**
     * 删除指定教师的待办事项（逻辑删除）
     *
     * @param teacherId 教师ID
     * @param todoId    待办事项ID
     * @return 删除是否成功
     */
    boolean deleteTeacherTodo(String teacherId, String todoId);
}
