<template>
  <div class="assess-submission-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考核审批</span>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="考核任务">
          <el-select v-model="searchForm.taskId" placeholder="全部任务" clearable filterable>
            <el-option v-for="task in taskList" :key="task.id" :label="task.title" :value="task.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待评分" :value="0" />
            <el-option label="已评分" :value="1" />
            <el-option label="已退回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column label="员工" width="140">
          <template #default="{ row }">{{ row.empName }} ({{ row.empCode }})</template>
        </el-table-column>
        <el-table-column prop="taskTitle" label="考核任务" min-width="160" show-overflow-tooltip />
        <el-table-column prop="fileName" label="提交文件" min-width="150" show-overflow-tooltip />
        <el-table-column prop="fileSize" label="文件大小" width="100" align="center">
          <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="170" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status]" size="small">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="80" align="center">
          <template #default="{ row }">{{ row.score ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="grade" label="等级" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.grade" :type="gradeTagMap[row.grade] || 'info'" size="small">{{ row.grade }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDownload(row)">下载</el-button>
            <el-button v-if="row.status === 0 || row.status === 2" type="success" link @click="handleScore(row)">评分</el-button>
            <el-button v-if="row.status === 0" type="warning" link @click="handleReject(row)">退回</el-button>
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

    <!-- 评分对话框 -->
    <el-dialog v-model="scoreDialogVisible" title="评分" width="600px">
      <el-form ref="scoreFormRef" :model="scoreForm" label-width="120px">
        <el-descriptions :column="2" border class="submission-info">
          <el-descriptions-item label="员工">{{ currentSubmission?.empName }}</el-descriptions-item>
          <el-descriptions-item label="任务">{{ currentSubmission?.taskTitle }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentSubmission?.submitTime }}</el-descriptions-item>
          <el-descriptions-item label="文件">
            <el-button type="primary" link @click="handleDownload(currentSubmission)">下载</el-button>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <el-form-item label="评分方式">
          <el-radio-group v-model="scoreMode">
            <el-radio value="simple">直接评分</el-radio>
            <el-radio value="detail">按指标评分</el-radio>
          </el-radio-group>
        </el-form-item>

        <template v-if="scoreMode === 'simple'">
          <el-form-item label="总分">
            <el-input-number v-model="scoreForm.score" :min="0" :max="100" :precision="2" style="width: 100%" />
          </el-form-item>
          <el-form-item label="等级">
            <el-select v-model="scoreForm.grade" style="width: 100%">
              <el-option label="A - 优秀" value="A" />
              <el-option label="B - 良好" value="B" />
              <el-option label="C - 合格" value="C" />
              <el-option label="D - 待改进" value="D" />
              <el-option label="E - 不合格" value="E" />
            </el-select>
          </el-form-item>
        </template>

        <template v-if="scoreMode === 'detail'">
          <el-table :data="scoreForm.details" stripe style="margin-bottom: 10px">
            <el-table-column label="指标" prop="indicatorName" width="120" />
            <el-table-column label="权重" prop="weight" width="60" align="center" />
            <el-table-column label="满分" prop="maxScore" width="60" align="center" />
            <el-table-column label="得分" width="100">
              <template #default="{ row, $index }">
                <el-input-number
                  v-model="row.score"
                  :min="0"
                  :max="row.maxScore"
                  :precision="2"
                  size="small"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="150">
              <template #default="{ row }">
                <el-input v-model="row.remark" size="small" placeholder="备注" />
              </template>
            </el-table-column>
          </el-table>
          <el-alert v-if="templateLoading" type="info" :closable="false" text="正在加载评分模板..." />
          <el-empty v-if="!templateLoading && scoreForm.details.length === 0" description="该任务未关联考核方案" />
        </template>

        <el-form-item label="评分评语">
          <el-input v-model="scoreForm.scorerComment" type="textarea" rows="3" placeholder="请输入评分评语" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitScore" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>

    <!-- 退回对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="退回提交" width="500px">
      <el-form ref="rejectFormRef" :model="rejectForm" label-width="80px">
        <el-descriptions :column="1" border class="submission-info" style="margin-bottom: 16px">
          <el-descriptions-item label="员工">{{ currentSubmission?.empName }}</el-descriptions-item>
          <el-descriptions-item label="任务">{{ currentSubmission?.taskTitle }}</el-descriptions-item>
        </el-descriptions>
        <el-form-item label="退回原因" prop="scorerComment" :rules="[{ required: true, message: '请输入退回原因', trigger: 'blur' }]">
          <el-input v-model="rejectForm.scorerComment" type="textarea" rows="4" placeholder="请输入退回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="handleSubmitReject" :loading="rejecting">确认退回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageSubmission,
  scoreSubmission,
  rejectSubmission,
  downloadSubmissionFile,
  getSubmissionScoreTemplate
} from '@/api/assess'

const loading = ref(false)
const submitting = ref(false)
const rejecting = ref(false)
const templateLoading = ref(false)
const scoreDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const tableData = ref([])
const taskList = ref([])
const currentSubmission = ref(null)
const scoreMode = ref('simple')
const scoreFormRef = ref()
const rejectFormRef = ref()

const searchForm = reactive({ taskId: null, status: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const statusMap = { 0: '待评分', 1: '已评分', 2: '已退回' }
const statusTagMap = { 0: 'warning', 1: 'success', 2: 'danger' }
const gradeTagMap = { A: 'success', B: 'primary', C: 'warning', D: 'danger', E: 'danger' }

const scoreForm = reactive({
  score: null,
  grade: 'C',
  scorerComment: '',
  details: []
})

const rejectForm = reactive({
  scorerComment: ''
})

const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageSubmission({ ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0

    // 提取任务列表（从返回的数据中）
    const uniqueTasks = [...new Map(tableData.value.map(item => [item.taskId, { id: item.taskId, title: item.taskTitle }])).values()]
    if (!taskList.value.length) {
      taskList.value = uniqueTasks
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取提交记录失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.taskId = null
  searchForm.status = null
  pagination.pageNum = 1
  fetchData()
}

const handleDownload = async (row) => {
  try {
    const blob = await downloadSubmissionFile(row.id)
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = row.fileName
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    console.error(e)
    ElMessage.error('下载失败')
  }
}

const handleScore = async (row) => {
  currentSubmission.value = row
  scoreMode.value = 'simple'
  scoreForm.score = row.score || null
  scoreForm.grade = row.grade || 'C'
  scoreForm.scorerComment = row.scorerComment || ''
  scoreForm.details = []
  scoreDialogVisible.value = true
}

// 加载评分模板
watch(scoreMode, async (newMode) => {
  if (newMode === 'detail' && currentSubmission.value) {
    templateLoading.value = true
    try {
      const res = await getSubmissionScoreTemplate(currentSubmission.value.id)
      scoreForm.details = (res.data || []).map(item => ({
        indicatorId: item.indicatorId,
        indicatorName: item.indicatorName,
        weight: item.weight,
        maxScore: item.maxScore,
        score: null,
        remark: ''
      }))
    } catch (e) {
      console.error(e)
    } finally {
      templateLoading.value = false
    }
  }
})

const handleSubmitScore = async () => {
  const payload = {
    scorerComment: scoreForm.scorerComment
  }

  if (scoreMode.value === 'simple') {
    if (scoreForm.score === null) {
      ElMessage.warning('请输入总分')
      return
    }
    payload.score = scoreForm.score
    payload.grade = scoreForm.grade
  } else {
    if (scoreForm.details.length === 0) {
      ElMessage.warning('该任务未关联考核方案，无法按指标评分')
      return
    }
    // 验证指标得分
    for (const detail of scoreForm.details) {
      if (detail.score === null) {
        ElMessage.warning('请填写所有指标得分')
        return
      }
    }
    payload.details = scoreForm.details
  }

  submitting.value = true
  try {
    await scoreSubmission(currentSubmission.value.id, payload)
    ElMessage.success('评分成功')
    scoreDialogVisible.value = false
    fetchData()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const handleReject = async (row) => {
  currentSubmission.value = row
  rejectForm.scorerComment = ''
  rejectDialogVisible.value = true
}

const handleSubmitReject = async () => {
  if (!rejectForm.scorerComment.trim()) {
    ElMessage.warning('请输入退回原因')
    return
  }

  rejecting.value = true
  try {
    await rejectSubmission(currentSubmission.value.id, { scorerComment: rejectForm.scorerComment })
    ElMessage.success('已退回')
    rejectDialogVisible.value = false
    fetchData()
  } catch (e) {
    console.error(e)
  } finally {
    rejecting.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
.submission-info { margin-bottom: 16px; }
</style>
