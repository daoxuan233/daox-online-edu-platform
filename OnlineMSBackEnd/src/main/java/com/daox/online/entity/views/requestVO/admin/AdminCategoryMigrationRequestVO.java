package com.daox.online.entity.views.requestVO.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理员分类迁移请求参数。
 * 用于将指定分类及其子分类下的课程迁移到新的目标分类。
 */
@Getter
@Setter
public class AdminCategoryMigrationRequestVO {

    /**
     * 源分类ID
     */
    @NotBlank(message = "源分类ID不能为空")
    private String sourceCategoryId;

    /**
     * 目标分类ID
     */
    @NotBlank(message = "目标分类ID不能为空")
    private String targetCategoryId;

    /**
     * 迁移原因
     */
    private String reason;
}
