<template>
  <div class="salary-item-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>薪资项目管理</span>
          <el-button type="primary" @click="handleAdd">新增薪资项目</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="项目名称">
          <el-input v-model="searchForm.itemName" placeholder="请输入项目名称" clearable />
        </el-form-item>
        <el-form-item label="项目类型">
          <el-select v-model="searchForm.itemType" placeholder="全部" clearable>
            <el-option label="固定收入" :value="1" />
            <el-option label="浮动收入" :value="2" />
            <el-option label="扣款" :value="3" />
            <el-option label="社保" :value="4" />
            <el-option label="公积金" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="itemCode" label="项目编码" width="120" />
        <el-table-column prop="itemName" label="项目名称" width="150" />
        <el-table-column label="项目类型" width="110">
          <template #default="{ row }">
            <el-tag :type="itemTypeTagMap[row.itemType]">{{ itemTypeMap[row.itemType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="计算方式" width="110">
          <template #default="{ row }">
            {{ calcTypeMap[row.calcType] }}
          </template>
        </el-table-column>
        <el-table-column prop="calcFormula" label="计算公式" width="120" show-overflow-tooltip />
        <el-table-column label="映射字段" width="130">
          <template #default="{ row }">
            <el-tag v-if="row.recordField" type="info" size="small">{{ row.recordField }}</el-tag>
            <span v-else style="color: #c0c4cc">未设置</span>
          </template>
        </el-table-column>
        <el-table-column label="是否计税" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isTax === 1 ? 'warning' : 'info'" size="small">
              {{ row.isTax === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否必填" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isRequired === 1 ? 'danger' : 'info'" size="small">
              {{ row.isRequired === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑薪资项目' : '新增薪资项目'" width="600px">
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="项目编码" prop="itemCode">
          <el-input v-model="editForm.itemCode" placeholder="如 BASE_SALARY" />
        </el-form-item>
        <el-form-item label="项目名称" prop="itemName">
          <el-input v-model="editForm.itemName" placeholder="如 基本工资" />
        </el-form-item>
        <el-form-item label="项目类别" prop="itemType">
          <el-radio-group v-model="category" @change="onCategoryChange">
            <el-radio-button value="income">收入项</el-radio-button>
            <el-radio-button value="deduction">扣款项</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="项目类型" prop="itemType">
          <el-select v-model="editForm.itemType" placeholder="请选择" style="width: 100%">
            <el-option v-for="t in currentSubTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="计算方式" prop="calcType">
          <el-select v-model="editForm.calcType" placeholder="请选择" style="width: 100%">
            <el-option label="固定金额" :value="1" />
            <el-option label="按比例" :value="2" />
            <el-option label="按公式" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="计算公式">
          <el-input v-model="editForm.calcFormula" placeholder="如按比例时填写比例值" />
        </el-form-item>
        <el-form-item label="映射字段" prop="recordField">
          <el-select v-model="editForm.recordField" placeholder="选择映射到薪资记录的字段" style="width: 100%">
            <el-option-group label="收入项">
              <el-option v-for="f in incomeFields" :key="f.value" :label="f.label" :value="f.value" />
            </el-option-group>
            <el-option-group label="扣款项">
              <el-option v-for="f in deductionFields" :key="f.value" :label="f.label" :value="f.value" />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否计税">
              <el-switch v-model="editForm.isTax" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否必填">
              <el-switch v-model="editForm.isRequired" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="editForm.sort" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0"
                active-text="启用" inactive-text="禁用" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="说明">
          <el-input v-model="editForm.description" type="textarea" rows="2" placeholder="项目说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageSalaryItem, addSalaryItem, updateSalaryItem, deleteSalaryItem } from '@/api/salary'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const tableData = ref([])

const searchForm = reactive({ itemName: '', itemType: null, status: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const itemTypeMap = { 1: '固定收入', 2: '浮动收入', 3: '扣款', 4: '社保', 5: '公积金' }
const itemTypeTagMap = { 1: 'success', 2: 'primary', 3: 'danger', 4: 'warning', 5: '' }
const calcTypeMap = { 1: '固定金额', 2: '按比例', 3: '按公式' }

// 大类：收入项/扣款项
const categoryTypeMap = { income: '收入项', deduction: '扣款项' }
const incomeItemTypes = [
  { label: '固定收入', value: 1 },
  { label: '浮动收入', value: 2 },
]
const deductionItemTypes = [
  { label: '扣款', value: 3 },
  { label: '社保', value: 4 },
  { label: '公积金', value: 5 },
]

const category = ref('income')

const onCategoryChange = (val) => {
  // 切换大类时，自动选择第一个子类型
  const types = val === 'income' ? incomeItemTypes : deductionItemTypes
  editForm.itemType = types[0].value
}

const currentSubTypes = computed(() => {
  return category.value === 'income' ? incomeItemTypes : deductionItemTypes
})

// 映射字段选项
const incomeFields = [
  { label: '基本工资 (baseSalary)', value: 'baseSalary' },
  { label: '岗位工资 (positionSalary)', value: 'positionSalary' },
  { label: '绩效工资 (performanceSalary)', value: 'performanceSalary' },
  { label: '加班费 (overtimePay)', value: 'overtimePay' },
  { label: '全勤奖 (fullAttendanceBonus)', value: 'fullAttendanceBonus' },
  { label: '津贴 (allowance)', value: 'allowance' },
  { label: '奖金 (bonus)', value: 'bonus' },
]
const deductionFields = [
  { label: '社保扣款 (socialInsurance)', value: 'socialInsurance' },
  { label: '公积金 (housingFund)', value: 'housingFund' },
  { label: '个人所得税 (personalTax)', value: 'personalTax' },
  { label: '其他扣款 (otherDeduction)', value: 'otherDeduction' },
]

const defaultForm = () => ({
  id: null, itemCode: '', itemName: '', itemType: 1, calcType: 1,
  calcFormula: '', recordField: '', isTax: 0, isRequired: 0, sort: 0, status: 1, description: ''
})

const editForm = reactive(defaultForm())

const formRules = {
  itemCode: [{ required: true, message: '请输入项目编码', trigger: 'blur' }],
  itemName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  itemType: [{ required: true, message: '请选择项目类型', trigger: 'change' }],
  calcType: [{ required: true, message: '请选择计算方式', trigger: 'change' }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageSalaryItem({
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

const handleReset = () => {
  searchForm.itemName = ''
  searchForm.itemType = null
  searchForm.status = null
  pagination.pageNum = 1
  fetchData()
}

const handleAdd = () => {
  Object.assign(editForm, defaultForm())
  category.value = 'income'
  editForm.itemType = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(editForm, { ...row })
  category.value = [1, 2].includes(row.itemType) ? 'income' : 'deduction'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editForm.id) {
      await updateSalaryItem(editForm)
      ElMessage.success('修改成功')
    } else {
      await addSalaryItem(editForm)
      ElMessage.success('新增成功')
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
    await ElMessageBox.confirm(`确定删除薪资项目"${row.itemName}"吗？`, '提示', { type: 'warning' })
    await deleteSalaryItem(row.id)
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
