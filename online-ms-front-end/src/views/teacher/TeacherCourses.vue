<template>
  <div class="teacher-courses">
    <!-- 页面头部 -->
    <div class="page-header glass-card">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <i class="fas fa-book"></i>
            课程管理
          </h1>
          <p class="page-subtitle">管理您的所有课程内容</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" class="neu-button" @click="createCourse">
            <font-awesome-icon :icon="['fas', 'plus']" />
            创建课程
          </el-button>
          <el-button class="neu-button" @click="importCourse">
            <font-awesome-icon :icon="['fas', 'upload']" />
            导入课程
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-card neu-card" v-for="stat in courseStats" :key="stat.key">
        <div class="stat-icon" :style="{ backgroundColor: stat.color }">
          <i :class="stat.icon"></i>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section neu-card">
      <div class="filter-row">
        <div class="search-group">
          <el-input
            v-model="searchQuery"
            placeholder="搜索课程名称、描述..."
            class="search-input"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <font-awesome-icon :icon="['fas', 'search']" />
            </template>
          </el-input>
        </div>
        
        <div class="filter-group">
          <el-select v-model="filterStatus" placeholder="状态" class="filter-select" @change="handleFilter">
            <el-option label="全部状态" value="" />
            <el-option label="已发布" value="published" />
            <el-option label="草稿" value="draft" />
            <el-option label="已归档" value="archived" />
          </el-select>
          
          <el-select v-model="filterCategory" placeholder="分类" class="filter-select" @change="handleFilter">
            <el-option label="全部分类" value="" />
            <el-option label="前端开发" value="frontend" />
            <el-option label="后端开发" value="backend" />
            <el-option label="移动开发" value="mobile" />
            <el-option label="数据科学" value="data" />
            <el-option label="设计" value="design" />
          </el-select>
          
          <el-select v-model="sortBy" placeholder="排序" class="filter-select" @change="handleSort">
            <el-option label="最新创建" value="created_desc" />
            <el-option label="最新更新" value="updated_desc" />
            <el-option label="学生最多" value="students_desc" />
            <el-option label="评分最高" value="rating_desc" />
          </el-select>
        </div>
        
        <div class="view-toggle">
          <el-button-group>
            <el-button 
              :type="viewMode === 'grid' ? 'primary' : ''"
              @click="viewMode = 'grid'"
              class="view-btn"
            >
              <font-awesome-icon :icon="['fas', 'th-large']" />
            </el-button>
            <el-button 
              :type="viewMode === 'list' ? 'primary' : ''"
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
      <div v-if="viewMode === 'grid'" class="courses-grid">
        <div 
          v-for="course in filteredCourses" 
          :key="course.id" 
          class="course-card neu-card"
        >
          <div class="course-cover">
            <img :src="course.cover" :alt="course.title" />
            <div class="course-status" :class="course.status">
              {{ getStatusText(course.status) }}
            </div>
            <div class="course-actions">
              <el-button circle size="small" @click="editCourse(course.id)">
                <font-awesome-icon :icon="['fas', 'edit']" />
              </el-button>
              <el-button circle size="small" @click="previewCourse(course.id)">
                <font-awesome-icon :icon="['fas', 'eye']" />
              </el-button>
              <el-dropdown @command="handleCourseAction">
                <el-button circle size="small">
                  <font-awesome-icon :icon="['fas', 'ellipsis-v']" />
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{ action: 'duplicate', id: course.id }">
                      <font-awesome-icon :icon="['fas', 'copy']" /> 复制课程
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'export', id: course.id }">
                      <font-awesome-icon :icon="['fas', 'download']" /> 导出课程
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'archive', id: course.id }" v-if="course.status !== 'archived'">
                      <font-awesome-icon :icon="['fas', 'archive']" /> 归档课程
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'delete', id: course.id }" divided>
                      <font-awesome-icon :icon="['fas', 'trash']" /> 删除课程
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          
          <div class="course-content">
            <h3 class="course-title">{{ course.title }}</h3>
            <p class="course-description">{{ course.description }}</p>
            
            <div class="course-meta">
              <div class="meta-item">
                <font-awesome-icon :icon="['fas', 'users']" />
                <span>{{ course.studentCount }} 学生</span>
              </div>
              <div class="meta-item">
                <font-awesome-icon :icon="['fas', 'star']" />
                <span>{{ course.rating }}</span>
              </div>
              <div class="meta-item">
                <font-awesome-icon :icon="['fas', 'clock']" />
                <span>{{ course.duration }}</span>
              </div>
            </div>
            
            <div class="course-footer">
              <div class="course-updated">
                <span>{{ formatRelativeTime(course.updatedAt) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-else class="courses-list">
        <div class="list-header">
          <div class="header-cell course-info">课程信息</div>
          <div class="header-cell">状态</div>
          <div class="header-cell">学生数</div>
          <div class="header-cell">评分</div>
          <div class="header-cell">价格</div>
          <div class="header-cell">更新时间</div>
          <div class="header-cell">操作</div>
        </div>
        
        <div 
          v-for="course in filteredCourses" 
          :key="course.id" 
          class="list-item neu-card"
        >
          <div class="list-cell course-info">
            <div class="course-basic">
              <img :src="course.cover" :alt="course.title" class="course-thumbnail" />
              <div class="course-details">
                <h4 class="course-title">{{ course.title }}</h4>
                <p class="course-category">{{ getCategoryText(course.category) }}</p>
                <div class="course-progress-mini">
                  <span>完成度: {{ course.completionRate }}%</span>
                  <el-progress
                    :percentage="course.completionRate"
                    :stroke-width="4"
                    :show-text="false"
                  />
                </div>
              </div>
            </div>
          </div>
          
          <div class="list-cell">
            <el-tag :type="getStatusType(course.status)" size="small">
              {{ getStatusText(course.status) }}
            </el-tag>
          </div>
          
          <div class="list-cell">
            <span class="student-count">
              <font-awesome-icon :icon="['fas', 'users']" />
              {{ course.studentCount }}
            </span>
          </div>
          
          <div class="list-cell">
            <div class="rating">
              <font-awesome-icon :icon="['fas', 'star']" />
              <span>{{ course.rating }}</span>
            </div>
          </div>
          
          <div class="list-cell">
            <span class="price">¥{{ course.price }}</span>
          </div>
          
          <div class="list-cell">
            <span class="update-time">{{ formatRelativeTime(course.updatedAt) }}</span>
          </div>
          
          <div class="list-cell actions">
            <el-button size="small" @click="editCourse(course.id)">
              <font-awesome-icon :icon="['fas', 'edit']" />
            </el-button>
            <el-button size="small" @click="previewCourse(course.id)">
              <font-awesome-icon :icon="['fas', 'eye']" />
            </el-button>
            <el-dropdown @command="handleCourseAction">
              <el-button size="small">
                <font-awesome-icon :icon="['fas', 'ellipsis-v']" />
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{ action: 'duplicate', id: course.id }">
                    <font-awesome-icon :icon="['fas', 'copy']" /> 复制
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'export', id: course.id }">
                    <font-awesome-icon :icon="['fas', 'download']" /> 导出
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'archive', id: course.id }" v-if="course.status !== 'archived'">
                    <font-awesome-icon :icon="['fas', 'archive']" /> 归档
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'delete', id: course.id }" divided>
                    <font-awesome-icon :icon="['fas', 'trash']" /> 删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
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
      />
    </div>

    <!-- 空状态 -->
    <div v-if="filteredCourses.length === 0" class="empty-state neu-card">
      <div class="empty-icon">
        <font-awesome-icon :icon="['fas', 'book-open']" />
      </div>
      <h3 class="empty-title">暂无课程</h3>
      <p class="empty-description">
        {{ searchQuery || filterStatus || filterCategory ? '没有找到符合条件的课程' : '您还没有创建任何课程' }}
      </p>
      <el-button type="primary" @click="createCourse" v-if="!searchQuery && !filterStatus && !filterCategory">
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

const router = useRouter()

// 响应式数据
const searchQuery = ref('')
const filterStatus = ref('')
const filterCategory = ref('')
const sortBy = ref('updated_desc')
const viewMode = ref('grid')
const currentPage = ref(1)
const pageSize = ref(12)

// 课程统计
const courseStats = ref([
  {
    key: 'total',
    label: '总课程数',
    value: '12',
    icon: 'fas fa-book',
    color: '#409EFF'
  },
  {
    key: 'published',
    label: '已发布',
    value: '8',
    icon: 'fas fa-check-circle',
    color: '#67C23A'
  },
  {
    key: 'draft',
    label: '草稿',
    value: '3',
    icon: 'fas fa-edit',
    color: '#E6A23C'
  },
  {
    key: 'students',
    label: '总学生数',
    value: '486',
    icon: 'fas fa-users',
    color: '#F56C6C'
  }
])

// 加载课程列表
const loadCourseList = async () => {
  try {
    loading.value = true
    getMyCourseList(
      (response) => {
        // 成功回调：处理后端返回的数据
        console.log('后端返回数据:', response)
        
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
          price: course.price || (Math.floor(Math.random() * 500) + 99),
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
      }
    )
  } catch (error) {
    console.error('加载课程列表异常:', error)
    ElMessage.error('加载课程列表异常')
  } finally {
    loading.value = false
  }
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

// 课程数据
const courses = ref([])
const loading = ref(true)

// 模拟数据（作为备用）
const mockCourses = ref([
  {
    id: 'mock-1',
    title: '示例课程 1',
    description: '这是一个示例课程描述',
    cover: '/api/placeholder/300/200',
    status: 'published',
    category: 'frontend',
    studentCount: 120,
    rating: 4.5,
    price: 199,
    duration: '10小时',
    completionRate: 85,
    createdAt: new Date('2024-01-01'),
    updatedAt: new Date('2024-01-15')
  },
  {
    id: 'mock-2',
    title: '示例课程 2',
    description: '这是另一个示例课程描述',
    cover: '/api/placeholder/300/200',
    status: 'draft',
    category: 'backend',
    studentCount: 80,
    rating: 4.2,
    price: 299,
    duration: '15小时',
    completionRate: 60,
    createdAt: new Date('2024-01-10'),
    updatedAt: new Date('2024-01-20')
  }
])

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

const getStatusType = (status) => {
  const typeMap = {
    published: 'success',
    draft: 'warning',
    archived: 'info'
  }
  return typeMap[status] || ''
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
  router.push({ name: 'CourseEdit', params: { id: 'new' } })
}

const importCourse = () => {
  ElMessage.info('导入课程功能开发中')
}

const editCourse = (courseId) => {
  router.push({ name: 'CourseEdit', params: { id: courseId } })
}

const previewCourse = (courseId) => {
  // 在新窗口打开课程预览
  const routeData = router.resolve({ name: 'CourseDetail', params: { id: courseId } })
  window.open(routeData.href, '_blank')
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
})
</script>

<style scoped>
.teacher-courses {
  min-height: 100vh;
  background: var(--bg-color);
  padding: 20px;
}

.page-header {
  padding: 30px;
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title i {
  color: var(--primary-color);
}

.page-subtitle {
  color: var(--text-secondary);
  margin: 0;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 15px;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  padding: 25px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 5px;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 14px;
}

.filter-section {
  padding: 20px;
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  gap: 20px;
  align-items: center;
}

.search-group {
  flex: 1;
  max-width: 400px;
}

.search-input {
  width: 100%;
}

.filter-group {
  display: flex;
  gap: 15px;
}

.filter-select {
  width: 120px;
}

.view-toggle .view-btn {
  padding: 8px 12px;
}

.courses-section {
  margin-bottom: 30px;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.course-card {
  overflow: hidden;
  transition: all 0.3s ease;
}

.course-card:hover {
  transform: translateY(-5px);
}

.course-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.course-card:hover .course-cover img {
  transform: scale(1.05);
}

.course-status {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  color: white;
  font-weight: 500;
}

.course-status.published {
  background: var(--success-color);
}

.course-status.draft {
  background: var(--warning-color);
}

.course-status.archived {
  background: var(--info-color);
}

.course-actions {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.course-card:hover .course-actions {
  opacity: 1;
}

.course-content {
  padding: 20px;
}

.course-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 10px 0;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-description {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 15px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-meta {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: var(--text-secondary);
  font-size: 12px;
}

.meta-item i {
  color: var(--primary-color);
}

.course-progress {
  margin-bottom: 15px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 12px;
  color: var(--text-secondary);
}

.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-price {
  display: flex;
  align-items: center;
  gap: 5px;
}

.price-label {
  color: var(--text-secondary);
  font-size: 12px;
}

.price-value {
  color: var(--primary-color);
  font-size: 16px;
  font-weight: 600;
}

.course-updated {
  color: var(--text-secondary);
  font-size: 12px;
}

.courses-list {
  background: var(--bg-color);
  border-radius: var(--border-radius);
  overflow: hidden;
}

.list-header {
  display: grid;
  grid-template-columns: 2fr 100px 100px 80px 80px 120px 120px;
  gap: 15px;
  padding: 15px 20px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.list-item {
  display: grid;
  grid-template-columns: 2fr 100px 100px 80px 80px 120px 120px;
  gap: 15px;
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.list-item:hover {
  background: var(--primary-light);
}

.list-item:last-child {
  border-bottom: none;
}

.list-cell {
  display: flex;
  align-items: center;
}

.course-basic {
  display: flex;
  gap: 15px;
  align-items: center;
}

.course-thumbnail {
  width: 60px;
  height: 45px;
  border-radius: var(--border-radius-sm);
  object-fit: cover;
}

.course-details {
  flex: 1;
}

.course-details .course-title {
  font-size: 16px;
  margin-bottom: 5px;
}

.course-category {
  color: var(--text-secondary);
  font-size: 12px;
  margin: 0 0 8px 0;
}

.course-progress-mini {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.course-progress-mini span {
  font-size: 11px;
  color: var(--text-secondary);
}

.student-count {
  display: flex;
  align-items: center;
  gap: 5px;
  color: var(--text-secondary);
}

.student-count i {
  color: var(--primary-color);
}

.rating {
  display: flex;
  align-items: center;
  gap: 5px;
  color: var(--text-secondary);
}

.rating i {
  color: #f39c12;
}

.price {
  color: var(--primary-color);
  font-weight: 600;
}

.update-time {
  color: var(--text-secondary);
  font-size: 12px;
}

.actions {
  gap: 8px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.empty-state {
  text-align: center;
  padding: 60px 30px;
}

.empty-icon {
  font-size: 64px;
  color: var(--text-disabled);
  margin-bottom: 20px;
}

.empty-title {
  font-size: 20px;
  color: var(--text-primary);
  margin: 0 0 10px 0;
}

.empty-description {
  color: var(--text-secondary);
  margin: 0 0 30px 0;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .courses-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }
  
  .list-header,
  .list-item {
    grid-template-columns: 2fr 80px 80px 60px 60px 100px 100px;
    gap: 10px;
  }
}

@media (max-width: 768px) {
  .teacher-courses {
    padding: 10px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .header-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .stats-section {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }
  
  .filter-row {
    flex-direction: column;
    gap: 15px;
  }
  
  .filter-group {
    width: 100%;
    justify-content: space-between;
  }
  
  .filter-select {
    flex: 1;
  }
  
  .courses-grid {
    grid-template-columns: 1fr;
  }
  
  .courses-list {
    overflow-x: auto;
  }
  
  .list-header,
  .list-item {
    min-width: 800px;
  }
}
</style>