<template>
  <div class="profile-page">
    <!-- 页面头部 -->
    <div class="page-header glass-card">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <font-awesome-icon :icon="['fas', 'user-circle']" />
            个人资料
          </h1>
          <p class="page-subtitle">管理您的个人信息和账户设置</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" class="neu-button" @click="saveProfile">
            <font-awesome-icon :icon="['fas', 'save']" />
            保存更改
          </el-button>
        </div>
      </div>
    </div>

    <div class="profile-content">
      <!-- 侧边导航 -->
      <ProfileSidebar 
        :active-tab="activeTab" 
        @tab-change="activeTab = $event" 
      />

      <!-- 主要内容区域 -->
      <div class="profile-main">
        <!-- 基本信息 -->
        <div v-if="activeTab === 'basic'" class="profile-section neu-card">
          <div class="section-header">
            <h2 class="section-title">
              <font-awesome-icon :icon="['fas', 'user']" />
              基本信息
            </h2>
            <p class="section-subtitle">更新您的基本个人信息</p>
          </div>

          <div class="profile-form">
            <!-- 头像上传 -->
            <div class="avatar-section">
              <div class="avatar-upload">
                <div class="avatar-preview neu-card">
                  <img v-if="userInfo.avatar" :src="userInfo.avatar" alt="头像" class="avatar-image" />
                  <div v-else class="avatar-placeholder">
                    <font-awesome-icon :icon="['fas', 'user']" />
                  </div>
                  <div class="avatar-overlay">
                    <font-awesome-icon :icon="['fas', 'camera']" />
                  </div>
                </div>
                <div class="avatar-actions">
                  <el-button size="small" class="neu-button upload-btn" @click="uploadAvatar">
                    <font-awesome-icon :icon="['fas', 'upload']" />
                    上传头像
                  </el-button>
                  <el-button size="small" class="neu-button save-btn" type="success" @click="updateAvatar">
                    <font-awesome-icon :icon="['fas', 'save']" />
                    保存头像
                  </el-button>
                  <el-button size="small" class="neu-button delete-btn" type="danger" @click="removeAvatar">
                    <font-awesome-icon :icon="['fas', 'trash']" />
                    删除
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 基本信息表单 -->
            <div class="form-grid">
              <div class="form-group">
                <label class="form-label">用户名</label>
                <el-input 
                  v-model="userInfo.username" 
                  placeholder="请输入用户名"
                  class="neu-input"
                  disabled
                >
                  <template #prefix>
                    <font-awesome-icon :icon="['fas', 'user']" />
                  </template>
                </el-input>
                <span class="form-hint">用户名不可修改</span>
              </div>

              <div class="form-group">
                <label class="form-label">学号</label>
                <el-input 
                  v-model="userInfo.identifier" 
                  placeholder="学号"
                  class="neu-input"
                  disabled
                >
                  <template #prefix>
                    <font-awesome-icon :icon="['fas', 'id-card']" />
                  </template>
                </el-input>
                <span class="form-hint">学号不可修改</span>
              </div>

              <div class="form-group">
                <label class="form-label">角色</label>
                <el-input 
                  :value="getRoleDisplayName(userInfo.role)" 
                  placeholder="角色"
                  class="neu-input"
                  disabled
                >
                  <template #prefix>
                    <font-awesome-icon :icon="['fas', 'user-tag']" />
                  </template>
                </el-input>
                <span class="form-hint">角色不可修改</span>
              </div>

              <div class="form-group">
                <label class="form-label">邮箱地址</label>
                <el-input 
                  v-model="userInfo.email" 
                  placeholder="请输入邮箱地址"
                  class="neu-input"
                >
                  <template #prefix>
                    <font-awesome-icon :icon="['fas', 'envelope']" />
                  </template>
                  <template #suffix>
                    <el-tag v-if="userInfo.emailVerified" type="success" size="small">
                      已验证
                    </el-tag>
                    <el-button v-else type="primary" text size="small" @click="verifyEmail">
                      验证
                    </el-button>
                  </template>
                </el-input>
              </div>

              <div class="form-group">
                <label class="form-label">手机号码</label>
                <el-input 
                  v-model="userInfo.phone" 
                  placeholder="请输入手机号码"
                  class="neu-input"
                >
                  <template #prefix>
                    <font-awesome-icon :icon="['fas', 'phone']" />
                  </template>
                  <template #suffix>
                    <el-tag v-if="userInfo.phoneVerified" type="success" size="small">
                      已验证
                    </el-tag>
                    <el-button v-else type="primary" text size="small" @click="verifyPhone">
                      验证
                    </el-button>
                  </template>
                </el-input>
              </div>

              <div class="form-group">
                <label class="form-label">性别</label>
                <el-radio-group v-model="userInfo.gender" class="gender-group">
                  <el-radio value="man" class="neu-radio">
                    <font-awesome-icon :icon="['fas', 'mars']" />
                    男
                  </el-radio>
                  <el-radio value="female" class="neu-radio">
                    <font-awesome-icon :icon="['fas', 'venus']" />
                    女
                  </el-radio>
                  <el-radio value="other" class="neu-radio">
                    <font-awesome-icon :icon="['fas', 'genderless']" />
                    其他
                  </el-radio>
                </el-radio-group>
              </div>

              <div class="form-group">
                <label class="form-label">出生日期</label>
                <el-date-picker
                  v-model="userInfo.birthday"
                  type="date"
                  placeholder="选择出生日期"
                  class="neu-input"
                  style="width: 100%"
                />
              </div>

              <div class="form-group full-width">
                <label class="form-label">个人简介</label>
                <el-input 
                  v-model="userInfo.bio" 
                  type="textarea"
                  :rows="4"
                  placeholder="介绍一下自己..."
                  class="neu-input"
                  show-word-limit
                  maxlength="200"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 学习偏好 -->
        <div v-if="activeTab === 'preferences'" class="profile-section neu-card">
          <div class="section-header">
            <h2 class="section-title">
              <font-awesome-icon :icon="['fas', 'cog']" />
              学习偏好
            </h2>
            <p class="section-subtitle">设置您的学习偏好和兴趣领域</p>
          </div>

          <div class="preferences-content">
            <!-- 兴趣领域 -->
            <div class="preference-group">
              <h3 class="group-title">兴趣领域</h3>
              <div class="interest-tags">
                <div 
                  v-for="interest in availableInterests" 
                  :key="interest.id"
                  class="interest-tag"
                  :class="{ selected: userPreferences.interests.includes(interest.id) }"
                  @click="toggleInterest(interest.id)"
                >
                  <font-awesome-icon :icon="[interest.iconType, interest.icon]" />
                  <span>{{ interest.name }}</span>
                </div>
              </div>
            </div>

            <!-- 学习目标 -->
            <div class="preference-group">
              <h3 class="group-title">学习目标</h3>
              <el-radio-group v-model="userPreferences.learningGoal" class="goal-group">
                <el-radio value="career" class="goal-option neu-card">
                  <div class="goal-content">
                    <font-awesome-icon :icon="['fas', 'briefcase']" />
                    <div>
                      <div class="goal-title">职业发展</div>
                      <div class="goal-desc">提升职业技能，获得更好的工作机会</div>
                    </div>
                  </div>
                </el-radio>
                <el-radio value="academic" class="goal-option neu-card">
                  <div class="goal-content">
                    <font-awesome-icon :icon="['fas', 'graduation-cap']" />
                    <div>
                      <div class="goal-title">学术研究</div>
                      <div class="goal-desc">深入学习专业知识，进行学术研究</div>
                    </div>
                  </div>
                </el-radio>
                <el-radio value="hobby" class="goal-option neu-card">
                  <div class="goal-content">
                    <font-awesome-icon :icon="['fas', 'heart']" />
                    <div>
                      <div class="goal-title">兴趣爱好</div>
                      <div class="goal-desc">出于兴趣学习，丰富个人知识</div>
                    </div>
                  </div>
                </el-radio>
              </el-radio-group>
            </div>

            <!-- 学习时间 -->
            <div class="preference-group">
              <h3 class="group-title">每日学习时间</h3>
              <el-slider
                v-model="userPreferences.dailyStudyTime"
                :min="30"
                :max="480"
                :step="30"
                :format-tooltip="formatStudyTime"
                class="study-time-slider"
              />
              <div class="time-display">
                <span>{{ formatStudyTime(userPreferences.dailyStudyTime) }}</span>
              </div>
            </div>

            <!-- 通知设置 -->
            <div class="preference-group">
              <h3 class="group-title">通知设置</h3>
              <div class="notification-settings">
                <div class="setting-item">
                  <div class="setting-info">
                    <div class="setting-title">课程提醒</div>
                    <div class="setting-desc">新课程和课程更新通知</div>
                  </div>
                  <el-switch v-model="userPreferences.notifications.course" />
                </div>
                <div class="setting-item">
                  <div class="setting-info">
                    <div class="setting-title">作业提醒</div>
                    <div class="setting-desc">作业截止日期提醒</div>
                  </div>
                  <el-switch v-model="userPreferences.notifications.assignment" />
                </div>
                <div class="setting-item">
                  <div class="setting-info">
                    <div class="setting-title">学习报告</div>
                    <div class="setting-desc">每周学习进度报告</div>
                  </div>
                  <el-switch v-model="userPreferences.notifications.report" />
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 安全设置 -->
        <div v-if="activeTab === 'security'" class="profile-section neu-card">
          <div class="section-header">
            <h2 class="section-title">
              <font-awesome-icon :icon="['fas', 'shield-alt']" />
              安全设置
            </h2>
            <p class="section-subtitle">管理您的账户安全和隐私设置</p>
          </div>

          <div class="security-content">
            <!-- 密码修改 -->
            <div class="security-group">
              <h3 class="group-title">修改密码</h3>
              <div class="password-form">
                <div class="form-group">
                  <label class="form-label">当前密码</label>
                  <el-input 
                    v-model="passwordForm.currentPassword" 
                    type="password"
                    placeholder="请输入当前密码"
                    class="neu-input"
                    show-password
                  >
                    <template #prefix>
                      <font-awesome-icon :icon="['fas', 'lock']" />
                    </template>
                  </el-input>
                </div>
                <div class="form-group">
                  <label class="form-label">新密码</label>
                  <el-input 
                    v-model="passwordForm.newPassword" 
                    type="password"
                    placeholder="请输入新密码"
                    class="neu-input"
                    show-password
                  >
                    <template #prefix>
                      <font-awesome-icon :icon="['fas', 'key']" />
                    </template>
                  </el-input>
                  <div class="password-strength">
                    <div class="strength-bar">
                      <div class="strength-fill" :style="{ width: passwordStrength + '%', backgroundColor: getPasswordStrengthColor() }"></div>
                    </div>
                    <span class="strength-text">{{ getPasswordStrengthText() }}</span>
                  </div>
                </div>
                <div class="form-group">
                  <label class="form-label">确认新密码</label>
                  <el-input 
                    v-model="passwordForm.confirmPassword" 
                    type="password"
                    placeholder="请再次输入新密码"
                    class="neu-input"
                    show-password
                  >
                    <template #prefix>
                      <font-awesome-icon :icon="['fas', 'check']" />
                    </template>
                  </el-input>
                </div>
                <el-button type="primary" class="neu-button" @click="changePassword">
                  <font-awesome-icon :icon="['fas', 'save']" />
                  更新密码
                </el-button>
              </div>
            </div>

            <!-- 两步验证 -->
            <div class="security-group">
              <h3 class="group-title">两步验证</h3>
              <div class="two-factor-setting">
                <div class="setting-item">
                  <div class="setting-info">
                    <div class="setting-title">启用两步验证</div>
                    <div class="setting-desc">为您的账户添加额外的安全保护</div>
                  </div>
                  <div class="setting-action">
                    <el-switch v-model="securitySettings.twoFactorEnabled" @change="toggleTwoFactor" />
                  </div>
                </div>
                <div v-if="securitySettings.twoFactorEnabled" class="two-factor-methods">
                  <div class="method-item">
                    <font-awesome-icon :icon="['fas', 'mobile-alt']" />
                    <span>短信验证</span>
                    <el-tag type="success" size="small">已启用</el-tag>
                  </div>
                  <div class="method-item">
                    <font-awesome-icon :icon="['fas', 'qrcode']" />
                    <span>身份验证器</span>
                    <el-button size="small" text>设置</el-button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 登录记录 -->
            <div class="security-group">
              <h3 class="group-title">登录记录</h3>
              <div class="login-history">
                <div v-for="record in loginHistory" :key="record.id" class="history-item">
                  <div class="history-info">
                    <div class="history-device">
                      <font-awesome-icon :icon="[getDeviceIcon(record.device).iconType, getDeviceIcon(record.device).icon]" />
                      <span>{{ record.device }}</span>
                    </div>
                    <div class="history-location">
                      <font-awesome-icon :icon="['fas', 'map-marker-alt']" />
                      <span>{{ record.location }}</span>
                    </div>
                    <div class="history-time">
                      <font-awesome-icon :icon="['fas', 'clock']" />
                      <span>{{ formatDate(record.time) }}</span>
                    </div>
                  </div>
                  <div class="history-status">
                    <el-tag v-if="record.current" type="success" size="small">
                      当前会话
                    </el-tag>
                    <el-button v-else size="small" text type="danger">
                      终止会话
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 隐私设置 -->
        <div v-if="activeTab === 'privacy'" class="profile-section neu-card">
          <div class="section-header">
            <h2 class="section-title">
              <font-awesome-icon :icon="['fas', 'user-secret']" />
              隐私设置
            </h2>
            <p class="section-subtitle">控制您的个人信息可见性</p>
          </div>

          <div class="privacy-content">
            <div class="privacy-group">
              <h3 class="group-title">个人信息可见性</h3>
              <div class="privacy-settings">
                <div class="setting-item">
                  <div class="setting-info">
                    <div class="setting-title">真实姓名</div>
                    <div class="setting-desc">其他用户是否可以看到您的真实姓名</div>
                  </div>
                  <el-select v-model="privacySettings.realNameVisibility" class="privacy-select">
                    <el-option label="公开" value="public" />
                    <el-option label="仅好友" value="friends" />
                    <el-option label="私密" value="private" />
                  </el-select>
                </div>
                <div class="setting-item">
                  <div class="setting-info">
                    <div class="setting-title">邮箱地址</div>
                    <div class="setting-desc">其他用户是否可以看到您的邮箱</div>
                  </div>
                  <el-select v-model="privacySettings.emailVisibility" class="privacy-select">
                    <el-option label="公开" value="public" />
                    <el-option label="仅好友" value="friends" />
                    <el-option label="私密" value="private" />
                  </el-select>
                </div>
                <div class="setting-item">
                  <div class="setting-info">
                    <div class="setting-title">学习进度</div>
                    <div class="setting-desc">其他用户是否可以看到您的学习进度</div>
                  </div>
                  <el-select v-model="privacySettings.progressVisibility" class="privacy-select">
                    <el-option label="公开" value="public" />
                    <el-option label="仅好友" value="friends" />
                    <el-option label="私密" value="private" />
                  </el-select>
                </div>
              </div>
            </div>

            <div class="privacy-group">
              <h3 class="group-title">数据使用</h3>
              <div class="data-settings">
                <div class="setting-item">
                  <div class="setting-info">
                    <div class="setting-title">个性化推荐</div>
                    <div class="setting-desc">允许系统根据您的学习行为推荐相关内容</div>
                  </div>
                  <el-switch v-model="privacySettings.personalizedRecommendation" />
                </div>
                <div class="setting-item">
                  <div class="setting-info">
                    <div class="setting-title">学习分析</div>
                    <div class="setting-desc">允许系统分析您的学习数据以改善服务</div>
                  </div>
                  <el-switch v-model="privacySettings.learningAnalytics" />
                </div>
                <div class="setting-item">
                  <div class="setting-info">
                    <div class="setting-title">营销邮件</div>
                    <div class="setting-desc">接收课程推荐和平台更新邮件</div>
                  </div>
                  <el-switch v-model="privacySettings.marketingEmails" />
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
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AVATAR_LARGE } from '@/utils/placeholders'
import { getStudentProfile, updateProfile as updateProfileAPI } from '@/api/students/stuAPI.js'
import {getIdentifier, getUserId} from '@/utils/tokenAnalysis.js'
import { getRoleDisplayName } from '@/utils/roleUtils.js'
import { uploadAvatar as uploadAvatarAPI} from '@/api/publicApi.js'
import { updateAvatar as updateAvatarAPI } from '@/api/students/stuAPI.js'
import ProfileSidebar from '@/components/ProfileSidebar.vue'

// 响应式数据
const activeTab = ref('basic')

// 用户信息
const userInfo = ref({
  id: '',
  username: '',
  identifier: '',
  role: '',
  email: '@example.com',
  phone: '',
  gender: '',
  birthday: new Date('1995-06-15'),
  bio: '',
  avatar: AVATAR_LARGE,
  emailVerified: true,
  phoneVerified: false
})

// 学习偏好
const userPreferences = ref({
  interests: [1, 3, 5],
  learningGoal: 'career',
  dailyStudyTime: 120,
  notifications: {
    course: true,
    assignment: true,
    report: false
  }
})

// 全局变量，存储上传图像后后端返回的永久访问链接
const permanentAvatarUrl = ref('')

// 可选兴趣领域
const availableInterests = ref([
  { id: 1, name: '前端开发', icon: 'js-square', iconType: 'fab' },
  { id: 2, name: '后端开发', icon: 'server', iconType: 'fas' },
  { id: 3, name: '移动开发', icon: 'mobile-alt', iconType: 'fas' },
  { id: 4, name: '数据科学', icon: 'chart-bar', iconType: 'fas' },
  { id: 5, name: '人工智能', icon: 'robot', iconType: 'fas' },
  { id: 6, name: '云计算', icon: 'cloud', iconType: 'fas' },
  { id: 7, name: '网络安全', icon: 'shield-alt', iconType: 'fas' },
  { id: 8, name: '区块链', icon: 'link', iconType: 'fas' },
  { id: 9, name: '游戏开发', icon: 'gamepad', iconType: 'fas' },
  { id: 10, name: '设计', icon: 'palette', iconType: 'fas' }
])

// 密码表单
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 安全设置
const securitySettings = ref({
  twoFactorEnabled: false
})

// 隐私设置
const privacySettings = ref({
  realNameVisibility: 'public',
  emailVisibility: 'friends',
  progressVisibility: 'public',
  personalizedRecommendation: true,
  learningAnalytics: true,
  marketingEmails: false
})

// 登录记录
const loginHistory = ref([
  {
    id: 1,
    device: 'Chrome on Windows',
    location: '北京市',
    time: new Date(),
    current: true
  },
  {
    id: 2,
    device: 'Safari on iPhone',
    location: '上海市',
    time: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000),
    current: false
  },
  {
    id: 3,
    device: 'Firefox on Mac',
    location: '广州市',
    time: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
    current: false
  }
])

// 计算属性
const passwordStrength = computed(() => {
  const password = passwordForm.value.newPassword
  if (!password) return 0
  
  let strength = 0
  if (password.length >= 8) strength += 25
  if (/[a-z]/.test(password)) strength += 25
  if (/[A-Z]/.test(password)) strength += 25
  if (/[0-9]/.test(password)) strength += 25
  
  return strength
})

// 方法
const toggleInterest = (interestId) => {
  const index = userPreferences.value.interests.indexOf(interestId)
  if (index > -1) {
    userPreferences.value.interests.splice(index, 1)
  } else {
    userPreferences.value.interests.push(interestId)
  }
}

const formatStudyTime = (minutes) => {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0) {
    return `${hours}小时${mins > 0 ? mins + '分钟' : ''}`
  }
  return `${mins}分钟`
}

const getPasswordStrengthColor = () => {
  const strength = passwordStrength.value
  if (strength < 50) return '#F56C6C'
  if (strength < 75) return '#E6A23C'
  return '#67C23A'
}

const getPasswordStrengthText = () => {
  const strength = passwordStrength.value
  if (strength === 0) return ''
  if (strength < 50) return '弱'
  if (strength < 75) return '中'
  return '强'
}

const getDeviceIcon = (device) => {
  if (device.includes('iPhone') || device.includes('Android')) {
    return { iconType: 'fas', icon: 'mobile-alt' }
  }
  if (device.includes('Mac')) {
    return { iconType: 'fab', icon: 'apple' }
  }
  if (device.includes('Windows')) {
    return { iconType: 'fab', icon: 'windows' }
  }
  return { iconType: 'fas', icon: 'desktop' }
}

const formatDate = (date) => {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const uploadAvatar = () => {
  // 创建文件输入元素
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.style.display = 'none'
  
  input.onchange = async (event) => {
    const file = event.target.files[0]
    if (!file) return
    
    // 文件大小验证（限制为5MB）
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('头像文件大小不能超过5MB')
      return
    }
    
    // 文件类型验证
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件')
      return
    }
    
    try {
      // 获取用户ID
      const userId = getIdentifier()
      if (!userId) {
        ElMessage.error('无法获取用户信息，请重新登录')
        return
      }
      
      ElMessage.info('正在上传头像...')
      
      // 调用上传API
      const data = await uploadAvatarAPI(file)
      
      // 获取永久访问链接并更新用户头像
      const permanentUrl = data.permanentUrl
      userInfo.value.avatar = permanentUrl
      
      // 存储在全局变量中
      permanentAvatarUrl.value = permanentUrl
      
      ElMessage.success('头像上传成功')
      console.log('头像上传成功，永久链接:', permanentUrl)
      console.log('完整响应数据:', data)
    } catch (error) {
      console.error('头像上传失败:', error)
      ElMessage.error('头像上传失败，请稍后重试')
    }
  }
  
  // 触发文件选择
  document.body.appendChild(input)
  input.click()
  document.body.removeChild(input)
}

const removeAvatar = () => {
  userInfo.value.avatar = ''
  // 清空全局变量中的永久链接
  permanentAvatarUrl.value = ''
  ElMessage.success('头像已删除')
}



const verifyEmail = () => {
  // 邮箱验证逻辑
  ElMessage.info('验证邮件已发送')
}

const verifyPhone = () => {
  // 手机验证逻辑
  ElMessage.info('验证短信已发送')
}

// 加载用户数据
const loadUserProfile = async () => {
  try {
    const identifier = getIdentifier()
    if (!identifier) {
      ElMessage.error('无法获取用户标识，请重新登录')
      return
    }
    
    getStudentProfile(
      identifier,
      (data) => {
        // 成功回调
        if (data) {
          const profile = data
          userInfo.value = {
            ...userInfo.value,
            id: profile.id || userInfo.value.id,
            username: profile.nickname || userInfo.value.username,
            identifier: profile.identifier || userInfo.value.identifier,
            role: profile.role || userInfo.value.role,
            email: profile.email || userInfo.value.email,
            phone: profile.phone || userInfo.value.phone,
            gender: profile.gender || userInfo.value.gender,
            birthday: profile.birthday || userInfo.value.birthday,
            bio:profile.biography || userInfo.value.bio,
            avatar: profile.avatarUrl || userInfo.value.avatar
          }
          ElMessage.success('个人信息加载成功')
          console.log('用户信息已更新:', userInfo.value)
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

const saveProfile = async () => {
  try {
    // 构建要发送的个人资料数据，匹配后端 ProfileVo 结构
    const profileData = {
      id: userInfo.value.id,
      identifier: userInfo.value.identifier,
      nickname: userInfo.value.username,
      email: userInfo.value.email,
      role: userInfo.value.role,
      avatarUrl: userInfo.value.avatar,
      phone: userInfo.value.phone,
      gender: userInfo.value.gender,
      birthday: userInfo.value.birthday,
      biography: userInfo.value.bio
    }
    
    console.log('开始保存个人资料:', profileData)
    
    // 调用更新个人资料API
    const result = await updateProfileAPI(profileData)
    
    console.log('保存个人资料成功:', result)
    ElMessage.success('个人资料已保存')
    
    // 重新加载用户信息
    await loadUserProfile()
    
  } catch (error) {
    console.error('保存个人资料失败:', error)
    ElMessage.error(`保存个人资料失败: ${error.message}`)
  }
}

const changePassword = () => {
  if (!passwordForm.value.currentPassword) {
    ElMessage.error('请输入当前密码')
    return
  }
  if (!passwordForm.value.newPassword) {
    ElMessage.error('请输入新密码')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  if (passwordStrength.value < 50) {
    ElMessage.error('密码强度太弱，请设置更复杂的密码')
    return
  }
  
  // 修改密码逻辑
  ElMessage.success('密码修改成功')
  passwordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
}

const toggleTwoFactor = (enabled) => {
  if (enabled) {
    ElMessage.success('两步验证已启用')
  } else {
    ElMessageBox.confirm('确定要关闭两步验证吗？这会降低您账户的安全性。', '确认操作', {
      type: 'warning'
    }).then(() => {
      ElMessage.success('两步验证已关闭')
    }).catch(() => {
      securitySettings.value.twoFactorEnabled = true
    })
  }
}

// 修改头像方法
const updateAvatar = async () => {
  try {
    // 检查是否有永久头像链接
    if (!permanentAvatarUrl.value) {
      ElMessage.error('请先上传头像')
      return
    }

    console.log('开始修改头像:', {
      avatarUrl: permanentAvatarUrl.value
    })
    
    // 调用修改头像API
    const result = await updateAvatarAPI(permanentAvatarUrl.value)
    
    console.log('修改头像成功:', result)
    ElMessage.success('头像修改成功')
    
    // 重新加载用户信息
    await loadUserProfile()
    
  } catch (error) {
    console.error('修改头像失败:', error)
    ElMessage.error(`修改头像失败: ${error.message}`)
  }
}

// 组件挂载时加载用户数据
onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
/* CSS变量定义 */
:root {
  --primary-color: #002FA7;
  --secondary-color: #517B4D;
  --success-color: #67C23A;
  --warning-color: #E6A23C;
  --danger-color: #F56C6C;
  --info-color: #909399;
  
  --bg-primary: #F0F0F3;
  --bg-secondary: #E8E8EB;
  --bg-glass: rgba(240, 240, 243, 0.25);
  
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-tertiary: #909399;
  
  --border-color: #DCDFE6;
  --border-radius: 12px;
  --border-radius-lg: 16px;
  --border-radius-xl: 24px;
  
  --shadow-neu-raised: 8px 8px 16px #d1d1d4, -8px -8px 16px #ffffff;
  --shadow-neu-inset: inset 8px 8px 16px #d1d1d4, inset -8px -8px 16px #ffffff;
  --shadow-neu-hover: 4px 4px 8px #d1d1d4, -4px -4px 8px #ffffff;
  --shadow-glass: 0 8px 32px rgba(0, 47, 167, 0.08);
  
  --transition-smooth: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-normal: all 0.3s ease;
}

.profile-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding: 1.5rem;
  position: relative;
}

.profile-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(0, 47, 167, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(81, 123, 77, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(103, 194, 58, 0.02) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.page-header {
  background: var(--bg-glass);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--border-radius-xl);
  padding: 2rem 2.5rem;
  margin-bottom: 2rem;
  box-shadow: var(--shadow-glass);
  position: relative;
  z-index: 1;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.1) 0%, 
    transparent 50%, 
    rgba(0, 47, 167, 0.05) 100%);
  pointer-events: none;
  border-radius: var(--border-radius-xl);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
  display: flex;
  align-items: center;
  gap: 1rem;
  line-height: 1.2;
}

.page-title i {
  color: var(--primary-color);
  font-size: 1.75rem;
  padding: 0.75rem;
  background: rgba(0, 47, 167, 0.1);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-neu-raised);
}

.page-subtitle {
  color: var(--text-secondary);
  margin: 0;
  font-size: 1.1rem;
  font-weight: 500;
}

.profile-content {
  display: flex;
  gap: 2rem;
  min-height: calc(100vh - 250px);
  position: relative;
  z-index: 1;
}



.profile-main {
  flex: 1;
  background: var(--bg-glass);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--border-radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-glass);
  position: relative;
}

.profile-main::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.1) 0%, 
    transparent 50%, 
    rgba(0, 47, 167, 0.05) 100%);
  pointer-events: none;
}

.profile-section {
  padding: 2.5rem;
  position: relative;
  z-index: 1;
  height: 100%;
  overflow-y: auto;
}

.section-header {
  margin-bottom: 2.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
}

.section-header::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 60px;
  height: 3px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 2px;
}

.section-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
  display: flex;
  align-items: center;
  gap: 1rem;
  line-height: 1.3;
}

.section-title i {
  color: var(--primary-color);
  font-size: 1.5rem;
  padding: 0.75rem;
  background: rgba(0, 47, 167, 0.1);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-neu-raised);
}

.section-subtitle {
  color: var(--text-secondary);
  margin: 0;
  font-size: 1rem;
  font-weight: 500;
  line-height: 1.5;
}

.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 2.5rem;
  position: relative;
}

.avatar-upload {
  text-align: center;
  position: relative;
}

.avatar-preview {
  position: relative;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  margin: 0 auto 1.5rem;
  overflow: hidden;
  cursor: pointer;
  background: var(--bg-primary);
  box-shadow: var(--shadow-neu-raised);
  transition: var(--transition-smooth);
  border: 4px solid rgba(255, 255, 255, 0.2);
}

.avatar-preview:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 
    12px 12px 24px rgba(0, 47, 167, 0.15),
    -6px -6px 12px rgba(255, 255, 255, 0.8);
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  font-size: 3rem;
  font-weight: 300;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 47, 167, 0.8);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
  opacity: 0;
  transition: var(--transition-normal);
  border-radius: 50%;
}

.avatar-preview:hover .avatar-overlay {
  opacity: 1;
}

.avatar-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.avatar-actions .el-button {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius);
  color: var(--text-primary);
  font-weight: 500;
  padding: 0.75rem 1.5rem;
  transition: var(--transition-normal);
  box-shadow: var(--shadow-neu-raised);
}

.avatar-actions .el-button:hover {
  background: rgba(0, 47, 167, 0.1);
  color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: var(--shadow-neu-hover);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  position: relative;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.95rem;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.form-label::before {
  content: '';
  width: 4px;
  height: 16px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 2px;
}

.form-hint {
  font-size: 0.8rem;
  color: var(--text-tertiary);
  font-style: italic;
  margin-top: 0.25rem;
}

.gender-group {
  display: flex;
  gap: 2rem;
  margin-top: 0.5rem;
}

.neu-radio {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: var(--transition-normal);
  box-shadow: var(--shadow-neu-raised);
  font-weight: 500;
}

.neu-radio:hover {
  background: rgba(0, 47, 167, 0.08);
  color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: var(--shadow-neu-hover);
}

.neu-radio.checked {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.15) 0%, rgba(81, 123, 77, 0.1) 100%);
  color: var(--primary-color);
  border-color: var(--primary-color);
  box-shadow: var(--shadow-neu-inset);
}

/* Element Plus 组件样式覆盖 */
.form-group :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-neu-inset);
  transition: var(--transition-normal);
  padding: 0.75rem 1rem;
}

.form-group :deep(.el-input__wrapper:hover) {
  border-color: rgba(0, 47, 167, 0.3);
  background: rgba(255, 255, 255, 0.15);
}

.form-group :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  background: rgba(255, 255, 255, 0.2);
  box-shadow: 
    var(--shadow-neu-inset),
    0 0 0 3px rgba(0, 47, 167, 0.1);
}

.form-group :deep(.el-input__inner) {
  color: var(--text-primary);
  font-weight: 500;
  background: transparent;
}

.form-group :deep(.el-input__inner::placeholder) {
  color: var(--text-tertiary);
  font-weight: 400;
}

.form-group :deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-neu-inset);
  color: var(--text-primary);
  font-weight: 500;
  padding: 1rem;
  transition: var(--transition-normal);
  resize: vertical;
  min-height: 120px;
}

.form-group :deep(.el-textarea__inner:hover) {
  border-color: rgba(0, 47, 167, 0.3);
  background: rgba(255, 255, 255, 0.15);
}

.form-group :deep(.el-textarea__inner:focus) {
  border-color: var(--primary-color);
  background: rgba(255, 255, 255, 0.2);
  box-shadow: 
    var(--shadow-neu-inset),
    0 0 0 3px rgba(0, 47, 167, 0.1);
}

.form-group :deep(.el-date-editor) {
  width: 100%;
}

.form-group :deep(.el-date-editor .el-input__wrapper) {
  width: 100%;
}

.preferences-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.preference-group {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.group-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.interest-tags {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}

.interest-tag {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1.25rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 2rem;
  cursor: pointer;
  transition: var(--transition-normal);
  font-weight: 500;
  color: var(--text-primary);
  box-shadow: var(--shadow-neu-raised);
  position: relative;
  overflow: hidden;
  justify-content: center;
}

.interest-tag::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.interest-tag:hover::before {
  left: 100%;
}

.interest-tag:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: var(--shadow-neu-hover);
}

.interest-tag.selected {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  border-color: var(--primary-color);
  box-shadow: var(--shadow-neu-inset);
  transform: scale(1.05);
}

.goal-group {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.goal-option {
  padding: 0;
  margin: 0;
  border: none;
  background: transparent;
}

.goal-content {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  width: 100%;
}

.goal-content i {
  font-size: 24px;
  color: var(--primary-color);
}

.goal-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 5px;
}

.goal-desc {
  font-size: 14px;
  color: var(--text-secondary);
}

.study-time-slider {
  margin: 2rem 0;
}

.time-display {
  text-align: center;
  margin: 2rem 0;
  padding: 1.5rem;
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.1) 0%, rgba(81, 123, 77, 0.05) 100%);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-neu-inset);
}

.time-display span {
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.notification-settings,
.privacy-settings,
.data-settings {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: var(--transition-normal);
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-item:hover {
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--border-radius);
  padding-left: 1rem;
  padding-right: 1rem;
}

.setting-info {
  flex: 1;
}

.setting-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.setting-desc {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.setting-action {
  margin-left: 1.25rem;
}

/* Element Plus 滑块样式覆盖 */
.preference-group :deep(.el-slider__runway) {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 1rem;
  height: 8px;
}

.preference-group :deep(.el-slider__bar) {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 1rem;
}

.preference-group :deep(.el-slider__button) {
  background: white;
  border: 3px solid var(--primary-color);
  box-shadow: var(--shadow-neu-raised);
  width: 20px;
  height: 20px;
}

.preference-group :deep(.el-slider__button:hover) {
  transform: scale(1.2);
  box-shadow: var(--shadow-neu-hover);
}

/* Element Plus 开关样式覆盖 */
.notification-settings :deep(.el-switch),
.privacy-settings :deep(.el-switch),
.data-settings :deep(.el-switch) {
  --el-switch-on-color: var(--primary-color);
  --el-switch-off-color: rgba(255, 255, 255, 0.2);
  --el-switch-border-color: rgba(255, 255, 255, 0.3);
}

.notification-settings :deep(.el-switch__core),
.privacy-settings :deep(.el-switch__core),
.data-settings :deep(.el-switch__core) {
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: var(--shadow-neu-inset);
}

.notification-settings :deep(.el-switch__action),
.privacy-settings :deep(.el-switch__action),
.data-settings :deep(.el-switch__action) {
  background: white;
  box-shadow: var(--shadow-neu-raised);
}

.privacy-select {
  width: 120px;
}

.security-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.security-group {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.password-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 400px;
}

.password-strength {
  margin-top: 1rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-neu-inset);
}

.strength-bar {
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 1rem;
  overflow: hidden;
  margin-bottom: 0.75rem;
  position: relative;
  box-shadow: var(--shadow-neu-inset);
}

.strength-fill {
  height: 100%;
  transition: var(--transition-normal);
  border-radius: 1rem;
  position: relative;
  overflow: hidden;
}

.strength-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { left: -100%; }
  100% { left: 100%; }
}

.strength-text {
  font-size: 0.85rem;
  font-weight: 600;
  text-align: center;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-secondary);
}

.two-factor-setting {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.two-factor-methods {
  margin-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.method-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 15px;
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
}

.method-item i {
  color: var(--primary-color);
  width: 20px;
}

.login-history {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-height: 400px;
  overflow-y: auto;
  padding-right: 0.5rem;
}

.login-history::-webkit-scrollbar {
  width: 6px;
}

.login-history::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.login-history::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 3px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-neu-raised);
  transition: var(--transition-normal);
}

.history-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-neu-hover);
  background: rgba(255, 255, 255, 0.12);
}

.history-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.history-device,
.history-location,
.history-time {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.history-device {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 1rem;
}

.history-status {
  margin-left: 1.25rem;
}

.privacy-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.privacy-group {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .profile-content {
    flex-direction: column;
    height: auto;
  }
}

@media (max-width: 768px) {
  .profile-page {
    padding: 10px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .goal-group {
    gap: 10px;
  }
  
  .goal-content {
    padding: 15px;
  }
  
  .interest-tags {
    gap: 8px;
  }
  
  .interest-tag {
    padding: 6px 12px;
    font-size: 14px;
  }
  
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .setting-action {
    margin-left: 0;
    align-self: flex-end;
  }
  
  .history-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .history-status {
    margin-left: 0;
    align-self: flex-end;
  }
}

/* 按钮样式 */
.neu-button {
  padding: 1rem 2rem;
  border: none;
  border-radius: var(--border-radius);
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  transition: var(--transition-normal);
  box-shadow: var(--shadow-neu-raised);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  min-width: 120px;
}

.neu-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.neu-button:hover::before {
  left: 100%;
}

/* Element Plus 按钮样式覆盖 */
:deep(.el-button.neu-button) {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  border-color: var(--primary-color);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: var(--shadow-neu-raised);
  backdrop-filter: blur(10px);
  transition: var(--transition-normal);
}

:deep(.el-button.neu-button:hover) {
  transform: translateY(-3px);
  box-shadow: var(--shadow-neu-hover);
  background: linear-gradient(135deg, #0056d6 0%, #6b8e23 100%);
}

:deep(.el-button.neu-button:active) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-neu-inset);
}

:deep(.el-button.neu-button.is-disabled) {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: var(--shadow-neu-inset) !important;
}

/* 头像按钮特定样式 */
:deep(.el-button.upload-btn) {
  background: linear-gradient(135deg, #409EFF 0%, #79bbff 100%);
  color: white;
  border-color: #409EFF;
}

:deep(.el-button.upload-btn:hover) {
  background: linear-gradient(135deg, #337ecc 0%, #66b1ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.3);
}

:deep(.el-button.save-btn) {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
  color: white;
  border-color: #67C23A;
}

:deep(.el-button.save-btn:hover) {
  background: linear-gradient(135deg, #529b2e 0%, #73c83f 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(103, 194, 58, 0.3);
}

:deep(.el-button.delete-btn) {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
  color: white;
  border-color: #F56C6C;
}

:deep(.el-button.delete-btn:hover) {
  background: linear-gradient(135deg, #dd6161 0%, #f56c6c 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(245, 108, 108, 0.3);
}

/* 头像按钮容器优化 */
.avatar-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
  margin-top: 1rem;
}

.avatar-actions .el-button {
  flex: 1;
  max-width: 120px;
  font-size: 0.85rem;
  padding: 0.6rem 1rem;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.avatar-actions .el-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

/* 状态样式 */
.status-success {
  padding: 0.5rem 1rem;
  border-radius: 1.5rem;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: var(--shadow-neu-raised);
  backdrop-filter: blur(10px);
  background: linear-gradient(135deg, rgba(81, 123, 77, 0.2) 0%, rgba(81, 123, 77, 0.1) 100%);
  color: var(--success-color);
  border: 1px solid rgba(81, 123, 77, 0.3);
}

.status-failed {
  padding: 0.5rem 1rem;
  border-radius: 1.5rem;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: var(--shadow-neu-raised);
  backdrop-filter: blur(10px);
  background: linear-gradient(135deg, rgba(220, 38, 38, 0.2) 0%, rgba(220, 38, 38, 0.1) 100%);
  color: var(--danger-color);
  border: 1px solid rgba(220, 38, 38, 0.3);
}

.status-warning {
  padding: 0.5rem 1rem;
  border-radius: 1.5rem;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: var(--shadow-neu-raised);
  backdrop-filter: blur(10px);
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.2) 0%, rgba(245, 158, 11, 0.1) 100%);
  color: var(--warning-color);
  border: 1px solid rgba(245, 158, 11, 0.3);
}
</style>