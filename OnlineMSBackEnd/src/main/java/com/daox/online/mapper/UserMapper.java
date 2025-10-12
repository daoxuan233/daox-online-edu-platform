package com.daox.online.mapper;

import com.daox.online.entity.mysql.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 根据用户 ID 列表批量查询用户信息
     * @param ids 用户 ID 列表
     * @return 用户实体列表
     */
    List<Users> findByIdIn(@Param("ids") List<String> ids);
}
