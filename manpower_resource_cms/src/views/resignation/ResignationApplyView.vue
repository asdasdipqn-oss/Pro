<template>
  <div class="resignation-apply-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>离职申请</span>
          <el-button type="primary" @click="handleApply">提交离职申请</el-button>
        </div>
      </template>

      <el-table :data="myApplications" v-loading="loading" stripe>
        <el-table-column label="离职类型" width="120">
          <template #default="{ row }">
            {{ resignationTypeMap[row.resignationType] }}
          </template>
        </el-table-column>
        <el-table-column prop="expectedDate" label="期望离职日期" width="130" />
        <el-table-column prop="reason" label="离职原因" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approveRemark" label="审批备注" width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="danger" link @click="handleCancel(row)"
              >撤销</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 申请对话框 -->
    <el-dialog v-model="dialogVisible" title="提交离职申请" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="离职类型" prop="resignationType">
          <el-select v-model="form.resignationType" placeholder="请选择" style="width: 100%">
            <el-option label="主动离职" :value="1" />
            <el-option label="协商离职" :value="2" />
            <el-option label="合同到期" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="期望离职日期" prop="expectedDate">
          <el-date-picker
            v-model="form.expectedDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="工作交接人">
          <el-select
            v-model="form.handoverTo"
            placeholder="请选择交接人"
            filterable
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="emp in employeeList"
              :key="emp.id"
              :label="emp.empName"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="离职原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" rows="4" placeholder="请说明离职原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyResignations, applyResignation, cancelResignation } from '@/api/resignation'
import { listEmployee } from '@/api/employee'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const myApplications = ref([])
const employeeList = ref([])

const form = reactive({
  resignationType: 1,
  expectedDate: '',
  handoverTo: null,
  reason: '',
})

const formRules = {
  resignationType: [{ required: true, message: '请选择离职类型', trigger: 'change' }],
  expectedDate: [{ required: true, message: '请选择期望离职日期', trigger: 'change' }],
  reason: [{ required: true, message: '请说明离职原因', trigger: 'blur' }],
}

const resignationTypeMap = { 1: '主动离职', 2: '协商离职', 3: '合同到期' }
const statusMap = { 0: '待审批', 1: '已批准', 2: '已拒绝', 3: '已撤销' }
const statusTypeMap = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 不能选择今天之前的日期
}

const fetchMyApplications = async () => {
  loading.value = true
  try {
    const res = await getMyResignations()
    myApplications.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchEmployees = async () => {
  try {
    const res = await listEmployee()
    employeeList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const handleApply = () => {
  Object.assign(form, { resignationType: 1, expectedDate: '', handoverTo: null, reason: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    await applyResignation(form)
    ElMessage.success('离职申请已提交，请等待审批')
    dialogVisible.value = false
    fetchMyApplications()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要撤销此离职申请吗？', '提示', { type: 'warning' })
    await cancelResignation(row.id)
    ElMessage.success('已撤销')
    fetchMyApplications()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

onMounted(() => {
  fetchMyApplications()
  fetchEmployees()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
