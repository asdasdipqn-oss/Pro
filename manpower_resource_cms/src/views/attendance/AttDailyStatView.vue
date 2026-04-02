<template>
  <div class="att-daily-stat-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>日考勤统计</span>
          <el-button type="success" size="small" @click="handleGenerate" :loading="generating">生成统计数据</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="员工">
          <el-select v-model="searchForm.employeeId" placeholder="全部员工" clearable filterable>
            <el-option v-for="emp in employeeList" :key="emp.id"
              :label="`${emp.empName} (${emp.empCode})`" :value="emp.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="未打卡" :value="0" />
            <el-option label="正常" :value="1" />
            <el-option label="迟到" :value="2" />
            <el-option label="早退" :value="3" />
            <el-option label="迟到+早退" :value="4" />
            <el-option label="缺勤" :value="5" />
            <el-option label="请假" :value="6" />
            <el-option label="出差" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="searchForm.startDate" type="date" placeholder="开始日期"
            value-format="YYYY-MM-DD" style="width:150px" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="searchForm.endDate" type="date" placeholder="结束日期"
            value-format="YYYY-MM-DD" style="width:150px" />
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
        <el-table-column prop="statDate" label="日期" width="120" />
        <el-table-column prop="clockInTime" label="上班打卡" width="170" />
        <el-table-column prop="clockOutTime" label="下班打卡" width="170" />
        <el-table-column prop="workDuration" label="工作时长(分)" width="110" align="center" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="dailyStatusTag[row.status]" size="small">{{ dailyStatusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lateMinutes" label="迟到(分)" width="90" align="center" />
        <el-table-column prop="earlyMinutes" label="早退(分)" width="90" align="center" />
        <el-table-column label="工作日" width="80" align="center">
          <template #default="{ row }">{{ row.isWorkday === 1 ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
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
import { ElMessage } from 'element-plus'
import { pageDailyStat, generateDailyStats } from '@/api/att-stat'
import { listEmployee } from '@/api/employee'

const loading = ref(false)
const generating = ref(false)
const tableData = ref([])
const employeeList = ref([])

const searchForm = reactive({ employeeId: null, status: null, startDate: null, endDate: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const dailyStatusMap = { 0: '未打卡', 1: '正常', 2: '迟到', 3: '早退', 4: '迟到+早退', 5: '缺勤', 6: '请假', 7: '出差' }
const dailyStatusTag = { 0: 'info', 1: 'success', 2: 'warning', 3: 'warning', 4: 'danger', 5: 'danger', 6: 'primary', 7: '' }

const getEmployeeName = (id) => {
  const emp = employeeList.value.find(e => e.id === id)
  return emp ? `${emp.empName} (${emp.empCode})` : id || '-'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageDailyStat({ ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleReset = () => {
  searchForm.employeeId = null; searchForm.status = null
  searchForm.startDate = null; searchForm.endDate = null
  pagination.pageNum = 1; fetchData()
}

const handleGenerate = async () => {
  generating.value = true
  try {
    const params = {}
    if (searchForm.startDate) params.startDate = searchForm.startDate
    if (searchForm.endDate) params.endDate = searchForm.endDate
    if (searchForm.employeeId) params.employeeId = searchForm.employeeId
    const res = await generateDailyStats(params)
    ElMessage.success(`成功生成 ${res.data || 0} 条统计数据`)
    fetchData()
  } catch (e) {
    console.error(e)
    ElMessage.error('生成统计数据失败')
  } finally {
    generating.value = false
  }
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
