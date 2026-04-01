package com.daox.online.uilts.constant;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 课程状态机定义。
 * <p>
 * 统一维护课程状态与动作之间的合法跳转关系，避免业务代码各自硬编码状态判断。
 * </p>
 * <p>
 * 当前合法流转如下：
 * draft + submit_review -> pending
 * draft + archive -> archived
 * pending + approve -> published
 * pending + reject -> draft
 * published + take_down -> taken_down
 * taken_down + republish -> published
 * taken_down + archive -> archived
 * </p>
 */
public final class CourseStatusFlow {

    /** 教师提交审核动作。 */
    public static final String ACTION_SUBMIT_REVIEW = "submit_review";
    /** 管理员审核通过动作。 */
    public static final String ACTION_APPROVE = "approve";
    /** 管理员审核驳回动作。 */
    public static final String ACTION_REJECT = "reject";
    /** 管理员下架动作。 */
    public static final String ACTION_TAKE_DOWN = "take_down";
    /** 管理员重新上架动作。 */
    public static final String ACTION_REPUBLISH = "republish";
    /** 归档动作。 */
    public static final String ACTION_ARCHIVE = "archive";

    private static final Map<String, Map<String, String>> TRANSITION_MAP = buildTransitionMap();

    private CourseStatusFlow() {
    }

    /**
     * 判断给定状态下是否允许执行指定动作。
     *
     * @param currentStatus 当前状态
     * @param action        业务动作
     * @return 允许返回 true，否则返回 false
     */
    public static boolean canTransition(String currentStatus, String action) {
        return nextStatus(currentStatus, action) != null;
    }

    /**
     * 解析状态机下一状态。
     *
     * @param currentStatus 当前状态
     * @param action        业务动作
     * @return 下一状态；若不存在合法跳转则返回 null
     */
    public static String nextStatus(String currentStatus, String action) {
        Map<String, String> actionMap = TRANSITION_MAP.get(normalize(currentStatus));
        if (actionMap == null) {
            return null;
        }
        return actionMap.get(normalize(action));
    }

    /**
     * 获取当前状态允许的全部动作。
     *
     * @param currentStatus 当前状态
     * @return 可执行动作集合
     */
    public static Set<String> getAllowedActions(String currentStatus) {
        Map<String, String> actionMap = TRANSITION_MAP.get(normalize(currentStatus));
        return actionMap == null ? Collections.emptySet() : actionMap.keySet();
    }

    private static Map<String, Map<String, String>> buildTransitionMap() {
        Map<String, Map<String, String>> transitionMap = new LinkedHashMap<>();
        transitionMap.put(Const.COURSE_STATUS_DRAFT, Map.of(
                ACTION_SUBMIT_REVIEW, Const.COURSE_STATUS_PENDING,
                ACTION_ARCHIVE, Const.COURSE_STATUS_ARCHIVED
        ));
        transitionMap.put(Const.COURSE_STATUS_PENDING, Map.of(
                ACTION_APPROVE, Const.COURSE_STATUS_PUBLISHED,
                ACTION_REJECT, Const.COURSE_STATUS_DRAFT
        ));
        transitionMap.put(Const.COURSE_STATUS_PUBLISHED, Map.of(
                ACTION_TAKE_DOWN, Const.COURSE_STATUS_TAKEN_DOWN
        ));
        transitionMap.put(Const.COURSE_STATUS_TAKEN_DOWN, Map.of(
                ACTION_REPUBLISH, Const.COURSE_STATUS_PUBLISHED,
                ACTION_ARCHIVE, Const.COURSE_STATUS_ARCHIVED
        ));
        transitionMap.put(Const.COURSE_STATUS_ARCHIVED, Collections.emptyMap());
        return Collections.unmodifiableMap(transitionMap);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}