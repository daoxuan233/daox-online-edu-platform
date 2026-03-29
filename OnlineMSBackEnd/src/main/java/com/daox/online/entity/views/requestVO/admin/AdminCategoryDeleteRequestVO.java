package com.daox.online.entity.views.requestVO.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理员分类删除请求参数。
 * 用于接收紧急删除与常规删除时的分类操作说明。
 */
@Getter
@Setter
public class AdminCategoryDeleteRequestVO {

    /**
     * 待删除的分类ID
     */
    @NotBlank(message = "分类ID不能为空")
    private String categoryId;

    /**
     * 删除原因
     */
    private String reason;
}
