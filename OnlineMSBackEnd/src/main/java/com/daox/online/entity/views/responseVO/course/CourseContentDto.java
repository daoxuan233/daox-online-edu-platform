package com.daox.online.entity.views.responseVO.course;

import com.daox.online.entity.dto.ChapterDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 课程完整内容的顶层数据传输对象。
 * 包含了课程级别的资料和所有章节。
 * 这是整个接口返回给前端的最终数据结构。
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class CourseContentDto {

    /**
     * 课程id
     */
    private String courseId;

    /**
     * 课程标题
     */
    private String title;

    /**
     * 学习进度百分比
     */
    private Double progressPercentage;

    /**
     * 存放属于整个课程级别的所有附加资料列表。
     * 例如：“课程教学大纲.pdf”
     */
    private List<MaterialDto> courseMaterials;

    /**
     * 课程的所有章节列表，每个章节内部包含了小节和章节资料。
     */
    private List<ChapterDto> chapters;
}
