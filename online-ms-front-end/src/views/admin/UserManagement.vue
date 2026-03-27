<template>
  <div class="user-management-container">
    <!-- 页面头部 -->
    <div class="page-header glass-card" ref="headerRef">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <div class="icon-wrapper">
              <font-awesome-icon :icon="['fas', 'users']" />
            </div>
            用户管理
          </h1>
          <p class="page-subtitle">管理系统中的所有用户账户</p>
        </div>
        <div class="header-actions">
          <el-button class="action-btn glass-btn">
            <font-awesome-icon :icon="['fas', 'upload']" />
            批量导入
          </el-button>
          <el-button class="action-btn glass-btn" @click="exportCurrentView">
            <font-awesome-icon :icon="['fas', 'download']" />
            导出数据
          </el-button>
          <el-button type="primary" class="action-btn tech-btn" @click="openCreateDialog">
            <font-awesome-icon :icon="['fas', 'plus']" />
            添加用户
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid" ref="statsRef">
        <div class="stat-card glass-card hover-float total">
          <div class="stat-icon gradient-blue">
            <font-awesome-icon :icon="['fas', 'users']" />
          </div>
          <div class="stat-content">
            <div class="stat-number text-gradient-blue">{{ stats.total }}</div>
            <div class="stat-label">总用户数</div>
            <div class="stat-change positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              +{{ stats.totalGrowth }}%
            </div>
          </div>
        </div>
        
        <div class="stat-card glass-card hover-float students">
          <div class="stat-icon gradient-green">
            <font-awesome-icon :icon="['fas', 'graduation-cap']" />
          </div>
          <div class="stat-content">
            <div class="stat-number text-gradient-green">{{ stats.students }}</div>
            <div class="stat-label">学生用户</div>
            <div class="stat-change positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              +{{ stats.studentsGrowth }}%
            </div>
          </div>
        </div>
        
        <div class="stat-card glass-card hover-float teachers">
          <div class="stat-icon gradient-orange">
            <font-awesome-icon :icon="['fas', 'chalkboard-teacher']" />
          </div>
          <div class="stat-content">
            <div class="stat-number text-gradient-orange">{{ stats.teachers }}</div>
            <div class="stat-label">教师用户</div>
            <div class="stat-change positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              +{{ stats.teachersGrowth }}%
            </div>
          </div>
        </div>
        
        <div class="stat-card glass-card hover-float active">
          <div class="stat-icon gradient-purple">
            <font-awesome-icon :icon="['fas', 'user-check']" />
          </div>
          <div class="stat-content">
            <div class="stat-number text-gradient-purple">{{ stats.active }}</div>
            <div class="stat-label">活跃用户</div>
            <div class="stat-change positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              +{{ stats.activeGrowth }}%
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <div class="filter-content glass-card" ref="filterRef">
        <div class="filter-left">
          <div class="filter-group">
            <label class="filter-label">用户角色</label>
            <el-select v-model="filters.role" placeholder="全部角色" clearable class="tech-select">
              <el-option label="全部角色" value=""></el-option>
              <el-option label="学生" value="student"></el-option>
              <el-option label="教师" value="teacher"></el-option>
              <el-option label="管理员" value="admin"></el-option>
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">账户状态</label>
            <el-select v-model="filters.status" placeholder="全部状态" clearable class="tech-select">
              <el-option label="全部状态" value=""></el-option>
              <el-option label="正常" value="active"></el-option>
              <el-option label="禁用" value="disabled"></el-option>
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">注册时间</label>
            <el-date-picker
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
          
          <div class="filter-group">
            <label class="filter-label">排序方式</label>
            <el-select v-model="filters.sortBy" placeholder="排序方式" class="tech-select">
              <el-option label="注册时间" value="created_at"></el-option>
              <el-option label="最后登录" value="last_login"></el-option>
              <el-option label="用户名" value="username"></el-option>
              <el-option label="邮箱" value="email"></el-option>
            </el-select>
          </div>
        </div>
        
        <div class="filter-right">
          <div class="search-box">
            <el-input
              v-model="filters.search"
              placeholder="搜索用户名、邮箱或手机号"
              class="search-input tech-input"
            >
              <template #prefix>
                <font-awesome-icon :icon="['fas', 'search']" />
              </template>
            </el-input>
          </div>
          
          <div class="filter-actions">
            <el-button class="glass-btn" @click="resetFilters">
              <font-awesome-icon :icon="['fas', 'undo']" />
              重置
            </el-button>
            <el-button type="primary" class="tech-btn" @click="applyFilters">
              <font-awesome-icon :icon="['fas', 'filter']" />
              筛选
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="users-section">
      <div class="section-header" ref="listHeaderRef">
        <div class="section-title">
          <div class="title-icon"><font-awesome-icon :icon="['fas', 'list']" /></div>
          <h3>用户列表</h3>
          <span class="user-count pulse-badge">共 {{ filteredUsers.length }} 个用户</span>
        </div>
        
        <div class="section-actions">
          <div class="view-toggle">
            <el-radio-group v-model="viewMode" size="small" class="tech-radio">
              <el-radio-button label="table">
                <font-awesome-icon :icon="['fas', 'list']" />
                列表
              </el-radio-button>
              <el-radio-button label="grid">
                <font-awesome-icon :icon="['fas', 'th-large']" />
                卡片
              </el-radio-button>
            </el-radio-group>
          </div>
          
          <div class="batch-actions" v-if="selectedUsers.length > 0">
            <el-button size="small" class="tech-small-btn success" @click="batchEnable">
              <font-awesome-icon :icon="['fas', 'check']" />
              批量启用
            </el-button>
            <el-button size="small" class="tech-small-btn warning" @click="batchDisable">
              <font-awesome-icon :icon="['fas', 'ban']" />
              批量禁用
            </el-button>
            <el-button size="small" class="tech-small-btn danger" @click="batchDelete">
              <font-awesome-icon :icon="['fas', 'trash']" />
              批量删除
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 卡片视图 (全局默认使用) -->
      <div v-if="viewMode === 'grid'" class="grid-view" ref="listGridRef">
        <div class="users-grid">
          <div 
            v-for="user in paginatedUsers" 
            :key="user.id"
            class="user-card glass-card hover-float"
            :class="{ selected: selectedUsers.includes(user.id) }"
            @click="toggleUserSelection(user.id)"
          >
            <div class="card-header">
              <div class="user-avatar tech-avatar">
                <img :src="user.avatar" :alt="user.username" />
              </div>
              <div class="user-basic">
                <div class="user-name">{{ user.username }}</div>
                <div class="user-email">{{ user.email }}</div>
              </div>
              <div class="card-actions">
                <el-dropdown trigger="click" @click.stop>
                  <el-button size="small" circle class="glass-btn">
                    <font-awesome-icon :icon="['fas', 'ellipsis-v']" />
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu class="tech-dropdown">
                      <el-dropdown-item @click="viewUser(user)">
                        <font-awesome-icon :icon="['fas', 'eye']" /> 查看详情
                      </el-dropdown-item>
                      <el-dropdown-item @click="editUser(user)">
                        <font-awesome-icon :icon="['fas', 'edit']" /> 编辑
                      </el-dropdown-item>
                      <el-dropdown-item @click="resetPassword(user)">
                        <font-awesome-icon :icon="['fas', 'key']" /> 重置密码
                      </el-dropdown-item>
                      <el-dropdown-item @click="toggleStatus(user)" divided>
                        <font-awesome-icon :icon="['fas', user.status === 'active' ? 'ban' : 'check']" />
                        {{ user.status === 'active' ? '禁用' : '启用' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="deleteUser(user)">
                        <font-awesome-icon :icon="['fas', 'trash']" class="text-danger" /> 删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            
            <div class="card-content">
              <div class="user-meta">
                <div class="meta-item">
                  <span class="meta-label">角色</span>
                  <span :class="['tech-tag', getRoleTagType(user.role)]">
                    {{ getRoleName(user.role) }}
                  </span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">状态</span>
                  <span :class="['tech-tag', getStatusTagType(user.status)]">
                    {{ getStatusName(user.status) }}
                  </span>
                </div>
              </div>
              
              <div class="user-stats">
                <div class="stat-item-small">
                  <span class="stat-value text-gradient-blue">{{ user.coursesCount || 0 }}</span>
                  <span class="stat-label">课程数</span>
                </div>
                <div class="stat-item-small">
                  <span class="stat-value text-gradient-green">{{ user.loginCount || 0 }}</span>
                  <span class="stat-label">登录次数</span>
                </div>
              </div>
              
              <div class="user-dates">
                <div class="date-item">
                  <font-awesome-icon :icon="['fas', 'calendar-plus']" />
                  <span>注册：{{ formatDate(user.createdAt) }}</span>
                </div>
                <div class="date-item">
                  <font-awesome-icon :icon="['fas', 'clock']" />
                  <span>登录：{{ formatDate(user.lastLogin) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 表格视图 -->
      <div v-else class="table-view glass-card" ref="listTableRef">
        <el-table
          :data="paginatedUsers"
          @selection-change="handleSelectionChange"
          class="users-table tech-table"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          
          <el-table-column label="用户信息" min-width="220">
            <template #default="{ row }">
              <div class="user-info">
                <div class="user-avatar tech-avatar small">
                  <img :src="row.avatar" :alt="row.username" />
                </div>
                <div class="user-details">
                  <div class="user-name">{{ row.username }}</div>
                  <div class="user-email">{{ row.email }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="角色" width="100">
            <template #default="{ row }">
              <span :class="['tech-tag', getRoleTagType(row.role)]">
                {{ getRoleName(row.role) }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span :class="['tech-tag', getStatusTagType(row.status)]">
                {{ getStatusName(row.status) }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column label="注册时间" width="120">
            <template #default="{ row }">
              <span class="text-muted">{{ formatDate(row.createdAt) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="最后登录" width="120">
            <template #default="{ row }">
              <span class="text-muted">{{ formatDate(row.lastLogin) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button size="small" text class="tech-text-btn" @click="viewUser(row)">
                  <font-awesome-icon :icon="['fas', 'eye']" />
                </el-button>
                <el-button size="small" text class="tech-text-btn" @click="editUser(row)">
                  <font-awesome-icon :icon="['fas', 'edit']" />
                </el-button>
                <el-dropdown trigger="click">
                  <el-button size="small" text class="tech-text-btn">
                    <font-awesome-icon :icon="['fas', 'ellipsis-h']" />
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu class="tech-dropdown">
                      <el-dropdown-item @click="resetPassword(row)">
                        <font-awesome-icon :icon="['fas', 'key']" /> 重置密码
                      </el-dropdown-item>
                      <el-dropdown-item @click="toggleStatus(row)">
                        <font-awesome-icon :icon="['fas', row.status === 'active' ? 'ban' : 'check']" />
                        {{ row.status === 'active' ? '禁用' : '启用' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="deleteUser(row)" divided>
                        <font-awesome-icon :icon="['fas', 'trash']" class="text-danger" /> 删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-section glass-card">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredUsers.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="tech-pagination"
        />
      </div>
    </div>

    <!-- 创建用户对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      width="600px"
      class="tech-dialog glass-dialog"
      :show-close="false"
      append-to-body
      @open="onDialogOpen"
    >
      <template #header="{ close }">
        <div class="dialog-header">
          <div class="title-icon"><font-awesome-icon :icon="['fas', editingUserId ? 'user-pen' : 'user-plus']" /></div>
          <span>{{ editingUserId ? '编辑用户' : '添加用户' }}</span>
          <button class="close-btn" @click="close">
            <font-awesome-icon :icon="['fas', 'times']" />
          </button>
        </div>
      </template>

      <el-form :model="userForm" label-width="100px" class="create-form">
        <el-form-item label="学号/工号">
          <el-input v-model="userForm.identifier" placeholder="请输入学号或工号" class="tech-input" />
        </el-form-item>

        <el-form-item label="用户名">
          <el-input v-model="userForm.username" placeholder="请输入用户名" class="tech-input" />
        </el-form-item>
        
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" placeholder="请输入邮箱地址" class="tech-input" />
        </el-form-item>
        
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" class="tech-input" />
        </el-form-item>
        
        <el-form-item label="角色">
          <el-select v-model="userForm.role" placeholder="请选择角色" class="tech-select w-full">
            <el-option label="学生" value="student"></el-option>
            <el-option label="教师" value="teacher"></el-option>
            <el-option label="管理员" value="admin"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="!editingUserId" label="初始密码">
          <el-input v-model="userForm.password" type="password" placeholder="请输入初始密码" class="tech-input" />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-radio-group v-model="userForm.status" class="tech-radio">
            <el-radio label="active">正常</el-radio>
            <el-radio label="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button class="glass-btn" @click="closeUserDialog">取消</el-button>
          <el-button type="primary" class="tech-btn" @click="submitUserForm">
            {{ editingUserId ? '保存修改' : '确定创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-drawer
      v-model="showDetailDrawer"
      size="420px"
      :with-header="false"
      destroy-on-close
      append-to-body
      class="glass-drawer"
      @open="onDrawerOpen"
    >
      <div v-if="detailUser" class="user-detail-drawer">
        <div class="dialog-header">
          <div class="title-icon"><font-awesome-icon :icon="['fas', 'address-card']" /></div>
          <span>用户详情</span>
        </div>
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">用户名称</span>
            <strong>{{ detailUser.nickname || '-' }}</strong>
          </div>
          <div class="detail-item">
            <span class="detail-label">学号/工号</span>
            <strong>{{ detailUser.identifier || '-' }}</strong>
          </div>
          <div class="detail-item">
            <span class="detail-label">邮箱</span>
            <strong>{{ detailUser.email || '-' }}</strong>
          </div>
          <div class="detail-item">
            <span class="detail-label">手机号</span>
            <strong>{{ detailUser.phone || '-' }}</strong>
          </div>
          <div class="detail-item">
            <span class="detail-label">角色</span>
            <strong>{{ getRoleName(detailUser.role) }}</strong>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态</span>
            <strong>{{ getStatusName(detailUser.status) }}</strong>
          </div>
          <div class="detail-item">
            <span class="detail-label">创建时间</span>
            <strong>{{ formatDate(detailUser.createdAt) }}</strong>
          </div>
          <div class="detail-item">
            <span class="detail-label">更新时间</span>
            <strong>{{ formatDate(detailUser.lastLogin) }}</strong>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { gsap } from 'gsap'
import {
  createAdminUser,
  disableAdminUser,
  getAdminUserDetail,
  getAdminUsers,
  resetAdminUserPassword,
  restoreAdminUser,
  updateAdminUser
} from '@/api/admin/adminManagement'

const headerRef = ref(null)
const statsRef = ref(null)
const filterRef = ref(null)
const listHeaderRef = ref(null)
const listGridRef = ref(null)
const listTableRef = ref(null)
let tl = null

const filters = ref({
  role: '',
  status: '',
  dateRange: null,
  sortBy: 'created_at',
  search: ''
})
const viewMode = ref('grid')
const currentPage = ref(1)
const pageSize = ref(12)
const selectedUsers = ref([])
const showCreateDialog = ref(false)
const showDetailDrawer = ref(false)
const editingUserId = ref('')
const detailUser = ref(null)
const users = ref([])

/**
 * 统一的用户维护表单。
 * 通过 editingUserId 判断当前是新增还是编辑。
 */
const userForm = ref({
  identifier: '',
  username: '',
  email: '',
  phone: '',
  role: 'student',
  password: '',
  status: 'active'
})

/**
 * 将后端用户结构转换为前端视图结构。
 *
 * @param {Record<string, any>} user 后端用户对象
 * @returns {Record<string, any>} 前端展示对象
 */
const normalizeUser = (user = {}) => ({
  id: user.id,
  identifier: user.identifier || '',
  username: user.nickname || '',
  email: user.email || '',
  phone: user.phone || '',
  avatar: user.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(user.nickname || 'U')}&background=002FA7&color=fff`,
  role: user.role || 'student',
  status: user.isDeleted === 1 ? 'disabled' : 'active',
  createdAt: user.createdAt || '',
  lastLogin: user.updatedAt || '',
  coursesCount: user.coursesCount || 0,
  loginCount: user.loginCount || 0
})

/**
 * 刷新管理员用户列表。
 */
const loadUsers = async () => {
  try {
    const response = await getAdminUsers()
    users.value = (Array.isArray(response) ? response : []).map(normalizeUser)
    await nextTick()
    animateList()
  } catch (error) {
    console.error('管理员用户列表加载失败:', error)
    ElMessage.error('用户列表加载失败，请稍后重试')
  }
}

const stats = computed(() => {
  const total = users.value.length
  const students = users.value.filter(user => user.role === 'student').length
  const teachers = users.value.filter(user => user.role === 'teacher').length
  const active = users.value.filter(user => user.status === 'active').length
  const ratio = (value) => (total ? Number(((value / total) * 100).toFixed(1)) : 0)
  return {
    total,
    totalGrowth: ratio(active),
    students,
    studentsGrowth: ratio(students),
    teachers,
    teachersGrowth: ratio(teachers),
    active,
    activeGrowth: ratio(active)
  }
})

const filteredUsers = computed(() => {
  let result = [...users.value]
  if (filters.value.role) result = result.filter(user => user.role === filters.value.role)
  if (filters.value.status) result = result.filter(user => user.status === filters.value.status)
  if (filters.value.search) {
    const search = filters.value.search.toLowerCase()
    result = result.filter(user =>
      user.username.toLowerCase().includes(search) ||
      user.email.toLowerCase().includes(search) ||
      user.identifier.toLowerCase().includes(search) ||
      (user.phone && user.phone.includes(search))
    )
  }
  result.sort((a, b) => {
    const field = filters.value.sortBy
    if (field === 'created_at') {
      return new Date(b.createdAt || 0) - new Date(a.createdAt || 0)
    }
    if (field === 'last_login') {
      return new Date(b.lastLogin || 0) - new Date(a.lastLogin || 0)
    }
    return (a[field] || '').localeCompare(b[field] || '')
  })
  return result
})

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUsers.value.slice(start, end)
})

const getRoleTagType = (role) => ({ student: 'info', teacher: 'success', admin: 'danger' }[role] || 'info')
const getRoleName = (role) => ({ student: '学生', teacher: '教师', admin: '管理员' }[role] || '未知')
const getStatusTagType = (status) => ({ active: 'success', disabled: 'danger' }[status] || 'info')
const getStatusName = (status) => ({ active: '正常', disabled: '禁用' }[status] || '未知')
const formatDate = (date) => (!date ? '从未' : new Date(date).toLocaleString('zh-CN'))

const resetFilters = () => {
  filters.value = { role: '', status: '', dateRange: null, sortBy: 'created_at', search: '' }
  currentPage.value = 1
}

const applyFilters = () => {
  currentPage.value = 1
  animateList()
}

const handleSelectionChange = (selection) => {
  selectedUsers.value = selection.map(user => user.id)
}

const toggleUserSelection = (userId) => {
  const index = selectedUsers.value.indexOf(userId)
  if (index > -1) selectedUsers.value.splice(index, 1)
  else selectedUsers.value.push(userId)
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  animateList()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  animateList()
}

const resetUserForm = () => {
  userForm.value = {
    identifier: '',
    username: '',
    email: '',
    phone: '',
    role: 'student',
    password: '',
    status: 'active'
  }
  editingUserId.value = ''
}

const openCreateDialog = () => {
  resetUserForm()
  showCreateDialog.value = true
}

const onDialogOpen = () => {
  nextTick(() => {
    gsap.fromTo('.glass-dialog', 
      { opacity: 0, scale: 0.95, y: 20 },
      { opacity: 1, scale: 1, y: 0, duration: 0.4, ease: 'back.out(1.2)' }
    )
  })
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

const closeUserDialog = () => {
  showCreateDialog.value = false
  resetUserForm()
}

/**
 * 构建后端需要的用户保存请求体。
 */
const buildUserPayload = () => ({
  identifier: userForm.value.identifier.trim(),
  nickname: userForm.value.username.trim(),
  email: userForm.value.email.trim(),
  phone: userForm.value.phone.trim(),
  role: userForm.value.role,
  password: userForm.value.password,
  enabled: userForm.value.status === 'active'
})

const submitUserForm = async () => {
  if (!userForm.value.identifier || !userForm.value.username || !userForm.value.email) {
    ElMessage.error('请填写必要信息')
    return
  }
  if (!editingUserId.value && !userForm.value.password) {
    ElMessage.error('新增用户时必须填写初始密码')
    return
  }
  try {
    if (editingUserId.value) {
      await updateAdminUser(editingUserId.value, buildUserPayload())
      ElMessage.success('用户更新成功')
    } else {
      await createAdminUser(buildUserPayload())
      ElMessage.success('用户创建成功')
    }
    closeUserDialog()
    await loadUsers()
  } catch (error) {
    console.error('用户保存失败:', error)
    ElMessage.error(error?.message || '用户保存失败')
  }
}

const viewUser = async (user) => {
  try {
    const response = await getAdminUserDetail(user.id)
    detailUser.value = normalizeUser(response)
    showDetailDrawer.value = true
  } catch (error) {
    console.error('用户详情加载失败:', error)
    ElMessage.error('用户详情加载失败')
  }
}

const editUser = async (user) => {
  try {
    const response = await getAdminUserDetail(user.id)
    const detail = normalizeUser(response)
    editingUserId.value = detail.id
    userForm.value = {
      identifier: detail.identifier,
      username: detail.username,
      email: detail.email,
      phone: detail.phone,
      role: detail.role,
      password: '',
      status: detail.status
    }
    showCreateDialog.value = true
  } catch (error) {
    console.error('编辑用户前获取详情失败:', error)
    ElMessage.error('用户详情加载失败')
  }
}

const resetPassword = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要重置用户 ${user.username} 的密码吗？`, '重置密码', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const newPassword = await resetAdminUserPassword(user.id)
    ElMessage.success(`密码重置成功，新密码：${newPassword}`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('密码重置失败:', error)
      ElMessage.error('密码重置失败')
    }
  }
}

const toggleStatus = async (user) => {
  const action = user.status === 'active' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户 ${user.username} 吗？`, `${action}用户`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    if (user.status === 'active') {
      await disableAdminUser(user.id)
    } else {
      await restoreAdminUser(user.id)
    }
    ElMessage.success(`用户${action}成功`)
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('用户状态变更失败:', error)
      ElMessage.error('用户状态变更失败')
    }
  }
}

const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${user.username} 吗？此操作会将用户置为禁用状态。`, '删除用户', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    await disableAdminUser(user.id)
    ElMessage.success('用户删除成功')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('用户删除失败:', error)
      ElMessage.error('用户删除失败')
    }
  }
}

const batchEnable = async () => {
  try {
    await Promise.all(selectedUsers.value.map(userId => restoreAdminUser(userId)))
    selectedUsers.value = []
    ElMessage.success('批量启用成功')
    await loadUsers()
  } catch (error) {
    console.error('批量启用失败:', error)
    ElMessage.error('批量启用失败')
  }
}

const batchDisable = async () => {
  try {
    await Promise.all(selectedUsers.value.map(userId => disableAdminUser(userId)))
    selectedUsers.value = []
    ElMessage.success('批量禁用成功')
    await loadUsers()
  } catch (error) {
    console.error('批量禁用失败:', error)
    ElMessage.error('批量禁用失败')
  }
}

const batchDelete = async () => {
  try {
    await Promise.all(selectedUsers.value.map(userId => disableAdminUser(userId)))
    selectedUsers.value = []
    ElMessage.success('批量删除成功')
    await loadUsers()
  } catch (error) {
    console.error('批量删除失败:', error)
    ElMessage.error('批量删除失败')
  }
}

const exportCurrentView = () => {
  const blob = new Blob([JSON.stringify(filteredUsers.value, null, 2)], { type: 'application/json;charset=utf-8' })
  const objectUrl = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = objectUrl
  anchor.download = `admin-users-${Date.now()}.json`
  anchor.click()
  URL.revokeObjectURL(objectUrl)
  ElMessage.success('当前用户视图已导出')
}

const animateList = () => {
  nextTick(() => {
    if (viewMode.value === 'grid' && listGridRef.value) {
      gsap.fromTo(listGridRef.value.querySelectorAll('.user-card'),
        { y: 30, opacity: 0 },
        { y: 0, opacity: 1, duration: 0.4, stagger: 0.05, ease: 'power2.out', overwrite: true }
      )
    } else if (viewMode.value === 'table' && listTableRef.value) {
      const body = listTableRef.value.querySelector('.el-table__body-wrapper')
      if (body) {
        gsap.fromTo(body, { opacity: 0 }, { opacity: 1, duration: 0.5, ease: 'power2.out', overwrite: true })
      }
    }
  })
}

watch(viewMode, () => {
  animateList()
})

onMounted(async () => {
  await loadUsers()
  tl = gsap.timeline()
  tl.from(headerRef.value, { y: -20, opacity: 0, duration: 0.5, ease: 'power2.out' })
  if (statsRef.value) {
    tl.from(statsRef.value.children, {
      y: 20, opacity: 0, duration: 0.4, stagger: 0.1, ease: 'back.out(1.2)'
    }, '-=0.2')
  }
  if (filterRef.value) {
    tl.from(filterRef.value, { y: 20, opacity: 0, duration: 0.4, ease: 'power2.out' }, '-=0.2')
  }
  if (listHeaderRef.value) {
    tl.from(listHeaderRef.value, { y: 20, opacity: 0, duration: 0.4, ease: 'power2.out' }, '-=0.2')
  }
  animateList()
})

onUnmounted(() => {
  if (tl) tl.kill()
})
</script>

<style scoped>
.user-management-container {
  padding: 0;
  color: #334155;
  font-family: 'Inter', 'PingFang SC', sans-serif;
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

.gradient-blue { background: linear-gradient(135deg, #0061ff 0%, #60efff 100%); }
.gradient-green { background: linear-gradient(135deg, #10b981 0%, #34d399 100%); }
.gradient-orange { background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%); }
.gradient-purple { background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%); }

/* 页面头部 */
.page-header {
  padding: 24px 30px;
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(0, 97, 255, 0.1);
  color: #0061ff;
}

.page-subtitle {
  color: #64748b;
  margin: 0;
  font-size: 14px;
}

.header-actions {
  display: flex;
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
}

.glass-btn:hover {
  background: rgba(255, 255, 255, 0.8);
  color: #0061ff;
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

/* 统计卡片 */
.stats-section { margin-bottom: 24px; }
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
}

.stat-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.stat-content { flex: 1; }

.stat-number {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
}

.stat-change {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 12px;
  width: fit-content;
}

.stat-change.positive { background: rgba(16, 185, 129, 0.1); color: #10b981; }

/* 筛选区域 */
.filter-section { margin-bottom: 24px; }
.filter-content {
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  flex-wrap: wrap;
}

.filter-left {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  flex: 1;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filter-label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.filter-right {
  display: flex;
  gap: 16px;
  align-items: center;
}

.search-box { width: 280px; }

/* 用户列表头部 */
.users-section { margin-bottom: 24px; }

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-title h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.pulse-badge {
  background: linear-gradient(135deg, #0061ff, #60efff);
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 0 10px rgba(0, 97, 255, 0.4);
}

.section-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.batch-actions {
  display: flex;
  gap: 8px;
}

.tech-small-btn {
  border: none;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}
.tech-small-btn.success { background: rgba(16, 185, 129, 0.1); color: #10b981; }
.tech-small-btn.success:hover { background: #10b981; color: white; }
.tech-small-btn.warning { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
.tech-small-btn.warning:hover { background: #f59e0b; color: white; }
.tech-small-btn.danger { background: rgba(239, 68, 68, 0.1); color: #ef4444; }
.tech-small-btn.danger:hover { background: #ef4444; color: white; }

/* 卡片视图 */
.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.user-card {
  padding: 24px;
  cursor: pointer;
}

.user-card.selected {
  border: 2px solid #0061ff;
  background: rgba(0, 97, 255, 0.05);
  box-shadow: 0 8px 24px rgba(0, 97, 255, 0.15);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.tech-avatar {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  border: 2px solid white;
}

.tech-avatar.small {
  width: 40px;
  height: 40px;
}

.tech-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-basic { flex: 1; }
.user-name { font-weight: 600; color: #1e293b; font-size: 16px; }
.user-email { color: #64748b; font-size: 13px; margin-top: 2px; }

.card-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-meta {
  display: flex;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed rgba(0,0,0,0.1);
}

.meta-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-label { font-size: 12px; color: #64748b; }

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

.user-stats {
  display: flex;
  gap: 16px;
}

.stat-item-small {
  flex: 1;
  background: rgba(255,255,255,0.4);
  padding: 12px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-item-small .stat-value { font-size: 20px; font-weight: 700; }
.stat-item-small .stat-label { font-size: 12px; color: #64748b; }

.user-dates {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 4px;
}

.date-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
}

.date-item i, .date-item svg { color: #0061ff; opacity: 0.7; }

/* 表格视图 */
.table-view {
  padding: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.text-muted { color: #64748b; }
.text-danger { color: #ef4444; }

.tech-text-btn {
  color: #64748b;
  transition: all 0.3s ease;
}
.tech-text-btn:hover { color: #0061ff; background: rgba(0, 97, 255, 0.1); }

/* 分页 */
.pagination-section {
  margin-top: 24px;
  padding: 16px 24px;
  display: flex;
  justify-content: center;
}

/* 弹窗样式调整 */
.dialog-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.close-btn {
  margin-left: auto;
  background: none;
  border: none;
  font-size: 18px;
  color: #64748b;
  cursor: pointer;
  transition: color 0.3s;
}

.close-btn:hover { color: #ef4444; }

.create-form { padding: 20px 0; }
.w-full { width: 100%; }

.user-detail-drawer {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 8px 4px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.detail-item {
  padding: 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(255, 255, 255, 0.45);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.detail-label {
  display: block;
  margin-bottom: 8px;
  color: #64748b;
  font-size: 13px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .filter-content { flex-direction: column; align-items: stretch; }
  .filter-right { justify-content: space-between; }
  .search-box { flex: 1; }
}

@media (max-width: 768px) {
  .page-header { padding: 20px; }
  .header-content { flex-direction: column; gap: 16px; align-items: stretch; }
  .section-header { flex-direction: column; gap: 16px; align-items: stretch; }
  .section-actions { flex-direction: column; align-items: stretch; gap: 12px; }
  .batch-actions { justify-content: space-between; }
}
</style>

<style>
/* 全局弹窗样式，解决 append-to-body 后的样式丢失问题 */
.glass-dialog {
  background: rgba(255, 255, 255, 0.85) !important;
  backdrop-filter: blur(20px) !important;
  -webkit-backdrop-filter: blur(20px) !important;
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  border-radius: 24px !important;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1) !important;
  overflow: hidden;
}

.glass-drawer {
  background: rgba(255, 255, 255, 0.85) !important;
  backdrop-filter: blur(20px) !important;
  -webkit-backdrop-filter: blur(20px) !important;
  border-left: 1px solid rgba(255, 255, 255, 0.4) !important;
  box-shadow: -10px 0 40px rgba(0, 0, 0, 0.1) !important;
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.close-btn {
  margin-left: auto;
  background: none;
  border: none;
  font-size: 18px;
  color: #64748b;
  cursor: pointer;
  transition: color 0.3s;
}

.close-btn:hover { color: #ef4444; }

.create-form { padding: 20px 0; }
.w-full { width: 100%; }

.drawer-content {
  padding: 24px;
  height: 100%;
  overflow-y: auto;
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
  margin-bottom: 24px;
}

.header-main { display: flex; gap: 16px; align-items: center; }
.header-text { display: flex; flex-direction: column; gap: 4px; }
.title-row { display: flex; align-items: center; gap: 12px; }
.title-row h2 { margin: 0; font-size: 20px; font-weight: 700; color: #1e293b; }
.meta-info { margin: 0; font-size: 13px; color: #64748b; display: flex; align-items: center; gap: 8px; }
.divider { color: #cbd5e1; }

.detail-grid { display: flex; flex-direction: column; gap: 20px; }
.detail-card { padding: 20px; border-radius: 16px; background: rgba(255, 255, 255, 0.4); border: 1px solid rgba(255, 255, 255, 0.6); }
.detail-card h3 { margin: 0 0 16px 0; font-size: 15px; font-weight: 600; color: #1e293b; display: flex; align-items: center; }

.tech-dl { margin: 0; display: flex; flex-direction: column; gap: 12px; }
.tech-dl div { display: flex; justify-content: space-between; align-items: center; padding-bottom: 8px; border-bottom: 1px dashed rgba(0, 0, 0, 0.05); }
.tech-dl div:last-child { border-bottom: none; padding-bottom: 0; }
.tech-dl dt { color: #64748b; font-size: 13px; }
.tech-dl dd { margin: 0; font-weight: 500; color: #334155; font-size: 14px; }
</style>
