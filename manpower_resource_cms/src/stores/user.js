import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo as getUserInfoApi, logout as logoutApi } from '@/api/auth'
import { useMenuStore } from './menu'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))
  const permissions = ref(JSON.parse(localStorage.getItem('permissions') || '[]'))

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value.username || '')

  // 登录
  async function login(loginForm) {
    const res = await loginApi(loginForm)
    console.log('[Login] Response:', res.data)
    token.value = res.data.token
    // 兼容两种返回格式：userInfo对象或直接的username
    userInfo.value = res.data.userInfo || {
      userId: res.data.userId,
      username: res.data.username
    }
    roles.value = res.data.roles || []
    permissions.value = res.data.permissions || []

    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    localStorage.setItem('roles', JSON.stringify(roles.value))
    localStorage.setItem('permissions', JSON.stringify(permissions.value))

    // 获取用户菜单权限
    const menuStore = useMenuStore()
    try {
      await menuStore.fetchUserMenus(res.data.userId)
    } catch (err) {
      console.error('[Login] 获取菜单失败，但不影响登录:', err)
    }

    return res
  }

  // 获取用户信息
  async function fetchUserInfo() {
    const res = await getUserInfoApi()
    userInfo.value = res.data.userInfo || {}
    roles.value = res.data.roles || []
    permissions.value = res.data.permissions || []
    
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    localStorage.setItem('roles', JSON.stringify(roles.value))
    localStorage.setItem('permissions', JSON.stringify(permissions.value))
    
    return res
  }

  // 退出登录
  function logout() {
    token.value = ''
    userInfo.value = {}
    roles.value = []
    permissions.value = []
    
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('roles')
    localStorage.removeItem('permissions')
    
    // 清除菜单权限
    const menuStore = useMenuStore()
    menuStore.clearMenus()
  }

  // 检查是否有角色
  function hasRole(role) {
    return roles.value.includes(role)
  }

  // 检查是否有权限
  function hasPermission(perm) {
    return permissions.value.includes(perm)
  }

  return {
    token,
    userInfo,
    roles,
    permissions,
    isLoggedIn,
    username,
    login,
    fetchUserInfo,
    logout,
    hasRole,
    hasPermission
  }
})
