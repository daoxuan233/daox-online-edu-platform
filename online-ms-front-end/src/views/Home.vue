<template>
  <div class="home-page" ref="mainContainer">
    <!-- 动态科技背景 -->
    <div class="tech-background">
      <div class="glow-orb orb-1"></div>
      <div class="glow-orb orb-2"></div>
      <div class="glow-orb orb-3"></div>
      <div class="grid-overlay"></div>
      <div class="particles-container" ref="particlesRef"></div>
    </div>

    <!-- 顶部导航栏 -->
    <header class="top-header glass-card" ref="headerRef">
      <div class="header-container">
        <div class="logo-section">
          <img src="../assets/DaoX_C7-Center_Logo.svg" alt="DaoX Logo" class="logo-icon" />
          <span class="logo-text">DaoX-EduPlatform</span>
        </div>
        <div class="header-actions">
          <button class="glass-btn" @click="$router.push('/login')">
            <font-awesome-icon :icon="['fas', 'sign-in-alt']" class="mr-xs"/> 登录
          </button>
          <button class="primary-btn" @click="$router.push('/register')">
            <font-awesome-icon :icon="['fas', 'rocket']" class="mr-xs"/> 注册
          </button>
        </div>
      </div>
    </header>

    <!-- 英雄区域 -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-text" ref="heroTextRef">
          <div class="badge glass-badge">
            <span class="pulse-dot"></span>
            🚀 引领未来的学习方式
          </div>
          <h1 class="hero-title">
            开启您的<br/>
            <span class="gradient-text">数字学习之旅</span>
          </h1>
          <p class="hero-description">
            探索海量前沿优质课程，与顶级行业讲师互动。
            全方位重塑您的核心竞争力，触达科技最前沿。
          </p>
          <div class="hero-actions">
            <button class="primary-btn large-btn btn-glow" @click="$router.push('/register')">
              立即探索 <font-awesome-icon :icon="['fas', 'arrow-right']" class="ml-xs"/>
            </button>
            <button class="glass-btn large-btn" @click="scrollToFeatures">
              了解更多
            </button>
          </div>
        </div>
        
        <div class="hero-visual" ref="heroVisualRef">
          <!-- 3D 浮动卡片群 -->
          <div class="floating-card main-card glass-panel">
            <div class="card-glow"></div>
            <div class="card-header">
              <div class="avatar"><font-awesome-icon :icon="['fas', 'user-astronaut']"/></div>
              <div class="info">
                <h4>Alex Chen</h4>
                <p>AI 工程架构师</p>
              </div>
            </div>
            <div class="card-body">
              <div class="progress-block">
                <div class="label"><span>深度学习模型训练</span><span class="highlight">85%</span></div>
                <div class="bar-bg"><div class="bar-fill" style="width: 85%"></div></div>
              </div>
              <div class="tech-tags">
                <span class="tag">PyTorch</span>
                <span class="tag">TensorFlow</span>
                <span class="tag">CUDA</span>
              </div>
            </div>
          </div>
          <div class="floating-card sub-card card-1 glass-panel">
            <div class="icon-ring">
              <font-awesome-icon :icon="['fas', 'brain']" class="card-icon"/>
            </div>
            <span>AI 智能伴学</span>
          </div>
          <div class="floating-card sub-card card-2 glass-panel">
            <div class="icon-ring">
              <font-awesome-icon :icon="['fas', 'code']" class="card-icon"/>
            </div>
            <span>云端沉浸编程</span>
          </div>
          <div class="floating-card sub-card card-3 glass-panel">
            <div class="icon-ring">
              <font-awesome-icon :icon="['fas', 'chart-network']" class="card-icon"/>
            </div>
            <span>全息数据雷达</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 数据统计区域 -->
    <section class="stats-section" ref="statsSectionRef">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-card glass-panel" v-for="(stat, index) in stats" :key="index">
            <div class="stat-glow"></div>
            <div class="stat-icon-wrapper">
              <font-awesome-icon :icon="stat.icon" class="stat-icon"/>
            </div>
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 特色功能区域 -->
    <section class="features-section" ref="featuresSectionRef">
      <div class="container">
        <div class="section-header" ref="featuresHeaderRef">
          <h2 class="section-title">核心<span class="gradient-text">驱动引擎</span></h2>
          <p class="section-subtitle">基于下一代科技架构，为您打造极具未来感的在线教育体验</p>
        </div>
        <div class="features-grid">
          <div class="feature-card glass-panel" v-for="(feature, index) in features" :key="feature.id">
            <div class="feature-glow-bg"></div>
            <div class="feature-content">
              <div class="feature-icon-box">
                <font-awesome-icon :icon="feature.icon"/>
              </div>
              <h3 class="feature-title">{{ feature.title }}</h3>
              <p class="feature-desc">{{ feature.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'

gsap.registerPlugin(ScrollTrigger)

const router = useRouter()

// DOM Refs
const mainContainer = ref(null)
const headerRef = ref(null)
const heroTextRef = ref(null)
const heroVisualRef = ref(null)
const statsSectionRef = ref(null)
const featuresSectionRef = ref(null)
const featuresHeaderRef = ref(null)
const particlesRef = ref(null)

let ctx

// Data
const stats = [
  { icon: ['fas', 'users'], value: '50K+', label: '全球极客学员' },
  { icon: ['fas', 'cubes'], value: '1000+', label: '前沿技术课程' },
  { icon: ['fas', 'user-tie'], value: '200+', label: '行业大牛导师' },
  { icon: ['fas', 'bolt'], value: '99%', label: '高效完课率' }
]

const features = [
  { id: 1, icon: ['fas', 'microchip'], title: '元宇宙级课程', description: '采用 WebGL 与 3D 渲染技术，打造身临其境的交互式学习环境，让抽象知识具象化。' },
  { id: 2, icon: ['fas', 'robot'], title: 'AI 智能导师', description: '集成大模型能力的 24/7 专属 AI 导师，根据您的学习曲线实时动态调整教学策略。' },
  { id: 3, icon: ['fas', 'satellite-dish'], title: '毫秒级云同步', description: '采用分布式边缘计算架构，实现多终端学习进度无缝流转，随时随地开启学习。' },
  { id: 4, icon: ['fas', 'shield-halved'], title: '区块链确权', description: '学习记录与结业证书全面上链，加密不可篡改，构建全球互认的数字技能凭证。' },
  { id: 5, icon: ['fas', 'network-wired'], title: '高维极客生态', description: '汇聚全球顶尖开发者的互动社区，支持代码级在线协作与开源项目协同共建。' },
  { id: 6, icon: ['fas', 'chart-pie'], title: '全息能力画像', description: '多维度的大数据分析雷达，精准定位您的技术长短板，智能规划最优成长路径。' }
]

const scrollToFeatures = () => {
  featuresSectionRef.value?.scrollIntoView({ behavior: 'smooth' })
}

const initParticles = () => {
  if (!particlesRef.value) return
  const particleCount = 50
  for (let i = 0; i < particleCount; i++) {
    const particle = document.createElement('div')
    particle.classList.add('particle')
    const size = Math.random() * 3 + 1
    particle.style.width = `${size}px`
    particle.style.height = `${size}px`
    particle.style.left = `${Math.random() * 100}%`
    particle.style.top = `${Math.random() * 100}%`
    particlesRef.value.appendChild(particle)
  }

  // Animate particles with GSAP
  gsap.to('.particle', {
    y: 'random(-100, 100)',
    x: 'random(-100, 100)',
    opacity: 'random(0.1, 0.8)',
    duration: 'random(10, 20)',
    ease: 'sine.inOut',
    repeat: -1,
    yoyo: true,
    stagger: {
      amount: 5,
      from: 'random'
    }
  })
}

onMounted(() => {
  ctx = gsap.context(() => {
    // 检查 reduced motion
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    if (prefersReducedMotion) {
      gsap.set([headerRef.value, heroTextRef.value.children, '.floating-card', '.stat-card', '.feature-card', featuresHeaderRef.value.children], { opacity: 1, y: 0, x: 0, scale: 1 })
      return
    }

    initParticles()

    // 1. Header 入场
    gsap.from(headerRef.value, {
      y: -50,
      opacity: 0,
      duration: 1,
      ease: 'power3.out',
      delay: 0.1
    })

    // 2. Hero 文本序列入场
    const heroElements = heroTextRef.value.children
    gsap.from(heroElements, {
      x: -40,
      opacity: 0,
      duration: 1,
      stagger: 0.15,
      ease: 'power3.out',
      delay: 0.3
    })

    // 3. Hero 视觉卡片入场 & 浮动
    const visualCards = heroVisualRef.value.querySelectorAll('.floating-card')
    gsap.from(visualCards, {
      scale: 0.8,
      opacity: 0,
      y: 60,
      rotationX: 10,
      rotationY: -10,
      duration: 1.5,
      stagger: 0.2,
      ease: 'expo.out',
      delay: 0.5,
      onComplete: () => {
        // 持续悬浮动效
        visualCards.forEach((card, i) => {
          gsap.to(card, {
            y: `-=${15 + i * 5}`,
            rotationZ: i % 2 === 0 ? 1.5 : -1.5,
            duration: 3 + i * 0.5,
            yoyo: true,
            repeat: -1,
            ease: 'sine.inOut'
          })
        })
      }
    })

    // 4. 背景光球呼吸动效
    gsap.to('.glow-orb', {
      scale: 1.15,
      opacity: 0.7,
      duration: 5,
      yoyo: true,
      repeat: -1,
      stagger: 2,
      ease: 'sine.inOut'
    })

    // 5. 滚动触发：Stats
    const statCards = statsSectionRef.value.querySelectorAll('.stat-card')
    ScrollTrigger.batch(statCards, {
      start: 'top 85%',
      onEnter: (elements) => {
        gsap.fromTo(elements, 
          { y: 50, opacity: 0 },
          { y: 0, opacity: 1, duration: 0.8, stagger: 0.15, ease: 'power3.out', overwrite: true }
        )
      }
    })

    // 6. 滚动触发：Features 标题
    gsap.from(featuresHeaderRef.value.children, {
      scrollTrigger: {
        trigger: featuresSectionRef.value,
        start: 'top 85%',
      },
      y: 30,
      opacity: 0,
      duration: 0.8,
      stagger: 0.2,
      ease: 'power2.out'
    })

    // 7. 滚动触发：Features 卡片
    const featureCards = featuresSectionRef.value.querySelectorAll('.feature-card')
    ScrollTrigger.batch(featureCards, {
      start: 'top 80%',
      onEnter: (elements) => {
        gsap.fromTo(elements,
          { scale: 0.9, opacity: 0, y: 40 },
          { scale: 1, opacity: 1, y: 0, duration: 0.7, stagger: 0.1, ease: 'back.out(1.2)', overwrite: true }
        )
      }
    })

  }, mainContainer.value)
})

onUnmounted(() => {
  if (ctx) ctx.revert() // 清理所有 GSAP 动画和 ScrollTrigger
})
</script>

<style scoped>
/* 全局变量与科技风基础配置 */
:root {
  --primary: #0061ff;
  --secondary: #60efff;
  --bg-dark: #050914;
  --text-main: #ffffff;
  --text-muted: rgba(255, 255, 255, 0.65);
  --glass-bg: rgba(15, 22, 40, 0.45);
  --glass-border: rgba(255, 255, 255, 0.12);
  --radius-lg: 24px;
  --radius-md: 16px;
}

.home-page {
  background-color: var(--bg-dark);
  color: var(--text-main);
  min-height: 100vh;
  font-family: 'Inter', 'PingFang SC', sans-serif;
  overflow-x: hidden;
  position: relative;
  /* 确保不影响内部 sticky/fixed */
}

/* ================= 动态科技背景 ================= */
.tech-background {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: 0;
  pointer-events: none;
  background: radial-gradient(circle at 50% 50%, #0a1128 0%, #03050a 100%);
  overflow: hidden;
}

.grid-overlay {
  position: absolute;
  width: 200%; height: 200%;
  top: -50%; left: -50%;
  background-image: 
    linear-gradient(rgba(96, 239, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(96, 239, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  transform: perspective(1000px) rotateX(60deg) translateY(-100px) translateZ(-200px);
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% { transform: perspective(1000px) rotateX(60deg) translateY(0) translateZ(-200px); }
  100% { transform: perspective(1000px) rotateX(60deg) translateY(50px) translateZ(-200px); }
}

.glow-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.4;
}
.orb-1 { width: 450px; height: 450px; background: #0061ff; top: -150px; left: -100px; }
.orb-2 { width: 600px; height: 600px; background: #60efff; bottom: -250px; right: -150px; opacity: 0.25; }
.orb-3 { width: 400px; height: 400px; background: #8b5cf6; top: 40%; left: 50%; transform: translate(-50%, -50%); opacity: 0.15; }

.particles-container {
  position: absolute;
  width: 100%; height: 100%;
}
:deep(.particle) {
  position: absolute;
  background: #60efff;
  border-radius: 50%;
  box-shadow: 0 0 10px #60efff, 0 0 20px #60efff;
}

/* ================= 玻璃拟态卡片基础 ================= */
.glass-panel {
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.4);
  position: relative;
  overflow: hidden;
}

.glass-panel::before {
  content: '';
  position: absolute;
  top: 0; left: -100%; width: 50%; height: 100%;
  background: linear-gradient(to right, transparent, rgba(255,255,255,0.06), transparent);
  transform: skewX(-25deg);
  transition: 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-panel:hover::before {
  left: 150%;
}

.gradient-text {
  background: linear-gradient(135deg, var(--secondary) 0%, #ffffff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  display: inline-block;
  text-shadow: 0 0 30px rgba(96, 239, 255, 0.4);
}

/* ================= 按钮样式 ================= */
.primary-btn {
  background: linear-gradient(135deg, var(--primary) 0%, var(--secondary) 100%);
  color: #fff;
  border: none;
  padding: 10px 24px;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 97, 255, 0.4);
  position: relative;
  overflow: hidden;
}
.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(96, 239, 255, 0.5);
}

.glass-btn {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(12px);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 10px 24px;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}
.glass-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(96, 239, 255, 0.5);
  color: var(--secondary);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(96, 239, 255, 0.2);
}

.large-btn {
  padding: 16px 36px;
  font-size: 1.1rem;
  border-radius: 30px;
}

.btn-glow {
  animation: btnPulse 2s infinite;
}
@keyframes btnPulse {
  0% { box-shadow: 0 0 0 0 rgba(96, 239, 255, 0.4); }
  70% { box-shadow: 0 0 0 15px rgba(96, 239, 255, 0); }
  100% { box-shadow: 0 0 0 0 rgba(96, 239, 255, 0); }
}

.ml-xs { margin-left: 8px; }
.mr-xs { margin-right: 8px; }

/* ================= 头部 ================= */
.top-header {
  position: fixed;
  top: 24px; left: 50%;
  transform: translateX(-50%);
  width: 90%; max-width: 1200px;
  z-index: 1000;
  padding: 14px 28px;
  border-radius: 100px; /* 极度圆润的现代胶囊导航 */
  background: rgba(10, 15, 30, 0.5);
  border: 1px solid rgba(255,255,255,0.1);
  box-shadow: 0 10px 30px rgba(0,0,0,0.5);
  backdrop-filter: blur(24px);
}
.header-container {
  display: flex; justify-content: space-between; align-items: center;
}
.logo-section { display: flex; align-items: center; gap: 12px; }
.logo-icon { height: 36px; width: auto; filter: drop-shadow(0 0 8px rgba(96,239,255,0.6)); }
.logo-text { 
  font-size: 1.3rem; 
  font-weight: 800; 
  letter-spacing: 1px; 
  background: linear-gradient(90deg, #fff, #a5b4fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.header-actions { display: flex; gap: 16px; }

/* ================= Hero Section ================= */
.hero-section {
  position: relative;
  z-index: 1;
  padding-top: 160px;
  min-height: 100vh;
  display: flex;
  align-items: center;
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 60px;
  align-items: center;
  width: 100%;
}

.glass-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  background: rgba(96, 239, 255, 0.05);
  border: 1px solid rgba(96, 239, 255, 0.2);
  border-radius: 30px;
  color: var(--secondary);
  font-size: 0.9rem;
  font-weight: 600;
  margin-bottom: 24px;
  box-shadow: 0 0 20px rgba(96, 239, 255, 0.1);
  backdrop-filter: blur(10px);
}

.pulse-dot {
  width: 8px; height: 8px;
  background: var(--secondary);
  border-radius: 50%;
  box-shadow: 0 0 10px var(--secondary);
  animation: dotPulse 1.5s infinite;
}
@keyframes dotPulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

.hero-title {
  font-size: 4.5rem;
  font-weight: 900;
  line-height: 1.15;
  margin-bottom: 24px;
  letter-spacing: -1px;
}

.hero-description {
  font-size: 1.25rem;
  color: var(--text-muted);
  margin-bottom: 40px;
  line-height: 1.7;
  max-width: 90%;
  font-weight: 300;
}

.hero-actions { display: flex; gap: 20px; }

/* 3D Visuals */
.hero-visual {
  position: relative;
  height: 550px;
  perspective: 1200px;
}

.floating-card {
  position: absolute;
  transform-style: preserve-3d;
}

.main-card {
  width: 360px;
  padding: 30px;
  top: 50%; left: 50%;
  margin-top: -140px; margin-left: -180px;
  z-index: 3;
  background: rgba(15, 25, 45, 0.65);
  border: 1px solid rgba(96, 239, 255, 0.25);
  box-shadow: 0 30px 60px rgba(0,0,0,0.6), 0 0 40px rgba(0, 97, 255, 0.15);
  border-radius: 28px;
}

.card-glow {
  position: absolute;
  top: -50%; left: -50%; right: -50%; bottom: -50%;
  background: radial-gradient(circle at 50% 50%, rgba(96, 239, 255, 0.1), transparent 60%);
  pointer-events: none;
}

.card-header { display: flex; align-items: center; gap: 16px; margin-bottom: 28px; }
.avatar {
  width: 60px; height: 60px;
  border-radius: 20px; /* 超级圆角矩形 */
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  display: flex; align-items: center; justify-content: center;
  font-size: 1.8rem; color: #fff;
  box-shadow: 0 10px 20px rgba(0, 97, 255, 0.4);
}
.info h4 { margin: 0; font-size: 1.25rem; font-weight: 700; color: #fff; }
.info p { margin: 4px 0 0; font-size: 0.9rem; color: var(--secondary); font-weight: 500;}

.progress-block { 
  background: rgba(0,0,0,0.4); 
  padding: 20px; 
  border-radius: 16px; 
  border: 1px solid rgba(255,255,255,0.05);
  margin-bottom: 16px;
}
.progress-block .label { display: flex; justify-content: space-between; font-size: 0.9rem; margin-bottom: 12px; color: #e2e8f0; font-weight: 500; }
.progress-block .highlight { color: var(--secondary); font-weight: 700; }
.bar-bg { height: 8px; background: rgba(255,255,255,0.05); border-radius: 4px; overflow: hidden; box-shadow: inset 0 1px 3px rgba(0,0,0,0.5); }
.bar-fill { height: 100%; background: linear-gradient(90deg, var(--primary), var(--secondary)); box-shadow: 0 0 15px var(--secondary); border-radius: 4px; position: relative;}
.bar-fill::after {
  content: ''; position: absolute; top: 0; right: 0; bottom: 0; left: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
  animation: shimmer 2s infinite;
}
@keyframes shimmer { 0% { transform: translateX(-100%); } 100% { transform: translateX(100%); } }

.tech-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.tech-tags .tag { 
  font-size: 0.75rem; padding: 4px 12px; 
  background: rgba(96, 239, 255, 0.1); 
  color: var(--secondary); 
  border-radius: 20px; 
  border: 1px solid rgba(96, 239, 255, 0.2);
}

.sub-card {
  padding: 14px 24px;
  display: flex; align-items: center; gap: 14px;
  font-weight: 600; font-size: 1rem;
  z-index: 2;
  white-space: nowrap;
  background: rgba(15, 25, 45, 0.75);
  border-radius: 100px;
}
.icon-ring {
  width: 36px; height: 36px;
  border-radius: 50%;
  background: rgba(96, 239, 255, 0.1);
  display: flex; align-items: center; justify-content: center;
  border: 1px solid rgba(96, 239, 255, 0.3);
}
.sub-card .card-icon { font-size: 1.1rem; color: var(--secondary); }

.card-1 { top: 5%; right: 0%; }
.card-2 { bottom: 15%; left: -10%; }
.card-3 { bottom: 25%; right: -5%; }

/* ================= Stats Section ================= */
.stats-section {
  position: relative;
  z-index: 1;
  padding: 100px 0;
}

.container {
  max-width: 1200px; margin: 0 auto; padding: 0 24px;
}

.stats-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px;
}

.stat-card {
  padding: 40px 24px;
  text-align: center;
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.4s ease;
  border-radius: var(--radius-lg);
}
.stat-card:hover {
  transform: translateY(-12px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5), 0 0 20px rgba(96, 239, 255, 0.15);
  border-color: rgba(96, 239, 255, 0.4);
}

.stat-glow {
  position: absolute;
  top: 0; left: 50%; transform: translateX(-50%);
  width: 80%; height: 100%;
  background: radial-gradient(ellipse at top, rgba(96, 239, 255, 0.1), transparent 70%);
  pointer-events: none;
}

.stat-icon-wrapper {
  width: 70px; height: 70px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, rgba(0,97,255,0.15), rgba(96,239,255,0.15));
  border-radius: 20px;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid rgba(96, 239, 255, 0.2);
  position: relative;
}
.stat-icon-wrapper::after {
  content: ''; position: absolute; inset: -2px;
  border-radius: 22px;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  z-index: -1; opacity: 0; transition: opacity 0.3s;
}
.stat-card:hover .stat-icon-wrapper::after { opacity: 0.3; filter: blur(8px); }

.stat-icon { font-size: 2rem; color: var(--secondary); }
.stat-value { font-size: 3rem; font-weight: 900; margin-bottom: 8px; color: #fff; letter-spacing: -1px; }
.stat-label { font-size: 1.05rem; color: var(--text-muted); font-weight: 500; }

/* ================= Features Section ================= */
.features-section {
  position: relative;
  z-index: 1;
  padding: 120px 0 160px;
}

.section-header { text-align: center; margin-bottom: 80px; }
.section-title { font-size: 3rem; font-weight: 800; margin-bottom: 20px; letter-spacing: -1px; }
.section-subtitle { font-size: 1.2rem; color: var(--text-muted); max-width: 650px; margin: 0 auto; line-height: 1.6; }

.features-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 32px;
}

.feature-card {
  padding: 40px 32px;
  transition: all 0.4s cubic-bezier(0.25, 1, 0.5, 1);
  border-radius: var(--radius-lg);
  display: flex; flex-direction: column;
}

.feature-glow-bg {
  position: absolute;
  width: 150px; height: 150px;
  background: var(--primary);
  filter: blur(80px);
  opacity: 0;
  top: 0; right: 0;
  transition: opacity 0.5s ease;
  pointer-events: none;
}

.feature-card:hover {
  transform: translateY(-12px);
  border-color: rgba(96, 239, 255, 0.4);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5), inset 0 0 30px rgba(96, 239, 255, 0.05);
}

.feature-card:hover .feature-glow-bg { opacity: 0.25; }

.feature-content { position: relative; z-index: 2; }

.feature-icon-box {
  width: 64px; height: 64px;
  border-radius: 18px;
  background: rgba(15, 25, 45, 0.8);
  border: 1px solid rgba(96, 239, 255, 0.3);
  display: flex; align-items: center; justify-content: center;
  font-size: 1.8rem; color: var(--secondary);
  margin-bottom: 28px;
  box-shadow: 0 8px 20px rgba(0,0,0,0.3);
  transition: transform 0.3s, background 0.3s;
}
.feature-card:hover .feature-icon-box {
  transform: scale(1.1) rotate(-5deg);
  background: linear-gradient(135deg, rgba(0,97,255,0.2), rgba(96,239,255,0.3));
}

.feature-title { font-size: 1.4rem; font-weight: 700; margin-bottom: 16px; color: #fff; }
.feature-desc { color: var(--text-muted); font-size: 1.05rem; line-height: 1.7; }

/* ================= 响应式 ================= */
@media (max-width: 1024px) {
  .hero-content { grid-template-columns: 1fr; text-align: center; gap: 40px; }
  .hero-description { margin: 0 auto 40px; }
  .hero-actions { justify-content: center; }
  .hero-visual { margin-top: 40px; height: 450px; }
  .main-card { margin-left: -180px; }
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .features-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .hero-title { font-size: 3.2rem; }
  .hero-visual { display: none; /* 在手机端隐藏复杂的3D卡片以保证性能和排版 */ }
  .stats-grid { grid-template-columns: 1fr; }
  .features-grid { grid-template-columns: 1fr; }
  .top-header { width: 95%; padding: 12px 20px; border-radius: 20px; }
  .logo-text { display: none; }
  .hero-section { padding-top: 120px; min-height: auto; padding-bottom: 60px; }
  .section-title { font-size: 2.2rem; }
}
</style>
