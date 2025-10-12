package com.daox.online.entity.views.responseVO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类VO
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class CategoryVo implements Serializable {
    /**
     * 分类ID
     */
    private String id;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 父分类ID (0/null表示顶级分类)
     */
    private String parentId;
    /**
     * 排序值，值越小越靠前
     */
    private Integer orderIndex;

    /**
     * 用于存放子分类的列表
     */
    private List<CategoryVo> children = new ArrayList<>();
}
