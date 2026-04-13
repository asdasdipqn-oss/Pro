<template>
  <div class="assess-task-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考核任务管理</span>
          <el-button type="primary" @click="handleAdd">新建考核任务</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="任务标题">
          <el-input v-model="searchForm.title" placeholder="请输入标题" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable size="large" style="width: 180px;">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已截止" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="title" label="任务标题" min-width="200" show-overflow-tooltip />
        <el-table-column
          prop="description"
          label="任务描述"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="deadline" label="截止时间" width="170" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="success" link @click="handlePublish(row)"
              >发布</el-button
            >
            <el-button v-if="row.status === 1" type="warning" link @click="handleClose(row)"
              >截止</el-button
            >
            <el-button type="info" link @click="handleViewSubmissions(row)">查看提交</el-button>
            <el-button v-if="row.status === 0" type="danger" link @click="handleDelete(row)"
              >删除</el-button
            >
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editForm.id ? '编辑考核任务' : '新建考核任务'"
      width="600px"
    >
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            rows="4"
            placeholder="请输入任务描述和要求"
          />
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            v-model="editForm.deadline"
            type="datetime"
            placeholder="选择截止时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="关联方案">
          <el-select
            v-model="editForm.planId"
            placeholder="可选，关联考核方案"
            clearable
            style="width: 100%"
          >
            <el-option v-for="p in planOptions" :key="p.id" :label="p.planName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="允许文件类型">
          <el-input v-model="editForm.allowFileTypes" placeholder=".doc,.docx,.pdf,.zip" />
        </el-form-item>
        <el-form-item label="最大文件(MB)">
          <el-input-number v-model="editForm.maxFileSize" :min="1" :max="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看提交记录对话框 -->
    <el-dialog v-model="submissionDialogVisible" title="提交记录" width="900px">
      <el-table :data="submissionList" v-loading="submissionLoading" stripe>
        <el-table-column prop="empCode" label="工号" width="100" />
        <el-table-column prop="empName" label="姓名" width="100" />
        <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column label="文件大小" width="100">
          <template #default="{ row }">
            {{ row.fileSize ? (row.fileSize / 1024).toFixed(1) + ' KB' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="170" />
        <el-table-column prop="revisionNo" label="版本" width="70" align="center" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="subStatusTypeMap[row.status]">{{ subStatusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="80" />
        <el-table-column prop="grade" label="等级" width="70" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDownload(row)">下载</el-button>
            <el-button type="success" link @click="openScoreDialog(row)">评分</el-button>
            <el-button type="warning" link @click="openRejectDialog(row)">退回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="subPagination.pageNum"
          v-model:page-size="subPagination.pageSize"
          :total="subPagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchSubmissions"
          @current-change="fetchSubmissions"
        />
      </div>
    </el-dialog>

    <!-- 评分对话框 -->
    <el-dialog v-model="scoreDialogVisible" title="评分" width="760px">
      <el-form label-width="80px">
        <el-form-item label="员工">
          <span>{{ currentSubmission.empName }} ({{ currentSubmission.empCode }})</span>
        </el-form-item>

        <template v-if="hasScoreTemplate">
          <el-alert
            type="info"
            :closable="false"
            title="已启用指标评分模板：请按各项指标打分，系统将自动按权重汇总总分"
            show-icon
            style="margin-bottom: 12px"
          />
          <el-table :data="scoreTemplate" border size="small" max-height="260">
            <el-table-column
              prop="indicatorName"
              label="指标"
              min-width="150"
              show-overflow-tooltip
            />
            <el-table-column prop="weight" label="权重(%)" width="90" align="center" />
            <el-table-column prop="maxScore" label="满分" width="80" align="center" />
            <el-table-column label="本项得分" width="140">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.assessorScore"
                  :min="0"
                  :max="row.maxScore || 100"
                  :precision="2"
                  controls-position="right"
                  style="width: 120px"
                />
              </template>
            </el-table-column>
            <el-table-column
              prop="scoringStandard"
              label="评分标准"
              min-width="180"
              show-overflow-tooltip
            />
          </el-table>
          <el-form-item label="自动总分" style="margin-top: 12px">
            <el-input :model-value="Number(autoTotalScore || 0).toFixed(2)" disabled />
          </el-form-item>
        </template>

        <el-form-item v-else label="得分">
          <el-input-number
            v-model="scoreForm.score"
            :min="0"
            :max="100"
            :precision="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="scoreForm.grade" placeholder="请选择等级" style="width: 100%">
            <el-option label="A - 优秀" value="A" />
            <el-option label="B - 良好" value="B" />
            <el-option label="C - 合格" value="C" />
            <el-option label="D - 待改进" value="D" />
            <el-option label="E - 不合格" value="E" />
          </el-select>
        </el-form-item>
        <el-form-item label="评语">
          <el-input
            v-model="scoreForm.scorerComment"
            type="textarea"
            rows="3"
            placeholder="请输入评语"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleScore" :loading="scoring">确认评分</el-button>
      </template>
    </el-dialog>

    <!-- 退回对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="退回作业" width="400px">
      <el-form label-width="80px">
        <el-form-item label="退回原因">
          <el-input v-model="rejectComment" type="textarea" rows="3" placeholder="请输入退回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="handleReject" :loading="rejecting">确认退回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageAssessTask,
  addAssessTask,
  updateAssessTask,
  publishAssessTask,
  closeAssessTask,
  deleteAssessTask,
  pageSubmission,
  scoreSubmission,
  rejectSubmission,
  downloadSubmissionFile,
  pageAssessPlan,
  getSubmissionScoreTemplate,
} from '@/api/assess'

const loading = ref(false)
const submitting = ref(false)
const scoring = ref(false)
const rejecting = ref(false)
const dialogVisible = ref(false)
const submissionDialogVisible = ref(false)
const scoreDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const submissionLoading = ref(false)
const formRef = ref(null)
const tableData = ref([])
const submissionList = ref([])
const planOptions = ref([])
const currentTaskId = ref(null)
const currentSubmission = ref({})
const rejectComment = ref('')
const scoreTemplate = ref([])

const searchForm = reactive({ title: '', status: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const subPagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const editForm = reactive({
  id: null,
  title: '',
  description: '',
  deadline: null,
  planId: null,
  allowFileTypes: '.doc,.docx,.pdf,.xls,.xlsx,.ppt,.pptx,.zip,.rar,.jpg,.png',
  maxFileSize: 10,
})

const scoreForm = reactive({
  score: null,
  grade: null,
  scorerComment: '',
})

const hasScoreTemplate = computed(() => scoreTemplate.value.length > 0)

const autoTotalScore = computed(() => {
  if (!hasScoreTemplate.value) {
    return scoreForm.score
  }
  return scoreTemplate.value.reduce((sum, item) => {
    if (item.assessorScore === null || item.assessorScore === undefined || !item.maxScore) {
      return sum
    }
    const weighted = (Number(item.assessorScore) / Number(item.maxScore)) * Number(item.weight || 0)
    return sum + weighted
  }, 0)
})

const scorePayload = computed(() => {
  const base = {
    grade: scoreForm.grade,
    scorerComment: scoreForm.scorerComment,
  }
  if (!hasScoreTemplate.value) {
    return {
      ...base,
      score: scoreForm.score,
    }
  }
  return {
    ...base,
    details: scoreTemplate.value.map((item) => ({
      indicatorId: item.indicatorId,
      score: item.assessorScore,
      remark: item.remark,
    })),
  }
})

const formRules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入任务描述', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
}

const statusMap = { 0: '草稿', 1: '已发布', 2: '已截止' }
const statusTypeMap = { 0: 'info', 1: 'success', 2: 'warning' }
const subStatusMap = { 0: '待评分', 1: '已评分', 2: '已退回' }
const subStatusTypeMap = { 0: 'warning', 1: 'success', 2: 'danger' }

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageAssessTask({
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

const fetchPlanOptions = async () => {
  try {
    const res = await pageAssessPlan({ pageNum: 1, pageSize: 100 })
    planOptions.value = res.data?.records || []
  } catch (e) {
    console.error(e)
  }
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.status = null
  pagination.pageNum = 1
  fetchData()
}

const resetForm = () => {
  Object.assign(editForm, {
    id: null,
    title: '',
    description: '',
    deadline: null,
    planId: null,
    allowFileTypes: '.doc,.docx,.pdf,.xls,.xlsx,.ppt,.pptx,.zip,.rar,.jpg,.png',
    maxFileSize: 10,
  })
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(editForm, {
    id: row.id,
    title: row.title,
    description: row.description,
    deadline: row.deadline,
    planId: row.planId,
    allowFileTypes: row.allowFileTypes,
    maxFileSize: row.maxFileSize,
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    const payload = { ...editForm }
    console.log('=== Submitting AssessTask ===')
    console.log('editForm:', editForm)
    console.log('payload:', payload)
    console.log('deadline value:', payload.deadline)
    console.log('deadline type:', typeof payload.deadline)
    // 移除 null 值
    if (payload.id === null || payload.id === undefined) {
      delete payload.id
    }
    if (payload.deadline) {
      // value-format 已经是 "YYYY-MM-DD HH:mm:ss" 格式，直接使用
      console.log('Deadline format:', payload.deadline)
    } else {
      delete payload.deadline
    }
    if (!payload.planId) {
      delete payload.planId
    }
    if (editForm.id) {
      await updateAssessTask(payload)
      ElMessage.success('更新成功')
    } else {
      await addAssessTask(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('handleSubmit error:', error)
    console.error('Response data:', error.response?.data)
    console.error('Response status:', error.response?.status)
    ElMessage.error(error.response?.data?.message || error.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确定要发布该考核任务吗？发布后员工可见。', '提示', {
      type: 'warning',
    })
    await publishAssessTask(row.id)
    ElMessage.success('发布成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleClose = async (row) => {
  try {
    await ElMessageBox.confirm('确定要截止该考核任务吗？截止后员工无法再提交。', '提示', {
      type: 'warning',
    })
    await closeAssessTask(row.id)
    ElMessage.success('已截止')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该考核任务吗？', '提示', { type: 'warning' })
    await deleteAssessTask(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

// 提交记录
const handleViewSubmissions = (row) => {
  currentTaskId.value = row.id
  subPagination.pageNum = 1
  submissionDialogVisible.value = true
  fetchSubmissions()
}

const fetchSubmissions = async () => {
  submissionLoading.value = true
  try {
    const res = await pageSubmission({
      taskId: currentTaskId.value,
      pageNum: subPagination.pageNum,
      pageSize: subPagination.pageSize,
    })
    submissionList.value = res.data?.records || []
    subPagination.total = res.data?.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    submissionLoading.value = false
  }
}

const handleDownload = async (row) => {
  try {
    const blob = await downloadSubmissionFile(row.id)
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = row.fileName || 'download'
    a.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error(error)
  }
}

const openScoreDialog = async (row) => {
  currentSubmission.value = row
  scoreForm.score = row.score
  scoreForm.grade = row.grade
  scoreForm.scorerComment = row.scorerComment || ''
  scoreTemplate.value = []
  try {
    const res = await getSubmissionScoreTemplate(row.id)
    scoreTemplate.value = (res.data || []).map((item) => ({
      ...item,
      assessorScore: null,
      remark: '',
    }))
    if (scoreTemplate.value.length > 0) {
      scoreForm.score = null
    }
  } catch (error) {
    console.error(error)
  }
  scoreDialogVisible.value = true
}

const handleScore = async () => {
  if (!hasScoreTemplate.value && (scoreForm.score === null || scoreForm.score === undefined)) {
    ElMessage.warning('请输入得分')
    return
  }
  if (
    hasScoreTemplate.value &&
    scoreTemplate.value.some(
      (item) => item.assessorScore === null || item.assessorScore === undefined,
    )
  ) {
    ElMessage.warning('请完成所有指标评分')
    return
  }
  scoring.value = true
  try {
    await scoreSubmission(currentSubmission.value.id, scorePayload.value)
    ElMessage.success('评分成功')
    scoreDialogVisible.value = false
    fetchSubmissions()
  } catch (error) {
    console.error(error)
  } finally {
    scoring.value = false
  }
}

const openRejectDialog = (row) => {
  currentSubmission.value = row
  rejectComment.value = ''
  rejectDialogVisible.value = true
}

const handleReject = async () => {
  rejecting.value = true
  try {
    await rejectSubmission(currentSubmission.value.id, { scorerComment: rejectComment.value })
    ElMessage.success('已退回')
    rejectDialogVisible.value = false
    fetchSubmissions()
  } catch (error) {
    console.error(error)
  } finally {
    rejecting.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchPlanOptions()
})
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
