import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useCandidateStore } from '@/stores/candidate'
import router from '@/router'

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 60000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    const isLoginRequest = config.url?.includes('/auth/login') || config.url?.includes('/candidate/login')
    if (token && !isLoginRequest) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 根据用户类型退出登录并跳转
function handleAuthExpired() {
  const userType = localStorage.getItem('userType')
  const redirectPath = userType === 'candidate' ? '/candidate' : '/login'

  if (userType === 'candidate') {
    const candidateStore = useCandidateStore()
    candidateStore.logout()
  } else {
    const userStore = useUserStore()
    userStore.logout()
  }

  // 清空所有 cookie
  document.cookie.split(';').forEach((cookie) => {
    const name = cookie.split('=')[0].trim()
    document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/`
  })

  router.push(redirectPath)
}

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 处理 blob 类型响应（文件下载）
    if (response.config.responseType === 'blob') {
      return response.data
    }

    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 2001) {
        handleAuthExpired()
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    // 处理请求取消错误
    if (axios.isCancel(error)) {
      console.log('Request canceled:', error.message)
      return Promise.reject(error)
    }

    const status = error.response?.status

    // 处理 40x 错误（400-499）
    if (status >= 400 && status < 500) {
      const errorMessages = {
        400: '请求参数错误',
        401: '登录已过期，请重新登录',
        403: '没有访问权限',
        404: '请求的资源不存在'
      }
      ElMessage.error(errorMessages[status] || '请求失败')

      // 只有 401（未认证/token过期）才需要退出登录
      if (status === 401) {
        handleAuthExpired()
      }
      return Promise.reject(error)
    }

    // 处理 blob 类型的错误响应
    if (error.response?.config?.responseType === 'blob' && error.response?.data) {
      const reader = new FileReader()
      reader.onload = () => {
        try {
          const result = JSON.parse(reader.result)
          ElMessage.error(result.message || '导出失败')
        } catch {
          ElMessage.error('导出失败')
        }
      }
      reader.readAsText(error.response.data)
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
