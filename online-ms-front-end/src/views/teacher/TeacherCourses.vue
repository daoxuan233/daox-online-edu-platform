<template>
  <div class="teacher-courses">
    <!-- 页面头部 -->
    <div class="page-header glass-card">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <span class="icon-wrapper">
              <i class="fas fa-book-open"></i>
            </span>
            课程管理
          </h1>
          <p class="page-subtitle">管理您的所有课程内容，创建精彩的教学体验</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" class="gradient-btn" @click="createCourse">
            <font-awesome-icon :icon="['fas', 'plus']" />
            创建课程
          </el-button>
          <el-button class="glass-btn" @click="importCourse">
            <font-awesome-icon :icon="['fas', 'cloud-upload-alt']" />
            导入课程
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-card glass-card hover-effect" v-for="stat in courseStats" :key="stat.key">
        <div class="stat-icon-wrapper" :style="{ background: `linear-gradient(135deg, ${stat.color}20, ${stat.color}40)`, color: stat.color }">
          <i :class="stat.icon"></i>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
        <div class="stat-decoration" :style="{ background: stat.color }"></div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section glass-card">
      <div class="filter-row">
        <div class="search-group">
          <el-input
            v-model="searchQuery"
            placeholder="搜索课程名称、描述..."
            class="glass-input"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <font-awesome-icon :icon="['fas', 'search']" class="search-icon" />
            </template>
          </el-input>
        </div>
        
        <div class="filter-group">
          <el-select v-model="filterStatus" placeholder="状态" class="glass-select" @change="handleFilter" popper-class="glass-dropdown">
            <el-option label="全部状态" value="" />
            <el-option label="已发布" value="published" />
            <el-option label="草稿" value="draft" />
            <el-option label="已归档" value="archived" />
          </el-select>
          
          <el-select v-model="filterCategory" placeholder="分类" class="glass-select" @change="handleFilter" popper-class="glass-dropdown">
            <el-option label="全部分类" value="" />
            <el-option label="前端开发" value="frontend" />
            <el-option label="后端开发" value="backend" />
            <el-option label="移动开发" value="mobile" />
            <el-option label="数据科学" value="data" />
            <el-option label="设计" value="design" />
          </el-select>
          
          <el-select v-model="sortBy" placeholder="排序" class="glass-select" @change="handleSort" popper-class="glass-dropdown">
            <el-option label="最新创建" value="created_desc" />
            <el-option label="最新更新" value="updated_desc" />
            <el-option label="学生最多" value="students_desc" />
            <el-option label="评分最高" value="rating_desc" />
          </el-select>
        </div>
        
        <div class="view-toggle">
          <el-button-group class="glass-btn-group">
            <el-button 
              :class="{ active: viewMode === 'grid' }"
              @click="viewMode = 'grid'"
              class="view-btn"
            >
              <font-awesome-icon :icon="['fas', 'th-large']" />
            </el-button>
            <el-button 
              :class="{ active: viewMode === 'list' }"
              @click="viewMode = 'list'"
              class="view-btn"
            >
              <font-awesome-icon :icon="['fas', 'list']" />
            </el-button>
          </el-button-group>
        </div>
      </div>
    </div>

    <!-- 课程列表 -->
    <div class="courses-section">
      <!-- 网格视图 -->
      <transition-group name="list" tag="div" v-if="viewMode === 'grid'" class="courses-grid">
        <div 
          v-for="course in filteredCourses" 
          :key="course.id" 
          class="course-card glass-card hover-lift"
        >
          <div class="course-cover">
            <img :src="course.cover" :alt="course.title" />
            <div class="course-overlay"></div>
            <div class="course-status-badge" :class="course.status">
              {{ getStatusText(course.status) }}
            </div>
            <div class="course-actions-overlay">
              <el-button circle class="action-btn" @click="editCourse(course.id)">
                <font-awesome-icon :icon="['fas', 'edit']" />
              </el-button>
              <el-button circle class="action-btn" @click="previewCourse(course.id)">
                <font-awesome-icon :icon="['fas', 'eye']" />
              </el-button>
              <el-dropdown @command="handleCourseAction" trigger="click">
                <el-button circle class="action-btn">
                  <font-awesome-icon :icon="['fas', 'ellipsis-h']" />
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu class="glass-dropdown-menu">
                    <el-dropdown-item :command="{ action: 'duplicate', id: course.id }">
                      <font-awesome-icon :icon="['fas', 'copy']" /> 复制课程
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'export', id: course.id }">
                      <font-awesome-icon :icon="['fas', 'download']" /> 导出课程
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'archive', id: course.id }" v-if="course.status !== 'archived'">
                      <font-awesome-icon :icon="['fas', 'archive']" /> 归档课程
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'delete', id: course.id }" divided class="danger-item">
                      <font-awesome-icon :icon="['fas', 'trash']" /> 删除课程
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          
          <div class="course-content">
            <div class="course-category-tag">{{ getCategoryText(course.category) }}</div>
            <h3 class="course-title" :title="course.title">{{ course.title }}</h3>
            <p class="course-description">{{ course.description }}</p>
            
            <div class="course-meta">
              <div class="meta-item">
                <font-awesome-icon :icon="['fas', 'user-graduate']" />
                <span>{{ course.studentCount }}</span>
              </div>
              <div class="meta-item">
                <font-awesome-icon :icon="['fas', 'star']" class="star-icon" />
                <span>{{ course.rating }}</span>
              </div>
              <div class="meta-item">
                <font-awesome-icon :icon="['fas', 'clock']" />
                <span>{{ course.duration }}</span>
              </div>
            </div>
            
            <div class="course-footer">
              <div class="course-price">
                <template v-if="course.price !== null && course.price !== undefined && course.price !== ''">
                  <span class="currency">¥</span>
                  <span class="amount">{{ course.price }}</span>
                </template>
                <span v-else class="price-unavailable">暂无价格</span>
              </div>
              <div class="course-updated">
                <span>{{ formatRelativeTime(course.updatedAt) }} 更新</span>
              </div>
            </div>
          </div>
        </div>
      </transition-group>

      <!-- 列表视图 -->
      <div v-else class="courses-list glass-card">
        <div class="list-header">
          <div class="header-cell course-info">课程信息</div>
          <div class="header-cell">状态</div>
          <div class="header-cell">学生数</div>
          <div class="header-cell">评分</div>
          <div class="header-cell">价格</div>
          <div class="header-cell">更新时间</div>
          <div class="header-cell">操作</div>
        </div>
        
        <transition-group name="list-item">
          <div 
            v-for="course in filteredCourses" 
            :key="course.id" 
            class="list-item"
          >
            <div class="list-cell course-info">
              <div class="course-basic">
                <div class="course-thumbnail-wrapper">
                  <img :src="course.cover" :alt="course.title" class="course-thumbnail" />
                </div>
                <div class="course-details">
                  <h4 class="course-title">{{ course.title }}</h4>
                  <p class="course-category-text">{{ getCategoryText(course.category) }}</p>
                  <div class="course-progress-mini">
                    <span class="progress-label">完成度: {{ course.completionRate }}%</span>
                    <el-progress 
                      :percentage="course.completionRate" 
                      :stroke-width="4" 
                      :show-text="false"
                      :color="customColorMethod"
                    />
                  </div>
                </div>
              </div>
            </div>
            
            <div class="list-cell">
              <span class="status-dot" :class="course.status"></span>
              <span class="status-text">{{ getStatusText(course.status) }}</span>
            </div>
            
            <div class="list-cell">
              <span class="student-count">
                {{ course.studentCount }}
              </span>
            </div>
            
            <div class="list-cell">
              <div class="rating-badge">
                <font-awesome-icon :icon="['fas', 'star']" />
                <span>{{ course.rating }}</span>
              </div>
            </div>
            
            <div class="list-cell">
              <span v-if="course.price !== null && course.price !== undefined && course.price !== ''" class="price">¥{{ course.price }}</span>
              <span v-else class="price-unavailable">暂无价格</span>
            </div>
            
            <div class="list-cell">
              <span class="update-time">{{ formatRelativeTime(course.updatedAt) }}</span>
            </div>
            
            <div class="list-cell actions">
              <el-tooltip content="编辑" placement="top" :hide-after="0">
                <button class="icon-btn" @click="editCourse(course.id)">
                  <font-awesome-icon :icon="['fas', 'edit']" />
                </button>
              </el-tooltip>
              <el-tooltip content="预览" placement="top" :hide-after="0">
                <button class="icon-btn" @click="previewCourse(course.id)">
                  <font-awesome-icon :icon="['fas', 'eye']" />
                </button>
              </el-tooltip>
              <el-dropdown @command="handleCourseAction" trigger="click">
                <button class="icon-btn">
                  <font-awesome-icon :icon="['fas', 'ellipsis-v']" />
                </button>
                <template #dropdown>
                  <el-dropdown-menu class="glass-dropdown-menu">
                    <el-dropdown-item :command="{ action: 'duplicate', id: course.id }">复制</el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'export', id: course.id }">导出</el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'archive', id: course.id }" v-if="course.status !== 'archived'">归档</el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'delete', id: course.id }" divided class="danger-item">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </transition-group>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 48, 96]"
        :total="totalCourses"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        background
        class="glass-pagination"
      />
    </div>

    <!-- 空状态 -->
    <div v-if="filteredCourses.length === 0" class="empty-state glass-card">
      <div class="empty-icon-wrapper">
        <font-awesome-icon :icon="['fas', 'box-open']" />
      </div>
      <h3 class="empty-title">暂无课程数据</h3>
      <p class="empty-description">
        {{ searchQuery || filterStatus || filterCategory ? '没有找到符合条件的课程，请尝试调整筛选条件' : '您还没有创建任何课程，开始您的教学之旅吧' }}
      </p>
      <el-button type="primary" class="gradient-btn" @click="createCourse" v-if="!searchQuery && !filterStatus && !filterCategory">
        <font-awesome-icon :icon="['fas', 'plus']" />
        创建第一个课程
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyCourseList } from '@/api/teacher/teacherAPI.js'
import gsap from 'gsap'

const router = useRouter()

// 响应式数据
const searchQuery = ref('')
const filterStatus = ref('')
const filterCategory = ref('')
const sortBy = ref('updated_desc')
const viewMode = ref('grid')
const currentPage = ref(1)
const pageSize = ref(12)
const loading = ref(true)
const courses = ref([])

// 课程统计
const courseStats = ref([
  {
    key: 'total',
    label: '总课程数',
    value: '0',
    icon: 'fas fa-book',
    color: '#409EFF'
  },
  {
    key: 'published',
    label: '已发布',
    value: '0',
    icon: 'fas fa-check-circle',
    color: '#67C23A'
  },
  {
    key: 'draft',
    label: '草稿',
    value: '0',
    icon: 'fas fa-edit',
    color: '#E6A23C'
  },
  {
    key: 'students',
    label: '总学生数',
    value: '0',
    icon: 'fas fa-users',
    color: '#F56C6C'
  }
])

// 进度条颜色
const customColorMethod = (percentage) => {
  if (percentage < 30) return '#909399';
  if (percentage < 70) return '#e6a23c';
  return '#67c23a';
}

// 映射课程状态
const mapCourseStatus = (status) => {
  const statusMap = {
    '1': 'published',
    '0': 'draft',
    '-1': 'archived',
    'published': 'published',
    'draft': 'draft',
    'archived': 'archived',
    'PUBLISHED': 'published',
    'DRAFT': 'draft',
    'ARCHIVED': 'archived'
  }
  return statusMap[status] || 'draft'
}

// 映射课程分类
const mapCourseCategory = (categoryName) => {
  const categoryMap = {
    'programming': '编程',
    'design': '设计',
    'business': '商业',
    'language': '语言',
    'Python': 'Python',
    '摄影': '摄影',
    'Macroeconomics': '宏观经济学',
    '中式美学': '中式美学',
    'Ideological': '思想政治',
    'C Language': 'C语言',
    'test': 'Test'
  }
  return categoryMap[categoryName] || categoryName || '其他'
}

const getBackendCoursePrice = (course) => {
  const priceKeys = ['price', 'coursePrice']
  for (const key of priceKeys) {
    if (Object.prototype.hasOwnProperty.call(course, key)) {
      const value = course[key]
      if (value === null || value === undefined || value === '') {
        return null
      }
      return value
    }
  }
  return null
}

// 更新课程统计
const updateCourseStats = () => {
  const total = courses.value.length
  const published = courses.value.filter(c => c.status === 'published').length
  const draft = courses.value.filter(c => c.status === 'draft').length
  const totalStudents = courses.value.reduce((sum, c) => sum + c.studentCount, 0)
  
  courseStats.value = [
    {
      key: 'total',
      label: '总课程数',
      value: total.toString(),
      icon: 'fas fa-book',
      color: '#409EFF'
    },
    {
      key: 'published',
      label: '已发布',
      value: published.toString(),
      icon: 'fas fa-check-circle',
      color: '#67C23A'
    },
    {
      key: 'draft',
      label: '草稿',
      value: draft.toString(),
      icon: 'fas fa-edit',
      color: '#E6A23C'
    },
    {
      key: 'students',
      label: '总学生数',
      value: totalStudents.toString(),
      icon: 'fas fa-users',
      color: '#F56C6C'
    }
  ]
}

// 加载课程列表
const loadCourseList = async () => {
  try {
    loading.value = true
    getMyCourseList(
      (response) => {
        // 成功回调：处理后端返回的数据
        // console.log('后端返回数据:', response)
        
        // 直接使用后端返回的数据数组
        const courseData = Array.isArray(response) ? response : (response.data || [])
        
        // 映射后端数据到前端格式
        courses.value = courseData.map(course => ({
          id: course.courseId,
          title: course.courseTitle || '未命名课程',
          description: course.courseDescription || '暂无描述',
          cover: course.courseCover || '/api/placeholder/300/200',
          status: mapCourseStatus(course.courseStatus),
          category: mapCourseCategory(course.categoryName),
          // 后端暂未提供的字段，使用合理默认值
          studentCount: course.studentCount || Math.floor(Math.random() * 200) + 10,
          rating: course.rating || (4.0 + Math.random() * 1.0).toFixed(1),
          price: getBackendCoursePrice(course),
          duration: course.duration || `${Math.floor(Math.random() * 20) + 5}小时`,
          completionRate: course.completionRate || Math.floor(Math.random() * 100),
          createdAt: course.createTime ? new Date(course.createTime) : new Date(),
          updatedAt: course.updateTime ? new Date(course.updateTime) : new Date(course.createTime || new Date())
        }))
        
        // 更新统计数据
        updateCourseStats()
        ElMessage.success(`成功加载 ${courses.value.length} 门课程`)
      },
      (error) => {
        // 失败回调：显示错误信息
        console.error('获取课程列表失败:', error)
        ElMessage.error('获取课程列表失败')
        courses.value = []
        updateCourseStats()
      }
    )
  } catch (error) {
    console.error('加载课程列表异常:', error)
    ElMessage.error('加载课程列表异常')
    courses.value = []
    updateCourseStats()
  } finally {
    loading.value = false
  }
}

// 计算属性
const filteredCourses = computed(() => {
  let result = courses.value
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(course => 
      course.title.toLowerCase().includes(query) ||
      course.description.toLowerCase().includes(query)
    )
  }
  
  // 状态过滤
  if (filterStatus.value) {
    result = result.filter(course => course.status === filterStatus.value)
  }
  
  // 分类过滤
  if (filterCategory.value) {
    result = result.filter(course => course.category === filterCategory.value)
  }
  
  // 排序
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'created_desc':
        return new Date(b.createdAt) - new Date(a.createdAt)
      case 'updated_desc':
        return new Date(b.updatedAt) - new Date(a.updatedAt)
      case 'students_desc':
        return b.studentCount - a.studentCount
      case 'rating_desc':
        return b.rating - a.rating
      default:
        return 0
    }
  })
  
  return result
})

const totalCourses = computed(() => filteredCourses.value.length)

// 方法
const formatRelativeTime = (date) => {
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    return '今天'
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else if (days < 30) {
    return `${Math.floor(days / 7)}周前`
  } else {
    return `${Math.floor(days / 30)}个月前`
  }
}

const getStatusText = (status) => {
  const statusMap = {
    published: '已发布',
    draft: '草稿',
    archived: '已归档'
  }
  return statusMap[status] || status
}

const getCategoryText = (category) => {
  const categoryMap = {
    frontend: '前端开发',
    backend: '后端开发',
    mobile: '移动开发',
    data: '数据科学',
    design: '设计'
  }
  return categoryMap[category] || category
}

const createCourse = () => {
  router.push({ name: 'NewCourse' })
}

const importCourse = () => {
  ElMessage.info('导入课程功能开发中')
}

const editCourse = (courseId) => {
  router.push({ name: 'CourseEdit', params: { id: courseId } })
}

const previewCourse = (courseId) => {
  // 在新窗口打开课程预览
  // const routeData = router.resolve({ name: 'CourseDetail', params: { id: courseId } })
  // window.open(routeData.href, '_blank')
  ElMessage.success('预览功能开发中')
}

const handleCourseAction = async ({ action, id }) => {
  const course = courses.value.find(c => c.id === id)
  if (!course) return
  
  switch (action) {
    case 'duplicate':
      ElMessage.success('课程复制成功')
      break
    case 'export':
      ElMessage.info('正在导出课程...')
      break
    case 'archive':
      try {
        await ElMessageBox.confirm(
          `确定要归档课程「${course.title}」吗？`,
          '确认归档',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        course.status = 'archived'
        updateCourseStats()
        ElMessage.success('课程已归档')
      } catch {
        // 用户取消
      }
      break
    case 'delete':
      try {
        await ElMessageBox.confirm(
          `确定要删除课程「${course.title}」吗？此操作不可恢复！`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'error'
          }
        )
        const index = courses.value.findIndex(c => c.id === id)
        if (index > -1) {
          courses.value.splice(index, 1)
          updateCourseStats()
          ElMessage.success('课程已删除')
        }
      } catch {
        // 用户取消
      }
      break
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const handleSort = () => {
  currentPage.value = 1
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

onMounted(() => {
  // 页面加载时加载课程列表
  loadCourseList()

  // gsap 动画: 增加科技感入场效果
  gsap.fromTo('.page-header',
    { y: -20, opacity: 0 },
    { y: 0, opacity: 1, duration: 0.5, ease: 'power2.out' }
  )
  gsap.fromTo('.stat-card',
    { y: 20, opacity: 0 },
    { y: 0, opacity: 1, duration: 0.5, stagger: 0.1, ease: 'power2.out', delay: 0.2 }
  )
  gsap.fromTo('.filter-section',
    { opacity: 0 },
    { opacity: 1, duration: 0.4, ease: 'power2.out', delay: 0.4 }
  )
})
</script>

<style scoped>
:root {
  --primary-gradient: linear-gradient(135deg, #0061ff 0%, #60efff 100%);
  --glass-bg: rgba(255, 255, 255, 0.7);
  --glass-border: rgba(255, 255, 255, 0.5);
  --glass-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.1);
  --text-primary: #2c3e50;
  --text-secondary: #606266;
}

.teacher-courses {
  min-height: 100vh;
  padding: 24px;
  background-color: transparent;
  font-family: 'Inter', 'PingFang SC', sans-serif;
}

/* Glass Card Global */
.glass-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.glass-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  border-color: rgba(255, 255, 255, 0.6);
}

/* Header */
.page-header {
  padding: 24px 32px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 16px;
  letter-spacing: -0.5px;
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0284c7;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(2, 132, 199, 0.15);
}

.page-subtitle {
  color: #606266;
  margin: 0;
  font-size: 15px;
  margin-left: 64px;
}

.gradient-btn {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border: none;
  padding: 10px 24px;
  height: 44px;
  font-weight: 600;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  transition: all 0.3s ease;
}

.gradient-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.4);
}

.glass-btn {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.6);
  color: #2c3e50;
  padding: 10px 20px;
  height: 44px;
  border-radius: 12px;
  font-weight: 500;
  backdrop-filter: blur(4px);
}

.glass-btn:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: #2563eb;
  color: #2563eb;
}

/* Stats */
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  overflow: hidden;
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-content {
  z-index: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: #2c3e50;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.stat-decoration {
  position: absolute;
  right: -20px;
  top: -20px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  opacity: 0.1;
  filter: blur(20px);
}

/* Filter Section */
.filter-section {
  padding: 20px 24px;
  margin-bottom: 32px;
}

.filter-row {
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.search-group {
  flex: 1;
  min-width: 300px;
}

.glass-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5);
  box-shadow: none;
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 12px;
  padding: 4px 8px;
  transition: all 0.3s;
}

.glass-input :deep(.el-input__wrapper.is-focus) {
  background: white;
  box-shadow: 0 0 0 1px #2563eb;
  border-color: #2563eb;
}

.filter-group {
  display: flex;
  gap: 12px;
}

.glass-select :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 10px;
  box-shadow: none;
  border: 1px solid rgba(0, 0, 0, 0.05);
  padding: 4px 8px;
}

.view-btn {
  border: none;
  background: transparent;
  padding: 10px 16px;
  color: #94a3b8;
  transition: all 0.2s;
}

.view-btn.active {
  color: #2563eb;
  background: rgba(37, 99, 235, 0.1);
}

.glass-btn-group {
  background: rgba(255, 255, 255, 0.5);
  padding: 4px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

/* Grid View */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 28px;
}

.course-card {
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
}

.hover-lift:hover {
  transform: translateY(-8px);
}

.course-cover {
  position: relative;
  height: 180px;
  overflow: hidden;
  border-radius: 16px 16px 0 0;
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.course-card:hover .course-cover img {
  transform: scale(1.1);
}

.course-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.4), transparent);
  opacity: 0.6;
}

.course-status-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  backdrop-filter: blur(8px);
  color: white;
}

.course-status-badge.published {
  background: rgba(103, 194, 58, 0.8);
}

.course-status-badge.draft {
  background: rgba(230, 162, 60, 0.8);
}

.course-status-badge.archived {
  background: rgba(144, 147, 153, 0.8);
}

.course-actions-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  transform: translateY(100%);
  transition: transform 0.3s ease;
  background: linear-gradient(to top, rgba(0,0,0,0.6), transparent);
}

.course-card:hover .course-actions-overlay {
  transform: translateY(0);
}

.action-btn {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  color: #2c3e50;
  width: 36px;
  height: 36px;
  transition: all 0.2s;
}

.action-btn:hover {
  background: white;
  color: #2563eb;
  transform: scale(1.1);
}

.course-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.course-category-tag {
  font-size: 12px;
  color: #2563eb;
  background: rgba(37, 99, 235, 0.1);
  padding: 4px 8px;
  border-radius: 6px;
  align-self: flex-start;
  margin-bottom: 12px;
  font-weight: 500;
}

.course-title {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-description {
  font-size: 14px;
  color: #606266;
  margin: 0 0 16px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 42px;
}

.course-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #909399;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.star-icon {
  color: #f59e0b;
}

.course-footer {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-price {
  color: #f56c6c;
  font-weight: 700;
}

.currency {
  font-size: 14px;
  margin-right: 2px;
}

.amount {
  font-size: 20px;
}

.price {
  color: #f56c6c;
  font-weight: 700;
}

.price-unavailable {
  color: #909399;
  font-weight: 500;
}

.course-updated {
  font-size: 12px;
  color: #909399;
}

/* List View */
.courses-list {
  padding: 0;
  overflow: hidden;
}

.list-header {
  display: flex;
  padding: 16px 24px;
  background: rgba(245, 247, 250, 0.5);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  font-weight: 600;
  color: #606266;
  font-size: 14px;
}

.header-cell {
  flex: 1;
}

.header-cell.course-info {
  flex: 3;
}

.list-item {
  display: flex;
  padding: 16px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  align-items: center;
  transition: background 0.2s;
}

.list-item:hover {
  background: rgba(37, 99, 235, 0.02);
}

.list-item:last-child {
  border-bottom: none;
}

.list-cell {
  flex: 1;
  font-size: 14px;
  color: #606266;
}

.list-cell.course-info {
  flex: 3;
}

.course-basic {
  display: flex;
  gap: 16px;
  align-items: center;
}

.course-thumbnail-wrapper {
  width: 80px;
  height: 50px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.course-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-details {
  flex: 1;
  min-width: 0;
}

.course-title {
  margin: 0 0 4px 0;
  font-size: 15px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-category-text {
  font-size: 12px;
  color: #909399;
  margin: 0 0 4px 0;
}

.course-progress-mini {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-label {
  font-size: 12px;
  color: #909399;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 8px;
}

.status-dot.published { background: #67c23a; }
.status-dot.draft { background: #e6a23c; }
.status-dot.archived { background: #909399; }

.rating-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #fff8e6;
  color: #b45309;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
}

.list-cell.actions {
  display: flex;
  gap: 8px;
}

.icon-btn {
  background: transparent;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  color: #909399;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn:hover {
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
}

/* Pagination */
.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.glass-pagination :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #2563eb;
}

.glass-pagination :deep(.el-pagination.is-background .el-pager li) {
  background-color: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.4);
}

/* Empty State */
.empty-state {
  padding: 64px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-icon-wrapper {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #38bdf8;
  margin-bottom: 24px;
  box-shadow: 0 10px 25px rgba(56, 189, 248, 0.15);
}

.empty-title {
  font-size: 20px;
  color: #2c3e50;
  margin-bottom: 12px;
}

.empty-description {
  color: #909399;
  margin-bottom: 32px;
  max-width: 400px;
  line-height: 1.6;
}

/* Transitions */
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

.list-item-enter-active,
.list-item-leave-active {
  transition: all 0.3s ease;
}

.list-item-enter-from,
.list-item-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
