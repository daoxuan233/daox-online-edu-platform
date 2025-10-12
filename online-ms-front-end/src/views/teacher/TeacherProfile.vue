<template>
  <div class="teacher-profile">
    <!-- 页面标题 -->
    <div class="page-header glass-card">
      <div class="header-content">
        <div class="header-icon">
          <font-awesome-icon :icon="['fas', 'user-circle']"/>
        </div>
        <div class="header-text">
          <h1 class="page-title">个人资料</h1>
          <p class="page-subtitle">管理您的个人信息和教学资料</p>
        </div>
      </div>
    </div>

    <div class="profile-content">
      <!-- 左侧：头像和基本信息 -->
      <div class="profile-sidebar">
        <!-- 头像卡片 -->
        <div class="avatar-card neu-card">
          <div class="avatar-section">
            <div class="avatar-container">
              <img v-if="profile.avatar" :src="profile.avatar" :alt="profile.name" class="avatar-image"/>
              <div v-else class="avatar-placeholder">
                <font-awesome-icon :icon="['fas', 'user']"/>
              </div>
              <div class="avatar-overlay" @click="uploadAvatar">
                <font-awesome-icon :icon="['fas', 'camera']"/>
                <span>更换头像</span>
              </div>
            </div>
            <div class="avatar-info">
              <h3 class="teacher-name">{{ profile.name }}</h3>
              <p class="teacher-title">{{ profile.title || '讲师' }}</p>
              <div class="teacher-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ profile.courseCount || 0 }}</span>
                  <span class="stat-label">授课数量</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ profile.studentCount || 0 }}</span>
                  <span class="stat-label">学生数量</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ profile.rating || '5.0' }}</span>
                  <span class="stat-label">教学评分</span>
                </div>
              </div>
            </div>
            <div class="avatar-actions">
              <!--              <el-button size="small" class="neu-button upload-btn" @click="uploadAvatar">
                              <font-awesome-icon :icon="['fas', 'upload']" />
                              上传头像
                            </el-button>-->
              <el-button size="small" class="neu-button save-btn" type="success" @click="updateAvatarTeacher">
                <font-awesome-icon :icon="['fas', 'save']"/>
                保存头像
              </el-button>
              <el-button size="small" class="neu-button delete-btn" type="danger" @click="removeAvatar">
                <font-awesome-icon :icon="['fas', 'trash']"/>
                删除
              </el-button>
            </div>
          </div>
        </div>

        <!-- 快速操作 -->
        <div class="quick-actions-card neu-card">
          <h4 class="card-title">
            <font-awesome-icon :icon="['fas', 'bolt']"/>
            快速操作
          </h4>
          <div class="action-buttons">
            <button class="action-btn" @click="changePassword">
              <font-awesome-icon :icon="['fas', 'key']"/>
              <span>修改密码</span>
            </button>
            <button class="action-btn" @click="exportData">
              <font-awesome-icon :icon="['fas', 'download']"/>
              <span>导出数据</span>
            </button>
            <button class="action-btn" @click="viewAnalytics">
              <font-awesome-icon :icon="['fas', 'chart-line']"/>
              <span>教学统计</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 右侧：详细信息表单 -->
      <div class="profile-main">
        <!-- 基本信息 -->
        <div class="info-card neu-card">
          <div class="card-header">
            <h3 class="card-title">
              <font-awesome-icon :icon="['fas', 'info-circle']"/>
              基本信息
            </h3>
            <el-button type="primary" class="neu-button" @click="editMode = !editMode">
              <font-awesome-icon :icon="editMode ? ['fas', 'times'] : ['fas', 'edit']"/>
              {{ editMode ? '取消编辑' : '编辑信息' }}
            </el-button>
          </div>

          <el-form :model="profile" :rules="rules" ref="profileForm" label-width="120px" class="profile-form">
            <div class="form-grid">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="profile.name" :disabled="!editMode" class="neu-input"/>
              </el-form-item>

              <el-form-item label="工号" prop="employeeId">
                <el-input v-model="profile.employeeId" disabled class="neu-input"/>
              </el-form-item>

              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profile.email" :disabled="!editMode" class="neu-input"/>
              </el-form-item>

              <el-form-item label="手机号" prop="phone">
                <el-input v-model="profile.phone" :disabled="!editMode" class="neu-input"/>
              </el-form-item>

              <el-form-item label="职称" prop="title">
                <el-select v-model="profile.title" :disabled="!editMode" class="neu-select" placeholder="请选择职称">
                  <el-option label="助教" value="助教"/>
                  <el-option label="讲师" value="讲师"/>
                  <el-option label="副教授" value="副教授"/>
                  <el-option label="教授" value="教授"/>
                </el-select>
              </el-form-item>

              <el-form-item label="所属学院" prop="department">
                <el-input v-model="profile.department" :disabled="!editMode" class="neu-input"/>
              </el-form-item>
            </div>

            <el-form-item label="个人简介" prop="bio">
              <el-input
                  v-model="profile.bio"
                  :disabled="!editMode"
                  type="textarea"
                  :rows="4"
                  class="neu-textarea"
                  placeholder="请输入个人简介..."
              />
            </el-form-item>

            <el-form-item label="专业领域" prop="specialties">
              <el-tag
                  v-for="(tag, index) in profile.specialties"
                  :key="index"
                  :closable="editMode"
                  @close="removeSpecialty(index)"
                  class="specialty-tag"
              >
                {{ tag }}
              </el-tag>
              <el-input
                  v-if="editMode && inputVisible"
                  ref="inputRef"
                  v-model="inputValue"
                  class="tag-input"
                  size="small"
                  @keyup.enter="handleInputConfirm"
                  @blur="handleInputConfirm"
              />
              <el-button
                  v-else-if="editMode"
                  class="new-tag-btn"
                  size="small"
                  @click="showInput"
              >
                + 添加专业领域
              </el-button>
            </el-form-item>

            <div v-if="editMode" class="form-actions">
              <el-button type="primary" class="neu-button" @click="saveProfile" :loading="saving">
                <font-awesome-icon :icon="['fas', 'save']"/>
                保存修改
              </el-button>
              <el-button class="neu-button" @click="resetForm">
                <font-awesome-icon :icon="['fas', 'undo']"/>
                重置
              </el-button>
            </div>
          </el-form>
        </div>

        <!-- 教学成就 -->
        <div class="achievements-card neu-card">
          <div class="card-header">
            <h3 class="card-title">
              <font-awesome-icon :icon="['fas', 'trophy']"/>
              教学成就
            </h3>
          </div>

          <div class="achievements-grid">
            <div v-for="achievement in achievements" :key="achievement.id" class="achievement-item">
              <div class="achievement-icon" :style="{ backgroundColor: achievement.color }">
                <font-awesome-icon :icon="achievement.icon"/>
              </div>
              <div class="achievement-content">
                <h4 class="achievement-title">{{ achievement.title }}</h4>
                <p class="achievement-desc">{{ achievement.description }}</p>
                <span class="achievement-date">{{ achievement.date }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px" class="neu-dialog">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password class="neu-input"/>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password class="neu-input"/>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password class="neu-input"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false" class="neu-button">取消</el-button>
        <el-button type="primary" @click="submitPasswordChange" class="neu-button">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, nextTick, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {useRouter} from 'vue-router'
import {uploadAvatar as uploadAvatarAPI} from "@/api/publicApi.js";
import {getTeacherProfile, updateAvatarTeacher as updateAvatarAPI} from '@/api/teacher/teacherAPI.js'
import {getIdentifier, getUserId} from '@/utils/tokenAnalysis.js'



const router = useRouter()

// 响应式数据
const editMode = ref(false)
const saving = ref(false)
const inputVisible = ref(false)
const inputValue = ref('')
const inputRef = ref()
const passwordDialogVisible = ref(false)
// 确保定义了 permanentAvatarUrl
const permanentAvatarUrl = ref('')

// 个人资料数据
const profile = reactive({
  name: '张教授',
  employeeId: 'T001',
  email: 'zhang.prof@university.edu.cn',
  phone: '138****8888',
  title: '教授',
  department: '计算机科学与技术学院',
  bio: '专注于人工智能、机器学习和数据挖掘领域的研究与教学，拥有15年的教学经验，发表学术论文50余篇。',
  specialties: ['人工智能', '机器学习', '数据挖掘', '深度学习'],
  avatar: '',
  courseCount: 12,
  studentCount: 856,
  rating: 4.8
})

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
            Object.assign(profile, {
              id: data.id,
              name: data.nickname || profile.name,
              employeeId: data.identifier || profile.employeeId,
              email: data.email || profile.email,
              phone: data.phone || profile.phone,
              gender: data.gender || profile.gender,
              birthday: data.birthday || profile.birthday,
              bio: data.biography || profile.bio,
              avatar: data.avatarUrl || profile.avatar,
              role: data.role || profile.role
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

// 密码修改表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 教学成就数据
const achievements = ref([
  {
    id: 1,
    title: '优秀教师奖',
    description: '连续三年获得学院优秀教师称号',
    date: '2023年12月',
    icon: ['fas', 'award'],
    color: '#FFD700'
  },
  {
    id: 2,
    title: '教学创新奖',
    description: '在线教学模式创新获得校级奖励',
    date: '2023年6月',
    icon: ['fas', 'lightbulb'],
    color: '#FF6B6B'
  },
  {
    id: 3,
    title: '学术贡献奖',
    description: '发表高质量学术论文获得认可',
    date: '2023年3月',
    icon: ['fas', 'graduation-cap'],
    color: '#4ECDC4'
  }
])

// 表单验证规则
const rules = {
  name: [{required: true, message: '请输入姓名', trigger: 'blur'}],
  email: [
    {required: true, message: '请输入邮箱', trigger: 'blur'},
    {type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur'}
  ],
  phone: [
    {required: true, message: '请输入手机号', trigger: 'blur'},
    {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur'}
  ]
}

const passwordRules = {
  oldPassword: [{required: true, message: '请输入原密码', trigger: 'blur'}],
  newPassword: [
    {required: true, message: '请输入新密码', trigger: 'blur'},
    {min: 6, message: '密码长度不能少于6位', trigger: 'blur'}
  ],
  confirmPassword: [
    {required: true, message: '请确认新密码', trigger: 'blur'},
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

// 方法
const uploadAvatar = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.style.display = 'none'

  input.onchange = async (event) => {
    const file = event.target.files[0]
    if (!file) return

    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('头像文件大小不能超过5MB')
      return
    }

    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件')
      return
    }

    try {
      const userId = profile.employeeId // 使用教师工号作为用户ID
      if (!userId) {
        ElMessage.error('无法获取用户信息，请重新登录')
        return
      }

      ElMessage.info('正在上传头像...')

      const data = await uploadAvatarAPI(file)

      const permanentUrl = data.permanentUrl
      profile.avatar = permanentUrl

      permanentAvatarUrl.value = permanentUrl

      ElMessage.success('头像上传成功')
      console.log('头像上传成功，永久链接:', permanentUrl)
      console.log('完整响应数据:', data)
    } catch (error) {
      console.error('头像上传失败:', error)
      ElMessage.error('头像上传失败，请稍后重试')
    }
  }

  document.body.appendChild(input)
  input.click()
  document.body.removeChild(input)
}

// 删除头像方法
const removeAvatar = () => {
  profile.avatar = ''
  // 清空全局变量中的永久链接
  ElMessage.success('头像已删除')
}

// 修改头像方法
const updateAvatarTeacher = async () => {
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

const changePassword = () => {
  passwordDialogVisible.value = true
}

const exportData = () => {
  ElMessage.info('数据导出功能开发中...')
}

const viewAnalytics = () => {
  router.push('/teacher/analytics')
}

const saveProfile = () => {
  saving.value = true
  // 模拟保存操作
  setTimeout(() => {
    saving.value = false
    editMode.value = false
    ElMessage.success('个人信息保存成功！')
  }, 1000)
}

const resetForm = () => {
  // 重置表单逻辑
  ElMessage.info('表单已重置')
}

const removeSpecialty = (index) => {
  profile.specialties.splice(index, 1)
}

const showInput = () => {
  inputVisible.value = true
  nextTick(() => {
    inputRef.value?.focus()
  })
}

const handleInputConfirm = () => {
  if (inputValue.value && !profile.specialties.includes(inputValue.value)) {
    profile.specialties.push(inputValue.value)
  }
  inputVisible.value = false
  inputValue.value = ''
}

const submitPasswordChange = () => {
  // 实现密码修改逻辑
  passwordDialogVisible.value = false
  ElMessage.success('密码修改成功！')
}

onMounted(() => {
  // 组件挂载时的初始化逻辑
  loadUserProfile()
})
</script>

<style scoped>
/* 继承TeacherLayout的CSS变量 */
.teacher-profile {
  padding: var(--spacing-lg);
  background: var(--neumorphism-bg);
  min-height: 100vh;
}

/* 页面标题 */
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
.profile-content {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: var(--spacing-lg);
}

/* 新拟态卡片样式 */
.neu-card {
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  box-shadow: 8px 8px 16px var(--neumorphism-shadow-dark),
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

.neu-card:hover {
  transform: translateY(-2px);
  box-shadow: 12px 12px 24px var(--neumorphism-shadow-dark),
  -12px -12px 24px var(--neumorphism-shadow-light);
}

/* 头像卡片 */
.avatar-section {
  text-align: center;
  position: relative;
  z-index: 1;
}

/* 头像按钮容器优化 */
.avatar-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
  margin-top: 1rem;
}

.avatar-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.avatar-container {
  position: relative;
  display: inline-block;
  margin-bottom: var(--spacing-lg);
}

.avatar-image, .avatar-placeholder {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid var(--glass-border);
  box-shadow: inset 4px 4px 8px rgba(255, 255, 255, 0.2),
  inset -4px -4px 8px rgba(0, 0, 0, 0.1),
  0 8px 16px rgba(0, 0, 0, 0.1);
}

.avatar-placeholder {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 48px;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity var(--transition-normal);
  cursor: pointer;
  font-size: 14px;
}

.avatar-container:hover .avatar-overlay {
  opacity: 1;
}

.teacher-name {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.teacher-title {
  color: var(--text-secondary);
  font-size: 16px;
  margin: 0 0 var(--spacing-lg) 0;
}

.teacher-stats {
  display: flex;
  justify-content: space-around;
  gap: var(--spacing-md);
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: var(--spacing-xs);
}

.stat-label {
  font-size: 12px;
  color: var(--text-auxiliary);
}

/* 快速操作 */
.card-title {
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

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  position: relative;
  z-index: 1;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  background: var(--neumorphism-bg);
  border: none;
  border-radius: var(--border-radius-md);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: 4px 4px 8px var(--neumorphism-shadow-dark),
  -4px -4px 8px var(--neumorphism-shadow-light);
}

.action-btn:hover {
  color: var(--primary-color);
  transform: translateY(-1px);
  box-shadow: 6px 6px 12px var(--neumorphism-shadow-dark),
  -6px -6px 12px var(--neumorphism-shadow-light);
}

.action-btn:active {
  transform: translateY(0);
  box-shadow: inset 2px 2px 4px var(--neumorphism-shadow-dark),
  inset -2px -2px 4px var(--neumorphism-shadow-light);
}

/* 表单样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  position: relative;
  z-index: 1;
}

.profile-form {
  position: relative;
  z-index: 1;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

.neu-input :deep(.el-input__wrapper) {
  background: var(--neumorphism-bg);
  border: none;
  border-radius: var(--border-radius-md);
  box-shadow: inset 4px 4px 8px var(--neumorphism-shadow-dark),
  inset -4px -4px 8px var(--neumorphism-shadow-light);
  transition: all var(--transition-normal);
}

.neu-input :deep(.el-input__wrapper:hover) {
  box-shadow: inset 6px 6px 12px var(--neumorphism-shadow-dark),
  inset -6px -6px 12px var(--neumorphism-shadow-light);
}

.neu-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: inset 4px 4px 8px var(--neumorphism-shadow-dark),
  inset -4px -4px 8px var(--neumorphism-shadow-light),
  0 0 0 2px rgba(0, 47, 167, 0.2);
}

.neu-textarea :deep(.el-textarea__inner) {
  background: var(--neumorphism-bg);
  border: none;
  border-radius: var(--border-radius-md);
  box-shadow: inset 4px 4px 8px var(--neumorphism-shadow-dark),
  inset -4px -4px 8px var(--neumorphism-shadow-light);
  transition: all var(--transition-normal);
}

.neu-select :deep(.el-select__wrapper) {
  background: var(--neumorphism-bg);
  border: none;
  border-radius: var(--border-radius-md);
  box-shadow: inset 4px 4px 8px var(--neumorphism-shadow-dark),
  inset -4px -4px 8px var(--neumorphism-shadow-light);
}

.neu-button {
  background: var(--neumorphism-bg);
  border: none;
  border-radius: var(--border-radius-md);
  padding: var(--spacing-md) var(--spacing-lg);
  color: #ffffff;
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: 4px 4px 8px var(--neumorphism-shadow-dark),
  -4px -4px 8px var(--neumorphism-shadow-light);
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color))
}

.neu-button:hover {
  transform: translateY(-1px);
  box-shadow: 6px 6px 12px var(--neumorphism-shadow-dark),
  -6px -6px 12px var(--neumorphism-shadow-light);
  background: linear-gradient(135deg, var(--secondary-color), var(--primary-color))
}

.neu-button.el-button--primary {
  background: linear-gradient(135deg, var(--primary-color), #4A90E2);
  color: white;
}

.neu-button.el-button--primary:hover {
  background: linear-gradient(135deg, #0026A3, var(--primary-color));
}

/* 专业领域标签 */
.specialty-tag {
  margin-right: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  border: none;
  border-radius: var(--border-radius-sm);
}

.tag-input {
  width: 120px;
  margin-right: var(--spacing-sm);
}

.new-tag-btn {
  background: var(--neumorphism-bg);
  border: 1px dashed var(--text-auxiliary);
  color: var(--text-auxiliary);
  border-radius: var(--border-radius-sm);
}

.form-actions {
  display: flex;
  gap: var(--spacing-md);
  justify-content: flex-end;
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--glass-border);
}

/* 教学成就 */
.achievements-grid {
  display: grid;
  gap: var(--spacing-lg);
  position: relative;
  z-index: 1;
}

.achievement-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-md);
  box-shadow: 4px 4px 8px var(--neumorphism-shadow-dark),
  -4px -4px 8px var(--neumorphism-shadow-light);
  transition: all var(--transition-normal);
}

.achievement-item:hover {
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px var(--neumorphism-shadow-dark),
  -6px -6px 12px var(--neumorphism-shadow-light);
}

.achievement-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  flex-shrink: 0;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.achievement-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.achievement-desc {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0 0 var(--spacing-xs) 0;
  line-height: 1.5;
}

.achievement-date {
  color: var(--text-auxiliary);
  font-size: 12px;
}

/* 对话框样式 */
.neu-dialog :deep(.el-dialog) {
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-lg);
  box-shadow: 8px 8px 16px var(--neumorphism-shadow-dark),
  -8px -8px 16px var(--neumorphism-shadow-light);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .profile-content {
    grid-template-columns: 300px 1fr;
  }
}

@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .teacher-stats {
    flex-direction: column;
    gap: var(--spacing-sm);
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md);
  }
}

@media (max-width: 480px) {
  .teacher-profile {
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
}
</style>