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
        <span v-show="!isCollapse" class="logo-text">企业人力资源管理系统</span>
      </div>
      
      <!-- 动态菜单 -->
      <SidebarMenu :collapsed="isCollapse" />
    </aside>
    
    <!-- 全局设置: 保留原有菜单作为Fallback，当用户没有配置菜单时显示 -->
      <nav v-if="false" class="nav-menu">
        <router-link to="/dashboard" class="nav-item" :class="{ active: isActive('/dashboard') }">
          <span class="nav-icon">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zm0 6a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zm10 0a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z"/>
            </svg>
          </span>
          <span v-show="!isCollapse" class="nav-text">工作台</span>
        </router-link>
        
        <template v-if="hasRole('ADMIN') || hasRole('HR')">
          <div class="nav-group">
            <div class="nav-group-title" v-show="!isCollapse">员工管理</div>
            <router-link to="/employee/list" class="nav-item" :class="{ active: isActive('/employee') }">
              <span class="nav-icon">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/>
                </svg>
              </span>
              <span v-show="!isCollapse" class="nav-text">员工列表</span>
            </router-link>
          </div>
          
          <div class="nav-group">
            <div class="nav-group-title" v-show="!isCollapse">组织架构</div>
            <router-link to="/org/department" class="nav-item" :class="{ active: isActive('/org/department') }">
              <span class="nav-icon">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M4 4a2 2 0 012-2h8a2 2 0 012 2v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"/>
                </svg>
              </span>
              <span v-show="!isCollapse" class="nav-text">部门管理</span>
            </router-link>
            <router-link to="/org/position" class="nav-item" :class="{ active: isActive('/org/position') }">
              <span class="nav-icon">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M6 6V5a3 3 0 013-3h2a3 3 0 013 3v1h2a2 2 0 012 2v3.57A22.952 22.952 0 0110 13a22.95 22.95 0 01-8-1.43V8a2 2 0 012-2h2zm2-1a1 1 0 011-1h2a1 1 0 011 1v1H8V5zm1 5a1 1 0 011-1h.01a1 1 0 110 2H10a1 1 0 01-1-1z" clip-rule="evenodd"/>
                  <path d="M2 13.692V16a2 2 0 002 2h12a2 2 0 002-2v-2.308A24.974 24.974 0 0110 15c-2.796 0-5.487-.46-8-1.308z"/>
                </svg>
              </span>
              <span v-show="!isCollapse" class="nav-text">岗位管理</span>
            </router-link>
          </div>
        </template>
        
        <div class="nav-group">
          <div class="nav-group-title" v-show="!isCollapse">考勤管理</div>
          <router-link to="/attendance/clock" class="nav-item" :class="{ active: isActive('/attendance/clock') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">我的打卡</span>
          </router-link>
          <router-link to="/attendance/record" class="nav-item" :class="{ active: isActive('/attendance/record') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z"/>
                <path fill-rule="evenodd" d="M4 5a2 2 0 012-2 3 3 0 003 3h2a3 3 0 003-3 2 2 0 012 2v11a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 000 2h.01a1 1 0 100-2H7zm3 0a1 1 0 000 2h3a1 1 0 100-2h-3zm-3 4a1 1 0 100 2h.01a1 1 0 100-2H7zm3 0a1 1 0 100 2h3a1 1 0 100-2h-3z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">打卡记录</span>
          </router-link>
          <router-link v-if="hasRole('ADMIN') || hasRole('HR')" to="/attendance/location" class="nav-item" :class="{ active: isActive('/attendance/location') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">打卡地点</span>
          </router-link>
          <router-link to="/attendance/appeal" class="nav-item" :class="{ active: isActive('/attendance/appeal') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">考勤申诉</span>
          </router-link>
          <router-link v-if="hasRole('ADMIN') || hasRole('HR') || hasRole('MANAGER')" to="/attendance/appeal-approve" class="nav-item" :class="{ active: isActive('/attendance/appeal-approve') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">申诉审批</span>
          </router-link>
          <router-link v-if="hasRole('ADMIN') || hasRole('HR') || hasRole('MANAGER')" to="/attendance/calendar" class="nav-item" :class="{ active: isActive('/attendance/calendar') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">考勤日历</span>
          </router-link>
        </div>
        
        <div class="nav-group">
          <div class="nav-group-title" v-show="!isCollapse">假期管理</div>
          <router-link to="/leave/apply" class="nav-item" :class="{ active: isActive('/leave/apply') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">请假申请</span>
          </router-link>
          <router-link to="/leave/my" class="nav-item" :class="{ active: isActive('/leave/my') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M4 4a2 2 0 00-2 2v1h16V6a2 2 0 00-2-2H4z"/>
                <path fill-rule="evenodd" d="M18 9H2v5a2 2 0 002 2h12a2 2 0 002-2V9zM4 13a1 1 0 011-1h1a1 1 0 110 2H5a1 1 0 01-1-1zm5-1a1 1 0 100 2h1a1 1 0 100-2H9z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">我的假期</span>
          </router-link>
          <router-link v-if="hasRole('ADMIN') || hasRole('MANAGER') || hasRole('HR')" to="/leave/approve" class="nav-item" :class="{ active: isActive('/leave/approve') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">假期审批</span>
          </router-link>
        </div>
        
        <div class="nav-group">
          <div class="nav-group-title" v-show="!isCollapse">离职管理</div>
          <router-link to="/resignation/apply" class="nav-item" :class="{ active: isActive('/resignation/apply') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">离职申请</span>
          </router-link>
          <router-link v-if="hasRole('ADMIN') || hasRole('MANAGER') || hasRole('HR')" to="/resignation/approve" class="nav-item" :class="{ active: isActive('/resignation/approve') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">离职审批</span>
          </router-link>
        </div>
        
        <div class="nav-group">
          <div class="nav-group-title" v-show="!isCollapse">薪资管理</div>
          <router-link to="/salary/my" class="nav-item" :class="{ active: isActive('/salary/my') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4z"/>
                <path fill-rule="evenodd" d="M6 8a2 2 0 012-2h8a2 2 0 012 2v6a2 2 0 01-2 2H8a2 2 0 01-2-2V8zm6 4a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">我的薪资</span>
          </router-link>
          <router-link v-if="hasRole('ADMIN') || hasRole('HR')" to="/salary/manage" class="nav-item" :class="{ active: isActive('/salary/manage') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M4 4a2 2 0 00-2 2v8a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2H4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">薪资发放</span>
          </router-link>
        </div>
        
        <div class="nav-group" v-if="hasRole('ADMIN') || hasRole('HR') || hasRole('MANAGER')">
          <div class="nav-group-title" v-show="!isCollapse">审批管理</div>
          <router-link to="/approval/pending" class="nav-item" :class="{ active: isActive('/approval/pending') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 0v3.586L7.707 9.293a1 1 0 00-1.414 1.414l3 3a1 1 0 001.414 0l3-3a1 1 0 00-1.414-1.414L11 10.586V7z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">待我审批</span>
          </router-link>
          <router-link to="/approval/approved" class="nav-item" :class="{ active: isActive('/approval/approved') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">已审批记录</span>
          </router-link>
          <router-link v-if="hasRole('ADMIN')" to="/approval/flow" class="nav-item" :class="{ active: isActive('/approval/flow') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">流程配置</span>
          </router-link>
        </div>
        
        <router-link v-if="hasRole('ADMIN') || hasRole('HR')" to="/report/index" class="nav-item" :class="{ active: isActive('/report') }">
          <span class="nav-icon">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path d="M2 11a1 1 0 011-1h2a1 1 0 011 1v5a1 1 0 01-1 1H3a1 1 0 01-1-1v-5zM8 7a1 1 0 011-1h2a1 1 0 011 1v9a1 1 0 01-1 1H9a1 1 0 01-1-1V7zM14 4a1 1 0 011-1h2a1 1 0 011 1v12a1 1 0 01-1 1h-2a1 1 0 01-1-1V4z"/>
            </svg>
          </span>
          <span v-show="!isCollapse" class="nav-text">统计报表</span>
        </router-link>
        
        <router-link v-if="hasRole('ADMIN') || hasRole('HR')" to="/ai/resignation-prediction" class="nav-item" :class="{ active: isActive('/ai/resignation') }">
          <span class="nav-icon">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M12.316 3.051a1 1 0 01.633 1.265l-4 12a1 1 0 11-1.898-.632l4-12a1 1 0 011.265-.633zM5.707 6.293a1 1 0 010 1.414L3.414 10l2.293 2.293a1 1 0 11-1.414 1.414l-3-3a1 1 0 010-1.414l3-3a1 1 0 011.414 0zm8.586 0a1 1 0 011.414 0l3 3a1 1 0 010 1.414l-3 3a1 1 0 11-1.414-1.414L16.586 10l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd"/>
            </svg>
          </span>
          <span v-show="!isCollapse" class="nav-text">AI离职预测</span>
        </router-link>
        
        <template v-if="hasRole('ADMIN') || hasRole('HR')">
          <div class="nav-group">
            <div class="nav-group-title" v-show="!isCollapse">招聘管理</div>
            <router-link to="/recruit/job" class="nav-item" :class="{ active: isActive('/recruit/job') }">
              <span class="nav-icon">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M8 9a3 3 0 100-6 3 3 0 000 6zM8 11a6 6 0 016 6H2a6 6 0 016-6zM16 7a1 1 0 10-2 0v1h-1a1 1 0 100 2h1v1a1 1 0 102 0v-1h1a1 1 0 100-2h-1V7z"/>
                </svg>
              </span>
              <span v-show="!isCollapse" class="nav-text">招聘岗位</span>
            </router-link>
            <router-link to="/recruit/resume" class="nav-item" :class="{ active: isActive('/recruit/resume') }">
              <span class="nav-icon">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"/>
                </svg>
              </span>
              <span v-show="!isCollapse" class="nav-text">简历管理</span>
            </router-link>
          </div>
        </template>
        
        <div class="nav-group">
          <div class="nav-group-title" v-show="!isCollapse">培训管理</div>
          <router-link to="/train/my" class="nav-item" :class="{ active: isActive('/train/my') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M10.394 2.08a1 1 0 00-.788 0l-7 3a1 1 0 000 1.84L5.25 8.051a.999.999 0 01.356-.257l4-1.714a1 1 0 11.788 1.838L7.667 9.088l1.94.831a1 1 0 00.787 0l7-3a1 1 0 000-1.838l-7-3zM3.31 9.397L5 10.12v4.102a8.969 8.969 0 00-1.05-.174 1 1 0 01-.89-.89 11.115 11.115 0 01.25-3.762zM9.3 16.573A9.026 9.026 0 007 14.935v-3.957l1.818.78a3 3 0 002.364 0l5.508-2.361a11.026 11.026 0 01.25 3.762 1 1 0 01-.89.89 8.968 8.968 0 00-5.35 2.524 1 1 0 01-1.4 0zM6 18a1 1 0 001-1v-2.065a8.935 8.935 0 00-2-.712V17a1 1 0 001 1z"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">我的培训</span>
          </router-link>
          <router-link to="/train/request" class="nav-item" :class="{ active: isActive('/train/request') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M13.586 3.586a1 1 0 112.828 0l-4.586 4.586a1 1 0 01-1.414 0H4a2 2 0 00-2 2v12a2 2 0 002 2h8a1 1 0 001-1V5.414a1 1 0 00-.586-.828zM4 4h8v12H4V4zm3 5a1 1 0 011-1h4a1 1 0 110 2H7a1 1 0 01-1-1zm2 3a1 1 0 011-1h2a1 1 0 110 2H9a1 1 0 01-1-1z"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">培训需求</span>
          </router-link>
          <router-link v-if="hasRole('ADMIN') || hasRole('HR') || hasRole('MANAGER')" to="/train/list" class="nav-item" :class="{ active: route.path === '/train/list' }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M9 4.804A7.968 7.968 0 005.5 4c-1.255 0-2.443.29-3.5.804v10A7.969 7.969 0 015.5 14c1.669 0 3.218.51 4.5 1.385A7.962 7.962 0 0114.5 14c1.255 0 2.443.29 3.5.804v-10A7.968 7.968 0 0014.5 4c-1.255 0-2.443.29-3.5.804V12a1 1 0 11-2 0V4.804z"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">培训计划</span>
          </router-link>
          <router-link v-if="hasRole('ADMIN') || hasRole('HR') || hasRole('MANAGER')" to="/train/approval" class="nav-item" :class="{ active: isActive('/train/approval') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">培训审核</span>
          </router-link>
          <router-link v-if="hasRole('ADMIN') || hasRole('HR') || hasRole('MANAGER')" to="/assess/plan" class="nav-item" :class="{ active: isActive('/assess') }">
            <span class="nav-icon">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M6 2a2 2 0 00-2 2v12a2 2 0 002 2h8a2 2 0 002-2V7.414A2 2 0 0015.414 6L12 2.586A2 2 0 0010.586 2H6zm2 10a1 1 0 10-2 0v3a1 1 0 102 0v-3zm2-3a1 1 0 011 1v5a1 1 0 11-2 0v-5a1 1 0 011-1zm4-1a1 1 0 10-2 0v7a1 1 0 102 0V8z" clip-rule="evenodd"/>
              </svg>
            </span>
            <span v-show="!isCollapse" class="nav-text">考核方案</span>
          </router-link>
        </div>
        
        <template v-if="hasRole('ADMIN')">
          <div class="nav-group">
            <div class="nav-group-title" v-show="!isCollapse">系统管理</div>
            <router-link to="/system/user" class="nav-item" :class="{ active: isActive('/system/user') }">
              <span class="nav-icon">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z"/>
                </svg>
              </span>
              <span v-show="!isCollapse" class="nav-text">用户管理</span>
            </router-link>
            <router-link to="/system/role" class="nav-item" :class="{ active: isActive('/system/role') }">
              <span class="nav-icon">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M2.166 4.999A11.954 11.954 0 0010 1.944 11.954 11.954 0 0017.834 5c.11.65.166 1.32.166 2.001 0 5.225-3.34 9.67-8 11.317C5.34 16.67 2 12.225 2 7c0-.682.057-1.35.166-2.001zm11.541 3.708a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                </svg>
              </span>
              <span v-show="!isCollapse" class="nav-text">角色管理</span>
            </router-link>
            <router-link to="/system/menu" class="nav-item" :class="{ active: isActive('/system/menu') }">
              <span class="nav-icon">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M3 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 15a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z"/>
                </svg>
              </span>
              <span v-show="!isCollapse" class="nav-text">菜单管理</span>
            </router-link>
            <router-link to="/system/dict" class="nav-item" :class="{ active: isActive('/system/dict') }">
              <span class="nav-icon">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M9 4.804A7.968 7.968 0 005.5 4c-1.255 0-2.443.29-3.5.804v10A7.969 7.969 0 015.5 14c1.669 0 3.218.51 4.5 1.385A7.962 7.962 0 0114.5 14c1.255 0 2.443.29 3.5.804v-10A7.968 7.968 0 0014.5 4c-1.255 0-2.443.29-3.5.804V12a1 1 0 11-2 0V4.804z"/>
                </svg>
              </span>
              <span v-show="!isCollapse" class="nav-text">字典管理</span>
            </router-link>
            <router-link to="/system/log" class="nav-item" :class="{ active: isActive('/system/log') }">
              <span class="nav-icon">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" clip-rule="evenodd"/>
                </svg>
              </span>
              <span v-show="!isCollapse" class="nav-text">操作日志</span>
            </router-link>
          </div>
        </template>
      </nav>
    
    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 头部 -->
      <header class="header">
        <div class="header-left">
          <button class="toggle-btn" @click="isCollapse = !isCollapse">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M3 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 15a1 1 0 011-1h6a1 1 0 110 2H4a1 1 0 01-1-1z" clip-rule="evenodd"/>
            </svg>
          </button>
          <h1 class="page-title">{{ $route.meta.title || '工作台' }}</h1>
        </div>
        
        <div class="header-right">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
            <button class="notification-btn" @click="router.push('/notification')">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M10 2a6 6 0 00-6 6v3.586l-.707.707A1 1 0 004 14h12a1 1 0 00.707-1.707L16 11.586V8a6 6 0 00-6-6zM10 18a3 3 0 01-3-3h6a3 3 0 01-3 3z"/>
              </svg>
            </button>
          </el-badge>
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <div class="user-avatar">
                {{ userStore.username?.charAt(0)?.toUpperCase() || 'U' }}
              </div>
              <span class="username">{{ userStore.username }}</span>
              <svg width="16" height="16" viewBox="0 0 16 16" fill="#86868B">
                <path fill-rule="evenodd" d="M4.22 6.22a.75.75 0 011.06 0L8 8.94l2.72-2.72a.75.75 0 111.06 1.06l-3.25 3.25a.75.75 0 01-1.06 0L4.22 7.28a.75.75 0 010-1.06z" clip-rule="evenodd"/>
              </svg>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="notification">消息通知</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <!-- 内容区 -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'
import SidebarMenu from '@/components/SidebarMenu.vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const unreadCount = ref(0)

const isActive = (path) => route.path.startsWith(path)
const hasRole = (role) => userStore.hasRole(role)

const fetchUnreadCount = async () => {
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
  switch (command) {
    case 'profile':
      router.push('/profile')
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
      router.push('/login')
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
}

.nav-text {
  margin-left: 12px;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

/* 主内容区 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
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

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1D1D1F;
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
  margin: 0 8px;
  font-size: 14px;
  font-weight: 500;
  color: #1D1D1F;
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
