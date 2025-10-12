package com.daox.online.uilts;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class FileUtils {
    /**
     * 验证头像文件类型是否被允许
     *
     * @param contentType   文件MIME类型
     * @param fileExtension 文件扩展名
     * @return 是否允许上传
     */
    public boolean isAllowedAvatarFileType(String contentType, String fileExtension) {
        // 定义允许的头像MIME类型
        Set<String> allowedMimeTypes = new HashSet<>(Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"));

        // 定义允许的头像文件扩展名
        Set<String> allowedExtensions = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp"));

        // 检查MIME类型
        boolean mimeTypeAllowed = contentType != null && allowedMimeTypes.contains(contentType.toLowerCase());

        // 检查文件扩展名
        boolean extensionAllowed = fileExtension != null && allowedExtensions.contains(fileExtension.toLowerCase());

        // 只要其中一个匹配就允许（考虑到某些情况下MIME类型可能不准确）
        return mimeTypeAllowed || extensionAllowed;
    }
}
