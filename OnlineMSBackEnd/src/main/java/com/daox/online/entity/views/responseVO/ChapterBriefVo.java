package com.daox.online.entity.views.responseVO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 章节简要信息
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ChapterBriefVo {
    /**
     * 章节id
     */
    private String id;
    /**
     * 章节标题
     */
    private String title;
    /**
     * 章节顺序
     */
    private Integer order;
    /**
     * 章节下的小节数量
     */
    private Integer sectionCount;
}
