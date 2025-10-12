package com.daox.online.service.Impl;

import com.daox.online.entity.mysql.Users;
import com.daox.online.entity.mongodb.Discussion;
import com.daox.online.mapper.CoursesMapper;
import com.daox.online.repository.mongodb.DiscussionRepository;
import com.daox.online.service.SysUserService;
import com.daox.online.service.DiscussionsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 讨论区服务实现类
 * 提供课程讨论相关的业务操作
 */
@Service
@Slf4j
public class DiscussionsServiceImpl implements DiscussionsService {

    @Resource
    private DiscussionRepository discussionRepository;

    @Resource
    private CoursesMapper coursesMapper;

    @Resource
    private SysUserService sysUserService;

    /**
     * 获取课程讨论列表
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 讨论列表（按创建时间倒序）
     */
    @Override
    public List<Discussion> getCourseDiscussions(String userId, String courseId) {
        // 参数校验
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(courseId)) {
            log.warn("[getCourseDiscussions.method]参数错误: userId={}, courseId={}", userId, courseId);
            return Collections.emptyList();
        }

        // 验证用户是否加入课程
        if (!coursesMapper.isUserEnrolledInCourse(userId, courseId)) {
            log.warn("[getCourseDiscussions.method]用户未加入该课程，无权访问讨论: userId={}, courseId={}", userId, courseId);
            return Collections.emptyList();
        }

        try {
            List<Discussion> discussions = discussionRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
            log.info("[getCourseDiscussions.method]获取课程讨论成功: userId={}, courseId={}, count={}", userId, courseId, discussions.size());
            return discussions;
        } catch (Exception e) {
            log.error("[getCourseDiscussions.method]获取课程讨论失败: userId={}, courseId={}", userId, courseId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取课程置顶讨论
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 置顶讨论列表
     */
    @Override
    public List<Discussion> getPinnedDiscussions(String userId, String courseId) {
        // 参数校验
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(courseId)) {
            log.warn("[getPinnedDiscussions.method]参数错误: userId={}, courseId={}", userId, courseId);
            return Collections.emptyList();
        }

        // 验证用户是否加入课程
        if (!coursesMapper.isUserEnrolledInCourse(userId, courseId)) {
            log.warn("[getPinnedDiscussions.method]用户未加入该课程，无权访问讨论: userId={}, courseId={}", userId, courseId);
            return Collections.emptyList();
        }

        try {
            List<Discussion> pinnedDiscussions = discussionRepository.findByCourseIdAndIsPinnedTrueOrderByCreatedAtDesc(courseId);
            log.info("[getPinnedDiscussions.method]获取置顶讨论成功: userId={}, courseId={}, count={}", userId, courseId, pinnedDiscussions.size());
            return pinnedDiscussions;
        } catch (Exception e) {
            log.error("[getPinnedDiscussions.method]获取置顶讨论失败: userId={}, courseId={}", userId, courseId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取课程提问帖
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 提问帖列表
     */
    @Override
    public List<Discussion> getQuestionDiscussions(String userId, String courseId) {
        // 参数校验
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(courseId)) {
            log.warn("[getQuestionDiscussions.method]参数错误: userId={}, courseId={}", userId, courseId);
            return Collections.emptyList();
        }

        // 验证用户是否加入课程
        if (!coursesMapper.isUserEnrolledInCourse(userId, courseId)) {
            log.warn("[getQuestionDiscussions.method]用户未加入该课程，无权访问讨论: userId={}, courseId={}", userId, courseId);
            return Collections.emptyList();
        }

        try {
            List<Discussion> questionDiscussions = discussionRepository.findByCourseIdAndIsQuestionTrueOrderByCreatedAtDesc(courseId);
            log.info("[getQuestionDiscussions.method]获取提问帖成功: userId={}, courseId={}, count={}", userId, courseId, questionDiscussions.size());
            return questionDiscussions;
        } catch (Exception e) {
            log.error("[getQuestionDiscussions.method]获取提问帖失败: userId={}, courseId={}", userId, courseId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取未解决的提问帖
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 未解决提问帖列表
     */
    @Override
    public List<Discussion> getUnresolvedQuestions(String userId, String courseId) {
        // 参数校验
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(courseId)) {
            log.warn("[getUnresolvedQuestions.method]参数错误: userId={}, courseId={}", userId, courseId);
            return Collections.emptyList();
        }

        // 验证用户是否加入课程
        if (!coursesMapper.isUserEnrolledInCourse(userId, courseId)) {
            log.warn("[getUnresolvedQuestions.method]用户未加入该课程，无权访问讨论: userId={}, courseId={}", userId, courseId);
            return Collections.emptyList();
        }

        try {
            List<Discussion> unresolvedQuestions = discussionRepository.findByCourseIdAndIsQuestionTrueAndIsResolvedFalseOrderByCreatedAtDesc(courseId);
            log.info("[getUnresolvedQuestions.method]获取未解决提问帖成功: userId={}, courseId={}, count={}", userId, courseId, unresolvedQuestions.size());
            return unresolvedQuestions;
        } catch (Exception e) {
            log.error("[getUnresolvedQuestions.method]获取未解决提问帖失败: userId={}, courseId={}", userId, courseId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 发布讨论
     *
     * @param userId     用户ID
     * @param courseId   课程ID
     * @param title      讨论标题
     * @param content    讨论内容
     * @param isQuestion 是否为提问帖
     * @return 创建的讨论
     */
    @Override
    public Discussion createDiscussion(String userId, String courseId, String title, String content, Boolean isQuestion) {
        // 参数校验
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(courseId) ||
                !StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            log.warn("[createDiscussion.method]参数错误: userId={}, courseId={}, title={}, content={}",
                    userId, courseId, title, content);
            return null;
        }

        // 验证用户是否加入课程
        if (!coursesMapper.isUserEnrolledInCourse(userId, courseId)) {
            log.warn("[createDiscussion.method]用户未加入该课程，无权发布讨论: userId={}, courseId={}", userId, courseId);
            return null;
        }

        try {
            // 获取用户信息
            Users user = sysUserService.findUserById(userId);
            if (user == null) {
                log.warn("[createDiscussion.method]用户不存在: userId={}", userId);
                return null;
            }

            // 创建作者信息
            Discussion.Author author = new Discussion.Author();
            author.setId(userId);
            author.setNickname(user.getNickname());
            author.setAvatarUrl(user.getAvatarUrl());

            // 创建讨论
            Discussion discussion = new Discussion();
            discussion.setCourseId(courseId);
            discussion.setAuthor(author);
            discussion.setTitle(title);
            discussion.setContent(content);
            discussion.setIsQuestion(isQuestion != null ? isQuestion : false);
            discussion.setIsPinned(false);
            discussion.setIsResolved(false);
            discussion.setVotes(0);
            discussion.setReplies(new ArrayList<>());
            discussion.setCreatedAt(new Date());

            Discussion savedDiscussion = discussionRepository.save(discussion);
            log.info("[createDiscussion.method]发布讨论成功: userId={}, courseId={}, discussionId={}",
                    userId, courseId, savedDiscussion.getId());
            return savedDiscussion;
        } catch (Exception e) {
            log.error("[createDiscussion.method]发布讨论失败: userId={}, courseId={}", userId, courseId, e);
            return null;
        }
    }

    /**
     * 获取讨论详情
     *
     * @param userId       用户ID
     * @param discussionId 讨论ID
     * @return 讨论详细信息
     */
    @Override
    public Discussion getDiscussionDetail(String userId, String discussionId) {
        // 参数校验
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(discussionId)) {
            log.warn("[getDiscussionDetail.method]参数错误: userId={}, discussionId={}", userId, discussionId);
            return null;
        }

        try {
            Optional<Discussion> discussionOpt = discussionRepository.findById(discussionId);
            if (discussionOpt.isEmpty()) {
                log.warn("[getDiscussionDetail.method]讨论不存在: discussionId={}", discussionId);
                return null;
            }

            Discussion discussion = discussionOpt.get();

            // 验证用户是否加入课程
            if (!coursesMapper.isUserEnrolledInCourse(userId, discussion.getCourseId())) {
                log.warn("[getDiscussionDetail.method]用户未加入该课程，无权访问讨论: userId={}, courseId={}",
                        userId, discussion.getCourseId());
                return null;
            }

            log.info("[getDiscussionDetail.method]获取讨论详情成功: userId={}, discussionId={}", userId, discussionId);
            return discussion;
        } catch (Exception e) {
            log.error("[getDiscussionDetail.method]获取讨论详情失败: userId={}, discussionId={}", userId, discussionId, e);
            return null;
        }
    }

    /**
     * 获取讨论回复
     *
     * @param userId       用户ID
     * @param discussionId 讨论ID
     * @return 讨论回复列表
     */
    @Override
    public Discussion getDiscussionWithReplies(String userId, String discussionId) {
        // 复用getDiscussionDetail方法，因为Discussion实体已包含replies字段
        return getDiscussionDetail(userId, discussionId);
    }

    /**
     * 发布回复
     *
     * @param userId       用户ID
     * @param discussionId 讨论ID
     * @param content      回复内容
     * @return 更新后的讨论（包含新回复）
     */
    @Override
    public Discussion createReply(String userId, String discussionId, String content) {
        // 参数校验
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(discussionId) || !StringUtils.hasText(content)) {
            log.warn("[createReply.method]参数错误: userId={}, discussionId={}, content={}",
                    userId, discussionId, content);
            return null;
        }

        try {
            Optional<Discussion> discussionOpt = discussionRepository.findById(discussionId);
            if (discussionOpt.isEmpty()) {
                log.warn("[createReply.method]讨论不存在: discussionId={}", discussionId);
                return null;
            }

            Discussion discussion = discussionOpt.get();

            // 验证用户是否加入课程
            if (!coursesMapper.isUserEnrolledInCourse(userId, discussion.getCourseId())) {
                log.warn("[createReply.method]用户未加入该课程，无权回复讨论: userId={}, courseId={}",
                        userId, discussion.getCourseId());
                return null;
            }

            // 获取用户信息
            Users user = sysUserService.findUserById(userId);
            if (user == null) {
                log.warn("[createReply.method]用户不存在: userId={}", userId);
                return null;
            }

            // 创建作者信息
            Discussion.Author author = new Discussion.Author();
            author.setId(userId);
            author.setNickname(user.getNickname());
            author.setAvatarUrl(user.getAvatarUrl());

            // 创建回复
            Discussion.Reply reply = new Discussion.Reply();
            reply.setId(UUID.randomUUID().toString());
            reply.setAuthor(author);
            reply.setContent(content);
            reply.setVotes(0);
            reply.setCreatedAt(new Date());
            reply.setReplies(new ArrayList<>());

            // 添加回复到讨论
            if (discussion.getReplies() == null) {
                discussion.setReplies(new ArrayList<>());
            }
            discussion.getReplies().add(reply);

            Discussion savedDiscussion = discussionRepository.save(discussion);
            log.info("[createReply.method]发布回复成功: userId={}, discussionId={}, replyId={}",
                    userId, discussionId, reply.getId());
            return savedDiscussion;
        } catch (Exception e) {
            log.error("[createReply.method]发布回复失败: userId={}, discussionId={}", userId, discussionId, e);
            return null;
        }
    }

    /**
     * 点赞回复
     *
     * @param userId  用户ID
     * @param replyId 回复ID
     * @return 操作结果（true表示成功）
     */
    @Override
    public boolean likeReply(String userId, String replyId) {
        // 参数校验
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(replyId)) {
            log.warn("[likeReply.method]参数错误: userId={}, replyId={}", userId, replyId);
            return false;
        }

        try {
            // 查找包含该回复的讨论
            List<Discussion> discussions = discussionRepository.findAll();
            for (Discussion discussion : discussions) {
                if (updateReplyVotes(discussion.getReplies(), replyId)) {
                    // 验证用户是否加入课程
                    if (!coursesMapper.isUserEnrolledInCourse(userId, discussion.getCourseId())) {
                        log.warn("[likeReply.method]用户未加入该课程，无权点赞回复: userId={}, courseId={}",
                                userId, discussion.getCourseId());
                        return false;
                    }

                    discussionRepository.save(discussion);
                    log.info("[likeReply.method]点赞回复成功: userId={}, replyId={}", userId, replyId);
                    return true;
                }
            }

            log.warn("[likeReply.method]回复不存在: replyId={}", replyId);
            return false;
        } catch (Exception e) {
            log.error("[likeReply.method]点赞回复失败: userId={}, replyId={}", userId, replyId, e);
            return false;
        }
    }

    /**
     * 递归更新回复的点赞数
     *
     * @param replies 回复列表
     * @param replyId 回复ID
     * @return 是否找到并更新了回复
     */
    private boolean updateReplyVotes(List<Discussion.Reply> replies, String replyId) {
        if (replies == null || replies.isEmpty()) {
            return false;
        }

        for (Discussion.Reply reply : replies) {
            if (replyId.equals(reply.getId())) {
                reply.setVotes(reply.getVotes() + 1);
                return true;
            }

            // 递归查找嵌套回复
            if (updateReplyVotes(reply.getReplies(), replyId)) {
                return true;
            }
        }

        return false;
    }
}