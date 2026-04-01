import request from '@/utils/request'

// 提交申诉
export function submitAppeal(data) {
  return request.post('/att/appeal', data)
}

// 撤回申诉
export function cancelAppeal(id) {
  return request.put(`/att/appeal/${id}/cancel`)
}

// 我的申诉
export function getMyAppeals(params) {
  return request.get('/att/appeal/my', { params })
}

// 获取详情
export function getAppealDetail(id) {
  return request.get(`/att/appeal/${id}`)
}

// 待审批列表
export function getPendingAppeals() {
  return request.get('/att/appeal/pending')
}

// 审批
export function approveAppeal(id, status, comment) {
  return request.put(`/att/appeal/${id}/approve`, null, { params: { status, comment } })
}

// 分页查询
export function getAppealPage(params) {
  return request.get('/att/appeal/page', { params })
}
