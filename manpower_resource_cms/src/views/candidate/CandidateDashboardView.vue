<template>
  <div class="candidate-layout">
    <!-- 侧边栏 -->
    <aside :class="['sidebar', { collapsed: isCollapse }]">
      <div class="sidebar-header">
        <div class="logo">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
            <rect width="32" height="32" rx="8" fill="#1D1D1F"/>
            <path d="M10 16h12M16 10v12" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <span v-show="!isCollapse" class="logo-text">求职者中心</span>
      </div>

      <!-- 动态菜单 -->
      <div class="nav-group-title" v-show="!isCollapse">功能导航</div>
      <router-link
        to="/candidate/dashboard"
        class="nav-item"
        :class="{ active: isActive('/candidate/dashboard') }"
      >
        <IconComponent :icon="getIcon('首页')" class="nav-icon" />
        <span v-show="!isCollapse" class="nav-text">首页</span>
      </router-link>
      <router-link
        to="/candidate/jobs"
        class="nav-item"
        :class="{ active: isActive('/candidate/jobs') }"
      >
        <IconComponent :icon="getIcon('岗位招聘')" class="nav-icon" />
        <span v-show="!isCollapse" class="nav-text">岗位招聘</span>
      </router-link>
      <router-link
        to="/candidate/process"
        class="nav-item"
        :class="{ active: isActive('/candidate/process') }"
      >
        <IconComponent :icon="getIcon('招聘流程')" class="nav-icon" />
        <span v-show="!isCollapse" class="nav-text">招聘流程</span>
      </router-link>
      <router-link
        to="/candidate/profile"
        class="nav-item"
        :class="{ active: isActive('/candidate/profile') }"
      >
        <IconComponent :icon="getIcon('个人信息')" class="nav-icon" />
        <span v-show="!isCollapse" class="nav-text">个人信息</span>
      </router-link>

      <!-- 退出登录 -->
      <div class="nav-group">
        <div class="nav-group-title" v-show="!isCollapse">账户</div>
        <button class="nav-item logout-btn" @click="handleLogout">
          <IconComponent :icon="getIcon('退出登录')" class="nav-icon" />
          <span v-show="!isCollapse" class="nav-text">退出登录</span>
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-container">
      <header class="header">
        <div class="header-left">
          <button class="toggle-btn" @click="isCollapse = !isCollapse">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 12h18M3 6h18"/>
            </svg>
          </button>
        </div>
        <div class="header-right">
          <div class="user-info">
            <div class="user-avatar">{{ displayInitial }}</div>
            <span class="username">{{ displayName }}</span>
          </div>
        </div>
      </header>
      <div class="content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import IconComponent from '@/components/IconComponent.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const displayName = computed(() => {
  return localStorage.getItem('username') || '求职者'
})

const displayInitial = computed(() => {
  const name = displayName.value
  return name.charAt(0).toUpperCase()
})

const isActive = (path) => {
  if (!path) return false
  return route.path.startsWith(path)
}

// 菜单名称到图标的映射
const iconMap = {
  '首页': 'home',
  '岗位招聘': 'plus',
  '招聘流程': 'list',
  '个人信息': 'user',
  '退出登录': 'logout'
}

const getIcon = (menuName) => {
  // 精确匹配
  if (iconMap[menuName]) {
    return iconMap[menuName]
  }

  // 关键词匹配
  for (const [key, icon] of Object.entries(iconMap)) {
    if (menuName.includes(key)) {
      return icon
    }
  }

  // 默认图标
  return 'file'
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/candidate')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.candidate-layout {
  display: flex;
  height: 100vh;
  background: #F5F5F7;
}

/* 侧边栏 */
.sidebar {
  width: 240px;
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
  font-size: 16px;
  font-weight: 600;
  color: #1D1D1F;
  white-space: nowrap;
}

.nav-group-title {
  font-size: 11px;
  font-weight: 600;
  color: #86868B;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 16px 20px 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: #1D1D1F;
  text-decoration: none;
  margin-bottom: 2px;
  transition: background 0.15s ease;
}

.nav-item:hover {
  background: #F5F5F7;
}

.nav-item.active {
  background: #F5F5F7;
  color: #007AFF;
}

.nav-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.nav-text {
  margin-left: 12px;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.logout-btn {
  background: none;
  border: none;
  cursor: pointer;
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
  transition: background 0.15s;
}

.toggle-btn:hover {
  background: #F5F5F7;
}

.header-right {
  display: flex;
  align-items: center;
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
}
</style>
