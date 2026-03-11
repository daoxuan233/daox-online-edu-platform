package com.daox.online.controller.teacher;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.views.requestVO.teacher.CreateTeacherTodoRequest;
import com.daox.online.entity.views.requestVO.teacher.UpdateTeacherTodoRequest;
import com.daox.online.entity.views.responseVO.teacher.TeacherTodoVo;
import com.daox.online.service.TeacherTodoService;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师端待办事项控制器
 * 对外提供待办事项的查询、新增、修改、删除接口
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/todos")
public class TeacherTodoController {

    @Resource
    private TeacherTodoService teacherTodoService;

    /**
     * 获取当前登录教师的待办事项列表
     *
     * @param request HTTP请求对象
     * @return 待办事项列表
     */
    @GetMapping
    public RestBean<List<TeacherTodoVo>> listTeacherTodos(HttpServletRequest request) {
        String teacherId = UserUtils.getCurrentUserId(request);
        if (teacherId == null) {
            log.warn("[TeacherTodoController.listTeacherTodos] 当前用户未认证");
            return RestBean.unauthorized("用户未认证");
        }
        List<TeacherTodoVo> todos = teacherTodoService.listTeacherTodos(teacherId);
        return RestBean.success(todos);
    }

    /**
     * 新增待办事项
     *
     * @param request    HTTP请求对象
     * @param todoRequest 创建请求参数
     * @return 新建后的待办事项
     */
    @PostMapping
    public RestBean<TeacherTodoVo> createTeacherTodo(HttpServletRequest request,
                                                     @Valid @RequestBody CreateTeacherTodoRequest todoRequest) {
        String teacherId = UserUtils.getCurrentUserId(request);
        if (teacherId == null) {
            log.warn("[TeacherTodoController.createTeacherTodo] 当前用户未认证");
            return RestBean.unauthorized("用户未认证");
        }
        TeacherTodoVo createdTodo = teacherTodoService.createTeacherTodo(teacherId, todoRequest);
        return RestBean.success(createdTodo);
    }

    /**
     * 修改待办事项
     *
     * @param request     HTTP请求对象
     * @param todoId      待办事项ID
     * @param todoRequest 更新请求参数
     * @return 更新后的待办事项
     */
    @PutMapping("/{todoId}")
    public RestBean<TeacherTodoVo> updateTeacherTodo(HttpServletRequest request,
                                                     @PathVariable("todoId") String todoId,
                                                     @Valid @RequestBody UpdateTeacherTodoRequest todoRequest) {
        String teacherId = UserUtils.getCurrentUserId(request);
        if (teacherId == null) {
            log.warn("[TeacherTodoController.updateTeacherTodo] 当前用户未认证");
            return RestBean.unauthorized("用户未认证");
        }
        TeacherTodoVo updatedTodo = teacherTodoService.updateTeacherTodo(teacherId, todoId, todoRequest);
        return RestBean.success(updatedTodo);
    }

    /**
     * 删除待办事项（逻辑删除）
     *
     * @param request HTTP请求对象
     * @param todoId  待办事项ID
     * @return 删除结果
     */
    @DeleteMapping("/{todoId}")
    public RestBean<Boolean> deleteTeacherTodo(HttpServletRequest request,
                                               @PathVariable("todoId") String todoId) {
        String teacherId = UserUtils.getCurrentUserId(request);
        if (teacherId == null) {
            log.warn("[TeacherTodoController.deleteTeacherTodo] 当前用户未认证");
            return RestBean.unauthorized("用户未认证");
        }
        boolean deleted = teacherTodoService.deleteTeacherTodo(teacherId, todoId);
        return RestBean.success(deleted);
    }
}
