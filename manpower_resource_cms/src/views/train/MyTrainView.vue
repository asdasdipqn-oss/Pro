<template>
  <div class="my-train-view">
    <el-card>
      <template #header>我的培训</template>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无培训记录" />

      <div class="train-list" v-else>
        <el-card v-for="item in tableData" :key="item.id" class="train-item" shadow="hover">
          <div class="train-header">
            <span class="train-name">{{ item.planName }}</span>
            <el-tag :type="getStatusType(item.status)" size="small">{{
              getStatusName(item.status)
            }}</el-tag>
          </div>
          <div class="train-info">
            <div class="info-row">
              <span class="label">培训类型：</span>
              <span>{{ getTypeName(item.trainType) }}</span>
            </div>
            <div class="info-row">
              <span class="label">讲师：</span>
              <span>{{ item.trainer || '待定' }}</span>
            </div>
            <div class="info-row">
              <span class="label">培训时间：</span>
              <span>{{ formatDateTime(item.startTime) }} ~ {{ formatDateTime(item.endTime) }}</span>
            </div>
            <div class="info-row">
              <span class="label">培训地点：</span>
              <span>{{ item.trainLocation || '待定' }}</span>
            </div>
          </div>
          <div class="train-content" v-if="item.description">
            <span class="label">培训内容：</span>
            <p>{{ item.description }}</p>
          </div>
          <div class="train-actions" v-if="item.status === 1">
            <el-button type="primary" @click="handleSignIn(item)" :loading="signingId === item.id">
              培训签到
            </el-button>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyTrains, signIn } from '@/api/train'

const loading = ref(false)
const tableData = ref([])
const signingId = ref(null)

const getTypeName = (type) => {
  const map = { 1: '入职培训', 2: '技能培训', 3: '管理培训', 4: '安全培训' }
  return map[type] || '其他'
}

const getStatusName = (status) => {
  const map = { 0: '未开始', 1: '进行中', 2: '已结束', 3: '已取消' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'primary', 2: 'success', 3: 'warning' }
  return map[status] || 'info'
}

const formatDateTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyTrains()
    tableData.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSignIn = async (item) => {
  signingId.value = item.id
  try {
    await signIn(item.id)
    ElMessage.success('签到成功')
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    signingId.value = null
  }
}

onMounted(fetchData)
</script>

<style scoped>
.train-list {
  display: grid;
  gap: 16px;
}
.train-item {
  border-radius: 12px;
}
.train-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.train-name {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
}
.train-info {
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
.train-content {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  font-size: 14px;
}
.train-content .label {
  color: #909399;
}
.train-actions {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
}
.train-content p {
  margin: 8px 0 0;
  color: #606266;
  line-height: 1.6;
}
</style>
