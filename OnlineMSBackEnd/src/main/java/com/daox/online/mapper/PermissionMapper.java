package com.daox.online.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PermissionMapper {
    /**
     * 检查用户是否已加入某文件关联的课程
     * 支持检查course_materials表中的文件和sections表中的视频文件
     * @param userId 用户 ID
     * @param fileId 文件 ID
     * @return 存在记录则返回 1，否则返回 0 或 null
     */
    Integer checkUserAccessToFile(@Param("userId") String userId, @Param("fileId") String fileId);
}
