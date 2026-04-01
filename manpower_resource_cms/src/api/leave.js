import request from '@/utils/request'

// 获取假期类型列表
export function listLeaveType() {
  return request.get('/leave/type/list')
}

// 提交请假申请
export function applyLeave(data) {
  return request.post('/leave/application', data)
}

// 获取我的请假列表
export function getMyLeave(params) {
  return request.get('/leave/application/my', { params })
}

// 撤回请假申请
export function cancelLeave(id) {
  return request.put(`/leave/application/${id}/cancel`)
}

// 获取待审批的请假列表
export function getPendingLeave() {
  return request.get('/leave/application/pending')
}

// 审批请假
export function approveLeave(id, status, comment) {
  return request.put(`/leave/application/${id}/approve`, null, {
    params: { status, comment },
  })
}

// ====== 假期类型管理 ======

// 获取所有假期类型（含禁用）
export function listAllLeaveType() {
  return request.get('/leave/type/all')
}

// 新增假期类型
export function addLeaveType(data) {
  return request.post('/leave/type', data)
}

// 修改假期类型
export function updateLeaveType(data) {
  return request.put('/leave/type', data)
}

// 删除假期类型
export function deleteLeaveType(id) {
  return request.delete(`/leave/type/${id}`)
}
