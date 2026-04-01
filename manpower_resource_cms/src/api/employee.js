import request from '@/utils/request'

// 分页查询员工
export function pageEmployee(params) {
  return request.get('/emp/employee/page', { params })
}

// 获取员工列表（不分页）
export function listEmployee() {
  return request.get('/emp/employee/list')
}

// 新增员工
export function addEmployee(data) {
  return request.post('/emp/employee', data)
}

// 修改员工
export function updateEmployee(data) {
  return request.put('/emp/employee', data)
}

// 删除员工
export function deleteEmployee(id) {
  return request.delete(`/emp/employee/${id}`)
}

// 批量删除员工
export function batchDeleteEmployee(ids) {
  return request.delete('/emp/employee/batch', { data: ids })
}

// 批量修改员工状态
export function batchUpdateStatus(ids, status) {
  return request.put('/emp/employee/batch/status', null, {
    params: { ids: ids.join(','), status }
  })
}

// 导出员工
export function exportEmployee(params) {
  return request.get('/emp/employee/export', {
    params,
    responseType: 'blob'
  })
}

// 下载导入模板
export function downloadTemplate() {
  return request.get('/emp/employee/template', { responseType: 'blob' })
}

// 导入员工
export function importEmployee(formData) {
  return request.post('/emp/employee/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
