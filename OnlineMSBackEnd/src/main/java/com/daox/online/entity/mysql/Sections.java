package com.daox.online.entity.mysql;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 小节表<br />
 * 课程内容的最小学习单元，通常对应一个教学视频或一篇文档。学生的学习进度将记录到小节级别。<br />
 * TableName:  sections
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Sections implements Serializable {

    /**
     * 小节ID
     */
    private String id;
    /**
     * 章节ID
     */
    private String chapterId;
    /**
     * 小节标题
     */
    private String title;
    /**
     * 视频URL/文件URL
     */
    private String videoUrl;
    /**
     * 视频时长(秒)/如果是文件则为空
     */
    private Integer durationSeconds;
    /**
     * 显示顺序
     */
    private Integer orderIndex;
}
