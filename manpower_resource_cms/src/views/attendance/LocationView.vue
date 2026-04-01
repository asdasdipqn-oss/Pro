<template>
  <div class="location-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-info">
        <h2>打卡地点管理</h2>
        <p>管理允许打卡的地点和范围</p>
      </div>
      <button class="add-btn" @click="handleAdd">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
          <path
            d="M8 2a.75.75 0 01.75.75v4.5h4.5a.75.75 0 010 1.5h-4.5v4.5a.75.75 0 01-1.5 0v-4.5h-4.5a.75.75 0 010-1.5h4.5v-4.5A.75.75 0 018 2z"
          />
        </svg>
        新增地点
      </button>
    </div>

    <!-- 地点列表 -->
    <div class="location-grid">
      <div class="location-card" v-for="item in locationList" :key="item.id">
        <div class="card-map" :ref="(el) => setMapRef(el, item.id)"></div>
        <div class="card-content">
          <div class="card-header">
            <h3>{{ item.locationName }}</h3>
            <span class="status-tag" :class="item.status === 1 ? 'active' : 'inactive'">
              {{ item.status === 1 ? '启用' : '禁用' }}
            </span>
          </div>
          <p class="address">{{ item.address }}</p>
          <div class="info-row">
            <span class="info-item">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="#86868B">
                <circle cx="7" cy="7" r="6" fill="none" stroke="currentColor" stroke-width="1.5" />
              </svg>
              范围: {{ (item.radius / 1000).toFixed(1) }}km
            </span>
            <span class="info-item">
              坐标: {{ item.latitude?.toFixed(4) }}, {{ item.longitude?.toFixed(4) }}
            </span>
          </div>
          <div class="card-actions">
            <button class="action-btn" @click="handleEdit(item)">编辑</button>
            <button class="action-btn" @click="handleToggleStatus(item)">
              {{ item.status === 1 ? '禁用' : '启用' }}
            </button>
            <button class="action-btn danger" @click="handleDelete(item)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑打卡地点' : '新增打卡地点'"
      width="600px"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="地点名称" prop="locationName">
          <el-input v-model="form.locationName" placeholder="请输入地点名称" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input-number
            v-model="form.longitude"
            :precision="7"
            :step="0.0001"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input-number
            v-model="form.latitude"
            :precision="7"
            :step="0.0001"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="打卡范围" prop="radius">
          <el-slider
            v-model="form.radius"
            :min="100"
            :max="20000"
            :step="100"
            show-input
            :format-tooltip="(v) => (v / 1000).toFixed(1) + 'km'"
          />
        </el-form-item>
        <el-form-item label="地图选点">
          <div class="dialog-map" ref="dialogMapRef"></div>
          <p class="map-tip">点击地图选择位置</p>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="btn-cancel" @click="dialogVisible = false">取消</button>
        <button class="btn-confirm" @click="handleSubmit">确定</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageLocation,
  addLocation,
  updateLocation,
  toggleLocationStatus,
  deleteLocation,
} from '@/api/location'

// OpenLayers
import Map from 'ol/Map'
import View from 'ol/View'
import TileLayer from 'ol/layer/Tile'
import VectorLayer from 'ol/layer/Vector'
import VectorSource from 'ol/source/Vector'
import XYZ from 'ol/source/XYZ'
import { fromLonLat, toLonLat } from 'ol/proj'
import { Circle as CircleStyle, Fill, Stroke, Style } from 'ol/style'
import Feature from 'ol/Feature'
import Point from 'ol/geom/Point'
import { circular } from 'ol/geom/Polygon'
import 'ol/ol.css'

const locationList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const dialogMapRef = ref(null)
const cardMaps = ref({})

let dialogMap = null
let dialogMarker = null
let dialogCircle = null
let dialogRangeSource = null

const form = reactive({
  id: null,
  locationName: '',
  address: '',
  latitude: 39.9042,
  longitude: 116.4074,
  radius: 10000,
  status: 1,
  remark: '',
})

const rules = {
  locationName: [{ required: true, message: '请输入地点名称', trigger: 'blur' }],
  longitude: [{ required: true, message: '请输入经度', trigger: 'blur' }],
  latitude: [{ required: true, message: '请输入纬度', trigger: 'blur' }],
  radius: [{ required: true, message: '请设置打卡范围', trigger: 'blur' }],
}

// 设置卡片地图引用
const setMapRef = (el, id) => {
  if (el) {
    cardMaps.value[id] = el
  }
}

// 初始化卡片小地图
const initCardMaps = () => {
  locationList.value.forEach((item) => {
    const container = cardMaps.value[item.id]
    if (!container || container._mapInitialized) return

    const lon = parseFloat(item.longitude)
    const lat = parseFloat(item.latitude)
    const center = fromLonLat([lon, lat])

    const rangeSource = new VectorSource()
    // 使用circular创建地理坐标系下精确的圆形
    const circleGeom = circular([lon, lat], item.radius, 64)
    circleGeom.transform('EPSG:4326', 'EPSG:3857')
    const circle = new Feature({ geometry: circleGeom })
    rangeSource.addFeature(circle)

    new Map({
      target: container,
      layers: [
        new TileLayer({
          source: new XYZ({
            url: 'https://webrd01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}',
          }),
        }),
        new VectorLayer({
          source: rangeSource,
          style: new Style({
            fill: new Fill({ color: 'rgba(0, 122, 255, 0.15)' }),
            stroke: new Stroke({ color: '#007AFF', width: 2 }),
          }),
        }),
      ],
      view: new View({ center, zoom: 12 }),
      controls: [],
      interactions: [],
    })

    container._mapInitialized = true
  })
}

// 初始化弹窗地图
const initDialogMap = () => {
  if (!dialogMapRef.value) return

  const center = fromLonLat([form.longitude, form.latitude])

  const markerSource = new VectorSource()
  dialogMarker = new Feature({ geometry: new Point(center) })
  dialogMarker.setStyle(
    new Style({
      image: new CircleStyle({
        radius: 8,
        fill: new Fill({ color: '#007AFF' }),
        stroke: new Stroke({ color: '#fff', width: 2 }),
      }),
    }),
  )
  markerSource.addFeature(dialogMarker)

  dialogRangeSource = new VectorSource()
  // 使用circular创建地理坐标系下精确的圆形
  const circleGeom = circular([form.longitude, form.latitude], form.radius, 64)
  circleGeom.transform('EPSG:4326', 'EPSG:3857')
  dialogCircle = new Feature({ geometry: circleGeom })
  dialogRangeSource.addFeature(dialogCircle)

  dialogMap = new Map({
    target: dialogMapRef.value,
    layers: [
      new TileLayer({
        source: new XYZ({
          url: 'https://webrd01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}',
        }),
      }),
      new VectorLayer({
        source: dialogRangeSource,
        style: new Style({
          fill: new Fill({ color: 'rgba(0, 122, 255, 0.1)' }),
          stroke: new Stroke({ color: '#007AFF', width: 2 }),
        }),
      }),
      new VectorLayer({ source: markerSource }),
    ],
    view: new View({ center, zoom: 13 }),
  })

  // 点击地图选点
  dialogMap.on('click', (e) => {
    const coords = toLonLat(e.coordinate)
    form.longitude = coords[0]
    form.latitude = coords[1]
    updateDialogMapMarker()
  })
}

// 更新弹窗地图标记
const updateDialogMapMarker = () => {
  if (!dialogMap || !dialogMarker || !dialogRangeSource) return
  const center = fromLonLat([form.longitude, form.latitude])
  dialogMarker.getGeometry().setCoordinates(center)

  // 重新创建圆形
  dialogRangeSource.clear()
  const circleGeom = circular([form.longitude, form.latitude], form.radius, 64)
  circleGeom.transform('EPSG:4326', 'EPSG:3857')
  dialogCircle = new Feature({ geometry: circleGeom })
  dialogRangeSource.addFeature(dialogCircle)

  dialogMap.getView().animate({ center, duration: 300 })
}

// 监听范围变化
watch(
  () => form.radius,
  () => {
    if (dialogRangeSource && form.longitude && form.latitude) {
      dialogRangeSource.clear()
      const circleGeom = circular([form.longitude, form.latitude], form.radius, 64)
      circleGeom.transform('EPSG:4326', 'EPSG:3857')
      dialogCircle = new Feature({ geometry: circleGeom })
      dialogRangeSource.addFeature(dialogCircle)
    }
  },
)

// 获取列表
const fetchList = async () => {
  try {
    const res = await pageLocation({ pageNum: 1, pageSize: 100 })
    locationList.value = res.data?.records || []
    await nextTick()
    initCardMaps()
  } catch (error) {
    console.error(error)
  }
}

const handleAdd = async () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    locationName: '',
    address: '',
    latitude: 39.9042,
    longitude: 116.4074,
    radius: 10000,
    status: 1,
    remark: '',
  })
  dialogVisible.value = true
  await nextTick()
  initDialogMap()
}

const handleEdit = async (row) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
  await nextTick()
  initDialogMap()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (isEdit.value) {
      await updateLocation(form)
      ElMessage.success('修改成功')
    } else {
      await addLocation(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (error) {
    console.error(error)
  }
}

const handleToggleStatus = async (row) => {
  try {
    await toggleLocationStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('状态已更新')
    fetchList()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该打卡地点吗？', '提示', { type: 'warning' })
  try {
    await deleteLocation(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.location-view {
  max-width: 1200px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-info h2 {
  font-size: 22px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.header-info p {
  font-size: 14px;
  color: #86868b;
  margin: 4px 0 0 0;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  background: #1d1d1f;
  color: #ffffff;
  transition: all 0.2s;
}

.add-btn:hover {
  background: #000000;
}

/* 卡片网格 */
.location-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}

.location-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
}

.card-map {
  height: 160px;
  width: 100%;
}

.card-content {
  padding: 16px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.status-tag {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
}

.status-tag.active {
  background: rgba(52, 199, 89, 0.12);
  color: #34c759;
}

.status-tag.inactive {
  background: rgba(142, 142, 147, 0.12);
  color: #8e8e93;
}

.address {
  font-size: 13px;
  color: #86868b;
  margin: 0 0 12px 0;
}

.info-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #86868b;
}

.card-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f5f5f7;
}

.action-btn {
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  background: #f5f5f7;
  color: #1d1d1f;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #e5e5ea;
}

.action-btn.danger {
  color: #ff3b30;
}

.action-btn.danger:hover {
  background: rgba(255, 59, 48, 0.1);
}

/* 弹窗地图 */
.dialog-map {
  width: 100%;
  height: 250px;
  border-radius: 8px;
  overflow: hidden;
}

.map-tip {
  font-size: 12px;
  color: #86868b;
  margin-top: 8px;
}

/* 弹窗按钮 */
.btn-cancel,
.btn-confirm {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  margin-left: 12px;
}

.btn-cancel {
  background: #f5f5f7;
  border: none;
  color: #1d1d1f;
}

.btn-confirm {
  background: #1d1d1f;
  border: none;
  color: #ffffff;
}

.btn-confirm:hover {
  background: #000000;
}
</style>
