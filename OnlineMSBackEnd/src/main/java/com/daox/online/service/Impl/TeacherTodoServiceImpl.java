package com.daox.online.service.Impl;

import com.daox.online.controller.exception.BusinessException;
import com.daox.online.entity.mysql.TeacherTodos;
import com.daox.online.entity.views.requestVO.teacher.CreateTeacherTodoRequest;
import com.daox.online.entity.views.requestVO.teacher.UpdateTeacherTodoRequest;
import com.daox.online.entity.views.responseVO.teacher.TeacherTodoVo;
import com.daox.online.mapper.TeacherTodosMapper;
import com.daox.online.service.TeacherTodoService;
import com.daox.online.uilts.HybridIdGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 教师待办事项服务实现类
 * 承担教师待办事项的核心业务处理，包括输入校验、权限归属校验、数据转换和持久化操作
 */
@Slf4j
@Service
public class TeacherTodoServiceImpl implements TeacherTodoService {

    /**
     * 支持的优先级集合
     */
    private static final Set<String> ALLOWED_PRIORITY = Set.of("high", "medium", "low");

    /**
     * 默认优先级
     */
    private static final String DEFAULT_PRIORITY = "medium";

    @Resource
    private TeacherTodosMapper teacherTodosMapper;

    @Resource
    private HybridIdGenerator hybridIdGenerator;

    /**
     * 查询指定教师的全部待办事项
     *
     * @param teacherId 教师ID
     * @return 待办事项列表
     */
    @Override
    public List<TeacherTodoVo> listTeacherTodos(String teacherId) {
        if (teacherId == null || teacherId.isBlank()) {
            throw new BusinessException("401", "用户未认证，无法查询待办事项");
        }
        return teacherTodosMapper.selectByTeacherId(teacherId).stream()
                .map(this::convertToVo)
                .toList();
    }

    /**
     * 创建教师待办事项
     *
     * @param teacherId 教师ID
     * @param request   创建请求参数
     * @return 创建后的待办事项
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeacherTodoVo createTeacherTodo(String teacherId, CreateTeacherTodoRequest request) {
        if (teacherId == null || teacherId.isBlank()) {
            throw new BusinessException("401", "用户未认证，无法创建待办事项");
        }
        String content = normalizeContent(request.getContent());
        String priority = normalizePriority(request.getPriority());

        TeacherTodos todo = new TeacherTodos()
                .setId(hybridIdGenerator.generateId())
                .setTeacherId(teacherId)
                .setContent(content)
                .setPriority(priority)
                .setCompleted(0)
                .setDueDate(request.getDueDate())
                .setIsDeleted(0);

        int affectedRows = teacherTodosMapper.insertSelective(todo);
        if (affectedRows <= 0) {
            throw new BusinessException("500", "创建待办事项失败，请稍后重试");
        }
        TeacherTodos createdTodo = teacherTodosMapper.selectByIdAndTeacherId(todo.getId(), teacherId);
        if (createdTodo == null) {
            throw new BusinessException("500", "创建待办事项后读取数据失败");
        }
        return convertToVo(createdTodo);
    }

    /**
     * 更新指定教师的待办事项
     *
     * @param teacherId 教师ID
     * @param todoId    待办事项ID
     * @param request   更新请求参数
     * @return 更新后的待办事项
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeacherTodoVo updateTeacherTodo(String teacherId, String todoId, UpdateTeacherTodoRequest request) {
        if (teacherId == null || teacherId.isBlank()) {
            throw new BusinessException("401", "用户未认证，无法修改待办事项");
        }
        if (todoId == null || todoId.isBlank()) {
            throw new BusinessException("400", "待办事项ID不能为空");
        }
        TeacherTodos existingTodo = teacherTodosMapper.selectByIdAndTeacherId(todoId, teacherId);
        if (existingTodo == null) {
            throw new BusinessException("404", "待办事项不存在或无权限操作");
        }

        TeacherTodos updateRecord = new TeacherTodos()
                .setId(todoId)
                .setTeacherId(teacherId);

        boolean hasFieldToUpdate = false;

        if (request.getContent() != null) {
            updateRecord.setContent(normalizeContent(request.getContent()));
            hasFieldToUpdate = true;
        }
        if (request.getPriority() != null) {
            updateRecord.setPriority(normalizePriority(request.getPriority()));
            hasFieldToUpdate = true;
        }
        if (request.getCompleted() != null) {
            updateRecord.setCompleted(request.getCompleted() ? 1 : 0);
            hasFieldToUpdate = true;
        }
        if (request.getDueDate() != null) {
            updateRecord.setDueDate(request.getDueDate());
            hasFieldToUpdate = true;
        }
        if (!hasFieldToUpdate) {
            throw new BusinessException("400", "更新内容不能为空");
        }

        int affectedRows = teacherTodosMapper.updateByPrimaryKeySelective(updateRecord);
        if (affectedRows <= 0) {
            throw new BusinessException("500", "更新待办事项失败，请稍后重试");
        }

        TeacherTodos updatedTodo = teacherTodosMapper.selectByIdAndTeacherId(todoId, teacherId);
        if (updatedTodo == null) {
            throw new BusinessException("500", "更新待办事项后读取数据失败");
        }
        return convertToVo(updatedTodo);
    }

    /**
     * 删除指定教师的待办事项（逻辑删除）
     *
     * @param teacherId 教师ID
     * @param todoId    待办事项ID
     * @return 删除是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeacherTodo(String teacherId, String todoId) {
        if (teacherId == null || teacherId.isBlank()) {
            throw new BusinessException("401", "用户未认证，无法删除待办事项");
        }
        if (todoId == null || todoId.isBlank()) {
            throw new BusinessException("400", "待办事项ID不能为空");
        }
        int affectedRows = teacherTodosMapper.softDeleteByIdAndTeacherId(todoId, teacherId);
        if (affectedRows <= 0) {
            throw new BusinessException("404", "待办事项不存在或无权限操作");
        }
        return true;
    }

    /**
     * 标准化并校验待办内容
     *
     * @param content 原始内容
     * @return 规范化后的内容
     */
    private String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("400", "待办事项内容不能为空");
        }
        String normalizedContent = content.trim();
        if (normalizedContent.length() > 255) {
            throw new BusinessException("400", "待办事项内容长度不能超过255个字符");
        }
        return normalizedContent;
    }

    /**
     * 标准化并校验优先级
     *
     * @param priority 原始优先级
     * @return 规范化后的优先级
     */
    private String normalizePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return DEFAULT_PRIORITY;
        }
        String normalizedPriority = priority.trim().toLowerCase();
        if (!ALLOWED_PRIORITY.contains(normalizedPriority)) {
            log.warn("检测到非法优先级参数: {}", priority);
            throw new BusinessException("400", "优先级仅支持 high、medium、low");
        }
        return normalizedPriority;
    }

    /**
     * 将数据库实体转换为前端响应对象
     *
     * @param teacherTodo 数据库实体
     * @return 前端响应对象
     */
    private TeacherTodoVo convertToVo(TeacherTodos teacherTodo) {
        return new TeacherTodoVo()
                .setId(teacherTodo.getId())
                .setContent(teacherTodo.getContent())
                .setPriority(teacherTodo.getPriority())
                .setCompleted(teacherTodo.getCompleted() != null && teacherTodo.getCompleted() == 1)
                .setDueDate(teacherTodo.getDueDate())
                .setCreatedAt(teacherTodo.getCreatedAt())
                .setUpdatedAt(teacherTodo.getUpdatedAt());
    }
}
