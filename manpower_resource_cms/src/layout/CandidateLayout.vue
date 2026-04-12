<template>
  <div class="candidate-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
            <rect width="32" height="32" rx="8" fill="#1D1D1F"/>
            <path d="M10 16h12M16 10v12" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <span class="logo-text">求职者中心</span>
      </div>

      <nav class="nav-menu">
        <router-link to="/candidate/jobs" class="nav-item" :class="{ active: isActive('/candidate/jobs') }">
          <IconComponent icon="briefcase" class="nav-icon" />
          <span class="nav-text">招聘岗位</span>
        </router-link>
        <router-link to="/candidate/process" class="nav-item" :class="{ active: isActive('/candidate/process') }">
          <IconComponent icon="flow" class="nav-icon" />
          <span class="nav-text">招聘流程</span>
        </router-link>
        <router-link to="/candidate/profile" class="nav-item" :class="{ active: isActive('/candidate/profile') }">
          <IconComponent icon="user" class="nav-icon" />
          <span class="nav-text">个人信息</span>
        </router-link>
      </nav>
    </aside>

    <div class="main-container">
      <header class="top-header">
        <div class="header-title">{{ route.meta.title || '' }}</div>
        <div class="header-right">
          <div class="user-avatar">{{ (candidateStore.username || 'U').charAt(0).toUpperCase() }}</div>
          <span class="username">{{ candidateStore.username }}</span>
          <el-button size="small" @click="handleLogout" class="logout-btn">退出登录</el-button>
        </div>
      </header>
      <div class="content">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useCandidateStore } from '@/stores/candidate'
import IconComponent from '@/components/IconComponent.vue'

const route = useRoute()
const router = useRouter()
const candidateStore = useCandidateStore()

const isActive = (path) => {
  return route.path === path
}

const handleLogout = () => {
  candidateStore.logout()
  router.push('/candidate')
}
</script>

<style scoped>
.candidate-layout {
  display: flex;
  min-height: 100vh;
  background: #F5F5F7;
}

.sidebar {
  width: 220px;
  background: #FFFFFF;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #E5E5EA;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #F0F0F0;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #1D1D1F;
}

.nav-menu {
  flex: 1;
  padding: 12px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  color: #1D1D1F;
  text-decoration: none;
  margin-bottom: 4px;
  transition: all 0.15s ease;
}

.nav-item:hover {
  background: #F5F5F7;
}

.nav-item.active {
  background: #007AFF;
  color: #FFFFFF;
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
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.top-header {
  height: 56px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #FFFFFF;
  border-bottom: 1px solid #E5E5EA;
  flex-shrink: 0;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1D1D1F;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #007AFF;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #1D1D1F;
}

.logout-btn {
  margin-left: 4px;
}

.content {
  flex: 1;
  overflow-y: auto;
}
</style>
