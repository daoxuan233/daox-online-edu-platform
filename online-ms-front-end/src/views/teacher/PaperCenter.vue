<template>
  <div class="paper-center-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <el-button
              link
              @click="$router.go(-1)"
              class="back-btn"
          >
            <font-awesome-icon :icon="['fas', 'arrow-left']"/>
            返回测评管理
          </el-button>
          <h1 class="page-title">
            <font-awesome-icon :icon="['fas', 'file-alt']"/>
            试卷编辑中心
          </h1>
          <p class="page-subtitle">{{ paperData.title || '未命名试卷' }}</p>
        </div>
        <div class="header-actions">
          <el-button class="action-btn" @click="previewPaper">
            <font-awesome-icon :icon="['fas', 'eye']"/>
            预览试卷
          </el-button>
          <el-button class="action-btn primary" @click="savePaper">
            <font-awesome-icon :icon="['fas', 'save']"/>
            保存试卷
          </el-button>
        </div>
      </div>
    </div>

    <!-- 试卷信息卡片 -->
    <div class="paper-info-section">
      <div class="info-card neumorphism-raised">
        <div class="info-header">
          <h3>
            <font-awesome-icon :icon="['fas', 'info-circle']"/>
            试卷信息
          </h3>
        </div>
        <div class="info-content">
          <div class="info-row">
            <div class="info-item">
              <label class="info-label">试卷标题</label>
              <el-input 
                v-model="paperData.title" 
                placeholder="请输入试卷标题"
                class="custom-input"
                @blur="updatePaperInfo"
              />
            </div>
            <div class="info-item">
              <label class="info-label">试卷描述</label>
              <el-input 
                v-model="paperData.description" 
                type="textarea"
                :rows="2"
                placeholder="请输入试卷描述"
                class="custom-textarea"
                @blur="updatePaperInfo"
              />
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <label class="info-label">总分</label>
              <div class="score-display">
                <span class="score-value">{{ totalScore }}</span>
                <span class="score-unit">分</span>
              </div>
            </div>
            <div class="info-item">
              <label class="info-label">题目数量</label>
              <div class="count-display">
                <span class="count-value">{{ totalQuestions }}</span>
                <span class="count-unit">题</span>
              </div>
            </div>
            <div class="info-item">
              <label class="info-label">分组数量</label>
              <div class="section-display">
                <span class="section-value">{{ paperData.sections.length }}</span>
                <span class="section-unit">组</span>
              </div>
            </div>
          </div>
          
          <!-- 测评详细信息（如果有完整数据） -->
          <div v-if="paperData.assessmentInfo" class="info-row assessment-details">
            <div class="info-item">
              <label class="info-label">测评类型</label>
              <div class="type-display">
                <span class="type-value">{{ getAssessmentTypeName(paperData.assessmentInfo.assessmentType) }}</span>
              </div>
            </div>
            <div class="info-item">
              <label class="info-label">答题时长</label>
              <div class="duration-display">
                <span class="duration-value">{{ paperData.assessmentInfo.durationMinutes }}</span>
                <span class="duration-unit">分钟</span>
              </div>
            </div>
            <div class="info-item">
              <label class="info-label">发布状态</label>
              <div class="status-display">
                <span :class="['status-badge', paperData.assessmentInfo.isPublished ? 'published' : 'draft']">
                  {{ paperData.assessmentInfo.isPublished ? '已发布' : '草稿' }}
                </span>
              </div>
            </div>
          </div>
          
          <div v-if="paperData.assessmentInfo && paperData.assessmentInfo.startTime" class="info-row time-details">
            <div class="info-item">
              <label class="info-label">开始时间</label>
              <div class="time-display">
                <span class="time-value">{{ formatDateTime(paperData.assessmentInfo.startTime) }}</span>
              </div>
            </div>
            <div class="info-item">
              <label class="info-label">结束时间</label>
              <div class="time-display">
                <span class="time-value">{{ formatDateTime(paperData.assessmentInfo.endTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="content-wrapper">
        <!-- 左侧编辑区域 -->
        <div class="edit-area">
          <!-- 试卷结构编辑器 -->
          <div class="section-card">
            <div class="section-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'sitemap']"/>
                试卷结构
              </h3>
              <div class="header-actions">
                <el-button size="small" class="action-btn primary" @click="addSection">
                  <font-awesome-icon :icon="['fas', 'plus']"/>
                  添加分组
                </el-button>
              </div>
            </div>
            <div class="section-content">
              <!-- 加载状态 -->
              <div v-if="loading" class="loading-container">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>正在加载试卷结构...</span>
              </div>
              
              <!-- 无数据提示 -->
              <div v-else-if="paperData.sections.length === 0" class="empty-state">
                <el-empty description="暂无试卷内容">
                  <template #description>
                    <p>您还没有创建试卷分组</p>
                    <p>点击"添加分组"开始构建您的试卷</p>
                  </template>
                </el-empty>
              </div>
              
              <!-- 试卷编辑器 -->
              <div v-else class="paper-editor">
                <!-- 分组列表 -->
                <draggable
                    v-model="paperData.sections"
                    group="sections"
                    item-key="id"
                    class="sections-list"
                    :animation="200"
                    ghost-class="ghost-section"
                    chosen-class="chosen-section"
                    drag-class="drag-section"
                    @start="onDragStart"
                    @end="onDragEnd"
                >
                  <template #item="{ element: section, index: sectionIndex }">
                    <div class="section-item neumorphism-raised" :key="section.id">
                      <!-- 分组头部 -->
                      <div class="section-header">
                        <div class="section-info">
                          <div class="drag-handle">
                            <font-awesome-icon :icon="['fas', 'grip-vertical']"/>
                          </div>
                          <div class="section-number">第{{ sectionIndex + 1 }}部分</div>
                          <div class="section-title-container">
                            <el-input
                                v-model="section.title"
                                placeholder="请输入分组标题"
                                class="section-title-input"
                                @blur="updateSectionTitle(section.id, section.title)"
                            />
                          </div>
                        </div>
                        <div class="section-actions">
                          <el-button size="small" class="action-btn" @click="addQuestion(section.id)" style="margin-left: 15px">
                            <font-awesome-icon :icon="['fas', 'plus']"/>
                            添加题目
                          </el-button>
                          <el-button size="small" class="action-btn" @click="toggleSection(section.id)">
                            <font-awesome-icon :icon="section.expanded ? ['fas', 'chevron-up'] : ['fas', 'chevron-down']"/>
                          </el-button>
                          <el-button size="small" class="action-btn danger" @click="deleteSection(section.id)">
                            <font-awesome-icon :icon="['fas', 'trash']"/>
                          </el-button>
                        </div>
                      </div>

                      <!-- 分组描述 -->
                      <div v-show="section.expanded" class="section-description">
                        <el-input
                            v-model="section.description"
                            type="textarea"
                            :rows="2"
                            placeholder="请输入分组描述（可选）"
                            class="section-description-input"
                            @blur="updateSectionDescription(section.id, section.description)"
                        />
                      </div>

                      <!-- 题目列表 -->
                      <div v-show="section.expanded" class="questions-container">
                        <draggable
                            v-model="section.questions"
                            group="questions"
                            item-key="id"
                            class="questions-list"
                            :animation="200"
                            ghost-class="ghost-question"
                            chosen-class="chosen-question"
                            drag-class="drag-question"
                            @start="onQuestionDragStart"
                            @end="onQuestionDragEnd"
                        >
                          <template #item="{ element: question, index: questionIndex }">
                            <div class="question-item neumorphism-inset" :key="question.id">
                              <div class="question-info">
                                <div class="drag-handle">
                                  <font-awesome-icon :icon="['fas', 'grip-vertical']"/>
                                </div>
                                <div class="question-icon">
                                  <font-awesome-icon :icon="getQuestionIcon(question.type)"/>
                                </div>
                                <div class="question-number">{{ sectionIndex + 1 }}.{{ questionIndex + 1 }}</div>
                                <div class="question-content">
                                  <div class="question-title">{{ question.title || '未选择题目' }}</div>
                                  <div class="question-meta">
                                    <span class="question-type">{{ getQuestionTypeName(question.type) }}</span>
                                    <span class="question-difficulty">{{ getDifficultyName(question.difficulty) }}</span>
                                  </div>
                                </div>
                              </div>
                              <div class="question-score">
                                <el-input-number
                                    v-model="question.score"
                                    :min="0.5"
                                    :max="100"
                                    :step="0.5"
                                    :precision="1"
                                    size="small"
                                    class="score-input"
                                    @change="updateQuestionScore(question.id, question.score)"
                                />
                                <span class="score-unit">分</span>
                              </div>
                              <div class="question-actions">
                                <el-button size="small" class="action-btn" @click="editQuestion(question)">
                                  <font-awesome-icon :icon="['fas', 'edit']"/>
                                </el-button>
                                <el-button size="small" class="action-btn danger" @click="deleteQuestion(section.id, question.id)">
                                  <font-awesome-icon :icon="['fas', 'trash']"/>
                                </el-button>
                              </div>
                            </div>
                          </template>
                        </draggable>
                        
                        <!-- 空状态 -->
                        <div v-if="section.questions.length === 0" class="empty-questions">
                          <div class="empty-icon">
                            <font-awesome-icon :icon="['fas', 'plus-circle']"/>
                          </div>
                          <p>暂无题目，点击"添加题目"开始创建</p>
                        </div>
                      </div>
                    </div>
                  </template>
                </draggable>
                
                <!-- 空状态 -->
                <div v-if="paperData.sections.length === 0" class="empty-sections">
                  <div class="empty-icon">
                    <font-awesome-icon :icon="['fas', 'file-alt']"/>
                  </div>
                  <h3>开始创建试卷结构</h3>
                  <p>添加分组和题目，构建完整的试卷内容</p>
                  <el-button class="action-btn primary" @click="addSection">
                    <font-awesome-icon :icon="['fas', 'plus']"/>
                    添加第一个分组
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧预览区域 -->
        <div class="preview-area">
          <!-- 试卷预览 -->
          <div class="preview-card">
            <div class="preview-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'eye']"/>
                试卷预览
              </h3>
            </div>
            <div class="preview-content">
              <div class="paper-preview">
                <div class="preview-title">{{ paperData.title || '未命名试卷' }}</div>
                <div class="preview-description">{{ paperData.description || '暂无描述' }}</div>
                
                <div v-for="(section, sectionIndex) in paperData.sections" :key="section.id" class="preview-section">
                  <div class="preview-section-title">
                    <span class="section-number">第{{ sectionIndex + 1 }}部分</span>
                    <span class="section-title">{{ section.title || '未命名分组' }}</span>
                  </div>
                  <div v-if="section.description" class="preview-section-description">
                    {{ section.description }}
                  </div>
                  <div class="preview-questions">
                    <div v-for="(question, questionIndex) in section.questions" :key="question.id" class="preview-question">
                      <span class="question-number">{{ sectionIndex + 1 }}.{{ questionIndex + 1 }}</span>
                      <font-awesome-icon :icon="getQuestionIcon(question.type)" class="question-icon"/>
                      <span class="question-title">{{ question.title || '未选择题目' }}</span>
                      <span class="question-score">{{ question.score }}分</span>
                    </div>
                  </div>
                </div>
                
                <div v-if="paperData.sections.length === 0" class="preview-empty">
                  <p>暂无内容</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 统计信息 -->
          <div class="stats-card">
            <div class="stats-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'chart-bar']"/>
                试卷统计
              </h3>
            </div>
            <div class="stats-content">
              <div class="stat-item">
                <div class="stat-icon">
                  <font-awesome-icon :icon="['fas', 'layer-group']"/>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ paperData.sections.length }}</div>
                  <div class="stat-label">分组数</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <font-awesome-icon :icon="['fas', 'question-circle']"/>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ totalQuestions }}</div>
                  <div class="stat-label">题目数</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <font-awesome-icon :icon="['fas', 'star']"/>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ totalScore }}</div>
                  <div class="stat-label">总分</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 题目类型分布 -->
          <div class="distribution-card">
            <div class="distribution-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'pie-chart']"/>
                题型分布
              </h3>
            </div>
            <div class="distribution-content">
              <div v-for="(item, index) in questionTypeDistribution" :key="index" class="distribution-item">
                <div class="type-info">
                  <font-awesome-icon :icon="getQuestionIcon(item.type)" class="type-icon"/>
                  <span class="type-name">{{ getQuestionTypeName(item.type) }}</span>
                </div>
                <div class="type-stats">
                  <span class="type-count">{{ item.count }}题</span>
                  <span class="type-score">{{ item.totalScore }}分</span>
                </div>
              </div>
              
              <div v-if="questionTypeDistribution.length === 0" class="distribution-empty">
                <p>暂无题目</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 题目选择对话框 -->
    <el-dialog 
      v-model="showQuestionDialog" 
      title="选择题目" 
      width="1000px"
      class="question-dialog"
    >
      <div class="question-selector">
        <!-- 题目筛选 -->
        <div class="filter-section">
          <div class="filter-row">
            <el-input 
              v-model="questionFilter.tagSearch" 
              placeholder="搜索标签..."
              class="filter-input tag-search"
            >
              <template #prefix>
                <font-awesome-icon :icon="['fas', 'tag']"/>
              </template>
            </el-input>
            
            <el-select v-model="questionFilter.type" placeholder="题目类型" class="filter-select">
              <el-option label="全部类型" value="" />
              <el-option label="单选题" value="single_choice" />
              <el-option label="多选题" value="multiple_choice" />
              <el-option label="判断题" value="true_false" />
              <el-option label="填空题" value="fill_blank" />
              <el-option label="简答题" value="short_answer" />
              <el-option label="程序题" value="programming" />
            </el-select>
            
            <el-select v-model="questionFilter.difficulty" placeholder="难度等级" class="filter-select">
              <el-option label="全部难度" value="" />
              <el-option label="简单" value="easy" />
              <el-option label="中等" value="medium" />
              <el-option label="困难" value="hard" />
            </el-select>
            
            <el-input 
              v-model="questionFilter.keyword" 
              placeholder="搜索题目内容..."
              class="filter-input"
            >
              <template #prefix>
                <font-awesome-icon :icon="['fas', 'search']"/>
              </template>
            </el-input>
            
            <el-button 
              type="primary" 
              class="public-bank-btn"
              @click="loadPublicQuestions"
              :loading="loadingPublicQuestions"
            >
              <font-awesome-icon :icon="['fas', 'database']"/>
              公共题库
            </el-button>
          </div>
        </div>
        
        <!-- 题目列表 -->
        <div class="question-list">
          <div v-for="question in filteredQuestions" :key="question.id" class="question-option">
            <el-checkbox 
              v-model="question.selected"
              class="question-checkbox"
              @change="handleQuestionSelect(question)"
            />
            <div class="question-content">
              <div class="question-header">
                <font-awesome-icon :icon="getQuestionIcon(question.type)" class="question-type-icon"/>
                <span class="question-type-text">{{ getQuestionTypeName(question.type) }}</span>
                <span class="question-difficulty-badge" :class="question.difficulty">
                  {{ getDifficultyName(question.difficulty) }}
                </span>
              </div>
              <div class="question-title">{{ question.title }}</div>
              <div class="question-preview">{{ question.content }}</div>
            </div>
          </div>
          
          <div v-if="filteredQuestions.length === 0" class="no-questions">
            <el-empty description="暂无符合条件的题目" />
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelQuestionSelect" class="action-btn">
            <font-awesome-icon :icon="['fas', 'times']"/>
            取消
          </el-button>
          <el-button @click="confirmQuestionSelect" class="action-btn primary">
            <font-awesome-icon :icon="['fas', 'check']"/>
            确定添加 ({{ selectedQuestions.length }})
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import { getAssessmentDetails, getCourseQuestions, getQuestionsList, createPaper } from '@/api/teacher/teacherAPI.js'
import { getCurrentUserId } from '@/api/index.js'

const route = useRoute()
const router = useRouter()

/**
 * 数据接收和验证逻辑
 * 支持多种数据来源：路由查询参数、路由状态、API获取
 */

// 路由参数验证和获取
const assessmentId = ref('')
const courseId = ref('')
const assessmentTitle = ref('')

// 从路由状态获取完整数据
const routeState = history.state || {}
const completeAssessmentData = ref(null)
const isFromCreate = ref(false)
const sourceRoute = ref('')

// 数据接收状态
const dataReceiveStatus = ref({
  hasQueryParams: false,
  hasStateData: false,
  isDataValid: false,
  errors: []
})

/**
 * 初始化路由数据接收
 * @returns {void}
 */
const initializeRouteData = () => {
  try {
    console.log('开始初始化路由数据...')
    console.log('Route query:', route.query)
    console.log('Route state:', routeState)
    
    // 重置状态
    dataReceiveStatus.value = {
      hasQueryParams: false,
      hasStateData: false,
      isDataValid: false,
      errors: []
    }

    // 1. 验证和获取查询参数
    if (route.query.assessmentId) {
      assessmentId.value = route.query.assessmentId
      courseId.value = route.query.courseId || ''
      assessmentTitle.value = route.query.title || ''
      dataReceiveStatus.value.hasQueryParams = true
      console.log('成功获取查询参数:', {
        assessmentId: assessmentId.value,
        courseId: courseId.value,
        title: assessmentTitle.value
      })
    } else {
      dataReceiveStatus.value.errors.push('缺少必要的查询参数 assessmentId')
      console.warn('缺少查询参数 assessmentId')
    }

    // 2. 验证和获取状态数据
    if (routeState.assessmentData) {
      // 验证状态数据的完整性
      const stateData = routeState.assessmentData
      if (stateData.id && stateData.title) {
        completeAssessmentData.value = stateData
        isFromCreate.value = routeState.isFromCreate || false
        sourceRoute.value = routeState.sourceRoute || 'unknown'
        dataReceiveStatus.value.hasStateData = true
        
        console.log('成功获取状态数据:', {
          id: stateData.id,
          title: stateData.title,
          courseId: stateData.courseId,
          isFromCreate: isFromCreate.value,
          sourceRoute: sourceRoute.value,
          transferTime: stateData._transferTime
        })
        
        // 如果状态数据存在但查询参数缺失，使用状态数据补充
        if (!assessmentId.value && stateData.id) {
          assessmentId.value = stateData.id
          console.log('使用状态数据补充 assessmentId:', assessmentId.value)
        }
        if (!courseId.value && stateData.courseId) {
          courseId.value = stateData.courseId
          console.log('使用状态数据补充 courseId:', courseId.value)
        }
        if (!assessmentTitle.value && stateData.title) {
          assessmentTitle.value = stateData.title
          console.log('使用状态数据补充 title:', assessmentTitle.value)
        }
      } else {
        dataReceiveStatus.value.errors.push('状态数据不完整，缺少必要字段')
        console.warn('状态数据不完整:', stateData)
      }
    } else {
      console.log('未获取到状态数据，将依赖查询参数和API获取')
    }

    // 3. 最终验证
    if (assessmentId.value) {
      dataReceiveStatus.value.isDataValid = true
      console.log('数据接收验证通过')
    } else {
      dataReceiveStatus.value.errors.push('无法获取有效的测评ID')
      console.error('数据接收验证失败：无法获取有效的测评ID')
    }

    // 4. 显示数据接收状态
    if (dataReceiveStatus.value.errors.length > 0) {
      console.warn('数据接收存在问题:', dataReceiveStatus.value.errors)
      ElMessage.warning(`数据接收存在问题: ${dataReceiveStatus.value.errors.join(', ')}`)
    } else {
      console.log('数据接收完成，状态:', dataReceiveStatus.value)
    }

  } catch (error) {
    console.error('初始化路由数据失败:', error)
    dataReceiveStatus.value.errors.push(`初始化失败: ${error.message}`)
    ElMessage.error('页面数据初始化失败，请重试')
  }
}

// 立即执行数据初始化
initializeRouteData()

// 试卷数据 - 基于Paper.java数据结构
const paperData = ref({
  id: '',
  courseId: '',
  creatorId: '',
  title: '',
  description: '',
  assessmentId: '',
  totalScore: 0,
  createdAt: null,
  updatedAt: null,
  sections: []
})

// 加载状态
const loading = ref(false)
const hasUnsavedChanges = ref(false)

// 题目选择对话框
const showQuestionDialog = ref(false)
const currentSectionId = ref('')
const loadingPublicQuestions = ref(false)
const questionFilter = ref({
  type: '',
  difficulty: '',
  keyword: '',
  tagSearch: ''
})

// 模拟题目数据（实际应从API获取）
const availableQuestions = ref([
  {
    id: 'q1',
    title: 'Vue.js的核心特性是什么？',
    content: 'Vue.js是一个渐进式JavaScript框架，具有以下核心特性...',
    type: 'single_choice',
    difficulty: 'easy',
    selected: false
  },
  {
    id: 'q2',
    title: '请解释JavaScript中的闭包概念',
    content: '闭包是指有权访问另一个函数作用域中变量的函数...',
    type: 'short_answer',
    difficulty: 'medium',
    selected: false
  },
  {
    id: 'q3',
    title: 'HTML5相比HTML4有哪些新特性？',
    content: 'HTML5引入了许多新的元素和API...',
    type: 'multiple_choice',
    difficulty: 'medium',
    selected: false
  }
])

// 计算属性
const totalQuestions = computed(() => {
  return paperData.value.sections.reduce((total, section) => total + section.questions.length, 0)
})

const totalScore = computed(() => {
  return paperData.value.sections.reduce((total, section) => {
    return total + section.questions.reduce((sectionTotal, question) => sectionTotal + (question.score || 0), 0)
  }, 0)
})

const questionTypeDistribution = computed(() => {
  const distribution = {}
  paperData.value.sections.forEach(section => {
    section.questions.forEach(question => {
      if (!distribution[question.type]) {
        distribution[question.type] = {
          type: question.type,
          count: 0,
          totalScore: 0
        }
      }
      distribution[question.type].count++
      distribution[question.type].totalScore += question.score || 0
    })
  })
  return Object.values(distribution)
})

const filteredQuestions = computed(() => {
  return availableQuestions.value.filter(question => {
    const matchType = !questionFilter.value.type || question.type === questionFilter.value.type
    const matchDifficulty = !questionFilter.value.difficulty || question.difficulty === questionFilter.value.difficulty
    const matchKeyword = !questionFilter.value.keyword || 
      question.title.toLowerCase().includes(questionFilter.value.keyword.toLowerCase()) ||
      question.content.toLowerCase().includes(questionFilter.value.keyword.toLowerCase())
    
    // 标签搜索功能
    const matchTag = !questionFilter.value.tagSearch || 
      (question.tags && question.tags.some(tag => 
        tag.toLowerCase().includes(questionFilter.value.tagSearch.toLowerCase())
      ))
    
    return matchType && matchDifficulty && matchKeyword && matchTag
  })
})

const selectedQuestions = computed(() => {
  return availableQuestions.value.filter(question => question.selected)
})

// 生成唯一ID
const generateId = () => {
  return 'id_' + Math.random().toString(36).substr(2, 9) + '_' + Date.now()
}

/**
 * 初始化试卷数据
 * 根据数据接收状态选择最佳的初始化策略
 * @returns {Promise<void>}
 */
const initializePaper = async () => {
  loading.value = true
  
  try {
    console.log('开始初始化试卷数据...')
    console.log('数据接收状态:', dataReceiveStatus.value)
    
    // 检查数据有效性
    if (!dataReceiveStatus.value.isDataValid) {
      throw new Error('数据接收验证失败，无法初始化试卷')
    }

    let initializationMethod = 'unknown'
    let assessmentDetails = null

    // 策略1：优先使用完整的状态数据
    if (dataReceiveStatus.value.hasStateData && completeAssessmentData.value) {
      initializationMethod = 'state-data'
      const assessment = completeAssessmentData.value
      
      console.log('使用状态数据初始化试卷:', {
        id: assessment.id,
        title: assessment.title,
        courseId: assessment.courseId,
        transferTime: assessment._transferTime
      })
      
      paperData.value = {
        id: generateId(), // 试卷ID，新生成
        courseId: assessment.courseId || courseId.value || '',
        creatorId: await getCurrentUserId() || '', // 获取当前用户ID
        title: assessment.title || '未命名试卷',
        description: assessment.description || '', // 使用测评描述作为试卷描述
        assessmentId: assessment.id || assessmentId.value || '',
        totalScore: 0, // 初始为0，随题目添加而计算
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        sections: [],
        // 保存完整的测评信息以备后用
        assessmentInfo: {
          assessmentType: assessment.assessmentType || 'exam',
          startTime: assessment.startTime,
          endTime: assessment.endTime,
          durationMinutes: assessment.durationMinutes || 60,
          isPublished: assessment.isPublished || false,
          courseInfo: assessment.courseInfo,
          sourceRoute: sourceRoute.value,
          isFromCreate: isFromCreate.value
        }
      }
      
      ElMessage.success('使用传递的测评数据初始化成功')
      
    } 
    // 策略2：通过API获取测评详情
    else if (assessmentId.value) {
      initializationMethod = 'api-fetch'
      
      console.log('通过API获取测评详情，assessmentId:', assessmentId.value)
      
      try {
        assessmentDetails = await getAssessmentDetails(assessmentId.value)
        console.log('API获取到的测评详情:', assessmentDetails)
        
        // 使用API返回的数据初始化试卷
        paperData.value = {
          id: generateId(), // 试卷ID，新生成
          courseId: assessmentDetails.courseId || courseId.value || '',
          creatorId: await getCurrentUserId() || '', // 获取当前用户ID
          title: assessmentDetails.title || assessmentTitle.value || '未命名试卷',
          description: assessmentDetails.description || '', // 使用测评描述作为试卷描述
          assessmentId: assessmentDetails.id || assessmentId.value || '',
          totalScore: 0, // 初始为0，随题目添加而计算
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          sections: [],
          // 保存完整的测评信息以备后用
          assessmentInfo: {
            assessmentType: assessmentDetails.assessmentType || 'exam',
            startTime: assessmentDetails.startTime,
            endTime: assessmentDetails.endTime,
            durationMinutes: assessmentDetails.durationMinutes || 60,
            isPublished: assessmentDetails.isPublished || false,
            courseInfo: assessmentDetails.courseInfo
          }
        }
        
        ElMessage.success('测评详情加载成功')
        
      } catch (apiError) {
        console.warn('API获取测评详情失败，使用降级方案:', apiError)
        initializationMethod = 'fallback-query'
        
        // API失败时的降级处理
        paperData.value = {
          id: generateId(),
          courseId: courseId.value || '',
          creatorId: await getCurrentUserId() || '',
          title: assessmentTitle.value || '未命名试卷',
          description: '',
          assessmentId: assessmentId.value || '',
          totalScore: 0,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          sections: [],
          assessmentInfo: {
            assessmentType: 'exam',
            durationMinutes: 60,
            isPublished: false
          }
        }
        
        ElMessage.warning('无法获取完整测评信息，使用基础模式初始化')
      }
    } 
    // 策略3：最后的降级处理
    else {
      initializationMethod = 'fallback-minimal'
      
      console.log('使用最小化数据初始化试卷（最后降级方案）')
      
      paperData.value = {
        id: generateId(),
        courseId: courseId.value || '',
        creatorId: await getCurrentUserId() || '',
        title: assessmentTitle.value || '未命名试卷',
        description: '',
        assessmentId: assessmentId.value || '',
        totalScore: 0,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        sections: [],
        assessmentInfo: {
          assessmentType: 'exam',
          durationMinutes: 60,
          isPublished: false
        }
      }
      
      ElMessage.info('使用基础模式初始化试卷')
    }

    console.log('试卷初始化完成:', {
      method: initializationMethod,
      paperId: paperData.value.id,
      title: paperData.value.title,
      assessmentId: paperData.value.assessmentId,
      courseId: paperData.value.courseId
    })

  } catch (error) {
    console.error('初始化试卷数据失败:', error)
    ElMessage.error(`初始化失败: ${error.message}`)
    
    // 最终的错误降级处理
    try {
      paperData.value = {
        id: generateId(),
        courseId: courseId.value || '',
        creatorId: await getCurrentUserId() || '',
        title: assessmentTitle.value || '未命名试卷',
        description: '',
        assessmentId: assessmentId.value || '',
        totalScore: 0,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        sections: [],
        assessmentInfo: {
          assessmentType: 'exam',
          durationMinutes: 60,
          isPublished: false
        }
      }
      console.log('使用错误降级方案初始化试卷')
    } catch (fallbackError) {
      console.error('降级初始化也失败:', fallbackError)
      ElMessage.error('试卷初始化完全失败，请刷新页面重试')
    }
  } finally {
    loading.value = false
  }
}

// 添加分组
const addSection = () => {
  const newSection = {
    id: generateId(),
    title: `第${paperData.value.sections.length + 1}部分`,
    description: '',
    questions: [],
    expanded: true
  }
  paperData.value.sections.push(newSection)
  hasUnsavedChanges.value = true
}

// 删除分组
const deleteSection = async (sectionId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个分组吗？删除后该分组下的所有题目也将被移除。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const index = paperData.value.sections.findIndex(section => section.id === sectionId)
    if (index !== -1) {
      paperData.value.sections.splice(index, 1)
      hasUnsavedChanges.value = true
      ElMessage.success('分组删除成功')
    }
  } catch {
    // 用户取消删除
  }
}

// 切换分组展开状态
const toggleSection = (sectionId) => {
  const section = paperData.value.sections.find(s => s.id === sectionId)
  if (section) {
    section.expanded = !section.expanded
  }
}

// 更新分组标题
const updateSectionTitle = (sectionId, title) => {
  const section = paperData.value.sections.find(s => s.id === sectionId)
  if (section) {
    section.title = title
    hasUnsavedChanges.value = true
  }
}

// 更新分组描述
const updateSectionDescription = (sectionId, description) => {
  const section = paperData.value.sections.find(s => s.id === sectionId)
  if (section) {
    section.description = description
    hasUnsavedChanges.value = true
  }
}

// 添加题目
const addQuestion = async (sectionId) => {
  currentSectionId.value = sectionId
  
  // 检查courseId是否存在
  if (!courseId.value) {
    ElMessage.error('缺少课程ID，无法获取题目')
    return
  }
  
  try {
    loading.value = true
    // 调用API获取课程题目
    const questions = await getCourseQuestions(courseId.value)
    
    // 转换后端数据格式为前端格式
    availableQuestions.value = questions.map(question => ({
      id: question.id,
      type: mapBackendTypeToFrontend(question.type),
      title: question.stem || '无标题',
      content: question.stem || '无内容',
      difficulty: mapBackendDifficultyToFrontend(question.difficulty),
      selected: false,
      // 保留原始数据用于后续处理
      originalData: question
    }))
    
    showQuestionDialog.value = true
    ElMessage.success(`成功获取 ${questions.length} 道题目`)
  } catch (error) {
    console.error('获取课程题目失败:', error)
    ElMessage.error('获取课程题目失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 加载公共题库
const loadPublicQuestions = async () => {
  try {
    loadingPublicQuestions.value = true
    
    // 调用API获取公共题库
    const publicQuestions = await getQuestionsList()
    
    // 处理返回的数据结构 - 根据API返回的QuestionVo结构
    let allQuestions = []
    
    if (Array.isArray(publicQuestions)) {
      // 遍历每个课程的题目
      publicQuestions.forEach(courseQuestions => {
        if (courseQuestions.question && Array.isArray(courseQuestions.question)) {
          const courseTitle = courseQuestions.title || '未知课程'
          
          courseQuestions.question.forEach(question => {
            allQuestions.push({
              id: question.id,
              type: mapBackendTypeToFrontend(question.type),
              title: question.stem || '无标题',
              content: question.stem || '无内容',
              difficulty: mapBackendDifficultyToFrontend(question.difficulty),
              tags: question.tags || [],
              courseTitle: courseTitle,
              selected: false,
              originalData: question
            })
          })
        }
      })
    }
    
    // 更新可用题目列表
    availableQuestions.value = allQuestions
    
    ElMessage.success(`成功加载 ${allQuestions.length} 道公共题目`)
  } catch (error) {
    console.error('加载公共题库失败:', error)
    ElMessage.error('加载公共题库失败: ' + error.message)
  } finally {
    loadingPublicQuestions.value = false
  }
}

// 处理题目选择
const handleQuestionSelect = (question) => {
  // 这里可以添加选择逻辑
  // TODO 增加选择后不显示的功能
}

// 确认添加题目
const confirmQuestionSelect = () => {
  const section = paperData.value.sections.find(s => s.id === currentSectionId.value)
  if (section && selectedQuestions.value.length > 0) {
    selectedQuestions.value.forEach((question, index) => {
      const newQuestion = {
        id: generateId(),
        questionId: question.id,
        title: question.title,
        type: question.type,
        difficulty: question.difficulty,
        score: getDefaultScore(question.type),
        orderIndex: section.questions.length + index
      }
      section.questions.push(newQuestion)
    })
    
    hasUnsavedChanges.value = true
    showQuestionDialog.value = false
    ElMessage.success(`成功添加 ${selectedQuestions.value.length} 道题目`)
  }
}

// 取消题目选择
const cancelQuestionSelect = () => {
  showQuestionDialog.value = false
  availableQuestions.value.forEach(question => {
    question.selected = false
  })
}

// 删除题目
const deleteQuestion = async (sectionId, questionId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这道题目吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const section = paperData.value.sections.find(s => s.id === sectionId)
    if (section) {
      const index = section.questions.findIndex(q => q.id === questionId)
      if (index !== -1) {
        section.questions.splice(index, 1)
        hasUnsavedChanges.value = true
        ElMessage.success('题目删除成功')
      }
    }
  } catch {
    // 用户取消删除
  }
}

// 更新题目分数
const updateQuestionScore = (questionId, score) => {
  paperData.value.sections.forEach(section => {
    const question = section.questions.find(q => q.id === questionId)
    if (question) {
      question.score = score
      hasUnsavedChanges.value = true
    }
  })
}

// 编辑题目
const editQuestion = (question) => {
  ElMessage.info('题目编辑功能开发中...')
}

// 更新试卷信息
const updatePaperInfo = () => {
  hasUnsavedChanges.value = true
  paperData.value.updatedAt = new Date().toISOString()
}

// 数据验证函数
const validatePaperData = (data) => {
  const errors = []
  
  // 验证必需字段
  if (!data.assessmentId) {
    errors.push('测评ID不能为空')
  }
  if (!data.courseId) {
    errors.push('课程ID不能为空')
  }
  if (typeof data.totalScore !== 'number' || data.totalScore <= 0) {
    errors.push('试卷总分必须是大于0的数字')
  }
  if (!Array.isArray(data.sections) || data.sections.length === 0) {
    errors.push('试卷必须包含至少一个题目分组')
  }
  
  // 验证分组数据
  data.sections.forEach((section, sectionIndex) => {
    if (!section.title || section.title.trim() === '') {
      errors.push(`第${sectionIndex + 1}个分组的标题不能为空`)
    }
    if (!Array.isArray(section.questions) || section.questions.length === 0) {
      errors.push(`第${sectionIndex + 1}个分组必须包含至少一道题目`)
    }
    
    // 验证题目数据
    section.questions.forEach((question, questionIndex) => {
      if (!question.questionId) {
        errors.push(`第${sectionIndex + 1}个分组的第${questionIndex + 1}道题目ID不能为空`)
      }
      if (typeof question.score !== 'number' || question.score <= 0) {
        errors.push(`第${sectionIndex + 1}个分组的第${questionIndex + 1}道题目分值必须是大于0的数字`)
      }
      if (typeof question.orderIndex !== 'number' || question.orderIndex < 0) {
        errors.push(`第${sectionIndex + 1}个分组的第${questionIndex + 1}道题目排序索引必须是非负数`)
      }
    })
  })
  
  return errors
}

// 保存试卷
const savePaper = async () => {
  try {
    // 获取当前用户ID
    const currentUserId = getCurrentUserId()
    if (!currentUserId) {
      ElMessage.error('无法获取当前用户信息，请重新登录')
      return
    }
    
    // 更新总分
    paperData.value.totalScore = totalScore.value
    
    // 准备要发送的数据，严格按照Paper.java结构
    const paperDataToSave = {
      assessmentId: assessmentId.value,
      courseId: courseId.value,
      creatorId: currentUserId, // 使用从token解析的用户ID
      title: paperData.value.title || '',
      description: paperData.value.description || '',
      totalScore: paperData.value.totalScore,
      sections: paperData.value.sections.map(section => ({
        title: section.title,
        description: section.description || '',
        questions: section.questions.map((question, index) => ({
          questionId: question.questionId,
          score: question.score,
          orderIndex: index // 确保orderIndex从0开始递增
        }))
      }))
    }
    
    // 数据验证
    const validationErrors = validatePaperData(paperDataToSave)
    if (validationErrors.length > 0) {
      ElMessage.error(`数据验证失败：${validationErrors.join('；')}`)
      return
    }
    
    // 显示加载状态
    const loadingMessage = ElMessage({
      message: '正在保存试卷...',
      type: 'info',
      duration: 0
    })
    
    try {
      // 调用API保存试卷数据
      const result = await createPaper(paperDataToSave)
      
      // 关闭加载消息
      loadingMessage.close()
      
      // 更新本地数据
      if (result && result.id) {
        paperData.value.id = result.id
      }
      paperData.value.updatedAt = new Date().toISOString()
      hasUnsavedChanges.value = false
      
      ElMessage.success('试卷保存成功')
      
      // 可选：保存成功后跳转到试卷列表或其他页面
      // router.push('/teacher/papers')
      
    } catch (apiError) {
      // 关闭加载消息
      loadingMessage.close()
      
      console.error('API调用失败:', apiError)
      
      // 根据错误类型提供具体的错误信息
      let errorMessage = '保存试卷失败，请重试'
      if (apiError.message) {
        if (apiError.message.includes('网络')) {
          errorMessage = '网络连接失败，请检查网络后重试'
        } else if (apiError.message.includes('权限')) {
          errorMessage = '没有权限保存试卷，请联系管理员'
        } else if (apiError.message.includes('数据')) {
          errorMessage = '数据格式错误，请检查试卷内容'
        } else {
          errorMessage = `保存失败：${apiError.message}`
        }
      }
      
      ElMessage.error(errorMessage)
    }
    
  } catch (error) {
    console.error('保存试卷时发生未知错误:', error)
    ElMessage.error('保存试卷时发生未知错误，请重试')
  }
}

// 预览试卷
const previewPaper = () => {
  ElMessage.info('试卷预览功能开发中...')
}

// 批量删除题目
const batchDeleteQuestions = async (sectionId, questionIds) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${questionIds.length} 道题目吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const section = paperData.value.sections.find(s => s.id === sectionId)
    if (section) {
      section.questions = section.questions.filter(q => !questionIds.includes(q.id))
      hasUnsavedChanges.value = true
      ElMessage.success(`成功删除 ${questionIds.length} 道题目`)
    }
  } catch {
    // 用户取消删除
  }
}

// 复制题目
const duplicateQuestion = (sectionId, questionId) => {
  const section = paperData.value.sections.find(s => s.id === sectionId)
  if (section) {
    const originalQuestion = section.questions.find(q => q.id === questionId)
    if (originalQuestion) {
      const duplicatedQuestion = {
        ...originalQuestion,
        id: generateId(),
        title: originalQuestion.title + ' (副本)',
        orderIndex: section.questions.length
      }
      section.questions.push(duplicatedQuestion)
      hasUnsavedChanges.value = true
      ElMessage.success('题目复制成功')
    }
  }
}

// 移动题目到其他分组
const moveQuestionToSection = (fromSectionId, questionId, toSectionId) => {
  const fromSection = paperData.value.sections.find(s => s.id === fromSectionId)
  const toSection = paperData.value.sections.find(s => s.id === toSectionId)
  
  if (fromSection && toSection && fromSectionId !== toSectionId) {
    const questionIndex = fromSection.questions.findIndex(q => q.id === questionId)
    if (questionIndex !== -1) {
      const question = fromSection.questions.splice(questionIndex, 1)[0]
      question.orderIndex = toSection.questions.length
      toSection.questions.push(question)
      hasUnsavedChanges.value = true
      ElMessage.success('题目移动成功')
    }
  }
}

// 批量设置题目分数
const batchSetQuestionScore = (sectionId, questionIds, score) => {
  const section = paperData.value.sections.find(s => s.id === sectionId)
  if (section) {
    section.questions.forEach(question => {
      if (questionIds.includes(question.id)) {
        question.score = score
      }
    })
    hasUnsavedChanges.value = true
    ElMessage.success(`成功设置 ${questionIds.length} 道题目的分数`)
  }
}

// 自动分配题目分数
const autoAssignScores = () => {
  paperData.value.sections.forEach(section => {
    section.questions.forEach(question => {
      question.score = getDefaultScore(question.type)
    })
  })
  hasUnsavedChanges.value = true
  ElMessage.success('自动分配分数完成')
}

// 题目排序
const sortQuestions = (sectionId, sortType) => {
  const section = paperData.value.sections.find(s => s.id === sectionId)
  if (section) {
    switch (sortType) {
      case 'difficulty':
        section.questions.sort((a, b) => {
          const difficultyOrder = { easy: 1, medium: 2, hard: 3 }
          return difficultyOrder[a.difficulty] - difficultyOrder[b.difficulty]
        })
        break
      case 'type':
        section.questions.sort((a, b) => a.type.localeCompare(b.type))
        break
      case 'score':
        section.questions.sort((a, b) => (b.score || 0) - (a.score || 0))
        break
      default:
        break
    }
    
    // 重新分配orderIndex
    section.questions.forEach((question, index) => {
      question.orderIndex = index
    })
    
    hasUnsavedChanges.value = true
    ElMessage.success('题目排序完成')
  }
}

// 获取题目类型图标
const getQuestionIcon = (type) => {
  const icons = {
    single_choice: ['fas', 'dot-circle'],
    multiple_choice: ['fas', 'check-square'],
    true_false: ['fas', 'question-circle'],
    fill_blank: ['fas', 'edit'],
    short_answer: ['fas', 'align-left'],
    programming: ['fas', 'code']
  }
  return icons[type] || ['fas', 'question']
}

// 获取题目类型名称
const getQuestionTypeName = (type) => {
  const names = {
    single_choice: '单选题',
    multiple_choice: '多选题',
    true_false: '判断题',
    fill_blank: '填空题',
    short_answer: '简答题',
    programming: '程序题'
  }
  return names[type] || '未知题型'
}

// 获取难度名称
const getDifficultyName = (difficulty) => {
  const names = {
    easy: '简单',
    medium: '中等',
    hard: '困难'
  }
  return names[difficulty] || '未知'
}

// 获取默认分数
const getDefaultScore = (type) => {
  const defaultScores = {
    single_choice: 2,
    multiple_choice: 3,
    true_false: 1,
    fill_blank: 2,
    short_answer: 5,
    programming: 15
  }
  return defaultScores[type] || 2
}

// 获取测评类型名称
const getAssessmentTypeName = (type) => {
  const typeNames = {
    ClassroomExam: '课堂作业',
    ChapterExam: '章节作业',
    MidtermExam: '期中考试',
    FinalExam: '期末考试',
    homework: '作业'
  }
  return typeNames[type] || '未知类型'
}

// 映射后端题目类型到前端
const mapBackendTypeToFrontend = (backendType) => {
  const typeMap = {
    'SINGLE_CHOICE': 'single_choice',
    'MULTI_CHOICE': 'multiple_choice',
    'FILL_IN_BLANKS': 'fill_blank',
    'TRUE_FALSE': 'true_false',
    'SHORT_ANSWER': 'short_answer',
    'PROGRAMMING': 'programming'
  }
  return typeMap[backendType] || 'single_choice'
}

// 映射后端难度到前端
const mapBackendDifficultyToFrontend = (backendDifficulty) => {
  const difficultyMap = {
    'EASY': 'easy',
    'MEDIUM': 'medium',
    'HARD': 'hard'
  }
  return difficultyMap[backendDifficulty] || 'easy'
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '未设置'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 拖拽事件处理
const onDragStart = () => {
  // 拖拽开始时的处理
  console.log('开始拖拽')
}

const onDragEnd = () => {
  // 拖拽结束时的处理
  hasUnsavedChanges.value = true
  updateTotalScore()
  ElMessage.success('试卷结构已更新')
}

// 题目拖拽处理
const onQuestionDragStart = () => {
  console.log('开始拖拽题目')
}

const onQuestionDragEnd = () => {
  hasUnsavedChanges.value = true
  updateTotalScore()
}

// 监听数据变化
watch(
  paperData,
  () => {
    // 可以在这里添加自动保存逻辑
  },
  { deep: true }
)

// 页面初始化
onMounted(() => {
  initializePaper()
})

// 页面离开前确认
window.addEventListener('beforeunload', (e) => {
  if (hasUnsavedChanges.value) {
    e.preventDefault()
    e.returnValue = '您有未保存的更改，确定要离开吗？'
  }
})
</script>

<style scoped>
/* 新拟态设计基础类 */
.neumorphism-raised {
  background: #f0f0f3;
  box-shadow: 
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.neumorphism-raised:hover {
  box-shadow: 
    4px 4px 8px #d1d1d4,
    -4px -4px 8px #ffffff;
  transform: translateY(-2px);
}

.neumorphism-inset {
  background: #f0f0f3;
  box-shadow: 
    inset 8px 8px 16px #d1d1d4,
    inset -8px -8px 16px #ffffff;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.frosted-glass {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 16px;
}

/* 基础样式 */
.paper-center-container {
  min-height: 100vh;
  background: linear-gradient(145deg, #f0f0f3 0%, #e8e8eb 100%);
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: #303133;
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background: #f0f0f3;
  box-shadow: 
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
  border-radius: 16px;
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.header-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  pointer-events: none;
  z-index: -1;
}

.header-left {
  flex: 1;
  position: relative;
  z-index: 1000;
}

.back-btn {
  color: #606266;
  font-size: 14px;
  margin-bottom: 8px;
  padding: 0;
  border: none;
  background: none;
  position: relative;
  z-index: 1001;
}

.back-btn:hover {
  color: #409EFF;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 1001;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  letter-spacing: -0.5px;
  line-height: 1.2;
  text-rendering: optimizeLegibility;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.page-subtitle {
  font-size: 16px;
  color: #606266;
  margin: 0;
  position: relative;
  z-index: 1001;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  background: #f0f0f3;
  box-shadow: 
    6px 6px 12px #d1d1d4,
    -6px -6px 12px #ffffff;
  border: none;
  border-radius: 12px;
  padding: 12px 20px;
  font-weight: 500;
  font-size: 14px;
  color: #606266;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  z-index: 1002;
}

.action-btn:hover {
  box-shadow: 
    3px 3px 6px #d1d1d4,
    -3px -3px 6px #ffffff;
  transform: translateY(-1px);
  color: #409EFF;
}

.action-btn:active {
  box-shadow: 
    inset 3px 3px 6px #d1d1d4,
    inset -3px -3px 6px #ffffff;
  transform: translateY(0);
}

.action-btn.primary {
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  color: white;
  box-shadow: 
    6px 6px 12px rgba(64, 158, 255, 0.3),
    -6px -6px 12px rgba(255, 255, 255, 0.8);
}

.action-btn.primary:hover {
  box-shadow: 
    3px 3px 6px rgba(64, 158, 255, 0.4),
    -3px -3px 6px rgba(255, 255, 255, 0.9);
}

.action-btn.danger {
  color: #F56C6C;
}

.action-btn.danger:hover {
  color: #F56C6C;
  background: rgba(245, 108, 108, 0.1);
}

/* 新拟态设计样式 */
.neumorphism-raised {
  background: #f0f0f3;
  box-shadow: 8px 8px 16px #d1d1d4, -8px -8px 16px #ffffff;
}

.neumorphism-inset {
  background: #f0f0f3;
  box-shadow: inset 8px 8px 16px #d1d1d4, inset -8px -8px 16px #ffffff;
}

/* 试卷信息区域 */
.paper-info-section {
  margin-bottom: 24px;
}

.info-card {
  background: #f0f0f3;
  box-shadow: 
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
  border-radius: 16px;
  padding: 24px;
  transition: all 0.3s ease;
}

.info-card:hover {
  box-shadow: 
    4px 4px 8px #d1d1d4,
    -4px -4px 8px #ffffff;
  transform: translateY(-2px);
}

.info-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-row {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-item {
  flex: 1;
}

.info-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
}

.custom-input,
.custom-textarea {
  background: #f0f0f3;
  box-shadow: 
    inset 6px 6px 12px #d1d1d4,
    inset -6px -6px 12px #ffffff;
  border: none;
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 14px;
  color: #303133;
  transition: all 0.3s ease;
  width: 100%;
}

.custom-input:focus,
.custom-textarea:focus {
  outline: none;
  box-shadow: 
    inset 3px 3px 6px #d1d1d4,
    inset -3px -3px 6px #ffffff,
    0 0 0 2px rgba(64, 158, 255, 0.2);
}

.custom-input::placeholder,
.custom-textarea::placeholder {
  color: #C0C4CC;
}

.score-display,
.count-display,
.section-display {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.score-value,
.count-value,
.section-value {
  font-size: 24px;
  font-weight: 600;
  color: #409EFF;
}

.score-unit,
.count-unit,
.section-unit {
  font-size: 14px;
  color: #909399;
}

/* 主要内容区域 */
.main-content {
  display: flex;
  gap: 24px;
}

.content-wrapper {
  display: flex;
  gap: 24px;
  width: 100%;
}

.edit-area {
  flex: 2;
}

.preview-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 分组卡片 */
.section-card {
  border-radius: 16px;
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
  z-index: 1001;
}

.add-section-btn {
  background: #409EFF;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.add-section-btn:hover {
  background: #66b1ff;
  transform: translateY(-1px);
}

/* 加载和空状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.loading-container .el-icon {
  font-size: 32px;
  margin-bottom: 16px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-sections {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 48px;
  color: #C0C4CC;
  margin-bottom: 16px;
}

.empty-sections h3 {
  font-size: 18px;
  color: #606266;
  margin: 16px 0 8px 0;
}

.empty-sections p {
  color: #909399;
  margin-bottom: 24px;
}

.add-first-section-btn {
  background: #409EFF;
  color: white;
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  font-size: 16px;
  transition: all 0.3s ease;
}

.add-first-section-btn:hover {
  background: #66b1ff;
  transform: translateY(-2px);
}

/* 分组项目 */
.sections-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-item {
  background: #f0f0f3;
  box-shadow: 
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
  border-radius: 16px;
  padding: 20px;
  transition: all 0.3s ease;
  position: relative;
}

.section-item:hover {
  box-shadow: 
    4px 4px 8px #d1d1d4,
    -4px -4px 8px #ffffff;
  transform: translateY(-2px);
}

.section-item .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.drag-handle {
  color: #909399;
  cursor: grab;
  font-size: 16px;
  padding: 8px;
  border-radius: 8px;
  background: #f0f0f3;
  box-shadow: 
    4px 4px 8px #d1d1d4,
    -4px -4px 8px #ffffff;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.drag-handle:hover {
  color: #409EFF;
  box-shadow: 
    2px 2px 4px #d1d1d4,
    -2px -2px 4px #ffffff;
  transform: scale(1.05);
}

.drag-handle:active {
  cursor: grabbing;
  box-shadow: 
    inset 4px 4px 8px #d1d1d4,
    inset -4px -4px 8px #ffffff;
  transform: scale(0.95);
}

.section-number {
  font-size: 14px;
  font-weight: 600;
  color: #409EFF;
  min-width: 80px;
  position: relative;
  z-index: 1001;
}

.section-title-container {
  flex: 1;
}

.section-title-input {
  background: transparent;
  border: none;
  box-shadow: none;
}

.section-actions {
  display: flex;
  gap: 8px;
}

.section-actions .action-btn {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 8px;
}

.expand-btn {
  background: #E6F7FF;
  color: #1890ff;
}

.delete-btn {
  background: #FFF2F0;
  color: #ff4d4f;
}

/* 分组描述 */
.section-description {
  margin-bottom: 16px;
}

.section-description-input {
  background: #f0f0f3;
  box-shadow: inset 4px 4px 8px #d1d1d4, inset -4px -4px 8px #ffffff;
  border: none;
  border-radius: 8px;
}

/* 题目容器 */
.questions-container {
  margin-top: 16px;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.question-item {
  background: #f0f0f3;
  box-shadow: 
    6px 6px 12px #d1d1d4,
    -6px -6px 12px #ffffff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
  position: relative;
}

.question-item:hover {
  box-shadow: 
    3px 3px 6px #d1d1d4,
    -3px -3px 6px #ffffff;
  transform: translateY(-1px);
}

.question-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.question-icon {
  color: #409EFF;
  font-size: 16px;
}

.question-number {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  min-width: 60px;
}

.question-content {
  flex: 1;
}

.question-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.question-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.question-score {
  display: flex;
  align-items: center;
  gap: 4px;
}

.score-input {
  width: 80px;
}

.score-unit {
  font-size: 12px;
  color: #909399;
}

.question-actions {
  display: flex;
  gap: 4px;
}

.question-actions .action-btn {
  padding: 4px 8px;
  font-size: 12px;
  border-radius: 6px;
}

.edit-btn {
  background: #F0F9FF;
  color: #0ea5e9;
}

/* 空题目状态 */
.empty-questions {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.empty-questions .empty-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

/* 预览区域 */
.preview-card,
.stats-card,
.distribution-card {
  background: #f0f0f3;
  box-shadow: 
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
  border-radius: 16px;
  padding: 20px;
  transition: all 0.3s ease;
}

.preview-card:hover,
.stats-card:hover,
.distribution-card:hover {
  box-shadow: 
    4px 4px 8px #d1d1d4,
    -4px -4px 8px #ffffff;
  transform: translateY(-2px);
}

.preview-header,
.stats-header,
.distribution-header {
  margin-bottom: 16px;
}

.preview-header h3,
.stats-header h3,
.distribution-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 试卷预览 */
.preview-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.preview-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 20px;
}

.preview-section {
  margin-bottom: 20px;
}

.preview-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #409EFF;
  margin-bottom: 8px;
  display: flex;
  gap: 8px;
  position: relative;
  z-index: 1001;
}

.preview-section-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
  font-style: italic;
}

.preview-questions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-question {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
}

.preview-question .question-number {
  color: #606266;
  font-weight: 500;
  min-width: 50px;
}

.preview-question .question-icon {
  color: #409EFF;
  font-size: 12px;
}

.preview-question .question-title {
  flex: 1;
  color: #303133;
}

.preview-question .question-score {
  color: #E6A23C;
  font-weight: 500;
}

.preview-empty {
  text-align: center;
  color: #C0C4CC;
  font-style: italic;
}

/* 统计卡片 */
.stats-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  background: #409EFF;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  display: block;
  position: relative;
  z-index: 1001;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  position: relative;
  z-index: 1001;
}

/* 题型分布 */
.distribution-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.distribution-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
}

.type-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.type-icon {
  color: #409EFF;
  font-size: 14px;
}

.type-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  position: relative;
  z-index: 1001;
}

.type-stats {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.type-count,
.type-score {
  font-size: 12px;
  color: #606266;
}

.distribution-empty {
  text-align: center;
  color: #C0C4CC;
  font-style: italic;
  padding: 20px;
}

/* 题目选择对话框 */
.question-dialog {
  border-radius: 16px;
}

.question-selector {
  max-height: 600px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.filter-section {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #EBEEF5;
}

.filter-row {
  display: flex;
  gap: 16px;
  align-items: center;
}

.filter-select {
  width: 150px;
}

.filter-input {
  flex: 1;
}

.tag-search {
  width: 200px;
  flex: none;
}

.public-bank-btn {
  background: #409EFF;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.public-bank-btn:hover {
  background: #337ecc;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.3);
}

.question-list {
  flex: 1;
  overflow-y: auto;
  max-height: 400px;
}

.question-option {
  display: flex;
  gap: 12px;
  padding: 16px;
  border: 1px solid #EBEEF5;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.3s ease;
}

.question-option:hover {
  border-color: #409EFF;
  background: #F0F9FF;
}

.question-checkbox {
  margin-top: 4px;
}

.question-content {
  flex: 1;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.question-type-icon {
  color: #409EFF;
  font-size: 14px;
}

.question-type-text {
  font-size: 12px;
  color: #606266;
  background: #F0F9FF;
  padding: 2px 8px;
  border-radius: 4px;
}

.question-difficulty-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  color: white;
}

.question-difficulty-badge.easy {
  background: #67C23A;
}

.question-difficulty-badge.medium {
  background: #E6A23C;
}

.question-difficulty-badge.hard {
  background: #F56C6C;
}

.question-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.question-preview {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.no-questions {
  text-align: center;
  padding: 40px;
}

/* 对话框底部 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn {
  background: #f0f0f3;
  box-shadow: 8px 8px 16px #d1d1d4, -8px -8px 16px #ffffff;
  border: none;
  border-radius: 8px;
  color: #606266;
}

.confirm-btn {
  background: #409EFF;
  color: white;
  border: none;
  border-radius: 8px;
}

/* 拖拽样式 */
.ghost-section,
.ghost-question {
  opacity: 0.3;
  background: rgba(64, 158, 255, 0.1);
  border: 2px dashed #409EFF;
  border-radius: 12px;
  transform: scale(0.95);
}

.chosen-section,
.chosen-question {
  transform: scale(1.02);
  box-shadow: 
    12px 12px 24px #d1d1d4,
    -12px -12px 24px #ffffff;
  transition: all 0.2s ease;
}

.drag-section,
.drag-question {
  transform: rotate(2deg) scale(1.05);
  box-shadow: 
    16px 16px 32px rgba(209, 209, 212, 0.8),
    -16px -16px 32px rgba(255, 255, 255, 0.8);
  z-index: 1000;
  transition: all 0.3s ease;
}

/* 测评详细信息样式 */
.assessment-details {
  border-top: 1px solid #e8e8e8;
  padding-top: 16px;
  margin-top: 16px;
}

.type-display,
.duration-display,
.status-display,
.time-display {
  display: flex;
  align-items: center;
  gap: 4px;
}

.type-value,
.duration-value,
.time-value {
  font-weight: 600;
  color: #2c3e50;
}

.duration-unit {
  color: #7f8c8d;
  font-size: 12px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.published {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  color: white;
}

.status-badge.draft {
  background: linear-gradient(135deg, #f56c6c, #f78989);
  color: white;
}

.time-details {
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
  margin-top: 12px;
}

.time-value {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  background: #f8f9fa;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid #e9ecef;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content-wrapper {
    flex-direction: column;
  }
  
  .preview-area {
    flex-direction: row;
  }
  
  .page-title {
    font-size: 28px;
  }
}

@media (max-width: 768px) {
  .paper-center-container {
    padding: 12px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .page-title {
    font-size: 24px;
    gap: 8px;
  }
  
  .info-row {
    flex-direction: column;
    gap: 16px;
  }
  
  .preview-area {
    flex-direction: column;
  }
  
  .filter-row {
    flex-direction: column;
    gap: 12px;
  }
  
  .filter-select {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 20px;
    gap: 6px;
    flex-direction: column;
    align-items: flex-start;
    line-height: 1.3;
  }
  
  .page-subtitle {
    font-size: 14px;
    margin-top: 4px;
  }
}
</style>