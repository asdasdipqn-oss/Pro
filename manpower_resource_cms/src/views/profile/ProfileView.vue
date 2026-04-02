<template>
  <div class="profile-view">
    <el-row :gutter="20">
      <!-- 个人信息卡片 -->
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="avatar-section">
            <div class="avatar">{{ userStore.username?.charAt(0)?.toUpperCase() || 'U' }}</div>
            <h2 class="username">{{ userStore.username }}</h2>
            <div class="role-tags">
              <el-tag v-for="role in userStore.roles" :key="role" size="small" type="primary">{{
                role
              }}</el-tag>
            </div>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-item">
              <span class="label">用户ID</span>
              <span class="value">{{ userInfo.id || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">关联员工</span>
              <span class="value">{{ userInfo.employeeId || '未关联' }}</span>
            </div>
            <div class="info-item">
              <span class="label">岗位</span>
              <span class="value">{{ userInfo.positionName || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">账号状态</span>
              <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'" size="small">
                {{ userInfo.status === 1 ? '正常' : '已冻结' }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="label">最后登录</span>
              <span class="value">{{ userInfo.lastLoginTime || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">注册时间</span>
              <span class="value">{{ userInfo.createTime || '-' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧操作区 -->
      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="个人信息" name="profile">
              <el-form
                ref="profileFormRef"
                :model="profileForm"
                label-width="100px"
                style="max-width: 500px"
              >
                <el-form-item label="用户名">
                  <el-input :value="userInfo.username" disabled />
                </el-form-item>
                <el-form-item label="昵称">
                  <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    @click="handleUpdateProfile"
                    :loading="profileSubmitting"
                    >保存修改</el-button
                  >
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="password">
              <el-form
                ref="pwdFormRef"
                :model="pwdForm"
                :rules="pwdRules"
                label-width="100px"
                style="max-width: 500px"
              >
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input
                    v-model="pwdForm.oldPassword"
                    type="password"
                    show-password
                    placeholder="请输入原密码"
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="pwdForm.newPassword"
                    type="password"
                    show-password
                    placeholder="请输入新密码(6-20位)"
                  />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="pwdForm.confirmPassword"
                    type="password"
                    show-password
                    placeholder="请再次输入新密码"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleChangePassword" :loading="submitting"
                    >修改密码</el-button
                  >
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="安全设置" name="security">
              <div class="security-list">
                <div class="security-item">
                  <div class="security-info">
                    <div class="security-title">登录密码</div>
                    <div class="security-desc">定期更换密码可以保护账号安全</div>
                  </div>
                  <el-button type="primary" link @click="activeTab = 'password'">修改</el-button>
                </div>
                <el-divider />
                <div class="security-item">
                  <div class="security-info">
                    <div class="security-title">账号绑定</div>
                    <div class="security-desc">
                      已绑定员工ID：{{ userInfo.employeeId || '未绑定' }}
                    </div>
                  </div>
                  <el-tag type="success" v-if="userInfo.employeeId">已绑定</el-tag>
                  <el-tag type="info" v-else>未绑定</el-tag>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="操作日志" name="logs">
              <el-empty description="暂无操作日志" />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getCurrentUser, changePassword, updateProfile } from '@/api/user'

const userStore = useUserStore()
const activeTab = ref('profile')
const submitting = ref(false)
const profileSubmitting = ref(false)
const pwdFormRef = ref(null)
const profileFormRef = ref(null)

const profileForm = reactive({
  nickname: '',
  email: '',
  phone: '',
})

const userInfo = ref({
  id: null,
  status: 1,
  employeeId: null,
  positionName: null,
  lastLoginTime: null,
  createTime: null,
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
}

const fetchUserInfo = async () => {
  try {
    const res = await getCurrentUser()
    if (res.data) {
      userInfo.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const handleChangePassword = async () => {
  try {
    await pwdFormRef.value.validate()
    submitting.value = true
    await changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleUpdateProfile = async () => {
  profileSubmitting.value = true
  try {
    await updateProfile(profileForm)
    ElMessage.success('个人信息修改成功')
    fetchUserInfo()
  } catch (error) {
    console.error(error)
  } finally {
    profileSubmitting.value = false
  }
}

onMounted(async () => {
  await fetchUserInfo()
  profileForm.nickname = userInfo.value.nickname || ''
  profileForm.email = userInfo.value.email || ''
  profileForm.phone = userInfo.value.phone || ''
})
</script>

<style scoped>
.profile-card {
  text-align: center;
}
.avatar-section {
  padding: 20px 0;
}
.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 32px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}
.username {
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 12px;
}
.role-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}
.info-list {
  text-align: left;
}
.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}
.info-item:last-child {
  border-bottom: none;
}
.info-item .label {
  color: #909399;
  font-size: 14px;
}
.info-item .value {
  color: #303133;
  font-size: 14px;
}
.security-list {
  padding: 10px 0;
}
.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}
.security-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}
.security-desc {
  font-size: 13px;
  color: #909399;
}
</style>
