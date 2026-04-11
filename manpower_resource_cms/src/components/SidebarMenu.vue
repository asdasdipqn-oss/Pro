<template>
	<nav class="nav-menu">
	    <!-- 求职者专用菜单 -->
	    <template v-if="isCandidate">
	      <div class="nav-group">
	        <div class="nav-group-title" v-show="!collapsed">招聘</div>
	        <router-link to="/candidate/jobs" class="nav-item" :class="{ active: isActive('/candidate/jobs') }">
	          <IconComponent icon="briefcase" class="nav-icon" />
	          <span v-show="!collapsed" class="nav-text">岗位招聘</span>
	        </router-link>
	        <router-link to="/candidate/process" class="nav-item" :class="{ active: isActive('/candidate/process') }">
	          <IconComponent icon="chart" class="nav-icon" />
	          <span v-show="!collapsed" class="nav-text">招聘流程</span>
	        </router-link>
	      </div>

	      <div class="nav-group">
	        <div class="nav-group-title" v-show="!collapsed">个人</div>
	        <router-link to="/candidate/profile" class="nav-item" :class="{ active: isActive('/candidate/profile') }">
	          <IconComponent icon="user" class="nav-icon" />
	          <span v-show="!collapsed" class="nav-text">个人信息</span>
	        </router-link>
	      </div>
	    </template>

	    <!-- HR员工动态菜单 -->
	    <template v-else>
	    <!-- 动态菜单 -->
	    <template v-for="menu in filteredMenuTree" :key="menu.id">
	      <!-- 目录类型（有子菜单） -->
	      <div v-if="menu.menuType === 1 && menu.children && menu.children.length > 0" class="nav-group">
	        <div class="nav-group-title" v-show="!collapsed">{{ menu.menuName }}</div>
	        <template v-for="child in getFilteredChildren(menu.children)" :key="child.id">
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
    </template>
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

	// 判断是否是求职者用户
	const isCandidate = computed(() => {
			const userType = localStorage.getItem('userType')
			console.log('[SidebarMenu] User type:', userType, 'isCandidate:', userType === 'candidate')
			return userType === 'candidate'
		})

	// 判断是否是管理员
	const isAdmin = computed(() => {
			const roles = userStore.roles || []
			console.log('[SidebarMenu] isAdmin check - roles:', roles)
			console.log('[SidebarMenu] isAdmin check - roles JSON:', JSON.stringify(roles))
			console.log('[SidebarMenu] isAdmin check - roles type:', typeof roles)
			const result = roles.some(role =>
				role && role.toUpperCase() && ['ADMIN', 'SUPER_ADMIN', 'admin', 'super_admin'].includes(role.toUpperCase())
			)
			console.log('[SidebarMenu] isAdmin result:', result)
			return result
		})

	const menuTree = computed(() => menuStore.menuTree)

	// 过滤子菜单（过滤掉工作台）
	const getFilteredChildren = (children) => {
			if (!children) return []
			return children.filter(child => child.menuName !== '工作台')
		}

	// 过滤后的菜单树（HR或admin显示所有菜单，普通员工隐藏工作台和招聘管理）
	const filteredMenuTree = computed(() => {
			const roles = userStore.roles || []
			console.log('[SidebarMenu] FilteredMenuTree - userStore.roles:', roles)
			console.log('[SidebarMenu] FilteredMenuTree - userStore.roles JSON:', JSON.stringify(roles))
			console.log('[SidebarMenu] FilteredMenuTree - isAdmin:', isAdmin.value)
			const isAdminOrHr = isAdmin.value || roles.some(role =>
				role && role.toUpperCase() && ['ADMIN', 'SUPER_ADMIN', 'admin', 'super_admin', 'HR'].includes(role.toUpperCase())
			)
			console.log('[SidebarMenu] FilteredMenuTree - isAdminOrHr:', isAdminOrHr)
			console.log('[SidebarMenu] FilteredMenuTree - includes HR (case-sensitive):', roles.includes('HR'))
			console.log('[SidebarMenu] FilteredMenuTree - includes HR (case-insensitive):',
				roles.some(r => r && r.toUpperCase() === 'HR')
			)
			if (isAdminOrHr) {
				console.log('[SidebarMenu] Showing all menus (HR or admin)')
				return menuTree.value
			}
			// 普通员工：过滤掉工作台和招聘管理菜单
			const filteredMenus = menuTree.value.filter(menu => {
				// 过滤掉名为"工作台"或"招聘管理"的顶级菜单
				if (menu.menuName === '工作台' || menu.menuName === '招聘管理') {
					return false
				}
				return true
			})
			console.log('[SidebarMenu] Filtered menus (regular employee):', filteredMenus.map(m => m.menuName))
			return filteredMenus
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
	  '培训需求': 'edit',
	  '培训': 'play',
	  '培训计划': 'play',
	  '培训审核': 'check',
	  '考核方案': 'book',
	  '考核': 'book',
	  '用户管理': 'user',
	  '角色管理': 'setting',
	  '菜单管理': 'list',
	  '字典管理': 'list',
	  '操作日志': 'file',
	  '日志': 'file',
	  '系统管理': 'setting',
	  '设置': 'setting',
	  '首页': 'home',
	  '个人信息': 'user'
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
</script>

<style scoped>
	.nav-menu {
		  flex:1;
		  padding:16px 12px;
		  overflow-y: auto;
	}

	.nav-group {
		  margin-bottom:8px;
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
