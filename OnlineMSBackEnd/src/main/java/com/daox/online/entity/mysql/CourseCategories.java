package com.daox.online.entity.mysql;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 课程分类表<br />
 * 用于对课程进行结构化分类，方便用户按类别浏览和检索课程。<br />
 * 支持父子关系，可以构建多层级的课程目录体系<br />
 * TableName: course_categories
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CourseCategories implements Serializable {

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


}
