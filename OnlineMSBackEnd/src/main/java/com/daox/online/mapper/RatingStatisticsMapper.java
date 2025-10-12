package com.daox.online.mapper;

import com.daox.online.entity.dto.RatingStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RatingStatisticsMapper {
    /**
     * 获取评分统计信息
     */
    List<RatingStatistics> getRatingStatistics(@Param("courseId") String courseId,
                                               @Param("teacherId") String teacherId);
}
