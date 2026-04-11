<template>
  <div class="profile-container">
    <div class="profile-header">
      <h1>个人信息</h1>
      <p>完善您的个人信息，让HR更了解您</p>
    </div>

    <!-- 已记录数据展示区 -->
    <div v-if="savedData" class="saved-data-section">
      <h3 class="saved-data-title">
        <span class="check-icon">✓</span>
        已保存的信息
      </h3>
      <div class="saved-data-grid">
        <div class="saved-data-item">
          <span class="label">姓名：</span>
          <span class="value">{{ savedData.realName || '-' }}</span>
        </div>
        <div class="saved-data-item">
          <span class="label">手机：</span>
          <span class="value">{{ savedData.phone || '-' }}</span>
        </div>
        <div class="saved-data-item">
          <span class="label">邮箱：</span>
          <span class="value">{{ savedData.email || '-' }}</span>
        </div>
        <div class="saved-data-item">
          <span class="label">学历：</span>
          <span class="value">{{ getEducationLabel(savedData.education) }}</span>
        </div>
        <div class="saved-data-item">
          <span class="label">期望岗位：</span>
          <span class="value">{{ savedData.expectedPosition || '-' }}</span>
        </div>
      </div>
      <div class="save-time">
        <span class="label">保存时间：</span>
        <span class="value">{{ formatTime(savedData.saveTime) }}</span>
      </div>
    </div>

    <!-- 历史记录区域 -->
    <div v-if="historyList.length > 0" class="history-section">
      <h3 class="history-title">
        <span class="history-icon">📋</span>
        修改记录（最近10次）
      </h3>
      <div class="history-list">
        <div v-for="(record, index) in historyList.slice(0, 10)" :key="index" class="history-item">
          <div class="history-info">
            <div class="history-header">
              <span class="history-action">历史记录</span>
              <span class="history-time">{{ formatTime(record.submitTime) }}</span>
            </div>
            <div class="history-content">
              <div class="history-detail" v-for="(label, key) in displayFields" :key="key">
                <span class="detail-label">{{ label }}：</span>
                <span class="detail-value">{{ record[key] || '-' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="empty-history">
      <div class="empty-icon">📝</div>
      <p>暂无修改记录</p>
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
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)
const username = ref('')
const savedData = ref(null)
const historyList = ref([])

// 监听路由变化，当从其他页面返回时重新加载
watch(() => route.path, (newPath) => {
  if (newPath === '/candidate/profile') {
    loadProfile()
  }
}, { immediate: false })

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
  education: [
    { required: true, message: '请选择学历', trigger: 'change' }
  ]
}

const getEducationLabel = (edu) => {
  const map = { 1: '高中', 2: '大专', 3: '本科', 4: '硕士', 5: '博士' }
  return map[edu] || '-'
}

const displayFields = {
  realName: '姓名',
  phone: '手机号',
  email: '邮箱',
  gender: '性别',
  idCard: '身份证号',
  education: '学历',
  graduateSchool: '毕业院校',
  major: '专业',
  graduateDate: '毕业日期',
  workExperience: '工作年限',
  expectedSalary: '期望薪资',
  expectedPosition: '期望岗位'
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const time = new Date(timeStr)
  return time.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await request.put('/candidate/profile', form)
    ElMessage.success('保存成功')

    // 保存成功后重新加载数据
    await loadProfile()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.response?.data?.message || '保存失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  router.push('/candidate/profile')
}

onMounted(() => {
  username.value = localStorage.getItem('username') || ''
  loadProfile()
  loadHistory()
})

const loadProfile = async () => {
  try {
    const response = await request.get('/candidate/profile')

    const data = response.data.data
    if (data) {

      // 保存已记录的数据
      savedData.value = { ...data }

      // 只保留存在的字段，避免覆盖为null
      if (data.realName !== undefined && data.realName !== null) form.realName = data.realName
      if (data.phone !== undefined && data.phone !== null) form.phone = data.phone
      if (data.email !== undefined && data.email !== null) form.email = data.email
      if (data.gender !== undefined && data.gender !== null) form.gender = data.gender
      if (data.idCard !== undefined && data.idCard !== null) form.idCard = data.idCard
      if (data.education !== undefined && data.education !== null) form.education = data.education
      if (data.graduateSchool !== undefined && data.graduateSchool !== null) form.graduateSchool = data.graduateSchool
      if (data.major !== undefined && data.major !== null) form.major = data.major
      if (data.graduateDate !== undefined && data.graduateDate !== null) form.graduateDate = data.graduateDate
      if (data.workExperience !== undefined && data.workExperience !== null) form.workExperience = data.workExperience
      if (data.expectedSalary !== undefined && data.expectedSalary !== null) form.expectedSalary = data.expectedSalary
      if (data.expectedPosition !== undefined && data.expectedPosition !== null) form.expectedPosition = data.expectedPosition
      if (data.resumeUrl !== undefined && data.resumeUrl !== null) form.resumeUrl = data.resumeUrl

    } else {
    }
  } catch (error) {
    console.error('[CandidateProfile] 加载个人信息失败:', error)
    console.error('[CandidateProfile] 错误详情:', error.response?.data)
  }
}

const loadHistory = async () => {
  try {
    const response = await request.get('/candidate/profile/history')

    if (response.code === 200 && response.data) {
      historyList.value = response.data || []
    }
  } catch (error) {
    console.error('[CandidateProfile] 加载历史记录失败:', error)
    // 如果是404或没有数据，保持为空数组
    historyList.value = []
  }
}
</script>

<style scoped>
.profile-container {
  padding-top: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.profile-header {
  text-align: center;
  margin-bottom: 40px;
}

.profile-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 12px;
}

.profile-header p {
  font-size: 16px;
  color: #86868B;
}

.saved-data-section {
  width: 600px;
  margin: 0 auto 24px;
  background: #F0FDF4;
  padding: 20px;
  border-radius: 12px;
  border-left: 4px solid #007AFF;
}

.saved-data-title {
  font-size: 16px;
  font-weight: 600;
  color: #007AFF;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.check-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #007AFF;
  color: #FFFFFF;
  border-radius: 50%;
  font-size: 14px;
  font-weight: bold;
}

.saved-data-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.saved-data-item {
  display: flex;
  align-items: center;
  background: #FFFFFF;
  padding: 10px 12px;
  border-radius: 8px;
}

.saved-data-item .label {
  font-size: 14px;
  color: #86868B;
  min-width: 80px;
}

.saved-data-item .value {
  font-size: 14px;
  font-weight: 500;
  color: #1D1D1F;
}

.save-time {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #E5E5EA;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.save-time .label {
  color: #86868B;
}

.save-time .value {
  color: #1D1D1F;
  font-weight: 500;
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

.history-section {
  width: 600px;
  margin: 0 auto 24px;
  background: #F5F5F7;
  padding: 20px;
  border-radius: 12px;
}

.history-title {
  font-size: 16px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.history-icon {
  font-size: 18px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  background: #FFFFFF;
  border-radius: 8px;
  padding: 16px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.history-action {
  font-size: 14px;
  font-weight: 600;
  color: #007AFF;
}

.history-time {
  font-size: 12px;
  color: #86868B;
}

.history-content {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.history-detail {
  font-size: 14px;
}

.detail-label {
  color: #86868B;
  margin-right: 4px;
}

.detail-value {
  color: #1D1D1F;
  font-weight: 500;
}

.empty-history {
  width: 600px;
  margin: 0 auto 24px;
  text-align: center;
  padding: 40px 20px;
  background: #F5F5F7;
  border-radius: 12px;
}

.empty-history .empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-history p {
  font-size: 14px;
  color: #86868B;
  margin: 0;
}
</style>
