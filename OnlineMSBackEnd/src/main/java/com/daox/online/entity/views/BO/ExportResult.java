package com.daox.online.entity.views.BO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 导出结果DTO
 * 用于封装文件导出操作的结果信息
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExportResult {
    /**
     * 下载链接
     */
    private String downloadUrl;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 导出的记录数量
     */
    private Integer recordCount;
    
    /**
     * 导出时间
     */
    private Date exportTime;
    
    /**
     * 链接有效期（秒）
     */
    private Integer validitySeconds;
    
    /**
     * 链接过期时间
     */
    private Date expiryTime;
    
    /**
     * 存储路径
     */
    private String storagePath;
}