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
