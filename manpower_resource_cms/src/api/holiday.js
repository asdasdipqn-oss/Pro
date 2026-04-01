import request from '@/utils/request'

// 分页查询节假日
export function pageHoliday(params) {
  return request.get('/sys/holiday/page', { params })
}

// 查询年度节假日
export function getHolidayByYear(year) {
  return request.get(`/sys/holiday/year/${year}`)
}

// 新增节假日
export function addHoliday(data) {
  return request.post('/sys/holiday', data)
}

// 批量新增节假日
export function batchAddHoliday(data) {
  return request.post('/sys/holiday/batch', data)
}

// 修改节假日
export function updateHoliday(data) {
  return request.put('/sys/holiday', data)
}

// 删除节假日
export function deleteHoliday(id) {
  return request.delete(`/sys/holiday/${id}`)
}
