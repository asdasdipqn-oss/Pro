<template>
  <div class="holiday-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>节假日管理</span>
          <el-button type="primary" @click="handleAdd">新增节假日</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="年度">
          <el-date-picker v-model="searchYear" type="year" placeholder="选择年度"
            format="YYYY" @change="handleYearChange" style="width: 120px" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.holidayType" placeholder="全部" clearable>
            <el-option label="法定节假日" :value="1" />
            <el-option label="调休工作日" :value="2" />
            <el-option label="公司假日" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="holidayName" label="节假日名称" width="180" />
        <el-table-column prop="holidayDate" label="日期" width="130" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.holidayType]">{{ typeMap[row.holidayType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="year" label="年份" width="80" align="center" />
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑节假日' : '新增节假日'" width="500px">
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="节假日名称" prop="holidayName">
          <el-input v-model="editForm.holidayName" placeholder="如 春节" />
        </el-form-item>
        <el-form-item label="日期" prop="holidayDate">
          <el-date-picker v-model="editForm.holidayDate" type="date"
            placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="类型" prop="holidayType">
          <el-select v-model="editForm.holidayType" style="width: 100%">
            <el-option label="法定节假日" :value="1" />
            <el-option label="调休工作日" :value="2" />
            <el-option label="公司假日" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageHoliday, addHoliday, updateHoliday, deleteHoliday } from '@/api/holiday'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const tableData = ref([])
const searchYear = ref(new Date())

const currentYear = new Date().getFullYear()
const searchForm = reactive({ year: currentYear, holidayType: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const typeMap = { 1: '法定节假日', 2: '调休工作日', 3: '公司假日' }
const typeTagMap = { 1: 'danger', 2: 'warning', 3: 'success' }

const defaultForm = () => ({ id: null, holidayName: '', holidayDate: '', holidayType: 1, remark: '' })
const editForm = reactive(defaultForm())

const formRules = {
  holidayName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  holidayDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  holidayType: [{ required: true, message: '请选择类型', trigger: 'change' }],
}

const handleYearChange = (val) => { searchForm.year = val ? new Date(val).getFullYear() : null }

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageHoliday({ ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleReset = () => { searchForm.year = currentYear; searchForm.holidayType = null; searchYear.value = new Date(); pagination.pageNum = 1; fetchData() }
const handleAdd = () => { Object.assign(editForm, defaultForm()); dialogVisible.value = true }
const handleEdit = (row) => { Object.assign(editForm, { ...row }); dialogVisible.value = true }

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editForm.id) { await updateHoliday(editForm); ElMessage.success('修改成功') }
    else { await addHoliday(editForm); ElMessage.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } catch (e) { console.error(e) } finally { submitting.value = false }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除"${row.holidayName}"吗？`, '提示', { type: 'warning' })
    await deleteHoliday(row.id); ElMessage.success('删除成功'); fetchData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
