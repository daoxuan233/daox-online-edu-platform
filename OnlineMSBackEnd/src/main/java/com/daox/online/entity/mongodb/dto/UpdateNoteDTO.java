package com.daox.online.entity.mongodb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateNoteDTO {
    // 更新时，标题、内容、标签等都可以被修改
    private String title;
    private String content;
    private List<String> tags;
    private Boolean isPrivate;
}
