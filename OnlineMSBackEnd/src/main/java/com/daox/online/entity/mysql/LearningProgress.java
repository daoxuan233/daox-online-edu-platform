package com.daox.online.entity.mysql;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 学习进度表<br />
 * 精确记录每个学生对每个课程小节的学习状态，是实现“断点续学”和学习数据分析的关键<br />
 * TableName:  learning_progress
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class LearningProgress implements Serializable {

    /**
     * 进度ID
     */
    private String id;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 小节ID
     */
    private String sectionId;
    /**
     * 学习状态 <br />
     * 'not_started' : 未开始<br />
     * 'in_progress' : 进行中<br />
     * 'start_expired':已过期<br />
     * 'completed':已完成
     */
    private String status;
    /**
     * 视频观看时长
     */
    private Integer progressSeconds;
    /**
     * 更新时间
     */
    private Date updatedAt;


}
