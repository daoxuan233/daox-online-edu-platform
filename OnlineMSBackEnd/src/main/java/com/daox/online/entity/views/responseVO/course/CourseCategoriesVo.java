package com.daox.online.entity.views.responseVO.course;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 课程分类视图对象
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class CourseCategoriesVo {
    private String id;
    private String name;
    private String parentId;  // 父分类ID，null或空字符串表示根节点
    private Integer orderIndex;
    private List<CourseCategoriesVo> children; // 子分类列表
}
