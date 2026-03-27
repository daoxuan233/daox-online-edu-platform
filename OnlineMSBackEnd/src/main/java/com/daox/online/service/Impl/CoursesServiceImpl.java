package com.daox.online.service.Impl;

import com.daox.online.controller.exception.BusinessException;
import com.daox.online.entity.mysql.*;
import com.daox.online.entity.dto.CourseCoreInfoDto;
import com.daox.online.entity.dto.CourseOutlineDto;
import com.daox.online.entity.dto.CourseSearchDTO;
import com.daox.online.entity.views.requestVO.teacher.CoursePropertiesVo;
import com.daox.online.entity.views.requestVO.teacher.TeacherCourseVo;
import com.daox.online.entity.views.responseVO.*;
import com.daox.online.entity.views.responseVO.course.CourseCategoriesVo;
import com.daox.online.entity.views.responseVO.course.CourseDetailedStatisticsVo;
import com.daox.online.entity.views.responseVO.course.CourseVo;
import com.daox.online.entity.views.responseVO.course.TeacherCourseDetailVo;
import com.daox.online.entity.views.responseVO.user.UserCoursesVo;
import com.daox.online.mapper.*;
import com.daox.online.service.AuditLogService;
import com.daox.online.service.CoursesService;
import com.daox.online.service.SysUserService;
import com.daox.online.service.UsersService;
import com.daox.online.uilts.constant.CourseLevel;
import com.daox.online.uilts.constant.Const;
import com.daox.online.uilts.HybridIdGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
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
    public Courses createCourse(CourseCoreInfoDto courseDto) {
        // 1. 创建并保存 Course 实体
        Courses course = new Courses();
        String courseId = hybridIdGenerator.generateId(); // 在业务层生成唯一ID
        course.setId(courseId);
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setCoverImageUrl(courseDto.getCoverImageUrl());
        course.setTeacherId(courseDto.getTeacherId());
        course.setCategoryId(courseDto.getCategoryId());
        course.setStatus(courseDto.getStatus());
        course.setIsPrivate(courseDto.isPrivateCourse() ? 1 : 0);
        course.setEnrollmentCount(0);
        course.setCreatedAt(new Date());
        course.setUpdatedAt(new Date());
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
        refreshTeacherCourseCache(course.getTeacherId());
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
     * 更新核心课程 信息
     * {@code @Transactional} 保证数据一致性。
     *
     * @param courseId  课程id
     * @param courseDto 课程信息
     * @return 更新结果
     */
    @Override
    @Transactional
    public Courses updateCourseCoreInfo(String courseId, CourseCoreInfoDto courseDto) {
        // 1. 校验课程是否存在
        Courses course = courseMapper.selectByPrimaryKey(courseId);
        if (course == null) {
            throw new RuntimeException("操作失败：ID为 " + courseId + " 的课程不存在。");
        }

        // 2. 更新 Course 表
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setCoverImageUrl(courseDto.getCoverImageUrl());
        course.setTeacherId(courseDto.getTeacherId());
        course.setCategoryId(courseDto.getCategoryId());
        course.setStatus(courseDto.getStatus());
        course.setUpdatedAt(new Date());
        course.setIsPrivate(courseDto.isPrivateCourse() ? 1 : 0);
        course.setIsDeleted(0);
        courseMapper.updateByPrimaryKeySelective(course);

        // 3. 更新 CourseProperties 表
        CourseCoreInfoDto.CoursePropertiesDto propsDto = courseDto.getProperties();
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
        return course;
    }

    /**
     * 全量更新课程大纲 --> 更新为: 增量更新课程大纲
     *
     * @param courseId   课程id
     * @param outlineDto 课程大纲
     */
    @Transactional
    @Override
    public void updateCourseOutline(String courseId, CourseOutlineDto outlineDto) {
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
     * @return 创建结果 true：成功，false：失败
     */
    @Override
    public String updateCourse(String userId, String courseId, TeacherCourseVo courseVo, CoursePropertiesVo coursePropertiesVo) {
        // 参数校验
        if (userId == null || userId.isEmpty() || courseId == null || courseId.isEmpty()) {
            log.warn("[updateCourse.method] 参数错误: userId={}, courseId={}", userId, courseId);
            return "更新失败，请检查参数，输入是否正确";
        }
        if (courseVo == null) {
            log.warn("[updateCourse.method] 课程信息错误: userId={}, courseId={}, courseVo=null", userId, courseId);
            return "课程信息错误";
        }
        if (coursePropertiesVo == null) {
            log.warn("[updateCourse.method] 课程属性信息错误: userId={}, courseId={}, coursePropertiesVo=null", userId, courseId);
            return "课程属性信息错误";
        }
        if (!usersService.isTeacher(userId)) {
            log.warn("[updateCourse.method] 用户不是教师: userId={}", userId);
            return "用户不是教师";
        }
        if (!coursesMapper.checkCourseExists(courseId)) {
            log.warn("[updateCourse.method] 课程不存在: userId={}, courseId={}", userId, courseId);
            return "课程不存在";
        }
        // 更新课程
        int course = coursesMapper.updateCourse(new Courses(courseId, courseVo.getTitle(), courseVo.getDescription(), courseVo.getCoverImageUrl(), courseVo.getTeacherId(), courseVo.getCategoryId(), courseVo.getStatus(), courseVo.getEnrollmentCount(), new Date(), new Date(), courseVo.getIsDeleted(), courseVo.getIsPrivate()));
        if (course > 0) {
            log.info("[updateCourse.method] 更新课程成功: userId={}, courseId={}", userId, courseId);
            // 检查属性信息
            CourseProperties coursePropertiesById = coursesMapper.getCoursePropertiesById(courseId);
            if (coursePropertiesById == null) {
                log.warn("[updateCourse.method] 课程属性不存在: userId={}, courseId={}", userId, courseId);
                // 创建属性信息
                int coursePropertiesBool = coursesMapper.createCourseProperties(new CourseProperties(
                        hybridIdGenerator.generateId(), courseId,
                        coursePropertiesVo.getLevel(), coursePropertiesVo.getIsNew(),
                        coursePropertiesVo.getTargetAudience(), coursePropertiesVo.getRequirements(),
                        coursePropertiesVo.getPrice(), coursePropertiesVo.getOriginalPrice())
                );
                if (coursePropertiesBool > 0) {
                    log.info("[updateCourse.method] 创建课程属性成功: userId={}, courseId={}", userId, courseId);
                    return "更新完成";
                } else {
                    log.warn("[updateCourse.method] 创建课程属性失败: userId={}, courseId={}", userId, courseId);
                }
            } else {
                // 更新课程属性
                int courseProperties = coursesMapper.updateCourseProperties(new CourseProperties()
                        .setId(coursePropertiesById.getId()).setCourseId(courseId)
                        .setLevel(coursePropertiesVo.getLevel()).setIsNew(coursePropertiesVo.getIsNew())
                        .setTargetAudience(coursePropertiesVo.getTargetAudience()).setRequirements(coursePropertiesVo.getRequirements())
                        .setPrice(coursePropertiesVo.getPrice()).setOriginalPrice(coursePropertiesVo.getOriginalPrice())
                );
                if (courseProperties > 0) {
                    log.info("[updateCourse.method] 更新课程属性成功: userId={}, courseId={}", userId, courseId);
                    return "更新完成";
                } else {
                    log.warn("[updateCourse.method] 更新课程属性失败: userId={}, courseId={}", userId, courseId);
                    return "网络超时...更新失败";
                }
            }
        }
        log.info("[updateCourse.method] 更新课程失败: userId={}, courseId={}", userId, courseId);
        return "更新失败";
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
     * 发布课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 发布结果 true：成功，false：失败
     */
    @Override
    public boolean publishCourse(String userId, String courseId) {
        if (usersService.isTeacher(userId)) {
            log.info("[publishCourse.method] 课程发布: userId={}, courseId={}", userId, courseId);
            // 检查课程是否存在
            if (!coursesMapper.checkCourseExists(courseId)) {
                log.warn("[publishCourse.method] 课程不存在: userId={}, courseId={}", userId, courseId);
                return false;
            }
            boolean published = coursesMapper.publishCourse(courseId) > 0;
            if (published) {
                refreshTeacherCourseCache(userId);
            }
            return published;
        }
        return false;
    }

    /**
     * 归档课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 归档结果 true：成功，false：失败
     */
    @Override
    public boolean archiveCourse(String userId, String courseId) {
        if (usersService.isTeacher(userId)) {
            if (coursesMapper.checkCourseExists(courseId)) {
                log.info("[archiveCourse.method] 课程归档: userId={}, courseId={}", userId, courseId);
                int i = coursesMapper.archiveCourse(courseId, userId);
                if (i > 0) {
                    log.info("[archiveCourse.method] 课程归档成功: userId={}, courseId={}", userId, courseId);
                    return true;
                }
            }
        }
        return false;
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
     * 将 CourseCategories 对象转换为 CourseCategoriesVo 对象
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
}
