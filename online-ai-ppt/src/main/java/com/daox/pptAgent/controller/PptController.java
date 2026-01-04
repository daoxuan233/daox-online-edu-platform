package com.daox.pptAgent.controller;

import com.daox.pptAgent.entity.RestBean;
import com.daox.pptAgent.entity.vo.ChatRequest;
import com.daox.pptAgent.entity.vo.ChatResponse;
import com.daox.pptAgent.entity.vo.GenerationTask;
import com.daox.pptAgent.entity.vo.TaskStatus;
import com.daox.pptAgent.service.PptAgentService;
import com.daox.pptAgent.service.PptToolbox;
import com.daox.pptAgent.service.TaskStatusManager;
import com.daox.pptAgent.utils.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ppt")
@RequiredArgsConstructor
public class PptController {

    private final PptAgentService agentService;
    private final TaskStatusManager taskStatusManager; // 注入任务管理器

    /**
     * 对话接口
     *
     * @param chatRequest 包含用户消息的请求体
     * @param request     HTTP请求对象，用于获取userId
     * @return 返回经过RestBean包装的统一响应结构
     */
    @PostMapping("/chat")
    public RestBean<Object> chat(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        // 如果过滤器未能解析出用户ID，则返回未授权错误
        String currentUserId = UserUtils.getCurrentUserId(request);
        if (currentUserId == null) {
            return RestBean.unauthorized("用户未授权，请检查Token");
        }

        // 调用核心服务处理聊天逻辑
        Object responseMessage = agentService.chat(currentUserId, chatRequest.message());
        log.info("对话结果类型: {}, 内容: {}", responseMessage.getClass().getName(), responseMessage);
        
        // 根据返回类型包装成不同的VO
        if (responseMessage instanceof String reply) {
            log.info("返回字符串响应: {}", reply);
            return RestBean.success(new ChatResponse(reply));
        } else if (responseMessage instanceof GenerationTask task) {
            log.info("返回GenerationTask - 任务ID: {}", task.taskId());
            return RestBean.success(task);
        } else if (responseMessage instanceof Map) {
            // 处理Map响应，直接返回
            log.info("返回Map响应: {}", responseMessage);
            return RestBean.success(responseMessage);
        }
        
        // 默认情况，直接返回
        log.warn("未知响应类型: {}", responseMessage.getClass().getName());
        return RestBean.success(responseMessage);
    }

    /**
     * 新增：任务状态查询接口
     *
     * @param taskId 任务ID
     * @return 任务的当前状态
     */
    @GetMapping("/task/{taskId}")
    public RestBean<TaskStatus> getTaskStatus(@PathVariable String taskId) {
        log.info("查询任务状态 - 任务ID: {}", taskId);
        TaskStatus status = taskStatusManager.getStatus(taskId);
        if (status == null) {
            log.warn("任务不存在或已过期 - 任务ID: {}", taskId);
            return RestBean.failure(404, "任务不存在或已过期");
        }
        log.info("任务状态: {}", status.getStatus());
        return RestBean.success(status);
    }

    /**
     * 测试接口：获取系统状态
     *
     * @return 系统状态信息
     */
    @GetMapping("/health")
    public RestBean<Map<String, Object>> health() {
        log.info("系统健康检查");
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "PPT Agent Service");
        status.put("version", "1.0.0");
        status.put("springAI", "1.0.0");
        status.put("timestamp", System.currentTimeMillis());
        return RestBean.success(status);
    }

    /**
     * 测试接口：直接生成测试大纲
     *
     * @return 测试大纲
     */
    @GetMapping("/test/outline")
    public RestBean<PptToolbox.PptOutline> testOutline() {
        log.info("生成测试PPT大纲");

        // 创建测试大纲
        List<PptToolbox.Slide> slides = List.of(
                new PptToolbox.Slide("封面页", List.of(
                        "Spring AI 1.0.0 集成测试",
                        "为技术团队准备",
                        "日期: " + java.time.LocalDate.now()
                )),
                new PptToolbox.Slide("目录", List.of(
                        "系统架构",
                        "功能特性",
                        "API 集成",
                        "测试结果"
                )),
                new PptToolbox.Slide("核心功能", List.of(
                        "AI 对话能力",
                        "PPT 自动生成",
                        "图像生成集成",
                        "异步任务处理"
                )),
                new PptToolbox.Slide("谢谢", List.of(
                        "感谢聆听",
                        "Q&A"
                ))
        );

        PptToolbox.PptOutline outline = new PptToolbox.PptOutline(
                "Spring AI 1.0.0 PPT生成系统测试",
                slides
        );

        log.info("测试大纲生成成功，幻灯片数: {}", slides.size());
        return RestBean.success(outline);
    }

    /**
     * 测试接口：获取所有活动任务
     *
     * @return 活动任务列表
     */
    @GetMapping("/tasks/active")
    public RestBean<Map<String, Object>> getActiveTasks() {
        log.info("查询所有活动任务");

        Map<String, Object> result = new HashMap<>();
        result.put("message", "活动任务列表");
        result.put("count", 0); // 可以根据实际情况获取
        result.put("timestamp", System.currentTimeMillis());

        return RestBean.success(result);
    }

}
