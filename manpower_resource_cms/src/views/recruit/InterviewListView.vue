<template>
  <div class="interview-list-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>面试管理</span>
          <el-button type="primary" @click="handleAdd">安排面试</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable @change="fetchData">
            <el-option label="待面试" :value="0" />
            <el-option label="已完成" :value="1" />
            <el-option label="已取消" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="searchForm.result" placeholder="全部" clearable @change="fetchData">
            <el-option label="通过" :value="1" />
            <el-option label="待定" :value="2" />
            <el-option label="不通过" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="resumeId" label="简历ID" width="80" />
        <el-table-column prop="interviewRound" label="面试轮次" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small">第{{ row.interviewRound }}轮</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="面试方式" width="100">
          <template #default="{ row }">
            {{ interviewTypeMap[row.interviewType] }}
          </template>
        </el-table-column>
        <el-table-column prop="interviewTime" label="面试时间" width="170" />
        <el-table-column prop="interviewAddress" label="面试地点" min-width="150" show-overflow-tooltip />
        <el-table-column prop="interviewerId" label="面试官ID" width="90" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="结果" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.result" :type="resultTypeMap[row.result]" size="small">
              {{ resultMap[row.result] }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="评分" width="70" align="center">
          <template #default="{ row }">
            <span v-if="row.score" :style="{ color: row.score >= 70 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              {{ row.score }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" link @click="handleComplete(row)">填写结果</el-button>
            <el-button v-if="row.status === 0" type="warning" link @click="handleCancel(row)">取消</el-button>
            <el-button type="info" link @click="handleView(row)">详情</el-button>
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

    <!-- 安排面试对话框 -->
    <el-dialog v-model="addDialogVisible" title="安排面试" width="600px">
      <el-form ref="addFormRef" :model="addForm" :rules="addFormRules" label-width="100px">
        <el-form-item label="简历" prop="resumeId">
          <el-select v-model="addForm.resumeId" placeholder="选择简历" filterable style="width: 100%">
            <el-option v-for="r in resumeList" :key="r.id"
              :label="`${r.name} - ${r.phone}`" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="面试轮次" prop="interviewRound">
          <el-input-number v-model="addForm.interviewRound" :min="1" :max="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="面试方式" prop="interviewType">
          <el-select v-model="addForm.interviewType" placeholder="选择方式" style="width: 100%">
            <el-option label="现场面试" :value="1" />
            <el-option label="电话面试" :value="2" />
            <el-option label="视频面试" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="面试时间" prop="interviewTime">
          <el-date-picker v-model="addForm.interviewTime" type="datetime"
            placeholder="选择面试时间" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="面试地点">
          <el-input v-model="addForm.interviewAddress" placeholder="面试地点/会议链接" />
        </el-form-item>
        <el-form-item label="面试官ID">
          <el-input-number v-model="addForm.interviewerId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="addForm.remark" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 填写面试结果对话框 -->
    <el-dialog v-model="completeDialogVisible" title="填写面试结果" width="600px">
      <el-form ref="completeFormRef" :model="completeForm" :rules="completeRules" label-width="100px">
        <el-form-item label="面试评价" prop="evaluation">
          <el-input v-model="completeForm.evaluation" type="textarea" rows="4" placeholder="请填写面试评价" />
        </el-form-item>
        <el-form-item label="评分" prop="score">
          <el-input-number v-model="completeForm.score" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="面试结果" prop="result">
          <el-radio-group v-model="completeForm.result">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="2">待定</el-radio>
            <el-radio :value="3">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="completeForm.remark" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCompleteSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="面试详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="简历ID">{{ detailData.resumeId }}</el-descriptions-item>
        <el-descriptions-item label="面试轮次">第{{ detailData.interviewRound }}轮</el-descriptions-item>
        <el-descriptions-item label="面试方式">{{ interviewTypeMap[detailData.interviewType] }}</el-descriptions-item>
        <el-descriptions-item label="面试时间">{{ detailData.interviewTime }}</el-descriptions-item>
        <el-descriptions-item label="面试地点">{{ detailData.interviewAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="面试官ID">{{ detailData.interviewerId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTypeMap[detailData.status]">{{ statusMap[detailData.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="结果">
          <el-tag v-if="detailData.result" :type="resultTypeMap[detailData.result]">
            {{ resultMap[detailData.result] }}
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="评分">{{ detailData.score ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="面试评价" :span="2">{{ detailData.evaluation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageInterviews, addInterview, completeInterview, cancelInterview, deleteInterview,
  pageResumes
} from '@/api/recruit'

const loading = ref(false)
const submitting = ref(false)
const addDialogVisible = ref(false)
const completeDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const addFormRef = ref()
const completeFormRef = ref()
const tableData = ref([])
const resumeList = ref([])
const detailData = ref({})

const searchForm = reactive({ status: null, result: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const interviewTypeMap = { 1: '现场面试', 2: '电话面试', 3: '视频面试' }
const statusMap = { 0: '待面试', 1: '已完成', 2: '已取消' }
const statusTypeMap = { 0: 'warning', 1: 'success', 2: 'info' }
const resultMap = { 1: '通过', 2: '待定', 3: '不通过' }
const resultTypeMap = { 1: 'success', 2: 'warning', 3: 'danger' }

const defaultAddForm = () => ({
  resumeId: null, interviewRound: 1, interviewType: 1,
  interviewTime: '', interviewAddress: '', interviewerId: null, remark: ''
})
const addForm = reactive(defaultAddForm())

const addFormRules = {
  resumeId: [{ required: true, message: '请选择简历', trigger: 'change' }],
  interviewRound: [{ required: true, message: '请输入轮次', trigger: 'blur' }],
  interviewType: [{ required: true, message: '请选择方式', trigger: 'change' }],
  interviewTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
}

const completeForm = reactive({ id: null, evaluation: '', score: null, result: null, remark: '' })
const completeRules = {
  evaluation: [{ required: true, message: '请填写评价', trigger: 'blur' }],
  score: [{ required: true, message: '请输入评分', trigger: 'blur' }],
  result: [{ required: true, message: '请选择结果', trigger: 'change' }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageInterviews({
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchResumes = async () => {
  try {
    const res = await pageResumes({ pageNum: 1, pageSize: 200, status: 2 })
    resumeList.value = res.data?.records || []
  } catch (error) {
    console.error(error)
  }
}

const handleReset = () => {
  searchForm.status = null
  searchForm.result = null
  pagination.pageNum = 1
  fetchData()
}

const handleAdd = () => {
  Object.assign(addForm, defaultAddForm())
  fetchResumes()
  addDialogVisible.value = true
}

const handleAddSubmit = async () => {
  await addFormRef.value.validate()
  submitting.value = true
  try {
    await addInterview(addForm)
    ElMessage.success('面试安排成功')
    addDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleComplete = (row) => {
  completeForm.id = row.id
  completeForm.evaluation = ''
  completeForm.score = null
  completeForm.result = null
  completeForm.remark = ''
  completeDialogVisible.value = true
}

const handleCompleteSubmit = async () => {
  await completeFormRef.value.validate()
  submitting.value = true
  try {
    await completeInterview(completeForm.id, {
      evaluation: completeForm.evaluation,
      score: completeForm.score,
      result: completeForm.result,
      remark: completeForm.remark
    })
    ElMessage.success('面试结果已提交')
    completeDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消该面试吗？', '提示', { type: 'warning' })
    await cancelInterview(row.id)
    ElMessage.success('已取消')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleView = (row) => {
  detailData.value = { ...row }
  detailDialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该面试记录吗？', '提示', { type: 'warning' })
    await deleteInterview(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
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
</style>
