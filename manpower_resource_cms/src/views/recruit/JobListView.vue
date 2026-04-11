<template>
  <div class="job-list-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>招聘岗位管理</span>
          <el-button type="primary" @click="handleAdd">发布岗位</el-button>
        </div>
      </template>
      
      <el-form :inline="true" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable @change="fetchData" @clear="handleStatusClear">
            <el-option label="全部" :value="null" />
            <el-option label="招聘中" :value="1" />
            <el-option label="已关闭" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="jobName" label="岗位名称" width="180" />
        <el-table-column prop="deptName" label="所属部门" width="150" />
        <el-table-column prop="headcount" label="招聘人数" width="100" align="center" />
        <el-table-column label="薪资范围" width="150">
          <template #default="{ row }">
            {{ row.salaryMin ? `${row.salaryMin}-${row.salaryMax}` : '面议' }}
          </template>
        </el-table-column>
                <el-table-column prop="requirements" label="任职要求" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '招聘中' : '已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" type="warning" link @click="handleClose(row)">关闭</el-button>
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
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑岗位' : '发布岗位'" width="650px">
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位名称" prop="jobName">
              <el-input v-model="editForm.jobName" placeholder="请输入岗位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="招聘人数" prop="headcount">
              <el-input-number v-model="editForm.headcount" :min="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最低薪资">
              <el-input-number v-model="editForm.salaryMin" :min="0" placeholder="元/月" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高薪资">
              <el-input-number v-model="editForm.salaryMax" :min="0" placeholder="元/月" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="岗位描述">
          <el-input v-model="editForm.jobDescription" type="textarea" rows="3" placeholder="请输入岗位描述" />
        </el-form-item>
        <el-form-item label="任职要求">
          <el-input v-model="editForm.requirements" type="textarea" rows="3" placeholder="请输入任职要求" />
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
import { pageJobs, publishJob, updateJob, closeJob } from '@/api/recruit'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const formRef = ref(null)

const searchForm = reactive({ status: null })

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const editForm = reactive({
  id: null,
  jobName: '',
  headcount: 1,
  salaryMin: null,
  salaryMax: null,
  jobDescription: '',
  requirements: ''
})

const formRules = {
  jobName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  headcount: [{ required: true, message: '请输入招聘人数', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    console.log('[JobListView] 查询参数:', { ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    const res = await pageJobs({
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    console.log('[JobListView] 查询结果:', res.data)
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
    id: null, jobName: '', headcount: 1, salaryMin: null, salaryMax: null,
    jobDescription: '', requirements: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(editForm, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    if (editForm.id) {
      await updateJob(editForm)
      ElMessage.success('更新成功')
    } else {
      await publishJob(editForm)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleClose = async (row) => {
  try {
    await ElMessageBox.confirm('确定要关闭该招聘岗位吗？', '提示', { type: 'warning' })
    console.log('[JobListView] 关闭岗位:', row)
    await closeJob(row.id)
    console.log('[JobListView] 关闭成功，刷新列表')
    ElMessage.success('已关闭')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleStatusClear = () => {
  console.log('[JobListView] 清空状态选择')
  searchForm.status = null
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
