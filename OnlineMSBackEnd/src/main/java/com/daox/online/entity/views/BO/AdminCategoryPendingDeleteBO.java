package com.daox.online.entity.views.BO;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 分类常规删除待处理缓存对象。
 * 用于将三天保留期内的分类删除申请落盘到 Redis，供定时任务轮询处理。
 */
@Getter
@Setter
@Accessors(chain = true)
public class AdminCategoryPendingDeleteBO implements Serializable {

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 是否为顶级分类
     */
    private Boolean topLevel;

    /**
     * 删除模式
     */
    private String deleteMode;

    /**
     * 删除原因
     */
    private String reason;

    /**
     * 创建人ID
     */
    private String operatorId;

    /**
     * 公告ID
     */
    private String announcementId;

    /**
     * 受影响课程ID列表
     */
    private List<String> affectedCourseIds = new ArrayList<>();

    /**
     * 待删除分类ID列表
     */
    private List<String> categoryIdsToDelete = new ArrayList<>();

    /**
     * 待确认教师待办绑定关系
     */
    private List<TeacherConfirmationItem> teacherConfirmations = new ArrayList<>();

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 保留截止时间
     */
    private Date preserveUntil;

    /**
     * 教师确认绑定信息
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class TeacherConfirmationItem implements Serializable {

        /**
         * 教师ID
         */
        private String teacherId;

        /**
         * 教师待办ID
         */
        private String todoId;
    }
}
