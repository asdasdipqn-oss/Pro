<template>
  <div class="salary-standard-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>薪资标准管理</span>
          <el-button type="primary" @click="handleAdd">新增薪资标准</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="员工">
          <el-select v-model="searchForm.employeeId" placeholder="全部员工" clearable filterable>
            <el-option v-for="emp in employeeList" :key="emp.id"
              :label="`${emp.empName} (${emp.empCode})`" :value="emp.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="薪资项目">
          <el-select v-model="searchForm.itemId" placeholder="全部项目" clearable>
            <el-option v-for="item in salaryItemList" :key="item.id"
              :label="item.itemName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column label="员工" width="160">
          <template #default="{ row }">
            {{ getEmployeeName(row.employeeId) }}
          </template>
        </el-table-column>
        <el-table-column label="薪资项目" width="150">
          <template #default="{ row }">
            {{ getItemName(row.itemId) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="120" align="right">
          <template #default="{ row }">
            <span style="font-weight: bold; color: #409eff">{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveDate" label="生效日期" width="130" />
        <el-table-column prop="expireDate" label="失效日期" width="130">
          <template #default="{ row }">
            {{ row.expireDate || '长期' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
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
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑薪资标准' : '新增薪资标准'" width="550px">
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="editForm.employeeId" placeholder="选择员工" filterable style="width: 100%"
            :disabled="!!editForm.id">
            <el-option v-for="emp in employeeList" :key="emp.id"
              :label="`${emp.empName} (${emp.empCode})`" :value="emp.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="薪资项目" prop="itemId">
          <el-select v-model="editForm.itemId" placeholder="选择薪资项目" style="width: 100%"
            :disabled="!!editForm.id">
            <el-option v-for="item in salaryItemList" :key="item.id"
              :label="item.itemName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="editForm.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="生效日期" prop="effectiveDate">
          <el-date-picker v-model="editForm.effectiveDate" type="date"
            placeholder="选择生效日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="失效日期">
          <el-date-picker v-model="editForm.expireDate" type="date"
            placeholder="不填表示长期有效" value-format="YYYY-MM-DD" style="width: 100%" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageSalaryStandard, addSalaryStandard, updateSalaryStandard, deleteSalaryStandard,
  listSalaryItem
} from '@/api/salary'
import { listEmployee } from '@/api/employee'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const tableData = ref([])
const employeeList = ref([])
const salaryItemList = ref([])

const searchForm = reactive({ employeeId: null, itemId: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const defaultForm = () => ({
  id: null, employeeId: null, itemId: null, amount: 0, effectiveDate: '', expireDate: ''
})

const editForm = reactive(defaultForm())

const formRules = {
  employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  itemId: [{ required: true, message: '请选择薪资项目', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }],
}

const getEmployeeName = (id) => {
  const emp = employeeList.value.find(e => e.id === id)
  return emp ? `${emp.empName} (${emp.empCode})` : id
}

const getItemName = (id) => {
  const item = salaryItemList.value.find(i => i.id === id)
  return item ? item.itemName : id
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageSalaryStandard({
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

const fetchBaseData = async () => {
  try {
    const [empRes, itemRes] = await Promise.all([
      listEmployee(),
      listSalaryItem()
    ])
    employeeList.value = empRes.data || []
    salaryItemList.value = itemRes.data || []
  } catch (error) {
    console.error(error)
  }
}

const handleReset = () => {
  searchForm.employeeId = null
  searchForm.itemId = null
  pagination.pageNum = 1
  fetchData()
}

const handleAdd = () => {
  Object.assign(editForm, defaultForm())
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(editForm, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editForm.id) {
      await updateSalaryStandard(editForm)
      ElMessage.success('修改成功')
    } else {
      await addSalaryStandard(editForm)
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
    await ElMessageBox.confirm('确定删除该薪资标准吗？', '提示', { type: 'warning' })
    await deleteSalaryStandard(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

onMounted(() => {
  fetchBaseData()
  fetchData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
