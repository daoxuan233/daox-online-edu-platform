<template>
  <div ref="pageRef" class="course-review-page">
    <section ref="heroRef" class="hero-shell">
      <div class="hero-copy">
        <p class="eyebrow">Admin Course Review Desk</p>
        <h1 class="hero-title">课程审核与上下架工作台</h1>
        <p class="hero-description">
          管理员负责处理待审课程、已发布课程治理和下架后的重新上架或归档动作，
          所有操作都会直接调用课程状态机接口并向教师发送结果通知。
        </p>
        <div class="hero-actions">
          <el-button type="primary" class="primary-action" :loading="loading" @click="refreshWorkbench">
            <font-awesome-icon :icon="['fas', 'arrows-rotate']" />
            刷新工作台
          </el-button>
          <el-button class="ghost-action" @click="navigateToAuditLogs">
            <font-awesome-icon :icon="['fas', 'file-shield']" />
            查看审计日志
          </el-button>
        </div>
      </div>

      <div class="hero-focus glass-panel">
        <span class="focus-label">当前焦点</span>
        <strong class="focus-title">{{ spotlight.title }}</strong>
        <p class="focus-description">{{ spotlight.description }}</p>
        <div class="focus-metrics">
          <div class="focus-metric">
            <span>待审核</span>
            <strong>{{ boardStats.pending }}</strong>
          </div>
          <div class="focus-metric">
            <span>已下架</span>
            <strong>{{ boardStats.takenDown }}</strong>
          </div>
          <div class="focus-metric">
            <span>已发布</span>
            <strong>{{ boardStats.published }}</strong>
          </div>
        </div>
      </div>
    </section>

    <section ref="workflowRef" class="workflow-shell glass-panel">
      <div class="section-heading">
        <div>
          <p class="section-eyebrow">Workflow</p>
          <h2>状态机与管理员职责</h2>
        </div>
        <span class="section-chip">所有流转均同步后端审核接口</span>
      </div>
      <div class="workflow-track">
        <article
          v-for="(step, index) in COURSE_WORKFLOW_STEPS"
          :key="step.key"
          class="workflow-step"
          :style="{ '--step-accent': getCourseStatusMeta(step.key).accent, '--step-soft': getCourseStatusMeta(step.key).soft }"
        >
          <span class="step-index">0{{ index + 1 }}</span>
          <div class="step-copy">
            <strong>{{ step.label }}</strong>
            <p>{{ step.description }}</p>
          </div>
        </article>
      </div>
    </section>

    <section ref="statsRef" class="stats-grid">
      <article
        v-for="card in summaryCards"
        :key="card.key"
        class="stat-card glass-panel"
        :style="{ '--card-accent': card.accent, '--card-soft': card.soft }"
      >
        <div class="stat-icon">
          <font-awesome-icon :icon="['fas', card.icon]" />
        </div>
        <div class="stat-copy">
          <span>{{ card.label }}</span>
          <strong>{{ card.value }}</strong>
          <small>{{ card.description }}</small>
        </div>
      </article>
    </section>

    <div class="page-grid">
      <aside class="policy-column">
        <article class="policy-card glass-panel">
          <h3>审核守则</h3>
          <ul class="policy-list">
            <li v-for="rule in reviewRules" :key="rule">{{ rule }}</li>
          </ul>
        </article>

        <article class="policy-card glass-panel">
          <h3>操作边界</h3>
          <div
            v-for="statusKey in ['pending', 'published', 'taken_down', 'archived']"
            :key="statusKey"
            class="status-brief"
          >
            <span
              class="status-pill"
              :style="{
                '--pill-accent': getCourseStatusMeta(statusKey).accent,
                '--pill-soft': getCourseStatusMeta(statusKey).soft
              }"
            >
              {{ getCourseStatusMeta(statusKey).label }}
            </span>
            <p>{{ getCourseStatusMeta(statusKey).description }}</p>
          </div>
        </article>
      </aside>

      <section ref="boardShellRef" class="board-shell glass-panel">
        <header class="board-header">
          <div>
            <p class="section-eyebrow">Workbench</p>
            <h2>课程处理面板</h2>
            <p class="board-description">{{ currentBoardMeta.description }}</p>
          </div>

          <div class="board-toolbar">
            <el-input
              v-model.trim="keyword"
              class="toolbar-input"
              clearable
              placeholder="搜索课程标题、简介或教师 ID"
            >
              <template #prefix>
                <font-awesome-icon :icon="['fas', 'magnifying-glass']" />
              </template>
            </el-input>

            <el-select v-model="categoryFilter" class="toolbar-select" placeholder="全部分类" clearable>
              <el-option label="全部分类" value="" />
              <el-option
                v-for="category in categoryOptions"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>

            <el-select v-model="sortBy" class="toolbar-select" placeholder="排序方式">
              <el-option label="最近更新" value="updated_desc" />
              <el-option label="最早创建" value="created_asc" />
              <el-option label="标题 A-Z" value="title_asc" />
            </el-select>
          </div>
        </header>

        <div ref="tabsRef" class="board-tabs">
          <button
            v-for="tab in boardTabs"
            :key="tab.key"
            type="button"
            class="board-tab"
            :class="{ active: activeBoard === tab.key }"
            @click="activeBoard = tab.key"
          >
            <span>{{ tab.label }}</span>
            <strong>{{ tab.count }}</strong>
          </button>
        </div>

        <div class="board-state-bar">
          <span
            class="status-pill"
            :style="{
              '--pill-accent': currentBoardMeta.accent,
              '--pill-soft': currentBoardMeta.soft
            }"
          >
            {{ currentBoardMeta.label }}
          </span>
          <span class="state-copy">{{ filteredBoardCourses.length }} 门课程匹配当前筛选条件</span>
        </div>

        <div v-if="loading" class="loading-shell">
          <div class="loading-orb"></div>
          <p>正在同步课程审核数据...</p>
        </div>

        <div v-else-if="filteredBoardCourses.length === 0" class="empty-shell">
          <div class="empty-icon">
            <font-awesome-icon :icon="['fas', 'inbox']" />
          </div>
          <h3>当前没有可处理课程</h3>
          <p>尝试切换工作台分组或调整搜索条件。</p>
        </div>

        <div v-else ref="boardRef" class="review-grid">
          <article
            v-for="course in filteredBoardCourses"
            :key="course.id"
            class="review-card"
            :style="{
              '--status-accent': getCourseStatusMeta(course.status).accent,
              '--status-soft': getCourseStatusMeta(course.status).soft
            }"
          >
            <div class="review-cover">
              <img v-if="course.cover" :src="course.cover" :alt="course.title" />
              <div v-else class="cover-fallback">{{ buildInitials(course.title) }}</div>
              <span class="status-badge">{{ getCourseStatusMeta(course.status).label }}</span>
            </div>

            <div class="review-body">
              <div class="title-row">
                <div>
                  <h3>{{ course.title }}</h3>
                  <p class="category-line">{{ course.categoryName }}</p>
                </div>
                <span class="course-id">{{ shortId(course.id) }}</span>
              </div>

              <el-tooltip
                v-if="hasCourseDescriptionOverflow(course.description)"
                popper-class="course-description-tooltip"
                placement="top-start"
                :show-after="140"
                :enterable="false"
              >
                <template #content>
                  <div v-if="hoveredDescriptionCourseId === course.id" class="description-tooltip-copy">
                    {{ normalizeCourseDescriptionText(course.description) }}
                  </div>
                </template>
                <p
                  class="review-description description-trigger is-truncated"
                  tabindex="0"
                  @mouseenter="activateDescriptionPreview(course.id)"
                  @mouseleave="clearDescriptionPreview(course.id)"
                  @focus="activateDescriptionPreview(course.id)"
                  @blur="clearDescriptionPreview(course.id)"
                >
                  {{ getCourseDescriptionPreview(course.description) }}
                </p>
              </el-tooltip>
              <p v-else class="review-description">{{ normalizeCourseDescriptionText(course.description) }}</p>

              <div class="meta-grid">
                <span>教师 {{ course.teacherDisplay }}</span>
                <span>学习人数 {{ course.enrollmentCount }}</span>
                <span>创建于 {{ formatDate(course.createdAt) }}</span>
                <span>更新于 {{ formatDate(course.updatedAt) }}</span>
              </div>

              <div class="mini-track">
                <span
                  v-for="step in COURSE_WORKFLOW_STEPS"
                  :key="`${course.id}-${step.key}`"
                  class="track-node"
                  :class="{
                    complete: getWorkflowStepIndex(course.status) > COURSE_WORKFLOW_STEPS.findIndex((item) => item.key === step.key),
                    current: step.key === course.status
                  }"
                ></span>
              </div>

              <p class="status-description">{{ getCourseStatusMeta(course.status).description }}</p>

              <div class="action-row">
                <template v-if="course.status === 'pending'">
                  <el-button type="primary" class="action-primary" @click="approveCourse(course)">
                    审核通过
                  </el-button>
                  <el-button class="action-warning" @click="openCommentDialog('reject', course)">
                    驳回
                  </el-button>
                </template>

                <template v-else-if="course.status === 'published'">
                  <el-button class="action-warning" @click="openCommentDialog('take_down', course)">
                    下架课程
                  </el-button>
                </template>

                <template v-else-if="course.status === 'taken_down'">
                  <el-button type="primary" class="action-primary" @click="republishCourse(course)">
                    重新上架
                  </el-button>
                  <el-button class="action-danger" @click="openCommentDialog('archive', course)">
                    归档课程
                  </el-button>
                </template>

                <template v-else>
                  <span class="readonly-text">该状态仅供查看，无需继续处理。</span>
                </template>
              </div>
            </div>
          </article>
        </div>
      </section>
    </div>

    <el-dialog
      v-model="workflowDialog.visible"
      :title="workflowDialog.title"
      width="520px"
      class="workflow-dialog"
      modal-class="workflow-dialog-overlay"
      append-to-body
      align-center
      destroy-on-close
    >
      <div
        class="dialog-hero"
        :style="{
          '--dialog-accent': currentDialogTone.accent,
          '--dialog-soft': currentDialogTone.soft
        }"
      >
        <span class="dialog-badge">{{ currentDialogTone.badge }}</span>
        <p class="dialog-description">{{ workflowDialog.description }}</p>
      </div>
      <div class="dialog-body">
        <div class="dialog-course">
          <span>当前课程</span>
          <strong>{{ workflowDialog.course?.title }}</strong>
        </div>
        <label class="dialog-field-label" for="workflow-comment">操作说明</label>
        <el-input
          id="workflow-comment"
          v-model.trim="workflowDialog.comment"
          type="textarea"
          :rows="5"
          maxlength="500"
          show-word-limit
          :placeholder="workflowDialog.placeholder"
        />
        <p class="dialog-tip">该说明会写入审核日志，并同步发送给教师作为审核结果通知。</p>
      </div>
      <template #footer>
        <el-button @click="closeWorkflowDialog">取消</el-button>
        <el-button type="primary" :loading="dialogSubmitting" @click="submitWorkflowDialog">
          {{ workflowDialog.confirmText }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { gsap } from 'gsap'
import {
  approveAdminCourseReview,
  archiveAdminCourse,
  getAdminCategoryTree,
  getAdminCourseList,
  getAdminPendingReviewCourses,
  rejectAdminCourseReview,
  republishAdminCourse,
  takeDownAdminCourse
} from '@/api/admin/adminManagement.js'
import {
  COURSE_WORKFLOW_STEPS,
  compareCourseStatus,
  getCourseDescriptionPreview,
  getCourseStatusMeta,
  hasCourseDescriptionOverflow,
  getWorkflowStepIndex,
  normalizeCourseDescriptionText,
  normalizeCourseStatus
} from '@/utils/courseWorkflow.js'

const router = useRouter()

const pageRef = ref(null)
const heroRef = ref(null)
const workflowRef = ref(null)
const statsRef = ref(null)
const boardShellRef = ref(null)
const tabsRef = ref(null)
const boardRef = ref(null)

const loading = ref(false)
const dialogSubmitting = ref(false)
const allCourses = ref([])
const pendingCourses = ref([])
const categoryOptions = ref([])

const activeBoard = ref('pending')
const keyword = ref('')
const categoryFilter = ref('')
const sortBy = ref('updated_desc')
const hoveredDescriptionCourseId = ref('')

const workflowDialog = ref({
  visible: false,
  action: '',
  title: '',
  description: '',
  placeholder: '',
  confirmText: '',
  requireComment: false,
  comment: '',
  course: null
})

let enterContext = null
let boardTween = null
let dialogTween = null

const reviewRules = [
  '只对处于待审核状态的课程执行通过或驳回，避免越权跳转。',
  '下架与归档动作建议填写原因，便于教师侧通知与审计追踪。',
  '重新上架前确认课程内容、分类与发布时间仍满足平台要求。'
]

/**
 * 递归展开分类树，便于下拉筛选与名称映射。
 *
 * @param {Array} nodes 分类树节点
 * @param {Array} bucket 承接结果的数组
 * @returns {Array} 扁平化后的分类数组
 */
const flattenCategoryTree = (nodes, bucket = []) => {
  ;(nodes || []).forEach((node) => {
    bucket.push({
      id: node.id,
      name: node.name
    })
    if (Array.isArray(node.children) && node.children.length) {
      flattenCategoryTree(node.children, bucket)
    }
  })
  return bucket
}

/**
 * 解析日期值。
 *
 * @param {string|Date|null|undefined} value 日期值
 * @returns {Date|null} 解析后的日期对象
 */
const parseDateValue = (value) => {
  if (!value) {
    return null
  }
  const dateValue = value instanceof Date ? value : new Date(value)
  return Number.isNaN(dateValue.getTime()) ? null : dateValue
}

/**
 * 构建管理员端课程展示模型。
 *
 * @param {Array} courseList 原始课程列表
 * @param {Map<string, string>} categoryLookup 分类映射
 * @returns {Array} 前端课程数据
 */
const normalizeAdminCourses = (courseList, categoryLookup) => {
  return (Array.isArray(courseList) ? courseList : []).map((course) => ({
    id: course.id,
    title: course.title || '未命名课程',
    description: course.description || '暂无课程简介',
    cover: course.coverImageUrl || '',
    status: normalizeCourseStatus(course.status),
    teacherId: course.teacherId || '',
    teacherDisplay: course.teacherId ? `ID·${String(course.teacherId).slice(-6)}` : '未识别教师',
    categoryId: course.categoryId || '',
    categoryName: categoryLookup.get(course.categoryId) || '未分类',
    enrollmentCount: Number(course.enrollmentCount || 0),
    createdAt: parseDateValue(course.createdAt),
    updatedAt: parseDateValue(course.updatedAt || course.createdAt)
  }))
}

/**
 * 是否开启减少动态效果模式。
 *
 * @returns {boolean} 是否减少动态
 */
const prefersReducedMotion = () => {
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

/**
 * 格式化时间展示。
 *
 * @param {Date|null} value 日期对象
 * @returns {string} 格式化字符串
 */
const formatDate = (value) => {
  if (!value) {
    return '时间未知'
  }
  return value.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 提取课程标题首字，用于封面占位。
 *
 * @param {string} title 课程标题
 * @returns {string} 首字串
 */
const buildInitials = (title) => {
  const safeTitle = String(title || '课程').trim()
  return safeTitle.slice(0, 2)
}

/**
 * 压缩显示长 ID，方便管理员快速比对。
 *
 * @param {string} id 课程ID
 * @returns {string} 缩略 ID
 */
const shortId = (id) => {
  if (!id) {
    return 'ID 缺失'
  }
  return `${id.slice(0, 4)}...${id.slice(-4)}`
}

/**
 * 激活课程简介完整内容预览。
 *
 * @param {string} courseId 课程ID
 */
const activateDescriptionPreview = (courseId) => {
  hoveredDescriptionCourseId.value = courseId
}

/**
 * 清理课程简介完整内容预览。
 *
 * @param {string} courseId 课程ID
 */
const clearDescriptionPreview = (courseId) => {
  if (hoveredDescriptionCourseId.value === courseId) {
    hoveredDescriptionCourseId.value = ''
  }
}

const boardStats = computed(() => {
  const countByStatus = (status) => allCourses.value.filter((course) => course.status === status).length
  return {
    pending: pendingCourses.value.length,
    published: countByStatus('published'),
    takenDown: countByStatus('taken_down'),
    archived: countByStatus('archived')
  }
})

const summaryCards = computed(() => ([
  {
    key: 'pending',
    label: '待审核课程',
    value: boardStats.value.pending,
    description: '等待管理员审核通过或驳回',
    icon: 'clipboard-check',
    accent: getCourseStatusMeta('pending').accent,
    soft: getCourseStatusMeta('pending').soft
  },
  {
    key: 'published',
    label: '已发布课程',
    value: boardStats.value.published,
    description: '当前对学生开放的课程数量',
    icon: 'tower-broadcast',
    accent: getCourseStatusMeta('published').accent,
    soft: getCourseStatusMeta('published').soft
  },
  {
    key: 'taken_down',
    label: '已下架课程',
    value: boardStats.value.takenDown,
    description: '等待重新上架或归档处理',
    icon: 'box-archive',
    accent: getCourseStatusMeta('taken_down').accent,
    soft: getCourseStatusMeta('taken_down').soft
  },
  {
    key: 'archived',
    label: '已归档课程',
    value: boardStats.value.archived,
    description: '处于终态的课程存量',
    icon: 'folder-open',
    accent: getCourseStatusMeta('archived').accent,
    soft: getCourseStatusMeta('archived').soft
  }
]))

const spotlight = computed(() => {
  if (boardStats.value.pending > 0) {
    return {
      title: '优先清理待审核队列',
      description: `当前有 ${boardStats.value.pending} 门课程等待审核，建议先处理发布前的阻塞项。`
    }
  }
  if (boardStats.value.takenDown > 0) {
    return {
      title: '关注已下架课程去向',
      description: '下架课程需要明确重新上架还是归档，避免课程长期停留在半完成状态。'
    }
  }
  return {
    title: '发布面整体稳定',
    description: '当前没有积压的待审课程，可通过筛选关注已发布课程的治理与归档。'
  }
})

const boardTabs = computed(() => ([
  { key: 'pending', label: '待审核', count: boardStats.value.pending },
  { key: 'published', label: '已发布', count: boardStats.value.published },
  { key: 'taken_down', label: '已下架', count: boardStats.value.takenDown },
  { key: 'archived', label: '已归档', count: boardStats.value.archived }
]))

const currentDialogTone = computed(() => {
  const toneMap = {
    reject: {
      ...getCourseStatusMeta('draft'),
      badge: '退回草稿'
    },
    take_down: {
      ...getCourseStatusMeta('taken_down'),
      badge: '下架治理'
    },
    archive: {
      ...getCourseStatusMeta('archived'),
      badge: '终态归档'
    }
  }

  return toneMap[workflowDialog.value.action] || {
    ...getCourseStatusMeta('pending'),
    badge: '课程流转'
  }
})

const currentBoardMeta = computed(() => {
  const metaMap = {
    pending: {
      ...getCourseStatusMeta('pending'),
      description: '处理教师提交的课程审核请求，可执行通过或驳回。'
    },
    published: {
      ...getCourseStatusMeta('published'),
      description: '对已发布课程进行运营治理，可执行下架操作。'
    },
    taken_down: {
      ...getCourseStatusMeta('taken_down'),
      description: '对已下架课程决定重新上架或归档。'
    },
    archived: {
      ...getCourseStatusMeta('archived'),
      description: '归档课程仅做查看，不再提供继续流转动作。'
    }
  }
  return metaMap[activeBoard.value]
})

const currentBoardCourses = computed(() => {
  if (activeBoard.value === 'pending') {
    return pendingCourses.value
  }
  return allCourses.value.filter((course) => course.status === activeBoard.value)
})

const filteredBoardCourses = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase()
  let result = [...currentBoardCourses.value]

  if (normalizedKeyword) {
    result = result.filter((course) => {
      return [course.title, course.description, course.teacherId, course.categoryName]
        .filter(Boolean)
        .some((field) => String(field).toLowerCase().includes(normalizedKeyword))
    })
  }

  if (categoryFilter.value) {
    result = result.filter((course) => course.categoryId === categoryFilter.value)
  }

  result.sort((firstCourse, secondCourse) => {
    if (sortBy.value === 'created_asc') {
      return (firstCourse.createdAt?.getTime() || 0) - (secondCourse.createdAt?.getTime() || 0)
    }
    if (sortBy.value === 'title_asc') {
      return firstCourse.title.localeCompare(secondCourse.title, 'zh-CN')
    }
    const statusDelta = compareCourseStatus(firstCourse.status, secondCourse.status)
    if (statusDelta !== 0 && activeBoard.value === 'pending') {
      return statusDelta
    }
    return (secondCourse.updatedAt?.getTime() || 0) - (firstCourse.updatedAt?.getTime() || 0)
  })

  return result
})

/**
 * 加载管理员课程审核工作台数据。
 *
 * @returns {Promise<void>} 异步结果
 */
const loadWorkbenchData = async () => {
  loading.value = true
  try {
    const [courseList, pendingList, categoryTree] = await Promise.all([
      getAdminCourseList(),
      getAdminPendingReviewCourses(),
      getAdminCategoryTree().catch(() => [])
    ])

    const flatCategories = flattenCategoryTree(Array.isArray(categoryTree) ? categoryTree : [])
    const categoryLookup = new Map(flatCategories.map((item) => [item.id, item.name]))

    categoryOptions.value = flatCategories
    allCourses.value = normalizeAdminCourses(courseList, categoryLookup)
    pendingCourses.value = normalizeAdminCourses(pendingList, categoryLookup)
  } catch (error) {
    console.error('加载管理员课程审核工作台失败:', error)
    ElMessage.error(error?.message || '加载课程审核工作台失败')
    allCourses.value = []
    pendingCourses.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 播放页面首次入场动画。
 */
const playEnterAnimation = () => {
  enterContext?.revert()
  enterContext = gsap.context(() => {
    const statCards = statsRef.value?.querySelectorAll('.stat-card') || []

    if (prefersReducedMotion()) {
      gsap.set([heroRef.value, workflowRef.value, boardShellRef.value, statCards], { opacity: 1, y: 0 })
      return
    }

    const timeline = gsap.timeline({ defaults: { ease: 'power3.out' } })
    timeline
      .fromTo(heroRef.value, { opacity: 0, y: 28 }, { opacity: 1, y: 0, duration: 0.56 })
      .fromTo(workflowRef.value, { opacity: 0, y: 24 }, { opacity: 1, y: 0, duration: 0.46 }, '-=0.28')
      .fromTo(statCards, { opacity: 0, y: 22 }, { opacity: 1, y: 0, duration: 0.42, stagger: 0.08 }, '-=0.2')
      .fromTo(boardShellRef.value, { opacity: 0, y: 24 }, { opacity: 1, y: 0, duration: 0.42 }, '-=0.16')
  }, pageRef.value)
}

/**
 * 播放工作台卡片动画。
 */
const animateBoardCards = () => {
  if (prefersReducedMotion()) {
    return
  }
  boardTween?.kill()
  const cards = boardRef.value?.querySelectorAll('.review-card') || []
  if (!cards.length) {
    return
  }
  boardTween = gsap.fromTo(
    cards,
    { opacity: 0, y: 18, scale: 0.985 },
    { opacity: 1, y: 0, scale: 1, duration: 0.38, ease: 'power2.out', stagger: 0.06 }
  )
}

/**
 * 刷新管理员工作台数据。
 *
 * @returns {Promise<void>} 异步结果
 */
const refreshWorkbench = async () => {
  await loadWorkbenchData()
  await nextTick()
  animateBoardCards()
  ElMessage.success('课程审核工作台已刷新')
}

/**
 * 审核通过课程。
 *
 * @param {object} course 课程对象
 * @returns {Promise<void>} 异步结果
 */
const approveCourse = async (course) => {
  try {
    await ElMessageBox.confirm(
      `确定审核通过课程“${course.title}”吗？课程将立即发布并向教师发送结果通知。`,
      '审核通过',
      {
        confirmButtonText: '确认通过',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    await approveAdminCourseReview(course.id)
    ElMessage.success('课程已审核通过')
    await loadWorkbenchData()
    await nextTick()
    animateBoardCards()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '审核通过失败')
    }
  }
}

/**
 * 重新上架课程。
 *
 * @param {object} course 课程对象
 * @returns {Promise<void>} 异步结果
 */
const republishCourse = async (course) => {
  try {
    await ElMessageBox.confirm(
      `确定将课程“${course.title}”重新上架吗？学生会再次收到课程开放通知。`,
      '重新上架',
      {
        confirmButtonText: '确认上架',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    await republishAdminCourse(course.id)
    ElMessage.success('课程已重新上架')
    await loadWorkbenchData()
    await nextTick()
    animateBoardCards()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '重新上架失败')
    }
  }
}

/**
 * 打开需要填写说明的工作流弹窗。
 *
 * @param {'reject'|'take_down'|'archive'} action 动作编码
 * @param {object} course 课程对象
 */
const openCommentDialog = (action, course) => {
  const dialogConfigMap = {
    reject: {
      title: '驳回课程审核',
      description: '驳回后课程会退回草稿，教师将直接收到这段审核说明。',
      placeholder: '请输入驳回原因，例如课程信息不完整、封面不规范或目录内容不足。',
      confirmText: '确认驳回',
      requireComment: true
    },
    take_down: {
      title: '下架已发布课程',
      description: '下架会同步通知教师和学生，请明确写出治理原因。',
      placeholder: '请输入下架说明，例如内容整改、分类错误或发布窗口结束。',
      confirmText: '确认下架',
      requireComment: true
    },
    archive: {
      title: '归档下架课程',
      description: '归档后课程进入终态，不再允许继续流转。',
      placeholder: '请输入归档说明，例如课程周期结束、内容停运或合并迁移。',
      confirmText: '确认归档',
      requireComment: true
    }
  }

  workflowDialog.value = {
    visible: true,
    action,
    course,
    comment: '',
    ...dialogConfigMap[action]
  }
}

/**
 * 关闭工作流弹窗并重置状态。
 */
const closeWorkflowDialog = () => {
  workflowDialog.value = {
    visible: false,
    action: '',
    title: '',
    description: '',
    placeholder: '',
    confirmText: '',
    requireComment: false,
    comment: '',
    course: null
  }
}

/**
 * 提交带备注的工作流动作。
 *
 * @returns {Promise<void>} 异步结果
 */
const submitWorkflowDialog = async () => {
  const trimmedComment = workflowDialog.value.comment.trim()
  if (workflowDialog.value.requireComment && !trimmedComment) {
    ElMessage.warning('请填写操作说明后再提交')
    return
  }

  try {
    dialogSubmitting.value = true

    if (workflowDialog.value.action === 'reject') {
      await rejectAdminCourseReview(workflowDialog.value.course.id, trimmedComment)
      ElMessage.success('课程已驳回并退回草稿')
    }

    if (workflowDialog.value.action === 'take_down') {
      await takeDownAdminCourse(workflowDialog.value.course.id, trimmedComment)
      ElMessage.success('课程已下架')
    }

    if (workflowDialog.value.action === 'archive') {
      await archiveAdminCourse(workflowDialog.value.course.id, trimmedComment)
      ElMessage.success('课程已归档')
    }

    closeWorkflowDialog()
    await loadWorkbenchData()
    await nextTick()
    animateBoardCards()
  } catch (error) {
    console.error('提交课程工作流动作失败:', error)
    ElMessage.error(error?.message || '课程工作流操作失败')
  } finally {
    dialogSubmitting.value = false
  }
}

const navigateToAuditLogs = () => {
  router.push('/admin/audit-logs')
}

watch(
  () => [activeBoard.value, keyword.value, categoryFilter.value, sortBy.value],
  async () => {
    await nextTick()
    animateBoardCards()
  }
)

watch(
  () => workflowDialog.value.visible,
  async (visible) => {
    dialogTween?.kill()

    if (!visible || prefersReducedMotion()) {
      return
    }

    await nextTick()
    const overlayElement = document.querySelector('.workflow-dialog-overlay')
    const dialogElement = document.querySelector('.workflow-dialog .el-dialog') || document.querySelector('.workflow-dialog')

    if (dialogElement) {
      dialogTween = gsap.timeline({ defaults: { ease: 'power2.out' } })
      if (overlayElement) {
        dialogTween.fromTo(overlayElement, { opacity: 0 }, { opacity: 1, duration: 0.2 }, 0)
      }
      dialogTween.fromTo(
        dialogElement,
        { opacity: 0, y: 26, scale: 0.96 },
        { opacity: 1, y: 0, scale: 1, duration: 0.3 },
        0
      )
    }
  }
)

onMounted(async () => {
  await loadWorkbenchData()
  await nextTick()
  playEnterAnimation()
  animateBoardCards()
})

onUnmounted(() => {
  enterContext?.revert()
  boardTween?.kill()
  dialogTween?.kill()
})
</script>

<style scoped>
.course-review-page {
  min-height: 100%;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background:
    radial-gradient(circle at top left, rgba(14, 165, 233, 0.12), transparent 32%),
    radial-gradient(circle at 92% 10%, rgba(245, 158, 11, 0.1), transparent 24%),
    linear-gradient(180deg, #f7fbfd 0%, #edf4f7 100%);
}

.glass-panel {
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(255, 255, 255, 0.72);
  box-shadow: 0 22px 60px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border-radius: 24px;
}

.hero-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(320px, 0.9fr);
  gap: 20px;
}

.hero-copy,
.hero-focus {
  padding: 28px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(255, 255, 255, 0.84);
  box-shadow: 0 24px 56px rgba(15, 23, 42, 0.08);
}

.eyebrow,
.section-eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #0f766e;
  font-weight: 700;
}

.hero-title,
.section-heading h2,
.board-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 30px;
  line-height: 1.12;
}

.hero-description,
.board-description,
.focus-description {
  margin: 14px 0 0;
  color: #526072;
  line-height: 1.7;
}

.hero-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.primary-action,
.ghost-action,
.action-primary,
.action-warning,
.action-danger {
  height: 42px;
  border-radius: 14px;
  font-weight: 600;
}

.primary-action,
.action-primary {
  border: none;
  background: linear-gradient(135deg, #0f766e 0%, #14b8a6 100%);
  box-shadow: 0 16px 30px rgba(15, 118, 110, 0.22);
}

.ghost-action,
.action-warning,
.action-danger {
  border: 1px solid rgba(148, 163, 184, 0.22);
  background: rgba(255, 255, 255, 0.86);
  color: #1e293b;
}

.action-warning:hover {
  color: #b45309;
  border-color: rgba(180, 83, 9, 0.26);
}

.action-danger:hover {
  color: #b91c1c;
  border-color: rgba(185, 28, 28, 0.24);
}

.hero-focus {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.9), rgba(238, 246, 246, 0.94)),
    linear-gradient(135deg, rgba(15, 118, 110, 0.08), rgba(37, 99, 235, 0.06));
}

.focus-label {
  display: inline-flex;
  width: fit-content;
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #155e75;
  background: rgba(207, 250, 254, 0.88);
}

.focus-title {
  margin-top: 18px;
  color: #0f172a;
  font-size: 26px;
  line-height: 1.18;
}

.focus-metrics {
  margin-top: 22px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.focus-metric {
  padding: 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(255, 255, 255, 0.78);
}

.focus-metric span,
.stat-copy span,
.category-line,
.meta-grid span,
.readonly-text {
  color: #64748b;
  font-size: 13px;
}

.focus-metric strong,
.stat-copy strong {
  display: block;
  margin-top: 6px;
  color: #0f172a;
  font-size: 24px;
}

.workflow-shell,
.board-shell,
.policy-card {
  padding: 24px;
}

.section-heading,
.board-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.section-chip {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(240, 249, 255, 0.88);
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 700;
}

.workflow-track {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.workflow-step {
  padding: 16px;
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), var(--step-soft));
  border: 1px solid rgba(255, 255, 255, 0.85);
}

.step-index {
  color: var(--step-accent);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

.step-copy strong {
  display: block;
  margin-top: 10px;
  color: #0f172a;
}

.step-copy p,
.policy-list li,
.status-brief p,
.status-description,
.review-description,
.dialog-description {
  margin: 8px 0 0;
  color: #526072;
  line-height: 1.65;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  padding: 20px;
  display: flex;
  gap: 16px;
  align-items: center;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.92), var(--card-soft));
}

.stat-icon {
  width: 54px;
  height: 54px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.86);
  color: var(--card-accent);
  font-size: 18px;
}

.stat-copy small {
  display: block;
  margin-top: 8px;
  color: #64748b;
}

.page-grid {
  display: grid;
  grid-template-columns: minmax(260px, 320px) minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.policy-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.policy-card h3 {
  margin: 0;
  font-size: 18px;
  color: #0f172a;
}

.policy-list {
  margin: 16px 0 0;
  padding-left: 18px;
}

.status-brief + .status-brief {
  margin-top: 14px;
}

.status-pill,
.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: fit-content;
  padding: 7px 12px;
  border-radius: 999px;
  color: var(--pill-accent, var(--status-accent));
  background: var(--pill-soft, var(--status-soft));
  font-size: 12px;
  font-weight: 700;
}

.board-toolbar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.toolbar-input,
.toolbar-select {
  width: 220px;
}

.toolbar-input :deep(.el-input__wrapper),
.toolbar-select :deep(.el-select__wrapper) {
  min-height: 42px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: none;
}

.board-tabs {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.board-tab {
  border: 1px solid rgba(148, 163, 184, 0.16);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  padding: 14px 16px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.board-tab span {
  display: block;
  color: #64748b;
  font-size: 13px;
}

.board-tab strong {
  display: block;
  margin-top: 8px;
  color: #0f172a;
  font-size: 22px;
}

.board-tab.active {
  border-color: rgba(15, 118, 110, 0.22);
  box-shadow: 0 18px 32px rgba(15, 118, 110, 0.12);
  transform: translateY(-2px);
}

.board-state-bar {
  margin-top: 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.state-copy {
  color: #526072;
}

.loading-shell,
.empty-shell {
  min-height: 260px;
  display: grid;
  place-items: center;
  text-align: center;
  color: #64748b;
}

.loading-orb {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  border: 4px solid rgba(20, 184, 166, 0.18);
  border-top-color: #0f766e;
  animation: spin 1s linear infinite;
}

.empty-icon {
  width: 68px;
  height: 68px;
  border-radius: 22px;
  display: grid;
  place-items: center;
  margin: 0 auto 14px;
  background: rgba(226, 232, 240, 0.72);
  color: #64748b;
  font-size: 22px;
}

.review-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.review-card {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr);
  gap: 16px;
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.96), var(--status-soft));
  border: 1px solid rgba(255, 255, 255, 0.84);
  box-shadow: 0 18px 34px rgba(15, 23, 42, 0.06);
}

.review-cover {
  position: relative;
  border-radius: 18px;
  overflow: hidden;
  min-height: 156px;
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.12), rgba(148, 163, 184, 0.16));
}

.review-cover img,
.cover-fallback {
  width: 100%;
  height: 100%;
}

.review-cover img {
  object-fit: cover;
}

.cover-fallback {
  display: grid;
  place-items: center;
  color: var(--status-accent);
  font-size: 24px;
  font-weight: 700;
  background:
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.8), transparent 32%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.82), var(--status-soft));
}

.status-badge {
  position: absolute;
  top: 10px;
  left: 10px;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.title-row h3,
.empty-shell h3 {
  margin: 0;
  color: #0f172a;
}

.course-id {
  color: #64748b;
  font-size: 12px;
  white-space: nowrap;
}

.review-description {
  min-height: 44px;
}

.description-trigger {
  display: inline-flex;
  align-items: center;
  width: fit-content;
}

.description-trigger.is-truncated {
  cursor: help;
  border-bottom: 1px dashed rgba(15, 118, 110, 0.26);
  transition: color 0.2s ease, border-color 0.2s ease;
}

.description-trigger.is-truncated:hover,
.description-trigger.is-truncated:focus-visible {
  color: #0f172a;
  border-bottom-color: rgba(15, 118, 110, 0.5);
  outline: none;
}

:deep(.course-description-tooltip) {
  max-width: min(320px, calc(100vw - 32px));
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.82);
  background: rgba(15, 23, 42, 0.88);
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.26);
  padding: 10px 12px;
}

:deep(.course-description-tooltip .description-tooltip-copy) {
  color: #f8fafc;
  line-height: 1.65;
  white-space: normal;
  word-break: break-word;
}

.meta-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 12px;
}

.mini-track {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 8px;
}

.track-node {
  height: 8px;
  border-radius: 999px;
  background: rgba(203, 213, 225, 0.6);
}

.track-node.complete,
.track-node.current {
  background: var(--status-accent);
}

.action-row {
  margin-top: 18px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

:deep(.workflow-dialog-overlay) {
  background: rgba(226, 232, 240, 0.34);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

:deep(.workflow-dialog) {
  width: min(560px, calc(100vw - 32px));
  margin: 0 !important;
  border-radius: 28px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.88);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.97), rgba(248, 250, 252, 0.95)),
    linear-gradient(135deg, rgba(15, 118, 110, 0.04), rgba(37, 99, 235, 0.04));
  box-shadow: 0 32px 90px rgba(15, 23, 42, 0.18);
}

:deep(.workflow-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 22px 24px 0;
}

:deep(.workflow-dialog .el-dialog__title) {
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.01em;
}

:deep(.workflow-dialog .el-dialog__body) {
  padding: 16px 24px 18px;
}

:deep(.workflow-dialog .el-dialog__footer) {
  padding: 0 24px 24px;
}

:deep(.workflow-dialog .el-dialog__headerbtn) {
  top: 18px;
  right: 18px;
}

:deep(.workflow-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: #64748b;
}

:deep(.workflow-dialog .el-dialog__headerbtn:hover .el-dialog__close) {
  color: #0f172a;
}

:deep(.workflow-dialog .el-button) {
  min-width: 96px;
  border-radius: 14px;
  font-weight: 600;
}

:deep(.workflow-dialog .el-textarea__inner) {
  min-height: 148px !important;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: none;
  color: #0f172a;
  padding: 14px 16px;
}

:deep(.workflow-dialog .el-textarea__inner:focus) {
  border-color: rgba(13, 148, 136, 0.3);
  box-shadow: 0 0 0 4px rgba(13, 148, 136, 0.12);
}

:deep(.workflow-dialog .el-input__count) {
  color: #64748b;
}

.dialog-hero {
  padding: 18px;
  border-radius: 22px;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.92), var(--dialog-soft)),
    linear-gradient(135deg, rgba(255, 255, 255, 0.88), rgba(255, 255, 255, 0.52));
  border: 1px solid rgba(255, 255, 255, 0.82);
}

.dialog-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: fit-content;
  padding: 7px 12px;
  border-radius: 999px;
  color: var(--dialog-accent);
  background: rgba(255, 255, 255, 0.78);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.dialog-body {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.dialog-course {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px 18px;
  border-radius: 16px;
  background: rgba(241, 245, 249, 0.88);
  border: 1px solid rgba(226, 232, 240, 0.92);
}

.dialog-course span,
.dialog-field-label,
.dialog-tip {
  color: #64748b;
  font-size: 13px;
}

.dialog-course strong {
  color: #0f172a;
  font-size: 16px;
  line-height: 1.5;
}

.dialog-field-label {
  font-weight: 600;
}

.dialog-tip {
  margin: 0;
  line-height: 1.6;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1240px) {
  .hero-shell,
  .page-grid {
    grid-template-columns: 1fr;
  }

  .workflow-track,
  .stats-grid,
  .board-tabs,
  .review-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .course-review-page {
    padding: 16px;
  }

  .hero-copy,
  .hero-focus,
  .workflow-shell,
  .board-shell,
  .policy-card {
    padding: 20px;
  }

  .hero-title,
  .section-heading h2,
  .board-header h2 {
    font-size: 24px;
  }

  .workflow-track,
  .stats-grid,
  .board-tabs,
  .review-grid,
  .focus-metrics,
  .meta-grid {
    grid-template-columns: 1fr;
  }

  .review-card {
    grid-template-columns: 1fr;
  }

  .review-cover {
    min-height: 180px;
  }

  :deep(.workflow-dialog) {
    width: min(100vw - 24px, 560px);
  }

  .board-toolbar,
  .section-heading,
  .board-header {
    flex-direction: column;
  }

  .toolbar-input,
  .toolbar-select {
    width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .loading-orb {
    animation: none;
  }

  .board-tab,
  .primary-action,
  .ghost-action,
  .action-primary,
  .action-warning,
  .action-danger,
  .description-trigger.is-truncated {
    transition: none;
  }
}
</style>