import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useCandidateStore = defineStore('candidate', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userType = ref(localStorage.getItem('userType') || '')
  const username = ref(localStorage.getItem('candidateUsername') || '')
  const profile = ref(null)

  const isLoggedIn = computed(() => !!token.value && userType.value === 'candidate')

  async function login(loginForm) {
    localStorage.removeItem('token')

    const res = await request.post('/candidate/login', loginForm)
    token.value = res.data.token
    userType.value = 'candidate'
    username.value = res.data.username

    localStorage.setItem('token', token.value)
    localStorage.setItem('userType', 'candidate')
    localStorage.setItem('candidateUsername', username.value)

    return res
  }

  async function fetchProfile() {
    const res = await request.get('/candidate/profile')
    profile.value = res.data
    return res
  }

  function logout() {
    token.value = ''
    userType.value = ''
    username.value = ''
    profile.value = null

    localStorage.removeItem('token')
    localStorage.removeItem('userType')
    localStorage.removeItem('candidateUsername')
  }

  return {
    token,
    userType,
    username,
    profile,
    isLoggedIn,
    login,
    fetchProfile,
    logout
  }
})
