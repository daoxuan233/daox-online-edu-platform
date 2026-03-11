<template>
  <div class="teacher-dashboard">
    <!-- 欢迎区域 - Modern Hero -->
    <div class="welcome-hero">
      <div class="hero-bg-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
      </div>
      
      <div class="hero-content">
        <div class="hero-text-group">
          <div class="hero-greeting">
            <h1 class="greeting-title">
              <span class="text-light">Welcome back,</span>
              <span class="text-highlight">{{ teacherInfo.name }}</span>
            </h1>
            <div class="teacher-badge" v-if="teacherInfo.title">
              {{ teacherInfo.title }}
            </div>
          </div>
          <p class="hero-subtitle">
            <font-awesome-icon :icon="['fas', 'calendar-alt']" class="icon-mr" />
            {{ formatDate(new Date()) }} · 准备好开始今天的教学了吗？
          </p>
        </div>
        
        <div class="hero-actions">
          <button class="action-btn primary-btn" @click="createCourse">
            <div class="btn-icon">
              <font-awesome-icon :icon="['fas', 'plus']" />
            </div>
            <span>创建课程</span>
          </button>
          <button class="action-btn glass-btn" @click="viewSchedule">
            <div class="btn-icon">
              <font-awesome-icon :icon="['fas', 'calendar-check']" />
            </div>
            <span>查看课表</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 - Glass Cards -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.key">
        <div class="stat-icon-wrapper" :style="{ '--icon-color': stat.color }">
          <font-awesome-icon :icon="stat.icon" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
        <div class="stat-trend" :class="stat.trend">
          <font-awesome-icon :icon="stat.trend === 'up' ? ['fas', 'arrow-trend-up'] : ['fas', 'arrow-trend-down']" />
          <span>{{ stat.change }}</span>
        </div>
      </div>
    </div>

    <div class="dashboard-grid">
      <!-- 左侧内容 -->
      <div class="main-column">
        <!-- 我的课程 -->
        <div class="dashboard-card">
          <div class="card-header">
            <h2 class="card-title">
              <span class="title-decoration"></span>
              我的课程
            </h2>
            <button class="text-btn" @click="viewAllCourses">
              全部课程
              <font-awesome-icon :icon="['fas', 'arrow-right']" />
            </button>
          </div>
          
          <div class="course-list">
            <div 
              v-for="course in recentCourses" 
              :key="course.id" 
              class="course-item"
              @click="editCourse(course.id)"
            >
              <div class="course-cover-wrapper">
                <img :src="course.cover" :alt="course.title" class="course-img" />
                <div class="course-overlay">
                  <span class="status-badge" :class="course.status">
                    {{ getStatusText(course.status) }}
                  </span>
                </div>
              </div>
              
              <div class="course-details">
                <h3 class="course-name">{{ course.title }}</h3>
                <div class="course-tags">
                  <span class="tag" v-if="course.categoryName">
                    {{ course.categoryName }}
                  </span>
                  <span class="meta-item">
                    <font-awesome-icon :icon="['fas', 'clock']" />
                    {{ formatRelativeTime(course.updatedAt) }}
                  </span>
                </div>
                <div class="course-desc" v-if="course.description">
                  {{ course.description }}
                </div>
              </div>
              
              <div class="course-action">
                <button class="icon-btn">
                  <font-awesome-icon :icon="['fas', 'ellipsis-h']" />
                </button>
              </div>
            </div>
            
            <div v-if="recentCourses.length === 0" class="empty-state">
              <font-awesome-icon :icon="['fas', 'inbox']" class="empty-icon" />
              <p>暂无课程，快去创建吧</p>
            </div>
          </div>
        </div>

        <!-- 最近活动 -->
        <div class="dashboard-card">
          <div class="card-header">
            <h2 class="card-title">
              <span class="title-decoration"></span>
              最近活动
            </h2>
          </div>
          
          <div class="activity-timeline">
            <div v-for="(activity, index) in recentActivities" :key="activity.id" class="timeline-item">
              <div class="timeline-line" v-if="index !== recentActivities.length - 1"></div>
              <div class="timeline-dot" :style="{ backgroundColor: activity.color }">
                <font-awesome-icon :icon="['fas', 'circle']" style="font-size: 8px; color: white;" />
              </div>
              <div class="timeline-content">
                <div class="activity-msg">{{ activity.text }}</div>
                <div class="activity-time">{{ formatRelativeTime(activity.time) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="side-column">
        <!-- 待办事项 -->
        <div class="dashboard-card highlight-card">
          <div class="card-header">
            <h2 class="card-title">
              <font-awesome-icon :icon="['fas', 'check-square']" class="title-icon" />
              待办事项
            </h2>
            <button class="add-btn" @click="openCreateTodoDialog">
              <font-awesome-icon :icon="['fas', 'plus']" />
            </button>
          </div>
          
          <div class="todo-list-modern">
            <div v-for="todo in todoList" :key="todo.id" class="todo-row">
              <label class="custom-checkbox">
                <input 
                  type="checkbox" 
                  :checked="todo.completed" 
                  @change="toggleTodo(todo.id)"
                />
                <span class="checkmark"></span>
              </label>
              <div class="todo-info" :class="{ 'is-done': todo.completed }">
                <div class="todo-title">{{ todo.text }}</div>
                <div class="todo-footer">
                  <span class="priority-tag" :class="todo.priority">
                    {{ getPriorityText(todo.priority) }}
                  </span>
                  <span class="due-date">{{ formatDate(todo.dueDate) }}</span>
                </div>
              </div>
              <div class="todo-actions">
                <button class="todo-action-btn edit" @click.stop="openEditTodoDialog(todo)">
                  <font-awesome-icon :icon="['fas', 'pen']" />
                </button>
                <button class="todo-action-btn delete" @click.stop="deleteTodo(todo.id)">
                  <font-awesome-icon :icon="['fas', 'trash']" />
                </button>
              </div>
            </div>
            <div v-if="todoList.length === 0" class="todo-empty">暂无待办事项</div>
          </div>
        </div>

        <!-- 快捷数据 -->
        <div class="dashboard-card gradient-card">
          <div class="card-header white-text">
            <h2 class="card-title">本周概览</h2>
          </div>
          <div class="quick-stats-grid">
            <div class="q-stat-item">
              <div class="q-val">{{ weeklyStats.newStudents }}</div>
              <div class="q-label">新增学生</div>
            </div>
            <div class="q-stat-item">
              <div class="q-val">{{ weeklyStats.completedLessons }}</div>
              <div class="q-label">课程完成</div>
            </div>
            <div class="q-stat-item">
              <div class="q-val">{{ weeklyStats.submittedAssignments }}</div>
              <div class="q-label">作业提交</div>
            </div>
            <div class="q-stat-item">
              <div class="q-val">{{ weeklyStats.averageRating }}</div>
              <div class="q-label">平均评分</div>
            </div>
          </div>
        </div>

        <!-- 学生反馈 -->
        <div class="dashboard-card">
          <div class="card-header">
            <h2 class="card-title">最新反馈</h2>
          </div>
          <div class="feedback-stack">
            <div v-for="feedback in recentFeedback" :key="feedback.id" class="feedback-mini-card">
              <div class="feedback-top">
                <div class="f-user">
                  <img :src="feedback.student.avatar" class="f-avatar" />
                  <span class="f-name">{{ feedback.student.name }}</span>
                </div>
                <el-rate v-model="feedback.rating" disabled size="small" />
              </div>
              <div class="f-content">{{ feedback.content }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="todoDialogVisible"
      :show-close="false"
      width="460px"
      class="todo-dialog"
      align-center
    >
      <template #header>
        <div class="todo-dialog-header">
          <div class="todo-dialog-title-wrap">
            <span class="todo-dialog-title-icon">
              <font-awesome-icon :icon="['fas', todoDialogMode === 'create' ? 'plus' : 'pen']" />
            </span>
            <h3 class="todo-dialog-title">{{ todoDialogMode === 'create' ? '新增待办事项' : '编辑待办事项' }}</h3>
          </div>
          <button class="todo-dialog-close" @click="todoDialogVisible = false">
            <font-awesome-icon :icon="['fas', 'xmark']" />
          </button>
        </div>
      </template>

      <div class="todo-dialog-body">
        <el-form label-position="top">
          <el-form-item label="待办内容" required>
            <el-input
              v-model="todoForm.content"
              type="textarea"
              :rows="3"
              maxlength="255"
              show-word-limit
              placeholder="例如：准备下周《React Hooks》课程内容"
              class="todo-dialog-input"
            />
          </el-form-item>

          <div class="todo-dialog-grid">
            <el-form-item label="优先级">
              <el-select v-model="todoForm.priority" class="todo-dialog-select" placeholder="请选择优先级">
                <el-option label="高优先级" value="high" />
                <el-option label="中优先级" value="medium" />
                <el-option label="低优先级" value="low" />
              </el-select>
            </el-form-item>

            <el-form-item label="截止时间">
              <el-date-picker
                v-model="todoForm.dueDate"
                type="datetime"
                placeholder="选择截止时间"
                class="todo-dialog-date"
                clearable
              />
            </el-form-item>
          </div>
        </el-form>
      </div>

      <template #footer>
        <div class="todo-dialog-footer">
          <button class="todo-dialog-btn cancel" @click="todoDialogVisible = false">取消</button>
          <button class="todo-dialog-btn confirm" :disabled="todoSubmitting" @click="submitTodoDialog">
            {{ todoSubmitting ? '提交中...' : (todoDialogMode === 'create' ? '创建待办' : '保存修改') }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 快速操作浮动按钮 -->
    <div class="fab-wrapper">
      <div class="fab-menu" :class="{ 'active': showQuickActions }">
        <button 
          v-for="action in quickActions" 
          :key="action.key"
          class="fab-item"
          :class="action.type"
          @click="handleQuickAction(action.key)"
          :title="action.key"
        >
          <i :class="action.icon"></i>
        </button>
      </div>
      <button 
        class="fab-main-btn"
        @click="showQuickActions = !showQuickActions"
      >
        <i class="fas" :class="showQuickActions ? 'fa-times' : 'fa-plus'"></i>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {getIdentifier} from '@/utils/tokenAnalysis.js'
import {
  getTeacherProfile,
  getMyCourseList,
  getTeacherTodoList,
  createTeacherTodo,
  updateTeacherTodo,
  deleteTeacherTodo
} from '@/api/teacher/teacherAPI.js'

const router = useRouter()

// 响应式数据
const showQuickActions = ref(false)

// 教师信息
const teacherInfo = ref({
  id: 1,
  name: '李明',
  avatar: '/api/placeholder/60/60',
  title: '高级讲师',
  department: '计算机科学系'
})

// 加载用户数据
const loadUserProfile = async () => {
  try {
    const identifier = getIdentifier()
    if (!identifier) {
      ElMessage.error('无法获取用户标识，请重新登录')
      return
    }

    getTeacherProfile(
        identifier,
        (data) => {
          // 成功回调
          if (data) {
            // 直接更新reactive对象的属性
            Object.assign(teacherInfo.value, {
              id: data.id,
               name: data.nickname || teacherInfo.value.name,
               employeeId: data.identifier || teacherInfo.value.employeeId,
               email: data.email || teacherInfo.value.email,
               phone: data.phone || teacherInfo.value.phone,
               gender: data.gender || teacherInfo.value.gender,
               birthday: data.birthday || teacherInfo.value.birthday,
               bio: data.biography || teacherInfo.value.bio,
               avatar: data.avatarUrl || teacherInfo.value.avatar,
               role: data.role || teacherInfo.value.role
            })
            ElMessage.success('个人信息加载成功')
            console.log('用户信息已更新:', teacherInfo.value)
          }
        },
        (error) => {
          // 失败回调
          console.error('获取个人信息失败:', error)
          ElMessage.error('获取个人信息失败，请稍后重试')
        }
    )
  } catch (error) {
    console.error('加载用户数据时发生错误:', error)
    ElMessage.error('加载用户数据失败')
  }
}

// 加载课程数据
const loadCourseData = async () => {
  try {
    getMyCourseList(
      (data) => {
        // 成功回调
        if (data && Array.isArray(data)) {
          // 转换后端数据格式为前端需要的格式
          const allCourses = data.map(course => ({
            id: course.courseId,
            title: course.courseTitle,
            cover: course.courseCover || '/api/placeholder/80/60',
            status: course.courseStatus || 'draft',
            studentCount: 0, // 后端暂无此字段，使用默认值
            completionRate: 0, // 后端暂无此字段，使用默认值
            updatedAt: course.createTime ? new Date(course.createTime) : new Date(),
            description: course.courseDescription,
            teacherName: course.teacherName,
            categoryName: course.categoryName,
            enrollmentDate: course.enrollmentDate ? new Date(course.enrollmentDate) : null,
            // 使用开课时间作为排序依据，如果没有开课时间则使用创建时间
            sortDate: course.enrollmentDate ? new Date(course.enrollmentDate) : (course.createTime ? new Date(course.createTime) : new Date())
          }))
          
          // 按开课时间从近到远排序，并只取前3条记录
          recentCourses.value = allCourses
            .sort((a, b) => b.sortDate - a.sortDate) // 降序排列，最新的在前
            .slice(0, 3) // 只取前3条
          
          console.log('课程数据加载成功，显示最近3门课程:', recentCourses.value)
        }
      },
      (error) => {
        // 失败回调
        console.error('获取课程列表失败:', error)
        ElMessage.error('获取课程列表失败，请稍后重试')
      }
    )
  } catch (error) {
    console.error('加载课程数据时发生错误:', error)
    ElMessage.error('加载课程数据失败')
  }
}

// 统计数据
const stats = ref([
  {
    key: 'courses',
    label: '我的课程',
    value: '12',
    change: '+2',
    trend: 'up',
    icon: 'fas fa-book',
    color: '#4F46E5'
  },
  {
    key: 'students',
    label: '学生总数',
    value: '486',
    change: '+23',
    trend: 'up',
    icon: 'fas fa-users',
    color: '#10B981'
  },
  {
    key: 'assignments',
    label: '待批作业',
    value: '28',
    change: '-5',
    trend: 'down',
    icon: 'fas fa-clipboard-check',
    color: '#F59E0B'
  },
  {
    key: 'rating',
    label: '平均评分',
    value: '4.8',
    change: '+0.2',
    trend: 'up',
    icon: 'fas fa-star',
    color: '#EF4444'
  }
])

// 最近课程
const recentCourses = ref([])

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    text: '张三提交了《Vue组件开发》作业',
    time: new Date(Date.now() - 30 * 60 * 1000),
    icon: 'fas fa-file-upload',
    color: '#409EFF'
  },
  {
    id: 2,
    text: '李四对《JavaScript基础》课程给出了5星评价',
    time: new Date(Date.now() - 2 * 60 * 60 * 1000),
    icon: 'fas fa-star',
    color: '#F56C6C'
  },
  {
    id: 3,
    text: '王五完成了《React Hooks》章节学习',
    time: new Date(Date.now() - 4 * 60 * 60 * 1000),
    icon: 'fas fa-check-circle',
    color: '#67C23A'
  },
  {
    id: 4,
    text: '赵六在《Vue Router》课程中提出了问题',
    time: new Date(Date.now() - 6 * 60 * 60 * 1000),
    icon: 'fas fa-question-circle',
    color: '#E6A23C'
  }
])

// 待办事项
const todoList = ref([])
const todoDialogVisible = ref(false)
const todoDialogMode = ref('create')
const todoSubmitting = ref(false)
const editingTodoId = ref('')
const todoForm = ref({
  content: '',
  priority: 'medium',
  dueDate: null
})

const mapTodoFromApi = (todo) => {
  return {
    id: todo.id,
    text: todo.content,
    completed: Boolean(todo.completed),
    priority: todo.priority || 'medium',
    dueDate: todo.dueDate ? new Date(todo.dueDate) : null
  }
}

const loadTodoData = async () => {
  try {
    const data = await getTeacherTodoList()
    todoList.value = Array.isArray(data) ? data.map(mapTodoFromApi) : []
  } catch (error) {
    console.error('获取待办事项失败:', error)
    ElMessage.error('获取待办事项失败，请稍后重试')
  }
}

const normalizeTodoDate = (dateValue) => {
  if (!dateValue) return null
  const parsedDate = dateValue instanceof Date ? dateValue : new Date(dateValue)
  if (Number.isNaN(parsedDate.getTime())) return null
  return parsedDate.toISOString()
}

const resetTodoForm = () => {
  todoForm.value = {
    content: '',
    priority: 'medium',
    dueDate: null
  }
  editingTodoId.value = ''
}

const openCreateTodoDialog = () => {
  todoDialogMode.value = 'create'
  resetTodoForm()
  todoDialogVisible.value = true
}

const openEditTodoDialog = (todo) => {
  todoDialogMode.value = 'edit'
  editingTodoId.value = todo.id
  todoForm.value = {
    content: todo.text || '',
    priority: todo.priority || 'medium',
    dueDate: todo.dueDate ? new Date(todo.dueDate) : null
  }
  todoDialogVisible.value = true
}

const submitTodoDialog = async () => {
  const content = (todoForm.value.content || '').trim()
  if (!content) {
    ElMessage.warning('请输入待办事项内容')
    return
  }

  const requestBody = {
    content,
    priority: todoForm.value.priority || 'medium'
  }
  const dueDate = normalizeTodoDate(todoForm.value.dueDate)
  if (dueDate) {
    requestBody.dueDate = dueDate
  }

  todoSubmitting.value = true
  try {
    if (todoDialogMode.value === 'create') {
      await createTeacherTodo(requestBody)
      ElMessage.success('待办事项新增成功')
    } else {
      if (!editingTodoId.value) {
        ElMessage.error('待办事项ID无效')
        return
      }
      await updateTeacherTodo(editingTodoId.value, requestBody)
      ElMessage.success('待办事项更新成功')
    }
    todoDialogVisible.value = false
    resetTodoForm()
    await loadTodoData()
  } catch (error) {
    console.error('提交待办事项失败:', error)
    ElMessage.error(error.message || '提交待办事项失败')
  } finally {
    todoSubmitting.value = false
  }
}

// 学生反馈
const recentFeedback = ref([
  {
    id: 1,
    student: {
      name: '张三',
      avatar: '/api/placeholder/40/40'
    },
    courseName: 'Vue.js 3.0 完整开发教程',
    rating: 5,
    content: '老师讲解得非常清楚，例子很实用，学到了很多实战技巧！',
    time: new Date(Date.now() - 2 * 60 * 60 * 1000)
  },
  {
    id: 2,
    student: {
      name: '李四',
      avatar: '/api/placeholder/40/40'
    },
    courseName: 'JavaScript 高级编程',
    rating: 4,
    content: '课程内容很丰富，希望能增加更多的练习题。',
    time: new Date(Date.now() - 5 * 60 * 60 * 1000)
  },
  {
    id: 3,
    student: {
      name: '王五',
      avatar: '/api/placeholder/40/40'
    },
    courseName: 'React 实战项目开发',
    rating: 5,
    content: '项目案例很棒，跟着做完后对React的理解更深入了。',
    time: new Date(Date.now() - 8 * 60 * 60 * 1000)
  }
])

// 本周统计
const weeklyStats = ref({
  newStudents: 45,
  completedLessons: 128,
  submittedAssignments: 67,
  averageRating: 4.7
})

// 快速操作
const quickActions = ref([
  { key: 'course', icon: 'fas fa-book', type: 'primary' },
  { key: 'assignment', icon: 'fas fa-clipboard-list', type: 'success' },
  { key: 'question', icon: 'fas fa-question', type: 'warning' },
  { key: 'announcement', icon: 'fas fa-bullhorn', type: 'info' }
])

// 方法
const formatDate = (date) => {
  if (!date) return '无截止日期'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  }).format(date)
}

const formatRelativeTime = (date) => {
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else {
    return `${days}天前`
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

const getPriorityText = (priority) => {
  const priorityMap = {
    high: '高',
    medium: '中',
    low: '低'
  }
  return priorityMap[priority] || priority
}

const createCourse = () => {
  router.push({ name: 'NewCourse' })
}

const viewSchedule = () => {
  ElMessage.info('课程表功能开发中')
}

const viewAllCourses = () => {
  router.push({ name: 'TeacherCourses' })
}

const editCourse = (courseId) => {
  router.push({ name: 'CourseEdit', params: { id: courseId } })
}

const toggleTodo = async (todoId) => {
  const todo = todoList.value.find(t => t.id === todoId)
  if (todo) {
    const targetCompleted = !todo.completed
    try {
      await updateTeacherTodo(todoId, { completed: targetCompleted })
      todo.completed = targetCompleted
      ElMessage.success(targetCompleted ? '任务已完成' : '任务已取消完成')
    } catch (error) {
      console.error('更新待办事项状态失败:', error)
      ElMessage.error(error.message || '更新待办事项状态失败')
    }
  }
}

const deleteTodo = async (todoId) => {
  try {
    await ElMessageBox.confirm('确定删除该待办事项吗？', '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteTeacherTodo(todoId)
    todoList.value = todoList.value.filter(todo => todo.id !== todoId)
    ElMessage.success('待办事项删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除待办事项失败:', error)
      ElMessage.error(error.message || '删除待办事项失败')
    }
  }
}

const viewAllFeedback = () => {
  ElMessage.info('查看全部反馈功能开发中')
}

const handleQuickAction = (actionKey) => {
  showQuickActions.value = false
  
  switch (actionKey) {
    case 'course':
      createCourse()
      break
    case 'assignment':
      router.push({ name: 'AssessmentManagement' })
      break
    case 'question':
      router.push({ name: 'QuestionBank' })
      break
    case 'announcement':
      ElMessage.info('发布公告功能开发中')
      break
    default:
      ElMessage.info('功能开发中')
  }
}

onMounted(() => {
  // 页面加载时的初始化逻辑
  loadUserProfile()
  loadCourseData()
  loadTodoData()
})
</script>

<style scoped>
.teacher-dashboard {
  /* 使用透明背景，因为 Layout 已经有了背景 */
  background: transparent;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* Hero Section */
.welcome-hero {
  position: relative;
  background: linear-gradient(135deg, #2563EB 0%, #10B981 100%);
  border-radius: 24px;
  padding: 40px;
  color: white;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(37, 99, 235, 0.25);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hero-bg-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(5px);
}

.shape-1 {
  width: 200px;
  height: 200px;
  top: -50px;
  right: -50px;
}

.shape-2 {
  width: 150px;
  height: 150px;
  bottom: -30px;
  left: 20%;
}

.hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.hero-text-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.greeting-title {
  font-size: 36px;
  margin: 0;
  line-height: 1.2;
}

.text-light {
  font-weight: 300;
  opacity: 0.9;
  margin-right: 12px;
}

.text-highlight {
  font-weight: 700;
}

.teacher-badge {
  display: inline-block;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  align-self: flex-start;
  margin-top: 8px;
}

.hero-subtitle {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
  font-weight: 400;
  display: flex;
  align-items: center;
}

.icon-mr {
  margin-right: 8px;
}

.hero-actions {
  display: flex;
  gap: 16px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  border-radius: 16px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.primary-btn {
  background: white;
  color: #2563EB;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.glass-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(4px);
}

.glass-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.stat-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: flex-start;
  position: relative;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.08), 0 4px 6px -2px rgba(0, 0, 0, 0.04);
}

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: var(--icon-color);
  color: white;
  margin-right: 16px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #64748B;
  font-weight: 500;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 8px;
  background: #F8FAFC;
}

.stat-trend.up {
  color: #10B981;
  background: rgba(16, 185, 129, 0.1);
}

.stat-trend.down {
  color: #EF4444;
  background: rgba(239, 68, 68, 0.1);
}

/* Dashboard Grid */
.dashboard-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.main-column, .side-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.dashboard-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #0F172A;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-decoration {
  width: 4px;
  height: 18px;
  background: #2563EB;
  border-radius: 2px;
}

.text-btn {
  background: none;
  border: none;
  color: #2563EB;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.text-btn:hover {
  background: #EFF6FF;
}

/* Course List */
.course-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.course-item {
  display: flex;
  gap: 20px;
  padding: 16px;
  border-radius: 16px;
  background: #F8FAFC;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid transparent;
}

.course-item:hover {
  background: white;
  border-color: #E2E8F0;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.course-cover-wrapper {
  width: 120px;
  height: 80px;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.course-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-overlay {
  position: absolute;
  top: 8px;
  right: 8px;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 10px;
  font-weight: 600;
  color: white;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.status-badge.published { background: #10B981; }
.status-badge.draft { background: #F59E0B; }
.status-badge.archived { background: #94A3B8; }

.course-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
}

.course-name {
  font-size: 16px;
  font-weight: 600;
  color: #0F172A;
  margin: 0;
}

.course-tags {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #64748B;
  align-items: center;
}

.tag {
  background: #E0F2FE;
  color: #0369A1;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.course-desc {
  font-size: 13px;
  color: #64748B;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-action {
  display: flex;
  align-items: center;
}

.icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #94A3B8;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn:hover {
  background: #F1F5F9;
  color: #0F172A;
}

/* Timeline */
.activity-timeline {
  position: relative;
  padding-left: 12px;
}

.timeline-item {
  position: relative;
  padding-bottom: 24px;
  display: flex;
  gap: 16px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-line {
  position: absolute;
  left: 5px;
  top: 14px;
  bottom: 0;
  width: 2px;
  background: #E2E8F0;
}

.timeline-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  z-index: 1;
  margin-top: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 0 4px #F1F5F9;
}

.timeline-content {
  flex: 1;
}

.activity-msg {
  font-size: 14px;
  color: #334155;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #94A3B8;
}

/* Todo List */
.highlight-card {
  border: 1px solid #E2E8F0;
}

.add-btn {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: #EFF6FF;
  color: #2563EB;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.add-btn:hover {
  background: #2563EB;
  color: white;
}

.todo-list-modern {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F1F5F9;
}

.todo-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.custom-checkbox {
  position: relative;
  width: 20px;
  height: 20px;
  cursor: pointer;
  margin-top: 2px;
}

.custom-checkbox input {
  opacity: 0;
  width: 0;
  height: 0;
}

.checkmark {
  position: absolute;
  top: 0;
  left: 0;
  height: 20px;
  width: 20px;
  background-color: #F1F5F9;
  border-radius: 6px;
  border: 1px solid #CBD5E1;
  transition: all 0.2s;
}

.custom-checkbox input:checked ~ .checkmark {
  background-color: #2563EB;
  border-color: #2563EB;
}

.checkmark:after {
  content: "";
  position: absolute;
  display: none;
  left: 6px;
  top: 2px;
  width: 6px;
  height: 12px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.custom-checkbox input:checked ~ .checkmark:after {
  display: block;
}

.todo-info {
  flex: 1;
}

.todo-actions {
  display: flex;
  gap: 6px;
  margin-top: 2px;
}

.todo-action-btn {
  width: 26px;
  height: 26px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  background: #F8FAFC;
  color: #64748B;
}

.todo-action-btn.edit:hover {
  background: #EFF6FF;
  color: #2563EB;
}

.todo-action-btn.delete:hover {
  background: #FEF2F2;
  color: #EF4444;
}

.todo-title {
  font-size: 14px;
  color: #334155;
  margin-bottom: 4px;
  transition: color 0.2s;
}

.is-done .todo-title {
  color: #94A3B8;
  text-decoration: line-through;
}

.todo-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
}

.priority-tag {
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.priority-tag.high { background: #FEF2F2; color: #EF4444; }
.priority-tag.medium { background: #FFFBEB; color: #F59E0B; }
.priority-tag.low { background: #F0F9FF; color: #0EA5E9; }

.due-date {
  color: #94A3B8;
}

.todo-empty {
  text-align: center;
  color: #94A3B8;
  font-size: 13px;
  padding: 6px 0;
}

:deep(.todo-dialog) {
  border-radius: 20px;
  overflow: hidden;
}

:deep(.todo-dialog .el-dialog) {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(18px);
  border: 1px solid rgba(226, 232, 240, 0.95);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.12);
}

:deep(.todo-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 20px 22px 0;
}

:deep(.todo-dialog .el-dialog__body) {
  padding: 0 22px;
}

:deep(.todo-dialog .el-dialog__footer) {
  padding: 18px 22px 22px;
}

.todo-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.todo-dialog-title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.todo-dialog-title-icon {
  width: 30px;
  height: 30px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  color: #FFFFFF;
  font-size: 13px;
}

.todo-dialog-title {
  margin: 0;
  font-size: 18px;
  color: #0F172A;
  font-weight: 700;
}

.todo-dialog-close {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 10px;
  background: #F1F5F9;
  color: #64748B;
  cursor: pointer;
  transition: all 0.2s;
}

.todo-dialog-close:hover {
  background: #E2E8F0;
  color: #334155;
}

.todo-dialog-body {
  padding-top: 14px;
}

.todo-dialog-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.todo-dialog-input :deep(.el-textarea__inner),
.todo-dialog-select :deep(.el-select__wrapper),
.todo-dialog-date :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px #E2E8F0 inset;
  transition: all 0.2s;
  background: #FFFFFF;
}

.todo-dialog-input :deep(.el-textarea__inner:focus),
.todo-dialog-select :deep(.el-select__wrapper.is-focused),
.todo-dialog-date :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #3B82F6 inset, 0 0 0 3px rgba(59, 130, 246, 0.15);
}

.todo-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.todo-dialog-btn {
  min-width: 96px;
  height: 38px;
  border-radius: 11px;
  border: none;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.todo-dialog-btn.cancel {
  background: #F1F5F9;
  color: #475569;
}

.todo-dialog-btn.cancel:hover {
  background: #E2E8F0;
  color: #334155;
}

.todo-dialog-btn.confirm {
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  color: #FFFFFF;
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.26);
}

.todo-dialog-btn.confirm:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 12px 22px rgba(37, 99, 235, 0.32);
}

.todo-dialog-btn.confirm:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  box-shadow: none;
}

/* Quick Stats Gradient Card */
.gradient-card {
  background: linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%);
  color: white;
}

.white-text .card-title {
  color: white;
}

.quick-stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.q-stat-item {
  background: rgba(255, 255, 255, 0.1);
  padding: 16px;
  border-radius: 12px;
  text-align: center;
  backdrop-filter: blur(4px);
}

.q-val {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
}

.q-label {
  font-size: 12px;
  opacity: 0.8;
}

/* Feedback Stack */
.feedback-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feedback-mini-card {
  background: #F8FAFC;
  padding: 12px;
  border-radius: 12px;
}

.feedback-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.f-user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.f-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
}

.f-name {
  font-size: 12px;
  font-weight: 600;
  color: #334155;
}

.f-content {
  font-size: 12px;
  color: #64748B;
  line-height: 1.4;
}

/* FAB */
.fab-wrapper {
  position: fixed;
  bottom: 32px;
  right: 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  z-index: 100;
}

.fab-main-btn {
  width: 56px;
  height: 56px;
  border-radius: 28px;
  background: #2563EB;
  color: white;
  border: none;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.4);
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: flex;
  align-items: center;
  justify-content: center;
}

.fab-main-btn:hover {
  transform: scale(1.1);
}

.fab-menu {
  display: flex;
  flex-direction: column;
  gap: 12px;
  opacity: 0;
  transform: translateY(20px) scale(0.8);
  pointer-events: none;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.fab-menu.active {
  opacity: 1;
  transform: translateY(0) scale(1);
  pointer-events: auto;
}

.fab-item {
  width: 48px;
  height: 48px;
  border-radius: 24px;
  border: none;
  background: white;
  color: #64748B;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.fab-item:hover {
  transform: translateY(-2px);
}

.fab-item.primary { color: #2563EB; }
.fab-item.success { color: #10B981; }
.fab-item.warning { color: #F59E0B; }
.fab-item.info { color: #0EA5E9; }

/* Responsive */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
  
  .welcome-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 24px;
  }
  
  .hero-actions {
    width: 100%;
  }
  
  .action-btn {
    flex: 1;
    justify-content: center;
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
