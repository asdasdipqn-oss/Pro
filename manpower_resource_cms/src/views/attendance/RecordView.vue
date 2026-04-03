<template>
  <div class="record-view">
    <el-card>
      <template #header>打卡记录</template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="月份">
          <el-date-picker
            v-model="month"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            @change="fetchData"
          />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="clockDate" label="日期" width="120" />
        <el-table-column prop="clockType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.clockType === 1 ? 'primary' : 'success'">
              {{ row.clockType === 1 ? '上班' : '下班' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="clockTime" label="打卡时间" width="180">
          <template #default="{ row }">
            {{ row.clockTime ? row.clockTime.substring(11, 19) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="lateMinutes" label="迟到(分)" width="90" align="center">
          <template #default="{ row }">
            <span :class="{ 'late-text': row.lateMinutes > 0 }">
              {{ row.lateMinutes ? row.lateMinutes : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="earlyMinutes" label="早退(分)" width="90" align="center">
          <template #default="{ row }">
            <span :class="{ 'early-text': row.earlyMinutes > 0 }">
              {{ row.earlyMinutes ? row.earlyMinutes : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="clockAddress" label="打卡地点" />
        <el-table-column prop="locationStatus" label="定位状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.locationStatus === 1 ? 'success' : 'danger'">
              {{ row.locationStatus === 1 ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMonthRecords } from '@/api/attendance'

const loading = ref(false)
const tableData = ref([])
const month = ref(new Date())
const dateRange = ref([])
const allMonthData = ref([])

const fetchData = async () => {
  if (!month.value) return
  loading.value = true
  try {
    const date = new Date(month.value)
    const res = await getMonthRecords(date.getFullYear(), date.getMonth() + 1)
    allMonthData.value = res.data || []
    filterByDateRange()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleDateChange = () => {
  filterByDateRange()
}

const filterByDateRange = () => {
  if (!dateRange.value || dateRange.value.length === 0) {
    tableData.value = allMonthData.value
    return
  }

  const [startDate, endDate] = dateRange.value
  const start = new Date(startDate).setHours(0, 0, 0, 0)
  const end = new Date(endDate).setHours(23, 59, 59, 999)

  tableData.value = allMonthData.value.filter(row => {
    const rowDate = new Date(row.clockDate).getTime()
    return rowDate >= start && rowDate <= end
  })
}

onMounted(fetchData)
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}
.late-text,
.early-text {
  color: #e6a23c;
  font-weight: bold;
}
</style>
