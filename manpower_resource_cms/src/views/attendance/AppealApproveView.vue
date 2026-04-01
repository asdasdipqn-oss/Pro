<template>
  <div class="appeal-approve">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考勤申诉审批</span>
          <el-button type="primary" link @click="fetchData">刷新</el-button>
        </div>
      </template>
      
      <el-empty v-if="!loading && tableData.length === 0" description="暂无待审批的申诉" />
      
      <el-table v-else :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="appealCode" label="申诉编号" width="200" />
        <el-table-column prop="employeeName" label="申诉人" width="100" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="appealDate" label="申诉日期" width="120" />
        <el-table-column prop="appealTypeName" label="申诉类型" width="100" />
        <el-table-column prop="appealReason" label="申诉原因" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申诉时间" width="160">
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
    
    <!-- 审批对话框 -->
    <el-dialog v-model="showDialog" :title="approveStatus === 2 ? '通过申诉' : '驳回申诉'" width="400px">
      <el-form label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="comment" type="textarea" :rows="3" placeholder="请输入审批意见(可选)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button :type="approveStatus === 2 ? 'success' : 'danger'" @click="submitApprove" :loading="submitting">
          {{ approveStatus === 2 ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingAppeals, approveAppeal } from '@/api/appeal'

const loading = ref(false)
const submitting = ref(false)
const showDialog = ref(false)
const tableData = ref([])
const currentRow = ref(null)
const approveStatus = ref(2)
const comment = ref('')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPendingAppeals()
    tableData.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleApprove = (row, status) => {
  currentRow.value = row
  approveStatus.value = status
  comment.value = ''
  showDialog.value = true
}

const submitApprove = async () => {
  submitting.value = true
  try {
    await approveAppeal(currentRow.value.id, approveStatus.value, comment.value)
    ElMessage.success(approveStatus.value === 2 ? '已通过' : '已驳回')
    showDialog.value = false
    fetchData()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
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
