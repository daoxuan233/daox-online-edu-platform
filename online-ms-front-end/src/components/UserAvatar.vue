<template>
  <div
      class="user-avatar"
      :class="[
      `size-${size}`,
      { 'clickable': clickable, 'online': showStatus && isOnline }
    ]"
      @click="handleClick"
  >
    <div class="avatar-container">
      <img
          v-if="src"
          :src="src"
          :alt="name || 'User Avatar'"
          class="avatar-image"
          @error="handleImageError"
      >
      <div v-else class="avatar-placeholder">
        <span class="avatar-text">{{ avatarText }}</span>
      </div>

      <!-- 在线状态指示器 -->
      <div
          v-if="showStatus"
          class="status-indicator"
          :class="{ 'online': isOnline, 'offline': !isOnline }"
      ></div>

      <!-- 角色徽章 -->
<!--      <div v-if="role" class="role-badge">
        <font-awesome-icon :icon="roleIcon.split(' ')"/>
      </div>-->
    </div>

    <!-- 用户信息 -->
    <div v-if="showInfo" class="user-info">
      <div class="user-name">{{ name }}</div>
      <div v-if="title" class="user-title">{{ title }}</div>
    </div>
  </div>
</template>

<script setup>
import {computed, ref} from 'vue'

const props = defineProps({
  src: {
    type: String,
    default: ''
  },
  name: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['xs', 'sm', 'md', 'lg', 'xl'].includes(value)
  },
  role: {
    type: String,
    default: '',
    validator: (value) => ['', 'student', 'teacher', 'admin'].includes(value)
  },
  showStatus: {
    type: Boolean,
    default: false
  },
  isOnline: {
    type: Boolean,
    default: false
  },
  showInfo: {
    type: Boolean,
    default: false
  },
  clickable: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

const imageError = ref(false)

// 计算头像文字（取姓名首字母）
const avatarText = computed(() => {
  if (!props.name) return '?'
  const names = props.name.split(' ')
  if (names.length >= 2) {
    return (names[0][0] + names[1][0]).toUpperCase()
  }
  return props.name.slice(0, 2).toUpperCase()
})

// 角色图标映射
const roleIcon = computed(() => {
  const icons = {
    student: 'fas fa-user-graduate',
    teacher: 'fas fa-chalkboard-teacher',
    admin: 'fas fa-user-shield'
  }
  return icons[props.role] || ''
})

const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}

const handleImageError = () => {
  imageError.value = true
}
</script>

<style scoped>
/* CSS变量定义 */
:root {
  --neumorphism-bg: #f0f0f3;
  --neumorphism-shadow-dark: #d1d1d4;
  --neumorphism-shadow-light: #ffffff;
  --primary-color: #002FA7;
  --secondary-color: #517B4D;
  --text-primary: #303133;
  --text-secondary: #606266;
  --success-color: #67C23A;
  --border-radius: 16px;
  --transition-smooth: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-avatar {
  display: inline-flex;
  align-items: center;
  gap: 35px;
  background: var(--neumorphism-bg);
  box-shadow: 5px 5px 16px var(--neumorphism-shadow-dark),
  -5px -5px 16px var(--neumorphism-shadow-light);
  transition: var(--transition-smooth);
  position: relative;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 1rem;
  width: 300px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 磨砂玻璃效果叠加 */
.user-avatar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: var(--border-radius);
  pointer-events: none;
}

.user-avatar.clickable {
  cursor: pointer;
}

.user-avatar.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 4px 4px 8px var(--neumorphism-shadow-dark),
  -4px -4px 8px var(--neumorphism-shadow-light),
  0 8px 24px rgba(0, 47, 167, 0.15);
}

.user-avatar.clickable:active {
  transform: translateY(0);
  box-shadow: inset 4px 4px 8px var(--neumorphism-shadow-dark),
  inset -4px -4px 8px var(--neumorphism-shadow-light);
}

.avatar-container {
  position: relative;
  border-radius: 100%;
  overflow: hidden;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  box-shadow: 4px 4px 8px var(--neumorphism-shadow-dark),
  -4px -4px 8px var(--neumorphism-shadow-light);
  z-index: 1;
}

/* 尺寸变体 */
.size-xs .avatar-container {
  width: 24px;
  height: 24px;
}

.size-sm .avatar-container {
  width: 32px;
  height: 32px;
}

.size-md .avatar-container {
  width: 40px;
  height: 40px;
}

.size-lg .avatar-container {
  width: 56px;
  height: 56px;
}

.size-xl .avatar-container {
  width: 80px;
  height: 80px;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  font-weight: 600;
  position: relative;
  overflow: hidden;
}

.avatar-placeholder::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
}

.size-xs .avatar-text {
  font-size: 0.625rem;
}

.size-sm .avatar-text {
  font-size: 0.75rem;
}

.size-md .avatar-text {
  font-size: 0.875rem;
}

.size-lg .avatar-text {
  font-size: 1rem;
}

.size-xl .avatar-text {
  font-size: 1.25rem;
}

.status-indicator {
  position: absolute;
  bottom: -2px;
  right: -2px;
  border: 3px solid var(--neumorphism-bg);
  border-radius: 50%;
  background: #F56C6C;
  box-shadow: 2px 2px 4px var(--neumorphism-shadow-dark),
  -1px -1px 2px var(--neumorphism-shadow-light);
  z-index: 2;
}

.size-xs .status-indicator {
  width: 8px;
  height: 8px;
}

.size-sm .status-indicator {
  width: 10px;
  height: 10px;
}

.size-md .status-indicator {
  width: 12px;
  height: 12px;
}

.size-lg .status-indicator {
  width: 16px;
  height: 16px;
}

.size-xl .status-indicator {
  width: 20px;
  height: 20px;
}

.status-indicator.online {
  background: linear-gradient(135deg, var(--success-color), #85ce61);
  animation: pulse 2s infinite;
  box-shadow: 2px 2px 4px var(--neumorphism-shadow-dark),
  -1px -1px 2px var(--neumorphism-shadow-light),
  0 0 8px rgba(103, 194, 58, 0.4);
}

.status-indicator.offline {
  background: #909399;
}

.role-badge {
  position: absolute;
  top: -4px;
  left: -4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--neumorphism-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.625rem;
  box-shadow: 3px 3px 6px var(--neumorphism-shadow-dark),
  -2px -2px 4px var(--neumorphism-shadow-light);
  border: 2px solid rgba(255, 255, 255, 0.8);
  z-index: 2;
}

.size-xs .role-badge,
.size-sm .role-badge {
  display: none;
}

.size-lg .role-badge,
.size-xl .role-badge {
  width: 20px;
  height: 20px;
  font-size: 0.75rem;
}

.role-badge .fas {
  color: var(--primary-color);
  filter: drop-shadow(0 1px 2px rgba(0, 47, 167, 0.3));
}

.user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
  z-index: 1;
  position: relative;
}

.user-name {
  color: var(--success-color);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 10px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  letter-spacing: 0.02em;
  font-size: 1.5rem; /* Adjusted font size */
}

.user-title {
  font-size: 0.95rem;
  color: var(--secondary-color);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 400;
  opacity: 0.8;
  letter-spacing: 0.01em;
}

.size-xs .user-name {
  font-size: 0.75rem;
}

.size-sm .user-name {
  font-size: 0.875rem;
}

.size-md .user-name {
  font-size: 0.875rem;
}

.size-lg .user-name {
  font-size: 1rem;
}

.size-xl .user-name {
  font-size: 1.125rem;
}

@keyframes pulse {
  0% {
    box-shadow: 2px 2px 4px var(--neumorphism-shadow-dark),
    -1px -1px 2px var(--neumorphism-shadow-light),
    0 0 0 0 rgba(103, 194, 58, 0.7);
  }
  50% {
    box-shadow: 2px 2px 4px var(--neumorphism-shadow-dark),
    -1px -1px 2px var(--neumorphism-shadow-light),
    0 0 0 4px rgba(103, 194, 58, 0.3);
  }
  100% {
    box-shadow: 2px 2px 4px var(--neumorphism-shadow-dark),
    -1px -1px 2px var(--neumorphism-shadow-light),
    0 0 0 0 rgba(103, 194, 58, 0);
  }
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* 悬浮时的微光效果 */
.user-avatar:hover::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
      90deg,
      transparent,
      rgba(255, 255, 255, 0.2),
      transparent
  );
  background-size: 200% 100%;
  animation: shimmer 2s ease-in-out;
  border-radius: var(--border-radius);
  pointer-events: none;
  z-index: 3;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .user-avatar {
    padding: 8px;
    gap: 12px;
    border-radius: 12px;
  }

  .user-info {
    display: none;
  }

  .size-xl .avatar-container {
    width: 56px;
    height: 56px;
  }

  .size-lg .avatar-container {
    width: 48px;
    height: 48px;
  }

  .role-badge {
    width: 16px;
    height: 16px;
    top: -2px;
    left: -2px;
  }
}

@media (max-width: 480px) {
  .user-avatar {
    padding: 6px;
    gap: 8px;
    border-radius: 10px;
  }

  .size-lg .avatar-container,
  .size-xl .avatar-container {
    width: 40px;
    height: 40px;
  }
}

/* 深色主题支持 */
@media (prefers-color-scheme: dark) {
  :root {
    --neumorphism-bg: #2c2c2c;
    --neumorphism-shadow-dark: #1a1a1a;
    --neumorphism-shadow-light: #3e3e3e;
    --text-primary: #ffffff;
    --text-secondary: #cccccc;
  }

  .user-avatar::before {
    background: rgba(0, 0, 0, 0.25);
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .avatar-placeholder::before {
    background: rgba(0, 0, 0, 0.1);
  }
}

/* 高对比度模式支持 */
@media (prefers-contrast: high) {
  .user-avatar {
    border: 2px solid var(--text-primary);
  }

  .status-indicator {
    border-width: 4px;
  }

  .role-badge {
    border-width: 3px;
    border-color: var(--text-primary);
  }
}

/* 减少动画偏好支持 */
@media (prefers-reduced-motion: reduce) {
  .user-avatar,
  .user-avatar.clickable:hover,
  .user-avatar.clickable:active,
  .status-indicator.online {
    transition: none;
    animation: none;
  }

  .user-avatar:hover::after {
    animation: none;
  }
}
</style>