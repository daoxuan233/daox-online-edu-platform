package com.daox.online.uilts.constant;

/**
 * 考核类型枚举类
 */
public enum AssessmentType {

    CLASSROOM_EXAM("ClassroomExam"),
    CHAPTER_EXAM("ChapterExam"),
    MIDTERM_EXAM("MidtermExam"),
    FINAL_EXAM("FinalExam"),
    HOMEWORK("homework");

    public final String value;

    AssessmentType(String value) {
        this.value = value;
    }

    // 检查字符串是否是有效的考核类型
    public static boolean isValid(String levelStr) {
        for (AssessmentType type : values()) {
            if (type.value.equalsIgnoreCase(levelStr)) {
                return true;
            }
        }
        return false;
    }

}
