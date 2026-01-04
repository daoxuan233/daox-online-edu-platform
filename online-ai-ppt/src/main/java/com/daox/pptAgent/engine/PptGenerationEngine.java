package com.daox.pptAgent.engine;

import com.daox.pptAgent.entity.vo.TaskStatus;
import com.daox.pptAgent.repository.ConversationRepository;
import com.daox.pptAgent.service.ImageGenerationService;
import com.daox.pptAgent.service.MinioService;
import com.daox.pptAgent.service.PptToolbox;
import com.daox.pptAgent.service.TaskStatusManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async; // 导入@Async

import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * PPT生成引擎
 * 职责：封装所有使用Apache POI库来实际创建和写入.pptx文件的底层逻辑。
 * 它不关心AI或对话，只负责根据结构化数据生成文件。
 */
@Component
@Slf4j
public class PptGenerationEngine {

    /**
     * 任务状态管理器
     */
    private final TaskStatusManager taskStatusManager;
    /**
     * 会话存储
     */
    private final ConversationRepository conversationRepository;
    /**
     * MinIO服务
     */
    private final MinioService minioService;
    /**
     * 图片生成服务
     */
    private final ImageGenerationService imageGenerationService;

    public PptGenerationEngine(TaskStatusManager taskStatusManager, ConversationRepository conversationRepository, MinioService minioService, ImageGenerationService imageGenerationService) {
        this.taskStatusManager = taskStatusManager;
        this.conversationRepository = conversationRepository;
        this.minioService = minioService;
        this.imageGenerationService = imageGenerationService;
    }

    /**
     * 在后台异步生成PPT的核心方法
     *
     * @param taskId  当前任务的ID
     * @param outline PPT大纲内容
     */
    @Async // <-- 标记为异步方法
    public void generatePptInBackground(String taskId, PptToolbox.PptOutline outline) {
        log.info("开始PPT生成任务 - 任务ID: {}, 大纲标题: {}", taskId, outline.title());
        log.info("大纲包含幻灯片数量: {}", outline.slides() != null ? outline.slides().size() : 0);

        TaskStatus taskStatus = taskStatusManager.getStatus(taskId);
        if (taskStatus == null) {
            log.warn("任务不存在或已过期 - 任务ID: {}", taskId);
            return;
        }

        String conversationId = taskStatus.getConversationId();
        taskStatusManager.updateStatus(taskId, TaskStatus.Status.PROCESSING);
        log.info("任务状态更新为 PROCESSING - 任务ID: {}", taskId);

        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlideMaster master = ppt.getSlideMasters().get(0);

        // 设置幻灯片大小（16:9）
        ppt.setPageSize(new java.awt.Dimension(960, 540));

        // 封面页
        XSLFSlideLayout titleLayout = master.getLayout(SlideLayout.TITLE);
        XSLFSlide titleSlide = ppt.createSlide(titleLayout);
        titleSlide.getPlaceholder(0).setText(outline.title());
        titleSlide.getPlaceholder(1).setText("由 AI 智能生成\n" + java.time.LocalDate.now());
        log.info("已创建封面页");

        // 内容页
        int slideCount = 0;
        if (outline.slides() != null && !outline.slides().isEmpty()) {
            for (PptToolbox.Slide slideData : outline.slides()) {
                log.info("处理幻灯片: {}", slideData.title());

                // 跳过封面页（已单独创建）
                if (slideData.title().equals("封面页") || slideData.title().equals("封面")) {
                    log.info("跳过封面页");
                    continue;
                }

                XSLFSlideLayout contentLayout = master.getLayout(SlideLayout.TITLE_AND_CONTENT);
                XSLFSlide slide = ppt.createSlide(contentLayout);

                // 设置标题
                XSLFTextShape titleShape = slide.getPlaceholder(0);
                if (titleShape != null) {
                    titleShape.setText(slideData.title());
                }

                // 设置内容
                XSLFTextShape body = slide.getPlaceholder(1);
                if (body != null && slideData.bulletPoints() != null) {
                    body.clearText();
                    for (String point : slideData.bulletPoints()) {
                        XSLFTextParagraph p = body.addNewTextParagraph();
                        p.setBullet(true);
                        p.setBulletCharacter("•");
                        XSLFTextRun run = p.addNewTextRun();
                        run.setText(point);
                        run.setFontSize(18.0);
                    }
                }

                slideCount++;
                log.info("已创建幻灯片: {} (第{}页)", slideData.title(), slideCount + 1);

                // 暂时禁用AI生图功能，避免影响PPT生成
                /*
                try {
                    String imagePrompt = slideData.title() + ", " +
                        (slideData.bulletPoints().isEmpty() ? "专业展示" : slideData.bulletPoints().get(0));
                    log.info("正在为幻灯片 '{}' 生成图片", slideData.title());

                    String imageUrl = imageGenerationService.generateImageUrl(imagePrompt);
                    byte[] imageBytes = imageGenerationService.downloadImage(imageUrl);
                    XSLFPictureData picData = ppt.addPicture(imageBytes, PictureData.PictureType.PNG);
                    slide.createPicture(picData).setAnchor(new Rectangle(400, 140, 300, 225));

                    log.info("图片生成成功: {}", slideData.title());
                } catch (Exception e) {
                    log.error("为幻灯片 '{}' 生成图片失败: {}", slideData.title(), e.getMessage());
                }
                */
            }
        } else {
            log.warn("大纲中没有幻灯片数据，仅生成封面页");
        }

        log.info("总共生成 {} 张幻灯片（含封面）", slideCount + 1);

        try {
            log.info("开始将PPT写入字节流并上传到MinIO");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ppt.write(baos);
            ppt.close();

            log.info("PPT文件大小: {} 字节", baos.size());

            String finalUrl = minioService.uploadFile(
                    new ByteArrayInputStream(baos.toByteArray()),
                    baos.size(),
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            );

            log.info("✅ PPT生成成功! 任务ID: {}, 下载链接: {}", taskId, finalUrl);

            taskStatusManager.setSuccess(taskId, finalUrl);
            updateMongoDocument(conversationId, "COMPLETED", finalUrl);

        } catch (Exception e) {
            log.error("❌ PPT生成失败! 任务ID: {}, 错误: {}", taskId, e.getMessage(), e);

            String errorMessage = "生成PPT时发生错误: " + e.getMessage();
            taskStatusManager.setFailure(taskId, errorMessage);
            updateMongoDocument(conversationId, "FAILED", null);
        }
    }

    private void updateMongoDocument(String conversationId, String status, String finalUrl) {
        if (conversationId == null) return;
        conversationRepository.findById(conversationId).ifPresent(doc -> {
            doc.setStatus(status);
            doc.setFinalPptPath(finalUrl);
            doc.setEndTime(LocalDateTime.now());
            conversationRepository.save(doc);
        });
    }

    /**
     * 创建一个PPT演示文稿
     *
     * @param title 演示文稿的标题
     * @return 生成的PPT文件的临时路径
     */
    public String createPresentation(String title) {
        return createPresentation(title, null);
    }

    /**
     * 创建一个PPT演示文稿（重载方法，支持大纲）
     *
     * @param title   演示文稿的标题
     * @param outline PPT大纲（可选）
     * @return 生成的PPT文件的临时路径
     */
    public String createPresentation(String title, PptToolbox.PptOutline outline) {
        log.info("开始创建PPT演示文稿 - 标题: {}", title);

        XMLSlideShow ppt = null;
        FileOutputStream fos = null;

        try {
            // 1. 创建XMLSlideShow对象 (一个PPT文档)
            ppt = new XMLSlideShow();

            // 设置幻灯片大小（16:9）
            java.awt.Dimension slideSize = new java.awt.Dimension(960, 540);
            ppt.setPageSize(slideSize);

            // 获取母版
            XSLFSlideMaster master = ppt.getSlideMasters().get(0);

            // 2. 创建封面页
            XSLFSlide titleSlide = createTitleSlide(ppt, master, title, outline);

            // 3. 如果有大纲，根据大纲创建内容页
            if (outline != null && outline.slides() != null) {
                for (PptToolbox.Slide slideData : outline.slides()) {
                    // 跳过封面页（避免重复）
                    if (slideData.title().equals("封面页") || slideData.title().equals("封面")) {
                        continue;
                    }

                    // 根据幻灯片类型创建不同的页面
                    if (slideData.title().equals("目录") || slideData.title().contains("目录")) {
                        createTableOfContentsSlide(ppt, master, slideData);
                    } else if (slideData.title().equals("谢谢") || slideData.title().contains("感谢")) {
                        createThankYouSlide(ppt, master, slideData);
                    } else {
                        createContentSlide(ppt, master, slideData);
                    }
                }
            } else {
                // 如果没有大纲，创建默认的示例幻灯片
                createSampleSlides(ppt, master, title);
            }

            // 4. 创建临时文件并写入PPT
            Path tempDir = Files.createTempDirectory("ppt_generations");
            String sanitizedTitle = title.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]+", "_");
            File tempFile = new File(tempDir.toFile(), sanitizedTitle + "_" + System.currentTimeMillis() + ".pptx");

            fos = new FileOutputStream(tempFile);
            ppt.write(fos);

            log.info("✅ PPT文件创建成功: {}", tempFile.getAbsolutePath());
            log.info("文件大小: {} KB", tempFile.length() / 1024);

            return tempFile.getAbsolutePath();

        } catch (IOException e) {
            log.error("创建PPT文件时发生错误", e);
            return null;
        } finally {
            // 关闭资源
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error("关闭文件输出流时发生错误", e);
                }
            }
            if (ppt != null) {
                try {
                    ppt.close();
                } catch (IOException e) {
                    log.error("关闭PPT对象时发生错误", e);
                }
            }
        }
    }

    /**
     * 创建标题幻灯片
     */
    private XSLFSlide createTitleSlide(XMLSlideShow ppt, XSLFSlideMaster master,
                                       String title, PptToolbox.PptOutline outline) {
        log.debug("创建封面页");

        XSLFSlideLayout titleLayout = master.getLayout(SlideLayout.TITLE);
        XSLFSlide slide = ppt.createSlide(titleLayout);

        // 设置主标题
        XSLFTextShape titleShape = slide.getPlaceholder(0);
        if (titleShape != null) {
            titleShape.setText(outline != null && outline.title() != null ? outline.title() : title);
            // 设置标题样式
            for (XSLFTextParagraph paragraph : titleShape.getTextParagraphs()) {
                for (XSLFTextRun run : paragraph.getTextRuns()) {
                    run.setFontSize(44.0);
                    run.setBold(true);
                    run.setFontColor(java.awt.Color.DARK_GRAY);
                }
            }
        }

        // 设置副标题
        XSLFTextShape subtitleShape = slide.getPlaceholder(1);
        if (subtitleShape != null) {
            subtitleShape.setText("由 AI 智能生成\n" + java.time.LocalDate.now());
            for (XSLFTextParagraph paragraph : subtitleShape.getTextParagraphs()) {
                for (XSLFTextRun run : paragraph.getTextRuns()) {
                    run.setFontSize(24.0);
                    run.setFontColor(java.awt.Color.GRAY);
                }
            }
        }

        return slide;
    }

    /**
     * 创建目录幻灯片
     */
    private XSLFSlide createTableOfContentsSlide(XMLSlideShow ppt, XSLFSlideMaster master,
                                                 PptToolbox.Slide slideData) {
        log.debug("创建目录页: {}", slideData.title());

        XSLFSlideLayout layout = master.getLayout(SlideLayout.TITLE_AND_CONTENT);
        XSLFSlide slide = ppt.createSlide(layout);

        // 设置标题
        XSLFTextShape titleShape = slide.getPlaceholder(0);
        if (titleShape != null) {
            titleShape.setText(slideData.title());
            formatTitleText(titleShape);
        }

        // 设置内容（目录项）
        XSLFTextShape bodyShape = slide.getPlaceholder(1);
        if (bodyShape != null) {
            bodyShape.clearText();
            int index = 1;
            for (String point : slideData.bulletPoints()) {
                XSLFTextParagraph paragraph = bodyShape.addNewTextParagraph();
                paragraph.setIndentLevel(0);
                paragraph.setBullet(false); // 目录不使用项目符号

                XSLFTextRun run = paragraph.addNewTextRun();
                run.setText(index + ". " + point);
                run.setFontSize(20.0);
                run.setFontColor(java.awt.Color.DARK_GRAY);

                index++;
            }
        }

        return slide;
    }

    /**
     * 创建内容幻灯片
     */
    private XSLFSlide createContentSlide(XMLSlideShow ppt, XSLFSlideMaster master,
                                         PptToolbox.Slide slideData) {
        log.debug("创建内容页: {}", slideData.title());

        XSLFSlideLayout layout = master.getLayout(SlideLayout.TITLE_AND_CONTENT);
        XSLFSlide slide = ppt.createSlide(layout);

        // 设置标题
        XSLFTextShape titleShape = slide.getPlaceholder(0);
        if (titleShape != null) {
            titleShape.setText(slideData.title());
            formatTitleText(titleShape);
        }

        // 设置内容
        XSLFTextShape bodyShape = slide.getPlaceholder(1);
        if (bodyShape != null) {
            bodyShape.clearText();
            for (String point : slideData.bulletPoints()) {
                XSLFTextParagraph paragraph = bodyShape.addNewTextParagraph();
                paragraph.setIndentLevel(0);
                paragraph.setBullet(true);
                paragraph.setBulletCharacter("•");

                XSLFTextRun run = paragraph.addNewTextRun();
                run.setText(point);
                run.setFontSize(18.0);
                run.setFontColor(java.awt.Color.DARK_GRAY);
            }
        }

        // 可选：添加页脚或页码
        addFooter(slide, ppt.getSlides().size());

        return slide;
    }

    /**
     * 创建感谢幻灯片
     */
    private XSLFSlide createThankYouSlide(XMLSlideShow ppt, XSLFSlideMaster master,
                                          PptToolbox.Slide slideData) {
        log.debug("创建感谢页");

        XSLFSlideLayout layout = master.getLayout(SlideLayout.TITLE_ONLY);
        XSLFSlide slide = ppt.createSlide(layout);

        // 设置标题
        XSLFTextShape titleShape = slide.getPlaceholder(0);
        if (titleShape != null) {
            titleShape.setText(slideData.title());
            // 设置感谢页标题样式
            for (XSLFTextParagraph paragraph : titleShape.getTextParagraphs()) {
                paragraph.setTextAlign(XSLFTextParagraph.TextAlign.CENTER);
                for (XSLFTextRun run : paragraph.getTextRuns()) {
                    run.setFontSize(48.0);
                    run.setBold(true);
                    run.setFontColor(new java.awt.Color(0, 102, 204)); // 蓝色
                }
            }
        }

        // 添加子内容（如有）
        if (slideData.bulletPoints() != null && !slideData.bulletPoints().isEmpty()) {
            XSLFTextBox textBox = slide.createTextBox();
            textBox.setAnchor(new Rectangle(100, 300, 760, 200));

            for (String point : slideData.bulletPoints()) {
                XSLFTextParagraph paragraph = textBox.addNewTextParagraph();
                paragraph.setTextAlign(XSLFTextParagraph.TextAlign.CENTER);

                XSLFTextRun run = paragraph.addNewTextRun();
                run.setText(point);
                run.setFontSize(24.0);
                run.setFontColor(java.awt.Color.GRAY);
            }
        }

        return slide;
    }

    /**
     * 创建示例幻灯片（当没有大纲时）
     */
    private void createSampleSlides(XMLSlideShow ppt, XSLFSlideMaster master, String title) {
        log.debug("创建示例幻灯片");

        // 创建介绍页
        XSLFSlideLayout layout = master.getLayout(SlideLayout.TITLE_AND_CONTENT);
        XSLFSlide slide1 = ppt.createSlide(layout);
        slide1.getPlaceholder(0).setText("介绍");
        XSLFTextShape body1 = slide1.getPlaceholder(1);
        body1.clearText();
        body1.addNewTextParagraph().addNewTextRun().setText("• 这是一个由AI生成的PPT演示文稿");
        body1.addNewTextParagraph().addNewTextRun().setText("• 主题: " + title);
        body1.addNewTextParagraph().addNewTextRun().setText("• 生成时间: " + java.time.LocalDateTime.now());

        // 创建内容页
        XSLFSlide slide2 = ppt.createSlide(layout);
        slide2.getPlaceholder(0).setText("主要内容");
        XSLFTextShape body2 = slide2.getPlaceholder(1);
        body2.clearText();
        body2.addNewTextParagraph().addNewTextRun().setText("• 核心概念");
        body2.addNewTextParagraph().addNewTextRun().setText("• 技术细节");
        body2.addNewTextParagraph().addNewTextRun().setText("• 实施方案");
        body2.addNewTextParagraph().addNewTextRun().setText("• 预期成果");

        // 创建总结页
        XSLFSlide slide3 = ppt.createSlide(layout);
        slide3.getPlaceholder(0).setText("总结");
        XSLFTextShape body3 = slide3.getPlaceholder(1);
        body3.clearText();
        body3.addNewTextParagraph().addNewTextRun().setText("• 主要观点回顾");
        body3.addNewTextParagraph().addNewTextRun().setText("• 关键要素总结");
        body3.addNewTextParagraph().addNewTextRun().setText("• 下一步计划");

        // 创建感谢页
        XSLFSlideLayout thanksLayout = master.getLayout(SlideLayout.TITLE_ONLY);
        XSLFSlide slide4 = ppt.createSlide(thanksLayout);
        slide4.getPlaceholder(0).setText("谢谢！");
    }

    /**
     * 格式化标题文本
     */
    private void formatTitleText(XSLFTextShape titleShape) {
        for (XSLFTextParagraph paragraph : titleShape.getTextParagraphs()) {
            for (XSLFTextRun run : paragraph.getTextRuns()) {
                run.setFontSize(32.0);
                run.setBold(true);
                run.setFontColor(new java.awt.Color(51, 51, 51));
            }
        }
    }

    /**
     * 添加页脚信息
     */
    private void addFooter(XSLFSlide slide, int pageNumber) {
        try {
            XSLFTextBox footer = slide.createTextBox();
            footer.setAnchor(new Rectangle(10, 500, 940, 30));

            XSLFTextParagraph paragraph = footer.addNewTextParagraph();
            paragraph.setTextAlign(XSLFTextParagraph.TextAlign.RIGHT);

            XSLFTextRun run = paragraph.addNewTextRun();
            run.setText("第 " + pageNumber + " 页");
            run.setFontSize(10.0);
            run.setFontColor(java.awt.Color.LIGHT_GRAY);
        } catch (Exception e) {
            log.debug("添加页脚时发生错误: {}", e.getMessage());
        }
    }
}