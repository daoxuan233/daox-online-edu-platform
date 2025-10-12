package com.daox.online.service;

import com.daox.online.entity.mysql.CourseMaterials;
import com.daox.online.entity.dto.UploadRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 核心文件业务服务接口。
 * 定义了文件上传、关联以及文件访问服务的顶层业务逻辑。
 */
public interface FileService {

    /**
     * 处理文件上传，并将其与课程资料关联。
     *
     * @param request    包含文件和其他元数据的上传请求 DTO。
     * @param uploaderId 上传者（通常是教师）的用户ID。
     * @return 创建的课程资料记录。
     */
    CourseMaterials uploadAndLinkMaterial(UploadRequestDto request, String uploaderId);

    /**
     * 统一处理文件上传并将其智能关联到课程、章节或小节。
     *
     * @param request    包含文件和关联信息的统一DTO。
     * @param uploaderId 上传者ID。
     * @return 返回一个代表操作结果的对象（如 Files, CourseMaterials, Sections）。
     */
    Object uploadAndAssociateFile(UploadRequestDto request, String uploaderId);

    /**
     * 提供文件访问服务。
     * 根据文件类型和权限，以流式或下载方式响应。
     *
     * @param fileId   要访问的文件ID。
     * @param userId   请求访问的用户ID。
     * @param request  HTTP 请求对象，用于获取 Range 头。
     * @param response HTTP 响应对象，用于写入文件流。
     */
    void serveFile(String fileId, String userId, HttpServletRequest request, HttpServletResponse response);
}
