<template>
  <div class="my-courses-page">
    <!-- 页面头部 -->
    <div class="page-header" style="margin-bottom: 10px;">
      <div class="container">
        <div class="header-content">
          <div class="header-info">
            <h1 class="page-title">我的课程</h1>
            <p class="page-subtitle">管理您的学习进度，继续您的学习之旅</p>
          </div>

          <div class="header-stats">
            <div class="stat-card neumorphism-card">
              <div class="stat-value">{{ stats.totalCourses }}</div>
              <div class="stat-label">总课程数</div>
            </div>
            <div class="stat-card neumorphism-card">
              <div class="stat-value">{{ stats.completedCourses }}</div>
              <div class="stat-label">已完成</div>
            </div>
            <div class="stat-card neumorphism-card">
              <div class="stat-value">{{ stats.inProgressCourses }}</div>
              <div class="stat-label">学习中</div>
            </div>
            <div class="stat-card neumorphism-card">
              <div class="stat-value">{{ stats.totalHours }}</div>
              <div class="stat-label">学习时长</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="page-content">
      <div class="container" style=" padding-left: 0;">
        <div class="content-layout" style="padding-left: 0;">
          <!-- 筛选和搜索 -->
          <div class="filters-section neumorphism-glass">
            <div class="filters-header">
              <h3 class="filters-title">筛选课程</h3>
              <el-button
                  type="primary"
                  text
                  @click="resetFilters"
                  class="reset-btn"
              >
                <font-awesome-icon :icon="['fas', 'undo']" class="mr-xs"/>
                重置
              </el-button>
            </div>

            <div class="filters-content">
              <!-- 搜索框 -->
              <div class="filter-group search-group">
                <label class="filter-label">搜索课程</label>
                <div class="search-wrapper">
                  <SearchBar
                      v-model="searchQuery"
                      placeholder="搜索我的课程..."
                      size="medium"
                      :show-suggestions="false"
                      @search="handleSearch"
                      class="custom-search-bar"
                  />
                </div>
              </div>

              <!-- 状态筛选 -->
              <div class="filter-group">
                <label class="filter-label">学习状态</label>
                <div class="filter-options">
                  <el-radio-group v-model="filters.status" @change="handleFilterChange">
                    <el-radio-button label="all">全部</el-radio-button>
                    <el-radio-button label="not_started">未开始</el-radio-button>
                    <el-radio-button label="in_progress">学习中</el-radio-button>
                    <el-radio-button label="completed">已完成</el-radio-button>
                  </el-radio-group>
                </div>
              </div>

              <!-- 分类筛选 -->
              <div class="filter-group">
                <label class="filter-label">课程分类</label>
                <div class="filter-options">
                  <el-select
                      v-model="filters.category"
                      placeholder="选择分类"
                      clearable
                      @change="handleFilterChange"
                  >
                    <el-option label="全部分类" value=""/>
                    <el-option
                        v-for="category in categories"
                        :key="category.value"
                        :label="category.label"
                        :value="category.value"
                    />
                  </el-select>
                </div>
              </div>

              <!-- 排序 -->
              <div class="filter-group">
                <label class="filter-label">排序方式</label>
                <div class="filter-options">
                  <el-select v-model="filters.sortBy" @change="handleFilterChange">
                    <el-option label="最近学习" value="recent"/>
                    <el-option label="购买时间" value="purchase_date"/>
                    <el-option label="课程名称" value="name"/>
                    <el-option label="学习进度" value="progress"/>
                  </el-select>
                </div>
              </div>
            </div>
          </div>

          <!-- 课程列表 -->
          <div class="courses-section">
            <!-- 视图切换 -->
            <div class="view-controls neumorphism-glass">
              <div class="view-info">
                <span class="course-count">共 {{ filteredCourses.length }} 门课程</span>
              </div>

              <div class="view-actions">
                <el-radio-group v-model="viewMode" size="small">
                  <el-radio-button label="grid">
                    <font-awesome-icon :icon="['fas', 'th-large']"/>
                  </el-radio-button>
                  <el-radio-button label="list">
                    <font-awesome-icon :icon="['fas', 'list']"/>
                  </el-radio-button>
                </el-radio-group>
              </div>
            </div>

            <!-- 课程网格视图 -->
            <div v-if="viewMode === 'grid'" class="courses-grid">
              <div
                  v-for="course in paginatedCourses"
                  :key="course.id"
                  class="course-card neumorphism-card"
                  @click="goToCourse(course)"
              >
                <div class="course-cover">
                  <img :src="course.cover" :alt="course.title"/>
                  <div class="course-overlay">
                    <div class="course-actions">
                      <el-button
                          type="primary"
                          circle
                          @click.stop="continueLearning(course)"
                      >
                        <font-awesome-icon :icon="['fas', 'play']"/>
                      </el-button>
                    </div>
                  </div>

                  <!-- 状态标签 -->
                  <div class="course-status">
                    <el-tag
                        :type="getStatusTagType(course.status)"
                        size="small"
                    >
                      {{ getStatusText(course.status) }}
                    </el-tag>
                  </div>
                </div>

                <div class="course-info">
                  <h3 class="course-title">{{ course.title }}</h3>
                  <p class="course-instructor">{{ course.instructor.name }}</p>

                  <!-- 学习进度 -->
                  <div class="course-progress">
                    <div class="progress-info">
                      <span class="progress-text">学习进度</span>
                      <span class="progress-percent">{{ course.progress }}%</span>
                    </div>
                    <el-progress
                        :percentage="course.progress"
                        :stroke-width="6"
                        :show-text="false"
                    />
                  </div>

                  <!-- 课程统计 -->
                  <div class="course-stats">
                    <div class="stat">
                      <font-awesome-icon :icon="['fas', 'clock']" class="text-secondary"/>
                      <span>{{ course.duration }}</span>
                    </div>
                    <div class="stat">
                      <font-awesome-icon :icon="['fas', 'calendar']" class="text-primary"/>
                      <span>{{ course.lastStudyTime }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 课程列表视图 -->
            <div v-else class="courses-list">
              <div
                  v-for="course in paginatedCourses"
                  :key="course.id"
                  class="course-item neumorphism-card"
                  @click="goToCourse(course)"
              >
                <div class="course-cover">
                  <img :src="course.cover" :alt="course.title"/>
                </div>

                <div class="course-details">
                  <div class="course-header">
                    <div class="course-main">
                      <h3 class="course-title">{{ course.title }}</h3>
                      <p class="course-instructor">讲师：{{ course.instructor.name }}</p>
                    </div>

                    <div class="course-status">
                      <el-tag
                          :type="getStatusTagType(course.status)"
                          size="small"
                      >
                        {{ getStatusText(course.status) }}
                      </el-tag>
                    </div>
                  </div>

                  <div class="course-progress">
                    <div class="progress-info">
                      <span class="progress-text">学习进度：{{ course.progress }}%</span>
                      <span class="progress-detail">{{ course.completedLessons }}/{{ course.totalLessons }} 课时</span>
                    </div>
                    <el-progress
                        :percentage="course.progress"
                        :stroke-width="8"
                        :show-text="false"
                    />
                  </div>

                  <div class="course-meta">
                    <div class="meta-item">
                      <font-awesome-icon :icon="['fas', 'clock']" class="text-secondary"/>
                      <span>总时长：{{ course.duration }}</span>
                    </div>
                    <div class="meta-item">
                      <font-awesome-icon :icon="['fas', 'calendar']" class="text-primary"/>
                      <span>最近学习：{{ course.lastStudyTime }}</span>
                    </div>
                    <div class="meta-item">
                      <font-awesome-icon :icon="['fas', 'shopping-cart']" class="text-secondary"/>
                      <span>购买时间：{{ course.purchaseDate }}</span>
                    </div>
                  </div>
                </div>

                <div class="course-actions">
                  <el-button
                      type="primary"
                      @click.stop="continueLearning(course)"
                  >
                    <font-awesome-icon :icon="['fas', 'play']" class="mr-xs"/>
                    {{ course.status === 'not_started' ? '开始学习' : '继续学习' }}
                  </el-button>

                  <el-dropdown @command="handleCourseAction">
                    <el-button circle>
                      <font-awesome-icon :icon="['fas', 'ellipsis-v']"/>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item :command="{ action: 'detail', course }">
                          <font-awesome-icon :icon="['fas', 'info-circle']" class="mr-xs"/>
                          课程详情
                        </el-dropdown-item>
                        <el-dropdown-item :command="{ action: 'download', course }">
                          <font-awesome-icon :icon="['fas', 'download']" class="mr-xs"/>
                          下载资料
                        </el-dropdown-item>
                        <el-dropdown-item :command="{ action: 'certificate', course }"
                                          v-if="course.status === 'completed'">
                          <font-awesome-icon :icon="['fas', 'certificate']" class="mr-xs"/>
                          查看证书
                        </el-dropdown-item>
                        <el-dropdown-item :command="{ action: 'review', course }" divided>
                          <font-awesome-icon :icon="['fas', 'star']" class="mr-xs"/>
                          评价课程
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="filteredCourses.length === 0" class="empty-state neumorphism-card">
              <div class="empty-icon">
                <font-awesome-icon :icon="['fas', 'book-open']" class="text-6xl text-muted"/>
              </div>
              <h3 class="empty-title">暂无课程</h3>
              <p class="empty-description">
                {{ searchQuery ? '没有找到匹配的课程' : '您还没有购买任何课程' }}
              </p>
              <el-button
                  type="primary"
                  @click="$router.push('/index/courses')"
                  class="neumorphism-button"
              >
                <font-awesome-icon :icon="['fas', 'search']" class="mr-xs"/>
                去发现课程
              </el-button>
            </div>

            <!-- 分页 -->
            <div v-if="filteredCourses.length > 0" class="pagination-wrapper">
              <el-pagination
                  v-model:current-page="currentPage"
                  :page-size="pageSize"
                  :total="filteredCourses.length"
                  layout="prev, pager, next, jumper"
                  class="custom-pagination"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, reactive, computed, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import SearchBar from '@/components/SearchBar.vue'
import {getMyCourseList} from '@/api/students/stuAPI.js'

const router = useRouter()

// 响应式数据
const searchQuery = ref('')
const viewMode = ref('grid')
const currentPage = ref(1)
const pageSize = ref(12)

// 筛选条件
const filters = reactive({
  status: 'all',
  category: '',
  sortBy: 'recent'
})

// 统计数据
const stats = reactive({
  totalCourses: 8,
  completedCourses: 3,
  inProgressCourses: 4,
  totalHours: '156h'
})

// 分类选项
const categories = ref([
  {label: '前端开发', value: 'frontend'},
  {label: '后端开发', value: 'backend'},
  {label: '移动开发', value: 'mobile'},
  {label: '数据科学', value: 'data'},
  {label: '人工智能', value: 'ai'},
  {label: '设计', value: 'design'}
])

// 课程数据
const courses = ref([])

// 计算属性
const filteredCourses = computed(() => {
  let result = courses.value

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(course =>
        course.title.toLowerCase().includes(query) ||
        course.instructor.name.toLowerCase().includes(query)
    )
  }

  // 状态过滤
  if (filters.status !== 'all') {
    result = result.filter(course => course.status === filters.status)
  }

  // 分类过滤
  if (filters.category) {
    result = result.filter(course => course.category === filters.category)
  }

  // 排序
  result.sort((a, b) => {
    switch (filters.sortBy) {
      case 'recent':
        return new Date(b.purchaseDate) - new Date(a.purchaseDate)
      case 'purchase_date':
        return new Date(b.purchaseDate) - new Date(a.purchaseDate)
      case 'name':
        return a.title.localeCompare(b.title)
      case 'progress':
        return b.progress - a.progress
      default:
        return 0
    }
  })

  return result
})

const paginatedCourses = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredCourses.value.slice(start, end)
})

// 方法
const getStatusText = (status) => {
  const statusMap = {
    not_started: '未开始',
    in_progress: '学习中',
    completed: '已完成',
    start_expired: '已过期'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const typeMap = {
    not_started: 'info',
    in_progress: 'warning',
    completed: 'success',
    start_expired: 'danger'
  }
  return typeMap[status] || 'info'
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleFilterChange = () => {
  currentPage.value = 1
}

const resetFilters = () => {
  searchQuery.value = ''
  filters.status = 'all'
  filters.category = ''
  filters.sortBy = 'recent'
  currentPage.value = 1
}

const goToCourse = (course) => {
  router.push(`/index/courses/${course.id}`)
}

const continueLearning = (course) => {
  if (course.status === 'not_started') {
    ElMessage.success(`开始学习《${course.title}》`)
  } else {
    ElMessage.success(`继续学习《${course.title}》`)
  }
  router.push(`/index/learn/${course.id}`)
}

const handleCourseAction = ({action, course}) => {
  switch (action) {
    case 'detail':
      router.push(`/index/courses/${course.id}`)
      break
    case 'download':
      ElMessage.success('开始下载课程资料...')
      break
    case 'certificate':
      ElMessage.success('查看学习证书...')
      break
    case 'review':
      ElMessage.info('评价功能开发中...')
      break
    default:
      break
  }
}

// 加载我的课程列表
async function loadMyCourses() {
  try {
    const courseList = await getMyCourseList();
    if (courseList && Array.isArray(courseList)) {
      // 将后端数据转换为前端需要的格式
      courses.value = courseList.map(course => ({
        id: course.courseId,
        title: course.courseTitle,
        cover: course.courseCover || '/images/courses/default.jpg',
        instructor: {
          name: course.teacherName,
          avatar: '/api/placeholder/40/40'
        },
        category: course.categoryName || 'general',
        duration: '待计算', // 课程时长
        progress: course.progressPercentage || 0, // 学习进度
        completedLessons: 0, // 已完成课时数
        totalLessons: course.totalSections || 0, // 总课时数
        status: course.courseStatus || 'not_started',
        lastStudyTime: '未开始',
        purchaseDate: course.enrollmentDate ? new Date(course.enrollmentDate).toLocaleDateString() : '未知'
      }));

      // 更新统计数据
      stats.totalCourses = courses.value.length;
      stats.completedCourses = courses.value.filter(c => c.status === 'completed').length;
      stats.inProgressCourses = courses.value.filter(c => c.status === 'in_progress').length;
    }
  } catch (error) {
    console.error('获取我的课程列表失败:', error);
    ElMessage.error('获取我的课程列表失败');
  }
}

// 生命周期
onMounted(async () => {
  await loadMyCourses();
})
</script>

<style scoped>

/* 通用区域样式 */
.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
}

.my-courses-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #F0F0F3 0%, #E8E8EB 50%, #F0F0F3 100%);
  position: relative;
}

.my-courses-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 80%, rgba(0, 47, 167, 0.03) 0%, transparent 50%),
  radial-gradient(circle at 80% 20%, rgba(81, 123, 77, 0.03) 0%, transparent 50%),
  radial-gradient(circle at 40% 40%, rgba(103, 194, 58, 0.02) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg,
  rgba(255, 255, 255, 0.4) 0%,
  rgba(0, 47, 167, 0.05) 50%,
  rgba(81, 123, 77, 0.05) 100%);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: var(--spacing-4xl) 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 1;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-xl);
  position: relative;
  z-index: 2;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: var(--spacing-sm);
  position: relative;
}

.page-title::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 60px;
  height: 4px;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  border-radius: 2px;
}

.page-subtitle {
  font-size: 1.125rem;
  color: #606266;
  margin: 0;
  font-weight: 400;
  line-height: 1.5;
}

.header-stats {
  display: flex;
  gap: var(--spacing-lg);
}

.stat-card {
  text-align: center;
  padding: var(--spacing-lg) var(--spacing-xl);
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--border-radius-xl);
  min-width: 120px;
  position: relative;
  overflow: hidden;
  transition: all var(--transition-normal);
  box-shadow: 12px 12px 24px rgba(0, 47, 167, 0.08),
  -6px -6px 12px rgba(255, 255, 255, 0.25);
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
  box-shadow: 20px 20px 40px rgba(0, 47, 167, 0.15),
  -10px -10px 20px rgba(255, 255, 255, 0.35);
}

.stat-value {
  font-size: 2rem;
  font-weight: 800;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: block;
  margin-bottom: var(--spacing-xs);
  position: relative;
  z-index: 1;
}

.stat-label {
  font-size: 0.875rem;
  color: #606266;
  font-weight: 500;
  position: relative;
  z-index: 1;
}

/* 页面内容 */
.page-content {
  padding: var(--spacing-4xl) 0;
  position: relative;
  z-index: 1;
}

.content-layout {
  display: flex;
  gap: 1.5rem;;
  min-height: calc(100vh - 250px);
  position: relative;
  z-index: 1;
  padding-left: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

/* 筛选区域 */
.filters-section {
  width: 260px;
  min-width: 260px;
  background: rgba(240, 240, 243, 0.25);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 24px;
  padding: 0;
  height: fit-content;
  box-shadow: 8px 8px 32px rgba(0, 47, 167, 0.1),
  inset 1px 1px 0 rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
  box-shadow: 8px 8px 32px rgba(0, 47, 167, 0.1),
  inset 1px 1px 0 rgba(255, 255, 255, 0.2);
}

.filters-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg,
  rgba(0, 47, 167, 0.02) 0%,
  rgba(81, 123, 77, 0.01) 50%,
  rgba(103, 194, 58, 0.02) 100%);
  pointer-events: none;
}

.filters-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
  padding: var(--spacing-xl) var(--spacing-xl) var(--spacing-lg);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.05);
}

.filters-title {
  font-size: 1.25rem;
  font-weight: 700;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  position: relative;
}

.reset-btn {
  font-size: 0.9rem;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  color: #4a5568;
  font-weight: 600;
  padding: 0.75rem 1.5rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  box-shadow: inset 2px 2px 6px rgba(0, 47, 167, 0.05),
  inset -1px -1px 3px rgba(255, 255, 255, 0.1);
  letter-spacing: 0.5px;
  z-index: 1;
}

.reset-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
  transparent 0%,
  rgba(255, 107, 107, 0.15) 50%,
  transparent 100%);
  transition: left 0.6s ease;
}

.reset-btn:hover {
  background: rgba(255, 107, 107, 0.12);
  border-color: rgba(255, 107, 107, 0.25);
  color: #e53e3e;
  transform: translateX(-4px) scale(1.02);
  box-shadow: -6px 6px 16px rgba(255, 107, 107, 0.15),
  3px -3px 8px rgba(255, 255, 255, 0.3),
  inset 2px 2px 8px rgba(255, 107, 107, 0.08);
}

.reset-btn:hover::before {
  left: 100%;
}

.reset-btn:active {
  transform: translateX(-2px) scale(0.98);
  box-shadow: -3px 3px 8px rgba(255, 107, 107, 0.2),
  2px -2px 4px rgba(255, 255, 255, 0.4),
  inset 3px 3px 10px rgba(255, 107, 107, 0.1);
}

.filters-content {
  display: flex;
  flex-direction: column;
  gap: 0;
  position: relative;
  z-index: 1;
  padding: 0;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.filter-group:last-child {
  border-bottom: none;
}

.filter-group::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.02);
  opacity: 0;
  transition: opacity var(--transition-normal);
  pointer-events: none;
}

.filter-group:hover::before {
  opacity: 1;
}

.filter-label {
  font-size: 0.9375rem;
  font-weight: 600;
  color: #303133;
  margin-bottom: var(--spacing-xs);
  position: relative;
}

.filter-label::before {
  content: '';
  position: absolute;
  left: -12px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  border-radius: 2px;
}

.filter-options {
  width: 100%;
}

.filter-options .el-radio-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.filter-options .el-radio-button {
  margin: 0;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  cursor: pointer;
  color: #4a5568;
  font-weight: 500;
  letter-spacing: 0.3px;
  padding: 0.75rem 1rem;
}

.filter-options .el-radio-button::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  transform: scaleY(0);
  transition: transform 0.3s ease;
  border-radius: 0 2px 2px 0;
}

.filter-options .el-radio-button:hover {
  background: rgba(0, 47, 167, 0.08);
  color: #002FA7;
  transform: translateX(8px);
  box-shadow: 8px 8px 16px rgba(0, 47, 167, 0.1),
  -4px -4px 8px rgba(255, 255, 255, 0.3);
}

.filter-options .el-radio-button:hover::before {
  transform: scaleY(1);
}

.filter-options .el-radio-button.is-active {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.15) 0%, rgba(81, 123, 77, 0.1) 100%);
  color: #002FA7;
  font-weight: 600;
  box-shadow: inset 4px 4px 8px rgba(0, 47, 167, 0.1),
  inset -2px -2px 4px rgba(255, 255, 255, 0.2),
  0 4px 15px rgba(0, 47, 167, 0.2);
}

.filter-options .el-radio-button.is-active::before {
  transform: scaleY(1);
}

/* 选择器样式优化 */
.filter-options .el-select {
  width: 100%;
}

.filter-options .el-select .el-input__wrapper {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: none;
  padding: 0.5rem 1rem;
}

.filter-options .el-select .el-input__wrapper:hover {
  background: rgba(0, 47, 167, 0.08);
  border-color: rgba(0, 47, 167, 0.2);
  transform: translateX(4px);
  box-shadow: 4px 4px 12px rgba(0, 47, 167, 0.08),
  -2px -2px 6px rgba(255, 255, 255, 0.2);
}

.filter-options .el-select .el-input__wrapper.is-focus {
  background: rgba(0, 47, 167, 0.1);
  border-color: #002FA7;
  transform: translateX(6px);
  box-shadow: 6px 6px 16px rgba(0, 47, 167, 0.12),
  -3px -3px 8px rgba(255, 255, 255, 0.25);
}

.filter-options .el-select .el-input__inner {
  color: #4a5568;
  font-weight: 500;
  background: transparent;
  border: none;
  box-shadow: none;
}

.filter-options .el-select .el-input__inner::placeholder {
  color: #a0aec0;
  font-weight: 400;
}

/* 搜索框样式优化 */
.search-group {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 20px;
  position: relative;
}

.search-group::before {
  background: linear-gradient(135deg,
  rgba(0, 47, 167, 0.05) 0%,
  rgba(81, 123, 77, 0.03) 100%);
}

.search-wrapper {
  position: relative;
  z-index: 1;
}

.custom-search-bar {
  width: 100%;
}

.custom-search-bar .el-input__wrapper {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 18px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: inset 2px 2px 6px rgba(0, 47, 167, 0.05),
  inset -1px -1px 3px rgba(255, 255, 255, 0.1);
  padding: 0.75rem 1.25rem;
  position: relative;
  overflow: hidden;
}

.custom-search-bar .el-input__wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
  transparent 0%,
  rgba(0, 47, 167, 0.1) 50%,
  transparent 100%);
  transition: left 0.6s ease;
}

.custom-search-bar .el-input__wrapper:hover {
  background: rgba(0, 47, 167, 0.12);
  border-color: rgba(0, 47, 167, 0.25);
  transform: translateX(6px) scale(1.02);
  box-shadow: 8px 8px 20px rgba(0, 47, 167, 0.12),
  -4px -4px 10px rgba(255, 255, 255, 0.3),
  inset 2px 2px 8px rgba(0, 47, 167, 0.08);
}

.custom-search-bar .el-input__wrapper:hover::before {
  left: 100%;
}

.custom-search-bar .el-input__wrapper.is-focus {
  background: rgba(0, 47, 167, 0.15);
  border-color: #002FA7;
  transform: translateX(8px) scale(1.03);
  box-shadow: 12px 12px 24px rgba(0, 47, 167, 0.18),
  -6px -6px 12px rgba(255, 255, 255, 0.35),
  inset 3px 3px 10px rgba(0, 47, 167, 0.1),
  0 0 0 3px rgba(0, 47, 167, 0.1);
}

.custom-search-bar .el-input__inner {
  color: #4a5568;
  font-weight: 500;
  background: transparent;
  border: none;
  box-shadow: none;
  font-size: 0.95rem;
  letter-spacing: 0.3px;
}

.custom-search-bar .el-input__inner::placeholder {
  color: #a0aec0;
  font-weight: 400;
  font-style: italic;
}

.custom-search-bar .el-input__prefix,
.custom-search-bar .el-input__suffix {
  color: #002FA7;
  transition: all 0.3s ease;
}

.custom-search-bar .el-input__wrapper:hover .el-input__prefix,
.custom-search-bar .el-input__wrapper:hover .el-input__suffix {
  color: #517B4D;
  transform: scale(1.1);
}

.custom-search-bar .el-input__wrapper.is-focus .el-input__prefix,
.custom-search-bar .el-input__wrapper.is-focus .el-input__suffix {
  color: #002FA7;
  transform: scale(1.15);
}

/* 下拉选择框样式优化 */
.filter-options .el-select .el-select__wrapper {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: inset 2px 2px 6px rgba(0, 47, 167, 0.05),
  inset -1px -1px 3px rgba(255, 255, 255, 0.1);
  position: relative;
  overflow: hidden;
}

.filter-options .el-select .el-select__wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
  transparent 0%,
  rgba(0, 47, 167, 0.08) 50%,
  transparent 100%);
  transition: left 0.6s ease;
}

.filter-options .el-select .el-select__wrapper:hover {
  background: rgba(0, 47, 167, 0.12);
  border-color: rgba(0, 47, 167, 0.25);
  transform: translateX(4px) scale(1.01);
  box-shadow: 6px 6px 16px rgba(0, 47, 167, 0.1),
  -3px -3px 8px rgba(255, 255, 255, 0.25),
  inset 2px 2px 6px rgba(0, 47, 167, 0.06);
}

.filter-options .el-select .el-select__wrapper:hover::before {
  left: 100%;
}

.filter-options .el-select .el-select__wrapper.is-focused {
  background: rgba(0, 47, 167, 0.15);
  border-color: #002FA7;
  transform: translateX(6px) scale(1.02);
  box-shadow: 8px 8px 20px rgba(0, 47, 167, 0.15),
  -4px -4px 10px rgba(255, 255, 255, 0.3),
  inset 2px 2px 8px rgba(0, 47, 167, 0.08),
  0 0 0 2px rgba(0, 47, 167, 0.1);
}

.filter-options .el-select .el-select__suffix {
  color: #002FA7;
  transition: all 0.3s ease;
}

.filter-options .el-select .el-select__wrapper:hover .el-select__suffix {
  color: #517B4D;
  transform: scale(1.1) rotate(180deg);
}

.filter-options .el-select .el-select__wrapper.is-focused .el-select__suffix {
  color: #002FA7;
  transform: scale(1.15) rotate(180deg);
}

/* 下拉菜单弹出层样式 */
.el-select-dropdown {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 47, 167, 0.15),
  0 8px 16px rgba(0, 0, 0, 0.1),
  inset 0 1px 0 rgba(255, 255, 255, 0.3);
  padding: 8px;
  margin-top: 8px;
  animation: dropdownSlideIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes dropdownSlideIn {
  from {
    opacity: 0;
    transform: translateY(-10px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.el-select-dropdown .el-select-dropdown__item {
  background: transparent;
  color: #4a5568;
  font-weight: 500;
  padding: 12px 16px;
  margin: 2px 0;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.el-select-dropdown .el-select-dropdown__item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
  transparent 0%,
  rgba(0, 47, 167, 0.1) 50%,
  transparent 100%);
  transition: left 0.5s ease;
}

.el-select-dropdown .el-select-dropdown__item:hover {
  background: rgba(0, 47, 167, 0.08);
  color: #002FA7;
  transform: translateX(4px);
  box-shadow: 4px 4px 12px rgba(0, 47, 167, 0.1),
  -2px -2px 6px rgba(255, 255, 255, 0.5);
}

.el-select-dropdown .el-select-dropdown__item:hover::before {
  left: 100%;
}

.el-select-dropdown .el-select-dropdown__item.is-selected {
  background: linear-gradient(135deg,
  rgba(0, 47, 167, 0.15) 0%,
  rgba(81, 123, 77, 0.1) 100%);
  color: #002FA7;
  font-weight: 600;
  transform: translateX(6px);
  box-shadow: 6px 6px 16px rgba(0, 47, 167, 0.15),
  -3px -3px 8px rgba(255, 255, 255, 0.6),
  inset 2px 2px 6px rgba(0, 47, 167, 0.05);
}

.el-select-dropdown .el-select-dropdown__item.is-selected::after {
  content: '✓';
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #517B4D;
  font-weight: bold;
  font-size: 14px;
}

/* 筛选区域整体动画效果 */
.filters-section {
  animation: filterSlideIn 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes filterSlideIn {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 筛选项交错动画 */
.filter-group:nth-child(1) {
  animation-delay: 0.1s;
}

.filter-group:nth-child(2) {
  animation-delay: 0.2s;
}

.filter-group:nth-child(3) {
  animation-delay: 0.3s;
}

.filter-group:nth-child(4) {
  animation-delay: 0.4s;
}

.filter-group:nth-child(5) {
  animation-delay: 0.5s;
}

.filter-group {
  animation: filterGroupSlideIn 0.5s cubic-bezier(0.4, 0, 0.2, 1) both;
}

@keyframes filterGroupSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 大屏幕优化 */
@media (min-width: 1400px) {
  .content-layout {
    max-width: 1600px;
    padding-left: 3rem;
    gap: 4rem;
  }

  .courses-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 2rem;
  }
}

/* 筛选区域响应式优化 */
@media (max-width: 1200px) {
  .content-layout {
    padding-left: 1rem;
    gap: 2rem;
  }

  .filters-section {
    width: 240px;
    min-width: 240px;
    padding: 1.5rem;
  }

  .filter-group {
    padding: 1rem 1.25rem;
  }

  .custom-search-bar .el-input__wrapper {
    padding: 0.625rem 1rem;
  }

  .courses-grid {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  }
}

@media (max-width: 768px) {
  .content-layout {
    flex-direction: column;
    gap: 1.5rem;
    padding-left: 1rem;
    padding-right: 1rem;
  }

  .filters-section {
    position: static;
    width: 100%;
    min-width: auto;
    margin-bottom: 0;
    border-radius: 16px;
  }

  .courses-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }

  .filter-group {
    padding: 0.875rem 1rem;
  }

  .filters-header {
    padding: 1rem 1.25rem;
  }

  .reset-btn {
    padding: 0.625rem 1.25rem;
    font-size: 0.85rem;
  }
}

@media (max-width: 480px) {
  .filters-section {
    padding: 1rem;
  }

  .filter-group {
    padding: 0.75rem;
  }

  .filters-header {
    padding: 0.875rem 1rem;
  }

  .filter-label {
    font-size: 0.875rem;
  }

  .custom-search-bar .el-input__wrapper {
    padding: 0.5rem 0.875rem;
  }

  .reset-btn {
    padding: 0.5rem 1rem;
    font-size: 0.8rem;
  }
}

/* 课程区域 */
.courses-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.view-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg) var(--spacing-xl);
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--border-radius-xl);
  box-shadow: 12px 12px 24px rgba(0, 47, 167, 0.08),
  -6px -6px 12px rgba(255, 255, 255, 0.25);
  position: relative;
  overflow: hidden;
}

.view-controls::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg,
  rgba(255, 255, 255, 0.1) 0%,
  transparent 50%,
  rgba(0, 47, 167, 0.03) 100%);
  pointer-events: none;
}

.course-count {
  font-size: 0.9375rem;
  color: #606266;
  font-weight: 500;
  position: relative;
  z-index: 1;
}

.view-actions {
  position: relative;
  z-index: 1;
}

/* 网格视图 */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.course-card {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--border-radius-2xl);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;
  z-index: 1;
  box-shadow: 16px 16px 32px rgba(0, 47, 167, 0.08),
  -8px -8px 16px rgba(255, 255, 255, 0.25);
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
  box-shadow: 24px 24px 48px rgba(0, 47, 167, 0.15),
  -12px -12px 24px rgba(255, 255, 255, 0.35);
}

.course-cover {
  position: relative;
  aspect-ratio: 16/9;
  overflow: hidden;
  border-radius: var(--border-radius-xl) var(--border-radius-xl) 0 0;
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-normal);
}

.course-card:hover .course-cover img {
  transform: scale(1.08);
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
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.course-card:hover .course-overlay {
  opacity: 1;
}

.course-actions .el-button {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: #002FA7;
  font-size: 1.25rem;
  transition: all var(--transition-normal);
  box-shadow: 8px 8px 16px rgba(0, 47, 167, 0.2),
  -4px -4px 8px rgba(255, 255, 255, 0.3);
}

.course-actions .el-button:hover {
  transform: scale(1.1);
  background: rgba(255, 255, 255, 1);
  box-shadow: 12px 12px 24px rgba(0, 47, 167, 0.3),
  -6px -6px 12px rgba(255, 255, 255, 0.4);
}

.course-status {
  position: absolute;
  top: var(--spacing-md);
  right: var(--spacing-md);
  z-index: 2;
}

.course-status .el-tag {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: var(--border-radius-md);
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.course-info {
  padding: var(--spacing-xl);
  position: relative;
  z-index: 1;
}

.course-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #303133;
  margin-bottom: var(--spacing-sm);
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color var(--transition-normal);
}

.course-card:hover .course-title {
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.course-instructor {
  font-size: 0.9375rem;
  color: #606266;
  margin-bottom: var(--spacing-lg);
  font-weight: 500;
}

.course-progress {
  margin-bottom: var(--spacing-lg);
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-md);
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
  font-size: 0.875rem;
}

.progress-text {
  color: #606266;
  font-weight: 500;
}

.progress-percent {
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
}

.course-progress .el-progress {
  margin-top: var(--spacing-xs);
}

.course-progress .el-progress__bar {
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius-sm);
  overflow: hidden;
}

.course-progress .el-progress-bar__outer {
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius-sm);
}

.course-progress .el-progress-bar__inner {
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  border-radius: var(--border-radius-sm);
  transition: all var(--transition-normal);
}

.course-stats {
  display: flex;
  justify-content: space-between;
  font-size: 0.8125rem;
  gap: var(--spacing-sm);
}

.stat {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: #909399;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-md);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all var(--transition-normal);
}

.stat:hover {
  background: rgba(255, 255, 255, 0.3);
  color: #606266;
}

.stat svg {
  color: #002FA7;
  font-size: 0.75rem;
}

/* 列表视图 */
.courses-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.course-item {
  display: flex;
  gap: var(--spacing-xl);
  padding: var(--spacing-xl);
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--border-radius-2xl);
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
  box-shadow: 16px 16px 32px rgba(0, 47, 167, 0.08),
  -8px -8px 16px rgba(255, 255, 255, 0.25);
}

.course-item::before {
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

.course-item:hover {
  transform: translateY(-8px);
  box-shadow: 24px 24px 48px rgba(0, 47, 167, 0.15),
  -12px -12px 24px rgba(255, 255, 255, 0.35);
}

.course-item .course-cover {
  width: 220px;
  aspect-ratio: 16/9;
  border-radius: var(--border-radius-xl);
  overflow: hidden;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  box-shadow: 8px 8px 16px rgba(0, 47, 167, 0.1),
  -4px -4px 8px rgba(255, 255, 255, 0.2);
}

.course-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
  position: relative;
  z-index: 1;
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.course-main .course-title {
  font-size: 1.375rem;
  margin-bottom: var(--spacing-sm);
  font-weight: 700;
}

.course-main .course-instructor {
  margin: 0;
  font-size: 1rem;
  color: #606266;
}

.course-item .course-progress {
  margin: 0;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-md);
}

.progress-detail {
  color: #909399;
  font-size: 0.8125rem;
  font-weight: 500;
}

.course-meta {
  display: flex;
  gap: var(--spacing-xl);
  font-size: 0.9375rem;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: #606266;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-md);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all var(--transition-normal);
}

.meta-item:hover {
  background: rgba(255, 255, 255, 0.25);
  color: #303133;
}

.meta-item svg {
  color: #002FA7;
  font-size: 0.875rem;
}

.course-actions {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
  align-items: center;
  justify-content: center;
  padding-left: var(--spacing-xl);
  border-left: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 1;
}

.course-actions .el-button {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: var(--border-radius-lg);
  transition: all var(--transition-normal);
  font-weight: 600;
}

.course-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 8px 8px 16px rgba(0, 47, 167, 0.1),
  -4px -4px 8px rgba(255, 255, 255, 0.3);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: var(--spacing-4xl);
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius-2xl);
  margin: var(--spacing-xl) 0;
  position: relative;
  overflow: hidden;
  box-shadow: 16px 16px 32px rgba(0, 47, 167, 0.08),
  -8px -8px 16px rgba(255, 255, 255, 0.25);
}

.empty-state::before {
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

.empty-icon {
  margin-bottom: var(--spacing-xl);
  color: #002FA7;
  opacity: 0.6;
  position: relative;
  z-index: 1;
}

.empty-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: var(--spacing-md);
  position: relative;
  z-index: 1;
  background: linear-gradient(135deg, #002FA7, #1890FF);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.empty-description {
  color: #606266;
  margin-bottom: var(--spacing-lg);
  line-height: 1.6;
  font-size: 1rem;
  position: relative;
  z-index: 1;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--spacing-3xl);
  padding: var(--spacing-xl) 0;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: var(--border-radius-xl);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.pagination-wrapper .el-pagination {
  background: transparent;
}

.pagination-wrapper .el-pagination .el-pager li {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius-md);
  margin: 0 var(--spacing-xs);
  transition: all var(--transition-normal);
}

.pagination-wrapper .el-pagination .el-pager li:hover,
.pagination-wrapper .el-pagination .el-pager li.is-active {
  background: rgba(0, 47, 167, 0.8);
  color: white;
  transform: translateY(-2px);
  box-shadow: 4px 4px 8px rgba(0, 47, 167, 0.2),
  -2px -2px 4px rgba(255, 255, 255, 0.3);
}

.pagination-wrapper .el-pagination .btn-prev,
.pagination-wrapper .el-pagination .btn-next {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius-md);
  transition: all var(--transition-normal);
}

.pagination-wrapper .el-pagination .btn-prev:hover,
.pagination-wrapper .el-pagination .btn-next:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.custom-pagination {
  --el-pagination-button-color: var(--text-secondary);
  --el-pagination-hover-color: var(--primary-color);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content-layout {
    grid-template-columns: 280px 1fr;
    gap: var(--spacing-lg);
  }

  .courses-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-lg);
  }

  .course-item {
    padding: var(--spacing-lg);
  }
}

@media (max-width: 1024px) {
  .content-layout {
    grid-template-columns: 250px 1fr;
    gap: var(--spacing-lg);
  }

  .header-stats {
    flex-wrap: wrap;
    gap: var(--spacing-md);
  }

  .courses-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 768px) {
  .content-layout {
    grid-template-columns: 1fr;
  }

  .filters-section {
    position: static;
    order: 1;
    margin: var(--spacing-lg);
  }

  .courses-section {
    order: 2;
  }

  .page-header {
    padding: var(--spacing-xl) 0;
    background: linear-gradient(135deg,
    rgba(0, 47, 167, 0.15) 0%,
    rgba(24, 144, 255, 0.1) 50%,
    rgba(255, 255, 255, 0.05) 100%);
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-lg);
  }

  .header-stats {
    width: 100%;
    justify-content: space-between;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-md);
  }

  .stat-card {
    flex: 1;
    min-width: auto;
  }

  .courses-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg);
  }

  .course-item {
    flex-direction: column;
    gap: var(--spacing-lg);
    padding: var(--spacing-lg);
  }

  .course-item .course-cover {
    width: 100%;
    max-width: 320px;
    margin: 0 auto;
  }

  .course-actions {
    flex-direction: row;
    justify-content: center;
    padding-left: 0;
    border-left: none;
    border-top: 1px solid rgba(255, 255, 255, 0.2);
    padding-top: var(--spacing-lg);
    gap: var(--spacing-md);
  }

  .empty-state {
    padding: var(--spacing-2xl) var(--spacing-lg);
    margin: var(--spacing-lg) 0;
  }

  .pagination-wrapper {
    margin-top: var(--spacing-xl);
    padding: var(--spacing-lg) var(--spacing-sm);
  }
}

@media (max-width: 480px) {
  .page-header,
  .page-content {
    padding: var(--spacing-lg) 0;
  }

  .page-title {
    font-size: 1.5rem;
  }

  .header-stats {
    grid-template-columns: 1fr;
    gap: var(--spacing-sm);
  }

  .stat-card {
    padding: var(--spacing-md);
  }

  .filters-section {
    padding: var(--spacing-md);
    margin: var(--spacing-md);
  }

  .view-controls {
    padding: var(--spacing-md);
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: stretch;
  }

  .course-info {
    padding: var(--spacing-md);
  }

  .course-item {
    padding: var(--spacing-md);
  }

  .course-item .course-cover {
    max-width: 280px;
  }

  .course-main .course-title {
    font-size: 1.25rem;
  }

  .courses-grid {
    grid-template-columns: 1fr;
  }

  .filter-options .el-radio-group {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .empty-state {
    padding: var(--spacing-xl) var(--spacing-md);
  }

  .empty-icon {
    font-size: 4rem;
  }

  .empty-title {
    font-size: 1.25rem;
  }
}
</style>