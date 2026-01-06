<template>
  <div class="notes-page notes-container">
    <!-- 页面头部 -->
    <div class="notes-header neumorphism-raised">
      <div class="header-content">
        <div class="header-left">
          <div class="page-title">
            <font-awesome-icon :icon="['fas', 'sticky-note']" class="title-icon"/>
            <h1>学习笔记</h1>
          </div>
          <p class="page-subtitle">记录学习心得，整理知识要点</p>
        </div>
        <div class="header-actions">
          <button class="action-btn neumorphism-raised" @click="createNewNote">
            <font-awesome-icon :icon="['fas', 'plus']"/>
            <span>新建笔记</span>
          </button>
          <button class="action-btn save-btn" @click="saveNote" :disabled="isSaving">
            <font-awesome-icon :icon="['fas', 'save']" :class="{ 'fa-spin': isSaving }"/>
            <span>{{ isSaving ? '保存中...' : '保存笔记' }}</span>
          </button>
          <button class="action-btn refresh-btn neumorphism-raised" @click="refreshNotes" :disabled="isLoading">
            <font-awesome-icon :icon="['fas', 'sync-alt']" :class="{ 'fa-spin': isLoading }"/>
            <span>{{ isLoading ? '加载中...' : '刷新' }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 笔记列表和编辑器 -->
    <div class="notes-main" style="padding-left: 0;">
      <!-- 左侧笔记列表 -->
      <div class="notes-sidebar frosted-glass" :class="{ 'collapsed': sidebarCollapsed }">
        <div class="sidebar-header">
          <div class="search-container">
            <div class="search-wrapper neumorphism-inset">
              <font-awesome-icon :icon="['fas', 'search']" class="search-icon"/>
              <input
                  v-model="searchQuery"
                  placeholder="搜索笔记..."
                  class="search-input"
              />
            </div>
          </div>
          <div class="main-tabs">
            <div 
              class="main-tab-item neumorphism-raised" 
              :class="{ 'active': activeFilter === 'inbox' }"
              @click="setActiveFilter('inbox')"
            >
              <div class="tab-content">
                <font-awesome-icon :icon="['fas', 'lightbulb']" class="tab-icon inbox-icon"/>
                <div class="tab-info">
                  <span class="tab-label">灵感池</span>
                  <span class="tab-count" v-if="getFilterCount('inbox') > 0">{{ getFilterCount('inbox') }}</span>
                </div>
              </div>
            </div>
            
            <div 
              class="main-tab-item neumorphism-raised" 
              :class="{ 'active': activeFilter !== 'inbox' }"
              @click="setActiveFilter('free')"
            >
              <div class="tab-content">
                <font-awesome-icon :icon="['fas', 'sticky-note']" class="tab-icon free-icon"/>
                <div class="tab-info">
                  <span class="tab-label">自由笔记</span>
                  <div class="mini-counts" v-if="activeFilter !== 'inbox'">
                     <span class="mini-count" title="自创">{{ getFilterCount('free') }}</span>
                     <span class="divider">/</span>
                     <span class="mini-count" title="课程">{{ getFilterCount('course') }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 二级分类 (仅在自由笔记模式下显示) -->
          <div class="sub-tabs-container" v-if="activeFilter !== 'inbox'">
             <div class="sub-tabs-group neumorphism-inset">
               <button 
                 class="sub-tab-btn" 
                 :class="{ 'active': activeFilter === 'free' || activeFilter === 'all' || activeFilter === 'recent' }"
                 @click="setActiveFilter('free')"
               >
                 <span class="sub-label">自创</span>
                 <span class="sub-count-badge">{{ getFilterCount('free') }}</span>
               </button>
               <button 
                 class="sub-tab-btn" 
                 :class="{ 'active': activeFilter === 'course' }"
                 @click="setActiveFilter('course')"
               >
                 <span class="sub-label">课程</span>
                 <span class="sub-count-badge">{{ getFilterCount('course') }}</span>
               </button>
             </div>
          </div>

          <div v-if="activeFilter === 'inbox' && inboxTagCloud.length" class="inbox-tag-cloud">
            <button
                v-if="inboxSelectedTag"
                class="tag-chip clear-chip"
                @click="inboxSelectedTag = ''"
            >
              <font-awesome-icon :icon="['fas', 'xmark']"/>
              清除
            </button>
            <button
                v-for="item in inboxTagCloud"
                :key="item.tag"
                class="tag-chip"
                :class="{ active: inboxSelectedTag === item.tag }"
                :style="{ fontSize: `${item.size}px` }"
                @click="inboxSelectedTag = item.tag"
            >
              <span class="tag-text">{{ item.tag }}</span>
              <span class="tag-count">{{ item.count }}</span>
            </button>
          </div>
        </div>

        <div class="notes-list" :class="{ 'card-view': viewMode === 'card', 'list-view': viewMode === 'list' }">
          <!-- 加载状态 -->
          <div v-if="isListLoading" class="loading-state">
            <div class="loading-content">
              <font-awesome-icon :icon="['fas', 'spinner']" class="fa-spin loading-icon"/>
              <p>{{ listLoadingText }}</p>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-else-if="filteredNotes.length === 0" class="empty-notes-state">
            <div class="empty-content">
              <div class="empty-illustration">
                <font-awesome-icon :icon="['fas', 'book-open']" class="empty-icon-primary"/>
                <font-awesome-icon :icon="['fas', 'pen-fancy']" class="empty-icon-secondary"/>
              </div>
              <h3 class="empty-title">
                {{ searchQuery ? '未找到相关笔记' : 
                   activeFilter === 'course' ? '当前没有课程笔记' : activeFilter === 'inbox' ? '当前没有灵感池笔记' : activeFilter === 'free' ? '当前没有自主笔记' : '当前没有自由笔记' }}
              </h3>
              <p class="empty-description">
                {{ searchQuery 
                  ? '尝试调整搜索关键词或清空搜索条件查看所有笔记' 
                  : activeFilter === 'course' 
                    ? '课程笔记将显示您在学习过程中记录的重要内容和心得体会' 
                    : activeFilter === 'inbox'
                      ? '灵感池用于快速收集灵感与碎片想法，支持按标签聚合'
                      : activeFilter === 'free'
                        ? '自主笔记不绑定课程，可随时归档与整理'
                    : '记录学习心得，整理知识要点，让学习更有条理' 
                }}
              </p>
              <div class="empty-actions">
                <button v-if="!searchQuery" class="primary-action-btn neumorphism-raised" @click="createNewNote">
                  <font-awesome-icon :icon="['fas', 'plus']"/>
                  <span>创建新笔记</span>
                </button>
                <button v-if="searchQuery" class="secondary-action-btn neumorphism-raised" @click="searchQuery = ''">
                  <font-awesome-icon :icon="['fas', 'times']"/>
                  <span>清空搜索</span>
                </button>
              </div>
              <div v-if="!searchQuery" class="empty-tips">
                <div class="tip-item">
                  <font-awesome-icon :icon="['fas', 'lightbulb']" class="tip-icon"/>
                  <span>支持 Markdown 语法，让笔记更丰富</span>
                </div>
                <div class="tip-item">
                  <font-awesome-icon :icon="['fas', 'tags']" class="tip-icon"/>
                  <span>添加标签分类，便于快速查找</span>
                </div>
                <div class="tip-item">
                  <font-awesome-icon :icon="['fas', 'cloud']" class="tip-icon"/>
                  <span>自动保存，随时随地访问你的笔记</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 笔记列表 -->
          <div
              v-else
              v-for="note in filteredNotes"
              :key="note.id"
              class="note-item neumorphism-raised"
              :class="{ 'active': selectedNote?.id === note.id, 'card-item': viewMode === 'card', 'list-item': viewMode === 'list' }"
              @click="selectNote(note)"
              @mouseenter="handleNoteHover(note.id)"
              @mouseleave="handleNoteLeave"
          >
            <!-- 悬浮预览层 -->
            <transition name="fade">
              <div v-if="hoveredNoteId === note.id && showPreview" class="note-tooltip neumorphism-raised">
                <div class="tooltip-content">{{ getPreviewText(note.content) }}</div>
                <div class="tooltip-arrow"></div>
              </div>
            </transition>

            <div class="note-header">
              <div class="note-title-row">
                <h3 class="note-title" :title="note.title || '无标题笔记'">{{ note.title || '无标题笔记' }}</h3>
                <span class="note-date">{{ formatDate(note.updatedAt) }}</span>
              </div>
              
              <div class="note-tags" v-if="note.tags && note.tags.length">
                <span
                    v-for="tag in note.tags.slice(0, 3)"
                    :key="tag"
                    class="note-tag"
                >
                  {{ tag }}
                </span>
                <span v-if="note.tags.length > 3" class="note-tag-more">+{{ note.tags.length - 3 }}</span>
              </div>

              <div class="note-actions">
                <template v-if="activeFilter === 'inbox'">
                  <button
                      class="note-action-btn archive-btn"
                      @click.stop="archiveInboxToFree(note)"
                      title="归档到自由笔记"
                  >
                    <font-awesome-icon :icon="['fas', 'box-archive']"/>
                  </button>
                </template>
                <template v-else>
                  <button class="note-action-btn" @click.stop="toggleNoteFavorite(note)" :title="note.isFavorite ? '取消收藏' : '收藏'">
                    <font-awesome-icon
                        :icon="note.isFavorite ? ['fas', 'heart'] : ['far', 'heart']"
                        :class="{ 'favorite': note.isFavorite }"
                    />
                  </button>
                </template>
                <button class="note-action-btn delete-btn" @click.stop="deleteNote(note)" title="删除笔记">
                  <font-awesome-icon :icon="['fas', 'trash']"/>
                </button>
              </div>
            </div>
          </div>
        </div>

        <button class="sidebar-toggle" @click="toggleSidebar">
          <font-awesome-icon :icon="sidebarCollapsed ? ['fas', 'chevron-right'] : ['fas', 'chevron-left']"/>
        </button>
      </div>

      <!-- 右侧编辑器 -->
      <div class="editor-container">

        
        <div v-if="selectedNote" class="editor-wrapper neumorphism-raised">
          <!-- 编辑器头部 -->
          <div class="editor-header frosted-glass">
            <div class="editor-title">
              <input
                  v-model="selectedNote.title"
                  placeholder="请输入笔记标题..."
                  class="title-input"
                  @blur="saveNote"
              />
            </div>
            <div class="editor-actions">

              <div class="action-buttons">
                <button class="action-btn neumorphism-raised" @click="exportNote">
                  <font-awesome-icon :icon="['fas', 'download']"/>
                  <span>导出</span>
                </button>
                <button class="action-btn immersive-btn" @click="toggleImmersiveMode" :class="{ 'active': isImmersiveMode }">
                  <font-awesome-icon :icon="isImmersiveMode ? ['fas', 'compress'] : ['fas', 'expand']"/>
                  <span>{{ isImmersiveMode ? '退出道' : '道模式' }}</span><!--专注模式-->
                </button>
              </div>
            </div>
          </div>

          <!-- Markdown 编辑器 -->
          <div class="editor-content">
            <MdEditor
                v-model="selectedNote.content"
                :preview="editorMode !== 'edit'"
                :preview-only="editorMode === 'preview'"
                :editor-id="'notes-editor'"
                :theme="editorTheme"
                :language="'zh-CN'"
                :toolbars="toolbars"
                :footers="footers"
                @save="saveNote"
                @upload-img="handleImageUpload"
                @on-fullscreen="handleFullscreen"
                @on-exit-fullscreen="handleExitFullscreen"
                class="md-editor"
            />
          </div>

          <!-- 笔记标签编辑 -->
          <div class="note-footer frosted-glass">
            <div class="tags-section">
              <label class="tags-label">
                <font-awesome-icon :icon="['fas', 'tags']"/>
                标签：
              </label>
              <div class="tags-input-wrapper">
                <div class="tags-display">
                  <span
                      v-for="(tag, index) in selectedNote.tags"
                      :key="index"
                      class="tag-item"
                  >
                    {{ tag }}
                    <button class="tag-remove" @click="removeTag(index)">
                      <font-awesome-icon :icon="['fas', 'times']"/>
                    </button>
                  </span>
                </div>
                <input
                    v-model="newTag"
                    placeholder="添加标签..."
                    class="tag-input"
                    @keyup.enter="addTag"
                    @blur="addTag"
                />
              </div>
            </div>
            <div class="note-stats">
              <span class="stat-item">
                <font-awesome-icon :icon="['fas', 'clock']"/>
                最后编辑：{{ formatDate(selectedNote.updatedAt) }}
              </span>
              <span class="stat-item">
                <font-awesome-icon :icon="['fas', 'file-alt']"/>
                字数：{{ getWordCount(selectedNote.content) }}
              </span>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="editor-empty-state neumorphism-raised">
          <div class="empty-content">
            <div class="empty-illustration">
              <font-awesome-icon :icon="['fas', 'edit']" class="empty-icon-primary"/>
            </div>
            <h3 class="empty-title">选择或创建一个笔记</h3>
            <p class="empty-description">
              在左侧选择一个笔记开始编辑，或创建一个新的笔记开始记录你的学习心得
            </p>
            <div class="empty-actions">
              <button class="primary-action-btn neumorphism-raised" @click="createNewNote">
                <font-awesome-icon :icon="['fas', 'plus']"/>
                <span>创建新笔记</span>
              </button>
            </div>
            <div class="empty-tips">
              <div class="tip-item">
                <font-awesome-icon :icon="['fas', 'keyboard']" class="tip-icon"/>
                <span>使用快捷键 Ctrl+S 快速保存</span>
              </div>
              <div class="tip-item">
                <font-awesome-icon :icon="['fas', 'expand']" class="tip-icon"/>
                <span>点击专注模式获得更好的编辑体验</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 保存笔记弹窗 -->
    <el-dialog
        v-model="saveDialogVisible"
        title="保存笔记"
        width="600px"
        class="save-note-dialog frosted-glass"
        :close-on-click-modal="false"
        :close-on-press-escape="true"
    >
      <el-form
          :model="saveForm"
          label-width="100px"
          class="save-note-form"
      >
        <el-form-item label="所属课程" class="form-item-custom">
          <el-select
              v-model="saveForm.courseId"
              placeholder="请选择课程（可选）"
              clearable
              class="custom-select neumorphism-inset"
              @change="handleCourseChange"
          >
            <el-option
                v-for="course in courses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item
            v-if="saveForm.courseId"
            label="所属章节"
            class="form-item-custom"
        >
          <el-select
              v-model="saveForm.chapterId"
              placeholder="请选择章节（可选）"
              clearable
              class="custom-select neumorphism-inset"
              @change="handleChapterChange"
          >
            <el-option
                v-for="chapter in availableChapters"
                :key="chapter.id"
                :label="chapter.name"
                :value="chapter.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item
            v-if="saveForm.chapterId"
            label="所属小节"
            class="form-item-custom"
        >
          <el-select
              v-model="saveForm.sectionId"
              placeholder="请选择小节（可选）"
              clearable
              class="custom-select neumorphism-inset"
          >
            <el-option
                v-for="section in availableSections"
                :key="section.id"
                :label="section.name"
                :value="section.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="笔记标题" class="form-item-custom">
          <el-input
              v-model="saveForm.title"
              placeholder="请输入笔记标题（可选）"
              class="custom-input neumorphism-inset"
          />
        </el-form-item>

        <el-form-item label="标签" class="form-item-custom">
          <el-input
              v-model="saveForm.tags"
              placeholder="请输入标签，多个标签用逗号分隔（可选）"
              class="custom-input neumorphism-inset"
          />
        </el-form-item>

        <el-form-item label="隐私设置" class="form-item-custom">
          <el-checkbox
              v-model="saveForm.isPrivate"
              class="custom-checkbox"
          >
            设为私有笔记
            <span class="checkbox-hint">（默认私有）</span>
          </el-checkbox>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button
              class="cancel-btn neumorphism-raised"
              @click="cancelSaveNote"
          >
            取消
          </el-button>
          <el-button
              type="primary"
              class="confirm-btn neumorphism-raised"
              @click="confirmSaveNote"
              :disabled="isSaving"
              :loading="isSaving"
          >
            <font-awesome-icon :icon="['fas', 'save']" />
            {{ isSaving ? '保存中...' : '保存笔记' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, computed, onMounted, watch, onUnmounted, nextTick} from 'vue'
import {MdEditor} from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import {ElMessage, ElMessageBox, ElDialog, ElForm, ElFormItem, ElSelect, ElOption, ElInput, ElCheckbox, ElButton} from 'element-plus'
import { createFreedomNote, updateNote, getAllLearningNotes, getCourseNotes, getMyCourseList } from '@/api/students/stuAPI.js'
import { getInboxNotes, archiveNote } from '@/api/students/learning.js'

// 响应式数据
const searchQuery = ref('')
const activeFilter = ref('all')
const selectedNote = ref(null)
const sidebarCollapsed = ref(false)
const editorMode = ref('split')
const editorTheme = ref('light')
const newTag = ref('')
const isFullscreen = ref(false)
const viewMode = ref('list') // 添加视图模式：'list' 或 'card'
const isImmersiveMode = ref(false) // 沉浸模式状态
const isLoading = ref(false) // 数据加载状态
const isSaving = ref(false) // 保存状态
const courseNotes = ref([]) // 课程笔记数据
const courseNotesLoading = ref(false) // 课程笔记加载状态
const isNewNote = ref(false) // 标识是否为新建笔记
const inboxNotes = ref([])
const inboxNotesLoading = ref(false)
const inboxSelectedTag = ref('')
const hoveredNoteId = ref(null)
const showPreview = ref(false)
let previewTimer = null

// 保存笔记弹窗相关数据
const saveDialogVisible = ref(false)
const saveForm = ref({
  courseId: '',
  chapterId: '',
  sectionId: '',
  title: '',
  tags: '',
  isPrivate: true
})

const courses = ref([])

// 模拟章节数据
const chapters = ref({
  '1': [
    { id: '1-1', name: '第一章：Vue.js 简介' },
    { id: '1-2', name: '第二章：组件基础' },
    { id: '1-3', name: '第三章：响应式原理' }
  ],
  '2': [
    { id: '2-1', name: '第一章：ES6+ 新特性' },
    { id: '2-2', name: '第二章：异步编程' },
    { id: '2-3', name: '第三章：模块化开发' }
  ],
  '3': [
    { id: '3-1', name: '第一章：构建工具' },
    { id: '3-2', name: '第二章：代码规范' },
    { id: '3-3', name: '第三章：性能优化' }
  ],
  '4': [
    { id: '4-1', name: '第一章：React 基础' },
    { id: '4-2', name: '第二章：状态管理' },
    { id: '4-3', name: '第三章：路由系统' }
  ]
})

// 模拟小节数据
const sections = ref({
  '1-1': [
    { id: '1-1-1', name: '1.1 什么是Vue.js' },
    { id: '1-1-2', name: '1.2 Vue.js的特点' },
    { id: '1-1-3', name: '1.3 开发环境搭建' }
  ],
  '1-2': [
    { id: '1-2-1', name: '2.1 组件定义' },
    { id: '1-2-2', name: '2.2 组件通信' },
    { id: '1-2-3', name: '2.3 组件生命周期' }
  ],
  '2-1': [
    { id: '2-1-1', name: '1.1 let和const' },
    { id: '2-1-2', name: '1.2 箭头函数' },
    { id: '2-1-3', name: '1.3 解构赋值' }
  ],
  '3-1': [
    { id: '3-1-1', name: '1.1 Webpack配置' },
    { id: '3-1-2', name: '1.2 Vite使用' },
    { id: '3-1-3', name: '1.3 构建优化' }
  ],
  '4-1': [
    { id: '4-1-1', name: '1.1 JSX语法' },
    { id: '4-1-2', name: '1.2 组件创建' },
    { id: '4-1-3', name: '1.3 Props传递' }
  ]
})

// 笔记数据
const notes = ref([])

// 模拟笔记数据（用于开发测试）
const mockNotes = ref([
  {
    id: '1',
    title: 'Vue 3 Composition API 学习笔记',
    content: `# Vue 3 Composition API 学习笔记\n\n## 概述\n\nComposition API 是 Vue 3 中引入的新特性，它提供了一种更灵活的方式来组织组件逻辑。\n\n## 核心概念\n\n### 1. setup() 函数\n\n\`\`\`javascript\nimport { ref, reactive } from 'vue'\n\nexport default {\n  setup() {\n    const count = ref(0)\n    const state = reactive({\n      name: 'Vue 3'\n    })\n    \n    return {\n      count,\n      state\n    }\n  }\n}\n\`\`\`\n\n### 2. 响应式 API\n\n- **ref()**: 创建响应式引用\n- **reactive()**: 创建响应式对象\n- **computed()**: 创建计算属性\n- **watch()**: 创建侦听器\n\n## 数学公式示例\n\n当我们讨论响应式系统的性能时，可以用以下公式表示：\n\n$$\nPerformance = \\frac{Updates}{Time} \\times Efficiency\n$$\n\n其中：\n- $Updates$ 表示更新次数\n- $Time$ 表示时间\n- $Efficiency$ 表示效率系数\n\n## 流程图\n\n\`\`\`mermaid\ngraph TD\n    A[组件初始化] --> B[setup函数执行]\n    B --> C[创建响应式数据]\n    C --> D[返回数据和方法]\n    D --> E[模板渲染]\n    E --> F[用户交互]\n    F --> G[数据更新]\n    G --> H[重新渲染]\n    H --> F\n\`\`\`\n\n## 总结\n\nComposition API 让我们能够：\n1. 更好地组织代码逻辑\n2. 提高代码复用性\n3. 获得更好的 TypeScript 支持\n`,
    tags: ['Vue3', 'Composition API', 'JavaScript'],
    isFavorite: true,
    createdAt: new Date('2024-01-15'),
    updatedAt: new Date('2024-01-20')
  },
  {
    id: '2',
    title: '数据结构与算法 - 排序算法',
    content: `# 排序算法学习笔记\n\n## 冒泡排序\n\n### 算法思想\n冒泡排序是一种简单的排序算法，它重复地遍历要排序的数列。\n\n### 代码实现\n\n\`\`\`python\ndef bubble_sort(arr):\n    n = len(arr)\n    for i in range(n):\n        for j in range(0, n-i-1):\n            if arr[j] > arr[j+1]:\n                arr[j], arr[j+1] = arr[j+1], arr[j]\n    return arr\n\`\`\`\n\n### 时间复杂度\n\n- 最好情况：$O(n)$\n- 平均情况：$O(n^2)$\n- 最坏情况：$O(n^2)$\n\n### 算法流程图\n\n\`\`\`mermaid\nflowchart TD\n    A[开始] --> B[i = 0]\n    B --> C[j = 0]\n    C --> D{j < n-i-1?}\n    D -->|是| E{arr[j] > arr[j+1]?}\n    E -->|是| F[交换 arr[j] 和 arr[j+1]]\n    E -->|否| G[j++]\n    F --> G\n    G --> D\n    D -->|否| H[i++]\n    H --> I{i < n?}\n    I -->|是| C\n    I -->|否| J[结束]\n\`\`\`\n`,
    tags: ['算法', '排序', 'Python'],
    isFavorite: false,
    createdAt: new Date('2024-01-10'),
    updatedAt: new Date('2024-01-18')
  },
  {
    id: '3',
    title: 'React Hooks 使用指南',
    content: `# React Hooks 使用指南\n\n## useState Hook\n\n\`\`\`jsx\nimport React, { useState } from 'react';\n\nfunction Counter() {\n  const [count, setCount] = useState(0);\n\n  return (\n    <div>\n      <p>You clicked {count} times</p>\n      <button onClick={() => setCount(count + 1)}>\n        Click me\n      </button>\n    </div>\n  );\n}\n\`\`\`\n\n## useEffect Hook\n\n\`\`\`jsx\nimport React, { useState, useEffect } from 'react';\n\nfunction Example() {\n  const [count, setCount] = useState(0);\n\n  useEffect(() => {\n    document.title = \`You clicked \${count} times\`;\n  });\n\n  return (\n    <div>\n      <p>You clicked {count} times</p>\n      <button onClick={() => setCount(count + 1)}>\n        Click me\n      </button>\n    </div>\n  );\n}\n\`\`\`\n`,
    tags: ['React', 'Hooks', 'JavaScript'],
    isFavorite: true,
    createdAt: new Date('2024-01-12'),
    updatedAt: new Date('2024-01-19')
  }
])

// 过滤标签
const filterTabs = ref([
  {key: 'inbox', label: '灵感池', icon: ['fas', 'lightbulb']},
  {key: 'all', label: '自由', icon: ['fas', 'sticky-note']},
  {key: 'free', label: '自主', icon: ['fas', 'pen-fancy']},
  {key: 'course', label: '课程', icon: ['fas', 'graduation-cap']},
  {key: 'recent', label: '最近', icon: ['fas', 'clock']}
])

// 编辑器工具栏配置
const toolbars = ref([
  'bold', 'underline', 'italic', '-',
  'title', 'strikeThrough', 'sub', 'sup', 'quote', 'unorderedList', 'orderedList', 'task', '-',
  'codeRow', 'code', 'link', 'image', 'table', 'mermaid', 'katex', '-',
  'revoke', 'next', 'save', '=',
  'pageFullscreen', 'fullscreen', 'preview', 'htmlPreview', 'catalog'
])

const footers = ref(['markdownTotal', '=', 'scrollSwitch'])

// 计算属性
const filteredNotes = computed(() => {
  let base = []
  if (activeFilter.value === 'course') {
    base = courseNotes.value
  } else if (activeFilter.value === 'inbox') {
    base = inboxNotes.value
  } else {
    base = notes.value
  }

  if (activeFilter.value === 'free') {
    base = notes.value.filter(note => !note.courseId)
  }

  if (activeFilter.value === 'recent') {
    base = notes.value.filter(note => {
      const daysDiff = (new Date() - note.updatedAt) / (1000 * 60 * 60 * 24)
      return daysDiff <= 7
    })
  }

  if (activeFilter.value === 'inbox' && inboxSelectedTag.value) {
    base = base.filter(note => (note.tags || []).includes(inboxSelectedTag.value))
  }

  // 根据搜索查询过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    base = base.filter(note =>
        (note.title || '').toLowerCase().includes(query) ||
        (note.content || '').toLowerCase().includes(query) ||
        (note.tags || []).some(tag => (tag || '').toLowerCase().includes(query))
    )
  }

  // 按更新时间排序
  return [...base].sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
})

const isListLoading = computed(() => {
  if (activeFilter.value === 'course') return courseNotesLoading.value
  if (activeFilter.value === 'inbox') return inboxNotesLoading.value
  return isLoading.value
})

const listLoadingText = computed(() => {
  if (activeFilter.value === 'course') return '正在加载课程笔记数据...'
  if (activeFilter.value === 'inbox') return '正在加载灵感池数据...'
  return '正在加载笔记数据...'
})

const inboxTagCloud = computed(() => {
  const counts = new Map()
  for (const note of inboxNotes.value) {
    for (const tag of (note.tags || [])) {
      if (!tag) continue
      counts.set(tag, (counts.get(tag) || 0) + 1)
    }
  }
  const entries = Array.from(counts.entries()).sort((a, b) => b[1] - a[1]).slice(0, 30)
  if (entries.length === 0) return []
  const max = entries[0][1]
  const min = entries[entries.length - 1][1]
  return entries.map(([tag, count]) => {
    const t = max === min ? 0.5 : (count - min) / (max - min)
    const size = 12 + t * 10
    return { tag, count, size }
  })
})

// 动态章节选项
const availableChapters = computed(() => {
  if (!saveForm.value.courseId) return []
  return chapters.value[saveForm.value.courseId] || []
})

// 动态小节选项
const availableSections = computed(() => {
  if (!saveForm.value.chapterId) return []
  return sections.value[saveForm.value.chapterId] || []
})

// 方法
// 获取所有学习笔记
const fetchAllNotes = async () => {
  try {
    isLoading.value = true
    const result = await getAllLearningNotes()
    
    if (result && Array.isArray(result)) {
      notes.value = result.map(note => ({
        ...note,
        createdAt: new Date(note.createdAt),
        updatedAt: new Date(note.updatedAt),
        tags: note.tags || []
      }))
      ElMessage.success(`成功加载 ${notes.value.length} 条笔记`)
    } else {
      // 如果后端返回空数据或格式不正确，使用模拟数据
      notes.value = mockNotes.value
      ElMessage.info('暂无笔记数据，显示示例笔记')
    }
  } catch (error) {
    console.error('获取笔记失败:', error)
    // 发生错误时使用模拟数据
    notes.value = mockNotes.value
    ElMessage.error('获取笔记失败: ' + (error.message || '网络错误'))
  } finally {
    isLoading.value = false
  }
}

const fetchInbox = async () => {
  try {
    inboxNotesLoading.value = true
    const result = await getInboxNotes({ page: 0, size: 60, sort: 'updatedAt,desc' })
    const content = result?.content || []
    inboxNotes.value = content.map(note => ({
      ...note,
      createdAt: new Date(note.createdAt),
      updatedAt: new Date(note.updatedAt),
      tags: note.tags || []
    }))
  } catch (error) {
    console.error('获取灵感池失败:', error)
    inboxNotes.value = []
    ElMessage.error('获取灵感池失败: ' + (error.message || '网络错误'))
  } finally {
    inboxNotesLoading.value = false
  }
}

// 刷新笔记数据
const refreshNotes = async () => {
  await fetchAllNotes()
}

// 获取课程笔记
const fetchCourseNotes = async () => {
  try {
    courseNotesLoading.value = true
    const result = await getCourseNotes({ page: 0, size: 60, sort: 'updatedAt,desc' })
    const content = result?.content || []
    courseNotes.value = content.map(note => ({
      ...note,
      createdAt: new Date(note.createdAt),
      updatedAt: new Date(note.updatedAt),
      tags: note.tags || []
    }))
  } catch (error) {
    console.error('获取课程笔记失败:', error)
    courseNotes.value = []
    ElMessage.error('获取课程笔记失败: ' + (error.message || '网络错误'))
  } finally {
    courseNotesLoading.value = false
  }
}

const createNewNote = () => {
  const newNote = {
    id: Date.now().toString(),
    title: '',
    content: '# 新建笔记\n\n开始记录你的学习心得...',
    tags: [],
    isFavorite: false,
    createdAt: new Date(),
    updatedAt: new Date()
  }

  notes.value.unshift(newNote)
  selectedNote.value = newNote
  isNewNote.value = true // 标记为新建笔记
  ElMessage.success('创建新笔记成功')
}

const selectNote = (note) => {
  selectedNote.value = note
  isNewNote.value = false // 选择现有笔记时标记为非新建笔记
}

const saveNote = async () => {
  if (!selectedNote.value) {
    ElMessage.warning('请先选择要保存的笔记')
    return
  }
  
  if (isNewNote.value) {
    // 新建笔记场景：显示弹窗收集完整信息
    saveForm.value = {
      courseId: '',
      chapterId: '',
      sectionId: '',
      title: selectedNote.value.title || '',
      tags: selectedNote.value.tags.join(', '),
      isPrivate: true
    }
    saveDialogVisible.value = true
  } else {
    // 修改现有笔记场景：直接调用updateNote API
    try {
      isSaving.value = true
      
      const updateData = {
        title: selectedNote.value.title,
        content: selectedNote.value.content,
        tags: selectedNote.value.tags,
        isPrivate: true // 可以根据需要调整
      }
      
      await updateNote(selectedNote.value.id, updateData)
      
      // 更新本地数据
      selectedNote.value.updatedAt = new Date()
      
      ElMessage.success('笔记更新成功')
    } catch (error) {
      console.error('更新笔记失败:', error)
      ElMessage.error('更新笔记失败: ' + (error.message || '网络错误'))
    } finally {
      isSaving.value = false
    }
  }
}

// 处理课程选择变化
const handleCourseChange = () => {
  // 重置章节和小节选择
  saveForm.value.chapterId = ''
  saveForm.value.sectionId = ''
}

// 处理章节选择变化
const handleChapterChange = () => {
  // 重置小节选择
  saveForm.value.sectionId = ''
}

// 确认保存笔记
const confirmSaveNote = async () => {
  if (!selectedNote.value) return
  
  try {
    isSaving.value = true
    
    // 构建符合CreateNoteDTO规范的数据
    const createData = {
      courseId: saveForm.value.courseId || undefined,
      chapterId: saveForm.value.chapterId || undefined, 
      sectionId: saveForm.value.sectionId || undefined,
      title: saveForm.value.title || selectedNote.value.title,
      content: selectedNote.value.content,
      tags: saveForm.value.tags.split(',').map(tag => tag.trim()).filter(tag => tag),
      isPrivate: saveForm.value.isPrivate,
      videoTime: undefined // 如果需要可以添加视频时间
    }
    
    // 调用createFreedomNote API
    const result = await createFreedomNote(createData)
    
    // 更新本地笔记数据
    if (result && result.id) {
      selectedNote.value.id = result.id
      selectedNote.value.title = createData.title
      selectedNote.value.tags = createData.tags
      selectedNote.value.updatedAt = new Date()
      
      // 标记为非新建笔记
      isNewNote.value = false
    }
    
    // 关闭对话框
    saveDialogVisible.value = false
    
    ElMessage.success('笔记创建成功')
    
    // 刷新笔记列表
    await fetchAllNotes()
    
  } catch (error) {
    console.error('创建笔记失败:', error)
    ElMessage.error('创建笔记失败: ' + (error.message || '网络错误'))
  } finally {
    isSaving.value = false
  }
}

// 取消保存
const cancelSaveNote = () => {
  saveDialogVisible.value = false
}

const deleteNote = async (note) => {
  try {
    await ElMessageBox.confirm(
        '确定要删除这个笔记吗？此操作不可恢复。',
        '删除确认',
        {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const index = notes.value.findIndex(n => n.id === note.id)
    if (index > -1) {
      notes.value.splice(index, 1)
      if (selectedNote.value?.id === note.id) {
        selectedNote.value = notes.value[0] || null
      }
      ElMessage.success('笔记已删除')
    }
  } catch {
    // 用户取消删除
  }
}

const toggleNoteFavorite = (note) => {
  note.isFavorite = !note.isFavorite
  note.updatedAt = new Date()
  ElMessage.success(note.isFavorite ? '已添加到收藏' : '已取消收藏')
}

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const setActiveFilter = async (filter) => {
  activeFilter.value = filter
  if (filter !== 'inbox') {
    inboxSelectedTag.value = ''
  }
  
  // 如果点击课程按钮，获取课程笔记数据
  if (filter === 'course') {
    await fetchCourseNotes()
  }
  if (filter === 'inbox') {
    viewMode.value = 'card'
    await fetchInbox()
  }
}

const handleNoteHover = (noteId) => {
  if (previewTimer) clearTimeout(previewTimer)
  hoveredNoteId.value = noteId
  previewTimer = setTimeout(() => {
    showPreview.value = true
  }, 400) // 400ms 延迟
}

const handleNoteLeave = () => {
  if (previewTimer) clearTimeout(previewTimer)
  showPreview.value = false
  hoveredNoteId.value = null
}

const archiveInboxToFree = async (note) => {
  try {
    await ElMessageBox.confirm(
        '归档后将从灵感池移入自主笔记，是否继续？',
        '归档确认',
        {
          confirmButtonText: '归档',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
    await archiveNote(note.id)
    inboxNotes.value = inboxNotes.value.filter(n => n.id !== note.id)
    if (selectedNote.value?.id === note.id) {
      selectedNote.value = null
    }
    ElMessage.success('归档成功')
    await fetchAllNotes()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    console.error('归档失败:', error)
    ElMessage.error('归档失败: ' + (error.message || '网络错误'))
  }
}

const setEditorMode = (mode) => {
  editorMode.value = mode

  // 确保编辑器正确响应模式变化
  nextTick(() => {
    // 触发编辑器重新渲染
    const editorElement = document.querySelector('.md-editor')
    if (editorElement) {
      editorElement.dispatchEvent(new Event('resize'))
    }
  })
}

// 切换预览模式
const togglePreviewMode = () => {
  if (editorMode.value === 'preview') {
    editorMode.value = 'edit'
    ElMessage.success('已切换到编辑模式')
  } else {
    editorMode.value = 'preview'
    ElMessage.success('已切换到预览模式')
  }

  // 确保编辑器正确响应模式变化
  nextTick(() => {
    const editorElement = document.querySelector('.md-editor')
    if (editorElement) {
      editorElement.dispatchEvent(new Event('resize'))
    }
  })
}

const getFilterCount = (filter) => {
  switch (filter) {
    case 'inbox':
      return inboxNotes.value.length
    case 'all':
      return notes.value.length
    case 'free':
      return notes.value.filter(note => !note.courseId).length
    case 'course':
      return courseNotes.value.length
    case 'recent':
      return notes.value.filter(note => {
        const daysDiff = (new Date() - note.updatedAt) / (1000 * 60 * 60 * 24)
        return daysDiff <= 7
      }).length
    default:
      return 0
  }
}

const fetchCourseList = async () => {
  try {
    const result = await getMyCourseList()
    if (Array.isArray(result)) {
      courses.value = result.map(c => ({
        id: c.id,
        name: c.title || c.name || c.courseName || ''
      })).filter(c => c.id && c.name)
    } else {
      courses.value = []
    }
  } catch (error) {
    courses.value = []
  }
}

const formatDate = (date) => {
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return minutes <= 1 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

const getPreviewText = (content) => {
  // 移除 Markdown 语法，获取纯文本预览
  return content
      .replace(/#{1,6}\s+/g, '')
      .replace(/\*\*(.*?)\*\*/g, '$1')
      .replace(/\*(.*?)\*/g, '$1')
      .replace(/`(.*?)`/g, '$1')
      .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
      .replace(/\n/g, ' ')
      .substring(0, 100) + (content.length > 100 ? '...' : '')
}

const getWordCount = (content) => {
  return content.replace(/\s+/g, '').length
}

const addTag = () => {
  if (newTag.value.trim() && selectedNote.value) {
    const tag = newTag.value.trim()
    if (!selectedNote.value.tags.includes(tag)) {
      selectedNote.value.tags.push(tag)
      selectedNote.value.updatedAt = new Date()
    }
    newTag.value = ''
  }
}

const removeTag = (index) => {
  if (selectedNote.value) {
    selectedNote.value.tags.splice(index, 1)
    selectedNote.value.updatedAt = new Date()
  }
}

const handleImageUpload = async (files, callback) => {
  // 这里实现图片上传逻辑
  // 暂时返回一个示例 URL
  const urls = files.map(() => 'https://via.placeholder.com/300x200')
  callback(urls)
}

const exportNote = () => {
  if (selectedNote.value) {
    const blob = new Blob([selectedNote.value.content], {type: 'text/markdown'})
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${selectedNote.value.title || '笔记'}.md`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('笔记导出成功')
  }
}

// 专注模式功能
const toggleImmersiveMode = async () => {
  isImmersiveMode.value = !isImmersiveMode.value
  
  if (isImmersiveMode.value) {
    // 进入专注模式
    await enterImmersiveMode()
  } else {
    // 退出专注模式
    await exitImmersiveMode()
  }
}

// 进入专注模式
const enterImmersiveMode = async () => {
  // 保存当前笔记
  await saveNote()
  
  // 添加专注模式样式类到笔记容器
  const notesContainer = document.querySelector('.notes-container')
  if (notesContainer) {
    notesContainer.classList.add('focus-mode')
  }
  
  // 隐藏导航栏和侧边栏（参考StuChat的实现）
  const navbar = document.querySelector('.navbar, .nav-bar, .header, .layout-header')
  const sidebar = document.querySelector('.sidebar, .side-nav, .menu, .layout-sidebar')
  const layout = document.querySelector('.layout, .main-layout')

  if (navbar) {
    navbar.style.display = 'none'
    navbar.setAttribute('data-hidden-by-focus', 'true')
  }
  if (sidebar) {
    sidebar.style.display = 'none'
    sidebar.setAttribute('data-hidden-by-focus', 'true')
  }
  if (layout) {
    layout.style.padding = '0'
    layout.setAttribute('data-modified-by-focus', 'true')
  }
  
  ElMessage.success('已进入专注模式，按ESC键退出')
}

// 退出专注模式
const exitImmersiveMode = async () => {
  // 自动保存当前编辑内容
  await saveNote()
  
  // 移除专注模式样式类
  const notesContainer = document.querySelector('.notes-container')
  if (notesContainer) {
    notesContainer.classList.remove('focus-mode')
  }
  
  // 恢复导航栏和侧边栏（参考StuChat的实现）
  const hiddenElements = document.querySelectorAll('[data-hidden-by-focus="true"]')
  hiddenElements.forEach(element => {
    element.style.display = ''
    element.removeAttribute('data-hidden-by-focus')
  })

  const modifiedElements = document.querySelectorAll('[data-modified-by-focus="true"]')
  modifiedElements.forEach(element => {
    element.style.padding = ''
    element.removeAttribute('data-modified-by-focus')
  })
  
  ElMessage.success('已退出专注模式')
}

// ESC键退出专注模式
const handleEscapeKey = (e) => {
  if (e.key === 'Escape' && isImmersiveMode.value) {
    exitImmersiveMode()
    isImmersiveMode.value = false
  }
}

// 处理编辑器全屏事件
const handleFullscreen = () => {
  isFullscreen.value = true
  toggleLayoutVisibility()
}

const handleExitFullscreen = () => {
  isFullscreen.value = false
  toggleLayoutVisibility()
}

// 全屏状态检测
const checkFullscreen = () => {
  const isCurrentlyFullscreen = !!(document.fullscreenElement ||
      document.webkitFullscreenElement ||
      document.mozFullScreenElement ||
      document.msFullscreenElement)

  if (isCurrentlyFullscreen !== isFullscreen.value) {
    isFullscreen.value = isCurrentlyFullscreen
    toggleLayoutVisibility()
  }
}

// 切换布局可见性
const toggleLayoutVisibility = () => {
  const studentLayout = document.querySelector('.student-layout')
  const sidebar = document.querySelector('.sidebar')
  const navbar = document.querySelector('.navbar')

  if (isFullscreen.value) {
    // 全屏时隐藏布局元素
    if (sidebar) sidebar.style.display = 'none'
    if (navbar) navbar.style.display = 'none'
    if (studentLayout) {
      studentLayout.style.background = 'transparent'
    }

    // 调整笔记容器样式
    const notesContainer = document.querySelector('.notes-container')
    if (notesContainer) {
      notesContainer.style.position = 'fixed'
      notesContainer.style.top = '0'
      notesContainer.style.left = '0'
      notesContainer.style.width = '100vw'
      notesContainer.style.height = '100vh'
      notesContainer.style.zIndex = '9999'
      notesContainer.style.background = '#f0f0f3'
    }
  } else {
    // 退出全屏时恢复布局元素
    if (sidebar) sidebar.style.display = ''
    if (navbar) navbar.style.display = ''
    if (studentLayout) {
      studentLayout.style.background = ''
    }

    // 恢复笔记容器样式
    const notesContainer = document.querySelector('.notes-container')
    if (notesContainer) {
      notesContainer.style.position = ''
      notesContainer.style.top = ''
      notesContainer.style.left = ''
      notesContainer.style.width = ''
      notesContainer.style.height = ''
      notesContainer.style.zIndex = ''
      notesContainer.style.background = ''
    }
  }
}

// 生命周期
onMounted(async () => {
  // 初始化加载所有笔记数据
  await fetchAllNotes()
  await fetchCourseList()
  await fetchInbox()
  
  // 默认选择第一个笔记
  if (notes.value.length > 0) {
    selectedNote.value = notes.value[0]
  }

  // 监听全屏变化事件
  document.addEventListener('fullscreenchange', checkFullscreen)
  document.addEventListener('webkitfullscreenchange', checkFullscreen)
  document.addEventListener('mozfullscreenchange', checkFullscreen)
  document.addEventListener('MSFullscreenChange', checkFullscreen)

  // 监听md-editor的全屏事件和沉浸模式ESC键
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && isFullscreen.value) {
      checkFullscreen()
    }
  })
  
  // 监听ESC键退出沉浸模式
  document.addEventListener('keydown', handleEscapeKey)
})

onUnmounted(() => {
  // 清理事件监听器
  document.removeEventListener('fullscreenchange', checkFullscreen)
  document.removeEventListener('webkitfullscreenchange', checkFullscreen)
  document.removeEventListener('mozfullscreenchange', checkFullscreen)
  document.removeEventListener('MSFullscreenChange', checkFullscreen)
  document.removeEventListener('keydown', handleEscapeKey)
  
  // 确保退出专注模式并保存内容
  if (isImmersiveMode.value) {
    exitImmersiveMode()
    isImmersiveMode.value = false
  }
})

// 监听选中笔记的内容变化，自动保存
watch(
    () => selectedNote.value?.content,
    () => {
      if (selectedNote.value) {
        selectedNote.value.updatedAt = new Date()
      }
    },
    {deep: true}
)

</script>

<style scoped>
/* 主容器 */
.notes-container {
  height: 100vh;
  background: #f0f0f3;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 页面头部 */
.notes-header {
  padding: 20px 24px;
  margin: 16px;
  border-radius: 16px;
  background: #f0f0f3;
  box-shadow: 8px 8px 16px #d1d1d4,
  -8px -8px 16px #ffffff;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.title-icon {
  font-size: 28px;
  color: #002FA7;
}

.page-title h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  border-radius: 12px;
  background: #f0f0f3;
  color: #303133;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 4px 4px 8px #d1d1d4,
  -4px -4px 8px #ffffff;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px #d1d1d4,
  -6px -6px 12px #ffffff;
}

.action-btn:active {
  transform: translateY(0);
  box-shadow: inset 4px 4px 8px #d1d1d4,
  inset -4px -4px 8px #ffffff;
}

/* 保存按钮特殊样式 */
.save-btn {
  background: #67C23A;
  color: #ffffff !important;
  border: none;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

.save-btn:hover {
  background: #5daf34;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
}

/* 沉浸模式按钮样式 */
.immersive-btn {
  background: #002FA7;
  color: #ffffff !important;
  border: none;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 47, 167, 0.3);
}

.immersive-btn:hover {
  background: #1e4ba8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 47, 167, 0.4);
}

/* 主要内容区域 */
.notes-main {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 0 16px 16px;
  overflow: hidden;
}

/* 左侧边栏 */
.notes-sidebar {
  width: 320px;
  min-width: 320px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  position: relative;
  transition: all 0.3s ease;
}

.notes-sidebar.collapsed {
  width: 60px;
  min-width: 60px;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.search-container {
  margin-bottom: 16px;
}

.search-wrapper {
  position: relative;
  background: #f0f0f3;
  border-radius: 12px;
  box-shadow: inset 4px 4px 8px #d1d1d4,
  inset -4px -4px 8px #ffffff;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-icon {
  color: #909399;
  font-size: 14px;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  color: #303133;
  font-size: 14px;
}

.search-input::placeholder {
  color: #909399;
}

/* 侧边栏样式优化 */
.sidebar-header {
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.main-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.main-tab-item {
  flex: 1;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border: 2px solid transparent;
}

.main-tab-item:hover {
  transform: translateY(-2px);
}

.main-tab-item.active {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.05) 0%, rgba(81, 123, 77, 0.05) 100%);
  border-color: rgba(0, 47, 167, 0.3);
  box-shadow: inset 2px 2px 5px rgba(209, 209, 212, 0.5),
              inset -2px -2px 5px rgba(255, 255, 255, 0.8);
}

.tab-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
}

.tab-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.inbox-icon {
  color: #E6A23C;
}

.free-icon {
  color: #002FA7;
}

.tab-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.tab-label {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.tab-count {
  font-size: 10px;
  color: #E6A23C;
  background: rgba(230, 162, 60, 0.1);
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
}

.mini-counts {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 10px;
  color: #909399;
}

.sub-tabs-container {
  margin-bottom: 16px;
  padding: 0 4px;
}

.sub-tabs-group {
  display: flex;
  padding: 4px;
  border-radius: 12px;
  gap: 4px;
}

.sub-tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #606266;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.sub-tab-btn:hover {
  color: #303133;
  background: rgba(255, 255, 255, 0.5);
}

.sub-tab-btn.active {
  background: #ffffff;
  color: #002FA7;
  font-weight: 600;
  box-shadow: 2px 2px 5px rgba(209, 209, 212, 0.3),
              -2px -2px 5px rgba(255, 255, 255, 0.8);
}

.sub-count-badge {
  font-size: 10px;
  background: rgba(0, 0, 0, 0.05);
  padding: 1px 5px;
  border-radius: 8px;
  color: #909399;
}

.sub-tab-btn.active .sub-count-badge {
  background: rgba(0, 47, 167, 0.1);
  color: #002FA7;
}

.filter-tabs {
  display: none;
}

/* 覆盖旧样式 */
.filter-tab {
  display: none;
}

.inbox-tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border: none;
  border-radius: 999px;
  background: rgba(0, 47, 167, 0.08);
  color: #303133;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tag-chip:hover {
  background: rgba(0, 47, 167, 0.14);
}

.tag-chip.active {
  background: rgba(0, 47, 167, 0.2);
  color: #002FA7;
}

.tag-chip.clear-chip {
  background: rgba(245, 108, 108, 0.12);
  color: #F56C6C;
}

.tag-count {
  font-size: 11px;
  padding: 0 6px;
  border-radius: 10px;
  background: rgba(0, 47, 167, 0.12);
  color: #002FA7;
}

.tag-chip.clear-chip .tag-count {
  background: rgba(245, 108, 108, 0.12);
  color: #F56C6C;
}

.notes-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px 20px;
}

/* 卡片视图样式 */
.notes-list.card-view {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); /* 缩小最小宽度，一行更多 */
  gap: 16px;
}

.note-item {
  padding: 12px 16px;
  margin-bottom: 12px;
  border-radius: 12px;
  background: #f0f0f3;
  box-shadow: 4px 4px 8px #d1d1d4,
  -4px -4px 8px #ffffff;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative; /* 为tooltip定位 */
}

/* 列表项样式 */
.note-item.list-item {
  margin-bottom: 12px;
}

/* 卡片项样式 */
.note-item.card-item {
  margin-bottom: 0;
  height: fit-content;
  min-height: 100px; /* 减小高度 */
  display: flex;
  flex-direction: column;
}

/* 移除旧的 note-preview 样式 */

.note-item:hover {
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px #d1d1d4,
  -6px -6px 12px #ffffff;
  z-index: 10; /* hover时层级提高 */
}

.note-item.active {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.1) 0%, rgba(81, 123, 77, 0.1) 100%);
  border: 1px solid rgba(0, 47, 167, 0.2);
}

.note-header {
  display: flex;
  flex-direction: column; /* 改为纵向布局 */
  gap: 8px;
}

.note-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}

.note-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 160px; /* 限制标题宽度 */
}

.note-date {
  font-size: 11px;
  color: #909399;
  flex-shrink: 0;
  margin-top: 2px;
}

.note-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 2px;
}

.note-tag {
  font-size: 10px;
  padding: 2px 8px;
  background: rgba(0, 47, 167, 0.05);
  color: #002FA7;
  border-radius: 4px;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.note-tag-more {
  font-size: 10px;
  color: #909399;
  padding: 2px 4px;
}

.note-actions {
  display: flex;
  justify-content: flex-end; /* 靠右对齐 */
  gap: 8px;
  margin-top: 4px;
  border-top: 1px solid rgba(0,0,0,0.03);
  padding-top: 8px;
}

.note-action-btn {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #909399;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.note-action-btn:hover {
  background: rgba(0,0,0,0.05);
  color: #303133;
}

.note-action-btn.delete-btn:hover {
  color: #F56C6C;
  background: rgba(245, 108, 108, 0.1);
}

.note-action-btn.archive-btn:hover {
  color: #E6A23C;
  background: rgba(230, 162, 60, 0.1);
}

.note-action-btn .favorite {
  color: #F56C6C;
}

/* 悬浮预览层样式 */
.note-tooltip {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0; /* 左右都设为0，强制撑满父容器宽度 */
  width: auto; /* 重置宽度 */
  min-width: 0; /* 移除最小宽度限制 */
  background: #ffffff;
  padding: 12px;
  border-radius: 8px;
  z-index: 100;
  pointer-events: none;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1); /* 加深阴影 */
  border: 1px solid rgba(0,0,0,0.05);
}

/* 列表视图下的特殊处理 - 移除之前的 400px 强制宽度 */
.list-view .note-tooltip {
  width: auto;
  left: 0;
  transform: none;
}

.tooltip-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 8;
  -webkit-box-orient: vertical;
}

.tooltip-arrow {
  position: absolute;
  top: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-bottom: 6px solid #ffffff;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}


.note-action-btn.archive-btn {
  color: #E6A23C;
}

.note-action-btn.archive-btn:hover {
  color: #d48e28;
}

.note-action-btn:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #606266;
}

.note-action-btn .favorite {
  color: #f56c6c;
}

.note-preview {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.note-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.note-tag {
  padding: 2px 8px;
  background: rgba(0, 47, 167, 0.1);
  color: #002FA7;
  border-radius: 12px;
  font-size: 12px;
}

.sidebar-toggle {
  position: absolute;
  right: -12px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 50%;
  background: #f0f0f3;
  color: #606266;
  cursor: pointer;
  box-shadow: 2px 2px 4px #d1d1d4,
  -2px -2px 4px #ffffff;
  transition: all 0.3s ease;
}

.sidebar-toggle:hover {
  color: #002FA7;
}

/* 编辑器容器 */
.editor-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.editor-wrapper {
  flex: 1;
  background: #f0f0f3;
  border-radius: 16px;
  box-shadow: 8px 8px 16px #d1d1d4,
  -8px -8px 16px #ffffff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.editor-header {
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 16px 16px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-title {
  flex: 1;
}

.title-input {
  width: 100%;
  border: none;
  background: transparent;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  outline: none;
}

.title-input::placeholder {
  color: #909399;
}

.editor-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.editor-modes {
  display: flex;
  gap: 4px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  padding: 4px;
}

.mode-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #606266;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.mode-btn.active {
  background: rgba(0, 47, 167, 0.1);
  color: #002FA7;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.editor-content {
  flex: 1;
  overflow: hidden;
}

.md-editor {
  height: 100%;
  border: none;
  border-radius: 0;
}

/* 笔记底部 */
.note-footer {
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 0 0 16px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tags-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.tags-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
  white-space: nowrap;
}

.tags-input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.tags-display {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: rgba(0, 47, 167, 0.1);
  color: #002FA7;
  border-radius: 12px;
  font-size: 12px;
}

.tag-remove {
  width: 14px;
  height: 14px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.1);
  color: #606266;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
}

.tag-remove:hover {
  background: rgba(0, 0, 0, 0.2);
}

.tag-input {
  padding: 4px 8px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  color: #303133;
  font-size: 12px;
  outline: none;
  min-width: 100px;
}

.tag-input::placeholder {
  color: #909399;
}

.note-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 12px;
}

/* 空状态 */
.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f3;
  border-radius: 16px;
  box-shadow: 8px 8px 16px #d1d1d4,
  -8px -8px 16px #ffffff;
}

.empty-content {
  text-align: center;
  max-width: 300px;
}

.empty-icon {
  font-size: 64px;
  color: #d1d1d4;
  margin-bottom: 16px;
}

.empty-content h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #303133;
}

.empty-content p {
  margin: 0 0 24px 0;
  color: #606266;
  line-height: 1.5;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 12px;
  background: #f0f0f3;
  color: #002FA7;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 4px 4px 8px #d1d1d4,
  -4px -4px 8px #ffffff;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px #d1d1d4,
  -6px -6px 12px #ffffff;
}

/* 响应式设计 */
/* 大屏幕 (1200px+) */
@media (min-width: 1200px) {
  .notes-sidebar {
    width: 320px;
    min-width: 320px;
  }

  .notes-header {
    padding: 24px 32px;
  }

  .notes-main {
    padding: 0 32px 32px;
  }
}

/* 中等屏幕 (768px - 1199px) */
@media (max-width: 1199px) and (min-width: 768px) {
  .notes-sidebar {
    width: 280px;
    min-width: 280px;
  }

  .notes-header {
    padding: 20px 24px;
  }

  .notes-main {
    padding: 0 24px 24px;
  }

  .header-content {
    gap: 16px;
  }

  .action-btn span {
    display: none;
  }

  .mode-btn span {
    display: none;
  }

  .filter-tab span:not(.count) {
    display: none;
  }
}

/* 小屏幕 (481px - 767px) */
@media (max-width: 767px) and (min-width: 481px) {
  .notes-page {
    padding: 0;
  }

  .notes-header {
    padding: 16px 20px;
    border-radius: 0;
  }

  .header-content {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .header-left {
    text-align: center;
  }

  .page-title h1 {
    font-size: 20px;
  }

  .page-subtitle {
    font-size: 12px;
  }

  .header-actions {
    justify-content: center;
    gap: 8px;
  }

  .action-btn {
    flex: 1;
    justify-content: center;
    padding: 8px 12px;
  }

  .action-btn span {
    font-size: 12px;
  }

  .notes-main {
    flex-direction: column;
    padding: 0 16px 16px;
    gap: 16px;
  }

  .notes-sidebar {
    width: 100%;
    min-width: auto;
    height: 280px;
    order: 2;
  }

  .notes-sidebar.collapsed {
    height: 60px;
    overflow: hidden;
  }

  .editor-container {
    order: 1;
    min-height: 400px;
  }

  .sidebar-header {
    padding: 12px 16px;
  }

  .filter-tabs {
    gap: 4px;
  }

  .filter-tab {
    padding: 6px 8px;
    font-size: 11px;
  }

  .filter-tab span:not(.count) {
    display: none;
  }

  .notes-list {
    padding: 8px;
  }

  .note-item {
    padding: 12px;
  }

  .note-title {
    font-size: 14px;
  }

  .note-preview {
    font-size: 12px;
    line-height: 1.4;
  }

  .editor-header {
    padding: 12px 16px;
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .editor-actions {
    justify-content: space-between;
  }

  .editor-modes {
    gap: 2px;
  }

  .mode-btn {
    padding: 6px 8px;
    font-size: 11px;
  }

  .mode-btn span {
    display: none;
  }

  .action-buttons {
    gap: 4px;
  }

  .action-buttons .action-btn {
    padding: 6px 8px;
    font-size: 11px;
  }

  .action-buttons .action-btn span {
    display: none;
  }

  .note-footer {
    padding: 12px 16px;
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .tags-section {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }

  .tags-input-wrapper {
    flex-direction: column;
    gap: 8px;
  }

  .note-stats {
    justify-content: center;
    gap: 12px;
  }
}

/* 超小屏幕 (480px及以下) */
@media (max-width: 480px) {
  .notes-page {
    padding: 0;
  }

  .notes-header {
    padding: 12px 16px;
    border-radius: 0;
  }

  .header-content {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }

  .header-left {
    text-align: center;
  }

  .page-title {
    flex-direction: column;
    gap: 4px;
  }

  .page-title h1 {
    font-size: 18px;
  }

  .page-subtitle {
    font-size: 11px;
  }

  .header-actions {
    flex-direction: column;
    gap: 8px;
  }

  .action-btn {
    width: 100%;
    justify-content: center;
    padding: 10px 16px;
  }

  .notes-main {
    flex-direction: column;
    padding: 0 12px 12px;
    gap: 12px;
  }

  .notes-sidebar {
    width: 100%;
    min-width: auto;
    height: 250px;
    order: 2;
  }

  .notes-sidebar.collapsed {
    height: 50px;
  }

  .editor-container {
    order: 1;
    min-height: 350px;
  }

  .sidebar-header {
    padding: 8px 12px;
  }

  .search-wrapper {
    margin-bottom: 8px;
  }

  .search-input {
    font-size: 14px;
  }

  .filter-tabs {
    gap: 2px;
    flex-wrap: wrap;
  }

  .filter-tab {
    padding: 4px 6px;
    font-size: 10px;
    min-width: 0;
    flex: 1;
  }

  .filter-tab span:not(.count) {
    display: none;
  }

  .count {
    font-size: 9px;
  }

  .notes-list {
    padding: 6px;
  }

  .note-item {
    padding: 10px;
    margin-bottom: 8px;
  }

  .note-title {
    font-size: 13px;
    line-height: 1.3;
  }

  .note-preview {
    font-size: 11px;
    line-height: 1.3;
    margin-top: 4px;
  }

  .note-meta {
    margin-top: 6px;
  }

  .note-date {
    font-size: 10px;
  }

  .note-actions {
    gap: 4px;
  }

  .note-action-btn {
    width: 24px;
    height: 24px;
    font-size: 10px;
  }

  .note-tags {
    margin-top: 6px;
    gap: 4px;
  }

  .note-tag {
    font-size: 9px;
    padding: 2px 6px;
  }

  .editor-header {
    padding: 8px 12px;
    flex-direction: column;
    gap: 8px;
  }

  .title-input {
    font-size: 16px;
  }

  .editor-actions {
    flex-direction: column;
    gap: 8px;
  }

  .editor-modes {
    justify-content: center;
  }

  .mode-btn {
    padding: 8px 12px;
    font-size: 12px;
  }

  .mode-btn span {
    display: inline;
  }

  .action-buttons {
    justify-content: center;
    gap: 8px;
  }

  .action-buttons .action-btn {
    padding: 6px 12px;
    font-size: 12px;
  }

  .action-buttons .action-btn span {
    display: inline;
  }

  .note-footer {
    padding: 8px 12px;
    flex-direction: column;
    gap: 8px;
  }

  .tags-section {
    flex-direction: column;
    gap: 6px;
  }

  .tags-input-wrapper {
    flex-direction: column;
    gap: 6px;
  }

  .tag-input {
    width: 100%;
    min-width: auto;
  }

  .note-stats {
    flex-direction: column;
    gap: 4px;
    text-align: center;
  }

  .stat-item {
    justify-content: center;
    font-size: 11px;
  }

  .empty-content {
    padding: 20px;
  }

  .empty-icon {
    font-size: 48px;
  }

  .empty-content h3 {
    font-size: 16px;
  }

  .empty-content p {
    font-size: 12px;
  }

  .create-btn {
    padding: 10px 20px;
    font-size: 12px;
  }
}

/* 横屏模式优化 */
@media (max-height: 600px) and (orientation: landscape) {
  .notes-header {
    padding: 8px 16px;
  }

  .page-title h1 {
    font-size: 16px;
  }

  .page-subtitle {
    display: none;
  }

  .notes-main {
    flex-direction: row;
  }

  .notes-sidebar {
    width: 280px;
    height: auto;
    order: 1;
  }

}

/* 保存笔记弹窗样式 */
.save-note-dialog {
  .el-dialog {
    background: rgba(255, 255, 255, 0.25);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.18);
    border-radius: 16px;
    box-shadow: 
      8px 8px 16px rgba(209, 209, 212, 0.3),
      -8px -8px 16px rgba(255, 255, 255, 0.8);
  }

  .el-dialog__header {
    background: rgba(255, 255, 255, 0.1);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 16px 16px 0 0;
    padding: 20px 24px;
  }

  .el-dialog__title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-dialog__footer {
    padding: 16px 24px 24px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
  }
}

.save-note-form {
  .form-item-custom {
    margin-bottom: 20px;

    .el-form-item__label {
      font-weight: 500;
      color: #303133;
      text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
    }
  }

  .custom-select,
  .custom-input {
    width: 100%;
    
    .el-input__wrapper,
    .el-select__wrapper {
      background: #f0f0f3;
      box-shadow: 
        inset 8px 8px 16px #d1d1d4,
        inset -8px -8px 16px #ffffff;
      border-radius: 12px;
      border: none;
      padding: 12px 16px;
      transition: all 0.3s ease;
    }

    .el-input__wrapper:focus-within,
    .el-select__wrapper:focus-within {
      box-shadow: 
        inset 4px 4px 8px #d1d1d4,
        inset -4px -4px 8px #ffffff,
        0 0 0 2px rgba(64, 158, 255, 0.2);
    }

    .el-input__inner,
    .el-select__input {
      background: transparent;
      border: none;
      color: #303133;
      font-size: 14px;
    }

    .el-input__inner::placeholder,
    .el-select__placeholder {
      color: #909399;
    }
  }

  .custom-checkbox {
    .el-checkbox__input {
      .el-checkbox__inner {
        background: #f0f0f3;
        box-shadow: 
          inset 4px 4px 8px #d1d1d4,
          inset -4px -4px 8px #ffffff;
        border-radius: 6px;
        border: none;
        width: 18px;
        height: 18px;
      }

      &.is-checked .el-checkbox__inner {
        background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
        box-shadow: 
          4px 4px 8px #d1d1d4,
          -4px -4px 8px #ffffff;
      }
    }

    .el-checkbox__label {
      color: #303133;
      font-weight: 500;
      text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
    }

    .checkbox-hint {
      color: #909399;
      font-size: 12px;
      font-weight: normal;
      margin-left: 4px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;

  .cancel-btn,
  .confirm-btn {
    background: #f0f0f3;
    box-shadow: 
      8px 8px 16px #d1d1d4,
      -8px -8px 16px #ffffff;
    border-radius: 12px;
    border: none;
    padding: 10px 20px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .cancel-btn {
    color: #606266;

    &:hover {
      box-shadow: 
        4px 4px 8px #d1d1d4,
        -4px -4px 8px #ffffff;
      transform: translateY(-2px);
    }

    &:active {
      box-shadow: 
        inset 4px 4px 8px #d1d1d4,
        inset -4px -4px 8px #ffffff;
      transform: translateY(0);
    }
  }

  .confirm-btn {
    background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
    color: white;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);

    &:hover {
      box-shadow: 
        4px 4px 8px #d1d1d4,
        -4px -4px 8px #ffffff,
        0 0 12px rgba(64, 158, 255, 0.3);
      transform: translateY(-2px);
    }

    &:active {
      box-shadow: 
        inset 2px 2px 4px rgba(0, 0, 0, 0.1),
        4px 4px 8px #d1d1d4,
        -4px -4px 8px #ffffff;
      transform: translateY(0);
    }
  }
}

/* Element Plus 下拉选项样式覆盖 */
.el-select-dropdown {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 12px;
  box-shadow: 
    8px 8px 16px rgba(209, 209, 212, 0.3),
    -8px -8px 16px rgba(255, 255, 255, 0.8);

  .el-select-dropdown__item {
    padding: 12px 16px;
    color: #303133;
    transition: all 0.3s ease;

    &:hover {
      background: rgba(64, 158, 255, 0.1);
      color: #409EFF;
    }

    &.selected {
      background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(103, 194, 58, 0.1) 100%);
      color: #409EFF;
      font-weight: 500;
    }
  }
}

/* 响应式设计 - 保存弹窗 */
@media (max-width: 768px) {
  .save-note-dialog {
    .el-dialog {
      width: 95% !important;
      margin: 5vh auto;
    }

    .el-dialog__body {
      padding: 16px;
    }

    .save-note-form {
      .form-item-custom {
        margin-bottom: 16px;
      }

      .el-form-item__label {
        font-size: 13px;
      }
    }

    .dialog-footer {
      flex-direction: column;
      gap: 8px;

      .cancel-btn,
      .confirm-btn {
        width: 100%;
        justify-content: center;
      }
    }
  }
}

/* 横屏模式下的额外样式 */
@media (max-height: 600px) and (orientation: landscape) {
  .editor-container {
    order: 2;
  }

  .sidebar-header {
    padding: 8px 12px;
  }

  .filter-tabs {
    flex-direction: column;
    gap: 2px;
  }

  .filter-tab {
    padding: 4px 8px;
  }
}

/* 卡片视图响应式 */
@media (max-width: 767px) {
  .notes-list.card-view {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .note-item.card-item {
    min-height: auto;
  }
}

@media (max-width: 480px) {
  .notes-list.card-view {
    gap: 8px;
  }
}

/* 触摸设备优化 */
@media (hover: none) and (pointer: coarse) {
  .action-btn,
  .mode-btn,
  .filter-tab,
  .note-action-btn {
    min-height: 44px;
    min-width: 44px;
  }

  .note-item {
    padding: 16px;
  }

  .sidebar-toggle {
    width: 44px;
    height: 44px;
  }
}

/* 高对比度模式支持 */
@media (prefers-contrast: high) {
  .neumorphism-raised,
  .neumorphism-inset {
    border: 1px solid #000;
  }

  .frosted-glass {
    background: rgba(255, 255, 255, 0.9);
    border: 1px solid #000;
  }
}

/* 减少动画模式支持 */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* 新拟态设计辅助类 */
.neumorphism-raised {
  background: #f0f0f3;
  box-shadow: 8px 8px 16px #d1d1d4,
  -8px -8px 16px #ffffff;
}

.neumorphism-inset {
  background: #f0f0f3;
  box-shadow: inset 8px 8px 16px #d1d1d4,
  inset -8px -8px 16px #ffffff;
}

.neumorphism-hover:hover {
  box-shadow: 4px 4px 8px #d1d1d4,
  -4px -4px 8px #ffffff;
  transform: translateY(-2px);
  transition: all 0.3s ease;
}

.frosted-glass {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
}

/* 专注模式退出按钮样式 */
.focus-mode-exit {
  position: absolute;
  top: 1rem;
  right: 1rem;
  z-index: 1001;
}

.exit-focus-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: rgba(220, 38, 38, 0.9);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
  backdrop-filter: blur(10px);
}

.exit-focus-btn:hover {
  background: rgba(220, 38, 38, 1);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(220, 38, 38, 0.4);
}

.exit-icon {
  font-size: 1rem;
}

/* 专注模式下的笔记容器样式 */
.notes-container.focus-mode {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100vw;
  height: 100vh;
  padding: 0;
  z-index: 999;
  background: #f0f0f3;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.notes-container.focus-mode .notes-header,
.notes-container.focus-mode .notes-sidebar {
  display: none;
}

.notes-container.focus-mode .notes-main {
  width: 100%;
  height: 100%;
  padding: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.notes-container.focus-mode .editor-container {
  width: 100%;
  height: 100%;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.notes-container.focus-mode .editor-wrapper {
  border-radius: 0;
  box-shadow: none;
  height: 100%;
  margin: 0;
  padding: 2rem;
  transition: all 0.3s ease;
}

/* 专注模式按钮样式 */
.immersive-btn {
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.immersive-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.immersive-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.immersive-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.immersive-btn:hover::before {
  left: 100%;
}

/* 平滑过渡动画 */
.notes-header,
.notes-sidebar,
.notes-main,
.editor-container,
.editor-wrapper {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 加载状态样式 */
.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  width: 100%;
}

.loading-content {
  text-align: center;
  color: var(--text-secondary);
}

.loading-icon {
  font-size: 2rem;
  margin-bottom: 1rem;
  color: var(--primary-color);
}

.loading-content p {
  margin: 0;
  font-size: 1rem;
}

/* 空状态样式 */
.empty-notes-state,
.editor-empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  width: 100%;
  padding: 2rem;
}

.empty-content {
  text-align: center;
  max-width: 480px;
  width: 100%;
}

/* 空状态插图 */
.empty-illustration {
  position: relative;
  margin-bottom: 2rem;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.empty-icon-primary {
  font-size: 4rem;
  color: #002FA7;
  opacity: 0.8;
  z-index: 2;
}

.empty-icon-secondary {
  font-size: 2rem;
  color: #517B4D;
  opacity: 0.6;
  position: absolute;
  top: 10px;
  right: 20px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

/* 空状态文本 */
.empty-title {
  margin: 0 0 1rem 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.empty-description {
  margin: 0 0 2rem 0;
  font-size: 1rem;
  color: #606266;
  line-height: 1.6;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

/* 空状态操作按钮 */
.empty-actions {
  margin-bottom: 2.5rem;
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.primary-action-btn {
  padding: 1rem 2rem;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    8px 8px 16px rgba(0, 47, 167, 0.15),
    -8px -8px 16px rgba(255, 255, 255, 0.7);
}

.primary-action-btn:hover {
  transform: translateY(-3px);
  box-shadow: 
    12px 12px 24px rgba(0, 47, 167, 0.2),
    -12px -12px 24px rgba(255, 255, 255, 0.8);
}

.primary-action-btn:active {
  transform: translateY(-1px);
  box-shadow: 
    4px 4px 8px rgba(0, 47, 167, 0.2),
    -4px -4px 8px rgba(255, 255, 255, 0.8);
}

.secondary-action-btn {
  padding: 0.75rem 1.5rem;
  background: #f0f0f3;
  color: #606266;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  box-shadow: 
    6px 6px 12px #d1d1d4,
    -6px -6px 12px #ffffff;
}

.secondary-action-btn:hover {
  transform: translateY(-2px);
  color: #303133;
  box-shadow: 
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
}

/* 空状态提示 */
.empty-tips {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 360px;
  margin: 0 auto;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: rgba(240, 240, 243, 0.6);
  border-radius: 8px;
  font-size: 0.875rem;
  color: #606266;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.tip-item:hover {
  background: rgba(240, 240, 243, 0.8);
  transform: translateX(4px);
}

.tip-icon {
  font-size: 1rem;
  color: #002FA7;
  opacity: 0.7;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .empty-notes-state,
  .editor-empty-state {
    padding: 1.5rem 1rem;
    min-height: 300px;
  }
  
  .empty-illustration {
    height: 80px;
    margin-bottom: 1.5rem;
  }
  
  .empty-icon-primary {
    font-size: 3rem;
  }
  
  .empty-icon-secondary {
    font-size: 1.5rem;
  }
  
  .empty-title {
    font-size: 1.25rem;
  }
  
  .empty-description {
    font-size: 0.9rem;
  }
  
  .primary-action-btn {
    padding: 0.875rem 1.5rem;
    font-size: 0.9rem;
  }
  
  .empty-tips {
    gap: 0.75rem;
  }
  
  .tip-item {
    padding: 0.625rem 0.875rem;
    font-size: 0.8rem;
  }
}

/* 刷新按钮样式 */
.refresh-btn {
  background: var(--secondary-color) !important;
  color: #ffffff !important;
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 专注模式进入动画 */
@keyframes fadeInScale {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.notes-container.focus-mode .editor-wrapper {
  animation: fadeInScale 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 响应式专注模式 */
@media (max-width: 768px) {
  .notes-container.focus-mode .editor-wrapper {
    padding: 1rem;
  }
  
  .notes-container.focus-mode .editor-content {
    height: calc(100vh - 120px);
  }
}
</style>
