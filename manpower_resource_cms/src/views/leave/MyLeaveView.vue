<template>
  <div class="my-leave">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的假期</span>
          <el-button type="primary" @click="$router.push('/leave/apply')">新增申请</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="applyCode" label="申请编号" width="180" />
        <el-table-column prop="leaveTypeId" label="假期类型" width="100">
          <template #default="{ row }">
            {{ getTypeName(row.leaveTypeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column prop="duration" label="天数" width="80" />
        <el-table-column prop="reason" label="原因" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0 || row.status === 1"
              type="danger"
              link
              @click="handleCancel(row)"
            >
              撤回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        class="pagination"
        @current-change="fetchData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyLeave, listLeaveType, cancelLeave } from '@/api/leave'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const leaveTypes = ref([])

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '待审批', 1: '审批中', 2: '已通过', 3: '已驳回', 4: '已撤回' }
  return map[status] || '未知'
}

const getTypeName = (typeId) => {
  const type = leaveTypes.value.find((t) => t.id === typeId)
  return type ? type.typeName : '-'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyLeave({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchLeaveTypes = async () => {
  try {
    const res = await listLeaveType()
    leaveTypes.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确定要撤回该申请吗？', '提示', { type: 'warning' })
  try {
    await cancelLeave(row.id)
    ElMessage.success('撤回成功')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchData()
  fetchLeaveTypes()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
