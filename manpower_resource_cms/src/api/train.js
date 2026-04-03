import request from '@/utils/request'

// 分页查询培训计划
export function pageTrainPlans(params) {
  return request.get('/train/plan/page', { params })
}

// 查询我的培训
export function getMyTrains() {
  return request.get('/train/plan/my')
}

// 获取培训详情
export function getTrainDetail(id) {
  return request.get(`/train/plan/${id}`)
}

// 创建培训计划
export function createTrainPlan(data) {
  return request.post('/train/plan', data)
}

// 修改培训计划
export function updateTrainPlan(data) {
  return request.put('/train/plan', data)
}

// 删除培训计划
export function deleteTrainPlan(id) {
  return request.delete(`/train/plan/${id}`)
}

// ====== 参训人员管理 ======
// 指定参训人员
export function assignParticipants(planId, employeeIds) {
  return request.post(`/train/plan/${planId}/participants`, employeeIds)
}

// 获取参训人员列表
export function getParticipants(planId) {
  return request.get(`/train/plan/${planId}/participants`)
}

// 移除参训人员
export function removeParticipant(planId, employeeId) {
  return request.delete(`/train/plan/${planId}/participants/${employeeId}`)
}

// 参训签到
export function signIn(planId) {
  return request.put(`/train/plan/${planId}/sign-in`)
}

// 录入考核成绩
export function recordScore(participantId, score, evaluation) {
  return request.put(`/train/plan/participant/${participantId}/score`, null, {
    params: { score, evaluation }
  })
}

// ====== 培训需求管理 ======
// 提交培训需求
export function submitTrainRequest(data) {
  return request.post('/train/request', data)
}

// 撤回培训需求
export function cancelTrainRequest(id) {
  return request.put(`/train/request/${id}/cancel`)
}

// 获取我的培训需求
export function getMyTrainRequests() {
  return request.get('/train/request/my')
}

// 获取培训需求详情
export function getTrainRequestDetail(id) {
  return request.get(`/train/request/${id}`)
}

// 获取待审批的培训需求
export function getPendingTrainRequests() {
  return request.get('/train/request/pending')
}

// 审批培训需求
export function approveTrainRequest(id, status, comment) {
  return request.put(`/train/request/${id}/approve`, null, {
    params: { status, comment }
  })
}

// 分页查询所有培训需求
export function pageTrainRequests(params) {
  return request.get('/train/request/page', { params })
}
