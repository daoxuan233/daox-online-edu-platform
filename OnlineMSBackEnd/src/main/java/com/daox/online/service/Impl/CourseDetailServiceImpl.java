package com.daox.online.service.Impl;

import com.daox.online.controller.exception.BusinessException;
import com.daox.online.entity.dto.CourseDetailDTO;
import com.daox.online.entity.dto.RatingStatistics;
import com.daox.online.entity.views.responseVO.course.CourseDetailVO;
import com.daox.online.entity.views.responseVO.course.CourseInstructorVO;
import com.daox.online.entity.views.responseVO.course.CourseVo;
import com.daox.online.mapper.CourseDetailMapper;
import com.daox.online.mapper.RatingStatisticsMapper;
import com.daox.online.service.CourseDetailService;
import com.daox.online.uilts.constant.Const;
import com.daox.online.uilts.constant.RatingErrorCodes;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourseDetailServiceImpl implements CourseDetailService {

    @Resource
    private CourseDetailMapper courseDetailMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RatingStatisticsMapper ratingStatisticsMapper;


    /**
     * 获取课程详情
     *
     * @param courseId 课程ID
     * @return 课程详情
     */
    @Override
    public CourseDetailVO getCourseDetail(String courseId) {
        // 1. 参数校验
        if (StringUtils.isBlank(courseId)) {
            throw new BusinessException(RatingErrorCodes.RATING_VALIDATION_INVALID_COURSE_ID, "课程ID不能为空");
        }

        // 2. 缓存检查
        String cacheKey = Const.COURSE_CACHE_KEY_PREFIX + courseId;
        CourseDetailVO cached = (CourseDetailVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("课程详情缓存命中: courseId={}", courseId);
            return cached;
        }
        // 3. 数据库查询
        try {
            CourseDetailVO result = buildCourseDetail(courseId);

            // 4. 缓存结果
            redisTemplate.opsForValue().set(cacheKey, result, Const.CACHE_TTL, TimeUnit.SECONDS);
            log.info("课程详情查询成功: courseId={}", courseId);
            return result;

        } catch (Exception e) {
            log.error("课程详情查询失败: courseId={}", courseId, e);
            throw new BusinessException("404", "课程详情查询失败");
        }
    }

    private CourseDetailVO buildCourseDetail(String courseId) {
        // 1. 主查询：获取课程基本信息和讲师信息
        CourseDetailDTO courseData = courseDetailMapper.getCourseDetailById(courseId);
        if (courseData == null) {
            throw new BusinessException("404", "课程不存在");
        }

        // 2. 并行查询优化
        CompletableFuture<Map<String, RatingStatistics>> ratingsFuture =
                CompletableFuture.supplyAsync(() -> getRatingStatistics(courseId, courseData.getTeacherId()));

        CompletableFuture<String> durationFuture =
                CompletableFuture.supplyAsync(() -> formatCourseDuration(courseId));

        CompletableFuture<List<CourseVo>> instructorCoursesFuture =
                CompletableFuture.supplyAsync(() -> getInstructorCourses(courseData.getTeacherId()));

        CompletableFuture<Integer> courseCountFuture =
                CompletableFuture.supplyAsync(() -> getInstructorCourseCount(courseData.getTeacherId()));

        try {
            // 3. 等待所有异步查询完成
            Map<String, RatingStatistics> ratings = ratingsFuture.get(2, TimeUnit.SECONDS);
            String duration = durationFuture.get(2, TimeUnit.SECONDS);
            List<CourseVo> instructorCourses = instructorCoursesFuture.get(2, TimeUnit.SECONDS);
            Integer courseCount = courseCountFuture.get(2, TimeUnit.SECONDS);

            // 4. 组装结果
            return assembleCourseDetailVO(courseData, ratings, duration, instructorCourses, courseCount);

        } catch (TimeoutException e) {
            log.warn("课程详情查询超时，使用降级数据: courseId={}", courseId);
            return assembleCourseDetailVOWithFallback(courseData);
        } catch (Exception e) {
            log.error("课程详情并行查询异常: courseId={}", courseId, e);
            throw new BusinessException(RatingErrorCodes.RATING_SYSTEM_DATABASE_ERROR, "数据查询异常");
        }
    }

    /**
     * 获取评分统计
     *
     * @param courseId  课程ID
     * @param teacherId 讲师ID
     * @return 评分统计
     */
    private Map<String, RatingStatistics> getRatingStatistics(String courseId, String teacherId) {
        try {
            List<RatingStatistics> statistics = ratingStatisticsMapper.getRatingStatistics(courseId, teacherId);
            return statistics.stream()
                    .collect(Collectors.toMap(
                            stat -> stat.getTargetType() + "_" + stat.getTargetId(),
                            stat -> stat
                    ));
        } catch (Exception e) {
            log.warn("评分统计查询失败: courseId={}, teacherId={}", courseId, teacherId, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 格式化课程时长
     *
     * @param courseId 课程ID
     * @return 课程时长字符串
     */
    private String formatCourseDuration(String courseId) {
        try {
            Integer totalSeconds = courseDetailMapper.getCourseDurationSeconds(courseId);
            if (totalSeconds == null || totalSeconds <= 0) {
                return "未知";
            }

            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;

            if (hours > 0) {
                return String.format("%d小时%d分钟", hours, minutes);
            } else {
                return String.format("%d分钟", minutes);
            }
        } catch (Exception e) {
            log.warn("课程时长计算失败: courseId={}", courseId, e);
            return "未知";
        }
    }

    /**
     * 获取讲师的课程列表
     *
     * @param teacherId 讲师ID
     * @return 讲师的课程列表
     */
    private List<CourseVo> getInstructorCourses(String teacherId) {
        try {
            return courseDetailMapper.getInstructorCourses(teacherId);
        } catch (Exception e) {
            log.warn("讲师课程列表查询失败: teacherId={}", teacherId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取讲师的课程数量
     *
     * @param teacherId 讲师ID
     * @return 课程数量
     */
    private Integer getInstructorCourseCount(String teacherId) {
        try {
            return courseDetailMapper.getInstructorCourseCount(teacherId);
        } catch (Exception e) {
            log.warn("讲师课程数量查询失败: teacherId={}", teacherId, e);
            return 0;
        }
    }

    /**
     * 组装课程详情VO
     *
     * @param courseData        课程数据
     * @param ratings           评分数据
     * @param duration          课程时长
     * @param instructorCourses 讲师课程 列表
     * @param courseCount       课程数量
     * @return 课程详情VO
     */
    private CourseDetailVO assembleCourseDetailVO(CourseDetailDTO courseData,
                                                  Map<String, RatingStatistics> ratings,
                                                  String duration,
                                                  List<CourseVo> instructorCourses,
                                                  Integer courseCount) {

        // 获取课程评分
        RatingStatistics courseRating = ratings.get("course_" + courseData.getCourseId());
        BigDecimal rating = courseRating != null ? courseRating.getAverageRating() : BigDecimal.ZERO;
        Integer ratingCount = courseRating != null ? courseRating.getTotalRatings() : 0;

        // 获取讲师评分
        RatingStatistics teacherRating = ratings.get("teacher_" + courseData.getTeacherId());
        BigDecimal instructorRatingValue = teacherRating != null ? teacherRating.getAverageRating() : BigDecimal.ZERO;

        // 构建讲师信息
        CourseInstructorVO instructor = new CourseInstructorVO()
                .setId(courseData.getInstructorId())
                .setName(courseData.getInstructorName())
                .setTitle(courseData.getInstructorTitle())
                .setBiography(courseData.getInstructorBiography())
                .setAvatar(courseData.getInstructorAvatar())
                .setCourseCount(courseCount)
                .setCourseList(instructorCourses)
                .setRating(instructorRatingValue);

        // 构建课程详情
        return new CourseDetailVO()
                .setCourseId(courseData.getCourseId())
                .setTitle(courseData.getTitle())
                .setDescription(courseData.getDescription())
                .setCover(courseData.getCover())
                .setPrice(courseData.getPrice())
                .setOriginalPrice(courseData.getOriginalPrice())
                .setLevel(courseData.getLevel())
                .setDuration(duration)
                .setIsNew(courseData.getIsNew())
                .setStudentCount(courseData.getStudentCount())
                .setRating(rating)
                .setRatingCount(ratingCount)
                .setTargetAudience(courseData.getTargetAudience())
                .setTechnicalRequirements(courseData.getTechnicalRequirements())
                .setInstructor(instructor);
    }

    /**
     * 构建课程详情 视图
     *
     * @param courseData 课程数据
     * @return 课程详情 视图
     */
    private CourseDetailVO assembleCourseDetailVOWithFallback(CourseDetailDTO courseData) {
        // 降级处理：返回基础信息，评分和时长使用默认值
        CourseInstructorVO instructor = new CourseInstructorVO()
                .setId(courseData.getInstructorId())
                .setName(courseData.getInstructorName())
                .setTitle(courseData.getInstructorTitle())
                .setBiography(courseData.getInstructorBiography())
                .setAvatar(courseData.getInstructorAvatar())
                .setCourseCount(0)
                .setCourseList(Collections.emptyList())
                .setRating(BigDecimal.ZERO);

        return new CourseDetailVO()
                .setCourseId(courseData.getCourseId())
                .setTitle(courseData.getTitle())
                .setDescription(courseData.getDescription())
                .setCover(courseData.getCover())
                .setPrice(courseData.getPrice())
                .setOriginalPrice(courseData.getOriginalPrice())
                .setLevel(courseData.getLevel())
                .setDuration("未知")
                .setIsNew(courseData.getIsNew())
                .setStudentCount(courseData.getStudentCount())
                .setRating(BigDecimal.ZERO)
                .setRatingCount(0)
                .setTargetAudience(courseData.getTargetAudience())
                .setTechnicalRequirements(courseData.getTechnicalRequirements())
                .setInstructor(instructor);
    }
}
