<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-text">
        <h1>{{ greeting }}，{{ displayName }}</h1>
        <p>{{ currentDate }}</p>
      </div>
      <div class="quick-actions">
        <button class="action-btn primary" @click="router.push('/attendance/clock')">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd" />
          </svg>
          打卡
        </button>
        <button class="action-btn" @click="router.push('/leave/apply')">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L7 5.586V3a1 1 0 00-1-1z" clip-rule="evenodd" />
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
            <path fill-rule="evenodd" d="M8 12V4m0 0l4 4m-4-4H1a7 7 0 017 7z" clip-rule="evenodd" />
          </svg>
          <span>+12%</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-value">{{ stats.departments }}</span>
          <span class="stat-label">部门数量</span>
        </div>
        <div class="stat-trend up">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
            <path fill-rule="evenodd" d="M8 12V4m0 0l4 4m-4-4H1a7 7 0 017 7z" clip-rule="evenodd" />
          </svg>
        </div>
      </div>

      <div class="stat-card clickable" @click="goToPendingLeaves">
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
                <path fill-rule="evenodd" d="M12 11a4 4 0 100-8 4 4 0 000 8zm-2 9a7 7 0 1114 0H10z" clip-rule="evenodd" />
              </svg>
            </div>
            <span>员工管理</span>
          </div>
          <div class="shortcut-item" @click="router.push('/attendance/record')">
            <div class="shortcut-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                <path fill-rule="evenodd" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67V7z" clip-rule="evenodd" />
              </svg>
            </div>
            <span>考勤记录</span>
          </div>
          <div class="shortcut-item" @click="router.push('/leave/my')">
            <div class="shortcut-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                <path fill-rule="evenodd" d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11z" clip-rule="evenodd" />
              </svg>
            </div>
            <span>我的假期</span>
          </div>
          <div class="shortcut-item" @click="router.push('/salary/my')">
            <div class="shortcut-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                <path fill-rule="evenodd" d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z" clip-rule="evenodd" />
              </svg>
            </div>
            <span>我的薪资</span>
          </div>
          <div class="shortcut-item" @click="router.push('/assess/my')">
            <div class="shortcut-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                <path fill-rule="evenodd" d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14z" clip-rule="evenodd" />
              </svg>
            </div>
            <span>我的考核</span>
          </div>
        </div>
      </div>

      <!-- 系统消息通知（使用与 NotificationView 相同的数据源） -->
      <div class="module-card notice-card">
        <div class="module-header">
          <h3>系统消息</h3>
          <span class="view-all" @click="router.push('/notification')">查看全部</span>
        </div>
        <div class="notice-list" v-loading="loading">
          <div
            v-for="item in notifications"
            :key="`${item.type}-${item.id}`"
            class="notification-item"
            :class="{ unread: item.status === 0 }"
            @click="handleClick(item)"
          >
            <div class="notification-icon" :class="item.type">
              <el-icon :size="20">
                <component :is="getIcon(item.type)" />
              </el-icon>
            </div>
            <div class="notification-content">
              <div class="notification-title">
                {{ item.title }}
                <el-tag v-if="item.status === 0" type="danger" size="small">未读</el-tag>
              </div>
              <div class="notification-desc">{{ item.content }}</div>
              <div class="notification-time">{{ formatTime(item.createTime) }}</div>
              <div class="arrow-icon"><ArrowRight /></div>
            </div>
          </div>
          <el-empty v-if="!loading && notifications.length === 0" description="暂无消息" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getNotifications, getUnreadCount, markNotificationRead, markAllNotificationRead } from '@/api/notification'
import { ElMessage } from 'element-plus'
import { Bell, Document, Calendar, Wallet, Checked, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const notifications = ref([])
const unreadCount = ref(0)

const stats = ref({
  employees: 156,
  departments: 8,
  pendingLeaves: 0,
  todayAttendance: 142
})

const attendanceRate = ref(91)

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const displayName = computed(() => {
  const info = userStore.userInfo
  return info.employeeName || info.username || info.realName || '用户'
})

const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
})

const getIcon = (type) => {
  const icons = {
    announcement: Document,
    approval: Document,
    leave: Calendar,
    train: Bell,
    salary: Wallet,
    assess: Checked
  }
  return icons[type] || Bell
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 86400000) + '小时前'
  return date.toLocaleDateString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const fetchData = async () => {
  loading.value = true
  try {
    const [listRes, countRes] = await Promise.all([getNotifications(), getUnreadCount()])
    notifications.value = listRes.data || []
    unreadCount.value = countRes.data?.totalCount || 0
    stats.value.pendingLeaves = unreadCount.value
  } catch (error) {
    console.error('获取消息列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleClick = async (item) => {
  try {
    await markNotificationRead({ type: item.type, id: item.id })
    item.status = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    if (item.type === 'approval') {
      router.push('/approval/pending')
    } else if (item.type === 'leave') {
      router.push('/leave/my')
    }
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllNotificationRead()
    ElMessage.success('已全部标记为已读')
    notifications.value = notifications.value.map((item) => ({ ...item, status: 1 }))
    unreadCount.value = 0
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const goToPendingLeaves = () => {
  router.push('/leave/approve')
}

onMounted(fetchData)
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
  letter-spacing: 0.5px;
}

.welcome-text p {
  color: #86868b;
  font-size: 15px;
  margin: 0;
}

.quick-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e5e5ea;
  background: #1d1d1f;
  color: #ffffff;
}

.action-btn:hover {
  background: #333;
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
  transition: all 0.2s;
}

.stat-card.clickable {
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: 0.5px;
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  color: #86868b;
  letter-spacing: 0.5px;
}

.stat-trend.up {
  position: absolute;
  top: 24px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #34c759;
}

.stat-badge {
  position: absolute;
  top: -2px;
  right: 0px;
  padding: 4px 8px;
  border-radius: 12px;
  background: #ef4444;
  color: #ffffff;
  font-size: 11px;
  font-weight: 500;
}

.stat-progress {
  height: 4px;
  background: #f5f5f7;
  border-radius: 2px;
  overflow: hidden;
  margin-top: 8px;
}

.progress-bar {
  height: 100%;
  background: #007aff;
  border-radius: 2px;
}

/* 功能模块 */
.modules-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
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

.shortcuts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.shortcut-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.shortcut-item:hover {
  background: #f5f5f7;
}

.shortcut-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: #1d1d1f;
}

.shortcuts-card {
  grid-column: span 1;
}

.notice-card {
  grid-column: span 3;
}

.view-all {
  font-size: 13px;
  color: #007aff;
  cursor: pointer;
}

.view-all:hover {
  text-decoration: underline;
}

.notice-list {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.notification-item:hover {
  background: #f5f5f7;
}

.notification-item.unread {
  background: rgba(237, 108, 233, 0.06);
}

.notification-icon {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: #007aff;
}

.notification-content {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.notification-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.arrow-icon {
  width: 16px;
  height: 24px;
  color: #c0c4cc;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
