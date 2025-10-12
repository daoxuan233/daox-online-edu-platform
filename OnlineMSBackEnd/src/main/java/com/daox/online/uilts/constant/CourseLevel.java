package com.daox.online.uilts.constant;

/**
 * 课程难度枚举类
 */
public enum CourseLevel {
    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    ADVANCED("advanced");

    public final String value;

    CourseLevel(String value) {
        this.value = value;
    }

    // 检查字符串是否是有效的level
    public static boolean isValid(String levelStr) {
        for (CourseLevel level : values()) {
            if (level.value.equalsIgnoreCase(levelStr)) {
                return true;
            }
        }
        return false;
    }
}
