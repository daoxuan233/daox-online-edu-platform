<template>
  <div class="markdown-wrapper">
    <!-- 主内容渲染区 -->
    <div
        class="markdown-container"
        ref="markdownRef"
        v-html="renderedHtml"
    ></div>

    <!-- 底部功能区：复制与导出 -->
    <div class="copy-actions">
      <!-- 导出笔记按钮 -->
      <button @click="handleExportNote" class="copy-btn export-btn" :disabled="isExporting">
        <span class="icon-wrapper">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14z"/>
            <path d="M7 12h2v5H7zm4-3h2v8h-2zm4-3h2v11h-2z"/>
          </svg>
          <span class="btn-text">{{ isExporting ? '解析中...' : '导出笔记' }}</span>
        </span>
      </button>

      <div class="copy-dropdown" :class="{ active: showCopyOptions }" ref="dropdownRef">
        <!-- 主复制按钮 -->
        <button @click="toggleCopyOptions" class="copy-btn main-btn">
          <span v-if="mainCopyStatus === 'success'" class="icon-wrapper success">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
            </svg>
            <span class="btn-text">已复制</span>
          </span>
          <span v-else class="icon-wrapper">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"/>
            </svg>
            <span class="btn-text">复制内容</span>
          </span>
        </button>

        <!-- 下拉菜单 -->
        <div class="copy-options" v-show="showCopyOptions">
          <button @click="handleMainCopy('markdown')" class="copy-option">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M22.46 6c-.77.35-1.6.58-2.46.69.88-.53 1.56-1.37 1.88-2.38-.83.5-1.75.85-2.72 1.05C18.37 4.5 17.26 4 16 4c-2.35 0-4.27 1.92-4.27 4.29 0 .34.04.67.11.98C8.28 9.09 5.11 7.38 3 4.79c-.37.63-.58 1.37-.58 2.15 0 1.49.75 2.81 1.91 3.56-.71 0-1.37-.2-1.95-.5v.03c0 2.08 1.48 3.82 3.44 3.95-.36.1-.74.15-1.13.15-.27 0-.54-.03-.8-.08.54 1.69 2.11 2.95 4 2.98-1.46 1.16-3.31 1.84-5.33 1.84-.35 0-.69-.02-1.02-.06C3.44 20.29 5.7 21 8.12 21 16 21 20.33 14.46 20.33 8.79c0-.19 0-.37-.01-.56.84-.6 1.56-1.36 2.14-2.23z"/></svg>
            Markdown
          </button>
          <button @click="handleMainCopy('html')" class="copy-option">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
            HTML
          </button>
          <button @click="handleMainCopy('text')" class="copy-option">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M14,17H7V15H14M17,13H7V11H17M17,9H7V7H17M19,3H5C3.89,3 3,3.89 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V5C0,3.89 20.11,3 19,3Z"/></svg>
            纯文本
          </button>
        </div>
      </div>
    </div>

    <!-- 导出笔记弹窗 -->
    <el-dialog
      v-model="showNoteDialog"
      title="导出为学习笔记"
      width="600px"
      append-to-body
      class="note-export-dialog"
    >
      <el-form :model="noteForm" label-position="top">
        <el-form-item label="笔记标题" required>
          <el-input v-model="noteForm.title" placeholder="请输入笔记标题" maxlength="50" show-word-limit />
        </el-form-item>
        
        <el-form-item label="标签">
          <div class="tags-wrapper">
            <el-tag
              v-for="tag in noteForm.tags"
              :key="tag"
              closable
              :disable-transitions="false"
              @close="handleCloseTag(tag)"
              class="note-tag"
            >
              {{ tag }}
            </el-tag>
            <el-input
              v-if="inputVisible"
              ref="inputRef"
              v-model="inputValue"
              class="tag-input-new"
              size="small"
              @keyup.enter="handleInputConfirm"
              @blur="handleInputConfirm"
            />
            <el-button v-else class="button-new-tag" size="small" @click="showInput">
              + New Tag
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="私密笔记">
          <el-switch v-model="noteForm.isPrivate" active-text="仅自己可见" inactive-text="公开" />
        </el-form-item>

        <el-form-item label="笔记内容" required>
          <el-input
            v-model="noteForm.content"
            type="textarea"
            :rows="10"
            placeholder="笔记内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showNoteDialog = false">取消</el-button>
          <el-button type="primary" @click="submitNote" :loading="noteLoading">保存笔记</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted, watch, reactive } from "vue";
import MarkdownIt from "markdown-it";
import hljs from "highlight.js";
import "highlight.js/styles/github.css"; // 引入代码高亮样式
import { parseTag } from "@/api/students/AIChatAPI.js";
import { createFreedomNote } from "@/api/students/stuAPI.js";
import { ElMessage } from "element-plus";

// === KaTeX 核心 ===
import katex from "katex";
import "katex/dist/katex.min.css";
import markdownItKatex from "@iktakahiro/markdown-it-katex";

const props = defineProps({
  content: { type: String, default: "" },
  recordId: { type: String, default: null }
});

const markdownRef = ref(null);
const dropdownRef = ref(null);
const showCopyOptions = ref(false);
const mainCopyStatus = ref('default');

// ==========================================
// 1. 初始化 Markdown-it 引擎 (修复高亮逻辑)
// ==========================================
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: false,
  highlight: function (str, lang) {
    let highlighted = '';
    let languageClass = '';

    // 1. 尝试使用指定语言高亮
    if (lang && hljs.getLanguage(lang)) {
      try {
        highlighted = hljs.highlight(str, { language: lang, ignoreIllegals: true }).value;
        languageClass = `language-${lang}`;
      } catch (__) {
        highlighted = md.utils.escapeHtml(str);
      }
    }
    // 2. 自动检测语言 (如果未指定语言或语言无效)
    else {
      try {
        const result = hljs.highlightAuto(str);
        highlighted = result.value;
        // 如果检测到了语言，添加对应的类名
        languageClass = result.language ? `language-${result.language}` : '';
      } catch (__) {
        highlighted = md.utils.escapeHtml(str);
      }
    }

    // 关键修复：显式将 class="${languageClass}" 加回去
    // 这样后续脚本才能正确提取语言名称
    return `<pre class="hljs"><code class="${languageClass}">${highlighted}</code></pre>`;
  }
});

// 注册标准公式插件
md.use(markdownItKatex, { throwOnError: false, errorColor: "#cc0000" });

// ==========================================
// 2. 拦截器：智能处理 ```latex
// ==========================================
const defaultFence = md.renderer.rules.fence || function(tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options);
};

md.renderer.rules.fence = (tokens, idx, options, env, self) => {
  const token = tokens[idx];
  const langName = token.info ? token.info.trim().toLowerCase() : '';
  const content = token.content;

  // 拦截 latex 代码块
  if (langName === 'latex' || langName === 'tex') {
    const isFullDocument =
        content.includes('\\documentclass') ||
        content.includes('\\begin{document}') ||
        content.includes('\\usepackage');

    if (!isFullDocument) {
      try {
        const rendered = katex.renderToString(content, {
          throwOnError: false,
          displayMode: true,
          fleqn: false
        });
        return `<div class="katex-block-wrapper">${rendered}</div>`;
      } catch (e) {
        // 失败则回退
      }
    }
  }
  return defaultFence(tokens, idx, options, env, self);
};

// 列表修复
md.renderer.rules.paragraph_open = () => '<p class="md-paragraph">';
md.renderer.rules.list_item_open = () => '<li class="md-list-item">';

// ==========================================
// 3. 流式渲染与自动补全
// ==========================================
const renderedHtml = computed(() => {
  let raw = props.content || "";
  const fenceCount = (raw.match(/^```/gm) || []).length;
  if (fenceCount % 2 !== 0) {
    raw += "\n```";
  }
  return md.render(raw);
});

watch(renderedHtml, () => {
  nextTick(() => enhanceCodeBlocks());
}, { immediate: true });

// ==========================================
// 4. 代码块增强 (功能最全版)
// ==========================================
const enhanceCodeBlocks = () => {
  if (!markdownRef.value) return;

  const preElements = markdownRef.value.querySelectorAll('pre');
  preElements.forEach((pre) => {
    if (pre.querySelector('.code-header')) return;

    const code = pre.querySelector('code');
    if (!code) return;

    // 1. 提取语言：优先从 class 提取
    let language = 'TEXT';
    // 兼容 highlight.js 自动检测生成的类名
    const match = code.className.match(/language-([\w-]+)/);
    if (match) {
      language = match[1].toUpperCase();
    } else {
      // 兜底策略
      language = 'TEXT';
    }

    // 如果是自动检测的，有时 hljs 会返回很奇怪的名字，可以做个映射或保持原样
    if (language === 'UNDEFINED') language = 'TEXT';

    const codeText = code.textContent || '';

    // 2. 创建 Header
    const header = document.createElement('div');
    header.className = 'code-header';
    header.innerHTML = `
      <div class="code-info">
        <span class="language-dot"></span>
        <span class="language-tag">${language}</span>
      </div>
      <div class="code-actions">
        <button class="code-action-btn theme-btn" title="切换主题">
           <svg viewBox="0 0 24 24" width="14" height="14" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>
        </button>
        <button class="code-action-btn fold-btn" title="折叠">
          <svg viewBox="0 0 24 24" width="14" height="14" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg>
        </button>
        <button class="code-action-btn copy-btn" title="复制代码">
          <span class="icon-copy"><svg viewBox="0 0 24 24" width="14" height="14" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg></span>
          <span class="icon-check" style="display:none"><svg viewBox="0 0 24 24" width="14" height="14" stroke="#67C23A" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg></span>
        </button>
      </div>
    `;

    pre.insertBefore(header, code);

    // 3. 绑定事件
    const copyBtn = header.querySelector('.copy-btn');
    const themeBtn = header.querySelector('.theme-btn');
    const foldBtn = header.querySelector('.fold-btn');
    const iconCopy = copyBtn.querySelector('.icon-copy');
    const iconCheck = copyBtn.querySelector('.icon-check');

    copyBtn.addEventListener('click', () => {
      copyToClipboard(codeText, () => {
        iconCopy.style.display = 'none';
        iconCheck.style.display = 'inline-flex';
        setTimeout(() => {
          iconCopy.style.display = 'inline-flex';
          iconCheck.style.display = 'none';
        }, 2000);
      });
    });

    themeBtn.addEventListener('click', () => {
      pre.classList.toggle('dark-theme');
    });

    foldBtn.addEventListener('click', () => {
      const isHidden = code.style.display === 'none';
      code.style.display = isHidden ? 'block' : 'none';
      foldBtn.classList.toggle('folded');
    });
  });
};

// ==========================================
// 5. 通用功能
// ==========================================
const toggleCopyOptions = () => showCopyOptions.value = !showCopyOptions.value;

const copyToClipboard = async (text, onSuccess) => {
  if (!text) return;
  try {
    await navigator.clipboard.writeText(text);
    if (onSuccess) onSuccess();
  } catch (err) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.select();
    document.execCommand('copy');
    document.body.removeChild(textArea);
    if (onSuccess) onSuccess();
  }
};

const handleMainCopy = (type) => {
  let contentToCopy = "";
  if (type === 'markdown') contentToCopy = props.content;
  else if (type === 'html') contentToCopy = renderedHtml.value;
  else if (type === 'text') {
    if (markdownRef.value) contentToCopy = markdownRef.value.innerText;
  }

  copyToClipboard(contentToCopy, () => {
    mainCopyStatus.value = 'success';
    showCopyOptions.value = false;
    setTimeout(() => { mainCopyStatus.value = 'default'; }, 2000);
  });
};

const handleClickOutside = (e) => {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    showCopyOptions.value = false;
  }
};

// ==========================================
// 6. 导出笔记功能
// ==========================================
const isExporting = ref(false);
const showNoteDialog = ref(false);
const noteLoading = ref(false);
const noteForm = reactive({
  title: '',
  content: '',
  tags: [],
  isPrivate: true,
  sourceType: 'AI_CHAT',
  sourceRefId: ''
});

// 标签输入相关
const inputVisible = ref(false);
const inputValue = ref('');
const inputRef = ref(null);

const handleCloseTag = (tag) => {
  noteForm.tags.splice(noteForm.tags.indexOf(tag), 1);
};

const showInput = () => {
  inputVisible.value = true;
  nextTick(() => {
    // 兼容 Element Plus 的 input ref
    if (inputRef.value && inputRef.value.input) {
      inputRef.value.input.focus();
    } else if (inputRef.value && inputRef.value.focus) {
      inputRef.value.focus();
    }
  });
};

const handleInputConfirm = () => {
  if (inputValue.value) {
    if (!noteForm.tags.includes(inputValue.value)) {
      noteForm.tags.push(inputValue.value);
    }
  }
  inputVisible.value = false;
  inputValue.value = '';
};

const handleExportNote = async () => {
  if (isExporting.value) return;
  isExporting.value = true;
  try {
    const content = props.content || "";
    if (!content.trim()) {
      ElMessage.warning('内容为空，无法导出');
      return;
    }

    // 1. 自动提取标题（取第一行，去除#号）
    const lines = content.split('\n');
    let title = 'AI 学习笔记';
    for (let line of lines) {
      if (line.trim()) {
        // 去除 Markdown 标题符号
        title = line.replace(/^[#\s]+/, '').trim().substring(0, 50); 
        break;
      }
    }

    // 2. 调用API解析标签
    let tags = [];
    try {
      tags = await parseTag({ content });
    } catch (e) {
      console.warn('自动解析标签失败，使用空标签', e);
    }

    // 3. 填充表单
    noteForm.title = title;
    noteForm.content = content;
    noteForm.tags = tags || [];
    noteForm.isPrivate = true;
    // 这里的 sourceRefId 从 props 传入
    noteForm.sourceRefId = props.recordId || ''; 
    
    // 4. 打开弹窗
    showNoteDialog.value = true;
  } catch (error) {
    ElMessage.error('导出准备失败：' + error.message);
  } finally {
    isExporting.value = false;
  }
};

const submitNote = async () => {
  if (!noteForm.title || !noteForm.content) {
    ElMessage.warning('标题和内容不能为空');
    return;
  }
  
  noteLoading.value = true;
  try {
    // 严格按照后端 CreateNoteDTO.java 构造请求参数
    const noteDTO = {
      // 1. 必填项
      title: noteForm.title,
      content: noteForm.content,
      
      // 2. 来源信息
      sourceType: noteForm.sourceType || 'AI_CHAT', // 默认为 AI_CHAT
      sourceRefId: noteForm.sourceRefId || null,     // AI对话ID或为空
      
      // 3. 笔记属性
      tags: noteForm.tags || [],
      isPrivate: noteForm.isPrivate,
      
      // 4. 关联信息 (自由笔记这些字段为 null)
      courseId: null,
      chapterId: null,
      sectionId: null,
      videoTime: null
    };

    await createFreedomNote(noteDTO);
    ElMessage.success('笔记保存成功！');
    showNoteDialog.value = false;
  } catch (error) {
    ElMessage.error('保存失败：' + error.message);
  } finally {
    noteLoading.value = false;
  }
};

onMounted(() => document.addEventListener('click', handleClickOutside));
onUnmounted(() => document.removeEventListener('click', handleClickOutside));
</script>

<style scoped>
/* 变量定义，尽量复用 global.css */
.markdown-wrapper {
  position: relative;
  width: 100%;
}

.markdown-container {
  background: var(--background-color, #f0f0f3);
  color: var(--text-primary, #303133);
  border-radius: 12px;
  padding: 24px;
  box-shadow:
      8px 8px 16px var(--neumorphism-shadow-dark, #d1d1d4),
      -8px -8px 16px var(--neumorphism-shadow-light, #ffffff);
  line-height: 1.6;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  transition: all 0.3s ease;
}

/* 布局修复 */
.markdown-container :deep(ul), .markdown-container :deep(ol) {
  padding-left: 1.5em;
  margin: 0.5em 0;
}
.markdown-container :deep(li) {
  margin: 0.3em 0;
}
.markdown-container :deep(li > p) {
  margin: 0;
  display: inline-block;
}
.markdown-container :deep(h1), .markdown-container :deep(h2), .markdown-container :deep(h3) {
  color: var(--primary-color, #002FA7);
  font-weight: 600;
  margin-top: 1.2em;
  margin-bottom: 0.6em;
}
.markdown-container :deep(h1) { border-bottom: 2px solid var(--secondary-color, #517B4D); padding-bottom: 0.3em; font-size: 1.8em; }
.markdown-container :deep(h2) { border-bottom: 1px solid #e0e0e0; padding-bottom: 0.3em; font-size: 1.5em; }

.markdown-container :deep(a) {
  color: #409EFF;
  text-decoration: none;
  border-bottom: 1px dashed transparent;
}
.markdown-container :deep(a:hover) { border-bottom-color: #409EFF; }

.markdown-container :deep(blockquote) {
  margin: 1em 0;
  padding: 10px 16px;
  background: rgba(81, 123, 77, 0.08);
  border-left: 4px solid var(--secondary-color, #517B4D);
  border-radius: 4px;
  color: #555;
}
.markdown-container :deep(blockquote p) { margin: 0; }

/* KaTeX */
.markdown-container :deep(.katex) { font-size: 1.15em; font-family: 'KaTeX_Main', serif; }
.markdown-container :deep(.katex-block-wrapper) {
  margin: 1em 0; text-align: center; overflow-x: auto; padding: 4px 0;
}

/* 代码块增强 */
.markdown-container :deep(pre) {
  background: #f0f0f3;
  border-radius: 12px;
  margin: 1.2em 0;
  padding: 0;
  position: relative;
  box-shadow: inset 5px 5px 10px var(--neumorphism-shadow-dark, #d1d1d4),
  inset -5px -5px 10px var(--neumorphism-shadow-light, #ffffff);
  border: 1px solid rgba(255,255,255,0.5);
  overflow: hidden;
}

.markdown-container :deep(.code-header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 12px;
  background: rgba(0,0,0,0.03);
  border-bottom: 1px solid rgba(0,0,0,0.05);
}

.markdown-container :deep(.code-info) { display: flex; align-items: center; gap: 6px; }
.markdown-container :deep(.language-dot) { width: 8px; height: 8px; border-radius: 50%; background: #409EFF; }
.markdown-container :deep(.language-tag) { font-size: 12px; font-weight: 600; color: #666; text-transform: uppercase; }

.markdown-container :deep(.code-actions) { display: flex; gap: 4px; }
.markdown-container :deep(.code-action-btn) {
  background: transparent; border: none; cursor: pointer; padding: 4px;
  border-radius: 4px; color: #888; display: flex; align-items: center; justify-content: center;
}
.markdown-container :deep(.code-action-btn:hover) { background: rgba(0,0,0,0.05); color: var(--primary-color, #002FA7); }

.markdown-container :deep(.fold-btn) { transition: transform 0.3s; }
.markdown-container :deep(.fold-btn.folded) { transform: rotate(-90deg); }

.markdown-container :deep(pre code) {
  display: block; padding: 1.2em; overflow-x: auto; font-family: 'Fira Code', monospace; font-size: 14px;
}

/* 深色主题 (代码块) */
.markdown-container :deep(pre.dark-theme) {
  background: #282c34; color: #abb2bf;
  box-shadow: inset 4px 4px 8px #1c1f25, inset -4px -4px 8px #343943;
}
.markdown-container :deep(pre.dark-theme .code-header) {
  background: rgba(255,255,255,0.05); border-bottom: 1px solid rgba(255,255,255,0.1);
}
.markdown-container :deep(pre.dark-theme .code-action-btn) { color: #888; }
.markdown-container :deep(pre.dark-theme .code-action-btn:hover) { color: #fff; }

/* 底部功能区 */
.copy-actions { margin-top: 16px; display: flex; justify-content: flex-end; }
.copy-dropdown { position: relative; }

.copy-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 16px;
  border-radius: 8px;
  border: none;
  background: var(--background-color, #f0f0f3);
  color: var(--text-primary, #606266);
  font-size: 14px; cursor: pointer;
  box-shadow: 6px 6px 12px var(--neumorphism-shadow-dark, #d1d1d4),
  -6px -6px 12px var(--neumorphism-shadow-light, #ffffff);
  transition: all 0.2s;
}
.copy-btn:hover { color: var(--primary-color, #002FA7); transform: translateY(-1px); }
.copy-btn:active, .copy-dropdown.active .copy-btn {
  box-shadow: inset 4px 4px 8px var(--neumorphism-shadow-dark, #d1d1d4),
  inset -4px -4px 8px var(--neumorphism-shadow-light, #ffffff);
  transform: translateY(0);
}

.icon-wrapper { display: flex; align-items: center; gap: 6px; }
.icon-wrapper.success { color: #67C23A; }

.copy-options {
  position: absolute; bottom: 100%; right: 0; margin-bottom: 10px;
  background: var(--background-color, #f0f0f3);
  padding: 8px; border-radius: 8px;
  box-shadow: 8px 8px 16px var(--neumorphism-shadow-dark, #d1d1d4),
  -8px -8px 16px var(--neumorphism-shadow-light, #ffffff);
  min-width: 130px; z-index: 100;
}
.copy-option {
  width: 100%; display: flex; align-items: center; gap: 8px;
  padding: 10px 12px; border: none; background: transparent;
  color: var(--text-primary, #606266); font-size: 13px; cursor: pointer;
  border-radius: 6px; text-align: left;
}
.copy-option:hover { background: rgba(64, 158, 255, 0.1); color: #409EFF; }

/* 导出按钮样式 */
.export-btn {
  margin-right: 12px;
}

/* 标签输入样式 */
.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.tag-input-new {
  width: 90px;
  vertical-align: bottom;
}
.button-new-tag {
  height: 24px;
  line-height: 22px;
  padding-top: 0;
  padding-bottom: 0;
}
.note-tag {
  margin-right: 0;
}
</style>

<!-- 全局样式覆盖：针对笔记导出弹窗 (Frosted Glass + Neumorphism) -->
<style>
.note-export-dialog.el-dialog {
  /* 磨砂玻璃背景 (Frosted Glass) - 结合设计文档 */
  background: rgba(240, 240, 243, 0.75) !important; /* 新拟态背景色 + 透明度 */
  backdrop-filter: blur(20px) !important;
  -webkit-backdrop-filter: blur(20px) !important;
  border: 1px solid rgba(255, 255, 255, 0.4) !important;
  border-radius: 20px !important;
  /* 混合阴影：深色投影 + 白色高光 */
  box-shadow: 
    20px 20px 60px rgba(163, 177, 198, 0.4), 
    -20px -20px 60px rgba(255, 255, 255, 0.9) !important;
  padding: 10px;
  overflow: visible !important;
}

.note-export-dialog .el-dialog__header {
  margin-right: 0;
  border-bottom: 1px solid rgba(163, 177, 198, 0.1);
  padding: 20px 20px 15px;
  margin-bottom: 10px;
}

.note-export-dialog .el-dialog__title {
  color: #002FA7; /* 主色调 Klein Blue */
  font-weight: 600;
  font-size: 1.25rem;
  letter-spacing: 0.5px;
  text-shadow: 1px 1px 2px rgba(255, 255, 255, 0.8);
}

.note-export-dialog .el-dialog__body {
  padding: 10px 25px 20px;
}

.note-export-dialog .el-dialog__footer {
  padding: 10px 25px 25px;
  border-top: 1px solid rgba(163, 177, 198, 0.1);
}

/* 表单 Label */
.note-export-dialog .el-form-item__label {
  color: #303133;
  font-weight: 600;
  margin-bottom: 8px;
}

/* 输入框 - 新拟态凹陷 (Neumorphism Inset) */
.note-export-dialog .el-input__wrapper,
.note-export-dialog .el-textarea__inner {
  background: #eef0f5 !important;
  /* 经典的 Neumorphism Inset 阴影参数 */
  box-shadow: 
    inset 6px 6px 12px #d1d1d4, 
    inset -6px -6px 12px #ffffff !important;
  border-radius: 12px !important;
  border: none !important;
  padding: 12px 15px !important;
  transition: all 0.3s ease;
}

.note-export-dialog .el-input__wrapper:hover,
.note-export-dialog .el-input__wrapper.is-focus,
.note-export-dialog .el-textarea__inner:focus {
  background: #f0f0f3 !important;
  box-shadow: 
    inset 4px 4px 8px #ccc, 
    inset -4px -4px 8px #fff !important;
}

.note-export-dialog .el-input__inner {
  color: #303133 !important;
  font-weight: 500;
}

/* 标签 - 新拟态凸起 (Neumorphism Raised) */
.note-export-dialog .el-tag {
  background: #f0f0f3 !important;
  border: none !important;
  color: #002FA7 !important;
  /* 凸起效果 */
  box-shadow: 
    5px 5px 10px #d1d1d4, 
    -5px -5px 10px #ffffff !important;
  border-radius: 8px !important;
  padding: 0 12px !important;
  height: 32px !important;
  line-height: 32px !important;
  margin-right: 12px !important;
  font-weight: 500;
}

.note-export-dialog .el-tag .el-tag__close {
  color: #606266;
  transition: all 0.2s;
}
.note-export-dialog .el-tag .el-tag__close:hover {
  background: transparent;
  color: #F56C6C; /* 危险色 */
  transform: scale(1.2);
}

/* 新增标签按钮 */
.note-export-dialog .button-new-tag {
  background: #f0f0f3 !important;
  border: none !important;
  color: #517B4D !important; /* 副色调 Green Glaze */
  box-shadow: 
    5px 5px 10px #d1d1d4, 
    -5px -5px 10px #ffffff !important;
  border-radius: 8px !important;
  transition: all 0.3s ease;
}

.note-export-dialog .button-new-tag:hover {
  transform: translateY(-2px);
  color: #409EFF !important;
  box-shadow: 
    6px 6px 12px #d1d1d4, 
    -6px -6px 12px #ffffff !important;
}

/* 开关 Switch - 定制化 */
.note-export-dialog .el-switch {
  height: 24px;
}
.note-export-dialog .el-switch__core {
  background: #e4e4e4;
  border: none !important;
  box-shadow: inset 2px 2px 4px #cdcdcd, inset -2px -2px 4px #fff !important;
  height: 24px;
  border-radius: 12px;
}
.note-export-dialog .el-switch.is-checked .el-switch__core {
  background: #002FA7 !important;
  box-shadow: inset 2px 2px 4px rgba(0,0,0,0.2) !important;
}
.note-export-dialog .el-switch__action {
  box-shadow: 1px 1px 3px rgba(0,0,0,0.3);
}

/* 底部按钮 - 新拟态凸起 (Neumorphism Raised) */
.note-export-dialog .dialog-footer .el-button {
  border: none !important;
  border-radius: 12px !important;
  padding: 12px 28px !important;
  height: auto !important;
  font-weight: 600 !important;
  font-size: 14px;
  letter-spacing: 0.5px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

/* 取消按钮 */
.note-export-dialog .dialog-footer .el-button--default {
  background: #f0f0f3 !important;
  color: #606266 !important;
  box-shadow: 
    8px 8px 16px #d1d1d4, 
    -8px -8px 16px #ffffff !important;
}

.note-export-dialog .dialog-footer .el-button--default:hover {
  transform: translateY(-2px);
  box-shadow: 
    10px 10px 20px #d1d1d4, 
    -10px -10px 20px #ffffff !important;
  color: #002FA7 !important;
}

/* 确认按钮 */
.note-export-dialog .dialog-footer .el-button--primary {
  background: #f0f0f3 !important;
  color: #002FA7 !important;
  box-shadow: 
    8px 8px 16px #d1d1d4, 
    -8px -8px 16px #ffffff !important;
}

.note-export-dialog .dialog-footer .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 
    10px 10px 20px #d1d1d4, 
    -10px -10px 20px #ffffff !important;
  color: #517B4D !important; /* Hover 变绿琉璃色 */
  background: linear-gradient(145deg, #f0f0f3, #e6e6e9) !important;
}

.note-export-dialog .dialog-footer .el-button--primary:active {
  transform: translateY(0);
  box-shadow: 
    inset 4px 4px 8px #d1d1d4, 
    inset -4px -4px 8px #ffffff !important;
}
</style>