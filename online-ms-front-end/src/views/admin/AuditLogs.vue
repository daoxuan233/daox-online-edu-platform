<template>
  <div class="audit-logs-container">
    <!-- 页面头部 -->
    <section class="page-hero glass-card" ref="headerRef">
      <div class="hero-copy">
        <div class="hero-title-row">
          <span class="hero-badge tech-tag info">统一审计日志</span>
          <span class="hero-status tech-tag" :class="usingMockData ? 'warning' : 'success'">
            <span class="pulse-dot" :class="usingMockData ? 'warning' : 'success'"></span>
            {{ usingMockData ? '演示数据模式' : '实时数据模式' }}
          </span>
        </div>
        <h1 class="page-title">
          <div class="icon-wrapper">
            <font-awesome-icon :icon="['fas', 'shield-halved']" />
          </div>
          敏感操作审计中心
        </h1>
        <p class="page-subtitle">
          汇总删课、密码重置、同步触发等管理员敏感行为，统一查看操作者、请求源、资源定位与变更快照。
        </p>
        <div class="hero-meta" aria-label="审计范围说明">
          <span class="hero-meta-item glass-btn">
            <font-awesome-icon :icon="['fas', 'route']" class="text-gradient-blue" />
            请求源留痕
          </span>
          <span class="hero-meta-item glass-btn">
            <font-awesome-icon :icon="['fas', 'fingerprint']" class="text-gradient-green" />
            操作者识别
          </span>
          <span class="hero-meta-item glass-btn">
            <font-awesome-icon :icon="['fas', 'camera']" class="text-gradient-orange" />
            前后快照对比
          </span>
        </div>
      </div>
      <div class="hero-actions">
        <el-button class="action-btn glass-btn" @click="exportCurrentView">
          <font-awesome-icon :icon="['fas', 'download']" />
          导出当前视图
        </el-button>
        <el-button type="primary" class="action-btn tech-btn" :loading="loading" @click="loadAuditLogs">
          <font-awesome-icon :icon="['fas', 'arrows-rotate']" :class="{'fa-spin': loading}" />
          刷新日志
        </el-button>
      </div>
    </section>

    <el-alert
      v-if="usingMockData"
      class="fallback-alert glass-card warning-alert"
      type="warning"
      :closable="false"
      show-icon
      title="当前未获取到后端审计查询接口数据，页面已自动切换为演示数据，便于管理员先确认信息结构与交互。"
      role="alert"
    />

    <!-- 统计卡片 -->
    <section class="stats-grid" ref="statsRef">
      <article class="stat-card glass-card hover-float">
        <div class="stat-icon gradient-blue">
          <font-awesome-icon :icon="['fas', 'database']" />
        </div>
        <div class="stat-content">
          <span class="stat-label">日志总数</span>
          <strong class="stat-value text-gradient-blue">{{ stats.total }}</strong>
          <span class="stat-hint">按当前数据源统计</span>
        </div>
      </article>
      <article class="stat-card glass-card hover-float">
        <div class="stat-icon gradient-danger">
          <font-awesome-icon :icon="['fas', 'triangle-exclamation']" />
        </div>
        <div class="stat-content">
          <span class="stat-label">失败事件</span>
          <strong class="stat-value text-gradient-danger">{{ stats.failed }}</strong>
          <span class="stat-hint">需要优先复核的异常操作</span>
        </div>
      </article>
      <article class="stat-card glass-card hover-float">
        <div class="stat-icon gradient-green">
          <font-awesome-icon :icon="['fas', 'book-open']" />
        </div>
        <div class="stat-content">
          <span class="stat-label">删课记录</span>
          <strong class="stat-value text-gradient-green">{{ stats.courseDelete }}</strong>
          <span class="stat-hint">课程资源链路变更</span>
        </div>
      </article>
      <article class="stat-card glass-card hover-float">
        <div class="stat-icon gradient-warning">
          <font-awesome-icon :icon="['fas', 'key']" />
        </div>
        <div class="stat-content">
          <span class="stat-label">密码重置</span>
          <strong class="stat-value text-gradient-warning">{{ stats.passwordReset }}</strong>
          <span class="stat-hint">账户安全敏感动作</span>
        </div>
      </article>
      <article class="stat-card glass-card hover-float">
        <div class="stat-icon gradient-purple">
          <font-awesome-icon :icon="['fas', 'arrows-rotate']" />
        </div>
        <div class="stat-content">
          <span class="stat-label">同步触发</span>
          <strong class="stat-value text-gradient-purple">{{ stats.syncTrigger }}</strong>
          <span class="stat-hint">向量与数据同步动作</span>
        </div>
      </article>
    </section>

    <!-- 筛选面板 -->
    <section class="filter-panel glass-card" ref="filterRef">
      <div class="filter-panel-header">
        <div class="section-title">
          <div class="title-icon"><font-awesome-icon :icon="['fas', 'sliders']" /></div>
          <div>
            <h3>日志筛选</h3>
            <p class="section-subtitle">支持按动作、状态、角色、时间窗口与关键词快速定位关键操作。</p>
          </div>
        </div>
        <div class="filter-actions">
          <el-button class="glass-btn" @click="resetFilters">重置</el-button>
          <el-button type="primary" class="tech-btn" @click="applyFilters">应用筛选</el-button>
        </div>
      </div>

      <div class="filter-grid">
        <div class="filter-item">
          <label for="audit-keyword" class="filter-label">关键词</label>
          <el-input
            id="audit-keyword"
            v-model="filters.keyword"
            placeholder="搜索资源ID、操作者等"
            clearable
            class="tech-input"
          >
            <template #prefix>
              <font-awesome-icon :icon="['fas', 'magnifying-glass']" />
            </template>
          </el-input>
        </div>
        <div class="filter-item">
          <label for="audit-action" class="filter-label">操作类型</label>
          <el-select id="audit-action" v-model="filters.action" placeholder="全部操作" clearable class="tech-select">
            <el-option
              v-for="option in actionOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </div>
        <div class="filter-item">
          <label for="audit-status" class="filter-label">状态</label>
          <el-select id="audit-status" v-model="filters.status" placeholder="全部状态" clearable class="tech-select">
            <el-option
              v-for="option in statusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </div>
        <div class="filter-item">
          <label for="audit-role" class="filter-label">操作者角色</label>
          <el-select id="audit-role" v-model="filters.operatorRole" placeholder="全部角色" clearable class="tech-select">
            <el-option
              v-for="option in roleOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </div>
        <div class="filter-item filter-item-wide">
          <label for="audit-range" class="filter-label">时间范围</label>
          <el-date-picker
            id="audit-range"
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="tech-date-picker"
          />
        </div>
      </div>
    </section>

    <!-- 列表内容区域 -->
    <section class="audit-content">
      <div class="content-header" ref="listHeaderRef">
        <div class="section-title">
          <div class="title-icon"><font-awesome-icon :icon="['fas', 'list-check']" /></div>
          <div>
            <h3>审计事件列表</h3>
            <p class="section-subtitle">当前共命中 {{ filteredLogs.length }} 条日志，默认按最新时间倒序展示。</p>
          </div>
        </div>
        
        <div class="content-summary">
          <!-- 视图切换 (全局默认卡片) -->
          <div class="view-toggle mr-md">
            <el-radio-group v-model="viewMode" size="small" class="tech-radio">
              <el-radio-button label="grid">
                <font-awesome-icon :icon="['fas', 'th-large']" />
                卡片
              </el-radio-button>
              <el-radio-button label="table">
                <font-awesome-icon :icon="['fas', 'list']" />
                列表
              </el-radio-button>
            </el-radio-group>
          </div>
          
          <span class="summary-pill glass-btn">
            <font-awesome-icon :icon="['fas', 'server']" class="text-gradient-blue" />
            {{ topRequestSource }}
          </span>
          <span class="summary-pill glass-btn">
            <font-awesome-icon :icon="['fas', 'user-shield']" class="text-gradient-orange" />
            {{ topOperator }}
          </span>
        </div>
      </div>

      <div v-if="loading" class="skeleton-grid" aria-live="polite">
        <el-skeleton v-for="item in 4" :key="item" animated class="skeleton-card glass-card">
          <template #template>
            <el-skeleton-item variant="rect" style="width: 100%; height: 180px" />
          </template>
        </el-skeleton>
      </div>

      <template v-else>
        <el-empty
          v-if="!paginatedLogs.length"
          description="当前条件下没有匹配的审计日志"
          class="glass-card"
        />

        <!-- 卡片视图 (默认) -->
        <div v-else-if="viewMode === 'grid'" class="logs-grid" ref="listGridRef">
          <div 
            v-for="log in paginatedLogs" 
            :key="log.id"
            class="log-card glass-card hover-float"
            @click="openDetail(log)"
          >
            <div class="log-card-header">
              <div class="log-action">
                <div class="action-icon tech-icon-box small" :class="actionToneClass(log.action)">
                  <font-awesome-icon :icon="actionIcon(log.action)" />
                </div>
                <div class="action-info">
                  <span class="action-name">{{ actionLabel(log.action) }}</span>
                  <span class="action-time">{{ formatDateTime(log.createdAt) }}</span>
                </div>
              </div>
              <div class="log-status">
                <span :class="['tech-tag', statusTagType(log.status)]">
                  {{ statusLabel(log.status) }}
                </span>
              </div>
            </div>
            
            <div class="log-card-body">
              <p class="log-message">{{ log.message || '无额外说明' }}</p>
              
              <div class="log-details-grid">
                <div class="detail-col">
                  <span class="detail-label">操作者</span>
                  <span class="detail-value">{{ log.operatorName || '系统任务' }}</span>
                  <span class="detail-sub">{{ log.operatorIdentifier || log.operatorId || '未知标识' }}</span>
                </div>
                <div class="detail-col">
                  <span class="detail-label">资源定位</span>
                  <span class="detail-value">{{ resourceLabel(log.resourceType) }}</span>
                  <span class="detail-sub text-truncate" :title="log.resourceId">{{ log.resourceId || '未记录资源ID' }}</span>
                </div>
                <div class="detail-col">
                  <span class="detail-label">请求来源</span>
                  <span class="detail-value">{{ sourceLabel(log) }}</span>
                  <span class="detail-sub">{{ log.requestIp || '未知IP' }}</span>
                </div>
              </div>
            </div>
            
            <div class="log-card-footer">
              <span class="request-method" :class="log.requestMethod.toLowerCase()">
                {{ log.requestMethod || 'GET' }}
              </span>
              <span class="request-path text-truncate" :title="log.requestPath">
                {{ log.requestPath || '/' }}
              </span>
              <el-button size="small" text class="tech-text-btn ml-auto" @click.stop="openDetail(log)">
                详情 <font-awesome-icon :icon="['fas', 'arrow-right']" class="ml-xs" />
              </el-button>
            </div>
          </div>
        </div>

        <!-- 表格视图 -->
        <div v-else class="table-shell glass-card" ref="listTableRef">
          <el-table
            :data="paginatedLogs"
            row-key="id"
            class="audit-table tech-table"
            @row-click="openDetail"
          >
            <el-table-column label="操作事件" min-width="240">
              <template #default="{ row }">
                <div class="action-cell">
                  <span class="action-icon tech-icon-box small" :class="actionToneClass(row.action)">
                    <font-awesome-icon :icon="actionIcon(row.action)" />
                  </span>
                  <div class="action-copy">
                    <strong>{{ actionLabel(row.action) }}</strong>
                    <span class="text-muted text-truncate" :title="row.message">{{ row.message || '无额外说明' }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="操作者" min-width="170">
              <template #default="{ row }">
                <div class="operator-cell">
                  <strong>{{ row.operatorName || '系统任务' }}</strong>
                  <span class="text-muted">{{ row.operatorIdentifier || row.operatorId || '未知标识' }}</span>
                  <span :class="['tech-tag', roleTagType(row.operatorRole)]">
                    {{ roleLabel(row.operatorRole) }}
                  </span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="资源定位" min-width="180">
              <template #default="{ row }">
                <div class="resource-cell">
                  <strong>{{ resourceLabel(row.resourceType) }}</strong>
                  <span class="text-muted text-truncate" :title="row.resourceId">{{ row.resourceId || '未记录资源ID' }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="请求源" min-width="180">
              <template #default="{ row }">
                <div class="source-cell">
                  <strong>{{ sourceLabel(row) }}</strong>
                  <span class="text-muted">{{ row.requestIp || '未知IP' }}</span>
                  <small class="text-gradient-blue">{{ row.requestMethod || 'GET' }} {{ row.requestPath || '/' }}</small>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <span :class="['tech-tag', statusTagType(row.status)]">
                  {{ statusLabel(row.status) }}
                </span>
              </template>
            </el-table-column>

            <el-table-column label="发生时间" width="160">
              <template #default="{ row }">
                <span class="text-muted">{{ formatDateTime(row.createdAt) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button
                  text
                  class="tech-text-btn"
                  aria-label="查看审计详情"
                  @click.stop="openDetail(row)"
                >
                  <font-awesome-icon :icon="['fas', 'up-right-from-square']" />
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div class="pagination-section glass-card mt-lg">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :page-sizes="[10, 20, 50]"
            :total="filteredLogs.length"
            class="tech-pagination"
            @current-change="handlePageChange"
            @size-change="handlePageChange"
          />
        </div>
      </template>
    </section>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="detailVisible"
      size="50%"
      :with-header="false"
      destroy-on-close
      append-to-body
      class="tech-drawer glass-drawer"
      @open="onDrawerOpen"
    >
      <div v-if="selectedLog" class="drawer-content">
        <header class="drawer-header glass-card">
          <div class="header-main">
            <div class="action-icon tech-icon-box" :class="actionToneClass(selectedLog.action)">
              <font-awesome-icon :icon="actionIcon(selectedLog.action)" />
            </div>
            <div class="header-text">
              <div class="title-row">
                <span class="drawer-badge tech-tag info">{{ actionLabel(selectedLog.action) }}</span>
                <h2>{{ selectedLog.message || actionLabel(selectedLog.action) }}</h2>
              </div>
              <p class="meta-info">
                <font-awesome-icon :icon="['fas', 'clock']" class="mr-xs text-gradient-blue" />
                {{ formatDateTime(selectedLog.createdAt) }} 
                <span class="divider">|</span> 
                <span class="request-method" :class="selectedLog.requestMethod?.toLowerCase()">{{ selectedLog.requestMethod || 'GET' }}</span> 
                {{ selectedLog.requestPath || '/' }}
              </p>
            </div>
          </div>
          <div class="header-status">
            <span :class="['tech-tag', statusTagType(selectedLog.status)]" style="font-size: 14px; padding: 6px 16px;">
              {{ statusLabel(selectedLog.status) }}
            </span>
          </div>
        </header>

        <section class="detail-grid">
          <article class="detail-card glass-card">
            <h3>
              <font-awesome-icon :icon="['fas', 'user-shield']" class="text-gradient-orange mr-sm" />
              操作者信息
            </h3>
            <dl class="tech-dl">
              <div>
                <dt>姓名</dt>
                <dd>{{ selectedLog.operatorName || '系统任务' }}</dd>
              </div>
              <div>
                <dt>标识</dt>
                <dd class="code-font">{{ selectedLog.operatorIdentifier || selectedLog.operatorId || '未知' }}</dd>
              </div>
              <div>
                <dt>角色</dt>
                <dd><span :class="['tech-tag', roleTagType(selectedLog.operatorRole)]">{{ roleLabel(selectedLog.operatorRole) }}</span></dd>
              </div>
            </dl>
          </article>

          <article class="detail-card glass-card">
            <h3>
              <font-awesome-icon :icon="['fas', 'box']" class="text-gradient-green mr-sm" />
              资源信息
            </h3>
            <dl class="tech-dl">
              <div>
                <dt>资源类型</dt>
                <dd>{{ resourceLabel(selectedLog.resourceType) }}</dd>
              </div>
              <div>
                <dt>资源ID</dt>
                <dd class="code-font">{{ selectedLog.resourceId || '未记录' }}</dd>
              </div>
              <div>
                <dt>操作动作</dt>
                <dd class="code-font">{{ selectedLog.action }}</dd>
              </div>
            </dl>
          </article>

          <article class="detail-card detail-card-wide glass-card">
            <h3>
              <font-awesome-icon :icon="['fas', 'network-wired']" class="text-gradient-blue mr-sm" />
              请求源留痕
            </h3>
            <div class="request-source-grid">
              <div class="source-metric tech-inset">
                <span>客户端IP</span>
                <strong class="code-font">{{ selectedLog.requestIp || '未知' }}</strong>
              </div>
              <div class="source-metric tech-inset">
                <span>来源判定</span>
                <strong>{{ sourceLabel(selectedLog) }}</strong>
              </div>
              <div class="source-metric tech-inset">
                <span>Host</span>
                <strong class="code-font">{{ selectedLog.requestSourceObject.host || '未记录' }}</strong>
              </div>
              <div class="source-metric tech-inset">
                <span>Referer</span>
                <strong class="code-font text-truncate" :title="selectedLog.requestSourceObject.referer">{{ selectedLog.requestSourceObject.referer || '未记录' }}</strong>
              </div>
            </div>
            <div class="source-user-agent tech-inset mt-sm">
              <span>User-Agent</span>
              <p class="code-font text-muted">{{ selectedLog.requestSourceObject.userAgent || '未记录 User-Agent' }}</p>
            </div>
          </article>
        </section>

        <section class="snapshot-section">
          <div class="snapshot-card glass-card dark-mode">
            <div class="snapshot-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'file-code']" class="mr-sm" />
                变更前快照
              </h3>
              <span class="tech-tag info">{{ selectedLog.beforeSnapshotObject ? '已记录' : '无数据' }}</span>
            </div>
            <div class="code-container">
              <pre><code>{{ selectedLog.beforeSnapshotPretty }}</code></pre>
            </div>
          </div>
          <div class="snapshot-card glass-card dark-mode">
            <div class="snapshot-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'file-code']" class="mr-sm text-gradient-green" />
                变更后快照
              </h3>
              <span class="tech-tag info">{{ selectedLog.afterSnapshotObject ? '已记录' : '无数据' }}</span>
            </div>
            <div class="code-container">
              <pre><code>{{ selectedLog.afterSnapshotPretty }}</code></pre>
            </div>
          </div>
        </section>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminAuditLogs } from '@/api/admin/auditLogs'
import { formatDateTime } from '@/utils/dateUtils'
import { gsap } from 'gsap'

// GSAP Refs
const headerRef = ref(null)
const statsRef = ref(null)
const filterRef = ref(null)
const listHeaderRef = ref(null)
const listGridRef = ref(null)
const listTableRef = ref(null)
let tl = null

const loading = ref(false)
const usingMockData = ref(false)
const detailVisible = ref(false)
const selectedLog = ref(null)
const currentPage = ref(1)
const pageSize = ref(12) // 默认 12 以适应卡片网格
const logs = ref([])
const viewMode = ref('grid') // 默认使用卡片流布局

const filters = ref({
  keyword: '',
  action: '',
  status: '',
  operatorRole: '',
  dateRange: []
})

const actionOptions = [
  { label: '删除课程', value: 'COURSE_DELETE' },
  { label: '密码重置', value: 'USER_PASSWORD_RESET' },
  { label: '同步触发', value: 'QUESTION_VECTOR_SYNC_TRIGGER' }
]

const statusOptions = [
  { label: '成功', value: 'SUCCESS' },
  { label: '失败', value: 'FAILED' },
  { label: '已启动', value: 'STARTED' }
]

const roleOptions = [
  { label: '管理员', value: 'admin' },
  { label: '教师', value: 'teacher' },
  { label: '学生', value: 'student' },
  { label: '系统任务', value: 'system' }
]

const safeParseJson = (value) => {
  if (!value) return null
  if (typeof value === 'object') return value
  try { return JSON.parse(value) } catch { return null }
}

const prettyPrint = (value) => {
  if (!value) return '暂无记录'
  if (typeof value === 'string') return value
  return JSON.stringify(value, null, 2)
}

const normalizePayload = (payload) => {
  if (Array.isArray(payload)) return payload
  if (!payload || typeof payload !== 'object') return []
  const possibleKeys = ['records', 'items', 'list', 'content', 'data']
  for (const key of possibleKeys) {
    if (Array.isArray(payload[key])) return payload[key]
  }
  return []
}

const normalizeLog = (item, index) => {
  const requestSourceObject = safeParseJson(item.requestSource) || {}
  const beforeSnapshotObject = safeParseJson(item.beforeSnapshot)
  const afterSnapshotObject = safeParseJson(item.afterSnapshot)
  return {
    id: item.id || `audit-log-${index + 1}`,
    action: item.action || 'UNKNOWN',
    resourceType: item.resourceType || 'unknown',
    resourceId: item.resourceId || '',
    status: item.status || 'UNKNOWN',
    message: item.message || '',
    operatorId: item.operatorId || '',
    operatorIdentifier: item.operatorIdentifier || '',
    operatorName: item.operatorName || '',
    operatorRole: item.operatorRole || (item.operatorId ? 'admin' : 'system'),
    requestIp: item.requestIp || requestSourceObject.ip || '',
    requestMethod: item.requestMethod || '',
    requestPath: item.requestPath || '',
    requestSourceObject,
    beforeSnapshotObject,
    afterSnapshotObject,
    beforeSnapshotPretty: prettyPrint(beforeSnapshotObject || item.beforeSnapshot),
    afterSnapshotPretty: prettyPrint(afterSnapshotObject || item.afterSnapshot),
    createdAt: item.createdAt || new Date().toISOString()
  }
}

const mockAuditLogs = () => ([
  {
    id: 'audit-1',
    action: 'COURSE_DELETE',
    resourceType: 'course',
    resourceId: 'COURSE-20260327-001',
    status: 'SUCCESS',
    message: '管理员删除课程成功',
    operatorId: 'admin-1001',
    operatorIdentifier: 'A0001',
    operatorName: '系统管理员',
    operatorRole: 'admin',
    requestIp: '127.0.0.1',
    requestMethod: 'POST',
    requestPath: '/api/admin/course/delete',
    requestSource: JSON.stringify({
      ip: '127.0.0.1',
      host: 'localhost:8080',
      referer: 'http://localhost:5173/admin/audit-logs',
      userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome'
    }),
    beforeSnapshot: JSON.stringify({
      course: {
        id: 'COURSE-20260327-001',
        title: '软件工程基础',
        teacherId: 'TEACHER-3001',
        status: 'published'
      },
      chapterIds: ['CH-1', 'CH-2', 'CH-3'],
      chapterCount: 3
    }),
    afterSnapshot: JSON.stringify({
      success: true,
      deletedSections: 12,
      deletedChapters: 3,
      deletedMaterials: 8,
      deletedUserCourses: 132,
      deletedCourse: true
    }),
    createdAt: '2026-03-27T09:15:22'
  },
  {
    id: 'audit-2',
    action: 'USER_PASSWORD_RESET',
    resourceType: 'user',
    resourceId: 'USER-2009',
    status: 'SUCCESS',
    message: '管理员重置用户密码成功',
    operatorId: 'admin-1002',
    operatorIdentifier: 'A0002',
    operatorName: '安全审计员',
    operatorRole: 'admin',
    requestIp: '10.10.2.14',
    requestMethod: 'POST',
    requestPath: '/api/admin/user/password',
    requestSource: JSON.stringify({
      ip: '10.10.2.14',
      host: 'ops.online-education.local',
      referer: 'http://localhost:5173/admin/users',
      userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Safari'
    }),
    beforeSnapshot: JSON.stringify({
      id: 'USER-2009',
      identifier: '20231234',
      nickname: '张同学',
      role: 'student',
      passwordSet: true
    }),
    afterSnapshot: JSON.stringify({
      id: 'USER-2009',
      role: 'student',
      passwordSet: true,
      passwordReset: true,
      updatedAt: '2026-03-27T10:02:11'
    }),
    createdAt: '2026-03-27T10:02:11'
  },
  {
    id: 'audit-3',
    action: 'QUESTION_VECTOR_SYNC_TRIGGER',
    resourceType: 'question_vector_sync',
    resourceId: 'TASK-9db123',
    status: 'STARTED',
    message: '管理员触发题目全量同步',
    operatorId: 'admin-1001',
    operatorIdentifier: 'A0001',
    operatorName: '系统管理员',
    operatorRole: 'admin',
    requestIp: '172.18.0.1',
    requestMethod: 'POST',
    requestPath: '/api/admin/sync/start-all',
    requestSource: JSON.stringify({
      ip: '172.18.0.1',
      host: 'localhost:8080',
      referer: 'http://localhost:5173/admin/audit-logs',
      userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Edge'
    }),
    beforeSnapshot: JSON.stringify({
      questionCount: 5280,
      syncScope: 'ALL_QUESTIONS'
    }),
    afterSnapshot: JSON.stringify({
      taskId: 'TASK-9db123',
      dispatchMode: 'ASYNC',
      status: 'STARTED'
    }),
    createdAt: '2026-03-27T10:20:49'
  },
  {
    id: 'audit-4',
    action: 'COURSE_DELETE',
    resourceType: 'course',
    resourceId: 'COURSE-20260327-022',
    status: 'FAILED',
    message: '管理员删除课程存在部分失败',
    operatorId: 'admin-1003',
    operatorIdentifier: 'A0003',
    operatorName: '运维管理员',
    operatorRole: 'admin',
    requestIp: '10.0.5.22',
    requestMethod: 'POST',
    requestPath: '/api/admin/course/delete',
    requestSource: JSON.stringify({
      ip: '10.0.5.22',
      host: 'ops.online-education.local',
      referer: 'http://localhost:5173/admin/audit-logs',
      userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome Mobile'
    }),
    beforeSnapshot: JSON.stringify({
      course: {
        id: 'COURSE-20260327-022',
        title: '离散数学',
        teacherId: 'TEACHER-0190',
        status: 'published'
      },
      chapterIds: ['CH-11', 'CH-12'],
      chapterCount: 2
    }),
    afterSnapshot: JSON.stringify({
      success: false,
      deletedSections: 7,
      deletedChapters: 1,
      deletedMaterials: 0,
      deletedUserCourses: 53,
      deletedCourse: false
    }),
    createdAt: '2026-03-27T11:05:30'
  },
  {
    id: 'audit-5',
    action: 'USER_PASSWORD_RESET',
    resourceType: 'user',
    resourceId: 'USER-4005',
    status: 'FAILED',
    message: '管理员重置用户密码失败',
    operatorId: 'admin-1002',
    operatorIdentifier: 'A0002',
    operatorName: '安全审计员',
    operatorRole: 'admin',
    requestIp: '192.168.31.8',
    requestMethod: 'POST',
    requestPath: '/api/admin/user/password',
    requestSource: JSON.stringify({
      ip: '192.168.31.8',
      host: 'localhost:8080',
      referer: 'http://localhost:5173/admin/users',
      userAgent: 'Mozilla/5.0 (X11; Linux x86_64) Firefox'
    }),
    beforeSnapshot: JSON.stringify({
      id: 'USER-4005',
      identifier: 'T9988',
      nickname: '李老师',
      role: 'teacher',
      passwordSet: true
    }),
    afterSnapshot: JSON.stringify({
      id: 'USER-4005',
      role: 'teacher',
      passwordSet: true,
      passwordReset: false,
      updatedAt: '2026-03-27T12:10:21'
    }),
    createdAt: '2026-03-27T12:10:21'
  }
]).map(normalizeLog)

/**
 * 根据筛选项构建审计日志查询参数。
 *
 * @returns {Record<string, string>} 审计日志查询参数
 */
const buildAuditParams = () => {
  const [startDate, endDate] = filters.value.dateRange || []
  return {
    keyword: filters.value.keyword,
    action: filters.value.action,
    status: filters.value.status,
    operatorRole: filters.value.operatorRole,
    startDate,
    endDate
  }
}

const loadAuditLogs = async () => {
  loading.value = true
  try {
    const response = await getAdminAuditLogs(buildAuditParams())
    logs.value = normalizePayload(response).map(normalizeLog)
    usingMockData.value = false
  } catch (error) {
    logs.value = mockAuditLogs()
    usingMockData.value = true
    // ElMessage.warning('审计日志接口暂不可用，已切换为演示数据')
  } finally {
    loading.value = false
    animateList()
  }
}

const actionLabel = (action) => {
  const labels = {
    COURSE_DELETE: '删除课程',
    USER_PASSWORD_RESET: '密码重置',
    QUESTION_VECTOR_SYNC_TRIGGER: '同步触发',
    UNKNOWN: '未知动作'
  }
  return labels[action] || action
}

const resourceLabel = (resourceType) => {
  const labels = {
    course: '课程资源',
    user: '用户账户',
    question_vector_sync: '向量同步任务',
    unknown: '未知资源'
  }
  return labels[resourceType] || resourceType
}

const statusLabel = (status) => {
  const labels = {
    SUCCESS: '成功',
    FAILED: '失败',
    STARTED: '已启动',
    UNKNOWN: '未知'
  }
  return labels[status] || status
}

const roleLabel = (role) => {
  const labels = {
    admin: '管理员',
    teacher: '教师',
    student: '学生',
    system: '系统任务'
  }
  return labels[role] || '未知角色'
}

const statusTagType = (status) => {
  const types = { SUCCESS: 'success', FAILED: 'danger', STARTED: 'warning' }
  return types[status] || 'info'
}

const roleTagType = (role) => {
  const types = { admin: 'danger', teacher: 'success', student: 'info', system: 'warning' }
  return types[role] || 'info'
}

const actionIcon = (action) => {
  const icons = {
    COURSE_DELETE: ['fas', 'book-open'],
    USER_PASSWORD_RESET: ['fas', 'key'],
    QUESTION_VECTOR_SYNC_TRIGGER: ['fas', 'arrows-rotate']
  }
  return icons[action] || ['fas', 'circle-info']
}

const actionToneClass = (action) => {
  const tones = {
    COURSE_DELETE: 'tone-danger',
    USER_PASSWORD_RESET: 'tone-warning',
    QUESTION_VECTOR_SYNC_TRIGGER: 'tone-info'
  }
  return tones[action] || 'tone-neutral'
}

const sourceLabel = (log) => {
  const userAgent = (log.requestSourceObject.userAgent || '').toLowerCase()
  if (userAgent.includes('mobile')) return '移动端浏览器'
  if (userAgent.includes('chrome') || userAgent.includes('edge') || userAgent.includes('firefox') || userAgent.includes('safari')) return 'Web 管理后台'
  if (!userAgent) return '系统任务'
  return '未知来源'
}

const filteredLogs = computed(() => {
  const keyword = filters.value.keyword.trim().toLowerCase()
  const [startDate, endDate] = filters.value.dateRange || []
  return [...logs.value]
    .filter(log => !filters.value.action || log.action === filters.value.action)
    .filter(log => !filters.value.status || log.status === filters.value.status)
    .filter(log => !filters.value.operatorRole || log.operatorRole === filters.value.operatorRole)
    .filter(log => {
      if (!keyword) return true
      const corpus = [
        log.resourceId, log.operatorName, log.operatorIdentifier,
        log.requestPath, log.requestIp, log.message,
        actionLabel(log.action), resourceLabel(log.resourceType)
      ].filter(Boolean).join(' ').toLowerCase()
      return corpus.includes(keyword)
    })
    .filter(log => {
      if (!startDate || !endDate) return true
      const current = new Date(log.createdAt).getTime()
      const start = new Date(`${startDate}T00:00:00`).getTime()
      const end = new Date(`${endDate}T23:59:59`).getTime()
      return current >= start && current <= end
    })
    .sort((left, right) => new Date(right.createdAt) - new Date(left.createdAt))
})

const paginatedLogs = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredLogs.value.slice(start, start + pageSize.value)
})

const stats = computed(() => ({
  total: logs.value.length,
  failed: logs.value.filter(item => item.status === 'FAILED').length,
  courseDelete: logs.value.filter(item => item.action === 'COURSE_DELETE').length,
  passwordReset: logs.value.filter(item => item.action === 'USER_PASSWORD_RESET').length,
  syncTrigger: logs.value.filter(item => item.action === 'QUESTION_VECTOR_SYNC_TRIGGER').length
}))

const topRequestSource = computed(() => {
  if (!filteredLogs.value.length) return '暂无来源统计'
  const countMap = filteredLogs.value.reduce((map, item) => {
    const key = sourceLabel(item)
    map[key] = (map[key] || 0) + 1
    return map
  }, {})
  return Object.entries(countMap).sort((a, b) => b[1] - a[1])[0][0]
})

const topOperator = computed(() => {
  if (!filteredLogs.value.length) return '暂无操作者'
  const countMap = filteredLogs.value.reduce((map, item) => {
    const key = item.operatorName || '系统任务'
    map[key] = (map[key] || 0) + 1
    return map
  }, {})
  return Object.entries(countMap).sort((a, b) => b[1] - a[1])[0][0]
})

const openDetail = (log) => {
  selectedLog.value = log
  detailVisible.value = true
}

const onDrawerOpen = () => {
  nextTick(() => {
    gsap.fromTo('.glass-drawer', 
      { opacity: 0, x: 50 },
      { opacity: 1, x: 0, duration: 0.4, ease: 'power2.out' }
    )
    gsap.fromTo('.detail-card',
      { opacity: 0, y: 20 },
      { opacity: 1, y: 0, duration: 0.4, stagger: 0.1, delay: 0.2, ease: 'back.out(1.2)' }
    )
  })
}

const resetFilters = () => {
  filters.value = { keyword: '', action: '', status: '', operatorRole: '', dateRange: [] }
  currentPage.value = 1
  loadAuditLogs()
  animateList()
}

const applyFilters = async () => {
  currentPage.value = 1
  await loadAuditLogs()
}

const handlePageChange = () => {
  animateList()
}

const exportCurrentView = () => {
  const content = JSON.stringify(
    filteredLogs.value.map(item => ({
      id: item.id, action: item.action, actionLabel: actionLabel(item.action),
      resourceType: item.resourceType, resourceId: item.resourceId,
      status: item.status, operatorName: item.operatorName,
      operatorIdentifier: item.operatorIdentifier, requestIp: item.requestIp,
      requestMethod: item.requestMethod, requestPath: item.requestPath,
      createdAt: item.createdAt
    })), null, 2
  )
  const blob = new Blob([content], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `audit-logs-${new Date().getTime()}.json`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('当前视图已导出')
}

// 动画逻辑
const animateList = () => {
  nextTick(() => {
    if (viewMode.value === 'grid' && listGridRef.value) {
      gsap.fromTo(listGridRef.value.querySelectorAll('.log-card'), 
        { y: 30, opacity: 0 },
        { y: 0, opacity: 1, duration: 0.4, stagger: 0.05, ease: "power2.out", overwrite: true }
      )
    } else if (viewMode.value === 'table' && listTableRef.value) {
      gsap.fromTo(listTableRef.value.querySelector('.el-table__body-wrapper'), 
        { opacity: 0 },
        { opacity: 1, duration: 0.5, ease: "power2.out", overwrite: true }
      )
    }
  })
}

watch(viewMode, () => {
  animateList()
})

onMounted(() => {
  tl = gsap.timeline()
  
  tl.from(headerRef.value, { y: -20, opacity: 0, duration: 0.5, ease: "power2.out" })
  
  if (statsRef.value) {
    tl.from(statsRef.value.children, {
      y: 20, opacity: 0, duration: 0.4, stagger: 0.1, ease: "back.out(1.2)"
    }, "-=0.2")
  }
  
  if (filterRef.value) {
    tl.from(filterRef.value, { y: 20, opacity: 0, duration: 0.4, ease: "power2.out" }, "-=0.2")
  }
  
  if (listHeaderRef.value) {
    tl.from(listHeaderRef.value, { y: 20, opacity: 0, duration: 0.4, ease: "power2.out" }, "-=0.2")
  }

  loadAuditLogs()
})

onUnmounted(() => {
  if (tl) tl.kill()
})
</script>

<style scoped>
.audit-logs-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  color: #334155;
  font-family: 'Inter', 'PingFang SC', sans-serif;
  padding-bottom: 24px;
}

/* 全局 Glassmorphism 卡片样式 */
.glass-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}

.hover-float {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.hover-float:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 97, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

/* 渐变色定义 */
.text-gradient-blue { background: linear-gradient(135deg, #0061ff 0%, #60efff 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.text-gradient-green { background: linear-gradient(135deg, #10b981 0%, #34d399 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.text-gradient-orange { background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.text-gradient-purple { background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.text-gradient-danger { background: linear-gradient(135deg, #ef4444 0%, #f87171 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.text-gradient-warning { background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }

.gradient-blue { background: linear-gradient(135deg, #0061ff 0%, #60efff 100%); }
.gradient-green { background: linear-gradient(135deg, #10b981 0%, #34d399 100%); }
.gradient-orange { background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%); }
.gradient-purple { background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%); }
.gradient-danger { background: linear-gradient(135deg, #ef4444 0%, #f87171 100%); }
.gradient-warning { background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%); }

/* 页面头部 */
.page-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 30px;
}

.hero-copy {
  max-width: 800px;
}

.hero-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.hero-badge, .hero-status, .summary-pill, .drawer-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
  display: inline-block;
  animation: pulse-glow 2s infinite;
}
.pulse-dot.success { background: #10b981; box-shadow: 0 0 8px #10b981; }
.pulse-dot.warning { background: #f59e0b; box-shadow: 0 0 8px #f59e0b; }

@keyframes pulse-glow {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.page-title {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 12px 0;
}

.icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: rgba(0, 97, 255, 0.1);
  color: #0061ff;
  font-size: 22px;
}

.page-subtitle {
  color: #64748b;
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 20px;
}

.hero-meta-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  font-weight: 500;
  font-size: 14px;
}

.hero-actions {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.action-btn {
  padding: 10px 20px;
  border-radius: 12px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  border: none;
}

.glass-btn {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.6);
  color: #475569;
  transition: all 0.3s ease;
  cursor: pointer;
}

.glass-btn:hover {
  background: rgba(255, 255, 255, 0.8);
  color: #0061ff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.tech-btn {
  background: linear-gradient(135deg, #0061ff 0%, #60efff 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(0, 97, 255, 0.3);
  transition: all 0.3s ease;
}

.tech-btn:hover {
  box-shadow: 0 6px 20px rgba(0, 97, 255, 0.4);
  transform: translateY(-2px);
}

.warning-alert {
  border: 1px solid rgba(245, 158, 11, 0.3);
  background: rgba(245, 158, 11, 0.1);
  color: #b45309;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
}

.stat-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  color: #fff;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label { color: #64748b; font-size: 13px; font-weight: 500; }
.stat-value { font-size: 28px; font-weight: 700; line-height: 1; }
.stat-hint { color: #94a3b8; font-size: 12px; margin-top: 4px; }

/* 筛选面板 */
.filter-panel { padding: 24px; }

.filter-panel-header, .content-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(0, 97, 255, 0.1);
  color: #0061ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.section-title h3 { font-size: 18px; font-weight: 600; color: #1e293b; margin: 0; }
.section-subtitle { margin: 4px 0 0 0; color: #64748b; font-size: 13px; }

.filter-actions { display: flex; gap: 12px; }

.filter-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.filter-item { display: flex; flex-direction: column; gap: 8px; }
.filter-label { color: #475569; font-weight: 600; font-size: 13px; }
.filter-item-wide { grid-column: span 2; }

/* 列表内容区域 */
.content-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.mr-md { margin-right: 16px; }

/* 卡片流布局 */
.logs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
}

.log-card {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  cursor: pointer;
}

.log-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 12px;
  border-bottom: 1px dashed rgba(0,0,0,0.1);
}

.log-action {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tech-icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: white;
}

.tech-icon-box.small {
  width: 36px;
  height: 36px;
  font-size: 14px;
  border-radius: 10px;
}

.tone-danger { background: linear-gradient(135deg, #ef4444, #f87171); box-shadow: 0 4px 10px rgba(239,68,68,0.2); }
.tone-warning { background: linear-gradient(135deg, #f59e0b, #fbbf24); box-shadow: 0 4px 10px rgba(245,158,11,0.2); }
.tone-info { background: linear-gradient(135deg, #0ea5e9, #38bdf8); box-shadow: 0 4px 10px rgba(14,165,233,0.2); }
.tone-neutral { background: linear-gradient(135deg, #64748b, #94a3b8); box-shadow: 0 4px 10px rgba(100,116,139,0.2); }

.action-info { display: flex; flex-direction: column; gap: 2px; }
.action-name { font-weight: 600; color: #1e293b; font-size: 15px; }
.action-time { color: #94a3b8; font-size: 12px; }

.tech-tag {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  width: fit-content;
}
.tech-tag.info { background: rgba(0, 97, 255, 0.1); color: #0061ff; }
.tech-tag.success { background: rgba(16, 185, 129, 0.1); color: #10b981; }
.tech-tag.danger { background: rgba(239, 68, 68, 0.1); color: #ef4444; }
.tech-tag.warning { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }

.log-card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-message {
  font-size: 14px;
  color: #334155;
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.log-details-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  background: rgba(255,255,255,0.4);
  padding: 12px;
  border-radius: 12px;
}

.detail-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow: hidden;
}

.detail-label { font-size: 11px; color: #94a3b8; }
.detail-value { font-size: 13px; font-weight: 600; color: #1e293b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.detail-sub { font-size: 11px; color: #64748b; font-family: 'Fira Code', monospace; }

.log-card-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(0,0,0,0.05);
}

.request-method {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 6px;
  font-family: 'Fira Code', monospace;
}
.request-method.get { background: rgba(16, 185, 129, 0.1); color: #10b981; }
.request-method.post { background: rgba(0, 97, 255, 0.1); color: #0061ff; }
.request-method.put { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
.request-method.delete { background: rgba(239, 68, 68, 0.1); color: #ef4444; }

.request-path {
  font-size: 12px;
  color: #64748b;
  font-family: 'Fira Code', monospace;
  flex: 1;
}

.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ml-auto { margin-left: auto; }
.ml-xs { margin-left: 4px; }

/* 表格视图 */
.table-shell { padding: 24px; }
.action-cell, .operator-cell, .resource-cell, .source-cell {
  display: flex; flex-direction: column; gap: 4px;
}
.action-cell { flex-direction: row; align-items: flex-start; gap: 12px; }
.action-copy { display: flex; flex-direction: column; gap: 4px; }
.text-muted { color: #64748b; }

.tech-text-btn { color: #64748b; transition: all 0.3s ease; }
.tech-text-btn:hover { color: #0061ff; background: rgba(0, 97, 255, 0.1); }

.mt-lg { margin-top: 24px; }
.pagination-section { padding: 16px 24px; display: flex; justify-content: center; }

/* 抽屉样式 */
.drawer-content { display: flex; flex-direction: column; gap: 24px; padding: 24px; }

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px;
}

.header-main { display: flex; gap: 20px; }
.title-row { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.title-row h2 { margin: 0; font-size: 20px; color: #1e293b; }
.meta-info { margin: 0; color: #64748b; font-size: 14px; display: flex; align-items: center; gap: 8px; }
.divider { color: #cbd5e1; }

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.detail-card { padding: 20px; }
.detail-card-wide { grid-column: span 2; }
.detail-card h3 { margin: 0 0 16px 0; font-size: 16px; display: flex; align-items: center; }

.tech-dl {
  display: grid;
  gap: 12px;
  margin: 0;
}
.tech-dl div {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px dashed rgba(0,0,0,0.05);
}
.tech-dl dt { color: #64748b; font-size: 14px; }
.tech-dl dd { margin: 0; font-weight: 600; color: #1e293b; }

.code-font { font-family: 'Fira Code', Consolas, monospace; }

.request-source-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.tech-inset {
  background: rgba(255,255,255,0.4);
  border: 1px solid rgba(255,255,255,0.6);
  border-radius: 12px;
  padding: 16px;
}
.tech-inset span { display: block; color: #64748b; font-size: 12px; margin-bottom: 8px; }
.tech-inset strong, .tech-inset p { margin: 0; color: #1e293b; font-size: 14px; }

.snapshot-section {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.snapshot-card {
  padding: 20px;
}

.snapshot-card.dark-mode {
  background: #0f172a;
  border-color: #1e293b;
}

.snapshot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.snapshot-header h3 { margin: 0; font-size: 15px; color: #f8fafc; display: flex; align-items: center; }

.code-container {
  background: #020617;
  border-radius: 8px;
  padding: 16px;
  overflow: auto;
  max-height: 400px;
}

.code-container pre { margin: 0; }
.code-container code {
  font-family: 'Fira Code', Consolas, monospace;
  font-size: 13px;
  color: #e2e8f0;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

/* 响应式设计 */
@media (max-width: 1280px) {
  .stats-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .filter-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 992px) {
  .page-hero, .filter-panel-header, .content-header { flex-direction: column; gap: 16px; }
  .hero-actions { width: 100%; justify-content: flex-start; }
  .stats-grid, .detail-grid, .snapshot-section, .request-source-grid { grid-template-columns: 1fr; }
  .detail-card-wide, .filter-item-wide { grid-column: span 1; }
  .drawer-header { flex-direction: column; }
}
</style>

<style>
/* 全局弹窗样式，解决 append-to-body 后的样式丢失问题 */
.glass-drawer {
  background: rgba(255, 255, 255, 0.85) !important;
  backdrop-filter: blur(20px) !important;
  -webkit-backdrop-filter: blur(20px) !important;
  border-left: 1px solid rgba(255, 255, 255, 0.4) !important;
  box-shadow: -10px 0 40px rgba(0, 0, 0, 0.1) !important;
}
.drawer-content {
  padding: 24px;
  height: 100%;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.drawer-header {
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}
.header-main { display: flex; gap: 16px; }
.header-text { display: flex; flex-direction: column; gap: 8px; }
.title-row { display: flex; align-items: center; gap: 12px; }
.title-row h2 { margin: 0; font-size: 20px; font-weight: 700; color: #1e293b; }
.meta-info { margin: 0; font-size: 13px; color: #64748b; display: flex; align-items: center; gap: 8px; }
.divider { color: #cbd5e1; }
.request-method { padding: 2px 6px; border-radius: 4px; font-size: 11px; font-weight: 700; font-family: monospace; }
.request-method.get { background: rgba(16, 185, 129, 0.1); color: #10b981; }
.request-method.post { background: rgba(0, 97, 255, 0.1); color: #0061ff; }
.request-method.put { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
.request-method.delete { background: rgba(239, 68, 68, 0.1); color: #ef4444; }
.detail-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
.detail-card { padding: 20px; border-radius: 16px; }
.detail-card-wide { grid-column: 1 / -1; }
.detail-card h3 { margin: 0 0 16px 0; font-size: 15px; font-weight: 600; color: #1e293b; display: flex; align-items: center; }
.tech-dl { margin: 0; display: flex; flex-direction: column; gap: 12px; }
.tech-dl div { display: flex; justify-content: space-between; align-items: center; padding-bottom: 8px; border-bottom: 1px dashed rgba(0, 0, 0, 0.05); }
.tech-dl div:last-child { border-bottom: none; padding-bottom: 0; }
.tech-dl dt { color: #64748b; font-size: 13px; }
.tech-dl dd { margin: 0; font-weight: 500; color: #334155; font-size: 14px; }
.request-source-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.tech-inset { background: rgba(255, 255, 255, 0.4); border: 1px solid rgba(255, 255, 255, 0.6); border-radius: 12px; padding: 12px; display: flex; flex-direction: column; gap: 6px; box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.02); }
.tech-inset span { font-size: 12px; color: #64748b; }
.tech-inset strong { font-size: 14px; color: #1e293b; }
.source-user-agent p { margin: 0; font-size: 12px; line-height: 1.5; word-break: break-all; }
.snapshot-section { display: flex; flex-direction: column; gap: 20px; }
.snapshot-card { border-radius: 16px; overflow: hidden; }
.snapshot-card.dark-mode { background: #0f172a; border: 1px solid #1e293b; }
.snapshot-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid rgba(255, 255, 255, 0.1); background: rgba(255, 255, 255, 0.02); }
.snapshot-header h3 { margin: 0; color: #f8fafc; font-size: 14px; font-weight: 600; }
.code-container { padding: 20px; overflow-x: auto; }
.code-container pre { margin: 0; }
.code-container code { font-family: 'Fira Code', 'Consolas', monospace; font-size: 13px; color: #e2e8f0; line-height: 1.6; }
</style>
