package com.daox.online.mapper;

import com.daox.online.entity.mysql.UserDescribe;
import com.daox.online.entity.mysql.Users;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface SysUsersMapper {

    /**
     * 根据昵称,邮箱,编号查找用户
     *
     * @param keyword 昵称,邮箱,编号
     * @return 用户
     */
    @Select("SELECT * FROM users WHERE nickname = #{keyword} OR email = #{keyword} OR identifier =#{keyword}")
    @Results({@Result(property = "id", column = "id"), @Result(property = "identifier", column = "identifier"), @Result(property = "password", column = "password"), @Result(property = "nickname", column = "nickname"), @Result(property = "email", column = "email"), @Result(property = "role", column = "role"), @Result(property = "avatarUrl", column = "avatar_url"), @Result(property = "createdAt", column = "created_at"), @Result(property = "updatedAt", column = "updated_at"), @Result(property = "isDeleted", column = "is_deleted"),})
    Users findByUsernameOrEmailOrIdentifier(String keyword);

    /**
     * 添加用户 - 学生自行注册
     *
     * @param user 用户信息
     * @return 影响行数
     */
    @Insert("INSERT INTO users (id,identifier, password, nickname, email, " + "role, avatar_url, created_at, updated_at, is_deleted) " + "VALUES (#{id},#{identifier}, #{password}, #{nickname}, " + "#{email}, #{role}, #{avatarUrl}, #{createdAt}, #{updatedAt}, #{isDeleted})")
    @Results({@Result(property = "id", column = "id"), @Result(property = "identifier", column = "identifier"), @Result(property = "password", column = "password"), @Result(property = "nickname", column = "nickname"), @Result(property = "email", column = "email"), @Result(property = "role", column = "role"), @Result(property = "avatarUrl", column = "avatar_url"), @Result(property = "createdAt", column = "created_at"), @Result(property = "updatedAt", column = "updated_at"), @Result(property = "isDeleted", column = "is_deleted"),})
    int registerUser(Users user);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    @Results({@Result(property = "id", column = "id"), @Result(property = "identifier", column = "identifier"), @Result(property = "password", column = "password"), @Result(property = "nickname", column = "nickname"), @Result(property = "email", column = "email"), @Result(property = "role", column = "role"), @Result(property = "avatarUrl", column = "avatar_url"), @Result(property = "createdAt", column = "created_at"), @Result(property = "updatedAt", column = "updated_at"), @Result(property = "isDeleted", column = "is_deleted"),})
    Users findUserByEmail(String email);

    /**
     * 检查nickname是否存在
     *
     * @param nickname 昵称/用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE nickname = #{nickname}")
    @Results({@Result(property = "id", column = "id"), @Result(property = "identifier", column = "identifier"), @Result(property = "password", column = "password"), @Result(property = "nickname", column = "nickname"), @Result(property = "email", column = "email"), @Result(property = "role", column = "role"), @Result(property = "avatarUrl", column = "avatar_url"), @Result(property = "createdAt", column = "created_at"), @Result(property = "updatedAt", column = "updated_at"), @Result(property = "isDeleted", column = "is_deleted"),})
    Users findUserByNickname(String nickname);

    /**
     * 检查identifier是否存在
     *
     * @param identifier 学号/工号
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE identifier = #{identifier}")
    @Results({@Result(property = "id", column = "id"), @Result(property = "identifier", column = "identifier"), @Result(property = "password", column = "password"), @Result(property = "nickname", column = "nickname"), @Result(property = "email", column = "email"), @Result(property = "role", column = "role"), @Result(property = "avatarUrl", column = "avatar_url"), @Result(property = "createdAt", column = "created_at"), @Result(property = "updatedAt", column = "updated_at"), @Result(property = "isDeleted", column = "is_deleted"),})
    Users findUserByIdentifier(String identifier);

    /**
     * 更新密码
     *
     * @param email    邮箱
     * @param password 密码
     */
    @Update("update users set password=#{password} where email=#{email}")
    @Results({@Result(property = "id", column = "id"), @Result(property = "identifier", column = "identifier"), @Result(property = "password", column = "password"), @Result(property = "nickname", column = "nickname"), @Result(property = "email", column = "email"), @Result(property = "role", column = "role"), @Result(property = "avatarUrl", column = "avatar_url"), @Result(property = "createdAt", column = "created_at"), @Result(property = "updatedAt", column = "updated_at"), @Result(property = "isDeleted", column = "is_deleted"),})
    int updatePassword(String email, String password);

    /**
     * 更新密码
     *
     * @param email     邮箱
     * @param password  密码
     * @param updatedAt 更新时间
     * @return 影响行数
     */
    @Update("update users set password=#{password}, updated_at=#{updatedAt} where email=#{email}")
    int updatePassword_verify(@Param("email") String email, @Param("password") String password, @Param("updatedAt") Date updatedAt);

    /**
     * 根据用户id查询用户
     *
     * @param userId 用户id
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE id = #{userId}")
    @Results({@Result(property = "id", column = "id"), @Result(property = "identifier", column = "identifier"), @Result(property = "password", column = "password"), @Result(property = "nickname", column = "nickname"), @Result(property = "email", column = "email"), @Result(property = "role", column = "role"), @Result(property = "avatarUrl", column = "avatar_url"), @Result(property = "createdAt", column = "created_at"), @Result(property = "updatedAt", column = "updated_at"), @Result(property = "isDeleted", column = "is_deleted"),})
    Users findUserById(String userId);

    /**
     * 更新用户头像
     *
     * @param userId    用户ID
     * @param avatarUrl 新的头像URL
     * @return 更新结果
     */
    @Update("UPDATE users SET avatar_url = #{avatarUrl} WHERE id = #{userId}")
    int updateAvatar(@Param("userId") String userId, @Param("avatarUrl") String avatarUrl);

    /**
     * 获取用户列表 - admin
     *
     * @return 用户列表 - 非 Admin 用户
     */
    @Select("SELECT * FROM users WHERE role != 'admin'")
    @Results({@Result(property = "id", column = "id"), @Result(property = "identifier", column = "identifier"), @Result(property = "password", column = "password"), @Result(property = "nickname", column = "nickname"), @Result(property = "email", column = "email"), @Result(property = "role", column = "role"), @Result(property = "avatarUrl", column = "avatar_url"), @Result(property = "createdAt", column = "created_at"), @Result(property = "updatedAt", column = "updated_at"), @Result(property = "isDeleted", column = "is_deleted"),})
    List<Users> getUserListNotAdmin();

    /**
     * 获取全量用户列表。
     *
     * @return 用户列表
     */
    @Select("SELECT * FROM users")
    @Results({@Result(property = "id", column = "id"), @Result(property = "identifier", column = "identifier"), @Result(property = "password", column = "password"), @Result(property = "nickname", column = "nickname"), @Result(property = "email", column = "email"), @Result(property = "role", column = "role"), @Result(property = "avatarUrl", column = "avatar_url"), @Result(property = "createdAt", column = "created_at"), @Result(property = "updatedAt", column = "updated_at"), @Result(property = "isDeleted", column = "is_deleted"),})
    List<Users> getUserListAll();

    /**
     * 删除用户 - 逻辑删除
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    @Update("UPDATE users SET is_deleted = 1 WHERE id = #{userId}")
    int deleteUser(@Param("userId") String userId);

    /**
     * 恢复用户 - 取消逻辑删除。
     *
     * @param userId 用户ID
     * @return 更新结果
     */
    @Update("UPDATE users SET is_deleted = 0, updated_at = CURRENT_TIMESTAMP WHERE id = #{userId}")
    int restoreUser(@Param("userId") String userId);

    /**
     * 重置用户密码 - admin重置
     *
     * @param userId   用户ID
     * @param password 新密码
     * @return 修改结果
     */
    @Update("UPDATE users SET password = #{password} WHERE id = #{userId}")
    int resetUserPassword(@Param("userId") String userId, @Param("password") String password);

    /**
     * 获取用户统计 - 总数
     *
     * @return 用户总数
     */
    @Select("SELECT COUNT(*) FROM users")
    int getUserCount();

    /**
     * 获取管理员数量
     *
     * @return 管理员数量
     */
    @Select("SELECT COUNT(*) FROM users WHERE role = 'admin'")
    int getAdminCount();

    /**
     * 获取教师数量
     *
     * @return 教师数量
     */
    @Select("SELECT COUNT(*) FROM users WHERE role = 'teacher'")
    int getTeacherCount();

    /**
     * 获取学生数量
     *
     * @return 学生数量
     */
    @Select("SELECT COUNT(*) FROM users WHERE role = 'student'")
    int getStudentCount();

    /**
     * 获取用户统计 - 有效数据
     *
     * @return 用户有效数据
     */
    @Select("SELECT COUNT(*) FROM users WHERE is_deleted = 0")
    int getUserCountValid();

    /**
     * 获取管理员统计 - 有效数据
     *
     * @return 管理员有效数据
     */
    @Select("SELECT COUNT(*) FROM users WHERE role = 'admin' AND is_deleted = 0")
    int getAdminCountValid();

    /**
     * 获取教师统计 - 有效数据
     *
     * @return 教师有效数据
     */
    @Select("SELECT COUNT(*) FROM users WHERE role = 'teacher' AND is_deleted = 0")
    int getTeacherCountValid();

    /**
     * 获取学生统计 - 有效数据
     *
     * @return 学生有效数据
     */
    @Select("SELECT COUNT(*) FROM users WHERE role = 'student' AND is_deleted = 0")
    int getStudentCountValid();

    /**
     * 获取用户描述信息 - 通过id
     *
     * @param userId 用户id
     * @return 用户描述信息
     */
    @Select("SELECT * FROM user_describe WHERE user_id = #{userId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "birthday", column = "birthday"),
            @Result(property = "biography", column = "biography")
    })
    UserDescribe getUserDescribeById(String userId);

    /**
     * 更新user信息
     *
     * @param user user
     * @return 影响行数
     */
    @Update("UPDATE users SET identifier = #{identifier}, nickname = #{nickname}, email = #{email}, role = #{role}, avatar_url = #{avatarUrl}, is_deleted = #{isDeleted}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateUser(Users user);

    /**
     * 更新描述信息
     *
     * @param userDescribe userDescribe
     * @return 影响行数
     */
    @Update("UPDATE user_describe SET phone = #{phone}, gender = #{gender}, birthday = #{birthday}, biography = #{biography} WHERE user_id = #{userId}")
    int updateUserDescribe(UserDescribe userDescribe);

    /**
     * 插入user描述信息
     * @param userDescribe userDescribe
     * @return 影响行数
     */
    @Insert("INSERT INTO user_describe (id,user_id, phone, gender, birthday, biography) VALUES (#{id},#{userId}, #{phone}, #{gender}, #{birthday}, #{biography})")
    int insertUserDescribe(UserDescribe userDescribe);


    // 添加用户 - 分配用户 - assignment
}
