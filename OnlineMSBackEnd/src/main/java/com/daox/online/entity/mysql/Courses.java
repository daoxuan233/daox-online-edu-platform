package com.daox.online.entity.mysql;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程表<br />
 * 存储课程的核心信息-是组织教学活动（章节、测评）的中心实体<br />
 * TableName:  courses
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Courses implements Serializable {

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

}
