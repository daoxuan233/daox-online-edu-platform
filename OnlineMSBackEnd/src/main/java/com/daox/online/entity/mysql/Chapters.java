package com.daox.online.entity.mysql;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 章节表<br />
 * 将一门复杂的课程分解为多个逻辑单元（章节）。<br />
 * 它作为课程内容的组织层级，每个章节下可包含若干个具体的小节。<br />
 * TableName: chapters
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Chapters implements Serializable {

    /**
     * 章节ID
     */
    private String id;
    /**
     * 课程ID
     */
    private String courseId;
    /**
     * 章节标题
     */
    private String title;
    /**
     * 显示顺序
     */
    private Integer orderIndex;

}
