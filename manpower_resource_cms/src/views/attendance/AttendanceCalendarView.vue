<template>
  <div class="attendance-calendar-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考勤日历 - {{ year }}年{{ month }}月</span>
          <div class="actions">
            <el-date-picker
              v-model="monthPicker"
              type="month"
              placeholder="选择月份"
              format="YYYY年MM月"
              @change="fetchData"
              style="width: 160px; margin-right: 12px"
            />
            <el-button type="success" @click="handleExport">导出Excel</el-button>
          </div>
        </div>
      </template>

      <!-- 图例 -->
      <div class="legend">
        <span class="legend-item"><span class="dot normal"></span>正常</span>
        <span class="legend-item"><span class="dot half"></span>半天</span>
        <span class="legend-item"><span class="dot absent"></span>缺勤</span>
        <span class="legend-item"><span class="dot leave"></span>请假</span>
        <span class="legend-item"><span class="dot weekend"></span>周末</span>
        <span class="legend-item"><span class="dot overtime"></span>加班</span>
        <span class="legend-item"><span class="dot future"></span>未到</span>
      </div>

      <!-- 考勤表格 -->
      <div class="calendar-table-wrapper" v-loading="loading">
        <table class="calendar-table" v-if="calendarData">
          <thead>
            <tr>
              <th class="sticky-col col-code">工号</th>
              <th class="sticky-col col-name">姓名</th>
              <th v-for="day in daysInMonth" :key="day" :class="{ weekend: isWeekend(day) }">
                {{ day }}
              </th>
              <th class="stat-col">出勤</th>
              <th class="stat-col">缺勤</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in calendarData.rows" :key="row.employeeId">
              <td class="sticky-col col-code">{{ row.empCode }}</td>
              <td class="sticky-col col-name">{{ row.empName }}</td>
              <td v-for="day in daysInMonth" :key="day" :class="getCellClass(row.days[day])">
                {{ getCellText(row.days[day]) }}
              </td>
              <td class="stat-col">{{ countAttend(row.days) }}</td>
              <td class="stat-col absent-count">{{ countAbsent(row.days) }}</td>
            </tr>
          </tbody>
        </table>
        <el-empty v-else description="暂无数据" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCalendarData, exportAttendance } from '@/api/attendance'

const loading = ref(false)
const monthPicker = ref(new Date())
const calendarData = ref(null)

const year = computed(() =>
  monthPicker.value ? new Date(monthPicker.value).getFullYear() : new Date().getFullYear(),
)
const month = computed(() =>
  monthPicker.value ? new Date(monthPicker.value).getMonth() + 1 : new Date().getMonth() + 1,
)
const daysInMonth = computed(() => calendarData.value?.daysInMonth || 31)

const isWeekend = (day) => {
  const date = new Date(year.value, month.value - 1, day)
  return date.getDay() === 0 || date.getDay() === 6
}

const getCellClass = (status) => {
  switch (status) {
    case -1:
      return 'weekend'
    case -2:
      return 'future'
    case 0:
      return 'absent'
    case 1:
      return 'normal'
    case 2:
      return 'half'
    case 3:
      return 'half'
    case 4:
      return 'overtime'
    case 5:
      return 'leave'
    default:
      return ''
  }
}

const getCellText = (status) => {
  switch (status) {
    case -1:
      return '休'
    case -2:
      return '-'
    case 0:
      return '缺'
    case 1:
      return '√'
    case 2:
      return '上'
    case 3:
      return '下'
    case 4:
      return '加'
    case 5:
      return '假'
    default:
      return ''
  }
}

const countAttend = (days) => {
  return Object.values(days).filter((s) => s === 1 || s === 2 || s === 3 || s === 4).length
}

const countAbsent = (days) => {
  return Object.values(days).filter((s) => s === 0).length
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCalendarData(year.value, month.value)
    calendarData.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleExport = async () => {
  try {
    const res = await exportAttendance(year.value, month.value)
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${year.value}年${month.value}月考勤记录.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error(error)
    ElMessage.error('导出失败')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.actions {
  display: flex;
  align-items: center;
}
.legend {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
.dot {
  width: 16px;
  height: 16px;
  border-radius: 3px;
}
.dot.normal {
  background: #67c23a;
}
.dot.half {
  background: #e6a23c;
}
.dot.absent {
  background: #f56c6c;
}
.dot.leave {
  background: #9c27b0;
}
.dot.weekend {
  background: #909399;
}
.dot.overtime {
  background: #409eff;
}
.dot.future {
  background: #dcdfe6;
}

.calendar-table-wrapper {
  overflow-x: auto;
  max-height: calc(100vh - 300px);
}
.calendar-table {
  border-collapse: collapse;
  font-size: 13px;
  min-width: 100%;
}
.calendar-table th,
.calendar-table td {
  border: 1px solid #ebeef5;
  padding: 8px 4px;
  text-align: center;
  white-space: nowrap;
  min-width: 36px;
}
.calendar-table th {
  background: #f5f7fa;
  font-weight: 500;
  position: sticky;
  top: 0;
  z-index: 2;
}
.calendar-table th.weekend {
  background: #e0e0e0;
}
.sticky-col {
  position: sticky;
  background: #fff;
  z-index: 1;
}
.col-code {
  left: 0;
  min-width: 80px !important;
}
.col-name {
  left: 80px;
  min-width: 80px !important;
  border-right: 2px solid #dcdfe6 !important;
}
.calendar-table th.sticky-col {
  z-index: 3;
}
.stat-col {
  min-width: 50px !important;
  font-weight: 500;
}
.absent-count {
  color: #f56c6c;
}

/* 状态颜色 */
.calendar-table td.normal {
  background: #e1f3d8;
  color: #67c23a;
  font-weight: bold;
}
.calendar-table td.half {
  background: #faecd8;
  color: #e6a23c;
}
.calendar-table td.absent {
  background: #fde2e2;
  color: #f56c6c;
  font-weight: bold;
}
.calendar-table td.weekend {
  background: #f0f0f0;
  color: #909399;
}
.calendar-table td.future {
  background: #fafafa;
  color: #c0c4cc;
}
.calendar-table td.overtime {
  background: #d9ecff;
  color: #409eff;
  font-weight: bold;
}
.calendar-table td.overtime-half {
  background: #ecf5ff;
  color: #409eff;
}
.calendar-table td.leave {
  background: #f3e5f5;
  color: #9c27b0;
  font-weight: bold;
}
</style>
