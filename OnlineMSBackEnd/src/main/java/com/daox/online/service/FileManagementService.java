package com.daox.online.service;

import io.minio.GetObjectResponse;

import java.io.InputStream;

/**
 * 文件管理服务接口
 * 定义了文件上传、下载和分片下载的通用操作，实现与底层存储技术的解耦。
 */
public interface FileManagementService {
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
    void uploadFile(String bucketName, String objectName, InputStream stream, long size, String contentType) throws Exception;

    /**
     * 下载完整文件。
     *
     * @param bucketName 存储桶名称。
     * @param objectName 文件在存储桶中的唯一名称。
     * @return 包含文件流的 GetObjectResponse 对象。
     * @throws Exception 下载过程中发生的任何错误。
     */
    GetObjectResponse downloadFile(String bucketName, String objectName) throws Exception;

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
    GetObjectResponse downloadFileRange(String bucketName, String objectName, long offset, long length) throws Exception;
}