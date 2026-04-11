<template>
  <div class="candidate-dashboard">
    <div class="header">
      <h1>欢迎，{{ displayName }}！</h1>
      <p>开始您的求职之旅</p>
    </div>

    <div class="cards-container">
      <div class="nav-card" @click="goToProfile">
        <div class="card-icon">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <circle cx="24" cy="24" r="22" fill="#007AFF"/>
            <path d="M24 18a6 6 0 0 0 0 0-6 6 6 0 1 0 3-3 3-3v6" stroke="#fff" stroke-width="2"/>
          </svg>
        </div>
        <h2>个人信息</h2>
        <p>完善您的个人信息</p>
      </div>

      <div class="nav-card" @click="goToJobs">
        <div class="card-icon">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <circle cx="24" cy="24" r="22" fill="#34C759"/>
            <path d="M16 16h16M24 16v8" stroke="#fff" stroke-width="2"/>
          </svg>
        </div>
        <h2>岗位招聘</h2>
        <p>查看并申请心仪的岗位</p>
      </div>

      <div class="nav-card" @click="goToProcess">
        <div class="card-icon">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <circle cx="24" cy="24" r="22" fill="#FF9500"/>
            <path d="M12 24l3-3 4-4h3v2H12m3-3-4v2H20v6h6v-6h-6" stroke="#fff" stroke-width="2"/>
          </svg>
        </div>
        <h2>招聘流程</h2>
        <p>查看您的应聘进度和面试安排</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { computed } from 'vue'

const router = useRouter()

const displayName = computed(() => {
  return localStorage.getItem('username') || '求职者'
})

const goToProfile = () => {
  router.push('/candidate/profile')
}

const goToJobs = () => {
  router.push('/candidate/jobs')
}

const goToProcess = () => {
  router.push('/candidate/process')
}
</script>

<style scoped>
.candidate-dashboard {
  min-height: 100vh;
  background: #F5F5F7;
  padding: 40px 20px;
}

.header {
  text-align: center;
  margin-bottom: 60px;
}

.header h1 {
  font-size: 36px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 12px;
}

.header p {
  font-size: 16px;
  color: #86868B;
}

.cards-container {
  max-width: 900px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 60px;
}

.nav-card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.nav-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.card-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 20px;
  border-radius: 16px;
}

.card-icon svg {
  width: 100%;
  height: 100%;
}

.nav-card h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 8px;
}

.nav-card p {
  font-size: 14px;
  color: #86868B;
}
</style>
