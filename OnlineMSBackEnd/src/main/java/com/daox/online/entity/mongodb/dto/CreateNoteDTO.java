package com.daox.online.entity.mongodb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateNoteDTO implements Serializable {
    // courseId, chapterId, sectionId 都是可选的，不提供就是自由笔记
    private String courseId;
    private String chapterId;
    private String sectionId;

    // title 和 content 是必填项
    private String title;
    private String content;

    private List<String> tags;
    private Boolean isPrivate = true;
    private Integer videoTime;
}
