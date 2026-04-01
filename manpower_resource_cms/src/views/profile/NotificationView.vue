<template>
  <div class="notification-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消息通知</span>
          <div class="header-right">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0">
              <el-button type="primary" link>未读消息</el-button>
            </el-badge>
            <el-button
              v-if="notifications.length > 0"
              type="primary"
              link
              :disabled="unreadCount === 0"
              @click.stop="handleMarkAllRead"
            >
              全部已读
            </el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="!loading && notifications.length === 0" description="暂无消息" />

      <div class="notification-list" v-else v-loading="loading">
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
          </div>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell, Document, Calendar, Wallet, Checked, ArrowRight } from '@element-plus/icons-vue'
import {
  getNotifications,
  getUnreadCount,
  markNotificationRead,
  markAllNotificationRead,
} from '@/api/notification'

const router = useRouter()
const loading = ref(false)
const notifications = ref([])
const unreadCount = ref(0)

const getIcon = (type) => {
  const icons = {
    approval: markRaw(Document),
    leave: markRaw(Calendar),
    train: markRaw(Bell),
    salary: markRaw(Wallet),
    assess: markRaw(Checked),
  }
  return icons[type] || markRaw(Bell)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return date.toLocaleDateString()
}

const markRead = async (item) => {
  if (item.status !== 0) return
  await markNotificationRead({ type: item.type, id: item.id })
  item.status = 1
  unreadCount.value = Math.max(0, unreadCount.value - 1)
}

const handleClick = async (item) => {
  try {
    await markRead(item)
  } catch (error) {
    console.error(error)
  }

  switch (item.type) {
    case 'approval':
      router.push('/approval/pending')
      break
    case 'leave':
      router.push('/leave/my')
      break
    case 'train':
      router.push('/train/my')
      break
    case 'salary':
      router.push('/salary/my')
      break
    case 'assess':
      router.push('/assess/my')
      break
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllNotificationRead()
    notifications.value = notifications.value.map((item) => ({ ...item, status: 1 }))
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    console.error(error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const [listRes, countRes] = await Promise.all([getNotifications(), getUnreadCount()])
    notifications.value = listRes.data || []
    unreadCount.value = countRes.data?.totalCount || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.notification-list {
  display: flex;
  flex-direction: column;
}
.notification-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}
.notification-item:hover {
  background: #f5f7fa;
}
.notification-item.unread {
  background: #ecf5ff;
}
.notification-item.unread:hover {
  background: #d9ecff;
}
.notification-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: #fff;
}
.notification-icon.approval {
  background: linear-gradient(135deg, #667eea, #764ba2);
}
.notification-icon.leave {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}
.notification-icon.train {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}
.notification-icon.salary {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}
.notification-icon.assess {
  background: linear-gradient(135deg, #f6d365, #fda085);
}
.notification-content {
  flex: 1;
}
.notification-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}
.notification-desc {
  font-size: 13px;
  color: #606266;
  margin-top: 4px;
}
.notification-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.arrow-icon {
  color: #c0c4cc;
}
</style>
