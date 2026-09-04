<template>
  <div class="merchant">
    <div class="container" v-loading="loading">
      <el-card v-if="store" class="store-card">
        <div class="store-head">
          <el-icon :size="40" color="#4f6ef7"><Shop /></el-icon>
          <div class="store-info">
            <div class="name">{{ store.name }}</div>
            <div v-if="store.announcement" class="announcement">{{ store.announcement }}</div>
          </div>
          <el-button size="small" @click="openEdit">编辑店铺</el-button>
          <el-button size="small" type="danger" plain @click="onClose">关闭店铺</el-button>
        </div>
        <div class="stats">
          <div class="stat">
            <div class="num">{{ store.onSaleCount || 0 }}</div>
            <div class="label">在售商品</div>
          </div>
          <div class="stat">
            <div class="num">{{ store.soldCount || 0 }}</div>
            <div class="label">已售出</div>
          </div>
          <div class="stat">
            <div class="num">{{ store.rating ? store.rating.toFixed(1) : '-' }}</div>
            <div class="label">店铺评分</div>
          </div>
        </div>
      </el-card>

      <div class="nav-grid">
        <el-card class="nav-card" shadow="hover" @click="$router.push('/publish')">
          <el-icon :size="32" color="#4f6ef7"><Plus /></el-icon>
          <div class="nav-title">上架商品</div>
          <div class="nav-desc">发布新的闲置商品</div>
        </el-card>
        <el-card class="nav-card" shadow="hover" @click="$router.push('/merchant/products')">
          <el-icon :size="32" color="#7b5cf0"><Goods /></el-icon>
          <div class="nav-title">货架管理</div>
          <div class="nav-desc">查看/上下架商品</div>
        </el-card>
        <el-card class="nav-card" shadow="hover" @click="$router.push('/merchant/orders')">
          <el-icon :size="32" color="#f56c6c"><List /></el-icon>
          <div class="nav-title">卖出订单</div>
          <div class="nav-desc">处理订单并发货</div>
        </el-card>
      </div>
    </div>

    <el-dialog v-model="editVisible" title="编辑店铺" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="店铺名">
          <el-input v-model="form.name" maxlength="100" />
        </el-form-item>
        <el-form-item label="公告">
          <el-input v-model="form.announcement" type="textarea" :rows="3" maxlength="300" placeholder="店铺公告，如：本店出售毕业闲置，价格可小刀" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Shop, Plus, Goods, List } from '@element-plus/icons-vue'
import { getMyStore, updateStore, closeStore } from '../api/store'

const router = useRouter()

const store = ref(null)
const loading = ref(false)
const editVisible = ref(false)
const saving = ref(false)
const form = reactive({ name: '', announcement: '' })

async function load() {
  loading.value = true
  try {
    const res = await getMyStore()
    store.value = res.data
  } catch (e) {
    store.value = null
  } finally {
    loading.value = false
  }
}

function openEdit() {
  form.name = store.value.name
  form.announcement = store.value.announcement || ''
  editVisible.value = true
}

async function onSave() {
  saving.value = true
  try {
    const res = await updateStore({ name: form.name, announcement: form.announcement })
    store.value = res.data
    ElMessage.success('保存成功')
    editVisible.value = false
  } finally {
    saving.value = false
  }
}

async function onClose() {
  await ElMessageBox.confirm('关闭店铺后，所有在售商品将自动下架，确定关闭吗？', '提示', { type: 'warning' })
  await closeStore()
  ElMessage.success('店铺已关闭')
  router.push('/profile')
}

onMounted(load)
</script>

<style scoped>
.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.store-card {
  margin-bottom: 20px;
}
.store-head {
  display: flex;
  align-items: center;
  gap: 16px;
}
.store-info {
  flex: 1;
}
.name {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}
.announcement {
  color: #909399;
  font-size: 13px;
  margin-top: 6px;
}
.stats {
  display: flex;
  gap: 40px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
.stat {
  text-align: center;
}
.stat .num {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}
.stat .label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.nav-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.nav-card {
  cursor: pointer;
  text-align: center;
  padding: 12px 0;
}
.nav-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-top: 10px;
}
.nav-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
@media (max-width: 640px) {
  .nav-grid {
    grid-template-columns: 1fr;
  }
  .stats {
    gap: 20px;
    justify-content: space-between;
  }
}
</style>
