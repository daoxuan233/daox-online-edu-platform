package com.daox.online.mapper;

import com.daox.online.entity.dto.CourseSearchDTO;
import com.daox.online.entity.views.responseVO.course.CourseVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseSearchMapper {
    /**
     * 根据关键词进行全文检索，并只查询已发布的、公开的、未删除的课程
     * @param keyword 经过布尔模式处理的关键词
     * @return 匹配的课程列表，按相关性排序
     */
    List<CourseSearchDTO> searchPublishedCourses(@Param("keyword") String keyword);

    /**
     * 根据分类ID列表查询课程（带分页）
     * @param categoryIds 分类ID列表
     * @return 课程列表
     */
    List<CourseSearchDTO> findPublishedCoursesByCategories(@Param("categoryIds") List<String> categoryIds);

    /**
     * 根据课程难度等级查询已发布的、公开的、未删除的课程信息
     * @param level 难度等级 (e.g., 'beginner', 'intermediate', 'advanced')
     * @return 封装好的课程预览信息列表
     */
    List<CourseVo> findPublishedCoursesByLevel(@Param("level") String level);
}
