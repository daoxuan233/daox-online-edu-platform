<template>
  <div class="category-management-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <font-awesome-icon :icon="['fas', 'tags']" />
            分类管理
          </h1>
          <p class="page-subtitle">管理课程分类和标签体系</p>
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
            添加分类
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card total">
          <div class="stat-icon">
            <font-awesome-icon :icon="['fas', 'layer-group']" />
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalCategories }}</div>
            <div class="stat-label">总分类数</div>
            <div class="stat-change positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              +{{ stats.categoriesGrowth }}%
            </div>
          </div>
        </div>
        
        <div class="stat-card active">
          <div class="stat-icon">
            <font-awesome-icon :icon="['fas', 'check-circle']" />
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.activeCategories }}</div>
            <div class="stat-label">启用分类</div>
            <div class="stat-change positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              +{{ stats.activeGrowth }}%
            </div>
          </div>
        </div>
        
        <div class="stat-card courses">
          <div class="stat-icon">
            <font-awesome-icon :icon="['fas', 'book']" />
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.coursesWithCategories }}</div>
            <div class="stat-label">已分类课程</div>
            <div class="stat-change positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              +{{ stats.coursesGrowth }}%
            </div>
          </div>
        </div>
        
        <div class="stat-card popular">
          <div class="stat-icon">
            <font-awesome-icon :icon="['fas', 'fire']" />
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.popularCategories }}</div>
            <div class="stat-label">热门分类</div>
            <div class="stat-change positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              +{{ stats.popularGrowth }}%
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
            <label class="filter-label">分类状态</label>
            <el-select v-model="filters.status" placeholder="全部状态" clearable>
              <el-option label="全部状态" value=""></el-option>
              <el-option label="启用" value="active"></el-option>
              <el-option label="禁用" value="disabled"></el-option>
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">分类层级</label>
            <el-select v-model="filters.level" placeholder="全部层级" clearable>
              <el-option label="全部层级" value=""></el-option>
              <el-option label="一级分类" value="1"></el-option>
              <el-option label="二级分类" value="2"></el-option>
              <el-option label="三级分类" value="3"></el-option>
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">课程数量</label>
            <el-select v-model="filters.courseCount" placeholder="课程数量" clearable>
              <el-option label="全部" value=""></el-option>
              <el-option label="无课程" value="0"></el-option>
              <el-option label="1-10门" value="1-10"></el-option>
              <el-option label="11-50门" value="11-50"></el-option>
              <el-option label="50门以上" value="50+"></el-option>
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">排序方式</label>
            <el-select v-model="filters.sortBy" placeholder="排序方式">
              <el-option label="创建时间" value="created_at"></el-option>
              <el-option label="更新时间" value="updated_at"></el-option>
              <el-option label="分类名称" value="name"></el-option>
              <el-option label="课程数量" value="course_count"></el-option>
              <el-option label="排序权重" value="sort_order"></el-option>
            </el-select>
          </div>
        </div>
        
        <div class="filter-right">
          <div class="search-box">
            <el-input
              v-model="filters.search"
              placeholder="搜索分类名称或描述"
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

    <!-- 分类列表 -->
    <div class="categories-section">
      <div class="section-header">
        <div class="section-title">
          <h3>分类列表</h3>
          <span class="category-count">共 {{ filteredCategories.length }} 个分类</span>
        </div>
        
        <div class="section-actions">
          <div class="view-toggle">
            <el-radio-group v-model="viewMode" size="small">
              <el-radio-button label="tree">
                <font-awesome-icon :icon="['fas', 'sitemap']" />
                树形
              </el-radio-button>
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
          
          <div class="batch-actions" v-if="selectedCategories.length > 0">
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
      
      <!-- 树形视图 -->
      <div v-if="viewMode === 'tree'" class="tree-view">
        <div class="tree-container">
          <el-tree
            :data="treeData"
            :props="treeProps"
            node-key="id"
            :default-expand-all="true"
            :show-checkbox="true"
            @check="handleTreeCheck"
            class="category-tree"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <div class="node-content">
                  <div class="node-icon">
                    <font-awesome-icon :icon="data.icon ? data.icon.split(' ') : ['fas', 'folder']" />
                  </div>
                  <div class="node-info">
                    <div class="node-name">{{ data.name }}</div>
                    <div class="node-meta">
                      <span class="course-count">{{ data.courseCount || 0 }} 门课程</span>
                      <el-tag :type="data.status === 'active' ? 'success' : 'danger'" size="small">
                        {{ data.status === 'active' ? '启用' : '禁用' }}
                      </el-tag>
                    </div>
                  </div>
                </div>
                <div class="node-actions">
                  <el-button size="small" text @click="addSubCategory(data)">
                    <font-awesome-icon :icon="['fas', 'plus']" />
                  </el-button>
                  <el-button size="small" text @click="editCategory(data)">
                    <font-awesome-icon :icon="['fas', 'edit']" />
                  </el-button>
                  <el-button size="small" text @click="deleteCategory(data)">
                    <font-awesome-icon :icon="['fas', 'trash']" />
                  </el-button>
                </div>
              </div>
            </template>
          </el-tree>
        </div>
      </div>
      
      <!-- 表格视图 -->
      <div v-else-if="viewMode === 'table'" class="table-view">
        <el-table
          :data="paginatedCategories"
          @selection-change="handleSelectionChange"
          class="categories-table"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          
          <el-table-column label="分类信息" min-width="200">
            <template #default="{ row }">
              <div class="category-info">
                <div class="category-icon">
                  <font-awesome-icon :icon="row.icon ? row.icon.split(' ') : ['fas', 'folder']" />
                </div>
                <div class="category-details">
                  <div class="category-name">{{ row.name }}</div>
                  <div class="category-description">{{ row.description || '暂无描述' }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="层级" width="80">
            <template #default="{ row }">
              <el-tag size="small">{{ getLevelName(row.level) }}</el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="父分类" width="120">
            <template #default="{ row }">
              <span v-if="row.parentName">{{ row.parentName }}</span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          
          <el-table-column label="课程数" width="80">
            <template #default="{ row }">
              <span class="course-count">{{ row.courseCount || 0 }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="排序" width="80">
            <template #default="{ row }">
              <span>{{ row.sortOrder || 0 }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
                {{ row.status === 'active' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="创建时间" width="120">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button size="small" text @click="addSubCategory(row)">
                  <font-awesome-icon :icon="['fas', 'plus']" />
                  子分类
                </el-button>
                <el-button size="small" text @click="editCategory(row)">
                  <font-awesome-icon :icon="['fas', 'edit']" />
                  编辑
                </el-button>
                <el-dropdown trigger="click">
                  <el-button size="small" text>
                    <font-awesome-icon :icon="['fas', 'ellipsis-h']" />
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="toggleStatus(row)">
                        <font-awesome-icon :icon="['fas', row.status === 'active' ? 'ban' : 'check']" />
                        {{ row.status === 'active' ? '禁用' : '启用' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="moveCategory(row)">
                        <font-awesome-icon :icon="['fas', 'arrows-alt']" />
                        移动
                      </el-dropdown-item>
                      <el-dropdown-item @click="deleteCategory(row)" divided>
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
        <div class="categories-grid">
          <div 
            v-for="category in paginatedCategories" 
            :key="category.id"
            class="category-card"
            :class="{ selected: selectedCategories.includes(category.id) }"
            @click="toggleCategorySelection(category.id)"
          >
            <div class="card-header">
              <div class="category-icon">
                <font-awesome-icon :icon="category.icon ? category.icon.split(' ') : ['fas', 'folder']" />
              </div>
              <div class="category-basic">
                <div class="category-name">{{ category.name }}</div>
                <div class="category-level">{{ getLevelName(category.level) }}</div>
              </div>
              <div class="card-actions">
                <el-dropdown trigger="click" @click.stop>
                  <el-button size="small" circle>
                    <font-awesome-icon :icon="['fas', 'ellipsis-v']" />
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="addSubCategory(category)">
                        <font-awesome-icon :icon="['fas', 'plus']" />
                        添加子分类
                      </el-dropdown-item>
                      <el-dropdown-item @click="editCategory(category)">
                        <font-awesome-icon :icon="['fas', 'edit']" />
                        编辑
                      </el-dropdown-item>
                      <el-dropdown-item @click="toggleStatus(category)" divided>
                        <font-awesome-icon :icon="['fas', category.status === 'active' ? 'ban' : 'check']" />
                        {{ category.status === 'active' ? '禁用' : '启用' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="deleteCategory(category)">
                        <font-awesome-icon :icon="['fas', 'trash']" />
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            
            <div class="card-content">
              <div class="category-description">
                {{ category.description || '暂无描述' }}
              </div>
              
              <div class="category-meta">
                <div class="meta-item">
                  <span class="meta-label">状态</span>
                  <el-tag :type="category.status === 'active' ? 'success' : 'danger'" size="small">
                    {{ category.status === 'active' ? '启用' : '禁用' }}
                  </el-tag>
                </div>
                <div class="meta-item">
                  <span class="meta-label">父分类</span>
                  <span class="meta-value">{{ category.parentName || '无' }}</span>
                </div>
              </div>
              
              <div class="category-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ category.courseCount || 0 }}</span>
                  <span class="stat-label">课程数</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ category.sortOrder || 0 }}</span>
                  <span class="stat-label">排序权重</span>
                </div>
              </div>
              
              <div class="category-dates">
                <div class="date-item">
                  <font-awesome-icon :icon="['fas', 'calendar-plus']" />
                  <span>创建：{{ formatDate(category.createdAt) }}</span>
                </div>
                <div class="date-item">
                  <font-awesome-icon :icon="['fas', 'clock']" />
                  <span>更新：{{ formatDate(category.updatedAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-section" v-if="viewMode !== 'tree'">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredCategories.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 创建/编辑分类对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingCategory ? '编辑分类' : '添加分类'"
      width="600px"
      class="create-dialog"
    >
      <el-form :model="categoryForm" label-width="100px" class="create-form">
        <el-form-item label="分类名称">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        
        <el-form-item label="分类描述">
          <el-input 
            v-model="categoryForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        
        <el-form-item label="父分类">
          <el-select v-model="categoryForm.parentId" placeholder="请选择父分类" clearable>
            <el-option label="无（顶级分类）" :value="null"></el-option>
            <el-option 
              v-for="category in availableParents" 
              :key="category.id"
              :label="category.name"
              :value="category.id"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="分类图标">
          <el-input v-model="categoryForm.icon" placeholder="请输入图标类名，如：fas fa-code">
            <template #prepend>
              <font-awesome-icon :icon="categoryForm.icon ? categoryForm.icon.split(' ') : ['fas', 'folder']" />
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="排序权重">
          <el-input-number 
            v-model="categoryForm.sortOrder" 
            :min="0" 
            :max="9999"
            placeholder="数值越大排序越靠前"
          />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-radio-group v-model="categoryForm.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" @click="saveCategory">确定</el-button>
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
  totalCategories: 45,
  categoriesGrowth: 8.5,
  activeCategories: 38,
  activeGrowth: 12.3,
  coursesWithCategories: 156,
  coursesGrowth: 15.7,
  popularCategories: 12,
  popularGrowth: 6.8
})

// 筛选条件
const filters = ref({
  status: '',
  level: '',
  courseCount: '',
  sortBy: 'created_at',
  search: ''
})

// 视图模式
const viewMode = ref('tree')

// 分页
const currentPage = ref(1)
const pageSize = ref(20)

// 选中的分类
const selectedCategories = ref([])

// 对话框
const showCreateDialog = ref(false)
const editingCategory = ref(null)

// 分类表单
const categoryForm = ref({
  name: '',
  description: '',
  parentId: null,
  icon: 'fas fa-folder',
  sortOrder: 0,
  status: 'active'
})

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 模拟分类数据
const categories = ref([
  {
    id: 1,
    name: '编程开发',
    description: '编程语言和开发技术相关课程',
    parentId: null,
    parentName: null,
    level: 1,
    icon: 'fas fa-code',
    sortOrder: 100,
    status: 'active',
    courseCount: 45,
    createdAt: '2024-01-01',
    updatedAt: '2024-03-01'
  },
  {
    id: 2,
    name: 'Web前端',
    description: '前端开发技术',
    parentId: 1,
    parentName: '编程开发',
    level: 2,
    icon: 'fab fa-html5',
    sortOrder: 90,
    status: 'active',
    courseCount: 25,
    createdAt: '2024-01-02',
    updatedAt: '2024-03-02'
  },
  {
    id: 3,
    name: 'JavaScript',
    description: 'JavaScript编程语言',
    parentId: 2,
    parentName: 'Web前端',
    level: 3,
    icon: 'fab fa-js-square',
    sortOrder: 80,
    status: 'active',
    courseCount: 15,
    createdAt: '2024-01-03',
    updatedAt: '2024-03-03'
  },
  {
    id: 4,
    name: 'Vue.js',
    description: 'Vue.js框架',
    parentId: 2,
    parentName: 'Web前端',
    level: 3,
    icon: 'fab fa-vuejs',
    sortOrder: 75,
    status: 'active',
    courseCount: 10,
    createdAt: '2024-01-04',
    updatedAt: '2024-03-04'
  },
  {
    id: 5,
    name: '后端开发',
    description: '服务器端开发技术',
    parentId: 1,
    parentName: '编程开发',
    level: 2,
    icon: 'fas fa-server',
    sortOrder: 85,
    status: 'active',
    courseCount: 20,
    createdAt: '2024-01-05',
    updatedAt: '2024-03-05'
  },
  {
    id: 6,
    name: '设计创意',
    description: '设计和创意相关课程',
    parentId: null,
    parentName: null,
    level: 1,
    icon: 'fas fa-palette',
    sortOrder: 95,
    status: 'active',
    courseCount: 30,
    createdAt: '2024-01-06',
    updatedAt: '2024-03-06'
  },
  {
    id: 7,
    name: 'UI设计',
    description: '用户界面设计',
    parentId: 6,
    parentName: '设计创意',
    level: 2,
    icon: 'fas fa-paint-brush',
    sortOrder: 70,
    status: 'active',
    courseCount: 18,
    createdAt: '2024-01-07',
    updatedAt: '2024-03-07'
  },
  {
    id: 8,
    name: '数据科学',
    description: '数据分析和机器学习',
    parentId: null,
    parentName: null,
    level: 1,
    icon: 'fas fa-chart-bar',
    sortOrder: 90,
    status: 'disabled',
    courseCount: 12,
    createdAt: '2024-01-08',
    updatedAt: '2024-03-08'
  }
])

// 筛选后的分类
const filteredCategories = computed(() => {
  let result = [...categories.value]
  
  // 状态筛选
  if (filters.value.status) {
    result = result.filter(category => category.status === filters.value.status)
  }
  
  // 层级筛选
  if (filters.value.level) {
    result = result.filter(category => category.level === parseInt(filters.value.level))
  }
  
  // 课程数量筛选
  if (filters.value.courseCount) {
    const count = filters.value.courseCount
    if (count === '0') {
      result = result.filter(category => (category.courseCount || 0) === 0)
    } else if (count === '1-10') {
      result = result.filter(category => (category.courseCount || 0) >= 1 && (category.courseCount || 0) <= 10)
    } else if (count === '11-50') {
      result = result.filter(category => (category.courseCount || 0) >= 11 && (category.courseCount || 0) <= 50)
    } else if (count === '50+') {
      result = result.filter(category => (category.courseCount || 0) > 50)
    }
  }
  
  // 搜索筛选
  if (filters.value.search) {
    const search = filters.value.search.toLowerCase()
    result = result.filter(category => 
      category.name.toLowerCase().includes(search) ||
      (category.description && category.description.toLowerCase().includes(search))
    )
  }
  
  // 排序
  result.sort((a, b) => {
    const field = filters.value.sortBy
    if (field === 'created_at' || field === 'updated_at') {
      return new Date(b[field.replace('_', '')]) - new Date(a[field.replace('_', '')])
    } else if (field === 'course_count') {
      return (b.courseCount || 0) - (a.courseCount || 0)
    } else if (field === 'sort_order') {
      return (b.sortOrder || 0) - (a.sortOrder || 0)
    } else {
      return a.name.localeCompare(b.name)
    }
  })
  
  return result
})

// 分页后的分类
const paginatedCategories = computed(() => {
  if (viewMode.value === 'tree') return filteredCategories.value
  
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredCategories.value.slice(start, end)
})

// 树形数据
const treeData = computed(() => {
  const buildTree = (parentId = null) => {
    return filteredCategories.value
      .filter(category => category.parentId === parentId)
      .map(category => ({
        ...category,
        children: buildTree(category.id)
      }))
  }
  return buildTree()
})

// 可选父分类
const availableParents = computed(() => {
  return categories.value.filter(category => 
    category.level < 3 && 
    (!editingCategory.value || category.id !== editingCategory.value.id)
  )
})

// 获取层级名称
const getLevelName = (level) => {
  const names = {
    1: '一级',
    2: '二级',
    3: '三级'
  }
  return names[level] || '未知'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 重置筛选
const resetFilters = () => {
  filters.value = {
    status: '',
    level: '',
    courseCount: '',
    sortBy: 'created_at',
    search: ''
  }
}

// 应用筛选
const applyFilters = () => {
  currentPage.value = 1
  ElMessage.success('筛选条件已应用')
}

// 处理树形选择
const handleTreeCheck = (data, checked) => {
  console.log('树形选择:', data, checked)
}

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedCategories.value = selection.map(category => category.id)
}

// 切换分类选择
const toggleCategorySelection = (categoryId) => {
  const index = selectedCategories.value.indexOf(categoryId)
  if (index > -1) {
    selectedCategories.value.splice(index, 1)
  } else {
    selectedCategories.value.push(categoryId)
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

// 添加子分类
const addSubCategory = (parentCategory) => {
  if (parentCategory.level >= 3) {
    ElMessage.warning('最多支持三级分类')
    return
  }
  
  categoryForm.value = {
    name: '',
    description: '',
    parentId: parentCategory.id,
    icon: 'fas fa-folder',
    sortOrder: 0,
    status: 'active'
  }
  editingCategory.value = null
  showCreateDialog.value = true
}

// 编辑分类
const editCategory = (category) => {
  categoryForm.value = {
    name: category.name,
    description: category.description || '',
    parentId: category.parentId,
    icon: category.icon || 'fas fa-folder',
    sortOrder: category.sortOrder || 0,
    status: category.status
  }
  editingCategory.value = category
  showCreateDialog.value = true
}

// 取消编辑
const cancelEdit = () => {
  showCreateDialog.value = false
  editingCategory.value = null
  categoryForm.value = {
    name: '',
    description: '',
    parentId: null,
    icon: 'fas fa-folder',
    sortOrder: 0,
    status: 'active'
  }
}

// 保存分类
const saveCategory = () => {
  // 简单验证
  if (!categoryForm.value.name) {
    ElMessage.error('请输入分类名称')
    return
  }
  
  if (editingCategory.value) {
    // 编辑模式
    const index = categories.value.findIndex(c => c.id === editingCategory.value.id)
    if (index > -1) {
      const level = categoryForm.value.parentId ? 
        (categories.value.find(c => c.id === categoryForm.value.parentId)?.level || 0) + 1 : 1
      
      categories.value[index] = {
        ...categories.value[index],
        ...categoryForm.value,
        level,
        parentName: categoryForm.value.parentId ? 
          categories.value.find(c => c.id === categoryForm.value.parentId)?.name : null,
        updatedAt: new Date().toISOString().split('T')[0]
      }
      ElMessage.success('分类更新成功')
    }
  } else {
    // 创建模式
    const level = categoryForm.value.parentId ? 
      (categories.value.find(c => c.id === categoryForm.value.parentId)?.level || 0) + 1 : 1
    
    const newCategory = {
      id: Date.now(),
      ...categoryForm.value,
      level,
      parentName: categoryForm.value.parentId ? 
        categories.value.find(c => c.id === categoryForm.value.parentId)?.name : null,
      courseCount: 0,
      createdAt: new Date().toISOString().split('T')[0],
      updatedAt: new Date().toISOString().split('T')[0]
    }
    
    categories.value.unshift(newCategory)
    ElMessage.success('分类创建成功')
  }
  
  cancelEdit()
}

// 切换状态
const toggleStatus = (category) => {
  const action = category.status === 'active' ? '禁用' : '启用'
  ElMessageBox.confirm(
    `确定要${action}分类 ${category.name} 吗？`,
    `${action}分类`,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    category.status = category.status === 'active' ? 'disabled' : 'active'
    category.updatedAt = new Date().toISOString().split('T')[0]
    ElMessage.success(`分类${action}成功`)
  }).catch(() => {
    ElMessage.info(`已取消${action}`)
  })
}

// 移动分类
const moveCategory = (category) => {
  ElMessage.info('移动分类功能开发中')
}

// 删除分类
const deleteCategory = (category) => {
  // 检查是否有子分类
  const hasChildren = categories.value.some(c => c.parentId === category.id)
  if (hasChildren) {
    ElMessage.warning('该分类下还有子分类，请先删除子分类')
    return
  }
  
  // 检查是否有课程
  if (category.courseCount > 0) {
    ElMessage.warning('该分类下还有课程，请先移动或删除课程')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除分类 ${category.name} 吗？此操作不可恢复。`,
    '删除分类',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    }
  ).then(() => {
    const index = categories.value.findIndex(c => c.id === category.id)
    if (index > -1) {
      categories.value.splice(index, 1)
      ElMessage.success('分类删除成功')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 批量启用
const batchEnable = () => {
  ElMessageBox.confirm(
    `确定要启用选中的 ${selectedCategories.value.length} 个分类吗？`,
    '批量启用',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    categories.value.forEach(category => {
      if (selectedCategories.value.includes(category.id)) {
        category.status = 'active'
        category.updatedAt = new Date().toISOString().split('T')[0]
      }
    })
    selectedCategories.value = []
    ElMessage.success('批量启用成功')
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 批量禁用
const batchDisable = () => {
  ElMessageBox.confirm(
    `确定要禁用选中的 ${selectedCategories.value.length} 个分类吗？`,
    '批量禁用',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    categories.value.forEach(category => {
      if (selectedCategories.value.includes(category.id)) {
        category.status = 'disabled'
        category.updatedAt = new Date().toISOString().split('T')[0]
      }
    })
    selectedCategories.value = []
    ElMessage.success('批量禁用成功')
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 批量删除
const batchDelete = () => {
  // 检查选中的分类是否可以删除
  const cannotDelete = selectedCategories.value.filter(id => {
    const category = categories.value.find(c => c.id === id)
    const hasChildren = categories.value.some(c => c.parentId === id)
    return hasChildren || (category && category.courseCount > 0)
  })
  
  if (cannotDelete.length > 0) {
    ElMessage.warning('部分分类下还有子分类或课程，无法删除')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedCategories.value.length} 个分类吗？此操作不可恢复。`,
    '批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    }
  ).then(() => {
    categories.value = categories.value.filter(category => !selectedCategories.value.includes(category.id))
    selectedCategories.value = []
    ElMessage.success('批量删除成功')
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

onMounted(() => {
  // 组件挂载时的初始化逻辑
})
</script>

<style scoped>
.category-management-container {
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

.stat-card.active .stat-icon {
  background: linear-gradient(135deg, #28a745, #20c997);
}

.stat-card.courses .stat-icon {
  background: linear-gradient(135deg, #ffc107, #fd7e14);
}

.stat-card.popular .stat-icon {
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

/* 分类列表 */
.categories-section {
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

.category-count {
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

/* 树形视图 */
.tree-view {
  max-width: 1400px;
  margin: 0 auto;
}

.tree-container {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 
    15px 15px 30px rgba(0, 0, 0, 0.1),
    -15px -15px 30px rgba(255, 255, 255, 0.8);
}

.category-tree {
  width: 100%;
}

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.tree-node:hover {
  background: rgba(0, 47, 167, 0.05);
}

.node-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.node-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #002FA7, #517B4D);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
}

.node-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.node-name {
  font-weight: 600;
  color: #2c3e50;
}

.node-meta {
  display: flex;
  gap: 12px;
  align-items: center;
}

.course-count {
  color: #6b7280;
  font-size: 12px;
}

.node-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.tree-node:hover .node-actions {
  opacity: 1;
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

.categories-table {
  width: 100%;
}

.category-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.category-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #002FA7, #517B4D);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
}

.category-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.category-name {
  font-weight: 600;
  color: #2c3e50;
}

.category-description {
  color: #6b7280;
  font-size: 12px;
}

.text-muted {
  color: #9ca3af;
}

.course-count {
  color: #002FA7;
  font-weight: 600;
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

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.category-card {
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

.category-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    20px 20px 40px rgba(0, 0, 0, 0.15),
    -20px -20px 40px rgba(255, 255, 255, 0.9);
}

.category-card.selected {
  border: 2px solid #002FA7;
  background: rgba(0, 47, 167, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.category-basic {
  flex: 1;
}

.category-level {
  color: #6b7280;
  font-size: 12px;
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

.category-description {
  color: #6b7280;
  font-size: 14px;
  line-height: 1.5;
}

.category-meta {
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

.meta-value {
  font-size: 14px;
  color: #2c3e50;
}

.category-stats {
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

.category-dates {
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
    padding: 16px 20px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: space-between;
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
  
  .search-box {
    min-width: auto;
    width: 100%;
  }
  
  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .section-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .categories-grid {
    grid-template-columns: 1fr;
  }
  
  .category-meta {
    flex-direction: column;
    gap: 12px;
  }
  
  .category-stats {
    justify-content: space-around;
  }
  
  .table-actions {
    flex-direction: column;
    gap: 4px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 24px;
  }
  
  .action-btn {
    padding: 8px 16px;
    font-size: 14px;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-number {
    font-size: 24px;
  }
  
  .filter-content {
    padding: 16px;
  }
  
  .categories-section {
    padding: 16px;
  }
  
  .category-card {
    padding: 16px;
  }
  
  .tree-container {
    padding: 16px;
  }
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .category-management-container {
    background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  }
  
  .page-header {
    background: rgba(30, 30, 30, 0.95);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .page-title {
    color: #ffffff;
  }
  
  .page-subtitle {
    color: #a0a0a0;
  }
  
  .stat-card {
    background: rgba(30, 30, 30, 0.9);
    box-shadow: 
      15px 15px 30px rgba(0, 0, 0, 0.3),
      -15px -15px 30px rgba(255, 255, 255, 0.05);
  }
  
  .stat-number {
    color: #ffffff;
  }
  
  .filter-content {
    background: rgba(30, 30, 30, 0.9);
    box-shadow: 
      15px 15px 30px rgba(0, 0, 0, 0.3),
      -15px -15px 30px rgba(255, 255, 255, 0.05);
  }
  
  .filter-label {
    color: #d1d5db;
  }
  
  .section-title h3 {
    color: #ffffff;
  }
  
  .tree-container,
  .table-view,
  .category-card {
    background: rgba(30, 30, 30, 0.9);
    box-shadow: 
      15px 15px 30px rgba(0, 0, 0, 0.3),
      -15px -15px 30px rgba(255, 255, 255, 0.05);
  }
  
  .node-name,
  .category-name {
    color: #ffffff;
  }
  
  .category-description {
    color: #a0a0a0;
  }
}
</style>