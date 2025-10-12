package com.daox.online.uilts;

public class RedisKeyHelper {

    private static final String ASSESSMENT_SESSION_PREFIX = "session:assessment";

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
}
