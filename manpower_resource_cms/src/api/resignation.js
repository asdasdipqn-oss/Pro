import request from '@/utils/request'

// 获取我的离职申请
export function getMyResignations() {
  return request.get('/resignation/my')
}

// 提交离职申请
export function applyResignation(data) {
  return request.post('/resignation/apply', data)
}

// 撤销离职申请
export function cancelResignation(id) {
  return request.put(`/resignation/${id}/cancel`)
}

// 分页查询离职申请（审批端）
export function pageResignation(params) {
  return request.get('/resignation/page', { params })
}

// 审批离职申请
export function approveResignation(id, approved, remark) {
  return request.put(`/resignation/${id}/approve`, null, {
    params: { approved, remark }
  })
}
