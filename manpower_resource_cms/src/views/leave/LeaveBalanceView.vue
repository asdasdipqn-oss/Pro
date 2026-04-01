<template>
  <div class="leave-balance-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>假期额度管理</span>
          <div class="actions">
            <el-button type="success" @click="handleInitYear">一键初始化年度额度</el-button>
            <el-button type="primary" @click="handleAdd">新增额度</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="员工">
          <el-select v-model="searchForm.employeeId" placeholder="全部员工" clearable filterable>
            <el-option v-for="emp in employeeList" :key="emp.id"
              :label="`${emp.empName} (${emp.empCode})`" :value="emp.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="假期类型">
          <el-select v-model="searchForm.leaveTypeId" placeholder="全部类型" clearable>
            <el-option v-for="lt in leaveTypeList" :key="lt.id"
              :label="lt.typeName" :value="lt.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年度">
          <el-date-picker v-model="searchYear" type="year" placeholder="选择年度"
            format="YYYY" @change="handleYearChange" style="width: 120px" />
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
        <el-table-column label="假期类型" width="120">
          <template #default="{ row }">
            {{ getLeaveTypeName(row.leaveTypeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="year" label="年度" width="80" align="center" />
        <el-table-column prop="totalDays" label="总额度(天)" width="110" align="right">
          <template #default="{ row }">
            <span style="font-weight: bold">{{ row.totalDays }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="usedDays" label="已使用(天)" width="110" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.usedDays > 0 ? '#e6a23c' : '' }">{{ row.usedDays }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remainingDays" label="剩余(天)" width="110" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.remainingDays <= 0 ? '#f56c6c' : '#67c23a', fontWeight: 'bold' }">
              {{ row.remainingDays }}
            </span>
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
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑假期额度' : '新增假期额度'" width="500px">
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="editForm.employeeId" placeholder="选择员工" filterable style="width: 100%"
            :disabled="!!editForm.id">
            <el-option v-for="emp in employeeList" :key="emp.id"
              :label="`${emp.empName} (${emp.empCode})`" :value="emp.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="假期类型" prop="leaveTypeId">
          <el-select v-model="editForm.leaveTypeId" placeholder="选择类型" style="width: 100%"
            :disabled="!!editForm.id">
            <el-option v-for="lt in leaveTypeList" :key="lt.id"
              :label="lt.typeName" :value="lt.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年度" prop="year">
          <el-input-number v-model="editForm.year" :min="2020" :max="2030" style="width: 100%"
            :disabled="!!editForm.id" />
        </el-form-item>
        <el-form-item label="总额度(天)" prop="totalDays">
          <el-input-number v-model="editForm.totalDays" :min="0" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="已使用(天)" v-if="editForm.id">
          <el-input-number v-model="editForm.usedDays" :min="0" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="剩余(天)" v-if="editForm.id">
          <el-input-number v-model="editForm.remainingDays" :min="0" :precision="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 年度初始化对话框 -->
    <el-dialog v-model="initDialogVisible" title="初始化年度假期额度" width="400px">
      <el-form label-width="80px">
        <el-form-item label="年度">
          <el-input-number v-model="initYear" :min="2020" :max="2030" style="width: 100%" />
        </el-form-item>
        <el-alert type="info" :closable="false" style="margin-top: 10px">
          将为所有在职员工按各假期类型的默认额度自动创建假期余额记录（已存在的不会重复创建）
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="initDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleInitSubmit" :loading="submitting">确定初始化</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageLeaveBalance, addLeaveBalance, updateLeaveBalance, deleteLeaveBalance, initYearBalance } from '@/api/leave-balance'
import { listLeaveType } from '@/api/leave'
import { listEmployee } from '@/api/employee'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const initDialogVisible = ref(false)
const formRef = ref()
const tableData = ref([])
const employeeList = ref([])
const leaveTypeList = ref([])

const currentYear = new Date().getFullYear()
const searchYear = ref(new Date())
const initYear = ref(currentYear)

const searchForm = reactive({ employeeId: null, leaveTypeId: null, year: currentYear })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const defaultForm = () => ({
  id: null, employeeId: null, leaveTypeId: null, year: currentYear,
  totalDays: 0, usedDays: 0, remainingDays: 0
})

const editForm = reactive(defaultForm())

const formRules = {
  employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  leaveTypeId: [{ required: true, message: '请选择假期类型', trigger: 'change' }],
  year: [{ required: true, message: '请输入年度', trigger: 'blur' }],
  totalDays: [{ required: true, message: '请输入总额度', trigger: 'blur' }],
}

const getEmployeeName = (id) => {
  const emp = employeeList.value.find(e => e.id === id)
  return emp ? `${emp.empName} (${emp.empCode})` : id
}

const getLeaveTypeName = (id) => {
  const lt = leaveTypeList.value.find(t => t.id === id)
  return lt ? lt.typeName : id
}

const handleYearChange = (val) => {
  searchForm.year = val ? new Date(val).getFullYear() : null
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageLeaveBalance({
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
    const [empRes, ltRes] = await Promise.all([
      listEmployee(),
      listLeaveType()
    ])
    employeeList.value = empRes.data || []
    leaveTypeList.value = ltRes.data || []
  } catch (error) {
    console.error(error)
  }
}

const handleReset = () => {
  searchForm.employeeId = null
  searchForm.leaveTypeId = null
  searchForm.year = currentYear
  searchYear.value = new Date()
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
      await updateLeaveBalance(editForm)
      ElMessage.success('修改成功')
    } else {
      await addLeaveBalance(editForm)
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
    await ElMessageBox.confirm('确定删除该假期额度记录吗？', '提示', { type: 'warning' })
    await deleteLeaveBalance(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleInitYear = () => {
  initYear.value = currentYear
  initDialogVisible.value = true
}

const handleInitSubmit = async () => {
  submitting.value = true
  try {
    await initYearBalance(initYear.value)
    ElMessage.success(`${initYear.value}年度假期额度初始化成功`)
    initDialogVisible.value = false
    searchForm.year = initYear.value
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchBaseData()
  fetchData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.actions { display: flex; gap: 8px; }
.search-form { margin-bottom: 20px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
