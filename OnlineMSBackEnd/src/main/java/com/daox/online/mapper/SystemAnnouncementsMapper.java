package com.daox.online.mapper;

import com.daox.online.entity.mysql.SystemAnnouncements;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Mapper
public interface SystemAnnouncementsMapper {

    /**
     * 获取系统公告 - 分页查询
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 系统公告列表
     */
    @Select("SELECT * FROM system_announcements ORDER BY created_at DESC LIMIT #{pageNum}, #{pageSize}")
    @Results({@Result(column = "id", property = "id"), @Result(column = "title", property = "title"), @Result(column = "content", property = "content"), @Result(column = "creator_id", property = "creatorId"), @Result(column = "is_active", property = "isActive"), @Result(column = "created_at", property = "createdAt"), @Result(column = "expired_time", property = "expiredAt")})
    List<SystemAnnouncements> getSystemAnnouncements(int pageNum, int pageSize);

    /**
     * 发布系统公告
     *
     * @param systemAnnouncements 系统公告
     * @return 状态码
     */
    @Insert("insert into system_announcements (id, title, content, creator_id, is_active, created_at, expired_time) values (#{id}, #{title}, #{content}, #{creatorId}, #{isActive}, #{createdAt}, #{expiredAt})")
    int publishSystemAnnouncement(SystemAnnouncements systemAnnouncements);

    /**
     * 更新过期的系统公告
     *
     * @param now 当前时间
     * @return 更新的记录数
     */
    @Update("UPDATE system_announcements SET is_active = 1 WHERE expired_time < #{now} AND is_active = 0")
    int updateExpiredAnnouncements(@Param("now") LocalDateTime now);

    /**
     * 更新系统公告
     *
     * @param id          系统公告ID
     * @param title       系统公告标题
     * @param content     系统公告内容
     * @param isActive    系统公告是否激活
     * @param expiredTime 系统公告过期时间
     * @return 更新结果
     */
    @Update("UPDATE system_announcements SET title = #{title}, content = #{content}, is_active = #{isActive}, expired_time = #{expiredTime} WHERE id = #{id}")
    int updateSystemAnnouncement(@Param("id") String id,
                                 @Param("title") String title,
                                 @Param("content") String content,
                                 @Param("isActive") Integer isActive,
                                 @Param("expiredTime") Date expiredTime);

    /**
     * 根据id查询公告
     *
     * @param id 公告id
     * @return 公告对象
     */
    @Select("SELECT * FROM system_announcements WHERE id = #{id}")
    @Results({@Result(column = "id", property = "id"), @Result(column = "title", property = "title"), @Result(column = "content", property = "content"), @Result(column = "creator_id", property = "creatorId"), @Result(column = "is_active", property = "isActive"), @Result(column = "created_at", property = "createdAt"), @Result(column = "expired_time", property = "expiredAt")})
    SystemAnnouncements getSystemAnnouncementById(String id);

    /**
     * 删除公告 - 本质是过期公告
     *
     * @param id 公告id
     * @return 删除结果
     */
    @Update("UPDATE system_announcements SET is_active = 1 , expired_time=now() WHERE id = #{id}")
    int deleteSystemAnnouncement(String id);

}
