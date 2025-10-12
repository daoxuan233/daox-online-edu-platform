package com.daox.online.service.Impl;

import com.daox.online.service.FileManagementService;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * 文件管理服务实现类
 */
@Service
@Slf4j
public class FileManagementServiceImpl implements FileManagementService {
    @Resource
    private MinioClient minioClient;
    // private final MinioClient minioClient;
    // 通过构造函数注入您配置类中创建的 MinioClient Bean
//    public MinioFileStorageService(MinioClient minioClient) {
//        this.minioClient = minioClient;
//    }

    /**
     * 上传文件。
     *
     * @param bucketName  存储桶名称。
     * @param objectName  文件在存储桶中的唯一名称（路径）。
     * @param stream      文件输入流。
     * @param size        文件大小。
     * @param contentType 文件的MIME类型。
     * @throws Exception 上传过程中发生的任何错误。
     */
    @Override
    public void uploadFile(String bucketName, String objectName, InputStream stream, long size, String contentType) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(stream, size, -1) // 使用 -1 表示流的大小未知，让 SDK 自动处理
                        .contentType(contentType)
                        .build()
        );
    }

    /**
     * 下载完整文件。
     *
     * @param bucketName 存储桶名称。
     * @param objectName 文件在存储桶中的唯一名称。
     * @return 包含文件流的 GetObjectResponse 对象。
     * @throws Exception 下载过程中发生的任何错误。
     */
    @Override
    public GetObjectResponse downloadFile(String bucketName, String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    /**
     * 下载文件的指定字节范围（用于视频流）。
     *
     * @param bucketName 存储桶名称。
     * @param objectName 文件在存储桶中的唯一名称。
     * @param offset     起始字节位置。
     * @param length     要读取的字节长度。
     * @return 包含文件分片流的 GetObjectResponse 对象。
     * @throws Exception 下载过程中发生的任何错误。
     */
    @Override
    public GetObjectResponse downloadFileRange(String bucketName, String objectName, long offset, long length) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .offset(offset)
                        .length(length)
                        .build()
        );
    }
}