<template>
  <div class="profile-nav glass-card">
    <div class="nav-list">
      <div 
        v-for="item in navItems" 
        :key="item.key"
        class="nav-item"
        :class="{ active: activeTab === item.key }"
        @click="$emit('tab-change', item.key)"
      >
        <font-awesome-icon :icon="['fas', item.icon]" />
        <span>{{ item.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

// Props
defineProps({
  activeTab: {
    type: String,
    required: true
  }
})

// Emits
defineEmits(['tab-change'])

// 导航项
const navItems = [
  { key: 'basic', label: '基本信息', icon: 'user' },
  { key: 'preferences', label: '学习偏好', icon: 'cog' },
  { key: 'security', label: '安全设置', icon: 'shield-alt' },
  { key: 'privacy', label: '隐私设置', icon: 'user-secret' }
]
</script>

<style scoped>
.profile-nav {
  width: 280px;
  background: rgba(240, 240, 243, 0.25);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 24px;
  padding: 0;
  height: fit-content;
  box-shadow: 
    8px 8px 32px rgba(0, 47, 167, 0.1),
    inset 1px 1px 0 rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.profile-nav::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, 
    rgba(0, 47, 167, 0.02) 0%, 
    rgba(81, 123, 77, 0.01) 50%, 
    rgba(103, 194, 58, 0.02) 100%);
  pointer-events: none;
}

.nav-list {
  display: flex;
  flex-direction: column;
  padding: 1.5rem 0;
  position: relative;
  z-index: 1;
  list-style: none;
  margin: 0;
}

.nav-item {
  margin: 0.5rem 1.25rem;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.25rem;
  cursor: pointer;
  color: #4a5568;
  font-weight: 500;
  letter-spacing: 0.3px;
  position: relative;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(135deg, #002FA7 0%, #517B4D 100%);
  transform: scaleY(0);
  transition: transform 0.3s ease;
  border-radius: 0 2px 2px 0;
}

.nav-item:hover {
  background: rgba(0, 47, 167, 0.08);
  color: #002FA7;
  transform: translateX(8px);
  box-shadow: 
    8px 8px 16px rgba(0, 47, 167, 0.1),
    -4px -4px 8px rgba(255, 255, 255, 0.3);
}

.nav-item:hover::before {
  transform: scaleY(1);
}

.nav-item:hover svg {
  color: #002FA7;
  transform: scale(1.1);
}

.nav-item.active {
  background: linear-gradient(135deg, rgba(0, 47, 167, 0.15) 0%, rgba(81, 123, 77, 0.1) 100%);
  color: #002FA7;
  font-weight: 600;
  box-shadow: 
    inset 4px 4px 8px rgba(0, 47, 167, 0.1),
    inset -2px -2px 4px rgba(255, 255, 255, 0.2),
    0 4px 15px rgba(0, 47, 167, 0.2);
}

.nav-item.active::before {
  transform: scaleY(1);
}

.nav-item.active svg {
  color: #002FA7;
  transform: scale(1.1);
}

.nav-item.active span {
  color: #002FA7;
  font-weight: 600;
}

.nav-item svg {
  width: 24px;
  height: 24px;
  font-size: 1.125rem;
  transition: all 0.3s ease;
  min-width: 24px;
}

.nav-item span {
  font-weight: inherit;
  font-size: 0.95rem;
  transition: all 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .profile-nav {
    width: 100%;
    margin-bottom: 1.5rem;
  }
  
  .nav-list {
    flex-direction: row;
    overflow-x: auto;
    padding: 1rem;
    gap: 0.5rem;
  }
  
  .nav-item {
    min-width: 140px;
    justify-content: center;
    flex-direction: column;
    gap: 0.5rem;
    padding: 1rem;
    margin: 0;
    white-space: nowrap;
  }
  
  .nav-item:hover {
    transform: translateY(-2px);
  }
  
  .nav-item span {
    font-size: 0.875rem;
  }
}

@media (max-width: 768px) {
  .nav-item {
    min-width: 120px;
    padding: 0.75rem;
  }
  
  .nav-item span {
    font-size: 0.8rem;
  }
  
  .nav-item svg {
    width: 20px;
    height: 20px;
    font-size: 1rem;
  }
}
</style>