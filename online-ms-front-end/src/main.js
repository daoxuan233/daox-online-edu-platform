
import './styles/global.css'
import '@fortawesome/fontawesome-free/css/all.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/group-chat-dialog.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// FontAwesome imports
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { fab } from '@fortawesome/free-brands-svg-icons'

import axios from "axios";

// Add icons to the library
library.add(fas, fab)

// 创建axios实例
axios.defaults.baseURL = 'http://localhost:8080/api'

const app = createApp(App)
const pinia = createPinia()

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册FontAwesome组件
app.component('font-awesome-icon', FontAwesomeIcon)

app.use(router)
// app.use(pinia) // 暂不使用Pinia，仅导入依赖
app.use(ElementPlus)

// 全局错误处理
app.config.errorHandler = (err, instance, info) => {
  console.error('全局错误:', err)
  console.error('错误信息:', info)
}

app.mount('#app')
