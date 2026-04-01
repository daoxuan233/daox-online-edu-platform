package com.daox.online.entity.views.requestVO.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理员课程审核/上下架流转请求体。
 * <p>
 * 用于承载课程审核通过、驳回、下架、重新上架、归档等动作的公共入参。
 * </p>
 */
@Getter
@Setter
public class AdminCourseWorkflowRequestVO {

    /**
     * 课程ID。
     */
    @NotBlank(message = "课程ID不能为空")
    private String courseId;

    /**
     * 审核说明或操作备注。
     * <p>
     * 当前仅在审核驳回时为必填，由业务层进一步校验。
     * </p>
     */
    @Size(max = 500, message = "审核说明长度不能超过500个字符")
    private String comment;
}