import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo as getUserInfoApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))
  const permissions = ref(JSON.parse(localStorage.getItem('permissions') || '[]'))
  const deptId = ref(JSON.parse(localStorage.getItem('deptId') || 'null'))
  const departmentName = ref(JSON.parse(localStorage.getItem('departmentName') || ''))

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

    // 保存用户的部门信息
    if (res.data.userInfo && res.data.userInfo.deptId) {
      deptId.value = res.data.userInfo.deptId
      departmentName.value = res.data.userInfo.departmentName || ''
    }

    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    localStorage.setItem('roles', JSON.stringify(roles.value))
    localStorage.setItem('permissions', JSON.stringify(permissions.value))
    localStorage.setItem('deptId', JSON.stringify(deptId.value))
    localStorage.setItem('departmentName', JSON.stringify(departmentName.value))

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

    // 更新部门信息
    if (res.data.userInfo && res.data.userInfo.deptId) {
      deptId.value = res.data.userInfo.deptId
      departmentName.value = res.data.userInfo.departmentName || ''
    }

    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    localStorage.setItem('roles', JSON.stringify(roles.value))
    localStorage.setItem('permissions', JSON.stringify(permissions.value))
    localStorage.setItem('deptId', JSON.stringify(deptId.value))
    localStorage.setItem('departmentName', JSON.stringify(departmentName.value))

    return res
  }

  // 退出登录
  function logout() {
    token.value = ''
    userInfo.value = {}
    roles.value = []
    permissions.value = []
    deptId.value = null
    departmentName.value = ''

    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('roles')
    localStorage.removeItem('permissions')
    localStorage.removeItem('deptId')
    localStorage.removeItem('departmentName')

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
    deptId,
    departmentName,
    isLoggedIn,
    username,
    login,
    fetchUserInfo,
    logout,
    hasRole,
    hasPermission
  }
})
