package com.daox.online.service.Impl;

import com.daox.online.controller.exception.BusinessException;
import com.daox.online.entity.mysql.*;
import com.daox.online.entity.dto.CourseCoreInfoDto;
import com.daox.online.entity.dto.CourseOutlineDto;
import com.daox.online.entity.dto.CourseSearchDTO;
import com.daox.online.entity.views.BO.AdminCategoryPendingDeleteBO;
import com.daox.online.entity.views.requestVO.admin.AdminCategoryDeleteRequestVO;
import com.daox.online.entity.views.requestVO.admin.AdminCategoryMigrationRequestVO;
import com.daox.online.entity.views.requestVO.teacher.CreateTeacherTodoRequest;
import com.daox.online.entity.views.requestVO.teacher.CoursePropertiesVo;
import com.daox.online.entity.views.requestVO.teacher.TeacherCourseVo;
import com.daox.online.entity.views.responseVO.*;
import com.daox.online.entity.views.responseVO.admin.AdminCategoryDeleteResultVO;
import com.daox.online.entity.views.responseVO.admin.AdminCategoryOperationPreviewVO;
import com.daox.online.entity.views.responseVO.course.CourseCategoriesVo;
import com.daox.online.entity.views.responseVO.course.CourseDetailedStatisticsVo;
import com.daox.online.entity.views.responseVO.course.CourseVo;
import com.daox.online.entity.views.responseVO.course.TeacherCourseDetailVo;
import com.daox.online.entity.views.responseVO.teacher.TeacherTodoVo;
import com.daox.online.entity.views.responseVO.user.UserCoursesVo;
import com.daox.online.mapper.*;
import com.daox.online.service.AuditLogService;
import com.daox.online.service.CategoryNotificationService;
import com.daox.online.service.CoursesService;
import com.daox.online.service.NotificationCenterService;
import com.daox.online.service.SysUserService;
import com.daox.online.service.SystemAnnouncementsService;
import com.daox.online.service.TeacherTodoService;
import com.daox.online.service.UsersService;
import com.daox.online.uilts.constant.CourseLevel;
import com.daox.online.uilts.constant.CourseStatusFlow;
import com.daox.online.uilts.constant.Const;
import com.daox.online.uilts.constant.NotificationConstants;
import com.daox.online.uilts.HybridIdGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 课程服务实现类
 */
@Service
@Slf4j
public class CoursesServiceImpl implements CoursesService {

    private static final String CATEGORY_DELETE_MODE_EMERGENCY = "emergency";
    private static final String CATEGORY_DELETE_MODE_REGULAR = "regular";
    private static final String CATEGORY_OPERATION_DELETE = "delete";
    private static final String CATEGORY_OPERATION_MIGRATION = "migration";
    private static final int CATEGORY_REGULAR_DELETE_RETAIN_DAYS = 3;
    private static final int CATEGORY_NOTICE_EXPIRE_DAYS = 7;

    @Resource
    private CoursesMapper coursesMapper;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private HybridIdGenerator hybridIdGenerator;
    @Resource
    private UsersService usersService;
    @Resource
    private ChaptersANDSectionsMapper chaptersANDSectionsMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private LearningProgressMapper learningProgressMapper;
    @Resource
    private CourseSearchMapper courseSearchMapper;
    @Resource
    private CourseCategoryMapper courseCategoryMapper;
    @Resource
    private CoursePropertiesMapper coursePropertiesMapper;
    @Resource
    private ChapterMapper chapterMapper;
    @Resource
    private SectionMapper sectionMapper;
    @Resource
    private CourseMaterialsMapper courseMaterialsMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private AuditLogService auditLogService;
    @Resource
    private SystemAnnouncementsService systemAnnouncementsService;
    @Resource
    private TeacherTodoService teacherTodoService;
    @Resource
    private TeacherTodosMapper teacherTodosMapper;
    @Resource
    private SysUsersMapper sysUsersMapper;

    @Resource
    private CourseReviewLogMapper courseReviewLogMapper;

    @Resource
    private NotificationCenterService notificationCenterService;

    /**
     * 分类操作邮件通知服务（异步发送，事务提交后回调触发，不影响主业务事务）
     */
    @Resource
    private CategoryNotificationService categoryNotificationService;

    /**
     * 公开课程列表 - 分页查询
     *
     * @param pageSize 每页显示的记录数
     * @param offset   偏移量，用于分页查询
     * @return 公开课程列表
     */
    @Override
    public List<CourseVo> publicCourseList(int pageSize, int offset) {
        // 0、安全校验
        if (pageSize <= 0 || offset < 0) {
            pageSize = 10;
            offset = 0;
        }
        // 1、查询公开课程列表
        List<Courses> courses = coursesMapper.publicCourseList(pageSize, offset, Const.COURSE_STATUS_PUBLISHED);
        if (courses == null || courses.isEmpty()) {
            log.error("[publicCourseList.method]:查询公开课程列表时数据库操作失败: pageSize={}, offset={}", pageSize, offset);
            return Collections.emptyList();
        }
        List<CourseVo> courseVos = courses.stream().map(course -> new CourseVo().setCourseId(course.getId()).setCourseTitle(course.getTitle()).setCourseDescription(course.getDescription()).setTeacherId(course.getTeacherId()).setCategoryId(course.getCategoryId()).setCourseCover(course.getCoverImageUrl()).setCourseStatus(course.getStatus()).setCreateTime(course.getCreatedAt()).setUpdateTime(course.getUpdatedAt())).toList();
        // 2、 完善Vo查询教师姓名
        for (Courses course : courses) {
            // 1、1 查询教师姓名
            Users userById = sysUserService.findUserById(course.getTeacherId());
            if (userById == null) {
                log.error("[publicCourseList.method]:查询教师姓名时数据库操作失败: courseId={}", course.getId());
                return Collections.emptyList();
            } else {
                courseVos.forEach(courseVo -> {
                    if (courseVo.getTeacherId().equals(course.getTeacherId())) {
                        courseVo.setTeacherName(userById.getNickname());
                    }
                });
            }
            // 1、2 查询分类名称
            CourseCategories categoryById = coursesMapper.getCategoryById(course.getCategoryId());
            if (categoryById == null) {
                log.error("查询分类名称时数据库操作失败: courseId={}", course.getId());
                return Collections.emptyList();
            } else {
                courseVos.forEach(courseVo -> {
                    if (courseVo.getCategoryId().equals(course.getCategoryId())) {
                        courseVo.setCategoryName(categoryById.getName());
                    }
                });
            }
        }

        if (courseVos.isEmpty()) {
            log.error("查询课程列表时数据库操作失败: pageSize={}, offset={}", pageSize, offset);
            return Collections.emptyList();
        }
        return courseVos;
    }

    /**
     * 获取课程详情
     *
     * @param courseId 课程ID
     * @return 课程详情
     */
    @Override
    public CourseVo getCourseDetail(String courseId) {
        if (courseId == null || courseId.isEmpty()) {
            log.error("[publicCourseList.method]:课程ID不能为空");
            return null;
        }
        Courses courseById = coursesMapper.getCourseById(courseId);
        if (courseById == null) {
            log.error("[publicCourseList.method]:查询课程详情时数据库操作失败: courseId={}", courseId);
            return null;
        }
        Users userById = sysUserService.findUserById(courseById.getTeacherId());
        if (userById == null) {
            log.error("[getCourseDetail.method]查询教师姓名时数据库操作失败: courseId={}", courseId);
            return null;
        }
        CourseCategories categoryById = coursesMapper.getCategoryById(courseById.getCategoryId());
        if (categoryById == null) {
            log.error("[getCourseDetail.method]查询课程类别时数据库操作失败: courseId={}", courseId);
            return null;
        }
        return new CourseVo(courseById.getId(), courseById.getTitle(), courseById.getDescription(), courseById.getTeacherId(), userById.getNickname(), courseById.getCategoryId(), categoryById.getName(), courseById.getCoverImageUrl(), courseById.getStatus(), courseById.getCreatedAt(), courseById.getUpdatedAt());
    }

    /**
     * 获取课程分类 - 树形结构
     *
     * @return 课程分类树形结构
     */
    @Override
    public List<CourseCategoriesVo> getCourseCategoryTree() {
        List<CourseCategories> courseCategoriesTree = coursesMapper.getCourseCategoriesTree();
        if (courseCategoriesTree == null) {
            log.error("[getCourseCategoryTree.method]查询课程分类时数据库操作失败");
            return Collections.emptyList();
        }
        // 将所有分类转换为 CourseCategoriesVo 对象
        List<CourseCategoriesVo> allCategoriesVo = courseCategoriesTree.stream().map(this::convertToVo).collect(Collectors.toList());
        // 构建树形结构
        return buildCategoryTree(allCategoriesVo);
    }

    /**
     * 获取课程详情 - 带课程分类信息
     *
     * @param courseId 课程ID
     * @return 课程详情
     */
    @Override
    public TeacherCourseDetailVo getCourseDetailWithCategories(String courseId) {
        if (courseId == null || courseId.isEmpty()) {
            log.error("[getCourseDetailWithCategories.method]课程ID不能为空");
            return null;
        }
        Courses courseById = coursesMapper.getCourseById(courseId);
        if (courseById == null) {
            log.error("[getCourseDetailWithCategories.method]查询课程详情时数据库操作失败: courseId={}", courseId);
            return null;
        }
        // 获取分类名称
        CourseCategories categoryById = coursesMapper.getCategoryById(courseById.getCategoryId());
        if (categoryById == null) {
            log.error("[getCourseDetailWithCategories.method]查询课程类别时数据库操作失败: courseId={}", courseId);
            return null;
        }
        // 获取课程属性信息
        CourseProperties coursePropertiesById = coursesMapper.getCoursePropertiesById(courseById.getId());
        if (coursePropertiesById == null) {
            log.error("[getCourseDetailWithCategories.method]查询课程属性时数据库操作失败: courseId={}", courseId);
            return null;
        }
        return new TeacherCourseDetailVo()
                .setCourseId(courseById.getId())
                .setTitle(courseById.getTitle())
                .setDescription(courseById.getDescription())
                .setCover(courseById.getCoverImageUrl())
                .setCategories(categoryById.getName())
                .setStatus(courseById.getStatus())
                .setEnrollmentCount(courseById.getEnrollmentCount())
                .setIsDeleted(courseById.getIsDeleted())
                .setIsPrivate(courseById.getIsPrivate())
                .setLevel(coursePropertiesById.getLevel() == null ? "" : coursePropertiesById.getLevel())
                .setIsNew(coursePropertiesById.getIsNew() == null ? "1" : coursePropertiesById.getIsNew().toString())
                .setTargetAudience(coursePropertiesById.getTargetAudience() == null ? "" : coursePropertiesById.getTargetAudience())
                .setRequirements(coursePropertiesById.getRequirements() == null ? "" : coursePropertiesById.getRequirements())
                .setPrice(coursePropertiesById.getPrice() == null ? BigDecimal.ZERO : coursePropertiesById.getPrice())
                .setOriginalPrice(coursePropertiesById.getOriginalPrice() == null ? BigDecimal.ZERO : coursePropertiesById.getOriginalPrice());
    }

    /**
     * 教师端 - 获取课程列表
     *
     * @param userId 用户ID
     * @return 课程列表
     */
    @Override
    public List<UserCoursesVo> getTeacherCourseList(String userId) {
        // 定义缓存键
        String cacheKey = Const.COURSE_INFO_KEY + userId;

        // 1. 尝试从缓存中获取
        Object cachedData = redisTemplate.opsForValue().get(cacheKey);

        if (cachedData != null) {
            try {
                // 如果缓存命中，则反序列化并返回
                List<UserCoursesVo> UserCoursesVo = objectMapper.convertValue(cachedData, new TypeReference<List<UserCoursesVo>>() {
                });
                log.info("[getTeacherCourseList.method] 缓存命中，userId: {}", userId);
                return UserCoursesVo;
            } catch (Exception e) {
                log.error("[getTeacherCourseList.method] 反序列化缓存失败，userId: {}", userId, e);
            }
        }
        log.info("[getTeacherCourseList.method] 缓存未命中，查询数据库，userId: {}", userId);

        // 检查用户ID
        if (userId == null || userId.isEmpty()) {
            log.error("[getTeacherCourseList.method]用户ID不能为空");
            return Collections.emptyList();
        }
        Users userById = sysUserService.findUserById(userId);
        List<Courses> teacherCourseList = coursesMapper.getTeacherCourseList(userId);
        if (teacherCourseList == null || teacherCourseList.isEmpty()) {
            log.error("[getTeacherCourseList.method]查询课程列表时数据库操作失败: userId={}", userId);
            return Collections.emptyList();
        }
        List<UserCoursesVo> collect = teacherCourseList.stream().map(course -> {
            // 获取课程类别信息
            CourseCategories courseCategoryById = coursesMapper.getCategoryById(course.getCategoryId());
            if (courseCategoryById == null) {
                log.error("[getTeacherCourseList.method]查询课程类别信息失败: categoryId={}", course.getCategoryId());
                return null;
            }
            // 获取课程价格信息
            CourseProperties coursePropertiesById = coursesMapper.getCoursePropertiesById(course.getId());
            return new UserCoursesVo()
                    .setCourseId(course.getId())
                    .setCourseTitle(course.getTitle())
                    .setCourseDescription(course.getDescription())
                    .setTeacherId(course.getTeacherId())
                    .setTeacherName(userById.getNickname())
                    .setCategoryId(course.getCategoryId())
                    .setCategoryName(courseCategoryById.getName())
                    .setCourseCover(course.getCoverImageUrl())
                    .setCourseStatus(course.getStatus())
                    .setCreateTime(course.getCreatedAt())
                    .setCoursePrice(coursePropertiesById.getPrice())
                    ;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        // 3. 如果数据库查询结果非空，则存入缓存
        if (!collect.isEmpty()) {
            try {
                // 设置1小时的缓存过期时间
                redisTemplate.opsForValue().set(cacheKey, collect, Const.CACHE_TTL, TimeUnit.SECONDS);
                log.info("[getTeacherCourseList.method] 查询结果已存入缓存，userId: {}", userId);
            } catch (Exception e) {
                log.error("[getTeacherCourseList.method] 缓存题目列表失败，courseId: {}\n 原因:{}", userId, e.getMessage());
            }
        }
        return collect;
    }

    /**
     * 获取我的课程列表 - 学生端
     *
     * @param userId 用户ID
     * @return 我的课程列表
     */
    @Override
    public List<StuUserCoursesVo> getStuMyCourseList(String userId) {
        // 检查用户ID
        if (userId == null || userId.isEmpty()) {
            log.error("[getStuMyCourseList.method]用户ID不能为空");
            return Collections.emptyList();
        }
        // 检查用户是否存在
        Users userById = sysUserService.findUserById(userId);
        if (userById == null) {
            log.error("[getStuMyCourseList.method]查询用户信息时数据库操作失败: userId={}", userId);
            return Collections.emptyList();
        }
        // 查询我的课程列表
        List<UserCourses> myCourseList = coursesMapper.getMyCourseList(userId);
        if (myCourseList == null || myCourseList.isEmpty()) {
            log.error("[getStuMyCourseList.method]查询我的课程列表时数据库操作失败: userId={}", userId);
            return Collections.emptyList();
        }
        return myCourseList.stream().map(userCourse -> {
            // 获取课程信息
            Courses courseById = coursesMapper.getCourseById(userCourse.getCourseId());
            if (courseById == null) {
                log.error("[getStuMyCourseList.method]查询课程信息失败: courseId={}", userCourse.getCourseId());
                return null;
            }
            // 获取课程类别信息
            CourseCategories courseCategoryById = coursesMapper.getCategoryById(courseById.getCategoryId());
            if (courseCategoryById == null) {
                log.error("[getStuMyCourseList.method]查询课程类别信息失败: categoryId={}", courseById.getCategoryId());
                return null;
            }
            // 获取学习进度百分比
            Integer completedSectionsByUserIdAndCourseId = chaptersANDSectionsMapper.getCompletedSectionsByUserIdAndCourseId(userId, courseById.getId());
            Integer totalSectionsByCourseId = chaptersANDSectionsMapper.getTotalSectionsByCourseId(courseById.getId());
            Double progressPercentage = (completedSectionsByUserIdAndCourseId).doubleValue() / (totalSectionsByCourseId.doubleValue()) * 100;
            // 剩余学习时间
            Integer remainingLearningTime = null;
            Integer totalDurationByCourseId = chaptersANDSectionsMapper.getTotalDurationByCourseId(courseById.getId()); // 总学习时间
            if (totalDurationByCourseId == null) {
                totalDurationByCourseId = 0;
            }
            Integer totalLearningTimeByCourseId = learningProgressMapper.getTotalLearningTimeByCourseId(userId, courseById.getId());// 已学习时间
            if (totalLearningTimeByCourseId == null) {
                totalLearningTimeByCourseId = 0;
            }
            if (totalLearningTimeByCourseId > totalDurationByCourseId) {
                remainingLearningTime = totalLearningTimeByCourseId - totalDurationByCourseId;
            } else {
                remainingLearningTime = 0;
            }
            String courseStatusByUserIdAndCourseId = learningProgressMapper.getCourseStatusByUserIdAndCourseId(userId, courseById.getId());
            if (courseStatusByUserIdAndCourseId == null) {
                courseStatusByUserIdAndCourseId = Const.LEARNING_STATUS_NOT_START;
            }

            // 教师信息
            Users teacherById = sysUserService.findUserById(courseById.getTeacherId());
            if (teacherById == null) {
                log.error("[getStuMyCourseList.method]查询教师信息时数据库操作失败: teacherId={}", courseById.getTeacherId());
                return null;
            }
            return new StuUserCoursesVo()
                    .setCourseId(courseById.getId())
                    .setCourseTitle(courseById.getTitle())
                    .setCourseDescription(courseById.getDescription())
                    .setTeacherId(courseById.getTeacherId())
                    .setTeacherName(teacherById.getNickname())
                    .setCategoryId(courseCategoryById.getId())
                    .setCategoryName(courseCategoryById.getName())
                    .setCourseCover(courseById.getCoverImageUrl())
                    .setCourseStatus(courseStatusByUserIdAndCourseId)
                    .setCreateTime(courseById.getCreatedAt())
                    .setEnrollmentDate(userCourse.getEnrollmentDate())
                    .setProgressPercentage(progressPercentage)
                    .setRemainingTime(remainingLearningTime)
                    .setTotalSections(totalSectionsByCourseId)
                    ;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 加入课程 - 已有课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 加入结果 true：成功，false：失败
     */
    @Override
    public boolean joinCourse(String userId, String courseId) {
        if (isUserAlreadyEnrolled(userId, courseId)) {
            log.info("[joinCourse.method]用户已加入课程: userId={}, courseId={}", userId, courseId);
            return false;
        }
        String id = hybridIdGenerator.generateId();
        int i = coursesMapper.joinCourse(id, userId, courseId, new Date());
        if (i > 0) {
            log.info("[joinCourse.method]加入课程成功: userId={}, courseId={}", userId, courseId);
            Courses course = coursesMapper.getCourseById(courseId);
            Integer enrollmentCount = course != null ? course.getEnrollmentCount() : null;
            int nextCount = (enrollmentCount == null ? 0 : enrollmentCount) + 1;
            int i1 = coursesMapper.updateCourseEnrollmentCount(courseId, nextCount);
            if (i1 > 0) {
                log.info("[joinCourse.method]更新课程成功后课程学习人数+1: courseId={}", courseId);
                return true;
            }
            log.warn("[joinCourse.method]更新课程成功后课程学习人数+1失败: courseId={}", courseId);
            return false;
        }
        log.info("[joinCourse.method]加入课程失败: userId={}, courseId={}", userId, courseId);
        return false;
    }

    @Override
    public boolean isUserEnrolledInCourse(String userId, String courseId) {
        if (userId == null || userId.isEmpty() || courseId == null || courseId.isEmpty()) {
            return false;
        }
        return coursesMapper.isUserEnrolledInCourse(userId, courseId);
    }

    private boolean isUserAlreadyEnrolled(String userId, String courseId) {
        if (userId == null || userId.isEmpty() || courseId == null || courseId.isEmpty()) {
            return false;
        }
        return coursesMapper.isUserEnrolledInCourse(userId, courseId);
    }

    /**
     * 退出课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 退出结果 true：成功，false：失败
     */
    @Override
    public boolean quitCourse(String userId, String courseId) {
        int i = coursesMapper.quitCourse(userId, courseId);
        if (i > 0) {
            log.info("[quitCourse.method]退出课程成功: userId={}, courseId={}", userId, courseId);
            Courses course = coursesMapper.getCourseById(courseId);
            Integer enrollmentCount = course != null ? course.getEnrollmentCount() : null;
            int nextCount = (enrollmentCount == null ? 0 : enrollmentCount) - 1;
            if (nextCount < 0) {
                nextCount = 0;
            }
            int i1 = coursesMapper.updateCourseEnrollmentCount(courseId, nextCount);
            if (i1 > 0) {
                log.info("[quitCourse.method]更新课程报名人数成功: courseId={}", courseId);
            }
            log.info("[quitCourse.method]更新课程报名人数失败: courseId={}", courseId);
            // TODO [思考：是否应当还有逻辑，如退课发送信息等]
            return false;
        }
        log.info("[quitCourse.method]退出课程失败: userId={}, courseId={}", userId, courseId);
        return false;
    }

    /**
     * 获取课程资料 - 所有课程资料
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 课程资料列表
     */
    @Override
    public List<CourseMaterials> getCourseMaterials(String userId, String courseId) {
        // 参数校验
        if (userId == null || userId.isEmpty() || courseId == null || courseId.isEmpty()) {
            log.error("[getCourseMaterials.method] 参数错误 [null]: userId={}, courseId={}", userId, courseId);
            return Collections.emptyList();
        }

        // 检查用户是否已加入课程
        boolean isEnrolled = coursesMapper.isUserEnrolledInCourse(userId, courseId);
        if (!isEnrolled) {
            log.warn("[getCourseMaterials.method] 用户未加入该课程: userId={}, courseId={}", userId, courseId);
            return Collections.emptyList();
        }

        // 获取课程资料
        List<CourseMaterials> courseMaterials = coursesMapper.getCourseMaterials(courseId);
        if (courseMaterials == null || courseMaterials.isEmpty()) {
            log.info("[getCourseMaterials.method] 课程无资料: courseId={}", courseId);
            return Collections.emptyList();
        }

        return courseMaterials;
    }

    /**
     * 获取课程资料 - 章节
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @return 课程资料列表
     */
    @Override
    public List<CourseMaterials> getCourseMaterialsByChapterId(String userId, String courseId, String chapterId) {
        // 参数校验
        if (userId == null || userId.isEmpty() || courseId == null || courseId.isEmpty() || chapterId == null || chapterId.isEmpty()) {
            log.error("[getCourseMaterialsByChapterId.method] 参数错误: userId={}, courseId={}, chapterId={}", userId, courseId, chapterId);
            return Collections.emptyList();
        }

        // 检查用户是否已加入课程
        boolean isEnrolled = coursesMapper.isUserEnrolledInCourse(userId, courseId);
        if (!isEnrolled) {
            log.warn("[getCourseMaterialsByChapterId.method] 用户未加入该课程: userId={}, courseId={}", userId, courseId);
            return Collections.emptyList();
        }

        // 获取指定章节的课程资料
        List<CourseMaterials> chapterMaterials = coursesMapper.getCourseMaterialsByChapterId(courseId, chapterId);
        if (chapterMaterials == null || chapterMaterials.isEmpty()) {
            log.info("[getCourseMaterialsByChapterId.method] 该章节无资料: courseId={}, chapterId={}", courseId, chapterId);
            return Collections.emptyList();
        }

        return chapterMaterials;
    }

    /**
     * 获取课程资料 - 小节
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @param sectionId 小节ID
     * @return 课程资料列表
     */
    @Override
    public List<CourseMaterials> getCourseMaterialsBySectionIdANDChapterId(String userId, String courseId, String chapterId, String sectionId) {
        // 参数校验
        if (userId == null || userId.isEmpty() || courseId == null || courseId.isEmpty() || chapterId == null || chapterId.isEmpty() || sectionId == null || sectionId.isEmpty()) {
            log.error("[getCourseMaterialsBySectionIdANDChapterId.method] 参数错误: userId={}, courseId={}, chapterId={}, sectionId={}", userId, courseId, chapterId, sectionId);
            return Collections.emptyList();
        }

        // 检查用户是否已加入课程
        boolean isEnrolled = coursesMapper.isUserEnrolledInCourse(userId, courseId);
        if (!isEnrolled) {
            log.warn("[getCourseMaterialsBySectionIdANDChapterId.method] 用户未加入该课程: userId={}, courseId={}", userId, courseId);
            return Collections.emptyList();
        }

        // 获取指定小节的课程资料
        List<CourseMaterials> sectionMaterials = coursesMapper.getCourseMaterialsBySectionIdANDChapterId(courseId, chapterId, sectionId);
        if (sectionMaterials == null || sectionMaterials.isEmpty()) {
            log.info("[getCourseMaterialsBySectionIdANDChapterId.method] 该小节无资料: courseId={}, chapterId={}, sectionId={}", courseId, chapterId, sectionId);
            return Collections.emptyList();
        }

        return sectionMaterials;
    }

    /**
     * 创建课程核心信息。
     * {@code @Transactional} 注解保证了对 `courses` 和 `course_properties` 两个表的插入操作
     * 要么同时成功，要么同时失败回滚。
     */
    @Transactional
    @Override
    public Courses createCourse(String operatorId, CourseCoreInfoDto courseDto) {
        requireTeacherOperator(operatorId);
        if (courseDto == null) {
            throw new BusinessException("400", "课程信息不能为空");
        }
        // 1. 创建并保存 Course 实体
        Courses course = new Courses();
        String courseId = hybridIdGenerator.generateId(); // 在业务层生成唯一ID
        course.setId(courseId);
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setCoverImageUrl(courseDto.getCoverImageUrl());
        course.setTeacherId(operatorId);
        course.setCategoryId(courseDto.getCategoryId());
        course.setStatus(Const.COURSE_STATUS_DRAFT);
        course.setIsPrivate(courseDto.isPrivateCourse() ? 1 : 0);
        course.setEnrollmentCount(0);
        course.setCreatedAt(new Date());
        course.setUpdatedAt(new Date());
        course.setIsDeleted(0);
        // 使用 insertSelective 更为健壮
        courseMapper.insertSelective(course);
        // 2. 如果 DTO 中包含属性信息，则创建并保存 CourseProperties 实体
        CourseCoreInfoDto.CoursePropertiesDto propsDto = courseDto.getProperties();
        if (propsDto != null) {
            CourseProperties properties = new CourseProperties();
            properties.setId(UUID.randomUUID().toString());
            properties.setCourseId(courseId);
            properties.setLevel(propsDto.getLevel());
            properties.setTargetAudience(propsDto.getTargetAudience());
            properties.setRequirements(propsDto.getRequirements());
            properties.setPrice(propsDto.getPrice());
            properties.setOriginalPrice(propsDto.getOriginalPrice());
            // is_new 默认为1，可以根据需要设置
            properties.setIsNew(1);
            coursePropertiesMapper.insertSelective(properties);
        }
        refreshTeacherCourseCacheAfterCommit(course.getTeacherId());
        return course;
    }

    private void refreshTeacherCourseCache(String teacherId) {
        if (!StringUtils.hasText(teacherId)) {
            return;
        }
        String cacheKey = Const.COURSE_INFO_KEY + teacherId;
        try {
            redisTemplate.delete(cacheKey);
            getTeacherCourseList(teacherId);
        } catch (Exception e) {
            log.warn("[refreshTeacherCourseCache.method] 刷新教师课程缓存失败: teacherId={}", teacherId, e);
        }
    }

    /**
     * 在事务成功提交后刷新教师课程缓存，避免缓存先于事务提交生效。
     *
     * @param teacherId 教师ID
     */
    private void refreshTeacherCourseCacheAfterCommit(String teacherId) {
        if (!StringUtils.hasText(teacherId)) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            refreshTeacherCourseCache(teacherId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                refreshTeacherCourseCache(teacherId);
            }
        });
    }

    /**
     * 更新核心课程 信息
     * {@code @Transactional} 保证数据一致性。
     *
     * @param operatorId 当前教师ID
     * @param courseId   课程id
     * @param courseDto  课程信息
     * @return 更新结果
     */
    @Override
    @Transactional
    public Courses updateCourseCoreInfo(String operatorId, String courseId, CourseCoreInfoDto courseDto) {
        requireTeacherOperator(operatorId);
        if (courseDto == null) {
            throw new BusinessException("400", "课程信息不能为空");
        }
        Courses course = requireTeacherOwnedCourse(operatorId, courseId, false);
        CourseProperties existingProperties = coursePropertiesMapper.selectByCourseId(courseId);
        boolean coreChanged = !Objects.equals(course.getTitle(), courseDto.getTitle())
                || !Objects.equals(course.getDescription(), courseDto.getDescription())
                || !Objects.equals(course.getCoverImageUrl(), courseDto.getCoverImageUrl())
                || !Objects.equals(course.getCategoryId(), courseDto.getCategoryId())
                || !Objects.equals(course.getIsPrivate(), courseDto.isPrivateCourse() ? 1 : 0);

        // 2. 更新 Course 表
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setCoverImageUrl(courseDto.getCoverImageUrl());
        course.setCategoryId(courseDto.getCategoryId());
        course.setUpdatedAt(new Date());
        course.setIsPrivate(courseDto.isPrivateCourse() ? 1 : 0);
        course.setIsDeleted(0);
        courseMapper.updateByPrimaryKeySelective(course);

        // 3. 更新 CourseProperties 表
        CourseCoreInfoDto.CoursePropertiesDto propsDto = courseDto.getProperties();
    boolean propertiesChanged = propsDto != null && (existingProperties == null
        || !Objects.equals(existingProperties.getLevel(), propsDto.getLevel())
        || !Objects.equals(existingProperties.getTargetAudience(), propsDto.getTargetAudience())
        || !Objects.equals(existingProperties.getRequirements(), propsDto.getRequirements())
        || !Objects.equals(existingProperties.getPrice(), propsDto.getPrice())
        || !Objects.equals(existingProperties.getOriginalPrice(), propsDto.getOriginalPrice()));
        if (propsDto != null) {
            CourseProperties properties = coursePropertiesMapper.selectByCourseId(courseId);
            if (properties == null) {
                // 如果之前没有属性，则本次为新增
                properties = new CourseProperties();
                properties.setId(hybridIdGenerator.generateId());
                properties.setCourseId(courseId);
                properties.setLevel(propsDto.getLevel());
                properties.setTargetAudience(propsDto.getTargetAudience());
                properties.setRequirements(propsDto.getRequirements());
                properties.setPrice(propsDto.getPrice());
                properties.setOriginalPrice(propsDto.getOriginalPrice());
                coursePropertiesMapper.insertSelective(properties);
            } else {
                // 如果已有属性，则更新
                properties.setLevel(propsDto.getLevel());
                properties.setTargetAudience(propsDto.getTargetAudience());
                properties.setRequirements(propsDto.getRequirements());
                properties.setPrice(propsDto.getPrice());
                properties.setOriginalPrice(propsDto.getOriginalPrice());
                coursePropertiesMapper.updateByCourseIdSelective(properties);
            }
        }
        if (coreChanged || propertiesChanged) {
            notificationCenterService.notifyCourseChanged(
                    course,
                    sysUserService.findUserById(operatorId),
                    NotificationConstants.COURSE_ACTION_INFO_UPDATED
            );
            refreshTeacherCourseCacheAfterCommit(course.getTeacherId());
        }
        return course;
    }

    /**
     * 全量更新课程大纲 --> 更新为: 增量更新课程大纲
     *
     * @param operatorId 当前教师ID
     * @param courseId   课程id
     * @param outlineDto 课程大纲
     */
    @Transactional
    @Override
    public void updateCourseOutline(String operatorId, String courseId, CourseOutlineDto outlineDto) {
        requireTeacherOwnedCourse(operatorId, courseId, false);
        // 1. 获取数据库中的旧数据，并转为Map以便快速查找
        Map<String, Chapters> oldChaptersMap = chapterMapper.findByCourseId(courseId).stream()
                .collect(Collectors.toMap(Chapters::getId, Function.identity()));
        Map<String, Sections> oldSectionsMap = sectionMapper.findByCourseId(courseId).stream()
                .collect(Collectors.toMap(Sections::getId, Function.identity()));

        // 2. 准备用于分类处理的列表和集合
        List<Chapters> chaptersToInsert = new ArrayList<>();
        List<Chapters> chaptersToUpdate = new ArrayList<>();
        List<Sections> sectionsToInsert = new ArrayList<>();
        List<Sections> sectionsToUpdate = new ArrayList<>();
        Set<String> incomingChapterIds = new HashSet<>();
        Set<String> incomingSectionIds = new HashSet<>();

        // 3. 遍历前端数据，进行比对和分类
        if (outlineDto != null && !CollectionUtils.isEmpty(outlineDto.getOutline())) {
            for (CourseOutlineDto.ChapterDto chapterDto : outlineDto.getOutline()) {
                String chapterId = chapterDto.getId();
                Chapters oldChapter = StringUtils.hasText(chapterId) ? oldChaptersMap.get(chapterId) : null;
                if (oldChapter == null) {
                    Chapters newChapter = new Chapters();
                    chapterId = hybridIdGenerator.generateId();
                    newChapter.setId(chapterId);
                    newChapter.setCourseId(courseId);
                    newChapter.setTitle(chapterDto.getTitle());
                    newChapter.setOrderIndex(chapterDto.getOrderIndex());
                    chaptersToInsert.add(newChapter);
                } else {
                    incomingChapterIds.add(chapterId);
                    if (!Objects.equals(oldChapter.getTitle(), chapterDto.getTitle()) || !Objects.equals(oldChapter.getOrderIndex(), chapterDto.getOrderIndex())) {
                        oldChapter.setTitle(chapterDto.getTitle());
                        oldChapter.setOrderIndex(chapterDto.getOrderIndex());
                        chaptersToUpdate.add(oldChapter);
                    }
                }

                // 3.2 处理小节
                if (!CollectionUtils.isEmpty(chapterDto.getSections())) {
                    for (CourseOutlineDto.SectionDto sectionDto : chapterDto.getSections()) {
                        String sectionId = sectionDto.getId();
                        Sections oldSection = StringUtils.hasText(sectionId) ? oldSectionsMap.get(sectionId) : null;
                        if (oldSection == null) {
                            Sections newSection = new Sections();
                            newSection.setId(hybridIdGenerator.generateId());
                            newSection.setChapterId(chapterId);
                            newSection.setTitle(sectionDto.getTitle());
                            newSection.setVideoUrl(sectionDto.getVideoUrl());
                            newSection.setDurationSeconds(sectionDto.getDurationSeconds());
                            newSection.setOrderIndex(sectionDto.getOrderIndex());
                            sectionsToInsert.add(newSection);
                        } else {
                            incomingSectionIds.add(sectionId);
                            if (!Objects.equals(oldSection.getTitle(), sectionDto.getTitle())
                                    || !Objects.equals(oldSection.getOrderIndex(), sectionDto.getOrderIndex())
                                    || !Objects.equals(oldSection.getVideoUrl(), sectionDto.getVideoUrl())
                                    || !Objects.equals(oldSection.getDurationSeconds(), sectionDto.getDurationSeconds())) {
                                oldSection.setTitle(sectionDto.getTitle());
                                oldSection.setOrderIndex(sectionDto.getOrderIndex());
                                oldSection.setVideoUrl(sectionDto.getVideoUrl());
                                oldSection.setDurationSeconds(sectionDto.getDurationSeconds());
                                sectionsToUpdate.add(oldSection);
                            }
                        }
                    }
                }
            }
        }

        // 4. 计算需要删除的条目
        Set<String> chapterIdsToDelete = new HashSet<>(oldChaptersMap.keySet());
        chapterIdsToDelete.removeAll(incomingChapterIds);

        Set<String> sectionIdsToDelete = new HashSet<>(oldSectionsMap.keySet());
        sectionIdsToDelete.removeAll(incomingSectionIds);

        // 5. 按正确顺序执行数据库操作（删除 -> 更新 -> 新增）
        // 5.1 执行删除（必须先删除子表记录）
        if (!sectionIdsToDelete.isEmpty()) {
            // 【安全检查】
            if (courseMaterialsMapper.countBySectionIds(new ArrayList<>(sectionIdsToDelete)) > 0) {
                throw new RuntimeException("操作失败：无法删除已关联课程资料的小节。");
            }
            if (learningProgressMapper.countBySectionIds(new ArrayList<>(sectionIdsToDelete)) > 0) {
                throw new RuntimeException("操作失败：无法删除已有学生学习进度的小节。");
            }
            sectionMapper.deleteBatchByIds(new ArrayList<>(sectionIdsToDelete));
        }
        if (!chapterIdsToDelete.isEmpty()) {
            chapterMapper.deleteBatchByIds(new ArrayList<>(chapterIdsToDelete));
        }

        // 5.2 执行更新
        if (!chaptersToUpdate.isEmpty()) {
            chapterMapper.updateBatch(chaptersToUpdate);
        }
        if (!sectionsToUpdate.isEmpty()) {
            sectionMapper.updateBatch(sectionsToUpdate);
        }

        // 5.3 执行新增（必须先新增父表记录）
        if (!chaptersToInsert.isEmpty()) {
            chapterMapper.insertBatch(chaptersToInsert);
        }
        if (!sectionsToInsert.isEmpty()) {
            sectionMapper.insertBatch(sectionsToInsert);
        }
        boolean outlineChanged = !chaptersToInsert.isEmpty()
                || !chaptersToUpdate.isEmpty()
                || !sectionsToInsert.isEmpty()
                || !sectionsToUpdate.isEmpty()
                || !chapterIdsToDelete.isEmpty()
                || !sectionIdsToDelete.isEmpty();
        if (outlineChanged) {
            Courses course = courseMapper.selectByPrimaryKey(courseId);
            Users actor = course == null ? null : sysUserService.findUserById(course.getTeacherId());
            notificationCenterService.notifyCourseChanged(course, actor, NotificationConstants.COURSE_ACTION_OUTLINE_UPDATED);
        }
    }

    /**
     * 获取课程大纲
     *
     * @param courseId 课程id
     * @return 课程大纲
     */
    @Override
    public CourseOutlineDto getCourseOutline(String courseId) {
        // 1. 根据课程ID查询所有章节
        List<Chapters> chapters = chapterMapper.findByCourseId(courseId);
        if (CollectionUtils.isEmpty(chapters)) {
            return new CourseOutlineDto(new ArrayList<>());
        }

        // 2. 根据课程ID查询所有小节
        List<Sections> sections = sectionMapper.findByCourseId(courseId);

        // 3. 将小节按章节ID进行分组
        Map<String, List<Sections>> sectionsByChapterId = sections.stream()
                .collect(Collectors.groupingBy(Sections::getChapterId));

        // 4. 构建课程大纲DTO
        List<CourseOutlineDto.ChapterDto> chapterDtos = chapters.stream()
                .sorted(Comparator.comparingInt(Chapters::getOrderIndex)) // 按章节顺序排序
                .map(chapter -> {
                    CourseOutlineDto.ChapterDto chapterDto = new CourseOutlineDto.ChapterDto();
                    chapterDto.setId(chapter.getId());
                    chapterDto.setTitle(chapter.getTitle());
                    chapterDto.setOrderIndex(chapter.getOrderIndex());

                    // 获取当前章节下的小节列表，并按小节顺序排序
                    List<Sections> chapterSections = sectionsByChapterId.getOrDefault(chapter.getId(), new ArrayList<>());
                    List<CourseOutlineDto.SectionDto> sectionDtos = chapterSections.stream()
                            .sorted(Comparator.comparingInt(Sections::getOrderIndex))
                            .map(section -> {
                                CourseOutlineDto.SectionDto sectionDto = new CourseOutlineDto.SectionDto();
                                sectionDto.setId(section.getId());
                                sectionDto.setTitle(section.getTitle());
                                sectionDto.setVideoUrl(section.getVideoUrl());
                                Integer duration = section.getDurationSeconds();
                                sectionDto.setDurationSeconds(duration != null ? duration : 0);
                                sectionDto.setOrderIndex(section.getOrderIndex());
                                return sectionDto;
                            }).collect(Collectors.toList());
                    chapterDto.setSections(sectionDtos);
                    return chapterDto;
                }).collect(Collectors.toList());

        return new CourseOutlineDto(chapterDtos);
    }

    /**
     * 更新课程
     *
     * @param userId             用户ID
     * @param courseId           课程ID
     * @param courseVo           课程信息
     * @param coursePropertiesVo 课程属性信息
     * @return 更新结果提示
     */
    @Override
    @Transactional
    public String updateCourse(String userId, String courseId, TeacherCourseVo courseVo, CoursePropertiesVo coursePropertiesVo) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(courseId)) {
            throw new BusinessException("400", "课程ID不能为空");
        }
        if (courseVo == null) {
            throw new BusinessException("400", "课程信息不能为空");
        }
        if (coursePropertiesVo == null) {
            throw new BusinessException("400", "课程属性信息不能为空");
        }
        requireTeacherOperator(userId);

        Courses currentCourse = requireTeacherOwnedCourse(userId, courseId, false);
        CourseProperties existingProperties = coursesMapper.getCoursePropertiesById(courseId);
        Integer nextIsPrivate = courseVo.getIsPrivate() == null ? currentCourse.getIsPrivate() : courseVo.getIsPrivate();

        boolean courseChanged = !Objects.equals(currentCourse.getTitle(), courseVo.getTitle())
                || !Objects.equals(currentCourse.getDescription(), courseVo.getDescription())
                || !Objects.equals(currentCourse.getCoverImageUrl(), courseVo.getCoverImageUrl())
                || !Objects.equals(currentCourse.getCategoryId(), courseVo.getCategoryId())
                || !Objects.equals(currentCourse.getIsPrivate(), nextIsPrivate);

        if (courseChanged) {
            currentCourse.setTitle(courseVo.getTitle());
            currentCourse.setDescription(courseVo.getDescription());
            currentCourse.setCoverImageUrl(courseVo.getCoverImageUrl());
            currentCourse.setCategoryId(courseVo.getCategoryId());
            currentCourse.setIsPrivate(nextIsPrivate);
            currentCourse.setUpdatedAt(new Date());
            int courseUpdated = coursesMapper.updateCourse(currentCourse);
            if (courseUpdated <= 0) {
                throw new BusinessException("500", "课程更新失败，请稍后重试");
            }
        }

        boolean propertiesChanged = existingProperties == null
                || !Objects.equals(existingProperties.getLevel(), coursePropertiesVo.getLevel())
                || !Objects.equals(existingProperties.getIsNew(), coursePropertiesVo.getIsNew())
                || !Objects.equals(existingProperties.getTargetAudience(), coursePropertiesVo.getTargetAudience())
                || !Objects.equals(existingProperties.getRequirements(), coursePropertiesVo.getRequirements())
                || !Objects.equals(existingProperties.getPrice(), coursePropertiesVo.getPrice())
                || !Objects.equals(existingProperties.getOriginalPrice(), coursePropertiesVo.getOriginalPrice());

        if (propertiesChanged) {
            if (existingProperties == null) {
                int created = coursesMapper.createCourseProperties(new CourseProperties(
                        hybridIdGenerator.generateId(),
                        courseId,
                        coursePropertiesVo.getLevel(),
                        coursePropertiesVo.getIsNew(),
                        coursePropertiesVo.getTargetAudience(),
                        coursePropertiesVo.getRequirements(),
                        coursePropertiesVo.getPrice(),
                        coursePropertiesVo.getOriginalPrice())
                );
                if (created <= 0) {
                    throw new BusinessException("500", "课程属性创建失败，请稍后重试");
                }
            } else {
                int updated = coursesMapper.updateCourseProperties(new CourseProperties()
                        .setId(existingProperties.getId())
                        .setCourseId(courseId)
                        .setLevel(coursePropertiesVo.getLevel())
                        .setIsNew(coursePropertiesVo.getIsNew())
                        .setTargetAudience(coursePropertiesVo.getTargetAudience())
                        .setRequirements(coursePropertiesVo.getRequirements())
                        .setPrice(coursePropertiesVo.getPrice())
                        .setOriginalPrice(coursePropertiesVo.getOriginalPrice())
                );
                if (updated <= 0) {
                    throw new BusinessException("500", "课程属性更新失败，请稍后重试");
                }
            }
        }

        if (courseChanged || propertiesChanged) {
            notificationCenterService.notifyCourseChanged(
                    coursesMapper.getCourseById(courseId),
                    sysUserService.findUserById(userId),
                    NotificationConstants.COURSE_ACTION_INFO_UPDATED
            );
            refreshTeacherCourseCacheAfterCommit(currentCourse.getTeacherId());
        }
        return "更新完成";
    }


    /**
     * 删除课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 删除结果 true：成功，false：失败
     */
    @Override
    public boolean deleteCourse(String userId, String courseId) {
        if (courseId == null || courseId.isEmpty()) {
            log.warn("[deleteCourse] 课程id不能为空: courseId={}", courseId);
            return false;
        }
        if (!usersService.isTeacher(userId)) {
            log.warn("[deleteCourse] 用户不是教师: userId={}", userId);
            return false;
        }

        // 检查课程是否存在
        Courses courseById = coursesMapper.getCourseById(courseId);
        if (courseById == null) {
            log.warn("[deleteCourse] 课程不存在: courseId={}", courseId);
            return false;
        }

        List<Chapters> chapters = chaptersANDSectionsMapper.getChaptersByCourseId(courseId);
        Map<String, Object> beforeSnapshot = buildCourseAuditBeforeSnapshot(courseById, chapters);
        boolean success = true;
        int totalDeletedSections = 0;

        for (Chapters chapter : chapters) {
            int deletedSections = coursesMapper.deleteSections(chapter.getId());
            totalDeletedSections += Math.max(deletedSections, 0);
            log.info("[deleteCourse] 删除章节下的小节: chapterId={}, 删除数量={}", chapter.getId(), deletedSections);
            if (deletedSections == 0) {
                log.warn("[deleteCourse] 删除章节下的小节失败或无小节: chapterId={}", chapter.getId());
                success = false;
            }
        }

        // 删除章节
        int deletedChapters = coursesMapper.deleteChapters(courseId);
        log.info("[deleteCourse] 删除章节: courseId={}, 删除数量={}", courseId, deletedChapters);
        if (deletedChapters == 0) {
            log.warn("[deleteCourse] 删除章节失败或无章节: courseId={}", courseId);
            success = false;
        }

        // 删除课程资料
        int deletedMaterials = coursesMapper.deleteCourseAndMaterials(courseId);
        log.info("[deleteCourse] 删除课程资料: courseId={}, 删除数量={}", courseId, deletedMaterials);
        if (deletedMaterials == 0) {
            log.warn("[deleteCourse] 删除课程资料失败或无资料: courseId={}", courseId);
            success = false;
        }

        // 删除用户和课程关系
        int deletedUserCourses = coursesMapper.deleteUserCourses(courseId);
        log.info("[deleteCourse] 删除用户和课程关系: courseId={}, 删除数量={}", courseId, deletedUserCourses);
        if (deletedUserCourses == 0) {
            log.warn("[deleteCourse] 删除用户和课程关系失败或无关系: courseId={}", courseId);
            success = false;
        }

        // 删除课程
        int deletedCourse = coursesMapper.deleteCourse(courseId);
        log.info("[deleteCourse] 删除课程: courseId={}, 删除结果={}", courseId, deletedCourse > 0);
        if (deletedCourse == 0) {
            log.error("[deleteCourse] 删除课程失败: courseId={}", courseId);
            success = false;
        }

        if (success) {
            log.info("[deleteCourse] 课程及相关数据删除成功: userId={}, courseId={}", userId, courseId);
        } else {
            log.warn("[deleteCourse] 课程或部分相关数据删除失败: userId={}, courseId={}", userId, courseId);
        }

        auditLogService.recordSensitiveOperation(
                "COURSE_DELETE",
                "course",
                courseId,
                beforeSnapshot,
                buildCourseAuditAfterSnapshot(success, totalDeletedSections, deletedChapters, deletedMaterials, deletedUserCourses, deletedCourse),
                success ? "SUCCESS" : "FAILED",
                success ? "教师删除课程成功" : "教师删除课程存在部分失败");
        return success;
    }

    /**
     * 教师提交课程审核。
     *
     * @param teacherId 教师ID
     * @param courseId  课程ID
     */
    @Override
    @Transactional
    public void submitCourseForReview(String teacherId, String courseId) {
        Courses course = requireTeacherOwnedCourse(teacherId, courseId, true);
        transitionCourseStatus(course, CourseStatusFlow.ACTION_SUBMIT_REVIEW, teacherId, Const.ROLE_TEACHERS, null);
        refreshTeacherCourseCacheAfterCommit(course.getTeacherId());
    }

    /**
     * 教师归档草稿课程。
     *
     * @param teacherId 教师ID
     * @param courseId  课程ID
     */
    @Override
    @Transactional
    public void archiveCourseByTeacher(String teacherId, String courseId) {
        Courses course = requireTeacherOwnedCourse(teacherId, courseId, true);
        transitionCourseStatus(course, CourseStatusFlow.ACTION_ARCHIVE, teacherId, Const.ROLE_TEACHERS, null);
        refreshTeacherCourseCacheAfterCommit(course.getTeacherId());
    }

    /**
     * 管理员审核通过课程。
     *
     * @param adminId  管理员ID
     * @param courseId 课程ID
     */
    @Override
    @Transactional
    public void approveCourseReview(String adminId, String courseId) {
        Courses course = requireCourseForAdmin(adminId, courseId, true);
        transitionCourseStatus(course, CourseStatusFlow.ACTION_APPROVE, adminId, Const.ROLE_ADMIN, null);
        Users actor = sysUserService.findUserById(adminId);
        notificationCenterService.notifyCourseReviewResult(course, actor, NotificationConstants.COURSE_ACTION_REVIEW_APPROVED, null);
        notificationCenterService.notifyCourseChanged(course, actor, NotificationConstants.COURSE_ACTION_PUBLISHED);
        refreshTeacherCourseCacheAfterCommit(course.getTeacherId());
    }

    /**
     * 管理员驳回课程审核。
     *
     * @param adminId  管理员ID
     * @param courseId 课程ID
     * @param comment  审核意见
     */
    @Override
    @Transactional
    public void rejectCourseReview(String adminId, String courseId, String comment) {
        Courses course = requireCourseForAdmin(adminId, courseId, true);
        String normalizedComment = normalizeWorkflowComment(comment);
        transitionCourseStatus(course, CourseStatusFlow.ACTION_REJECT, adminId, Const.ROLE_ADMIN, normalizedComment);
        notificationCenterService.notifyCourseReviewResult(
                course,
                sysUserService.findUserById(adminId),
                NotificationConstants.COURSE_ACTION_REVIEW_REJECTED,
                normalizedComment
        );
        refreshTeacherCourseCacheAfterCommit(course.getTeacherId());
    }

    /**
     * 管理员下架课程。
     *
     * @param adminId  管理员ID
     * @param courseId 课程ID
     * @param comment  下架说明
     */
    @Override
    @Transactional
    public void takeDownCourse(String adminId, String courseId, String comment) {
        Courses course = requireCourseForAdmin(adminId, courseId, true);
        String normalizedComment = normalizeWorkflowComment(comment);
        transitionCourseStatus(course, CourseStatusFlow.ACTION_TAKE_DOWN, adminId, Const.ROLE_ADMIN, normalizedComment);
        Users actor = sysUserService.findUserById(adminId);
        notificationCenterService.notifyCourseReviewResult(course, actor, NotificationConstants.COURSE_ACTION_TAKEN_DOWN, normalizedComment);
        notificationCenterService.notifyCourseChanged(course, actor, NotificationConstants.COURSE_ACTION_TAKEN_DOWN);
        refreshTeacherCourseCacheAfterCommit(course.getTeacherId());
    }

    /**
     * 管理员重新上架课程。
     *
     * @param adminId  管理员ID
     * @param courseId 课程ID
     */
    @Override
    @Transactional
    public void republishCourse(String adminId, String courseId) {
        Courses course = requireCourseForAdmin(adminId, courseId, true);
        transitionCourseStatus(course, CourseStatusFlow.ACTION_REPUBLISH, adminId, Const.ROLE_ADMIN, null);
        Users actor = sysUserService.findUserById(adminId);
        notificationCenterService.notifyCourseReviewResult(course, actor, NotificationConstants.COURSE_ACTION_REPUBLISHED, null);
        notificationCenterService.notifyCourseChanged(course, actor, NotificationConstants.COURSE_ACTION_REPUBLISHED);
        refreshTeacherCourseCacheAfterCommit(course.getTeacherId());
    }

    /**
     * 管理员归档课程。
     *
     * @param adminId  管理员ID
     * @param courseId 课程ID
     * @param comment  归档说明
     */
    @Override
    @Transactional
    public void archiveCourseByAdmin(String adminId, String courseId, String comment) {
        Courses course = requireCourseForAdmin(adminId, courseId, true);
        String normalizedComment = normalizeWorkflowComment(comment);
        transitionCourseStatus(course, CourseStatusFlow.ACTION_ARCHIVE, adminId, Const.ROLE_ADMIN, normalizedComment);
        Users actor = sysUserService.findUserById(adminId);
        notificationCenterService.notifyCourseReviewResult(course, actor, NotificationConstants.COURSE_ACTION_ARCHIVED, normalizedComment);
        notificationCenterService.notifyCourseChanged(course, actor, NotificationConstants.COURSE_ACTION_ARCHIVED);
        refreshTeacherCourseCacheAfterCommit(course.getTeacherId());
    }

    /**
     * 获取选课学生
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 选课学生列表
     */
    @Override
    public List<EnrolledStudentVO> getStudentList(String userId, String courseId) {
        if (!usersService.isTeacher(userId)) {
            log.warn("[getStudentList.method]用户不是教师: userId={}", userId);
            return Collections.emptyList();
        }
        if (courseId == null || courseId.isEmpty()) {
            log.warn("[getStudentList.method]参数错误: courseId={}", courseId);
            return Collections.emptyList();
        }
        if (!coursesMapper.checkCourseExists(courseId)) {
            log.warn("[getStudentList.method]课程不存在: courseId={}", courseId);
            return Collections.emptyList();
        }
        List<EnrolledStudentVO> enrolledStudents = coursesMapper.getEnrolledStudents(courseId);
        if (enrolledStudents == null || enrolledStudents.isEmpty()) {
            log.info("[getStudentList.method]该课程无学生: courseId={}", courseId);
            return Collections.emptyList();
        }
        return enrolledStudents;
    }

    /**
     * 获取所有课程 - 管理员
     *
     * @return 课程列表
     */
    @Override
    public List<Courses> getCourseListAll() {
        List<Courses> courseListAll = coursesMapper.getCourseListAll();
        if (courseListAll == null || courseListAll.isEmpty()) {
            log.warn("[getCourseListAll.method]课程列表为空");
            return Collections.emptyList();
        }
        return courseListAll;
    }

    /**
     * 获取全部待审核课程。
     *
     * @return 待审核课程列表
     */
    @Override
    public List<Courses> listPendingReviewCourses() {
        List<Courses> pendingCourses = coursesMapper.listPendingReviewCourses();
        return pendingCourses == null ? Collections.emptyList() : pendingCourses;
    }

    /**
     * 校验当前操作者是否为教师。
     *
     * @param teacherId 教师ID
     */
    private void requireTeacherOperator(String teacherId) {
        if (!StringUtils.hasText(teacherId)) {
            throw new BusinessException("401", "用户未认证");
        }
        if (!usersService.isTeacher(teacherId)) {
            throw new BusinessException("403", "当前用户不是教师，无法执行该操作");
        }
    }

    /**
     * 校验当前操作者是否为管理员。
     *
     * @param adminId 管理员ID
     */
    private void requireAdminOperator(String adminId) {
        if (!StringUtils.hasText(adminId)) {
            throw new BusinessException("401", "用户未认证");
        }
        if (!usersService.isAdmin(adminId)) {
            throw new BusinessException("403", "当前用户不是管理员，无法执行该操作");
        }
    }

    /**
     * 加载并校验教师本人课程。
     *
     * @param teacherId  教师ID
     * @param courseId   课程ID
     * @param lockCourse 是否加锁查询
     * @return 课程实体
     */
    private Courses requireTeacherOwnedCourse(String teacherId, String courseId, boolean lockCourse) {
        requireTeacherOperator(teacherId);
        Courses course = loadCourse(courseId, lockCourse);
        if (!Objects.equals(course.getTeacherId(), teacherId)) {
            throw new BusinessException("403", "无权操作其他教师的课程");
        }
        return course;
    }

    /**
     * 加载并校验管理员可操作课程。
     *
     * @param adminId    管理员ID
     * @param courseId   课程ID
     * @param lockCourse 是否加锁查询
     * @return 课程实体
     */
    private Courses requireCourseForAdmin(String adminId, String courseId, boolean lockCourse) {
        requireAdminOperator(adminId);
        return loadCourse(courseId, lockCourse);
    }

    /**
     * 读取课程实体。
     *
     * @param courseId   课程ID
     * @param lockCourse 是否加锁查询
     * @return 课程实体
     */
    private Courses loadCourse(String courseId, boolean lockCourse) {
        if (!StringUtils.hasText(courseId)) {
            throw new BusinessException("400", "课程ID不能为空");
        }
        Courses course = lockCourse ? coursesMapper.getCourseByIdForUpdate(courseId) : coursesMapper.getCourseById(courseId);
        if (course == null || (course.getIsDeleted() != null && course.getIsDeleted() == 1)) {
            throw new BusinessException("404", "课程不存在");
        }
        return course;
    }

    /**
     * 执行课程状态流转，并写入审核日志。
     *
     * @param course        课程实体
     * @param action        流转动作
     * @param operatorId    操作人ID
     * @param operatorRole  操作人角色
     * @param comment       审核说明
     */
    private void transitionCourseStatus(Courses course,
                                        String action,
                                        String operatorId,
                                        String operatorRole,
                                        String comment) {
        String fromStatus = normalizeCourseStatus(course.getStatus());
        String nextStatus = CourseStatusFlow.nextStatus(fromStatus, action);
        if (!StringUtils.hasText(nextStatus)) {
            throw new BusinessException("400", buildIllegalTransitionMessage(fromStatus, action));
        }

        String normalizedComment = normalizeWorkflowComment(comment);
        if (CourseStatusFlow.ACTION_REJECT.equals(action) && !StringUtils.hasText(normalizedComment)) {
            throw new BusinessException("400", "驳回待审核课程时必须填写审核意见");
        }

        Date now = new Date();
        int updated = coursesMapper.updateCourseStatus(course.getId(), fromStatus, nextStatus, now);
        if (updated <= 0) {
            throw new BusinessException("409", "课程状态已发生变化，请刷新后重试");
        }

        int inserted = courseReviewLogMapper.insert(new CourseReviewLog()
                .setId(hybridIdGenerator.generateId())
                .setCourseId(course.getId())
                .setFromStatus(fromStatus)
                .setToStatus(nextStatus)
                .setOperatorId(operatorId)
                .setOperatorRole(operatorRole)
                .setComment(normalizedComment)
                .setCreatedAt(now));
        if (inserted <= 0) {
            throw new BusinessException("500", "写入课程审核记录失败");
        }

        course.setStatus(nextStatus);
        course.setUpdatedAt(now);
    }

    /**
     * 规范化课程状态值。
     *
     * @param status 原始状态
     * @return 规范化后的状态
     */
    private String normalizeCourseStatus(String status) {
        return status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 规范化工作流备注。
     *
     * @param comment 原始备注
     * @return 去除空白后的备注，若为空则返回 null
     */
    private String normalizeWorkflowComment(String comment) {
        if (!StringUtils.hasText(comment)) {
            return null;
        }
        String normalizedComment = comment.trim();
        if (normalizedComment.length() > 500) {
            throw new BusinessException("400", "审核说明长度不能超过500个字符");
        }
        return normalizedComment;
    }

    /**
     * 构建非法状态流转提示。
     *
     * @param currentStatus 当前状态
     * @param action        业务动作
     * @return 错误提示语
     */
    private String buildIllegalTransitionMessage(String currentStatus, String action) {
        Set<String> allowedActions = CourseStatusFlow.getAllowedActions(currentStatus);
        if (allowedActions.isEmpty()) {
            return "当前课程状态不允许继续流转";
        }
        String allowedActionText = allowedActions.stream()
                .map(this::resolveCourseActionLabel)
                .collect(Collectors.joining("、"));
        return "当前课程状态不支持执行“" + resolveCourseActionLabel(action) + "”，允许的操作为：“" + allowedActionText + "”";
    }

    /**
     * 将内部动作编码转换为中文说明。
     *
     * @param action 动作编码
     * @return 中文说明
     */
    private String resolveCourseActionLabel(String action) {
        if (CourseStatusFlow.ACTION_SUBMIT_REVIEW.equals(action)) {
            return "提交审核";
        }
        if (CourseStatusFlow.ACTION_APPROVE.equals(action)) {
            return "审核通过";
        }
        if (CourseStatusFlow.ACTION_REJECT.equals(action)) {
            return "审核驳回";
        }
        if (CourseStatusFlow.ACTION_TAKE_DOWN.equals(action)) {
            return "下架课程";
        }
        if (CourseStatusFlow.ACTION_REPUBLISH.equals(action)) {
            return "重新上架";
        }
        if (CourseStatusFlow.ACTION_ARCHIVE.equals(action)) {
            return "归档课程";
        }
        return action;
    }

    /**
     * 删除课程 - 管理员
     *
     * @param courseId 课程ID
     * @return 删除结果 true：成功，false：失败
     */
    @Override
    public boolean deleteCourse(String courseId) {
        Courses courseById = coursesMapper.getCourseById(courseId);
        if (courseById == null) {
            log.warn("[deleteCourse.method - admin] 课程不存在: courseId={}", courseId);
            return false;
        }
        List<Chapters> chapters = chaptersANDSectionsMapper.getChaptersByCourseId(courseId);
        Map<String, Object> beforeSnapshot = buildCourseAuditBeforeSnapshot(courseById, chapters);
        boolean success = true;
        int totalDeletedSections = 0;
        for (Chapters chapter : chapters) {
            int deletedSections = coursesMapper.deleteSections(chapter.getId());
            totalDeletedSections += Math.max(deletedSections, 0);
            log.info("[deleteCourse.method - admin] 删除章节下的小节: chapterId={}, 删除数量={}", chapter.getId(), deletedSections);
            if (deletedSections == 0) {
                log.warn("[deleteCourse.method - admin] 删除章节下的小节失败或无小节: chapterId={}", chapter.getId());
                success = false;
            }
        }
        // 删除章节
        int deletedChapters = coursesMapper.deleteChapters(courseId);
        log.info("[deleteCourse.method - admin] 删除章节: courseId={}, 删除数量={}", courseId, deletedChapters);
        if (deletedChapters == 0) {
            log.warn("[deleteCourse.method - admin] 删除章节失败或无章节: courseId={}", courseId);
            success = false;
        }

        // 删除课程资料
        int deletedMaterials = coursesMapper.deleteCourseAndMaterials(courseId);
        log.info("[deleteCourse.method - admin] 删除课程资料: courseId={}, 删除数量={}", courseId, deletedMaterials);
        if (deletedMaterials == 0) {
            log.warn("[deleteCourse.method - admin] 删除课程资料失败或无资料: courseId={}", courseId);
            success = false;
        }

        // 删除用户和课程关系
        int deletedUserCourses = coursesMapper.deleteUserCourses(courseId);
        log.info("[deleteCourse.method - admin] 删除用户和课程关系: courseId={}, 删除数量={}", courseId, deletedUserCourses);
        if (deletedUserCourses == 0) {
            log.warn("[deleteCourse.method - admin] 删除用户和课程关系失败或无关系: courseId={}", courseId);
            success = false;
        }

        // 删除课程
        int deletedCourse = coursesMapper.deleteCourse(courseId);
        log.info("[deleteCourse.method - admin] 删除课程: courseId={}, 删除结果={}", courseId, deletedCourse > 0);
        if (deletedCourse == 0) {
            log.error("[deleteCourse.method - admin] 删除课程失败: courseId={}", courseId);
            success = false;
        }

        if (success) {
            log.info("[deleteCourse.method - admin] 课程及相关数据删除成功: courseId={}", courseId);
        } else {
            log.warn("[deleteCourse.method - admin] 课程或部分相关数据删除失败: courseId={}", courseId);
        }
        auditLogService.recordSensitiveOperation(
                "COURSE_DELETE",
                "course",
                courseId,
                beforeSnapshot,
                buildCourseAuditAfterSnapshot(success, totalDeletedSections, deletedChapters, deletedMaterials, deletedUserCourses, deletedCourse),
                success ? "SUCCESS" : "FAILED",
                success ? "管理员删除课程成功" : "管理员删除课程存在部分失败");
        return success;
    }

    private Map<String, Object> buildCourseAuditBeforeSnapshot(Courses course, List<Chapters> chapters) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("course", course);
        snapshot.put("chapterIds", chapters == null ? Collections.emptyList() : chapters.stream().map(Chapters::getId).toList());
        snapshot.put("chapterCount", chapters == null ? 0 : chapters.size());
        return snapshot;
    }

    private Map<String, Object> buildCourseAuditAfterSnapshot(boolean success,
                                                              int totalDeletedSections,
                                                              int deletedChapters,
                                                              int deletedMaterials,
                                                              int deletedUserCourses,
                                                              int deletedCourse) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("success", success);
        snapshot.put("deletedSections", totalDeletedSections);
        snapshot.put("deletedChapters", deletedChapters);
        snapshot.put("deletedMaterials", deletedMaterials);
        snapshot.put("deletedUserCourses", deletedUserCourses);
        snapshot.put("deletedCourse", deletedCourse > 0);
        return snapshot;
    }

    /**
     * 获取课程分类信息 - 通过id查询
     *
     * @param id 分类id
     * @return 分类信息
     */
    @Override
    public Map<String, Object> getCourseCategoryById(String id) {
        if (id == null) {
            log.warn("[getCourseCategoryById.method - admin] 分类id不能为空!");
            return Collections.emptyMap();
        }
        CourseCategories courseCategoryById = coursesMapper.getCourseCategoryById(id);
        if (courseCategoryById == null) {
            log.warn("[getCourseCategoryById.method - admin] 分类不存在!");
            return Collections.emptyMap();
        }

        List<CourseCategories> topLevelCategories = coursesMapper.getTopLevelCategories();

        Map<String, Object> result = new HashMap<>();

        if (topLevelCategories.stream().anyMatch(c -> c.getId().equals(id))) {
            // 当前分类是顶级分类
            result.put("currentCategory", courseCategoryById);
            List<CourseCategories> subCategories = coursesMapper.getSubCategories(id);
            result.put("subCategories", subCategories);
        } else {
            // 当前分类不是顶级分类
            CourseCategories parentCategory = topLevelCategories.stream().filter(c -> c.getId().equals(String.valueOf(courseCategoryById.getParentId()))).findFirst().orElse(null);

            if (parentCategory == null) {
                log.warn("[getCourseCategoryById.method - admin] 无法找到父级分类!");
                return Collections.emptyMap();
            }

            result.put("topCategory", parentCategory);
            result.put("currentCategory", courseCategoryById);
            List<CourseCategories> siblingCategories = coursesMapper.getSubCategories(String.valueOf(courseCategoryById.getParentId()));
            result.put("siblingCategories", siblingCategories);
        }

        return result;
    }

    /**
     * 创建课程分类
     *
     * @param name     分类名称
     * @param parentId 父级分类id
     * @param orderIndex 排序权重
     * @return 创建结果 true：成功，false：失败
     */
    @Override
    public boolean createCourseCategory(String name, String parentId, Integer orderIndex) {
        if (name == null || name.isEmpty()) {
            log.warn("[createCourseCategory.method] 参数错误: name 不能为空");
            return false;
        }
        if (parentId == null || parentId.isEmpty()) {
            parentId = "0";
        }
        String id = hybridIdGenerator.generateId();
        return coursesMapper.createCategory(new CourseCategories()
                .setId(id)
                .setName(name)
                .setParentId(parentId)
                .setOrderIndex(orderIndex == null ? 0 : orderIndex)) > 0;
    }

    /**
     * 更新课程分类
     *
     * @param id       分类id
     * @param name     分类名称
     * @param parentId 父级分类id
     * @param orderIndex 排序权重
     * @return 更新结果 true：成功，false：失败
     */
    @Override
    public boolean updateCourseCategory(String id, String name, String parentId, Integer orderIndex) {
        if (id == null || id.isEmpty()) {
            log.warn("[updateCourseCategory.method]参数错误：id不能为空");
            return false;
        }
        CourseCategories courseCategoryById = coursesMapper.getCourseCategoryById(id);
        if (courseCategoryById == null) {
            log.warn("[updateCourseCategory.method]参数错误：id不存在");
            return false;
        }
        if (parentId == null || parentId.isEmpty()) {
            parentId = "0";
        }
        return coursesMapper.updateCategory(new CourseCategories()
                .setId(id)
                .setName(name)
                .setParentId(parentId)
                .setOrderIndex(orderIndex == null ? 0 : orderIndex)) > 0;
    }

    /**
     * 删除课程分类
     *
     * @param id 分类id
     * @return 删除结果 true：成功，false：失败
     */
    @Override
    public boolean deleteCourseCategory(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("[deleteCourseCategory.method]参数错误：id不能为空");
            return false;
        }
        CourseCategories courseCategoryById = coursesMapper.getCourseCategoryById(id);
        if (courseCategoryById == null) {
            log.warn("[deleteCourseCategory.method]分类不存在：id={}", id);
            return false;
        }
        if (coursesMapper.countSubCategories(id) > 0) {
            log.warn("[deleteCourseCategory.method]存在子分类，不能删除：id={}", id);
            return false;
        }
        if (coursesMapper.countCoursesByCategoryId(id) > 0) {
            log.warn("[deleteCourseCategory.method]存在关联课程，不能删除：id={}", id);
            return false;
        }
        return coursesMapper.deleteCategory(id) > 0;
    }

    /**
     * 预览分类删除影响范围。
     *
     * @param categoryId 分类ID
     * @return 预览结果
     */
    @Override
    public AdminCategoryOperationPreviewVO previewDeleteCourseCategory(String categoryId) {
        CategoryDeleteContext context = buildCategoryDeleteContext(categoryId);
        return buildOperationPreview(context, true);
    }

    /**
     * 执行紧急删除。
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>构建删除上下文并校验合法性；</li>
     *   <li>若非顶级分类且存在课程，先将课程迁入兜底分类；</li>
     *   <li>发布系统公告；</li>
     *   <li>物理删除分类；</li>
     *   <li>写入完成审计日志；</li>
     *   <li>注册事务提交后回调：事务成功提交后，异步向受影响教师发送邮件通知。</li>
     * </ol>
     *
     * <p><b>注意：</b>邮件通知故意在事务提交后异步触发，即使邮件发送失败也不会引起事务回滚。</p>
     *
     * @param operatorId 当前管理员ID
     * @param request    删除请求
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminCategoryDeleteResultVO emergencyDeleteCourseCategory(String operatorId, AdminCategoryDeleteRequestVO request) {
        CategoryDeleteContext context = buildCategoryDeleteContext(request.getCategoryId());
        validateDeleteRequestContext(context);
        if (context.isTopLevel() && !context.getAffectedCourses().isEmpty()) {
            throw new BusinessException("400", "顶级分类仍存在关联课程，请先完成分类迁移后再执行删除");
        }

        String reason = normalizeReason(request.getReason());
        Map<String, Object> beforeSnapshot = buildCategoryDeleteBeforeSnapshot(context, reason);
        auditLogService.recordSensitiveOperation("ADMIN_CAT_DEL_EM_REQ",
                "course_category",
                context.getCategory().getId(),
                beforeSnapshot,
                Map.of("mode", CATEGORY_DELETE_MODE_EMERGENCY),
                "SUCCESS",
                "管理员发起课程分类紧急删除");

        String fallbackCategoryId = null;
        if (!context.isTopLevel() && !context.getAffectedCourses().isEmpty()) {
            fallbackCategoryId = ensureFallbackCategory().getId();
            migrateCoursesToCategory(context.getAffectedCourses(), fallbackCategoryId);
        }

        // 在删除分类前，提前构建受影响课程信息（含教师邮箱），供事务提交后的异步邮件通知使用
        // 之所以在此处构建而非 afterCommit 内部，是因为分类删除后部分信息将无法查询
        final List<AdminCategoryOperationPreviewVO.AffectedCourseItem> affectedItems =
                buildAffectedCourseItems(context.getAffectedCourses());
        // 确定兜底分类名称：有课程被自动迁入时告知教师
        final String finalFallbackName = StringUtils.hasText(fallbackCategoryId)
                ? Const.CATEGORY_SYSTEM_FALLBACK_NAME : null;
        final String deletedCategoryName = context.getCategory().getName();

        String announcementId = publishDeleteAnnouncement(operatorId, context, CATEGORY_DELETE_MODE_EMERGENCY, reason);
        // 执行物理删除（按深度倒序，先删子分类）
        deleteCategoriesInOrder(context.getCategoryIdsToDelete());

        Map<String, Object> afterSnapshot = buildCategoryDeleteAfterSnapshot(context, CATEGORY_DELETE_MODE_EMERGENCY,
                announcementId, fallbackCategoryId, null);
        auditLogService.recordSensitiveOperation("ADMIN_CAT_DEL_EM_DONE",
                "course_category",
                context.getCategory().getId(),
                beforeSnapshot,
                afterSnapshot,
                "SUCCESS",
                "管理员完成课程分类紧急删除");

        // 注册事务提交后回调：
        // - 只有事务真正提交成功，才会触发邮件通知，避免事务回滚时发出错误通知
        // - 邮件通知方法标注了 @Async，afterCommit 内仅做线程提交，不阻塞当前请求
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                categoryNotificationService.sendEmergencyDeleteEmails(
                        deletedCategoryName, affectedItems, finalFallbackName);
            }
        });

        return buildDeleteResult(context,
                CATEGORY_OPERATION_DELETE,
                CATEGORY_DELETE_MODE_EMERGENCY,
                hybridIdGenerator.generateId(),
                announcementId,
                new Date(),
                null,
                0,
                "分类已紧急删除，系统公告已发布，邮件通知将异步发送给相关教师");
    }

    /**
     * 提交常规删除申请。
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>构建删除上下文并校验合法性；</li>
     *   <li>写入申请审计日志；</li>
     *   <li>发布系统公告，为每位受影响教师创建确认待办；</li>
     *   <li>将删除任务序列化后存入 Redis 待处理集合；</li>
     *   <li>写入等待状态审计日志；</li>
     *   <li>注册事务提交后回调：事务成功提交后，异步向受影响教师发送邮件通知。</li>
     * </ol>
     *
     * <p>实际物理删除由定时任务 {@code CategoryDeleteTask} 在保留期届满或教师全部确认后执行。</p>
     *
     * @param operatorId 当前管理员ID
     * @param request    删除请求
     * @return 删除结果（含请求ID、保留截止时间等信息）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminCategoryDeleteResultVO regularDeleteCourseCategory(String operatorId, AdminCategoryDeleteRequestVO request) {
        CategoryDeleteContext context = buildCategoryDeleteContext(request.getCategoryId());
        validateDeleteRequestContext(context);
        if (context.isTopLevel() && !context.getAffectedCourses().isEmpty()) {
            throw new BusinessException("400", "顶级分类仍存在关联课程，请先完成分类迁移后再提交常规删除");
        }

        String reason = normalizeReason(request.getReason());
        Date now = new Date();
        Date preserveUntil = Date.from(LocalDateTime.now()
                .plusDays(CATEGORY_REGULAR_DELETE_RETAIN_DAYS)
                .atZone(ZoneId.systemDefault())
                .toInstant());
        Map<String, Object> beforeSnapshot = buildCategoryDeleteBeforeSnapshot(context, reason);
        auditLogService.recordSensitiveOperation("ADMIN_CAT_DEL_RG_REQ",
                "course_category",
                context.getCategory().getId(),
                beforeSnapshot,
                Map.of("mode", CATEGORY_DELETE_MODE_REGULAR, "preserveUntil", preserveUntil),
                "SUCCESS",
                "管理员提交课程分类常规删除申请");

        String announcementId = publishDeleteAnnouncement(operatorId, context, CATEGORY_DELETE_MODE_REGULAR, reason);
        List<AdminCategoryPendingDeleteBO.TeacherConfirmationItem> teacherConfirmations =
                createTeacherConfirmationTodos(context, preserveUntil);
        String requestId = hybridIdGenerator.generateId();
        AdminCategoryPendingDeleteBO pendingDelete = new AdminCategoryPendingDeleteBO()
                .setRequestId(requestId)
                .setCategoryId(context.getCategory().getId())
                .setCategoryName(context.getCategory().getName())
                .setTopLevel(context.isTopLevel())
                .setDeleteMode(CATEGORY_DELETE_MODE_REGULAR)
                .setReason(reason)
                .setOperatorId(operatorId)
                .setAnnouncementId(announcementId)
                .setAffectedCourseIds(context.getAffectedCourses().stream().map(Courses::getId).toList())
                .setCategoryIdsToDelete(new ArrayList<>(context.getCategoryIdsToDelete()))
                .setTeacherConfirmations(teacherConfirmations)
                .setCreatedAt(now)
                .setPreserveUntil(preserveUntil);
        savePendingDeleteRecord(pendingDelete);

        // 提前构建受影响课程信息（含教师邮箱），供事务提交后的异步邮件通知使用
        final List<AdminCategoryOperationPreviewVO.AffectedCourseItem> affectedItems =
                buildAffectedCourseItems(context.getAffectedCourses());
        final String pendingCategoryName = context.getCategory().getName();
        final Date finalPreserveUntil = preserveUntil;

        Map<String, Object> afterSnapshot = buildCategoryDeleteAfterSnapshot(context, CATEGORY_DELETE_MODE_REGULAR,
                announcementId, null, preserveUntil);
        auditLogService.recordSensitiveOperation("ADMIN_CAT_DEL_RG_WAIT",
                "course_category",
                context.getCategory().getId(),
                beforeSnapshot,
                afterSnapshot,
                "SUCCESS",
                "课程分类已进入三天保留期，等待自动删除或教师提前确认");

        // 注册事务提交后回调：
        // - 事务成功提交后，异步发送常规删除申请邮件给受影响教师
        // - 邮件中包含截止时间和待办事项提示，帮助教师及时处理
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                categoryNotificationService.sendRegularDeleteRequestEmails(
                        pendingCategoryName, finalPreserveUntil, affectedItems);
            }
        });

        return buildDeleteResult(context,
                CATEGORY_OPERATION_DELETE,
                CATEGORY_DELETE_MODE_REGULAR,
                requestId,
                announcementId,
                now,
                preserveUntil,
                teacherConfirmations.size(),
                "分类已进入三天保留期；到期或所有相关教师确认后，系统将自动完成删除，邮件通知将异步发送给相关教师");
    }

    /**
     * 预览分类迁移影响范围。
     *
     * @param categoryId 分类ID
     * @return 预览结果
     */
    @Override
    public AdminCategoryOperationPreviewVO previewCategoryMigration(String categoryId) {
        CategoryDeleteContext context = buildCategoryDeleteContext(categoryId);
        AdminCategoryOperationPreviewVO preview = buildOperationPreview(context, true);
        if (context.getAffectedCourses().isEmpty()) {
            preview.getWarnings().add("当前分类及其子分类下暂无课程，无需执行迁移。");
        } else {
            preview.getWarnings().add("分类迁移开始后，教师端会立即收到系统公告提醒。");
        }
        return preview;
    }

    /**
     * 执行分类迁移。
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>校验源分类与目标分类不能相同，且目标不能为系统保留分类或源分类树内分类；</li>
     *   <li>在迁移前提前构建受影响课程信息（含教师邮箱），供后续邮件通知使用；</li>
     *   <li>批量更新课程的分类字段至目标分类；</li>
     *   <li>发布系统公告通知教师；</li>
     *   <li>写入审计日志；</li>
     *   <li>注册事务提交后回调：事务成功提交后，异步向受影响教师发送邮件通知。</li>
     * </ol>
     *
     * @param operatorId 当前管理员ID
     * @param request    迁移请求（包含源分类ID、目标分类ID、原因）
     * @return 迁移结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminCategoryDeleteResultVO migrateCategoryCourses(String operatorId, AdminCategoryMigrationRequestVO request) {
        if (Objects.equals(request.getSourceCategoryId(), request.getTargetCategoryId())) {
            throw new BusinessException("400", "源分类和目标分类不能相同");
        }
        CategoryDeleteContext context = buildCategoryDeleteContext(request.getSourceCategoryId());
        CourseCategories targetCategory = requireCategory(request.getTargetCategoryId());
        if (Const.CATEGORY_SYSTEM_FALLBACK_NAME.equals(targetCategory.getName())) {
            throw new BusinessException("400", "分类迁移不能直接迁移到系统保留分类");
        }
        if (context.getScopeCategoryIds().contains(targetCategory.getId())) {
            throw new BusinessException("400", "目标分类不能选择当前分类或其子分类");
        }

        String reason = normalizeReason(request.getReason());

        // 在迁移执行前，提前构建受影响课程信息（含教师邮箱），供事务提交后的异步邮件通知使用
        // 迁移后课程的 categoryId 已更新，此处需在变更前捕获原始状态
        final List<AdminCategoryOperationPreviewVO.AffectedCourseItem> affectedItems =
                buildAffectedCourseItems(context.getAffectedCourses());
        final String sourceCategoryName = context.getCategory().getName();
        final String targetCategoryName = targetCategory.getName();

        Map<String, Object> beforeSnapshot = buildCategoryMigrationBeforeSnapshot(context, targetCategory, reason);
        auditLogService.recordSensitiveOperation("ADMIN_CAT_MIG_REQ",
                "course_category",
                context.getCategory().getId(),
                beforeSnapshot,
                Map.of("targetCategoryId", targetCategory.getId(), "targetCategoryName", targetCategory.getName()),
                "SUCCESS",
                "管理员发起课程分类迁移");

        migrateCoursesToCategory(context.getAffectedCourses(), targetCategory.getId());
        String announcementId = publishMigrationAnnouncement(operatorId, context, targetCategory, reason);

        Map<String, Object> afterSnapshot = Map.of(
                "targetCategoryId", targetCategory.getId(),
                "targetCategoryName", targetCategory.getName(),
                "affectedCourseCount", context.getAffectedCourses().size(),
                "announcementId", announcementId
        );
        auditLogService.recordSensitiveOperation("ADMIN_CAT_MIG_DONE",
                "course_category",
                context.getCategory().getId(),
                beforeSnapshot,
                afterSnapshot,
                "SUCCESS",
                "管理员完成课程分类迁移");

        // 注册事务提交后回调：
        // - 事务成功提交后，异步发送迁移完成邮件给受影响教师
        // - 告知教师课程所属分类已变更，请核对确认
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                categoryNotificationService.sendMigrationEmails(
                        sourceCategoryName, targetCategoryName, affectedItems);
            }
        });

        return buildDeleteResult(context,
                CATEGORY_OPERATION_MIGRATION,
                "execute",
                hybridIdGenerator.generateId(),
                announcementId,
                new Date(),
                null,
                0,
                "分类关联课程已迁移完成，系统公告已发布，邮件通知将异步发送给相关教师");
    }

    /**
     * 处理 Redis 中待完成的常规删除任务。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processPendingCategoryDeletionTasks() {
        Set<Object> requestIds = redisTemplate.opsForSet().members(Const.CATEGORY_PENDING_DELETE_INDEX_KEY);
        if (CollectionUtils.isEmpty(requestIds)) {
            return;
        }
        for (Object requestIdObj : requestIds) {
            if (requestIdObj == null) {
                continue;
            }
            String requestId = String.valueOf(requestIdObj);
            try {
                AdminCategoryPendingDeleteBO pendingDelete = loadPendingDeleteRecord(requestId);
                if (pendingDelete == null) {
                    redisTemplate.opsForSet().remove(Const.CATEGORY_PENDING_DELETE_INDEX_KEY, requestId);
                    continue;
                }
                if (!shouldFinalizePendingDelete(pendingDelete)) {
                    continue;
                }
                finalizePendingDelete(pendingDelete);
            } catch (Exception exception) {
                log.error("[processPendingCategoryDeletionTasks.method] 处理常规删除任务失败: requestId={}", requestId, exception);
            }
        }
    }

    /**
     * 获取课程统计
     *
     * @return 课程统计
     */
    @Override
    public int getCourseCount() {
        return coursesMapper.getCourseCount();
    }

    @Override
    public CourseDetailedStatisticsVo getCourseDetailedStatistics() {
        try {
            // 获取基础统计数据
            int totalCourses = coursesMapper.getCourseCount();
            int publishedCourses = coursesMapper.getPublishedCourseCount();
            int draftCourses = coursesMapper.getDraftCourseCount();
            int archivedCourses = coursesMapper.getArchivedCourseCount();
            int publicCourses = coursesMapper.getPublicCourseCount();
            int privateCourses = coursesMapper.getPrivateCourseCount();
            int totalEnrollments = coursesMapper.getTotalEnrollments();

            // 获取分类统计
            List<Map<String, Object>> categoryStatsData = coursesMapper.getCategoryStatistics();
            List<CourseDetailedStatisticsVo.CategoryStatistics> categoryStatistics = categoryStatsData.stream().map(data -> new CourseDetailedStatisticsVo.CategoryStatistics().setCategoryId((String) data.get("categoryId")).setCategoryName((String) data.get("categoryName")).setCourseCount(((Number) data.get("courseCount")).intValue()).setTotalEnrollments(((Number) data.get("totalEnrollments")).intValue())).toList();

            // 获取状态统计
            List<Map<String, Object>> statusStatsData = coursesMapper.getStatusStatistics();
            List<CourseDetailedStatisticsVo.StatusStatistics> statusStatistics = statusStatsData.stream().map(data -> {
                String status = (String) data.get("status");
                String statusName = switch (status) {
                    case "published" -> "已发布";
                    case "draft" -> "草稿";
                    case "pending" -> "待审核";
                    case "taken_down" -> "已下架";
                    case "archived" -> "已归档";
                    default -> status;
                };
                return new CourseDetailedStatisticsVo.StatusStatistics().setStatus(status).setStatusName(statusName).setCourseCount(((Number) data.get("courseCount")).intValue());
            }).toList();

            // 获取热门课程
            List<Map<String, Object>> popularCoursesData = coursesMapper.getPopularCourses();
            List<CourseDetailedStatisticsVo.PopularCourse> popularCourses = popularCoursesData.stream().map(data -> new CourseDetailedStatisticsVo.PopularCourse().setCourseId((String) data.get("courseId")).setCourseTitle((String) data.get("courseTitle")).setTeacherName((String) data.get("teacherName")).setCategoryName((String) data.get("categoryName")).setEnrollmentCount(((Number) data.get("enrollmentCount")).intValue()).setStatus((String) data.get("status"))).toList();

            // 构建返回对象
            return new CourseDetailedStatisticsVo().setTotalCourses(totalCourses).setPublishedCourses(publishedCourses).setDraftCourses(draftCourses).setArchivedCourses(archivedCourses).setPublicCourses(publicCourses).setPrivateCourses(privateCourses).setTotalEnrollments(totalEnrollments).setCategoryStatistics(categoryStatistics).setStatusStatistics(statusStatistics).setPopularCourses(popularCourses);

        } catch (Exception e) {
            log.error("[getCourseDetailedStatistics.method] 获取详细课程统计失败", e);
            return null;
        }
    }

    /**
     * 课程搜索
     *
     * @param keyword 关键词
     * @return 课程列表
     */
    @Override
    public List<CourseVo> searchCourses(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("[searchCourses.method]参数错误：关键词不能为空");
            return Collections.emptyList();
        }
        // 将用户输入的查询字符串转换为布尔模式的查询语句
        String booleanQuery = formatToBooleanMode(keyword);
        List<CourseSearchDTO> courseSearchDTOS = courseSearchMapper.searchPublishedCourses(booleanQuery);
        return getCourseVos(courseSearchDTOS);
    }

    /**
     * 课程搜索 - 分类搜索
     *
     * @param categoryId 分类id
     * @return 课程列表
     */
    @Override
    public List<CourseVo> searchCoursesByCategory(String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) {
            log.warn("[searchCoursesByCategory.method] 参数错误：分类ID不能为空");
            return Collections.emptyList();
        }
        // 1. 获取该分类及其所有子孙分类的ID
        List<String> categoryIds = courseCategoryMapper.findSelfAndDescendantIds(categoryId);
        // 2. 如果没有找到任何分类ID（例如传入了不存在的ID），直接返回空列表
        if (categoryIds == null || categoryIds.isEmpty()) {
            log.warn("[searchCoursesByCategory.method] 未找到分类ID: categoryId={}", categoryId);
            return Collections.emptyList();
        }
        // 3. 使用获取到的ID列表去查询课程
        List<CourseSearchDTO> courseSearchDTOS = courseSearchMapper.findPublishedCoursesByCategories(categoryIds);
        return getCourseVos(courseSearchDTOS);
    }

    /**
     * 课程搜索 - 难度搜索
     *
     * @param level 难度
     * @return 课程列表
     */
    @Override
    public List<CourseVo> searchCoursesByLevel(String level) {
        // 增加参数校验
        if (level == null || !CourseLevel.isValid(level.toLowerCase())) {
            // 抛出自定义异常 new InvalidArgumentException("无效的课程难度等级");
            throw new BusinessException("INVALID_ARGUMENT", "无效的课程难度等级");
        }
        return courseSearchMapper.findPublishedCoursesByLevel(level);
    }

    /**
     * 获取用户已学课程数
     *
     * @param userId 用户ID
     * @return 用户已学课程数
     */
    // TODO 待完善 - 获取用户已学课程数
    @Override
    public Integer getCompletedCoursesCount(String userId) {
        // 参数校验
        if (userId == null || userId.isEmpty()) {
            log.error("[getCompletedCoursesCount.method] 用户ID不能为空");
            return 0;
        }

        // 检查用户是否存在
        Users userById = sysUserService.findUserById(userId);
        if (userById == null) {
            log.warn("[getCompletedCoursesCount.method] 用户不存在: userId={}", userId);
            return 0;
        }

        try {
            int completedCount = courseMapper.getProgressCourseCount(userId);
            log.info("[getCompletedCoursesCount.method] 查询用户已完成课程数量成功: userId={}, count={}", userId, completedCount);
            return completedCount;
        } catch (Exception e) {
            log.error("[getCompletedCoursesCount.method] 查询用户已完成课程数量失败: userId={}, error={}", userId, e.getMessage());
            return 0;
        }
    }

    /**
     * 构建分类删除上下文。
     *
     * @param categoryId 分类ID
     * @return 删除上下文
     */
    private CategoryDeleteContext buildCategoryDeleteContext(String categoryId) {
        CourseCategories category = requireCategory(categoryId);
        boolean topLevel = isTopLevelCategory(category);
        List<String> scopeCategoryIds = getCategoryTreeIds(categoryId);
        List<String> categoryIdsToDelete = topLevel ? new ArrayList<>(scopeCategoryIds) : List.of(categoryId);
        List<Courses> affectedCourses = scopeCategoryIds.isEmpty()
                ? Collections.emptyList()
                : coursesMapper.listCoursesByCategoryIds(scopeCategoryIds);
        return new CategoryDeleteContext()
                .setCategory(category)
                .setTopLevel(topLevel)
                .setScopeCategoryIds(scopeCategoryIds)
                .setCategoryIdsToDelete(categoryIdsToDelete)
                .setAffectedCourses(affectedCourses)
                .setHasChildCategories(scopeCategoryIds.size() > 1);
    }

    /**
     * 构建分类操作预览。
     *
     * @param context              分类上下文
     * @param includeTargetOptions 是否包含目标分类候选项
     * @return 预览结果
     */
    private AdminCategoryOperationPreviewVO buildOperationPreview(CategoryDeleteContext context, boolean includeTargetOptions) {
        AdminCategoryOperationPreviewVO preview = new AdminCategoryOperationPreviewVO()
                .setCategoryId(context.getCategory().getId())
                .setCategoryName(context.getCategory().getName())
                .setTopLevel(context.isTopLevel())
                .setMigrationRecommended(context.isTopLevel() || !context.getAffectedCourses().isEmpty())
                .setCascadeDeleteChildren(context.isTopLevel())
                .setDescendantCategoryCount(Math.max(context.getScopeCategoryIds().size() - 1, 0))
                .setAffectedCourseCount(context.getAffectedCourses().size())
                .setAffectedTeacherCount(countAffectedTeachers(context.getAffectedCourses()));

        boolean deleteAllowed = true;
        if (!context.isTopLevel() && context.isHasChildCategories()) {
            preview.getWarnings().add("该非顶级分类下仍存在子分类，请先处理下级分类后再删除。");
            deleteAllowed = false;
        }
        if (context.isTopLevel()) {
            preview.getWarnings().add("顶级分类删除会级联删除其下全部子分类，属于高风险操作。");
            preview.getWarnings().add("建议优先执行分类迁移，确认分类树下不存在任何课程后再执行删除。");
            if (!context.getAffectedCourses().isEmpty()) {
                preview.getWarnings().add("当前顶级分类树下仍存在课程，删除操作将被后端拦截，必须先完成迁移。");
                deleteAllowed = false;
            }
        } else {
            preview.getWarnings().add("紧急删除会立即删除分类，并通过系统公告和邮件通知相关教师尽快更换课程分类。");
            preview.getWarnings().add("常规删除会保留三天；到期或相关教师全部确认后，系统才会真正删除该分类。");
            if (!context.getAffectedCourses().isEmpty()) {
                preview.getWarnings().add("若删除时仍有课程停留在该分类下，系统会自动转入“待重新分类”保留分类。");
            }
        }
        preview.setEmergencyDeleteAllowed(deleteAllowed);
        preview.setRegularDeleteAllowed(deleteAllowed);
        preview.setAffectedCourses(buildAffectedCourseItems(context.getAffectedCourses()));
        if (includeTargetOptions) {
            preview.setAvailableTargetCategories(buildTargetCategoryOptions(context.getScopeCategoryIds()));
        }
        return preview;
    }

    /**
     * 校验删除上下文是否合法。
     *
     * @param context 删除上下文
     */
    private void validateDeleteRequestContext(CategoryDeleteContext context) {
        if (!context.isTopLevel() && context.isHasChildCategories()) {
            throw new BusinessException("400", "当前非顶级分类下仍存在子分类，请先处理子分类后再删除");
        }
    }

    /**
     * 构建受影响课程摘要。
     *
     * @param courses 课程列表
     * @return 摘要列表
     */
    private List<AdminCategoryOperationPreviewVO.AffectedCourseItem> buildAffectedCourseItems(List<Courses> courses) {
        Map<String, Users> teacherMap = sysUsersMapper.getValidTeacherList().stream()
                .collect(Collectors.toMap(Users::getId, Function.identity(), (left, right) -> left));
        Map<String, CourseCategories> categoryMap = courseCategoryMapper.findAll().stream()
                .collect(Collectors.toMap(CourseCategories::getId, Function.identity(), (left, right) -> left));
        return courses.stream().map(course -> {
            Users teacher = teacherMap.get(course.getTeacherId());
            CourseCategories courseCategory = categoryMap.get(course.getCategoryId());
            return new AdminCategoryOperationPreviewVO.AffectedCourseItem()
                    .setCourseId(course.getId())
                    .setCourseTitle(course.getTitle())
                    .setTeacherId(course.getTeacherId())
                    .setTeacherName(teacher == null ? null : teacher.getNickname())
                    .setTeacherEmail(teacher == null ? null : teacher.getEmail())
                    .setCurrentCategoryId(course.getCategoryId())
                    .setCurrentCategoryName(courseCategory == null ? null : courseCategory.getName());
        }).toList();
    }

    /**
     * 构建可迁移目标分类选项。
     *
     * @param excludedCategoryIds 需要排除的分类ID
     * @return 目标分类列表
     */
    private List<AdminCategoryOperationPreviewVO.CategoryOptionItem> buildTargetCategoryOptions(List<String> excludedCategoryIds) {
        Set<String> excludedSet = new HashSet<>(excludedCategoryIds);
        return courseCategoryMapper.findAll().stream()
                .filter(category -> !excludedSet.contains(category.getId()))
                .filter(category -> !Const.CATEGORY_SYSTEM_FALLBACK_NAME.equals(category.getName()))
                .map(category -> new AdminCategoryOperationPreviewVO.CategoryOptionItem()
                        .setCategoryId(category.getId())
                        .setCategoryName(category.getName())
                        .setParentId(category.getParentId()))
                .toList();
    }

    /**
     * 获取分类树中自身及全部子孙分类ID。
     *
     * @param categoryId 分类ID
     * @return 分类ID列表
     */
    private List<String> getCategoryTreeIds(String categoryId) {
        List<String> categoryIds = courseCategoryMapper.findSelfAndDescendantIds(categoryId);
        if (CollectionUtils.isEmpty(categoryIds)) {
            return List.of(categoryId);
        }
        return categoryIds;
    }

    /**
     * 判断当前分类是否为顶级分类。
     *
     * @param category 分类实体
     * @return 是否为顶级分类
     */
    private boolean isTopLevelCategory(CourseCategories category) {
        return category.getParentId() == null || category.getParentId().isBlank() || "0".equals(category.getParentId());
    }

    /**
     * 强制校验分类存在。
     *
     * @param categoryId 分类ID
     * @return 分类实体
     */
    private CourseCategories requireCategory(String categoryId) {
        if (!StringUtils.hasText(categoryId)) {
            throw new BusinessException("400", "分类ID不能为空");
        }
        CourseCategories category = coursesMapper.getCourseCategoryById(categoryId);
        if (category == null) {
            throw new BusinessException("404", "目标分类不存在");
        }
        return category;
    }

    /**
     * 统计受影响教师数量。
     *
     * @param courses 课程列表
     * @return 教师数量
     */
    private int countAffectedTeachers(List<Courses> courses) {
        return (int) courses.stream()
                .map(Courses::getTeacherId)
                .filter(StringUtils::hasText)
                .distinct()
                .count();
    }

    /**
     * 发布分类删除系统公告。
     *
     * @param operatorId 操作人ID
     * @param context    删除上下文
     * @param mode       删除模式
     * @param reason     删除原因
     * @return 公告ID
     */
    private String publishDeleteAnnouncement(String operatorId,
                                             CategoryDeleteContext context,
                                             String mode,
                                             String reason) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plusDays(CATEGORY_NOTICE_EXPIRE_DAYS);
        String title = CATEGORY_DELETE_MODE_EMERGENCY.equals(mode)
                ? "课程分类紧急删除通知"
                : "课程分类常规删除通知";
        String content = buildDeleteAnnouncementContent(context, mode, reason);
        SystemAnnouncementsVo announcement = systemAnnouncementsService.publishSystemAnnouncement(
                operatorId,
                title,
                content,
                Const.IS_ACTIVE_TRUE,
                now,
                expiredAt
        );
        return announcement == null ? null : announcement.getId();
    }

    /**
     * 发布分类迁移系统公告。
     *
     * @param operatorId     操作人ID
     * @param context        分类上下文
     * @param targetCategory 目标分类
     * @param reason         迁移原因
     * @return 公告ID
     */
    private String publishMigrationAnnouncement(String operatorId,
                                                CategoryDeleteContext context,
                                                CourseCategories targetCategory,
                                                String reason) {
        LocalDateTime now = LocalDateTime.now();
        SystemAnnouncementsVo announcement = systemAnnouncementsService.publishSystemAnnouncement(
                operatorId,
                "课程分类迁移通知",
                "管理员已启动课程分类迁移，原分类“" + context.getCategory().getName() + "”下的课程将迁移至“"
                        + targetCategory.getName() + "”。请相关教师及时核对课程分类设置。迁移原因："
                        + reason,
                Const.IS_ACTIVE_TRUE,
                now,
                now.plusDays(CATEGORY_NOTICE_EXPIRE_DAYS)
        );
        return announcement == null ? null : announcement.getId();
    }

    /**
     * 构建删除公告内容。
     *
     * @param context 删除上下文
     * @param mode    删除模式
     * @param reason  删除原因
     * @return 公告内容
     */
    private String buildDeleteAnnouncementContent(CategoryDeleteContext context, String mode, String reason) {
        if (CATEGORY_DELETE_MODE_EMERGENCY.equals(mode)) {
            return "管理员已执行课程分类“" + context.getCategory().getName() + "”的紧急删除操作。"
                    + "请相关教师立即检查并调整名下课程分类。删除原因：" + reason;
        }
        return "管理员已提交课程分类“" + context.getCategory().getName() + "”的常规删除申请。"
                + "该分类将进入三天保留期，若相关教师全部确认或保留期结束，系统将自动完成删除。删除原因："
                + reason;
    }

    /**
     * 为相关教师创建确认待办。
     *
     * @param context       删除上下文
     * @param preserveUntil 保留截止时间
     * @return 教师确认绑定列表
     */
    private List<AdminCategoryPendingDeleteBO.TeacherConfirmationItem> createTeacherConfirmationTodos(CategoryDeleteContext context,
                                                                                                      Date preserveUntil) {
        Map<String, Users> teacherMap = sysUsersMapper.getValidTeacherList().stream()
                .collect(Collectors.toMap(Users::getId, Function.identity(), (left, right) -> left));
        List<AdminCategoryPendingDeleteBO.TeacherConfirmationItem> confirmationItems = new ArrayList<>();
        Set<String> teacherIds = context.getAffectedCourses().stream()
                .map(Courses::getTeacherId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (String teacherId : teacherIds) {
            Users teacher = teacherMap.get(teacherId);
            if (teacher == null) {
                continue;
            }
            CreateTeacherTodoRequest todoRequest = new CreateTeacherTodoRequest();
            todoRequest.setContent("【分类删除确认】分类“" + context.getCategory().getName()
                    + "”将在保留期后删除，请及时处理课程分类并完成确认。");
            todoRequest.setPriority("high");
            todoRequest.setDueDate(preserveUntil);
            TeacherTodoVo teacherTodo = teacherTodoService.createTeacherTodo(teacherId, todoRequest);
            confirmationItems.add(new AdminCategoryPendingDeleteBO.TeacherConfirmationItem()
                    .setTeacherId(teacherId)
                    .setTodoId(teacherTodo.getId()));
        }
        return confirmationItems;
    }

    /**
     * 确保系统兜底分类存在。
     *
     * @return 兜底分类
     */
    private CourseCategories ensureFallbackCategory() {
        CourseCategories fallbackCategory = coursesMapper.getCategoryByName(Const.CATEGORY_SYSTEM_FALLBACK_NAME);
        if (fallbackCategory != null) {
            return fallbackCategory;
        }
        String categoryId = hybridIdGenerator.generateId();
        coursesMapper.createCategory(new CourseCategories()
                .setId(categoryId)
                .setName(Const.CATEGORY_SYSTEM_FALLBACK_NAME)
                .setParentId("0")
                .setOrderIndex(Const.ORDER_INDEX_MAX));
        return coursesMapper.getCourseCategoryById(categoryId);
    }

    /**
     * 批量迁移课程到指定分类。
     *
     * @param courses           课程列表
     * @param targetCategoryId  目标分类ID
     */
    private void migrateCoursesToCategory(List<Courses> courses, String targetCategoryId) {
        if (CollectionUtils.isEmpty(courses)) {
            return;
        }
        List<String> courseIds = courses.stream().map(Courses::getId).toList();
        coursesMapper.batchUpdateCourseCategory(targetCategoryId, courseIds);
    }

    /**
     * 按深度顺序删除分类，确保子分类先删。
     *
     * @param categoryIds 分类ID列表
     */
    private void deleteCategoriesInOrder(List<String> categoryIds) {
        Map<String, CourseCategories> categoryMap = courseCategoryMapper.findAll().stream()
                .collect(Collectors.toMap(CourseCategories::getId, Function.identity(), (left, right) -> left));
        List<String> deleteOrder = new ArrayList<>(categoryIds);
        deleteOrder.sort(Comparator.comparingInt((String id) -> calculateCategoryDepth(id, categoryMap)).reversed());
        for (String categoryId : deleteOrder) {
            coursesMapper.deleteCategory(categoryId);
        }
    }

    /**
     * 计算分类深度。
     *
     * @param categoryId  分类ID
     * @param categoryMap 分类映射
     * @return 深度值
     */
    private int calculateCategoryDepth(String categoryId, Map<String, CourseCategories> categoryMap) {
        int depth = 0;
        CourseCategories current = categoryMap.get(categoryId);
        while (current != null && StringUtils.hasText(current.getParentId()) && !"0".equals(current.getParentId())) {
            depth++;
            current = categoryMap.get(current.getParentId());
        }
        return depth;
    }

    /**
     * 保存常规删除待处理记录。
     *
     * @param pendingDelete 待处理记录
     */
    private void savePendingDeleteRecord(AdminCategoryPendingDeleteBO pendingDelete) {
        try {
            String key = Const.CATEGORY_PENDING_DELETE_PREFIX + pendingDelete.getRequestId();
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(pendingDelete), 30, TimeUnit.DAYS);
            redisTemplate.opsForSet().add(Const.CATEGORY_PENDING_DELETE_INDEX_KEY, pendingDelete.getRequestId());
        } catch (Exception exception) {
            throw new BusinessException("500", "常规删除任务写入缓存失败");
        }
    }

    /**
     * 读取常规删除待处理记录。
     *
     * @param requestId 请求ID
     * @return 待处理记录
     */
    private AdminCategoryPendingDeleteBO loadPendingDeleteRecord(String requestId) {
        try {
            Object value = redisTemplate.opsForValue().get(Const.CATEGORY_PENDING_DELETE_PREFIX + requestId);
            if (value == null) {
                return null;
            }
            String json = value instanceof String ? (String) value : objectMapper.writeValueAsString(value);
            return objectMapper.readValue(json, AdminCategoryPendingDeleteBO.class);
        } catch (Exception exception) {
            log.error("[loadPendingDeleteRecord.method] 读取常规删除任务失败: requestId={}", requestId, exception);
            return null;
        }
    }

    /**
     * 判断待处理删除是否满足最终执行条件。
     *
     * @param pendingDelete 待处理记录
     * @return 是否可以执行最终删除
     */
    private boolean shouldFinalizePendingDelete(AdminCategoryPendingDeleteBO pendingDelete) {
        boolean expired = pendingDelete.getPreserveUntil() != null && !pendingDelete.getPreserveUntil().after(new Date());
        boolean hasConfirmations = !CollectionUtils.isEmpty(pendingDelete.getTeacherConfirmations());
        return expired || (hasConfirmations && areAllTeachersConfirmed(pendingDelete));
    }

    /**
     * 判断相关教师是否均已完成确认待办。
     *
     * @param pendingDelete 待处理记录
     * @return 是否全部确认
     */
    private boolean areAllTeachersConfirmed(AdminCategoryPendingDeleteBO pendingDelete) {
        if (CollectionUtils.isEmpty(pendingDelete.getTeacherConfirmations())) {
            return false;
        }
        for (AdminCategoryPendingDeleteBO.TeacherConfirmationItem item : pendingDelete.getTeacherConfirmations()) {
            TeacherTodos teacherTodo = teacherTodosMapper.selectByIdAndTeacherId(item.getTodoId(), item.getTeacherId());
            if (teacherTodo == null || teacherTodo.getCompleted() == null || teacherTodo.getCompleted() != 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * 最终执行常规删除。
     *
     * <p>由定时任务 {@code CategoryDeleteTask} 调用，在保留期届满或相关教师全部确认后触发。</p>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>确认分类仍存在（已被手动删除则清理记录直接退出）；</li>
     *   <li>查询保留期内残留的课程；若是顶级分类且仍有课程则跳过（需管理员手动处理）；</li>
     *   <li>非顶级分类的残留课程自动迁入兜底分类；</li>
     *   <li>从受影响教师列表中预先构建邮件目标（含教师邮箱）；</li>
     *   <li>物理删除分类，清理 Redis 记录；</li>
     *   <li>写入完成审计日志；</li>
     *   <li>注册事务提交后回调：异步向受影响教师发送最终删除完成邮件。</li>
     * </ol>
     *
     * @param pendingDelete 待处理的常规删除记录（来自 Redis）
     */
    private void finalizePendingDelete(AdminCategoryPendingDeleteBO pendingDelete) {
        CourseCategories currentCategory = coursesMapper.getCourseCategoryById(pendingDelete.getCategoryId());
        if (currentCategory == null) {
            // 分类已不存在，直接清理 Redis 记录即可
            cleanupPendingDeleteRecord(pendingDelete.getRequestId());
            return;
        }

        // 查询在保留期内尚未被教师手动迁移的残留课程
        List<Courses> remainingCourses = CollectionUtils.isEmpty(pendingDelete.getCategoryIdsToDelete())
                ? Collections.emptyList()
                : coursesMapper.listCoursesByCategoryIds(pendingDelete.getCategoryIdsToDelete());

        if (Boolean.TRUE.equals(pendingDelete.getTopLevel()) && !remainingCourses.isEmpty()) {
            // 顶级分类仍存在课程：出于安全保护，暂不执行删除，需管理员介入处理
            log.warn("[finalizePendingDelete.method] 顶级分类仍存在课程，暂不执行删除: requestId={}, categoryId={}",
                    pendingDelete.getRequestId(), pendingDelete.getCategoryId());
            return;
        }

        String fallbackCategoryId = null;
        if (!Boolean.TRUE.equals(pendingDelete.getTopLevel()) && !remainingCourses.isEmpty()) {
            // 非顶级分类存在残留课程：自动迁入兜底分类，确保教师课程不丢失
            fallbackCategoryId = ensureFallbackCategory().getId();
            migrateCoursesToCategory(remainingCourses, fallbackCategoryId);
        }

        // 在物理删除前，从 teacherConfirmations 中查询受影响教师的邮箱信息
        // 这样即便删除后无法再查询到分类信息，邮件通知的目标仍然可以确定
        final List<AdminCategoryOperationPreviewVO.AffectedCourseItem> emailTargets =
                buildEmailTargetsFromConfirmations(pendingDelete.getTeacherConfirmations());
        final String finalCategoryName = pendingDelete.getCategoryName();

        // 执行物理删除（按分类深度倒序，确保子分类先删）
        deleteCategoriesInOrder(pendingDelete.getCategoryIdsToDelete());
        // 清理 Redis 中的待处理记录
        cleanupPendingDeleteRecord(pendingDelete.getRequestId());

        // 构建审计快照并写入日志
        Map<String, Object> beforeSnapshot = new LinkedHashMap<>();
        beforeSnapshot.put("requestId", pendingDelete.getRequestId());
        beforeSnapshot.put("categoryId", pendingDelete.getCategoryId());
        beforeSnapshot.put("categoryName", pendingDelete.getCategoryName());
        beforeSnapshot.put("categoryIdsToDelete", pendingDelete.getCategoryIdsToDelete());
        beforeSnapshot.put("reason", pendingDelete.getReason());

        Map<String, Object> afterSnapshot = new LinkedHashMap<>();
        afterSnapshot.put("requestId", pendingDelete.getRequestId());
        afterSnapshot.put("announcementId", pendingDelete.getAnnouncementId());
        afterSnapshot.put("fallbackCategoryId", fallbackCategoryId);
        afterSnapshot.put("executedAt", new Date());

        auditLogService.recordSensitiveOperation("ADMIN_CAT_DEL_RG_DONE",
                "course_category",
                pendingDelete.getCategoryId(),
                beforeSnapshot,
                afterSnapshot,
                "SUCCESS",
                "常规删除任务已由系统自动完成");

        // 注册事务提交后回调：
        // - 事务成功提交后，异步通知受影响教师分类已正式删除
        // - 此时 emailTargets 已在删除前构建完毕，不依赖已删除的分类数据
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                categoryNotificationService.sendRegularDeleteFinalizedEmails(finalCategoryName, emailTargets);
            }
        });
    }

    /**
     * 根据教师确认列表构建邮件通知目标。
     *
     * <p>从 {@code teacherConfirmations} 中提取教师ID，查询有效教师列表后组装
     * {@link AdminCategoryOperationPreviewVO.AffectedCourseItem}（仅包含教师信息，课程字段为空），
     * 用于常规删除最终执行时的邮件通知。</p>
     *
     * <p>之所以从 confirmations 而非当前课程列表构建，是因为在保留期内教师可能已
     * 自行将课程迁移至其他分类，但仍需接收最终删除的完成通知。</p>
     *
     * @param confirmations 教师确认待办绑定列表（来自 Redis 持久化对象）
     * @return 邮件目标列表（包含教师邮箱）
     */
    private List<AdminCategoryOperationPreviewVO.AffectedCourseItem> buildEmailTargetsFromConfirmations(
            List<AdminCategoryPendingDeleteBO.TeacherConfirmationItem> confirmations) {
        if (CollectionUtils.isEmpty(confirmations)) {
            return Collections.emptyList();
        }
        // 一次性查询所有有效教师，减少数据库交互
        Map<String, Users> teacherMap = sysUsersMapper.getValidTeacherList().stream()
                .collect(Collectors.toMap(Users::getId, Function.identity(), (left, right) -> left));

        return confirmations.stream()
                .map(AdminCategoryPendingDeleteBO.TeacherConfirmationItem::getTeacherId)
                .filter(StringUtils::hasText)
                .distinct()
                .map(teacherMap::get)
                .filter(Objects::nonNull)
                .map(teacher -> new AdminCategoryOperationPreviewVO.AffectedCourseItem()
                        .setTeacherId(teacher.getId())
                        .setTeacherName(teacher.getNickname())
                        .setTeacherEmail(teacher.getEmail()))
                .toList();
    }

    /**
     * 清理 Redis 中的常规删除待处理记录。
     *
     * <p>同时从待处理索引集合和具体详情 Key 两处删除，确保不留残留数据。</p>
     *
     * @param requestId 请求ID（对应 Redis key 中的唯一标识）
     */
    private void cleanupPendingDeleteRecord(String requestId) {
        redisTemplate.delete(Const.CATEGORY_PENDING_DELETE_PREFIX + requestId);
        redisTemplate.opsForSet().remove(Const.CATEGORY_PENDING_DELETE_INDEX_KEY, requestId);
    }

    /**
     * 归一化删除或迁移原因。
     *
     * @param reason 原始原因
     * @return 归一化后的原因
     */
    private String normalizeReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            return "管理员未填写额外原因说明";
        }
        return reason.trim();
    }

    /**
     * 构建删除前快照。
     *
     * @param context 删除上下文
     * @param reason  删除原因
     * @return 快照
     */
    private Map<String, Object> buildCategoryDeleteBeforeSnapshot(CategoryDeleteContext context, String reason) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("categoryId", context.getCategory().getId());
        snapshot.put("categoryName", context.getCategory().getName());
        snapshot.put("topLevel", context.isTopLevel());
        snapshot.put("scopeCategoryIds", context.getScopeCategoryIds());
        snapshot.put("categoryIdsToDelete", context.getCategoryIdsToDelete());
        snapshot.put("affectedCourseIds", context.getAffectedCourses().stream().map(Courses::getId).toList());
        snapshot.put("reason", reason);
        return snapshot;
    }

    /**
     * 构建删除后快照。
     *
     * @param context            删除上下文
     * @param mode               删除模式
     * @param announcementId     公告ID
     * @param fallbackCategoryId 兜底分类ID
     * @param preserveUntil      保留截止时间
     * @return 快照
     */
    private Map<String, Object> buildCategoryDeleteAfterSnapshot(CategoryDeleteContext context,
                                                                 String mode,
                                                                 String announcementId,
                                                                 String fallbackCategoryId,
                                                                 Date preserveUntil) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("mode", mode);
        snapshot.put("categoryId", context.getCategory().getId());
        snapshot.put("announcementId", announcementId);
        snapshot.put("fallbackCategoryId", fallbackCategoryId);
        snapshot.put("preserveUntil", preserveUntil);
        snapshot.put("affectedCourseCount", context.getAffectedCourses().size());
        snapshot.put("affectedTeacherCount", countAffectedTeachers(context.getAffectedCourses()));
        return snapshot;
    }

    /**
     * 构建迁移前快照。
     *
     * @param context        分类上下文
     * @param targetCategory 目标分类
     * @param reason         迁移原因
     * @return 快照
     */
    private Map<String, Object> buildCategoryMigrationBeforeSnapshot(CategoryDeleteContext context,
                                                                     CourseCategories targetCategory,
                                                                     String reason) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("sourceCategoryId", context.getCategory().getId());
        snapshot.put("sourceCategoryName", context.getCategory().getName());
        snapshot.put("targetCategoryId", targetCategory.getId());
        snapshot.put("targetCategoryName", targetCategory.getName());
        snapshot.put("affectedCourseIds", context.getAffectedCourses().stream().map(Courses::getId).toList());
        snapshot.put("reason", reason);
        return snapshot;
    }

    /**
     * 构建统一返回结果。
     *
     * @param context                    分类上下文
     * @param operationType              操作类型
     * @param mode                       模式
     * @param requestId                  请求ID
     * @param announcementId             公告ID
     * @param effectiveAt                生效时间
     * @param preserveUntil              保留截止时间
     * @param pendingTeacherConfirmations 待确认教师数量
     * @param message                    结果说明
     * @return 返回结果
     */
    private AdminCategoryDeleteResultVO buildDeleteResult(CategoryDeleteContext context,
                                                          String operationType,
                                                          String mode,
                                                          String requestId,
                                                          String announcementId,
                                                          Date effectiveAt,
                                                          Date preserveUntil,
                                                          Integer pendingTeacherConfirmations,
                                                          String message) {
        return new AdminCategoryDeleteResultVO()
                .setRequestId(requestId)
                .setOperationType(operationType)
                .setMode(mode)
                .setCategoryId(context.getCategory().getId())
                .setCategoryName(context.getCategory().getName())
                .setTopLevel(context.isTopLevel())
                .setDescendantCategoryCount(Math.max(context.getScopeCategoryIds().size() - 1, 0))
                .setAffectedCourseCount(context.getAffectedCourses().size())
                .setAffectedTeacherCount(countAffectedTeachers(context.getAffectedCourses()))
                .setPendingTeacherConfirmations(pendingTeacherConfirmations)
                .setAnnouncementId(announcementId)
                .setEffectiveAt(effectiveAt)
                .setPreserveUntil(preserveUntil)
                .setMessage(message);
    }


    /**
     *
     * @param category CourseCategories 对象
     * @return CourseCategoriesVo 对象
     */
    private CourseCategoriesVo convertToVo(CourseCategories category) {
        return new CourseCategoriesVo()
                .setId(category.getId())
                .setName(category.getName())
                .setParentId(category.getParentId().equals("0") ? null : category.getParentId())
                .setOrderIndex(category.getOrderIndex())
                .setChildren(new ArrayList<>());
    }

    /**
     * 构建课程分类树形结构
     *
     * @param allCategories 所有课程分类
     * @return List<CourseCategoriesVo> 树形结构
     */
    private List<CourseCategoriesVo> buildCategoryTree(List<CourseCategoriesVo> allCategories) {
        // 使用 Map 来存储所有分类，方便查找
        Map<String, CourseCategoriesVo> categoryMap = allCategories.stream().collect(Collectors.toMap(CourseCategoriesVo::getId, category -> category));

        List<CourseCategoriesVo> rootCategories = new ArrayList<>();

        for (CourseCategoriesVo category : allCategories) {
            if (category.getParentId() == null || category.getParentId().isEmpty()) {
                // 这是一个根分类
                rootCategories.add(category);
            } else {
                // 这是一个子分类，将其添加到父分类的 children 列表中
                CourseCategoriesVo parentCategory = categoryMap.get(category.getParentId());
                if (parentCategory != null) {
                    parentCategory.getChildren().add(category);
                }
            }
        }

        return rootCategories;
    }

    /**
     * 转换课程列表 --> CourseVo
     *
     * @param courseSearchDTOS 课程搜索结果
     * @return 课程列表
     */
    @NotNull
    private List<CourseVo> getCourseVos(List<CourseSearchDTO> courseSearchDTOS) {
        return courseSearchDTOS.stream().map(dto -> {
            // 1. 获取教师信息
            Users teacher = sysUserService.findUserById(dto.getTeacherId());
            // 2. 获取分类信息
            CourseCategories category = coursesMapper.getCategoryById(dto.getCategoryId());

            // 3. 构建 CourseVo 对象并填充数据
            return new CourseVo()
                    .setCourseId(dto.getId())
                    .setCourseTitle(dto.getTitle())
                    .setCourseDescription(dto.getDescription())
                    .setTeacherId(dto.getTeacherId())
                    // 安全地获取教师姓名，如果teacher为null则返回null
                    .setTeacherName(teacher != null ? teacher.getNickname() : null)
                    .setCategoryId(dto.getCategoryId())
                    // 安全地获取分类名称，如果category为null则返回null
                    .setCategoryName(category != null ? category.getName() : null)
                    .setCourseCover(dto.getCoverImageUrl())
                    .setCourseStatus(dto.getStatus())
                    .setCreateTime(dto.getCreatedAt())
                    .setUpdateTime(dto.getUpdatedAt());
        }).toList();
    }

    /**
     * 将用户输入 "java spring" 转换为 "+java +spring"
     * 这样可以确保每个词都必须存在于文档中
     */
    private String formatToBooleanMode(String query) {
        // 使用正则表达式替换一个或多个空白字符
        String[] words = query.trim().split("\\s+");
        return Arrays.stream(words)
                .map(word -> "+" + word)
                .collect(Collectors.joining(" "));
    }

    /**
     * 分类删除上下文。
     */
    @lombok.Getter
    @lombok.Setter
    @lombok.experimental.Accessors(chain = true)
    private static class CategoryDeleteContext {

        /**
         * 当前分类
         */
        private CourseCategories category;

        /**
         * 是否为顶级分类
         */
        private boolean topLevel;

        /**
         * 当前分类树范围
         */
        private List<String> scopeCategoryIds = new ArrayList<>();

        /**
         * 最终待删除分类ID列表
         */
        private List<String> categoryIdsToDelete = new ArrayList<>();

        /**
         * 受影响课程
         */
        private List<Courses> affectedCourses = new ArrayList<>();

        /**
         * 是否存在子分类
         */
        private boolean hasChildCategories;
    }
}
