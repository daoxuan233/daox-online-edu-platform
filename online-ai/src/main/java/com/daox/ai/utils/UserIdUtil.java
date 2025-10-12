package com.daox.ai.utils;

/**
 * 用户ID相关的工具类。
 */
public final class UserIdUtil {
    /**
     * 私有构造函数，防止实例化。
     */
    private UserIdUtil() {
    }

    /**
     * 一个简单的容器，用于持有排序后的用户ID。
     *
     * @param userOneId 字典序较小的ID
     * @param userTwoId 字典序较大的ID
     */
    public record OrderedUserIds(String userOneId, String userTwoId) {
    }

    /**
     * 比较两个用户ID，并返回一个包含排序后ID的对象（字典序较小的在前）。
     * 这对于在数据库中创建规范化的关系记录至关重要。
     *
     * @param id1 第一个用户ID
     * @param id2 第二个用户ID
     * @return 一个 OrderedUserIds 对象，其中 userOneId 的字典序小于或等于 userTwoId。
     * @throws IllegalArgumentException 如果任一ID为null。
     */
    public static OrderedUserIds orderUserIds(String id1, String id2) {
        if (id1 == null || id2 == null) {
            throw new IllegalArgumentException("User IDs cannot be null.");
        }

        // String.compareTo() 执行字典序比较，这正是我们所需要的。
        if (id1.compareTo(id2) > 0) {
            // 如果 id1 大于 id2，则交换它们的位置
            return new OrderedUserIds(id2, id1);
        } else {
            // 否则，保持原有顺序（id1 小于或等于 id2）
            return new OrderedUserIds(id1, id2);
        }
    }
}
