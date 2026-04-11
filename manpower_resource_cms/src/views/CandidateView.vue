<template>
  <div class="candidate-register-container">
    <div class="top-right-switch">
      <el-button type="primary" link @click="router.push('/login')">
        员工登录
      </el-button>
      <el-button
        v-if="loginMode === 'register'"
        type="primary"
        link
        @click="loginMode = 'login'"
      >
        求职者登录
      </el-button>
      <el-button
        v-else
        type="primary"
        link
        @click="loginMode = 'register'"
      >
        立即注册
      </el-button>
    </div>

    <div class="register-card" v-if="loginMode === 'register'">
      <div class="register-header">
        <div class="logo-icon">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="12" fill="#1D1D1F"/>
            <path d="M16 24h16M24 16v16" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
        </div>
        <h1>求职者注册</h1>
        <p>简单三步，开启求职之旅</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="register-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" size="large" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" class="register-btn" :loading="loading" @click="handleRegister">
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <a href="javascript:;" @click="loginMode = 'login'">立即登录</a>
      </div>
    </div>

    <div class="login-card" v-else>
      <div class="card-header">
        <div class="login-header">
          <div class="logo-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect width="48" height="48" rx="12" fill="#1D1D1F"/>
              <path d="M16 24h16M24 16v16" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
            </svg>
          </div>
          <h1>求职者登录</h1>
          <p>欢迎回来，继续您的求职之旅</p>
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

      <div class="login-footer">
        <span>还没有账号？</span>
        <a href="javascript:;" @click="loginMode = 'register'">立即注册</a>
      </div>
    </div>

    <div class="register-copyright">
      © 2025 HR Management System. All rights reserved.
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const formRef = ref()
const loginFormRef = ref()
const loading = ref(false)
const loginMode = ref('register')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const loginForm = reactive({
  username: '',
  password: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await axios.post('/api/candidate/register', form)
    ElMessage.success('注册成功，请登录')
    loginMode.value = 'login'
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.response?.data?.message || '注册失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleLogin = async () => {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const response = await axios.post('/api/candidate/login', loginForm)
    ElMessage.success('登录成功')

    // 保存token到本地存储
    localStorage.setItem('token', response.data.data.token)
    localStorage.setItem('userType', 'candidate')
    localStorage.setItem('username', response.data.data.username)

    // 跳转到求职者首页
    router.push('/candidate/dashboard')
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.response?.data?.message || '登录失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.candidate-register-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #F5F5F7;
  padding: 40px 20px;
  position: relative;
}

.top-right-switch {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;
}

.register-card,
.login-card {
  width: 380px;
  padding: 40px;
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
  margin-bottom: 16px;
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

.register-form,
.login-form {
  margin-top: 24px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.register-form :deep(.el-input__wrapper),
.login-form :deep(.el-input__wrapper) {
  padding: 8px 16px;
  height: 48px;
  background: #F5F5F7;
  border: 1px solid transparent !important;
}

.register-form :deep(.el-input__wrapper:hover),
.login-form :deep(.el-input__wrapper:hover) {
  border-color: #E5E5EA !important;
}

.register-form :deep(.el-input__wrapper.is-focus),
.login-form :deep(.el-input__wrapper.is-focus) {
  background: #FFFFFF;
  border-color: #007AFF !important;
}

.register-form :deep(.el-input__inner),
.login-form :deep(.el-input__inner) {
  font-size: 15px;
}

.register-btn,
.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 10px !important;
  background: #1D1D1F !important;
  border-color: #1D1D1F !important;
}

.register-btn:hover,
.login-btn:hover {
  background: #000000 !important;
  border-color: #000000 !important;
}

.register-footer,
.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #86868B;
}

.register-footer a,
.login-footer a {
  color: #007AFF;
  font-weight: 500;
  margin-left: 4px;
}

.register-footer a:hover,
.login-footer a:hover {
  text-decoration: underline;
}

.register-copyright {
  margin-top: 30px;
  font-size: 12px;
  color: #86868B;
}

.top-right-switch :deep(.el-button) {
  font-size: 13px;
  padding: 4px 12px;
}
</style>
