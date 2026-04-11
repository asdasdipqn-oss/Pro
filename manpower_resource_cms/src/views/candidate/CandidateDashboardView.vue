<template>
  <div class="candidate-dashboard">
    <div class="dashboard-container">
      <!-- 左侧个人信息面板 - 始终显示 -->
      <div class="left-panel">
        <div class="profile-summary">
          <div class="profile-avatar">
            {{ displayInitial }}
          </div>
          <h3 class="profile-name">{{ profile.value.realName || '请完善信息' }}</h3>
          <p class="profile-status">{{ hasCompleteProfile ? '已完善信息' : '未完善信息' }}</p>
        </div>
        <div class="profile-info-list">
          <div class="info-row">
            <span class="label">手机号码</span>
            <span class="value">{{ profile.value.phone }}</span>
          </div>
          <div class="info-row">
            <span class="label">邮箱</span>
            <span class="value">{{ profile.value.email || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">学历</span>
            <span class="value">{{ getEducationLabel(profile.value.education) }}</span>
          </div>
          <div class="info-row">
            <span class="label">期望岗位</span>
            <span class="value">{{ profile.value.expectedPosition || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">最后更新</span>
            <span class="value">{{ profile.value.lastUpdateTime || '暂无更新' }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧主内容 -->
      <div class="right-content">
        <!-- 顶部操作区 -->
        <div class="action-bar">
          <el-button type="primary" size="large" @click="goToProfile">
            <span v-if="!hasCompleteProfile">填写个人信息</span>
            <span v-else>编辑个人信息</span>
          </el-button>
        </div>

        <!-- 未完善信息提示 -->
        <div class="welcome-banner" v-if="!hasCompleteProfile">
          <h1>欢迎，{{ username }}！</h1>
          <p>请完善您的个人信息以开始求职之旅</p>
        </div>

        <!-- 投递记录 -->
        <div class="history-section" v-if="hasCompleteProfile">
          <h2 class="section-title">我的投递记录</h2>
          <el-table :data="applicationList" stripe v-loading="applicationLoading" class="application-table">
            <el-table-column prop="jobName" label="岗位名称" width="200" />
            <el-table-column prop="applyTime" label="投递时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.applyTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="hrReviewTime" label="筛选时间" width="180">
              <template #default="{ row }">
                {{ row.hrReviewTime ? formatTime(row.hrReviewTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="hrComment" label="备注" min-width="150" show-overflow-tooltip />
          </el-table>
          <div v-if="applicationList.length === 0 && !applicationLoading" class="empty-tip">
            暂无投递记录，去岗位招聘页面投递简历吧
          </div>
        </div>

        <!-- 个人信息提交历史 -->
        <div class="history-section">
          <h2 class="section-title">个人信息修改记录</h2>
          <el-table :data="historyList" stripe v-loading="loading" class="history-table">
            <el-table-column prop="submitTime" label="提交时间" width="180">
              <template #default="{ row }">
                {{ formatSubmitTime(row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="realName" label="姓名" width="100" />
            <el-table-column prop="phone" label="手机号" width="130" />
            <el-table-column prop="email" label="邮箱" width="180" />
            <el-table-column prop="education" label="学历" width="100">
              <template #default="{ row }">
                {{ getEducationLabel(row.education) }}
              </template>
            </el-table-column>
            <el-table-column prop="expectedPosition" label="期望岗位" width="150" />
            <el-table-column prop="expectedSalary" label="期望薪资" width="120">
              <template #default="{ row }">
                {{ row.expectedSalary ? row.expectedSalary + '元/月' : '-' }}
              </template>
            </el-table-column>
          </el-table>
          <div v-if="historyList.length === 0" class="empty-tip">
            暂无历史记录，请先填写个人信息
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getMyApplications } from '@/api/recruit'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const username = ref('')
const hasCompleteProfile = ref(false)
const loading = ref(false)
const applicationLoading = ref(false)

const profile = ref({
  realName: '',
  phone: '',
  email: '',
  education: '',
  graduateSchool: '',
  major: '',
  expectedPosition: '',
  expectedSalary: '',
  lastUpdateTime: ''
})

const historyList = ref([])
const applicationList = ref([])

const getEducationLabel = (edu) => {
  const map = { 1: '高中', 2: '大专', 3: '本科', 4: '硕士', 5: '博士' }
  return map[edu] || '-'
}

const displayInitial = computed(() => {
  const name = profile.value.realName
  return name ? name.charAt(0).toUpperCase() : ''
})

const getStatusType = (status) => {
  const typeMap = {
    0: 'info',      // 待处理
    1: 'warning',   // 已查看
    2: 'primary',   // 面试
    3: 'success'    // 已录用
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    0: '待处理',
    1: '已查看',
    2: '面试中',
    3: '已录用'
  }
  return textMap[status] || '未知'
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const time = new Date(timeStr)
  const now = new Date()
  const diff = now - time
  const diffMinutes = Math.floor(diff / 60000)

  if (diffMinutes < 1) {
    return '刚刚'
  } else if (diffMinutes < 60) {
    return diffMinutes + '分钟前'
  } else if (diffMinutes < 1440) {
    const hours = Math.floor(diffMinutes / 60)
    return hours + '小时前'
  } else if (diffMinutes < 43200) {
    const days = Math.floor(diffMinutes / 1440)
    return days + '天前'
  } else {
    return timeStr.substring(0, 16)
  }
}

watch(() => route.path, (newPath) => {
  if (newPath === '/candidate/dashboard') {
    loadProfile()
    loadHistory()
    loadApplications()
  }
}, { immediate: false })

onMounted(() => {
  username.value = localStorage.getItem('username') || ''

  const token = localStorage.getItem('token')
  if (!token) {
    console.log('[CandidateDashboard] No token found, skipping data load')
    return
  }

  console.log('[CandidateDashboard] Token found, loading data:', token.substring(0, 20) + '...')
  loadProfile()
  loadHistory()
  loadApplications()
})

const loadProfile = async () => {
  try {
    console.log('[CandidateDashboard] 开始加载个人信息...')
    const response = await axios.get('/api/candidate/profile')
    console.log('[CandidateDashboard] 完整响应:', response)

    if (response && response.code === 200) {
      const data = response.data
      console.log('[CandidateDashboard] 解析后的数据:', data)

      if (data) {
        console.log('[CandidateDashboard] 设置profile数据:', data)
        profile.value = data
        hasCompleteProfile.value = !!data.realName
        console.log('[CandidateDashboard] hasCompleteProfile:', hasCompleteProfile.value)

        if (data.updateTime) {
          try {
            const updateTime = new Date(data.updateTime)
            const now = new Date()
            const diff = now - updateTime
            if (diff < 60000) {
              profile.value.lastUpdateTime = '刚刚'
            } else if (diff < 3600000) {
              profile.value.lastUpdateTime = Math.floor(diff / 60000) + '分钟前'
            } else if (diff < 86400000) {
              const hours = Math.floor(diff / 3600000)
              profile.value.lastUpdateTime = hours + '小时前'
            } else {
              profile.value.lastUpdateTime = data.updateTime
            }
          } catch (e) {
            console.error('[CandidateDashboard] 日期解析错误:', e)
            profile.value.lastUpdateTime = data.updateTime || '暂无更新'
          }
        } else {
          profile.value.lastUpdateTime = '暂无更新'
        }
      } else {
        hasCompleteProfile.value = false
        console.log('[CandidateDashboard] 数据为空，hasCompleteProfile设为false')
      }
    } else {
      hasCompleteProfile.value = false
      console.log('[CandidateDashboard] 响应状态异常:', response)
    }
  } catch (error) {
    console.error('[CandidateDashboard] 加载个人信息失败:', error)
    console.error('[CandidateDashboard] 错误详情:', error.response)
    hasCompleteProfile.value = false
  }
}

const loadHistory = async () => {
  loading.value = true
  try {
    console.log('[CandidateDashboard] 开始加载历史记录...')
    const response = await axios.get('/api/candidate/profile/history')
    console.log('[CandidateDashboard] 历史记录完整响应:', response)

    if (response && response.code === 200) {
      const data = response.data
      console.log('[CandidateDashboard] 解析历史数据:', data)
      historyList.value = data || []
    } else {
      historyList.value = []
    }
  } catch (error) {
    console.error('[CandidateDashboard] 加载历史记录失败:', error)
    historyList.value = []
  } finally {
    loading.value = false
  }
}

const loadApplications = async () => {
  applicationLoading.value = true
  try {
    console.log('[CandidateDashboard] 开始加载投递记录...')
    const response = await getMyApplications()
    console.log('[CandidateDashboard] 投递记录响应:', response)

    if (response && response.code === 200) {
      const data = response.data
      console.log('[CandidateDashboard] 解析投递数据:', data)
      applicationList.value = data || []
    } else {
      applicationList.value = []
    }
  } catch (error) {
    console.error('[CandidateDashboard] 加载投递记录失败:', error)
    applicationList.value = []
  } finally {
    applicationLoading.value = false
  }
}

const goToProfile = () => {
  router.push('/candidate/profile')
}

const formatSubmitTime = (timeStr) => {
  if (!timeStr) return '-'
  const time = new Date(timeStr)
  const now = new Date()
  const diff = now - time
  const diffMinutes = Math.floor(diff / 60000)

  if (diffMinutes < 1) {
    return '刚刚'
  } else if (diffMinutes < 60) {
    return diffMinutes + '分钟前'
  } else if (diffMinutes < 1440) {
    const hours = Math.floor(diffMinutes / 60)
    return hours + '小时前'
  } else if (diffMinutes < 43200) {
    const days = Math.floor(diffMinutes / 1440)
    return days + '天前'
  } else {
    return timeStr.substring(0, 16)
  }
}
</script>

<style scoped>
.candidate-dashboard {
  padding-top: 24px;
  padding-left: 24px;
  padding-right: 24px;
}

.dashboard-container {
  display: flex;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.left-panel {
  width: 320px;
  flex-shrink: 0;
}

.profile-summary {
  background: #FFFFFF;
  padding: 32px 24px;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  text-align: center;
  margin-bottom: 20px;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #FFFFFF;
  font-size: 32px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.profile-name {
  font-size: 20px;
  font-weight: 600;
  color: #1D1D1F;
  margin: 0 0 8px;
}

.profile-status {
  font-size: 14px;
  color: #007AFF;
  margin: 0 0 24px;
}

.profile-info-list {
  background: #F5F5F7;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #E5E5EA;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  font-size: 14px;
  color: #86868B;
}

.info-row .value {
  font-size: 14px;
  font-weight: 500;
  color: #1D1D1F;
}

.right-content {
  flex: 1;
  min-width: 0;
}

.action-bar {
  background: #FFFFFF;
  padding: 20px 24px;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-bar .el-button {
  background: #007AFF;
  border-color: #007AFF;
  color: #FFFFFF;
  font-weight: 500;
  padding: 12px 32px;
  border-radius: 10px;
}

.action-bar .el-button:hover {
  background: #0066D6;
  border-color: #0066D6;
}

.welcome-banner {
  background: #FFFFFF;
  padding: 40px 60px;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
}

.welcome-banner h1 {
  font-size: 28px;
  color: #1D1D1F;
  margin-bottom: 12px;
}

.welcome-banner p {
  font-size: 16px;
  color: #86868B;
}

.history-section {
  background: #FFFFFF;
  padding: 24px;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 20px;
}

.history-table {
  margin-top: 10px;
}

.application-table {
  margin-top: 10px;
}

.history-table :deep(.el-table__header-wrapper),
.application-table :deep(.el-table__header-wrapper) {
  background: #F5F5F7;
}

.history-table :deep(.el-table__header th),
.application-table :deep(.el-table__header th) {
  background: #FFFFFF;
  color: #86868B;
  font-weight: 600;
  border-bottom: 2px solid #E5E5EA;
}

.history-table :deep(.el-table__body td),
.application-table :deep(.el-table__body td) {
  padding: 16px 12px;
}

.history-table :deep(.el-table__row--striped),
.application-table :deep(.el-table__row--striped) {
  background: #FFFFFF;
}

.history-table :deep(.el-table__row--striped:nth-child(even)),
.application-table :deep(.el-table__row--striped:nth-child(even)) {
  background: #F9FAFB;
}

.empty-tip {
  text-align: center;
  padding: 40px 20px;
  color: #86868B;
  font-size: 14px;
}
</style>
