<template>
  <div class="student-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar modern-sidebar" :class="{ 'open': sidebarOpen, 'collapsed': isCollapsed }">
      <div class="sidebar-header">
        <div class="logo modern-logo">
          <div class="logo-icon">
            <font-awesome-icon :icon="['fas', 'graduation-cap']" />
          </div>
          <div class="logo-text">
            <h2 class="logo-title">DaoX-EduPlatform</h2>
            <span class="logo-subtitle">学习平台</span>
          </div>
        </div>
        <div class="collapse-btn" @click="toggleCollapse">
          <font-awesome-icon :icon="['fas', isCollapsed ? 'chevron-right' : 'chevron-left']" />
        </div>
      </div>
      
      <nav class="sidebar-nav">
        <ul class="nav-list">
          <li class="nav-item">
            <el-tooltip content="首页" placement="right" :disabled="!isCollapsed">
              <router-link to="/index" class="nav-link" exact-active-class="active">
                <div class="nav-icon">
                  <font-awesome-icon :icon="['fas', 'home']" />
                </div>
                <span class="nav-text">首页</span>
                <div class="nav-indicator"></div>
              </router-link>
            </el-tooltip>
          </li>
          <li class="nav-item">
            <el-tooltip content="课程中心" placement="right" :disabled="!isCollapsed">
              <router-link to="/index/courses" class="nav-link" active-class="active">
                <div class="nav-icon">
                  <font-awesome-icon :icon="['fas', 'book']" />
                </div>
                <span class="nav-text">课程中心</span>
                <div class="nav-indicator"></div>
              </router-link>
            </el-tooltip>
          </li>
          <li class="nav-item">
            <el-tooltip content="我的课程" placement="right" :disabled="!isCollapsed">
              <router-link to="/index/my-courses" class="nav-link" active-class="active">
                <div class="nav-icon">
                  <font-awesome-icon :icon="['fas', 'bookmark']" />
                </div>
                <span class="nav-text">我的课程</span>
                <div class="nav-indicator"></div>
              </router-link>
            </el-tooltip>
          </li>
          <li class="nav-item">
            <el-tooltip content="考试测评" placement="right" :disabled="!isCollapsed">
              <router-link to="/index/assessments" class="nav-link" active-class="active">
                <div class="nav-icon">
                  <font-awesome-icon :icon="['fas', 'clipboard-check']" />
                </div>
                <span class="nav-text">考试测评</span>
                <div class="nav-indicator"></div>
              </router-link>
            </el-tooltip>
          </li>
          <li class="nav-item">
            <el-tooltip content="自由笔记" placement="right" :disabled="!isCollapsed">
              <router-link to="/index/notes" class="nav-link" active-class="active">
                <div class="nav-icon">
                  <font-awesome-icon :icon="['fas', 'sticky-note']" />
                </div>
                <span class="nav-text">自由笔记</span>
                <div class="nav-indicator"></div>
              </router-link>
            </el-tooltip>
          </li>
          <li class="nav-item">
            <el-tooltip content="在线交流" placement="right" :disabled="!isCollapsed">
              <router-link to="/index/chat" class="nav-link" active-class="active">
                <div class="nav-icon">
                  <font-awesome-icon :icon="['fas', 'comments']" />
                </div>
                <span class="nav-text">在线交流</span>
                <div class="nav-indicator"></div>
              </router-link>
            </el-tooltip>
          </li>
          <li class="nav-item">
            <el-tooltip content="个人中心" placement="right" :disabled="!isCollapsed">
              <router-link to="/index/profile" class="nav-link" active-class="active">
                <div class="nav-icon">
                  <font-awesome-icon :icon="['fas', 'user']" />
                </div>
                <span class="nav-text">个人中心</span>
                <div class="nav-indicator"></div>
              </router-link>
            </el-tooltip>
          </li>
        </ul>
      </nav>
    </aside>
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <header class="navbar modern-navbar">
        <div class="navbar-left">
          <button class="menu-toggle modern-button" @click="toggleSidebar">
            <font-awesome-icon :icon="['fas', 'bars']" />
          </button>
          <div class="breadcrumb">
            <div class="breadcrumb-content">
              <font-awesome-icon :icon="['fas', 'map-marker-alt']" class="breadcrumb-icon" />
              <span class="breadcrumb-text">{{ currentPageTitle }}</span>
            </div>
          </div>
        </div>
        
        <div class="navbar-center">
          <div class="search-container">
            <div class="search-wrapper">
              <font-awesome-icon :icon="['fas', 'search']" class="search-icon" />
              <input 
                v-model="searchQuery" 
                placeholder="搜索课程、资源、讲师..."
                class="search-input"
                @focus="onSearchFocus"
                @blur="onSearchBlur"
              />
              <div class="search-suggestions" v-if="showSearchSuggestions">
                <div class="suggestion-item" v-for="suggestion in searchSuggestions" :key="suggestion.id">
                  <font-awesome-icon :icon="suggestion.icon" class="suggestion-icon" />
                  <span>{{ suggestion.text }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="navbar-right">
          <div class="navbar-actions">
            <button class="action-btn notification-btn" @click="toggleNotifications">
              <font-awesome-icon :icon="['fas', 'bell']" />
              <span class="notification-badge" v-if="unreadNotifications > 0">{{ unreadNotifications }}</span>
            </button>
            
            <button class="action-btn message-btn" @click="toggleMessages">
              <font-awesome-icon :icon="['fas', 'envelope']" />
              <span class="message-badge" v-if="unreadMessages > 0">{{ unreadMessages }}</span>
            </button>
          </div>
          
          <div class="user-menu">
            <el-dropdown trigger="click" @command="handleUserCommand">
              <div class="user-profile">
                <div class="user-avatar">
                  <img v-if="userAvatar" :src="userAvatar" alt="用户头像" class="avatar-image" />
                  <div v-else class="avatar-placeholder">
                    <font-awesome-icon :icon="['fas', 'user']" />
                  </div>
                  <div class="online-indicator"></div>
                </div>
                <div class="user-info">
                  <span class="username">{{ userName || '学生用户' }}</span>
                  <span class="user-role">学生</span>
                </div>
                <font-awesome-icon :icon="['fas', 'chevron-down']" class="dropdown-arrow" />
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown">
                  <el-dropdown-item command="profile" class="dropdown-item">
                    <font-awesome-icon :icon="['fas', 'user']" class="item-icon" />
                    <span>个人资料</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="settings" class="dropdown-item">
                    <font-awesome-icon :icon="['fas', 'cog']" class="item-icon" />
                    <span>设置</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="help" class="dropdown-item">
                    <font-awesome-icon :icon="['fas', 'question-circle']" class="item-icon" />
                    <span>帮助中心</span>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout" class="dropdown-item logout-item">
                    <font-awesome-icon :icon="['fas', 'sign-out-alt']" class="item-icon" />
                    <span>退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <main class="page-content">
        <router-view />
      </main>
    </div>
    
    <!-- 移动端遮罩 -->
    <div 
      v-if="sidebarOpen" 
      class="sidebar-overlay" 
      @click="closeSidebar"
    ></div>
  </div>
</template>

<script setup>
import {ref, computed, watch, onMounted} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {getUsername, getIdentifier} from "@/utils/tokenAnalysis.js";
import { getStudentProfile } from '@/api/students/stuAPI.js';
import { logout } from '@/api/index.js';
import { ElMessage } from 'element-plus';

const route = useRoute()
const router = useRouter()
const sidebarOpen = ref(false)
const isCollapsed = ref(false)
const searchQuery = ref('')
const showSearchSuggestions = ref(false)
const unreadNotifications = ref(3)
const unreadMessages = ref(1)
const userName = ref('')
const userAvatar = ref(null)

// 搜索建议数据
const searchSuggestions = ref([
  { id: 1, text: 'Vue.js 基础教程', icon: ['fas', 'play-circle'] },
  { id: 2, text: 'JavaScript 进阶', icon: ['fas', 'code'] },
  { id: 3, text: '前端开发实战', icon: ['fas', 'laptop-code'] },
  { id: 4, text: '李老师', icon: ['fas', 'user-tie'] }
])

// 页面标题映射
const pageTitles = {
  'StudentHome': '首页',
  'Courses': '课程中心',
  'CourseDetail': '课程详情',
  'MyCourses': '我的课程',
  'Learn': '学习中心',
  'Assessments': '考试测评',
  'Exam': '在线考试',
  'Notes': '学习笔记',
  'Profile': '个人中心',
  'StudentChat': '在线交流'
}

const currentPageTitle = computed(() => {
  return pageTitles[route.name] || '学习平台'
})

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

const closeSidebar = () => {
  sidebarOpen.value = false
}

const onSearchFocus = () => {
  if (searchQuery.value.length > 0) {
    showSearchSuggestions.value = true
  }
}

const onSearchBlur = () => {
  // 延迟隐藏，允许点击建议项
  setTimeout(() => {
    showSearchSuggestions.value = false
  }, 200)
}

const toggleNotifications = () => {
  // 跳转到聊天页面
  router.push('/index/chat')
}

const toggleMessages = () => {
  // 处理消息点击
  console.log('打开消息面板')
}

const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/index/profile')
      break
    case 'settings':
      console.log('打开设置')
      break
    case 'help':
      console.log('打开帮助中心')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 处理登出
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

// 监听搜索查询变化
watch(searchQuery, (newVal) => {
  if (newVal.length > 0) {
    showSearchSuggestions.value = true
  } else {
    showSearchSuggestions.value = false
  }
})

// 加载用户头像
const loadUserAvatar = () => {
  const identifier = getIdentifier();
  if (!identifier) {
    console.warn('无法获取用户标识符');
    return;
  }
  
  getStudentProfile(
    identifier,
    (data) => {
      // 成功回调
      if (data && data.avatarUrl) {
        userAvatar.value = data.avatarUrl;
      }
    },
    (error) => {
      // 失败回调
      console.error('获取用户头像失败:', error);
      // 头像获取失败不显示错误消息，使用默认图标即可
    }
  );
}

onMounted(async () => {
  // 页面加载动画或其他初始化逻辑
  try {
    userName.value = await getUsername();
    // 加载用户头像
    loadUserAvatar();
  } catch (error) {
    console.error('获取用户名失败:', error);
    ElMessage.error('获取用户名失败，请重新登录');
  }
})
</script>

<style scoped>
/* 布局基础样式 */
.student-layout {
  display: flex;
  height: 100vh;
  background: linear-gradient(135deg, #F0F0F3 0%, #E8F4FD 50%, #F0F0F3 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  position: relative;
  overflow-x: hidden;
}

.student-layout::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 50%, rgba(0, 47, 167, 0.03) 0%, transparent 50%),
              radial-gradient(circle at 80% 20%, rgba(81, 123, 77, 0.03) 0%, transparent 50%),
              radial-gradient(circle at 40% 80%, rgba(103, 194, 58, 0.02) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

/* 侧边栏样式 */
.sidebar {
  width: 240px;
  background: rgba(240, 240, 243, 0.25);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid rgba(255, 255, 255, 0.18);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  z-index: 1000;
  box-shadow: 
    8px 0 32px rgba(0, 47, 167, 0.1),
    inset 1px 0 0 rgba(255, 255, 255, 0.2);
}

.modern-sidebar {
  border-radius: 0 24px 24px 0;
}

.modern-sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, 
    rgba(0, 47, 167, 0.02) 0%, 
    rgba(81, 123, 77, 0.01) 50%, 
    rgba(103, 194, 58, 0.02) 100%);
  pointer-events: none;
}

.sidebar-header {
  padding: 2rem 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  color: white;
  position: relative;
  overflow: hidden;
}

.sidebar-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

.modern-logo {
  display: flex;
  align-items: center;
  gap: 1rem;
  position: relative;
  z-index: 1;
}

.logo-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 
    4px 4px 8px rgba(0, 0, 0, 0.1),
    -2px -2px 4px rgba(255, 255, 255, 0.1);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.logo-icon:hover {
  transform: scale(1.05) rotate(5deg);
  box-shadow: 
    6px 6px 12px rgba(0, 0, 0, 0.15),
    -3px -3px 6px rgba(255, 255, 255, 0.15);
}

.logo-icon::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transform: rotate(45deg);
  animation: logoShine 3s infinite;
}

@keyframes logoShine {
  0% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
  50% { transform: translateX(100%) translateY(100%) rotate(45deg); }
  100% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-title {
  margin: 0;
  font-weight: 700;
  font-size: 1.25rem;
  color: white;
  line-height: 1.2;
}

.logo-subtitle {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  margin-top: 2px;
}

/* 导航样式 */
.sidebar-nav {
  padding: 1.5rem 0;
  position: relative;
  z-index: 1;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  margin: 0.5rem 1.25rem;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.25rem;
  color: #4a5568;
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  font-weight: 500;
  letter-spacing: 0.3px;
}

.nav-link::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  transform: scaleY(0);
  transition: transform 0.3s ease;
  border-radius: 0 2px 2px 0;
}

.nav-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.125rem;
  transition: all 0.3s ease;
}

.nav-text {
  font-weight: inherit;
  font-size: 0.95rem;
}

.nav-indicator {
  position: absolute;
  right: 1rem;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: transparent;
  transition: all 0.3s ease;
}

.nav-link:hover {
  background: rgba(0, 47, 167, 0.08);
  color: #002FA7;
  transform: translateX(8px);
  box-shadow: 
    8px 8px 16px rgba(0, 47, 167, 0.1),
    -4px -4px 8px rgba(255, 255, 255, 0.3);
}

.nav-link:hover::before {
  transform: scaleY(1);
}

.nav-link:hover .nav-icon {
  color: #002FA7;
  transform: scale(1.1);
}

.nav-link:hover .nav-indicator {
  background: #002FA7;
  box-shadow: 0 0 8px rgba(0, 47, 167, 0.4);
}

.nav-link.active {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.15) 0%, rgba(81, 123, 77, 0.1) 100%);
  color: #002FA7;
  font-weight: 600;
  box-shadow: 
    inset 4px 4px 8px rgba(0, 47, 167, 0.1),
    inset -2px -2px 4px rgba(255, 255, 255, 0.2),
    0 4px 15px rgba(0, 47, 167, 0.2);
}

.nav-link.active::before {
  transform: scaleY(1);
}

.nav-link.active .nav-icon {
  color: #002FA7;
  transform: scale(1.1);
}

.nav-link.active .nav-text {
  color: #002FA7;
  font-weight: 600;
}

.nav-link.active .nav-indicator {
  background: #002FA7;
  box-shadow: 0 0 8px rgba(0, 47, 167, 0.4);
}

/* 主内容区域样式 */
.main-content {
  flex: 1;
  margin-left: 240px;
  padding-top: 90px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
  height: 100vh;
  overflow-y: auto;
}

/* 顶部导航栏样式 */
.navbar {
  height: 80px;
  padding: 0 0.75rem;
  margin: 0.5rem;
  background: rgba(240, 240, 243, 0.25);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 8px 32px rgba(0, 47, 167, 0.08);
  position: fixed;
  top: 0;
  left: 240px;
  right: 0;
  z-index: 999;
}

.navbar::before {
  content: '';
  position: absolute;
  top: 0;
  left: -2rem;
  right: -2rem;
  bottom: 0;
  background: linear-gradient(90deg, 
    rgba(0, 47, 167, 0.02) 0%, 
    rgba(255, 255, 255, 0.05) 50%, 
    rgba(81, 123, 77, 0.02) 100%);
  pointer-events: none;
}

.modern-navbar {
  border-radius: 0 0 24px 24px;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  position: relative;
  z-index: 1;
}

.menu-toggle {
  display: none;
  width: 48px;
  height: 48px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  color: #002FA7;
  font-size: 1.25rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    4px 4px 8px rgba(0, 47, 167, 0.1),
    -2px -2px 4px rgba(255, 255, 255, 0.3);
}

.menu-toggle:hover {
  background: rgba(0, 47, 167, 0.1);
  transform: translateY(-2px);
  box-shadow: 
    6px 6px 12px rgba(0, 47, 167, 0.15),
    -3px -3px 6px rgba(255, 255, 255, 0.4);
}

.breadcrumb {
  display: flex;
  align-items: center;
}

.breadcrumb-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.breadcrumb-content:hover {
  background: rgba(0, 47, 167, 0.05);
  color: #002FA7;
}

.breadcrumb-icon {
  color: #002FA7;
  font-size: 0.875rem;
}

.breadcrumb-text {
  color: #1e293b;
  font-weight: 500;
  font-size: 0.875rem;
}

/* 搜索区域样式 */
.navbar-center {
  flex: 1;
  display: flex;
  justify-content: center;
  max-width: 600px;
  margin: 0 2rem;
  position: relative;
  z-index: 1;
}

.search-container {
  width: 100%;
  position: relative;
}

.search-wrapper {
  position: relative;
  width: 100%;
}

.search-input {
  width: 100%;
  height: 48px;
  padding: 0 1rem 0 3rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  font-size: 0.95rem;
  color: #4a5568;
  outline: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    inset 4px 4px 8px rgba(0, 47, 167, 0.05),
    inset -2px -2px 4px rgba(255, 255, 255, 0.2);
}

.search-input::placeholder {
  color: #a0aec0;
}

.search-input:focus {
  border-color: rgba(0, 47, 167, 0.3);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 
    inset 2px 2px 4px rgba(0, 47, 167, 0.1),
    inset -1px -1px 2px rgba(255, 255, 255, 0.3),
    0 0 0 3px rgba(0, 47, 167, 0.1);
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #a0aec0;
  font-size: 1rem;
  pointer-events: none;
  transition: color 0.3s ease;
}

.search-input:focus + .search-icon {
  color: #002FA7;
}

.search-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  margin-top: 0.5rem;
  padding: 0.5rem;
  box-shadow: 
    12px 12px 24px rgba(0, 47, 167, 0.1),
    -6px -6px 12px rgba(255, 255, 255, 0.8);
  z-index: 1000;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #64748b;
}

.suggestion-item:hover {
  background: rgba(0, 47, 167, 0.1);
  color: #002FA7;
  transform: translateX(4px);
}

.suggestion-icon {
  font-size: 0.875rem;
}

/* 右侧操作区域样式 */
.navbar-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  position: relative;
  z-index: 1;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.action-btn {
  position: relative;
  width: 48px;
  height: 48px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  color: #4a5568;
  font-size: 1.125rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    4px 4px 8px rgba(0, 47, 167, 0.05),
    -2px -2px 4px rgba(255, 255, 255, 0.2);
}

.action-btn:hover {
  background: rgba(0, 47, 167, 0.1);
  color: #002FA7;
  transform: translateY(-2px);
  box-shadow: 
    6px 6px 12px rgba(0, 47, 167, 0.1),
    -3px -3px 6px rgba(255, 255, 255, 0.3);
}

.notification-badge,
.message-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #F56C6C 0%, #E6A23C 100%);
  color: white;
  border-radius: 10px;
  font-size: 0.75rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 4px 8px rgba(245, 108, 108, 0.3),
    0 0 0 2px rgba(255, 255, 255, 0.9);
}

/* 用户菜单样式 */
.user-menu {
  position: relative;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    4px 4px 8px rgba(0, 47, 167, 0.05),
    -2px -2px 4px rgba(255, 255, 255, 0.2);
}

.user-profile:hover {
  background: rgba(0, 47, 167, 0.05);
  transform: translateY(-2px);
  box-shadow: 
    6px 6px 12px rgba(0, 47, 167, 0.1),
    -3px -3px 6px rgba(255, 255, 255, 0.3);
}

.user-avatar {
  position: relative;
  width: 40px;
  height: 40px;
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 
    4px 4px 8px rgba(203, 213, 225, 0.3),
    -4px -4px 8px rgba(255, 255, 255, 0.8);
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  color: white;
  font-size: 1.125rem;
  box-shadow: 
    4px 4px 8px rgba(0, 47, 167, 0.3),
    -2px -2px 4px rgba(255, 255, 255, 0.2);
}

.online-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 0 8px rgba(103, 194, 58, 0.4);
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.username {
  font-weight: 600;
  color: #1e293b;
  font-size: 0.95rem;
  line-height: 1.2;
}

.user-role {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 500;
}

.dropdown-arrow {
  color: #4a5568;
  font-size: 0.875rem;
  transition: transform 0.3s ease;
}

.user-profile:hover .dropdown-arrow {
  transform: rotate(180deg);
}

/* 下拉菜单样式 */
.user-dropdown {
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  border-radius: 16px !important;
  box-shadow: 
    12px 12px 24px rgba(0, 47, 167, 0.1),
    -6px -6px 12px rgba(255, 255, 255, 0.8) !important;
  padding: 0.5rem !important;
}

.dropdown-item {
  display: flex !important;
  align-items: center !important;
  gap: 0.75rem !important;
  padding: 0.75rem 1rem !important;
  border-radius: 12px !important;
  transition: all 0.3s ease !important;
  color: #64748b !important;
  margin-bottom: 0.25rem !important;
}

.dropdown-item:hover {
  background: rgba(0, 47, 167, 0.1) !important;
  color: #002FA7 !important;
  transform: translateX(4px) !important;
}

.logout-item:hover {
  background: rgba(245, 108, 108, 0.1) !important;
  color: #F56C6C !important;
}

.item-icon {
  font-size: 0.875rem !important;
}

/* 页面内容样式 */
.page-content {
  padding: 0.75rem;
  min-height: calc(100vh - 80px);
  background: transparent;
  position: relative;
  z-index: 1;
}

/* 遮罩层样式 */
.sidebar-overlay {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 47, 167, 0.1);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  z-index: 150;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 折叠按钮样式 */
.collapse-btn {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.6);
  transition: all 0.3s ease;
  z-index: 10;
}

.collapse-btn:hover {
  color: white;
  transform: translateY(-50%) scale(1.1);
}

/* 桌面端折叠功能 */
@media (min-width: 769px) {
  /* 折叠状态样式 */
  .sidebar.collapsed {
    width: 80px;
  }

  .sidebar.collapsed .logo-text,
  .sidebar.collapsed .logo-subtitle {
    opacity: 0;
    width: 0;
    pointer-events: none;
    display: none;
  }

  .sidebar.collapsed .sidebar-header {
    padding: 2rem 0;
    display: flex;
    justify-content: center;
  }

  .sidebar.collapsed .logo-icon {
    margin: 0;
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }

  .sidebar.collapsed .collapse-btn {
    right: 50%;
    transform: translateX(50%);
    top: auto;
    bottom: 15px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    width: 24px;
    height: 24px;
    font-size: 0.9rem;
  }

  .sidebar.collapsed .nav-text,
  .sidebar.collapsed .nav-indicator {
    opacity: 0;
    display: none;
  }

  .sidebar.collapsed .nav-link {
    padding: 1rem 0;
    justify-content: center;
  }

  .sidebar.collapsed .nav-link::before {
    display: none;
  }

  .sidebar.collapsed .nav-item {
    margin: 0.5rem 10px;
  }

  /* 内容区域跟随折叠 */
  .sidebar.collapsed ~ .main-content {
    margin-left: 80px;
  }

  .sidebar.collapsed ~ .main-content .navbar {
    left: 80px;
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .navbar {
    padding: 0 1.5rem;
  }
  
  .navbar-center {
    margin: 0 1rem;
  }
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    transform: translateX(-100%);
    z-index: 1100;
    width: 280px;
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
  
  .collapse-btn {
    display: none;
  }
  
  .navbar {
    padding: 0 1rem;
  }
  
  .navbar-center {
    margin: 0 0.5rem;
  }
  
  .search-input {
    font-size: 0.875rem;
  }
  
  .sidebar-overlay {
    display: block;
  }
  
  .breadcrumb {
    display: none;
  }
  
  .user-info {
    display: none;
  }
  
  .page-content {
    padding: 1.5rem;
    margin: 0.5rem 0.5rem 0 0;
  }
}

@media (max-width: 480px) {
  .navbar {
    height: 64px;
    padding: 0 0.75rem;
  }
  
  .navbar-center {
    margin: 0 0.25rem;
  }
  
  .search-input {
    height: 40px;
    font-size: 0.875rem;
  }
  
  .action-btn {
    width: 40px;
    height: 40px;
    font-size: 1rem;
  }
  
  .user-profile {
    padding: 0.25rem 0.5rem;
  }
  
  .user-avatar {
    width: 32px;
    height: 32px;
  }
  
  .page-content {
    padding: 1rem;
    min-height: calc(100vh - 64px);
  }
  
  .navbar-actions {
    gap: 0.5rem;
  }
  
  .navbar-right {
    gap: 0.75rem;
  }
}
</style>