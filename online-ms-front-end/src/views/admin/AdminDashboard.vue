<template>
  <div class="admin-dashboard-container">
    <!-- 页面头部 -->
    <div class="page-header glass-card" ref="headerRef">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <div class="icon-wrapper">
              <font-awesome-icon :icon="['fas', 'tachometer-alt']" />
            </div>
            管理员控制台
          </h1>
          <p class="page-subtitle">系统运行状态与数据中枢</p>
        </div>
        <div class="header-actions">
          <el-button class="action-btn glass-btn" @click="generateReport">
            <font-awesome-icon :icon="['fas', 'database']" />
            数据备份
          </el-button>
          <el-button type="primary" class="action-btn tech-btn" @click="showSystemSettings">
            <font-awesome-icon :icon="['fas', 'cog']" />
            系统设置
          </el-button>
        </div>
      </div>
    </div>

    <!-- 系统概览统计 -->
    <div class="overview-section">
      <div class="overview-grid" ref="overviewRef">
        <div class="overview-card glass-card users hover-float">
          <div class="card-header">
            <div class="card-icon gradient-blue">
              <font-awesome-icon :icon="['fas', 'users']" />
            </div>
            <div class="card-title">用户统计</div>
          </div>
          <div class="card-content">
            <div class="stat-item">
              <span class="stat-number text-gradient-blue">{{ overview.totalUsers }}</span>
              <span class="stat-label">总用户数</span>
            </div>
            <div class="stat-row">
              <div class="stat-item small">
                <span class="stat-number">{{ overview.students }}</span>
                <span class="stat-label">学生</span>
              </div>
              <div class="stat-item small">
                <span class="stat-number">{{ overview.teachers }}</span>
                <span class="stat-label">教师</span>
              </div>
            </div>
            <div class="growth-indicator positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              <span>有效占比 {{ overview.userGrowth }}%</span>
            </div>
          </div>
        </div>
        
        <div class="overview-card glass-card courses hover-float">
          <div class="card-header">
            <div class="card-icon gradient-green">
              <font-awesome-icon :icon="['fas', 'book']" />
            </div>
            <div class="card-title">课程统计</div>
          </div>
          <div class="card-content">
            <div class="stat-item">
              <span class="stat-number text-gradient-green">{{ overview.totalCourses }}</span>
              <span class="stat-label">总课程数</span>
            </div>
            <div class="stat-row">
              <div class="stat-item small">
                <span class="stat-number">{{ overview.activeCourses }}</span>
                <span class="stat-label">活跃</span>
              </div>
              <div class="stat-item small">
                <span class="stat-number">{{ overview.pendingCourses }}</span>
                <span class="stat-label">待审核</span>
              </div>
            </div>
            <div class="growth-indicator positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              <span>发布占比 {{ overview.courseGrowth }}%</span>
            </div>
          </div>
        </div>
        
        
        
        <div class="overview-card glass-card system hover-float">
          <div class="card-header">
            <div class="card-icon gradient-purple">
              <font-awesome-icon :icon="['fas', 'server']" />
            </div>
            <div class="card-title">系统状态</div>
          </div>
          <div class="card-content">
            <div class="system-status">
              <div class="status-item">
                <span class="status-label">服务器状态</span>
                <span class="status-value online">
                  <span class="pulse-dot"></span>
                  正常
                </span>
              </div>
              <div class="status-item">
                <span class="status-label">数据库</span>
                <span class="status-value online">
                  <span class="pulse-dot"></span>
                  正常
                </span>
              </div>
              <div class="status-item">
                <span class="status-label">审计失败率</span>
                <span class="status-value warning">
                  <span class="pulse-dot warning"></span>
                  {{ overview.storageUsage }}%
                </span>
              </div>
            </div>
            <div class="uptime-info">
              <span class="uptime-label">最新公告</span>
              <span class="uptime-value">{{ overview.uptime }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="content-grid" ref="contentGridRef">
        <!-- 左侧内容 -->
        <div class="left-content">
          <!-- 用户增长趋势 -->
          <div class="chart-section glass-card gs-card">
            <div class="section-header">
              <h3 class="section-title">
                <div class="title-icon"><font-awesome-icon :icon="['fas', 'chart-area']" /></div>
                用户增长趋势
              </h3>
              <div class="chart-controls">
                <el-radio-group v-model="chartPeriod" size="small" class="tech-radio">
                  <el-radio-button label="7d">7天</el-radio-button>
                  <el-radio-button label="30d">30天</el-radio-button>
                  <el-radio-button label="90d">90天</el-radio-button>
                </el-radio-group>
              </div>
            </div>
            
            <div class="chart-container">
              <div v-if="dashboardLoading" class="chart-placeholder">
                <div class="hologram-effect">
                  <font-awesome-icon :icon="['fas', 'chart-line']" />
                </div>
                <p>数据加载中...</p>
                <small>正在同步管理员概览数据</small>
              </div>
              <div v-else class="trend-visual-card">
                <div class="trend-metrics">
                  <div class="metric-chip">
                    <span>新增用户</span>
                    <strong>{{ trendSummary.latestUsers }}</strong>
                  </div>
                  <div class="metric-chip">
                    <span>新增课程</span>
                    <strong>{{ trendSummary.latestCourses }}</strong>
                  </div>
                  <div class="metric-chip">
                    <span>累计用户</span>
                    <strong>{{ trendSummary.cumulativeUsers }}</strong>
                  </div>
                </div>
                <svg
                  class="trend-chart"
                  :viewBox="`0 0 ${chartWidth} ${chartHeight}`"
                  role="img"
                  aria-label="管理员用户增长趋势图"
                >
                  <defs>
                    <linearGradient id="userTrendGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                      <stop offset="0%" stop-color="#002FA7" />
                      <stop offset="100%" stop-color="#60efff" />
                    </linearGradient>
                    <linearGradient id="courseTrendGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                      <stop offset="0%" stop-color="#517B4D" />
                      <stop offset="100%" stop-color="#67C23A" />
                    </linearGradient>
                  </defs>
                  <g v-for="item in 4" :key="item">
                    <line
                      :x1="chartPadding"
                      :x2="chartWidth - chartPadding"
                      :y1="chartPadding + ((chartHeight - chartPadding * 2) / 3) * (item - 1)"
                      :y2="chartPadding + ((chartHeight - chartPadding * 2) / 3) * (item - 1)"
                      class="chart-grid-line"
                    />
                  </g>
                  <path :d="userTrendPath" class="trend-path user-path" />
                  <path :d="courseTrendPath" class="trend-path course-path" />
                  <g v-for="(point, index) in trendPoints" :key="`${point.label}-${index}`">
                    <text
                      :x="chartPadding + ((chartWidth - chartPadding * 2) / Math.max(trendPoints.length - 1, 1)) * index"
                      :y="chartHeight - 8"
                      class="chart-axis-text"
                      text-anchor="middle"
                    >
                      {{ point.label }}
                    </text>
                  </g>
                </svg>
                <div class="trend-legend">
                  <span class="legend-item"><i class="legend-dot user-dot"></i>新增用户</span>
                  <span class="legend-item"><i class="legend-dot course-dot"></i>新增课程</span>
                </div>
              </div>
            </div>
            <div class="system-status-grid">
              <div
                v-for="statusItem in systemStatuses"
                :key="statusItem.code"
                class="system-status-item"
              >
                <div class="system-status-head">
                  <span class="system-status-label">{{ statusItem.label }}</span>
                  <el-tag size="small" effect="dark" :type="systemStatusType(statusItem.status)">
                    {{ statusItem.status === 'danger' ? '异常' : statusItem.status === 'warning' ? '关注' : '正常' }}
                  </el-tag>
                </div>
                <strong class="system-status-value">{{ statusItem.value }}</strong>
                <span class="system-status-desc">{{ statusItem.description }}</span>
              </div>
            </div>
          </div>

          <!-- 最近活动 -->
          <div class="activity-section glass-card gs-card mt-lg">
            <div class="section-header">
              <h3 class="section-title">
                <div class="title-icon"><font-awesome-icon :icon="['fas', 'history']" /></div>
                最近活动
              </h3>
              <el-button text class="view-all-btn tech-text-btn">
                查看全部
                <font-awesome-icon :icon="['fas', 'arrow-right']" class="ml-xs" />
              </el-button>
            </div>
            
            <div class="activity-list">
              <div 
                v-for="activity in recentActivities" 
                :key="activity.id"
                class="activity-item tech-list-item"
              >
                <div class="activity-icon">
                  <font-awesome-icon :icon="activity.icon.replace('fas ', '').replace('fa-', '')" />
                </div>
                <div class="activity-content">
                  <div class="activity-title">{{ activity.title }}</div>
                  <div class="activity-description">{{ activity.description }}</div>
                  <div class="activity-time">{{ activity.time }}</div>
                </div>
                <div class="activity-status">
                  <span :class="['status-ring', activity.status]"></span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧内容 -->
        <div class="right-content">
          <!-- 快速操作 -->
          <div class="quick-actions-section glass-card gs-card mb-lg">
            <div class="section-header">
              <h3 class="section-title">
                <div class="title-icon"><font-awesome-icon :icon="['fas', 'bolt']" /></div>
                快速操作
              </h3>
            </div>
            
            <div class="quick-actions-grid">
              <div class="quick-action-item tech-btn-card" @click="navigateTo('/admin/users')">
                <div class="action-icon">
                  <font-awesome-icon :icon="['fas', 'user-plus']" />
                </div>
                <div class="action-title">添加用户</div>
              </div>
              
              <div class="quick-action-item tech-btn-card" @click="navigateTo('/admin/categories')">
                <div class="action-icon">
                  <font-awesome-icon :icon="['fas', 'tags']" />
                </div>
                <div class="action-title">管理分类</div>
              </div>

              <div class="quick-action-item tech-btn-card" @click="navigateTo('/admin/course-review')">
                <div class="action-icon">
                  <font-awesome-icon :icon="['fas', 'clipboard-check']" />
                </div>
                <div class="action-title">课程审核台</div>
              </div>
              
              <div class="quick-action-item tech-btn-card" @click="showSystemSettings">
                <div class="action-icon">
                  <font-awesome-icon :icon="['fas', 'cog']" />
                </div>
                <div class="action-title">系统设置</div>
              </div>
              
              <div class="quick-action-item tech-btn-card" @click="generateReport">
                <div class="action-icon">
                  <font-awesome-icon :icon="['fas', 'chart-bar']" />
                </div>
                <div class="action-title">生成报告</div>
              </div>
            </div>
          </div>

          <!-- 待处理事项 -->
          <div class="pending-section glass-card gs-card mb-lg">
            <div class="section-header">
              <h3 class="section-title">
                <div class="title-icon"><font-awesome-icon :icon="['fas', 'tasks']" /></div>
                待处理事项
              </h3>
              <span class="pending-count pulse-badge">{{ pendingTasks.length }}</span>
            </div>
            
            <div class="pending-list">
              <div 
                v-for="task in pendingTasks" 
                :key="task.id"
                class="pending-item tech-list-item"
              >
                <div class="pending-content">
                  <div class="pending-title">{{ task.title }}</div>
                  <div class="pending-description">{{ task.description }}</div>
                  <div class="pending-meta">
                    <span class="pending-time">{{ task.time }}</span>
                    <span :class="['pending-priority', task.priority]">{{ getPriorityName(task.priority) }}</span>
                  </div>
                </div>
                <div class="pending-actions">
                  <el-button size="small" class="tech-small-btn" @click="handleTask(task)">
                    处理
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 系统通知 -->
          <div class="notification-section glass-card gs-card">
            <div class="section-header">
              <h3 class="section-title">
                <div class="title-icon"><font-awesome-icon :icon="['fas', 'bell']" /></div>
                系统通知
              </h3>
              <el-button text class="mark-read-btn tech-text-btn" @click="systemNotifications.forEach(markAsRead)">
                全部已读
              </el-button>
            </div>
            
            <div class="notification-list">
              <div 
                v-for="notification in systemNotifications" 
                :key="notification.id"
                :class="['notification-item tech-list-item', { unread: !notification.read }]"
              >
                <div class="notification-icon">
                  <font-awesome-icon :icon="notification.icon.replace('fas ', '').replace('fa-', '')" />
                </div>
                <div class="notification-content">
                  <div class="notification-title">{{ notification.title }}</div>
                  <div class="notification-message">{{ notification.message }}</div>
                  <div class="notification-time">{{ notification.time }}</div>
                </div>
                <div class="notification-actions" v-if="!notification.read">
                  <el-button size="small" text class="tech-text-btn" @click="markAsRead(notification)">
                    标记已读
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { gsap } from 'gsap'
import {
  getAdminDashboardOverview
} from '@/api/admin/adminManagement'

const router = useRouter()

const headerRef = ref(null)
const overviewRef = ref(null)
const contentGridRef = ref(null)
let tl = null
const dashboardLoading = ref(false)

/**
 * 管理员首页概览数据。
 * 所有数值都尽量来自后端实时接口，避免继续展示硬编码示例。
 */
const overview = ref({
  totalUsers: 0,
  students: 0,
  teachers: 0,
  userGrowth: 0,
  totalCourses: 0,
  activeCourses: 0,
  pendingCourses: 0,
  courseGrowth: 0,
  storageUsage: 0,
  uptime: '暂无公告'
})

const chartPeriod = ref('30d')
const trendPoints = ref([])
const systemStatuses = ref([])
const recentActivities = ref([])
const pendingTasks = ref([])
const systemNotifications = ref([])

const trendDaysMap = {
  '7d': 7,
  '30d': 30,
  '90d': 90
}

const loadDashboardData = async () => {
  dashboardLoading.value = true
  try {
    const response = await getAdminDashboardOverview({ days: trendDaysMap[chartPeriod.value] || 30 })
    overview.value = response?.overview || overview.value
    trendPoints.value = Array.isArray(response?.userGrowthTrend) ? response.userGrowthTrend : []
    systemStatuses.value = Array.isArray(response?.systemStatuses) ? response.systemStatuses : []
    recentActivities.value = Array.isArray(response?.recentActivities) ? response.recentActivities : []
    pendingTasks.value = Array.isArray(response?.pendingTasks) ? response.pendingTasks : []
    systemNotifications.value = Array.isArray(response?.systemNotifications) ? response.systemNotifications : []
  } catch (error) {
    console.error('管理员首页数据加载失败:', error)
    ElMessage.error('管理员首页数据加载失败，请稍后重试')
  } finally {
    dashboardLoading.value = false
  }
}

const chartWidth = 720
const chartHeight = 240
const chartPadding = 28

const currentTrendMax = computed(() => {
  const values = trendPoints.value.flatMap(item => [item.newUsers || 0, item.newCourses || 0, item.cumulativeUsers || 0])
  return Math.max(...values, 1)
})

const createLinePath = (key) => {
  if (!trendPoints.value.length) {
    return ''
  }
  const stepX = trendPoints.value.length > 1 ? (chartWidth - chartPadding * 2) / (trendPoints.value.length - 1) : 0
  return trendPoints.value.map((point, index) => {
    const x = chartPadding + stepX * index
    const y = chartHeight - chartPadding - ((point[key] || 0) / currentTrendMax.value) * (chartHeight - chartPadding * 2)
    return `${index === 0 ? 'M' : 'L'} ${x} ${y}`
  }).join(' ')
}

const userTrendPath = computed(() => createLinePath('newUsers'))
const courseTrendPath = computed(() => createLinePath('newCourses'))
const trendSummary = computed(() => {
  const latestPoint = trendPoints.value[trendPoints.value.length - 1]
  return {
    cumulativeUsers: latestPoint?.cumulativeUsers || 0,
    latestUsers: latestPoint?.newUsers || 0,
    latestCourses: latestPoint?.newCourses || 0
  }
})

const systemStatusType = (status) => {
  if (status === 'danger') {
    return 'danger'
  }
  if (status === 'warning') {
    return 'warning'
  }
  return 'success'
}

const getPriorityName = (priority) => {
  const names = { high: '高', medium: '中', low: '低' }
  return names[priority] || '未知'
}

const handleTask = (task) => {
  if (task.actionType === 'course') {
    navigateTo('/admin/course-review')
    return
  }
  if (task.actionType === 'audit') {
    navigateTo('/admin/audit-logs')
    return
  }
  if (task.actionType === 'user') {
    navigateTo('/admin/users')
  }
}

const markAsRead = (notification) => {
  notification.read = true
}

const navigateTo = (path) => {
  router.push(path)
}

const showSystemSettings = () => {
  ElMessage.info('当前版本尚未提供独立系统设置页面，已为你保留管理入口')
}

const generateReport = () => {
  const reportContent = JSON.stringify({
    generatedAt: new Date().toISOString(),
    overview: overview.value,
    recentActivities: recentActivities.value,
    pendingTasks: pendingTasks.value,
    systemNotifications: systemNotifications.value
  }, null, 2)
  const blob = new Blob([reportContent], { type: 'application/json;charset=utf-8' })
  const objectUrl = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = objectUrl
  anchor.download = `admin-dashboard-report-${Date.now()}.json`
  anchor.click()
  URL.revokeObjectURL(objectUrl)
  ElMessage.success('管理员首页数据报告已导出')
}

onMounted(async () => {
  await loadDashboardData()

  tl = gsap.timeline()
  tl.from(headerRef.value, {
    y: -20,
    opacity: 0,
    duration: 0.6,
    ease: 'power2.out'
  })

  if (overviewRef.value) {
    tl.from(overviewRef.value.children, {
      y: 30,
      opacity: 0,
      duration: 0.5,
      stagger: 0.1,
      ease: 'back.out(1.2)'
    }, '-=0.2')
  }

  if (contentGridRef.value) {
    const cards = contentGridRef.value.querySelectorAll('.gs-card')
    if (cards.length > 0) {
      tl.from(cards, {
        y: 20,
        opacity: 0,
        duration: 0.5,
        stagger: 0.1,
        ease: 'power2.out'
      }, '-=0.2')
    }
  }
})

watch(chartPeriod, async () => {
  await loadDashboardData()
})

onUnmounted(() => {
  if (tl) tl.kill()
})
</script>

<style scoped>
.admin-dashboard-container {
  padding: 0;
  color: #334155;
  font-family: 'Inter', 'PingFang SC', sans-serif;
}

/* 全局 Glassmorphism 卡片样式 */
.glass-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}

.hover-float {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.hover-float:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 97, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

/* 渐变色定义 */
.text-gradient-blue {
  background: linear-gradient(135deg, #0061ff 0%, #60efff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.text-gradient-green {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.text-gradient-orange {
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.gradient-blue { background: linear-gradient(135deg, #0061ff 0%, #60efff 100%); }
.gradient-green { background: linear-gradient(135deg, #10b981 0%, #34d399 100%); }
.gradient-orange { background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%); }
.gradient-purple { background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%); }

/* 页面头部 */
.page-header {
  padding: 24px 30px;
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(0, 97, 255, 0.1);
  color: #0061ff;
}

.page-subtitle {
  color: #64748b;
  margin: 0;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 16px;
}

.action-btn {
  padding: 10px 20px;
  border-radius: 12px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  border: none;
}

.glass-btn {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.6);
  color: #475569;
  transition: all 0.3s ease;
}

.glass-btn:hover {
  background: rgba(255, 255, 255, 0.8);
  color: #0061ff;
}

.tech-btn {
  background: linear-gradient(135deg, #0061ff 0%, #60efff 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(0, 97, 255, 0.3);
  transition: all 0.3s ease;
}

.tech-btn:hover {
  box-shadow: 0 6px 20px rgba(0, 97, 255, 0.4);
  transform: translateY(-2px);
}

/* 系统概览 */
.overview-section {
  margin-bottom: 24px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
}

.overview-card {
  padding: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #334155;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item.small {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.stat-item.small .stat-number {
  font-size: 20px;
  color: #1e293b;
}

.stat-label {
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
}

.stat-row {
  display: flex;
  gap: 20px;
}

.growth-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 20px;
  background: rgba(16, 185, 129, 0.1);
  width: fit-content;
}

.growth-indicator.positive {
  color: #10b981;
}

.growth-indicator.negative {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.system-status {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-label {
  font-size: 14px;
  color: #64748b;
}

.status-value {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
}

.status-value.online { color: #10b981; }
.status-value.warning { color: #f59e0b; }
.status-value.error { color: #ef4444; }

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 8px #10b981;
  animation: pulse-glow 2s infinite;
}

.pulse-dot.warning {
  background: #f59e0b;
  box-shadow: 0 0 8px #f59e0b;
}

.uptime-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.uptime-label {
  font-size: 14px;
  color: #64748b;
}

.uptime-value {
  font-size: 14px;
  font-weight: 600;
  color: #0061ff;
}

/* 主要内容区域 */
.content-grid {
  margin-top: 2.5rem;
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

/* 通用区块样式 */
.chart-section,
.activity-section,
.pending-section,
.notification-section,
.quick-actions-section {
  padding: 24px;
}

.mt-lg { margin-top: 24px; }
.mb-lg { margin-bottom: 24px; }

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(0, 97, 255, 0.1);
  color: #0061ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.tech-text-btn {
  color: #0061ff;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.tech-text-btn:hover {
  color: #60efff;
}

/* 列表项样式 */
.tech-list-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
  margin-bottom: 12px;
}

.tech-list-item:last-child {
  margin-bottom: 0;
}

.tech-list-item:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: translateX(4px);
  border-color: rgba(0, 97, 255, 0.2);
}

/* 最近活动 */
.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(0, 97, 255, 0.1), rgba(96, 239, 255, 0.1));
  color: #0061ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
  font-size: 15px;
}

.activity-description {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 4px;
}

.activity-time {
  color: #94a3b8;
  font-size: 12px;
}

.status-ring {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid;
}

.status-ring.success { border-color: #10b981; }
.status-ring.info { border-color: #0061ff; }
.status-ring.warning { border-color: #f59e0b; }
.status-ring.error { border-color: #ef4444; }

/* 图表区域 */
.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  position: relative;
  overflow: hidden;
}

.chart-placeholder {
  text-align: center;
  color: #64748b;
  z-index: 1;
}

.hologram-effect {
  font-size: 48px;
  margin-bottom: 16px;
  color: #0061ff;
  opacity: 0.5;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
  100% { transform: translateY(0px); }
}

.chart-placeholder p {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #334155;
}

.chart-placeholder small {
  font-size: 13px;
}

.trend-visual-card {
  width: 100%;
  height: 100%;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.trend-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.metric-chip {
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(255, 255, 255, 0.48);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.05);
}

.metric-chip span {
  display: block;
  color: #64748b;
  font-size: 12px;
  margin-bottom: 6px;
}

.metric-chip strong {
  color: #0f172a;
  font-size: 20px;
}

.trend-chart {
  width: 100%;
  flex: 1;
  min-height: 180px;
}

.chart-grid-line {
  stroke: rgba(148, 163, 184, 0.2);
  stroke-width: 1;
}

.trend-path {
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 4;
}

.user-path {
  stroke: url(#userTrendGradient);
}

.course-path {
  stroke: url(#courseTrendGradient);
}

.chart-axis-text {
  fill: #64748b;
  font-size: 12px;
}

.trend-legend {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #475569;
  font-size: 13px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.user-dot {
  background: #002fa7;
}

.course-dot {
  background: #67c23a;
}

.system-status-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.system-status-item {
  padding: 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.46);
}

.system-status-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.system-status-label {
  color: #64748b;
  font-size: 13px;
}

.system-status-value {
  display: block;
  margin-top: 8px;
  color: #0f172a;
  font-size: 16px;
}

.system-status-desc {
  display: block;
  margin-top: 6px;
  color: #94a3b8;
  font-size: 12px;
  line-height: 1.5;
}

/* 待处理事项 */
.pulse-badge {
  background: linear-gradient(135deg, #ef4444, #f87171);
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 0 10px rgba(239, 68, 68, 0.4);
}

.pending-content {
  flex: 1;
}

.pending-title {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
  font-size: 15px;
}

.pending-description {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 8px;
}

.pending-meta {
  display: flex;
  gap: 12px;
  align-items: center;
}

.pending-time {
  color: #94a3b8;
  font-size: 12px;
}

.pending-priority {
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 600;
}

.pending-priority.high {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.pending-priority.medium {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.pending-priority.low {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.tech-small-btn {
  background: rgba(0, 97, 255, 0.1);
  border: none;
  color: #0061ff;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.tech-small-btn:hover {
  background: #0061ff;
  color: white;
}

/* 系统通知 */
.notification-item.unread {
  background: rgba(0, 97, 255, 0.05);
  border-left: 3px solid #0061ff;
}

.notification-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #0061ff, #60efff);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(0, 97, 255, 0.2);
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
  font-size: 14px;
}

.notification-message {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 4px;
}

.notification-time {
  color: #94a3b8;
  font-size: 12px;
}

/* 快速操作 */
.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.tech-btn-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
}

.tech-btn-card:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 97, 255, 0.1);
  border-color: rgba(0, 97, 255, 0.2);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #0061ff, #60efff);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(0, 97, 255, 0.3);
}

.action-title {
  font-weight: 600;
  color: #334155;
  text-align: center;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .system-status-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 20px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .chart-container {
    height: auto;
    min-height: 220px;
  }

  .trend-metrics {
    grid-template-columns: 1fr;
  }
  
  .quick-actions-grid {
    grid-template-columns: 1fr;
  }
  
  .pending-item {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .pending-meta {
    justify-content: space-between;
  }
}
</style>
