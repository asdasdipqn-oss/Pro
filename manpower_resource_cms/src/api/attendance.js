import request from '@/utils/request'

// 获取今日打卡记录
export function getTodayRecords() {
  return request.get('/att/clock/today')
}

// 打卡
export function clockIn(data) {
  return request.post('/att/clock', data)
}

// 获取月度打卡记录
export function getMonthRecords(year, month) {
  return request.get('/att/clock/month', {
    params: { year, month }
  })
}

// 获取考勤日历数据
export function getCalendarData(year, month) {
  return request.get('/att/clock/calendar', {
    params: { year, month }
  })
}

// 导出考勤数据
export function exportAttendance(year, month) {
  return request.get('/att/clock/export', {
    params: { year, month },
    responseType: 'blob'
  })
}
