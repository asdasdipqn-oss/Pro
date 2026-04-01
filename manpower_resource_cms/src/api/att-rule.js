import request from '@/utils/request'

// 分页查询考勤规则
export function pageAttRule(params) {
  return request.get('/att/rule/page', { params })
}

// 获取所有启用的考勤规则
export function listAttRule() {
  return request.get('/att/rule/list')
}

// 获取默认考勤规则
export function getDefaultAttRule() {
  return request.get('/att/rule/default')
}

// 获取考勤规则详情
export function getAttRule(id) {
  return request.get(`/att/rule/${id}`)
}

// 新增考勤规则
export function addAttRule(data) {
  return request.post('/att/rule', data)
}

// 修改考勤规则
export function updateAttRule(data) {
  return request.put('/att/rule', data)
}

// 删除考勤规则
export function deleteAttRule(id) {
  return request.delete(`/att/rule/${id}`)
}
