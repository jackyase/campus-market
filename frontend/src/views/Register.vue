<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="brand">
        <el-icon :size="36" color="#4f6ef7"><School /></el-icon>
        <h1>创建账号</h1>
        <p>加入校园二手集市</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent>
        <el-form-item prop="username">
          <el-input v-model="form.username" size="large" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" size="large" placeholder="昵称（可选）" :prefix-icon="Postcard" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            size="large"
            type="password"
            show-password
            placeholder="密码（至少 6 位）"
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item prop="confirm">
          <el-input
            v-model="form.confirm"
            size="large"
            type="password"
            show-password
            placeholder="确认密码"
            :prefix-icon="Lock"
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="submit" :loading="loading" @click="onSubmit">
          注册
        </el-button>
      </el-form>
      <div class="foot">
        已有账号？
        <router-link to="/login">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Postcard } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', nickname: '', password: '', confirm: '' })
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度需在 2-50 之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度需在 6-50 之间', trigger: 'blur' }
  ],
  confirm: [
    {
      validator: (rule, value, cb) =>
        value === form.password ? cb() : cb(new Error('两次输入的密码不一致')),
      trigger: 'blur'
    }
  ]
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.register({
      username: form.username,
      nickname: form.nickname,
      password: form.password
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4f6ef7 0%, #7b5cf0 100%);
}
.auth-card {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18);
}
.brand {
  text-align: center;
  margin-bottom: 28px;
}
.brand h1 {
  font-size: 22px;
  margin: 10px 0 6px;
  color: #303133;
}
.brand p {
  color: #909399;
  font-size: 14px;
}
.submit {
  width: 100%;
  margin-top: 4px;
}
.foot {
  margin-top: 18px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}
.foot a {
  color: #4f6ef7;
  text-decoration: none;
  font-weight: 600;
}
</style>
