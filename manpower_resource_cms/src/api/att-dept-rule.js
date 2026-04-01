import request from '@/utils/request'

// 查询部门的考勤规则
export function getDeptRule(deptId) {
  return request.get(`/att/dept-rule/dept/${deptId}`)
}

// 设置部门考勤规则
export function setDeptRule(data) {
  return request.post('/att/dept-rule', data)
}

// 删除部门考勤规则关联
export function deleteDeptRule(id) {
  return request.delete(`/att/dept-rule/${id}`)
}
