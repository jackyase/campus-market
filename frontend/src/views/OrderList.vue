<template>
  <div class="order-page">
    <div class="container">
      <h2 class="page-title">我的订单</h2>

      <el-tabs v-model="activeStatus" @tab-change="onTabChange">
        <el-tab-pane v-for="t in tabs" :key="t.value" :name="t.value">
          <template #label>
            {{ t.label }}
            <el-badge v-if="t.value === '' ? total > 0 : counts[t.value]" :value="t.value === '' ? total : counts[t.value]" class="tab-badge" />
          </template>
        </el-tab-pane>
      </el-tabs>

      <el-empty v-if="orders.length === 0" description="暂无订单" :image-size="120" />

      <div v-else class="order-list">
        <el-card v-for="o in orders" :key="o.id" class="order-card" :body-style="{ padding: 0 }">
          <div class="order-head">
            <span>订单号 {{ o.orderNo }}</span>
            <span class="time">{{ formatTime(o.createdAt) }}</span>
            <el-tag :type="ORDER_STATUS_TAG_TYPES[o.status]" size="small">
              {{ ORDER_STATUS_LABELS[o.status] }}
            </el-tag>
          </div>
          <div class="order-body" @click="$router.push(`/orders/${o.id}`)">
            <el-image :src="o.productImage" fit="cover" class="goods-img">
              <template #error><div class="img-fallback"><el-icon><Picture /></el-icon></div></template>
            </el-image>
            <div class="goods-info">
              <div class="goods-title">{{ o.productTitle }}</div>
              <div class="goods-meta">卖家：{{ o.sellerName }}</div>
              <div class="goods-price">¥{{ o.price }} × {{ o.quantity }}</div>
            </div>
            <div class="order-total">
              <div class="amount">¥{{ o.totalAmount }}</div>
            </div>
          </div>
          <div class="order-foot">
            <span class="seller-tip">{{ sellerTip(o.status) }}</span>
            <div class="actions">
              <el-button v-if="o.status === 'PENDING_PAYMENT'" size="small" @click="onCancel(o)">取消订单</el-button>
              <el-button v-if="o.status === 'PENDING_PAYMENT'" type="danger" size="small" @click="onPay(o)">去支付</el-button>
              <el-button v-if="o.status === 'SHIPPED'" type="primary" size="small" @click="onConfirm(o)">确认收货</el-button>
              <el-button v-if="o.status === 'COMPLETED' && !o.reviewed" type="warning" size="small" @click="openReview(o)">评价</el-button>
              <el-button size="small" @click="$router.push(`/orders/${o.id}`)">详情</el-button>
            </div>
          </div>
        </el-card>
      </div>

      <div v-if="total > size" class="pager">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page + 1"
          @current-change="onPageChange"
        />
      </div>
    </div>

    <el-dialog v-model="reviewVisible" title="评价" width="480px">
      <div v-if="reviewTarget" class="review-dialog">
        <div class="review-goods">{{ reviewTarget.productTitle }}</div>
        <div class="rate-row">
          <span class="rate-label">商品评分</span>
          <el-rate v-model="reviewForm.productRating" />
        </div>
        <div class="rate-row">
          <span class="rate-label">卖家评分</span>
          <el-rate v-model="reviewForm.sellerRating" />
        </div>
        <el-input
          v-model="reviewForm.content"
          type="textarea"
          :rows="4"
          maxlength="300"
          show-word-limit
          placeholder="说说这次交易的体验～"
        />
      </div>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingReview" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getOrders, getOrderCounts, payOrder, cancelOrder, confirmOrder } from '../api/order'
import { createReview } from '../api/review'
import { ORDER_STATUS_LABELS, ORDER_STATUS_TAG_TYPES } from '../utils/constants'

const router = useRouter()
const activeStatus = ref('')
const orders = ref([])
const counts = ref({})
const total = ref(0)
const page = ref(0)
const size = 10

const tabs = [
  { label: '全部', value: '' },
  { label: '待付款', value: 'PENDING_PAYMENT' },
  { label: '待发货', value: 'PAID' },
  { label: '已发货', value: 'SHIPPED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

async function load() {
  const params = { page: page.value, size }
  if (activeStatus.value) params.status = activeStatus.value
  const res = await getOrders(params)
  orders.value = res.data.list
  total.value = res.data.total
}

async function loadCounts() {
  const res = await getOrderCounts()
  counts.value = res.data
}

function onTabChange() {
  page.value = 0
  load()
}

function onPageChange(p) {
  page.value = p - 1
  load()
}

async function onPay(o) {
  router.push(`/orders/${o.id}`)
}

async function onCancel(o) {
  await ElMessageBox.confirm('确定取消该订单吗？', '提示', { type: 'warning' })
  await cancelOrder(o.id)
  ElMessage.success('订单已取消')
  load()
  loadCounts()
}

async function onConfirm(o) {
  await ElMessageBox.confirm('确认已收到货？', '提示', { type: 'warning' })
  await confirmOrder(o.id)
  ElMessage.success('已确认收货')
  load()
  loadCounts()
}

function sellerTip(status) {
  if (status === 'PAID') return '等待卖家发货'
  if (status === 'SHIPPED') return '商品已发出，注意查收'
  if (status === 'COMPLETED') return '交易完成'
  if (status === 'CANCELLED') return '订单已取消'
  return ''
}

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : ''
}

const reviewVisible = ref(false)
const reviewTarget = ref(null)
const submittingReview = ref(false)
const reviewForm = reactive({ productRating: 5, sellerRating: 5, content: '' })

function openReview(o) {
  reviewTarget.value = o
  reviewForm.productRating = 5
  reviewForm.sellerRating = 5
  reviewForm.content = ''
  reviewVisible.value = true
}

async function submitReview() {
  if (!reviewForm.productRating || !reviewForm.sellerRating) {
    ElMessage.warning('请完成评分')
    return
  }
  submittingReview.value = true
  try {
    await createReview({
      orderId: reviewTarget.value.id,
      productRating: reviewForm.productRating,
      sellerRating: reviewForm.sellerRating,
      content: reviewForm.content
    })
    ElMessage.success('评价成功')
    reviewVisible.value = false
    load()
    loadCounts()
  } finally {
    submittingReview.value = false
  }
}

onMounted(() => {
  load()
  loadCounts()
})
</script>

<style scoped>
.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 16px;
}
.tab-badge {
  margin-left: 4px;
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
  gap: 16px;
  padding: 10px 16px;
  font-size: 13px;
  color: #909399;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}
.order-head .time {
  margin-left: auto;
}
.order-body {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  cursor: pointer;
}
.goods-img {
  width: 72px;
  height: 72px;
  border-radius: 6px;
  flex-shrink: 0;
}
.img-fallback {
  width: 72px;
  height: 72px;
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
  font-weight: 600;
}
.goods-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}
.goods-price {
  font-size: 13px;
  color: #606266;
  margin-top: 6px;
}
.order-total {
  text-align: right;
}
.amount {
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
.seller-tip {
  font-size: 13px;
  color: #909399;
}
.actions {
  display: flex;
  gap: 8px;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.review-goods {
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}
.rate-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.rate-label {
  color: #606266;
  width: 60px;
}
</style>
