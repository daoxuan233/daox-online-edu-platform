package com.daox.online.service.Impl;

import com.daox.online.service.FileStorageService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * MinIO文件存储服务实现
 * 提供文件上传、下载URL生成等功能
 * 支持自动创建bucket、文件类型分类存储等特性
 */
@Service
@Slf4j
public class MinioFileStorageService implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${app.file-storage.minio.bucketName}")
    private String bucketName;

    @Value("${app.file-storage.minio.maxFileSize:104857600}") // 默认100MB
    private long maxFileSize;

    @Value("${app.file-storage.minio.defaultPartSize:10485760}") // 默认10MB
    private long defaultPartSize;

    @Value("${app.file-storage.minio.maxUrlDuration:604800}") // 默认7天
    private int maxUrlDuration;

    /**
     * 构造函数注入MinioClient
     * 在服务启动时检查bucket是否存在，不存在则创建
     */
    public MinioFileStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 初始化方法，确保bucket存在
     */
    private void ensureBucketExists() throws Exception {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            
            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("成功创建MinIO bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("检查或创建bucket失败: {}", bucketName, e);
            throw new RuntimeException("MinIO bucket初始化失败", e);
        }
    }

    /**
     * 上传文件到MinIO
     * 支持自动创建bucket、文件大小校验、优化的分片上传等功能
     *
     * @param inputStream 文件内容的输入流
     * @param objectName  在MinIO中存储的对象名称（包含路径）
     * @param contentType 文件的MIME类型
     * @param size        文件大小（字节）
     * @return 成功上传后返回objectName
     * @throws Exception 上传过程中的各种异常
     */
    @Override
    public String uploadFile(InputStream inputStream, String objectName, String contentType, long size)
            throws Exception {
        
        // 参数校验
        if (inputStream == null) {
            throw new IllegalArgumentException("输入流不能为null");
        }
        
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }
        
        // 文件大小校验
        if (size > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("文件大小超出限制: %d bytes > %d bytes", size, maxFileSize));
        }
        
        // 确保bucket存在
        ensureBucketExists();
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 构建上传参数
            PutObjectArgs.Builder argsBuilder = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName);

            // 根据文件大小选择合适的上传策略
            if (size >= 0) {
                if (size > defaultPartSize) {
                    // 大文件使用分片上传
                    argsBuilder.stream(inputStream, size, defaultPartSize);
                    log.debug("使用分片上传: 文件大小={} bytes, 分片大小={} bytes", size, defaultPartSize);
                } else {
                    // 小文件直接上传
                    argsBuilder.stream(inputStream, size, -1);
                }
            } else {
                // 大小未知，使用默认分片大小
                log.warn("文件 '{}' 大小未知，使用默认分片上传策略", objectName);
                argsBuilder.stream(inputStream, -1, defaultPartSize);
            }

            // 设置Content-Type
            if (StringUtils.hasText(contentType)) {
                argsBuilder.contentType(contentType);
            } else {
                // 根据文件扩展名推测Content-Type
                String inferredContentType = inferContentType(objectName);
                if (StringUtils.hasText(inferredContentType)) {
                    argsBuilder.contentType(inferredContentType);
                    log.debug("根据文件扩展名推测Content-Type: {} -> {}", objectName, inferredContentType);
                } else {
                    log.warn("无法确定文件 '{}' 的Content-Type", objectName);
                }
            }

            // 执行上传
            minioClient.putObject(argsBuilder.build());
            
            long uploadTime = System.currentTimeMillis() - startTime;
            
            log.info("文件上传成功: bucket='{}', object='{}', size={} bytes, type='{}', 耗时={}ms",
                    bucketName, objectName, size >= 0 ? size : "未知", 
                    contentType != null ? contentType : "未知", uploadTime);
            
            return objectName;
            
        } catch (Exception e) {
            log.error("文件上传失败: bucket='{}', object='{}', size={} bytes, 错误={}", 
                    bucketName, objectName, size, e.getMessage(), e);
            
            // 包装异常，提供更友好的错误信息
            if (e instanceof ErrorResponseException) {
                ErrorResponseException ere = (ErrorResponseException) e;
                throw new RuntimeException("MinIO服务错误: " + ere.errorResponse().message(), e);
            } else if (e instanceof IOException) {
                throw new RuntimeException("文件读取错误: " + e.getMessage(), e);
            } else if (e instanceof InvalidKeyException || e instanceof NoSuchAlgorithmException) {
                throw new RuntimeException("MinIO认证错误: " + e.getMessage(), e);
            } else {
                throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 生成永久访问URL
     * 通过应用服务器代理访问，实现永久链接效果
     *
     * @param objectName 要下载的文件在存储系统中的唯一标识符
     * @param baseUrl 应用服务器的基础URL
     * @return 永久访问URL
     * @throws Exception 生成URL过程中的各种异常
     */
    public String generatePermanentUrl(String objectName, String baseUrl) throws Exception {
        // 参数校验
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }
        
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalArgumentException("基础URL不能为空");
        }
        
        try {
            // 检查文件是否存在
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            
            // 构建永久访问URL
            String permanentUrl = baseUrl + "/api/public/file/" + objectName;
            
            log.info("成功生成永久访问链接: bucket='{}', object='{}', url='{}'", 
                    bucketName, objectName, permanentUrl);
            
            return permanentUrl;
            
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                throw new RuntimeException("文件不存在: " + objectName);
            }
            throw new RuntimeException("MinIO服务错误: " + e.errorResponse().message(), e);
        } catch (Exception e) {
            log.error("生成永久访问链接失败: bucket='{}', object='{}', 错误={}", 
                    bucketName, objectName, e.getMessage(), e);
            throw new RuntimeException("生成永久访问链接失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成预签名下载URL
     * 支持有效期校验、文件存在性检查等功能
     *
     * @param objectName      要下载的文件在存储系统中的唯一标识符
     * @param durationSeconds URL的有效时长（秒）
     * @return 可供下载的URL对象
     * @throws Exception 生成URL过程中的各种异常
     */
    @Override
    public URL generatePresignedDownloadUrl(String objectName, int durationSeconds) throws Exception {
        
        // 参数校验
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }
        
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("URL有效时长必须为正数");
        }
        
        if (durationSeconds > maxUrlDuration) {
            throw new IllegalArgumentException(
                    String.format("URL有效期超出限制: %d秒 > %d秒", durationSeconds, maxUrlDuration));
        }
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 检查文件是否存在
            try {
                minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build());
            } catch (ErrorResponseException e) {
                if ("NoSuchKey".equals(e.errorResponse().code())) {
                    throw new RuntimeException("文件不存在: " + objectName);
                }
                throw e;
            }
            
            // 生成预签名URL
            String urlString = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(durationSeconds, TimeUnit.SECONDS)
                            .build());
            
            long generateTime = System.currentTimeMillis() - startTime;
            
            log.info("成功生成下载链接: bucket='{}', object='{}', 有效期={}秒, 耗时={}ms", 
                    bucketName, objectName, durationSeconds, generateTime);
            
            return new URL(urlString);
            
        } catch (MalformedURLException e) {
            log.error("生成的下载链接格式错误: {}", e.getMessage());
            throw new RuntimeException("下载链接格式错误", e);
        } catch (Exception e) {
            log.error("生成下载链接失败: bucket='{}', object='{}', 错误={}", 
                    bucketName, objectName, e.getMessage(), e);
            
            // 包装异常，提供更友好的错误信息
            if (e instanceof ErrorResponseException ere) {
                throw new RuntimeException("MinIO服务错误: " + ere.errorResponse().message(), e);
            } else if (e instanceof InvalidKeyException || e instanceof NoSuchAlgorithmException) {
                throw new RuntimeException("MinIO认证错误: " + e.getMessage(), e);
            } else {
                throw new RuntimeException("生成下载链接失败: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 根据文件扩展名推测Content-Type
     *
     * @param objectName 文件对象名称
     * @return 推测的Content-Type，如果无法推测则返回null
     */
    private String inferContentType(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            return null;
        }
        
        String lowerName = objectName.toLowerCase();
        
        // 图片类型
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerName.endsWith(".png")) {
            return "image/png";
        } else if (lowerName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerName.endsWith(".bmp")) {
            return "image/bmp";
        } else if (lowerName.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerName.endsWith(".svg")) {
            return "image/svg+xml";
        }
        
        // 视频类型
        else if (lowerName.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerName.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (lowerName.endsWith(".mov")) {
            return "video/quicktime";
        } else if (lowerName.endsWith(".wmv")) {
            return "video/x-ms-wmv";
        } else if (lowerName.endsWith(".flv")) {
            return "video/x-flv";
        } else if (lowerName.endsWith(".webm")) {
            return "video/webm";
        }
        
        // 音频类型
        else if (lowerName.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (lowerName.endsWith(".wav")) {
            return "audio/wav";
        } else if (lowerName.endsWith(".flac")) {
            return "audio/flac";
        } else if (lowerName.endsWith(".aac")) {
            return "audio/aac";
        } else if (lowerName.endsWith(".ogg")) {
            return "audio/ogg";
        }
        
        // 文档类型
        else if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerName.endsWith(".doc")) {
            return "application/msword";
        } else if (lowerName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (lowerName.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        } else if (lowerName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (lowerName.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        } else if (lowerName.endsWith(".pptx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if (lowerName.endsWith(".txt")) {
            return "text/plain";
        }
        
        // 压缩文件
        else if (lowerName.endsWith(".zip")) {
            return "application/zip";
        } else if (lowerName.endsWith(".rar")) {
            return "application/vnd.rar";
        } else if (lowerName.endsWith(".7z")) {
            return "application/x-7z-compressed";
        } else if (lowerName.endsWith(".tar")) {
            return "application/x-tar";
        } else if (lowerName.endsWith(".gz")) {
            return "application/gzip";
        }
        
        // 其他常见类型
        else if (lowerName.endsWith(".json")) {
            return "application/json";
        } else if (lowerName.endsWith(".xml")) {
            return "application/xml";
        } else if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) {
            return "text/html";
        } else if (lowerName.endsWith(".css")) {
            return "text/css";
        } else if (lowerName.endsWith(".js")) {
            return "application/javascript";
        }
        
        return null;
    }
}
