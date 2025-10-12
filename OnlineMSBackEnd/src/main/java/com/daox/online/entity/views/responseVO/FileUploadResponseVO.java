package com.daox.online.entity.views.responseVO;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 文件上传响应VO
 * 用于返回文件上传结果信息
 */
@Data
@Accessors(chain = true)
public class FileUploadResponseVO {
    
    /**
     * 原始文件名
     */
    private String originalFilename;
    
    /**
     * 存储后的文件名
     */
    private String storedFilename;
    
    /**
     * 在存储系统中的对象名称（包含路径）
     */
    private String objectName;
    
    /**
     * 文件下载URL（临时链接，有时效性）
     */
    private String downloadUrl;
    
    /**
     * 永久访问链接（通过应用服务器代理）
     */
    private String permanentUrl;
    
    /**
     * 文件类型分类（用于文件夹分类）
     */
    private String fileType;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件MIME类型
     */
    private String contentType;
    
    /**
     * 上传者ID（统一命名为userId）
     */
    private String userId;
    
    /**
     * 上传时间戳
     */
    private Long uploadTime;
    
    public FileUploadResponseVO() {
        this.uploadTime = System.currentTimeMillis();
    }
}