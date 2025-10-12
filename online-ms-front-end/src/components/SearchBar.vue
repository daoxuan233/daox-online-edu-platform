<template>
  <div class="search-bar" :class="[`size-${size}`, { 'focused': isFocused }]">
    <div class="search-container neumorphism-input">
      <div class="search-icon">
        <font-awesome-icon :icon="['fas', 'search']" />
      </div>
      
      <input
        ref="searchInput"
        v-model="searchQuery"
        type="text"
        :placeholder="placeholder"
        class="search-input"
        @focus="handleFocus"
        @blur="handleBlur"
        @keyup.enter="handleSearch"
        @input="handleInput"
      >
      
      <div class="search-actions">
        <button 
          v-if="searchQuery && showClear"
          class="clear-btn"
          @click="handleClear"
        >
          <font-awesome-icon :icon="['fas', 'times']" />
        </button>
        
        <button 
          v-if="showSearchButton"
          class="search-btn neumorphism-button"
          @click="handleSearch"
        >
          <font-awesome-icon :icon="['fas', 'search']" />
        </button>
        
        <button 
          v-if="showFilterButton"
          class="filter-btn neumorphism-button"
          @click="handleFilter"
          :class="{ 'active': hasActiveFilters }"
        >
          <font-awesome-icon :icon="['fas', 'filter']" />
        </button>
      </div>
    </div>
    
    <!-- 搜索建议下拉框 -->
    <div 
      v-if="showSuggestions && suggestions.length > 0"
      class="suggestions-dropdown glass"
    >
      <div class="suggestions-header" v-if="suggestionsTitle">
        <span class="text-sm text-muted">{{ suggestionsTitle }}</span>
      </div>
      
      <ul class="suggestions-list">
        <li 
          v-for="(suggestion, index) in suggestions"
          :key="index"
          class="suggestion-item"
          :class="{ 'highlighted': index === highlightedIndex }"
          @click="handleSuggestionClick(suggestion)"
          @mouseenter="highlightedIndex = index"
        >
          <div class="suggestion-content">
            <font-awesome-icon v-if="suggestion.icon" :icon="suggestion.icon.split(' ')" class="suggestion-icon" />
            <div class="suggestion-text">
              <div class="suggestion-title">{{ suggestion.title || suggestion.text }}</div>
              <div v-if="suggestion.subtitle" class="suggestion-subtitle">{{ suggestion.subtitle }}</div>
            </div>
          </div>
          <div v-if="suggestion.category" class="suggestion-category">
            <span class="category-tag">{{ suggestion.category }}</span>
          </div>
        </li>
      </ul>
      
      <div v-if="showMoreResults" class="suggestions-footer">
        <button class="more-results-btn" @click="handleShowMore">
          <font-awesome-icon :icon="['fas', 'arrow-right']" class="mr-xs" />
          查看更多结果
        </button>
      </div>
    </div>
    
    <!-- 热门搜索标签 -->
<!--    <div v-if="showHotTags && hotTags.length > 0 && !isFocused" class="hot-tags">
      <span class="hot-tags-label">热门搜索：</span>
      <div class="tags-container">
        <button 
          v-for="tag in hotTags"
          :key="tag"
          class="hot-tag"
          @click="handleTagClick(tag)"
        >
          {{ tag }}
        </button>
      </div>
    </div>-->
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '搜索课程、资源...'
  },
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  showClear: {
    type: Boolean,
    default: true
  },
  showSearchButton: {
    type: Boolean,
    default: false
  },
  showFilterButton: {
    type: Boolean,
    default: false
  },
  hasActiveFilters: {
    type: Boolean,
    default: false
  },
  suggestions: {
    type: Array,
    default: () => []
  },
  showSuggestions: {
    type: Boolean,
    default: true
  },
  suggestionsTitle: {
    type: String,
    default: '搜索建议'
  },
  showMoreResults: {
    type: Boolean,
    default: false
  },
  hotTags: {
    type: Array,
    default: () => ['Vue.js', 'JavaScript', 'Python', 'React', '数据结构']
  },
  showHotTags: {
    type: Boolean,
    default: true
  },
  debounceTime: {
    type: Number,
    default: 300
  }
})

const emit = defineEmits([
  'update:modelValue',
  'search',
  'input',
  'focus',
  'blur',
  'clear',
  'filter',
  'suggestion-click',
  'tag-click',
  'show-more'
])

const searchInput = ref(null)
const isFocused = ref(false)
const highlightedIndex = ref(-1)
const debounceTimer = ref(null)

const searchQuery = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const handleFocus = () => {
  isFocused.value = true
  highlightedIndex.value = -1
  emit('focus')
}

const handleBlur = () => {
  // 延迟隐藏建议，以便点击建议项
  setTimeout(() => {
    isFocused.value = false
    emit('blur')
  }, 200)
}

const handleInput = (event) => {
  const value = event.target.value
  
  // 清除之前的防抖定时器
  if (debounceTimer.value) {
    clearTimeout(debounceTimer.value)
  }
  
  // 设置新的防抖定时器
  debounceTimer.value = setTimeout(() => {
    emit('input', value)
  }, props.debounceTime)
}

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    emit('search', searchQuery.value.trim())
    searchInput.value?.blur()
  }
}

const handleClear = () => {
  searchQuery.value = ''
  emit('clear')
  nextTick(() => {
    searchInput.value?.focus()
  })
}

const handleFilter = () => {
  emit('filter')
}

const handleSuggestionClick = (suggestion) => {
  searchQuery.value = suggestion.title || suggestion.text
  emit('suggestion-click', suggestion)
  handleSearch()
}

const handleTagClick = (tag) => {
  searchQuery.value = tag
  emit('tag-click', tag)
  handleSearch()
}

const handleShowMore = () => {
  emit('show-more')
}

// 键盘导航
const handleKeydown = (event) => {
  if (!props.showSuggestions || props.suggestions.length === 0) return
  
  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault()
      highlightedIndex.value = Math.min(
        highlightedIndex.value + 1,
        props.suggestions.length - 1
      )
      break
    case 'ArrowUp':
      event.preventDefault()
      highlightedIndex.value = Math.max(highlightedIndex.value - 1, -1)
      break
    case 'Enter':
      event.preventDefault()
      if (highlightedIndex.value >= 0) {
        handleSuggestionClick(props.suggestions[highlightedIndex.value])
      } else {
        handleSearch()
      }
      break
    case 'Escape':
      searchInput.value?.blur()
      break
  }
}

// 监听建议变化，重置高亮索引
watch(() => props.suggestions, () => {
  highlightedIndex.value = -1
})
</script>

<style scoped>
.search-bar {
  position: relative;
  width: 100%;
}

.search-container {
  display: flex;
  align-items: center;
  background: var(--surface-color);
  border-radius: var(--border-radius-md);
  transition: all var(--transition-fast);
  position: relative;
}

.search-bar.focused .search-container {
  box-shadow: 
    var(--neumorphism-inset-light),
    var(--neumorphism-inset-dark),
    0 0 0 2px var(--primary-color);
}

.search-icon {
  padding: 0 var(--spacing-md);
  color: var(--text-muted);
  pointer-events: none;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  color: var(--text-primary);
  font-size: 0.875rem;
}

.search-input::placeholder {
  color: var(--text-muted);
}

.search-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding-right: var(--spacing-sm);
}

.clear-btn,
.search-btn,
.filter-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  border-radius: var(--border-radius-sm);
  transition: all var(--transition-fast);
  width: 28px;
  height: 28px;
}

.clear-btn:hover {
  color: var(--text-secondary);
  background: var(--glass-background);
}

.search-btn:hover,
.filter-btn:hover {
  color: var(--primary-color);
  transform: scale(1.05);
}

.filter-btn.active {
  color: var(--primary-color);
  background: var(--glass-background);
}

/* 尺寸变体 */
.size-sm .search-container {
  height: 36px;
}

.size-md .search-container {
  height: 40px;
}

.size-lg .search-container {
  height: 48px;
}

.size-sm .search-input {
  font-size: 0.75rem;
}

.size-lg .search-input {
  font-size: 1rem;
}

/* 建议下拉框 */
.suggestions-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 1000;
  margin-top: var(--spacing-xs);
  border-radius: var(--border-radius-md);
  overflow: hidden;
  max-height: 300px;
  overflow-y: auto;
}

.suggestions-header {
  padding: var(--spacing-sm) var(--spacing-md);
  border-bottom: 1px solid var(--glass-border);
}

.suggestions-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.suggestion-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-md);
  cursor: pointer;
  transition: background var(--transition-fast);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.suggestion-item:hover,
.suggestion-item.highlighted {
  background: var(--glass-background);
}

.suggestion-item:last-child {
  border-bottom: none;
}

.suggestion-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  flex: 1;
}

.suggestion-icon {
  color: var(--text-muted);
  width: 16px;
}

.suggestion-text {
  flex: 1;
}

.suggestion-title {
  font-size: 0.875rem;
  color: var(--text-primary);
}

.suggestion-subtitle {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-top: 2px;
}

.suggestion-category {
  flex-shrink: 0;
}

.category-tag {
  background: var(--primary-color);
  color: white;
  padding: 2px 6px;
  border-radius: var(--border-radius-sm);
  font-size: 0.625rem;
  font-weight: 500;
}

.suggestions-footer {
  padding: var(--spacing-sm) var(--spacing-md);
  border-top: 1px solid var(--glass-border);
}

.more-results-btn {
  width: 100%;
  padding: var(--spacing-sm);
  border: none;
  background: transparent;
  color: var(--primary-color);
  cursor: pointer;
  border-radius: var(--border-radius-sm);
  transition: background var(--transition-fast);
  font-size: 0.875rem;
}

.more-results-btn:hover {
  background: var(--glass-background);
}

/* 热门标签 */
.hot-tags {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-sm);
  flex-wrap: wrap;
}

.hot-tags-label {
  font-size: 0.75rem;
  color: var(--text-muted);
  white-space: nowrap;
}

.tags-container {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.hot-tag {
  padding: 4px 8px;
  border: 1px solid var(--glass-border);
  background: var(--surface-color);
  color: var(--text-secondary);
  border-radius: var(--border-radius-sm);
  font-size: 0.75rem;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.hot-tag:hover {
  color: var(--primary-color);
  border-color: var(--primary-color);
  transform: translateY(-1px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-actions {
    gap: 4px;
  }
  
  .hot-tags {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .suggestions-dropdown {
    margin-top: 4px;
  }
}
</style>