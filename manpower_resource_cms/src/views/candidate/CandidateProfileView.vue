<template>
  <div class="profile-container">
    <div class="profile-header">
      <h1>个人信息</h1>
      <p>完善您的个人信息，让HR更了解您</p>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" class="profile-form" label-width="120px">
      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="form.realName" placeholder="请输入真实姓名" size="large" />
      </el-form-item>

      <el-form-item label="手机号码" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号码" size="large" />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" size="large" />
      </el-form-item>

      <el-form-item label="性别" prop="gender">
        <el-radio-group v-model="form.gender">
          <el-radio :label="1">男</el-radio>
          <el-radio :label="0">女</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="身份证号" prop="idCard">
        <el-input v-model="form.idCard" placeholder="请输入身份证号" size="large" />
      </el-form-item>

      <el-form-item label="学历" prop="education">
        <el-select v-model="form.education" placeholder="请选择学历" size="large">
          <el-option :label="1" value="1">高中</el-option>
          <el-option :label="2" value="2">大专</el-option>
          <el-option :label="3" value="3">本科</el-option>
          <el-option :label="4" value="4">硕士</el-option>
          <el-option :label="5" value="5">博士</el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="毕业院校" prop="graduateSchool">
        <el-input v-model="form.graduateSchool" placeholder="请输入毕业院校" size="large" />
      </el-form-item>

      <el-form-item label="专业" prop="major">
        <el-input v-model="form.major" placeholder="请输入专业" size="large" />
      </el-form-item>

      <el-form-item label="毕业日期" prop="graduateDate">
        <el-date-picker
          v-model="form.graduateDate"
          type="date"
          placeholder="请选择毕业日期"
          size="large"
          style="width: 100%"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item label="工作年限（年）" prop="workExperience">
        <el-input-number v-model="form.workExperience" :min="0" :max="50" placeholder="请输入工作年限" size="large" style="width: 100%" />
      </el-form-item>

      <el-form-item label="期望薪资（元/月）" prop="expectedSalary">
        <el-input v-model="form.expectedSalary" placeholder="请输入期望薪资" size="large">
          <template #append>元</template>
        </el-input>
      </el-form-item>

      <el-form-item label="期望岗位" prop="expectedPosition">
        <el-input v-model="form.expectedPosition" placeholder="请输入期望岗位" size="large" />
      </el-form-item>

      <el-form-item label="简历地址" prop="resumeUrl">
        <el-input v-model="form.resumeUrl" placeholder="请输入简历地址（选填）" size="large" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleSave">
          保存信息
        </el-button>
        <el-button size="large" @click="handleCancel">
          取消
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const username = ref('')

const form = reactive({
  realName: '',
  phone: '',
  email: '',
  gender: 1,
  idCard: '',
  education: 3,
  graduateSchool: '',
  major: '',
  graduateDate: '',
  workExperience: 0,
  expectedSalary: '',
  expectedPosition: '',
  resumeUrl: ''
})

const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  idCard: [
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[1-2]\d|3[0-1])\d{3}[0-9Xx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  education: [
    { required: true, message: '请选择学历', trigger: 'change' }
  ]
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await axios.put('/api/candidate/profile', form)
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.response?.data?.message || '保存失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  router.push('/candidate/dashboard')
}

onMounted(() => {
  username.value = localStorage.getItem('username') || ''
  loadProfile()
})

const loadProfile = async () => {
  try {
    const response = await axios.get('/api/candidate/profile')
    const data = response.data.data
    if (data) {
      Object.assign(form, data)
    }
  } catch (error) {
    console.error('加载个人信息失败:', error)
  }
}
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #F5F5F7;
  padding: 40px 20px;
}

.profile-header {
  text-align: center;
  margin-bottom: 40px;
}

.profile-header h1 {
  font-size: 32px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 12px;
}

.profile-header p {
  font-size: 16px;
  color: #86868B;
}

.profile-form {
  max-width: 600px;
  margin: 0 auto;
  background: #FFFFFF;
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.profile-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.profile-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #1D1D1F;
}

.profile-form :deep(.el-input__wrapper) {
  padding: 8px 16px;
  height: 44px;
  background: #F5F5F7;
  border: 1px solid transparent;
}

.profile-form :deep(.el-input__wrapper:hover) {
  border-color: #E5E5EA;
}

.profile-form :deep(.el-input__wrapper.is-focus) {
  background: #FFFFFF;
  border-color: #007AFF;
}

.profile-form :deep(.el-input__inner) {
  font-size: 15px;
}

.profile-form :deep(.el-select .el-input__wrapper) {
  padding: 8px 16px;
  height: 44px;
  background: #F5F5F7;
  border: 1px solid transparent;
}

.profile-form :deep(.el-input-number) {
  width: 100%;
}

.profile-form :deep(.el-input-number .el-input__inner) {
  padding: 8px 16px;
  height: 44px;
  background: #F5F5F7;
  border: 1px solid transparent;
}

.submit-btn {
  width: 60%;
  margin-right: 12px;
}

.profile-form :deep(.el-form-item__content) {
  flex-direction: row;
  gap: 12px;
}
</style>
