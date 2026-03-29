<template>
  <div class="admin-dashboard-container ai-governance-container bg-neumorphism">
    <div class="page-header frosted-glass modern-header" ref="headerRef">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title text-primary">
            <div class="icon-wrapper primary-glow">
              <font-awesome-icon :icon="['fas', 'microchip']" />
            </div>
            AI 治理与监控中心
          </h1>
          <p class="page-subtitle text-secondary">
            面向管理员展示 AI 调用概览、待审队列、运行时策略与配额治理。
          </p>
          <div class="header-meta text-secondary">
            <span>待审记录 {{ overview.pendingReviews || 0 }} 条</span>
            <span>策略更新时间 {{ runtimeStrategy.updatedAt ? formatDateTime(runtimeStrategy.updatedAt) : '未配置' }}</span>
          </div>
        </div>
        <div class="header-actions">
          <el-button
            type="primary"
            class="action-btn custom-button gradient-primary"
            @click="refreshData"
            :loading="refreshing"
          >
            <font-awesome-icon :icon="['fas', 'sync-alt']" :class="{ 'fa-spin': refreshing }" />
            数据态势同步
          </el-button>
        </div>
      </div>
    </div>

    <el-alert
      v-if="loadError"
      class="mt-lg frosted-glass"
      type="error"
      :closable="false"
      show-icon
      :title="loadError"
    />

    <div class="bento-grid-container mt-lg" ref="contentGridRef">
      <div
        v-for="(stat, index) in stats"
        :key="`stat-${index}`"
        class="bento-stat-card neumorphism-raised hover-float"
      >
        <div class="card-header">
          <div :class="['card-icon-container neumorphism-inset flex-center', stat.colorClass]">
            <font-awesome-icon :icon="['fas', stat.icon]" class="icon-glow" />
          </div>
          <div class="card-title text-main">{{ stat.title }}</div>
        </div>
        <div class="card-content">
          <div class="stat-item">
            <span :class="['stat-number', stat.textClass]">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
          <div :class="['growth-indicator', stat.indicatorClass]">
            <font-awesome-icon :icon="['fas', stat.metaIcon]" />
            <span>{{ stat.meta }}</span>
          </div>
        </div>
      </div>

      <div class="bento-main-card neumorphism-raised slide-up-element">
        <div class="section-header align-start">
          <div>
            <h3 class="section-title">
              <div class="title-icon text-main"><font-awesome-icon :icon="['fas', 'project-diagram']" /></div>
              智能降级与重试链路
            </h3>
            <p class="section-subtitle text-secondary">该区域直接对接后端运行时策略，修改后会立即影响后续 AI 调用治理流程。</p>
          </div>
          <div class="section-actions">
            <el-tag
              size="small"
              :type="runtimeStrategy.reviewEnabled ? 'success' : 'warning'"
              effect="dark"
              class="pulse-tag border-none neumorphism-raised font-bold" style="color: #517B4D;"
            >
              {{ runtimeStrategy.reviewEnabled ? '内容审查已启用' : '内容审查已关闭' }}
            </el-tag>
            <el-button
              size="small"
              type="primary"
              class="custom-button save-btn"
              :loading="savingStrategy"
              @click="saveRuntimeConfig"
            >
              保存运行策略
            </el-button>
          </div>
        </div>

        <div class="fallback-container mt-md">
          <div class="fallback-chain frosted-glass-light">
            <div v-for="(model, index) in fallbackChain" :key="model.platform" class="chain-node-wrapper">
              <div :class="['chain-node neumorphism-raised', { 'active-node': index === 0 }]">
                <div class="action-icon" :class="index === 0 ? 'text-primary' : 'text-secondary'">
                  <font-awesome-icon :icon="index === 0 ? ['fas', 'bolt'] : ['fas', 'link']" />
                </div>
                <div class="node-info">
                  <span class="provider">{{ model.provider }}</span>
                  <strong class="model-name">{{ model.name }}</strong>
                </div>
                <div class="node-status" v-if="index === 0">
                  <span class="pulse-dot"></span> 主力
                </div>
                <div class="chain-actions">
                  <el-button
                    size="small"
                    circle
                    text
                    class="custom-text-btn"
                    :disabled="index === 0"
                    @click="moveFallbackPlatform(index, -1)"
                  >
                    <font-awesome-icon :icon="['fas', 'arrow-left']" />
                  </el-button>
                  <el-button
                    size="small"
                    circle
                    text
                    class="custom-text-btn"
                    :disabled="index === fallbackChain.length - 1"
                    @click="moveFallbackPlatform(index, 1)"
                  >
                    <font-awesome-icon :icon="['fas', 'arrow-right']" />
                  </el-button>
                </div>
              </div>
              <div class="node-arrow" v-if="index < fallbackChain.length - 1">
                <font-awesome-icon :icon="['fas', 'angle-double-right']" class="text-secondary" />
              </div>
            </div>
          </div>

          <div class="retry-config-panel neumorphism-inset mt-lg p-md">
            <h4 class="text-sm font-bold mb-md text-main">
              <font-awesome-icon :icon="['fas', 'sliders-h']" class="mr-sm" />链路容灾配置
            </h4>
            <el-form label-position="top" size="small" class="tech-form">
              <el-row :gutter="20">
                <el-col :xl="8" :lg="12" :md="12" :sm="24">
                  <el-form-item label="内容审查开关">
                    <div class="inline-control-row">
                      <el-switch
                        v-model="runtimeStrategy.reviewEnabled"
                        style="--el-switch-on-color: #517B4D; --el-switch-off-color: #d1d1d4;"
                      />
                      <span class="text-secondary text-sm">关闭后将跳过关键词审查与待审队列写入。</span>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :xl="8" :lg="12" :md="12" :sm="24">
                  <el-form-item label="最大重试次数">
                    <el-input-number
                      v-model="runtimeStrategy.maxAttempts"
                      :min="1"
                      :max="5"
                      controls-position="right"
                      class="w-full custom-input"
                    />
                  </el-form-item>
                </el-col>
                <el-col :xl="8" :lg="24" :md="24" :sm="24">
                  <el-form-item label="降级平台顺序">
                    <el-select
                      v-model="runtimeStrategy.fallbackPlatforms"
                      multiple
                      collapse-tags
                      collapse-tags-tooltip
                      class="w-full"
                      placeholder="请选择平台，按选中顺序生效"
                    >
                      <el-option
                        v-for="platform in platformOptions"
                        :key="platform.value"
                        :label="platform.label"
                        :value="platform.value"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>

          <div class="platform-metrics-grid mt-lg" v-if="platformMetrics.length">
            <div
              v-for="metric in platformMetrics"
              :key="metric.platform"
              class="platform-metric-card neumorphism-inset"
            >
              <div class="platform-metric-head">
                <span class="platform-name text-main">{{ platformLabel(metric.platform) }}</span>
                <span class="text-secondary text-xs">{{ metric.totalCalls }} 次</span>
              </div>
              <strong class="platform-metric-value text-primary">{{ metric.successCalls }}</strong>
              <span class="platform-metric-meta text-secondary">成功 {{ metric.successCalls }} · 降级 {{ metric.degradedCalls }}</span>
            </div>
          </div>

          <div class="keywords-grid mt-lg">
            <div class="keyword-panel neumorphism-inset p-md">
              <div class="keyword-panel-header">
                <h4 class="text-sm font-bold text-main">阻断关键词</h4>
                <span class="text-secondary text-xs">命中后直接进入拦截态</span>
              </div>
              <el-input
                v-model="blockedKeywordsText"
                type="textarea"
                :rows="6"
                resize="none"
                placeholder="支持换行、英文逗号、中文逗号分隔"
              />
            </div>
            <div class="keyword-panel neumorphism-inset p-md">
              <div class="keyword-panel-header">
                <h4 class="text-sm font-bold text-main">人工复核关键词</h4>
                <span class="text-secondary text-xs">命中后进入管理员待审队列</span>
              </div>
              <el-input
                v-model="reviewKeywordsText"
                type="textarea"
                :rows="6"
                resize="none"
                placeholder="支持换行、英文逗号、中文逗号分隔"
              />
            </div>
          </div>
        </div>
      </div>

      <div class="bento-right-card neumorphism-raised slide-up-element">
        <div class="section-header align-start">
          <div>
            <h3 class="section-title">
              <div class="title-icon text-danger"><font-awesome-icon :icon="['fas', 'shield-virus']" /></div>
              内容审查待审队列
            </h3>
            <p class="section-subtitle text-secondary">仅展示最近待审记录，审查结论会回写后端 MongoDB 治理记录。</p>
          </div>
          <span class="pending-count pulse-badge neumorphism-inset text-danger">{{ overview.pendingReviews || pendingRecords.length }}</span>
        </div>

        <div class="review-queue-list mt-md">
          <div v-if="refreshing && !pendingRecords.length" class="empty-state text-secondary py-xl">
            正在同步待审队列...
          </div>

          <div
            v-for="item in pendingRecords"
            :key="item.id"
            :data-record-id="item.id"
            class="pending-item neumorphism-inset mb-md p-md tech-border-left-red hover-lift"
          >
            <div class="w-full">
              <div class="flex justify-between items-center mb-sm gap-sm wrap-row">
                <div class="font-bold text-md flex items-center text-main">
                  <font-awesome-icon :icon="['fas', 'user-ninja']" class="mr-xs text-secondary" />
                  {{ item.user }}
                </div>
                <div class="text-xs text-secondary">
                  <font-awesome-icon :icon="['fas', 'clock']" class="mr-xs" />{{ formatDateTime(item.createdAt) }}
                </div>
              </div>

              <div class="mb-sm wrap-row gap-sm">
                <el-tag size="small" type="danger" effect="light" class="mr-sm frosted-danger neumorphism-raised border-none text-danger">
                  <font-awesome-icon :icon="['fas', 'exclamation-triangle']" class="mr-xs" />
                  {{ item.reviewReason || '命中治理规则，待人工复核' }}
                </el-tag>
                <el-tag size="small" type="info" effect="plain" class="frosted-info border-none text-secondary">
                  {{ item.model || platformLabel(item.finalPlatform) }}
                </el-tag>
                <el-tag size="small" :type="item.callStatus === 'REJECTED' ? 'warning' : 'primary'" effect="plain">
                  {{ item.callStatus === 'REJECTED' ? '已拦截' : '等待复核' }}
                </el-tag>
              </div>

              <div class="review-content neumorphism-inset p-sm rounded-md mb-sm text-sm text-content record-content">
                {{ item.questionContent || '当前记录无提问内容' }}
              </div>
              <div class="queue-meta text-secondary text-xs mb-md">
                <span>平台：{{ platformLabel(item.finalPlatform || item.requestedPlatform) }}</span>
                <span>耗时：{{ item.latencyMs || 0 }} ms</span>
                <span>用户角色：{{ item.userRole || '未知' }}</span>
              </div>

              <div class="flex justify-end gap-sm mt-sm">
                <el-button
                  size="small"
                  type="danger"
                  class="neumorphism-raised text-danger border-none custom-button-small"
                  :loading="reviewingRecordId === item.id && reviewingDecision === 'REJECTED'"
                  @click="handleReview(item.id, 'REJECTED')"
                >
                  <font-awesome-icon :icon="['fas', 'ban']" class="mr-xs" />确认拦截
                </el-button>
                <el-button
                  size="small"
                  type="success"
                  class="neumorphism-raised text-success border-none custom-button-small"
                  :loading="reviewingRecordId === item.id && reviewingDecision === 'APPROVED'"
                  @click="handleReview(item.id, 'APPROVED')"
                >
                  <font-awesome-icon :icon="['fas', 'check-circle']" class="mr-xs" />标记误判
                </el-button>
              </div>
            </div>
          </div>

          <div v-if="!refreshing && !pendingRecords.length" class="flex-center flex-col h-full text-secondary py-xl empty-panel">
            <font-awesome-icon :icon="['fas', 'check-double']" class="text-4xl mb-md text-success opacity-50 drop-shadow-success" />
            <p>当前队列清空，无待审查条目</p>
          </div>
        </div>
      </div>

      <div class="bento-bottom-card frosted-glass slide-up-element">
        <div class="section-header align-start">
          <div>
            <h3 class="section-title">
              <div class="title-icon text-main"><font-awesome-icon :icon="['fas', 'user-shield']" /></div>
              接口配额与访问策略
            </h3>
            <p class="section-subtitle text-secondary">后端会自动回退到系统默认策略，管理员可在此基础上按角色覆盖并持久化。</p>
          </div>
          <el-button text class="view-all-btn custom-text-btn text-primary" @click="refreshPoliciesOnly">
            刷新策略
          </el-button>
        </div>

        <div class="policy-table-container mt-md">
          <el-table
            :data="policies"
            :row-key="getPolicyRowKey"
            style="width: 100%"
            :row-class-name="'neumorphism-table-row'"
            :header-cell-style="{ background: 'transparent', color: '#606266', fontWeight: 'bold' }"
          >
            <el-table-column label="策略角色" min-width="220">
              <template #default="scope">
                <div class="flex items-center text-main">
                  <div class="icon-circle mr-sm neumorphism-inset flex-center">
                    <font-awesome-icon :icon="['fas', scope.row.icon]" class="text-primary" />
                  </div>
                  <div class="policy-title-group">
                    <span class="font-bold">{{ scope.row.displayName }}</span>
                    <span class="policy-scope-desc text-secondary text-xs">{{ scope.row.scopeDescription }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="hourlyLimit" label="每小时限额" min-width="160" align="center">
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.hourlyLimit"
                  :min="0"
                  :max="99999"
                  controls-position="right"
                  class="policy-number-input"
                />
              </template>
            </el-table-column>
            <el-table-column prop="dailyLimit" label="每日总额度" min-width="160" align="center">
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.dailyLimit"
                  :min="0"
                  :max="999999"
                  controls-position="right"
                  class="policy-number-input"
                />
              </template>
            </el-table-column>
            <el-table-column label="启用状态" min-width="120" align="center">
              <template #default="scope">
                <el-switch
                  v-model="scope.row.enabled"
                  style="--el-switch-on-color: #517B4D; --el-switch-off-color: #d1d1d4;"
                />
              </template>
            </el-table-column>
            <el-table-column label="备注说明" min-width="240">
              <template #default="scope">
                <el-input
                  v-model="scope.row.remark"
                  maxlength="100"
                  show-word-limit
                  placeholder="请输入策略说明"
                  class="policy-remark-input"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="140" align="center">
              <template #default="scope">
                <el-button
                  size="small"
                  type="primary"
                  class="custom-button save-btn"
                  :loading="savingPolicyKey === getPolicyRowKey(scope.row)"
                  @click="savePolicy(scope.row)"
                >
                  保存
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { gsap } from 'gsap'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils/dateUtils'
import {
  getAiGovernanceOverview,
  getAiGovernanceRecords,
  getAiQuotaPolicies,
  getAiRuntimeStrategy,
  reviewAiGovernanceRecord,
  saveAiQuotaPolicy,
  saveAiRuntimeStrategy
} from '@/api/admin/aiGovernance.js'

/**
 * 平台基础元数据，用于前端展示治理链路和平台标签。
 */
const platformOptions = [
  { value: 'openai', label: 'OpenAI', model: 'gpt-4o' },
  { value: 'deepseek', label: 'DeepSeek', model: 'deepseek-chat' },
  { value: 'qwen', label: '通义千问', model: 'qwen-max' }
]

const headerRef = ref(null)
const contentGridRef = ref(null)
const refreshing = ref(false)
const savingStrategy = ref(false)
const savingPolicyKey = ref('')
const reviewingRecordId = ref('')
const reviewingDecision = ref('')
const loadError = ref('')

const overview = ref(createDefaultOverview())
const runtimeStrategy = ref(createDefaultRuntimeStrategy())
const policies = ref([])
const pendingRecords = ref([])
const blockedKeywordsText = ref('')
const reviewKeywordsText = ref('')

let enterAnim = null

/**
 * 创建默认概览数据，避免首屏渲染空引用。
 *
 * @returns {Record<string, any>} 默认概览对象
 */
function createDefaultOverview() {
  return {
    totalCallsToday: 0,
    successCallsToday: 0,
    failedCallsToday: 0,
    rejectedCallsToday: 0,
    degradedCallsToday: 0,
    pendingReviews: 0,
    platformMetrics: []
  }
}

/**
 * 创建默认运行时策略。
 *
 * @returns {Record<string, any>} 默认运行时策略
 */
function createDefaultRuntimeStrategy() {
  return {
    reviewEnabled: true,
    blockedKeywords: [],
    reviewKeywords: [],
    maxAttempts: 3,
    fallbackPlatforms: platformOptions.map((item) => item.value),
    updatedAt: null,
    updatedBy: null
  }
}

/**
 * 将关键字数组转换为适合文本域编辑的多行字符串。
 *
 * @param {string[]} keywords 关键词数组
 * @returns {string} 文本域内容
 */
function keywordsToText(keywords = []) {
  return keywords.join('\n')
}

/**
 * 将文本域中的关键词输入解析为数组。
 *
 * @param {string} text 原始文本
 * @returns {string[]} 清洗后的关键词数组
 */
function parseKeywords(text) {
  return (text || '')
    .split(/[\n,，;；]+/)
    .map((item) => item.trim())
    .filter(Boolean)
    .filter((item, index, list) => list.indexOf(item) === index)
}

/**
 * 将后端运行时策略转换为页面可编辑状态。
 *
 * @param {Record<string, any>} strategy 后端返回策略
 * @returns {Record<string, any>} 规范化后的策略对象
 */
function normalizeRuntimeStrategy(strategy = {}) {
  const nextStrategy = {
    ...createDefaultRuntimeStrategy(),
    ...strategy,
    reviewEnabled: strategy.reviewEnabled !== false,
    blockedKeywords: Array.isArray(strategy.blockedKeywords) ? strategy.blockedKeywords : [],
    reviewKeywords: Array.isArray(strategy.reviewKeywords) ? strategy.reviewKeywords : [],
    fallbackPlatforms: Array.isArray(strategy.fallbackPlatforms) && strategy.fallbackPlatforms.length
      ? strategy.fallbackPlatforms
      : platformOptions.map((item) => item.value),
    maxAttempts: Number(strategy.maxAttempts || 3)
  }
  nextStrategy.fallbackPlatforms = nextStrategy.fallbackPlatforms.filter((platform) =>
    platformOptions.some((item) => item.value === platform)
  )
  if (!nextStrategy.fallbackPlatforms.length) {
    nextStrategy.fallbackPlatforms = platformOptions.map((item) => item.value)
  }
  return nextStrategy
}

/**
 * 规范化配额策略数据，补齐页面展示字段。
 *
 * @param {Record<string, any>} policy 原始策略对象
 * @returns {Record<string, any>} 可直接渲染的策略对象
 */
function normalizePolicy(policy = {}) {
  const scopeValue = policy.scopeValue || ''
  const scopeType = policy.scopeType || 'GLOBAL'
  const scopeMeta = resolvePolicyMeta(scopeType, scopeValue.toLowerCase())
  return {
    ...policy,
    scopeType,
    scopeValue,
    hourlyLimit: Number(policy.hourlyLimit || 0),
    dailyLimit: Number(policy.dailyLimit || 0),
    enabled: policy.enabled !== false,
    remark: policy.remark || scopeMeta.defaultRemark,
    displayName: scopeMeta.label,
    icon: scopeMeta.icon,
    scopeDescription: scopeMeta.description
  }
}

/**
 * 规范化待审记录字段，屏蔽后端命名差异。
 *
 * @param {Record<string, any>} record 原始调用记录
 * @returns {Record<string, any>} 可渲染的待审记录
 */
function normalizePendingRecord(record = {}) {
  return {
    ...record,
    user: record.userId || '未知用户',
    questionContent: record.questionContent || '',
    reviewReason: record.reviewReason || '',
    finalPlatform: record.finalPlatform || record.requestedPlatform || 'openai',
    requestedPlatform: record.requestedPlatform || 'openai',
    userRole: record.userRole || '',
    latencyMs: Number(record.latencyMs || 0)
  }
}

/**
 * 解析配额策略的展示信息。
 *
 * @param {string} scopeType 作用域类型
 * @param {string} scopeValue 作用域值
 * @returns {{label: string, icon: string, description: string, defaultRemark: string}} 展示元数据
 */
function resolvePolicyMeta(scopeType, scopeValue) {
  if (scopeType === 'GLOBAL') {
    return {
      label: '全局基线策略',
      icon: 'globe',
      description: '所有未命中更细粒度规则的用户都会继承该基线配额。',
      defaultRemark: '系统默认全局基线配额'
    }
  }
  if (scopeType === 'ROLE' && scopeValue === 'admin') {
    return {
      label: '管理员角色',
      icon: 'user-shield',
      description: '用于后台运营与系统排查场景。',
      defaultRemark: '管理员角色默认配额'
    }
  }
  if (scopeType === 'ROLE' && scopeValue === 'teacher') {
    return {
      label: '教师角色',
      icon: 'chalkboard-teacher',
      description: '适用于课程备课、评测与教研助手调用。',
      defaultRemark: '教师角色默认配额'
    }
  }
  if (scopeType === 'ROLE' && scopeValue === 'student') {
    return {
      label: '学生角色',
      icon: 'user-graduate',
      description: '适用于学习辅导与问答对话场景。',
      defaultRemark: '学生角色默认配额'
    }
  }
  return {
    label: scopeType === 'USER' ? `用户 ${scopeValue}` : scopeValue || '未命名策略',
    icon: 'id-badge',
    description: '该策略为定向覆盖规则。',
    defaultRemark: '自定义治理策略'
  }
}

/**
 * 获取策略行唯一键，用于表格更新与局部保存状态跟踪。
 *
 * @param {Record<string, any>} row 策略行
 * @returns {string} 唯一键
 */
function getPolicyRowKey(row) {
  return row.id || `${row.scopeType}:${row.scopeValue}`
}

/**
 * 对数量做简短格式化，便于统计卡展示。
 *
 * @param {number} value 原始数量
 * @returns {string} 格式化后的数量
 */
function formatCompactNumber(value) {
  const numericValue = Number(value || 0)
  if (numericValue >= 1000000) {
    return `${(numericValue / 1000000).toFixed(1)}M`
  }
  if (numericValue >= 1000) {
    return `${(numericValue / 1000).toFixed(1)}k`
  }
  return `${numericValue}`
}

/**
 * 返回平台中文名称。
 *
 * @param {string} platform 平台编码
 * @returns {string} 平台展示名
 */
function platformLabel(platform) {
  return platformOptions.find((item) => item.value === platform)?.label || 'OpenAI'
}

/**
 * 返回平台默认模型名称。
 *
 * @param {string} platform 平台编码
 * @returns {string} 模型名称
 */
function platformModel(platform) {
  return platformOptions.find((item) => item.value === platform)?.model || 'gpt-4o'
}

/**
 * 将治理链路中的某个平台向前或向后移动。
 *
 * @param {number} index 当前索引
 * @param {number} delta 偏移量
 */
function moveFallbackPlatform(index, delta) {
  const targetIndex = index + delta
  if (targetIndex < 0 || targetIndex >= runtimeStrategy.value.fallbackPlatforms.length) {
    return
  }
  const nextPlatforms = [...runtimeStrategy.value.fallbackPlatforms]
  const [currentPlatform] = nextPlatforms.splice(index, 1)
  nextPlatforms.splice(targetIndex, 0, currentPlatform)
  runtimeStrategy.value.fallbackPlatforms = nextPlatforms
}

/**
 * 读取后端治理总览。
 */
async function loadOverview() {
  overview.value = {
    ...createDefaultOverview(),
    ...(await getAiGovernanceOverview())
  }
}

/**
 * 读取运行时治理策略。
 */
async function loadRuntimeStrategy() {
  runtimeStrategy.value = normalizeRuntimeStrategy(await getAiRuntimeStrategy())
  blockedKeywordsText.value = keywordsToText(runtimeStrategy.value.blockedKeywords)
  reviewKeywordsText.value = keywordsToText(runtimeStrategy.value.reviewKeywords)
}

/**
 * 读取配额策略列表。
 */
async function loadPolicies() {
  const list = await getAiQuotaPolicies()
  policies.value = Array.isArray(list) ? list.map(normalizePolicy) : []
}

/**
 * 读取待审队列。
 */
async function loadPendingRecords() {
  const list = await getAiGovernanceRecords({ reviewStatus: 'PENDING', page: 0, size: 8 })
  pendingRecords.value = Array.isArray(list) ? list.map(normalizePendingRecord) : []
}

/**
 * 初始化页面进入动画。
 */
async function initEnterAnimation() {
  await nextTick()
  if (enterAnim) {
    enterAnim.kill()
  }
  enterAnim = gsap.timeline()
  enterAnim
    .fromTo(
      headerRef.value,
      { y: -40, opacity: 0, filter: 'blur(10px)' },
      { y: 0, opacity: 1, filter: 'blur(0px)', duration: 0.8, ease: 'power3.out' }
    )
    .fromTo(
      '.bento-grid-container > div',
      { y: 40, opacity: 0, scale: 0.95 },
      { y: 0, opacity: 1, scale: 1, duration: 0.6, stagger: 0.08, ease: 'back.out(1.2)' },
      '-=0.4'
    )
    .fromTo(
      '.slide-up-element .chain-node-wrapper, .slide-up-element .pending-item, .platform-metric-card',
      { x: -25, opacity: 0 },
      { x: 0, opacity: 1, duration: 0.5, stagger: 0.05, ease: 'power2.out' },
      '-=0.3'
    )
}

/**
 * 执行一次完整的治理数据刷新。
 */
async function refreshData() {
  if (refreshing.value) {
    return
  }
  refreshing.value = true
  loadError.value = ''
  try {
    await Promise.all([loadOverview(), loadRuntimeStrategy(), loadPolicies(), loadPendingRecords()])
    await nextTick()
    gsap.fromTo(
      '.bento-stat-card',
      { y: 4, scale: 0.98, boxShadow: 'inset 4px 4px 8px #d1d1d4, inset -4px -4px 8px #ffffff' },
      { y: 0, scale: 1, boxShadow: '8px 8px 16px #d1d1d4, -8px -8px 16px #ffffff', duration: 0.5, stagger: 0.05, ease: 'back.out(2)' }
    )
  } catch (error) {
    console.error('加载 AI 治理数据失败:', error)
    loadError.value = typeof error === 'string' ? error : 'AI 治理数据加载失败，请检查后端服务或管理员权限。'
    ElMessage.error(loadError.value)
  } finally {
    refreshing.value = false
  }
}

/**
 * 仅刷新策略列表，避免整页重新拉取。
 */
async function refreshPoliciesOnly() {
  try {
    await loadPolicies()
    ElMessage.success({ message: '配额策略已刷新', customClass: 'neumorphism-message' })
  } catch (error) {
    console.error('刷新配额策略失败:', error)
    ElMessage.error(typeof error === 'string' ? error : '配额策略刷新失败')
  }
}

/**
 * 保存运行时治理策略。
 */
async function saveRuntimeConfig() {
  if (savingStrategy.value) {
    return
  }
  savingStrategy.value = true
  try {
    const payload = {
      reviewEnabled: runtimeStrategy.value.reviewEnabled,
      blockedKeywords: parseKeywords(blockedKeywordsText.value),
      reviewKeywords: parseKeywords(reviewKeywordsText.value),
      maxAttempts: runtimeStrategy.value.maxAttempts,
      fallbackPlatforms: runtimeStrategy.value.fallbackPlatforms
    }
    const savedStrategy = await saveAiRuntimeStrategy(payload)
    runtimeStrategy.value = normalizeRuntimeStrategy(savedStrategy)
    blockedKeywordsText.value = keywordsToText(runtimeStrategy.value.blockedKeywords)
    reviewKeywordsText.value = keywordsToText(runtimeStrategy.value.reviewKeywords)
    ElMessage.success({ message: '运行时治理策略已保存', customClass: 'neumorphism-message' })
  } catch (error) {
    console.error('保存运行时策略失败:', error)
    ElMessage.error(typeof error === 'string' ? error : '运行时策略保存失败')
  } finally {
    savingStrategy.value = false
  }
}

/**
 * 保存单条配额策略。
 *
 * @param {Record<string, any>} row 策略行
 */
async function savePolicy(row) {
  const policyKey = getPolicyRowKey(row)
  if (savingPolicyKey.value === policyKey) {
    return
  }
  savingPolicyKey.value = policyKey
  try {
    const savedPolicy = await saveAiQuotaPolicy({
      scopeType: row.scopeType,
      scopeValue: row.scopeValue,
      dailyLimit: Number(row.dailyLimit || 0),
      hourlyLimit: Number(row.hourlyLimit || 0),
      enabled: row.enabled,
      remark: row.remark
    })
    const normalized = normalizePolicy(savedPolicy)
    policies.value = policies.value.map((item) =>
      getPolicyRowKey(item) === policyKey ? normalized : item
    )
    ElMessage.success({ message: `${normalized.displayName} 已保存`, customClass: 'neumorphism-message' })
  } catch (error) {
    console.error('保存配额策略失败:', error)
    ElMessage.error(typeof error === 'string' ? error : '配额策略保存失败')
  } finally {
    savingPolicyKey.value = ''
  }
}

/**
 * 播放待审卡片退出动画。
 *
 * @param {string} recordId 记录 ID
 * @param {string} decision 审查结论
 * @returns {Promise<void>} 动画完成 Promise
 */
function animateReviewItem(recordId, decision) {
  return new Promise((resolve) => {
    const element = document.querySelector(`[data-record-id="${recordId}"]`)
    if (!element) {
      resolve()
      return
    }
    gsap.to(element, {
      x: decision === 'APPROVED' ? 120 : -120,
      opacity: 0,
      height: 0,
      marginBottom: 0,
      paddingTop: 0,
      paddingBottom: 0,
      duration: 0.35,
      ease: 'power2.inOut',
      onComplete: resolve
    })
  })
}

/**
 * 提交管理员审查结论并刷新待审队列。
 *
 * @param {string} recordId 记录 ID
 * @param {'APPROVED' | 'REJECTED'} decision 审查结论
 */
async function handleReview(recordId, decision) {
  if (reviewingRecordId.value) {
    return
  }
  reviewingRecordId.value = recordId
  reviewingDecision.value = decision
  try {
    await reviewAiGovernanceRecord(recordId, {
      decision,
      note: decision === 'APPROVED'
        ? '管理员确认该记录为误判，已移出待审队列'
        : '管理员确认命中治理规则，保留拦截结论'
    })
    await animateReviewItem(recordId, decision)
    pendingRecords.value = pendingRecords.value.filter((item) => item.id !== recordId)
    await Promise.all([loadOverview(), loadPendingRecords()])
    ElMessage({
      type: decision === 'APPROVED' ? 'success' : 'warning',
      message: decision === 'APPROVED' ? '该记录已标记为误判' : '该记录已确认拦截',
      customClass: 'neumorphism-message'
    })
  } catch (error) {
    console.error('提交审查结论失败:', error)
    ElMessage.error(typeof error === 'string' ? error : '审查提交失败')
  } finally {
    reviewingRecordId.value = ''
    reviewingDecision.value = ''
  }
}

const successRate = computed(() => {
  if (!overview.value.totalCallsToday) {
    return 0
  }
  return Number(((overview.value.successCallsToday / overview.value.totalCallsToday) * 100).toFixed(1))
})

const platformMetrics = computed(() => Array.isArray(overview.value.platformMetrics) ? overview.value.platformMetrics : [])

const fallbackChain = computed(() => runtimeStrategy.value.fallbackPlatforms.map((platform) => ({
  platform,
  provider: platformLabel(platform),
  name: platformModel(platform)
})))

const stats = computed(() => [
  {
    title: '今日 AI 调用',
    value: formatCompactNumber(overview.value.totalCallsToday),
    label: '次',
    icon: 'bolt',
    colorClass: 'text-primary',
    textClass: 'text-primary',
    meta: `成功 ${overview.value.successCallsToday} 次`,
    metaIcon: 'check',
    indicatorClass: 'positive'
  },
  {
    title: '待审治理队列',
    value: formatCompactNumber(overview.value.pendingReviews),
    label: '条',
    icon: 'shield-virus',
    colorClass: 'text-danger',
    textClass: 'text-danger',
    meta: `拒绝 ${overview.value.rejectedCallsToday} 次`,
    metaIcon: 'triangle-exclamation',
    indicatorClass: 'warning'
  },
  {
    title: '降级重试触发',
    value: formatCompactNumber(overview.value.degradedCallsToday),
    label: '次',
    icon: 'project-diagram',
    colorClass: 'text-warning',
    textClass: 'text-warning',
    meta: `失败 ${overview.value.failedCallsToday} 次`,
    metaIcon: 'repeat',
    indicatorClass: 'warning'
  },
  {
    title: '今日调用成功率',
    value: `${successRate.value}%`,
    label: '成功率',
    icon: 'chart-line',
    colorClass: 'text-success',
    textClass: 'text-success',
    meta: `平台 ${platformMetrics.value.length} 个`,
    metaIcon: 'server',
    indicatorClass: 'positive'
  }
])

onMounted(async () => {
  await refreshData()
  await initEnterAnimation()
})

onUnmounted(() => {
  if (enterAnim) {
    enterAnim.kill()
  }
})
</script>

<style scoped>
/* ====================================================
   全局基础配置与变量：遵循《前端开发设计约束手册》
   主色: #002FA7 (克莱因蓝) | 辅助/成功色: #517B4D (绿琉璃)
   危险色: #F56C6C | 警告色: #E6A23C
   背景色: #F0F0F3 (新拟态基底)
==================================================== */
.bg-neumorphism {
  background-color: #F0F0F3;
  color: #303133;
  min-height: 100vh;
  padding: 24px;
}

.text-primary { color: #002FA7 !important; }
.text-success { color: #517B4D !important; }
.text-danger { color: #F56C6C !important; }
.text-warning { color: #E6A23C !important; }
.text-main { color: #303133; font-weight: 600; }
.text-secondary { color: #909399; }
.text-content { color: #606266; }

.drop-shadow-success { filter: drop-shadow(0px 4px 6px rgba(81,123,77,0.3)); }

/* ====================================================
   新拟态卡片 (Neumorphism Design)
   对应设计约束手册: raised / inset / hover
==================================================== */
.neumorphism-raised {
  background: #f0f0f3;
  box-shadow: 8px 8px 16px #d1d1d4, -8px -8px 16px #ffffff;
  border-radius: 16px;
  border: none;
  transition: all 0.3s ease;
}

.neumorphism-inset {
  background: #f0f0f3;
  box-shadow: inset 6px 6px 12px #d1d1d4, inset -6px -6px 12px #ffffff;
  border-radius: 12px;
  border: none;
}

.neumorphism-hover:hover, .hover-lift:hover {
  box-shadow: 12px 12px 20px #d1d1d4, -12px -12px 20px #ffffff;
  transform: translateY(-3px);
}

/* ====================================================
   磨砂玻璃 (Frosted Glass Style)
   对应前端设计约束手册
==================================================== */
.frosted-glass {
  background: rgba(240, 240, 243, 0.45);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  box-shadow: 4px 4px 10px rgba(0,0,0,0.03);
}

.frosted-glass-light {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 12px;
}

/* ====================================================
   组件级定制样式
==================================================== */

/* Header */
.modern-header {
  padding: 24px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  width: 100%;
}
.header-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.header-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}
.header-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 13px;
}
.page-title {
  display: flex;
  align-items: center;
  font-size: 24px;
  font-weight: 800;
  margin: 0 0 8px 0;
  letter-spacing: 0.5px;
}
.icon-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  margin-right: 16px;
  background: #f0f0f3;
  box-shadow: 4px 4px 8px #d1d1d4, -4px -4px 8px #ffffff;
  font-size: 20px;
}
.primary-glow {
  text-shadow: 0 0 12px rgba(0, 47, 167, 0.25);
}

.custom-button {
  background: #f0f0f3;
  color: #002FA7;
  box-shadow: 6px 6px 12px #d1d1d4, -6px -6px 12px #ffffff;
  border: none;
  font-weight: 600;
  border-radius: 10px;
  padding: 10px 24px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.custom-button:hover {
  color: #002FA7;
  box-shadow: 3px 3px 6px #d1d1d4, -3px -3px 6px #ffffff;
  transform: scale(0.98);
}
.custom-button:active {
  box-shadow: inset 4px 4px 8px #d1d1d4, inset -4px -4px 8px #ffffff;
}

.custom-button-small {
  background: #f0f0f3;
  font-weight: 600;
  padding: 6px 16px;
  transition: all 0.2s;
}
.custom-button-small:hover {
  box-shadow: inset 2px 2px 5px #d1d1d4, inset -2px -2px 5px #ffffff;
}

.custom-text-btn {
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s;
}
.custom-text-btn:hover {
  background: #f0f0f3;
  box-shadow: 4px 4px 8px #d1d1d4, -4px -4px 8px #ffffff;
}
.save-btn {
  min-width: 104px;
}

/* Bento Grid System by UI-UX-Pro-Max recommendation */
.bento-grid-container {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  grid-auto-rows: auto;
  gap: 24px;
}

.bento-stat-card {
  grid-column: span 3;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 140px;
}

.bento-main-card {
  grid-column: span 8;
  padding: 24px;
}

.bento-right-card {
  grid-column: span 4;
  grid-row: span 2;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.bento-bottom-card {
  grid-column: span 8;
  padding: 24px;
}

@media (max-width: 1300px) {
  .bento-stat-card { grid-column: span 6; }
  .bento-main-card { grid-column: span 12; }
  .bento-right-card { grid-column: span 12; grid-row: auto; }
  .bento-bottom-card { grid-column: span 12; }
}

/* Card Internals */
.card-header, .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.align-start {
  align-items: flex-start;
}
.section-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.section-subtitle {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.6;
}
.section-title {
  display: flex;
  align-items: center;
  font-size: 17px;
  margin: 0;
}
.title-icon {
  margin-right: 12px;
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #f0f0f3;
  box-shadow: 4px 4px 8px #d1d1d4, -4px -4px 8px #ffffff;
  font-size: 14px;
}
.card-icon-container {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  font-size: 16px;
}
.stat-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 12px;
}
.stat-number {
  font-size: 32px;
  font-weight: 900;
  line-height: 1.1;
  letter-spacing: -0.5px;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
  margin-top: 4px;
}
.growth-indicator {
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  padding: 5px 10px;
  border-radius: 20px;
  gap: 6px;
  font-weight: 600;
  background: #f0f0f3;
  box-shadow: inset 3px 3px 6px #d1d1d4, inset -3px -3px 6px #ffffff;
}
.growth-indicator.positive {
  color: #517B4D;
}
.growth-indicator.warning {
  color: #E6A23C;
}

/* Fallback Chain */
.fallback-chain {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  padding: 16px;
}
.chain-node-wrapper {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.chain-node {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  min-width: 160px;
  position: relative;
  gap: 14px;
}
.chain-node.active-node {
  box-shadow: inset 3px 3px 6px #d1d1d4, inset -3px -3px 6px #ffffff;
  border: 1px solid rgba(0, 47, 167, 0.15);
}
.node-info {
  display: flex;
  flex-direction: column;
  margin-left: 14px;
}
.node-info .provider {
  font-size: 11px;
  color: #909399;
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 0.5px;
}
.node-info .model-name {
  font-size: 14px;
  color: #303133;
}
.chain-node.active-node .model-name {
  color: #002FA7;
  font-weight: 700;
}
.chain-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-left: auto;
}
.node-arrow { margin: 0 16px; font-size: 18px; }
.node-status {
  position: absolute;
  top: -8px;
  right: -8px;
  font-size: 11px;
  display: flex;
  align-items: center;
  color: #517B4D;
  background: #f0f0f3;
  box-shadow: 2px 2px 5px #d1d1d4, -2px -2px 5px #ffffff;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 700;
  letter-spacing: 0.5px;
}
.pulse-dot {
  width: 6px;
  height: 6px;
  background-color: #517B4D;
  border-radius: 50%;
  margin-right: 6px;
  box-shadow: 0 0 8px #517B4D;
  animation: pulse-op 1.5s infinite;
}
.pulse-badge {
  color: #F56C6C;
  font-weight: bold;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 14px;
}

.inline-control-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.platform-metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.platform-metric-card {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.platform-metric-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.platform-name {
  font-size: 14px;
  font-weight: 700;
}

.platform-metric-value {
  font-size: 24px;
  line-height: 1;
}

.platform-metric-meta {
  font-size: 12px;
}

.keywords-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.keyword-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.keyword-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

@keyframes pulse-op {
  0% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(1.3); }
  100% { opacity: 1; transform: scale(1); }
}

/* Policies Table */
.icon-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}
.policy-title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.policy-scope-desc {
  line-height: 1.4;
}
.policy-number-input {
  width: 120px;
}
.policy-remark-input {
  width: 100%;
}
.neumorphism-tag {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  background: #f0f0f3;
  box-shadow: inset 3px 3px 6px #d1d1d4, inset -3px -3px 6px #ffffff;
}
.flex-center {
  display: flex;
  justify-content: center;
  align-items: center;
}

/* Queue List */
.tech-border-left-red {
  position: relative;
  overflow: hidden;
}
.tech-border-left-red::before {
  content: "";
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 5px;
  background: #F56C6C;
  box-shadow: 2px 0 8px rgba(245, 108, 108, 0.4);
}
.queue-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  line-height: 1.5;
}
.record-content {
  line-height: 1.7;
  max-height: 150px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
}
.empty-state,
.empty-panel {
  text-align: center;
}

/* Utilities */
.flex { display: flex; }
.justify-between { justify-content: space-between; }
.justify-end { justify-content: flex-end; }
.items-center { align-items: center; }
.flex-col { flex-direction: column; }
.wrap-row { flex-wrap: wrap; }
.gap-sm { gap: 10px; }
.p-md { padding: 16px; }
.p-sm { padding: 12px; }
.py-xl { padding-top: 40px; padding-bottom: 40px; }
.mb-sm { margin-bottom: 12px; }
.mt-sm { margin-top: 12px; }
.mt-md { margin-top: 16px; }
.mt-lg { margin-top: 24px; }
.mr-sm { margin-right: 12px; }
.mr-xs { margin-right: 6px; }
.w-full { width: 100%; }
.h-full { height: 100%; }
.text-xs { font-size: 12px; }
.text-sm { font-size: 14px; }
.text-md { font-size: 15px; }
.rounded-md { border-radius: 10px; }
.font-bold { font-weight: 700; }
.border-none { border: none !important; }

/* Element Plus global overrides scoped to component */
:deep(.el-table) {
  background: transparent !important;
  --el-table-border-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-row-hover-bg-color: rgba(0, 47, 167, 0.03);
}
:deep(.el-table tr), :deep(.el-table th.el-table__cell) {
  background: transparent !important;
  border-bottom: 2px solid rgba(209, 209, 212, 0.3);
}
:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid rgba(209, 209, 212, 0.3);
}
:deep(.el-table::before) {
  display: none;
}
:deep(.el-input-number) {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: inset 4px 4px 8px #d1d1d4, inset -4px -4px 8px #ffffff;
}
:deep(.el-input-number .el-input__wrapper) {
  background: transparent;
  box-shadow: none !important;
  padding: 0 10px;
}
:deep(.el-input-number__decrease), :deep(.el-input-number__increase) {
  background: transparent !important;
  border: none !important;
  color: #002FA7;
}
:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-select__wrapper) {
  background: rgba(240, 240, 243, 0.95);
  box-shadow: inset 4px 4px 8px #d1d1d4, inset -4px -4px 8px #ffffff;
  border: none !important;
}

@media (max-width: 992px) {
  .header-content {
    flex-direction: column;
  }

  .platform-metrics-grid,
  .keywords-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .modern-header {
    padding: 20px;
  }

  .page-title {
    font-size: 20px;
  }

  .policy-number-input {
    width: 100%;
  }
}
</style>