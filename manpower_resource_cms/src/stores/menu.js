import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useMenuStore = defineStore('menu', () => {
  const menuTree = ref(JSON.parse(localStorage.getItem('menuTree') || '[]'))
  const menuPaths = ref(JSON.parse(localStorage.getItem('menuPaths') || '[]'))

  // 从后端获取用户菜单
  async function fetchUserMenus(userId) {
    try {
      const res = await request.get('/sys/menu/user/tree', { params: { userId } })
      const menus = res.data || []
      menuTree.value = menus
      // 提取所有菜单路径用于权限判断
      menuPaths.value = extractPaths(menus)

      localStorage.setItem('menuTree', JSON.stringify(menus))
      localStorage.setItem('menuPaths', JSON.stringify(menuPaths.value))

      return menus
    } catch (error) {
      console.error('获取用户菜单失败', error)
      return []
    }
  }

  // 递归提取所有菜单路径
  function extractPaths(menus) {
    const paths = []
    function traverse(items) {
      for (const item of items) {
        if (item.path) {
          paths.push(item.path)
        }
        if (item.children && item.children.length > 0) {
          traverse(item.children)
        }
      }
    }
    traverse(menus)
    return paths
  }

  // 获取用户类型
  function getUserType() {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.roles ? 'admin' : 'user'
  }

  // 检查是否是管理员
  function isAdminUser() {
    return getUserType() === 'admin'
  }

  // 需要限制的路径列表
  const restrictedPaths = ['/dashboard']

  // 检查用户是否有访问路径的权限（用于限制普通员工访问工作台）
  function hasPathPermission(path) {
    // 管理员可以访问所有路径
    if (isAdminUser()) return true

    // 检查是否是需要限制的路径
    const isRestrictedPath = restrictedPaths.some(rp => path.startsWith(rp))
    if (!isRestrictedPath) return true

    // 检查用户是否有工作台菜单权限
    const hasDashboardMenu = menuTree.value.some(m =>
      restrictedPaths.some(rp => m.path === rp || m.path.startsWith(rp))
    )

    return hasDashboardMenu
  }

  // 清除菜单
  function clearMenus() {
    menuTree.value = []
    menuPaths.value = []
    localStorage.removeItem('menuTree')
    localStorage.removeItem('menuPaths')
  }

  return {
    menuTree,
    menuPaths,
    fetchUserMenus,
    extractPaths,
    getUserType,
    isAdminUser,
    hasPathPermission,
    clearMenus
  }
})
