<template>
  <div class="register-page">
    <div class="register-container">
      <!-- 背景装饰 -->
      <div class="background-decoration">
        <div class="decoration-circle circle-1"></div>
        <div class="decoration-circle circle-2"></div>
        <div class="decoration-circle circle-3"></div>
      </div>
      
      <!-- 注册表单 -->
      <div class="register-form-container neumorphism-glass">
        <div class="form-header">
          <div class="logo-section">
            <img src="/DaoX_C7-Center_Logo.svg" alt="DaoX Logo" style="width: 48px; height: 48px; object-fit: contain; margin-right: 12px;" />
            <h1 class="logo-text">DaoX- EduPlatform</h1>
          </div>
          <h2 class="form-title">创建新账户</h2>
          <p class="form-subtitle">加入我们，开启您的学习之旅</p>
        </div>
        
        <el-form 
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
          @submit.prevent="handleRegister"
        >
          <!-- 用户类型选择 -->
<!--          <el-form-item prop="userType">
            <div class="user-type-selector">
              <div class="selector-label">选择账户类型</div>
              <el-radio-group v-model="registerForm.userType" class="type-radio-group">
                <el-radio-button label="student" class="type-radio">
                  <font-awesome-icon :icon="['fas', 'user-graduate']" />
                  <span>学生</span>
                </el-radio-button>
                <el-radio-button label="teacher" class="type-radio">
                  <font-awesome-icon :icon="['fas', 'chalkboard-teacher']" />
                  <span>教师</span>
                </el-radio-button>
              </el-radio-group>
            </div>
          </el-form-item>-->
          
          <el-form-item prop="username">
            <div class="input-wrapper">
              <font-awesome-icon :icon="['fas', 'user']" class="input-icon" />
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                size="large"
                class="custom-input"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="email">
            <div class="input-wrapper">
              <font-awesome-icon :icon="['fas', 'envelope']" class="input-icon" />
              <el-input
                v-model="registerForm.email"
                type="email"
                placeholder="请输入邮箱地址"
                size="large"
                class="custom-input"
                @input="validateEmailField"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="studentId">
            <div class="input-wrapper">
              <font-awesome-icon :icon="['fas', 'id-card']" class="input-icon" />
              <el-input
                v-model="registerForm.studentId"
                placeholder="请输入学号"
                size="large"
                class="custom-input"
              />
            </div>
          </el-form-item>
<!--
          <el-form-item prop="phone">
            <div class="input-wrapper">
              <font-awesome-icon :icon="['fas', 'phone']" class="input-icon" />
              <el-input
                v-model="registerForm.phone"
                placeholder="请输入手机号码"
                size="large"
                class="custom-input"
              />
            </div>
          </el-form-item>-->
          
          <el-form-item prop="password">
            <div class="input-wrapper">
              <font-awesome-icon :icon="['fas', 'lock']" class="input-icon" />
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                class="custom-input"
                show-password
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <div class="input-wrapper">
              <font-awesome-icon :icon="['fas', 'lock']" class="input-icon" />
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请确认密码"
                size="large"
                class="custom-input"
                show-password
              />
            </div>
          </el-form-item>
          
          <!-- 验证码 -->
          <el-form-item prop="verificationCode">
            <div class="verification-wrapper">
              <div class="input-wrapper flex-1">
                <font-awesome-icon :icon="['fas', 'shield-alt']" class="input-icon" />
                <el-input
                  v-model="registerForm.verificationCode"
                  placeholder="请输入验证码"
                  size="large"
                  class="custom-input"
                />
              </div>
              <el-button 
                class="verification-btn neumorphism-button"
                :disabled="verificationDisabled"
                @click="sendVerificationCode"
              >
                {{ verificationText }}
              </el-button>
            </div>
          </el-form-item>
          
          <!-- 协议同意 -->
          <el-form-item prop="agreement">
            <el-checkbox v-model="registerForm.agreement" class="agreement-checkbox">
              我已阅读并同意
              <el-link type="primary" class="agreement-link">《用户协议》</el-link>
              和
              <el-link type="primary" class="agreement-link">《隐私政策》</el-link>
            </el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              class="register-button neumorphism-button"
              :loading="loading"
              @click="handleRegister"
            >
              <font-awesome-icon :icon="['fas', 'user-plus']" class="mr-sm" />
              立即注册
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- 登录链接 -->
        <div class="login-link">
          <span class="text-muted">已有账户？</span>
          <router-link to="/login" class="link-primary">
            立即登录
          </router-link>
        </div>
      </div>
      
      <!-- 侧边信息面板 -->
      <div class="info-panel glass">
        <div class="panel-content">
          <h3 class="panel-title">为什么选择我们？</h3>
          <p class="panel-description">
            我们致力于为每一位学习者提供最优质的在线教育体验，
            帮助您实现学习目标，提升职业技能。
          </p>
          
          <div class="benefits-list">
            <div class="benefit-item">
              <div class="benefit-icon">
                <font-awesome-icon :icon="['fas', 'book-open']" class="text-primary" />
              </div>
              <div class="benefit-content">
                <h4>丰富课程资源</h4>
                <p>涵盖多个领域的专业课程</p>
              </div>
            </div>
            
            <div class="benefit-item">
              <div class="benefit-icon">
                <font-awesome-icon :icon="['fas', 'users']" class="text-secondary" />
              </div>
              <div class="benefit-content">
                <h4>专业师资团队</h4>
                <p>经验丰富的行业专家授课</p>
              </div>
            </div>
            
            <div class="benefit-item">
              <div class="benefit-icon">
                <font-awesome-icon :icon="['fas', 'certificate']" class="text-primary" />
              </div>
              <div class="benefit-content">
                <h4>权威认证证书</h4>
                <p>完成课程获得行业认可证书</p>
              </div>
            </div>
            
            <div class="benefit-item">
              <div class="benefit-icon">
                <i class="fas fa-mobile-alt text-secondary"></i>
              </div>
              <div class="benefit-content">
                <h4>随时随地学习</h4>
                <p>支持多设备，灵活安排学习时间</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { API_get, API_post } from '@/api'

const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)
const verificationCountdown = ref(0)
const coldTime = ref(0)
const isEmailValid = ref(false)

const registerForm = reactive({
  userType: 'student',
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  verificationCode: '',
  code: '',
  idNumber: '',
  studentId: '',
  agreement: false
})



const registerRules = {
  userType: [
    { required: true, message: '请选择账户类型', trigger: 'change' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],

  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  idNumber: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { 
      pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, 
      message: '请输入正确的身份证号码', 
      trigger: 'blur' 
    }
  ],
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 6, max: 20, message: '学号长度应为6-20位', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 4, max: 6, message: '验证码长度应为4-6位', trigger: 'blur' }
  ],
  agreement: [
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请阅读并同意用户协议和隐私政策'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ]
}

const onValidate = (prop, isValid) => {
  if (prop === 'email')
    isEmailValid.value = isValid
}

// Validate email field manually
const validateEmailField = () => {
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  isEmailValid.value = emailRegex.test(registerForm.email)
}

// Watch for email changes
watch(() => registerForm.email, () => {
  validateEmailField()
})

const verificationDisabled = computed(() => {
  return coldTime.value > 0 || !isEmailValid.value
})

const verificationText = computed(() => {
  return coldTime.value > 0 ? `${coldTime.value}s后重发` : '发送验证码'
})

// 获取验证码
const getVerificationCode = async () => {
  coldTime.value = 60
  API_get(`/public/auth/ask-code?email=${registerForm.email}&type=register`, () => {
    ElMessage.success(`验证码已发送到邮箱: ${registerForm.email}，请注意查收`)
    const handle = setInterval(() => {
      coldTime.value--
      if(coldTime.value === 0) {
        clearInterval(handle)
      }
    }, 1000)
  }, undefined, (message) => {
    ElMessage.warning(message)
    coldTime.value = 0
  })
}

const sendVerificationCode = getVerificationCode

// 处理注册
const register = async () => {
  if (registerFormRef.value) {
    registerFormRef.value.validate(async (valid) => {
      if (valid) {
        // 检查用户协议是否勾选
        if (!registerForm.agreement) {
          ElMessage.warning('请同意用户协议和隐私政策')
          return
        }
        try {
          loading.value = true
          API_post('/public/auth/register', {
            username: registerForm.username,
            password: registerForm.password,
            email: registerForm.email,
            code: registerForm.verificationCode,
            //idCard: registerForm.idNumber,
            identifier: registerForm.studentId
          }, () => {
            ElMessage.success('注册成功，欢迎加入我们')
            router.push("/login")
          })
        } catch (error) {
          ElMessage.error(error.message || '注册失败')
        } finally {
          loading.value = false
        }
      } else {
        ElMessage.warning('请填写完整且正确的注册信息')
      }
    })
  } else {
    ElMessage.warning('表单验证未初始化')
  }
}

const handleRegister = register

// 返回登录页
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--background-color) 0%, #E8F4FD 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-lg);
  position: relative;
  overflow: hidden;
}

.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--secondary-color), var(--primary-color));
  opacity: 0.1;
  animation: float 8s ease-in-out infinite;
}

.circle-1 {
  width: 180px;
  height: 180px;
  top: 15%;
  right: 10%;
  animation-delay: 0s;
}

.circle-2 {
  width: 120px;
  height: 120px;
  bottom: 30%;
  left: 15%;
  animation-delay: 3s;
}

.circle-3 {
  width: 80px;
  height: 80px;
  top: 50%;
  left: 5%;
  animation-delay: 6s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-15px) rotate(180deg);
  }
}

.register-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  max-width: 1200px;
  width: 100%;
  gap: var(--spacing-xl);
  position: relative;
  z-index: 1;
}

.register-form-container {
  padding: var(--spacing-xl);
  border-radius: var(--border-radius-xl);
  max-height: 90vh;
  overflow-y: auto;
}

.form-header {
  text-align: center;
  margin-bottom: var(--spacing-lg);
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--primary-color);
  margin: 0;
}

.form-title {
  font-size: 1.75rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.form-subtitle {
  color: var(--text-secondary);
  margin: 0;
}

.register-form {
  margin-bottom: var(--spacing-md);
}

.user-type-selector {
  margin-bottom: var(--spacing-sm);
}

.selector-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: var(--spacing-sm);
}

.type-radio-group {
  display: flex;
  gap: var(--spacing-sm);
  width: 100%;
}

.type-radio {
  flex: 1;
}

.type-radio :deep(.el-radio-button__inner) {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-md);
  border-radius: var(--border-radius-md);
  width: 100%;
  height: auto;
}

.type-radio i {
  font-size: 1.25rem;
}

.input-wrapper {
  position: relative;
  flex: 1;
}

.input-icon {
  position: absolute;
  left: var(--spacing-md);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
  z-index: 1;
}

.custom-input :deep(.el-input__wrapper) {
  padding-left: 40px;
  border-radius: var(--border-radius-md);
  box-shadow: var(--neumorphism-inset-light), var(--neumorphism-inset-dark);
  border: none;
}

.verification-wrapper {
  display: flex;
  gap: var(--spacing-sm);
  align-items: flex-start;
}

.verification-btn {
  height: 40px;
  padding: 0 var(--spacing-md);
  white-space: nowrap;
  border-radius: var(--border-radius-md);
}

.agreement-checkbox {
  color: var(--text-secondary);
  line-height: 1.5;
}

.agreement-link {
  font-size: inherit;
}

.register-button {
  width: 100%;
  height: 48px;
  font-size: 1rem;
  font-weight: 500;
  border-radius: var(--border-radius-md);
}

.login-link {
  text-align: center;
  font-size: 0.875rem;
}

.link-primary {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  margin-left: var(--spacing-xs);
}

.link-primary:hover {
  text-decoration: underline;
}

.info-panel {
  padding: var(--spacing-xl);
  border-radius: var(--border-radius-xl);
  display: flex;
  align-items: center;
}

.panel-content {
  width: 100%;
}

.panel-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.panel-description {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: var(--spacing-xl);
}

.benefits-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.benefit-item {
  display: flex;
  gap: var(--spacing-md);
  align-items: flex-start;
}

.benefit-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--border-radius-md);
  background: var(--surface-color);
  box-shadow: var(--neumorphism-light), var(--neumorphism-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.benefit-icon i {
  font-size: 1.25rem;
}

.benefit-content h4 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.benefit-content p {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.4;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-container {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg);
  }
  
  .info-panel {
    order: -1;
    padding: var(--spacing-lg);
  }
  
  .register-form-container {
    padding: var(--spacing-lg);
    max-height: none;
  }
  
  .type-radio-group {
    flex-direction: column;
  }
  
  .verification-wrapper {
    flex-direction: column;
  }
  
  .verification-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .register-page {
    padding: var(--spacing-md);
  }
  
  .form-title {
    font-size: 1.5rem;
  }
  
  .panel-title {
    font-size: 1.25rem;
  }
  
  .benefits-list {
    gap: var(--spacing-md);
  }
}
</style>