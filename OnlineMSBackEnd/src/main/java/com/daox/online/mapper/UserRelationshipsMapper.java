package com.daox.online.mapper;

import com.daox.online.entity.mysql.UserRelationships;
import com.daox.online.entity.views.responseVO.chat.ChatUserVo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

@Mapper
public interface UserRelationshipsMapper {
    /**
     * 检查普通好友关系
     * <p>
     * 注意：该方法仅判断 “正常好友关系”（status = 1 / ACCEPTED）。
     * 如果存在 PENDING / DECLINED / BLOCKED 等状态，需要通过 {@link #findRelationship(String, String)} 进一步获取详情。
     * <p>
     * 强约束：本项目约定 user_one_id / user_two_id 必须按字典序有序存储，
     * 因此调用方必须在入参阶段先通过 {@code UserIdUtil.orderUserIds(...)} 排序后再传入。
     *
     * @param userOneId 用户1ID
     * @param userTwoId 用户2ID
     * @return 用户关系ID
     */
    @Select("SELECT id FROM user_relationships WHERE user_one_id = #{userOneId} AND user_two_id = #{userTwoId} AND status = 1")
    String checkNormalFriendship(@Param("userOneId") String userOneId, @Param("userTwoId") String userTwoId);

    /**
     * 查询双方之间的关系记录（不限制 status）
     * <p>
     * 使用场景：
     * - 需要判断是否存在历史关系（例如：曾拒绝、曾拉黑、待处理等）；
     * - 需要将非 ACCEPTED 的关系升级为 ACCEPTED（或未来扩展为 PENDING）。
     * <p>
     * 重要：调用方必须保证 userOneId / userTwoId 已按字典序排序（有序存储）。
     *
     * @param userOneId 字典序较小的用户ID
     * @param userTwoId 字典序较大的用户ID
     * @return 关系实体，不存在则返回 null
     */
    @Select("SELECT id, user_one_id, user_two_id, status, action_user_id, remark_one, remark_two, created_at, updated_at " +
            "FROM user_relationships " +
            "WHERE user_one_id = #{userOneId} AND user_two_id = #{userTwoId} " +
            "LIMIT 1")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userOneId", column = "user_one_id"),
            @Result(property = "userTwoId", column = "user_two_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "actionUserId", column = "action_user_id"),
            @Result(property = "remarkOne", column = "remark_one"),
            @Result(property = "remarkTwo", column = "remark_two"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    UserRelationships findRelationship(@Param("userOneId") String userOneId, @Param("userTwoId") String userTwoId);

    /**
     * 统计用户待处理好友申请数（status = PENDING）
     *
     * @param userId 用户ID
     * @return 待处理好友申请数
     */
    @Select("SELECT COUNT(*) FROM user_relationships WHERE status = 0 AND (user_one_id = #{userId} OR user_two_id = #{userId}) AND action_user_id != #{userId}")
    int countPendingFriendships(@Param("userId") String userId);

    List<ChatUserVo> getPendingFriendRequestList(@Param("userId") String userId);

    /**
     * 新增一条用户关系记录
     * <p>
     * 重要约束：
     * - relationship.userOneId / relationship.userTwoId 必须是有序的（字典序较小的在前）；
     * - status 的取值需遵循 {@link com.daox.online.uilts.constant.Const} 中的好友关系常量：
     * 0=PENDING，1=ACCEPTED，2=DECLINED，3=BLOCKED。
     *
     * @param relationship 关系实体（包含主键id、双方id、状态、操作人、时间戳等）
     * @return 影响行数
     */
    @Insert("INSERT INTO user_relationships " +
            "(id, user_one_id, user_two_id, status, action_user_id, remark_one, remark_two, created_at, updated_at) " +
            "VALUES " +
            "(#{id}, #{userOneId}, #{userTwoId}, #{status}, #{actionUserId}, #{remarkOne}, #{remarkTwo}, #{createdAt}, #{updatedAt})")
    int insertRelationship(UserRelationships relationship);

    /**
     * 更新关系状态（不改动双方ID与备注字段）
     * <p>
     * 设计目的：在不破坏备注字段（remark_one/remark_two）的前提下，仅修改状态与最后操作人。
     *
     * @param id           关系主键ID
     * @param status       新状态
     * @param actionUserId 最后执行该操作的用户ID
     * @param updatedAt    更新时间
     * @return 影响行数
     */
    @Update("UPDATE user_relationships " +
            "SET status = #{status}, action_user_id = #{actionUserId}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int updateRelationshipStatus(@Param("id") String id,
                                 @Param("status") Integer status,
                                 @Param("actionUserId") String actionUserId,
                                 @Param("updatedAt") Date updatedAt);

    @Update("UPDATE user_relationships " +
            "SET remark_one = #{remarkOne}, action_user_id = #{actionUserId}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int updateRemarkOneById(@Param("id") String id,
                            @Param("remarkOne") String remarkOne,
                            @Param("actionUserId") String actionUserId,
                            @Param("updatedAt") Date updatedAt);

    @Update("UPDATE user_relationships " +
            "SET remark_two = #{remarkTwo}, action_user_id = #{actionUserId}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int updateRemarkTwoById(@Param("id") String id,
                            @Param("remarkTwo") String remarkTwo,
                            @Param("actionUserId") String actionUserId,
                            @Param("updatedAt") Date updatedAt);

    @Update("UPDATE user_relationships " +
            "SET status = #{status}, remark_one = #{remarkOne}, action_user_id = #{actionUserId}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int updateStatusAndRemarkOneById(@Param("id") String id,
                                     @Param("status") Integer status,
                                     @Param("remarkOne") String remarkOne,
                                     @Param("actionUserId") String actionUserId,
                                     @Param("updatedAt") Date updatedAt);

    @Update("UPDATE user_relationships " +
            "SET status = #{status}, remark_two = #{remarkTwo}, action_user_id = #{actionUserId}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int updateStatusAndRemarkTwoById(@Param("id") String id,
                                     @Param("status") Integer status,
                                     @Param("remarkTwo") String remarkTwo,
                                     @Param("actionUserId") String actionUserId,
                                     @Param("updatedAt") Date updatedAt);

    /**
     * 获取用户好友列表
     *
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
