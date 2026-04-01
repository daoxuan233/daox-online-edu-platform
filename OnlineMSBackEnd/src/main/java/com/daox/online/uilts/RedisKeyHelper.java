package com.daox.online.uilts;

public class RedisKeyHelper {

    private static final String ASSESSMENT_SESSION_PREFIX = "session:assessment";
    private static final String COURSE_GROUP_MUTE_INDEX_PREFIX = "chat:group:mute:index";
    private static final String COURSE_GROUP_MUTE_MEMBER_PREFIX = "chat:group:mute:member";
    private static final String COURSE_GROUP_ALL_MUTE_PREFIX = "chat:group:mute:all";
    private static final String COURSE_GROUP_REMOVED_MEMBER_PREFIX = "chat:group:removed";
    private static final String COURSE_GROUP_UNREAD_PREFIX = "chat:group:unread";

    /**
     * 生成学生测评会话的 Redis Key
     * 格式: session:assessment:{assessmentId}:{userId}
     *
     * @param assessmentId 测评ID
     * @param userId       用户ID
     * @return Redis Key
     */
    public static String getAssessmentSessionKey(String assessmentId, String userId) {
        return ASSESSMENT_SESSION_PREFIX + ":" + assessmentId + ":" + userId;
    }

    /**
     * 生成课程群聊禁言索引集合的 Redis Key。
     * <p>
     * 该索引集合仅用于快速枚举当前课程被禁言的用户列表，
     * 真正的禁言生效依赖成员级 Redis Key 的 TTL。
     * </p>
     *
     * @param courseId 课程ID
     * @return Redis Key
     */
    public static String getCourseGroupMuteIndexKey(String courseId) {
        return COURSE_GROUP_MUTE_INDEX_PREFIX + ":" + courseId;
    }

    /**
     * 生成课程群聊成员禁言状态的 Redis Key。
     * <p>
     * 该 Key 采用成员粒度，便于为每个学生独立设置 TTL，实现临时禁言自动过期。
     * </p>
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     * @return Redis Key
     */
    public static String getCourseGroupMuteMemberKey(String courseId, String userId) {
        return COURSE_GROUP_MUTE_MEMBER_PREFIX + ":" + courseId + ":" + userId;
    }

    /**
     * 生成课程群聊全员禁言状态的 Redis Key。
     * <p>
     * 该 Key 采用单键 TTL 模式，用于表达整个课程群当前是否开启全员禁言。
     * </p>
     *
     * @param courseId 课程ID
     * @return Redis Key
     */
    public static String getCourseGroupAllMuteKey(String courseId) {
        return COURSE_GROUP_ALL_MUTE_PREFIX + ":" + courseId;
    }

    /**
     * 生成课程群聊已移出成员集合的 Redis Key。
     * <p>
     * 该集合用于记录虽然仍保留选课关系，但已被教师移出群聊的学生。
     * </p>
     *
     * @param courseId 课程ID
     * @return Redis Key
     */
    public static String getCourseGroupRemovedMembersKey(String courseId) {
        return COURSE_GROUP_REMOVED_MEMBER_PREFIX + ":" + courseId;
    }

    /**
     * 生成课程群聊成员未读计数的 Redis Key。
     * <p>
     * 该 Key 以“课程 + 用户”为粒度，累计用户当前尚未查看的课程群消息条数。
     * </p>
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     * @return Redis Key
     */
    public static String getCourseGroupUnreadKey(String courseId, String userId) {
        return COURSE_GROUP_UNREAD_PREFIX + ":" + courseId + ":" + userId;
    }
}
