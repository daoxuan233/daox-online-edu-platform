package com.daox.online.mapper;

import com.daox.online.entity.mysql.CourseProperties;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CoursePropertiesMapper {

    /**
     * 新增一条完整的课程属性记录
     * @param record 包含所有字段的 CourseProperties 对象
     * @return 影响的行数
     */
    int insert(CourseProperties record);

    /**
     * 选择性地新增一条课程属性记录 (只插入非空字段)
     * @param record 可能包含部分字段的 CourseProperties 对象
     * @return 影响的行数
     */
    int insertSelective(CourseProperties record);

    /**
     * 根据主键 (id) 查询课程属性
     * @param id 属性表的主键 ID
     * @return 课程属性对象
     */
    CourseProperties selectByPrimaryKey(String id);

    /**
     * 根据外键 (course_id) 查询课程属性，这是最常用的查询方法
     * @param courseId 课程的 ID
     * @return 课程属性对象
     */
    CourseProperties selectByCourseId(String courseId);

    /**
     * 根据主键 (id) 选择性地更新课程属性 (只更新非空字段)
     * @param record 包含要更新字段的 CourseProperties 对象
     * @return 影响的行数
     */
    int updateByPrimaryKeySelective(CourseProperties record);

    /**
     * 根据外键 (course_id) 选择性地更新课程属性 (只更新非空字段)
     * @param record 包含 courseId 和要更新字段的 CourseProperties 对象
     * @return 影响的行数
     */
    int updateByCourseIdSelective(CourseProperties record);

    /**
     * 根据主键 (id) 删除课程属性
     * @param id 属性表的主键 ID
     * @return 影响的行数
     */
    int deleteByPrimaryKey(String id);

    /**
     * 根据外键 (course_id) 删除课程属性
     * @param courseId 课程的 ID
     * @return 影响的行数
     */
    int deleteByCourseId(String courseId);
}
