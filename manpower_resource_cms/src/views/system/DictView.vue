<template>
  <div class="dict-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>字典管理</span>
          <el-button type="primary" @click="handleAdd">新增字典</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="dictType" label="字典类型" />
        <el-table-column prop="dictName" label="字典名称" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewData(row)">查看数据</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑字典类型对话框 -->
    <el-dialog
      v-model="typeDialogVisible"
      :title="editForm.id ? '编辑字典' : '新增字典'"
      width="500px"
    >
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="字典类型" prop="dictType">
          <el-input
            v-model="editForm.dictType"
            placeholder="请输入字典类型"
            :disabled="!!editForm.id"
          />
        </el-form-item>
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="editForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据弹窗 -->
    <el-dialog
      v-model="dataDialogVisible"
      :title="`字典数据 - ${currentDict.dictName}`"
      width="700px"
    >
      <div style="margin-bottom: 16px; text-align: right">
        <el-button type="primary" size="small" @click="handleAddData">新增数据</el-button>
      </div>
      <el-table :data="dictDataList" stripe>
        <el-table-column prop="dictLabel" label="字典标签" />
        <el-table-column prop="dictValue" label="字典值" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditData(row)"
              >编辑</el-button
            >
            <el-button type="danger" link size="small" @click="handleDeleteData(row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 新增/编辑字典数据对话框 -->
    <el-dialog
      v-model="dataFormDialogVisible"
      :title="dataForm.id ? '编辑字典数据' : '新增字典数据'"
      width="450px"
    >
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataFormRules" label-width="100px">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dataForm.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataFormDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitData" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listDictType,
  addDictType,
  updateDictType,
  deleteDictType,
  listDictData,
  addDictData,
  updateDictData,
  deleteDictData,
} from '@/api/dict'

const loading = ref(false)
const submitting = ref(false)
const typeDialogVisible = ref(false)
const dataDialogVisible = ref(false)
const dataFormDialogVisible = ref(false)
const tableData = ref([])
const dictDataList = ref([])
const formRef = ref(null)
const dataFormRef = ref(null)

const currentDict = reactive({ dictName: '', dictType: '' })

const editForm = reactive({
  id: null,
  dictType: '',
  dictName: '',
  status: 1,
  remark: '',
})

const dataForm = reactive({
  id: null,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  sort: 0,
  status: 1,
})

const formRules = {
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
}

const dataFormRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listDictType()
    tableData.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(editForm, { id: null, dictType: '', dictName: '', status: 1, remark: '' })
}

const handleAdd = () => {
  resetForm()
  typeDialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(editForm, {
    id: row.id,
    dictType: row.dictType,
    dictName: row.dictName,
    status: row.status,
    remark: row.remark,
  })
  typeDialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    if (editForm.id) {
      await updateDictType(editForm)
      ElMessage.success('更新成功')
    } else {
      await addDictType(editForm)
      ElMessage.success('新增成功')
    }
    typeDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleViewData = async (row) => {
  currentDict.dictName = row.dictName
  currentDict.dictType = row.dictType
  try {
    const res = await listDictData(row.dictType)
    dictDataList.value = res.data || []
    dataDialogVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleAddData = () => {
  Object.assign(dataForm, {
    id: null,
    dictType: currentDict.dictType,
    dictLabel: '',
    dictValue: '',
    sort: 0,
    status: 1,
  })
  dataFormDialogVisible.value = true
}

const handleEditData = (row) => {
  Object.assign(dataForm, {
    id: row.id,
    dictType: currentDict.dictType,
    dictLabel: row.dictLabel,
    dictValue: row.dictValue,
    sort: row.sort,
    status: row.status,
  })
  dataFormDialogVisible.value = true
}

const handleSubmitData = async () => {
  try {
    await dataFormRef.value.validate()
    submitting.value = true
    if (dataForm.id) {
      await updateDictData(dataForm)
      ElMessage.success('更新成功')
    } else {
      await addDictData(dataForm)
      ElMessage.success('新增成功')
    }
    dataFormDialogVisible.value = false
    const res = await listDictData(currentDict.dictType)
    dictDataList.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleDeleteData = async (row) => {
  await ElMessageBox.confirm('确定要删除该字典数据吗？', '提示', { type: 'warning' })
  try {
    await deleteDictData(row.id)
    ElMessage.success('删除成功')
    const res = await listDictData(currentDict.dictType)
    dictDataList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该字典吗？', '提示', { type: 'warning' })
  try {
    await deleteDictType(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    console.error(error)
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
</style>
