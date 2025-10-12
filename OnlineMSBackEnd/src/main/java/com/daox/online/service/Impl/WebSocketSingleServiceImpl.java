package com.daox.online.service.Impl;

import com.daox.online.config.WebSocketSingleServiceConfig;
import com.daox.online.controller.exception.BusinessException;
import com.daox.online.entity.mongodb.ChatMessage;
import com.daox.online.entity.views.responseVO.chat.ChatUserVo;
import com.daox.online.entity.views.responseVO.chat.ConversationVO;
import com.daox.online.entity.views.responseVO.chat.LastMessageInfo;
import com.daox.online.mapper.CoursesMapper;
import com.daox.online.mapper.UserRelationshipsMapper;
import com.daox.online.repository.mongodb.ChatMessageRepository;
import com.daox.online.service.ChatMessageService;
import com.daox.online.service.WebSocketSingleService;
import com.daox.online.uilts.JSONUtil;
import com.daox.online.uilts.UserIdUtil;
import com.daox.online.uilts.constant.Const;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import jakarta.annotation.Resource;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@Slf4j
public class WebSocketSingleServiceImpl implements WebSocketSingleService {

    @Resource
    private ChatMessageRepository chatMessageRepository;

    @Resource
    private UserRelationshipsMapper userRelationshipsMapper;

    @Resource
    private CoursesMapper coursesMapper;

    @Resource
    private ChatMessageService chatMessageService;

    /**
     * 添加单聊信息
     *
     * @param message 消息
     * @return 添加后的消息
     */
    @Override
    public ChatMessage addSingleMessage(ChatMessage message) {
        log.info("[addSingleMessage.method] 接收到的消息对象: senderId={}, receiverId={}, content={}, messageType={}",
                message.getSenderId(), message.getReceiverId(), message.getContent(), message.getMessageType());
        // 获取发送者 & 接收者id
        String senderId = message.getSenderId();
        String receiverIdFromRequest = message.getReceiverId();
        log.info("[addSingleMessage.method] 提取的senderId={}, 原始receiverId字段值={}", senderId, receiverIdFromRequest);

        String receiverId;
        // BUG-FIX: The receiverId from the client might be a conversationId (e.g., "userId1_userId2").
        // We need to extract the actual receiverId from it.
        if (receiverIdFromRequest != null && receiverIdFromRequest.contains("_")) {
            String[] ids = receiverIdFromRequest.split("_");
            if (ids.length == 2) {
                // The other user is the one that is not the sender
                if (ids[0].equals(senderId)) {
                    receiverId = ids[1];
                } else {
                    receiverId = ids[0];
                }
                log.info("[addSingleMessage.method] 从conversationId '{}' 中解析出的真实receiverId='{}'", receiverIdFromRequest, receiverId);
            } else {
                // If the format is unexpected, we treat it as a normal (but likely incorrect) ID.
                receiverId = receiverIdFromRequest;
                log.warn("[addSingleMessage.method] receiverId字段值 '{}' 包含下划线但格式不符合预期, 将其作为普通ID处理", receiverIdFromRequest);
            }
        } else {
            receiverId = receiverIdFromRequest;
        }

        UserIdUtil.OrderedUserIds orderedUserIds = UserIdUtil.orderUserIds(senderId, receiverId);

        boolean isFriend = userRelationshipsMapper.checkNormalFriendship(orderedUserIds.userOneId(), orderedUserIds.userTwoId()) != null;
        String newTag;

        if (isFriend) {
            log.info("[addSingleMessage.method] 用户{}与用户{}存在直接好友关系", senderId, receiverId);
            newTag = Const.FRIEND_TAG_FRIEND;
        } else {
            log.warn("[addSingleMessage.method] 用户{}与用户{}不存在直接好友关系", senderId, receiverId);
            // 检查是否在同一课程
            // 注意：此处假设 checkUserInCourse 返回的课程ID可用于比较，如果返回null或不同课程ID，则认为不在同一课程
            boolean inSameCourse = Objects.equals(coursesMapper.checkUserInCourse(orderedUserIds.userOneId()), coursesMapper.checkUserInCourse(orderedUserIds.userTwoId())) && coursesMapper.checkUserInCourse(orderedUserIds.userOneId()) != null;

            if (inSameCourse) {
                log.info("[addSingleMessage.method] 用户{}与用户{}在同一课程，但不存在直接好友关系", senderId, receiverId);
                newTag = Const.FRIEND_TAG_TEMPORARY;
            } else {
                log.error("[addSingleMessage.method] 用户{}与用户{}既不是直接好友且不在同一课程", senderId, receiverId);
                throw new BusinessException("400", "既不是直接好友且不在同一课程，不能发送消息");
            }
        }

        // 统一设置tag并保存
        if (!Objects.equals(message.getTag(), newTag)) {
            message.setTag(newTag);
        }

        // 处理会话ID conversation_id
        // 私聊： 一个唯一且固定的 conversationId,将两个用户的ID组合起来。为了保证组合的唯一性（无论A找B还是B找A，ID都应该相同）
        String conversationId = orderedUserIds.userOneId() + "_" + orderedUserIds.userTwoId();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);

        log.info("[addSingleMessage.method] 添加单聊信息: {}", message);
        ChatMessage chatMessage = chatMessageRepository.save(message);
        log.info("[addSingleMessage.method] 添加单聊信息成功: {}", chatMessage);
        return chatMessage;
    }

    /**
     * 查看好友列表
     *
     * @param userId 用户id
     * @return 好友列表
     */
    @Override
    public List<ChatUserVo> getFriendList(String userId) {
        List<ChatUserVo> friendList = userRelationshipsMapper.getFriendList(userId);
        log.info("[getFriendList.method] 查看好友列表成功: {}", friendList);
        return friendList;
    }

    /**
     * 获取用户的聊天会话列表
     *
     * @param currentUserId 当前登录用户id
     * @return 组装好的会话列表
     */
    @Override
    public List<ConversationVO> getConversationList(String currentUserId) {
        // 获取每个会话的最后一条消息
        List<LastMessageInfo> lastMessages = chatMessageService.findLastMessagesPerConversation(currentUserId);
        log.info("[getConversationList.method] MongoDB查询结果: {}", lastMessages);
        
        // 详细打印每个LastMessageInfo对象的内容
        for (int i = 0; i < lastMessages.size(); i++) {
            LastMessageInfo msg = lastMessages.get(i);
            log.info("[getConversationList.method] 消息[{}]: id={}, conversationId={}, senderId={}, receiverId={}, content={}, timestamp={}, status={}",
                    i, msg.getId(), msg.getConversationId(), msg.getSenderId(), msg.getReceiverId(), msg.getContent(), msg.getTimestamp(), msg.getStatus());
        }

        if (lastMessages.isEmpty()) {
            log.info("[getConversationList.method] 用户{}没有聊天会话", currentUserId);
            return Collections.emptyList();
        }

        // 2. 提取所有好友的ID
        List<String> friendIds = lastMessages.stream()
                .map(msg -> {
                    log.info("[getConversationList.method] 处理消息: senderId={}, receiverId={}, currentUserId={}", 
                            msg.getSenderId(), msg.getReceiverId(), currentUserId);
                    return currentUserId.equals(msg.getSenderId()) ? msg.getReceiverId() : msg.getSenderId();
                })
                .distinct()
                .toList();
        log.info("[getConversationList.method] 提取的friendIds: {}", friendIds);
        // 3. 批量查询好友详情
        List<ChatUserVo> friendDetails = userRelationshipsMapper.findFriendInfoByIds(friendIds, currentUserId);
        // 为了方便后续查找，将其转换为Map
        Map<String, ChatUserVo> friendInfoMap = friendDetails.stream()
                .collect(Collectors.toMap(ChatUserVo::getFriendId, Function.identity()));

        // 4. 组装最终的VO列表
        return lastMessages.stream().map(msg -> {
            String friendId = currentUserId.equals(msg.getSenderId()) ? msg.getReceiverId() : msg.getSenderId();
            ChatUserVo friendInfo = friendInfoMap.getOrDefault(friendId, new ChatUserVo().setFriendId(friendId).setUserName("未知用户"));

            ConversationVO vo = new ConversationVO();
            vo.setConversationId(msg.getConversationId());
            vo.setLastMessageContent(msg.getContent());
            vo.setTimestamp(msg.getTimestamp());
            vo.setStatus(msg.getStatus());
            vo.setFriendInfo(friendInfo);
            // 计算未读消息数，只要不是READ状态都算未读
            long unreadCount = chatMessageRepository.countByConversationIdAndReceiverIdAndStatusNot(
                    msg.getId(),
                    currentUserId,
                    ChatMessage.MessageStatus.READ
            );
            vo.setUnreadCount((int) unreadCount);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取和指定对象的聊天记录
     *
     * @param currentUserId 当前登录用户id
     * @param friendId      好友id
     * @return 聊天记录列表
     */
    @Override
    public List<ChatMessage> getChatHistory(String currentUserId, String friendId) {
        // 获取聊天记录前，先将该会话中的消息标记为已读
        this.markMessagesAsRead(currentUserId, friendId);

        UserIdUtil.OrderedUserIds orderedUserIds = UserIdUtil.orderUserIds(currentUserId, friendId);
        String conversationId = orderedUserIds.userOneId() + "_" + orderedUserIds.userTwoId();
        return chatMessageRepository.findByConversationIdOrderByTimestampDesc(conversationId);
    }

    /**
     * 将指定好友发送给当前用户的消息标记为已读
     *
     * @param currentUserId 当前用户ID
     * @param friendId      好友ID
     */
    @Override
    public void markMessagesAsRead(String currentUserId, String friendId) {
        UserIdUtil.OrderedUserIds orderedUserIds = UserIdUtil.orderUserIds(currentUserId, friendId);
        String conversationId = orderedUserIds.userOneId() + "_" + orderedUserIds.userTwoId();

        // 1. 更新数据库中的消息状态
        chatMessageRepository.updateStatusToRead(conversationId, currentUserId);
        log.info("[markMessagesAsRead] 用户 {} 在会话 {} 中发送给 {} 的消息已标记为已读", friendId, conversationId, currentUserId);

        // 2. (可选) 通过WebSocket通知好友消息已读
        Session friendSession = WebSocketSingleServiceConfig.numberOfConnectionsMap.get(friendId);
        if (friendSession != null && friendSession.isOpen()) {
            try {
                // 构建一个已读回执消息
                Map<String, String> readReceipt = new HashMap<>();
                readReceipt.put("type", "READ_RECEIPT");
                readReceipt.put("conversationId", conversationId);
                readReceipt.put("readerId", currentUserId);

                friendSession.getBasicRemote().sendText(JSONUtil.toJson(readReceipt));
                log.info("[markMessagesAsRead] 已向用户 {} 发送会话 {} 的已读回执", friendId, conversationId);
            } catch (IOException e) {
                log.error("[markMessagesAsRead] 向用户 {} 发送已读回执时发生错误", friendId, e);
            }
        }
    }

    /**
     * 获取指定用户的未读消息总数
     *
     * @param userId 用户ID
     * @return 未读消息总数
     */
    @Override
    public long getTotalUnreadCount(String userId) {
        log.info("[getTotalUnreadCount] 开始查询用户 {} 的未读消息总数", userId);
        long count = chatMessageRepository.countByReceiverIdAndStatusNot(userId, ChatMessage.MessageStatus.READ);
        log.info("[getTotalUnreadCount] 用户 {} 的未读消息总数为: {}", userId, count);
        return count;
    }
}