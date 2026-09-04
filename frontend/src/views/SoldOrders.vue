<template>
  <div class="sold-orders">
    <div class="container" v-loading="loading">
      <h2 class="title">卖出订单</h2>

      <el-empty v-if="!loading && orders.length === 0" description="还没有卖出订单" :image-size="120" />

      <div v-else class="order-list">
        <el-card v-for="o in orders" :key="o.id" class="order-card" :body-style="{ padding: 0 }">
          <div class="order-head">
            <span>订单号 {{ o.orderNo }}</span>
            <el-tag :type="ORDER_STATUS_TAG_TYPES[o.status]" size="small">{{ ORDER_STATUS_LABELS[o.status] }}</el-tag>
          </div>
          <div class="order-body">
            <el-image :src="o.productImage" fit="cover" class="goods-img">
              <template #error><div class="img-fallback"><el-icon><Picture /></el-icon></div></template>
            </el-image>
            <div class="info">
              <div class="goods-title">{{ o.productTitle }}</div>
              <div class="meta">买家：{{ o.buyerName }}</div>
              <div class="meta">¥{{ o.price }} × {{ o.quantity }}</div>
            </div>
            <div class="total">¥{{ o.totalAmount }}</div>
          </div>
          <div class="order-foot">
            <span class="time">{{ formatTime(o.createdAt) }}</span>
            <el-button v-if="o.status === 'PAID'" type="primary" size="small" @click="onShip(o)">发货</el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getSoldOrders, shipOrder } from '../api/order'
import { ORDER_STATUS_LABELS, ORDER_STATUS_TAG_TYPES } from '../utils/constants'

const orders = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await getSoldOrders()
    orders.value = res.data
  } finally {
    loading.value = false
  }
}

async function onShip(o) {
  await ElMessageBox.confirm('确认已发货？', '提示', { type: 'warning' })
  await shipOrder(o.id)
  ElMessage.success('已发货')
  load()
}

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : ''
}

onMounted(load)
</script>

<style scoped>
.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.title {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 16px;
}
.order-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.order-card {
  border-radius: 10px;
}
.order-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  font-size: 13px;
  color: #909399;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}
.order-body {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
}
.goods-img {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  flex-shrink: 0;
}
.img-fallback {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #c0c4cc;
}
.info {
  flex: 1;
}
.goods-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.meta {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.total {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 700;
}
.order-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-top: 1px solid #f0f0f0;
}
.time {
  font-size: 13px;
  color: #c0c4cc;
}
</style>
