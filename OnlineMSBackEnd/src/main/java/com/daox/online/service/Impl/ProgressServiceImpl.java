package com.daox.online.service.Impl;

import com.daox.online.entity.mysql.Files;
import com.daox.online.entity.mysql.LearningProgress;
import com.daox.online.entity.mysql.Sections;
import com.daox.online.mapper.FilesMapper;
import com.daox.online.mapper.LearningProgressMapper;
import com.daox.online.mapper.SectionMapper;
import com.daox.online.service.ProgressService;
import com.daox.online.uilts.HybridIdGenerator;
import com.daox.online.uilts.constant.Const;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ProgressServiceImpl implements ProgressService {

    @Resource
    private LearningProgressMapper learningProgressMapper;
    @Resource
    private SectionMapper sectionMapper;
    @Resource
    private HybridIdGenerator hybridIdGenerator;
    @Resource
    private FilesMapper filesMapper;

    /**
     * 在用户首次访问小节内容时，初始化或更新其学习进度。
     *
     * @param userId    当前用户ID。
     * @param sectionId 正在访问的小节ID。
     * @param fileInfo  小节关联的文件信息，用于判断是视频还是文档。
     */
    @Override
    @Transactional
    public void initOrUpdateProgressOnAccess(String userId, String sectionId, Files fileInfo) {
        LearningProgress progress = learningProgressMapper.findByUserAndSection(userId, sectionId);

        // 通过查询小节下的所有文件来判断其性质（是否包含视频）
        List<Files> sectionFiles = filesMapper.findFilesBySectionId(sectionId);
        boolean sectionHasVideo = sectionFiles.stream()
                .anyMatch(f -> f.getMimeType() != null && f.getMimeType().startsWith("video/"));

        if (progress == null) {
            // 场景：首次访问，创建新的学习记录
            progress = new LearningProgress();
            progress.setId(hybridIdGenerator.generateId());
            progress.setUserId(userId);
            progress.setSectionId(sectionId);
            progress.setProgressSeconds(0);

            if (sectionHasVideo) {
                // 如果小节包含视频（无论是否还有文档），则进度由视频驱动
                progress.setStatus("in_progress");
            } else {
                // 如果小节只有文档等非视频资料，则首次访问即完成
                progress.setStatus("completed");
            }
            learningProgressMapper.insert(progress);
            log.info("为用户 {} 在小节 {} 创建了新的学习记录，状态为 {}", userId, sectionId, progress.getStatus());
        } else {
            // 场景：学习记录已存在，处理状态转换
            boolean needsUpdate = false;

            // 如果一个原先只有文档的小节（已完成）后来加入了视频，当用户访问该视频时，应重置状态
            boolean accessedFileIsVideo = fileInfo.getMimeType() != null && fileInfo.getMimeType().startsWith("video/");
            if (sectionHasVideo && accessedFileIsVideo && "completed".equals(progress.getStatus())) {
                progress.setStatus("in_progress");
                progress.setProgressSeconds(0); // 重置观看进度
                needsUpdate = true;
                log.info("小节 {} 已更新，为用户 {} 重置学习状态为 'in_progress'", sectionId, userId);
            }

            // 如果一个纯文档小节由于某种原因未被标记为完成，在此处修正
            if (!sectionHasVideo && !"completed".equals(progress.getStatus())) {
                progress.setStatus("completed");
                needsUpdate = true;
                log.info("用户 {} 访问文档小节 {}，更新状态为 'completed'", userId, sectionId);
            }

            if (needsUpdate) {
                learningProgressMapper.update(progress);
            }
        }
    }

    /**
     * 更新学生观看视频的进度。
     *
     * @param userId          当前用户ID。
     * @param sectionId       正在观看的小节ID。
     * @param progressSeconds 前端上报的最新观看秒数。
     */
    @Override
    @Transactional
    public void updateVideoProgress(String userId, String sectionId, int progressSeconds) {
        LearningProgress progress = learningProgressMapper.findByUserAndSection(userId, sectionId);
        if (progress == null) {
            // 增加鲁棒性：如果因异常流程导致进度记录不存在，则自动创建一条
            log.warn("用户 {} 尝试更新不存在的学习记录 (小节ID: {})，将自动创建记录。", userId, sectionId);
            progress = new LearningProgress();
            progress.setId(hybridIdGenerator.generateId());
            progress.setUserId(userId);
            progress.setSectionId(sectionId);
            progress.setStatus("in_progress");
            progress.setProgressSeconds(0);
            learningProgressMapper.insert(progress);
        }

        // 仅当上报的进度大于当前记录的进度时才更新，防止网络延迟等问题导致的回退
        if (progressSeconds > progress.getProgressSeconds()) {
            progress.setProgressSeconds(progressSeconds);

            // 检查是否已完成
            Sections section = sectionMapper.findById(sectionId);
            if (section != null && section.getDurationSeconds() != null && section.getDurationSeconds() > 0) {
                if (!"completed".equals(progress.getStatus()) && // 避免重复更新
                        progressSeconds >= section.getDurationSeconds() * Const.COMPLETION_THRESHOLD) {
                    progress.setStatus("completed");
                    log.info("用户 {} 在小节 {} 的学习进度已达到完成阈值，状态更新为 completed", userId, sectionId);
                }
            }
            learningProgressMapper.update(progress);
        }
    }
}
