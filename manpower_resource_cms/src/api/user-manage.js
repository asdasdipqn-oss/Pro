import request from '@/utils/request'

// 分页查询用户
export function pageUser(params) {
  return request.get('/sys/user/page', { params })
}

// 获取用户详情
export function getUserDetail(id) {
  return request.get(`/sys/user/${id}`)
}

// 新增用户
export function addUser(data) {
  return request.post('/sys/user', data)
}

// 修改用户
export function updateUser(data) {
  return request.put('/sys/user', data)
}

// 删除用户
export function deleteUser(id) {
  return request.delete(`/sys/user/${id}`)
}

// 获取用户角色
export function getUserRoles(userId) {
  return request.get(`/sys/user/${userId}/roles`)
}

// 重置密码
export function resetPassword(userId) {
  return request.put(`/sys/user/${userId}/password`)
}

// 修改用户状态
export function changeUserStatus(userId, status) {
  return request.put(`/sys/user/${userId}/status/${status}`)
}
