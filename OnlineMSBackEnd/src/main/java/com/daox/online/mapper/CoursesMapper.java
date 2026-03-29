package com.daox.online.mapper;

import com.daox.online.entity.mysql.*;
import com.daox.online.entity.views.responseVO.EnrolledStudentVO;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface CoursesMapper {

    /**
     * 获取公开课程列表
     *
     * @param pageSize 页大小，默认为10
     * @param offset   偏移量，默认为0
     * @return 公开课程列表
     */
    @Select("SELECT * FROM courses WHERE is_deleted = 0 AND is_private = 0 and status=#{status} LIMIT #{pageSize} OFFSET #{offset}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "category_id", property = "categoryId"),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "cover_url", property = "coverUrl"),
            @Result(column = "is_public", property = "isPublic"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "is_deleted", property = "isDeleted"),
            @Result(column = "teacher_id", property = "teacherId"),
            @Result(column = "enrollment_count", property = "enrollmentCount"),
            @Result(column = "is_private", property = "isPrivate")
    })
    List<Courses> publicCourseList(int pageSize, int offset,String status);

    /**
     * 查询分类信息
     *
     * @param categoryId 分类id
     * @return 分类信息
     */
    @Select("select * from course_categories where id = #{categoryId}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "parent_id", property = "parentId"),
            @Result(column = "order_index", property = "orderIndex")
    })
    CourseCategories getCategoryById(String categoryId);

    /**
     * 获取课程基本信息
     *
     * @param courseId 课程id
     * @return 课程基本信息
     */
    @Select("select * from courses where id = #{courseId}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "category_id", property = "categoryId"),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "cover_url", property = "coverUrl"),
            @Result(column = "is_public", property = "isPublic"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "is_deleted", property = "isDeleted"),
            @Result(column = "teacher_id", property = "teacherId"),
            @Result(column = "enrollment_count", property = "enrollmentCount"),
            @Result(column = "is_private", property = "isPrivate")
    })
    Courses getCourseById(String courseId);

    /**
     * 获取课程分类 - 树形结构
     *
     * @return 课程分类
     */
    @Select("select * from course_categories")
    @Results({@Result(column = "id", property = "id"), @Result(column = "name", property = "name"), @Result(column = "parent_id", property = "parentId"), @Result(column = "order_index", property = "orderIndex")})
    List<CourseCategories> getCourseCategoriesTree();

    /**
     * 获取我的课程列表
     *
     * @param userId 用户ID
     * @return 课程列表
     */
    @Select("select * from user_courses where user_id=#{userId}")
    @Results({@Result(column = "id", property = "id"), @Result(column = "user_id", property = "userId"), @Result(column = "course_id", property = "courseId"), @Result(column = "enrollment_date", property = "enrollmentDate")})
    List<UserCourses> getMyCourseList(String userId);

    /**
     * 加入课程 - 已有课程
     *
     * @param id             主键ID
     * @param userId         用户ID
     * @param courseId       课程ID
     * @param enrollmentDate 加入时间
     * @return 插入结果
     */
    @Insert("insert into user_courses(id, user_id, course_id, enrollment_date) values(#{id}, #{userId}, #{courseId}, #{enrollmentDate})")
    int joinCourse(String id, String userId, String courseId, Date enrollmentDate);

    /**
     * 更新课程学习人数
     *
     * @param courseId        课程ID
     * @param enrollmentCount 新增学习人数
     * @return 更新结果
     */
    @Update("update courses set enrollment_count = #{enrollmentCount} where id = #{courseId}")
    int updateCourseEnrollmentCount(String courseId, int enrollmentCount);

    /**
     * 退出课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 退出结果
     */
    @Delete("delete from user_courses where user_id = #{userId} and course_id = #{courseId}")
    int quitCourse(String userId, String courseId);

    /**
     * 通过教师id查询课程列表
     *
     * @param teacherId 教师id
     * @return 课程列表
     */
    @Select("select * from courses where teacher_id = #{teacherId}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "category_id", property = "categoryId"),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "cover_url", property = "coverUrl"),
            @Result(column = "is_public", property = "isPublic"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "is_deleted", property = "isDeleted"),
            @Result(column = "teacher_id", property = "teacherId"),
            @Result(column = "enrollment_count", property = "enrollmentCount"),
            @Result(column = "is_private", property = "isPrivate")
    })
    List<Courses> getTeacherCourseList(String teacherId);

    /**
     * 获取课程资料 - 所有课程资料
     *
     * @param courseId 课程ID
     * @return 课程资料列表
     */
    @Select("select * from course_materials where course_id = #{courseId}")
    @Results({@Result(id = true, column = "id", property = "id"), @Result(column = "course_id", property = "courseId"), @Result(column = "chapter_id", property = "chapterId"), @Result(column = "section_id", property = "sectionId"), @Result(column = "title", property = "title"), @Result(column = "description", property = "description"), @Result(column = "material_url", property = "materialUrl"), @Result(column = "download_count", property = "downloadCount"), @Result(column = "is_public", property = "isPublic"), @Result(column = "upload_time", property = "uploadTime"), @Result(column = "updated_at", property = "updatedAt"), @Result(column = "is_deleted", property = "isDeleted")})
    List<CourseMaterials> getCourseMaterials(String courseId);

    /**
     * 获取课程资料 - 章节资料
     *
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @return 课程资料列表
     */
    @Select("select * from course_materials where course_id = #{courseId} and chapter_id = #{chapterId}")
    List<CourseMaterials> getCourseMaterialsByChapterId(String courseId, String chapterId);

    /**
     * 获取课程资料 - 小节资料
     *
     * @param courseId  课程ID
     * @param chapterId 章节ID
     * @param sectionId 小节ID
     * @return 课程资料
     */
    @Select("select * from course_materials where course_id = #{courseId} and chapter_id = #{chapterId} and section_id = #{sectionId}")
    List<CourseMaterials> getCourseMaterialsBySectionIdANDChapterId(String courseId, String chapterId, String sectionId);

    /**
     * 检查用户是否已加入指定课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 如果用户已加入课程返回true，否则返回false
     */
    @Select("SELECT COUNT(*) > 0 FROM user_courses WHERE user_id = #{userId} AND course_id = #{courseId}")
    boolean isUserEnrolledInCourse(String userId, String courseId);

    /**
     * 创建课程
     *
     * @param course 课程信息
     * @return 创建结果
     */
    @Insert("INSERT INTO courses (id, title, description, cover_image_url, teacher_id, category_id, status, enrollment_count, created_at, updated_at, is_deleted, is_private) " + "VALUES (#{id}, #{title}, #{description}, #{coverImageUrl}, #{teacherId}, #{categoryId}, #{status}, #{enrollmentCount}, #{createdAt}, #{updatedAt}, #{isDeleted}, #{isPrivate})")
    @Results({@Result(column = "id", property = "id"), @Result(column = "category_id", property = "categoryId"), @Result(column = "title", property = "title"), @Result(column = "description", property = "description"), @Result(column = "cover_url", property = "coverUrl"), @Result(column = "is_public", property = "isPublic"), @Result(column = "created_at", property = "createdAt"), @Result(column = "updated_at", property = "updatedAt"), @Result(column = "is_deleted", property = "isDeleted"), @Result(column = "teacher_id", property = "teacherId"), @Result(column = "enrollment_count", property = "enrollmentCount"), @Result(column = "is_private", property = "isPrivate")})
    int createCourse(Courses course);

    /**
     * 更新课程
     *
     * @param course 课程
     * @return 影响行数
     */
    @Update("UPDATE courses SET title = #{title}, description = #{description}, cover_image_url = #{coverImageUrl}, teacher_id = #{teacherId}, category_id = #{categoryId}, status = #{status}, enrollment_count = #{enrollmentCount}, updated_at = #{updatedAt}, is_deleted = #{isDeleted}, is_private = #{isPrivate} WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "category_id", property = "categoryId"),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "cover_url", property = "coverUrl"),
            @Result(column = "is_public", property = "isPublic"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "is_deleted", property = "isDeleted"),
            @Result(column = "teacher_id", property = "teacherId"),
            @Result(column = "enrollment_count", property = "enrollmentCount"),
            @Result(column = "is_private", property = "isPrivate")
    })
    int updateCourse(Courses course);

    /**
     * 更新课程属性
     *
     * @param courseProperties 课程属性
     * @return 影响行数
     */
    @Update("update course_properties set is_new=#{isNew},target_audience=#{targetAudience},requirements=#{requirements},price=#{price},original_price=#{originalPrice} where course_id=#{courseId}")
    int updateCourseProperties(CourseProperties courseProperties);

    /**
     * 更新课程封面
     *
     * @param courseID 课程ID
     * @param coverUrl 新的封面URL
     * @return 更新结果
     */
    @Update("UPDATE courses SET cover_image_url = #{coverUrl} WHERE id=#{courseID} and teacher_id=#{teacherId}")
    int updateCover(@Param("courseID") String courseID, @Param("coverUrl") String coverUrl, @Param("teacherId") String teacherId);

    /**
     * 检查分类id是否存在
     *
     * @param categoryId 分类id
     * @return true 存在 false 不存在
     */
    @Select("SELECT COUNT(*) > 0 FROM course_categories WHERE id = #{categoryId}")
    boolean isCategoryIdExists(String categoryId);

    /**
     * 判断课程id的合法性
     *
     * @param courseId 课程id
     * @return true 合法 false 不合法
     */
    @Select("SELECT COUNT(*) > 0 FROM courses WHERE id = #{courseId}")
    boolean isCourseIdExists(String courseId);

    /**
     * 删除课程 - 只是删除课程 - 逻辑删除
     *
     * @param courseId 课程id
     * @return 影响行数
     */
    @Update("update courses set is_deleted=1 where id=#{courseId}")
    int deleteCourse(String courseId);

    /**
     * 删除课程 - 删除课程下章节
     *
     * @param courseId 课程id
     * @return 影响行数
     */
    @Delete("DELETE FROM chapters WHERE course_id = #{courseId}")
    int deleteChapters(String courseId);

    /**
     * 删除课程 - 删除课程下章节下的小节
     *
     * @param chapterId 章节id
     * @return 影响行数
     */
    @Delete("DELETE FROM sections WHERE chapter_id = #{chapterId}")
    int deleteSections(String chapterId);

    /**
     * 删除课程 - 删除课程下的所有课程资料
     * TODO: 思考是否要删除，还是作为公开资料让所有教师可以选取？
     *
     * @param courseId 课程id
     * @return 影响行数
     */
    @Delete("DELETE FROM course_materials WHERE course_id = #{courseId}")
    int deleteCourseAndMaterials(String courseId);

    /**
     * 删除课程 - 删除课程下的所有用户课程关系
     *
     * @param courseId 课程id
     * @return 影响行数
     */
    @Delete("DELETE FROM user_courses WHERE course_id = #{courseId}")
    int deleteUserCourses(String courseId);

    /**
     * 发布课程
     *
     * @param courseId 课程id
     * @return 影响行数
     */
    @Update("UPDATE courses SET status = 'published' WHERE id = #{courseId}")
    int publishCourse(String courseId);

    /**
     * 检查课程是否存在
     *
     * @param courseId 课程id
     * @return 如果课程存在返回true，否则返回false
     */
    @Select("SELECT COUNT(*) > 0 FROM courses WHERE id = #{courseId}")
    boolean checkCourseExists(String courseId);

    /**
     * 归档课程
     *
     * @param courseId  课程id
     * @param teacherId 教师id
     * @return 受影响行数
     */
    @Update("UPDATE courses SET status = 'archived', updated_at = CURRENT_TIMESTAMP WHERE id = #{courseId} AND teacher_id = #{teacherId} AND status = 'published'")
    int archiveCourse(String courseId, String teacherId);

    /**
     * 获取选课学生列表
     *
     * @param courseId 课程ID
     * @return 选课学生列表
     */
    @Select("SELECT u.identifier, u.nickname, u.email, u.avatar_url AS avatarUrl, uc.enrollment_date " + "FROM users u " + "INNER JOIN user_courses uc ON u.id = uc.user_id " + "WHERE uc.course_id = #{courseId} " + "ORDER BY uc.enrollment_date DESC")
    @Results({
            @Result(column = "identifier", property = "identifier"), @Result(column = "nickname", property = "nickname"), @Result(column = "email", property = "email"), @Result(column = "avatarUrl", property = "avatarUrl"), @Result(column = "enrollment_date", property = "enrollmentDate")})
    List<EnrolledStudentVO> getEnrolledStudents(String courseId);

    /**
     * 检查课程所有权
     *
     * @param courseId  课程ID
     * @param teacherId 教师ID
     * @return 如果课程属于该教师返回1，否则返回0
     */
    @Select("SELECT COUNT(*) FROM courses WHERE id = #{courseId} AND teacher_id = #{teacherId} AND is_deleted = 0")
    int checkCourseOwnership(@Param("courseId") String courseId, @Param("teacherId") String teacherId);

    /**
     * 校验课程状态是否为发布状态
     *
     * @param courseId 课程ID
     * @return true: 课程已发布，false: 课程未发布
     */
    @Select("SELECT IF(status = 'published', true, false) FROM courses WHERE id = #{courseId}")
    boolean checkCourseStatus(String courseId);

    /**
     * 获取所有课程
     *
     * @return List<Courses>
     */
    @Select("SELECT * FROM courses")
    List<Courses> getCourseListAll();

    /**
     * 获取课程分类信息 - 通过id查询
     *
     * @param categoryId 分类id
     * @return 课程分类信息
     */
    @Select("SELECT * FROM course_categories WHERE id = #{categoryId}")
    @Results({@Result(column = "id", property = "id"), @Result(column = "name", property = "name"), @Result(column = "parent_id", property = "parentId"), @Result(column = "order_index", property = "orderIndex")})
    CourseCategories getCourseCategoryById(String categoryId);

    /**
     * 获取所有顶级课程分类（父级id为空）
     *
     * @return 顶级课程分类列表
     */
    @Select("SELECT id, name, parent_id, order_index FROM course_categories WHERE parent_id IS NULL OR parent_id = '0'")
    @Results({@Result(column = "id", property = "id"), @Result(column = "name", property = "name"), @Result(column = "parent_id", property = "parentId"), @Result(column = "order_index", property = "orderIndex")})
    List<CourseCategories> getTopLevelCategories();

    /**
     * 获取指定父分类的所有子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @Select("SELECT * FROM course_categories WHERE parent_id = #{parentId}")
    @Results({@Result(column = "id", property = "id"), @Result(column = "name", property = "name"), @Result(column = "parent_id", property = "parentId"), @Result(column = "order_index", property = "orderIndex")})
    List<CourseCategories> getSubCategories(String parentId);

    /**
     * 按名称查询分类。
     *
     * @param name 分类名称
     * @return 分类信息
     */
    @Select("SELECT * FROM course_categories WHERE name = #{name} LIMIT 1")
    @Results({@Result(column = "id", property = "id"), @Result(column = "name", property = "name"), @Result(column = "parent_id", property = "parentId"), @Result(column = "order_index", property = "orderIndex")})
    CourseCategories getCategoryByName(String name);

    /**
     * 创建分类
     *
     * @param courseCategories 分类
     * @return 创建结果
     */
    @Insert("INSERT INTO course_categories (id,name, parent_id, order_index) VALUES (#{id},#{name}, #{parentId}, #{orderIndex})")
    int createCategory(CourseCategories courseCategories);

    /**
     * 更新分类
     *
     * @param courseCategories 分类
     * @return 更新结果
     */
    @Update("UPDATE course_categories SET name = #{name}, parent_id = #{parentId}, order_index = #{orderIndex} WHERE id = #{id}")
    int updateCategory(CourseCategories courseCategories);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 删除结果
     */
    @Delete("DELETE FROM course_categories WHERE id = #{id}")
    int deleteCategory(String id);

    /**
     * 批量删除分类。
     *
     * @param ids 分类ID列表
     * @return 删除数量
     */
    @Delete("""
            <script>
            DELETE FROM course_categories
            WHERE id IN
            <foreach collection='ids' item='id' open='(' separator=',' close=')'>
                #{id}
            </foreach>
            </script>
            """)
    int batchDeleteCategories(@Param("ids") List<String> ids);

    /**
     * 统计指定分类下的直接子分类数量。
     *
     * @param parentId 父分类ID
     * @return 子分类数量
     */
    @Select("SELECT COUNT(*) FROM course_categories WHERE parent_id = #{parentId}")
    int countSubCategories(String parentId);

    /**
     * 统计指定分类下关联的课程数量。
     *
     * @param categoryId 分类ID
     * @return 课程数量
     */
    @Select("SELECT COUNT(*) FROM courses WHERE category_id = #{categoryId} AND COALESCE(is_deleted, 0) = 0")
    int countCoursesByCategoryId(String categoryId);

    /**
     * 查询多个分类下的有效课程。
     *
     * @param categoryIds 分类ID列表
     * @return 课程列表
     */
    @Select("""
            <script>
            SELECT *
            FROM courses
            WHERE COALESCE(is_deleted, 0) = 0
              AND category_id IN
              <foreach collection='categoryIds' item='categoryId' open='(' separator=',' close=')'>
                  #{categoryId}
              </foreach>
            ORDER BY updated_at DESC, created_at DESC
            </script>
            """)
    List<Courses> listCoursesByCategoryIds(@Param("categoryIds") List<String> categoryIds);

    /**
     * 批量迁移课程分类。
     *
     * @param targetCategoryId 目标分类ID
     * @param courseIds        课程ID列表
     * @return 更新数量
     */
    @Update("""
            <script>
            UPDATE courses
            SET category_id = #{targetCategoryId},
                updated_at = CURRENT_TIMESTAMP
            WHERE id IN
            <foreach collection='courseIds' item='courseId' open='(' separator=',' close=')'>
                #{courseId}
            </foreach>
            </script>
            """)
    int batchUpdateCourseCategory(@Param("targetCategoryId") String targetCategoryId,
                                  @Param("courseIds") List<String> courseIds);

    /**
     * 获取课程统计 - 有效数量
     *
     * @return 课程统计数量
     */
    @Select("SELECT COUNT(*) FROM courses WHERE COALESCE(is_deleted, 0) = 0")
    int getCourseCount();

    /**
     * 获取已发布课程数量
     *
     * @return 已发布课程数量
     */
    @Select("SELECT COUNT(*) FROM courses WHERE status = 'published' AND COALESCE(is_deleted, 0) = 0")
    int getPublishedCourseCount();

    /**
     * 获取草稿课程数量
     *
     * @return 草稿课程数量
     */
    @Select("SELECT COUNT(*) FROM courses WHERE status = 'draft' AND COALESCE(is_deleted, 0) = 0")
    int getDraftCourseCount();

    /**
     * 获取已归档课程数量
     *
     * @return 已归档课程数量
     */
    @Select("SELECT COUNT(*) FROM courses WHERE status = 'archived' AND COALESCE(is_deleted, 0) = 0")
    int getArchivedCourseCount();

    /**
     * 获取公开课程数量
     *
     * @return 公开课程数量
     */
    @Select("SELECT COUNT(*) FROM courses WHERE is_private = 0 AND COALESCE(is_deleted, 0) = 0")
    int getPublicCourseCount();

    /**
     * 获取私有课程数量
     *
     * @return 私有课程数量
     */
    @Select("SELECT COUNT(*) FROM courses WHERE is_private = 1 AND COALESCE(is_deleted, 0) = 0")
    int getPrivateCourseCount();

    /**
     * 获取总学习人数
     *
     * @return 总学习人数
     */
    @Select("SELECT COALESCE(SUM(enrollment_count), 0) FROM courses WHERE COALESCE(is_deleted, 0) = 0")
    int getTotalEnrollments();

    /**
     * 按分类统计课程数量和学习人数
     *
     * @return 分类统计列表
     */
    @Select("SELECT cc.id as categoryId, cc.name as categoryName, " +
            "COUNT(c.id) as courseCount, COALESCE(SUM(c.enrollment_count), 0) as totalEnrollments " +
            "FROM course_categories cc " +
            "LEFT JOIN courses c ON cc.id = c.category_id AND COALESCE(c.is_deleted, 0) = 0 " +
            "GROUP BY cc.id, cc.name " +
            "ORDER BY courseCount DESC")
    @Results({
            @Result(column = "categoryId", property = "categoryId"),
            @Result(column = "categoryName", property = "categoryName"),
            @Result(column = "courseCount", property = "courseCount"),
            @Result(column = "totalEnrollments", property = "totalEnrollments")
    })
    List<Map<String, Object>> getCategoryStatistics();

    /**
     * 按状态统计课程数量
     *
     * @return 状态统计列表
     */
    @Select("SELECT status, COUNT(*) as courseCount " +
            "FROM courses " +
            "WHERE COALESCE(is_deleted, 0) = 0 " +
            "GROUP BY status " +
            "ORDER BY courseCount DESC")
    @Results({
            @Result(column = "status", property = "status"),
            @Result(column = "courseCount", property = "courseCount")
    })
    List<Map<String, Object>> getStatusStatistics();

    /**
     * 获取最受欢迎的课程（前10名）
     *
     * @return 热门课程列表
     */
    @Select("SELECT c.id as courseId, c.title as courseTitle, u.nickname as teacherName, " +
            "cc.name as categoryName, c.enrollment_count as enrollmentCount, c.status " +
            "FROM courses c " +
            "LEFT JOIN users u ON c.teacher_id = u.id " +
            "LEFT JOIN course_categories cc ON c.category_id = cc.id " +
            "WHERE COALESCE(c.is_deleted, 0) = 0 " +
            "ORDER BY c.enrollment_count DESC " +
            "LIMIT 10")
    @Results({
            @Result(column = "courseId", property = "courseId"),
            @Result(column = "courseTitle", property = "courseTitle"),
            @Result(column = "teacherName", property = "teacherName"),
            @Result(column = "categoryName", property = "categoryName"),
            @Result(column = "enrollmentCount", property = "enrollmentCount"),
            @Result(column = "status", property = "status")
    })
    List<Map<String, Object>> getPopularCourses();

    /**
     * 通过课程id获取课程属性信息
     *
     * @param courseId 课程id
     * @return 课程属性信息
     */
    @Select("select * from course_properties where course_id=#{courseId}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "course_id", property = "courseId"),
            @Result(column = "level", property = "level"),
            @Result(column = "is_new", property = "isNew"),
            @Result(column = "target_audience", property = "targetAudience"),
            @Result(column = "requirements", property = "requirements"),
            @Result(column = "price", property = "price"),
            @Result(column = "original_price", property = "originalPrice")
    })
    CourseProperties getCoursePropertiesById(String courseId);

    /**
     * 创建课程属性
     *
     * @param courseProperties 课程属性
     * @return 创建结果
     */
    @Insert("insert into course_properties (id,course_id, level, is_new, target_audience, requirements, price, original_price) values (#{id},#{courseId}, #{level}, #{isNew}, #{targetAudience}, #{requirements}, #{price}, #{originalPrice})")
    int createCourseProperties(CourseProperties courseProperties);

    /**
     * 检查两个用户是否属于同一课程
     *
     * @param userId 用户id
     * @return 课程id
     */
    @Select("select course_id from user_courses where user_id =#{userId}")
    String checkUserInCourse(String userId);
}
