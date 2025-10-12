<template>
  <div class="admin-dashboard-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <font-awesome-icon :icon="['fas', 'tachometer-alt']" />
            管理员控制台
          </h1>
          <p class="page-subtitle">系统概览与管理中心</p>
        </div>
        <div class="header-actions">
          <el-button class="action-btn backup-btn">
            <font-awesome-icon :icon="['fas', 'database']" />
            数据备份
          </el-button>
          <el-button type="primary" class="action-btn settings-btn">
            <font-awesome-icon :icon="['fas', 'cog']" />
            系统设置
          </el-button>
        </div>
      </div>
    </div>

    <!-- 系统概览统计 -->
    <div class="overview-section">
      <div class="overview-grid">
        <div class="overview-card users">
          <div class="card-header">
            <div class="card-icon">
              <font-awesome-icon :icon="['fas', 'users']" />
            </div>
            <div class="card-title">用户统计</div>
          </div>
          <div class="card-content">
            <div class="stat-item">
              <span class="stat-number">{{ overview.totalUsers }}</span>
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
              <span>+{{ overview.userGrowth }}% 本月</span>
            </div>
          </div>
        </div>
        
        <div class="overview-card courses">
          <div class="card-header">
            <div class="card-icon">
              <font-awesome-icon :icon="['fas', 'book']" />
            </div>
            <div class="card-title">课程统计</div>
          </div>
          <div class="card-content">
            <div class="stat-item">
              <span class="stat-number">{{ overview.totalCourses }}</span>
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
              <span>+{{ overview.courseGrowth }}% 本月</span>
            </div>
          </div>
        </div>
        
        <div class="overview-card revenue">
          <div class="card-header">
            <div class="card-icon">
              <font-awesome-icon :icon="['fas', 'chart-line']" />
            </div>
            <div class="card-title">收入统计</div>
          </div>
          <div class="card-content">
            <div class="stat-item">
              <span class="stat-number">¥{{ overview.totalRevenue.toLocaleString() }}</span>
              <span class="stat-label">总收入</span>
            </div>
            <div class="stat-row">
              <div class="stat-item small">
                <span class="stat-number">¥{{ overview.monthlyRevenue.toLocaleString() }}</span>
                <span class="stat-label">本月</span>
              </div>
              <div class="stat-item small">
                <span class="stat-number">¥{{ overview.dailyRevenue.toLocaleString() }}</span>
                <span class="stat-label">今日</span>
              </div>
            </div>
            <div class="growth-indicator positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              <span>+{{ overview.revenueGrowth }}% 本月</span>
            </div>
          </div>
        </div>
        
        <div class="overview-card system">
          <div class="card-header">
            <div class="card-icon">
              <font-awesome-icon :icon="['fas', 'server']" />
            </div>
            <div class="card-title">系统状态</div>
          </div>
          <div class="card-content">
            <div class="system-status">
              <div class="status-item">
                <span class="status-label">服务器状态</span>
                <span class="status-value online">
                  <font-awesome-icon :icon="['fas', 'circle']" />
                  正常
                </span>
              </div>
              <div class="status-item">
                <span class="status-label">数据库</span>
                <span class="status-value online">
                  <font-awesome-icon :icon="['fas', 'circle']" />
                  正常
                </span>
              </div>
              <div class="status-item">
                <span class="status-label">存储空间</span>
                <span class="status-value warning">
                  <font-awesome-icon :icon="['fas', 'circle']" />
                  {{ overview.storageUsage }}%
                </span>
              </div>
            </div>
            <div class="uptime-info">
              <span class="uptime-label">运行时间</span>
              <span class="uptime-value">{{ overview.uptime }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="content-grid">
        <!-- 左侧内容 -->
        <div class="left-content">
          <!-- 最近活动 -->
          <div class="activity-section">
            <div class="section-header">
              <h3 class="section-title">
                <font-awesome-icon :icon="['fas', 'history']" />
                最近活动
              </h3>
              <el-button text class="view-all-btn">
                查看全部
                <font-awesome-icon :icon="['fas', 'arrow-right']" />
              </el-button>
            </div>
            
            <div class="activity-list">
              <div 
                v-for="activity in recentActivities" 
                :key="activity.id"
                class="activity-item"
              >
                <div class="activity-icon">
                  <font-awesome-icon :icon="activity.icon" />
                </div>
                <div class="activity-content">
                  <div class="activity-title">{{ activity.title }}</div>
                  <div class="activity-description">{{ activity.description }}</div>
                  <div class="activity-time">{{ activity.time }}</div>
                </div>
                <div class="activity-status">
                  <span :class="['status-dot', activity.status]"></span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 用户增长趋势 -->
          <div class="chart-section">
            <div class="section-header">
              <h3 class="section-title">
                <font-awesome-icon :icon="['fas', 'chart-area']" />
                用户增长趋势
              </h3>
              <div class="chart-controls">
                <el-radio-group v-model="chartPeriod" size="small">
                  <el-radio-button label="7d">7天</el-radio-button>
                  <el-radio-button label="30d">30天</el-radio-button>
                  <el-radio-button label="90d">90天</el-radio-button>
                </el-radio-group>
              </div>
            </div>
            
            <div class="chart-container">
              <div class="chart-placeholder">
                <i class="fas fa-chart-line"></i>
                <p>用户增长趋势图表</p>
                <small>这里将显示用户增长的可视化图表</small>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧内容 -->
        <div class="right-content">
          <!-- 待处理事项 -->
          <div class="pending-section">
            <div class="section-header">
              <h3 class="section-title">
                <i class="fas fa-tasks"></i>
                待处理事项
              </h3>
              <span class="pending-count">{{ pendingTasks.length }}</span>
            </div>
            
            <div class="pending-list">
              <div 
                v-for="task in pendingTasks" 
                :key="task.id"
                class="pending-item"
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
                  <el-button size="small" type="primary" @click="handleTask(task)">
                    处理
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 系统通知 -->
          <div class="notification-section">
            <div class="section-header">
              <h3 class="section-title">
                <i class="fas fa-bell"></i>
                系统通知
              </h3>
              <el-button text class="mark-read-btn">
                全部已读
              </el-button>
            </div>
            
            <div class="notification-list">
              <div 
                v-for="notification in systemNotifications" 
                :key="notification.id"
                :class="['notification-item', { unread: !notification.read }]"
              >
                <div class="notification-icon">
                  <i :class="notification.icon"></i>
                </div>
                <div class="notification-content">
                  <div class="notification-title">{{ notification.title }}</div>
                  <div class="notification-message">{{ notification.message }}</div>
                  <div class="notification-time">{{ notification.time }}</div>
                </div>
                <div class="notification-actions" v-if="!notification.read">
                  <el-button size="small" text @click="markAsRead(notification)">
                    标记已读
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 快速操作 -->
          <div class="quick-actions-section">
            <div class="section-header">
              <h3 class="section-title">
                <i class="fas fa-bolt"></i>
                快速操作
              </h3>
            </div>
            
            <div class="quick-actions-grid">
              <div class="quick-action-item" @click="navigateTo('/admin/users')">
                <div class="action-icon">
                  <i class="fas fa-user-plus"></i>
                </div>
                <div class="action-title">添加用户</div>
              </div>
              
              <div class="quick-action-item" @click="navigateTo('/admin/categories')">
                <div class="action-icon">
                  <i class="fas fa-tags"></i>
                </div>
                <div class="action-title">管理分类</div>
              </div>
              
              <div class="quick-action-item" @click="showSystemSettings">
                <div class="action-icon">
                  <i class="fas fa-cog"></i>
                </div>
                <div class="action-title">系统设置</div>
              </div>
              
              <div class="quick-action-item" @click="generateReport">
                <div class="action-icon">
                  <font-awesome-icon :icon="['fas', 'chart-bar']" />
                </div>
                <div class="action-title">生成报告</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 系统概览数据
const overview = ref({
  totalUsers: 1248,
  students: 1089,
  teachers: 159,
  userGrowth: 12.5,
  totalCourses: 156,
  activeCourses: 142,
  pendingCourses: 14,
  courseGrowth: 8.3,
  totalRevenue: 2456789,
  monthlyRevenue: 156789,
  dailyRevenue: 5234,
  revenueGrowth: 15.2,
  storageUsage: 78,
  uptime: '15天 8小时 32分钟'
})

// 图表时间周期
const chartPeriod = ref('30d')

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    icon: 'fas fa-user-plus',
    title: '新用户注册',
    description: '张三 注册了新账户',
    time: '2分钟前',
    status: 'success'
  },
  {
    id: 2,
    icon: 'fas fa-book',
    title: '课程发布',
    description: '李老师发布了《Vue.js进阶教程》',
    time: '15分钟前',
    status: 'info'
  },
  {
    id: 3,
    icon: 'fas fa-exclamation-triangle',
    title: '系统警告',
    description: '服务器CPU使用率超过80%',
    time: '1小时前',
    status: 'warning'
  },
  {
    id: 4,
    icon: 'fas fa-dollar-sign',
    title: '收入记录',
    description: '用户购买课程，收入 ¥299',
    time: '2小时前',
    status: 'success'
  },
  {
    id: 5,
    icon: 'fas fa-shield-alt',
    title: '安全事件',
    description: '检测到异常登录尝试',
    time: '3小时前',
    status: 'error'
  }
])

// 待处理事项
const pendingTasks = ref([
  {
    id: 1,
    title: '课程审核',
    description: '《React高级开发》等待审核',
    time: '2小时前',
    priority: 'high'
  },
  {
    id: 2,
    title: '用户申诉',
    description: '用户投诉课程质量问题',
    time: '4小时前',
    priority: 'medium'
  },
  {
    id: 3,
    title: '系统维护',
    description: '定期数据库优化',
    time: '1天前',
    priority: 'low'
  },
  {
    id: 4,
    title: '财务审核',
    description: '教师提现申请审核',
    time: '2天前',
    priority: 'high'
  }
])

// 系统通知
const systemNotifications = ref([
  {
    id: 1,
    icon: 'fas fa-info-circle',
    title: '系统更新',
    message: '系统将于今晚23:00进行例行维护',
    time: '30分钟前',
    read: false
  },
  {
    id: 2,
    icon: 'fas fa-shield-alt',
    title: '安全提醒',
    message: '建议启用双因素认证提高账户安全性',
    time: '2小时前',
    read: false
  },
  {
    id: 3,
    icon: 'fas fa-chart-line',
    title: '数据报告',
    message: '本月数据统计报告已生成',
    time: '1天前',
    read: true
  },
  {
    id: 4,
    icon: 'fas fa-database',
    title: '备份完成',
    message: '数据库备份已成功完成',
    time: '2天前',
    read: true
  }
])

// 获取优先级名称
const getPriorityName = (priority) => {
  const names = {
    high: '高',
    medium: '中',
    low: '低'
  }
  return names[priority] || '未知'
}

// 处理任务
const handleTask = (task) => {
  console.log('处理任务:', task)
}

// 标记为已读
const markAsRead = (notification) => {
  notification.read = true
}

// 导航到指定页面
const navigateTo = (path) => {
  router.push(path)
}

// 显示系统设置
const showSystemSettings = () => {
  console.log('显示系统设置')
}

// 生成报告
const generateReport = () => {
  console.log('生成报告')
}

onMounted(() => {
  // 组件挂载时的初始化逻辑
})
</script>

<style scoped>
.admin-dashboard-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f0f3 0%, #e8e8eb 100%);
  padding: 0;
}

/* 页面头部 */
.page-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 24px 30px;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title i {
  color: #002FA7;
}

.page-subtitle {
  color: #6b7280;
  margin: 0;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.backup-btn {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  color: #6c757d;
}

.settings-btn {
  background: linear-gradient(135deg, #002FA7, #517B4D);
  border: none;
  color: white;
}

/* 系统概览 */
.overview-section {
  padding: 30px;
  padding-bottom: 0;
}

.overview-grid {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.overview-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 
    20px 20px 40px rgba(0, 0, 0, 0.1),
    -20px -20px 40px rgba(255, 255, 255, 0.8),
    inset 0 0 0 1px rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    25px 25px 50px rgba(0, 0, 0, 0.15),
    -25px -25px 50px rgba(255, 255, 255, 0.9);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.overview-card.users .card-icon {
  background: linear-gradient(135deg, #002FA7, #517B4D);
}

.overview-card.courses .card-icon {
  background: linear-gradient(135deg, #28a745, #20c997);
}

.overview-card.revenue .card-icon {
  background: linear-gradient(135deg, #ffc107, #fd7e14);
}

.overview-card.system .card-icon {
  background: linear-gradient(135deg, #6f42c1, #e83e8c);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
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
  color: #2c3e50;
  line-height: 1;
}

.stat-item.small .stat-number {
  font-size: 20px;
}

.stat-label {
  color: #6b7280;
  font-size: 14px;
}

.stat-row {
  display: flex;
  gap: 20px;
}

.growth-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
}

.growth-indicator.positive {
  color: #28a745;
}

.growth-indicator.negative {
  color: #dc3545;
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
  color: #6b7280;
}

.status-value {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
}

.status-value.online {
  color: #28a745;
}

.status-value.warning {
  color: #ffc107;
}

.status-value.error {
  color: #dc3545;
}

.status-value i {
  font-size: 8px;
}

.uptime-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.uptime-label {
  font-size: 14px;
  color: #6b7280;
}

.uptime-value {
  font-size: 14px;
  font-weight: 600;
  color: #002FA7;
}

/* 主要内容区域 */
.main-content {
  padding: 30px;
}

.content-grid {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 30px;
}

/* 通用区块样式 */
.activity-section,
.chart-section,
.pending-section,
.notification-section,
.quick-actions-section {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 
    15px 15px 30px rgba(0, 0, 0, 0.1),
    -15px -15px 30px rgba(255, 255, 255, 0.8);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title i {
  color: #002FA7;
}

.view-all-btn {
  color: #002FA7;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 最近活动 */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  background: rgba(248, 249, 250, 0.5);
  transition: all 0.3s ease;
}

.activity-item:hover {
  background: rgba(248, 249, 250, 0.8);
  transform: translateX(4px);
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #002FA7, #517B4D);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.activity-description {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 4px;
}

.activity-time {
  color: #9ca3af;
  font-size: 12px;
}

.activity-status {
  display: flex;
  align-items: center;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.success {
  background: #28a745;
}

.status-dot.info {
  background: #17a2b8;
}

.status-dot.warning {
  background: #ffc107;
}

.status-dot.error {
  background: #dc3545;
}

/* 图表区域 */
.chart-controls {
  display: flex;
  gap: 8px;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(248, 249, 250, 0.5);
  border-radius: 12px;
  border: 2px dashed #d1d5db;
}

.chart-placeholder {
  text-align: center;
  color: #6b7280;
}

.chart-placeholder i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #d1d5db;
}

.chart-placeholder p {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 8px 0;
}

.chart-placeholder small {
  font-size: 14px;
}

/* 待处理事项 */
.pending-count {
  background: #dc3545;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.pending-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pending-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-radius: 12px;
  background: rgba(248, 249, 250, 0.5);
  transition: all 0.3s ease;
}

.pending-item:hover {
  background: rgba(248, 249, 250, 0.8);
}

.pending-content {
  flex: 1;
}

.pending-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.pending-description {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 8px;
}

.pending-meta {
  display: flex;
  gap: 12px;
  align-items: center;
}

.pending-time {
  color: #9ca3af;
  font-size: 12px;
}

.pending-priority {
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 500;
}

.pending-priority.high {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.pending-priority.medium {
  background: rgba(255, 193, 7, 0.1);
  color: #ffc107;
}

.pending-priority.low {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

/* 系统通知 */
.mark-read-btn {
  color: #002FA7;
  font-size: 14px;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  background: rgba(248, 249, 250, 0.5);
  transition: all 0.3s ease;
}

.notification-item.unread {
  background: rgba(0, 47, 167, 0.05);
  border-left: 3px solid #002FA7;
}

.notification-item:hover {
  background: rgba(248, 249, 250, 0.8);
}

.notification-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #002FA7, #517B4D);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.notification-message {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 4px;
}

.notification-time {
  color: #9ca3af;
  font-size: 12px;
}

.notification-actions {
  display: flex;
  align-items: center;
}

/* 快速操作 */
.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-radius: 12px;
  background: rgba(248, 249, 250, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
}

.quick-action-item:hover {
  background: rgba(0, 47, 167, 0.05);
  transform: translateY(-2px);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #002FA7, #517B4D);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.action-title {
  font-weight: 500;
  color: #2c3e50;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .overview-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 20px 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .overview-section,
  .main-content {
    padding: 20px 16px;
  }
  
  .overview-grid {
    grid-template-columns: 1fr;
  }
  
  .content-grid {
    gap: 20px;
  }
  
  .activity-section,
  .chart-section,
  .pending-section,
  .notification-section,
  .quick-actions-section {
    margin-bottom: 20px;
  }
  
  .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .chart-container {
    height: 200px;
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