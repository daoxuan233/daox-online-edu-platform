package com.daox.ai.utils;

public final class Const {
    private Const() { // 私有构造函数防止实例化
    }
    // 请求自定义属性 - jwt
    public final static String ATTR_USER_ID = "userId"; //用户ID

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
}
