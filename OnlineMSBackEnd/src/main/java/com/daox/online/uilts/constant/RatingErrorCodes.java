package com.daox.online.uilts.constant;

/**
 * 评分系统错误码常量
 * 错误码格式：RATING_模块_具体错误
 */
public final class RatingErrorCodes {

    private RatingErrorCodes() {
    }

    // ==================== 权限相关错误 (1001-1099) ====================
    public static final String RATING_PERMISSION_NOT_ENROLLED = "1001";
    public static final String RATING_PERMISSION_INSUFFICIENT_PROGRESS = "1002";
    public static final String RATING_PERMISSION_ALREADY_RATED = "1003";
    public static final String RATING_PERMISSION_COURSE_NOT_PUBLISHED = "1004";
    public static final String RATING_PERMISSION_TEACHER_MISMATCH = "1005";
    public static final String RATING_PERMISSION_RATE_LIMIT_EXCEEDED = "1006";

    // ==================== 数据校验错误 (1101-1199) ====================
    public static final String RATING_VALIDATION_INVALID_RATING_VALUE = "1101"; // 评分值无效
    public static final String RATING_VALIDATION_MISSING_REQUIRED_FIELD = "1102"; // 缺少必填字段
    public static final String RATING_VALIDATION_COMMENT_TOO_LONG = "1103"; // 评论过长
    public static final String RATING_VALIDATION_INVALID_COURSE_ID = "1104"; // 课程ID无效
    public static final String RATING_VALIDATION_INVALID_TEACHER_ID = "1105"; // 教师ID无效

    // ==================== 业务逻辑错误 (1201-1299) ====================
    public static final String RATING_BUSINESS_SUBMIT_FAILED = "1201"; // 评分提交失败
    public static final String RATING_BUSINESS_UPDATE_FAILED = "1202"; // 评分更新失败
    public static final String RATING_BUSINESS_DELETE_FAILED = "1203"; // 评分删除失败
    public static final String RATING_BUSINESS_STATISTICS_UPDATE_FAILED = "1204"; // 评分统计更新失败
    public static final String RATING_BUSINESS_DIMENSION_NOT_FOUND = "1205"; // 评分维度未找到

    // ==================== 系统错误 (1301-1399) ====================
    public static final String RATING_SYSTEM_DATABASE_ERROR = "1301"; // 数据库错误
    public static final String RATING_SYSTEM_CACHE_ERROR = "1302"; // 缓存错误
    public static final String RATING_SYSTEM_CONCURRENT_ERROR = "1303"; // 并发错误
    public static final String RATING_SYSTEM_TIMEOUT_ERROR = "1304"; // 超时错误

    // ==================== 管理员功能错误 (1401-1499) ====================
    public static final String RATING_ADMIN_DIMENSION_CREATE_FAILED = "1401";
    public static final String RATING_ADMIN_DIMENSION_UPDATE_FAILED = "1402";
    public static final String RATING_ADMIN_DIMENSION_DELETE_FAILED = "1403";
    public static final String RATING_ADMIN_DIMENSION_DUPLICATE_KEY = "1404";
}
