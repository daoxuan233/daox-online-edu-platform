<template>
  <div ref="workspaceRef" class="group-chat-page">
    <div class="ambient ambient-left"></div>
    <div class="ambient ambient-right"></div>

    <section ref="heroPanelRef" class="hero-panel clay-card">
      <div class="hero-copy">
        <span class="hero-kicker">{{ isTeacherView ? '教师端课程群聊' : '学生端课程群聊' }}</span>
        <h1 class="hero-title">{{ isTeacherView ? '课程讨论指挥台' : '课程讨论学习台' }}</h1>
        <p class="hero-description">
          {{ isTeacherView
            ? '在同一个工作台里查看课程讨论、发布公告、管理成员秩序。'
            : '把选修课程讨论集中到一个实时空间里，课程消息与公告同步可见。' }}
        </p>
      </div>

      <div class="hero-metrics">
        <article class="metric-chip">
          <span class="metric-label">可访问群聊</span>
          <strong class="metric-value">{{ groups.length }}</strong>
          <span class="metric-note">课程即频道</span>
        </article>
        <article class="metric-chip">
          <span class="metric-label">当前身份</span>
          <strong class="metric-value">{{ currentRoleLabel }}</strong>
          <span class="metric-note">{{ selectedGroup ? selectedGroup.courseTitle : '等待选择群聊' }}</span>
        </article>
        <article class="metric-chip">
          <span class="metric-label">实时连接</span>
          <strong class="metric-value">{{ wsConnected ? '已连接' : '待连接' }}</strong>
          <span class="metric-note">{{ wsConnected ? '消息会即时推送' : '选择群聊后建立连接' }}</span>
        </article>
        <article class="metric-chip accent">
          <span class="metric-label">当前状态</span>
          <strong class="metric-value">{{ currentSpeakingStatus }}</strong>
          <span class="metric-note">{{ currentSpeakingHint }}</span>
        </article>
      </div>
    </section>

    <section ref="workspaceShellRef" class="workspace-shell" :style="workspaceShellStyle">
      <aside ref="courseRailRef" class="course-rail clay-card">
        <header class="rail-header">
          <div>
            <p class="section-eyebrow">群聊总览</p>
            <h2 class="section-title">课程频道</h2>
          </div>
          <button class="ghost-button" type="button" @click="loadGroups">
            <font-awesome-icon :icon="['fas', 'rotate']" />
            <span>刷新</span>
          </button>
        </header>

        <label class="search-box" for="course-chat-search">
          <font-awesome-icon :icon="['fas', 'magnifying-glass']" />
          <input
            id="course-chat-search"
            v-model.trim="searchKeyword"
            type="text"
            placeholder="搜索课程标题或教师名称"
          />
        </label>

        <div class="course-rail-scroll" role="region" aria-label="课程频道列表" tabindex="0">
          <div v-if="groupsLoading" class="skeleton-list">
            <div v-for="index in 4" :key="`group-skeleton-${index}`" class="skeleton-card"></div>
          </div>

          <div v-else-if="groupsError" class="empty-state compact">
            <font-awesome-icon :icon="['fas', 'triangle-exclamation']" class="empty-icon" />
            <h3>课程群聊加载失败</h3>
            <p>{{ groupsError }}</p>
            <button class="primary-button" type="button" @click="loadGroups">重新加载</button>
          </div>

          <div v-else-if="!filteredGroups.length" class="empty-state compact">
            <font-awesome-icon :icon="['fas', 'layer-group']" class="empty-icon" />
            <h3>{{ searchKeyword ? '没有匹配的课程群聊' : '暂无课程群聊' }}</h3>
            <p>{{ searchKeyword ? '试试更换搜索关键词。' : '加入课程后，对应课程群聊会出现在这里。' }}</p>
          </div>

          <div v-else class="course-list">
            <article
              v-for="group in filteredGroups"
              :key="group.courseId"
              :ref="setCourseCardRef"
              class="course-card"
              :class="{ active: activeCourseId === group.courseId }"
              @click="openGroup(group.courseId)"
            >
              <div class="course-card-top">
                <div class="course-visual">
                  <img v-if="group.coverImageUrl" :src="group.coverImageUrl" :alt="group.courseTitle" />
                  <div v-else class="course-placeholder">
                    <font-awesome-icon :icon="['fas', 'book-open-reader']" />
                  </div>
                </div>

                <div class="course-content">
                  <div class="course-title-row">
                    <h3>{{ group.courseTitle }}</h3>
                    <div class="course-title-meta">
                      <span v-if="normalizeUnreadCount(group.unreadCount) > 0" class="course-unread-badge">
                        {{ formatUnreadCount(group.unreadCount) }}
                      </span>
                      <span class="time-pill">{{ formatConversationTime(group.lastMessageTime) }}</span>
                    </div>
                  </div>
                  <p class="course-teacher">{{ group.teacherName || '教师待同步' }}</p>
                  <p class="course-preview">{{ formatPreview(group.lastMessageContent) }}</p>
                </div>
              </div>

              <div class="course-card-bottom">
                <span class="status-badge soft">{{ toRoleLabel(group.currentUserRole) }}</span>
                <span v-if="group.groupAllMuted" class="status-badge warning">全员禁言</span>
                <span v-if="group.currentUserMuted" class="status-badge danger">你已被禁言</span>
                <span class="status-badge neutral">{{ group.memberCount || 0 }} 人</span>
              </div>
            </article>
          </div>
        </div>
      </aside>

      <main ref="dialogPanelRef" class="dialog-panel clay-card">
        <div v-if="!selectedGroup" class="empty-state spacious">
          <font-awesome-icon :icon="['fas', 'comments']" class="empty-icon" />
          <h3>选择一个课程群聊</h3>
          <p>左侧课程列表会根据你的身份和课程关系自动生成，点击任意课程即可进入实时讨论区。</p>
        </div>

        <template v-else>
          <header class="dialog-header">
            <div class="dialog-course-meta">
              <div class="dialog-avatar">
                <img v-if="groupDetail?.coverImageUrl" :src="groupDetail.coverImageUrl" :alt="groupDetail.courseTitle" />
                <div v-else class="course-placeholder">
                  <font-awesome-icon :icon="['fas', 'people-group']" />
                </div>
              </div>

              <div>
                <div class="dialog-title-row">
                  <h2>{{ groupDetail?.courseTitle || selectedGroup.courseTitle }}</h2>
                  <span class="status-badge soft">{{ groupDetail?.memberCount || selectedGroup.memberCount || 0 }} 人</span>
                </div>
                <p class="dialog-subtitle">
                  任课教师：{{ groupDetail?.teacherName || selectedGroup.teacherName || '教师待同步' }}
                  <span class="dialog-divider">/</span>
                  {{ toRoleLabel(groupDetail?.currentUserRole || selectedGroup.currentUserRole) }}
                </p>
              </div>
            </div>

            <div class="dialog-actions">
              <button class="ghost-button" type="button" @click="refreshActiveGroup" :disabled="detailLoading || messagesLoading || membersLoading">
                <font-awesome-icon :icon="['fas', 'arrows-rotate']" />
                <span>同步</span>
              </button>
              <button
                v-if="isTeacherView"
                class="primary-button"
                type="button"
                @click="announcementDialogVisible = true"
              >
                <font-awesome-icon :icon="['fas', 'bullhorn']" />
                <span>发布公告</span>
              </button>
            </div>
          </header>

          <div class="status-stack">
            <div v-if="socketError" class="status-banner warning-banner">
              <font-awesome-icon :icon="['fas', 'wifi']" />
              <span>{{ socketError }}</span>
            </div>
            <div v-if="groupDetail?.groupAllMuted" class="status-banner accent-banner">
              <font-awesome-icon :icon="['fas', 'volume-xmark']" />
              <span>
                当前课程群已开启全员禁言。
                <template v-if="groupAllMuteExpireAt">预计 {{ formatDateTime(groupAllMuteExpireAt) }} 自动解除。</template>
              </span>
            </div>
            <div v-if="groupDetail?.currentUserMuted" class="status-banner danger-banner">
              <font-awesome-icon :icon="['fas', 'microphone-lines-slash']" />
              <span>你当前处于教师禁言状态，暂时不能发送消息。</span>
            </div>
          </div>

          <div class="message-stream" ref="messageScrollerRef" role="region" aria-label="课程群聊消息列表" tabindex="0">
            <div class="message-toolbar">
              <button
                class="ghost-button small"
                type="button"
                @click="loadOlderMessages"
                :disabled="loadingOlderMessages || !hasMoreHistory"
              >
                <font-awesome-icon :icon="['fas', loadingOlderMessages ? 'spinner' : 'clock-rotate-left']" :spin="loadingOlderMessages" />
                <span>{{ hasMoreHistory ? '加载更早消息' : '没有更多历史消息' }}</span>
              </button>
            </div>

            <div v-if="messagesLoading" class="skeleton-messages">
              <div v-for="index in 5" :key="`message-skeleton-${index}`" class="message-skeleton"></div>
            </div>

            <div v-else-if="!renderedMessages.length" class="empty-state compact inner-empty">
              <font-awesome-icon :icon="['fas', 'comment-dots']" class="empty-icon" />
              <h3>还没有群消息</h3>
              <p>{{ isTeacherView ? '你可以先发起一条公告或普通消息，建立本课程的讨论节奏。' : '这里会同步教师公告和同学讨论消息。' }}</p>
            </div>

            <div v-else class="message-list">
              <article
                v-for="message in renderedMessages"
                :key="message.id"
                :ref="setMessageItemRef"
                class="message-row"
                :class="{
                  own: message.isOwn,
                  system: message.isAnnouncement,
                  teacher: message.senderTag === 'teacher' && !message.isAnnouncement
                }"
              >
                <template v-if="message.isAnnouncement">
                  <div class="announcement-card">
                    <div class="announcement-header">
                      <span class="status-badge accent">课程公告</span>
                      <span class="announcement-time">{{ formatDateTime(message.timestamp) }}</span>
                    </div>
                    <h3>{{ message.announcement.title }}</h3>
                    <p>{{ message.announcement.body }}</p>
                    <div class="announcement-footer">发布人：{{ message.senderName }}</div>
                  </div>
                </template>

                <template v-else>
                  <div v-if="!message.isOwn" class="message-avatar">
                    <img v-if="message.senderAvatar" :src="message.senderAvatar" :alt="message.senderName" />
                    <div v-else class="avatar-fallback">{{ message.senderName.slice(0, 1) }}</div>
                  </div>

                  <div class="message-body">
                    <div v-if="!message.isOwn" class="message-meta">
                      <span class="message-sender">{{ message.senderName }}</span>
                      <span class="message-tag" :class="message.senderTag === 'teacher' ? 'teacher-tag' : 'student-tag'">
                        {{ message.senderTag === 'teacher' ? '教师' : '学生' }}
                      </span>
                    </div>

                    <div class="message-bubble">
                      <p>{{ message.content }}</p>
                    </div>

                    <div class="message-time">{{ formatDateTime(message.timestamp) }}</div>
                  </div>
                </template>
              </article>
            </div>
          </div>

          <footer class="composer-panel">
            <label class="composer-box" for="course-chat-input">
              <textarea
                id="course-chat-input"
                v-model.trim="messageInput"
                rows="3"
                :disabled="Boolean(sendDisabledReason)"
                :placeholder="sendDisabledReason || '输入群消息，Enter 发送，Shift + Enter 换行'"
                @keydown="handleComposerKeydown"
              ></textarea>
            </label>

            <div class="composer-actions">
              <p class="composer-hint">{{ sendDisabledReason || '消息会通过 WebSocket 实时同步到当前课程成员。' }}</p>
              <button class="primary-button" type="button" :disabled="Boolean(sendDisabledReason) || !messageInput" @click="sendMessage">
                <font-awesome-icon :icon="['fas', 'paper-plane']" />
                <span>发送消息</span>
              </button>
            </div>
          </footer>
        </template>
      </main>

      <aside ref="insightPanelRef" class="insight-panel clay-card">
        <div class="insight-scroll" role="region" aria-label="频道信息与群成员" tabindex="0">
          <template v-if="!selectedGroup">
            <div class="empty-state compact inner-empty">
              <font-awesome-icon :icon="['fas', 'sparkles']" class="empty-icon" />
              <h3>等待载入课程详情</h3>
              <p>选择群聊后，这里会展示课程简介、成员结构与治理能力。</p>
            </div>
          </template>

          <template v-else>

            <section class="side-section members-section">
              <div class="side-header">
                <div>
                  <p class="section-eyebrow">成员结构</p>
                  <h3 class="section-title">群成员</h3>
                </div>
                <span class="status-badge neutral">{{ members.length }} 人</span>
              </div>

              <div v-if="membersLoading" class="skeleton-list tight">
                <div v-for="index in 4" :key="`member-skeleton-${index}`" class="skeleton-card small"></div>
              </div>

              <div v-else class="member-list">
                <article v-for="member in members" :key="member.userId" class="member-item">
                  <div class="member-profile">
                    <img v-if="member.avatarUrl" :src="member.avatarUrl" :alt="member.userName" class="member-avatar" />
                    <div v-else class="avatar-fallback member-avatar">{{ member.userName?.slice(0, 1) || 'U' }}</div>
                    <div>
                      <div class="member-name-row">
                        <strong>{{ member.userName || member.userId }}</strong>
                        <span v-if="member.teacherOwner" class="status-badge soft">群主</span>
                      </div>
                      <div class="member-meta-row">
                        <span>{{ member.role === 'teacher' ? '教师' : '学生' }}</span>
                        <span v-if="member.muted" class="status-badge danger tiny">已禁言</span>
                      </div>
                    </div>
                  </div>

                  <div v-if="isTeacherView && !member.teacherOwner" class="member-actions">
                    <button
                        v-if="!member.muted"
                        class="mini-button"
                        type="button"
                        @click="openMuteMemberDialog(member)"
                    >
                      禁言
                    </button>
                    <button
                        v-else
                        class="mini-button"
                        type="button"
                        @click="handleUnmuteMember(member)"
                    >
                      解禁
                    </button>
                    <button class="mini-button danger-outline" type="button" @click="handleKickMember(member)">
                      移出
                    </button>
                  </div>
                </article>
              </div>
            </section>

            <section class="side-section overview-section">
              <div class="side-header">
                <div>
                  <p class="section-eyebrow">课程概况</p>
                  <h3 class="section-title">频道信息</h3>
                </div>
                <span class="status-badge neutral">{{ groupDetail?.courseStatus || '运行中' }}</span>
              </div>
<!--              <p class="overview-description">{{ groupDetail?.courseDescription || '当前课程暂无简介，聊天仍可正常进行。' }}</p>-->
              <div class="overview-grid">
                <div class="overview-chip">
                  <span>任课教师</span>
                  <strong>{{ groupDetail?.teacherName || selectedGroup.teacherName || '待同步' }}</strong>
                </div>
                <div class="overview-chip">
                  <span>当前身份</span>
                  <strong>{{ toRoleLabel(groupDetail?.currentUserRole || selectedGroup.currentUserRole) }}</strong>
                </div>
                <div class="overview-chip">
                  <span>全员禁言</span>
                  <strong>{{ groupDetail?.groupAllMuted ? '开启中' : '未开启' }}</strong>
                </div>
                <div class="overview-chip">
                  <span>发言状态</span>
                  <strong>{{ currentSpeakingStatus }}</strong>
                </div>
              </div>
            </section>

            <section v-if="isTeacherView" class="side-section governance-section">
              <div class="side-header">
                <div>
                  <p class="section-eyebrow">教师治理</p>
                  <h3 class="section-title">秩序控制</h3>
                </div>
                <span class="status-badge" :class="muteAllStatus.enabled ? 'warning' : 'soft'">
                  {{ muteAllStatus.enabled ? '全员禁言中' : '讨论开放中' }}
                </span>
              </div>

              <div class="governance-actions">
                <button class="primary-button compact-button" type="button" @click="announcementDialogVisible = true">
                  <font-awesome-icon :icon="['fas', 'bullhorn']" />
                  <span>发布公告</span>
                </button>
                <button
                  v-if="!muteAllStatus.enabled"
                  class="ghost-button compact-button"
                  type="button"
                  @click="muteAllDialogVisible = true"
                >
                  <font-awesome-icon :icon="['fas', 'volume-xmark']" />
                  <span>开启全员禁言</span>
                </button>
                <button
                  v-else
                  class="ghost-button compact-button danger-outline"
                  type="button"
                  @click="handleUnmuteAll"
                >
                  <font-awesome-icon :icon="['fas', 'volume-high']" />
                  <span>解除全员禁言</span>
                </button>
              </div>

              <p class="governance-note">
                {{ muteAllStatus.enabled
                  ? `预计 ${formatDateTime(muteAllStatus.expireAt)} 自动解除，教师本人不受影响。`
                  : '当前课程群对学生开放发言，教师可在需要时临时收束讨论。'}}
              </p>
            </section>



            <section v-if="isTeacherView" class="side-section muted-section">
              <div class="side-header">
                <div>
                  <p class="section-eyebrow">禁言名单</p>
                  <h3 class="section-title">当前禁言成员</h3>
                </div>
                <span class="status-badge warning">{{ mutedMembers.length }} 人</span>
              </div>

              <div v-if="governanceLoading" class="skeleton-list tight">
                <div v-for="index in 3" :key="`muted-skeleton-${index}`" class="skeleton-card small"></div>
              </div>

              <div v-else-if="!mutedMembers.length" class="empty-state compact inner-empty muted-empty">
                <font-awesome-icon :icon="['fas', 'shield-heart']" class="empty-icon" />
                <h3>当前没有被禁言的学生</h3>
                <p>当教师对学生执行禁言后，名单会出现在这里。</p>
              </div>

              <div v-else class="muted-list">
                <article v-for="muted in mutedMembers" :key="muted.userId" class="muted-item">
                  <div>
                    <strong>{{ muted.userName || muted.userId }}</strong>
                    <p>{{ formatDuration(muted.remainingSeconds) }} 后自动解除</p>
                  </div>
                  <button class="mini-button" type="button" @click="handleUnmuteById(muted.userId, muted.userName)">
                    立即解禁
                  </button>
                </article>
              </div>
            </section>
          </template>
        </div>
      </aside>
    </section>

    <el-dialog
      v-model="muteMemberDialogVisible"
      width="460px"
      title="设置成员禁言"
      destroy-on-close
      append-to-body
      align-center
      class="course-group-dialog dialog-mute-member"
      modal-class="course-group-dialog-overlay"
      @open="handleDialogOpen('dialog-mute-member')"
    >
      <div class="dialog-body-block">
        <p class="dialog-body-title">目标成员</p>
        <p class="dialog-body-copy">{{ selectedMember?.userName || '未选择成员' }}</p>
      </div>

      <div class="dialog-body-block">
        <p class="dialog-body-title">快捷时长</p>
        <div class="quick-option-row">
          <button
            v-for="minutes in quickMuteOptions"
            :key="`mute-${minutes}`"
            type="button"
            class="quick-option"
            :class="{ active: muteForm.durationMinutes === minutes }"
            @click="muteForm.durationMinutes = minutes"
          >
            {{ formatMinuteOption(minutes) }}
          </button>
        </div>
      </div>

      <div class="dialog-body-block">
        <p class="dialog-body-title">自定义分钟数</p>
        <el-input-number v-model="muteForm.durationMinutes" :min="1" :max="10080" controls-position="right" />
      </div>

      <template #footer>
        <div class="dialog-footer-row">
          <button class="ghost-button" type="button" @click="muteMemberDialogVisible = false">取消</button>
          <button class="primary-button" type="button" @click="submitMuteMember">确认禁言</button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="muteAllDialogVisible"
      width="460px"
      title="开启全员禁言"
      destroy-on-close
      append-to-body
      align-center
      class="course-group-dialog dialog-mute-all"
      modal-class="course-group-dialog-overlay"
      @open="handleDialogOpen('dialog-mute-all')"
    >
      <div class="dialog-body-block">
        <p class="dialog-body-title">快捷时长</p>
        <div class="quick-option-row">
          <button
            v-for="minutes in quickMuteAllOptions"
            :key="`mute-all-${minutes}`"
            type="button"
            class="quick-option"
            :class="{ active: muteAllForm.durationMinutes === minutes }"
            @click="muteAllForm.durationMinutes = minutes"
          >
            {{ formatMinuteOption(minutes) }}
          </button>
        </div>
      </div>

      <div class="dialog-body-block">
        <p class="dialog-body-title">自定义分钟数</p>
        <el-input-number v-model="muteAllForm.durationMinutes" :min="1" :max="10080" controls-position="right" />
      </div>

      <template #footer>
        <div class="dialog-footer-row">
          <button class="ghost-button" type="button" @click="muteAllDialogVisible = false">取消</button>
          <button class="primary-button" type="button" @click="submitMuteAll">开启全员禁言</button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="announcementDialogVisible"
      width="560px"
      title="发布课程公告"
      destroy-on-close
      append-to-body
      align-center
      class="course-group-dialog dialog-announcement"
      modal-class="course-group-dialog-overlay"
      @open="handleDialogOpen('dialog-announcement')"
    >
      <div class="dialog-body-block">
        <p class="dialog-body-title">公告标题</p>
        <el-input v-model.trim="announcementForm.title" maxlength="100" placeholder="可选，不填时只展示公告正文" />
      </div>

      <div class="dialog-body-block">
        <p class="dialog-body-title">公告正文</p>
        <el-input
          v-model.trim="announcementForm.content"
          type="textarea"
          maxlength="2000"
          :rows="6"
          show-word-limit
          placeholder="请输入公告内容，发布后会进入群聊历史并实时广播给所有成员。"
        />
      </div>

      <template #footer>
        <div class="dialog-footer-row">
          <button class="ghost-button" type="button" @click="announcementDialogVisible = false">取消</button>
          <button class="primary-button" type="button" @click="submitAnnouncement">立即发布</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUpdate, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import gsap from 'gsap'
import { getCurrentUserId } from '@/api/index.js'
import { studentGroupChatApi, teacherGroupChatApi } from '@/api/groupChat.js'
import { createGroupChatWebSocket } from '@/utils/websocket.js'

const route = useRoute()
const router = useRouter()

const MESSAGE_PAGE_SIZE = 40
const ANNOUNCEMENT_PREFIX = '【课程公告】'
const quickMuteOptions = [15, 60, 180, 720]
const quickMuteAllOptions = [30, 120, 480, 1440]

const workspaceRef = ref(null)
const heroPanelRef = ref(null)
const workspaceShellRef = ref(null)
const courseRailRef = ref(null)
const dialogPanelRef = ref(null)
const insightPanelRef = ref(null)
const messageScrollerRef = ref(null)
const courseCardRefs = ref([])
const messageItemRefs = ref([])
const workspaceShellHeight = ref(0)

const groups = ref([])
const groupsLoading = ref(false)
const groupsError = ref('')
const searchKeyword = ref('')
const activeCourseId = ref('')
const groupDetail = ref(null)
const members = ref([])
const messages = ref([])
const messagesLoading = ref(false)
const membersLoading = ref(false)
const detailLoading = ref(false)
const governanceLoading = ref(false)
const loadingOlderMessages = ref(false)
const hasMoreHistory = ref(false)
const messageInput = ref('')
const wsConnected = ref(false)
const socketError = ref('')
const mutedMembers = ref([])
const muteAllStatus = ref({ enabled: false, remainingSeconds: 0, expireAt: null })
const muteMemberDialogVisible = ref(false)
const muteAllDialogVisible = ref(false)
const announcementDialogVisible = ref(false)
const selectedMember = ref(null)
const muteForm = ref({ durationMinutes: 15 })
const muteAllForm = ref({ durationMinutes: 30 })
const announcementForm = ref({ title: '', content: '' })

let groupSocket = null
let closeSocketSilently = false
let workspaceTimeline = null
let courseTimeline = null
let messageTimeline = null
let muteAllTimer = null
let heroPanelResizeObserver = null
let workspaceShellFrame = 0
const dialogAnimations = new Map()

const currentUserId = ref(getCurrentUserId())
const isTeacherView = computed(() => false)
const groupApi = computed(() => isTeacherView.value ? teacherGroupChatApi : studentGroupChatApi)
const workspaceShellStyle = computed(() => workspaceShellHeight.value > 0
  ? { '--workspace-shell-height': `${workspaceShellHeight.value}px` }
  : {})

const selectedGroup = computed(() => groups.value.find(group => group.courseId === activeCourseId.value) || null)

const filteredGroups = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return groups.value
  }

  return groups.value.filter(group => {
    const haystack = [group.courseTitle, group.teacherName, group.lastMessageContent]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    return haystack.includes(keyword)
  })
})

const memberDirectory = computed(() => {
  const directory = new Map()
  members.value.forEach(member => {
    directory.set(member.userId, member)
  })
  return directory
})

const currentRoleLabel = computed(() => {
  if (groupDetail.value?.currentUserRole || selectedGroup.value?.currentUserRole) {
    return toRoleLabel(groupDetail.value?.currentUserRole || selectedGroup.value?.currentUserRole)
  }
  return isTeacherView.value ? '教师' : '学生'
})

const sendDisabledReason = computed(() => {
  if (!selectedGroup.value) {
    return '请选择课程群聊'
  }
  if (!wsConnected.value) {
    return '正在建立实时连接，请稍候'
  }
  if (groupDetail.value?.currentUserMuted) {
    return '你已被教师禁言，暂时不能发言'
  }
  if (groupDetail.value?.groupAllMuted && !groupDetail.value?.currentUserTeacherOwner) {
    return '当前课程群已开启全员禁言'
  }
  return ''
})

const groupAllMuteExpireAt = computed(() => muteAllStatus.value?.expireAt || groupDetail.value?.groupAllMutedExpireAt || null)

const currentSpeakingStatus = computed(() => {
  if (!selectedGroup.value) {
    return '待加入'
  }
  if (groupDetail.value?.currentUserMuted) {
    return '已禁言'
  }
  if (groupDetail.value?.groupAllMuted && !groupDetail.value?.currentUserTeacherOwner) {
    return '全员禁言'
  }
  return '可发言'
})

const currentSpeakingHint = computed(() => {
  if (!selectedGroup.value) {
    return '选择课程群聊后显示状态'
  }
  if (groupDetail.value?.currentUserMuted) {
    return '等待教师解除禁言'
  }
  if (groupDetail.value?.groupAllMuted && !groupDetail.value?.currentUserTeacherOwner) {
    return '教师临时收束讨论中'
  }
  return '消息会实时同步到当前课程群'
})

/**
 * 计算工作台三栏区域可用高度。
 * 桌面端固定为视口内独立活动区，移动端回退为自然高度。
 */
function syncWorkspaceShellHeight() {
  if (!workspaceShellRef.value) {
    return
  }

  if (window.matchMedia('(max-width: 1080px)').matches) {
    workspaceShellHeight.value = 0
    return
  }

  const rect = workspaceShellRef.value.getBoundingClientRect()
  const viewportHeight = window.innerHeight || document.documentElement.clientHeight
  const availableHeight = Math.floor(viewportHeight - rect.top - 24)
  workspaceShellHeight.value = Math.max(620, availableHeight)
}

/**
 * 通过 requestAnimationFrame 合并高度同步，避免重复测量。
 */
function scheduleWorkspaceShellHeightSync() {
  if (workspaceShellFrame) {
    window.cancelAnimationFrame(workspaceShellFrame)
  }

  workspaceShellFrame = window.requestAnimationFrame(() => {
    workspaceShellFrame = 0
    syncWorkspaceShellHeight()
  })
}

/**
 * 为弹窗寻找首个可聚焦控件，保证弹窗打开后可以立即操作。
 *
 * @param {string} dialogClass 弹窗类名
 */
function focusDialogPrimaryField(dialogClass) {
  const dialog = document.querySelector(`.${dialogClass}`)
  if (!dialog) {
    return
  }

  const target = dialog.querySelector('.el-dialog__body textarea, .el-dialog__body input, .el-dialog__body button:not([disabled]), .el-dialog__footer button:not([disabled])')
  if (target instanceof HTMLElement) {
    target.focus({ preventScroll: true })
  }
}

/**
 * 运行弹窗开启动效，并确保弹窗定位在视口中间区域。
 *
 * @param {string} dialogClass 弹窗类名
 */
function handleDialogOpen(dialogClass) {
  nextTick(() => {
    window.requestAnimationFrame(() => {
      const dialog = document.querySelector(`.${dialogClass}`)
      const overlay = document.querySelector('.course-group-dialog-overlay')
      if (!(dialog instanceof HTMLElement)) {
        return
      }

      dialog.scrollTop = 0
      dialogAnimations.get(dialogClass)?.kill()

      if (prefersReducedMotion()) {
        gsap.set(dialog, { opacity: 1, y: 0, scale: 1, clearProps: 'transform' })
        if (overlay instanceof HTMLElement) {
          gsap.set(overlay, { opacity: 1 })
        }
        focusDialogPrimaryField(dialogClass)
        return
      }

      if (overlay instanceof HTMLElement) {
        gsap.fromTo(overlay,
          { opacity: 0 },
          { opacity: 1, duration: 0.22, ease: 'power1.out' }
        )
      }

      const tween = gsap.fromTo(dialog,
        { opacity: 0, y: 28, scale: 0.96, transformOrigin: '50% 50%' },
        {
          opacity: 1,
          y: 0,
          scale: 1,
          duration: 0.34,
          ease: 'power2.out',
          onComplete: () => {
            focusDialogPrimaryField(dialogClass)
          }
        }
      )

      dialogAnimations.set(dialogClass, tween)
    })
  })
}

const renderedMessages = computed(() => messages.value.map(message => {
  const sender = memberDirectory.value.get(message.senderId)
  const announcement = parseAnnouncement(message)
  return {
    ...message,
    senderName: sender?.userName || (message.tag === 'teacher' ? (groupDetail.value?.teacherName || '任课教师') : (message.senderId || '群成员')),
    senderAvatar: sender?.avatarUrl || (message.senderId === groupDetail.value?.teacherId ? groupDetail.value?.teacherAvatarUrl : null),
    senderTag: message.tag || sender?.role || 'student',
    isOwn: message.senderId === currentUserId.value,
    isAnnouncement: Boolean(announcement),
    announcement
  }
}))

/**
 * Vue 在更新前重置 ref 数组，便于 GSAP 重新采集节点。
 */
onBeforeUpdate(() => {
  courseCardRefs.value = []
  messageItemRefs.value = []
})

/**
 * 记录课程卡片节点。
 *
 * @param {HTMLElement|null} element DOM节点
 */
const setCourseCardRef = (element) => {
  if (element) {
    courseCardRefs.value.push(element)
  }
}

/**
 * 记录消息节点。
 *
 * @param {HTMLElement|null} element DOM节点
 */
const setMessageItemRef = (element) => {
  if (element) {
    messageItemRefs.value.push(element)
  }
}

/**
 * 判断是否开启减少动效偏好。
 *
 * @returns {boolean} 是否应减少动画
 */
const prefersReducedMotion = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches

/**
 * 统一格式化角色文案。
 *
 * @param {string} role 角色值
 * @returns {string} 角色文本
 */
function toRoleLabel(role) {
  if (role === 'teacher') {
    return '教师'
  }
  if (role === 'student') {
    return '学生'
  }
  if (role === 'admin') {
    return '管理员'
  }
  return '成员'
}

/**
 * 格式化群列表时间。
 *
 * @param {string} timestamp 时间戳
 * @returns {string} 格式化结果
 */
function formatConversationTime(timestamp) {
  if (!timestamp) {
    return '暂无消息'
  }
  const date = new Date(timestamp)
  if (Number.isNaN(date.getTime())) {
    return '时间待同步'
  }
  const now = new Date()
  const isSameDay = now.toDateString() === date.toDateString()
  return isSameDay
    ? `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    : `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

/**
 * 格式化完整时间。
 *
 * @param {string} timestamp 时间戳
 * @returns {string} 完整时间文本
 */
function formatDateTime(timestamp) {
  if (!timestamp) {
    return '时间待同步'
  }
  const date = new Date(timestamp)
  if (Number.isNaN(date.getTime())) {
    return '时间待同步'
  }
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

/**
 * 生成课程卡片消息预览。
 *
 * @param {string} content 原始消息内容
 * @returns {string} 预览文本
 */
function formatPreview(content) {
  if (!content) {
    return '还没有群消息，点击进入后开始讨论。'
  }
  return content.replace(/\s+/g, ' ').slice(0, 48)
}

/**
 * 将未读数归一化为非负整数。
 *
 * @param {number|string|null|undefined} unreadCount 原始未读数
 * @returns {number} 归一化后的未读数
 */
function normalizeUnreadCount(unreadCount) {
  const numericCount = Number(unreadCount || 0)
  return Number.isFinite(numericCount) && numericCount > 0 ? Math.trunc(numericCount) : 0
}

/**
 * 统一格式化课程群未读徽标文案。
 *
 * @param {number|string|null|undefined} unreadCount 原始未读数
 * @returns {string} 徽标显示文本
 */
function formatUnreadCount(unreadCount) {
  const normalizedCount = normalizeUnreadCount(unreadCount)
  return normalizedCount > 99 ? '99+' : String(normalizedCount)
}

/**
 * 格式化秒数时长。
 *
 * @param {number} seconds 剩余秒数
 * @returns {string} 文本描述
 */
function formatDuration(seconds) {
  const value = Number(seconds || 0)
  if (value <= 0) {
    return '即将解除'
  }
  const days = Math.floor(value / 86400)
  const hours = Math.floor((value % 86400) / 3600)
  const minutes = Math.floor((value % 3600) / 60)
  if (days > 0) {
    return `${days}天${hours}小时`
  }
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  }
  if (minutes > 0) {
    return `${minutes}分钟`
  }
  return `${value}秒`
}

/**
 * 格式化分钟快捷选项。
 *
 * @param {number} minutes 分钟数
 * @returns {string} 文本描述
 */
function formatMinuteOption(minutes) {
  if (minutes >= 1440) {
    return `${Math.round(minutes / 1440)}天`
  }
  if (minutes >= 60) {
    return `${Math.round(minutes / 60)}小时`
  }
  return `${minutes}分钟`
}

/**
 * 将群聊消息按时间升序排列。
 *
 * @param {Array} list 原始消息列表
 * @returns {Array} 排序后的消息列表
 */
function sortMessages(list) {
  return [...list].sort((left, right) => new Date(left.timestamp).getTime() - new Date(right.timestamp).getTime())
}

/**
 * 解析教师公告内容。
 *
 * @param {object} message 消息实体
 * @returns {object|null} 公告信息
 */
function parseAnnouncement(message) {
  if (message?.messageType !== 'SYSTEM' || !message.content?.startsWith(ANNOUNCEMENT_PREFIX)) {
    return null
  }

  const rawContent = message.content.slice(ANNOUNCEMENT_PREFIX.length)
  const lines = rawContent.split('\n')
  const firstLine = lines[0]?.trim() || ''
  const body = lines.slice(1).join('\n').trim()

  return {
    title: body ? (firstLine || '课程公告') : '课程公告',
    body: body || firstLine || '公告内容待同步'
  }
}

/**
 * 更新课程列表中指定群聊的摘要信息。
 *
 * @param {string} courseId 课程ID
 * @param {object} patch 需要合并的字段
 */
function patchGroupSummary(courseId, patch) {
  groups.value = groups.value
    .map(group => group.courseId === courseId ? { ...group, ...patch } : group)
    .sort((left, right) => new Date(right.lastMessageTime || 0).getTime() - new Date(left.lastMessageTime || 0).getTime())
}

/**
 * 同步 mute-all 计时器，保证界面倒计时平滑下降。
 */
function syncMuteAllTimer() {
  if (muteAllTimer) {
    window.clearInterval(muteAllTimer)
    muteAllTimer = null
  }

  if (!muteAllStatus.value.enabled || !muteAllStatus.value.remainingSeconds) {
    return
  }

  muteAllTimer = window.setInterval(() => {
    if (!muteAllStatus.value.enabled || muteAllStatus.value.remainingSeconds <= 0) {
      window.clearInterval(muteAllTimer)
      muteAllTimer = null
      muteAllStatus.value = { enabled: false, remainingSeconds: 0, expireAt: null }
      if (groupDetail.value) {
        groupDetail.value = {
          ...groupDetail.value,
          groupAllMuted: false,
          groupAllMutedExpireAt: null
        }
      }
      patchGroupSummary(activeCourseId.value, { groupAllMuted: false })
      return
    }

    muteAllStatus.value = {
      ...muteAllStatus.value,
      remainingSeconds: muteAllStatus.value.remainingSeconds - 1
    }
  }, 1000)
}

/**
 * 拉取课程群聊列表，并在用户显式指定课程时再进入群聊。
 * <p>
 * 首次进入页面时不再自动打开首个课程，避免列表刚渲染就触发已读清零，
 * 导致未读红点在教师端和学生端都“加载出来又立刻消失”。
 * </p>
 */
async function loadGroups() {
  groupsLoading.value = true
  groupsError.value = ''

  try {
    const data = await groupApi.value.listGroups()
    groups.value = (Array.isArray(data) ? data : [])
      .sort((left, right) => new Date(right.lastMessageTime || 0).getTime() - new Date(left.lastMessageTime || 0).getTime())

    await nextTick()
    runCourseCardAnimation()

    if (!groups.value.length) {
      clearActiveGroup()
      return
    }

    const queryCourseId = typeof route.query.courseId === 'string' ? route.query.courseId : ''
    const nextCourseId = groups.value.some(group => group.courseId === queryCourseId)
      ? queryCourseId
      : groups.value.some(group => group.courseId === activeCourseId.value)
        ? activeCourseId.value
        : ''

    if (nextCourseId) {
      await openGroup(nextCourseId, { forceRefresh: nextCourseId === activeCourseId.value })
    } else {
      clearActiveGroup()
    }
  } catch (error) {
    groupsError.value = error.message || '课程群聊加载失败'
    ElMessage.error(groupsError.value)
  } finally {
    groupsLoading.value = false
  }
}

/**
 * 清空当前激活群聊。
 */
function clearActiveGroup() {
  activeCourseId.value = ''
  groupDetail.value = null
  members.value = []
  messages.value = []
  mutedMembers.value = []
  muteAllStatus.value = { enabled: false, remainingSeconds: 0, expireAt: null }
  messageInput.value = ''
  socketError.value = ''
  closeGroupSocket(true)

  const nextQuery = { ...route.query }
  delete nextQuery.courseId
  router.replace({ query: nextQuery })
}

/**
 * 打开指定课程群聊。
 *
 * @param {string} courseId 课程ID
 * @param {object} options 控制参数
 */
async function openGroup(courseId, options = {}) {
  if (!courseId) {
    return
  }

  const forceRefresh = Boolean(options.forceRefresh)
  if (activeCourseId.value === courseId && !forceRefresh) {
    return
  }

  activeCourseId.value = courseId
  const nextQuery = { ...route.query, courseId }
  if (route.query.courseId !== courseId) {
    router.replace({ query: nextQuery })
  }

  await Promise.all([
    loadGroupDetail(courseId),
    loadGroupMembers(courseId),
    loadInitialMessages(courseId),
    isTeacherView.value ? loadTeacherGovernance(courseId) : Promise.resolve()
  ])

  connectGroupSocket(courseId)
  await markGroupAsRead(courseId)
}

/**
 * 刷新当前选中群聊的详情数据。
 */
async function refreshActiveGroup() {
  if (!activeCourseId.value) {
    return
  }
  await openGroup(activeCourseId.value, { forceRefresh: true })
  ElMessage.success('课程群聊数据已同步')
}

/**
 * 获取课程群聊详情。
 *
 * @param {string} courseId 课程ID
 */
async function loadGroupDetail(courseId) {
  detailLoading.value = true
  try {
    const detail = await groupApi.value.getGroupDetail(courseId)
    groupDetail.value = detail
    patchGroupSummary(courseId, {
      courseTitle: detail.courseTitle,
      coverImageUrl: detail.coverImageUrl,
      teacherId: detail.teacherId,
      teacherName: detail.teacherName,
      teacherAvatarUrl: detail.teacherAvatarUrl,
      memberCount: detail.memberCount,
      currentUserRole: detail.currentUserRole,
      currentUserMuted: detail.currentUserMuted,
      groupAllMuted: detail.groupAllMuted,
      unreadCount: activeCourseId.value === courseId ? 0 : undefined
    })
  } catch (error) {
    ElMessage.error(error.message || '群聊详情加载失败')
    throw error
  } finally {
    detailLoading.value = false
  }
}

/**
 * 获取课程群成员列表。
 *
 * @param {string} courseId 课程ID
 */
async function loadGroupMembers(courseId) {
  membersLoading.value = true
  try {
    const data = await groupApi.value.getGroupMembers(courseId)
    members.value = Array.isArray(data) ? data : []
  } catch (error) {
    ElMessage.error(error.message || '群成员加载失败')
    throw error
  } finally {
    membersLoading.value = false
  }
}

/**
 * 获取教师端治理数据。
 *
 * @param {string} courseId 课程ID
 */
async function loadTeacherGovernance(courseId) {
  if (!isTeacherView.value) {
    return
  }

  governanceLoading.value = true
  try {
    const [muteList, muteStatus] = await Promise.all([
      teacherGroupChatApi.getMutedMembers(courseId),
      teacherGroupChatApi.getMuteAllStatus(courseId)
    ])
    mutedMembers.value = Array.isArray(muteList) ? muteList : []
    muteAllStatus.value = muteStatus || { enabled: false, remainingSeconds: 0, expireAt: null }
    if (groupDetail.value) {
      groupDetail.value = {
        ...groupDetail.value,
        groupAllMuted: Boolean(muteAllStatus.value.enabled),
        groupAllMutedExpireAt: muteAllStatus.value.expireAt
      }
    }
    patchGroupSummary(courseId, { groupAllMuted: Boolean(muteAllStatus.value.enabled) })
    syncMuteAllTimer()
  } catch (error) {
    ElMessage.error(error.message || '教师治理数据加载失败')
    throw error
  } finally {
    governanceLoading.value = false
  }
}

/**
 * 首次加载群聊消息历史。
 *
 * @param {string} courseId 课程ID
 */
async function loadInitialMessages(courseId) {
  messagesLoading.value = true
  try {
    const data = await groupApi.value.getGroupMessages(courseId, { limit: MESSAGE_PAGE_SIZE })
    const normalized = Array.isArray(data) ? sortMessages(data) : []
    messages.value = normalized
    hasMoreHistory.value = normalized.length >= MESSAGE_PAGE_SIZE
    await nextTick()
    scrollMessagesToBottom('auto')
    runMessageAnimation(true)
  } catch (error) {
    ElMessage.error(error.message || '群消息加载失败')
    throw error
  } finally {
    messagesLoading.value = false
  }
}

/**
 * 向上加载更早历史消息。
 */
async function loadOlderMessages() {
  if (!activeCourseId.value || !messages.value.length || !hasMoreHistory.value) {
    return
  }

  loadingOlderMessages.value = true
  const scroller = messageScrollerRef.value
  const previousHeight = scroller?.scrollHeight || 0
  const previousTop = scroller?.scrollTop || 0

  try {
    const data = await groupApi.value.getGroupMessages(activeCourseId.value, {
      before: messages.value[0].timestamp,
      limit: MESSAGE_PAGE_SIZE
    })
    const normalized = Array.isArray(data) ? sortMessages(data) : []
    const existingIds = new Set(messages.value.map(message => message.id))
    const incoming = normalized.filter(message => !existingIds.has(message.id))
    messages.value = [...incoming, ...messages.value]
    hasMoreHistory.value = normalized.length >= MESSAGE_PAGE_SIZE

    await nextTick()
    if (scroller) {
      const currentHeight = scroller.scrollHeight
      scroller.scrollTop = currentHeight - previousHeight + previousTop
    }
  } catch (error) {
    ElMessage.error(error.message || '加载更早消息失败')
  } finally {
    loadingOlderMessages.value = false
  }
}

/**
 * 将当前课程群的未读数清零，并同步回课程列表。
 *
 * @param {string} courseId 课程ID
 */
async function markGroupAsRead(courseId) {
  if (!courseId) {
    return
  }

  try {
    await groupApi.value.markGroupAsRead(courseId)
    patchGroupSummary(courseId, { unreadCount: 0 })
  } catch (error) {
    console.error('清空课程群未读数失败:', error)
  }
}

/**
 * 连接当前课程群聊 WebSocket。
 *
 * @param {string} courseId 课程ID
 */
function connectGroupSocket(courseId) {
  closeGroupSocket(true)
  socketError.value = ''
  wsConnected.value = false

  groupSocket = createGroupChatWebSocket(courseId, {
    onOpen: () => {
      wsConnected.value = true
      socketError.value = ''
    },
    onMessage: (event) => {
      handleSocketMessage(event.data)
    },
    onClose: (event) => {
      wsConnected.value = false
      if (closeSocketSilently) {
        closeSocketSilently = false
        return
      }
      if (event.reason) {
        socketError.value = event.reason
        ElMessage.warning(event.reason)
      }
      if (event.reason?.includes('移出') || event.reason?.includes('无权')) {
        loadGroups()
      }
    },
    onError: () => {
      wsConnected.value = false
      socketError.value = '课程群聊实时连接出现异常，请稍后重试。'
    }
  })
}

/**
 * 关闭当前群聊 WebSocket。
 *
 * @param {boolean} silent 是否静默关闭
 */
function closeGroupSocket(silent = false) {
  if (!groupSocket) {
    return
  }
  closeSocketSilently = silent
  groupSocket.close()
  groupSocket = null
  wsConnected.value = false
}

/**
 * 处理 WebSocket 消息。
 *
 * @param {string} rawMessage 原始消息文本
 */
function handleSocketMessage(rawMessage) {
  try {
    const payload = JSON.parse(rawMessage)
    if (payload.type === 'ERROR') {
      ElMessage.error(payload.message || '群聊消息发送失败')
      return
    }

    if (!payload?.conversationId || payload.conversationId !== activeCourseId.value) {
      return
    }

    const exists = messages.value.some(message => message.id === payload.id)
    messages.value = exists
      ? messages.value.map(message => message.id === payload.id ? payload : message)
      : sortMessages([...messages.value, payload])

    patchGroupSummary(payload.conversationId, {
      lastMessageContent: payload.content,
      lastMessageSenderId: payload.senderId,
      lastMessageTag: payload.tag,
      lastMessageTime: payload.timestamp,
      unreadCount: 0
    })

    nextTick(() => {
      runMessageAnimation(false)
      scrollMessagesToBottom('smooth')
    })
  } catch (error) {
    console.error('解析课程群聊消息失败:', error, rawMessage)
  }
}

/**
 * 将视图滚动到消息底部。
 *
 * @param {ScrollBehavior} behavior 滚动行为
 */
function scrollMessagesToBottom(behavior = 'smooth') {
  const scroller = messageScrollerRef.value
  if (!scroller) {
    return
  }
  scroller.scrollTo({ top: scroller.scrollHeight, behavior })
}

/**
 * 处理输入框快捷发送。
 *
 * @param {KeyboardEvent} event 键盘事件
 */
function handleComposerKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

/**
 * 发送群聊消息。
 */
function sendMessage() {
  if (!groupSocket || groupSocket.readyState !== WebSocket.OPEN) {
    ElMessage.warning('课程群聊实时连接尚未就绪')
    return
  }
  if (sendDisabledReason.value || !messageInput.value.trim()) {
    return
  }

  groupSocket.send(JSON.stringify({
    content: messageInput.value.trim(),
    contentType: 'TEXT'
  }))
  messageInput.value = ''
}

/**
 * 打开成员禁言弹窗。
 *
 * @param {object} member 成员对象
 */
function openMuteMemberDialog(member) {
  selectedMember.value = member
  muteForm.value.durationMinutes = 15
  muteMemberDialogVisible.value = true
}

/**
 * 提交成员禁言操作。
 */
async function submitMuteMember() {
  if (!selectedMember.value || !activeCourseId.value) {
    return
  }

  try {
    await teacherGroupChatApi.muteMember(activeCourseId.value, {
      targetUserId: selectedMember.value.userId,
      durationMinutes: Number(muteForm.value.durationMinutes)
    })
    muteMemberDialogVisible.value = false
    ElMessage.success(`已禁言 ${selectedMember.value.userName}`)
    await Promise.all([loadGroupMembers(activeCourseId.value), loadTeacherGovernance(activeCourseId.value), loadGroupDetail(activeCourseId.value)])
  } catch (error) {
    ElMessage.error(error.message || '成员禁言失败')
  }
}

/**
 * 对成员解除禁言。
 *
 * @param {object} member 成员对象
 */
async function handleUnmuteMember(member) {
  if (!activeCourseId.value) {
    return
  }
  await handleUnmuteById(member.userId, member.userName)
}

/**
 * 根据用户ID解除禁言。
 *
 * @param {string} userId 用户ID
 * @param {string} userName 用户名称
 */
async function handleUnmuteById(userId, userName = '该成员') {
  if (!activeCourseId.value) {
    return
  }
  try {
    await teacherGroupChatApi.unmuteMember(activeCourseId.value, userId)
    ElMessage.success(`已解除 ${userName} 的禁言`)
    await Promise.all([loadGroupMembers(activeCourseId.value), loadTeacherGovernance(activeCourseId.value), loadGroupDetail(activeCourseId.value)])
  } catch (error) {
    ElMessage.error(error.message || '解除禁言失败')
  }
}

/**
 * 将成员移出当前课程群聊。
 *
 * @param {object} member 成员对象
 */
async function handleKickMember(member) {
  if (!activeCourseId.value) {
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认将 ${member.userName} 移出当前课程群聊吗？该操作不会删除选课关系，但会立即取消其群聊访问权。`,
      '移出群聊',
      {
        confirmButtonText: '确认移出',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await teacherGroupChatApi.kickMember(activeCourseId.value, member.userId)
    ElMessage.success(`${member.userName} 已被移出课程群聊`)
    await Promise.all([loadGroupMembers(activeCourseId.value), loadTeacherGovernance(activeCourseId.value), loadGroupDetail(activeCourseId.value), loadGroups()])
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    ElMessage.error(error.message || '移出群聊失败')
  }
}

/**
 * 提交全员禁言操作。
 */
async function submitMuteAll() {
  if (!activeCourseId.value) {
    return
  }

  try {
    await teacherGroupChatApi.muteAllMembers(activeCourseId.value, {
      durationMinutes: Number(muteAllForm.value.durationMinutes)
    })
    muteAllDialogVisible.value = false
    ElMessage.success('已开启全员禁言')
    await Promise.all([loadTeacherGovernance(activeCourseId.value), loadGroupDetail(activeCourseId.value)])
  } catch (error) {
    ElMessage.error(error.message || '开启全员禁言失败')
  }
}

/**
 * 解除全员禁言。
 */
async function handleUnmuteAll() {
  if (!activeCourseId.value) {
    return
  }

  try {
    await teacherGroupChatApi.unmuteAllMembers(activeCourseId.value)
    ElMessage.success('已解除全员禁言')
    await Promise.all([loadTeacherGovernance(activeCourseId.value), loadGroupDetail(activeCourseId.value)])
  } catch (error) {
    ElMessage.error(error.message || '解除全员禁言失败')
  }
}

/**
 * 发布教师公告。
 */
async function submitAnnouncement() {
  if (!activeCourseId.value || !announcementForm.value.content) {
    ElMessage.warning('请输入公告正文')
    return
  }

  try {
    const message = await teacherGroupChatApi.publishAnnouncement(activeCourseId.value, announcementForm.value)
    announcementDialogVisible.value = false
    announcementForm.value = { title: '', content: '' }
    handleSocketMessage(JSON.stringify(message))
    ElMessage.success('课程公告已发布')
  } catch (error) {
    ElMessage.error(error.message || '发布公告失败')
  }
}

/**
 * 执行页面首屏动画。
 */
function runWorkspaceAnimation() {
  if (prefersReducedMotion()) {
    gsap.set([workspaceRef.value, courseRailRef.value, dialogPanelRef.value, insightPanelRef.value], { opacity: 1, x: 0, y: 0 })
    return
  }

  workspaceTimeline?.kill()
  workspaceTimeline = gsap.timeline({ defaults: { ease: 'power2.out' } })
  workspaceTimeline
    .fromTo('.hero-panel', { y: 24, opacity: 0 }, { y: 0, opacity: 1, duration: 0.55 })
    .fromTo('.course-rail', { x: -30, opacity: 0 }, { x: 0, opacity: 1, duration: 0.45 }, '-=0.2')
    .fromTo('.dialog-panel', { y: 24, opacity: 0 }, { y: 0, opacity: 1, duration: 0.45 }, '-=0.15')
    .fromTo('.insight-panel', { x: 30, opacity: 0 }, { x: 0, opacity: 1, duration: 0.45 }, '-=0.2')
}

/**
 * 执行课程卡片入场动画。
 */
function runCourseCardAnimation() {
  if (prefersReducedMotion() || !courseCardRefs.value.length) {
    return
  }
  courseTimeline?.kill()
  courseTimeline = gsap.fromTo(courseCardRefs.value,
    { y: 18, opacity: 0 },
    { y: 0, opacity: 1, duration: 0.3, stagger: 0.06, ease: 'power2.out' }
  )
}

/**
 * 执行消息列表动画。
 *
 * @param {boolean} initial 是否首屏动画
 */
function runMessageAnimation(initial = false) {
  if (prefersReducedMotion() || !messageItemRefs.value.length) {
    return
  }

  const targets = initial ? messageItemRefs.value : messageItemRefs.value.slice(-Math.min(3, messageItemRefs.value.length))
  messageTimeline?.kill()
  messageTimeline = gsap.fromTo(targets,
    { y: 16, opacity: 0 },
    { y: 0, opacity: 1, duration: 0.25, stagger: 0.04, ease: 'power2.out' }
  )
}

watch(() => route.query.courseId, async (courseId) => {
  if (typeof courseId !== 'string' || !courseId || courseId === activeCourseId.value) {
    return
  }
  if (groups.value.some(group => group.courseId === courseId)) {
    await openGroup(courseId)
  }
})

watch(renderedMessages, async (value) => {
  if (!value.length || loadingOlderMessages.value) {
    return
  }
  await nextTick()
  runMessageAnimation(false)
}, { deep: true })

onMounted(async () => {
  runWorkspaceAnimation()
  scheduleWorkspaceShellHeightSync()

  window.addEventListener('resize', scheduleWorkspaceShellHeightSync, { passive: true })
  if ('ResizeObserver' in window && heroPanelRef.value) {
    heroPanelResizeObserver = new ResizeObserver(() => {
      scheduleWorkspaceShellHeightSync()
    })
    heroPanelResizeObserver.observe(heroPanelRef.value)
  }

  await loadGroups()
  await nextTick()
  scheduleWorkspaceShellHeightSync()
})

onUnmounted(() => {
  closeGroupSocket(true)
  workspaceTimeline?.kill()
  courseTimeline?.kill()
  messageTimeline?.kill()
  dialogAnimations.forEach((animation) => animation.kill())
  dialogAnimations.clear()
  if (muteAllTimer) {
    window.clearInterval(muteAllTimer)
  }
  if (heroPanelResizeObserver) {
    heroPanelResizeObserver.disconnect()
  }
  if (workspaceShellFrame) {
    window.cancelAnimationFrame(workspaceShellFrame)
  }
  window.removeEventListener('resize', scheduleWorkspaceShellHeightSync)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Baloo+2:wght@500;600;700&family=Comic+Neue:wght@400;700&display=swap');

.group-chat-page {
  --chat-primary: #0D9488;
  --chat-primary-dark: #115E59;
  --chat-secondary: #2DD4BF;
  --chat-accent: #F97316;
  --chat-accent-soft: #FFEDD5;
  --chat-bg: #F0FDFA;
  --chat-card: rgba(255, 255, 255, 0.92);
  --chat-text: #134E4A;
  --chat-muted: #4B7C78;
  --chat-border: rgba(13, 148, 136, 0.14);
  --chat-shadow: 0 18px 45px rgba(13, 148, 136, 0.12);
  --chat-shadow-soft: 0 10px 24px rgba(15, 23, 42, 0.08);
  position: relative;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 22px;
  padding: 8px 0 20px;
  min-height: 0;
  color: var(--chat-text);
  font-family: 'Comic Neue', 'Segoe UI', sans-serif;
}

.ambient {
  position: absolute;
  inset: auto;
  border-radius: 999px;
  filter: blur(12px);
  pointer-events: none;
  opacity: 0.7;
}

.ambient-left {
  top: 32px;
  left: -36px;
  width: 180px;
  height: 180px;
  background: radial-gradient(circle, rgba(45, 212, 191, 0.3), transparent 68%);
}

.ambient-right {
  right: -20px;
  bottom: 32px;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(249, 115, 22, 0.18), transparent 72%);
}

.clay-card {
  position: relative;
  background: var(--chat-card);
  border: 1px solid rgba(255, 255, 255, 0.88);
  border-radius: 28px;
  box-shadow: var(--chat-shadow), inset 4px 4px 0 rgba(255, 255, 255, 0.85), inset -6px -6px 0 rgba(13, 148, 136, 0.05);
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr);
  gap: 24px;
  padding: 28px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(236, 253, 245, 0.92));
}

.hero-kicker,
.section-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 0.82rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--chat-primary);
}

.hero-title,
.section-title,
.announcement-card h3,
.course-card h3,
.dialog-title-row h2 {
  font-family: 'Baloo 2', 'Segoe UI', sans-serif;
}

.hero-title {
  margin: 8px 0 12px;
  font-size: clamp(2rem, 4vw, 2.9rem);
  line-height: 1.04;
}

.hero-description {
  max-width: 640px;
  font-size: 1rem;
  line-height: 1.7;
  color: var(--chat-muted);
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.metric-chip {
  display: grid;
  gap: 8px;
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(180deg, #ffffff, #f7fffd);
  border: 2px solid rgba(13, 148, 136, 0.08);
  box-shadow: var(--chat-shadow-soft);
}

.metric-chip.accent {
  background: linear-gradient(180deg, #fff8f2, #fff2e5);
  border-color: rgba(249, 115, 22, 0.18);
}

.metric-label {
  font-size: 0.84rem;
  color: var(--chat-muted);
}

.metric-value {
  font-size: 1.3rem;
  color: var(--chat-text);
}

.metric-note {
  font-size: 0.82rem;
  color: var(--chat-muted);
}

.workspace-shell {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr) 340px;
  gap: 20px;
  height: var(--workspace-shell-height, auto);
  min-height: 0;
  align-items: stretch;
}

.course-rail,
.dialog-panel,
.insight-panel {
  min-height: 0;
  height: 100%;
}

.course-rail,
.insight-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 20px;
  overflow: hidden;
}

.course-rail-scroll,
.insight-scroll,
.message-stream {
  min-height: 0;
  overflow: auto;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
  scrollbar-width: thin;
  scrollbar-color: rgba(13, 148, 136, 0.34) transparent;
}

.course-rail-scroll,
.insight-scroll {
  padding-right: 6px;
}

.course-rail-scroll:focus-visible,
.insight-scroll:focus-visible,
.message-stream:focus-visible {
  outline: 2px solid rgba(13, 148, 136, 0.28);
  outline-offset: 4px;
}

.course-rail-scroll::-webkit-scrollbar,
.insight-scroll::-webkit-scrollbar,
.message-stream::-webkit-scrollbar {
  width: 10px;
}

.course-rail-scroll::-webkit-scrollbar-thumb,
.insight-scroll::-webkit-scrollbar-thumb,
.message-stream::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, rgba(13, 148, 136, 0.34), rgba(45, 212, 191, 0.48));
  border: 2px solid transparent;
  border-radius: 999px;
  background-clip: padding-box;
}

.course-rail-scroll::-webkit-scrollbar-track,
.insight-scroll::-webkit-scrollbar-track,
.message-stream::-webkit-scrollbar-track {
  background: transparent;
}

.insight-scroll {
  display: grid;
  gap: 18px;
}

.dialog-panel {
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr) auto;
  gap: 16px;
  padding: 20px;
  overflow: hidden;
}

.rail-header,
.side-header,
.dialog-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.dialog-header {
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px dashed rgba(13, 148, 136, 0.16);
}

.search-box,
.composer-box {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  width: 100%;
  padding: 14px 16px;
  border-radius: 18px;
  background: #ffffff;
  border: 2px solid transparent;
  box-shadow: inset 2px 2px 0 rgba(255, 255, 255, 0.92), inset -3px -3px 0 rgba(13, 148, 136, 0.05), var(--chat-shadow-soft);
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.search-box:focus-within,
.composer-box:focus-within {
  border-color: rgba(13, 148, 136, 0.32);
  transform: translateY(-1px);
}

.search-box input,
.composer-box textarea {
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  resize: none;
  color: var(--chat-text);
  font: inherit;
}

.course-list,
.member-list,
.muted-list,
.skeleton-list {
  display: grid;
  gap: 12px;
}

.course-list {
  margin-top: 16px;
}

.course-card {
  display: grid;
  gap: 12px;
  padding: 16px;
  border-radius: 22px;
  background: linear-gradient(180deg, #ffffff, #f8fffd);
  border: 2px solid transparent;
  box-shadow: var(--chat-shadow-soft);
  cursor: pointer;
  transition: transform 0.22s ease, border-color 0.22s ease, box-shadow 0.22s ease;
}

.course-card:hover,
.course-card:focus-visible {
  transform: translateY(-2px);
  border-color: rgba(13, 148, 136, 0.24);
}

.course-card.active {
  border-color: rgba(13, 148, 136, 0.46);
  box-shadow: 0 16px 32px rgba(13, 148, 136, 0.16);
}

.course-card-top {
  display: flex;
  gap: 14px;
}

.course-visual,
.dialog-avatar,
.course-placeholder {
  width: 62px;
  height: 62px;
  flex-shrink: 0;
  border-radius: 18px;
}

.course-visual img,
.dialog-avatar img,
.message-avatar img,
.member-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: inherit;
}

.course-placeholder,
.avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(13, 148, 136, 0.18), rgba(45, 212, 191, 0.28));
  color: var(--chat-primary-dark);
  font-weight: 700;
}

.course-content {
  min-width: 0;
}

.course-title-row,
.member-name-row,
.message-meta,
.announcement-header,
.dialog-title-row,
.composer-actions,
.course-card-bottom,
.member-meta-row,
.dialog-footer-row,
.quick-option-row,
.overview-grid {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.course-title-row {
  justify-content: space-between;
  align-items: flex-start;
}

.course-title-meta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.course-card h3 {
  font-size: 1.1rem;
  line-height: 1.15;
}

.course-unread-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  background: linear-gradient(180deg, #ef4444, #dc2626);
  color: #ffffff;
  font-size: 0.74rem;
  font-weight: 700;
  line-height: 1;
  box-shadow: 0 10px 18px rgba(220, 38, 38, 0.24);
}

.course-teacher,
.course-preview,
.dialog-subtitle,
.overview-description,
.governance-note,
.member-meta-row,
.muted-item p,
.message-time,
.composer-hint,
.empty-state p,
.dialog-body-copy {
  color: var(--chat-muted);
}

.course-preview {
  margin-top: 6px;
  font-size: 0.92rem;
  line-height: 1.45;
}

.time-pill,
.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 700;
}

.status-badge.soft {
  background: rgba(13, 148, 136, 0.12);
  color: var(--chat-primary-dark);
}

.status-badge.warning,
.accent-banner,
.announcement-card {
  background: #fff5eb;
  color: #9A3412;
}

.status-badge.danger,
.danger-banner {
  background: #fff1f2;
  color: #BE123C;
}

.status-badge.neutral,
.warning-banner {
  background: rgba(15, 23, 42, 0.06);
  color: #334155;
}

.status-badge.accent {
  background: rgba(249, 115, 22, 0.16);
  color: #9A3412;
}

.status-badge.tiny {
  min-height: 22px;
  font-size: 0.72rem;
}

.dialog-course-meta,
.member-profile,
.muted-item,
.overview-chip,
.message-row,
.message-body,
.governance-actions {
  display: flex;
  gap: 14px;
}

.dialog-course-meta,
.member-profile,
.overview-chip,
.message-row {
  align-items: center;
}

.dialog-course-meta {
  min-width: 0;
}

.dialog-actions,
.member-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.dialog-divider {
  margin: 0 8px;
}

.status-stack {
  display: grid;
  gap: 10px;
}

.status-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 18px;
  border: 1px solid rgba(13, 148, 136, 0.08);
}

.message-stream {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  min-height: 0;
  gap: 12px;
  padding-right: 4px;
}

.message-toolbar {
  display: flex;
  justify-content: center;
}

.message-list,
.skeleton-messages {
  display: grid;
  gap: 14px;
}

.message-row {
  align-items: flex-end;
}

.message-row.own {
  justify-content: flex-end;
}

.message-row.own .message-body {
  align-items: flex-end;
}

.message-row.teacher .message-bubble {
  background: linear-gradient(180deg, #ecfdf5, #d1fae5);
}

.message-row.own .message-bubble {
  background: linear-gradient(180deg, #0D9488, #0F766E);
  color: #ffffff;
}

.message-avatar,
.member-avatar {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  flex-shrink: 0;
}

.message-body {
  flex-direction: column;
  align-items: flex-start;
  max-width: min(76%, 520px);
}

.message-sender {
  font-weight: 700;
}

.message-tag {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
}

.teacher-tag {
  background: rgba(13, 148, 136, 0.12);
  color: var(--chat-primary-dark);
}

.student-tag {
  background: rgba(15, 23, 42, 0.06);
  color: #475569;
}

.message-bubble {
  padding: 14px 16px;
  border-radius: 22px 22px 22px 10px;
  background: #ffffff;
  box-shadow: var(--chat-shadow-soft);
  line-height: 1.6;
  word-break: break-word;
}

.message-row.own .message-bubble {
  border-radius: 22px 22px 10px 22px;
}

.announcement-card {
  width: min(100%, 620px);
  margin: 0 auto;
  padding: 18px;
  border-radius: 24px;
  border: 2px solid rgba(249, 115, 22, 0.18);
  box-shadow: 0 16px 28px rgba(249, 115, 22, 0.12);
}

.announcement-card h3 {
  margin: 12px 0 8px;
  font-size: 1.2rem;
}

.announcement-card p {
  line-height: 1.7;
}

.announcement-footer,
.announcement-time {
  margin-top: 12px;
  font-size: 0.84rem;
  color: #9A3412;
}

.composer-panel {
  display: grid;
  gap: 12px;
  padding-top: 6px;
  border-top: 1px dashed rgba(13, 148, 136, 0.16);
}

.composer-actions {
  justify-content: space-between;
}

.composer-hint {
  flex: 1;
  min-width: 180px;
}

.side-section {
  display: grid;
  gap: 14px;
}

.side-section + .side-section {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px dashed rgba(13, 148, 136, 0.16);
}

.overview-grid {
  gap: 12px;
}

.overview-chip {
  flex: 1 1 140px;
  flex-direction: column;
  align-items: flex-start;
  padding: 14px;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff, #f8fffd);
  box-shadow: var(--chat-shadow-soft);
}

.overview-chip span {
  font-size: 0.84rem;
  color: var(--chat-muted);
}

.governance-actions {
  flex-wrap: wrap;
}

.member-item,
.muted-item {
  justify-content: space-between;
  align-items: center;
  padding: 14px;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff, #f8fffd);
  box-shadow: var(--chat-shadow-soft);
}

.member-profile {
  flex: 1;
  min-width: 0;
}

.member-item strong,
.muted-item strong {
  font-size: 0.96rem;
}

.ghost-button,
.primary-button,
.mini-button,
.quick-option {
  border: none;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
  font: inherit;
}

.ghost-button,
.primary-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 42px;
  padding: 0 16px;
  border-radius: 16px;
}

.ghost-button.small {
  min-height: 36px;
  font-size: 0.84rem;
}

.ghost-button {
  background: rgba(255, 255, 255, 0.88);
  color: var(--chat-text);
  box-shadow: var(--chat-shadow-soft);
}

.primary-button {
  background: linear-gradient(180deg, var(--chat-accent), #EA580C);
  color: #ffffff;
  box-shadow: 0 12px 22px rgba(249, 115, 22, 0.22);
}

.ghost-button:hover,
.primary-button:hover,
.mini-button:hover,
.quick-option:hover {
  transform: translateY(-1px);
}

.ghost-button:disabled,
.primary-button:disabled {
  opacity: 0.56;
  cursor: not-allowed;
  transform: none;
}

.mini-button {
  min-height: 32px;
  padding: 0 12px;
  border-radius: 12px;
  background: rgba(13, 148, 136, 0.1);
  color: var(--chat-primary-dark);
}

.compact-button {
  min-height: 38px;
}

.danger-outline {
  background: rgba(255, 241, 242, 0.82);
  color: #BE123C;
}

.empty-state {
  display: grid;
  place-items: center;
  gap: 10px;
  text-align: center;
}

.empty-state.compact {
  padding: 22px 12px;
}

.empty-state.spacious {
  align-content: center;
  min-height: 100%;
}

.empty-icon {
  font-size: 2rem;
  color: var(--chat-primary);
}

.inner-empty {
  padding: 18px 6px;
}

.muted-empty .empty-icon {
  color: var(--chat-accent);
}

.skeleton-card,
.message-skeleton {
  border-radius: 18px;
  background: linear-gradient(90deg, rgba(13, 148, 136, 0.08), rgba(255, 255, 255, 0.88), rgba(13, 148, 136, 0.08));
  background-size: 220% 100%;
  animation: skeletonMove 1.4s ease infinite;
}

.skeleton-card {
  height: 112px;
}

.skeleton-card.small {
  height: 72px;
}

.message-skeleton {
  height: 76px;
}

.skeleton-list.tight {
  gap: 10px;
}

.dialog-body-block {
  display: grid;
  gap: 10px;
  margin-bottom: 18px;
}

.dialog-body-title {
  font-weight: 700;
  color: var(--chat-text);
}

.quick-option-row {
  gap: 8px;
}

.quick-option {
  min-height: 38px;
  padding: 0 14px;
  border-radius: 14px;
  background: rgba(13, 148, 136, 0.08);
  color: var(--chat-primary-dark);
}

.quick-option.active {
  background: rgba(13, 148, 136, 0.18);
  box-shadow: inset 0 0 0 2px rgba(13, 148, 136, 0.16);
}

.dialog-footer-row {
  justify-content: flex-end;
}

:deep(.course-group-dialog-overlay) {
  background:
    radial-gradient(circle at top left, rgba(45, 212, 191, 0.18), transparent 38%),
    radial-gradient(circle at bottom right, rgba(249, 115, 22, 0.12), transparent 42%),
    rgba(15, 23, 42, 0.44);
  backdrop-filter: blur(16px);
}

:deep(.course-group-dialog) {
  margin: 0;
  width: min(calc(100vw - 32px), var(--el-dialog-width, 560px));
  max-width: calc(100vw - 32px);
  border: 1px solid rgba(255, 255, 255, 0.92);
  border-radius: 28px;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(240, 253, 250, 0.96));
  box-shadow: 0 28px 60px rgba(15, 23, 42, 0.22);
}

:deep(.course-group-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 22px 24px 12px;
  background: linear-gradient(135deg, rgba(13, 148, 136, 0.12), rgba(255, 255, 255, 0.88));
  border-bottom: 1px solid rgba(13, 148, 136, 0.1);
}

:deep(.course-group-dialog .el-dialog__title) {
  font-family: 'Baloo 2', 'Segoe UI', sans-serif;
  font-size: 1.45rem;
  font-weight: 700;
  color: var(--chat-text);
}

:deep(.course-group-dialog .el-dialog__headerbtn) {
  top: 18px;
  right: 18px;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.72);
  transition: background-color 0.2s ease, transform 0.2s ease;
}

:deep(.course-group-dialog .el-dialog__headerbtn:hover) {
  background: rgba(13, 148, 136, 0.12);
  transform: translateY(-1px);
}

:deep(.course-group-dialog .el-dialog__body) {
  padding: 22px 24px 12px;
  max-height: min(70vh, 620px);
  overflow: auto;
}

:deep(.course-group-dialog .el-dialog__footer) {
  padding: 0 24px 24px;
}

:deep(.course-group-dialog .el-input__wrapper),
:deep(.course-group-dialog .el-textarea__inner),
:deep(.course-group-dialog .el-input-number) {
  border-radius: 16px;
  box-shadow: inset 2px 2px 0 rgba(255, 255, 255, 0.9), inset -3px -3px 0 rgba(13, 148, 136, 0.05), var(--chat-shadow-soft);
}

:deep(.course-group-dialog .el-input__wrapper.is-focus),
:deep(.course-group-dialog .el-textarea__inner:focus),
:deep(.course-group-dialog .el-input-number:focus-within) {
  box-shadow: 0 0 0 2px rgba(13, 148, 136, 0.16), inset 2px 2px 0 rgba(255, 255, 255, 0.9), inset -3px -3px 0 rgba(13, 148, 136, 0.05), var(--chat-shadow-soft);
}

:deep(.course-group-dialog .el-input-number) {
  width: 100%;
}

:deep(.course-group-dialog .el-input-number .el-input__wrapper) {
  box-shadow: none;
}

:deep(.course-group-dialog .el-textarea__inner) {
  min-height: 144px;
  padding: 14px 16px;
  resize: vertical;
}

:deep(.course-group-dialog .el-dialog__headerbtn:focus-visible),
:deep(.course-group-dialog .el-input__wrapper:focus-within),
:deep(.course-group-dialog .el-textarea__inner:focus-visible) {
  outline: 2px solid rgba(13, 148, 136, 0.3);
  outline-offset: 2px;
}

@keyframes skeletonMove {
  from {
    background-position: 200% 0;
  }
  to {
    background-position: -20% 0;
  }
}

@media (max-width: 1320px) {
  .workspace-shell {
    grid-template-columns: 280px minmax(0, 1fr) 300px;
  }
}

@media (max-width: 1080px) {
  .hero-panel,
  .workspace-shell {
    grid-template-columns: 1fr;
  }

  .workspace-shell {
    height: auto;
  }

  .hero-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .course-rail,
  .insight-panel {
    min-height: 340px;
    max-height: 42vh;
  }

  .dialog-panel {
    height: min(72vh, 760px);
    min-height: 560px;
  }
}

@media (max-width: 640px) {
  .group-chat-page {
    gap: 16px;
  }

  .hero-panel,
  .course-rail,
  .dialog-panel,
  .insight-panel {
    padding: 16px;
    border-radius: 24px;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }

  .course-rail,
  .insight-panel {
    min-height: 300px;
    max-height: 38vh;
  }

  .dialog-panel {
    height: min(68vh, 680px);
    min-height: 480px;
  }

  .course-card-top,
  .dialog-course-meta,
  .member-item,
  .muted-item,
  .composer-actions,
  .dialog-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .dialog-actions,
  .member-actions,
  .governance-actions {
    width: 100%;
  }

  .dialog-actions > button,
  .governance-actions > button,
  .composer-actions > button {
    width: 100%;
  }

  .message-body {
    max-width: 100%;
  }

  .message-row {
    align-items: flex-start;
  }

  :deep(.course-group-dialog) {
    width: min(calc(100vw - 20px), var(--el-dialog-width, 560px));
    max-width: calc(100vw - 20px);
    border-radius: 24px;
  }

  :deep(.course-group-dialog .el-dialog__header) {
    padding: 18px 18px 10px;
  }

  :deep(.course-group-dialog .el-dialog__body) {
    padding: 18px 18px 10px;
    max-height: min(74vh, 70dvh);
  }

  :deep(.course-group-dialog .el-dialog__footer) {
    padding: 0 18px 18px;
  }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}
</style>