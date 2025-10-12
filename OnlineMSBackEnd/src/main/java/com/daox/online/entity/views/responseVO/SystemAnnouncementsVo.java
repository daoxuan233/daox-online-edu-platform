package com.daox.online.entity.views.responseVO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 返回给前端的系统公告表
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SystemAnnouncementsVo {
    /**
     * 公告ID
     */
    private String id;
    /**
     * 公告标题
     */
    private String title;
    /**
     * 公告内容
     */
    private String content;
    /**
     * 发布者ID
     */
    private String creatorId;
    /**
     * 发布者昵称
     */
    private String creatorNickname;
    /**
     * 是否生效 0-是，1-否
     */
    private Integer isActive;
    /**
     * 发布时间
     */
    private Date createdAt;
    /**
     * 失效时间
     */
    private Date expiredAt;
}
