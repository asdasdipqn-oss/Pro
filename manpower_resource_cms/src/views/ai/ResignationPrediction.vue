<template>
  <div class="resignation-prediction">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-info">
        <h2>AI离职率预测</h2>
        <p>基于考勤和请假数据的智能分析</p>
      </div>
    </div>

    <!-- 预测输入区 -->
    <div class="prediction-input-card">
      <h3>选择员工进行预测</h3>
      <div class="input-row">
        <div class="input-item">
          <label>员工</label>
          <el-select
            v-model="predictForm.employeeId"
            placeholder="选择员工"
            filterable
            style="width: 300px;"
          >
            <el-option
              v-for="emp in employeeList"
              :key="emp.id"
              :label="`${emp.empName} (${emp.empCode})`"
              :value="emp.id"
            />
          </el-select>
        </div>
        <div class="input-item">
          <label>分析月数</label>
          <el-input-number
            v-model="predictForm.months"
            :min="1"
            :max="12"
            placeholder="分析月数"
            style="width: 150px;"
          />
        </div>
        <el-button type="primary" @click="handlePredict" :loading="predicting">
          开始预测
        </el-button>
        <el-button type="success" @click="handleExport" :disabled="!predictionResult" :loading="exporting">
          {{ exporting ? '导出中...' : '导出报告' }}
        </el-button>
      </div>
    </div>

    <!-- 预测结果展示 -->
    <div v-if="predictionResult" class="result-container">
      <!-- 员工基础信息 -->
      <div class="employee-info-card">
        <h3>员工信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">姓名</span>
            <span class="value">{{ predictionResult.employeeName }}</span>
          </div>
          <div class="info-item">
            <span class="label">部门</span>
            <span class="value">{{ predictionResult.departmentName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">岗位</span>
            <span class="value">{{ predictionResult.positionName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">分析周期</span>
            <span class="value">近{{ predictionResult.analysisMonths }}个月</span>
          </div>
        </div>
      </div>

      <!-- 风险评估结果 -->
      <div class="risk-assessment-card">
        <h3>离职风险评估</h3>
        <div class="risk-content">
          <div class="risk-circle" :class="getRiskClass(predictionResult.riskLevel)">
            <div class="risk-level">{{ getRiskLevelText(predictionResult.riskLevel) }}</div>
            <div class="risk-probability">{{ predictionResult.resignationProbability }}%</div>
            <div class="risk-label">离职概率</div>
          </div>
          <div class="risk-details">
            <div class="risk-badge" :class="getRiskBadgeClass(predictionResult.riskLevel)">
              {{ getRiskLevelText(predictionResult.riskLevel) }}风险
            </div>
            <div class="risk-desc">
              根据员工近{{ predictionResult.analysisMonths }}个月的考勤和请假数据分析，
              该员工的离职风险等级为<strong>{{ getRiskLevelText(predictionResult.riskLevel) }}</strong>，
              建议管理层重点关注。
            </div>
          </div>
        </div>
      </div>

      <!-- 考勤统计 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon workdays">📅</div>
          <div class="stat-content">
            <div class="stat-label">应出勤天数</div>
            <div class="stat-value">{{ predictionResult.totalWorkDays }}天</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon actualdays">✅</div>
          <div class="stat-content">
            <div class="stat-label">实际出勤天数</div>
            <div class="stat-value">{{ predictionResult.actualAttendDays }}天</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon attendance">📊</div>
          <div class="stat-content">
            <div class="stat-label">出勤率</div>
            <div class="stat-value">{{ predictionResult.attendanceRate }}%</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon late">⏰</div>
          <div class="stat-content">
            <div class="stat-label">迟到次数</div>
            <div class="stat-value">{{ predictionResult.lateCount }}次</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon early">🏃</div>
          <div class="stat-content">
            <div class="stat-label">早退次数</div>
            <div class="stat-value">{{ predictionResult.earlyLeaveCount }}次</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon absent">❌</div>
          <div class="stat-content">
            <div class="stat-label">缺勤次数</div>
            <div class="stat-value">{{ predictionResult.absentCount }}次</div>
          </div>
        </div>
      </div>

      <!-- AI分析结果 -->
      <div class="analysis-card">
        <h3>
          AI分析
          <span v-if="streaming" class="streaming-indicator">
            <span class="dot"></span>正在分析...
          </span>
        </h3>
        <div class="analysis-content">
          <span v-if="streamingText">{{ streamingText }}</span>
          <span v-else>{{ predictionResult.aiAnalysis }}</span>
          <span v-if="streaming" class="cursor">|</span>
        </div>
      </div>

      <!-- 管理建议 -->
      <div class="suggestions-card">
        <h3>管理建议</h3>
        <div class="suggestions-content">
          {{ predictionResult.suggestions }}
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">🤖</div>
      <p>选择员工并开始预测，AI将为您分析离职风险</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from '@/utils/request'

// 数据
const employeeList = ref([])
const predicting = ref(false)
const streaming = ref(false)
const streamingText = ref('')
const predictionResult = ref(null)
const exporting = ref(false)
let eventSource = null

const predictForm = ref({
  employeeId: null,
  months: 6
})

// 加载员工列表
const fetchEmployeeList = async () => {
  try {
    const { data } = await axios.get('/emp/employee/list')
    employeeList.value = data
  } catch (error) {
    ElMessage.error('加载员工列表失败')
  }
}

  // 执行预测 - 使用SSE流式接收
const handlePredict = () => {
  if (!predictForm.value.employeeId) {
    ElMessage.warning('请选择员工')
    return
  }

  // 关閉之前的连接
  if (eventSource) {
    eventSource.close()
  }

  predicting.value = true
  streaming.value = true
  streamingText.value = ''
  predictionResult.value = null

  // 获取token
  const token = localStorage.getItem('token')
  
  // 构建 SSE URL - 使用相对路径，让浏览器自动处理CORS代理
  const url = `/api/ai/resignation/predict/stream?employeeId=${predictForm.value.employeeId}&months=${predictForm.value.months}&token=${token}`

  eventSource = new EventSource(url)

  // 接收基础统计数据
  eventSource.addEventListener('stats', (event) => {
    try {
      const stats = JSON.parse(event.data)
      predictionResult.value = stats
    } catch (e) {
      console.error('解析统计数据失败', e)
    }
  })

  // 接收AI流式token
  eventSource.addEventListener('token', (event) => {
    streamingText.value += event.data
  })

  // 接收最终结果
  eventSource.addEventListener('result', (event) => {
    try {
      const result = JSON.parse(event.data)
      predictionResult.value = result
      streamingText.value = ''
    } catch (e) {
      console.error('解析结果失败', e)
    }
  })

  // 完成
  eventSource.addEventListener('done', () => {
    streaming.value = false
    predicting.value = false
    eventSource.close()
    eventSource = null
    ElMessage.success('预测完成')
  })

  // 错误处理
  eventSource.addEventListener('error', (event) => {
    if (event.data) {
      ElMessage.error('预测失败: ' + event.data)
    }
  })

  eventSource.onerror = (error) => {
    console.error('SSE连接错误', error)
    streaming.value = false
    predicting.value = false
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    if (!predictionResult.value) {
      ElMessage.error('连接失败，请重试')
    }
  }
}

// 组件卸载时关闭连接
onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
  }
})

// 导出报告
const handleExport = async () => {
  if (!predictionResult.value) {
    ElMessage.warning('请先进行预测')
    return
  }
  exporting.value = true
  try {
    const response = await axios.get(
      `/ai/resignation/export/${predictForm.value.employeeId}`,
      {
        params: { months: predictForm.value.months },
        responseType: 'blob',
        timeout: 60000 // 1分钟超时
      }
    )
    // 创建下载链接
    const blob = new Blob([response], { 
      type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${predictionResult.value.employeeName}-离职风险评估报告.docx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('报告导出成功')
  } catch (error) {
    console.error('导出失败', error)
    if (error.code === 'ECONNABORTED') {
      ElMessage.error('导出超时，请稍后重试')
    } else {
      ElMessage.error('导出报告失败')
    }
  } finally {
    exporting.value = false
  }
}

// 获取风险等级样式类
const getRiskClass = (level) => {
  return {
    'risk-low': level === 'LOW',
    'risk-medium': level === 'MEDIUM',
    'risk-high': level === 'HIGH'
  }
}

// 获取风险徽章样式类
const getRiskBadgeClass = (level) => {
  return {
    'badge-low': level === 'LOW',
    'badge-medium': level === 'MEDIUM',
    'badge-high': level === 'HIGH'
  }
}

// 获取风险等级文本
const getRiskLevelText = (level) => {
  const levelMap = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高'
  }
  return levelMap[level] || '未知'
}

onMounted(() => {
  fetchEmployeeList()
})
</script>

<style scoped>
.resignation-prediction {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
}

.header-info h2 {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 8px 0;
}

.header-info p {
  font-size: 15px;
  color: #86868b;
  margin: 0;
}

/* 预测输入卡片 */
.prediction-input-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.prediction-input-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 20px 0;
}

.input-row {
  display: flex;
  align-items: flex-end;
  gap: 16px;
}

.input-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-item label {
  font-size: 14px;
  font-weight: 500;
  color: #1d1d1f;
}

/* 结果容器 */
.result-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 员工信息卡片 */
.employee-info-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.employee-info-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 20px 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item .label {
  font-size: 13px;
  color: #86868b;
}

.info-item .value {
  font-size: 16px;
  font-weight: 500;
  color: #1d1d1f;
}

/* 风险评估卡片 */
.risk-assessment-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.risk-assessment-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 24px 0;
}

.risk-content {
  display: flex;
  align-items: center;
  gap: 48px;
}

.risk-circle {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 4px solid;
}

.risk-circle.risk-low {
  border-color: #34c759;
  background: rgba(52, 199, 89, 0.1);
}

.risk-circle.risk-medium {
  border-color: #ff9500;
  background: rgba(255, 149, 0, 0.1);
}

.risk-circle.risk-high {
  border-color: #ff3b30;
  background: rgba(255, 59, 48, 0.1);
}

.risk-level {
  font-size: 20px;
  font-weight: 600;
}

.risk-circle.risk-low .risk-level {
  color: #34c759;
}

.risk-circle.risk-medium .risk-level {
  color: #ff9500;
}

.risk-circle.risk-high .risk-level {
  color: #ff3b30;
}

.risk-probability {
  font-size: 32px;
  font-weight: 700;
  color: #1d1d1f;
}

.risk-label {
  font-size: 13px;
  color: #86868b;
}

.risk-details {
  flex: 1;
}

.risk-badge {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 16px;
}

.risk-badge.badge-low {
  background: rgba(52, 199, 89, 0.15);
  color: #34c759;
}

.risk-badge.badge-medium {
  background: rgba(255, 149, 0, 0.15);
  color: #ff9500;
}

.risk-badge.badge-high {
  background: rgba(255, 59, 48, 0.15);
  color: #ff3b30;
}

.risk-desc {
  font-size: 15px;
  line-height: 1.6;
  color: #515154;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.workdays {
  background: rgba(64, 158, 255, 0.1);
}

.stat-icon.actualdays {
  background: rgba(52, 199, 89, 0.1);
}

.stat-icon.attendance {
  background: rgba(0, 122, 255, 0.1);
}

.stat-icon.late {
  background: rgba(255, 149, 0, 0.1);
}

.stat-icon.early {
  background: rgba(255, 59, 48, 0.1);
}

.stat-icon.absent {
  background: rgba(245, 108, 108, 0.1);
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1d1d1f;
}

/* AI分析卡片 */
.analysis-card,
.suggestions-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.analysis-card h3,
.suggestions-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 16px 0;
}

.analysis-content,
.suggestions-content {
  font-size: 15px;
  line-height: 1.8;
  color: #515154;
  white-space: pre-line;
}

/* 空状态 */
.empty-state {
  background: #fff;
  border-radius: 16px;
  padding: 80px 24px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 15px;
  color: #86868b;
  margin: 0;
}

/* 流式输出样式 */
.streaming-indicator {
  font-size: 14px;
  font-weight: normal;
  color: #007aff;
  margin-left: 12px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.streaming-indicator .dot {
  width: 8px;
  height: 8px;
  background: #007aff;
  border-radius: 50%;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.cursor {
  animation: blink 0.8s infinite;
  color: #007aff;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}
</style>
