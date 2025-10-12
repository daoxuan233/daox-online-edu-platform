package com.daox.online.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程搜索结果
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CourseSearchDTO implements Serializable {
    /**
     * 课程ID
     */
    private String id;
    /**
     * 课程标题
     */
    private String title;
    /**
     * 课程描述
     */
    private String description;
    /**
     * 封面图URL
     */
    private String coverImageUrl;
    /**
     * 教师ID
     */
    private String teacherId;
    /**
     * 分类ID
     */
    private String categoryId;
    /**
     * 课程状态:<br />
     * 'draft' : 草案<br />
     * 'published' : 发表<br />
     * 'archived' : 存档
     */
    private String status;
    /**
     * 学习人数
     */
    private Integer enrollmentCount;
    /**
     * 创建时间
     */
    private Date createdAt;
    /**
     * 更新时间
     */
    private Date updatedAt;
    /**
     * 逻辑删除标志 [1-"删除"，0-"正常"]
     */
    private Integer isDeleted;
    /**
     * 是否公开课程 [1-"私有"，0-"公开"]
     */
    private Integer isPrivate;
    /**
     * 相关性得分
     */
    private Double score;
}
