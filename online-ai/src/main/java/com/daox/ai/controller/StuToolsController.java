package com.daox.ai.controller;

import com.daox.ai.entity.RestBean;
import com.daox.ai.utils.AdvancedTagTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/students/ai/tool")
public class StuToolsController {

    private final AdvancedTagTool advancedTagTool = new AdvancedTagTool();

    /**
     * 解析笔记标签
     *
     * @param content 笔记内容
     * @return 提取到的标签列表
     */
    @PostMapping("/parse-tag")
    public RestBean<List<String>> parseTag(String content) {
        // 1. 校验参数
        if (content == null || content.isEmpty()) {
            return RestBean.failure(400, "笔记内容不能为空");
        }
        // 2. 调用工具类提取标签
        List<String> tags = advancedTagTool.extractSmartTags(content, 5);
        return RestBean.success(tags);
    }
}
