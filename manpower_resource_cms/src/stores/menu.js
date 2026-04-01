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

  // 检查是否有某个路径的权限
  function hasMenuPermission(path) {
    // 如果菜单为空，表示还没获取到，默认放行
    if (menuPaths.value.length === 0) return true
    // 检查路径是否在菜单中
    return menuPaths.value.some(p => path.startsWith(p) || p.startsWith(path))
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
    hasMenuPermission,
    clearMenus
  }
})
