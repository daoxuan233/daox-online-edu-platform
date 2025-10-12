<template>
  <div class="course-create-container">
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
            创建新课程
          </h1>
        </div>
        <div class="header-actions">
          <el-button class="action-btn save-btn" @click="saveDraft">
            <font-awesome-icon :icon="['fas', 'save']"/>
            保存草稿
          </el-button>
          <el-button type="primary" class="action-btn publish-btn" @click="createCourse">
            <font-awesome-icon :icon="['fas', 'rocket']"/>
            创建课程
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
              <el-form :model="courseForm" label-width="100px" ref="basicInfoForm" :rules="formRules">
                <el-form-item label="课程标题" prop="title">
                  <el-input
                      v-model="courseForm.title"
                      placeholder="请输入课程标题"
                      class="form-input"
                      maxlength="100"
                      show-word-limit
                  />
                </el-form-item>

                <el-form-item label="课程分类" prop="category">
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

                <el-form-item label="难度等级" prop="level">
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
                  <span class="price-tip">设置为0表示免费课程</span>
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
                <span class="char-count" :class="charCountStatus">{{ courseForm.description.length }}/2000</span>
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
                  <div class="preview-meta">
                    <span class="meta-item">
                      <font-awesome-icon :icon="['fas', 'tag']"/>
                      {{ getCategoryName(courseForm.category) || '分类' }}
                    </span>
                    <span class="meta-item">
                      <font-awesome-icon :icon="['fas', 'signal']"/>
                      {{ getLevelText(courseForm.level) || '难度' }}
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

          <!-- 创建设置 -->
          <div class="settings-card">
            <div class="settings-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'cog']"/>
                创建设置
              </h3>
            </div>
            <div class="settings-content">
              <div class="setting-item">
                <label class="setting-label">课程状态</label>
                <el-select v-model="courseForm.status" class="setting-select">
                  <el-option label="草稿" value="draft"/>
                  <el-option label="已发布" value="published"/>
                </el-select>
              </div>

              <div class="setting-item">
                <label class="setting-label">是否为免费课程</label>
                <el-switch v-model="courseForm.isFree" @change="handleFreeChange"/>
              </div>

              <div class="setting-item">
                <label class="setting-label">是否为公开课程</label>
                <el-switch v-model="courseForm.isPublic"/>
              </div>
            </div>
          </div>

          <!-- 创建提示 -->
          <div class="tips-card">
            <div class="tips-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'lightbulb']"/>
                创建提示
              </h3>
            </div>
            <div class="tips-content">
              <div class="tip-item">
                <font-awesome-icon :icon="['fas', 'check-circle']" class="tip-icon success"/>
                <span>填写完整的课程信息有助于提高课程质量</span>
              </div>
              <div class="tip-item">
                <font-awesome-icon :icon="['fas', 'info-circle']" class="tip-icon info"/>
                <span>建议上传高质量的课程封面图片</span>
              </div>
              <div class="tip-item">
                <font-awesome-icon :icon="['fas', 'exclamation-triangle']" class="tip-icon warning"/>
                <span>课程创建后可以继续编辑和完善内容</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted, nextTick} from 'vue'
import {useRouter} from 'vue-router'
import {updateCoverTeacher,updateCourseCover,createCourse as createCourseAPI} from '@/api/teacher/teacherAPI.js'
import {getCourseCategoryTree} from '@/api/publicApi.js'
import {getOptimizationAssistantResponse,getOptimizationAssistantCrowd,getOptimizationAssistantRequirements} from '@/api/teacher/TeacherAIChatAPI.js'
import {ElMessage, ElMessageBox} from 'element-plus'
import {getCurrentUserId} from "@/api/index.js";

const router = useRouter()

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
  coverUrl: '',
  targetAudience: '',
  requirements: '',
  status: 'draft',
  isFree: false,
  isPublic: true
})

// 表单验证规则
const formRules = {
  title: [
    {required: true, message: '请输入课程标题', trigger: 'blur'},
    {min: 5, max: 100, message: '课程标题长度应在 5 到 100 个字符之间', trigger: 'blur'}
  ],
  category: [
    {required: true, message: '请选择课程分类', trigger: 'change'}
  ],
  level: [
    {required: true, message: '请选择难度等级', trigger: 'change'}
  ]
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

// 编辑器状态
const editorMode = ref('edit')
const isPreviewMode = ref(false)
const descriptionTextarea = ref(null)

// 计算文本区域行数
const textareaLines = computed(() => {
  const lines = courseForm.value.description.split('\n').length
  return Math.max(lines, 10) // 最少显示10行
})

// 字符计数状态
const charCountStatus = computed(() => {
  const count = courseForm.value.description.length
  if (count > 2000) return 'danger'
  if (count > 1500) return 'warning'
  return 'normal'
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
    coverUrl.value = result.permanentUrl
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

// 获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId || !categoryTree.value.length) return ''

  for (const category of categoryTree.value) {
    if (category.children) {
      const subCategory = category.children.find(sub => sub.id === categoryId)
      if (subCategory) return subCategory.name
    }
  }
  return ''
}

// 获取难度等级文本
const getLevelText = (level) => {
  const levelMap = {
    beginner: '初级',
    intermediate: '中级',
    advanced: '高级'
  }
  return levelMap[level] || ''
}

// 处理免费课程切换
const handleFreeChange = (value) => {
  if (value) {
    courseForm.value.price = 0
  }
}

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

// 切换编辑/预览模式
const togglePreview = () => {
  isPreviewMode.value = !isPreviewMode.value
  editorMode.value = isPreviewMode.value ? 'preview' : 'edit'
}

// 简单的Markdown渲染
const renderMarkdown = (text) => {
  if (!text) return '暂无内容...'

  return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/`(.*?)`/g, '<code>$1</code>')
      .replace(/\n/g, '<br>')
}

// 保存草稿
const saveDraft = async () => {
  try {
    // 这里可以调用保存草稿的API
    ElMessage.success('草稿保存成功')
  } catch (error) {
    ElMessage.error('保存草稿失败：' + error.message)
  }
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

// 创建课程
const createCourse = async () => {
  try {
    // 验证必填字段
    if (!courseForm.value.title.trim()) {
      ElMessage.error('请输入课程标题')
      return
    }

    if (!courseForm.value.category) {
      ElMessage.error('请选择课程分类')
      return
    }

    if (!courseForm.value.level) {
      ElMessage.error('请选择难度等级')
      return
    }

    // 确认创建
    await ElMessageBox.confirm(
        '确定要创建这门课程吗？创建后可以继续编辑和完善内容。',
        '确认创建',
        {
          confirmButtonText: '确定创建',
          cancelButtonText: '取消',
          type: 'info'
        }
    )

    // 构建符合CourseCoreInfoDto结构的数据
    const courseCoreInfo = {
      title: courseForm.value.title,
      description: courseForm.value.description,
      coverImageUrl: courseForm.value.coverUrl,
      teacherId: getCurrentUserId(),
      categoryId: courseForm.value.category,
      status: courseForm.value.status,
      privateCourse: !courseForm.value.isPublic,
      properties: {
        level: courseForm.value.level,
        targetAudience: courseForm.value.targetAudience,
        requirements: courseForm.value.requirements,
        price: courseForm.value.isFree ? 0 : courseForm.value.price,
        originalPrice: courseForm.value.price
      }
    }

    // 调用创建课程的API
    const result = await createCourseAPI(courseCoreInfo)

    ElMessage.success('课程创建成功！')

    // 跳转到课程编辑页面或课程列表
    router.push({name: 'TeacherCourses'})

  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('创建课程失败：' + error.message)
    }
  }
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

// 组件挂载时加载数据
onMounted(() => {
  loadCategoryTree() // 只加载课程分类，不加载课程详情
})
</script>

<style scoped>
/* 复用CourseEdit.vue的样式，但做一些调整 */
.course-create-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f0f3 0%, #e8e8eb 100%);
  padding: 0;
}

/* 页面头部 */
.page-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  padding: 20px 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  color: #666;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(0, 47, 167, 0.1);
  color: #002FA7;
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
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
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
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
}

.save-btn {
  background: rgba(103, 194, 58, 0.1);
  color: #67C23A;
  border: 1px solid rgba(103, 194, 58, 0.3);
}

.save-btn:hover {
  background: rgba(103, 194, 58, 0.2);
  transform: translateY(-1px);
}

.publish-btn {
  background: linear-gradient(135deg, #002FA7, #517B4D);
  border: none;
  color: white;
}

.publish-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 47, 167, 0.3);
}

/* 主要内容区域 */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 20px;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 30px;
}

.edit-area {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.preview-area {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 卡片样式 */
.section-card,
.preview-card,
.settings-card,
.tips-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.3s ease;
}

.section-card:hover,
.preview-card:hover,
.settings-card:hover,
.tips-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.section-header,
.preview-header,
.settings-header,
.tips-header {
  padding: 20px 25px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3,
.preview-header h3,
.settings-header h3,
.tips-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-content,
.preview-content,
.settings-content,
.tips-content {
  padding: 24px;
}

/* 表单样式 */
.form-input,
.form-select,
.form-number {
  width: 100%;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
}

.form-input:focus,
.form-select:focus,
.form-number:focus {
  border-color: #002FA7;
  box-shadow: 0 0 0 2px rgba(0, 47, 167, 0.1);
}

.price-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

/* 上传区域样式 */
.upload-area {
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(249, 250, 251, 0.5);
}

.upload-area:hover {
  border-color: #002FA7;
  background: rgba(0, 47, 167, 0.05);
}

.upload-area.uploading {
  border-color: #67C23A;
  background: rgba(103, 194, 58, 0.05);
}

.upload-placeholder {
  color: #909399;
}

.upload-placeholder i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.upload-placeholder p {
  margin: 8px 0;
}

.upload-tip {
  font-size: 12px;
  color: #c0c4cc;
}

.upload-loading {
  color: #67C23A;
}

.upload-loading i {
  font-size: 32px;
  margin-bottom: 12px;
}

.upload-preview {
  position: relative;
}

.cover-image {
  width: 100%;
  max-width: 300px;
  height: auto;
  border-radius: 8px;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 300px;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 8px;
}

.upload-preview:hover .upload-overlay {
  opacity: 1;
}

/* 编辑器样式 */
.modern-editor-container {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 16px;
  padding: 20px;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.06);
}

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

.toolbar-divider {
  width: 1px;
  height: 20px;
  background: #e4e7ed;
  margin: 0 8px;
}

.toolbar-spacer {
  flex: 1;
}

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
  background: #fafafa;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
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
  min-height: 300px;
}

.custom-textarea-container {
  position: relative;
}

.custom-textarea {
  width: 100%;
  min-height: 300px;
  border: none;
  outline: none;
  padding: 20px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
  background: transparent;
}

.textarea-overlay {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
}

.line-numbers {
  padding: 20px 0 20px 10px;
  color: #c0c4cc;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.6;
  user-select: none;
}

.line-number {
  display: block;
  height: 22.4px;
}

.preview-content-area {
  padding: 20px;
  min-height: 300px;
}

.markdown-preview {
  line-height: 1.6;
  color: #2c3e50;
}

.description-stats {
  display: flex;
  align-items: center;
  gap: 10px;
}

.char-count {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  background: #f0f2f5;
  color: #606266;
}

.char-count.warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.char-count.danger {
  background: #fef0f0;
  color: #f56c6c;
}

/* 预览区域样式 */
.course-preview {
  text-align: center;
}

.preview-cover {
  position: relative;
  margin-bottom: 16px;
}

.preview-cover img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 8px;
}

.preview-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  background: rgba(0, 0, 0, 0.7);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.preview-info {
  text-align: left;
}

.preview-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.preview-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
}

.meta-item.price {
  color: #f56c6c;
  font-weight: 600;
}

/* 设置区域样式 */
.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.setting-item:last-child {
  margin-bottom: 0;
}

.setting-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.setting-select {
  width: 120px;
}

/* 提示区域样式 */
.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 12px;
  font-size: 13px;
  line-height: 1.5;
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

.tip-item:last-child {
  margin-bottom: 0;
}

.tip-icon {
  margin-top: 2px;
  flex-shrink: 0;
}

.tip-icon.success {
  color: #67c23a;
}

.tip-icon.info {
  color: #409eff;
}

.tip-icon.warning {
  color: #e6a23c;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content-wrapper {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .quick-actions-panel {
    flex-direction: row;
    overflow-x: auto;
  }

  .preview-area {
    order: -1;
  }

  .quick-action-card {
    min-width: 200px;
  }

  .modern-editor-body {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .quick-actions-panel {
    flex-direction: row;
    overflow-x: auto;
  }

  .header-left {
    justify-content: center;
  }

  .header-actions {
    justify-content: center;
  }

  .main-content {
    padding: 20px 15px;
  }

  .section-content,
  .preview-content,
  .settings-content,
  .tips-content {
    padding: 20px;
  }
}

</style>