package com.daox.ai.utils;

public final class Const {
    private Const() { // 私有构造函数防止实例化
    }
    // 请求自定义属性 - jwt
    public final static String ATTR_USER_ID = "userId"; //用户ID
    public final static String ATTR_USER_ROLE = "userRole"; // 用户角色

    // 身份角色常量
    public final static String ROLE_ADMIN = "admin";
    public final static String ROLE_STUDENT = "student";
    public final static String ROLE_TEACHER = "teacher";

    /**
     * AI - 对话标识
     */
    public final static String AI_ASSISTANT = "ai_assistant";
    /**
     * AI - 对话会话标识
     */
    public final static String AI_ASSISTANT_CONVERSATION = "ai_ass_con_";
    /**
     * redis key - jwt请求频率
     */
    public final static String JWT_FREQUENCY = "jwt:frequency:";
    /**
     * redis key - jwt黑名单
     */
    public final static String JWT_BLACK_LIST = "jwt:blacklist:";
    /**
     * redis key - AI 调用日额度
     */
    public static final String AI_QUOTA_DAILY = "ai:quota:daily:";
    /**
     * redis key - AI 调用小时额度
     */
    public static final String AI_QUOTA_HOURLY = "ai:quota:hourly:";
    /**
     * redis key - 对话内存会话标识
     */
    public static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    /**
     * redis key - 对话内存检索大小
     */
    public static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_retrieve_size";
}
