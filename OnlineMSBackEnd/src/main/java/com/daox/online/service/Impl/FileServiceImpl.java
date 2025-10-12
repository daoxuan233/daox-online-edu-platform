package com.daox.online.service.Impl;

import com.daox.online.controller.exception.AccessDeniedException;
import com.daox.online.controller.exception.ResourceNotFoundException;
import com.daox.online.entity.mysql.CourseMaterials;
import com.daox.online.entity.mysql.Files;
import com.daox.online.entity.dto.UploadRequestDto;
import com.daox.online.mapper.*;
import com.daox.online.service.ProgressService;
import io.minio.GetObjectResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import com.daox.online.service.FileManagementService;
import com.daox.online.service.FileService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Resource
    private FileManagementService fileManagementService; // 依赖抽象接口
    @Resource
    private FilesMapper filesMapper;
    @Resource
    private CourseMaterialsMapper courseMaterialsMapper;
    @Resource
    private PermissionMapper permissionMapper;
    @Value("${app.file-storage.minio.bucketName}")
    private String bucketName;
    @Resource
    private SectionMapper sectionMapper;
    @Resource
    private ProgressService progressService;
    @Resource
    private ChapterMapper chapterMapper;
    @Resource
    private CourseMapper courseMapper;

    /**
     * 处理文件上传，并将其与课程资料关联。
     *
     * @param request    包含文件和其他元数据的上传请求 DTO。
     * @param uploaderId 上传者（通常是教师）的用户ID。
     * @return 创建的课程资料记录。
     */
    @Override
    @Transactional // 声明式事务，确保数据库操作的原子性
    public CourseMaterials uploadAndLinkMaterial(UploadRequestDto request, String uploaderId) {
        try {
            // 1. 生成文件元数据
            String fileId = UUID.randomUUID().toString().replace("-", "");
            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(request.getFile().getOriginalFilename()));
            String objectName = String.format("materials/%s/%s_%s", request.getCourseId(), fileId, originalFilename);
            boolean isVideo = request.getFile().getContentType() != null && request.getFile().getContentType().startsWith("video/");
            boolean allowDownload = !isVideo && request.getAllowDownload();

            // 2. 调用文件存储服务上传文件
            fileManagementService.uploadFile(
                    bucketName,
                    objectName,
                    request.getFile().getInputStream(),
                    request.getFile().getSize(),
                    request.getFile().getContentType()
            );

            // 3. 在 'files' 表中插入记录
            Files fileRecord = Files.builder()
                    .id(fileId)
                    .originalFilename(originalFilename)
                    .objectName(objectName)
                    .fileType("material")
                    .mimeType(request.getFile().getContentType())
                    .fileSize(request.getFile().getSize())
                    .uploaderId(uploaderId)
                    .allowDownload(allowDownload)
                    .build();
            filesMapper.insert(fileRecord);

            // 4. 在 'course_materials' 表中插入记录
            CourseMaterials materialRecord = CourseMaterials.builder()
                    .id(UUID.randomUUID().toString().replace("-", ""))
                    .courseId(request.getCourseId())
                    .chapterId(request.getChapterId())
                    .sectionId(request.getSectionId())
                    .title(request.getTitle())
                    .fileId(fileId)
                    .build();
            courseMaterialsMapper.insert(materialRecord);

            log.info("成功上传文件 {} 并关联到课程 {}", fileId, request.getCourseId());
            return materialRecord;

        } catch (Exception e) {
            log.error("为课程 {} 上传资料失败", request.getCourseId(), e);
            throw new RuntimeException("文件上传失败，请稍后重试。", e);
        }
    }

    /**
     * 统一处理文件上传并将其智能关联到课程、章节或小节。
     *
     * @param request    包含文件和关联信息的统一DTO。
     * @param uploaderId 上传者ID。
     * @return 返回一个代表操作结果的对象（如 Files, CourseMaterials, Sections）。
     */
    @Override
    @Transactional
    public Object uploadAndAssociateFile(UploadRequestDto request, String uploaderId) {
        // 1. 基础校验：至少提供一个关联ID
        if (!StringUtils.hasText(request.getCourseId()) &&
                !StringUtils.hasText(request.getChapterId()) &&
                !StringUtils.hasText(request.getSectionId())) {
            throw new IllegalArgumentException("必须提供 courseId, chapterId, 或 sectionId 中的至少一个来指定关联目标。");
        }

        try {
            // 2. 通用逻辑：上传文件到 MinIO 并在 files 表创建记录
            Files fileRecord = uploadFileAndGetRecord(request, uploaderId); // 这是一个需要实现的私有辅助方法

            // 3. 核心业务分发逻辑
            boolean isVideo = fileRecord.getMimeType() != null && fileRecord.getMimeType().startsWith("video/");

            // --- 场景一：处理视频上传 ---
            if (isVideo) {
                // 视频必须且只能关联到小节
                if (!StringUtils.hasText(request.getSectionId())) {
                    throw new IllegalArgumentException("视频文件必须关联到一个具体的小节 (sectionId)。");
                }
                // 校验视频时长
                if (request.getDurationSeconds() == null || request.getDurationSeconds() <= 0) {
                    throw new IllegalArgumentException("上传视频文件时必须提供有效的 'durationSeconds'。");
                }

                // 校验小节是否存在 (此处省略了 mapper.findById 的调用)
                log.info("开始将视频文件 {} 关联到小节 {}", fileRecord.getId(), request.getSectionId());
                sectionMapper.updateVideoInfo(request.getSectionId(), fileRecord.getId(), request.getDurationSeconds());
                // 这里可以返回更新后的小节实体，或者返回 File 记录
                return fileRecord;
            }

            // --- 场景二：处理非视频资料上传 ---
            else {
                if (!StringUtils.hasText(request.getTitle())) {
                    request.setTitle(fileRecord.getOriginalFilename()); // 默认标题
                }

                CourseMaterials materialRecord = CourseMaterials.builder()
                        .id(UUID.randomUUID().toString().replace("-", ""))
                        .fileId(fileRecord.getId())
                        .title(request.getTitle())
                        // 智能判断关联层级
                        .courseId(determineCourseId(request)) // 这是一个需要实现的私有辅助方法
                        .chapterId(request.getChapterId()) // 可以为 null
                        .sectionId(request.getSectionId()) // 可以为 null
                        .build();

                courseMaterialsMapper.insert(materialRecord);
                log.info("成功上传资料文件 {} 并关联到目标。", fileRecord.getId());
                return materialRecord;
            }
        } catch (Exception e) {
            log.error("文件上传和关联过程中发生错误", e);
            throw new RuntimeException("文件处理失败。", e);
        }
    }

    /**
     * 提供文件访问服务。
     * 根据文件类型和权限，以流式或下载方式响应。
     *
     * @param fileId   要访问的文件ID。
     * @param userId   请求访问的用户ID。
     * @param request  HTTP 请求对象，用于获取 Range 头。
     * @param response HTTP 响应对象，用于写入文件流。
     */
    @Override
    public void serveFile(String fileId, String userId, HttpServletRequest request, HttpServletResponse response) {
        // 1. 权限校验
        if (permissionMapper.checkUserAccessToFile(userId, fileId) == null) {
            throw new AccessDeniedException("您没有权限访问此文件。");
        }

        // 2. 获取文件元数据
        Files fileInfo = filesMapper.findById(fileId);
        if (fileInfo == null) {
            throw new ResourceNotFoundException("文件不存在 (ID: " + fileId + ")");
        }

        // 3. 触发学习进度处理
        String sectionId = filesMapper.findSectionIdByFileId(fileId);
        if (sectionId != null) {
            // 调用 ProgressService 来处理状态变换
            progressService.initOrUpdateProgressOnAccess(userId, sectionId, fileInfo);
        } else {
            log.warn("文件 {} 未关联到任何小节，无法记录学习进度。", fileId);
        }

        // 4. 根据文件类型和权限分发处理
        String mimeType = fileInfo.getMimeType();
        if (mimeType != null && mimeType.startsWith("video/")) {
            serveVideoStream(fileInfo, request, response);
        } else {
            serveDocument(fileInfo, response);
        }
    }

    /**
     * 处理文档文件
     * @param fileInfo 文件信息
     * @param response 响应对象
     */
    private void serveDocument(Files fileInfo, HttpServletResponse response) {
        String dispositionType = Boolean.TRUE.equals(fileInfo.getAllowDownload()) ? "attachment" : "inline";
        try (GetObjectResponse fileStream = fileManagementService.downloadFile(bucketName, fileInfo.getObjectName())) {
            response.setContentType(fileInfo.getMimeType());
            response.setContentLengthLong(fileInfo.getFileSize());
            String encodedFileName = URLEncoder.encode(fileInfo.getOriginalFilename(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

            try (OutputStream os = response.getOutputStream()) {
                fileStream.transferTo(os);
            }
        } catch (Exception e) {
            log.error("服务文档文件 {} 时出错", fileInfo.getId(), e);
            throw new RuntimeException("服务文档文件时发生内部错误。", e);
        }
    }

    /**
     * 处理视频文件
     * @param fileInfo 文件信息
     * @param request HTTP 请求对象，用于获取 Range 头。
     * @param response HTTP 响应对象，用于写入文件流。
     */
    private void serveVideoStream(Files fileInfo, HttpServletRequest request, HttpServletResponse response) {
        long fileSize = fileInfo.getFileSize();
        String rangeHeader = request.getHeader(HttpHeaders.RANGE);

        response.setContentType(fileInfo.getMimeType());
        response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

        if (rangeHeader == null) {
            // 如果没有 Range 请求，则返回整个文件信息，但不返回内容
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize));
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        long start = 0;
        long end = fileSize - 1;

        Pattern pattern = Pattern.compile("bytes=(\\d*)-(\\d*)");
        Matcher matcher = pattern.matcher(rangeHeader);
        if (matcher.matches()) {
            String startGroup = matcher.group(1);
            if (StringUtils.hasText(startGroup)) {
                start = Long.parseLong(startGroup);
            }
            String endGroup = matcher.group(2);
            if (StringUtils.hasText(endGroup)) {
                end = Long.parseLong(endGroup);
            }
        }

        long contentLength = (end - start) + 1;

        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
        response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);

        try (GetObjectResponse fileStream = fileManagementService.downloadFileRange(bucketName, fileInfo.getObjectName(), start, contentLength);
             OutputStream os = response.getOutputStream()) {
            fileStream.transferTo(os);
        } catch (Exception e) {
            log.error("流式传输视频 {} 时出错", fileInfo.getId(), e);
            throw new RuntimeException("流式传输视频时发生内部错误。", e);
        }
    }

    /**
     * [私有辅助方法] 根据 DTO 中提供的关联ID，安全且高效地确定文件所属的顶级课程ID。
     * 该方法用于构建文件的物理存储路径，并确保数据关联的正确性。
     *
     * @param request 包含关联ID的文件上传DTO。
     * @return 查找到的课程ID。
     * @throws ResourceNotFoundException 如果提供的ID在数据库中不存在。
     * @throws IllegalStateException 如果无法根据提供的ID确定课程ID（理论上不应发生）。
     */
    private String determineCourseId(UploadRequestDto request) {
        // 优先级1：直接提供了 courseId
        if (StringUtils.hasText(request.getCourseId())) {
            // 最佳实践：校验 courseId 是否真实存在
            if (courseMapper.existsById(request.getCourseId())) { // 假设 courseMapper 有此方法
                return request.getCourseId();
            } else {
                throw new ResourceNotFoundException("关联的课程不存在 (ID: " + request.getCourseId() + ")");
            }
        }

        // 优先级2：提供了 chapterId
        if (StringUtils.hasText(request.getChapterId())) {
            String courseId = chapterMapper.findCourseIdByChapterId(request.getChapterId());
            if (courseId == null) {
                // 如果找不到，说明 chapterId 无效
                throw new ResourceNotFoundException("关联的章节不存在 (ID: " + request.getChapterId() + ")");
            }
            return courseId;
        }

        // 优先级3：提供了 sectionId (使用性能更优的单次JOIN查询)
        if (StringUtils.hasText(request.getSectionId())) {
            String courseId = sectionMapper.findCourseIdBySectionId(request.getSectionId());
            if (courseId == null) {
                // 如果找不到，说明 sectionId 无效
                throw new ResourceNotFoundException("关联的小节不存在 (ID: " + request.getSectionId() + ")");
            }
            return courseId;
        }

        // 安全防卫代码：根据主方法开头的校验，此行代码理论上永远不会被执行。
        // 但保留它可以增加代码的健壮性，防止未来重构时意外移除校验。
        throw new IllegalStateException("无法确定课程ID，必须提供有效的 courseId, chapterId, 或 sectionId。");
    }
    /**
     * [私有辅助方法] 封装了文件上传到 MinIO 和在 'files' 表中创建记录的通用逻辑。
     *
     * @param request    包含文件和元数据的统一上传DTO。
     * @param uploaderId 执行上传操作的用户ID。
     * @return 创建并持久化到数据库的 Files 实体对象。
     * @throws Exception 如果文件上传或数据库插入失败。
     */
    private Files uploadFileAndGetRecord(UploadRequestDto request, String uploaderId) throws Exception {
        MultipartFile file = request.getFile();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件内容不能为空。");
        }

        // 1. 确定文件最终归属的课程ID，用于构建存储路径
        //    这个 courseId 是通过 DTO 中传入的 courseId/chapterId/sectionId 反查得到的，
        //    以保证路径的逻辑归属正确。
        String courseId = determineCourseId(request);

        // 2. 准备文件元数据
        String fileId = UUID.randomUUID().toString().replace("-", "");
        // 使用 StringUtils.cleanPath 防止目录遍历攻击，并处理不同操作系统的路径分隔符
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        // 3. 构建在 MinIO 中的对象名称 (Object Name)，包含逻辑路径
        //    格式: materials/{courseId}/{uuid}_{originalFilename}
        //    这样的结构便于按课程管理和查找文件。
        String objectName = String.format("materials/%s/%s_%s", courseId, fileId, originalFilename);

        // 4. 调用文件存储服务，将文件上传到 MinIO
        //    这里将 I/O 操作放在数据库事务之外，或者如果存储服务支持事务，则进行相应配置。
        //    通常建议先上传物理文件，成功后再写入数据库记录。
        fileManagementService.uploadFile(
                bucketName,
                objectName,
                file.getInputStream(),
                file.getSize(),
                file.getContentType()
        );
        log.info("文件已成功上传到 MinIO，对象名称: {}", objectName);

        // 5. 准备要存入数据库的 Files 实体对象
        boolean isVideo = file.getContentType() != null && file.getContentType().startsWith("video/");
        String fileType = isVideo ? "video" : "document"; // 可以更细致地分类

        Files fileRecord = Files.builder()
                .id(fileId)
                .originalFilename(originalFilename)
                .objectName(objectName)
                .fileType(fileType) // 根据MIME类型设置一个逻辑文件类型
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .uploaderId(uploaderId)
                // 对于视频，allowDownload 字段无意义，但可以统一设置
                // 对于非视频文件，使用 DTO 中传入的值
                .allowDownload(!isVideo && request.getAllowDownload())
                .build();

        // 6. 将文件元数据持久化到数据库的 'files' 表中
        filesMapper.insert(fileRecord);
        log.info("文件元数据已成功存入数据库，File ID: {}", fileId);

        // 7. 返回创建好的 Files 实体，供上层调用方法继续使用
        return fileRecord;
    }
}
