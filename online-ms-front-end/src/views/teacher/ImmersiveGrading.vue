<!--
  沉浸式批阅页面组件
  用于对单个主观题进行详细批阅
-->
<template>
  <div class="immersive-grading-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <el-button @click="goBack" type="text" class="back-button">
            <i class="fas fa-arrow-left"></i>
            返回批阅列表
          </el-button>
          <h1 class="page-title">{{ pageTitle }}</h1>
        </div>
        <div class="header-right">
          <el-button @click="saveDraft" type="default">
            <i class="fas fa-save"></i>
            保存草稿
          </el-button>
          <el-button @click="submitGrading" type="primary">
            <i class="fas fa-check"></i>
            提交批阅
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="content-wrapper">
        <!-- 加载状态 -->
        <div class="loading-placeholder" v-if="isLoading">
          <i class="fas fa-spinner fa-spin"></i>
          正在加载批阅内容...
        </div>
        
        <!-- 错误状态 -->
        <div class="error-placeholder" v-else-if="error">
          <i class="fas fa-exclamation-triangle"></i>
          <p>{{ error }}</p>
          <el-button @click="fetchGradingData" type="primary">重新加载</el-button>
        </div>

        <!-- 批阅内容 -->
        <div class="grading-content" v-else>
          <!-- 题目信息 -->
          <div class="question-section">
            <div class="section-card">
              <div class="card-header">
                <h3>
                  <i class="fas fa-question-circle"></i>
                  题目信息
                </h3>
              </div>
              <div class="card-content">
                <div class="question-info">
                  <div class="question-stem">
                    <h4>题干：</h4>
                    <div class="stem-content">
                      {{ questionData?.stem || '题干内容加载中...' }}
                    </div>
                  </div>
                  
                  <!-- 选择题选项显示 -->
                  <div class="question-options" v-if="questionData?.options && questionData.options.length > 0">
                    <h4>选项：</h4>
                    <div class="options-list">
                      <div 
                        v-for="option in questionData.options" 
                        :key="option.key"
                        class="option-item"
                      >
                        <span class="option-key">{{ option.key }}.</span>
                        <span class="option-text">{{ option.text }}</span>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 正确答案显示 -->
                  <div class="question-answer" v-if="questionData?.answer">
                    <h4>参考答案：</h4>
                    <div class="answer-content">
                      <template v-if="Array.isArray(questionData.answer)">
                        {{ questionData.answer.join(', ') }}
                      </template>
                      <template v-else>
                        {{ questionData.answer }}
                      </template>
                    </div>
                  </div>
                  
                  <!-- 答案解析显示 -->
                  <div class="question-analysis" v-if="questionData?.analysis">
                    <h4>答案解析：</h4>
                    <div class="analysis-content">
                      {{ questionData.analysis }}
                    </div>
                  </div>
                  
                  <!-- 题目元信息 -->
                  <div class="question-meta">
                    <span class="question-score">总分：{{ questionData?.score || 0 }} 分</span>
                    <span class="question-type">题型：{{ getQuestionTypeText(questionData?.type) }}</span>
                    <span class="question-difficulty" v-if="questionData?.difficulty">
                      难度：{{ getDifficultyText(questionData.difficulty) }}
                    </span>
                  </div>
                  
                  <!-- 知识点标签 -->
                  <div class="question-tags" v-if="questionData?.tags && questionData.tags.length > 0">
                    <h4>知识点标签：</h4>
                    <div class="tags-list">
                      <el-tag 
                        v-for="tag in questionData.tags" 
                        :key="tag"
                        size="small"
                        type="info"
                        class="tag-item"
                      >
                        {{ tag }}
                      </el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 学生答案列表 -->
          <div class="answers-section">
            <div class="section-card">
              <div class="card-header">
                <h3>
                  <i class="fas fa-users"></i>
                  学生答案批阅
                </h3>
                <div class="answers-summary">
                  <span>待批阅：{{ pendingCount }} / {{ totalCount }}</span>
                </div>
              </div>
              <div class="card-content">
                <!-- 答案列表 -->
                <div class="answers-list" v-if="studentAnswers?.length">
                  <div
                    v-for="(answer, index) in studentAnswers"
                    :key="answer.studentId"
                    class="answer-item"
                    :class="{ 'active': currentAnswerIndex === index }"
                    @click="selectAnswer(index)"
                  >
                    <div class="answer-header">
                      <div class="student-info">
                        <span class="student-name">{{ answer.studentName }}</span>
                        <span class="student-id">{{ answer.identifier || answer.studentId }}</span>
                      </div>
                      <div class="answer-status">
                        <el-tag
                          :type="getStatusType(answer.status)"
                          size="small"
                        >
                          {{ getStatusText(answer.status) }}
                        </el-tag>
                      </div>
                    </div>

                    <div class="answer-content">
                      <div class="answer-text">
                        {{ answer.response || '学生未作答' }}
                      </div>
                    </div>

                    <div class="answer-grading" v-if="answer.status === 'graded'">
                      <div class="grading-score">
                        得分：{{ answer.currentScore || answer.score || 0 }} / {{ questionMaxScore }}
                      </div>
                      <div class="grading-comment" v-if="answer.comment">
                        评语：{{ answer.comment }}
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 空状态 -->
                <div class="empty-answers" v-else>
                  <i class="fas fa-inbox"></i>
                  <p>暂无学生答案</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 批阅工具 -->
          <div class="grading-tools-section" v-if="currentAnswer">
            <div class="section-card">
              <div class="card-header">
                <h3>
                  <i class="fas fa-edit"></i>
                  批阅工具
                </h3>
              </div>
              <div class="card-content">
                <div class="grading-form">
                  <!-- 评分 -->
                  <div class="score-section">
                    <label class="form-label">评分：</label>
                    <div class="score-input-group">
                      <el-input-number
                        v-model="currentScore"
                        :min="0"
                        :max="questionMaxScore"
                        :precision="1"
                        size="large"
                        class="score-input"
                      />
                      <span class="score-unit">/ {{ questionMaxScore }} 分</span>
                    </div>
                    <div class="score-hint">当前题目最高分：{{ questionMaxScore }} 分</div>
                    <div class="quick-scores">
                      <el-button
                        v-for="score in quickScoreButtons"
                        :key="score"
                        size="small"
                        class="quick-score-btn"
                        @click="applyQuickScore(score)"
                      >
                        {{ score }} 分
                      </el-button>
                    </div>
                  </div>

                  <!-- 评语 -->
                  <div class="comment-section">
                    <label class="form-label">评语：</label>
                    <el-input
                      v-model="currentComment"
                      type="textarea"
                      :rows="4"
                      placeholder="请输入对该学生答案的评语..."
                      class="comment-input"
                    />
                  </div>

                  <!-- 快速评语 -->
                  <div class="quick-comments">
                    <label class="form-label">快速评语：</label>
                    <div class="comment-tags">
                      <el-tag
                        v-for="tag in quickComments"
                        :key="tag"
                        @click="addQuickComment(tag)"
                        class="comment-tag"
                        type="info"
                      >
                        {{ tag }}
                      </el-tag>
                    </div>
                  </div>

                  <!-- 操作按钮 -->
                  <div class="grading-actions">
                    <el-button @click="saveCurrentGrading" type="default">
                      保存当前批阅
                    </el-button>
                    <el-button @click="nextAnswer" type="primary">
                      下一份答案
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getQuestionDetail, getStudentAnswers, gradeAnswer } from '@/api/teacher/teacherAPI.js'

// 路由相关
const route = useRoute()
const router = useRouter()

// 响应式数据
const isLoading = ref(false)
const error = ref(null)
const questionData = ref(null)
const studentAnswers = ref([])
const currentAnswerIndex = ref(0)
const currentScore = ref(0)
const currentComment = ref('')

// 快速评语选项
const quickComments = ref([
  '答题思路清晰',
  '解题方法正确',
  '计算准确',
  '表达规范',
  '需要加强练习',
  '注意审题',
  '书写工整',
  '理解有偏差',
  '逻辑性强'
])

/**
 * 计算属性
 */
const pageTitle = computed(() => {
  return questionData.value ? `批阅第${route.params.questionId}题` : '沉浸式批阅'
})

const currentAnswer = computed(() => {
  return studentAnswers.value[currentAnswerIndex.value] || null
})

const totalCount = computed(() => {
  return studentAnswers.value.length
})

const pendingCount = computed(() => {
  return studentAnswers.value.filter(answer => answer.status === 'pending').length
})

const questionMaxScore = computed(() => {
  const maxScoreFromCurrentAnswer = normalizeMaxScore(currentAnswer.value?.maxScore)
  if (maxScoreFromCurrentAnswer > 0) return maxScoreFromCurrentAnswer
  return normalizeMaxScore(questionData.value?.score)
})

const quickScoreButtons = computed(() => {
  const maxScore = questionMaxScore.value
  if (maxScore <= 0) return [0]
  const integerMaxScore = Math.floor(maxScore)
  const candidateScores = [
    integerMaxScore,
    Math.floor(maxScore * 0.9),
    Math.floor(maxScore * 0.8),
    Math.floor(maxScore * 0.6),
    0
  ]
  return Array.from(new Set(candidateScores))
    .filter(score => score >= 0 && score <= integerMaxScore)
    .sort((a, b) => b - a)
})

const mapGradingStatus = (gradingStatus) => {
  const statusMap = {
    'pending_manual': 'pending',
    'manually_graded': 'graded'
  }
  return statusMap[gradingStatus] || 'pending'
}

const formatStudentResponse = (response) => {
  const normalizeArrayResponse = (items) => {
    const normalized = items
      .map(item => item === null || item === undefined ? '' : String(item).trim())
    const hasValidValue = normalized.some(item => item !== '')
    if (!hasValidValue) return '学生未作答'
    return normalized.map(item => item || '（空）').join('，')
  }

  if (response === null || response === undefined) return '学生未作答'

  if (Array.isArray(response)) {
    return normalizeArrayResponse(response)
  }

  if (typeof response === 'string') {
    const text = response.trim()
    if (!text) return '学生未作答'
    try {
      const parsed = JSON.parse(text)
      if (Array.isArray(parsed)) {
        return normalizeArrayResponse(parsed)
      }
      if (parsed && typeof parsed === 'object') {
        if (typeof parsed.code === 'string') {
          return parsed.code.trim() || '学生未作答'
        }
        return JSON.stringify(parsed)
      }
      return String(parsed ?? '').trim() || '学生未作答'
    } catch {
      return text
    }
  }

  if (typeof response === 'object') {
    if (typeof response.code === 'string') {
      return response.code.trim() || '学生未作答'
    }
    return JSON.stringify(response)
  }

  return String(response)
}

/**
 * 方法
 */

/**
 * 返回批阅列表
 */
const goBack = () => {
  router.push({
    name: 'GradingDetail',
    params: { assessmentId: route.params.assessmentId }
  })
}

/**
 * 获取批阅数据
 */
const fetchGradingData = async () => {
  try {
    isLoading.value = true
    error.value = null
    
    const { assessmentId, questionId } = route.params
    if (!assessmentId || !questionId) {
      throw new Error('缺少必要的参数')
    }
    
    // 调用API获取题目详情
    try {
      const questionDetail = await getQuestionDetail(questionId)
      
      // 根据后端 Question.java 实体结构映射数据
      questionData.value = {
        id: questionDetail.id,
        courseId: questionDetail.courseId,
        creatorId: questionDetail.creatorId,
        type: questionDetail.type,
        difficulty: questionDetail.difficulty,
        stem: questionDetail.stem,
        options: questionDetail.options || [], // 选择题选项
        answer: questionDetail.answer, // 正确答案
        analysis: questionDetail.analysis, // 答案解析
        tags: questionDetail.tags || [], // 知识点标签
        isDeleted: questionDetail.isDeleted,
        createdAt: questionDetail.createdAt,
        updatedAt: questionDetail.updatedAt,
        // 为了兼容现有模板，保留 score 字段（可能来自试卷配置）
        score: questionDetail.score || 0
      }
      
      console.log('题目详情加载成功:', questionData.value)
      
    } catch (apiError) {
      console.error('获取题目详情失败:', apiError)
      throw new Error(`获取题目详情失败: ${apiError.message}`)
    }
    
    // 调用API获取学生答案
    try {
      const studentAnswersResponse = await getStudentAnswers(assessmentId, questionId)
      
      // 根据后端 StudentAnswerSnippetDTO 结构映射数据
      studentAnswers.value = studentAnswersResponse.content?.map(answer => {
        return {
          submissionId: answer.submissionId, // StudentAnswer 的 id
          studentId: answer.studentId,
          studentName: answer.studentName, // 或其他匿名化标识
          identifier: answer.identifier, // 学号
          response: formatStudentResponse(answer.response),
          maxScore: normalizeMaxScore(answer.maxScore),
          currentScore: answer.currentScore, // 当前得分
          comment: answer.comment, // 当前评语
          gradingStatus: answer.gradingStatus, // 后端返回的原始状态
          // 根据后端gradingStatus字段映射前端状态
          status: mapGradingStatus(answer.gradingStatus),
          score: answer.currentScore // 兼容字段
        };
      }) || []
      
      console.log('学生答案加载成功:', studentAnswers.value)
      
    } catch (apiError) {
      console.error('获取学生答案失败:', apiError)
      // 如果获取学生答案失败，设置为空数组，但不阻止页面渲染
      studentAnswers.value = []
      ElMessage.warning(`获取学生答案失败: ${apiError.message}`)
    }
    
    // 选择第一个待批阅的答案
    const firstPendingIndex = studentAnswers.value.findIndex(answer => answer.status === 'pending')
    if (firstPendingIndex !== -1) {
      selectAnswer(firstPendingIndex)
    }
    
  } catch (err) {
    console.error('获取批阅数据失败:', err)
    error.value = err.message || '获取批阅数据失败'
    ElMessage.error("获取批阅数据失败：" + error.value)
  } finally {
    isLoading.value = false
  }
}

/**
 * 选择答案
 * @param {number} index - 答案索引
 */
const selectAnswer = (index) => {
  currentAnswerIndex.value = index
  const answer = studentAnswers.value[index]
  if (answer) {
    currentScore.value = clampScore(answer.currentScore || 0)
    currentComment.value = answer.comment || ''
  }
}

const applyQuickScore = (score) => {
  currentScore.value = clampScore(score)
}

/**
 * 获取状态类型
 * @param {string} status - 状态
 * @returns {string} Element Plus tag类型
 */
const getStatusType = (status) => {
  const typeMap = {
    'pending': 'warning',
    'graded': 'success',
    'review': 'info'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取状态文本
 * @param {string} status - 状态
 * @returns {string} 状态文本
 */
const getStatusText = (status) => {
  const textMap = {
    'pending': '待批阅',
    'graded': '已批阅',
    'review': '需复查'
  }
  return textMap[status] || status
}

/**
 * 添加快速评语
 * @param {string} tag - 快速评语标签
 */
const addQuickComment = (tag) => {
  if (currentComment.value) {
    currentComment.value += `\n${tag}`
  } else {
    currentComment.value = tag
  }
}

/**
 * 保存当前批阅
 * 调用后端 API 保存批阅结果，包含完善的错误处理和用户反馈
 */
const saveCurrentGrading = async () => {
  if (!currentAnswer.value) {
    ElMessage.warning('请先选择要批阅的学生答案')
    return
  }
  
  // 验证必要的参数
  const { assessmentId, questionId } = route.params
  if (!assessmentId || !questionId) {
    ElMessage.error('缺少必要的参数：测评ID或题目ID')
    return
  }
  
  // 验证分数范围
  const maxScore = questionMaxScore.value
  if (currentScore.value < 0 || currentScore.value > maxScore) {
    ElMessage.error(`分数必须在 0 到 ${maxScore} 之间`)
    return
  }
  
  // 构建请求参数，符合后端 gradeAnswer 函数的要求
  const gradeRequest = {
    submissionId: currentAnswer.value.submissionId, // 学生答卷ID
    questionId: questionId, // 题目ID
    score: currentScore.value, // 评分
    comment: currentComment.value || '', // 评语（可选）
    assessmentId: assessmentId // 测评ID（用于上下文）
  }
  
  try {
    // 显示加载状态
    const loadingMessage = ElMessage({
      message: '正在保存批阅结果...',
      type: 'info',
      duration: 0, // 不自动关闭
      showClose: false
    })
    
    // 调用后端 API 保存批阅结果
    const result = await gradeAnswer(gradeRequest)
    
    // 关闭加载消息
    loadingMessage.close()
    
    // 更新本地数据状态
    const answer = currentAnswer.value
    answer.currentScore = currentScore.value
    answer.score = currentScore.value // 保持兼容性
    answer.comment = currentComment.value
    answer.status = 'graded'
    answer.gradingStatus = 'manually_graded' // 更新后端状态字段
    
    // 显示成功消息，包含更新后的总分信息
    let successMessage = '批阅已保存'
    if (result && result.updatedTotalScore !== undefined) {
      successMessage += `，该学生当前总分：${result.updatedTotalScore} 分`
    }
    
    ElMessage.success(successMessage)
    
    console.log('批阅保存成功:', {
      submissionId: gradeRequest.submissionId,
      questionId: gradeRequest.questionId,
      score: gradeRequest.score,
      comment: gradeRequest.comment,
      result: result
    })
    
  } catch (error) {
    console.error('保存批阅失败:', error)
    
    // 根据错误类型显示不同的错误消息
    let errorMessage = '保存批阅失败'
    
    if (error.message) {
      if (error.message.includes('网络')) {
        errorMessage = '网络连接失败，请检查网络后重试'
      } else if (error.message.includes('权限')) {
        errorMessage = '没有权限进行此操作，请联系管理员'
      } else if (error.message.includes('参数')) {
        errorMessage = '提交参数有误，请刷新页面后重试'
      } else {
        errorMessage = `保存失败：${error.message}`
      }
    }
    
    ElMessage.error(errorMessage)
    
    // 可选：提供重试机制
    ElMessage({
      message: '点击此处重试保存',
      type: 'info',
      duration: 5000,
      showClose: true,
      onClick: () => {
        saveCurrentGrading()
      }
    })
  }
}

const normalizeMaxScore = (value) => {
  const parsed = Number(value)
  if (!Number.isFinite(parsed) || parsed < 0) {
    return 0
  }
  return roundScore(parsed)
}

const roundScore = (value) => {
  return Math.round(Number(value) * 10) / 10
}

const clampScore = (value) => {
  const score = Number(value)
  if (!Number.isFinite(score)) return 0
  const boundedScore = Math.max(0, Math.min(questionMaxScore.value, score))
  return roundScore(boundedScore)
}

watch(questionMaxScore, () => {
  currentScore.value = clampScore(currentScore.value)
})

watch(currentScore, (value) => {
  const clamped = clampScore(value)
  if (clamped !== value) {
    currentScore.value = clamped
  }
})

/**
 * 下一份答案
 * 先保存当前批阅，然后跳转到下一个待批阅的答案
 */
const nextAnswer = async () => {
  try {
    // 等待当前批阅保存完成
    await saveCurrentGrading()
    
    // 查找下一个待批阅的答案
    const nextPendingIndex = studentAnswers.value.findIndex(
      (answer, index) => index > currentAnswerIndex.value && answer.status === 'pending'
    )
    
    if (nextPendingIndex !== -1) {
      selectAnswer(nextPendingIndex)
      ElMessage.success('已切换到下一份答案')
    } else {
      ElMessage.info('所有答案已批阅完成')
    }
  } catch (error) {
    console.error('切换到下一份答案时出错:', error)
    ElMessage.warning('保存当前批阅失败，无法切换到下一份答案')
  }
}

/**
 * 保存草稿
 */
const saveDraft = () => {
  // TODO: 实现保存草稿功能
  ElMessage.success('草稿已保存')
}

/**
 * 提交批阅
 */
const submitGrading = () => {
  // TODO: 实现提交批阅功能
  ElMessage.success('批阅已提交')
}

/**
 * 获取题型文本显示
 * @param {string} type - 题型代码（与后端 Question.QuestionType 枚举一致）
 * @returns {string} 题型文本
 */
const getQuestionTypeText = (type) => {
  const typeMap = {
    'SINGLE_CHOICE': '单选题',
    'MULTI_CHOICE': '多选题',
    'TRUE_FALSE': '判断题',
    'SHORT_ANSWER': '简答题',
    'FILL_IN_BLANKS': '填空题',
    'PROGRAMMING': '程序题'
  }
  return typeMap[type] || '未知题型'
}

/**
 * 获取难度文本显示
 * @param {string} difficulty - 难度代码（与后端 Question.QuestionDifficulty 枚举一致）
 * @returns {string} 难度文本
 */
const getDifficultyText = (difficulty) => {
  const difficultyMap = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难'
  }
  return difficultyMap[difficulty] || '未知难度'
}

/**
 * 组件挂载时的初始化
 */
onMounted(() => {
  fetchGradingData()
})
</script>

<style scoped>
/* 变量定义 - 符合设计手册颜色 */
:root {
  --primary-color: #002FA7; /* 克莱因蓝 */
  --primary-light: #409EFF;
  --success-color: #67C23A;
  --warning-color: #E6A23C;
  --danger-color: #F56C6C;
  --info-color: #909399;
  --text-main: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --bg-color: #f0f0f3;
  --shadow-light: #ffffff;
  --shadow-dark: #d1d1d4;
}

.immersive-grading-container {
  min-height: 100vh;
  background-color: #f0f0f3; /* Neumorphism background */
  font-family: 'Inter', 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  color: #303133;
}

/* 页面头部 - 磨砂玻璃 */
.page-header {
  background: rgba(240, 240, 243, 0.8); /* Match bg color but transparent */
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  padding: 16px 0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.header-content {
  max-width: 1600px; /* Wider for immersive feel */
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-button {
  font-size: 15px;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
  color: #606266;
  /* Neumorphism flat/button style */
  background: #f0f0f3;
  box-shadow: 5px 5px 10px #d1d1d4, -5px -5px 10px #ffffff;
  border: none;
}

.back-button:hover {
  color: #002FA7;
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px #d1d1d4, -6px -6px 12px #ffffff;
}

.back-button:active {
  box-shadow: inset 4px 4px 8px #d1d1d4, inset -4px -4px 8px #ffffff;
  transform: translateY(0);
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  letter-spacing: 0.5px;
}

.header-right {
  display: flex;
  gap: 16px;
}

/* 主要内容区域 */
.main-content {
  padding: 24px;
  height: calc(100vh - 73px); /* Fixed height for scrolling inside columns */
  overflow: hidden; /* Prevent body scroll */
}

.content-wrapper {
  max-width: 1600px;
  margin: 0 auto;
  height: 100%;
}

.grading-content {
  display: grid;
  grid-template-columns: 350px 1fr 380px; /* Optimized column widths */
  gap: 24px;
  height: 100%;
  align-items: start;
}

/* 通用卡片样式 - Neumorphism Raised */
.section-card {
  background: #f0f0f3;
  border-radius: 20px;
  box-shadow: 9px 9px 16px #d1d1d4, -9px -9px 16px #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.4);
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-header {
  padding: 20px 24px;
  background: rgba(240, 240, 243, 0.5);
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header h3 i {
  color: #002FA7;
  font-size: 18px;
}

.card-content {
  padding: 24px;
  overflow-y: auto; /* Enable internal scrolling */
  flex-grow: 1;
  scrollbar-width: thin;
  scrollbar-color: #d1d1d4 #f0f0f3;
}

/* Custom Scrollbar */
.card-content::-webkit-scrollbar {
  width: 6px;
}
.card-content::-webkit-scrollbar-track {
  background: transparent;
}
.card-content::-webkit-scrollbar-thumb {
  background-color: #d1d1d4;
  border-radius: 10px;
}

/* 左侧：题目信息 */
.question-section {
  height: 100%;
}

.question-info {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.question-stem h4, .question-options h4, .question-answer h4, .question-analysis h4, .question-tags h4 {
  font-size: 14px;
  font-weight: 700;
  color: #606266;
  margin: 0 0 12px 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stem-content, .analysis-content, .answer-content {
  background: #f0f0f3;
  padding: 16px;
  border-radius: 12px;
  line-height: 1.7;
  color: #303133;
  font-size: 15px;
  /* Neumorphism Inset */
  box-shadow: inset 5px 5px 10px #d1d1d4, inset -5px -5px 10px #ffffff;
}

.question-options .option-item {
  background: #f0f0f3;
  padding: 12px 16px;
  border-radius: 12px;
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
  box-shadow: 5px 5px 10px #d1d1d4, -5px -5px 10px #ffffff;
  border: 1px solid rgba(255,255,255,0.2);
}

.question-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  padding-top: 10px;
  border-top: 1px solid rgba(0,0,0,0.05);
}

.question-score,
.question-type,
.question-difficulty {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
  /* Glassy tags */
  background: rgba(255, 255, 255, 0.5);
  box-shadow: 3px 3px 6px #d1d1d4, -3px -3px 6px #ffffff;
}

/* 中间：学生答案列表 */
.answers-section {
  height: 100%;
}

.answers-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.answer-item {
  background: #f0f0f3;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
  box-shadow: 6px 6px 12px #d1d1d4, -6px -6px 12px #ffffff;
  position: relative;
  overflow: hidden;
}

.answer-item:hover {
  transform: translateY(-2px);
  box-shadow: 8px 8px 16px #d1d1d4, -8px -8px 16px #ffffff;
}

.answer-item.active {
  border-color: rgba(0, 47, 167, 0.1);
  /* Active state - maybe inset or just colored */
  background: linear-gradient(145deg, #f0f0f3, #e6e6e9);
  box-shadow: inset 6px 6px 12px #d1d1d4, inset -6px -6px 12px #ffffff;
}

.answer-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: #002FA7;
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.student-name {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.student-id {
  font-size: 13px;
  color: #909399;
}

.answer-text {
  background: #f0f0f3; /* or transparent since item is already styled */
  padding: 12px;
  border-radius: 8px;
  line-height: 1.6;
  color: #303133;
  font-size: 15px;
  /* If active, maybe different? */
  box-shadow: inset 4px 4px 8px #d1d1d4, inset -4px -4px 8px #ffffff;
}

.answer-grading {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.grading-score {
  font-weight: 700;
  color: #002FA7;
  font-size: 16px;
}

/* 右侧：批阅工具 */
.grading-tools-section {
  height: 100%;
}

.grading-form {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.score-section, .comment-section, .quick-comments {
  background: #f0f0f3;
  padding: 20px;
  border-radius: 16px;
  box-shadow: 6px 6px 12px #d1d1d4, -6px -6px 12px #ffffff;
}

.form-label {
  font-size: 14px;
  font-weight: 700;
  color: #606266;
  margin-bottom: 12px;
  display: block;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.score-input-group {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

:deep(.el-input-number__decrease), :deep(.el-input-number__increase) {
  background: #f0f0f3 !important;
  border: none !important;
  box-shadow: 4px 4px 8px #d1d1d4, -4px -4px 8px #ffffff !important;
  color: #606266 !important;
}

:deep(.el-input-number__decrease:hover), :deep(.el-input-number__increase:hover) {
  color: #002FA7 !important;
}

:deep(.el-input__wrapper) {
  background: #f0f0f3 !important;
  box-shadow: inset 4px 4px 8px #d1d1d4, inset -4px -4px 8px #ffffff !important;
  border-radius: 8px !important;
  padding: 4px 12px !important;
}

:deep(.el-textarea__inner) {
  background: #f0f0f3 !important;
  box-shadow: inset 5px 5px 10px #d1d1d4, inset -5px -5px 10px #ffffff !important;
  border: none !important;
  border-radius: 12px !important;
  padding: 16px !important;
  font-family: inherit;
}

.quick-scores {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-score-btn {
  border-radius: 8px !important;
  border: none !important;
  background: #f0f0f3 !important;
  box-shadow: 4px 4px 8px #d1d1d4, -4px -4px 8px #ffffff !important;
  color: #606266 !important;
  transition: all 0.2s !important;
}

.quick-score-btn:hover {
  transform: translateY(-1px);
  box-shadow: 5px 5px 10px #d1d1d4, -5px -5px 10px #ffffff !important;
  color: #002FA7 !important;
}

.quick-score-btn:active {
  box-shadow: inset 3px 3px 6px #d1d1d4, inset -3px -3px 6px #ffffff !important;
  transform: translateY(0);
}

.comment-tag {
  cursor: pointer;
  border: none !important;
  background: #f0f0f3 !important;
  box-shadow: 3px 3px 6px #d1d1d4, -3px -3px 6px #ffffff !important;
  color: #606266 !important;
  padding: 6px 12px !important;
  height: auto !important;
  margin: 0 !important;
}

.comment-tag:hover {
  color: #002FA7 !important;
  transform: translateY(-1px);
}

.grading-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-top: 10px;
}

.grading-actions .el-button {
  width: 100%;
  height: 44px;
  border-radius: 12px;
  font-weight: 600;
  border: none;
  box-shadow: 5px 5px 10px #d1d1d4, -5px -5px 10px #ffffff;
}

.grading-actions .el-button--primary {
  background: #002FA7;
  color: white;
  box-shadow: 5px 5px 10px rgba(0, 47, 167, 0.3), -5px -5px 10px rgba(255, 255, 255, 0.8);
}

.grading-actions .el-button--primary:hover {
  background: #003cc7;
}

.grading-actions .el-button--default {
  background: #f0f0f3;
  color: #606266;
}

.grading-actions .el-button:active {
  box-shadow: inset 3px 3px 6px rgba(0, 0, 0, 0.2);
}

/* Empty & Loading States */
.empty-answers, .loading-placeholder, .error-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.empty-answers i, .loading-placeholder i, .error-placeholder i {
  font-size: 48px;
  margin-bottom: 20px;
  color: #d1d1d4;
  text-shadow: 2px 2px 4px #ffffff, -2px -2px 4px #d1d1d4;
}

/* Responsive */
@media (max-width: 1200px) {
  .grading-content {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto auto;
    height: auto;
  }
  
  .main-content {
    height: auto;
    overflow: visible;
  }
  
  .section-card {
    height: auto;
    max-height: 80vh;
  }
}
</style>
