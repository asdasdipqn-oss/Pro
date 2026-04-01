import request from '@/utils/request'

// 获取部门树
export function getDepartmentTree() {
  return request.get('/org/department/tree')
}

// 新增部门
export function addDepartment(data) {
  return request.post('/org/department', data)
}

// 修改部门
export function updateDepartment(data) {
  return request.put('/org/department', data)
}

// 删除部门
export function deleteDepartment(id) {
  return request.delete(`/org/department/${id}`)
}
