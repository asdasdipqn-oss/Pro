<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <div class="logo-icon">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="12" fill="#1D1D1F"/>
            <path d="M16 24h16M24 16v16" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
        </div>
        <h1>创建账户</h1>
        <p>注册您的 HR 管理系统账户</p>
      </div>
      
      <el-form ref="formRef" :model="form" :rules="rules" class="register-form" hide-required-asterisk>
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" size="large" show-password />
        </el-form-item>
        
        <el-button type="primary" size="large" class="register-btn" :loading="loading" @click="handleRegister">
          创建账户
        </el-button>
      </el-form>
      
      <div class="register-footer">
        <span>已有账户？</span>
        <a href="javascript:;" @click="router.push('/login')">登录</a>
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
import { register } from '@/api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
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

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    await register({
      username: form.username,
      password: form.password
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #F5F5F7;
}

.register-card {
  width: 380px;
  padding: 48px 40px;
  background: #FFFFFF;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.register-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-icon {
  margin-bottom: 20px;
}

.register-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.register-header p {
  color: #86868B;
  font-size: 15px;
}

.register-form {
  margin-top: 24px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.register-form :deep(.el-input__wrapper) {
  padding: 8px 16px;
  height: 48px;
  background: #F5F5F7;
  border: 1px solid transparent !important;
}

.register-form :deep(.el-input__wrapper:hover) {
  border-color: #E5E5EA !important;
}

.register-form :deep(.el-input__wrapper.is-focus) {
  background: #FFFFFF;
  border-color: #007AFF !important;
}

.register-form :deep(.el-input__inner) {
  font-size: 16px;
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 12px !important;
  background: #1D1D1F !important;
  border-color: #1D1D1F !important;
  margin-top: 8px;
}

.register-btn:hover {
  background: #000000 !important;
  border-color: #000000 !important;
}

.register-footer {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: #86868B;
}

.register-footer a {
  color: #007AFF;
  font-weight: 500;
  margin-left: 4px;
}

.register-footer a:hover {
  text-decoration: underline;
}

.register-copyright {
  margin-top: 40px;
  font-size: 12px;
  color: #86868B;
}
</style>
