<template>
	<nav class="nav-menu">
	    <!-- 动态菜单 -->
	    <template v-for="menu in menuTree" :key="menu.id">
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

	// 检查路径是否匹配
	const isActive = (path) => {
	  return route.path === path || route.path.startsWith(path + '/')
	}

	// 根据菜单名称获取图标
	const getIcon = (menuName) => {
	  const iconMap = {
	    '工作台': 'layout',
	    '员工管理': 'user',
	    '员工列表': 'user',
	    '员工异动': 'user',
	    '组织架构': 'building',
	    '部门管理': 'building',
	    '岗位管理': 'briefcase',
	    '考勤管理': 'clock',
	    '我的打卡': 'clock',
	    '打卡记录': 'list',
	    '打卡地点管理': 'location',
	    '我的申诉': 'message',
	    '申诉审批': 'check-circle',
	    '考勤日历': 'calendar',
	    '考勤规则': 'setting',
	    '日考勤统计': 'chart',
	    '假期管理': 'calendar',
	    '请假申请': 'edit',
	    '我的假期': 'document',
	    '假期审批': 'check-circle',
	    '假期类型管理': 'folder',
	    '假期额度管理': 'money',
	    '薪资管理': 'money',
	    '我的薪资': 'money',
	    '薪资管理': 'wallet',
	    '薪资项目': 'tag',
	    '薪资标准': 'setting',
	    '审批管理': 'document',
	    '审批流程配置': 'flow',
	    '待我审批': 'clock',
	    '已审批记录': 'check-circle',
	    '报表统计': 'chart',
	    'AI离职率预测': 'pie-chart',
	    '招聘管理': 'user-add',
	    '招聘岗位': 'briefcase',
	    '简历管理': 'document',
	    '面试管理': 'message',
	    '培训管理': 'reading',
	    '培训计划': 'calendar',
	    '我的培训': 'book',
	    '培训需求': 'edit',
	    '培训审核': 'check-circle',
	    '考核管理': 'star',
	    '考核方案': 'document',
	    '考核任务管理': 'task',
	    '我的考核': 'star',
	    '考核审批': 'check-circle',
	    '离职管理': 'logout',
	    '离职申请': 'edit',
	    '离职审批': 'check-circle',
	    '系统管理': 'setting',
	    '用户管理': 'user',
	    '角色管理': 'team',
	    '菜单管理': 'menu',
	    '字典管理': 'book',
	    '操作日志': 'document',
	    '节假日管理': 'calendar',
	    '系统配置': 'setting',
	    '审批日志': 'document',
	    '个人中心': 'user',
	    '消息通知': 'bell'
	  }
	  return iconMap[menuName] || 'menu'
	}

	// 过滤子菜单（过滤掉工作台）
	const getFilteredChildren = (children) => {
			if (!children) return []
			return children.filter(child => child.menuName !== '工作台')
		}

	// 过滤后的菜单树（普通员工隐藏工作台）
	const filteredMenuTree = computed(() => menuTree.value)
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
