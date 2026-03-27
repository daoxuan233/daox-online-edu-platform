<template>
  <div class="category-management-container">
    <!-- 页面头部 -->
    <div class="page-header glass-card" ref="headerRef">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <div class="icon-wrapper">
              <font-awesome-icon :icon="['fas', 'tags']" />
            </div>
            分类管理
          </h1>
          <p class="page-subtitle">管理课程分类和标签体系</p>
        </div>
        <div class="header-actions">
          <el-button class="action-btn glass-btn">
            <font-awesome-icon :icon="['fas', 'upload']" />
            批量导入
          </el-button>
          <el-button class="action-btn glass-btn">
            <font-awesome-icon :icon="['fas', 'download']" />
            导出数据
          </el-button>
          <el-button type="primary" class="action-btn tech-btn" @click="openCreateDialog">
            <font-awesome-icon :icon="['fas', 'plus']" />
            添加分类
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid" ref="statsRef">
        <div class="stat-card glass-card hover-float total">
          <div class="stat-icon gradient-blue">
            <font-awesome-icon :icon="['fas', 'layer-group']" />
          </div>
          <div class="stat-content">
            <div class="stat-number text-gradient-blue">{{ stats.totalCategories }}</div>
            <div class="stat-label">总分类数</div>
            <div class="stat-change positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              +{{ stats.categoriesGrowth }}%
            </div>
          </div>
        </div>
        
        <div class="stat-card glass-card hover-float active">
          <div class="stat-icon gradient-green">
            <font-awesome-icon :icon="['fas', 'check-circle']" />
          </div>
          <div class="stat-content">
            <div class="stat-number text-gradient-green">{{ stats.activeCategories }}</div>
            <div class="stat-label">启用分类</div>
            <div class="stat-change positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              +{{ stats.activeGrowth }}%
            </div>
          </div>
        </div>
        
        <div class="stat-card glass-card hover-float courses">
          <div class="stat-icon gradient-orange">
            <font-awesome-icon :icon="['fas', 'book']" />
          </div>
          <div class="stat-content">
            <div class="stat-number text-gradient-orange">{{ stats.coursesWithCategories }}</div>
            <div class="stat-label">已分类课程</div>
            <div class="stat-change positive">
              <font-awesome-icon :icon="['fas', 'arrow-up']" />
              +{{ stats.coursesGrowth }}%
            </div>
          </div>
        </div>
        
        <div class="stat-card glass-card hover-float popular">
          <div class="stat-icon gradient-purple">
            <font-awesome-icon :icon="['fas', 'fire']" />
          </div>
          <div class="stat-content">
            <div class="stat-number text-gradient-purple">{{ stats.popularCategories }}</div>
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
      <div class="filter-content glass-card" ref="filterRef">
        <div class="filter-left">
          <div class="filter-group">
            <label class="filter-label">分类状态</label>
            <el-select v-model="filters.status" placeholder="全部状态" clearable class="tech-select">
              <el-option label="全部状态" value=""></el-option>
              <el-option label="启用" value="active"></el-option>
              <el-option label="禁用" value="disabled"></el-option>
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">分类层级</label>
            <el-select v-model="filters.level" placeholder="全部层级" clearable class="tech-select">
              <el-option label="全部层级" value=""></el-option>
              <el-option label="一级分类" value="1"></el-option>
              <el-option label="二级分类" value="2"></el-option>
              <el-option label="三级分类" value="3"></el-option>
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">课程数量</label>
            <el-select v-model="filters.courseCount" placeholder="课程数量" clearable class="tech-select">
              <el-option label="全部" value=""></el-option>
              <el-option label="无课程" value="0"></el-option>
              <el-option label="1-10门" value="1-10"></el-option>
              <el-option label="11-50门" value="11-50"></el-option>
              <el-option label="50门以上" value="50+"></el-option>
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">排序方式</label>
            <el-select v-model="filters.sortBy" placeholder="排序方式" class="tech-select">
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

    <!-- 分类列表 -->
    <div class="categories-section">
      <div class="section-header" ref="listHeaderRef">
        <div class="section-title">
          <div class="title-icon"><font-awesome-icon :icon="['fas', 'list']" /></div>
          <h3>分类列表</h3>
          <span class="category-count pulse-badge">共 {{ filteredCategories.length }} 个分类</span>
        </div>
        
        <div class="section-actions">
          <div class="view-toggle">
            <el-radio-group v-model="viewMode" size="small" class="tech-radio">
              <el-radio-button label="grid">
                <font-awesome-icon :icon="['fas', 'th-large']" />
                卡片
              </el-radio-button>
              <el-radio-button label="tree">
                <font-awesome-icon :icon="['fas', 'sitemap']" />
                树形
              </el-radio-button>
              <el-radio-button label="table">
                <font-awesome-icon :icon="['fas', 'list']" />
                列表
              </el-radio-button>
            </el-radio-group>
          </div>
          
          <div class="batch-actions" v-if="selectedCategories.length > 0">
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
      
      <!-- 卡片视图 (全局默认) -->
      <div v-if="viewMode === 'grid'" class="grid-view" ref="listGridRef">
        <div class="categories-grid">
          <div 
            v-for="category in paginatedCategories" 
            :key="category.id"
            class="category-card glass-card hover-float"
            :class="{ selected: selectedCategories.includes(category.id) }"
            @click="toggleCategorySelection(category.id)"
          >
            <div class="card-header">
              <div class="category-icon tech-icon-box">
                <font-awesome-icon :icon="category.icon ? category.icon.split(' ') : ['fas', 'folder']" />
              </div>
              <div class="category-basic">
                <div class="category-name">{{ category.name }}</div>
                <div class="category-level text-muted">{{ getLevelName(category.level) }}</div>
              </div>
              <div class="card-actions">
                <el-dropdown trigger="click" @click.stop>
                  <el-button size="small" circle class="glass-btn">
                    <font-awesome-icon :icon="['fas', 'ellipsis-v']" />
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu class="tech-dropdown">
                      <el-dropdown-item @click="addSubCategory(category)">
                        <font-awesome-icon :icon="['fas', 'plus']" /> 添加子分类
                      </el-dropdown-item>
                      <el-dropdown-item @click="editCategory(category)">
                        <font-awesome-icon :icon="['fas', 'edit']" /> 编辑
                      </el-dropdown-item>
                      <el-dropdown-item @click="toggleStatus(category)" divided>
                        <font-awesome-icon :icon="['fas', category.status === 'active' ? 'ban' : 'check']" />
                        {{ category.status === 'active' ? '禁用' : '启用' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="deleteCategory(category)">
                        <font-awesome-icon :icon="['fas', 'trash']" class="text-danger" /> 删除
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
                  <span :class="['tech-tag', getStatusTagType(category.status)]">
                    {{ category.status === 'active' ? '启用' : '禁用' }}
                  </span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">父分类</span>
                  <span class="meta-value">{{ category.parentName || '无' }}</span>
                </div>
              </div>
              
              <div class="category-stats">
                <div class="stat-item-small">
                  <span class="stat-value text-gradient-blue">{{ category.courseCount || 0 }}</span>
                  <span class="stat-label">课程数</span>
                </div>
                <div class="stat-item-small">
                  <span class="stat-value text-gradient-green">{{ category.sortOrder || 0 }}</span>
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

      <!-- 树形视图 -->
      <div v-else-if="viewMode === 'tree'" class="tree-view glass-card" ref="listTreeRef">
        <div class="tree-container">
          <el-tree
            :data="treeData"
            :props="treeProps"
            node-key="id"
            :default-expand-all="true"
            :show-checkbox="true"
            @check="handleTreeCheck"
            class="category-tree tech-tree"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <div class="node-content">
                  <div class="node-icon tech-icon-box small">
                    <font-awesome-icon :icon="data.icon ? data.icon.split(' ') : ['fas', 'folder']" />
                  </div>
                  <div class="node-info">
                    <div class="node-name">{{ data.name }}</div>
                    <div class="node-meta">
                      <span class="course-count text-gradient-blue">{{ data.courseCount || 0 }} 门课程</span>
                      <span :class="['tech-tag', getStatusTagType(data.status)]">
                        {{ data.status === 'active' ? '启用' : '禁用' }}
                      </span>
                    </div>
                  </div>
                </div>
                <div class="node-actions">
                  <el-button size="small" text class="tech-text-btn" @click.stop="addSubCategory(data)">
                    <font-awesome-icon :icon="['fas', 'plus']" />
                  </el-button>
                  <el-button size="small" text class="tech-text-btn" @click.stop="editCategory(data)">
                    <font-awesome-icon :icon="['fas', 'edit']" />
                  </el-button>
                  <el-button size="small" text class="tech-text-btn danger" @click.stop="deleteCategory(data)">
                    <font-awesome-icon :icon="['fas', 'trash']" />
                  </el-button>
                </div>
              </div>
            </template>
          </el-tree>
        </div>
      </div>
      
      <!-- 表格视图 -->
      <div v-else class="table-view glass-card" ref="listTableRef">
        <el-table
          :data="paginatedCategories"
          @selection-change="handleSelectionChange"
          class="categories-table tech-table"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          
          <el-table-column label="分类信息" min-width="200">
            <template #default="{ row }">
              <div class="category-info">
                <div class="category-icon tech-icon-box small">
                  <font-awesome-icon :icon="row.icon ? row.icon.split(' ') : ['fas', 'folder']" />
                </div>
                <div class="category-details">
                  <div class="category-name">{{ row.name }}</div>
                  <div class="category-description">{{ row.description || '暂无描述' }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="层级" width="100">
            <template #default="{ row }">
              <span class="tech-tag info">{{ getLevelName(row.level) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="父分类" width="120">
            <template #default="{ row }">
              <span v-if="row.parentName">{{ row.parentName }}</span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          
          <el-table-column label="课程数" width="100">
            <template #default="{ row }">
              <span class="course-count text-gradient-blue">{{ row.courseCount || 0 }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="排序" width="80">
            <template #default="{ row }">
              <span>{{ row.sortOrder || 0 }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span :class="['tech-tag', getStatusTagType(row.status)]">
                {{ row.status === 'active' ? '启用' : '禁用' }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button size="small" text class="tech-text-btn" @click="addSubCategory(row)">
                  <font-awesome-icon :icon="['fas', 'plus']" />
                </el-button>
                <el-button size="small" text class="tech-text-btn" @click="editCategory(row)">
                  <font-awesome-icon :icon="['fas', 'edit']" />
                </el-button>
                <el-dropdown trigger="click">
                  <el-button size="small" text class="tech-text-btn">
                    <font-awesome-icon :icon="['fas', 'ellipsis-h']" />
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu class="tech-dropdown">
                      <el-dropdown-item @click="toggleStatus(row)">
                        <font-awesome-icon :icon="['fas', row.status === 'active' ? 'ban' : 'check']" />
                        {{ row.status === 'active' ? '禁用' : '启用' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="moveCategory(row)">
                        <font-awesome-icon :icon="['fas', 'arrows-alt']" /> 移动
                      </el-dropdown-item>
                      <el-dropdown-item @click="deleteCategory(row)" divided>
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
      <div class="pagination-section glass-card" v-if="viewMode !== 'tree'">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredCategories.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="tech-pagination"
        />
      </div>
    </div>

    <!-- 创建/编辑分类对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingCategory ? '编辑分类' : '添加分类'"
      width="600px"
      class="tech-dialog glass-dialog"
      :show-close="false"
      append-to-body
      @open="onDialogOpen"
    >
      <template #header="{ close }">
        <div class="dialog-header">
          <div class="title-icon"><font-awesome-icon :icon="['fas', editingCategory ? 'edit' : 'plus']" /></div>
          <span>{{ editingCategory ? '编辑分类' : '添加分类' }}</span>
          <button class="close-btn" @click="close">
            <font-awesome-icon :icon="['fas', 'times']" />
          </button>
        </div>
      </template>

      <el-form :model="categoryForm" label-width="100px" class="create-form">
        <el-form-item label="分类名称">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" class="tech-input" />
        </el-form-item>
        
        <el-form-item label="父分类">
          <el-select v-model="categoryForm.parentId" placeholder="请选择父分类" clearable class="tech-select w-full">
            <el-option label="无（顶级分类）" :value="null"></el-option>
            <el-option 
              v-for="category in availableParents" 
              :key="category.id"
              :label="category.name"
              :value="category.id"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="排序权重">
          <el-input-number 
            v-model="categoryForm.sortOrder" 
            :min="0" 
            :max="9999"
            placeholder="数值越大越靠前"
            class="tech-input-number"
          />
        </el-form-item>
        
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button class="glass-btn" @click="cancelEdit">取消</el-button>
          <el-button type="primary" class="tech-btn" @click="saveCategory">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { gsap } from 'gsap'
import {
  createAdminCategory,
  deleteAdminCategory,
  getAdminCategoryTree,
  getAdminCourseList,
  updateAdminCategory
} from '@/api/admin/adminManagement'

const headerRef = ref(null)
const statsRef = ref(null)
const filterRef = ref(null)
const listHeaderRef = ref(null)
const listGridRef = ref(null)
const listTableRef = ref(null)
const listTreeRef = ref(null)
let tl = null

const filters = ref({
  status: '',
  level: '',
  courseCount: '',
  sortBy: 'sort_order',
  search: ''
})
const viewMode = ref('grid')
const currentPage = ref(1)
const pageSize = ref(12)
const selectedCategories = ref([])
const showCreateDialog = ref(false)
const editingCategory = ref(null)
const categories = ref([])

/**
 * 分类表单仅保留后端真实支持的字段。
 */
const categoryForm = ref({
  name: '',
  parentId: null,
  sortOrder: 0
})

const treeProps = {
  children: 'children',
  label: 'name'
}

const resolveCategoryIcon = (level) => {
  if (level === 1) return 'fas fa-layer-group'
  if (level === 2) return 'fas fa-folder-tree'
  return 'fas fa-folder'
}

/**
 * 将后端树形分类转成前端管理视图。
 *
 * @param {Array} nodes 分类树节点
 * @param {number} level 当前层级
 * @param {string|null} parentName 父分类名称
 * @returns {Array} 扁平分类数组
 */
const flattenCategoryTree = (nodes = [], level = 1, parentName = null) => nodes.flatMap(node => {
  const currentNode = {
    id: node.id,
    name: node.name,
    parentId: node.parentId || null,
    parentName,
    level,
    icon: resolveCategoryIcon(level),
    sortOrder: node.orderIndex || 0,
    status: 'active',
    courseCount: 0,
    description: level === 1 ? '平台一级课程分类' : `归属于 ${parentName || '顶级分类'} 的课程分类`,
    createdAt: '',
    updatedAt: ''
  }
  return [currentNode, ...flattenCategoryTree(node.children || [], level + 1, node.name)]
})

const attachCourseCount = (flatCategories, courses) => {
  const categoryMap = new Map(flatCategories.map(item => [item.id, { ...item }]))
  const parentMap = new Map(flatCategories.map(item => [item.id, item.parentId]))

  courses.forEach(course => {
    let currentId = course.categoryId
    while (currentId && categoryMap.has(currentId)) {
      const currentCategory = categoryMap.get(currentId)
      currentCategory.courseCount = (currentCategory.courseCount || 0) + 1
      currentId = parentMap.get(currentId)
    }
  })

  return Array.from(categoryMap.values())
}

/**
 * 拉取分类树和课程列表，并完成前端统计映射。
 */
const loadCategories = async () => {
  try {
    const [tree, courseList] = await Promise.all([getAdminCategoryTree(), getAdminCourseList()])
    const flatCategories = flattenCategoryTree(Array.isArray(tree) ? tree : [])
    categories.value = attachCourseCount(flatCategories, Array.isArray(courseList) ? courseList : [])
    await nextTick()
    animateList()
  } catch (error) {
    console.error('管理员分类数据加载失败:', error)
    ElMessage.error('分类数据加载失败，请稍后重试')
  }
}

const stats = computed(() => {
  const totalCategories = categories.value.length
  const activeCategories = categories.value.length
  const coursesWithCategories = categories.value.reduce((sum, item) => sum + (item.courseCount || 0), 0)
  const popularCategories = categories.value.filter(item => (item.courseCount || 0) >= 10).length
  return {
    totalCategories,
    categoriesGrowth: totalCategories ? 100 : 0,
    activeCategories,
    activeGrowth: totalCategories ? 100 : 0,
    coursesWithCategories,
    coursesGrowth: totalCategories ? Number((coursesWithCategories / totalCategories).toFixed(1)) : 0,
    popularCategories,
    popularGrowth: totalCategories ? Number(((popularCategories / totalCategories) * 100).toFixed(1)) : 0
  }
})

const filteredCategories = computed(() => {
  let result = [...categories.value]
  if (filters.value.status) {
    result = result.filter(category => category.status === filters.value.status)
  }
  if (filters.value.level) {
    result = result.filter(category => category.level === Number(filters.value.level))
  }
  if (filters.value.courseCount) {
    const count = filters.value.courseCount
    if (count === '0') result = result.filter(category => (category.courseCount || 0) === 0)
    if (count === '1-10') result = result.filter(category => (category.courseCount || 0) >= 1 && (category.courseCount || 0) <= 10)
    if (count === '11-50') result = result.filter(category => (category.courseCount || 0) >= 11 && (category.courseCount || 0) <= 50)
    if (count === '50+') result = result.filter(category => (category.courseCount || 0) > 50)
  }
  if (filters.value.search) {
    const search = filters.value.search.toLowerCase()
    result = result.filter(category =>
      category.name.toLowerCase().includes(search) ||
      category.description.toLowerCase().includes(search)
    )
  }
  result.sort((a, b) => {
    if (filters.value.sortBy === 'course_count') return (b.courseCount || 0) - (a.courseCount || 0)
    if (filters.value.sortBy === 'name') return a.name.localeCompare(b.name)
    return (b.sortOrder || 0) - (a.sortOrder || 0)
  })
  return result
})

const paginatedCategories = computed(() => {
  if (viewMode.value === 'tree') return filteredCategories.value
  const start = (currentPage.value - 1) * pageSize.value
  return filteredCategories.value.slice(start, start + pageSize.value)
})

const treeData = computed(() => {
  const buildTree = (parentId = null) => filteredCategories.value
    .filter(category => category.parentId === parentId)
    .map(category => ({ ...category, children: buildTree(category.id) }))
  return buildTree()
})

const availableParents = computed(() => categories.value.filter(category =>
  category.level < 3 && (!editingCategory.value || category.id !== editingCategory.value.id)
))

const getLevelName = (level) => ({ 1: '一级', 2: '二级', 3: '三级' }[level] || '未知')
const getStatusTagType = (status) => (status === 'active' ? 'success' : 'danger')
const formatDate = (date) => (date ? new Date(date).toLocaleDateString('zh-CN') : '-')

const resetFilters = () => {
  filters.value = { status: '', level: '', courseCount: '', sortBy: 'sort_order', search: '' }
  currentPage.value = 1
}

const applyFilters = () => {
  currentPage.value = 1
  animateList()
}

const handleTreeCheck = (_data, checked) => {
  selectedCategories.value = checked.checkedKeys
}

const handleSelectionChange = (selection) => {
  selectedCategories.value = selection.map(category => category.id)
}

const toggleCategorySelection = (categoryId) => {
  const index = selectedCategories.value.indexOf(categoryId)
  if (index > -1) selectedCategories.value.splice(index, 1)
  else selectedCategories.value.push(categoryId)
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

const resetCategoryForm = () => {
  categoryForm.value = { name: '', parentId: null, sortOrder: 0 }
  editingCategory.value = null
}

const openCreateDialog = () => {
  resetCategoryForm()
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

const addSubCategory = (parentCategory) => {
  if (parentCategory.level >= 3) {
    ElMessage.warning('最多支持三级分类')
    return
  }
  categoryForm.value = {
    name: '',
    parentId: parentCategory.id,
    sortOrder: 0
  }
  editingCategory.value = null
  showCreateDialog.value = true
}

const editCategory = (category) => {
  categoryForm.value = {
    name: category.name,
    parentId: category.parentId,
    sortOrder: category.sortOrder || 0
  }
  editingCategory.value = category
  showCreateDialog.value = true
}

const cancelEdit = () => {
  showCreateDialog.value = false
  resetCategoryForm()
}

const saveCategory = async () => {
  if (!categoryForm.value.name) {
    ElMessage.error('请输入分类名称')
    return
  }
  try {
    const payload = {
      name: categoryForm.value.name.trim(),
      parentId: categoryForm.value.parentId,
      orderIndex: categoryForm.value.sortOrder || 0
    }
    if (editingCategory.value) {
      await updateAdminCategory(editingCategory.value.id, payload)
      ElMessage.success('分类更新成功')
    } else {
      await createAdminCategory(payload)
      ElMessage.success('分类创建成功')
    }
    cancelEdit()
    await loadCategories()
  } catch (error) {
    console.error('分类保存失败:', error)
    ElMessage.error(error?.message || '分类保存失败')
  }
}

const toggleStatus = () => {
  ElMessage.info('当前后端课程分类未提供启停字段，前端已切换为真实结构展示模式')
}

const moveCategory = () => {
  ElMessage.info('当前版本未提供分类移动接口，请通过排序权重调整顺序')
}

const deleteCategory = async (category) => {
  try {
    await ElMessageBox.confirm(`确定要删除分类 ${category.name} 吗？`, '删除分类', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    await deleteAdminCategory(category.id)
    ElMessage.success('分类删除成功')
    await loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('分类删除失败:', error)
      ElMessage.error(error?.message || '分类删除失败，请确认分类下没有子分类和课程')
    }
  }
}

const batchEnable = () => {
  ElMessage.info('当前后端课程分类未提供批量启停能力')
}

const batchDisable = () => {
  ElMessage.info('当前后端课程分类未提供批量启停能力')
}

const batchDelete = async () => {
  if (!selectedCategories.value.length) {
    ElMessage.warning('请先选择分类')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedCategories.value.length} 个分类吗？`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    for (const categoryId of selectedCategories.value) {
      await deleteAdminCategory(categoryId)
    }
    selectedCategories.value = []
    ElMessage.success('批量删除成功')
    await loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error(error?.message || '批量删除失败，请确认分类下没有子分类和课程')
    }
  }
}

const animateList = () => {
  nextTick(() => {
    if (viewMode.value === 'grid' && listGridRef.value) {
      gsap.fromTo(listGridRef.value.querySelectorAll('.category-card'),
        { y: 30, opacity: 0 },
        { y: 0, opacity: 1, duration: 0.4, stagger: 0.05, ease: 'power2.out', overwrite: true }
      )
    } else if (viewMode.value === 'table' && listTableRef.value) {
      const body = listTableRef.value.querySelector('.el-table__body-wrapper')
      if (body) {
        gsap.fromTo(body, { opacity: 0 }, { opacity: 1, duration: 0.5, ease: 'power2.out', overwrite: true })
      }
    } else if (viewMode.value === 'tree' && listTreeRef.value) {
      gsap.fromTo(listTreeRef.value.querySelectorAll('.tree-node'),
        { x: -20, opacity: 0 },
        { x: 0, opacity: 1, duration: 0.4, stagger: 0.05, ease: 'power2.out', overwrite: true }
      )
    }
  })
}

watch(viewMode, () => {
  animateList()
})

onMounted(async () => {
  await loadCategories()
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
.category-management-container {
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
  gap: 8px;
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

/* 列表头部 */
.categories-section { margin-bottom: 24px; }

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
.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.category-card {
  padding: 24px;
  cursor: pointer;
}

.category-card.selected {
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

.tech-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #0061ff, #60efff);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(0, 97, 255, 0.3);
}

.tech-icon-box.small {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  font-size: 14px;
}

.category-basic { flex: 1; }
.category-name { font-weight: 600; color: #1e293b; font-size: 16px; }
.category-level { font-size: 13px; margin-top: 2px; }

.card-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.category-description {
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
  min-height: 40px;
}

.category-meta {
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
.meta-value { font-size: 14px; font-weight: 500; color: #1e293b; }

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

.category-stats {
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

.category-dates {
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

/* 树形视图 */
.tree-view {
  padding: 24px;
}

.tech-tree {
  background: transparent;
}

:deep(.el-tree-node__content) {
  height: auto;
  padding: 8px 0;
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-tree-node__content:hover) {
  background: rgba(0, 97, 255, 0.05);
}

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 16px;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.node-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.node-name { font-weight: 600; color: #1e293b; }

.node-meta {
  display: flex;
  gap: 12px;
  align-items: center;
}

.node-actions {
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  gap: 4px;
}

.tree-node:hover .node-actions {
  opacity: 1;
}

/* 表格视图 */
.table-view {
  padding: 24px;
}

.category-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.category-details {
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
.tech-text-btn.danger:hover { color: #ef4444; background: rgba(239, 68, 68, 0.1); }

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
</style>
