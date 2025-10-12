package com.daox.online.entity.dto;

import com.daox.online.entity.views.responseVO.course.MaterialDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 课程小节的数据传输对象，包含了学生的个性化学习进度。
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SectionDto {
    private String id;              // 小节ID
    private String title;           // 小节标题
    private Integer orderIndex;     // 显示顺序
    private String videoUrl;        // 视频的访问URL
    private Integer durationSeconds; // 视频总时长 (秒)
    private List<MaterialDto> materials; // 挂载在该小节下的资料列表

    // --- 个性化学习进度字段 ---
    /**
     * 当前学生的学习状态 ('not_started', 'in_progress', 'completed')。
     */
    private String status;

    /**
     * 视频已观看的时长（秒），用于实现断点续播。
     */
    private Integer progressSeconds;
}
