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
<!--            <div class="stat-item">
              <div class="stat-number">{{ achievements }}</div>
              <div class="stat-label">获得成就</div>
            </div>-->
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
              <div class="course-progress-section">
                <div class="progress-info-compact">
                  <span class="progress-label-compact">已完成 {{ course.progress }}%</span>
                  <span class="next-lesson-hint" v-if="course.progress < 100">继续学习</span>
                  <span class="next-lesson-hint completed" v-else>已完成</span>
                </div>
                <div class="progress-bar-bold">
                  <div class="progress-fill-bold" :style="{ width: course.progress + '%' }"></div>
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
import gsap from 'gsap';

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
    if (hours > 0) {
      studyHours.value = `${(totalSeconds / 3600).toFixed(1)} 小时`;
    } else if (totalMinutes > 0) {
      studyHours.value = `${totalMinutes} 分钟`;
    } else {
      studyHours.value = `${totalSeconds} 秒`;
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
  // 页面数据加载
  await stuName();
  await loadCompletedCoursesCount();
  await loadTotalLearningTime();
  await loadOverallProgress();
  await loadMyCourseList();

  // 添加全局页面入场动画 (GSAP)
  if (!window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    const tl = gsap.timeline({ defaults: { ease: 'power3.out' } });

    // 首屏区域入场
    tl.fromTo('.hero-text > *',
      { y: 30, opacity: 0 },
      { y: 0, opacity: 1, duration: 0.8, stagger: 0.1 }
    )
    .fromTo('.hero-visual',
      { scale: 0.9, opacity: 0, rotation: 5 },
      { scale: 1, opacity: 1, rotation: 0, duration: 1, ease: 'back.out(1.7)' },
      '-=0.6'
    )
    .fromTo('.floating-elements .floating-icon',
      { y: 20, opacity: 0 },
      { y: 0, opacity: 1, duration: 0.6, stagger: 0.1 },
      '-=0.4'
    );

    // 下方卡片区域滚动入场逻辑 (简单延迟或者使用 ScrollTrigger，这里用基础延迟显示)
    gsap.fromTo('.my-courses-section, .features-section, .news-section',
      { y: 40, opacity: 0 },
      { y: 0, opacity: 1, duration: 0.8, stagger: 0.2, delay: 0.5 }
    );
  }
})
</script>

<style scoped>
.student-home {
  --primary-color: #002fa7;
  --secondary-color: #517b4d;
  --accent-color: #e6a23c;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --info-color: #909399;
  --background-color: #f0f0f3;
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-muted: #909399;
  --border-color: #dcdfe6;
  --surface-color: #f0f0f3;
  --glass-bg: rgba(255, 255, 255, 0.6);
  --glass-border: rgba(255, 255, 255, 0.4);
  --shadow-light: #ffffff;
  --shadow-dark: rgba(166, 171, 189, 0.3);
  --spacing-xs: 0.25rem;
  --spacing-sm: 0.5rem;
  --spacing-md: 1rem;
  --spacing-lg: 1.5rem;
  --spacing-xl: 2rem;
  --spacing-2xl: 3rem;
  --spacing-3xl: 3.5rem;
  --spacing-4xl: 4.5rem;
  --border-radius-sm: 10px;
  --border-radius-md: 14px;
  --border-radius-lg: 18px;
  --border-radius-xl: 24px;
  --transition-fast: 0.15s ease;
  --transition-normal: 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-slow: 0.45s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 100vh;
  background: linear-gradient(145deg, #f0f0f3 0%, #e8e8eb 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  position: relative;
  overflow-x: hidden;
  color: var(--text-primary);
}

.student-home::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 15%, rgba(0, 47, 167, 0.08) 0%, transparent 55%),
    radial-gradient(circle at 85% 25%, rgba(81, 123, 77, 0.08) 0%, transparent 55%);
  pointer-events: none;
  z-index: 0;
}

.hero-section {
  padding: var(--spacing-3xl) var(--spacing-xl);
  margin: var(--spacing-lg);
  border-radius: 24px;
  position: relative;
  overflow: hidden;
  z-index: 1;
  background: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow:
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
}

.hero-background {
  position: absolute;
  inset: 0;
  overflow: hidden;
  z-index: 0;
}

.background-particles {
  display: none;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  25% {
    transform: translateY(-18px) rotate(60deg);
  }
  50% {
    transform: translateY(-12px) rotate(120deg);
  }
  75% {
    transform: translateY(-24px) rotate(180deg);
  }
}

.background-waves {
  position: absolute;
  bottom: -20px;
  left: 0;
  width: 100%;
  height: 220px;
  overflow: hidden;
}

.wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 200%;
  height: 120px;
  background: linear-gradient(90deg, rgba(0, 47, 167, 0.12) 0%, rgba(81, 123, 77, 0.08) 50%, rgba(0, 47, 167, 0.12) 100%);
  border-radius: 50% 50% 0 0;
  animation: wave 7s ease-in-out infinite;
}

.wave-1 {
  animation-delay: 0s;
  opacity: 0.4;
}

.wave-2 {
  animation-delay: 2s;
  opacity: 0.2;
  height: 90px;
}

.wave-3 {
  animation-delay: 4s;
  opacity: 0.15;
  height: 70px;
}

@keyframes wave {
  0%, 100% {
    transform: translateX(-50%) translateY(0px);
  }
  50% {
    transform: translateX(-50%) translateY(-18px);
  }
}

.hero-content {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: var(--spacing-3xl);
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
  padding: 0.45rem 1rem;
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  border-radius: 999px;
  color: var(--primary-color);
  font-size: 0.875rem;
  font-weight: 600;
  margin-bottom: var(--spacing-lg);
  box-shadow: 8px 8px 16px var(--shadow-dark), -8px -8px 16px var(--shadow-light);
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
}

.welcome-badge:hover {
  transform: translateY(-2px);
  box-shadow: 4px 4px 8px var(--shadow-dark), -4px -4px 8px var(--shadow-light);
}

.badge-icon {
  color: var(--accent-color);
  animation: sparkle 2.2s ease-in-out infinite;
}

@keyframes sparkle {
  0%, 100% {
    transform: scale(1) rotate(0deg);
  }
  50% {
    transform: scale(1.12) rotate(160deg);
  }
}

.hero-title {
  font-size: 3.15rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
  line-height: 1.08;
  position: relative;
}

.highlight-text {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-decoration {
  position: absolute;
  bottom: -10px;
  left: 0;
  width: 64px;
  height: 4px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 999px;
  animation: expandWidth 2s ease-out;
}

@keyframes expandWidth {
  0% {
    width: 0;
  }
  100% {
    width: 64px;
  }
}

.hero-description {
  font-size: 1.15rem;
  color: var(--text-secondary);
  margin-bottom: var(--spacing-xl);
  line-height: 1.7;
  font-weight: 500;
}

.hero-stats {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
  flex-wrap: wrap;
}

.hero-stats .stat-item {
  text-align: left;
  padding: 0.75rem 1.1rem;
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-lg);
  box-shadow: 8px 8px 16px var(--shadow-dark), -8px -8px 16px var(--shadow-light);
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  min-width: 120px;
}

.hero-stats .stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 4px 4px 8px var(--shadow-dark), -4px -4px 8px var(--shadow-light);
}

.hero-stats .stat-number {
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: var(--spacing-xs);
}

.hero-stats .stat-label {
  font-size: 0.8rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.hero-actions {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
  flex-wrap: wrap;
}

.modern-btn {
  position: relative;
  padding: 0.85rem 1.8rem;
  font-size: 1rem;
  font-weight: 600;
  border: none;
  border-radius: var(--border-radius-lg);
  cursor: pointer;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal), background var(--transition-normal);
  overflow: hidden;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 160px;
  height: 50px;
}

.modern-btn:focus-visible {
  outline: 3px solid rgba(59, 130, 246, 0.35);
  outline-offset: 2px;
}

.primary-btn {
  background: linear-gradient(135deg, #002fa7 0%, #517b4d 100%);
  color: white;
  box-shadow: 8px 8px 16px var(--shadow-dark), -8px -8px 16px var(--shadow-light), 0 0 12px rgba(0, 47, 167, 0.25);
}

.primary-btn:hover {
  transform: translateY(-3px);
  box-shadow: 4px 4px 8px var(--shadow-dark), -4px -4px 8px var(--shadow-light), 0 0 16px rgba(0, 47, 167, 0.3);
}

.primary-btn:active {
  transform: translateY(-1px);
}

.secondary-btn {
  background: var(--surface-color);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  box-shadow: 8px 8px 16px var(--shadow-dark), -8px -8px 16px var(--shadow-light);
}

.secondary-btn:hover {
  transform: translateY(-3px);
  background: var(--surface-color);
  box-shadow: 4px 4px 8px var(--shadow-dark), -4px -4px 8px var(--shadow-light);
}

.secondary-btn:active {
  transform: translateY(-1px);
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
  transform: scale(1.08);
}

.btn-glow {
  position: absolute;
  inset: 0;
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
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  border-radius: 32px;
  box-shadow: 12px 12px 24px var(--shadow-dark), -12px -12px 24px var(--shadow-light);
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
  filter: drop-shadow(0 8px 12px rgba(0, 47, 167, 0.2));
}

.progress-bg {
  fill: none;
  stroke: rgba(144, 147, 153, 0.35);
  stroke-width: 8;
}

.progress-bar {
  fill: none;
  stroke: var(--primary-color);
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
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  border-radius: 50%;
  width: 120px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: inset 8px 8px 16px var(--shadow-dark), inset -8px -8px 16px var(--shadow-light);
}

.progress-percentage {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: var(--spacing-xs);
}

.progress-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
  font-weight: 600;
}

.floating-elements {
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.floating-icon {
  position: absolute;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15);
  animation: floatOrbit 20s linear infinite;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
}

.floating-icon:hover {
  transform: scale(1.08);
  box-shadow: 4px 4px 8px var(--shadow-dark), -4px -4px 8px var(--shadow-light);
}

.floating-icon svg {
  font-size: 1.4rem;
  color: var(--primary-color);
  transition: transform var(--transition-normal);
}

.icon-1 {
  top: 15%;
  left: 50%;
  transform: translateX(-50%);
  animation-delay: 0s;
}

.icon-1 svg {
  color: var(--success-color);
}

.icon-2 {
  top: 50%;
  right: 12%;
  transform: translateY(-50%);
  animation-delay: 5s;
}

.icon-3 {
  bottom: 15%;
  left: 50%;
  transform: translateX(-50%);
  animation-delay: 10s;
}

.icon-3 svg {
  color: var(--warning-color);
}

.icon-4 {
  top: 50%;
  left: 12%;
  transform: translateY(-50%);
  animation-delay: 15s;
}

.icon-4 svg {
  color: var(--danger-color);
}

@keyframes floatOrbit {
  0% {
    transform: rotate(0deg) translateX(136px) rotate(0deg);
  }
  100% {
    transform: rotate(360deg) translateX(136px) rotate(-360deg);
  }
}

.quick-stats-section {
  padding: var(--spacing-3xl) 0;
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
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-xl);
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  position: relative;
  overflow: hidden;
  box-shadow: 12px 12px 24px var(--shadow-dark), -12px -12px 24px var(--shadow-light);
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 6px 6px 12px var(--shadow-dark), -6px -6px 12px var(--shadow-light);
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
  width: 52px;
  height: 52px;
  border-radius: var(--border-radius-lg);
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
  color: var(--primary-color);
  box-shadow: inset 4px 4px 8px var(--shadow-dark), inset -4px -4px 8px var(--shadow-light);
}

.stat-trend {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: 0.2rem 0.55rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.stat-trend.up {
  background: rgba(103, 194, 58, 0.12);
  color: #67c23a;
}

.stat-trend.down {
  background: rgba(245, 108, 108, 0.12);
  color: #f56c6c;
}

.stat-trend.stable {
  background: rgba(144, 147, 153, 0.18);
  color: #909399;
}

.stat-content {
  position: relative;
  z-index: 1;
}

.stat-content .stat-number {
  font-size: 2.25rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
  line-height: 1;
}

.stat-content .stat-label {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.stat-description {
  font-size: 0.9rem;
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
  background: var(--surface-color);
  border-radius: 999px;
  overflow: hidden;
  box-shadow: inset 4px 4px 8px var(--shadow-dark), inset -4px -4px 8px var(--shadow-light);
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 999px;
  transition: width 1.5s ease-out;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
  position: relative;
  z-index: 1;
}

.section-header {
  text-align: center;
  margin-bottom: var(--spacing-3xl);
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
}

.section-title {
  font-size: 2.6rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 64px;
  height: 4px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 999px;
  box-shadow: 0 6px 16px rgba(0, 47, 167, 0.3);
}

.section-subtitle {
  font-size: 1.15rem;
  color: var(--text-secondary);
  max-width: 720px;
  margin: 0 auto;
  line-height: 1.6;
  font-weight: 500;
}

.section-description {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.6;
}

.my-courses-section .section-header {
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-end;
  text-align: left;
  gap: var(--spacing-lg);
}

.my-courses-section .section-title::after {
  left: 0;
  transform: translateX(0);
}

.my-courses-section .header-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.view-all-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 0.6rem 1.35rem;
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  color: var(--primary-color);
  border-radius: 999px;
  font-weight: 600;
  font-size: 0.95rem;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  box-shadow: 8px 8px 16px var(--shadow-dark), -8px -8px 16px var(--shadow-light);
  position: relative;
  overflow: hidden;
}

.view-all-btn:hover {
  transform: translateY(-2px);
  box-shadow: 4px 4px 8px var(--shadow-dark), -4px -4px 8px var(--shadow-light);
}

.my-courses-section {
  padding: var(--spacing-2xl) var(--spacing-xl);
  margin: var(--spacing-lg);
  border-radius: 24px;
  position: relative;
  background: rgba(240, 240, 243, 0.45);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow:
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
}

.my-courses-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.4) 0%, transparent 55%, rgba(0, 47, 167, 0.04) 100%);
  pointer-events: none;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
  gap: var(--spacing-2xl);
}

.course-card {
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-xl);
  overflow: hidden;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  position: relative;
  cursor: pointer;
  z-index: 1;
  box-shadow: 12px 12px 24px var(--shadow-dark), -12px -12px 24px var(--shadow-light);
}

.course-card:hover {
  transform: translateY(-10px);
  box-shadow: 6px 6px 12px var(--shadow-dark), -6px -6px 12px var(--shadow-light);
}

.course-image {
  position: relative;
  height: 210px;
  overflow: hidden;
  border-radius: var(--border-radius-xl) var(--border-radius-xl) 0 0;
}

.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-normal);
}

.course-card:hover .course-image img {
  transform: scale(1.04);
}

.course-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.82) 0%, rgba(81, 123, 77, 0.75) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-normal);
  z-index: 2;
}

.course-card:hover .course-overlay {
  opacity: 1;
}

.play-button {
  width: 68px;
  height: 68px;
  border-radius: 50%;
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  color: var(--primary-color);
  font-size: 1.4rem;
  cursor: pointer;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 6px 6px 12px var(--shadow-dark), -6px -6px 12px var(--shadow-light);
  position: relative;
  z-index: 3;
}

.play-button:hover {
  transform: scale(1.08);
  box-shadow: 4px 4px 8px var(--shadow-dark), -4px -4px 8px var(--shadow-light);
}

.play-button:active {
  transform: scale(0.96);
}

.course-progress-overlay {
  position: absolute;
  bottom: var(--spacing-lg);
  left: var(--spacing-lg);
  right: var(--spacing-lg);
  background: rgba(255, 255, 255, 0.9);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius-md);
  text-align: center;
  z-index: 3;
}

.progress-text {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--primary-color);
}

.course-badge {
  position: absolute;
  top: var(--spacing-md);
  right: var(--spacing-md);
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: white;
  padding: 0.2rem 0.55rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
  z-index: 2;
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.35);
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
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.course-level {
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.course-level.初级 {
  background: rgba(103, 194, 58, 0.12);
  color: #67c23a;
}

.course-level.中级 {
  background: rgba(230, 162, 60, 0.12);
  color: #e6a23c;
}

.course-level.高级 {
  background: rgba(245, 108, 108, 0.12);
  color: #f56c6c;
}

.course-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  line-height: 1.35;
}

.course-description {
  color: var(--text-secondary);
  font-size: 0.98rem;
  line-height: 1.6;
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
  font-size: 0.85rem;
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
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.course-stats .stat svg {
  color: var(--primary-color);
  font-size: 0.85rem;
}

.learning-paths-section {
  padding: var(--spacing-4xl) 0;
  position: relative;
}

.learning-paths-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.04) 0%, transparent 55%, rgba(255, 255, 255, 0.45) 100%);
  pointer-events: none;
}

.paths-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
  gap: var(--spacing-2xl);
}

.path-card {
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-xl);
  cursor: pointer;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 420px;
  z-index: 1;
  box-shadow: 12px 12px 24px var(--shadow-dark), -12px -12px 24px var(--shadow-light);
}

.path-card:hover {
  transform: translateY(-10px);
  box-shadow: 6px 6px 12px var(--shadow-dark), -6px -6px 12px var(--shadow-light);
}

.path-background {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.path-pattern {
  position: absolute;
  top: -45%;
  right: -15%;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(0, 47, 167, 0.12) 0%, transparent 70%);
  border-radius: 50%;
  animation: float 6s ease-in-out infinite;
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
  padding: 0.25rem 0.7rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.path-badge.初级 {
  background: rgba(103, 194, 58, 0.16);
  color: #67c23a;
  border: 1px solid rgba(103, 194, 58, 0.25);
}

.path-badge.中级 {
  background: rgba(230, 162, 60, 0.16);
  color: #e6a23c;
  border: 1px solid rgba(230, 162, 60, 0.25);
}

.path-badge.高级 {
  background: rgba(245, 108, 108, 0.16);
  color: #f56c6c;
  border: 1px solid rgba(245, 108, 108, 0.25);
}

.path-content {
  flex: 1;
  padding: 0 var(--spacing-xl);
  position: relative;
  z-index: 1;
}

.path-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  line-height: 1.35;
}

.path-description {
  color: var(--text-secondary);
  font-size: 0.98rem;
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
  font-size: 0.85rem;
  color: var(--text-secondary);
  padding: 0.35rem 0.65rem;
  background: var(--surface-color);
  border-radius: 999px;
  border: 1px solid var(--border-color);
  box-shadow: inset 4px 4px 8px var(--shadow-dark), inset -4px -4px 8px var(--shadow-light);
}

.path-stats .stat svg {
  color: var(--primary-color);
  font-size: 0.95rem;
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
  font-size: 0.85rem;
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
  padding: 0.85rem 1.5rem;
  background: linear-gradient(135deg, #002fa7 0%, #517b4d 100%);
  color: white;
  border: none;
  border-radius: var(--border-radius-lg);
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  box-shadow: 8px 8px 16px var(--shadow-dark), -8px -8px 16px var(--shadow-light), 0 0 10px rgba(0, 47, 167, 0.2);
  position: relative;
  overflow: hidden;
}

.continue-btn:hover {
  transform: translateY(-2px);
  box-shadow: 4px 4px 8px var(--shadow-dark), -4px -4px 8px var(--shadow-light), 0 0 14px rgba(0, 47, 167, 0.25);
}

.continue-btn:active {
  transform: translateY(-1px);
}

.continue-btn svg {
  font-size: 1rem;
  transition: transform var(--transition-normal);
}

.continue-btn:hover svg {
  transform: scale(1.08);
}

.features-section {
  padding: var(--spacing-2xl) var(--spacing-xl);
  margin: var(--spacing-lg);
  border-radius: 24px;
  position: relative;
  background: rgba(240, 240, 243, 0.45);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow:
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
}

.features-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.04) 0%, transparent 55%, rgba(255, 255, 255, 0.5) 100%);
  pointer-events: none;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: var(--spacing-xl);
}

.feature-card {
  padding: var(--spacing-xl);
  text-align: center;
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-xl);
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  position: relative;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 12px 12px 24px var(--shadow-dark), -12px -12px 24px var(--shadow-light);
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 6px 6px 12px var(--shadow-dark), -6px -6px 12px var(--shadow-light);
}

.feature-icon {
  width: 68px;
  height: 68px;
  border-radius: 50%;
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--spacing-lg);
  box-shadow: inset 4px 4px 8px var(--shadow-dark), inset -4px -4px 8px var(--shadow-light);
}

.feature-icon i {
  font-size: 1.6rem;
  color: var(--primary-color);
}

.feature-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.feature-description {
  color: var(--text-secondary);
  line-height: 1.6;
  font-size: 0.98rem;
}

.news-section {
  padding: var(--spacing-2xl) var(--spacing-xl);
  margin: var(--spacing-lg);
  border-radius: 24px;
  position: relative;
  background: rgba(240, 240, 243, 0.45);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow:
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
}

.news-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.05) 0%, transparent 55%, rgba(255, 255, 255, 0.45) 100%);
  pointer-events: none;
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: var(--spacing-xl);
}

.news-card {
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-xl);
  overflow: hidden;
  cursor: pointer;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  position: relative;
  box-shadow: 12px 12px 24px var(--shadow-dark), -12px -12px 24px var(--shadow-light);
}

.news-card:hover {
  transform: translateY(-8px);
  box-shadow: 6px 6px 12px var(--shadow-dark), -6px -6px 12px var(--shadow-light);
}

.news-image {
  height: 180px;
  background: linear-gradient(135deg, #002fa7, #517b4d);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.image-placeholder i {
  font-size: 3.2rem;
  color: white;
  opacity: 0.92;
}

.news-category {
  position: absolute;
  top: var(--spacing-lg);
  right: var(--spacing-lg);
  background: rgba(255, 255, 255, 0.9);
  padding: 0.3rem 0.7rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--primary-color);
  z-index: 2;
  box-shadow: 4px 4px 8px var(--shadow-dark), -4px -4px 8px var(--shadow-light);
}

.news-content {
  padding: var(--spacing-xl);
  position: relative;
  z-index: 1;
}

.news-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  line-height: 1.4;
}

.news-summary {
  color: var(--text-secondary);
  font-size: 0.98rem;
  line-height: 1.6;
  margin-bottom: var(--spacing-lg);
}

.news-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.85rem;
  color: var(--text-secondary);
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--border-color);
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

@media (max-width: 1200px) {
  .container {
    max-width: 1200px;
    padding: 0 var(--spacing-lg);
  }

  .courses-grid {
    grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
    gap: var(--spacing-xl);
  }

  .paths-container {
    grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
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
    font-size: 2.4rem;
  }

  .section-title {
    font-size: 2.1rem;
  }

  .hero-actions {
    justify-content: center;
  }

  .hero-stats {
    justify-content: center;
  }

  .learning-dashboard {
    width: 280px;
    height: 280px;
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
    grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  }

  .news-grid {
    grid-template-columns: 1fr;
  }

  .floating-icon {
    width: 46px;
    height: 46px;
  }

  .floating-icon svg {
    font-size: 1.2rem;
  }

  .my-courses-section .section-header {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 var(--spacing-md);
  }

  .hero-title {
    font-size: 1.9rem;
  }

  .section-title {
    font-size: 1.8rem;
  }

  .hero-description {
    font-size: 1rem;
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
    width: 56px;
    height: 56px;
    font-size: 1.2rem;
  }

  .continue-btn {
    padding: 0.75rem 1rem;
    font-size: 0.875rem;
  }

  .feature-card,
  .news-card {
    padding: var(--spacing-lg);
  }

  .floating-elements {
    display: none;
  }
}

/* Refactored Course Card Styles */
.course-content {
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.course-title {
  margin-bottom: var(--spacing-xs);
  font-size: 1.1rem;
  line-height: 1.4;
}

.course-progress-section {
  margin-top: var(--spacing-xs);
  padding-top: var(--spacing-sm);
}

.progress-info-compact {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  font-size: 0.8rem;
}

.progress-label-compact {
  font-weight: 600;
  color: var(--text-secondary);
}

.next-lesson-hint {
  color: var(--primary-color);
  font-weight: 600;
  font-size: 0.75rem;
  background: rgba(0, 47, 167, 0.08);
  padding: 2px 8px;
  border-radius: 4px;
}

.next-lesson-hint.completed {
  color: var(--success-color);
  background: rgba(103, 194, 58, 0.1);
}

.progress-bar-bold {
  width: 100%;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill-bold {
  height: 100%;
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
  border-radius: 4px;
  transition: width 0.6s ease;
}

/* Feature & News Card Shadow Updates */
.feature-card, .news-card {
  box-shadow: 0 10px 30px -10px rgba(0,0,0,0.08);
  border: 1px solid rgba(0,0,0,0.03);
}

.feature-card:hover, .news-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px -10px rgba(0,0,0,0.12);
}

/* News Image Placeholder Update */
.news-image {
  background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%); /* More modern gradient */
}

.image-placeholder i {
  color: #fff;
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.1));
}
</style>
