<template>
  <div class="admin-layout">
    <!-- 背景动态科技元素 -->
    <div class="tech-bg"></div>
    
    <!-- 侧边栏 (浮动卡片式) -->
    <aside class="sidebar glass-card" :class="{ 'open': sidebarOpen }" ref="sidebarRef">
      <div class="sidebar-header">
        <div class="logo flex-center">
          <img src="/DaoX_C7-Center_Logo.svg" alt="DaoX Logo" class="mr-sm logo-img" style="width: 32px; height: 32px; object-fit: contain;" />
          <h2 class="text-lg font-bold text-gradient">管理后台</h2>
        </div>
      </div>
      
      <nav class="sidebar-nav mt-lg">
        <ul class="nav-list">
          <li class="nav-item">
            <router-link to="/admin" class="nav-link" active-class="active" exact-active-class="active">
              <font-awesome-icon :icon="['fas', 'chart-pie']" />
              <span>数据概览</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/users" class="nav-link" active-class="active">
              <font-awesome-icon :icon="['fas', 'users']" />
              <span>用户管理</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/categories" class="nav-link" active-class="active">
              <font-awesome-icon :icon="['fas', 'tags']" />
              <span>分类管理</span>
            </router-link>
          </li>
          <li class="nav-item">
            <div class="nav-section-title">
              <span class="text-muted text-sm">系统管理</span>
            </div>
          </li>
        
          <li class="nav-item">
            <router-link to="/admin/audit-logs" class="nav-link" active-class="active">
              <font-awesome-icon :icon="['fas', 'file-shield']" />
              <span>审计日志</span>
            </router-link>
          </li>
        </ul>
      </nav>
    </aside>
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 顶部导航栏 (浮动卡片式) -->
      <header class="navbar glass-card" ref="navbarRef">
        <div class="navbar-left flex">
          <button class="menu-toggle glass-btn" @click="toggleSidebar">
            <font-awesome-icon :icon="['fas', 'bars']" />
          </button>
          <div class="breadcrumb ml-lg">
            <span class="text-muted">{{ currentPageTitle }}</span>
          </div>
        </div>
        
        <div class="navbar-right flex">
          <div class="system-status mr-md">
            <div class="status-indicator flex-center">
              <div class="status-dot online"></div>
              <span class="text-sm text-muted ml-xs">系统正常</span>
            </div>
          </div>
          
          <div class="notifications mr-md">
            <el-badge :value="5" class="notification-badge">
              <font-awesome-icon :icon="['fas', 'bell']" class="text-lg cursor-pointer" />
            </el-badge>
          </div>
          
          <div class="admin-tools mr-md">
            <el-dropdown>
              <el-button size="small" class="glass-btn">
                <font-awesome-icon :icon="['fas', 'tools']" class="mr-xs" />
                管理工具
                <font-awesome-icon :icon="['fas', 'chevron-down']" class="ml-xs" />
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>
                    <font-awesome-icon :icon="['fas', 'sync']" class="mr-sm" />刷新缓存
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <font-awesome-icon :icon="['fas', 'download']" class="mr-sm" />导出数据
                  </el-dropdown-item>
                  <el-dropdown-item @click="goToAuditLogs">
                    <font-awesome-icon :icon="['fas', 'file-shield']" class="mr-sm" />审计日志
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          
          <div class="user-menu">
            <el-dropdown>
              <div class="user-avatar flex-center">
                <div class="avatar-placeholder">
                  <font-awesome-icon :icon="['fas', 'user-circle']" />
                </div>
                <span class="username ml-sm">管理员</span>
                <font-awesome-icon :icon="['fas', 'chevron-down']" class="ml-sm" />
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>
                    <font-awesome-icon :icon="['fas', 'user']" class="mr-sm" />个人资料
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <font-awesome-icon :icon="['fas', 'cog']" class="mr-sm" />系统设置
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <font-awesome-icon :icon="['fas', 'chart-line']" class="mr-sm" />运营报告
                  </el-dropdown-item>
                  <el-dropdown-item divided>
                    <font-awesome-icon :icon="['fas', 'sign-out-alt']" class="mr-sm" />退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <main class="page-content" ref="contentRef">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { gsap } from 'gsap'

const route = useRoute()
const router = useRouter()
const sidebarOpen = ref(false)

const sidebarRef = ref(null)
const navbarRef = ref(null)
const contentRef = ref(null)

let enterAnim = null

// 页面标题映射
const pageTitles = {
  'AdminDashboard': '数据概览',
  'UserManagement': '用户管理',
  'CategoryManagement': '分类管理',
  'AuditLogs': '审计日志'
}

const currentPageTitle = computed(() => {
  return pageTitles[route.name] || '管理后台'
})

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const closeSidebar = () => {
  sidebarOpen.value = false
}

const goToAuditLogs = () => {
  router.push('/admin/audit-logs')
}

onMounted(() => {
  // GSAP 科技感入场动画
  enterAnim = gsap.timeline()
  
  // Sidebar slides in
  enterAnim.from(sidebarRef.value, {
    x: -50,
    opacity: 0,
    duration: 0.8,
    ease: "power3.out"
  })
  // Navbar drops down
  .from(navbarRef.value, {
    y: -30,
    opacity: 0,
    duration: 0.6,
    ease: "power3.out"
  }, "-=0.6")
  // Content fades in
  .from(contentRef.value, {
    y: 30,
    opacity: 0,
    duration: 0.8,
    ease: "power3.out"
  }, "-=0.4")
})

onUnmounted(() => {
  if (enterAnim) enterAnim.kill()
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f0f4f8;
  position: relative;
  overflow: hidden;
  font-family: 'Inter', 'PingFang SC', sans-serif;
}

/* 科技感背景 */
.tech-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: 
    radial-gradient(circle at 15% 50%, rgba(0, 97, 255, 0.08), transparent 25%),
    radial-gradient(circle at 85% 30%, rgba(96, 239, 255, 0.08), transparent 25%);
  z-index: 0;
  pointer-events: none;
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

.text-gradient {
  background: linear-gradient(135deg, #0061ff 0%, #60efff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 侧边栏浮动设计 */
.sidebar {
  width: 260px;
  margin: 16px 0 16px 16px;
  height: calc(100vh - 32px);
  z-index: 200;
  transition: transform 0.3s ease;
  position: fixed;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
}

.logo-img {
  width: 36px;
  height: 36px;
  object-fit: contain;
  filter: drop-shadow(0 4px 6px rgba(0,97,255,0.3));
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  margin-bottom: 8px;
}

.nav-section-title {
  padding: 20px 24px 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: #4b5563;
  text-decoration: none;
  border-radius: 12px;
  margin: 0 16px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.8);
  color: #0061ff;
  transform: translateX(4px);
}

.nav-link.active {
  background: linear-gradient(135deg, #0061ff 0%, #60efff 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(0, 97, 255, 0.3);
  font-weight: 500;
}

.nav-link i, .nav-link svg {
  width: 20px;
  margin-right: 12px;
  font-size: 1.1em;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  margin-left: 292px; /* 260 + 16 + 16 */
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
  min-height: 100vh;
}

/* 顶部导航浮动卡片 */
.navbar {
  height: 64px;
  margin: 16px 16px 0 0;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.glass-btn {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 10px;
  padding: 8px 16px;
  color: #4b5563;
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.glass-btn:hover {
  background: rgba(255, 255, 255, 0.9);
  color: #0061ff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.menu-toggle {
  display: none;
  width: 40px;
  height: 40px;
  padding: 0;
  justify-content: center;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 10px #10b981;
  animation: pulse-glow 2s infinite;
}

@keyframes pulse-glow {
  0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4); }
  70% { box-shadow: 0 0 0 8px rgba(16, 185, 129, 0); }
  100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
}

.notification-badge {
  cursor: pointer;
  padding: 8px;
  border-radius: 10px;
  transition: all 0.3s ease;
  color: #4b5563;
}

.notification-badge:hover {
  background: rgba(255, 255, 255, 0.7);
  color: #0061ff;
}

.user-avatar {
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.user-avatar:hover {
  background: rgba(255, 255, 255, 0.8);
}

.avatar-placeholder {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0061ff, #60efff);
  color: white;
  font-size: 18px;
  box-shadow: 0 2px 8px rgba(0, 97, 255, 0.3);
}

.username {
  font-weight: 600;
  color: #1f2937;
}

.page-content {
  padding: 24px 16px 24px 0;
  flex: 1;
}

/* 路由切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.sidebar-overlay {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  z-index: 150;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    margin: 0;
    height: 100vh;
    border-radius: 0 20px 20px 0;
    transform: translateX(-100%);
  }
  
  .sidebar.open {
    transform: translateX(0);
  }
  
  .main-content {
    margin-left: 0;
  }
  
  .navbar {
    margin: 16px;
  }
  
  .page-content {
    padding: 16px;
  }
  
  .menu-toggle {
    display: flex;
  }
  
  .admin-tools {
    display: none;
  }
  
  .system-status {
    display: none;
  }
  
  .sidebar-overlay {
    display: block;
  }
  
  .breadcrumb {
    display: none;
  }
}

@media (max-width: 480px) {
  .username {
    display: none;
  }
  
  .notifications {
    margin-right: 8px;
  }
}
</style>