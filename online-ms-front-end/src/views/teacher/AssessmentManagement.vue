<template>
  <div class="assessment-management-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <i class="fas fa-clipboard-list"></i>
            测评管理
          </h1>
          <p class="page-subtitle">创建和管理课程测评与考试</p>
        </div>
        <div class="header-actions">
          <el-button class="action-btn template-btn">
            <i class="fas fa-file-alt"></i>
            测评模板
          </el-button>
          <el-button type="primary" class="action-btn create-btn" @click="showCreateDialog = true">
            <i class="fas fa-plus"></i>
            创建测评
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-clipboard-list"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalAssessments }}</div>
            <div class="stat-label">总测评数</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-play"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.activeAssessments }}</div>
            <div class="stat-label">进行中</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-users"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalParticipants }}</div>
            <div class="stat-label">参与人数</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-chart-line"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.averageScore }}</div>
            <div class="stat-label">平均分数</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <div class="filter-card">
        <div class="filter-row">
          <div class="filter-group">
            <label class="filter-label">测评类型</label>
            <el-select v-model="filters.type" placeholder="全部类型" class="filter-select">
              <el-option label="全部类型" value="" />
              <el-option label="课堂测验" value="quiz" />
              <el-option label="章节测试" value="test" />
              <el-option label="期中考试" value="midterm" />
              <el-option label="期末考试" value="final" />
              <el-option label="作业" value="homework" />
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">状态</label>
            <el-select v-model="filters.status" placeholder="全部状态" class="filter-select">
              <el-option label="全部状态" value="" />
              <el-option label="草稿" value="draft" />
              <el-option label="已发布" value="published" />
              <el-option label="进行中" value="active" />
              <el-option label="已结束" value="ended" />
              <el-option label="已归档" value="archived" />
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">所属课程</label>
            <el-select v-model="filters.course" placeholder="全部课程" class="filter-select">
              <el-option label="全部课程" value="" />
              <el-option label="Vue.js 基础" value="vue-basic" />
              <el-option label="React 进阶" value="react-advanced" />
              <el-option label="Node.js 实战" value="nodejs-practice" />
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">时间范围</label>
            <el-date-picker
              v-model="filters.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              class="filter-date"
              style="width: 99%"
            />
          </div>
        </div>
        
        <div class="search-row">
          <div class="search-group">
            <el-input 
              v-model="filters.search" 
              placeholder="搜索测评标题、描述或课程..."
              class="search-input"
            >
              <template #prefix>
                <i class="fas fa-search"></i>
              </template>
            </el-input>
          </div>
          
          <div class="action-group">
            <el-button class="filter-btn" @click="resetFilters">
              <i class="fas fa-undo"></i>
              重置
            </el-button>
            <el-button type="primary" class="filter-btn">
              <i class="fas fa-search"></i>
              搜索
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 测评列表 -->
    <div class="assessments-section">
      <div class="section-header">
        <div class="header-left">
          <h3>测评列表</h3>
          <span class="assessment-count">共 {{ filteredAssessments.length }} 个测评</span>
        </div>
        <div class="header-actions">
          <el-button-group class="view-toggle">
            <el-button 
              :type="viewMode === 'grid' ? 'primary' : ''"
              @click="viewMode = 'grid'"
              class="toggle-btn"
            >
              <i class="fas fa-th"></i>
            </el-button>
          </el-button-group>
          
          <el-select v-model="sortBy" placeholder="排序方式" class="sort-select">
            <el-option label="创建时间" value="createTime" />
            <el-option label="开始时间" value="startTime" />
            <el-option label="结束时间" value="endTime" />
            <el-option label="参与人数" value="participants" />
          </el-select>
        </div>
      </div>
      
      <div class="assessments-content">
        <div :class="['assessments-list', viewMode]">
          <div 
            v-for="assessment in paginatedAssessments" 
            :key="assessment.id"
            class="assessment-card"
            @click="navigateToPaperCenter(assessment)"
          >
            <div class="assessment-header">
              <div class="assessment-type">
                <span :class="['type-badge', assessment.type]">
                  <i :class="getTypeIcon(assessment.type)"></i>
                  {{ getTypeName(assessment.type) }}
                </span>
                <span :class="['status-badge', assessment.status]">
                  {{ getStatusName(assessment.status) }}
                </span>
              </div>
              <div class="assessment-actions" @click.stop>
                <el-dropdown trigger="click">
                  <el-button class="action-btn">
                    <i class="fas fa-ellipsis-v"></i>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item>
                        <i class="fas fa-edit"></i> 编辑
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <i class="fas fa-copy"></i> 复制
                      </el-dropdown-item>
                      <el-dropdown-item @click="handlePreviewAssessment(assessment.id)">
                        <i class="fas fa-eye"></i> 预览
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <i class="fas fa-chart-bar"></i> 统计
                      </el-dropdown-item>
                      <el-dropdown-item 
                        v-if="assessment.status === 'draft'" 
                        @click="handlePublishAssessment(assessment.id)"
                        divided
                      >
                        <i class="fas fa-rocket"></i> 发布
                      </el-dropdown-item>
                      <el-dropdown-item v-if="assessment.status === 'active'">
                        <i class="fas fa-stop"></i> 结束
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <i class="fas fa-archive"></i> 归档
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <i class="fas fa-trash"></i> 删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            
            <div class="assessment-content">
              <h4 class="assessment-title">{{ assessment.title }}</h4>
              <p class="assessment-description">{{ assessment.description }}</p>
              
              <div class="assessment-details">
                <div class="detail-row">
                  <div class="detail-item">
                    <i class="fas fa-book"></i>
                    <span>{{ assessment.course }}</span>
                  </div>
                  <div class="detail-item">
                    <i class="fas fa-question-circle"></i>
                    <span>{{ assessment.questionCount }} 题</span>
                  </div>
                  <div class="detail-item">
                    <i class="fas fa-clock"></i>
                    <span>{{ assessment.duration }} 分钟</span>
                  </div>
                  <div class="detail-item">
                    <i class="fas fa-star"></i>
                    <span>{{ assessment.totalScore }} 分</span>
                  </div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-item">
                    <i class="fas fa-calendar-alt"></i>
                    <span>{{ assessment.startTime }} - {{ assessment.endTime }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="assessment-footer">
              <div class="assessment-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ assessment.participants }}</span>
                  <span class="stat-label">参与人数</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ assessment.shouldParticipants }}</span>
                  <span class="stat-label">应参考人数</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ formatAverageScore(assessment.averageScore) }}</span>
                  <span class="stat-label">平均分</span>
                </div>
              </div>
              
              <div class="assessment-progress">
                <div class="progress-info">
                  <span class="progress-text">完成率</span>
                  <span class="progress-percent">{{ formatCompletionRate(assessment.completionRate) }}</span>
                </div>
                <el-progress 
                  :percentage="toProgressPercentage(assessment.completionRate)"
                  :stroke-width="6"
                  :show-text="false"
                  class="progress-bar"
                />
              </div>
            </div>
          </div>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="filteredAssessments.length"
            layout="total, sizes, prev, pager, next, jumper"
            class="custom-pagination"
          />
        </div>
      </div>
    </div>

    <!-- 创建测评对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      title="创建新测评" 
      width="900px"
      class="create-dialog"
      append-to-body
    >
      <div class="create-form">
        <el-form :model="newAssessment" :rules="formRules" ref="assessmentForm" label-width="120px">
          <div class="form-row">
            <el-form-item label="测评标题" prop="title" class="form-item">
              <el-input 
                v-model="newAssessment.title" 
                placeholder="例如：第一章 数据库基础 课后作业"
                class="custom-input"
              />
            </el-form-item>
            
            <el-form-item label="测评类型" prop="assessmentType" class="form-item">
              <el-select v-model="newAssessment.assessmentType" placeholder="请选择类型" class="custom-select">
                <el-option label="课堂作业" value="ClassroomExam" />
                <el-option label="章节作业" value="ChapterExam" />
                <el-option label="期中考试" value="MidtermExam" />
                <el-option label="期末考试" value="FinalExam" />
                <el-option label="作业" value="homework" />
              </el-select>
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="所属课程" prop="courseId" class="form-item">
              <el-select 
                v-model="newAssessment.courseId" 
                placeholder="请选择课程"
                @focus="handleCourseSelectFocus"
                @change="handleCourseChange"
                filterable
                clearable
                class="custom-select"
              >
                <el-option 
                  v-for="course in courseList" 
                  :key="getCourseId(course)" 
                  :label="getCourseTitle(course)" 
                  :value="getCourseId(course)"
                  class="course-option-item"
                >
                  <div class="course-option-content">
                    <div class="course-main-info">
                      <span class="course-name text-clamp-1" :title="getCourseTitle(course)">{{ getCourseTitle(course) }}</span>
                      <span class="course-status" :class="'status-' + getCourseStatus(course).toLowerCase()">
                        {{ getCourseStatusText(course) }}
                      </span>
                    </div>
                    <div class="course-sub-info">
                      <span class="course-id">ID: {{ getCourseId(course).substring(0, 15) }}...</span>
                    </div>
                  </div>
                </el-option>
                
                <!-- 如果没有课程数据，显示提示 -->
                <el-option 
                  v-if="courseList.length === 0" 
                  label="暂无课程数据，请先创建课程" 
                  value="" 
                  disabled
                />
              </el-select>
            </el-form-item>

            <!-- 选中课程的卡片显示 -->
            <div v-if="selectedCourse" class="selected-course-card">
              <div class="course-card">
                <div class="course-info">
                  <div class="course-header">
                    <h4 class="course-title">{{ getCourseTitle(selectedCourse) }}</h4>
                    <span class="course-status-badge" :class="'status-' + getCourseStatus(selectedCourse).toLowerCase()">
                      {{ getCourseStatusText(selectedCourse) }}
                    </span>
                  </div>
                  <p class="course-description">{{ getCourseDescription(selectedCourse) || '暂无描述' }}</p>
                  
                  <div v-if="getCourseCategory(selectedCourse)" class="course-category-info">
                    <i class="fas fa-tag"></i>
                    <span>{{ getCourseCategory(selectedCourse) }}</span>
                  </div>
                  
                  <div class="course-meta">
                    <span class="meta-item">
                      <i class="fas fa-calendar"></i>
                      创建时间: {{ formatCourseDate(selectedCourse.createTime) }}
                    </span>
                  </div>
                  
                  <div v-if="selectedCourse.tags && selectedCourse.tags.length > 0" class="course-tags">
                    <span v-for="tag in selectedCourse.tags.slice(0, 3)" :key="tag" class="course-tag">
                      {{ tag }}
                    </span>
                    <span v-if="selectedCourse.tags.length > 3" class="more-tags">
                      +{{ selectedCourse.tags.length - 3 }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            
            <el-form-item label="答题时长" prop="durationMinutes" class="form-item">
              <div style="display: flex; align-items: center;">
                <el-input-number 
                  v-model="newAssessment.durationMinutes" 
                  :min="1" 
                  :max="300"
                  placeholder="分钟"
                  class="custom-input-number"
                />
                <span class="input-suffix">分钟</span>
              </div>
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="开始时间" prop="startTime" class="form-item">
              <el-date-picker
                v-model="newAssessment.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
                class="custom-date-picker"
              />
            </el-form-item>
            
            <el-form-item label="结束时间" prop="endTime" class="form-item">
              <el-date-picker
                v-model="newAssessment.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
                class="custom-date-picker"
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="发布状态" prop="isPublished" class="form-item">
              <el-radio-group v-model="newAssessment.isPublished" class="custom-radio-group">
                <el-radio :label="0" class="custom-radio">
                  <i class="fas fa-save"></i>
                  暂存为草稿
                </el-radio>
                <el-radio :label="1" class="custom-radio">
                  <i class="fas fa-paper-plane"></i>
                  立即发布
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelCreate" class="cancel-btn">
            <i class="fas fa-times"></i>
            取消
          </el-button>
          <el-button type="primary" @click="createAssessment" class="create-btn">
            <i class="fas fa-plus"></i>
            {{ newAssessment.isPublished ? '创建并发布' : '保存草稿' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 试卷预览弹窗 -->
    <el-dialog 
      v-model="showPreviewDialog" 
      title="试卷预览" 
      width="1200px"
      class="preview-dialog"
      :before-close="() => { showPreviewDialog = false; previewPaperData = null }"
      append-to-body
    >
      <div v-if="previewPaperData" class="preview-container">
        <div class="preview-content">
          <!-- 左侧试卷内容 -->
          <div class="paper-content">
            <div class="paper-header">
              <h2 class="paper-title">{{ previewPaperData.title || '未命名试卷' }}</h2>
              <p v-if="previewPaperData.description" class="paper-description">
                {{ previewPaperData.description }}
              </p>
            </div>
            
            <div class="paper-sections">
              <div v-for="(section, sectionIndex) in previewPaperData.sections" :key="section.id" class="preview-section">
                <div class="section-header">
                  <h3 class="section-title">
                    <span class="section-number">第{{ sectionIndex + 1 }}部分</span>
                    <span class="section-name">{{ section.title || '未命名分组' }}</span>
                    <span v-if="section._stats" class="section-stats">
                      ({{ section._stats.questionCount }}题，共{{ section._stats.totalScore }}分)
                    </span>
                  </h3>
                  <p v-if="section.description" class="section-description">
                    {{ section.description }}
                  </p>
                </div>
                
                <div class="section-questions">
                  <div v-for="(question, questionIndex) in section.questions" :key="question.id" class="preview-question">
                    <div class="question-header">
                      <span class="question-number">{{ sectionIndex + 1 }}.{{ questionIndex + 1 }}</span>
                      <font-awesome-icon :icon="getQuestionIcon(question.type)" class="question-icon"/>
                      <span class="question-title">{{ question.title || '未选择题目' }}</span>
                      <span class="question-score">{{ question.score }}分</span>
                    </div>
                    <div class="question-type">{{ getQuestionTypeName(question.type) }}</div>
                    
                    <!-- 题目详细内容 -->
                    <div v-if="question.content" class="question-content">
                      <div class="content-text" v-html="question.content"></div>
                    </div>
                    
                    <!-- 选择题选项 -->
                    <div v-if="question.options && question.options.length > 0" class="question-options">
                      <div v-for="(option, optionIndex) in question.options" :key="optionIndex" class="option-item">
                        <span class="option-label">{{ String.fromCharCode(65 + optionIndex) }}.</span>
                        <span class="option-text">{{ typeof option === 'object' ? option.text : option }}</span>
                      </div>
                    </div>
                    
                    <!-- 答案和解析（仅在预览模式下显示） -->
                    <div class="question-meta">
                      <div v-if="question.answer" class="question-answer">
                        <strong>参考答案：</strong>{{ question.answer }}
                      </div>
                      <div v-if="question.explanation" class="question-explanation">
                        <strong>解析：</strong>{{ question.explanation }}
                      </div>
                    </div>
                  </div>
                  
                  <div v-if="!section.questions || section.questions.length === 0" class="no-questions">
                    <font-awesome-icon :icon="['fas', 'info-circle']"/>
                    该分组暂无题目
                  </div>
                </div>
              </div>
              
              <div v-if="!previewPaperData.sections || previewPaperData.sections.length === 0" class="empty-content">
                <font-awesome-icon :icon="['fas', 'file-alt']" class="empty-icon"/>
                <p>暂无试卷内容</p>
              </div>
            </div>
          </div>
          
          <!-- 右侧统计信息 -->
          <div class="preview-sidebar">
            <!-- 试卷统计 -->
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
                    <div class="stat-value">{{ previewPaperData.sections ? previewPaperData.sections.length : 0 }}</div>
                    <div class="stat-label">分组数</div>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon">
                    <font-awesome-icon :icon="['fas', 'question-circle']"/>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ previewTotalQuestions }}</div>
                    <div class="stat-label">题目数</div>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon">
                    <font-awesome-icon :icon="['fas', 'star']"/>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ previewTotalScore }}</div>
                    <div class="stat-label">总分</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 题型分布 -->
            <div class="distribution-card">
              <div class="distribution-header">
                <h3>
                  <font-awesome-icon :icon="['fas', 'pie-chart']"/>
                  题型分布
                </h3>
              </div>
              <div class="distribution-content">
                <div v-for="(item, index) in previewQuestionTypeDistribution" :key="index" class="distribution-item">
                  <div class="type-info">
                    <font-awesome-icon :icon="getQuestionIcon(item.type)" class="type-icon"/>
                    <span class="type-name">{{ getQuestionTypeName(item.type) }}</span>
                  </div>
                  <div class="type-stats">
                    <span class="type-count">{{ item.count }}题</span>
                    <span class="type-score">{{ item.totalScore }}分</span>
                  </div>
                </div>
                
                <div v-if="previewQuestionTypeDistribution.length === 0" class="distribution-empty">
                  <p>暂无题目</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="loading-content">
        <font-awesome-icon :icon="['fas', 'spinner']" spin class="loading-icon"/>
        <p>加载中...</p>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showPreviewDialog = false" class="close-btn">
            <font-awesome-icon :icon="['fas', 'times']"/>
            关闭
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyCourseList, getAllAssessments, getAssessmentResultSummary, publishAssessment, getPaperByAssessmentId } from '@/api/teacher/teacherAPI.js'

// 路由实例
const router = useRouter()

// 统计数据
const stats = ref({
  totalAssessments: 24,
  activeAssessments: 6,
  totalParticipants: 156,
  averageScore: 78
})

// 筛选条件
const filters = ref({
  type: '',
  status: '',
  course: '',
  dateRange: null,
  search: ''
})

// 视图模式和排序
const viewMode = ref('grid')
const sortBy = ref('createTime')

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 创建测评对话框
const showCreateDialog = ref(false)
const assessmentForm = ref(null)
const newAssessment = ref({
  title: '',
  courseId: '',
  assessmentType: '',
  startTime: null,
  endTime: null,
  durationMinutes: 60,
  isPublished: 0
})

// 课程相关数据
const courseList = ref([])
const selectedCourse = ref(null)

// 预览相关数据
const showPreviewDialog = ref(false)
const previewPaperData = ref(null)

// 预览数据的计算属性
const previewTotalQuestions = computed(() => {
  if (!previewPaperData.value) return 0
  
  // 优先使用处理后的统计数据
  if (previewPaperData.value._stats && previewPaperData.value._stats.totalQuestions !== undefined) {
    return previewPaperData.value._stats.totalQuestions
  }
  
  // 回退到原始计算方式
  if (!previewPaperData.value.sections) return 0
  return previewPaperData.value.sections.reduce((total, section) => {
    return total + (section.questions ? section.questions.length : 0)
  }, 0)
})

const previewTotalScore = computed(() => {
  if (!previewPaperData.value) return 0
  
  // 优先使用试卷的 totalScore 字段
  if (previewPaperData.value.totalScore !== undefined) {
    return previewPaperData.value.totalScore
  }
  
  // 回退到原始计算方式
  if (!previewPaperData.value.sections) return 0
  return previewPaperData.value.sections.reduce((total, section) => {
    if (!section.questions) return total
    return total + section.questions.reduce((sectionTotal, question) => {
      return sectionTotal + (question.score || 0)
    }, 0)
  }, 0)
})

const previewQuestionTypeDistribution = computed(() => {
  if (!previewPaperData.value) return []
  
  // 优先使用处理后的统计数据
  if (previewPaperData.value._stats && previewPaperData.value._stats.questionTypes) {
    const typeMap = previewPaperData.value._stats.questionTypes
    return Array.from(typeMap.entries()).map(([type, data]) => ({
      type,
      count: data.count,
      totalScore: data.totalScore
    }))
  }
  
  // 回退到原始计算方式
  if (!previewPaperData.value.sections) return []
  
  const typeMap = {}
  previewPaperData.value.sections.forEach(section => {
    if (section.questions) {
      section.questions.forEach(question => {
        if (!typeMap[question.type]) {
          typeMap[question.type] = { count: 0, totalScore: 0 }
        }
        typeMap[question.type].count++
        typeMap[question.type].totalScore += question.score || 0
      })
    }
  })
  
  return Object.entries(typeMap).map(([type, data]) => ({
    type,
    count: data.count,
    totalScore: data.totalScore
  }))
})

// 数据缓存机制
const dataCache = ref({
  assessments: {
    data: null,
    timestamp: null,
    expiry: 5 * 60 * 1000 // 5分钟缓存过期时间
  },
  courses: {
    data: null,
    timestamp: null,
    expiry: 10 * 60 * 1000 // 10分钟缓存过期时间
  },
  papers: new Map() // 试卷数据缓存，使用 Map 存储多个试卷
})

// 缓存工具函数
const isCacheValid = (cacheItem) => {
  if (!cacheItem.data || !cacheItem.timestamp) return false
  return Date.now() - cacheItem.timestamp < cacheItem.expiry
}

const setCacheData = (cacheKey, data) => {
  if (cacheKey === 'papers') {
    // 试卷缓存特殊处理
    return
  }
  dataCache.value[cacheKey] = {
    data: data,
    timestamp: Date.now(),
    expiry: dataCache.value[cacheKey].expiry
  }
}

const getCacheData = (cacheKey) => {
  if (cacheKey === 'papers') {
    // 试卷缓存特殊处理
    return null
  }
  const cacheItem = dataCache.value[cacheKey]
  return isCacheValid(cacheItem) ? cacheItem.data : null
}

// 试卷缓存专用函数
const setPaperCache = (assessmentId, paperData) => {
  dataCache.value.papers.set(assessmentId, {
    data: paperData,
    timestamp: Date.now(),
    expiry: 3 * 60 * 1000 // 3分钟缓存过期时间
  })
}

const getPaperCache = (assessmentId) => {
  const cacheItem = dataCache.value.papers.get(assessmentId)
  if (!cacheItem) return null
  
  if (Date.now() - cacheItem.timestamp > cacheItem.expiry) {
    dataCache.value.papers.delete(assessmentId)
    return null
  }
  
  return cacheItem.data
}

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入测评标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  courseId: [
    { required: true, message: '请选择所属课程', trigger: 'change' }
  ],
  assessmentType: [
    { required: true, message: '请选择测评类型', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  durationMinutes: [
    { required: true, message: '请设置答题时长', trigger: 'blur' },
    { type: 'number', min: 1, max: 300, message: '答题时长应在 1-300 分钟之间', trigger: 'blur' }
  ]
}

// 测评数据（将通过API从后端获取，这里保留空数组作为初始值）
const assessments = ref([
  // 示例数据结构（与后端Assessments.java字段匹配）：
  // {
  //   id: 'assessment_id_string',           // 测评ID (String)
  //   courseId: 'course_id_string',         // 课程ID (String) 
  //   creatorId: 'creator_id_string',       // 创建者ID (String)
  //   assessmentType: 'ClassroomExam',      // 测评类型 (String): ClassroomExam, ChapterExam, MidtermExam, FinalExam, homework
  //   title: '测评标题',                     // 测评标题 (String)
  //   startTime: new Date(),                // 开始时间 (Date)
  //   endTime: new Date(),                  // 结束时间 (Date)
  //   durationMinutes: 60,                  // 答题时长(分) (Integer)
  //   isPublished: 1,                       // 是否发布 (Integer): 0-未发布，1-发布，2-过期[逻辑删除]
  //   
  //   // 以下字段为前端显示需要，通过数据转换获得：
  //   description: '测评描述',               // 前端显示用描述（使用title）
  //   type: 'quiz',                         // 前端显示用类型（转换自assessmentType）
  //   status: 'published',                  // 前端显示用状态（转换自isPublished）
  //   course: '课程名称',                    // 前端显示用课程名（需要根据courseId获取）
  //   questionCount: 0,                     // 题目数量（需要从试卷数据获取）
  //   duration: 60,                         // 时长（等同于durationMinutes）
  //   totalScore: 0,                        // 总分（需要从试卷数据获取）
  //   participants: 0,                      // 参与人数（需要从参与记录获取）
  //   completed: 0,                         // 完成人数（需要从参与记录获取）
  //   averageScore: 0,                      // 平均分（需要从成绩统计获取）
  //   createTime: '2024-01-10'              // 创建时间（格式化显示）
  // }
])

// 筛选后的测评
const filteredAssessments = computed(() => {
  let result = assessments.value
  
  if (filters.value.type) {
    result = result.filter(a => a.type === filters.value.type)
  }
  
  if (filters.value.status) {
    result = result.filter(a => a.status === filters.value.status)
  }
  
  if (filters.value.course) {
    result = result.filter(a => a.course === filters.value.course)
  }
  
  if (filters.value.search) {
    const search = filters.value.search.toLowerCase()
    result = result.filter(a => 
      a.title.toLowerCase().includes(search) ||
      a.description.toLowerCase().includes(search) ||
      a.course.toLowerCase().includes(search)
    )
  }
  
  return result
})

// 分页后的测评
const paginatedAssessments = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredAssessments.value.slice(start, end)
})

// 获取测评类型图标
const getTypeIcon = (type) => {
  const icons = {
    quiz: 'fas fa-question-circle',
    test: 'fas fa-clipboard-check',
    midterm: 'fas fa-graduation-cap',
    final: 'fas fa-certificate',
    homework: 'fas fa-tasks'
  }
  return icons[type] || 'fas fa-clipboard-list'
}

// 获取测评类型名称
const getTypeName = (type) => {
  const names = {
    quiz: '课堂测验',
    test: '章节测试',
    midterm: '期中考试',
    final: '期末考试',
    homework: '作业'
  }
  return names[type] || '未知'
}

// 获取状态名称
const getStatusName = (status) => {
  const names = {
    draft: '草稿',
    published: '已发布',
    active: '进行中',
    ended: '已结束',
    archived: '已归档'
  }
  return names[status] || '未知'
}

/**
 * 将完成率（0~1）转换为进度条百分比（0~100）
 * @param {number} completionRate 完成率
 * @returns {number} 百分比数值
 */
const toProgressPercentage = (completionRate) => {
  const normalized = Number(completionRate)
  if (!Number.isFinite(normalized) || normalized <= 0) {
    return 0
  }
  const percentage = normalized * 100
  return Number(Math.min(100, Math.max(0, percentage)).toFixed(2))
}

/**
 * 格式化完成率展示文本
 * @param {number} completionRate 完成率
 * @returns {string} 百分比字符串
 */
const formatCompletionRate = (completionRate) => `${toProgressPercentage(completionRate)}%`

/**
 * 格式化平均分展示文本
 * @param {number} averageScore 平均分
 * @returns {string} 保留两位小数的平均分
 */
const formatAverageScore = (averageScore) => {
  const normalized = Number(averageScore)
  return Number.isFinite(normalized) ? normalized.toFixed(2) : '0.00'
}

// 重置筛选条件
const resetFilters = () => {
  filters.value = {
    type: '',
    status: '',
    course: '',
    dateRange: null,
    search: ''
  }
}

// 取消创建
const cancelCreate = () => {
  showCreateDialog.value = false
  resetForm()
}

// 创建测评
const createAssessment = async () => {
  if (!assessmentForm.value) return
  
  try {
    // 表单验证
    await assessmentForm.value.validate()
    
    // 时间验证
    if (new Date(newAssessment.value.startTime) >= new Date(newAssessment.value.endTime)) {
      ElMessage.error('结束时间必须晚于开始时间')
      return
    }
    
    // 构建符合后端AssessmentCreateDTO规范的数据结构
    const assessmentData = {
      // 课程ID (必填) - String类型
      courseId: newAssessment.value.courseId,
      
      // 测评类型 (必填) - 确保与AssessmentType枚举值一致
      // 前端选项值已与后端枚举值保持一致：ClassroomExam、ChapterExam、MidtermExam、FinalExam、homework
      assessmentType: newAssessment.value.assessmentType,
      
      // 测评标题 (必填) - String类型
      title: newAssessment.value.title,
      
      // 测评开始时间 (必填) - LocalDateTime格式
      // 后端期望LocalDateTime，前端传递ISO格式字符串，Spring Boot会自动转换
      startTime: newAssessment.value.startTime,
      
      // 测评结束时间 (必填) - LocalDateTime格式
      endTime: newAssessment.value.endTime,
      
      // 答题时长(分钟) (必填) - Integer类型
      durationMinutes: parseInt(newAssessment.value.durationMinutes),
      
      // 是否发布 (必填) - Integer类型
      // 0-暂存为草稿，1-正式发布，2-删除
      isPublished: parseInt(newAssessment.value.isPublished)
      
      // 注意：creatorId由后端API自动从token获取，前端无需传递
    }
    
    // 数据验证：确保所有必填字段都有值
    const requiredFields = ['courseId', 'assessmentType', 'title', 'startTime', 'endTime', 'durationMinutes', 'isPublished']
    for (const field of requiredFields) {
      if (assessmentData[field] === null || assessmentData[field] === undefined || assessmentData[field] === '') {
        ElMessage.error(`${field} 字段不能为空`)
        return
      }
    }
    
    // 调用API创建测评
    const { createAssessment: createAssessmentAPI } = await import('@/api/teacher/teacherAPI.js')
    
    const result = await createAssessmentAPI(assessmentData)
    
    // 成功提示
    ElMessage.success(assessmentData.isPublished === 1 ? '测评创建并发布成功！' : '测评保存为草稿成功！')
    
    // 关闭对话框并重置表单
    // showCreateDialog.value = false
    // resetForm()
    
    // 跳转到试卷中心页面，传递完整的测评数据对象
    if (result && result.id) {
      // 构建完整的测评数据对象，包含后端返回的所有数据和前端表单数据
      router.push({
        path: '/teacher/paper-center',
        // 保留基本query参数作为备用
        query: {
          assessmentId: result.id,
          courseId: assessmentData.courseId
        },
      })
    } else {
      ElMessage.warning('测评创建成功，但无法获取测评ID，请手动进入试卷编辑页面')
    }
    
    // 刷新测评列表（这里可以调用获取测评列表的方法）
    // await loadAssessments()
    
  } catch (error) {
    console.error('创建测评失败:', error)
    ElMessage.error(error.message || '创建测评失败，请重试')
  }
}

// 重置表单
const resetForm = () => {
  if (assessmentForm.value) {
    assessmentForm.value.resetFields()
  }
  newAssessment.value = {
    title: '',
    courseId: '',
    assessmentType: '',
    startTime: null,
    endTime: null,
    durationMinutes: 60,
    isPublished: 0
  }
  selectedCourse.value = null
}

// 课程选择器焦点事件处理
const handleCourseSelectFocus = async () => {
  console.log('课程选择器获得焦点，重新加载课程列表')
  await loadCourses()
}

// 课程选择变化处理
const handleCourseChange = (courseId) => {
  console.log('课程选择变化:', courseId)
  if (courseId) {
    // 根据选中的课程ID找到对应的课程对象
    const course = courseList.value.find(c => getCourseId(c) === String(courseId))
    if (course) {
      selectedCourse.value = course
      console.log('选中课程:', course)
    }
  } else {
    selectedCourse.value = null
    console.log('清除课程选择')
  }
}

// 格式化课程日期
const formatCourseDate = (dateString) => {
  if (!dateString) return '未知'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  } catch (error) {
    console.error('日期格式化错误:', error)
    return '未知'
  }
}

// 获取课程ID的工具函数
const getCourseId = (course) => {
  if (!course) return ''
  return String(course.courseId || course.id || '')
}

// 获取课程标题的工具函数
const getCourseTitle = (course) => {
  if (!course) return ''
  return course.title || course.name || course.courseTitle || course.courseName || '未知课程'
}

// 获取课程描述的工具函数
const getCourseDescription = (course) => {
  if (!course) return ''
  return course.description || course.summary || ''
}

// 获取课程状态的工具函数
const getCourseStatus = (course) => {
  if (!course) return 'UNKNOWN'
  const rawStatus = course.status || course.courseStatus
  if (!rawStatus) return 'unknown'
  const normalized = String(rawStatus).trim().toLowerCase()
  if (normalized === 'published' || normalized === 'draft' || normalized === 'archived' || normalized === 'reviewing' || normalized === 'rejected') {
    return normalized
  }
  if (rawStatus === '已发布') return 'published'
  if (rawStatus === '草稿') return 'draft'
  if (rawStatus === '已归档') return 'archived'
  if (rawStatus === '审核中') return 'reviewing'
  if (rawStatus === '已拒绝') return 'rejected'
  return 'unknown'
}

// 获取课程状态显示文本的工具函数
const getCourseStatusText = (course) => {
  const status = getCourseStatus(course)
  const statusMap = {
    published: '已发布',
    draft: '草稿',
    reviewing: '审核中',
    rejected: '已拒绝',
    archived: '已归档',
    unknown: '未知状态'
  }
  return statusMap[status] || '未知状态'
}

// 获取课程分类的工具函数
const getCourseCategory = (course) => {
  if (!course) return ''
  return course.category || course.categoryName || ''
}

/**
 * 处理课程数据的工具函数
 * 根据CourseBaseDTO数据结构处理课程信息
 * CourseBaseDTO结构: { id: String, name: String }
 * @param {Object} courseData - 课程数据对象，可能是CourseBaseDTO或包含CourseBaseDTO的复杂对象
 * @returns {Object} 处理后的课程数据
 */
const processCourseData = (courseData) => {
  if (!courseData || typeof courseData !== 'object') {
    console.warn('课程数据无效:', courseData)
    return {
      id: null,
      courseId: null,
      name: '未知课程',
      title: '未知课程',
      description: '',
      category: '',
      tags: [],
      createTime: null,
      updateTime: null,
      status: 'UNKNOWN'
    }
  }

  console.log('处理课程数据:', courseData)

  // 处理可能的嵌套结构
  let processedCourse = courseData
  
  // 如果存在 courseBase 字段，优先使用（对应CourseBaseDTO结构）
  if (courseData.courseBase && typeof courseData.courseBase === 'object') {
    processedCourse = courseData.courseBase
    console.log('使用嵌套的 courseBase 数据:', processedCourse)
  }
  
  // 如果存在 course 字段，使用该字段
  if (courseData.course && typeof courseData.course === 'object') {
    processedCourse = courseData.course
    console.log('使用嵌套的 course 数据:', processedCourse)
  }

  // 根据CourseBaseDTO的实际结构处理数据
  const result = {
    // 基本标识信息 - 对应CourseBaseDTO的id字段
    id: processedCourse.id || processedCourse.courseId || null,
    courseId: processedCourse.courseId || processedCourse.id || null,
    
    // 基本信息 - 对应CourseBaseDTO的name字段
    name: processedCourse.name || processedCourse.title || processedCourse.courseTitle || processedCourse.courseName || '未知课程',
    title: processedCourse.title || processedCourse.courseTitle || processedCourse.name || processedCourse.courseName || '未知课程',
    
    // 以下字段为扩展字段，用于兼容前端显示需求
    description: processedCourse.description || processedCourse.courseDescription || processedCourse.summary || '',
    category: processedCourse.category || processedCourse.categoryName || '',
    tags: Array.isArray(processedCourse.tags) ? processedCourse.tags : 
          Array.isArray(processedCourse.keywords) ? processedCourse.keywords : [],
    
    // 状态信息
    status: processedCourse.status || processedCourse.courseStatus || processedCourse.auditStatus || 'UNKNOWN',
    isPublished: Boolean(processedCourse.isPublished || processedCourse.status === 'PUBLISHED' || processedCourse.courseStatus === 'PUBLISHED'),
    
    // 时间信息
    createTime: processedCourse.createTime || processedCourse.createdAt || null,
    updateTime: processedCourse.updateTime || processedCourse.updatedAt || null,
    
    // 教学信息（扩展字段）
    grade: processedCourse.grade || '',
    subject: processedCourse.subject || '',
    difficulty: processedCourse.difficulty || '',
    
    // 统计信息（扩展字段）
    studentCount: Number(processedCourse.studentCount) || 0,
    lessonCount: Number(processedCourse.lessonCount || processedCourse.totalSections) || 0,
    
    // 保留原始数据用于调试
    _original: courseData
  }

  console.log('课程数据处理结果:', result)
  return result
}

// 加载课程列表
const loadCourses = async () => {
  try {
    console.log('开始加载课程列表...')
    
    // 检查缓存
    const cachedCourses = getCacheData('courses')
    if (cachedCourses) {
      courseList.value = cachedCourses
      console.log('从缓存加载课程列表，共', cachedCourses.length, '门课程')
      return
    }
    
    // 使用Promise包装getMyCourseList调用
    const courseData = await new Promise((resolve, reject) => {
      getMyCourseList(
        (data) => {
          console.log('课程列表API返回数据:', data)
          resolve(data)
        },
        (message, code) => {
          console.error('课程列表API失败:', message, code)
          reject(new Error(message || '获取课程列表失败'))
        }
      )
    })
    
    // 处理返回的课程数据
    let rawCourseList = []
    
    if (Array.isArray(courseData)) {
      rawCourseList = courseData
      console.log('课程列表为数组格式，共', courseData.length, '门课程')
    } else if (courseData && typeof courseData === 'object') {
      // 如果返回的是对象，尝试提取课程数组
      if (Array.isArray(courseData.courses)) {
        rawCourseList = courseData.courses
      } else if (Array.isArray(courseData.data)) {
        rawCourseList = courseData.data
      } else if (courseData.data && Array.isArray(courseData.data.records)) {
        rawCourseList = courseData.data.records
      } else if (courseData.data && Array.isArray(courseData.data.list)) {
        rawCourseList = courseData.data.list
      } else if (Array.isArray(courseData.records)) {
        rawCourseList = courseData.records
      } else if (Array.isArray(courseData.list)) {
        rawCourseList = courseData.list
      } else {
        rawCourseList = [courseData]
      }
      console.log('课程列表为对象格式，提取后共', rawCourseList.length, '门课程')
    } else {
      rawCourseList = []
      console.warn('课程列表数据格式异常:', courseData)
    }
    
    // 使用增强的处理函数处理每个课程数据
    const processedCourseList = rawCourseList.map((course, index) => {
      console.log(`处理第 ${index + 1} 门课程:`, course)
      return processCourseData(course)
    }).filter(course => course.id !== null) // 过滤掉无效的课程数据
    
    courseList.value = processedCourseList
    setCacheData('courses', processedCourseList) // 设置缓存
    console.log('课程列表处理完成，有效课程', processedCourseList.length, '门')
    
    // 如果课程列表为空，显示提示
    if (courseList.value.length === 0) {
      ElMessage.warning('暂无课程数据，请先创建课程')
    }
    
  } catch (error) {
    console.error('加载课程列表异常:', error)
    ElMessage.error('加载课程列表失败: ' + (error.message || '网络异常，请重试'))
    courseList.value = []
  }
}

// 加载测评列表
const loadAssessments = async () => {
  try {
    console.log('开始加载测评列表...')
    
    // 检查缓存
    const cachedAssessments = getCacheData('assessments')
    if (cachedAssessments) {
      assessments.value = cachedAssessments
      console.log('从缓存加载测评列表，共', cachedAssessments.length, '个测评')
      updateStats()
      return
    }
    
    // 调用getAllAssessments API获取测评数据
    const assessmentData = await getAllAssessments()
    console.log('测评列表API返回数据:', assessmentData)
    
    // 处理返回的测评数据
    if (Array.isArray(assessmentData)) {
      // 将后端 AssessmentTeacherDTO 数据转换为前端需要的格式
      const baseAssessments = assessmentData
        .map(assessment => processAssessmentTeacherDTO(assessment))
        .filter(assessment => assessment && assessment.id)
      assessments.value = await attachAssessmentResultSummary(baseAssessments)
      setCacheData('assessments', assessments.value) // 设置缓存
      
      console.log('测评列表加载成功，共', assessments.value.length, '个测评')
      
      // 更新统计数据
      updateStats()
      
    } else if (assessmentData && typeof assessmentData === 'object') {
      // 如果返回的是对象，尝试提取测评数组
      const dataArray = assessmentData.assessments || assessmentData.data || [assessmentData]
      const baseAssessments = dataArray
        .map(assessment => processAssessmentTeacherDTO(assessment))
        .filter(assessment => assessment && assessment.id)
      assessments.value = await attachAssessmentResultSummary(baseAssessments)
      setCacheData('assessments', assessments.value) // 设置缓存
      
      console.log('测评列表加载成功（对象格式），共', assessments.value.length, '个测评')
      updateStats()
      
    } else {
      assessments.value = []
      console.warn('测评列表数据格式异常:', assessmentData)
    }
    
    // 如果测评列表为空，显示提示
    if (assessments.value.length === 0) {
      ElMessage.info('暂无测评数据')
    }
    
  } catch (error) {
    console.error('加载测评列表异常:', error)
    ElMessage.error('加载测评列表失败: ' + (error.message || '网络异常，请重试'))
    assessments.value = []
  }
}

// 映射测评类型：后端枚举值到前端显示值
const mapAssessmentType = (backendType) => {
  const typeMap = {
    'ClassroomExam': 'quiz',      // 课堂作业 -> 课堂测验
    'ChapterExam': 'test',        // 章节作业 -> 章节测试
    'MidtermExam': 'midterm',     // 期中考试 -> 期中考试
    'FinalExam': 'final',         // 期末考试 -> 期末考试
    'homework': 'homework'        // 作业 -> 作业
  }
  return typeMap[backendType] || 'quiz'
}

// 映射测评状态：后端isPublished字段到前端状态
const mapAssessmentStatus = (isPublished) => {
  const statusMap = {
    0: 'draft',      // 未发布 -> 草稿
    1: 'published',  // 已发布 -> 已发布
    2: 'archived'    // 过期/逻辑删除 -> 已归档
  }
  return statusMap[isPublished] || 'draft'
}

/**
 * 为测评列表补充后端返回的结果汇总指标
 * @param {Array<Object>} assessmentList 测评列表
 * @returns {Promise<Array<Object>>} 包含统计指标的测评列表
 */
const attachAssessmentResultSummary = async (assessmentList) => {
  if (!Array.isArray(assessmentList) || assessmentList.length === 0) {
    return []
  }

  const enhancedAssessments = await Promise.all(
    assessmentList.map(async (assessment) => {
      try {
        const summary = await getAssessmentResultSummary(assessment.id)
        return {
          ...assessment,
          participants: Number(summary.actualParticipantCount) || 0,
          shouldParticipants: Number(summary.shouldParticipantCount) || 0,
          averageScore: Number(summary.averageScore) || 0,
          completionRate: Number(summary.completionRate) || 0
        }
      } catch (error) {
        console.warn('获取测评结果汇总失败，使用默认值:', assessment.id, error)
        return {
          ...assessment,
          participants: 0,
          shouldParticipants: 0,
          averageScore: 0,
          completionRate: 0
        }
      }
    })
  )

  return enhancedAssessments
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  try {
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    console.error('日期时间格式化错误:', error)
    return ''
  }
}

// 格式化日期
const formatDate = (dateTime) => {
  if (!dateTime) return ''
  try {
    const date = new Date(dateTime)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  } catch (error) {
    console.error('日期格式化错误:', error)
    return ''
  }
}

// 更新统计数据
const updateStats = () => {
  const total = assessments.value.length
  const active = assessments.value.filter(a => a.status === 'published' || a.status === 'active').length
  const totalParticipants = assessments.value.reduce((sum, a) => sum + a.participants, 0)
  const totalScores = assessments.value.filter(a => a.averageScore > 0)
  const averageScore = totalScores.length > 0 
    ? Math.round(totalScores.reduce((sum, a) => sum + a.averageScore, 0) / totalScores.length)
    : 0
  
  stats.value = {
    totalAssessments: total,
    activeAssessments: active,
    totalParticipants: totalParticipants,
    averageScore: averageScore
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

// 预览测评
const handlePreviewAssessment = async (assessmentId) => {
  // 参数验证
  if (!assessmentId) {
    ElMessage.error('测评ID不能为空')
    return
  }

  // 显示预览对话框和加载状态
  showPreviewDialog.value = true
  previewPaperData.value = null

  try {
    // 检查试卷缓存
    const cachedPaper = getPaperCache(assessmentId)
    if (cachedPaper) {
      previewPaperData.value = cachedPaper
      console.log('从缓存加载试卷数据:', assessmentId)
      return
    }
    
    // 显示加载状态
    const loadingMessage = ElMessage({
      message: '正在加载试卷数据...',
      type: 'info',
      duration: 0
    })
    
    // 调用API获取试卷数据
    const paperData = await getPaperByAssessmentId(String(assessmentId))
    
    // 关闭加载提示
    loadingMessage.close()
    
    // 数据验证和处理
    if (!paperData) {
      throw new Error('未获取到试卷数据')
    }

    // 处理 PaperDTO 数据结构
    const processedPaperData = processPaperData(paperData)
    
    // 设置缓存
    setPaperCache(assessmentId, processedPaperData)
    
    // 设置预览数据
    previewPaperData.value = processedPaperData
    
    console.log('试卷预览数据加载成功:', processedPaperData)
    
  } catch (error) {
    console.error('获取试卷预览数据失败:', error)
    ElMessage.error(error.message || '获取试卷预览数据失败，请稍后重试')
    
    // 关闭预览对话框
    showPreviewDialog.value = false
    previewPaperData.value = null
  }
}

// 处理 PaperDTO 数据结构的函数
const processPaperData = (paperData) => {
  if (!paperData) {
    console.warn('试卷数据为空')
    return null
  }

  console.log('开始处理试卷数据:', paperData)

  // 确保基本字段存在
  const processedData = {
    id: paperData.id || '',
    title: paperData.title || '未命名试卷',
    description: paperData.description || '',
    totalScore: paperData.totalScore || 0,
    createdAt: paperData.createdAt || null,
    updatedAt: paperData.updatedAt || null,
    sections: [],
    // 添加统计信息
    _stats: {
      totalQuestions: 0,
      totalSections: 0,
      questionTypes: new Map()
    }
  }

  // 处理 sections 数组
  if (paperData.sections && Array.isArray(paperData.sections)) {
    console.log('处理试卷分组，共', paperData.sections.length, '个分组')
    
    processedData.sections = paperData.sections.map((section, sectionIndex) => {
      // 验证分组数据
      if (!section || typeof section !== 'object') {
        console.warn(`分组 ${sectionIndex} 数据无效:`, section)
        return null
      }

      const processedSection = {
        id: section.id || `section_${sectionIndex}`,
        title: section.title || `第${sectionIndex + 1}部分`,
        description: section.description || '',
        questions: [],
        // 分组统计
        _stats: {
          questionCount: 0,
          totalScore: 0
        }
      }

      // 处理 questions 数组
      if (section.questions && Array.isArray(section.questions)) {
        console.log(`分组 ${sectionIndex} 包含 ${section.questions.length} 个题目`)
        
        processedSection.questions = section.questions.map((questionItem, questionIndex) => {
          // 验证题目数据
          if (!questionItem || typeof questionItem !== 'object') {
            console.warn(`分组 ${sectionIndex} 题目 ${questionIndex} 数据无效:`, questionItem)
            return null
          }

          // 处理 PaperQuestion 结构
          const processedQuestion = {
            id: questionItem.question?.id || `question_${sectionIndex}_${questionIndex}`,
            title: questionItem.question?.stem || questionItem.question?.title || '未选择题目',
            type: questionItem.question?.type || 'UNKNOWN',
            score: Number(questionItem.score) || 0,
            orderIndex: questionItem.orderIndex !== undefined ? questionItem.orderIndex : questionIndex,
            // 保留完整的 question 对象以备后用
            question: questionItem.question || {},
            // 添加题目详细信息
            content: questionItem.question?.content || '',
            options: questionItem.question?.options || [],
            answer: questionItem.question?.answer || '',
            explanation: questionItem.question?.explanation || ''
          }

          // 更新分组统计
          processedSection._stats.questionCount++
          processedSection._stats.totalScore += processedQuestion.score

          // 更新全局统计
          processedData._stats.totalQuestions++
          
          // 统计题目类型
          const typeCount = processedData._stats.questionTypes.get(processedQuestion.type) || { count: 0, totalScore: 0 }
          typeCount.count++
          typeCount.totalScore += processedQuestion.score
          processedData._stats.questionTypes.set(processedQuestion.type, typeCount)

          return processedQuestion
        }).filter(question => question !== null) // 过滤掉无效题目
      } else {
        console.log(`分组 ${sectionIndex} 没有题目`)
      }

      return processedSection
    }).filter(section => section !== null) // 过滤掉无效分组

    processedData._stats.totalSections = processedData.sections.length
  } else {
    console.warn('试卷没有分组数据')
  }

  // 如果没有从题目计算出总分，使用试卷的 totalScore
  if (processedData._stats.totalQuestions > 0) {
    const calculatedScore = processedData.sections.reduce((total, section) => {
      return total + section._stats.totalScore
    }, 0)
    
    if (calculatedScore !== processedData.totalScore) {
      console.log(`试卷总分不一致: 计算值=${calculatedScore}, 原始值=${processedData.totalScore}`)
      processedData.totalScore = calculatedScore // 使用计算值
    }
  }

  console.log('试卷数据处理完成:', processedData)
  return processedData
}

// 处理 AssessmentTeacherDTO 数据结构
const processAssessmentTeacherDTO = (assessment) => {
  // 参数验证
  if (!assessment || typeof assessment !== 'object') {
    console.warn('无效的测评数据:', assessment)
    return null
  }

  // 处理课程信息
  const courseInfo = processCourseData(assessment.course)

  // 转换为前端需要的格式
  const processedAssessment = {
    // 基本信息
    id: assessment.id || null,
    title: assessment.title || '未命名测评',
    description: assessment.title || '未命名测评', // 使用 title 作为描述
    
    // 测评类型和状态
    type: mapAssessmentType(assessment.assessmentType),
    status: mapAssessmentStatus(assessment.isPublished),
    assessmentType: assessment.assessmentType || 'UNKNOWN',
    isPublished: Boolean(assessment.isPublished),
    
    // 课程信息（来自 CourseBaseDTO）
    course: courseInfo.name,
    courseId: courseInfo.id,
    courseTitle: courseInfo.title,
    
    // 创建者信息
    creatorId: assessment.creatorId || null,
    
    // 时间信息
    startTime: formatDateTime(assessment.startTime),
    endTime: formatDateTime(assessment.endTime),
    createTime: formatDate(assessment.startTime), // 使用 startTime 作为创建时间
    
    // 持续时间
    duration: assessment.durationMinutes || 0,
    durationMinutes: assessment.durationMinutes || 0,
    
    // 默认值（需要从后端统计接口补充）
    questionCount: 0, // 需要从试卷数据获取
    totalScore: 0, // 需要从试卷数据获取
    participants: 0, // 实际参考人数
    shouldParticipants: 0, // 应参考人数（课程学习人数）
    averageScore: 0, // 平均分（测评总分/应参考人数）
    completionRate: 0, // 完成率（实际参考人数/应参考人数）
    
    // 保留原始数据以备后用
    _originalData: assessment
  }

  return processedAssessment
}

/**
 * 跳转到试卷中心进行组卷操作
 * @param {Object} assessment - 测评对象
 * @param {string} assessment.id - 测评ID
 * @param {string} assessment.title - 测评标题
 * @param {string} assessment.courseId - 课程ID
 * @returns {void}
 */
const navigateToPaperCenter = (assessment) => {
  try {
    // 详细的参数验证
    if (!assessment) {
      ElMessage.error('测评信息为空，无法进行组卷操作')
      console.error('navigateToPaperCenter: assessment 参数为空')
      return
    }

    if (!assessment.id) {
      ElMessage.error('测评ID缺失，无法进行组卷操作')
      console.error('navigateToPaperCenter: assessment.id 缺失', assessment)
      return
    }

    if (!assessment.title || assessment.title.trim() === '') {
      ElMessage.warning('测评标题为空，将使用默认标题')
      console.warn('navigateToPaperCenter: assessment.title 为空', assessment)
    }

    // 获取课程ID，优先使用当前数据，其次使用原始数据
    const courseId = assessment.courseId || assessment._originalData?.courseId
    if (!courseId) {
      ElMessage.warning('课程信息缺失，可能影响组卷功能')
      console.warn('navigateToPaperCenter: courseId 缺失', assessment)
    }

    console.log('跳转到试卷中心，测评信息:', {
      id: assessment.id,
      title: assessment.title,
      courseId: courseId,
      hasOriginalData: !!assessment._originalData
    })

    // 构建完整的测评数据对象
    const assessmentData = {
      id: assessment.id,
      title: assessment.title || '未命名测评',
      courseId: courseId,
      assessmentType: assessment.assessmentType || assessment._originalData?.assessmentType || 'exam',
      startTime: assessment.startTime || assessment._originalData?.startTime,
      endTime: assessment.endTime || assessment._originalData?.endTime,
      durationMinutes: assessment.durationMinutes || assessment._originalData?.durationMinutes || 60,
      isPublished: assessment.isPublished !== undefined ? assessment.isPublished : 
                   (assessment._originalData?.isPublished !== undefined ? assessment._originalData.isPublished : false),
      description: assessment.description || assessment._originalData?.description || '',
      // 保留完整的原始数据以备后用
      _originalData: assessment._originalData || assessment,
      // 添加时间戳用于调试
      _transferTime: new Date().toISOString()
    }

    // 构建跳转参数
    const routeParams = {
      path: '/teacher/paper-center',
      query: {
        assessmentId: assessment.id,
        courseId: courseId,
        title: assessment.title || '未命名测评'
      },
      state: {
        assessmentData: assessmentData,
        isFromCreate: false, // 标识这是从测评管理页面跳转，而非创建页面
        sourceRoute: 'assessment-management' // 标识来源路由
      }
    }

    // 验证路由参数的完整性
    if (!routeParams.query.assessmentId) {
      throw new Error('路由参数中缺少 assessmentId')
    }

    console.log('准备跳转，路由参数:', routeParams)

    // 执行路由跳转
    router.push(routeParams)
    
    ElMessage.success('正在跳转到试卷中心...')
    
  } catch (error) {
    console.error('跳转到试卷中心失败:', error)
    ElMessage.error(`跳转失败: ${error.message || '未知错误'}，请重试`)
    
    // 可选：提供降级方案
    if (assessment?.id) {
      console.log('尝试降级跳转方案...')
      try {
        router.push({
          path: '/teacher/paper-center',
          query: {
            assessmentId: assessment.id,
            title: assessment.title || '未命名测评'
          }
        })
        ElMessage.info('使用简化模式跳转到试卷中心')
      } catch (fallbackError) {
        console.error('降级跳转也失败:', fallbackError)
        ElMessage.error('跳转完全失败，请检查网络连接或刷新页面重试')
      }
    }
  }
}

// 发布测评
const handlePublishAssessment = async (assessmentId) => {
  try {
    // 参数验证
    if (!assessmentId) {
      ElMessage.error('测评ID不能为空')
      return
    }
    
    // 确认对话框
    await ElMessageBox.confirm(
      '确定要发布这个测评吗？发布后学生将可以看到并参与测评。',
      '确认发布',
      {
        confirmButtonText: '确定发布',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    // 显示加载状态
    const loadingMessage = ElMessage({
      message: '正在发布测评...',
      type: 'info',
      duration: 0
    })
    
    // 调用API发布测评，确保参数类型为字符串
    const result = await publishAssessment(String(assessmentId))
    
    // 关闭加载提示
    loadingMessage.close()
    
    // 显示成功消息
    ElMessage.success(result || '测评发布成功')
    
    // 重新加载测评列表以更新状态
    await loadAssessments()
    
  } catch (error) {
    console.error('发布测评失败:', error)
    
    // 如果是用户取消操作，不显示错误消息
    if (error.message && error.message.includes('cancel')) {
      return
    }
    
    // 显示错误消息
    ElMessage.error('发布测评失败: ' + (error.message || '网络异常，请重试'))
  }
}

onMounted(() => {
  // 组件挂载时加载课程列表和测评列表
  loadCourses()
  loadAssessments()
})
</script>

<style scoped>
:root {
  --primary-gradient: linear-gradient(135deg, #0061ff 0%, #60efff 100%);
  --glass-bg: rgba(255, 255, 255, 0.65);
  --glass-border: rgba(255, 255, 255, 0.4);
  --glass-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.1);
  --text-primary: #2c3e50;
  --text-secondary: #606266;
  --primary-color: #002FA7; /* Klein Blue */
  --success-color: #67C23A;
  --warning-color: #E6A23C;
  --danger-color: #F56C6C;
  --info-color: #909399;
}

.assessment-management-container {
  min-height: 100vh;
  background-color: transparent;
  padding: 24px;
  font-family: 'Inter', 'PingFang SC', sans-serif;
}

/* Glass Components */
.glass-panel {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 20px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.05);
}

.glass-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.glass-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
  border-color: rgba(255, 255, 255, 0.8);
}

.hover-lift {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

/* 页面头部 */
.page-header {
  padding: 24px 32px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  position: relative;
  z-index: 10;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 20px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.05);
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 16px;
  letter-spacing: -0.5px;
}

.page-title i {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  color: #0284c7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(2, 132, 199, 0.15);
}

.page-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
  margin-top: 4px;
  margin-left: 64px;
}

.header-actions {
  display: flex;
  gap: 16px;
}

.action-btn {
  padding: 10px 24px;
  border-radius: 12px;
  font-weight: 600;
  height: 44px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.template-btn {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.6);
  color: var(--text-primary);
  backdrop-filter: blur(4px);
}

.template-btn:hover {
  background: rgba(255, 255, 255, 0.8);
  color: #2563eb;
  border-color: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.1);
}

.create-btn {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  color: white;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.4);
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
  border-color: rgba(255, 255, 255, 0.8);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

/* Specific gradients for stat icons */
.stat-card:nth-child(1) .stat-icon { background: linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%); }
.stat-card:nth-child(2) .stat-icon { background: linear-gradient(135deg, #10b981 0%, #34d399 100%); }
.stat-card:nth-child(3) .stat-icon { background: linear-gradient(135deg, #8b5cf6 0%, #d946ef 100%); }
.stat-card:nth-child(4) .stat-icon { background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%); }

.stat-content {
  display: flex;
  flex-direction: column;
  z-index: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

/* 筛选区域 */
.filter-section {
  margin-bottom: 32px;
}

.filter-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
}

.filter-row {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 180px;
}

.filter-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-left: 4px;
}

.filter-select :deep(.el-input__wrapper),
.filter-date :deep(.el-input__wrapper),
.search-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5);
  box-shadow: none !important;
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 12px;
  padding: 8px 16px;
  height: 40px;
  transition: all 0.3s;
}

.filter-select :deep(.el-input__wrapper:hover),
.filter-date :deep(.el-input__wrapper:hover),
.search-input :deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.8);
  border-color: #3b82f6;
}

.filter-select :deep(.el-input__wrapper.is-focus),
.filter-date :deep(.el-input__wrapper.is-focus),
.search-input :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2) !important;
  border-color: #3b82f6;
}

.search-row {
  display: flex;
  gap: 24px;
  align-items: center;
}

.search-group {
  flex: 1;
}

.action-group {
  display: flex;
  gap: 16px;
}

.filter-btn {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.6);
  color: var(--text-primary);
  padding: 10px 24px;
  border-radius: 12px;
  font-weight: 600;
  height: 44px;
  backdrop-filter: blur(4px);
  transition: all 0.3s ease;
}

.filter-btn:hover {
  background: rgba(255, 255, 255, 0.8);
  color: #2563eb;
  border-color: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.1);
}

.filter-btn.el-button--primary {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.filter-btn.el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.4);
}

/* 测评列表 */
.assessments-section {
  margin-bottom: 32px;
}

.section-header {
  padding: 16px 24px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  display: inline-block;
  margin-right: 16px;
}

.assessment-count {
  font-size: 13px;
  color: var(--text-secondary);
  background: rgba(0, 0, 0, 0.05);
  padding: 4px 10px;
  border-radius: 20px;
}

.header-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.view-toggle {
  background: rgba(255, 255, 255, 0.5);
  padding: 4px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.toggle-btn {
  border: none;
  background: transparent;
  padding: 8px 12px;
  color: #94a3b8;
  transition: all 0.2s;
  border-radius: 8px;
}

.toggle-btn.el-button--primary {
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
}

.sort-select :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 10px;
  box-shadow: none !important;
  border: 1px solid rgba(0, 0, 0, 0.05);
  padding: 4px 12px;
  height: 32px;
}

.assessments-list {
  display: grid;
  gap: 28px;
  margin-bottom: 30px;
}

.assessments-list.list {
  grid-template-columns: 1fr;
}

.assessments-list.grid {
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
}

.assessment-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.assessment-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
  border-color: rgba(255, 255, 255, 0.8);
}

.assessment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.assessment-type {
  display: flex;
  gap: 8px;
}

.type-badge {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  backdrop-filter: blur(4px);
}

.type-badge.quiz { background: rgba(59, 130, 246, 0.1); color: #3b82f6; border: 1px solid rgba(59, 130, 246, 0.2); }
.type-badge.test { background: rgba(16, 185, 129, 0.1); color: #10b981; border: 1px solid rgba(16, 185, 129, 0.2); }
.type-badge.midterm { background: rgba(245, 158, 11, 0.1); color: #f59e0b; border: 1px solid rgba(245, 158, 11, 0.2); }
.type-badge.final { background: rgba(239, 68, 68, 0.1); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.2); }
.type-badge.homework { background: rgba(139, 92, 246, 0.1); color: #8b5cf6; border: 1px solid rgba(139, 92, 246, 0.2); }

.status-badge {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.draft { background: rgba(148, 163, 184, 0.1); color: #64748b; }
.status-badge.published { background: rgba(59, 130, 246, 0.1); color: #3b82f6; }
.status-badge.active { background: rgba(34, 197, 94, 0.1); color: #22c55e; }
.status-badge.ended { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
.status-badge.archived { background: rgba(100, 116, 139, 0.1); color: #475569; }

.assessment-content {
  margin-bottom: 24px;
  flex: 1;
}

.assessment-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.assessment-description {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 16px 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.assessment-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.detail-item i {
  color: #3b82f6;
  width: 14px;
  text-align: center;
}

.assessment-footer {
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  padding-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-top: auto;
}

.assessment-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #94a3b8;
}

.assessment-progress {
  flex: 1;
  max-width: 180px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.progress-text {
  font-size: 12px;
  color: #94a3b8;
}

.progress-percent {
  font-size: 12px;
  font-weight: 600;
  color: #3b82f6;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.custom-pagination :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #2563eb;
}

.custom-pagination :deep(.el-pagination.is-background .el-pager li) {
  background-color: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 8px;
  margin: 0 4px;
}

.custom-pagination :deep(.btn-prev),
.custom-pagination :deep(.btn-next) {
  background-color: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 8px;
}

/* 对话框样式 */
.create-dialog :deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.create-dialog :deep(.el-dialog__header) {
  padding: 24px 32px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  margin-right: 0;
}

.create-dialog :deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.create-dialog :deep(.el-dialog__body) {
  padding: 32px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

/* Custom Inputs in Dialog */
.custom-input :deep(.el-input__wrapper),
.custom-select :deep(.el-select__wrapper),
.custom-date-picker :deep(.el-input__wrapper),
.custom-input-number :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5);
  box-shadow: none !important;
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 12px;
  padding: 8px 16px;
  height: 40px;
}

.custom-input :deep(.el-input__wrapper:hover),
.custom-select :deep(.el-select__wrapper:hover),
.custom-date-picker :deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.8);
  border-color: #3b82f6;
}

.custom-input :deep(.el-input__wrapper.is-focus),
.custom-select :deep(.el-select__wrapper.is-focused),
.custom-date-picker :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2) !important;
  border-color: #3b82f6;
}

/* Course Card in Dialog */
.selected-course-card {
  margin: 20px 0;
}

.course-card {
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: none;
}

/* Dialog Footer */
.dialog-footer {
  padding: 24px 32px;
  background: rgba(255, 255, 255, 0.3);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.cancel-btn {
  background: rgba(255, 255, 255, 0.6) !important;
  border: 1px solid rgba(0, 0, 0, 0.05) !important;
  color: var(--text-secondary) !important;
  border-radius: 12px !important;
  padding: 10px 24px !important;
  font-weight: 600 !important;
  box-shadow: none !important;
}

.cancel-btn:hover {
  background: white !important;
  color: #2563eb !important;
}

.create-btn {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%) !important;
  border: none !important;
  border-radius: 12px !important;
  padding: 10px 24px !important;
  color: white !important;
  font-weight: 600 !important;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3) !important;
}

.create-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.4) !important;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .assessments-list.grid {
    grid-template-columns: 1fr;
  }
}

/* 课程卡片样式 */
.selected-course-card {
  margin-top: 16px;
  margin-bottom: 16px;
}

.course-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

.course-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.course-cover {
  width: 80px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  margin-right: 16px;
  flex-shrink: 0;
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.course-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.course-description {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 12px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #9ca3af;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-item i {
  color: #002FA7;
  width: 12px;
}

/* 预览弹窗样式 */
.preview-dialog {
  .el-dialog__body {
    padding: 0;
  }
}

.preview-container {
  height: 70vh;
  overflow: hidden;
}

.preview-content {
  display: flex;
  height: 100%;
  gap: 20px;
}

.paper-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.paper-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e9ecef;
}

.paper-title {
  font-size: 24px;
  font-weight: 600;
  color: #002FA7;
  margin: 0 0 10px 0;
}

.paper-description {
  color: #6c757d;
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
}

.preview-section {
  margin-bottom: 30px;
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.section-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e9ecef;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #343a40;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-number {
  background: #002FA7;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.section-name {
  flex: 1;
}

.section-description {
  color: #6c757d;
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
}

.preview-question {
  padding: 15px;
  margin-bottom: 10px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #002FA7;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.question-number {
  font-weight: 600;
  color: #002FA7;
  min-width: 40px;
}

.question-icon {
  color: #6c757d;
  width: 16px;
}

.question-title {
  flex: 1;
  font-weight: 500;
  color: #343a40;
}

.question-score {
  background: #28a745;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.question-type {
  font-size: 12px;
  color: #6c757d;
  margin-left: 50px;
}

.empty-content {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
}

.empty-icon {
  font-size: 48px;
  color: #dee2e6;
  margin-bottom: 15px;
}

.preview-sidebar {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px 20px 20px 0;
}

.stats-card, .distribution-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 47, 167, 0.1);
  overflow: hidden;
}

.stats-header, .distribution-header {
  background: linear-gradient(135deg, #002FA7 0%, #0056b3 100%);
  color: white;
  padding: 15px 20px;
  margin: 0;
}

.stats-header h3, .distribution-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.stats-content {
  padding: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.stat-item:last-child {
  margin-bottom: 0;
}

.stat-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #002FA7 0%, #0056b3 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #002FA7;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #6c757d;
  margin-top: 2px;
}

.distribution-content {
  padding: 20px;
}

.distribution-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f1f3f4;
}

.distribution-item:last-child {
  border-bottom: none;
}

.type-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.type-icon {
  color: #002FA7;
  width: 16px;
}

.type-name {
  font-weight: 500;
  color: #343a40;
}

.type-stats {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.type-count {
  font-size: 14px;
  font-weight: 600;
  color: #002FA7;
}

.type-score {
  font-size: 12px;
  color: #6c757d;
}

.distribution-empty {
  text-align: center;
  padding: 40px 20px;
  color: #6c757d;
}

.loading-content {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
}

.loading-icon {
  font-size: 32px;
  color: #002FA7;
  margin-bottom: 15px;
}

.close-btn {
  background: #6c757d;
  border-color: #6c757d;
  color: white;
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: 500;
}

.close-btn:hover {
  background: #5a6268;
  border-color: #5a6268;
}

/* 新增的预览内容样式 */
.section-stats {
  font-size: 12px;
  color: #666;
  font-weight: normal;
  margin-left: 8px;
}

.question-content {
  margin: 12px 0;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 3px solid #002FA7;
}

.content-text {
  line-height: 1.6;
  color: #333;
}

.question-options {
  margin: 12px 0;
  padding: 0 16px;
}

.option-item {
  display: flex;
  align-items: flex-start;
  margin: 8px 0;
  padding: 8px 12px;
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.option-item:hover {
  background: #f8f9fa;
  border-color: #002FA7;
}

.option-label {
  font-weight: 600;
  color: #002FA7;
  margin-right: 8px;
  min-width: 20px;
}

.option-text {
  flex: 1;
  line-height: 1.5;
}

.question-meta {
  margin-top: 16px;
  padding: 12px;
  background: #e8f4fd;
  border-radius: 6px;
  border: 1px solid #b3d9ff;
}

.question-answer {
  margin-bottom: 8px;
  color: #0066cc;
}

.question-explanation {
  color: #666;
  line-height: 1.6;
}

.question-answer strong,
.question-explanation strong {
  color: #333;
  margin-right: 4px;
}

.no-questions {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-style: italic;
}

.no-questions .fa-info-circle {
  margin-right: 8px;
  color: #ffc107;
}

/* 课程选择下拉框样式 */
.course-option {
  padding: 0 !important;
}

.course-option-content {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.course-option:last-child .course-option-content {
  border-bottom: none;
}

/* 课程下拉选项样式优化 */
.course-option-item {
  height: auto !important;
  padding: 8px 12px !important;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.course-option-item:last-child {
  border-bottom: none;
}

.course-option-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.course-main-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.course-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
  flex: 1;
}

.text-clamp-1 {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-status {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
  white-space: nowrap;
}

.course-status.status-published {
  background: rgba(34, 197, 94, 0.1);
  color: #16a34a;
}

.course-status.status-draft {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}

.course-status.status-archived {
  background: rgba(100, 116, 139, 0.1);
  color: #64748b;
}

.course-sub-info {

  justify-content: flex-end;
}

.course-id {
  font-size: 11px;
  color: var(--text-secondary);
}

/* 选中课程卡片样式 */
.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.course-status-badge {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.course-status-badge.status-published {
  background: #e8f5e8;
  color: #2e7d32;
}

.course-status-badge.status-draft {
  background: #fff3e0;
  color: #f57c00;
}

.course-status-badge.status-archived {
  background: #f5f5f5;
  color: #757575;
}

.course-category-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  padding: 6px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
}

.course-category-info .fas {
  color: #007bff;
}

.course-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
}

.course-tag {
  padding: 4px 8px;
  background: #e3f2fd;
  color: #1976d2;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.more-tags {
  padding: 4px 8px;
  background: #f5f5f5;
  color: #666;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

@media (max-width: 768px) {
  .page-header {
    padding: 20px 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .stats-section,
  .filter-section,
  .assessments-section {
    padding: 20px 16px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-row {
    grid-template-columns: 1fr;
  }
  
  .search-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: space-between;
  }
  
  .assessment-footer {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .assessment-stats {
    justify-content: space-around;
  }
  
  .assessment-progress {
    max-width: none;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .detail-row {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
