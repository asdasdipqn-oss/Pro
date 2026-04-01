import request from '@/utils/request'

// AI离职预测 - 完整参数
export function predictResignation(data) {
  return request.post('/ai/resignation/predict', data)
}

// AI离职预测 - 快速预测（使用默认6个月）
export function quickPredict(employeeId) {
  return request.get(`/ai/resignation/predict/${employeeId}`)
}
