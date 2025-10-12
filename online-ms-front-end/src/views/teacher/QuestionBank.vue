<template>
  <div class="question-bank-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <i class="fas fa-question-circle"></i>
            题库管理
          </h1>
          <p class="page-subtitle">管理和组织您的题目资源</p>
        </div>
        <div class="header-actions">
          <el-button class="action-btn import-btn">
            <font-awesome-icon :icon="['fas', 'file-import']" />
            批量导入
          </el-button>
          <el-button type="primary" class="action-btn create-btn" @click="openCreateDialog">
            <font-awesome-icon :icon="['fas', 'plus']" />
            创建题目
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-question-circle"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalQuestions }}</div>
            <div class="stat-label">总题目数</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-check-circle"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.singleChoice }}</div>
            <div class="stat-label">单选题</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-list-ul"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.multipleChoice }}</div>
            <div class="stat-label">多选题</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-edit"></i>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.essay }}</div>
            <div class="stat-label">问答题</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <div class="filter-card">
        <div class="filter-row">
          <div class="filter-group">
            <label class="filter-label">题目类型</label>
            <el-select v-model="filters.type" placeholder="全部类型" class="filter-select">
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
            <el-select v-model="filters.difficulty" placeholder="全部难度" class="filter-select">
              <el-option label="全部难度" value="" />
              <el-option label="简单" value="easy" />
              <el-option label="中等" value="medium" />
              <el-option label="困难" value="hard" />
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
            <label class="filter-label">标签</label>
            <el-select v-model="filters.tags" placeholder="选择标签" multiple class="filter-select">
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
              class="search-input"
            >
              <template #prefix>
                <font-awesome-icon :icon="['fas', 'search']" />
              </template>
            </el-input>
          </div>
          
          <div class="action-group">
            <el-button class="filter-btn" @click="resetFilters">
              <font-awesome-icon :icon="['fas', 'undo']" />
              重置
            </el-button>
            <el-button type="primary" class="filter-btn">
              <font-awesome-icon :icon="['fas', 'search']" />
              搜索
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 题目列表 -->
    <div class="questions-section">
      <div class="section-header">
        <div class="header-left">
          <h3>题目列表</h3>
          <span class="question-count">共 {{ filteredQuestions.length }} 道题目</span>
        </div>
        <div class="header-actions">
          <el-select v-model="sortBy" placeholder="排序方式" class="sort-select">
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
            class="question-card"
          >
            <div class="question-header">
              <div class="question-type">
                <span :class="['type-badge', question.type]">
                  <i :class="getTypeIcon(question.type)"></i>
                  {{ getTypeName(question.type) }}
                </span>
                <span :class="['difficulty-badge', question.difficulty]">
                  {{ getDifficultyName(question.difficulty) }}
                </span>
              </div>
              <div class="question-actions">
                <el-dropdown trigger="click">
                  <el-button class="action-btn">
                    <i class="fas fa-ellipsis-v"></i>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item>
                        <font-awesome-icon :icon="['fas', 'edit']" /> 编辑
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <font-awesome-icon :icon="['fas', 'copy']" /> 复制
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <font-awesome-icon :icon="['fas', 'eye']" /> 预览
                      </el-dropdown-item>
                      <el-dropdown-item divided>
                        <font-awesome-icon :icon="['fas', 'trash']" /> 删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            
            <div class="question-content">
              <div class="question-text">{{ question.content }}</div>
              
              <div v-if="question.type === 'single' || question.type === 'multiple'" class="question-options">
                <div 
                  v-for="(option, index) in question.options" 
                  :key="index"
                  :class="['option-item', { correct: question.correctAnswers.includes(index) }]"
                >
                  <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
                  <span class="option-text">{{ option }}</span>
                  <i v-if="question.correctAnswers.includes(index)" class="fas fa-check correct-icon"></i>
                </div>
              </div>
              
              <div v-if="question.type === 'fill'" class="question-answer">
                <div class="answer-label">参考答案：</div>
                <div class="answer-content" v-html="question.answer ? question.answer.replace(/\n/g, '<br>') : ''"></div>
              </div>
              
              <div v-if="question.type === 'essay'" class="question-answer">
                <div class="answer-label">参考答案：</div>
                <div class="answer-content" v-html="question.answer ? question.answer.replace(/\n/g, '<br>') : ''"></div>
              </div>
              
              <div v-if="question.type === 'judge'" class="question-judge">
                <div class="judge-options">
                  <div :class="['judge-option', { 'correct': question.answer === true, 'selected': question.answer === true }]">
                    <span class="judge-label">正确</span>
                    <i v-if="question.answer === true" class="fas fa-check correct-icon"></i>
                  </div>
                  <div :class="['judge-option', { 'correct': question.answer === false, 'selected': question.answer === false }]">
                    <span class="judge-label">错误</span>
                    <i v-if="question.answer === false" class="fas fa-check correct-icon"></i>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="question-footer">
              <div class="question-meta">
                <span class="meta-item">
                  <i class="fas fa-book"></i>
                  {{ question.course }}
                </span>
                <span class="meta-item">
                  <i class="fas fa-clock"></i>
                  {{ question.createTime }}
                </span>
              </div>
              
              <div class="question-tags">
                <el-tag 
                  v-for="tag in question.tags" 
                  :key="tag"
                  size="small"
                  class="question-tag"
                >
                  {{ tag }}
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
            class="custom-pagination"
          />
        </div>
      </div>
    </div>

    <!-- 创建题目对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      title="创建新题目" 
      width="800px"
      class="create-dialog"
    >
      <div class="create-form">
        <el-form :model="newQuestion" label-width="100px">
          <el-form-item label="题目类型">
            <el-select v-model="newQuestion.type" placeholder="请选择题目类型">
              <el-option label="单选题" value="single" />
              <el-option label="多选题" value="multiple" />
              <el-option label="判断题" value="judge" />
              <el-option label="填空题" value="fill" />
              <el-option label="问答题" value="essay" />
              <el-option label="编程题" value="coding" />
            </el-select>
          </el-form-item>
          <el-form-item label="题目难度">
            <el-select v-model="newQuestion.difficulty" placeholder="请选择题目难度">
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
            />
          </el-form-item>
          
          <!-- 选择题选项 -->
          <template v-if="newQuestion.type === 'single' || newQuestion.type === 'multiple'">
            <el-form-item label="选项设置">
              <div class="options-container">
                <div v-for="(option, index) in newQuestion.options" :key="index" class="option-item">
                  <el-input 
                    v-model="newQuestion.options[index]" 
                    :placeholder="`选项 ${String.fromCharCode(65 + index)}`"
                    class="option-input"
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
                  type="primary" 
                  size="small"
                  plain
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
              <el-select v-model="newQuestion.answer" placeholder="请选择正确答案">
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
              />
            </el-form-item>
          </template>
          
          <el-form-item label="答案解析">
            <el-input 
              v-model="newQuestion.analysis" 
              type="textarea" 
              :rows="3"
              placeholder="请输入答案解析（可选）"
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
            <div class="selected-course-card">
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
                      <i class="fas fa-users"></i>
                      {{ selectedCourse.enrollmentCount || 0 }} 人学习
                    </span>
                    <span class="meta-item">
                      <i class="fas fa-clock"></i>
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
            />
            <div class="tag-input-hint">
              <i class="fas fa-info-circle"></i>
              多个标签请用中文顿号、分隔
            </div>
            <div class="tags-container" v-if="newQuestion.tags.length > 0">
              <el-tag 
                v-for="(tag, index) in newQuestion.tags" 
                :key="index" 
                closable 
                @close="removeTag(index)"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
            </div>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="createQuestionAISuggestion" :loading="aiSuggestionLoading" :disabled="aiSuggestionLoading">AI智能建议</el-button>
          <el-button type="primary" @click="createQuestion">创建题目</el-button>
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
  essay: 46
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
  
  stats.value = {
    totalQuestions: total,
    singleChoice,
    multipleChoice,
    essay
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
.question-bank-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f0f3 0%, #e8e8eb 100%);
  padding: 0;
}

/* 页面头部 */
.page-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 24px 30px;
}

/* 选中课程卡片样式 */
.selected-course-card {
  background: #f0f0f3;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
  transition: all 0.3s ease;
}

.selected-course-card:hover {
  box-shadow: 
    4px 4px 8px #d1d1d4,
    -4px -4px 8px #ffffff;
  transform: translateY(-2px);
}

.course-card-header {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.course-cover {
  width: 80px;
  height: 60px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 
    4px 4px 8px #d1d1d4,
    -4px -4px 8px #ffffff;
}

.course-info {
  flex: 1;
  min-width: 0;
}

.course-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.course-description {
  font-size: 14px;
  color: #606266;
  margin: 0 0 12px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.course-meta .meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
}

.course-meta .meta-item i {
  font-size: 12px;
  color: #002FA7;
}

/* 标签输入提示样式 */
.tag-input-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.tag-input-hint i {
  color: #409EFF;
  font-size: 12px;
}

/* 创建题目对话框样式 */
.create-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.create-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 30px;
  border-bottom: none;
}

.create-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
}

.create-dialog :deep(.el-dialog__body) {
  padding: 30px;
  background: #f8f9fa;
}

.create-form {
  max-height: 70vh;
  overflow-y: auto;
}

.create-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #2c3e50;
}

.option-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.option-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  background: #f0f0f3;
  box-shadow: 
    4px 4px 8px #d1d1d4,
    -4px -4px 8px #ffffff;
}

.option-btn:hover {
  transform: translateY(-1px);
  box-shadow: 
    2px 2px 4px #d1d1d4,
    -2px -2px 4px #ffffff;
}

.option-btn:active {
  transform: translateY(0);
  box-shadow: 
    inset 2px 2px 4px #d1d1d4,
    inset -2px -2px 4px #ffffff;
}

.add-option-btn {
  color: #002FA7;
  font-weight: bold;
}

.add-option-btn:hover {
  color: #517B4D;
}

.remove-option-btn {
  margin-left: 10px;
  color: #F56C6C;
  background: transparent;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: bold;
}

.remove-option-btn:hover {
  color: #E6A23C;
}

.create-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: inset 2px 2px 5px rgba(0, 0, 0, 0.1), inset -2px -2px 5px rgba(255, 255, 255, 0.8);
  border: none;
  background: #f0f0f3;
}

.create-form :deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
  box-shadow: inset 2px 2px 5px rgba(0, 0, 0, 0.1), inset -2px -2px 5px rgba(255, 255, 255, 0.8);
}

.create-form :deep(.el-textarea__inner) {
  border-radius: 12px;
  box-shadow: inset 2px 2px 5px rgba(0, 0, 0, 0.1), inset -2px -2px 5px rgba(255, 255, 255, 0.8);
  border: none;
  background: #f0f0f3;
}

/* 选项容器样式 */
.options-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.option-input {
  flex: 1;
}

.option-item :deep(.el-button.is-circle) {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.1), -2px -2px 5px rgba(255, 255, 255, 0.8);
  border: none;
  background: #f0f0f3;
}

/* 标签容器样式 */
.tags-container {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  border-radius: 20px;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.1), -2px -2px 5px rgba(255, 255, 255, 0.8);
  border: none;
  background: #f0f0f3;
  color: #667eea;
  font-weight: 500;
}

/* 对话框底部按钮样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 30px;
  background: #f8f9fa;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.dialog-footer :deep(.el-button) {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  box-shadow: 
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
  border: none;
  background: #f0f0f3;
  color: #2c3e50;
  transition: all 0.3s ease;
}

.dialog-footer :deep(.el-button--primary) {
  background: #f0f0f3;
  color: #002FA7;
  box-shadow: 
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
}

.dialog-footer :deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 
    4px 4px 8px #d1d1d4,
    -4px -4px 8px #ffffff;
}

.dialog-footer :deep(.el-button--primary:hover) {
  color: #517B4D;
  transform: translateY(-2px);
  box-shadow: 
    4px 4px 8px #d1d1d4,
    -4px -4px 8px #ffffff;
}

.dialog-footer :deep(.el-button:active) {
  transform: translateY(0);
  box-shadow: 
    inset 4px 4px 8px #d1d1d4,
    inset -4px -4px 8px #ffffff;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
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

.action-btn {
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
  background: #f0f0f3;
  border: none;
  cursor: pointer;
  box-shadow: 
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 
    4px 4px 8px #d1d1d4,
    -4px -4px 8px #ffffff;
}

.action-btn:active {
  transform: translateY(0);
  box-shadow: 
    inset 4px 4px 8px #d1d1d4,
    inset -4px -4px 8px #ffffff;
}

.import-btn {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  color: #6c757d;
}

.create-btn {
  background: #f0f0f3;
  border: none;
  color: #002FA7;
  box-shadow: 
    8px 8px 16px #d1d1d4,
    -8px -8px 16px #ffffff;
}

.create-btn:hover {
  color: #517B4D;
  transform: translateY(-2px);
  box-shadow: 
    4px 4px 8px #d1d1d4,
    -4px -4px 8px #ffffff;
}

.create-btn:active {
  transform: translateY(0);
  box-shadow: 
    inset 4px 4px 8px #d1d1d4,
    inset -4px -4px 8px #ffffff;
}

/* 统计卡片 */
.stats-section {
  padding: 30px;
  padding-bottom: 0;
}

.stats-grid {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 
    20px 20px 40px rgba(0, 0, 0, 0.1),
    -20px -20px 40px rgba(255, 255, 255, 0.8),
    inset 0 0 0 1px rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    25px 25px 50px rgba(0, 0, 0, 0.15),
    -25px -25px 50px rgba(255, 255, 255, 0.9);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  background: linear-gradient(135deg, #002FA7, #517B4D);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1;
}

.stat-label {
  color: #6b7280;
  font-size: 14px;
  margin-top: 4px;
}

/* 筛选区域 */
.filter-section {
  padding: 30px;
  padding-bottom: 0;
}

.filter-card {
  max-width: 1400px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 
    20px 20px 40px rgba(0, 0, 0, 0.1),
    -20px -20px 40px rgba(255, 255, 255, 0.8);
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-label {
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.filter-select {
  width: 100%;
}

.search-row {
  display: flex;
  gap: 16px;
  align-items: end;
}

.search-group {
  flex: 1;
}

.search-input {
  width: 100%;
}

.action-group {
  display: flex;
  gap: 12px;
}

.filter-btn {
  padding: 10px 20px;
  border-radius: 10px;
}

/* 题目列表 */
.questions-section {
  padding: 30px;
}

.section-header {
  max-width: 1400px;
  margin: 0 auto 24px auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.question-count {
  color: #6b7280;
  font-size: 14px;
  margin-left: 12px;
}

.header-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}



.sort-select {
  width: 150px;
}

.questions-content {
  max-width: 1400px;
  margin: 0 auto;
}

.questions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.question-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 
    15px 15px 30px rgba(0, 0, 0, 0.1),
    -15px -15px 30px rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.question-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    20px 20px 40px rgba(0, 0, 0, 0.15),
    -20px -20px 40px rgba(255, 255, 255, 0.9);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.question-type {
  display: flex;
  gap: 8px;
}

.type-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.type-badge.single {
  background: rgba(0, 47, 167, 0.1);
  color: #002FA7;
}

.type-badge.multiple {
  background: rgba(81, 123, 77, 0.1);
  color: #517B4D;
}

.type-badge.fill {
  background: rgba(255, 193, 7, 0.1);
  color: #ffc107;
}

.type-badge.essay {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.type-badge.coding {
  background: rgba(111, 66, 193, 0.1);
  color: #6f42c1;
}

.difficulty-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.difficulty-badge.easy {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.difficulty-badge.medium {
  background: rgba(255, 193, 7, 0.1);
  color: #ffc107;
}

.difficulty-badge.hard {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.question-content {
  margin-bottom: 16px;
}

.question-text {
  font-size: 16px;
  color: #2c3e50;
  line-height: 1.6;
  margin-bottom: 12px;
}

.question-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  background: rgba(248, 249, 250, 0.8);
  transition: all 0.2s ease;
}

.option-item.correct {
  background: rgba(40, 167, 69, 0.1);
  border: 1px solid rgba(40, 167, 69, 0.2);
}

.option-label {
  font-weight: 600;
  color: #6b7280;
  min-width: 20px;
}

.option-text {
  flex: 1;
  color: #374151;
}

.correct-icon {
  color: #28a745;
}

.question-answer {
  padding: 12px;
  background: rgba(248, 249, 250, 0.8);
  border-radius: 8px;
}

.question-judge {
  margin-top: 12px;
}

.judge-options {
  display: flex;
  gap: 12px;
  justify-content: flex-start;
}

.judge-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 8px;
  background: rgba(248, 249, 250, 0.8);
  border: 1px solid rgba(229, 231, 235, 0.8);
  transition: all 0.2s ease;
  min-width: 80px;
  justify-content: center;
}

.judge-option.correct {
  background: rgba(40, 167, 69, 0.1);
  border: 1px solid rgba(40, 167, 69, 0.3);
}

.judge-option.selected {
  font-weight: 600;
}

.judge-label {
  color: #374151;
  font-size: 14px;
}

.judge-option.correct .judge-label {
  color: #28a745;
  font-weight: 600;
}

.answer-label {
  font-weight: 500;
  color: #6b7280;
  margin-bottom: 4px;
}

.answer-content {
  color: #374151;
}

.question-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.question-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
}

.question-tags {
  display: flex;
  gap: 6px;
}

.question-tag {
  background: rgba(0, 47, 167, 0.1);
  color: #002FA7;
  border: none;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.custom-pagination {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 16px 24px;
  box-shadow: 
    15px 15px 30px rgba(0, 0, 0, 0.1),
    -15px -15px 30px rgba(255, 255, 255, 0.8);
}

/* 对话框样式 */
.create-dialog :deep(.el-dialog) {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
}

.create-form {
  padding: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .questions-grid {
    grid-template-columns: 1fr;
  }
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
  .questions-section {
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
  
  .question-footer {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
}
</style>