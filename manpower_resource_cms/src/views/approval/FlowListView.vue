<template>
  <div class="flow-list-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>审批流程配置</span>
          <el-button type="primary" @click="handleAdd">新增流程</el-button>
        </div>
      </template>
      
      <el-form :inline="true" class="search-form">
        <el-form-item label="流程类型">
          <el-select v-model="searchForm.flowType" placeholder="全部" clearable @change="fetchData">
            <el-option label="请假审批" :value="1" />
            <el-option label="考勤异常" :value="2" />
            <el-option label="调岗审批" :value="3" />
            <el-option label="离职审批" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable @change="fetchData">
            <el-option label="禁用" :value="0" />
            <el-option label="启用" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="flowCode" label="流程编码" width="150" />
        <el-table-column prop="flowName" label="流程名称" width="180" />
        <el-table-column prop="flowType" label="流程类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getFlowTypeColor(row.flowType)">{{ getFlowTypeName(row.flowType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
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
    
    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑流程' : '新增流程'" width="600px">
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="流程编码" prop="flowCode">
          <el-input v-model="editForm.flowCode" placeholder="请输入流程编码" :disabled="!!editForm.id" />
        </el-form-item>
        <el-form-item label="流程名称" prop="flowName">
          <el-input v-model="editForm.flowName" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="流程类型" prop="flowType">
          <el-select v-model="editForm.flowType" placeholder="请选择流程类型">
            <el-option label="请假审批" :value="1" />
            <el-option label="考勤异常" :value="2" />
            <el-option label="调岗审批" :value="3" />
            <el-option label="离职审批" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" rows="3" placeholder="请输入描述" />
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
import { pageApprovalFlow, createApprovalFlow, updateApprovalFlow, deleteApprovalFlow } from '@/api/approval'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const formRef = ref(null)

const searchForm = reactive({
  flowType: null,
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const editForm = reactive({
  id: null,
  flowCode: '',
  flowName: '',
  flowType: null,
  status: 1,
  description: ''
})

const formRules = {
  flowCode: [{ required: true, message: '请输入流程编码', trigger: 'blur' }],
  flowName: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
  flowType: [{ required: true, message: '请选择流程类型', trigger: 'change' }]
}

const getFlowTypeName = (type) => {
  const map = { 1: '请假审批', 2: '考勤异常', 3: '调岗审批', 4: '离职审批' }
  return map[type] || '未知'
}

const getFlowTypeColor = (type) => {
  const map = { 1: 'primary', 2: 'warning', 3: 'info', 4: 'danger' }
  return map[type] || 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageApprovalFlow({
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

const handleAdd = () => {
  Object.assign(editForm, {
    id: null,
    flowCode: '',
    flowName: '',
    flowType: null,
    status: 1,
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(editForm, {
    id: row.id,
    flowCode: row.flowCode,
    flowName: row.flowName,
    flowType: row.flowType,
    status: row.status,
    description: row.description
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (editForm.id) {
      await updateApprovalFlow(editForm)
      ElMessage.success('更新成功')
    } else {
      await createApprovalFlow(editForm)
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

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该审批流程吗？', '提示', {
      type: 'warning'
    })
    await deleteApprovalFlow(row.id)
    ElMessage.success('删除成功')
    fetchData()
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
