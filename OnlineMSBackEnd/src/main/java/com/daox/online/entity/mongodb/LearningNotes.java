package com.daox.online.entity.mongodb;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习笔记集合 (Collection: learning_notes)
 * 存储学生在学习过程中记录的个人笔记，支持按课程、章节、小节进行分层管理。<br />
 * 提供富文本内容存储和快速检索功能，是个性化学习的重要组成部分
 */
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "learning_notes")
@CompoundIndex(name = "student_created_idx", def = "{'student_id': 1, 'created_at': -1}")
@CompoundIndex(name = "course_idx", def = "{'course_id': 1}")
@CompoundIndex(name = "tags_idx", def = "{'tags': 1}")
public class LearningNotes {

    /**
     * 文档唯一主键，自动生成
     */
    @Id
    private String id;

    /**
     * 关联MySQL users.id，标识笔记所属
     */
    @Field("student_id")
    @Indexed
    private String studentId;

    /**
     * 关联MySQL courses.id，标识笔记所属课程 (可选)
     */
    @Field("course_id")
    @Indexed
    private String courseId;

    /**
     * 关联MySQL chapters.id，标识笔记所属章节（可选）
     */
    @Field("chapter_id")
    private String chapterId;

    /**
     * 关联MySQL sections.id，标识笔记所属小节（可选）
     */
    @Field("section_id")
    private String sectionId;

    /**
     * 笔记标题
     */
    @Field("title")
    private String title;

    /**
     * 笔记内容，支持富文本格式（HTML或Markdown）
     */
    @Field("content")
    private String content;

    /**
     * 笔记标签数组，用于分类和检索
     */
    @Field("tags")
    @Indexed
    private List<String> tags;

    /**
     * 是否为私人笔记，默认为true
     */
    @Field("is_private")
    private Boolean isPrivate = true;

    /**
     * 关联的视频时间点（秒），用于视频笔记定位（可选）
     */
    @Field("video_time")
    private Integer videoTime;

    /**
     * 笔记创建时间
     */
    @Field("created_at")
    @Indexed
    private LocalDateTime createdAt;

    /**
     * 笔记最后更新时间
     */
    @Field("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志，默认为false
     */
    @Field("is_deleted")
    @Indexed
    private Boolean isDeleted = false;
}
