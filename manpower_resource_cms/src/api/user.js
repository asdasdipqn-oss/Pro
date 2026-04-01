import request from '@/utils/request'

// 获取当前用户信息
export function getCurrentUser() {
  return request.get('/auth/info')
}

// 修改密码
export function changePassword(data) {
  return request.put('/sys/user/password', data)
}

// 更新个人信息
export function updateProfile(data) {
  return request.put('/sys/user/profile', data)
}
