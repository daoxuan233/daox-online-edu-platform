package com.daox.online.service;

import com.daox.online.entity.mysql.Files;

/**
 * 学习进度服务接口。
 * 负责处理所有与学生学习状态相关的业务逻辑。
 */
public interface ProgressService {
    /**
     * 在用户首次访问小节内容时，初始化或更新其学习进度。
     *
     * @param userId    当前用户ID。
     * @param sectionId 正在访问的小节ID。
     * @param fileInfo  小节关联的文件信息，用于判断是视频还是文档。
     */
    void initOrUpdateProgressOnAccess(String userId, String sectionId, Files fileInfo);

    /**
     * 更新学生观看视频的进度。
     *
     * @param userId          当前用户ID。
     * @param sectionId       正在观看的小节ID。
     * @param progressSeconds 前端上报的最新观看秒数。
     */
    void updateVideoProgress(String userId, String sectionId, int progressSeconds);
}
