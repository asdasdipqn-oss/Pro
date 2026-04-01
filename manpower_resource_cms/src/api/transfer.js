import request from '@/utils/request'

// 分页查询异动记录
export function pageTransfer(params) {
  return request.get('/emp/transfer/page', { params })
}

// 查询员工异动历史
export function getTransferByEmployee(employeeId, params) {
  return request.get(`/emp/transfer/employee/${employeeId}`, { params })
}

// 获取异动记录详情
export function getTransfer(id) {
  return request.get(`/emp/transfer/${id}`)
}

// 新增异动记录
export function addTransfer(data) {
  return request.post('/emp/transfer', data)
}

// 删除异动记录
export function deleteTransfer(id) {
  return request.delete(`/emp/transfer/${id}`)
}
