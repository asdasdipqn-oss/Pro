<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-text">
        <h1>{{ greeting }}，{{ userStore.username }}</h1>
        <p>{{ currentDate }}</p>
      </div>
      <div class="quick-actions">
        <button class="action-btn primary" @click="router.push('/attendance/clock')">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <path
              fill-rule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z"
              clip-rule="evenodd"
            />
          </svg>
          打卡
        </button>
        <button class="action-btn" @click="router.push('/leave/apply')">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <path
              fill-rule="evenodd"
              d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z"
              clip-rule="evenodd"
            />
          </svg>
          请假
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-value">{{ stats.employees }}</span>
          <span class="stat-label">员工总数</span>
        </div>
        <div class="stat-trend up">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
            <path d="M8 12V4M8 4l4 4M8 4L4 8" />
          </svg>
          <span>+12%</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-value">{{ stats.departments }}</span>
          <span class="stat-label">部门数量</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-value">{{ stats.pendingLeaves }}</span>
          <span class="stat-label">待审批假期</span>
        </div>
        <div class="stat-badge" v-if="stats.pendingLeaves > 0">需处理</div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-value">{{ stats.todayAttendance }}</span>
          <span class="stat-label">今日出勤</span>
        </div>
        <div class="stat-progress">
          <div class="progress-bar" :style="{ width: attendanceRate + '%' }"></div>
        </div>
      </div>
    </div>

    <!-- 功能模块 -->
    <div class="modules-section">
      <div class="module-card shortcuts-card">
        <div class="module-header">
          <h3>快捷入口</h3>
        </div>
        <div class="shortcuts-grid">
          <div class="shortcut-item" @click="router.push('/employee/list')">
            <div class="shortcut-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                <path
                  fill-rule="evenodd"
                  d="M12 11a4 4 0 100-8 4 4 0 000 8zm-7 9a7 7 0 1114 0H5z"
                  clip-rule="evenodd"
                />
              </svg>
            </div>
            <span>员工管理</span>
          </div>
          <div class="shortcut-item" @click="router.push('/attendance/record')">
            <div class="shortcut-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                <path
                  fill-rule="evenodd"
                  d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10zm1-14a1 1 0 10-2 0v5a1 1 0 00.293.707l3.5 3.5a1 1 0 001.414-1.414L13 12.586V8z"
                  clip-rule="evenodd"
                />
              </svg>
            </div>
            <span>考勤记录</span>
          </div>
          <div class="shortcut-item" @click="router.push('/leave/my')">
            <div class="shortcut-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                <path
                  fill-rule="evenodd"
                  d="M6 2a1 1 0 00-1 1v2H4a2 2 0 00-2 2v14a2 2 0 002 2h16a2 2 0 002-2V7a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v2H7V3a1 1 0 00-1-1zm0 6a1 1 0 000 2h12a1 1 0 100-2H6z"
                  clip-rule="evenodd"
                />
              </svg>
            </div>
            <span>我的假期</span>
          </div>
          <div class="shortcut-item" @click="router.push('/salary/my')">
            <div class="shortcut-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                <path d="M4 4a2 2 0 00-2 2v6a2 2 0 002 2v-2h14V6a2 2 0 00-2-2H4z" />
                <path
                  fill-rule="evenodd"
                  d="M6 10a2 2 0 012-2h12a2 2 0 012 2v8a2 2 0 01-2 2H8a2 2 0 01-2-2v-8zm8 4a2 2 0 100-4 2 2 0 000 4z"
                  clip-rule="evenodd"
                />
              </svg>
            </div>
            <span>我的薪资</span>
          </div>
        </div>
      </div>

      <div class="module-card notice-card">
        <div class="module-header">
          <h3>系统公告</h3>
          <span class="view-all">查看全部</span>
        </div>
        <div class="notice-list">
          <div class="notice-empty">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="#C7C7CC">
              <path
                d="M24 8v2M24 38v2M8 24H6M42 24h2M12.4 12.4l-1.4-1.4M37 11l-1.4 1.4M12.4 35.6l-1.4 1.4M37 37l-1.4-1.4"
              />
              <circle cx="24" cy="24" r="8" />
            </svg>
            <p>暂无公告</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getDashboardStats } from '@/api/dashboard'

const router = useRouter()
const userStore = useUserStore()

const stats = ref({
  employees: 0,
  departments: 0,
  pendingLeaves: 0,
  todayAttendance: 0,
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const currentDate = computed(() => {
  const now = new Date()
  const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
  return now.toLocaleDateString('zh-CN', options)
})

const attendanceRate = computed(() => {
  return stats.value.employees > 0
    ? Math.round((stats.value.todayAttendance / stats.value.employees) * 100)
    : 0
})

// 获取统计数据
const fetchStats = async () => {
  try {
    const res = await getDashboardStats()
    if (res.data) {
      stats.value = {
        employees: res.data.employees || 0,
        departments: res.data.departments || 0,
        pendingLeaves: res.data.pendingLeaves || 0,
        todayAttendance: res.data.todayAttendance || 0,
      }
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

/* 欢迎区域 */
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.welcome-text h1 {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 4px 0;
  letter-spacing: -0.5px;
}

.welcome-text p {
  font-size: 15px;
  color: #86868b;
  margin: 0;
}

.quick-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e5e5ea;
  background: #ffffff;
  color: #1d1d1f;
}

.action-btn:hover {
  background: #f5f5f7;
}

.action-btn.primary {
  background: #1d1d1f;
  border-color: #1d1d1f;
  color: #ffffff;
}

.action-btn.primary:hover {
  background: #000000;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  position: relative;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -1px;
}

.stat-label {
  font-size: 13px;
  color: #86868b;
  margin-top: 4px;
}

.stat-trend {
  position: absolute;
  top: 24px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
}

.stat-trend.up {
  color: #34c759;
}

.stat-badge {
  position: absolute;
  top: 24px;
  right: 24px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
  background: rgba(255, 149, 0, 0.12);
  color: #ff9500;
}

.stat-progress {
  margin-top: 12px;
  height: 4px;
  background: #f5f5f7;
  border-radius: 2px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: #1d1d1f;
  border-radius: 2px;
  transition: width 0.3s ease;
}

/* 功能模块 */
.modules-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.module-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
}

.module-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.module-header h3 {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.view-all {
  font-size: 13px;
  color: #007aff;
  cursor: pointer;
}

.view-all:hover {
  text-decoration: underline;
}

/* 快捷入口 */
.shortcuts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.shortcut-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  background: #f5f5f7;
  cursor: pointer;
  transition: all 0.2s;
}

.shortcut-item:hover {
  background: #e5e5ea;
}

.shortcut-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1d1d1f;
}

.shortcut-item span {
  font-size: 14px;
  font-weight: 500;
  color: #1d1d1f;
}

/* 公告 */
.notice-list {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notice-empty {
  text-align: center;
}

.notice-empty p {
  margin-top: 12px;
  font-size: 14px;
  color: #86868b;
}
</style>
