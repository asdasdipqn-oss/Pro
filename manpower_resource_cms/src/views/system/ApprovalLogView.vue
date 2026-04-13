<template>
  <div class="approval-log-view">
    <el-card>
      <template #header>
        <div class="card-header"><span>审批日志</span></div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="业务类型">
          <el-select v-model="searchForm.businessType" placeholder="全部" clearable size="large" style="width: 180px;" @change="fetchData">
            <el-option label="请假" value="LEAVE" />
            <el-option label="离职" value="RESIGNATION" />
            <el-option label="考勤申诉" value="APPEAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable size="large" style="width: 180px;" @change="fetchData">
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="filteredData" v-loading="loading" stripe>
        <el-table-column prop="businessType" label="业务类型" width="110">
          <template #default="{ row }">
            <el-tag :type="bizTypeTag[row.businessType]">{{ row.businessType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="action" label="操作" width="100">
          <template #default="{ row }">
            <el-tag :type="actionTag[row.action]" size="small">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approverName" label="操作人" width="100" />
        <el-table-column prop="comment" label="审批意见" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag[row.status]" size="small">{{ statusText[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="170" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { pageApprovalLog } from '@/api/approval-log'

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({ businessType: null, status: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const bizTypeTag = { '请假': 'primary', '考勤申诉': 'warning', '离职': 'danger', '调岗': 'info', '培训需求': 'success' }
const actionTag = { '待审批': 'warning', '通过': 'success', '驳回': 'danger' }
const statusTag = { 0: 'warning', 1: 'success', 2: 'danger' }
const statusText = { 0: '待审批', 1: '已通过', 2: '已驳回' }

// 前端按状态二次过滤
const filteredData = computed(() => {
  if (searchForm.status === null || searchForm.status === undefined || searchForm.status === '') {
    return tableData.value
  }
  return tableData.value.filter(row => row.status === searchForm.status)
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
    if (searchForm.businessType) params.businessType = searchForm.businessType
    const res = await pageApprovalLog(params)
    tableData.value = res.data?.records || res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
