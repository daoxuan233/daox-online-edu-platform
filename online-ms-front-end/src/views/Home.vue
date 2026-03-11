<template>
  <div class="home-page">
    <!-- 顶部导航栏 -->
    <header class="top-header">
      <div class="header-container">
        <div class="logo-section">
          <img src="../assets/DaoX_C7-Center_Logo.svg" alt="DaoX Logo" class="logo-icon" style="height: 40px; width: auto; margin-right: 10px;" />
          <span class="logo-text">DaoX- EduPlatform</span>
        </div>
        <div class="header-actions">
          <el-button 
            class="login-btn" 
            @click="$router.push('/login')"
          >
            <font-awesome-icon :icon="['fas', 'sign-in-alt']" class="mr-xs"/>
            登录
          </el-button>
          <el-button 
            type="primary" 
            class="register-btn"
            @click="$router.push('/register')"
          >
            <font-awesome-icon :icon="['fas', 'user-plus']" class="mr-xs"/>
            注册
          </el-button>
        </div>
      </div>
    </header>

    <!-- 英雄区域 -->
    <section class="hero-section">
      <!-- 动态背景粒子 -->
      <div class="particles-background">
        <div class="particle" v-for="n in 50" :key="n" :style="getParticleStyle(n)"></div>
      </div>
      
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">
            开启您的
            <span class="highlight-text">学习之旅</span>
          </h1>
          <p class="hero-description">
            探索海量优质课程，与专业讲师互动学习，
            提升技能，成就更好的自己。
          </p>
          <div class="hero-actions">
            <el-button
                type="primary"
                size="large"
                class="cta-button modern-button"
                @click="$router.push('/register')"
            >
              <font-awesome-icon :icon="['fas', 'rocket']" class="mr-sm"/>
              立即注册
            </el-button>
            <el-button
                size="large"
                class="secondary-button modern-button"
                @click="scrollToFeatures"
            >
              <font-awesome-icon :icon="['fas', 'info-circle']" class="mr-sm"/>
              了解更多
            </el-button>
          </div>
        </div>
        <div class="hero-visual">
          <div class="visual-container modern-card">
            <!-- 3D旋转环 -->
            <div class="rotating-rings">
              <div class="ring ring-1"></div>
              <div class="ring ring-2"></div>
              <div class="ring ring-3"></div>
            </div>
            
            <!-- 浮动图标群 -->
            <div class="floating-icons">
              <div class="icon-orbit orbit-1">
                <div class="icon-wrapper">
                  <font-awesome-icon :icon="['fas', 'book-open']" class="floating-icon"/>
                </div>
              </div>
              <div class="icon-orbit orbit-2">
                <div class="icon-wrapper">
                  <font-awesome-icon :icon="['fas', 'graduation-cap']" class="floating-icon"/>
                </div>
              </div>
              <div class="icon-orbit orbit-3">
                <div class="icon-wrapper">
                  <font-awesome-icon :icon="['fas', 'certificate']" class="floating-icon"/>
                </div>
              </div>
              <div class="icon-orbit orbit-4">
                <div class="icon-wrapper">
                  <font-awesome-icon :icon="['fas', 'users']" class="floating-icon"/>
                </div>
              </div>
              <div class="icon-orbit orbit-5">
                <div class="icon-wrapper">
                  <font-awesome-icon :icon="['fas', 'brain']" class="floating-icon"/>
                </div>
              </div>
              <div class="icon-orbit orbit-6">
                <div class="icon-wrapper">
                  <font-awesome-icon :icon="['fas', 'lightbulb']" class="floating-icon"/>
                </div>
              </div>
            </div>
            
            <!-- 中央3D图形 - 替换为学生卡片 -->
            <div class="central-3d">
              <div class="student-card-visual glass-card-effect">
                <div class="card-header-visual">
                  <div class="avatar-circle-visual">
                    <font-awesome-icon :icon="['fas', 'user-graduate']" />
                  </div>
                  <div class="card-info-visual">
                    <div class="info-name-visual">Alex Chen</div>
                    <div class="info-status-visual">正在学习: Java 高级编程</div>
                  </div>
                </div>
                <div class="card-body-visual">
                  <div class="course-preview-visual">
                    <div class="play-icon-wrapper">
                      <font-awesome-icon :icon="['fas', 'play']" class="play-icon-visual" />
                    </div>
                    <div class="wave-visual"></div>
                  </div>
                  <div class="progress-container-visual">
                    <div class="progress-label-visual">
                      <span>学习进度</span>
                      <span>78%</span>
                    </div>
                    <div class="progress-bar-bg-visual">
                      <div class="progress-bar-fill-visual"></div>
                    </div>
                  </div>
                </div>
                <!-- 浮动徽章 -->
                <div class="floating-badge badge-1">
                  <font-awesome-icon :icon="['fas', 'check-circle']" />
                  <span>已打卡</span>
                </div>
              </div>
            </div>
            
            <!-- 能量波纹 -->
            <div class="energy-waves">
              <div class="wave wave-1"></div>
              <div class="wave wave-2"></div>
              <div class="wave wave-3"></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 统计数据 -->
    <section class="stats-section">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-item neumorphism-card" v-for="stat in stats" :key="stat.label">
            <div class="stat-icon">
              <font-awesome-icon :icon="stat.icon"/>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能特色 -->
    <section class="features-section" ref="featuresSection">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">为什么选择我们</h2>
          <p class="section-subtitle">专业的在线教育平台，为您提供最佳学习体验</p>
        </div>

        <div class="features-grid">
          <div
              class="feature-card neumorphism-card"
              v-for="feature in features"
              :key="feature.id"
          >
            <div class="feature-icon">
              <font-awesome-icon :icon="feature.icon"/>
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-description">{{ feature.description }}</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const featuresSection = ref(null)

// 统计数据
const stats = ref([
  {
    icon: ['fas', 'users'],
    value: '10,000+',
    label: '注册学员'
  },
  {
    icon: ['fas', 'book'],
    value: '500+',
    label: '精品课程'
  },
  {
    icon: ['fas', 'chalkboard-teacher'],
    value: '100+',
    label: '专业讲师'
  },
  {
    icon: ['fas', 'certificate'],
    value: '95%',
    label: '完课率'
  }
])

// 功能特色
const features = ref([
  {
    id: 1,
    icon: ['fas', 'video'],
    title: '丰富课程资源',
    description: '涵盖多个领域的专业课程，满足不同学习需求，助力技能提升'
  },
  {
    id: 2,
    icon: ['fas', 'users-cog'],
    title: '专业师资团队',
    description: '经验丰富的行业专家和资深讲师，提供高质量的教学内容'
  },
  {
    id: 3,
    icon: ['fas', 'mobile-alt'],
    title: '随时随地学习',
    description: '支持多终端学习，PC、手机、平板随时切换，学习更自由'
  },
  {
    id: 4,
    icon: ['fas', 'certificate'],
    title: '权威认证证书',
    description: '完成课程获得权威认证证书，为职业发展增添有力证明'
  },
  {
    id: 5,
    icon: ['fas', 'comments'],
    title: '互动学习体验',
    description: '在线讨论、实时答疑、作业批改，打造沉浸式学习体验'
  },
  {
    id: 6,
    icon: ['fas', 'chart-line'],
    title: '学习进度跟踪',
    description: '智能学习分析，实时跟踪学习进度，个性化推荐学习路径'
  }
])

// 滚动到功能特色区域
const scrollToFeatures = () => {
  if (featuresSection.value) {
    featuresSection.value.scrollIntoView({ 
      behavior: 'smooth',
      block: 'start'
    })
  }
}

// 生成粒子样式
const getParticleStyle = (index) => {
  const size = Math.random() * 4 + 2
  const x = Math.random() * 100
  const y = Math.random() * 100
  const duration = Math.random() * 20 + 10
  const delay = Math.random() * 5
  
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${x}%`,
    top: `${y}%`,
    animationDuration: `${duration}s`,
    animationDelay: `${delay}s`
  }
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
}

/* 顶部导航栏 */
.top-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(240, 240, 243, 0.25);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.18);
  padding: var(--spacing-md) 0;
  transition: all var(--transition-normal);
  box-shadow: 0 8px 32px rgba(0, 47, 167, 0.1);
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.logo-icon {
  font-size: 1.5rem;
  color: #002FA7;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 
    4px 4px 8px rgba(0, 47, 167, 0.3),
    -4px -4px 8px rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
}

.logo-icon::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.logo-icon:hover::before {
  left: 100%;
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 700;
  background: #F0F9FF;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-actions {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
}

.login-btn {
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  color: #002FA7;
  border: 1px solid rgba(0, 47, 167, 0.2);
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 
    4px 4px 8px rgba(0, 47, 167, 0.1),
    -4px -4px 8px rgba(255, 255, 255, 0.3);
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.login-btn:hover::before {
  left: 100%;
}

.login-btn:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.4);
  box-shadow: 
    6px 6px 12px rgba(0, 47, 167, 0.15),
    -6px -6px 12px rgba(255, 255, 255, 0.4);
}

.register-btn {
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 600;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  color: #FFFFFF;
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 
    6px 6px 12px rgba(0, 47, 167, 0.3),
    -6px -6px 12px rgba(255, 255, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.register-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.register-btn:hover::before {
  left: 100%;
}

.register-btn:hover {
  transform: translateY(-3px);
  box-shadow: 
    8px 8px 16px rgba(0, 47, 167, 0.4),
    -8px -8px 16px rgba(255, 255, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.mr-xs {
  margin-right: 0.25rem;
}

/* 英雄区域 */
.hero-section {
  padding: calc(80px + var(--spacing-xl)) 0 var(--spacing-xl) 0;
  background: linear-gradient(135deg, #001f3f 0%, #003366 50%, #004080 100%); /* 更深邃的科技蓝背景 */
  position: relative;
  overflow: hidden;
  min-height: 100vh;
}

/* 动态背景粒子 */
.particles-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.particle {
  position: absolute;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  animation: float infinite ease-in-out;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
    opacity: 0.8;
  }
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-xl);
  align-items: center;
  min-height: 80vh;
  position: relative;
  z-index: 2;
}

.hero-text {
  z-index: 3;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  color: #FFFFFF;
  margin-bottom: var(--spacing-lg);
  line-height: 1.2;
  text-shadow: 0 0 30px rgba(0, 255, 255, 0.3); /* 发光效果 */
}

.highlight-text {
  background: linear-gradient(135deg, #00FFFF 0%, #FFFFFF 100%); /* 亮青色到白色渐变 */
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  filter: drop-shadow(0 0 10px rgba(0, 255, 255, 0.5));
}

.highlight-text::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #00FFFF, transparent);
  border-radius: 2px;
  box-shadow: 0 0 10px rgba(0, 255, 255, 0.8);
}

.hero-description {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: var(--spacing-xl);
  line-height: 1.6;
  font-weight: 300;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.hero-actions {
  display: flex;
  gap: var(--spacing-md);
}

/* 现代化按钮样式 */
.modern-button {
  position: relative;
  padding: 16px 36px;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 50px; /* 圆角更大，更现代 */
  border: none;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.modern-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.5s;
}

.modern-button:hover::before {
  left: 100%;
}

.cta-button {
  background: linear-gradient(135deg, #0066FF 0%, #00CCFF 100%); /* 高饱和度科技蓝 */
  color: #FFFFFF;
  box-shadow: 0 0 20px rgba(0, 204, 255, 0.6); /* 强发光 */
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.cta-button:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 0 30px rgba(0, 204, 255, 0.8);
}

.secondary-button {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  color: #FFFFFF;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.secondary-button:hover {
  transform: translateY(-3px);
  background: rgba(255, 255, 255, 0.2);
  border-color: #FFFFFF;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
}

.hero-visual {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 600px;
  perspective: 1000px;
  z-index: 2;
}

.visual-container {
  width: 500px;
  height: 500px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  transform-style: preserve-3d;
}

/* 3D旋转环 - 更细更轻盈 */
.rotating-rings {
  position: absolute;
  width: 460px;
  height: 460px;
  transform-style: preserve-3d;
  animation: rotate3d 30s infinite linear;
}

.ring {
  position: absolute;
  border: 1px solid; /* 变细 */
  border-radius: 50%;
  opacity: 0.4; /* 降低不透明度 */
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.1);
}

.ring-1 {
  width: 360px;
  height: 360px;
  border-color: rgba(0, 255, 255, 0.5);
  top: 50px;
  left: 50px;
  transform: rotateX(0deg) rotateY(0deg);
}

.ring-2 {
  width: 280px;
  height: 280px;
  border-color: rgba(255, 255, 255, 0.4);
  top: 90px;
  left: 90px;
  transform: rotateX(60deg) rotateY(60deg);
}

.ring-3 {
  width: 200px;
  height: 200px;
  border-color: rgba(0, 102, 255, 0.5);
  top: 130px;
  left: 130px;
  transform: rotateX(120deg) rotateY(120deg);
}

@keyframes rotate3d {
  0% {
    transform: rotateX(0deg) rotateY(0deg) rotateZ(0deg);
  }
  100% {
    transform: rotateX(360deg) rotateY(360deg) rotateZ(360deg);
  }
}

@keyframes ringPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
  }
}

/* 浮动图标群 - 调整为更科技感 */
.floating-icons {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.icon-orbit {
  position: absolute;
  width: 50px;
  height: 50px;
  animation: floatIcon 8s infinite ease-in-out;
}

.icon-wrapper {
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(5px);
  border-radius: 50%; /* 圆形图标 */
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 0 15px rgba(0, 255, 255, 0.1);
}

.floating-icon {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.8);
}

.orbit-1 { top: 15%; left: 15%; animation-delay: 0s; }
.orbit-2 { top: 25%; right: 20%; animation-delay: 1.5s; }
.orbit-3 { bottom: 25%; left: 10%; animation-delay: 3s; }
.orbit-4 { bottom: 15%; right: 15%; animation-delay: 4.5s; }
.orbit-5 { top: 60%; left: 5%; animation-delay: 2s; }
.orbit-6 { top: 40%; right: 5%; animation-delay: 5s; }

@keyframes floatIcon {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

/* 中央学生卡片视觉 */
.central-3d {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
  width: 340px;
}

.student-card-visual {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 24px;
  box-shadow: 
    0 20px 50px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  position: relative;
  transform: rotateY(-5deg) rotateX(5deg);
  transition: transform 0.5s ease;
  animation: cardFloat 6s ease-in-out infinite;
}

@keyframes cardFloat {
  0%, 100% { transform: rotateY(-5deg) rotateX(5deg) translateY(0); }
  50% { transform: rotateY(-5deg) rotateX(5deg) translateY(-15px); }
}

.card-header-visual {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.avatar-circle-visual {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00C6FF, #0072FF);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.info-name-visual {
  color: white;
  font-weight: 700;
  font-size: 1.1rem;
  margin-bottom: 4px;
}

.info-status-visual {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.8rem;
}

.card-body-visual {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 0;
}

.course-preview-visual {
  height: 100px;
  background: linear-gradient(135deg, rgba(255,255,255,0.1), rgba(255,255,255,0.05));
  border-radius: 12px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.play-icon-wrapper {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
  z-index: 2;
  cursor: pointer;
  transition: transform 0.3s;
}

.play-icon-wrapper:hover {
  transform: scale(1.1);
  background: rgba(255, 255, 255, 0.3);
}

.play-icon-visual {
  color: white;
  font-size: 14px;
  margin-left: 2px;
}

.progress-container-visual {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-label-visual {
  display: flex;
  justify-content: space-between;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.8rem;
}

.progress-bar-bg-visual {
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar-fill-visual {
  height: 100%;
  width: 78%;
  background: linear-gradient(90deg, #00C6FF, #0072FF);
  border-radius: 3px;
}

.floating-badge {
  position: absolute;
  top: -10px;
  right: -10px;
  background: rgba(46, 204, 113, 0.9); /* Success Green */
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(4px);
  animation: badgeFloat 3s ease-in-out infinite;
}

@keyframes badgeFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

/* 能量波纹 - 移除或简化 */
.energy-waves {
  display: none;
}

.floating-elements {
  position: absolute;
  width: 100%;
  height: 100%;
}

.element {
  position: absolute;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--surface-color);
  box-shadow: var(--neumorphism-light), var(--neumorphism-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: orbit 20s linear infinite;
}

.element i {
  font-size: 1.25rem;
  color: var(--primary-color);
}

.element-1 {
  top: 10%;
  left: 50%;
  transform: translateX(-50%);
  animation-delay: 0s;
}

.element-2 {
  top: 50%;
  right: 10%;
  transform: translateY(-50%);
  animation-delay: 5s;
}

.element-3 {
  bottom: 10%;
  left: 50%;
  transform: translateX(-50%);
  animation-delay: 10s;
}

.element-4 {
  top: 50%;
  left: 10%;
  transform: translateY(-50%);
  animation-delay: 15s;
}

@keyframes orbit {
  from {
    transform: rotate(0deg) translateX(120px) rotate(0deg);
  }
  to {
    transform: rotate(360deg) translateX(120px) rotate(-360deg);
  }
}

.central-graphic {
  z-index: 1;
}

/* 统计数据 */
.stats-section {
  padding: var(--spacing-xl) 0;
  background: #FFFFFF; /* 纯白背景 */
  position: relative;
  overflow: hidden;
}

.stats-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grid" width="10" height="10" patternUnits="userSpaceOnUse"><path d="M 10 0 L 0 0 0 10" fill="none" stroke="%23000000" stroke-width="0.5" opacity="0.03"/></pattern></defs><rect width="100" height="100" fill="url(%23grid)"/></svg>');
  opacity: 1; /* 图案内置了低透明度 */
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-lg);
  position: relative;
  z-index: 2;
}

.stat-item {
  padding: var(--spacing-xl);
  text-align: center;
  background: #FFFFFF;
  border-radius: 20px;
  /* 极简阴影 - 悬浮感 */
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04); 
  border: 1px solid rgba(0, 0, 0, 0.02);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-item::before {
  display: none; /* 移除原来的高光扫过效果，保持简洁 */
}

.stat-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  margin-bottom: var(--spacing-md);
}

.stat-icon i {
  font-size: 2.5rem;
  background: linear-gradient(135deg, #0066FF 0%, #00CCFF 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-number {
  font-size: 3rem;
  font-weight: 700;
  color: #333;
  -webkit-text-fill-color: #333; /* 移除渐变文字，更清晰 */
  margin-bottom: var(--spacing-xs);
  position: relative;
}

.stat-label {
  color: #666;
  font-size: 1rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 通用区域样式 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
}

.section-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
  position: relative;
}

.section-title {
  font-size: 2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.section-subtitle {
  color: var(--text-secondary);
  font-size: 1rem;
  margin-bottom: var(--spacing-lg);
}

.view-all-btn {
  position: absolute;
  right: 0;
  top: 0;
}

/* 热门课程 */
.featured-courses-section {
  padding: var(--spacing-xl) 0;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--spacing-lg);
}

/* 学习路径 */
.learning-paths-section {
  padding: var(--spacing-xl) 0;
  background: var(--surface-color);
}

.paths-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: var(--spacing-lg);
}

.path-card {
  padding: var(--spacing-lg);
  border-radius: var(--border-radius-lg);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.path-card:hover {
  transform: translateY(-4px);
}

.path-header {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.path-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--border-radius-md);
  background: var(--background-color);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.path-icon i {
  font-size: 1.25rem;
}

.path-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.path-description {
  color: var(--text-secondary);
  font-size: 0.875rem;
  line-height: 1.4;
}

.path-stats {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.path-stat {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.path-progress {
  margin-top: auto;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
  font-size: 0.875rem;
}

.progress-label {
  color: var(--text-secondary);
}

.progress-value {
  color: var(--primary-color);
  font-weight: 500;
}

/* 功能特色 */
.features-section {
  padding: var(--spacing-xl) 0;
  background: #FAFAFA; /* 极简灰白背景 */
  position: relative;
}

.features-section::before {
  display: none; /* 移除复杂的背景渐变 */
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: var(--spacing-xl);
  position: relative;
  z-index: 2;
}

.feature-card {
  padding: var(--spacing-xl);
  text-align: center;
  background: #FFFFFF;
  border-radius: 24px;
  border: 1px solid rgba(0, 0, 0, 0.03);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.03); /* 极轻阴影 */
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  display: none;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.06);
}

.feature-icon {
  width: 80px;
  height: 80px;
  border-radius: 24px; /* 方圆形 */
  background: rgba(0, 102, 255, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--spacing-lg);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
}

.feature-icon::before {
  display: none;
}

.feature-card:hover .feature-icon {
  background: rgba(0, 102, 255, 0.1);
  transform: scale(1.1);
}

.feature-icon i {
  font-size: 2rem;
  background: linear-gradient(135deg, #0066FF 0%, #00CCFF 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  z-index: 1;
}

.feature-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #333;
  margin-bottom: var(--spacing-md);
  position: relative;
}

.feature-title::after {
  display: none; /* 移除下划线 */
}

.feature-description {
  color: #666;
  line-height: 1.6;
  font-size: 1rem;
  font-weight: 400;
}

/* 最新动态 */
.news-section {
  padding: var(--spacing-xl) 0;
  background: var(--surface-color);
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: var(--spacing-lg);
}

.news-card {
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.news-card:hover {
  transform: translateY(-4px);
}

.news-image {
  height: 160px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-placeholder i {
  font-size: 3rem;
  color: white;
  opacity: 0.8;
}

.news-category {
  position: absolute;
  top: var(--spacing-md);
  right: var(--spacing-md);
  background: rgba(255, 255, 255, 0.9);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-sm);
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--text-primary);
}

.news-content {
  padding: var(--spacing-lg);
}

.news-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
  line-height: 1.4;
}

.news-summary {
  color: var(--text-secondary);
  font-size: 0.875rem;
  line-height: 1.5;
  margin-bottom: var(--spacing-md);
}

.news-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: var(--text-muted);
}

.news-date,
.news-views {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-container {
    padding: 0 var(--spacing-md);
  }

  .logo-icon {
    width: 32px;
    height: 32px;
    font-size: 1rem;
  }

  .logo-text {
    font-size: 1.25rem;
  }

  .header-actions {
    gap: var(--spacing-xs);
  }

  .login-btn,
  .register-btn {
    padding: 8px 16px;
    font-size: 0.875rem;
    letter-spacing: 0.3px;
  }

  .hero-section {
    min-height: 100vh;
    padding: calc(60px + var(--spacing-lg)) 0 var(--spacing-lg) 0;
  }

  .hero-content {
    grid-template-columns: 1fr;
    text-align: center;
    gap: var(--spacing-lg);
    min-height: 70vh;
  }

  .hero-title {
    font-size: 2.5rem;
  }

  .hero-visual {
    height: 400px;
  }

  .rotating-rings {
    width: 300px;
    height: 300px;
  }

  .ring-1 {
    width: 200px;
    height: 200px;
    top: 50px;
    left: 50px;
  }

  .ring-2 {
    width: 150px;
    height: 150px;
    top: 75px;
    left: 75px;
  }

  .ring-3 {
    width: 100px;
    height: 100px;
    top: 100px;
    left: 100px;
  }

  .cube-container {
    width: 80px;
    height: 80px;
  }

  .face {
    width: 80px;
    height: 80px;
  }

  .central-icon {
    font-size: 20px;
  }

  .floating-icon {
    font-size: 16px;
  }

  .icon-orbit {
    width: 40px;
    height: 40px;
  }

  .hero-actions {
    justify-content: center;
    flex-direction: column;
    align-items: center;
  }

  .modern-button {
    width: 100%;
    max-width: 280px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }

  .stat-item {
    padding: var(--spacing-lg);
  }

  .stat-number {
    font-size: 2.5rem;
  }

  .features-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg);
  }

  .feature-card {
    padding: var(--spacing-lg);
  }

  .feature-icon {
    width: 60px;
    height: 60px;
  }

  .feature-icon i {
    font-size: 1.5rem;
  }

  .visual-container {
    width: 350px;
    height: 350px;
  }

  .view-all-btn {
    position: static;
    margin-top: var(--spacing-md);
  }

  .section-header {
    text-align: center;
  }
}

@media (max-width: 480px) {
  .header-container {
    padding: 0 var(--spacing-sm);
  }

  .logo-text {
    display: none;
  }

  .header-actions {
    gap: var(--spacing-xs);
  }

  .login-btn,
  .register-btn {
    padding: var(--spacing-xs) var(--spacing-sm);
    font-size: 0.75rem;
  }

  .hero-title {
    font-size: 2rem;
  }

  .hero-description {
    font-size: 1rem;
  }

  .hero-visual {
    height: 300px;
  }

  .rotating-rings {
    width: 200px;
    height: 200px;
  }

  .ring-1 {
    width: 150px;
    height: 150px;
    top: 25px;
    left: 25px;
  }

  .ring-2 {
    width: 100px;
    height: 100px;
    top: 50px;
    left: 50px;
  }

  .ring-3 {
    width: 50px;
    height: 50px;
    top: 75px;
    left: 75px;
  }

  .cube-container {
    width: 60px;
    height: 60px;
  }

  .face {
    width: 60px;
    height: 60px;
  }

  .central-icon {
    font-size: 16px;
  }

  .visual-container {
    width: 250px;
    height: 250px;
  }

  .section-title {
    font-size: 1.5rem;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .hero-actions {
    flex-direction: column;
  }

  .cta-button,
  .secondary-button {
    width: 100%;
  }
}
</style>