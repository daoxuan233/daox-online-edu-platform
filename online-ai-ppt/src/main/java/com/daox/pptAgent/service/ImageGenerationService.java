package com.daox.pptAgent.service;

import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 图片生成服务类
 * <p>
 * 该服务类提供AI图片生成功能，基于Spring AI 1.0.0框架实现
 * 支持通过文本提示词生成图片，并提供图片下载功能
 *
 * @author daox
 * @version 1.0.0
 */
@Service
public class ImageGenerationService {

    /**
     * Spring AI图片生成模型接口
     * 用于与AI图片生成服务进行交互
     */
    private final ImageModel imageModel;

    /**
     * REST模板，用于HTTP请求
     * 主要用于下载生成的图片
     */
    private final RestTemplate restTemplate;

    /**
     * 构造函数
     *
     * @param imageModel Spring AI图片生成模型实例，不能为null
     */
    public ImageGenerationService(ImageModel imageModel) {
        this.imageModel = imageModel;
        this.restTemplate = new RestTemplate();
    }

    /**
     * 生成图片URL
     * <p>
     * 根据提供的文本提示词生成AI图片，并返回图片的URL地址
     * 会自动优化提示词以获得更好的图片质量
     *
     * @param prompt 用户提供的图片描述提示词，不能为null或空字符串
     * @return 生成图片的URL地址
     * @throws IllegalArgumentException 当prompt为null或空时抛出
     * @throws RuntimeException         当图片生成失败时抛出
     */
    public String generateImageUrl(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("图片生成提示词不能为空");
        }

        // 优化提示词以获得更好的图片质量
        String optimizedPrompt = prompt + ", photorealistic, cinematic, high detail, professional photography";

        // 创建图片生成请求
        ImagePrompt imagePrompt = new ImagePrompt(optimizedPrompt);

        System.out.println("请求生成图片，Prompt: " + optimizedPrompt);

        // 调用AI模型生成图片
        ImageResponse response = imageModel.call(imagePrompt);

        // 返回生成图片的URL
        return response.getResult().getOutput().getUrl();
    }

    /**
     * 下载图片
     * <p>
     * 根据提供的图片URL下载图片数据
     *
     * @param imageUrl 图片的URL地址，不能为null或空字符串
     * @return 图片的字节数组数据
     * @throws IllegalArgumentException 当imageUrl为null或空时抛出
     * @throws RuntimeException         当图片下载失败时抛出
     */
    public byte[] downloadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("图片URL不能为空");
        }

        try {
            return restTemplate.getForObject(imageUrl, byte[].class);
        } catch (Exception e) {
            throw new RuntimeException("图片下载失败: " + e.getMessage(), e);
        }
    }
}
