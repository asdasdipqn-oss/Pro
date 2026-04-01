import request from '@/utils/request'

// ====== 审批流程 ======
// 创建审批流程
export function createApprovalFlow(data) {
  return request.post('/approval/flow', data)
}

// 更新审批流程
export function updateApprovalFlow(data) {
  return request.put('/approval/flow', data)
}

// 删除审批流程
export function deleteApprovalFlow(id) {
  return request.delete(`/approval/flow/${id}`)
}

// 获取审批流程详情
export function getApprovalFlowDetail(id) {
  return request.get(`/approval/flow/${id}`)
}

// 分页查询审批流程
export function pageApprovalFlow(params) {
  return request.get('/approval/flow/page', { params })
}

// 获取启用的审批流程列表
export function listEnabledFlows(flowType) {
  return request.get('/approval/flow/list', { params: { flowType } })
}

// ====== 审批记录 ======
// 执行审批
export function approve(data) {
  return request.post('/approval/record/approve', data)
}

// 查询待我审批的列表
export function getPendingList(params) {
  return request.get('/approval/record/pending', { params })
}

// 查询我已审批的列表
export function getApprovedList(params) {
  return request.get('/approval/record/approved', { params })
}

// 根据业务查询审批记录
export function getByBusiness(businessType, businessId) {
  return request.get('/approval/record/business', { params: { businessType, businessId } })
}

// 分页查询所有审批记录
export function pageAllRecords(params) {
  return request.get('/approval/record/page', { params })
}
