<template>
  <div class="salary-manage-view">
    <el-card class="summary-card">
      <template #header>
        <div class="card-header">
          <span>薪资管理 - {{ year }}年{{ month }}月</span>
          <div class="actions">
            <el-date-picker
              v-model="monthPicker"
              type="month"
              placeholder="选择月份"
              format="YYYY年MM月"
              @change="handleMonthChange"
              style="width: 160px; margin-right: 12px"
            />
            <el-button type="primary" @click="handleGenerate">生成薪资</el-button>
            <el-button type="warning" @click="handleBatchConfirm">一键确认</el-button>
            <el-button type="success" @click="handleBatchPay">一键发放</el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <div class="stat-cards">
        <div class="stat-item">
          <div class="stat-value">{{ summary.totalCount || 0 }}</div>
          <div class="stat-label">总人数</div>
        </div>
        <div class="stat-item draft">
          <div class="stat-value">{{ summary.draftCount || 0 }}</div>
          <div class="stat-label">草稿</div>
        </div>
        <div class="stat-item confirmed">
          <div class="stat-value">{{ summary.confirmedCount || 0 }}</div>
          <div class="stat-label">已确认</div>
        </div>
        <div class="stat-item paid">
          <div class="stat-value">{{ summary.paidCount || 0 }}</div>
          <div class="stat-label">已发放</div>
        </div>
        <div class="stat-item amount">
          <div class="stat-value">¥{{ formatMoney(summary.totalAmount) }}</div>
          <div class="stat-label">总金额</div>
        </div>
      </div>
    </el-card>

    <el-card style="margin-top: 16px">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="empCode" label="工号" width="100" />
        <el-table-column prop="empName" label="姓名" width="100" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="baseSalary" label="基本工资" width="100" align="right" />
        <el-table-column prop="performanceSalary" label="全勤奖" width="100" align="right" />
        <el-table-column prop="leaveDays" label="请假天数" width="90" align="center" />
        <el-table-column prop="grossSalary" label="应发工资" width="100" align="right" />
        <el-table-column prop="totalDeduction" label="扣款合计" width="100" align="right" />
        <el-table-column prop="netSalary" label="实发工资" width="110" align="right">
          <template #default="{ row }">
            <span style="color: #409eff; font-weight: bold">{{ row.netSalary }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{
              getStatusText(row.status)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" v-if="canEditSalary && row.status !== 2"
              >编辑</el-button
            >
            <el-button type="info" link @click="handleRecalculate(row)" v-if="canEditSalary && row.status === 0"
              >重算</el-button
            >
            <el-button type="warning" link @click="handleConfirm(row)" v-if="canEditSalary && row.status === 0"
              >确认</el-button
            >
            <el-button type="success" link @click="handlePay(row)" v-if="canEditSalary && row.status === 1"
              >发放</el-button
            >
            <el-tag v-if="row.status === 2" type="success" size="small">已发放</el-tag>
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

    <!-- 编辑薪资对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑薪资记录" width="700px">
      <el-form :model="editForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工">
              <el-input :value="editForm.empName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门">
              <el-input :value="editForm.deptName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">收入项</el-divider>
        <el-row :gutter="20">
          <el-col :span="8" v-for="item in incomeItems" :key="item.code">
            <el-form-item :label="item.name">
              <el-input-number
                v-model="editForm[item.field]"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">扣款项</el-divider>
        <el-row :gutter="20">
          <el-col :span="8" v-for="item in deductionItems" :key="item.code">
            <el-form-item :label="item.name">
              <el-input-number
                v-model="editForm[item.field]"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">备注</el-divider>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" rows="2" placeholder="薪资调整说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  pageSalaryByMonth,
  getSalarySummary,
  generateMonthlySalary,
  batchConfirmSalary,
  batchPaySalary,
  confirmSalary,
  paySalary,
  recalculateSalary,
  getSalaryDetail,
  updateSalaryRecord,
  listSalaryItem,
} from '@/api/salary'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const editDialogVisible = ref(false)
const tableData = ref([])
const summary = ref({})
const monthPicker = ref(new Date())

// 所有启用的薪资项目
const salaryItemList = ref([])

// 收入项（itemType 1,2）且有 recordField 的去重列表
const incomeItems = computed(() => {
  const seen = new Set()
  return salaryItemList.value
    .filter(item => (item.itemType === 1 || item.itemType === 2) && item.recordField)
    .filter(item => {
      if (seen.has(item.recordField)) return false
      seen.add(item.recordField)
      return true
    })
    .map(item => ({
      code: item.itemCode,
      name: item.itemName,
      field: item.recordField,
    }))
})

// 扣款项（itemType 3,4,5）且有 recordField 的去重列表
const deductionItems = computed(() => {
  const seen = new Set()
  return salaryItemList.value
    .filter(item => (item.itemType === 3 || item.itemType === 4 || item.itemType === 5) && item.recordField)
    .filter(item => {
      if (seen.has(item.recordField)) return false
      seen.add(item.recordField)
      return true
    })
    .map(item => ({
      code: item.itemCode,
      name: item.itemName,
      field: item.recordField,
    }))
})

const editForm = reactive({
  id: null,
  empName: '',
  deptName: '',
  baseSalary: 0,
  positionSalary: 0,
  performanceSalary: 0,
  fullAttendanceBonus: 0,
  overtimePay: 0,
  allowance: 0,
  bonus: 0,
  socialInsurance: 0,
  housingFund: 0,
  personalTax: 0,
  otherDeduction: 0,
  remark: '',
})

const year = computed(() =>
  monthPicker.value ? new Date(monthPicker.value).getFullYear() : new Date().getFullYear(),
)
const month = computed(() =>
  monthPicker.value ? new Date(monthPicker.value).getMonth() + 1 : new Date().getMonth() + 1,
)

// 权限检查：HR和Admin可以编辑/删除薪资，经理和普通员工只能查看
const canEditSalary = computed(() => {
  const isAdmin = userStore.roles.some(role =>
    role === 'ADMIN' || role === 'SUPER_ADMIN' || role === 'admin' || role === 'super_admin' || role === 'HR'
  )
  return isAdmin
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '草稿', 1: '已确认', 2: '已发放' }
  return map[status] || '未知'
}

const formatMoney = (val) => {
  if (!val) return '0.00'
  return Number(val)
    .toFixed(2)
    .replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const fetchData = async () => {
  loading.value = true
  try {
    const [listRes, summaryRes] = await Promise.all([
      pageSalaryByMonth({
        year: year.value,
        month: month.value,
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
      }),
      getSalarySummary(year.value, month.value),
    ])
    tableData.value = listRes.data?.records || []
    pagination.total = listRes.data?.total || 0
    summary.value = summaryRes.data || {}
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleMonthChange = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleGenerate = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要生成 ${year.value}年${month.value}月 的薪资吗？\n薪资将根据员工已设置的薪资标准进行计算，未设置薪资标准的员工各项金额将为0`,
      '生成薪资',
      { type: 'info' },
    )
    loading.value = true
    const res = await generateMonthlySalary({ year: year.value, month: month.value })
    ElMessage.success(res.data?.message || '生成成功')
    // 如果有员工未设置薪资标准，给出警告提示
    if (res.data?.warning) {
      setTimeout(() => {
        ElMessage.warning({ message: res.data.warning, duration: 6000 })
      }, 500)
    }
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  } finally {
    loading.value = false
  }
}

const handleBatchConfirm = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要一键确认 ${year.value}年${month.value}月 所有草稿薪资吗？`,
      '一键确认',
      { type: 'warning' },
    )
    loading.value = true
    const res = await batchConfirmSalary(year.value, month.value)
    ElMessage.success(res.data?.message || '确认成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  } finally {
    loading.value = false
  }
}

const handleBatchPay = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要一键发放 ${year.value}年${month.value}月 所有已确认薪资吗？`,
      '一键发放',
      { type: 'warning' },
    )
    loading.value = true
    const res = await batchPaySalary(year.value, month.value)
    ElMessage.success(res.data?.message || '发放成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  } finally {
    loading.value = false
  }
}

const handleRecalculate = async (row) => {
  try {
    await recalculateSalary(row.id)
    ElMessage.success('重新计算成功')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

const handleConfirm = async (row) => {
  try {
    await confirmSalary(row.id)
    ElMessage.success('确认成功')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

const handlePay = async (row) => {
  try {
    await paySalary(row.id)
    ElMessage.success('发放成功')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

const handleEdit = async (row) => {
  try {
    const res = await getSalaryDetail(row.id)
    const data = res.data || row
    Object.assign(editForm, {
      id: data.id,
      empName: row.empName,
      deptName: row.deptName,
      baseSalary: data.baseSalary || 0,
      positionSalary: data.positionSalary || 0,
      performanceSalary: data.performanceSalary || 0,
      fullAttendanceBonus: data.fullAttendanceBonus || 0,
      overtimePay: data.overtimePay || 0,
      allowance: data.allowance || 0,
      bonus: data.bonus || 0,
      socialInsurance: data.socialInsurance || 0,
      housingFund: data.housingFund || 0,
      personalTax: data.personalTax || 0,
      otherDeduction: data.otherDeduction || 0,
      remark: data.remark || '',
    })
    editDialogVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleEditSubmit = async () => {
  submitting.value = true
  try {
    await updateSalaryRecord(editForm.id, {
      baseSalary: editForm.baseSalary,
      positionSalary: editForm.positionSalary,
      performanceSalary: editForm.performanceSalary,
      fullAttendanceBonus: editForm.fullAttendanceBonus,
      overtimePay: editForm.overtimePay,
      allowance: editForm.allowance,
      bonus: editForm.bonus,
      socialInsurance: editForm.socialInsurance,
      housingFund: editForm.housingFund,
      personalTax: editForm.personalTax,
      otherDeduction: editForm.otherDeduction,
      remark: editForm.remark,
    })
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchData()
  listSalaryItem().then(res => {
    salaryItemList.value = (res.data || []).filter(i => i.status === 1)
  }).catch(() => {})
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.actions {
  display: flex;
  align-items: center;
}
.stat-cards {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
.stat-item {
  flex: 1;
  min-width: 140px;
  padding: 16px 20px;
  background: #f5f7fa;
  border-radius: 8px;
  text-align: center;
}
.stat-item.draft {
  background: #e6f7ff;
}
.stat-item.confirmed {
  background: #fff7e6;
}
.stat-item.paid {
  background: #f6ffed;
}
.stat-item.amount {
  background: #f0f5ff;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
