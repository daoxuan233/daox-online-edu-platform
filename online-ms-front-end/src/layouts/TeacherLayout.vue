<template>
  <div class="teacher-layout">
    <!-- 背景装饰 -->
    <div class="bg-decoration-1"></div>
    <div class="bg-decoration-2"></div>

    <!-- 侧边栏 -->
    <aside class="sidebar glass-sidebar" :class="{ 'open': sidebarOpen }">
      <div class="sidebar-header">
        <div class="logo flex-center">
          <img src="/DaoX_C7-Center_Logo.svg" alt="DaoX Logo" class="logo-img" />
          <h2 class="logo-text">教师工作台</h2>
        </div>
      </div>

      <nav class="sidebar-nav">
        <ul class="nav-list">
          <li class="nav-item">
            <router-link to="/teacher" class="nav-link" exact-active-class="active">
              <div class="icon-wrapper">
                <font-awesome-icon :icon="['fas', 'tachometer-alt']"/>
              </div>
              <span class="nav-text">工作台</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/teacher/courses" class="nav-link" exact-active-class="active">
              <div class="icon-wrapper">
                <font-awesome-icon :icon="['fas', 'book-open']"/>
              </div>
              <span class="nav-text">课程管理</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/teacher/chat" class="nav-link" exact-active-class="active">
              <div class="icon-wrapper">
                <font-awesome-icon :icon="['fas', 'comments']"/>
              </div>
              <span class="nav-text">对话中心</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/teacher/group-chat" class="nav-link" exact-active-class="active">
              <div class="icon-wrapper">
                <font-awesome-icon :icon="['fas', 'chalkboard-user']"/>
              </div>
              <span class="nav-text">课程群聊</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/teacher/questions" class="nav-link" exact-active-class="active">
              <div class="icon-wrapper">
                <font-awesome-icon :icon="['fas', 'question-circle']"/>
              </div>
              <span class="nav-text">题库管理</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/teacher/assessments" class="nav-link" exact-active-class="active">
              <div class="icon-wrapper">
                <font-awesome-icon :icon="['fas', 'clipboard-list']"/>
              </div>
              <span class="nav-text">考试管理</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/teacher/grading" class="nav-link" exact-active-class="active">
              <div class="icon-wrapper">
                <font-awesome-icon :icon="['fas', 'graduation-cap']"/>
              </div>
              <span class="nav-text">阅卷中心</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/teacher/notifications" class="nav-link" exact-active-class="active">
              <div class="icon-wrapper">
                <font-awesome-icon :icon="['fas', 'bell']"/>
              </div>
              <span class="nav-text">通知中心</span>
            </router-link>
          </li>
        </ul>
      </nav>
    </aside>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <header class="navbar glass-navbar">
        <div class="navbar-left">
          <button class="menu-toggle neu-button-icon" @click="toggleSidebar">
            <font-awesome-icon :icon="['fas', 'bars']"/>
          </button>
          <div class="breadcrumb">
            <span class="breadcrumb-item">首页</span>
            <span class="breadcrumb-separator">/</span>
            <span class="breadcrumb-current">{{ currentPageTitle }}</span>
          </div>
        </div>

        <div class="navbar-right">
          <div class="notifications">
            <el-badge :value="unreadNotifications" :hidden="unreadNotifications === 0" class="notification-badge" @click="toggleNotifications">
              <button class="neu-button-icon sm">
                <font-awesome-icon :icon="['fas', 'bell']" />
              </button>
            </el-badge>
          </div>

          <div class="user-menu">
            <el-dropdown @command="handleUserCommand" trigger="click">
              <div class="user-profile-trigger">
                <div class="avatar-wrapper">
                  <img v-if="teacherInfo.avatar" :src="teacherInfo.avatar" :alt="teacherInfo.name" class="user-avatar-img"/>
                  <div v-else class="avatar-placeholder">
                    {{ teacherInfo.name ? teacherInfo.name.charAt(0) : 'T' }}
                  </div>
                </div>
                <span class="username">{{ teacherInfo.name }}</span>
                <font-awesome-icon :icon="['fas', 'chevron-down']" class="dropdown-icon"/>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="custom-dropdown">
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
                    <font-awesome-icon :icon="['fas', 'sign-out-alt']" class="text-danger mr-sm"/>
                    <span class="text-danger">退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="page-content-wrapper">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>

    <!-- 移动端遮罩 -->
    <div
        v-if="sidebarOpen"
        class="sidebar-overlay"
        :class="{ 'show': sidebarOpen }"
        @click="closeSidebar"
    ></div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted, onUnmounted, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {logout} from '@/api/index.js'
import {ElMessage} from 'element-plus'
import {getIdentifier} from '@/utils/tokenAnalysis.js'
import {getTeacherProfile} from '@/api/teacher/teacherAPI.js'
import { getUnreadNotificationCount } from '@/api/notifications.js'
import gsap from 'gsap'

const route = useRoute()
const router = useRouter()
const sidebarOpen = ref(true)
const unreadNotifications = ref(0)

/**
 * 教师端布局动画时间线。
 * 页面卸载时需要主动销毁，避免 GSAP 实例残留。
 */
let layoutTimeline = null

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
  'TeacherChat': '对话中心',
  'TeacherGroupChat': '课程群聊',
  'CourseEdit': '编辑课程',
  'QuestionBank': '题库管理',
  'AssessmentManagement': '考试管理',
  'GradingCenter': '阅卷中心',
  'TeacherNotifications': '通知中心'
}

/**
 * 响应通知中心页面发出的未读数同步事件。
 *
 * @param {CustomEvent} event 通知事件对象
 */
const handleUnreadNotificationUpdate = (event) => {
  unreadNotifications.value = Number(event?.detail?.unreadCount || 0)
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
  router.push('/teacher/notifications')
}

/**
 * 获取教师端未读通知数量。
 *
 * @returns {Promise<void>} 异步结果
 */
const loadUnreadNotifications = async () => {
  try {
    const data = await getUnreadNotificationCount()
    unreadNotifications.value = Number(data?.unreadCount || 0)
  } catch (error) {
    console.error('获取教师未读通知数失败:', error)
  }
}

/**
 * 执行教师布局入场动画。
 * 仅使用 transform 与 opacity，减少布局抖动。
 */
const runLayoutAnimation = () => {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    gsap.set(['.sidebar', '.navbar', '.page-content-wrapper'], { opacity: 1, x: 0, y: 0 })
    return
  }

  layoutTimeline?.kill()
  layoutTimeline = gsap.timeline({ defaults: { ease: 'power2.out' } })
  layoutTimeline
    .fromTo('.sidebar', { x: -50, opacity: 0 }, { x: 0, opacity: 1, duration: 0.6 })
    .fromTo('.navbar', { y: -20, opacity: 0 }, { y: 0, opacity: 1, duration: 0.5 }, '-=0.25')
    .fromTo('.page-content-wrapper', { y: 30, opacity: 0 }, { y: 0, opacity: 1, duration: 0.55 }, '-=0.2')
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
  loadUnreadNotifications()
  runLayoutAnimation()
  window.addEventListener('notification-unread-updated', handleUnreadNotificationUpdate)
})

watch(() => route.fullPath, () => {
  loadUnreadNotifications()
})

onUnmounted(() => {
  layoutTimeline?.kill()
  window.removeEventListener('notification-unread-updated', handleUnreadNotificationUpdate)
})
</script>

<style scoped>
/* CSS变量定义 - 现代科技感配色 */
:root {
  /* 核心颜色 */
  --primary-color: #2563EB; /* 科技蓝 */
  --primary-light: #60A5FA;
  --primary-dark: #1E40AF;
  --secondary-color: #10B981; /* 活力绿 */
  --accent-color: #F59E0B; /* 强调色 */
  
  /* 背景色 */
  --bg-base: #F8FAFC;
  --bg-glass: rgba(255, 255, 255, 0.7);
  --bg-glass-strong: rgba(255, 255, 255, 0.9);
  
  /* 文字颜色 */
  --text-primary: #0F172A;
  --text-secondary: #475569;
  --text-muted: #94A3B8;

  /* 阴影 - 新拟态/Glassmorphism */
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  --shadow-neu-light: -5px -5px 10px #ffffff, 5px 5px 10px #d1d5db;
  --shadow-neu-pressed: inset 3px 3px 6px #d1d5db, inset -3px -3px 6px #ffffff;
  
  /* 边框 */
  --border-glass: 1px solid rgba(255, 255, 255, 0.5);
  --border-radius-lg: 16px;
  --border-radius-xl: 24px;
}

.teacher-layout {
  display: flex;
  min-height: 100vh;
  background-color: #F0F2F5;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  position: relative;
  overflow-x: hidden;
}

/* 背景装饰 - 增加层次感 */
.bg-decoration-1 {
  position: fixed;
  top: -10%;
  right: -5%;
  width: 50vw;
  height: 50vw;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.1) 0%, rgba(255, 255, 255, 0) 70%);
  border-radius: 50%;
  z-index: 0;
  pointer-events: none;
}

.bg-decoration-2 {
  position: fixed;
  bottom: -10%;
  left: -5%;
  width: 40vw;
  height: 40vw;
  background: radial-gradient(circle, rgba(16, 185, 129, 0.1) 0%, rgba(255, 255, 255, 0) 70%);
  border-radius: 50%;
  z-index: 0;
  pointer-events: none;
}

/* 侧边栏 - 高级磨砂玻璃 */
.sidebar {
  width: 260px;
  z-index: 100;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-right: 1px solid rgba(255, 255, 255, 0.6);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), width 0.3s cubic-bezier(0.4, 0, 0.2, 1), border-color 0.3s ease, box-shadow 0.3s ease;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.02);
}

.sidebar-header {
  padding: 32px 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-img {
  width: 36px;
  height: 36px;
  object-fit: contain;
  filter: drop-shadow(0 4px 6px rgba(37, 99, 235, 0.2));
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #2563EB 0%, #10B981 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
  letter-spacing: -0.5px;
}

.sidebar-nav {
  flex: 1;
  padding: 0 16px;
  overflow-y: auto;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  color: #64748B;
  text-decoration: none;
  border-radius: 12px;
  transition: all 0.3s ease;
  font-weight: 500;
  font-size: 15px;
}

.icon-wrapper {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  transition: all 0.3s ease;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.6);
  color: #2563EB;
  transform: translateX(4px);
}

.nav-link.active {
  background: white;
  color: #2563EB;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.15);
  font-weight: 600;
}

.nav-link.active .icon-wrapper {
  color: #2563EB;
  transform: scale(1.1);
}

/* 主内容区域 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 10;
  width: calc(100% - 260px);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 顶部导航栏 - 悬浮玻璃 */
.navbar {
  height: 72px;
  padding: 0 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: transparent;
  margin-top: 8px;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.menu-toggle {
  display: none;
}

@media (min-width: 1025px) {
  .menu-toggle {
    display: flex;
  }

  .sidebar {
    min-width: 260px;
  }

  .sidebar:not(.open) {
    width: 80px;
    min-width: 80px;
  }

  .sidebar:not(.open) .sidebar-header {
    padding: 24px 0;
  }

  .sidebar:not(.open) .logo {
    justify-content: center;
    gap: 0;
  }

  .sidebar:not(.open) .logo-text {
    display: none;
  }

  .sidebar:not(.open) .sidebar-nav {
    padding: 0 10px;
  }

  .sidebar:not(.open) .nav-link {
    justify-content: center;
    padding: 12px;
  }

  .sidebar:not(.open) .nav-link:hover {
    transform: none;
  }

  .sidebar:not(.open) .icon-wrapper {
    margin-right: 0;
  }

  .sidebar:not(.open) .nav-text {
    display: none;
  }

  .sidebar:not(.open) + .main-content {
    width: calc(100% - 80px);
  }
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #64748B;
}

.breadcrumb-separator {
  color: #CBD5E1;
}

.breadcrumb-current {
  color: #0F172A;
  font-weight: 600;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

/* 新拟态按钮图标 */
.neu-button-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: none;
  background: #F8FAFC;
  color: #64748B;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: -4px -4px 10px #ffffff, 4px 4px 10px #e2e8f0;
  transition: all 0.2s ease;
}

.neu-button-icon:hover {
  color: #2563EB;
  transform: translateY(-2px);
  box-shadow: -6px -6px 12px #ffffff, 6px 6px 12px #cbd5e1;
}

.neu-button-icon:active {
  transform: translateY(0);
  box-shadow: inset 3px 3px 6px #cbd5e1, inset -3px -3px 6px #ffffff;
}

.neu-button-icon.sm {
  width: 36px;
  height: 36px;
  font-size: 14px;
}

/* 用户配置触发器 */
.user-profile-trigger {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px;
  border-radius: 40px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-profile-trigger:hover {
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.avatar-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #2563EB, #60A5FA);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
}

.username {
  font-weight: 600;
  color: #0F172A;
  font-size: 14px;
}

.dropdown-icon {
  font-size: 12px;
  color: #94A3B8;
}

/* 页面内容包裹器 */
.page-content-wrapper {
  flex: 1;
  padding: 0 32px 32px 32px;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 过渡动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 响应式 */
@media (max-width: 1024px) {
  .sidebar {
    position: fixed;
    transform: translateX(-100%);
    box-shadow: 8px 0 24px rgba(0, 0, 0, 0.1);
  }

  .sidebar.open {
    transform: translateX(0);
  }

  .main-content {
    width: 100%;
  }

  .menu-toggle {
    display: flex;
  }

  .sidebar-overlay {
    display: block;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(15, 23, 42, 0.3);
    backdrop-filter: blur(4px);
    z-index: 90;
    opacity: 0;
    pointer-events: none;
    transition: opacity 0.3s ease;
  }

  .sidebar-overlay.show {
    opacity: 1;
    pointer-events: auto;
  }
  
  .navbar {
    padding: 0 20px;
  }
  
  .page-content-wrapper {
    padding: 0 20px 20px 20px;
  }
}

/* 文本辅助类 */
.text-danger {
  color: #EF4444;
}

.mr-sm {
  margin-right: 8px;
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
