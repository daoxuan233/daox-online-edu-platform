package com.daox.online.entity.mysql;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程表<br />
 * 存储课程的核心信息-是组织教学活动（章节、测评）的中心实体<br />
 * TableName:  courses
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Courses implements Serializable {

    /**
     * 课程ID
     */
    private String id;
    /**
     * 课程标题
     */
    private String title;
    /**
     * 课程描述
     */
    private String description;
    /**
     * 封面图URL
     */
    private String coverImageUrl;
    /**
     * 教师ID
     */
    private String teacherId;
    /**
     * 分类ID
     */
    private String categoryId;
    /**
     * 课程状态:<br />
     * draft[草稿] <br />
     * pending[待审核] <br />
     * published[已发布] <br />
     * taken_down[已下架] <br />
     * archived[已归档]
     * <p>
     * 五个状态值和合法跳转关系：<br />
     * draft       → pending（教师提交审核）<br />
     * draft       → archived（教师直接放弃）<br />
     * pending     → published（管理员审核通过）<br />
     * pending     → draft（管理员拒绝，退回修改）<br />
     * published   → taken_down（管理员下架）<br />
     * taken_down  → published（管理员重新上架）<br />
     * taken_down  → archived（管理员归档）<br />
     * archived    → （终态，不可跳转）
    * </p>
    * <p>
    * 注意：该字段只能通过课程状态机相关业务接口流转修改，
    * 不应在普通课程编辑接口中被直接覆盖。
     * </p>
     */
    private String status;
    /**
     * 学习人数
     */
    private Integer enrollmentCount;
    /**
     * 创建时间
     */
    private Date createdAt;
    /**
     * 更新时间
     */
    private Date updatedAt;
    /**
     * 逻辑删除标志 [1-"删除"，0-"正常"]
     */
    private Integer isDeleted;
    /**
     * 是否公开课程 [1-"私有"，0-"公开"]
     */
    private Integer isPrivate;

}
