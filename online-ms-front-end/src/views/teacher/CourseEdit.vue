<template>
  <div class="course-edit-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <el-button
              type="text"
              @click="$router.go(-1)"
              class="back-btn"
          >
            <font-awesome-icon :icon="['fas', 'arrow-left']"/>
            返回课程列表
          </el-button>
          <h1 class="page-title">
            {{ isEdit ? '编辑课程' : '创建新课程' }}
          </h1>
        </div>
        <div class="header-actions">
          <el-button class="action-btn save-btn">
            <font-awesome-icon :icon="['fas', 'save']"/>
            保存草稿
          </el-button>
          <el-button type="primary" class="action-btn publish-btn" @click="handlePublishOrUpdate">
            <font-awesome-icon :icon="['fas', 'rocket']"/>
            {{ isEdit ? '更新基本信息' : '发布课程' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="content-wrapper">
        <!-- 左侧编辑区域 -->
        <div class="edit-area">
          <!-- 基本信息 -->
          <div class="section-card">
            <div class="section-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'info-circle']"/>
                基本信息
              </h3>
            </div>
            <div class="section-content">
              <el-form :model="courseForm" label-width="100px" ref="basicInfoForm">
                <el-form-item label="课程标题">
                  <el-input
                      v-model="courseForm.title"
                      placeholder="请输入课程标题"
                      class="form-input"
                  />
                </el-form-item>

                <el-form-item label="课程分类">
                  <el-select
                      v-model="courseForm.category"
                      placeholder="请选择课程分类"
                      class="form-select"
                      :loading="categoryLoading"
                  >
                    <template v-for="category in categoryTree" :key="category.id">
                      <el-option-group :label="category.name">
                        <el-option
                            v-for="subCategory in category.children"
                            :key="subCategory.id"
                            :label="subCategory.name"
                            :value="subCategory.id"
                        />
                      </el-option-group>
                    </template>
                  </el-select>
                </el-form-item>

                <el-form-item label="难度等级">
                  <el-select
                      v-model="courseForm.level"
                      placeholder="请选择难度等级"
                      class="form-select"
                  >
                    <el-option label="初级" value="beginner"/>
                    <el-option label="中级" value="intermediate"/>
                    <el-option label="高级" value="advanced"/>
                  </el-select>
                </el-form-item>

                <el-form-item label="课程价格">
                  <el-input-number
                      v-model="courseForm.price"
                      :min="0"
                      :precision="2"
                      :disabled="courseForm.isFree"
                      class="form-number"
                  />
                </el-form-item>

                <el-form-item label="原价">
                  <el-input-number
                      v-model="courseForm.originalPrice"
                      :min="0"
                      :precision="2"
                      :disabled="courseForm.isFree"
                      class="form-number"
                      placeholder="用于显示折扣"
                  />
                </el-form-item>

                <el-form-item label="适合人群">
                  <div class="input-with-ai-suggestion">
                    <el-input
                        v-model="courseForm.targetAudience"
                        placeholder="请输入适合人群"
                        class="form-input"
                    />
                    <el-button
                        type="primary"
                        size="small"
                        class="ai-suggestion-btn"
                        @click="getAISuggestionForTargetAudience"
                    >
                      <font-awesome-icon :icon="['fas', 'magic']"/>
                      AI建议
                    </el-button>
                  </div>
                </el-form-item>

                <el-form-item label="技术要求">
                  <div class="input-with-ai-suggestion">
                    <el-input
                        v-model="courseForm.requirements"
                        placeholder="请输入技术要求"
                        class="form-input"
                    />
                    <el-button
                        type="primary"
                        size="small"
                        class="ai-suggestion-btn"
                        @click="getAISuggestionForRequirements"
                    >
                      <font-awesome-icon :icon="['fas', 'magic']"/>
                      AI建议
                    </el-button>
                  </div>
                </el-form-item>

                <el-form-item label="课程封面">
                  <div class="upload-area" @click="triggerFileUpload" :class="{ 'uploading': uploading }">
                    <div v-if="!coverUrl && !uploading" class="upload-placeholder">
                      <font-awesome-icon :icon="['fas', 'cloud-upload-alt']"/>
                      <p>点击或拖拽上传课程封面</p>
                      <p class="upload-tip">支持 JPG、PNG 格式，建议尺寸 16:9，文件大小不超过10MB</p>
                    </div>
                    <div v-else-if="uploading" class="upload-loading">
                      <font-awesome-icon :icon="['fas', 'spinner']" spin/>
                      <p>正在上传...</p>
                    </div>
                    <div v-else class="upload-preview">
                      <img :src="coverUrl" alt="课程封面" class="cover-image"/>
                      <div class="upload-overlay">
                        <font-awesome-icon :icon="['fas', 'edit']"/>
                        <p>点击更换封面</p>
                      </div>
                    </div>
                  </div>
                </el-form-item>
              </el-form>
              <div class="avatar-actions" style="margin-left: 40px;">
                <el-button size="small" class="neu-button save-btn" type="success" @click="saveCourseCover">
                  <font-awesome-icon :icon="['fas', 'save']"/>
                  保存封面
                </el-button>
              </div>
            </div>
          </div>

          <!-- 课程描述 -->
          <div class="section-card">
            <div class="section-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'align-left']"/>
                课程描述
              </h3>
              <div class="description-stats">
                <span class="char-count">{{ courseForm.description.length }}/2000</span>
              </div>
            </div>
            <div class="section-content">
              <div class="modern-editor-container">
                <!-- 编辑器工具栏 -->
                <div class="modern-toolbar">
                  <div class="toolbar-group">
                    <button class="modern-toolbar-btn" title="加粗" @click="makeBold">
                      <font-awesome-icon :icon="['fas', 'bold']"/>
                    </button>
                    <button class="modern-toolbar-btn" title="斜体" @click="makeItalic">
                      <font-awesome-icon :icon="['fas', 'italic']"/>
                    </button>
                    <button class="modern-toolbar-btn" title="下划线" @click="makeUnderline">
                      <font-awesome-icon :icon="['fas', 'underline']"/>
                    </button>
                  </div>
                  <div class="toolbar-divider"></div>
                  <div class="toolbar-group">
                    <button class="modern-toolbar-btn" title="无序列表" @click="insertList">
                      <font-awesome-icon :icon="['fas', 'list-ul']"/>
                    </button>
                    <button class="modern-toolbar-btn" title="有序列表" @click="insertNumberedList">
                      <font-awesome-icon :icon="['fas', 'list-ol']"/>
                    </button>
                  </div>
                  <div class="toolbar-divider"></div>
                  <div class="toolbar-group">
                    <button class="modern-toolbar-btn" title="插入链接" @click="insertLink">
                      <font-awesome-icon :icon="['fas', 'link']"/>
                    </button>
                    <button class="modern-toolbar-btn" title="插入图片" @click="insertImage">
                      <font-awesome-icon :icon="['fas', 'image']"/>
                    </button>
                    <button class="modern-toolbar-btn" title="插入代码" @click="insertCode">
                      <font-awesome-icon :icon="['fas', 'code']"/>
                    </button>
                  </div>
                  <div class="toolbar-spacer"></div>
                  <div class="toolbar-group">
                    <button class="modern-toolbar-btn preview-toggle" title="预览模式" @click="togglePreview">
                      <font-awesome-icon :icon="['fas', 'eye']"/>
                    </button>
                  </div>
                </div>

                <!-- 编辑器主体 -->
                <div class="modern-editor-body">
                  <div class="editor-card">
                    <div class="editor-header">
                      <div class="editor-tabs">
                        <button class="editor-tab" :class="{ active: !isPreviewMode }"
                                @click="editorMode = 'edit'; isPreviewMode = false">
                          <font-awesome-icon :icon="['fas', 'edit']"/>
                          编辑
                        </button>
                        <button class="editor-tab" :class="{ active: isPreviewMode }"
                                @click="editorMode = 'preview'; isPreviewMode = true">
                          <font-awesome-icon :icon="['fas', 'eye']"/>
                          预览
                        </button>
                      </div>
                    </div>
                    <div class="editor-content">
                      <div v-if="!isPreviewMode" class="custom-textarea-container">
                        <textarea
                            v-model="courseForm.description"
                            ref="descriptionTextarea"
                            placeholder="请输入详细的课程描述...\n\n支持 Markdown 语法：\n- **粗体文本**\n- *斜体文本*\n- [链接](URL)\n- ![图片](URL)\n- `代码`\n\n让我们开始创建精彩的课程内容吧！"
                            class="custom-textarea"
                            maxlength="2000"
                        ></textarea>
                        <div class="textarea-overlay">
                          <div class="line-numbers">
                            <span v-for="n in textareaLines" :key="n" class="line-number">{{ n }}</span>
                          </div>
                        </div>
                      </div>
                      <div v-else class="preview-content-area">
                        <div class="markdown-preview"
                             v-html="renderMarkdown(courseForm.description || '暂无内容...')"></div>
                      </div>
                    </div>
                  </div>

                  <!-- 快捷操作面板 -->
                  <div class="quick-actions-panel">
                    <div class="quick-action-card" @click="openAIAssistant">
                      <div class="action-icon">
                        <font-awesome-icon :icon="['fas', 'magic']"/>
                      </div>
                      <div class="action-content">
                        <h4>AI 助手</h4>
                        <p>让 AI 帮你优化描述</p>
                      </div>
                    </div>
                    <div class="quick-action-card" @click="openTemplateLibrary">
                      <div class="action-icon">
                        <font-awesome-icon :icon="['fas', 'template']"/>
                      </div>
                      <div class="action-content">
                        <h4>模板库</h4>
                        <p>选择预设模板</p>
                      </div>
                    </div>
                    <div class="quick-action-card" @click="autoSave">
                      <div class="action-icon">
                        <font-awesome-icon :icon="['fas', 'save']"/>
                      </div>
                      <div class="action-content">
                        <h4>自动保存</h4>
                        <p>已保存 2 分钟前</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 课程大纲 -->
          <div class="section-card">
            <div class="section-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'list']"/>
                课程大纲
              </h3>
              <div class="header-actions">
                <el-button class="add-chapter-btn" @click="goToCourseOutline">
                  <font-awesome-icon :icon="['fas', 'plus']"/>
                  添加章节
                </el-button>
                <el-button type="primary" class="update-outline-btn" @click="updateCourseOutline">
                  <font-awesome-icon :icon="['fas', 'save']"/>
                  更新大纲
                </el-button>
              </div>
            </div>
            <div class="section-content">
              <el-form :model="outlineForm" ref="outlineForm">
                <div class="curriculum-builder">
                  <div
                      v-for="(chapter, chapterIndex) in outlineForm.chapters"
                      :key="chapterIndex"
                      class="chapter-item"
                  >
                    <div class="chapter-header">
                      <div class="chapter-info">
                        <font-awesome-icon :icon="['fas', 'grip-vertical']" class="drag-handle"/>
                        <span class="chapter-number">第{{ chapterIndex + 1 }}章</span>
                        <el-input
                            v-model="chapter.title"
                            placeholder="章节标题"
                            class="chapter-title-input"
                        />
                      </div>
                      <div class="chapter-actions">
                        <el-button size="small" class="action-btn">
                          <font-awesome-icon :icon="['fas', 'plus']"/>
                          添加课时
                        </el-button>
                        <el-button size="small" class="action-btn delete-btn">
                          <font-awesome-icon :icon="['fas', 'trash']"/>
                        </el-button>
                      </div>
                    </div>

                    <div class="lessons-list">
                      <div
                          v-for="(lesson, lessonIndex) in chapter.lessons"
                          :key="lessonIndex"
                          class="lesson-item"
                      >
                        <div class="lesson-info">
                          <font-awesome-icon :icon="['fas', 'grip-vertical']" class="drag-handle"/>
                          <font-awesome-icon :icon="['fas', 'play-circle']" class="lesson-icon"/>
                          <el-input
                              v-model="lesson.title"
                              placeholder="课时标题"
                              class="lesson-title-input"
                          />
                          <span class="lesson-duration">{{ lesson.duration }}</span>
                        </div>
                        <div class="lesson-actions">
                          <el-button size="small" class="action-btn">
                            <font-awesome-icon :icon="['fas', 'edit']"/>
                          </el-button>
                          <el-button size="small" class="action-btn delete-btn">
                            <font-awesome-icon :icon="['fas', 'trash']"/>
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </el-form>
            </div>
          </div>
        </div>

        <!-- 右侧预览区域 -->
        <div class="preview-area">
          <div class="preview-card">
            <div class="preview-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'eye']"/>
                课程预览
              </h3>
            </div>
            <div class="preview-content">
              <div class="course-preview">
                <div class="preview-cover">
                  <img :src="coverUrl || '/placeholder-course.jpg'" alt="课程封面"/>
                  <div class="preview-overlay">
                    <font-awesome-icon :icon="['fas', 'play']"/>
                  </div>
                </div>
                <div class="preview-info">
                  <h4 class="preview-title">{{ courseForm.title || '课程标题' }}</h4>
                  <!--                  <p class="preview-subtitle">{{ courseForm.subtitle || '课程简介' }}</p>-->
                  <div class="preview-meta">
                    <span class="meta-item">
                      <font-awesome-icon :icon="['fas', 'tag']"/>
                      {{ courseForm.category || '分类' }}
                    </span>
                    <span class="meta-item">
                      <font-awesome-icon :icon="['fas', 'signal']"/>
                      {{ courseForm.level || '难度' }}
                    </span>
                    <span class="meta-item price">
                      <font-awesome-icon :icon="['fas', 'yen-sign']"/>
                      {{ courseForm.price || 0 }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 发布设置 -->
          <div class="settings-card">
            <div class="settings-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'cog']"/>
                发布设置
              </h3>
            </div>
            <div class="settings-content">
              <div class="setting-item">
                <label class="setting-label">课程状态</label>
                <el-select v-model="courseForm.status" class="setting-select">
                  <el-option label="草稿" value="draft"/>
                  <el-option label="已发布" value="published"/>
                  <el-option label="已下架" value="archived"/>
                </el-select>
              </div>

              <div class="setting-item">
                <label class="setting-label">是否为免费课程</label>
                <el-switch v-model="courseForm.isFree" @change="handleFreeChange"/>
              </div>
              <div class="setting-item">
                <label class="setting-label">是否为公开课程</label>
                <el-switch v-model="courseForm.isPrivate"/>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {updateCoverTeacher, updateCourseCover, getCourseDetail, updateCourseInfo} from '@/api/teacher/teacherAPI.js'
import {getCourseCategoryTree} from '@/api/publicApi.js'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  getOptimizationAssistantResponse,
  getOptimizationAssistantCrowd,
  getOptimizationAssistantRequirements
} from '@/api/teacher/TeacherAIChatAPI.js'
import {getUserId} from '@/utils/tokenAnalysis.js'

const route = useRoute()
const router = useRouter()

// 判断是编辑还是创建
const isEdit = computed(() => route.params.id !== undefined)

// 上传相关的响应式变量
const coverUrl = ref('')
const uploading = ref(false)

// 课程分类数据
const categoryTree = ref([])
const categoryLoading = ref(false)

// 课程表单数据
const courseForm = ref({
  title: '',
  description: '',
  category: '',
  level: '',
  price: 0,
  originalPrice: 0,
  coverUrl: '',
  targetAudience: '',
  requirements: '',
  status: 'draft',
  isFree: false,
  isPrivate: false,
  publishTime: null
})

// 课程大纲表单数据
const outlineForm = ref({
  chapters: [
    {
      title: '第一章 课程介绍',
      lessons: [
        {title: '课程概述', duration: '10:30'},
        {title: '学习目标', duration: '8:45'}
      ]
    },
    {
      title: '第二章 基础知识',
      lessons: [
        {title: '基本概念', duration: '15:20'},
        {title: '环境搭建', duration: '12:10'}
      ]
    }
  ]
})

// 文件上传处理函数
const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支持 JPG、PNG 格式的图片')
    return
  }

  // 验证文件大小（限制为10MB）
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过10MB')
    return
  }

  uploadCover(file)
}

// 上传封面图片
const uploadCover = async (file) => {
  uploading.value = true

  try {
    const result = await updateCoverTeacher(file)
    // 使用 downloadUrl 或 permanentUrl 字段，而不是 url
    coverUrl.value = result.permanentUrl
    // 同步coverUrl变量
    courseForm.value.coverUrl = coverUrl.value
    ElMessage.success('封面上传成功')
  } catch (error) {
    ElMessage.error('封面上传失败：' + error.message)
  } finally {
    uploading.value = false
  }
}

// 触发文件选择
const triggerFileUpload = () => {
  const fileInput = document.createElement('input')
  fileInput.type = 'file'
  fileInput.accept = 'image/jpeg,image/jpg,image/png'
  fileInput.onchange = handleFileUpload
  fileInput.click()
}

// 保存课程封面
const saveCourseCover = async () => {
  try {
    // 检查是否有永久封面链接
    if (!coverUrl.value || !courseForm.value.coverUrl) {
      ElMessage.warning('请先上传封面图片')
      return
    }

    // 检查是否有课程ID（编辑模式下需要）
    if (!route.params.id) {
      ElMessage.warning('课程ID不存在，无法保存封面')
      return
    }

    // 调用API保存课程封面
    await updateCourseCover(coverUrl.value, route.params.id)
    ElMessage.success('课程封面保存成功')

    // 重载用户信息
  } catch (error) {
    console.error('保存课程封面失败:', error)
    ElMessage.error(`保存课程封面失败: ${error.message}`)
  }
}

// 处理免费课程切换
const handleFreeChange = (value) => {
  if (value) {
    // 当设置为免费课程时，将价格设为0
    courseForm.value.price = 0
    courseForm.value.originalPrice = 0
  }
}

// 加载课程分类数据
const loadCategoryTree = async () => {
  categoryLoading.value = true
  try {
    const categories = await getCourseCategoryTree()
    categoryTree.value = categories
  } catch (error) {
    console.error('加载课程分类失败:', error)
    ElMessage.error('加载课程分类失败: ' + error.message)
  } finally {
    categoryLoading.value = false
  }
}

// 获取分类ID通过分类名称
const getCategoryIdByName = (categoryName) => {
  if (!categoryName || !categoryTree.value.length) return ''
  
  for (const category of categoryTree.value) {
    if (category.children) {
      const subCategory = category.children.find(sub => sub.name === categoryName)
      if (subCategory) return subCategory.id
    }
  }
  return ''
}

// 加载课程详情
const loadCourseDetail = async () => {
  if (isEdit.value && route.params.id) {
    try {
      const courseDetail = await getCourseDetail(route.params.id)

      // 通过分类名称查找分类ID
      const categoryId = getCategoryIdByName(courseDetail.categories)

      // 填充表单数据
      courseForm.value = {
        title: courseDetail.title || '',
        description: courseDetail.description || '',
        category: categoryId, // 使用分类ID而不是分类名称
        level: courseDetail.level || '',
        price: courseDetail.price || 0,
        originalPrice: courseDetail.originalPrice || courseDetail.price || 0,
        coverUrl: courseDetail.cover || '',
        targetAudience: courseDetail.targetAudience || '',
        requirements: courseDetail.requirements || '',
        status: courseDetail.status || 'draft',
        isFree: courseDetail.price === 0,
        isPrivate: courseDetail.isPrivate === 0, // 当后端isPrivate为0时表示公开课程，开关应为选中状态
        publishTime: null
      }

      // 如果有课程大纲数据，则加载到outlineForm中
      if (courseDetail.chapters && courseDetail.chapters.length > 0) {
        outlineForm.value.chapters = courseDetail.chapters
      }

      // 同步封面URL
      coverUrl.value = courseDetail.cover || ''

      ElMessage.success('课程详情加载成功')
    } catch (error) {
      console.error('加载课程详情失败:', error)
      ElMessage.error('加载课程详情失败: ' + error.message)
    }
  }
}

// 计算文本区域行数
const textareaLines = computed(() => {
  const lines = courseForm.value.description.split('\n').length
  return Math.max(lines, 10) // 最少显示10行
})

// 编辑器状态
const editorMode = ref('edit') // 'edit' 或 'preview'
const isPreviewMode = ref(false)
const descriptionTextarea = ref(null)

// 字符计数状态
const charCountStatus = computed(() => {
  const count = courseForm.value.description.length
  if (count > 2000) return 'danger'
  if (count > 1500) return 'warning'
  return 'normal'
})

// 编辑器工具栏方法
const insertText = (before, after = '') => {
  const textarea = descriptionTextarea.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = courseForm.value.description.substring(start, end)
  const newText = before + selectedText + after

  courseForm.value.description =
      courseForm.value.description.substring(0, start) +
      newText +
      courseForm.value.description.substring(end)

  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + before.length, start + before.length + selectedText.length)
  })
}

// 格式化工具
const makeBold = () => {
  insertText('**', '**')
}

const makeItalic = () => {
  insertText('*', '*')
}

const makeUnderline = () => {
  insertText('<u>', '</u>')
}

const insertList = () => {
  insertText('\n- ')
}

const insertNumberedList = () => {
  insertText('\n1. ')
}

const insertLink = () => {
  insertText('[链接文本](', ')')
}

const insertImage = () => {
  insertText('![图片描述](', ')')
}

const insertCode = () => {
  insertText('`', '`')
}

const insertCodeBlock = () => {
  insertText('\n```\n', '\n```\n')
}

// 切换编辑/预览模式
const togglePreview = () => {
  isPreviewMode.value = !isPreviewMode.value
  editorMode.value = isPreviewMode.value ? 'preview' : 'edit'
}

// 快捷操作
const openAIAssistant = async () => {
  try {
    // 获取textarea中的内容
    let content = courseForm.value.description?.trim() || ''

    // 如果用户未填写任何内容，提供默认内容或提示
    if (!content) {
      // 检查是否有课程标题，如果有则使用标题作为默认内容
      if (courseForm.value.title?.trim()) {
        content = `课程名称：${courseForm.value.title.trim()}`
        // 显示确认对话框
        await ElMessageBox.confirm(
            `检测到您还未填写课程描述，将使用课程标题"${courseForm.value.title.trim()}"作为AI优化的基础内容，是否继续？`,
            'AI助手提示',
            {
              confirmButtonText: '继续优化',
              cancelButtonText: '取消',
              type: 'info'
            }
        )
      } else {
        // 如果连标题都没有，提示用户输入内容
        ElMessage.warning('请先填写课程标题或课程描述，然后再使用AI助手优化功能')
        return
      }
    }

    // 验证内容长度
    if (content.length > 2000) {
      ElMessage.error('内容长度超过2000字符限制，请先精简内容后再使用AI助手')
      return
    }

    // 显示加载提示
    const loadingMessage = ElMessage({
      message: 'AI助手正在为您优化课程描述，请稍候...',
      type: 'info',
      duration: 0,
      showClose: false
    })

    let optimizedContent = ''

    // 调用AI优化接口
    await getOptimizationAssistantResponse(
        {question: content},
        // onData回调 - 处理流式响应
        (aiResponse) => {
          if (aiResponse && aiResponse.content) {
            optimizedContent += aiResponse.content
          }
        },
        // onComplete回调 - 流结束时的处理
        async () => {
          loadingMessage.close()

          if (optimizedContent.trim()) {
            // 显示优化结果确认对话框
            try {
              await ElMessageBox.confirm(
                  `AI助手已为您生成优化后的课程描述，是否应用到课程描述中？\n\n优化后的内容：\n${optimizedContent.substring(0, 200)}${optimizedContent.length > 200 ? '...' : ''}`,
                  'AI优化结果',
                  {
                    confirmButtonText: '应用优化',
                    cancelButtonText: '取消',
                    type: 'success',
                    customClass: 'ai-result-dialog'
                  }
              )

              // 用户确认后，将优化内容应用到表单
              courseForm.value.description = optimizedContent.trim()
              ElMessage.success('AI优化内容已应用到课程描述中')

              // 切换到编辑模式以便用户查看结果
              isPreviewMode.value = false
              editorMode.value = 'edit'

            } catch (error) {
              if (error !== 'cancel') {
                console.error('应用AI优化内容时出错:', error)
              }
            }
          } else {
            ElMessage.warning('AI助手未能生成有效的优化内容，请稍后重试')
          }
        },
        // onError回调 - 错误处理
        (error) => {
          loadingMessage.close()
          console.error('AI助手优化失败:', error)
          ElMessage.error('AI助手服务暂时不可用，请稍后重试')
        }
    )

  } catch (error) {
    console.error('调用AI助手时发生错误:', error)
    if (error !== 'cancel') {
      ElMessage.error('AI助手功能调用失败，请检查网络连接后重试')
    }
  }
}

const openTemplateLibrary = () => {
  ElMessage.info('模板库功能开发中...')
}

const autoSave = () => {
  ElMessage.success('内容已自动保存')
}

// AI建议功能
const getAISuggestionForTargetAudience = async () => {
  try {
    // 获取课程标题作为参数
    const courseTitle = courseForm.value.title?.trim() || ''

    if (!courseTitle) {
      ElMessage.warning('请先填写课程标题')
      return
    }

    // 显示加载提示
    const loadingMessage = ElMessage({
      message: 'AI正在为您生成适合人群建议...',
      type: 'info',
      duration: 0,
      showClose: false
    })

    let aiSuggestion = ''

    await getOptimizationAssistantCrowd(
        {question: courseTitle},
        (data) => {
          // 处理流式响应数据
          if (data && data.content) {
            aiSuggestion += data.content
          }
        },
        async () => {
          // 流结束后的处理
          loadingMessage.close()

          if (aiSuggestion.trim()) {
            // 显示AI建议确认对话框
            try {
              await ElMessageBox.confirm(
                  `AI建议的适合人群：\n\n${aiSuggestion}\n\n是否应用此建议？`,
                  'AI建议',
                  {
                    confirmButtonText: '应用建议',
                    cancelButtonText: '取消',
                    type: 'info',
                    customClass: 'ai-suggestion-dialog'
                  }
              )

              // 用户确认后应用建议
              courseForm.value.targetAudience = aiSuggestion.trim()
              ElMessage.success('已应用AI建议到适合人群')
            } catch {
              // 用户取消，不做任何操作
            }
          } else {
            ElMessage.warning('AI未能生成有效建议，请稍后重试')
          }
        },
        (error) => {
          // 错误处理
          loadingMessage.close()
          console.error('AI适合人群建议失败:', error)
          ElMessage.error('AI建议服务暂时不可用，请稍后重试')
        }
    )

  } catch (error) {
    console.error('调用AI适合人群建议时发生错误:', error)
    ElMessage.error('AI建议功能调用失败，请检查网络连接后重试')
  }
}

const getAISuggestionForRequirements = async () => {
  try {
    // 获取课程标题作为参数
    const courseTitle = courseForm.value.title?.trim() || ''

    if (!courseTitle) {
      ElMessage.warning('请先填写课程标题')
      return
    }

    // 显示加载提示
    const loadingMessage = ElMessage({
      message: 'AI正在为您生成技术要求建议...',
      type: 'info',
      duration: 0,
      showClose: false
    })

    let aiSuggestion = ''

    await getOptimizationAssistantRequirements(
        {question: courseTitle},
        (data) => {
          // 处理流式响应数据
          if (data && data.content) {
            aiSuggestion += data.content
          }
        },
        async () => {
          // 流结束后的处理
          loadingMessage.close()

          if (aiSuggestion.trim()) {
            // 显示AI建议确认对话框
            try {
              await ElMessageBox.confirm(
                  `AI建议的技术要求：\n\n${aiSuggestion}\n\n是否应用此建议？`,
                  'AI建议',
                  {
                    confirmButtonText: '应用建议',
                    cancelButtonText: '取消',
                    type: 'info',
                    customClass: 'ai-suggestion-dialog'
                  }
              )

              // 用户确认后应用建议
              courseForm.value.requirements = aiSuggestion.trim()
              ElMessage.success('已应用AI建议到技术要求')
            } catch {
              // 用户取消，不做任何操作
            }
          } else {
            ElMessage.warning('AI未能生成有效建议，请稍后重试')
          }
        },
        (error) => {
          // 错误处理
          loadingMessage.close()
          console.error('AI技术要求建议失败:', error)
          ElMessage.error('AI建议服务暂时不可用，请稍后重试')
        }
    )

  } catch (error) {
    console.error('调用AI技术要求建议时发生错误:', error)
    ElMessage.error('AI建议功能调用失败，请检查网络连接后重试')
  }
}

// 更新课程大纲
const updateCourseOutline = () => {
  console.log('更新课程大纲:', outlineForm.value)
  ElMessage.success('课程大纲更新成功')
}

// 跳转到课程大纲编辑页面
const goToCourseOutline = () => {
  const courseId = route.params.id
  if (!courseId) {
    ElMessage.error('请先保存课程基本信息')
    return
  }
  router.push({
    path: '/teacher/course-outline',
    query: { courseId: courseId }
  })
}

// 处理发布或更新按钮点击
const handlePublishOrUpdate = () => {
  if (isEdit.value) {
    updateBasicInfo()
  } else {
    publishCourse()
  }
}

// 更新基本信息
const updateBasicInfo = async () => {
  try {
    // 验证必填字段
    if (!courseForm.value.title?.trim()) {
      ElMessage.error('请填写课程标题')
      return
    }

    if (!courseForm.value.category) {
      ElMessage.error('请选择课程分类')
      return
    }

    if (!route.params.id) {
      ElMessage.error('课程ID不存在，无法更新')
      return
    }

    // 构建TeacherCourseVo对象
    const courseVo = {
      title: courseForm.value.title,
      description: courseForm.value.description || '',
      coverImageUrl: courseForm.value.coverUrl || '',
      teacherId: getUserId(),
      categoryId: courseForm.value.category,
      status: courseForm.value.status || 'draft',
      enrollmentCount: null, // 后端维护
      isDeleted: 0, // 默认未删除
      isPrivate: courseForm.value.isPrivate ? 1 : 0
    }

    // 构建CoursePropertiesVo对象
    const coursePropertiesVo = {
      courseId: route.params.id,
      level: courseForm.value.level || 'beginner',
      isNew: 1, // 默认为新课程
      targetAudience: courseForm.value.targetAudience || '',
      requirements: courseForm.value.requirements || '',
      price: courseForm.value.price || 0,
      originalPrice: courseForm.value.originalPrice || courseForm.value.price || 0
    }

    // 显示加载提示
    const loadingMessage = ElMessage({
      message: '正在更新课程信息...',
      type: 'info',
      duration: 0,
      showClose: false
    })

    // 调用API更新课程信息
    await updateCourseInfo(route.params.id, courseVo, coursePropertiesVo)

    loadingMessage.close()
    ElMessage.success('课程基本信息更新成功')

    // 重新加载课程详情以获取最新数据
    await loadCourseDetail()

  } catch (error) {
    console.error('更新课程基本信息失败:', error)
    ElMessage.error(`更新失败: ${error.message}`)
  }
}

// 发布课程（创建模式）
const publishCourse = () => {
  ElMessage.info('发布课程功能开发中...')
}

// 渲染 Markdown（简单实现）
const renderMarkdown = (text) => {
  return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/<u>(.*?)<\/u>/g, '<u>$1</u>')
      .replace(/`(.*?)`/g, '<code>$1</code>')
      .replace(/\n/g, '<br>')
}

// 组件挂载时加载数据
onMounted(() => {
  loadCategoryTree() // 加载课程分类
  loadCourseDetail() // 如果是编辑模式，加载课程数据
})
</script>

<style scoped>
.course-edit-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f0f3 0%, #e8e8eb 100%);
  padding: 0;
}

/* 页面头部 */
.page-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 20px 30px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  color: #002FA7;
  font-size: 14px;
  padding: 8px 0;
}

.back-btn:hover {
  color: #517B4D;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 10px 20px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.preview-btn {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  color: #6c757d;
}

.save-btn {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  color: #856404;
}

.publish-btn {
  background: linear-gradient(135deg, #002FA7, #517B4D);
  border: none;
  color: white;
}

/* 主要内容区域 */
.main-content {
  padding: 30px;
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 30px;
}

/* 编辑区域 */
.edit-area {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 20px 20px 40px rgba(0, 0, 0, 0.1),
  -20px -20px 40px rgba(255, 255, 255, 0.8),
  inset 0 0 0 1px rgba(255, 255, 255, 0.3);
  overflow: hidden;
}

.section-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header .header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-header i {
  color: #002FA7;
}

.section-content {
  padding: 24px;
}

/* 表单样式 */
.form-input,
.form-select,
.form-number {
  width: 100%;
  border-radius: 12px;
}

.form-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  box-shadow: inset 2px 2px 4px rgba(0, 0, 0, 0.1);
}

/* 上传区域 */
.upload-area {
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  background: rgba(249, 250, 251, 0.5);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-area:hover {
  border-color: #002FA7;
  background: rgba(0, 47, 167, 0.05);
}

.upload-area.uploading {
  border-color: #002FA7;
  background: rgba(0, 47, 167, 0.1);
  cursor: not-allowed;
}

.upload-placeholder i {
  font-size: 48px;
  color: #9ca3af;
  margin-bottom: 16px;
}

.upload-placeholder p {
  margin: 8px 0;
  color: #6b7280;
}

.upload-tip {
  font-size: 12px;
  color: #9ca3af;
}

.upload-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.upload-loading i {
  font-size: 32px;
  color: #002FA7;
}

.upload-loading p {
  color: #002FA7;
  font-weight: 500;
}

.upload-preview {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: white;
  gap: 8px;
}

.upload-preview:hover .upload-overlay {
  opacity: 1;
}

.upload-overlay i {
  font-size: 24px;
}

.upload-overlay p {
  margin: 0;
  font-size: 14px;
}

/* 现代化编辑器样式 */
.description-stats {
  display: flex;
  align-items: center;
  gap: 12px;
}

.char-count {
  font-size: 12px;
  color: #6c757d;
  background: rgba(108, 117, 125, 0.1);
  padding: 4px 8px;
  border-radius: 12px;
  font-weight: 500;
}

.modern-editor-container {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 16px;
  padding: 20px;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 现代化工具栏 */
.modern-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.toolbar-group {
  display: flex;
  gap: 4px;
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: linear-gradient(to bottom, transparent, #dee2e6, transparent);
}

.toolbar-spacer {
  flex: 1;
}

.modern-toolbar-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: 8px;
  color: #495057;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.modern-toolbar-btn:before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(0, 47, 167, 0.1), transparent);
  transition: left 0.5s;
}

.modern-toolbar-btn:hover {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.1), rgba(81, 123, 77, 0.1));
  color: #002FA7;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 47, 167, 0.2);
}

.modern-toolbar-btn:hover:before {
  left: 100%;
}

.modern-toolbar-btn:active {
  transform: translateY(0);
}

.preview-toggle {
  background: linear-gradient(135deg, #002FA7, #517B4D);
  color: white;
}

.preview-toggle:hover {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.8), rgba(81, 123, 77, 0.8));
  color: white;
}

/* 编辑器主体 */
.modern-editor-body {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 20px;
}

.editor-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
}

.editor-header {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  padding: 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.editor-tabs {
  display: flex;
}

.editor-tab {
  flex: 1;
  padding: 12px 20px;
  border: none;
  background: transparent;
  color: #6c757d;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 500;
  position: relative;
}

.editor-tab.active {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.1), rgba(81, 123, 77, 0.1));
  color: #002FA7;
}

.editor-tab.active:after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(135deg, #002FA7, #517B4D);
  border-radius: 3px 3px 0 0;
}

.editor-tab:hover:not(.active) {
  background: rgba(0, 47, 167, 0.05);
  color: #002FA7;
}

.editor-content {
  padding: 0;
}

/* 自定义文本区域 */
.custom-textarea-container {
  position: relative;
  min-height: 400px;
}

.custom-textarea {
  width: 100%;
  min-height: 400px;
  border: none;
  outline: none;
  padding: 20px 20px 20px 60px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #2c3e50;
  background: transparent;
  resize: vertical;
  overflow-y: auto;
}

.custom-textarea::placeholder {
  color: #adb5bd;
  font-style: italic;
}

.textarea-overlay {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  padding: 20px 0;
}

.line-numbers {
  display: flex;
  flex-direction: column;
  width: 40px;
  padding-left: 12px;
}

.line-number {
  height: 22.4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: #adb5bd;
  text-align: right;
  padding-right: 8px;
  user-select: none;
}

/* 快捷操作面板 */
.quick-actions-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-action-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 12px;
}

.quick-action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 47, 167, 0.15);
  background: rgba(255, 255, 255, 0.95);
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #002FA7, #517B4D);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.action-content h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.action-content p {
  margin: 0;
  font-size: 12px;
  color: #6c757d;
  line-height: 1.4;
}

/* 预览内容区域 */
.preview-content-area {
  min-height: 400px;
  padding: 20px;
}

.markdown-preview {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 14px;
  line-height: 1.6;
  color: #2c3e50;
  word-wrap: break-word;
}

.markdown-preview h1,
.markdown-preview h2,
.markdown-preview h3,
.markdown-preview h4,
.markdown-preview h5,
.markdown-preview h6 {
  margin: 1.5em 0 0.5em 0;
  font-weight: 600;
  color: #1a1a1a;
}

.markdown-preview h1 {
  font-size: 1.8em;
  border-bottom: 2px solid #e9ecef;
  padding-bottom: 0.3em;
}

.markdown-preview h2 {
  font-size: 1.5em;
  border-bottom: 1px solid #e9ecef;
  padding-bottom: 0.3em;
}

.markdown-preview h3 {
  font-size: 1.3em;
}

.markdown-preview p {
  margin: 1em 0;
}

.markdown-preview strong {
  font-weight: 600;
  color: #002FA7;
}

.markdown-preview em {
  font-style: italic;
  color: #517B4D;
}

.markdown-preview u {
  text-decoration: underline;
  text-decoration-color: #002FA7;
}

.markdown-preview code {
  background: rgba(0, 47, 167, 0.1);
  color: #002FA7;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.9em;
}

.markdown-preview pre {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  overflow-x: auto;
  margin: 1em 0;
}

.markdown-preview pre code {
  background: none;
  color: inherit;
  padding: 0;
}

.markdown-preview ul,
.markdown-preview ol {
  margin: 1em 0;
  padding-left: 2em;
}

.markdown-preview li {
  margin: 0.5em 0;
}

.markdown-preview blockquote {
  border-left: 4px solid #002FA7;
  background: rgba(0, 47, 167, 0.05);
  margin: 1em 0;
  padding: 1em 1.5em;
  border-radius: 0 8px 8px 0;
}

.markdown-preview a {
  color: #002FA7;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.2s ease;
}

.markdown-preview a:hover {
  border-bottom-color: #002FA7;
}

.markdown-preview img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin: 1em 0;
}

.markdown-preview table {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.markdown-preview th,
.markdown-preview td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e9ecef;
}

.markdown-preview th {
  background: linear-gradient(135deg, #002FA7, #517B4D);
  color: white;
  font-weight: 600;
}

.markdown-preview tr:hover {
  background: rgba(0, 47, 167, 0.05);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .modern-editor-body {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .quick-actions-panel {
    flex-direction: row;
    overflow-x: auto;
  }

  .quick-action-card {
    min-width: 200px;
  }
}

@media (max-width: 768px) {
  .modern-toolbar {
    flex-wrap: wrap;
    gap: 8px;
  }

  .toolbar-group {
    gap: 2px;
  }

  .modern-toolbar-btn {
    width: 32px;
    height: 32px;
  }

  .custom-textarea {
    padding-left: 50px;
  }

  .line-numbers {
    width: 35px;
  }

  .preview-content-area {
    padding: 16px;
  }

  .markdown-preview {
    font-size: 13px;
  }
}

/* 课程大纲 */
.add-chapter-btn {
  background: linear-gradient(135deg, #002FA7, #517B4D);
  color: white;
  border: none;
  border-radius: 10px;
  padding: 8px 16px;
  font-size: 14px;
}

.curriculum-builder {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chapter-item {
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  overflow: hidden;
}

.chapter-header {
  background: #f8f9fa;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chapter-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.drag-handle {
  color: #9ca3af;
  cursor: grab;
}

.chapter-number {
  font-weight: 600;
  color: #002FA7;
  min-width: 60px;
}

.chapter-title-input {
  flex: 1;
}

.chapter-actions {
  display: flex;
  gap: 8px;
}

.lessons-list {
  padding: 0;
}

.lesson-item {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.lesson-item:last-child {
  border-bottom: none;
}

.lesson-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.lesson-icon {
  color: #002FA7;
}

.lesson-title-input {
  flex: 1;
}

.lesson-duration {
  color: #6b7280;
  font-size: 14px;
  min-width: 60px;
  text-align: right;
}

.lesson-actions {
  display: flex;
  gap: 4px;
}

.delete-btn {
  color: #dc3545;
}

.delete-btn:hover {
  background: rgba(220, 53, 69, 0.1);
}

/* 预览区域 */
.preview-area {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preview-card,
.settings-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 20px 20px 40px rgba(0, 0, 0, 0.1),
  -20px -20px 40px rgba(255, 255, 255, 0.8);
  overflow: hidden;
}

.preview-header,
.settings-header {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.preview-header h3,
.settings-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-content,
.settings-content {
  padding: 20px;
}

.course-preview {
  text-align: center;
}

.preview-cover {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 16px;
}

.preview-cover img {
  width: 100%;
  height: auto;
  display: block;
}

.preview-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  background: rgba(0, 47, 167, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.preview-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.preview-subtitle {
  color: #6b7280;
  margin: 0 0 16px 0;
  line-height: 1.5;
}

.preview-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
  color: #6b7280;
}

.meta-item.price {
  color: #002FA7;
  font-weight: 600;
  font-size: 16px;
}

/* 设置区域 */
.setting-item {
  margin-bottom: 20px;
}

.setting-item:last-child {
  margin-bottom: 0;
}

.setting-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #374151;
}

.setting-select,
.setting-date {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content-wrapper {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .preview-area {
    order: -1;
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 20px 16px;
  }

  .page-header {
    padding: 16px 20px;
  }

  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .header-actions {
    justify-content: center;
  }

  .chapter-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .lesson-item {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }
}

/* 课程分类选择器样式优化 */
.form-select {
  width: 100%;
}

/* AI建议按钮样式 */
.input-with-ai-suggestion {
  display: flex;
  gap: 10px;
  align-items: center;
}

.input-with-ai-suggestion .form-input {
  flex: 1;
}

.ai-suggestion-btn {
  flex-shrink: 0;
  border-radius: 6px;
  font-size: 12px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  transition: all 0.3s ease;
}

.ai-suggestion-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

/* 选择器输入框样式 */
.form-select :deep(.el-input__wrapper) {
  border: 2px solid rgba(0, 47, 167, 0.2);
  border-radius: 12px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: none;
}

.form-select :deep(.el-input.is-focus .el-input__wrapper) {
  border-color: #002FA7;
  box-shadow: 0 0 0 3px rgba(0, 47, 167, 0.1);
}

.form-select :deep(.el-input:hover .el-input__wrapper) {
  border-color: rgba(0, 47, 167, 0.4);
}

/* 选择器下拉箭头样式 */
.form-select :deep(.el-select__caret) {
  color: #002FA7;
  transition: transform 0.3s ease;
}

.form-select :deep(.el-select.is-focused .el-select__caret) {
  transform: rotateZ(180deg);
  color: #517B4D;
}

</style>

<style>
/* 课程分类下拉框全局样式 - 不使用scoped以确保能覆盖Element Plus样式 */
.el-select-dropdown {
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(20px);
}

/* 一级分类组标题样式 */
.el-select-group__title {
  background: linear-gradient(135deg, #002FA7, #517B4D) !important;
  color: white !important;
  font-weight: 600 !important;
  font-size: 14px !important;
  padding: 8px 16px !important;
  margin: 0 !important;
  border-radius: 8px !important;
  margin-bottom: 4px !important;
  text-transform: uppercase !important;
  letter-spacing: 0.5px !important;
  position: relative !important;
  overflow: hidden !important;
}

.el-select-group__title:before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.el-select-group__title:hover:before {
  left: 100%;
}

/* 二级分类选项样式 */
.el-select-dropdown__item {
  background: rgba(255, 255, 255, 0.9) !important;
  color: #2c3e50 !important;
  padding: 10px 20px !important;
  margin: 2px 8px !important;
  border-radius: 6px !important;
  font-size: 13px !important;
  transition: all 0.3s ease !important;
  border-left: 3px solid transparent !important;
  position: relative !important;
}

/* 二级分类悬停效果 */
.el-select-dropdown__item:hover {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.1), rgba(81, 123, 77, 0.1)) !important;
  color: #002FA7 !important;
  border-left-color: #002FA7 !important;
  transform: translateX(4px) !important;
  box-shadow: 0 4px 12px rgba(0, 47, 167, 0.15) !important;
}

/* 选中状态样式 */
.el-select-dropdown__item.selected {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.2), rgba(81, 123, 77, 0.2)) !important;
  color: #002FA7 !important;
  font-weight: 600 !important;
  border-left-color: #517B4D !important;
}

/* 一级分类组容器样式 */
.el-select-group {
  margin-bottom: 12px !important;
  padding: 0 !important;
}

.el-select-group:last-child {
  margin-bottom: 0 !important;
}

/* 一级分类悬停时的弱色标注 */
.el-select-group:hover .el-select-group__title {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.8), rgba(81, 123, 77, 0.8)) !important;
  transform: scale(1.02) !important;
  box-shadow: 0 4px 16px rgba(0, 47, 167, 0.3) !important;
}

/* 一级分类悬停时二级分类的样式变化 */
.el-select-group:hover .el-select-dropdown__item {
  background: rgba(0, 47, 167, 0.05) !important;
  border-left-color: rgba(0, 47, 167, 0.3) !important;
}
</style>