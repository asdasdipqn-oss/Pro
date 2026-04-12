import request from '@/utils/request'

// 求职者认证
export function candidateLogin(data) {
  return request.post('/candidate/login', data)
}

export function candidateRegister(data) {
  return request.post('/candidate/register', data)
}

// 求职者个人信息
export function getCandidateProfile() {
  return request.get('/candidate/profile')
}

export function updateCandidateProfile(data) {
  return request.put('/candidate/profile', data)
}

// 求职者招聘相关
export function getPublishedJobs(page, pageSize) {
  return request.get('/recruit/jobs', { params: { page, pageSize } })
}

export function getJobDetail(id) {
  return request.get(`/recruit/jobs/${id}`)
}

export function applyJob(data) {
  return request.post('/recruit/applications', data)
}

export function getMyApplications() {
  return request.get('/recruit/applications/my')
}
