<template>
  <div class="user-management-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <font-awesome-icon :icon="['fas', 'users']" />
            用户管理
          </h1>
          <p class="page-subtitle">管理系统中的所有用户账户</p>
        </div>
        <div class="header-actions">
          <el-button class="action-btn import-btn">
            <font-awesome-icon :icon="['fas', 'upload']" />
            批量导入
          </el-button>
          <el-button class="action-btn export-btn">
            <font-awesome-icon :icon="['fas', 'download']" />
            导出数据
          </el-button>
          <el-button type="primary" class="action-btn create-btn" @click="showCreateDialog = true">
            <font-awesome-icon :icon="['fas', 'plus']" />
            添加用户
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card total">
          <div class="stat-icon">
            <font-awesome-icon :icon="['fas', 'users']" />
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.total }}</div>
            <div class="stat-label">总用户数</div>
            <div class="stat-change positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              +{{ stats.totalGrowth }}%
            </div>
          </div>
        </div>
        
        <div class="stat-card students">
          <div class="stat-icon">
            <font-awesome-icon :icon="['fas', 'graduation-cap']" />
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.students }}</div>
            <div class="stat-label">学生用户</div>
            <div class="stat-change positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              +{{ stats.studentsGrowth }}%
            </div>
          </div>
        </div>
        
        <div class="stat-card teachers">
          <div class="stat-icon">
            <font-awesome-icon :icon="['fas', 'chalkboard-teacher']" />
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.teachers }}</div>
            <div class="stat-label">教师用户</div>
            <div class="stat-change positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              +{{ stats.teachersGrowth }}%
            </div>
          </div>
        </div>
        
        <div class="stat-card active">
          <div class="stat-icon">
            <font-awesome-icon :icon="['fas', 'user-check']" />
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.active }}</div>
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
      <div class="filter-content">
        <div class="filter-left">
          <div class="filter-group">
            <label class="filter-label">用户角色</label>
            <el-select v-model="filters.role" placeholder="全部角色" clearable>
              <el-option label="全部角色" value=""></el-option>
              <el-option label="学生" value="student"></el-option>
              <el-option label="教师" value="teacher"></el-option>
              <el-option label="管理员" value="admin"></el-option>
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">账户状态</label>
            <el-select v-model="filters.status" placeholder="全部状态" clearable>
              <el-option label="全部状态" value=""></el-option>
              <el-option label="正常" value="active"></el-option>
              <el-option label="禁用" value="disabled"></el-option>
              <el-option label="待激活" value="pending"></el-option>
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
            />
          </div>
          
          <div class="filter-group">
            <label class="filter-label">排序方式</label>
            <el-select v-model="filters.sortBy" placeholder="排序方式">
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
              class="search-input"
            >
              <template #prefix>
                <font-awesome-icon :icon="['fas', 'search']" />
              </template>
            </el-input>
          </div>
          
          <div class="filter-actions">
            <el-button @click="resetFilters">
              <font-awesome-icon :icon="['fas', 'undo']" />
              重置
            </el-button>
            <el-button type="primary" @click="applyFilters">
              <font-awesome-icon :icon="['fas', 'filter']" />
              筛选
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="users-section">
      <div class="section-header">
        <div class="section-title">
          <h3>用户列表</h3>
          <span class="user-count">共 {{ filteredUsers.length }} 个用户</span>
        </div>
        
        <div class="section-actions">
          <div class="view-toggle">
            <el-radio-group v-model="viewMode" size="small">
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
            <el-button size="small" @click="batchEnable">
              <font-awesome-icon :icon="['fas', 'check']" />
              批量启用
            </el-button>
            <el-button size="small" @click="batchDisable">
              <font-awesome-icon :icon="['fas', 'ban']" />
              批量禁用
            </el-button>
            <el-button size="small" type="danger" @click="batchDelete">
              <font-awesome-icon :icon="['fas', 'trash']" />
              批量删除
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 表格视图 -->
      <div v-if="viewMode === 'table'" class="table-view">
        <el-table
          :data="paginatedUsers"
          @selection-change="handleSelectionChange"
          class="users-table"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          
          <el-table-column label="用户信息" min-width="200">
            <template #default="{ row }">
              <div class="user-info">
                <div class="user-avatar">
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
              <el-tag :type="getRoleTagType(row.role)" size="small">
                {{ getRoleName(row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" size="small">
                {{ getStatusName(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="注册时间" width="120">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          
          <el-table-column label="最后登录" width="120">
            <template #default="{ row }">
              {{ formatDate(row.lastLogin) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button size="small" text @click="viewUser(row)">
                  <font-awesome-icon :icon="['fas', 'eye']" />
                  查看
                </el-button>
                <el-button size="small" text @click="editUser(row)">
                  <font-awesome-icon :icon="['fas', 'edit']" />
                  编辑
                </el-button>
                <el-dropdown trigger="click">
                  <el-button size="small" text>
                    <font-awesome-icon :icon="['fas', 'ellipsis-h']" />
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="resetPassword(row)">
                        <font-awesome-icon :icon="['fas', 'key']" />
                        重置密码
                      </el-dropdown-item>
                      <el-dropdown-item @click="toggleStatus(row)">
                        <font-awesome-icon :icon="['fas', row.status === 'active' ? 'ban' : 'check']" />
                        {{ row.status === 'active' ? '禁用' : '启用' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="deleteUser(row)" divided>
                        <font-awesome-icon :icon="['fas', 'trash']" />
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 卡片视图 -->
      <div v-else class="grid-view">
        <div class="users-grid">
          <div 
            v-for="user in paginatedUsers" 
            :key="user.id"
            class="user-card"
            :class="{ selected: selectedUsers.includes(user.id) }"
            @click="toggleUserSelection(user.id)"
          >
            <div class="card-header">
              <div class="user-avatar">
                <img :src="user.avatar" :alt="user.username" />
              </div>
              <div class="user-basic">
                <div class="user-name">{{ user.username }}</div>
                <div class="user-email">{{ user.email }}</div>
              </div>
              <div class="card-actions">
                <el-dropdown trigger="click" @click.stop>
                  <el-button size="small" circle>
                    <font-awesome-icon :icon="['fas', 'ellipsis-v']" />
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="viewUser(user)">
                        <font-awesome-icon :icon="['fas', 'eye']" />
                        查看详情
                      </el-dropdown-item>
                      <el-dropdown-item @click="editUser(user)">
                        <font-awesome-icon :icon="['fas', 'edit']" />
                        编辑
                      </el-dropdown-item>
                      <el-dropdown-item @click="resetPassword(user)">
                        <font-awesome-icon :icon="['fas', 'key']" />
                        重置密码
                      </el-dropdown-item>
                      <el-dropdown-item @click="toggleStatus(user)" divided>
                        <font-awesome-icon :icon="['fas', user.status === 'active' ? 'ban' : 'check']" />
                        {{ user.status === 'active' ? '禁用' : '启用' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="deleteUser(user)">
                        <font-awesome-icon :icon="['fas', 'trash']" />
                        删除
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
                  <el-tag :type="getRoleTagType(user.role)" size="small">
                    {{ getRoleName(user.role) }}
                  </el-tag>
                </div>
                <div class="meta-item">
                  <span class="meta-label">状态</span>
                  <el-tag :type="getStatusTagType(user.status)" size="small">
                    {{ getStatusName(user.status) }}
                  </el-tag>
                </div>
              </div>
              
              <div class="user-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ user.coursesCount || 0 }}</span>
                  <span class="stat-label">课程数</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ user.loginCount || 0 }}</span>
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
                  <span>最后登录：{{ formatDate(user.lastLogin) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredUsers.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 创建用户对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="添加用户"
      width="600px"
      class="create-dialog"
    >
      <el-form :model="newUser" label-width="100px" class="create-form">
        <el-form-item label="用户名">
          <el-input v-model="newUser.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="邮箱">
          <el-input v-model="newUser.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        
        <el-form-item label="手机号">
          <el-input v-model="newUser.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="角色">
          <el-select v-model="newUser.role" placeholder="请选择角色">
            <el-option label="学生" value="student"></el-option>
            <el-option label="教师" value="teacher"></el-option>
            <el-option label="管理员" value="admin"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="初始密码">
          <el-input v-model="newUser.password" type="password" placeholder="请输入初始密码" />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-radio-group v-model="newUser.status">
            <el-radio label="active">正常</el-radio>
            <el-radio label="pending">待激活</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="createUser">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 统计数据
const stats = ref({
  total: 1248,
  totalGrowth: 12.5,
  students: 1089,
  studentsGrowth: 15.2,
  teachers: 159,
  teachersGrowth: 8.7,
  active: 1156,
  activeGrowth: 10.3
})

// 筛选条件
const filters = ref({
  role: '',
  status: '',
  dateRange: null,
  sortBy: 'created_at',
  search: ''
})

// 视图模式
const viewMode = ref('table')

// 分页
const currentPage = ref(1)
const pageSize = ref(20)

// 选中的用户
const selectedUsers = ref([])

// 对话框
const showCreateDialog = ref(false)

// 新用户表单
const newUser = ref({
  username: '',
  email: '',
  phone: '',
  role: 'student',
  password: '',
  status: 'active'
})

// 模拟用户数据
const users = ref([
  {
    id: 1,
    username: '张三',
    email: 'zhangsan@example.com',
    phone: '13800138001',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    role: 'student',
    status: 'active',
    createdAt: '2024-01-15',
    lastLogin: '2024-03-15',
    coursesCount: 5,
    loginCount: 128
  },
  {
    id: 2,
    username: '李老师',
    email: 'liteacher@example.com',
    phone: '13800138002',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    role: 'teacher',
    status: 'active',
    createdAt: '2024-01-10',
    lastLogin: '2024-03-14',
    coursesCount: 12,
    loginCount: 256
  },
  {
    id: 3,
    username: '王五',
    email: 'wangwu@example.com',
    phone: '13800138003',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    role: 'student',
    status: 'disabled',
    createdAt: '2024-02-01',
    lastLogin: '2024-03-10',
    coursesCount: 2,
    loginCount: 45
  },
  {
    id: 4,
    username: '赵管理员',
    email: 'admin@example.com',
    phone: '13800138004',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    role: 'admin',
    status: 'active',
    createdAt: '2024-01-01',
    lastLogin: '2024-03-15',
    coursesCount: 0,
    loginCount: 512
  },
  {
    id: 5,
    username: '孙学生',
    email: 'sunstudent@example.com',
    phone: '13800138005',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    role: 'student',
    status: 'pending',
    createdAt: '2024-03-01',
    lastLogin: null,
    coursesCount: 0,
    loginCount: 0
  }
])

// 筛选后的用户
const filteredUsers = computed(() => {
  let result = [...users.value]
  
  // 角色筛选
  if (filters.value.role) {
    result = result.filter(user => user.role === filters.value.role)
  }
  
  // 状态筛选
  if (filters.value.status) {
    result = result.filter(user => user.status === filters.value.status)
  }
  
  // 搜索筛选
  if (filters.value.search) {
    const search = filters.value.search.toLowerCase()
    result = result.filter(user => 
      user.username.toLowerCase().includes(search) ||
      user.email.toLowerCase().includes(search) ||
      (user.phone && user.phone.includes(search))
    )
  }
  
  // 排序
  result.sort((a, b) => {
    const field = filters.value.sortBy
    if (field === 'created_at') {
      return new Date(b.createdAt) - new Date(a.createdAt)
    } else if (field === 'last_login') {
      const aDate = a.lastLogin ? new Date(a.lastLogin) : new Date(0)
      const bDate = b.lastLogin ? new Date(b.lastLogin) : new Date(0)
      return bDate - aDate
    } else {
      return a[field].localeCompare(b[field])
    }
  })
  
  return result
})

// 分页后的用户
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUsers.value.slice(start, end)
})

// 获取角色标签类型
const getRoleTagType = (role) => {
  const types = {
    student: '',
    teacher: 'success',
    admin: 'danger'
  }
  return types[role] || ''
}

// 获取角色名称
const getRoleName = (role) => {
  const names = {
    student: '学生',
    teacher: '教师',
    admin: '管理员'
  }
  return names[role] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const types = {
    active: 'success',
    disabled: 'danger',
    pending: 'warning'
  }
  return types[status] || ''
}

// 获取状态名称
const getStatusName = (status) => {
  const names = {
    active: '正常',
    disabled: '禁用',
    pending: '待激活'
  }
  return names[status] || '未知'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '从未'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 重置筛选
const resetFilters = () => {
  filters.value = {
    role: '',
    status: '',
    dateRange: null,
    sortBy: 'created_at',
    search: ''
  }
}

// 应用筛选
const applyFilters = () => {
  currentPage.value = 1
  ElMessage.success('筛选条件已应用')
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection.map(user => user.id)
}

// 切换用户选择
const toggleUserSelection = (userId) => {
  const index = selectedUsers.value.indexOf(userId)
  if (index > -1) {
    selectedUsers.value.splice(index, 1)
  } else {
    selectedUsers.value.push(userId)
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 查看用户
const viewUser = (user) => {
  console.log('查看用户:', user)
  ElMessage.info(`查看用户：${user.username}`)
}

// 编辑用户
const editUser = (user) => {
  console.log('编辑用户:', user)
  ElMessage.info(`编辑用户：${user.username}`)
}

// 重置密码
const resetPassword = (user) => {
  ElMessageBox.confirm(
    `确定要重置用户 ${user.username} 的密码吗？`,
    '重置密码',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('密码重置成功')
  }).catch(() => {
    ElMessage.info('已取消重置')
  })
}

// 切换状态
const toggleStatus = (user) => {
  const action = user.status === 'active' ? '禁用' : '启用'
  ElMessageBox.confirm(
    `确定要${action}用户 ${user.username} 吗？`,
    `${action}用户`,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    user.status = user.status === 'active' ? 'disabled' : 'active'
    ElMessage.success(`用户${action}成功`)
  }).catch(() => {
    ElMessage.info(`已取消${action}`)
  })
}

// 删除用户
const deleteUser = (user) => {
  ElMessageBox.confirm(
    `确定要删除用户 ${user.username} 吗？此操作不可恢复。`,
    '删除用户',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    }
  ).then(() => {
    const index = users.value.findIndex(u => u.id === user.id)
    if (index > -1) {
      users.value.splice(index, 1)
      ElMessage.success('用户删除成功')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 批量启用
const batchEnable = () => {
  ElMessageBox.confirm(
    `确定要启用选中的 ${selectedUsers.value.length} 个用户吗？`,
    '批量启用',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    users.value.forEach(user => {
      if (selectedUsers.value.includes(user.id)) {
        user.status = 'active'
      }
    })
    selectedUsers.value = []
    ElMessage.success('批量启用成功')
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 批量禁用
const batchDisable = () => {
  ElMessageBox.confirm(
    `确定要禁用选中的 ${selectedUsers.value.length} 个用户吗？`,
    '批量禁用',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    users.value.forEach(user => {
      if (selectedUsers.value.includes(user.id)) {
        user.status = 'disabled'
      }
    })
    selectedUsers.value = []
    ElMessage.success('批量禁用成功')
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 批量删除
const batchDelete = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedUsers.value.length} 个用户吗？此操作不可恢复。`,
    '批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    }
  ).then(() => {
    users.value = users.value.filter(user => !selectedUsers.value.includes(user.id))
    selectedUsers.value = []
    ElMessage.success('批量删除成功')
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 创建用户
const createUser = () => {
  // 简单验证
  if (!newUser.value.username || !newUser.value.email || !newUser.value.role) {
    ElMessage.error('请填写必要信息')
    return
  }
  
  const user = {
    id: Date.now(),
    ...newUser.value,
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    createdAt: new Date().toISOString().split('T')[0],
    lastLogin: null,
    coursesCount: 0,
    loginCount: 0
  }
  
  users.value.unshift(user)
  showCreateDialog.value = false
  
  // 重置表单
  newUser.value = {
    username: '',
    email: '',
    phone: '',
    role: 'student',
    password: '',
    status: 'active'
  }
  
  ElMessage.success('用户创建成功')
}

onMounted(() => {
  // 组件挂载时的初始化逻辑
})
</script>

<style scoped>
.user-management-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f0f3 0%, #e8e8eb 100%);
  padding: 0;
}

/* 页面头部 */
.page-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 24px 30px;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title i {
  color: #002FA7;
}

.page-subtitle {
  color: #6b7280;
  margin: 0;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.import-btn,
.export-btn {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  color: #6c757d;
}

.create-btn {
  background: linear-gradient(135deg, #002FA7, #517B4D);
  border: none;
  color: white;
}

/* 统计卡片 */
.stats-section {
  padding: 30px;
  padding-bottom: 0;
}

.stats-grid {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 
    15px 15px 30px rgba(0, 0, 0, 0.1),
    -15px -15px 30px rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    20px 20px 40px rgba(0, 0, 0, 0.15),
    -20px -20px 40px rgba(255, 255, 255, 0.9);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-card.total .stat-icon {
  background: linear-gradient(135deg, #002FA7, #517B4D);
}

.stat-card.students .stat-icon {
  background: linear-gradient(135deg, #28a745, #20c997);
}

.stat-card.teachers .stat-icon {
  background: linear-gradient(135deg, #ffc107, #fd7e14);
}

.stat-card.active .stat-icon {
  background: linear-gradient(135deg, #6f42c1, #e83e8c);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-change {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
}

.stat-change.positive {
  color: #28a745;
}

.stat-change.negative {
  color: #dc3545;
}

/* 筛选区域 */
.filter-section {
  padding: 30px;
  padding-bottom: 0;
}

.filter-content {
  max-width: 1400px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 
    15px 15px 30px rgba(0, 0, 0, 0.1),
    -15px -15px 30px rgba(255, 255, 255, 0.8);
  display: flex;
  justify-content: space-between;
  gap: 24px;
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
  gap: 8px;
  min-width: 160px;
}

.filter-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.filter-right {
  display: flex;
  gap: 16px;
  align-items: flex-end;
}

.search-box {
  min-width: 280px;
}

.search-input {
  border-radius: 12px;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

/* 用户列表 */
.users-section {
  padding: 30px;
}

.section-header {
  max-width: 1400px;
  margin: 0 auto 24px auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-title h3 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.user-count {
  color: #6b7280;
  font-size: 14px;
}

.section-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.view-toggle {
  display: flex;
}

.batch-actions {
  display: flex;
  gap: 8px;
}

/* 表格视图 */
.table-view {
  max-width: 1400px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 
    15px 15px 30px rgba(0, 0, 0, 0.1),
    -15px -15px 30px rgba(255, 255, 255, 0.8);
}

.users-table {
  width: 100%;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-weight: 600;
  color: #2c3e50;
}

.user-email {
  color: #6b7280;
  font-size: 12px;
}

.table-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 卡片视图 */
.grid-view {
  max-width: 1400px;
  margin: 0 auto;
}

.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.user-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 
    15px 15px 30px rgba(0, 0, 0, 0.1),
    -15px -15px 30px rgba(255, 255, 255, 0.8);
}

.user-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    20px 20px 40px rgba(0, 0, 0, 0.15),
    -20px -20px 40px rgba(255, 255, 255, 0.9);
}

.user-card.selected {
  border: 2px solid #002FA7;
  background: rgba(0, 47, 167, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.user-basic {
  flex: 1;
}

.card-actions {
  display: flex;
  align-items: center;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.meta-label {
  font-size: 12px;
  color: #6b7280;
}

.user-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #002FA7;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
}

.user-dates {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.date-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #6b7280;
}

.date-item i {
  color: #002FA7;
}

/* 分页 */
.pagination-section {
  max-width: 1400px;
  margin: 24px auto 0 auto;
  display: flex;
  justify-content: center;
}

/* 对话框 */
.create-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.create-form {
  padding: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .filter-content {
    flex-direction: column;
    gap: 20px;
  }
  
  .filter-left {
    justify-content: space-between;
  }
  
  .filter-right {
    justify-content: space-between;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 20px 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .stats-section,
  .filter-section,
  .users-section {
    padding: 20px 16px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-left {
    flex-direction: column;
    gap: 16px;
  }
  
  .filter-group {
    min-width: auto;
  }
  
  .filter-right {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    min-width: auto;
  }
  
  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .section-actions {
    justify-content: space-between;
  }
  
  .users-grid {
    grid-template-columns: 1fr;
  }
  
  .user-meta {
    flex-direction: column;
    gap: 12px;
  }
  
  .meta-item {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
}
</style>