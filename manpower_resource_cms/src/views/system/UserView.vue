<template>
  <div class="user-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入用户名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryForm.roleId" placeholder="全部" clearable style="width: 150px">
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <div class="spacer"></div>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 150px">
            <el-option label="正常" :value="1" />
            <el-option label="冻结" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="employeeName" label="关联员工" width="150" />
        <el-table-column prop="roleNames" label="角色" width="150">
          <template #default="{ row }">
            {{ row.roleNames ? row.roleNames.join(', ') : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleResetPwd(row)">重置密码</el-button>
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '冻结' : '解冻' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑用户' : '新增用户'" width="500px">
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="editForm.username"
            placeholder="请输入用户名"
            :disabled="!!editForm.id"
          />
        </el-form-item>
        <el-form-item v-if="!editForm.id" label="密码" prop="password">
          <el-input
            v-model="editForm.password"
            type="password"
            show-password
            placeholder="请输入密码"
          />
        </el-form-item>
        <el-form-item label="关联员工" prop="employeeId">
          <el-select
            v-model="editForm.employeeId"
            placeholder="请选择员工"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="emp in employeeList"
              :key="emp.id"
              :label="`${emp.empName} (${emp.empCode})`"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select
            v-model="editForm.roleIds"
            multiple
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">冻结</el-radio>
          </el-radio-group>
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
  pageUser,
  addUser,
  updateUser,
  deleteUser,
  getUserRoles,
  getUserDetail,
  resetPassword,
  changeUserStatus,
} from '@/api/user-manage'
import { listRole } from '@/api/role'
import { pageEmployee } from '@/api/employee'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const roleList = ref([])
const employeeList = ref([])
const formRef = ref(null)

const queryForm = reactive({
  username: '',
  roleId: null,
  status: null,
  pageNum: 1,
  pageSize: 10,
})

const editForm = reactive({
  id: null,
  username: '',
  password: '',
  employeeId: null,
  roleIds: [],
  status: 1,
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  employeeId: [{ required: true, message: '请选择关联员工', trigger: 'change', type: 'number' }],
  roleIds: [{ required: true, message: '请选择角色', trigger: 'change', type: 'array' }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageUser(queryForm)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchUsers = async () => {
  try {
    const res = await pageEmployee({ pageNum: 1, pageSize: 1000 })
    employeeList.value = res.data?.records || []
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  fetchData()
}

const handleReset = () => {
  queryForm.username = ''
  queryForm.roleId = null
  queryForm.status = null
  queryForm.pageNum = 1
  fetchData()
}

const fetchRoles = async () => {
  try {
    const res = await listRole()
    roleList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const fetchEmployees = async () => {
  try {
    const res = await pageEmployee({ pageNum: 1, pageSize: 1000 })
    employeeList.value = res.data?.records || []
  } catch (error) {
    console.error(error)
  }
}

const resetForm = () => {
  Object.assign(editForm, {
    id: null,
    username: '',
    password: '',
    employeeId: null,
    roleIds: [],
    status: 1,
  })
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  Object.assign(editForm, {
    id: row.id,
    username: row.username,
    employeeId: row.employeeId,
    status: row.status,
  })
  // 获取用户角色ID列表
  try {
    const roles = await getUserRoles(row.id)
    editForm.roleIds = roles.map(r => r.id) || []
  } catch (error) {
    console.error(error)
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    if (editForm.id) {
      await updateUser(editForm)
      ElMessage.success('更新成功')
    } else {
      await addUser(editForm)
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

const handleResetPwd = async (row) => {
  await ElMessageBox.confirm(`确定要重置用户 ${row.username} 的密码吗？`, '提示', {
    type: 'warning',
  })
  try {
    await resetPassword(row.id)
    ElMessage.success('密码已重置为 123456')
  } catch (error) {
    console.error(error)
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '冻结' : '解冻'
  await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', { type: 'warning' })
  try {
    await changeUserStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchData()
  fetchRoles()
  fetchEmployees()
})
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

.search-form .spacer {
  width: 30px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
