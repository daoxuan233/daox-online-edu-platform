package com.daox.online.mapper;

import com.daox.online.entity.mysql.Files;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 文件管理Mapper接口
 */
@Mapper
public interface FilesMapper {
    /**
     * 插入文件信息
     * @param file 文件信息
     */
    @Insert("INSERT INTO files (id, original_filename, object_name, file_type, mime_type, file_size, uploader_id, allow_download, upload_time, updated_at) " +
            "VALUES (#{id}, #{originalFilename}, #{objectName}, #{fileType}, #{mimeType}, #{fileSize}, #{uploaderId}, #{allowDownload}, NOW(), NOW())")
    void insert(Files file);

    /**
     * 查询所有文件
     * @param id 文件ID
     * @return 文件列表
     */
    @Select("SELECT * FROM files WHERE id = #{id} AND is_deleted = 0")
    Files findById(String id);

    /**
     * 根据文件ID反查关联的小节ID（可能通过 course_materials 或 sections 表）
     * @param fileId 文件ID
     * @return 关联的小节ID
     */
    @Select("SELECT section_id FROM course_materials WHERE file_id = #{fileId} " +
            "UNION " +
            "SELECT id FROM sections WHERE video_url = #{fileId}")
    String findSectionIdByFileId(String fileId);

    /**
     * 根据小节ID查询关联的文件
     * @param sectionId 小节ID
     * @return 文件列表
     */
    @Select("SELECT f.* FROM files f JOIN course_materials cm ON f.id = cm.file_id WHERE cm.section_id = #{sectionId}")
    List<Files> findFilesBySectionId(@Param("sectionId") String sectionId);

}