<template>
  <div class="course-card neumorphism neumorphism-hover" @click="handleClick">
    <!-- 课程封面 -->
    <div class="course-cover">
      <img 
        :src="course.cover || 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDMwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIzMDAiIGhlaWdodD0iMjAwIiBmaWxsPSIjRjVGNUY1Ii8+CjxwYXRoIGQ9Ik0xMzUgODBIMTY1VjEyMEgxMzVWODBaIiBmaWxsPSIjQ0NDQ0NDIi8+CjxwYXRoIGQ9Ik0xMjAgOTVIMTgwVjEwNUgxMjBWOTVaIiBmaWxsPSIjQ0NDQ0NDIi8+Cjx0ZXh0IHg9IjE1MCIgeT0iMTQwIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjOTk5OTk5IiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMTIiPkNvdXJzZSBJbWFnZTwvdGV4dD4KPC9zdmc+'" 
        :alt="course.title"
        class="cover-image"
      >
      <div class="course-badge" v-if="course.badge">
        <span class="badge-text">{{ course.badge }}</span>
      </div>
      <div class="course-progress" v-if="showProgress && course.progress !== undefined">
        <div class="progress-bar">
          <div 
            class="progress-fill" 
            :style="{ width: course.progress + '%' }"
          ></div>
        </div>
        <span class="progress-text">{{ course.progress }}%</span>
      </div>
    </div>
    
    <!-- 课程信息 -->
    <div class="course-info">
      <h3 class="course-title">{{ course.title }}</h3>
      <p class="course-description">{{ course.description }}</p>
      
      <div class="course-meta">
        <div class="meta-item">
          <font-awesome-icon :icon="['fas', 'user']" class="text-primary" />
          <span>{{ course.instructor }}</span>
        </div>
      </div>
      
      <div class="course-footer">
        <div class="course-rating" v-if="course.rating">
          <div class="stars">
            <font-awesome-icon 
              v-for="star in 5" 
              :key="star"
              :icon="['fas', 'star']"
              :class="{
                'star-filled': star <= Math.floor(course.rating),
                'star-half': star === Math.ceil(course.rating) && course.rating % 1 !== 0,
                'star-empty': star > Math.ceil(course.rating)
              }"
            />
          </div>
          <span class="rating-text">{{ course.rating }}</span>
        </div>
        
        <div class="course-price">
          <span v-if="course.price === 0" class="price-free">免费</span>
          <span v-else class="price-paid">¥{{ course.price }}</span>
        </div>
      </div>
    </div>
    
    <!-- 悬浮操作按钮 -->
    <div class="course-actions" v-if="showActions">
      <el-button 
        size="small" 
        type="primary" 
        class="action-btn"
        @click.stop="handleAction('learn')"
      >
        <font-awesome-icon :icon="['fas', 'play']" class="mr-xs" />
        {{ actionText }}
      </el-button>
      <el-button 
        size="small" 
        class="action-btn"
        @click.stop="handleAction('favorite')"
      >
        <font-awesome-icon :icon="['fas', 'heart']" :class="{ 'text-red-500': course.isFavorite }" />
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  course: {
    type: Object,
    required: true,
    default: () => ({
      id: '',
      title: '',
      description: '',
      cover: '',
      instructor: '',
      duration: '',
      students: 0,
      rating: 0,
      price: 0,
      progress: 0,
      badge: '',
      isFavorite: false
    })
  },
  showProgress: {
    type: Boolean,
    default: false
  },
  showActions: {
    type: Boolean,
    default: true
  },
  actionText: {
    type: String,
    default: '开始学习'
  }
})

const emit = defineEmits(['click', 'action'])

const handleClick = () => {
  emit('click', props.course)
}

const handleAction = (action) => {
  emit('action', { action, course: props.course })
}
</script>

<style scoped>
.course-card {
  position: relative;
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  background: var(--surface-color);
  max-width: 320px;
  margin: var(--spacing-md);
}

.course-card:hover .course-actions {
  opacity: 1;
  transform: translateY(0);
}

.course-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-normal);
}

.course-card:hover .cover-image {
  transform: scale(1.05);
}

.course-badge {
  position: absolute;
  top: var(--spacing-sm);
  right: var(--spacing-sm);
  background: var(--primary-color);
  color: white;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-sm);
  font-size: 0.75rem;
  font-weight: 500;
}

.course-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  padding: var(--spacing-sm);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.progress-bar {
  flex: 1;
  height: 4px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--primary-color);
  transition: width var(--transition-normal);
}

.progress-text {
  color: white;
  font-size: 0.75rem;
  font-weight: 500;
  min-width: 35px;
}

.course-info {
  padding: var(--spacing-lg);
}

.course-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-description {
  color: var(--text-secondary);
  font-size: 0.875rem;
  line-height: 1.5;
  margin-bottom: var(--spacing-md);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-meta {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-md);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.meta-item i {
  width: 14px;
  font-size: 0.75rem;
}

.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-rating {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.stars {
  display: flex;
  gap: 2px;
}

.stars i {
  font-size: 0.75rem;
}

.star-filled {
  color: #FFD700;
}

.star-half {
  color: #FFD700;
}

.star-empty {
  color: var(--text-muted);
}

.rating-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.course-price {
  font-weight: 600;
}

.price-free {
  color: var(--secondary-color);
  font-size: 0.875rem;
}

.price-paid {
  color: var(--primary-color);
  font-size: 1rem;
}

.course-actions {
  position: absolute;
  top: var(--spacing-md);
  left: var(--spacing-md);
  display: flex;
  gap: var(--spacing-sm);
  opacity: 0;
  transform: translateY(-10px);
  transition: all var(--transition-normal);
}

.action-btn {
  border-radius: var(--border-radius-sm);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.9);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .course-card {
    margin: var(--spacing-sm);
    max-width: 100%;
  }
  
  .course-info {
    padding: var(--spacing-md);
  }
  
  .course-meta {
    flex-direction: column;
  }
  
  .course-actions {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>