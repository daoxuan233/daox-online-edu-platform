<template>
  <div class="grading-center-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <i class="fas fa-clipboard-check"></i>
            阅卷中心
          </h1>
          <p class="page-subtitle">管理和批改学生作业与考试</p>
        </div>
        <div class="header-actions">
          <el-button class="action-btn export-btn">
            <font-awesome-icon :icon="['fas', 'download']" />
            导出成绩
          </el-button>
          <el-button type="primary" class="action-btn batch-btn" @click="showBatchGrading = true">
            <i class="fas fa-tasks"></i>
            批量阅卷
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon pending">
            <i class="fas fa-clock"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.pendingGrading }}</div>
            <div class="stat-label">待阅卷</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon completed">
            <i class="fas fa-check-circle"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.completedGrading }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon average">
            <i class="fas fa-chart-line"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.averageScore }}%</div>
            <div class="stat-label">平均分数</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon time">
            <i class="fas fa-stopwatch"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.averageTime }}min</div>
            <div class="stat-label">平均用时</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <div class="filter-card">
        <div class="filter-row">
          <div class="filter-group">
            <label class="filter-label">阅卷状态</label>
            <el-select v-model="filters.status" placeholder="全部状态" class="filter-select">
              <el-option label="全部状态" value="" />
              <el-option label="待阅卷" value="pending" />
              <el-option label="阅卷中" value="grading" />
              <el-option label="已完成" value="completed" />
              <el-option label="需复查" value="review" />
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">测评类型</label>
            <el-select v-model="filters.type" placeholder="全部类型" class="filter-select">
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
            <el-select v-model="filters.course" placeholder="全部课程" class="filter-select">
              <el-option label="全部课程" value="" />
              <el-option label="Vue.js 基础" value="vue-basic" />
              <el-option label="React 进阶" value="react-advanced" />
              <el-option label="Node.js 实战" value="nodejs-practice" />
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">分数范围</label>
            <el-select v-model="filters.scoreRange" placeholder="全部分数" class="filter-select">
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
              class="search-input"
            >
              <template #prefix>
                <font-awesome-icon :icon="['fas', 'search']" />
              </template>
            </el-input>
          </div>
          
          <div class="action-group">
            <el-button class="filter-btn" @click="resetFilters">
              <i class="fas fa-undo"></i>
              重置
            </el-button>
            <el-button type="primary" class="filter-btn">
              <i class="fas fa-search"></i>
              搜索
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 阅卷任务列表 -->
    <div class="grading-section">
      <div class="section-header">
        <div class="header-left">
          <h3>阅卷任务</h3>
          <span class="grading-count" v-if="!isLoading">共 {{ filteredTasks.length }} 个任务</span>
        </div>
        <div class="header-actions" v-if="!isLoading && !error">
          <el-button-group class="view-toggle">
            <el-button 
              :type="viewMode === 'list' ? 'primary' : ''"
              @click="viewMode = 'list'"
              class="toggle-btn"
            >
              <i class="fas fa-list"></i>
            </el-button>
            <el-button 
              :type="viewMode === 'grid' ? 'primary' : ''"
              @click="viewMode = 'grid'"
              class="toggle-btn"
            >
              <i class="fas fa-th"></i>
            </el-button>
          </el-button-group>
          
          <el-select v-model="sortBy" placeholder="排序方式" class="sort-select">
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
          <div class="error-content">
            <div class="error-icon">
              <i class="fas fa-exclamation-triangle"></i>
            </div>
            <h3 class="error-title">加载失败</h3>
            <p class="error-message">{{ error }}</p>
            <el-button type="primary" @click="retryFetch" class="retry-btn">
              <i class="fas fa-redo"></i>
              重试
            </el-button>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else-if="tasks.length === 0" class="empty-state">
          <div class="empty-content">
            <div class="empty-icon">
              <i class="fas fa-clipboard-check"></i>
            </div>
            <h3 class="empty-title">太棒了！</h3>
            <p class="empty-message">目前没有需要批阅的试卷。</p>
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
      class="batch-dialog"
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
                <i class="fas fa-robot"></i>
                <div>
                  <div class="radio-title">自动阅卷</div>
                  <div class="radio-desc">对客观题进行自动评分</div>
                </div>
              </div>
            </el-radio>
            
            <el-radio label="setScore" class="batch-radio">
              <div class="radio-content">
                <i class="fas fa-calculator"></i>
                <div>
                  <div class="radio-title">批量给分</div>
                  <div class="radio-desc">为特定题目批量设置分数</div>
                </div>
              </div>
            </el-radio>
            
            <el-radio label="addComment" class="batch-radio">
              <div class="radio-content">
                <i class="fas fa-comment"></i>
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
          <i class="fas fa-info-circle"></i>
          将对 <strong>{{ selectedTasks.length }}</strong> 份答卷执行此操作
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showBatchGrading = false">取消</el-button>
          <el-button type="primary" @click="executeBatchAction">执行操作</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 阅卷中心主页面组件
 * 负责展示阅卷任务列表和相关统计信息
 * @component GradingCenter
 */

import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getGradingTasks, getGradingTaskStatus } from '@/api/teacher/teacherAPI.js'
import GradingTaskCard from '@/components/teacher/GradingTaskCard.vue'

/**
 * 页面状态管理
 */
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
const viewMode = ref('list')
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
})

onUnmounted(() => {
  stopSilentRefresh()
})
</script>

<style scoped>
.grading-center-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f0f3 0%, #e8e8eb 100%);
  padding: 0;
}

/* 页面头部 */
.page-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 24px 30px;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title i {
  color: #002FA7;
}

.page-subtitle {
  color: #6b7280;
  margin: 0;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.export-btn {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  color: #6c757d;
}

.batch-btn {
  background: linear-gradient(135deg, #002FA7, #517B4D);
  border: none;
  color: white;
}

/* 统计卡片 */
.stats-section {
  padding: 30px;
  padding-bottom: 0;
}

.stats-grid {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 
    20px 20px 40px rgba(0, 0, 0, 0.1),
    -20px -20px 40px rgba(255, 255, 255, 0.8),
    inset 0 0 0 1px rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    25px 25px 50px rgba(0, 0, 0, 0.15),
    -25px -25px 50px rgba(255, 255, 255, 0.9);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.stat-icon.pending {
  background: linear-gradient(135deg, #ffc107, #fd7e14);
}

.stat-icon.completed {
  background: linear-gradient(135deg, #28a745, #20c997);
}

.stat-icon.average {
  background: linear-gradient(135deg, #002FA7, #517B4D);
}

.stat-icon.time {
  background: linear-gradient(135deg, #6f42c1, #e83e8c);
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1;
}

.stat-label {
  color: #6b7280;
  font-size: 14px;
  margin-top: 4px;
}

/* 筛选区域 */
.filter-section {
  padding: 30px;
  padding-bottom: 0;
}

.filter-card {
  max-width: 1400px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 
    20px 20px 40px rgba(0, 0, 0, 0.1),
    -20px -20px 40px rgba(255, 255, 255, 0.8);
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-label {
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.filter-select {
  width: 100%;
}

.search-row {
  display: flex;
  gap: 16px;
  align-items: end;
}

.search-group {
  flex: 1;
}

.search-input {
  width: 100%;
}

.action-group {
  display: flex;
  gap: 12px;
}

.filter-btn {
  padding: 10px 20px;
  border-radius: 10px;
}

/* 阅卷列表 */
.grading-section {
  padding: 30px;
}

.section-header {
  max-width: 1400px;
  margin: 0 auto 24px auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.grading-count {
  color: #6b7280;
  font-size: 14px;
  margin-left: 12px;
}

.header-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.view-toggle {
  border-radius: 10px;
  overflow: hidden;
}

.toggle-btn {
  padding: 8px 12px;
  border-radius: 0;
}

.sort-select {
  width: 150px;
}

.grading-content {
  max-width: 1400px;
  margin: 0 auto;
}

.grading-list {
  display: grid;
  gap: 20px;
  margin-bottom: 30px;
}

.grading-list.list {
  grid-template-columns: 1fr;
}

.grading-list.grid {
  grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
}

.grading-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 
    15px 15px 30px rgba(0, 0, 0, 0.1),
    -15px -15px 30px rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.grading-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    20px 20px 40px rgba(0, 0, 0, 0.15),
    -20px -20px 40px rgba(255, 255, 255, 0.9);
}

.grading-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.student-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.student-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.student-id {
  font-size: 14px;
  color: #6b7280;
  margin: 4px 0 0 0;
}

.grading-status {
  display: flex;
  gap: 8px;
  align-items: center;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-badge.pending {
  background: rgba(255, 193, 7, 0.1);
  color: #ffc107;
}

.status-badge.grading {
  background: rgba(0, 123, 255, 0.1);
  color: #007bff;
}

.status-badge.completed {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.status-badge.review {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.priority-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
  display: flex;
  align-items: center;
  gap: 4px;
}

.assessment-info {
  margin-bottom: 16px;
}

.assessment-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.assessment-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #6b7280;
}

.meta-item i {
  color: #002FA7;
  width: 14px;
}

.grading-details {
  margin-bottom: 20px;
}

.detail-row {
  display: flex;
  gap: 24px;
  margin-bottom: 8px;
}

.detail-item {
  display: flex;
  gap: 8px;
  font-size: 14px;
}

.detail-label {
  color: #6b7280;
  min-width: 80px;
}

.detail-value {
  color: #2c3e50;
  font-weight: 500;
}

.detail-value.highlight {
  color: #dc3545;
  font-weight: 600;
}

.grading-footer {
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  padding-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.score-section {
  flex: 1;
}

.current-score {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.score-label {
  font-size: 14px;
  color: #6b7280;
}

.score-value {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
}

.score-total {
  font-size: 16px;
  color: #6b7280;
  font-weight: 500;
}

.score-percentage {
  font-size: 14px;
  color: #6b7280;
}

.score-progress {
  margin: 0;
}

.action-section {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.custom-pagination {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 16px 24px;
  box-shadow: 
    15px 15px 30px rgba(0, 0, 0, 0.1),
    -15px -15px 30px rgba(255, 255, 255, 0.8);
}

/* 批量阅卷对话框 */
.batch-dialog :deep(.el-dialog) {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
}

.batch-content {
  padding: 20px 0;
}

.batch-header {
  margin-bottom: 24px;
}

.batch-header h4 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.batch-header p {
  color: #6b7280;
  margin: 0;
}

.batch-options {
  margin-bottom: 24px;
}

.batch-radio-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.batch-radio {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  margin: 0;
  transition: all 0.3s ease;
}

.batch-radio:hover {
  border-color: #002FA7;
  background: rgba(0, 47, 167, 0.02);
}

.batch-radio.is-checked {
  border-color: #002FA7;
  background: rgba(0, 47, 167, 0.05);
}

.radio-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.radio-content i {
  font-size: 20px;
  color: #002FA7;
}

.radio-title {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.radio-desc {
  font-size: 14px;
  color: #6b7280;
}

.batch-form {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.selected-count {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(0, 47, 167, 0.1);
  border-radius: 8px;
  color: #002FA7;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 状态样式 */
.loading-state,
.error-state,
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: 40px 20px;
}

/* 加载状态 */
.skeleton-container {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.skeleton-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #e5e7eb;
}

.skeleton-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.skeleton-title {
  width: 60%;
  height: 20px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 4px;
}

.skeleton-badge {
  width: 80px;
  height: 24px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 12px;
}

.skeleton-content {
  margin-bottom: 16px;
}

.skeleton-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.skeleton-stat {
  flex: 1;
  height: 16px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 4px;
}

.skeleton-progress {
  width: 100%;
  height: 8px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 4px;
}

.skeleton-footer {
  display: flex;
  justify-content: flex-end;
}

.skeleton-action {
  width: 100px;
  height: 32px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 6px;
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 错误状态 */
.error-content {
  text-align: center;
  max-width: 400px;
}

.error-icon {
  font-size: 64px;
  color: #ef4444;
  margin-bottom: 16px;
}

.error-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.error-message {
  font-size: 16px;
  color: #6b7280;
  margin-bottom: 24px;
  line-height: 1.5;
}

.retry-btn {
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 8px;
}

/* 空状态 */
.empty-content {
  text-align: center;
  max-width: 400px;
}

.empty-icon {
  font-size: 64px;
  color: #10b981;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.empty-message {
  font-size: 16px;
  color: #6b7280;
  line-height: 1.5;
}

/* 任务列表样式 */
.task-list {
  display: grid;
  gap: 20px;
}

.task-list.grid {
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
}

.task-list.list {
  grid-template-columns: 1fr;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .grading-list.grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 20px 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .stats-section,
  .filter-section,
  .grading-section {
    padding: 20px 16px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-row {
    grid-template-columns: 1fr;
  }
  
  .search-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: space-between;
  }
  
  .grading-footer {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .action-section {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .detail-row {
    flex-direction: column;
    gap: 8px;
  }
  
  .assessment-meta {
    flex-direction: column;
    gap: 8px;
  }
  
  .batch-radio-group {
    gap: 12px;
  }
}
</style>
