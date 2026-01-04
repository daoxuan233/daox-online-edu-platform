package com.daox.pptAgent.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class MinioService {

    private final MinioClient minioClient;
    private final String bucketName;
    private final String endpoint;

    public MinioService(
            @Value("${app.file-storage.minio.endpoint}") String endpoint,
            @Value("${app.file-storage.minio.accessKey}") String accessKey,
            @Value("${app.file-storage.minio.secretKey}") String secretKey,
            @Value("${app.file-storage.minio.bucketName}") String bucketName
    ) {
        this.minioClient = MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
        this.bucketName = bucketName;
        this.endpoint = endpoint;
    }

    /**
     * 上传文件
     *
     * @param stream      文件输入流
     * @param size        文件大小
     * @param contentType 文件类型
     * @return 文件访问URL
     * @throws Exception 上传失败时抛出异常
     */
    public String uploadFile(InputStream stream, long size, String contentType) throws Exception {
        ensureBucketExists();
        String objectName = UUID.randomUUID() + ".pptx";
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(stream, size, -1)
                        .contentType(contentType)
                        .build()
        );
        return String.join("/", endpoint, bucketName, objectName);
    }

    /**
     * 确保Bucket存在
     *
     * @throws Exception 创建Bucket失败时抛出异常
     */
    private void ensureBucketExists() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            log.info("[ensureBucketExists.method] Bucket '{}' not found. Creating it...", bucketName);
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("[ensureBucketExists.method] Bucket '{}' created successfully.", bucketName);
        } else {
            log.debug("[ensureBucketExists.method] Bucket '{}' already exists.", bucketName);
        }
    }
}