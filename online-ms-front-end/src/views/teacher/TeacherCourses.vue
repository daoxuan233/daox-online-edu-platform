<template>
  <div ref="pageRef" class="teacher-review-page">
    <section ref="heroRef" class="hero-shell">
      <div class="hero-copy">
        <p class="eyebrow">Teacher Review Console</p>
        <h1 class="hero-title">课程提审与状态工作台</h1>
        <p class="hero-description">
          教师端不再直接发课。课程必须先处于草稿，再提交审核，后续由管理员负责发布、下架与归档。
          当前页面直接对接后端课程状态机接口，状态变化会同步反映到通知中心。
        </p>
        <div class="hero-actions">
          <el-button type="primary" class="primary-action" @click="createCourse">
            <font-awesome-icon :icon="['fas', 'plus']" />
            新建课程
          </el-button>
          <el-button class="ghost-action" :loading="loading" @click="refreshCourses">
            <font-awesome-icon :icon="['fas', 'arrows-rotate']" />
            刷新状态
          </el-button>
          <el-button class="ghost-action" @click="openNotificationsCenter">
            <font-awesome-icon :icon="['fas', 'bell']" />
            审核通知
          </el-button>
        </div>
      </div>

      <div class="hero-focus glass-panel">
        <span class="focus-label">当前重点</span>
        <strong class="focus-title">{{ spotlight.title }}</strong>
        <p class="focus-description">{{ spotlight.description }}</p>
        <div class="focus-metrics">
          <div class="focus-metric">
            <span>课程总数</span>
            <strong>{{ boardStats.total }}</strong>
          </div>
          <div class="focus-metric">
            <span>待处理草稿</span>
            <strong>{{ boardStats.draft }}</strong>
          </div>
          <div class="focus-metric">
            <span>审核中</span>
            <strong>{{ boardStats.pending }}</strong>
          </div>
        </div>
      </div>
    </section>

    <section ref="workflowRef" class="workflow-shell glass-panel">
      <div class="section-heading">
        <div>
          <p class="section-eyebrow">Workflow</p>
          <h2>教师侧状态机一览</h2>
        </div>
        <span class="section-chip">仅允许通过提审接口变更发布状态</span>
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

    <section ref="toolbarRef" class="toolbar-shell glass-panel">
      <el-input
        v-model.trim="keyword"
        class="toolbar-input"
        clearable
        placeholder="搜索课程标题、简介或分类"
      >
        <template #prefix>
          <font-awesome-icon :icon="['fas', 'magnifying-glass']" />
        </template>
      </el-input>

      <el-select v-model="statusFilter" class="toolbar-select" placeholder="全部状态" clearable>
        <el-option label="全部状态" value="" />
        <el-option
          v-for="step in COURSE_WORKFLOW_STEPS"
          :key="step.key"
          :label="step.label"
          :value="step.key"
        />
      </el-select>

      <el-select v-model="sortBy" class="toolbar-select" placeholder="排序方式">
        <el-option label="状态优先" value="workflow" />
        <el-option label="最近创建" value="created_desc" />
        <el-option label="标题 A-Z" value="title_asc" />
      </el-select>
    </section>

    <div class="page-grid">
      <aside class="insight-column">
        <article class="insight-card glass-panel">
          <h3>提审前检查</h3>
          <ul class="insight-list">
            <li v-for="item in reviewChecklist" :key="item">{{ item }}</li>
          </ul>
        </article>

        <article class="insight-card glass-panel">
          <h3>状态说明</h3>
          <div
            v-for="statusKey in ['draft', 'pending', 'published', 'taken_down', 'archived']"
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

      <section class="board-shell glass-panel">
        <header class="board-header">
          <div>
            <p class="section-eyebrow">Courses</p>
            <h2>课程清单</h2>
            <p class="board-description">{{ boardDescription }}</p>
          </div>
          <span class="board-chip">共 {{ filteredCourses.length }} 门符合条件的课程</span>
        </header>

        <div v-if="loading" class="loading-shell">
          <div class="loading-orb"></div>
          <p>正在同步教师课程状态...</p>
        </div>

        <div v-else-if="filteredCourses.length === 0" class="empty-shell">
          <div class="empty-icon">
            <font-awesome-icon :icon="['fas', 'book-open']" />
          </div>
          <h3>暂无匹配课程</h3>
          <p>你可以先创建课程，或调整筛选条件重新查看。</p>
          <el-button type="primary" class="primary-action" @click="createCourse">创建第一门课程</el-button>
        </div>

        <div v-else ref="listRef" class="course-grid">
          <article
            v-for="course in paginatedCourses"
            :key="course.id"
            class="course-card"
            :style="{
              '--status-accent': getCourseStatusMeta(course.status).accent,
              '--status-soft': getCourseStatusMeta(course.status).soft
            }"
          >
            <div class="card-cover">
              <img v-if="course.cover" :src="course.cover" :alt="course.title" loading="lazy" decoding="async" />
              <div v-else class="cover-fallback">{{ buildInitials(course.title) }}</div>
              <span class="status-badge">{{ getCourseStatusMeta(course.status).label }}</span>
            </div>

            <div class="card-body">
              <div class="title-row">
                <div>
                  <span class="category-chip">{{ course.categoryName }}</span>
                  <h3>{{ course.title }}</h3>
                </div>
                <span class="price-chip">{{ course.priceText }}</span>
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
                  class="course-description description-trigger is-truncated"
                  tabindex="0"
                  @mouseenter="activateDescriptionPreview(course.id)"
                  @mouseleave="clearDescriptionPreview(course.id)"
                  @focus="activateDescriptionPreview(course.id)"
                  @blur="clearDescriptionPreview(course.id)"
                >
                  {{ getCourseDescriptionPreview(course.description) }}
                </p>
              </el-tooltip>
              <p v-else class="course-description">{{ normalizeCourseDescriptionText(course.description) }}</p>

              <div class="meta-row">
                <span>课程 ID {{ shortId(course.id) }}</span>
                <span>创建于 {{ formatDate(course.createdAt) }}</span>
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
                <template v-if="course.status === 'draft'">
                  <el-button
                    type="primary"
                    class="action-primary"
                    :loading="processingCourseId === course.id && processingAction === 'submit'"
                    @click="submitForReview(course)"
                  >
                    提交审核
                  </el-button>
                  <el-button class="action-secondary" @click="editCourse(course.id)">继续编辑</el-button>
                  <el-button
                    class="action-secondary danger"
                    :loading="processingCourseId === course.id && processingAction === 'archive'"
                    @click="archiveDraft(course)"
                  >
                    归档草稿
                  </el-button>
                </template>

                <template v-else-if="course.status === 'pending'">
                  <el-button class="action-pending" disabled>审核中</el-button>
                  <el-button class="action-secondary" @click="openNotificationsCenter">查看通知</el-button>
                </template>

                <template v-else-if="course.status === 'published'">
                  <el-button class="action-secondary" @click="editCourse(course.id)">查看课程</el-button>
                  <el-button class="action-secondary" @click="previewStatusGuide(course)">状态说明</el-button>
                </template>

                <template v-else>
                  <el-button class="action-secondary" @click="openNotificationsCenter">查看通知</el-button>
                  <el-button class="action-secondary" @click="previewStatusGuide(course)">状态说明</el-button>
                </template>
              </div>
            </div>
          </article>
        </div>

        <footer v-if="filteredCourses.length > pageSize" class="pagination-shell">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            layout="prev, pager, next"
            :page-size="pageSize"
            :total="filteredCourses.length"
          />
        </footer>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { gsap } from 'gsap'
import { archiveTeacherCourse, getMyCourseList, submitCourseReview } from '@/api/teacher/teacherAPI.js'
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
const toolbarRef = ref(null)
const listRef = ref(null)

const loading = ref(false)
const courses = ref([])
const keyword = ref('')
const statusFilter = ref('')
const sortBy = ref('workflow')
const currentPage = ref(1)
const pageSize = ref(6)
const processingCourseId = ref('')
const processingAction = ref('')
const hoveredDescriptionCourseId = ref('')

let enterContext = null
let listTween = null

const reviewChecklist = [
  '课程标题、简介和封面已经补齐，不再出现占位信息。',
  '分类、价格和公开属性已经确认，教师无需再通过发布接口改状态。',
  '目录与关键资料至少具备首版内容，便于管理员完成上线判断。',
  '若课程被驳回或下架，请优先到通知中心查看原因后再继续处理。'
]

/**
 * 将教师课程列表接口包装为 Promise，便于组合式页面管理异步状态。
 *
 * @returns {Promise<Array>} 课程列表
 */
const requestTeacherCourses = () => {
  return new Promise((resolve, reject) => {
    getMyCourseList(
      (data) => resolve(Array.isArray(data) ? data : []),
      (message, code) => {
        if (code === 403 || String(message || '').includes('没有课程')) {
          resolve([])
          return
        }
        reject(new Error(message || '获取课程列表失败'))
      }
    )
  })
}

/**
 * 解析日期值。
 *
 * @param {string|Date|null|undefined} value 日期值
 * @returns {Date|null} 日期对象
 */
const parseDateValue = (value) => {
  if (!value) {
    return null
  }
  const dateValue = value instanceof Date ? value : new Date(value)
  return Number.isNaN(dateValue.getTime()) ? null : dateValue
}

/**
 * 格式化课程价格。
 *
 * @param {string|number|null|undefined} value 价格值
 * @returns {string} 展示文本
 */
const formatPrice = (value) => {
  if (value === null || value === undefined || value === '') {
    return '未定价'
  }
  const numericValue = Number(value)
  if (Number.isNaN(numericValue)) {
    return `¥${value}`
  }
  return `¥${numericValue.toFixed(Number.isInteger(numericValue) ? 0 : 2)}`
}

/**
 * 构建教师课程展示模型。
 *
 * @param {Array} courseList 原始课程列表
 * @returns {Array} 前端展示数据
 */
const normalizeTeacherCourses = (courseList) => {
  return (Array.isArray(courseList) ? courseList : []).map((course) => ({
    id: course.courseId,
    title: course.courseTitle || '未命名课程',
    description: course.courseDescription || '暂无课程简介',
    cover: course.courseCover || '',
    categoryName: course.categoryName || '未分类',
    status: normalizeCourseStatus(course.courseStatus),
    priceText: formatPrice(course.coursePrice),
    createdAt: parseDateValue(course.createTime || course.createdAt)
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
 * @returns {string} 时间文本
 */
const formatDate = (value) => {
  if (!value) {
    return '时间未知'
  }
  return value.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit'
  })
}

/**
 * 生成课程封面占位首字。
 *
 * @param {string} title 课程标题
 * @returns {string} 首字文本
 */
const buildInitials = (title) => {
  return String(title || '课程').trim().slice(0, 2)
}

/**
 * 缩短课程 ID 展示，提升列表可读性。
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
  const countByStatus = (status) => courses.value.filter((course) => course.status === status).length
  return {
    total: courses.value.length,
    draft: countByStatus('draft'),
    pending: countByStatus('pending'),
    published: countByStatus('published'),
    takenDown: countByStatus('taken_down'),
    archived: countByStatus('archived')
  }
})

const summaryCards = computed(() => ([
  {
    key: 'draft',
    label: '草稿',
    value: boardStats.value.draft,
    description: '可继续编辑并提交审核',
    icon: 'pen-ruler',
    accent: getCourseStatusMeta('draft').accent,
    soft: getCourseStatusMeta('draft').soft
  },
  {
    key: 'pending',
    label: '待审核',
    value: boardStats.value.pending,
    description: '等待管理员处理',
    icon: 'hourglass-half',
    accent: getCourseStatusMeta('pending').accent,
    soft: getCourseStatusMeta('pending').soft
  },
  {
    key: 'published',
    label: '已发布',
    value: boardStats.value.published,
    description: '对学生开放学习',
    icon: 'tower-broadcast',
    accent: getCourseStatusMeta('published').accent,
    soft: getCourseStatusMeta('published').soft
  },
  {
    key: 'taken_down',
    label: '已下架',
    value: boardStats.value.takenDown,
    description: '等待管理员后续处理',
    icon: 'box-archive',
    accent: getCourseStatusMeta('taken_down').accent,
    soft: getCourseStatusMeta('taken_down').soft
  },
  {
    key: 'archived',
    label: '已归档',
    value: boardStats.value.archived,
    description: '终态课程存量',
    icon: 'folder-open',
    accent: getCourseStatusMeta('archived').accent,
    soft: getCourseStatusMeta('archived').soft
  }
]))

const spotlight = computed(() => {
  if (boardStats.value.draft > 0) {
    return {
      title: '先把草稿推进入审',
      description: `当前还有 ${boardStats.value.draft} 门草稿课程未提交审核，优先清理可缩短上线等待时间。`
    }
  }
  if (boardStats.value.pending > 0) {
    return {
      title: '留意审核结果通知',
      description: `当前有 ${boardStats.value.pending} 门课程正在审核中，结果会进入通知中心。`
    }
  }
  if (boardStats.value.takenDown > 0) {
    return {
      title: '关注下架课程反馈',
      description: '已下架课程需要根据管理员说明继续整改、等待重新上架或接受归档。'
    }
  }
  return {
    title: '课程发布面稳定',
    description: '当前没有积压草稿或待审课程，可以继续维护已发布课程的内容质量。'
  }
})

const filteredCourses = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase()
  let result = [...courses.value]

  if (normalizedKeyword) {
    result = result.filter((course) => {
      return [course.title, course.description, course.categoryName]
        .filter(Boolean)
        .some((field) => String(field).toLowerCase().includes(normalizedKeyword))
    })
  }

  if (statusFilter.value) {
    result = result.filter((course) => course.status === statusFilter.value)
  }

  result.sort((firstCourse, secondCourse) => {
    if (sortBy.value === 'created_desc') {
      return (secondCourse.createdAt?.getTime() || 0) - (firstCourse.createdAt?.getTime() || 0)
    }
    if (sortBy.value === 'title_asc') {
      return firstCourse.title.localeCompare(secondCourse.title, 'zh-CN')
    }
    const statusDelta = compareCourseStatus(firstCourse.status, secondCourse.status)
    if (statusDelta !== 0) {
      return statusDelta
    }
    return (secondCourse.createdAt?.getTime() || 0) - (firstCourse.createdAt?.getTime() || 0)
  })

  return result
})

const paginatedCourses = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  return filteredCourses.value.slice(startIndex, startIndex + pageSize.value)
})

const boardDescription = computed(() => {
  if (statusFilter.value) {
    return `当前已锁定 ${getCourseStatusMeta(statusFilter.value).label} 课程，可结合通知结果继续处理。`
  }
  if (keyword.value) {
    return `当前根据关键词“${keyword.value}”筛选课程，状态流转动作会直接回写后端。`
  }
  return '聚焦草稿、待审、已发布、已下架与已归档五态流转，所有状态都来自后端接口返回。'
})

/**
 * 加载教师课程状态数据。
 *
 * @returns {Promise<void>} 异步结果
 */
const loadTeacherCourses = async () => {
  loading.value = true
  try {
    const courseList = await requestTeacherCourses()
    courses.value = normalizeTeacherCourses(courseList)
  } catch (error) {
    console.error('加载教师课程列表失败:', error)
    ElMessage.error(error?.message || '加载课程列表失败')
    courses.value = []
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
      gsap.set([heroRef.value, workflowRef.value, toolbarRef.value, statCards], { opacity: 1, y: 0 })
      return
    }

    const timeline = gsap.timeline({ defaults: { ease: 'power3.out' } })
    timeline
      .fromTo(heroRef.value, { opacity: 0, y: 28 }, { opacity: 1, y: 0, duration: 0.56 })
      .fromTo(workflowRef.value, { opacity: 0, y: 24 }, { opacity: 1, y: 0, duration: 0.46 }, '-=0.28')
      .fromTo(statCards, { opacity: 0, y: 18 }, { opacity: 1, y: 0, duration: 0.42, stagger: 0.08 }, '-=0.2')
      .fromTo(toolbarRef.value, { opacity: 0, y: 18 }, { opacity: 1, y: 0, duration: 0.38 }, '-=0.14')
  }, pageRef.value)
}

/**
 * 播放课程卡片动画。
 */
const animateCourseCards = () => {
  if (prefersReducedMotion()) {
    return
  }
  listTween?.kill()
  const cards = listRef.value?.querySelectorAll('.course-card') || []
  if (!cards.length) {
    return
  }
  listTween = gsap.fromTo(
    cards,
    { opacity: 0, y: 18, scale: 0.985 },
    { opacity: 1, y: 0, scale: 1, duration: 0.36, ease: 'power2.out', stagger: 0.06 }
  )
}

/**
 * 刷新课程状态。
 *
 * @returns {Promise<void>} 异步结果
 */
const refreshCourses = async () => {
  await loadTeacherCourses()
  await nextTick()
  animateCourseCards()
  ElMessage.success('课程状态已刷新')
}

const createCourse = () => {
  router.push({ name: 'NewCourse' })
}

const editCourse = (courseId) => {
  router.push({ name: 'CourseEdit', params: { id: courseId } })
}

const openNotificationsCenter = () => {
  router.push({ name: 'TeacherNotifications' })
}

/**
 * 提交课程审核。
 *
 * @param {object} course 课程对象
 * @returns {Promise<void>} 异步结果
 */
const submitForReview = async (course) => {
  try {
    await ElMessageBox.confirm(
      `确定将课程“${course.title}”提交审核吗？提交后教师端将不能再直接发布该课程。`,
      '提交审核',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    processingCourseId.value = course.id
    processingAction.value = 'submit'
    await submitCourseReview(course.id)
    ElMessage.success('课程已提交审核')
    await loadTeacherCourses()
    await nextTick()
    animateCourseCards()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '提交审核失败')
    }
  } finally {
    processingCourseId.value = ''
    processingAction.value = ''
  }
}

/**
 * 归档草稿课程。
 *
 * @param {object} course 课程对象
 * @returns {Promise<void>} 异步结果
 */
const archiveDraft = async (course) => {
  try {
    await ElMessageBox.confirm(
      `确定归档草稿课程“${course.title}”吗？归档后该课程会进入终态，不再继续提审。`,
      '归档草稿',
      {
        confirmButtonText: '确认归档',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    processingCourseId.value = course.id
    processingAction.value = 'archive'
    await archiveTeacherCourse(course.id)
    ElMessage.success('草稿课程已归档')
    await loadTeacherCourses()
    await nextTick()
    animateCourseCards()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '归档草稿失败')
    }
  } finally {
    processingCourseId.value = ''
    processingAction.value = ''
  }
}

/**
 * 展示课程状态说明，帮助教师理解当前阶段的后续动作。
 *
 * @param {object} course 课程对象
 */
const previewStatusGuide = (course) => {
  const statusMeta = getCourseStatusMeta(course.status)
  const nextActionMap = {
    pending: '管理员会在课程审核台中执行通过或驳回，你可以到通知中心查看结果。',
    published: '课程已上线，如需内容维护可继续编辑课程信息，但不能直接变更发布状态。',
    taken_down: '课程已下架，请先查看管理员说明，等待重新上架或归档决定。',
    archived: '课程已归档，当前不会再进入发布流转。'
  }

  ElMessageBox.alert(
    `${statusMeta.description}\n\n${nextActionMap[course.status] || '当前状态无需额外处理。'}`,
    `${course.title} · ${statusMeta.label}`,
    {
      confirmButtonText: '知道了'
    }
  )
}

watch(
  () => [keyword.value, statusFilter.value, sortBy.value],
  () => {
    currentPage.value = 1
  }
)

watch(
  () => [keyword.value, statusFilter.value, sortBy.value, currentPage.value, pageSize.value, courses.value.length],
  async () => {
    await nextTick()
    animateCourseCards()
  }
)

onMounted(async () => {
  await loadTeacherCourses()
  await nextTick()
  playEnterAnimation()
  animateCourseCards()
})

onUnmounted(() => {
  enterContext?.revert()
  listTween?.kill()
})
</script>

<style scoped>
.teacher-review-page {
  min-height: 100%;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background:
    radial-gradient(circle at top left, rgba(14, 165, 233, 0.1), transparent 30%),
    radial-gradient(circle at 88% 8%, rgba(217, 119, 6, 0.1), transparent 24%),
    linear-gradient(180deg, #fcfaf6 0%, #eef5f2 100%);
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
.board-header h2,
.empty-shell h3,
.title-row h3 {
  margin: 0;
  color: #0f172a;
}

.hero-title,
.section-heading h2,
.board-header h2 {
  font-size: 30px;
  line-height: 1.12;
}

.hero-description,
.focus-description,
.board-description,
.course-description,
.status-description,
.status-brief p,
.insight-list li {
  color: #526072;
  line-height: 1.7;
}

.hero-description,
.focus-description,
.board-description {
  margin: 14px 0 0;
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
.action-secondary,
.action-pending {
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
.action-secondary,
.action-pending {
  border: 1px solid rgba(148, 163, 184, 0.22);
  background: rgba(255, 255, 255, 0.86);
  color: #1e293b;
}

.action-secondary.danger:hover {
  color: #b45309;
  border-color: rgba(180, 83, 9, 0.22);
}

.hero-focus {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.9), rgba(245, 242, 235, 0.94)),
    linear-gradient(135deg, rgba(15, 118, 110, 0.08), rgba(217, 119, 6, 0.06));
}

.focus-label,
.board-chip,
.section-chip,
.status-pill,
.status-badge,
.category-chip,
.price-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: fit-content;
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.focus-label {
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
.meta-row span,
.price-chip,
.category-chip {
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
.toolbar-shell,
.board-shell,
.insight-card {
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
  background: rgba(240, 249, 255, 0.88);
  color: #1d4ed8;
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
.stat-copy small,
.category-chip,
.price-chip,
.course-description,
.status-description,
.status-brief p {
  margin-top: 8px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  padding: 20px;
  display: flex;
  gap: 16px;
  align-items: center;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.96), var(--card-soft));
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

.toolbar-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) 220px 220px;
  gap: 12px;
}

.toolbar-input :deep(.el-input__wrapper),
.toolbar-select :deep(.el-select__wrapper) {
  min-height: 42px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: none;
}

.page-grid {
  display: grid;
  grid-template-columns: minmax(260px, 320px) minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.insight-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.insight-card h3 {
  margin: 0;
  font-size: 18px;
  color: #0f172a;
}

.insight-list {
  margin: 16px 0 0;
  padding-left: 18px;
}

.status-brief + .status-brief {
  margin-top: 14px;
}

.status-pill,
.status-badge {
  color: var(--pill-accent, var(--status-accent));
  background: var(--pill-soft, var(--status-soft));
}

.board-chip {
  background: rgba(15, 118, 110, 0.1);
  color: #0f766e;
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

.course-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.course-card {
  display: grid;
  grid-template-columns: minmax(188px, 36%) minmax(0, 1fr);
  align-items: start;
  gap: 18px;
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.96), var(--status-soft));
  border: 1px solid rgba(255, 255, 255, 0.84);
  box-shadow: 0 18px 34px rgba(15, 23, 42, 0.06);
}

.card-cover {
  position: relative;
  align-self: start;
  isolation: isolate;
  border-radius: 18px;
  overflow: hidden;
  aspect-ratio: 4 / 5;
  min-height: 0;
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.12), rgba(148, 163, 184, 0.16));
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.54),
    0 20px 36px rgba(15, 23, 42, 0.12);
}

.card-cover::before {
  content: '';
  position: absolute;
  inset: 10px;
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.22), transparent 52%);
  z-index: 0;
  pointer-events: none;
}

.card-cover::after {
  content: '';
  position: absolute;
  inset: auto 12% 10px 12%;
  height: 24%;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(15, 118, 110, 0.18), transparent 72%);
  filter: blur(18px);
  z-index: 0;
  pointer-events: none;
}

.card-cover img,
.cover-fallback {
  position: relative;
  z-index: 1;
  display: block;
  width: 100%;
  height: 100%;
}

.card-cover img {
  object-fit: cover;
  object-position: center;
}

.cover-fallback {
  display: grid;
  place-items: center;
  color: var(--status-accent);
  font-size: 24px;
  font-weight: 700;
  background:
    radial-gradient(circle at 18% 18%, rgba(255, 255, 255, 0.82), transparent 32%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.82), var(--status-soft));
}

.status-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 2;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.12);
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.course-description {
  min-height: 48px;
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

.meta-row {
  margin-top: 14px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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

.action-pending {
  color: #2563eb;
  border-color: rgba(37, 99, 235, 0.2);
}

.pagination-shell {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1260px) {
  .hero-shell,
  .page-grid,
  .toolbar-shell,
  .stats-grid,
  .course-grid,
  .workflow-track,
  .focus-metrics {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .teacher-review-page {
    padding: 16px;
  }

  .hero-copy,
  .hero-focus,
  .workflow-shell,
  .toolbar-shell,
  .board-shell,
  .insight-card {
    padding: 20px;
  }

  .hero-title,
  .section-heading h2,
  .board-header h2 {
    font-size: 24px;
  }

  .course-card {
    grid-template-columns: 1fr;
  }

  .card-cover {
    aspect-ratio: 16 / 9;
    min-height: 0;
  }

  .section-heading,
  .board-header,
  .title-row {
    flex-direction: column;
  }
}

@media (prefers-reduced-motion: reduce) {
  .loading-orb {
    animation: none;
  }

  .primary-action,
  .ghost-action,
  .action-primary,
  .action-secondary,
  .action-pending,
  .description-trigger.is-truncated {
    transition: none;
  }
}
</style>