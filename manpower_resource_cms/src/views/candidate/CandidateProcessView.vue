<template>
  <div class="candidate-process">
    <div class="header">
      <h1>招聘流程</h1>
      <p>查看您的应聘进度和面试安排</p>
    </div>

    <div v-loading="loading">
      <el-empty v-if="applicationList.length === 0 && !loading" description="暂无投递记录">
        <el-button type="primary" @click="goToJobs">去投递简历</el-button>
      </el-empty>

      <div v-for="app in applicationList" :key="app.id" class="process-card">
        <div class="card-header">
          <div class="job-info">
            <h3>{{ app.jobName }}</h3>
            <el-tag :type="getStatusType(app.status)">{{ getStatusText(app.status) }}</el-tag>
          </div>
          <div class="apply-time">投递时间：{{ formatTime(app.applyTime) }}</div>
        </div>

        <div class="card-body">
          <!-- 流程时间线 -->
          <div class="timeline">
            <div class="timeline-item" :class="{ active: app.status >= 0 }">
              <div class="timeline-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="timeline-content">
                <h4>简历投递</h4>
                <p>{{ formatTime(app.applyTime) }}</p>
              </div>
            </div>

            <div class="timeline-line" :class="{ active: app.status >= 1 }"></div>

            <div class="timeline-item" :class="{ active: app.status >= 1 }">
              <div class="timeline-icon">
                <el-icon><View /></el-icon>
              </div>
              <div class="timeline-content">
                <h4>简历筛选</h4>
                <p v-if="app.hrReviewTime">{{ formatTime(app.hrReviewTime) }}</p>
                <p v-else>等待筛选</p>
              </div>
            </div>

            <div class="timeline-line" :class="{ active: app.status >= 2 }"></div>

            <div class="timeline-item" :class="{ active: app.status >= 2 }">
              <div class="timeline-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="timeline-content">
                <h4>面试环节</h4>
                <p v-if="interviews[app.id] && interviews[app.id].length > 0">
                  已安排 {{ interviews[app.id].length }} 轮面试
                </p>
                <p v-else>待安排</p>
              </div>
            </div>

            <div class="timeline-line" :class="{ active: app.status >= 3 }"></div>

            <div class="timeline-item" :class="{ active: app.status >= 4, rejected: app.status === 5 }">
              <div class="timeline-icon">
                <el-icon><SuccessFilled /></el-icon>
              </div>
              <div class="timeline-content">
                <h4>录用结果</h4>
                <p v-if="app.status === 4">恭喜您已通过面试！</p>
                <p v-else-if="app.status === 5">很遗憾，您未通过本次面试</p>
                <p v-else>待定</p>
              </div>
            </div>
          </div>

          <!-- 面试详情 -->
          <div v-if="interviews[app.id] && interviews[app.id].length > 0" class="interview-detail">
            <h4>面试安排</h4>
            <div v-for="interview in interviews[app.id]" :key="interview.id" class="interview-item">
              <div class="interview-header">
                <span class="round">第{{ interview.interviewRound }}轮</span>
                <el-tag :type="getInterviewStatusType(interview.status)">
                  {{ getInterviewStatusText(interview.status) }}
                </el-tag>
              </div>
              <div class="interview-info">
                <div class="info-row">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatDateTime(interview.interviewTime) }}</span>
                </div>
                <div class="info-row" v-if="interview.interviewAddress">
                  <el-icon><Location /></el-icon>
                  <span>{{ interview.interviewAddress }}</span>
                </div>
                <div class="info-row">
                  <el-icon><Monitor /></el-icon>
                  <span>{{ getInterviewTypeText(interview.interviewType) }}</span>
                </div>
              </div>
              <div v-if="interview.status === 1 && interview.evaluation" class="interview-result">
                <h5>面试评价</h5>
                <p>{{ interview.evaluation }}</p>
                <div v-if="interview.score" class="score">
                  评分：<el-tag>{{ interview.score }}分</el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyApplications, getInterviewsByApplication } from '@/api/recruit'
import { Document, View, ChatDotRound, SuccessFilled, Clock, Location, Monitor } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const applicationList = ref([])
const interviews = ref({})

const getStatusType = (status) => {
  const typeMap = {
    0: 'info',      // 待处理
    1: 'warning',   // 已查看
    2: 'primary',   // 面试中
    3: 'primary',   // 面试中
    4: 'success',    // 已录用
    5: 'danger'     // 未通过
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    0: '待处理',
    1: '已查看',
    2: '面试中',
    3: '面试中',
    4: '已录用',
    5: '未通过'
  }
  return textMap[status] || '未知'
}

const getInterviewStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'info'
  }
  return typeMap[status] || 'info'
}

const getInterviewStatusText = (status) => {
  const textMap = {
    0: '待面试',
    1: '已完成',
    2: '已取消'
  }
  return textMap[status] || '未知'
}

const getInterviewTypeText = (type) => {
  const textMap = {
    1: '现场面试',
    2: '电话面试',
    3: '视频面试'
  }
  return textMap[type] || '未知'
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

const formatDateTime = (timeStr) => {
  if (!timeStr) return '-'
  const time = new Date(timeStr)
  return time.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    weekday: 'long'
  })
}

const loadApplications = async () => {
  loading.value = true
  try {
    console.log('[CandidateProcess] 开始加载投递记录...')
    const res = await getMyApplications()
    console.log('[CandidateProcess] 响应:', res)
    if (res && res.code === 200) {
      applicationList.value = res.data || []
      console.log('[CandidateProcess] 解析数据:', applicationList.value)
      // 加载每个投递记录的面试详情
      for (const app of applicationList.value) {
        if (app.id) {
          console.log('[CandidateProcess] 加载投递记录的面试详情，applicationId:', app.id)
          try {
            const interviewRes = await getInterviewsByApplication(app.id)
            console.log('[CandidateProcess] 面试详情响应:', interviewRes)
            if (interviewRes && interviewRes.code === 200) {
              interviews.value[app.id] = interviewRes.data || []
            }
          } catch (e) {
            console.error('[CandidateProcess] 加载面试详情失败:', e)
            interviews.value[app.id] = []
          }
        }
      }
    } else {
      console.error('[CandidateProcess] 响应状态异常:', res?.code, res?.message)
    }
  } catch (error) {
    console.error('[CandidateProcess] 加载投递记录失败:', error)
    console.error('[CandidateProcess] 错误详情:', error.response?.data)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const goToJobs = () => {
  router.push('/candidate/jobs')
}

onMounted(() => {
  loadApplications()
})
</script>

<style scoped>
.candidate-process {
  padding-top: 24px;
  padding-left: 24px;
  padding-right: 24px;
  padding-bottom: 24px;
}

.header {
  text-align: center;
  margin-bottom: 32px;
}

.header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 8px;
}

.header p {
  font-size: 16px;
  color: #86868B;
}

.process-card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #E5E5EA;
}

.job-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.job-info h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1D1D1F;
  margin: 0;
}

.apply-time {
  font-size: 14px;
  color: #86868B;
}

.card-body {
  padding: 0 20px;
}

.timeline {
  display: flex;
  align-items: flex-start;
  margin-bottom: 32px;
}

.timeline-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100px;
  flex-shrink: 0;
}

.timeline-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #F5F5F7;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #86868B;
  font-size: 20px;
  transition: all 0.3s;
}

.timeline-item.active .timeline-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #FFFFFF;
}

.timeline-item.rejected .timeline-icon {
  background: #F56C6C;
  color: #FFFFFF;
}

.timeline-content {
  margin-top: 12px;
  text-align: center;
}

.timeline-content h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1D1D1F;
  margin: 0 0 4px;
}

.timeline-content p {
  font-size: 12px;
  color: #86868B;
  margin: 0;
  white-space: nowrap;
}

.timeline-line {
  flex: 1;
  height: 2px;
  background: #E5E5EA;
  margin-top: 24px;
  min-width: 40px;
}

.timeline-line.active {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.interview-detail {
  background: #F5F5F7;
  border-radius: 16px;
  padding: 20px;
}

.interview-detail h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1D1D1F;
  margin: 0 0 16px;
}

.interview-item {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.interview-item:last-child {
  margin-bottom: 0;
}

.interview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.round {
  font-size: 14px;
  font-weight: 600;
  color: #1D1D1F;
}

.interview-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #86868B;
}

.info-row .el-icon {
  font-size: 16px;
}

.interview-result {
  padding-top: 12px;
  border-top: 1px solid #E5E5EA;
}

.interview-result h5 {
  font-size: 14px;
  font-weight: 600;
  color: #1D1D1F;
  margin: 0 0 8px;
}

.interview-result p {
  font-size: 14px;
  color: #1D1D1F;
  line-height: 1.6;
  margin: 0 0 8px;
}

.score {
  display: inline-block;
}
</style>
