<template>
  <div class="assessments-page">
    <!-- 页面头部 -->
    <header class="page-header frosted-glass" role="banner">
      <div class="header-content">
        <h1 class="page-title">
          <i class="fas fa-clipboard-list" aria-hidden="true"></i>
          我的测评
        </h1>
        <p class="page-description">查看和参加课程测评，检验学习成果</p>
      </div>
      <div class="header-stats">
        <div class="stat-item neumorphism-raised">
          <div class="stat-number">{{ stats.total }}</div>
          <div class="stat-label">总测评</div>
        </div>
        <div class="stat-item neumorphism-raised">
          <div class="stat-number">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
        <div class="stat-item neumorphism-raised">
          <div class="stat-number">{{ stats.pending }}</div>
          <div class="stat-label">待完成</div>
        </div>
        <div class="stat-item neumorphism-raised">
          <div class="stat-number">{{ stats.avgScore }}%</div>
          <div class="stat-label">平均分</div>
        </div>
      </div>
    </header>

    <!-- 筛选和搜索 -->
    <section class="filter-section frosted-glass" aria-label="测评筛选">
      <div class="filter-left">
        <SearchBar
            v-model="searchQuery"
            placeholder="搜索测评名称或课程..."
            size="default"
            :show-filter="false"
            class="neumorphism-inset"
        />
      </div>
      <div class="filter-right">
        <el-select
            v-model="filterStatus"
            placeholder="状态筛选"
            class="custom-select neumorphism-inset"
            clearable
        >
          <el-option label="全部状态" value=""/>
          <el-option label="未开始" value="not_started"/>
          <el-option label="进行中" value="in_progress"/>
          <el-option label="已完成" value="completed"/>
          <el-option label="已过期" value="expired"/>
        </el-select>
        <el-select
            v-model="filterType"
            placeholder="类型筛选"
            class="custom-select neumorphism-inset"
            clearable
        >
          <el-option label="全部类型" value=""/>
          <el-option label="课堂测验" value="quiz"/>
          <el-option label="章节测试" value="chapter_test"/>
          <el-option label="期中考试" value="midterm"/>
          <el-option label="期末考试" value="final"/>
          <el-option label="作业" value="assignment"/>
        </el-select>
        <el-select
            v-model="sortBy"
            placeholder="排序方式"
            class="custom-select neumorphism-inset"
        >
          <el-option label="截止时间" value="deadline"/>
          <el-option label="创建时间" value="created_at"/>
          <el-option label="分数" value="score"/>
          <el-option label="难度" value="difficulty"/>
        </el-select>
      </div>
    </section>

    <!-- 测评列表 -->
    <main class="assessments-list" role="main">
      <!-- 空状态 -->
      <div v-if="filteredAssessments.length === 0" class="empty-state neumorphism-inset">
        <div class="empty-icon">
          <i class="fas fa-clipboard-list" aria-hidden="true"></i>
        </div>
        <h3>暂无测评</h3>
        <p>当前没有符合条件的测评</p>
        <el-button type="primary" class="custom-button neumorphism-raised" @click="resetFilters">
          <i class="fas fa-refresh" aria-hidden="true"></i>
          重置筛选
        </el-button>
      </div>

      <!-- 测评网格 -->
      <div v-else class="assessment-grid">
        <article
            v-for="assessment in paginatedAssessments"
            :key="assessment.id"
            class="assessment-card neumorphism-raised"
            :class="getCardClass(assessment.status)"
            tabindex="0"
            @keydown.enter="viewDetails(assessment)"
        >
          <!-- 卡片头部 -->
          <header class="card-header">
            <div class="assessment-type">
              <i :class="getTypeIcon(assessment.type)" aria-hidden="true"></i>
              <span>{{ getTypeName(assessment.type) }}</span>
            </div>
            <div class="assessment-status">
              <el-tag
                  :type="getStatusType(assessment.status)"
                  size="small"
                  class="status-tag"
                  effect="light"
              >
                {{ getStatusName(assessment.status) }}
              </el-tag>
            </div>
          </header>

          <!-- 卡片内容 -->
          <div class="card-content">
            <h3 class="assessment-title">{{ assessment.title }}</h3>
            <p class="assessment-description">{{ assessment.description }}</p>

            <div class="assessment-info">
              <div class="info-item">
                <i class="fas fa-book" aria-hidden="true"></i>
                <span>{{ assessment.courseName }}</span>
              </div>
              <!--              <div class="info-item">
                              <i class="fas fa-question-circle" aria-hidden="true"></i>
                              <span>{{ assessment.questionCount }} 题</span>
                            </div>-->
              <div class="info-item">
                <i class="fas fa-clock" aria-hidden="true"></i>
                <span>{{ assessment.duration }} 分钟</span>
              </div>
              <!--              <div class="info-item">
                              <i class="fas fa-star" aria-hidden="true"></i>
                              <span class="difficulty" :class="`difficulty-${assessment.difficulty}`">
                                {{ getDifficultyName(assessment.difficulty) }}
                              </span>
                            </div>-->
            </div>

            <!-- 时间信息 -->
            <div class="time-info neumorphism-inset">
              <div class="time-item">
                <span class="time-label">
                  <i class="fas fa-play-circle" aria-hidden="true"></i>
                  开始时间
                </span>
                <span class="time-value">{{ formatDate(assessment.startTime) }}</span>
              </div>
              <div class="time-item">
                <span class="time-label">
                  <i class="fas fa-stop-circle" aria-hidden="true"></i>
                  截止时间
                </span>
                <span class="time-value" :class="{ 'urgent': isUrgent(assessment.deadline) }">
                  {{ formatDate(assessment.deadline) }}
                  <i v-if="isUrgent(assessment.deadline)" class="fas fa-exclamation-triangle urgent-icon"
                     aria-hidden="true"></i>
                </span>
              </div>
              <div v-if="assessment.completedAt" class="time-item">
                <span class="time-label">
                  <i class="fas fa-check-circle" aria-hidden="true"></i>
                  完成时间
                </span>
                <span class="time-value">{{ formatDate(assessment.completedAt) }}</span>
              </div>
            </div>

            <!-- 成绩信息 -->
            <div v-if="assessment.score !== null" class="score-info neumorphism-inset">
              <div class="score-display">
                <div class="score-main">
                  <span class="score-number">{{ assessment.score }}</span>
                  <span class="score-total">/{{ assessment.totalScore }}</span>
                </div>
                <div class="score-percentage" :class="getScoreClass(assessment.score, assessment.totalScore)">
                  {{ Math.round((assessment.score / assessment.totalScore) * 100) }}%
                </div>
              </div>
              <el-progress
                  :percentage="Math.round((assessment.score / assessment.totalScore) * 100)"
                  :stroke-width="8"
                  :show-text="false"
                  :color="getProgressColor(assessment.score, assessment.totalScore)"
                  class="score-progress"
              />
            </div>
          </div>

          <!-- 卡片操作 -->
          <footer class="card-actions">
            <div class="action-buttons">
              <el-button
                  v-if="assessment.status === 'not_started'"
                  type="primary"
                  size="default"
                  class="action-btn"
                  @click="startAssessment(assessment)"
                  :disabled="!canStart(assessment)"
                  :loading="loading"
              >
                <i class="fas fa-play" aria-hidden="true"></i>
                开始测评
              </el-button>

              <el-button
                  v-if="assessment.status === 'in_progress'"
                  size="default"
                  class="action-btn neumorphism-raised continue-btn"
                  @click="continueAssessment(assessment)"
                  :loading="loading"
                  style="background-color: #002FA7; color: #FFFFFF !important; border-color: #002FA7;"
              >
                <i class="fas fa-edit" aria-hidden="true"></i>
                继续答题
              </el-button>

              <el-button
                  v-if="assessment.status === 'completed'"
                  type="success"
                  size="default"
                  class="action-btn"
                  @click="viewResult(assessment)"
                  :loading="loading"
              >
                <i class="fas fa-chart-line" aria-hidden="true"></i>
                查看结果
              </el-button>

              <el-button
                  v-if="assessment.status === 'completed' && assessment.allowRetake"
                  size="default"
                  class="action-btn neumorphism-raised"
                  @click="retakeAssessment(assessment)"
                  :loading="loading"
                  style="background-color: #E6A23C; color: #FFFFFF; border-color: #E6A23C;"
              >
                <i class="fas fa-redo" aria-hidden="true"></i>
                重新测评
              </el-button>

              <el-button
                  v-if="assessment.status === 'expired'"
                  disabled
                  size="default"
                  class="action-btn"
                  style="background-color: #F56C6C; color: #FFFFFF; border-color: #F56C6C;"
              >
                <i class="fas fa-times" aria-hidden="true"></i>
                已过期
              </el-button>

              <el-button
                  size="default"
                  class="action-btn neumorphism-inset detail-btn"
                  @click="viewDetails(assessment)"
                  plain
              >
                <i class="fas fa-info-circle" aria-hidden="true"></i>
                详情
              </el-button>
            </div>
          </footer>
        </article>
      </div>
    </main>

    <!-- 分页 -->
    <nav
        v-if="totalAssessments > 0"
        class="pagination-container frosted-glass"
        role="navigation"
        aria-label="测评列表分页导航"
    >
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalAssessments"
          layout="total, sizes, prev, pager, next, jumper"
          class="custom-pagination"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </nav>

    <!-- 测评详情对话框 -->
    <el-dialog
        v-model="showDetailDialog"
        title="测评详情"
        width="900px"
        class="detail-dialog"
        destroy-on-close
        center
    >
      <div v-if="selectedAssessment" class="assessment-detail">
        <section class="detail-section neumorphism-inset">
          <h4 class="section-title">
            <i class="fas fa-info-circle" aria-hidden="true"></i>
            基本信息
          </h4>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">
                <i class="fas fa-heading" aria-hidden="true"></i>
                测评名称
              </span>
              <span class="value">{{ selectedAssessment.title }}</span>
            </div>
            <div class="detail-item">
              <span class="label">
                <i class="fas fa-book" aria-hidden="true"></i>
                所属课程
              </span>
              <span class="value">{{ selectedAssessment.courseName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">
                <i class="fas fa-tag" aria-hidden="true"></i>
                测评类型
              </span>
              <span class="value">{{ getTypeName(selectedAssessment.type) }}</span>
            </div>
            <!--            <div class="detail-item">
                          <span class="label">
                            <i class="fas fa-star" aria-hidden="true"></i>
                            难度等级
                          </span>
                          <span class="value difficulty" :class="`difficulty-${selectedAssessment.difficulty}`">
                            {{ getDifficultyName(selectedAssessment.difficulty) }}
                          </span>
                        </div>
                        <div class="detail-item">
                          <span class="label">
                            <i class="fas fa-question-circle" aria-hidden="true"></i>
                            题目数量
                          </span>
                          <span class="value">{{ selectedAssessment.questionCount }} 题</span>
                        </div>-->
            <div class="detail-item">
              <span class="label">
                <i class="fas fa-clock" aria-hidden="true"></i>
                考试时长
              </span>
              <span class="value">{{ selectedAssessment.duration }} 分钟</span>
            </div>
          </div>
        </section>

        <!-- 时间信息 -->
        <section class="detail-section neumorphism-inset">
          <h4 class="section-title">
            <i class="fas fa-calendar-alt" aria-hidden="true"></i>
            时间信息
          </h4>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">
                <i class="fas fa-play-circle" aria-hidden="true"></i>
                开始时间
              </span>
              <span class="value">{{ formatDate(selectedAssessment.startTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">
                <i class="fas fa-stop-circle" aria-hidden="true"></i>
                截止时间
              </span>
              <span class="value" :class="{ 'urgent': isUrgent(selectedAssessment.deadline) }">
                {{ formatDate(selectedAssessment.deadline) }}
                <i v-if="isUrgent(selectedAssessment.deadline)" class="fas fa-exclamation-triangle urgent-icon"
                   aria-hidden="true"></i>
              </span>
            </div>
            <div v-if="selectedAssessment.completedAt" class="detail-item">
              <span class="label">
                <i class="fas fa-check-circle" aria-hidden="true"></i>
                完成时间
              </span>
              <span class="value">{{ formatDate(selectedAssessment.completedAt) }}</span>
            </div>
          </div>
        </section>

        <!-- 成绩信息 -->
        <section v-if="selectedAssessment.score !== null" class="detail-section neumorphism-inset">
          <h4 class="section-title">
            <i class="fas fa-chart-bar" aria-hidden="true"></i>
            成绩信息
          </h4>
          <div class="score-detail">
            <div class="score-display">
              <div class="score-main">
                <span class="score-number">{{ selectedAssessment.score }}</span>
                <span class="score-total">/{{ selectedAssessment.totalScore }}</span>
              </div>
              <div class="score-percentage"
                   :class="getScoreClass(selectedAssessment.score, selectedAssessment.totalScore)">
                {{ Math.round((selectedAssessment.score / selectedAssessment.totalScore) * 100) }}%
              </div>
            </div>
            <el-progress
                :percentage="Math.round((selectedAssessment.score / selectedAssessment.totalScore) * 100)"
                :stroke-width="12"
                :color="getProgressColor(selectedAssessment.score, selectedAssessment.totalScore)"
                class="score-progress"
            />
          </div>
        </section>

        <!-- 说明 -->
        <section class="detail-section neumorphism-inset">
          <h4 class="section-title">
            <i class="fas fa-file-alt" aria-hidden="true"></i>
            说明
          </h4>
          <p class="description">{{ selectedAssessment.description || '暂无说明' }}</p>
        </section>

        <!-- 考试须知 -->
        <section v-if="selectedAssessment.instructions && selectedAssessment.instructions.length > 0"
                 class="detail-section neumorphism-inset">
          <h4 class="section-title">
            <i class="fas fa-exclamation-circle" aria-hidden="true"></i>
            考试须知
          </h4>
          <ul class="instructions">
            <li v-for="(instruction, index) in selectedAssessment.instructions" :key="index">
              <i class="fas fa-info-circle" aria-hidden="true"></i>
              <span class="instruction-text">{{ instruction }}</span>
            </li>
          </ul>
        </section>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button
              size="default"
              class="dialog-btn neumorphism-inset"
              @click="showDetailDialog = false"
          >
            <i class="fas fa-times" aria-hidden="true"></i>
            关闭
          </el-button>
          <el-button
              v-if="selectedAssessment && canStart(selectedAssessment)"
              type="primary"
              size="default"
              class="dialog-btn"
              style="background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);"
              @click="startFromDialog"
          >
            <i class="fas fa-play" aria-hidden="true"></i>
            开始测评
          </el-button>
          <el-button
              v-if="selectedAssessment && selectedAssessment.status === 'in_progress'"
              type="warning"
              size="default"
              class="dialog-btn neumorphism-raised"
              @click="continueAssessment(selectedAssessment)"
          >
            <i class="fas fa-edit" aria-hidden="true"></i>
            继续答题
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import SearchBar from '@/components/SearchBar.vue'
import {getMyAssessments, startAssessment as startAssessmentAPI} from '@/api/students/stuAPI.js'

const router = useRouter()

// 响应式数据
const searchQuery = ref('')
const filterStatus = ref('')
const filterType = ref('')
const sortBy = ref('deadline')
const currentPage = ref(1)
const pageSize = ref(12)
const showDetailDialog = ref(false)
const selectedAssessment = ref(null)
const loading = ref(false)
const error = ref(null)

// 统计数据
const stats = ref({
  total: 24,
  completed: 18,
  pending: 6,
  avgScore: 85
})

// 测评数据
const assessments = ref([])

/**
 * 加载测评数据
 * @param {string} status - 可选的状态过滤参数
 */
const loadAssessments = async (status = null) => {
  try {
    loading.value = true
    error.value = null

    const data = await getMyAssessments(status)

    // 将后端数据映射到前端数据结构
    assessments.value = data.map(item => {
      // 状态映射：后端 -> 前端
      const mapStatus = (backendStatus) => {
        const statusMap = {
          'not_started': 'not_started',
          'in_progress': 'in_progress',
          'submitted': 'completed',
          'graded': 'completed'
        }
        return statusMap[backendStatus] || 'not_started'
      }

      // 检查是否过期
      const isExpired = () => {
        const now = new Date()
        const endTime = new Date(item.endTime)
        return now > endTime && (item.status === 'not_started' || item.status === 'in_progress')
      }

      const frontendStatus = isExpired() ? 'expired' : mapStatus(item.status)

      /**
       * 类型映射函数：将后端类型转换为前端类型
       * @param {string} backendType - 后端 assessmentType 字段值
       * @returns {string} 前端类型值
       *
       * 映射关系：
       * - ClassroomExam (课堂作业) -> quiz (课堂测验)
       * - ChapterExam (章节作业) -> chapter_test (章节测试)
       * - MidtermExam (期中考试) -> midterm (期中考试)
       * - FinalExam (期末考试) -> final (期末考试)
       * - homework (作业) -> assignment (作业)
       */
      const mapType = (backendType) => {
        const typeMapping = {
          'ClassroomExam': 'quiz',
          'ChapterExam': 'chapter_test',
          'MidtermExam': 'midterm',
          'FinalExam': 'final',
          'homework': 'assignment'
        }
        return typeMapping[backendType] || 'quiz'
      }

      const frontendType = mapType(item.assessmentType)

      return {
        // 后端字段映射
        id: item.assessmentId,
        assessmentId: item.assessmentId,
        title: item.title,
        startTime: item.startTime,
        endTime: item.endTime,
        deadline: item.endTime, // 使用endTime作为deadline
        duration: item.durationMinutes,
        durationMinutes: item.durationMinutes,
        courseId: item.courseId,
        courseName: item.courseTitle,
        courseTitle: item.courseTitle,
        courseCover: item.courseCover,
        status: frontendStatus,
        submitTime: item.submitTime,
        completedAt: item.submitTime, // 使用submitTime作为completedAt
        score: item.score,
        type: frontendType, // 使用映射后的前端类型
        assessmentType: item.assessmentType, // 保留原始后端类型
        canStart: item.canStart,

        // 前端需要的额外字段（设置默认值）
        description: '', // 后端没有description字段，设置为空

        totalScore: 100, // 设置默认总分
        allowRetake: false, // 设置默认值
        instructions: [
          "1. 各位考生应自觉维护考试严肃性、权威性和公信力，自觉诚信考试，在规定考试时间内独立作答，考试期间不与任何第三人交流考试内容，通过诚信考试对自身课程学习效果做到客观的诊断评价",
          "2. 建议考生选择安静适宜的环境进行线上考试，避免干扰",
          "3. 考生应准备用于线上考试的计算机设备，并于考前登录线上考试系统完成计算机设备的兼容性测试。为避免兼容性问题，各考生应在用于线上考试的计算机设备上安装chrome 核心的主流网络浏览器，需注意同时使用多个浏览器页面进入考试系统的，会造成答题数据丢失和混乱",
          "4. 考生可在考试时间（以服务器时间为准）结束前，提交答卷。考试时间结束，系统将自动提交答卷，未保存内容将丢失",
          "5. 上传答题或提交试卷时必须等待上传成功的提示，一般等待时间不超过 45 秒，超过后可刷新页面重试，如重试后依旧未提示上传成功，可尝试使用其他浏览器重试",
          "6. 考试过程中请勿打开其他窗口，请勿使用浏览器的刷新功能，请勿使用浏览器的回退功能，请勿使用浏览器的打印功能，请勿使用浏览器的截图功能，请勿使用浏览器的保存功能，请勿使用浏览器的搜索功能，请勿使用浏览器的翻译功能",
          "7. 课程考试时间结束后半小时内(非存在主观题情况下，若存在主观题需等待教师批改后查看)，考生可以登录系统，在“{XX}”中查看本场考试本人的答题记录"
        ] // 设置默认值
      }
    })

    // 更新统计数据
    updateStats()

  } catch (err) {
    console.error('加载测评数据失败:', err)
    error.value = err.message || '加载测评数据失败'
  } finally {
    loading.value = false
  }
}

/**
 * 更新统计数据
 */
const updateStats = () => {
  const total = assessments.value.length
  const completed = assessments.value.filter(a => a.status === 'completed').length
  const pending = assessments.value.filter(a => a.status === 'not_started' || a.status === 'in_progress').length

  // 计算平均分
  const gradedAssessments = assessments.value.filter(a => a.score !== null && a.score !== undefined)
  const avgScore = gradedAssessments.length > 0
      ? Math.round(gradedAssessments.reduce((sum, a) => sum + Number(a.score), 0) / gradedAssessments.length)
      : 0

  stats.value = {
    total,
    completed,
    pending,
    avgScore
  }
}

// 计算属性
const filteredAssessments = computed(() => {
  let filtered = assessments.value

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(assessment =>
        assessment.title.toLowerCase().includes(query) ||
        assessment.courseName.toLowerCase().includes(query) ||
        assessment.description.toLowerCase().includes(query)
    )
  }

  // 状态过滤
  if (filterStatus.value) {
    filtered = filtered.filter(assessment => assessment.status === filterStatus.value)
  }

  // 类型过滤
  if (filterType.value) {
    filtered = filtered.filter(assessment => assessment.type === filterType.value)
  }

  // 排序
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'deadline':
        return new Date(a.deadline) - new Date(b.deadline)
      case 'created_at':
        return new Date(b.startTime) - new Date(a.startTime)
      case 'score':
        return (b.score || 0) - (a.score || 0)
      case 'difficulty':
        const difficultyOrder = {easy: 1, medium: 2, hard: 3}
        return difficultyOrder[a.difficulty] - difficultyOrder[b.difficulty]
      default:
        return 0
    }
  })

  return filtered
})

const totalAssessments = computed(() => filteredAssessments.value.length)

// 方法

/**
 * 获取测评类型图标
 * @param {string} type - 前端类型值 (quiz, chapter_test, midterm, final, assignment)
 * @returns {string} 图标类名
 */
const getTypeIcon = (type) => {
  const icons = {
    quiz: 'fas fa-question-circle',        // 课堂测验
    chapter_test: 'fas fa-file-alt',       // 章节测试
    midterm: 'fas fa-clipboard-check',     // 期中考试
    final: 'fas fa-graduation-cap',        // 期末考试
    assignment: 'fas fa-tasks'             // 作业
  }
  return icons[type] || 'fas fa-file'
}

/**
 * 获取测评类型名称
 * @param {string} type - 前端类型值 (quiz, chapter_test, midterm, final, assignment)
 * @returns {string} 类型显示名称
 *
 * 映射关系：
 * - quiz (课堂测验) <- ClassroomExam (课堂作业)
 * - chapter_test (章节测试) <- ChapterExam (章节作业)
 * - midterm (期中考试) <- MidtermExam (期中考试)
 * - final (期末考试) <- FinalExam (期末考试)
 * - assignment (作业) <- homework (作业)
 */
const getTypeName = (type) => {
  const names = {
    quiz: '课堂测验',
    chapter_test: '章节测试',
    midterm: '期中考试',
    final: '期末考试',
    assignment: '作业'
  }
  return names[type] || '未知类型'
}

const getStatusType = (status) => {
  const types = {
    not_started: '',
    in_progress: 'warning',
    completed: 'success',
    expired: 'danger'
  }
  return types[status] || ''
}

const getStatusName = (status) => {
  const names = {
    not_started: '未开始',
    in_progress: '进行中',
    completed: '已完成',
    expired: '已过期'
  }
  return names[status] || '未知状态'
}

const getDifficultyName = (difficulty) => {
  const names = {
    easy: '简单',
    medium: '中等',
    hard: '困难'
  }
  return names[difficulty] || '未知难度'
}

const getCardClass = (status) => {
  return {
    'status-not-started': status === 'not_started',
    'status-in-progress': status === 'in_progress',
    'status-completed': status === 'completed',
    'status-expired': status === 'expired'
  }
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const isUrgent = (deadline) => {
  const now = new Date()
  const deadlineDate = new Date(deadline)
  const timeDiff = deadlineDate - now
  const daysDiff = timeDiff / (1000 * 60 * 60 * 24)
  return daysDiff <= 3 && daysDiff > 0
}

const canStart = (assessment) => {
  const now = new Date()
  const startTime = new Date(assessment.startTime)
  const deadline = new Date(assessment.deadline)
  return now >= startTime && now <= deadline && assessment.status === 'not_started'
}

/**
 * 开始测评功能实现
 * @param {Object} assessment - 测评对象
 * @param {string} assessment.id - 测评ID
 * @param {string} assessment.title - 测评标题
 */
const startAssessment = async (assessment) => {
  // 参数验证
  if (!assessment || !assessment.id) {
    ElMessage.error('测评信息无效，请刷新页面后重试')
    return
  }

  // 验证assessmentId格式（确保不为空且为有效字符串）
  const assessmentId = String(assessment.id).trim()
  if (!assessmentId) {
    ElMessage.error('测评ID无效，无法开始测评')
    return
  }

  // 设置加载状态
  loading.value = true

  try {
    // 调用API开始测评
    const request = {
      assessmentId: assessmentId
    }
    
    // 发起测评请求
    await startAssessmentAPI(request)
    
    // API调用成功，显示成功提示
    ElMessage.success(`正在进入测评：${assessment.title}`)
    
    // 跳转到考试页面，携带assessmentId参数
    await router.push({
      name: 'Exam', 
      params: { 
        assessmentId: assessmentId 
      }
    })
    
  } catch (error) {
    // 异常处理
    console.error('开始测评失败:', error)
    
    // 根据错误类型显示不同的错误信息
    let errorMessage = '开始测评失败，请稍后重试'
    
    if (error.message) {
      // 后端返回的具体错误信息
      if (error.message.includes('网络') || error.message.includes('timeout') || error.message.includes('连接')) {
        errorMessage = '网络连接异常，请检查网络后重试'
      } else if (error.message.includes('权限') || error.message.includes('认证')) {
        errorMessage = '权限验证失败，请重新登录后重试'
      } else if (error.message.includes('时间') || error.message.includes('过期')) {
        errorMessage = '测评时间已过期，无法开始'
      } else if (error.message.includes('状态')) {
        errorMessage = '测评状态异常，请刷新页面后重试'
      } else {
        errorMessage = error.message
      }
    }
    
    // 显示错误提示
    ElMessage.error(errorMessage)
    
    // 刷新测评列表以获取最新状态
    try {
      await loadAssessments()
    } catch (refreshError) {
      console.error('刷新测评列表失败:', refreshError)
    }
    
  } finally {
    // 清除加载状态
    loading.value = false
  }
}

/**
 * 继续测评功能实现
 * @param {Object} assessment - 测评对象
 */
const continueAssessment = async (assessment) => {
  // 参数验证
  if (!assessment || !assessment.id) {
    ElMessage.error('测评信息无效，请刷新页面后重试')
    return
  }

  // 验证assessmentId格式
  const assessmentId = String(assessment.id).trim()
  if (!assessmentId) {
    ElMessage.error('测评ID无效，无法继续测评')
    return
  }

  // 设置加载状态
  loading.value = true

  try {
    // 显示提示信息
    ElMessage.success(`正在继续测评：${assessment.title}`)
    
    // 直接跳转到考试页面（继续测评不需要调用API）
    await router.push({
      name: 'Exam', 
      params: { 
        assessmentId: assessmentId 
      }
    })
    
  } catch (error) {
    console.error('继续测评失败:', error)
    ElMessage.error('页面跳转失败，请重试')
  } finally {
    loading.value = false
  }
}

/**
 * 重新测评功能实现
 * @param {Object} assessment - 测评对象
 */
const retakeAssessment = async (assessment) => {
  // 参数验证
  if (!assessment || !assessment.id) {
    ElMessage.error('测评信息无效，请刷新页面后重试')
    return
  }

  // 验证assessmentId格式
  const assessmentId = String(assessment.id).trim()
  if (!assessmentId) {
    ElMessage.error('测评ID无效，无法重新测评')
    return
  }

  // 设置加载状态
  loading.value = true

  try {
    // 调用API开始重新测评
    const request = {
      assessmentId: assessmentId
    }
    
    // 发起重新测评请求
    await startAssessmentAPI(request)
    
    // API调用成功，显示成功提示
    ElMessage.success(`正在重新开始测评：${assessment.title}`)
    
    // 跳转到考试页面，携带assessmentId参数
    await router.push({
      name: 'Exam', 
      params: { 
        assessmentId: assessmentId 
      }
    })
    
  } catch (error) {
    // 异常处理
    console.error('重新测评失败:', error)
    
    // 根据错误类型显示不同的错误信息
    let errorMessage = '重新测评失败，请稍后重试'
    
    if (error.message) {
      if (error.message.includes('网络') || error.message.includes('timeout') || error.message.includes('连接')) {
        errorMessage = '网络连接异常，请检查网络后重试'
      } else if (error.message.includes('权限') || error.message.includes('认证')) {
        errorMessage = '权限验证失败，请重新登录后重试'
      } else if (error.message.includes('时间') || error.message.includes('过期')) {
        errorMessage = '测评时间已过期，无法重新开始'
      } else if (error.message.includes('状态')) {
        errorMessage = '测评状态异常，请刷新页面后重试'
      } else {
        errorMessage = error.message
      }
    }
    
    // 显示错误提示
    ElMessage.error(errorMessage)
    
    // 刷新测评列表以获取最新状态
    try {
      await loadAssessments()
    } catch (refreshError) {
      console.error('刷新测评列表失败:', refreshError)
    }
    
  } finally {
    // 清除加载状态
    loading.value = false
  }
}

const viewResult = (assessment) => {
  // 查看测评结果
  console.log('查看结果:', assessment.id)
}

const viewDetails = (assessment) => {
  selectedAssessment.value = assessment
  showDetailDialog.value = true
}

const startFromDialog = () => {
  showDetailDialog.value = false
  startAssessment(selectedAssessment.value)
}

// 新增方法
const getScoreClass = (score, totalScore) => {
  const percentage = (score / totalScore) * 100
  if (percentage >= 90) return 'excellent'
  if (percentage >= 80) return 'good'
  if (percentage >= 70) return 'average'
  if (percentage >= 60) return 'pass'
  return 'fail'
}

const getProgressColor = (score, totalScore) => {
  const percentage = (score / totalScore) * 100
  if (percentage >= 90) return '#67c23a'
  if (percentage >= 80) return '#409eff'
  if (percentage >= 70) return '#e6a23c'
  if (percentage >= 60) return '#f56c6c'
  return '#909399'
}

const canStartAssessment = (assessment) => {
  return assessment.status === 'not_started' || assessment.status === 'pending'
}

const handleKeydown = (event, assessment) => {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault()
    viewDetails(assessment)
  }
}

const resetFilters = () => {
  searchQuery.value = ''
  filterStatus.value = ''
  filterType.value = ''
  currentPage.value = 1
}

const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
}

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
}

// 分页数据
const paginatedAssessments = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredAssessments.value.slice(start, end)
})

onMounted(() => {
  // 组件挂载时加载测评数据
  loadAssessments()
})
</script>

<style scoped>
/* 新拟态设计变量 */
:root {
  --neumorphism-bg: #f0f2f5;
  --neumorphism-shadow-light: #ffffff;
  --neumorphism-shadow-dark: #d1d9e6;
  --glass-bg: rgba(255, 255, 255, 0.25);
  --glass-border: rgba(255, 255, 255, 0.18);
  --glass-shadow: rgba(31, 38, 135, 0.37);
}

.assessments-page {
  min-height: 100vh;
  padding: 20px;
  position: relative;
}

.assessments-page::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="50" cy="50" r="0.5" fill="%23ffffff" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>') repeat;
  pointer-events: none;
  z-index: 0;
}

/* 新拟态效果 */
.neumorphism-raised {
  background: var(--neumorphism-bg);
  box-shadow: 8px 8px 16px var(--neumorphism-shadow-dark),
  -8px -8px 16px var(--neumorphism-shadow-light);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.neumorphism-inset {
  background: var(--neumorphism-bg);
  box-shadow: inset 4px 4px 8px var(--neumorphism-shadow-dark),
  inset -4px -4px 8px var(--neumorphism-shadow-light);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

/* 磨砂玻璃效果 */
.frosted-glass {
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  box-shadow: 0 8px 32px var(--glass-shadow);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30px;
  margin-bottom: 30px;
  border-radius: 20px;
  position: relative;
  z-index: 1;
}

.header-content h1 {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 15px;
  text-shadow: 0 2px 4px rgba(255, 255, 255, 0.8);
}

.header-content i {
  color: #002FA7;
  filter: drop-shadow(0 2px 4px rgba(255, 255, 255, 0.8));
}

.page-description {
  color: #606266;
  margin: 0;
  font-size: 16px;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
}

.header-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 24px;
  min-width: 120px;
  border-radius: 16px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
  border-radius: inherit;
  z-index: -1;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 12px 12px 24px var(--neumorphism-shadow-dark),
  -12px -12px 24px var(--neumorphism-shadow-light);
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #002FA7;
  display: block;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px;
  margin-bottom: 30px;
  gap: 20px;
  border-radius: 20px;
  position: relative;
  z-index: 1;
}

.filter-left {
  flex: 1;
  max-width: 400px;
}

.filter-right {
  display: flex;
  gap: 15px;
}

.custom-select {
  width: 160px;
  border-radius: 12px;
}

.custom-select.neumorphism-inset {
  background: var(--neumorphism-bg);
}

.assessments-list {
  margin-bottom: 30px;
  position: relative;
  z-index: 1;
}

.empty-state {
  text-align: center;
  padding: 80px 30px;
  border-radius: 20px;
  position: relative;
}

.empty-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #002FA7;
}

.empty-state h3 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.empty-state p {
  color: #606266;
  margin-bottom: 24px;
  font-size: 16px;
}

.reset-btn {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  transform: translateY(-1px);
}

.assessment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(420px, 1fr));
  gap: 24px;
}

.assessment-card {
  padding: 28px;
  border-radius: 20px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
}

.assessment-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
  border-radius: inherit;
  z-index: -1;
}

.assessment-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 12px 12px 32px var(--neumorphism-shadow-dark),
  -12px -12px 32px var(--neumorphism-shadow-light),
  0 8px 32px rgba(102, 126, 234, 0.3);
}

.assessment-card:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.3);
}

.assessment-card.status-not-started {
  border-left: 4px solid #409eff;
}

.assessment-card.status-in-progress {
  border-left: 4px solid #e6a23c;
  animation: pulse 2s infinite;
}

.assessment-card.status-completed {
  border-left: 4px solid #67c23a;
}

.assessment-card.status-expired {
  border-left: 4px solid #f56c6c;
  opacity: 0.7;
}

@keyframes pulse {
  0%, 100% {
    border-left-color: #e6a23c;
  }
  50% {
    border-left-color: #f39c12;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.assessment-type {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #002FA7;
  font-weight: 600;
  font-size: 15px;
}

.assessment-type i {
  width: 18px;
  font-size: 16px;
}

.status-tag {
  border-radius: 8px;
  font-weight: 500;
  padding: 4px 12px;
}

.card-content {
  margin-bottom: 24px;
}

.assessment-title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 12px 0;
  line-height: 1.3;
  letter-spacing: -0.02em;
}

.assessment-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 20px;
}

.assessment-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  transition: all 0.2s ease;
}

.info-item:hover {
  background: rgba(255, 255, 255, 0.7);
  transform: translateY(-1px);
}

.info-item i {
  width: 16px;
  color: #002FA7;
  font-size: 14px;
}

.difficulty {
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 12px;
}

.difficulty-easy {
  background: #e8f5e8;
  color: #52c41a;
}

.difficulty-medium {
  background: #fff7e6;
  color: #fa8c16;
}

.difficulty-hard {
  background: #fff2f0;
  color: #ff4d4f;
}

.time-info {
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.time-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 13px;
  padding: 6px 0;
}

.time-item:last-child {
  margin-bottom: 0;
}

.time-label {
  color: #606266;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.time-label i {
  color: #002FA7;
  width: 14px;
}

.time-value {
  color: #303133;
  font-weight: 600;
  font-size: 12px;
}

.time-value.urgent {
  color: #ff4757;
  font-weight: 700;
  animation: blink 1.5s infinite;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0.6;
  }
}

.score-info {
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.score-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.score-main {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.score-number {
  font-size: 28px;
  font-weight: 800;
  color: #303133;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.score-total {
  font-size: 16px;
  color: #606266;
  font-weight: 500;
}

.score-percentage {
  font-size: 18px;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.5);
}

.score-percentage.excellent {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.1);
}

.score-percentage.good {
  color: #1890ff;
  background: rgba(24, 144, 255, 0.1);
}

.score-percentage.average {
  color: #fa8c16;
  background: rgba(250, 140, 22, 0.1);
}

.score-percentage.pass {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}

.score-percentage.fail {
  color: #909399;
  background: rgba(144, 147, 153, 0.1);
}

.score-progress {
  margin-top: 8px;
}

.card-actions {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 20px;
  margin-top: 20px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.action-btn {
  border-radius: 10px;
  padding: 10px 16px;
  font-weight: 600;
  font-size: 13px;
  transition: all 0.3s ease;
  border: none;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.action-btn:hover::before {
  left: 100%;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.action-btn i {
  margin-right: 6px;
}

.detail-btn {
  margin-left: auto;
  background: rgba(255, 255, 255, 0.2);
  color: #002FA7 !important;
  border: 1px solid rgba(0, 47, 167, 0.3);
}

.detail-btn:hover {
  background: rgba(0, 47, 167, 0.1);
  border-color: #002FA7;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 30px 0;
  position: relative;
  z-index: 1;
}

.pagination-container .el-pagination {
  background: transparent;
}

.pagination-container .el-pagination .el-pager li {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  margin: 0 4px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.pagination-container .el-pagination .el-pager li:hover,
.pagination-container .el-pagination .el-pager li.is-active {
  background: #002FA7;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 47, 167, 0.3);
}

.assessment-detail {
  max-height: 75vh;
  overflow-y: auto;
  padding: 8px;
}

.assessment-detail::-webkit-scrollbar {
  width: 6px;
}

.assessment-detail::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.assessment-detail::-webkit-scrollbar-thumb {
  background: rgba(0, 47, 167, 0.3);
  border-radius: 3px;
}

.assessment-detail::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 47, 167, 0.5);
}

.detail-section {
  margin-bottom: 28px;
  border-radius: 16px;
  padding: 20px;
  position: relative;
}

.detail-section h4 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 700;
  padding-bottom: 12px;
  border-bottom: 2px solid #002FA7;
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-section h4 i {
  color: #002FA7;
  font-size: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 12px;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.detail-item:hover {
  background: rgba(255, 255, 255, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.detail-item .label {
  font-size: 13px;
  color: #606266;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.detail-item .label i {
  color: #002FA7;
  width: 14px;
}

.detail-item .value {
  font-size: 15px;
  color: #303133;
  font-weight: 700;
  line-height: 1.4;
}

.description {
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

/* 考试须知样式优化 */
.instructions {
  margin: 0;
  padding: 0;
  list-style: none;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.instructions li {
  color: var(--text-primary);
  margin-bottom: 16px;
  line-height: 1.6;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  border-left: 4px solid #002FA7;
  position: relative;
  transition: all 0.3s ease;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.instructions li:last-child {
  margin-bottom: 0;
}

.instructions li:hover {
  background: rgba(255, 255, 255, 0.05);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 47, 167, 0.1);
}

.instructions li i {
  color: #002FA7;
  font-size: 14px;
  margin-top: 2px;
  flex-shrink: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 47, 167, 0.1);
  border-radius: 50%;
  transition: all 0.3s ease;
}

.instructions li:hover i {
  background: rgba(0, 47, 167, 0.2);
  transform: scale(1.1);
}

.instructions li .instruction-text {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
}

/* 考试须知标题样式增强 */
.detail-section .section-title {
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-section .section-title i {
  color: #002FA7;
  font-size: 18px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 24px 0 0 0;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  margin-top: 20px;
}

.dialog-footer .el-button {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.3s ease;
  border: none;
  position: relative;
  overflow: hidden;
  color: #FFFFFF !important;
}

.dialog-footer .el-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.dialog-footer .el-button:hover::before {
  left: 100%;
}

.dialog-footer .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.requirements-list {
  list-style: none;
  padding: 0;
  margin: 16px 0;
}

.requirements-list li {
  padding: 8px 0;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.requirements-list li i {
  color: #002FA7;
  width: 16px;
  flex-shrink: 0;
}

/* 按钮颜色增强 */
.el-button--primary {
  color: #FFFFFF !important;
}

.el-button--success {
  color: #FFFFFF !important;
}

.el-button--warning {
  color: #FFFFFF !important;
}

.el-button--danger {
  color: #FFFFFF !important;
}

.el-button--info {
  color: #FFFFFF !important;
}

.el-button[disabled] {
  color: #C0C4CC !important;
}

.custom-button {
  color: #FFFFFF !important;
}

.dialog-btn.neumorphism-inset {
  color: #303133 !important;
  background: rgba(255, 255, 255, 0.8);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .assessment-grid {
    grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  }
}

@media (max-width: 768px) {
  .assessments-page {
    padding: 15px;
  }

  .page-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .header-stats {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
    width: 100%;
  }

  .filter-section {
    flex-direction: column;
    gap: 15px;
    padding: 20px;
  }

  .filter-right {
    width: 100%;
    justify-content: space-between;
  }

  .neu-select {
    flex: 1;
    min-width: 0;
  }

  .assessment-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .assessment-info {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .card-actions {
    flex-direction: column;
    gap: 8px;
  }

  .detail-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }

  .page-header {
    padding: 20px;
  }

  .header-content h1 {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .assessments-page {
    padding: 10px;
  }

  .stat-cards {
    grid-template-columns: 1fr;
  }

  .assessment-card {
    padding: 20px 16px;
  }

  .card-actions .action-buttons {
    flex-direction: column;
    gap: 8px;
  }

  .action-btn {
    width: 100%;
    justify-content: center;
  }

  .dialog-footer {
    flex-direction: column;
    gap: 12px;
  }

  .dialog-footer .el-button {
    width: 100%;
  }
}
</style>