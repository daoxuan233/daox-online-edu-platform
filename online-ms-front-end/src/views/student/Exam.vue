<template>
  <div class="exam-page">
    <!-- 加载状态 -->
    <div v-if="loading || showStartConfirm" class="loading-container">
      <div v-if="loading" class="loading-content">
        <el-icon class="loading-icon" size="48">
          <Loading />
        </el-icon>
        <p class="loading-text">正在加载试卷内容...</p>
      </div>
    </div>
    
    <!-- 考试内容 -->
    <div v-else-if="!loading && !showStartConfirm && questions.length > 0" class="exam-content">
      <!-- 考试头部信息 -->
      <div class="exam-header glass-card">
      <div class="exam-info">
        <h1 class="exam-title">{{ examInfo.title }}</h1>
        <div class="exam-meta">
          <span v-if="examInfo.description" class="exam-description">
            <i class="fas fa-info-circle"></i>
            {{ examInfo.description }}
          </span>
          <span class="question-count">
            <i class="fas fa-question-circle"></i>
            {{ examInfo.totalQuestions }} 题
          </span>
          <span class="total-score">
            <i class="fas fa-star"></i>
            总分 {{ examInfo.totalScore }} 分
          </span>
        </div>
      </div>
      
      <div class="exam-status">
        <!-- 倒计时 -->
        <div class="countdown-timer neu-card">
          <div class="timer-display">
            <span class="time-number">{{ formatTime(remainingTime) }}</span>
            <span class="time-label">剩余时间</span>
          </div>
          <el-progress 
            :percentage="timeProgress" 
            :stroke-width="6" 
            :show-text="false"
            :color="getTimeColor()"
            class="time-progress"
          />
        </div>
        
        <!-- 进度信息 -->
        <div class="progress-info neu-card">
          <div class="progress-display">
            <span class="progress-number">{{ answeredCount }}/{{ examInfo.totalQuestions }}</span>
            <span class="progress-label">已答题</span>
          </div>
          <el-progress 
            :percentage="answerProgress" 
            :stroke-width="6" 
            :show-text="false"
            class="answer-progress"
          />
        </div>
      </div>
    </div>

    <div class="exam-content" style="display: flex;">
      <!-- 题目导航 -->
      <div class="question-nav glass-card">
        <div class="nav-header">
          <h3>题目导航</h3>
          <el-button text @click="toggleNav">
            <i class="fas fa-bars"></i>
          </el-button>
        </div>
        
        <div class="nav-legend">
          <div class="legend-item">
            <span class="legend-dot answered"></span>
            <span>已答</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot current"></span>
            <span>当前</span>
          </div>
        </div>
        
        <div class="question-grid">
          <div 
            v-for="(question, index) in questions" 
            :key="question.id"
            class="question-nav-item"
            :class="getQuestionNavClass(question, index)"
            @click="goToQuestion(index)"
          >
            {{ index + 1 }}
          </div>
        </div>
        
        <div class="nav-actions">
          <el-button 
            type="danger"
            @click="showSubmitDialog = true"
            style="margin-left: 0"
          >
            <i class="fas fa-paper-plane"></i>
            提交试卷
          </el-button>
        </div>
      </div>

      <!-- 题目内容 -->
      <div class="question-content">
        <div class="question-card neu-card">
          <!-- 题目头部 -->
          <div class="question-header">
            <div class="question-number">
              <span class="number">{{ currentQuestionIndex + 1 }}</span>
              <span class="total">/ {{ questions.length }}</span>
            </div>
            <div class="question-type">
              <el-tag :type="getQuestionTypeColor(currentQuestion.type)" size="small">
                {{ getQuestionTypeName(currentQuestion.type) }}
              </el-tag>
            </div>
            <div class="question-score">
              <span class="score">{{ currentQuestion.score }}</span>
              <span class="unit">分</span>
            </div>
          </div>

          <!-- 题目内容 -->
          <div class="question-body">
            <div class="question-text" v-html="currentQuestion.content"></div>
            
            <!-- 图片或附件 -->
            <div v-if="currentQuestion.attachments" class="question-attachments">
              <div v-for="attachment in currentQuestion.attachments" :key="attachment.id" class="attachment-item">
                <img v-if="attachment.type === 'image'" :src="attachment.url" :alt="attachment.name" class="attachment-image" />
                <div v-else class="attachment-file">
                  <i :class="getFileIcon(attachment.type)"></i>
                  <span>{{ attachment.name }}</span>
                </div>
              </div>
            </div>

            <!-- 选择题选项 -->
            <div v-if="isChoiceQuestion(currentQuestion.type)" class="question-options">
              <div 
                v-for="option in currentQuestion.options" 
                :key="option.id"
                class="option-item"
                :class="{ selected: isOptionSelected(option.id) }"
                @click="selectOption(option.id)"
              >
                <div class="option-marker">
                  <span class="option-label">{{ option.label }}</span>
                  <div class="option-indicator">
                    <i v-if="currentQuestion.type === 'single_choice'" 
                       :class="isOptionSelected(option.id) ? 'fas fa-dot-circle' : 'far fa-circle'"></i>
                    <i v-else 
                       :class="isOptionSelected(option.id) ? 'fas fa-check-square' : 'far fa-square'"></i>
                  </div>
                </div>
                <div class="option-content" v-html="option.content"></div>
              </div>
            </div>

            <!-- 填空题 -->
            <div v-else-if="currentQuestion.type === 'fill_blank'" class="question-blanks">
              <div v-for="(blank, index) in currentQuestion.blanks" :key="index" class="blank-item">
                <label class="blank-label">空{{ index + 1 }}：</label>
                <el-input 
                  v-model="answers[currentQuestion.id].blanks[index]"
                  placeholder="请输入答案"
                  class="neu-input blank-input"
                />
              </div>
            </div>

            <!-- 简答题 -->
            <div v-else-if="currentQuestion.type === 'essay'" class="question-essay">
              <el-input 
                v-model="answers[currentQuestion.id].content"
                type="textarea"
                :rows="8"
                placeholder="请输入你的答案..."
                class="neu-input essay-input"
                show-word-limit
                :maxlength="currentQuestion.maxLength || 1000"
              />
            </div>

            <!-- 编程题 -->
            <div v-else-if="currentQuestion.type === 'coding'" class="question-coding">
              <div class="coding-header">
                <span class="language-label">编程语言：</span>
                <el-select v-model="answers[currentQuestion.id].language" class="language-select">
                  <el-option 
                    v-for="lang in currentQuestion.languages" 
                    :key="lang" 
                    :label="lang" 
                    :value="lang"
                  />
                </el-select>
              </div>
              <el-input 
                v-model="answers[currentQuestion.id].code"
                type="textarea"
                :rows="12"
                placeholder="请输入你的代码..."
                class="neu-input coding-input"
                show-word-limit
                :maxlength="5000"
              />
            </div>
          </div>
        </div>

        <!-- 题目导航按钮 -->
        <div class="question-navigation">
          <el-button 
            @click="previousQuestion" 
            :disabled="currentQuestionIndex === 0"
            class="neu-button nav-btn"
          >
            <i class="fas fa-chevron-left"></i>
            上一题
          </el-button>
          
          <div class="nav-center">
            <el-button 
              @click="saveAnswer"
              type="primary"
              class="neu-button-submit"
            >
              <i class="fas fa-save"></i>
              保存答案
            </el-button>
          </div>
          
          <el-button 
            @click="nextQuestion" 
            :disabled="currentQuestionIndex === questions.length - 1"
            class="neu-button nav-btn"
          >
            下一题
            <i class="fas fa-chevron-right"></i>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 提交确认对话框 -->
    <el-dialog 
      v-model="showSubmitDialog" 
      title="提交试卷" 
      width="500px"
      class="custom-dialog"
    >
      <div class="submit-summary">
        <div class="summary-item">
          <span class="label">总题数：</span>
          <span class="value">{{ questions.length }} 题</span>
        </div>
        <div class="summary-item">
          <span class="label">已答题：</span>
          <span class="value answered">{{ answeredCount }} 题</span>
        </div>
        <div class="summary-item">
          <span class="label">未答题：</span>
          <span class="value unanswered">{{ questions.length - answeredCount }} 题</span>
        </div>
        <div class="summary-item">
          <span class="label">剩余时间：</span>
          <span class="value time">{{ formatTime(remainingTime) }}</span>
        </div>
      </div>
      
      <div class="submit-warning">
        <i class="fas fa-exclamation-triangle"></i>
        <p>提交后将无法修改答案，请确认所有题目都已完成。</p>
      </div>
      
      <template #footer>
        <el-button @click="showSubmitDialog = false">取消</el-button>
        <el-button type="primary" @click="submitExam">确认提交</el-button>
      </template>
    </el-dialog>

      <!-- 自动保存提示 -->
      <div v-if="showAutoSave" class="auto-save-tip">
        <i class="fas fa-check-circle"></i>
        <span>答案已自动保存</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getPaperContent, submitAnswer, submitAssessment } from '@/api/students/stuAPI.js'

const route = useRoute()
const router = useRouter()
const assessmentId = route.params.assessmentId

// 响应式数据
const loading = ref(false)
const showStartConfirm = ref(true)
const paperData = ref(null)
const examInfo = ref({
  id: assessmentId,
  title: '',
  description: '',
  totalScore: 0,
  totalQuestions: 0,
  duration: 0 // 分钟，默认值
})

const currentQuestionIndex = ref(0)
const remainingTime = ref() // 秒
const showSubmitDialog = ref(false)
const showAutoSave = ref(false)
const timer = ref(null)

// 题目数据
const questions = ref([
  {
    id: 1,
    type: 'single_choice',
    content: 'Vue.js 3.0 中用于创建响应式数据的函数是？',
    score: 5,
    marked: false,
    options: [
      { id: 'A', label: 'A', content: 'reactive()' },
      { id: 'B', label: 'B', content: 'ref()' },
      { id: 'C', label: 'C', content: 'computed()' },
      { id: 'D', label: 'D', content: '以上都是' }
    ]
  },
  {
    id: 2,
    type: 'multiple_choice',
    content: '以下哪些是 Vue 3 Composition API 的特性？',
    score: 8,
    marked: false,
    options: [
      { id: 'A', label: 'A', content: '更好的 TypeScript 支持' },
      { id: 'B', label: 'B', content: '更灵活的逻辑复用' },
      { id: 'C', label: 'C', content: '更小的包体积' },
      { id: 'D', label: 'D', content: '向后兼容 Vue 2' }
    ]
  },
  {
    id: 3,
    type: 'fill_blank',
    content: '在 Vue 3 中，使用 _____ 函数可以创建一个响应式的引用，使用 _____ 函数可以创建一个响应式的对象。',
    score: 6,
    marked: false,
    blanks: ['', '']
  },
  {
    id: 4,
    type: 'essay',
    content: '请简述 Vue 3 Composition API 相比 Options API 的优势，并举例说明。',
    score: 15,
    marked: false,
    maxLength: 500
  },
  {
    id: 5,
    type: 'coding',
    content: '请使用 Vue 3 Composition API 编写一个计数器组件，包含增加、减少和重置功能。',
    score: 20,
    marked: false,
    languages: ['JavaScript', 'TypeScript']
  }
])

// 答案数据
const answers = ref({})

/**
 * 初始化答案数据结构
 */
const initAnswers = () => {
  questions.value.forEach(question => {
    answers.value[question.id] = {
      questionId: question.id,
      type: question.type,
      selectedOptions: [],
      blanks: question.type === 'fill_blank' ? new Array(question.blanks?.length || 1).fill('') : [],
      content: '',
      code: '',
      language: question.languages ? question.languages[0] : 'Java'
    }
  })
}

// 计算属性
const currentQuestion = computed(() => questions.value[currentQuestionIndex.value])

const answeredCount = computed(() => {
  return questions.value.filter(question => {
    const answer = answers.value[question.id]
    if (!answer) return false
    
    switch (question.type) {
      case 'single_choice':
      case 'multiple_choice':
      case 'true_false':
        return answer.selectedOptions.length > 0
      case 'fill_blank':
        return answer.blanks.some(blank => blank.trim() !== '')
      case 'essay':
        return answer.content.trim() !== ''
      case 'coding':
        return answer.code.trim() !== ''
      default:
        return false
    }
  }).length
})

const markedCount = computed(() => {
  return questions.value.filter(q => q.marked).length
})

const answerProgress = computed(() => {
  return Math.round((answeredCount.value / questions.value.length) * 100)
})
/**
 * 时间进度
 * @type {ComputedRef<number>} 时间进度，单位：%
 */
const timeProgress = computed(() => {
  const totalTime = examInfo.value.duration * 60
  return Math.round(((totalTime - remainingTime.value) / totalTime) * 100)
})

// 方法
const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  if (hours > 0) {
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  }
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

/**
 * 获取试卷内容
 * @param {string} assessmentId - 测评ID
 */
const fetchPaperContent = async (assessmentId) => {
  try {
    loading.value = true
    console.log('开始获取试卷内容，assessmentId:', assessmentId)
    
    const response = await getPaperContent(assessmentId)
    console.log('获取试卷内容响应:', response)

    paperData.value = response
    console.log('试卷数据:', response)

    // 更新考试信息
    examInfo.value = {
      id: assessmentId,
      title: response.title || '',
      description: response.description || '',
      totalScore: response.totalScore || 0,
      totalQuestions: response.sections?.reduce((total, section) =>
          total + (section.questions?.length || 0), 0) || 0,
      duration: response.durationMinutes
    }


    // 设置考试时间
    remainingTime.value = examInfo.value.duration * 60

    // 转换试卷数据为前端格式
    console.log('开始转换试卷数据')
    convertPaperData(response)
    console.log('转换后的题目数量:', questions.value.length)

    ElMessage.success('试卷加载成功')
  } catch (error) {
    console.error('获取试卷内容失败:', error)
    console.error('错误详情:', error.response || error)
    ElMessage.error(error.message || '获取试卷内容失败，请重试')
    // 返回上一页
    router.back()
  } finally {
    loading.value = false
  }
}

/**
 * 将后端题目类型映射为前端类型
 * @param {string} backendType - 后端题目类型
 * @returns {string} 前端题目类型
 */
const mapQuestionType = (backendType) => {
  const typeMap = {
    'SINGLE_CHOICE': 'single_choice',
    'MULTI_CHOICE': 'multiple_choice',
    'TRUE_FALSE': 'true_false',
    'FILL_IN_BLANKS': 'fill_blank',
    'SHORT_ANSWER': 'essay',
    'PROGRAMMING': 'coding'
  }
  return typeMap[backendType] || backendType.toLowerCase()
}

/**
 * 转换后端试卷数据为前端格式
 * @param {Object} paperData - 后端试卷数据
 */
const convertPaperData = (paperData) => {
  const convertedQuestions = []
  
  if (paperData.sections && Array.isArray(paperData.sections)) {
    paperData.sections.forEach(section => {
      if (section.questions && Array.isArray(section.questions)) {
        section.questions.forEach(paperQuestion => {
          // 注意：后端返回的是 questionDetails 而不是 question
          const questionDetails = paperQuestion.questionDetails
          if (questionDetails) {
            // 映射题目类型
            const frontendType = mapQuestionType(questionDetails.type)
            
            const convertedQuestion = {
              id: questionDetails.id,
              type: frontendType,
              content: questionDetails.stem, // 后端使用 stem 字段
              score: paperQuestion.score || 0,
              marked: false,
              sectionId: section.id,
              sectionTitle: section.title
            }
            
            // 根据题目类型添加特定字段
            switch (frontendType) {
              case 'single_choice':
              case 'multiple_choice':
                // 转换选项格式：{key, text} -> {id, label, content}
                if (questionDetails.options && Array.isArray(questionDetails.options)) {
                  convertedQuestion.options = questionDetails.options.map(option => ({
                    id: option.key,
                    label: option.key,
                    content: option.text
                  }))
                } else {
                  convertedQuestion.options = []
                }
                break
              case 'true_false':
                // 判断题：后端options为null，前端提供固定选项
                convertedQuestion.options = [
                  { id: 'T', label: 'T', content: '正确' },
                  { id: 'F', label: 'F', content: '错误' }
                ]
                break
              case 'fill_blank':
                // 填空题：后端options为null，根据题目内容中的空格数量确定填空数
                const blankCount = (questionDetails.stem.match(/___/g) || []).length
                convertedQuestion.blanks = new Array(Math.max(1, blankCount)).fill('')
                break
              case 'essay':
                // 简答题
                convertedQuestion.maxLength = questionDetails.maxLength || 1000
                break
              case 'coding':
                // 编程题
                convertedQuestion.languages = questionDetails.languages || ['Java', 'JavaScript', 'Python']
                convertedQuestion.template = questionDetails.template || ''
                break
            }
            
            convertedQuestions.push(convertedQuestion)
          }
        })
      }
    })
  }
  
  questions.value = convertedQuestions
  
  // 初始化答案数据结构
  initAnswers()
}

/**
 * 确认开始考试
 */
const confirmStartExam = async () => {
  try {
    const result = await ElMessageBox.confirm(
      '是否确认开始考试？',
      '开始考试确认',
      {
        confirmButtonText: '确认开始',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }
    )
    
    if (result === 'confirm') {
      showStartConfirm.value = false
      await fetchPaperContent(assessmentId)
      startTimer()
    }
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消，返回上一页
      router.back()
    }
  }
}

const getTimeColor = () => {
  const percentage = timeProgress.value
  if (percentage < 50) return '#67C23A'
  if (percentage < 80) return '#E6A23C'
  return '#F56C6C'
}

const getQuestionNavClass = (question, index) => {
  const answer = answers.value[question.id]
  const isAnswered = answer && (
    (question.type === 'single_choice' || question.type === 'multiple_choice') ? answer.selectedOptions.length > 0 :
    question.type === 'fill_blank' ? answer.blanks.some(blank => blank.trim() !== '') :
    question.type === 'essay' ? answer.content.trim() !== '' :
    question.type === 'coding' ? answer.code.trim() !== '' : false
  )
  
  return {
    current: index === currentQuestionIndex.value,
    answered: isAnswered,
    marked: question.marked,
    unanswered: !isAnswered
  }
}

const getQuestionTypeColor = (type) => {
  const colors = {
    single_choice: 'primary',
    multiple_choice: 'success',
    true_false: 'warning',
    fill_blank: 'info',
    essay: 'danger',
    coding: 'primary'
  }
  return colors[type] || ''
}

const getQuestionTypeName = (type) => {
  const names = {
    single_choice: '单选题',
    multiple_choice: '多选题',
    true_false: '判断题',
    fill_blank: '填空题',
    essay: '简答题',
    coding: '编程题'
  }
  return names[type] || '未知题型'
}

const isChoiceQuestion = (type) => {
  return type === 'single_choice' || type === 'multiple_choice' || type === 'true_false'
}

const isOptionSelected = (optionId) => {
  const answer = answers.value[currentQuestion.value.id]
  return answer && answer.selectedOptions.includes(optionId)
}

const selectOption = (optionId) => {
  const answer = answers.value[currentQuestion.value.id]
  if (!answer) return
  
  if (currentQuestion.value.type === 'single_choice' || currentQuestion.value.type === 'true_false') {
    // 单选题和判断题只能选择一个选项
    answer.selectedOptions = [optionId]
  } else if (currentQuestion.value.type === 'multiple_choice') {
    // 多选题可以选择多个选项
    const index = answer.selectedOptions.indexOf(optionId)
    if (index > -1) {
      answer.selectedOptions.splice(index, 1)
    } else {
      answer.selectedOptions.push(optionId)
    }
  }
  
  autoSave()
}

const goToQuestion = (index) => {
  currentQuestionIndex.value = index
}

const previousQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++
  }
}

const markQuestion = () => {
  currentQuestion.value.marked = !currentQuestion.value.marked
}

/**
 * 格式化当前题目的答案数据为API所需格式
 * @returns {Object} 格式化后的答案数据
 */
const formatCurrentAnswerData = () => {
  const question = currentQuestion.value
  const answer = answers.value[question.id]
  
  let response = ''
  
  switch (question.type) {
    case 'single_choice':
    case 'true_false':
      // 单选题和判断题：返回选中的选项ID
      response = answer.selectedOptions.length > 0 ? answer.selectedOptions[0] : ''
      break
    case 'multiple_choice':
      // 多选题：返回选中选项ID的JSON数组字符串
      response = JSON.stringify(answer.selectedOptions)
      break
    case 'fill_blank':
      // 填空题：返回所有空格答案的JSON数组字符串
      response = JSON.stringify(answer.blanks)
      break
    case 'essay':
      // 简答题：返回文本内容
      response = answer.content
      break
    case 'coding':
      // 编程题：返回包含代码和语言的JSON字符串
      response = JSON.stringify({
        code: answer.code,
        language: answer.language
      })
      break
    default:
      response = ''
  }
  
  return {
    questionId: question.id,
    response: response
  }
}

/**
 * 手动保存当前题目答案
 */
const saveAnswer = async () => {
  try {
    await showAutoSaveMessage()
    ElMessage.success('答案保存成功')
  } catch (error) {
    // 错误处理已在showAutoSaveMessage中完成
    console.error('手动保存答案失败:', error)
  }
}

/**
 * 自动保存当前题目答案（静默保存，不显示成功消息）
 */
const autoSave = async () => {
  try {
    await showAutoSaveMessage()
  } catch (error) {
    // 错误处理已在showAutoSaveMessage中完成
    console.error('自动保存答案失败:', error)
  }
}

/**
 * 显示自动保存消息并调用API保存当前题目答案
 */
const showAutoSaveMessage = async () => {
  try {
    // 获取当前题目的答案数据
    const answerData = formatCurrentAnswerData()
    
    // 检查是否有有效答案需要保存
    if (!answerData.response || answerData.response.trim() === '' || 
        answerData.response === '[]' || answerData.response === '[""]') {
      // 如果没有有效答案，只显示提示但不调用API
      showAutoSave.value = true
      setTimeout(() => {
        showAutoSave.value = false
      }, 2000)
      return
    }
    
    // 调用API保存答案
    await submitAnswer(assessmentId, answerData)
    
    // 显示成功提示
    showAutoSave.value = true
    setTimeout(() => {
      showAutoSave.value = false
    }, 2000)
    
    console.log('答案保存成功:', answerData)
    
  } catch (error) {
    console.error('保存答案失败:', error)
    
    // 显示错误提示
    ElMessage.error({
      message: `保存答案失败: ${error.message}`,
      duration: 3000,
      showClose: true
    })
    
    // 即使保存失败，也要隐藏自动保存提示
    showAutoSave.value = false
  }
}
/**
 * 提交考试
 */
const submitExam = async () => {
  try {
    // 关闭提交确认对话框
    showSubmitDialog.value = false
    
    // 显示提交中的加载提示
    const loadingMessage = ElMessage({
      message: '正在提交试卷...',
      type: 'info',
      duration: 0, // 不自动关闭
      showClose: false
    })
    
    // 调用 API 提交试卷
    await submitAssessment(assessmentId)
    
    // 关闭加载提示
    loadingMessage.close()
    
    // 停止计时器
    if (timer.value) {
      clearInterval(timer.value)
      timer.value = null
    }
    
    // 显示成功提示
    await ElMessageBox.alert('试卷提交成功！', '提交成功', {
      confirmButtonText: '查看结果',
      type: 'success'
    })
    
    // 跳转到结果页面或返回测评列表
    router.push({ name: 'Assessments' })
  } catch (error) {
    console.error('提交试卷失败:', error)
    
    // 重新显示提交确认对话框
    showSubmitDialog.value = true
    
    // 显示错误提示
    ElMessage.error(error.message || '提交失败，请重试')
  }
}

const startTimer = () => {
  timer.value = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    } else {
      // 时间到，自动提交
      clearInterval(timer.value)
      ElMessageBox.alert('考试时间已到，系统将自动提交试卷。', '时间到', {
        confirmButtonText: '确定',
        type: 'warning'
      }).then(() => {
        submitExam()
      })
    }
  }, 1000)
}

const toggleNav = () => {
  // 切换导航栏显示
  console.log('切换导航栏')
}

const getFileIcon = (type) => {
  const icons = {
    pdf: 'fas fa-file-pdf',
    doc: 'fas fa-file-word',
    image: 'fas fa-file-image',
    video: 'fas fa-file-video'
  }
  return icons[type] || 'fas fa-file'
}

// 防抖定时器
let autoSaveTimer = null

/**
 * 防抖自动保存函数
 * 避免用户快速输入时频繁调用API
 */
const debouncedAutoSave = () => {
  // 清除之前的定时器
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer)
  }
  
  // 设置新的定时器，2秒后执行自动保存
  autoSaveTimer = setTimeout(() => {
    autoSave()
  }, 2000)
}

// 监听答案变化，防抖自动保存
watch(answers, () => {
  debouncedAutoSave()
}, { deep: true })

// 监听题目切换，立即保存当前题目答案
watch(currentQuestionIndex, (newIndex, oldIndex) => {
  if (oldIndex !== undefined && oldIndex !== newIndex) {
    // 清除防抖定时器
    if (autoSaveTimer) {
      clearTimeout(autoSaveTimer)
      autoSaveTimer = null
    }
    // 立即保存答案
    autoSave()
  }
})

// 页面离开前确认并保存答案
const beforeUnload = (e) => {
  // 清除防抖定时器并立即保存答案
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer)
    autoSaveTimer = null
  }
  
  // 尝试保存当前答案（同步方式，因为页面即将关闭）
  try {
    const answerData = formatCurrentAnswerData()
    if (answerData.response && answerData.response.trim() !== '' && 
        answerData.response !== '[]' && answerData.response !== '[""]') {
      // 使用 navigator.sendBeacon 进行可靠的数据发送
      const apiUrl = `/api/student/assessments/${assessmentId}/answer`
      const data = JSON.stringify(answerData)
      navigator.sendBeacon(apiUrl, data)
    }
  } catch (error) {
    console.error('页面离开前保存答案失败:', error)
  }
  
  e.preventDefault()
  e.returnValue = ''
}

onMounted(async () => {
  // 由于从Assessments页面已经调用了startAssessment，直接加载试卷内容
  showStartConfirm.value = false
  await fetchPaperContent(assessmentId)
  startTimer()
  window.addEventListener('beforeunload', beforeUnload)
})

onUnmounted(() => {
  // 清理定时器
  if (timer.value) {
    clearInterval(timer.value)
  }
  
  // 清理自动保存定时器
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer)
    autoSaveTimer = null
  }
  
  // 移除事件监听器
  window.removeEventListener('beforeunload', beforeUnload)
})
</script>

<style scoped>
/* 严格遵循设计约束手册的新拟态设计规范 */
:root {
  /* 主色调 - 克莱因蓝和绿琉璃色 */
  --primary-color: #002FA7;
  --primary-light: rgba(0, 47, 167, 0.1);
  --primary-dark: #001f73;
  --secondary-color: #517B4D;
  
  /* 功能色彩 - 完全符合设计规范 */
  --success-color: #67C23A;
  --warning-color: #E6A23C;
  --danger-color: #F56C6C;
  --info-color: #909399;
  
  /* 新拟态背景色 */
  --bg-color: #F0F0F3;
  --card-bg: #F0F0F3;
  --bg-secondary: rgba(255, 255, 255, 0.8);
  
  /* 文字颜色 - 严格按照设计规范 */
  --text-primary: #303133;   /* 文字主色 */
  --text-secondary: #606266; /* 文字次色 */
  --text-auxiliary: #909399; /* 文字辅助色 */
  
  /* 边框颜色 */
  --border-color: #DCDFE6;
  --border-light: rgba(220, 223, 230, 0.5);
  
  /* 圆角 - 符合新拟态设计 */
  --border-radius: 8px;
  --border-radius-sm: 4px;
  --border-radius-lg: 12px;
  --border-radius-xl: 16px;
  
  /* 新拟态阴影 - 精确按照设计手册参数 */
  --neu-shadow-raised: 8px 8px 16px #d1d1d4, -8px -8px 16px #ffffff;
  --neu-shadow-inset: inset 8px 8px 16px #d1d1d4, inset -8px -8px 16px #ffffff;
  --neu-shadow-hover: 4px 4px 8px #d1d1d4, -4px -4px 8px #ffffff;
  
  /* 字体规范 - 基于设计约束手册 */
  --font-family-zh: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --font-family-en: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  --font-family-code: 'Consolas', 'Monaco', 'Courier New', monospace;
  
  /* 字体大小规范 */
  --font-size-title-xl: 24px;
  --font-size-title-lg: 20px;
  --font-size-title-md: 18px;
  --font-size-title-sm: 16px;
  --font-size-body: 14px;
  --font-size-caption: 12px;
  --font-size-small: 10px;
}

.exam-page {
  min-height: 100vh;
  background: linear-gradient(145deg, #f0f0f3 0%, #e8e8eb 100%);
  padding: 20px;
  font-family: var(--font-family-zh), var(--font-family-en);
  font-size: var(--font-size-body);
  color: var(--text-primary);
}

/* 新拟态卡片基础样式 */
.neu-card {
  background: var(--card-bg);
  box-shadow: var(--neu-shadow-raised);
  border-radius: var(--border-radius);
  transition: all 0.3s ease;
}

.neu-card:hover {
  box-shadow: var(--neu-shadow-hover);
  transform: translateY(-2px);
}

/* 磨砂玻璃效果 */
.glass-card {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--border-radius-lg);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

/* 新拟态按钮样式 */
.neu-button {
  background: var(--card-bg);
  box-shadow: var(--neu-shadow-raised);
  border: none;
  border-radius: var(--border-radius);
  padding: 12px 24px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(linear-gradient(135deg, #002FA7 0%, #517B4D 100%));
}

.neu-button-submit{
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  box-shadow: var(--neu-shadow-raised);
  padding: 12px 24px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.neu-button:hover {
  box-shadow: var(--neu-shadow-hover);
  transform: translateY(-1px);
}

.neu-button:active {
  box-shadow: var(--neu-shadow-inset);
  transform: translateY(0);
}

/* 新拟态输入框样式 */
.neu-input {
  background: var(--card-bg);
  box-shadow: var(--neu-shadow-inset);
  border: none;
  border-radius: var(--border-radius);
  padding: 12px 16px;
  font-size: 14px;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.neu-input:focus {
  outline: none;
  box-shadow: 
    inset 4px 4px 8px #d1d1d4,
    inset -4px -4px 8px #ffffff,
    0 0 0 2px rgba(0, 47, 167, 0.2);
}

/* 加载状态样式 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.loading-content {
  text-align: center;
  padding: 40px;
  background: var(--card-bg);
  box-shadow: var(--neu-shadow-raised);
  border-radius: var(--border-radius-lg);
}

.loading-icon {
  color: var(--primary-color);
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: var(--font-size-title-sm);
  color: var(--text-secondary);
  margin: 0;
  font-weight: 500;
  font-family: var(--font-family-zh), var(--font-family-en);
}

/* 考试头部样式 */
.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  margin-bottom: 24px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--border-radius-lg);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.exam-info {
  flex: 1;
}

.exam-info h1 {
  font-size: var(--font-size-title-xl);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 12px 0;
  letter-spacing: -0.5px;
  font-family: var(--font-family-zh), var(--font-family-en);
}

.exam-meta {
  display: flex;
  gap: 24px;
  color: var(--text-secondary);
  font-size: var(--font-size-body);
  flex-wrap: wrap;
}

.exam-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: var(--border-radius-sm);
  font-weight: 500;
}

.exam-meta i {
  color: var(--primary-color);
  font-size: 16px;
}

.exam-status {
  display: flex;
  gap: 20px;
}

.countdown-timer,
.progress-info {
  text-align: center;
  padding: 20px 24px;
  min-width: 140px;
  background: var(--card-bg);
  box-shadow: var(--neu-shadow-raised);
  border-radius: var(--border-radius);
  transition: all 0.3s ease;
}

.countdown-timer:hover,
.progress-info:hover {
  box-shadow: var(--neu-shadow-hover);
  transform: translateY(-2px);
}

.timer-display,
.progress-display {
  margin-bottom: 12px;
}

.time-number,
.progress-number {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary-color);
  display: block;
  letter-spacing: -0.5px;
}

.time-label,
.progress-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.time-progress,
.answer-progress {
  margin-top: 12px;
}

/* 考试内容区域 */
.exam-content {
  gap: 24px;
  height: calc(100vh - 220px);
}

/* 题目导航区域 */
.question-nav {
  width: 300px;
  /*height: 400px;*/
  padding: 24px;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--border-radius-lg);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(255, 255, 255, 0.3);
}

.nav-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
}

.nav-legend {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 24px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: var(--border-radius);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.legend-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  box-shadow: var(--neu-shadow-raised);
  transition: all 0.3s ease;
}

.legend-dot.answered {
  background: var(--success-color);
  border-color: var(--success-color);
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
  /*字体颜色*/
  color: var(linear-gradient(135deg, #002FA7 0%, #517B4D 100%));
}

.legend-dot.current {
  background: var(--primary-color);
  border-color: var(--primary-color);
  box-shadow: 0 2px 8px rgba(0, 47, 167, 0.3);
}

.legend-dot.unanswered {
  background: transparent;
  box-shadow: var(--neu-shadow-inset);
}

.legend-dot.marked {
  background: var(--warning-color);
  border-color: var(--warning-color);
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.3);
}

.question-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.question-nav-item {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(linear-gradient(135deg, #002FA7 0%, #517B4D 100%));
  box-shadow: var(--neu-shadow-raised);
  border-radius: var(--border-radius);
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  color: var(linear-gradient(135deg, #002FA7 0%, #517B4D 100%));
  border: none;
}

.question-nav-item:hover {
  box-shadow: var(--neu-shadow-hover);
  transform: translateY(-1px);
  color: var(--primary-color);
}

.question-nav-item.current {
  background: var(--primary-color);
  color: white;
  box-shadow: 0 4px 16px rgba(0, 47, 167, 0.3);
}

.question-nav-item.answered {
  background: var(--success-color);
  color: white;
  box-shadow: 0 4px 16px rgba(103, 194, 58, 0.3);
}

.question-nav-item.marked {
  position: relative;
}

.question-nav-item.marked::after {
  content: '';
  position: absolute;
  top: -3px;
  right: -3px;
  width: 10px;
  height: 10px;
  background: var(--warning-color);
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(230, 162, 60, 0.4);
}

.nav-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 题目内容区域 */
.question-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.question-card {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
  background: var(--card-bg);
  box-shadow: var(--neu-shadow-raised);
  border-radius: var(--border-radius-lg);
  transition: all 0.3s ease;
}

.question-card:hover {
  box-shadow: var(--neu-shadow-hover);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 2px solid rgba(0, 47, 167, 0.1);
  position: relative;
}

.question-header::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
  border-radius: 1px;
}

.question-number {
  display: flex;
  align-items: baseline;
  gap: 4px;
  padding: 8px 16px;
  background: rgba(0, 47, 167, 0.1);
  border-radius: var(--border-radius);
}

.question-number .number {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary-color);
  letter-spacing: -0.5px;
}

.question-number .total {
  font-size: var(--font-size-title-md);
  color: var(--text-secondary);
  font-weight: 500;
  font-family: var(--font-family-zh), var(--font-family-en);
}

.question-score {
  display: flex;
  align-items: baseline;
  gap: 4px;
  padding: 8px 16px;
  background: rgba(103, 194, 58, 0.1);
  border-radius: var(--border-radius);
}

.question-score .score {
  font-size: var(--font-size-title-lg);
  font-weight: 700;
  color: var(--success-color);
  letter-spacing: -0.5px;
  font-family: var(--font-family-zh), var(--font-family-en);
}

.question-score .unit {
  font-size: var(--font-size-body);
  color: var(--text-secondary);
  font-weight: 500;
  font-family: var(--font-family-zh), var(--font-family-en);
}

.question-text {
  font-size: var(--font-size-title-sm);
  line-height: 1.7;
  color: var(--text-primary);
  margin-bottom: 24px;
  font-weight: 400;
  letter-spacing: 0.2px;
  font-family: var(--font-family-zh), var(--font-family-en);
}

/* 题目附件 */
.question-attachments {
  margin-bottom: 24px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--card-bg);
  border-radius: var(--border-radius);
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--neu-shadow-inset);
  border: 1px solid rgba(0, 47, 167, 0.1);
}

.attachment-item:hover {
  background: rgba(0, 47, 167, 0.05);
  box-shadow: var(--neu-shadow-raised);
  transform: translateY(-1px);
}

.attachment-image {
  max-width: 100%;
  height: auto;
  border-radius: var(--border-radius);
  box-shadow: var(--neu-shadow-raised);
}

.attachment-file {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--primary-color);
  font-weight: 500;
}

.attachment-icon {
  color: var(--primary-color);
  font-size: 18px;
}

.attachment-name {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 500;
}

/* 题目选项 */
.question-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.option-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 5px;
  background: var(--card-bg);
  border: 2px solid transparent;
  border-radius: var(--border-radius-lg);
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--neu-shadow-inset);
  position: relative;
  overflow: hidden;
}

.option-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.02), rgba(103, 194, 58, 0.02));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.option-item:hover {
  border-color: rgba(0, 47, 167, 0.3);
  box-shadow: var(--neu-shadow-raised);
  transform: translateY(-2px);
}

.option-item:hover::before {
  opacity: 1;
}

.option-item.selected {
  background: rgba(0, 47, 167, 0.08);
  border-color: var(--primary-color);
  box-shadow: var(--neu-shadow-hover);
}

.option-item.selected::before {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.1), rgba(103, 194, 58, 0.05));
  opacity: 1;
}

.option-marker {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.option-label {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: var(--card-bg);
  border: 2px solid var(--border-color);
  border-radius: 50%;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-secondary);
  flex-shrink: 0;
  transition: all 0.3s ease;
  box-shadow: var(--neu-shadow-inset);
  position: relative;
  z-index: 1;
}

.option-item.selected .option-label {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
  box-shadow: var(--neu-shadow-raised);
  transform: scale(1.1);
}

.option-indicator i {
  color: var(--primary-color);
  font-size: 16px;
}

.option-content {
  flex: 1;
  font-size: 16px;
  line-height: 1.6;
  color: var(--text-primary);
  font-weight: 400;
  position: relative;
  z-index: 1;
}

.question-blanks {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.blank-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.blank-label {
  min-width: 80px;
  font-weight: 600;
  color: var(--text-primary);
  font-size: 15px;
}

.blank-input {
  flex: 1;
  max-width: 350px;
  padding: 12px 16px;
  border: 2px solid transparent;
  border-bottom: 3px solid var(--primary-color);
  background: rgba(0, 47, 167, 0.05);
  font-size: 16px;
  color: var(--text-primary);
  outline: none;
  transition: all 0.3s ease;
  border-radius: 8px 8px 0 0;
  font-weight: 500;
  box-shadow: var(--neu-shadow-inset);
}

.blank-input:focus {
  border-bottom-color: var(--secondary-color);
  background: rgba(0, 47, 167, 0.1);
  box-shadow: var(--neu-shadow-raised);
  transform: translateY(-1px);
}

.question-essay {
  margin-top: 15px;
}

.essay-input {
  width: 100%;
  min-height: 240px;
  padding: 20px;
  border: 2px solid transparent;
  border-radius: var(--border-radius-lg);
  font-size: 15px;
  line-height: 1.6;
  color: var(--text-primary);
  background: var(--card-bg);
  resize: vertical;
  outline: none;
  transition: all 0.3s ease;
  box-shadow: var(--neu-shadow-inset);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.essay-input:focus {
  border-color: var(--primary-color);
  box-shadow: var(--neu-shadow-raised);
  background: rgba(0, 47, 167, 0.02);
}

.question-coding {
  margin-top: 15px;
}

.coding-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  padding: 12px 16px;
  background: rgba(0, 47, 167, 0.05);
  border-radius: var(--border-radius);
}

.language-label {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 15px;
}

.language-select {
  width: 180px;
}

.coding-input {
  width: 100%;
  min-height: 280px;
  padding: 20px;
  border: 2px solid transparent;
  border-radius: var(--border-radius-lg);
  font-size: 14px;
  line-height: 1.5;
  color: #e8e8e8;
  background: var(linear-gradient(135deg, #002FA7 0%, #517B4D 100%));
  resize: vertical;
  outline: none;
  transition: all 0.3s ease;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.3);
  font-family: 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
}

.coding-input:focus {
  border-color: var(--primary-color);
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.3), 0 0 0 3px rgba(0, 47, 167, 0.2);
}

.question-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
  border-top: 2px solid rgba(0, 47, 167, 0.1);
  margin-top: 32px;
  position: relative;
}

.question-navigation::before {
  content: '';
  position: absolute;
  top: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 2px;
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
  border-radius: 1px;
}

.nav-center {
  display: flex;
  gap: 15px;
}

.nav-btn {
  min-width: 120px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 28px;
  background: var(--card-bg);
  border: 2px solid transparent;
  border-radius: var(--border-radius-lg);
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--neu-shadow-raised);
  position: relative;
  overflow: hidden;
}

.nav-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.nav-btn:hover {
  border-color: var(--primary-color);
  box-shadow: var(--neu-shadow-hover);
  transform: translateY(-2px);
}

.nav-btn:hover::before {
  left: 100%;
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: var(--neu-shadow-inset);
}

.nav-btn:disabled:hover {
  border-color: transparent;
  transform: none;
  box-shadow: var(--neu-shadow-inset);
}

/* 提交总结 */
.submit-summary {
  padding: 40px;
  background: var(--card-bg);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--neu-shadow-raised);
  border: 1px solid rgba(0, 47, 167, 0.1);
  position: relative;
  overflow: hidden;
  margin-bottom: 24px;
}

.submit-summary::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(0, 47, 167, 0.1);
  background: rgba(255, 255, 255, 0.5);
  border-radius: var(--border-radius);
  margin-bottom: 12px;
  transition: all 0.3s ease;
}

.summary-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.summary-item:hover {
  background: rgba(0, 47, 167, 0.05);
  transform: translateX(4px);
}

.summary-item .label {
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 15px;
}

.summary-item .value {
  font-weight: 700;
  font-size: 16px;
  padding: 6px 12px;
  border-radius: var(--border-radius-sm);
  background: rgba(255, 255, 255, 0.8);
}

.summary-item .value.answered {
  color: var(--success-color);
  background: rgba(103, 194, 58, 0.1);
}

.summary-item .value.unanswered {
  color: var(--danger-color);
  background: rgba(245, 108, 108, 0.1);
}

.summary-item .value.marked {
  color: var(--warning-color);
  background: rgba(230, 162, 60, 0.1);
}

.summary-item .value.time {
  color: var(--primary-color);
  background: rgba(0, 47, 167, 0.1);
}

/* 警告消息 */
.submit-warning {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 193, 7, 0.1);
  border: 2px solid #ffc107;
  border-radius: var(--border-radius-lg);
  color: #856404;
  margin-bottom: 24px;
  box-shadow: var(--neu-shadow-inset);
  position: relative;
  overflow: hidden;
}

.submit-warning::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: #ffc107;
}

.submit-warning i {
  color: #ffc107;
  font-size: 24px;
  animation: pulse 2s infinite;
}

.submit-warning p {
  margin: 0;
  line-height: 1.6;
  font-weight: 600;
  font-size: 15px;
}

/* 自动保存提示 */
.auto-save-tip {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: rgba(103, 194, 58, 0.1);
  border: 2px solid var(--success-color);
  border-radius: var(--border-radius-lg);
  color: var(--success-color);
  font-size: 15px;
  font-weight: 600;
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 1000;
  box-shadow: var(--neu-shadow-raised);
  animation: slideIn 0.4s ease;
  backdrop-filter: blur(10px);
}

.auto-save-tip i {
  animation: spin 1s linear infinite;
  font-size: 18px;
}

/* 动画效果 */
@keyframes slideIn {
  from {
    transform: translateX(100%) scale(0.8);
    opacity: 0;
  }
  to {
    transform: translateX(0) scale(1);
    opacity: 1;
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .exam-page {
    padding: 20px;
  }
  
  .exam-header {
    padding: 24px;
  }
  
  .question-card {
    padding: 28px;
  }
}

@media (max-width: 1200px) {
  .exam-page {
    padding: 16px;
  }
  
  .exam-header {
    flex-direction: column;
    gap: 20px;
    padding: 20px;
  }
  
  .exam-status {
    justify-content: center;
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .exam-meta {
    flex-direction: column;
    align-items: center;
    gap: 12px;
  }
  
  .exam-content {
    flex-direction: column;
    height: auto;
    gap: 20px;
  }
  
  .question-nav {
    width: 100%;
    height: auto;
    max-height: 240px;
    padding: 20px;
    order: 2;
  }
  
  .question-content {
    order: 1;
  }
  
  .question-grid {
    grid-template-columns: repeat(8, 1fr);
    gap: 12px;
  }
  
  .summary-stats {
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .exam-page {
    padding: 12px;
  }
  
  .exam-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
    padding: 16px;
    border-radius: var(--border-radius);
  }
  
  .exam-title {
    font-size: 22px;
    font-weight: 700;
  }
  
  .exam-status {
    justify-content: center;
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .timer-display {
    font-size: 18px;
    padding: 12px 16px;
  }
  
  .progress-info {
    font-size: 14px;
    padding: 10px 14px;
  }
  
  .exam-meta {
    flex-direction: column;
    gap: 10px;
    align-items: center;
  }
  
  .question-card {
    padding: 20px;
    border-radius: var(--border-radius);
  }
  
  .question-number .number {
    font-size: 24px;
  }
  
  .question-text {
    font-size: 16px;
    line-height: 1.6;
  }
  
  .option-item {
    padding: 16px;
    gap: 12px;
  }
  
  .option-label {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }
  
  .question-grid {
    grid-template-columns: repeat(6, 1fr);
    gap: 10px;
  }
  
  .nav-item {
    width: 40px;
    height: 40px;
    font-size: 14px;
  }
  
  .question-navigation {
    flex-direction: column;
    gap: 16px;
    padding: 16px 0;
  }
  
  .nav-center {
    order: -1;
    gap: 12px;
  }
  
  .nav-btn {
    padding: 12px 20px;
    font-size: 14px;
    min-width: 100px;
  }
  
  .summary-stats {
    grid-template-columns: 1fr 1fr;
    gap: 16px;
  }
  
  .stat-value {
    font-size: 28px;
  }
  
  .auto-save-tip {
    bottom: 16px;
    right: 16px;
    padding: 12px 16px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .exam-page {
    padding: 8px;
  }
  
  .exam-header {
    padding: 12px;
  }
  
  .exam-title {
    font-size: 20px;
  }
  
  .timer-display {
    font-size: 16px;
    padding: 10px 12px;
  }
  
  .progress-info {
    font-size: 13px;
    padding: 8px 12px;
  }
  
  .question-card {
    padding: 16px;
  }
  
  .question-number .number {
    font-size: 20px;
  }
  
  .question-text {
    font-size: 15px;
  }
  
  .option-item {
    padding: 14px;
    gap: 10px;
  }
  
  .option-label {
    width: 24px;
    height: 24px;
    font-size: 12px;
  }
  
  .question-grid {
    grid-template-columns: repeat(5, 1fr);
    gap: 8px;
  }
  
  .nav-item {
    width: 35px;
    height: 35px;
    font-size: 12px;
  }
  
  .question-navigation {
    gap: 12px;
  }
  
  .nav-btn {
    width: 100%;
    justify-content: center;
    padding: 14px 20px;
  }
  
  .summary-stats {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .stat-item {
    padding: 16px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .submission-summary {
    padding: 20px;
  }
  
  .summary-title {
    font-size: 22px;
  }
  
  .auto-save-tip {
    bottom: 12px;
    right: 12px;
    left: 12px;
    padding: 12px;
    font-size: 13px;
  }
  
  .blank-input {
    max-width: 100%;
  }
  
  .essay-answer, .coding-answer, .essay-input, .coding-input {
    min-height: 180px;
    padding: 16px;
    font-size: 14px;
  }
}
</style>