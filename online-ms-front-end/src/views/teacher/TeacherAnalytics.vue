<template>
  <div class="teacher-analytics">
    <!-- 页面标题 -->
    <div class="page-header glass-card">
      <div class="header-content">
        <div class="header-icon">
          <font-awesome-icon :icon="['fas', 'chart-line']" />
        </div>
        <div class="header-text">
          <h1 class="page-title">教学分析</h1>
          <p class="page-subtitle">深入了解您的教学效果和学生表现数据</p>
        </div>
        <div class="header-actions">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="neu-date-picker"
            @change="handleDateRangeChange"
          />
          <el-button type="primary" class="neu-button" @click="exportReport">
            <font-awesome-icon :icon="['fas', 'download']" />
            导出报告
          </el-button>
        </div>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <div class="metrics-grid">
      <div v-for="metric in coreMetrics" :key="metric.key" class="metric-card neu-card">
        <div class="metric-icon" :style="{ background: metric.gradient }">
          <font-awesome-icon :icon="metric.icon" />
        </div>
        <div class="metric-content">
          <h3 class="metric-title">{{ metric.title }}</h3>
          <div class="metric-value">{{ metric.value }}</div>
          <div class="metric-change" :class="metric.changeType">
            <font-awesome-icon :icon="metric.changeIcon" />
            <span>{{ metric.change }}</span>
          </div>
        </div>
        <div class="metric-chart">
          <canvas :ref="el => metricChartRefs[metric.key] = el" :id="`chart-${metric.key}`"></canvas>
        </div>
      </div>
    </div>

    <!-- 主要图表区域 -->
    <div class="charts-section">
      <div class="charts-grid">
        <!-- 学生活跃度趋势 -->
        <div class="chart-container neu-card">
          <div class="chart-header">
            <h3 class="chart-title">
              <font-awesome-icon :icon="['fas', 'users']" />
              学生活跃度趋势
            </h3>
            <div class="chart-controls">
              <el-radio-group v-model="activityPeriod" size="small" class="neu-radio-group">
                <el-radio-button label="7d">7天</el-radio-button>
                <el-radio-button label="30d">30天</el-radio-button>
                <el-radio-button label="90d">90天</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div class="chart-content">
            <canvas ref="activityChartRef" id="activity-chart"></canvas>
          </div>
        </div>

        <!-- 课程完成率分析 -->
        <div class="chart-container neu-card">
          <div class="chart-header">
            <h3 class="chart-title">
              <font-awesome-icon :icon="['fas', 'graduation-cap']" />
              课程完成率分析
            </h3>
            <div class="chart-legend">
              <div class="legend-item">
                <span class="legend-color" style="background: #4A90E2;"></span>
                <span>已完成</span>
              </div>
              <div class="legend-item">
                <span class="legend-color" style="background: #F5A623;"></span>
                <span>进行中</span>
              </div>
              <div class="legend-item">
                <span class="legend-color" style="background: #D0021B;"></span>
                <span>未开始</span>
              </div>
            </div>
          </div>
          <div class="chart-content">
            <canvas ref="completionChartRef" id="completion-chart"></canvas>
          </div>
        </div>

        <!-- 成绩分布 -->
        <div class="chart-container neu-card">
          <div class="chart-header">
            <h3 class="chart-title">
              <font-awesome-icon :icon="['fas', 'chart-bar']" />
              成绩分布统计
            </h3>
            <el-select v-model="selectedCourse" placeholder="选择课程" class="neu-select">
              <el-option
                v-for="course in courses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              />
            </el-select>
          </div>
          <div class="chart-content">
            <canvas ref="gradeChartRef" id="grade-chart"></canvas>
          </div>
        </div>

        <!-- 学习时长分析 -->
        <div class="chart-container neu-card">
          <div class="chart-header">
            <h3 class="chart-title">
              <font-awesome-icon :icon="['fas', 'clock']" />
              学习时长分析
            </h3>
            <div class="time-stats">
              <div class="time-stat">
                <span class="stat-label">平均学习时长</span>
                <span class="stat-value">{{ averageStudyTime }}</span>
              </div>
              <div class="time-stat">
                <span class="stat-label">总学习时长</span>
                <span class="stat-value">{{ totalStudyTime }}</span>
              </div>
            </div>
          </div>
          <div class="chart-content">
            <canvas ref="timeChartRef" id="time-chart"></canvas>
          </div>
        </div>
      </div>
    </div>

    <!-- 详细数据表格 -->
    <div class="data-tables-section">
      <div class="tables-grid">
        <!-- 学生表现排行 -->
        <div class="table-container neu-card">
          <div class="table-header">
            <h3 class="table-title">
              <font-awesome-icon :icon="['fas', 'trophy']" />
              学生表现排行
            </h3>
            <div class="table-actions">
              <el-input
                v-model="studentSearchKeyword"
                placeholder="搜索学生"
                prefix-icon="Search"
                class="neu-input search-input"
                clearable
              />
            </div>
          </div>
          <div class="table-content">
            <el-table
              :data="filteredStudentRankings"
              class="neu-table"
              stripe
              :header-cell-style="{ background: 'var(--neumorphism-bg)', color: 'var(--text-primary)' }"
            >
              <el-table-column type="index" label="排名" width="60" align="center">
                <template #default="{ $index }">
                  <div class="rank-badge" :class="getRankClass($index)">
                    {{ $index + 1 }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="学生姓名" min-width="120">
                <template #default="{ row }">
                  <div class="student-info">
                    <el-avatar :size="32" :src="row.avatar" class="student-avatar" />
                    <span class="student-name">{{ row.name }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="score" label="综合得分" width="100" align="center">
                <template #default="{ row }">
                  <div class="score-badge" :class="getScoreClass(row.score)">
                    {{ row.score }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="completedCourses" label="完成课程" width="100" align="center" />
              <el-table-column prop="studyTime" label="学习时长" width="100" align="center" />
              <el-table-column prop="lastActive" label="最后活跃" width="120" align="center" />
              <el-table-column label="操作" width="120" align="center">
                <template #default="{ row }">
                  <el-button size="small" type="primary" class="neu-button-small" @click="viewStudentDetail(row)">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 课程统计 -->
        <div class="table-container neu-card">
          <div class="table-header">
            <h3 class="table-title">
              <font-awesome-icon :icon="['fas', 'book']" />
              课程统计概览
            </h3>
            <div class="table-actions">
              <el-select v-model="courseFilter" placeholder="筛选状态" class="neu-select">
                <el-option label="全部" value="all" />
                <el-option label="进行中" value="active" />
                <el-option label="已结束" value="completed" />
                <el-option label="草稿" value="draft" />
              </el-select>
            </div>
          </div>
          <div class="table-content">
            <el-table
              :data="filteredCourseStats"
              class="neu-table"
              stripe
              :header-cell-style="{ background: 'var(--neumorphism-bg)', color: 'var(--text-primary)' }"
            >
              <el-table-column prop="name" label="课程名称" min-width="150" />
              <el-table-column prop="students" label="学生数" width="80" align="center" />
              <el-table-column prop="completionRate" label="完成率" width="100" align="center">
                <template #default="{ row }">
                  <el-progress
                    :percentage="row.completionRate"
                    :stroke-width="8"
                    :show-text="false"
                    class="completion-progress"
                  />
                  <span class="completion-text">{{ row.completionRate }}%</span>
                </template>
              </el-table-column>
              <el-table-column prop="avgScore" label="平均分" width="80" align="center" />
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)" class="status-tag">
                    {{ getStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" align="center">
                <template #default="{ row }">
                  <el-button size="small" type="primary" class="neu-button-small" @click="viewCourseAnalytics(row)">
                    详细分析
                  </el-button>
                  <el-button size="small" class="neu-button-small" @click="manageCourse(row)">
                    管理
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>

    <!-- 学生详情对话框 -->
    <el-dialog v-model="studentDetailVisible" title="学生详细信息" width="800px" class="neu-dialog">
      <div v-if="selectedStudent" class="student-detail">
        <div class="student-profile">
          <el-avatar :size="80" :src="selectedStudent.avatar" class="profile-avatar" />
          <div class="profile-info">
            <h3 class="profile-name">{{ selectedStudent.name }}</h3>
            <p class="profile-id">学号: {{ selectedStudent.id }}</p>
            <div class="profile-stats">
              <div class="stat-item">
                <span class="stat-label">综合得分</span>
                <span class="stat-value">{{ selectedStudent.score }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">完成课程</span>
                <span class="stat-value">{{ selectedStudent.completedCourses }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">学习时长</span>
                <span class="stat-value">{{ selectedStudent.studyTime }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="student-charts">
          <div class="chart-item">
            <h4>学习进度</h4>
            <canvas ref="studentProgressChartRef" id="student-progress-chart"></canvas>
          </div>
          <div class="chart-item">
            <h4>成绩趋势</h4>
            <canvas ref="studentGradeChartRef" id="student-grade-chart"></canvas>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="studentDetailVisible = false" class="neu-button">关闭</el-button>
        <el-button type="primary" @click="exportStudentReport" class="neu-button">导出报告</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import Chart from 'chart.js/auto'

// 响应式数据
const dateRange = ref([new Date(Date.now() - 30 * 24 * 60 * 60 * 1000), new Date()])
const activityPeriod = ref('30d')
const selectedCourse = ref('')
const studentSearchKeyword = ref('')
const courseFilter = ref('all')
const studentDetailVisible = ref(false)
const selectedStudent = ref(null)

// 图表引用
const metricChartRefs = reactive({})
const activityChartRef = ref(null)
const completionChartRef = ref(null)
const gradeChartRef = ref(null)
const timeChartRef = ref(null)
const studentProgressChartRef = ref(null)
const studentGradeChartRef = ref(null)

// 核心指标数据
const coreMetrics = ref([
  {
    key: 'totalStudents',
    title: '总学生数',
    value: '1,234',
    change: '+12.5%',
    changeType: 'positive',
    changeIcon: ['fas', 'arrow-up'],
    icon: ['fas', 'users'],
    gradient: 'linear-gradient(135deg, #4A90E2, #357ABD)'
  },
  {
    key: 'activeCourses',
    title: '活跃课程',
    value: '28',
    change: '+3',
    changeType: 'positive',
    changeIcon: ['fas', 'arrow-up'],
    icon: ['fas', 'book-open'],
    gradient: 'linear-gradient(135deg, #7ED321, #5CB85C)'
  },
  {
    key: 'avgCompletion',
    title: '平均完成率',
    value: '87.3%',
    change: '+5.2%',
    changeType: 'positive',
    changeIcon: ['fas', 'arrow-up'],
    icon: ['fas', 'chart-line'],
    gradient: 'linear-gradient(135deg, #F5A623, #E67E22)'
  },
  {
    key: 'avgScore',
    title: '平均成绩',
    value: '82.1',
    change: '-1.3',
    changeType: 'negative',
    changeIcon: ['fas', 'arrow-down'],
    icon: ['fas', 'star'],
    gradient: 'linear-gradient(135deg, #BD10E0, #9013FE)'
  }
])

// 课程数据
const courses = ref([
  { id: '1', name: 'JavaScript基础' },
  { id: '2', name: 'Vue.js进阶' },
  { id: '3', name: 'Node.js开发' },
  { id: '4', name: '数据库设计' }
])

// 学生排行数据
const studentRankings = ref([
  {
    id: '001',
    name: '张三',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    score: 95.5,
    completedCourses: 12,
    studyTime: '156h',
    lastActive: '2小时前'
  },
  {
    id: '002',
    name: '李四',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    score: 92.3,
    completedCourses: 10,
    studyTime: '142h',
    lastActive: '1天前'
  },
  {
    id: '003',
    name: '王五',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    score: 89.7,
    completedCourses: 11,
    studyTime: '138h',
    lastActive: '3小时前'
  },
  {
    id: '004',
    name: '赵六',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    score: 87.2,
    completedCourses: 9,
    studyTime: '125h',
    lastActive: '5小时前'
  },
  {
    id: '005',
    name: '钱七',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    score: 85.8,
    completedCourses: 8,
    studyTime: '118h',
    lastActive: '1天前'
  }
])

// 课程统计数据
const courseStats = ref([
  {
    id: '1',
    name: 'JavaScript基础',
    students: 156,
    completionRate: 87,
    avgScore: 82.5,
    status: 'active'
  },
  {
    id: '2',
    name: 'Vue.js进阶',
    students: 98,
    completionRate: 73,
    avgScore: 79.2,
    status: 'active'
  },
  {
    id: '3',
    name: 'Node.js开发',
    students: 67,
    completionRate: 65,
    avgScore: 76.8,
    status: 'active'
  },
  {
    id: '4',
    name: '数据库设计',
    students: 134,
    completionRate: 91,
    avgScore: 85.3,
    status: 'completed'
  }
])

// 计算属性
const filteredStudentRankings = computed(() => {
  if (!studentSearchKeyword.value) return studentRankings.value
  return studentRankings.value.filter(student => 
    student.name.toLowerCase().includes(studentSearchKeyword.value.toLowerCase())
  )
})

const filteredCourseStats = computed(() => {
  if (courseFilter.value === 'all') return courseStats.value
  return courseStats.value.filter(course => course.status === courseFilter.value)
})

const averageStudyTime = computed(() => {
  const total = studentRankings.value.reduce((sum, student) => {
    return sum + parseInt(student.studyTime.replace('h', ''))
  }, 0)
  return Math.round(total / studentRankings.value.length) + 'h'
})

const totalStudyTime = computed(() => {
  const total = studentRankings.value.reduce((sum, student) => {
    return sum + parseInt(student.studyTime.replace('h', ''))
  }, 0)
  return total + 'h'
})

// 图表实例
let charts = {}

// 方法
const handleDateRangeChange = (dates) => {
  if (dates) {
    // 重新加载数据
    loadAnalyticsData()
  }
}

const exportReport = () => {
  ElMessage.success('报告导出功能开发中...')
}

const getRankClass = (index) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return 'rank-normal'
}

const getScoreClass = (score) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 70) return 'score-average'
  return 'score-poor'
}

const getStatusType = (status) => {
  const types = {
    active: 'success',
    completed: 'info',
    draft: 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    active: '进行中',
    completed: '已结束',
    draft: '草稿'
  }
  return texts[status] || '未知'
}

const viewStudentDetail = (student) => {
  selectedStudent.value = student
  studentDetailVisible.value = true
  nextTick(() => {
    initStudentCharts()
  })
}

const viewCourseAnalytics = (course) => {
  ElMessage.info(`查看课程 "${course.name}" 的详细分析`)
}

const manageCourse = (course) => {
  ElMessage.info(`管理课程 "${course.name}"`)
}

const exportStudentReport = () => {
  ElMessage.success('学生报告导出功能开发中...')
}

const loadAnalyticsData = () => {
  // 模拟数据加载
  ElMessage.success('数据已更新')
}

// 初始化图表
const initCharts = () => {
  // 初始化核心指标小图表
  coreMetrics.value.forEach(metric => {
    const canvas = metricChartRefs[metric.key]
    if (canvas) {
      const ctx = canvas.getContext('2d')
      charts[metric.key] = new Chart(ctx, {
        type: 'line',
        data: {
          labels: ['', '', '', '', '', '', ''],
          datasets: [{
            data: [65, 59, 80, 81, 56, 55, 70],
            borderColor: metric.gradient.includes('4A90E2') ? '#4A90E2' : '#7ED321',
            backgroundColor: 'transparent',
            borderWidth: 2,
            pointRadius: 0,
            tension: 0.4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          },
          scales: {
            x: { display: false },
            y: { display: false }
          },
          elements: {
            point: { radius: 0 }
          }
        }
      })
    }
  })

  // 学生活跃度趋势图
  if (activityChartRef.value) {
    const ctx = activityChartRef.value.getContext('2d')
    charts.activity = new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        datasets: [{
          label: '活跃学生数',
          data: [120, 135, 142, 138, 155, 98, 87],
          borderColor: '#4A90E2',
          backgroundColor: 'rgba(74, 144, 226, 0.1)',
          borderWidth: 3,
          fill: true,
          tension: 0.4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: {
              color: 'rgba(255, 255, 255, 0.1)'
            },
            ticks: {
              color: 'var(--text-secondary)'
            }
          },
          x: {
            grid: {
              color: 'rgba(255, 255, 255, 0.1)'
            },
            ticks: {
              color: 'var(--text-secondary)'
            }
          }
        }
      }
    })
  }

  // 课程完成率饼图
  if (completionChartRef.value) {
    const ctx = completionChartRef.value.getContext('2d')
    charts.completion = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['已完成', '进行中', '未开始'],
        datasets: [{
          data: [65, 25, 10],
          backgroundColor: ['#4A90E2', '#F5A623', '#D0021B'],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        cutout: '70%'
      }
    })
  }

  // 成绩分布柱状图
  if (gradeChartRef.value) {
    const ctx = gradeChartRef.value.getContext('2d')
    charts.grade = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['0-60', '60-70', '70-80', '80-90', '90-100'],
        datasets: [{
          label: '学生人数',
          data: [5, 15, 35, 45, 25],
          backgroundColor: [
            '#D0021B',
            '#F5A623',
            '#7ED321',
            '#4A90E2',
            '#BD10E0'
          ],
          borderRadius: 8,
          borderSkipped: false
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: {
              color: 'rgba(255, 255, 255, 0.1)'
            },
            ticks: {
              color: 'var(--text-secondary)'
            }
          },
          x: {
            grid: {
              display: false
            },
            ticks: {
              color: 'var(--text-secondary)'
            }
          }
        }
      }
    })
  }

  // 学习时长分析
  if (timeChartRef.value) {
    const ctx = timeChartRef.value.getContext('2d')
    charts.time = new Chart(ctx, {
      type: 'radar',
      data: {
        labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        datasets: [{
          label: '平均学习时长(小时)',
          data: [3.2, 4.1, 3.8, 4.5, 3.9, 2.1, 1.8],
          borderColor: '#7ED321',
          backgroundColor: 'rgba(126, 211, 33, 0.2)',
          borderWidth: 2,
          pointBackgroundColor: '#7ED321',
          pointBorderColor: '#fff',
          pointBorderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          r: {
            beginAtZero: true,
            grid: {
              color: 'rgba(255, 255, 255, 0.1)'
            },
            pointLabels: {
              color: 'var(--text-secondary)'
            },
            ticks: {
              color: 'var(--text-secondary)',
              backdropColor: 'transparent'
            }
          }
        }
      }
    })
  }
}

const initStudentCharts = () => {
  // 学生进度图表
  if (studentProgressChartRef.value) {
    const ctx = studentProgressChartRef.value.getContext('2d')
    charts.studentProgress = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['JavaScript基础', 'Vue.js进阶', 'Node.js开发', '数据库设计'],
        datasets: [{
          label: '完成进度',
          data: [100, 85, 60, 30],
          backgroundColor: '#4A90E2',
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          y: {
            beginAtZero: true,
            max: 100,
            ticks: {
              callback: function(value) {
                return value + '%'
              }
            }
          }
        }
      }
    })
  }

  // 学生成绩趋势
  if (studentGradeChartRef.value) {
    const ctx = studentGradeChartRef.value.getContext('2d')
    charts.studentGrade = new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['第1周', '第2周', '第3周', '第4周', '第5周', '第6周'],
        datasets: [{
          label: '成绩',
          data: [78, 82, 85, 88, 92, 95],
          borderColor: '#7ED321',
          backgroundColor: 'rgba(126, 211, 33, 0.1)',
          borderWidth: 3,
          fill: true,
          tension: 0.4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          y: {
            beginAtZero: true,
            max: 100
          }
        }
      }
    })
  }
}

onMounted(() => {
  nextTick(() => {
    initCharts()
  })
})
</script>

<style scoped>
/* 继承TeacherLayout的CSS变量 */
.teacher-analytics {
  padding: var(--spacing-lg);
  background: var(--neumorphism-bg);
  min-height: 100vh;
}

/* 页面标题样式 */
.page-header {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-backdrop);
  -webkit-backdrop-filter: var(--glass-backdrop);
  border: 1px solid var(--glass-border);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(0, 47, 167, 0.1) 0%, 
    rgba(81, 123, 77, 0.1) 100%);
  pointer-events: none;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 1;
}

.header-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 0 8px 16px rgba(0, 47, 167, 0.3);
}

.header-text {
  flex: 1;
  margin-left: var(--spacing-lg);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  color: var(--text-secondary);
  margin: var(--spacing-xs) 0 0 0;
  font-size: 16px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

/* 核心指标卡片 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.metric-card {
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  box-shadow: 
    8px 8px 16px var(--neumorphism-shadow-dark),
    -8px -8px 16px var(--neumorphism-shadow-light);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  transition: all var(--transition-normal);
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    12px 12px 24px var(--neumorphism-shadow-dark),
    -12px -12px 24px var(--neumorphism-shadow-light);
}

.metric-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.1) 0%, 
    rgba(255, 255, 255, 0.05) 100%);
  pointer-events: none;
}

.metric-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
}

.metric-content {
  flex: 1;
  position: relative;
  z-index: 1;
}

.metric-title {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 var(--spacing-xs) 0;
  font-weight: 500;
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.metric-change {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 12px;
  font-weight: 600;
}

.metric-change.positive {
  color: var(--success-color);
}

.metric-change.negative {
  color: var(--danger-color);
}

.metric-chart {
  width: 80px;
  height: 40px;
  position: relative;
  z-index: 1;
}

/* 图表区域 */
.charts-section {
  margin-bottom: var(--spacing-xl);
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: var(--spacing-lg);
}

.chart-container {
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  box-shadow: 
    8px 8px 16px var(--neumorphism-shadow-dark),
    -8px -8px 16px var(--neumorphism-shadow-light);
  position: relative;
  overflow: hidden;
}

.chart-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.1) 0%, 
    rgba(255, 255, 255, 0.05) 100%);
  pointer-events: none;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  position: relative;
  z-index: 1;
}

.chart-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.chart-controls {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.chart-legend {
  display: flex;
  gap: var(--spacing-md);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 12px;
  color: var(--text-secondary);
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.time-stats {
  display: flex;
  gap: var(--spacing-lg);
}

.time-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--primary-color);
}

.chart-content {
  height: 300px;
  position: relative;
  z-index: 1;
}

/* 数据表格区域 */
.data-tables-section {
  margin-bottom: var(--spacing-xl);
}

.tables-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(600px, 1fr));
  gap: var(--spacing-lg);
}

.table-container {
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  box-shadow: 
    8px 8px 16px var(--neumorphism-shadow-dark),
    -8px -8px 16px var(--neumorphism-shadow-light);
  position: relative;
  overflow: hidden;
}

.table-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.1) 0%, 
    rgba(255, 255, 255, 0.05) 100%);
  pointer-events: none;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  position: relative;
  z-index: 1;
}

.table-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.table-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.search-input {
  width: 200px;
}

.table-content {
  position: relative;
  z-index: 1;
}

/* 表格样式 */
.neu-table {
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-md);
  overflow: hidden;
}

.neu-table :deep(.el-table__body tr:hover > td) {
  background-color: rgba(74, 144, 226, 0.1) !important;
}

.neu-table :deep(.el-table__row--striped td) {
  background-color: rgba(255, 255, 255, 0.02) !important;
}

/* 排名徽章 */
.rank-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.rank-gold {
  background: linear-gradient(135deg, #FFD700, #FFA500);
}

.rank-silver {
  background: linear-gradient(135deg, #C0C0C0, #A0A0A0);
}

.rank-bronze {
  background: linear-gradient(135deg, #CD7F32, #B8860B);
}

.rank-normal {
  background: linear-gradient(135deg, #6C757D, #495057);
}

/* 学生信息 */
.student-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.student-avatar {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.student-name {
  font-weight: 500;
  color: var(--text-primary);
}

/* 分数徽章 */
.score-badge {
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-sm);
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.score-excellent {
  background: linear-gradient(135deg, #7ED321, #5CB85C);
}

.score-good {
  background: linear-gradient(135deg, #4A90E2, #357ABD);
}

.score-average {
  background: linear-gradient(135deg, #F5A623, #E67E22);
}

.score-poor {
  background: linear-gradient(135deg, #D0021B, #B71C1C);
}

/* 完成率进度条 */
.completion-progress {
  margin-bottom: var(--spacing-xs);
}

.completion-text {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 状态标签 */
.status-tag {
  border-radius: var(--border-radius-sm);
  font-size: 12px;
}

/* 按钮样式 */
.neu-button, .neu-button-small {
  background: var(--neumorphism-bg);
  border: none;
  border-radius: var(--border-radius-md);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: 
    4px 4px 8px var(--neumorphism-shadow-dark),
    -4px -4px 8px var(--neumorphism-shadow-light);
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.neu-button {
  padding: var(--spacing-md) var(--spacing-lg);
}

.neu-button-small {
  padding: var(--spacing-xs) var(--spacing-sm);
  font-size: 12px;
}

.neu-button:hover, .neu-button-small:hover {
  transform: translateY(-1px);
  box-shadow: 
    6px 6px 12px var(--neumorphism-shadow-dark),
    -6px -6px 12px var(--neumorphism-shadow-light);
}

.neu-button.el-button--primary {
  background: linear-gradient(135deg, var(--primary-color), #4A90E2);
  color: white;
}

/* 表单控件样式 */
.neu-date-picker :deep(.el-input__wrapper) {
  background: var(--neumorphism-bg);
  border: none;
  border-radius: var(--border-radius-md);
  box-shadow: 
    inset 4px 4px 8px var(--neumorphism-shadow-dark),
    inset -4px -4px 8px var(--neumorphism-shadow-light);
}

.neu-input :deep(.el-input__wrapper) {
  background: var(--neumorphism-bg);
  border: none;
  border-radius: var(--border-radius-md);
  box-shadow: 
    inset 4px 4px 8px var(--neumorphism-shadow-dark),
    inset -4px -4px 8px var(--neumorphism-shadow-light);
}

.neu-select :deep(.el-select__wrapper) {
  background: var(--neumorphism-bg);
  border: none;
  border-radius: var(--border-radius-md);
  box-shadow: 
    inset 4px 4px 8px var(--neumorphism-shadow-dark),
    inset -4px -4px 8px var(--neumorphism-shadow-light);
}

.neu-radio-group :deep(.el-radio-button__inner) {
  background: var(--neumorphism-bg);
  border: none;
  box-shadow: 
    4px 4px 8px var(--neumorphism-shadow-dark),
    -4px -4px 8px var(--neumorphism-shadow-light);
  color: var(--text-secondary);
}

.neu-radio-group :deep(.el-radio-button__original:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, var(--primary-color), #4A90E2);
  color: white;
  box-shadow: 
    inset 4px 4px 8px rgba(0, 0, 0, 0.2),
    inset -4px -4px 8px rgba(255, 255, 255, 0.1);
}

/* 学生详情对话框 */
.neu-dialog :deep(.el-dialog) {
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-lg);
  box-shadow: 
    8px 8px 16px var(--neumorphism-shadow-dark),
    -8px -8px 16px var(--neumorphism-shadow-light);
}

.student-detail {
  display: grid;
  gap: var(--spacing-xl);
}

.student-profile {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-lg);
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-md);
  box-shadow: 
    inset 4px 4px 8px var(--neumorphism-shadow-dark),
    inset -4px -4px 8px var(--neumorphism-shadow-light);
}

.profile-avatar {
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.profile-info {
  flex: 1;
}

.profile-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.profile-id {
  color: var(--text-secondary);
  margin: 0 0 var(--spacing-md) 0;
}

.profile-stats {
  display: flex;
  gap: var(--spacing-lg);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
}

.student-charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-lg);
}

.chart-item {
  background: var(--neumorphism-bg);
  border-radius: var(--border-radius-md);
  padding: var(--spacing-lg);
  box-shadow: 
    4px 4px 8px var(--neumorphism-shadow-dark),
    -4px -4px 8px var(--neumorphism-shadow-light);
}

.chart-item h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-md) 0;
  text-align: center;
}

.chart-item canvas {
  height: 200px;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .metrics-grid {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  }
  
  .charts-grid {
    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  }
  
  .tables-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1024px) {
  .header-content {
    flex-direction: column;
    gap: var(--spacing-lg);
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .metrics-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
  
  .metric-card {
    flex-direction: column;
    text-align: center;
  }
  
  .metric-chart {
    width: 100%;
    height: 60px;
  }
}

@media (max-width: 768px) {
  .teacher-analytics {
    padding: var(--spacing-md);
  }
  
  .page-header {
    padding: var(--spacing-lg);
  }
  
  .metrics-grid {
    grid-template-columns: 1fr;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-header {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: stretch;
  }
  
  .time-stats {
    justify-content: center;
  }
  
  .table-header {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: stretch;
  }
  
  .search-input {
    width: 100%;
  }
  
  .student-charts {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .neu-card, .chart-container, .table-container {
    padding: var(--spacing-lg);
  }
  
  .metric-card {
    padding: var(--spacing-md);
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .metric-value {
    font-size: 24px;
  }
  
  .chart-content {
    height: 250px;
  }
}
</style>