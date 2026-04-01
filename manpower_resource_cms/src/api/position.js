import request from '@/utils/request'

// 获取岗位列表
export function listPosition() {
  return request.get('/org/position/list')
}

// 新增岗位
export function addPosition(data) {
  return request.post('/org/position', data)
}

// 修改岗位
export function updatePosition(data) {
  return request.put('/org/position', data)
}

// 删除岗位
export function deletePosition(id) {
  return request.delete(`/org/position/${id}`)
}
