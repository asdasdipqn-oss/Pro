<template>
  <div class="salary-view">
    <el-card>
      <template #header>我的薪资</template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="年份">
          <el-date-picker
            v-model="year"
            type="year"
            placeholder="选择年份"
            format="YYYY"
            @change="fetchData"
          />
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe show-summary>
        <el-table-column prop="salaryMonth" label="月份" width="80">
          <template #default="{ row }">{{ row.salaryMonth }}月</template>
        </el-table-column>
        <el-table-column prop="baseSalary" label="基本工资" width="120" />
        <el-table-column prop="positionSalary" label="岗位工资" width="120" />
        <el-table-column prop="performanceSalary" label="绩效工资" width="120" />
        <el-table-column prop="allowance" label="补贴" width="100" />
        <el-table-column prop="bonus" label="奖金" width="100" />
        <el-table-column prop="grossSalary" label="应发工资" width="120" />
        <el-table-column prop="socialInsurance" label="社保扣款" width="120" />
        <el-table-column prop="housingFund" label="公积金" width="100" />
        <el-table-column prop="personalTax" label="个税" width="100" />
        <el-table-column prop="netSalary" label="实发工资" width="120">
          <template #default="{ row }">
            <span style="color: #409eff; font-weight: bold">{{ row.netSalary }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMySalary } from '@/api/salary'

const loading = ref(false)
const tableData = ref([])
const year = ref(new Date())

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '草稿', 1: '已确认', 2: '已发放' }
  return map[status] || '未知'
}

const fetchData = async () => {
  if (!year.value) return
  loading.value = true
  try {
    const date = new Date(year.value)
    const res = await getMySalary(date.getFullYear())
    tableData.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}
</style>
