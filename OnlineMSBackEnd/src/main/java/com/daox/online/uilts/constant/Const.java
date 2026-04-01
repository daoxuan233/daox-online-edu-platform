package com.daox.online.uilts.constant;

public final class Const {
    private Const() { // 私有构造函数防止实例化
    }

    // 身份校验时的角色常量 'student' 'teacher' 'admin'
    public final static String ROLE_ADMIN = "admin";
    public final static String ROLE_STUDENTS = "student";
    public final static String ROLE_TEACHERS = "teacher";

    /**
     * 视频完成度的阈值，设置为 95%。
     */
    public static final double COMPLETION_THRESHOLD = 0.95;

    // 用户描述信息 user_describe
    /**
     * 性别 - 男 [man]
     */
    public final static String GENDER_MAN = "man";
    /**
     * 性别 - 女 [female]
     */
    public final static String GENDER_FEMALE = "female";
    /**
     * 性别 - 其他 [other] - 默认
     */
    public final static String GENDER_OTHER = "other";

    // 是否删除 - 逻辑处理
    public final static Integer USER_IS_DELETED_TRUE = 1; // 删除
    public final static Integer USER_IS_DELETED_FALSE = 0; // 正常

    // 逻辑判定
    /**
     * false - 1
     */
    public final static Integer LOGICAL_JUDGMENT_FALSE = 1;
    /**
     * true - 0
     */
    public final static Integer LOGICAL_JUDGMENT_NORMAL_TRUE = 0;

    // System accounts
    public final static Integer IS_ACTIVE_TRUE = 0; // 生效
    public final static Integer IS_ACTIVE_FALSE = 1; // 失效

    // learning status
    public final static String LEARNING_STATUS_NOT_START = "not_started"; // 未开始
    public final static String LEARNING_STATUS_IN_PROGRESS = "in_progress"; // 进行中
    public final static String LEARNING_STATUS_COMPLETED = "completed"; // 已完成
    public final static String LEARNING_STATUS_START_EXPIRED = "start_expired"; // 已过期

    // course status
    /**
     * 课程状态 - 草稿。
     */
    public final static String COURSE_STATUS_DRAFT = "draft";
    /**
     * 课程状态 - 待审核。
     */
    public final static String COURSE_STATUS_PENDING = "pending";
    /**
     * 课程状态 - 已发布。
     */
    public final static String COURSE_STATUS_PUBLISHED = "published";
    /**
     * 课程状态 - 已下架。
     */
    public final static String COURSE_STATUS_TAKEN_DOWN = "taken_down";
    /**
     * 课程状态 - 已归档。
     */
    public final static String COURSE_STATUS_ARCHIVED = "archived";

    // order index 值越小越靠前
    public final static Integer ORDER_INDEX_DEFAULT = 0;
    public final static Integer ORDER_INDEX_MAX = 9999;
    public final static Integer ORDER_INDEX_MIN = -9999;

    // assessment status published
    public final static Integer IS_PUBLISHED_FALSE = 0; // 未发布;
    public final static Integer IS_PUBLISHED_TRUE = 1; // 已发布;

    // assessment status
    /**
     * 正在填写
     */
    public static final String ASSESSMENT_STATUS_IN_PROGRESS = "in_progress";
    /**
     * 已提交
     */
    public static final String ASSESSMENT_STATUS_SUBMITTED = "submitted";
    /**
     * 评分中 - 批阅中
     */
    public static final String ASSESSMENT_STATUS_GRADING = "grading";
    /**
     * 已评分
     */
    public static final String ASSESSMENT_STATUS_GRADED = "graded";

    // 题型类型 single_choice, multi_choice, true_false, short_answer
    /**
     * 单选题
     */
    public static final String QUESTION_TYPE_SINGLE_CHOICE = "single_choice";
    /**
     * 多选题
     */
    public static final String QUESTION_TYPE_MULTI_CHOICE = "multi_choice";
    /**
     * 判断题
     */
    public static final String QUESTION_TYPE_TRUE_FALSE = "true_false";
    /**
     * 简答题
     */
    public static final String QUESTION_TYPE_SHORT_ANSWER = "short_answer";

    /**
     * 好友关系状态 - PENDING - 等待处理 = 0
     */
    public static final Integer FRIEND_RELATIONSHIP_STATUS_PENDING = 0;

    /**
     * 好友关系状态 - ACCEPTED - 正常好友关系 = 1
     */
    public static final Integer FRIEND_RELATIONSHIP_STATUS_ACCEPTED = 1;

    /**
     * 好友关系状态 - DECLINED - 拒绝好友 = 2
     */
    public static final Integer FRIEND_RELATIONSHIP_STATUS_DECLINED = 2;

    /**
     * 好友关系状态 - BLOCKED - 拉黑对伐 = 3
     */
    public static final Integer FRIEND_RELATIONSHIP_STATUS_BLOCKED = 3;

    /**
     * 好友标签 - friend - 好友
     */
    public static final String FRIEND_TAG_FRIEND = "friend";

    /**
     * 好友标签 - temporary - 临时对话
     */
    public static final String FRIEND_TAG_TEMPORARY = "temporary";

    // 过滤器优先级
    public final static int ORDER_FLOW_LIMIT = -101;
    public final static int ORDER_CORS = -102;

    // 请求频率限制
    public final static String FLOW_LIMIT_COUNTER = "flow:counter:";
    public final static String FLOW_LIMIT_BLOCK = "flow:block:";

    // 请求自定义属性
    public final static String ATTR_USER_ID = "userId"; //用户ID

    // ----------------------Redis键前缀----------------------
    // redis key - jwt白名单
    public static final String JWT_WHITE_LIST = "jwt:whitelist:";
    // redis key - jwt黑名单
    public final static String JWT_BLACK_LIST = "jwt:blacklist:";
    // redis key - jwt请求频率
    public final static String JWT_FREQUENCY = "jwt:frequency:";
    /**
     * redis key - 课程评价的统计数据缓存
     */
    public static final String COURSE_RATING_STATS_KEY = "rating:course:stats:";
    /**
     * redis key - 讲师评分统计
     */
    public static final String TEACHER_RATING_STATS_KEY = "rating:teacher:stats:";
    /**
     * redis key - 热门课程列表
     */
    public static final String POPULAR_COURSES_KEY = "course:popular:";
    /**
     * redis key - 评分权限缓存
     */
    public static final String RATING_PERMISSION_KEY = "rating:permission:";
    /**
     * redis key - 课程详情
     */
    public static final String COURSE_CACHE_KEY_PREFIX = "course:detail:";
    /**
     * redis key - 课程题库
     */
    public static final String QUESTION_COURSE = "course:questions:";
    /**
     * redis key - 课程信息
     */
    public static final String COURSE_INFO_KEY = "course:info:";

    /**
     * redis key - 分类常规删除索引集合
     */
    public static final String CATEGORY_PENDING_DELETE_INDEX_KEY = "category:delete:pending:index";

    /**
     * redis key - 分类常规删除详情前缀
     */
    public static final String CATEGORY_PENDING_DELETE_PREFIX = "category:delete:pending:";

    /**
     * 系统保留分类名称
     */
    public static final String CATEGORY_SYSTEM_FALLBACK_NAME = "待重新分类";

    /**
     * 统计数据缓存时间 - 1小时缓存
     */
    public static final long CACHE_TTL = 3600; // 时间单位 秒

    //消息队列
    public final static String MQ_MAIL = "mail";
    //邮件验证码
    public final static String VERIFY_EMAIL_LIMIT = "verify:email:limit:";
    public final static String VERIFY_EMAIL_DATA = "verify:email:data:";

}
