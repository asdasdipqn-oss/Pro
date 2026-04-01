<template>
  <div class="leave-type-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>假期类型管理</span>
          <el-button type="primary" @click="handleAdd">新增假期类型</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="typeCode" label="类型编码" width="120" />
        <el-table-column prop="typeName" label="类型名称" width="130" />
        <el-table-column label="是否带薪" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isPaid === 1 ? 'success' : 'info'" size="small">
              {{ row.isPaid === 1 ? '带薪' : '无薪' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="需要证明" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.needProof === 1 ? 'warning' : 'info'" size="small">
              {{ row.needProof === 1 ? '需要' : '不需要' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxDays" label="最大天数" width="100" align="center">
          <template #default="{ row }">
            {{ row.maxDays || '不限' }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
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
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑假期类型' : '新增假期类型'" width="550px">
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="类型编码" prop="typeCode">
          <el-input v-model="editForm.typeCode" placeholder="如 ANNUAL_LEAVE" />
        </el-form-item>
        <el-form-item label="类型名称" prop="typeName">
          <el-input v-model="editForm.typeName" placeholder="如 年假" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否带薪">
              <el-switch v-model="editForm.isPaid" :active-value="1" :inactive-value="0"
                active-text="带薪" inactive-text="无薪" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需要证明">
              <el-switch v-model="editForm.needProof" :active-value="1" :inactive-value="0"
                active-text="需要" inactive-text="不需要" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大天数">
              <el-input-number v-model="editForm.maxDays" :min="0" placeholder="0为不限" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="editForm.sort" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0"
            active-text="启用" inactive-text="禁用" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="editForm.description" type="textarea" rows="2" placeholder="假期类型说明" />
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
import { listAllLeaveType, addLeaveType, updateLeaveType, deleteLeaveType } from '@/api/leave'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const tableData = ref([])

const defaultForm = () => ({
  id: null, typeCode: '', typeName: '', isPaid: 1, needProof: 0,
  maxDays: null, description: '', status: 1, sort: 0
})

const editForm = reactive(defaultForm())

const formRules = {
  typeCode: [{ required: true, message: '请输入类型编码', trigger: 'blur' }],
  typeName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAllLeaveType()
    tableData.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
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
      await updateLeaveType(editForm)
      ElMessage.success('修改成功')
    } else {
      await addLeaveType(editForm)
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
    await ElMessageBox.confirm(`确定删除假期类型"${row.typeName}"吗？`, '提示', { type: 'warning' })
    await deleteLeaveType(row.id)
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
</style>
