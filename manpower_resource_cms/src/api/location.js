import request from '@/utils/request'

// 分页查询打卡地点
export function pageLocation(params) {
  return request.get('/att/location/page', { params })
}

// 获取启用的打卡地点
export function listEnabledLocation() {
  return request.get('/att/location/enabled')
}

// 新增打卡地点
export function addLocation(data) {
  return request.post('/att/location', data)
}

// 修改打卡地点
export function updateLocation(data) {
  return request.put('/att/location', data)
}

// 切换地点状态
export function toggleLocationStatus(id, status) {
  return request.put(`/att/location/${id}/status`, null, {
    params: { status }
  })
}

// 删除打卡地点
export function deleteLocation(id) {
  return request.delete(`/att/location/${id}`)
}
