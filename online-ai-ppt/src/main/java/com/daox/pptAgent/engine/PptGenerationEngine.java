package com.daox.pptAgent.engine;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * PPT生成引擎
 * 职责：封装所有使用Apache POI库来实际创建和写入.pptx文件的底层逻辑。
 * 它不关心AI或对话，只负责根据结构化数据生成文件。
 */
@Component
public class PptGenerationEngine {
    /**
     * 创建一个PPT演示文稿
     *
     * @param title 演示文稿的标题
     * @return 生成的PPT文件的临时路径
     */
    public String createPresentation(String title) {
        // --- TODO: 在后续阶段，这里将是完整的Apache POI实现 ---
        // 1. 创建XMLSlideShow对象 (一个PPT文档)
        // 2. 遍历传入的结构化内容（如此处的title）
        // 3. 创建幻灯片 (XSLFSlide)
        // 4. 添加标题和内容
        // 5. 将文件写入临时目录

        // 在第一阶段，我们仅模拟一个文件创建过程，并返回路径
        try {
            Path tempDir = Files.createTempDirectory("ppt_generations");
            File tempFile = new File(tempDir.toFile(), title.replaceAll("\\s+", "_") + ".pptx");
            tempFile.createNewFile(); // 创建一个空文件作为占位符
            System.out.println("模拟生成PPT文件于: " + tempFile.getAbsolutePath());
            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
