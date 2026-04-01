package com.daox.online.service.Impl;

import com.daox.online.config.WebSocketGroupChatServiceConfig;
import com.daox.online.controller.exception.BusinessException;
import com.daox.online.entity.mongodb.ChatMessage;
import com.daox.online.entity.mysql.Courses;
import com.daox.online.entity.mysql.Users;
import com.daox.online.entity.views.responseVO.chat.group.CourseGroupConversationVO;
import com.daox.online.entity.views.responseVO.chat.group.CourseGroupDetailVO;
import com.daox.online.entity.views.responseVO.chat.group.CourseGroupMemberVO;
import com.daox.online.entity.views.responseVO.chat.group.GroupMuteAllStatusVO;
import com.daox.online.entity.views.responseVO.chat.group.MutedGroupMemberVO;
import com.daox.online.mapper.CoursesMapper;
import com.daox.online.mapper.SysUsersMapper;
import com.daox.online.mapper.UserCoursesMapper;
import com.daox.online.mapper.UserMapper;
import com.daox.online.repository.mongodb.ChatMessageRepository;
import com.daox.online.service.CourseGroupChatService;
import com.daox.online.service.UsersService;
import com.daox.online.uilts.JSONUtil;
import com.daox.online.uilts.RedisKeyHelper;
import com.daox.online.uilts.constant.Const;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 课程群聊服务实现类。
 * <p>
 * 该实现基于现有课程、选课和用户表完成群聊权限校验，并将群消息统一落库到 MongoDB。
 * 教师禁言状态采用“Redis Set 索引 + 成员级 TTL Key”的方式实现，既保证了枚举效率，
 * 又保留了每个学生单独的自动解禁能力。
 * </p>
 */
@Slf4j
@Service
public class CourseGroupChatServiceImpl implements CourseGroupChatService {

    private static final int DEFAULT_HISTORY_LIMIT = 50;
    private static final int MAX_HISTORY_LIMIT = 100;
    private static final List<ChatMessage.MessageType> GROUP_VISIBLE_MESSAGE_TYPES = List.of(
            ChatMessage.MessageType.GROUP,
            ChatMessage.MessageType.SYSTEM
    );
    private static final String ANNOUNCEMENT_PREFIX = "【课程公告】";

    @Resource
    private CoursesMapper coursesMapper;

    @Resource
    private UserCoursesMapper userCoursesMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private SysUsersMapper sysUsersMapper;

    @Resource
    private UsersService usersService;

    @Resource
    private ChatMessageRepository chatMessageRepository;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取当前用户可访问的课程群聊列表。
     *
     * @param userId 当前用户ID
     * @return 群聊摘要列表
     */
    @Override
    public List<CourseGroupConversationVO> listAccessibleGroups(String userId) {
        List<Courses> accessibleCourses = loadAccessibleCourses(userId);
        if (accessibleCourses.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Users> teacherMap = loadSimpleUsersByIds(accessibleCourses.stream()
                .map(Courses::getTeacherId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList());

        List<CourseGroupConversationVO> conversations = accessibleCourses.stream()
                .map(course -> buildConversationSummary(userId, course, teacherMap.get(course.getTeacherId())))
                .sorted(Comparator.comparing(CourseGroupConversationVO::getLastMessageTime,
                                Comparator.nullsLast(LocalDateTime::compareTo))
                        .reversed()
                        .thenComparing(CourseGroupConversationVO::getCourseTitle, Comparator.nullsLast(String::compareTo)))
                .toList();

        log.info("[CourseGroupChatService.listAccessibleGroups] 用户 {} 共加载 {} 个课程群聊", userId, conversations.size());
        return conversations;
    }

    /**
     * 获取课程群聊详情。
     *
     * @param userId   当前用户ID
     * @param courseId 课程ID
     * @return 群聊详情
     */
    @Override
    public CourseGroupDetailVO getGroupDetail(String userId, String courseId) {
        Courses course = requireCourseChatAccess(userId, courseId);
        Users teacher = sysUsersMapper.findUserById(course.getTeacherId());
        GroupMuteAllStatusVO allMuteStatus = buildMuteAllStatus(courseId);

        return new CourseGroupDetailVO()
                .setCourseId(course.getId())
                .setConversationId(course.getId())
                .setCourseTitle(course.getTitle())
                .setCourseDescription(course.getDescription())
                .setCoverImageUrl(course.getCoverImageUrl())
                .setCourseStatus(course.getStatus())
                .setTeacherId(course.getTeacherId())
                .setTeacherName(resolveUserName(teacher, course.getTeacherId()))
                .setTeacherAvatarUrl(teacher == null ? null : teacher.getAvatarUrl())
                .setMemberCount(countGroupMembers(course))
                .setCurrentUserRole(resolveCurrentUserRole(course, userId))
                .setCurrentUserMuted(isUserMuted(courseId, userId))
                .setGroupAllMuted(Boolean.TRUE.equals(allMuteStatus.getEnabled()))
                .setGroupAllMutedExpireAt(allMuteStatus.getExpireAt())
                .setCurrentUserTeacherOwner(Objects.equals(course.getTeacherId(), userId));
    }

    /**
     * 获取课程群聊历史消息。
     *
     * @param userId     当前用户ID
     * @param courseId   课程ID
     * @param beforeTime 查询时间上界，可为空
     * @param limit      查询条数
     * @return 群聊历史消息
     */
    @Override
    public List<ChatMessage> getGroupHistory(String userId, String courseId, LocalDateTime beforeTime, Integer limit) {
        requireCourseChatAccess(userId, courseId);
        Pageable pageable = PageRequest.of(0, normalizeHistoryLimit(limit));

        if (beforeTime != null) {
            return chatMessageRepository.findByConversationIdAndMessageTypeInAndTimestampBeforeOrderByTimestampDesc(
                    courseId,
                    GROUP_VISIBLE_MESSAGE_TYPES,
                    beforeTime,
                    pageable
            );
        }

        return chatMessageRepository.findByConversationIdAndMessageTypeInOrderByTimestampDesc(
                courseId,
                GROUP_VISIBLE_MESSAGE_TYPES,
                pageable
        );
    }

    /**
     * 获取课程群聊成员列表。
     *
     * @param userId   当前用户ID
     * @param courseId 课程ID
     * @return 成员列表
     */
    @Override
    public List<CourseGroupMemberVO> getGroupMembers(String userId, String courseId) {
        Courses course = requireCourseChatAccess(userId, courseId);
        List<CourseGroupMemberVO> members = new ArrayList<>();

        Users teacher = sysUsersMapper.findUserById(course.getTeacherId());
        members.add(new CourseGroupMemberVO()
                .setUserId(course.getTeacherId())
                .setUserName(resolveUserName(teacher, course.getTeacherId()))
                .setAvatarUrl(teacher == null ? null : teacher.getAvatarUrl())
                .setRole(Const.ROLE_TEACHERS)
                .setTeacherOwner(true)
                .setMuted(false));

        List<String> studentIds = loadActiveStudentIds(courseId);
        if (studentIds.isEmpty()) {
            return members;
        }

        Map<String, Users> studentMap = loadSimpleUsersByIds(studentIds);
        List<CourseGroupMemberVO> studentMembers = studentIds.stream()
                .filter(studentId -> !Objects.equals(studentId, course.getTeacherId()))
                .distinct()
                .map(studentId -> {
                    Users student = studentMap.get(studentId);
                    return new CourseGroupMemberVO()
                            .setUserId(studentId)
                            .setUserName(resolveUserName(student, studentId))
                            .setAvatarUrl(student == null ? null : student.getAvatarUrl())
                            .setRole(Const.ROLE_STUDENTS)
                            .setTeacherOwner(false)
                            .setMuted(isUserMuted(courseId, studentId));
                })
                .sorted(Comparator.comparing(CourseGroupMemberVO::getUserName, Comparator.nullsLast(String::compareTo)))
                .toList();

        members.addAll(studentMembers);
        return members;
    }

    /**
     * 将当前用户在指定课程群中的未读消息标记为已读。
     *
     * @param userId   当前用户ID
     * @param courseId 课程ID
     */
    @Override
    public void markGroupAsRead(String userId, String courseId) {
        requireCourseChatAccess(userId, courseId);
        stringRedisTemplate.delete(RedisKeyHelper.getCourseGroupUnreadKey(courseId, userId));
        log.info("[CourseGroupChatService.markGroupAsRead] 用户 {} 已清空课程 {} 群聊未读数", userId, courseId);
    }

    /**
     * 获取当前课程群聊的禁言名单。
     *
     * @param teacherId 教师ID
     * @param courseId  课程ID
     * @return 禁言名单
     */
    @Override
    public List<MutedGroupMemberVO> getMutedMembers(String teacherId, String courseId) {
        requireTeacherOwnedCourse(teacherId, courseId);

        String indexKey = RedisKeyHelper.getCourseGroupMuteIndexKey(courseId);
        Set<String> mutedUserIds = stringRedisTemplate.opsForSet().members(indexKey);
        if (mutedUserIds == null || mutedUserIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> activeStudentIds = Set.copyOf(loadActiveStudentIds(courseId));
        List<String> activeMutedUserIds = mutedUserIds.stream()
            .filter(activeStudentIds::contains)
            .filter(userId -> isUserMuted(courseId, userId))
                .distinct()
                .toList();

        mutedUserIds.stream()
                .filter(userId -> !activeMutedUserIds.contains(userId))
                .forEach(staleUserId -> stringRedisTemplate.opsForSet().remove(indexKey, staleUserId));

        Map<String, Users> userMap = loadSimpleUsersByIds(activeMutedUserIds);
        LocalDateTime now = LocalDateTime.now();
        List<MutedGroupMemberVO> result = activeMutedUserIds.stream()
                .map(userId -> {
                    Long remainingSeconds = stringRedisTemplate.getExpire(
                            RedisKeyHelper.getCourseGroupMuteMemberKey(courseId, userId),
                            TimeUnit.SECONDS
                    );
                    Users user = userMap.get(userId);
                    return new MutedGroupMemberVO()
                            .setUserId(userId)
                            .setUserName(resolveUserName(user, userId))
                            .setAvatarUrl(user == null ? null : user.getAvatarUrl())
                            .setRole(Const.ROLE_STUDENTS)
                            .setRemainingSeconds(remainingSeconds == null ? 0L : Math.max(remainingSeconds, 0L))
                            .setExpireAt(remainingSeconds == null || remainingSeconds <= 0 ? now : now.plusSeconds(remainingSeconds));
                })
                .sorted(Comparator.comparing(MutedGroupMemberVO::getExpireAt, Comparator.nullsLast(LocalDateTime::compareTo)))
                .toList();

        cleanupMuteIndexIfEmpty(indexKey);
        return result;
    }

    /**
     * 获取当前课程群聊的全员禁言状态。
     *
     * @param userId   当前用户ID
     * @param courseId 课程ID
     * @return 全员禁言状态
     */
    @Override
    public GroupMuteAllStatusVO getMuteAllStatus(String userId, String courseId) {
        requireCourseChatAccess(userId, courseId);
        return buildMuteAllStatus(courseId);
    }

    /**
     * 教师对课程群成员执行禁言。
     *
     * @param teacherId       教师ID
     * @param courseId        课程ID
     * @param targetUserId    被禁言学生ID
     * @param durationMinutes 禁言分钟数
     */
    @Override
    public void muteMember(String teacherId, String courseId, String targetUserId, Long durationMinutes) {
        Courses course = requireTeacherOwnedCourse(teacherId, courseId);
        validateMuteOperation(course, teacherId, targetUserId, durationMinutes);

        String muteIndexKey = RedisKeyHelper.getCourseGroupMuteIndexKey(courseId);
        String muteMemberKey = RedisKeyHelper.getCourseGroupMuteMemberKey(courseId, targetUserId);
        long ttlSeconds = TimeUnit.MINUTES.toSeconds(durationMinutes);

        stringRedisTemplate.opsForValue().set(muteMemberKey, teacherId, durationMinutes, TimeUnit.MINUTES);
        stringRedisTemplate.opsForSet().add(muteIndexKey, targetUserId);

        Long currentIndexExpire = stringRedisTemplate.getExpire(muteIndexKey, TimeUnit.SECONDS);
        if (currentIndexExpire == null || currentIndexExpire < ttlSeconds) {
            stringRedisTemplate.expire(muteIndexKey, ttlSeconds, TimeUnit.SECONDS);
        }

        log.info("[CourseGroupChatService.muteMember] 教师 {} 已在课程 {} 中禁言学生 {}，时长 {} 分钟",
                teacherId, courseId, targetUserId, durationMinutes);
    }

    /**
     * 教师对课程群执行全员禁言。
     *
     * @param teacherId       教师ID
     * @param courseId        课程ID
     * @param durationMinutes 全员禁言分钟数
     */
    @Override
    public void muteAllMembers(String teacherId, String courseId, Long durationMinutes) {
        requireTeacherOwnedCourse(teacherId, courseId);
        long normalizedDuration = normalizeMuteDuration(durationMinutes, "全员禁言时长必须大于0分钟");
        stringRedisTemplate.opsForValue().set(
                RedisKeyHelper.getCourseGroupAllMuteKey(courseId),
                teacherId,
                normalizedDuration,
                TimeUnit.MINUTES
        );
        log.info("[CourseGroupChatService.muteAllMembers] 教师 {} 已在课程 {} 中开启全员禁言，时长 {} 分钟",
                teacherId, courseId, normalizedDuration);
    }

    /**
     * 教师解除课程群成员禁言。
     *
     * @param teacherId    教师ID
     * @param courseId     课程ID
     * @param targetUserId 被解除禁言学生ID
     */
    @Override
    public void unmuteMember(String teacherId, String courseId, String targetUserId) {
        requireTeacherOwnedCourse(teacherId, courseId);
        if (!StringUtils.hasText(targetUserId)) {
            throw new BusinessException("400", "被解除禁言学生ID不能为空");
        }

        clearMemberMuteState(courseId, targetUserId);
        String muteIndexKey = RedisKeyHelper.getCourseGroupMuteIndexKey(courseId);
        cleanupMuteIndexIfEmpty(muteIndexKey);

        log.info("[CourseGroupChatService.unmuteMember] 教师 {} 已在课程 {} 中解除学生 {} 的禁言",
                teacherId, courseId, targetUserId);
    }

    /**
     * 教师解除课程群全员禁言。
     *
     * @param teacherId 教师ID
     * @param courseId  课程ID
     */
    @Override
    public void unmuteAllMembers(String teacherId, String courseId) {
        requireTeacherOwnedCourse(teacherId, courseId);
        stringRedisTemplate.delete(RedisKeyHelper.getCourseGroupAllMuteKey(courseId));
        log.info("[CourseGroupChatService.unmuteAllMembers] 教师 {} 已在课程 {} 中解除全员禁言", teacherId, courseId);
    }

    /**
     * 教师将课程群成员移出群聊。
     *
     * @param teacherId    教师ID
     * @param courseId     课程ID
     * @param targetUserId 被移出学生ID
     */
    @Override
    public void removeMember(String teacherId, String courseId, String targetUserId) {
        Courses course = requireTeacherOwnedCourse(teacherId, courseId);
        validateRemoveMemberOperation(course, teacherId, targetUserId);

        stringRedisTemplate.opsForSet().add(RedisKeyHelper.getCourseGroupRemovedMembersKey(courseId), targetUserId);
        clearMemberMuteState(courseId, targetUserId);
        stringRedisTemplate.delete(RedisKeyHelper.getCourseGroupUnreadKey(courseId, targetUserId));
        WebSocketGroupChatServiceConfig.disconnectCourseUser(courseId, targetUserId, "您已被教师移出该课程群聊");

        log.info("[CourseGroupChatService.removeMember] 教师 {} 已将学生 {} 移出课程 {} 群聊",
                teacherId, targetUserId, courseId);
    }

    /**
     * 教师发布课程群公告消息。
     *
     * @param teacherId 教师ID
     * @param courseId  课程ID
     * @param title     公告标题
     * @param content   公告内容
     * @return 落库后的公告消息
     */
    @Override
    public ChatMessage publishAnnouncement(String teacherId, String courseId, String title, String content) {
        requireTeacherOwnedCourse(teacherId, courseId);

        ChatMessage message = new ChatMessage();
        message.setConversationId(courseId);
        message.setGroupId(courseId);
        message.setSenderId(teacherId);
        message.setReceiverId(null);
        message.setMessageType(ChatMessage.MessageType.SYSTEM);
        message.setContentType(ChatMessage.ContentType.TEXT);
        message.setContent(buildAnnouncementContent(title, content));
        message.setStatus(ChatMessage.MessageStatus.SENT);
        message.setTag("teacher");
        message.setTimestamp(LocalDateTime.now());

        ChatMessage savedMessage = chatMessageRepository.save(message);
        increaseUnreadCountForRecipients(course, teacherId);
        WebSocketGroupChatServiceConfig.broadcastMessageToCourse(courseId, JSONUtil.toJson(savedMessage));
        log.info("[CourseGroupChatService.publishAnnouncement] 教师 {} 已向课程 {} 群发布公告", teacherId, courseId);
        return savedMessage;
    }

    /**
     * 保存课程群聊消息。
     *
     * @param courseId             课程ID
     * @param requestMessage       客户端消息载荷
     * @param authenticatedUserId 经过握手认证的用户ID
     * @return 保存后的消息实体
     */
    @Override
    public ChatMessage saveGroupMessage(String courseId, ChatMessage requestMessage, String authenticatedUserId) {
        Courses course = requireCourseChatAccess(authenticatedUserId, courseId);
        validateGroupMessage(requestMessage, authenticatedUserId);

        boolean teacherOwner = Objects.equals(course.getTeacherId(), authenticatedUserId);
        if (!teacherOwner && isGroupAllMuted(courseId)) {
            throw new BusinessException("403", "当前课程群已开启全员禁言，暂时无法发送群消息");
        }
        if (!teacherOwner && isUserMuted(courseId, authenticatedUserId)) {
            throw new BusinessException("403", "当前已被教师禁言，暂时无法发送群消息");
        }

        ChatMessage message = requestMessage == null ? new ChatMessage() : requestMessage;
        message.setId(null);
        message.setConversationId(courseId);
        message.setGroupId(courseId);
        message.setSenderId(authenticatedUserId);
        message.setReceiverId(null);
        message.setMessageType(ChatMessage.MessageType.GROUP);
        message.setContentType(message.getContentType() == null ? ChatMessage.ContentType.TEXT : message.getContentType());
        message.setStatus(message.getStatus() == null ? ChatMessage.MessageStatus.SENT : message.getStatus());
        message.setTag(teacherOwner ? "teacher" : "student");
        message.setTimestamp(LocalDateTime.now());

        ChatMessage savedMessage = chatMessageRepository.save(message);
        increaseUnreadCountForRecipients(course, authenticatedUserId);
        log.info("[CourseGroupChatService.saveGroupMessage] 用户 {} 在课程 {} 群聊中发送消息成功", authenticatedUserId, courseId);
        return savedMessage;
    }

    /**
     * 判断用户是否允许连接课程群聊 WebSocket。
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     * @return true 表示允许连接
     */
    @Override
    public boolean canJoinGroupChat(String courseId, String userId) {
        try {
            requireCourseChatAccess(userId, courseId);
            return true;
        } catch (BusinessException exception) {
            log.warn("[CourseGroupChatService.canJoinGroupChat] 用户 {} 无法接入课程 {} 群聊，原因: {}",
                    userId, courseId, exception.getMessage());
            return false;
        }
    }

    /**
     * 构建群聊摘要对象。
     *
     * @param userId  当前用户ID
     * @param course  课程实体
     * @param teacher 教师信息
     * @return 群聊摘要
     */
    private CourseGroupConversationVO buildConversationSummary(String userId, Courses course, Users teacher) {
        ChatMessage lastMessage = chatMessageRepository.findFirstByConversationIdAndMessageTypeInOrderByTimestampDesc(
                course.getId(),
                GROUP_VISIBLE_MESSAGE_TYPES
        );

        return new CourseGroupConversationVO()
                .setCourseId(course.getId())
                .setConversationId(course.getId())
                .setCourseTitle(course.getTitle())
                .setCoverImageUrl(course.getCoverImageUrl())
                .setTeacherId(course.getTeacherId())
                .setTeacherName(resolveUserName(teacher, course.getTeacherId()))
                .setTeacherAvatarUrl(teacher == null ? null : teacher.getAvatarUrl())
                .setMemberCount(countGroupMembers(course))
                .setCurrentUserRole(resolveCurrentUserRole(course, userId))
                .setCurrentUserMuted(isUserMuted(course.getId(), userId))
                .setGroupAllMuted(isGroupAllMuted(course.getId()))
                .setUnreadCount(getUnreadCount(course.getId(), userId))
                .setLastMessageContent(lastMessage == null ? null : lastMessage.getContent())
                .setLastMessageSenderId(lastMessage == null ? null : lastMessage.getSenderId())
                .setLastMessageTag(lastMessage == null ? null : lastMessage.getTag())
                .setLastMessageTime(lastMessage == null ? null : lastMessage.getTimestamp());
    }

    /**
     * 加载当前用户可访问的课程列表。
     *
     * @param userId 当前用户ID
     * @return 课程列表
     */
    private List<Courses> loadAccessibleCourses(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("401", "用户未认证");
        }

        if (usersService.isTeacher(userId)) {
            return coursesMapper.getTeacherCourseList(userId).stream()
                    .filter(this::isCourseAvailable)
                    .toList();
        }

        if (usersService.isStudent(userId)) {
            List<String> courseIds = userCoursesMapper.findCourseIdsByUserId(userId);
            if (courseIds == null || courseIds.isEmpty()) {
                return Collections.emptyList();
            }
            return courseIds.stream()
                    .filter(courseId -> !isUserRemoved(courseId, userId))
                    .map(coursesMapper::getCourseById)
                    .filter(Objects::nonNull)
                    .filter(this::isCourseAvailable)
                    .collect(Collectors.toMap(Courses::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new))
                    .values()
                    .stream()
                    .toList();
        }

        throw new BusinessException("403", "当前角色不支持课程群聊功能");
    }

    /**
     * 校验用户对指定课程群聊的访问权限。
     *
     * @param userId   当前用户ID
     * @param courseId 课程ID
     * @return 课程实体
     */
    private Courses requireCourseChatAccess(String userId, String courseId) {
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("401", "用户未认证");
        }
        if (!StringUtils.hasText(courseId)) {
            throw new BusinessException("400", "课程ID不能为空");
        }

        Courses course = coursesMapper.getCourseById(courseId);
        if (!isCourseAvailable(course)) {
            throw new BusinessException("404", "课程不存在或已删除");
        }

        boolean teacherOwner = Objects.equals(course.getTeacherId(), userId);
        boolean enrolledStudent = userCoursesMapper.findByUserIdAndCourseId(userId, courseId) != null;
        if (!teacherOwner && (!enrolledStudent || isUserRemoved(courseId, userId))) {
            throw new BusinessException("403", "当前用户不在该课程群聊中");
        }

        return course;
    }

    /**
     * 校验教师是否为当前课程群主。
     *
     * @param teacherId 教师ID
     * @param courseId  课程ID
     * @return 课程实体
     */
    private Courses requireTeacherOwnedCourse(String teacherId, String courseId) {
        Courses course = requireCourseChatAccess(teacherId, courseId);
        if (!Objects.equals(course.getTeacherId(), teacherId)) {
            throw new BusinessException("403", "只有课程教师才能管理群聊禁言");
        }
        return course;
    }

    /**
     * 校验课程是否可用于群聊。
     *
     * @param course 课程实体
     * @return true 表示课程有效
     */
    private boolean isCourseAvailable(Courses course) {
        return course != null && !Objects.equals(course.getIsDeleted(), Const.LOGICAL_JUDGMENT_FALSE);
    }

    /**
     * 统计当前课程群有效成员总数。
     *
     * @param course 课程实体
     * @return 成员总数（教师 + 当前有效学生）
     */
    private long countGroupMembers(Courses course) {
        return loadActiveStudentIds(course.getId()).stream()
                .filter(studentId -> !Objects.equals(studentId, course.getTeacherId()))
                .count() + 1;
    }

    /**
     * 解析当前用户在课程群聊中的角色。
     *
     * @param course  课程实体
     * @param userId  当前用户ID
     * @return 角色值 teacher / student
     */
    private String resolveCurrentUserRole(Courses course, String userId) {
        return Objects.equals(course.getTeacherId(), userId) ? Const.ROLE_TEACHERS : Const.ROLE_STUDENTS;
    }

    /**
     * 校验教师禁言操作的合法性。
     *
     * @param course           课程实体
     * @param teacherId        教师ID
     * @param targetUserId     被禁言用户ID
     * @param durationMinutes  禁言时长（分钟）
     */
    private void validateMuteOperation(Courses course, String teacherId, String targetUserId, Long durationMinutes) {
        if (!StringUtils.hasText(targetUserId)) {
            throw new BusinessException("400", "被禁言学生ID不能为空");
        }
        if (Objects.equals(teacherId, targetUserId)) {
            throw new BusinessException("400", "教师不能禁言自己");
        }
        normalizeMuteDuration(durationMinutes, "禁言时长必须大于0分钟");
        if (!usersService.isStudent(targetUserId)) {
            throw new BusinessException("400", "当前仅支持禁言课程内学生成员");
        }
        if (userCoursesMapper.findByUserIdAndCourseId(targetUserId, course.getId()) == null || isUserRemoved(course.getId(), targetUserId)) {
            throw new BusinessException("403", "目标用户不在当前课程群聊中");
        }
    }

    /**
     * 校验移出群成员操作是否合法。
     *
     * @param course        课程实体
     * @param teacherId     教师ID
     * @param targetUserId  目标用户ID
     */
    private void validateRemoveMemberOperation(Courses course, String teacherId, String targetUserId) {
        if (!StringUtils.hasText(targetUserId)) {
            throw new BusinessException("400", "被移出学生ID不能为空");
        }
        if (Objects.equals(teacherId, targetUserId) || Objects.equals(course.getTeacherId(), targetUserId)) {
            throw new BusinessException("400", "不能将课程教师移出群聊");
        }
        if (!usersService.isStudent(targetUserId)) {
            throw new BusinessException("400", "当前仅支持移出课程内学生成员");
        }
        if (userCoursesMapper.findByUserIdAndCourseId(targetUserId, course.getId()) == null) {
            throw new BusinessException("403", "目标用户不在当前课程群聊中");
        }
        if (isUserRemoved(course.getId(), targetUserId)) {
            throw new BusinessException("409", "目标用户已被移出该课程群聊");
        }
    }

    /**
     * 校验群聊消息内容。
     *
     * @param requestMessage      客户端消息载荷
     * @param authenticatedUserId 认证后的发送人ID
     */
    private void validateGroupMessage(ChatMessage requestMessage, String authenticatedUserId) {
        if (!StringUtils.hasText(authenticatedUserId)) {
            throw new BusinessException("401", "群聊连接未认证，无法发送消息");
        }
        if (requestMessage == null) {
            throw new BusinessException("400", "消息体不能为空");
        }
        if (!StringUtils.hasText(requestMessage.getContent())) {
            throw new BusinessException("400", "消息内容不能为空");
        }
    }

    /**
     * 判断用户当前是否被课程群聊禁言。
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     * @return true 表示已被禁言
     */
    private boolean isUserMuted(String courseId, String userId) {
        if (!StringUtils.hasText(courseId) || !StringUtils.hasText(userId)) {
            return false;
        }
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(
                RedisKeyHelper.getCourseGroupMuteMemberKey(courseId, userId)
        ));
    }

    /**
     * 判断课程群是否开启全员禁言。
     *
     * @param courseId 课程ID
     * @return true 表示开启全员禁言
     */
    private boolean isGroupAllMuted(String courseId) {
        if (!StringUtils.hasText(courseId)) {
            return false;
        }
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(RedisKeyHelper.getCourseGroupAllMuteKey(courseId)));
    }

    /**
     * 判断用户是否已被移出课程群聊。
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     * @return true 表示已被移出
     */
    private boolean isUserRemoved(String courseId, String userId) {
        if (!StringUtils.hasText(courseId) || !StringUtils.hasText(userId)) {
            return false;
        }
        Boolean removed = stringRedisTemplate.opsForSet().isMember(
                RedisKeyHelper.getCourseGroupRemovedMembersKey(courseId),
                userId
        );
        return Boolean.TRUE.equals(removed);
    }

    /**
     * 获取课程群当前有效学生成员列表。
     *
     * @param courseId 课程ID
     * @return 有效学生ID列表
     */
    private List<String> loadActiveStudentIds(String courseId) {
        List<String> studentIds = userCoursesMapper.findUserIdsByCourseId(courseId);
        if (studentIds == null || studentIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> removedMemberIds = getRemovedMemberIds(courseId);
        return studentIds.stream()
                .filter(StringUtils::hasText)
                .filter(studentId -> !removedMemberIds.contains(studentId))
                .distinct()
                .toList();
    }

    /**
     * 获取课程群被移出成员集合。
     *
     * @param courseId 课程ID
     * @return 被移出成员ID集合
     */
    private Set<String> getRemovedMemberIds(String courseId) {
        Set<String> removedMemberIds = stringRedisTemplate.opsForSet().members(
                RedisKeyHelper.getCourseGroupRemovedMembersKey(courseId)
        );
        return removedMemberIds == null ? Collections.emptySet() : removedMemberIds;
    }

    /**
     * 构建课程群全员禁言状态视图对象。
     *
     * @param courseId 课程ID
     * @return 全员禁言状态
     */
    private GroupMuteAllStatusVO buildMuteAllStatus(String courseId) {
        String key = RedisKeyHelper.getCourseGroupAllMuteKey(courseId);
        Long remainingSeconds = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        boolean enabled = remainingSeconds != null && remainingSeconds > 0;
        return new GroupMuteAllStatusVO()
                .setEnabled(enabled)
                .setRemainingSeconds(enabled ? remainingSeconds : 0L)
                .setExpireAt(enabled ? LocalDateTime.now().plusSeconds(remainingSeconds) : null);
    }

    /**
     * 清理指定成员的单人禁言状态。
     *
     * @param courseId     课程ID
     * @param targetUserId 目标用户ID
     */
    private void clearMemberMuteState(String courseId, String targetUserId) {
        stringRedisTemplate.delete(RedisKeyHelper.getCourseGroupMuteMemberKey(courseId, targetUserId));
        stringRedisTemplate.opsForSet().remove(RedisKeyHelper.getCourseGroupMuteIndexKey(courseId), targetUserId);
    }

    /**
     * 获取指定用户在课程群中的未读消息数。
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     * @return 未读消息数
     */
    private int getUnreadCount(String courseId, String userId) {
        if (!StringUtils.hasText(courseId) || !StringUtils.hasText(userId)) {
            return 0;
        }

        String unreadValue = stringRedisTemplate.opsForValue().get(RedisKeyHelper.getCourseGroupUnreadKey(courseId, userId));
        if (!StringUtils.hasText(unreadValue)) {
            return 0;
        }

        try {
            return Math.max(Integer.parseInt(unreadValue), 0);
        } catch (NumberFormatException exception) {
            log.warn("[CourseGroupChatService.getUnreadCount] 课程 {} 用户 {} 的未读计数格式异常: {}", courseId, userId, unreadValue);
            return 0;
        }
    }

    /**
     * 在课程群消息落库后，递增其他成员的未读数。
     * <p>
     * 当前已经在线连接到该课程群的成员视为正在阅读，不再重复累计未读。
     * </p>
     *
     * @param course    课程实体
     * @param senderId  发送者ID
     */
    private void increaseUnreadCountForRecipients(Courses course, String senderId) {
        if (course == null || !StringUtils.hasText(senderId)) {
            return;
        }

        List<String> recipients = new ArrayList<>();
        if (StringUtils.hasText(course.getTeacherId())) {
            recipients.add(course.getTeacherId());
        }
        recipients.addAll(loadActiveStudentIds(course.getId()));

        recipients.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .filter(userId -> !Objects.equals(userId, senderId))
                .filter(userId -> !WebSocketGroupChatServiceConfig.isCourseUserConnected(course.getId(), userId))
                .forEach(userId -> stringRedisTemplate.opsForValue().increment(
                        RedisKeyHelper.getCourseGroupUnreadKey(course.getId(), userId)
                ));
    }

    /**
     * 规范化禁言时长。
     *
     * @param durationMinutes 原始分钟数
     * @param errorMessage    非法时的错误提示
     * @return 规范化后的分钟数
     */
    private long normalizeMuteDuration(Long durationMinutes, String errorMessage) {
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new BusinessException("400", errorMessage);
        }
        return durationMinutes;
    }

    /**
     * 构建教师群公告消息内容。
     *
     * @param title   公告标题
     * @param content 公告正文
     * @return 拼装后的公告文本
     */
    private String buildAnnouncementContent(String title, String content) {
        String normalizedContent = normalizeRequiredText(content, "公告内容不能为空");
        String normalizedTitle = StringUtils.hasText(title) ? title.trim() : "";
        return StringUtils.hasText(normalizedTitle)
                ? ANNOUNCEMENT_PREFIX + normalizedTitle + "\n" + normalizedContent
                : ANNOUNCEMENT_PREFIX + "\n" + normalizedContent;
    }

    /**
     * 规范化必填文本字段。
     *
     * @param value        原始文本
     * @param errorMessage 非法时的错误提示
     * @return 规范化后的文本
     */
    private String normalizeRequiredText(String value, String errorMessage) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException("400", errorMessage);
        }
        return value.trim();
    }

    /**
     * 加载简化版用户信息映射。
     *
     * @param userIds 用户ID列表
     * @return 用户信息映射
     */
    private Map<String, Users> loadSimpleUsersByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.findByIdIn(userIds).stream()
                .collect(Collectors.toMap(Users::getId, Function.identity(), (left, right) -> left));
    }

    /**
     * 解析用户昵称。
     *
     * @param user   用户信息
     * @param userId 用户ID
     * @return 昵称或兜底文案
     */
    private String resolveUserName(Users user, String userId) {
        if (user != null && StringUtils.hasText(user.getNickname())) {
            return user.getNickname();
        }
        return StringUtils.hasText(userId) ? "用户-" + userId : "未知用户";
    }

    /**
     * 规范化群聊历史查询条数。
     *
     * @param limit 原始查询条数
     * @return 规范化后的条数
     */
    private int normalizeHistoryLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_HISTORY_LIMIT;
        }
        return Math.min(limit, MAX_HISTORY_LIMIT);
    }

    /**
     * 若禁言索引集合已空，则同步删除该索引 Key。
     *
     * @param indexKey 索引 Key
     */
    private void cleanupMuteIndexIfEmpty(String indexKey) {
        Long size = stringRedisTemplate.opsForSet().size(indexKey);
        if (size == null || size <= 0) {
            stringRedisTemplate.delete(indexKey);
        }
    }
}