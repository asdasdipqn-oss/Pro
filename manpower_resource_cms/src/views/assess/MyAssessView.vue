<template>
  <div class="my-assess-view">
    <!-- 考核任务列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的考核</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="考核任务" name="tasks">
          <el-table :data="taskList" v-loading="taskLoading" stripe>
            <el-table-column prop="title" label="任务标题" min-width="200" show-overflow-tooltip />
            <el-table-column
              prop="description"
              label="任务描述"
              min-width="250"
              show-overflow-tooltip
            />
            <el-table-column prop="deadline" label="截止时间" width="170">
              <template #default="{ row }">
                <span :class="{ 'text-danger': isOverdue(row.deadline) }">{{
                  row.deadline || '无限制'
                }}</span>
              </template>
            </el-table-column>
            <el-table-column label="任务状态" width="100">
              <template #default="{ row }">
                <el-tag :type="taskStatusTypeMap[row.status]">{{
                  taskStatusMap[row.status]
                }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="提交状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row._submission" :type="subStatusTypeMap[row._submission.status]">
                  {{ subStatusMap[row._submission.status] }}
                </el-tag>
                <el-tag v-else type="info">未提交</el-tag>
                <div v-if="row._submission?.revisionNo" class="revision-tip">
                  第 {{ row._submission.revisionNo }} 版
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 1 && (!row._submission || row._submission.status === 2)"
                  type="primary"
                  link
                  @click="openUploadDialog(row)"
                >
                  {{ row._submission ? '重新提交' : '提交作业' }}
                </el-button>
                <el-button v-if="row._submission" type="info" link @click="viewDetail(row)"
                  >查看详情</el-button
                >
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="taskPagination.pageNum"
              v-model:page-size="taskPagination.pageSize"
              :total="taskPagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="fetchTasks"
              @current-change="fetchTasks"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的成绩" name="scores">
          <el-table :data="mySubmissions" v-loading="scoreLoading" stripe>
            <el-table-column
              prop="taskTitle"
              label="考核任务"
              min-width="200"
              show-overflow-tooltip
            />
            <el-table-column
              prop="fileName"
              label="提交文件"
              min-width="180"
              show-overflow-tooltip
            />
            <el-table-column prop="submitTime" label="提交时间" width="170" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="subStatusTypeMap[row.status]">{{ subStatusMap[row.status] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="score" label="得分" width="80" align="center">
              <template #default="{ row }">
                <span :class="scoreClass(row.score)">{{ row.score ?? '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="revisionNo" label="版本" width="70" align="center" />
            <el-table-column prop="grade" label="等级" width="70" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.grade" :type="gradeTypeMap[row.grade]" size="small">{{
                  row.grade
                }}</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="scorerComment"
              label="评语"
              min-width="200"
              show-overflow-tooltip
            />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="提交作业" width="500px">
      <div class="upload-info">
        <p><strong>任务：</strong>{{ currentTask.title }}</p>
        <p><strong>要求：</strong>{{ currentTask.description }}</p>
        <p v-if="currentTask.deadline">
          <strong>截止时间：</strong>
          <span :class="{ 'text-danger': isOverdue(currentTask.deadline) }">{{
            currentTask.deadline
          }}</span>
        </p>
        <p><strong>允许文件类型：</strong>{{ currentTask.allowFileTypes || '不限' }}</p>
        <p><strong>最大文件大小：</strong>{{ currentTask.maxFileSize || 10 }} MB</p>
      </div>

      <el-divider />

      <el-form label-width="80px">
        <el-form-item label="上传文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-exceed="handleExceed"
            drag
          >
            <el-icon class="el-icon--upload"><i class="el-icon-upload" /></el-icon>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">
                支持 {{ currentTask.allowFileTypes || '所有类型' }}，最大
                {{ currentTask.maxFileSize || 10 }} MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="uploadRemark"
            type="textarea"
            rows="2"
            placeholder="可选，填写备注说明"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleUpload"
          :loading="uploading"
          :disabled="!selectedFile"
        >
          提交
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="提交详情" width="550px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="考核任务">{{ detailData.taskTitle }}</el-descriptions-item>
        <el-descriptions-item label="当前版本"
          >第 {{ detailData.revisionNo || 1 }} 版</el-descriptions-item
        >
        <el-descriptions-item label="提交文件">{{ detailData.fileName }}</el-descriptions-item>
        <el-descriptions-item label="文件大小">
          {{ detailData.fileSize ? (detailData.fileSize / 1024).toFixed(1) + ' KB' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detailData.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="subStatusTypeMap[detailData.status]">{{
            subStatusMap[detailData.status]
          }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="得分">
          <span :class="scoreClass(detailData.score)">{{ detailData.score ?? '暂未评分' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="等级">
          <el-tag v-if="detailData.grade" :type="gradeTypeMap[detailData.grade]">{{
            detailData.grade
          }}</el-tag>
          <span v-else>暂未评级</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.scorerComment" label="评语">
          {{ detailData.scorerComment }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">修订历史</el-divider>
      <el-table :data="historyList" size="small" max-height="220">
        <el-table-column prop="revisionNo" label="版本" width="70" align="center" />
        <el-table-column prop="submitTime" label="提交时间" width="170" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="subStatusTypeMap[row.status]" size="small">{{
              subStatusMap[row.status]
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="80" align="center" />
        <el-table-column prop="scorerComment" label="评语" min-width="160" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  pagePublishedTask,
  mySubmissions as fetchMySubmissions,
  myTaskSubmission,
  myTaskSubmissionHistory,
  uploadSubmission,
} from '@/api/assess'

const activeTab = ref('tasks')
const taskLoading = ref(false)
const scoreLoading = ref(false)
const uploading = ref(false)
const uploadDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const uploadRef = ref(null)

const taskList = ref([])
const mySubmissionsList = ref([])
const currentTask = ref({})
const selectedFile = ref(null)
const uploadRemark = ref('')
const detailData = ref({})
const historyList = ref([])

const taskPagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const taskStatusMap = { 0: '草稿', 1: '进行中', 2: '已截止' }
const taskStatusTypeMap = { 0: 'info', 1: 'success', 2: 'warning' }
const subStatusMap = { 0: '待评分', 1: '已评分', 2: '已退回' }
const subStatusTypeMap = { 0: 'warning', 1: 'success', 2: 'danger' }
const gradeTypeMap = { A: 'success', B: 'primary', C: 'warning', D: 'info', E: 'danger' }

const isOverdue = (deadline) => {
  if (!deadline) return false
  return new Date(deadline) < new Date()
}

const scoreClass = (score) => {
  if (score === null || score === undefined) return ''
  if (score >= 90) return 'score-excellent'
  if (score >= 70) return 'score-good'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

const fetchTasks = async () => {
  taskLoading.value = true
  try {
    const res = await pagePublishedTask({
      pageNum: taskPagination.pageNum,
      pageSize: taskPagination.pageSize,
    })
    const tasks = res.data?.records || []
    taskPagination.total = res.data?.total || 0

    // 为每个任务查询当前用户的提交状态
    const enriched = await Promise.all(
      tasks.map(async (task) => {
        try {
          const subRes = await myTaskSubmission(task.id)
          task._submission = subRes.data || null
        } catch {
          task._submission = null
        }
        return task
      }),
    )
    taskList.value = enriched
  } catch (error) {
    console.error(error)
  } finally {
    taskLoading.value = false
  }
}

const mySubmissions = ref([])

const fetchMyScores = async () => {
  scoreLoading.value = true
  try {
    const res = await fetchMySubmissions()
    mySubmissions.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    scoreLoading.value = false
  }
}

const openUploadDialog = (row) => {
  currentTask.value = row
  selectedFile.value = null
  uploadRemark.value = ''
  uploadDialogVisible.value = true
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件，请先移除已选文件')
}

const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  const maxSize = (currentTask.value.maxFileSize || 10) * 1024 * 1024
  if (selectedFile.value.size > maxSize) {
    ElMessage.warning(`文件大小不能超过 ${currentTask.value.maxFileSize || 10} MB`)
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('taskId', currentTask.value.id)
    formData.append('file', selectedFile.value)
    if (uploadRemark.value) {
      formData.append('remark', uploadRemark.value)
    }

    await uploadSubmission(formData)
    ElMessage.success('提交成功')
    uploadDialogVisible.value = false
    fetchTasks()
    fetchMyScores()
  } catch (error) {
    console.error(error)
  } finally {
    uploading.value = false
  }
}

const viewDetail = async (row) => {
  detailData.value = row._submission || {}
  detailData.value.taskTitle = row.title
  historyList.value = []

  try {
    const historyRes = await myTaskSubmissionHistory(row.id)
    historyList.value = historyRes.data || []
  } catch (error) {
    console.error(error)
  }

  detailDialogVisible.value = true
}

onMounted(() => {
  fetchTasks()
  fetchMyScores()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.upload-info p {
  margin: 8px 0;
  color: #606266;
}
.revision-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
.text-danger {
  color: #f56c6c;
}
.score-excellent {
  color: #67c23a;
  font-weight: bold;
}
.score-good {
  color: #409eff;
  font-weight: bold;
}
.score-pass {
  color: #e6a23c;
  font-weight: bold;
}
.score-fail {
  color: #f56c6c;
  font-weight: bold;
}
</style>
