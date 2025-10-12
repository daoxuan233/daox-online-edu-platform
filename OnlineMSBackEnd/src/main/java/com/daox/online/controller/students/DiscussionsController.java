package com.daox.online.controller.students;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.mongodb.Discussion;
import com.daox.online.service.DiscussionsService;
import com.daox.online.uilts.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 学生端 - 讨论区控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/student/discussions")
public class DiscussionsController {

    @Resource
    private DiscussionsService discussionsService;

    /**
     * 获取课程讨论列表
     *
     * @param courseId 课程ID
     * @param request  HTTP请求对象
     * @return 讨论列表
     */
    @GetMapping("/course/{courseId}")
    public RestBean<List<Discussion>> getCourseDiscussions(
            @PathVariable String courseId,
            HttpServletRequest request) {

        // 获取当前用户ID（用于日志记录和权限验证）
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[getCourseDiscussions.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }

        try {
            List<Discussion> discussions = discussionsService.getCourseDiscussions(userId, courseId);
            log.info("[getCourseDiscussions.method]获取课程讨论成功: userId={}, courseId={}, count={}", 
                    userId, courseId, discussions.size());
            return RestBean.success(discussions);
        } catch (Exception e) {
            log.error("[getCourseDiscussions.method]获取课程讨论失败: userId={}, courseId={}", userId, courseId, e);
            return RestBean.failure(500, "获取课程讨论失败");
        }
    }

    /**
     * 获取课程置顶讨论
     *
     * @param courseId 课程ID
     * @param request  HTTP请求对象
     * @return 置顶讨论列表
     */
    @GetMapping("/course/{courseId}/pinned")
    public RestBean<List<Discussion>> getPinnedDiscussions(
            @PathVariable String courseId,
            HttpServletRequest request) {

        // 获取当前用户ID（用于日志记录和权限验证）
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[getPinnedDiscussions.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }

        try {
            List<Discussion> pinnedDiscussions = discussionsService.getPinnedDiscussions(userId, courseId);
            log.info("[getPinnedDiscussions.method]获取置顶讨论成功: userId={}, courseId={}, count={}", 
                    userId, courseId, pinnedDiscussions.size());
            return RestBean.success(pinnedDiscussions);
        } catch (Exception e) {
            log.error("[getPinnedDiscussions.method]获取置顶讨论失败: userId={}, courseId={}", userId, courseId, e);
            return RestBean.failure(500, "获取置顶讨论失败");
        }
    }

    /**
     * 获取课程提问帖
     *
     * @param courseId 课程ID
     * @param request  HTTP请求对象
     * @return 提问帖列表
     */
    @GetMapping("/course/{courseId}/questions")
    public RestBean<List<Discussion>> getQuestionDiscussions(
            @PathVariable String courseId,
            HttpServletRequest request) {

        // 获取当前用户ID（用于日志记录和权限验证）
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[getQuestionDiscussions.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }

        try {
            List<Discussion> questionDiscussions = discussionsService.getQuestionDiscussions(userId, courseId);
            log.info("[getQuestionDiscussions.method]获取提问帖成功: userId={}, courseId={}, count={}", 
                    userId, courseId, questionDiscussions.size());
            return RestBean.success(questionDiscussions);
        } catch (Exception e) {
            log.error("[getQuestionDiscussions.method]获取提问帖失败: userId={}, courseId={}", userId, courseId, e);
            return RestBean.failure(500, "获取提问帖失败");
        }
    }

    /**
     * 获取未解决的提问帖
     *
     * @param courseId 课程ID
     * @param request  HTTP请求对象
     * @return 未解决提问帖列表
     */
    @GetMapping("/course/{courseId}/questions/unresolved")
    public RestBean<List<Discussion>> getUnresolvedQuestions(
            @PathVariable String courseId,
            HttpServletRequest request) {

        // 获取当前用户ID（用于日志记录和权限验证）
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[getUnresolvedQuestions.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }

        try {
            List<Discussion> unresolvedQuestions = discussionsService.getUnresolvedQuestions(userId, courseId);
            log.info("[getUnresolvedQuestions.method]获取未解决提问帖成功: userId={}, courseId={}, count={}", 
                    userId, courseId, unresolvedQuestions.size());
            return RestBean.success(unresolvedQuestions);
        } catch (Exception e) {
            log.error("[getUnresolvedQuestions.method]获取未解决提问帖失败: userId={}, courseId={}", userId, courseId, e);
            return RestBean.failure(500, "获取未解决提问帖失败");
        }
    }

    /**
     * 发布讨论
     *
     * @param courseId   课程ID
     * @param title      讨论标题
     * @param content    讨论内容
     * @param isQuestion 是否为提问帖
     * @param request    HTTP请求对象
     * @return 创建的讨论
     */
    @PostMapping
    public RestBean<Discussion> createDiscussion(
            @RequestParam String courseId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "false") Boolean isQuestion,
            HttpServletRequest request) {

        // 获取当前用户ID
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[createDiscussion.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }

        // 参数校验
        if (!StringUtils.hasText(courseId) || !StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            log.warn("[createDiscussion.method]参数错误: courseId={}, title={}, content={}", courseId, title, content);
            return RestBean.failure(400, "参数不能为空");
        }

        try {
            Discussion discussion = discussionsService.createDiscussion(userId, courseId, title, content, isQuestion);
            if (discussion != null) {
                log.info("[createDiscussion.method]发布讨论成功: userId={}, courseId={}, discussionId={}", 
                        userId, courseId, discussion.getId());
                return RestBean.success(discussion);
            } else {
                return RestBean.failure(500, "发布讨论失败");
            }
        } catch (Exception e) {
            log.error("[createDiscussion.method]发布讨论失败: userId={}, courseId={}", userId, courseId, e);
            return RestBean.failure(500, "发布讨论失败");
        }
    }

    /**
     * 获取讨论详情
     *
     * @param id      讨论ID
     * @param request HTTP请求对象
     * @return 讨论详细信息
     */
    @GetMapping("/{id}")
    public RestBean<Discussion> getDiscussionDetail(
            @PathVariable String id,
            HttpServletRequest request) {

        // 获取当前用户ID
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[getDiscussionDetail.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }

        try {
            Discussion discussion = discussionsService.getDiscussionDetail(userId, id);
            if (discussion != null) {
                log.info("[getDiscussionDetail.method]获取讨论详情成功: userId={}, discussionId={}", userId, id);
                return RestBean.success(discussion);
            } else {
                return RestBean.failure(404, "讨论不存在或无权访问");
            }
        } catch (Exception e) {
            log.error("[getDiscussionDetail.method]获取讨论详情失败: userId={}, discussionId={}", userId, id, e);
            return RestBean.failure(500, "获取讨论详情失败");
        }
    }

    /**
     * 获取讨论回复
     *
     * @param id      讨论ID
     * @param request HTTP请求对象
     * @return 讨论回复列表
     */
    @GetMapping("/{id}/replies")
    public RestBean<Discussion> getDiscussionReplies(
            @PathVariable String id,
            HttpServletRequest request) {

        // 获取当前用户ID
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[getDiscussionReplies.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }

        try {
            Discussion discussion = discussionsService.getDiscussionWithReplies(userId, id);
            if (discussion != null) {
                log.info("[getDiscussionReplies.method]获取讨论回复成功: userId={}, discussionId={}, replyCount={}", 
                        userId, id, discussion.getReplies() != null ? discussion.getReplies().size() : 0);
                return RestBean.success(discussion);
            } else {
                return RestBean.failure(404, "讨论不存在或无权访问");
            }
        } catch (Exception e) {
            log.error("[getDiscussionReplies.method]获取讨论回复失败: userId={}, discussionId={}", userId, id, e);
            return RestBean.failure(500, "获取讨论回复失败");
        }
    }

    /**
     * 发布回复
     *
     * @param id      讨论ID
     * @param content 回复内容
     * @param request HTTP请求对象
     * @return 更新后的讨论
     */
    @PostMapping("/{id}/replies")
    public RestBean<Discussion> createReply(
            @PathVariable String id,
            @RequestParam String content,
            HttpServletRequest request) {

        // 获取当前用户ID
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[createReply.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }

        // 参数校验
        if (!StringUtils.hasText(content)) {
            log.warn("[createReply.method]回复内容不能为空: userId={}, discussionId={}", userId, id);
            return RestBean.failure(400, "回复内容不能为空");
        }

        try {
            Discussion discussion = discussionsService.createReply(userId, id, content);
            if (discussion != null) {
                log.info("[createReply.method]发布回复成功: userId={}, discussionId={}", userId, id);
                return RestBean.success(discussion);
            } else {
                return RestBean.failure(500, "发布回复失败");
            }
        } catch (Exception e) {
            log.error("[createReply.method]发布回复失败: userId={}, discussionId={}", userId, id, e);
            return RestBean.failure(500, "发布回复失败");
        }
    }

    /**
     * 点赞回复
     *
     * @param id      回复ID
     * @param request HTTP请求对象
     * @return 操作结果
     */
    @PostMapping("/replies/{id}/like")
    public RestBean<String> likeReply(
            @PathVariable String id,
            HttpServletRequest request) {

        // 获取当前用户ID
        String userId = UserUtils.getCurrentUserId(request);
        if (userId == null) {
            log.warn("[likeReply.method]用户未认证");
            return RestBean.failure(401, "用户未认证");
        }

        try {
            boolean success = discussionsService.likeReply(userId, id);
            if (success) {
                log.info("[likeReply.method]点赞回复成功: userId={}, replyId={}", userId, id);
                return RestBean.success("点赞成功");
            } else {
                return RestBean.failure(500, "点赞失败");
            }
        } catch (Exception e) {
            log.error("[likeReply.method]点赞回复失败: userId={}, replyId={}", userId, id, e);
            return RestBean.failure(500, "点赞失败");
        }
    }
}
