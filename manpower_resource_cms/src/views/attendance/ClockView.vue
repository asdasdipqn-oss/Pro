<template>
  <div class="clock-view">
    <div class="clock-grid">
      <!-- 左侧：地图和位置信息 -->
      <div class="map-section">
        <div class="map-container" ref="mapContainer"></div>
        <div class="location-info">
          <div
            class="location-status"
            :class="{ valid: isInRange, invalid: !isInRange && currentPosition }"
          >
            <svg v-if="isInRange" width="20" height="20" viewBox="0 0 20 20" fill="#34C759">
              <path
                fill-rule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                clip-rule="evenodd"
              />
            </svg>
            <svg
              v-else-if="currentPosition"
              width="20"
              height="20"
              viewBox="0 0 20 20"
              fill="#FF3B30"
            >
              <path
                fill-rule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                clip-rule="evenodd"
              />
            </svg>
            <svg v-else width="20" height="20" viewBox="0 0 20 20" fill="#86868B">
              <path
                fill-rule="evenodd"
                d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z"
                clip-rule="evenodd"
              />
            </svg>
            <span v-if="locationLoading">正在获取位置...</span>
            <span v-else-if="isInRange">您在打卡范围内 ({{ matchedLocation?.locationName }})</span>
            <span v-else-if="currentPosition">您不在打卡范围内</span>
            <span v-else>请允许获取位置信息</span>
          </div>
          <div class="location-address" v-if="currentAddress">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="#86868B">
              <path
                fill-rule="evenodd"
                d="M8 1a5 5 0 00-5 5c0 2.761 5 9 5 9s5-6.239 5-9a5 5 0 00-5-5zm0 7a2 2 0 100-4 2 2 0 000 4z"
                clip-rule="evenodd"
              />
            </svg>
            <span>{{ currentAddress }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧：打卡操作 -->
      <div class="clock-section">
        <div class="time-display">
          <div class="current-time">{{ currentTime }}</div>
          <div class="current-date">{{ currentDate }}</div>
        </div>

        <div class="clock-buttons">
          <button
            class="clock-btn clock-in"
            :class="{ disabled: hasClockIn || !isInRange }"
            :disabled="hasClockIn || !isInRange"
            @click="handleClock(1)"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
              />
            </svg>
            <span>上班打卡</span>
            <small v-if="hasClockIn">已打卡 {{ clockInTime }}</small>
          </button>

          <button
            class="clock-btn clock-out"
            :class="{ disabled: hasClockOut || !isInRange }"
            :disabled="hasClockOut || !isInRange"
            @click="handleClock(2)"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
              />
            </svg>
            <span>下班打卡</span>
            <small v-if="hasClockOut">已打卡 {{ clockOutTime }}</small>
          </button>
        </div>

        <!-- 今日记录 -->
        <div class="records-section">
          <h3>今日打卡记录</h3>
          <div class="records-list" v-if="todayRecords.length">
            <div class="record-item" v-for="record in todayRecords" :key="record.id">
              <div class="record-icon" :class="record.clockType === 1 ? 'in' : 'out'">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                  <path
                    fill-rule="evenodd"
                    d="M8 15A7 7 0 108 1a7 7 0 000 14zm1-9a1 1 0 10-2 0v3a1 1 0 00.293.707l2 2a1 1 0 001.414-1.414L9 8.586V6z"
                    clip-rule="evenodd"
                  />
                </svg>
              </div>
              <div class="record-info">
                <div class="record-type">
                  {{ record.clockType === 1 ? '上班打卡' : '下班打卡' }}
                </div>
                <div class="record-time">{{ record.clockTime?.substring(11, 19) }}</div>
              </div>
              <div
                class="record-status"
                :class="record.locationStatus === 1 ? 'success' : 'warning'"
              >
                <span>{{ record.locationStatus === 1 ? '正常' : '异常' }}</span>
              </div>
            </div>
          </div>
          <div class="records-empty" v-else>
            <p>暂无打卡记录</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { listEnabledLocation } from '@/api/location'
import { getTodayRecords, clockIn } from '@/api/attendance'
import { wgs84ToGcj02 } from '@/utils/coordTransform'

// OpenLayers imports
import Map from 'ol/Map'
import View from 'ol/View'
import TileLayer from 'ol/layer/Tile'
import VectorLayer from 'ol/layer/Vector'
import VectorSource from 'ol/source/Vector'
import XYZ from 'ol/source/XYZ'
import { fromLonLat, toLonLat } from 'ol/proj'
import { Circle as CircleStyle, Fill, Stroke, Style, Icon } from 'ol/style'
import Feature from 'ol/Feature'
import Point from 'ol/geom/Point'
import { circular } from 'ol/geom/Polygon'
import 'ol/ol.css'

const mapContainer = ref(null)
const currentTime = ref('')
const currentDate = ref('')
const todayRecords = ref([])
const clockLocations = ref([])
const currentPosition = ref(null)
const currentAddress = ref('')
const matchedLocation = ref(null)
const isInRange = ref(false)
const locationLoading = ref(true)
let timer = null
let map = null
let positionFeature = null

const hasClockIn = computed(() => todayRecords.value.some((r) => r.clockType === 1))
const hasClockOut = computed(() => todayRecords.value.some((r) => r.clockType === 2))
const clockInTime = computed(() => {
  const record = todayRecords.value.find((r) => r.clockType === 1)
  return record ? record.clockTime.substring(11, 19) : ''
})
const clockOutTime = computed(() => {
  const record = todayRecords.value.find((r) => r.clockType === 2)
  return record ? record.clockTime.substring(11, 19) : ''
})

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
  currentDate.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  })
}

// 初始化地图
const initMap = () => {
  // 高德地图瓦片
  const tileLayer = new TileLayer({
    source: new XYZ({
      url: 'https://webrd01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}',
    }),
  })

  // 打卡范围图层
  const rangeSource = new VectorSource()
  const rangeLayer = new VectorLayer({
    source: rangeSource,
    style: new Style({
      fill: new Fill({ color: 'rgba(0, 122, 255, 0.1)' }),
      stroke: new Stroke({ color: '#007AFF', width: 2 }),
    }),
  })

  // 位置标记图层
  const markerSource = new VectorSource()
  const markerLayer = new VectorLayer({
    source: markerSource,
  })

  map = new Map({
    target: mapContainer.value,
    layers: [tileLayer, rangeLayer, markerLayer],
    view: new View({
      center: fromLonLat([116.4074, 39.9042]), // 默认北京
      zoom: 12,
    }),
  })

  // 添加打卡地点范围
  clockLocations.value.forEach((loc) => {
    const lon = parseFloat(loc.longitude)
    const lat = parseFloat(loc.latitude)
    const center = fromLonLat([lon, lat])

    // 使用circular创建地理坐标系下精确的圆形
    const circleGeom = circular([lon, lat], loc.radius, 64)
    circleGeom.transform('EPSG:4326', 'EPSG:3857')
    const circle = new Feature({ geometry: circleGeom })
    rangeSource.addFeature(circle)

    // 添加地点标记
    const marker = new Feature({
      geometry: new Point(center),
    })
    marker.setStyle(
      new Style({
        image: new CircleStyle({
          radius: 8,
          fill: new Fill({ color: '#007AFF' }),
          stroke: new Stroke({ color: '#fff', width: 2 }),
        }),
      }),
    )
    markerSource.addFeature(marker)
  })

  // 当前位置标记
  positionFeature = new Feature()
  positionFeature.setStyle(
    new Style({
      image: new CircleStyle({
        radius: 10,
        fill: new Fill({ color: '#34C759' }),
        stroke: new Stroke({ color: '#fff', width: 3 }),
      }),
    }),
  )
  markerSource.addFeature(positionFeature)
}

// 获取当前位置（使用原生 Geolocation API）
const getCurrentPosition = () => {
  if (!navigator.geolocation) {
    ElMessage.warning('浏览器不支持定位功能')
    locationLoading.value = false
    return
  }

  navigator.geolocation.getCurrentPosition(
    (position) => {
      locationLoading.value = false

      // 原生 API 返回的是 WGS-84 坐标
      const wgsLng = position.coords.longitude
      const wgsLat = position.coords.latitude

      // WGS-84 转 GCJ-02（高德地图坐标系）
      const gcj02 = wgs84ToGcj02(wgsLng, wgsLat)
      const latitude = gcj02.lat
      const longitude = gcj02.lng

      // 调试日志
      console.log('=== 原生定位坐标信息 ===')
      console.log('原始坐标 (WGS-84):', wgsLng, wgsLat)
      console.log('转换后坐标 (GCJ-02):', longitude, latitude)
      console.log('====================')

      currentPosition.value = { latitude, longitude }
      currentAddress.value = `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`

      // 更新地图中心和位置标记
      if (map && positionFeature) {
        const coords = fromLonLat([longitude, latitude])
        positionFeature.setGeometry(new Point(coords))
        map.getView().animate({ center: coords, zoom: 14, duration: 500 })
      }

      // 验证是否在打卡范围内
      checkInRange(latitude, longitude)
    },
    (error) => {
      locationLoading.value = false
      console.error('定位失败:', error.message)
      switch (error.code) {
        case error.PERMISSION_DENIED:
          ElMessage.error('用户拒绝了定位请求')
          break
        case error.POSITION_UNAVAILABLE:
          ElMessage.error('位置信息不可用')
          break
        case error.TIMEOUT:
          ElMessage.error('获取位置超时')
          break
        default:
          ElMessage.error('获取位置失败')
      }
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 },
  )
}

// 检查是否在打卡范围内
const checkInRange = (lat, lon) => {
  for (const loc of clockLocations.value) {
    const distance = calculateDistance(
      lat,
      lon,
      parseFloat(loc.latitude),
      parseFloat(loc.longitude),
    )
    if (distance <= loc.radius) {
      isInRange.value = true
      matchedLocation.value = loc
      return
    }
  }
  isInRange.value = false
  matchedLocation.value = null
}

// 计算两点距离（Haversine公式）
const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371000 // 地球半径（米）
  const rad = Math.PI / 180
  const dLat = (lat2 - lat1) * rad
  const dLon = (lon2 - lon1) * rad
  const a =
    Math.sin(dLat / 2) ** 2 + Math.cos(lat1 * rad) * Math.cos(lat2 * rad) * Math.sin(dLon / 2) ** 2
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

// 逆地理编码获取地址
const getAddressFromCoords = async (lat, lon) => {
  try {
    // 使用简单的地址显示
    currentAddress.value = `${lat.toFixed(6)}, ${lon.toFixed(6)}`
  } catch (error) {
    console.error(error)
  }
}

// 获取打卡地点列表
const fetchClockLocations = async () => {
  try {
    const res = await listEnabledLocation()
    clockLocations.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const fetchTodayRecords = async () => {
  try {
    const res = await getTodayRecords()
    todayRecords.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const handleClock = async (clockType) => {
  if (!currentPosition.value) {
    ElMessage.warning('请先获取位置信息')
    return
  }

  try {
    await clockIn({
      clockType,
      latitude: currentPosition.value.latitude,
      longitude: currentPosition.value.longitude,
      address: currentAddress.value,
    })
    ElMessage.success(clockType === 1 ? '上班打卡成功' : '下班打卡成功')
    fetchTodayRecords()
  } catch (error) {
    console.error(error)
  }
}

onMounted(async () => {
  updateTime()
  timer = setInterval(updateTime, 1000)

  await fetchClockLocations()
  await fetchTodayRecords()

  await nextTick()
  initMap()

  // 获取当前位置
  getCurrentPosition()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (map) map.setTarget(null)
})
</script>

<style scoped>
.clock-view {
  max-width: 1100px;
  margin: 0 auto;
}

.clock-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 24px;
}

/* 地图区域 */
.map-section {
  background: #ffffff;
  border-radius: 20px;
  overflow: hidden;
}

.map-container {
  width: 100%;
  height: 400px;
}

.location-info {
  padding: 16px 20px;
  border-top: 1px solid #f5f5f7;
}

.location-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #86868b;
}

.location-status.valid {
  color: #34c759;
}

.location-status.invalid {
  color: #ff3b30;
}

.location-address {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #86868b;
  margin-top: 8px;
}

/* 打卡区域 */
.clock-section {
  background: #ffffff;
  border-radius: 20px;
  padding: 32px 24px;
}

.time-display {
  text-align: center;
  margin-bottom: 32px;
}

.current-time {
  font-size: 48px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -2px;
  font-variant-numeric: tabular-nums;
}

.current-date {
  font-size: 14px;
  color: #86868b;
  margin-top: 6px;
}

.clock-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.clock-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 20px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
  position: relative;
}

.clock-btn span {
  flex: 1;
}

.clock-btn small {
  position: absolute;
  right: 14px;
  font-size: 11px;
  opacity: 0.7;
}

.clock-btn.clock-in {
  background: #1d1d1f;
  color: #ffffff;
}

.clock-btn.clock-in:hover:not(.disabled) {
  background: #000000;
}

.clock-btn.clock-out {
  background: #f5f5f7;
  color: #1d1d1f;
}

.clock-btn.clock-out:hover:not(.disabled) {
  background: #e5e5ea;
}

.clock-btn.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* 记录区域 */
.records-section {
  border-top: 1px solid #f5f5f7;
  padding-top: 20px;
}

.records-section h3 {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 16px 0;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.record-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f5f7;
  border-radius: 10px;
}

.record-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.record-icon.in {
  background: rgba(52, 199, 89, 0.12);
  color: #34c759;
}

.record-icon.out {
  background: rgba(0, 122, 255, 0.12);
  color: #007aff;
}

.record-info {
  flex: 1;
}

.record-type {
  font-size: 13px;
  font-weight: 500;
  color: #1d1d1f;
}

.record-time {
  font-size: 12px;
  color: #86868b;
  margin-top: 2px;
}

.record-status {
  font-size: 12px;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 6px;
}

.record-status.success {
  color: #34c759;
  background: rgba(52, 199, 89, 0.12);
}

.record-status.warning {
  color: #ff9500;
  background: rgba(255, 149, 0, 0.12);
}

.records-empty {
  text-align: center;
  padding: 30px 20px;
}

.records-empty p {
  font-size: 13px;
  color: #86868b;
}

/* 响应式 */
@media (max-width: 768px) {
  .clock-grid {
    grid-template-columns: 1fr;
  }

  .map-container {
    height: 300px;
  }
}
</style>
