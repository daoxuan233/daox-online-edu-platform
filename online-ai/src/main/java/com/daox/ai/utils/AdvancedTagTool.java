package com.daox.ai.utils;

import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.summary.TextRankKeyword;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdvancedTagTool {

    // 1. 定义一份针对 AI 对话的“黑名单”（停用词）
    // 实际项目中建议放入配置文件或数据库
    private static final List<String> STOP_WORDS = Arrays.asList(
            "你好", "请问", "谢谢", "告诉", "什么", "怎么", "基于", "可以", "虽然", "但是",
            "chatgpt", "ai", "助手", "回答", "建议", "如下", "首先", "其次"
    );

    /**
     * 从 AI 对话内容中提取智能标签（关键词）
     * @param content AI 对话内容
     * @param size    提取的标签数量
     * @return        智能提取的标签列表
     */
    public List<String> extractSmartTags(String content, int size) {
        // 2. 动态添加业务相关的“白名单”（自定义词典）
        // 比如你的系统也是 AI 相关的，可能 "Ollama" 分词分不出来，需要手动加
        CustomDictionary.add("Ollama");
        CustomDictionary.add("Spring Boot"); // 强制作为一个词
        CustomDictionary.add("微服务");

        // 3. 实例化 TextRank 对象
        TextRankKeyword textRank = new TextRankKeyword();

        // 4. 获取关键词
        List<String> rawTags = textRank.getKeyword(content);

        // 5. 过滤停用词 & 截取前 N 个
        return rawTags.stream()
                .filter(tag -> !STOP_WORDS.contains(tag)) // 剔除废话
                .filter(tag -> tag.length() > 1)          // 剔除单字，如"是"、"的"
                .limit(size)
                .collect(Collectors.toList());
    }
}