<template>
  <div class="my-train-request-view">
    <el-card>
      <template #header>我的培训需求</template>

      <el-form :inline="true" class="search-form">
        <el-button type="primary" @click="handleAdd">提交培训需求</el-button>
      </el-form>

      <div class="request-list" v-if="tableData.length > 0">
        <el-card v-for="item in tableData" :key="item.id" class="request-item" shadow="hover">
          <div class="request-header">
            <span class="request-title">{{ item.trainTitle }}</span>
            <el-tag :type="getStatusType(item.status)" size="small">{{
              item.statusName
            }}</el-tag>
          </div>
          <div class="request-info">
            <div class="info-row">
              <span class="label">培训类型：</span>
              <span>{{ item.trainTypeName }}</span>
            </div>
            <div class="info-row">
              <span class="label">期望时间：</span>
              <span>{{ formatDateTime(item.expectedDate) }}</span>
            </div>
            <div class="info-row">
              <span class="label">期望时长：</span>
              <span>{{ item.expectedHours || '-' }} 小时</span>
            </div>
            <div class="info-row">
              <span class="label">参与人数：</span>
              <span>{{ item.participantCount || '-' }} 人</span>
            </div>
            <div class="info-row">
              <span class="label">申请部门：</span>
              <span>{{ item.deptName || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">申请时间：</span>
              <span>{{ formatDateTime(item.createTime) }}</span>
            </div>
          </div>
          <div class="request-content">
            <span class="label">需求描述：</span>
            <p>{{ item.requestReason }}</p>
          </div>
          <div class="request-result" v-if="item.status !== 0 && item.status !== 3">
            <div class="info-row" v-if="item.approvalComment">
              <span class="label">审批意见：</span>
              <span>{{ item.approvalComment }}</span>
            </div>
            <div class="info-row" v-if="item.approvalTime">
              <span class="label">审批时间：</span>
              <span>{{ formatDateTime(item.approvalTime) }}</span>
            </div>
          </div>
          <div class="request-actions" v-if="item.status === 0">
            <el-button type="danger" link @click="handleCancel(item)">撤回</el-button>
          </div>
        </el-card>
      </div>

      <el-empty v-else description="暂无培训需求" />
    </el-card>

    <!-- 提交需求对话框 -->
    <el-dialog v-model="dialogVisible" title="提交培训需求" width="600px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="培训主题" prop="trainTitle">
          <el-input v-model="form.trainTitle" placeholder="请输入培训主题" />
        </el-form-item>
        <el-form-item label="培训类型" prop="trainType">
          <el-select v-model="form.trainType" placeholder="请选择" style="width: 100%">
            <el-option label="入职培训" :value="1" />
            <el-option label="技能培训" :value="2" />
            <el-option label="管理培训" :value="3" />
            <el-option label="安全培训" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="期望时间">
              <el-date-picker
                v-model="form.expectedDate"
                type="datetime"
                placeholder="选择期望培训时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期望时长">
              <el-input-number v-model="form.expectedHours" :min="1" :max="100" style="width: 100%">
                <template #append>小时</template>
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="参与人数">
          <el-input-number v-model="form.participantCount" :min="1" style="width: 100%">
            <template #append>人</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="需求描述" prop="requestReason">
          <el-input
            v-model="form.requestReason"
            type="textarea"
            rows="4"
            placeholder="请详细描述培训需求..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyTrainRequests, submitTrainRequest, cancelTrainRequest } from '@/api/train'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const formRef = ref(null)

const form = reactive({
  trainTitle: '',
  trainType: null,
  expectedDate: null,
  expectedHours: 8,
  participantCount: 1,
  requestReason: ''
})

const formRules = {
  trainTitle: [{ required: true, message: '请输入培训主题', trigger: 'blur' }],
  trainType: [{ required: true, message: '请选择培训类型', trigger: 'change' }],
  requestReason: [{ required: true, message: '请输入需求描述', trigger: 'blur' }]
}

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
    const res = await getMyTrainRequests()
    tableData.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, {
    trainTitle: '',
    trainType: null,
    expectedDate: null,
    expectedHours: 8,
    participantCount: 1,
    requestReason: ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    await submitTrainRequest(form)
    ElMessage.success('提交成功')
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要撤回该培训需求吗？', '提示', { type: 'warning' })
    await cancelTrainRequest(row.id)
    ElMessage.success('已撤回')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

onMounted(fetchData)
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}
.request-list {
  display: grid;
  gap: 16px;
}
.request-item {
  border-radius: 12px;
}
.request-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.request-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
}
.request-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.info-row {
  font-size: 14px;
  color: #606266;
}
.info-row .label {
  color: #909399;
}
.request-content {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  font-size: 14px;
}
.request-content .label {
  color: #909399;
}
.request-result {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  font-size: 14px;
}
.request-actions {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
}
.request-content p {
  margin: 8px 0 0;
  color: #606266;
  line-height: 1.6;
}
</style>
