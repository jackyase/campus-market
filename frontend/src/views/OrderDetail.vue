<template>
  <div class="order-detail">
    <div class="container" v-loading="loading">
      <template v-if="order">
        <el-card class="status-card">
          <el-steps :active="stepActive" align-center finish-status="success">
            <el-step title="提交订单" :description="formatTime(order.createdAt)" />
            <el-step title="付款" :description="order.paidAt ? formatTime(order.paidAt) : ''" />
            <el-step title="卖家发货" />
            <el-step title="确认收货" />
          </el-steps>
        </el-card>

        <el-card class="main-card">
          <div class="seller-row">
            <el-avatar :size="32" class="avatar">{{ (order.sellerName || 'U')[0] }}</el-avatar>
            <span class="seller-name">{{ order.sellerName }}</span>
            <el-tag :type="ORDER_STATUS_TAG_TYPES[order.status]" size="small" style="margin-left: auto">
              {{ ORDER_STATUS_LABELS[order.status] }}
            </el-tag>
          </div>

          <div class="goods-row">
            <el-image :src="order.productImage" fit="cover" class="goods-img">
              <template #error><div class="img-fallback"><el-icon><Picture /></el-icon></div></template>
            </el-image>
            <div class="goods-info">
              <div class="goods-title" @click="$router.push(`/product/${order.productId}`)">
                {{ order.productTitle }}
              </div>
              <div class="goods-price">¥{{ order.price }} × {{ order.quantity }}</div>
            </div>
            <div class="total">
              <div class="label">实付款</div>
              <div class="amount">¥{{ order.totalAmount }}</div>
            </div>
          </div>

          <el-divider />

          <div class="meta-list">
            <div class="meta-row"><span class="k">订单编号</span><span class="v">{{ order.orderNo }}</span></div>
            <div class="meta-row"><span class="k">支付方式</span><span class="v">{{ payMethodLabel }}</span></div>
            <div class="meta-row"><span class="k">流水号</span><span class="v">{{ order.transactionNo || '-' }}</span></div>
            <div class="meta-row"><span class="k">下单时间</span><span class="v">{{ formatTime(order.createdAt) }}</span></div>
          </div>

          <div class="actions">
            <template v-if="order.status === 'PENDING_PAYMENT'">
              <el-button @click="onCancel">取消订单</el-button>
              <el-button type="danger" size="large" @click="payVisible = true">立即支付</el-button>
            </template>
            <template v-else-if="order.status === 'SHIPPED'">
              <el-button type="primary" size="large" @click="onConfirm">确认收货</el-button>
            </template>
            <el-button v-if="order.status !== 'PENDING_PAYMENT'" @click="$router.push('/orders')">返回订单列表</el-button>
          </div>
        </el-card>
      </template>
    </div>

    <el-dialog v-model="payVisible" title="收银台" width="420px" @closed="payMethod = 'MOCK'">
      <div class="pay-box">
        <div class="pay-amount">
          需支付 <span class="amount">¥{{ order ? order.totalAmount : '' }}</span>
        </div>
        <el-radio-group v-model="payMethod" class="pay-methods">
          <el-radio value="WECHAT" border class="pay-method">微信支付</el-radio>
          <el-radio value="ALIPAY" border class="pay-method">支付宝</el-radio>
          <el-radio value="MOCK" border class="pay-method">模拟支付</el-radio>
        </el-radio-group>
        <p class="pay-tip">当前为模拟支付，不会产生真实扣款。接入真实支付后替换 PayService 实现即可。</p>
      </div>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="danger" :loading="paying" @click="onPay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getOrder, payOrder, cancelOrder, confirmOrder } from '../api/order'
import { ORDER_STATUS_LABELS, ORDER_STATUS_TAG_TYPES } from '../utils/constants'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(false)
const payVisible = ref(false)
const paying = ref(false)
const payMethod = ref('MOCK')

const stepActive = computed(() => {
  if (!order.value) return 0
  const s = order.value.status
  if (s === 'PENDING_PAYMENT') return 0
  if (s === 'PAID') return 1
  if (s === 'SHIPPED') return 2
  if (s === 'COMPLETED') return 4
  return 0
})

const payMethodLabel = computed(() => {
  const m = order.value?.paymentMethod
  if (m === 'WECHAT') return '微信支付'
  if (m === 'ALIPAY') return '支付宝'
  if (m === 'MOCK') return '模拟支付'
  return '-'
})

async function load() {
  loading.value = true
  try {
    const res = await getOrder(route.params.id)
    order.value = res.data
  } finally {
    loading.value = false
  }
}

async function onPay() {
  paying.value = true
  try {
    await payOrder(order.value.id, payMethod.value)
    ElMessage.success('支付成功')
    payVisible.value = false
    load()
  } finally {
    paying.value = false
  }
}

async function onCancel() {
  await ElMessageBox.confirm('确定取消该订单吗？', '提示', { type: 'warning' })
  await cancelOrder(order.value.id)
  ElMessage.success('订单已取消')
  load()
}

async function onConfirm() {
  await ElMessageBox.confirm('确认已收到货？', '提示', { type: 'warning' })
  await confirmOrder(order.value.id)
  ElMessage.success('已确认收货')
  load()
}

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : ''
}

onMounted(load)
</script>

<style scoped>
.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.status-card {
  margin-bottom: 16px;
}
.seller-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.avatar {
  background: #4f6ef7;
  color: #fff;
}
.seller-name {
  font-weight: 600;
  color: #303133;
}
.goods-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
}
.goods-img {
  width: 88px;
  height: 88px;
  border-radius: 8px;
  flex-shrink: 0;
}
.img-fallback {
  width: 88px;
  height: 88px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #c0c4cc;
}
.goods-info {
  flex: 1;
}
.goods-title {
  font-size: 15px;
  color: #303133;
  cursor: pointer;
}
.goods-price {
  font-size: 13px;
  color: #909399;
  margin-top: 8px;
}
.total {
  text-align: right;
}
.total .label {
  font-size: 12px;
  color: #909399;
}
.total .amount {
  font-size: 20px;
  color: #f56c6c;
  font-weight: 700;
}
.meta-list {
  padding: 4px 0;
}
.meta-row {
  display: flex;
  gap: 20px;
  padding: 6px 0;
  font-size: 14px;
}
.meta-row .k {
  color: #909399;
  width: 80px;
}
.meta-row .v {
  color: #303133;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
}
.pay-box {
  text-align: center;
}
.pay-amount {
  font-size: 16px;
  color: #303133;
  margin-bottom: 16px;
}
.pay-amount .amount {
  font-size: 28px;
  color: #f56c6c;
  font-weight: 700;
}
.pay-methods {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}
.pay-method {
  width: 100%;
  justify-content: center;
  margin-right: 0;
}
.pay-tip {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 14px;
}
</style>
