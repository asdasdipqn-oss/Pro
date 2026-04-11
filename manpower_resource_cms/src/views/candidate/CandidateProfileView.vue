<template>
  <div class="profile-container">
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
          <el-option label="高中" :value="1" />
          <el-option label="大专" :value="2" />
          <el-option label="本科" :value="3" />
          <el-option label="硕士" :value="4" />
          <el-option label="博士" :value="5" />
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
          <template #append>元/月</template>
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
      </el-form-item>
    </el-form>

    <!-- 历史记录 -->
    <div class="history-section">
      <h3 class="history-title">保存记录</h3>
      <div class="history-list">
        <div v-for="history in historyList" :key="history.id" class="history-item">
          <div class="history-header">
            <span class="save-time">{{ formatTime(history.submitTime) }}</span>
          </div>
          <el-table :data="history.data" border stripe size="small" class="history-table">
            <el-table-column prop="field" label="字段" width="100" />
            <el-table-column prop="value" label="值" width="200" />
          </el-table>
        </div>
      </div>
    </div>
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
const historyList = ref([])

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
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(\d|X|x)\d{2}[0-9X]\d{2})\d{3}[0-9]\d{3}[0-9X]\d{2})$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  education: [
    { required: true, message: '请选择学历', trigger: 'change' }
  ]
}

const displayFields = {
  realName: '真实姓名',
  phone: '手机号码',
  email: '邮箱',
  gender: '性别',
  idCard: '身份证号',
  education: '学历',
  graduateSchool: '毕业院校',
  major: '专业',
  graduateDate: '毕业日期',
  workExperience: '工作年限',
  expectedSalary: '期望薪资',
  expectedPosition: '期望岗位',
  resumeUrl: '简历地址'
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await axios.put('/candidate/profile', form)
    ElMessage.success('保存成功')
    loadHistory()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.response?.data?.message || '保存失败，请重试')
  } finally {
    loading.value = false
  }
}

const loadHistory = async () => {
  try {
    const response = await axios.get('/candidate/profile/history')
    historyList.value = response.data || []
  } catch (error) {
    console.error('加载历史记录失败:', error)
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  username.value = localStorage.getItem('username') || ''
  loadProfile()
  loadHistory()
})

const loadProfile = async () => {
  try {
    const response = await axios.get('/candidate/profile')
    const data = response.data || {}
    Object.assign(form, data)
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

.submit-btn {
  width: 60%;
  margin-right: 12px;
}

.history-section {
  max-width: 800px;
  margin: 40px auto 0;
  background: #FFFFFF;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.history-title {
  font-size: 18px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #E5E5EA;
}

.history-list {
  max-height: 400px;
  overflow-y: auto;
}

.history-item {
  margin-bottom: 24px;
}

.history-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.save-time {
  font-size: 12px;
  color: #86868B;
  font-weight: 500;
}

.history-table {
  margin-top: 12px;
}
</style>
