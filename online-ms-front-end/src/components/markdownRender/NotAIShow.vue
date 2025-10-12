<template>
  <div class="markdown-wrapper">
    <div class="markdown-container" ref="markdownRef">
      <div v-html="html" class="markdown-content"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted } from "vue";
import markdownIt from "markdown-it";

const props = defineProps({
  content: {
    type: String,
    default: ""
  }
});

const html = ref("");
const markdownRef = ref(null);
const showCopyOptions = ref(false);
const codeTheme = ref('light'); // 代码主题状态
import highlightjs from "highlight.js";
import 'highlight.js/styles/github.css'
import markdownItHighlightjs from "markdown-it-highlightjs";

const md = new markdownIt({
  html: true,
  linkify: true,
  typographer: true,
  xhtmlOut: true,
  langPrefix: "language-",
  breaks: false, // 禁用自动换行以解决过度换行问题
});

// 使用 markdown-it-highlightjs 插件来为代码块提供语法高亮
md.use(markdownItHighlightjs, {
  // 传入 highlight.js 库的实例
  highlightjs,
  // 自动检测代码块的编程语言
  auto: true,
  // 启用对整个代码块（<pre><code>...</code></pre>）的高亮
  code: true,
  // 对行内代码（`...`）也尝试进行高亮
  inline: true,
  // 显式启用行内代码高亮
  inlineCode: true,
  // 显式启用块级代码高亮
  block: true,
})

// 自定义段落渲染规则，优化间距
md.renderer.rules.paragraph_open = function(tokens, idx, options, env, renderer) {
  return '<p class="md-paragraph">';
};

// 自定义列表项渲染规则
md.renderer.rules.list_item_open = function(tokens, idx, options, env, renderer) {
  return '<li class="md-list-item">';
};

// 复制功能
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text);
    // 这里可以添加成功提示
  } catch (err) {
    // 降级方案
    const textArea = document.createElement('textarea');
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.select();
    document.execCommand('copy');
    document.body.removeChild(textArea);
  }
};

// 代码块复制功能
const copyCodeBlock = (code) => {
  copyToClipboard(code);
};

// 切换代码主题
const toggleCodeTheme = () => {
  codeTheme.value = codeTheme.value === 'light' ? 'dark' : 'light';
  updateCodeBlocks();
};

// 更新代码块
const updateCodeBlocks = () => {
  if (!markdownRef.value) return;

  const preElements = markdownRef.value.querySelectorAll('pre');
  preElements.forEach((pre, index) => {
    if (pre.querySelector('.code-header')) return; // 已经处理过的跳过

    const code = pre.querySelector('code');
    if (!code) return;

    const codeText = code.textContent || '';
    const language = getLanguageFromClass(code.className) || 'text';

    // 创建代码块头部
    const header = document.createElement('div');
    header.className = 'code-header';
    header.innerHTML = `
      <div class="code-info">
        <span class="language-tag">${language}</span>
      </div>
      <div class="code-actions">
        <button class="code-action-btn theme-btn" title="切换主题">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12,18C11.11,18 10.26,17.8 9.5,17.46C11.56,16.06 13,13.72 13,11A6,6 0 0,0 7,5C7,3.89 7.89,3 9,3A9,9 0 0,1 18,12C18,15.31 15.31,18 12,18Z"/>
          </svg>
        </button>
        <button class="code-action-btn fold-btn" title="折叠/展开">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
            <path d="M7.41,8.58L12,13.17L16.59,8.58L18,10L12,16L6,10L7.41,8.58Z"/>
          </svg>
        </button>
        <button class="code-action-btn copy-btn" title="复制代码">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
            <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"/>
          </svg>
        </button>
      </div>
    `;

    // 添加事件监听器
    const copyBtn = header.querySelector('.copy-btn');
    const themeBtn = header.querySelector('.theme-btn');
    const foldBtn = header.querySelector('.fold-btn');

    copyBtn.addEventListener('click', () => copyCodeBlock(codeText));
    themeBtn.addEventListener('click', () => {
      pre.classList.toggle('dark-theme');
    });
    foldBtn.addEventListener('click', () => {
      code.style.display = code.style.display === 'none' ? 'block' : 'none';
      foldBtn.classList.toggle('folded');
    });

    // 插入头部
    pre.insertBefore(header, code);

    // 应用主题
    if (codeTheme.value === 'dark') {
      pre.classList.add('dark-theme');
    }
  });
};

// 从类名中提取语言
const getLanguageFromClass = (className) => {
  const match = className.match(/language-([\w-]+)/);
  return match ? match[1] : null;
};
// 监听内容变化
watch(() => props.content, (newVal) => {
  if (newVal) {
    html.value = md.render(newVal);
    nextTick(() => {
      updateCodeBlocks();
    });
  }
}, { immediate: true });

// 点击外部关闭复制选项
onMounted(() => {
  document.addEventListener('click', (e) => {
    if (!e.target.closest('.copy-dropdown')) {
      showCopyOptions.value = false;
    }
  });
});
</script>

<style scoped>

/* 新拟态设计基础样式 */
.markdown-wrapper {
  position: relative;
}

.markdown-container {
  background: #f0f0f3;
  border-radius: 12px;
  padding: 20px;
  box-shadow:
      8px 8px 16px #d1d1d4,
      -8px -8px 16px #ffffff;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.4;
  color: #303133;
}

.markdown-content {
  word-wrap: break-word;
  overflow-wrap: break-word;
}

/* 标题样式 */
.markdown-container :deep(h1),
.markdown-container :deep(h2),
.markdown-container :deep(h3),
.markdown-container :deep(h4),
.markdown-container :deep(h5),
.markdown-container :deep(h6) {
  color: #002FA7;
  font-weight: 600;
  margin: 1em 0 0.5em 0;
  line-height: 1.0;
}

.markdown-container :deep(h1) {
  font-size: 2em;
  border-bottom: 2px solid #517B4D;
  padding-bottom: 0.5em;
}

.markdown-container :deep(h2) {
  font-size: 1.6em;
  border-bottom: 1px solid #DCDFE6;
  padding-bottom: 0.3em;
}

.markdown-container :deep(h3) {
  font-size: 1.3em;
}

.markdown-container :deep(h4) {
  font-size: 1.1em;
}

.markdown-container :deep(h5) {
  font-size: 1.05em;
}

.markdown-container :deep(h6) {
  font-size: 1em;
}

/* 标题与其他元素的间距优化 */
.markdown-container :deep(h1 + p),
.markdown-container :deep(h2 + p),
.markdown-container :deep(h3 + p),
.markdown-container :deep(h4 + p),
.markdown-container :deep(h5 + p),
.markdown-container :deep(h6 + p) {
  margin-top: 0.3em;
}

.markdown-container :deep(h1 + ul),
.markdown-container :deep(h2 + ul),
.markdown-container :deep(h3 + ul),
.markdown-container :deep(h4 + ul),
.markdown-container :deep(h5 + ul),
.markdown-container :deep(h6 + ul),
.markdown-container :deep(h1 + ol),
.markdown-container :deep(h2 + ol),
.markdown-container :deep(h3 + ol),
.markdown-container :deep(h4 + ol),
.markdown-container :deep(h5 + ol),
.markdown-container :deep(h6 + ol) {
  margin-top: 0.4em;
}

/* 段落样式 - 优化换行 */
.markdown-container :deep(p) {
  margin: 0.5em 0;
  color: #606266;
  line-height: 1.5;
}

.markdown-container :deep(p:first-child) {
  margin-top: 0;
}

.markdown-container :deep(p:last-child) {
  margin-bottom: 0;
}

/* 连续段落间距优化 */
.markdown-container :deep(p + p) {
  margin-top: 0.3em;
}

/* 自定义段落样式 */
.markdown-container :deep(.md-paragraph) {
  margin: 0.4em 0;
  line-height: 1.45;
}

.markdown-container :deep(.md-paragraph:first-child) {
  margin-top: 0;
}

.markdown-container :deep(.md-paragraph:last-child) {
  margin-bottom: 0;
}

/* 链接样式 */
.markdown-container :deep(a) {
  color: #409EFF;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.3s ease;
}

.markdown-container :deep(a:hover) {
  color: #002FA7;
  border-bottom-color: #409EFF;
}

/* 列表样式 */
.markdown-container :deep(ul),
.markdown-container :deep(ol) {
  margin: 0.8em 0;
  padding-left: 1.8em;
}

.markdown-container :deep(li) {
  margin: 0.2em 0;
  color: #606266;
  line-height: 1.4;
}

.markdown-container :deep(li::marker) {
  color: #517B4D;
}

/* 嵌套列表间距优化 */
.markdown-container :deep(li > ul),
.markdown-container :deep(li > ol) {
  margin: 0.3em 0;
}

/* 列表项内段落优化 */
.markdown-container :deep(li p) {
  margin: 0.2em 0;
}

/* 自定义列表项样式 */
.markdown-container :deep(.md-list-item) {
  margin: 0.15em 0;
  line-height: 1.35;
}

.markdown-container :deep(.md-list-item:first-child) {
  margin-top: 0;
}

.markdown-container :deep(.md-list-item:last-child) {
  margin-bottom: 0;
}

/* 引用块样式 */
.markdown-container :deep(blockquote) {
  margin: 1em 0;
  padding: 0.8em 1.2em;
  background: rgba(81, 123, 77, 0.1);
  border-left: 4px solid #517B4D;
  border-radius: 0 8px 8px 0;
  box-shadow:
      inset 4px 4px 8px rgba(209, 209, 212, 0.3),
      inset -4px -4px 8px rgba(255, 255, 255, 0.3);
}

.markdown-container :deep(blockquote p) {
  margin: 0;
  color: #517B4D;
  font-style: italic;
}

/* 代码块样式 - 新拟态设计 */
.markdown-container :deep(pre) {
  background: #f0f0f3;
  border-radius: 12px;
  padding: 0;
  margin: 1em 0;
  overflow: hidden;
  box-shadow:
      inset 8px 8px 16px #d1d1d4,
      inset -8px -8px 16px #ffffff;
  border: 1px solid rgba(220, 223, 230, 0.5);
  position: relative;
}

/* 代码块头部 */
.markdown-container :deep(.code-header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: linear-gradient(145deg, #e8e8eb 0%, #f0f0f3 100%);
  border-bottom: 1px solid rgba(220, 223, 230, 0.5);
  font-size: 12px;
}

.markdown-container :deep(.code-info) {
  display: flex;
  align-items: center;
  gap: 8px;
}

.markdown-container :deep(.language-tag) {
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
  font-size: 11px;
  text-transform: uppercase;
}

.markdown-container :deep(.code-actions) {
  display: flex;
  gap: 4px;
}

.markdown-container :deep(.code-action-btn) {
  background: transparent;
  border: none;
  padding: 4px;
  border-radius: 4px;
  cursor: pointer;
  color: #606266;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.markdown-container :deep(.code-action-btn:hover) {
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

.markdown-container :deep(.code-action-btn.folded) {
  transform: rotate(180deg);
}

/* 代码内容区域 */
.markdown-container :deep(pre code) {
  display: block;
  padding: 1.5em;
  overflow-x: auto;
  background: transparent;
  color: #303133;
  border-radius: 0;
  box-shadow: none;
}

/* 深色主题代码块 */
.markdown-container :deep(pre.dark-theme) {
  background: #2d3748;
  box-shadow:
      inset 8px 8px 16px #1a202c,
      inset -8px -8px 16px #4a5568;
}

.markdown-container :deep(pre.dark-theme .code-header) {
  background: linear-gradient(145deg, #2d3748 0%, #4a5568 100%);
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

.markdown-container :deep(pre.dark-theme .language-tag) {
  background: rgba(129, 140, 248, 0.2);
  color: #818cf8;
}

.markdown-container :deep(pre.dark-theme .code-action-btn) {
  color: #a0aec0;
}

.markdown-container :deep(pre.dark-theme .code-action-btn:hover) {
  background: rgba(129, 140, 248, 0.2);
  color: #818cf8;
}

.markdown-container :deep(pre.dark-theme code) {
  color: #e2e8f0;
}

.markdown-container :deep(code) {
  font-family: 'Fira Code', 'Monaco', 'Consolas', 'Ubuntu Mono', monospace;
  font-size: 0.9em;
}

/* 行内代码样式 */
.markdown-container :deep(p code),
.markdown-container :deep(li code),
.markdown-container :deep(td code) {
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
  padding: 0.2em 0.4em;
  border-radius: 6px;
  font-weight: 500;
  box-shadow:
      inset 2px 2px 4px rgba(209, 209, 212, 0.3),
      inset -2px -2px 4px rgba(255, 255, 255, 0.7);
}

/* 底部复制功能样式 */
.copy-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.copy-dropdown {
  position: relative;
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f0f0f3;
  border: none;
  border-radius: 8px;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow:
      4px 4px 8px #d1d1d4,
      -4px -4px 8px #ffffff;
}

.copy-btn:hover {
  color: #409EFF;
  box-shadow:
      2px 2px 4px #d1d1d4,
      -2px -2px 4px #ffffff;
}

.copy-dropdown.active .copy-btn {
  box-shadow:
      inset 2px 2px 4px #d1d1d4,
      inset -2px -2px 4px #ffffff;
}

.copy-options {
  position: absolute;
  bottom: 100%;
  right: 0;
  margin-bottom: 8px;
  background: #f0f0f3;
  border-radius: 8px;
  padding: 8px;
  box-shadow:
      8px 8px 16px #d1d1d4,
      -8px -8px 16px #ffffff;
  min-width: 120px;
  z-index: 1000;
}

.copy-option {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 12px;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: #606266;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.copy-option:hover {
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

.copy-option svg {
  flex-shrink: 0;
}

/* 表格样式 */
.markdown-container :deep(table) {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  margin: 1em 0;
  background: #f0f0f3;
  border-radius: 12px;
  overflow: hidden;
  box-shadow:
      8px 8px 16px #d1d1d4,
      -8px -8px 16px #ffffff;
}

.markdown-container :deep(th),
.markdown-container :deep(td) {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid rgba(220, 223, 230, 0.5);
  line-height: 1.3;
}

.markdown-container :deep(th) {
  background: linear-gradient(145deg, #e8e8eb 0%, #f0f0f3 100%);
  color: #002FA7;
  font-weight: 600;
}

.markdown-container :deep(tr:last-child td) {
  border-bottom: none;
}

.markdown-container :deep(tr:hover) {
  background: rgba(64, 158, 255, 0.05);
}

/* 分割线样式 */
.markdown-container :deep(hr) {
  border: none;
  height: 2px;
  background: linear-gradient(90deg, transparent, #DCDFE6, transparent);
  margin: 1.5em 0;
}

/* 强调文本样式 */
.markdown-container :deep(strong) {
  color: #002FA7;
  font-weight: 600;
}

.markdown-container :deep(em) {
  color: #517B4D;
  font-style: italic;
}

/* 删除线样式 */
.markdown-container :deep(del) {
  color: #909399;
  text-decoration: line-through;
}

/* 图片样式 */
.markdown-container :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 0.8em 0;
  box-shadow:
      4px 4px 8px rgba(209, 209, 212, 0.5),
      -4px -4px 8px rgba(255, 255, 255, 0.5);
  transition: transform 0.3s ease;
}

.markdown-container :deep(img:hover) {
  transform: scale(1.02);
}

/* 键盘按键样式 */
.markdown-container :deep(kbd) {
  background: #f0f0f3;
  border-radius: 6px;
  padding: 0.2em 0.5em;
  font-family: monospace;
  font-size: 0.9em;
  color: #303133;
  box-shadow:
      2px 2px 4px #d1d1d4,
      -2px -2px 4px #ffffff,
      inset 1px 1px 2px rgba(209, 209, 212, 0.3);
}

/* 标记高亮样式 */
.markdown-container :deep(mark) {
  background: linear-gradient(120deg, rgba(255, 230, 109, 0.8) 0%, rgba(255, 230, 109, 0.3) 100%);
  color: #303133;
  padding: 0.1em 0.3em;
  border-radius: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .markdown-container {
    padding: 16px;
    margin: 0 8px;
  }

  .markdown-container :deep(h1) {
    font-size: 1.6em;
  }

  .markdown-container :deep(h2) {
    font-size: 1.4em;
  }

  .markdown-container :deep(pre) {
    padding: 1em;
    font-size: 0.85em;
  }

  .markdown-container :deep(table) {
    font-size: 0.9em;
  }

  .markdown-container :deep(th),
  .markdown-container :deep(td) {
    padding: 8px 12px;
  }
}

/* 深色主题支持 */
@media (prefers-color-scheme: dark) {
  .markdown-container {
    background: #2c2c2c;
    color: #e4e4e7;
    box-shadow:
        8px 8px 16px #1a1a1a,
        -8px -8px 16px #3e3e3e;
  }

  .markdown-container :deep(h1),
  .markdown-container :deep(h2),
  .markdown-container :deep(h3),
  .markdown-container :deep(h4),
  .markdown-container :deep(h5),
  .markdown-container :deep(h6) {
    color: #60a5fa;
  }

  .markdown-container :deep(p),
  .markdown-container :deep(li) {
    color: #d1d5db;
  }

  .markdown-container :deep(pre) {
    background: #2c2c2c;
    box-shadow:
        inset 8px 8px 16px #1a1a1a,
        inset -8px -8px 16px #3e3e3e;
  }

  .markdown-container :deep(table) {
    background: #2c2c2c;
    box-shadow:
        8px 8px 16px #1a1a1a,
        -8px -8px 16px #3e3e3e;
  }

  .markdown-container :deep(th) {
    background: linear-gradient(145deg, #1a1a1a 0%, #2c2c2c 100%);
    color: #60a5fa;
  }

  /* 深色主题下的复制按钮 */
  .copy-btn {
    background: #2c2c2c;
    color: #d1d5db;
    box-shadow:
        4px 4px 8px #1a1a1a,
        -4px -4px 8px #3e3e3e;
  }

  .copy-btn:hover {
    color: #60a5fa;
    box-shadow:
        2px 2px 4px #1a1a1a,
        -2px -2px 4px #3e3e3e;
  }

  .copy-dropdown.active .copy-btn {
    box-shadow:
        inset 2px 2px 4px #1a1a1a,
        inset -2px -2px 4px #3e3e3e;
  }

  .copy-options {
    background: #2c2c2c;
    box-shadow:
        8px 8px 16px #1a1a1a,
        -8px -8px 16px #3e3e3e;
  }

  .copy-option {
    color: #d1d5db;
  }

  .copy-option:hover {
    background: rgba(96, 165, 250, 0.1);
    color: #60a5fa;
  }
}
</style>