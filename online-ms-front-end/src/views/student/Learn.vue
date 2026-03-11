<template>
  <div class="learn-page">
    <!-- 课程学习头部 -->
    <div class="learn-header glass-card">
      <div class="course-info">
        <div class="course-meta">
          <el-breadcrumb separator=">">
            <el-breadcrumb-item :to="{ name: 'MyCourses' }">我的课程</el-breadcrumb-item>
            <el-breadcrumb-item>{{ courseInfo.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1 class="course-title">{{ courseInfo.title }}</h1>
        <div class="progress-info">
          <span class="progress-text">学习进度：{{ courseProgress }}%</span>
          <el-progress :percentage="courseProgress" :stroke-width="8" class="progress-bar" />
        </div>
      </div>
      <div class="course-actions">
        <el-button type="primary" class="neu-button" @click="toggleNotes">
          <i class="fas fa-sticky-note"></i>
          笔记
        </el-button>
        <el-button class="neu-button" @click="toggleBookmarks">
          <font-awesome-icon :icon="['fas', 'bookmark']" />
          收藏
        </el-button>
        <el-button class="neu-button" @click="shareLesson">
          <font-awesome-icon :icon="['fas', 'share']" />
          分享
        </el-button>
      </div>
    </div>

    <div class="learn-content">
      <!-- 左侧章节列表 -->
      <div class="chapter-sidebar glass-card">
        <div class="sidebar-header">
          <h3>课程目录</h3>
          <el-button text @click="toggleSidebar">
            <font-awesome-icon :icon="['fas', 'bars']" />
          </el-button>
        </div>
        <div class="chapter-list">
          <div 
            v-for="(chapter, index) in formattedChapters" 
            :key="chapter.id"
            class="chapter-item"
            :class="{ active: currentChapter === chapter.id }"
          >
            <div class="chapter-header" @click="selectChapter(chapter.id)">
              <div class="chapter-info">
                <span class="chapter-number">{{ index + 1 }}</span>
                <span class="chapter-title">{{ chapter.title }}</span>
              </div>
              <div class="chapter-status">
                <i v-if="chapter.completed" class="fas fa-check-circle completed"></i>
                <i v-else-if="chapter.current" class="fas fa-play-circle current"></i>
                <i v-else class="fas fa-circle pending"></i>
              </div>
            </div>
            <div v-if="chapter.lessons" class="lesson-list">
              <div 
                v-for="lesson in chapter.lessons" 
                :key="lesson.id"
                class="lesson-item"
                :class="{ active: currentLesson === lesson.id }"
                @click="selectLesson(lesson.id)"
              >
                <div class="lesson-info">
                  <i :class="getLessonIcon(lesson.type)"></i>
                  <span class="lesson-title">{{ lesson.title }}</span>
                  <span class="lesson-duration">{{ lesson.duration }}</span>
                </div>
                <div class="lesson-status">
                  <i v-if="lesson.completed" class="fas fa-check-circle completed"></i>
                  <i v-else-if="lesson.current" class="fas fa-play-circle current"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 主要学习区域 -->
      <div class="main-content">
        <!-- 视频播放器 -->
        <div class="video-container neu-card">
          <div class="video-player">
            <video 
              v-if="currentVideoUrl"
              ref="videoElement"
              :src="currentVideoUrl"
              controls
              class="video-element"
              @play="handleVideoPlay"
              @pause="handleVideoPause"
              @timeupdate="handleTimeUpdate"
              @loadedmetadata="handleVideoLoaded"
              @ended="handleVideoEnded"
            >
              您的浏览器不支持视频播放。
            </video>
            <div v-else class="video-placeholder">
              <i class="fas fa-play-circle"></i>
              <p>暂无视频内容</p>
              <p class="video-title">{{ currentLessonInfo.title }}</p>
            </div>
          </div>
          <div class="video-controls">
            <div class="control-left">
              <el-button @click="previousLesson" :disabled="!hasPrevious">
                <i class="fas fa-step-backward"></i>
                上一节
              </el-button>
              <el-button type="primary" @click="togglePlay">
                <i :class="isPlaying ? 'fas fa-pause' : 'fas fa-play'"></i>
                {{ isPlaying ? '暂停' : '播放' }}
              </el-button>
              <el-button @click="nextLesson" :disabled="!hasNext">
                下一节
                <i class="fas fa-step-forward"></i>
              </el-button>
            </div>
            <div class="control-right">
              <el-button @click="toggleSpeed">
                <i class="fas fa-tachometer-alt"></i>
                {{ playSpeed }}x
              </el-button>
              <el-button @click="toggleFullscreen">
                <i class="fas fa-expand"></i>
              </el-button>
            </div>
          </div>
        </div>

        <!-- 课程内容标签页 -->
        <div class="content-tabs neu-card">
          <el-tabs v-model="activeTab" class="custom-tabs">

            <el-tab-pane name="materials">
              <template #label>
                <span>
                  <font-awesome-icon :icon="['fas', 'file-alt']" />
                  课件资料
                </span>
              </template>
              <div class="materials-container">
                <!-- 课程级别资料 -->
                <div v-if="courseContent.courseMaterials && courseContent.courseMaterials.length > 0" class="materials-section">
                  <div class="section-header">
                    <h4>
                      <font-awesome-icon :icon="['fas', 'graduation-cap']" />
                      课程资料
                    </h4>
                    <span class="material-count">({{ courseContent.courseMaterials.length }}个文件)</span>
                  </div>
                  <div class="materials-list">
                    <div v-for="material in courseContent.courseMaterials" :key="material.id" class="material-item neu-item">
                      <div class="material-info">
                        <i :class="getMaterialIcon(material.mimeType)"></i>
                        <div class="material-details">
                          <span class="material-name">{{ material.title }}</span>
                          <span class="material-type">{{ formatMimeType(material.mimeType) }}</span>
                        </div>
                      </div>
                      <el-button 
                        type="primary" 
                        size="small" 
                        class="download-btn"
                        @click="downloadMaterial(material)"
                        :loading="downloadingMaterials.includes(material.id)"
                      >
                        <font-awesome-icon :icon="['fas', 'download']" />
                        下载
                      </el-button>
                    </div>
                  </div>
                </div>

                <!-- 当前小节资料 -->
                <div v-if="currentSectionMaterials.length > 0" class="materials-section">
                  <div class="section-header">
                    <h4>
                      <font-awesome-icon :icon="['fas', 'video']" />
                      本节资料
                    </h4>
                    <span class="material-count">({{ currentSectionMaterials.length }}个文件)</span>
                  </div>
                  <div class="materials-list">
                    <div v-for="material in currentSectionMaterials" :key="material.id" class="material-item neu-item">
                      <div class="material-info">
                        <i :class="getMaterialIcon(material.mimeType)"></i>
                        <div class="material-details">
                          <span class="material-name">{{ material.title }}</span>
                          <span class="material-type">{{ formatMimeType(material.mimeType) }}</span>
                        </div>
                      </div>
                      <el-button 
                        type="primary" 
                        size="small" 
                        class="download-btn"
                        @click="downloadMaterial(material)"
                        :loading="downloadingMaterials.includes(material.id)"
                      >
                        <font-awesome-icon :icon="['fas', 'download']" />
                        下载
                      </el-button>
                    </div>
                  </div>
                </div>

                <!-- 无资料提示 -->
                <div v-if="!courseContent.courseMaterials?.length && !currentSectionMaterials.length" class="no-materials">
                  <div class="empty-state">
                    <font-awesome-icon :icon="['fas', 'folder-open']" class="empty-icon" />
                    <p>暂无课件资料</p>
                    <p class="empty-desc">老师还未上传相关资料</p>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane name="discussion">
              <template #label>
                <span>
                  <font-awesome-icon :icon="['fas', 'comments']" />
                  讨论区
                </span>
              </template>
              <div class="discussion-area">
                <div class="discussion-input">
                  <el-input
                    v-model="newComment"
                    type="textarea"
                    :rows="3"
                    placeholder="发表你的想法..."
                    class="neu-input"
                  />
                  <el-button type="primary" class="submit-btn">
                    <font-awesome-icon :icon="['fas', 'paper-plane']" />
                    发表
                  </el-button>
                </div>
                <div class="comments-list">
                  <div v-for="comment in comments" :key="comment.id" class="comment-item">
                    <div class="comment-avatar">
                      <img :src="comment.avatar" :alt="comment.username" />
                    </div>
                    <div class="comment-content">
                      <div class="comment-header">
                        <span class="username">{{ comment.username }}</span>
                        <span class="time">{{ comment.time }}</span>
                      </div>
                      <div class="comment-text">{{ comment.content }}</div>
                      <div class="comment-actions">
                        <el-button text size="small">
                          <font-awesome-icon :icon="['fas', 'thumbs-up']" />
                          {{ comment.likes }}
                        </el-button>
                        <el-button text size="small">
                          <font-awesome-icon :icon="['fas', 'reply']" />
                          回复
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>

    <!-- 笔记侧边栏 -->
    <div v-if="showNotes" class="notes-sidebar glass-card">
      <div class="notes-header">
        <h3>我的笔记</h3>
        <el-button text @click="toggleNotes">
          <i class="fas fa-times"></i>
        </el-button>
      </div>
      <div class="notes-content">
        <el-input 
          v-model="noteContent" 
          type="textarea" 
          :rows="10" 
          placeholder="在这里记录你的学习笔记..."
          class="neu-input"
        />
        <el-button type="primary" class="save-note-btn">
          <i class="fas fa-save"></i>
          保存笔记
        </el-button>
      </div>
    </div>

    <!-- 章节切换确认对话框 -->
    <el-dialog
      v-model="showChapterConfirmDialog"
      title="章节切换确认"
      width="480px"
      :before-close="cancelChapterSwitch"
      class="chapter-confirm-dialog"
    >
      <div class="confirm-content">
        <div class="confirm-icon">
          <i class="fas fa-question-circle"></i>
        </div>
        <div class="confirm-text">
          <h3>确认切换章节</h3>
          <p>您即将切换到新的章节，请选择要学习的具体小节：</p>
          <div class="lesson-options" v-if="pendingChapterId">
            <div 
              v-for="lesson in formattedChapters.find(c => c.id === pendingChapterId)?.lessons || []"
              :key="lesson.id"
              class="lesson-option"
              :class="{ selected: currentLesson === lesson.id }"
              @click="currentLesson = lesson.id"
            >
              <div class="lesson-option-info">
                <i :class="getLessonIcon(lesson.type)"></i>
                <span class="lesson-option-title">{{ lesson.title }}</span>
                <span class="lesson-option-duration">{{ lesson.duration }}</span>
              </div>
              <div class="lesson-option-status">
                <i v-if="lesson.completed" class="fas fa-check-circle completed"></i>
                <i v-else-if="lesson.current" class="fas fa-play-circle current"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelChapterSwitch" class="neu-button">
            <i class="fas fa-times"></i>
            取消
          </el-button>
          <el-button type="primary" @click="confirmChapterSwitch" class="neu-button">
            <i class="fas fa-check"></i>
            确认切换
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getCourseContent, updateVideoProgress } from '@/api/students/stuAPI.js'
import { takeAccessToken } from '@/api/index.js'
import { ElMessage } from 'element-plus'

const route = useRoute()
const courseId = route.params.courseId

// 响应式数据
const courseInfo = ref({
  id: courseId,
  title: 'Vue.js 3.0 完整开发教程',
  instructor: '张老师',
  totalLessons: 24
})

const courseProgress = ref(45)
const currentChapter = ref(1)
const currentLesson = ref(3)
const activeTab = ref('intro')
const showNotes = ref(false)
const isPlaying = ref(false)
const playSpeed = ref(1.0)
const noteContent = ref('')
const newComment = ref('')
const currentVideoUrl = ref('')

// 新增：章节切换确认状态
const showChapterConfirmDialog = ref(false)
const pendingChapterId = ref(null)

// 视频学习进度相关数据
const videoElement = ref(null)
const currentProgress = ref(0) // 当前播放进度（秒）
const lastSavedProgress = ref(0) // 上次保存的进度
const progressSaveTimer = ref(null) // 定期保存定时器
const isProgressSaving = ref(false) // 是否正在保存进度
const PROGRESS_SAVE_INTERVAL = 10000 // 10秒自动保存一次
const PROGRESS_SAVE_THRESHOLD = 5 // 进度差异超过5秒才保存

// 课程内容数据（从API获取）
const courseContent = ref({
  courseId: '',
  title: '',
  courseMaterials: [],
  chapters: []
})

// 资料相关数据
const downloadingMaterials = ref([]) // 正在下载的资料ID列表

// 章节数据（保持原有结构作为默认数据）
const chapters = ref([
  {
    id: 1,
    title: 'Vue.js 基础入门',
    completed: true,
    current: false,
    lessons: [
      { id: 1, title: 'Vue.js 简介', type: 'video', duration: '15:30', completed: true },
      { id: 2, title: '环境搭建', type: 'video', duration: '12:45', completed: true },
      { id: 3, title: '第一个Vue应用', type: 'video', duration: '18:20', completed: true, current: true }
    ]
  },
  {
    id: 2,
    title: 'Composition API',
    completed: false,
    current: true,
    lessons: [
      { id: 4, title: 'setup函数', type: 'video', duration: '20:15', completed: false },
      { id: 5, title: 'ref和reactive', type: 'video', duration: '25:30', completed: false },
      { id: 6, title: '计算属性和监听器', type: 'video', duration: '22:10', completed: false }
    ]
  },
  {
    id: 3,
    title: '组件开发',
    completed: false,
    current: false,
    lessons: [
      { id: 7, title: '组件基础', type: 'video', duration: '18:45', completed: false },
      { id: 8, title: '组件通信', type: 'video', duration: '24:20', completed: false },
      { id: 9, title: '插槽使用', type: 'video', duration: '16:30', completed: false }
    ]
  }
])

// 当前课程信息
const currentLessonInfo = ref({
  title: '第一个Vue应用',
  description: '在这节课中，我们将创建第一个Vue.js应用程序，了解Vue的基本语法和数据绑定。',
  objectives: [
    '理解Vue实例的创建过程',
    '掌握模板语法和数据绑定',
    '学会使用指令控制DOM',
    '了解事件处理机制'
  ]
})

// 课件资料
const materials = ref([
  { id: 1, name: 'Vue.js基础PPT', type: 'ppt', size: '2.5MB' },
  { id: 2, name: '示例代码', type: 'zip', size: '1.2MB' },
  { id: 3, name: '学习笔记模板', type: 'doc', size: '0.8MB' }
])

// 讨论评论
const comments = ref([
  {
    id: 1,
    username: '小明',
    avatar: '/api/placeholder/40/40',
    time: '2小时前',
    content: '这节课讲得很清楚，终于理解了Vue的响应式原理！',
    likes: 12
  },
  {
    id: 2,
    username: '小红',
    avatar: '/api/placeholder/40/40',
    time: '1天前',
    content: '老师能不能再详细讲解一下组件的生命周期？',
    likes: 8
  }
])

// 计算属性
// 将后端数据格式转换为前端需要的格式
const formattedChapters = computed(() => {
  if (!courseContent.value.chapters || courseContent.value.chapters.length === 0) {
    return chapters.value // 使用默认数据
  }
  
  return courseContent.value.chapters.map(chapter => {
    const lessons = chapter.sections ? chapter.sections.map(section => ({
      id: section.id,
      title: section.title,
      type: section.videoUrl ? 'video' : 'text', // 根据是否有视频URL判断类型
      duration: formatDuration(section.durationSeconds || 0),
      completed: section.status === 'completed',
      current: section.id === currentLesson.value,
      videoUrl: formatVideoUrl(section.videoUrl),
      materials: section.materials || [], // 添加资料信息
      progressSeconds: section.progressSeconds || 0,
      orderIndex: section.orderIndex || 0
    })) : []
    
    // 计算章节完成状态：所有小节都完成才算章节完成
    const completedLessons = lessons.filter(lesson => lesson.completed).length
    const isChapterCompleted = lessons.length > 0 && completedLessons === lessons.length
    const isChapterCurrent = lessons.some(lesson => lesson.current)
    
    return {
      id: chapter.id,
      title: chapter.title,
      completed: isChapterCompleted,
      current: isChapterCurrent,
      lessons: lessons,
      orderIndex: chapter.orderIndex || 0
    }
  }).sort((a, b) => a.orderIndex - b.orderIndex) // 按顺序排序
})

const hasPrevious = computed(() => {
  // 检查是否有上一课
  return currentLesson.value > 1
})

const hasNext = computed(() => {
  // 检查是否有下一课
  const totalLessons = formattedChapters.value.reduce((total, chapter) => {
    return total + (chapter.lessons ? chapter.lessons.length : 0)
  }, 0)
  return currentLesson.value < totalLessons
})

// 当前小节的资料
const currentSectionMaterials = computed(() => {
  // 查找当前小节
  for (const chapter of formattedChapters.value) {
    if (chapter.lessons) {
      const currentSection = chapter.lessons.find(lesson => lesson.id === currentLesson.value)
      if (currentSection && currentSection.materials) {
        return currentSection.materials
      }
    }
  }
  return []
})

// 辅助方法：格式化时长（秒转为分:秒格式）
const formatDuration = (seconds) => {
  if (!seconds) return '0:00'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 辅助方法：处理视频URL，将相对路径转换为完整URL
const formatVideoUrl = (videoUrl) => {
  if (!videoUrl) return null
  // 如果是相对路径，拼接完整URL
  if (videoUrl.startsWith('/')) {
    return 'http://localhost:8080' + videoUrl
  }
  // 如果已经是完整URL，直接返回
  return videoUrl
}

// 获取带认证的视频URL（直接返回URL，不转换为blob）
const getAuthenticatedVideoUrl = async (videoUrl) => {
  if (!videoUrl) return null
  
  try {
    const token = takeAccessToken()
    if (!token) {
      ElMessage.warning('用户未登录，无法播放视频')
      return null
    }
    
    // 直接返回带认证参数的URL，不下载为blob
    // 这样可以保持HTTP Range请求的支持
    return videoUrl + (videoUrl.includes('?') ? '&' : '?') + 'token=' + encodeURIComponent(token)
  } catch (error) {
    console.error('获取视频URL失败:', error)
    ElMessage.error('视频URL生成失败，请稍后重试')
    return null
  }
}

// 辅助方法：根据小节ID查找所属章节
const findChapterByLessonId = (lessonId) => {
  for (const chapter of formattedChapters.value) {
    if (chapter.lessons && chapter.lessons.find(l => l.id === lessonId)) {
      return chapter.id
    }
  }
  return null
}

// 辅助方法：获取章节的第一个小节ID
const getFirstLessonOfChapter = (chapterId) => {
  const chapter = formattedChapters.value.find(c => c.id === chapterId)
  return chapter && chapter.lessons && chapter.lessons.length > 0 ? chapter.lessons[0].id : null
}

// 辅助方法：获取下一个小节信息
const getNextLessonInfo = (currentLessonId) => {
  let foundCurrent = false
  for (const chapter of formattedChapters.value) {
    if (chapter.lessons) {
      for (let i = 0; i < chapter.lessons.length; i++) {
        if (foundCurrent) {
          return {
            lessonId: chapter.lessons[i].id,
            chapterId: chapter.id,
            isNewChapter: false
          }
        }
        if (chapter.lessons[i].id === currentLessonId) {
          foundCurrent = true
          // 如果是当前章节的最后一课，检查下一章节
          if (i === chapter.lessons.length - 1) {
            const nextChapter = formattedChapters.value.find(c => c.id === chapter.id + 1)
            if (nextChapter && nextChapter.lessons && nextChapter.lessons.length > 0) {
              return {
                lessonId: nextChapter.lessons[0].id,
                chapterId: nextChapter.id,
                isNewChapter: true
              }
            }
          }
        }
      }
    }
  }
  return null
}

// 辅助方法：获取上一个小节信息
const getPreviousLessonInfo = (currentLessonId) => {
  let previousLesson = null
  for (const chapter of formattedChapters.value) {
    if (chapter.lessons) {
      for (let i = 0; i < chapter.lessons.length; i++) {
        if (chapter.lessons[i].id === currentLessonId) {
          if (previousLesson) {
            return {
              lessonId: previousLesson.id,
              chapterId: previousLesson.chapterId,
              isNewChapter: previousLesson.chapterId !== chapter.id
            }
          }
          return null
        }
        previousLesson = {
          id: chapter.lessons[i].id,
          chapterId: chapter.id
        }
      }
    }
  }
  return null
}

// 方法：选择章节（增强版）
const selectChapter = (chapterId) => {
  // 如果是当前章节，直接返回
  if (currentChapter.value === chapterId) {
    return
  }
  
  // 设置待确认的章节ID并显示确认对话框
  pendingChapterId.value = chapterId
  showChapterConfirmDialog.value = true
}

// 方法：确认章节切换
const confirmChapterSwitch = async () => {
  if (pendingChapterId.value) {
    currentChapter.value = pendingChapterId.value
    // 获取新章节的第一个小节
    const firstLessonId = getFirstLessonOfChapter(pendingChapterId.value)
    if (firstLessonId) {
      currentLesson.value = firstLessonId
      await updateCurrentLessonInfo(firstLessonId)
    }
  }
  showChapterConfirmDialog.value = false
  pendingChapterId.value = null
}

// 方法：取消章节切换
const cancelChapterSwitch = () => {
  showChapterConfirmDialog.value = false
  pendingChapterId.value = null
}

// 方法：选择小节（增强版）
const selectLesson = async (lessonId) => {
  // 切换小节前先保存当前进度
  if (currentLesson.value && currentProgress.value > 0) {
    await saveVideoProgress(true)
  }
  
  currentLesson.value = lessonId
  
  // 重置进度相关数据
  currentProgress.value = 0
  lastSavedProgress.value = 0
  
  // 自动切换到对应章节
  const lessonChapterId = findChapterByLessonId(lessonId)
  if (lessonChapterId && lessonChapterId !== currentChapter.value) {
    currentChapter.value = lessonChapterId
  }
  
  // 更新当前课程信息
  await updateCurrentLessonInfo(lessonId)
}

const updateCurrentLessonInfo = async (lessonId) => {
  // 根据lessonId更新当前课程信息
  for (const chapter of formattedChapters.value) {
    if (chapter.lessons) {
      const lesson = chapter.lessons.find(l => l.id === lessonId)
      if (lesson) {
        currentLessonInfo.value.title = lesson.title
        
        // 如果有视频URL，获取带认证的视频URL
        if (lesson.videoUrl) {
          const formattedUrl = formatVideoUrl(lesson.videoUrl)
          const authenticatedUrl = await getAuthenticatedVideoUrl(formattedUrl)
          currentVideoUrl.value = authenticatedUrl || ''
        } else {
          currentVideoUrl.value = ''
        }
        break
      }
    }
  }
}

const toggleNotes = () => {
  showNotes.value = !showNotes.value
}

const toggleBookmarks = () => {
  // 切换收藏状态
  console.log('切换收藏')
}

const shareLesson = () => {
  // 分享课程
  console.log('分享课程')
}

const toggleSidebar = () => {
  // 切换侧边栏显示
  console.log('切换侧边栏')
}

const previousLesson = async () => {
  // 切换前先保存当前进度
  if (currentLesson.value && currentProgress.value > 0) {
    await saveVideoProgress(true)
  }
  
  const prevInfo = getPreviousLessonInfo(currentLesson.value)
  if (prevInfo) {
    currentLesson.value = prevInfo.lessonId
    
    // 重置进度相关数据
    currentProgress.value = 0
    lastSavedProgress.value = 0
    
    // 如果切换到了新章节，自动更新章节导航
    if (prevInfo.isNewChapter) {
      currentChapter.value = prevInfo.chapterId
    }
    
    await updateCurrentLessonInfo(currentLesson.value)
  }
}

const nextLesson = async () => {
  // 切换前先保存当前进度
  if (currentLesson.value && currentProgress.value > 0) {
    await saveVideoProgress(true)
  }
  
  const nextInfo = getNextLessonInfo(currentLesson.value)
  if (nextInfo) {
    currentLesson.value = nextInfo.lessonId
    
    // 重置进度相关数据
    currentProgress.value = 0
    lastSavedProgress.value = 0
    
    // 如果切换到了新章节，自动更新章节导航
    if (nextInfo.isNewChapter) {
      currentChapter.value = nextInfo.chapterId
    }
    
    await updateCurrentLessonInfo(currentLesson.value)
  }
}

const togglePlay = () => {
  if (videoElement.value) {
    if (isPlaying.value) {
      videoElement.value.pause()
    } else {
      videoElement.value.play()
    }
  }
}

const toggleSpeed = () => {
  const speeds = [1.0, 1.25, 1.5, 2.0]
  const currentIndex = speeds.indexOf(playSpeed.value)
  playSpeed.value = speeds[(currentIndex + 1) % speeds.length]
}

const toggleFullscreen = () => {
  // 切换全屏
  console.log('切换全屏')
}

const getLessonIcon = (type) => {
  const icons = {
    video: 'fas fa-play-circle',
    document: 'fas fa-file-alt',
    quiz: 'fas fa-question-circle',
    assignment: 'fas fa-tasks'
  }
  return icons[type] || 'fas fa-file'
}

const getMaterialIcon = (mimeType) => {
  if (!mimeType) return 'fas fa-file'
  
  const icons = {
    // PDF文件
    'application/pdf': 'fas fa-file-pdf',
    // Word文档
    'application/msword': 'fas fa-file-word',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'fas fa-file-word',
    // Excel文件
    'application/vnd.ms-excel': 'fas fa-file-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'fas fa-file-excel',
    // PowerPoint文件
    'application/vnd.ms-powerpoint': 'fas fa-file-powerpoint',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'fas fa-file-powerpoint',
    // 压缩文件
    'application/zip': 'fas fa-file-archive',
    'application/x-rar-compressed': 'fas fa-file-archive',
    'application/x-7z-compressed': 'fas fa-file-archive',
    // 视频文件
    'video/mp4': 'fas fa-file-video',
    'video/avi': 'fas fa-file-video',
    'video/mov': 'fas fa-file-video',
    // 音频文件
    'audio/mp3': 'fas fa-file-audio',
    'audio/wav': 'fas fa-file-audio',
    // 图片文件
    'image/jpeg': 'fas fa-file-image',
    'image/png': 'fas fa-file-image',
    'image/gif': 'fas fa-file-image',
    // 文本文件
    'text/plain': 'fas fa-file-alt',
    'text/html': 'fas fa-file-code',
    'text/css': 'fas fa-file-code',
    'text/javascript': 'fas fa-file-code',
    // 其他常见格式
    'application/octet-stream': 'fas fa-file-archive' // CHM等二进制文件
  }
  
  return icons[mimeType] || 'fas fa-file'
}

// 格式化MIME类型为用户友好的文件类型
const formatMimeType = (mimeType) => {
  if (!mimeType) return '未知类型'
  
  const typeMap = {
    'application/pdf': 'PDF文档',
    'application/msword': 'Word文档',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'Word文档',
    'application/vnd.ms-excel': 'Excel表格',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'Excel表格',
    'application/vnd.ms-powerpoint': 'PowerPoint演示',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'PowerPoint演示',
    'application/zip': 'ZIP压缩包',
    'application/x-rar-compressed': 'RAR压缩包',
    'application/x-7z-compressed': '7Z压缩包',
    'video/mp4': 'MP4视频',
    'video/avi': 'AVI视频',
    'video/mov': 'MOV视频',
    'audio/mp3': 'MP3音频',
    'audio/wav': 'WAV音频',
    'image/jpeg': 'JPEG图片',
    'image/png': 'PNG图片',
    'image/gif': 'GIF图片',
    'text/plain': '文本文件',
    'text/html': 'HTML文件',
    'text/css': 'CSS文件',
    'text/javascript': 'JavaScript文件',
    'application/octet-stream': '二进制文件'
  }
  
  return typeMap[mimeType] || mimeType.split('/')[1]?.toUpperCase() || '未知类型'
}

// 下载资料
const downloadMaterial = async (material) => {
  if (!material || !material.fileId) {
    ElMessage.error('资料信息不完整，无法下载')
    return
  }
  
  // 防止重复下载
  if (downloadingMaterials.value.includes(material.id)) {
    return
  }
  
  try {
    downloadingMaterials.value.push(material.id)
    
    const token = takeAccessToken()
    if (!token) {
      ElMessage.warning('用户未登录，无法下载资料')
      return
    }
    
    // 构造下载URL
    const downloadUrl = `http://localhost:8080/api/student/learning/files/${material.fileId}`
    
    // 创建下载链接
    const response = await fetch(downloadUrl, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (!response.ok) {
      throw new Error(`下载失败: ${response.status} ${response.statusText}`)
    }
    
    // 获取文件blob
    const blob = await response.blob()
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = material.title || `material_${material.id}`
    
    // 触发下载
    document.body.appendChild(link)
    link.click()
    
    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success(`${material.title} 下载成功`)
    
  } catch (error) {
    console.error('下载资料失败:', error)
    ElMessage.error(`下载失败: ${error.message}`)
  } finally {
    // 移除下载状态
    const index = downloadingMaterials.value.indexOf(material.id)
    if (index > -1) {
      downloadingMaterials.value.splice(index, 1)
    }
  }
}

// 视频学习进度相关方法

// 保存学习进度到后端
const saveVideoProgress = async (force = false) => {
  if (isProgressSaving.value) {
    return // 避免重复保存
  }
  
  // 检查是否需要保存（进度差异超过阈值或强制保存）
  const progressDiff = Math.abs(currentProgress.value - lastSavedProgress.value)
  if (!force && progressDiff < PROGRESS_SAVE_THRESHOLD) {
    return
  }
  
  if (!currentLesson.value || currentProgress.value < 0) {
    return
  }
  
  try {
    isProgressSaving.value = true
    
    await updateVideoProgress(
      currentLesson.value.toString(),
      Math.floor(currentProgress.value)
    )
    
    lastSavedProgress.value = currentProgress.value
    console.log(`学习进度已保存: ${Math.floor(currentProgress.value)}秒`)
    
  } catch (error) {
    console.error('保存学习进度失败:', error)
    // 不显示错误消息，避免干扰用户学习体验
  } finally {
    isProgressSaving.value = false
  }
}

// 启动定期保存定时器
const startProgressSaveTimer = () => {
  stopProgressSaveTimer() // 先清除现有定时器
  progressSaveTimer.value = setInterval(() => {
    if (isPlaying.value) {
      saveVideoProgress()
    }
  }, PROGRESS_SAVE_INTERVAL)
}

// 停止定期保存定时器
const stopProgressSaveTimer = () => {
  if (progressSaveTimer.value) {
    clearInterval(progressSaveTimer.value)
    progressSaveTimer.value = null
  }
}

// 视频播放事件处理
const handleVideoPlay = () => {
  isPlaying.value = true
  startProgressSaveTimer()
}

// 视频暂停事件处理
const handleVideoPause = () => {
  isPlaying.value = false
  stopProgressSaveTimer()
  // 暂停时立即保存进度
  saveVideoProgress(true)
}

// 视频时间更新事件处理
const handleTimeUpdate = () => {
  if (videoElement.value) {
    currentProgress.value = videoElement.value.currentTime
  }
}

// 视频加载完成事件处理
const handleVideoLoaded = () => {
  // 视频加载完成后可以设置初始播放位置
  console.log('视频加载完成')
}

// 视频播放结束事件处理
const handleVideoEnded = async () => {
  isPlaying.value = false
  stopProgressSaveTimer()
  // 视频结束时保存最终进度
  await saveVideoProgress(true)

  // 可以在此处添加自动跳转到下一节课的逻辑
  // await nextLesson()
}

// 页面关闭前保存进度
const handleBeforeUnload = () => {
  // 使用 navigator.sendBeacon 进行异步保存，避免阻塞页面关闭
  if (currentLesson.value && currentProgress.value > 0) {
    const data = {
      sectionId: currentLesson.value.toString(),
      progressSeconds: Math.floor(currentProgress.value)
    }
    
    const token = takeAccessToken()
    if (token) {
      // 构造请求URL和数据
      const url = 'http://localhost:8080/api/student/learning/update'
      const formData = new FormData()
      formData.append('sectionId', data.sectionId)
      formData.append('progressSeconds', data.progressSeconds)
      
      // 使用 sendBeacon 发送数据
      navigator.sendBeacon(url, formData)
    }
  }
}

onMounted(async () => {
  try {
    // 获取课程内容数据
    const response = await getCourseContent(courseId)
    
    // 检查响应格式，适配后端返回的数据结构
    let data
    if (response && response.data) {
      // 如果是包装的响应格式 {code, data, message, success}
      data = response.data
    } else {
      // 如果直接返回数据
      data = response
    }
    
    // 验证数据结构并设置课程内容
    if (data && typeof data === 'object') {
      courseContent.value = {
        courseId: data.courseId || courseId,
        title: data.title || courseInfo.value.title,
        courseMaterials: data.courseMaterials || [],
        chapters: data.chapters || []
      }
      
      // 更新课程基本信息
      if (data.title) {
        courseInfo.value.title = data.title
      }
      
      // 如果有课程数据，初始化当前章节和小节
      if (data.chapters && data.chapters.length > 0) {
        const firstChapter = data.chapters[0]
        currentChapter.value = firstChapter.id
        
        if (firstChapter.sections && firstChapter.sections.length > 0) {
          const firstSection = firstChapter.sections[0]
          currentLesson.value = firstSection.id
          await updateCurrentLessonInfo(firstSection.id)
        }
      } else {
        // 如果没有章节数据，使用默认数据
        console.warn('课程暂无章节内容，使用默认数据')
        await updateCurrentLessonInfo(currentLesson.value)
      }
    } else {
      throw new Error('课程数据格式不正确')
    }
  } catch (error) {
    console.error('获取课程内容失败:', error)
    ElMessage.error('获取课程内容失败，请刷新页面重试')
    // 发生错误时使用默认数据
    await updateCurrentLessonInfo(currentLesson.value)
  }
  
  // 添加页面关闭前的事件监听器
  window.addEventListener('beforeunload', handleBeforeUnload)
  window.addEventListener('pagehide', handleBeforeUnload)
})

// 组件卸载时清理资源
onUnmounted(() => {
  // 清理定时器
  stopProgressSaveTimer()
  
  // 保存最后的进度
  if (currentLesson.value && currentProgress.value > 0) {
    saveVideoProgress(true)
  }
  
  // 移除事件监听器
  window.removeEventListener('beforeunload', handleBeforeUnload)
  window.removeEventListener('pagehide', handleBeforeUnload)
})
</script>

<style scoped>
/* CSS变量定义 - 符合设计约束手册规范 */
:root {
  /* 主色调 */
  --primary-color: #002FA7;
  --primary-light: rgba(0, 47, 167, 0.1);
  --primary-dark: #001f73;
  --secondary-color: #517B4D;
  --secondary-light: rgba(81, 123, 77, 0.1);
  --secondary-dark: #3a5a37;
  --success-color: #67C23A;
  --warning-color: #E6A23C;
  --danger-color: #F56C6C;
  --info-color: #909399;
  
  /* 中性色 */
  --bg-color: #F0F0F3;
  --bg-secondary: #E8E8EB;
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-auxiliary: #909399;
  --border-color: #DCDFE6;
  --hover-color: #F5F7FA;
  --primary-light: rgba(0, 47, 167, 0.1);
  
  /* 边框圆角 */
  --border-radius: 12px;
  --border-radius-sm: 8px;
  --border-radius-lg: 16px;
  
  /* 阴影效果 - 新拟态设计 */
  --shadow-raised: 8px 8px 16px #d1d1d4, -8px -8px 16px #ffffff;
  --shadow-inset: inset 8px 8px 16px #d1d1d4, inset -8px -8px 16px #ffffff;
  --shadow-hover: 4px 4px 8px #d1d1d4, -4px -4px 8px #ffffff;
  
  /* 磨砂玻璃效果 */
  --glass-bg: rgba(255, 255, 255, 0.25);
  --glass-border: rgba(255, 255, 255, 0.18);
  --glass-backdrop: blur(10px);
}

/* 新拟态设计基础类 */
.neumorphism-raised {
  background: var(--bg-color);
  box-shadow: var(--shadow-raised);
  border-radius: var(--border-radius);
  transition: all 0.3s ease;
}

.neumorphism-inset {
  background: var(--bg-color);
  box-shadow: var(--shadow-inset);
  border-radius: var(--border-radius);
}

.neumorphism-hover:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

/* 磨砂玻璃基础类 */
.frosted-glass {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--border-radius-lg);
}

/* 页面主体样式 */
.learn-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f0f3 0%, #e8e8eb 100%);
  padding: 24px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 课程学习头部 */
.learn-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  margin-bottom: 24px;
}

.course-info {
  flex: 1;
}

.course-meta {
  margin-bottom: 12px;
}

.course-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 12px 0 16px 0;
  line-height: 1.3;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 16px;
}

.progress-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  min-width: 120px;
}

.progress-bar {
  flex: 1;
  max-width: 320px;
}

/* 课程操作按钮 */
.course-actions {
  display: flex;
  gap: 16px;
}

.course-actions .neu-button {
  padding: 12px 20px;
  font-size: 14px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.course-actions .neu-button:not(.el-button--primary) {
  background: var(--bg-color);
  color: var(--text-primary);
  box-shadow: var(--shadow-raised);
  border-radius: var(--border-radius);
}

.course-actions .neu-button:not(.el-button--primary):hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.course-actions .neu-button:not(.el-button--primary):active {
  box-shadow: var(--shadow-inset);
  transform: translateY(0);
}

.course-actions .el-button--primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border: none;
  box-shadow: var(--shadow-raised);
  border-radius: var(--border-radius);
}

.course-actions .el-button--primary:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

/* 学习内容区域 */
.learn-content {
  display: flex;
  gap: 24px;
  min-height: calc(100vh - 240px);
}

/* 章节侧边栏 */
.chapter-sidebar {
  width: 320px;
  padding: 24px;
  overflow-y: auto;
  max-height: calc(100vh - 240px);
}

/* 侧边栏头部 */
.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--border-color);
}

.sidebar-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
}

/* 章节项目 */
.chapter-item {
  margin-bottom: 16px;
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: var(--bg-color);
  box-shadow: var(--shadow-raised);
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.3s ease;
}

.chapter-header:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-1px);
}

.chapter-item.active .chapter-header {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  box-shadow: var(--shadow-hover);
}

/* 章节信息 */
.chapter-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chapter-number {
  width: 28px;
  height: 28px;
  background: var(--bg-color);
  color: var(--primary-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  box-shadow: var(--shadow-inset);
}

.chapter-item.active .chapter-number {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  box-shadow: inset 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.chapter-title {
  font-weight: 600;
  font-size: 15px;
}

/* 章节状态图标 */
.chapter-status .completed {
  color: var(--success-color);
  font-size: 16px;
}

.chapter-status .current {
  color: var(--primary-color);
  font-size: 16px;
}

.chapter-status .pending {
  color: var(--text-auxiliary);
  font-size: 16px;
}

.chapter-item.active .chapter-status i {
  color: rgba(255, 255, 255, 0.8);
}

/* 课程列表 */
.lesson-list {
  margin-top: 12px;
  padding-left: 24px;
}

.lesson-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 8px;
  background: var(--bg-secondary);
  box-shadow: inset 2px 2px 4px rgba(209, 209, 212, 0.3), inset -2px -2px 4px rgba(255, 255, 255, 0.7);
  border-radius: var(--border-radius-sm);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  position: relative;
}

.lesson-item:hover {
  box-shadow: 2px 2px 6px rgba(209, 209, 212, 0.4), -2px -2px 6px rgba(255, 255, 255, 0.8);
  transform: translateY(-1px);
  border-color: rgba(0, 47, 167, 0.2);
}

.lesson-item.active {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  box-shadow: 0 8px 24px rgba(0, 47, 167, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.2);
  border-color: var(--primary-color);
  transform: translateY(-2px);
}

.lesson-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, #ffffff 0%, rgba(255, 255, 255, 0.6) 100%);
  border-radius: 0 2px 2px 0;
}

/* 课程信息 */
.lesson-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.lesson-info i {
  color: var(--primary-color);
  width: 16px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.lesson-item.active .lesson-info i {
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.lesson-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.lesson-item.active .lesson-title {
  color: white;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.lesson-duration {
  font-size: 12px;
  color: var(--text-auxiliary);
  margin-left: auto;
  background: rgba(0, 47, 167, 0.1);
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.lesson-item.active .lesson-duration {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-weight: 600;
  backdrop-filter: blur(4px);
}

/* 主内容区域 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 视频容器 */
.video-container {
  background: var(--bg-color);
  box-shadow: 8px 8px 20px #d1d1d4, -8px -8px 20px #ffffff;
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.video-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.5), transparent);
  z-index: 1;
}

.video-container:hover {
  box-shadow: 4px 4px 12px #d1d1d4, -4px -4px 12px #ffffff;
  transform: translateY(-4px);
}

.video-player {
  position: relative;
  width: 100%;
  height: 450px;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--border-radius-lg) var(--border-radius-lg) 0 0;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
}

.video-element {
  width: 100%;
  height: 100%;
  border-radius: var(--border-radius-lg) var(--border-radius-lg) 0 0;
  object-fit: contain;
  background: #000;
}

.video-placeholder {
  text-align: center;
  color: white;
  position: relative;
}

.video-placeholder::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  z-index: 0;
}

.video-placeholder i {
  font-size: 72px;
  margin-bottom: 20px;
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
  position: relative;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.video-placeholder:hover i {
  color: var(--primary-color);
  transform: scale(1.15);
  text-shadow: 0 4px 16px rgba(0, 0, 0, 0.4);
}

.video-placeholder p {
  margin: 8px 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  z-index: 1;
  position: relative;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

.video-title {
  font-size: 20px;
  font-weight: 600;
  margin-top: 16px;
  color: white;
  z-index: 1;
  position: relative;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
  text-align: center;
  max-width: 80%;
}

.video-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(145deg, #f0f0f3 0%, #e8e8eb 100%);
  border-top: 1px solid rgba(220, 223, 230, 0.5);
  border-radius: 0 0 var(--border-radius) var(--border-radius);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.control-left,
.control-right {
  display: flex;
  gap: 16px;
  align-items: center;
}

.video-controls .el-button {
  padding: 12px 20px;
  font-size: 14px;
  font-weight: 500;
  border: none;
  background: var(--bg-color);
  color: var(--text-primary);
  box-shadow: 6px 6px 12px #d1d1d4, -6px -6px 12px #ffffff;
  border-radius: var(--border-radius);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.video-controls .el-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.video-controls .el-button:hover {
  box-shadow: 3px 3px 8px #d1d1d4, -3px -3px 8px #ffffff;
  transform: translateY(-2px);
}

.video-controls .el-button:hover::before {
  left: 100%;
}

.video-controls .el-button:active {
  box-shadow: inset 3px 3px 6px #d1d1d4, inset -3px -3px 6px #ffffff;
  transform: translateY(0);
}

.video-controls .el-button.el-button--primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  box-shadow: 6px 6px 12px rgba(0, 47, 167, 0.2), -6px -6px 12px rgba(255, 255, 255, 0.8);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.video-controls .el-button.el-button--primary:hover {
  box-shadow: 3px 3px 8px rgba(0, 47, 167, 0.3), -3px -3px 8px rgba(255, 255, 255, 0.9);
  transform: translateY(-3px);
}

.video-controls .el-button.el-button--primary:active {
  box-shadow: inset 2px 2px 4px rgba(0, 0, 0, 0.2), inset -2px -2px 4px rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

.video-controls .el-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: inset 2px 2px 4px #d1d1d4, inset -2px -2px 4px #ffffff;
  transform: none;
}

.video-controls .el-button:disabled:hover {
  transform: none;
  box-shadow: inset 2px 2px 4px #d1d1d4, inset -2px -2px 4px #ffffff;
}

/* 内容标签页 */
.content-tabs {
  flex: 1;
  background: var(--bg-color);
  box-shadow: var(--shadow-raised);
  border-radius: var(--border-radius-lg);
  padding: 24px;
  transition: all 0.3s ease;
}

.content-tabs:hover {
  box-shadow: var(--shadow-hover);
}

.el-tabs__header {
  margin: 0 0 24px 0;
}

.el-tabs__nav-wrap::after {
  display: none;
}

.el-tabs__item {
  padding: 0 24px;
  height: 44px;
  line-height: 44px;
  font-weight: 600;
  font-size: 15px;
  border-radius: var(--border-radius-sm) var(--border-radius-sm) 0 0;
  margin-right: 8px;
  transition: all 0.3s ease;
}

.el-tabs__item:hover {
  background: rgba(0, 47, 167, 0.05);
}

.el-tabs__item.is-active {
  color: var(--primary-color);
  background: rgba(0, 47, 167, 0.1);
  box-shadow: inset 0 -2px 0 var(--primary-color);
}

.tab-content {
  min-height: 320px;
  line-height: 1.6;
  color: var(--text-primary);
}

.lesson-intro h3 {
  color: var(--text-primary);
  margin-bottom: 15px;
}

.lesson-description {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 20px;
}

.lesson-objectives h4 {
  color: var(--text-primary);
  margin-bottom: 10px;
}

.lesson-objectives ul {
  padding-left: 20px;
}

.lesson-objectives li {
  color: var(--text-secondary);
  margin-bottom: 5px;
}

.materials-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.material-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: var(--bg-color);
  border-radius: var(--border-radius);
  border: 1px solid var(--border-color);
}

.material-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.material-info i {
  color: var(--primary-color);
  width: 20px;
}

.material-name {
  font-weight: 500;
  color: var(--text-primary);
}

.material-size {
  color: var(--text-secondary);
  font-size: 12px;
}

.discussion-area {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.discussion-input {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.submit-btn {
  align-self: flex-end;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 15px;
  background: var(--bg-color);
  border-radius: var(--border-radius);
}

.comment-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.username {
  font-weight: 500;
  color: var(--text-primary);
}

.time {
  font-size: 12px;
  color: var(--text-secondary);
}

.comment-text {
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 10px;
}

.comment-actions {
  display: flex;
  gap: 15px;
}

/* 笔记侧边栏 */
.notes-sidebar {
  width: 320px;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-backdrop);
  border: 1px solid var(--glass-border);
  border-radius: var(--border-radius-lg);
  padding: 24px;
  position: fixed;
  right: 20px;
  top: 20px;
  height: calc(100vh - 40px);
  z-index: 1000;
  box-shadow: var(--shadow-raised);
}

.notes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(0, 47, 167, 0.1);
}

.notes-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.notes-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
  height: calc(100% - 60px);
}

.notes-content .el-textarea {
  flex: 1;
}

.save-note-btn {
  align-self: flex-end;
}

/* 添加笔记按钮样式 */
.add-note-btn {
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 600;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--border-radius-sm);
  box-shadow: var(--shadow-raised);
  transition: all 0.3s ease;
}

.add-note-btn:hover {
  background: var(--primary-dark);
  box-shadow: var(--shadow-hover);
  transform: translateY(-1px);
}

/* 笔记列表 */
.note-list {
  max-height: calc(100vh - 320px);
  overflow-y: auto;
  padding: 12px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: var(--border-radius);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.note-list::-webkit-scrollbar {
  width: 8px;
}

.note-list::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
  box-shadow: inset 1px 1px 2px rgba(0, 0, 0, 0.1);
}

.note-list::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.note-list::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, var(--primary-dark) 0%, var(--secondary-dark) 100%);
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.3);
}

/* 笔记项 */
.note-item {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: var(--border-radius-sm);
  padding: 18px;
  margin-bottom: 16px;
  border: 1px solid rgba(220, 223, 230, 0.4);
  box-shadow: 4px 4px 12px rgba(0, 0, 0, 0.08), -2px -2px 8px rgba(255, 255, 255, 0.8);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.note-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.note-item:hover {
  box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.12), -1px -1px 6px rgba(255, 255, 255, 0.9);
  transform: translateY(-3px);
  background: rgba(255, 255, 255, 0.98);
  border-color: rgba(0, 47, 167, 0.2);
}

.note-item:hover::before {
  opacity: 1;
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(220, 223, 230, 0.3);
}

.note-time {
  font-size: 12px;
  color: var(--text-secondary);
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.1) 0%, rgba(81, 123, 77, 0.1) 100%);
  padding: 6px 12px;
  border-radius: var(--border-radius-sm);
  font-weight: 500;
  border: 1px solid rgba(0, 47, 167, 0.1);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  box-shadow: 1px 1px 3px rgba(0, 0, 0, 0.05);
}

.note-content {
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-primary);
  margin-bottom: 16px;
  word-break: break-word;
  position: relative;
  padding: 8px 0;
}

.note-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  justify-content: flex-end;
  padding-top: 8px;
  border-top: 1px solid rgba(220, 223, 230, 0.2);
}

.note-actions .el-button {
  padding: 8px 16px;
  font-size: 12px;
  font-weight: 500;
  border-radius: var(--border-radius-sm);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  position: relative;
  overflow: hidden;
}

.note-actions .el-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.4s;
}

.note-actions .el-button:hover::before {
  left: 100%;
}

.note-actions .el-button--text {
  color: var(--text-secondary);
  background: rgba(240, 240, 243, 0.8);
  box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.05), -2px -2px 4px rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
}

.note-actions .el-button--text:hover {
  color: var(--primary-color);
  background: rgba(0, 47, 167, 0.1);
  transform: translateY(-2px);
  box-shadow: 1px 1px 3px rgba(0, 0, 0, 0.1), -1px -1px 3px rgba(255, 255, 255, 0.9);
}

/* 笔记弹窗 */
.notes-modal .el-dialog {
  background: rgba(240, 240, 243, 0.95);
  border-radius: var(--border-radius-xl);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1), 0 8px 16px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
  position: relative;
}

.notes-modal .el-dialog::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.6), transparent);
  z-index: 1;
}

.notes-modal .el-dialog__header {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.9) 0%, rgba(81, 123, 77, 0.9) 100%);
  color: white;
  padding: 24px 28px;
  border-radius: var(--border-radius-xl) var(--border-radius-xl) 0 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  position: relative;
}

.notes-modal .el-dialog__header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
}

.notes-modal .el-dialog__title {
  font-size: 20px;
  font-weight: 600;
  color: white;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  gap: 12px;
}

.notes-modal .el-dialog__title::before {
  content: '\f15c';
  font-family: 'Font Awesome 5 Free';
  font-weight: 900;
  font-size: 18px;
  opacity: 0.8;
}

.notes-modal .el-dialog__body {
  padding: 28px;
  background: rgba(240, 240, 243, 0.6);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.notes-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.notes-form .el-input,
.notes-form .el-textarea {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(220, 223, 230, 0.6);
  border-radius: var(--border-radius);
  box-shadow: inset 2px 2px 4px rgba(0, 0, 0, 0.05), inset -2px -2px 4px rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.notes-form .el-input:focus-within,
.notes-form .el-textarea:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(0, 47, 167, 0.1), inset 1px 1px 2px rgba(0, 0, 0, 0.05);
  background: rgba(255, 255, 255, 0.95);
  transform: translateY(-1px);
}

.notes-form .el-input__inner,
.notes-form .el-textarea__inner {
  background: transparent;
  border: none;
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.5;
}

.notes-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid rgba(220, 223, 230, 0.3);
}

.notes-actions .el-button {
  padding: 12px 24px;
  border-radius: var(--border-radius);
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  position: relative;
  overflow: hidden;
}

.notes-actions .el-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.notes-actions .el-button:hover::before {
  left: 100%;
}

.notes-actions .el-button--default {
  background: rgba(255, 255, 255, 0.8);
  color: var(--text-primary);
  box-shadow: 3px 3px 6px rgba(0, 0, 0, 0.1), -3px -3px 6px rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
}

.notes-actions .el-button--default:hover {
  box-shadow: 1px 1px 3px rgba(0, 0, 0, 0.15), -1px -1px 3px rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.95);
}

.notes-actions .el-button--primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  box-shadow: 4px 4px 8px rgba(0, 47, 167, 0.3), -4px -4px 8px rgba(255, 255, 255, 0.8);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.notes-actions .el-button--primary:hover {
  box-shadow: 2px 2px 6px rgba(0, 47, 167, 0.4), -2px -2px 6px rgba(255, 255, 255, 0.9);
  transform: translateY(-3px);
}

.notes-actions .el-button:active {
  transform: translateY(0);
  box-shadow: inset 2px 2px 4px rgba(0, 0, 0, 0.1), inset -2px -2px 4px rgba(255, 255, 255, 0.8);
}

/* 章节切换确认对话框样式 */
.chapter-confirm-dialog .el-dialog {
  background: rgba(240, 240, 243, 0.9);
  backdrop-filter: blur(25px);
  -webkit-backdrop-filter: blur(25px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: var(--border-radius-lg);
  box-shadow: 
    0 12px 40px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.chapter-confirm-dialog .el-dialog::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.15) 0%, 
    rgba(255, 255, 255, 0.08) 100%);
  border-radius: var(--border-radius-lg);
  pointer-events: none;
}

.chapter-confirm-dialog .el-dialog__header {
  background: rgba(255, 255, 255, 0.15);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: var(--border-radius-lg) var(--border-radius-lg) 0 0;
  padding: 20px 24px;
  position: relative;
}

.chapter-confirm-dialog .el-dialog__title {
  color: var(--text-primary);
  font-weight: 600;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chapter-confirm-dialog .el-dialog__body {
  padding: 24px;
  background: transparent;
}

.confirm-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 20px;
}

.confirm-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, var(--warning-color) 0%, #f39c12 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-raised);
  position: relative;
}

.confirm-icon::before {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  right: 2px;
  bottom: 2px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  pointer-events: none;
}

.confirm-icon i {
  font-size: 28px;
  color: white;
  position: relative;
  z-index: 1;
}

.confirm-text h3 {
  margin: 0 0 12px 0;
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 600;
}

.confirm-text p {
  margin: 0 0 20px 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.5;
}

.lesson-options {
  width: 100%;
  max-height: 240px;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius);
  padding: 8px;
  backdrop-filter: blur(10px);
}

.lesson-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 4px;
  background: rgba(255, 255, 255, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius-sm);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.lesson-option::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--border-radius-sm);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.lesson-option:hover::before {
  opacity: 1;
}

.lesson-option:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.lesson-option.selected {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(0, 47, 167, 0.3);
}

.lesson-option-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.lesson-option-info i {
  font-size: 14px;
  width: 16px;
  text-align: center;
  transition: color 0.3s ease;
}

.lesson-option-title {
  font-weight: 500;
  font-size: 14px;
  transition: color 0.3s ease;
}

.lesson-option-duration {
  font-size: 12px;
  opacity: 0.8;
  transition: color 0.3s ease;
}

.lesson-option-status i {
  font-size: 14px;
  transition: color 0.3s ease;
}

.lesson-option-status .completed {
  color: var(--success-color);
}

.lesson-option-status .current {
  color: var(--primary-color);
}

.lesson-option.selected .lesson-option-status i {
  color: rgba(255, 255, 255, 0.9);
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px 0 0 0;
}

.dialog-footer .neu-button {
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 100px;
}

.dialog-footer .neu-button:not(.el-button--primary) {
  background: var(--bg-color);
  color: var(--text-primary);
  box-shadow: var(--shadow-raised);
  border-radius: var(--border-radius);
}

.dialog-footer .neu-button:not(.el-button--primary):hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  box-shadow: var(--shadow-raised);
  border-radius: var(--border-radius);
}

.dialog-footer .el-button--primary:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .notes-sidebar {
    width: 280px;
  }
}

@media (max-width: 1200px) {
  .notes-sidebar {
    display: none;
  }
  
  .learn-content {
    gap: 20px;
  }
  
  .chapter-sidebar {
    width: 280px;
  }
}

@media (max-width: 992px) {
  .learn-content {
    flex-direction: column;
    gap: 20px;
  }
  
  .chapter-sidebar {
    width: 100%;
    max-height: 400px;
    padding: 20px;
  }
  
  .main-content {
    gap: 20px;
  }
  
  .video-player {
    height: 350px;
  }
}

@media (max-width: 768px) {
  .learn-page {
    padding: 12px;
  }
  
  .learn-header {
    padding: 16px 20px;
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .course-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .course-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .chapter-sidebar {
    max-height: 350px;
    padding: 16px;
  }
  
  .video-player {
    height: 280px;
  }
  
  .video-controls {
    flex-direction: column;
    gap: 10px;
  }
  
  .control-left,
  .control-right {
    justify-content: center;
  }
  
  .content-tabs {
    padding: 20px;
  }
  
  .el-tabs__item {
    padding: 0 16px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .learn-page {
    padding: 12px;
  }
  
  .learn-header {
    padding: 12px 16px;
  }
  
  .course-title {
    font-size: 18px;
  }
  
  .course-actions {
    flex-direction: column;
    gap: 12px;
  }
  
  .course-actions .el-button {
    width: 100%;
    justify-content: center;
  }
  
  .chapter-sidebar {
    padding: 12px;
    max-height: 300px;
  }
  
  .sidebar-header h3 {
    font-size: 16px;
  }
  
  .chapter-item {
    margin-bottom: 12px;
  }
  
  .chapter-header {
    padding: 12px;
  }
  
  .lesson-item {
    padding: 10px 12px;
  }
  
  .video-player {
    height: 220px;
  }
  
  .video-placeholder i {
    font-size: 48px;
  }
  
  .video-title {
    font-size: 16px;
  }
  
  .content-tabs {
    padding: 16px;
  }
  
  .el-tabs__item {
    padding: 0 12px;
    height: 40px;
    line-height: 40px;
    font-size: 13px;
  }
  
  .tab-content {
    min-height: 250px;
    font-size: 14px;
  }
}

/* 资料相关样式 */
.materials-container {
  padding: 0;
}

.materials-section {
  margin-bottom: 32px;
}

.materials-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 0 4px;
}

.section-header h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.section-header h4 i {
  color: var(--primary-color);
  font-size: 14px;
}

.material-count {
  font-size: 12px;
  color: var(--text-auxiliary);
  background: var(--bg-secondary);
  padding: 4px 8px;
  border-radius: 12px;
}

.materials-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.material-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: var(--bg-color);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.material-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.02) 0%, rgba(81, 123, 77, 0.02) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.material-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
  border-color: var(--primary-color);
}

.material-item:hover::before {
  opacity: 1;
}

.material-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.material-info i {
  font-size: 24px;
  color: var(--primary-color);
  width: 32px;
  text-align: center;
  flex-shrink: 0;
}

.material-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
  flex: 1;
}

.material-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.4;
  word-break: break-all;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.material-type {
  font-size: 12px;
  color: var(--text-auxiliary);
  background: var(--bg-secondary);
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
  width: fit-content;
}

.download-btn {
  flex-shrink: 0;
  height: 36px;
  padding: 0 16px;
  font-size: 13px;
  border-radius: var(--border-radius-sm);
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border: none;
  color: white;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.download-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.download-btn:hover::before {
  left: 100%;
}

.download-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 47, 167, 0.3);
}

.download-btn:active {
  transform: translateY(0);
}

.download-btn i {
  margin-right: 6px;
  font-size: 12px;
}

/* 无资料状态 */
.no-materials {
  padding: 60px 20px;
  text-align: center;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-icon {
  font-size: 48px;
  color: var(--text-auxiliary);
  opacity: 0.6;
}

.empty-state p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 16px;
  font-weight: 500;
}

.empty-desc {
  font-size: 14px !important;
  color: var(--text-auxiliary) !important;
  font-weight: 400 !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .material-item {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
    padding: 16px;
  }
  
  .material-info {
    gap: 12px;
  }
  
  .material-info i {
    font-size: 20px;
    width: 24px;
  }
  
  .download-btn {
    width: 100%;
    justify-content: center;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .material-count {
    align-self: flex-end;
  }
}

.learn-page {
  --primary-color: #002fa7;
  --primary-strong: #001f73;
  --secondary-color: #517b4d;
  --secondary-strong: #3a5a37;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --text-primary: #1a2233;
  --text-secondary: #4a5a73;
  --text-auxiliary: #7d8ba3;
  --border-soft: rgba(255, 255, 255, 0.62);
  --border-strong: rgba(0, 47, 167, 0.18);
  --glass-surface: rgba(255, 255, 255, 0.72);
  --glass-strong: rgba(255, 255, 255, 0.84);
  --surface-main: #f3f6ff;
  --surface-subtle: #edf2ff;
  --radius-xs: 10px;
  --radius-sm: 14px;
  --radius-md: 18px;
  --radius-lg: 24px;
  --shadow-soft: 0 10px 28px rgba(27, 46, 94, 0.11);
  --shadow-pop: 0 18px 44px rgba(17, 35, 82, 0.16);
  --shadow-glow: 0 0 0 1px rgba(255, 255, 255, 0.56), 0 12px 30px rgba(0, 47, 167, 0.12);
  position: relative;
  overflow: hidden;
  min-height: 100vh;
  padding: 28px;
  background:
    radial-gradient(circle at 18% 16%, rgba(0, 47, 167, 0.12) 0%, rgba(0, 47, 167, 0) 36%),
    radial-gradient(circle at 84% 12%, rgba(81, 123, 77, 0.14) 0%, rgba(81, 123, 77, 0) 30%),
    linear-gradient(145deg, #f8fbff 0%, #f0f4ff 48%, #eef3fb 100%);
}

.learn-page::before {
  content: '';
  position: fixed;
  inset: 0;
  background-image:
    linear-gradient(rgba(0, 47, 167, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 47, 167, 0.05) 1px, transparent 1px);
  background-size: 38px 38px;
  mask-image: radial-gradient(circle at center, rgba(0, 0, 0, 0.36), transparent 82%);
  pointer-events: none;
  z-index: 0;
}

.learn-header,
.learn-content,
.notes-sidebar {
  position: relative;
  z-index: 1;
}

.glass-card,
.neu-card {
  background: var(--glass-surface);
  border: 1px solid var(--border-soft);
  box-shadow: var(--shadow-glow);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-lg);
}

.learn-header {
  gap: 24px;
  align-items: flex-start;
  padding: 28px 30px;
  margin-bottom: 26px;
}

.course-meta :deep(.el-breadcrumb__inner),
.course-meta :deep(.el-breadcrumb__inner a) {
  color: var(--text-secondary);
  font-weight: 500;
}

.course-meta :deep(.el-breadcrumb__separator) {
  color: rgba(0, 47, 167, 0.55);
}

.course-title {
  margin: 10px 0 14px;
  font-size: 32px;
  letter-spacing: 0.01em;
  background: linear-gradient(120deg, var(--primary-color) 0%, #2044bc 34%, var(--secondary-color) 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
}

.progress-info {
  align-items: center;
  gap: 16px;
}

.progress-text {
  min-width: 132px;
  color: var(--text-secondary);
  font-weight: 600;
}

.progress-bar :deep(.el-progress-bar__outer) {
  height: 10px !important;
  border-radius: 999px;
  background: rgba(0, 47, 167, 0.11);
}

.progress-bar :deep(.el-progress-bar__inner) {
  border-radius: 999px;
  background: linear-gradient(90deg, var(--primary-color) 0%, #3767e0 45%, var(--secondary-color) 100%);
  box-shadow: 0 6px 14px rgba(0, 47, 167, 0.28);
}

.course-actions {
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.course-actions .neu-button {
  min-height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  font-weight: 600;
  border: 1px solid rgba(0, 47, 167, 0.14);
  background: var(--glass-strong);
  color: var(--text-primary);
  box-shadow: 0 8px 20px rgba(24, 48, 110, 0.11);
}

.course-actions .neu-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 24px rgba(0, 47, 167, 0.17);
}

.course-actions .el-button--primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, #2852d0 44%, var(--secondary-color) 100%);
  color: #fff;
  border: none;
}

.learn-content {
  gap: 24px;
  min-height: calc(100vh - 220px);
}

.chapter-sidebar {
  width: 332px;
  padding: 22px 20px;
  border-radius: var(--radius-lg);
  max-height: calc(100vh - 220px);
}

.sidebar-header {
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 47, 167, 0.14);
}

.sidebar-header h3 {
  font-size: 19px;
  color: var(--text-primary);
}

.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.chapter-item {
  margin-bottom: 0;
}

.chapter-header {
  background: linear-gradient(140deg, rgba(255, 255, 255, 0.74) 0%, rgba(240, 246, 255, 0.88) 100%);
  border: 1px solid rgba(0, 47, 167, 0.12);
  border-radius: var(--radius-sm);
  box-shadow: 0 8px 18px rgba(12, 45, 110, 0.09);
}

.chapter-header:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 26px rgba(0, 47, 167, 0.15);
}

.chapter-item.active .chapter-header {
  background: linear-gradient(135deg, var(--primary-color) 0%, #2d5de0 50%, var(--secondary-color) 100%);
  border-color: transparent;
  box-shadow: 0 16px 28px rgba(0, 47, 167, 0.28);
}

.chapter-number {
  width: 30px;
  height: 30px;
  font-size: 12px;
  border: 1px solid rgba(0, 47, 167, 0.12);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 3px 8px rgba(0, 47, 167, 0.12);
}

.lesson-list {
  margin-top: 10px;
  padding-left: 16px;
}

.lesson-item {
  border: 1px solid transparent;
  border-radius: var(--radius-xs);
  background: rgba(255, 255, 255, 0.7);
  box-shadow: 0 8px 18px rgba(22, 49, 112, 0.08);
}

.lesson-item:hover {
  transform: translateY(-1px);
  border-color: rgba(0, 47, 167, 0.24);
  box-shadow: 0 12px 22px rgba(0, 47, 167, 0.14);
}

.lesson-item.active {
  border-color: transparent;
  box-shadow: 0 14px 28px rgba(0, 47, 167, 0.26);
}

.lesson-title {
  color: var(--text-primary);
}

.lesson-duration {
  background: rgba(0, 47, 167, 0.08);
  color: #22458f;
  border: 1px solid rgba(0, 47, 167, 0.15);
}

.main-content {
  gap: 22px;
}

.video-container {
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-soft);
  background: rgba(255, 255, 255, 0.7);
  box-shadow: var(--shadow-pop);
}

.video-player {
  height: 500px;
  background:
    radial-gradient(circle at 18% 24%, rgba(38, 76, 184, 0.2) 0%, rgba(38, 76, 184, 0) 40%),
    radial-gradient(circle at 86% 10%, rgba(81, 123, 77, 0.2) 0%, rgba(81, 123, 77, 0) 35%),
    linear-gradient(155deg, #0b1224 0%, #111c36 48%, #132743 100%);
}

.video-controls {
  border-top: 1px solid rgba(0, 47, 167, 0.12);
  background: rgba(246, 250, 255, 0.82);
}

.video-controls .el-button {
  border-radius: 999px;
  border: 1px solid rgba(0, 47, 167, 0.14);
  background: rgba(255, 255, 255, 0.85);
  box-shadow: 0 10px 20px rgba(0, 47, 167, 0.12);
  color: #1f335c;
}

.video-controls .el-button.el-button--primary {
  border: none;
  color: #fff;
  background: linear-gradient(135deg, var(--primary-color) 0%, #2e5ce0 45%, var(--secondary-color) 100%);
  box-shadow: 0 12px 24px rgba(0, 47, 167, 0.32);
}

.content-tabs {
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-soft);
  background: rgba(255, 255, 255, 0.76);
  box-shadow: var(--shadow-soft);
}

.content-tabs :deep(.el-tabs__header) {
  margin: 0 0 18px;
}

.content-tabs :deep(.el-tabs__item) {
  height: 42px;
  line-height: 42px;
  border-radius: 999px;
  margin-right: 8px;
  color: var(--text-secondary);
  border: 1px solid transparent;
}

.content-tabs :deep(.el-tabs__item:hover) {
  background: rgba(0, 47, 167, 0.08);
  color: var(--primary-color);
}

.content-tabs :deep(.el-tabs__item.is-active) {
  border-color: rgba(0, 47, 167, 0.18);
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.12) 0%, rgba(81, 123, 77, 0.12) 100%);
  color: var(--primary-color);
  box-shadow: none;
}

.content-tabs :deep(.el-tabs__active-bar) {
  display: none;
}

.discussion-input :deep(.el-textarea__inner),
.notes-content :deep(.el-textarea__inner) {
  border-radius: var(--radius-sm);
  border: 1px solid rgba(0, 47, 167, 0.15);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: inset 0 2px 8px rgba(0, 47, 167, 0.08);
  color: var(--text-primary);
}

.discussion-input :deep(.el-textarea__inner:focus),
.notes-content :deep(.el-textarea__inner:focus) {
  border-color: rgba(0, 47, 167, 0.36);
  box-shadow: 0 0 0 3px rgba(0, 47, 167, 0.12);
}

.submit-btn,
.save-note-btn,
.download-btn {
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--primary-color) 0%, #2d5de0 45%, var(--secondary-color) 100%);
  box-shadow: 0 12px 22px rgba(0, 47, 167, 0.28);
}

.submit-btn:hover,
.save-note-btn:hover,
.download-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 26px rgba(0, 47, 167, 0.34);
}

.material-item,
.comment-item,
.note-item {
  border: 1px solid rgba(0, 47, 167, 0.12);
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 10px 22px rgba(0, 47, 167, 0.08);
}

.material-item:hover,
.comment-item:hover,
.note-item:hover {
  border-color: rgba(0, 47, 167, 0.24);
  box-shadow: 0 16px 28px rgba(0, 47, 167, 0.14);
}

.material-count,
.material-type,
.note-time {
  border-radius: 999px;
  border: 1px solid rgba(0, 47, 167, 0.16);
  background: rgba(0, 47, 167, 0.08);
  color: #2b4e99;
}

.notes-sidebar {
  top: 24px;
  right: 24px;
  height: calc(100vh - 48px);
  width: 336px;
  padding: 22px;
}

:deep(.chapter-confirm-dialog .el-dialog) {
  border-radius: var(--radius-md);
  border: 1px solid rgba(255, 255, 255, 0.44);
  background: rgba(247, 251, 255, 0.88);
  backdrop-filter: blur(24px);
  box-shadow: 0 20px 44px rgba(13, 32, 78, 0.24);
}

:deep(.chapter-confirm-dialog .el-dialog__header) {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.12) 0%, rgba(81, 123, 77, 0.12) 100%);
  border-bottom: 1px solid rgba(0, 47, 167, 0.15);
}

.lesson-option {
  border: 1px solid rgba(0, 47, 167, 0.16);
  background: rgba(255, 255, 255, 0.72);
}

.lesson-option.selected {
  box-shadow: 0 10px 18px rgba(0, 47, 167, 0.24);
}

@media (max-width: 1200px) {
  .learn-page {
    padding: 20px;
  }

  .chapter-sidebar {
    width: 300px;
  }
}

@media (max-width: 992px) {
  .learn-header {
    align-items: stretch;
  }

  .course-actions {
    justify-content: flex-start;
  }

  .video-player {
    height: 390px;
  }
}

@media (max-width: 768px) {
  .learn-page {
    padding: 14px;
  }

  .learn-header {
    padding: 20px 18px;
    gap: 14px;
  }

  .course-title {
    font-size: 24px;
  }

  .course-actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr;
  }

  .course-actions .neu-button {
    width: 100%;
    justify-content: center;
  }

  .chapter-sidebar {
    width: 100%;
    max-height: 360px;
  }

  .video-player {
    height: 270px;
  }

  .video-controls {
    padding: 14px;
  }

  .control-left,
  .control-right {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
  }

  .content-tabs {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .course-title {
    font-size: 20px;
  }

  .progress-info {
    flex-direction: column;
    align-items: flex-start;
  }

  .video-player {
    height: 224px;
  }

  .notes-sidebar {
    right: 12px;
    top: 12px;
    width: calc(100vw - 24px);
    height: calc(100vh - 24px);
  }
}
</style>
