package com.daox.online.entity.mysql;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件管理表<br />
 * 用于存储系统中所有上传的文件信息，包括视频、图片、文档等各种类型的文件。<br />
 * 提供完整的文件元数据管理，支持文件分类、权限控制和统计功能。<br />
 * TableName: files
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class Files implements Serializable {

    /**
     * 文件ID
     */
    private String id;
    
    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 在存储系统中的对象名称（包含路径）
     */
    private String objectName;
    
    /**
     * 文件类型分类（video/image/document/material）
     */
    private String fileType;
    
    /**
     * 文件MIME类型
     */
    private String mimeType;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件上传者ID
     */
    private String uploaderId;
    
    /**
     * 文件标题（可选）
     */
    private String title;
    
    /**
     * 文件描述（可选）
     */
    private String description;

    /**
     * 下载次数
     */
    @Builder.Default
    private Integer downloadCount = 0;

    /**
     * 是否公开 1=公开，0=私有
     */
    @Builder.Default
    private Integer publicAccess = 0;

    /**
     * 是否允许下载 1=允许，0=不允许
     */
    private Boolean allowDownload;

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
    @Builder.Default
    private Integer isDeleted = 0;

}