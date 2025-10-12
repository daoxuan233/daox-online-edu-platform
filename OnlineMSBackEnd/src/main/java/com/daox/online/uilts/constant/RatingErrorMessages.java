package com.daox.online.uilts.constant;

/**
 * 评分系统错误消息常量
 */
public final class RatingErrorMessages {

    private RatingErrorMessages() {
        // 禁止实例化
    }

    // 权限相关错误消息
    public static final String NOT_ENROLLED = "您尚未选修此课程，无法进行评分";
    public static final String INSUFFICIENT_PROGRESS = "学习进度不足，需要完成至少60%的课程内容才能评分";
    public static final String ALREADY_RATED = "您已经对此课程/讲师进行过评分";
    public static final String COURSE_NOT_PUBLISHED = "课程尚未发布，无法进行评分";
    public static final String TEACHER_MISMATCH = "该讲师与课程不匹配";
    public static final String RATE_LIMIT_EXCEEDED = "评分过于频繁，请稍后再试";

    // 数据校验错误消息
    public static final String INVALID_RATING_VALUE = "评分值必须在1-5之间";
    public static final String MISSING_REQUIRED_FIELD = "缺少必填的评分维度";
    public static final String COMMENT_TOO_LONG = "评价内容不能超过1000字";
    public static final String INVALID_COURSE_ID = "无效的课程ID";
    public static final String INVALID_TEACHER_ID = "无效的讲师ID";

    // 业务逻辑错误消息
    public static final String SUBMIT_FAILED = "评分提交失败，请重试";
    public static final String UPDATE_FAILED = "评分更新失败，请重试";
    public static final String DELETE_FAILED = "评分删除失败，请重试";
    public static final String STATISTICS_UPDATE_FAILED = "评分统计更新失败";
    public static final String DIMENSION_NOT_FOUND = "评分维度不存在";

    // 系统错误消息
    public static final String DATABASE_ERROR = "数据库操作失败";
    public static final String CACHE_ERROR = "缓存操作失败";
    public static final String CONCURRENT_ERROR = "并发操作冲突，请重试";
    public static final String TIMEOUT_ERROR = "操作超时，请重试";

    // 管理员功能错误消息
    public static final String DIMENSION_CREATE_FAILED = "评分维度创建失败";
    public static final String DIMENSION_UPDATE_FAILED = "评分维度更新失败";
    public static final String DIMENSION_DELETE_FAILED = "评分维度删除失败";
    public static final String DIMENSION_DUPLICATE_KEY = "评分维度键名已存在";
}
