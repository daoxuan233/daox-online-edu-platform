package com.daox.online.service.Impl;

import com.daox.online.controller.exception.BusinessException;
import com.daox.online.entity.mysql.CourseRatings;
import com.daox.online.entity.mysql.TeacherRatings;
import com.daox.online.entity.views.requestVO.ratings.CourseRatingRequest;
import com.daox.online.entity.views.requestVO.ratings.TeacherRatingRequest;
import com.daox.online.entity.views.responseVO.TopRatedCourseVO;
import com.daox.online.entity.views.responseVO.ratings.CourseRatingStatisticsVO;
import com.daox.online.entity.views.responseVO.ratings.RatingPermissionVO;
import com.daox.online.entity.views.responseVO.ratings.TeacherRatingStatisticsVO;
import com.daox.online.mapper.RatingsMapper;
import com.daox.online.service.RatingsService;
import com.daox.online.uilts.DateUtils;
import com.daox.online.uilts.constant.Const;
import com.daox.online.uilts.constant.RatingErrorCodes;
import com.daox.online.uilts.constant.RatingErrorMessages;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RatingsServiceImpl implements RatingsService {

    @Resource
    private RatingsMapper ratingsMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取课程评分统计
     *
     * @param courseId 课程ID
     * @return 评分统计
     */
    @Override
    public CourseRatingStatisticsVO getCourseRatingStatistics(String courseId) {
        // 先从缓存获取
        String cacheKey = Const.COURSE_RATING_STATS_KEY + courseId;
        CourseRatingStatisticsVO cached = (CourseRatingStatisticsVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        // 从数据库获取
        CourseRatingStatisticsVO statistics = ratingsMapper.getCourseRatingStatistics(courseId);
        if (statistics != null) {
            // 缓存结果
            redisTemplate.opsForValue().set(cacheKey, statistics, Const.CACHE_TTL, TimeUnit.SECONDS);
        }
        return statistics;
    }

    /**
     * 获取讲师评分统计
     *
     * @param teacherId 讲师ID
     * @return 讲师评分统计信息
     */
    @Override
    public TeacherRatingStatisticsVO getTeacherRatingStatistics(String teacherId) {
        // 先从缓存获取
        String cacheKey = Const.TEACHER_RATING_STATS_KEY + teacherId;
        TeacherRatingStatisticsVO cached = (TeacherRatingStatisticsVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        // 从数据库获取
        TeacherRatingStatisticsVO statistics = ratingsMapper.getTeacherRatingStatistics(teacherId);
        if (statistics != null) {
            // 缓存结果
            redisTemplate.opsForValue().set(cacheKey, statistics, Const.CACHE_TTL, TimeUnit.SECONDS);
        }
        return statistics;
    }

    /**
     * 获取热门课程排行
     *
     * @param limit      返回数量限制
     * @param minRatings 最少评分数量要求
     * @return 热门课程列表
     */
    @Override
    public List<TopRatedCourseVO> getTopRatedCourses(Integer limit, Integer minRatings) {
        // 先从缓存获取
        String cacheKey = Const.POPULAR_COURSES_KEY + limit + "_" + minRatings;
        Object cachedObject = redisTemplate.opsForValue().get(cacheKey);
        if (cachedObject instanceof List<?> cachedList) {
            if (!cachedList.isEmpty() && cachedList.getFirst() instanceof TopRatedCourseVO) {
                @SuppressWarnings("unchecked") // 忽略类型转换警告
                List<TopRatedCourseVO> cached = (List<TopRatedCourseVO>) cachedList;
                return cached;
            }
        }
        // 从数据库获取
        List<TopRatedCourseVO> topRatedCourses = ratingsMapper.getTopRatedCourses(limit, minRatings);
        if (topRatedCourses != null) {
            // 缓存结果
            redisTemplate.opsForValue().set(cacheKey, topRatedCourses, Const.CACHE_TTL, TimeUnit.SECONDS);
        }
        return topRatedCourses;
    }

    /**
     * 检查用户评分权限
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 评分权限信息
     */
    @Override
    public RatingPermissionVO checkRatingPermission(String userId, String courseId) {
        // 先从缓存获取
        String cacheKey = Const.RATING_PERMISSION_KEY + userId + ":" + courseId;
        RatingPermissionVO cached = (RatingPermissionVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        RatingPermissionVO permission = new RatingPermissionVO();
        permission.setUserId(userId);
        permission.setCourseId(courseId);
        try {
            // 1. 检查是否已选课
            boolean isEnrolled = ratingsMapper.isUserEnrolledInCourse(userId, courseId);
            if (!isEnrolled) {
                permission.setCanRate(false);
                permission.setReason("您尚未选修此课程");
                return permission;
            }

            // 2. 检查学习进度
            BigDecimal progressRate = ratingsMapper.getUserLearningProgress(userId, courseId);
            if (progressRate.compareTo(new BigDecimal("0.3")) < 0) {
                permission.setCanRate(false);
                permission.setReason("学习进度不足30%，无法评分");
                permission.setCurrentProgress(progressRate);
                permission.setRequiredProgress(new BigDecimal("0.3"));
                return permission;
            }

            // 3. 检查是否已评分
            boolean hasRated = ratingsMapper.hasUserRatedCourse(userId, courseId);
            if (hasRated) {
                permission.setCanRate(false);
                permission.setReason("您已对此课程进行过评分");
                return permission;
            }

            // 4. 检查评分频率限制
            int recentRatings = ratingsMapper.getUserRecentRatingsCount(userId, 1); // 1小时内
            if (recentRatings >= 10) {
                permission.setCanRate(false);
                permission.setReason("评分过于频繁，请稍后再试");
                return permission;
            }

            // 通过所有检查
            permission.setCanRate(true);
            permission.setReason("可以评分");
            permission.setCurrentProgress(progressRate);

            // 缓存结果5分钟
            redisTemplate.opsForValue().set(cacheKey, permission, 300, TimeUnit.SECONDS);

        } catch (Exception e) {
            permission.setCanRate(false);
            permission.setReason("系统错误，请稍后重试");
        }

        return permission;
    }

    /**
     * 提交课程评分
     *
     * @param userId  用户ID
     * @param request 评分请求
     * @return 提交结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitCourseRating(String userId, CourseRatingRequest request) {
        try {
            // 再次检查权限（防止并发问题）
            RatingPermissionVO permission = checkRatingPermission(userId, request.getCourseId());
            if (!permission.getCanRate()) {
                throw new BusinessException(RatingErrorCodes.RATING_PERMISSION_INSUFFICIENT_PROGRESS,
                        permission.getReason());
            }
            // 构建评分实体
            CourseRatings rating = new CourseRatings()
                    .setId(UUID.randomUUID().toString())
                    .setUserId(userId)
                    .setCourseId(request.getCourseId())
                    .setOverallRating(BigDecimal.valueOf(request.getOverallRating()))
                    .setContentQuality(request.getContentQuality() != null ?
                            BigDecimal.valueOf(request.getContentQuality()) : null)
                    .setDifficultyLevel(request.getDifficultyLevel() != null ?
                            BigDecimal.valueOf(request.getDifficultyLevel()) : null)
                    .setPracticality(request.getPracticality() != null ?
                            BigDecimal.valueOf(request.getPracticality()) : null)
                    .setComment(request.getComment())
                    .setIsAnonymous(request.getIsAnonymous() ? 1 : 0)
                    .setCreatedAt(DateUtils.convertToDate(LocalDateTime.now()))
                    .setUpdatedAt(DateUtils.convertToDate(LocalDateTime.now()))
                    .setIsDeleted(0);

            // 插入评分记录
            int result = ratingsMapper.insertCourseRating(rating);
            if (result <= 0) {
                throw new BusinessException(RatingErrorCodes.RATING_BUSINESS_SUBMIT_FAILED,
                        RatingErrorMessages.SUBMIT_FAILED);
            }
            // 异步更新统计数据（通过数据库触发器自动处理）
            // 清除相关缓存
            clearRatingCache(request.getCourseId(), "course");

            log.info("课程评分提交成功: userId={}, courseId={}, ratingId={}",
                    userId, request.getCourseId(), rating.getId());

            return "评分提交成功";
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("提交课程评分异常: userId={}, request={}", userId, request, e);
            throw new BusinessException(RatingErrorCodes.RATING_BUSINESS_SUBMIT_FAILED,
                    RatingErrorMessages.SUBMIT_FAILED);
        }
    }

    /**
     * 提交讲师评分
     *
     * @param userId  用户ID
     * @param request 评分请求
     * @return 提交结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitTeacherRating(String userId, TeacherRatingRequest request) {
        try {
            // 再次检查权限
            RatingPermissionVO permission = checkTeacherRatingPermission(userId, request.getTeacherId());
            if (!permission.getCanRate()) {
                throw new BusinessException(RatingErrorCodes.RATING_PERMISSION_TEACHER_MISMATCH,
                        permission.getReason());
            }
            // 构建评分实体
            TeacherRatings rating = new TeacherRatings()
                    .setId(UUID.randomUUID().toString())
                    .setUserId(userId)
                    .setTeacherId(request.getTeacherId())
                    .setCourseId(request.getCourseId())
                    .setOverallRating(BigDecimal.valueOf(request.getOverallRating()))
                    .setTeachingQuality(request.getTeachingQuality() != null ?
                            BigDecimal.valueOf(request.getTeachingQuality()) : null)
                    .setInteraction(request.getInteraction() != null ?
                            BigDecimal.valueOf(request.getInteraction()) : null)
                    .setProfessionalism(request.getProfessionalism() != null ?
                            BigDecimal.valueOf(request.getProfessionalism()) : null)
                    .setComment(request.getComment())
                    .setIsAnonymous(request.getIsAnonymous() ? 1 : 0)
                    .setCreatedAt(DateUtils.convertToDate(LocalDateTime.now()))
                    .setUpdatedAt(DateUtils.convertToDate(LocalDateTime.now()))
                    .setIsDeleted(0);
            // 插入评分记录
            int result = ratingsMapper.insertTeacherRating(rating);
            if (result <= 0) {
                throw new BusinessException(RatingErrorCodes.RATING_BUSINESS_SUBMIT_FAILED,
                        RatingErrorMessages.SUBMIT_FAILED);
            }

            // 清除相关缓存
            clearRatingCache(request.getTeacherId(), "teacher");

            log.info("讲师评分提交成功: userId={}, teacherId={}, ratingId={}",
                    userId, request.getTeacherId(), rating.getId());

            return "评分提交成功";
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("提交讲师评分异常: userId={}, request={}", userId, request, e);
            throw new BusinessException(RatingErrorCodes.RATING_BUSINESS_SUBMIT_FAILED,
                    RatingErrorMessages.SUBMIT_FAILED);
        }
    }

    /**
     * 检查讲师评分权限
     *
     * @param userId    用户ID
     * @param teacherId 讲师ID
     * @return 评分权限信息
     */
    @Override
    public RatingPermissionVO checkTeacherRatingPermission(String userId, String teacherId) {
        RatingPermissionVO permission = new RatingPermissionVO();
        permission.setUserId(userId);

        try {
            // 1. 检查用户是否选修过该讲师的课程
            boolean hasTakenCourse = ratingsMapper.hasUserTakenTeacherCourse(userId, teacherId);
            if (!hasTakenCourse) {
                return permission.setCanRate(false)
                        .setReason("您尚未选修该讲师的课程，无法进行评分");
            }

            // 2. 检查是否已评分
            boolean hasRated = ratingsMapper.hasUserRatedTeacher(userId, teacherId);
            if (hasRated) {
                return permission.setCanRate(false)
                        .setReason(RatingErrorMessages.ALREADY_RATED);
            }

            // 3. 检查评分频率限制
            int recentRatings = ratingsMapper.getUserRecentRatingsCount(userId, 1);
            if (recentRatings >= 10) {
                return permission.setCanRate(false)
                        .setReason(RatingErrorMessages.RATE_LIMIT_EXCEEDED);
            }

            return permission.setCanRate(true).setReason("可以进行评分");
        } catch (Exception e) {
            log.error("检查讲师评分权限异常: userId={}, teacherId={}", userId, teacherId, e);
            throw new BusinessException(RatingErrorCodes.RATING_SYSTEM_DATABASE_ERROR,
                    RatingErrorMessages.DATABASE_ERROR);
        }
    }

    /**
     * 清除评分相关缓存
     */
    private void clearRatingCache(String targetId, String targetType) {
        try {
            if ("course".equals(targetType)) {
                redisTemplate.delete(Const.COURSE_RATING_STATS_KEY + targetId);
                // 清除热门课程缓存
                redisTemplate.delete(Const.POPULAR_COURSES_KEY + "*");
            } else if ("teacher".equals(targetType)) {
                redisTemplate.delete(Const.TEACHER_RATING_STATS_KEY + targetId);
            }
        } catch (Exception e) {
            log.warn("清除评分缓存失败: targetId={}, targetType={}", targetId, targetType, e);
        }
    }

}
