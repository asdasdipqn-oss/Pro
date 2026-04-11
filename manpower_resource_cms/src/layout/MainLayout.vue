<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <aside :class="['sidebar', { collapsed: isCollapse }]">
      <div class="sidebar-header">
        <div class="logo">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
            <rect width="32" height="32" rx="8" fill="#1D1D1F"/>
            <path d="M10 16h12M16 10v12" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <span v-show="!isCollapse" class="logo-text">{{ logoText }}</span>
      </div>

      <!-- 动态菜单 -->
      <SidebarMenu :collapsed="isCollapse" />
    </aside>

    <!-- 主内容区 -->
    <main class="main-container">
      <header class="header">
        <div class="header-left">
          <button class="toggle-btn" @click="isCollapse = !isCollapse">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 12h18M3 6h18M3 18h18"/>
            </svg>
          </button>
        </div>
        <div class="header-right">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
            <button class="notification-btn" @click="handleCommand('notification')">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
              </svg>
            </button>
          </el-badge>
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-info">
              <div class="user-avatar">{{ displayInitial }}</div>
              <span class="username">{{ displayName }}</span>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M6 9l6 6 6-6"/>
              </svg>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><user /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="notification">
                  <el-icon><bell /></el-icon>
                  消息通知
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><switch-button /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <div class="content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage, ElDropdown, ElDropdownMenu, ElDropdownItem, ElBadge, ElIcon } from 'element-plus'
import { User, Bell, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'
import SidebarMenu from '@/components/SidebarMenu.vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()

const isCollapse = ref(false)
const unreadCount = ref(0)

// 根据用户类型显示不同的logo文字
const logoText = computed(() => {
  const userType = localStorage.getItem('userType')
  return userType === 'candidate' ? '求职者中心' : '企业人力资源管理系统'
})

// 获取显示的用户名
const displayName = computed(() => {
  const userType = localStorage.getItem('userType')
  if (userType === 'candidate') {
    return localStorage.getItem('username') || '求职者'
  }
  const info = userStore.userInfo
  return info.employeeName || info.username || info.realName || '用户'
})

// 获取用户名首字母
const displayInitial = computed(() => {
  const name = displayName.value
  return name.charAt(0).toUpperCase()
})

const isActive = (path) => route.path.startsWith(path)
const hasRole = (role) => userStore.hasRole(role)

const fetchUnreadCount = async () => {
  const userType = localStorage.getItem('userType')
  if (userType === 'candidate') {
    // 求职者不获取通知数量
    return
  }
  try {
    const res = await request.get('/notification/unread-count')
    unreadCount.value = res.data?.totalCount || 0
  } catch (error) {
    // 忽略请求取消错误
    if (error.message !== 'cancel') {
      console.error('获取求未读数量失败:', error)
    }
  }
}

const handleCommand = async (command) => {
  const userType = localStorage.getItem('userType')
  switch (command) {
    case 'profile':
      if (userType === 'candidate') {
        router.push('/candidate/profile')
      } else {
        router.push('/profile')
      }
      break
    case 'notification':
      router.push('/notification')
      break
    case 'logout':
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      userStore.logout()
      ElMessage.success('已退出登录')
      if (userType === 'candidate') {
        router.push('/candidate')
      } else {
        router.push('/login')
      }
      break
  }
}

onMounted(() => {
  fetchUnreadCount()
  // 每分钟刷新一次未读数
  setInterval(fetchUnreadCount, 60000)
})
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  background: #F5F5F7;
}

/* 侧边栏 */
.sidebar {
  width: 260px;
  background: #FFFFFF;
  border-right: 1px solid #E5E5EA;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
}

.sidebar.collapsed {
  width: 72px;
}

.sidebar-header {
  height: 64px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #E5E5EA;
}

.logo {
  flex-shrink: 0;
}

.logo-text {
  margin-left: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #1D1D1F;
  white-space: nowrap;
}

/* 主内容区 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.header {
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #FFFFFF;
  border-bottom: 1px solid #E5E5EA;
}

.header-left {
  display: flex;
  align-items: center;
}

.toggle-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  color: #1D1D1F;
  margin-right: 16px;
  transition: background 0.15s;
}

.toggle-btn:hover {
  background: #F5F5F7;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-badge {
  margin-right: 8px;
}

.notification-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  color: #1D1D1F;
  transition: background 0.15s;
}

.notification-btn:hover {
  background: #F5F5F7;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 20px;
  cursor: pointer;
  transition: background 0.15s;
}

.user-info:hover {
  background: #F5F5F7;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #1D1D1F;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.username {
  margin-left: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #1D1D1F;
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: calc(100vh - 64px);
}
</style>
