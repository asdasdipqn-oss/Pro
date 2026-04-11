<template>
  <div class="candidate-process">
    <div v-loading="loading">
      <el-empty v-if="applicationList.length === 0 && !loading" description="暂无投递记录" />

      <div v-for="app in applicationList" :key="app.id" class="process-card">
        <div class="card-header">
          <div class="job-info">
            <h3>{{ app.jobName }}</h3>
            <el-tag :type="getStatusType(app.status)">{{ getStatusText(app.status) }}</el-tag>
          </div>
          <div class="apply-time">投递时间：{{ formatTime(app.applyTime) }}</div>
        </div>

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
          <div class="timeline-line" :class="{ active: app.status >= 0 }"></div>

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
          <div class="timeline-line" :class="{ active: app.status >= 1 }"></div>

          <div class="timeline-item" :class="{ active: app.status >= 2 }">
            <div class="timeline-icon">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="timeline-content">
              <h4>面试安排</h4>
              <p v-if="app.interviewTime">{{ formatTime(app.interviewTime) }}</p>
              <p v-else>待安排</p>
            </div>
          </div>
          <div class="timeline-line" :class="{ active: app.status >= 2 }"></div>

          <div class="timeline-item" :class="{ active: app.status >= 3 }">
            <div class="timeline-icon">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="timeline-content">
              <h4>{{ getStatusText(app.status) }}</h4>
              <p v-if="app.completeTime">{{ formatTime(app.completeTime) }}</p>
              <p v-else>待完成</p>
            </div>
          </div>
          <div class="timeline-line" :class="{ active: app.status >= 3 }"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, View, Clock, CircleCheck } from '@element-plus/icons-vue'
import { getMyApplications } from '@/api/recruit'
import request from '@/utils/request'

const loading = ref(false)
const applicationList = ref([])

const getStatusType = (status) => {
  const statusMap = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'success'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    0: '已投递',
    1: '筛选中',
    2: '面试中',
    3: '已通过'
  }
  return statusMap[status] || '待处理'
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN')
}

const fetchApplications = async () => {
  loading.value = true
  try {
    const res = await getMyApplications()
    applicationList.value = res.data || []
    // 按申请时间倒序排列
    applicationList.value.sort((a, b) => new Date(b.applyTime) - new Date(a.applyTime))
  } catch (error) {
    console.error('获取申请记录失败:', error)
    ElMessage.error('获取申请记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchApplications)
</script>

<style scoped>
.candidate-process {
  padding: 24px;
}

.process-card {
  background: #FFFFFF;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #E5E5EA;
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
  text-align: right;
}

.timeline {
  padding-left: 30px;
  position: relative;
}

.timeline-item {
  position: relative;
  padding-bottom: 32px;
}

.timeline-icon {
  position: absolute;
  left: -15px;
  top: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F5F5F7;
  border-radius: 50%;
  color: #FFFFFF;
  z-index: 1;
}

.timeline-line {
  position: absolute;
  left: 0;
  top: 30px;
  width: 2px;
  height: 100%;
  background: #E5E5EA;
  z-index: 0;
}

.timeline-line.active {
  background: #007AFF;
}

.timeline-content {
  padding-left: 45px;
}

.timeline-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1D1D1F;
  margin: 0 0 8px;
}

.timeline-content p {
  font-size: 14px;
  color: #86868B;
  margin: 0;
}
</style>
