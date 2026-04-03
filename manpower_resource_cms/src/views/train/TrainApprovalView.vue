<template>
  <div class="train-approval-view">
    <el-card>
      <template #header>培训审核</template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="trainTitle" label="培训主题" width="200" />
        <el-table-column prop="trainTypeName" label="培训类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.trainTypeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column label="期望时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.expectedDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="expectedHours" label="期望时长" width="100" align="center">
          <template #default="{ row }">
            {{ row.expectedHours || '-' }} 小时
          </template>
        </el-table-column>
        <el-table-column prop="participantCount" label="参与人数" width="100" align="center">
          <template #default="{ row }">
            {{ row.participantCount || '-' }} 人
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button type="success" link @click="handleApprove(row, 1)" v-if="row.status === 0">通过</el-button>
            <el-button type="danger" link @click="handleApprove(row, 2)" v-if="row.status === 0">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无待审批的培训需求" />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="培训需求详情" width="600px">
      <div class="detail-content" v-if="currentRecord">
        <div class="detail-item">
          <span class="label">需求编号：</span>
          <span>{{ currentRecord.requestCode }}</span>
        </div>
        <div class="detail-item">
          <span class="label">培训主题：</span>
          <span>{{ currentRecord.trainTitle }}</span>
        </div>
        <div class="detail-item">
          <span class="label">培训类型：</span>
          <span>{{ currentRecord.trainTypeName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">申请人：</span>
          <span>{{ currentRecord.applicantName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">部门：</span>
          <span>{{ currentRecord.deptName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">期望时间：</span>
          <span>{{ formatDateTime(currentRecord.expectedDate) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">期望时长：</span>
          <span>{{ currentRecord.expectedHours || '-' }} 小时</span>
        </div>
        <div class="detail-item">
          <span class="label">参与人数：</span>
          <span>{{ currentRecord.participantCount || '-' }} 人</span>
        </div>
        <div class="detail-item">
          <span class="label">申请时间：</span>
          <span>{{ formatDateTime(currentRecord.createTime) }}</span>
        </div>
        <div class="detail-item full-width">
          <span class="label">需求描述：</span>
          <p>{{ currentRecord.requestReason }}</p>
        </div>
        <div class="detail-result" v-if="currentRecord.status !== 0 && currentRecord.status !== 3">
          <div class="detail-item">
            <span class="label">审批结果：</span>
            <el-tag :type="currentRecord.status === 1 ? 'success' : 'danger'">
              {{ currentRecord.statusName }}
            </el-tag>
          </div>
          <div class="detail-item" v-if="currentRecord.approvalComment">
            <span class="label">审批意见：</span>
            <span>{{ currentRecord.approvalComment }}</span>
          </div>
          <div class="detail-item" v-if="currentRecord.approvalTime">
            <span class="label">审批时间：</span>
            <span>{{ formatDateTime(currentRecord.approvalTime) }}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog v-model="approveDialogVisible" :title="approveStatus === 1 ? '审批通过' : '审批拒绝'" width="500px">
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="approveForm.comment" type="textarea" rows="4" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button :type="approveStatus === 1 ? 'success' : 'danger'" @click="submitApprove" :loading="submitting">
          {{ approveStatus === 1 ? '确认通过' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingTrainRequests, approveTrainRequest } from '@/api/train'

const loading = ref(false)
const submitting = ref(false)
const detailDialogVisible = ref(false)
const approveDialogVisible = ref(false)
const tableData = ref([])
const currentRecord = ref(null)
const approveStatus = ref(1)

const approveForm = reactive({
  comment: ''
})

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[status] || 'info'
}

const formatDateTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPendingTrainRequests()
    tableData.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleDetail = (row) => {
  currentRecord.value = row
  detailDialogVisible.value = true
}

const handleApprove = (row, status) => {
  currentRecord.value = row
  approveStatus.value = status
  approveForm.comment = ''
  approveDialogVisible.value = true
}

const submitApprove = async () => {
  try {
    submitting.value = true
    await approveTrainRequest(currentRecord.value.id, approveStatus.value, approveForm.comment)
    ElMessage.success(approveStatus.value === 1 ? '审批通过' : '已拒绝')
    approveDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.detail-content {
  font-size: 14px;
}
.detail-item {
  margin-bottom: 16px;
  display: flex;
  align-items: flex-start;
}
.detail-item .label {
  color: #909399;
  min-width: 100px;
  flex-shrink: 0;
}
.detail-item span {
  color: #606266;
}
.detail-item.full-width {
  flex-direction: column;
}
.detail-item.full-width p {
  margin-top: 8px;
  line-height: 1.6;
}
.detail-result {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}
</style>
