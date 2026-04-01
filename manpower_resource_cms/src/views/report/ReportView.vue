<template>
  <div class="report-view">
    <!-- 员工统计 -->
    <el-row :gutter="16">
      <el-col :span="6" v-for="item in employeeStats" :key="item.key">
        <el-card class="stat-card" :style="{ borderTop: `3px solid ${item.color}` }">
          <div class="stat-icon" :style="{ background: item.color }">
            <el-icon :size="24"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card>
          <template #header>部门人员分布</template>
          <div ref="deptChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>薪资年度趋势</span>
              <el-date-picker
                v-model="salaryYear"
                type="year"
                placeholder="选择年份"
                format="YYYY"
                @change="fetchSalaryTrend"
                style="width: 120px"
              />
            </div>
          </template>
          <div ref="salaryChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 考勤和请假统计 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>考勤月度统计</span>
              <el-date-picker
                v-model="attMonth"
                type="month"
                placeholder="选择月份"
                format="YYYY-MM"
                @change="fetchAttendance"
                style="width: 140px"
              />
            </div>
          </template>
          <div ref="attChartRef" style="height: 280px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>请假统计</span>
              <el-date-picker
                v-model="leaveYear"
                type="year"
                placeholder="选择年份"
                format="YYYY"
                @change="fetchLeaveStats"
                style="width: 120px"
              />
            </div>
          </template>
          <div ref="leaveChartRef" style="height: 280px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 薪资月度明细 + 审批统计 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>薪资月度统计</span>
              <el-date-picker
                v-model="salaryMonth"
                type="month"
                placeholder="选择月份"
                format="YYYY-MM"
                @change="fetchSalaryMonthly"
                style="width: 140px"
              />
            </div>
          </template>
          <div ref="salaryMonthlyChartRef" style="height: 280px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>审批统计</template>
          <div ref="approvalChartRef" style="height: 280px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, markRaw } from 'vue'
import { User, TrendCharts, Timer, Tickets } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getEmployeeOverview,
  getDeptDistribution,
  getAttendanceMonthly,
  getSalaryMonthly,
  getSalaryTrend,
  getLeaveStatistics,
  getApprovalStatistics,
} from '@/api/report'

const deptChartRef = ref(null)
const salaryChartRef = ref(null)
const attChartRef = ref(null)
const leaveChartRef = ref(null)

let deptChart = null
let salaryChart = null
let attChart = null
let leaveChart = null

const salaryMonthlyChartRef = ref(null)
const approvalChartRef = ref(null)
let salaryMonthlyChart = null
let approvalChart = null

const salaryYear = ref(new Date())
const salaryMonth = ref(new Date())
const attMonth = ref(new Date())
const leaveYear = ref(new Date())

const employeeStats = ref([
  { key: 'active', label: '在职员工', value: 0, color: '#409eff', icon: markRaw(User) },
  { key: 'probation', label: '试用期', value: 0, color: '#e6a23c', icon: markRaw(Timer) },
  { key: 'left', label: '离职员工', value: 0, color: '#f56c6c', icon: markRaw(Tickets) },
  { key: 'newHire', label: '本月入职', value: 0, color: '#67c23a', icon: markRaw(TrendCharts) },
])

// 获取员工概览
const fetchEmployeeOverview = async () => {
  try {
    const res = await getEmployeeOverview()
    const data = res.data || {}
    employeeStats.value[0].value = data.activeCount || 0
    employeeStats.value[1].value = data.probationCount || 0
    employeeStats.value[2].value = data.leftCount || 0
    employeeStats.value[3].value = data.newHireCount || 0
  } catch (error) {
    console.error(error)
  }
}

// 获取部门分布
const fetchDeptDistribution = async () => {
  try {
    const res = await getDeptDistribution()
    const data = res.data || []

    await nextTick()
    if (!deptChart) {
      deptChart = echarts.init(deptChartRef.value)
    }

    deptChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
      legend: { orient: 'vertical', left: 'left' },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          label: { show: true, formatter: '{b}: {c}人' },
          data: data.map((d) => ({ name: d.deptName, value: d.count })),
        },
      ],
    })
  } catch (error) {
    console.error(error)
  }
}

// 获取薪资趋势
const fetchSalaryTrend = async () => {
  try {
    const yearVal = salaryYear.value
      ? new Date(salaryYear.value).getFullYear()
      : new Date().getFullYear()
    const res = await getSalaryTrend(yearVal)
    const data = res.data || []

    await nextTick()
    if (!salaryChart) {
      salaryChart = echarts.init(salaryChartRef.value)
    }

    salaryChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.map((d) => `${d.month}月`) },
      yAxis: { type: 'value', name: '金额(元)' },
      series: [
        {
          name: '发放总额',
          type: 'bar',
          data: data.map((d) => d.totalAmount),
          itemStyle: { color: '#409eff' },
        },
      ],
    })
  } catch (error) {
    console.error(error)
  }
}

// 获取考勤统计
const fetchAttendance = async () => {
  try {
    const date = attMonth.value ? new Date(attMonth.value) : new Date()
    const res = await getAttendanceMonthly(date.getFullYear(), date.getMonth() + 1)
    const data = res.data || {}

    await nextTick()
    if (!attChart) {
      attChart = echarts.init(attChartRef.value)
    }

    attChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [
        {
          type: 'pie',
          radius: '60%',
          data: [
            { name: '正常', value: data.normalCount || 0, itemStyle: { color: '#67c23a' } },
            { name: '迟到', value: data.lateCount || 0, itemStyle: { color: '#e6a23c' } },
            { name: '早退', value: data.earlyCount || 0, itemStyle: { color: '#f56c6c' } },
            { name: '缺勤', value: data.absentCount || 0, itemStyle: { color: '#909399' } },
            { name: '请假', value: data.leaveCount || 0, itemStyle: { color: '#409eff' } },
          ],
        },
      ],
    })
  } catch (error) {
    console.error(error)
  }
}

// 获取请假统计
const fetchLeaveStats = async () => {
  try {
    const yearVal = leaveYear.value
      ? new Date(leaveYear.value).getFullYear()
      : new Date().getFullYear()
    const res = await getLeaveStatistics(yearVal)
    const data = res.data || {}

    await nextTick()
    if (!leaveChart) {
      leaveChart = echarts.init(leaveChartRef.value)
    }

    leaveChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [
        {
          type: 'pie',
          radius: '60%',
          data: [
            { name: '待审批', value: data.pendingCount || 0, itemStyle: { color: '#e6a23c' } },
            { name: '已通过', value: data.approvedCount || 0, itemStyle: { color: '#67c23a' } },
            { name: '已驳回', value: data.rejectedCount || 0, itemStyle: { color: '#f56c6c' } },
          ],
        },
      ],
    })
  } catch (error) {
    console.error(error)
  }
}

// 薪资月度统计
const fetchSalaryMonthly = async () => {
  try {
    const date = salaryMonth.value ? new Date(salaryMonth.value) : new Date()
    const res = await getSalaryMonthly(date.getFullYear(), date.getMonth() + 1)
    const data = res.data || {}

    await nextTick()
    if (!salaryMonthlyChart) {
      salaryMonthlyChart = echarts.init(salaryMonthlyChartRef.value)
    }

    salaryMonthlyChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [
        {
          type: 'pie',
          radius: '60%',
          data: [
            { name: '已发放', value: data.paidCount || 0, itemStyle: { color: '#67c23a' } },
            { name: '待确认', value: data.pendingCount || 0, itemStyle: { color: '#e6a23c' } },
            { name: '待生成', value: data.draftCount || 0, itemStyle: { color: '#909399' } },
          ],
        },
      ],
    })
  } catch (error) {
    console.error(error)
  }
}

// 审批统计
const fetchApprovalStats = async () => {
  try {
    const res = await getApprovalStatistics()
    const data = res.data || {}

    await nextTick()
    if (!approvalChart) {
      approvalChart = echarts.init(approvalChartRef.value)
    }

    approvalChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [
        {
          type: 'pie',
          radius: '60%',
          data: [
            { name: '待审批', value: data.pendingCount || 0, itemStyle: { color: '#e6a23c' } },
            { name: '已通过', value: data.approvedCount || 0, itemStyle: { color: '#67c23a' } },
            { name: '已拒绝', value: data.rejectedCount || 0, itemStyle: { color: '#f56c6c' } },
          ],
        },
      ],
    })
  } catch (error) {
    console.error(error)
  }
}

// 窗口resize处理
const handleResize = () => {
  deptChart?.resize()
  salaryChart?.resize()
  attChart?.resize()
  leaveChart?.resize()
  salaryMonthlyChart?.resize()
  approvalChart?.resize()
}

onMounted(() => {
  fetchEmployeeOverview()
  fetchDeptDistribution()
  fetchSalaryTrend()
  fetchSalaryMonthly()
  fetchAttendance()
  fetchLeaveStats()
  fetchApprovalStats()

  window.addEventListener('resize', handleResize)
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
}
.stat-content {
  flex: 1;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
