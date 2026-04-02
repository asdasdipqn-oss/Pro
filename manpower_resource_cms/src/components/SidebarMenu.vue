<template>
<nav class="nav-menu">
    <!-- 动态菜单 -->
    <template v-for="menu in filteredMenuTree" :key="menu.id">
      <!-- 目录类型（有子菜单） -->
      <div v-if="menu.menuType === 1 && menu.children && menu.children.length > 0" class="nav-group">
        <div class="nav-group-title" v-show="!collapsed">{{ menu.menuName }}</div>
        <template v-for="child in menu.children" :key="child.id">
          <router-link
            v-if="child.menuType === 2 && child.visible === 1"
            :to="child.path"
            class="nav-item"
            :class="{ active: isActive(child.path) }"
          >
            <IconComponent :icon="getIcon(child.menuName)" class="nav-icon" />
            <span v-show="!collapsed" class="nav-text">{{ child.menuName }}</span>
          </router-link>
        </template>
      </div>

      <!-- 菜单类型（无子菜单） -->
      <router-link
        v-else-if="menu.menuType === 2 && menu.visible === 1"
        :to="menu.path"
        class="nav-item"
        :class="{ active: isActive(menu.path) }"
      >
        <IconComponent :icon="getIcon(menu.menuName)" class="nav-icon" />
        <span v-show="!collapsed" class="nav-text">{{ menu.menuName }}</span>
      </router-link>
    </template>
    
    <!-- 个人中心（所有人可见） -->
    <div class="nav-group">
      <div class="nav-group-title" v-show="!collapsed">个人</div>
      <router-link to="/profile" class="nav-item" :class="{ active: isActive('/profile') }">
        <IconComponent icon="user" class="nav-icon" />
        <span v-show="!collapsed" class="nav-text">个人中心</span>
      </router-link>
      <router-link to="/notification" class="nav-item" :class="{ active: isActive('/notification') }">
        <IconComponent icon="bell" class="nav-icon" />
        <span v-show="!collapsed" class="nav-text">消息通知</span>
      </router-link>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useMenuStore } from '@/stores/menu'
import { useUserStore } from '@/stores/user'
import IconComponent from './IconComponent.vue'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const menuStore = useMenuStore()
const userStore = useUserStore()

const menuTree = computed(() => menuStore.menuTree)

// 过滤菜单：管理员显示所有，普通员工隐藏工作台
const filteredMenuTree = computed(() => {
  const isAdmin = userStore.roles?.includes('ADMIN')
  if (isAdmin) {
    return menuTree.value
  }

  // 递归过滤掉工作台菜单
  const filterMenu = (menus) => {
    return menus
      .filter(menu => {
        // 隐藏工作台菜单
        if (menu.path === '/dashboard') {
          return false
        }
        // 隐藏工作台子菜单
        if (menu.path?.startsWith('/dashboard')) {
          return false
        }
        return true
      })
      .map(menu => {
        // 如果有子菜单，递归过滤
        if (menu.children && menu.children.length > 0) {
          const filteredChildren = filterMenu(menu.children)
          return {
            ...menu,
            children: filteredChildren
          }
        }
        return menu
      })
      .filter(menu => {
        // 如果是目录类型且过滤后没有子菜单，也隐藏该目录
        return !(menu.menuType === 1 && (!menu.children || menu.children.length === 0))
      })
  }

  return filterMenu(menuTree.value)
})

const isActive = (path) => {
  if (!path) return false
  return route.path.startsWith(path)
}

// 菜单名称到图标的映射
const iconMap = {
  '工作台': 'chart',
  '员工': 'user',
  '员工列表': 'user',
  '员工管理': 'user',
  '部门': 'building',
  '部门管理': 'building',
  '岗位': 'briefcase',
  '岗位管理': 'briefcase',
  '我的打卡': 'clock',
  '打卡': 'clock',
  '考勤': 'calendar',
  '打卡记录': 'file',
  '打卡地点': 'location',
  '考勤申诉': 'bell',
  '申诉审批': 'check',
  '考勤日历': 'calendar',
  '请假申请': 'calendar',
  '请假': 'calendar',
  '假期': 'calendar',
  '我的假期': 'calendar',
  '假期审批': 'check',
  '离职申请': 'logout',
  '离职': 'logout',
  '离职审批': 'check',
  '我的薪资': 'money',
  '薪资': 'money',
  '薪资管理': 'money',
  '薪资发放': 'money',
  '待我审批': 'check',
  '已审批记录': 'check',
  '流程配置': 'setting',
  '审批': 'check',
  '统计报表': 'chart',
  '统计': 'chart',
  '报表': 'chart',
  'AI离职预测': 'chart',
  '招聘岗位': 'plus',
  '招聘': 'plus',
  '简历管理': 'file',
  '简历': 'file',
  '我的培训': 'play',
  '培训': 'play',
  '培训计划': 'play',
  '考核方案': 'book',
  '考核': 'book',
  '用户管理': 'user',
  '角色管理': 'setting',
  '菜单管理': 'list',
  '字典管理': 'list',
  '操作日志': 'file',
  '日志': 'file',
  '系统管理': 'setting',
  '系统': 'setting',
  '设置': 'setting'
}

const getIcon = (menuName) => {
  // 精确匹配
  if (iconMap[menuName]) {
    return iconMap[menuName]
  }
  
  // 关键词匹配
  for (const [key, icon] of Object.entries(iconMap)) {
    if (key !== '工作台' && menuName.includes(key)) {
      return icon
    }
  }
  
  // 默认图标
  return 'file'
}
</script>

<style scoped>
.nav-menu {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.nav-group {
  margin-bottom: 8px;
}

.nav-group-title {
  font-size: 11px;
  font-weight: 600;
  color: #86868B;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 16px 12px 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  color: #1D1D1F;
  text-decoration: none;
  margin-bottom: 2px;
  transition: all 0.15s ease;
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
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-text {
  margin-left: 12px;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}
</style>
