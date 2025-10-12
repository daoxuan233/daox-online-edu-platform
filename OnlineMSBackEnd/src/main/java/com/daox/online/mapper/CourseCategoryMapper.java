package com.daox.online.mapper;

import com.daox.online.entity.mysql.CourseCategories;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseCategoryMapper {
    /**
     * 使用递归CTE查询指定分类ID及其所有子孙分类的ID
     *
     * @param categoryId 父分类ID
     * @return 包含父分类ID自身和所有子孙分类ID的列表
     */
    List<String> findSelfAndDescendantIds(@Param("categoryId") String categoryId);

    /**
     * 查询所有的课程分类
     *
     * @return 扁平的分类列表
     */
    List<CourseCategories> findAll();

}
