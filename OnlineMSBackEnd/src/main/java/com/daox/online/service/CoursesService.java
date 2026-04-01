package com.daox.online.service;

import com.daox.online.entity.mysql.CourseMaterials;
import com.daox.online.entity.mysql.Courses;
import com.daox.online.entity.dto.CourseCoreInfoDto;
import com.daox.online.entity.dto.CourseOutlineDto;
import com.daox.online.entity.views.requestVO.admin.AdminCategoryDeleteRequestVO;
import com.daox.online.entity.views.requestVO.admin.AdminCategoryMigrationRequestVO;
import com.daox.online.entity.views.requestVO.teacher.CoursePropertiesVo;
import com.daox.online.entity.views.requestVO.teacher.TeacherCourseVo;
import com.daox.online.entity.views.responseVO.admin.AdminCategoryDeleteResultVO;
import com.daox.online.entity.views.responseVO.admin.AdminCategoryOperationPreviewVO;
import com.daox.online.entity.views.responseVO.*;
import com.daox.online.entity.views.responseVO.course.*;
import com.daox.online.entity.views.responseVO.user.UserCoursesVo;

import java.util.List;
import java.util.Map;

/**
 * 课程服务接口
 */
public interface CoursesService {
    /**
     * 公开课程列表 - 分页查询
     *
     * @param pageSize 每页显示的记录数
     * @param offset   偏移量，用于分页查询
     * @return 公开课程列表
     */
    List<CourseVo> publicCourseList(int pageSize, int offset);

    /**
     * 获取课程详情
     *
     * @param courseId 课程ID
     * @return 课程详情
     */
    CourseVo getCourseDetail(String courseId);

    /**
     * 获取课程分类 - 树形结构
     *
     * @return 课程分类树形结构
     */
    List<CourseCategoriesVo> getCourseCategoryTree();

    /**
     * 获取课程详情 - 带课程分类信息
     *
     * @param courseId 课程ID
     * @return 课程详情
     */
    TeacherCourseDetailVo getCourseDetailWithCategories(String courseId);

    /**
     * 教师端 - 获取课程列表
     *
     * @param userId 用户ID
     * @return 课程列表
     */
    List<UserCoursesVo> getTeacherCourseList(String userId);


    /**
     * 获取我的课程列表 - 学生端
     *
     * @param userId 用户ID
     * @return 我的课程列表
     */
    List<StuUserCoursesVo> getStuMyCourseList(String userId);


    /**
     * 加入课程 - 已有课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 加入结果 true：成功，false：失败
     */
    boolean joinCourse(String userId, String courseId);

    boolean isUserEnrolledInCourse(String userId, String courseId);

    /**
     * 退出课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 退出结果 true：成功，false：失败
     */
    boolean quitCourse(String userId, String courseId);

    /**
     * 获取课程资料 - 所有课程资料
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     * @return 课程资料列表
     */
    List<CourseMaterials> getCourseMaterials(String userId, String courseId);

    /**
     * 获取课程资料 - 章节
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @return 课程资料列表
     */
    List<CourseMaterials> getCourseMaterialsByChapterId(String userId, String courseId, String chapterId);

    /**
     * 获取课程资料 - 小节
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @param sectionId 小节ID
     * @return 课程资料列表
     */
    List<CourseMaterials> getCourseMaterialsBySectionIdANDChapterId(String userId, String courseId, String chapterId, String sectionId);

    /**
     * 创建课程
     *
     * @param operatorId 当前教师ID
     * @param courseDto 课程信息
     * @return 创建结果
     */
    Courses createCourse(String operatorId, CourseCoreInfoDto courseDto);

    /**
     * 更新核心课程 信息
     *
     * @param operatorId 当前教师ID
     * @param courseId   课程id
     * @param courseDto  课程信息
     * @return 更新结果
     */
    Courses updateCourseCoreInfo(String operatorId, String courseId, CourseCoreInfoDto courseDto);

    /**
     * 全量更新课程大纲 --> 更新为: 增量更新课程大纲
     *
     * @param operatorId 当前教师ID
     * @param courseId   课程id
     * @param outlineDto 课程大纲
     */
    void updateCourseOutline(String operatorId, String courseId, CourseOutlineDto outlineDto);

    /**
     * 获取课程大纲
     *
     * @param courseId 课程id
     * @return 课程大纲
     */
    CourseOutlineDto getCourseOutline(String courseId);

    /**
     * 更新课程
     *
     * @param userId             用户ID
     * @param courseId           课程ID
     * @param courseVo           课程信息
     * @param coursePropertiesVo 课程属性信息
     * @return 创建结果
     */
    String updateCourse(String userId, String courseId, TeacherCourseVo courseVo, CoursePropertiesVo coursePropertiesVo);

    /**
     * 删除课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 删除结果 true：成功，false：失败
     */
    boolean deleteCourse(String userId, String courseId);

    /**
     * 教师提交课程审核。
     *
     * @param teacherId 教师ID
     * @param courseId  课程ID
     */
    void submitCourseForReview(String teacherId, String courseId);

    /**
     * 教师归档草稿课程。
     *
     * @param teacherId 教师ID
     * @param courseId  课程ID
     */
    void archiveCourseByTeacher(String teacherId, String courseId);

    /**
     * 获取选课学生
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 选课学生列表
     */
    List<EnrolledStudentVO> getStudentList(String userId, String courseId);

    /**
     * 获取所有课程 - 管理员
     *
     * @return 课程列表
     */
    List<Courses> getCourseListAll();

    /**
     * 获取全部待审核课程。
     *
     * @return 待审核课程列表
     */
    List<Courses> listPendingReviewCourses();

    /**
     * 管理员审核通过课程。
     *
     * @param adminId  管理员ID
     * @param courseId 课程ID
     */
    void approveCourseReview(String adminId, String courseId);

    /**
     * 管理员驳回课程审核。
     *
     * @param adminId  管理员ID
     * @param courseId 课程ID
     * @param comment  审核意见
     */
    void rejectCourseReview(String adminId, String courseId, String comment);

    /**
     * 管理员下架已发布课程。
     *
     * @param adminId  管理员ID
     * @param courseId 课程ID
     * @param comment  下架说明
     */
    void takeDownCourse(String adminId, String courseId, String comment);

    /**
     * 管理员重新上架课程。
     *
     * @param adminId  管理员ID
     * @param courseId 课程ID
     */
    void republishCourse(String adminId, String courseId);

    /**
     * 管理员归档已下架课程。
     *
     * @param adminId  管理员ID
     * @param courseId 课程ID
     * @param comment  归档说明
     */
    void archiveCourseByAdmin(String adminId, String courseId, String comment);

    /**
     * 删除课程 - 管理员
     *
     * @param courseId 课程ID
     * @return 删除结果 true：成功，false：失败
     */
    boolean deleteCourse(String courseId);

    /**
     * 获取课程分类信息 - 通过id查询
     *
     * @param id 分类id
     * @return 分类信息
     */
    Map<String, Object> getCourseCategoryById(String id);

    /**
     * 创建课程分类
     *
     * @param name     分类名称
     * @param parentId 父级分类id
     * @param orderIndex 排序权重
     * @return 创建结果 true：成功，false：失败
     */
    boolean createCourseCategory(String name, String parentId, Integer orderIndex);

    /**
     * 获取用户已完成课程数量
     *
     * @param userId 用户ID
     * @return 已完成课程数量
     */
    Integer getCompletedCoursesCount(String userId);

    /**
     * 更新课程分类
     *
     * @param id       分类id
     * @param name     分类名称
     * @param parentId 父级分类id
     * @param orderIndex 排序权重
     * @return 更新结果 true：成功，false：失败
     */
    boolean updateCourseCategory(String id, String name, String parentId, Integer orderIndex);

    /**
     * 删除课程分类
     *
     * @param id 分类id
     * @return 删除结果 true：成功，false：失败
     */
    //TODO: 2025/7/27 删除课程分类 逻辑待完善
    boolean deleteCourseCategory(String id);

    /**
     * 预览分类删除影响范围。
     *
     * @param categoryId 分类ID
     * @return 预览信息
     */
    AdminCategoryOperationPreviewVO previewDeleteCourseCategory(String categoryId);

    /**
     * 执行紧急删除。
     *
     * @param operatorId 当前管理员ID
     * @param request    删除请求
     * @return 删除结果
     */
    AdminCategoryDeleteResultVO emergencyDeleteCourseCategory(String operatorId, AdminCategoryDeleteRequestVO request);

    /**
     * 提交常规删除申请。
     *
     * @param operatorId 当前管理员ID
     * @param request    删除请求
     * @return 删除结果
     */
    AdminCategoryDeleteResultVO regularDeleteCourseCategory(String operatorId, AdminCategoryDeleteRequestVO request);

    /**
     * 预览分类迁移影响范围。
     *
     * @param categoryId 分类ID
     * @return 预览信息
     */
    AdminCategoryOperationPreviewVO previewCategoryMigration(String categoryId);

    /**
     * 执行分类迁移。
     *
     * @param operatorId 当前管理员ID
     * @param request    迁移请求
     * @return 迁移结果
     */
    AdminCategoryDeleteResultVO migrateCategoryCourses(String operatorId, AdminCategoryMigrationRequestVO request);

    /**
     * 处理 Redis 中待完成的常规删除任务。
     */
    void processPendingCategoryDeletionTasks();

    /**
     * 获取课程统计
     *
     * @return 课程统计
     */
    int getCourseCount();

    /**
     * 获取详细课程统计
     *
     * @return 详细课程统计信息
     */
    CourseDetailedStatisticsVo getCourseDetailedStatistics();

    /**
     * 课程搜索 - 关键词搜索
     *
     * @param keyword 关键词
     * @return 课程列表
     */
    List<CourseVo> searchCourses(String keyword);

    /**
     * 课程搜索 - 分类搜索
     *
     * @param categoryId 分类id
     * @return 课程列表
     */
    List<CourseVo> searchCoursesByCategory(String categoryId);

    /**
     * 课程搜索 - 难度搜索
     *
     * @param level 难度
     * @return 课程列表
     */
    List<CourseVo> searchCoursesByLevel(String level);
}
