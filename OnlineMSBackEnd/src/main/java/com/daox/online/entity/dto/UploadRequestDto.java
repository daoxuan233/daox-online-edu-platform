package com.daox.online.entity.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * 课程资料上传请求的数据传输对象 (DTO)。
 * 用于封装教师上传课程资料时，通过 multipart/form-data 形式提交的
 * 文件本身以及相关的元数据。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UploadRequestDto implements Serializable {
    /**
     * 上传的文件本身。
     * Spring Boot 会自动将请求中的文件部分绑定到此字段。
     * - @NotNull: 确保请求中必须包含文件部分，不能为空。
     */
    @NotNull(message = "上传文件不能为空")
    private MultipartFile file;

    /**
     * 资料关联的课程ID。
     * 这是一个必填项，因为所有资料都必须属于某一门课程。
     * - @NotBlank: 确保课程ID不为空，且不只包含空白字符。
     */
    @NotBlank(message = "课程ID不能为空")
    private String courseId;

    /**
     * 资料关联的章节ID (可选)。
     * 如果资料是章节级别的，则需要此字段。
     * 如果是课程级别的资料，则此字段为空。
     */
    private String chapterId;

    /**
     * 资料关联的小节ID (可选)。
     * 如果资料是小节级别的，则需要此字段。
     * 如果是课程或章节级别的资料，则此字段为空。
     */
    private String sectionId;

    /**
     * 视频时长（秒）。
     * 仅当上传的文件是视频时，此字段为必填。
     */
    @Positive(message = "视频时长必须为正数")
    private Integer durationSeconds;

    /**
     * 资料的标题。
     * 这是一个必填项，用于在前端展示。
     * - @NotBlank: 确保标题不为空，且不只包含空白字符。
     */
    @NotBlank(message = "资料标题不能为空")
    private String title;

    /**
     * 是否允许学生下载该文件 (针对非视频文件)。
     * 前端可以传递此布尔值。
     * 如果前端未传递，则默认为 true，即允许下载。
     */
    private Boolean allowDownload = true;
}
