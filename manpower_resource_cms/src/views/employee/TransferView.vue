<template>
  <div class="transfer-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>员工异动管理</span>
          <el-button type="primary" @click="handleAdd">新增异动</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="员工">
          <el-select v-model="searchForm.employeeId" placeholder="全部员工" clearable filterable>
            <el-option v-for="emp in employeeList" :key="emp.id"
              :label="`${emp.empName} (${emp.empCode})`" :value="emp.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="异动类型">
          <el-select v-model="searchForm.transferType" placeholder="全部" clearable>
            <el-option label="入职" :value="1" />
            <el-option label="转正" :value="2" />
            <el-option label="调岗" :value="3" />
            <el-option label="晋升" :value="4" />
            <el-option label="降级" :value="5" />
            <el-option label="离职" :value="6" />
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
        <el-table-column label="异动类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.transferType]" size="small">{{ typeMap[row.transferType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="原部门" width="130">
          <template #default="{ row }">{{ getDeptName(row.oldDeptId) }}</template>
        </el-table-column>
        <el-table-column label="新部门" width="130">
          <template #default="{ row }">{{ getDeptName(row.newDeptId) }}</template>
        </el-table-column>
        <el-table-column label="原岗位" width="130">
          <template #default="{ row }">{{ getPositionName(row.oldPositionId) }}</template>
        </el-table-column>
        <el-table-column label="新岗位" width="130">
          <template #default="{ row }">{{ getPositionName(row.newPositionId) }}</template>
        </el-table-column>
        <el-table-column prop="effectiveDate" label="生效日期" width="120" />
        <el-table-column prop="reason" label="异动原因" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
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

    <el-dialog v-model="dialogVisible" title="新增异动记录" width="600px">
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="editForm.employeeId" placeholder="选择员工" filterable style="width: 100%"
            @change="handleEmployeeChange">
            <el-option v-for="emp in employeeList" :key="emp.id"
              :label="`${emp.empName} (${emp.empCode})`" :value="emp.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="异动类型" prop="transferType">
          <el-select v-model="editForm.transferType" style="width: 100%">
            <el-option label="入职" :value="1" />
            <el-option label="转正" :value="2" />
            <el-option label="调岗" :value="3" />
            <el-option label="晋升" :value="4" />
            <el-option label="降级" :value="5" />
            <el-option label="离职" :value="6" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="原部门">
              <el-input :value="getDeptName(editForm.oldDeptId)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新部门">
              <el-select v-model="editForm.newDeptId" placeholder="选择新部门" clearable style="width: 100%">
                <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="原岗位">
              <el-input :value="getPositionName(editForm.oldPositionId)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新岗位">
              <el-select v-model="editForm.newPositionId" placeholder="选择新岗位" clearable style="width: 100%">
                <el-option v-for="p in positionList" :key="p.id" :label="p.positionName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="生效日期" prop="effectiveDate">
          <el-date-picker v-model="editForm.effectiveDate" type="date"
            placeholder="选择生效日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="异动原因" prop="reason">
          <el-input v-model="editForm.reason" type="textarea" rows="2" placeholder="请输入异动原因" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" rows="2" />
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
import { pageTransfer, addTransfer, deleteTransfer } from '@/api/transfer'
import { listEmployee } from '@/api/employee'
import { getDepartmentTree } from '@/api/department'
import { listPosition } from '@/api/position'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const tableData = ref([])
const employeeList = ref([])
const deptList = ref([])
const positionList = ref([])

const searchForm = reactive({ employeeId: null, transferType: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const typeMap = { 1: '入职', 2: '转正', 3: '调岗', 4: '晋升', 5: '降级', 6: '离职' }
const typeTagMap = { 1: 'success', 2: 'primary', 3: 'warning', 4: 'success', 5: 'danger', 6: 'info' }

const defaultForm = () => ({
  employeeId: null, transferType: 3, oldDeptId: null, oldPositionId: null,
  newDeptId: null, newPositionId: null, effectiveDate: '', reason: '', remark: ''
})
const editForm = reactive(defaultForm())

const formRules = {
  employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  transferType: [{ required: true, message: '请选择异动类型', trigger: 'change' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }],
  reason: [{ required: true, message: '请输入异动原因', trigger: 'blur' }],
}

const flattenDept = (tree, result = []) => {
  for (const node of tree) {
    result.push(node)
    if (node.children) flattenDept(node.children, result)
  }
  return result
}

const getEmployeeName = (id) => {
  const emp = employeeList.value.find(e => e.id === id)
  return emp ? `${emp.empName} (${emp.empCode})` : id || '-'
}
const getDeptName = (id) => {
  const d = deptList.value.find(d => d.id === id)
  return d ? d.deptName : id || '-'
}
const getPositionName = (id) => {
  const p = positionList.value.find(p => p.id === id)
  return p ? p.positionName : id || '-'
}

const handleEmployeeChange = (empId) => {
  const emp = employeeList.value.find(e => e.id === empId)
  if (emp) {
    editForm.oldDeptId = emp.deptId
    editForm.oldPositionId = emp.positionId
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageTransfer({ ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const fetchBaseData = async () => {
  try {
    const [empRes, deptRes, posRes] = await Promise.all([
      listEmployee(), getDepartmentTree(), listPosition()
    ])
    employeeList.value = empRes.data || []
    deptList.value = flattenDept(deptRes.data || [])
    positionList.value = posRes.data || []
  } catch (e) { console.error(e) }
}

const handleReset = () => { searchForm.employeeId = null; searchForm.transferType = null; pagination.pageNum = 1; fetchData() }
const handleAdd = () => { Object.assign(editForm, defaultForm()); dialogVisible.value = true }

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await addTransfer(editForm)
    ElMessage.success('异动记录创建成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) { console.error(e) } finally { submitting.value = false }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该异动记录吗？', '提示', { type: 'warning' })
    await deleteTransfer(row.id); ElMessage.success('删除成功'); fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

onMounted(() => { fetchBaseData(); fetchData() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
