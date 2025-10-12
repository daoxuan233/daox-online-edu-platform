package com.daox.online.entity.views.requestVO.teacher;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 课程资料请求VO
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class CourseMaterialVo {
    /**
     * 课程ID
     */
    @NotBlank(message = "课程ID不能为空")
    private String courseId;
    /**
     * 章节ID (可选)
     */
    private String chapterId;
    /**
     * 小节ID (可选)
     */
    private String sectionId;
    /**
     * 资料标题
     */
    @NotBlank(message = "资料标题不能为空")
    private String title;
    /**
     * 资料描述
     */
    private String description;
    /**
     * 课件资料URL
     */
    @NotBlank(message = "资料URL不能为空")
    private String materialUrl;
    /**
     * 是否公开 1=公开，0=仅选课学生可见
     */
    private Integer isPublic;
}
