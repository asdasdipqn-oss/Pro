import request from '@/utils/request'

// 员工统计概览
export function getEmployeeOverview() {
  return request.get('/report/employee/overview')
}

// 部门员工分布统计
export function getDeptDistribution() {
  return request.get('/report/employee/dept-distribution')
}

// 考勤月度统计
export function getAttendanceMonthly(year, month) {
  return request.get('/report/attendance/monthly', { params: { year, month } })
}

// 薪资月度统计
export function getSalaryMonthly(year, month) {
  return request.get('/report/salary/monthly', { params: { year, month } })
}

// 薪资年度趋势
export function getSalaryTrend(year) {
  return request.get('/report/salary/trend', { params: { year } })
}

// 请假统计
export function getLeaveStatistics(year, month) {
  return request.get('/report/leave/statistics', { params: { year, month } })
}

// 审批统计
export function getApprovalStatistics() {
  return request.get('/report/approval/statistics')
}
