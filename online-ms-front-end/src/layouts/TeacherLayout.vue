<template>
  <div class="teacher-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar glass-sidebar" :class="{ 'open': sidebarOpen }">
      <div class="sidebar-header">
        <div class="logo flex-center">
          <img src="/DaoX_C7-Center_Logo.svg" alt="DaoX Logo" class="mr-sm" style="width: 32px; height: 32px; object-fit: contain;" />
          <h2 class="text-lg font-bold text-primary">教师工作台</h2>
        </div>
      </div>

      <nav class="sidebar-nav mt-lg">
        <ul class="nav-list">
          <li class="nav-item">
            <router-link to="/teacher" class="nav-link" exact-active-class="active">
              <font-awesome-icon :icon="['fas', 'tachometer-alt']"/>
              <span>工作台</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/teacher/courses" class="nav-link" exact-active-class="active">
              <font-awesome-icon :icon="['fas', 'book-open']"/>
              <span>课程管理</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/teacher/questions" class="nav-link" exact-active-class="active">
              <font-awesome-icon :icon="['fas', 'question-circle']"/>
              <span>题库管理</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/teacher/assessments" class="nav-link" exact-active-class="active">
              <font-awesome-icon :icon="['fas', 'clipboard-list']"/>
              <span>考试管理</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/teacher/grading" class="nav-link" exact-active-class="active">
              <font-awesome-icon :icon="['fas', 'graduation-cap']"/>
              <span>阅卷中心</span>
            </router-link>
          </li>
        </ul>
      </nav>
    </aside>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <header class="navbar glass-navbar">
        <div class="navbar-left flex">
          <button class="menu-toggle neumorphism-button" @click="toggleSidebar">
            <font-awesome-icon :icon="['fas', 'bars']"/>
          </button>
          <div class="breadcrumb ml-lg">
            <span class="text-muted">{{ currentPageTitle }}</span>
          </div>
        </div>

        <div class="navbar-right flex">

          <div class="notifications mr-md">
            <el-badge :value="3" class="notification-badge"  @click="toggleNotifications">
              <font-awesome-icon :icon="['fas', 'bell']" class="text-lg cursor-pointer"/>
            </el-badge>
          </div>

          <div class="user-menu">
            <el-dropdown @command="handleUserCommand">
              <div class="user-avatar flex-center">
                <div class="avatar-placeholder" v-if="!teacherInfo.avatar">
                  <font-awesome-icon :icon="['fas', 'user-circle']"/>
                </div>
                <img v-else :src="teacherInfo.avatar" :alt="teacherInfo.name" class="user-avatar-img"/>
                <span class="username ml-sm">{{ teacherInfo.name }}</span>
                <font-awesome-icon :icon="['fas', 'chevron-down']" class="ml-sm"/>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <font-awesome-icon :icon="['fas', 'user']" class="mr-sm"/>
                    个人资料
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <font-awesome-icon :icon="['fas', 'cog']" class="mr-sm"/>
                    设置
                  </el-dropdown-item>
                  <el-dropdown-item command="analytics">
                    <font-awesome-icon :icon="['fas', 'chart-bar']" class="mr-sm"/>
                    教学统计
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <font-awesome-icon :icon="['fas', 'sign-out-alt']" class="mr-sm"/>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="page-content">
        <router-view/>
      </main>
    </div>

    <!-- 移动端遮罩 -->
    <div
        v-if="sidebarOpen"
        class="sidebar-overlay show"
        @click="closeSidebar"
    ></div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {logout} from '@/api/index.js'
import {ElMessage} from 'element-plus'
import {getIdentifier} from '@/utils/tokenAnalysis.js'
import {getTeacherProfile} from '@/api/teacher/teacherAPI.js'

const route = useRoute()
const router = useRouter()
const sidebarOpen = ref(false)

// 教师信息
const teacherInfo = ref({
  id: null,
  name: '张老师',
  avatar: null,
})

// 页面标题映射
const pageTitles = {
  'TeacherDashboard': '教师工作台',
  'TeacherCourses': '课程管理',
  'CourseEdit': '编辑课程',
  'QuestionBank': '题库管理',
  'AssessmentManagement': '考试管理',
  'GradingCenter': '阅卷中心'
}

const currentPageTitle = computed(() => {
  return pageTitles[route.name] || '教师工作台'
})

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const closeSidebar = () => {
  sidebarOpen.value = false
}

// 加载用户数据
const loadUserProfile = async () => {
  try {
    const identifier = getIdentifier()
    if (!identifier) {
      ElMessage.error('无法获取用户标识，请重新登录')
      return
    }

    getTeacherProfile(
        identifier,
        (data) => {
          // 成功回调
          if (data) {
            // 直接更新reactive对象的属性
            Object.assign(teacherInfo.value, {
              id: data.id,
              name: data.nickname || teacherInfo.value.name,
              avatar: data.avatarUrl || teacherInfo.value.avatar,
            })
            ElMessage.success('个人信息加载成功')
          }
        },
        (error) => {
          // 失败回调
          console.error('获取个人信息失败:', error)
          ElMessage.error('获取个人信息失败，请稍后重试')
        }
    )
  } catch (error) {
    console.error('加载用户数据时发生错误:', error)
    ElMessage.error('加载用户数据失败')
  }
}

const toggleNotifications = () => {
  // 跳转到聊天页面
  router.push('/teacher/chat')
}

// 处理用户菜单命令
const handleUserCommand = (command) => {
  switch (command) {
    case 'logout':
      handleLogout()
      break
    case 'profile':
      // 跳转到个人资料页面
      router.push('/teacher/profile')
      break
    case 'settings':
      // 跳转到设置页面
      router.push('/teacher/settings')
      break
    case 'analytics':
      // 跳转到教学统计页面
      router.push('/teacher/analytics')
      break
    default:
      console.log('未知命令:', command)
  }
}

// 退出登录处理
const handleLogout = () => {
  logout(
      () => {
        // 登出成功回调
        router.push('/login')
      },
      (message, code) => {
        // 登出失败回调
        console.error('登出失败:', message, code)
        // 即使失败也跳转到登录页
        router.push('/login')
      }
  )
}

// 组件挂载时加载用户数据
onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
/* CSS变量定义 */
:root {
  /* 新拟态设计颜色 */
  --neumorphism-bg: #f0f0f3;
  --neumorphism-shadow-light: #ffffff;
  --neumorphism-shadow-dark: #d1d1d4;

  /* 磨砂玻璃效果 */
  --glass-bg: rgba(255, 255, 255, 0.25);
  --glass-border: rgba(255, 255, 255, 0.18);
  --glass-backdrop: blur(10px);

  /* 主题色彩 */
  --primary-color: #002FA7;
  --secondary-color: #517B4D;
  --success-color: #67C23A;
  --warning-color: #E6A23C;
  --danger-color: #F56C6C;
  --info-color: #909399;

  /* 文字颜色 */
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-auxiliary: #909399;

  /* 间距和圆角 */
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;
  --border-radius-sm: 8px;
  --border-radius-md: 12px;
  --border-radius-lg: 16px;

  /* 过渡效果 */
  --transition-fast: 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-normal: 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-slow: 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.teacher-layout {
  display: flex;
  min-height: 100vh;
  background: var(--neumorphism-bg);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 侧边栏样式 - 磨砂玻璃效果 */
.sidebar {
  width: 250px;
  z-index: 200;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-backdrop);
  -webkit-backdrop-filter: var(--glass-backdrop);
  border-right: 1px solid var(--glass-border);
  transition: transform var(--transition-normal);
  position: relative;
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg,
  rgba(255, 255, 255, 0.1) 0%,
  rgba(255, 255, 255, 0.05) 100%);
  pointer-events: none;
}

.sidebar-header {
  padding: var(--spacing-lg) 0;
  border-bottom: 1px solid var(--glass-border);
  position: relative;
  z-index: 1;
}

.logo {
  padding: 0 var(--spacing-md);
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-list {
  list-style: none;
  padding: var(--spacing-md) 0;
  margin: 0;
}

.nav-item {
  margin-bottom: var(--spacing-md);
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  position: relative;
  margin-bottom: 15px;
  margin-top: 10px;
  z-index: 1;
}

/* 右侧操作区域样式 */
.navbar-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  position: relative;
  z-index: 1;
  /*右对齐*/
  justify-content: flex-end;
}

/* 导航链接 - 新拟态效果 */
.nav-link {
  display: flex;
  align-items: center;
  padding: var(--spacing-md);
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: var(--border-radius-md);
  margin: 0 var(--spacing-sm);
  transition: all var(--transition-normal);
  background: var(--neumorphism-bg);
  box-shadow: 4px 4px 8px var(--neumorphism-shadow-dark),
  -4px -4px 8px var(--neumorphism-shadow-light);
  position: relative;
  overflow: hidden;
}

.nav-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
  transparent,
  rgba(255, 255, 255, 0.2),
  transparent);
  transition: left var(--transition-normal);
}

.nav-link:hover {
  color: var(--secondary-color);
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px var(--neumorphism-shadow-dark),
  -6px -6px 12px var(--neumorphism-shadow-light);
}

.nav-link:hover::before {
  left: 100%;
}

.nav-link.active {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  box-shadow: inset 4px 4px 8px rgba(0, 0, 0, 0.2),
  inset -4px -4px 8px rgba(255, 255, 255, 0.1),
  0 8px 16px rgba(81, 123, 77, 0.3);
}

.nav-link i {
  width: 20px;
  margin-right: var(--spacing-sm);
  font-size: 16px;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  transition: margin-left var(--transition-normal);
  background: var(--neumorphism-bg);
}

/* 顶部导航栏 - 磨砂玻璃效果 */
.navbar {
  height: 0px;
  padding: 0 var(--spacing-lg);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-backdrop);
  -webkit-backdrop-filter: var(--glass-backdrop);
  border-bottom: 1px solid var(--glass-border);
  position: relative;
  z-index: 100;
}

.navbar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg,
  rgba(255, 255, 255, 0.1) 0%,
  rgba(255, 255, 255, 0.05) 50%,
  rgba(255, 255, 255, 0.1) 100%);
  pointer-events: none;
}

/* 菜单切换按钮 - 新拟态效果 */
.menu-toggle {
  display: none;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: var(--border-radius-md);
  background: var(--neumorphism-bg);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: 4px 4px 8px var(--neumorphism-shadow-dark),
  -4px -4px 8px var(--neumorphism-shadow-light);
  position: relative;
  z-index: 1;
}

.menu-toggle:hover {
  color: var(--primary-color);
  transform: translateY(-1px);
  box-shadow: 6px 6px 12px var(--neumorphism-shadow-dark),
  -6px -6px 12px var(--neumorphism-shadow-light);
}

.menu-toggle:active {
  transform: translateY(0);
  box-shadow: inset 2px 2px 4px var(--neumorphism-shadow-dark),
  inset -2px -2px 4px var(--neumorphism-shadow-light);
}

/* 快捷操作按钮 */
.quick-actions .el-button {
  border-radius: var(--border-radius-md);
  border: none;
  background: var(--neumorphism-bg);
  box-shadow: 4px 4px 8px var(--neumorphism-shadow-dark),
  -4px -4px 8px var(--neumorphism-shadow-light);
  transition: all var(--transition-normal);
  position: relative;
  z-index: 1;
}

.quick-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px var(--neumorphism-shadow-dark),
  -6px -6px 12px var(--neumorphism-shadow-light);
}

.quick-actions .el-button.el-button--primary {
  background: linear-gradient(135deg, var(--primary-color), #4A90E2);
  color: white;
}

.quick-actions .el-button.el-button--success {
  background: linear-gradient(135deg, var(--secondary-color), var(--success-color));
  color: white;
}

/* 通知徽章 */
.notification-badge {
  cursor: pointer;
  padding: var(--spacing-sm);
  border-radius: var(--border-radius-md);
  background: var(--neumorphism-bg);
  box-shadow: 4px 4px 8px var(--neumorphism-shadow-dark),
  -4px -4px 8px var(--neumorphism-shadow-light);
  transition: all var(--transition-normal);
  position: relative;
  z-index: 1;
}

.notification-badge:hover {
  transform: translateY(-1px);
  box-shadow: 6px 6px 12px var(--neumorphism-shadow-dark),
  -6px -6px 12px var(--neumorphism-shadow-light);
}

/* 用户头像区域 */
.user-avatar {
  cursor: pointer;
  padding: var(--spacing-sm);
  border-radius: var(--border-radius-md);
  background: var(--neumorphism-bg);
  box-shadow: 4px 4px 8px var(--neumorphism-shadow-dark),
  -4px -4px 8px var(--neumorphism-shadow-light);
  transition: all var(--transition-normal);
  position: relative;
  z-index: 1;
}

.user-avatar:hover {
  transform: translateY(-1px);
  box-shadow: 6px 6px 12px var(--neumorphism-shadow-dark),
  -6px -6px 12px var(--neumorphism-shadow-light);
}

.avatar-placeholder {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  font-size: 18px;
  box-shadow: inset 2px 2px 4px rgba(255, 255, 255, 0.2),
  inset -2px -2px 4px rgba(0, 0, 0, 0.2);
}

.user-avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: inset 2px 2px 4px rgba(255, 255, 255, 0.2),
  inset -2px -2px 4px rgba(0, 0, 0, 0.2);
}

.username {
  font-weight: 500;
  color: var(--text-primary);
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
}

/* 页面内容区域 */
.page-content {
  padding: var(--spacing-lg);
  min-height: calc(100vh - 64px);
  background: var(--neumorphism-bg);
  position: relative;
  margin-top: 4.5rem;
}

.page-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 80%,
  rgba(0, 47, 167, 0.03) 0%,
  transparent 50%),
  radial-gradient(circle at 80% 20%,
      rgba(81, 123, 77, 0.03) 0%,
      transparent 50%);
  pointer-events: none;
}

/* 侧边栏遮罩 */
.sidebar-overlay {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  z-index: 150;
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.sidebar-overlay.show {
  opacity: 1;
}

/* 面包屑导航 */
.breadcrumb {
  color: var(--text-auxiliary);
  font-size: 14px;
  font-weight: 400;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
}

/* 动画效果 */
@keyframes shimmer {
  0% {
    background-position: -200px 0;
  }
  100% {
    background-position: calc(200px + 100%) 0;
  }
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 4px 4px 8px var(--neumorphism-shadow-dark),
    -4px -4px 8px var(--neumorphism-shadow-light);
  }
  50% {
    box-shadow: 6px 6px 12px var(--neumorphism-shadow-dark),
    -6px -6px 12px var(--neumorphism-shadow-light);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    transform: translateX(-100%);
    z-index: 200;
    width: 280px;
    box-shadow: 8px 0 24px rgba(0, 0, 0, 0.15),
    0 0 0 1px var(--glass-border);
  }

  .sidebar.open {
    transform: translateX(0);
  }

  .main-content {
    margin-left: 0;
  }

  .menu-toggle {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .quick-actions {
    display: none;
  }

  .sidebar-overlay {
    display: block;
  }

  .breadcrumb {
    display: none;
  }

  .navbar {
    padding: 0 var(--spacing-md);
  }

  .page-content {
    padding: var(--spacing-md);
  }
}

@media (max-width: 480px) {
  .username {
    display: none;
  }

  .notifications {
    margin-right: var(--spacing-sm);
  }

  .sidebar {
    width: 100vw;
  }

  .navbar {
    padding: 0 var(--spacing-sm);
  }

  .page-content {
    padding: var(--spacing-sm);
  }

  .nav-link {
    padding: var(--spacing-lg) var(--spacing-md);
    font-size: 16px;
  }

  .logo h2 {
    font-size: 18px;
  }
}

/* 深色主题支持 */
@media (prefers-color-scheme: dark) {
  :root {
    --neumorphism-bg: #2a2a2a;
    --neumorphism-shadow-light: #363636;
    --neumorphism-shadow-dark: #1e1e1e;
    --glass-bg: rgba(0, 0, 0, 0.25);
    --glass-border: rgba(255, 255, 255, 0.1);
    --text-primary: #ffffff;
    --text-secondary: #cccccc;
    --text-auxiliary: #999999;
  }
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .nav-link {
    border: 2px solid transparent;
  }

  .nav-link:focus {
    border-color: var(--primary-color);
    outline: 2px solid var(--primary-color);
    outline-offset: 2px;
  }
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>