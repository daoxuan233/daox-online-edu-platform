<template>
  <div class="grading-detail-container">
    <!-- 背景装饰 -->
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>

    <!-- 页面头部 -->
    <header class="page-header glass-card">
      <div class="header-left">
        <el-button 
          class="back-btn neu-btn-icon" 
          @click="goBack" 
          title="返回阅卷中心"
        >
          <i class="fas fa-arrow-left"></i>
        </el-button>
        <div class="page-title-section">
          <h1 class="page-title">阅卷详情</h1>
          <p class="page-subtitle">批阅学生的主观题答卷</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button class="neu-btn" @click="saveGrading">
          <i class="fas fa-save"></i>
          保存进度
        </el-button>
        <el-button class="neu-btn-primary" @click="submitGrading">
          <i class="fas fa-check-circle"></i>
          提交阅卷
        </el-button>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <div class="content-wrapper">
        
        <!-- 测评基本信息卡片 -->
        <section class="assessment-info-section glass-card" v-if="currentTaskDetail?.assessmentInfo">
          <div class="section-header">
            <h3><i class="fas fa-info-circle"></i> 测评信息</h3>
          </div>
          <div class="info-grid">
            <div class="info-item neu-inset">
              <span class="info-label">测评标题</span>
              <span class="info-value">{{ currentTaskDetail.assessmentInfo.title }}</span>
            </div>
            <div class="info-item neu-inset">
              <span class="info-label">测评类型</span>
              <span class="info-value">
                <el-tag effect="dark" round color="#517B4D" style="border: none;">
                  {{ getAssessmentTypeText(currentTaskDetail.assessmentInfo.assessmentType) }}
                </el-tag>
              </span>
            </div>
            <div class="info-item neu-inset">
              <span class="info-label">答题时长</span>
              <span class="info-value">{{ currentTaskDetail.assessmentInfo.durationMinutes }} 分钟</span>
            </div>
            <div class="info-item neu-inset time-range">
              <span class="info-label">有效时间</span>
              <span class="info-value">
                {{ formatDateTime(currentTaskDetail.assessmentInfo.startTime) }} ~ {{ formatDateTime(currentTaskDetail.assessmentInfo.endTime) }}
              </span>
            </div>
          </div>
        </section>

        <!-- 主观题目列表 -->
        <section class="questions-section">
          <div class="section-header-row">
            <h3>
              <i class="fas fa-list-alt"></i>
              主观题列表
              <span class="badge" v-if="currentTaskDetail?.subjectiveQuestions">
                {{ currentTaskDetail.subjectiveQuestions.length }}
              </span>
            </h3>
          </div>

          <!-- 加载状态 -->
          <div class="loading-state glass-card" v-if="isLoading">
            <div class="loading-content">
              <i class="fas fa-circle-notch fa-spin"></i>
              <p>正在加载批阅任务...</p>
            </div>
          </div>
          
          <!-- 错误状态 -->
          <div class="error-state glass-card" v-else-if="error">
            <i class="fas fa-exclamation-triangle"></i>
            <p>{{ error }}</p>
            <el-button class="neu-btn-primary" @click="fetchGradingTaskDetail">重新加载</el-button>
          </div>
          
          <!-- 空状态 -->
          <div class="empty-state glass-card" v-else-if="!currentTaskDetail?.subjectiveQuestions?.length">
            <i class="fas fa-clipboard-list"></i>
            <p>暂无主观题需要批阅</p>
          </div>
          
          <!-- 题目列表 -->
          <div class="questions-list" v-else>
            <div 
              v-for="(question, index) in currentTaskDetail.subjectiveQuestions" 
              :key="question.questionId"
              class="question-card neu-card-interactive"
              @click="navigateToImmersiveGrading(question.questionId)"
            >
              <div class="q-card-left">
                <div class="q-number">Q{{ index + 1 }}</div>
                <div class="q-score-badge">{{ question.score }} 分</div>
              </div>
              
              <div class="q-card-main">
                <div class="q-stem">
                  {{ truncateText(question.stem, 120) }}
                </div>
                <div class="q-progress-wrapper">
                  <div class="progress-labels">
                    <span class="label">批阅进度</span>
                    <span class="value">{{ question.gradedCount }} / {{ question.totalCount }}</span>
                  </div>
                  <el-progress 
                    :percentage="calculateProgressPercentage(question.gradedCount, question.totalCount)"
                    :stroke-width="8"
                    :show-text="false"
                    :color="getProgressColor(question.gradedCount, question.totalCount)"
                    class="custom-progress"
                  />
                </div>
              </div>
              
              <div class="q-card-action">
                <button class="action-btn">
                  <i class="fas fa-pen-fancy"></i>
                </button>
                <span class="action-text">批阅</span>
              </div>
            </div>
          </div>
        </section>

      </div>
    </main>
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
 */
const calculateProgressPercentage = (gradedCount, totalCount) => {
  if (totalCount === 0) return 0;
  return Math.round((gradedCount / totalCount) * 100);
};

const getProgressColor = (graded, total) => {
  const percentage = calculateProgressPercentage(graded, total)
  if (percentage === 100) return '#67C23A' // Success
  if (percentage > 50) return '#002FA7' // Primary
  return '#E6A23C' // Warning
}

/**
 * 截断文本
 */
const truncateText = (text, maxLength) => {
  if (!text) return '';
  // 移除HTML标签以显示纯文本预览
  const plainText = text.replace(/<[^>]+>/g, '');
  if (plainText.length <= maxLength) return plainText;
  return plainText.substring(0, maxLength) + '...';
};

/**
 * 导航到沉浸式批阅页面
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
 */
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 获取测评类型文本
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

onMounted(() => {
  fetchGradingTaskDetail()
})
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
}

.grading-detail-container {
  min-height: 100vh;
  background-color: #F0F0F3;
  color: #303133;
  font-family: 'Inter', 'PingFang SC', sans-serif;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 40px;
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
  background: rgba(0, 47, 167, 0.08);
  top: -100px;
  right: -100px;
}
.shape-2 {
  width: 300px;
  height: 300px;
  background: rgba(81, 123, 77, 0.08);
  bottom: 100px;
  left: -50px;
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
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.05);
}

.neu-card-interactive {
  background: #F0F0F3;
  box-shadow: 6px 6px 12px #d1d1d4, -6px -6px 12px #ffffff;
  border-radius: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid transparent;
}
.neu-card-interactive:hover {
  transform: translateY(-4px);
  box-shadow: 8px 8px 16px #d1d1d4, -8px -8px 16px #ffffff;
  border-color: rgba(0, 47, 167, 0.1);
}

.neu-inset {
  background: #F0F0F3;
  box-shadow: inset 4px 4px 8px #d1d1d4, inset -4px -4px 8px #ffffff;
  border-radius: 12px;
}

.neu-btn {
  background: #F0F0F3;
  border: none;
  box-shadow: 5px 5px 10px #d1d1d4, -5px -5px 10px #ffffff;
  color: #606266;
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 600;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}
.neu-btn:hover {
  transform: translateY(-2px);
  color: #002FA7;
}
.neu-btn:active {
  box-shadow: inset 3px 3px 6px #d1d1d4, inset -3px -3px 6px #ffffff;
  transform: translateY(0);
}

.neu-btn-primary {
  background: #002FA7;
  color: #fff;
  border: none;
  box-shadow: 4px 4px 10px rgba(0, 47, 167, 0.3);
  border-radius: 10px;
  padding: 10px 24px;
  font-weight: 600;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}
.neu-btn-primary:hover {
  background: #003ccf;
  transform: translateY(-2px);
  box-shadow: 6px 6px 14px rgba(0, 47, 167, 0.4);
}
.neu-btn-primary:active {
  transform: translateY(0);
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

/* =========================================
   3. 页面布局
   ========================================= */
.page-header {
  position: sticky;
  top: 20px;
  margin: 20px auto;
  max-width: 1400px;
  padding: 16px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.page-title-section {
  display: flex;
  flex-direction: column;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #002FA7;
  margin: 0;
}

.page-subtitle {
  font-size: 0.9rem;
  color: #606266;
  margin: 4px 0 0 0;
}

.header-actions {
  display: flex;
  gap: 16px;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* =========================================
   4. 测评信息卡片
   ========================================= */
.assessment-info-section {
  padding: 24px 30px;
}

.section-header {
  margin-bottom: 20px;
}
.section-header h3 {
  font-size: 1.1rem;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}
.section-header h3 i {
  color: #517B4D;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.info-item {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-label {
  font-size: 0.85rem;
  color: #909399;
  font-weight: 600;
}

.info-value {
  font-size: 1rem;
  color: #303133;
  font-weight: 500;
}

.time-range {
  grid-column: span 2;
}
@media (max-width: 768px) {
  .time-range { grid-column: span 1; }
}

/* =========================================
   5. 题目列表
   ========================================= */
.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
}
.section-header-row h3 {
  font-size: 1.25rem;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}
.badge {
  background: #002FA7;
  color: #fff;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 700;
}

.questions-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 24px;
}

.question-card {
  padding: 24px;
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.q-card-left {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.q-number {
  font-size: 1.4rem;
  font-weight: 800;
  color: #002FA7;
  font-family: 'Arial', sans-serif;
}

.q-score-badge {
  background: rgba(81, 123, 77, 0.1);
  color: #517B4D;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 700;
}

.q-card-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.q-stem {
  font-size: 1rem;
  color: #606266;
  line-height: 1.6;
  min-height: 3.2em;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.q-progress-wrapper {
  background: rgba(255,255,255,0.5);
  border-radius: 12px;
  padding: 12px;
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 0.85rem;
}
.progress-labels .label { color: #909399; }
.progress-labels .value { color: #303133; font-weight: 600; }

.q-card-action {
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  opacity: 0.8;
  transition: opacity 0.2s;
}
.question-card:hover .q-card-action {
  opacity: 1;
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #002FA7;
  color: #fff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 4px 4px 10px rgba(0, 47, 167, 0.3);
}

.action-text {
  font-size: 0.9rem;
  font-weight: 600;
  color: #002FA7;
}

/* =========================================
   6. 状态样式
   ========================================= */
.loading-state, .error-state, .empty-state {
  padding: 60px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: #909399;
}
.loading-state i, .error-state i, .empty-state i {
  font-size: 3rem;
  color: #d1d1d4;
  margin-bottom: 10px;
}
.loading-state i { color: #002FA7; }

/* 响应式调整 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
  .questions-list {
    grid-template-columns: 1fr;
  }
}
</style>