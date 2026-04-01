<template>
  <div class="employee-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-info">
        <h2>员工列表</h2>
        <p>共 {{ total }} 名员工</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">新增员工</el-button>
        <el-button @click="handleExport">导出</el-button>
        <el-button @click="showImportDialog = true">导入</el-button>
        <el-button @click="handleDownloadTemplate">下载模板</el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-card">
      <div class="search-row">
        <div class="search-item">
          <label>工号</label>
          <el-input v-model="queryForm.empCode" placeholder="输入工号" clearable />
        </div>
        <div class="search-item">
          <label>姓名</label>
          <el-input v-model="queryForm.empName" placeholder="输入姓名" clearable />
        </div>
        <div class="search-item">
          <label>部门</label>
          <el-select v-model="queryForm.deptId" placeholder="选择部门" clearable>
            <el-option
              v-for="dept in deptList"
              :key="dept.id"
              :label="dept.deptName"
              :value="dept.id"
            />
          </el-select>
        </div>
        <div class="search-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑员工' : '新增员工'" width="700px">
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工号" prop="empCode">
              <el-input v-model="editForm.empCode" placeholder="请输入工号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="empName">
              <el-input v-model="editForm.empName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="editForm.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="editForm.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门" prop="deptId">
              <el-select v-model="editForm.deptId" placeholder="请选择部门" style="width: 100%">
                <el-option
                  v-for="dept in deptList"
                  :key="dept.id"
                  :label="dept.deptName"
                  :value="dept.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位" prop="positionId">
              <el-select v-model="editForm.positionId" placeholder="请选择岗位" style="width: 100%">
                <el-option
                  v-for="pos in positionList"
                  :key="pos.id"
                  :label="pos.positionName"
                  :value="pos.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入职日期" prop="hireDate">
              <el-date-picker
                v-model="editForm.hireDate"
                type="date"
                placeholder="选择入职日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="empStatus">
              <el-select v-model="editForm.empStatus" placeholder="请选择状态" style="width: 100%">
                <el-option label="在职" :value="1" />
                <el-option label="离职" :value="2" />
                <el-option label="试用期" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="身份证号">
          <el-input v-model="editForm.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="editForm.address" placeholder="请输入地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog v-model="showImportDialog" title="导入员工" width="500px">
      <el-upload
        ref="uploadRef"
        drag
        action="#"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">只能上传xlsx/xls文件</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button type="primary" @click="handleImport" :loading="importing">确认导入</el-button>
      </template>
    </el-dialog>

    <!-- 表格 -->
    <div class="table-card">
      <!-- 批量操作栏 -->
      <div class="batch-actions" v-if="selectedRows.length > 0">
        <span>已选择 {{ selectedRows.length }} 项</span>
        <el-button size="small" @click="handleBatchUpdateStatus(1)">批量设为在职</el-button>
        <el-button size="small" @click="handleBatchUpdateStatus(2)">批量设为离职</el-button>
        <el-button size="small" type="danger" @click="handleBatchDelete">批量删除</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="empCode" label="工号" min-width="100" />
        <el-table-column prop="empName" label="姓名" min-width="120">
          <template #default="{ row }">
            <div class="employee-name">
              <div class="avatar">{{ row.empName?.charAt(0) }}</div>
              <span>{{ row.empName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" min-width="60">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="departmentName" label="部门" min-width="100" />
        <el-table-column prop="positionName" label="岗位" min-width="100" />
        <el-table-column prop="empStatus" label="状态" min-width="80">
          <template #default="{ row }">
            <span :class="['status-badge', getStatusClass(row.empStatus)]">
              {{ getStatusText(row.empStatus) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="hireDate" label="入职日期" min-width="100" />
        <el-table-column label="操作" min-width="120" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <span class="pagination-info">
          显示 {{ (queryForm.pageNum - 1) * queryForm.pageSize + 1 }} -
          {{ Math.min(queryForm.pageNum * queryForm.pageSize, total) }} 条
        </span>
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, UploadFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageEmployee,
  addEmployee,
  updateEmployee,
  deleteEmployee,
  batchDeleteEmployee,
  batchUpdateStatus,
  exportEmployee,
  downloadTemplate,
  importEmployee,
} from '@/api/employee'
import { getDepartmentTree } from '@/api/department'
import { listPosition } from '@/api/position'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const showImportDialog = ref(false)
const importing = ref(false)
const tableData = ref([])
const total = ref(0)
const deptList = ref([])
const positionList = ref([])
const formRef = ref(null)
const uploadRef = ref(null)
const importFile = ref(null)
const selectedRows = ref([])

const queryForm = reactive({
  empCode: '',
  empName: '',
  deptId: null,
  pageNum: 1,
  pageSize: 10,
})

const editForm = reactive({
  id: null,
  empCode: '',
  empName: '',
  gender: 1,
  phone: '',
  deptId: null,
  positionId: null,
  hireDate: '',
  empStatus: 1,
  idCard: '',
  email: '',
  address: '',
})

const formRules = {
  empCode: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  empName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  positionId: [{ required: true, message: '请选择岗位', trigger: 'change' }],
  hireDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }],
  empStatus: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const getStatusClass = (status) => {
  const map = { 1: 'active', 2: 'inactive', 3: 'pending' }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = { 1: '在职', 2: '离职', 3: '试用期' }
  return map[status] || '未知'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageEmployee(queryForm)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    // 忽略请求取消错误
    if (error.message !== 'cancel') {
      console.error('获取员工列表失败:', error)
    }
  } finally {
    loading.value = false
  }
}

const fetchDepts = async () => {
  try {
    const res = await getDepartmentTree()
    deptList.value = flattenTree(res.data || [])
  } catch (error) {
    if (error.message !== 'cancel') {
      console.error('获取部门列表失败:', error)
    }
  }
}

const flattenTree = (tree, result = []) => {
  tree.forEach((node) => {
    result.push(node)
    if (node.children && node.children.length) {
      flattenTree(node.children, result)
    }
  })
  return result
}

const handleSearch = () => {
  queryForm.pageNum = 1
  fetchData()
}

const handleReset = () => {
  queryForm.empCode = ''
  queryForm.empName = ''
  queryForm.deptId = null
  queryForm.pageNum = 1
  fetchData()
}

const fetchPositions = async () => {
  try {
    const res = await listPosition()
    positionList.value = res.data || []
  } catch (error) {
    if (error.message !== 'cancel') {
      console.error('获取岗位列表失败:', error)
    }
  }
}

const resetForm = () => {
  Object.assign(editForm, {
    id: null,
    empCode: '',
    empName: '',
    gender: 1,
    phone: '',
    deptId: null,
    positionId: null,
    hireDate: '',
    empStatus: 1,
    idCard: '',
    email: '',
    address: '',
  })
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(editForm, {
    id: row.id,
    empCode: row.empCode,
    empName: row.empName,
    gender: row.gender,
    phone: row.phone,
    deptId: row.deptId,
    positionId: row.positionId,
    hireDate: row.hireDate,
    empStatus: row.empStatus,
    idCard: row.idCard,
    email: row.email,
    address: row.address,
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    if (editForm.id) {
      await updateEmployee(editForm)
      ElMessage.success('更新成功')
    } else {
      await addEmployee(editForm)
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
  await ElMessageBox.confirm('确定要删除该员工吗？', '提示', { type: 'warning' })
  try {
    await deleteEmployee(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

// 导出员工
const handleExport = async () => {
  try {
    const res = await exportEmployee({
      empCode: queryForm.empCode || '',
      empName: queryForm.empName || '',
      deptId: queryForm.deptId || '',
    })
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `员工列表_${new Date().toLocaleDateString()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error(error)
    ElMessage.error('导出失败')
  }
}

// 下载导入模板
const handleDownloadTemplate = async () => {
  try {
    const res = await downloadTemplate()
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '员工导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error(error)
    ElMessage.error('下载模板失败')
  }
}

// 文件变化
const handleFileChange = (file) => {
  importFile.value = file.raw
}

// 导入员工
const handleImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  importing.value = true
  const formData = new FormData()
  formData.append('file', importFile.value)

  try {
    const res = await importEmployee(formData)
    ElMessage.success(res.data || '导入成功')
    showImportDialog.value = false
    importFile.value = null
    uploadRef.value?.clearFiles()
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    importing.value = false
  }
}

// 选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  await ElMessageBox.confirm(`确定要删除选中的${selectedRows.value.length}名员工吗？`, '提示', {
    type: 'warning',
  })
  try {
    await batchDeleteEmployee(selectedRows.value.map((r) => r.id))
    ElMessage.success('批量删除成功')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

// 批量修改状态
const handleBatchUpdateStatus = async (status) => {
  if (selectedRows.value.length === 0) return
  const statusText = status === 1 ? '在职' : '离职'
  await ElMessageBox.confirm(
    `确定要将选中的${selectedRows.value.length}名员工设为${statusText}吗？`,
    '提示',
  )
  try {
    await batchUpdateStatus(
      selectedRows.value.map((r) => r.id),
      status,
    )
    ElMessage.success('批量修改成功')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchData()
  fetchDepts()
  fetchPositions()
})
</script>

<style scoped>
.employee-list {
  max-width: 1400px;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-info h2 {
  font-size: 22px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.header-info p {
  font-size: 14px;
  color: #86868b;
  margin: 4px 0 0 0;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  background: #1d1d1f;
  color: #ffffff;
  transition: all 0.2s;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f0f9ff;
  border-bottom: 1px solid #e0f2fe;
}

.batch-actions span {
  color: #0369a1;
  font-weight: 500;
}

/* 搜索栏 */
.search-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 16px;
}

.search-row {
  display: flex;
  align-items: flex-end;
  gap: 20px;
}

.search-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.search-item label {
  font-size: 12px;
  font-weight: 500;
  color: #86868b;
}

.search-item :deep(.el-input),
.search-item :deep(.el-select) {
  width: 180px;
}

.search-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

/* 表格卡片 */
.table-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 0;
  overflow: hidden;
}

.table-card :deep(.el-table) {
  --el-table-border-color: transparent;
}

.table-card :deep(.el-table th) {
  background: #fafafa !important;
  padding: 16px 12px !important;
}

.table-card :deep(.el-table td) {
  padding: 16px 12px !important;
}

/* 员工名称 */
.employee-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #1d1d1f;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 500;
}

/* 状态徽章 */
.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background: rgba(52, 199, 89, 0.12);
  color: #34c759;
}

.status-badge.inactive {
  background: rgba(255, 59, 48, 0.12);
  color: #ff3b30;
}

.status-badge.pending {
  background: rgba(255, 149, 0, 0.12);
  color: #ff9500;
}

/* 操作按钮 */
.action-btns {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-top: 1px solid #f5f5f7;
}

.pagination-info {
  font-size: 13px;
  color: #86868b;
}
</style>
