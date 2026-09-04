<template>
  <div class="detail-page">
    <div class="container" v-loading="loading">
      <el-card v-if="product" :body-style="{ padding: 0 }">
        <div class="detail">
          <div class="gallery">
            <el-image v-if="mainImage" :src="mainImage" fit="contain" class="main-img" />
            <div v-else class="main-img img-fallback">
              <el-icon :size="48"><Picture /></el-icon>
            </div>
            <div v-if="product.images.length > 1" class="thumbs">
              <img
                v-for="(img, i) in product.images"
                :key="i"
                :src="img"
                class="thumb"
                :class="{ active: img === mainImage }"
                @click="mainImage = img"
              />
            </div>
          </div>

          <div class="info">
            <div class="price-line">
              <span class="price">¥{{ product.price }}</span>
              <span v-if="product.originalPrice" class="original">¥{{ product.originalPrice }}</span>
            </div>
            <h1 class="title">{{ product.title }}</h1>
            <div class="tags">
              <el-tag :type="CONDITION_TAG_TYPES[product.condition]" size="small">
                {{ CONDITION_LABELS[product.condition] }}
              </el-tag>
              <el-tag type="info" size="small">{{ product.categoryName }}</el-tag>
              <el-tag type="info" size="small" effect="plain">{{ STATUS_LABELS[product.status] }}</el-tag>
            </div>
            <div class="stats">
              <span>浏览量 {{ product.viewCount }}</span>
              <span>库存 {{ product.stock }}</span>
            </div>
            <div class="seller-box" @click="$router.push(`/user/${product.sellerId}`)">
              <el-avatar :size="40" class="avatar">{{ (product.sellerName || 'U')[0] }}</el-avatar>
              <div class="seller-mid">
                <span class="seller-name">{{ product.sellerName }}</span>
                <div v-if="sellerRatingCount" class="seller-rating">
                  <el-rate :model-value="Math.round(sellerRating)" disabled size="small" />
                  <span class="rating-num">{{ sellerRating.toFixed(1) }}</span>
                </div>
              </div>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </div>
            <div class="actions">
              <el-button size="large" @click="onChat">联系卖家</el-button>
              <el-button size="large" @click="onCart">加入购物车</el-button>
              <el-button type="danger" size="large" @click="onBuy">立即购买</el-button>
            </div>
          </div>
        </div>

        <el-divider style="margin: 0" />
        <div class="desc">
          <h3>商品描述</h3>
          <p v-if="product.description" class="desc-text">{{ product.description }}</p>
          <el-empty v-else description="卖家没有填写描述" :image-size="80" />
        </div>

        <div class="desc reviews">
          <h3>商品评价 ({{ reviews.length }})</h3>
          <div v-if="reviews.length" class="review-list">
            <div v-for="r in reviews" :key="r.id" class="review-item">
              <el-avatar :size="32" :src="r.reviewerAvatar">{{ (r.reviewerName || 'U')[0] }}</el-avatar>
              <div class="review-main">
                <div class="review-head">
                  <span class="review-name">{{ r.reviewerName }}</span>
                  <el-rate :model-value="r.productRating" disabled size="small" />
                </div>
                <div class="review-content">{{ r.content || '（无文字评价）' }}</div>
                <div class="review-time">{{ formatTime(r.createdAt) }}</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无评价" :image-size="80" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getProduct } from '../api/product'
import { addCart } from '../api/cart'
import { createOrder } from '../api/order'
import { getPublicProfile } from '../api/user'
import { getProductReviews } from '../api/review'
import { useUserStore } from '../store/user'
import { CONDITION_LABELS, CONDITION_TAG_TYPES, STATUS_LABELS } from '../utils/constants'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const product = ref(null)
const mainImage = ref('')
const loading = ref(false)
const reviews = ref([])
const sellerRating = ref(0)
const sellerRatingCount = ref(0)

async function load() {
  loading.value = true
  try {
    const res = await getProduct(route.params.id)
    product.value = res.data
    mainImage.value = res.data.images[0] || ''
    const [sellerRes, reviewsRes] = await Promise.all([
      getPublicProfile(res.data.sellerId),
      getProductReviews(res.data.id)
    ])
    sellerRating.value = sellerRes.data.sellerRating || 0
    sellerRatingCount.value = sellerRes.data.sellerRatingCount || 0
    reviews.value = reviewsRes.data
  } finally {
    loading.value = false
  }
}

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : ''
}

function requireLogin() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

async function onCart() {
  if (!requireLogin()) return
  await addCart(product.value.id, 1)
  ElMessage.success('已加入购物车')
}

async function onBuy() {
  if (!requireLogin()) return
  const res = await createOrder({ productId: product.value.id, quantity: 1 })
  ElMessage.success('下单成功')
  router.push(`/orders/${res.data.id}`)
}

function onChat() {
  if (!requireLogin()) return
  router.push(`/chat?with=${product.value.sellerId}`)
}

onMounted(load)
</script>

<style scoped>
.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.detail {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  padding: 24px;
}
.gallery .main-img {
  width: 100%;
  height: 360px;
  background: #f5f6f8;
  border-radius: 10px;
}
.img-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}
.thumbs {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}
.thumb {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  border: 2px solid transparent;
}
.thumb.active {
  border-color: #4f6ef7;
}
.info {
  display: flex;
  flex-direction: column;
}
.price-line {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.price {
  color: #f56c6c;
  font-size: 32px;
  font-weight: 700;
}
.original {
  color: #c0c4cc;
  font-size: 16px;
  text-decoration: line-through;
}
.title {
  font-size: 20px;
  color: #303133;
  margin: 14px 0;
  line-height: 1.4;
}
.tags {
  display: flex;
  gap: 8px;
}
.stats {
  display: flex;
  gap: 24px;
  color: #909399;
  font-size: 13px;
  margin: 14px 0;
}
.seller-box {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: #f8f9fb;
  border-radius: 8px;
  margin-bottom: 20px;
}
.avatar {
  background: #4f6ef7;
  color: #fff;
}
.seller-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.actions .el-button {
  flex: 1;
}
.hint {
  margin-top: 12px;
  font-size: 12px;
  color: #c0c4cc;
}
.desc {
  padding: 24px;
}
.desc h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 12px;
}
.desc-text {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}
.seller-box {
  cursor: pointer;
}
.seller-mid {
  flex: 1;
}
.seller-rating {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 2px;
}
.rating-num {
  color: #f56c6c;
  font-size: 12px;
}
.arrow {
  color: #c0c4cc;
}
.reviews {
  border-top: 1px solid #f0f0f0;
}
.review-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.review-item {
  display: flex;
  gap: 10px;
}
.review-main {
  flex: 1;
}
.review-head {
  display: flex;
  align-items: center;
  gap: 10px;
}
.review-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}
.review-content {
  color: #606266;
  margin-top: 6px;
  line-height: 1.6;
  font-size: 14px;
}
.review-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 6px;
}
@media (max-width: 768px) {
  .detail {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 16px;
  }
  .gallery .main-img {
    height: 280px;
  }
}
</style>
