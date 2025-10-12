package com.daox.online.service;

import java.io.InputStream;
import java.net.URL;

/**
 * 文件存储服务接口
 */
public interface FileStorageService {

    /**
     * 上传文件 (通过输入流)。
     * 适用于文件内容不是来自 HTTP 请求，而是程序内部生成或从其他来源获取的情况。
     *
     * @param inputStream 文件内容的输入流。调用者负责关闭此流 (除非实现明确说明会关闭)。
     * @param objectName  必须提供的对象名称 (存储路径+文件名)。服务应使用此名称。
     * @param contentType 文件的 MIME 类型 (例如 "image/jpeg", "application/pdf")。
     * @param size        文件的大小 (字节)。对于某些存储系统 (如 S3 分片上传) 可能需要。
     * @return 文件在存储系统中的唯一标识符 (通常等于传入的 objectName)。保证非 null。
     * @throws Exception 如果上传过程中发生任何错误。
     */
    String uploadFile(InputStream inputStream, String objectName, String contentType, long size) throws Exception;

    /**
     * 生成一个有时效性的、预签名的文件下载 URL。
     * 客户端可以使用此 URL 直接从存储系统下载文件，而无需通过应用服务器中转。
     *
     * @param objectName    要下载的文件在存储系统中的唯一标识符。
     * @param durationSeconds URL 的有效时长 (秒)。存储系统策略可能限制最大时长。
     * @return 可供下载的 URL 对象。保证非 null。
     * @throws Exception 如果生成 URL 过程中发生错误 (例如文件不存在、权限不足)。
     */
    URL generatePresignedDownloadUrl(String objectName, int durationSeconds) throws Exception;
    
    /**
     * 生成永久访问URL。
     * 通过应用服务器代理访问，实现永久链接效果。
     *
     * @param objectName 要下载的文件在存储系统中的唯一标识符。
     * @param baseUrl 应用服务器的基础URL。
     * @return 永久访问URL字符串。保证非 null。
     * @throws Exception 如果生成 URL 过程中发生错误 (例如文件不存在、权限不足)。
     */
    String generatePermanentUrl(String objectName, String baseUrl) throws Exception;
}
