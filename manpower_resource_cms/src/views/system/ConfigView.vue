<template>
  <div class="config-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统配置管理</span>
          <el-button type="primary" @click="handleAdd">新增配置</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="配置名称">
          <el-input v-model="searchForm.configName" placeholder="请输入配置名称" clearable />
        </el-form-item>
        <el-form-item label="配置类型">
          <el-select v-model="searchForm.configType" placeholder="全部" clearable>
            <el-option label="系统配置" :value="1" />
            <el-option label="业务配置" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="configKey" label="配置键" width="200" show-overflow-tooltip />
        <el-table-column prop="configName" label="配置名称" width="180" />
        <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.configType === 1 ? 'danger' : 'primary'" size="small">
              {{ row.configType === 1 ? '系统' : '业务' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
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

    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑配置' : '新增配置'" width="550px">
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="editForm.configKey" placeholder="如 sys.upload.maxSize" :disabled="!!editForm.id" />
        </el-form-item>
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="editForm.configName" placeholder="如 上传文件大小限制" />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input v-model="editForm.configValue" placeholder="配置值" />
        </el-form-item>
        <el-form-item label="配置类型" prop="configType">
          <el-select v-model="editForm.configType" style="width: 100%">
            <el-option label="系统配置" :value="1" />
            <el-option label="业务配置" :value="2" />
          </el-select>
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
import { pageConfig, addConfig, updateConfig, deleteConfig } from '@/api/config'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const tableData = ref([])

const searchForm = reactive({ configName: '', configType: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const defaultForm = () => ({ id: null, configKey: '', configName: '', configValue: '', configType: 2, remark: '' })
const editForm = reactive(defaultForm())

const formRules = {
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }],
  configType: [{ required: true, message: '请选择类型', trigger: 'change' }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageConfig({ ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleReset = () => { searchForm.configName = ''; searchForm.configType = null; pagination.pageNum = 1; fetchData() }
const handleAdd = () => { Object.assign(editForm, defaultForm()); dialogVisible.value = true }
const handleEdit = (row) => { Object.assign(editForm, { ...row }); dialogVisible.value = true }

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editForm.id) { await updateConfig(editForm); ElMessage.success('修改成功') }
    else { await addConfig(editForm); ElMessage.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } catch (e) { console.error(e) } finally { submitting.value = false }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除配置"${row.configName}"吗？`, '提示', { type: 'warning' })
    await deleteConfig(row.id); ElMessage.success('删除成功'); fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
