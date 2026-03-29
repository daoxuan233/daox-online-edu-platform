package com.daox.online.mapper;

import com.daox.online.entity.mysql.UserNotifications;
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
public interface UserNotificationsMapper {

    @Insert({
            "<script>",
            "INSERT INTO user_notifications (",
            "id, user_id, notification_type, source_type, source_id, title, content, level, actor_id, course_id, related_id,",
            "is_read, is_deleted, created_at, read_at, expires_at",
            ") VALUES ",
            "<foreach collection='notifications' item='item' separator=','>",
            "(#{item.id}, #{item.userId}, #{item.notificationType}, #{item.sourceType}, #{item.sourceId}, #{item.title}, #{item.content}, #{item.level}, #{item.actorId}, #{item.courseId}, #{item.relatedId},",
            "#{item.isRead}, #{item.isDeleted}, #{item.createdAt}, #{item.readAt}, #{item.expiresAt})",
            "</foreach>",
            "</script>"
    })
    int batchInsert(@Param("notifications") List<UserNotifications> notifications);

    @Select({
            "<script>",
            "SELECT * FROM user_notifications",
            "WHERE user_id = #{userId}",
            "AND is_deleted = 0",
            "<if test='unreadOnly != null and unreadOnly'>AND is_read = 0</if>",
            "<if test='notificationType != null and notificationType != \"\"'>AND notification_type = #{notificationType}</if>",
            "ORDER BY is_read ASC, created_at DESC",
            "LIMIT #{offset}, #{pageSize}",
            "</script>"
    })
    @Results(id = "userNotificationMap", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "notification_type", property = "notificationType"),
            @Result(column = "source_type", property = "sourceType"),
            @Result(column = "source_id", property = "sourceId"),
            @Result(column = "title", property = "title"),
            @Result(column = "content", property = "content"),
            @Result(column = "level", property = "level"),
            @Result(column = "actor_id", property = "actorId"),
            @Result(column = "course_id", property = "courseId"),
            @Result(column = "related_id", property = "relatedId"),
            @Result(column = "is_read", property = "isRead"),
            @Result(column = "is_deleted", property = "isDeleted"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "read_at", property = "readAt"),
            @Result(column = "expires_at", property = "expiresAt")
    })
    List<UserNotifications> listByUserId(@Param("userId") String userId,
                                         @Param("offset") int offset,
                                         @Param("pageSize") int pageSize,
                                         @Param("unreadOnly") Boolean unreadOnly,
                                         @Param("notificationType") String notificationType);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM user_notifications",
            "WHERE user_id = #{userId}",
            "AND is_deleted = 0",
            "<if test='unreadOnly != null and unreadOnly'>AND is_read = 0</if>",
            "<if test='notificationType != null and notificationType != \"\"'>AND notification_type = #{notificationType}</if>",
            "</script>"
    })
    long countByUserId(@Param("userId") String userId,
                       @Param("unreadOnly") Boolean unreadOnly,
                       @Param("notificationType") String notificationType);

    @Select("SELECT COUNT(*) FROM user_notifications WHERE user_id = #{userId} AND is_deleted = 0 AND is_read = 0")
    long countUnreadByUserId(@Param("userId") String userId);

    @Update("UPDATE user_notifications SET is_read = 1, read_at = #{readAt} WHERE id = #{id} AND user_id = #{userId} AND is_deleted = 0 AND is_read = 0")
    int markAsRead(@Param("userId") String userId, @Param("id") String id, @Param("readAt") Date readAt);

    @Update("UPDATE user_notifications SET is_read = 1, read_at = #{readAt} WHERE user_id = #{userId} AND is_deleted = 0 AND is_read = 0")
    int markAllAsRead(@Param("userId") String userId, @Param("readAt") Date readAt);

    @Update("UPDATE user_notifications SET title = #{title}, content = #{content}, level = #{level}, expires_at = #{expiresAt} WHERE source_type = #{sourceType} AND source_id = #{sourceId} AND is_deleted = 0")
    int updateBySource(@Param("sourceType") String sourceType,
                       @Param("sourceId") String sourceId,
                       @Param("title") String title,
                       @Param("content") String content,
                       @Param("level") String level,
                       @Param("expiresAt") Date expiresAt);

    @Update("UPDATE user_notifications SET is_deleted = 1 WHERE source_type = #{sourceType} AND source_id = #{sourceId} AND is_deleted = 0")
    int markDeletedBySource(@Param("sourceType") String sourceType, @Param("sourceId") String sourceId);
}
