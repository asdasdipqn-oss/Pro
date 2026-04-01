<template>
  <div class="approve-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>假期审批</span>
          <el-button type="primary" link @click="fetchData">刷新</el-button>
        </div>
      </template>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无待审批的请假申请" />

      <el-table v-else :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="applyCode" label="申请编号" width="200" />
        <el-table-column prop="employeeName" label="申请人" width="100" />
        <el-table-column label="请假时间" width="320">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="天数" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.duration }}天</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="请假原因" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link @click="handleApprove(row, 2)">通过</el-button>
            <el-button type="danger" link @click="handleApprove(row, 3)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingLeave, approveLeave } from '@/api/leave'

const loading = ref(false)
const tableData = ref([])

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPendingLeave()
    tableData.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleApprove = async (row, status) => {
  const action = status === 2 ? '通过' : '驳回'
  const { value: comment } = await ElMessageBox.prompt(`请输入审批意见`, `确认${action}`, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入审批意见（可选）',
  }).catch(() => ({ value: '' }))

  if (comment === undefined) return

  try {
    await approveLeave(row.id, status, comment)
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch (error) {
    console.error(error)
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
</style>
