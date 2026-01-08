package com.daox.online.service.friend;

import com.daox.online.entity.RestBean;
import com.daox.online.entity.mysql.UserRelationships;
import com.daox.online.entity.mysql.Users;
import com.daox.online.entity.views.responseVO.chat.ChatUserVo;
import com.daox.online.mapper.CoursesMapper;
import com.daox.online.mapper.SysUsersMapper;
import com.daox.online.mapper.UserCoursesMapper;
import com.daox.online.mapper.UserRelationshipsMapper;
import com.daox.online.uilts.HybridIdGenerator;
import com.daox.online.uilts.UserIdUtil;
import com.daox.online.uilts.constant.Const;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 好友相关业务实现类
 * <p>
 * 当前类主要负责“根据前端传入的 friend_identifier 查询好友候选人”的业务逻辑：
 * 1. 根据 friend_identifier 查询 users 表，确认该标识符对应的用户是否存在；
 * 2. 如果存在候选用户，检查当前用户与该用户是否在同一门课程中（user_courses 表）；
 * 3. 如果在同一门课程中，则允许添加为好友，返回对方的基础信息；
 * 4. 如果不在同一门课程中，则进一步检查该 friend_identifier 是否是当前用户所选课程的任课教师；
 * 5. 如果是任课教师，则同样返回教师的基础信息，允许作为聊天对象展示；
 * 6. 如果上述条件都不满足，则返回空结果（或由上层根据 null 判定“不允许添加好友”）。
 */
@Slf4j
@Service
public class FriendsServiceImpl {

    /**
     * 用户基础信息 Mapper，用于访问 users 表
     */
    @Resource
    private SysUsersMapper sysUsersMapper;

    /**
     * 用户选课 Mapper，用于访问 user_courses 表
     */
    @Resource
    private UserCoursesMapper userCoursesMapper;

    /**
     * 课程 Mapper，用于访问 courses 表
     */
    @Resource
    private CoursesMapper coursesMapper;

    /**
     * 用户关系 Mapper，用于检查是否已是好友
     */
    @Resource
    private UserRelationshipsMapper userRelationshipsMapper;

    @Resource
    private HybridIdGenerator hybridIdGenerator;

    /**
     * 添加好友（前端直接传入双方用户ID）
     * <p>
     * 该方法负责创建“好友申请”（PENDING）并写入 user_relationships 表，遵循以下关键约束与规则：
     * <ul>
     *     <li>写库前必须对双方ID进行排序，确保 user_one_id / user_two_id 有序存储（字典序较小的在前）。</li>
     *     <li>若双方已是好友（status=ACCEPTED），直接返回“双方已经是好友”。</li>
     *     <li>若已存在 PENDING 申请：返回明确提示（已发送/待你确认）。</li>
     *     <li>若存在 DECLINED/BLOCKED：明确返回拒绝，不允许再次发起。</li>
     *     <li>禁止添加自己；禁止添加已删除用户；禁止添加 admin。</li>
     * </ul>
     * <p>
     * 说明：本方法只负责把关系写成 PENDING（Const.FRIEND_RELATIONSHIP_STATUS_PENDING），
     * 只有对方通过“确认好友申请”接口后，状态才会变更为 ACCEPTED（Const.FRIEND_RELATIONSHIP_STATUS_ACCEPTED）。
     *
     * @param currentUserId 当前登录用户ID（发起添加的人）
     * @param targetUserId  目标用户ID（被添加的人）
     * @return RestBean：成功/失败及提示信息
     */
    @Transactional
    public RestBean<String> addFriend(String currentUserId, String targetUserId, String remark) {
        // 1) 参数校验：双方ID均不能为空
        if (currentUserId == null || currentUserId.isEmpty() || targetUserId == null || targetUserId.isEmpty()) {
            return RestBean.failure(400, "用户ID不能为空");
        }
        // 2) 禁止添加自己
        if (currentUserId.equals(targetUserId)) {
            return RestBean.failure(403, "不能添加自己为好友");
        }

        // 3) 校验双方用户是否存在以及是否可用
        Users currentUser = sysUsersMapper.findUserById(currentUserId);
        Users targetUser = sysUsersMapper.findUserById(targetUserId);
        if (currentUser == null || targetUser == null) {
            return RestBean.failure(404, "用户不存在");
        }
        if (currentUser.getIsDeleted() != null && currentUser.getIsDeleted() != 0) {
            return RestBean.failure(403, "当前用户不可用");
        }
        if (targetUser.getIsDeleted() != null && targetUser.getIsDeleted() != 0) {
            return RestBean.failure(403, "目标用户不可用");
        }
        // 4) 按项目约束：不允许添加 admin 为好友
        if ("admin".equalsIgnoreCase(targetUser.getRole())) {
            return RestBean.failure(403, "不允许添加该用户为好友");
        }

        // 5) 写库前必须先排序：确保 user_one_id / user_two_id 有序存入，避免重复关系记录
        //    例如：A-B 与 B-A 应视为同一条关系；通过排序统一到 (minId, maxId) 的存储形式
        UserIdUtil.OrderedUserIds ordered = UserIdUtil.orderUserIds(currentUserId, targetUserId);
        String userOneId = ordered.userOneId();
        String userTwoId = ordered.userTwoId();
        String cleanedRemark = null;
        if (remark != null) {
            String temp = remark.trim();
            if (!temp.isEmpty()) {
                cleanedRemark = temp;
            }
        }
        boolean currentIsUserOne = currentUserId.equals(userOneId);

        // 6) 先快速判断“正常好友关系”是否存在（status=ACCEPTED）
        String acceptedRelationshipId = userRelationshipsMapper.checkNormalFriendship(userOneId, userTwoId);
        if (acceptedRelationshipId != null) {
            return RestBean.success("双方已经是好友");
        }

        // 7) 查询是否存在历史关系记录（包括 PENDING/DECLINED/BLOCKED 等），便于做明确状态返回
        UserRelationships existing = userRelationshipsMapper.findRelationship(userOneId, userTwoId);
        Date now = new Date();
        if (existing == null) {
            // 7.1 不存在任何历史记录：插入一条新的关系记录，状态为 PENDING（等待对方确认）
            UserRelationships relationship = new UserRelationships()
                    .setId(hybridIdGenerator.generateId())
                    .setUserOneId(userOneId)
                    .setUserTwoId(userTwoId)
                    .setStatus(Const.FRIEND_RELATIONSHIP_STATUS_PENDING)
                    .setActionUserId(currentUserId)
                    .setRemarkOne(currentIsUserOne ? cleanedRemark : null)
                    .setRemarkTwo(currentIsUserOne ? null : cleanedRemark)
                    .setCreatedAt(now)
                    .setUpdatedAt(now);
            int inserted = userRelationshipsMapper.insertRelationship(relationship);
            if (inserted <= 0) {
                return RestBean.failure(500, "添加好友失败");
            }
            return RestBean.success("好友申请已发送，等待对方确认");
        } else {
            // 7.2 已存在历史记录：根据不同 status 做明确返回
            if (Const.FRIEND_RELATIONSHIP_STATUS_ACCEPTED.equals(existing.getStatus())) {
                return RestBean.success("双方已经是好友");
            }
            if (Const.FRIEND_RELATIONSHIP_STATUS_BLOCKED.equals(existing.getStatus())) {
                if (currentUserId.equals(existing.getActionUserId())) {
                    return RestBean.failure(403, "你已将对方拉黑，无法添加好友");
                }
                return RestBean.failure(403, "对方已将你拉黑，无法添加好友");
            }
            if (Const.FRIEND_RELATIONSHIP_STATUS_DECLINED.equals(existing.getStatus())) {
                return RestBean.failure(403, "对方已拒绝你的好友申请");
            }
            if (Const.FRIEND_RELATIONSHIP_STATUS_PENDING.equals(existing.getStatus())) {
                if (currentUserId.equals(existing.getActionUserId())) {
                    if (cleanedRemark != null) {
                        int remarkUpdated;
                        if (currentIsUserOne) {
                            remarkUpdated = userRelationshipsMapper.updateRemarkOneById(existing.getId(), cleanedRemark, currentUserId, now);
                        } else {
                            remarkUpdated = userRelationshipsMapper.updateRemarkTwoById(existing.getId(), cleanedRemark, currentUserId, now);
                        }
                        if (remarkUpdated <= 0) {
                            return RestBean.failure(500, "更新备注失败");
                        }
                    }
                    return RestBean.success("好友申请已发送，等待对方确认");
                } else {
                    return RestBean.failure(409, "对方已向你发起好友申请，请先确认");
                }
            }
            return RestBean.failure(403, "当前关系状态不允许添加好友");
        }
    }

    /**
     * 确认好友申请：将双方关系从 PENDING 更新为 ACCEPTED
     * <p>
     * 约定：
     * - PENDING 状态下，action_user_id 记录“发起申请的人”；
     * - 只有“被申请的人”才能确认（即当前用户ID不能等于 action_user_id）。
     * <p>
     * 对于 DECLINED/BLOCKED：
     * - 直接拒绝确认并返回明确提示（避免绕过拉黑/拒绝逻辑）。
     *
     * @param currentUserId   当前登录用户ID（确认的人）
     * @param requesterUserId 发起好友申请的用户ID
     * @return RestBean：确认结果
     */
    @Transactional
    public RestBean<String> confirmFriendRequest(String currentUserId, String requesterUserId, String remark) {
        if (currentUserId == null || currentUserId.isEmpty() || requesterUserId == null || requesterUserId.isEmpty()) {
            return RestBean.failure(400, "用户ID不能为空");
        }
        if (currentUserId.equals(requesterUserId)) {
            return RestBean.failure(403, "不能确认自己发起的好友申请");
        }

        Users currentUser = sysUsersMapper.findUserById(currentUserId);
        Users requesterUser = sysUsersMapper.findUserById(requesterUserId);
        if (currentUser == null || requesterUser == null) {
            return RestBean.failure(404, "用户不存在");
        }
        if (currentUser.getIsDeleted() != null && currentUser.getIsDeleted() != 0) {
            return RestBean.failure(403, "当前用户不可用");
        }
        if (requesterUser.getIsDeleted() != null && requesterUser.getIsDeleted() != 0) {
            return RestBean.failure(403, "目标用户不可用");
        }

        UserIdUtil.OrderedUserIds ordered = UserIdUtil.orderUserIds(currentUserId, requesterUserId);
        String userOneId = ordered.userOneId();
        String userTwoId = ordered.userTwoId();

        String acceptedRelationshipId = userRelationshipsMapper.checkNormalFriendship(userOneId, userTwoId);
        if (acceptedRelationshipId != null) {
            return RestBean.success("双方已经是好友");
        }

        UserRelationships existing = userRelationshipsMapper.findRelationship(userOneId, userTwoId);
        if (existing == null) {
            return RestBean.failure(404, "好友申请不存在");
        }
        if (Const.FRIEND_RELATIONSHIP_STATUS_BLOCKED.equals(existing.getStatus())) {
            if (currentUserId.equals(existing.getActionUserId())) {
                return RestBean.failure(403, "你已将对方拉黑，无法确认好友申请");
            }
            return RestBean.failure(403, "对方已将你拉黑，无法确认好友申请");
        }
        if (Const.FRIEND_RELATIONSHIP_STATUS_DECLINED.equals(existing.getStatus())) {
            return RestBean.failure(403, "该好友申请已被拒绝，无法确认");
        }
        if (!Const.FRIEND_RELATIONSHIP_STATUS_PENDING.equals(existing.getStatus())) {
            return RestBean.failure(403, "当前关系状态无法确认好友申请");
        }

        if (currentUserId.equals(existing.getActionUserId())) {
            return RestBean.failure(403, "不能确认自己发起的好友申请");
        }
        if (!requesterUserId.equals(existing.getActionUserId())) {
            return RestBean.failure(403, "好友申请发起人不匹配，无法确认");
        }

        Date now = new Date();
        String cleanedRemark = null;
        if (remark != null) {
            String temp = remark.trim();
            if (!temp.isEmpty()) {
                cleanedRemark = temp;
            }
        }
        int updated;
        if (cleanedRemark == null) {
            updated = userRelationshipsMapper.updateRelationshipStatus(
                    existing.getId(),
                    Const.FRIEND_RELATIONSHIP_STATUS_ACCEPTED,
                    currentUserId,
                    now
            );
        } else {
            boolean currentIsUserOne = currentUserId.equals(userOneId);
            if (currentIsUserOne) {
                updated = userRelationshipsMapper.updateStatusAndRemarkOneById(
                        existing.getId(),
                        Const.FRIEND_RELATIONSHIP_STATUS_ACCEPTED,
                        cleanedRemark,
                        currentUserId,
                        now
                );
            } else {
                updated = userRelationshipsMapper.updateStatusAndRemarkTwoById(
                        existing.getId(),
                        Const.FRIEND_RELATIONSHIP_STATUS_ACCEPTED,
                        cleanedRemark,
                        currentUserId,
                        now
                );
            }
        }
        if (updated <= 0) {
            return RestBean.failure(500, "确认好友申请失败");
        }
        return RestBean.success("确认好友成功");
    }

    /**
     * 根据前端传入的 friend_identifier 查询好友候选人，并给出“是否允许添加”的判定结果。
     * <p>
     * 返回值：
     * - 200：允许添加好友，返回对方基础信息（ChatUserVo）。
     * - 403：不允许添加好友，返回对方基础信息（ChatUserVo）。
     * <p>
     * 业务规则：
     * 1. 先根据 friend_identifier 在 users 表中查询用户，获取候选用户信息 candidateUser；
     * 2. 如果候选用户不存在，则直接返回 null（上层可提示“用户不存在或不可添加为好友”）；
     * 3. 如果存在候选用户，先检查当前用户与候选用户是否在同一门课程中：
     * 3.1 查询当前用户已加入的课程列表；
     * 3.2 对于每一门课程，检查 candidateUser 是否也在该课程的 user_courses 记录中；
     * 3.3 若存在任意一门课程双方均加入，则允许添加好友，返回对方的基础信息；
     * 4. 如果没有共同课程，则需要进一步检查 friend_identifier 是否为任课教师的 identifier：
     * 4.1 查询当前用户所有已选课程的课程信息（courses 表，含 teacher_id）；
     * 4.2 根据每门课程的 teacher_id 从 users 表查出教师用户 teacherUser；
     * 4.3 如果 teacherUser 的 identifier 与 friend_identifier 匹配，则将该教师作为候选对象返回；
     * 5. 如果既不是同课同学，又不是任课教师，则返回 null。
     *
     * @param currentUserId    当前登录用户ID
     * @param friendIdentifier 前端输入的好友唯一标识符（学号/工号）
     * @return RestBean，data 为对方信息；code 为 200/403；message 为原因说明
     */
    public RestBean<ChatUserVo> queryFriendByIdentifier(String currentUserId, String friendIdentifier) {
        // 参数基本校验：当前用户ID 与 friend_identifier 不能为空
        if (currentUserId == null || currentUserId.isEmpty()) {
            log.warn("[queryFriendByIdentifier.method] 当前用户ID为空，无法执行好友查询");
            return RestBean.failure(400, "当前用户ID不能为空");
        }
        if (friendIdentifier == null || friendIdentifier.isEmpty()) {
            log.warn("[queryFriendByIdentifier.method] friend_identifier 为空，无法执行好友查询");
            return RestBean.failure(400, "friend_identifier不能为空");
        }

        // 步骤 1：根据 friend_identifier 在 users 表中查询候选用户
        Users candidateUser = sysUsersMapper.findUserByIdentifier(friendIdentifier);
        if (candidateUser == null) {
            log.info("[queryFriendByIdentifier.method] 未找到对应的用户，friend_identifier={}", friendIdentifier);
            return RestBean.failure(404, "未找到该用户");
        }

        // 逻辑删除/管理员用户不允许作为好友候选人
        if (candidateUser.getIsDeleted() != null && candidateUser.getIsDeleted() != 0) {
            log.info("[queryFriendByIdentifier.method] 用户已被逻辑删除，friend_identifier={}", friendIdentifier);
            return RestBean.failure(404, "未找到该用户");
        }
        if ("admin".equalsIgnoreCase(candidateUser.getRole())) {
            log.info("[queryFriendByIdentifier.method] 不允许添加管理员为好友，friend_identifier={}", friendIdentifier);
            return RestBean.failure(403, "不允许添加该用户为好友");
        }

        // 如果候选用户就是当前用户本人，则不允许添加为好友，直接返回 null
        if (candidateUser.getId().equals(currentUserId)) {
            log.info("[queryFriendByIdentifier.method] 尝试添加自己为好友，直接拒绝，currentUserId={}", currentUserId);
            return RestBean.failure(403, "不能添加自己为好友");
        }

        // 统一处理用户关系中 user_one_id < user_two_id 的约束，防止重复关系
        String userOneId;
        String userTwoId;
        if (currentUserId.compareTo(candidateUser.getId()) < 0) {
            userOneId = currentUserId;
            userTwoId = candidateUser.getId();
        } else {
            userOneId = candidateUser.getId();
            userTwoId = currentUserId;
        }

        // 在进行课程校验前，可以先快速检查是否已经是好友
        String relationshipId = userRelationshipsMapper.checkNormalFriendship(userOneId, userTwoId);
        if (relationshipId != null) {
            log.info("[queryFriendByIdentifier.method] 双方已经是好友，relationshipId={}", relationshipId);
            // 已经是好友也视为“允许添加/展示”，直接返回对方信息
            return RestBean.success(buildChatUserVo(candidateUser));
        }

        // 步骤 2：检查当前用户与候选用户是否存在同一门课程
        // 2.1 查询当前用户已加入的课程列表（user_courses）
        List<String> currentUserCourseIds = getCourseIdsByUserId(currentUserId);
        if (!currentUserCourseIds.isEmpty()) {
            // 2.2 遍历当前用户的课程列表，检查候选用户是否也在这些课程中
            for (String courseId : currentUserCourseIds) {
                Integer exists = userCoursesMapper.findByUserIdAndCourseId(candidateUser.getId(), courseId);
                if (exists != null && exists == 1) {
                    log.info("[queryFriendByIdentifier.method] 找到同一门课程，允许添加为好友，courseId={}", courseId);
                    return RestBean.success(buildChatUserVo(candidateUser));
                }
            }
        }

        // 步骤 3：当前用户与候选用户没有共同课程，则检查 friend_identifier 是否为任课教师
        // 逻辑：拿当前用户已选课程 ID，在 courses 表中查教师 ID，再回到 users 表比对 identifier
        if (!currentUserCourseIds.isEmpty()) {
            // 使用 Set 保证去重，避免同一教师多门课程导致重复查询
            Set<String> teacherIds = new HashSet<>();
            for (String courseId : currentUserCourseIds) {
                // 根据课程ID获取课程详情，从中读取 teacher_id
                String teacherId = getTeacherIdByCourseId(courseId);
                if (teacherId != null && !teacherId.isEmpty()) {
                    teacherIds.add(teacherId);
                }
            }

            if (!teacherIds.isEmpty()) {
                // 将教师ID集合转换为列表，便于批量查询
                List<String> teacherIdList = new ArrayList<>(teacherIds);
                for (String teacherId : teacherIdList) {
                    Users teacherUser = sysUsersMapper.findUserById(teacherId);
                    if (teacherUser == null) {
                        continue;
                    }
                    if (teacherUser.getIsDeleted() != null && teacherUser.getIsDeleted() != 0) {
                        continue;
                    }
                    // 如果教师的 identifier 与 friend_identifier 匹配，则视为有效
                    if (friendIdentifier.equals(teacherUser.getIdentifier())) {
                        log.info("[queryFriendByIdentifier.method] friend_identifier 为任课教师，teacherId={}", teacherId);
                        return RestBean.success(buildChatUserVo(teacherUser));
                    }
                }
            }
        }

        // 步骤 4：既不是同课同学，也不是任课教师，视为不允许添加好友
        log.info("[queryFriendByIdentifier.method] 未通过课程或教师校验，不允许添加好友，friend_identifier={}", friendIdentifier);
        return RestBean.failure(403, "不允许添加该用户为好友", buildChatUserVo(candidateUser));
    }

    /**
     * 获取待确认好友申请列表
     *
     * @param userId 用户ID
     * @return 待确认好友申请列表
     */
    public RestBean<List<ChatUserVo>> getPendingFriendRequestList(String userId) {
        if (userId == null || userId.isEmpty()) {
            return RestBean.failure(400, "用户ID不能为空");
        }
        List<ChatUserVo> list = userRelationshipsMapper.getPendingFriendRequestList(userId);
        if (list == null) {
            list = List.of();
        }
        return RestBean.success(list);
    }

    /**
     * 统计用户待处理好友申请数（status = PENDING）
     *
     * @param userId 用户ID
     * @return 待处理好友申请数
     */
    public RestBean<Integer> countPendingFriendRequest(String userId) {
        if (userId == null || userId.isEmpty()) {
            return RestBean.failure(400, "用户ID不能为空");
        }
        int count = userRelationshipsMapper.countPendingFriendships(userId);
        return RestBean.success(count);
    }

    /**
     * 根据用户ID查询其已加入课程的ID列表
     *
     * @param userId 用户ID
     * @return 课程ID列表，可能为空列表但不会为 null
     */
    private List<String> getCourseIdsByUserId(String userId) {
        // 直接复用 CoursesMapper 中对 user_courses 表的查询逻辑
        if (userId == null || userId.isEmpty()) {
            return List.of();
        }
        var myCourseList = coursesMapper.getMyCourseList(userId);
        if (myCourseList == null || myCourseList.isEmpty()) {
            return List.of();
        }
        return myCourseList.stream().map(userCourses -> userCourses.getCourseId()).toList();
    }


    /**
     * 根据课程ID获取课程对应的任课教师ID
     *
     * @param courseId 课程ID
     * @return 教师ID，如果课程不存在或未设置教师则返回 null
     */
    private String getTeacherIdByCourseId(String courseId) {
        if (courseId == null || courseId.isEmpty()) {
            return null;
        }
        var course = coursesMapper.getCourseById(courseId);
        return course != null ? course.getTeacherId() : null;
    }

    /**
     * 将 Users 实体转换为 ChatUserVo，供前端统一使用
     *
     * @param user 用户实体
     * @return ChatUserVo 视图对象
     */
    private ChatUserVo buildChatUserVo(Users user) {
        if (user == null) {
            return null;
        }
        ChatUserVo vo = new ChatUserVo();
        vo.setFriendId(user.getId());
        vo.setUserName(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setRole(user.getRole());
        vo.setRemark(null);
        return vo;
    }
}
