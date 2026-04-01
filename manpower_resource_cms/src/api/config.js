import request from '@/utils/request'

// 分页查询系统配置
export function pageConfig(params) {
  return request.get('/sys/config/page', { params })
}

// 获取所有配置
export function listConfig() {
  return request.get('/sys/config/list')
}

// 根据key获取配置
export function getConfigByKey(key) {
  return request.get(`/sys/config/key/${key}`)
}

// 新增配置
export function addConfig(data) {
  return request.post('/sys/config', data)
}

// 修改配置
export function updateConfig(data) {
  return request.put('/sys/config', data)
}

// 删除配置
export function deleteConfig(id) {
  return request.delete(`/sys/config/${id}`)
}
