package com.daox.ai.entity;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 课程大纲 DTO
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CourseOutlineDto implements Serializable {
    /**
     * 课程大纲
     */
    private List<ChapterDto> outline;

    /**
     * 内部类，用于表示章节
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class ChapterDto {
        private String title;
        /**
         * 显示顺序
         */
        private int orderIndex;
        private List<SectionDto> sections;
    }

    /**
     * 内部类，用于表示小节
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class SectionDto {
        private String title;
        /**
         * 显示顺序
         */
        private int orderIndex;
    }
}
