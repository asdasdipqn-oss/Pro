<template>
  <div class="job-list-container">
    <div class="page-header">
      <h1>招聘岗位</h1>
      <p>发现理想职位，开启职业新篇章</p>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索岗位名称..."
        size="large"
        clearable
      style="width: 400px"
      @input="handleSearch"
      >
    </div>

    <div class="job-list">
      <div v-loading="loading" v-if="loading" class="loading-wrapper">
        <el-icon class="is-loading"><loading-icon /></el-icon>
        <span>加载中...</span>
      </div>

      <el-empty v-if="jobs.length === 0 && !loading" description="暂无岗位信息">
        <template #empty>
          <el-icon :size="80" color="#909399"><PictureFilled /></el-icon>
          <p>暂无岗位信息</p>
        </template>
      </el-empty>

      <el-card v-for="job in jobs" :key="job.id" class="job-card" @click="showJobDetail(job)">
        <div class="job-header">
          <span class="job-name">{{ job.jobName }}</span>
          <el-tag type="primary" size="small">{{ job.educationReqText }}</el-tag>
          <span class="salary-range" v-if="job.salaryMin && job.salaryMax">
            {{ job.salaryMin }}-{{ job.salaryMax }}元
          </span>
          <span class="salary-single" v-else-if="job.salaryMin && job.salaryMax && job.salaryMin === job.salaryMax">
            {{ job.salaryMin }}元
          </span>
          <span class="salary-range" v-else-if="job.salaryMin && job.salaryMax">
            {{ job.salaryMin }}-{{ job.salaryMax }}元
          </span>
          <el-tag type="warning" size="small">{{ job.experienceReqText }}</el-tag>
        </div>
        <div class="job-body">
          <p class="job-desc">{{ job.jobDescription }}</p>
          <div class="job-requirements">
            <strong>任职要求：</strong>
            <p>{{ job.requirements }}</p>
          </div>
          <div class="job-info">
            <span><el-icon><Location /></el-icon> {{ job.location }}</span>
            <span v-if="job.publisherName"><el-icon><User /></el-icon> {{ job.publisherName }}</span>
            <span>{{ job.publishTimeText }}</span>
          </div>
        </div>
        <div class="job-actions">
          <el-button type="primary" @click="showJobDetail(job)">查看详情</el-button>
          <el-button type="success" @click="applyJob(job)">投递简历</el-button>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="detailDialogVisible" title="岗位详情" width="600px" @close="detailDialogVisible = false">
      <div v-if="currentJob">
        <div class="job-detail-header">
          <h2>{{ currentJob.jobName }}</h2>
          <el-tag>{{ currentJob.educationReqText }}</el-tag>
          <el-tag type="warning">{{ currentJob.experienceReqText }}</el-tag>
        </div>
        <div class="job-detail-info">
          <div class="info-row">
            <span class="label"><el-icon><Location /></el-icon> 工作地点：</span>
            <span>{{ currentJob.location }}</span>
          </div>
          <div class="info-row">
            <span class="label"><el-icon><Money /></el-icon> 薪资范围：</span>
            <span>{{ currentJob.salaryRange }}</span>
          </div>
          <div class="info-row">
            <span class="label"><el-icon><User /></el-icon> 招聘人数：</span>
            <span>{{ currentJob.headcount }}人</span>
          </div>
          <div class="info-row">
            <span class="label"><el-icon><Calendar /></el-icon> 截止日期：</span>
            <span>{{ currentJob.deadlineText }}</span>
          </div>
        </div>
        <div class="job-desc">
          <p><strong>岗位描述：</strong></p>
          <p>{{ currentJob.jobDescription }}</p>
          <div class="job-requirements">
            <p><strong>任职要求：</strong></p>
            <p>{{ currentJob.requirements }}</p>
        </div>
        <div class="job-actions">
          <el-button type="primary" @click="applyJob(currentJob)">投递简历</el-button>
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </div>

      <el-dialog v-model="applyDialogVisible" title="投递简历" width="500px" @close="applyDialogVisible = false">
        <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px">
          <el-form-item label="选择简历">
            <el-select v-model="applyForm.resumeId" placeholder="请选择简历">
              <el-option v-for="resume in myResumes" :key="resume.id" :label="resume.file_name || '简历'">
                {{ resume.file_name || '我的简历 ' }}
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="是否附带求职信">
            <el-radio-group v-model="applyForm.coverLetter">
              <el-radio :label="0">否</el-radio>
              <el-radio :label="1">是</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="个人说明">
            <el-input
              v-model="applyForm.selfIntro"
              type="textarea"
              :rows="4"
              placeholder="请简要介绍您的优势（200字以内）"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" @click="handleApply">
              确认投递
            </el-button>
            <el-button @click="applyDialogVisible = false">取消
          </el-form-item>
        </el-form>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const jobs = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const detailDialogVisible = ref(false)
const applyDialogVisible = ref(false)
const currentJob = ref(null)
const myResumes = ref([])
const applyFormRef = ref()

const applyForm = reactive({
  resumeId: null,
  coverLetter: 0,
  selfIntro: ''
})

const applyRules = {
  resumeId: [{ required: true, message: '请选择简历', trigger: 'change' }],
  selfIntro: [
    { max: 200, message: '个人说明不能超过200个字符', trigger: 'blur' }
  ]
}

const loadJobs = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/recruit/jobs', {
      params: { keyword: searchKeyword.value, page: 1, pageSize: 10 }
    })
    jobs.value = response.data.data.map(job => ({
      ...job,
      educationReqText: formatEducation(job.educationReq),
      experienceReqText: formatExperience(job.experienceReq),
      salaryRange: formatSalary(job),
      publishTimeText: formatDate(job.publishTime),
      deadlineText: formatDate(job.deadline)
    }))
  } catch (error) {
    console.error('加载岗位列表失败:', error)
    ElMessage.error('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

const formatEducation = (req) => {
  const map = { 1: '不限', 2: '大专', 3: '本科', 4: '硕士', 5: '博士' }
  return map[req] || '不限'
}

const formatExperience = (req) => {
  const map = { 0: '不限', 1: '1年', 2: '2年', 3: '3-5年', 5: '5年以上' }
  return map[req] || '不限'
}

const formatSalary = (job) => {
  if (job.salaryMin && job.salaryMax) {
    return `${job.salaryMin}-${job.salaryMax}元`
  } else if (job.salaryMin && !job.salaryMax) {
    return `${job.salaryMin}元`
  } else {
    return '面议'
  }
}

const formatDate = (date) => {
  if (!date) return '暂无'
  return new Date(date).toLocaleDateString('zh-CN')
}

const showJobDetail = (job) => {
  currentJob.value = job
  detailDialogVisible.value = true
}

const applyJob = (job) => {
  applyDialogVisible.value = true
}

const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    loadJobs()
  }
}

const handleApply = async () => {
  const valid = await applyFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await axios.post('/recruit/applications', {
      jobId: currentJob.value.id,
      jobSeekerId: localStorage.getItem('jobSeekerId'),
      ...applyForm
    })
    ElMessage.success('投递成功')
    applyDialogVisible.value = false
    detailDialogVisible.value = false
  } catch (error) {
    console.error('投递失败:', error)
    ElMessage.error(error.response?.data?.message || '投递失败，请重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadJobs()
  loadMyResumes()
})
</script>

<style scoped>
.job-list-container {
  min-height: 100vh;
  background: #F5F5F7;
  padding: 40px 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 600;
  color: #1D1D1F;
}

.page-header p {
  color: #86868B;
  font-size: 16px;
}

.search-bar {
  margin-bottom: 32px;
  display: flex;
  gap: 16px;
}

.job-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.job-card {
  background: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  cursor: pointer;
  border: 1px solid transparent;
}

.job-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.job-name {
  font-size: 20px;
  font-weight: 600;
  color: #1D1D1F;
  flex: 1;
  align-items: center;
}

.job-name .job-tags {
  margin-left: 8px;
}

.job-desc {
  color: #86868B;
  font-size: 15px;
  margin-bottom: 16px;
  line-height: 1.5;
}

.job-actions {
  margin-top: 16px;
}

.job-actions .el-button {
  margin-right: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  color: #86868B;
  font-size: 14px;
  margin-bottom: 12px;
}

.info-row .label {
  color: #1D1D1F;
  font-weight: 500;
  width: 70px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-row span {
  color: #86868B;
}

.job-detail-header {
  margin-bottom: 24px;
  border-bottom: 1px solid #F0F0F0;
  padding-bottom: 16px;
}

.job-detail-info {
  margin-bottom: 24px;
}

.job-detail-info .info-row {
  margin-bottom: 12px;
}

.job-desc {
  line-height: 1.6;
  color: #4A4A4A4;
}

.job-desc strong {
  color: #1D1D1F;
}

.job-requirements strong {
  color: #1D1D1F;
}

.job-actions {
  justify-content: flex-end;
  gap: 12px;
}

.is-loading {
  animation: rotate 1s linear infinite;
}
</style>
