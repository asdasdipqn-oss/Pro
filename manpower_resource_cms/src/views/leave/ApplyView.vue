<template>
  <div class="apply-view">
    <el-card>
      <template #header>请假申请</template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="max-width: 600px"
      >
        <el-form-item label="假期类型" prop="leaveTypeId">
          <el-select v-model="form.leaveTypeId" placeholder="请选择假期类型" style="width: 100%">
            <el-option
              v-for="type in leaveTypes"
              :key="type.id"
              :label="type.typeName"
              :value="type.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
            @change="calculateDuration"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
            @change="calculateDuration"
          />
        </el-form-item>

        <el-form-item label="请假天数" prop="duration">
          <el-input-number v-model="form.duration" :min="0.5" :step="0.5" :precision="1" disabled />
          <span style="margin-left: 10px; color: #909399; font-size: 12px">根据时间自动计算</span>
        </el-form-item>

        <el-form-item label="请假原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请输入请假原因" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">提交申请</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listLeaveType, applyLeave } from '@/api/leave'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const leaveTypes = ref([])

const form = reactive({
  leaveTypeId: null,
  startTime: null,
  endTime: null,
  duration: 1,
  reason: '',
})

const rules = {
  leaveTypeId: [{ required: true, message: '请选择假期类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  duration: [{ required: true, message: '请输入请假天数', trigger: 'blur' }],
}

const fetchLeaveTypes = async () => {
  try {
    const res = await listLeaveType()
    leaveTypes.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

// 计算请假天数
const calculateDuration = () => {
  if (form.startTime && form.endTime) {
    const start = new Date(form.startTime)
    const end = new Date(form.endTime)
    if (end > start) {
      // 计算毫秒差，转为天数，保留一位小数
      const diffMs = end.getTime() - start.getTime()
      const diffDays = diffMs / (1000 * 60 * 60 * 24)
      // 最小0.5天，四舍五入到0.5
      form.duration = Math.max(0.5, Math.round(diffDays * 2) / 2)
    } else {
      form.duration = 0.5
    }
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await applyLeave(form)
    ElMessage.success('申请提交成功')
    router.push('/leave/my')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formRef.value.resetFields()
}

onMounted(fetchLeaveTypes)
</script>
