<template>
  <div class="resume-list-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>简历管理</span>
        </div>
      </template>
      
      <el-form :inline="true" class="search-form">
        <el-form-item label="应聘岗位">
          <el-select v-model="searchForm.jobId" placeholder="全部" clearable @change="fetchData">
            <el-option v-for="job in jobList" :key="job.id" :label="job.jobName" :value="job.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable @change="fetchData">
            <el-option label="待筛选" :value="1" />
            <el-option label="筛选通过" :value="2" />
            <el-option label="面试中" :value="3" />
            <el-option label="已录用" :value="4" />
            <el-option label="未通过" :value="5" />
            <el-option label="已放弃" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column label="学历" width="80">
          <template #default="{ row }">
            {{ educationMap[row.education] || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="workYears" label="工作年限" width="100">
          <template #default="{ row }">
            {{ row.workYears ? row.workYears + '年' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="expectedSalary" label="期望薪资" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="投递时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 1" type="success" link @click="handleScreen(row, 2)">通过</el-button>
            <el-button v-if="row.status === 1" type="danger" link @click="handleScreen(row, 5)">不通过</el-button>
            <el-button v-if="row.status === 2" type="warning" link @click="handleUpdateStatus(row, 4)">录用</el-button>
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
    
    <!-- 查看简历对话框 -->
    <el-dialog v-model="viewDialogVisible" title="简历详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ currentResume.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ currentResume.gender === 1 ? '男' : '女' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentResume.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentResume.email }}</el-descriptions-item>
        <el-descriptions-item label="学历">{{ educationMap[currentResume.education] }}</el-descriptions-item>
        <el-descriptions-item label="工作年限">{{ currentResume.workYears }}年</el-descriptions-item>
        <el-descriptions-item label="毕业院校">{{ currentResume.graduateSchool }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ currentResume.major }}</el-descriptions-item>
        <el-descriptions-item label="当前公司">{{ currentResume.currentCompany }}</el-descriptions-item>
        <el-descriptions-item label="当前职位">{{ currentResume.currentPosition }}</el-descriptions-item>
        <el-descriptions-item label="期望薪资">{{ currentResume.expectedSalary }}</el-descriptions-item>
        <el-descriptions-item label="简历来源">{{ currentResume.source }}</el-descriptions-item>
        <el-descriptions-item label="自我介绍" :span="2">{{ currentResume.selfIntro || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageResumes, screenResume, updateResumeStatus, getResumeDetail } from '@/api/recruit'
import { pageJobs } from '@/api/recruit'

const loading = ref(false)
const viewDialogVisible = ref(false)
const tableData = ref([])
const jobList = ref([])
const currentResume = ref({})

const searchForm = reactive({ jobId: null, status: null })

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const statusMap = {
  1: '待筛选',
  2: '筛选通过',
  3: '面试中',
  4: '已录用',
  5: '未通过',
  6: '已放弃'
}

const statusTypeMap = {
  1: 'info',
  2: 'primary',
  3: 'warning',
  4: 'success',
  5: 'danger',
  6: ''
}

const educationMap = {
  1: '不限',
  2: '大专',
  3: '本科',
  4: '硕士',
  5: '博士'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageResumes({
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

const fetchJobs = async () => {
  try {
    const res = await pageJobs({ pageNum: 1, pageSize: 100 })
    jobList.value = res.data?.records || []
  } catch (error) {
    console.error(error)
  }
}

const handleView = async (row) => {
  try {
    const res = await getResumeDetail(row.id)
    currentResume.value = res.data || row
    viewDialogVisible.value = true
  } catch (error) {
    currentResume.value = row
    viewDialogVisible.value = true
  }
}

const handleScreen = async (row, status) => {
  const action = status === 2 ? '通过' : '不通过'
  try {
    await ElMessageBox.confirm(`确定将该简历标记为"${action}"吗？`, '提示', { type: 'warning' })
    await screenResume(row.id, status)
    ElMessage.success('操作成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleUpdateStatus = async (row, status) => {
  const statusText = statusMap[status]
  try {
    await ElMessageBox.confirm(`确定将该简历状态更新为"${statusText}"吗？`, '提示', { type: 'warning' })
    await updateResumeStatus(row.id, status)
    ElMessage.success('操作成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

onMounted(() => {
  fetchData()
  fetchJobs()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
