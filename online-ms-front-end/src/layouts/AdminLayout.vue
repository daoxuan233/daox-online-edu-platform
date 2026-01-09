<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar glass-sidebar" :class="{ 'open': sidebarOpen }">
      <div class="sidebar-header">
        <div class="logo flex-center">
          <img src="/DaoX_C7-Center_Logo.svg" alt="DaoX Logo" class="mr-sm" style="width: 32px; height: 32px; object-fit: contain;" />
          <h2 class="text-lg font-bold text-primary">管理后台</h2>
        </div>
      </div>
      
      <nav class="sidebar-nav mt-lg">
        <ul class="nav-list">
          <li class="nav-item">
            <router-link to="/admin" class="nav-link" active-class="active">
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
            <a href="#" class="nav-link">
              <font-awesome-icon :icon="['fas', 'database']" />
              <span>数据备份</span>
            </a>
          </li>
          <li class="nav-item">
            <a href="#" class="nav-link">
              <font-awesome-icon :icon="['fas', 'shield-alt']" />
              <span>安全设置</span>
            </a>
          </li>
          <li class="nav-item">
            <a href="#" class="nav-link">
              <font-awesome-icon :icon="['fas', 'file-alt']" />
              <span>系统日志</span>
            </a>
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
              <el-button size="small" class="neumorphism-button">
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
                  <el-dropdown-item>
                    <font-awesome-icon :icon="['fas', 'broom']" class="mr-sm" />清理日志
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
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const sidebarOpen = ref(false)

// 页面标题映射
const pageTitles = {
  'AdminDashboard': '数据概览',
  'UserManagement': '用户管理',
  'CategoryManagement': '分类管理'
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
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--background-color);
}

.sidebar {
  width: 250px;
  z-index: 200;
  transition: transform var(--transition-normal);
}

.sidebar-header {
  padding: var(--spacing-lg) 0;
  border-bottom: 1px solid var(--glass-border);
}

.logo {
  padding: 0 var(--spacing-md);
}

.nav-list {
  list-style: none;
}

.nav-item {
  margin-bottom: var(--spacing-xs);
}

.nav-section-title {
  padding: var(--spacing-md) var(--spacing-md) var(--spacing-sm);
  margin-top: var(--spacing-lg);
}

.nav-link {
  display: flex;
  align-items: center;
  padding: var(--spacing-md);
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: var(--border-radius-sm);
  margin: 0 var(--spacing-sm);
  transition: all var(--transition-fast);
}

.nav-link:hover {
  background: var(--glass-background);
  color: var(--primary-color);
  transform: translateX(4px);
}

.nav-link.active {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  box-shadow: 0 4px 12px rgba(0, 47, 167, 0.3);
}

.nav-link i {
  width: 20px;
  margin-right: var(--spacing-sm);
}

.main-content {
  flex: 1;
  margin-left: 250px;
  transition: margin-left var(--transition-normal);
}

.navbar {
  height: 64px;
  padding: 0 var(--spacing-lg);
  border-bottom: 1px solid var(--glass-border);
}

.menu-toggle {
  display: none;
  width: 40px;
  height: 40px;
  border-radius: var(--border-radius-sm);
}

.system-status {
  padding: var(--spacing-sm);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #67C23A;
  animation: pulse 2s infinite;
}

.status-dot.online {
  background: #67C23A;
}

.status-dot.warning {
  background: #E6A23C;
}

.status-dot.error {
  background: #F56C6C;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(103, 194, 58, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0);
  }
}

.notification-badge {
  cursor: pointer;
  padding: var(--spacing-sm);
  border-radius: var(--border-radius-sm);
  transition: background var(--transition-fast);
}

.notification-badge:hover {
  background: var(--glass-background);
}

.user-avatar {
  cursor: pointer;
  padding: var(--spacing-sm);
  border-radius: var(--border-radius-sm);
  transition: background var(--transition-fast);
}

.user-avatar:hover {
  background: var(--glass-background);
}

.avatar-placeholder {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #002FA7, #517B4D);
  color: white;
  font-size: 18px;
  border: 2px solid var(--primary-color);
}

.username {
  font-weight: 500;
  color: var(--text-primary);
}

.page-content {
  padding: var(--spacing-lg);
  min-height: calc(100vh - 64px);
}

.sidebar-overlay {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 150;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    transform: translateX(-100%);
    z-index: 200;
  }
  
  .sidebar.open {
    transform: translateX(0);
  }
  
  .main-content {
    margin-left: 0;
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
    margin-right: var(--spacing-sm);
  }
}
</style>