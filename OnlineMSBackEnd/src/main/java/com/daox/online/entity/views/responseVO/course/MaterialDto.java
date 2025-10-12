package com.daox.online.entity.views.responseVO.course;

import com.daox.online.entity.mysql.CourseMaterials;
import com.daox.online.entity.mysql.Files;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 课程资料视图对象
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MaterialDto {
    /**
     * 资料ID (course_materials.id)
     */
    private String id;
    /**
     * 资料标题
     */
    private String title;
    /**
     * 关联的文件ID
     * @see Files#getId()
     * @see CourseMaterials#getFileId()
     * @see com.daox.online.controller.students.LearningController#getFile(String, HttpServletRequest, HttpServletResponse) 
     */
    private String fileId;
    /**
     * 文件类型 (e.g., 'application/pdf')，用于前端显示对应的文件图标
     */
    private String mimeType;
}
