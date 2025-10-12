<template>
  <div class="teacher-dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section glass-card">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1 class="welcome-title">
            <font-awesome-icon :icon="['fas', 'chalkboard-teacher']" />
            欢迎回来，{{ teacherInfo.name }}老师
          </h1>
          <p class="welcome-subtitle">今天是 {{ formatDate(new Date()) }}，祝您教学愉快！</p>
        </div>
        <div class="welcome-actions">
          <el-button type="primary" class="neu-button" @click="createCourse">
            <font-awesome-icon :icon="['fas', 'plus']" />
            创建新课程
          </el-button>
          <el-button class="neu-button" @click="viewSchedule">
            <font-awesome-icon :icon="['fas', 'calendar']" />
            查看课程表
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card neu-card" v-for="stat in stats" :key="stat.key">
        <div class="stat-icon" :style="{ backgroundColor: stat.color }">
          <font-awesome-icon :icon="stat.icon" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-change" :class="stat.trend">
            <font-awesome-icon :icon="stat.trend === 'up' ? ['fas', 'arrow-up'] : ['fas', 'arrow-down']" />
            <span>{{ stat.change }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="dashboard-content">
      <!-- 左侧内容 -->
      <div class="left-content">
        <!-- 我的课程 -->
        <div class="section-card neu-card">
          <div class="section-header">
            <h2 class="section-title">
              <font-awesome-icon :icon="['fas', 'book']" />
              我的课程
            </h2>
            <el-button text @click="viewAllCourses">
              查看全部
              <font-awesome-icon :icon="['fas', 'arrow-right']" />
            </el-button>
          </div>
          
          <div class="course-list">
            <div 
              v-for="course in recentCourses" 
              :key="course.id" 
              class="course-item"
              @click="editCourse(course.id)"
            >
              <div class="course-cover">
                <img :src="course.cover" :alt="course.title" />
                <div class="course-status" :class="course.status">
                  {{ getStatusText(course.status) }}
                </div>
              </div>
              <div class="course-info">
                <h3 class="course-title">{{ course.title }}</h3>
                <div class="course-meta">
                  <span class="teacher-name" v-if="course.teacherName">
                    <font-awesome-icon :icon="['fas', 'user']" />
                    {{ course.teacherName }}
                  </span>
                  <span class="category-name" v-if="course.categoryName">
                    <font-awesome-icon :icon="['fas', 'tag']" />
                    {{ course.categoryName }}
                  </span>
                  <span class="update-time">
                    <font-awesome-icon :icon="['fas', 'clock']" />
                    {{ formatRelativeTime(course.updatedAt) }}
                  </span>
                </div>
                <div class="course-description" v-if="course.description">
                  {{ course.description }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 最近活动 -->
        <div class="section-card neu-card">
          <div class="section-header">
            <h2 class="section-title">
              <font-awesome-icon :icon="['fas', 'history']" />
              最近活动
            </h2>
          </div>
          
          <div class="activity-list">
            <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
              <div class="activity-icon" :style="{ backgroundColor: activity.color }">
                <i :class="activity.icon"></i>
              </div>
              <div class="activity-content">
                <div class="activity-text">{{ activity.text }}</div>
                <div class="activity-time">{{ formatRelativeTime(activity.time) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="right-content">
        <!-- 待办事项 -->
        <div class="section-card neu-card">
          <div class="section-header">
            <h2 class="section-title">
              <font-awesome-icon :icon="['fas', 'tasks']" />
              待办事项
            </h2>
            <el-button text @click="addTodo">
              <font-awesome-icon :icon="['fas', 'plus']" />
              添加
            </el-button>
          </div>
          
          <div class="todo-list">
            <div v-for="todo in todoList" :key="todo.id" class="todo-item">
              <el-checkbox 
                v-model="todo.completed" 
                @change="toggleTodo(todo.id)"
                class="todo-checkbox"
              />
              <div class="todo-content" :class="{ completed: todo.completed }">
                <div class="todo-text">{{ todo.text }}</div>
                <div class="todo-meta">
                  <span class="todo-priority" :class="todo.priority">
                    {{ getPriorityText(todo.priority) }}
                  </span>
                  <span class="todo-due">{{ formatDate(todo.dueDate) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 学生反馈 -->
        <div class="section-card neu-card">
          <div class="section-header">
            <h2 class="section-title">
              <font-awesome-icon :icon="['fas', 'comments']" />
              学生反馈
            </h2>
            <el-button text @click="viewAllFeedback">
              查看全部
              <font-awesome-icon :icon="['fas', 'arrow-right']" />
            </el-button>
          </div>
          
          <div class="feedback-list">
            <div v-for="feedback in recentFeedback" :key="feedback.id" class="feedback-item">
              <div class="feedback-header">
                <div class="student-info">
                  <img :src="feedback.student.avatar" :alt="feedback.student.name" class="student-avatar" />
                  <div class="student-details">
                    <div class="student-name">{{ feedback.student.name }}</div>
                    <div class="course-name">{{ feedback.courseName }}</div>
                  </div>
                </div>
                <div class="feedback-rating">
                  <el-rate v-model="feedback.rating" disabled size="small" />
                </div>
              </div>
              <div class="feedback-content">{{ feedback.content }}</div>
              <div class="feedback-time">{{ formatRelativeTime(feedback.time) }}</div>
            </div>
          </div>
        </div>

        <!-- 快速统计 -->
        <div class="section-card neu-card">
          <div class="section-header">
            <h2 class="section-title">
              <font-awesome-icon :icon="['fas', 'chart-pie']" />
              本周统计
            </h2>
          </div>
          
          <div class="quick-stats">
            <div class="quick-stat-item">
              <div class="quick-stat-label">新增学生</div>
              <div class="quick-stat-value">{{ weeklyStats.newStudents }}</div>
            </div>
            <div class="quick-stat-item">
              <div class="quick-stat-label">课程完成</div>
              <div class="quick-stat-value">{{ weeklyStats.completedLessons }}</div>
            </div>
            <div class="quick-stat-item">
              <div class="quick-stat-label">作业提交</div>
              <div class="quick-stat-value">{{ weeklyStats.submittedAssignments }}</div>
            </div>
            <div class="quick-stat-item">
              <div class="quick-stat-label">平均评分</div>
              <div class="quick-stat-value">{{ weeklyStats.averageRating }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快速操作浮动按钮 -->
    <div class="fab-container">
      <el-button 
        type="primary" 
        circle 
        size="large" 
        class="fab-main"
        @click="showQuickActions = !showQuickActions"
      >
        <i class="fas fa-plus"></i>
      </el-button>
      
      <transition-group name="fab" tag="div" class="fab-actions" v-show="showQuickActions">
        <el-button 
          v-for="action in quickActions" 
          :key="action.key"
          :type="action.type" 
          circle 
          class="fab-action"
          @click="handleQuickAction(action.key)"
        >
          <i :class="action.icon"></i>
        </el-button>
      </transition-group>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {getIdentifier} from '@/utils/tokenAnalysis.js'
import {getTeacherProfile, getMyCourseList} from '@/api/teacher/teacherAPI.js'

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
    color: '#409EFF'
  },
  {
    key: 'students',
    label: '学生总数',
    value: '486',
    change: '+23',
    trend: 'up',
    icon: 'fas fa-users',
    color: '#67C23A'
  },
  {
    key: 'assignments',
    label: '待批作业',
    value: '28',
    change: '-5',
    trend: 'down',
    icon: 'fas fa-clipboard-check',
    color: '#E6A23C'
  },
  {
    key: 'rating',
    label: '平均评分',
    value: '4.8',
    change: '+0.2',
    trend: 'up',
    icon: 'fas fa-star',
    color: '#F56C6C'
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
const todoList = ref([
  {
    id: 1,
    text: '批改《Vue组件开发》作业',
    completed: false,
    priority: 'high',
    dueDate: new Date(Date.now() + 24 * 60 * 60 * 1000)
  },
  {
    id: 2,
    text: '准备下周的《React Hooks》课程内容',
    completed: false,
    priority: 'medium',
    dueDate: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000)
  },
  {
    id: 3,
    text: '回复学生关于JavaScript闭包的问题',
    completed: true,
    priority: 'low',
    dueDate: new Date(Date.now() - 24 * 60 * 60 * 1000)
  },
  {
    id: 4,
    text: '更新《TypeScript入门》课程大纲',
    completed: false,
    priority: 'medium',
    dueDate: new Date(Date.now() + 5 * 24 * 60 * 60 * 1000)
  }
])

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

const addTodo = () => {
  ElMessage.info('添加待办事项功能开发中')
}

const toggleTodo = (todoId) => {
  const todo = todoList.value.find(t => t.id === todoId)
  if (todo) {
    ElMessage.success(todo.completed ? '任务已完成' : '任务已取消完成')
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
})
</script>

<style scoped>
.teacher-dashboard {
  min-height: 100vh;
  background: var(--bg-color);
  padding: 20px;
}

.welcome-section {
  padding: 30px;
  margin-bottom: 20px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.welcome-title i {
  color: var(--primary-color);
}

.welcome-subtitle {
  color: var(--text-secondary);
  margin: 0;
  font-size: 16px;
}

.welcome-actions {
  display: flex;
  gap: 15px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
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
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 5px;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-change {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 500;
}

.stat-change.up {
  color: var(--success-color);
}

.stat-change.down {
  color: var(--danger-color);
}

.dashboard-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.left-content,
.right-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-card {
  padding: 25px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--border-color);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title i {
  color: var(--primary-color);
}

.course-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.course-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.3s ease;
  background: var(--bg-color);
}

.course-item:hover {
  border-color: var(--primary-color);
  background: var(--primary-light);
  transform: translateY(-2px);
}

.course-cover {
  position: relative;
  width: 80px;
  height: 60px;
  border-radius: var(--border-radius-sm);
  overflow: hidden;
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-status {
  position: absolute;
  top: 5px;
  right: 5px;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 10px;
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

.course-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.course-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.3;
}

.course-meta {
  display: flex;
  gap: 15px;
  color: var(--text-secondary);
  font-size: 12px;
  flex-wrap: wrap;
}

.course-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.course-description {
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.4;
  margin-top: 5px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.course-progress {
  margin-top: auto;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 12px;
  color: var(--text-secondary);
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.activity-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.activity-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
}

.activity-text {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.4;
  margin-bottom: 4px;
}

.activity-time {
  color: var(--text-secondary);
  font-size: 12px;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
}

.todo-content {
  flex: 1;
}

.todo-content.completed {
  opacity: 0.6;
}

.todo-content.completed .todo-text {
  text-decoration: line-through;
}

.todo-text {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.4;
  margin-bottom: 6px;
}

.todo-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
}

.todo-priority {
  padding: 2px 6px;
  border-radius: 10px;
  color: white;
  font-weight: 500;
}

.todo-priority.high {
  background: var(--danger-color);
}

.todo-priority.medium {
  background: var(--warning-color);
}

.todo-priority.low {
  background: var(--info-color);
}

.todo-due {
  color: var(--text-secondary);
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.feedback-item {
  padding: 15px;
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
}

.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.student-info {
  display: flex;
  gap: 10px;
  align-items: center;
}

.student-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.student-name {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 14px;
}

.course-name {
  color: var(--text-secondary);
  font-size: 12px;
}

.feedback-content {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 8px;
}

.feedback-time {
  color: var(--text-secondary);
  font-size: 12px;
}

.quick-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.quick-stat-item {
  text-align: center;
  padding: 15px;
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
}

.quick-stat-label {
  color: var(--text-secondary);
  font-size: 12px;
  margin-bottom: 5px;
}

.quick-stat-value {
  color: var(--primary-color);
  font-size: 20px;
  font-weight: 600;
}

.fab-container {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 1000;
}

.fab-main {
  width: 56px;
  height: 56px;
  box-shadow: var(--shadow-neu);
}

.fab-actions {
  position: absolute;
  bottom: 70px;
  right: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.fab-action {
  width: 48px;
  height: 48px;
}

.fab-enter-active,
.fab-leave-active {
  transition: all 0.3s ease;
}

.fab-enter-from,
.fab-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.8);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .dashboard-content {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}

@media (max-width: 768px) {
  .teacher-dashboard {
    padding: 10px;
  }
  
  .welcome-content {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .welcome-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    padding: 20px;
  }
  
  .course-item {
    flex-direction: column;
    text-align: center;
  }
  
  .course-cover {
    width: 100%;
    height: 120px;
  }
  
  .quick-stats {
    grid-template-columns: 1fr;
  }
  
  .fab-container {
    bottom: 20px;
    right: 20px;
  }
}
</style>