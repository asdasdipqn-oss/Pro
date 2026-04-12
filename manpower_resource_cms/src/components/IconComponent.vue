<template>
  <svg
    :class="['icon', `icon-${icon}`]"
    viewBox="0 0 24 24"
    fill="currentColor"
    xmlns="http://www.w3.org/2000/svg"
  >
    <!-- 用户图标 -->
    <circle v-if="icon === 'user'" cx="12" cy="8" r="4" />
    <path v-if="icon === 'user'" d="M12 14c-6 0-8 3-8 3v6h16v-6s-2-3-8-3z" />

    <!-- 钟表图标 -->
    <circle v-if="icon === 'clock'" cx="12" cy="12" r="10" />
    <line v-if="icon === 'clock'" x1="12" y1="6" x2="12" y2="12" stroke="white" stroke-width="2" />
    <line v-if="icon === 'clock'" x1="12" y1="12" x2="15" y2="15" stroke="white" stroke-width="2" />

    <!-- 日历图标 -->
    <rect v-if="icon === 'calendar'" x="3" y="4" width="18" height="18" rx="2" />
    <line v-if="icon === 'calendar'" x1="16" y1="2" x2="16" y2="6" stroke="white" stroke-width="2" />
    <line v-if="icon === 'calendar'" x1="8" y1="2" x2="8" y2="6" stroke="white" stroke-width="2" />
    <line v-if="icon === 'calendar'" x1="3" y1="10" x2="21" y2="10" stroke="white" stroke-width="2" />

    <!-- 文件图标 -->
    <path
      v-if="icon === 'file'"
      d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"
    />
    <polyline v-if="icon === 'file'" points="14 2 14 8 20 8" stroke="white" stroke-width="2" fill="none" />

    <!-- 建筑物图标 -->
    <rect v-if="icon === 'building'" x="4" y="2" width="16" height="20" rx="1" />
    <line v-if="icon === 'building'" x1="4" y1="6" x2="20" y2="6" stroke="white" stroke-width="2" />
    <line v-if="icon === 'building'" x1="4" y1="10" x2="20" y2="10" stroke="white" stroke-width="2" />
    <line v-if="icon === 'building'" x1="4" y1="14" x2="20" y2="14" stroke="white" stroke-width="2" />
    <line v-if="icon === 'building'" x1="4" y1="18" x2="20" y2="18" stroke="white" stroke-width="2" />

    <!-- 公文包/岗位图标 -->
    <rect v-if="icon === 'briefcase'" x="2" y="7" width="20" height="14" rx="2" />
    <path v-if="icon === 'briefcase'" d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2" />

    <!-- 金钱图标 -->
    <circle v-if="icon === 'money'" cx="12" cy="12" r="10" />
    <path v-if="icon === 'money'" d="M12 8v8M9 11h6" stroke="white" stroke-width="2" fill="none" />

    <!-- 复选标记图标 -->
    <polyline v-if="icon === 'check'" points="20 6 9 17 4 12" stroke="currentColor" stroke-width="2" fill="none" />

    <!-- 设置图标 -->
    <circle v-if="icon === 'setting'" cx="12" cy="12" r="3" />
    <path
      v-if="icon === 'setting'"
      d="M12 1v6m0 6v6M4.22 4.22l4.24 4.24m2.12 2.12l4.24 4.24M1 12h6m6 0h6m-17.78 7.78l4.24-4.24m2.12-2.12l4.24-4.24"
      stroke="currentColor"
      stroke-width="2"
      fill="none"
    />

    <!-- 列表图标 -->
    <line v-if="icon === 'list'" x1="8" y1="6" x2="21" y2="6" stroke="currentColor" stroke-width="2" />
    <line v-if="icon === 'list'" x1="8" y1="12" x2="21" y2="12" stroke="currentColor" stroke-width="2" />
    <line v-if="icon === 'list'" x1="8" y1="18" x2="21" y2="18" stroke="currentColor" stroke-width="2" />
    <line v-if="icon === 'list'" x1="3" y1="6" x2="3.01" y2="6" stroke="currentColor" stroke-width="2" />
    <line v-if="icon === 'list'" x1="3" y1="12" x2="3.01" y2="12" stroke="currentColor" stroke-width="2" />
    <line v-if="icon === 'list'" x1="3" y1="18" x2="3.01" y2="18" stroke="currentColor" stroke-width="2" />

    <!-- 地点/位置图标 -->
    <path v-if="icon === 'location'" d="M12 2C7.58 2 4 5.58 4 10c0 5.25 8 13 8 13s8-7.75 8-13c0-4.42-3.58-8-8-8z" />
    <circle v-if="icon === 'location'" cx="12" cy="10" r="2.5" fill="white" />

    <!-- 门/登出图标 -->
    <path v-if="icon === 'logout'" d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" stroke="currentColor" stroke-width="2" fill="none" />
    <polyline v-if="icon === 'logout'" points="16 17 21 12 16 7" stroke="currentColor" stroke-width="2" fill="none" />
    <line v-if="icon === 'logout'" x1="21" y1="12" x2="9" y2="12" stroke="currentColor" stroke-width="2" />

    <!-- 图表图标 -->
    <line v-if="icon === 'chart'" x1="12" y1="5" x2="12" y2="19" stroke="currentColor" stroke-width="2" />
    <polyline v-if="icon === 'chart'" points="19 9 15 15 9 9 5 13" stroke="currentColor" stroke-width="2" fill="none" />

    <!-- 播放/播放视频图标 -->
    <polygon v-if="icon === 'play'" points="5 3 19 12 5 21" fill="currentColor" />

    <!-- 书籍图标 -->
    <path v-if="icon === 'book'" d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" stroke-width="2" fill="none" />
    <path v-if="icon === 'book'" d="M6.5 2H20v10H6.5a2.5 2.5 0 0 1 0-5A2.5 2.5 0 0 1 6.5 2z" />

    <!-- 加号图标 -->
    <line v-if="icon === 'plus'" x1="12" y1="5" x2="12" y2="19" stroke="currentColor" stroke-width="2" />
    <line v-if="icon === 'plus'" x1="5" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="2" />

    <!-- 铃铛图标 -->
    <path v-if="icon === 'bell'" d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
    <path v-if="icon === 'bell'" d="M13.73 21a2 2 0 0 1-3.46 0" />

    <!-- 编辑图标 -->
    <path v-if="icon === 'edit'" d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" stroke-width="2" fill="none" />
    <path v-if="icon === 'edit'" d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2" fill="none" />

    <!-- 布局/工作台图标 -->
    <rect v-if="icon === 'layout'" x="3" y="3" width="7" height="7" rx="1" />
    <rect v-if="icon === 'layout'" x="14" y="3" width="7" height="4" rx="1" />
    <rect v-if="icon === 'layout'" x="14" y="11" width="7" height="10" rx="1" />
    <rect v-if="icon === 'layout'" x="3" y="14" width="7" height="7" rx="1" />

    <!-- 消息图标 -->
    <path v-if="icon === 'message'" d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />

    <!-- 勾选圆圈图标 -->
    <circle v-if="icon === 'check-circle'" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none" />
    <polyline v-if="icon === 'check-circle'" points="9 12 11 14 15 10" stroke="currentColor" stroke-width="2" fill="none" />

    <!-- 文档图标 -->
    <path v-if="icon === 'document'" d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
    <polyline v-if="icon === 'document'" points="14 2 14 8 20 8" stroke="white" stroke-width="2" fill="none" />
    <line v-if="icon === 'document'" x1="8" y1="13" x2="16" y2="13" stroke="white" stroke-width="1.5" />
    <line v-if="icon === 'document'" x1="8" y1="17" x2="16" y2="17" stroke="white" stroke-width="1.5" />

    <!-- 文件夹图标 -->
    <path v-if="icon === 'folder'" d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />

    <!-- 钱包图标 -->
    <rect v-if="icon === 'wallet'" x="2" y="6" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2" fill="none" />
    <path v-if="icon === 'wallet'" d="M2 10h20" stroke="currentColor" stroke-width="2" />
    <circle v-if="icon === 'wallet'" cx="17" cy="15" r="1.5" />

    <!-- 标签图标 -->
    <path v-if="icon === 'tag'" d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z" />
    <line v-if="icon === 'tag'" x1="7" y1="7" x2="7.01" y2="7" stroke="white" stroke-width="3" />

    <!-- 流程图标 -->
    <circle v-if="icon === 'flow'" cx="6" cy="6" r="3" stroke="currentColor" stroke-width="2" fill="none" />
    <circle v-if="icon === 'flow'" cx="18" cy="6" r="3" stroke="currentColor" stroke-width="2" fill="none" />
    <circle v-if="icon === 'flow'" cx="12" cy="18" r="3" stroke="currentColor" stroke-width="2" fill="none" />
    <line v-if="icon === 'flow'" x1="9" y1="6" x2="15" y2="6" stroke="currentColor" stroke-width="2" />
    <line v-if="icon === 'flow'" x1="8.5" y1="8" x2="10.5" y2="15.5" stroke="currentColor" stroke-width="2" />
    <line v-if="icon === 'flow'" x1="15.5" y1="8" x2="13.5" y2="15.5" stroke="currentColor" stroke-width="2" />

    <!-- 用户添加图标 -->
    <circle v-if="icon === 'user-add'" cx="9" cy="7" r="4" />
    <path v-if="icon === 'user-add'" d="M3 21v-2a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v2" />
    <line v-if="icon === 'user-add'" x1="19" y1="8" x2="19" y2="14" stroke="currentColor" stroke-width="2" />
    <line v-if="icon === 'user-add'" x1="16" y1="11" x2="22" y2="11" stroke="currentColor" stroke-width="2" />

    <!-- 阅读/培训图标 -->
    <path v-if="icon === 'reading'" d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z" stroke="currentColor" stroke-width="2" fill="none" />
    <path v-if="icon === 'reading'" d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z" stroke="currentColor" stroke-width="2" fill="none" />

    <!-- 星星图标 -->
    <polygon v-if="icon === 'star'" points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26" />

    <!-- 任务图标 -->
    <rect v-if="icon === 'task'" x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2" fill="none" />
    <polyline v-if="icon === 'task'" points="9 11 12 14 22 4" stroke="currentColor" stroke-width="2" fill="none" />
    <line v-if="icon === 'task'" x1="8" y1="6" x2="8.01" y2="6" stroke="currentColor" stroke-width="2" />

    <!-- 团队图标 -->
    <circle v-if="icon === 'team'" cx="9" cy="7" r="3" />
    <path v-if="icon === 'team'" d="M3 21v-2a3 3 0 0 1 3-3h6a3 3 0 0 1 3 3v2" />
    <circle v-if="icon === 'team'" cx="17" cy="7" r="3" />
    <path v-if="icon === 'team'" d="M21 21v-2a3 3 0 0 0-3-3h-1" />

    <!-- 菜单图标（fallback 默认） -->
    <line v-if="icon === 'menu'" x1="3" y1="6" x2="21" y2="6" stroke="currentColor" stroke-width="2" />
    <line v-if="icon === 'menu'" x1="3" y1="12" x2="21" y2="12" stroke="currentColor" stroke-width="2" />
    <line v-if="icon === 'menu'" x1="3" y1="18" x2="21" y2="18" stroke="currentColor" stroke-width="2" />

    <!-- 饼图图标 -->
    <circle v-if="icon === 'pie-chart'" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none" />
    <path v-if="icon === 'pie-chart'" d="M12 2v10l7.07 7.07" stroke="currentColor" stroke-width="2" fill="none" />
  </svg>
</template>

<script setup>
defineProps({
  icon: {
    type: String,
    default: 'file'
  }
})
</script>

<style scoped>
.icon {
  width: 1em;
  height: 1em;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
}
</style>
