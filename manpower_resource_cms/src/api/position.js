import request from '@/utils/request'

// 获取岗位列表
export function listPosition(params) {
  return request.get('/org/position/list', { params })
}

// 根据部门获取岗位列表
export function listPositionByDept(deptId) {
  return request.get(`/org/position/list/${deptId}`)
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
