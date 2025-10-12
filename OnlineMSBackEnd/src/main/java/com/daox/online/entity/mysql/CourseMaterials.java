package com.daox.online.entity.mysql;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程资料表<br />
 * 用于存储课程相关的学习资料文件，支持按课程、章节、小节进行分层管理。<br />
 * 提供完整的文件元数据管理和下载统计功能，是课程资源管理的核心表。<br />
 * TableName: course_materials
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class CourseMaterials implements Serializable {

    /**
     * 资料ID
     */
    private String id;
    /**
     * 课程ID
     */
    private String courseId;
    /**
     * 章节ID
     */
    private String chapterId;
    /**
     * 小节ID
     */
    private String sectionId;
    /**
     * 资料标题
     */
    private String title;
    /**
     * 资料描述
     */
    private String description;
    /**
     * 文件ID（对应文件表的文件ID）
     */
    private String fileId; // 字段已更改
    /**
     * 下载次数
     */
    private Integer downloadCount;
    /**
     * 是否公开 1=公开，0=仅选课学生可见
     */
    private Integer isPublic;
    /**
     * 上传时间
     */
    private Date uploadTime;
    /**
     * 更新时间
     */
    private Date updatedAt;
    /**
     * 逻辑删除标志 [1-删除，0-正常]
     */
    private Integer isDeleted;

}