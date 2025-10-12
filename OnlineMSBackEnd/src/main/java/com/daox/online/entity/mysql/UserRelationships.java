package com.daox.online.entity.mysql;

import java.io.Serializable;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 用户关系表<br />
 * 用于存储用户之间的关系，如好友、黑名单等
 * TableName: user_relationships
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserRelationships implements Serializable {

    /**
     * 关系ID，主键
     */
    private String id;
    /**
     * 用户1的ID (值较小的用户ID)
     */
    private String userOneId;
    /**
     * 用户2的ID (值较大的用户ID)
     */
    private String userTwoId;
    /**
     * 关系状态 (<br />
     * 0: PENDING [ 等待确认 ], <br />
     * 1: ACCEPTED [ 双方已是好友 ], <br />
     * 2: DECLINED [ 拒绝 ], <br />
     * 3: BLOCKED [ 拉黑 ])
     */
    private Integer status;
    /**
     * 执行最后操作的用户ID
     */
    private String actionUserId;
    /**
     * user_one_id对user_two_id的备注
     */
    private String remarkOne;
    /**
     * user_two_id对user_one_id的备注
     */
    private String remarkTwo;
    /**
     * 创建时间
     */
    private Date createdAt;
    /**
     * 更新时间
     */
    private Date updatedAt;
}
