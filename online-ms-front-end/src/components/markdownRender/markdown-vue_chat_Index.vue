<template>
  <div class="markdown-wrapper">
    <!-- 主内容渲染区 -->
    <div
        class="markdown-container"
        ref="markdownRef"
        v-html="renderedHtml"
    ></div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from "vue";
import MarkdownIt from "markdown-it";
import hljs from "highlight.js";
import "highlight.js/styles/github.css"; // 引入代码高亮样式

// === KaTeX 核心 ===
import katex from "katex";
import "katex/dist/katex.min.css";
import markdownItKatex from "@iktakahiro/markdown-it-katex";

const props = defineProps({
  content: { type: String, default: "" },
  recordId: { type: String, default: null }
});

const markdownRef = ref(null);

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
      </div>
    `;

    pre.insertBefore(header, code);

    // 3. 绑定事件
    const themeBtn = header.querySelector('.theme-btn');
    const foldBtn = header.querySelector('.fold-btn');

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
</style>
