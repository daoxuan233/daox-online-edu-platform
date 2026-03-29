package com.daox.online.entity.views.responseVO.admin;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 管理员分类操作结果响应对象。
 * 统一承载删除申请、立即删除和分类迁移的处理结果。
 */
@Getter
@Setter
@Accessors(chain = true)
public class AdminCategoryDeleteResultVO {

    /**
     * 操作请求ID
     */
    private String requestId;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 删除模式或迁移模式
     */
    private String mode;

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
     * 子孙分类数量
     */
    private Integer descendantCategoryCount;

    /**
     * 受影响课程数量
     */
    private Integer affectedCourseCount;

    /**
     * 受影响教师数量
     */
    private Integer affectedTeacherCount;

    /**
     * 当前待确认教师数量
     */
    private Integer pendingTeacherConfirmations;

    /**
     * 公告ID
     */
    private String announcementId;

    /**
     * 生效时间
     */
    private Date effectiveAt;

    /**
     * 保留截止时间
     */
    private Date preserveUntil;

    /**
     * 操作说明
     */
    private String message;
}
