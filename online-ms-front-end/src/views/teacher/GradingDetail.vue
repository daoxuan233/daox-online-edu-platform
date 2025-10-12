<template>
  <div class="grading-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <el-button 
            type="text" 
            @click="goBack" 
            class="back-btn"
          >
            <i class="fas fa-arrow-left"></i>
            返回阅卷中心
          </el-button>
          <div class="page-title-section">
            <h1 class="page-title">
              <i class="fas fa-clipboard-check"></i>
              阅卷详情
            </h1>
            <p class="page-subtitle">详细批阅学生答卷</p>
          </div>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="saveGrading">
            <i class="fas fa-save"></i>
            保存批阅
          </el-button>
          <el-button @click="submitGrading">
            <i class="fas fa-check"></i>
            提交批阅
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="content-wrapper">
        <!-- 测评基本信息 -->
        <div class="assessment-info-section" v-if="currentTaskDetail?.assessmentInfo">
          <div class="section-card">
            <div class="card-header">
              <h3>
                <i class="fas fa-info-circle"></i>
                测评信息
              </h3>
            </div>
            <div class="card-content">
              <div class="assessment-info-grid">
                <div class="info-item">
                  <span class="info-label">测评标题：</span>
                  <span class="info-value">{{ currentTaskDetail.assessmentInfo.title }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">测评类型：</span>
                  <span class="info-value">{{ getAssessmentTypeText(currentTaskDetail.assessmentInfo.assessmentType) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">开始时间：</span>
                  <span class="info-value">{{ formatDateTime(currentTaskDetail.assessmentInfo.startTime) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">结束时间：</span>
                  <span class="info-value">{{ formatDateTime(currentTaskDetail.assessmentInfo.endTime) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">答题时长：</span>
                  <span class="info-value">{{ currentTaskDetail.assessmentInfo.durationMinutes }} 分钟</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 主观题目列表 -->
        <div class="questions-section">
          <div class="section-card">
            <div class="card-header">
              <h3>
                <i class="fas fa-list-alt"></i>
                主观题批阅列表
              </h3>
              <div class="questions-summary" v-if="currentTaskDetail?.subjectiveQuestions">
                <span>共 {{ currentTaskDetail.subjectiveQuestions.length }} 道主观题</span>
              </div>
            </div>
            <div class="card-content">
              <!-- 加载状态 -->
              <div class="loading-placeholder" v-if="isLoading">
                <i class="fas fa-spinner fa-spin"></i>
                正在加载批阅任务...
              </div>
              
              <!-- 错误状态 -->
              <div class="error-placeholder" v-else-if="error">
                <i class="fas fa-exclamation-triangle"></i>
                <p>{{ error }}</p>
                <el-button @click="fetchGradingTaskDetail" type="primary">重新加载</el-button>
              </div>
              
              <!-- 空状态 -->
              <div class="empty-placeholder" v-else-if="!currentTaskDetail?.subjectiveQuestions?.length">
                <i class="fas fa-inbox"></i>
                <p>暂无主观题需要批阅</p>
              </div>
              
              <!-- 题目列表 -->
              <div class="questions-list" v-else>
                <div 
                  v-for="(question, index) in currentTaskDetail.subjectiveQuestions" 
                  :key="question.questionId"
                  class="question-item"
                  @click="navigateToImmersiveGrading(question.questionId)"
                >
                  <div class="question-header">
                    <div class="question-number">第 {{ index + 1 }} 题</div>
                    <div class="question-score">{{ question.score }} 分</div>
                  </div>
                  
                  <div class="question-content">
                    <div class="question-stem">
                      <span class="stem-label">题干：</span>
                      <span class="stem-text">{{ truncateText(question.stem, 100) }}</span>
                    </div>
                  </div>
                  
                  <div class="question-progress">
                    <div class="progress-info">
                      <span class="progress-text">
                        已批阅：{{ question.gradedCount }} / {{ question.totalCount }}
                      </span>
                      <span class="progress-percentage">
                        {{ calculateProgressPercentage(question.gradedCount, question.totalCount) }}%
                      </span>
                    </div>
                    <el-progress 
                      :percentage="calculateProgressPercentage(question.gradedCount, question.totalCount)"
                      :stroke-width="8"
                      :show-text="false"
                      class="progress-bar"
                    />
                  </div>
                  
                  <div class="question-actions">
                    <el-button type="primary" size="small">
                      <i class="fas fa-edit"></i>
                      开始批阅
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
/**
 * 阅卷详情页面组件
 * 用于显示具体的试卷内容和提供批阅工具
 */
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGradingTaskDetail } from '@/api/teacher/teacherAPI.js'

// 路由相关
const route = useRoute()
const router = useRouter()

// 响应式数据
const isLoading = ref(true)
const error = ref(null)
const currentTaskDetail = ref(null)

/**
 * 返回阅卷中心
 */
const goBack = () => {
  router.push('/teacher/grading')
}

/**
 * 添加快速评语
 * @param {string} tag - 评语标签
 */
const addQuickComment = (tag) => {
  // 此功能在当前页面暂不使用，将在沉浸式批阅页面实现
  console.log('添加快速评语:', tag)
}

/**
 * 保存批阅结果
 */
const saveGrading = () => {
  // 这里应该调用API保存批阅结果
  ElMessage.success('批阅结果已保存')
}

/**
 * 提交批阅结果
 */
const submitGrading = () => {
  // 检查是否有未完成的批阅任务
  if (currentTaskDetail.value?.subjectiveQuestions?.some(q => q.gradedCount < q.totalCount)) {
    ElMessage.warning('还有未完成的批阅任务')
    return
  }
  
  // 这里应该调用API提交批阅结果
  ElMessage.success('批阅结果已提交')
  goBack()
}

/**
 * 计算进度百分比
 * @param {number} gradedCount - 已批阅数量
 * @param {number} totalCount - 总数量
 * @returns {number} 进度百分比
 */
const calculateProgressPercentage = (gradedCount, totalCount) => {
  if (totalCount === 0) return 0;
  return Math.round((gradedCount / totalCount) * 100);
};

/**
 * 截断文本
 * @param {string} text - 原始文本
 * @param {number} maxLength - 最大长度
 * @returns {string} 截断后的文本
 */
const truncateText = (text, maxLength) => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

/**
 * 导航到沉浸式批阅页面
 * @param {string} questionId - 题目ID
 */
const navigateToImmersiveGrading = (questionId) => {
  router.push({
    name: 'ImmersiveGrading',
    params: {
      assessmentId: route.params.assessmentId,
      questionId: questionId
    }
  });
};

/**
 * 格式化日期时间
 * @param {string} dateTimeStr - 日期时间字符串
 * @returns {string} - 格式化后的日期时间
 */
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 获取测评类型文本
 * @param {string} type - 测评类型
 * @returns {string} - 测评类型文本
 */
const getAssessmentTypeText = (type) => {
  const typeMap = {
    'ClassroomExam': '课堂作业',
    'ChapterExam': '章节作业',
    'MidtermExam': '期中考试',
    'FinalExam': '期末考试',
    'homework': '作业'
  }
  return typeMap[type] || type
}

/**
 * 获取批阅任务详情
 */
const fetchGradingTaskDetail = async () => {
  try {
    isLoading.value = true
    error.value = null
    
    const assessmentId = route.params.assessmentId
    const data = await getGradingTaskDetail(assessmentId)
    currentTaskDetail.value = data
    
  } catch (err) {
    error.value = err.message || '获取批阅任务详情失败'
    ElMessage.error(error.value)
  } finally {
    isLoading.value = false
  }
}

/**
 * 组件挂载时的初始化
 */
onMounted(() => {
  fetchGradingTaskDetail()
})
</script>

<style scoped>
.grading-detail-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f0f3 0%, #e8e8eb 100%);
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

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  color: #002FA7;
  font-size: 16px;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(0, 47, 167, 0.1);
}

.page-title-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
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

/* 主要内容 */
.main-content {
  padding: 30px;
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* 测评信息区域 */
.assessment-info-section {
  background: #f8f9fa;
  border-radius: 15px;
  box-shadow: 
    8px 8px 16px rgba(0, 0, 0, 0.1),
    -8px -8px 16px rgba(255, 255, 255, 0.8);
}

.assessment-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  box-shadow: inset 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.info-label {
  font-weight: 600;
  color: #666;
  margin-right: 8px;
  min-width: 80px;
}

.info-value {
  color: #333;
  font-weight: 500;
}

/* 主观题目列表区域 */
.questions-section {
  background: #f8f9fa;
  border-radius: 15px;
  box-shadow: 
    8px 8px 16px rgba(0, 0, 0, 0.1),
    -8px -8px 16px rgba(255, 255, 255, 0.8);
}

.questions-summary {
  color: #666;
  font-size: 14px;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.question-item {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 
    4px 4px 8px rgba(0, 0, 0, 0.1),
    -4px -4px 8px rgba(255, 255, 255, 0.8);
}

.question-item:hover {
  transform: translateY(-2px);
  box-shadow: 
    6px 6px 12px rgba(0, 0, 0, 0.15),
    -6px -6px 12px rgba(255, 255, 255, 0.9);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.question-number {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

.question-score {
  font-size: 14px;
  font-weight: 600;
  color: #e6a23c;
  background: rgba(230, 162, 60, 0.1);
  padding: 4px 8px;
  border-radius: 6px;
}

.question-content {
  margin-bottom: 16px;
}

.question-stem {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.stem-label {
  font-weight: 600;
  color: #666;
  min-width: 40px;
}

.stem-text {
  color: #333;
  line-height: 1.5;
  flex: 1;
}

.question-progress {
  margin-bottom: 16px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-text {
  font-size: 14px;
  color: #666;
}

.progress-percentage {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
}

.progress-bar {
  margin-bottom: 0;
}

.question-actions {
  display: flex;
  justify-content: flex-end;
}

/* 错误和空状态样式 */
.error-placeholder,
.empty-placeholder {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
  font-size: 16px;
}

.error-placeholder i,
.empty-placeholder i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #d1d5db;
  display: block;
}

.error-placeholder p,
.empty-placeholder p {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #6b7280;
}

/* 卡片样式 */
.section-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 
    20px 20px 40px rgba(0, 0, 0, 0.1),
    -20px -20px 40px rgba(255, 255, 255, 0.8);
  overflow: hidden;
}

.card-header {
  padding: 24px 30px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  background: rgba(255, 255, 255, 0.5);
}

.card-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header h3 i {
  color: #002FA7;
}

.assessment-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.assessment-title {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
}

.student-info {
  font-size: 14px;
  color: #6b7280;
}

.card-content {
  padding: 30px;
}

/* 试卷内容区域 */
.loading-placeholder {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
  font-size: 16px;
}

.loading-placeholder i {
  font-size: 24px;
  margin-right: 8px;
  color: #002FA7;
}

.paper-content {
  min-height: 400px;
}

.placeholder-text {
  text-align: center;
  color: #6b7280;
  font-size: 16px;
  line-height: 1.6;
  padding: 60px 20px;
}



/* 响应式设计 */
@media (max-width: 1200px) {
  .content-wrapper {
    max-width: 100%;
    padding: 0 16px;
  }
  
  .main-content {
    padding: 20px 16px;
  }
  
  .page-header {
    padding: 20px 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-left {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .assessment-info-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .card-content {
    padding: 20px;
  }
  
  .card-header {
    padding: 20px;
  }
  
  .question-item {
    padding: 16px;
  }
  
  .question-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .question-stem {
    flex-direction: column;
    gap: 4px;
  }
  
  .progress-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>