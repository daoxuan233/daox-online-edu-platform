<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 背景装饰 -->
      <div class="background-decoration">
        <div class="decoration-circle circle-1"></div>
        <div class="decoration-circle circle-2"></div>
        <div class="decoration-circle circle-3"></div>
      </div>
      
      <!-- 登录表单 -->
      <div class="login-form-container neumorphism-glass">
        <div class="form-header">
          <div class="logo-section">
            <font-awesome-icon :icon="['fas', 'graduation-cap']" class="text-3xl text-primary" />
            <h1 class="logo-text">DaoX- EduPlatform</h1>
          </div>
          <h2 class="form-title">欢迎回来</h2>
          <p class="form-subtitle">登录您的账户继续学习之旅</p>
        </div>
        
        <el-form 
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <div class="input-wrapper">
              <font-awesome-icon :icon="['fas', 'user']" class="input-icon" />
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名/邮箱"
                size="large"
                class="custom-input"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="password">
            <div class="input-wrapper">
              <font-awesome-icon :icon="['fas', 'lock']" class="input-icon" />
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                class="custom-input"
                show-password
              />
            </div>
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox v-model="loginForm.remember" class="remember-checkbox">
              记住我
            </el-checkbox>
            <el-link type="primary" class="forgot-link">
              忘记密码？
            </el-link>
          </div>
          
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              class="login-button neumorphism-button"
              :loading="loading"
              @click="handleLogin"
            >
              <font-awesome-icon :icon="['fas', 'sign-in-alt']" class="mr-sm" />
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- 第三方登录 -->
<!--        <div class="social-login">
          <div class="divider">
            <span class="divider-text">或者使用以下方式登录</span>
          </div>
          
          <div class="social-buttons">
            <button class="social-btn neumorphism-button" @click="handleSocialLogin('wechat')">
              <font-awesome-icon :icon="['fab', 'weixin']" class="text-secondary" />
              <span>微信</span>
            </button>
            <button class="social-btn neumorphism-button" @click="handleSocialLogin('qq')">
              <font-awesome-icon :icon="['fab', 'qq']" class="text-primary" />
              <span>QQ</span>
            </button>
            <button class="social-btn neumorphism-button" @click="handleSocialLogin('github')">
              <font-awesome-icon :icon="['fab', 'github']" />
              <span>GitHub</span>
            </button>
          </div>
        </div>-->
        
        <!-- 注册链接 -->
        <div class="register-link">
          <span class="text-muted">还没有账户？</span>
          <router-link to="/register" class="link-primary">
            立即注册
          </router-link>
        </div>
      </div>
      
      <!-- 侧边信息面板 -->
      <div class="info-panel glass">
        <div class="panel-content">
          <h3 class="panel-title">开启学习新体验</h3>
          <p class="panel-description">
            加入我们的在线教育平台，享受高质量的课程内容，
            与优秀的讲师互动，提升您的专业技能。
          </p>
          
          <div class="features-list">
            <div class="feature-item">
              <i class="fas fa-check-circle text-secondary"></i>
              <span>海量优质课程资源</span>
            </div>
            <div class="feature-item">
              <i class="fas fa-check-circle text-secondary"></i>
              <span>专业讲师在线指导</span>
            </div>
            <div class="feature-item">
              <i class="fas fa-check-circle text-secondary"></i>
              <span>个性化学习路径</span>
            </div>
            <div class="feature-item">
              <i class="fas fa-check-circle text-secondary"></i>
              <span>学习进度实时跟踪</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {login} from '@/api/index.js'

const router = useRouter()
const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 5, max: 20, message: '密码长度在 5 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return
    
    loading.value = true
    
    // 登录请求
    loginFormRef.value.validate((valid)=>{
      if (valid) {
        login(loginForm.username, loginForm.password, loginForm.remember, (data) => {
          console.log(data)
          // 解析token获取role
          const token = data.token;
          const payload = JSON.parse(atob(token.split('.')[1]));
          const role = payload.role;
          switch (role){
            case 'student':
              router.push('/index')
              break;
            case 'teacher':
              router.push('/teacher')
              break;
            case 'admin':
              router.push('/admin')
              break;
            default:
              ElMessage.error('登录失败，请检查用户名和密码')
          }
        }, (message) => {
          ElMessage.error(message || '登录失败')
        })
      } else {
        ElMessage.warning('请填写完整的登录信息')
      }
    })
    
  } catch (error) {
    ElMessage.error('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
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
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  opacity: 0.1;
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.circle-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.circle-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.login-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  max-width: 1000px;
  width: 100%;
  gap: var(--spacing-xl);
  position: relative;
  z-index: 1;
}

.login-form-container {
  padding: var(--spacing-xl);
  border-radius: var(--border-radius-xl);
}

.form-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
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

.login-form {
  margin-bottom: var(--spacing-lg);
  width: 100%;
}

.login-form :deep(.el-form-item) {
  width: 100%;
  margin-bottom: var(--spacing-lg);
}

.login-form :deep(.el-form-item__content) {
  width: 100%;
}

.input-wrapper {
  position: relative;
  width: 100%;
}

.input-icon {
  position: absolute;
  left: var(--spacing-md);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
  z-index: 1;
}

.custom-input {
  width: 100%;
}

.custom-input :deep(.el-input__wrapper) {
  width: 100%;
  padding-left: 40px;
  border-radius: var(--border-radius-md);
  box-shadow: var(--neumorphism-inset-light), var(--neumorphism-inset-dark);
  border: none;
}

.custom-input :deep(.el-input__inner) {
  width: 100%;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.remember-checkbox {
  color: var(--text-secondary);
}

.forgot-link {
  font-size: 0.875rem;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 1rem;
  font-weight: 500;
  border-radius: var(--border-radius-md);
}

.social-login {
  margin-bottom: var(--spacing-lg);
}

.divider {
  position: relative;
  text-align: center;
  margin: var(--spacing-lg) 0;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--glass-border);
}

.divider-text {
  background: var(--surface-color);
  padding: 0 var(--spacing-md);
  color: var(--text-muted);
  font-size: 0.875rem;
  position: relative;
  z-index: 1;
}

.social-buttons {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
}

.social-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-md);
  border: none;
  border-radius: var(--border-radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  min-width: 80px;
}

.social-btn i {
  font-size: 1.25rem;
}

.social-btn span {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.register-link {
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
  margin-bottom: var(--spacing-lg);
}

.features-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--text-secondary);
}

.feature-item i {
  font-size: 1rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg);
  }
  
  .info-panel {
    order: -1;
    padding: var(--spacing-lg);
  }
  
  .login-form-container {
    padding: var(--spacing-lg);
  }
  
  .social-buttons {
    flex-direction: column;
  }
  
  .social-btn {
    flex-direction: row;
    justify-content: center;
    min-width: auto;
    width: 100%;
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: var(--spacing-md);
  }
  
  .form-title {
    font-size: 1.5rem;
  }
  
  .panel-title {
    font-size: 1.25rem;
  }
}
</style>