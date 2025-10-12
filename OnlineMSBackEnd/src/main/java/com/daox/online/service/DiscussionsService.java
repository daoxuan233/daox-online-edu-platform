package com.daox.online.service;

import com.daox.online.entity.mongodb.Discussion;

import java.util.List;

/**
 * 讨论区服务接口
 * 提供课程讨论相关的业务操作
 */
public interface DiscussionsService {
    
    /**
     * 获取课程讨论列表
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 讨论列表（按创建时间倒序）
     */
    List<Discussion> getCourseDiscussions(String userId, String courseId);
    
    /**
     * 获取课程置顶讨论
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 置顶讨论列表
     */
    List<Discussion> getPinnedDiscussions(String userId, String courseId);
    
    /**
     * 获取课程提问帖
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 提问帖列表
     */
    List<Discussion> getQuestionDiscussions(String userId, String courseId);
    
    /**
     * 获取未解决的提问帖
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 未解决提问帖列表
     */
    List<Discussion> getUnresolvedQuestions(String userId, String courseId);
    
    /**
     * 发布讨论
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param title     讨论标题
     * @param content   讨论内容
     * @param isQuestion 是否为提问帖
     * @return 创建的讨论
     */
    Discussion createDiscussion(String userId, String courseId, String title, String content, Boolean isQuestion);
    
    /**
     * 获取讨论详情
     *
     * @param userId      用户ID
     * @param discussionId 讨论ID
     * @return 讨论详细信息
     */
    Discussion getDiscussionDetail(String userId, String discussionId);
    
    /**
     * 获取讨论回复
     *
     * @param userId      用户ID
     * @param discussionId 讨论ID
     * @return 讨论回复列表
     */
    Discussion getDiscussionWithReplies(String userId, String discussionId);
    
    /**
     * 发布回复
     *
     * @param userId      用户ID
     * @param discussionId 讨论ID
     * @param content     回复内容
     * @return 更新后的讨论（包含新回复）
     */
    Discussion createReply(String userId, String discussionId, String content);
    
    /**
     * 点赞回复
     *
     * @param userId  用户ID
     * @param replyId 回复ID
     * @return 操作结果（true表示成功）
     */
    boolean likeReply(String userId, String replyId);
}
