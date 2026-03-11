<template>
  <div class="exam-page">
    <!-- 背景装饰 -->
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>
    
    <!-- 加载状态 -->
    <div v-if="loading || showStartConfirm" class="loading-container glass-card">
      <div v-if="loading" class="loading-content">
        <el-icon class="loading-icon" size="48">
          <Loading />
        </el-icon>
        <p class="loading-text">正在加载试卷内容...</p>
      </div>
    </div>
    
    <!-- 考试内容 -->
    <div v-else-if="!loading && !showStartConfirm && questions.length > 0" class="exam-container">
      
      <!-- 顶部信息栏 (Sticky) -->
      <header class="exam-header glass-card">
        <div class="header-left">
          <h1 class="exam-title">{{ examInfo.title }}</h1>
          <div class="exam-meta">
            <el-tag effect="dark" round color="#002FA7" style="border: none;">
              <i class="fas fa-question-circle"></i> {{ examInfo.totalQuestions }} 题
            </el-tag>
            <el-tag effect="dark" round color="#517B4D" style="border: none;">
              <i class="fas fa-star"></i> 总分 {{ examInfo.totalScore }} 分
            </el-tag>
          </div>
        </div>
        
        <div class="header-right">
          <!-- 倒计时 -->
          <div class="timer-card neu-inset">
            <span class="timer-icon"><i class="far fa-clock"></i></span>
            <span class="timer-text" :class="{ 'timer-warning': remainingTime < 300 }">
              {{ formatTime(remainingTime) }}
            </span>
          </div>
          
          <!-- 进度 -->
          <div class="progress-card">
            <span class="progress-text">已答 {{ answeredCount }}/{{ examInfo.totalQuestions }}</span>
            <el-progress 
              :percentage="answerProgress" 
              :stroke-width="8" 
              :show-text="false"
              color="#002FA7"
              class="custom-progress"
            />
          </div>

          <el-button type="danger" round class="submit-btn neu-btn-danger" @click="showSubmitDialog = true">
            <i class="fas fa-paper-plane"></i> 交卷
          </el-button>
        </div>
      </header>

      <div class="exam-main-layout">
        <!-- 左侧：题目导航 -->
        <aside class="question-nav glass-card" :class="{ 'collapsed': isNavCollapsed }">
          <div class="nav-header">
            <h3>答题卡</h3>
            <button class="toggle-nav-btn neu-btn-icon" @click="toggleNav">
              <i :class="isNavCollapsed ? 'fas fa-chevron-right' : 'fas fa-chevron-left'"></i>
            </button>
          </div>
          
          <div class="nav-legend">
            <div class="legend-item"><span class="dot current"></span>当前</div>
            <div class="legend-item"><span class="dot answered"></span>已答</div>
            <div class="legend-item"><span class="dot unanswered"></span>未答</div>
            <div class="legend-item"><span class="dot marked"></span>标记</div>
          </div>
          
          <div class="question-grid-wrapper">
            <div class="question-grid">
              <button 
                v-for="(question, index) in questions" 
                :key="question.id"
                class="nav-item neu-btn-sm"
                :class="getQuestionNavClass(question, index)"
                @click="goToQuestion(index)"
              >
                {{ index + 1 }}
                <span v-if="question.marked" class="mark-indicator"></span>
              </button>
            </div>
          </div>
        </aside>

        <!-- 右侧：题目内容 -->
        <main class="question-content-area">
          <div class="question-card glass-card">
            <!-- 题目头部 -->
            <div class="q-header">
              <div class="q-type-badge">
                <span class="q-number">Q{{ currentQuestionIndex + 1 }}</span>
                <span class="q-type">{{ getQuestionTypeName(currentQuestion.type) }}</span>
              </div>
              <div class="q-actions">
                <span class="q-score">({{ currentQuestion.score }}分)</span>
                <button 
                  class="mark-btn neu-btn-icon" 
                  :class="{ active: currentQuestion.marked }"
                  @click="markQuestion"
                  title="标记题目"
                >
                  <i class="fas fa-bookmark"></i>
                </button>
              </div>
            </div>

            <!-- 题目题干 (Markdown渲染) -->
            <div class="q-body">
              <div class="markdown-question-content">
                <MarkdownRender 
                  :content="currentQuestion.content" 
                  :hide-actions="true" 
                />
              </div>
              
              <!-- 附件 -->
              <div v-if="currentQuestion.attachments && currentQuestion.attachments.length" class="q-attachments">
                <div v-for="attachment in currentQuestion.attachments" :key="attachment.id" class="attachment-item neu-card">
                  <img v-if="attachment.type === 'image'" :src="attachment.url" :alt="attachment.name" />
                  <a v-else :href="attachment.url" target="_blank" class="file-link">
                    <i :class="getFileIcon(attachment.type)"></i> {{ attachment.name }}
                  </a>
                </div>
              </div>
            </div>

            <!-- 答题区域 -->
            <div class="q-answer-area">
              
              <!-- 选择题 (单选/多选/判断) -->
              <div v-if="isChoiceQuestion(currentQuestion.type)" class="options-list">
                <div 
                  v-for="option in currentQuestion.options" 
                  :key="option.id"
                  class="option-item neu-card-interactive"
                  :class="{ selected: isOptionSelected(option.id) }"
                  @click="selectOption(option.id)"
                >
                  <div class="option-prefix">{{ option.label }}</div>
                  <div class="option-text">{{ option.content }}</div>
                  <div class="option-check">
                    <i class="fas fa-check" v-if="isOptionSelected(option.id)"></i>
                  </div>
                </div>
              </div>

              <!-- 填空题 -->
              <div v-else-if="currentQuestion.type === 'fill_blank'" class="blanks-list">
                <div v-for="(blank, index) in currentQuestion.blanks" :key="index" class="blank-item">
                  <span class="blank-index">{{ index + 1 }}</span>
                  <el-input 
                    v-model="answers[currentQuestion.id].blanks[index]"
                    placeholder="请输入答案"
                    class="neu-input"
                  />
                </div>
              </div>

              <!-- 简答题 -->
              <div v-else-if="currentQuestion.type === 'essay'" class="essay-area">
                <el-input 
                  v-model="answers[currentQuestion.id].content"
                  type="textarea"
                  :rows="10"
                  placeholder="在此输入您的回答..."
                  class="neu-textarea"
                  show-word-limit
                  :maxlength="currentQuestion.maxLength || 2000"
                />
              </div>

              <!-- 编程题 -->
              <div v-else-if="currentQuestion.type === 'coding'" class="coding-area">
                <div class="coding-toolbar glass-card-sm">
                  <span class="label">编程语言:</span>
                  <el-select v-model="answers[currentQuestion.id].language" class="neu-select" size="small">
                    <el-option v-for="lang in currentQuestion.languages" :key="lang" :label="lang" :value="lang" />
                  </el-select>
                </div>
                <el-input 
                  v-model="answers[currentQuestion.id].code"
                  type="textarea"
                  :rows="15"
                  placeholder="// 在此编写代码..."
                  class="neu-code-editor"
                  spellcheck="false"
                />
              </div>

            </div>

            <!-- 底部导航按钮 -->
            <div class="q-footer">
              <el-button 
                @click="previousQuestion" 
                :disabled="currentQuestionIndex === 0"
                class="nav-btn neu-btn"
                round
              >
                <i class="fas fa-arrow-left"></i> 上一题
              </el-button>
              
              <el-button 
                @click="saveAnswer"
                class="save-btn neu-btn-primary"
                round
                :loading="isSaving"
              >
                <i class="fas fa-save"></i> 保存本题
              </el-button>
              
              <el-button 
                @click="nextQuestion" 
                :disabled="currentQuestionIndex === questions.length - 1"
                class="nav-btn neu-btn"
                round
              >
                下一题 <i class="fas fa-arrow-right"></i>
              </el-button>
            </div>

          </div>
        </main>
      </div>

      <!-- 自动保存提示 -->
      <transition name="fade">
        <div v-if="showAutoSave" class="auto-save-toast glass-card">
          <i class="fas fa-check-circle"></i> 答案已自动保存
        </div>
      </transition>
    </div>

    <!-- 提交确认弹窗 -->
    <el-dialog 
      v-model="showSubmitDialog" 
      title="确认提交试卷" 
      width="400px"
      class="neu-dialog"
      align-center
    >
      <div class="submit-summary">
        <div class="summary-stat">
          <div class="stat-item">
            <span class="value">{{ answeredCount }}</span>
            <span class="label">已答</span>
          </div>
          <div class="stat-item warning">
            <span class="value">{{ questions.length - answeredCount }}</span>
            <span class="label">未答</span>
          </div>
        </div>
        <p class="warning-text">
          <i class="fas fa-exclamation-triangle"></i>
          提交后将无法修改答案，请确认所有题目都已完成。
        </p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showSubmitDialog = false" class="neu-btn">再检查一下</el-button>
          <el-button type="primary" @click="submitExam" :loading="isSubmitting" class="neu-btn-primary">
            确认提交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getPaperContent, submitAnswer, submitAssessment } from '@/api/students/stuAPI.js'
import MarkdownRender from '@/components/markdownRender/markstream-vue_Index.vue'

const route = useRoute()
const router = useRouter()
const assessmentId = route.params.assessmentId

// 状态
const loading = ref(false)
const showStartConfirm = ref(true)
const isNavCollapsed = ref(false)
const isSubmitting = ref(false)
const isSaving = ref(false)
const showSubmitDialog = ref(false)
const showAutoSave = ref(false)
const timer = ref(null)

// 数据
const paperData = ref(null)
const examInfo = ref({
  id: assessmentId,
  title: '',
  description: '',
  totalScore: 0,
  totalQuestions: 0,
  duration: 0
})
const questions = ref([])
const answers = ref({})
const currentQuestionIndex = ref(0)
const remainingTime = ref(0)

// 计算属性
const currentQuestion = computed(() => questions.value[currentQuestionIndex.value] || {})

const answeredCount = computed(() => {
  return questions.value.filter(question => {
    const answer = answers.value[question.id]
    if (!answer) return false
    
    switch (question.type) {
      case 'single_choice':
      case 'multiple_choice':
      case 'true_false':
        return answer.selectedOptions && answer.selectedOptions.length > 0
      case 'fill_blank':
        return answer.blanks && answer.blanks.some(blank => blank.trim() !== '')
      case 'essay':
        return answer.content && answer.content.trim() !== ''
      case 'coding':
        return answer.code && answer.code.trim() !== ''
      default:
        return false
    }
  }).length
})

const answerProgress = computed(() => {
  if (questions.value.length === 0) return 0
  return Math.round((answeredCount.value / questions.value.length) * 100)
})

// 生命周期
onMounted(() => {
  // 禁止复制
  document.addEventListener('copy', handleCopy)
  document.addEventListener('contextmenu', handleContextMenu)
  
  // 确认开始
  confirmStartExam()
})

onUnmounted(() => {
  document.removeEventListener('copy', handleCopy)
  document.removeEventListener('contextmenu', handleContextMenu)
  if (timer.value) clearInterval(timer.value)
})

// 事件处理
const handleCopy = (e) => {
  e.preventDefault()
  ElMessage.warning('考试期间禁止复制内容！此行为已被记录。')
}

const handleContextMenu = (e) => {
  e.preventDefault()
}

// 核心逻辑
const fetchPaperContent = async (assessmentId) => {
  try {
    loading.value = true
    const response = await getPaperContent(assessmentId)
    paperData.value = response

    examInfo.value = {
      id: assessmentId,
      title: response.title || '在线考试',
      description: response.description || '',
      totalScore: response.totalScore || 100,
      totalQuestions: response.sections?.reduce((total, section) =>
          total + (section.questions?.length || 0), 0) || 0,
      duration: response.durationMinutes || 60
    }

    remainingTime.value = examInfo.value.duration * 60
    convertPaperData(response)
    
  } catch (error) {
    ElMessage.error('获取试卷失败：' + (error.message || '未知错误'))
    router.back()
  } finally {
    loading.value = false
  }
}

const convertPaperData = (paperData) => {
  const convertedQuestions = []
  
  if (paperData.sections && Array.isArray(paperData.sections)) {
    paperData.sections.forEach(section => {
      if (section.questions && Array.isArray(section.questions)) {
        section.questions.forEach(paperQuestion => {
          const qd = paperQuestion.questionDetails
          if (qd) {
            const frontendType = mapQuestionType(qd.type)
            
            const q = {
              id: qd.id,
              type: frontendType,
              content: qd.stem,
              score: paperQuestion.score || 0,
              marked: false,
              sectionId: section.id
            }
            
            // 处理选项和特定字段
            switch (frontendType) {
              case 'single_choice':
              case 'multiple_choice':
                q.options = qd.options ? qd.options.map(o => ({
                  id: o.key,
                  label: o.key,
                  content: o.text
                })) : []
                break
              case 'true_false':
                // 判断题：A=正确, B=错误
                q.options = [
                  { id: 'A', label: 'A', content: '正确' },
                  { id: 'B', label: 'B', content: '错误' }
                ]
                break
              case 'fill_blank':
                const blankCount = (qd.stem.match(/___/g) || []).length
                q.blanks = new Array(Math.max(1, blankCount)).fill('')
                break
              case 'essay':
                q.maxLength = qd.maxLength || 2000
                break
              case 'coding':
                q.languages = qd.languages || ['Java', 'JavaScript', 'Python', 'C++']
                break
            }
            
            convertedQuestions.push(q)
          }
        })
      }
    })
  }
  
  questions.value = convertedQuestions
  initAnswers()
}

const mapQuestionType = (backendType) => {
  const map = {
    'SINGLE_CHOICE': 'single_choice',
    'MULTI_CHOICE': 'multiple_choice',
    'TRUE_FALSE': 'true_false',
    'FILL_IN_BLANKS': 'fill_blank',
    'SHORT_ANSWER': 'essay',
    'PROGRAMMING': 'coding'
  }
  return map[backendType] || backendType.toLowerCase()
}

const initAnswers = () => {
  questions.value.forEach(q => {
    answers.value[q.id] = {
      questionId: q.id,
      type: q.type,
      selectedOptions: [],
      blanks: q.type === 'fill_blank' ? new Array(q.blanks.length).fill('') : [],
      content: '',
      code: '',
      language: q.languages ? q.languages[0] : 'Java'
    }
  })
}

const confirmStartExam = async () => {
  try {
    await ElMessageBox.confirm('是否确认开始考试？计时将立即开始。', '准备就绪', {
      confirmButtonText: '开始答题',
      cancelButtonText: '稍后',
      type: 'info',
      center: true
    })
    showStartConfirm.value = false
    await fetchPaperContent(assessmentId)
    startTimer()
  } catch (e) {
    router.back()
  }
}

const startTimer = () => {
  timer.value = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    } else {
      clearInterval(timer.value)
      submitExam(true) // 强制提交
    }
  }, 1000)
}

const formatTime = (seconds) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h > 0 ? h + ':' : ''}${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

// 导航与操作
const toggleNav = () => isNavCollapsed.value = !isNavCollapsed.value

const getQuestionNavClass = (question, index) => {
  const answer = answers.value[question.id]
  let isAnswered = false
  if (answer) {
    if (['single_choice', 'multiple_choice', 'true_false'].includes(question.type)) {
      isAnswered = answer.selectedOptions.length > 0
    } else if (question.type === 'fill_blank') {
      isAnswered = answer.blanks.some(b => b.trim() !== '')
    } else if (question.type === 'essay') {
      isAnswered = answer.content.trim() !== ''
    } else if (question.type === 'coding') {
      isAnswered = answer.code.trim() !== ''
    }
  }
  
  return {
    current: index === currentQuestionIndex.value,
    answered: isAnswered,
    unanswered: !isAnswered && index !== currentQuestionIndex.value, // Only show unanswered if not current
    marked: question.marked
  }
}

const goToQuestion = (index) => currentQuestionIndex.value = index
const previousQuestion = () => { if (currentQuestionIndex.value > 0) currentQuestionIndex.value-- }
const nextQuestion = () => { if (currentQuestionIndex.value < questions.value.length - 1) currentQuestionIndex.value++ }
const markQuestion = () => currentQuestion.value.marked = !currentQuestion.value.marked

// 答题逻辑
const isOptionSelected = (id) => {
  const ans = answers.value[currentQuestion.value.id]
  return ans && ans.selectedOptions.includes(id)
}

const selectOption = (id) => {
  const q = currentQuestion.value
  const ans = answers.value[q.id]
  if (!ans) return
  
  if (q.type === 'single_choice' || q.type === 'true_false') {
    ans.selectedOptions = [id]
  } else {
    const idx = ans.selectedOptions.indexOf(id)
    if (idx > -1) ans.selectedOptions.splice(idx, 1)
    else ans.selectedOptions.push(id)
  }
  autoSave(q.id)
}

// 保存与提交
const saveAnswer = async () => {
  isSaving.value = true
  try {
    await submitCurrentAnswer()
    ElMessage.success('保存成功')
  } catch (e) {
    // Silent
  } finally {
    isSaving.value = false
  }
}

let autoSaveTimer = null
const autoSave = (questionId = currentQuestion.value?.id) => {
  if (!questionId) return
  if (autoSaveTimer) clearTimeout(autoSaveTimer)
  autoSaveTimer = setTimeout(async () => {
    try {
      await submitAnswerByQuestionId(questionId)
      showAutoSave.value = true
      setTimeout(() => showAutoSave.value = false, 2000)
    } catch (e) {}
  }, 2000)
}

const buildResponseByType = (q, ans) => {
  let response = ''
  if (['single_choice', 'true_false'].includes(q.type)) response = ans.selectedOptions[0] || ''
  else if (q.type === 'multiple_choice') response = JSON.stringify(ans.selectedOptions)
  else if (q.type === 'fill_blank') response = JSON.stringify(ans.blanks)
  else if (q.type === 'essay') response = ans.content
  else if (q.type === 'coding') response = JSON.stringify({ code: ans.code, language: ans.language })
  return response
}

const isSkippableResponse = (response) => !response || response === '[]' || response === '""'

const submitAnswerByQuestionId = async (questionId) => {
  const q = questions.value.find(item => item.id === questionId)
  if (!q) return
  const ans = answers.value[questionId]
  if (!ans) return

  const response = buildResponseByType(q, ans)
  if (isSkippableResponse(response)) return

  await submitAnswer(assessmentId, {
    questionId,
    response
  })
}

const submitCurrentAnswer = async () => {
  const questionId = currentQuestion.value?.id
  if (!questionId) return
  await submitAnswerByQuestionId(questionId)
}

const submitAllAnsweredQuestions = async () => {
  const submitTasks = questions.value.map((q) => submitAnswerByQuestionId(q.id))
  const results = await Promise.allSettled(submitTasks)
  const failedCount = results.filter(item => item.status === 'rejected').length
  if (failedCount > 0) {
    throw new Error(`有 ${failedCount} 道题保存失败，请稍后重试提交`)
  }
}

const submitExam = async (force = false) => {
  if (isSubmitting.value) return
  isSubmitting.value = true
  
  try {
    if (autoSaveTimer) {
      clearTimeout(autoSaveTimer)
      autoSaveTimer = null
    }

    if (!force) {
      // 提交前保存当前题目
      await submitCurrentAnswer()
    }

    await submitAllAnsweredQuestions()
    
    await submitAssessment(assessmentId)
    ElMessage.success('试卷提交成功！')
    router.push({ name: 'Assessments', query: { from: 'exam_submit' } })
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    isSubmitting.value = false
    showSubmitDialog.value = false
  }
}

// 辅助
const getQuestionTypeName = (type) => {
  const map = {
    single_choice: '单选题',
    multiple_choice: '多选题',
    true_false: '判断题',
    fill_blank: '填空题',
    essay: '简答题',
    coding: '编程题'
  }
  return map[type] || '题目'
}

const getFileIcon = (type) => 'fas fa-file'
const isChoiceQuestion = (type) => ['single_choice', 'multiple_choice', 'true_false'].includes(type)

// 监听内容变化以触发自动保存 (针对非选择题)
watch(() => answers.value[currentQuestion.value?.id], (newVal) => {
  if (newVal && !isChoiceQuestion(newVal.type)) {
    autoSave(currentQuestion.value?.id)
  }
}, { deep: true })

</script>

<style scoped>
/* =========================================
   1. 核心变量 (Design System)
   ========================================= */
:root {
  --primary-color: #002FA7; /* Klein Blue */
  --secondary-color: #517B4D; /* Green Glaze */
  --bg-color: #F0F0F3;
  --text-main: #303133;
  --text-light: #606266;
  --glass-bg: rgba(255, 255, 255, 0.65);
  --glass-border: 1px solid rgba(255, 255, 255, 0.4);
  --shadow-light: -8px -8px 16px #ffffff;
  --shadow-dark: 8px 8px 16px #d1d1d4;
  --radius-lg: 24px;
  --radius-md: 16px;
  --radius-sm: 12px;
}

.exam-page {
  min-height: 100vh;
  background-color: #F0F0F3;
  color: #303133;
  font-family: 'Inter', 'PingFang SC', sans-serif;
  position: relative;
  overflow-x: hidden;
  padding: 20px;
  box-sizing: border-box;
}

/* 背景装饰 */
.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
}
.shape-1 {
  width: 400px;
  height: 400px;
  background: rgba(0, 47, 167, 0.1);
  top: -100px;
  left: -100px;
}
.shape-2 {
  width: 300px;
  height: 300px;
  background: rgba(81, 123, 77, 0.1);
  bottom: -50px;
  right: -50px;
}

/* =========================================
   2. 通用样式类 (Neumorphism & Glass)
   ========================================= */
.glass-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.07);
}

.glass-card-sm {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 8px 12px;
}

.neu-card {
  background: #F0F0F3;
  box-shadow: 8px 8px 16px #d1d1d4, -8px -8px 16px #ffffff;
  border-radius: 16px;
}

.neu-inset {
  background: #F0F0F3;
  box-shadow: inset 6px 6px 12px #d1d1d4, inset -6px -6px 12px #ffffff;
  border-radius: 12px;
}

.neu-btn {
  background: #F0F0F3;
  border: none;
  box-shadow: 6px 6px 12px #d1d1d4, -6px -6px 12px #ffffff;
  color: #606266;
  transition: all 0.2s ease;
}
.neu-btn:hover {
  transform: translateY(-2px);
  color: #002FA7;
}
.neu-btn:active {
  box-shadow: inset 4px 4px 8px #d1d1d4, inset -4px -4px 8px #ffffff;
  transform: translateY(0);
}

.neu-btn-primary {
  background: #002FA7;
  color: #fff;
  box-shadow: 4px 4px 10px rgba(0, 47, 167, 0.3);
}
.neu-btn-primary:hover {
  background: #003ccf;
  transform: translateY(-2px);
}

.neu-btn-danger {
  background: #F0F0F3;
  color: #F56C6C;
  box-shadow: 6px 6px 12px #d1d1d4, -6px -6px 12px #ffffff;
}
.neu-btn-danger:hover {
  color: #ff4d4f;
  box-shadow: inset 4px 4px 8px #d1d1d4, inset -4px -4px 8px #ffffff;
}

.neu-btn-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: #F0F0F3;
  box-shadow: 5px 5px 10px #d1d1d4, -5px -5px 10px #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #606266;
  transition: all 0.2s;
}
.neu-btn-icon:hover {
  color: #002FA7;
  transform: scale(1.05);
}
.neu-btn-icon.active {
  color: #F56C6C;
  box-shadow: inset 3px 3px 6px #d1d1d4, inset -3px -3px 6px #ffffff;
}

/* =========================================
   3. 布局样式
   ========================================= */
.exam-container {
  max-width: 1600px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 40px);
  gap: 20px;
}

/* Header */
.exam-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 30px;
}

.header-left .exam-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #002FA7;
  margin: 0 0 8px 0;
}

.exam-meta {
  display: flex;
  gap: 10px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.timer-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 20px;
  color: #303133;
  font-weight: 600;
  font-size: 1.2rem;
  font-family: 'Courier New', Courier, monospace;
}
.timer-warning {
  color: #F56C6C;
  animation: pulse 1s infinite;
}

.progress-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 180px;
}
.progress-text {
  font-size: 0.85rem;
  color: #606266;
  text-align: right;
}

/* Main Layout */
.exam-main-layout {
  display: flex;
  gap: 24px;
  flex: 1;
  overflow: hidden; /* 内部滚动 */
}

/* Sidebar (Nav) */
.question-nav {
  width: 280px;
  display: flex;
  flex-direction: column;
  padding: 20px;
  transition: width 0.3s ease;
}
.question-nav.collapsed {
  width: 60px;
  padding: 20px 10px;
}
.question-nav.collapsed .nav-header h3,
.question-nav.collapsed .nav-legend,
.question-nav.collapsed .question-grid-wrapper {
  display: none;
}
.question-nav.collapsed .toggle-nav-btn {
  margin: 0 auto;
}

.nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.nav-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #303133;
}

.nav-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  font-size: 0.85rem;
  color: #606266;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.dot.current { background: #002FA7; box-shadow: 0 0 4px #002FA7; }
.dot.answered { background: #67C23A; }
.dot.unanswered { background: #E4E7ED; border: 1px solid #DCDFE6; }
.dot.marked { background: #E6A23C; }

.question-grid-wrapper {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}
.question-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
}

.nav-item {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8px;
  border: none;
  background: #F0F0F3;
  box-shadow: 3px 3px 6px #d1d1d4, -3px -3px 6px #ffffff;
  cursor: pointer;
  font-size: 0.9rem;
  color: #606266;
  position: relative;
  transition: all 0.2s;
}
.nav-item:hover {
  transform: translateY(-2px);
  color: #002FA7;
}
.nav-item.current {
  background: #002FA7;
  color: #fff;
  box-shadow: inset 2px 2px 5px rgba(0,0,0,0.2);
}
.nav-item.answered {
  background: #f0f9eb;
  color: #67C23A;
  border: 1px solid #c2e7b0;
  box-shadow: none;
}
.nav-item.marked::after {
  content: '';
  position: absolute;
  top: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  background: #E6A23C;
  border-radius: 50%;
  border: 1px solid #fff;
}

/* Question Content */
.question-content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.question-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 30px 40px;
  overflow-y: auto;
}

.q-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
}

.q-type-badge {
  display: flex;
  align-items: center;
  gap: 12px;
}
.q-number {
  font-size: 1.8rem;
  font-weight: 800;
  color: #002FA7;
  font-family: 'Arial', sans-serif;
}
.q-type {
  background: #ecf5ff;
  color: #409EFF;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
}

.q-body {
  margin-bottom: 30px;
  font-size: 1.1rem;
  line-height: 1.6;
  color: #303133;
}

.q-attachments {
  margin-top: 16px;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
.attachment-item {
  padding: 8px;
  display: flex;
  align-items: center;
}
.attachment-item img {
  max-width: 200px;
  border-radius: 8px;
}

/* 答题区样式 */
.options-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.neu-card-interactive {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-radius: 12px;
  background: #F0F0F3;
  box-shadow: 6px 6px 12px #d1d1d4, -6px -6px 12px #ffffff;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}
.neu-card-interactive:hover {
  transform: translateY(-2px);
  box-shadow: 8px 8px 16px #d1d1d4, -8px -8px 16px #ffffff;
}
.neu-card-interactive.selected {
  border-color: #002FA7;
  background: #ecf5ff;
  box-shadow: inset 4px 4px 8px #d6e8fa, inset -4px -4px 8px #ffffff;
}

.option-prefix {
  font-weight: 700;
  margin-right: 16px;
  width: 24px;
  color: #606266;
}
.selected .option-prefix { color: #002FA7; }

.option-text { flex: 1; }
.option-check { color: #002FA7; width: 20px; }

/* 填空/简答/编程 */
.neu-input :deep(.el-input__wrapper),
.neu-textarea :deep(.el-textarea__inner),
.neu-code-editor :deep(.el-textarea__inner) {
  background: #F0F0F3;
  box-shadow: inset 4px 4px 8px #d1d1d4, inset -4px -4px 8px #ffffff !important;
  border-radius: 12px;
  padding: 12px;
  border: none;
}
.neu-code-editor :deep(.el-textarea__inner) {
  font-family: 'Fira Code', monospace;
  font-size: 14px;
  line-height: 1.5;
  background: #282c34;
  color: #abb2bf;
  box-shadow: inset 4px 4px 10px #1e2127, inset -4px -4px 10px #323741 !important;
}

.coding-toolbar {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Footer */
.q-footer {
  margin-top: auto;
  padding-top: 24px;
  display: flex;
  justify-content: center;
  gap: 20px;
  border-top: 1px solid rgba(0,0,0,0.05);
}
.nav-btn {
  width: 120px;
}
.save-btn {
  width: 140px;
}

/* Auto Save Toast */
.auto-save-toast {
  position: fixed;
  top: 100px;
  right: 40px;
  padding: 12px 24px;
  color: #67C23A;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  z-index: 100;
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.5s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* Keyframes */
@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

/* Scrollbar */
::-webkit-scrollbar { width: 8px; height: 8px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #ccc; border-radius: 4px; }
::-webkit-scrollbar-thumb:hover { background: #aaa; }
</style>
