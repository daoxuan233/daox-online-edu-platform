/**
 * 课程状态到展示元数据的映射。
 * 用于教师端与管理员端统一渲染颜色、标签和说明文案。
 */
export const COURSE_STATUS_META = {
  draft: {
    key: 'draft',
    label: '草稿',
    accent: '#b86a27',
    soft: 'rgba(248, 223, 184, 0.36)',
    description: '课程仍在教师工作区，可继续完善内容后再提交审核。'
  },
  pending: {
    key: 'pending',
    label: '待审核',
    accent: '#2563eb',
    soft: 'rgba(191, 219, 254, 0.38)',
    description: '课程已进入管理员审核队列，教师会通过通知中心接收处理结果。'
  },
  published: {
    key: 'published',
    label: '已发布',
    accent: '#1f8f5f',
    soft: 'rgba(187, 247, 208, 0.38)',
    description: '课程对学生开放学习，管理员仍可根据治理要求下架。'
  },
  taken_down: {
    key: 'taken_down',
    label: '已下架',
    accent: '#b45309',
    soft: 'rgba(254, 215, 170, 0.38)',
    description: '课程已被管理员下架，后续只能重新上架或归档。'
  },
  archived: {
    key: 'archived',
    label: '已归档',
    accent: '#64748b',
    soft: 'rgba(226, 232, 240, 0.52)',
    description: '课程已进入终态，不再继续参与发布流转。'
  }
}

/**
 * 课程状态机步骤说明。
 */
export const COURSE_WORKFLOW_STEPS = [
  {
    key: 'draft',
    label: '草稿',
    description: '教师编辑课程基础信息、目录和资料。'
  },
  {
    key: 'pending',
    label: '待审核',
    description: '教师主动提交审核，管理员进入审核队列处理。'
  },
  {
    key: 'published',
    label: '已发布',
    description: '审核通过后课程正式上架，对学生可见。'
  },
  {
    key: 'taken_down',
    label: '已下架',
    description: '管理员可对已发布课程执行下架治理动作。'
  },
  {
    key: 'archived',
    label: '已归档',
    description: '归档后课程结束生命周期，不再继续流转。'
  }
]

/**
 * 课程状态的展示顺序。
 */
export const COURSE_STATUS_PRIORITY = {
  draft: 0,
  pending: 1,
  published: 2,
  taken_down: 3,
  archived: 4
}

/**
 * 课程简介列表预览长度。
 */
export const COURSE_DESCRIPTION_PREVIEW_LIMIT = 5

const STATUS_ALIAS_MAP = {
  draft: 'draft',
  pending: 'pending',
  published: 'published',
  taken_down: 'taken_down',
  archived: 'archived',
  DRAFT: 'draft',
  PENDING: 'pending',
  PUBLISHED: 'published',
  TAKEN_DOWN: 'taken_down',
  ARCHIVED: 'archived',
  0: 'draft',
  1: 'published',
  '-1': 'archived'
}

/**
 * 归一化课程状态，兼容旧数值状态和大小写差异。
 *
 * @param {string|number|null|undefined} status 原始状态
 * @returns {string} 归一化状态
 */
export const normalizeCourseStatus = (status) => {
  const rawValue = String(status ?? '').trim()
  if (!rawValue) {
    return 'draft'
  }
  const exactMatch = STATUS_ALIAS_MAP[rawValue]
  if (exactMatch) {
    return exactMatch
  }
  const lowerCaseValue = rawValue.toLowerCase()
  return STATUS_ALIAS_MAP[lowerCaseValue] || 'draft'
}

/**
 * 获取课程状态的展示元数据。
 *
 * @param {string|number|null|undefined} status 原始状态
 * @returns {{key: string, label: string, accent: string, soft: string, description: string}} 状态元数据
 */
export const getCourseStatusMeta = (status) => {
  const normalizedStatus = normalizeCourseStatus(status)
  return COURSE_STATUS_META[normalizedStatus] || COURSE_STATUS_META.draft
}

/**
 * 获取课程所处的流程步骤索引。
 *
 * @param {string|number|null|undefined} status 原始状态
 * @returns {number} 流程索引
 */
export const getWorkflowStepIndex = (status) => {
  const normalizedStatus = normalizeCourseStatus(status)
  const index = COURSE_WORKFLOW_STEPS.findIndex((step) => step.key === normalizedStatus)
  return index >= 0 ? index : 0
}

/**
 * 比较两个课程状态的优先级。
 *
 * @param {string} firstStatus 第一个状态
 * @param {string} secondStatus 第二个状态
 * @returns {number} 比较结果
 */
export const compareCourseStatus = (firstStatus, secondStatus) => {
  const firstPriority = COURSE_STATUS_PRIORITY[normalizeCourseStatus(firstStatus)] ?? 0
  const secondPriority = COURSE_STATUS_PRIORITY[normalizeCourseStatus(secondStatus)] ?? 0
  return firstPriority - secondPriority
}

/**
 * 规范化课程简介，避免换行和连续空白影响卡片展示。
 *
 * @param {string|null|undefined} description 原始简介
 * @returns {string} 规范化后的简介文本
 */
export const normalizeCourseDescriptionText = (description) => {
  const safeText = String(description ?? '').replace(/\s+/g, ' ').trim()
  return safeText || '暂无课程简介'
}

/**
 * 获取课程简介预览文本。
 *
 * @param {string|null|undefined} description 原始简介
 * @param {number} [limit=COURSE_DESCRIPTION_PREVIEW_LIMIT] 预览长度
 * @returns {string} 预览文本
 */
export const getCourseDescriptionPreview = (
  description,
  limit = COURSE_DESCRIPTION_PREVIEW_LIMIT
) => {
  const normalizedText = normalizeCourseDescriptionText(description)
  const characters = Array.from(normalizedText)
  if (characters.length <= limit) {
    return normalizedText
  }
  return `${characters.slice(0, limit).join('')}...`
}

/**
 * 判断课程简介是否超出预览长度。
 *
 * @param {string|null|undefined} description 原始简介
 * @param {number} [limit=COURSE_DESCRIPTION_PREVIEW_LIMIT] 预览长度
 * @returns {boolean} 是否需要悬停查看完整简介
 */
export const hasCourseDescriptionOverflow = (
  description,
  limit = COURSE_DESCRIPTION_PREVIEW_LIMIT
) => {
  const characters = Array.from(normalizeCourseDescriptionText(description))
  return characters.length > limit
}