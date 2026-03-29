<template>
  <div class="notification-center-page">
    <section ref="heroRef" class="notification-hero glass-shell">
      <div class="hero-copy">
        <span class="hero-kicker">{{ pageMeta.kicker }}</span>
        <h1 class="hero-title">{{ pageMeta.title }}</h1>
        <p class="hero-description">{{ pageMeta.description }}</p>
      </div>

      <div class="hero-summary">
        <article class="summary-card glass-panel primary-card">
          <span class="summary-label">未读通知</span>
          <strong class="summary-value">{{ unreadCount }}</strong>
          <span class="summary-hint">优先显示未读与高等级通知</span>
        </article>
        <article class="summary-card glass-panel warm-card">
          <span class="summary-label">当前筛选结果</span>
          <strong class="summary-value">{{ notificationPage.total || 0 }}</strong>
          <span class="summary-hint">支持按类型与未读状态过滤</span>
        </article>
      </div>
    </section>

    <section ref="toolbarRef" class="notification-toolbar glass-shell">
      <div class="toolbar-group type-chips">
        <button
          v-for="type in notificationTypes"
          :key="type.value"
          type="button"
          :class="['type-chip', { active: filters.notificationType === type.value }]"
          @click="handleTypeChange(type.value)"
        >
          <span>{{ type.label }}</span>
        </button>
      </div>

      <div class="toolbar-group control-row">
        <label class="toggle-card">
          <span class="toggle-copy">
            <strong>只看未读</strong>
            <small>快速聚焦需要立即处理的提醒</small>
          </span>
          <el-switch
            v-model="filters.unreadOnly"
            style="--el-switch-on-color: #0f766e; --el-switch-off-color: #cbd5e1;"
            @change="refreshNotifications(1)"
          />
        </label>

        <el-button
          class="glass-action"
          :disabled="unreadCount === 0 || markingAll"
          :loading="markingAll"
          @click="handleMarkAllRead"
        >
          全部标记已读
        </el-button>
      </div>
    </section>

    <section ref="listRef" class="notification-list-shell glass-shell">
      <header class="section-head">
        <div>
          <h2 class="section-title">消息流</h2>
          <p class="section-subtitle">统一承接系统公告、测评发布、阅卷完成与课程变动提醒。</p>
        </div>
        <el-button class="glass-action secondary" :loading="loading" @click="refreshNotifications(filters.pageNum)">
          刷新通知
        </el-button>
      </header>

      <div v-if="loading" class="state-board loading-board">
        <div class="pulse-ring"></div>
        <p>正在同步最新通知...</p>
      </div>

      <div v-else-if="notificationItems.length" class="notification-list">
        <article
          v-for="item in notificationItems"
          :key="item.id"
          class="notification-card glass-panel"
          :class="[levelClass(item.level), { unread: !item.isRead }]"
        >
          <div class="notification-accent"></div>
          <div class="notification-main">
            <div class="notification-meta-row">
              <div class="meta-left">
                <span class="notification-badge">{{ typeLabelMap[item.notificationType] || '系统提醒' }}</span>
                <span v-if="!item.isRead" class="unread-dot">未读</span>
              </div>
              <span class="notification-time">{{ formatDisplayTime(item.createdAt) }}</span>
            </div>

            <h3 class="notification-title">{{ item.title }}</h3>
            <p class="notification-content">{{ item.content }}</p>

            <div class="notification-extra">
              <span v-if="item.courseId">课程ID：{{ item.courseId }}</span>
              <span v-if="item.sourceId">来源ID：{{ item.sourceId }}</span>
              <span v-if="item.readAt">已读于 {{ formatDisplayTime(item.readAt) }}</span>
            </div>
          </div>

          <div class="notification-actions">
            <el-button
              v-if="!item.isRead"
              class="glass-action secondary"
              :loading="markingId === item.id"
              @click="handleMarkRead(item.id)"
            >
              标记已读
            </el-button>
            <span v-else class="read-state">已处理</span>
          </div>
        </article>
      </div>

      <div v-else class="state-board empty-board">
        <div class="empty-orbit">
          <div class="orbit-core"></div>
        </div>
        <h3>当前筛选下没有通知</h3>
        <p>你可以切换通知类型，或者关闭“只看未读”查看完整历史记录。</p>
      </div>

      <div class="pagination-wrap" v-if="notificationPage.total > filters.pageSize">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="filters.pageNum"
          :page-size="filters.pageSize"
          :total="notificationPage.total"
          @current-change="refreshNotifications"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import gsap from 'gsap'
import {
  getNotificationPage,
  getUnreadNotificationCount,
  markAllNotificationsAsRead,
  markNotificationAsRead
} from '@/api/notifications.js'

const route = useRoute()
const heroRef = ref(null)
const toolbarRef = ref(null)
const listRef = ref(null)

const filters = reactive({
  pageNum: 1,
  pageSize: 10,
  unreadOnly: false,
  notificationType: ''
})

const notificationPage = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  unreadCount: 0,
  items: []
})

const loading = ref(false)
const unreadCount = ref(0)
const markingId = ref('')
const markingAll = ref(false)

let enterTimeline = null

const notificationTypes = [
  { label: '全部', value: '' },
  { label: '系统公告', value: 'system_announcement' },
  { label: '测评发布', value: 'assessment_published' },
  { label: '阅卷完成', value: 'grading_completed' },
  { label: '课程变动', value: 'course_changed' }
]

const typeLabelMap = {
  system_announcement: '系统公告',
  assessment_published: '测评发布',
  grading_completed: '阅卷完成',
  course_changed: '课程变动'
}

const pageMeta = computed(() => {
  const isTeacher = route.path.startsWith('/teacher')
  return isTeacher
    ? {
        kicker: 'Teacher Signal Center',
        title: '教师消息通知中心',
        description: '把课程发布、阅卷结果与公告提醒集中到一个稳定入口，避免在多个页面之间来回切换。'
      }
    : {
        kicker: 'Student Signal Center',
        title: '学生消息通知中心',
        description: '统一查看课程动态、测评发布、阅卷反馈与平台公告，让学习提醒保持单一入口。'
      }
})

const notificationItems = computed(() => notificationPage.value.items || [])

const loadUnreadCount = async () => {
  const data = await getUnreadNotificationCount()
  unreadCount.value = Number(data?.unreadCount || 0)
  window.dispatchEvent(new CustomEvent('notification-unread-updated', {
    detail: { unreadCount: unreadCount.value }
  }))
}

const refreshNotifications = async (pageNum = filters.pageNum) => {
  loading.value = true
  try {
    filters.pageNum = pageNum
    const data = await getNotificationPage({
      pageNum: filters.pageNum,
      pageSize: filters.pageSize,
      unreadOnly: filters.unreadOnly,
      notificationType: filters.notificationType
    })
    notificationPage.value = {
      pageNum: Number(data?.pageNum || filters.pageNum),
      pageSize: Number(data?.pageSize || filters.pageSize),
      total: Number(data?.total || 0),
      unreadCount: Number(data?.unreadCount || 0),
      items: Array.isArray(data?.items) ? data.items : []
    }
    unreadCount.value = notificationPage.value.unreadCount
    await nextTick()
    animateListItems()
  } catch (error) {
    console.error('加载通知列表失败:', error)
    ElMessage.error(error?.message || '加载通知列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleTypeChange = (type) => {
  filters.notificationType = type
  refreshNotifications(1)
}

const handleMarkRead = async (notificationId) => {
  if (!notificationId || markingId.value) {
    return
  }
  markingId.value = notificationId
  try {
    await markNotificationAsRead(notificationId)
    ElMessage.success('已将通知标记为已读')
    await Promise.all([refreshNotifications(filters.pageNum), loadUnreadCount()])
  } catch (error) {
    console.error('标记通知已读失败:', error)
    ElMessage.error(error?.message || '标记通知已读失败')
  } finally {
    markingId.value = ''
  }
}

const handleMarkAllRead = async () => {
  if (markingAll.value || unreadCount.value === 0) {
    return
  }
  markingAll.value = true
  try {
    const data = await markAllNotificationsAsRead()
    ElMessage.success(`本次共处理 ${Number(data?.updatedCount || 0)} 条通知`)
    await Promise.all([refreshNotifications(1), loadUnreadCount()])
  } catch (error) {
    console.error('全部标记已读失败:', error)
    ElMessage.error(error?.message || '全部标记已读失败')
  } finally {
    markingAll.value = false
  }
}

const levelClass = (level) => {
  switch (level) {
    case 'important':
      return 'level-important'
    case 'warning':
      return 'level-warning'
    case 'success':
      return 'level-success'
    default:
      return 'level-info'
  }
}

const formatDisplayTime = (value) => {
  if (!value) {
    return '刚刚'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '时间未知'
  }
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const playEnterAnimation = () => {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    gsap.set([heroRef.value, toolbarRef.value, listRef.value], { opacity: 1, y: 0 })
    return
  }

  enterTimeline?.kill()
  enterTimeline = gsap.timeline({ defaults: { ease: 'power3.out' } })
  enterTimeline
    .fromTo(heroRef.value, { opacity: 0, y: 28 }, { opacity: 1, y: 0, duration: 0.6 })
    .fromTo(toolbarRef.value, { opacity: 0, y: 24 }, { opacity: 1, y: 0, duration: 0.45 }, '-=0.3')
    .fromTo(listRef.value, { opacity: 0, y: 24 }, { opacity: 1, y: 0, duration: 0.45 }, '-=0.22')
}

const animateListItems = () => {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    return
  }
  const cards = listRef.value?.querySelectorAll('.notification-card') || []
  if (!cards.length) {
    return
  }
  gsap.fromTo(
    cards,
    { opacity: 0, y: 18, scale: 0.98 },
    { opacity: 1, y: 0, scale: 1, duration: 0.38, ease: 'power2.out', stagger: 0.06 }
  )
}

watch(
  () => route.fullPath,
  async () => {
    await nextTick()
    playEnterAnimation()
  }
)

onMounted(async () => {
  await Promise.all([refreshNotifications(1), loadUnreadCount()])
  await nextTick()
  playEnterAnimation()
})

onUnmounted(() => {
  enterTimeline?.kill()
})
</script>

<style scoped>
.notification-center-page {
  min-height: 100%;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background:
    radial-gradient(circle at top left, rgba(14, 165, 233, 0.12), transparent 34%),
    radial-gradient(circle at 85% 10%, rgba(245, 158, 11, 0.12), transparent 26%),
    linear-gradient(180deg, #f8fcff 0%, #eef6f7 100%);
}

.glass-shell,
.glass-panel {
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid rgba(255, 255, 255, 0.68);
  box-shadow: 0 22px 60px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(18px);
}

.notification-hero,
.notification-toolbar,
.notification-list-shell {
  border-radius: 28px;
}

.notification-hero {
  padding: 28px;
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(300px, 0.9fr);
  gap: 18px;
}

.hero-kicker {
  display: inline-flex;
  margin-bottom: 14px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(15, 118, 110, 0.1);
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-title {
  margin: 0;
  font-size: clamp(30px, 3vw, 42px);
  line-height: 1.06;
  color: #0f172a;
}

.hero-description {
  margin: 14px 0 0;
  max-width: 680px;
  color: #475569;
  font-size: 15px;
  line-height: 1.8;
}

.hero-summary {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}

.summary-card {
  border-radius: 24px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.primary-card {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.16), rgba(255, 255, 255, 0.72));
}

.warm-card {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.16), rgba(255, 255, 255, 0.72));
}

.summary-label,
.summary-hint,
.section-subtitle,
.notification-content,
.notification-extra,
.notification-time {
  color: #64748b;
}

.summary-value {
  font-size: 34px;
  color: #0f172a;
  line-height: 1;
}

.notification-toolbar {
  padding: 18px 20px;
  display: flex;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.type-chip {
  min-height: 40px;
  padding: 0 16px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.76);
  color: #334155;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
}

.type-chip.active {
  color: #ffffff;
  background: linear-gradient(135deg, #0f766e 0%, #0ea5e9 100%);
  border-color: transparent;
  box-shadow: 0 16px 28px rgba(14, 165, 233, 0.22);
}

.control-row {
  margin-left: auto;
}

.toggle-card {
  min-width: 260px;
  padding: 12px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(148, 163, 184, 0.18);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.toggle-copy {
  display: flex;
  flex-direction: column;
  gap: 3px;
  color: #0f172a;
}

.toggle-copy small {
  color: #64748b;
}

.glass-action {
  min-height: 42px;
  padding: 0 18px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.76);
  color: #0f172a;
  font-weight: 700;
}

.glass-action.secondary {
  color: #0f766e;
}

.notification-list-shell {
  padding: 22px;
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.section-title {
  margin: 0;
  color: #0f172a;
  font-size: 22px;
}

.section-subtitle {
  margin: 8px 0 0;
  line-height: 1.7;
}

.notification-list {
  margin-top: 20px;
  display: grid;
  gap: 14px;
}

.notification-card {
  position: relative;
  border-radius: 22px;
  padding: 20px 20px 18px 24px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  overflow: hidden;
}

.notification-card.unread {
  box-shadow: 0 26px 54px rgba(15, 23, 42, 0.12);
}

.notification-accent {
  position: absolute;
  top: 18px;
  left: 0;
  bottom: 18px;
  width: 5px;
  border-radius: 0 999px 999px 0;
}

.level-info .notification-accent { background: #0ea5e9; }
.level-success .notification-accent { background: #10b981; }
.level-warning .notification-accent { background: #f59e0b; }
.level-important .notification-accent { background: #ef4444; }

.notification-meta-row,
.meta-left,
.notification-extra {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.notification-badge {
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
  color: #0f172a;
  font-size: 12px;
  font-weight: 700;
}

.unread-dot {
  color: #ef4444;
  font-size: 12px;
  font-weight: 700;
}

.notification-title {
  margin: 12px 0 8px;
  color: #0f172a;
  font-size: 18px;
}

.notification-content {
  margin: 0;
  line-height: 1.8;
}

.notification-extra {
  margin-top: 12px;
  font-size: 12px;
}

.notification-actions {
  display: flex;
  align-items: center;
}

.read-state {
  color: #10b981;
  font-weight: 700;
}

.state-board {
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  color: #64748b;
}

.pulse-ring {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 2px solid rgba(14, 165, 233, 0.2);
  border-top-color: #0ea5e9;
  animation: spin-ring 0.9s linear infinite;
}

.empty-orbit {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  border: 1px dashed rgba(14, 165, 233, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.orbit-core {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0ea5e9 0%, #0f766e 100%);
  box-shadow: 0 0 24px rgba(14, 165, 233, 0.28);
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@keyframes spin-ring {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 960px) {
  .notification-center-page {
    padding: 16px;
  }

  .notification-hero {
    grid-template-columns: 1fr;
  }

  .notification-card {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .notification-toolbar,
  .section-head {
    flex-direction: column;
    align-items: stretch;
  }

  .control-row {
    margin-left: 0;
  }

  .toggle-card {
    min-width: 100%;
  }
}
</style>