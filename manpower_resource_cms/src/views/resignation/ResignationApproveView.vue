<template>
  <div class="resignation-approve-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>离职申请审批</span>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchStatus" placeholder="全部" clearable size="large" style="width: 180px;" @change="fetchData">
            <el-option label="待审批" :value="0" />
            <el-option label="已批准" :value="1" />
            <el-option label="已拒绝" :value="2" />
            <el-option label="已撤销" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="empCode" label="工号" width="100" />
        <el-table-column prop="empName" label="姓名" width="100" />
        <el-table-column label="离职类型" width="100">
          <template #default="{ row }">
            {{ resignationTypeMap[row.resignationType] }}
          </template>
        </el-table-column>
        <el-table-column prop="expectedDate" label="期望离职日期" width="120" />
        <el-table-column prop="reason" label="离职原因" show-overflow-tooltip />
        <el-table-column prop="handoverName" label="交接人" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" link @click="handleApprove(row, true)">批准</el-button>
              <el-button type="danger" link @click="handleApprove(row, false)">拒绝</el-button>
            </template>
            <span v-else>{{ row.approveRemark || '-' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approveDialogVisible"
      :title="approving ? '批准离职' : '拒绝离职'"
      width="400px"
    >
      <el-form label-width="80px">
        <el-form-item label="审批备注">
          <el-input v-model="approveRemark" type="textarea" rows="3" placeholder="请输入审批备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button
          :type="approving ? 'success' : 'danger'"
          @click="submitApprove"
          :loading="submitting"
        >
          {{ approving ? '确认批准' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pageResignation, approveResignation } from '@/api/resignation'

const loading = ref(false)
const submitting = ref(false)
const approveDialogVisible = ref(false)
const approving = ref(true)
const approveRemark = ref('')
const currentRow = ref(null)
const tableData = ref([])
const searchStatus = ref(null)

const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const resignationTypeMap = { 1: '主动离职', 2: '协商离职', 3: '合同到期' }
const statusMap = { 0: '待审批', 1: '已批准', 2: '已拒绝', 3: '已撤销' }
const statusTypeMap = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageResignation({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      status: searchStatus.value,
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleApprove = (row, approved) => {
  currentRow.value = row
  approving.value = approved
  approveRemark.value = ''
  approveDialogVisible.value = true
}

const submitApprove = async () => {
  submitting.value = true
  try {
    await approveResignation(currentRow.value.id, approving.value, approveRemark.value)
    ElMessage.success(approving.value ? '已批准离职申请' : '已拒绝离职申请')
    approveDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 20px;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
