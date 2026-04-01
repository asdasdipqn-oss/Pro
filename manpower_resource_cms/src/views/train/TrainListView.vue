<template>
  <div class="train-list-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>培训计划管理</span>
          <el-button type="primary" @click="handleAdd">创建计划</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable @change="fetchData">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="planName" label="培训名称" width="200" />
        <el-table-column prop="trainType" label="培训类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTypeName(row.trainType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="trainer" label="讲师" width="120" />
        <el-table-column label="培训时间" width="200">
          <template #default="{ row }">
            {{ formatDate(row.startTime) }} ~ {{ formatDate(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="trainLocation" label="培训地点" width="150" />
        <el-table-column prop="maxParticipants" label="人数上限" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link @click="handleParticipants(row)">参训人员</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑培训' : '创建培训'" width="650px">
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="培训名称" prop="planName">
          <el-input v-model="editForm.planName" placeholder="请输入培训名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="培训类型" prop="trainType">
              <el-select v-model="editForm.trainType" placeholder="请选择" style="width: 100%">
                <el-option label="入职培训" :value="1" />
                <el-option label="技能培训" :value="2" />
                <el-option label="管理培训" :value="3" />
                <el-option label="安全培训" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="讲师">
              <el-input v-model="editForm.trainer" placeholder="请输入讲师" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="editForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="editForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="培训地点">
              <el-input v-model="editForm.trainLocation" placeholder="请输入培训地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="人数上限">
              <el-input-number v-model="editForm.maxParticipants" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="培训内容">
          <el-input
            v-model="editForm.description"
            type="textarea"
            rows="3"
            placeholder="请输入培训内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 参训人员管理对话框 -->
    <el-dialog v-model="participantDialogVisible" title="参训人员管理" width="800px">
      <div class="participant-header">
        <el-select
          v-model="selectedEmployees"
          multiple
          filterable
          placeholder="选择员工添加"
          style="width: 400px"
        >
          <el-option
            v-for="emp in availableEmployees"
            :key="emp.id"
            :label="`${emp.empName} (${emp.empCode})`"
            :value="emp.id"
          />
        </el-select>
        <el-button
          type="primary"
          @click="handleAddParticipants"
          :disabled="selectedEmployees.length === 0"
        >
          添加选中人员
        </el-button>
      </div>

      <el-table :data="participants" v-loading="participantLoading" stripe style="margin-top: 16px">
        <el-table-column prop="empCode" label="工号" width="100" />
        <el-table-column prop="employeeName" label="姓名" width="100" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="attendanceStatusName" label="签到状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.attendanceStatus === 1 ? 'success' : 'info'">{{
              row.attendanceStatusName
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signInTime" label="签到时间" width="170" />
        <el-table-column prop="score" label="成绩" width="80" align="center" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleRecordScore(row)">录入成绩</el-button>
            <el-button type="danger" link @click="handleRemoveParticipant(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 录入成绩对话框 -->
    <el-dialog v-model="scoreDialogVisible" title="录入成绩" width="400px">
      <el-form label-width="80px">
        <el-form-item label="成绩">
          <el-input-number v-model="scoreForm.score" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="评价">
          <el-input
            v-model="scoreForm.evaluation"
            type="textarea"
            rows="3"
            placeholder="培训评价"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitScore">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageTrainPlans,
  createTrainPlan,
  updateTrainPlan,
  deleteTrainPlan,
  getParticipants,
  assignParticipants,
  removeParticipant,
  recordScore,
} from '@/api/train'
import { listEmployee } from '@/api/employee'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const participantDialogVisible = ref(false)
const participantLoading = ref(false)
const scoreDialogVisible = ref(false)
const tableData = ref([])
const formRef = ref(null)
const currentPlan = ref(null)
const participants = ref([])
const availableEmployees = ref([])
const selectedEmployees = ref([])
const scoreForm = reactive({ participantId: null, score: null, evaluation: '' })

const searchForm = reactive({ status: null })

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const editForm = reactive({
  id: null,
  planName: '',
  trainType: null,
  trainer: '',
  startTime: null,
  endTime: null,
  trainLocation: '',
  maxParticipants: 50,
  description: '',
})

const formRules = {
  planName: [{ required: true, message: '请输入培训名称', trigger: 'blur' }],
  trainType: [{ required: true, message: '请选择培训类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
}

const getTypeName = (type) => {
  const map = { 1: '入职培训', 2: '技能培训', 3: '管理培训', 4: '安全培训' }
  return map[type] || '其他'
}

const getStatusName = (status) => {
  const map = { 0: '未开始', 1: '进行中', 2: '已结束', 3: '已取消' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'primary', 2: 'success', 3: 'warning' }
  return map[status] || 'info'
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString()
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageTrainPlans({
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(editForm, {
    id: null,
    planName: '',
    trainType: null,
    trainer: '',
    startTime: null,
    endTime: null,
    trainLocation: '',
    maxParticipants: 50,
    description: '',
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(editForm, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    if (editForm.id) {
      await updateTrainPlan(editForm)
      ElMessage.success('更新成功')
    } else {
      await createTrainPlan(editForm)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该培训计划吗？', '提示', { type: 'warning' })
    await deleteTrainPlan(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

// ============ 参训人员管理 ============
const handleParticipants = async (row) => {
  currentPlan.value = row
  participantDialogVisible.value = true
  await Promise.all([fetchParticipants(), fetchAvailableEmployees()])
}

const fetchParticipants = async () => {
  participantLoading.value = true
  try {
    const res = await getParticipants(currentPlan.value.id)
    participants.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    participantLoading.value = false
  }
}

const fetchAvailableEmployees = async () => {
  try {
    const res = await listEmployee()
    availableEmployees.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const handleAddParticipants = async () => {
  try {
    await assignParticipants(currentPlan.value.id, selectedEmployees.value)
    ElMessage.success('添加成功')
    selectedEmployees.value = []
    fetchParticipants()
  } catch (error) {
    console.error(error)
  }
}

const handleRemoveParticipant = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要移除 ${row.employeeName} 吗？`, '提示', { type: 'warning' })
    await removeParticipant(currentPlan.value.id, row.employeeId)
    ElMessage.success('已移除')
    fetchParticipants()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleRecordScore = (row) => {
  scoreForm.participantId = row.id
  scoreForm.score = row.score || 0
  scoreForm.evaluation = row.evaluation || ''
  scoreDialogVisible.value = true
}

const submitScore = async () => {
  try {
    await recordScore(scoreForm.participantId, scoreForm.score, scoreForm.evaluation)
    ElMessage.success('成绩录入成功')
    scoreDialogVisible.value = false
    fetchParticipants()
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
.search-form {
  margin-bottom: 20px;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.participant-header {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
