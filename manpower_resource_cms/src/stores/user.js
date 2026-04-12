import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo as getUserInfoApi } from '@/api/auth'
import { useMenuStore } from '@/stores/menu'

// 安全的 JSON 解析函数
function safeJSONParse(str, defaultValue) {
  if (!str) return defaultValue
  try {
    return JSON.parse(str)
  } catch (error) {
    console.error('JSON parse error:', error)
    return defaultValue
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(safeJSONParse(localStorage.getItem('userInfo'), '{}'))
  const roles = ref(safeJSONParse(localStorage.getItem('roles'), '[]'))
  const permissions = ref(safeJSONParse(localStorage.getItem('permissions'), '[]'))
  const deptId = ref(safeJSONParse(localStorage.getItem('deptId'), null))
  const departmentName = ref(safeJSONParse(localStorage.getItem('departmentName'), ''))
  const userType = ref(localStorage.getItem('userType') || '')

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => {
    try {
      return userInfo.value.username || ''
    } catch (error) {
      return ''
    }
  })

  // 登录
  async function login(loginForm) {
    // 登录前清除旧的token，避免求职者token影响员工登录
    localStorage.removeItem('token')

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
    userType.value = res.data.userType || 'EMPLOYEE'

    // 保存用户的部门信息
    if (res.data.deptId) {
      deptId.value = res.data.deptId
      departmentName.value = res.data.departmentName || ''
    }

    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    localStorage.setItem('roles', JSON.stringify(roles.value))
    localStorage.setItem('permissions', JSON.stringify(permissions.value))
    localStorage.setItem('userType', userType.value)
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
    try {
      const res = await getUserInfoApi()
      userInfo.value = res.data.userInfo || res.data || {}
      roles.value = res.data.roles || []
      permissions.value = res.data.permissions || []

      // 更新部门信息
      if (res.data.deptId) {
        deptId.value = res.data.deptId
        departmentName.value = res.data.departmentName || ''
      }

      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      localStorage.setItem('roles', JSON.stringify(roles.value))
      localStorage.setItem('permissions', JSON.stringify(permissions.value))
      localStorage.setItem('deptId', JSON.stringify(deptId.value))
      localStorage.setItem('departmentName', JSON.stringify(departmentName.value))
    } catch (error) {
      console.error('Fetch user info error:', error)
    }

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
    userType.value = ''

    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('roles')
    localStorage.removeItem('permissions')
    localStorage.removeItem('userType')
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
    userType,
    isLoggedIn,
    username,
    login,
    fetchUserInfo,
    logout,
    hasRole,
    hasPermission
  }
})
