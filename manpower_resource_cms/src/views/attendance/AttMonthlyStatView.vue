<template>
  <div class="att-monthly-stat-view">
    <el-card>
      <template #header>
        <div class="card-header"><span>月度考勤统计</span></div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="员工">
          <el-select v-model="searchForm.employeeId" placeholder="全部员工" clearable filterable>
            <el-option v-for="emp in employeeList" :key="emp.id"
              :label="`${emp.empName} (${emp.empCode})`" :value="emp.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年份">
          <el-date-picker v-model="searchForm.statYear" type="year" placeholder="选择年份"
            value-format="YYYY" style="width:120px" />
        </el-form-item>
        <el-form-item label="月份">
          <el-select v-model="searchForm.statMonth" placeholder="全部" clearable style="width:100px">
            <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column label="员工" width="150">
          <template #default="{ row }">{{ getEmployeeName(row.employeeId) }}</template>
        </el-table-column>
        <el-table-column label="月份" width="100" align="center">
          <template #default="{ row }">{{ row.statYear }}-{{ String(row.statMonth).padStart(2,'0') }}</template>
        </el-table-column>
        <el-table-column prop="workDays" label="应出勤" width="90" align="center" />
        <el-table-column prop="actualDays" label="实际出勤" width="90" align="center" />
        <el-table-column prop="lateCount" label="迟到次数" width="90" align="center" />
        <el-table-column prop="earlyCount" label="早退次数" width="90" align="center" />
        <el-table-column prop="absentCount" label="缺勤天数" width="90" align="center" />
        <el-table-column prop="leaveDays" label="请假天数" width="90" align="center" />
        <el-table-column prop="overtimeHours" label="加班(时)" width="90" align="center" />
        <el-table-column prop="travelDays" label="出差天数" width="90" align="center" />
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
          :total="pagination.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageMonthlyStat } from '@/api/att-stat'
import { listEmployee } from '@/api/employee'

const loading = ref(false)
const tableData = ref([])
const employeeList = ref([])

const searchForm = reactive({ employeeId: null, statYear: null, statMonth: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const getEmployeeName = (id) => {
  const emp = employeeList.value.find(e => e.id === id)
  return emp ? `${emp.empName} (${emp.empCode})` : id || '-'
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    if (params.statYear) params.statYear = parseInt(params.statYear)
    const res = await pageMonthlyStat(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleReset = () => {
  searchForm.employeeId = null; searchForm.statYear = null; searchForm.statMonth = null
  pagination.pageNum = 1; fetchData()
}

onMounted(async () => {
  try { const r = await listEmployee(); employeeList.value = r.data || [] } catch (e) { console.error(e) }
  fetchData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
