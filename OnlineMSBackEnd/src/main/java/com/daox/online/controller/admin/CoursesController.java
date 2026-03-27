package com.daox.online.controller.admin;

import com.daox.online.entity.mysql.Courses;
import com.daox.online.entity.RestBean;
import com.daox.online.entity.views.requestVO.admin.AdminCategorySaveVO;
import com.daox.online.entity.views.responseVO.course.CourseCategoriesVo;
import com.daox.online.service.CoursesService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员 - 课程相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/course")
public class CoursesController {

    @Resource
    private CoursesService coursesService;

    /**
     * 获取所有课程
     *
     * @return 课程列表
     */
    @GetMapping("/list")
    public RestBean<List<Courses>> getCourseListAll() {
        List<Courses> courseListAll = coursesService.getCourseListAll();
        if (courseListAll == null || courseListAll.isEmpty()) {
            log.warn("[getCourseListAll.method]课程列表为空");
            return RestBean.failure(404, "课程列表为空");
        }
        return RestBean.success(courseListAll);
    }

    /**
     * 删除课程
     *
     * @param courseId 课程ID
     * @return 删除结果
     */
    @PostMapping("/delete")
    public RestBean<String> deleteCourse(@RequestParam("courseId") String courseId) {
        boolean result = coursesService.deleteCourse(courseId);
        if (!result) {
            log.warn("[deleteCourse.method]删除课程失败");
            return RestBean.failure(500, "删除课程失败");
        }
        return RestBean.success("课程删除成功");
    }

    /**
     * 获取课程分类信息 - 通过id查询
     *
     * @param id 分类id
     * @return 分类信息
     */
    @GetMapping("/categories")
    public RestBean<Map<String, Object>> getCourseCategoryById(@RequestParam String id) {
        Map<String, Object> courseCategory = coursesService.getCourseCategoryById(id);
        if (courseCategory == null) return RestBean.failure(404, "课程分类信息不存在!");
        return RestBean.success(courseCategory);
    }

    /**
     * 获取课程分类信息 - 树形结构
     *
     * @return 课程分类 <br />返回一个树形结构的课程分类列表，其中根分类在顶层，子分类嵌套在父分类的 children 列表中
     */
    @GetMapping("/categories/tree")
    public RestBean<List<CourseCategoriesVo>> getCourseCategoryTree() {
        List<CourseCategoriesVo> courseCategoriesTree = coursesService.getCourseCategoryTree();
        if (courseCategoriesTree == null) return RestBean.failure(404, "课程分类信息不存在!");
        return RestBean.success(courseCategoriesTree);
    }

    /**
     * 创建分类
     *
     * @param categorySaveVO 分类维护请求体
     * @return 创建结果
     */
    @PostMapping("/categories/create")
    public RestBean<String> createCourseCategory(@RequestBody AdminCategorySaveVO categorySaveVO) {
        boolean result = coursesService.createCourseCategory(
                categorySaveVO.getName(),
                categorySaveVO.getParentId(),
                categorySaveVO.getOrderIndex());
        if (!result) return RestBean.failure(500, "创建课程分类失败!");
        return RestBean.success("创建课程分类成功!");
    }

    /**
     * 更新分类
     *
     * @param id       分类id
     * @param categorySaveVO 分类维护请求体
     * @return 更新结果
     */
    @PostMapping("/categories/update")
    public RestBean<String> updateCourseCategory(@RequestParam String id,
                                                 @RequestBody AdminCategorySaveVO categorySaveVO) {
        boolean result = coursesService.updateCourseCategory(
                id,
                categorySaveVO.getName(),
                categorySaveVO.getParentId(),
                categorySaveVO.getOrderIndex());
        if (!result) return RestBean.failure(500, "更新课程分类失败!");
        return RestBean.success("更新课程分类成功!");
    }

    /**
     * 删除分类。
     *
     * @param id 分类ID
     * @return 删除结果
     */
    @PostMapping("/categories/delete")
    public RestBean<String> deleteCourseCategory(@RequestParam String id) {
        boolean result = coursesService.deleteCourseCategory(id);
        if (!result) return RestBean.failure(500, "删除课程分类失败，请确认分类下没有子分类和课程");
        return RestBean.success("删除课程分类成功!");
    }

}
