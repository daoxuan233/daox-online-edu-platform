<template>
  <div class="grading-center-container">
    <!-- 扫描线背景装饰 -->
    <div class="scanline-overlay" aria-hidden="true"></div>
    <!-- 网格背景装饰 -->
    <div class="grid-bg" aria-hidden="true"></div>

    <!-- 页面头部 -->
    <div class="page-header" ref="headerRef">
      <div class="header-content">
        <div class="header-left">
          <!-- HUD 装饰角 -->
          <div class="hud-corner tl" aria-hidden="true"></div>
          <div class="header-badge">GRADING SYSTEM</div>
          <h1 class="page-title">
            <span class="title-icon-wrap">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="title-icon"><path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/><path d="m9 12 2 2 4-4"/></svg>
            </span>
            阅卷中心
            <span class="title-glow">阅卷中心</span>
          </h1>
          <p class="page-subtitle">
            <span class="subtitle-dot"></span>
            实时管理 · 智能批改 · 数据分析
          </p>
        </div>
        <div class="header-actions">
          <button class="hud-btn secondary-btn" type="button">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
            导出成绩
          </button>
          
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section" ref="statsRef">
      <div class="stats-grid">
        <div class="stat-card stat-pending" ref="statCards" @mouseenter="onStatCardEnter" @mouseleave="onStatCardLeave">
          <div class="stat-card-bg" aria-hidden="true"></div>
          <div class="stat-icon-wrap pending">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          </div>
          <div class="stat-content">
            <div class="stat-number" data-value="pendingGrading">{{ stats.pendingGrading }}</div>
            <div class="stat-label">待阅卷</div>
          </div>
          <div class="stat-corner-mark" aria-hidden="true"></div>
        </div>
        
        <div class="stat-card stat-completed" ref="statCards" @mouseenter="onStatCardEnter" @mouseleave="onStatCardLeave">
          <div class="stat-card-bg" aria-hidden="true"></div>
          <div class="stat-icon-wrap completed">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.completedGrading }}</div>
            <div class="stat-label">已完成</div>
          </div>
          <div class="stat-corner-mark" aria-hidden="true"></div>
        </div>
        
        <div class="stat-card stat-average" ref="statCards" @mouseenter="onStatCardEnter" @mouseleave="onStatCardLeave">
          <div class="stat-card-bg" aria-hidden="true"></div>
          <div class="stat-icon-wrap average">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.averageScore }}<span class="stat-unit">%</span></div>
            <div class="stat-label">平均分数</div>
          </div>
          <div class="stat-corner-mark" aria-hidden="true"></div>
        </div>
        
        <div class="stat-card stat-time" ref="statCards" @mouseenter="onStatCardEnter" @mouseleave="onStatCardLeave">
          <div class="stat-card-bg" aria-hidden="true"></div>
          <div class="stat-icon-wrap time">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/><path d="M16.24 7.76a6 6 0 0 1 0 8.49"/></svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.averageTime }}<span class="stat-unit">min</span></div>
            <div class="stat-label">平均用时</div>
          </div>
          <div class="stat-corner-mark" aria-hidden="true"></div>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section" ref="filterRef">
      <div class="filter-card">
        <div class="filter-card-header">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="filter-header-icon"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg>
          <span>筛选条件</span>
        </div>
        <div class="filter-row">
          <div class="filter-group">
            <label class="filter-label">阅卷状态</label>
            <el-select v-model="filters.status" placeholder="全部状态" class="filter-select hud-select">
              <el-option label="全部状态" value="" />
              <el-option label="待阅卷" value="pending" />
              <el-option label="阅卷中" value="grading" />
              <el-option label="已完成" value="completed" />
              <el-option label="需复查" value="review" />
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">测评类型</label>
            <el-select v-model="filters.type" placeholder="全部类型" class="filter-select hud-select">
              <el-option label="全部类型" value="" />
              <el-option label="课堂测验" value="quiz" />
              <el-option label="章节测试" value="test" />
              <el-option label="期中考试" value="midterm" />
              <el-option label="期末考试" value="final" />
              <el-option label="作业" value="homework" />
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">所属课程</label>
            <el-select v-model="filters.course" placeholder="全部课程" class="filter-select hud-select">
              <el-option label="全部课程" value="" />
              <el-option label="Vue.js 基础" value="vue-basic" />
              <el-option label="React 进阶" value="react-advanced" />
              <el-option label="Node.js 实战" value="nodejs-practice" />
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">分数范围</label>
            <el-select v-model="filters.scoreRange" placeholder="全部分数" class="filter-select hud-select">
              <el-option label="全部分数" value="" />
              <el-option label="90-100分" value="90-100" />
              <el-option label="80-89分" value="80-89" />
              <el-option label="70-79分" value="70-79" />
              <el-option label="60-69分" value="60-69" />
              <el-option label="60分以下" value="0-59" />
            </el-select>
          </div>
        </div>
        
        <div class="search-row">
          <div class="search-group">
            <el-input 
              v-model="filters.search" 
              placeholder="搜索学生姓名、学号或测评标题..."
              class="search-input hud-input"
            >
              <template #prefix>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="search-icon"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
              </template>
            </el-input>
          </div>
          
          <div class="action-group">
            <button class="hud-btn secondary-btn filter-btn" type="button" @click="resetFilters">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 .49-3.45"/></svg>
              重置
            </button>
            <button class="hud-btn primary-btn filter-btn" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
              搜索
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 阅卷任务列表 -->
    <div class="grading-section" ref="gradingSectionRef">
      <div class="section-header">
        <div class="header-left">
          <h3 class="section-title">
            <span class="section-title-mark"></span>
            阅卷任务
          </h3>
          <span class="grading-count" v-if="!isLoading">
            <span class="count-dot"></span>
            共 {{ filteredTasks.length }} 个任务
          </span>
        </div>
        <div class="header-actions" v-if="!isLoading && !error">
          <div class="view-toggle">
            <button 
              :class="['toggle-btn', { active: viewMode === 'list' }]"
              type="button"
              @click="viewMode = 'list'"
              aria-label="列表视图"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
            </button>
            <button 
              :class="['toggle-btn', { active: viewMode === 'grid' }]"
              type="button"
              @click="viewMode = 'grid'"
              aria-label="网格视图"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
            </button>
          </div>
          
          <el-select v-model="sortBy" placeholder="排序方式" class="sort-select hud-select">
            <el-option label="提交时间" value="submitTime" />
            <el-option label="任务标题" value="title" />
            <el-option label="课程名称" value="courseName" />
            <el-option label="待批阅数量" value="pendingSubmissionCount" />
          </el-select>
        </div>
      </div>
      
      <div class="grading-content">
        <!-- 加载状态 -->
        <div v-if="isLoading" class="loading-state">
          <div class="skeleton-container">
            <div v-for="i in 3" :key="i" class="skeleton-card">
              <div class="skeleton-header">
                <div class="skeleton-title"></div>
                <div class="skeleton-badge"></div>
              </div>
              <div class="skeleton-content">
                <div class="skeleton-stats">
                  <div class="skeleton-stat"></div>
                  <div class="skeleton-stat"></div>
                </div>
                <div class="skeleton-progress"></div>
              </div>
              <div class="skeleton-footer">
                <div class="skeleton-action"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 错误状态 -->
        <div v-else-if="error" class="error-state">
          <div class="state-card error-card">
            <div class="state-icon-wrap error-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            </div>
            <h3 class="state-title">数据加载失败</h3>
            <p class="state-message">{{ error }}</p>
            <button class="hud-btn primary-btn retry-btn" type="button" @click="retryFetch">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 .49-3.45"/></svg>
              重新加载
            </button>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else-if="tasks.length === 0" class="empty-state">
          <div class="state-card empty-card">
            <div class="state-icon-wrap empty-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/><path d="m9 12 2 2 4-4"/></svg>
            </div>
            <h3 class="state-title">阅卷任务清空</h3>
            <p class="state-message">目前没有需要批阅的试卷，所有任务已完成。</p>
          </div>
        </div>

        <!-- 任务列表 -->
        <div v-else :class="['task-list', viewMode]">
          <GradingTaskCard 
            v-for="task in paginatedTasks" 
            :key="task.assessmentId"
            :task="task"
          />
         </div>
         
         <!-- 分页 -->
         <div class="pagination-wrapper" v-if="!isLoading && !error && tasks.length > 0">
           <el-pagination
             v-model:current-page="currentPage"
             v-model:page-size="pageSize"
             :page-sizes="[10, 20, 50, 100]"
             :total="filteredTasks.length"
             layout="total, sizes, prev, pager, next, jumper"
             class="custom-pagination"
           />
         </div>
      </div>
    </div>

    <!-- 批量阅卷对话框 -->
    <el-dialog 
      v-model="showBatchGrading" 
      title="批量阅卷" 
      width="800px"
      class="batch-dialog hud-dialog"
    >
      <div class="batch-content">
        <div class="batch-header">
          <h4>选择批量操作</h4>
          <p>对选中的答卷执行批量操作</p>
        </div>
        
        <div class="batch-options">
          <el-radio-group v-model="batchAction" class="batch-radio-group">
            <el-radio label="autoGrade" class="batch-radio">
              <div class="radio-content">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="radio-icon"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 9h6M9 12h6M9 15h4"/><circle cx="17" cy="15" r="2"/><path d="m19 17-1.5-1.5"/></svg>
                <div>
                  <div class="radio-title">自动阅卷</div>
                  <div class="radio-desc">对客观题进行自动评分</div>
                </div>
              </div>
            </el-radio>
            
            <el-radio label="setScore" class="batch-radio">
              <div class="radio-content">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="radio-icon"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                <div>
                  <div class="radio-title">批量给分</div>
                  <div class="radio-desc">为特定题目批量设置分数</div>
                </div>
              </div>
            </el-radio>
            
            <el-radio label="addComment" class="batch-radio">
              <div class="radio-content">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="radio-icon"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                <div>
                  <div class="radio-title">批量评语</div>
                  <div class="radio-desc">为答卷添加统一评语</div>
                </div>
              </div>
            </el-radio>
          </el-radio-group>
        </div>
        
        <div class="batch-form" v-if="batchAction">
          <div v-if="batchAction === 'setScore'" class="score-form">
            <el-form label-width="100px">
              <el-form-item label="题目选择">
                <el-select v-model="batchForm.questionId" placeholder="选择题目">
                  <el-option label="第1题 - 单选题" value="1" />
                  <el-option label="第2题 - 多选题" value="2" />
                  <el-option label="第3题 - 填空题" value="3" />
                </el-select>
              </el-form-item>
              <el-form-item label="分数">
                <el-input-number v-model="batchForm.score" :min="0" :max="100" />
              </el-form-item>
            </el-form>
          </div>
          
          <div v-if="batchAction === 'addComment'" class="comment-form">
            <el-form label-width="100px">
              <el-form-item label="评语内容">
                <el-input 
                  v-model="batchForm.comment" 
                  type="textarea" 
                  :rows="4"
                  placeholder="请输入评语内容"
                />
              </el-form-item>
            </el-form>
          </div>
        </div>
        
        <div class="selected-count">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="info-icon"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          将对 <strong>{{ selectedTasks.length }}</strong> 份答卷执行此操作
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <button class="hud-btn secondary-btn" type="button" @click="showBatchGrading = false">取消</button>
          <button class="hud-btn primary-btn" type="button" @click="executeBatchAction">执行操作</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { gsap } from 'gsap'
import { ElMessage } from 'element-plus'
import { getGradingTasks, getGradingTaskStatus } from '@/api/teacher/teacherAPI.js'
import GradingTaskCard from '@/components/teacher/GradingTaskCard.vue'

// DOM refs for GSAP animations
const headerRef = ref(null)
const statsRef = ref(null)
const filterRef = ref(null)
const gradingSectionRef = ref(null)

// GSAP timeline storage for cleanup
let pageTimeline = null
let statCardTimelines = []
// 加载状态
const isLoading = ref(false)
// 错误状态
const error = ref(null)
// 阅卷任务列表
const tasks = ref([])
const refreshTimer = ref(null)

// 统计数据
const stats = ref({
  pendingGrading: 0,
  completedGrading: 0,
  averageScore: 0,
  averageTime: 0
})

// 筛选条件
const filters = ref({
  status: '',
  type: '',
  course: '',
  scoreRange: '',
  search: ''
})

// 视图模式和排序
const viewMode = ref('grid')
const sortBy = ref('submitTime')

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 批量阅卷
const showBatchGrading = ref(false)
const batchAction = ref('')
const selectedTasks = ref([])
const batchForm = ref({
  questionId: '',
  score: 0,
  comment: ''
})

/**
 * 获取阅卷任务列表
 * 从API获取数据并处理各种状态
 * @returns {Promise<void>}
 */
const fetchGradingTasks = async () => {
  try {
    isLoading.value = true
    error.value = null
    
    const response = await getGradingTasks()
    const latestTasks = response || []
    tasks.value = latestTasks.map(task => ({
      ...task,
      totalSubmissionCount: task.totalSubmissionCount || task.pendingSubmissionCount || 0,
      progressPercentage: task.pendingSubmissionCount > 0 ? 0 : 100,
      completed: task.pendingSubmissionCount === 0
    }))
    await refreshTaskStatusSilently()
    
    // 更新统计数据
    updateStats()
    
  } catch (err) {
    console.error('获取阅卷任务失败:', err)
    error.value = err.message || '获取阅卷任务失败，请稍后重试'
    ElMessage.error(error.value)
  } finally {
    isLoading.value = false
  }
}

const refreshTaskStatusSilently = async () => {
  if (!tasks.value.length) return
  const snapshot = [...tasks.value]
  const statusResults = await Promise.all(snapshot.map(async (task) => {
    try {
      const status = await getGradingTaskStatus(task.assessmentId)
      return { assessmentId: task.assessmentId, status }
    } catch {
      return { assessmentId: task.assessmentId, status: null }
    }
  }))

  const statusMap = new Map(
    statusResults
      .filter(item => item.status)
      .map(item => [item.assessmentId, item.status])
  )

  tasks.value = tasks.value.map(task => {
    const status = statusMap.get(task.assessmentId)
    if (!status) return task
    return {
      ...task,
      pendingSubmissionCount: status.pendingSubmissionCount,
      totalSubmissionCount: status.totalSubmissionCount,
      gradedSubmissionCount: status.gradedSubmissionCount,
      progressPercentage: status.progressPercentage,
      completed: status.completed
    }
  })
  updateStats()
}

const startSilentRefresh = () => {
  stopSilentRefresh()
  refreshTimer.value = setInterval(async () => {
    await refreshTaskStatusSilently()
  }, 8000)
}

const stopSilentRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
}

/**
 * 更新统计数据
 * 基于任务列表计算统计信息
 */
const updateStats = () => {
  if (!tasks.value || tasks.value.length === 0) {
    stats.value = {
      pendingGrading: 0,
      completedGrading: 0,
      averageScore: 0,
      averageTime: 0
    }
    return
  }
  
  // 计算待批阅总数
  const pendingCount = tasks.value.reduce((sum, task) => sum + task.pendingSubmissionCount, 0)
  const completedCount = tasks.value.filter(task => task.completed).length
  
  stats.value = {
    pendingGrading: pendingCount,
    completedGrading: completedCount,
    averageScore: 0, // 这个需要从后端获取
    averageTime: 0 // 这个需要从后端获取
  }
}

/**
 * 重试获取数据
 * 用于错误状态下的重试操作
 */
const retryFetch = () => {
  fetchGradingTasks()
}

/**
 * 筛选后的阅卷任务
 * 根据筛选条件过滤任务列表
 * @returns {Array} 筛选后的任务列表
 */
const filteredTasks = computed(() => {
  let result = tasks.value
  
  if (filters.value.course) {
    result = result.filter(task => task.courseName === filters.value.course)
  }
  
  if (filters.value.search) {
    const search = filters.value.search.toLowerCase()
    result = result.filter(task => 
      task.title.toLowerCase().includes(search) ||
      task.courseName.toLowerCase().includes(search)
    )
  }
  
  return result
})

/**
 * 分页后的阅卷任务
 * 对筛选后的任务进行分页处理
 * @returns {Array} 分页后的任务列表
 */
const paginatedTasks = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTasks.value.slice(start, end)
})

// 获取状态图标
const getStatusIcon = (status) => {
  const icons = {
    pending: 'fas fa-clock',
    grading: 'fas fa-edit',
    completed: 'fas fa-check-circle',
    review: 'fas fa-flag'
  }
  return icons[status] || 'fas fa-question-circle'
}

// 获取状态名称
const getStatusName = (status) => {
  const names = {
    pending: '待阅卷',
    grading: '阅卷中',
    completed: '已完成',
    review: '需复查'
  }
  return names[status] || '未知'
}

// 获取类型图标
const getTypeIcon = (type) => {
  const icons = {
    quiz: 'fas fa-question-circle',
    test: 'fas fa-clipboard-check',
    midterm: 'fas fa-graduation-cap',
    final: 'fas fa-certificate',
    homework: 'fas fa-tasks'
  }
  return icons[type] || 'fas fa-clipboard-list'
}

// 获取类型名称
const getTypeName = (type) => {
  const names = {
    quiz: '课堂测验',
    test: '章节测试',
    midterm: '期中考试',
    final: '期末考试',
    homework: '作业'
  }
  return names[type] || '未知'
}

// 获取分数颜色
const getScoreColor = (percentage) => {
  if (percentage >= 90) return '#28a745'
  if (percentage >= 80) return '#17a2b8'
  if (percentage >= 70) return '#ffc107'
  if (percentage >= 60) return '#fd7e14'
  return '#dc3545'
}

// 重置筛选条件
const resetFilters = () => {
  filters.value = {
    status: '',
    type: '',
    course: '',
    scoreRange: '',
    search: ''
  }
}

// 开始阅卷
const startGrading = (grading) => {
  console.log('开始阅卷:', grading)
  // 这里会跳转到阅卷详情页面
}

// 查看阅卷详情
const viewGrading = (grading) => {
  console.log('查看阅卷详情:', grading)
}

// 编辑阅卷
const editGrading = (grading) => {
  console.log('编辑阅卷:', grading)
}

// 下载答卷
const downloadPaper = (grading) => {
  console.log('下载答卷:', grading)
}

// 发送反馈
const sendFeedback = (grading) => {
  console.log('发送反馈:', grading)
}

// 标记复查
const markForReview = (grading) => {
  console.log('标记复查:', grading)
}

// 重置阅卷
const resetGrading = (grading) => {
  console.log('重置阅卷:', grading)
}

// 执行批量操作
const executeBatchAction = () => {
  console.log('执行批量操作:', batchAction.value, batchForm.value)
  showBatchGrading.value = false
}

/**
 * 组件挂载时的初始化
 * 获取阅卷任务数据
 */
onMounted(() => {
  fetchGradingTasks()
  startSilentRefresh()
  initPageAnimations()
})

onUnmounted(() => {
  stopSilentRefresh()
  pageTimeline?.kill()
  statCardTimelines.forEach(t => t?.kill())
})

/**
 * 初始化页面入场动效 (GSAP)
 * Respects prefers-reduced-motion
 */
const initPageAnimations = () => {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    gsap.set([headerRef.value, statsRef.value, filterRef.value, gradingSectionRef.value], { opacity: 1, y: 0 })
    return
  }

  pageTimeline = gsap.timeline({ defaults: { ease: 'power3.out' } })

  // Header slides down
  pageTimeline
    .from(headerRef.value, { opacity: 0, y: -30, duration: 0.6 })
    .from(statsRef.value, { opacity: 0, y: 20, duration: 0.5 }, '-=0.3')
    .from(filterRef.value, { opacity: 0, y: 20, duration: 0.5 }, '-=0.25')
    .from(gradingSectionRef.value, { opacity: 0, y: 20, duration: 0.5 }, '-=0.2')

  // Stat cards stagger animation
  const statCards = document.querySelectorAll('.stat-card')
  if (statCards.length) {
    gsap.from(statCards, {
      opacity: 0,
      y: 30,
      scale: 0.95,
      duration: 0.5,
      stagger: 0.1,
      ease: 'back.out(1.4)',
      delay: 0.3
    })
  }
}

/**
 * GSAP hover pulse for stat cards (called on mouseenter)
 */
const onStatCardEnter = (event) => {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  const tl = gsap.timeline()
  tl.to(event.currentTarget, { scale: 1.03, duration: 0.2, ease: 'power2.out' })
    .to(event.currentTarget, { scale: 1, duration: 0.3, ease: 'elastic.out(1, 0.5)' })
  statCardTimelines.push(tl)
}

const onStatCardLeave = (event) => {
  gsap.to(event.currentTarget, { scale: 1, duration: 0.2, ease: 'power2.out' })
}
</script>

<style scoped>
:root {
  --gc-primary: #2563eb;
  --gc-primary-deep: #1d4ed8;
  --gc-accent: #0ea5e9;
  --gc-success: #10b981;
  --gc-warning: #f59e0b;
  --gc-danger: #ef4444;
  --gc-bg-soft: #f4f8ff;
  --gc-bg-soft-2: #f0f7f5;
  --gc-card-bg: rgba(255, 255, 255, 0.82);
  --gc-card-border: rgba(148, 163, 184, 0.2);
  --gc-card-shadow: 0 10px 28px rgba(37, 99, 235, 0.1);
  --gc-text-primary: #0f172a;
  --gc-text-secondary: #64748b;
}

.grading-center-container {
  position: relative;
  min-height: 100vh;
  padding: 24px;
  overflow: hidden;
  color: var(--gc-text-primary);
  font-family: "Manrope", "Noto Sans SC", "Microsoft YaHei", sans-serif;
  background:
    radial-gradient(circle at 10% 15%, rgba(37, 99, 235, 0.12), transparent 44%),
    radial-gradient(circle at 92% 6%, rgba(16, 185, 129, 0.1), transparent 34%),
    linear-gradient(145deg, var(--gc-bg-soft) 0%, var(--gc-bg-soft-2) 100%);
}

.scanline-overlay,
.grid-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.scanline-overlay {
  background: repeating-linear-gradient(
    180deg,
    rgba(15, 23, 42, 0.028) 0,
    rgba(15, 23, 42, 0.028) 1px,
    transparent 1px,
    transparent 6px
  );
  mix-blend-mode: soft-light;
  opacity: 0.35;
}

.grid-bg {
  background-image:
    linear-gradient(rgba(37, 99, 235, 0.055) 1px, transparent 1px),
    linear-gradient(90deg, rgba(37, 99, 235, 0.055) 1px, transparent 1px);
  background-size: 38px 38px;
  opacity: 0.25;
}

.page-header,
.stats-section,
.filter-section,
.grading-section {
  position: relative;
  z-index: 1;
}

.page-header,
.filter-card,
.grading-content,
.custom-pagination,
.state-card,
.batch-content {
  border-radius: 24px;
  border: 1px solid var(--gc-card-border);
  background: var(--gc-card-bg);
  backdrop-filter: blur(16px);
  box-shadow: var(--gc-card-shadow);
}

.page-header {
  padding: 26px 30px;
  margin-bottom: 22px;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.header-left {
  position: relative;
}

.hud-corner {
  position: absolute;
  width: 16px;
  height: 16px;
  border-left: 2px solid rgba(37, 99, 235, 0.4);
  border-top: 2px solid rgba(37, 99, 235, 0.4);
}

.hud-corner.tl {
  left: -14px;
  top: -6px;
}

.header-badge {
  width: fit-content;
  margin-bottom: 8px;
  padding: 4px 10px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.14), rgba(14, 165, 233, 0.14));
  color: var(--gc-primary-deep);
  font-size: 11px;
  letter-spacing: 0.08em;
  font-weight: 700;
}

.page-title {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: clamp(24px, 2.1vw, 31px);
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--gc-text-primary);
}

.title-icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(145deg, rgba(37, 99, 235, 0.16), rgba(14, 165, 233, 0.18));
  color: var(--gc-primary);
}

.title-icon {
  width: 22px;
  height: 22px;
}

.title-glow {
  position: absolute;
  opacity: 0;
  pointer-events: none;
  color: rgba(37, 99, 235, 0.22);
  filter: blur(10px);
}

.page-subtitle {
  margin: 10px 0 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--gc-text-secondary);
  font-size: 14px;
  font-weight: 600;
}

.subtitle-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--gc-primary), var(--gc-success));
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.12);
}

.page-header .header-actions,
.section-header .header-actions,
.action-group,
.dialog-footer {
  display: flex;
  gap: 12px;
  align-items: center;
}

.hud-btn {
  border: 1px solid transparent;
  border-radius: 12px;
  height: 42px;
  padding: 0 18px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.hud-btn svg {
  width: 16px;
  height: 16px;
}

.primary-btn {
  color: #ffffff;
  border-color: transparent;
  background: linear-gradient(135deg, var(--gc-primary), var(--gc-accent));
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.24);
}

.primary-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 24px rgba(37, 99, 235, 0.3);
}

.secondary-btn {
  color: var(--gc-primary-deep);
  border-color: rgba(37, 99, 235, 0.28);
  background: rgba(255, 255, 255, 0.9);
}

.secondary-btn:hover {
  background: rgba(37, 99, 235, 0.08);
  border-color: rgba(37, 99, 235, 0.4);
}

.stats-section,
.filter-section,
.grading-section {
  padding: 0;
  margin-top: 22px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  border: 1px solid var(--gc-card-border);
  background: rgba(255, 255, 255, 0.85);
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.06);
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 24px rgba(37, 99, 235, 0.14);
}

.stat-card-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.72), rgba(255, 255, 255, 0.12));
}

.stat-corner-mark {
  position: absolute;
  right: 12px;
  top: 10px;
  width: 24px;
  height: 24px;
  border-top: 2px solid rgba(37, 99, 235, 0.22);
  border-right: 2px solid rgba(37, 99, 235, 0.22);
  border-radius: 0 8px 0 0;
}

.stat-icon-wrap,
.stat-content {
  position: relative;
  z-index: 1;
}

.stat-icon-wrap {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon-wrap svg {
  width: 22px;
  height: 22px;
}

.stat-icon-wrap.pending {
  color: #9a6b00;
  background: linear-gradient(145deg, rgba(245, 158, 11, 0.22), rgba(251, 191, 36, 0.24));
}

.stat-icon-wrap.completed {
  color: #047857;
  background: linear-gradient(145deg, rgba(16, 185, 129, 0.2), rgba(52, 211, 153, 0.24));
}

.stat-icon-wrap.average {
  color: var(--gc-primary-deep);
  background: linear-gradient(145deg, rgba(37, 99, 235, 0.2), rgba(59, 130, 246, 0.24));
}

.stat-icon-wrap.time {
  color: #0f766e;
  background: linear-gradient(145deg, rgba(20, 184, 166, 0.2), rgba(45, 212, 191, 0.24));
}

.stat-number {
  font-size: 28px;
  line-height: 1;
  font-weight: 800;
  color: var(--gc-text-primary);
  letter-spacing: -0.03em;
}

.stat-unit {
  margin-left: 3px;
  font-size: 13px;
  color: var(--gc-text-secondary);
  font-weight: 600;
}

.stat-label {
  margin-top: 6px;
  font-size: 13px;
  color: var(--gc-text-secondary);
  font-weight: 600;
}

.filter-card {
  padding: 20px 22px;
}

.filter-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  font-size: 15px;
  font-weight: 700;
  color: #334155;
}

.filter-header-icon {
  width: 16px;
  height: 16px;
  color: var(--gc-primary);
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 14px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.filter-label {
  margin: 0;
  font-size: 13px;
  color: #475569;
  font-weight: 600;
}

.search-row {
  display: flex;
  align-items: flex-end;
  gap: 14px;
}

.search-group {
  flex: 1;
}

.search-icon,
.info-icon,
.radio-icon {
  width: 16px;
  height: 16px;
}

.filter-btn {
  min-width: 92px;
}

.hud-select,
.hud-input {
  width: 100%;
}

.hud-select :deep(.el-select__wrapper),
.hud-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.3);
  box-shadow: none;
  min-height: 40px;
}

.hud-select :deep(.el-select__wrapper.is-focused),
.hud-input :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(37, 99, 235, 0.5);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.grading-section {
  margin-bottom: 6px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 0 2px;
}

.section-title {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  color: var(--gc-text-primary);
  font-weight: 800;
}

.section-title-mark {
  width: 4px;
  height: 18px;
  border-radius: 2px;
  background: linear-gradient(180deg, var(--gc-primary), var(--gc-success));
}

.grading-count {
  margin-left: 12px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--gc-text-secondary);
  font-size: 13px;
  font-weight: 600;
}

.count-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--gc-accent);
}

.view-toggle {
  display: inline-flex;
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  padding: 3px;
}

.toggle-btn {
  width: 36px;
  height: 34px;
  border: none;
  border-radius: 9px;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.toggle-btn svg {
  width: 16px;
  height: 16px;
}

.toggle-btn.active {
  background: rgba(37, 99, 235, 0.14);
  color: var(--gc-primary-deep);
}

.sort-select {
  width: 160px;
}

.grading-content {
  padding: 18px;
}

.task-list {
  display: grid;
  gap: 16px;
}

.task-list.grid {
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
}

.task-list.list {
  grid-template-columns: 1fr;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.custom-pagination {
  padding: 12px 14px;
}

.custom-pagination :deep(.el-pagination.is-background .el-pager li) {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.28);
}

.custom-pagination :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: var(--gc-primary);
}

.loading-state,
.error-state,
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 340px;
  padding: 12px;
}

.skeleton-container {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 14px;
}

.skeleton-card {
  border-radius: 16px;
  padding: 16px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  background: rgba(255, 255, 255, 0.92);
}

.skeleton-header,
.skeleton-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.skeleton-content {
  margin: 12px 0;
}

.skeleton-stats {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.skeleton-title,
.skeleton-badge,
.skeleton-stat,
.skeleton-progress,
.skeleton-action {
  background: linear-gradient(90deg, #e5edf9 25%, #dbe8f8 50%, #e5edf9 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.3s ease infinite;
}

.skeleton-title {
  width: 62%;
  height: 18px;
  border-radius: 4px;
}

.skeleton-badge {
  width: 78px;
  height: 22px;
  border-radius: 999px;
}

.skeleton-stat {
  flex: 1;
  height: 14px;
  border-radius: 4px;
}

.skeleton-progress {
  width: 100%;
  height: 8px;
  border-radius: 999px;
}

.skeleton-action {
  width: 90px;
  height: 30px;
  border-radius: 8px;
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.state-card {
  max-width: 460px;
  width: 100%;
  padding: 28px 26px;
  text-align: center;
}

.state-icon-wrap {
  margin: 0 auto 16px;
  width: 74px;
  height: 74px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.state-icon-wrap svg {
  width: 34px;
  height: 34px;
}

.error-icon {
  color: var(--gc-danger);
  background: rgba(239, 68, 68, 0.12);
}

.empty-icon {
  color: var(--gc-success);
  background: rgba(16, 185, 129, 0.14);
}

.state-title {
  margin: 0 0 8px;
  font-size: 20px;
  color: var(--gc-text-primary);
  font-weight: 800;
}

.state-message {
  margin: 0;
  color: var(--gc-text-secondary);
  line-height: 1.65;
}

.retry-btn {
  margin-top: 18px;
}

.batch-dialog :deep(.el-dialog) {
  border-radius: 22px;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.28);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  box-shadow: 0 22px 56px rgba(15, 23, 42, 0.16);
}

.batch-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding: 18px 20px 0;
}

.batch-dialog :deep(.el-dialog__body) {
  padding: 16px 20px 6px;
}

.batch-dialog :deep(.el-dialog__footer) {
  padding: 12px 20px 20px;
}

.batch-content {
  border-radius: 16px;
  padding: 16px;
  border-color: rgba(148, 163, 184, 0.24);
  box-shadow: none;
}

.batch-header h4 {
  margin: 0;
  font-size: 18px;
  color: var(--gc-text-primary);
}

.batch-header p {
  margin: 6px 0 0;
  color: var(--gc-text-secondary);
  font-size: 13px;
}

.batch-options {
  margin-top: 14px;
}

.batch-radio-group {
  display: grid;
  gap: 10px;
}

.batch-radio {
  margin: 0;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  background: rgba(255, 255, 255, 0.74);
  padding: 12px 14px;
  transition: all 0.2s ease;
}

.batch-radio:hover,
.batch-radio.is-checked {
  border-color: rgba(37, 99, 235, 0.5);
  background: rgba(37, 99, 235, 0.08);
}

.radio-content {
  display: flex;
  gap: 10px;
  align-items: center;
}

.radio-icon {
  color: var(--gc-primary);
}

.radio-title {
  color: var(--gc-text-primary);
  font-weight: 700;
}

.radio-desc {
  margin-top: 2px;
  color: var(--gc-text-secondary);
  font-size: 12px;
}

.batch-form {
  margin-top: 12px;
  border-radius: 14px;
  padding: 14px;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.22);
}

.batch-form :deep(.el-input__wrapper),
.batch-form :deep(.el-textarea__inner),
.batch-form :deep(.el-select__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.3) inset;
}

.selected-count {
  margin-top: 12px;
  border-radius: 12px;
  border: 1px solid rgba(37, 99, 235, 0.22);
  background: rgba(37, 99, 235, 0.09);
  color: var(--gc-primary-deep);
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .grading-center-container {
    padding: 16px;
  }

  .header-content,
  .section-header {
    flex-direction: column;
    align-items: stretch;
    gap: 14px;
  }

  .section-header .header-actions {
    justify-content: space-between;
  }

  .search-row {
    flex-direction: column;
    align-items: stretch;
  }

  .action-group {
    justify-content: flex-end;
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .stats-grid,
  .filter-row,
  .task-list.grid {
    grid-template-columns: 1fr;
  }

  .page-header,
  .filter-card,
  .grading-content {
    padding: 16px;
  }

  .page-title {
    font-size: 24px;
  }

  .section-header .header-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .sort-select {
    width: 100%;
  }

  .view-toggle {
    width: fit-content;
  }

  .page-header .header-actions {
    flex-wrap: wrap;
  }
}
</style>
