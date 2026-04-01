<template>
  <div class="assess-plan-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考核方案管理</span>
          <el-button type="primary" @click="handleAdd">新建考核方案</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="方案名称">
          <el-input v-model="searchForm.planName" placeholder="请输入方案名称" clearable />
        </el-form-item>
        <el-form-item label="考核类型">
          <el-select v-model="searchForm.assessType" placeholder="全部" clearable>
            <el-option label="月度考核" :value="1" />
            <el-option label="季度考核" :value="2" />
            <el-option label="年度考核" :value="3" />
            <el-option label="试用期考核" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="考核中" :value="2" />
            <el-option label="已结束" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="planCode" label="方案编号" width="180" />
        <el-table-column prop="planName" label="方案名称" width="200" />
        <el-table-column label="考核类型" width="120">
          <template #default="{ row }">
            {{ assessTypeMap[row.assessType] }}
          </template>
        </el-table-column>
        <el-table-column prop="assessPeriod" label="考核周期" width="120" />
        <el-table-column label="考核时间" width="200">
          <template #default="{ row }"> {{ row.startDate }} ~ {{ row.endDate }} </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="info" link @click="handleIndicators(row)">指标</el-button>
            <el-button v-if="row.status === 0" type="success" link @click="handlePublish(row)"
              >发布</el-button
            >
            <el-button
              v-if="row.status === 1 || row.status === 2"
              type="warning"
              link
              @click="handleFinish(row)"
              >结束</el-button
            >
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
      :title="editForm.id ? '编辑考核方案' : '新建考核方案'"
      width="600px"
    >
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="方案名称" prop="planName">
          <el-input v-model="editForm.planName" placeholder="请输入方案名称" />
        </el-form-item>
        <el-form-item label="考核类型" prop="assessType">
          <el-select v-model="editForm.assessType" placeholder="请选择考核类型" style="width: 100%">
            <el-option label="月度考核" :value="1" />
            <el-option label="季度考核" :value="2" />
            <el-option label="年度考核" :value="3" />
            <el-option label="试用期考核" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="考核年份" prop="assessYear">
          <el-date-picker
            v-model="editForm.assessYear"
            type="year"
            placeholder="选择年份"
            value-format="YYYY"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="考核周期" prop="assessPeriod">
          <el-input v-model="editForm.assessPeriod" placeholder="如：2024Q1、202401" />
        </el-form-item>
        <el-form-item label="考核时间" prop="dateRange">
          <el-date-picker
            v-model="editForm.dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="考核说明">
          <el-input
            v-model="editForm.description"
            type="textarea"
            rows="3"
            placeholder="请输入考核说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 考核指标管理对话框 -->
    <el-dialog
      v-model="indicatorDialogVisible"
      :title="`考核指标 - ${currentPlan.planName || ''}`"
      width="800px"
    >
      <div style="margin-bottom: 12px; display: flex; justify-content: flex-end">
        <el-button type="primary" size="small" @click="handleAddIndicator">新增指标</el-button>
      </div>
      <el-table :data="indicatorList" v-loading="indicatorLoading" stripe size="small">
        <el-table-column prop="indicatorName" label="指标名称" min-width="150" />
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.indicatorType === 1 ? 'primary' : 'success'" size="small">
              {{ row.indicatorType === 1 ? '定量' : '定性' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="权重(%)" width="90" align="center" />
        <el-table-column prop="maxScore" label="满分" width="70" align="center" />
        <el-table-column prop="description" label="说明" min-width="150" show-overflow-tooltip />
        <el-table-column
          prop="scoringStandard"
          label="评分标准"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column prop="sort" label="排序" width="60" align="center" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditIndicator(row)"
              >编辑</el-button
            >
            <el-button type="danger" link size="small" @click="handleDeleteIndicator(row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 新增/编辑指标对话框 -->
    <el-dialog
      v-model="indicatorFormVisible"
      :title="indicatorForm.id ? '编辑指标' : '新增指标'"
      width="550px"
      append-to-body
    >
      <el-form
        ref="indicatorFormRef"
        :model="indicatorForm"
        :rules="indicatorRules"
        label-width="100px"
      >
        <el-form-item label="指标名称" prop="indicatorName">
          <el-input v-model="indicatorForm.indicatorName" placeholder="如 工作业绩" />
        </el-form-item>
        <el-form-item label="指标类型" prop="indicatorType">
          <el-select v-model="indicatorForm.indicatorType" style="width: 100%">
            <el-option label="定量" :value="1" />
            <el-option label="定性" :value="2" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权重(%)" prop="weight">
              <el-input-number
                v-model="indicatorForm.weight"
                :min="0"
                :max="100"
                :precision="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="满分" prop="maxScore">
              <el-input-number v-model="indicatorForm.maxScore" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="排序">
          <el-input-number v-model="indicatorForm.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="指标说明">
          <el-input v-model="indicatorForm.description" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="评分标准">
          <el-input
            v-model="indicatorForm.scoringStandard"
            type="textarea"
            rows="2"
            placeholder="如：90-100分优秀，70-89分良好..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="indicatorFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleIndicatorSubmit" :loading="indicatorSubmitting"
          >确定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageAssessPlan,
  addAssessPlan,
  updateAssessPlan,
  publishAssessPlan,
  finishAssessPlan,
  deleteAssessPlan,
  getIndicatorsByPlan,
  addIndicator,
  updateIndicator,
  deleteIndicator,
} from '@/api/assess'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const tableData = ref([])

const searchForm = reactive({ planName: '', assessType: null, status: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const editForm = reactive({
  id: null,
  planName: '',
  assessType: null,
  assessYear: null,
  assessPeriod: '',
  dateRange: [],
  description: '',
})

const formRules = {
  planName: [{ required: true, message: '请输入方案名称', trigger: 'blur' }],
  assessType: [{ required: true, message: '请选择考核类型', trigger: 'change' }],
  assessYear: [{ required: true, message: '请选择考核年份', trigger: 'change' }],
  assessPeriod: [{ required: true, message: '请输入考核周期', trigger: 'blur' }],
}

const assessTypeMap = { 1: '月度考核', 2: '季度考核', 3: '年度考核', 4: '试用期考核' }
const statusMap = { 0: '草稿', 1: '已发布', 2: '考核中', 3: '已结束' }
const statusTypeMap = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'success' }

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageAssessPlan({
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

const handleReset = () => {
  searchForm.planName = ''
  searchForm.assessType = null
  searchForm.status = null
  pagination.pageNum = 1
  fetchData()
}

const resetForm = () => {
  Object.assign(editForm, {
    id: null,
    planName: '',
    assessType: null,
    assessYear: null,
    assessPeriod: '',
    dateRange: [],
    description: '',
  })
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(editForm, {
    id: row.id,
    planName: row.planName,
    assessType: row.assessType,
    assessYear: row.assessYear?.toString(),
    assessPeriod: row.assessPeriod,
    dateRange: row.startDate && row.endDate ? [row.startDate, row.endDate] : [],
    description: row.description,
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    const data = {
      ...editForm,
      assessYear: parseInt(editForm.assessYear),
      startDate: editForm.dateRange?.[0],
      endDate: editForm.dateRange?.[1],
    }
    delete data.dateRange

    if (editForm.id) {
      await updateAssessPlan(data)
      ElMessage.success('更新成功')
    } else {
      await addAssessPlan(data)
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

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确定要发布该考核方案吗？', '提示', { type: 'warning' })
    await publishAssessPlan(row.id)
    ElMessage.success('发布成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleFinish = async (row) => {
  try {
    await ElMessageBox.confirm('确定要结束该考核方案吗？', '提示', { type: 'warning' })
    await finishAssessPlan(row.id)
    ElMessage.success('已结束')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该考核方案吗？', '提示', { type: 'warning' })
    await deleteAssessPlan(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

// ===================== 考核指标管理 =====================
const indicatorDialogVisible = ref(false)
const indicatorFormVisible = ref(false)
const indicatorLoading = ref(false)
const indicatorSubmitting = ref(false)
const indicatorFormRef = ref(null)
const indicatorList = ref([])
const currentPlan = ref({})

const defaultIndicatorForm = () => ({
  id: null,
  planId: null,
  indicatorName: '',
  indicatorType: 1,
  weight: 0,
  maxScore: 100,
  description: '',
  scoringStandard: '',
  sort: 0,
})
const indicatorForm = reactive(defaultIndicatorForm())

const indicatorRules = {
  indicatorName: [{ required: true, message: '请输入指标名称', trigger: 'blur' }],
  indicatorType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  weight: [{ required: true, message: '请输入权重', trigger: 'blur' }],
  maxScore: [{ required: true, message: '请输入满分', trigger: 'blur' }],
}

const handleIndicators = async (row) => {
  currentPlan.value = row
  indicatorDialogVisible.value = true
  indicatorLoading.value = true
  try {
    const res = await getIndicatorsByPlan(row.id)
    indicatorList.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    indicatorLoading.value = false
  }
}

const handleAddIndicator = () => {
  Object.assign(indicatorForm, defaultIndicatorForm())
  indicatorForm.planId = currentPlan.value.id
  indicatorFormVisible.value = true
}

const handleEditIndicator = (row) => {
  Object.assign(indicatorForm, { ...row })
  indicatorFormVisible.value = true
}

const handleIndicatorSubmit = async () => {
  await indicatorFormRef.value.validate()
  indicatorSubmitting.value = true
  try {
    if (indicatorForm.id) {
      await updateIndicator(indicatorForm)
      ElMessage.success('修改成功')
    } else {
      await addIndicator(indicatorForm)
      ElMessage.success('新增成功')
    }
    indicatorFormVisible.value = false
    handleIndicators(currentPlan.value)
  } catch (error) {
    console.error(error)
  } finally {
    indicatorSubmitting.value = false
  }
}

const handleDeleteIndicator = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除指标"${row.indicatorName}"吗？`, '提示', {
      type: 'warning',
    })
    await deleteIndicator(row.id)
    ElMessage.success('删除成功')
    handleIndicators(currentPlan.value)
  } catch (error) {
    if (error !== 'cancel') console.error(error)
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
</style>
