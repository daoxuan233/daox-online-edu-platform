package com.daox.online.controller.teacher;

import com.daox.online.entity.mysql.CourseMaterials;
import com.daox.online.entity.RestBean;
import com.daox.online.entity.dto.UploadRequestDto;
import com.daox.online.service.FileService;
import com.daox.online.uilts.UserUtils;
import com.daox.online.uilts.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 教师文件管理控制器
 * 实现资源文件管理的所有接口
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher")
public class FileManagementController {

    @Resource
    private FileService fileService;
    
    @Resource
    private JwtUtils jwtUtils;

    /**
     * 教师端统一文件上传接口
     * 使用 @ModelAttribute 来接收 multipart/form-data 请求。
     * @param request            上传请求的数据传输对象
     * @param httpServletRequest HTTP 请求对象
     * @return 上传结果的响应对象
     */
    @PostMapping("/courses/files/upload")
    public RestBean<?> uploadAndAssociateFile(@Valid @ModelAttribute UploadRequestDto request,HttpServletRequest httpServletRequest) {
        String currentUserId = UserUtils.getCurrentUserId(httpServletRequest);
        if (currentUserId == null || currentUserId.isEmpty()) {
            log.warn("[uploadAndAssociateFile.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        Object result = fileService.uploadAndAssociateFile(request, currentUserId);
        return RestBean.success(result);
    }

    /**
     * 上传课程资料接口
     * 使用 @ModelAttribute 来接收 multipart/form-data 请求。
     *
     * @param request            课程资料上传请求的数据传输对象
     * @param httpServletRequest HTTP 请求对象
     * @return 创建的课程资料对象
     */
    @PostMapping("/materials/upload")
    public RestBean<CourseMaterials> uploadMaterial(@Valid @ModelAttribute UploadRequestDto request, HttpServletRequest httpServletRequest) {
        String currentUserId = UserUtils.getCurrentUserId(httpServletRequest);
        if (currentUserId == null || currentUserId.isEmpty()) {
            log.warn("[uploadMaterial.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }
        CourseMaterials createdMaterial = fileService.uploadAndLinkMaterial(request, currentUserId);
        return RestBean.success(createdMaterial);
    }

    /**
     * 统一文件访问接口。
     * 处理视频流、文件下载和在线预览。
     * 支持从URL参数中获取token进行认证。
     */
    @GetMapping("/files/{fileId}")
    public void getFile(@PathVariable String fileId, 
                       @RequestParam(value = "token", required = false) String token,
                       HttpServletRequest request, HttpServletResponse response) {
        String currentUserId = getCurrentUserIdWithTokenSupport(request, token);
        fileService.serveFile(fileId, currentUserId, request, response);
    }
    
    /**
     * 获取当前用户ID，支持从URL参数中的token进行认证
     * @param request HTTP请求对象
     * @param token URL参数中的token
     * @return 用户ID
     */
    private String getCurrentUserIdWithTokenSupport(HttpServletRequest request, String token) {
        // 首先尝试从请求属性获取（正常的JWT认证流程）
        String userId = UserUtils.getCurrentUserId(request);
        if (userId != null) {
            return userId;
        }
        
        // 如果请求属性中没有用户ID，尝试从URL参数的token中解析
        if (token != null && !token.trim().isEmpty()) {
            try {
                // JwtUtils.resolveJWT方法期望token以"Bearer "开头，所以需要添加前缀
                com.auth0.jwt.interfaces.DecodedJWT jwt = jwtUtils.resolveJWT("Bearer " + token);
                if (jwt != null) {
                    return jwtUtils.toId(jwt);
                }
            } catch (Exception e) {
                log.warn("从URL参数解析token失败: {}", e.getMessage());
            }
        }
        
        return null;
    }
}