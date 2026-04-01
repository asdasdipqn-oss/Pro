<template>
  <div class="my-appeal">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的考勤申诉</span>
          <el-button type="primary" @click="showDialog = true">发起申诉</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="appealCode" label="申诉编号" width="200" />
        <el-table-column prop="appealDate" label="申诉日期" width="120" />
        <el-table-column prop="appealTypeName" label="申诉类型" width="100" />
        <el-table-column prop="appealReason" label="申诉原因" show-overflow-tooltip />
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申诉时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="danger" link @click="handleCancel(row)">撤回</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 申诉对话框 -->
    <el-dialog v-model="showDialog" title="发起申诉" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="申诉日期" prop="appealDate">
          <el-date-picker v-model="form.appealDate" type="date" placeholder="选择申诉日期" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="申诉类型" prop="appealType">
          <el-select v-model="form.appealType" placeholder="请选择" style="width: 100%;" @change="handleAppealTypeChange">
            <el-option label="漏打卡" :value="1" />
            <el-option label="定位异常" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.appealType === 1" label="打卡类型" prop="clockType">
          <el-select v-model="form.clockType" placeholder="请选择补卡类型" style="width: 100%;">
            <el-option label="上班卡" :value="1" />
            <el-option label="下班卡" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="申诉原因" prop="appealReason">
          <el-input v-model="form.appealReason" type="textarea" :rows="4" placeholder="请详细描述申诉原因" />
        </el-form-item>
        <el-form-item label="证明材料">
          <el-input v-model="form.proofUrl" placeholder="证明材料链接(可选)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyAppeals, submitAppeal, cancelAppeal } from '@/api/appeal'

const loading = ref(false)
const submitting = ref(false)
const showDialog = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10 })

const form = reactive({
  appealDate: '',
  appealType: null,
  clockType: null,
  appealReason: '',
  proofUrl: ''
})

const rules = {
  appealDate: [{ required: true, message: '请选择申诉日期', trigger: 'change' }],
  appealType: [{ required: true, message: '请选择申诉类型', trigger: 'change' }],
  appealReason: [{ required: true, message: '请输入申诉原因', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyAppeals(query)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  // 漏打卡申诉必须选择打卡类型
  if (form.appealType === 1 && !form.clockType) {
    ElMessage.warning('请选择打卡类型')
    return
  }
  
  submitting.value = true
  try {
    await submitAppeal(form)
    ElMessage.success('申诉提交成功')
    showDialog.value = false
    resetForm()
    fetchData()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确定要撤回此申诉吗？', '提示', { type: 'warning' })
  try {
    await cancelAppeal(row.id)
    ElMessage.success('撤回成功')
    fetchData()
  } catch (e) {
    console.error(e)
  }
}

const resetForm = () => {
  form.appealDate = ''
  form.appealType = null
  form.clockType = null
  form.appealReason = ''
  form.proofUrl = ''
}

// 申诉类型变化时重置打卡类型
const handleAppealTypeChange = () => {
  form.clockType = null
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'info', 2: 'success', 3: 'danger', 4: 'info' }
  return map[status] || ''
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
