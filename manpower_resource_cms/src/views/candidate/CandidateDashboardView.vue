<template>
  <div class="candidate-layout">
    <!-- 左侧菜单栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
            <rect width="32" height="32" rx="8" fill="#1D1D1F"/>
            <path d="M10 16h12M16 10v12" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <span class="logo-text">求职者中心</span>
      </div>

      <div class="nav-item" :class="{ active: currentMenu === 'jobs' }" @click="currentMenu = 'jobs'">
        <div class="nav-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" fill="#34C759"/>
            <path d="M8 12h8M16 12v8" stroke="#fff" stroke-width="2"/>
          </svg>
        </div>
        <span class="nav-text">招聘岗位</span>
      </div>

      <div class="nav-item" :class="{ active: currentMenu === 'process' }" @click="currentMenu = 'process'">
        <div class="nav-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" fill="#FF9500"/>
            <path d="M12 12l-3 4-4h3v2H12m3-3 4v2" stroke="#fff" stroke-width="2"/>
          </svg>
        </div>
        <span class="nav-text">招聘流程</span>
      </div>

      <div class="nav-item" :class="{ active: currentMenu === 'profile' }" @click="currentMenu = 'profile'">
        <div class="nav-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" fill="#007AFF"/>
            <path d="M12 6v6" stroke="#fff" stroke-width="2"/>
          </svg>
        </div>
        <span class="nav-text">个人信息</span>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 岗位招聘 -->
      <div v-show="currentMenu === 'jobs'" class="content-panel">
        <h2 class="panel-title">招聘岗位</h2>
        <div v-if="jobList.length === 0 && !loading" class="empty-tip">暂无招聘岗位信息</div>
        <div v-else v-loading="loading" class="job-list">
          <div v-for="job in jobList" :key="job.id" class="job-card">
            <div class="job-header">
              <h3>{{ job.jobName }}</h3>
              <el-tag type="success" size="small">招聘中</el-tag>
            </div>
            <div class="job-info">
              <div class="info-item">
                <span class="label">部门：</span>
                <span class="value">{{ job.deptName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">招聘人数：</span>
                <span class="value">{{ job.headcount }} 人</span>
              </div>
              <div class="info-item">
                <span class="label">薪资范围：</span>
                <span class="value">
                  {{ job.salaryMin && job.salaryMax ? `${job.salaryMin}-${job.salaryMax}元/月` : '薪资面议' }}
                </span>
              </div>
            </div>
            <div class="job-description">
              <div class="desc-item">
                <strong>岗位职责：</strong>
                <p>{{ job.jobDescription || '-' }}</p>
              </div>
              <div class="desc-item">
                <strong>任职要求：</strong>
                <p>{{ job.requirements || '-' }}</p>
              </div>
            </div>
            <div class="job-footer">
              <span class="publish-time">发布于 {{ formatDate(job.createTime) }}</span>
              <el-button type="primary" size="small" @click="handleApply(job)">投递简历</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 招聘流程 -->
      <div v-show="currentMenu === 'process'" class="content-panel">
        <h2 class="panel-title">我的应聘流程</h2>
        <div v-if="applicationList.length === 0 && !loading" class="empty-tip">暂无应聘记录</div>
        <div v-else v-loading="loading" class="process-list">
          <div v-for="app in applicationList" :key="app.id" class="process-item">
            <div class="process-header">
              <span class="job-name">{{ app.jobName }}</span>
              <el-tag :type="getStatusType(app.status)" size="small">
                {{ getStatusText(app.status) }}
              </el-tag>
            </div>
            <div class="process-timeline">
              <div class="timeline-item completed">
                <div class="timeline-icon">✓</div>
                <div class="timeline-content">
                  <div class="timeline-title">简历投递</div>
                  <div class="timeline-time">{{ formatTime(app.applyTime) }}</div>
                </div>
              </div>
              <div class="timeline-item" :class="{ completed: app.status >= 1, current: app.status === 1 }">
                <div class="timeline-icon">✓</div>
                <div class="timeline-content">
                  <div class="timeline-title">简历筛选</div>
                  <div class="timeline-time" v-if="app.hrReviewTime">{{ formatTime(app.hrReviewTime) }}</div>
                </div>
              </div>
              <div class="timeline-item" :class="{ completed: app.status >= 2, current: app.status === 2 }">
                <div class="timeline-icon" :class="{ current: app.status === 2 }">
                  <svg v-if="app.status === 2" width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="8" cy="8" r="6" fill="#FF9500"/>
                    <path d="M8 4m0 0 0 4-4" stroke="#fff" stroke-width="2"/>
                  </svg>
                  <div v-else class="timeline-icon">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <circle cx="8" cy="8" r="6" fill="#E5E5EA"/>
                      <path d="M8 4m0 0 0 4-4" stroke="#fff" stroke-width="2"/>
                    </svg>
                  </div>
                  <div class="timeline-content">
                    <div class="timeline-title">面试安排</div>
                    <div class="timeline-time" v-if="app.interviewTime">{{ formatTime(app.interviewTime) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 个人信息 -->
      <div v-show="currentMenu === 'profile'" class="content-panel">
        <h2 class="panel-title">个人信息</h2>
        <div v-if="!hasCompleteProfile" class="empty-tip">
          <p>请先完善个人信息后再查看</p>
          <el-button type="primary" @click="currentMenu = 'profile'">去填写个人信息</el-button>
        </div>
        <div v-else>
          <div class="profile-summary">
            <div class="summary-item">
              <span class="summary-label">真实姓名：</span>
              <span class="summary-value">{{ profile.realName || '-' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">手机号码：</span>
              <span class="summary-value">{{ profile.phone || '-' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">邮箱：</span>
              <span class="summary-value">{{ profile.email || '-' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">学历：</span>
              <span class="summary-value">{{ getEducationLabel(profile.education) }}</span>
            </div>
          </div>
          <el-button type="primary" @click="goToProfileForm">编辑信息</el-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listPublishedJobs } from '@/api/recruit'
import { getMyApplications } from '@/api/recruit'

const router = useRouter()
const currentMenu = ref('jobs')
const jobList = ref([])
const applicationList = ref([])
const loading = ref(false)
const profile = ref({})

const getStatusType = (status) => {
  const statusMap = { 0: 'info', 1: 'success', 2: 'warning', 3: 'success' }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    0: '已投递',
    1: '已筛选',
    2: '面试中',
    3: '已通过'
  }
  return statusMap[status] || '待处理'
}

const getEducationLabel = (edu) => {
  const map = { 1: '高中', 2: '大专', 3: '本科', 4: '硕士', 5: '博士' }
  return map[edu] || '-'
}

const hasCompleteProfile = computed(() => {
  return !!(profile.realName && profile.phone && profile.email)
})

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  if (diff < 86400000) {
    return '今天'
  } else if (diff < 172800000) {
    const days = Math.floor(diff / 86400000)
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

const handleApply = (job) => {
  // 跳转到个人信息页面填写信息后再投递
  currentMenu.value = 'profile'
}

const goToProfileForm = () => {
  router.push('/candidate/profile')
}

const loadJobs = async () => {
  loading.value = true
  try {
    const res = await listPublishedJobs()
    jobList.value = res.data || []
  } catch (error) {
    console.error('加载招聘岗位失败:', error)
  } finally {
    loading.value = false
  }
}

const loadApplications = async () => {
  loading.value = true
  try {
    const res = await getMyApplications()
    applicationList.value = res.data || []
  } catch (error) {
    console.error('加载应聘记录失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadJobs()
  loadApplications()
})
</script>

<style scoped>
.candidate-layout {
  display: flex;
  height: 100vh;
  background: #F5F5F7;
}

.sidebar {
  width: 240px;
  background: #FFFFFF;
  border-right: 1px solid #E5E5EA;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  height: 64px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #E5E5EA;
}

.logo {
  flex-shrink: 0;
}

.logo-text {
  margin-left: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #1D1D1F;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  color: #1D1D1F;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
}

.nav-item:hover {
  background: #F5F5F7;
}

.nav-item.active {
  background: #F5F5F7;
  color: #007AFF;
}

.nav-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.nav-text {
  margin-left: 12px;
  font-size: 14px;
  font-weight: 500;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow-y: auto;
}

.content-panel {
  padding: 32px;
}

.panel-title {
  font-size: 24px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 24px;
}

.empty-tip {
  text-align: center;
  padding: 60px 20px;
  color: #86868B;
  font-size: 14px;
}

.job-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.job-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.job-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1D1D1F;
  margin: 0;
}

.job-info {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item .label {
  font-size: 14px;
  color: #86868B;
}

.info-item .value {
  font-size: 14px;
  font-weight: 500;
  color: #1D1D1F;
}

.job-description {
  background: #F5F5F7;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.job-description p {
  margin: 0;
  font-size: 14px;
  color: #1D1D1F;
  line-height: 1.6;
}

.job-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #E5E5EA;
}

.publish-time {
  font-size: 12px;
  color: #86868B;
}

.process-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.process-item {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.process-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.job-name {
  font-size: 16px;
  font-weight: 600;
  color: #1D1D1F;
}

.process-timeline {
  display: flex;
  padding-left: 16px;
}

.timeline-item {
  position: relative;
  padding-left: 32px;
  margin-bottom: 16px;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  width: 12px;
  height: 12px;
  background: #E5E5EA;
  border-radius: 50%;
}

.timeline-icon {
  position: absolute;
  left: -6px;
  top: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #007AFF;
  color: #FFFFFF;
  border-radius: 50%;
  font-size: 12px;
}

.timeline-content {
  margin-left: 12px;
}

.timeline-title {
  font-size: 14px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 4px;
}

.timeline-time {
  font-size: 12px;
  color: #86868B;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #E5E5EA;
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-label {
  font-size: 14px;
  color: #86868B;
}

.summary-value {
  font-size: 14px;
  font-weight: 500;
  color: #1D1D1F;
}
</style>
