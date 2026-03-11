<template>
  <div class="question-bank-container">
    <!-- 页面头部 -->
    <div class="page-header glass-panel">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <span class="icon-box header-icon"><font-awesome-icon :icon="['fas', 'book-open']"/></span>
            题库管理
          </h1>
          <p class="page-subtitle">管理和组织您的题目资源</p>
        </div>
        <div class="header-actions">
          <el-button class="glass-btn import-btn">
            <font-awesome-icon :icon="['fas', 'file-import']" />
            批量导入
          </el-button>
          <el-button type="primary" class="gradient-btn create-btn" @click="openCreateDialog">
            <font-awesome-icon :icon="['fas', 'plus']" />
            创建题目
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-icon gradient-blue">
            <font-awesome-icon :icon="['fas', 'question-circle']"/>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalQuestions }}</div>
            <div class="stat-label">总题目数</div>
          </div>
        </div>
        
        <div class="stat-card glass-card hover-lift">
          <div class="stat-icon gradient-green">
            <font-awesome-icon :icon="['fas', 'check-circle']"/>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.singleChoice }}</div>
            <div class="stat-label">单选题</div>
          </div>
        </div>
        
        <div class="stat-card glass-card hover-lift">
          <div class="stat-icon gradient-purple">
            <font-awesome-icon :icon="['fas', 'list-ul']"/>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.multipleChoice }}</div>
            <div class="stat-label">多选题</div>
          </div>
        </div>
        
        <div class="stat-card glass-card hover-lift">
          <div class="stat-icon gradient-orange">
            <font-awesome-icon :icon="['fas', 'edit']"/>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.essay }}</div>
            <div class="stat-label">问答题</div>
          </div>
        </div>
        
        <div class="stat-card glass-card hover-lift">
          <div class="stat-icon gradient-pink">
            <font-awesome-icon :icon="['fas', 'pen-alt']"/>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.fill }}</div>
            <div class="stat-label">填空题</div>
          </div>
        </div>
        
        <div class="stat-card glass-card hover-lift">
          <div class="stat-icon gradient-indigo">
            <font-awesome-icon :icon="['fas', 'code']"/>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.coding }}</div>
            <div class="stat-label">编程题</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <div class="filter-card glass-card">
        <div class="filter-row">
          <div class="filter-group">
            <label class="filter-label">题目类型</label>
            <el-select v-model="filters.type" placeholder="全部类型" class="glass-select" popper-class="glass-dropdown">
              <el-option label="全部类型" value="" />
              <el-option label="单选题" value="single" />
              <el-option label="多选题" value="multiple" />
              <el-option label="填空题" value="fill" />
              <el-option label="问答题" value="essay" />
              <el-option label="编程题" value="coding" />
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">难度等级</label>
            <el-select v-model="filters.difficulty" placeholder="全部难度" class="glass-select" popper-class="glass-dropdown">
              <el-option label="全部难度" value="" />
              <el-option label="简单" value="easy" />
              <el-option label="中等" value="medium" />
              <el-option label="困难" value="hard" />
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">所属课程</label>
            <el-select v-model="filters.course" placeholder="全部课程" class="glass-select" popper-class="glass-dropdown">
              <el-option label="全部课程" value="" />
              <el-option label="Vue.js 基础" value="vue-basic" />
              <el-option label="React 进阶" value="react-advanced" />
              <el-option label="Node.js 实战" value="nodejs-practice" />
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">标签</label>
            <el-select v-model="filters.tags" placeholder="选择标签" multiple class="glass-select" popper-class="glass-dropdown">
              <el-option label="基础" value="basic" />
              <el-option label="进阶" value="advanced" />
              <el-option label="实战" value="practice" />
              <el-option label="算法" value="algorithm" />
            </el-select>
          </div>
        </div>
        
        <div class="search-row">
          <div class="search-group">
            <el-input 
              v-model="filters.search" 
              placeholder="搜索题目内容、标签或知识点..."
              class="glass-input search-input"
            >
              <template #prefix>
                <font-awesome-icon :icon="['fas', 'search']" />
              </template>
            </el-input>
          </div>
          
          <div class="action-group">
            <el-button class="glass-btn" @click="resetFilters">
              <font-awesome-icon :icon="['fas', 'undo']" />
              重置
            </el-button>
            <el-button type="primary" class="gradient-btn">
              <font-awesome-icon :icon="['fas', 'search']" />
              搜索
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 题目列表 -->
    <div class="questions-section">
      <div class="section-header glass-panel">
        <div class="header-left">
          <h3>题目列表</h3>
          <span class="question-count">共 {{ filteredQuestions.length }} 道题目</span>
        </div>
        <div class="header-actions">
          <el-select v-model="sortBy" placeholder="排序方式" class="glass-select small" popper-class="glass-dropdown">
            <el-option label="创建时间" value="createTime" />
            <el-option label="更新时间" value="updateTime" />
            <el-option label="难度等级" value="difficulty" />
          </el-select>
        </div>
      </div>
      
      <div class="questions-content">
        <div class="questions-grid">
          <div 
            v-for="question in paginatedQuestions" 
            :key="question.id"
            class="question-card glass-card hover-lift"
          >
            <div class="question-header">
              <div class="question-type">
                <span :class="['type-badge', question.type]">
                  <font-awesome-icon :icon="getTypeIcon(question.type).split(' ')" />
                  {{ getTypeName(question.type) }}
                </span>
                <span :class="['difficulty-badge', question.difficulty]">
                  {{ getDifficultyName(question.difficulty) }}
                </span>
              </div>
              <div class="question-actions">
                <el-dropdown trigger="click">
                  <el-button class="icon-btn" text>
                    <font-awesome-icon :icon="['fas', 'ellipsis-v']" />
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu class="glass-dropdown-menu">
                      <el-dropdown-item>
                        <font-awesome-icon :icon="['fas', 'edit']" /> 编辑
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <font-awesome-icon :icon="['fas', 'copy']" /> 复制
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <font-awesome-icon :icon="['fas', 'eye']" /> 预览
                      </el-dropdown-item>
                      <el-dropdown-item divided class="danger-item">
                        <font-awesome-icon :icon="['fas', 'trash']" /> 删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            
            <div class="question-content">
              <el-tooltip placement="top-start" :show-after="250" effect="light">
                <template #content>
                  <div class="hover-full-text">{{ question.content }}</div>
                </template>
                <div class="question-text text-clamp-2">{{ question.content }}</div>
              </el-tooltip>
              
              <div v-if="question.type === 'single' || question.type === 'multiple'" class="question-options">
                <div 
                  v-for="(option, index) in question.options" 
                  :key="index"
                  :class="['option-item', { correct: question.correctAnswers.includes(index) }]"
                >
                  <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
                  <el-tooltip placement="top-start" :show-after="250" effect="light">
                    <template #content>
                      <div class="hover-full-text">{{ option }}</div>
                    </template>
                    <span class="option-text text-clamp-1">{{ option }}</span>
                  </el-tooltip>
                  <font-awesome-icon v-if="question.correctAnswers.includes(index)" :icon="['fas', 'check']" class="correct-icon" />
                </div>
              </div>
              
              <div v-if="question.type === 'fill'" class="question-answer">
                <div class="answer-label">参考答案：</div>
                <el-tooltip placement="top-start" :show-after="250" effect="light">
                  <template #content>
                    <div class="hover-full-text">{{ question.answer || '' }}</div>
                  </template>
                  <div class="answer-content text-clamp-2" v-html="question.answer ? question.answer.replace(/\n/g, '<br>') : ''"></div>
                </el-tooltip>
              </div>
              
              <div v-if="question.type === 'essay'" class="question-answer">
                <div class="answer-label">参考答案：</div>
                <el-tooltip placement="top-start" :show-after="250" effect="light">
                  <template #content>
                    <div class="hover-full-text">{{ question.answer || '' }}</div>
                  </template>
                  <div class="answer-content text-clamp-2" v-html="question.answer ? question.answer.replace(/\n/g, '<br>') : ''"></div>
                </el-tooltip>
              </div>
              
              <div v-if="question.type === 'judge'" class="question-judge">
                <div class="judge-options">
                  <div :class="['judge-option', { 'correct': question.answer === true, 'selected': question.answer === true }]">
                    <span class="judge-label">正确</span>
                    <font-awesome-icon v-if="question.answer === true" :icon="['fas', 'check']" class="correct-icon" />
                  </div>
                  <div :class="['judge-option', { 'correct': question.answer === false, 'selected': question.answer === false }]">
                    <span class="judge-label">错误</span>
                    <font-awesome-icon v-if="question.answer === false" :icon="['fas', 'check']" class="correct-icon" />
                  </div>
                </div>
              </div>
            </div>
            
            <div class="question-footer">
              <div class="question-meta">
                <span class="meta-item">
                  <font-awesome-icon :icon="['fas', 'clock']" />
                  {{ question.createTime }}
                </span>
              </div>
              
              <div class="question-tags">
                <el-tag 
                  v-for="tag in question.tags" 
                  :key="tag"
                  size="small"
                  class="question-tag glass-tag"
                >
                  <el-tooltip placement="top" :show-after="250" effect="light">
                    <template #content>
                      <div class="hover-full-text">{{ tag }}</div>
                    </template>
                    <span class="text-clamp-1">{{ tag }}</span>
                  </el-tooltip>
                </el-tag>
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
            :total="filteredQuestions.length"
            layout="total, sizes, prev, pager, next, jumper"
            class="glass-pagination"
            background
          />
        </div>
      </div>
    </div>

    <!-- 创建题目对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      title="创建新题目" 
      width="800px"
      custom-class="glass-dialog"
      :close-on-click-modal="false"
    >
      <div class="create-form">
        <el-form :model="newQuestion" label-width="100px" class="modern-form">
          <el-form-item label="题目类型">
            <el-select v-model="newQuestion.type" placeholder="请选择题目类型" class="glass-select" popper-class="glass-dropdown">
              <el-option label="单选题" value="single" />
              <el-option label="多选题" value="multiple" />
              <el-option label="判断题" value="judge" />
              <el-option label="填空题" value="fill" />
              <el-option label="问答题" value="essay" />
              <el-option label="编程题" value="coding" />
            </el-select>
          </el-form-item>
          <el-form-item label="题目难度">
            <el-select v-model="newQuestion.difficulty" placeholder="请选择题目难度" class="glass-select" popper-class="glass-dropdown">
              <el-option label="简单" value="easy" />
              <el-option label="中等" value="medium" />
              <el-option label="困难" value="hard" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="题目内容">
            <el-input 
              v-model="newQuestion.content" 
              type="textarea" 
              :rows="4"
              placeholder="请输入题目内容（题干）"
              class="glass-input"
            />
          </el-form-item>
          
          <!-- 选择题选项 -->
          <template v-if="newQuestion.type === 'single' || newQuestion.type === 'multiple'">
            <el-form-item label="选项设置">
              <div class="options-container">
                <div v-for="(option, index) in newQuestion.options" :key="index" class="option-item-edit">
                  <el-input 
                    v-model="newQuestion.options[index]" 
                    :placeholder="`选项 ${String.fromCharCode(65 + index)}`"
                    class="option-input glass-input"
                  />
                  <el-button 
                    v-if="newQuestion.options.length > 2" 
                    @click="removeOption(index)" 
                    type="danger" 
                    size="small"
                    circle
                    class="remove-option-btn"
                  >
                    <el-icon><Minus /></el-icon>
                  </el-button>
                </div>
                <el-button 
                  v-if="newQuestion.options.length < 6" 
                  @click="addOption" 
                  class="glass-btn add-option-btn"
                  size="small"
                >
                  <el-icon><Plus /></el-icon>
                  添加选项
                </el-button>
              </div>
            </el-form-item>
            
            <el-form-item label="正确答案">
              <el-select 
                v-model="newQuestion.answer" 
                :placeholder="newQuestion.type === 'single' ? '请选择正确答案' : '请选择正确答案（可多选）'"
                :multiple="newQuestion.type === 'multiple'"
                class="glass-select"
                popper-class="glass-dropdown"
              >
                <el-option 
                  v-for="(option, index) in newQuestion.options.filter(opt => opt.trim())" 
                  :key="index" 
                  :label="`${String.fromCharCode(65 + index)}. ${option}`" 
                  :value="index"
                />
              </el-select>
            </el-form-item>
          </template>
          
          <!-- 判断题答案 -->
          <template v-if="newQuestion.type === 'judge'">
            <el-form-item label="正确答案">
              <el-select v-model="newQuestion.answer" placeholder="请选择正确答案" class="glass-select" popper-class="glass-dropdown">
                <el-option label="正确" :value="true" />
                <el-option label="错误" :value="false" />
              </el-select>
            </el-form-item>
          </template>
          
          <!-- 填空题和问答题答案 -->
          <template v-if="newQuestion.type === 'fill' || newQuestion.type === 'essay' || newQuestion.type === 'coding'">
            <el-form-item label="参考答案">
              <el-input 
                v-model="newQuestion.answer" 
                type="textarea" 
                :rows="newQuestion.type === 'essay' || newQuestion.type === 'coding' ? 6 : 3"
                placeholder="请输入参考答案"
                class="glass-input"
              />
            </el-form-item>
          </template>
          
          <el-form-item label="答案解析">
            <el-input 
              v-model="newQuestion.analysis" 
              type="textarea" 
              :rows="3"
              placeholder="请输入答案解析（可选）"
              class="glass-input"
            />
          </el-form-item>
          
          <el-form-item label="所属课程">
            <el-select 
              v-model="newQuestion.courseId" 
              placeholder="请选择课程"
              @focus="handleCourseSelectFocus"
              @change="handleCourseChange"
              filterable
              clearable
              class="glass-select"
              popper-class="glass-dropdown"
            >
              <el-option 
                v-for="course in courseList" 
                :key="getCourseId(course)" 
                :label="getCourseTitle(course)" 
                :value="getCourseId(course)"
              >
                <span style="float: left">{{ getCourseTitle(course) }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">
                  ID: {{ getCourseId(course) }}
                </span>
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
          <el-form-item v-if="selectedCourse" label="选中课程">
            <div class="selected-course-card glass-panel">
              <div class="course-card-header">
                <img 
                  :src="selectedCourse.courseCover || selectedCourse.coverUrl || '/default-course-cover.jpg'" 
                  :alt="getCourseTitle(selectedCourse)"
                  class="course-cover"
                />
                <div class="course-info">
                  <h4 class="course-title">{{ getCourseTitle(selectedCourse) }}</h4>
                  <p class="course-description">{{ selectedCourse.courseDescription || selectedCourse.description || '暂无描述' }}</p>
                  <div class="course-meta">
                    <span class="meta-item">
                      <font-awesome-icon :icon="['fas', 'users']" />
                      {{ selectedCourse.enrollmentCount || 0 }} 人学习
                    </span>
                    <span class="meta-item">
                      <font-awesome-icon :icon="['fas', 'clock']" />
                      {{ formatCourseDate(selectedCourse.createTime || selectedCourse.createdAt) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </el-form-item>
          
          <el-form-item label="标签">
            <el-input 
              v-model="tagInput" 
              placeholder="输入标签，多个标签请用中文顿号、分隔"
              @keyup.enter="addTag"
              class="glass-input"
            />
            <div class="tag-input-hint">
              <font-awesome-icon :icon="['fas', 'info-circle']" />
              多个标签请用中文顿号、分隔
            </div>
            <div class="tags-container" v-if="newQuestion.tags.length > 0">
              <el-tag 
                v-for="(tag, index) in newQuestion.tags" 
                :key="index" 
                closable 
                @close="removeTag(index)"
                class="tag-item glass-tag"
              >
                {{ tag }}
              </el-tag>
            </div>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button class="glass-btn" @click="showCreateDialog = false">取消</el-button>
          <el-button class="glass-btn ai-btn" @click="createQuestionAISuggestion" :loading="aiSuggestionLoading" :disabled="aiSuggestionLoading">
             <font-awesome-icon :icon="['fas', 'magic']" class="ai-icon" /> AI智能建议
          </el-button>
          <el-button type="primary" class="gradient-btn" @click="createQuestion">创建题目</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Minus } from '@element-plus/icons-vue'
import { createQuestion as createQuestionAPI, getQuestionsList, getMyCourseList, getCourseOutline } from '@/api/teacher/teacherAPI.js'
import { getOptimizationAssistantQuestions } from '@/api/teacher/TeacherAIChatAPI.js'

// 统计数据
const stats = ref({
  totalQuestions: 156,
  singleChoice: 68,
  multipleChoice: 42,
  essay: 46,
  fill: 0,
  coding: 0
})

// 筛选条件
const filters = ref({
  type: '',
  difficulty: '',
  course: '',
  tags: [],
  search: ''
})

// 排序
const sortBy = ref('createTime')

// 分页
const currentPage = ref(1)
const pageSize = ref(20)

// 创建题目对话框
const showCreateDialog = ref(false)
const newQuestion = ref({
  content: '',
  type: '',
  difficulty:'',
  courseId: '',
  options: ['', '', '', ''],
  answer: '',
  analysis: '',
  tags: []
})

// 课程列表
const courseList = ref([])

// AI智能建议加载状态
const aiSuggestionLoading = ref(false)

// 选中的课程
const selectedCourse = ref(null)

// 标签输入
const tagInput = ref('')

// 模拟题目数据
const questions = ref([])

// 筛选后的题目
const filteredQuestions = computed(() => {
  let result = questions.value
  
  if (filters.value.type) {
    result = result.filter(q => q.type === filters.value.type)
  }
  
  if (filters.value.difficulty) {
    result = result.filter(q => q.difficulty === filters.value.difficulty)
  }
  
  if (filters.value.course) {
    result = result.filter(q => q.course === filters.value.course)
  }
  
  if (filters.value.search) {
    const search = filters.value.search.toLowerCase()
    result = result.filter(q => 
      q.content.toLowerCase().includes(search) ||
      q.tags.some(tag => tag.toLowerCase().includes(search))
    )
  }
  
  return result
})

// 分页后的题目
const paginatedQuestions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredQuestions.value.slice(start, end)
})

// 获取题目类型图标
const getTypeIcon = (type) => {
  const icons = {
    single: 'fas fa-dot-circle',
    multiple: 'fas fa-check-square',
    judge: 'fas fa-balance-scale',
    fill: 'fas fa-edit',
    essay: 'fas fa-file-alt',
    coding: 'fas fa-code'
  }
  return icons[type] || 'fas fa-question'
}

// 获取题目类型名称
const getTypeName = (type) => {
  const names = {
    single: '单选题',
    multiple: '多选题',
    judge: '判断题',
    fill: '填空题',
    essay: '问答题',
    coding: '编程题'
  }
  return names[type] || '未知'
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

// 重置筛选条件
const resetFilters = () => {
  filters.value = {
    type: '',
    difficulty: '',
    course: '',
    tags: [],
    search: ''
  }
}

// 打开创建对话框
const openCreateDialog = () => {
  showCreateDialog.value = true
}

// 重置表单
const resetForm = () => {
  newQuestion.value = {
    content: '',
    type: '',
    courseId: '',
    options: ['', '', '', ''],
    answer: '',
    analysis: '',
    tags: []
  }
}

// 表单验证
const validateForm = () => {
  const question = newQuestion.value;

  // 1. 验证题目内容
  if (!question.content || !question.content.trim()) {
    ElMessage.error('题目内容不能为空');
    return false;
  }

  // 4. 验证关联课程
  if (!question.courseId) {
    ElMessage.error('请关联一个课程');
    return false;
  }

  // 5. 验证标签
  if (question.tags.length === 0) {
    ElMessage.error('请至少添加一个标签');
    return false;
  }

  return true; // 所有验证通过
};


// 创建题目
const createQuestion = async () => {
  console.log('开始创建题目，表单数据:', newQuestion.value)
  
  if (!validateForm()) {
    return
  }
  
  try {
    // 构造符合后端Question实体的数据格式
    const questionData = {
      courseId: newQuestion.value.courseId,
      type: mapQuestionType(newQuestion.value.type),
      difficulty: mapDifficulty(newQuestion.value.difficulty),
      stem: newQuestion.value.content.trim(),
      options: formatOptions(newQuestion.value.type, newQuestion.value.options),
      answer: formatAnswer(newQuestion.value.type, newQuestion.value.answer),
      analysis: newQuestion.value.analysis.trim() || null,
      tags: newQuestion.value.tags
    }
    
    console.log('发送到后端的题目数据:', questionData)
    
    const result = await createQuestionAPI(questionData)
    console.log('题目创建成功，返回结果:', result)
    
    ElMessage.success('题目创建成功！')
    showCreateDialog.value = false
    resetForm()
    
    // 刷新题目列表
    await loadQuestions()
  } catch (error) {
    console.error('创建题目失败:', error)
    ElMessage.error(error.message || '创建题目失败，请重试')
  }
}

// AI智能建议函数
const createQuestionAISuggestion = async () => {
  console.log('开始AI智能建议，当前表单数据:', newQuestion.value)
  
  // 验证必要字段
  if (!newQuestion.value.courseId) {
    ElMessage.warning('请先选择课程')
    return
  }
  
  if (!newQuestion.value.type) {
    ElMessage.warning('请先选择题目类型')
    return
  }
  
  if (!newQuestion.value.difficulty) {
    ElMessage.warning('请先选择题目难度')
    return
  }
  
  try {
    // 设置加载状态
    aiSuggestionLoading.value = true
    
    // 获取课程大纲数据
    let outline = null
    try {
      const response = await getCourseOutline(newQuestion.value.courseId)
      
      // 处理不同的响应格式
      // 检查是否是标准API响应格式 {success: true, code: 200, data: {outline: []}}
      if (response && response.success && response.code === 200 && response.data && response.data.outline) {
        outline = response.data.outline
      }
      // 检查是否直接返回outline数组格式 {outline: []}
      else if (response && response.outline && Array.isArray(response.outline)) {
        outline = response.outline
      }
      // 检查是否直接返回数组格式 []
      else if (Array.isArray(response)) {
        outline = response
      }
      
      if (!outline || outline.length === 0) {
        console.warn('课程大纲为空，使用默认结构')
        outline = []
      }
    } catch (outlineError) {
      console.warn('获取课程大纲失败，使用默认结构:', outlineError)
      outline = []
    }
    
    // 根据CourseOutlineDto结构转换数据格式
    const sortedOutline = outline.length > 0 ? 
      [...outline].sort((a, b) => (a.orderIndex || 0) - (b.orderIndex || 0))
        .map(chapter => ({
          title: chapter.title || '',
          orderIndex: chapter.orderIndex || 0,
          sections: chapter.sections ? 
            [...chapter.sections]
              .sort((a, b) => (a.orderIndex || 0) - (b.orderIndex || 0))
              .map(section => ({
                title: section.title || '',
                orderIndex: section.orderIndex || 0
              })) : []
        })) : []
    
    // 获取选中的课程信息
    const selectedCourseData = selectedCourse.value || courseList.value.find(course => 
      getCourseId(course) === newQuestion.value.courseId
    )
    
    if (!selectedCourseData) {
      ElMessage.error('未找到选中的课程信息')
      return
    }
    
    // 构造符合QuestionGenerationRequest的请求数据
    const requestData = {
      course: {
        title: getCourseTitle(selectedCourseData),
        category: selectedCourseData.category || selectedCourseData.courseCategory || '通用',
        outline: {
          outline: sortedOutline
        }
      },
      creatorId: 'current_teacher_id', // 这里应该从用户状态中获取
      type: mapQuestionType(newQuestion.value.type),
      difficulty: mapDifficulty(newQuestion.value.difficulty)
    }
    
    console.log('发送给AI的请求数据:', requestData)
    
    // 调用AI建议API
    const aiResponse = await getOptimizationAssistantQuestions(requestData)
    
    if (aiResponse) {
      console.log('AI建议返回结果:', aiResponse)
      
      // 将AI建议的内容填充到表单中
      if (aiResponse.stem) {
        newQuestion.value.content = aiResponse.stem
      }
      
      if (aiResponse.options && Array.isArray(aiResponse.options)) {
        // 确保options数组长度至少为4
        const aiOptions = aiResponse.options.map(opt => opt.text || opt)
        newQuestion.value.options = [...aiOptions]
        // 如果选项少于4个，补充空选项
        while (newQuestion.value.options.length < 4) {
          newQuestion.value.options.push('')
        }
      }
      
      if (aiResponse.answer !== undefined && aiResponse.answer !== null) {
        // 根据题目类型处理答案
        if (newQuestion.value.type === 'single') {
          // 单选题：如果答案是字母，转换为索引
          if (typeof aiResponse.answer === 'string') {
            const answerIndex = aiResponse.answer.charCodeAt(0) - 65
            newQuestion.value.answer = answerIndex >= 0 ? answerIndex : 0
          } else {
            newQuestion.value.answer = aiResponse.answer
          }
        } else if (newQuestion.value.type === 'multiple') {
          // 多选题：处理数组答案
          if (Array.isArray(aiResponse.answer)) {
            newQuestion.value.answer = aiResponse.answer.map(ans => 
              typeof ans === 'string' ? ans.charCodeAt(0) - 65 : ans
            ).filter(index => index >= 0)
          } else {
            newQuestion.value.answer = []
          }
        } else {
          // 其他题型直接使用答案
          newQuestion.value.answer = aiResponse.answer
        }
      }
      
      if (aiResponse.analysis) {
        newQuestion.value.analysis = aiResponse.analysis
      }
      
      if (aiResponse.tags && Array.isArray(aiResponse.tags)) {
        newQuestion.value.tags = aiResponse.tags
      }
      
      ElMessage.success('AI智能建议已生成，请检查并完善题目内容')
    } else {
      ElMessage.warning('AI建议生成失败，请手动填写题目内容')
    }
    
  } catch (error) {
    console.error('AI智能建议失败:', error)
    ElMessage.error('AI智能建议失败: ' + (error.message || '服务暂时不可用，请稍后重试'))
  } finally {
    // 重置加载状态
    aiSuggestionLoading.value = false
  }
}

// 映射题目类型到后端枚举
const mapQuestionType = (frontendType) => {
  const typeMap = {
    'single': 'SINGLE_CHOICE',
    /**
     * 多选题
     */
    'multiple': 'MULTI_CHOICE',
    'judge': 'TRUE_FALSE',
    'fill': 'FILL_IN_BLANKS',
    'essay': 'SHORT_ANSWER',
    'coding': 'PROGRAMMING'
  }
  return typeMap[frontendType] || 'SINGLE_CHOICE'
}

// 映射难度到后端枚举
const mapDifficulty = (frontendDifficulty) => {
  const difficultyMap = {
    'easy': 'EASY',
    'medium': 'MEDIUM',
    'hard': 'HARD'
  }
  return difficultyMap[frontendDifficulty] || 'EASY'
}

// 格式化选项数据
const formatOptions = (type, options) => {
  if (type === 'single' || type === 'multiple') {
    return options.filter(opt => opt.trim()).map((text, index) => ({
      key: String.fromCharCode(65 + index), // A, B, C, D
      text: text
    }))
  }
  return null
}

// 格式化答案数据
const formatAnswer = (type, answer) => {
  if (type === 'single') {
    // 单选题：返回选项的key值
    return typeof answer === 'number' ? String.fromCharCode(65 + answer) : answer
  } else if (type === 'multiple') {
    // 多选题：返回选项key值数组
    if (Array.isArray(answer)) {
      return answer.map(index => String.fromCharCode(65 + index))
    }
    return []
  } else if (type === 'judge') {
    // 判断题：返回布尔值
    return answer
  } else {
    // 填空题、问答题、编程题：直接返回文本
    return answer
  }
}

// 加载课程列表
const loadCourses = async () => {
  try {
    console.log('开始加载课程列表...')
    
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
    if (Array.isArray(courseData)) {
      courseList.value = courseData
      console.log('课程列表加载成功，共', courseData.length, '门课程')
    } else if (courseData && typeof courseData === 'object') {
      // 如果返回的是对象，尝试提取课程数组
      courseList.value = courseData.courses || courseData.data || [courseData]
      console.log('课程列表加载成功（对象格式），共', courseList.value.length, '门课程')
    } else {
      courseList.value = []
      console.warn('课程列表数据格式异常:', courseData)
    }
    
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

// 加载题目列表
const loadQuestions = async () => {
  try {
    const response = await getQuestionsList()
    console.log('API返回数据:', response)
    
    // 处理后端返回的QuestionVo格式数据
    if (response && Array.isArray(response)) {
      // 将QuestionVo格式转换为前端显示格式
      questions.value = response.flatMap(courseData => 
        courseData.question.map(q => ({
          id: q.id,
          type: mapBackendTypeToFrontend(q.type),
          content: q.stem,
          options: q.options ? q.options.map(opt => opt.text) : [],
          correctAnswers: formatBackendAnswer(q.type, q.answer),
          answer: formatBackendAnswerForDisplay(q.type, q.answer),
          difficulty: mapBackendDifficultyToFrontend(q.difficulty),
          course: courseData.title,
          tags: q.tags || [],
          createTime: formatDate(q.createdAt),
        }))
      )
      
      // 更新统计数据
      updateStats()
    } else {
      // 如果返回数据格式不符合预期，设置空数组
      questions.value = []
      updateStats()
      console.warn('返回的数据格式不符合预期:', response)
    }
  } catch (error) {
    console.error('加载题目列表失败:', error)
    ElMessage.error('加载题目列表失败: ' + (error.message || '未知错误'))
    // 设置空数组避免界面异常
    questions.value = []
    updateStats()
  }
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

// 映射后端题目类型到前端
const mapBackendTypeToFrontend = (backendType) => {
  const typeMap = {
    'SINGLE_CHOICE': 'single',
    'MULTI_CHOICE': 'multiple',
    'FILL_IN_BLANKS': 'fill',
    'TRUE_FALSE': 'judge',
    'SHORT_ANSWER': 'essay',
    'PROGRAMMING': 'coding'
  }
  return typeMap[backendType] || 'single'
}

// 格式化后端答案为前端格式
const formatBackendAnswer = (type, answer) => {
  if (type === 'SINGLE_CHOICE') {
    // 单选题：将字母转换为索引
    return typeof answer === 'string' ? [answer.charCodeAt(0) - 65] : []
  } else if (type === 'MULTI_CHOICE') {
    // 多选题：将字母数组转换为索引数组
    if (Array.isArray(answer)) {
      return answer.map(letter => letter.charCodeAt(0) - 65)
    }
    return []
  }
  return []
}

// 格式化后端答案为显示格式
const formatBackendAnswerForDisplay = (type, answer) => {
  if (type === 'FILL_IN_BLANKS') {
    // 填空题：将数组答案格式化为"1、xx\n2、xx"格式
    if (Array.isArray(answer)) {
      return answer.map((item, index) => `${index + 1}、${item}`).join('\n')
    }
    // 如果答案是字符串，尝试按逗号或顿号分割
    if (typeof answer === 'string') {
      const separators = /,|、/
      if (separators.test(answer)) {
        return answer.split(separators)
          .map((item, index) => `${index + 1}、${item.trim()}`)
          .join('\n')
      }
      // 单个答案
      return `1、${answer}`
    }
    return answer
  } else if (type === 'SHORT_ANSWER') {
    // 问答题：确保答案内容完整显示
    if (typeof answer === 'string') {
      return answer
    }
    // 如果是数组，合并为字符串
    if (Array.isArray(answer)) {
      return answer.join('\n')
    }
    return answer || ''
  }
  // 其他题型直接返回原答案
  return answer
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return new Date().toISOString().split('T')[0]
  return new Date(dateString).toISOString().split('T')[0]
}

// 更新统计数据
const updateStats = () => {
  const total = questions.value.length
  const singleChoice = questions.value.filter(q => q.type === 'single').length
  const multipleChoice = questions.value.filter(q => q.type === 'multiple').length
  const essay = questions.value.filter(q => q.type === 'essay').length
  const fill = questions.value.filter(q => q.type === 'fill').length
  const coding = questions.value.filter(q => q.type === 'coding').length
  
  stats.value = {
    totalQuestions: total,
    singleChoice,
    multipleChoice,
    essay,
    fill,
    coding
  }
}

// 添加选项
const addOption = () => {
  if (newQuestion.value.options.length < 6) {
    newQuestion.value.options.push('')
  }
}

// 移除选项
const removeOption = (index) => {
  if (newQuestion.value.options.length > 2) {
    newQuestion.value.options.splice(index, 1)
    // 如果删除的选项是正确答案，需要重置答案
    if (Array.isArray(newQuestion.value.answer)) {
      newQuestion.value.answer = newQuestion.value.answer.filter(ans => ans !== index)
        .map(ans => ans > index ? ans - 1 : ans)
    } else if (newQuestion.value.answer === index) {
      newQuestion.value.answer = ''
    } else if (newQuestion.value.answer > index) {
      newQuestion.value.answer = newQuestion.value.answer - 1
    }
  }
}

// 标签格式验证函数
const validateTagFormat = (input) => {
  const trimmedInput = input.trim()
  
  // 检查是否为空
  if (!trimmedInput) {
    return { valid: false, message: '标签不能为空' }
  }
  
  // 检查是否包含非法字符（除了中文、英文、数字、中文顿号外的特殊字符）
  const invalidChars = /[^\u4e00-\u9fa5a-zA-Z0-9\u3001\s]/
  if (invalidChars.test(trimmedInput)) {
    return { valid: false, message: '标签只能包含中文、英文、数字和中文顿号' }
  }
  
  // 检查是否使用了错误的分隔符
  const wrongSeparators = /[,，;；|\|/\/]/
  if (wrongSeparators.test(trimmedInput)) {
    return { valid: false, message: '多个标签请使用中文顿号、进行分隔，不要使用逗号、分号等其他符号' }
  }
  
  // 检查是否有连续的顿号
  if (/\u3001{2,}/.test(trimmedInput)) {
    return { valid: false, message: '请不要使用连续的顿号' }
  }
  
  // 检查是否以顿号开头或结尾
  if (trimmedInput.startsWith('、') || trimmedInput.endsWith('、')) {
    return { valid: false, message: '标签不能以顿号开头或结尾' }
  }
  
  return { valid: true, message: '' }
}

// 解析标签字符串为数组
const parseTagsFromInput = (input) => {
  const trimmedInput = input.trim()
  if (!trimmedInput) return []
  
  // 使用中文顿号分割，并过滤空字符串
  return trimmedInput.split('、')
    .map(tag => tag.trim())
    .filter(tag => tag.length > 0)
    .filter(tag => tag.length <= 35) // 限制单个标签长度
}

// 添加标签
const addTag = () => {
  const input = tagInput.value.trim()
  
  if (!input) {
    ElMessage.warning('请输入标签内容')
    return
  }
  
  // 验证标签格式
  const validation = validateTagFormat(input)
  if (!validation.valid) {
    ElMessage.error(validation.message)
    return
  }
  
  // 解析标签
  const newTags = parseTagsFromInput(input)
  
  if (newTags.length === 0) {
    ElMessage.warning('请输入有效的标签内容')
    return
  }
  
  // 检查标签数量限制
  const totalTags = newQuestion.value.tags.length + newTags.length
  if (totalTags > 10) {
    ElMessage.error('最多只能添加10个标签')
    return
  }
  
  // 检查重复标签
  const duplicateTags = newTags.filter(tag => newQuestion.value.tags.includes(tag))
  if (duplicateTags.length > 0) {
    ElMessage.warning(`标签 "${duplicateTags.join('、')}" 已存在`)
    return
  }
  
  // 添加新标签
  newQuestion.value.tags.push(...newTags)
  tagInput.value = ''
  
  ElMessage.success(`成功添加 ${newTags.length} 个标签`)
}

// 移除标签
const removeTag = (index) => {
  newQuestion.value.tags.splice(index, 1)
}

// 处理课程下拉框获得焦点事件
const handleCourseSelectFocus = async () => {
  console.log('课程下拉框获得焦点，当前课程数量:', courseList.value.length)
  
  // 每次获得焦点都重新加载课程列表，确保数据最新
  if (courseList.value.length === 0) {
    console.log('课程列表为空，开始加载...')
    await loadCourses()
  } else {
    console.log('课程列表已存在，跳过加载')
  }
}

// 处理课程选择变化
const handleCourseChange = (courseId) => {
  console.log('课程选择变化:', courseId)
  console.log('当前课程列表:', courseList.value)
  
  if (courseId) {
    // 查找选中的课程
    selectedCourse.value = courseList.value.find(course => {
      return getCourseId(course) === courseId
    })
    
    if (selectedCourse.value) {
      console.log('找到选中课程:', selectedCourse.value)
    } else {
      console.warn('未找到对应的课程，courseId:', courseId)
      console.warn('可用课程列表:', courseList.value.map(c => ({ 
        id: getCourseId(c), 
        title: getCourseTitle(c) 
      })))
    }
  } else {
    selectedCourse.value = null
    console.log('清空选中课程')
  }
}

// 格式化课程日期
const formatCourseDate = (dateString) => {
  if (!dateString) return '未知时间'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 获取课程ID（兼容不同的数据结构）
const getCourseId = (course) => {
  if (!course) return ''
  return course.courseId || course.id || course.cid || course._id || ''
}

// 获取课程标题（兼容不同的数据结构）
const getCourseTitle = (course) => {
  if (!course) return '未知课程'
  return course.courseTitle || course.title || course.name || course.courseName || course.subject || '未知课程'
}

onMounted(async () => {
  console.log('QuestionBank组件已挂载，开始初始化...')
  
  // 组件挂载时的初始化逻辑
  try {
    await loadQuestions()
    console.log('初始化完成')
  } catch (error) {
    console.error('初始化失败:', error)
  }
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

.question-bank-container {
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

/* Header */
.page-header {
  padding: 24px 32px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  position: relative;
  z-index: 10;
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

.icon-box {
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

.glass-btn {
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

.glass-btn:hover {
  background: rgba(255, 255, 255, 0.8);
  color: #2563eb;
  border-color: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.1);
}

.gradient-btn {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  padding: 10px 24px;
  border-radius: 12px;
  font-weight: 600;
  height: 44px;
  color: white;
  transition: all 0.3s ease;
}

.gradient-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.4);
}

/* Stats */
.stats-section {
  margin-bottom: 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
}

.stat-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  position: relative;
  overflow: hidden;
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

.gradient-blue { background: linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%); }
.gradient-green { background: linear-gradient(135deg, #10b981 0%, #34d399 100%); }
.gradient-purple { background: linear-gradient(135deg, #8b5cf6 0%, #d946ef 100%); }
.gradient-orange { background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%); }
.gradient-pink { background: linear-gradient(135deg, #ec4899 0%, #f472b6 100%); }
.gradient-indigo { background: linear-gradient(135deg, #6366f1 0%, #818cf8 100%); }

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

/* Filters */
.filter-section {
  margin-bottom: 32px;
}

.filter-card {
  padding: 24px;
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

/* Deep selector for Element Plus inputs to override styles */
.glass-select :deep(.el-input__wrapper),
.glass-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5);
  box-shadow: none !important;
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 12px;
  padding: 8px 16px;
  height: 40px;
  transition: all 0.3s;
}

.glass-select :deep(.el-input__wrapper:hover),
.glass-input :deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.8);
  border-color: #3b82f6;
}

.glass-select :deep(.el-input__wrapper.is-focus),
.glass-input :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2) !important;
  border-color: #3b82f6;
}

.glass-select.small :deep(.el-input__wrapper) {
  height: 32px;
  padding: 4px 12px;
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

/* Questions List */
.questions-section {
  margin-bottom: 32px;
}

.section-header {
  padding: 16px 24px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  display: inline-block;
  margin-right: 16px;
}

.question-count {
  font-size: 13px;
  color: var(--text-secondary);
  background: rgba(0, 0, 0, 0.05);
  padding: 4px 10px;
  border-radius: 20px;
}

.questions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 28px;
}

.question-card {
  padding: 24px;
  display: flex;
  flex-direction: column;
  min-height: 320px;
  position: relative;
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.question-type {
  display: flex;
  gap: 10px;
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

.type-badge.single { background: rgba(59, 130, 246, 0.1); color: #3b82f6; border: 1px solid rgba(59, 130, 246, 0.2); }
.type-badge.multiple { background: rgba(139, 92, 246, 0.1); color: #8b5cf6; border: 1px solid rgba(139, 92, 246, 0.2); }
.type-badge.judge { background: rgba(16, 185, 129, 0.1); color: #10b981; border: 1px solid rgba(16, 185, 129, 0.2); }
.type-badge.fill { background: rgba(245, 158, 11, 0.1); color: #f59e0b; border: 1px solid rgba(245, 158, 11, 0.2); }
.type-badge.essay { background: rgba(236, 72, 153, 0.1); color: #ec4899; border: 1px solid rgba(236, 72, 153, 0.2); }
.type-badge.coding { background: rgba(99, 102, 241, 0.1); color: #6366f1; border: 1px solid rgba(99, 102, 241, 0.2); }

.difficulty-badge {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
}

.difficulty-badge.easy { background: rgba(22, 163, 74, 0.1); color: #16a34a; }
.difficulty-badge.medium { background: rgba(217, 119, 6, 0.1); color: #d97706; }
.difficulty-badge.hard { background: rgba(220, 38, 38, 0.1); color: #dc2626; }

.icon-btn {
  color: var(--text-secondary);
  font-size: 16px;
  transition: color 0.2s;
}

.icon-btn:hover {
  color: #3b82f6;
}

.question-content {
  margin-bottom: 24px;
  flex: 1;
}

.question-text {
  font-size: 16px;
  color: var(--text-primary);
  line-height: 1.6;
  margin-bottom: 20px;
  cursor: pointer;
  font-weight: 500;
}

.question-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.2s;
}

.option-item:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(59, 130, 246, 0.3);
}

.option-item.correct {
  background: rgba(34, 197, 94, 0.1);
  border-color: rgba(34, 197, 94, 0.3);
}

.option-label {
  font-weight: 700;
  color: var(--text-secondary);
  width: 24px;
}

.option-text {
  flex: 1;
  color: var(--text-primary);
  font-size: 14px;
  cursor: pointer;
}

.correct-icon {
  color: #22c55e;
}

.question-answer {
  background: rgba(248, 250, 252, 0.6);
  padding: 16px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.03);
}

.answer-label {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.answer-content {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
  cursor: pointer;
}

.judge-options {
  display: flex;
  gap: 16px;
}

.judge-option {
  padding: 10px 24px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.judge-option.correct {
  background: rgba(34, 197, 94, 0.1);
  color: #16a34a;
  border: 1px solid rgba(34, 197, 94, 0.3);
  font-weight: 600;
}

.question-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  margin-top: auto;
}

.question-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

.question-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  max-width: 60%;
  justify-content: flex-end;
}

.glass-tag {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(0, 0, 0, 0.05);
  color: var(--text-secondary);
  border-radius: 6px;
  padding: 2px 8px;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.glass-pagination :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #2563eb;
}

.glass-pagination :deep(.el-pagination.is-background .el-pager li) {
  background-color: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 8px;
  margin: 0 4px;
}

.glass-pagination :deep(.btn-prev),
.glass-pagination :deep(.btn-next) {
  background-color: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 8px;
}

.text-clamp-1 {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hover-full-text {
  white-space: pre-wrap;
  max-width: 420px;
  line-height: 1.6;
  color: #334155;
  font-size: 13px;
}

.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* Dialog */
.create-form {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 10px;
}

.options-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item-edit {
  display: flex;
  gap: 12px;
  align-items: center;
}

.remove-option-btn {
  flex-shrink: 0;
}

.add-option-btn {
  align-self: flex-start;
}

.selected-course-card {
  padding: 16px;
  border-radius: 12px;
}

.course-card-header {
  display: flex;
  gap: 16px;
}

.course-cover {
  width: 100px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}

.course-info {
  flex: 1;
}

.course-title {
  margin: 0 0 4px;
  font-size: 14px;
  color: #2c3e50;
}

.course-description {
  margin: 0 0 8px;
  font-size: 12px;
  color: #64748b;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tag-input-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tags-container {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ai-btn {
  background: linear-gradient(135deg, #8b5cf6 0%, #d946ef 100%);
  color: white;
  border: none;
}

.ai-btn:hover {
  background: linear-gradient(135deg, #7c3aed 0%, #c026d3 100%);
  color: white;
}

@media (max-width: 1200px) {
  .questions-grid {
    grid-template-columns: 1fr;
  }
}
</style>
