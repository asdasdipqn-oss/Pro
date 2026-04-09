<template>
  <div class="login-container">
    <!-- 右上角求职者登录按钮 -->
    <div class="top-right-switch">
      <el-button type="primary" link @click="router.push('/candidate')">
        求职者登录
      </el-button>
    </div>

    <div class="login-card">
      <div class="card-header">
        <div class="login-header">
          <div class="logo-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect width="48" height="48" rx="12" fill="#1D1D1F"/>
              <path d="M16 24h16M24 16v16" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
            </svg>
          </div>
          <h1>{{ loginMode === 'hr' ? '人力资源管理' : '求职者登录' }}</h1>
          <p>{{ loginMode === 'hr' ? '登录您的账户以继续' : '欢迎求职者登录' }}</p>
        </div>
      </div>

      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form" hide-required-asterisk>
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="form-options">
          <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
        </div>

        <el-button
          type="primary"
          size="large"
          class="login-btn"
          :loading="loading"
          @click="handleLogin"
        >
          登录
        </el-button>
      </el-form>
    </div>

    <div class="login-copyright">
      © 2025 HR Management System. All rights reserved.
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const loading = ref(false)
const loginMode = ref('hr') // 'hr' or 'candidate'

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({
      username: loginForm.username,
      password: loginForm.password
    })
    ElMessage.success('登录成功')

    // 直接根据角色跳转，避免路由守卫重复检查
    const roles = userStore.roles || []
    const isAdmin = roles.some(role =>
      role === 'ADMIN' || role === 'SUPER_ADMIN' || role === 'admin' || role === 'super_admin'
    )
    const target = isAdmin || roles.includes('HR') ? '/dashboard' : '/profile'
    router.push(target)
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #F5F5F7;
  position: relative;
}

.top-right-switch {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;
}

.login-card {
  width: 380px;
  max-width: 90vw;
  padding: 48px 40px;
  background: #FFFFFF;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.card-header {
  margin-bottom: 24px;
}

.login-header {
  text-align: center;
}

.logo-icon {
  margin-bottom: 20px;
}

.login-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.login-header p {
  color: #86868B;
  font-size: 15px;
}

.login-form {
  margin-top: 24px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 8px 16px;
  height: 48px;
  background: #F5F5F7;
  border: 1px solid transparent !important;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: #E5E5EA !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: #FFFFFF;
  border-color: #007AFF !important;
}

.login-form :deep(.el-input__inner) {
  font-size: 16px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.form-options :deep(.el-checkbox__label) {
  color: #86868B;
  font-size: 14px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 12px !important;
  background: #1D1D1F !important;
  border-color: #1D1D1F !important;
}

.login-btn:hover {
  background: #000000 !important;
  border-color: #000000 !important;
}

.login-footer {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: #86868B;
}

.login-footer a {
  color: #007AFF;
  font-weight: 500;
  margin-left: 4px;
}

.login-footer a:hover {
  text-decoration: underline;
}

.login-copyright {
  margin-top: 40px;
  font-size: 12px;
  color: #86868B;
}

.top-right-switch :deep(.el-button) {
  font-size: 13px;
  padding: 4px 12px;
}
</style>
