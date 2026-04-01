import request from '@/utils/request'

// 获取消息列表
export function getNotifications() {
  return request.get('/notification/list')
}

// 获取未读消息数量
export function getUnreadCount() {
  return request.get('/notification/unread-count')
}

// 标记单条消息已读
export function markNotificationRead(data) {
  return request.put('/notification/read', data)
}

// 全部标记已读
export function markAllNotificationRead() {
  return request.put('/notification/read-all')
}
