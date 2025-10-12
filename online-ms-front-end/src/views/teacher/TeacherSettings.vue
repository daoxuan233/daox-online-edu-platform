<template>
  <div class="teacher-settings">
    <!-- 页面标题 -->
    <div class="page-header glass-card">
      <div class="header-content">
        <div class="header-icon">
          <font-awesome-icon :icon="['fas', 'cog']" />
        </div>
        <div class="header-text">
          <h1 class="page-title">系统设置</h1>
          <p class="page-subtitle">个性化您的教学环境和系统偏好</p>
        </div>
      </div>
    </div>

    <div class="settings-content">
      <!-- 左侧导航 -->
      <div class="settings-sidebar">
        <div class="settings-nav neu-card">
          <h3 class="nav-title">
            <font-awesome-icon :icon="['fas', 'list']" />
            设置分类
          </h3>
          <ul class="nav-list">
            <li 
              v-for="category in settingsCategories" 
              :key="category.key"
              class="nav-item"
              :class="{ active: activeCategory === category.key }"
              @click="activeCategory = category.key"
            >
              <font-awesome-icon :icon="category.icon" />
              <span>{{ category.label }}</span>
            </li>
          </ul>
        </div>
      </div>

      <!-- 右侧设置内容 -->
      <div class="settings-main">
        <!-- 账户安全设置 -->
        <div v-show="activeCategory === 'security'" class="settings-section neu-card">
          <div class="section-header">
            <h3 class="section-title">
              <font-awesome-icon :icon="['fas', 'shield-alt']" />
              账户安全
            </h3>
            <p class="section-desc">管理您的账户安全设置和登录偏好</p>
          </div>

          <div class="settings-grid">
            <!-- 密码设置 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">登录密码</h4>
                <p class="setting-desc">定期更换密码以保护账户安全</p>
              </div>
              <el-button type="primary" class="neu-button" @click="showPasswordDialog">
                修改密码
              </el-button>
            </div>

            <!-- 两步验证 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">两步验证</h4>
                <p class="setting-desc">为您的账户添加额外的安全保护</p>
              </div>
              <el-switch 
                v-model="securitySettings.twoFactorAuth" 
                class="neu-switch"
                @change="handleTwoFactorChange"
              />
            </div>

            <!-- 登录通知 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">登录通知</h4>
                <p class="setting-desc">当有新设备登录时发送邮件通知</p>
              </div>
              <el-switch 
                v-model="securitySettings.loginNotification" 
                class="neu-switch"
              />
            </div>

            <!-- 会话管理 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">活跃会话</h4>
                <p class="setting-desc">查看和管理您的登录会话</p>
              </div>
              <el-button class="neu-button" @click="showSessionDialog">
                管理会话
              </el-button>
            </div>
          </div>
        </div>

        <!-- 通知设置 -->
        <div v-show="activeCategory === 'notifications'" class="settings-section neu-card">
          <div class="section-header">
            <h3 class="section-title">
              <font-awesome-icon :icon="['fas', 'bell']" />
              通知设置
            </h3>
            <p class="section-desc">自定义您希望接收的通知类型和方式</p>
          </div>

          <div class="notification-groups">
            <div v-for="group in notificationGroups" :key="group.key" class="notification-group">
              <h4 class="group-title">{{ group.title }}</h4>
              <div class="notification-items">
                <div v-for="item in group.items" :key="item.key" class="notification-item">
                  <div class="notification-info">
                    <h5 class="notification-title">{{ item.title }}</h5>
                    <p class="notification-desc">{{ item.description }}</p>
                  </div>
                  <div class="notification-controls">
                    <el-switch 
                      v-model="item.email" 
                      class="neu-switch"
                      size="small"
                    />
                    <span class="control-label">邮件</span>
                    <el-switch 
                      v-model="item.push" 
                      class="neu-switch"
                      size="small"
                    />
                    <span class="control-label">推送</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 界面设置 -->
        <div v-show="activeCategory === 'interface'" class="settings-section neu-card">
          <div class="section-header">
            <h3 class="section-title">
              <font-awesome-icon :icon="['fas', 'palette']" />
              界面设置
            </h3>
            <p class="section-desc">个性化您的界面外观和交互体验</p>
          </div>

          <div class="settings-grid">
            <!-- 主题设置 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">主题模式</h4>
                <p class="setting-desc">选择您偏好的界面主题</p>
              </div>
              <el-select v-model="interfaceSettings.theme" class="neu-select">
                <el-option label="自动" value="auto" />
                <el-option label="浅色" value="light" />
                <el-option label="深色" value="dark" />
              </el-select>
            </div>

            <!-- 语言设置 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">界面语言</h4>
                <p class="setting-desc">选择系统界面显示语言</p>
              </div>
              <el-select v-model="interfaceSettings.language" class="neu-select">
                <el-option label="简体中文" value="zh-CN" />
                <el-option label="English" value="en-US" />
              </el-select>
            </div>

            <!-- 侧边栏设置 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">侧边栏折叠</h4>
                <p class="setting-desc">默认折叠侧边栏以获得更多空间</p>
              </div>
              <el-switch 
                v-model="interfaceSettings.sidebarCollapsed" 
                class="neu-switch"
              />
            </div>

            <!-- 动画效果 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">动画效果</h4>
                <p class="setting-desc">启用界面过渡动画效果</p>
              </div>
              <el-switch 
                v-model="interfaceSettings.animations" 
                class="neu-switch"
              />
            </div>
          </div>
        </div>

        <!-- 教学偏好 -->
        <div v-show="activeCategory === 'teaching'" class="settings-section neu-card">
          <div class="section-header">
            <h3 class="section-title">
              <font-awesome-icon :icon="['fas', 'chalkboard-teacher']" />
              教学偏好
            </h3>
            <p class="section-desc">配置您的教学工具和默认设置</p>
          </div>

          <div class="settings-grid">
            <!-- 默认课程设置 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">默认课程可见性</h4>
                <p class="setting-desc">新创建课程的默认可见性设置</p>
              </div>
              <el-select v-model="teachingSettings.defaultVisibility" class="neu-select">
                <el-option label="公开" value="public" />
                <el-option label="私有" value="private" />
                <el-option label="仅限邀请" value="invite-only" />
              </el-select>
            </div>

            <!-- 自动保存 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">自动保存</h4>
                <p class="setting-desc">编辑课程内容时自动保存</p>
              </div>
              <el-switch 
                v-model="teachingSettings.autoSave" 
                class="neu-switch"
              />
            </div>

            <!-- 学生提交通知 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">作业提交通知</h4>
                <p class="setting-desc">学生提交作业时立即通知</p>
              </div>
              <el-switch 
                v-model="teachingSettings.assignmentNotification" 
                class="neu-switch"
              />
            </div>

            <!-- 评分模式 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">默认评分模式</h4>
                <p class="setting-desc">新建测评的默认评分方式</p>
              </div>
              <el-select v-model="teachingSettings.gradingMode" class="neu-select">
                <el-option label="百分制" value="percentage" />
                <el-option label="等级制" value="grade" />
                <el-option label="通过/不通过" value="pass-fail" />
              </el-select>
            </div>
          </div>
        </div>

        <!-- 隐私设置 -->
        <div v-show="activeCategory === 'privacy'" class="settings-section neu-card">
          <div class="section-header">
            <h3 class="section-title">
              <font-awesome-icon :icon="['fas', 'user-secret']" />
              隐私设置
            </h3>
            <p class="section-desc">控制您的个人信息和数据使用方式</p>
          </div>

          <div class="settings-grid">
            <!-- 个人资料可见性 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">个人资料可见性</h4>
                <p class="setting-desc">控制其他用户查看您个人资料的权限</p>
              </div>
              <el-select v-model="privacySettings.profileVisibility" class="neu-select">
                <el-option label="公开" value="public" />
                <el-option label="仅学生" value="students-only" />
                <el-option label="私有" value="private" />
              </el-select>
            </div>

            <!-- 在线状态 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">显示在线状态</h4>
                <p class="setting-desc">让其他用户看到您的在线状态</p>
              </div>
              <el-switch 
                v-model="privacySettings.showOnlineStatus" 
                class="neu-switch"
              />
            </div>

            <!-- 数据分析 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">使用数据分析</h4>
                <p class="setting-desc">允许系统收集使用数据以改进服务</p>
              </div>
              <el-switch 
                v-model="privacySettings.dataAnalytics" 
                class="neu-switch"
              />
            </div>

            <!-- 第三方集成 -->
            <div class="setting-item">
              <div class="setting-info">
                <h4 class="setting-title">第三方集成</h4>
                <p class="setting-desc">允许第三方应用访问您的数据</p>
              </div>
              <el-switch 
                v-model="privacySettings.thirdPartyIntegration" 
                class="neu-switch"
              />
            </div>
          </div>
        </div>

        <!-- 保存按钮 -->
        <div class="save-section">
          <el-button type="primary" size="large" class="neu-button save-btn" @click="saveSettings" :loading="saving">
            <font-awesome-icon :icon="['fas', 'save']" />
            保存所有设置
          </el-button>
          <el-button size="large" class="neu-button" @click="resetSettings">
            <font-awesome-icon :icon="['fas', 'undo']" />
            恢复默认
          </el-button>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px" class="neu-dialog">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password class="neu-input" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password class="neu-input" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password class="neu-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false" class="neu-button">取消</el-button>
        <el-button type="primary" @click="submitPasswordChange" class="neu-button">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 会话管理对话框 -->
    <el-dialog v-model="sessionDialogVisible" title="活跃会话管理" width="600px" class="neu-dialog">
      <div class="session-list">
        <div v-for="session in activeSessions" :key="session.id" class="session-item">
          <div class="session-info">
            <div class="session-device">
              <font-awesome-icon :icon="session.deviceIcon" />
              <span>{{ session.device }}</span>
            </div>
            <div class="session-details">
              <p class="session-location">{{ session.location }}</p>
              <p class="session-time">最后活跃: {{ session.lastActive }}</p>
            </div>
          </div>
          <div class="session-actions">
            <el-tag v-if="session.current" type="success">当前会话</el-tag>
            <el-button v-else type="danger" size="small" @click="terminateSession(session.id)">
              终止会话
            </el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="sessionDialogVisible = false" class="neu-button">关闭</el-button>
        <el-button type="danger" @click="terminateAllSessions" class="neu-button">终止所有其他会话</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const activeCategory = ref('security')
const saving = ref(false)
const passwordDialogVisible = ref(false)
const sessionDialogVisible = ref(false)

// 设置分类
const settingsCategories = [
  { key: 'security', label: '账户安全', icon: ['fas', 'shield-alt'] },
  { key: 'notifications', label: '通知设置', icon: ['fas', 'bell'] },
  { key: 'interface', label: '界面设置', icon: ['fas', 'palette'] },
  { key: 'teaching', label: '教学偏好', icon: ['fas', 'chalkboard-teacher'] },
  { key: 'privacy', label: '隐私设置', icon: ['fas', 'user-secret'] }
]

// 安全设置
const securitySettings = reactive({
  twoFactorAuth: false,
  loginNotification: true
})

// 界面设置
const interfaceSettings = reactive({
  theme: 'auto',
  language: 'zh-CN',
  sidebarCollapsed: false,
  animations: true
})

// 教学偏好设置
const teachingSettings = reactive({
  defaultVisibility: 'public',
  autoSave: true,
  assignmentNotification: true,
  gradingMode: 'percentage'
})

// 隐私设置
const privacySettings = reactive({
  profileVisibility: 'public',
  showOnlineStatus: true,
  dataAnalytics: true,
  thirdPartyIntegration: false
})

// 通知设置
const notificationGroups = ref([
  {
    key: 'course',
    title: '课程相关',
    items: [
      {
        key: 'new-enrollment',
        title: '新学生注册',
        description: '有新学生注册您的课程时通知',
        email: true,
        push: true
      },
      {
        key: 'assignment-submission',
        title: '作业提交',
        description: '学生提交作业时通知',
        email: true,
        push: false
      },
      {
        key: 'course-discussion',
        title: '课程讨论',
        description: '课程讨论区有新消息时通知',
        email: false,
        push: true
      }
    ]
  },
  {
    key: 'system',
    title: '系统通知',
    items: [
      {
        key: 'system-maintenance',
        title: '系统维护',
        description: '系统维护和更新通知',
        email: true,
        push: true
      },
      {
        key: 'security-alerts',
        title: '安全警报',
        description: '账户安全相关警报',
        email: true,
        push: true
      }
    ]
  }
])

// 密码修改表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 活跃会话数据
const activeSessions = ref([
  {
    id: '1',
    device: 'Windows PC - Chrome',
    deviceIcon: ['fas', 'desktop'],
    location: '北京市 - 中国',
    lastActive: '刚刚',
    current: true
  },
  {
    id: '2',
    device: 'iPhone - Safari',
    deviceIcon: ['fas', 'mobile-alt'],
    location: '上海市 - 中国',
    lastActive: '2小时前',
    current: false
  }
])

// 方法
const showPasswordDialog = () => {
  passwordDialogVisible.value = true
}

const showSessionDialog = () => {
  sessionDialogVisible.value = true
}

const handleTwoFactorChange = (value) => {
  if (value) {
    ElMessage.info('两步验证功能开发中...')
    // 这里可以添加设置两步验证的逻辑
  }
}

const submitPasswordChange = () => {
  // 实现密码修改逻辑
  passwordDialogVisible.value = false
  ElMessage.success('密码修改成功！')
}

const terminateSession = (sessionId) => {
  ElMessageBox.confirm('确定要终止这个会话吗？', '确认操作', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 实现终止会话逻辑
    const index = activeSessions.value.findIndex(s => s.id === sessionId)
    if (index > -1) {
      activeSessions.value.splice(index, 1)
    }
    ElMessage.success('会话已终止')
  })
}

const terminateAllSessions = () => {
  ElMessageBox.confirm('确定要终止所有其他会话吗？这将强制其他设备重新登录。', '确认操作', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 实现终止所有其他会话逻辑
    activeSessions.value = activeSessions.value.filter(s => s.current)
    sessionDialogVisible.value = false
    ElMessage.success('所有其他会话已终止')
  })
}

const saveSettings = () => {
  saving.value = true
  // 模拟保存操作
  setTimeout(() => {
    saving.value = false
    ElMessage.success('设置保存成功！')
  }, 1000)
}

const resetSettings = () => {
  ElMessageBox.confirm('确定要恢复所有设置到默认值吗？', '确认操作', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 重置所有设置到默认值
    ElMessage.success('设置已恢复到默认值')
  })
}

onMounted(() => {
  // 组件挂载时的初始化逻辑
})
</script>

<style scoped>
/* 继承TeacherLayout的CSS变量 */
.teacher-settings {
  padding: var(--spacing-lg);
  background: var(--neumorphism-bg);
  min-height: 100vh;
}

/* 页面标题样式 */
.page-header {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-backdrop);
  -webkit-backdrop-filter: var(--glass-backdrop);
  border: 1px solid var(--glass-border);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(0, 47, 167, 0.1) 0%, 
    rgba(81, 123, 77, 0.1) 100%);
  pointer-events: none;
}

.header-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  position: relative;
  z-index: 1;
}

.header-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 0 8px 16px rgba(0, 47, 167, 0.3);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  color: var(--text-secondary);
  margin: var(--spacing-xs) 0 0 0;
  font-size: 16px;
}

/* 主要内容布局 */
.settings-content {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: var(--spacing-lg);
}

/* 新拟态卡片样式 */
.neu-card {
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  box-shadow: 
    8px 8px 16px var(--neumorphism-shadow-dark),
    -8px -8px 16px var(--neumorphism-shadow-light);
  margin-bottom: var(--spacing-lg);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.neu-card::before {
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

/* 左侧导航 */
.nav-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-lg) 0;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  position: relative;
  z-index: 1;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
  position: relative;
  z-index: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
  border-radius: var(--border-radius-md);
  cursor: pointer;
  transition: all var(--transition-normal);
  color: var(--text-secondary);
  background: var(--neumorphism-bg);
  box-shadow: 
    4px 4px 8px var(--neumorphism-shadow-dark),
    -4px -4px 8px var(--neumorphism-shadow-light);
}

.nav-item:hover {
  color: var(--primary-color);
  transform: translateY(-1px);
  box-shadow: 
    6px 6px 12px var(--neumorphism-shadow-dark),
    -6px -6px 12px var(--neumorphism-shadow-light);
}

.nav-item.active {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  box-shadow: 
    inset 4px 4px 8px rgba(0, 0, 0, 0.2),
    inset -4px -4px 8px rgba(255, 255, 255, 0.1),
    0 8px 16px rgba(81, 123, 77, 0.3);
}

/* 设置区域 */
.settings-section {
  position: relative;
  z-index: 1;
}

.section-header {
  margin-bottom: var(--spacing-xl);
  position: relative;
  z-index: 1;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-sm) 0;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.section-desc {
  color: var(--text-secondary);
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
}

.settings-grid {
  display: grid;
  gap: var(--spacing-lg);
  position: relative;
  z-index: 1;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-md);
  box-shadow: 
    4px 4px 8px var(--neumorphism-shadow-dark),
    -4px -4px 8px var(--neumorphism-shadow-light);
  transition: all var(--transition-normal);
}

.setting-item:hover {
  transform: translateY(-1px);
  box-shadow: 
    6px 6px 12px var(--neumorphism-shadow-dark),
    -6px -6px 12px var(--neumorphism-shadow-light);
}

.setting-info {
  flex: 1;
}

.setting-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.setting-desc {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
  line-height: 1.4;
}

/* 通知设置 */
.notification-groups {
  display: grid;
  gap: var(--spacing-xl);
  position: relative;
  z-index: 1;
}

.notification-group {
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-md);
  padding: var(--spacing-lg);
  box-shadow: 
    4px 4px 8px var(--neumorphism-shadow-dark),
    -4px -4px 8px var(--neumorphism-shadow-light);
}

.group-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-lg) 0;
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--glass-border);
}

.notification-items {
  display: grid;
  gap: var(--spacing-md);
}

.notification-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md);
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-sm);
  box-shadow: 
    inset 2px 2px 4px var(--neumorphism-shadow-dark),
    inset -2px -2px 4px var(--neumorphism-shadow-light);
}

.notification-info {
  flex: 1;
}

.notification-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.notification-desc {
  color: var(--text-secondary);
  font-size: 12px;
  margin: 0;
  line-height: 1.3;
}

.notification-controls {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.control-label {
  font-size: 12px;
  color: var(--text-auxiliary);
}

/* 表单控件样式 */
.neu-input :deep(.el-input__wrapper) {
  background: var(--neumorphism-bg);
  border: none;
  border-radius: var(--border-radius-md);
  box-shadow: 
    inset 4px 4px 8px var(--neumorphism-shadow-dark),
    inset -4px -4px 8px var(--neumorphism-shadow-light);
  transition: all var(--transition-normal);
}

.neu-input :deep(.el-input__wrapper:hover) {
  box-shadow: 
    inset 6px 6px 12px var(--neumorphism-shadow-dark),
    inset -6px -6px 12px var(--neumorphism-shadow-light);
}

.neu-select :deep(.el-select__wrapper) {
  background: var(--neumorphism-bg);
  border: none;
  border-radius: var(--border-radius-md);
  box-shadow: 
    inset 4px 4px 8px var(--neumorphism-shadow-dark),
    inset -4px -4px 8px var(--neumorphism-shadow-light);
}

.neu-switch :deep(.el-switch__core) {
  background: var(--neumorphism-bg);
  border: none;
  box-shadow: 
    inset 4px 4px 8px var(--neumorphism-shadow-dark),
    inset -4px -4px 8px var(--neumorphism-shadow-light);
}

.neu-switch :deep(.el-switch__core::after) {
  background: var(--neumorphism-bg);
  box-shadow: 
    2px 2px 4px var(--neumorphism-shadow-dark),
    -2px -2px 4px var(--neumorphism-shadow-light);
}

.neu-button {
  background: var(--neumorphism-bg);
  border: none;
  border-radius: var(--border-radius-md);
  padding: var(--spacing-md) var(--spacing-lg);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: 
    4px 4px 8px var(--neumorphism-shadow-dark),
    -4px -4px 8px var(--neumorphism-shadow-light);
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.neu-button:hover {
  transform: translateY(-1px);
  box-shadow: 
    6px 6px 12px var(--neumorphism-shadow-dark),
    -6px -6px 12px var(--neumorphism-shadow-light);
}

.neu-button.el-button--primary {
  background: linear-gradient(135deg, var(--primary-color), #4A90E2);
  color: white;
}

.neu-button.el-button--danger {
  background: linear-gradient(135deg, var(--danger-color), #FF8A80);
  color: white;
}

/* 保存区域 */
.save-section {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
  padding: var(--spacing-xl) 0;
  border-top: 1px solid var(--glass-border);
  margin-top: var(--spacing-xl);
}

.save-btn {
  font-size: 16px;
  padding: var(--spacing-lg) var(--spacing-xl);
}

/* 会话管理 */
.session-list {
  display: grid;
  gap: var(--spacing-md);
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-md);
  box-shadow: 
    4px 4px 8px var(--neumorphism-shadow-dark),
    -4px -4px 8px var(--neumorphism-shadow-light);
}

.session-info {
  flex: 1;
}

.session-device {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.session-details {
  color: var(--text-secondary);
  font-size: 14px;
}

.session-location, .session-time {
  margin: 0;
  line-height: 1.4;
}

.session-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

/* 对话框样式 */
.neu-dialog :deep(.el-dialog) {
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-lg);
  box-shadow: 
    8px 8px 16px var(--neumorphism-shadow-dark),
    -8px -8px 16px var(--neumorphism-shadow-light);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .settings-content {
    grid-template-columns: 250px 1fr;
  }
}

@media (max-width: 768px) {
  .settings-content {
    grid-template-columns: 1fr;
  }
  
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md);
  }
  
  .notification-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }
  
  .notification-controls {
    align-self: stretch;
    justify-content: space-between;
  }
}

@media (max-width: 480px) {
  .teacher-settings {
    padding: var(--spacing-md);
  }
  
  .page-header {
    padding: var(--spacing-lg);
  }
  
  .header-content {
    flex-direction: column;
    text-align: center;
  }
  
  .neu-card {
    padding: var(--spacing-lg);
  }
  
  .save-section {
    flex-direction: column;
  }
}
</style>