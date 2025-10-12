<template>
  <div class="courses-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <div class="header-content">
          <div class="header-text">
            <h1 class="page-title">课程中心</h1>
            <p class="page-subtitle">探索海量优质课程，提升您的专业技能</p>
          </div>
<!--          <div class="header-stats">
            <div class="stat-item">
              <span class="stat-number">{{ totalCourses }}</span>
              <span class="stat-label">门课程</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ totalInstructors }}</span>
              <span class="stat-label">位讲师</span>
            </div>
          </div>-->
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-filter-section">
      <div class="container">
        <div class="search-filter-container neumorphism-glass">
          <!-- 搜索栏 -->
          <div class="search-section">
            <SearchBar 
              v-model="searchQuery"
              placeholder="搜索课程名称、讲师或关键词"
              size="lg"
              :show-filter="true"
              @search="handleSearch"
              @filter="showFilterDrawer = true"
            />
          </div>
          
          <!-- 快速筛选 -->
          <div class="quick-filters">
            <div class="filter-group category-tree-group">
              <span class="filter-label">分类：</span>
              <div class="category-tree-horizontal" v-loading="categoryLoading">
                <!-- 全部选项 -->
                <div 
                  class="category-item-horizontal all-category" 
                  :class="{ active: selectedCategory === '' }"
                  @click="selectedCategory = ''; handleCategoryChange()"
                  role="button"
                  tabindex="0"
                  aria-label="选择全部分类"
                >
                  <span class="category-name">全部</span>
                </div>
                
                <!-- 顶层分类节点 -->
                <template v-for="category in categories" :key="category.id">
                  <div 
                    class="category-item-horizontal top-level"
                    :class="{ 
                      active: selectedCategory === category.id,
                      'has-children': category.children && category.children.length > 0
                    }"
                    @mouseenter="handleCategoryHover(category.id, true)"
                    @mouseleave="handleCategoryHover(category.id, false)"
                    @click="selectedCategory = category.id; handleCategoryChange()"
                    role="button"
                    tabindex="0"
                    :aria-label="`选择${category.name}分类`"
                    :aria-expanded="category.children && category.children.length > 0 ? hoveredCategory === category.id : undefined"
                  >
                    <span class="category-name">{{ category.name }}</span>
                    <i 
                      v-if="category.children && category.children.length > 0"
                      class="expand-indicator"
                      :class="hoveredCategory === category.id ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"
                    ></i>
                    
                    <!-- 子分类下拉面板 -->
                    <transition name="dropdown-fade">
                      <div 
                        v-if="category.children && category.children.length > 0 && hoveredCategory === category.id"
                        class="sub-categories-dropdown"
                        @mouseenter="keepDropdownOpen = true"
                        @mouseleave="keepDropdownOpen = false; handleCategoryHover(category.id, false)"
                      >
                        <div 
                          v-for="subCategory in category.children"
                          :key="subCategory.id"
                          class="sub-category-item"
                          :class="{ active: selectedCategory === subCategory.id }"
                          @click.stop="selectedCategory = subCategory.id; handleCategoryChange()"
                          role="button"
                          tabindex="0"
                          :aria-label="`选择${subCategory.name}子分类`"
                        >
                          <span class="sub-category-name">{{ subCategory.name }}</span>
                        </div>
                      </div>
                    </transition>
                  </div>
                </template>
              </div>
            </div>
            
            <div class="filter-group">
              <span class="filter-label">难度：</span>
              <el-radio-group v-model="selectedLevel" @change="handleLevelChange">
                <el-radio-button value="" class="filter-radio">全部</el-radio-button>
                <el-radio-button value="beginner" class="filter-radio">初级</el-radio-button>
                <el-radio-button value="intermediate" class="filter-radio">中级</el-radio-button>
                <el-radio-button value="advanced" class="filter-radio">高级</el-radio-button>
              </el-radio-group>
            </div>
            
            <div class="filter-group">
              <span class="filter-label">排序：</span>
              <el-select v-model="sortBy" @change="handleSortChange" class="sort-select">
                <el-option label="最新发布" value="latest" />
                <el-option label="最受欢迎" value="popular" />
                <el-option label="评分最高" value="rating" />
                <el-option label="价格最低" value="price_low" />
                <el-option label="价格最高" value="price_high" />
              </el-select>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 课程列表 -->
    <div class="courses-section">
      <div class="container">
        <!-- 结果统计 -->
        <div class="results-header">
          <div class="results-info">
            <span class="results-count">找到 {{ filteredCourses.length }} 门课程</span>
            <div class="active-filters" v-if="hasActiveFilters">
              <el-tag 
                v-if="selectedCategory"
                closable
                @close="selectedCategory = ''"
                class="filter-tag"
              >
                {{ getCategoryName(selectedCategory) }}
              </el-tag>
              <el-tag 
                v-if="selectedLevel"
                closable
                @close="selectedLevel = ''"
                class="filter-tag"
              >
                {{ getLevelName(selectedLevel) }}
              </el-tag>
              <el-tag 
                v-if="searchQuery"
                closable
                @close="searchQuery = ''"
                class="filter-tag"
              >
                "{{ searchQuery }}"
              </el-tag>
            </div>
          </div>

        </div>
        
        <!-- 课程网格/列表 -->
        <div class="courses-container">
          <div v-if="loading" class="loading-container">
            <el-skeleton 
              v-for="n in 8" 
              :key="n"
              :loading="true"
              animated
              class="course-skeleton"
            >
              <template #template>
                <el-skeleton-item variant="image" style="width: 100%; height: 200px" />
                <div style="padding: 14px">
                  <el-skeleton-item variant="h3" style="width: 50%" />
                  <div style="display: flex; align-items: center; justify-items: space-between; margin-top: 16px">
                    <el-skeleton-item variant="text" style="margin-right: 16px" />
                    <el-skeleton-item variant="text" style="width: 30%" />
                  </div>
                </div>
              </template>
            </el-skeleton>
          </div>
          
          <div v-else-if="filteredCourses.length === 0" class="empty-state">
            <div class="empty-content neumorphism-card">
              <font-awesome-icon :icon="['fas', 'search']" class="text-6xl text-muted" />
              <h3 class="empty-title">未找到相关课程</h3>
              <p class="empty-description">尝试调整搜索条件或浏览其他分类</p>
              <el-button type="primary" @click="clearFilters">
                清除筛选条件
              </el-button>
            </div>
          </div>
          
          <div v-else :class="['courses-grid', viewMode]">
            <CourseCard 
              v-for="course in paginatedCourses" 
              :key="course.id"
              :course="course"
              :view-mode="viewMode"
              @click="$router.push(`/index/courses/${course.id}`)"
            />
          </div>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-container" v-if="filteredCourses.length > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="filteredCourses.length"
            layout="prev, pager, next, jumper, total"
            class="custom-pagination"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import CourseCard from '@/components/CourseCard.vue'
import SearchBar from '@/components/SearchBar.vue'
import { getCourseList, getCourseCategoryTreeStu } from '@/api/publicApi.js'
import { searchCoursesByCategory } from '@/api/students/stuAPI.js'

// 响应式数据
const loading = ref(true)
const searchQuery = ref('')
const selectedCategory = ref('')
const selectedLevel = ref('')
const sortBy = ref('latest')
const viewMode = ref('grid')
const currentPage = ref(1)
const pageSize = ref(12)
const showFilterDrawer = ref(false)

// 高级筛选
const priceRange = ref([0, 1000])
const selectedDurations = ref([])
const minRating = ref(0)
const selectedLanguages = ref([])
const selectedTags = ref([])

// 基础数据
const totalCourses = ref(1250)
const totalInstructors = ref(180)

// 分类树形数据
const categories = ref([])
const expandedCategories = ref(new Set()) // 记录展开的分类节点
const categoryLoading = ref(false)
const hoveredCategory = ref(null) // 当前悬停的分类ID
const keepDropdownOpen = ref(false) // 保持下拉菜单打开状态
const hoverTimer = ref(null) // 悬停延时器

const availableTags = ref([
  '实战项目', '零基础', '就业导向', '认证课程', '直播互动',
  '作业批改', '1对1辅导', '企业内训', '最新技术', '经典教程'
])

// 模拟课程数据
const allCourses = ref([])

// 计算属性
const hasActiveFilters = computed(() => {
  return selectedCategory.value || selectedLevel.value || searchQuery.value
})

const filteredCourses = computed(() => {
  let courses = [...allCourses.value]
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    courses = courses.filter(course => 
      course.title.toLowerCase().includes(query) ||
      course.description.toLowerCase().includes(query) ||
      course.instructor.toLowerCase().includes(query)
    )
  }
  
  // 难度过滤
  if (selectedLevel.value) {
    courses = courses.filter(course => course.level === selectedLevel.value)
  }
  
  // 价格过滤
  courses = courses.filter(course => 
    course.price >= priceRange.value[0] && course.price <= priceRange.value[1]
  )
  
  // 时长过滤
  if (selectedDurations.value.length > 0) {
    courses = courses.filter(course => {
      const hours = parseInt(course.duration)
      return selectedDurations.value.some(duration => {
        if (duration === 'short') return hours < 10
        if (duration === 'medium') return hours >= 10 && hours <= 30
        if (duration === 'long') return hours > 30
        return false
      })
    })
  }
  
  // 评分过滤
  if (minRating.value > 0) {
    courses = courses.filter(course => course.rating >= minRating.value)
  }
  
  // 语言过滤
  if (selectedLanguages.value.length > 0) {
    courses = courses.filter(course => 
      selectedLanguages.value.includes(course.language)
    )
  }
  
  // 标签过滤
  if (selectedTags.value.length > 0) {
    courses = courses.filter(course => 
      selectedTags.value.some(tag => course.tags.includes(tag))
    )
  }
  
  // 排序
  switch (sortBy.value) {
    case 'popular':
      courses.sort((a, b) => b.studentCount - a.studentCount)
      break
    case 'rating':
      courses.sort((a, b) => b.rating - a.rating)
      break
    case 'price_low':
      courses.sort((a, b) => a.price - b.price)
      break
    case 'price_high':
      courses.sort((a, b) => b.price - a.price)
      break
    case 'latest':
    default:
      courses.sort((a, b) => b.isNew - a.isNew)
      break
  }
  
  return courses
})

const paginatedCourses = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredCourses.value.slice(start, end)
})

// 方法
// 判断课程是否为新课程（7天内创建）
/*const isRecentCourse = (createTime) => {
  if (!createTime) return false
  const courseDate = new Date(createTime)
  const now = new Date()
  const diffTime = Math.abs(now - courseDate)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays <= 7
}*/

// 处理课程描述，移除Markdown格式和特殊字符
const formatDescription = (description) => {
  if (!description) return '暂无描述'
  
  // 移除Unicode转义字符
  let formatted = description.replace(/\\u[0-9A-Fa-f]{4}/g, '')
  
  // 移除Markdown格式
  formatted = formatted
    .replace(/#{1,6}\s*/g, '') // 移除标题标记
    .replace(/\*\*(.*?)\*\*/g, '$1') // 移除粗体标记
    .replace(/\*(.*?)\*/g, '$1') // 移除斜体标记
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1') // 移除链接格式
    .replace(/`([^`]+)`/g, '$1') // 移除代码标记
    .replace(/\n+/g, ' ') // 将换行符替换为空格
    .trim()
  
  // 限制描述长度
  if (formatted.length > 150) {
    formatted = formatted.substring(0, 150) + '...'
  }
  
  return formatted || '暂无描述'
}

// 根据分类加载课程数据
const loadCoursesByCategory = async (categoryId) => {
  try {
    loading.value = true
    
    // 调用分类搜索API
    const courseData = await searchCoursesByCategory(categoryId)
    
    // 检查API响应状态
    if (!courseData || !Array.isArray(courseData)) {
      console.warn('分类搜索API返回数据格式异常:', courseData)
      ElMessage.warning('获取分类课程数据异常，请稍后重试')
      allCourses.value = []
      return
    }

    // 将API返回的数据转换为组件需要的格式
    if (courseData.length > 0) {
      allCourses.value = courseData.map(course => ({
        id: course.courseId,
        title: course.courseTitle,
        description: formatDescription(course.courseDescription),
        instructor: course.teacherName || '未知讲师',
        instructorAvatar: '/api/placeholder/40/40',
        duration: '未知', // 后端暂无此字段
        studentCount: 0, // 后端暂无此字段
        rating: 4.5, // 后端暂无此字段，使用默认值
        reviewCount: 0, // 后端暂无此字段
        price: 0, // 后端暂无此字段
        originalPrice: 0, // 后端暂无此字段
        cover: course.courseCover || '/api/placeholder/300/200',
        category: course.categoryId || 'other',
        categoryName: course.categoryName || '其他',
        level: 'beginner', // 后端暂无此字段，使用默认值
        language: 'chinese',
        tags: [], // 后端暂无此字段
        isHot: false, // 后端暂无此字段
        courseStatus: course.courseStatus,
        createTime: course.createTime,
        updateTime: course.updateTime
      }))
      
      // 更新总课程数
      totalCourses.value = courseData.length
      
      ElMessage.success(`已加载 ${courseData.length} 门相关课程`)
    } else {
      allCourses.value = []
      ElMessage.info('该分类下暂无课程')
    }
  } catch (error) {
    console.error('获取分类课程列表失败:', error)
    ElMessage.error('获取分类课程列表失败，请稍后重试')
    allCourses.value = []
  } finally {
    loading.value = false
  }
}

// 加载课程数据
const loadCourseData = async () => {
  try {
    loading.value = true
    // 计算偏移量
    const offset = (currentPage.value - 1) * pageSize.value
    
    // 调用API获取课程列表数据
    const courseData = await getCourseList(pageSize.value, offset)
    
    // 检查API响应状态 - API_get已经处理了状态码，直接返回data部分
    if (!courseData || !Array.isArray(courseData)) {
      console.warn('API返回数据格式异常:', courseData)
      ElMessage.warning('获取课程数据异常，请稍后重试')
      allCourses.value = []
      return
    }

    // 将API返回的数据转换为组件需要的格式
    if (courseData.length > 0) {
      allCourses.value = courseData.map(course => ({
        id: course.courseId,
        title: course.courseTitle,
        description: formatDescription(course.courseDescription),
        instructor: course.teacherName || '未知讲师',
        instructorAvatar: '/api/placeholder/40/40',
        duration: '未知', // 后端暂无此字段
        studentCount: 0, // 后端暂无此字段
        rating: 4.5, // 后端暂无此字段，使用默认值
        reviewCount: 0, // 后端暂无此字段
        price: 0, // 后端暂无此字段
        originalPrice: 0, // 后端暂无此字段
        cover: course.courseCover || '/api/placeholder/300/200',
        category: course.categoryId || 'other',
        categoryName: course.categoryName || '其他',
        level: 'beginner', // 后端暂无此字段，使用默认值
        language: 'chinese',
        tags: [], // 后端暂无此字段
        // isNew: isRecentCourse(course.createTime),
        isHot: false, // 后端暂无此字段
        courseStatus: course.courseStatus,
        createTime: course.createTime,
        updateTime: course.updateTime
      }))
      
      // 更新总课程数
      totalCourses.value = courseData.length
    } else {
      allCourses.value = []
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败，请稍后重试')
    // 如果API调用失败，保持使用模拟数据或清空数据
    allCourses.value = []
  } finally {
    loading.value = false
  }
}

const getCategoryName = (categoryId) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.name : categoryId
}

const getLevelName = (level) => {
  const levels = {
    beginner: '初级',
    intermediate: '中级',
    advanced: '高级'
  }
  return levels[level] || level
}

const formatPrice = (value) => {
  return `¥${value}`
}

const handleSearch = async (query) => {
  searchQuery.value = query
  currentPage.value = 1
  await loadCourseData()
}

const handleCategoryChange = async () => {
  currentPage.value = 1
  
  // 如果选择了特定分类，使用分类搜索API
  if (selectedCategory.value && selectedCategory.value !== '') {
    await loadCoursesByCategory(selectedCategory.value)
  } else {
    // 如果选择"全部"，加载所有课程
    await loadCourseData()
  }
}

const handleLevelChange = async () => {
  currentPage.value = 1
  await loadCourseData()
}

const handleSortChange = async () => {
  currentPage.value = 1
  await loadCourseData()
}

const handlePageChange = async (page) => {
  currentPage.value = page
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
  
  // 重新加载当前页面的数据
  await loadCourseData()
}

const clearFilters = async () => {
  searchQuery.value = ''
  selectedCategory.value = ''
  selectedLevel.value = ''
  resetAdvancedFilters()
  currentPage.value = 1
  await loadCourseData()
}

const toggleTag = (tag) => {
  const index = selectedTags.value.indexOf(tag)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tag)
  }
}

const resetAdvancedFilters = () => {
  priceRange.value = [0, 1000]
  selectedDurations.value = []
  minRating.value = 0
  selectedLanguages.value = []
  selectedTags.value = []
}

const applyAdvancedFilters = async () => {
  showFilterDrawer.value = false
  currentPage.value = 1
  await loadCourseData()
  ElMessage.success('筛选条件已应用')
}

// 监听器
watch([searchQuery, selectedCategory, selectedLevel, priceRange, selectedDurations, minRating, selectedLanguages, selectedTags], () => {
  currentPage.value = 1
})

// 加载分类树数据
const loadCategoryTree = async () => {
  try {
    categoryLoading.value = true
    const data = await getCourseCategoryTreeStu()
    categories.value = data || []
    console.log('分类树数据加载成功:', data)
  } catch (error) {
    console.error('加载分类树失败:', error)
    ElMessage.error('加载分类数据失败')
  } finally {
    categoryLoading.value = false
  }
}

// 切换分类展开状态
const toggleCategory = (categoryId) => {
  if (expandedCategories.value.has(categoryId)) {
    expandedCategories.value.delete(categoryId)
  } else {
    expandedCategories.value.add(categoryId)
  }
}

// 处理分类悬停
const handleCategoryHover = (categoryId, isEntering) => {
  // 清除之前的定时器
  if (hoverTimer.value) {
    clearTimeout(hoverTimer.value)
    hoverTimer.value = null
  }

  if (isEntering) {
    // 鼠标进入时，延迟显示下拉菜单
    hoverTimer.value = setTimeout(() => {
      hoveredCategory.value = categoryId
    }, 150) // 150ms延迟，避免快速划过时频繁触发
  } else {
    // 鼠标离开时，延迟隐藏下拉菜单
    if (!keepDropdownOpen.value) {
      hoverTimer.value = setTimeout(() => {
        hoveredCategory.value = null
      }, 200) // 200ms延迟，给用户时间移动到下拉菜单
    }
  }
}

// 生命周期
onMounted(async () => {
  // 加载分类树数据
  await loadCategoryTree()
  // 加载课程数据
  await loadCourseData()
})

// 组件销毁前清理定时器
onBeforeUnmount(() => {
  if (hoverTimer.value) {
    clearTimeout(hoverTimer.value)
    hoverTimer.value = null
  }
})
</script>

<style scoped>
.courses-page {
  min-height: 100vh;
  background: var(--background-color);
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, var(--surface-color) 0%, #E8F4FD 100%);
  padding: var(--spacing-xl) 0;
  border-bottom: 1px solid var(--glass-border);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.page-subtitle {
  color: var(--text-secondary);
  font-size: 1rem;
}

.header-stats {
  display: flex;
  gap: var(--spacing-xl);
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--primary-color);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

/* 搜索筛选区域 */
.search-filter-section {
  padding: var(--spacing-lg) 0;
  background: var(--surface-color);
}

.search-filter-container {
  padding: var(--spacing-lg);
  border-radius: var(--border-radius-lg);
  position: relative;
  z-index: 1;
}

.search-section {
  margin-bottom: var(--spacing-lg);
}

.quick-filters {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-lg);
  align-items: center;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.filter-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  white-space: nowrap;
}

.filter-radio :deep(.el-radio-button__inner) {
  border-radius: var(--border-radius-sm);
  border: 1px solid var(--glass-border);
  background: var(--surface-color);
  box-shadow: var(--neumorphism-inset-light), var(--neumorphism-inset-dark);
}

.sort-select {
  width: 120px;
}

/* 分类树组件 - 横向布局 */
.category-tree-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  width: 100%;
  --category-bg: var(--surface-color, #ffffff);
  --category-hover-bg: var(--hover-color, #f5f7fa);
  --category-active-bg: var(--primary-color, #002FA7);
  --category-border: var(--glass-border, rgba(255, 255, 255, 0.18));
  --category-shadow: var(--neumorphism-inset-light, inset 2px 2px 5px #d1d1d4), var(--neumorphism-inset-dark, inset -2px -2px 5px #ffffff);
  --category-transition: var(--transition-normal, 0.3s ease);
  --category-border-radius: var(--border-radius-md, 12px);
}

.category-tree-horizontal {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm, 8px);
  width: 100%;
  padding: var(--spacing-sm, 8px);
  background: var(--category-bg);
  border: 1px solid var(--category-border);
  border-radius: var(--category-border-radius);
  box-shadow: var(--category-shadow);
  position: relative;
  z-index: 10;
}

/* 分类项基础样式 */
.category-item-horizontal {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--spacing-xs, 4px);
  padding: var(--spacing-sm, 8px) var(--spacing-md, 16px);
  background: var(--category-bg);
  border: 1px solid var(--category-border);
  border-radius: var(--category-border-radius);
  cursor: pointer;
  transition: var(--category-transition);
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-primary, #303133);
  white-space: nowrap;
  user-select: none;
  box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

/* 悬停效果 */
.category-item-horizontal:hover {
  background: var(--category-hover-bg);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* 激活状态 */
.category-item-horizontal.active {
  background: var(--category-active-bg);
  color: white;
  box-shadow: 0 4px 12px rgba(0, 47, 167, 0.3);
}

.category-item-horizontal.active .expand-indicator {
  color: white;
}

/* 有子分类的指示器 */
.category-item-horizontal.has-children .expand-indicator {
  font-size: 12px;
  color: var(--text-secondary, #606266);
  transition: transform var(--category-transition);
  margin-left: var(--spacing-xs, 4px);
}

.category-item-horizontal.has-children:hover .expand-indicator {
  transform: rotate(180deg);
}

/* 子分类下拉面板 */
.sub-categories-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 9999;
  background: var(--category-bg);
  border: 1px solid var(--category-border);
  border-radius: var(--category-border-radius);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  margin-top: var(--spacing-xs, 4px);
  overflow: hidden;
}

/* 子分类项 */
.sub-category-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm, 8px) var(--spacing-md, 16px);
  cursor: pointer;
  transition: var(--category-transition);
  border-bottom: 1px solid var(--category-border);
  font-size: 0.8rem;
  color: var(--text-secondary, #606266);
}

.sub-category-item:last-child {
  border-bottom: none;
}

.sub-category-item:hover {
  background: var(--category-hover-bg);
  color: var(--text-primary, #303133);
}

.sub-category-item.active {
  background: var(--category-active-bg);
  color: white;
}

/* 下拉动画 */
.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dropdown-fade-enter-from {
  opacity: 0;
  transform: translateY(-8px) scale(0.95);
}

.dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.95);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .category-tree-horizontal {
    flex-direction: column;
    gap: var(--spacing-xs, 4px);
  }
  
  .category-item-horizontal {
    width: 100%;
    justify-content: space-between;
  }
  
  .sub-categories-dropdown {
    position: static;
    margin-top: 0;
    border-radius: 0;
    box-shadow: none;
    background: var(--background-color, #f0f0f3);
  }
}

@media (max-width: 480px) {
  .category-item-horizontal {
    padding: var(--spacing-xs, 4px) var(--spacing-sm, 8px);
    font-size: 0.8rem;
  }
  
  .sub-category-item {
    padding: var(--spacing-xs, 4px) var(--spacing-sm, 8px);
    font-size: 0.75rem;
  }
}

/* 课程区域 */
.courses-section {
  padding: var(--spacing-xl) 0;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.results-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

.results-count {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.active-filters {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.filter-tag {
  cursor: pointer;
}

.view-controls :deep(.el-radio-button__inner) {
  padding: var(--spacing-sm);
  border-radius: var(--border-radius-sm);
}

/* 课程网格 */
.courses-container {
  margin-bottom: var(--spacing-xl);
}

.loading-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--spacing-lg);
}

.course-skeleton {
  border-radius: var(--border-radius-lg);
  overflow: hidden;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.empty-content {
  text-align: center;
  padding: var(--spacing-xl);
  border-radius: var(--border-radius-lg);
  max-width: 400px;
}

.empty-title {
  font-size: 1.25rem;
  color: var(--text-primary);
  margin: var(--spacing-lg) 0 var(--spacing-md);
}

.empty-description {
  color: var(--text-secondary);
  margin-bottom: var(--spacing-lg);
}

.courses-grid.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--spacing-lg);
}

.courses-grid.list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: var(--spacing-xl);
}

.custom-pagination :deep(.el-pager li) {
  border-radius: var(--border-radius-sm);
  margin: 0 2px;
}

/* 筛选抽屉 */
.filter-drawer :deep(.el-drawer__body) {
  padding: 0;
}

.drawer-content {
  padding: var(--spacing-lg);
  height: 100%;
  overflow-y: auto;
}

.filter-section {
  margin-bottom: var(--spacing-xl);
  padding-bottom: var(--spacing-lg);
  border-bottom: 1px solid var(--glass-border);
}

.filter-section:last-child {
  border-bottom: none;
}

.filter-title {
  font-size: 1rem;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.price-display {
  display: flex;
  justify-content: space-between;
  margin-top: var(--spacing-sm);
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.tag-item {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tag-item:hover {
  transform: translateY(-1px);
}

.drawer-footer {
  padding: var(--spacing-lg);
  border-top: 1px solid var(--glass-border);
  display: flex;
  gap: var(--spacing-md);
}

.drawer-footer .el-button {
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: var(--spacing-lg);
    text-align: center;
  }
  
  .header-stats {
    justify-content: center;
  }
  
  .quick-filters {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-label {
    margin-bottom: var(--spacing-xs);
  }
  
  .results-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .results-info {
    justify-content: center;
  }
  
  .view-controls {
    align-self: center;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.5rem;
  }
  
  .search-filter-container {
    padding: var(--spacing-md);
  }
  
  .courses-grid.grid {
    grid-template-columns: 1fr;
  }
  
  .filter-drawer {
    width: 100% !important;
  }
}
</style>