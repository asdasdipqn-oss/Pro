<template>
  <div class="assess-result-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考核结果管理</span>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="考核方案">
          <el-select v-model="searchForm.planId" placeholder="全部方案" clearable filterable>
            <el-option v-for="p in planList" :key="p.id" :label="p.planName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="员工">
          <el-select v-model="searchForm.employeeId" placeholder="全部员工" clearable filterable>
            <el-option v-for="emp in employeeList" :key="emp.id"
              :label="`${emp.empName} (${emp.empCode})`" :value="emp.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待考核" :value="0" />
            <el-option label="已自评" :value="1" />
            <el-option label="已考核" :value="2" />
            <el-option label="已确认" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column label="员工" width="140">
          <template #default="{ row }">{{ getEmployeeName(row.employeeId) }}</template>
        </el-table-column>
        <el-table-column label="考核方案" width="160">
          <template #default="{ row }">{{ getPlanName(row.planId) }}</template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总得分" width="90" align="center" />
        <el-table-column prop="grade" label="等级" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.grade" :type="gradeTagMap[row.grade] || 'info'" size="small">{{ row.grade }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status]" size="small">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="selfEvaluation" label="自我评价" min-width="150" show-overflow-tooltip />
        <el-table-column prop="assessorComment" label="考核评语" min-width="150" show-overflow-tooltip />
        <el-table-column prop="assessTime" label="考核时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">评分明细</el-button>
            <el-button v-if="row.status < 2" type="success" link @click="handleAssess(row)">评分</el-button>
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

    <!-- 评分对话框 -->
    <el-dialog v-model="assessDialogVisible" title="考核评分" width="500px">
      <el-form ref="assessFormRef" :model="assessForm" :rules="assessRules" label-width="100px">
        <el-form-item label="总得分" prop="totalScore">
          <el-input-number v-model="assessForm.totalScore" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="等级" prop="grade">
          <el-select v-model="assessForm.grade" style="width: 100%">
            <el-option label="A - 优秀" value="A" />
            <el-option label="B - 良好" value="B" />
            <el-option label="C - 合格" value="C" />
            <el-option label="D - 待改进" value="D" />
            <el-option label="E - 不合格" value="E" />
          </el-select>
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="assessForm.assessorComment" type="textarea" rows="3" placeholder="请输入考核评语" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assessDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAssess" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 评分明细对话框 -->
    <el-dialog v-model="detailDialogVisible" title="评分明细" width="700px">
      <el-descriptions :column="2" border class="detail-desc">
        <el-descriptions-item label="员工">{{ getEmployeeName(currentResult?.employeeId) }}</el-descriptions-item>
        <el-descriptions-item label="总得分">{{ currentResult?.totalScore ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="等级">{{ currentResult?.grade || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusMap[currentResult?.status] }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="scoreDetails" v-loading="detailLoading" stripe style="margin-top: 16px">
        <el-table-column label="指标" width="160">
          <template #default="{ row }">{{ getIndicatorName(row.indicatorId) }}</template>
        </el-table-column>
        <el-table-column prop="selfScore" label="自评得分" width="100" align="center" />
        <el-table-column prop="assessorScore" label="考核人评分" width="110" align="center" />
        <el-table-column prop="finalScore" label="最终得分" width="100" align="center" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      </el-table>

      <el-empty v-if="!detailLoading && scoreDetails.length === 0" description="暂无评分明细" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageAssessResult, assessScore, deleteAssessResult, getScoreDetailByResult } from '@/api/assess-result'
import { listEmployee } from '@/api/employee'
import { pageAssessPlan, getIndicatorsByPlan } from '@/api/assess'

const loading = ref(false)
const submitting = ref(false)
const detailLoading = ref(false)
const assessDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const tableData = ref([])
const employeeList = ref([])
const planList = ref([])
const scoreDetails = ref([])
const indicatorList = ref([])
const currentResult = ref(null)
const assessFormRef = ref()

const searchForm = reactive({ planId: null, employeeId: null, status: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const statusMap = { 0: '待考核', 1: '已自评', 2: '已考核', 3: '已确认' }
const statusTagMap = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success' }
const gradeTagMap = { A: 'success', B: 'primary', C: 'warning', D: 'danger', E: 'danger' }

const assessForm = reactive({ id: null, totalScore: 0, grade: 'C', assessorComment: '' })
const assessRules = {
  totalScore: [{ required: true, message: '请输入总得分', trigger: 'change' }],
  grade: [{ required: true, message: '请选择等级', trigger: 'change' }],
}

const getEmployeeName = (id) => {
  const emp = employeeList.value.find(e => e.id === id)
  return emp ? `${emp.empName} (${emp.empCode})` : id || '-'
}
const getPlanName = (id) => {
  const p = planList.value.find(p => p.id === id)
  return p ? p.planName : id || '-'
}
const getIndicatorName = (id) => {
  const ind = indicatorList.value.find(i => i.id === id)
  return ind ? ind.indicatorName : id || '-'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageAssessResult({ ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const fetchBaseData = async () => {
  try {
    const [empRes, planRes] = await Promise.all([
      listEmployee(),
      pageAssessPlan({ pageNum: 1, pageSize: 200 })
    ])
    employeeList.value = empRes.data || []
    planList.value = planRes.data?.records || []
  } catch (e) { console.error(e) }
}

const handleReset = () => {
  searchForm.planId = null; searchForm.employeeId = null; searchForm.status = null
  pagination.pageNum = 1; fetchData()
}

const handleAssess = (row) => {
  assessForm.id = row.id
  assessForm.totalScore = row.totalScore || 0
  assessForm.grade = row.grade || 'C'
  assessForm.assessorComment = row.assessorComment || ''
  assessDialogVisible.value = true
}

const handleSubmitAssess = async () => {
  await assessFormRef.value.validate()
  submitting.value = true
  try {
    await assessScore(assessForm.id, assessForm)
    ElMessage.success('评分成功')
    assessDialogVisible.value = false
    fetchData()
  } catch (e) { console.error(e) } finally { submitting.value = false }
}

const handleViewDetail = async (row) => {
  currentResult.value = row
  detailDialogVisible.value = true
  detailLoading.value = true
  try {
    // 加载指标列表和评分明细
    const [detailRes] = await Promise.all([
      getScoreDetailByResult(row.id),
    ])
    scoreDetails.value = detailRes.data || []
    // 加载该方案的指标
    if (row.planId) {
      const indRes = await getIndicatorsByPlan(row.planId)
      indicatorList.value = indRes.data || []
    }
  } catch (e) { console.error(e) } finally { detailLoading.value = false }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该考核结果吗？', '提示', { type: 'warning' })
    await deleteAssessResult(row.id); ElMessage.success('删除成功'); fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

onMounted(() => { fetchBaseData(); fetchData() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
.detail-desc { margin-bottom: 8px; }
</style>
