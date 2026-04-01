<template>
  <div class="att-rule-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考勤规则管理</span>
          <el-button type="primary" @click="handleAdd">新增规则</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="规则名称">
          <el-input v-model="searchForm.ruleName" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="ruleName" label="规则名称" width="160" />
        <el-table-column prop="workStartTime" label="上班时间" width="100" align="center" />
        <el-table-column prop="workEndTime" label="下班时间" width="100" align="center" />
        <el-table-column prop="lateThreshold" label="迟到阈值(分)" width="110" align="center" />
        <el-table-column prop="earlyThreshold" label="早退阈值(分)" width="110" align="center" />
        <el-table-column prop="absentThreshold" label="缺勤阈值(分)" width="110" align="center" />
        <el-table-column label="需定位" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.needLocation === 1 ? 'warning' : 'info'" size="small">
              {{ row.needLocation === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="默认" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault === 1" type="success" size="small">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleDeptRule(row)">部门分配</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="editForm.id ? '编辑考勤规则' : '新增考勤规则'"
      width="650px"
    >
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="120px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="editForm.ruleName" placeholder="如 标准考勤规则" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上班时间" prop="workStartTime">
              <el-time-picker
                v-model="editForm.workStartTime"
                placeholder="上班时间"
                format="HH:mm"
                value-format="HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下班时间" prop="workEndTime">
              <el-time-picker
                v-model="editForm.workEndTime"
                placeholder="下班时间"
                format="HH:mm"
                value-format="HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="迟到阈值(分)">
              <el-input-number v-model="editForm.lateThreshold" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="早退阈值(分)">
              <el-input-number v-model="editForm.earlyThreshold" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="缺勤阈值(分)">
              <el-input-number v-model="editForm.absentThreshold" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="需要定位">
              <el-switch v-model="editForm.needLocation" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="默认规则">
              <el-switch v-model="editForm.isDefault" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-switch
                v-model="editForm.status"
                :active-value="1"
                :inactive-value="0"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="打卡范围(米)" v-if="editForm.needLocation === 1">
          <el-input-number v-model="editForm.locationRange" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="工作地点地址" v-if="editForm.needLocation === 1">
          <el-input v-model="editForm.workAddress" placeholder="工作地点地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
    <!-- 部门规则分配对话框 -->
    <el-dialog v-model="deptDialogVisible" title="部门考勤规则分配" width="500px">
      <p style="margin-bottom: 12px">
        当前规则：<strong>{{ currentRuleName }}</strong>
      </p>
      <el-form label-width="80px">
        <el-form-item label="选择部门">
          <el-select v-model="selectedDeptId" placeholder="选择部门" filterable style="width: 100%">
            <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <div style="margin-top: 12px">
        <p style="font-weight: 600; margin-bottom: 8px">已分配此规则的部门：</p>
        <el-tag
          v-for="item in currentDeptRules"
          :key="item.id"
          closable
          style="margin: 0 8px 8px 0"
          @close="handleRemoveDeptRule(item)"
          >{{ getDeptName(item.deptId) }}</el-tag
        >
        <el-empty v-if="currentDeptRules.length === 0" description="暂无分配" :image-size="40" />
      </div>
      <template #footer>
        <el-button @click="deptDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleAssignDept" :disabled="!selectedDeptId"
          >分配</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageAttRule, addAttRule, updateAttRule, deleteAttRule } from '@/api/att-rule'
import { getDeptRule, setDeptRule, deleteDeptRule } from '@/api/att-dept-rule'
import { getDepartmentTree } from '@/api/department'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const deptDialogVisible = ref(false)
const formRef = ref()
const tableData = ref([])
const deptList = ref([])
const currentDeptRules = ref([])
const currentRuleName = ref('')
const currentRuleId = ref(null)
const selectedDeptId = ref(null)

const searchForm = reactive({ ruleName: '', status: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const defaultForm = () => ({
  id: null,
  ruleName: '',
  workStartTime: '09:00:00',
  workEndTime: '18:00:00',
  lateThreshold: 30,
  earlyThreshold: 30,
  absentThreshold: 120,
  needLocation: 0,
  locationRange: 500,
  workLatitude: null,
  workLongitude: null,
  workAddress: '',
  isDefault: 0,
  status: 1,
})
const editForm = reactive(defaultForm())

const formRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  workStartTime: [{ required: true, message: '请选择上班时间', trigger: 'change' }],
  workEndTime: [{ required: true, message: '请选择下班时间', trigger: 'change' }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageAttRule({
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.ruleName = ''
  searchForm.status = null
  pagination.pageNum = 1
  fetchData()
}
const handleAdd = () => {
  Object.assign(editForm, defaultForm())
  dialogVisible.value = true
}
const handleEdit = (row) => {
  Object.assign(editForm, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editForm.id) {
      await updateAttRule(editForm)
      ElMessage.success('修改成功')
    } else {
      await addAttRule(editForm)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除规则"${row.ruleName}"吗？`, '提示', { type: 'warning' })
    await deleteAttRule(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const flattenDept = (tree, result = []) => {
  for (const node of tree) {
    result.push(node)
    if (node.children) flattenDept(node.children, result)
  }
  return result
}
const getDeptName = (id) => {
  const d = deptList.value.find((d) => d.id === id)
  return d ? d.deptName : id || '-'
}

const fetchDeptList = async () => {
  try {
    const res = await getDepartmentTree()
    deptList.value = flattenDept(res.data || [])
  } catch (e) {
    console.error(e)
  }
}

const handleDeptRule = async (row) => {
  currentRuleName.value = row.ruleName
  currentRuleId.value = row.id
  selectedDeptId.value = null
  deptDialogVisible.value = true
  // 查找所有部门中使用了该规则的
  currentDeptRules.value = []
  for (const dept of deptList.value) {
    try {
      const res = await getDeptRule(dept.id)
      const rules = res.data || []
      for (const r of rules) {
        if (r.ruleId === row.id) currentDeptRules.value.push(r)
      }
    } catch (e) {
      /* ignore */
    }
  }
}

const handleAssignDept = async () => {
  try {
    await setDeptRule({ deptId: selectedDeptId.value, ruleId: currentRuleId.value })
    ElMessage.success('分配成功')
    selectedDeptId.value = null
    handleDeptRule({ id: currentRuleId.value, ruleName: currentRuleName.value })
  } catch (e) {
    console.error(e)
  }
}

const handleRemoveDeptRule = async (item) => {
  try {
    await deleteDeptRule(item.id)
    ElMessage.success('移除成功')
    currentDeptRules.value = currentDeptRules.value.filter((r) => r.id !== item.id)
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchData()
  fetchDeptList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 20px;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
