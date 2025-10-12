package com.daox.online.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
        private String id;
        private String title;
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
        private String id;
        private String title;
        private String videoUrl;
        private int durationSeconds;
        private int orderIndex;
    }
}
