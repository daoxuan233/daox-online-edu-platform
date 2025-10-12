package com.daox.online.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 课程章节的数据传输对象。
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ChapterDto {
    private String id;          // 章节ID
    private String title;       // 章节标题
    private Integer orderIndex; // 显示顺序
    private List<SectionDto> sections; // 该章节下的所有小节列表
}
