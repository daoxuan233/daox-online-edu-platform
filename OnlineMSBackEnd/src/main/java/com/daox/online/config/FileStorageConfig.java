package com.daox.online.config;

import com.daox.online.service.FileStorageService;
import com.daox.online.service.Impl.MinioFileStorageService;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 文件存储配置类。
 * 负责根据配置文件创建 MinIO 客户端 Bean，
 * 并将 MinioFileStorageService 注册为 FileStorageService 接口的实现 Bean。
 */
@Slf4j
@Configuration
public class FileStorageConfig {
    /**
     * 创建并配置 MinIO 客户端 Bean。
     * 从配置文件中读取 MinIO 的 Endpoint、Access Key 和 Secret Key。
     *
     * @param endpoint  MinIO 服务地址 (从 application.yml 读取)
     * @param accessKey MinIO Access Key (从 application.yml 读取)
     * @param secretKey MinIO Secret Key (从 application.yml 读取)
     * @return 配置好的 MinioClient 实例。
     * @throws IllegalStateException 如果关键配置缺失。
     */
    @Bean
    public MinioClient minioClient(
            @Value("${app.file-storage.minio.endpoint}") String endpoint,
            @Value("${app.file-storage.minio.accessKey}") String accessKey,
            @Value("${app.file-storage.minio.secretKey}") String secretKey) {

        log.info("开始配置 MinIO 客户端，Endpoint: {}", endpoint);

        if (!StringUtils.hasText(endpoint)) {
            throw new IllegalStateException("MinIO endpoint is missing in configuration (app.file-storage.minio.endpoint).");
        }
        if (!StringUtils.hasText(accessKey)) {
            throw new IllegalStateException("MinIO accessKey is missing in configuration (app.file-storage.minio.accessKey).");
        }
        if (!StringUtils.hasText(secretKey)) {
            throw new IllegalStateException("MinIO secretKey is missing in configuration (app.file-storage.minio.secretKey).");
        }

        try {
            // 使用 MinioClient.builder() 创建客户端实例
            MinioClient client = MinioClient.builder()
                    .endpoint(endpoint) // 设置 MinIO 服务地址
                    .credentials(accessKey, secretKey) // 设置访问凭证
                    // .httpClient(...) // 可选：自定义 OkHttpClient 配置 (例如设置超时)
                    .build();
            log.info("MinIO 客户端 Bean 创建成功。");
            return client;
        } catch (Exception e) {
            log.error("创建 MinIO 客户端时发生错误", e);
            // 抛出异常，阻止应用启动，因为文件存储是核心功能
            throw new RuntimeException("无法创建 MinIO 客户端 Bean: " + e.getMessage(), e);
        }
    }

    /**
     * 创建文件存储服务 Bean。
     * 将 MinioFileStorageService 注册为 FileStorageService 接口的实现。
     * Spring 会自动注入上面创建的 MinioClient Bean。
     *
     * @param minioClient 自动注入的 MinioClient Bean。
     * @return FileStorageService 的 MinIO 实现实例。
     */
    @Bean
    public FileStorageService fileStorageService(MinioClient minioClient) {
        log.info("创建 MinioFileStorageService Bean...");
        // 创建 MinIO 文件存储服务的实例，并将 MinIO 客户端注入
        MinioFileStorageService service = new MinioFileStorageService(minioClient);
        log.info("MinioFileStorageService Bean 创建成功。");
        // 返回接口类型，符合面向接口编程原则
        return service;
    }
}
