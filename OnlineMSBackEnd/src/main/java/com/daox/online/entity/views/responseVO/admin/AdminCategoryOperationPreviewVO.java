package com.daox.online.entity.views.responseVO.admin;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理员分类操作预览响应对象。
 * 用于在删除或迁移前向前端提供影响范围、风险提示和可选目标分类。
 */
@Getter
@Setter
@Accessors(chain = true)
public class AdminCategoryOperationPreviewVO {

    /**
     * 当前分类ID
     */
    private String categoryId;

    /**
     * 当前分类名称
     */
    private String categoryName;

    /**
     * 是否为顶级分类
     */
    private Boolean topLevel;

    /**
     * 是否建议优先执行迁移
     */
    private Boolean migrationRecommended;

    /**
     * 是否会级联删除子分类
     */
    private Boolean cascadeDeleteChildren;

    /**
     * 子孙分类数量
     */
    private Integer descendantCategoryCount;

    /**
     * 受影响课程数量
     */
    private Integer affectedCourseCount;

    /**
     * 受影响教师数量
     */
    private Integer affectedTeacherCount;

    /**
     * 是否允许紧急删除
     */
    private Boolean emergencyDeleteAllowed;

    /**
     * 是否允许常规删除
     */
    private Boolean regularDeleteAllowed;

    /**
     * 风险提示信息
     */
    private List<String> warnings = new ArrayList<>();

    /**
     * 受影响课程列表
     */
    private List<AffectedCourseItem> affectedCourses = new ArrayList<>();

    /**
     * 可迁移目标分类列表
     */
    private List<CategoryOptionItem> availableTargetCategories = new ArrayList<>();

    /**
     * 受影响课程摘要
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class AffectedCourseItem {

        /**
         * 课程ID
         */
        private String courseId;

        /**
         * 课程标题
         */
        private String courseTitle;

        /**
         * 教师ID
         */
        private String teacherId;

        /**
         * 教师名称
         */
        private String teacherName;

        /**
         * 教师邮箱
         */
        private String teacherEmail;

        /**
         * 当前课程分类ID
         */
        private String currentCategoryId;

        /**
         * 当前课程分类名称
         */
        private String currentCategoryName;
    }

    /**
     * 分类选项摘要
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class CategoryOptionItem {

        /**
         * 分类ID
         */
        private String categoryId;

        /**
         * 分类名称
         */
        private String categoryName;

        /**
         * 父分类ID
         */
        private String parentId;
    }
}
