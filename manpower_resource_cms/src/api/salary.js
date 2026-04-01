import request from '@/utils/request'

// 查询我的薪资记录
export function getMySalary(year) {
  return request.get('/salary/record/my', { params: { year } })
}

// 查询指定月份薪资条
export function getMySalaryByMonth(year, month) {
  return request.get(`/salary/record/my/${year}/${month}`)
}

// 分页查询月度薪资
export function pageSalaryByMonth(params) {
  return request.get('/salary/record/page', { params })
}

// 确认薪资
export function confirmSalary(id) {
  return request.put(`/salary/record/${id}/confirm`)
}

// 发放薪资
export function paySalary(id) {
  return request.put(`/salary/record/${id}/pay`)
}

// 生成月度薪资
export function generateMonthlySalary(data) {
  return request.post('/salary/record/generate', data)
}

// 一键发放薪资
export function batchPaySalary(year, month) {
  return request.put('/salary/record/batch-pay', null, { params: { year, month } })
}

// 一键确认薪资
export function batchConfirmSalary(year, month) {
  return request.put('/salary/record/batch-confirm', null, { params: { year, month } })
}

// 重新计算薪资
export function recalculateSalary(id) {
  return request.put(`/salary/record/${id}/recalculate`)
}

// 获取薪资统计摘要
export function getSalarySummary(year, month) {
  return request.get('/salary/record/summary', { params: { year, month } })
}

// 导出薪资数据
export function exportSalary(year, month) {
  return request.get('/salary/record/export', {
    params: { year, month },
    responseType: 'blob',
  })
}

// 推送薪资通知
export function pushSalaryNotification(year, month) {
  return request.post('/salary/record/push-notification', null, { params: { year, month } })
}

// 获取未读薪资通知
export function getUnreadNotifications() {
  return request.get('/salary/record/unread-notifications')
}

// 标记薪资通知已读
export function markSalaryAsRead(id) {
  return request.put(`/salary/record/${id}/read`)
}

// 获取薪资记录详情
export function getSalaryDetail(id) {
  return request.get(`/salary/record/${id}`)
}

// 更新薪资记录
export function updateSalaryRecord(id, data) {
  return request.put(`/salary/record/${id}`, data)
}

// ==================== 薪资项目管理 ====================

// 分页查询薪资项目
export function pageSalaryItem(params) {
  return request.get('/salary/item/page', { params })
}

// 获取所有启用的薪资项目
export function listSalaryItem() {
  return request.get('/salary/item/list')
}

// 获取薪资项目详情
export function getSalaryItem(id) {
  return request.get(`/salary/item/${id}`)
}

// 新增薪资项目
export function addSalaryItem(data) {
  return request.post('/salary/item', data)
}

// 修改薪资项目
export function updateSalaryItem(data) {
  return request.put('/salary/item', data)
}

// 删除薪资项目
export function deleteSalaryItem(id) {
  return request.delete(`/salary/item/${id}`)
}

// ==================== 薪资标准管理 ====================

// 分页查询薪资标准
export function pageSalaryStandard(params) {
  return request.get('/salary/standard/page', { params })
}

// 查询员工薪资标准
export function getEmployeeSalaryStandard(employeeId) {
  return request.get(`/salary/standard/employee/${employeeId}`)
}

// 获取薪资标准详情
export function getSalaryStandard(id) {
  return request.get(`/salary/standard/${id}`)
}

// 新增薪资标准
export function addSalaryStandard(data) {
  return request.post('/salary/standard', data)
}

// 批量设置员工薪资标准
export function batchSaveSalaryStandard(data) {
  return request.post('/salary/standard/batch', data)
}

// 修改薪资标准
export function updateSalaryStandard(data) {
  return request.put('/salary/standard', data)
}

// 删除薪资标准
export function deleteSalaryStandard(id) {
  return request.delete(`/salary/standard/${id}`)
}
