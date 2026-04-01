import request from '@/utils/request'

// 分页查询考核方案
export function pageAssessPlan(params) {
  return request.get('/assess/plan/page', { params })
}

// 新增考核方案
export function addAssessPlan(data) {
  return request.post('/assess/plan', data)
}

// 修改考核方案
export function updateAssessPlan(data) {
  return request.put('/assess/plan', data)
}

// 发布考核方案
export function publishAssessPlan(id) {
  return request.put(`/assess/plan/${id}/publish`)
}

// 结束考核方案
export function finishAssessPlan(id) {
  return request.put(`/assess/plan/${id}/finish`)
}

// 删除考核方案
export function deleteAssessPlan(id) {
  return request.delete(`/assess/plan/${id}`)
}

// ===================== 考核任务 =====================

// 分页查询考核任务（管理员）
export function pageAssessTask(params) {
  return request.get('/assess/task/page', { params })
}

// 查询已发布的考核任务（员工可见）
export function pagePublishedTask(params) {
  return request.get('/assess/task/published', { params })
}

// 获取考核任务详情
export function getAssessTask(id) {
  return request.get(`/assess/task/${id}`)
}

// 创建考核任务
export function addAssessTask(data) {
  return request.post('/assess/task', data)
}

// 更新考核任务
export function updateAssessTask(data) {
  return request.put('/assess/task', data)
}

// 发布考核任务
export function publishAssessTask(id) {
  return request.put(`/assess/task/${id}/publish`)
}

// 截止考核任务
export function closeAssessTask(id) {
  return request.put(`/assess/task/${id}/close`)
}

// 删除考核任务
export function deleteAssessTask(id) {
  return request.delete(`/assess/task/${id}`)
}

// ===================== 考核提交 =====================

// 员工上传作业
export function uploadSubmission(formData) {
  return request.post('/assess/submission/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 管理员分页查询提交记录
export function pageSubmission(params) {
  return request.get('/assess/submission/page', { params })
}

// 员工查看自己的提交记录
export function mySubmissions() {
  return request.get('/assess/submission/my')
}

// 员工查看某个任务的提交状态
export function myTaskSubmission(taskId) {
  return request.get(`/assess/submission/my/task/${taskId}`)
}

// 员工查看某个任务的提交历史
export function myTaskSubmissionHistory(taskId) {
  return request.get(`/assess/submission/my/task/${taskId}/history`)
}

// 管理员评分
export function scoreSubmission(id, data) {
  return request.put(`/assess/submission/${id}/score`, data)
}

// 管理员获取评分模板
export function getSubmissionScoreTemplate(id) {
  return request.get(`/assess/submission/${id}/score-template`)
}

// 管理员退回
export function rejectSubmission(id, data) {
  return request.put(`/assess/submission/${id}/reject`, data)
}

// 下载提交文件
export function downloadSubmissionFile(id) {
  return request.get(`/assess/submission/${id}/download`, { responseType: 'blob' })
}

// ===================== 考核指标 =====================

// 查询方案下的考核指标
export function getIndicatorsByPlan(planId) {
  return request.get(`/assess/indicator/plan/${planId}`)
}

// 获取指标详情
export function getIndicator(id) {
  return request.get(`/assess/indicator/${id}`)
}

// 新增考核指标
export function addIndicator(data) {
  return request.post('/assess/indicator', data)
}

// 批量新增考核指标
export function batchAddIndicator(data) {
  return request.post('/assess/indicator/batch', data)
}

// 修改考核指标
export function updateIndicator(data) {
  return request.put('/assess/indicator', data)
}

// 删除考核指标
export function deleteIndicator(id) {
  return request.delete(`/assess/indicator/${id}`)
}
