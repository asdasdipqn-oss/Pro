import request from '@/utils/request'

// 分页查询假期额度
export function pageLeaveBalance(params) {
  return request.get('/leave/balance/page', { params })
}

// 查询员工假期额度
export function getEmployeeBalance(employeeId, year) {
  return request.get(`/leave/balance/employee/${employeeId}`, { params: { year } })
}

// 新增假期额度
export function addLeaveBalance(data) {
  return request.post('/leave/balance', data)
}

// 批量初始化年度假期额度
export function initYearBalance(year) {
  return request.post(`/leave/balance/init/${year}`)
}

// 修改假期额度
export function updateLeaveBalance(data) {
  return request.put('/leave/balance', data)
}

// 删除假期额度
export function deleteLeaveBalance(id) {
  return request.delete(`/leave/balance/${id}`)
}
