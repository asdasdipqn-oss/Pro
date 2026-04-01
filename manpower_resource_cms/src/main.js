import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import './assets/main.css'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 全局错误处理 - 忽略一些浏览器扩展错误
app.config.errorHandler = (err, instance, info) => {
  // 忽略一些浏览器扩展错误
  if (err?.message?.includes('listener indicated an asynchronous response')) {
    return
  }
  console.error('[Vue Error]', err, info)
}

// 处理未捕获的Promise拒绝
window.addEventListener('unhandledrejection', (event) => {
  if (event.reason?.message?.includes('listener indicated an asynchronous response')) {
    event.preventDefault()
    return
  }
  // 不中断默认的处理
})

app.mount('#app')
