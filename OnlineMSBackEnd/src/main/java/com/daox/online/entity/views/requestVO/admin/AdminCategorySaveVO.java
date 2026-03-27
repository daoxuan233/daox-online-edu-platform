package com.daox.online.entity.views.requestVO.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 管理员课程分类维护请求体。
 * 用于创建或更新课程分类信息。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AdminCategorySaveVO {

    /**
     * 分类名称。
     */
    private String name;

    /**
     * 父分类ID，空值表示顶级分类。
     */
    private String parentId;

    /**
     * 排序权重。
     */
    private Integer orderIndex;
}
