import request from '@/utils/request'

// 根据业务查询审批日志
export function getApprovalLogByBusiness(businessType, businessId) {
  return request.get('/approval/log/business', { params: { businessType, businessId } })
}

// 分页查询所有审批日志
export function pageApprovalLog(params) {
  return request.get('/approval/log/page', { params })
}
