package com.daox.pptAgent.controller;

import com.daox.pptAgent.entity.RestBean;
import com.daox.pptAgent.entity.vo.ChatRequest;
import com.daox.pptAgent.entity.vo.ChatResponse;
import com.daox.pptAgent.service.PptAgentService;
import com.daox.pptAgent.utils.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ppt")
public class PptController {

    private final PptAgentService agentService;

    // 假设您有一个常量类来存储 "userId" 这个键
    public static final String ATTR_USER_ID = "userId";

    public PptController(PptAgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * 对话接口
     * @param chatRequest 包含用户消息的请求体
     * @param request HTTP请求对象，用于获取userId
     * @return 返回经过RestBean包装的统一响应结构
     */
    @PostMapping("/chat")
    public RestBean<ChatResponse> chat(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        // 从请求属性中获取由JwtRequestFilter设置的用户ID
        String userId = (String) request.getAttribute(ATTR_USER_ID);

        // 如果过滤器未能解析出用户ID，则返回未授权错误
        String currentUserId = UserUtils.getCurrentUserId(request);
        if (currentUserId == null) {
            return RestBean.unauthorized("用户未授权，请检查Token");
        }

        // 调用核心服务处理聊天逻辑
        String responseMessage = agentService.chat(userId, chatRequest.message());

        // 将String类型的响应包装在ChatResponse中，然后用RestBean.success()返回
        return RestBean.success(new ChatResponse(responseMessage));

        // 如果未来Service层可能抛出异常，我们可以使用try-catch来返回failure()
        /*
        try {
            String responseMessage = agentService.chat(userId, chatRequest.message());
            return RestBean.success(new ChatResponse(responseMessage));
        } catch (SomeBusinessException e) {
            return RestBean.failure(400, "业务处理失败：" + e.getMessage());
        }
        */
    }

}
