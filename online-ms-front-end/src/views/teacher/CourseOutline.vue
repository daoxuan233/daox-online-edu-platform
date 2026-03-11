<template>
  <div class="course-outline-container">
    <!-- 页面头部 -->
    <div class="page-header glass-panel">
      <div class="header-content">
        <div class="header-left">
          <el-button
              type="text"
              @click="$router.go(-1)"
              class="back-btn"
          >
            <font-awesome-icon :icon="['fas', 'arrow-left']"/>
            <span class="back-text">返回课程编辑</span>
          </el-button>
          <div class="title-wrapper">
            <h1 class="page-title">
              <span class="icon-box header-icon"><font-awesome-icon :icon="['fas', 'sitemap']"/></span>
              课程大纲编辑
            </h1>
            <span class="page-subtitle">构建清晰的课程结构，提升学习体验</span>
          </div>
        </div>
        <div class="header-actions">
           <!-- 上传资料/课程资料按钮 - 仅在内容已保存时显示 -->
          <el-button 
            v-if="isSaved && !hasUnsavedChanges && !uploadMode" 
            class="glass-btn" 
            @click="handleUploadClick"
            :disabled="!isSaved || hasUnsavedChanges"
          >
            <font-awesome-icon :icon="['fas', 'cloud-upload-alt']"/>
            资料管理
          </el-button>
          
          <!-- 课程资料按钮 -->
          <el-button 
            v-if="uploadMode" 
            class="glass-btn" 
            @click="showCourseUploadDialog"
          >
            <font-awesome-icon :icon="['fas', 'folder-open']"/>
            课程资料
          </el-button>
          
          <!-- 小节资料按钮 -->
          <el-button 
            v-if="uploadMode" 
            class="glass-btn" 
            @click="handleSectionMaterialsClick"
          >
            <font-awesome-icon :icon="['fas', 'file-alt']"/>
            小节资料
          </el-button>

          <el-button type="primary" class="gradient-btn" @click="saveOutline">
            <font-awesome-icon :icon="['fas', 'save']"/>
            保存大纲
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="content-wrapper">
        <!-- 左侧编辑区域 -->
        <div class="edit-area">
          <!-- 大纲编辑器 -->
          <div class="glass-card section-card">
            <div class="section-header">
              <h3>
                <span class="icon-box"><font-awesome-icon :icon="['fas', 'layer-group']"/></span>
                课程结构
              </h3>
              <div class="header-actions">
                <el-button size="small" class="add-chapter-btn gradient-btn-small" @click="addChapter">
                  <font-awesome-icon :icon="['fas', 'plus']"/>
                  添加章节
                </el-button>
              </div>
            </div>
            <div class="section-content">
              <!-- 加载状态 -->
              <div v-if="loading" class="loading-container">
                <div class="spinner"></div>
                <span>正在加载课程大纲...</span>
              </div>
              
              <!-- 无数据提示 -->
              <div v-else-if="!hasOutlineData && chapters.length === 0" class="empty-state">
                <div class="empty-icon-wrapper">
                   <font-awesome-icon :icon="['fas', 'folder-plus']"/>
                </div>
                <h3>暂无课程大纲</h3>
                <p>点击上方"添加章节"开始构建您的课程内容</p>
              </div>
              
              <!-- 大纲编辑器 -->
              <div v-else class="outline-editor">
                <!-- 章节列表 -->
                <draggable
                    v-model="chapters"
                    group="chapters"
                    item-key="id"
                    class="chapters-list"
                    :animation="300"
                    ghost-class="ghost-card"
                    drag-class="drag-card"
                    handle=".drag-handle"
                    @start="onDragStart"
                    @end="onDragEnd"
                >
                  <template #item="{ element: chapter, index: chapterIndex }">
                    <div class="chapter-item glass-panel" :key="chapter.id">
                      <!-- 章节头部 -->
                      <div class="chapter-header">
                        <div class="chapter-info">
                          <div class="drag-handle">
                            <font-awesome-icon :icon="['fas', 'grip-vertical']"/>
                          </div>
                          <div class="chapter-number">第 {{ chapterIndex + 1 }} 章</div>
                          <div class="chapter-title-container">
                            <el-input
                                v-model="chapter.title"
                                placeholder="请输入章节标题"
                                class="chapter-title-input glass-input"
                                @blur="updateChapterTitle(chapter.id, chapter.title)"
                            />
                          </div>
                        </div>
                        <div class="chapter-actions">
                          <el-tooltip content="添加课时" placement="top">
                             <button class="icon-btn action" @click="addLesson(chapter.id)">
                                <font-awesome-icon :icon="['fas', 'plus-circle']"/>
                             </button>
                          </el-tooltip>
                          <button class="icon-btn action" @click="toggleChapter(chapter.id)">
                            <font-awesome-icon :icon="chapter.expanded ? ['fas', 'chevron-up'] : ['fas', 'chevron-down']"/>
                          </button>
                          <el-tooltip content="删除章节" placement="top">
                            <button class="icon-btn delete" @click="deleteChapter(chapter.id)">
                                <font-awesome-icon :icon="['fas', 'trash-alt']"/>
                            </button>
                          </el-tooltip>
                        </div>
                      </div>

                      <!-- 课时列表 -->
                      <transition name="expand">
                      <div v-show="chapter.expanded" class="lessons-container">
                        <draggable
                            v-model="chapter.lessons"
                            group="lessons"
                            item-key="id"
                            class="lessons-list"
                            :animation="200"
                            ghost-class="ghost-lesson"
                            drag-class="drag-lesson"
                            handle=".drag-handle"
                        >
                          <template #item="{ element: lesson, index: lessonIndex }">
                            <div class="lesson-item" :key="lesson.id">
                              <div class="lesson-info">
                                <div class="drag-handle small">
                                  <font-awesome-icon :icon="['fas', 'grip-lines']"/>
                                </div>
                                <div class="lesson-icon-wrapper">
                                  <font-awesome-icon :icon="getLessonIcon(lesson.type)"/>
                                </div>
                                <div class="lesson-number">{{ chapterIndex + 1 }}.{{ lessonIndex + 1 }}</div>
                                <div class="lesson-title-container">
                                  <el-input
                                      v-model="lesson.title"
                                      placeholder="请输入课时标题"
                                      class="lesson-title-input glass-input small"
                                      @blur="updateLessonTitle(lesson.id, lesson.title)"
                                  />
                                </div>
                                <div class="lesson-duration-wrapper">
                                    <font-awesome-icon :icon="['fas', 'clock']" class="clock-icon"/>
                                    <span>{{ lesson.duration || '00:00' }}</span>
                                </div>
                              </div>
                              <div class="lesson-actions">
                                <button class="icon-btn delete small" @click="deleteLesson(chapter.id, lesson.id)">
                                  <font-awesome-icon :icon="['fas', 'times']"/>
                                </button>
                              </div>
                            </div>
                          </template>
                        </draggable>
                        
                        <!-- 空状态 -->
                        <div v-if="chapter.lessons.length === 0" class="empty-lessons">
                          <div class="empty-dashed-box" @click="addLesson(chapter.id)">
                            <font-awesome-icon :icon="['fas', 'plus']"/>
                            <span>点击添加第一个课时</span>
                          </div>
                        </div>
                      </div>
                      </transition>
                    </div>
                  </template>
                </draggable>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧预览区域 -->
        <div class="preview-area">
          <!-- 大纲预览 -->
          <div class="glass-card preview-card">
            <div class="preview-header">
              <h3>
                <span class="icon-box small"><font-awesome-icon :icon="['fas', 'mobile-alt']"/></span>
                学生端预览
              </h3>
            </div>
            <div class="preview-content">
              <div class="outline-preview-container">
                 <div class="phone-mockup">
                    <div class="phone-screen">
                        <div class="preview-list">
                            <div v-for="(chapter, chapterIndex) in chapters" :key="chapter.id" class="preview-chapter">
                              <div class="preview-chapter-title">
                                <span class="p-chapter-num">第{{ chapterIndex + 1 }}章</span>
                                <span class="p-chapter-text">{{ chapter.title || '未命名章节' }}</span>
                              </div>
                              <div class="preview-lessons">
                                <div v-for="(lesson, lessonIndex) in chapter.lessons" :key="lesson.id" class="preview-lesson">
                                  <div class="p-lesson-left">
                                      <span class="p-lesson-num">{{ chapterIndex + 1 }}.{{ lessonIndex + 1 }}</span>
                                      <span class="p-lesson-text">{{ lesson.title || '未命名课时' }}</span>
                                  </div>
                                  <font-awesome-icon :icon="['fas', 'play-circle']" class="p-play-icon"/>
                                </div>
                              </div>
                            </div>
                            <div v-if="chapters.length === 0" class="preview-empty">
                              <p>大纲内容将显示在这里</p>
                            </div>
                        </div>
                    </div>
                 </div>
              </div>
            </div>
          </div>

          <!-- 统计信息 -->
          <div class="glass-card stats-card">
            <div class="stats-header">
              <h3>
                <span class="icon-box small"><font-awesome-icon :icon="['fas', 'chart-pie']"/></span>
                课程统计
              </h3>
            </div>
            <div class="stats-content">
              <div class="stat-row">
                <div class="stat-item">
                    <div class="stat-val">{{ chapters.length }}</div>
                    <div class="stat-lbl">章节</div>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                    <div class="stat-val">{{ totalLessons }}</div>
                    <div class="stat-lbl">课时</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 课程资料上传-对话框 -->
    <el-dialog
      v-model="showUploadDialog"
      title="上传课程资料"
      width="500px"
      custom-class="glass-dialog"
      :close-on-click-modal="false"
    >
      <div class="upload-dialog-content">
        <el-upload
          class="glass-upload-area"
          drag
          action="#"
          multiple
          :auto-upload="false"
          :file-list="courseFileList"
          :on-change="handleCourseFileChange"
        >
          <div class="upload-icon-wrapper">
             <font-awesome-icon :icon="['fas', 'cloud-upload-alt']"/>
          </div>
          <div class="el-upload__text">
            拖拽文件到此处，或<em>点击上传</em>
          </div>
        </el-upload>
        <div class="upload-options glass-panel">
            <el-checkbox
              v-model="allowDownload"
              label="允许学生下载"
              :disabled="currentIsVideo"
            />
            <el-tooltip
              v-if="currentIsVideo"
              content="视频文件默认不允许下载"
              placement="top"
            >
              <font-awesome-icon :icon="['fas', 'info-circle']" class="info-icon" />
            </el-tooltip>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button class="glass-btn" @click="showUploadDialog = false">取消</el-button>
          <el-button type="primary" class="gradient-btn" @click="submitCourseUpload">确定上传</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 小节资料上传-对话框 -->
    <el-dialog
      v-model="showSectionUploadDialog"
      :title="`上传资料到【${selectedLesson?.chapterName || '未知章节'} - ${selectedLesson?.name || '未知小节'}】`"
      width="500px"
      custom-class="glass-dialog"
      :close-on-click-modal="false"
    >
      <div class="upload-dialog-content">
        <el-upload
          class="glass-upload-area"
          drag
          action="#"
          multiple
          :auto-upload="false"
          :file-list="courseFileList"
          :on-change="handleCourseFileChange"
        >
          <div class="upload-icon-wrapper">
             <font-awesome-icon :icon="['fas', 'cloud-upload-alt']"/>
          </div>
          <div class="el-upload__text">
            拖拽文件到此处，或<em>点击上传</em>
          </div>
        </el-upload>
        <div class="upload-options glass-panel">
            <el-checkbox
              v-model="allowDownload"
              label="允许学生下载"
              :disabled="currentIsVideo"
            />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button class="glass-btn" @click="showSectionUploadDialog = false">取消</el-button>
          <el-button type="primary" class="gradient-btn" @click="submitSectionUpload">确定上传</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 小节资料上传区域 (抽屉式) -->
    <teleport to="body">
      <div class="section-upload-drawer glass-panel" :class="{ open: showSectionUpload }">
        <div class="drawer-header">
          <h3>
            <font-awesome-icon :icon="['fas', 'file-upload']"/>
            选择小节上传资料
          </h3>
          <button class="icon-btn close" @click="showSectionUpload = false">
            <font-awesome-icon :icon="['fas', 'times']"/>
          </button>
        </div>
        <div class="drawer-content">
          <div class="section-list">
            <div v-for="(chapter, chapterIndex) in chapters" :key="chapter.id" class="chapter-upload-group">
              <div class="chapter-upload-title">第{{ chapterIndex + 1 }}章：{{ chapter.title || '未命名章节' }}</div>
              <div class="lessons-upload-list">
                <div 
                  v-for="(lesson, lessonIndex) in chapter.lessons" 
                  :key="lesson.id" 
                  class="lesson-upload-item glass-card hover-lift"
                  @click="openSectionUploadDialog(lesson, chapter)"
                >
                  <div class="lesson-upload-info">
                    <span class="lesson-number">{{ chapterIndex + 1 }}.{{ lessonIndex + 1 }}</span>
                    <span class="lesson-title">{{ lesson.title || '未命名课时' }}</span>
                  </div>
                  <div class="upload-action-icon">
                    <font-awesome-icon :icon="['fas', 'cloud-upload-alt']"/>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import draggable from 'vuedraggable'
import { getCourseOutline, updateCourseOutline, uploadAndAssociateFile } from '@/api/teacher/teacherAPI.js'

const route = useRoute()
const router = useRouter()

// 课程ID
const courseId = ref(route.query.courseId)

// 章节数据
const chapters = ref([])

// 加载状态
const loading = ref(false)
const hasOutlineData = ref(false)

// 编辑状态管理
const hasUnsavedChanges = ref(false)
const isSaved = ref(true)
const originalData = ref(null)

// 资料上传状态管理
const uploadMode = ref(false) // 是否进入资料上传模式
const showUploadDialog = ref(false) // 是否显示上传对话框
const showSectionUploadDialog = ref(false) // 是否显示小节资料上传对话框
const showSectionUpload = ref(false) // 是否显示小节上传区域
const courseFileList = ref([]) // 课程文件列表
const selectedLesson = ref(null) // 选中的小节
const allowDownload = ref(true) // 是否允许下载
const currentIsVideo = ref(false) // 当前文件是否为视频

// 计算属性
const totalLessons = computed(() => {
  return chapters.value.reduce((total, chapter) => total + chapter.lessons.length, 0)
})

// 深度比较函数
const deepEqual = (obj1, obj2) => {
  return JSON.stringify(obj1) === JSON.stringify(obj2)
}

// 检测数据变化
const checkForChanges = () => {
  if (originalData.value) {
    const currentData = JSON.parse(JSON.stringify(chapters.value))
    hasUnsavedChanges.value = !deepEqual(currentData, originalData.value)
    isSaved.value = !hasUnsavedChanges.value
  }
}

// 监听章节数据变化
watch(
  chapters,
  () => {
    checkForChanges()
  },
  { deep: true }
)

// 保存原始数据快照
const saveOriginalData = () => {
  originalData.value = JSON.parse(JSON.stringify(chapters.value))
  hasUnsavedChanges.value = false
  isSaved.value = true
}

// 处理上传资料按钮点击
const handleUploadClick = () => {
  uploadMode.value = true
  ElMessage.success('资料管理模式已激活')
}

const handleSectionMaterialsClick = () => {
  showSectionUpload.value = !showSectionUpload.value
}

// 显示课程上传对话框
const showCourseUploadDialog = () => {
  showUploadDialog.value = true
}

// 获取视频时长
const getVideoDuration = (file) => {
  return new Promise((resolve) => {
    const video = document.createElement('video');
    video.preload = 'metadata';
    video.onloadedmetadata = () => {
      window.URL.revokeObjectURL(video.src);
      resolve(Math.round(video.duration));
    };
    video.src = window.URL.createObjectURL(file);
  });
};

// 处理课程文件变化
const handleCourseFileChange = async (file, fileList) => {
  courseFileList.value = fileList;
  // 检查文件类型
  const videoTypes = ['video/mp4', 'video/webm', 'video/ogg'];
  currentIsVideo.value = fileList.some(f => videoTypes.includes(f.raw.type));
  if (currentIsVideo.value) {
    allowDownload.value = false; // 视频文件默认不允许下载
    // 获取视频时长
    for (const fileItem of courseFileList.value) {
      if (videoTypes.includes(fileItem.raw.type) && !fileItem.durationSeconds) {
        try {
          const duration = await getVideoDuration(fileItem.raw);
          fileItem.durationSeconds = duration;
        } catch (e) {
          console.error('Failed to get video duration', e);
        }
      }
    }
  }
};

// 上传课程文件
const submitCourseUpload = async () => {
  if (courseFileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件');
    return;
  }

  try {
    for (const fileItem of courseFileList.value) {
      const fileInfo = {
        file: fileItem.raw,
        courseId: courseId.value,
        title: fileItem.name,
        allowDownload: allowDownload.value,
        chapterId: null, // 课程级别资料，章节ID为空
        sectionId: null  // 课程级别资料，小节ID为空
      };

      const videoTypes = ['video/mp4', 'video/webm', 'video/ogg'];
      if (videoTypes.includes(fileItem.raw.type)) {
        if (fileItem.durationSeconds) {
          fileInfo.durationSeconds = fileItem.durationSeconds;
        }
      }

      await uploadAndAssociateFile(fileInfo);
    }

    ElMessage.success(`成功上传 ${courseFileList.value.length} 个课程资料文件`);
    courseFileList.value = [];
    showUploadDialog.value = false;
  } catch (error) {
    ElMessage.error(`上传失败: ${error.message}`);
  }
};

// 提交小节资料上传
const submitSectionUpload = async () => {
  if (courseFileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件');
    return;
  }

  if (!selectedLesson.value?.sectionId || !selectedLesson.value?.chapterId) {
    ElMessage.error('请先选择要上传资料的小节');
    return;
  }

  try {
    for (const fileItem of courseFileList.value) {
      const fileInfo = {
        file: fileItem.raw,
        courseId: courseId.value,
        title: fileItem.name,
        allowDownload: allowDownload.value,
        chapterId: selectedLesson.value.chapterId,
        sectionId: selectedLesson.value.sectionId
      };

      const videoTypes = ['video/mp4', 'video/webm', 'video/ogg'];
      if (videoTypes.includes(fileItem.raw.type)) {
        if (fileItem.durationSeconds) {
          fileInfo.durationSeconds = fileItem.durationSeconds;
        }
      }

      await uploadAndAssociateFile(fileInfo);
    }

    ElMessage.success(`成功上传 ${courseFileList.value.length} 个资料文件到【${selectedLesson.value?.name}】`);
    courseFileList.value = [];
    showSectionUploadDialog.value = false;
  } catch (error) {
    ElMessage.error(`上传失败: ${error.message}`);
  }
};

// 打开小节资料上传对话框
const openSectionUploadDialog = (lesson, chapter) => {
  selectedLesson.value = {
    name: lesson.title,
    chapterName: chapter.title,
    sectionId: lesson.id,
    chapterId: chapter.id
  }
  showSectionUploadDialog.value = true
}

// 方法
const addChapter = () => {
  const newChapter = {
    id: Date.now(),
    title: '',
    expanded: true,
    lessons: []
  }
  chapters.value.push(newChapter)
  checkForChanges()
  ElMessage.success('章节添加成功')
}

const deleteChapter = async (chapterId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个章节吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = chapters.value.findIndex(c => c.id === chapterId)
    if (index > -1) {
      chapters.value.splice(index, 1)
      checkForChanges()
      ElMessage.success('章节删除成功')
    }
  } catch {
    // 用户取消删除
  }
}

const toggleChapter = (chapterId) => {
  const chapter = chapters.value.find(c => c.id === chapterId)
  if (chapter) {
    chapter.expanded = !chapter.expanded
  }
}

const addLesson = (chapterId) => {
  const chapter = chapters.value.find(c => c.id === chapterId)
  if (chapter) {
    const newLesson = {
      id: Date.now(),
      title: '',
      type: 'video',
      duration: ''
    }
    chapter.lessons.push(newLesson)
    // 确保展开
    chapter.expanded = true
    checkForChanges()
    ElMessage.success('课时添加成功')
  }
}

const deleteLesson = async (chapterId, lessonId) => {
  try {
    const chapter = chapters.value.find(c => c.id === chapterId)
    if (chapter) {
      const index = chapter.lessons.findIndex(l => l.id === lessonId)
      if (index > -1) {
        chapter.lessons.splice(index, 1)
        checkForChanges()
      }
    }
  } catch {
    // 忽略
  }
}

const updateChapterTitle = (chapterId, title) => {
  const chapter = chapters.value.find(c => c.id === chapterId)
  if (chapter) {
    chapter.title = title
    checkForChanges()
  }
}

const updateLessonTitle = (lessonId, title) => {
  chapters.value.forEach(chapter => {
    const lesson = chapter.lessons.find(l => l.id === lessonId)
    if (lesson) {
      lesson.title = title
      checkForChanges()
    }
  })
}

// 格式化时长（秒转换为分:秒格式）
const formatDuration = (durationSeconds) => {
  if (!durationSeconds || durationSeconds === 0) {
    return '0:00'
  }
  
  const minutes = Math.floor(durationSeconds / 60)
  const seconds = durationSeconds % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}

// 将时长字符串转换为秒数
const parseDurationToSeconds = (durationStr) => {
  if (!durationStr || typeof durationStr !== 'string') return 0
  const parts = durationStr.split(':')
  if (parts.length !== 2) return 0
  const minutes = parseInt(parts[0]) || 0
  const seconds = parseInt(parts[1]) || 0
  return minutes * 60 + seconds
}

const getLessonIcon = (type) => {
  const icons = {
    video: ['fas', 'play-circle'],
    document: ['fas', 'file-alt'],
    quiz: ['fas', 'question-circle'],
    assignment: ['fas', 'tasks']
  }
  return icons[type] || ['fas', 'play-circle']
}

const onDragStart = () => {
  // 拖拽开始时的处理
}

const onDragEnd = () => {
  // 拖拽结束时的处理
  checkForChanges()
}

const saveOutline = async () => {
  if (chapters.value.length === 0) {
    ElMessage.warning('请至少添加一个章节')
    return
  }
  
  // 验证章节和课时标题
  for (const chapter of chapters.value) {
    if (!chapter.title.trim()) {
      ElMessage.warning('请填写所有章节标题')
      return
    }
    for (const lesson of chapter.lessons) {
      if (!lesson.title.trim()) {
        ElMessage.warning('请填写所有课时标题')
        return
      }
    }
  }
  
  // 构建符合CourseOutlineDto结构的数据
  const outlineDto = {
    outline: chapters.value.map((chapter, chapterIndex) => ({
      id: typeof chapter.id === 'string' ? chapter.id : null,
      title: chapter.title.trim(),
      orderIndex: chapterIndex, // 数值越大表示在界面中显示的位置越靠后
      sections: chapter.lessons.map((lesson, lessonIndex) => ({
        id: typeof lesson.id === 'string' ? lesson.id : null,
        title: lesson.title.trim(),
        videoUrl: lesson.videoUrl || null, // 如无数据显式设置为null
        durationSeconds: parseDurationToSeconds(lesson.duration) || 0,
        orderIndex: lessonIndex // 数值越大表示在界面中显示的位置越靠后
      }))
    }))
  }
  
  try {
    loading.value = true
    await updateCourseOutline(courseId.value, outlineDto)
    // 保存成功后更新状态
    saveOriginalData()
    ElMessage.success('课程大纲保存成功')
  } catch (error) {
    console.error('保存课程大纲失败:', error)
    ElMessage.error('保存课程大纲失败，请重试')
  } finally {
    loading.value = false
  }
}

// 加载课程大纲数据
const loadCourseOutline = async () => {
  if (!courseId.value) {
    ElMessage.error('缺少课程ID参数')
    router.go(-1)
    return
  }

  loading.value = true
  try {
    const response = await getCourseOutline(courseId.value)
    
    // 处理不同的响应格式
    let outline = null
    
    // 检查是否是标准API响应格式
    if (response && response.success && response.code === 200 && response.data && response.data.outline) {
      outline = response.data.outline
    }
    else if (response && response.outline && Array.isArray(response.outline)) {
      outline = response.outline
    }
    else if (Array.isArray(response)) {
      outline = response
    }
    
    if (outline && outline.length > 0) {
      // 根据CourseOutlineDto结构转换数据格式
      // 按orderIndex排序章节
      const sortedOutline = [...outline].sort((a, b) => a.orderIndex - b.orderIndex)
      
      chapters.value = sortedOutline.map(chapter => ({
        id: chapter.id || Date.now() + Math.random(), 
        title: chapter.title || '',
        expanded: true, // 默认展开
        orderIndex: chapter.orderIndex,
        lessons: chapter.sections ? 
          // 按orderIndex排序小节
          [...chapter.sections]
            .sort((a, b) => a.orderIndex - b.orderIndex)
            .map(section => ({
              id: section.id || Date.now() + Math.random(), 
              title: section.title || '',
              type: 'video', 
              duration: formatDuration(section.durationSeconds),
              videoUrl: section.videoUrl || '',
              orderIndex: section.orderIndex
            })) : []
      }))
      
      hasOutlineData.value = true
      saveOriginalData() // 保存初始状态
      ElMessage.success('课程大纲加载成功')
    } else {
      chapters.value = []
      hasOutlineData.value = false
      saveOriginalData()
    }
  } catch (error) {
    console.error('加载课程大纲失败:', error)
    chapters.value = []
    hasOutlineData.value = false
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadCourseOutline()
})
</script>

<style scoped>
:root {
  --primary-gradient: linear-gradient(135deg, #0061ff 0%, #60efff 100%);
}

.course-outline-container {
  min-height: 100vh;
  background-color: transparent;
  padding: 0;
  font-family: 'Inter', 'PingFang SC', sans-serif;
}

/* Glass Components */
.glass-panel {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.glass-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
}

/* Header */
.page-header {
  padding: 16px 0;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.back-btn {
  color: #64748b;
  font-size: 14px;
}

.title-wrapper {
  display: flex;
  flex-direction: column;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-subtitle {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.glass-btn {
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(0, 0, 0, 0.05);
  color: #475569;
  padding: 8px 16px;
  border-radius: 12px;
  font-weight: 600;
  height: 36px;
}

.glass-btn:hover {
  background: white;
  color: #2563eb;
  border-color: #2563eb;
}

.gradient-btn {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  padding: 8px 16px;
  border-radius: 12px;
  font-weight: 600;
  height: 36px;
}

.gradient-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.4);
}

.gradient-btn-small {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border: none;
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 8px;
  color: white;
}

/* Main Content */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 30px;
}

.section-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-box {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  color: #0284c7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.icon-box.small { width: 28px; height: 28px; font-size: 12px; }

.section-content {
  padding: 24px;
  min-height: 400px;
}

/* Draggable List */
.chapters-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chapter-item {
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s;
}

.chapter-header {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.4);
}

.chapter-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.drag-handle {
  color: #94a3b8;
  cursor: grab;
  padding: 4px;
}

.drag-handle.small { font-size: 12px; }

.chapter-number {
  font-weight: 700;
  color: #334155;
  width: 60px;
}

.glass-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5);
  box-shadow: none;
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

.glass-input.small :deep(.el-input__wrapper) {
  padding: 1px 8px;
  height: 28px;
}

.chapter-actions {
  display: flex;
  gap: 8px;
}

.icon-btn {
  background: transparent;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #2563eb;
}

.icon-btn.delete:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.lessons-container {
  background: rgba(248, 250, 252, 0.5);
  padding: 8px 16px 16px;
}

.lesson-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  margin-top: 8px;
  background: white;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.02);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
}

.lesson-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.lesson-icon-wrapper {
  color: #64748b;
  font-size: 14px;
}

.lesson-number {
  color: #64748b;
  font-size: 13px;
  width: 40px;
}

.lesson-duration-wrapper {
  margin-left: auto;
  margin-right: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #94a3b8;
}

/* Empty States */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #94a3b8;
}

.empty-icon-wrapper {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-dashed-box {
  border: 2px dashed rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  padding: 12px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
  cursor: pointer;
  margin-top: 8px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.empty-dashed-box:hover {
  border-color: #2563eb;
  color: #2563eb;
  background: rgba(37, 99, 235, 0.05);
}

/* Preview Phone */
.preview-card {
  margin-bottom: 24px;
}

.phone-mockup {
  background: #1e293b;
  border-radius: 30px;
  padding: 12px;
  max-width: 280px;
  min-height: 568px;
  margin: 0 auto;
  box-shadow: 0 20px 40px rgba(0,0,0,0.2);
}

.phone-screen {
  background: white;
  border-radius: 20px;
  height: 568px;
  overflow-y: auto;
  padding: 16px;
}

.preview-chapter {
  margin-bottom: 16px;
}

.preview-chapter-title {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
}

.p-chapter-num { color: #64748b; margin-right: 6px; }

.preview-lesson {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  background: #f8fafc;
  border-radius: 8px;
  margin-bottom: 6px;
}

.p-lesson-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.p-lesson-num { color: #94a3b8; }
.p-play-icon { color: #cbd5e1; font-size: 12px; }

/* Stats */
.stat-row {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.stat-item {
  text-align: center;
}

.stat-val {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
}

.stat-lbl {
  font-size: 12px;
  color: #64748b;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(0,0,0,0.05);
}

/* Drawer */
.section-upload-drawer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 300px;
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 3000;
  display: flex;
  flex-direction: column;
}

.section-upload-drawer.open {
  transform: translateY(0);
  box-shadow: 0 -10px 40px rgba(0,0,0,0.1);
}

.drawer-header {
  padding: 16px 24px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.drawer-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.section-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chapter-upload-title {
  font-weight: 600;
  color: #475569;
  margin-bottom: 12px;
  font-size: 14px;
}

.lessons-upload-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.lesson-upload-item {
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  border: 1px solid transparent;
}

.lesson-upload-item:hover {
  border-color: #2563eb;
}

.lesson-upload-info {
  display: flex;
  flex-direction: column;
}

.lesson-upload-info .lesson-number {
  font-size: 11px;
  color: #94a3b8;
}

.lesson-upload-info .lesson-title {
  font-size: 13px;
  font-weight: 500;
  color: #334155;
}

.upload-action-icon {
  color: #2563eb;
  opacity: 0;
  transition: opacity 0.2s;
}

.lesson-upload-item:hover .upload-action-icon {
  opacity: 1;
}

/* Responsive */
@media (max-width: 1200px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }
}
</style>
