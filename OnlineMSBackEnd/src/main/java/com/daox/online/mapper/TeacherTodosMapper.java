package com.daox.online.mapper;

import com.daox.online.entity.mysql.TeacherTodos;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 教师待办事项 Mapper 接口
 * 负责 teacher_todos 表的增删改查数据访问
 */
@Mapper
public interface TeacherTodosMapper {

    /**
     * 根据教师ID查询其全部未删除待办事项（按更新时间倒序）
     *
     * @param teacherId 教师ID
     * @return 待办事项列表
     */
    List<TeacherTodos> selectByTeacherId(@Param("teacherId") String teacherId);

    /**
     * 根据待办ID和教师ID查询单条待办事项
     *
     * @param id        待办事项ID
     * @param teacherId 教师ID
     * @return 待办事项实体，若不存在返回 null
     */
    TeacherTodos selectByIdAndTeacherId(@Param("id") String id, @Param("teacherId") String teacherId);

    /**
     * 新增待办事项（选择性插入）
     *
     * @param record 待办事项实体
     * @return 受影响行数
     */
    int insertSelective(TeacherTodos record);

    /**
     * 根据主键进行选择性更新
     *
     * @param record 待办事项实体
     * @return 受影响行数
     */
    int updateByPrimaryKeySelective(TeacherTodos record);

    /**
     * 按教师归属执行逻辑删除
     *
     * @param id        待办事项ID
     * @param teacherId 教师ID
     * @return 受影响行数
     */
    int softDeleteByIdAndTeacherId(@Param("id") String id, @Param("teacherId") String teacherId);
}
