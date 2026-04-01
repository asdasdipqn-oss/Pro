import request from '@/utils/request'

// 分页查询考核结果
export function pageAssessResult(params) {
  return request.get('/assess/result/page', { params })
}

// 查询方案下的考核结果
export function getResultByPlan(planId) {
  return request.get(`/assess/result/plan/${planId}`)
}

// 获取考核结果详情
export function getAssessResult(id) {
  return request.get(`/assess/result/${id}`)
}

// 新增考核结果
export function addAssessResult(data) {
  return request.post('/assess/result', data)
}

// 提交自评
export function selfEvaluate(id, data) {
  return request.put(`/assess/result/${id}/self-evaluate`, data)
}

// 考核评分
export function assessScore(id, data) {
  return request.put(`/assess/result/${id}/assess`, data)
}

// 确认考核结果
export function confirmResult(id) {
  return request.put(`/assess/result/${id}/confirm`)
}

// 删除考核结果
export function deleteAssessResult(id) {
  return request.delete(`/assess/result/${id}`)
}

// ===================== 评分明细 =====================

// 查询考核结果的评分明细
export function getScoreDetailByResult(resultId) {
  return request.get(`/assess/score-detail/result/${resultId}`)
}

// 批量保存评分明细
export function batchSaveScoreDetail(data) {
  return request.post('/assess/score-detail/batch', data)
}

// 删除评分明细
export function deleteScoreDetail(id) {
  return request.delete(`/assess/score-detail/${id}`)
}
