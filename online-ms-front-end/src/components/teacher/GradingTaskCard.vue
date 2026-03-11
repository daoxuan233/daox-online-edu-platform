<template>
  <router-link 
    :to="{ name: 'GradingDetail', params: { assessmentId: task.assessmentId } }"
    class="grading-task-card-link"
  >
    <div class="grading-task-card neumorphism-raised">
      <!-- 卡片头部 -->
      <div class="card-header">
        <div class="task-info">
          <h3 class="task-title">{{ task.title }}</h3>
          <p class="course-name">
            <i class="fas fa-book"></i>
            {{ task.courseName }}
          </p>
        </div>
        <div class="task-status">
          <span :class="['status-badge', task.completed ? 'completed' : 'pending']">
            <i :class="task.completed ? 'fas fa-check-circle' : 'fas fa-clock'"></i>
            {{ task.completed ? '已完成' : '待批阅' }}
          </span>
        </div>
      </div>

      <!-- 卡片内容 -->
      <div class="card-content">
        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-icon">
              <i class="fas fa-file-alt"></i>
            </div>
            <div class="stat-content">
              <span class="stat-number">{{ task.pendingSubmissionCount }}</span>
              <span class="stat-label">{{ task.completed ? '份待处理' : '份待批阅' }}</span>
            </div>
          </div>
          
          <div class="stat-item">
            <div class="stat-icon">
              <i class="fas fa-edit"></i>
            </div>
            <div class="stat-content">
              <span class="stat-number">{{ task.subjectiveQuestionCount }}</span>
              <span class="stat-label">道主观题</span>
            </div>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="progress-section">
          <div class="progress-info">
            <span class="progress-label">批阅进度</span>
            <span class="progress-text">
              {{ task.completed ? '已全部完成' : `${task.pendingSubmissionCount} 份待处理` }}
            </span>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: `${progressPercent}%` }"></div>
          </div>
        </div>
      </div>

      <!-- 卡片底部 -->
      <div class="card-footer">
        <div class="action-hint">
          <i class="fas fa-arrow-right"></i>
          {{ task.completed ? '批阅已完成' : '点击开始批阅' }}
        </div>
        <div class="priority-indicator" v-if="task.pendingSubmissionCount > 10">
          <i class="fas fa-exclamation-triangle"></i>
          <span>高优先级</span>
        </div>
      </div>
    </div>
  </router-link>
</template>

<script setup>
/**
 * 阅卷任务卡片组件
 * 用于展示单个阅卷任务的基本信息
 * @component GradingTaskCard
 */

/**
 * 组件属性定义
 * @typedef {Object} GradingTask
 * @property {string} assessmentId - 测评ID
 * @property {string} title - 测评标题
 * @property {string} courseName - 课程名称
 * @property {number} pendingSubmissionCount - 待批阅份数
 * @property {number} subjectiveQuestionCount - 主观题数量
 */

/**
 * Props定义
 * @param {GradingTask} task - 阅卷任务对象
 */
import { computed } from 'vue'

const props = defineProps({
  task: {
    type: Object,
    required: true,
    validator: (value) => {
      return value && 
             typeof value.assessmentId === 'string' &&
             typeof value.title === 'string' &&
             typeof value.courseName === 'string' &&
             typeof value.pendingSubmissionCount === 'number' &&
             typeof value.subjectiveQuestionCount === 'number'
    }
  }
})

const progressPercent = computed(() => {
  if (typeof props.task.progressPercentage === 'number') {
    return Math.max(0, Math.min(100, props.task.progressPercentage))
  }
  const total = Number(props.task.totalSubmissionCount || 0)
  const pending = Number(props.task.pendingSubmissionCount || 0)
  if (total <= 0) return pending > 0 ? 0 : 100
  return Math.max(0, Math.min(100, Math.round(((total - pending) * 100) / total)))
})
</script>

<style scoped>
/**
 * 阅卷任务卡片样式
 * 遵循新拟态设计规范
 */

.grading-task-card-link {
  text-decoration: none;
  color: inherit;
  display: block;
  transition: transform 0.3s ease;
}

.grading-task-card-link:hover {
  transform: translateY(-2px);
}

.grading-task-card {
  background: #f0f0f3;
  box-shadow: 
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.grading-task-card:hover {
  box-shadow: 
    4px 4px 8px #d1d1d4,
    -4px -4px 8px #ffffff;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.task-info {
  flex: 1;
}

.task-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.course-name {
  font-size: 14px;
  color: #606266;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.course-name i {
  color: #002FA7;
}

.task-status {
  flex-shrink: 0;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: rgba(230, 162, 60, 0.1);
  color: #E6A23C;
  border: 1px solid rgba(230, 162, 60, 0.2);
}

.status-badge.completed {
  background: rgba(103, 194, 58, 0.1);
  color: #67C23A;
  border: 1px solid rgba(103, 194, 58, 0.2);
}

/* 卡片内容 */
.card-content {
  margin-bottom: 20px;
}

.stats-row {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(0, 47, 167, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #002FA7;
  font-size: 16px;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

/* 进度条部分 */
.progress-section {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  padding: 16px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.progress-text {
  font-size: 12px;
  color: #909399;
}

.progress-bar {
  height: 6px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #002FA7 0%, #517B4D 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #002FA7;
  font-weight: 500;
}

.action-hint i {
  transition: transform 0.3s ease;
}

.grading-task-card:hover .action-hint i {
  transform: translateX(4px);
}

.priority-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #F56C6C;
  font-weight: 500;
}

.priority-indicator i {
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .grading-task-card {
    padding: 16px;
    margin-bottom: 16px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .stats-row {
    flex-direction: column;
    gap: 16px;
  }
  
  .task-title {
    font-size: 16px;
  }
  
  .stat-number {
    font-size: 18px;
  }
}

@media (max-width: 480px) {
  .grading-task-card {
    padding: 12px;
  }
  
  .progress-section {
    padding: 12px;
  }
  
  .card-footer {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
}
</style>
