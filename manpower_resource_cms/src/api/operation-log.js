import request from '@/utils/request'

// 分页查询操作日志
export function pageOperationLog(params) {
  return request.get('/sys/operation-log/page', { params })
}
