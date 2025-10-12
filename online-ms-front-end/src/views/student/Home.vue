<template>
  <div class="student-home">
    <!-- 英雄区域 -->
    <section class="hero-section">
      <div class="hero-background">
        <div class="background-particles" v-for="i in 20" :key="i" :style="getParticleStyle(i)"></div>
        <div class="background-waves">
          <div class="wave wave-1"></div>
          <div class="wave wave-2"></div>
          <div class="wave wave-3"></div>
        </div>
      </div>
      <div class="hero-content">
        <div class="hero-text">
          <div class="welcome-badge">
            <font-awesome-icon :icon="['fas', 'star']" class="badge-icon" />
            <span>欢迎回来</span>
          </div>
          <h1 class="hero-title">
            你好，<span class="highlight-text">{{ studentName }}</span>！
            <div class="title-decoration"></div>
          </h1>
          <p class="hero-description">
            继续你的学习之旅，在知识的海洋中探索无限可能
          </p>
          <div class="hero-stats">
            <div class="stat-item">
              <div class="stat-number">{{ completedCourses }}</div>
              <div class="stat-label">已完成课程</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ studyHours }}</div>
              <div class="stat-label">学习时长</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ achievements }}</div>
              <div class="stat-label">获得成就</div>
            </div>
          </div>
          <div class="hero-actions">
            <button class="modern-btn primary-btn" @click="continueLearning">
              <div class="btn-content">
                <font-awesome-icon :icon="['fas', 'play']" class="btn-icon" />
                <span>继续学习</span>
              </div>
              <div class="btn-glow"></div>
            </button>
            <button class="modern-btn secondary-btn" @click="exploreCourses">
              <div class="btn-content">
                <font-awesome-icon :icon="['fas', 'compass']" class="btn-icon" />
                <span>探索课程</span>
              </div>
              <div class="btn-glow"></div>
            </button>
          </div>
        </div>
        <div class="hero-visual">
          <div class="learning-dashboard">
            <div class="progress-container">
              <div class="progress-ring">
                <svg class="progress-svg" width="180" height="180">
                  <circle
                    class="progress-bg"
                    cx="90"
                    cy="90"
                    r="75"
                  />
                  <circle
                    class="progress-bar"
                    cx="90"
                    cy="90"
                    r="75"
                    :stroke-dasharray="circumference"
                    :stroke-dashoffset="progressOffset"
                  />
                </svg>
                <div class="progress-center">
                  <div class="progress-percentage">{{ learningProgress }}%</div>
                  <div class="progress-label">总体进度</div>
                </div>
              </div>
            </div>
            <div class="floating-elements">
              <div class="floating-icon icon-1">
                <font-awesome-icon :icon="['fas', 'book']" />
              </div>
              <div class="floating-icon icon-2">
                <font-awesome-icon :icon="['fas', 'graduation-cap']" />
              </div>
              <div class="floating-icon icon-3">
                <font-awesome-icon :icon="['fas', 'trophy']" />
              </div>
              <div class="floating-icon icon-4">
                <font-awesome-icon :icon="['fas', 'lightbulb']" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 快速统计 -->
<!--    <section class="quick-stats-section">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-card modern-card" v-for="stat in quickStats" :key="stat.id">
            <div class="stat-header">
              <div class="stat-icon">
                <font-awesome-icon :icon="stat.icon" />
              </div>
              <div class="stat-trend" :class="stat.trend">
                <font-awesome-icon :icon="stat.trendIcon" />
                <span>{{ stat.change }}</span>
              </div>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
              <div class="stat-description">{{ stat.description }}</div>
            </div>
            <div class="stat-progress">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: stat.progress + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>-->

    <!-- 我的课程 -->
    <section class="my-courses-section">
      <div class="container">
        <div class="section-header">
          <div class="header-content">
            <h2 class="section-title">
              <font-awesome-icon :icon="['fas', 'bookmark']" class="title-icon" />
              我的课程
            </h2>
            <p class="section-description">继续你的学习进度，掌握新技能</p>
          </div>
          <button class="view-all-btn" @click="viewAllCourses">
            <span>查看全部</span>
            <font-awesome-icon :icon="['fas', 'arrow-right']" />
          </button>
        </div>
        <div class="courses-grid">
          <div class="course-card modern-course-card" v-for="course in myCourses" :key="course.id">
            <div class="course-image">
              <img :src="course.image" :alt="course.title" />
              <div class="course-overlay">
                <button class="play-button" @click="continueCourse(course)">
                  <font-awesome-icon :icon="['fas', 'play']" />
                </button>
                <div class="course-progress-overlay">
                  <div class="progress-text">{{ course.progress }}% 完成</div>
                </div>
              </div>
              <div class="course-badge" v-if="course.isNew">
                <span>新课程</span>
              </div>
            </div>
            <div class="course-content">
              <div class="course-meta">
                <span class="course-category">{{ course.category }}</span>
              </div>
              <h3 class="course-title">{{ course.title }}</h3>
              <p class="course-description"><markdownRender :content="course.description" /></p>
              <div class="course-progress">
                <div class="progress-info">
                  <span class="progress-label">学习进度</span>
                  <span class="progress-percentage">{{ course.progress }}%</span>
                </div>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: course.progress + '%' }"></div>
                </div>
              </div>
              <div class="course-stats">
                <div class="stat">
                  <font-awesome-icon :icon="['fas', 'clock']" />
                  <span>{{ course.remainingTime }}</span>
                </div>
                <div class="stat">
                  <font-awesome-icon :icon="['fas', 'video']" />
                  <span>{{ course.totalLessons }}课时</span>
                </div>
                <div class="stat">
                  <font-awesome-icon :icon="['fas', 'certificate']" />
                  <span v-if="course.hasCertificate">可获证书</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 学习路径 -->
<!--    <section class="learning-paths-section">
      <div class="container">
        <div class="section-header">
          <div class="header-content">
            <h2 class="section-title">
              <font-awesome-icon :icon="['fas', 'route']" class="title-icon" />
              学习路径
            </h2>
            <p class="section-description">系统化学习计划，助你成为专业人才</p>
          </div>
          <button class="view-all-btn" @click="viewAllPaths">
            <span>查看全部</span>
            <font-awesome-icon :icon="['fas', 'arrow-right']" />
          </button>
        </div>
        <div class="paths-container">
          <div class="path-card modern-path-card" v-for="path in learningPaths" :key="path.id">
            <div class="path-background">
              <div class="path-pattern"></div>
            </div>
            <div class="path-header">
              <div class="path-icon">
                <font-awesome-icon :icon="path.icon" />
              </div>
              <div class="path-badge" :class="path.difficulty">
                {{ path.difficulty }}
              </div>
            </div>
            <div class="path-content">
              <h3 class="path-title">{{ path.title }}</h3>
              <p class="path-description">{{ path.description }}</p>
              <div class="path-stats">
                <div class="stat">
                  <font-awesome-icon :icon="['fas', 'book']" />
                  <span>{{ path.totalCourses }} 门课程</span>
                </div>
                <div class="stat">
                  <font-awesome-icon :icon="['fas', 'clock']" />
                  <span>{{ path.estimatedTime }}</span>
                </div>
                <div class="stat">
                  <font-awesome-icon :icon="['fas', 'users']" />
                  <span>{{ path.enrolledStudents }} 人在学</span>
                </div>
              </div>
            </div>
            <div class="path-progress">
              <div class="progress-header">
                <span class="progress-label">学习进度</span>
                <span class="progress-percentage">{{ path.progress }}%</span>
              </div>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: path.progress + '%' }"></div>
              </div>
              <div class="progress-details">
                <span>{{ path.completedCourses }}/{{ path.totalCourses }} 课程完成</span>
              </div>
            </div>
            <div class="path-footer">
              <button class="continue-btn" @click="continuePath(path)">
                <font-awesome-icon :icon="['fas', 'play']" />
                <span>{{ path.progress > 0 ? '继续学习' : '开始学习' }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>-->

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
              <font-awesome-icon :icon="feature.icon" />
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-description">{{ feature.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 最新动态 -->
    <section class="news-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">最新动态</h2>
          <p class="section-subtitle">了解平台最新资讯和学习资源</p>
        </div>
        
        <div class="news-grid">
          <div 
            class="news-card neumorphism-card" 
            v-for="news in latestNews" 
            :key="news.id"
            @click="handleNewsClick(news)"
          >
            <div class="news-image">
              <div class="image-placeholder">
                <i :class="news.icon"></i>
              </div>
              <div class="news-category">{{ news.category }}</div>
            </div>
            <div class="news-content">
              <h3 class="news-title">{{ news.title }}</h3>
              <p class="news-summary">{{ news.summary }}</p>
              <div class="news-meta">
                <span class="news-date">
                  <i class="fas fa-calendar-alt"></i>
                  {{ news.date }}
                </span>
                <span class="news-views">
                  <i class="fas fa-eye"></i>
                  {{ news.views }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {getUsername} from "@/utils/tokenAnalysis.js";
import { getCompletedCoursesCount, getTotalLearningTime, getOverallProgress, getMyCourseList } from '@/api/students/stuAPI.js';
import markdownRender from '@/components/markdownRender/NotAIShow.vue'

const router = useRouter()

const featuresSection = ref()

// 学生数据
const studentName = ref('')

// 课程数据
const completedCourses = ref(8)

// 学习时长
const studyHours = ref(156)
const achievements = ref(12)
const learningProgress = ref(68)



// 快速统计
const quickStats = ref([
  {
    id: 1,
    icon: ['fas', 'book-open'],
    value: '8',
    label: '进行中课程',
    description: '本周新增 2 门',
    progress: 75,
    trend: 'up',
    trendIcon: ['fas', 'arrow-up'],
    change: '+25%'
  },
  {
    id: 2,
    icon: ['fas', 'clock'],
    value: '24h',
    label: '本周学习',
    description: '超过 80% 同学',
    progress: 80,
    trend: 'up',
    trendIcon: ['fas', 'arrow-up'],
    change: '+12%'
  },
  {
    id: 3,
    icon: ['fas', 'trophy'],
    value: '12',
    label: '获得成就',
    description: '本月新增 3 个',
    progress: 60,
    trend: 'up',
    trendIcon: ['fas', 'arrow-up'],
    change: '+33%'
  },
  {
    id: 4,
    icon: ['fas', 'star'],
    value: '4.8',
    label: '平均评分',
    description: '课程完成质量',
    progress: 96,
    trend: 'stable',
    trendIcon: ['fas', 'minus'],
    change: '0%'
  }
])

// 我的课程
const myCourses = ref([])

// 学习路径
const learningPaths = ref([
  {
    id: 1,
    title: '前端开发工程师',
    description: '从零基础到前端专家的完整学习路径，掌握现代前端开发技术栈',
    icon: ['fas', 'laptop-code'],
    totalCourses: 12,
    completedCourses: 4,
    estimatedTime: '6个月',
    enrolledStudents: 5432,
    progress: 35,
    difficulty: '中级'
  },
  {
    id: 2,
    title: '全栈开发工程师',
    description: '掌握前后端开发技术，成为全栈工程师，具备完整项目开发能力',
    icon: ['fas', 'code'],
    totalCourses: 18,
    completedCourses: 0,
    estimatedTime: '9个月',
    enrolledStudents: 3210,
    progress: 0,
    difficulty: '高级'
  },
  {
    id: 3,
    title: '数据科学家',
    description: '学习数据分析和机器学习，成为数据专家，掌握AI时代核心技能',
    icon: ['fas', 'chart-line'],
    totalCourses: 15,
    completedCourses: 9,
    estimatedTime: '8个月',
    enrolledStudents: 2187,
    progress: 60,
    difficulty: '高级'
  }
])

// 功能特色
const features = ref([
  {
    id: 1,
    icon: 'fas fa-video text-primary',
    title: '高清视频教学',
    description: '4K高清视频，支持倍速播放，随时随地学习'
  },
  {
    id: 2,
    icon: 'fas fa-comments text-secondary',
    title: '互动式学习',
    description: '实时问答、作业批改，与讲师和同学深度交流'
  },
  {
    id: 3,
    icon: 'fas fa-mobile-alt text-primary',
    title: '多端同步',
    description: '支持手机、平板、电脑，学习进度云端同步'
  },
  {
    id: 4,
    icon: 'fas fa-certificate text-secondary',
    title: '权威认证',
    description: '完成课程获得行业认可的专业技能证书'
  },
  {
    id: 5,
    icon: 'fas fa-clock text-primary',
    title: '灵活学习',
    description: '自主安排学习时间，支持离线下载观看'
  },
  {
    id: 6,
    icon: 'fas fa-headset text-secondary',
    title: '专业服务',
    description: '7×24小时客服支持，学习路上不孤单'
  }
])

// 最新动态
const latestNews = ref([
  {
    id: 1,
    title: '平台新增AI编程助手功能',
    summary: '智能代码补全和错误检测，让编程学习更高效',
    category: '功能更新',
    date: '2024-01-15',
    views: 1250,
    icon: 'fas fa-robot text-primary'
  },
  {
    id: 2,
    title: '春季课程优惠活动开启',
    summary: '精选课程限时5折，更有免费试学机会',
    category: '活动公告',
    date: '2024-01-12',
    views: 2100,
    icon: 'fas fa-gift text-secondary'
  },
  {
    id: 3,
    title: '新增移动端学习APP',
    summary: '随时随地学习，支持离线下载和学习提醒',
    category: '产品发布',
    date: '2024-01-10',
    views: 1800,
    icon: 'fas fa-mobile-alt text-primary'
  }
])

// 计算属性
const circumference = computed(() => {
  return 2 * Math.PI * 75
})

const progressOffset = computed(() => {
  return circumference.value - (learningProgress.value / 100) * circumference.value
})

// 方法
const getParticleStyle = (index) => {
  const colors = ['#3b82f6', '#8b5cf6', '#10b981', '#f59e0b', '#ef4444']
  const size = Math.random() * 6 + 2
  const left = Math.random() * 100
  const animationDelay = Math.random() * 20
  const animationDuration = Math.random() * 10 + 10
  
  return {
    position: 'absolute',
    left: `${left}%`,
    top: `${Math.random() * 100}%`,
    width: `${size}px`,
    height: `${size}px`,
    backgroundColor: colors[index % colors.length],
    borderRadius: '50%',
    opacity: 0.6,
    animation: `float ${animationDuration}s ease-in-out infinite`,
    animationDelay: `${animationDelay}s`
  }
}

const continueLearning = () => {
  // 继续学习最近的课程
  const recentCourse = myCourses.value.find(course => course.progress > 0 && course.progress < 100)
  if (recentCourse) {
    continueCourse(recentCourse)
  } else {
    ElMessage.info('暂无进行中的课程')
  }
}

const exploreCourses = () => {
  router.push('/index/courses')
  ElMessage.success('即将跳转到课程页面')
}

const viewAllCourses = () => {
  router.push('/index/my-courses')
}

const viewAllPaths = () => {
  ElMessage.info('查看所有学习路径')
}

const continueCourse = (course) => {
  ElMessage.info(`继续学习：${course.title}`)
}

const continuePath = (path) => {
  ElMessage.info(`${path.progress > 0 ? '继续' : '开始'}学习路径：${path.title}`)
}

const scrollToFeatures = () => {
  featuresSection.value?.scrollIntoView({ behavior: 'smooth' })
}

const handleNewsClick = (news) => {
  ElMessage.info(`查看详情：${news.title}`)
}

async function stuName() {
  try {
    studentName.value = await getUsername();
  } catch (error) {
    console.error('获取用户名失败:', error);
    ElMessage.error('获取用户名失败，请重新登录');
  }
}

async function loadCompletedCoursesCount() {
  try {
    const count = await getCompletedCoursesCount();
    completedCourses.value = count;
  } catch (error) {
    console.error('获取已完成课程数量失败:', error);
    ElMessage.error('获取已完成课程数量失败');
  }
}

// 加载总学习时长
async function loadTotalLearningTime() {
  try {
    const result = await getTotalLearningTime();
    const totalSeconds = result.totalSeconds || 0;
    const totalMinutes = result.totalMinutes || 0;
    
    // 计算小时数
    const hours = Math.floor(totalSeconds / 3600);
    
    // 根据规则格式化显示
    if (hours > 15) {
      studyHours.value = `${hours}h`;
    } else if (totalMinutes > 125) {
      studyHours.value = `${totalMinutes}min`;
    } else {
      studyHours.value = `${totalSeconds}s`;
    }
  } catch (error) {
    console.error('获取总学习时长失败:', error);
    // 学习时长获取失败不显示错误消息，保持默认值
  }
}

// 加载总体学习进度
async function loadOverallProgress() {
  try {
    const progress = await getOverallProgress();
    if (progress !== null && progress !== undefined) {
      learningProgress.value = Math.round(progress); // 四舍五入到整数
    }
  } catch (error) {
    console.error('获取总体学习进度失败:', error);
    learningProgress.value = 0; // 默认值
  }
}

// 加载我的课程列表
async function loadMyCourseList() {
  try {
    const courseList = await getMyCourseList();
    if (courseList && Array.isArray(courseList)) {
      // 将后端数据转换为前端需要的格式
      myCourses.value = courseList.map(course => ({
        id: course.courseId,
        title: course.courseTitle,
        description: course.courseDescription,
        image: course.courseCover || '/images/courses/default.jpg', // 使用课程封面或默认图片
        category: course.categoryName, // 课程类别
        progress: course.progressPercentage || 0, // 学习进度百分比
        remainingTime: course.remainingTime||'待计算', // 剩余学习时间
        totalLessons: course.totalSections || 0, // 课程总课时数
        hasCertificate: false, // 后端暂无此字段，使用默认值 - 是否有证书
        isNew: false, // 后端暂无此字段，使用默认值 - 是否为新课程
        teacherName: course.teacherName,
        courseStatus: course.courseStatus,
        createTime: course.createTime,
        enrollmentDate: course.enrollmentDate
      }));
    }
  } catch (error) {
    console.error('获取我的课程列表失败:', error);
    ElMessage.error('获取我的课程列表失败');
  }
}

onMounted(async () => {
  // 页面加载动画或其他初始化逻辑
  await stuName();
  await loadCompletedCoursesCount();
  await loadTotalLearningTime();
  await loadOverallProgress();
  await loadMyCourseList();
})
</script>

<style scoped>
/* 全局样式变量 */
:root {
  --primary-color: #002FA7;
  --secondary-color: #517B4D;
  --success-color: #67C23A;
  --warning-color: #E6A23C;
  --danger-color: #F56C6C;
  --info-color: #909399;
  --background-color: #F0F0F3;
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-muted: #909399;
  --border-color: #DCDFE6;
  --surface-color: rgba(240, 240, 243, 0.8);
  --glass-bg: rgba(255, 255, 255, 0.25);
  --glass-border: rgba(255, 255, 255, 0.18);
  --spacing-xs: 0.25rem;
  --spacing-sm: 0.5rem;
  --spacing-md: 1rem;
  --spacing-lg: 1.5rem;
  --spacing-xl: 2rem;
  --spacing-2xl: 3rem;
  --border-radius-sm: 8px;
  --border-radius-md: 12px;
  --border-radius-lg: 16px;
  --border-radius-xl: 24px;
  --transition-fast: 0.15s ease;
  --transition-normal: 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-slow: 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.student-home {
  min-height: 100vh;
  background: linear-gradient(135deg, #F0F0F3 0%, #E8F4FD 50%, #F0F0F3 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  position: relative;
  overflow-x: hidden;
}

.student-home::before {
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

/* 英雄区域 */
.hero-section {
  padding: var(--spacing-2xl) 0;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  z-index: 0;
}

.background-particles {
  position: absolute;
  border-radius: 50%;
  opacity: 0.6;
  animation: float 15s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  25% { transform: translateY(-20px) rotate(90deg); }
  50% { transform: translateY(-10px) rotate(180deg); }
  75% { transform: translateY(-30px) rotate(270deg); }
}

.background-waves {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 200%;
  height: 100px;
  background: linear-gradient(90deg, 
    rgba(0, 47, 167, 0.1) 0%, 
    rgba(81, 123, 77, 0.1) 50%, 
    rgba(0, 47, 167, 0.1) 100%);
  border-radius: 50% 50% 0 0;
  animation: wave 6s ease-in-out infinite;
}

.wave-1 {
  animation-delay: 0s;
  opacity: 0.3;
}

.wave-2 {
  animation-delay: 2s;
  opacity: 0.2;
  height: 80px;
}

.wave-3 {
  animation-delay: 4s;
  opacity: 0.1;
  height: 60px;
}

@keyframes wave {
  0%, 100% { transform: translateX(-50%) translateY(0px); }
  50% { transform: translateX(-50%) translateY(-20px); }
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-2xl);
  align-items: center;
  min-height: 70vh;
  position: relative;
  z-index: 1;
}

.hero-text {
  position: relative;
  z-index: 2;
}

.welcome-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius-xl);
  color: var(--primary-color);
  font-size: 0.875rem;
  font-weight: 500;
  margin-bottom: var(--spacing-lg);
  box-shadow: 
    4px 4px 8px rgba(0, 47, 167, 0.1),
    -2px -2px 4px rgba(255, 255, 255, 0.3);
  transition: all var(--transition-normal);
}

.welcome-badge:hover {
  transform: translateY(-2px);
  box-shadow: 
    6px 6px 12px rgba(0, 47, 167, 0.15),
    -3px -3px 6px rgba(255, 255, 255, 0.4);
}

.badge-icon {
  color: #E6A23C;
  animation: sparkle 2s ease-in-out infinite;
}

@keyframes sparkle {
  0%, 100% { transform: scale(1) rotate(0deg); }
  50% { transform: scale(1.1) rotate(180deg); }
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
  line-height: 1.1;
  position: relative;
}

.highlight-text {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.title-decoration {
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 60px;
  height: 4px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 2px;
  animation: expandWidth 2s ease-out;
}

@keyframes expandWidth {
  0% { width: 0; }
  100% { width: 60px; }
}

.hero-description {
  font-size: 1.25rem;
  color: var(--text-secondary);
  margin-bottom: var(--spacing-xl);
  line-height: 1.6;
  font-weight: 400;
}

.hero-stats {
  display: flex;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.stat-item {
  text-align: center;
  padding: var(--spacing-md);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: var(--border-radius-lg);
  box-shadow: 
    inset 2px 2px 4px rgba(255, 255, 255, 0.1),
    inset -2px -2px 4px rgba(0, 47, 167, 0.05),
    4px 4px 8px rgba(0, 47, 167, 0.1);
  transition: all var(--transition-normal);
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 
    inset 2px 2px 4px rgba(255, 255, 255, 0.15),
    inset -2px -2px 4px rgba(0, 47, 167, 0.08),
    6px 6px 12px rgba(0, 47, 167, 0.15);
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: var(--spacing-xs);
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.hero-actions {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
}

.modern-btn {
  position: relative;
  padding: var(--spacing-md) var(--spacing-xl);
  font-size: 1rem;
  font-weight: 600;
  border: none;
  border-radius: var(--border-radius-xl);
  cursor: pointer;
  transition: all var(--transition-normal);
  overflow: hidden;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 160px;
  height: 48px;
}

.primary-btn {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  box-shadow: 
    8px 8px 16px rgba(0, 47, 167, 0.2),
    -4px -4px 8px rgba(255, 255, 255, 0.1);
}

.primary-btn:hover {
  transform: translateY(-3px);
  box-shadow: 
    12px 12px 24px rgba(0, 47, 167, 0.3),
    -6px -6px 12px rgba(255, 255, 255, 0.2);
}

.primary-btn:active {
  transform: translateY(-1px);
  box-shadow: 
    4px 4px 8px rgba(0, 47, 167, 0.3),
    -2px -2px 4px rgba(255, 255, 255, 0.1);
}

.secondary-btn {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  color: var(--text-primary);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 
    8px 8px 16px rgba(0, 47, 167, 0.1),
    -4px -4px 8px rgba(255, 255, 255, 0.3);
}

.secondary-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-3px);
  box-shadow: 
    12px 12px 24px rgba(0, 47, 167, 0.15),
    -6px -6px 12px rgba(255, 255, 255, 0.4);
}

.secondary-btn:active {
  transform: translateY(-1px);
  box-shadow: 
    inset 4px 4px 8px rgba(0, 47, 167, 0.1),
    inset -2px -2px 4px rgba(255, 255, 255, 0.2);
}

.btn-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  position: relative;
  z-index: 2;
}

.btn-icon {
  font-size: 1rem;
  transition: transform var(--transition-normal);
}

.modern-btn:hover .btn-icon {
  transform: scale(1.1);
}

.btn-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2) 0%, transparent 100%);
  opacity: 0;
  transition: opacity var(--transition-normal);
  border-radius: inherit;
}

.modern-btn:hover .btn-glow {
  opacity: 1;
}

.hero-visual {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.learning-dashboard {
  position: relative;
  width: 360px;
  height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-container {
  position: relative;
  z-index: 2;
}

.progress-ring {
  position: relative;
}

.progress-svg {
  transform: rotate(-90deg);
  filter: drop-shadow(4px 4px 8px rgba(0, 47, 167, 0.2));
}

.progress-bg {
  fill: none;
  stroke: rgba(255, 255, 255, 0.1);
  stroke-width: 8;
}

.progress-bar {
  fill: none;
  stroke: url(#progressGradient);
  stroke-width: 8;
  stroke-linecap: round;
  transition: stroke-dashoffset 2s ease-in-out;
}

.progress-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  width: 120px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 
    inset 4px 4px 8px rgba(255, 255, 255, 0.1),
    inset -4px -4px 8px rgba(0, 47, 167, 0.05),
    8px 8px 16px rgba(0, 47, 167, 0.1);
}

.progress-percentage {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: var(--spacing-xs);
}

.progress-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.floating-elements {
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.floating-icon {
  position: absolute;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    8px 8px 16px rgba(0, 47, 167, 0.1),
    -4px -4px 8px rgba(255, 255, 255, 0.3);
  animation: floatOrbit 20s linear infinite;
  transition: all var(--transition-normal);
}

.floating-icon:hover {
  transform: scale(1.1);
  box-shadow: 
    12px 12px 24px rgba(0, 47, 167, 0.15),
    -6px -6px 12px rgba(255, 255, 255, 0.4);
}

.floating-icon svg {
  font-size: 1.5rem;
  color: var(--primary-color);
  transition: all var(--transition-normal);
}

.icon-1 {
  top: 15%;
  left: 50%;
  transform: translateX(-50%);
  animation-delay: 0s;
}

.icon-1 svg {
  color: #67C23A;
}

.icon-2 {
  top: 50%;
  right: 15%;
  transform: translateY(-50%);
  animation-delay: 5s;
}

.icon-2 svg {
  color: var(--primary-color);
}

.icon-3 {
  bottom: 15%;
  left: 50%;
  transform: translateX(-50%);
  animation-delay: 10s;
}

.icon-3 svg {
  color: #E6A23C;
}

.icon-4 {
  top: 50%;
  left: 15%;
  transform: translateY(-50%);
  animation-delay: 15s;
}

.icon-4 svg {
  color: #F56C6C;
}

@keyframes floatOrbit {
  0% {
    transform: rotate(0deg) translateX(140px) rotate(0deg);
  }
  100% {
    transform: rotate(360deg) translateX(140px) rotate(-360deg);
  }
}

/* 快速统计区域 */
.quick-stats-section {
  padding: var(--spacing-2xl) 0;
  position: relative;
  z-index: 1;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--spacing-lg);
}

.stat-card {
  padding: var(--spacing-xl);
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--border-radius-xl);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
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

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 
    16px 16px 32px rgba(0, 47, 167, 0.15),
    -8px -8px 16px rgba(255, 255, 255, 0.3);
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-lg);
  position: relative;
  z-index: 1;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--border-radius-lg);
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: var(--primary-color);
  box-shadow: 
    inset 2px 2px 4px rgba(255, 255, 255, 0.1),
    inset -2px -2px 4px rgba(0, 47, 167, 0.05);
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-md);
  font-size: 0.75rem;
  font-weight: 600;
}

.stat-trend.up {
  background: rgba(103, 194, 58, 0.1);
  color: #67C23A;
}

.stat-trend.down {
  background: rgba(245, 108, 108, 0.1);
  color: #F56C6C;
}

.stat-trend.stable {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.stat-content {
  position: relative;
  z-index: 1;
}

.stat-number {
  font-size: 2.5rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
  line-height: 1;
}

.stat-label {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.stat-description {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: var(--spacing-lg);
}

.stat-progress {
  position: relative;
  z-index: 1;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
  box-shadow: inset 2px 2px 4px rgba(0, 47, 167, 0.1);
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 3px;
  transition: width 1.5s ease-out;
  box-shadow: 0 0 8px rgba(0, 47, 167, 0.3);
}

/* 统计数据 */
.stats-section {
  padding: var(--spacing-xl) 0;
  background: var(--surface-color);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-lg);
}

.stat-item {
  padding: var(--spacing-lg);
  text-align: center;
  border-radius: var(--border-radius-lg);
  transition: all var(--transition-normal);
}

.stat-item:hover {
  transform: translateY(-4px);
}

.stat-icon {
  margin-bottom: var(--spacing-md);
}

.stat-icon i {
  font-size: 2rem;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

/* 通用区域样式 */
.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
}

.section-header {
  text-align: center;
  margin-bottom: var(--spacing-4xl);
  position: relative;
  z-index: 10;
}

.section-header::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(0, 47, 167, 0.05) 0%, transparent 70%);
  border-radius: 50%;
  z-index: -1;
}

.section-title {
  font-size: 2.75rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
  position: relative;
  display: inline-block;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 2px;
  box-shadow: 0 2px 8px rgba(0, 47, 167, 0.3);
}

.section-subtitle {
  font-size: 1.25rem;
  color: var(--text-secondary);
  max-width: 700px;
  margin: 0 auto var(--spacing-lg);
  line-height: 1.6;
  font-weight: 500;
}

.view-all-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-xl);
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  color: var(--primary-color);
  text-decoration: none;
  border-radius: var(--border-radius-lg);
  font-weight: 600;
  font-size: 1rem;
  transition: all var(--transition-normal);
  box-shadow: 
    8px 8px 16px rgba(0, 47, 167, 0.1),
    -4px -4px 8px rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
  margin-top: var(--spacing-lg);
  z-index: 15;
}

.view-all-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s ease;
}

.view-all-btn:hover {
  transform: translateY(-4px);
  box-shadow: 
    12px 12px 24px rgba(0, 47, 167, 0.15),
    -6px -6px 12px rgba(255, 255, 255, 0.4);
  color: var(--primary-color);
}

.view-all-btn:hover::before {
  left: 100%;
}

.view-all-btn:active {
  transform: translateY(-2px);
}

/* 我的课程 */
.my-courses-section {
  padding: var(--spacing-4xl) 0;
  position: relative;
}

.my-courses-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.02) 0%, 
    transparent 50%, 
    rgba(0, 47, 167, 0.02) 100%);
  pointer-events: none;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
  gap: var(--spacing-2xl);
}

.course-card {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--border-radius-xl);
  overflow: hidden;
  transition: all var(--transition-normal);
  position: relative;
  cursor: pointer;
  z-index: 1;
  box-shadow: 
    12px 12px 24px rgba(0, 47, 167, 0.08),
    -6px -6px 12px rgba(255, 255, 255, 0.25);
}

.course-card::before {
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
  z-index: 0;
}

.course-card:hover {
  transform: translateY(-12px);
  z-index: 5;
  box-shadow: 
    20px 20px 40px rgba(0, 47, 167, 0.15),
    -10px -10px 20px rgba(255, 255, 255, 0.35);
}

.course-image {
  position: relative;
  height: 220px;
  overflow: hidden;
  border-radius: var(--border-radius-lg) var(--border-radius-lg) 0 0;
}

.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-normal);
}

.course-card:hover .course-image img {
  transform: scale(1.05);
}

.course-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(0, 47, 167, 0.7) 0%, 
    rgba(81, 123, 77, 0.7) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all var(--transition-normal);
  z-index: 2;
}

.course-card:hover .course-overlay {
  opacity: 1;
}

.play-button {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: var(--primary-color);
  font-size: 1.5rem;
  cursor: pointer;
  transition: all var(--transition-normal);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    8px 8px 16px rgba(0, 47, 167, 0.2),
    -4px -4px 8px rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 3;
}

.play-button:hover {
  transform: scale(1.1);
  background: rgba(255, 255, 255, 1);
  box-shadow: 
    12px 12px 24px rgba(0, 47, 167, 0.3),
    -6px -6px 12px rgba(255, 255, 255, 0.4);
}

.play-button:active {
  transform: scale(0.95);
}

.course-progress-overlay {
  position: absolute;
  bottom: var(--spacing-lg);
  left: var(--spacing-lg);
  right: var(--spacing-lg);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius-md);
  text-align: center;
  z-index: 3;
}

.progress-text {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--primary-color);
}

.course-badge {
  position: absolute;
  top: var(--spacing-md);
  right: var(--spacing-md);
  background: linear-gradient(135deg, #E6A23C, #F56C6C);
  color: white;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-md);
  font-size: 0.75rem;
  font-weight: 600;
  z-index: 2;
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.3);
}

.course-content {
  padding: var(--spacing-xl);
  position: relative;
  z-index: 1;
}

.course-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.course-category {
  background: rgba(0, 47, 167, 0.1);
  color: var(--primary-color);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-sm);
  font-size: 0.75rem;
  font-weight: 600;
}

.course-level {
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-sm);
  font-size: 0.75rem;
  font-weight: 600;
}

.course-level.初级 {
  background: rgba(103, 194, 58, 0.1);
  color: #67C23A;
}

.course-level.中级 {
  background: rgba(230, 162, 60, 0.1);
  color: #E6A23C;
}

.course-level.高级 {
  background: rgba(245, 108, 108, 0.1);
  color: #F56C6C;
}

.course-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  line-height: 1.3;
}

.course-description {
  color: var(--text-secondary);
  font-size: 1rem;
  line-height: 1.5;
  margin-bottom: var(--spacing-lg);
}

.course-progress {
  margin-bottom: var(--spacing-lg);
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
  font-size: 0.875rem;
}

.progress-label {
  color: var(--text-secondary);
  font-weight: 500;
}

.progress-percentage {
  color: var(--primary-color);
  font-weight: 700;
}

.course-stats {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.course-stats .stat {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.course-stats .stat svg {
  color: var(--primary-color);
  font-size: 0.875rem;
}

/* 学习路径 */
.learning-paths-section {
  padding: var(--spacing-4xl) 0;
  position: relative;
}

.learning-paths-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(0, 47, 167, 0.02) 0%, 
    transparent 50%, 
    rgba(255, 255, 255, 0.02) 100%);
  pointer-events: none;
}

.paths-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: var(--spacing-2xl);
}

.path-card {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--border-radius-xl);
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 420px;
  z-index: 1;
  box-shadow: 
    12px 12px 24px rgba(0, 47, 167, 0.08),
    -6px -6px 12px rgba(255, 255, 255, 0.25);
}

.path-card::before {
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
  z-index: 0;
}

.path-card:hover {
  transform: translateY(-12px);
  z-index: 5;
  box-shadow: 
    20px 20px 40px rgba(0, 47, 167, 0.15),
    -10px -10px 20px rgba(255, 255, 255, 0.35);
}

.path-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

.path-pattern {
  position: absolute;
  top: -50%;
  right: -20%;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(0, 47, 167, 0.05) 0%, transparent 70%);
  border-radius: 50%;
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

.path-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: var(--spacing-xl) var(--spacing-xl) 0;
  margin-bottom: var(--spacing-lg);
  position: relative;
  z-index: 1;
}

.path-badge {
  padding: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--border-radius-md);
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.path-badge.初级 {
  background: rgba(103, 194, 58, 0.15);
  color: #67C23A;
  border: 1px solid rgba(103, 194, 58, 0.3);
}

.path-badge.中级 {
  background: rgba(230, 162, 60, 0.15);
  color: #E6A23C;
  border: 1px solid rgba(230, 162, 60, 0.3);
}

.path-badge.高级 {
  background: rgba(245, 108, 108, 0.15);
  color: #F56C6C;
  border: 1px solid rgba(245, 108, 108, 0.3);
}

.path-content {
  flex: 1;
  padding: 0 var(--spacing-xl);
  position: relative;
  z-index: 1;
}

.path-title {
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  line-height: 1.3;
}

.path-description {
  color: var(--text-secondary);
  font-size: 1rem;
  line-height: 1.6;
  margin-bottom: var(--spacing-xl);
}

.path-stats {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.path-stats .stat {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 0.875rem;
  color: var(--text-secondary);
  padding: var(--spacing-sm) var(--spacing-md);
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--border-radius-md);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.path-stats .stat svg {
  color: var(--primary-color);
  font-size: 1rem;
}

.path-progress {
  padding: 0 var(--spacing-xl) var(--spacing-lg);
  position: relative;
  z-index: 1;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
  font-size: 0.875rem;
}

.progress-details {
  margin-top: var(--spacing-sm);
  font-size: 0.75rem;
  color: var(--text-secondary);
  text-align: center;
}

.path-footer {
  padding: var(--spacing-lg) var(--spacing-xl) var(--spacing-xl);
  position: relative;
  z-index: 1;
}

.continue-btn {
  width: 100%;
  padding: var(--spacing-md) var(--spacing-lg);
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  border: none;
  border-radius: var(--border-radius-lg);
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-normal);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  box-shadow: 
    8px 8px 16px rgba(0, 47, 167, 0.2),
    -4px -4px 8px rgba(255, 255, 255, 0.1);
  position: relative;
  overflow: hidden;
}

.continue-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s ease;
}

.continue-btn:hover {
  transform: translateY(-3px);
  box-shadow: 
    12px 12px 24px rgba(0, 47, 167, 0.3),
    -6px -6px 12px rgba(255, 255, 255, 0.2);
}

.continue-btn:hover::before {
  left: 100%;
}

.continue-btn:active {
  transform: translateY(-1px);
  box-shadow: 
    4px 4px 8px rgba(0, 47, 167, 0.3),
    -2px -2px 4px rgba(255, 255, 255, 0.1);
}

.continue-btn svg {
  font-size: 1rem;
  transition: transform var(--transition-normal);
}

.continue-btn:hover svg {
  transform: scale(1.1);
}

.path-header {
  display: flex;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
  position: relative;
  z-index: 1;
}

.path-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--border-radius-lg);
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 
    inset 2px 2px 4px rgba(255, 255, 255, 0.1),
    inset -2px -2px 4px rgba(0, 47, 167, 0.05);
}

.path-icon i {
  font-size: 1.5rem;
  color: var(--primary-color);
}

.path-info {
  flex: 1;
  position: relative;
  z-index: 1;
}

.path-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
  line-height: 1.3;
}

.path-description {
  color: var(--text-secondary);
  font-size: 1rem;
  line-height: 1.5;
  margin-bottom: var(--spacing-lg);
}

.path-stats {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
  position: relative;
  z-index: 1;
}

.path-stat {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 0.875rem;
  color: var(--text-secondary);
  padding: var(--spacing-xs) var(--spacing-sm);
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--border-radius-md);
  backdrop-filter: blur(5px);
}

.path-stat i {
  color: var(--primary-color);
}

.path-progress {
  margin-top: auto;
  position: relative;
  z-index: 1;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
  font-size: 0.875rem;
}

.progress-label {
  color: var(--text-secondary);
  font-weight: 500;
}

.progress-value {
  color: var(--primary-color);
  font-weight: 700;
  font-size: 1rem;
}

.path-progress-bar {
  width: 100%;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
  box-shadow: inset 2px 2px 4px rgba(0, 47, 167, 0.1);
}

.path-progress-fill {
  height: 100%;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 4px;
  transition: width 1.5s ease-out;
  box-shadow: 0 0 8px rgba(0, 47, 167, 0.3);
}

/* 功能特色 */
.features-section {
  padding: var(--spacing-4xl) 0;
  position: relative;
}

.features-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.02) 0%, 
    transparent 50%, 
    rgba(0, 47, 167, 0.02) 100%);
  pointer-events: none;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: var(--spacing-xl);
}

.feature-card {
  padding: var(--spacing-xl);
  text-align: center;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--border-radius-xl);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.feature-card::before {
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

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 
    16px 16px 32px rgba(0, 47, 167, 0.15),
    -8px -8px 16px rgba(255, 255, 255, 0.3);
}

.feature-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--spacing-lg);
  position: relative;
  z-index: 1;
  box-shadow: 
    inset 2px 2px 4px rgba(255, 255, 255, 0.1),
    inset -2px -2px 4px rgba(0, 47, 167, 0.05);
}

.feature-icon i {
  font-size: 1.75rem;
  color: var(--primary-color);
}

.feature-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  position: relative;
  z-index: 1;
}

.feature-description {
  color: var(--text-secondary);
  line-height: 1.6;
  font-size: 1rem;
  position: relative;
  z-index: 1;
}

/* 最新动态 */
.news-section {
  padding: var(--spacing-4xl) 0;
  position: relative;
}

.news-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(0, 47, 167, 0.02) 0%, 
    transparent 50%, 
    rgba(255, 255, 255, 0.02) 100%);
  pointer-events: none;
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
  gap: var(--spacing-xl);
}

.news-card {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--border-radius-xl);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;
}

.news-card::before {
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
  z-index: 0;
}

.news-card:hover {
  transform: translateY(-8px);
  box-shadow: 
    16px 16px 32px rgba(0, 47, 167, 0.15),
    -8px -8px 16px rgba(255, 255, 255, 0.3);
}

.news-image {
  height: 180px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.image-placeholder {
  position: relative;
  z-index: 1;
}

.image-placeholder i {
  font-size: 3.5rem;
  color: white;
  opacity: 0.9;
}

.news-category {
  position: absolute;
  top: var(--spacing-lg);
  right: var(--spacing-lg);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius-md);
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--primary-color);
  z-index: 2;
  box-shadow: 0 2px 8px rgba(0, 47, 167, 0.1);
}

.news-content {
  padding: var(--spacing-xl);
  position: relative;
  z-index: 1;
}

.news-title {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  line-height: 1.4;
}

.news-summary {
  color: var(--text-secondary);
  font-size: 1rem;
  line-height: 1.6;
  margin-bottom: var(--spacing-lg);
}

.news-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
  color: var(--text-secondary);
  padding-top: var(--spacing-md);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.news-date,
.news-views {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-weight: 500;
}

.news-date i,
.news-views i {
  color: var(--primary-color);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .container {
    max-width: 1200px;
    padding: 0 var(--spacing-lg);
  }
  
  .courses-grid {
    grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
    gap: var(--spacing-xl);
  }
  
  .paths-container {
    grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
    gap: var(--spacing-xl);
  }
  
  .features-grid {
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  }
  
  .news-grid {
    grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  }
}

@media (max-width: 768px) {
  .hero-content {
    grid-template-columns: 1fr;
    text-align: center;
    gap: var(--spacing-xl);
  }
  
  .hero-title {
    font-size: 2.25rem;
  }
  
  .section-title {
    font-size: 2.25rem;
  }
  
  .hero-actions {
    justify-content: center;
    flex-wrap: wrap;
    gap: var(--spacing-md);
  }
  
  .learning-dashboard {
    width: 280px;
    height: 280px;
  }
  
  .progress-ring {
    width: 200px;
    height: 200px;
  }
  
  .courses-grid,
  .paths-container {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg);
  }
  
  .course-image {
    height: 180px;
  }
  
  .path-card {
    min-height: 380px;
  }
  
  .path-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: var(--spacing-md);
  }
  
  .path-stats {
    justify-content: center;
    gap: var(--spacing-md);
  }
  
  .features-grid {
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  }
  
  .news-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-md);
  }
  
  .floating-icon {
    width: 48px;
    height: 48px;
  }
  
  .floating-icon svg {
    font-size: 1.25rem;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 var(--spacing-md);
  }
  
  .hero-title {
    font-size: 1.875rem;
  }
  
  .section-title {
    font-size: 1.875rem;
  }
  
  .hero-description {
    font-size: 1rem;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .hero-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .modern-btn {
    width: 100%;
    justify-content: center;
  }
  
  .learning-dashboard {
    width: 240px;
    height: 240px;
  }
  
  .progress-ring {
    width: 160px;
    height: 160px;
  }
  
  .courses-grid,
  .paths-container {
    gap: var(--spacing-md);
  }
  
  .course-image {
    height: 160px;
  }
  
  .course-content,
  .path-content {
    padding: var(--spacing-lg);
  }
  
  .path-card {
    min-height: 360px;
  }
  
  .path-footer {
    padding: var(--spacing-md) var(--spacing-lg) var(--spacing-lg);
  }
  
  .play-button {
    width: 60px;
    height: 60px;
    font-size: 1.25rem;
  }
  
  .continue-btn {
    padding: var(--spacing-sm) var(--spacing-md);
    font-size: 0.875rem;
  }
  
  .feature-card,
  .news-card {
    padding: var(--spacing-lg);
  }
  
  .floating-elements {
    display: none;
  }
  
  .section-header::before {
    width: 80px;
    height: 80px;
  }
  
  .path-header {
    flex-direction: column;
    text-align: center;
    gap: var(--spacing-md);
  }
  
  .path-stats {
    justify-content: center;
    gap: var(--spacing-sm);
  }
  
  .path-stats .stat {
    padding: var(--spacing-xs) var(--spacing-sm);
    font-size: 0.75rem;
  }
}
</style>