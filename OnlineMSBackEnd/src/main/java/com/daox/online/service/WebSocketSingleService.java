package com.daox.online.service;

import com.daox.online.entity.mongodb.ChatMessage;
import com.daox.online.entity.views.responseVO.chat.ChatUserVo;
import com.daox.online.entity.views.responseVO.chat.ConversationVO;

import java.util.List;

public interface WebSocketSingleService {

    /**
     * 添加单聊信息
     *
     * @param message 消息
     * @return 添加后的消息
     */
    ChatMessage addSingleMessage(ChatMessage message);

    /**
     * 查看好友列表
     * @param userId 用户id
     * @return 好友列表
     */
    List<ChatUserVo> getFriendList(String userId);

    /**
     * 获取用户的聊天会话列表
     * @param currentUserId 当前登录用户id
     * @return 组装好地会话列表
     */
    List<ConversationVO> getConversationList(String currentUserId);

    /**
     * 获取和指定对象的聊天记录
     * @param currentUserId 当前登录用户id
     * @param friendId 好友id
     * @return 聊天记录列表
     */
    List<ChatMessage> getChatHistory(String currentUserId, String friendId);

    /**
     * 将指定好友发送给当前用户的消息标记为已读
     *
     * @param currentUserId 当前用户ID
     * @param friendId      好友ID
     */
    void markMessagesAsRead(String currentUserId, String friendId);

    /**
     * 获取指定用户的未读消息总数
     *
     * @param userId 用户ID
     * @return 未读消息总数
     */
    long getTotalUnreadCount(String userId);
}
