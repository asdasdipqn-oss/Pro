<template>
  <div class="pending-view">
    <el-card>
      <template #header>待我审批</template>

      <el-form :inline="true" class="search-form">
        <el-form-item class="business-type-item" label="业务类型" style="width: 150px;">
          <el-select v-model="searchForm.businessType" placeholder="全部" clearable @change="fetchData" size="large">
            <el-option label="请假申请" :value="1" />
            <el-option label="考勤异常" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="businessType" label="业务类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.businessType === 1 ? 'primary' : 'warning'">
              {{ row.businessType === 1 ? '请假申请' : '考勤异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="createTime" label="申请时间" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link @click="handleApprove(row, 1)">通过</el-button>
            <el-button type="danger" link @click="handleApprove(row, 2)">驳回</el-button>
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

    <!-- 审批对话框 -->
    <el-dialog v-model="dialogVisible" :title="approveStatus === 1 ? '审批通过' : '审批驳回'" width="500px">
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="approveForm.opinion" type="textarea" rows="4" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :type="approveStatus === 1 ? 'success' : 'danger'" @click="submitApprove" :loading="submitting">
          {{ approveStatus === 1 ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingList, approve } from '@/api/approval'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const approveStatus = ref(1)
const currentRecord = ref(null)

const searchForm = reactive({
  businessType: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const approveForm = reactive({
  opinion: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPendingList({
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

const handleApprove = (row, status) => {
  currentRecord.value = row
  approveStatus.value = status
  approveForm.opinion = ''
  dialogVisible.value = true
}

const submitApprove = async () => {
  try {
    submitting.value = true
    await approve({
      businessType: currentRecord.value.businessType,
      businessId: currentRecord.value.businessId,
      status: approveStatus.value,
      opinion: approveForm.opinion
    })
    ElMessage.success(approveStatus.value === 1 ? '审批通过' : '已驳回')
    dialogVisible.value = false
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
.search-form {
  margin-bottom: 20px;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.pagination {
  margin-left: auto;
}
</style>
