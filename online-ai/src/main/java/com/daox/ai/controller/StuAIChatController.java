package com.daox.ai.controller;

import com.daox.ai.entity.RestBean;
import com.daox.ai.entity.dto.ConversationSummaryDTO;
import com.daox.ai.entity.mongodb.ChatMessage;
import com.daox.ai.entity.request.ModelPlatformOptions;
import com.daox.ai.entity.response.AIResponse;
import com.daox.ai.service.AIChatService;
import com.daox.ai.utils.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/students/ai")
public class StuAIChatController {
    @Resource
    private AIChatService aiChatService;

    /**
     * AI对话的流式API端点。
     *
     * @param question       用户的提问内容
     * @param conversationId (可选) 用于继续之前的对话
     * @return 返回一个 text/event-stream 类型的流式响应<br/><br/>
     * 1. 使用 @GetMapping 并将 produces 设置为 MediaType.TEXT_EVENT_STREAM_VALUE，这是SSE的核心。
     * 2. 方法直接返回 Flux<String>，Spring会自动处理为事件流。
     * 3. 去掉 RestBean 包装，这是导致当前问题的直接原因。
     * 4. 使用 @RequestParam 获取参数，这是GET请求的标准做法。
     */
    @GetMapping(value = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamAiChat(@RequestParam String question,
                                     @RequestParam(required = false) String conversationId,
                                     HttpServletRequest request) {
        log.info("[streamAiChat.method] 接收到的问题: {}", question);
        String userId = UserUtils.getCurrentUserId(request);
        // 2.【关键】执行安全校验
        //    如果userId为空，说明请求没有携带有效Token，或者Token无效
        if (userId == null) {
            // 抛出异常，这将导致前端收到一个 401 Unauthorized 错误
            // 对于响应式端点，必须通过 Flux.error() 或 Mono.error() 来传递错误
            return Flux.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户未登录或Token无效"));
        }
        return aiChatService.streamChatAndSave(userId, question, conversationId)
                .map(AIResponse::getContent);
    }

    /**
     * 获取AI对话的流式响应端点。
     *
     * @param question       用户的提问内容
     * @param conversationId (可选) 用于继续之前的对话
     * @param options        (可选) 模型平台选项
     * @param request        HTTP请求对象，用于获取当前用户ID
     * @return 返回一个 text/event-stream 类型的流式响应，包含AI的回复内容
     */
    @GetMapping(value = "/chat-stream/res", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AIResponse> streamAiChatResponse(@RequestParam String question,
                                                 @RequestParam(required = false) String conversationId,
                                                 @RequestParam(required = false) ModelPlatformOptions options,
                                                 HttpServletRequest request) {
        log.info("[streamAiChatResponse.method] 接收到的问题: {}", question);
        String userId = UserUtils.getCurrentUserId(request);
        // 2.【关键】执行安全校验
        //    如果userId为空，说明请求没有携带有效Token，或者Token无效
        if (userId == null) {
            // 抛出异常，这将导致前端收到一个 401 Unauthorized 错误
            // 对于响应式端点，必须通过 Flux.error() 或 Mono.error() 来传递错误
            return Flux.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户未登录或Token无效"));
        }
        if (options == null) {
            return aiChatService.streamChatAndSave(userId, question, conversationId);
        }
        String platform = options.getPlatform();
        return switch (platform) {
            case "deepseek" -> aiChatService.streamChatAndSaveDeepSeekRes(userId, question, conversationId);

            // TODO: 平台选择后的模型选择
            case "qwen" -> aiChatService.streamChatAndSaveQwenRes(userId, question, conversationId);
            default -> aiChatService.streamChatAndSave(userId, question, conversationId);
        };
    }

    /**
     * 获取指定用户的AI对话历史列表
     *
     * @param request 请求
     * @return 返回一个包含对话摘要列表的Mono，最终会序列化成一个JSON数组
     */
    @GetMapping("/chat/history/summaries")
    public Mono<List<ConversationSummaryDTO>> getHistorySummaries(HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[getHistorySummaries.method]用户未认证");
            return Mono.just(new ArrayList<>());
        }
        return aiChatService.getConversationSummaries(userId);
    }

    /**
     * 分页获取指定会话ID的聊天记录。
     *
     * @param conversationId 会话ID
     * @param page           页码，默认为0
     * @param size           每页数量，默认为20
     * @return 返回一页消息的JSON数组
     */
    @GetMapping("/chat/history/messages")
    public Mono<List<ChatMessage>> getConversationMessages(String conversationId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        return aiChatService.getMessagesForConversation(conversationId, page, size);
    }

    /**
     * 删除一个AI对话及其所有消息。
     *
     * @param conversationId 要删除的会话ID，从URL路径中获取。
     * @param request        HTTP请求对象，用于获取当前用户信息。
     * @return 返回一个Mono，其中包含表示操作结果的RestBean。
     * - 成功: 返回 code 200 的 RestBean。
     * - 未认证: 返回 code 401 的 RestBean。
     * - 无权限或失败: 返回 code 403/500 的 RestBean。
     */
    @GetMapping("/chat/history/{conversationId}")
    public Mono<RestBean<Object>> deleteConversation(@PathVariable String conversationId, HttpServletRequest request) {
        String userId = UserUtils.getCurrentUserId(request);

        // 1. 认证检查
        if (userId == null) {
            log.warn("[deleteConversation.method] 用户未认证，无法删除会话 {}", conversationId);
            // 直接返回一个包含401错误的RestBean
            return Mono.just(RestBean.unauthorized("用户未认证，请先登录"))
                    .map(stringRestBean -> new RestBean<>(stringRestBean.id(), stringRestBean.code(), null, stringRestBean.message()));
        }

        // 2. 调用服务层并处理响应式结果
        return aiChatService.deleteConversation(userId, conversationId)
                // 3. 成功处理: 当 aiChatService 返回的 Mono<Void> 成功完成时...
                .then(Mono.fromCallable(() -> {
                    log.info("[deleteConversation.method] 用户 {} 成功删除会话 {}", userId, conversationId);
                    // ...返回一个成功的 RestBean
                    return RestBean.success();
                }))
                // 4. 错误处理: 捕获并处理特定的业务异常（如权限不足）
                .onErrorResume(IllegalArgumentException.class, e -> {
                    log.warn("[deleteConversation.method] 用户 {} 删除会话 {} 失败: {}", userId, conversationId, e.getMessage());
                    // 返回一个包含403错误的RestBean
                    return Mono.just(RestBean.forbidden(e.getMessage()))
                            .map(stringRestBean -> new RestBean<>(stringRestBean.id(), stringRestBean.code(), null, stringRestBean.message()));
                })
                // 5. 错误处理: 捕获所有其他未预料到的异常
                .onErrorResume(Exception.class, e -> {
                    log.error("[deleteConversation.method] 删除会话 {} 时发生意外错误", conversationId, e);
                    // 返回一个包含500错误的RestBean
                    return Mono.just(RestBean.failure(500, "删除失败，发生内部错误"))
                            .map(stringRestBean -> new RestBean<>(stringRestBean.id(), stringRestBean.code(), null, stringRestBean.message()));
                });
    }
}
