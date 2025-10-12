package com.daox.online.mapper;

import com.daox.online.entity.views.responseVO.chat.ChatUserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRelationshipsMapper {
    /**
     * 检查普通好友关系
     * @param userOneId 用户1ID
     * @param userTwoId 用户2ID
     * @return 用户关系ID
     */
    @Select("SELECT id FROM user_relationships WHERE user_one_id = #{userOneId} AND user_two_id = #{userTwoId} AND status = 1")
    String checkNormalFriendship(String userOneId, String userTwoId);

    /**
     * 获取用户好友列表
     * @param userId 用户ID
     * @return 用户好友列表
     */
    List<ChatUserVo> getFriendList(@Param("userId") String userId);

    /**
     * 根据好友ID列表和当前用户ID，批量查询好友的详细信息，包括正确的备注。
     *
     * @param friendIds     好友ID列表
     * @param currentUserId 当前用户ID
     * @return 好友信息列表，封装为 ChatUserVo
     */
    List<ChatUserVo> findFriendInfoByIds(@Param("friendIds") List<String> friendIds, @Param("currentUserId") String currentUserId);
}