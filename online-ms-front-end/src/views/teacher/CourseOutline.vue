<template>
  <div class="course-outline-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <el-button
              link
              @click="$router.go(-1)"
              class="back-btn"
          >
            <font-awesome-icon :icon="['fas', 'arrow-left']"/>
            返回课程编辑
          </el-button>
          <h1 class="page-title">
            <font-awesome-icon :icon="['fas', 'list-alt']"/>
            课程大纲编辑
          </h1>
        </div>
        <div class="header-actions">
          <el-button type="primary" class="action-btn publish-btn" @click="saveOutline">
            <font-awesome-icon :icon="['fas', 'check']"/>
            保存大纲
          </el-button>
          <!-- 上传资料/课程资料按钮 - 仅在内容已保存时显示 -->
          <el-button 
            v-if="isSaved && !hasUnsavedChanges && !uploadMode" 
            class="action-btn upload-btn" 
            @click="handleUploadClick"
            :disabled="!isSaved || hasUnsavedChanges"
          >
            <font-awesome-icon :icon="['fas', 'upload']"/>
            上传资料
          </el-button>
          
          <!-- 课程资料按钮 -->
          <el-button 
            v-if="uploadMode" 
            class="action-btn course-materials-btn" 
            @click="showCourseUploadDialog"
          >
            <font-awesome-icon :icon="['fas', 'folder']"/>
            课程资料
          </el-button>
          
          <!-- 小节资料按钮 -->
          <!-- 小节资料按钮 -->
          <el-button 
            v-if="uploadMode" 
            class="action-btn section-materials-btn" 
            @click="handleSectionMaterialsClick"
          >
            <font-awesome-icon :icon="['fas', 'file-alt']"/>
            小节资料
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
          <div class="section-card">
            <div class="section-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'sitemap']"/>
                课程大纲结构
              </h3>
              <div class="header-actions">
                <el-button size="small" class="add-chapter-btn" @click="addChapter">
                  <font-awesome-icon :icon="['fas', 'plus']"/>
                  添加章节
                </el-button>
              </div>
            </div>
            <div class="section-content">
              <!-- 加载状态 -->
              <div v-if="loading" class="loading-container">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>正在加载课程大纲...</span>
              </div>
              
              <!-- 无数据提示 -->
              <div v-else-if="!hasOutlineData && chapters.length === 0" class="empty-state">
                <el-empty description="暂无课程大纲">
                  <template #description>
                    <p>您还没有创建课程大纲</p>
                    <p>点击"添加章节"开始创建您的课程内容</p>
                  </template>
                </el-empty>
              </div>
              
              <!-- 大纲编辑器 -->
              <div v-else class="outline-editor">
                <!-- 章节列表 -->
                <draggable
                    v-model="chapters"
                    group="chapters"
                    item-key="id"
                    class="chapters-list"
                    :animation="200"
                    ghost-class="ghost-chapter"
                    chosen-class="chosen-chapter"
                    drag-class="drag-chapter"
                    @start="onDragStart"
                    @end="onDragEnd"
                >
                  <template #item="{ element: chapter, index: chapterIndex }">
                    <div class="chapter-item neumorphism-raised" :key="chapter.id">
                      <!-- 章节头部 -->
                      <div class="chapter-header">
                        <div class="chapter-info">
                          <div class="drag-handle">
                            <font-awesome-icon :icon="['fas', 'grip-vertical']"/>
                          </div>
                          <div class="chapter-number">第{{ chapterIndex + 1 }}章</div>
                          <div class="chapter-title-container">
                            <el-input
                                v-model="chapter.title"
                                placeholder="请输入章节标题"
                                class="chapter-title-input"
                                @blur="updateChapterTitle(chapter.id, chapter.title)"
                            />
                          </div>
                        </div>
                        <div class="chapter-actions">
                          <el-button size="small" class="action-btn" @click="addLesson(chapter.id)">
                            <font-awesome-icon :icon="['fas', 'plus']"/>
                            添加课时
                          </el-button>
                          <el-button size="small" class="action-btn expand-btn" @click="toggleChapter(chapter.id)">
                            <font-awesome-icon :icon="chapter.expanded ? ['fas', 'chevron-up'] : ['fas', 'chevron-down']"/>
                          </el-button>
                          <el-button size="small" class="action-btn delete-btn" @click="deleteChapter(chapter.id)">
                            <font-awesome-icon :icon="['fas', 'trash']"/>
                          </el-button>
                        </div>
                      </div>

                      <!-- 课时列表 -->
                      <div v-show="chapter.expanded" class="lessons-container">
                        <draggable
                            v-model="chapter.lessons"
                            group="lessons"
                            item-key="id"
                            class="lessons-list"
                            :animation="200"
                            ghost-class="ghost-lesson"
                            chosen-class="chosen-lesson"
                            drag-class="drag-lesson"
                        >
                          <template #item="{ element: lesson, index: lessonIndex }">
                            <div class="lesson-item neumorphism-inset" :key="lesson.id">
                              <div class="lesson-info">
                                <div class="drag-handle">
                                  <font-awesome-icon :icon="['fas', 'grip-vertical']"/>
                                </div>
                                <div class="lesson-icon">
                                  <font-awesome-icon :icon="getLessonIcon(lesson.type)"/>
                                </div>
                                <div class="lesson-number">{{ chapterIndex + 1 }}.{{ lessonIndex + 1 }}</div>
                                <div class="lesson-title-container">
                                  <el-input
                                      v-model="lesson.title"
                                      placeholder="请输入课时标题"
                                      class="lesson-title-input"
                                      @blur="updateLessonTitle(lesson.id, lesson.title)"
                                  />
                                </div>

                              </div>
                              <div class="lesson-actions">

                                <el-button size="small" class="action-btn delete-btn" @click="deleteLesson(chapter.id, lesson.id)">
                                  <font-awesome-icon :icon="['fas', 'trash']"/>
                                </el-button>
                              </div>
                            </div>
                          </template>
                        </draggable>
                        
                        <!-- 空状态 -->
                        <div v-if="chapter.lessons.length === 0" class="empty-lessons">
                          <div class="empty-icon">
                            <font-awesome-icon :icon="['fas', 'plus-circle']"/>
                          </div>
                          <p>暂无课时，点击"添加课时"开始创建</p>
                        </div>
                      </div>
                    </div>
                  </template>
                </draggable>
                
                <!-- 空状态 -->
                <div v-if="chapters.length === 0" class="empty-chapters">
                  <div class="empty-icon">
                    <font-awesome-icon :icon="['fas', 'book-open']"/>
                  </div>
                  <h3>开始创建课程大纲</h3>
                  <p>添加章节和课时，构建完整的课程结构</p>
                  <el-button type="primary" class="add-first-chapter-btn" @click="addChapter">
                    <font-awesome-icon :icon="['fas', 'plus']"/>
                    添加第一个章节
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧预览区域 -->
        <div class="preview-area">
          <!-- 大纲预览 -->
          <div class="preview-card">
            <div class="preview-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'eye']"/>
                大纲预览
              </h3>
            </div>
            <div class="preview-content">
              <div class="outline-preview">
                <div v-for="(chapter, chapterIndex) in chapters" :key="chapter.id" class="preview-chapter">
                  <div class="preview-chapter-title">
                    <span class="chapter-number">第{{ chapterIndex + 1 }}章</span>
                    <span class="chapter-title">{{ chapter.title || '未命名章节' }}</span>
                  </div>
                  <div class="preview-lessons">
                    <div v-for="(lesson, lessonIndex) in chapter.lessons" :key="lesson.id" class="preview-lesson">
                      <span class="lesson-number">{{ chapterIndex + 1 }}.{{ lessonIndex + 1 }}</span>
                      <font-awesome-icon :icon="getLessonIcon(lesson.type)" class="lesson-icon"/>
                      <span class="lesson-title">{{ lesson.title || '未命名课时' }}</span>
                      <span class="lesson-duration">{{ lesson.duration || '0:00' }}</span>
                    </div>
                  </div>
                </div>
                
                <div v-if="chapters.length === 0" class="preview-empty">
                  <p>暂无内容</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 统计信息 -->
          <div class="stats-card">
            <div class="stats-header">
              <h3>
                <font-awesome-icon :icon="['fas', 'chart-bar']"/>
                课程统计
              </h3>
            </div>
            <div class="stats-content">
              <div class="stat-item">
                <div class="stat-icon">
                  <font-awesome-icon :icon="['fas', 'book']"/>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ chapters.length }}</div>
                  <div class="stat-label">章节数</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <font-awesome-icon :icon="['fas', 'play-circle']"/>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ totalLessons }}</div>
                  <div class="stat-label">课时数</div>
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
      :close-on-click-modal="false"
    >
      <div class="upload-dialog-content">
        <el-upload
          class="upload-demo"
          drag
          action="#"
          multiple
          :auto-upload="false"
          :file-list="courseFileList"
          :on-change="handleCourseFileChange"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持上传 PDF、DOC、PPT、视频等课程相关资料
            </div>
          </template>
        </el-upload>
        <div class="upload-options">
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
          <el-button @click="showUploadDialog = false">取消</el-button>
          <el-button type="primary" @click="submitCourseUpload">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 小节资料上传-对话框 -->
    <el-dialog
      v-model="showSectionUploadDialog"
      :title="`上传资料到【${selectedLesson?.chapterName || '未知章节'} - ${selectedLesson?.name || '未知小节'}】`"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="upload-dialog-content">
        <el-upload
          class="upload-demo"
          drag
          action="#"
          multiple
          :auto-upload="false"
          :file-list="courseFileList"
          :on-change="handleCourseFileChange"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持上传 PDF、DOC、PPT、视频等课程相关资料
            </div>
          </template>
        </el-upload>
        <div class="upload-options">
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
          <el-button @click="showSectionUploadDialog = false">取消</el-button>
          <el-button type="primary" @click="submitSectionUpload">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 小节资料上传区域 -->
    <div v-if="showSectionUpload" class="section-upload-area">
      <div class="section-upload-header">
        <h3>
          <font-awesome-icon :icon="['fas', 'file-upload']"/>
          小节资料上传
        </h3>
        <el-button size="small" @click="showSectionUpload = false">
          <font-awesome-icon :icon="['fas', 'times']"/>
          关闭
        </el-button>
      </div>
      <div class="section-upload-content">
        <p class="upload-tip">请选择要上传资料的小节：</p>
        <div class="section-list">
          <div v-for="(chapter, chapterIndex) in chapters" :key="chapter.id" class="chapter-upload-item">
            <div class="chapter-upload-title">第{{ chapterIndex + 1 }}章：{{ chapter.title || '未命名章节' }}</div>
            <div class="lessons-upload-list">
              <div 
            v-for="(lesson, lessonIndex) in chapter.lessons" 
            :key="lesson.id" 
            class="lesson-upload-item"
            @click="selectLessonForUpload(lesson, chapter, lessonIndex)"
          >
            <div class="lesson-upload-info">
              <font-awesome-icon :icon="getLessonIcon(lesson.type)"/>
              <span class="lesson-number">{{ chapterIndex + 1 }}.{{ lessonIndex + 1 }}</span>
              <span class="lesson-title">{{ lesson.title || '未命名课时' }}</span>
            </div>
            <el-button size="small" type="primary" @click.stop="openSectionUploadDialog(lesson, chapter)">
              <font-awesome-icon :icon="['fas', 'upload']"/>
              上传资料
            </el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, UploadFilled } from '@element-plus/icons-vue'
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

// 处理小节资料按钮点击
const handleSectionMaterialsClick = () => {
  showSectionUpload.value = !showSectionUpload.value
}

// 显示课程上传对话框
const showCourseUploadDialog = () => {
  showUploadDialog.value = true
}

// 切换小节上传区域显示
const toggleSectionUpload = () => {
  showSectionUpload.value = !showSectionUpload.value
  if (showSectionUpload.value) {
    ElMessage.info('请在下方选择要上传资料的小节')
  }
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
  // 检查文件类型，如果是视频文件则默认不允许下载
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

// 关闭上传对话框
const handleCloseUploadDialog = (done) => {
  if (courseFileList.value.length > 0) {
    ElMessageBox.confirm('确定要关闭吗？未保存的文件将丢失。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      courseFileList.value = []
      done()
    }).catch(() => {})
  } else {
    done()
  }
}

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

// 选择小节进行上传
const selectLessonForUpload = (lesson, chapter) => {
  selectedLesson.value = {
    name: lesson.title,
    chapterName: chapter.title,
    sectionId: lesson.id,
    chapterId: chapter.id
  }
}

// 打开小节资料上传对话框
const openSectionUploadDialog = (lesson, chapter) => {
  selectLessonForUpload(lesson, chapter)
  showSectionUploadDialog.value = true
}

// 课程资料上传功能（保留原有功能）
const uploadCourseMaterials = () => {
  ElMessage.info('课程资料上传功能开发中...')
  // TODO: 实现课程资料上传逻辑
}

// 方法
const addChapter = () => {
  const newChapter = {
    id: Date.now(),
    title: '',
    expanded: false,
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
    checkForChanges()
    ElMessage.success('课时添加成功')
  }
}

const deleteLesson = async (chapterId, lessonId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个课时吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const chapter = chapters.value.find(c => c.id === chapterId)
    if (chapter) {
      const index = chapter.lessons.findIndex(l => l.id === lessonId)
      if (index > -1) {
        chapter.lessons.splice(index, 1)
        checkForChanges()
        ElMessage.success('课时删除成功')
      }
    }
  } catch {
    // 用户取消删除
  }
}

const editLesson = (lesson) => {
  ElMessage.info('课时详细编辑功能开发中...')
}

const updateChapterTitle = (chapterId, title) => {
  const chapter = chapters.value.find(c => c.id === chapterId)
  if (chapter) {
    chapter.title = title
  }
}

const updateLessonTitle = (lessonId, title) => {
  chapters.value.forEach(chapter => {
    const lesson = chapter.lessons.find(l => l.id === lessonId)
    if (lesson) {
      lesson.title = title
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
  ElMessage.success('大纲结构已更新')
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
      id: chapter.id, // 包含章节ID
      title: chapter.title.trim(),
      orderIndex: chapterIndex, // 数值越大表示在界面中显示的位置越靠后
      sections: chapter.lessons.map((lesson, lessonIndex) => ({
        id: lesson.id, // 包含小节ID
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
    // 初始化原始数据快照
    saveOriginalData()
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
    
    if (outline && outline.length > 0) {
      // 根据CourseOutlineDto结构转换数据格式
      // 按orderIndex排序章节
      const sortedOutline = [...outline].sort((a, b) => a.orderIndex - b.orderIndex)
      
      chapters.value = sortedOutline.map(chapter => ({
        id: chapter.id || Date.now() + Math.random(), // 使用后端返回的章节ID，如果没有则生成
        title: chapter.title || '',
        expanded: false,
        orderIndex: chapter.orderIndex,
        lessons: chapter.sections ? 
          // 按orderIndex排序小节
          [...chapter.sections]
            .sort((a, b) => a.orderIndex - b.orderIndex)
            .map(section => ({
              id: section.id || Date.now() + Math.random(), // 使用后端返回的小节ID，如果没有则生成
              title: section.title || '',
              type: 'video', // 默认类型为视频
              duration: formatDuration(section.durationSeconds),
              videoUrl: section.videoUrl || '',
              orderIndex: section.orderIndex
            })) : []
      }))
      
      hasOutlineData.value = true
      ElMessage.success('课程大纲加载成功')
    } else {
      // 没有找到outline数据或outline为空
      chapters.value = []
      hasOutlineData.value = false
      ElMessage.info('暂无课程大纲，您可以开始创建')
    }
  } catch (error) {
    console.error('加载课程大纲失败:', error)
    chapters.value = []
    hasOutlineData.value = false
    ElMessage.warning('加载课程大纲失败，您可以创建新的大纲')
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
/* 基础样式 */
.course-outline-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  position: relative;
  overflow-x: hidden;
}

.course-outline-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(120, 119, 198, 0.2) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.course-outline-container > * {
  position: relative;
  z-index: 1;
}

/* 页面头部 */
.page-header {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 24px 0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 14px;
  padding: 12px 18px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
  color: #ffffff;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 
    0 6px 20px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.page-title {
  font-size: 32px;
  font-weight: 800;
  color: #ffffff;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  background: linear-gradient(135deg, #ffffff 0%, #f0f8ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  padding: 14px 24px;
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0.5px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.action-btn:hover::before {
  left: 100%;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.4);
  transform: translateY(-3px) scale(1.02);
  box-shadow: 
    0 8px 24px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.save-btn {
  color: #67C23A;
}

.publish-btn {
  background: linear-gradient(135deg, #002FA7, #517B4D);
  color: white;
  box-shadow: 8px 8px 16px rgba(0, 47, 167, 0.3), -8px -8px 16px rgba(255, 255, 255, 0.8);
}

.upload-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
}

.upload-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.upload-btn:hover::before {
  left: 100%;
}

.upload-btn:hover {
  background: linear-gradient(135deg, #f2a5fc 0%, #f76b7a 100%);
  transform: translateY(-3px) scale(1.02);
  box-shadow: 
    0 10px 30px rgba(240, 147, 251, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.upload-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
  background: rgba(255, 255, 255, 0.1) !important;
  box-shadow: inset 4px 4px 8px rgba(0, 0, 0, 0.1), inset -4px -4px 8px rgba(255, 255, 255, 0.1) !important;
}

/* 课程资料按钮样式 */
.course-materials-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.course-materials-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.course-materials-btn:hover::before {
  left: 100%;
}

.course-materials-btn:hover {
  background: linear-gradient(135deg, #7c8cec 0%, #8a5fb0 100%);
  transform: translateY(-3px) scale(1.02);
  box-shadow: 
    0 10px 30px rgba(102, 126, 234, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

/* 小节资料按钮样式 */
.section-materials-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.section-materials-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.section-materials-btn:hover::before {
  left: 100%;
}

.section-materials-btn:hover {
  background: linear-gradient(135deg, #f2a5fc 0%, #f76b7a 100%);
  transform: translateY(-3px) scale(1.02);
  box-shadow: 
    0 10px 30px rgba(240, 147, 251, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

/* 小节上传区域样式 */
.section-upload-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1px solid #e4e7ed;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 400px;
  overflow-y: auto;
}

.section-upload-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
  background: #f8f9fa;
}

.section-upload-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.section-upload-content {
  padding: 20px 24px;
}

.upload-tip {
  margin: 0 0 16px 0;
  color: #606266;
  font-size: 14px;
}

.chapter-upload-item {
  margin-bottom: 20px;
}

.chapter-upload-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: #f0f2f5;
  border-radius: 6px;
}

.lessons-upload-list {
  margin-left: 20px;
}

.lesson-upload-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 8px;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.lesson-upload-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.lesson-upload-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.lesson-upload-info .lesson-number {
  font-weight: 600;
  color: #909399;
  min-width: 40px;
}

.lesson-upload-info .lesson-title {
  color: #303133;
}

/* 上传对话框样式 */
.upload-dialog-content {
  padding: 24px 0;
  position: relative;
}

.upload-demo {
  width: 100%;
  margin-bottom: 24px;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(102, 126, 234, 0.05);
  border: 2px dashed rgba(102, 126, 234, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.upload-demo:hover {
  background: rgba(102, 126, 234, 0.1);
  border-color: rgba(102, 126, 234, 0.5);
  transform: scale(1.02);
}

.upload-options {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 12px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.upload-options:hover {
  background: rgba(102, 126, 234, 0.1);
}

.upload-options .info-icon {
  color: #667eea;
  font-size: 18px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

/* 主要内容 */
.main-content {
  padding: 24px;
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 32px;
}

/* 编辑区域 */
.edit-area {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-card {
  background: #f0f0f3;
  box-shadow: 8px 8px 16px #d1d1d4, -8px -8px 16px #ffffff;
  border-radius: 16px;
  overflow: hidden;
}

.section-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-content {
  padding: 24px;
}

/* 新拟态样式 */
.neumorphism-raised {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 
    0 12px 32px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.neumorphism-raised:hover {
  background: rgba(255, 255, 255, 0.35);
  transform: translateY(-4px) scale(1.01);
  box-shadow: 
    0 16px 40px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.neumorphism-inset {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 
    inset 0 4px 16px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.neumorphism-inset:hover {
  background: rgba(255, 255, 255, 0.2);
  box-shadow: 
    inset 0 6px 20px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

/* 大纲编辑器 */
.outline-editor {
  min-height: 400px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  padding: 32px;
  box-shadow: 
    0 16px 40px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.outline-editor:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 
    0 20px 50px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.chapters-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chapter-item {
  padding: 24px;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
}

.chapter-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.6s;
}

.chapter-item:hover::before {
  left: 100%;
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chapter-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.drag-handle {
  color: #909399;
  cursor: grab;
  padding: 4px;
}

.drag-handle:active {
  cursor: grabbing;
}

.chapter-number {
  background: linear-gradient(135deg, #002FA7, #517B4D);
  color: white;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  min-width: 60px;
  text-align: center;
}

.chapter-title-container {
  flex: 1;
}

.chapter-title-input {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.chapter-title-input:focus {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.1);
  outline: none;
}

.chapter-actions {
  display: flex;
  gap: 12px;
}

.chapter-actions .btn {
  padding: 8px 16px;
  font-size: 13px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.25);
  color: rgba(255, 255, 255, 0.9);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.chapter-actions .btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.add-chapter-btn {
  background: #f0f0f3;
  box-shadow: 8px 8px 16px #d1d1d4, -8px -8px 16px #ffffff;
  border-radius: 8px;
  border: none;
  padding: 8px 12px;
  color: #002FA7;
  font-size: 12px;
}

.expand-btn {
  color: #909399;
}

.delete-btn {
  color: #F56C6C;
}

/* 课时列表 */
.lessons-container {
  margin-top: 16px;
  padding-left: 40px;
}

.lessons-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.lesson-item {
  padding: 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  position: relative;
  overflow: hidden;
}

.lesson-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.08), transparent);
  transition: left 0.5s;
}

.lesson-item:hover::before {
  left: 100%;
}

.lesson-item:hover {
  transform: translateX(4px);
}

.lesson-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  margin-right: 11px;
}

.lesson-icon {
  color: #002FA7;
  width: 16px;
}

.lesson-number {
  background: rgba(0, 47, 167, 0.1);
  color: #002FA7;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  min-width: 40px;
  text-align: center;
}

.lesson-title-container {
  flex: 1;
}

.lesson-title-input {
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  padding: 10px 14px;
  font-size: 15px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.lesson-title-input:focus {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.35);
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.1);
  outline: none;
}

.type-select {
  width: 80px;
}

.duration-input {
  width: 80px;
}

.lesson-actions {
  display: flex;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.lesson-actions .btn {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.85);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
}

.lesson-actions .btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: #ffffff;
  transform: translateY(-1px) scale(1.05);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
}

.edit-btn {
  color: #409EFF;
}

/* 拖拽样式 */
.ghost-chapter {
  opacity: 0.5;
  background: rgba(0, 47, 167, 0.1);
}

.chosen-chapter {
  transform: rotate(5deg);
}

.drag-chapter {
  transform: rotate(5deg);
  opacity: 0.8;
}

.ghost-lesson {
  opacity: 0.5;
  background: rgba(0, 47, 167, 0.1);
}

.chosen-lesson {
  transform: rotate(2deg);
}

.drag-lesson {
  transform: rotate(2deg);
  opacity: 0.8;
}

/* 空状态 */
.empty-chapters, .empty-lessons {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-icon {
  font-size: 48px;
  color: #DCDFE6;
  margin-bottom: 16px;
}

.empty-chapters h3 {
  font-size: 18px;
  color: #606266;
  margin: 16px 0 8px;
}

.empty-chapters p {
  margin-bottom: 24px;
}

.add-first-chapter-btn {
  background: linear-gradient(135deg, #002FA7, #517B4D);
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  color: white;
  font-weight: 600;
}

/* 预览区域 */
.preview-area {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.preview-card, .stats-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 
    0 16px 40px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.preview-card:hover, .stats-card:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-4px);
  box-shadow: 
    0 20px 50px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.preview-header, .stats-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.1);
}

.preview-header h3, .stats-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.95);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #ffffff 0%, #f0f8ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.3px;
}

.preview-content, .stats-content {
  padding: 24px;
  background: rgba(255, 255, 255, 0.05);
}

/* 大纲预览 */
.outline-preview {
  max-height: 400px;
  overflow-y: auto;
}

.preview-chapter {
  margin-bottom: 20px;
}

.preview-chapter-title {
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.preview-chapter-title .chapter-number {
  background: linear-gradient(135deg, #002FA7, #517B4D);
  color: white;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 11px;
}

.preview-lessons {
  padding-left: 20px;
}

.preview-lesson {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.2s ease;
}

.preview-lesson:hover {
  color: rgba(255, 255, 255, 0.95);
  transform: translateX(4px);
}

.preview-lesson .lesson-number {
  background: rgba(0, 47, 167, 0.1);
  color: #002FA7;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  min-width: 30px;
  text-align: center;
}

.preview-lesson .lesson-icon {
  color: #002FA7;
  width: 12px;
}

.preview-lesson .lesson-title {
  flex: 1;
}

.preview-lesson .lesson-duration {
  color: #ffffff;
  font-size: 12px;
}

.preview-empty {
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
  padding: 60px 20px;
  font-style: italic;
}

/* 统计信息 */
.stats-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.stat-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.6s;
}

.stat-item:hover::before {
  left: 100%;
}

.stat-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #002FA7, #517B4D);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  box-shadow: 
    0 8px 16px rgba(0, 47, 167, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-item:hover .stat-icon {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 
    0 12px 24px rgba(0, 47, 167, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #ffffff 0%, #f0f8ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* 加载状态和空状态样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.loading-container .el-icon {
  font-size: 24px;
  margin-bottom: 12px;
}

.loading-container span {
  font-size: 14px;
}

.empty-state {
  padding: 40px 20px;
}

.empty-state .el-empty {
  padding: 20px;
}

.empty-state p {
  margin: 8px 0;
  color: #909399;
  font-size: 14px;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content-wrapper {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .preview-section {
    position: static;
    order: -1;
  }
}

@media (max-width: 768px) {
  .course-outline {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
    padding: 20px 24px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .upload-buttons {
    flex-direction: column;
    gap: 16px;
  }
  
  .upload-btn {
    width: 100%;
    justify-content: center;
  }
  
  .outline-editor {
    padding: 20px;
  }
  
  .chapter-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .chapter-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .lesson-item {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .lesson-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .chapter-info {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .lesson-info {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .lessons-container {
    padding-left: 20px;
  }
}

@media (max-width: 480px) {
  .course-outline {
    padding: 12px;
  }
  
  .page-header {
    padding: 16px 20px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .outline-editor {
    padding: 16px;
  }
  
  .chapter-item {
    padding: 16px;
  }
  
  .chapter-title {
    font-size: 16px;
  }
  
  .lesson-item {
    padding: 12px;
  }
  
  .lesson-title {
    font-size: 14px;
  }
  
  .preview-section {
    padding: 20px;
  }
  
  .stat-item {
    padding: 16px;
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }
  
  .stat-value {
    font-size: 20px;
  }
}
</style>