import request from '@/utils/request'

// 获取角色列表
export function listRole() {
  return request.get('/sys/role/list')
}

// 新增角色
export function addRole(data) {
  return request.post('/sys/role', data)
}

// 修改角色
export function updateRole(data) {
  return request.put('/sys/role', data)
}

// 删除角色
export function deleteRole(id) {
  return request.delete(`/sys/role/${id}`)
}

// 获取角色菜单
export function getRoleMenus(roleId) {
  return request.get(`/sys/role/${roleId}/menus`)
}

// 保存角色菜单
export function saveRoleMenus(roleId, menuIds) {
  return request.put(`/sys/role/${roleId}/menus`, menuIds)
}
