import request from '@/utils/request'

// ===================== 日考勤统计 =====================

// 分页查询日考勤统计
export function pageDailyStat(params) {
  return request.get('/att/daily/page', { params })
}

// 查询员工日考勤统计
export function getDailyStatByEmployee(employeeId, params) {
  return request.get(`/att/daily/employee/${employeeId}`, { params })
}

// 获取日考勤统计详情
export function getDailyStat(id) {
  return request.get(`/att/daily/${id}`)
}

// 生成日度考勤统计数据
export function generateDailyStats(params) {
  return request.post('/att/daily/generate', null, { params })
}

// ===================== 月度考勤统计 =====================

// 分页查询月度考勤统计
export function pageMonthlyStat(params) {
  return request.get('/att/monthly/page', { params })
}

// 查询员工月度考勤统计
export function getMonthlyStatByEmployee(employeeId, params) {
  return request.get(`/att/monthly/employee/${employeeId}`, { params })
}

// 获取月度统计详情
export function getMonthlyStat(id) {
  return request.get(`/att/monthly/${id}`)
}

// 生成月度考勤统计数据
export function generateMonthlyStats(params) {
  return request.post('/att/monthly/generate', null, { params })
}
