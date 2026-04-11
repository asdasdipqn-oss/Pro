import request from '@/utils/request'

// 获取已发布岗位列表
export function listPublishedJobs() {
  return request.get('/recruit/job/published')
}

// 获取我的投递记录
export function getMyApplications() {
  return request.get('/recruit/applications/my')
}

// 分页查询招聘岗位
export function pageJobs(params) {
  return request.get('/recruit/job/page', { params })
}

// 获取岗位详情
export function getJobDetail(id) {
  return request.get(`/recruit/job/${id}`)
}

// 发布招聘岗位
export function publishJob(data) {
  return request.post('/recruit/job', data)
}

// 修改招聘岗位
export function updateJob(data) {
  return request.put('/recruit/job', data)
}

// 关闭招聘岗位
export function closeJob(id) {
  return request.put(`/recruit/job/${id}/close`)
}

// ====== 简历管理 ======
// 投递简历
export function submitResume(data) {
  return request.post('/recruit/resume/submit', data)
}

// 分页查询简历
export function pageResumes(params) {
  return request.get('/recruit/resume/page', { params })
}

// 获取简历详情
export function getResumeDetail(id) {
  return request.get(`/recruit/resume/${id}`)
}

// 筛选简历
export function screenResume(id, status) {
  return request.put(`/recruit/resume/${id}/screen`, null, { params: { status } })
}

// 更新简历状态
export function updateResumeStatus(id, status) {
  return request.put(`/recruit/resume/${id}/status`, null, { params: { status } })
}

// 删除简历
export function deleteResume(id) {
  return request.delete(`/recruit/resume/${id}`)
}

// 招聘统计报表
export function getRecruitStatistics() {
  return request.get('/recruit/resume/statistics')
}

// ====== 面试管理 ======

// 分页查询面试记录
export function pageInterviews(params) {
  return request.get('/recruit/interview/page', { params })
}

// 查询简历的面试记录
export function getInterviewsByResume(resumeId) {
  return request.get(`/recruit/interview/resume/${resumeId}`)
}

// 获取面试记录详情
export function getInterviewDetail(id) {
  return request.get(`/recruit/interview/${id}`)
}

// 安排面试
export function addInterview(data) {
  return request.post('/recruit/interview', data)
}

// 更新面试记录
export function updateInterview(data) {
  return request.put('/recruit/interview', data)
}

// 完成面试（填写评价和结果）
export function completeInterview(id, data) {
  return request.put(`/recruit/interview/${id}/complete`, data)
}

// 取消面试
export function cancelInterview(id) {
  return request.put(`/recruit/interview/${id}/cancel`)
}

// 删除面试记录
export function deleteInterview(id) {
  return request.delete(`/recruit/interview/${id}`)
}

// 求职者 - 获取我的面试记录
export function getMyInterviews() {
  return request.get('/recruit/interview/my')
}

// 求职者 - 获取投递记录的面试详情
export function getInterviewsByApplication(applicationId) {
  return request.get(`/recruit/interview/application/${applicationId}`)
}
