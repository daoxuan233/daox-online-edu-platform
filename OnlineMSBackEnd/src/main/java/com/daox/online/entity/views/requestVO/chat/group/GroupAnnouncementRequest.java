package com.daox.online.entity.views.requestVO.chat.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 教师课程群公告消息请求体。
 * <p>
 * 公告会以系统消息形式写入 MongoDB，并在群聊历史中与普通消息一起展示。
 * </p>
 */
@Getter
@Setter
public class GroupAnnouncementRequest {

    /**
     * 公告标题。
     */
    @Size(max = 100, message = "公告标题长度不能超过100个字符")
    private String title;

    /**
     * 公告正文。
     */
    @NotBlank(message = "公告内容不能为空")
    @Size(max = 2000, message = "公告内容长度不能超过2000个字符")
    private String content;
}