<template>
  <div class="candidate-jobs">
    <div class="header">
      <h1>岗位招聘</h1>
      <p>查看并申请心仪的岗位</p>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索岗位名称"
        clearable
        style="width: 300px"
        @clear="fetchJobs"
        @keyup.enter="fetchJobs"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="fetchJobs">搜索</el-button>
    </div>

    <div class="job-list" v-loading="loading">
      <el-empty v-if="!jobList.length && !loading" description="暂无招聘信息" />

      <div v-for="job in jobList" :key="job.id" class="job-card">
        <div class="job-header">
          <h3 class="job-title">{{ job.jobName }}</h3>
          <el-tag type="success" size="small">招聘中</el-tag>
        </div>

        <div class="job-info">
          <div class="info-item">
            <el-icon><OfficeBuilding /></el-icon>
            <span>{{ job.deptName || '公司' }}</span>
          </div>
          <div class="info-item">
            <el-icon><User /></el-icon>
            <span>招聘 {{ job.headcount }} 人</span>
          </div>
          <div class="info-item">
            <el-icon><Money /></el-icon>
            <span>{{ job.salaryMin && job.salaryMax ? `${job.salaryMin}-${job.salaryMax}元/月` : '薪资面议' }}</span>
          </div>
        </div>

        <div class="job-description">
          <p><strong>岗位职责：</strong>{{ job.jobDescription || '无' }}</p>
          <p><strong>任职要求：</strong>{{ job.requirements || '无' }}</p>
        </div>

        <div class="job-footer">
          <span class="publish-time">发布于 {{ formatDate(job.createTime) }}</span>
          <el-button type="primary" @click="handleApply(job)">投递简历</el-button>
        </div>
      </div>
    </div>

    <!-- 投递简历对话框 -->
    <el-dialog v-model="applyDialogVisible" title="投递简历" width="500px">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px">
        <el-form-item label="岗位名称">
          <el-input v-model="applyForm.jobName" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="name">
          <el-input v-model="applyForm.name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="applyForm.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="applyForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="学历" prop="education">
          <el-select v-model="applyForm.education" placeholder="请选择学历" style="width: 100%">
            <el-option label="高中及以下" :value="1" />
            <el-option label="大专" :value="2" />
            <el-option label="本科" :value="3" />
            <el-option label="硕士" :value="4" />
            <el-option label="博士" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作年限">
          <el-input-number v-model="applyForm.workYears" :min="0" :max="50" placeholder="工作年限" style="width: 100%" />
        </el-form-item>
        <el-form-item label="自我介绍">
          <el-input v-model="applyForm.selfIntro" type="textarea" :rows="3" placeholder="请简单介绍一下自己" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitApply" :loading="submitting">确定投递</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, OfficeBuilding, User, Money } from '@element-plus/icons-vue'
import { listPublishedJobs, submitResume } from '@/api/recruit'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const searchKeyword = ref('')
const jobList = ref([])
const applyDialogVisible = ref(false)
const applyFormRef = ref(null)

const applyForm = reactive({
  jobId: null,
  jobName: '',
  name: '',
  phone: '',
  email: '',
  education: null,
  workYears: 0,
  selfIntro: ''
})

const applyRules = {
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const fetchJobs = async () => {
  loading.value = true
  try {
    const res = await listPublishedJobs()
    jobList.value = res.data || []
    // 按发布时间倒序排列
    jobList.value.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))

    // 搜索过滤
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      jobList.value = jobList.value.filter(job =>
        job.jobName.toLowerCase().includes(keyword) ||
        job.deptName?.toLowerCase().includes(keyword)
      )
    }
  } catch (error) {
    console.error('获取岗位列表失败:', error)
    ElMessage.error('获取岗位列表失败')
  } finally {
    loading.value = false
  }
}

const handleApply = async (job) => {
  // 先加载候选人个人信息
  try {
    const res = await request.get('/candidate/profile')
    const profile = res.data || {}
    applyForm.jobId = job.id
    applyForm.jobName = job.jobName
    applyForm.name = profile.realName || ''
    applyForm.phone = profile.phone || ''
    applyForm.email = profile.email || ''
    applyForm.education = profile.education || null
    applyForm.workYears = profile.workExperience || 0
    applyForm.selfIntro = ''
    applyDialogVisible.value = true
  } catch (error) {
    // 如果没有个人信息，显示空的表单
    applyForm.jobId = job.id
    applyForm.jobName = job.jobName
    applyForm.name = ''
    applyForm.phone = ''
    applyForm.email = ''
    applyForm.education = null
    applyForm.workYears = 0
    applyForm.selfIntro = ''
    applyDialogVisible.value = true
  }
}

const handleSubmitApply = async () => {
  try {
    await applyFormRef.value.validate()
    submitting.value = true
    await submitResume(applyForm)
    ElMessage.success('简历投递成功，请耐心等待HR审核')
    applyDialogVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      console.error('投递简历失败:', error)
      ElMessage.error(error.response?.data?.message || '投递失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

onMounted(fetchJobs)
</script>

<style scoped>
.candidate-jobs {
  padding-top: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header {
  text-align: center;
  margin-bottom: 32px;
}

.header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 8px;
}

.header p {
  font-size: 16px;
  color: #86868B;
}

.search-bar {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 32px;
}

.job-list {
  max-width: 900px;
  margin: 0 auto;
}

.job-card {
  background: #FFFFFF;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.2s;
}

.job-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.job-title {
  font-size: 20px;
  font-weight: 600;
  color: #1D1D1F;
  margin: 0;
}

.job-info {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #86868B;
}

.info-item .el-icon {
  font-size: 18px;
}

.job-description {
  background: #F5F5F7;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
}

.job-description p {
  font-size: 14px;
  color: #1D1D1F;
  line-height: 1.6;
  margin: 8px 0;
}

.job-description strong {
  color: #007AFF;
}

.job-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #E5E5EA;
}

.publish-time {
  font-size: 13px;
  color: #86868B;
}
</style>
