package com.daox.online.entity.mongodb;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 讨论区集合 (Collection: discussions)
 * 承载课程的互动讨论功能，通过将回复内嵌到主贴中，形成树状的文档结构
 */
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "discussions")
@CompoundIndex(name = "course_created_idx", def = "{'course_id': 1, 'created_at': -1}")
@CompoundIndex(name = "course_pinned_idx", def = "{'course_id': 1, 'is_pinned': -1}")
public class Discussion implements Serializable {

    /**
     * 主贴的唯一主键
     */
    @Id
    private String id;

    /**
     * 关联MySQL courses.id，标识所属课程
     */
    @Field("course_id")
    private String courseId;

    /**
     * 发帖人信息，冗余存储id, nickname, avatar_url以减少查询
     */
    @Field("author")
    private Author author;

    /**
     * 帖子的标题
     */
    @Field("title")
    private String title;

    /**
     * 帖子的主楼内容
     */
    @Field("content")
    private String content;

    /**
     * 是否置顶，默认为false
     */
    @Field("is_pinned")
    private Boolean isPinned = false;

    /**
     * 是否为提问帖，默认为false
     */
    @Field("is_question")
    private Boolean isQuestion = false;

    /**
     * 提问帖是否已解决，默认为false
     */
    @Field("is_resolved")
    private Boolean isResolved = false;

    /**
     * 点赞/支持数
     */
    @Field("votes")
    private Integer votes = 0;

    /**
     * 回复列表，每个回复是内嵌的文档，结构与主贴类似，可继续嵌套
     */
    @Field("replies")
    private List<Reply> replies;

    /**
     * 主贴创建时间
     */
    @Field("created_at")
    private Date createdAt;

    /**
     * 作者信息内部类
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Author implements Serializable {
        /**
         * 用户ID
         */
        @Field("id")
        private String id;
        
        /**
         * 用户昵称
         */
        @Field("nickname")
        private String nickname;
        
        /**
         * 用户头像URL
         */
        @Field("avatar_url")
        private String avatarUrl;
    }

    /**
     * 回复内部类
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Reply implements Serializable {
        /**
         * 回复的唯一ID
         */
        @Field("_id")
        private String id;
        
        /**
         * 回复作者信息
         */
        @Field("author")
        private Author author;
        
        /**
         * 回复内容
         */
        @Field("content")
        private String content;
        
        /**
         * 回复的点赞数
         */
        @Field("votes")
        private Integer votes = 0;
        
        /**
         * 回复创建时间
         */
        @Field("created_at")
        private Date createdAt;
        
        /**
         * 嵌套回复列表（支持多层嵌套）
         */
        @Field("replies")
        private List<Reply> replies;
    }
}