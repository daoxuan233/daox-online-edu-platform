<template>
  <div class="course-detail-page">
    <!-- 课程头部信息 -->
    <div class="course-header">
      <div class="container">
        <div class="header-content">
          <!-- 课程基本信息 -->
          <div class="course-info">
            <div class="breadcrumb">
              <el-breadcrumb separator=">">
                <el-breadcrumb-item :to="{ path: '/index' }">首页</el-breadcrumb-item>
                <el-breadcrumb-item :to="{ path: '/index/courses' }">课程中心</el-breadcrumb-item>
                <el-breadcrumb-item>{{ course.title }}</el-breadcrumb-item>
              </el-breadcrumb>
            </div>

            <div class="course-meta">
              <div class="course-badges">
                <el-tag v-if="course.isNew" type="danger" size="small">新课</el-tag>
                <el-tag v-if="course.isHot" type="warning" size="small">热门</el-tag>
                <el-tag :type="getLevelTagType(course.level)" size="small">
                  {{ getLevelName(course.level) }}
                </el-tag>
              </div>

              <h1 class="course-title">{{ course.title }}</h1>
<!--              <markdownRender :content="course.subtitle" class="course-subtitle" />
              <p class="course-subtitle">{{ course.subtitle }}</p>-->

              <div class="course-stats">
                <div class="stat-item">
                  <el-rate
                      :model-value="course.rating"
                      disabled
                      show-score
                      text-color="#ff9900"
                      score-template="{value} 分"
                      v-if="course.rating > 0"
                  />
                  <span v-else class="no-rating">暂无评分</span>
                  <span class="review-count">({{ course.ratingCount || 0 }} 评价)</span>
                </div>

                <div class="stat-item">
                  <font-awesome-icon :icon="['fas', 'users']" class="text-primary"/>
                  <span>{{ course.studentCount }} 人学习</span>
                </div>

                <div class="stat-item">
                  <font-awesome-icon :icon="['fas', 'clock']" class="text-secondary"/>
                  <span>{{ course.duration }}</span>
                </div>

                <div class="stat-item">
                  <font-awesome-icon :icon="['fas', 'calendar-alt']" class="text-primary"/>
                  <span>{{ formatDateTime(course.updateTime) }}</span>
                </div>
              </div>

              <div class="instructor-info">
                <UserAvatar
                    :src="course.instructor.avatar"
                    :name="course.instructor.name"
                    :title="course.instructor.title"
                    :show-info="true"
                    role="teacher"
                    style="width: 480px;"
                />
              </div>
            </div>
          </div>

          <!-- 课程封面和购买 -->
          <div class="course-purchase">
            <div class="course-cover neumorphism-card">
              <div class="cover-image">
                <img :src="course.cover" :alt="course.title"/>
                <div class="play-overlay" @click="showPreview = true">
                  <font-awesome-icon :icon="['fas', 'play']"/>
                  <span>预览课程</span>
                </div>
              </div>

              <div class="purchase-info">
                <div class="price-section">
                  <div class="current-price" v-if="course.price">¥{{ course.price }}</div>
                  <div class="current-price" v-else>免费</div>
                  <div class="original-price" v-if="course.originalPrice && course.price">¥{{
                      course.originalPrice
                    }}
                  </div>
                  <div class="discount" v-if="course.originalPrice && course.price">
                    {{ Math.round((1 - course.price / course.originalPrice) * 100) }}% OFF
                  </div>
                </div>

                <div class="purchase-actions">
                  <el-button
                      type="primary"
                      size="large"
                      class="purchase-btn neumorphism-button"
                      @click="handlePurchase"
                  >
                    <font-awesome-icon :icon="['fas', 'shopping-cart']" class="mr-sm"/>
                    立即购买
                  </el-button>

                  <el-button
                      size="large"
                      class="cart-btn neumorphism-button"
                      @click="handleAddToCart"
                  >
                    <font-awesome-icon :icon="['fas', 'heart']" class="mr-sm"/>
                    加入收藏
                  </el-button>
                </div>

                <div class="guarantee-info">
                  <div class="guarantee-item">
                    <font-awesome-icon :icon="['fas', 'shield-alt']" class="text-secondary"/>
                    <span>30天无理由退款</span>
                  </div>
                  <div class="guarantee-item">
                    <font-awesome-icon :icon="['fas', 'mobile-alt']" class="text-primary"/>
                    <span>支持手机和电脑学习</span>
                  </div>
                  <div class="guarantee-item">
                    <font-awesome-icon :icon="['fas', 'certificate']" class="text-secondary"/>
                    <span>完成后颁发证书</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 课程详情内容 -->
    <div class="course-content">
      <div class="container">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="8" animated/>
        </div>

        <div v-else class="content-layout">
          <!-- 主要内容 -->
          <div class="main-content">
            <!-- 导航标签 -->
            <div class="content-tabs neumorphism-glass">
              <el-tabs v-model="activeTab" class="custom-tabs">
                <el-tab-pane label="课程介绍" name="overview">
                  <div class="tab-content">
                    <!-- 课程描述 -->
                    <div class="section">
                      <h3 class="section-title">课程描述</h3>
                      <markdownRender :content="course.description" class="message-text" />
<!--                      <div class="course-description" v-html="course.description"></div>-->
                    </div>

                    <!-- 适合人群 -->
                    <div class="section">
                      <h3 class="section-title">适合人群</h3>
                      <div class="target-audience">
                        <div
                            v-for="audience in course.targetAudience"
                            :key="audience"
                            class="audience-item neumorphism-card"
                        >
                          <font-awesome-icon :icon="['fas', 'user-check']" class="text-primary"/>
                          <span>{{ audience }}</span>
                        </div>
                      </div>
                    </div>

                    <!-- 技术要求 -->
                    <div class="section">
                      <h3 class="section-title">技术要求</h3>
                      <ul class="requirements">
                        <li v-for="requirement in course.requirements" :key="requirement">
                          <font-awesome-icon :icon="['fas', 'laptop-code']" class="text-primary"/>
                          <span>{{ requirement }}</span>
                        </li>
                      </ul>
                    </div>
                  </div>
                </el-tab-pane>

                <el-tab-pane label="学员评价" name="reviews">
                  <div class="tab-content">
                    <!-- 评价统计 -->
                    <div class="review-stats neumorphism-card">
                      <div class="overall-rating">
                        <div class="rating-score">{{ course.rating }}</div>
                        <div class="rating-stars">
                          <el-rate :model-value="course.rating" disabled/>
                        </div>
                        <div class="rating-count">{{ course.reviewCount }} 条评价</div>
                      </div>

                      <div class="rating-breakdown">
                        <div
                            v-for="(count, star) in course.ratingBreakdown"
                            :key="star"
                            class="rating-bar"
                        >
                          <span class="star-label">{{ star }}星</span>
                          <el-progress
                              :percentage="(count / course.reviewCount) * 100"
                              :stroke-width="8"
                              :show-text="false"
                          />
                          <span class="count">{{ count }}</span>
                        </div>
                      </div>
                    </div>

                    <!-- 评价列表 -->
                    <div class="reviews-list">
                      <div
                          v-for="review in course.reviews"
                          :key="review.id"
                          class="review-item neumorphism-card"
                      >
                        <div class="review-header">
                          <UserAvatar
                              :src="review.user.avatar"
                              :name="review.user.name"
                              size="small"
                              :show-info="true"
                              role="student"
                          />
                          <div class="review-meta">
                            <el-rate :model-value="review.rating" disabled size="small"/>
                            <span class="review-date">{{ review.date }}</span>
                          </div>
                        </div>
                        <div class="review-content">
                          <p>{{ review.content }}</p>
                          <div v-if="review.images" class="review-images">
                            <img
                                v-for="image in review.images"
                                :key="image"
                                :src="image"
                                alt="评价图片"
                                class="review-image"
                            />
                          </div>
                        </div>
                        <div class="review-actions">
                          <el-button size="small" text @click="handleLikeReview(review)">
                            <font-awesome-icon :icon="['fas', 'thumbs-up']"/>
                            {{ review.likes }}
                          </el-button>
                          <el-button size="small" text @click="handleReplyReview(review)">
                            <font-awesome-icon :icon="['fas', 'reply']"/>
                            回复
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </div>

          <!-- 侧边栏 -->
          <div class="sidebar">
            <!-- 讲师信息 -->
            <div class="instructor-card neumorphism-card">
              <h4 class="card-title">讲师介绍</h4>
              <div class="instructor-profile">
                <UserAvatar
                    :src="course.instructor.avatar"
                    :name="course.instructor.name"
                    :title="course.instructor.title"
                    size="medium"
                    :show-info="true"
                    role="teacher"
                />
                <div class="instructor-stats">
                  <div class="stat">
                    <span class="stat-value">{{ course.instructor.courseCount }}</span>
                    <span class="stat-label">门课程</span>
                  </div>
                  <div class="stat">
                    <span class="stat-value">{{ course.instructor.studentCount }}</span>
                    <span class="stat-label">名学员</span>
                  </div>
                  <div class="stat">
                    <span class="stat-value">{{ course.instructor.rating }}</span>
                    <span class="stat-label">评分</span>
                  </div>
                </div>
                <p class="instructor-bio">{{ course.instructor.bio }}</p>

                <!-- 讲师课程列表 -->
                <div v-if="course.instructor.courseList && course.instructor.courseList.length > 0"
                     class="instructor-courses">
                  <h5 class="card-title">讲师其他课程</h5>
                  <div class="courses-list">
                    <div
                        v-for="instructorCourse in course.instructor.courseList"
                        :key="instructorCourse.id"
                        class="instructor-course-item"
                        @click="$router.push(`/index/courses/${instructorCourse.id}`)"
                    >
                      <img
                          :src="instructorCourse.cover || COURSE_THUMBNAIL"
                          :alt="instructorCourse.title"
                          class="course-thumb"
                      />
                      <div class="course-info">
                        <h6 class="course-title-other-courses">{{ instructorCourse.title }}</h6>
                        <!--                        <p class="course-category">{{ instructorCourse.categoryName }}</p>-->
                        <div class="course-meta">
                          <span class="course-status" :class="getStatusClass(instructorCourse.status)">
                            {{ getStatusText(instructorCourse.status) }}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 课程预览弹窗 -->
    <el-dialog
        v-model="showPreview"
        title="课程预览"
        width="80%"
        class="preview-dialog"
    >
      <div class="preview-content">
        <div class="video-placeholder">
          <font-awesome-icon :icon="['fas', 'play-circle']" class="text-6xl text-primary"/>
          <p>课程预览视频</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted} from 'vue'
import {useRoute} from 'vue-router'
import {ElMessage} from 'element-plus'
import UserAvatar from '@/components/UserAvatar.vue'
import {COURSE_DETAIL_PLACEHOLDER, AVATAR_MEDIUM, AVATAR_SMALL, COURSE_THUMBNAIL} from '@/utils/placeholders'
import {getCourseDetail} from '@/api/students/stuAPI.js'
import {formatDateTime} from '@/utils/dateUtils.js'
import markdownRender from '@/components/markdownRender/NotAIShow.vue'

const route = useRoute()
const activeTab = ref('overview')
const showPreview = ref(false)
const expandedChapters = ref([0]) // 默认展开第一章
const loading = ref(false) // 加载状态

// 课程数据
const course = reactive({
  id: 1,
  title: 'Vue.js 3.0 完整开发教程',
  subtitle: '从基础到进阶，全面掌握Vue.js 3.0开发技能',
  description: `
    <p>本课程是一门全面的Vue.js 3.0开发教程，从基础语法到高级特性，从理论知识到实战项目，帮助您系统掌握Vue.js 3.0的核心技术。</p>
    <p>课程采用最新的Composition API语法，结合TypeScript开发，让您的代码更加健壮和可维护。通过多个实战项目，您将学会如何构建现代化的前端应用。</p>
    <p>无论您是前端新手还是有经验的开发者，这门课程都能为您提供有价值的内容和实用的技能。</p>
  `,
  cover: COURSE_DETAIL_PLACEHOLDER,
  instructor: {
    id: 1,
    name: '张老师',
    title: '高级前端工程师',
    avatar: AVATAR_MEDIUM,
    bio: '10年前端开发经验，曾就职于知名互联网公司，专注于Vue.js生态系统的研究和实践。',
    courseCount: 15,
    studentCount: 12000,
    rating: 4.9
  },
  duration: '24小时',
  studentCount: 1250,
  rating: 4.8,
  reviewCount: 156,
  price: 299,
  originalPrice: 399,
  level: 'intermediate',
  updateTime: '2024-01-15',
  isNew: true,
  isHot: true,
  objectives: [
    '掌握Vue.js 3.0的核心概念和语法',
    '熟练使用Composition API进行组件开发',
    '学会Vue Router和Vuex的使用',
    '掌握组件通信和状态管理',
    '能够独立开发完整的Vue.js项目',
    '了解Vue.js生态系统和最佳实践'
  ],
  targetAudience: [
    '想要学习Vue.js的前端开发者',
    '有JavaScript基础的程序员',
    '希望提升前端技能的开发者',
    '准备转行前端的技术人员'
  ],
  requirements: [
    '具备HTML、CSS、JavaScript基础',
    '了解ES6+语法特性',
    '有基本的编程经验',
    '准备好学习的心态'
  ],
  curriculum: [
    {
      id: 1,
      title: 'Vue.js 3.0 基础入门',
      duration: '3小时',
      lessons: [
        {id: 1, title: 'Vue.js简介和环境搭建', type: 'video', duration: '30分钟', isFree: true},
        {id: 2, title: '模板语法和数据绑定', type: 'video', duration: '45分钟', isFree: true},
        {id: 3, title: '条件渲染和列表渲染', type: 'video', duration: '40分钟', isFree: false},
        {id: 4, title: '事件处理和表单输入', type: 'video', duration: '35分钟', isFree: false},
        {id: 5, title: '基础练习', type: 'exercise', duration: '30分钟', isFree: false}
      ]
    },
    {
      id: 2,
      title: 'Composition API 深入学习',
      duration: '4小时',
      lessons: [
        {id: 6, title: 'setup函数详解', type: 'video', duration: '50分钟', isFree: false},
        {id: 7, title: 'ref和reactive的使用', type: 'video', duration: '45分钟', isFree: false},
        {id: 8, title: 'computed和watch', type: 'video', duration: '40分钟', isFree: false},
        {id: 9, title: '生命周期钩子', type: 'video', duration: '35分钟', isFree: false},
        {id: 10, title: 'Composition API实战', type: 'project', duration: '50分钟', isFree: false}
      ]
    }
  ],
  ratingBreakdown: {
    5: 98,
    4: 45,
    3: 10,
    2: 2,
    1: 1
  },
  reviews: [
    {
      id: 1,
      user: {
        name: '学员A',
        avatar: AVATAR_SMALL
      },
      rating: 5,
      date: '2024-01-10',
      content: '课程内容非常详细，老师讲解清晰，实战项目很有帮助。强烈推荐！',
      likes: 15
    },
    {
      id: 2,
      user: {
        name: '学员B',
        avatar: AVATAR_SMALL
      },
      rating: 4,
      date: '2024-01-08',
      content: '整体不错，但是希望能增加更多的实战案例。',
      likes: 8
    }
  ]
})

// 方法
const getLevelName = (level) => {
  const levels = {
    beginner: '初级',
    intermediate: '中级',
    advanced: '高级'
  }
  return levels[level] || level
}

const getLevelTagType = (level) => {
  const types = {
    beginner: 'success',
    intermediate: 'warning',
    advanced: 'danger'
  }
  return types[level] || 'info'
}

const getLessonIcon = (type) => {
  const icons = {
    video: ['fas', 'play-circle'],
    exercise: ['fas', 'edit'],
    project: ['fas', 'code'],
    quiz: ['fas', 'question-circle']
  }
  return icons[type] || ['fas', 'file']
}

const toggleChapter = (index) => {
  const pos = expandedChapters.value.indexOf(index)
  if (pos > -1) {
    expandedChapters.value.splice(pos, 1)
  } else {
    expandedChapters.value.push(index)
  }
}

const handlePurchase = () => {
  ElMessage.success('跳转到支付页面...')
}

const handleAddToCart = () => {
  ElMessage.success('已加入收藏')
}

const handlePreviewLesson = (lesson) => {
  ElMessage.info(`播放课程：${lesson.title}`)
}

const handleLikeReview = (review) => {
  review.likes++
  ElMessage.success('点赞成功')
}

const handleReplyReview = (review) => {
  ElMessage.info('回复功能开发中...')
}

// 获取课程状态样式类
const getStatusClass = (status) => {
  const statusClasses = {
    'published': 'status-published',
    'PUBLISHED': 'status-published',
    'draft': 'status-draft',
    'DRAFT': 'status-draft',
    'archived': 'status-archived',
    'ARCHIVED': 'status-archived'
  }
  return statusClasses[status] || 'status-unknown'
}

// 获取课程状态文本
const getStatusText = (status) => {
  const statusTexts = {
    'published': '已发布',
    'PUBLISHED': '已发布',
    'draft': '草稿',
    'DRAFT': '草稿',
    'archived': '已归档',
    'ARCHIVED': '已归档'
  }
  return statusTexts[status] || '未知状态'
}

// 加载课程数据的方法
const loadCourseData = async (courseId) => {
  loading.value = true
  try {
    const response = await getCourseDetail(courseId)
    console.log('完整的响应对象:', response)

    // 检查响应结构，后端返回的是RestBean格式
    if (response) {
      const courseData = response

      // 映射后端数据到前端数据结构
      course.id = courseData.courseId
      course.title = courseData.title || '未知课程'
      course.subtitle = courseData.description || '暂无描述'
      course.description = courseData.description || '<p>暂无课程描述</p>'
      course.cover = courseData.cover || course.cover
      course.duration = courseData.duration || course.duration
      course.studentCount = courseData.studentCount || 0
      course.rating = courseData.rating || 0
      course.ratingCount = courseData.ratingCount || 0
      course.price = courseData.price
      course.originalPrice = courseData.originalPrice
      // 处理level字段，去除多余的引号
      course.level = courseData.level ? courseData.level.replace(/'/g, '') : '未知'
      /*course.updateTime = courseData.updateTime || '未知'*/
      course.isNew = courseData.isNew || false
      course.isHot = false // 后端暂无此字段，使用默认值

      // 处理适合人群和技术要求（后端返回的是字符串，转换为数组）
      if (courseData.targetAudience) {
        // 逗号分割字符串 , 中文逗号和英文逗号都需要处理
        course.targetAudience = courseData.targetAudience.split(/[，,]/).map(req => req.trim())
      }
      if (courseData.technicalRequirements) {
        // 逗号分割字符串 , 中文逗号和英文逗号都需要处理
        course.requirements = courseData.technicalRequirements.split(/[，,]/).map(req => req.trim())
      }

      // 映射讲师信息
      if (courseData.instructor) {
        course.instructor.id = courseData.instructor.id
        course.instructor.name = courseData.instructor.name || '未知讲师'
        course.instructor.title = courseData.instructor.title || '讲师'
        course.instructor.bio = courseData.instructor.biography || '暂无简介'
        course.instructor.avatar = courseData.instructor.avatar || course.instructor.avatar
        course.instructor.courseCount = courseData.instructor.courseCount || 0
        course.instructor.rating = courseData.instructor.rating || 4.5
        // 后端CourseInstructorVO没有studentCount字段，使用默认值
        course.instructor.studentCount = 1000

        // 映射教师的课程列表（排除当前课程）
        if (courseData.instructor.courseList && Array.isArray(courseData.instructor.courseList)) {
          course.instructor.courseList = courseData.instructor.courseList
              .filter(courseItem => courseItem.courseId !== courseData.courseId) // 过滤掉当前课程
              .map(courseItem => ({
                id: courseItem.courseId,
                title: courseItem.courseTitle,
                description: courseItem.courseDescription,
                cover: courseItem.courseCover,
                teacherId: courseItem.teacherId,
                teacherName: courseItem.teacherName,
                categoryId: courseItem.categoryId,
                categoryName: courseItem.categoryName,
                status: courseItem.courseStatus,
                createTime: courseItem.createTime,
                updateTime: courseItem.updateTime
              }))
          // 从教师的课程列表中获取当前课程的updateTime
          const currentCourse = courseData.instructor.courseList.find(c => c.courseId === courseData.courseId)
          if (currentCourse) {
            course.updateTime = currentCourse.updateTime || '未知'
          } else {
            course.updateTime = '未知'
          }
        } else {
          course.instructor.courseList = []
        }
      }

      console.log('映射后的课程数据:', JSON.parse(JSON.stringify(course)))
    }
  } catch (error) {
    console.error('加载课程数据失败:', error)
    ElMessage.error('加载课程数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  // 根据路由参数加载课程数据
  const courseId = route.params.id
  if (courseId) {
    loadCourseData(courseId)
  } else {
    console.log('使用默认课程数据')
  }
})
</script>

<style scoped>
/* 课程详情页面样式 - 现代化科技感设计 */

/* 全局变量定义 */
:root {
  --primary-color: #002FA7;
  --secondary-color: #517B4D;
  --success-color: #67C23A;
  --warning-color: #E6A23C;
  --danger-color: #F56C6C;
  --info-color: #909399;
  --bg-color: #F0F0F3;
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-auxiliary: #909399;
  --border-color: #DCDFE6;
  --shadow-light: #ffffff;
  --shadow-dark: #d1d1d4;
  --glass-bg: rgba(255, 255, 255, 0.25);
  --glass-border: rgba(255, 255, 255, 0.18);
}

/* 新拟态设计基础样式 */
.neumorphism-card {
  background: var(--bg-color);
  box-shadow: 8px 8px 16px var(--shadow-dark),
  -8px -8px 16px var(--shadow-light);
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.neumorphism-card:hover {
  box-shadow: 12px 12px 24px var(--shadow-dark),
  -12px -12px 24px var(--shadow-light);
  transform: translateY(-2px);
}

.neumorphism-button {
  background: var(--bg-color);
  box-shadow: 6px 6px 12px var(--shadow-dark),
  -6px -6px 12px var(--shadow-light);
  border: none;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.neumorphism-button:hover {
  box-shadow: 4px 4px 8px var(--shadow-dark),
  -4px -4px 8px var(--shadow-light);
  transform: translateY(-1px);
}

.neumorphism-button:active {
  box-shadow: inset 4px 4px 8px var(--shadow-dark),
  inset -4px -4px 8px var(--shadow-light);
  transform: translateY(0);
}

/* 磨砂玻璃效果 */
.neumorphism-glass {
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1),
  inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

/* 页面整体布局 */
.course-detail-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 课程头部区域 */
.course-header {
  background: linear-gradient(135deg, var(--primary-color) 1%, var(--secondary-color) 60%);
  color: white;
  padding: 2rem 0 4rem;
  position: relative;
  overflow: hidden;
}

.course-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="50" cy="50" r="1" fill="%23ffffff" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>') repeat;
  pointer-events: none;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
  position: relative;
  z-index: 1;
}

.header-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 3rem;
  align-items: start;
}

/* 面包屑导航 */
.breadcrumb {
  margin-bottom: 1.5rem;
}

.breadcrumb :deep(.el-breadcrumb__item) {
  color: rgba(255, 255, 255, 0.8);
}

.breadcrumb :deep(.el-breadcrumb__item:last-child) {
  color: white;
  font-weight: 600;
}

/* 课程基本信息 */
.course-info {
  animation: slideInLeft 0.8s ease-out;
}

.course-badges {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.course-title {
  font-size: 2.5rem;
  font-weight: 700;
  line-height: 1.3;
  margin: 0 0 2rem;
  text-overflow: ellipsis;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.course-title-other-courses-title {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-primary);
  margin: 0 0 0.25rem;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-title-other-courses {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-primary);
  margin: 0 0 0.25rem;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  /*左对齐*/
  text-align: left;
}

.course-subtitle {
  font-size: 1.125rem;
  margin: 0 0 2rem;
  line-height: 1.6;
}

.course-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 2rem;
  margin-bottom: 2rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.9);
}

.stat-item .fa-icon {
  font-size: 1rem;
}

.no-rating {
  color: rgba(255, 255, 255, 0.7);
}

.review-count {
  color: rgba(255, 255, 255, 0.8);
  margin-left: 0.5rem;
}

/* 讲师信息 */
.instructor-info {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 1rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 课程购买区域 */
.course-purchase {
  animation: slideInRight 0.8s ease-out;
}

.course-cover {
  background: white;
  padding: 1.5rem;
  position: sticky;
  top: 2rem;
}

.cover-image {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 1.5rem;
  aspect-ratio: 16/9;
}

.cover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  opacity: 0;
  transition: all 0.3s ease;
}

.cover-image:hover .play-overlay {
  opacity: 1;
}

.cover-image:hover img {
  transform: scale(1.05);
}

.play-overlay .fa-icon {
  font-size: 3rem;
  margin-bottom: 0.5rem;
}

/* 价格区域 */
.price-section {
  display: flex;
  align-items: baseline;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.current-price {
  font-size: 2rem;
  font-weight: 700;
  color: var(--primary-color);
}

.original-price {
  font-size: 1.125rem;
  color: var(--text-auxiliary);
  text-decoration: line-through;
}

.discount {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
}

/* 购买按钮 */
.purchase-actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.purchase-btn {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  font-weight: 600;
  padding: 0.875rem 1.5rem;
  font-size: 1rem;
}

.cart-btn {
  background: var(--bg-color);
  color: var(--text-primary);
  font-weight: 500;
  padding: 0.875rem 1.5rem;
}

/* 保障信息 */
.guarantee-info {
  border-top: 1px solid var(--border-color);
  padding-top: 1rem;
}

.guarantee-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.guarantee-item:last-child {
  margin-bottom: 0;
}

/* 课程内容区域 */
.course-content {
  padding: 3rem 0;
  margin-top: -2rem;
  position: relative;
  z-index: 2;
}

.content-layout {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 3rem;
  align-items: start;
}

/* 主要内容区域 */
.main-content {
  animation: fadeInUp 0.8s ease-out 0.2s both;
}

/* 标签页样式 */
.content-tabs {
  padding: 2rem;
  margin-bottom: 2rem;
}

.custom-tabs :deep(.el-tabs__header) {
  background: none;
  border: none;
  margin-bottom: 2rem;
}

.custom-tabs :deep(.el-tabs__nav-wrap) {
  background: var(--bg-color);
  border-radius: 12px;
  padding: 0.5rem;
  box-shadow: inset 4px 4px 8px var(--shadow-dark),
  inset -4px -4px 8px var(--shadow-light);
}

.custom-tabs :deep(.el-tabs__item) {
  border: none;
  background: none;
  color: var(--text-secondary);
  font-weight: 500;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.custom-tabs :deep(.el-tabs__item.is-active) {
  background: white;
  color: var(--primary-color);
  box-shadow: 2px 2px 4px var(--shadow-dark),
  -2px -2px 4px var(--shadow-light);
}

.custom-tabs :deep(.el-tabs__active-bar) {
  display: none;
}

/* 标签内容 */
.tab-content {
  animation: fadeIn 0.5s ease-out;
}

.section {
  margin-bottom: 3rem;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
  position: relative;
  padding-left: 1rem;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 1.5rem;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  border-radius: 2px;
}

/* 课程描述 */
.course-description {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 4px 4px 8px var(--shadow-dark),
  -4px -4px 8px var(--shadow-light);
  line-height: 1.8;
  color: var(--text-primary);
}

.course-description :deep(p) {
  margin-bottom: 1rem;
}

.course-description :deep(p:last-child) {
  margin-bottom: 0;
}

/* 适合人群 */
.target-audience {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.audience-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  background: white;
  color: var(--text-primary);
  font-weight: 500;
}

.audience-item .fa-icon {
  color: var(--primary-color);
  font-size: 1.125rem;
}

/* 技术要求 */
.requirements {
  list-style: none;
  padding: 0;
  margin: 0;
}

.requirements li {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: white;
  margin-bottom: 0.75rem;
  border-radius: 8px;
  box-shadow: 2px 2px 4px var(--shadow-dark),
  -2px -2px 4px var(--shadow-light);
  transition: all 0.3s ease;
}

.requirements li:hover {
  transform: translateX(4px);
  box-shadow: 4px 4px 8px var(--shadow-dark),
  -4px -4px 8px var(--shadow-light);
}

.requirements li:last-child {
  margin-bottom: 0;
}

.requirements li .fa-icon {
  color: var(--primary-color);
  font-size: 1.125rem;
}

/* 评价统计 */
.review-stats {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 2rem;
  padding: 2rem;
  background: white;
  margin-bottom: 2rem;
}

.overall-rating {
  text-align: center;
}

.rating-score {
  font-size: 3rem;
  font-weight: 700;
  color: var(--primary-color);
  line-height: 1;
  margin-bottom: 0.5rem;
}

.rating-stars {
  margin-bottom: 0.5rem;
}

.rating-count {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.rating-breakdown {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.rating-bar {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 1rem;
  align-items: center;
}

.star-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  min-width: 3rem;
}

.count {
  font-size: 0.875rem;
  color: var(--text-secondary);
  min-width: 2rem;
  text-align: right;
}

/* 评价列表 */
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.review-item {
  padding: 1.5rem;
  background: white;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.review-date {
  color: var(--text-auxiliary);
  font-size: 0.875rem;
}

.review-content {
  margin-bottom: 1rem;
}

.review-content p {
  line-height: 1.6;
  color: var(--text-primary);
  margin: 0;
}

.review-images {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

.review-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.review-image:hover {
  transform: scale(1.1);
}

.review-actions {
  display: flex;
  gap: 1rem;
}

/* 侧边栏 */
.sidebar {
  animation: fadeInUp 0.8s ease-out 0.4s both;
}

/* 讲师卡片 */
.instructor-card {
  padding: 2rem;
  background: white;
  position: sticky;
  top: 2rem;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
  text-align: center;
}

.instructor-profile {
  text-align: center;
}

.instructor-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin: 1.5rem 0;
  padding: 1rem 0;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
}

.stat {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--primary-color);
  line-height: 1;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-auxiliary);
  margin-top: 0.25rem;
}

.instructor-bio {
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 1.5rem 0;
  text-align: left;
}

/* 讲师课程列表 */
.instructor-courses {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-color);
}

.courses-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.courses-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.instructor-course-item {
  display: flex;
  gap: 0.75rem;
  padding: 0.75rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: var(--bg-color);
}

.instructor-course-item:hover {
  background: white;
  box-shadow: 2px 2px 4px var(--shadow-dark),
  -2px -2px 4px var(--shadow-light);
  transform: translateY(-1px);
}

.course-thumb {
  width: 75px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}

.course-info {
  flex: 1;
  min-width: 0;
}

/*.course-title {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-primary);
  margin: 0 0 0.25rem;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}*/

.course-category {
  font-size: 0.75rem;
  color: var(--text-auxiliary);
  /*左对齐*/
  text-align: left;
  margin: 0 0 0.25rem;
}

.course-meta {

  align-items: center;
  gap: 0.5rem;
}

.course-status {
  font-size: 0.625rem;
  padding: 0.125rem 0.375rem;
  border-radius: 4px;
  font-weight: 500;
  /*左对齐*/
  text-align: left;
}

.status-published {
  background: #f0fdf4;
  color: #16a34a;
  border: 1px solid #bbf7d0;
}

.status-draft {
  background: #fef3c7;
  color: #d97706;
}

.status-archived {
  background: #f3f4f6;
  color: #6b7280;
}

.status-unknown {
  background: #f3f4f6;
  color: #6b7280;
}

/* 预览弹窗 */
.preview-dialog :deep(.el-dialog) {
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
}

.preview-content {
  padding: 2rem;
  text-align: center;
}

.video-placeholder {
  background: var(--bg-color);
  border-radius: 12px;
  padding: 4rem 2rem;
  color: var(--text-auxiliary);
}

.video-placeholder .fa-icon {
  margin-bottom: 1rem;
}

/* 加载状态 */
.loading-container {
  padding: 3rem;
  background: white;
  border-radius: 16px;
  box-shadow: 8px 8px 16px var(--shadow-dark),
  -8px -8px 16px var(--shadow-light);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-content,
  .content-layout {
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  .course-purchase {
    order: -1;
  }

  .course-cover {
    position: static;
  }
}

@media (max-width: 768px) {
  .container {
    padding: 0 1rem;
  }

  .course-header {
    padding: 1.5rem 0 2rem;
  }

  .course-title {
    font-size: 1.75rem;
  }

  .course-stats {
    flex-direction: column;
    gap: 1rem;
  }

  .target-audience {
    grid-template-columns: 1fr;
  }

  .review-stats {
    grid-template-columns: 1fr;
    gap: 1.5rem;
    text-align: center;
  }

  .instructor-stats {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
}

/* 动画定义 */
@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 紧凑头像样式 */
.compact-avatar .user-avatar {
  max-width: 165px;
  padding: 0.75rem;
  gap: 6px;
}

.compact-avatar .user-name {
  font-size: 1rem;
}

.compact-avatar .user-title {
  font-size: 0.875rem;
}

/* 确保instructor-info区域有足够空间 */
.instructor-info {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 1rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  width: 100%;
  display: flex;
  align-items: center;
  min-height: 80px;
}

/* 工具类 */
.text-primary {
  color: var(--primary-color) !important;
}

.text-secondary {
  color: var(--text-secondary) !important;
}

.text-6xl {
  font-size: 4rem !important;
}

.mr-sm {
  margin-right: 0.5rem !important;
}
</style>